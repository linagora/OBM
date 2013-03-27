BEGIN;

--
-- Create tempory table to keep id to ISO3166 relation
--
CREATE TEMPORARY TABLE SortedIsoCode ( sortedisocode_id character(3), sortedisocode_iso varchar(2) );

--
-- Insert id to ISO3166 relation based on French sort of countries
--
INSERT INTO SortedIsoCode (sortedisocode_id, sortedisocode_iso) VALUES ('0', 'AF'),
('1', 'ZA'), ('2', 'AL'), ('3', 'DZ'), ('4', 'DE'), ('5', 'AD'), ('6', 'AO'), ('7', 'AI'),
('8', 'AQ'), ('9', 'AG'), ('10', 'AN'), ('11', 'SA'), ('12', 'AR'), ('13', 'AM'), ('14', 'AW'),
('15', 'AU'), ('16', 'AT'), ('17', 'AZ'), ('18', 'BS'), ('19', 'BH'), ('20', 'BD'), ('21', 'BB'),
('22', 'BY'), ('23', 'BE'), ('24', 'BZ'), ('25', 'BJ'), ('26', 'BM'), ('27', 'BT'), ('28', 'BO'),
('29', 'BA'), ('30', 'BW'), ('31', 'BR'), ('32', 'BN'), ('33', 'BG'), ('34', 'BF'), ('35', 'BI'),
('36', 'KH'), ('37', 'CM'), ('38', 'CA'), ('39', 'CV'), ('40', 'CF'), ('41', 'CL'), ('42', 'CN'),
('43', 'CY'), ('44', 'CO'), ('45', 'KM'), ('46', 'CG'), ('47', 'CD'), ('48', 'KP'), ('49', 'KR'),
('50', 'CR'), ('51', 'CI'), ('52', 'HR'), ('53', 'CU'), ('54', 'DK'), ('55', 'DJ'), ('56', 'DO'),
('57', 'DM'), ('58', 'EG'), ('59', 'SV'), ('60', 'AE'), ('61', 'EC'), ('62', 'ER'), ('63', 'ES'),
('64', 'EE'), ('65', 'US'), ('66', 'ET'), ('67', 'FJ'), ('68', 'FI'), ('69', 'FR'), ('70', 'GA'),
('71', 'GM'), ('72', 'GE'), ('73', 'GS'), ('74', 'GH'), ('75', 'GI'), ('76', 'GR'), ('77', 'GD'),
('78', 'GL'), ('79', 'GP'), ('80', 'GU'), ('81', 'GT'), ('82', 'GN'), ('83', 'GQ'), ('84', 'GW'),
('85', 'GY'), ('86', 'GF'), ('87', 'HT'), ('88', 'HN'), ('89', 'HK'), ('90', 'HU'), ('91', 'BV'),
('92', 'CX'), ('93', 'IM'), ('94', 'NF'), ('95', 'AX'), ('96', 'KY'), ('97', 'CC'), ('98', 'CK'),
('99', 'FK'), ('100', 'FO'), ('101', 'HM'), ('102', 'MP'), ('103', 'MH'), ('104', 'UM'), ('105', 'SB'),
('106', 'TC'), ('107', 'VG'), ('108', 'VI'), ('109', 'IN'), ('110', 'ID'), ('111', 'IR'), ('112', 'IQ'),
('113', 'IE'), ('114', 'IS'), ('115', 'IL'), ('116', 'IT'), ('117', 'JM'), ('118', 'JP'), ('119', 'JO'),
('120', 'KZ'), ('121', 'KE'), ('122', 'KG'), ('123', 'KI'), ('124', 'KW'), ('125', 'LA'), ('126', 'LS'),
('127', 'LV'), ('128', 'LB'), ('129', 'LR'), ('130', 'LY'), ('131', 'LI'), ('132', 'LT'), ('133', 'LU'),
('134', 'MO'), ('135', 'MK'), ('136', 'MG'), ('137', 'MY'), ('138', 'MW'), ('139', 'MV'), ('140', 'ML'),
('141', 'MT'), ('142', 'MA'), ('143', 'MQ'), ('144', 'MU'), ('145', 'MR'), ('146', 'YT'), ('147', 'MX'),
('148', 'FM'), ('149', 'MD'), ('150', 'MC'), ('151', 'MN'), ('152', 'MS'), ('153', 'MZ'), ('154', 'MM'),
('155', 'NA'), ('156', 'NR'), ('157', 'NP'), ('158', 'NI'), ('159', 'NE'), ('160', 'NG'), ('161', 'NU'),
('162', 'NO'), ('163', 'NC'), ('164', 'NZ'), ('165', 'IO'), ('166', 'OM'), ('167', 'UG'), ('168', 'UZ'),
('169', 'PK'), ('170', 'PW'), ('171', 'PA'), ('172', 'PG'), ('173', 'PY'), ('174', 'NL'), ('175', 'PE'),
('176', 'PH'), ('177', 'PN'), ('178', 'PL'), ('179', 'PF'), ('180', 'PR'), ('181', 'PT'), ('182', 'QA'),
('183', 'QO'), ('184', 'RE'), ('185', 'RO'), ('186', 'GB'), ('187', 'RU'), ('188', 'RW'), ('189', 'EH'),
('190', 'KN'), ('191', 'SM'), ('192', 'PM'), ('193', 'VA'), ('194', 'VC'), ('195', 'SH'), ('196', 'LC'),
('197', 'WS'), ('198', 'AS'), ('199', 'ST'), ('200', 'SN'), ('201', 'CS'), ('202', 'SC'), ('203', 'SL'),
('204', 'SG'), ('205', 'SK'), ('206', 'SI'), ('207', 'SO'), ('208', 'SD'), ('209', 'LK'), ('210', 'SE'),
('211', 'CH'), ('212', 'SR'), ('213', 'SJ'), ('214', 'SZ'), ('215', 'SY'), ('216', 'TJ'), ('217', 'TW'),
('218', 'TZ'), ('219', 'TD'), ('220', 'CZ'), ('221', 'TF'), ('222', 'PS'), ('223', 'TH'), ('224', 'TL'),
('225', 'TG'), ('226', 'TK'), ('227', 'TO'), ('228', 'TT'), ('229', 'TN'), ('230', 'TM'), ('231', 'TR'),
('232', 'TV'), ('233', 'UA'), ('234', 'UY'), ('235', 'VU'), ('236', 'VE'), ('237', 'VN'), ('238', 'WF'),
('239', 'YE'), ('240', 'ZM'), ('241', 'ZW');

