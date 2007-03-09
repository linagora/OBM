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
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'RDV');
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'Formation');
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'Commercial');
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'Reunion');
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'Appel tel.');
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'Support');
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'Intervention');
INSERT INTO CalendarCategory1 (calendarcategory1_timeupdate, calendarcategory1_timecreate, calendarcategory1_userupdate, calendarcategory1_usercreate, calendarcategory1_label) VALUES (null,null,null,1,'Personnel');


--
-- Dumping data for table 'ContractPriority'
--
INSERT INTO ContractPriority (contractpriority_color, contractpriority_code, contractpriority_label) VALUES ('FF0000', '1', 'Haute');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_code, contractpriority_label) VALUES ('FFA0A0', '2', 'Normale');
INSERT INTO ContractPriority (contractpriority_color, contractpriority_code, contractpriority_label) VALUES ('FFF0F0', '3', 'Faible');


--
-- Dumping data for table 'ContractStatus'
--
INSERT INTO ContractStatus (contractstatus_code, contractstatus_label) VALUES ('1', 'En cours');
INSERT INTO ContractStatus (contractstatus_code, contractstatus_label) VALUES ('2', 'Clos');


--
-- Dumping data for table 'IncidentPriority'
--
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,'1','FF0000','Urgente');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,'2','EE9D00','Forte');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,'3','550000', 'Normale');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES (null,null,null,1,'4','000000','Basse');


--
-- Dumping data for table 'IncidentStatus'
--
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES (null,null,null,1,'1','Ouvert');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES (null,null,null,1,'2','Appel');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES (null,null,null,1,'3','Attente Appel');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES (null,null,null,1,'4','En Pause');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES (null,null,null,1,'5','Cloturé');


--
-- Dumping data for table 'IncidentCategory1'
--
--INSERT INTO IncidentCategory1 (incidentcategory1_code, incidentcategory1_label) VALUES ('1', 'A distance');
--INSERT INTO IncidentCategory1 (incidentcategory1_code, incidentcategory1_label) VALUES ('2', 'Sur site');


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
-- Dumping data for table 'DocumentCategory1'
--
--INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Divers');
--INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Devis');
--INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Propal');
--INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Documentation');
--INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('Contrat');
--INSERT INTO DocumentCategory1 (documentcategory1_label) VALUES ('CR');

--
-- Dumping data for table 'DocumentCategory2'
--
--INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Interne');
--INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Fournisseur');
--INSERT INTO DocumentCategory2 (documentcategory2_label) VALUES ('Client');



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
VALUES ('Image JPG','jpg','image/jpg');
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
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('0', '0', '0', 'A créer');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '0', 'Envoyée');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '0', 'Payée partiellement');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '0', 'Litige');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '1', 'Payée');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('0', '1', '1', 'Annulée');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('0', '1', '1', 'Pertes et profits');
INSERT INTO InvoiceStatus (invoicestatus_payment, invoicestatus_created, invoicestatus_archive, invoicestatus_label) VALUES ('1', '1', '0', 'Reçue');


--
-- dump for table  PaymentKind :
--
INSERT INTO PaymentKind VALUES (1,'Ch', 0, 'Chèque');
INSERT INTO PaymentKind VALUES (2,'Vir', 0, 'Virement');
INSERT INTO PaymentKind VALUES (3,'TIP', 0, 'Titre Interbancaire de Paiement');
INSERT INTO PaymentKind VALUES (4,'PA', 0, 'Prélèvement Automatique');
INSERT INTO PaymentKind VALUES (5,'FrB', 0, 'Frais bancaires');
INSERT INTO PaymentKind VALUES (6,'BAO', 0, 'Billet à ordre');
INSERT INTO PaymentKind VALUES (7,'LC', 0, 'Lettre de change');


-- Add system Groups
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Admin', 'groupe système Administration', 'admin');
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Commercial', 'groupe système Commercial', '');
INSERT INTO UGroup (group_system, group_privacy, group_name, group_desc, group_email) VALUES
(1, 0, 'Production', 'groupe système Production', '');

