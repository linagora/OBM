-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.5-2.2.6.mysql.sql
-- 2009-06-23 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id:$
-- /////////////////////////////////////////////////////////////////////////////


-- contact query optimization
CREATE INDEX contact_privacy_key ON Contact (contact_privacy);

-- contact collected data type fix
ALTER TABLE Contact MODIFY COLUMN contact_collected BOOLEAN DEFAULT FALSE;

-- module 'resource'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resource_delegation', 5, 1);

-- module 'people'
INSERT INTO DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values (null, 'people', 'userobm_delegation', 11, 1);
INSERT INTO DisplayPref (display_user_id, display_entity, display_fieldname, display_fieldorder, display_display) values (null, 'people', 'userobm_vacation', 12, 1);

