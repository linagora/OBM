#!/usr/bin/python
# -*- coding: utf-8 -*-

# This script will re-index all contacts & events in SOLR.
#
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

import ConfigParser
import xml.dom.minidom
import httplib
import socket
import time
from datetime import datetime

def fetch_solr_servers():
	print "INFO: Fetch solr servers"
	cur = ds.cursor()
	cur.execute("""SELECT serviceproperty_property,
		domain_id,
		host_ip,
		domain_name
		FROM Domain
		INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
		LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
		LEFT JOIN Host ON host_id = """+sql_cast("serviceproperty_value", "integer")+"""
		WHERE serviceproperty_service = 'solr'""")
	rows = cur.fetchall()
	cur.close()
	return rows


def index_contact(domain, domain_name, solr):
	global doc
	print "INFO: Contact indexing for domain "+str(domain_name)
	cur = ds.cursor()
	if dbtype == 'MYSQL':
		cur.execute("SET NAMES UTF8");
	cur.execute("""SELECT contact_id,
		"""+sql_date_format("contact_timecreate", "timecreate")+""",
		"""+sql_date_format("contact_timeupdate", "timeupdate")+""",
		contact_usercreate,
		contact_userupdate,
		contact_datasource_id,
		contact_domain_id,
		AddressBook.name as addressbook_name,
		contact_addressbook_id,
		Company.company_name as company_name,
		contact_company_id,
		contact_lastname,
		contact_firstname,
		contact_middlename,
		contact_suffix,
		contact_aka,
		kind_minilabel,
		kind_header,
		contact_manager,
		contact_assistant,
		contact_spouse,
		contact_birthday_id,
		contact_anniversary_id,
		"""+sql_date_format("e1.event_date", "birthday")+""",
		"""+sql_date_format("e2.event_date", "anniversary")+""",
		contact_category,
		contact_service,
		ContactFunction.contactfunction_label,
		contact_title,
		contact_archive,
		contact_collected,
		contact_mailing_ok,
		contact_newsletter,
		"""+sql_date_format("contact_date", "contact_date")+""",
		contact_comment,
		contact_comment2,
		contact_comment3,
		contact_origin,
		contact_company
		FROM Contact 
		LEFT JOIN AddressBook ON contact_addressbook_id=id
		LEFT JOIN Company ON contact_company_id=company_id
		LEFT JOIN Kind ON contact_kind_id=kind_id
		LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
		LEFT JOIN Event e1 ON contact_birthday_id=e1.event_id
		LEFT JOIN Event e2 ON contact_anniversary_id=e2.event_id
		WHERE contact_domain_id='"""+str(domain)+"""'""")
	rows = cur.fetchall()
	print "INFO: Add "+str(len(rows))+" contacts"
	doc = xml.dom.minidom.Document()
	add = doc.createElement('add')
	for i in range(len(rows)):
		contact = doc.createElement('doc')
		contact.appendChild(solr_set_field(doc, 'id',            rows[i][0]))
		contact.appendChild(solr_set_field(doc, 'timecreate',    solr_date_format(rows[i][1])))
		if rows[i][2] != None:
			contact.appendChild(solr_set_field(doc, 
							   'timeupdate', 
							   solr_date_format(rows[i][2])))
		contact.appendChild(solr_set_field(doc, 'usercreate',    rows[i][3]))
		contact.appendChild(solr_set_field(doc, 'userupdate',    rows[i][4]))
		contact.appendChild(solr_set_field(doc, 'datasource',    rows[i][5]))
		contact.appendChild(solr_set_field(doc, 'domain',        rows[i][6]))
		contact.appendChild(solr_set_field(doc, 'in',            rows[i][7]))
		contact.appendChild(solr_set_field(doc, 'addressbookId', rows[i][8]))
                if rows[i][9] == None:
		        contact.appendChild(solr_set_field(doc, 'company',       rows[i][38]))
                else:
		        contact.appendChild(solr_set_field(doc, 'company',       rows[i][9]))
		contact.appendChild(solr_set_field(doc, 'companyId',     rows[i][10]))
		contact.appendChild(solr_set_field(doc, 'lastname',      rows[i][11]))
		contact.appendChild(solr_set_field(doc, 'firstname',     rows[i][12]))
		if rows[i][11] == None:
			lname = ""
		else:
			lname = rows[i][11]
		if rows[i][12] == None:
			fname = ""
		else:
			fname = rows[i][12]
		contact.appendChild(solr_set_field(doc, 'sortable',      lname+" "+fname))
		contact.appendChild(solr_set_field(doc, 'middlename',    rows[i][13]))
		contact.appendChild(solr_set_field(doc, 'suffix',        rows[i][14]))
		contact.appendChild(solr_set_field(doc, 'aka',           rows[i][15]))
		contact.appendChild(solr_set_field(doc, 'kind',          rows[i][16]))
		contact.appendChild(solr_set_field(doc, 'kind',          rows[i][17]))
		contact.appendChild(solr_set_field(doc, 'manager',       rows[i][18]))
		contact.appendChild(solr_set_field(doc, 'assistant',     rows[i][19]))
		contact.appendChild(solr_set_field(doc, 'spouse',        rows[i][20]))
		contact.appendChild(solr_set_field(doc, 'birthdayId',    rows[i][21]))
		contact.appendChild(solr_set_field(doc, 'anniversaryId', rows[i][22]))
		if rows[i][23]:
			contact.appendChild(solr_set_field(doc, 'birthday',    solr_date_format(rows[i][23])))
		if rows[i][24]:
			contact.appendChild(solr_set_field(doc, 'anniversary', solr_date_format(rows[i][24])))
		contact.appendChild(solr_set_field(doc, 'category',      rows[i][25]))
		categories = contact_get_categories(rows[i][0])
		for j in range(len(categories)):
			contact.appendChild(solr_set_field(doc, 'categoryId',  categories[j][3]))
		contact.appendChild(solr_set_field(doc, 'service',       rows[i][26]))
		contact.appendChild(solr_set_field(doc, 'function',      rows[i][27]))
		contact.appendChild(solr_set_field(doc, 'title',         rows[i][28]))
		if sql_is_true(rows[i][29]):
 			contact.appendChild(solr_set_field(doc, 'is', 'archive'))
		if sql_is_true(rows[i][30]):
			contact.appendChild(solr_set_field(doc, 'is', 'collected'))
		if sql_is_true(rows[i][31]):
			contact.appendChild(solr_set_field(doc, 'is', 'mailing'))
		if sql_is_true(rows[i][32]):
			contact.appendChild(solr_set_field(doc, 'is', 'newsletter'))

                hasACalendar = contact_count_calendars(rows[i][0])[0][0] > 0
                contact.appendChild(solr_set_field(doc, 'hasACalendar', hasACalendar))
		# Coords
		coords = contact_get_coords(rows[i][0])

		phones = coords['phones']
		for p in range(len(phones)):
			contact.appendChild(solr_set_field(doc, 'phone',	phones[p]['number']))

		emails = coords['emails']
		for e in range(len(emails)):
			contact.appendChild(solr_set_field(doc, 'email',	emails[e]['address']))

		ims = coords['ims']
		for l in range(len(ims)):
			contact.appendChild(solr_set_field(doc, 'jabber',	ims[l]['address']))

		addresses = coords['addresses']
		for k in range(len(addresses)):
			contact.appendChild(solr_set_field(doc, 'street', addresses[k]['street']))
			contact.appendChild(solr_set_field(doc, 'zipcode', addresses[k]['zipcode']))
			contact.appendChild(solr_set_field(doc, 'expresspostal', addresses[k]['expresspostal']))
			contact.appendChild(solr_set_field(doc, 'town', addresses[k]['town']))
			contact.appendChild(solr_set_field(doc, 'country', addresses[k]['country']))

		if rows[i][33]:
			contact.appendChild(solr_set_field(doc, 'date',          solr_date_format(rows[i][33])))
		contact.appendChild(solr_set_field(doc, 'comment',       rows[i][34]))
		contact.appendChild(solr_set_field(doc, 'comment2',      rows[i][35]))
		contact.appendChild(solr_set_field(doc, 'comment3',      rows[i][36]))
		contact.appendChild(solr_set_field(doc, 'from',          rows[i][37]))
		add.appendChild(contact)
		doc.appendChild(add)
		solr_add_document(doc, solr, 'contact')
		add.removeChild(contact)

	solr_commit(solr, 'contact')


