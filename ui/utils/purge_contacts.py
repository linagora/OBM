#!/usr/bin/python
# -*- coding: utf-8 -*-

# Depends: python-psycopg2 for postgresql or python-mysqldb for mysql.

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
