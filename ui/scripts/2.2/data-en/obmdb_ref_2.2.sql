-- /////////////////////////////////////////////////////////////////////////////
-- // OBM - File : obmdb_ref_2.NULL.sql                                          //
-- //     - Desc : English Database Referential 2.NULL                           //
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


--
-- dump for table  PaymentKind :
--
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain),'Ch', 'Cheque');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'C.T', 'credit transfert');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'TIP', 'Titre Interbancaire de Paiement');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'PA', 'Prélèvement Automatique');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'FrB', 'Frais bancaires');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'BAO', 'Billet à ordre');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'LC', 'Lettre de change');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'CC', 'Credit Card');


-- Add Country 
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AE', 'Emirats Arabes Unis', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AL', 'Albanie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AO', 'Angola', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SA', 'Arabie Saoudite', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AM', 'Arménie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AU', 'Australie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AZ', 'Azerbaidjan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BE', 'Belgique', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BD', 'Bangladesh', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BB', 'La Barbade', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BJ', 'Benin', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BG', 'Bulgarie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BO', 'Bolivie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BR', 'Brésil', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BS', 'Bahamas', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BF', 'Burkina Faso', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BY', 'Bielorussie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CM', 'Cameroun', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CA', 'Canada', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CH', 'Suisse', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CN', 'Chine', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CO', 'Colombie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KP', 'Corée du Nord', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CR', 'Costa Rica', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CZ', 'Rep.Tchèque', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CU', 'Cuba', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CY', 'Chypre', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'DE', 'Allemagne', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'DK', 'Danemark', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'DZ', 'Algérie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ES', 'Espagne', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'EE', 'Estonie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'EC', 'Equateur', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'EG', 'Egypte', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LI', 'Liechtenstein', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GA', 'Gabon', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GB', 'Royaume Uni', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GI', 'Gibraltar', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GT', 'Guatemala', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GE', 'Georgie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GH', 'Ghana', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GL', 'Groenland', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GR', 'Grèce', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'GN', 'Guinée', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'HU', 'Hongrie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'HK', 'Hong Kong', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'JO', 'Jordanie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'HR', 'Croatie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IT', 'Italie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IL', 'Israel', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IN', 'Inde', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IR', 'Iran', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IE', 'Irlande', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IS', 'Islande', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'JP', 'Japon', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'JM', 'Jamaique', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KZ', 'Kazakhstan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KE', 'Kenya', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KW', 'Koweit', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LU', 'Luxembourg', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LY', 'Libye', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LB', 'Liban', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LK', 'Sri Lanka', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LV', 'Lettonie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MT', 'Malte', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MA', 'Maroc', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MY', 'Malaisie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MC', 'Monaco', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MD', 'Moldova', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MX', 'Mexique', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MU', 'Mauritius', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MW', 'Malawi', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NO', 'Norvège', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NP', 'Népal', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NI', 'Nicaragua', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NE', 'Nigeria', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NL', 'Pays Bas', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NZ', 'Nouvelle Zélande', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'OM', 'Oman', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PT', 'Portugal', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PE', 'Pérou', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PH', 'Phillipines', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PK', 'Pakistan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PL', 'Pologne', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PF', 'Polynésie Française', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PR', 'Porto Rico', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'PY', 'Paraguay', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AR', 'Argentine', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TW', 'Taiwan', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CF', 'Rép. Centraficaine', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CL', 'Chili', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CI', 'Rep. Côte D''ivoire', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'KR', 'Corée du Sud', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ID', 'Indonésie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MG', 'Madagascar', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ML', 'Mali', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'RO', 'Roumanie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'UY', 'Uruguay', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'RU', 'Russie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SE', 'Suède', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SM', 'San Marino', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'FI', 'Finlande', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SG', 'Singapour', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SI', 'Slovenie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SK', 'Slovaquie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SN', 'Sénégal', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'NA', 'Namibie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'SY', 'Syrie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TH', 'Thailande', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TG', 'Togo', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TR', 'Turquie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TT', 'Trinité & Tobago', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'TN', 'Tunisie', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'UA', 'Ukraine', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'US', 'USA', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'VA', 'Saint-Siège', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'VN', 'Vietnam', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AT', 'Autriche', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'VE', 'Vénézuela', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ZA', 'Afriq. Sud', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'ZW', 'Zimbabwe', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'BA', 'Bosnie-Herzégovine ', 'EN', '+387');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'AD', 'Andorre', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CS', 'Serbie-Monténégro', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'CG', 'Congo ', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'IQ', 'Irak', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'LT', 'Lituanie ', 'EN', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'FR', 'France ', 'EN', '+33');
INSERT INTO Country VALUES ((SELECT domain_id From Domain), NULL, NULL,NULL, NULL, 'MK', '« L''ex République Yougoslave de Macedoine »', 'EN', '');

UPDATE Country SET country_name=trim(country_name);
