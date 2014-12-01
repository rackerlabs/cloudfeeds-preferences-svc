cloudfeeds-preferences-svc
==========================

A web service for managing flat JSON based preferences and metadata

## How to Build
To build this component, we require:
* Gradle version 2.2 or above
* JDK 7
* Scala 2.10 or above


### Simple build
```
gradle clean build
```

### Build a executable Jar
```
gradle clean build uberjar
```

### Build an installable app
```
gradle clean installApp
```

### Build an RPM
```
gradle clean buildRpm
```

## How to run the App
Run the build to create an installable app. Then you should have a startup script in the ```build``` directory, which you can run:
```
gradle clean installApp
sh build/install/cloudfeeds-preferences-svc/bin/cloudfeeds-preferences-svc
```

## Configuration

The Cloud Feeds Preferences Service uses the following configuration files:

### preferences-service.conf
This file configures the Preferences Service app itself. 
https://github.com/rackerlabs/cloudfeeds-preferences-svc/blob/master/src/main/resources/preferences-service.conf

By default, the Preferences Service app will try to find this file from classpath. This can be overriden by specifying the Java System properties ```-Dconfig.file=<path_to_file>```.

### c3p0-config.xml
This file has the C3P0 Connection Pool configuration.
https://github.com/rackerlabs/cloudfeeds-preferences-svc/blob/master/src/main/resources/c3p0-config.xml

By default, the Preferences Service app will try to load this file from classpath. This can be overriden by editing the ```preferences-service.conf``` file above.

### logback.xml
This file has the logging related configuration.
https://github.com/rackerlabs/cloudfeeds-preferences-svc/blob/master/src/main/resources/c3p0-config.xml

By default, the Preferencs Service app will try to load this file from classpath. 
