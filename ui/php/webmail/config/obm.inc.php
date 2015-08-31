<?php

// Ugly hack for DB_OBM
$GLOBALS['obmdb_host'] = $obmdb_host;
$GLOBALS['obmdb_user'] = $obmdb_user;
$GLOBALS['obmdb_password'] = $obmdb_password;
$GLOBALS['obmdb_db'] = $obmdb_db;
$GLOBALS['obmdb_dbtype'] = $obmdb_dbtype;

$query = "SELECT
		    domain_name, serviceproperty_property, host_ip
		    FROM Domain
		    INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
		    LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
		    LEFT JOIN Host ON host_id = #CAST(serviceproperty_value, INTEGER)
		    WHERE
		    serviceproperty_property = 'imap_frontend'
            OR serviceproperty_property = 'smtp_out'
            OR serviceproperty_property = 'obm_sync'";
$obm_q = new DB_OBM;
$obm_q->query($query);

$config['default_host'] = array();
$config['multiple_smtp_server'] = array();

while ($obm_q->next_record()) {
	if ($obm_q->f('serviceproperty_property') == 'imap_frontend') {
		$config['default_host'][$obm_q->f('host_ip')] = $obm_q->f('domain_name');
	} else if ($obm_q->f('serviceproperty_property') == 'smtp_out') {
		$config['multiple_smtp_server'][$obm_q->f('domain_name')] = $obm_q->f('host_ip');
	} else if ($obm_q->f('serviceproperty_property') == 'obm_sync') {
      $config["obmSyncIp"] = $obm_q->f('host_ip');
    }
}

// Use this folder to store log files (must be writeable for apache user)
$config['log_dir'] = '/var/log/webmail/';

// Store spam messages in this mailbox
$config['junk_mbox'] = 'SPAM';

// Display these folders separately in the mailbox list.
// These folders will also be displayed with localized names
$config['default_folders'] = array('INBOX', 'Drafts', 'Sent', 'SPAM', 'Trash');

// Automatically create the above listed default folders on first login
$config['create_default_folders'] = true;

// Lifetime of message cache. Possible units: s, m, h, d, w
$config['message_cache_lifetime'] = '10d';

// authenticate to the SMTP server
$config['smtp_user'] = '%u';
$config['smtp_pass'] = '%p';

// This key is used to encrypt the users imap password which is stored
// in the session record (and the client cookie if remember password is enabled).
// Please provide a string of exactly 24 chars.
$config['des_key'] = 'NIZLhTml&d$sl=g=AHPfi7Jx';

// compose in new window
$config['compose_extwin'] = true;

// session name.
$config['session_name'] = 'roundcube_obm_sessid';
// session lifetime
if ( $cs_lifetime ) {
  $rcSessionLifetime = round($cs_lifetime/60) + 1;
} else {
  // session life cookie
  $rcSessionLifetime = 10;
}
$config['session_lifetime'] = $rcSessionLifetime;

// setup required OBM modules
$config["plugins"][] = "multiple_smtp_server";
$config["plugins"][] = "obm_addressbook";
$config["plugins"][] = "obm_identities";
$config["plugins"][] = "obm_unread";
$config["plugins"][] = "obm_securetoken";

if ( $auth_kind && $auth_kind == "CAS" ) {
  $config["plugins"][] = "obm_cas_authn";
  $config["obmAuthType"] = "LemonLDAP";
  // CAS plugin configuration
  set_include_path(get_include_path() . PATH_SEPARATOR . "../../obminclude/lib/CAS");
  // force CAS authentication (doesn't fallback to roundcube db auth if CAS fails)
  $config['cas_force'] = true;
  // CAS in proxy mode
  $config['cas_proxy'] = true;
  // Cache CAS Proxy Tickets (this works if the cyrus saslauthd use the -c flag
  $config['cas_imap_caching'] = true;
  // CAS server host name.
  $config['cas_hostname'] = $cas_server;
  // CAS server port number.
  $config['cas_port'] = $cas_server_port;
  // CAS service URI on the CAS server.
  $config['cas_uri'] = $cas_server_uri;

  $config['cas_pgt_dir'] = '/tmp';
  $config['cas_login_url'] = '';
  $config['cas_logout_url'] = '';
  $config['cas_imap_name'] = 'imap://myimap.obm';
  $config['username_domain'] = 'mydomain.obm';
  $config['username_domain_forced'] = false;

  // CAS validation type
  if ( $cas_validation ) {
    $config['cas_validation'] = $cas_validation;
    $config['cas_cert'] = $cas_cert;
  }
  $config['cas_server_login_attribute'] = $cas_server_login_attribute;
} else if ( $auth_kind && $auth_kind == "LemonLDAP") {
  $config["plugins"][] = "http_authentication";
  $config["obmAuthType"] = "LemonLDAP";
} else {
  $config["plugins"][] = "obm_auth";
}

// Enable the html editor by default
$config['htmleditor'] = 1;

// Default LDAP addressbook configuration
$config['address_book_type'] = array('sql', 'ldap');
$config['autocomplete_addressbooks'] = array('sql', 'obm');

$config['ldap_public'] ['obm'] = array(
  'name'          => 'OBM',
  'hosts'         => array('127.0.0.1'),
  'port'          => 389,
  'use_tls'       => false,
  'user_specific' => false,
  'base_dn'       => 'dc=local',
  'bind_dn'       => '',
  'bind_pass'     => '',
  'search_base_dn' => '',
  'search_filter'  => '(&(objectClass=posixAccount)(uid=%u))',
  'writable'      => false,
  'ldap_version'  => 3,
  'search_fields' => array('mail', 'cn'),
  'name_field'    => 'cn',
  'hidden'        => false,
  'email_field'   => 'mail',
  'surname_field' => 'sn',
  'firstname_field' => 'gn',
  'sort'          => 'cn',
  'scope'         => 'sub',
  'filter'        => '(&(mail=*)(|(objectClass=obmUser)(objectClass=obmGroup)(objectClass=obmMailShare)))',
  'fuzzy_search'  => true,
  'sizelimit'     => '0',
  'timelimit'     => '0'
);

/**
 * PLEASE KEEP THIS AT THE END OF THIS FILE
 *
 * Include external configuration file if it exists
 */
$external_config_file = "/etc/obm/webmail.inc.php";
if (is_readable($external_config_file)) {
  require_once($external_config_file);
}