def index_event(domain, domain_name, solr):
	print "INFO: Event indexing for domain "+str(domain_name)
	cur = ds.cursor()
	if dbtype == 'MYSQL':
		cur.execute("SET NAMES UTF8");
	cur.execute("""SELECT 
		event_id,
		"""+sql_date_format("event_timeupdate", "timeupdate")+""",
		"""+sql_date_format("event_timecreate", "timecreate")+""",
		event_usercreate,
		event_userupdate,
		event_domain_id,
		event_title,
		event_location,
		"""+sql_date_format("event_date", "event_date")+""",
		event_duration,
		eventcategory1_id, 
		eventcategory1_label, 
		event_owner,
		event_description,
		eventtag_id,
		eventtag_label,
		event_allday,
		event_repeatkind,
		event_opacity,
		event_privacy,
		event_origin,
		"""+sql_concat(['userobm_lastname', '\' \'', 'userobm_firstname'])+""" as owner 
		FROM Event
		LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
		LEFT JOIN EventTag ON eventtag_id = event_tag_id
		INNER JOIN UserObm ON userobm_id = event_owner
		WHERE event_domain_id='"""+str(domain)+"""'""")
	rows = cur.fetchall()
	print "INFO: Add "+str(len(rows))+" events"
	doc = xml.dom.minidom.Document()
	add = doc.createElement('add')
	for i in range(len(rows)):
		event = doc.createElement('doc')
		event.appendChild(solr_set_field(doc, 'id',          rows[i][0]))
		if rows[i][1]:
			event.appendChild(solr_set_field(doc, 'timeupdate',  solr_date_format(rows[i][1])))
		event.appendChild(solr_set_field(doc, 'timecreate',  solr_date_format(rows[i][2])))
		event.appendChild(solr_set_field(doc, 'usercreate',  rows[i][3]))
		event.appendChild(solr_set_field(doc, 'userupdate',  rows[i][4]))
		event.appendChild(solr_set_field(doc, 'domain',      rows[i][5]))
		event.appendChild(solr_set_field(doc, 'title',       rows[i][6]))
		event.appendChild(solr_set_field(doc, 'location',    rows[i][7]))
		event.appendChild(solr_set_field(doc, 'date',        solr_date_format(rows[i][8])))
		event.appendChild(solr_set_field(doc, 'duration',    rows[i][9]))
		event.appendChild(solr_set_field(doc, 'category',    rows[i][10]))
		event.appendChild(solr_set_field(doc, 'category',    rows[i][11]))
		event.appendChild(solr_set_field(doc, 'ownerId',     rows[i][12]))
		event.appendChild(solr_set_field(doc, 'description', rows[i][13]))
		event.appendChild(solr_set_field(doc, 'tag',         rows[i][14]))
		event.appendChild(solr_set_field(doc, 'tag',         rows[i][15]))
		if sql_is_true(rows[i][16]):
			event.appendChild(solr_set_field(doc, 'is', 'allday'))
		if rows[i][17] != 'none':
			event.appendChild(solr_set_field(doc, 'is', 'periodic'))
		if rows[i][18] == 'OPAQUE':
			event.appendChild(solr_set_field(doc, 'is', 'busy'))
		elif rows[i][18] == 'TRANSPARENT':
			event.appendChild(solr_set_field(doc, 'is', 'free'))
		if sql_is_true(rows[i][19]):
			event.appendChild(solr_set_field(doc, 'is', 'private'))
		event.appendChild(solr_set_field(doc, 'from', rows[i][20]))
		event.appendChild(solr_set_field(doc, 'owner', rows[i][21]))
		attendees = event_get_attendees(str(rows[i][0]), domain)
		for j in range(len(attendees)):
			event.appendChild(solr_set_field(doc, 'withId', attendees[j][2]))
			event.appendChild(solr_set_field(doc, 'with', attendees[j][4]))
		add.appendChild(event)
		doc.appendChild(add)
		solr_add_document(doc, solr, 'event')
		add.removeChild(event)

	solr_commit(solr, 'event')


