
#-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.5.2 to 0.5.3	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////



# Table structure for table `GlobalPref`
#

CREATE TABLE GlobalPref (
  globalpref_option varchar(255) NOT NULL default '',
  globalpref_value varchar(255) NOT NULL default '',
  PRIMARY KEY  (globalpref_option),
  UNIQUE KEY globalpref_option (globalpref_option)
) TYPE=MyISAM;


# Dumping data for table `GlobalPref`
#

INSERT INTO GlobalPref VALUES ('lifetime', '14400');
    


