-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.4 to 0.8.5                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update UserObmGroup table for conformance
-------------------------------------------------------------------------------
ALTER TABLE UserObmGroup CHANGE userobmgroup_groupid userobmgroup_group_id int(8) DEFAULT '0' NOT NULL;
ALTER TABLE UserObmGroup CHANGE userobmgroup_userobmid userobmgroup_userobm_id int(8) DEFAULT '0' NOT NULL;


-------------------------------------------------------------------------------
-- Update GroupGroup table for conformance
-------------------------------------------------------------------------------
ALTER TABLE GroupGroup CHANGE groupgroup_parentid groupgroup_parent_id int(8) DEFAULT '0' NOT NULL;
ALTER TABLE GroupGroup CHANGE groupgroup_childid groupgroup_child_id int(8) DEFAULT '0' NOT NULL;


-------------------------------------------------------------------------------
-- Update Document table for conformance
-------------------------------------------------------------------------------
ALTER TABLE Document CHANGE document_category1 document_category1_id int(8) not null default '0';
ALTER TABLE Document CHANGE document_category2 document_category2_id int(8) not null default '0';


-------------------------------------------------------------------------------
-- Update Invoice table for conformance
-------------------------------------------------------------------------------
ALTER TABLE Invoice CHANGE invoice_invoicestatus_id invoice_status_id int(4) not null default '0';
ALTER TABLE Invoice CHANGE invoice_amount_HT invoice_amount_ht double(10,2);
ALTER TABLE Invoice CHANGE invoice_amount_TTC invoice_amount_ttc double(10,2);
UPDATE DisplayPref set display_fieldname="invoice_amount_ht" where display_fieldname="invoice_amount_HT";
UPDATE DisplayPref set display_fieldname="invoice_amount_ttc" where display_fieldname="invoice_amount_TTC";


-------------------------------------------------------------------------------
-- Update Contact, Deal, List, Document, CalendarEvent tables
-- for privacy conformance
-------------------------------------------------------------------------------
ALTER TABLE Contact CHANGE contact_visibility contact_privacy int(2) NOT NULL DEFAULT '0';
ALTER TABLE Deal CHANGE deal_visibility deal_privacy int(2) NOT NULL DEFAULT '0';
ALTER TABLE List CHANGE list_visibility list_privacy int(2) NOT NULL DEFAULT '0';
ALTER TABLE Document CHANGE document_private document_privacy int(2) NOT NULL DEFAULT '0';
ALTER TABLE CalendarEvent CHANGE calendarevent_privacy calendarevent_privacy int(2) NOT NULL DEFAULT '0';


-------------------------------------------------------------------------------
-- Clean Up UserObmPref table from Todo data
-------------------------------------------------------------------------------
DELETE FROM UserObmPref where userobmpref_option like 'todo_%';


-------------------------------------------------------------------------------
-- UserObm table
-------------------------------------------------------------------------------
-- Add column : userobm_datebegin
ALTER TABLE UserObm ADD COLUMN userobm_datebegin date after userobm_archive;


-------------------------------------------------------------------------------
-- List table
-------------------------------------------------------------------------------
-- Add column : list_static_nb
ALTER TABLE List ADD COLUMN list_static_nb int(10) DEFAULT 0 after list_mailing_ok;
