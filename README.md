cloudfeeds-preferences-svc-db
=============================

This project contains the db schema definition which Cloud Feeds Preferences 
Service(https://github.com/rackerlabs/cloudfeeds-preferences-svc) uses. The code in 
this repository uses flyway-command-line(http://flywaydb.org/documentation/commandline/) for database
migration.

Any service/product/team who might want to create their own installation of Cloud Feeds Preferences 
Service can use this project to create database for it. (DDL's are written for postgres but might work
with other sql complaint databases also)

## How to Build
To build this component, we require:
* Gradle version 2.2 or above
* JDK 7


### Build to generate zip file containing flyway command line tools
```
gradle clean buildZip
```

### Build a Jar
```
gradle clean build jar
```


Note: The current c3p0-config.xml file in classpath expects this file in home directory. You can change ```jdbcUrl``` property to point to the location of your database.

## How to run the App
Run the build to generate zip file. 
Unzip the zip file(```build/distributions/cloudfeeds-preferences-svc-db.zip```) to a local folder.
Command to run flyway script on a h2 test db.


```
gradle clean buildZip 
cd build/distributions/  
unzip cloudfeeds-preferences-svc-db.zip
                                         
sh flyway -user=root -url=jdbc:h2:/Users/chan5120/testdb migrate                                         
```

## Command line options

Command line options to migrate can be found at http://flywaydb.org/documentation/commandline/migrate.html
