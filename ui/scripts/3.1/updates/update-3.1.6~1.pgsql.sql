BEGIN;

ALTER TABLE mail_archive_processed_folder DROP mail_archive_processed_folder_lastuid;

UPDATE ObmInfo SET obminfo_value='3.1.6' WHERE obminfo_name='db_version';

COMMIT;
