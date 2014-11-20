package com.rackspace.prefs.model

import com.rackspace.prefs.model.DBTables._

import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.slick.driver.JdbcDriver.simple._

trait InitDbTrait {

  val ResourceTypeSlub = "archive_prefs"
  val ArchivingStateResourceType = "archiving_state"

  def createSchema(db: Database) {
    db withDynSession {
      (resourceTypes.ddl ++ resourceAttributes.ddl ++ resources.ddl).create
    }
  }

  def initMetaData(db: Database) {
    db withDynSession {
      resourceTypes += (ResourceTypeSlub, "Archive Preferences", "cloud_account_id")
      resourceAttributes += (1, ResourceTypeSlub, ArchivingStateResourceType, "String", "optional", "in (enabled, disabled)")
    }
  }

}
