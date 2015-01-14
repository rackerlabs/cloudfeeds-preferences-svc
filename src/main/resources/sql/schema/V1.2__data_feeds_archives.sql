insert
  into "preferences_metadata" ("slug", "description", "schema")
values ('archive',
        'Cloud feeds Archive Preferences',
        'object {
            boolean enabled;
            array [
              string [ "JSON", "XML" ];
            ] {1,2} data_format;
            string  default_region?;
            string  default_container_name = "FeedsArchives"?;
            object {
              string iad;
              string ord;
              string dfw;
              string lon;
              string hkg;
              string syd;
            } archive_container_urls?;
        };');



