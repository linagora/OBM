---------------------------------------------------------
--
-- Table structure for table 'ContactList'
--
CREATE TABLE ContactList (
   ContactList_listid int8 DEFAULT '0' NOT NULL,
   ContactList_contactid int8 DEFAULT '0' NOT NULL
);



----------------------------------------------------------
--
-- Table structure for table 'List'
--

CREATE TABLE List (
   list_id SERIAL,
   list_timeupdate datetime,
   list_timecreate datetime,
   list_userupdate int8,
   list_usercreate int8,
   list_name varchar(32) NOT NULL,
   list_subject varchar(70),
   list_auth_usermail int2 DEFAULT '0' NOT NULL,
   PRIMARY KEY (list_id),
   UNIQUE (list_name)
);


----------------------------------------------------------
--
-- Table structure for table 'ListDisplay'
--

CREATE TABLE ListDisplay (
   display_user_id int8 DEFAULT '0' NOT NULL,
   display_fieldname varchar(20),
   display_fieldorder int2,
   display_display int2 DEFAULT '1' NOT NULL
);
