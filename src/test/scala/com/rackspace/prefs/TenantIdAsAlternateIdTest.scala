package com.rackspace.prefs

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.FunSuiteLike
import scala.slick.jdbc.JdbcBackend._
import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.util.Random
import scala.io.Source
import com.rackspace.prefs.model.InitDbTrait
import org.scalatra.test.HttpComponentsClientResponse
import org.junit.Ignore

/**
 * User: shin4590
 * Date: 12/16/14
 */
@Ignore("This test needs the fix submitted in PR https://github.com/scalatra/scalatra/pull/449")
@RunWith(classOf[JUnitRunner])
class TenantIdAsAlternateIdTest extends ScalatraSuite with FunSuiteLike with InitDbTrait {

    val db = Database.forDataSource(new ComboPooledDataSource)
    addServlet(new PreferencesService(db), "/*")

    val schema = Source.fromInputStream(getClass().getResourceAsStream("/feeds_archives.schema.orderly")).getLines().mkString

    override def beforeAll {
        super.beforeAll
        clearData(db)
    }

    test("should get 201: POST /archive_prefs/:id with multiple x-tenant-id") {
        val randomId = Random.nextInt()
        info("Calling POST /archive_prefs/" + randomId)
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
        post("/archive_prefs/" + randomId, prefs,
             // change the Map() below to Seq() after this PR is accepted:
             //    https://github.com/scalatra/scalatra/pull/449
             Map("Content-Type" -> "application/json",
                 "x-tenant-id"  -> "123456",
                 "x-tenant-id"  -> "StagingUS_2309754-eff0-24234",
                 "x-tenant-id"  -> "foo bar none")) {
            if ( status != 201 ) {
                info(body)
            }
            status should equal (201)
        }
    }

    override def afterAll() {
        super.afterAll()
        clearData(db)
    }
}
