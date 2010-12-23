UPDATE ObmInfo SET obminfo_value = '2.3.15-pre' WHERE obminfo_name = 'db_version';
ALTER TABLE Event CHANGE COLUMN event_location event_location varchar(255) default NULL;
UPDATE ObmInfo SET obminfo_value = '2.3.15' WHERE obminfo_name = 'db_version';
