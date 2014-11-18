package com.rackspace.prefs.model

import scala.slick.driver.JdbcDriver.simple._

object DBTables {

  class ResourceTypes(tag: Tag)
  extends Table[(String, String, String)](tag, "RESOURCE_TYPES") {
    def slug = column[String]("SLUG", O.PrimaryKey)
    def name = column[String]("NAME")
    def identifier = column[String]("IDENTIFIER")
    def * = (slug, name, identifier)
  }
  val resourceTypes = TableQuery[ResourceTypes]

  class ResourceAttributes(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "RESOURCE_ATTRIBUTES") {
    def id = column[Int]("ID", O.PrimaryKey)
    def resourceType = column[String]("RESOURCE_TYPE")
    def key = column[String]("KEY")
    def valueType = column[String]("VALUE_TYPE")
    def use = column[String]("USE")
    def validation = column[String]("VALIDATION")

    def * = (id, resourceType, key, valueType, use, validation)

    def slug = foreignKey("ATTRIBUTE_SLUG_FK", resourceType, resourceTypes)(_.slug)
  }
  val resourceAttributes = TableQuery[ResourceAttributes]

  class Resources(tag: Tag) extends Table[(String, String, String)](tag, "RESOURCES") {
    //TODO: figure out how to make this unique on (resourceType, id)
    def resourceType = column[String]("RESOURCE_TYPE")
    def id = column[String]("ID")
    def payload = column[String]("PAYLOAD")
    def * = (resourceType, id ,payload)
    def slug = foreignKey("RESOURCE_SLUG_FK", resourceType, resourceTypes)(_.slug)
  }
  val resources = TableQuery[Resources]

}
