update "preferences_metadata"
set "schema" = 'object {
            boolean enabled;
            array [
              string [ "JSON", "XML" ];
            ] {1,2} data_format;
            string  default_archive_container_url?;
            object {
              string iad?;
              string ord?;
              string dfw?;
              string lon?;
              string hkg?;
              string syd?;
            } archive_container_urls?;
        };'
where "slug" = 'archive';


