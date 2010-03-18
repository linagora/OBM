UPDATE ObmInfo SET obminfo_value = '2.3.3-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE Account CHANGE COLUMN account_number account_number varchar(64) default NULL;
ALTER TABLE UGroup CHANGE COLUMN group_ext_id group_ext_id varchar(255) default NULL;
ALTER TABLE P_UGroup CHANGE COLUMN group_ext_id group_ext_id varchar(255) default NULL;

UPDATE ObmInfo SET obminfo_value = '2.3.3' WHERE obminfo_name = 'db_version';
