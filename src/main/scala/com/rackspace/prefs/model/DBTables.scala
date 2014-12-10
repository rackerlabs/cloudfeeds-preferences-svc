package com.rackspace.prefs.model

import java.sql.Timestamp

import org.joda.time.DateTime

import scala.slick.driver.JdbcDriver.simple._


object DBTables {

  //org.joda.time.DateTime -> java.sql.Timestamp mapper
  implicit def dateTime2TimeStamp  = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.getMillis),
    ts => new DateTime(ts.getTime)
  )

  class PreferencesMetadataTable(tag: Tag)
    extends Table[PreferencesMetadata](tag, "PREFERENCES_METADATA") {

    def slug = column[String]("SLUG", O.PrimaryKey, O.DBType("VARCHAR"))
    def description = column[String]("DESCRIPTION", O.DBType("VARCHAR"))
    def schema = column[String]("SCHEMA", O.DBType("VARCHAR"))
    def * = (slug, description, schema) <>
      (PreferencesMetadata.tupled, PreferencesMetadata.unapply)
  }
  val preferencesMetadata = TableQuery[PreferencesMetadataTable]

  class PreferencesTable(tag: Tag)
    extends Table[Preferences](tag, "PREFERENCES") {

    def id = column[String]("ID", O.DBType("VARCHAR"))
    def preferencesMetadataSlug = column[String]("PREFERENCES_METADATA_SLUG", O.DBType("VARCHAR"))
    def payload = column[String]("PAYLOAD", O.DBType("VARCHAR"))

    def created = column[DateTime]("CREATED", O.NotNull)
    def updated = column[DateTime]("UPDATED", O.NotNull)

    override def * = (id ,preferencesMetadataSlug, payload, created.?, updated.?) <>
      (Preferences.tupled, Preferences.unapply)

    def pk = primaryKey("compound_pk", (id, preferencesMetadataSlug))
    def slug = foreignKey("PREFERENCES_METADATA_SLUG_FK", preferencesMetadataSlug, preferencesMetadata)(_.slug)
  }

  val preferences = TableQuery[PreferencesTable]
}
