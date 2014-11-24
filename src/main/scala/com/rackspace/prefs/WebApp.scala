package com.rackspace.prefs

import com.rackspace.prefs.config.AppConfig

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object WebApp {

  def main(args: Array[String]) {

    val server = new Server(AppConfig.Http.port)
    val context = new WebAppContext()

    context.setServer(server)
    context.setContextPath("/")
    context.setResourceBase(".")
    context.setEventListeners(Array(new ScalatraListener))
    server.setHandler(context)

    try {
      server.start()
      server.join()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        System.exit(-1)
    }
  }
}
