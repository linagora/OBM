INSERT INTO Resource (resource_domain_id, resource_name, resource_description, resource_email) 
  VALUES
    (1, 'res', 'description of resa', 'res@domain.org');

INSERT INTO Entity (entity_mailing)
  VALUES
    (TRUE);

INSERT INTO ResourceEntity (resourceentity_entity_id, resourceentity_resource_id)
  VALUES
    (7, 1);

INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
  VALUES
    (7, NULL, 1, 1, 0, 0);
