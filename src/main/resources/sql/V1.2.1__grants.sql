-- this must be done every time there is a creation of a new table.
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO prefs_app_role;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO prefs_app_role;
ALTER TABLE preferences owner TO prefs_app_super_role;
ALTER TABLE preferences_metadata owner TO prefs_app_super_role;
