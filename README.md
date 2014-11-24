preferences-service
===================

A web service for managing flat JSON based preferences and metadata

## How to Build
To build this component, we require Gradle version 2.2 and above.


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

## How to run the App
Run the build to create an installable app. Then you should have a startup script in the ```build``` directory, which you can run:
```
sh build/install/preferences-service/bin/preferences-service
```


Configuration
-------------

The preferences service is configured by the following environment variables:

Environment Variable  | Meaning
----------------------|-----------------------------------------------------------------
PREFS_ENVIRONMENT     | Environment name (production, staging, test, development, etc…). Defaults to development
PREFS_PORT            | The port the webapp should listen on. Defaults to 8080
