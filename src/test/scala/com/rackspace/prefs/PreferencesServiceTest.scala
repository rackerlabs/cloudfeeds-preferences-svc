package com.rackspace.prefs

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.model.InitDbTrait
import org.junit.runner.RunWith
import org.scalatest.FunSuiteLike
import org.scalatest.junit.JUnitRunner
import org.scalatra.test.scalatest._

import scala.slick.jdbc.JdbcBackend.Database

/**
 * User: shin4590
 * Date: 12/4/14
 */
@RunWith(classOf[JUnitRunner])
class PreferencesServiceTest extends ScalatraSuite with FunSuiteLike with InitDbTrait {

    val db = Database.forDataSource(new ComboPooledDataSource)
    addServlet(new PreferencesService(db), "/*")

    override def beforeAll {
        super.beforeAll
        createSchema(db)
    }

    test("get of /") {
        get("/") {
            status should equal (200)
        }
    }

    test("get of /metadata") {
        get("/metadata") {
            status should equal (200)
        }
    }

    test("post of a good preferences to /archive_prefs/1") {
        post("/archive_prefs/1",
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
            status should equal (200)
        }
    }
}
