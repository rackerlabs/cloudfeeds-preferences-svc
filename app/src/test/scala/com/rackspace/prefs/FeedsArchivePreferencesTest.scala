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
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

/**
 * User: shin4590
 * Date: 12/4/14
 */
@RunWith(classOf[JUnitRunner])
class FeedsArchivePreferencesTest extends ScalatraSuite with FunSuiteLike with InitDbTrait {

    implicit val jsonFormats: Formats = DefaultFormats
    val db = Database.forDataSource(new ComboPooledDataSource)
    val preferencesService = new PreferencesService(db)
    addServlet(preferencesService, "/*")

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

    val encodedUrl = """http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds%20%21%40%23%2D%3D%5F%5f%f5~!@$*()-+=_;,.Archive"""
    val prefs_enable_all_encoded =
    f"""
      |{
      |  "enabled" : true,
      |  "data_format" : [ "JSON", "XML" ],
      |  "default_archive_container_url" : "$encodedUrl%s",
      |  "archive_container_urls": {
      |      "iad": "$encodedUrl%s",
      |      "dfw": "$encodedUrl%s",
      |      "ord": "$encodedUrl%s",
      |      "lon": "$encodedUrl%s",
      |      "hkg": "$encodedUrl%s",
      |      "syd": "$encodedUrl%s"
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

    test("should get 201: POST of a new good preferences to /archive/:id/ (with trailing slash)") {
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

    test("should get 201: POST of a new preferences with only one data_format to /archive/:id/ (with trailing slash)") {
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

    test("should get 200: POST of a good preferences to existing /archive/:id/ (with trailing slash)") {
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

    // test malformed url with invalid characters:  white space ^`{}|[]<>
    val invalidUrlChars = List(' ', '^', '`', '{', '}', '|', '[', ']', '<', '>')
    invalidUrlChars.foreach { testChar =>
        test("should get 400: POST of preferences with INVALID container URL (malformed '" + testChar + "' char) to /archive/:id") {
            val randomId = Random.nextInt()
            info("Calling POST /archive/" + randomId)

            val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives/Feeds" + testChar + "Archives"
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
    }

    test("should get 400: POST of preferences with DEFAULT container MISSING container name to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/"
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
            body should include ("Preferences for /archive/" + randomId + " is missing container name: " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with container MISSING container name to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/"
        post("/archive/" + randomId,
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
            , Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " is missing container name: " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with DEFAULT container name longer than 255 bytes to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl =
          """
            |http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/
            |12345678901234567890123456789012345678901234567890
            |12345678901234567890123456789012345678901234567890
            |12345678901234567890123456789012345678901234567890
            |12345678901234567890123456789012345678901234567890
            |12345678901234567890123456789012345678901234567890
            |%20_56
            |""".stripMargin.replaceAll("\n", "")

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
            body should include ("Preferences for /archive/" + randomId + " has an encoded container name longer than 255 bytes: " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with container name longer than 255 bytes to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl =
            """
              |http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/
              |12345678901234567890123456789012345678901234567890
              |12345678901234567890123456789012345678901234567890
              |12345678901234567890123456789012345678901234567890
              |12345678901234567890123456789012345678901234567890
              |12345678901234567890123456789012345678901234567890
              |%20_56
              |""".stripMargin.replaceAll("\n", "")

        post("/archive/" + randomId,
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
            , Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " has an encoded container name longer than 255 bytes: " + invalidUrl)
        }
    }

    // test for invalid uri chars in container name
    val invalidUriChars = List('#', '?', '%')
    invalidUriChars.foreach { testChar =>
        test("should get 400: POST of preferences with INVALID DEFAULT container name ('" + testChar + "' not encoded) to /archive/:id") {
            val randomId = Random.nextInt()
            info("Calling POST /archive/" + randomId)

            val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds" + testChar + "Archive"
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
                body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl + ". Url must be encoded and should not contain query parameters or url fragments. Encoded container name cannot contain a forward slash(/) and must be less than 256 bytes in length.")
            }
        }

        test("should get 400: POST of preferences with INVALID container name ('" + testChar + "' not encoded) to /archive/:id") {
            val randomId = Random.nextInt()
            info("Calling POST /archive/" + randomId)

            val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds" + testChar + "Archive"
            post("/archive/" + randomId,
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
                , Map("Content-Type" -> "application/json")) {
                status should equal (400)
                body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl + ". Url must be encoded and should not contain query parameters or url fragments. Encoded container name cannot contain a forward slash(/) and must be less than 256 bytes in length.")
            }
        }
    }


    test("should get 400: POST of preferences with INVALID DEFAULT container name (with literal '/') to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds#Archive"
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
            body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl + ". Url must be encoded and should not contain query parameters or url fragments. Encoded container name cannot contain a forward slash(/) and must be less than 256 bytes in length.")
        }
    }

    test("should get 400: POST of preferences with INVALID DEFAULT container name (with encoded '/') to /archive/:id") {
        // %2F is the url encoded value for '/'
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds%2FArchive"
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
            body should include ("Preferences for /archive/" + randomId + " has an invalid container name containing '/': " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with INVALID container name (with encoded '/') to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds%2FArchive"
        post("/archive/" + randomId,
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
            , Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " has an invalid container name containing '/': " + invalidUrl)
        }
    }

    test("should get 400: POST of preferences with INVALID DEFAULT container name (with mix encoded and non-encoded special chars) to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds!@#?$Arch%3F%23ive"
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
            body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl + ". Url must be encoded and should not contain query parameters or url fragments. Encoded container name cannot contain a forward slash(/) and must be less than 256 bytes in length.")
        }
    }

    test("should get 400: POST of preferences with INVALID container name (with mix encoded and non-encoded special chars) to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)

        val invalidUrl = "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/Feeds!@#?$Arch%3F%23ive"
        post("/archive/" + randomId,
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
            , Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " has an invalid url: " + invalidUrl + ". Url must be encoded and should not contain query parameters or url fragments. Encoded container name cannot contain a forward slash(/) and must be less than 256 bytes in length.")
        }
    }

    test("should get 201: POST of good preferences with container name having valid url encoding to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId, prefs_enable_all_encoded, Map("Content-Type" -> "application/json")) {
            status should equal (201)
        }
    }

    test("should get 201: POST of good preferences with container name having valid url encoding to /archive/:id/ (with trailing slash)") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/",
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "$encodedUrl%s/",
              |  "archive_container_urls": {
              |      "iad": "$encodedUrl%s/",
              |      "dfw": "$encodedUrl%s/",
              |      "ord": "$encodedUrl%s/",
              |      "lon": "$encodedUrl%s/",
              |      "hkg": "$encodedUrl%s/",
              |      "syd": "$encodedUrl%s/"
              |  }
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            status should equal (201)
        }
    }

    test("should get 200: POST of a good preferences with container name having valid url encoding to existing /archive/:id") {
        val randomId = Random.nextInt()

        info("Calling 1st POST /archive/" + randomId)
        post("/archive/" + randomId, prefs_enable_all_encoded, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }

        info("Calling 2nd POST /archive/" + randomId)
        post("/archive/" + randomId, prefs_enable_all_encoded, Map("Content-Type" -> "application/json")) {
            if ( status != 200 ) {
                info(body)
            }
            status should equal (200)
        }
    }

    test("should get 200: POST of a good preferences with container name having valid url encoding to existing /archive/:id/ (with trailing slash)") {
        val randomId = Random.nextInt()

        info("Calling 1st POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/", prefs_enable_all, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }

        info("Calling 2nd POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/",
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "$encodedUrl%s/",
              |  "archive_container_urls": {
              |      "iad": "$encodedUrl%s/",
              |      "dfw": "$encodedUrl%s/",
              |      "ord": "$encodedUrl%s/",
              |      "lon": "$encodedUrl%s/",
              |      "hkg": "$encodedUrl%s/",
              |      "syd": "$encodedUrl%s/"
              |  }
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            if ( status != 200 ) {
                info(body)
            }
            status should equal (200)
        }
    }

    test("should get 201: POST of good preferences with only the default and one data center (lon)") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/",
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "lon": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesLon"
              |  }
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }
    }

    test("should get 200: POST of good preferences with only the default and two data center (lon, dfw)") {
        val randomId = Random.nextInt()

        info("Calling 1st POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/", prefs_enable_all, Map("Content-Type" -> "application/json")) {
          if ( status != 201 ) {
            info(body)
          }
          status should equal (201)
        }

        info("Calling 2nd POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/",
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "lon": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesLon",
              |      "dfw": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesDfw"
              |  }
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            if ( status != 200 ) {
                info(body)
            }
            status should equal (200)
        }
    }

    test("should get 400: POST of invalid preferences with NO default and only one data center (lon)") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/",
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "archive_container_urls": {
              |      "lon": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesLon"
              |  }
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            if ( status != 400 ) {
                info(body)
            }
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " must have a default_container_url or must have all datacenter archive_container_urls present")
        }
    }

    test("should get 400: POST of invalid preferences with NO default and missing one data center (lon)") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/",
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "archive_container_urls": {
              |      "iad": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesIad",
              |      "dfw": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesDfw",
              |      "ord": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesOrd",
              |      "hkg": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesHkg",
              |      "syd": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesSyd"
              |  }
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            if ( status != 400 ) {
                info(body)
            }
            status should equal (400)
            body should include ("Preferences for /archive/" + randomId + " must have a default_container_url or must have all datacenter archive_container_urls present")
        }
    }

    test("should get 201: POST of good preferences with NO default and ALL data center") {
      val randomId = Random.nextInt()
      info("Calling POST /archive/" + randomId + "/")
      post("/archive/" + randomId + "/",
          f"""
            |{
            |  "enabled" : true,
            |  "data_format" : [ "JSON", "XML" ],
            |  "archive_container_urls": {
            |      "syd": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesSyd",
            |      "hkg": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesHkg",
            |      "lon": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesLon",
            |      "dfw": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesDfw",
            |      "iad": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesIad",
            |      "ord": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesOrd"
            |  }
            |}
          """.stripMargin
          , Map("Content-Type" -> "application/json")) {
          if ( status != 201 ) {
              info(body)
          }
          status should equal (201)
      }
    }

    test("should get 200: POST of good preferences with NO default and ALL data center") {
        val randomId = Random.nextInt()

        info("Calling 1st POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/", prefs_enable_all, Map("Content-Type" -> "application/json")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }

        info("Calling 2nd POST /archive/" + randomId + "/")
        post("/archive/" + randomId + "/",
            f"""
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "archive_container_urls": {
              |      "dfw": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesDfw",
              |      "iad": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesIad",
              |      "ord": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesOrd",
              |      "lon": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesLon",
              |      "syd": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesSyd",
              |      "hkg": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchivesHkg"
              |  }
              |}
            """.stripMargin
            , Map("Content-Type" -> "application/json")) {
            if ( status != 200 ) {
                info(body)
            }
            status should equal (200)
        }
    }

    test("should get 400: POST of preferences with invalid json formatted payload to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId,
            """
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
              |  }
              |};
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("must have valid json formatted payload")
        }
    }

    test("should get 400: POST of preferences with invalid extra json formatted payload to /archive/:id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive/" + randomId)
        post("/archive/" + randomId,
            """
              |{
              |  "enabled" : true,
              |  "data_format" : [ "JSON", "XML" ],
              |  "default_archive_container_url" : "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives"
              |  }
              |}
              |{
              |  "enabled": true,
              |  "data_format":["JSON", "XML"],
              |  "default_archive_container_url": "https://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives",
              |  "archive_container_urls": {
              |      "iad": "http://storage.stg.swift.racklabs.com/v1/Nast-Id_1/FeedsArchives2"
              |  }
              |}
            """.stripMargin, Map("Content-Type" -> "application/json")) {
            status should equal (400)
            body should include ("must have valid json formatted payload")
        }
    }

    test("jsonifyError should return valid json string") {
        // call the method with the errorMessage for testing
        val testErrorMessage = "here's a string with \"special\" characters!\n%#?\\/:[]{}"
        val jsonString = preferencesService.jsonifyError(testErrorMessage)

        // parse the return string to make sure it's valid json
        val json = parse(jsonString)
        val jsonError = json \ "error"
        assert(jsonError != null)

        // validate the message
        val returnedErrorString = jsonError.extract[String]
        returnedErrorString should include (testErrorMessage)
    }

    override def afterAll() {
        super.afterAll()
        clearData(db)
    }
}
