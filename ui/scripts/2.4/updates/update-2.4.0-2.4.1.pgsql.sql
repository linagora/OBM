UPDATE ObmInfo SET obminfo_value = '2.4.1-pre' WHERE obminfo_name = 'db_version';

DROP TABLE IF EXISTS opush_invitation_mapping;

ALTER TABLE domain ADD COLUMN domain_uuid char(36);
UPDATE domain SET domain_uuid=UUID() WHERE domain_uuid IS NULL;
ALTER TABLE domain ALTER domain_uuid SET NOT NULL;

ALTER TABLE p_domain ADD COLUMN domain_uuid CHAR(36);
UPDATE p_domain p SET domain_uuid=( select domain_uuid FROM domain d where d.domain_id=p.domain_id);
ALTER TABLE p_domain ALTER domain_uuid SET NOT NULL;

UPDATE ObmInfo SET obminfo_value = '2.4.1' WHERE obminfo_name = 'db_version';
