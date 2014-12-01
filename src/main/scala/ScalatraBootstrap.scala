import java.nio.file.{Paths, Files}
import javax.servlet.ServletContext

import com.mchange.v2.c3p0.{PooledDataSource, ComboPooledDataSource}
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
    logger.info("Created c3p0 connection pool")
  }

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    pooledDataSource.close
  }
}
