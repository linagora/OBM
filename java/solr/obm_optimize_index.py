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
import httplib
import socket

def fetch_solr_servers():
	print "INFO: Fetching solr servers"
	
	cur = ds.cursor()
	cur.execute("""SELECT serviceproperty_property,
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


def solr_ping(domain_name, solr):
	for i in range(0, 10):
		try:
			con = httplib.HTTPConnection(solr, 8080)
			con.request("POST", "/solr/admin/ping")

			print "INFO: Connecting to solr " + solr + " for domain " + domain_name

			return True
		except socket.error:
			print "INFO: Attempt to connect solr " + solr + " for domain " + domain_name

		time.sleep(1);

	print "ERROR: Failed to connect solr " + solr + " for domain " + domain_name

	return False;


def optimize(solr, entity):
	print "INFO: Optimizing index '" + entity + "' @" + solr
	
	con = httplib.HTTPConnection(solr, 8080)
	con.request("GET", "/solr/" + entity + "/update?optimize=true")

	res = con.getresponse()
	res.read();

	print "INFO: Optimization completed with result %d/%s" % (res.status, res.reason)


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
	solr = solr_servers[i][1]
	domain_name = solr_servers[i][2]

	if solr_ping(domain_name, solr):
		optimize(solr, entity)

ds.close()
