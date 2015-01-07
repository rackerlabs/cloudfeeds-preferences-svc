-- this must be done every time there is a creation of a new table.
REASSIGN OWNED BY prefs_app_super_svc TO prefs_app_super_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO prefs_app_role;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO prefs_app_role;
