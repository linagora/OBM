<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : domain_query.inc                                             //
//     - Desc : domain query & db File                                       //
// 2000-05-17 Aliacom - David Phan                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// domain query search query execution
// Parameters:
//   - $domain[] : domain search criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_search($domain, $restriction='') {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $domain['sql_order_dir'];
  $sql_order_field = $domain['sql_order_field'];
  $text = sql_search_text_parse($domain['text']);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $where = '';

  // If a domain label has been specified, get it
  if (trim($text) != '') {
    $where .= " domain_label $like '$text%'
      OR domain_description like '$text%'";
  }

  //if ((trim($where) != '') || (trim($where_restriction) != '')) {
  if (trim($where) != '') {
    //$whereq = "AND $where $where_restriction";
    $whereq = "AND $where";
  }

  $select = "SELECT distinct Domain.*,
    domain_id as id
    FROM Domain WHERE domain_global = false $whereq";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'domain_label';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    if (trim($text) != '') {
      $count = get_query_count("SELECT count(distinct domain_id) FROM Domain WHERE $where");
    } else {
      $count = get_query_count("SELECT count(distinct domain_id) FROM Domain");
    }
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_domain_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// domain detail query execution
// Parameters:
//   - $p_id : domain id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $timeupdate = sql_date_format($db_type, 'domain_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'domain_timecreate', 'timecreate');
  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT Domain.*,
    $timeupdate,
    $timecreate,
    c.userobm_login as usercreate,
    u.userobm_login as userupdate,
    serviceproperty_value as domain_mail_server_auto
    FROM Domain
    INNER JOIN DomainEntity ON domain_id = domainentity_domain_id
    LEFT JOIN ServiceProperty ON domainentity_entity_id = serviceproperty_entity_id AND serviceproperty_property = 'imap_autoselect'
    LEFT JOIN UserObm as c ON domain_usercreate=c.userobm_id
    LEFT JOIN UserObm as u ON domain_userupdate=u.userobm_id
    WHERE domain_id $p_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// domain insert query execution
// Parameters:
//   - $domain[]   : entry values
// Returns:
//   $id : new domain id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_insert($domain) {
  global $obm, $cdg_sql, $cgp_use;

  $uid = sql_parse_id($obm['uid']);
  $label = $domain['label'];
  $desc = $domain['desc'];
  $domain_name = $domain['domain_name'];
  $alias = $domain['alias'];
  $mail_server_auto = $domain['mail_server_auto'];
  $samba = $domain['samba'];

  $query = "INSERT INTO Domain (
    domain_timeupdate,
    domain_timecreate,
    domain_userupdate,
    domain_usercreate,
    domain_label,
    domain_description,
    domain_name,
    domain_alias)
    VALUES (
      null,
      NOW(),
      null,
      $uid,
      '$label',
      '$desc',
      '$domain_name',
      '$alias')";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_insert(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $domain_id = $obm_q->lastid();
    $entity_id = of_entity_insert($GLOBALS['module'], $domain_id);    
    if ($cgp_use['service']['mail']) {
      of_service_insert('mail', $entity_id);
      of_service_property_insert('mail','imap_autoselect', $entity_id, $mail_server_auto);
      of_service_property_insert('mail','imap', $entity_id, $domain['imap']);
      of_service_property_insert('mail','smtp_in', $entity_id, $domain['smtp_in']);
      of_service_property_insert('mail','smtp_out', $entity_id, $domain['smtp_out']);
    }
    if ($cgp_use['service']['samba']) {
      if(is_array($samba)) {
        of_service_insert('samba', $entity_id);
        foreach($samba as $property => $value) {
          of_service_property_insert('samba', $property, $entity_id, addslashes($value));
        }
      }
    } 

    // Init default domain groups
    if ($samba['domain'] != '') {
      run_query_domain_init_groups($domain_id, true);
    } else {
      run_query_domain_init_groups($domain_id, false);
    }

    // Init domain properties
    run_query_domain_replace_properties($domain_id, $domain);

    // Init domain reference data
    run_query_domain_init_data($domain_id);
  } else {
    $domain_id = false;
  }

  return $domain_id;
}


