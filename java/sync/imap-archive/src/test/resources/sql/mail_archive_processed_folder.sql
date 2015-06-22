CREATE DOMAIN mail_archive_status AS VARCHAR (16) CHECK VALUE IN (
   'SCHEDULED',
   'RUNNING',
   'ERROR',
   'SUCCESS'
);

CREATE TABLE mail_archive_run (
   id                                 SERIAL PRIMARY KEY,
   mail_archive_run_uuid              character(36) NOT NULL,
   mail_archive_run_domain_uuid       character(36) NOT NULL,
   mail_archive_run_status            mail_archive_status NOT NULL,
   mail_archive_run_schedule          TIMESTAMP,
   mail_archive_run_start             TIMESTAMP,
   mail_archive_run_end               TIMESTAMP,
   mail_archive_run_higher_boundary   TIMESTAMP,

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
   mail_archive_processed_folder_start      TIMESTAMP NOT NULL,
   mail_archive_processed_folder_end        TIMESTAMP NOT NULL,
   mail_archive_processed_folder_status    mail_archive_status NOT NULL,

   CONSTRAINT mail_archive_processed_folder_ukey UNIQUE (mail_archive_processed_folder_run_uuid, mail_archive_processed_folder_id),
   CONSTRAINT mail_archive_processed_folder_run_id_fkey FOREIGN KEY (mail_archive_processed_folder_run_uuid) REFERENCES mail_archive_run(mail_archive_run_uuid) ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT mail_archive_processed_folder_id_fkey FOREIGN KEY (mail_archive_processed_folder_id) REFERENCES mail_archive_folder(id) ON UPDATE CASCADE ON DELETE CASCADE
);
