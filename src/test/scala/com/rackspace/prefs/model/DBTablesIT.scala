package com.rackspace.prefs.model

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.rackspace.prefs.model.DBTables._
import org.h2.jdbc.JdbcSQLException
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.slf4j.LoggerFactory

import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.slick.jdbc.meta.MTable

class DBTablesIT extends FunSuite with BeforeAndAfterAll with InitDbTrait {

  val logger = LoggerFactory.getLogger(getClass)
  val pooledDataSource = new ComboPooledDataSource
  val db : Database = Database.forDataSource(pooledDataSource)

  override def beforeAll {
    createSchema(db)
  }

  test ("Verify if tables got created") {

    val tables = db withDynSession {
      MTable.getTables.list
    }

    assert(tables.size == 3)
    assert(tables.count(_.name.name.equalsIgnoreCase("RESOURCE_TYPES")) == 1, "RESOURCE_TYPES table is not created")
    assert(tables.count(_.name.name.equalsIgnoreCase("RESOURCE_ATTRIBUTES")) == 1, "RESOURCE_ATTRIBUTES table is not created")
    assert(tables.count(_.name.name.equalsIgnoreCase("RESOURCES")) == 1, "RESOURCES table is not created")
  }

  test ("Verify insertion of data into RESOURCE_TYPES table") {
    clearData()

    db withDynSession {
      resourceTypes += (ResourceTypeSlub, "Archive Preferences", "cloud_account_id")
    }

    val isArchiveResourceTypeExist = db withDynSession {
      resourceTypes.filter(_.slug === ResourceTypeSlub).run.nonEmpty
    }

    assert(isArchiveResourceTypeExist, "row not inserted in RESOURCE_TYPES table")
  }

  test ("Verify foreign key constraint of table RESOURCE_ATTRIBUTES") {
    clearData()

    try {
      db withDynSession {
        resourceAttributes +=(1, ResourceTypeSlub, ArchivingStateResourceType, "String", "optional", "in (enabled, disabled)")
      }
      fail() //This will trigger failure if the above insert does not fail
    }
    catch {
      case _: JdbcSQLException =>    // Expected as there is foreign key constraint violation
    }
  }

  test ("Verify insertion of data into table RESOURCE_ATTRIBUTES") {
    clearData()
    initMetaData(db)

    val isArchivingStateResourceAttrExist = db withDynSession {
      resourceAttributes.filter(_.key === ArchivingStateResourceType).run.nonEmpty
    }

    assert(isArchivingStateResourceAttrExist, "row not inserted in RESOURCE_ATTRIBUTES table")
  }


  test ("Verify insertion of data into table RESOURCES") {
    clearData()
    initMetaData(db)

    val tenantId = "tenant_1"

    db withDynSession {
      resources += (ResourceTypeSlub, tenantId, "{\"todo\": \"add JSON goodness\"}")
    }

    val isTenantResourceExist = db withDynSession {
      resources.filter(_.id === tenantId).run.nonEmpty
    }

    assert(isTenantResourceExist, "row not inserted in table RESOURCES")
  }

  test ("Verify only unique combinations of resourceType/tenantId can be inserted into RESOURCES table") {
    clearData()
    initMetaData(db)

    val tenantId = "tenant_1"

    try {
      db withDynSession {
        resources +=(ResourceTypeSlub, tenantId, "{\"todo\": \"add JSON goodness\"}")
        resources +=(ResourceTypeSlub, tenantId, "{\"todo\": \"add JSON goodness\"}")
      }
      fail()  //This will trigger failure if the above insert does not fail
    }
    catch {
      case _: JdbcSQLException =>  // Expected as there is unique key constraint violation
    }
  }

  def clearData() {
    db withDynSession {
      resources.delete
      resourceAttributes.delete
      resourceTypes.delete
    }
  }

  override def afterAll {
    pooledDataSource.close
  }
}
