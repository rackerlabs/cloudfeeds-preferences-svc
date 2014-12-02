cloudfeeds-preferences-svc
==========================

Cloud Feeds Preferences Service is a web service for managing flat JSON based preferences and metadata. 
Even though this service is initally conceived and now maintained by the Cloud Feeds team, it is 
a generic service that can be used by any team. The code in this repository does not have anything 
related to Cloud Feeds API. It is the goal of the Cloud Feeds team to keep this code generic 
as much as possible.

Any service/product/team who might want to expose a set of preferences to its consumers and 
to provide a set of API to set those preferences can stand up and deploy this service in their 
environments. The preferences is a flat JSON string value list. This service also provides 
an API to fetch the preferences' metadata.

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

If you are running this on Mac and you get ```UnknownHostException```, it's probably because your Mac's hostname is not the same as localhost. To fix this, run the following:
```
scutil --set "HostName" localhost
```

## Configuration

The Cloud Feeds Preferences Service uses the following configuration files:

### preferences-service.conf
This [preferences-service.conf](https://github.com/rackerlabs/cloudfeeds-preferences-svc/blob/master/src/main/resources/preferences-service.conf) file configures the Preferences Service app itself. 

By default, the Preferences Service app will try to find this file from classpath. This can be overriden by specifying the Java System properties ```-Dconfig.file=<path_to_file>```.

### c3p0-config.xml
This [c3p0-config.xml](https://github.com/rackerlabs/cloudfeeds-preferences-svc/blob/master/src/main/resources/c3p0-config.xml) file has the C3P0 Connection Pool configuration.

By default, the Preferences Service app will try to load this file from classpath. This can be overriden by editing the ```preferences-service.conf``` file above.

### logback.xml
This [logback.xml](https://github.com/rackerlabs/cloudfeeds-preferences-svc/blob/master/src/main/resources/c3p0-config.xml) file has the logging related configuration.

By default, the Preferencs Service app will try to load this file from classpath. This can be overriden by editing the ```preferences-service.conf``` file above.
