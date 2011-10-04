-- /////////////////////////////////////////////////////////////////////////////
-- // OBM - File : obmdb_ref_2.4.sql                                          //
-- //     - Desc : English Database Referential 2.4                           //
-- // 2007-04-23 AliaSource                                                   //
-- /////////////////////////////////////////////////////////////////////////////
-- $Id$
-- /////////////////////////////////////////////////////////////////////////////


--
-- Dumping data for table 'CompanyType'
--
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Customer');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Supplier');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Partner');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Prospect');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Media');


--
-- Dumping data for table 'CompanyActivity'
--
INSERT INTO CompanyActivity (companyactivity_domain_id, companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Education');
INSERT INTO CompanyActivity (companyactivity_domain_id, companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Industry');


---
-- Dumping data for table 'Kind'
--
INSERT INTO Kind (kind_domain_id, kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'EN','Mr','Mister');
INSERT INTO Kind (kind_domain_id, kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'EN','Mrs','Madam');
INSERT INTO Kind (kind_domain_id, kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'EN','Miss','Miss');


--
-- Dumping data for table 'DealStatus'
--
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'CONTACT',1,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'RDV',2,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'Waiting for Proposal',3,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'PROPOSAL',4,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'SIGNED',5,'100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'DONE',6,'100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'INVOICE',7,'100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'PAYED',8,'100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'LOST',9,'0');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'DISCONTINUED',10,'0');


--
-- Dumping data for table 'DealType'
--
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'SALE','+');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'PURCHASE','-');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'MEDIA','-');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'SOCIAL','-');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'JURIDICAL','-');


--
-- dump for table 'EventCategory1'
--
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'RDV');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Training');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Commercial');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Meeting');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Phone call');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Support');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Intervention');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Personal');


--
-- Dumping data for table 'ContractPriority'
--
INSERT INTO ContractPriority (contractpriority_domain_id, contractpriority_color, contractpriority_code, contractpriority_label) VALUES ((SELECT domain_id From Domain), 'FF0000', '1', 'High');
INSERT INTO ContractPriority (contractpriority_domain_id, contractpriority_color, contractpriority_code, contractpriority_label) VALUES ((SELECT domain_id From Domain), 'FFA0A0', '2', 'Normal');
INSERT INTO ContractPriority (contractpriority_domain_id, contractpriority_color, contractpriority_code, contractpriority_label) VALUES ((SELECT domain_id From Domain), 'FFF0F0', '3', 'Low');


--
-- Dumping data for table 'ContractStatus'
--
INSERT INTO ContractStatus (contractstatus_domain_id, contractstatus_code, contractstatus_label) VALUES ((SELECT domain_id From Domain), '1', 'Open');
INSERT INTO ContractStatus (contractstatus_domain_id, contractstatus_code, contractstatus_label) VALUES ((SELECT domain_id From Domain), '2', 'Close');


--
-- Dumping data for table 'IncidentPriority'
--
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,1,'FF0000','Red Hot');
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,2,'EE9D00','Hot');
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,3,'550000', 'Normal');
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,4,'000000','Low');


--
-- Dumping data for table 'IncidentStatus'
--
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'1','Open');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'2','Call');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'3','Wait for Call');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'4','Paused');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'5','Closed');


--
-- Dumping data for table 'TaskType'
--
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Development',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Support / Assistance',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Learning course',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Studies',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Network / Integration',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Graphics',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Others',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Hosting',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Hardware',0);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Before selling',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Support making',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Internal development',1);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Internal project',1);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Self formation',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Contract garanty',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Miscellaneaous(direction,others)',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Holydays,...',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Outgoings',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Graphics/Communication',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Administrative',2);
INSERT INTO TaskType(tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Meetings',2);


--
-- Dumping data for table 'DocumentMimeType'
--
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Html File','html','text/html');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'PNG Image','png','image/png');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Gif Image','gif','image/gif');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'JPG Image','jpg','image/jpg');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'PDF File','pdf','application/pdf');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Excel File','xls','application/vnd.ms-excel');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Text File','txt','text/plain');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Word File','doc','application/msword');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Binary File','exe','application/octet-stream');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'PowerPoint File','ppt','application/vnd.ms-powerpoint');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'CSV File','csv','text/c-xsv');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'ODT File','odt','application/vnd.oasis.opendocument.text');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'ODS File','ods','application/vnd.oasis.opendocument.spreadsheet');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'ODP File','odp','application/vnd.oasis.opendocument.presentation');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'ODG File','odg','application/vnd.oasis.opendocument.graphics');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'ODF File','odf','application/vnd.oasis.opendocument.formula');


