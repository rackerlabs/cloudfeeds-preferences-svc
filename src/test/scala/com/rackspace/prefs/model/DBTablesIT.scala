package com.rackspace.prefs.model

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.model.DBTables._
import org.h2.jdbc.JdbcSQLException
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.slf4j.LoggerFactory

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.slick.jdbc.meta.MTable


class DBTablesIT extends FunSuite with BeforeAndAfterAll with InitDbTrait {

  val logger = LoggerFactory.getLogger(getClass)
  val pooledDataSource = new ComboPooledDataSource
  val db : Database = Database.forDataSource(pooledDataSource)

  override def beforeAll {
    createSchema(db)
  }

  test ("Verify if tables got created") {

    val tables = db withDynSession {
      MTable.getTables.list
    }

    assert(tables.size == 2)
    assert(tables.count(_.name.name.equalsIgnoreCase("PREFERENCES")) == 1, "PREFERENCES table is not created")
    assert(tables.count(_.name.name.equalsIgnoreCase("PREFERENCES_METADATA")) == 1, "PREFERENCES_METADATA table is not created")
  }

  test ("Verify insertion of data into PREFERENCES_METADATA table") {
    clearData()

    db withDynSession {
      preferencesMetadata += PreferencesMetadata(ArchivePrefsMetadataSlug, "Cloud feeds Archive Preferences", "{\"schema\": \"JSON schema\"}")
    }

    val hasPreferenceMetadata = db withDynSession {
      preferencesMetadata.filter(_.slug === ArchivePrefsMetadataSlug).run.nonEmpty
    }

    assert(hasPreferenceMetadata, "row not inserted in PREFERENCES_METADATA table")
  }

  test ("Verify insertion of data into table PREFERENCES") {
    clearData()
    initMetaData(db)

    val tenantId = "tenant_1"
    val currentTime = new DateTime()

    db withDynSession {
      //current time and updated time should be inserted by database definition.
      preferences += Preferences(tenantId, ArchivePrefsMetadataSlug, "{\"payload\": \"JSON blob\"}",
        Option(currentTime), Option(currentTime))
    }

    val hasTenantPreference = db withDynSession {
      preferences.filter(_.id === tenantId).run.nonEmpty
    }

    assert(hasTenantPreference, "row not inserted in table PREFERENCES")
  }

  test ("Verify only unique combinations of tenantId/preferenceMetadata can be inserted into PREFERENCES table") {
    clearData()
    initMetaData(db)

    val tenantId = "tenant_1"
    val currentTime = new DateTime()

    val archivePrefs = Preferences(tenantId, ArchivePrefsMetadataSlug, "{\"payload\": \"JSON blob\"}",
      Option(currentTime), Option(currentTime))
    try {
      db withDynSession {
        preferences += archivePrefs
        preferences += archivePrefs
      }
      fail()  //This will trigger failure if the above insert does not fail
    }
    catch {
      case _: JdbcSQLException =>  // Expected as there is unique key constraint violation
    }
  }

  def clearData() {
    db withDynSession {
      preferences.delete
      preferencesMetadata.delete
    }
  }

  override def afterAll {
    pooledDataSource.close
  }
}
