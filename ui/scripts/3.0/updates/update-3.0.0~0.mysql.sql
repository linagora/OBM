--
-- Adds a new preference 'set_top_bar'
--

BEGIN;

INSERT INTO UserObmPref (userobmpref_option, userobmpref_value)
SELECT 'set_top_bar', 'yes' FROM Dual
WHERE NOT EXISTS (SELECT 1 FROM UserObmPref WHERE userobmpref_option='set_top_bar');

INSERT INTO UserObmPref (userobmpref_user_id, userobmpref_option, userobmpref_value)
SELECT userobm_id, 'set_top_bar', 'no' FROM UserObm;

COMMIT;
