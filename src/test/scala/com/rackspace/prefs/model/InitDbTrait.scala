package com.rackspace.prefs.model

import com.rackspace.prefs.model.DBTables._

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import org.joda.time.DateTime

trait InitDbTrait {

    val ArchivePrefsMetadataSlug = "archive_prefs"

    def createPreference(db: Database, id: String, preferenceSlug: String, payload: String) {
        val currentTime = new DateTime()
        db withDynSession {
          val prefsMetadataId =
            preferencesMetadata.filter(_.slug === preferenceSlug).map(_.id).run.head

            preferences
              .map(p => (p.id, p.preferencesMetadataId, p.payload, p.alternateId))
              .insert(id, prefsMetadataId, payload, Some("alternate_id"))
        }
    }

    def clearData(db: Database) {
      db withDynSession {
        preferences.delete
      }
    }
}
