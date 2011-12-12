-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : obm_default_values_2.4.sql                                   //
--     - Desc : Insertion of Default values (database independant)           //
-- 2007-04-23 Pierre Baudracco                                               //
-- /////////////////////////////////////////////////////////////////////////////
-- $Id$
-- /////////////////////////////////////////////////////////////////////////////


-- -----------------------------------------------------------------------------
-- Default Information (table ObmInfo)
-- -----------------------------------------------------------------------------

-- Update DB version
DELETE FROM ObmInfo where obminfo_name='db_version';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('db_version', '2.4.0');
DELETE FROM ObmInfo where obminfo_name='remote_access';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('remote_access', '0');
DELETE FROM ObmInfo where obminfo_name='update_lock';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('update_lock', '0');
DELETE FROM ObmInfo where obminfo_name='scope-progress';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('scope-progress', '0');


-- -----------------------------------------------------------------------------
-- Default Admin 0 creation
-- -----------------------------------------------------------------------------
DELETE FROM UserObm;

-- Global Domain
INSERT INTO Domain (domain_timecreate,domain_label,domain_description,domain_name,domain_global, domain_uuid) VALUES  (NOW(), 'Global Domain', 'Virtual domain for managing domains', 'global.virt', TRUE, UUID());
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO DomainEntity (domainentity_entity_id, domainentity_domain_id) SELECT MAX(entity_id), MAX(domain_id) FROM Domain, Entity;
-- Global ADMIN
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid) VALUES ((SELECT domain_id From Domain), 'admin0','admin','PLAIN','admin', 'Admin Lastname', 'Firstname', '1000', '512');
INSERT INTO Entity (entity_mailing) VALUES (TRUE);
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT MAX(entity_id), MAX(userobm_id) FROM UserObm, Entity;

-- -----------------------------------------------------------------------------
-- Default Domain properties
-- -----------------------------------------------------------------------------
DELETE FROM DomainProperty;
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default, domainproperty_readonly) VALUES ('update_state','integer', 1, 1);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_users','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_mailshares','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_resources','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('mail_quota','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('delegation','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address1','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address2','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address3','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('postcode','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('town','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('group_admin','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('group_com','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('group_prod','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default, domainproperty_readonly) VALUES ('mailshares_quota_default','integer','0','0');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default, domainproperty_readonly) VALUES ('mailshares_quota_max','integer','0','0');

-- Fill the initial update_state for each Domain
INSERT INTO DomainPropertyValue (domainpropertyvalue_domain_id, domainpropertyvalue_property_key, domainpropertyvalue_value) SELECT domain_id , 'update_state', 1 From Domain;

-- -----------------------------------------------------------------------------
-- Remplissage de la table 'UserSystem' : Utilisateurs systeme
-- La modification des valeurs de cette table a des impact sur la configuration
-- système
-- -----------------------------------------------------------------------------
DELETE FROM UserSystem;

-- utilisateur 'cyrus', mot de passe 'cyrus' - doit être administrateur Cyrus
INSERT INTO UserSystem (usersystem_login,usersystem_password,usersystem_uid,usersystem_gid,usersystem_homedir,usersystem_lastname,usersystem_firstname,usersystem_shell) VALUES ('cyrus','cyrus','103','8','/var/spool/cyrus','Cyrus','Administrator','/bin/false');
-- utilisateur 'ldapadmin', mot de passe 'mdp3PaAL' - doit avoir le droit
-- d'écriture sur l'arborescence LDAP d'OBM
INSERT INTO UserSystem (usersystem_login,usersystem_password,usersystem_uid,usersystem_gid,usersystem_homedir,usersystem_lastname,usersystem_firstname,usersystem_shell) VALUES ('ldapadmin','mdp3PaAL','150','65534','/var/lib/ldap','LDAP','Administrator','/bin/false');
-- utilisateur 'samba', mot de passe 'm#Pa!NtA' - doit avoir le droit de
-- lecture/écriture sur une partie de l'arborescence (cf. Samba doc)
INSERT INTO UserSystem (usersystem_login,usersystem_password,usersystem_uid,usersystem_gid,usersystem_homedir,usersystem_lastname,usersystem_firstname,usersystem_shell) VALUES ('samba','m#Pa!NtA','106','65534','/','SAMBA','LDAP writer','/bin/false');
-- utilisateur 'obmsatellite', mot de passe 'mG4_Zdnh' - doit avoir le droit de
-- lecture sur l'arborescence d'OBM
INSERT INTO UserSystem (usersystem_login,usersystem_password,usersystem_uid,usersystem_gid,usersystem_homedir,usersystem_lastname,usersystem_firstname,usersystem_shell) VALUES ('obmsatellite','mG4_Zdnh','200','65534','/','OBM Satellite','LDAP Reader','/bin/false');
-- utilisateur 'obmsatelliterequest', mot de passe 'PgpTWb7x' - utilisé pour
-- s'authentifier auprès d'obmSatellite
INSERT INTO UserSystem (usersystem_login, usersystem_password, usersystem_uid, usersystem_gid,usersystem_homedir, usersystem_lastname, usersystem_firstname, usersystem_shell) VALUES ( 'obmsatelliterequest', 'PgpTWb7x', 201, 65534, '/', 'OBM Satellite', 'HTTP auth request', '/bin/false' );


-- ------------------------------------------------------------------------------
-- Remplissage des tables relatives aux profils utilisateurs
-- ------------------------------------------------------------------------------


-- -----------------------------------------------------------------------------
-- Default Profiles
-- -----------------------------------------------------------------------------

-- -----------------------------------------------------------------------------
-- Admin Profile
INSERT INTO Profile (profile_timeupdate, profile_timecreate, profile_userupdate, profile_usercreate, profile_domain_id, profile_name) values (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, (select domain_id from Domain where domain_name='global.virt'), 'admin');

INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='admin'), TRUE);

INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='admin'), 31);
INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('domain', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='admin'), 0);

INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin'), 'level', '0');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin'), 'level_managepeers', '1');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin'), 'access_restriction', 'ALLOW_ALL');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin'), 'admin_realm', 'admin');
-- -----------------------------------------------------------------------------
-- Admin_delegue Profile
INSERT INTO Profile (profile_timeupdate, profile_timecreate, profile_userupdate, profile_usercreate, profile_domain_id, profile_name) values (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, (select domain_id from Domain where domain_name='global.virt'), 'admin_delegue');

INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin_delegue'), 'level', '2');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin_delegue'), 'level_managepeers', '1');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin_delegue'), 'access_restriction', 'ALLOW_ALL');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='admin_delegue'), 'admin_realm', 'user,delegation');

INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='admin_delegue'), TRUE);

INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='admin_delegue'), 31);
INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('domain', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='admin_delegue'), 0);

-- -----------------------------------------------------------------------------
-- Editor Profile
INSERT INTO Profile (profile_timeupdate, profile_timecreate, profile_userupdate, profile_usercreate, profile_domain_id, profile_name) values (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, (select domain_id from Domain where domain_name='global.virt'), 'editor');

INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='editor'), 'level', '3');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='editor'), 'level_managepeers', '0');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='editor'), 'access_restriction', 'ALLOW_ALL');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='editor'), 'admin_realm', '');

INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='editor'), FALSE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('gw', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='editor'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('prod', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='editor'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('user', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='editor'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('my', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='editor'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('webmail', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='editor'), TRUE);

INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='editor'), 7);

-- -----------------------------------------------------------------------------
-- User Profile
INSERT INTO Profile (profile_timeupdate, profile_timecreate, profile_userupdate, profile_usercreate, profile_domain_id, profile_name) values (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, (select domain_id from Domain where domain_name='global.virt'), 'user');

INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='user'), 'level', '4');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='user'), 'level_managepeers', '0');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='user'), 'access_restriction', 'ALLOW_ALL');
INSERT into ProfileProperty (profileproperty_profile_id, profileproperty_name, profileproperty_value) values ((select profile_id from Profile where profile_name='user'), 'admin_realm', '');

INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), FALSE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('gw', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('prod', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('user', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('my', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), TRUE);
INSERT into ProfileSection (profilesection_section_name, profilesection_domain_id, profilesection_profile_id, profilesection_show) values ('webmail', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), TRUE);

INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('default', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), 3);
INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('calendar', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), 7);
INSERT into ProfileModule (profilemodule_module_name, profilemodule_domain_id, profilemodule_profile_id, profilemodule_right) values ('mailbox', (select domain_id from Domain where domain_name='global.virt'), (select profile_id from Profile where profile_name='user'), 7);
