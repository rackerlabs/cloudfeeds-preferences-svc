package com.rackspace.prefs.model

import com.rackspace.prefs.model.DBTables._

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import org.joda.time.DateTime

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

        initMetaData(db, sampleSchema)
    }

    def initMetaData(db: Database, schema: String) {
        db withDynSession {
            preferencesMetadata.map(pm => (pm.slug, pm.description, pm.schema))
              .insert(ArchivePrefsMetadataSlug, "Cloud feeds Archive Preferences", schema)
        }
    }

    def createPreference(db: Database, id: String, preferenceSlug: String, payload: String) {
        val currentTime = new DateTime()
        db withDynSession {
          val prefsMetadataId =
            preferencesMetadata.filter(_.slug === preferenceSlug).map(_.id).run.head

            //current time and updated time should be inserted by database definition.
            preferences += Preferences(id, prefsMetadataId, payload,
                                        Some("alternate_id"),
                                        Option(currentTime), Option(currentTime))
        }
    }

    def deleteSchema(db: Database) {
        db withDynSession {
            (preferencesMetadata.ddl ++ preferences.ddl).drop
        }
    }

}
