package com.rackspace.prefs.model

import org.joda.time.DateTime

case class Preferences(id: String, preferencesMetadataId: Int,
                       payload: String, created: Option[DateTime] = None, updated: Option[DateTime] = None)