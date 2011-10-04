-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.15-2.2.16.mysql.sql
-- 2009-12-06 Pierre Baudracco
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////


UPDATE ObmInfo SET obminfo_value = '2.2.16-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE UGroup DROP KEY group_gid;
ALTER TABLE UGroup ADD UNIQUE KEY group_gid (group_domain_id, group_gid);
ALTER TABLE P_UGroup DROP KEY group_gid;
ALTER TABLE P_UGroup ADD UNIQUE KEY group_gid (group_domain_id, group_gid);

UPDATE ProjectUser SET projectuser_projectedtime=NULL WHERE projectuser_projecttask_id IS NULL AND projectuser_projectedtime=0;

UPDATE ObmInfo SET obminfo_value = '2.2.16' WHERE obminfo_name = 'db_version';
