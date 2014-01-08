-- /////////////////////////////////////////////////////////////////////////////
-- // OBM - File : obmdb_ref_2.4.sql                                          //
-- //     - Desc : French Database Referential 2.4                            //
-- // 2007-04-23 AliaSource - PB                                              //
-- /////////////////////////////////////////////////////////////////////////////


--
-- Dumping data for table 'CompanyType'
--
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Client');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Fournisseur');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Partenaire');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Prospect');
INSERT INTO CompanyType (companytype_domain_id, companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Media');


--
-- Dumping data for table 'CompanyActivity'
--
INSERT INTO CompanyActivity (companyactivity_domain_id, companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Education');
INSERT INTO CompanyActivity (companyactivity_domain_id, companyactivity_timeupdate, companyactivity_timecreate, companyactivity_userupdate, companyactivity_usercreate, companyactivity_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Industrie');


--
-- Dumping data for table 'Kind'
--
INSERT INTO Kind (kind_domain_id, kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'FR','M.','Monsieur');
INSERT INTO Kind (kind_domain_id, kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'FR','Mme','Madame');
INSERT INTO Kind (kind_domain_id, kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_lang, kind_minilabel, kind_header) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'FR','Mlle','Mademoiselle');


--
-- Dumping data for table 'DealStatus'
--
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'CONTACT',1,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'RDV',2,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'Attente de Proposition.',3,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'PROPOSITION',4,NULL);
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'SIGNEE',5, '100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'REALISEE',6, '100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'FACTUREE',7, '100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'PAYEE',8, '100');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'PERDUE',9, '0');
INSERT INTO DealStatus (dealstatus_domain_id, dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label, dealstatus_order, dealstatus_hitrate) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL, 'SANS SUITE',10, '0');


--
-- Dumping data for table 'DealType'
--
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'VENTE','+');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'ACHAT','-');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'MEDIA','-');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'SOCIAL','-');
INSERT INTO DealType (dealtype_domain_id, dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'JURIDIQUE','-');


--
-- dump for table 'EventCategory1'
--
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'RDV');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Formation');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Commercial');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Reunion');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Appel tel.');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Support');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Intervention');
INSERT INTO EventCategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,NULL,'Personnel');


--
-- Dumping data for table 'ContractPriority'
--
INSERT INTO ContractPriority (contractpriority_domain_id, contractpriority_color, contractpriority_code, contractpriority_label) VALUES ((SELECT domain_id From Domain), 'FF0000', '1', 'Haute');
INSERT INTO ContractPriority (contractpriority_domain_id, contractpriority_color, contractpriority_code, contractpriority_label) VALUES ((SELECT domain_id From Domain), 'FFA0A0', '2', 'Normale');
INSERT INTO ContractPriority (contractpriority_domain_id, contractpriority_color, contractpriority_code, contractpriority_label) VALUES ((SELECT domain_id From Domain), 'FFF0F0', '3', 'Faible');


--
-- Dumping data for table 'ContractStatus'
--
INSERT INTO ContractStatus (contractstatus_domain_id, contractstatus_code, contractstatus_label) VALUES ((SELECT domain_id From Domain), '1', 'En cours');
INSERT INTO ContractStatus (contractstatus_domain_id, contractstatus_code, contractstatus_label) VALUES ((SELECT domain_id From Domain), '2', 'Clos');


--
-- Dumping data for table 'IncidentPriority'
--
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'1','FF0000','Urgente');
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'2','EE9D00','Forte');
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'3','550000', 'Normale');
INSERT INTO IncidentPriority (incidentpriority_domain_id, incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_code,incidentpriority_color,incidentpriority_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'4','000000','Basse');


--
-- Dumping data for table 'IncidentStatus'
--
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'1','Ouvert');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'2','Appel');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'3','Attente Appel');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'4','En Pause');
INSERT INTO IncidentStatus (incidentstatus_domain_id, incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_code,incidentstatus_label) VALUES ((SELECT domain_id From Domain), NULL,NULL,NULL,1,'5','Cloturé');


--
-- Dumping data for table 'TaskType'
--
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Développement',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Sav / Maintenance',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Formation',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Etudes / Conseil',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Réseau / Intégration',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Infographie',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Hébergement',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Matériel',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Autres',0);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Avant vente',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Préparation formation',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Développements internes',1);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Projets internes',1);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Auto-Formations,Veille',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Garantie contractuelle projets',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Divers(direction,autres)',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Congés , absences , maladie',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Déplacements',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Infographie/Communication',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Administratif',2);
INSERT INTO TaskType (tasktype_domain_id, tasktype_label, tasktype_internal) VALUES ((SELECT domain_id From Domain), 'Réunions',2);


--
-- Dumping data for table 'DocumentMimeType'
--
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label, documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier Html','html','text/html');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime)
VALUES ((SELECT domain_id From Domain), 'Image PNG','png','image/png');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Image Gif','gif','image/gif');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Image JPG','jpg','image/jpg');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime)
VALUES ((SELECT domain_id From Domain), 'Fichier PDF','pdf','application/pdf');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier Excel','xls','application/vnd.ms-excel');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier Texte','txt','text/plain');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier Word','doc','application/msword');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier Binaire','exe','application/octet-stream');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier PowerPoint','ppt','application/vnd.ms-powerpoint');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier CSV','csv','text/x-csv');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier ODT','odt','application/vnd.oasis.opendocument.text');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier ODS','ods','application/vnd.oasis.opendocument.spreadsheet');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier ODP','odp','application/vnd.oasis.opendocument.presentation');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier ODG','odg','application/vnd.oasis.opendocument.graphics');
INSERT INTO DocumentMimeType (documentmimetype_domain_id, documentmimetype_label,documentmimetype_extension,documentmimetype_mime) 
VALUES ((SELECT domain_id From Domain), 'Fichier ODF','odf','application/vnd.oasis.opendocument.formula');




