-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : obmdb_test_values_2.4.sql                                    //
--     - Desc : Insertion of Test values (database independant)              //
-- 2005-06-08 Pierre Baudracco                                               //
-- /////////////////////////////////////////////////////////////////////////////
-- $Id$
-- /////////////////////////////////////////////////////////////////////////////


-- -----------------------------------------------------------------------------
-- Test : Domain
-- -----------------------------------------------------------------------------
--
-- Create first user domain
--
INSERT INTO Domain (
  domain_timecreate,
  domain_usercreate,
  domain_label,
  domain_description,
  domain_name)
VALUES (
  '2007-04-23 11:45:59',
  0,
  'Domain 1',
  '',
  'aliasource.fr');

INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO DomainEntity (domainentity_entity_id, domainentity_domain_id) SELECT MAX(entity_id), MAX(domain_id) FROM Domain, Entity;

-- Fill the initial update_state for each Domain
INSERT INTO DomainPropertyValue (domainpropertyvalue_domain_id, domainpropertyvalue_property_key, domainpropertyvalue_value) SELECT domain_id, 'update_state', 1 FROM Domain;


-- -----------------------------------------------------------------------------
-- Test : User
-- -----------------------------------------------------------------------------
-- USER domain 1
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid) VALUES (1, 'user1','user','PLAIN','user', 'Zizou', 'John', '1001', '513');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT MAX(entity_id), MAX(userobm_id) FROM UserObm, Entity;

-- EDITOR domain 1
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid) VALUES (1, 'editor1','editor','PLAIN','editor', 'Itor', 'Ed', '1002', '513');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT MAX(entity_id), MAX(userobm_id) FROM UserObm, Entity;

-- ADMIN domain 1
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid) VALUES (1, 'admin1','admin','PLAIN','admin', 'Chief', 'James', '1003', '513');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT MAX(entity_id), MAX(userobm_id) FROM UserObm, Entity;

-- -----------------------------------------------------------------------------
-- Test Company
-- -----------------------------------------------------------------------------
DELETE FROM Company;

