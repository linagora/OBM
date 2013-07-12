INSERT INTO entity (entity_mailing)
    VALUES
        (true),
        (true), // <- Domains
        (true),
        (true),
        (true),
        (true),
        (true), // <- Users
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true),
        (true); // <- Groups

INSERT INTO domain (domain_name, domain_uuid, domain_label) VALUES ('test.tlse.lng', 'ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6', 'test.tlse.lng');
INSERT INTO domainentity (domainentity_entity_id, domainentity_domain_id) VALUES (1, 1);
INSERT INTO domain (domain_name, domain_uuid, domain_label) VALUES ('test2.tlse.lng', '3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf', 'test2.tlse.lng');
INSERT INTO domainentity (domainentity_entity_id, domainentity_domain_id) VALUES (2, 2);

INSERT INTO host (host_domain_id, host_name, host_ip, host_fqdn)
    VALUES
        (1, 'mail', '1.2.3.4', 'mail.tlse.lng'),
        (1, 'sync', '1.2.3.5', 'sync.tlse.lng');
INSERT INTO serviceproperty (serviceproperty_service, serviceproperty_property, serviceproperty_entity_id, serviceproperty_value)
    VALUES
        ('mail', 'smtp_in', 1, 1),
        ('mail', 'imap', 1, 1),
        ('sync', 'obm_sync', 1, 2),
        ('mail', 'smtp_in', 2, 1);

INSERT INTO profile (profile_domain_id, profile_name) VALUES (1, 'admin');
INSERT INTO profile (profile_domain_id, profile_name) VALUES (1, 'user');
INSERT INTO profile (profile_domain_id, profile_name) VALUES (2, 'editor');

INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_archive, userobm_email, userobm_mail_server_id) 
    VALUES
        (1, 'user1','user1','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user1', 1),
        (1, 'user2','user2','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user2', 1),
        (1, 'user3','user3','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user3', 1),
        (1, 'user4','user4','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', '', NULL),
        (2, 'user1','user1','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user1', 1);
        
INSERT INTO userentity (userentity_entity_id, userentity_user_id)
    VALUES
        (3, 1),
        (4, 2),
        (5, 3),
        (6, 4),
        (7, 5);

INSERT INTO ugroup (group_id, group_domain_id, group_ext_id, group_name, group_desc)
    VALUES
        (1, 1, 'existing-nousers-nosubgroups', 'existing-nousers-nosubgroups-name', 'existing-nousers-nosubgroups-description'),
        (2, 1, 'existing-users-nosubgroups', 'existing-users-nosubgroups-name', 'existing-users-nosubgroups-description'),
        (3, 1, 'existing-nousers-subgroups', 'existing-nousers-subgroups-name', 'existing-nousers-subgroups-description'),
        (4, 1, 'existing-nousers-subgroups-child1', 'existing-nousers-subgroups-child1-name', 'existing-nousers-subgroups-child1-description'),
        (5, 1, 'existing-users-subgroups', 'existing-users-subgroups-name', 'existing-users-subgroups-description'),
        (6, 1, 'existing-users-subgroups-child1', 'existing-users-subgroups-child1-name', 'existing-users-subgroups-child1-description'),
        (7, 1, 'existing-users-subgroups-child2', 'existing-users-subgroups-child2-name', 'existing-users-subgroups-child2-description'),
        (8, 1, 'recursive-direct-parent', 'recursive-direct-parent-name', 'recursive-direct-parent-description'),
        (9, 1, 'recursive-direct-child1', 'recursive-direct-child1-name', 'recursive-direct-child1-description'),
        (10, 1, 'recursive-multichild-parent', 'recursive-multichild-parent-name', 'recursive-multichild-parent-description'),
        (11, 1, 'recursive-multichild-child1', 'recursive-multichild-child1-name', 'recursive-multichild-child1-description'),
        (12, 1, 'recursive-multichild-child2', 'recursive-multichild-child2-name', 'recursive-multichild-child2-description'),
        (13, 1, 'recursive-multichild-childcommon', 'recursive-multichild-childcommon-name', 'recursive-multichild-childcommon-description'),
        (14, 1, 'recursive-multichild-childcommonexpand', 'recursive-multichild-childcommonexpand-name', 'recursive-multichild-childcommonexpand-description'),
        (15, 1, 'modified-group', 'modified-group-name', 'modified-group-description'),
        (16, 1, 'delete-group', 'delete-group-name', 'delete-group-description'),
        (17, 1, 'addusersubgroup-group-parent', 'addusersubgroup-group-parent-name', 'addusersubgroup-group-parent-description'),
        (18, 1, 'addusersubgroup-group-child', 'addusersubgroup-group-child-name', 'addusersubgroup-group-child-description'),
        (19, 1, 'removeusersubgroup-group-parent', 'removeusersubgroup-group-parent-name', 'removeusersubgroup-group-parent-description'),
        (20, 1, 'removeusersubgroup-group-child', 'removeusersubgroup-group-child-name', 'removeusersubgroup-group-child-description');

ALTER SEQUENCE ugroup_group_id_seq RESTART WITH 21;

INSERT INTO groupentity (groupentity_entity_id, groupentity_group_id)
    VALUES
        (8, 1),
        (9, 2),
        (10, 3),
        (11, 4),
        (12, 5),
        (13, 6),
        (14, 7),
        (15, 8),
        (16, 9),
        (17, 10),
        (18, 11),
        (19, 12),
        (20, 13),
        (21, 14),
        (22, 15),
        (23, 16),
        (24, 17),
        (25, 18),
        (26, 19),
        (27, 20);

INSERT INTO userobmgroup (userobmgroup_userobm_id, userobmgroup_group_id) VALUES (1, 2);
INSERT INTO userobmgroup (userobmgroup_userobm_id, userobmgroup_group_id) VALUES (2, 4);
INSERT INTO userobmgroup (userobmgroup_userobm_id, userobmgroup_group_id) VALUES (1, 5);
INSERT INTO userobmgroup (userobmgroup_userobm_id, userobmgroup_group_id) VALUES (1, 19);

INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (3, 4);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (5, 6);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (5, 7);

INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (8, 9);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (9, 8);

INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (10, 11);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (10, 12);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (11, 13);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (12, 13);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (13, 14);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (19, 20);

INSERT INTO UserSystem (usersystem_login, usersystem_password, usersystem_homedir)
    VALUES
        ('obmsatelliterequest', 'osrpassword', ''),
        ('cyrus', 'cyrus', '');