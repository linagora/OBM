<?php

// This queries the OBM database to retrieve the available domains and associated mail servers.
// This then exposes the rcmail_x variables with the proper information.

$path = '..';
$module = "webmail";
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') {
	$obminclude = 'obminclude';
}
require_once("$obminclude/global.inc");

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

$rcmail_config['default_host'] = array();
$rcmail_config['multiple_smtp_server'] = array();

while ($obm_q->next_record()) {
	if ($obm_q->f('serviceproperty_property') == 'imap_frontend') {
		$rcmail_config['default_host'][$obm_q->f('host_ip')] = $obm_q->f('domain_name');
	} else if ($obm_q->f('serviceproperty_property') == 'smtp_out') {
		$rcmail_config['multiple_smtp_server'][$obm_q->f('domain_name')] = $obm_q->f('host_ip');
	} else if ($obm_q->f('serviceproperty_property') == 'obm_sync') {
      $rcmail_config["obmSyncIp"] = $obm_q->f('host_ip');
    }
}

// authenticate to the SMTP server
$rcmail_config['smtp_user'] = '%u';
$rcmail_config['smtp_pass'] = '%p';

// setup a DES key
$rcmail_config['des_key'] = 'NIZLhTml&d$sl=g=AHPfi7Jx';

// compose in new window
$rcmail_config['compose_extwin'] = true;

// session name.
$rcmail_config['session_name'] = 'roundcube_obm_sessid';
// session lifetime
if ( $cs_lifetime ) {
  $rcSessionLifetime = round($cs_lifetime/60) + 1;
} else {
  // session life cookie
  $rcSessionLifetime = 0;
}
$rcmail_config['session_lifetime'] = $rcSessionLifetime;




// setup required OBM modules
$rcmail_config["plugins"][] = "multiple_smtp_server";
$rcmail_config["plugins"][] = "obm_addressbook";
$rcmail_config["plugins"][] = "unread";
$rcmail_config["pluguns"][] = "obm_identities";


if ( $auth_kind && $auth_kind == "CAS" ) {
  $rcmail_config["plugins"][] = "cas_authn";
  // CAS plugin configuration
  set_include_path(get_include_path() . PATH_SEPARATOR . "../../obminclude/lib/CAS");
  // force CAS authentication (doesn't fallback to roundcube db auth if CAS fails)
  $rcmail_config['cas_force'] = true;
  // CAS in proxy mode
  $rcmail_config['cas_proxy'] = true;
  // Cache CAS Proxy Tickets (this works if the cyrus saslauthd use the -c flag
  $rcmail_config['cas_imap_caching'] = true;
  // CAS server host name.
  $rcmail_config['cas_hostname'] = $cas_server;
  // CAS server port number.
  $rcmail_config['cas_port'] = $cas_server_port;
  // CAS service URI on the CAS server.
  $rcmail_config['cas_uri'] = $cas_server_uri;
  // CAS validation type
  if ( $cas_validation ) {
    $rcmail_config['cas_validation'] = $cas_validation;
    $rcmail_config['cas_cert'] = $cas_cert;
  }
} else if ( $auth_kind && $auth_kind == "LemonLDAP") {
  $rcmail_config["plugins"][] = "http_authentication";
} else {
  $rcmail_config["plugins"][] = "obm_auth";
}

// Enable the html editor by default
$rcmail_config['htmleditor'] = 1;


// include external configuration file if it exists
$external_config_file = "/etc/obm/webmail/main.inc.php";
if ( file_exists($external_config_file) && is_readable($external_config_file) ) {
  require_once($external_config_file);
}

