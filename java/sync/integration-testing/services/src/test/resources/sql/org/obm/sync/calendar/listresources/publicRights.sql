INSERT INTO Resource (resource_domain_id, resource_name, resource_description, resource_email) 
  VALUES
    (1, 'resa', 'description of resa', 'res-a@domain.org'),
    (1, 'resb', 'description of resb', 'res-b@domain.org'),
    (1, 'resc', 'description of resc', 'res-c@domain.org');

INSERT INTO Entity (entity_mailing)
  VALUES
    (TRUE), (TRUE), (TRUE);

INSERT INTO ResourceEntity (resourceentity_entity_id, resourceentity_resource_id)
  VALUES
    (7, 1), (8, 2), (9, 3);

INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
  VALUES
    (7, NULL, 1, 1, 1, 0),
    (8, NULL, 1, 1, 0, 0),
    (9, NULL, 1, 1, 0, 0);