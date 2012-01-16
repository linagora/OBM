UPDATE ObmInfo SET obminfo_value = '2.4.0-pre' WHERE obminfo_name = 'db_version';

DROP TABLE IF EXISTS opush_invitation_mapping;

UPDATE ObmInfo SET obminfo_value = '2.4.0' WHERE obminfo_name = 'db_version';
