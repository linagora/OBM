UPDATE ObmInfo SET obminfo_value = '2.3.3-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE Account ALTER COLUMN account_number TYPE varchar(64);

UPDATE ObmInfo SET obminfo_value = '2.3.3' WHERE obminfo_name = 'db_version';