INSERT INTO Company (company_domain_id, company_timecreate, company_usercreate, company_number,company_archive,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_phone, company_fax, company_web, company_email) VALUES (1, null,2,'MonNumero123',0,'MaSociete',3,'mon adresse l1','mon adresse l2','31520','MaVille','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO CompanyEntity (companyentity_entity_id, companyentity_company_id) SELECT MAX(entity_id), MAX(company_id) FROM Company, Entity;

INSERT INTO Company (company_domain_id, company_timecreate, company_usercreate, company_number,company_archive,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_phone, company_fax, company_web, company_email) VALUES (1,null,2,'MyRef123',0,'MyCompany',3,'my address l1','my address l2','31520','MyTown','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO CompanyEntity (companyentity_entity_id, companyentity_company_id) SELECT MAX(entity_id), MAX(company_id) FROM Company, Entity;


-- -----------------------------------------------------------------------------
-- Test : Contact
-- -----------------------------------------------------------------------------
DELETE FROM Contact;

INSERT INTO Contact (contact_domain_id, contact_company_id, contact_kind_id, contact_lastname, contact_firstname, contact_address1,contact_address2, contact_zipcode, contact_town, contact_title, contact_phone, contact_email, contact_archive,contact_comment) VALUES (1,1,1,'Rabbit','Roger','ad1','ad2','31520','Ramonville','Manager','01 01 01 02 03','roger@rabbit.com','0','comment');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO ContactEntity (contactentity_entity_id, contactentity_contact_id) SELECT MAX(entity_id), MAX(contact_id) FROM Contact, Entity;


-- -----------------------------------------------------------------------------
-- Test : Host
-- -----------------------------------------------------------------------------

DELETE FROM Host;

INSERT INTO Host (host_uid, host_gid, host_name, host_ip, host_description) VALUES ('1500', '1000', 'srv-mail', '10.0.0.11', 'Serveur de courrier');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO HostEntity (hostentity_entity_id, hostentity_host_id) SELECT MAX(entity_id), MAX(host_id) FROM Host, Entity;

INSERT INTO Host (host_uid, host_gid, host_name, host_ip, host_description) VALUES ('1501', '1000', 'smtp-in', '10.0.0.10', 'SMTP entrant');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO HostEntity (hostentity_entity_id, hostentity_host_id) SELECT MAX(entity_id), MAX(host_id) FROM Host, Entity;

INSERT INTO Host (host_uid, host_gid, host_name, host_ip, host_description) VALUES ('1502', '1000', 'smtp-out', '10.0.0.9', 'SMTP sortant');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO HostEntity (hostentity_entity_id, hostentity_host_id) SELECT MAX(entity_id), MAX(host_id) FROM Host, Entity;


-- -----------------------------------------------------------------------------
-- Remplissage de la table 'MailServer' : déclaration des serveurs de BALs
-- -----------------------------------------------------------------------------
DELETE FROM MailServer;

-- Déclaration d'un serveur de BAL sans hôte relais (relayhost)
INSERT INTO MailServer (mailserver_host_id, mailserver_imap, mailserver_smtp_in, mailserver_smtp_out) VALUES ( (SELECT host_id FROM Host WHERE host_name='srv-mail'), 1, 0, 0 );
-- Déclaration d'un serveur SMTP entrant sans hôte relais (relayhost)
INSERT INTO MailServer (mailserver_host_id, mailserver_imap, mailserver_smtp_in, mailserver_smtp_out) VALUES ( (SELECT host_id FROM Host WHERE host_name='smtp-in'), 0, 1, 0 );
-- Déclaration d'un serveur SMTP sortant sans hôte relais (relayhost)
INSERT INTO MailServer (mailserver_host_id, mailserver_imap, mailserver_smtp_in, mailserver_smtp_out) VALUES ( (SELECT host_id FROM Host WHERE host_name='smtp-out'), 0, 0, 1 );


-- -----------------------------------------------------------------------------
-- Remplissage de la table 'DomainMailServer' : déclaration des serveurs de BALs
-- -----------------------------------------------------------------------------
DELETE FROM DomainMailServer;

-- Assignation du serveur SMTP entrant au domain 'Domain 1'
INSERT INTO DomainMailServer (domainmailserver_domain_id, domainmailserver_mailserver_id, domainmailserver_role) VALUES ( (SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), (SELECT i.mailserver_id FROM MailServer i, Host j WHERE i.mailserver_host_id=j.host_id AND j.host_name='smtp-in'), 'smtp_in' );
-- Assignation du serveur de BAL au domain 'Domain 1'
INSERT INTO DomainMailServer (domainmailserver_domain_id, domainmailserver_mailserver_id, domainmailserver_role) VALUES ( (SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), (SELECT i.mailserver_id FROM MailServer i, Host j WHERE i.mailserver_host_id=j.host_id AND j.host_name='srv-mail'), 'imap' );


-- -----------------------------------------------------------------------------
-- Test User creation
-- -----------------------------------------------------------------------------
-- Utilisateur de test :
--  - appartenant au domaine global ;
--  - ayant le droit mail ;
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password_type, userobm_password, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_address1, userobm_address2, userobm_address3, userobm_zipcode, userobm_town, userobm_phone, userobm_fax, userobm_mobile, userobm_mail_perms, userobm_mail_ext_perms, userobm_email, userobm_mail_server_id, userobm_title, userobm_service, userobm_description) VALUES ('0', 'test00', 'PLAIN', 'ptest00', 'user', 'User', 'Test 00',  '1050', '513', '23, rue des champs', 'Nolwen du lac', 'Près de la ferme', '31400', 'La ville à Ramon', '05 62 19 24 91', '05 62 19 24 92', '06 55 55 55 55', '1', '1', 'test00\r\nmail.test00', (SELECT mailserver_id FROM MailServer JOIN Host ON host_id=mailserver_host_id WHERE host_name='srv-mail'), 'Chef', 'Rapide', 'Utilisateur appartient au domaine global');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT MAX(entity_id), MAX(userobm_id) FROM UserObm, Entity;

-- Utilisateur de test :
--  - appartenant au domaine 1 ;
--  - ayant le droit mail ;
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password_type, userobm_password, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_address1, userobm_zipcode, userobm_town, userobm_phone, userobm_phone2, userobm_fax, userobm_fax2, userobm_mobile, userobm_mail_perms, userobm_mail_ext_perms, userobm_email, userobm_mail_server_id, userobm_title, userobm_service, userobm_description, userobm_vacation_enable, userobm_vacation_message) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 'test01', 'PLAIN', 'ptest01', 'user', 'User', 'Test 01', '1051', '513', '23, rue des champs', '31400', 'La ville à Raymond', '05 62 19 24 91', '123', '05 62 19 24 92', '+33 5 62 19 24 91', '06 55 55 55 55', '1', '1', 'test01\r\nmail.test01', (SELECT mailserver_id FROM MailServer JOIN Host ON host_id=mailserver_host_id WHERE host_name='srv-mail'), 'Autan en emporte le vent', 'Compris', 'Utilisateur n''appartenant qu''au domaine 1', 1, 'Ceci est le message d''absence');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT MAX(entity_id), MAX(userobm_id) FROM UserObm, Entity;

