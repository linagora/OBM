#
# Table structure for table 'List'
#

CREATE TABLE List (
   list_id int(8) NOT NULL auto_increment,
   list_timeupdate timestamp(14),
   list_timecreate timestamp(14),
   list_userupdate int(8),
   list_usercreate int(8),
   list_name varchar(32) NOT NULL,
   list_subject varchar(70),
   list_auth_usermail tinyint(2) DEFAULT '0' NOT NULL,
   PRIMARY KEY (list_id),
   UNIQUE list_name (list_name)
);

#
# Table structure for table 'ListDisplay'
#

CREATE TABLE ListDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(20),
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);


#
# Table structure for table 'ContactList'
#

CREATE TABLE ContactList (
   ContactList_listid int(8) DEFAULT '0' NOT NULL,
   ContactList_contactid int(8) DEFAULT '0' NOT NULL
);
