BEGIN;

UPDATE Contact SET contact_company_id = (SELECT company_id FROM Company WHERE company_name=contact_company LIMIT 1) WHERE contact_company IS NOT NULL AND contact_company_id IS NULL;

COMMIT;
