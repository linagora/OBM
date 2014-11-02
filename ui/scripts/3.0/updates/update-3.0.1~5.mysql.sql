BEGIN;

UPDATE rc_system SET value = '2014042900' WHERE name = 'roundcube-version';
UPDATE ObmInfo SET obminfo_value = '3.0.1' WHERE obminfo_name = 'db_version';

COMMIT;
