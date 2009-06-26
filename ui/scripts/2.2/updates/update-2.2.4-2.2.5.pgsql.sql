-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.4-2.2.5.pgsql.sql
-- 2009-06-02 David Phan
-- /////////////////////////////////////////////////////////////////////////////
-- $Id:$
-- /////////////////////////////////////////////////////////////////////////////

DELETE FROM DisplayPref WHERE display_entity='resource';
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resource_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resource_delegation', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resource_description', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resource_qty', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resourcetype_label', 5, 1);
