import java.nio.file.{Files, Paths}
import javax.servlet.ServletContext

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.mchange.v2.c3p0.{ComboPooledDataSource, PooledDataSource}
import com.rackspace.prefs.PreferencesService
import com.rackspace.prefs.config.AppConfig
import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

import scala.slick.jdbc.JdbcBackend.Database


class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  var pooledDataSource: PooledDataSource = null


  override def init(context: ServletContext) {

    intiDbPool
    configLog

    val db = Database.forDataSource(pooledDataSource)
    context.mount(new PreferencesService(db), "/*")
    context.initParameters("org.scalatra.environment") = AppConfig.Scalatra.environment
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }


  def intiDbPool {

    if (Files.exists(Paths.get(AppConfig.Db.c3p0ConfigFile))) {

      System.setProperty("com.mchange.v2.c3p0.cfg.xml", AppConfig.Db.c3p0ConfigFile)
      logger.info("Creating c3p0 connection pool from external config :" + AppConfig.Db.c3p0ConfigFile)
    } else {
      logger.info("Creating c3p0 connection pool from classpath config c3p0-config.xml")
    }

    pooledDataSource = new ComboPooledDataSource
    logger.debug("Created c3p0 connection pool")
  }

  def configLog {

    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    //check for external logback file.
    if (Files.exists(Paths.get(AppConfig.Log.configFile))) {

      try {
        val configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        configurator.doConfigure(AppConfig.Log.configFile);

        logger.info("Using logback config from external file :" + AppConfig.Log.configFile)

      } catch {
        case joe: JoranException =>   // StatusPrinter will handle this
      }

      StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
    }
  }

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    pooledDataSource.close
  }
}
