UPDATE ObmInfo SET obminfo_value = '2.3.6-pre' WHERE obminfo_name = 'db_version';

UPDATE Event SET event_timezone = (SELECT userobmpref_value FROM UserObmPref WHERE userobmpref_user_id = event_owner AND userobmpref_option = 'set_timezone' LIMIT 1 OFFSET 0) WHERE event_timezone = '' OR event_timezone IS NULL;
UPDATE Event SET event_timezone = (SELECT userobmpref_value FROM UserObmPref WHERE userobmpref_user_id IS NULL AND userobmpref_option = 'set_timezone' LIMIT 1 OFFSET 0) WHERE event_timezone = '' OR event_timezone IS NULL;
UPDATE Event SET event_timezone = 'GMT' WHERE event_timezone = '' OR event_timezone IS NULL;

UPDATE ObmInfo SET obminfo_value = '2.3.6' WHERE obminfo_name = 'db_version';