-- Add Country 
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AE', 'Emirats Arabes Unis', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AL', 'Albanie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AO', 'Angola', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SA', 'Arabie Saoudite', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AM', 'Arménie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AU', 'Australie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AZ', 'Azerbaidjan', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BE', 'Belgique', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BD', 'Bangladesh', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BB', 'La Barbade', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BJ', 'Benin', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BG', 'Bulgarie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BO', 'Bolivie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BR', 'Brésil', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BS', 'Bahamas', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BF', 'Burkina Faso', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BY', 'Bielorussie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CM', 'Cameroun', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CA', 'Canada', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CH', 'Suisse', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CN', 'Chine', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CO', 'Colombie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'KP', 'Corée du Nord', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CR', 'Costa Rica', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CZ', 'Rep.Tchèque', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CU', 'Cuba', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CY', 'Chypre', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'DE', 'Allemagne', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'DK', 'Danemark', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'DZ', 'Algérie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'ES', 'Espagne', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'EE', 'Estonie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'EC', 'Equateur', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'EG', 'Egypte', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'LI', 'Liechtenstein', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GA', 'Gabon', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GB', 'Royaume Uni', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GI', 'Gibraltar', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GT', 'Guatemala', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GE', 'Georgie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GH', 'Ghana', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GL', 'Groenland', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GR', 'Grèce', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'GN', 'Guinée', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'HU', 'Hongrie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'HK', 'Hong Kong', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'JO', 'Jordanie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'HR', 'Croatie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'IT', 'Italie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'IL', 'Israel', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'IN', 'Inde', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'IR', 'Iran', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'IE', 'Irlande', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'IS', 'Islande', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'JP', 'Japon', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'JM', 'Jamaique', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'KZ', 'Kazakhstan', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'KE', 'Kenya', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'KW', 'Koweit', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'LU', 'Luxembourg', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'LY', 'Libye', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'LB', 'Liban', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'LK', 'Sri Lanka', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'LV', 'Lettonie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MT', 'Malte', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MA', 'Maroc', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MY', 'Malaisie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MC', 'Monaco', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MD', 'Moldova', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MX', 'Mexique', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MU', 'Mauritius', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MW', 'Malawi', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'NO', 'Norvège', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'NP', 'Népal', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'NI', 'Nicaragua', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'NE', 'Nigeria', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'NL', 'Pays Bas', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'NZ', 'Nouvelle Zélande', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'OM', 'Oman', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PT', 'Portugal', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PE', 'Pérou', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PH', 'Phillipines', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PK', 'Pakistan', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PL', 'Pologne', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PF', 'Polynésie Française', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PR', 'Porto Rico', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'PY', 'Paraguay', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AR', 'Argentine', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'TW', 'Taiwan', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CF', 'Rép. Centraficaine', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CL', 'Chili', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CI', 'Rep. Côte D''ivoire', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'KR', 'Corée du Sud', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'ID', 'Indonésie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MG', 'Madagascar', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'ML', 'Mali', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'RO', 'Roumanie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'UY', 'Uruguay', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'RU', 'Russie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SE', 'Suède', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SM', 'San Marino', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'FI', 'Finlande', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SG', 'Singapour', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SI', 'Slovenie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SK', 'Slovaquie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SN', 'Sénégal', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'NA', 'Namibie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'SY', 'Syrie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'TH', 'Thailande', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'TG', 'Togo', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'TR', 'Turquie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'TT', 'Trinité & Tobago', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'TN', 'Tunisie', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'UA', 'Ukraine', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'US', 'USA', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'VA', 'Saint-Siège', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'VN', 'Vietnam', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AT', 'Autriche', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'VE', 'Vénézuela', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'ZA', 'Afriq. Sud', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'ZW', 'Zimbabwe', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'BA', 'Bosnie-Herzégovine ', 'FR', '+387');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'AD', 'Andorre', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CS', 'Serbie-Monténégro', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'CG', 'Congo ', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'LT', 'Lituanie ', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'FR', 'France ', 'FR', '+33');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'FR', 'France ', 'EN', '+33');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'IQ', 'Irak', 'FR', '');
INSERT INTO Country VALUES (NULL, NULL,NULL, 0, 0, 'MK', '« L''ex République Yougoslave de Macedoine »', 'FR', '');


UPDATE Country SET country_timeupdate = country_timeupdate, country_domain_id = 1;
UPDATE CompanyType SET companytype_timeupdate = companytype_timeupdate, companytype_domain_id = 1;
UPDATE CompanyActivity SET companyactivity_timeupdate = companyactivity_timeupdate, companyactivity_domain_id = 1;
UPDATE CompanyNafCode SET companynafcode_timeupdate = companynafcode_timeupdate, companynafcode_domain_id = 1;
UPDATE Kind SET kind_timeupdate = kind_timeupdate, kind_domain_id = 1;
UPDATE DealStatus SET dealstatus_timeupdate = dealstatus_timeupdate, dealstatus_domain_id = 1;
UPDATE DealType SET dealtype_timeupdate = dealtype_timeupdate, dealtype_domain_id = 1;
UPDATE CalendarCategory1 SET calendarcategory1_timeupdate = calendarcategory1_timeupdate, calendarcategory1_domain_id = 1;
UPDATE DocumentMimeType SET documentmimetype_timeupdate = documentmimetype_timeupdate, documentmimetype_domain_id = 1;
UPDATE TaskType SET tasktype_timeupdate = tasktype_timeupdate, tasktype_domain_id = 1;
UPDATE ContractPriority SET contractpriority_timeupdate = contractpriority_timeupdate, contractpriority_domain_id = 1;
UPDATE ContractStatus SET contractstatus_timeupdate = contractstatus_timeupdate, contractstatus_domain_id = 1;
UPDATE IncidentPriority SET incidentpriority_timeupdate = incidentpriority_timeupdate, incidentpriority_domain_id = 1;
UPDATE IncidentStatus SET incidentstatus_timeupdate = incidentstatus_timeupdate, incidentstatus_domain_id = 1;
UPDATE InvoiceStatus SET invoicestatus_domain_id = 1;
UPDATE PaymentKind SET paymentkind_domain_id = 1;
UPDATE UGroup SET group_timeupdate = group_timeupdate, group_domain_id = 1;

