<?php

/*
 +-----------------------------------------------------------------------+
 | Configuration file for database access                                |
 |                                                                       |
 | This file is part of the Roundcube Webmail client                     |
 | Copyright (C) 2005-2009, The Roundcube Dev Team                       |
 |                                                                       |
 | Licensed under the GNU General Public License version 3 or            |
 | any later version with exceptions for skins & plugins.                |
 | See the README file for a full license statement.                     |
 |                                                                       |
 +-----------------------------------------------------------------------+

*/

// OBM SPECIFIC

global $obmdb_dbtype, $obmdb_host, $obmdb_user, $obmdb_password, $obmdb_db;

// OBM SPECIFIC

$rcmail_config = array();

// PEAR database DSN for read/write operations
// format is db_provider://user:password@host/database 
// For examples see http://pear.php.net/manual/en/package.database.mdb2.intro-dsn.php
// currently supported db_providers: mysql, mysqli, pgsql, sqlite, mssql or sqlsrv

$rcmail_config['db_dsnw'] = strtolower($obmdb_dbtype) . '://' . $obmdb_user . ':' . $obmdb_password . '@' . $obmdb_host . '/'.$obmdb_db;
// postgres example: 'pgsql://roundcube:pass@localhost/roundcubemail';
// Warning: for SQLite use absolute path in DSN:
// sqlite example: 'sqlite:////full/path/to/sqlite.db?mode=0646';

// PEAR database DSN for read only operations (if empty write database will be used)
// useful for database replication
$rcmail_config['db_dsnr'] = '';

// maximum length of a query in bytes
$rcmail_config['db_max_length'] = 512000;  // 500K

// use persistent db-connections
// beware this will not "always" work as expected
// see: http://www.php.net/manual/en/features.persistent-connections.php
$rcmail_config['db_persistent'] = FALSE;


// you can define specific table names used to store webmail data
$rcmail_config['db_table_users'] = 'rc_users';
$rcmail_config['db_table_identities'] = 'rc_identities';
$rcmail_config['db_table_contacts'] = 'rc_contacts';
$rcmail_config['db_table_contactgroups'] = 'rc_contactgroups';
$rcmail_config['db_table_contactgroupmembers'] = 'rc_contactgroupmembers';
$rcmail_config['db_table_session'] = 'rc_session';
$rcmail_config['db_table_cache'] = 'rc_cache';
$rcmail_config['db_table_cache_index'] = 'rc_cache_index';
$rcmail_config['db_table_cache_thread'] = 'rc_cache_thread';
$rcmail_config['db_table_cache_messages'] = 'rc_cache_messages';


// you can define specific sequence names used in PostgreSQL
$rcmail_config['db_sequence_users'] = 'rc_user_ids';
$rcmail_config['db_sequence_identities'] = 'rc_identity_ids';
$rcmail_config['db_sequence_contacts'] = 'rc_contact_ids';
$rcmail_config['db_sequence_contactgroups'] = 'rc_contactgroups_ids';
$rcmail_config['db_sequence_cache'] = 'rc_cache_ids';
$rcmail_config['db_sequence_searches'] = 'rc_search_ids';


// end db config file
