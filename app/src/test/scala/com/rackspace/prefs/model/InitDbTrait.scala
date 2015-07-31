package com.rackspace.prefs.model

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.model.DBTables._

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import org.joda.time.DateTime
import scala.io.Source

trait InitDbTrait {

    /**
    * When you run the tests from command line using gradle(Ex: gradle test), it runs the
    * tests with reference to the app directory
    *
    * When you run the tests from intellij, it is running the test with reference to the main
    * project directory. This code handles the difference.
    */
    private val jdbcUrl = {
      val userDir: String = System.getProperty("user.dir").replaceFirst("/$", "")

      if (userDir.endsWith("app"))
        s"jdbc:h2:file:$userDir/build/db/test/preferencesdb;MODE=PostgreSQL;IGNORECASE=TRUE"
      else
        s"jdbc:h2:file:$userDir/app/build/db/test/preferencesdb;MODE=PostgreSQL;IGNORECASE=TRUE"
    }

    val ds: ComboPooledDataSource = new ComboPooledDataSource
    ds.setJdbcUrl(jdbcUrl)

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
