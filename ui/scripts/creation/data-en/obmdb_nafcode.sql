-- /////////////////////////////////////////////////////////////////////////////
-- // OBM - File : obmdb_nafcode_2.4.sql                                      //
-- //     - Desc : English Database NAF Code Referential 2.4                  //
-- // 2007-04-23 AliaSource - PB                                              //
-- /////////////////////////////////////////////////////////////////////////////
-- $Id$
-- /////////////////////////////////////////////////////////////////////////////

insert into CompanyNafCode (companynafcode_domain_id, companynafcode_title, companynafcode_code, companynafcode_label) values ((SELECT domain_id From Domain), '1', '01', 'Naf Code equivalence in english ?');
insert into CompanyNafCode (companynafcode_domain_id, companynafcode_title, companynafcode_code, companynafcode_label) values ((SELECT domain_id From Domain), '0', '012A', 'Naf Code example');
