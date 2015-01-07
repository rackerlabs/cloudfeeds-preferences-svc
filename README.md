cloudfeeds-preferences-svc-db
=============================

This project contains the db schema definition which Cloud Feeds Preferences
Service(https://github.com/rackerlabs/cloudfeeds-preferences-svc) uses. The code in
this repository uses flyway-command-line(http://flywaydb.org/documentation/commandline/) for database
migration.

Any service/product/team who might want to create their own installation of Cloud Feeds Preferences
Service can use this project to create database for it. (DDL's are written for postgres but might work
with other sql complaint databases also)

## *IMPORTANT*
Any time you create a new table, you must have a grant to allow the app access
and properly set ownership of the new tables.  See V1.2.1_grants.sql as an
example file that can be used for the grants and ownership change.

## How to Build
To build this component, we require:
* Gradle version 2.2 or above
* JDK 7


### Build to generate zip file containing flyway command line tools
```
gradle clean buildZip
```

## How to run the App
Run the build to generate zip file.
Unzip the zip file(```build/distributions/cloudfeeds-preferences-svc-db.zip```) to a local folder.
Command to run flyway script on a h2 test db.

### Local H2 example
```
gradle clean buildZip
cd build/distributions/
unzip cloudfeeds-preferences-svc-db.zip

sh flyway -user=root -url=jdbc:h2:/Users/chan5120/testdb migrate
```

### Remote PostgreSQL example
```
gradle clean buildZip
cd build/distributions/
unzip cloudfeeds-preferences-svc-db.zip
read -p "Username? " -s username ; read -p "Password? " -s passwd ; \
bash flyway -url='jdbc:postgresql://myhostname:5432/cloudfeeds_prefs?sslmode=require' -user=$username -password=$passwd migrate
```

## Command line options

Command line options to migrate can be found at http://flywaydb.org/documentation/commandline/migrate.html
