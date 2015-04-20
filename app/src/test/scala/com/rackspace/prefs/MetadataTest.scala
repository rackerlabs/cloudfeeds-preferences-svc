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
class MetadataTest extends ScalatraSuite with FunSuiteLike with InitDbTrait {

    val db = Database.forDataSource(new ComboPooledDataSource)
    addServlet(new PreferencesService(db), "/*")

    val schema = Source.fromInputStream(getClass().getResourceAsStream("/feeds_archives.schema.orderly")).getLines().mkString

    override def beforeAll {
        super.beforeAll
        clearData(db)
    }

    test("should get 404: GET /") {
        get("/") {
            println(body)
            status should equal (404)
        }
    }

    test("should get 404: GET /metadata") {
        get("/metadata") {
            println(body)
            status should equal (404)
        }
    }

    test("should get 200: GET /metadata/archive") {
        get("/metadata/archive") {
            println(body)
            status should equal (200)
            response.mediaType should equal (Some("application/json"))
        }
    }

    test("should get 200: GET /metadata/archive/") {
        get("/metadata/archive/") {
            println(body)
            status should equal (200)
            response.mediaType should equal (Some("application/json"))
        }
    }

    test("should get 404 on non existent metadata: GET /metadata/doesnotexist") {
        get("/metadata/doesnotexist") {
            println(body)
            status should equal (404)
        }
    }

    override def afterAll() {
        super.afterAll()
        clearData(db)
    }

}
