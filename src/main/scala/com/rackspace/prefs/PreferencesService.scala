package com.rackspace.prefs

import com.rackspace.prefs.model.ResourceTypeMetadata
import com.rackspace.prefs.model.AttributeMetadata
import com.rackspace.prefs.model.ResourceInstance
import com.rackspace.prefs.model.DBTables._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.JdbcDriver.simple._
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

    def dbSetup {
        db withDynSession {
            (resourceTypes.ddl ++ resourceAttributes.ddl ++ resources.ddl).create

            resourceTypes.insert("archive_prefs", "Archive Preferences", "cloud_account_id")

            //(id, resourceType, key, valueType, use, validation)
            resourceAttributes.insertAll(
                (1, "archive_prefs", "archiving_state", "String", "optional", "in (enabled, disabled)"),
                (2, "archive_prefs", "data_format", "String", "optional", "in (xml, json, xml+json)")
            )

            //(resourceType, id ,payload)
            resources.insert("archive_prefs", "tenant_1", "{\"todo\": \"add JSON goodness\"}")
        }
    }

    get("/") {
        <html>
            <body>
                Hello World! Hello Universe!
                {if (isDevelopmentMode) <h3>Dev Mode</h3>}
            </body>
        </html>
    }

    get("/metadata") {
        contentType = formats("json")
        db withDynSession {
            resourceTypes.run.map(m => ResourceTypeMetadata(m._1, m._2, m._3))
        }
    }

    get("/metadata/:resource_type") {
        val resource_type = params("resource_type")
        contentType = formats("json")
        db withDynSession {
            resourceAttributes.filter(_.resourceType === resource_type).run.map(m =>
                AttributeMetadata(m._3, m._4, m._5, m._6)
            )
        }
    }

    get("/archive_prefs/:id") {
        val id = params("id")
        db withDynSession {
            resources.filter(_.id === id).run.map(m => m._3).head
        }
    }

    post("/:resourceType/:id", request.getContentType() == "application/json") {
        val resourceType = params("resourceType")
        val id = params("id")
        val payload = request.body

        validate(payload)

        db withDynSession {
            if (resources.filter(_.id === id).run.isEmpty)
                resources.insert(resourceType, id, payload)
            else
                resources
                    .filter(r => r.resourceType === resourceType && r.id === id)
                    .map(r => (r.payload))
                    .update((payload))
        }
        //val instance: ResourceInstance = parsedBody.extract[ResourceInstance]
        //resources.insertOrUpdate( (instance.resourceType, instance.id, instance.payload) )
    }

    get("/setup") {
        dbSetup
    }

    def validate(payload: String) {
//        val jsonNode = objMapper.readTree(payload)
//        schema.validate(jsonNode)
        orderly.validate(payload)
    }

}
