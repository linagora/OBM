-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.13-2.2.14.pgsql.sql
-- 2009-10-14 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////

UPDATE ObmInfo SET obminfo_value = '2.2.14-pre' WHERE obminfo_name = 'db_version';
UPDATE ObmInfo SET obminfo_value = '2.2.14' WHERE obminfo_name = 'db_version';

