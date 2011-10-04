UPDATE ObmInfo SET obminfo_value = '2.3.2-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE UserObm DROP COLUMN userobm_nomade_datebegin;
ALTER TABLE UserObm DROP COLUMN userobm_nomade_dateend;

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (NULL,'parentdeal','parentdeal_archive',4,1);

UPDATE ObmInfo SET obminfo_value = '2.3.2' WHERE obminfo_name = 'db_version';