--
-- Create Temporary Table
--
CREATE TEMPORARY TABLE Addresstmp
AS SELECT address_id, address_entity_id, address_street, address_zipcode,
 address_town, address_expresspostal, address_state, address_country, address_label
FROM Address INNER JOIN SortedIsoCode ON address_country = sortedisocode_id;

--
-- Create a Log table to keep address was modify
--
CREATE TABLE LogModifiedAddress
AS SELECT contact_id, contact_firstname, contact_lastname, address_street,
 address_zipcode, address_town, address_expresspostal, address_state,
 address_country, address_label, address_entity_id, contactentity_contact_id
FROM Address
INNER JOIN SortedIsoCode ON address_country = sortedisocode_id
INNER JOIN ContactEntity ON address_entity_id = contactentity_entity_id
INNER JOIN Contact ON contactentity_contact_id = contact_id;

--
-- Set arbitrary 0 for FR, Afghanistan not more consistent than France for this specific case
--
UPDATE Addresstmp SET address_country = 'FR'
WHERE address_country = '0';

--
-- Update address_country with country in french translation based on SortedIsoCode
--
UPDATE Addresstmp SET address_country = (SELECT sortedisocode_iso FROM SortedIsoCode WHERE address_country = sortedisocode_id);

--
-- Update address_country with country in french translation based on SortedIsoCode
--
DELETE FROM Address WHERE address_id IN (SELECT address_id FROM Addresstmp);

--
-- Update address_country with country in french translation based on SortedIsoCode
--
INSERT INTO Address SELECT * FROM Addresstmp;

COMMIT;
