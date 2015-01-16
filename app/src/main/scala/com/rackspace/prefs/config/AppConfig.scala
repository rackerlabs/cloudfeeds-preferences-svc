package com.rackspace.prefs.config

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object AppConfig {

  val logger = LoggerFactory.getLogger(getClass)

  //pass the below system property while starting the application to load from external file
  //System.setProperty("config.file","/etc/preferences-service/preferences-service.conf")

  //load from classpath
  private val classpathConfig = ConfigFactory.load("preferences-service")

  /**
   * If external file is passed, load config from external file, if not load from the
   * file in classpath
   */
  val config = if (Option(System.getProperty("config.file")).nonEmpty) {

    logger.info("Loading config from external path:" + System.getProperty("config.file"))
    ConfigFactory.load()
      .withFallback(classpathConfig)

  } else {
    logger.info("Loading config from classpath")
    classpathConfig
  }

  object Http {
    private val http = config.getConfig("appConfig.http")

    val port = http.getInt("port")
  }

  object Db {
    private val http = config.getConfig("appConfig.db")

    val c3p0ConfigFile = http.getString("c3p0ConfigFile")
  }

  object Scalatra {
    val environment = config.getString("appConfig.scalatra.environment")
  }

  object Log {
    private val log = config.getConfig("appConfig.log")

    val configFile = log.getString("configFile")
  }
}