-- Utilisateur de test :
--  - appartenant au domaine 1 ;
--  - ayant le droit mail ;
--  - profil administrateur ;
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password_type, userobm_password, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_address1, userobm_zipcode, userobm_town, userobm_phone, userobm_phone2, userobm_fax, userobm_fax2, userobm_mobile, userobm_mail_perms, userobm_mail_ext_perms, userobm_email, userobm_mail_server_id, userobm_description) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 'test02', 'PLAIN', 'ptest02', 'admin', 'User', 'Test 02', '1052', '512', '23, rue des champs', '31400', 'La ville à Raymond', '05 62 19 24 91', '123', '05 62 19 24 92', '+33 5 62 19 24 91', '06 55 55 55 55', '1', '1', 'test02\r\ntest02.admin', (SELECT mailserver_id FROM MailServer JOIN Host ON host_id=mailserver_host_id WHERE host_name='srv-mail'), 'Utilisateur n''appartenant qu''au domaine 1');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT MAX(entity_id), MAX(userobm_id) FROM UserObm, Entity;

-- -----------------------------------------------------------------------------
-- Remplissage de la table 'UGroup' : Création d'un groupe
-- -----------------------------------------------------------------------------
DELETE FROM UGroup;

-- Admin Group
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 0, 0, 1, NULL, 0, 1000, 'Admin', 'Admin', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- Commercial Group
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 0, 0, 1, NULL, 0, 1001, 'Commercial', 'Commercial', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- Production Group
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 0, 0, 1, NULL, 0, 1002, 'Production', 'Production', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- Group test without e-mail
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 0, 0, 1, NULL, 0, 1003, 'grpTest00', 'Groupe de test 00', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- Groupe de test AVEC e-mail
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 0, 0, 1, NULL, 0, 1004, 'grpTest01', 'Groupe de test 01 avec e-mail', 'grpTest01', NULL);

