-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.4.x to 0.5	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////


-- Remove column company_mailing from table Company
ALTER table Company drop column company_mailing;

-- UserObmPref changes
-- 
-- update col name
alter table UserObmPref change userobmpref_choice userobmpref_value varchar(50);
alter table UserObmPref change userobmpref_id userobmpref_user_id int(8);
-- Update users preferences : replace removed theme alia_new
UPDATE UserObmPref set userobmpref_value='aliacom' where userobmpref_option='set_theme' and userobmpref_value!='aliacom' and userobmpref_value!='linux-mandrake' and userobmpref_value!='lmm2' and userobmpref_value!='standard';

------------------------------------------------------
--
-- New data structure for the table 'List'
--

ALTER TABLE List ADD list_email varchar(128);
ALTER TABLE List DROP list_auth_usermail;


------------------------------------------------------
--
-- New table 'EventUser'
--
CREATE TABLE EventUser (
       eventuser_event_id	int(8) NOT NULL,
       eventuser_contact_id	int(8),
       eventuser_group_id	int(8),
       eventuser_state		char(1)
);



------------------------------------------------------
--
-- New table 'CalendarLayer'
--
CREATE TABLE CalendarLayer (
       calendarlayer_contact_id	int(8),
       calendarlayer_group_id	int(8),
       calendarlayer_color	char(7)
);



------------------------------------------------------
--
-- New table 'CalendarEvent'
--
CREATE TABLE CalendarEvent (
       calendarevent_id			int(8) NOT NULL auto_increment,
       calendarevent_timeupdate		timestamp(14),
       calendarevent_timecreate		timestamp(14),
       calendarevent_userupdate		int(8),
       calendarevent_usercreate		int(8),
       calendarevent_origin_id		int(8),
       calendarevent_title		varchar(255),
       calendarevent_description	text,
       calendarevent_category_id	int(8),
       calendarevent_priority		int(2),
       calendarevent_privacy		int(2),
       calendarevent_datebegin		timestamp(14),
       calendarevent_dateend		timestamp(14),
       calendarevent_occupied_day	int(2),
       calendarevent_repeatkind		varchar(20),
       calendarevent_repeat_interval	int(2),
       calendarevent_repeatdays		char(7),
       calendarevent_endrepeat		timestamp(14),
       PRIMARY KEY(calendarevent_id)
);



------------------------------------------------------
--
-- New table 'EventCategory'
--
CREATE TABLE EventCategory (
       eventcategory_id			int(8) NOT NULL auto_increment,
       eventcategory_timeupdate		timestamp(14),
       eventcategory_timecreate		timestamp(14),
       eventcategory_userupdate		int(8),
       eventcategory_usercreate		int(8),
       eventcategory_label		varchar(128),
       PRIMARY KEY(eventcategory_id)    
);


------------------------------------------------------
---- NOT USED ANYMORE
-- New table 'RepeatKind'
--
--CREATE TABLE RepeatKind (
--       repeatkind_id			int(8) NOT NULL auto_increment,
--       repeatkind_timeupdate		timestamp(14),
--       repeatkind_timecreate		timestamp(14),
--       repeatkind_userupdate		int(8),
--       repeatkind_usercreate		int(8),
--       repeatkind_label			varchar(128),
--       PRIMARY KEY(repeatkind_id)	
--);


-------------------------------------------------------------------------------
--
-- dump for table 'EventCategory'
--
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'RDV');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Formation');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Commercial');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Reunion');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Appel tel.');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Support');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Developpement');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Personnel');


-------------------------------------------------------------------------------
--
-- New table 'DisplayPref'
--
CREATE TABLE DisplayPref (
  display_user_id int(8) NOT NULL default '0',
  display_entity varchar(32) NOT NULL default '',
  display_fieldname varchar(64) NOT NULL default '',
  display_fieldorder int(3) unsigned default NULL,
  display_display int(1) unsigned NOT NULL default '1',
  PRIMARY KEY(display_user_id, display_entity, display_fieldname),
  INDEX idx_user (display_user_id),
  INDEX idx_entity (display_entity)
) TYPE=MyISAM;

