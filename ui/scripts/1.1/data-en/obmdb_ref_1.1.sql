--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : obmdb_ref_1.0.sql                                          //
--//     - Desc : English Database Referential 1.0 insertion script          //
--// 2005-06-08 ALIACOM                                                      //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////

--
-- Dumping data for table 'CompanyType'
--
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Customer');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Supplier');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Partner');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Prospect');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Media');


--
-- Dumping data for table 'CompanyActivity'
--
INSERT INTO CompanyActivity (companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES (null,null,2,null,'Education');
INSERT INTO CompanyActivity (companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES (null,null,2,null,'Industry');


---
-- Dumping data for table 'Kind'
--
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES (null,null,2,null,'EN','Mr','Mister');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES (null,null,2,null,'EN','Mrs','Madam');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES (null,null,2,null,'EN','Miss','Miss');


--
-- Dumping data for table 'DealStatus'
--
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'CONTACT',1,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'RDV',2,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'Waiting for Proposal',3,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'PROPOSAL',4,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'SIGNED',5,'100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null,'DONE',6,'100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null,'INVOICE',7,'100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null,'PAYED',8,'100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'LOST',9,'0');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'DISCONTINUED',10,'0');


--
-- Dumping data for table 'DealType'
--
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SALE','+');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'PURCHASE','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'MEDIA','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SOCIAL','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'JURIDICAL','-');


--
-- dump for table 'CalendarCategory'
--
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'RDV');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Training');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Commercial');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Meeting');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Phone call');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Support');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Intervention');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Personal');


--
-- Dumping data for table 'ContractPriority'
--
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FF0000', 1, 'High');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FFA0A0', 2, 'Normal');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FFF0F0', 3, 'Low');


--
-- Dumping data for table 'ContractStatus'
--
INSERT INTO ContractStatus (contractstatus_order, contractstatus_label) VALUES (1, 'Open');
INSERT INTO ContractStatus (contractstatus_order, contractstatus_label) VALUES (2, 'Close');


--
-- Dumping data for table 'IncidentPriority'
--
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,1,'FF0000','Red Hot');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,2,'EE9D00','Hot');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,3,'550000', 'Normal');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,4,'000000','Low');


--
-- Dumping data for table 'IncidentStatus'
--
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,1,'Open');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,2,'Call');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,3,'Wait for Call');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,4,'Paused');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,5,'Closed');


--
-- Dumping data for table 'IncidentCategory1'
--
INSERT INTO IncidentCategory1 (incidentcategory1_order, incidentcategory1_label) VALUES (1, 'by email / phone');
INSERT INTO IncidentCategory1 (incidentcategory1_order, incidentcategory1_label) VALUES (2, 'on site');


--
-- Dumping data for table 'TaskType'
--
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Development',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Support / Assistance',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Learning course',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Studies',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Network / Integration',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Graphics',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Others',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Hosting',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Hardware',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Before selling',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Support making',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Internal development',1);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Internal project',1);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Self formation',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Contract garanty',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Miscellaneaous(direction,others)',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Holydays,...',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Outgoings',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Graphics/Communication',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Administrative',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Meetings',2);


--
-- Dumping data for table 'DocumentCategory1'
--
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Others');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Quotation');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Propal');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Documentation');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Contract');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('CR');


--
-- Dumping data for table 'DocumentCategory2'
--
INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Internal');
INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Tradesman');
INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Customers');


--
-- Dumping data for table 'DocumentMimeType'
--
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Html File','html','text/html');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('PNG Image','png','image/png');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Gif Image','gif','image/gif');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('JPG Image','jpg','image/jpg');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('PDF File','pdf','application/pdf');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Excel File','xls','application/vnd.ms-excel');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Text File','txt','text/plain');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Word File','doc','application/msword');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Binary File','exe','application/octet-stream');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('PowerPoint File','ppt','application/vnd.ms-powerpoint');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('CSV File','csv','text/c-xsv');


--
-- dump for table  InvoiceStatus :
--
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('0', '0', '0', 'To create');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '0', 'Sent');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '0', 'Partially paid');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '0', 'Conflict');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '1', 'Paid');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('0', '1', '1', 'Cancelled');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('0', '1', '1', 'Loss');

 
--
-- dump for table  PaymentKind :
--
INSERT INTO PaymentKind VALUES (1,'Ch','Cheque');
INSERT INTO PaymentKind VALUES (2,'C.T','credit transfert');
INSERT INTO PaymentKind VALUES (3,'TIP','Titre Interbancaire de Paiement');
INSERT INTO PaymentKind VALUES (4,'PA','Prélèvement Automatique');
INSERT INTO PaymentKind VALUES (5,'FrB','Frais bancaires');
INSERT INTO PaymentKind VALUES (6,'BAO','Billet à ordre');
INSERT INTO PaymentKind VALUES (7,'LC','Lettre de change');


-- Add system Groups
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Admin', 'Administration system group', 'admin');
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Commercial', 'Commercial system group', '');
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Production', 'Production system group', '');
