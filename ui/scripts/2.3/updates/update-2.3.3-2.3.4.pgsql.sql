UPDATE ObmInfo SET obminfo_value = '2.3.4-pre' WHERE obminfo_name = 'db_version';

INSERT INTO UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (NULL,'set_public_fb','0');

UPDATE ObmInfo SET obminfo_value = '2.3.4' WHERE obminfo_name = 'db_version';
