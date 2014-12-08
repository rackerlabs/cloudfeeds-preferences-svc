package com.rackspace.prefs.model

import org.joda.time.DateTime

case class Preferences(id: String, preferencesMetadataSlug: String,
                       payload: String, created: Option[DateTime] = None, updated: Option[DateTime] = None)