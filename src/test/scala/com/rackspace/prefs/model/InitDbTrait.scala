package com.rackspace.prefs.model

import com.rackspace.prefs.model.DBTables._

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

trait InitDbTrait {

  val ArchivePrefsMetadataSlug = "archive_prefs"

  def createSchema(db: Database) {

    db withDynSession {
      (preferencesMetadata.ddl ++ preferences.ddl).create
    }
  }

  def initMetaData(db: Database) {

    val sampleSchema =
        "{" +
        "\"title\": \"Archiving prefs Schema\"," +
        "\"type\": \"object\"," +
        "\"properties\": {" +
        "   \"archiving_state\": {" +
        "         \"type\": \"string\"" +
        "     }" +
        "}," +
        "\"required\": [\"archiving_state\"]" +
        "}"

    db withDynSession {
      preferencesMetadata += PreferencesMetadata(ArchivePrefsMetadataSlug, "Cloud feeds Archive Preferences", sampleSchema)
    }
  }

}
