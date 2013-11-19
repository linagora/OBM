INSERT INTO Resource (resource_domain_id, resource_name, resource_description, resource_email) 
  VALUES
    (1, 'resg', 'description of resg', 'res-g@domain.org'),
    (1, 'resh', 'description of resh', 'res-h@domain.org'),
    (1, 'resi', 'description of resi', 'res-i@domain.org');

INSERT INTO Entity (entity_mailing)
  VALUES
    (TRUE), (TRUE), (TRUE);

INSERT INTO ResourceEntity (resourceentity_entity_id, resourceentity_resource_id)
  VALUES
    (14, 7), (15, 8), (16, 9);

INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
  VALUES
    (14, 1, 1, 1, 1, 0),
    (15, 1, 1, 1, 0, 0),
    (16, 1, 1, 1, 0, 0);