--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_0.5_fr.mysql.sql                              //
--//     - Desc : French MySQL Database 0.5 creation script                  //
--// 2001-07-27 ALIACOM                                                      //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$ //
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
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'M.','Monsieur');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mme','Madame');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mlle','Mademoiselle');


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
-- dump for table  InvoiceStatus :
--
INSERT INTO InvoiceStatus VALUES(1, 'créée');
INSERT INTO InvoiceStatus VALUES(2, 'payée');
INSERT INTO InvoiceStatus VALUES(3, 'vérifiée');
INSERT INTO InvoiceStatus VALUES(4, 'problème');
 

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
-- Dumping data for table 'IncidentPriority'
--
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,1,'Urgente');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,2,'Forte');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,3,'Normale');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,4,'Basse');


--
-- Dumping data for table 'IncidentStatus'
--
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,1,'Ouvert');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,2,'Appel');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,3,'Attente Appel');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,4,'En Pause');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,5,'Cloturé');


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
