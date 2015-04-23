create table "preferences_metadata" (
   "slug" VARCHAR NOT NULL PRIMARY KEY,
   "description" VARCHAR NOT NULL,
   "schema" VARCHAR NOT NULL
);

create table "preferences" (
   "id" VARCHAR NOT NULL,
   "preferences_metadata_slug" VARCHAR NOT NULL,
   "payload" VARCHAR NOT NULL,
   "created" TIMESTAMP NOT NULL DEFAULT current_timestamp,
   "updated" TIMESTAMP NOT NULL DEFAULT current_timestamp
);

alter table "preferences" add constraint "compound_pk" primary key (
   "id","preferences_metadata_slug"
);

alter table "preferences" add constraint "preferences_metadata_slug_fk" foreign key (
   "preferences_metadata_slug"
)
references "preferences_metadata"("slug") on
update NO ACTION on
delete NO ACTION;
