BEGIN;

UPDATE ObmInfo SET obminfo_value = '2.5.0' WHERE obminfo_name = 'db_version';

COMMIT;
