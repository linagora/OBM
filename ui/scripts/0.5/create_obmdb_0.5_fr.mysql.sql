--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_0.5_fr.mysql.sql                              //
--//     - Desc : French MySQL Database 0.5 creation script                  //
--// 2001-07-27 ALIACOM                                                      //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$ //
--/////////////////////////////////////////////////////////////////////////////

--
-- Dumping data for table 'Company'
--
INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_state,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_country, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MonNumero123',1,'MaSociete',3,'mon adresse l1','mon adresse l2','31520','MaVille','','MyCountry','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);


--
-- Dumping data for table 'CompanyType'
--
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Client');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Fournisseur');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Partenaire');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Prospect');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Media');

--
-- Dumping data for table 'Kind'
--
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'M.','Monsieur');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mme','Madame');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mlle','Mademoiselle');


--
-- Dumping data for table 'Contact'
--
INSERT INTO Contact VALUES (1,'','',NULL,1,1,1,'Admin','admin','','','','','','','','','','','','','',0);


--
-- Dumping data for table 'DealStatus'
--
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'CONTACT',1);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'Attente de Proposition.',2);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'PROPOSITION',3);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'SIGNEE',4);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'PERDUE',5);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'REALISEE',6);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'FACTUREE',7);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'SOLDEE',8);


--
-- Dumping data for table 'DealType'
--
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'VENTE','+');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'ACHAT','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'MEDIA','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SOCIAL','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'JURIDIQUE','-');


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

--
-- dump for table  InvoiceStatus :
--
INSERT INTO InvoiceStatus VALUES(1, 'créée');
INSERT INTO InvoiceStatus VALUES(2, 'payée');
INSERT INTO InvoiceStatus VALUES(3, 'vérifiée');
INSERT INTO InvoiceStatus VALUES(4, 'probleme');
 

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


--
-- Dumping data for table 'TaskType'
--
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (1,'Développement',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (2,'Sav / Maintenance',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (3,'Formation',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (4,'Etudes / Conseil',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (5,'Réseau / Intégration',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (6,'Infographie',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (7,'Hébergement',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (8,'Matériel',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (9,'Autres',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (11,'Avant vente',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (12,'Préparation formation',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (13,'Développements internes',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (14,'Projets internes',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (15,'Auto-Formations,Veille',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (16,'Garantie contractuelle projets',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (17,'Divers(direction,autres)',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (18,'Congés , absences , maladie',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (19,'Déplacements',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (20,'Infographie/Communication',1);
