preferences-service
===================

A web service for managing flat JSON based preferences and metadata

Configuration
-------------

The preferences service is configured by the following environment variables:

Environment Variable  | Meaning
----------------------|-----------------------------------------------------------------
PREFS_ENVIRONMENT     | Environment name (production, staging, test, development, etc…). Defaults to development
PREFS_PORT            | The port the webapp should listen on. Defaults to 8080
