package com.rackspace.prefs

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.model.InitDbTrait
import org.junit.runner.RunWith
import org.scalatest.FunSuiteLike
import org.scalatest.junit.JUnitRunner
import org.scalatra.test.scalatest.ScalatraSuite

import scala.slick.jdbc.JdbcBackend._
import scala.util.parsing.json.JSON

/**
 * Created by shin4590 on 3/5/15.
 */
@RunWith(classOf[JUnitRunner])
class StatusTest extends ScalatraSuite with FunSuiteLike with InitDbTrait {

    val db = Database.forDataSource(ds)

    override def beforeAll {
        super.beforeAll
        clearData(db)
    }

    test("should get 200: GET /status") {
        addServlet(new PreferencesService(db), "/*")
        get("/status") {
            status should equal (200)
            val countConversionFunc = {input: String => new Integer(input)}
            JSON.perThreadNumberParser = countConversionFunc
            val prefStatus = JSON.parseFull(body)
            val statusMap = prefStatus.get.asInstanceOf[Map[String, Int]]
            statusMap.get("metadata-count").get should be > (0)
            info(body)
        }
    }

    // this needs to be the last test run, cuz it's going to drop
    // the Preferences Metadata table
    test("should get 500: GET /status when metadata table does not exist") {
        dropMetadata(db)
        try {
            get("/status") {
                status should equal(500)
                info(body)
            }
        } finally {
            createMetadata(db)
        }
    }
}