///////////////////////////////////////////////////////////////////////////////
// domain insert query execution
// Parameters:
//   - $domain[]   : entry values
// Returns:
//   $id : new domain id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_replace_properties($domain_id,$domain) {
  global $cdg_sql;

  $d_id = sql_parse_id($domain_id, true);
  $query = "DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_domain_id $d_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_replace_properties()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $prop_q = run_query_domain_properties();
  while($prop_q->next_record()) {
    $key = $prop_q->f('domainproperty_key');
    $value = $domain[$key];

    if ($value == '') {
      $value = $prop_q->f('domainproperty_default');
    }

    if ($value != '') {
      $domain_id = sql_parse_id($domain_id);
      $query = "INSERT INTO DomainPropertyValue (
        domainpropertyvalue_domain_id,
        domainpropertyvalue_property_key,
        domainpropertyvalue_value)
        VALUES (
          $domain_id,
          '$key',
          '$value'
        )";
      $obm_q->query($query);
    }
  }
}


/**
 * Get default data from global domain and make a copy into the new domain
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_domain_init_data($id) {
  global $obm, $cdg_sql;

  $db = new DB_OBM;
  $tables = array ('CompanyNafCode', 'CompanyActivity', 'CompanyType', 'Kind', 'DealType',
                   'DealStatus', 'EventCategory1', 'DocumentMimeType', 'TaskType', 'ContractStatus',
                   'ContractPriority', 'IncidentStatus', 'IncidentPriority', 'PaymentKind');
  $query = "INSERT INTO %s (%s) SELECT %s FROM %s JOIN Domain ON %s = domain_id WHERE domain_global = true GROUP BY %s";
  foreach ($tables as $table ) {
    $dynamicfields = array();
    $staticfields = array();
    $domain_field = null;
    $fields = $db->metadata($table, false);
    foreach ($fields as $field) {
      if (strpos(strtolower($field['flags']),'primary') === false) {
        if (strpos(strtolower($field['name']),'domain_id') !== false) {
          $staticfields[$field['name']] = $id;
          $domain_field = $field['name'];
        } elseif (strpos(strtolower($field['name']),'timeupdate') !== false) {
          $staticfields[$field['name']] = 'NULL';
        } elseif (strpos(strtolower($field['name']),'timecreate') !== false) {
          $staticfields[$field['name']] = 'NOW()';
        } elseif (strpos(strtolower($field['name']),'userupdate') !== false) {
          $staticfields[$field['name']] = 'NULL';
        } elseif (strpos(strtolower($field['name']),'usercreate') !== false) {
          $staticfields[$field['name']] = $obm['uid'];
        } else {
          $dynamicfields[] = $field['name'];
        }
      }
    }
    $squery = sprintf($query, $table, implode(', ',array_merge($dynamicfields,array_keys($staticfields))), 
                                      implode(', ', array_merge($dynamicfields, array_values($staticfields))),
                                      $table, $domain_field, implode(', ', $dynamicfields));
    display_debug_msg($squery, $cdg_sql, 'run_query_domain_init_data()');
    $db->query($squery);
  }
}


/**
 * Initialize default windows and user Group for a domain
 * 
 * @param int $domain_id domain id
 * @param boolean $samba
 * @access public
 * @return void
 */
function run_query_domain_init_groups($domain_id, $windows=false) {
  global $obm, $cdg_sql;
  global $cg_gid_smb_admin, $cg_gid_smb_user, $cg_gid_smb_guest;
  global $l_gid_smb_admin, $l_gid_smb_user, $l_gid_smb_guest;
  global $cg_gid_user, $l_gid_user;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm['uid']);

  if ($windows) {
    $gids = array($cg_gid_smb_admin => array($l_gid_smb_admin, 1),
		  $cg_gid_smb_user => array($l_gid_smb_user, 1),
		  $cg_gid_smb_guest => array($l_gid_smb_guest, 1),
		  $cg_gid_user => array($l_gid_user, 0));
  } else {
    $gids = array($cg_gid_user => array($l_gid_user, 0));
  }

  foreach ($gids as $gid => $group_infos) {
    $gname = $group_infos[0];
    $samba = $group_infos[1];
    if (! check_domain_group_exists($gid, $domain_id)) { 
      $query = "INSERT INTO UGroup (group_domain_id,
        group_usercreate,
        group_timecreate,
        group_samba,
        group_gid,
        group_name)
      VALUES (
        $domain_id,
        $uid,
        NOW(),
        $samba,
        $gid,
        '$gname')";
      display_debug_msg($query, $cdg_sql, 'run_query_domain_init_groups(1)');
      $obm_q->query($query);

      $multidomain = sql_multidomain('group');

      $query = "SELECT MAX(group_id) as  group_id
        FROM UGroup
        WHERE group_name='$gname'
        $multidomain";

      display_debug_msg($query, $cdg_sql, 'run_query_domain_init_groups(2)');
      
      $obm_q->query($query);
      if ($obm_q->num_rows() > 0) {
        $obm_q->next_record();
        $id = $obm_q->f('group_id');
        $entity_id = of_entity_insert('group', $id);  
      }
    }
  }
}


