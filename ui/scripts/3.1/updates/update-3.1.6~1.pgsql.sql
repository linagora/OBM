BEGIN;

ALTER TABLE mail_archive_processed_folder DROP mail_archive_processed_folder_lastuid;

ALTER TABLE mail_archive ADD COLUMN mail_archive_move boolean default false;

UPDATE ObmInfo SET obminfo_value = '3.1.6' WHERE obminfo_name = 'db_version';

COMMIT;
