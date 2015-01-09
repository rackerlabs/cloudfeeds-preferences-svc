package com.rackspace.prefs.model

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.model.DBTables._
import org.h2.jdbc.JdbcSQLException
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.slf4j.LoggerFactory

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database


class DBTablesIT extends FunSuite with BeforeAndAfterAll with InitDbTrait {

  val logger = LoggerFactory.getLogger(getClass)
  val pooledDataSource = new ComboPooledDataSource
  val db : Database = Database.forDataSource(pooledDataSource)

  test ("Verify insertion of data into table preferences") {
    clearData(db)

    val tenantId = "tenant_1"
    val currentTime = new DateTime()

    db.withSession { implicit session =>
      val archivePrefsMetadataId =
        preferencesMetadata.filter(_.slug === ArchivePrefsMetadataSlug).map(_.id).run.head

        preferences.map(prefs => (prefs.id, prefs.preferencesMetadataId, prefs.payload))
          .insert(tenantId, archivePrefsMetadataId, "{\"payload\": \"JSON blob\"}")
    }

    val hasTenantPreference = db.withSession { implicit session =>
      preferences.filter(_.id === tenantId).run.nonEmpty
    }

    assert(hasTenantPreference, "row not inserted in table preferences")
  }

  test ("Verify only unique combinations of tenantId/preferenceMetadata can be inserted into PREFERENCES table") {
    clearData(db)

    val tenantId = "tenant_1"
    val currentTime = new DateTime()

    try {
      db.withSession { implicit session =>
        val archivePrefsMetadataId =
          preferencesMetadata.filter(_.slug === ArchivePrefsMetadataSlug).map(_.id).run.head

        preferences.map(prefs => (prefs.id, prefs.preferencesMetadataId, prefs.payload))
          .insert(tenantId, archivePrefsMetadataId, "{\"payload\": \"JSON blob\"}")

        preferences.map(prefs => (prefs.id, prefs.preferencesMetadataId, prefs.payload))
          .insert(tenantId, archivePrefsMetadataId, "{\"payload\": \"JSON blob\"}")
      }
      fail()  //This will trigger failure if the above insert does not fail
    }
    catch {
      case _: JdbcSQLException =>  // Expected as there is unique key constraint violation
    }
  }

  override def afterAll {
    clearData(db)
    pooledDataSource.close
  }
}
