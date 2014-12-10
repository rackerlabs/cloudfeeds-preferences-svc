package com.rackspace.prefs

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.model.{Preferences, PreferencesMetadata, InitDbTrait}
import org.junit.runner.RunWith
import org.scalatest.FunSuiteLike
import org.scalatest.junit.JUnitRunner
import org.scalatra.test.scalatest._

import scala.slick.jdbc.JdbcBackend.Database
import com.rackspace.prefs.model.DBTables._
import scala.io.Source

/**
 * User: shin4590
 * Date: 12/4/14
 */
@RunWith(classOf[JUnitRunner])
class PreferencesServiceTest extends ScalatraSuite with FunSuiteLike with InitDbTrait {

    val db = Database.forDataSource(new ComboPooledDataSource)
    addServlet(new PreferencesService(db), "/*")

    val schema = Source.fromInputStream(getClass().getResourceAsStream("/feeds_archives.schema.orderly")).getLines().mkString

    override def beforeAll {
        super.beforeAll
        createSchema(db)
        initMetaData(db, schema)
    }

    test("get of /") {
        get("/") {
            status should equal (200)
        }
    }

    test("get of /metadata") {
        get("/metadata") {
            println(body)
            status should equal (200)
        }
    }

    test("get of /metadata/archive_prefs") {
        get("/metadata/archive_prefs") {
            println(body)
            status should equal (200)
        }
    }

    test("get of /archive_prefs/1") {
        createPreference(db, "1", "archive_prefs",
            """
              |{
              |  "enabled": true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_containers": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin)
        get("/archive_prefs/1") {
            println(body)
            status should equal (200)
        }
    }

    test("post of a good preferences to /archive_prefs/100") {
        post("/archive_prefs/100",
            """
              |{
              |  "enabled": true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_containers": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            println(body)
            status should equal (201)
        }
    }
}
