--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obmdb_test_values_1.2.sql                                    //
--     - Desc : Insertion of Test values (database independant)              //
-- 2005-06-08 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Default User creation
-------------------------------------------------------------------------------
DELETE FROM UserObm;

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname, userobm_domain_id, userobm_uid, userobm_gid) VALUES ('uadmin','6589c59cab273225e6662a1b1558e92b','admin', 'Admin Lastname', 'Firstname', '0', '1000', '512');

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname, userobm_domain_id, userobm_uid, userobm_gid) VALUES ('ueditor','43e77d678db7b87c7b7494685419f7b6','editor', 'Itor', 'Ed', '0', '1001', '513');

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname, userobm_domain_id, userobm_uid, userobm_gid) VALUES ('uuser','8cb1ecc05c0b32c2235046b0d9f23bea','user', 'User', 'John', '0', '1002', '513');



-------------------------------------------------------------------------------
-- Default Company creation
-------------------------------------------------------------------------------
DELETE FROM Company;

INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_archive,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MonNumero123',0,'MaSociete',3,'mon adresse l1','mon adresse l2','31520','MaVille','','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);

INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_archive,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MyRef123',0,'MyCompany',3,'my address l1','my address l2','31520','MyTown','','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);


-------------------------------------------------------------------------------
-- Default Contact creation
-------------------------------------------------------------------------------
DELETE FROM Contact;

INSERT INTO Contact (contact_company_id, contact_kind_id, contact_lastname, contact_firstname, contact_address1,contact_address2, contact_zipcode, contact_town, contact_title, contact_phone, contact_email, contact_archive,contact_comment) VALUES (1,1,'Rabbit','Roger','ad1','ad2','31520','Ramonville','Manager','01 01 01 02 03','roger@rabbit.com','0','comment');


-------------------------------------------------------------------------------
-- Remplissage de la table 'Ldap'
-------------------------------------------------------------------------------
DELETE FROM Ldap;

INSERT INTO Ldap VALUES (0,0,NULL,'rootLdap','dc=<domainName>,dc=local');
INSERT INTO Ldap VALUES (0,0,NULL,'dataType','sambaDomain sambaFreeUnixId');
INSERT INTO Ldap VALUES (1,0,0,'organizationalUnitName','users');
INSERT INTO Ldap VALUES (1,0,0,'organizationalUnitDesc','Users account');
INSERT INTO Ldap VALUES (1,0,0,'dataType','posixUsers');
INSERT INTO Ldap VALUES (2,0,0,'organizationalUnitName','sysusers');
INSERT INTO Ldap VALUES (2,0,0,'organizationalUnitDesc','System users');
INSERT INTO Ldap VALUES (2,0,0,'dataType','systemUsers');
INSERT INTO Ldap VALUES (3,0,0,'organizationalUnitName','groups');
INSERT INTO Ldap VALUES (3,0,0,'organizationalUnitDesc','Security Groups');
INSERT INTO Ldap VALUES (3,0,0,'dataType','posixGroups');
INSERT INTO Ldap VALUES (4,0,0,'organizationalUnitName','servicesConfiguration');
INSERT INTO Ldap VALUES (4,0,0,'organizationalUnitDesc','Services configuration');
INSERT INTO Ldap VALUES (4,0,0,'dataType','postfixConf');
INSERT INTO Ldap VALUES (7,0,0,'organizationalUnitName','mailShareDir');
INSERT INTO Ldap VALUES (7,0,0,'organizationalUnitDesc','Mail Share Directory');
INSERT INTO Ldap VALUES (7,0,0,'dataType','mailShareDir');
-------------------------------------------------------------------------------
-- Permet de maintenir une branche identique au 'posixGroups' mais utilisant
-- l'ancien objet 'groupOfNames'
--INSERT INTO Ldap VALUES (8,0,0,'organizationalUnitName','groupOfNames');
--INSERT INTO Ldap VALUES (8,0,0,'organizationalUnitDesc','Groups using groupOfNames LDAP object');
--INSERT INTO Ldap VALUES (8,0,0,'dataType','groupOfNames');
-------------------------------------------------------------------------------
-- Valeurs a mettre si module Samba actif
--UPDATE Ldap SET ldap_value='posixUsers sambaUsers' WHERE ldap_name='dataType' AND ldap_id='1';
--UPDATE Ldap SET ldap_value='posixGroups sambaGroups' WHERE ldap_name='dataType' AND ldap_id='3';
--INSERT INTO Ldap VALUES (5,0,0,'organizationalUnitName','computers');
--INSERT INTO Ldap VALUES (5,0,0,'organizationalUnitDesc','Samba WS');
--INSERT INTO Ldap VALUES (5,0,0,'dataType','sambaHosts');
--INSERT INTO Ldap VALUES (6,0,0,'organizationalUnitName','idmap');
--INSERT INTO Ldap VALUES (6,0,0,'organizationalUnitDesc','Samba Idmap');



