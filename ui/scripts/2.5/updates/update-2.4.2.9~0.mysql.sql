BEGIN;

UPDATE ObmInfo SET obminfo_value = '2.4.2.9-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE trusttoken ADD COLUMN email text DEFAULT NULL;
ALTER TABLE trusttoken ADD COLUMN password VARCHAR(255) DEFAULT NULL;

UPDATE ObmInfo SET obminfo_value = '2.4.2.9' WHERE obminfo_name = 'db_version';
COMMIT;
