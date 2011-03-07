UPDATE ObmInfo SET obminfo_value = '2.3.18-pre' WHERE obminfo_name = 'db_version';

-- Modification of the hit rate column to be NOT NULL
UPDATE Deal SET deal_hitrate=0 WHERE deal_hitrate IS NULL;
ALTER TABLE Deal ALTER COLUMN deal_hitrate SET NOT NULL;

-- Modification the lastname to allow NULL values (necessary when importing ics)
ALTER TABLE `Contact` CHANGE COLUMN contact_lastname `contact_lastname` character varying(64) DEFAULT NULL;

ALTER TABLE opush_sync_mail ADD COLUMN is_read boolean DEFAULT false;

UPDATE ObmInfo SET obminfo_value = '2.3.18' WHERE obminfo_name = 'db_version';

