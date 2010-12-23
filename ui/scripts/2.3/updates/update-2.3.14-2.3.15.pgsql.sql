UPDATE ObmInfo SET obminfo_value = '2.3.15-pre' WHERE obminfo_name = 'db_version';
ALTER TABLE ObmBookmarkProperty ALTER COLUMN obmbookmarkproperty_value TYPE varchar(255);
ALTER TABLE Event ALTER COLUMN event_location TYPE varchar(255);
UPDATE ObmInfo SET obminfo_value = '2.3.15' WHERE obminfo_name = 'db_version';
