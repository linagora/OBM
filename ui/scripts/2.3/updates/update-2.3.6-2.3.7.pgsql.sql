UPDATE ObmInfo SET obminfo_value = '2.3.7-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE opush_sync_state DROP CONSTRAINT unique_opush_col_dev;

UPDATE ObmInfo SET obminfo_value = '2.3.7' WHERE obminfo_name = 'db_version';
