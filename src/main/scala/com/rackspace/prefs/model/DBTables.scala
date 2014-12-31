package com.rackspace.prefs.model

import java.sql.Timestamp

import org.joda.time.DateTime

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.direct.AnnotationMapper.column
import scala.slick.lifted.TableQuery


object DBTables {

  //org.joda.time.DateTime -> java.sql.Timestamp mapper
  implicit def dateTime2TimeStamp  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.getMillis),
    ts => new DateTime(ts.getTime)
  )

  class PreferencesMetadataTable(tag: Tag)
    extends Table[PreferencesMetadata](tag, "preferences_metadata") {

    // h2 specifc: Using O.AutoInc make this column(ID) implicitly primary key
    // which it shouldnt be.
    def id = column[Int]("id", O.DBType("INTEGER AUTO_INCREMENT"))
    def slug = column[String]("slug", O.PrimaryKey, O.DBType("VARCHAR"))
    def description = column[String]("description", O.DBType("VARCHAR"))
    def schema = column[String]("schema", O.DBType("VARCHAR"))
    def * = (id.?, slug, description, schema) <>
      (PreferencesMetadata.tupled, PreferencesMetadata.unapply)
  }
  val preferencesMetadata = TableQuery[PreferencesMetadataTable]

  class PreferencesTable(tag: Tag)
    extends Table[Preferences](tag, "preferences") {

    def id = column[String]("id", O.DBType("VARCHAR"))
    def preferencesMetadataId = column[Int]("preferences_metadata_id")
    def payload = column[String]("payload", O.DBType("VARCHAR"))

    def alternateId = column[Option[String]]("alternate_id", O.Nullable, O.DBType("VARCHAR"))

    // This is not an ideal way to set a default timestamp while creating DDL. Better way would be to do this.
    // val now = SimpleFunction.nullary[Timestamp]("now")
    // and adding O.Default(now) instead of O.DBType.
    // To make this happen, we need to figure a way to make "now" of time "DateTime" instead of "TIMESTAMP"
    // Either way, this is not database agnostic
    def created = column[DateTime]("created", O.NotNull, O.DBType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def updated = column[DateTime]("updated", O.NotNull, O.DBType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))

    override def * = (id, preferencesMetadataId, payload, alternateId, created.?, updated.?) <>
      (Preferences.tupled, Preferences.unapply)

    def pk = primaryKey("compound_pk", (id, preferencesMetadataId))
    def slugId = foreignKey("preferences_metadata_slug_fk", preferencesMetadataId, preferencesMetadata)(_.id)
  }

  val preferences = TableQuery[PreferencesTable]
}