def sql_concat(args):
	if dbtype == 'MYSQL':
		return "CONCAT("+",".join(args)+")"	
	elif dbtype == 'PGSQL':
		return "("+"||".join(args)+")"
	else:
		print 'ERROR SQL_CONCAT'
		exit(1)


def sql_cast(field, type):
	if dbtype == 'MYSQL':
		if type == 'integer':
			return "CAST("+field+" AS UNSIGNED)"
	elif dbtype == 'PGSQL':
		if type == 'integer':
			return "CAST("+field+" AS INTEGER)"
	else:
		print 'ERROR SQL_CAST'
		exit(1)


def sql_date_format(field, name):
	if dbtype == 'MYSQL':
		return "UNIX_TIMESTAMP("+field+") AS "+name
	elif dbtype == 'PGSQL':
		return "EXTRACT (EPOCH FROM "+field+") AS "+name
	else:
		print 'ERROR SQL_DATE_FORMAT'
		exit(1)


def sql_is_true(field):
	if (dbtype == 'MYSQL') and (str(field) == '1'):
		return True
	elif (dbtype == 'PGSQL') and field:
		return True
	else:
		return False


def solr_date_format(timestamp):
	if timestamp:
		return datetime.fromtimestamp(timestamp).isoformat('T')+"Z"
	return ""


