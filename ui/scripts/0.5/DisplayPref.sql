#
# Table structure for table 'DisplayPref'
#

CREATE TABLE DisplayPref (
  display_user_id int(8) NOT NULL default '0',
  display_module varchar(32) NOT NULL default '',
  display_fieldname varchar(64) NOT NULL default '',
  display_fieldorder int(3) unsigned default NULL,
  display_display int(1) unsigned NOT NULL default '1'
) TYPE=MyISAM;

