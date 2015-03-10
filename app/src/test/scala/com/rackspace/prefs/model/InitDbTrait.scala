package com.rackspace.prefs.model

import com.rackspace.prefs.model.DBTables._

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import org.joda.time.DateTime
import scala.io.Source

trait InitDbTrait {

    val ArchivePrefsMetadataSlug = "archive"

    val archivingSchema = Source.fromInputStream(getClass.getResourceAsStream("/feeds_archives.schema.orderly")).mkString

    def createPreference(db: Database, id: String, preferenceSlug: String, payload: String) {
        val currentTime = new DateTime()
        db.withSession { implicit session =>
          val prefsMetadataId =
            preferencesMetadata.filter(_.slug === preferenceSlug).map(_.id).run.head

            preferences
              .map(p => (p.id, p.preferencesMetadataId, p.payload, p.alternateId))
              .insert(id, prefsMetadataId, payload, Some("alternate_id"))
        }
    }

    def clearData(db: Database) {
      db.withSession { implicit session =>
        preferences.delete
      }
    }

    def dropMetadata(db: Database) {
        db.withSession{ implicit session =>
            preferencesMetadata.ddl.drop
        }
    }

    def createMetadata(db: Database): Unit = {
        db.withSession { implicit session =>
            preferencesMetadata.ddl.create

            preferencesMetadata
                .map(pm => (pm.id, pm.slug, pm.description, pm.schema))
                .insert(1, ArchivePrefsMetadataSlug, "Cloud Feeds Archiving Preferences", archivingSchema)
        }
    }
}
