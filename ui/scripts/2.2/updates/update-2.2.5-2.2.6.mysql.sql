-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.5-2.2.6.mysql.sql
-- 2009-06-23 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id:$
-- /////////////////////////////////////////////////////////////////////////////


-- contact query optimization
CREATE INDEX contact_privacy_key ON Contact (contact_privacy);

-- module 'resource'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'resource', 'resource_delegation', 5, 1);



