package com.rackspace.prefs

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSuiteLike
import com.rackspace.prefs.model.InitDbTrait
import scala.io.Source
import scala.slick.jdbc.JdbcBackend._
import scala.util.Random
import com.mchange.v2.c3p0.ComboPooledDataSource

/**
 * User: shin4590
 * Date: 12/4/14
 */
@RunWith(classOf[JUnitRunner])
class FeedsArchivePreferencesTest extends ScalatraSuite with FunSuiteLike with InitDbTrait {

    val db = Database.forDataSource(new ComboPooledDataSource)
    addServlet(new PreferencesService(db), "/*")

    override def beforeAll {
        super.beforeAll
        clearData(db)
    }

    test("should get 200: GET /archive_prefs/:id") {
        // declaring this randomId outside test() as a var
        // doesn't work with subsequent tests :-(
        val randomId = Random.nextInt()
        val prefs = """
                      |{
                      |  "enabled": true,
                      |  "data_format" : [ "JSON", "XML" ],
                      |  "default_container_name" : "FeedsArchives",
                      |  "archive_container_urls": {
                      |      "iad": "http://...",
                      |      "dfw": "http://...",
                      |      "ord": "http://...",
                      |      "lon": "http://...",
                      |      "hkg": "http://...",
                      |      "syd": "http://..."
                      |  }
                      |}
                    """.stripMargin
        createPreference(db, randomId.toString(), "archive", prefs)

        info("Calling GET /archive/" + randomId)
        get("/archive/" + randomId) {
            status should equal (200)
            body should equal (prefs)
            response.mediaType should equal (Some("application/json"))
        }
    }

    test("should get 404: GET on nonexistent preferences /nonexistent/:id") {
        val randomId = Random.nextInt()
        info("Calling GET /nonexistent/" + randomId)
        get("/nonexistent/" + randomId) {
            status should equal (404)
        }
    }

    test("should get 404: GET on preferences without id /archive") {
        info("Calling GET /archive")
        get("/archive") {
            status should equal (404)
        }
    }

    test("should get 201: POST of a new good preferences to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        val prefs =
            """
              |{
              |  "enabled": true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin
        post("/archive/" + randomId, prefs, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }
    }

    test("should get 201: POST of a new preferences with only one data_format to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        val prefs =
            """
              |{
              |  "enabled": true,
              |  "data_format" : [ "JSON" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin
        post("/archive/" + randomId, prefs, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }
    }

    test("should get 200: POST of a good preferences to existing /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling 1st POST /archive/" + randomId)
        val prefs =
            """
              |{
              |  "enabled": false,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin
        post("/archive/" + randomId, prefs, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }

        info("Calling 2nd POST /archive/" + randomId)
        post("/archive/" + randomId,
            """
              |{
              |  "enabled": true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            if ( status != 200 ) {
                info(body)
            }
            status should equal (200)
        }
    }

    test("should get 400: POST of preferences with missing required field to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId,
            """
              |{
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("missing and is not optional")
        }
    }

    test("should get 400: POST of preferences with additional bogus field to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId,
            """
              |{
              |  "enabled" : false,
              |  "some_unknown_field": "asdfas",
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("is not defined in the schema")
        }
    }

    test("should get 400: POST of preferences with wrong enum in data_format to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId,
            """
              |{
              |  "enabled": false,
              |  "data_format" : [ "JSON", "BOGUS" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("does not match any enum value")
        }
    }

    test("should get 400: POST of preferences with no metadata /nometadata/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /nometadata/" + randomId)
        post("/nometadata/" + randomId,
            """
              |{
              |  "enabled": false,
              |  "data_format" : [ "JSON", "BOGUS" ],
              |  "default_container_name" : "FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://...",
              |      "dfw": "http://...",
              |      "ord": "http://...",
              |      "lon": "http://...",
              |      "hkg": "http://...",
              |      "syd": "http://..."
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("does not have any metadata")
        }
    }

    override def afterAll() {
        super.afterAll()
        clearData(db)
    }
}
