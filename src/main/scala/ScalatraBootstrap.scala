import com.rackspace.prefs.{PreferencesService, env}
import org.scalatra.LifeCycle
import javax.servlet.ServletContext

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import scala.slick.jdbc.JdbcBackend.Database


class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)
  val pooledDataSource = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    pooledDataSource.close
  }

  override def init(context: ServletContext) {
    val db = Database.forDataSource(pooledDataSource)
    context.mount(new PreferencesService(db), "/*")
    context.initParameters("org.scalatra.environment") = env("PREFS_ENVIRONMENT", "development")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }

}
