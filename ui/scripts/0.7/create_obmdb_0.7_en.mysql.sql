--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_0.7_en.mysql.sql                              //
--//     - Desc : English MySQL Database 0.7 creation script                 //
--// 2003-07-22 ALIACOM                                                      //
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
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mr','Mister');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mrs','Madam');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Miss','Miss');


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
-- dump for table 'CalendarEvent'
--
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'RDV');
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'Training');
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'Trade');
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'Meeting');
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'Call');
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'Support');
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'Development');
INSERT INTO CalendarEvent (calendarevent_timeupdate, calendarevent_timecreate, calendarevent_userupdate, calendarevent_usercreate, calendarevent_label) VALUES (null,null,null,1,'Private');


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
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (11,'Before selling',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (12,'Support making',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (13,'Internal development',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (14,'Internal project',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (15,'Self formation',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (16,'Contract garanty',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (17,'Miscellaneaous(direction,others)',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (18,'Holydays,...',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (19,'Outgoings',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (20,'Graphics/Communication',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (21,'Administrative',2);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (22,'Meetings',2);


--
-- Default Document root
--
INSERT INTO Document (document_title, document_name, document_kind, document_private, document_path) VALUES ('Root', 'Default', 0, 0, '/');


--
-- Dumping data for table 'DocumentCategory1'
--
INSERT INTO DocumentCategory1 (documentcategory1_id,documentcategory1_label) VALUES (1,'Others');
INSERT INTO DocumentCategory1 (documentcategory1_id,documentcategory1_label) VALUES (2,'Quotation');
INSERT INTO DocumentCategory1 (documentcategory1_id,documentcategory1_label) VALUES (3,'Propal');
INSERT INTO DocumentCategory1 (documentcategory1_id,documentcategory1_label) VALUES (4,'Documentation');
INSERT INTO DocumentCategory1 (documentcategory1_id,documentcategory1_label) VALUES (5,'Contract');
INSERT INTO DocumentCategory1 (documentcategory1_id,documentcategory1_label) VALUES (6,'CR');

--
-- Dumping data for table 'DocumentCategory2'
--
INSERT INTO DocumentCategory2 (documentcategory2_id,documentcategory2_label) VALUES (1,'Internal');
INSERT INTO DocumentCategory2 (documentcategory2_id,documentcategory2_label) VALUES (2,'Tradesman');
INSERT INTO DocumentCategory2 (documentcategory2_id,documentcategory2_label) VALUES (3,'Customers');



--
-- Dumping data for table 'DocumentMimeType'
--
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (1,'Html File','html','text/html');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (2,'PNG Image','png','image/png');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (3,'Gif Image','gif','image/gif');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (4,'JPG Image','JPG','image/jpg');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (5,'PDF File','pdf','application/pdf');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (6,'Excel File','xls','application/vnd.ms-excel');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (7,'Text File','txt','text/plain');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (8,'Word File','doc','application/msword');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (9,'Binary File','exe','application/octet-stream');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (10,'PowerPoint File','ppt','application/vnd.ms-powerpoint');
INSERT INTO DocumentMimeType (documentmimetype_id,documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES (11,'CSV File','csv','text/c-xsv');
