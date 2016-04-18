BEGIN;

CREATE VIEW UserObmEmailAlias
  AS
    SELECT userobm_id as userobmemailalias_userobm_id,
           regexp_split_to_table(userobm_email, E'\r\n') as userobmemailalias_alias
    FROM UserObm
    WHERE LENGTH(COALESCE(userobm_email, '')) > 0;

CREATE VIEW DomainAlias
  AS
    SELECT domain_id as domainalias_domain_id,
           regexp_split_to_table(trim(trailing E'\r\n' from (domain_name || E'\r\n' || COALESCE(domain_alias, ''))), E'\r\n') as domainalias_alias
    FROM Domain;

CREATE VIEW UserObmEmail
  AS
    SELECT userobm_id as userobmemail_userobm_id,
           CASE WHEN position('@' in userobmemailalias_alias) > 1 THEN userobmemailalias_alias
                ELSE userobmemailalias_alias || '@' || domainalias_alias
           END as userobmemail_email
    FROM UserObm
    INNER JOIN UserObmEmailAlias ON userobmemailalias_userobm_id = userobm_id
    LEFT JOIN DomainAlias ON domainalias_domain_id = userobm_domain_id AND position('@' in userobmemailalias_alias) = 0;

ALTER TABLE mail_archive
  RENAME mail_archive_scope_includes TO mail_archive_scope_users_includes;

COMMIT;
