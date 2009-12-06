-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.15-2.2.16.pgsql.sql
-- 2009-12-06 Pierre Baudracco
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////


UPDATE ObmInfo SET obminfo_value = '2.2.16-pre' WHERE obminfo_name = 'db_version';

UPDATE ProjectUser SET projectuser_projectedtime=NULL WHERE projectuser_projecttask_id IS NULL AND projectuser_projected_time=0;

UPDATE ObmInfo SET obminfo_value = '2.2.16' WHERE obminfo_name = 'db_version';
