-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 1.0 to 1.1                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.1' where obminfo_name='db_version';

-------------------------------------------------------------------------------
-- Update Contact table
-------------------------------------------------------------------------------
-- Add columns 
ALTER TABLE Contact ADD COLUMN contact_comment2 text NULL after contact_comment;
ALTER TABLE Contact ADD COLUMN contact_comment3 text NULL after contact_comment2;
ALTER TABLE Contact ADD COLUMN contact_date timestamp(14) after contact_privacy;


-------------------------------------------------------------------------------
-- Tables needed for Contact module
-------------------------------------------------------------------------------

--
-- Table structure for table 'ContactCategory3'
--
CREATE TABLE ContactCategory3 (
  contactcategory3_id          int(8) auto_increment,
  contactcategory3_timeupdate  timestamp(14),
  contactcategory3_timecreate  timestamp(14),
  contactcategory3_userupdate  int(8) default 0,
  contactcategory3_usercreate  int(8) default 0,
  contactcategory3_code        varchar(10) default '',
  contactcategory3_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory3_id)
);

--
-- Table structure for table 'ContactCategory3Link'
--
CREATE TABLE ContactCategory3Link (
  contactcategory3link_category_id  int(8) NOT NULL default 0,
  contactcategory3link_contact_id   int(8) NOT NULL default 0,
  PRIMARY KEY (contactcategory3link_category_id,contactcategory3link_contact_id),
  INDEX contcat3_idx_cont (contactcategory3link_contact_id)
);

--
-- Table structure for table 'ContactCategory4'
--
CREATE TABLE ContactCategory4 (
  contactcategory4_id          int(8) auto_increment,
  contactcategory4_timeupdate  timestamp(14),
  contactcategory4_timecreate  timestamp(14),
  contactcategory4_userupdate  int(8) default 0,
  contactcategory4_usercreate  int(8) default 0,
  contactcategory4_code        varchar(10) default '',
  contactcategory4_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory4_id)
);


--
-- Table structure for table 'ContactCategory4Link'
--
CREATE TABLE ContactCategory4Link (
  contactcategory4link_category_id  int(8) NOT NULL default 0,
  contactcategory4link_contact_id   int(8) NOT NULL default 0,
  PRIMARY KEY (contactcategory4link_category_id,contactcategory4link_contact_id),
  INDEX contcat4_idx_cont (contactcategory4link_contact_id)
);

-------------------------------------------------------------------------------
-- Create table structure for table 'CompanyCategory1'
-------------------------------------------------------------------------------
-- Create table 'CompanyCategory1'
CREATE TABLE CompanyCategory1 (
  companycategory1_id          int(8) auto_increment,
  companycategory1_timeupdate  timestamp(14),
  companycategory1_timecreate  timestamp(14),
  companycategory1_userupdate  int(8) NOT NULL default 0,
  companycategory1_usercreate  int(8) NOT NULL default 0,
  companycategory1_code        varchar(10) NOT NULL default '',
  companycategory1_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (companycategory1_id)
);

-- Update table 'CompanyCategory1'
INSERT INTO CompanyCategory1 (
  companycategory1_id,
  companycategory1_timeupdate,
  companycategory1_timecreate,
  companycategory1_userupdate,
  companycategory1_usercreate,
  companycategory1_code,
  companycategory1_label,
)
SELECT
  companycategory1_id,
  companycategory1_timeupdate,
  companycategory1_timecreate,
  companycategory1_userupdate,
  companycategory1_usercreate,
  companycategory1_code,
  companycategory1_label

-- DROP table CompanyCategory1
DROP TABLE IF EXISTS CompanyCategory1;



--
-- Table structure for table 'CompanyCategory1Link'
--
CREATE TABLE CompanyCategory1Link (
  companycategory1link_category_id  int(8) NOT NULL default 0,
  companycategory1link_company_id   int(8) NOT NULL default 0,
  PRIMARY KEY (companycategory1link_category_id,companycategory1link_company_id),
  INDEX compcat1_idx_comp (companycategory1link_company_id)
);


