INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_archive, userobm_email) 
  VALUES
    (1, 'userk','userk','PLAIN','user', 'Lastname_k', 'Firstname', '1000', '512', '0', 'userk'),
    (1, 'userl','userl','PLAIN','user', 'Lastname_l', 'Firstname', '1000', '512', '0', 'userl'),
    (1, 'userm','userm','PLAIN','user', 'Lastname_m', 'Firstname', '1000', '512', '0', 'userm'),
    (1, 'usern','usern','PLAIN','user', 'Lastname_n', 'Firstname', '1000', '512', '0', 'usern'),
    (1, 'usero','usero','PLAIN','user', 'Lastname_o', 'Firstname', '1000', '512', '0', 'usero');

INSERT INTO Entity (entity_mailing)
  VALUES
    (TRUE), (TRUE), (TRUE), (TRUE), (TRUE), (TRUE);

INSERT INTO UserEntity (userentity_entity_id, userentity_user_id)
  VALUES
    (17, 15), (18, 16), (19, 17), (20, 18), (21, 19);

INSERT INTO CalendarEntity (calendarentity_entity_id, calendarentity_calendar_id)
  VALUES
    (17, 15), (18, 16), (19, 17), (20, 18), (21, 19);

INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id)
  VALUES
    (22, 1);

INSERT INTO of_usergroup (of_usergroup_group_id, of_usergroup_user_id)
  VALUES
    (1, 1); /* user1 now belongs to fake group 1 */

INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
  VALUES
    (17, 22, 1, 1, 1, 0),
    (18, 22, 1, 1, 1, 0),
    (19, 22, 1, 1, 0, 0),
    (20, 22, 1, 1, 0, 0),
    (21, 22, 1, 1, 0, 0);