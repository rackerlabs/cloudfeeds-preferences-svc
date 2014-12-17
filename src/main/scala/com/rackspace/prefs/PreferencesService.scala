package com.rackspace.prefs

import com.nparry.orderly._
import com.rackspace.prefs.model.DBTables._
import com.rackspace.prefs.model.{Preferences, PreferencesMetadata}
import org.joda.time.DateTime
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import org.scalatra.scalate.ScalateSupport

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

case class PreferencesService(db: Database) extends ScalatraServlet
with ScalateSupport
with JacksonJsonSupport {

    protected implicit val jsonFormats: Formats = DefaultFormats

    get("/") {
        NotFound("Invalid URI: /")
    }

    get("/metadata/:preference_slug") {
        val preferenceSlug = params("preference_slug")
        contentType = formats("json")
        getMetadata(preferenceSlug) match {
            case Some(metadata: PreferencesMetadata) => metadata.schema
            case None => NotFound("Metadata preferences for /" + preferenceSlug + " not found")
        }
    }

    // anything that's not /metadata* goes here
    get("""^/(?!metadata)(.*)/(.*)""".r) {
        val uriParts = multiParams("captures")
        val preferenceSlug = uriParts(0)
        val id = uriParts(1)
        contentType = formats("json")

        db withDynSession {

          val getPayloadQuery = for {
              (prefs, metadata) <- preferences innerJoin preferencesMetadata on (_.preferencesMetadataId === _.id)
              if prefs.id === id && metadata.slug === preferenceSlug
          } yield (prefs.payload)

          getPayloadQuery.list match {
            case List(payload: String) => payload
            case _ => NotFound("Preferences for " + preferenceSlug + " with id " + id + " not found")
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
                  BadRequest("Preferences for /" + preferenceSlug + "/" + id +
                    " does not validate properly. " + head.path + " " + head.message)

                case Nil => {

                  db withDynSession {
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
                          .map(p => (p.id, p.preferencesMetadataId, p.payload))
                          .insert(id, metadata.id.get, payload)

                        Created("")
                      }
                    }
                  }
                }
            }
        }
        case None => BadRequest("Preferences for /" + preferenceSlug + " does not have any metadata")
      }
    }

    def getMetadata(slug: String) : Option[PreferencesMetadata] = {
      db withDynSession {
        preferencesMetadata.filter(_.slug === slug).list match {
            case List(metadata: PreferencesMetadata) => Some(metadata)
            case _ => None
        }
      }
    }
}
