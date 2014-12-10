package com.rackspace.prefs

import com.rackspace.prefs.model.DBTables._
import com.rackspace.prefs.model.{Preferences, PreferencesMetadata}
import org.joda.time.DateTime
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{Ok, Created, NotFound, ScalatraServlet}
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
            if ( result.length != 1 ) {
                NotFound("Metadata preferences for " + preferenceType + " not found")
            } else {
                result(0).schema
            }
        }
    }

    get("/archive_prefs/:id") {
        //val preferenceType = params("preference_type")
        val preferenceType = "archive_prefs"
        val id = params("id")
        db withDynSession {
            val result = preferences.filter( prefs => prefs.id === id && prefs.preferencesMetadataSlug === preferenceType).run
            result(0)
        }
    }

    post("/:preference_slug/:id", request.getContentType() == "application/json") {
        val preferenceSlug = params("preference_slug")
        val id = params("id")
        val payload = request.body

        val violations = validate(payload)

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
    }

    def validate(payload: String) {
//        val jsonNode = objMapper.readTree(payload)
//        schema.validate(jsonNode)
        orderly.validate(payload)
    }
}