-- 'Administrateurs du domaine' Group
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES (1, 1, 0, 0, NULL, 1, 512, 'Administrateurs du domaine', 'Groupe des administrateurs du domaine et d''OBM', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- 'Utilisateurs du domaine' Group
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES (1, 1, 0, 0, NULL, 1, 513, 'Utilisateurs du domaine', 'Groupe des utilisateurs du domaine Samba', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- 'Invités du domaine' Group
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES (1, 1, 0, 0, NULL, 1, 514, 'Invités du domaine', 'Groupe des invités du domaine Samba', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- "Hôtes du domaine' Group
INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES (1, 1, 0, 0, NULL, 1, 515, 'Hôtes du domaine', 'Groupe des hôtes du domaine Samba', '', NULL);
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT MAX(entity_id), MAX(group_id) FROM UGroup, Entity;

-- -----------------------------------------------------------------------------
-- Remplissage de la table 'UserObmGroup' :  Positionnement d'utilisateurs dans
-- les groupes
-- -----------------------------------------------------------------------------
DELETE FROM UserObmGroup;

-- Members of group 'Admin' : 'test01' 'test02'
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='Admin'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test01'));
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='Admin'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test02'));

-- Members of group 'Commercial' : 'test01' 'test02'
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='Commercial'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test01'));
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='Commercial'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test02'));

-- Members of group 'Production' : 'test01' 'test02'
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='Production'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test01'));
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='Production'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test02'));

-- Members of group 'grpTest00' : 'test01' et 'test02'
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='grpTest00'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test01'));
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='grpTest00'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test02'));

-- Members of group 'grpTest01' : 'test01'
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='grpTest01'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test01'));


-- -----------------------------------------------------------------------------
-- Remplissage de la table 'MailShare' : Création d'un répertoire partagé
-- -----------------------------------------------------------------------------
DELETE FROM MailShare;

-- Appartenant à tous les domaines
INSERT INTO MailShare (mailshare_domain_id, mailshare_mail_server_id, mailshare_name, mailshare_description, mailshare_email) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), (SELECT mailserver_id FROM MailServer JOIN Host ON host_id=mailserver_host_id WHERE host_name='srv-mail'), 'mailShare00', 'Répertoire partagé de test 00, appartenant au domaine 1', 'mailshare00');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO MailshareEntity (mailshareentity_entity_id, mailshareentity_mailshare_id) SELECT MAX(entity_id), MAX(mailshare_id) FROM Mailshare, Entity;

-- Appartenant au domaine 1
INSERT INTO MailShare (mailshare_domain_id, mailshare_mail_server_id, mailshare_name, mailshare_description, mailshare_email) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), (SELECT mailserver_id FROM MailServer JOIN Host ON host_id=mailserver_host_id WHERE host_name='srv-mail'), 'mailShare01', 'Répertoire partagé de test 01, appartenant au domaine 2', 'mailshare01');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO MailshareEntity (mailshareentity_entity_id, mailshareentity_mailshare_id) SELECT MAX(entity_id), MAX(mailshare_id) FROM Mailshare, Entity;

-- -----------------------------------------------------------------------------
-- Remplissage de la table 'Samba' : Création d'un domaine windows
-- -----------------------------------------------------------------------------
DELETE FROM Samba;

INSERT INTO Samba ( samba_domain_id, samba_name, samba_value ) VALUES ( 1, 'samba_domain', 'TEST-DOMAIN' );
INSERT INTO Samba ( samba_domain_id, samba_name, samba_value ) VALUES ( 1, 'samba_sid', 'S-1-5-21-735385164-1086204177-245137893' );
INSERT INTO Samba ( samba_domain_id, samba_name, samba_value ) VALUES ( 1, 'samba_pdc', 'PDCTEST' );
INSERT INTO Samba ( samba_domain_id, samba_name, samba_value ) VALUES ( 1, 'samba_profile', '\\\\PDCTEST\\%u\\.profiles' );
INSERT INTO Samba ( samba_domain_id, samba_name, samba_value ) VALUES ( 1, 'samba_home_def', '\\\\PDCTEST\\%u' );
INSERT INTO Samba ( samba_domain_id, samba_name, samba_value ) VALUES ( 1, 'samba_home_drive_def', 'P' );

-- -----------------------------------------------------------------------------
-- Remplissage de la table 'EntityRight' : Gestion des droits
-- -----------------------------------------------------------------------------
DELETE FROM EntityRight;

INSERT INTO EntityRight ( entityright_entity_id, entityright_consumer_id, entityright_read, entityright_write, entityright_admin ) 
SELECT mailshareentity_entity_id, userentity_entity_id, 0, 0, 1
FROM MailShare INNER JOIN MailshareEntity ON mailshare_id = mailshareentity_mailshare_id, UserObm INNER JOIN UserEntity ON userobm_id = userentity_user_id
WHERE userobm_login='admin1' AND mailshare_name='mailShare00'

INSERT INTO EntityRight ( entityright_entity_id, entityright_consumer_id, entityright_read, entityright_write, entityright_admin ) 
SELECT mailshareentity_entity_id, userentity_entity_id, 0, 0, 1
FROM MailShare INNER JOIN MailshareEntity ON mailshare_id = mailshareentity_mailshare_id, UserObm INNER JOIN UserEntity ON userobm_id = userentity_user_id
WHERE userobm_login='admin1' AND mailshare_name='mailShare01'
