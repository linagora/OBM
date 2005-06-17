--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : obmdb_ref_1.0.sql                                          //
--//     - Desc : French Database Referential 1.0 insertion script           //
--// 2005-06-08 ALIACOM - PB                                                 //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////

--
-- Dumping data for table 'CompanyType'
--
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Client');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Fournisseur');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Partenaire');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Prospect');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Media');


--
-- Dumping data for table 'CompanyActivity'
--
INSERT INTO CompanyActivity (companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES (null,null,2,null,'Education');
INSERT INTO CompanyActivity (companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES (null,null,2,null,'Industrie');


--
-- Dumping data for table 'Kind'
--
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES (null,null,2,null,'FR','M.','Monsieur');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES (null,null,2,null,'FR','Mme','Madame');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES (null,null,2,null,'FR','Mlle','Mademoiselle');


--
-- Dumping data for table 'DealStatus'
--
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'CONTACT',1,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'RDV',2,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'Attente de Proposition.',3,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'PROPOSITION',4,null);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'SIGNEE',5, '100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'REALISEE',6, '100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'FACTUREE',7, '100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'PAYEE',8, '100');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'PERDUE',9, '0');
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES (null,null,2,null, 'SANS SUITE',10, '0');


--
-- Dumping data for table 'DealType'
--
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'VENTE','+');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'ACHAT','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'MEDIA','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SOCIAL','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'JURIDIQUE','-');


--
-- dump for table 'CalendarCategory'
--
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'RDV');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Formation');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Commercial');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Reunion');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Appel tel.');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Support');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Intervention');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Personnel');


--
-- Dumping data for table 'ContractPriority'
--
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FF0000', 1, 'Haute');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FFA0A0', 2, 'Normale');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_order, contractpriority_label) VALUES ('FFF0F0', 3, 'Faible');


--
-- Dumping data for table 'ContractStatus'
--
INSERT INTO ContractStatus (contractstatus_order, contractstatus_label) VALUES (1, 'En cours');
INSERT INTO ContractStatus (contractstatus_order, contractstatus_label) VALUES (2, 'Clos');


--
-- Dumping data for table 'IncidentPriority'
--
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,1,'FF0000','Urgente');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,2,'EE9D00','Forte');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,3,'550000', 'Normale');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,4,'000000','Basse');


--
-- Dumping data for table 'IncidentStatus'
--
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,1,'Ouvert');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,2,'Appel');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,3,'Attente Appel');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,4,'En Pause');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,5,'Cloturé');


--
-- Dumping data for table 'IncidentCategory1'
--
INSERT INTO IncidentCategory1 (incidentcategory1_order, incidentcategory1_label) VALUES (1, 'A distance');
INSERT INTO IncidentCategory1 (incidentcategory1_order, incidentcategory1_label) VALUES (2, 'Sur site');


--
-- Dumping data for table 'TaskType'
--
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Développement',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Sav / Maintenance',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Formation',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Etudes / Conseil',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Réseau / Intégration',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Infographie',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Hébergement',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Matériel',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Autres',0);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Avant vente',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Préparation formation',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Développements internes',1);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Projets internes',1);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Auto-Formations,Veille',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Garantie contractuelle projets',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Divers(direction,autres)',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Congés , absences , maladie',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Déplacements',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Infographie/Communication',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Administratif',2);
INSERT INTO TaskType (tasktype_label, tasktype_internal) VALUES ('Réunions',2);


--
-- Default Document root
--
INSERT INTO Document (document_title, document_name, document_kind, document_privacy, document_path) VALUES ('Root', 'Default', 0, 0, '/');


--
-- Dumping data for table 'DocumentCategory1'
--
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Divers');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Devis');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Propal');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Documentation');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Contrat');
INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('CR');

--
-- Dumping data for table 'DocumentCategory2'
--
INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Interne');
INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Fournisseur');
INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Client');



--
-- Dumping data for table 'DocumentMimeType'
--
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier Html','html','text/html');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Image PNG','png','image/png');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Image Gif','gif','image/gif');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Image JPG','JPG','image/jpg');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier PDF','pdf','application/pdf');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier Excel','xls','application/vnd.ms-excel');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier Texte','txt','text/plain');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier Word','doc','application/msword');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier Binaire','exe','application/octet-stream');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier PowerPoint','ppt','application/vnd.ms-powerpoint');
INSERT INTO DocumentMimeType (documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ('Fichier CSV','csv','text/x-csv');


--
-- dump for table  InvoiceStatus :
--
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_archive, invoicestatus_label) VALUES ('0', '0', 'A créer');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_archive, invoicestatus_label) VALUES ('1', '0', 'Envoyée');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_archive, invoicestatus_label) VALUES ('1', '0', 'Payée partiellement');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_archive, invoicestatus_label) VALUES ('1', '0', 'Litige');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', 'Payée');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_archive, invoicestatus_label) VALUES ('0', '1', 'Annulée');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_archive, invoicestatus_label) VALUES ('0', '1', 'Pertes et profits');


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


-- Add system Groups
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Admin', 'groupe système Administration', 'admin');
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Commercial', 'groupe système Commercial', '');
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Production', 'groupe système Production', '');
