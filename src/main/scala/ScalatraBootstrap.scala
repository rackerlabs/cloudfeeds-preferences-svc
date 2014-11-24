import javax.servlet.ServletContext

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.PreferencesService
import com.rackspace.prefs.config.AppConfig
import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

import scala.slick.jdbc.JdbcBackend.Database


class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  System.setProperty("com.mchange.v2.c3p0.cfg.xml", AppConfig.Db.c3p0ConfigFile)
  val pooledDataSource = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    pooledDataSource.close
  }

  override def init(context: ServletContext) {
    val db = Database.forDataSource(pooledDataSource)
    context.mount(new PreferencesService(db), "/*")
    context.initParameters("org.scalatra.environment") = AppConfig.Scalatra.environment
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }

}
