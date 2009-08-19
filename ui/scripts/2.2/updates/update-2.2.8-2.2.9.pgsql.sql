-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.8-2.2.9.pgsql.sql
-- 2009-08-17 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////

UPDATE ObmInfo SET obminfo_value = 'pre-2.2.9' WHERE obminfo_name = 'db_version';

UPDATE Phone SET phone_label = REPLACE(phone_label , 'PREF;', '') WHERE phone_label like 'PREF%';
UPDATE IM SET im_label = REPLACE(im_label , 'PREF;', '') WHERE im_label like 'PREF%';
UPDATE Email SET email_label = REPLACE(email_label , 'PREF;', '') WHERE email_label like 'PREF%';
UPDATE Adress SET adress_label = REPLACE(adress_label , 'PREF;', '') WHERE adress_label like 'PREF%';
UPDATE Website SET website_label = REPLACE(website_label , 'PREF;', '') WHERE website_label like 'PREF%';
UPDATE DisplayPref SET display_fieldname = 'WorkPhone.phone_number' WHERE display_fieldname = 'phone_number';


-- Inserting preferences for phone
-- Contact
UPDATE DisplayPref SET display_fieldorder = display_fieldorder + 3 WHERE display_fieldorder > 
(
  SELECT ph.display_fieldorder FROM DisplayPref ph 
  WHERE (ph.display_user_id = DisplayPref.display_user_id OR (ph.display_user_id IS NULL AND DisplayPref.display_user_id IS NULL ))
  AND ph.display_fieldname = 'phone_number' AND ph.display_entity = 'contact'
) AND display_entity = 'contact';
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display)
SELECT display_user_id, 'contact', 'HomePhone.phone_number', display_fieldorder + 1, display_display FROM DisplayPref 
WHERE display_fieldname = 'phone_number' AND display_entity = 'contact';
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display)
SELECT display_user_id, 'contact', 'MobilePhone.phone_number', display_fieldorder + 2, display_display FROM DisplayPref 
WHERE display_fieldname = 'phone_number' AND display_entity = 'contact';
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display)
SELECT display_user_id, 'contact', 'FaxPhone.phone_number', display_fieldorder + 3, display_display FROM DisplayPref 
WHERE display_fieldname = 'phone_number' AND display_entity = 'contact';
UPDATE DisplayPref SET display_fieldname = 'WorkPhone.phone_number' WHERE display_fieldname = 'phone_number' AND display_entity = 'contact';
-- Company
UPDATE DisplayPref SET display_fieldorder = display_fieldorder + 1 WHERE display_fieldorder > 
(
  SELECT ph.display_fieldorder FROM DisplayPref ph 
  WHERE (ph.display_user_id = DisplayPref.display_user_id OR (ph.display_user_id IS NULL AND DisplayPref.display_user_id IS NULL ))
  AND ph.display_fieldname = 'phone_number' AND ph.display_entity = 'company'
) AND display_entity = 'company';
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display)
SELECT display_user_id, 'company', 'FaxPhone.phone_number', display_fieldorder + 1, display_display FROM DisplayPref 
WHERE display_fieldname = 'phone_number' AND display_entity = 'company';
UPDATE DisplayPref SET display_fieldname = 'WorkPhone.phone_number' WHERE display_fieldname = 'phone_number' AND display_entity = 'company';

UPDATE ObmInfo SET obminfo_value = '2.2.9' WHERE obminfo_name = 'db_version';
