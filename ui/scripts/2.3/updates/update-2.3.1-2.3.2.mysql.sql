UPDATE ObmInfo SET obminfo_value = '2.3.2-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE UserObm DROP COLUMN userobm_nomade_datebegin;
ALTER TABLE UserObm DROP COLUMN userobm_nomade_dateend;

UPDATE ObmInfo SET obminfo_value = '2.3.2' WHERE obminfo_name = 'db_version';
