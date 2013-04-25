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
error_log($query);
$obm_q = new DB_OBM;
$obm_q->query($query);

$rcmail_config = array();
$rcmail_config['default_host'] = array();
$rcmail_config['multiple_smtp_server'] = array();

while ($obm_q->next_record()) {
	if ($obm_q->f('serviceproperty_property') == 'imap_frontend') {
error_log("config: found iamp frontend ".$obm_q->f('host_ip')." ".$obm_q->f('domain_name'));
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

// setup required OBM modules
$rcmail_config["plugins"][] = "multiple_smtp_server";
//$rcmail_config["plugins"][] = "obm_addressbook";


if ( $auth_kind && $auth_kind == "CAS" ) {
  $rcmail_config["plugins"][] = "cas_authn";
}



// include external configuration file if it exists
$external_config_file = "/etc/obm/webmail/main.inc.php";
if ( file_exists($external_config_file) && is_readable($external_config_file) ) {
  require_once($external_config_file);
}