-------------------------------------------------------------------------------
--
-- New table 'Invoice'
--
CREATE TABLE Invoice ( 
	invoice_id		int(8) NOT NULL auto_increment,
	invoice_number		varchar(10) DEFAULT '0',
	invoice_label		varchar(40) NOT NULL DEFAULT '',
	invoice_amount_HT	double(10,2),
	invoice_amount_TTC	double(10,2),
	invoice_invoicestatus_id	int(4) DEFAULT '0' NOT NULL,
	invoice_usercreate	int(8),
	invoice_userupdate	int(8),
	invoice_timeupdate	timestamp(14),
	invoice_timecreate	timestamp(14),
	invoice_comment		text,
	invoice_date		date not NULL DEFAULT '0000-00-00' ,
	invoice_inout		char(1),
	invoice_archive		char(1) NOT NULL DEFAULT '0',
-- invoice_archive == 1 means invoice is filed and not to
-- be displayed (unless user asks to)
	PRIMARY KEY(invoice_id)
);


-------------------------------------------------------------------------------
--
-- New table 'InvoiceStatus'
--
CREATE TABLE InvoiceStatus (
	invoicestatus_id	int (8) NOT NULL auto_increment,
	invoicestatus_label	varchar(10) default '' NOT NULL,
	PRIMARY KEY(invoicestatus_id)
);

-------------------------------------------------------------------------------
--
-- dump for table  InvoiceStatus :
--
INSERT INTO InvoiceStatus VALUES(1, 'created');
INSERT INTO InvoiceStatus VALUES(2, 'paid');
INSERT INTO InvoiceStatus VALUES(3, 'checked');
INSERT INTO InvoiceStatus VALUES(4, 'trouble');

-------------------------------------------------------------------------------
--
-- New table 'DealInvoice'
--
CREATE TABLE DealInvoice (
	dealinvoice_deal_id 	int(8) NOT NULL,
	dealinvoice_invoice_id	int(8) NOT NULL,
	dealinvoice_timeupdate	timestamp(14),
	dealinvoice_timecreate	timestamp(14),
	dealinvoice_usercreate	int(8),
	dealinvoice_userupdate 	int(8),
	PRIMARY KEY(dealinvoice_deal_id, dealinvoice_invoice_id)
);

-------------------------------------------------------------------------------
--
-- New table 'Payment'
--
CREATE TABLE  Payment (
	payment_id			int(8) NOT NULL	auto_increment,
	payment_timeupdate		timestamp(14),
	payment_timecreate		timestamp(14),
	payment_usercreate		int(8),
	payment_userupdate 		int(8),
	payment_number			int(10) default null,
	payment_date			date,
	payment_expected_date 		date,		
	payment_amount			double(10,2) DEFAULT '0.0' NOT NULL,
	payment_label			varchar(40) NOT NULL DEFAULT '',
	payment_paymentkind_id 		int(8),
	payment_account_id		int(8),
	payment_comment			text,
	payment_inout			char(1) NOT NULL,
	payment_paid			char(1) NOT NULL DEFAULT '0',
	payment_checked			char(1) NOT NULL DEFAULT '0',
	PRIMARY KEY(payment_id)
);

-------------------------------------------------------------------------------
--
-- New table 'PaymentKind'
--
CREATE TABLE PaymentKind (
	paymentkind_id	 	int(8) NOT NULL auto_increment,
	paymentkind_shortlabel	varchar(3) NOT NULL DEFAULT '',
	paymentkind_longlabel	varchar(40) NOT NULL DEFAULT '',
	PRIMARY KEY(paymentkind_id)
);


