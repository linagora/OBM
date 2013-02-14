BEGIN;

UPDATE ObmInfo SET obminfo_value = '2.4.2.2-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE opush_sync_perms ADD COLUMN pending_accept BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE opush_sync_perms ALTER pending_accept DROP DEFAULT;

UPDATE ObmInfo SET obminfo_value = '2.4.2.2' WHERE obminfo_name = 'db_version';

COMMIT;
