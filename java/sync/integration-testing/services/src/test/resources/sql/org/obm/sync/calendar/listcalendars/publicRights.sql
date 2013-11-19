INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_archive, userobm_email) 
  VALUES
    (1, 'usera','usera','PLAIN','user', 'Lastname_a', 'Firstname', '1000', '512', '0', 'usera'),
    (1, 'userb','userb','PLAIN','user', 'Lastname_b', 'Firstname', '1000', '512', '0', 'userb'),
    (1, 'userc','userc','PLAIN','user', 'Lastname_c', 'Firstname', '1000', '512', '0', 'userc'),
    (1, 'userd','userd','PLAIN','user', 'Lastname_d', 'Firstname', '1000', '512', '0', 'userd'),
    (1, 'usere','usere','PLAIN','user', 'Lastname_e', 'Firstname', '1000', '512', '0', 'usere'),
    (1, 'userf','userf','PLAIN','user', 'Lastname_f', 'Firstname', '1000', '512', '0', 'userf'),
    (1, 'userg','userg','PLAIN','user', 'Lastname_g', 'Firstname', '1000', '512', '0', 'userg'),
    (1, 'userh','userh','PLAIN','user', 'Lastname_h', 'Firstname', '1000', '512', '0', 'userh'),
    (1, 'useri','useri','PLAIN','user', 'Lastname_i', 'Firstname', '1000', '512', '0', 'useri'),
    (1, 'userj','userj','PLAIN','user', 'Lastname_j', 'Firstname', '1000', '512', '0', 'userj');

INSERT INTO Entity (entity_mailing)
  VALUES
    (TRUE), (TRUE), (TRUE), (TRUE), (TRUE), (TRUE), (TRUE), (TRUE), (TRUE), (TRUE);

INSERT INTO UserEntity (userentity_entity_id, userentity_user_id)
  VALUES
    (7, 5), (8, 6), (9, 7), (10, 8), (11, 9), (12, 10), (13, 11), (14, 12), (15, 13), (16, 14);

INSERT INTO CalendarEntity (calendarentity_entity_id, calendarentity_calendar_id)
  VALUES
    (7, 5), (8, 6), (9, 7), (10, 8), (11, 9), (12, 10), (13, 11), (14, 12), (15, 13), (16, 14);

INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
  VALUES
    (7, NULL, 1, 1, 1, 0),
    (8, NULL, 1, 1, 1, 0),
    (9, NULL, 1, 1, 0, 0),
    (10, NULL, 1, 1, 0, 0),
    (11, NULL, 1, 1, 0, 0),
    (12, NULL, 1, 1, 0, 0),
    (13, NULL, 1, 1, 0, 0),
    (14, NULL, 1, 1, 0, 0),
    (15, NULL, 1, 1, 0, 0),
    (16, NULL, 1, 1, 0, 0);