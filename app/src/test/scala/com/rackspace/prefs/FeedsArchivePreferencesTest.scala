package com.rackspace.prefs

import com.rackspace.prefs.model.DBTables._
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

    val prefs_enable_all =
    """
      |{
      |  "enabled": true,
      |  "data_format" : [ "JSON", "XML" ],
      |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |  "archive_container_urls": {
      |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
      |  }
      |}
    """.stripMargin

    val prefs_enable_regions =
    """
      |{
      |  "enabled": true,
      |  "data_format" : [ "JSON", "XML" ],
      |  "archive_container_urls": {
      |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
      |  }
      |}
    """.stripMargin

    val prefs_enable_all_json =
    """
      |{
      |  "enabled": true,
      |  "data_format" : [ "JSON" ],
      |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |  "archive_container_urls": {
      |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
      |  }
      |}
    """.stripMargin

    val prefs_disable_all =
    """
      |{
      |  "enabled": false,
      |  "data_format" : [ "JSON", "XML" ],
      |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |  "archive_container_urls": {
      |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
      |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
      |  }
      |}
    """.stripMargin

    override def beforeAll {
        super.beforeAll
        clearData(db)
    }

    test("should get 200: GET /archive/:id") {
        val randomId = Random.nextInt()
        createPreference(db, randomId.toString(), "archive", prefs_enable_all)

        info("Calling GET /archive/" + randomId)
        get("/archive/" + randomId) {
            status should equal (200)
            body should equal (prefs_enable_all)
            response.mediaType should equal (Some("application/json"))
        }
    }

    test("should get 200: GET /archive/:id/") {
        val randomId = Random.nextInt()
        createPreference(db, randomId.toString(), "archive", prefs_enable_all)

        info("Calling GET /archive/" + randomId + "/")
        get("/archive/" + randomId + "/") {
            status should equal (200)
            body should equal (prefs_enable_all)
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

    test("should get 404: GET on preferences with extra resource in path /archive/:id/extra/stuff") {
        val randomId = Random.nextInt()
        createPreference(db, randomId.toString(), "archive", prefs_enable_all)

        info("Calling GET /archive/" + randomId + "/extra/stuff")
        get("/archive/" + randomId + "/extra/stuff") {
          status should equal (404)
        }
    }

    test("should get 201: POST of a new good preferences to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId, prefs_enable_regions, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }
    }

    test("should get 201: POST of a new good preferences to /archive/:id/") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/", prefs_enable_regions, Map("Content-Type" -> "application/json")) {
          if ( status != 201 ) {
            info(body)
          }
          status should equal (201)
        }
    }

    test("should get 201: POST of a new preferences with only one data_format to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId, prefs_enable_all_json, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
                println(body)
            }
            status should equal (201)
        }
    }

    test("should get 201: POST of a new preferences with only one data_format to /archive/:id/") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/", prefs_enable_all_json, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
                println(body)
            }
            status should equal (201)
        }
    }

    test("should get 200: POST of a good preferences to existing /archive/:id") {
        val randomId = Random.nextInt()

        info("Calling 1st POST /archive/" + randomId)
        post("/archive/" + randomId, prefs_disable_all, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }

        info("Calling 2nd POST /archive/" + randomId)
        post("/archive/" + randomId, prefs_enable_all, Map("Content-Type" -> "application/json")) {
            if ( status != 200 ) {
                info(body)
            }
            status should equal (200)
        }
    }

    test("should get 200: POST of a good preferences to existing /archive/:id/") {
        val randomId = Random.nextInt()

        info("Calling 1st POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/", prefs_disable_all, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }

        info("Calling 2nd POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/", prefs_enable_all, Map("Content-Type" -> "application/json")) {
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
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
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
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
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
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
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
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("does not have any metadata")
        }
    }

    test("should get 400: POST of preferences with INVALID DEFAULT container URL (bad scheme) to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "asdf://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
        post("/archive/" + randomId,
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "$invalidUrl%s"
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with INVALID DEFAULT container URL (malformed) to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives/url with spaces"
        post("/archive/" + randomId,
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "$invalidUrl%s"
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with INVALID container URL (bad scheme) to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "asdf://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
        val preferenceContent =
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "lon": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "syd": "$invalidUrl%s"
              |  }
              |}
            """.stripMargin

        post("/archive/" + randomId, preferenceContent, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with INVALID container URL (malformed) to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives/url with spaces"
        val preferenceContent =
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "dfw": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "ord": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "lon": "$invalidUrl%s",
              |      "hkg": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |      "syd": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
              |  }
              |}
            """.stripMargin

        post("/archive/" + randomId, preferenceContent, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl)
        }
    }

    override def afterAll() {
        super.afterAll()
        clearData(db)
    }
}
