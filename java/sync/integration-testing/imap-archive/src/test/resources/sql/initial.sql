CREATE DOMAIN repeat_kind AS VARCHAR (16) CHECK VALUE IN (
   'DAILY',
   'WEEKLY',
   'MONTHLY',
   'YEARLY'
);

CREATE TABLE mail_archive (
   id                             SERIAL PRIMARY KEY,
   mail_archive_domain_uuid       character(36) NOT NULL,
   mail_archive_activated         BOOLEAN DEFAULT FALSE,
   mail_archive_repeat_kind       repeat_kind,
   mail_archive_day_of_week       INTEGER,
   mail_archive_day_of_month      INTEGER,
   mail_archive_day_of_year       INTEGER,
   mail_archive_hour              INTEGER,
   mail_archive_minute            INTEGER,
   mail_archive_excluded_folder   TEXT,

   CONSTRAINT mail_archive_domain_uuid_ukey UNIQUE (mail_archive_domain_uuid)
);

CREATE TABLE mail_archive_excluded_users (
   id                                        SERIAL PRIMARY KEY,
   mail_archive_excluded_users_domain_uuid   character(36) NOT NULL,
   mail_archive_excluded_users_user_uuid     character(36) NOT NULL,
   mail_archive_excluded_users_user_login    TEXT NOT NULL,

   CONSTRAINT mail_archive_excluded_users_ukey UNIQUE (mail_archive_excluded_users_domain_uuid, mail_archive_excluded_users_user_uuid)
);

CREATE TABLE mail_archive_mailing (
   id                                 SERIAL PRIMARY KEY,
   mail_archive_mailing_domain_uuid   character(36) NOT NULL,
   mail_archive_mailing_email         TEXT NOT NULL,

   CONSTRAINT mail_archive_mailing_ukey UNIQUE (mail_archive_mailing_domain_uuid, mail_archive_mailing_email)
);

CREATE DOMAIN mail_archive_status AS VARCHAR (16) CHECK VALUE IN (
   'SCHEDULED',
   'RUNNING',
   'ERROR',
   'SUCCESS'
);

CREATE TABLE mail_archive_run (
   id                                SERIAL PRIMARY KEY,
   mail_archive_run_uuid             character(36) NOT NULL,
   mail_archive_run_domain_uuid      character(36) NOT NULL,
   mail_archive_run_status           mail_archive_status NOT NULL,
   mail_archive_run_schedule         TIMESTAMP,
   mail_archive_run_start            TIMESTAMP,
   mail_archive_run_end              TIMESTAMP,
   mail_archive_run_higher_boundary  TIMESTAMP,
   mail_archive_run_recurrent        BOOLEAN NOT NULL,

   CONSTRAINT mail_archive_run_uuid_ukey UNIQUE (mail_archive_run_uuid)
);

CREATE TABLE mail_archive_folder (
   id      SERIAL PRIMARY KEY,
   folder  TEXT,

   CONSTRAINT mail_archive_folder_folder_ukey UNIQUE (folder)
);

CREATE TABLE mail_archive_processed_folder (
   id                                       SERIAL PRIMARY KEY,
   mail_archive_processed_folder_run_uuid   character(36) NOT NULL,
   mail_archive_processed_folder_id         INTEGER NOT NULL,
   mail_archive_processed_folder_lastuid    NUMERIC(10,0) NOT NULL,
   mail_archive_processed_folder_start      TIMESTAMP NOT NULL,
   mail_archive_processed_folder_end        TIMESTAMP NOT NULL,
    mail_archive_processed_folder_status    mail_archive_status NOT NULL,

   CONSTRAINT mail_archive_processed_folder_ukey UNIQUE (mail_archive_processed_folder_run_uuid, mail_archive_processed_folder_id),
   CONSTRAINT mail_archive_processed_folder_run_id_fkey FOREIGN KEY (mail_archive_processed_folder_run_uuid) REFERENCES mail_archive_run(mail_archive_run_uuid) ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT mail_archive_processed_folder_id_fkey FOREIGN KEY (mail_archive_processed_folder_id) REFERENCES mail_archive_folder(id) ON UPDATE CASCADE ON DELETE CASCADE
);
