--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_0.5_en.mysql.sql                              //
--//     - Desc : English MySQL Database 0.5 creation script                 //
--// 2001-07-27 ALIACOM                                                      //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$ //
--/////////////////////////////////////////////////////////////////////////////

--
-- Dumping data for table 'Company'
--
INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_state,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_country, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MyRef123',1,'MyCompany',3,'my address l1','my address l2','31520','MyTown','','MyCountry','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);

--
-- Dumping data for table 'CompanyType'
--
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Customer');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Supplier');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Partner');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Prospect');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Media');


---
-- Dumping data for table 'Kind'
--
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mr','Mister');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mrs','Madam');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Miss','Miss');


--
-- Dumping data for table 'Contact'
--
INSERT INTO Contact VALUES (1,'','',NULL,1,1,1,'Admin','admin','','','','','','','','','','','','','',0);


--
-- Dumping data for table 'DealStatus'
--
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'CONTACT',1);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'Waiting for Proposal',2);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'PROPOSAL',3);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'SIGNED',4);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'LOST',5);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'DONE',6);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'INVOICE',7);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'CLOSED',8);

--
-- Dumping data for table 'DealType'
--
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SALE','+');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'PURCHASE','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'MEDIA','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SOCIAL','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'JURIDICAL','-');

--
-- dump for table 'EventCategory'
--
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'RDV');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Training');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Trade');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Meeting');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Call');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Support');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Development');
INSERT INTO EventCategory (eventcategory_timeupdate, eventcategory_timecreate, eventcategory_userupdate, eventcategory_usercreate, eventcategory_label) VALUES (null,null,null,1,'Private');


--
-- dump for table  InvoiceStatus :
--
INSERT INTO InvoiceStatus VALUES(1, 'created');
INSERT INTO InvoiceStatus VALUES(2, 'paid');
INSERT INTO InvoiceStatus VALUES(3, 'checked');
INSERT INTO InvoiceStatus VALUES(4, 'trouble');
 
--
-- dump for table  PaymentKind :
--
INSERT INTO PaymentKind VALUES (1,'Ch','Cheque');
INSERT INTO PaymentKind VALUES (2,'C.T.','credit transfert');
INSERT INTO PaymentKind VALUES (3,'TIP','Titre Interbancaire de Paiement');
INSERT INTO PaymentKind VALUES (4,'PA','Prélèvement Automatique');
INSERT INTO PaymentKind VALUES (5,'FrB','Frais bancaires');
INSERT INTO PaymentKind VALUES (6,'BAO','Billet à ordre');
INSERT INTO PaymentKind VALUES (7,'LC','Lettre de change');


--
-- Dumping data for table 'TaskType'
--
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (1,'Development',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (2,'Support / Assistance',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (3,'Learning course',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (4,'Studies',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (5,'Network / Integration',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (6,'Graphics',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (7,'Others',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (8,'Hosting',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (9,'Hardware',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (11,'Before selling',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (12,'Support making',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (13,'Internal development',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (14,'Internal project',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (15,'Self formation',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (16,'Contract garanty',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (17,'Miscellaneaous(direction,others)',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (18,'Holydays,...',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (19,'Outgoings',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (20,'Graphics/Communication',1);
