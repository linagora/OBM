--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : obmdb_nafcode_2.1.sql                                      //
--//     - Desc : English Database NAF Code Referential 2.1                  //
--// 2007-04-23 AliaSource - PB                                              //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////

insert into CompanyNafCode (companynafcode_title, companynafcode_code, companynafcode_label) values ('1', '01', 'Naf Code equivalence in english ?');
insert into CompanyNafCode (companynafcode_title, companynafcode_code, companynafcode_label) values ('0', '012A', 'Naf Code example');

UPDATE CompanyNafCode SET companynafcode_domain_id = 1;
