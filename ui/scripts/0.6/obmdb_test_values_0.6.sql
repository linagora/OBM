--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obmdb_test_values.sql                                        //
--     - Desc : Insertion of Test values (database independant)              //
-- 2002-12-17 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Default User creation
-------------------------------------------------------------------------------

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname) VALUES ('uadmin','padmin','admin', 'Admin Lastname', 'Admin Firstname');

INSERT INTO UserObm (userobm_login,userobm_password,userobm_perms) VALUES ('ueditor','peditor','editor');

INSERT INTO UserObm (userobm_login,userobm_password,userobm_perms) VALUES ('uuser','puser','user');


-------------------------------------------------------------------------------
-- Default Company creation
-------------------------------------------------------------------------------
INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_state,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_country, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MonNumero123',1,'MaSociete',3,'mon adresse l1','mon adresse l2','31520','MaVille','','MyCountry','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);

INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_state,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_country, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MyRef123',1,'MyCompany',3,'my address l1','my address l2','31520','MyTown','','MyCountry','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);


-------------------------------------------------------------------------------
-- Default Contact creation
-------------------------------------------------------------------------------
INSERT INTO Contact VALUES (1,'','',NULL,1,1,1,'Rabbit','Roger','','','','','','','','','','','','','',0);
