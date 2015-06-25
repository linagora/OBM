BEGIN;

ALTER TABLE mail_archive_processed_folder DROP mail_archive_processed_folder_lastuid;

ALTER TABLE mail_archive ADD COLUMN mail_archive_move boolean default false;

ALTER TABLE mail_archive ADD mail_archive_scope_includes  BOOLEAN DEFAULT FALSE;

DROP TABLE mail_archive_excluded_users;

CREATE TABLE mail_archive_scope_users (
 id                                    SERIAL PRIMARY KEY,
 mail_archive_scope_users_domain_uuid  character(36) NOT NULL,
 mail_archive_scope_users_user_uuid    character(36) NOT NULL,
 mail_archive_scope_users_user_login   TEXT NOT NULL,

 CONSTRAINT mail_archive_scope_users_ukey UNIQUE (mail_archive_scope_users_domain_uuid, mail_archive_scope_users_user_uuid)
);

UPDATE ObmInfo SET obminfo_value='3.1.6' WHERE obminfo_name='db_version';

COMMIT;
