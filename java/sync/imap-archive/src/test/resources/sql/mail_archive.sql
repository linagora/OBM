CREATE DOMAIN repeat_kind AS VARCHAR (16) CHECK VALUE IN (
 'DAILY',
 'WEEKLY',
 'MONTHLY',
 'YEARLY'
);

CREATE TABLE mail_archive (
 id                           SERIAL PRIMARY KEY,
 mail_archive_domain_uuid     character(36) NOT NULL,
 mail_archive_activated       BOOLEAN DEFAULT FALSE,
 mail_archive_main_folder     TEXT NOT NULL,
 mail_archive_repeat_kind     repeat_kind,
 mail_archive_day_of_week     INTEGER,
 mail_archive_day_of_month    INTEGER,
 mail_archive_day_of_year     INTEGER,
 mail_archive_hour            INTEGER,
 mail_archive_minute          INTEGER,
 mail_archive_excluded_folder TEXT,

 CONSTRAINT mail_archive_domain_uuid_ukey UNIQUE (mail_archive_domain_uuid)
);

CREATE TABLE mail_archive_excluded_users (
 id                                      SERIAL PRIMARY KEY,
 mail_archive_excluded_users_domain_uuid character(36) NOT NULL,
 mail_archive_excluded_users_user_uuid   character(36) NOT NULL,
 mail_archive_excluded_users_user_login  TEXT NOT NULL,

 CONSTRAINT mail_archive_excluded_users_ukey UNIQUE (mail_archive_excluded_users_domain_uuid, mail_archive_excluded_users_user_uuid)
);

CREATE TABLE mail_archive_mailing (
 id                               SERIAL PRIMARY KEY,
 mail_archive_mailing_domain_uuid character(36) NOT NULL,
 mail_archive_mailing_email       TEXT NOT NULL,

 CONSTRAINT mail_archive_mailing_ukey UNIQUE (mail_archive_mailing_domain_uuid, mail_archive_mailing_email)
);
