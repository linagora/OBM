
--
-- Adds resource_email column
--
 
BEGIN;
 
ALTER TABLE resource ADD COLUMN resource_email TEXT;

UPDATE resource r SET resource_email='res-' || r.resource_id || '@' || (SELECT d.domain_name FROM domain d WHERE d.domain_id=r.resource_domain_id);

-- Not set to NOT NULL on purpose (otherwise we have problems during insert)

ALTER TABLE ONLY resource ADD CONSTRAINT resource_resource_email_key UNIQUE(resource_email);

COMMIT;
