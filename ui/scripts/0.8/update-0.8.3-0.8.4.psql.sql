-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.3 to 0.8.4                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update ContactList table for conformance
-------------------------------------------------------------------------------
ALTER TABLE ContactList RENAME COLUMN ContactList_listid TO contactlist_list_id;
ALTER TABLE ContactList RENAME COLUMN ContactList_contactid TO contactlist_contact_id;