--
-- dump for table  PaymentKind :
--
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'Ch', 'Chèque');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'Vir', 'Virement');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'TIP', 'Titre Interbancaire de Paiement');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'PA', 'Prélèvement Automatique');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'FrB', 'Frais bancaires');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'BAO', 'Billet à ordre');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'LC', 'Lettre de change');
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES ((SELECT domain_id From Domain), 'CB', 'Carte de crédit');


-- Add Country
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AE', 'Emirats Arabes Unis', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AL', 'Albanie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AO', 'Angola', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SA', 'Arabie Saoudite', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AM', 'Arménie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AU', 'Australie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AZ', 'Azerbaidjan', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BE', 'Belgique', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BD', 'Bangladesh', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BB', 'La Barbade', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BJ', 'Benin', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BG', 'Bulgarie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BO', 'Bolivie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BR', 'Brésil', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BS', 'Bahamas', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BF', 'Burkina Faso', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BY', 'Bielorussie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CM', 'Cameroun', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CA', 'Canada', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CH', 'Suisse', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CN', 'Chine', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CO', 'Colombie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'KP', 'Corée du Nord', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CR', 'Costa Rica', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CZ', 'Rep.Tchèque', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CU', 'Cuba', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CY', 'Chypre', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'DE', 'Allemagne', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'DK', 'Danemark', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'DZ', 'Algérie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'ES', 'Espagne', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'EE', 'Estonie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'EC', 'Equateur', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'EG', 'Egypte', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'LI', 'Liechtenstein', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GA', 'Gabon', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GB', 'Royaume Uni', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GI', 'Gibraltar', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GT', 'Guatemala', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GE', 'Georgie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GH', 'Ghana', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GL', 'Groenland', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GR', 'Grèce', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'GN', 'Guinée', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'HU', 'Hongrie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'HK', 'Hong Kong', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'JO', 'Jordanie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'HR', 'Croatie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'IT', 'Italie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'IL', 'Israel', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'IN', 'Inde', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'IR', 'Iran', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'IE', 'Irlande', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'IS', 'Islande', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'JP', 'Japon', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'JM', 'Jamaique', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'KZ', 'Kazakhstan', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'KE', 'Kenya', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'KW', 'Koweit', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'LU', 'Luxembourg', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'LY', 'Libye', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'LB', 'Liban', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'LK', 'Sri Lanka', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'LV', 'Lettonie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MT', 'Malte', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MA', 'Maroc', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MY', 'Malaisie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MC', 'Monaco', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MD', 'Moldova', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MX', 'Mexique', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MU', 'Mauritius', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MW', 'Malawi', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'NO', 'Norvège', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'NP', 'Népal', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'NI', 'Nicaragua', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'NE', 'Nigeria', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'NL', 'Pays Bas', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'NZ', 'Nouvelle Zélande', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'OM', 'Oman', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PT', 'Portugal', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PE', 'Pérou', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PH', 'Phillipines', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PK', 'Pakistan', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PL', 'Pologne', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PF', 'Polynésie Française', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PR', 'Porto Rico', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'PY', 'Paraguay', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AR', 'Argentine', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'TW', 'Taiwan', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CF', 'Rép. Centraficaine', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CL', 'Chili', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CI', 'Rep. Côte D''ivoire', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'KR', 'Corée du Sud', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'ID', 'Indonésie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MG', 'Madagascar', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'ML', 'Mali', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'RO', 'Roumanie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'UY', 'Uruguay', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'RU', 'Russie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SE', 'Suède', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SM', 'San Marino', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'FI', 'Finlande', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SG', 'Singapour', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SI', 'Slovenie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SK', 'Slovaquie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SN', 'Sénégal', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'NA', 'Namibie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'SY', 'Syrie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'TH', 'Thailande', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'TG', 'Togo', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'TR', 'Turquie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'TT', 'Trinité & Tobago', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'TN', 'Tunisie', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'UA', 'Ukraine', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'US', 'USA', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'VA', 'Saint-Siège', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'VN', 'Vietnam', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AT', 'Autriche', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'VE', 'Vénézuela', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'ZA', 'Afriq. Sud', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'ZW', 'Zimbabwe', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'BA', 'Bosnie-Herzégovine ', 'FR', '+387');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'AD', 'Andorre', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CS', 'Serbie-Monténégro', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'CG', 'Congo ', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'LT', 'Lituanie ', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'FR', 'France ', 'FR', '+33');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'FR', 'France ', 'EN', '+33');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'IQ', 'Irak', 'FR', '');
INSERT INTO Country VALUES ((SELECT domain_id From Domain WHERE domain_global=true), NULL, NULL,NULL, NULL, 'MK', '« L''ex République Yougoslave de Macedoine »', 'FR', '');

UPDATE Country SET country_name=trim(country_name);
