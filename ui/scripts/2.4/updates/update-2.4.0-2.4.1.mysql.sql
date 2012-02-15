UPDATE ObmInfo SET obminfo_value = '2.4.1-pre' WHERE obminfo_name = 'db_version';

DROP TABLE IF EXISTS opush_invitation_mapping;

ALTER TABLE Domain ADD COLUMN domain_uuid CHAR(36) NOT NULL;
UPDATE Domain SET domain_uuid=UUID() WHERE domain_uuid='';

ALTER TABLE P_Domain ADD COLUMN domain_uuid CHAR(36);
UPDATE P_Domain p, Domain d SET p.domain_uuid=d.domain_uuid where p.domain_id=d.domain_id;
ALTER TABLE P_Domain MODIFY domain_uuid CHAR(36) NOT NULL;

UPDATE opush_sync_mail SET timestamp='1970-01-01 01:00:01' WHERE timestamp='0000-00-00 00:00:00';

UPDATE ObmInfo SET obminfo_value = '2.4.1' WHERE obminfo_name = 'db_version';

ALTER TABLE EventLink ADD COLUMN eventlink_comment VARCHAR(255);