-------------------------------------------------------------------------------
--
-- dump for table  PaymentKind :
--
INSERT INTO PaymentKind VALUES (1,'Ch','Chèque');
INSERT INTO PaymentKind VALUES (2,'Vir','Virement');
INSERT INTO PaymentKind VALUES (3,'TIP','Titre Interbancaire de Paiement');
INSERT INTO PaymentKind VALUES (4,'PA','Prélèvement Automatique');
INSERT INTO PaymentKind VALUES (5,'FrB','Frais bancaires');
INSERT INTO PaymentKind VALUES (6,'BAO','Billet à ordre');
INSERT INTO PaymentKind VALUES (7,'LC','Lettre de change');

-------------------------------------------------------------------------------
--
-- New table 'PaymentInvoice'
--
CREATE TABLE PaymentInvoice (
	paymentinvoice_invoice_id 	int(8) NOT NULL,
	paymentinvoice_payment_id	int(8) NOT NULL,
	paymentinvoice_amount		double (10,2) NOT NULL DEFAULT '0',
	paymentinvoice_timeupdate	timestamp(14),
	paymentinvoice_timecreate	timestamp(14),
	paymentinvoice_usercreate	int(8),
	paymentinvoice_userupdate 	int(8),
	PRIMARY KEY(paymentinvoice_invoice_id,paymentinvoice_payment_id)
);

-------------------------------------------------------------------------------
--
-- New table 'Account'
--
CREATE TABLE Account (
  account_id	     int(8) DEFAULT '0' NOT NULL auto_increment,
  account_bank	     varchar(60) DEFAULT '' NOT NULL,
  account_number     varchar(11) DEFAULT '0' NOT NULL,
  account_balance    double(15,2) DEFAULT '0.00' NOT NULL,
  account_today	     double(15,2) DEFAULT '0.00' NOT NULL,
  account_comment    varchar(100),
  account_label	     varchar(40) NOT NULL DEFAULT '',
  account_timeupdate timestamp(14),
  account_timecreate timestamp(14),
  account_usercreate int(8),
  account_userupdate int(8),
  PRIMARY KEY (account_id)
);


--
-- EntryTemp and PaymentTemp are used when importing data from the bank files
--

-------------------------------------------------------------------------------
--
-- New table 'PaymentTemp'
--
CREATE TABLE  PaymentTemp (
	paymenttemp_id			int(8) NOT NULL	auto_increment,
	paymenttemp_timeupdate		timestamp(14),
	paymenttemp_timecreate		timestamp(14),
	paymenttemp_usercreate		int(8),
	paymenttemp_userupdate 		int(8),
	paymenttemp_number		int(10) default null,
	paymenttemp_date		date,
	paymenttemp_expected_date	date,		
	paymenttemp_amount		double(10,2) DEFAULT '0.0' NOT NULL,
	paymenttemp_label		varchar(40) NOT NULL DEFAULT '',
	paymenttemp_paymentkind_id 	int(8),
	paymenttemp_account_id		int(8),
	paymenttemp_comment		text,
	paymenttemp_inout		char(1) NOT NULL,
	paymenttemp_paid		char(1) NOT NULL DEFAULT '0',
	paymenttemp_checked		char(1) NOT NULL DEFAULT '0',
	PRIMARY KEY(paymenttemp_id)
);


-------------------------------------------------------------------------------
--
-- New table 'EntryTemp'
--
CREATE TABLE EntryTemp (
	entrytemp_id		int(8) not null default '0' auto_increment,
	entrytemp_label		varchar(40),
  	entrytemp_amount	double(10,2) not null default '0.00',
	entrytemp_type		varchar(100),
	entrytemp_date		date not null default '0000-00-00',
	entrytemp_realdate	date not null default '0000-00-00',
	entrytemp_comment	varchar(100),
	entrytemp_checked	char(1) not null default '0',
	PRIMARY	KEY (entrytemp_id)
);
