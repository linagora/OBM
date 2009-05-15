-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.1-2.2.2.mysql.sql
-- 2009-05-11 Pierre Baudracco
-- /////////////////////////////////////////////////////////////////////////////
-- $Id:$
-- /////////////////////////////////////////////////////////////////////////////


-- Update default section and menu
-- 'com' section is now disabled, 'gw' is now enabled
-- replace 'com' with 'gw' in defined profiles
UPDATE ProfileSection SET profilesection_section_name='gw' WHERE profilesection_section_name='com';
ALTER TABLE Resource DROP KEY k_label_resource;
