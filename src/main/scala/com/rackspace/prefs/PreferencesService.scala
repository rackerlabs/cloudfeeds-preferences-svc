package com.rackspace.prefs

import com.rackspace.prefs.model.DBTables._
import com.rackspace.prefs.model.{Preferences, PreferencesMetadata}
import org.joda.time.DateTime
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json._
import org.scalatra.scalate.ScalateSupport

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

case class PreferencesService(db: Database) extends ScalatraServlet
      with ScalateSupport
      with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats

  get("/") {
    <html><body>
      Preferences Service!
      {if (isDevelopmentMode) <h3>Dev Mode</h3>}
    </body></html>
  }

  get("/metadata") {
    contentType = formats("json")
    db withDynSession { preferencesMetadata.list.map(m => (m.slug, m.schema)) }
  }


  get("/metadata/:preference_type") {
    val preferenceType = params("preference_type")
    contentType = formats("json")
    db withDynSession {
      preferencesMetadata.filter(_.slug === preferenceType).run
    }
  }

  get("/archive_prefs/:id") {
    val id = params("id")
    db withDynSession {
      preferences.filter(_.id === id).run
    }
  }

  post("/:preference_type/:id") {
    val preferenceType = params("preference_type")
    val id = params("id")
    val payload = request.body

    //TODO: Implement actual save to database
    Preferences(id, preferenceType, payload,
      Option(DateTime.now), Option(DateTime.now))
  }


}
