-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.9-2.2.10.pgsql.sql
-- 2009-08-17 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////

UPDATE ObmInfo SET obminfo_value = '2.2.10' WHERE obminfo_name = 'db_version';
