package com.rackspace.prefs

import com.nparry.orderly._
import com.rackspace.prefs.model.DBTables._
import com.rackspace.prefs.model.{Preferences, PreferencesMetadata}
import org.joda.time.DateTime
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import org.scalatra.scalate.ScalateSupport

import collection.JavaConverters._
import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory

case class PreferencesService(db: Database) extends ScalatraServlet
with ScalateSupport
with JacksonJsonSupport {

    protected implicit val jsonFormats: Formats = DefaultFormats

    val X_TENANT_ID = "x-tenant-id"

    val logger =  LoggerFactory.getLogger(getClass)

    get("/") {
        NotFound(jsonify("Invalid URI: /"))
    }

    get("/metadata/:preference_slug") {
        val preferenceSlug = params("preference_slug")
        contentType = formats("json")
        getMetadata(preferenceSlug) match {
            case Some(metadata: PreferencesMetadata) => metadata.schema
            case None => NotFound(jsonify("Metadata preferences for /" + preferenceSlug + " not found"))
        }
    }

    // anything that's not /metadata* goes here
    get( """^/(?!metadata)(.*)/(.*)""".r) {
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
                case _ => NotFound(jsonify("Preferences for " + preferenceSlug + " with id " + id + " not found"))
            }
        }
    }

    post("/:preference_slug/:id", request.getContentType() == "application/json") {

        val preferenceSlug = params("preference_slug")
        val id = params("id")
        val payload = request.body

        getMetadata(preferenceSlug) match {
            case Some(metadata: PreferencesMetadata) => {
                val orderly = Orderly(metadata.schema)

                orderly.validate(payload) match {
                    case head :: tail =>
                        // give them hints of what's wrong. Only print the first violation.
                        BadRequest(jsonify("Preferences for /" + preferenceSlug + "/" + id +
                            " does not validate properly. " + head.path + " " + head.message))

                    case Nil => {

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
                }
            }
            case None => BadRequest(jsonify("Preferences for /" + preferenceSlug + " does not have any metadata"))
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

    def jsonify(errorMessage: String) : String = {
        "{ \"error\": \"" + errorMessage + "\" }"
    }
}
