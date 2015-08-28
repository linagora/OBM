<?php

global $obmdb_dbtype, $obmdb_host, $obmdb_user, $obmdb_password, $obmdb_db;

$path = '..';
$module = "webmail";
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') {
  $obminclude = 'obminclude';
}
require_once("$obminclude/global.inc");

// OBM specific configuration
require_once("config/obm.inc.php");

// PEAR database DSN for read/write operations
// Format is db_provider://user:password@host/database
$config['db_dsnw'] = strtolower($obmdb_dbtype) . '://' . $obmdb_user . ':' . $obmdb_password . '@' . $obmdb_host . '/'.$obmdb_db;
$config['db_prefix'] = 'rc_';