def solr_set_field(doc, name, value):
	node = doc.createElement("field")
	node.setAttribute("name", name)
	if value == None:
		value = ""
	node.appendChild(doc.createTextNode(str(value)))
	return node


def solr_add_document(doc,solr, entity):
	con = httplib.HTTPConnection(solr, 8080)
	con.request("POST", "/solr/"+entity+"/update", doc.toxml())

def solr_ping(domain_name, solr):
	for i in range(0, 10):
		try:
			con = httplib.HTTPConnection(solr, 8080)
			con.request("POST", "/solr/admin/ping")
			print "INFO: Connect to solr "+solr+" for domain "+domain_name
			return True
		except socket.error:
			print "INFO: Attempt to connect solr "+solr+" for domain "+domain_name
		time.sleep(1);
	print "ERROR: Failed to connect solr "+solr+" for domain "+domain_name
	return False;


def solr_commit(solr, entity):
	print "INFO: Solr commit"
	doc = xml.dom.minidom.Document()
	add = doc.createElement('add')
	add.appendChild(doc.createElement('commit'))
	add.appendChild(doc.createElement('optimize'))
	doc.appendChild(add)
	con = httplib.HTTPConnection(solr, 8080)
	con.request("POST", "/solr/"+entity+"/update", doc.toxml())


def event_get_attendees(event, domain):
	cur = ds.cursor()
	cur.execute("""SELECT
		event_id,
		eventlink_state,
		userentity_user_id as eventlink_entity_id,
		'user' as eventlink_entity,
		"""+sql_concat(['userobm_lastname', '\' \'', 'userobm_firstname'])+""" as eventlink_label
		FROM Event
		INNER JOIN EventLink ON eventlink_event_id = event_id
		INNER JOIN UserEntity ON userentity_entity_id = eventlink_entity_id
		INNER JOIN UserObm ON userentity_user_id = userobm_id
		WHERE event_id = '"""+event+"""' AND event_domain_id='"""+str(domain)+"""'   
		UNION
		SELECT
		event_id,
		eventlink_state,
		resourceentity_resource_id as eventlink_entity_id,
		'resource' as eventlink_entity,
		resource_name as eventlink_label
		FROM Event
		INNER JOIN EventLink ON eventlink_event_id = event_id
		INNER JOIN ResourceEntity ON resourceentity_entity_id = eventlink_entity_id
		INNER JOIN Resource ON resourceentity_resource_id = resource_id
		WHERE event_id = '"""+event+"""' AND event_domain_id='"""+str(domain)+"""'
		UNION
		SELECT
		event_id,
		eventlink_state,
		contactentity_contact_id as eventlink_entity_id,
		'contact' as eventlink_entity,
		"""+sql_concat(['contact_lastname', '\' \'', 'contact_firstname'])+""" as eventlink_label
		FROM Event
		INNER JOIN EventLink ON eventlink_event_id = event_id
		INNER JOIN ContactEntity ON contactentity_entity_id = eventlink_entity_id
		INNER JOIN Contact ON contactentity_contact_id = contact_id
		WHERE event_id = '"""+event+"""' AND event_domain_id='"""+str(domain)+"""'""")
	rows = cur.fetchall()
	return rows


def contact_get_categories(contact_id):
	cur = ds.cursor()
	cur.execute("""SELECT contactentity_contact_id, category_category, category_code, category_id, category_label 
      FROM CategoryLink
      INNER JOIN Category ON categorylink_category_id = category_id
      INNER JOIN ContactEntity ON contactentity_entity_id = categorylink_entity_id
      WHERE contactentity_contact_id='"""+str(contact_id)+"""'
      ORDER BY contactentity_contact_id, category_category, category_code, category_label""")
	rows = cur.fetchall()
	return rows

def contact_count_calendars(contact_id):
	cur = ds.cursor()
	cur.execute("""SELECT COUNT(website_entity_id)
      FROM Contact
      INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
      LEFT JOIN Website ON website_entity_id = contactentity_entity_id AND website_label LIKE 'CALURI%'
      WHERE contact_id ='"""+str(contact_id)+"""';""")
	rows = cur.fetchall()
	return rows


