package com.rackspace.prefs

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
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jackson.JsonLoader
import com.fasterxml.jackson.databind.{ObjectMapper, JsonNode}
import com.nparry.orderly._
import net.liftweb.json.JsonAST._
import scala.io.Source

case class PreferencesService(db: Database) extends ScalatraServlet
with ScalateSupport
with JacksonJsonSupport {

    protected implicit val jsonFormats: Formats = DefaultFormats

    // hard code for now, eventually needs to load from DB
    val objMapper = new ObjectMapper()
    val schemaObj = JsonLoader.fromResource("/feeds_archives.schema.json")
    val schemaFactory = JsonSchemaFactory.byDefault()
    val schema = schemaFactory.getJsonSchema(schemaObj)

    // using Lift and Orderly
    val orderly = Orderly(Source.fromInputStream(getClass().getResourceAsStream("/feeds_archives.schema.orderly")).getLines().mkString)

    get("/") {
        <html><body>
            Preferences Service!
            {if (isDevelopmentMode) <h3>Dev Mode</h3>}
        </body></html>
    }

    // TODO: what should we return here?
    // Ideally it should present a list of links to fetch individual metadata.
    // But what json format should it be?
    get("/metadata") {
        contentType = formats("json")
        db withDynSession { preferencesMetadata.list.map(m => (m.slug, m.schema)) }
    }

    get("/metadata/:preference_type") {
        val preferenceType = params("preference_type")
        contentType = "application/schema+json"
        db withDynSession {
            val result = preferencesMetadata.filter(_.slug === preferenceType).run
            if ( result.length == 1 ) {
                result(0).schema
            } else {
                NotFound("Metadata preferences for " + preferenceType + " not found")
            }
        }
    }

    // anything that's not /metadata* goes here
    get("""^/(?!metadata)(.*)/(.*)""".r) {
        val uriParts = multiParams("captures")
        val preferenceType = uriParts(0)
        val id = uriParts(1)
        db withDynSession {
            val result = preferences.filter( prefs => prefs.id === id && prefs.preferencesMetadataSlug === preferenceType).run
            if ( result.length == 1 ) {
                result(0).payload
            } else {
                NotFound("Preferences for " + preferenceType + " with id " + id + " not found")
            }
        }
    }

    post("/:preference_slug/:id", request.getContentType() == "application/json") {
        val preferenceSlug = params("preference_slug")
        val id = params("id")
        val payload = request.body

        val violations = orderly.validate(payload)
        if ( violations.length == 0 ) {
            db withDynSession {
                if (preferences.filter(_.id === id).run.isEmpty)
                    preferences.map(p => (p.id, p.preferencesMetadataSlug, p.payload))
                               .insert(id, preferenceSlug, payload)
                else
                    preferences
                        .filter(r => r.preferencesMetadataSlug === preferenceSlug && r.id === id)
                        .map(r => (r.payload, r.updated))
                        .update((payload, DateTime.now()))
            }

            Created("")
        } else {
            BadRequest("Preferences /${preferenceSlug}/${id} does not validate properly")
        }
    }
}
