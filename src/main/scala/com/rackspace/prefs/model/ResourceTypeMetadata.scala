package com.rackspace.prefs.model

case class ResourceTypeMetadata(slug: String, name: String, id_type: String)

object ResourceTypeMetadata {

  val all = List(
    ResourceTypeMetadata("archive_config","Archive Config","cloud account id")
  )

}