/**
 * Check if a given group (bu gid) already exists in the domain
 * 
 * @param int $gid group gid
 * @param int $domain_id
 * @access public
 * @return void
 */
function check_domain_group_exists($gid, $domain_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('group');

  $gid = sql_parse_id($gid, true);
  $query = "SELECT group_id, group_name
    FROM UGroup
    WHERE group_gid $gid
      AND group_domain_id='$domain_id'";
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'check_domain_group_exists()');
  $obm_q->query($query);
  $nb = $obm_q->num_rows();

  if ($nb >= 1) {
    return true;
  }
  return false;
}


///////////////////////////////////////////////////////////////////////////////
// domain update query execution
// Parameters:
//   - $domain_id : domain id
//   - $domain[]  : entry values
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_update($domain_id, $domain) {
  global $obm, $cdg_sql, $cgp_use;

  $cur_samba = run_query_domain_samba_properties($domain_id);

  $uid = sql_parse_id($obm['uid']);
  $label = $domain['label'];
  $desc = $domain['desc'];
  $alias = $domain['alias'];
  $mail_server_auto = sql_parse_id($domain['mail_server_auto']);
  $did = sql_parse_id($domain_id, true);
  $samba = $domain['samba'];

  $query = "UPDATE Domain SET
    domain_timeupdate=NOW(),
    domain_userupdate=$uid,
    domain_label='$label',
    domain_description='$desc',
    domain_alias='$alias'
    WHERE domain_id $did ";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $entity_id = of_entity_get($GLOBALS['module'], $domain_id);
  if ($cgp_use['service']['mail']) {
    of_service_property_replace('mail','imap_autoselect', $entity_id, $mail_server_auto);
    of_service_property_replace('mail','imap',$entity_id , $domain['imap']);
    of_service_property_replace('mail','smtp_in', $entity_id, $domain['smtp_in']);
    of_service_property_replace('mail','smtp_out', $entity_id, $domain['smtp_out']);
  }  

  if ($cgp_use['service']['samba']) {
    if(is_array($samba)) {
      foreach($samba as $property => $value) {
        of_service_property_replace('samba', $property, $entity_id, addslashes($value));
      }
    } else {
      of_service_delete('samba', $entity_id);
    }
  }

  // Init default domain groups
  if ($samba['domain'] != '') {
    run_query_domain_init_groups($domain_id, true);
  } else {
    run_query_domain_init_groups($domain_id, false);
  }

  // Purge samba infos if windows property just removed
  if ($samba['domain'] == '') {
    if ($cur_samba['domain'] != '') {

      // Purge samba perms from users
      $query = "UPDATE UserObm
        SET userobm_samba_perms = 0
        WHERE userobm_domain_id $did";
      display_debug_msg($query, $cdg_sql, 'run_query_domain_update(purge samba users)');
      $obm_q->query($query);

      // Purge samba perms from groups
      $query = "UPDATE UGroup
        SET group_samba = 0
        WHERE group_domain_id $did";
      display_debug_msg($query, $cdg_sql, 'run_query_domain_update(purge samba groups)');
      $obm_q->query($query);
    }
  }

  // Init domain properties
  run_query_domain_replace_properties($domain_id, $domain);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// domain delete query execution
// Parameters:
//   - $p_id : domain id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_delete($p_id) {
  global $cdg_sql, $c_use_connectors;
  
  of_entity_delete($GLOBALS['module'], $p_id);

  $obm_q = new DB_OBM;
  $p_id = sql_parse_id($p_id, true);
  $query = "DELETE FROM Domain WHERE domain_id $p_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_delete()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $d_id     : domain id
//   - $user[]   : values checked
//     keys used : name, passwd, email
///////////////////////////////////////////////////////////////////////////////
function check_domain_data_form($d_id, $domain) {
  global $err, $l_label_error,$l_alias_user_used,$l_name_in_use;
  global $l_name_error, $cgp_use;

  $label = $domain['label'];
  $alias = trim($domain['alias']);
  $name = trim($domain['domain_name']);
  $samba = $domain['samba'];
  $id = $domain['domain_id'];

  // MANDATORY: Label
  if (trim($label) == '') {
    $err['msg'] = $l_label_error.' : '. $label;
    return false;
  }
  if (trim($name) == '' || !preg_match($GLOBALS['php_regexp_domain'], $name)) {
    $err['msg'] = $l_name_error.' : '. $name;
    return false;
  }
  $used_name = run_query_domain_get_used_name($alias, $name, $d_id);
  if (count($used_name) > 0) {
    $err['msg'] = "$l_name_in_use : ".implode(', ',$used_name);
    return false;
  }
  if ($d_id && !empty($alias)) {
    $users_aliases = run_query_domain_get_users_aliases($alias, $d_id);
    if (count($users_aliases) > 0) {
      $err['msg'] = "$l_alias_user_used : ".implode(', ',$users_aliases);
      return false;
    }
  }
  if ($cgp_use['service']['samba']) {
    if ($samba['domain'] != '') {
      if (!preg_match($GLOBALS['php_regexp_samba_domain'],$samba['domain'])) {
	$err['msg'] = "$GLOBALS[l_samba_domain_error] : $samba[domain]";
	return false;
      }
      if (!preg_match($GLOBALS['php_regexp_samba_sid'],$samba['sid'])) {
	$err['msg'] = "$GLOBALS[l_samba_sid_error] : $samba[sid]";
	return false;
      }
      if (!of_service_property_unique('samba', 'sid', $samba['sid'], $id)) {
	$err['msg'] = "$GLOBALS[l_samba_sid_used] : $samba[sid]";
	return false;
      }
      if (!of_service_property_unique('samba', 'domain', $samba['domain'], $id)) {
	$err['msg'] = "$GLOBALS[l_samba_domain_used] : $samba[domain]";
	return false;
      }    
      if ($samba['profile'] != '' && !preg_match($GLOBALS['php_regexp_samba_profile'],$samba['profile'])) {
	$err['msg'] = "$GLOBALS[l_samba_profile_error] : $samba[profile]";
	return false;
      }
      if ($samba['home'] != '' && !preg_match($GLOBALS['php_regexp_samba_home'],$samba['home'])) {
	$err['msg'] = "$GLOBALS[l_samba_home_error] : $samba[home]";
	return false;
      }
      if ($samba['drive'] != '' && !preg_match($GLOBALS['php_regexp_samba_drive'],$samba['drive'])) {
	$err['msg'] = "$GLOBALS[l_samba_drive_error] : $samba[drive]";
	return false;
      }
    } else {
      // If windows domain name not filled, windows fields must be blank
      if (($samba['sid'] != '')
	  || ($samba['profile'] != '')
	  || ($samba['home'] != '')
	  || ($samba['drive'] != '')) {
	$err['msg'] = "$GLOBALS[l_samba_domain_error] : $samba[domain]";
	return false;
      }
    }
  }
  return true;
}


/**
 * Check if aliases or domain name  are already in use
 * 
 * @param mixed $aliases 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_domain_get_used_name($alias, $name=null, $id=null) {
  global $cdg_sql;

  $return = array();
  $aliases = array();
  if (!empty($alias)) {
    $aliases = explode("\r\n",$alias);
  }
  if (!empty($id)) {
    $d = of_domain_get_domain_infos($id);
    array_push($aliases,$d['name']);
    $id = sql_parse_id($id, true, true);
    $dquery = " AND domain_id $id "; 
  } elseif(!empty($name)) {
    array_push($aliases,$name);    
  }
  if(count($aliases) > 0) {
    foreach($aliases as $alias) {
      $alias_query[] = "domain_alias LIKE '%$alias%'";
      $domain_query [] = "domain_name  = '$alias'";
    }
    $query = "SELECT domain_alias as name FROM Domain WHERE (".implode(' OR ', $alias_query).") $dquery
      UNION
      SELECT domain_name as name FROM Domain WHERE (".implode(' OR ', $domain_query).") $dquery";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'get_email_used(1)');
    $obm_q->query($query);
    while($obm_q->next_record()) {
      preg_match_all('/^('.implode('|',$aliases).')\r?$/m',$obm_q->f('name'),$results);
      $return = array_merge($return, $results[1]);      
    }
  }
  return $return;
}


/**
 * Check aliases integrity 
 * 
 * @param mixed $aliases 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_domain_get_users_aliases($aliases, $id) {
  global $cdg_sql;

  $d = of_domain_get_domain_infos($id);
  $return = array();
  if (!empty($d['alias'])) {
    $old_aliases = explode("\r\n",$d['alias']);
  } else {
    return array();
  }
  if (!empty($aliases)) {
    $aliases = explode("\r\n",$aliases);
    $aliases = array_diff($old_aliases,$aliases);
  } else {
    $aliases = $old_aliases;
  }
  if(count($aliases) > 0) {
    foreach($aliases as $alias) {
      $user_query[] = "userobm_email LIKE '%@$alias%'";
      $mailshare_query[] = "mailshare_email LIKE '%@$alias%'";
      $group_query[] = "group_email LIKE '%@$alias'";
      $regexp[] = "($alias)";
    }
    $id = sql_parse_id($id, true);
    $where_user .= " AND userobm_domain_id $id"; 
    $where_group .= " AND group_domain_id $id"; 
    $where_mailshare .= " AND mailshare_domain_id $id"; 

    $query = "SELECT userobm_email as mail FROM UserObm WHERE (".implode(' OR ', $user_query).") $where_user
      UNION
      SELECT mailshare_email as mail FROM MailShare WHERE (".implode(' OR ', $mailshare_query).") $where_mailshare
      UNION
      SELECT group_email as mail FROM UGroup WHERE (".implode(' OR ', $group_query).") $where_group";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'get_email_used(1)');
    $obm_q->query($query);
    while($obm_q->next_record()) {
      preg_match_all('/^[^@\n]*@('.implode('|',$aliases).').*\r?$/m',$obm_q->f('mail'),$results);
      $return = array_merge($return, $results[0]);
    }
  }
  return $return;
}


/**
 * Check if the domain can be deleted
 *
 * @param mixed $d_id : domain_id
 * @return true if the domain can be deleted, else false
 **/
function check_domain_can_delete($p_id) {
  global $err, $ok_msg;
  global $l_link_user, $l_link_user_no, $l_link_host, $l_link_host_no;

  $delete_ok = true;

  // Links from Users
  $nb = get_linked_user_domain($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= $l_link_user;
  } else {
    $ok_msg .= $l_link_user_no;
  }

  // Links from Hosts
  $nb = get_linked_host_domain($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= $l_link_host;
  } else {
    $ok_msg .= $l_link_host_no;
  }

  return $delete_ok;
}


/**
 * Count number of users in a domain
 *
 * @param mixed $domain_id : domain_id
 * @return Number of users in a domain
 **/
function get_linked_user_domain($domain_id) {
  $domain_id = sql_parse_id($domain_id, true);
  $nb = get_query_count("SELECT count(userobm_id) as nb
    FROM UserObm
    WHERE userobm_domain_id $domain_id ");

  return $nb;
}


/**
 * Count number of hosts in a domain
 *
 * @param mixed $domain_id : domain_id
 * @return Number of hosts in a domain
 **/
function get_linked_host_domain($domain_id) {
  $domain_id = sql_parse_id($domain_id, true);
  $nb = get_query_count("SELECT count(host_id) as nb
    FROM Host
    WHERE host_domain_id $domain_id ");

  return $nb;
}


/**
 * Check if a mailserver can be removed from a domain
 *
 * @param mixed $params : parameters : domain_id, mailserver_id
 * @return true if the mailserver can be removed from the domain
 **/
function check_domain_can_delete_mailserver(&$params) {
  global $err, $ok_msg;
  global $l_link_user_mailserver, $l_link_user_mailserver_no;

  $delete_ok = true;
  $mailservers = of_domain_get_domain_mailserver('', $params['domain_id']); 
  foreach ($mailservers as $mailserver_id => $mailserver) {
    foreach($mailserver as $server) {
      if($server['role'] == 'imap' && (!is_array($params[$server['role']]) || !in_array($mailserver_id, $params[$server['role']]))) {
        $nb = get_domain_linked_user_mailserver($params['domain_id'], $mailserver_id);
        if($nb > 0) {
          $params[$server['role']][] = $mailserver_id;
          $err['msg'] .= $server['name'].'('.$server['role'].')'.$l_link_user_mailserver;
          return false;
        }
      }
    }
  }  

  return true;
}


/**
 * Count number of users connected to a mailserver in a domain
 *
 * @param mixed $domain_id : domain_id
 * @param mixed $mailserver_id : mailserver_id
 * @return Number of users in a domain
 **/
function get_domain_linked_user_mailserver($domain_id, $mailserver_id) {
  $domain_id = sql_parse_id($domain_id, true);
  $mailserver_id = sql_parse_id($mailserver_id, true);
  $nb = get_query_count("SELECT count(userobm_id) as nb
    FROM UserObm
    WHERE userobm_domain_id $domain_id 
    AND userobm_mail_server_id $mailserver_id ");

  return $nb;
}


/**
 * Get the mailserver list candidate for a domain
 * where mailserver host_domain_id matches or from domain 0)
 * @param $role : restrict list to mail server role
 * @param $domain_id : restrict list to mail server in this domain
 *   if doma
 * @return array with mail servers
 **/
function get_domain_mailservers($role = NULL, $domain_id= NULL) {
  global $cdg_sql;

  if ($domain_id > 0) {
    $domain_id = sql_parse_id($domain_id, true);
    // For one domain specified (domain update), global or domain mailserver
    $where_domain = "AND (domain_id $domain_id OR domain_global = true)";
  } else {
    // New domain (no id specified), host from domain 0
    $where_domain = "AND (domain_global = true)";
  }
  if(!is_null($role)) {
    $where_role = " AND $role.service_id IS NOT NULL";
  } else {
    $where_role = " AND (imap.service_id IS NOT NULL OR smtp_out.service_id IS NOT NULL OR smtp_in.service_id IS NOT NULL)";
  }

  $obm_q = new DB_OBM;
  $query = "SELECT 
      Host.*,
      imap.service_id as host_imap,
      smtp_in.service_id as host_smtp_in,
      smtp_out.service_id as host_smtp_out
    FROM Host 
    INNER JOIN HostEntity ON hostentity_host_id = host_id
    LEFT JOIN Domain ON domain_id = host_domain_id
    LEFT JOIN Service as imap ON imap.service_service = 'imap' AND imap.service_entity_id = hostentity_entity_id
    LEFT JOIN Service as smtp_in ON smtp_in.service_service = 'smtp_in' AND smtp_in.service_entity_id = hostentity_entity_id
    LEFT JOIN Service as smtp_out ON smtp_out.service_service = 'smtp_out' AND smtp_out.service_entity_id = hostentity_entity_id    
    WHERE 1 = 1
    $where_role  
    $where_domain";
  display_debug_msg($query, $cdg_sql, 'get_domain_mailserver_list()');
  $obm_q->query($query);

  $m = array();
  while($obm_q->next_record()) {
    $id = $obm_q->f('host_id');
    $m[$id]['id'] = $id;
    $m[$id]['name'] = $obm_q->f('host_name');
    $m[$id]['imap'] = $obm_q->f('host_imap');
    $m[$id]['smtp_in'] = $obm_q->f('host_smtp_in');
    $m[$id]['smtp_out'] = $obm_q->f('host_smtp_out');
  }

  return $m;
}


///////////////////////////////////////////////////////////////////////////////
// Get a domain properties
// Parameters:
//   - $domain_id : domain_id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_properties($domain_id = NULL) {
  global $cdg_sql;

  if (!is_null($domain_id)) {
    $val = 'domainpropertyvalue_value,';
    $join = " LEFT JOIN DomainPropertyValue
      ON domainproperty_key = domainpropertyvalue_property_key
      AND domainpropertyvalue_domain_id = '$domain_id'";
  }
  $query = "SELECT 
    $val
    domainproperty_key,
    domainproperty_type,
    domainproperty_default,
    domainproperty_readonly
    FROM DomainProperty $join";

  display_debug_msg($query, $cdg_sql, 'run_query_domain_properties()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;

}


/**
 * Get domain samba properties 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_domain_samba_properties($id) {
  global $obm, $cdg_sql;

  $db = new DB_OBM;
  $query = "SELECT * FROM ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    WHERE serviceproperty_service = 'samba' and domainentity_domain_id = '$id'";
  display_debug_msg($query, $cdg_sql, 'run_query_domain_samba_properties()');
  $db->query($query);
  $samba = array();
  while($db->next_record()) {
    $samba[$db->f('serviceproperty_property')] = $db->f('serviceproperty_value');
  }
  return $samba;
}
?>