--
-- dump for table  PaymentKind :
--
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain),'Ch', 'Cheque');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'C.T', 'credit transfert');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'TIP', 'Interbank title of Payment');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'PA', 'Standing order(Direct debit)');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'FrB', 'Banking charges');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'BAO', 'Promissory note');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'LC', 'Bill of exchange');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'CC', 'Credit Card');


-- Add Country 
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AD', 'Andorra', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AE', 'United Arab Emirates', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AL', 'Albania', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AM', 'Armenia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AO', 'Angola', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AR', 'Argentina', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AT', 'Austria', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AU', 'Australia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AZ', 'Azerbaijan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BA', 'Bosnia and Herzegovina', 'EN', '+387');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BB', 'Barbados', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BD', 'Bangladesh', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BE', 'Belgium', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BF', 'Burkina Faso', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BG', 'Bulgaria', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BJ', 'Benin', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BO', 'Bolivia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BR', 'Brazil', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BS', 'Bahamas', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BY', 'Belarus', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CA', 'Canada', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CF', 'Central African Republic', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CG', 'Congo', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CH', 'Switzerland', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CI', 'Côte d''Ivoire', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CL', 'Chile', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CM', 'Cameroon', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CN', 'China', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CO', 'Colombia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CR', 'Costa Rica', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CU', 'Cuba', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CY', 'Cyprus', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CZ', 'Czech Republic', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'DE', 'Germany', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'DK', 'Denmark', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'DZ', 'Algeria', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'EC', 'Ecuador', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'EE', 'Estonia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'EG', 'Egypt', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ES', 'Spain', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'FI', 'Finland', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'FR', 'France', 'EN', '+33');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GA', 'Gabon', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GB', 'United Kingdom', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GE', 'Georgia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GH', 'Ghana', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GI', 'Gibraltar', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GL', 'Greenland', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GN', 'Guinea', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GR', 'Greece', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GT', 'Guatemala', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'HK', 'Hong Kong', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'HR', 'Croatia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'HU', 'Hungary', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ID', 'Indonesia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IE', 'Ireland', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IL', 'Israel', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IN', 'India', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IQ', 'Iraq', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IR', 'Iran, Islamic Republic of', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IS', 'Iceland', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IT', 'Italy', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'JM', 'Jamaica', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'JO', 'Jordan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'JP', 'Japan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KE', 'Kenya', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KP', 'Korea, Democratic People''s Republic of', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KR', 'Korea, Republic of', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KW', 'Kuwait', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KZ', 'Kazakhstan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LB', 'Lebanon', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LI', 'Liechtenstein', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LK', 'Sri Lanka', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LT', 'Lithuania', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LU', 'Luxembourg', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LV', 'Latvia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LY', 'Libyan Arab Jamahiriya', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MA', 'Morocco', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MC', 'Monaco', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MD', 'Moldova, Republic of', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MG', 'Madagascar', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MK', 'Macedonia, the former Yugoslav Republic of', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ML', 'Mali', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MT', 'Malta', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MU', 'Mauritius', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MW', 'Malawi', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MX', 'Mexico', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MY', 'Malaysia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NA', 'Namibia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NE', 'Niger', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NI', 'Nicaragua', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NL', 'Netherlands', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NO', 'Norway', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NP', 'Nepal', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NZ', 'New Zealand', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'OM', 'Oman', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PE', 'Peru', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PF', 'French Polynesia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PH', 'Philippines', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PK', 'Pakistan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PL', 'Poland', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PR', 'Puerto Rico', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PT', 'Portugal', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PY', 'Paraguay', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'RO', 'Romania', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'RU', 'Russian Federation', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SA', 'Saudi Arabia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SE', 'Sweden', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SG', 'Singapore', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SI', 'Slovenia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SK', 'Slovakia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SM', 'San Marino', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SN', 'Senegal', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SY', 'Syrian Arab Republic', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TG', 'Togo', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TH', 'Thailand', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TN', 'Tunisia', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TR', 'Turkey', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TT', 'Trinidad and Tobago', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TW', 'Taiwan, Province of China', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'UA', 'Ukraine', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'US', 'United States', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'UY', 'Uruguay', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'VA', 'Holy See (Vatican City State)', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'VE', 'Venezuela, Bolivarian Republic of', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'VN', 'Viet Nam', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ZA', 'South Africa', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ZW', 'Zimbabwe', 'EN', '');

UPDATE Country SET country_name=trim(country_name);
