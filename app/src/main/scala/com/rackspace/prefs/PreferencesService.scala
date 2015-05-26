package com.rackspace.prefs

import com.nparry.orderly._
import com.rackspace.prefs.model.DBTables._
import com.rackspace.prefs.model.{DBTables, Preferences, PreferencesMetadata}
import org.joda.time.DateTime
import org.json4s.{JValue, JNothing, DefaultFormats, Formats}
import org.json4s.JsonDSL.WithDouble._
import org.scalatra._
import org.scalatra.json._
import org.scalatra.scalate.ScalateSupport
import org.slf4j.LoggerFactory
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.web.util.UriUtils

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.util.control.Breaks._
import collection.JavaConverters._
import javax.servlet.http.HttpServletRequest

case class PreferencesService(db: Database) extends ScalatraServlet
with ScalateSupport
with JacksonJsonSupport {

    protected implicit val jsonFormats: Formats = DefaultFormats

    val X_TENANT_ID = "x-tenant-id"

    val logger =  LoggerFactory.getLogger(getClass)

    get("/") {
        NotFound(jsonifyError("Invalid URI: /"))
    }

    get("/status") {
        db.withSession { implicit session =>
           val metadataCount = Query(preferencesMetadata.length).first
           jsonifyStatus(metadataCount)
        }
    }

    get("/metadata/:preference_slug/?") {
        val preferenceSlug = params("preference_slug")
        contentType = formats("json")
        getMetadata(preferenceSlug) match {
            case Some(metadata: PreferencesMetadata) => metadata.schema
            case None => NotFound(jsonifyError("Metadata preferences for /" + preferenceSlug + " not found"))
        }
    }

    // anything that's not /metadata* goes here
    get( """^/(?!metadata)([^/]*)/([^/]*)/?$""".r) {
        val uriParts = multiParams("captures")
        val preferenceSlug = uriParts(0)
        val id = uriParts(1)
        contentType = formats("json")

        db.withSession { implicit session =>

            val getPayloadQuery = for {
                (prefs, metadata) <- preferences innerJoin preferencesMetadata on (_.preferencesMetadataId === _.id)
                if prefs.id === id && metadata.slug === preferenceSlug
            } yield (prefs.payload)

            getPayloadQuery.list match {
                case List(payload: String) => payload
                case _ => NotFound(jsonifyError("Preferences for " + preferenceSlug + " with id " + id + " not found"))
            }
        }
    }

    post("/:preference_slug/:id/?", request.getContentType() == "application/json") {
        val preferenceSlug = params("preference_slug")
        val id = params("id")
        val payload = request.body

        getMetadata(preferenceSlug) match {
            case Some(metadata: PreferencesMetadata) => {
                val orderly = Orderly(metadata.schema)

                orderly.validate(payload) match {
                    case head :: tail =>
                        // give them hints of what's wrong. Only print the first violation.
                        BadRequest(jsonifyError("Preferences for /" + preferenceSlug + "/" + id +
                            " does not validate properly. " + head.path + " " + head.message))

                    case Nil => {
                        // valid and non-empty json, write to db
                        validateAndWritePreference(metadata, preferenceSlug, id, payload)
                    }
                }
            }
            case None => BadRequest(jsonifyError("Preferences for /" + preferenceSlug + " does not have any metadata"))
        }
    }

  /**
   * Validate the preference json payload and write to the database
   * @param metadata
   * @param preferenceSlug
   * @param id
   * @param payload
   * @return
   */
    def validateAndWritePreference(metadata: PreferencesMetadata, preferenceSlug: String, id: String, payload: String): ActionResult = {
        // validate payload
        val jsonContent = parse(payload)

        // validate default_archive_container_url
        val defaultContainer = jsonContent \ "default_archive_container_url"
        var validateError = validateContainer(preferenceSlug, id, defaultContainer)

        //validate urls of archive_container_urls if defaultContainer is ok
        if (validateError == null) {
            val archiveContainers = (jsonContent \ "archive_container_urls").children
            breakable {
                archiveContainers.foreach { container =>
                    // validate and break when first validation failure occurred
                    validateError = validateContainer(preferenceSlug, id, container)
                    if (validateError != null) break
                }
            }
        }

        // if container urls and names are ok, validate that either default container is provided
        // or all datacenters container urls are provided
        if (validateError == null) {
            if ((defaultContainer == JNothing) && (!allDataCenterArePresent(preferenceSlug, id, jsonContent))) {
                // default container was not provided, and not all data centers container urls are provided , bad request
                validateError = BadRequest(jsonifyError("Preferences for /" + preferenceSlug + "/" + id + " must have a default_container_url or must have all datacenter archive_container_urls present." +
                    " See Cloud Feeds documentation for a list of valid datacenters."))
            }
        }

        // write to db if content pass validation
        if (validateError != null) { validateError }
        else { writePreferenceToDb(metadata, id, payload) }
    }

  /**
   * true if all data centers are present, false otherwise
   * @param preferenceSlug
   * @param id
   * @param preferenceJson
   * @return
   */
  def allDataCenterArePresent(preferenceSlug: String, id: String, preferenceJson: JValue): Boolean = {
      // extract "archive_container_urls": { datacenter: url } to Map(String, Any)
      val containerUrls = (preferenceJson \ "archive_container_urls").extract[Map[String, Any]]
      // check for all data centers and return boolean
      containerUrls.contains("iad") &&
      containerUrls.contains("dfw") &&
      containerUrls.contains("ord") &&
      containerUrls.contains("lon") &&
      containerUrls.contains("hkg") &&
      containerUrls.contains("syd")
  }

  /**
   * Validate that the container url is valid and that the container name is valid
   * @param preferenceSlug
   * @param id
   * @param container
   * @return
   */
    def validateContainer(preferenceSlug: String, id: String, container: JValue): ActionResult = {
        var result:ActionResult = null

        if (container != JNothing) {
            val validator = new UrlValidator()
            val containerUrl = container.extract[String]
            if (!validator.isValid(containerUrl)) {
                // validate url
                result = BadRequest(jsonifyError("Preferences for /" + preferenceSlug + "/" + id + " has an invalid url: " + containerUrl))
            }
            else {
                // validate container name in the url
                result = validateContainerName(preferenceSlug, id, containerUrl)
            }
        }
        result
    }

  /**
   * Cloud files has the following requirements for container name. This method validates to make sure the container name is compatible 
   * with cloud files. 
   * 
   * The only restrictions on container names is that they cannot contain a forward slash (/) and must be less than 256 bytes in length. 
   * Note that the length restriction applies to the name after it has been URL-encoded. For example, a container name of Course Docs 
   * would be URL-encoded as Course%20Docs and is therefore 13 bytes in length rather than the expected 11.
   * 
   * http://docs.rackspace.com/files/api/v1/cf-devguide/content/Containers-d1e458.html
   *
   * @param preferenceSlug
   * @param id
   * @param containerUrl
   * @return
   */
    def validateContainerName(preferenceSlug: String, id: String, containerUrl: String): ActionResult = {
        // validate container name
        var result:ActionResult = null

        // this pattern will match url of format http[s]://hostname/rootpath/nastId/container_name, and capture container_name
        val patternForContainer = "^https?://[^/]+/[^/]+/[^/]+/(.*)$".r
        val containerName = {
            patternForContainer.findFirstMatchIn(containerUrl) match {
                case Some(m) => m.group(1).replaceAll("/$", "")   // get first captured group and remove trailing slash if present
                case None => ""
            }
        }

        if (containerName == "") {
            // container name cannot be empty
            result = BadRequest(jsonifyError("Preferences for /" + preferenceSlug + "/" + id + " is missing container name: " + containerUrl))
        }
        else {

            if (containerName.length() >= 256) {
                logger.debug(s"Encoded container name should be less than 256 bytes in length:[$containerUrl]")

                result = BadRequest(jsonifyError("Preferences for /" + preferenceSlug + "/" + id + " has an encoded container name longer than 255 bytes: " + containerUrl +
                  "\nUrl must be encoded and should not contain query parameters or url fragments. Encoded container name cannot contain a forward slash(/) and must be less than 256 bytes in length."))
            } else {

                // container name must be less than 256 bytes in length, url encoded, and does not contain '/'
                val msgInvalidUrl =
                    "Preferences for /" + preferenceSlug + "/" + id + " has an invalid url: " + containerUrl +
                    "\nUrl must be encoded and should not contain query parameters or url fragments. Encoded container name cannot contain a forward slash(/) and must be less than 256 bytes in length."

                try {

                    // check to see if container has special chars and url encoded
                    // first decode the containerName
                    val decoded = UriUtils.decode(containerName, "UTF-8")

                    //If decoding results with the same container name, either it hasnt been encoded or encoding doesnt actually change anything (Ex: a alpha-numeric string like "Tom")
                    if (containerName == decoded) {

                        //To make sure whether encoding changes anything
                        val encode = UriUtils.encodePathSegment(containerName, "UTF-8")

                        // if encoding the container name isn't same as the original, then container has special chars that are not encoded, bad request
                        if (encode != containerName) {
                            logger.debug(s"Encoding the container name isn't same as the original:[$containerUrl]")
                            result = BadRequest(jsonifyError(msgInvalidUrl))
                        }

                    } else {
                        //Since decoded container name is not same as the original, we can think container name is probably already encoded.
                        //But they could have send mixed case where only part of the container name is encoded. So decoding results in a different container
                        //name but that doesnt mean the entire container name is properly encoded.

                        //Removing any hex-characters from original.
                        //Encoding the resultant string should not change it. If it changes, it indicates there are still special chars.

                        val hexStrippedContainerName = containerName.replaceAll("%[a-fA-F0-9][a-fA-F0-9]", "")
                        val encodedHexStrippedContainerName = UriUtils.encodePathSegment(hexStrippedContainerName, "UTF-8")

                        if (hexStrippedContainerName != encodedHexStrippedContainerName) {
                            logger.debug(s"mixed case(partially encoded) container name:[$containerUrl]")
                            // if encoding the container name isn't the same as the original, then container has special chars that are not encoded, bad request
                            result = BadRequest(jsonifyError(msgInvalidUrl))
                        }

                        if (decoded contains '/') {
                            logger.debug(s"Container name contains forward slash(/) which is invalid:[$containerUrl]")
                            // containerName contains '/', bad request
                            result = BadRequest(jsonifyError("Preferences for /" + preferenceSlug + "/" + id + " has an invalid container name containing '/': " + containerUrl +
                                "\nUrl must be encoded and should not contain query parameters or url fragments."))
                        }
                    }
                }
                catch {
                    case e: Exception => result = BadRequest(jsonifyError(msgInvalidUrl + "\nException: " + e.getMessage()))
                }
            }
        }
        result
    }

  /**
   * Write the preference json to the database
   *
   * @param metadata
   * @param id
   * @param payload
   * @return
   */
    def writePreferenceToDb(metadata: PreferencesMetadata, id: String, payload: String): ActionResult = {
        db.withSession { implicit session =>
            val prefsForIdandSlug = preferences.filter(prefs => prefs.id === id && prefs.preferencesMetadataId === metadata.id)

            prefsForIdandSlug.list match {
                case List(_: Preferences) => {
                    preferences
                      .filter(prefs => prefs.id === id && prefs.preferencesMetadataId === metadata.id)
                      .map(prefs => (prefs.payload, prefs.updated))
                      .update(payload, DateTime.now)

                    Ok()
                }
                case _ => {
                    preferences
                      .map(p => (p.id, p.preferencesMetadataId, p.payload, p.alternateId))
                      .insert(id, metadata.id.get, payload, getAlternateId(request))

                    Created()
                }
            }
        }
    }

    def getMetadata(slug: String): Option[PreferencesMetadata] = {
        db.withSession { implicit session =>
            preferencesMetadata.filter(_.slug === slug).list match {
                case List(metadata: PreferencesMetadata) => Some(metadata)
                case _ => None
            }
        }
    }

    def getAlternateId(request: HttpServletRequest): Option[String] = {
        // this is a temporary hack, until something better comes along
        // to find which one is the NAST tenantId, we pick the longest string
        request.getHeaders(X_TENANT_ID).asScala.reduceLeftOption((str1: String, str2: String) => if (str1.length > str2.length) str1 else str2) match {
            case Some(tenant) => {
                logger.debug("For request " + request.getRequestURI + ", alternateId is " + tenant)
                Some(tenant)
            }
            case _            => None
        }
    }

    def jsonifyError(errorMessage: String) : String = {
        val json = ("error" -> errorMessage)
        return pretty(render(json))
    }

    def jsonifyStatus(metadataCount: Int) : String = {
        "{ \"metadata-count\": " + metadataCount + " }"
    }

    error {
        case e => {
            logger.error("Request failed with exception", e)
            InternalServerError(jsonifyError("Request failed with exception:" + e + " message:" + e.getMessage))
        }
    }
}
