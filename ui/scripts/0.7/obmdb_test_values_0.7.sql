--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obmdb_test_values_0.7.sql                                    //
--     - Desc : Insertion of Test values (database independant)              //
-- 2003-07-22 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Default User creation
-------------------------------------------------------------------------------

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname) VALUES ('uadmin','padmin','admin', 'Admin Lastname', 'Admin Firstname');

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname) VALUES ('ueditor','peditor','editor', 'Itor', 'Ed');

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname) VALUES ('uuser','puser','user', 'User', 'John');


-------------------------------------------------------------------------------
-- Default Company creation
-------------------------------------------------------------------------------
INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_archive,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_country, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MonNumero123',0,'MaSociete',3,'mon adresse l1','mon adresse l2','31520','MaVille','','MyCountry','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);

INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_archive,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_country, company_phone, company_fax, company_web, company_email, company_comment) VALUES (null,null,2,0,'MyRef123',0,'MyCompany',3,'my address l1','my address l2','31520','MyTown','','MyCountry','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',NULL);


-------------------------------------------------------------------------------
-- Default Contact creation
-------------------------------------------------------------------------------
INSERT INTO Contact (contact_company_id, contact_kind_id, contact_lastname, contact_firstname, contact_address1,contact_address2, contact_zipcode, contact_town, contact_function, contact_phone, contact_email, contact_archive,contact_comment) VALUES (1,1,'Rabbit','Roger','ad1','ad2','31520','Ramonville','Manager','01 01 01 02 03','roger@rabbit.com','0','comment');
