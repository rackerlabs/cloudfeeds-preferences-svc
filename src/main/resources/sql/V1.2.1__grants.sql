-- this must be done every time there is a creation of a new table.
GRANT SELECT, INSERT, UPDATE, DELETE ON preferences TO prefs_app_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON preferences_metadata TO prefs_app_role;
GRANT USAGE, SELECT, UPDATE ON preferences_metadata_id_seq TO prefs_app_role;
ALTER TABLE preferences owner TO prefs_app_super_role;
ALTER TABLE preferences_metadata owner TO prefs_app_super_role;
