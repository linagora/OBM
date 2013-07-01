--
-- Adds a new preference 'set_top_bar'
--

BEGIN;

INSERT INTO userobmpref (userobmpref_option, userobmpref_value)
SELECT 'set_top_bar', 'yes' 
WHERE NOT EXISTS (SELECT 1 FROM userobmpref WHERE userobmpref_option='set_top_bar');

INSERT INTO userobmpref (userobmpref_user_id, userobmpref_option, userobmpref_value)
SELECT userobm_id, 'set_top_bar', 'no' FROM userobm;

COMMIT;
