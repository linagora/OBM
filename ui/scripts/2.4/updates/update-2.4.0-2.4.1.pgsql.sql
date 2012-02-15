UPDATE ObmInfo SET obminfo_value = '2.4.1-pre' WHERE obminfo_name = 'db_version';

DROP TABLE IF EXISTS opush_invitation_mapping;

CREATE OR REPLACE FUNCTION UUID()
  RETURNS uuid AS
$BODY$
 SELECT CAST(md5(current_database()|| user ||current_timestamp ||random()) as uuid)
$BODY$
  LANGUAGE 'sql' VOLATILE;

ALTER TABLE domain ADD COLUMN domain_uuid char(36);
UPDATE domain SET domain_uuid=UUID() WHERE domain_uuid IS NULL;
ALTER TABLE domain ALTER domain_uuid SET NOT NULL;

ALTER TABLE p_domain ADD COLUMN domain_uuid CHAR(36);
UPDATE p_domain p SET domain_uuid=( select domain_uuid FROM domain d where d.domain_id=p.domain_id);
ALTER TABLE p_domain ALTER domain_uuid SET NOT NULL;

UPDATE event SET event_privacy=0 WHERE event_privacy IS NULL;
ALTER TABLE event ALTER event_privacy SET DEFAULT 0;
ALTER TABLE event ALTER event_privacy SET NOT NULL;

UPDATE ObmInfo SET obminfo_value = '2.4.1' WHERE obminfo_name = 'db_version';

ALTER TABLE eventlink ADD COLUMN eventlink_comment VARCHAR(255);
