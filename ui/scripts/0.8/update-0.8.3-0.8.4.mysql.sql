-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.3 to 0.8.4                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update ContactList table for conformance
-------------------------------------------------------------------------------
ALTER TABLE ContactList CHANGE ContactList_listid contactlist_list_id int(8) DEFAULT '0' NOT NULL;
ALTER TABLE ContactList CHANGE ContactList_contactid contactlist_contact_id int(8) DEFAULT '0' NOT NULL;