-------------------------------------------------------------------------------
-- Début du jeu de test
-------------------------------------------------------------------------------


-------------------------------------------------------------------------------
-- Remplissage de la table 'UserSystem' : Utilisateurs systeme
-------------------------------------------------------------------------------
DELETE FROM UserSystem;

-- mot de passe mdp3pAsI
INSERT INTO UserSystem VALUES (1,'cyrus','cyrus','103','8','/var/spool/cyrus','Cyrus','Administrator','/bin/false');
-- mot de passe mdp2PaAL
INSERT INTO UserSystem VALUES (2,'ldapadmin','mdp3PaAL','150','65534','/var/lib/ldap','LDAP','Administrator','/bin/false');
-- mot de passe m#Pa!NtA
INSERT INTO UserSystem VALUES (3,'samba','m#Pa!NtA','106','65534','/','SAMBA','Administrateur','/bin/false');


-------------------------------------------------------------------------------
-- Test Host creation
-------------------------------------------------------------------------------

DELETE FROM Host;

INSERT INTO Host (host_uid, host_gid, host_name, host_ip, host_description) VALUES ('1500', '1000', 'srv-mail', '10.0.0.101', 'Serveur de courrier');


-------------------------------------------------------------------------------
-- Test User creation
-------------------------------------------------------------------------------
-- Utilisateur de test :
--  - appartenant a tous les domaines ;
--  - ayant le droit mail ;
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password_type, userobm_password, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_address1, userobm_zipcode, userobm_town, userobm_phone, userobm_fax, userobm_mobile, userobm_mail_perms, userobm_mail_ext_perms, userobm_email, userobm_mail_server_id, userobm_description) VALUES ('0', 'test00', 'PLAIN', 'ptest00', 'user', 'User', 'Test 00',  '1050', '512', '23, rue des champs', '31400', 'La ville à Ramon', '05 62 19 24 91', '05 62 19 24 92', '06 55 55 55 55', '1', '1', 'test00\r\nmail.test00', (SELECT host_id FROM Host WHERE host_name='srv-mail'), 'Utilisateur appartient au domaine global');

-- Utilisateur de test :
--  - appartenant au domaine 1 ;
--  - ayant le droit mail ;
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password_type, userobm_password, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_address1, userobm_zipcode, userobm_town, userobm_phone, userobm_phone2, userobm_fax, userobm_fax2, userobm_mobile, userobm_mail_perms, userobm_mail_ext_perms, userobm_email, userobm_mail_server_id, userobm_description) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 'test01', 'PLAIN', 'ptest01', 'user', 'User', 'Test 01', '1051', '512', '23, rue des champs', '31400', 'La ville à Raymond', '05 62 19 24 91', '123', '05 62 19 24 92', '+33 5 62 19 24 91', '06 55 55 55 55', '1', '1', 'test01\r\nmail.test01', (SELECT host_id FROM Host WHERE host_name='srv-mail'), 'Utilisateur n''appartenant qu''au domaine 1');


-------------------------------------------------------------------------------
-- Domain creation
-------------------------------------------------------------------------------
DELETE FROM Domain;

