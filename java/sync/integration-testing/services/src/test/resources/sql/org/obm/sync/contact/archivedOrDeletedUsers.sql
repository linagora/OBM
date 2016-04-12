INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_archive, userobm_email, userobm_timeupdate) 
  VALUES (1, 'userarchive','userarchive', 'PLAIN', 'user', 'Lastname', 'Firstname', '1000', '512', '1', 'userarchive', '2016-01-01');

INSERT INTO DeletedUser (deleteduser_user_id, deleteduser_timestamp)
    VALUES (666, '2016-01-01');
