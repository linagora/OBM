
--
-- Adds resource_email column
--
 
BEGIN;
 
ALTER TABLE Resource ADD COLUMN resource_email TEXT;

UPDATE Resource r SET resource_email='res-' || r.resource_id || '@' || (SELECT d.domain_name FROM domain d WHERE d.domain_id=r.resource_domain_id);

-- Not set to NOT NULL on purpose (otherwise we have problems during insert)

ALTER TABLE Resource ADD UNIQUE INDEX (resource_email(100));

COMMIT;
