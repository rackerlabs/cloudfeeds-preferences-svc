package com.rackspace.prefs

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object WebApp {

  def main(args: Array[String]) {

    val server = new Server(envInt("PREFS_PORT", 8080))
    val context = new WebAppContext()

    context.setServer(server)
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")
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
