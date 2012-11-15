--
-- Adds a new preference 'set_allday_opacity'
--

BEGIN;

INSERT INTO UserObmPref (userobmpref_option, userobmpref_value)
SELECT 'set_allday_opacity', 'TRANSPARENT' FROM Dual
WHERE NOT EXISTS (SELECT 1 FROM UserObmPref WHERE userobmpref_option='set_allday_opacity');

COMMIT;