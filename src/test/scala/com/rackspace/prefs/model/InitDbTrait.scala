package com.rackspace.prefs.model

import com.rackspace.prefs.model.DBTables._

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

trait InitDbTrait {

    val ArchivePrefsMetadataSlug = "archive_prefs"

    def createSchema(db: Database) {

        db withDynSession {
            val ddl = preferencesMetadata.ddl ++ preferences.ddl
            ddl.createStatements.foreach(println)

            ddl.create
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

        initMetaData(db, sampleSchema)
    }

    def initMetaData(db: Database, schema: String) {
        db withDynSession {
            preferencesMetadata += PreferencesMetadata(ArchivePrefsMetadataSlug, "Cloud feeds Archive Preferences", schema)
        }
    }

}
