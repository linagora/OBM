#!/usr/bin/python
# -*- coding: utf-8 -*-

# Depends: python-psycopg2 for postgresql or python-mysqldb for mysql.

# ##### BEGIN LICENSE BLOCK #####
# Copyright (C) 2011-2012  Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
# ##### END LICENSE BLOCK ######

import ConfigParser,sys

ds = None
userid = 0

def getAddressbookEntries():
        cur = ds.cursor()
        cur.execute("SELECT id FROM AddressBook WHERE owner=%d" % userid);
        rows = cur.fetchall()
        cur.close()
        return rows

def addressesOfEntity(id):
	cur=ds.cursor()
	cur.execute("DELETE FROM Address WHERE address_entity_id=%d" % id);
	return True

def phonesOfEntity(id):
        cur=ds.cursor()
        cur.execute("DELETE FROM Phone WHERE phone_entity_id=%d" % id);
	return True

def emailsOfEntity(id):
        cur=ds.cursor()
        cur.execute("DELETE FROM Email WHERE email_entity_id=%d" % id);
        return True


def websitesOfEntity(id):
        cur=ds.cursor()
        cur.execute("DELETE FROM Website WHERE website_entity_id=%d" % id);
        return True

def imsOfEntity(id):
        cur=ds.cursor()
        cur.execute("DELETE FROM IM WHERE im_entity_id=%d" % id);
        return True


def entitiesFromContact(id):
	cur=ds.cursor()
	cur.execute("SELECT contactentity_entity_id FROM ContactEntity WHERE contactentity_contact_id=%d" % id)
	rows=cur.fetchall()
        print(len(rows))
	for i in range(len(rows)):
		contactentity_entity_id=rows[i][0]
		if (not addressesOfEntity(contactentity_entity_id)
		or not phonesOfEntity(contactentity_entity_id)
		or not emailsOfEntity(contactentity_entity_id)
		or not websitesOfEntity(contactentity_entity_id)
		or not imsOfEntity(contactentity_entity_id) ):
			return False

	print "Deleting from ContactEntity..."
	cur.execute("DELETE from ContactEntity WHERE contactentity_contact_id = %d" % id)
	cur.close()
	return True

def contactsFromAddressbook(id):
        cur = ds.cursor()
        cur.execute("SELECT contact_id FROM Contact WHERE contact_addressbook_id=%d" % id);
        rows = cur.fetchall()
	print(str(len(rows)) + " - "  + str(id))
        for i in range(len(rows)):
                contact_id=rows[i][0]
		if entitiesFromContact(contact_id)==True:
			cur.execute("DELETE FROM Contact WHERE contact_id = %d" % contact_id)

	cur.close()

def getUserID(login):
	cur=ds.cursor()
	cur.execute("SELECT userobm_id FROM UserObm WHERE userobm_login='%s'" % login);
	rows=cur.fetchall()
	if len(rows)!=1:
		return 0
	return rows[0][0]

config = ConfigParser.ConfigParser()
config.readfp(open("/etc/obm/obm_conf.ini"))
dbtype = config.get("global", "dbtype").strip()
host = config.get("global", "host").strip()
db = config.get("global", "db").strip()
user = config.get("global", "user").strip()
password = config.get("global", "password").strip(" \"")
lang = config.get("global", "lang").strip()

if dbtype == "PGSQL":
        import psycopg2 as dbapi2
        ds = dbapi2.connect(host=host, database=db, user=user, password=password)
elif dbtype == 'MYSQL':
        import MySQLdb as dbapi2
        ds = dbapi2.connect(host=host, db=db, user=user, passwd=password)
else:
        exit(1)

#
# Find user id
if len(sys.argv)!=2:
	print "Usage: %s <login>" % sys.argv[0]
	exit(1)

login=sys.argv[1]
userid=getUserID(login)
print "User %s has ID %d" % (login, userid)

addressbookEntries = getAddressbookEntries()

for i in range(len(addressbookEntries)):
        contactsFromAddressbook(addressbookEntries[i][0])

ds.commit()
ds.close()