INSERT INTO Domain (domain_id, domain_timeupdate, domain_timecreate, domain_userupdate, domain_usercreate, domain_label, domain_description, domain_domain_name, domain_alias) VALUES ( 1, NULL, '2006-09-07 11:45:59', NULL, '', 'Domain 1', '', 'aliacom.fr', NULL );
INSERT INTO Domain (domain_id, domain_timeupdate, domain_timecreate, domain_userupdate, domain_usercreate, domain_label, domain_description, domain_domain_name, domain_alias) VALUES ( 2, NULL, '2006-09-07 11:45:59', NULL, '', 'Domain 2', '', 'test1.aliacom.fr', 'test2.aliacom.fr\r\ntest3.aliacom.com' );


-------------------------------------------------------------------------------
-- Remplissage de la table 'UGroup' : Création d'un groupe
-------------------------------------------------------------------------------
DELETE FROM UGroup;

-- Groupe de test SANS e-mail
INSERT INTO `UGroup` (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES (0, 0, 0, 1, NULL, 0, 1000, 'grpTest00', 'Groupe de test 00', '', NULL);

-- Groupe de test AVEC e-mail
INSERT INTO `UGroup` (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email, group_contacts) VALUES (0, 0, 0, 1, NULL, 0, 1001, 'grpTest01', 'Groupe de test 01 avec e-mail', 'grpTest01', NULL);


-------------------------------------------------------------------------------
-- Remplissage de la table 'UserObmGroup' :  Positionnement d'utilisateurs dans
-- les groupes
-------------------------------------------------------------------------------
DELETE FROM UserObmGroup;

-- Le groupe 'grpTest00' contient les utilisateurs 'test00' et 'test01'
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='grpTest00'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test00'));
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='grpTest00'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test01'));

-- Le groupe 'grpTest01' contient les utilisateurs 'test00' et 'test01'
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='grpTest01'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test00'));
INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES ((SELECT group_id FROM UGroup WHERE group_name='grpTest01'), (SELECT userobm_id FROM UserObm WHERE userobm_login='test01'));


-------------------------------------------------------------------------------
-- Remplissage de la table 'MailServer' : déclaration des serveurs de BALs
-------------------------------------------------------------------------------
DELETE FROM MailServer;

-- Déclaration d'un serveur de BAL sans hôte relais (relayhost)
INSERT INTO MailServer (mailserver_id) VALUES ( (SELECT host_id FROM Host WHERE host_name='srv-mail') );


-------------------------------------------------------------------------------
-- Remplissage de la table 'MailServerNetwork' : déclaration des serveurs
-- réseaux locaux des serveurs de BALs
-------------------------------------------------------------------------------
DELETE FROM MailServerNetwork;

INSERT INTO MailServerNetwork (mailservernetwork_mailserver_id, mailservernetwork_ip) VALUES ( (SELECT host_id FROM Host WHERE host_name='srv-mail'), '127.0.0.1' );
INSERT INTO MailServerNetwork (mailservernetwork_mailserver_id, mailservernetwork_ip) VALUES ( (SELECT host_id FROM Host WHERE host_name='srv-mail'), '10.0.0.0/24' );


-------------------------------------------------------------------------------
-- Remplissage de la table 'MailShareDir' : Création d'un répertoire partagé
-------------------------------------------------------------------------------
DELETE FROM MailShareDir;

-- Appartenant à tous les domaines
INSERT INTO MailShareDir (mailsharedir_domain_id, mailsharedir_name, mailsharedir_description, mailsharedir_email) VALUES (0, 'mailShare00', 'Répertoire partagé de test 00', 'mailshare00');

-- Appartenant au domaine 1
INSERT INTO MailShareDir (mailsharedir_domain_id, mailsharedir_name, mailsharedir_description, mailsharedir_email) VALUES ((SELECT domain_id FROM Domain WHERE domain_label='Domain 1'), 'mailShare01', 'Répertoire partagé de test 01, appartenant au domaine 1', 'mailshare01');
