-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.4 to 0.8.5                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update UserObmGroup table for conformance
-------------------------------------------------------------------------------
ALTER TABLE UserObmGroup RENAME COLUMN userobmgroup_groupid TO userobmgroup_group_id;
ALTER TABLE UserObmGroup RENAME COLUMN userobmgroup_userobmid TO userobmgroup_userobm_id;


-------------------------------------------------------------------------------
-- Update GroupGroup table for conformance
-------------------------------------------------------------------------------
ALTER TABLE GroupGroup RENAME COLUMN groupgroup_parentid TO groupgroup_parent_id;
ALTER TABLE GroupGroup RENAME COLUMN groupgroup_childid TO groupgroup_child_id;


-------------------------------------------------------------------------------
-- Update Document table for conformance
-------------------------------------------------------------------------------
ALTER TABLE Document ADD COLUMN document_category1_id integer;
ALTER TABLE Document ALTER COLUMN document_category1_id SET DEFAULT '0';
UPDATE Document set document_category1_id=to_number(document_category1, '9999') where document_category1 != '';
ALTER TABLE Document DROP COLUMN document_category1;

ALTER TABLE Document ADD COLUMN document_category2_id integer;
ALTER TABLE Document ALTER COLUMN document_category2_id SET DEFAULT '0';
UPDATE Document set document_category2_id=to_number(document_category2, '9999') where document_category2 != '';
ALTER TABLE Document DROP COLUMN document_category2;


-------------------------------------------------------------------------------
-- Update Invoice table for conformance
-------------------------------------------------------------------------------
ALTER TABLE Invoice RENAME COLUMN invoice_invoicestatus_id TO invoice_status_id;
UPDATE DisplayPref set display_fieldname='invoice_amount_ht' where display_fieldname='invoice_amount_HT';
UPDATE DisplayPref set display_fieldname='invoice_amount_ttc' where display_fieldname='invoice_amount_TTC';


-------------------------------------------------------------------------------
-- Update Contact, Deal, List, Document, CalendarEvent tables
-- for privacy conformance
-------------------------------------------------------------------------------
ALTER TABLE Contact RENAME COLUMN contact_visibility TO contact_privacy;
ALTER TABLE Deal RENAME COLUMN deal_visibility TO deal_privacy;
ALTER TABLE List RENAME COLUMN list_visibility TO list_privacy;
ALTER TABLE Document RENAME COLUMN document_private TO document_privacy;
ALTER TABLE Document ALTER COLUMN document_privacy SET DEFAULT '0';
ALTER TABLE Document ALTER COLUMN document_privacy SET NOT NULL;
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_privacy SET DEFAULT '0';
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_privacy SET NOT NULL;


-------------------------------------------------------------------------------
-- Clean Up UserObmPref table from Todo data
-------------------------------------------------------------------------------
DELETE FROM UserObmPref where userobmpref_option like 'todo_%';


-------------------------------------------------------------------------------
-- Update Incident table
-------------------------------------------------------------------------------
-- update column date from date to timestamp
ALTER TABLE Incident RENAME COLUMN incident_date TO incident_datetmp;
ALTER TABLE Incident ADD COLUMN incident_date TIMESTAMP;
UPDATE Incident set incident_date = incident_datetmp;
ALTER TABLE Incident DROP COLUMN incident_datetmp;


-------------------------------------------------------------------------------
-- UserObm table
-------------------------------------------------------------------------------
-- Add column : userobm_datebegin
ALTER TABLE UserObm ADD COLUMN userobm_datebegin date;


-------------------------------------------------------------------------------
-- List table
-------------------------------------------------------------------------------
-- Add column : list_static_nb
ALTER TABLE List ADD COLUMN list_static_nb integer;
ALTER TABLE List ALTER COLUMN list_static_nb SET DEFAULT 0;
