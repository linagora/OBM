-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.9 to 1.0                         //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.0' where obminfo_name='db_version';

-------------------------------------------------------------------------------
-- Update Contact table
-------------------------------------------------------------------------------
-- Add columns comment2, comment3
ALTER TABLE Contact ADD COLUMN contact_comment2 text;
ALTER TABLE Contact ADD COLUMN contact_comment3 text;
ALTER TABLE Contact ADD COLUMN contact_date TIMESTAMP;

-------------------------------------------------------------------------------
-- Tables needed for Contact Module
-------------------------------------------------------------------------------


--
-- Table structure for table 'ContactCategory3'
--
CREATE TABLE ContactCategory3 (
  contactcategory3_id          serial,
  contactcategory3_timeupdate  timestamp,
  contactcategory3_timecreate  timestamp,
  contactcategory3_userupdate  integer default 0,
  contactcategory3_usercreate  integer default 0,
  contactcategory3_code        varchar(10) default '',
  contactcategory3_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory3_id)
);


--
-- Table structure for table 'ContactCategory3Link'
--
CREATE TABLE ContactCategory3Link (
  contactcategory3link_category_id  integer NOT NULL default 0,
  contactcategory3link_contact_id   integer NOT NULL default 0,
  PRIMARY KEY (contactcategory3link_category_id,contactcategory3link_contact_id)
);
CREATE INDEX contcat3_idx_cont ON ContactCategory3Link (contactcategory3link_contact_id);


--
-- Table structure for table 'ContactCategory4'
--
CREATE TABLE ContactCategory4 (
  contactcategory4_id          serial,
  contactcategory4_timeupdate  timestamp,
  contactcategory4_timecreate  timestamp,
  contactcategory4_userupdate  integer default 0,
  contactcategory4_usercreate  integer default 0,
  contactcategory4_code        varchar(10) default '',
  contactcategory4_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory4_id)
);

--
-- Table structure for table 'ContactCategory4Link'
--
CREATE TABLE ContactCategory4Link (
  contactcategory4link_category_id  integer NOT NULL default 0,
  contactcategory4link_contact_id   integer NOT NULL default 0,
  PRIMARY KEY (contactcategory4link_category_id,contactcategory4link_contact_id)
);
CREATE INDEX contcat4_idx_cont ON ContactCategory4Link (contactcategory4link_contact_id);