def contact_get_coords(contact_id):
	cur = ds.cursor()

	phones = []
	cur.execute("""SELECT contactentity_contact_id AS contact_id, phone_label, phone_number FROM Phone 
      INNER JOIN ContactEntity ON phone_entity_id = contactentity_entity_id 
      WHERE contactentity_contact_id='"""+str(contact_id)+"""' UNION 
      SELECT contact_id, 'COMPANY;PHONE' as phone_label, phone_number
      FROM Phone 
      INNER JOIN CompanyEntity ON phone_entity_id = companyentity_entity_id 
      INNER JOIN Contact ON contact_company_id = companyentity_company_id
      WHERE contact_id='"""+str(contact_id)+"""' ORDER BY phone_label""")
	rows = cur.fetchall()
	for i in range(len(rows)):
		phones.append({'label':str(rows[i][1]), 'number':str(rows[i][2])})

	emails = []
	cur.execute("""SELECT contactentity_contact_id AS contact_id, email_label, email_address FROM Email 
      INNER JOIN ContactEntity ON email_entity_id = contactentity_entity_id 
      WHERE contactentity_contact_id='"""+str(contact_id)+"""' UNION 
      SELECT contact_id, 'COMPANY;EMAIL' as email_label, email_address
      FROM Email 
      INNER JOIN CompanyEntity ON email_entity_id = companyentity_entity_id 
      INNER JOIN Contact ON contact_company_id = companyentity_company_id
      WHERE contact_id='"""+str(contact_id)+"""' ORDER BY email_label""")
	rows = cur.fetchall()
	for i in range(len(rows)):
		emails.append({'label':str(rows[i][1]), 'address':str(rows[i][2])})

	ims = []
	cur.execute("""SELECT contactentity_contact_id AS contact_id, im_protocol, im_address FROM IM 
      INNER JOIN ContactEntity ON im_entity_id = contactentity_entity_id 
      WHERE  contactentity_contact_id='"""+str(contact_id)+"""'""")
	rows = cur.fetchall()
	for i in range(len(rows)):
		ims.append({'protocol':str(rows[i][1]), 'address':str(rows[i][2])})

	addresses = []
	cur.execute("""SELECT contactentity_contact_id AS contact_id, address_label, address_street,
      address_zipcode, address_expresspostal, address_town, address_country, country_name, country_iso3166
      FROM Address 
      INNER JOIN ContactEntity ON address_entity_id = contactentity_entity_id 
      LEFT JOIN Country ON country_iso3166 = address_country AND country_lang='"""+str(lang)+"""' 
      WHERE contactentity_contact_id='"""+str(contact_id)+"""' UNION
      SELECT contact_id, 'COMPANY;ADDRESS' as address_label, address_street, address_zipcode, address_expresspostal,
        address_town, address_country, country_name, country_iso3166
      FROM Address
      INNER JOIN CompanyEntity ON address_entity_id = companyentity_entity_id 
      INNER JOIN Contact ON contact_company_id = companyentity_company_id
      LEFT JOIN Country ON country_iso3166 = address_country
      AND country_lang = '"""+str(lang)+"""'
      WHERE contact_id='"""+str(contact_id)+"""' ORDER BY address_label""")
	rows = cur.fetchall()
	for i in range(len(rows)):
		addresses.append({'label':str(rows[i][1]), 'street':str(rows[i][2]), 'zipcode':str(rows[i][3]), 
'expresspostal':str(rows[i][4]), 'town':str(rows[i][5]), 'country':str(rows[i][7]), 'country_iso3166':str(rows[i][8])})

	coords = {'phones':phones, 'emails':emails, 'addresses':addresses, 'ims':ims}
	return coords


def init_obm_index(domain, domain_name, solr, entity):
	if entity == "contact":
		index_contact(domain, domain_name, solr)
	elif entity == "event":
		index_event(domain, domain_name, solr)
	else:
		print "ERROR: Unrecognised entity: "+entity


global lang
global ds
global dbtype

config = ConfigParser.ConfigParser()
config.readfp(open("/etc/obm/obm_conf.ini"))
dbtype = config.get("global", "dbtype").strip()
host = config.get("global", "host").strip()
db = config.get("global", "db").strip()
user = config.get("global", "user").strip()
password = config.get("global", "password").strip(" \"")
lang = config.get("global", "lang").strip()

ds = None
if dbtype == "PGSQL":
	import psycopg2 as dbapi2
	ds = dbapi2.connect(host=host, database=db, user=user, password=password)
elif dbtype == 'MYSQL':
	import MySQLdb as dbapi2
	ds = dbapi2.connect(host=host, db=db, user=user, passwd=password)
else:
	exit(1)

solr_servers = fetch_solr_servers()
for i in range(len(solr_servers)):
	entity = solr_servers[i][0]
	domain = solr_servers[i][1]
	solr = solr_servers[i][2]
	domain_name = solr_servers[i][3]
	if solr_ping(domain_name, solr):
		init_obm_index(domain, domain_name, solr, entity)

ds.close()
