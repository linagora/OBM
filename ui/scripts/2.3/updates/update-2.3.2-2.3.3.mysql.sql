UPDATE ObmInfo SET obminfo_value = '2.3.2-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE Account CHANGE COLUMN account_number account_number varchar(64) default NULL;

UPDATE ObmInfo SET obminfo_value = '2.3.2' WHERE obminfo_name = 'db_version';
