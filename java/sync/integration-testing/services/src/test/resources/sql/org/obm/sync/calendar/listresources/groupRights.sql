INSERT INTO Resource (resource_domain_id, resource_name, resource_description, resource_email) 
  VALUES
    (1, 'resd', 'description of resd', 'res-d@domain.org'),
    (1, 'rese', 'description of rese', 'res-e@domain.org'),
    (1, 'resf', 'description of resf', 'res-f@domain.org');

INSERT INTO Entity (entity_mailing)
  VALUES
    (TRUE), (TRUE), (TRUE), (TRUE);

INSERT INTO ResourceEntity (resourceentity_entity_id, resourceentity_resource_id)
  VALUES
    (10, 4), (11, 5), (12, 6);

INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id)
  VALUES
    (13, 1);

INSERT INTO of_usergroup (of_usergroup_group_id, of_usergroup_user_id)
  VALUES
    (1, 1); /* user1 now belongs to fake group 1 */

INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
  VALUES
    (10, 13, 1, 1, 1, 0),
    (11, 13, 1, 1, 0, 0),
    (12, 13, 1, 1, 0, 0);