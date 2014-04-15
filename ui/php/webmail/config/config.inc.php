<?php

global $obmdb_dbtype, $obmdb_host, $obmdb_user, $obmdb_password, $obmdb_db;

/* Local configuration for Roundcube Webmail */

// PEAR database DSN for read/write operations
// format is db_provider://user:password@host/database 
// For examples see http://pear.php.net/manual/en/package.database.mdb2.intro-dsn.php
// currently supported db_providers: mysql, mysqli, pgsql, sqlite, mssql or sqlsrv
// $config['db_dsnw'] = '://:@/';

$config['db_dsnw'] = strtolower($obmdb_dbtype) . '://' . $obmdb_user . ':' . $obmdb_password . '@' . $obmdb_host . '/'.$obmdb_db;
$config['db_prefix'] = 'rc_';

// ----------------------------------
// IMAP
// ----------------------------------
// The mail host chosen to perform the log-in.
// Leave blank to show a textbox at login, give a list of hosts
// to display a pulldown menu or set one host as string.
// To use SSL/TLS connection, enter hostname with prefix ssl:// or tls://
// Supported replacement variables:
// %n - hostname ($_SERVER['SERVER_NAME'])
// %t - hostname without the first part
// %d - domain (http hostname $_SERVER['HTTP_HOST'] without the first part)
// %s - domain name after the '@' from e-mail address provided at login screen
// For example %n = mail.domain.tld, %t = domain.tld
// WARNING: After hostname change update of mail_host column in users table is
//          required to match old user data records with the new host.
//$config['default_host'] = '';

// provide an URL where a user can get support for this Roundcube installation
// PLEASE DO NOT LINK TO THE ROUNDCUBE.NET WEBSITE HERE!
$config['support_url'] = '';

// use this folder to store log files (must be writeable for apache user)
// This is used by the 'file' log driver.
$config['log_dir'] = 'logs/';

// use this folder to store temp files (must be writeable for apache user)
$config['temp_dir'] = 'temp/';

// this key is used to encrypt the users imap password which is stored
// in the session record (and the client cookie if remember password is enabled).
// please provide a string of exactly 24 chars.
$config['des_key'] = 'j8+HMm&Y*zHO=*S%i67+hFVY';

// ----------------------------------
// PLUGINS
// ----------------------------------
// List of active plugins (in plugins/ directory)
//$config['plugins'] = array();

// store spam messages in this mailbox
// NOTE: Use folder names with namespace prefix (INBOX. on Courier-IMAP)
$config['junk_mbox'] = 'SPAM';

// display these folders separately in the mailbox list.
// these folders will also be displayed with localized names
// NOTE: Use folder names with namespace prefix (INBOX. on Courier-IMAP)
$config['default_folders'] = array('INBOX', 'Drafts', 'Sent', 'SPAM', 'Trash');

// automatically create the above listed default folders on first login
$config['create_default_folders'] = true;

// lifetime of message cache
// possible units: s, m, h, d, w
$config['message_cache_lifetime'] = '10d';