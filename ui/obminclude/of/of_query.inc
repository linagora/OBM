<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : of_query.inc                                                 //
//     - Desc : OBM Framework SQL and query Objects                          //
// 2005-06-29 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//--- SQL handling ----------------------------------------------------------//

///////////////////////////////////////////////////////////////////////////////
// Format a Date field query according to Database backend
// Parameters:
//   - $db_type : DB type
//   - $field   : date field name
//   - $as      : (Optionnal) alias fieldname to return
// Returns:
//   - Field query string
///////////////////////////////////////////////////////////////////////////////
function sql_date_format($db_type, $field, $as='') {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = "UNIX_TIMESTAMP($field)";
    if ($as != '') {
      $ret .= " as $as";
    }
  } elseif ($db_type == $db_type_pgsql) {
    $ret = "EXTRACT (EPOCH from $field)";
    if ($as != '') {
      $ret .= " as $as";
    }
  } else {
    $ret = $field;
  }

  return $ret;
}


/**
 * Return an int if id is numeric elser return NULL 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function sql_parse_id($id, $select=false, $not=false) {
  if($select) {
    if($not) {
      return (is_numeric($id) && $id > 0)?'!= '.(int)$id:'IS NOT NULL';
    } else {
      return (is_numeric($id) && $id > 0)?'= '.(int)$id:'IS NULL';
    }
  } else {
    return (is_numeric($id) && $id > 0)?(int)$id:'NULL';
  }
}

/**
 * Return an int if id is numeric elser return NULL 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function sql_implode_id($separator, $ids) {
    foreach($ids as $key => $id) {
      $ids[$key] = (is_numeric($id) && $id > 0)? (int)$id:'NULL';
    }
    return implode($separator, $ids);
}
/**
 * Return an int if $int is numeric else return 0
 * 
 * @param mixed $int 
 * @access public
 * @return void
 */
function sql_parse_int($int) {
  return (is_numeric($int))?(int)$int:0;
}


/**
 * Return an number if $num is numeric (can be a float) else return 0
 * 
 * @param mixed $num
 * @access public
 * @return void
 */
function sql_parse_num($num) {
  return (is_numeric($num))?(float)$num:0;
}


/**
 * Return a formatted and checked text from text
 * 
 * @param mixed $text
 * @access public
 * @return void
 */
function sql_parse_text($text) {

  $new_text = trim($text);

  return $new_text;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql word corresponding to case incensitive like
// Parameters:
//   - $db_type : DB type
// Returns:
//   - sql word 'ilike'
///////////////////////////////////////////////////////////////////////////////
function sql_casei_like($db_type) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = 'like';
  } elseif ($db_type == $db_type_pgsql) {
    $ret = 'ilike';
  } else {
    $ret = 'like';
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Transform and Return a searched string (allow global processing)
// Parameters:
//   - $text : Text o parse or transform
// Returns:
//   - updated text
///////////////////////////////////////////////////////////////////////////////
function sql_search_text_parse($text) {
  global $cgp_sql_star;

  $mytext = $text;

  if ($cgp_sql_star) {
    $mytext = preg_replace("/\*/", "%", $text);
  }

  return $mytext;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql order clause handling case insensitive sorting
// Parameters:
//   - $db_type : DB type
// Returns:
//   - sql word 'ilike'
///////////////////////////////////////////////////////////////////////////////
function sql_casei_sort($db_type, $field) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = "$field";
  } elseif ($db_type == $db_type_pgsql) {
    $ret = "UPPER($field)";
  } else {
    $ret = "$field";
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql corresponding to concat function
// Parameters:
//   - $db_type : DB type
//   - $ctt[]   : array of values ctt[0][type] = field | string, ctt[0][value]
// Returns:
//   - sql concatenation string
///////////////////////////////////////////////////////////////////////////////
function sql_string_concat($db_type, $ctt) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $start = 'concat(';
    $end = ')';
    $sep = ',';
  } elseif ($db_type == $db_type_pgsql) {
    $start = '(';
    $end = ')';
    $sep = '||';
  } else {
    $start = '(';
    $end = ')';
    $sep = '||';
  }

  $ret = $start;
  if ($ctt[0]['type'] == 'field') {
    $ret .= $ctt[0]['value'];
  } else {
    $ret .= "'" . $ctt[0]['value'] . "'";
  }
  $i = 1;
  while ($ctt[$i]) {
    $ret .= " $sep ";
    if ($ctt[$i]['type'] == 'field') {
      $ret .= $ctt[$i]['value'];
    } else {
      $ret .= "'" . $ctt[$i]['value'] . "'";
    }
    $i++;
  }
  $ret .= $end;

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql string corresponding to if then else
// Parameters:
//   - $db_type : DB type
// Returns:
//   - sql word 'ilike'
///////////////////////////////////////////////////////////////////////////////
function sql_if($db_type, $expr, $iftrue, $iffalse) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    $ret = "if ($expr, $iftrue, $iffalse)";
  } elseif ($db_type == $db_type_pgsql) {
    $ret = "case $expr when true then $iftrue else $iffalse end";
  } else {
    $ret = "if ($expr, $iftrue, $iffalse)";
  }

  return $ret;
}


/**
 * Return the correct order clause to have NULL first
 * 
 * @param mixed $db_type
 * @access public
 * @return string ('ASC' or 'DESC')
 */
function sql_order_null_first($db_type, $null_first=true) {
  global $db_type_mysql, $db_type_pgsql;

  if ($db_type == $db_type_mysql) {
    if ($null_first) {
      $ret = 'ASC';
    } else {
      $ret = 'DESC';
    }      
  } elseif ($db_type == $db_type_pgsql) {
    if ($null_first) {
      $ret = 'DESC';
    } else {
      $ret = 'ASC';
    }      
  } else {
    $ret = 'ASC';
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql string corresponding to the limit clause
// If number and count not given, user preferences values are used
// Parameters:
//   - $db_type  : DB type
//   - $number   : number of rows to return (set_rows by default)
//   - $p_offset : #rows to skip (start at offset + 1) (default from page)
//   - $name     : of_display name
// Returns:
//   - limit clause
///////////////////////////////////////////////////////////////////////////////
function sql_limit($db_type, $number='', $p_offset='', $name='') {
  global $db_type_mysql, $db_type_pgsql, $ctu_sql_limit;
  global $set_rows_default, $params;

  // If limit should not be used, return empty clause
  if (! $ctu_sql_limit) { return ''; }

  if ($number != '') {
    $num = $number;
  } else if ($_SESSION['set_rows'] != '') {
    $num = $_SESSION['set_rows'];
  } else {
    $num = $set_rows_default;
  }

  // Handle cursor only if rigth dataset
  if ($p_offset != '') {
    $off = $p_offset;
  } else if (($params['page'] > 0) && ($name == $params['of_display_name'])) {
    $off = ($params['page'] - 1) * $num;
  } else {
    $off = '0';
  }

  if (($db_type == $db_type_mysql) || ($db_type == $db_type_pgsql)) {
    $ret = "limit $num offset $off";
  } else {
    $ret = "limit $num offset $off";
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Check Privacy access
// Parameters:
//   - $entity : entity to check
//   - $p_uid  : user id to check access
// Returns true if entity is visible, else false
///////////////////////////////////////////////////////////////////////////////
function sql_obm_entity_privacy($entity, $p_uid='') {
  global $obm, $cdg_sql;

  // Set uid if not given
  if ($p_uid == '') {
    $uid = $obm['uid'];
  } else {
    $uid = $p_uid;
  }

  $field_pri = "${entity}_privacy";
  $field_uc = "${entity}_usercreate";

  $where = "($field_pri = '0' or $field_uc = '$uid')";

  return $where;
}


///////////////////////////////////////////////////////////////////////////////
// Return the sql domain clause according to multidomain mode
// Parameters:
//   - $entity : entity to add clause
//   - $and    : if true (default) add AND before the clause
//   - $extended : include domain 0 datas
// Returns:
//   - sql where clause for domain (empty for superadmin)
///////////////////////////////////////////////////////////////////////////////
function sql_multidomain($entity, $and=true) {
  global $obm;

  $where = '';

  // If user is multidomain, no restrictions on domain
  if ($obm['domain_global'] == true) {
    return $where;
  } else {
    $domain_id = $obm['domain_id'];
  }
  if ($and) {
    $wand = 'AND';
  }
  $where = "$wand (${entity}_domain_id='$domain_id')";

  return $where;
}


///////////////////////////////////////////////////////////////////////////////
// Return the count(*) of the query (the 1fst field)
// Parameters:
//   - $db_type  : DB type
// Returns:
//   - limit clause
///////////////////////////////////////////////////////////////////////////////
function get_query_count($query) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'get_query_count()');
  $obm_q->xquery($query);
  $obm_q->next_record();
  $count = $obm_q->f(0);

  return $count;
}


//--- Multi-domain handling -------------------------------------------------//


/**
 * Return the OBM domain list
 * @return array with domains
 **/
function of_domain_get_list() {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $query = "SELECT domain_id,
    domain_label,
    domain_name,
    domain_global
    FROM Domain
    ORDER BY domain_global, domain_name";
  display_debug_msg($query, $cdg_sql, 'of_domain_get_list()');
  $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f('domain_id');
    $domain[$id]['id'] = $id;
    $domain[$id]['label'] = $obm_q->f('domain_label');
    $domain[$id]['name'] = $obm_q->f('domain_name');
    $domain[$id]['global'] = $obm_q->f('domain_global');
  }

  return $domain;
}


/**
 * Get one domain mailservers
 *
 * @param mixed $domain_id : domain_id
 * @return mailserver array
 **/
function of_domain_get_domain_mailserver($role = 'imap', $domain_id = NULL) {
  global $cdg_sql, $obm;

  if (!is_null($domain_id)) {
    $d = of_domain_get_domain_infos($domain_id);
    if (! $d['global']) {
      $domain_id = sql_parse_id($domain_id, true);
      $where_domain = "AND (domain_id $domain_id OR domain_global = true)";
    }
  }
  if ($role != '') {
    $where_role = "AND serviceproperty_property = '$role'";
  } else {
    $where_role = "AND serviceproperty_property IN ('imap', 'smtp_in', 'smtp_out')";
  }

  $query = "SELECT 
    serviceproperty_property,
    host_id,
    host_name,
    host_ip
    FROM Domain
    INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
    LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
    LEFT JOIN Host ON host_id = #CAST(serviceproperty_value,INTEGER)
    WHERE 
    serviceproperty_service = 'mail'
    $where_role
    $where_domain
    GROUP BY serviceproperty_property, host_id, host_name, host_ip
";

  display_debug_msg($query, $cdg_sql, 'of_domain_get_domain_mailserver()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);

  $m = array();
  while ($obm_q->next_record()) {
    $m_id = $obm_q->f('host_id');
    $role = $obm_q->f('serviceproperty_property');
    $name = $obm_q->f('host_name');
    $ip = $obm_q->f('host_ip');
    $m[$m_id][] = array ('name' => $name, 'ip' => $ip, 'role' => $role);
  }

  return $m;
}

/**
 * Get one domain syncservers
 *
 * @param mixed $domain_id : domain_id
 * @return syncserver array
 **/
function of_domain_get_domain_syncserver($domain_id = NULL) {
  global $cdg_sql, $obm;

  if (!is_null($domain_id)) {
    $d = of_domain_get_domain_infos($domain_id);
    if (! $d['global']) {
      $domain_id = sql_parse_id($domain_id, true);
      $where_domain = "AND (domain_id $domain_id OR domain_global = true)";
    }
  }
  $where = "AND serviceproperty_property = 'obm_sync'";

  $query = "SELECT 
    serviceproperty_property,
    host_id,
    host_name,
    host_ip
    FROM Domain
    INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
    LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
    LEFT JOIN Host ON host_id = #CAST(serviceproperty_value,INTEGER)
    WHERE 
    serviceproperty_service = 'sync'
    $where
    $where_domain
    GROUP BY serviceproperty_property, host_id, host_name, host_ip
";

  display_debug_msg($query, $cdg_sql, 'of_domain_get_domain_syncserver()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);

  $m = array();
  while ($obm_q->next_record()) {
    $m_id = $obm_q->f('host_id');
    $role = $obm_q->f('serviceproperty_property');
    $name = $obm_q->f('host_name');
    $ip = $obm_q->f('host_ip');
    $m[$m_id][] = array ('name' => $name, 'ip' => $ip, 'role' => $role);
  }

  return $m;
}



/**
 * Get one domain solrservers
 *
 * @param mixed $domain_id : domain_id
 * @return solrserver array
 **/
function of_domain_get_domain_solrserver($domain_id = NULL, $entity = "") {
  global $cdg_sql, $obm;

  if (!is_null($domain_id)) {
    $d = of_domain_get_domain_infos($domain_id);
    if (! $d['global']) {
      $domain_id = sql_parse_id($domain_id, true);
      $where_domain = "AND (domain_id $domain_id OR domain_global = true)";
    }
  }
  if ($entity != "") {
    $where = "AND serviceproperty_property = '$entity'";
  }

  $query = "SELECT 
    serviceproperty_property,
    host_id,
    host_name,
    host_ip
    FROM Domain
    INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
    LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
    LEFT JOIN Host ON host_id = #CAST(serviceproperty_value,INTEGER)
    WHERE 
    serviceproperty_service = 'solr'
    $where
    $where_domain
    GROUP BY serviceproperty_property, host_id, host_name, host_ip";

  display_debug_msg($query, $cdg_sql, 'of_domain_get_domain_solrserver()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);

  $m = array();
  while ($obm_q->next_record()) {
    $m_id = $obm_q->f('host_id');
    $role = $obm_q->f('serviceproperty_property');
    $name = $obm_q->f('host_name');
    $ip = $obm_q->f('host_ip');
    $m[$m_id][] = array ('name' => $name, 'ip' => $ip, 'role' => $role);
  }

  return $m;
}



/**
 * Get one domain imap_frontend servers
 *
 * @param mixed $domain_id : domain_id
 * @return solrserver array
 **/
function of_domain_get_domain_imapfrontendserver($domain_id = NULL) {
  global $cdg_sql, $obm;

  if (!is_null($domain_id)) {
    $d = of_domain_get_domain_infos($domain_id);
    if (! $d['global']) {
      $domain_id = sql_parse_id($domain_id, true);
      $where_domain = "AND (domain_id $domain_id OR domain_global = true)";
    }
  }

  $query = "SELECT 
    serviceproperty_property,
    host_id,
    host_name,
    host_ip
    FROM Domain
    INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
    LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
    LEFT JOIN Host ON host_id = #CAST(serviceproperty_value,INTEGER)
    WHERE 
    serviceproperty_service = 'mail'
    AND serviceproperty_property = 'imap_frontend'
    $where_domain
    GROUP BY serviceproperty_property, host_id, host_name, host_ip";

  display_debug_msg($query, $cdg_sql, 'of_domain_get_domain_imapfrontendserver()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);

  $m = array();
  while ($obm_q->next_record()) {
    $m_id = $obm_q->f('host_id');
    $role = $obm_q->f('serviceproperty_property');
    $name = $obm_q->f('host_name');
    $ip = $obm_q->f('host_ip');
    $m[$m_id][] = array ('name' => $name, 'ip' => $ip, 'role' => $role);
  }

  return $m;
}



/**
 * Get one domain backup_ftp servers
 *
 * @param mixed $domain_id : domain_id
 * @return solrserver array
 **/
function of_domain_get_domain_backupftpserver($domain_id = NULL) {
  global $cdg_sql, $obm;

  if (!is_null($domain_id)) {
    $d = of_domain_get_domain_infos($domain_id);
    if (! $d['global']) {
      $domain_id = sql_parse_id($domain_id, true);
      $where_domain = "AND (domain_id $domain_id OR domain_global = true)";
    }
  }

  $query = "SELECT 
    serviceproperty_property,
    host_id,
    host_name,
    host_ip
    FROM Domain
    INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
    LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
    LEFT JOIN Host ON host_id = #CAST(serviceproperty_value,INTEGER)
    WHERE 
    serviceproperty_service = 'backup'
    AND serviceproperty_property = 'backup_ftp'
    $where_domain
    GROUP BY serviceproperty_property, host_id, host_name, host_ip";

  display_debug_msg($query, $cdg_sql, 'of_domain_get_domain_backupftpserver()');
  $obm_q = new DB_OBM;
  $obm_q->xquery($query);

  $m = array();
  while ($obm_q->next_record()) {
    $m_id = $obm_q->f('host_id');
    $role = $obm_q->f('serviceproperty_property');
    $name = $obm_q->f('host_name');
    $ip = $obm_q->f('host_ip');
    $m[$m_id][] = array ('name' => $name, 'ip' => $ip, 'role' => $role);
  }

  return $m;
}



///////////////////////////////////////////////////////////////////////////////
// Return a domain infos
// Parameters:
//   - $d_id           : domain in
//   - $get_properties : flag to get properties
// Returns:
//   - array with domain infos
///////////////////////////////////////////////////////////////////////////////
function of_domain_get_domain_infos($d_id, $get_properties=false) {
  global $cdg_sql;

  $d_id = sql_parse_id($d_id, true);
  $obm_q = new DB_OBM;
  $query = "SELECT domain_id,
    domain_label,
    domain_name,
    domain_alias,
    domain_global
    FROM Domain
    WHERE domain_id $d_id";
  display_debug_msg($query, $cdg_sql, 'of_domain_get_domain_infos()');
  $obm_q->query($query);
  $obm_q->next_record();

  $domain['id'] = $obm_q->f('domain_id');
  $domain['label'] = $obm_q->f('domain_label');
  $domain['name'] = $obm_q->f('domain_name');
  $domain['alias'] = $obm_q->f('domain_alias');
  $domain['global'] = $obm_q->f('domain_global');

  if ($get_properties) {
    $query = "SELECT * FROM DomainPropertyValue
      WHERE domainpropertyvalue_domain_id $d_id ";
    display_debug_msg($query, $cdg_sql, 'of_domain_get_domain_infos()');
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $prop = $obm_q->f('domainpropertyvalue_property_key');
      $value = $obm_q->f('domainpropertyvalue_value');
      $domain[$prop] = $value;
    }
  }

  return $domain;
}


///////////////////////////////////////////////////////////////////////////////
// Return one entity domain id
// Parameters:
//   - $id      : entity id
//   - $entityu : entity name (u cause may be table name : first char uppercase)
//   - $ptable  : table name (needed only if different from Entity)
// Returns:
//   - entity domain id
///////////////////////////////////////////////////////////////////////////////
function of_domain_get_entity_domain_id($id, $entityu, $ptable='') {
  global $cdg_sql;

  // domain default value
  $dom_id = 0;

  $entity = strtolower($entityu);
  if ($ptable == '') {
    $table = $entityu;
  } else {
    $table = $ptable;
  }
  $id = sql_parse_id($id, true);
  $obm_q = new DB_OBM;
  $query = "SELECT ${entity}_domain_id as domain_id
    FROM $table
    WHERE ${entity}_id $id ";
  display_debug_msg($query, $cdg_sql, 'of_domain_get_entity_domain_id()');
  $obm_q->query($query);
  $obm_q->next_record();
  $dom_id = $obm_q->f('domain_id');

  return $dom_id;
}


///////////////////////////////////////////////////////////////////////////////
// Display: domain html select component
// Parameters:
//   - $domains       : Domain array
//   - $domain_id     : selected domain
//   - $first_element : scalar or array : (['all'|'none'])
//   - $sel_append    : string (eg javascript code) to append at select
///////////////////////////////////////////////////////////////////////////////
function of_domain_dis_select($domain_id='', $first_element='', $sel_append='') {
  $domains = of_domain_get_list();
  $sel_dom = of_domain_html_select($domains, $domain_id, $first_element, $sel_append);

  return $sel_dom;
}


///////////////////////////////////////////////////////////////////////////////
// Display: domain html select component
// Parameters:
//   - $domains       : Domain array
//   - $domain_id     : selected domain
//   - $first_element : scalar or array : (['all'|'none'])
//   - $sel_append    : string (eg javascript code) to append at select
///////////////////////////////////////////////////////////////////////////////
function of_domain_html_select($domains, $domain_id='', $first_element='', $sel_append='') {

  if (is_array($first_element)) {
    foreach($first_element as $element) {
      $l_element = "l_${element}";
      $c_element = "c_${element}"; 
      global $$l_element, $$c_element;
      if ($$c_element == $domain_id) {
        $first_option .= "
          <option value=\"${$c_element}\" selected=\"selected\">${$l_element}</option>";
      } else {
        $first_option .= "
          <option value=\"${$c_element}\">${$l_element}</option>";
      }
    }
  } else if ($first_element != '') {
    $l_element = "l_${first_element}";
    $c_element = "c_${first_element}"; 
    global $$l_element, $$c_element;
    if ($$c_element == $domain_id) {
      $first_option .= "
        <option value=\"${$c_element}\" selected=\"selected\">${$l_element}</option>";
    } else {
      $first_option .= "
        <option value=\"${$c_element}\">${$l_element}</option>";
    }
  }

  // Domain select
  $sel_dom = "<select id=\"sel_domain_id\" name=\"sel_domain_id\" $sel_append>
    $first_option";
  if (is_array($domains)) {
    foreach ($domains as $one_domain) {
      $d_id = $one_domain['id'];
      $d_label = ($one_domain['label']);
      $d_name = ($one_domain['name']);
      $dis_name = ($d_name != '') ? " ($d_name)" : '';
      $sel_dom .= "\n<option value=\"$d_id\"";
      if ($d_id == $domain_id) { $sel_dom .= " selected=\"selected\""; }
      $sel_dom .= ">$d_label$dis_name</option>";
    }
  }
  $sel_dom .= '</select>';

  return $sel_dom;
}


/**
 * Display : domain mailserver html select
 * @param $role : restrict list to mail server role
 * @param $sel_id : selected entry
 * @param $domain_id : restrict list to mail server in this domain
 **/
function of_domain_dis_mailserver_select($role, $sel_id='', $domain_id='') {

  $dms = of_domain_get_domain_mailserver($role, $domain_id);

  // select
  $sel = "<select id=\"sel_mail_server_id\" name=\"sel_mail_server_id\">";
  if (is_array($dms)) {
    // one mail server
    foreach ($dms as $id => $m) {
      if (is_array($m)) {
        // one mail server role
        foreach ($m as $r) {
          $name = htmlentities($r['name'], ENT_COMPAT, 'UTF-8');
          $sel .= "<option value=\"$id\"";
          if ($id == $sel_id) { $sel .= " selected=\"selected\""; }
          $sel .= ">$name</option>";
        }
      }
    }
  }
  $sel .= '</select>';

  return $sel;
}


//--- Delegation handling ---------------------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Delegation : Display search form field
// Parameters :
//   - $value : default displayed value
///////////////////////////////////////////////////////////////////////////////
function of_delegation_dis_search_form($value) {
  global $cgp_use, $l_delegation;

  $block = '';
  if ($cgp_use['property']['delegation']) {
    $block = "
      <label>$l_delegation<br />
      <input type=\"text\" name=\"tf_delegation\" size=\"16\" maxlength=\"256\"
      value=\"$value\" />
      </label>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Delegation : Display block consult
// Parameters:
//   - $value : value displayed
// Returns : XHTML consult display block
///////////////////////////////////////////////////////////////////////////////
function of_delegation_dis_block_consult($value) {
  global $cgp_use, $l_delegation;

  $block = '';
  if ($cgp_use['property']['delegation']) {
    $block = "<tr>
      <th>$l_delegation</th>
      <td title=\"$value\">$value</td>
      </tr>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Delegation : Display update block
// Parameters:
//   - $value : value displayed
// Returns : XHTML consult display block
///////////////////////////////////////////////////////////////////////////////
function of_delegation_dis_block_update($value, $class) {
  global $obm, $cgp_use, $profiles, $l_delegation;

  $block = '';
  if ($cgp_use['property']['delegation']) {
    $delegation = $value;
    // Default to admin target if empty
    if ($delegation == '') {
      $delegation = $obm['delegation_target'];
    }
    $input_delegation = "<input name=\"tf_delegation\" id='delegationField' size=\"32\" maxlength=\"256\" value=\"$delegation\" />";
    $block = "
      <tr>
      <th class=\"$class\">$l_delegation</th>
      <td>$input_delegation</td>
      </tr>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Delegation : get query insert clauses
// Parameters:
//   - $field : Db field
//   - $value : value to insert
// Returns : XHTML consult display block
///////////////////////////////////////////////////////////////////////////////
function of_delegation_query_insert_clauses($field, $value) {
  global $obm, $cgp_use, $profiles;

  $obm_query = new DB_OBM;
  $q = array();
  if ($cgp_use['property']['delegation']) {
    $q['field'] = ", $field";
    $q['value'] = ", '".$obm_query->escape($value)."'";
  }

  return $q;
}


///////////////////////////////////////////////////////////////////////////////
// Delegation : get query update clause
// Parameters:
//   - $field : Db field
//   - $value : value to insert
// Returns : XHTML consult display block
///////////////////////////////////////////////////////////////////////////////
function of_delegation_query_update_clause($field, $value) {
  global $obm, $cgp_use, $profiles;

  $obm_query = new DB_OBM;
  $q = '';
  if ($cgp_use['property']['delegation']) {
    $q = ", $field='".$obm_query->escape($value)."'";
  }

  return $q;
}


///////////////////////////////////////////////////////////////////////////////
// Delegation : Check update rights
// Parameters:
//   - $params : parameters given (on insert or update action)
//   - $current_delegation : object current delegation to check right
// Returns : XHTML consult display block
///////////////////////////////////////////////////////////////////////////////
function of_delegation_check_update_rights($params, $current_delegation, $delegation=false) {
  global $obm, $cgp_use;
  
  if ($cgp_use['property']['delegation']) {
    $id = $params['id'];
    $action = $params['action'];
    if (!$delegation) $delegation = $params['delegation'];
    // Object (User,..) delegation must be in target delegation
    if ($obm['delegation_target'] != '') {
      if (strpos($current_delegation, $obm['delegation_target']) !== 0) {
        return false;
      }
      // Can not update to other delegation
      if ( (($action == 'insert') || ($action == 'update'))
        && (strpos($delegation, $obm['delegation_target']) !== 0)) {
        //&& ($delegation != $obm['delegation_target'])) {
          return false;
      }
    }
  }
  return true;
}


/**
 * of_delegation_check_data 
 * 
 * @param mixed $delegation 
 * @param mixed $domain_id 
 * @access public
 * @return void
 */
function of_delegation_check_data($delegation, $domain=false) {
  global $obm, $cgp_use, $err;

  if ($cgp_use['property']['delegation']) {
    // Check that delegation is in the domain delegation
    if (isset($domain) && ($domain != '') && strpos($delegation, $domain) !== 0) {
      $err['msg'] = "$GLOBALS[l_err_delegation_outside_domain] ($domain)";
      return false;
    }
    // Check that delegation is in the admin delegation target
    if (isset($obm['delegation_target']) && ($obm['delegation_target'] != '') && strpos($delegation, $obm['delegation_target']) !== 0) {
      $err['msg'] = "$GLOBALS[l_err_delegation_outside_target] ($obm[delegation_target])";
      return false;
    }
  }
  return true;
}

//--- User and Display preferences handling----------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Get the User's entity display preferences
// Parameters:
//   - $u_id   : user id to retrieve the pref
//   - $entity : entity to retrieve field from
//   - $all    : if not 0, get all fields (even those marked not to display)
// Returns:
//   - $prefs  : prefs[$fieldname] : ['order'], ['status'], ['notdefault']
///////////////////////////////////////////////////////////////////////////////
function get_display_pref($u_id, $entity, $all=0) {
  global $cdg_sql;

  $u_id = sql_parse_id($u_id, true);
  if ($all == 0) {
    $hide = true;
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $order_dir = sql_order_null_first($db_type, true);

  $query = "SELECT *
    FROM DisplayPref
    WHERE (display_user_id IS NULL OR display_user_id $u_id )
    AND display_entity='$entity'
    ORDER BY display_user_id $order_dir";

  display_debug_msg($query, $cdg_sql, 'get_display_pref()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // The query is sorted by user_id, hence default values first
  while ($obm_q->next_record()) {
    $user_id = $obm_q->f('display_user_id');
    $fieldname = $obm_q->f('display_fieldname');
    $fieldorder = $obm_q->f('display_fieldorder');
    $fieldstatus = $obm_q->f('display_display');

    // For specific user values, get them only if defined
    if ($user_id > 0) {
      if (isset($prefs[$fieldname])) {
        // Note if user value different from default value
        if ( ($prefs[$fieldname]['order'] != $fieldorder)
          || ($prefs[$fieldname]['status'] != $fieldstatus) ) {
            $prefs[$fieldname]['notdefault'] = true;
          }
        // If get only not hidden fields
        if ($hide) {
          if ($fieldstatus == 0) {
            $hidden[$fieldname] = true;
            // If user has set the field visible, show it
          } elseif (($hidden[$fieldname] == true) && ($fieldstatus != 0)) {
            unset($hidden[$fieldname]);
          }
        }
        $index_sort[$prefs[$fieldname]['order']] = $fieldorder;
        $prefs[$fieldname]['order'] = $fieldorder;
        $prefs[$fieldname]['status'] = $fieldstatus;
      }
      // Default values (user id is NULL)
    } else {
      $prefs[$fieldname]['order'] = $fieldorder;
      $prefs[$fieldname]['status'] = $fieldstatus;
      $index_sort[$fieldorder] = $fieldorder;
      if (($hide) && ($fieldstatus == 0)) {
        $hidden[$fieldname] = true;
      }
    }
  }

  // Sort the $prefs array according to the final fieldorder
  array_multisort($index_sort, $prefs);

  // If we do not show hidden fields, we remove these from result
  if ($hide) {
    if (is_array($hidden)) {
      while (list($key, $value) = each($hidden)) {
        unset($prefs[$key]);
      }
    }
  }
  return $prefs;
}


///////////////////////////////////////////////////////////////////////////////
// Get One User's entity field display preference
// Parameters:
//   - $u_id   : user id to retrieve the pref
//   - $entity : entity to retrieve field from
//   - $field  : field
// Returns:
//   - $prefs  : prefs[user_id] : ['order'],['status'], user_id in (0, $u_id)
///////////////////////////////////////////////////////////////////////////////
function get_one_display_pref($u_id, $entity, $fieldname) {
  global $cdg_sql;
  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT *
    FROM DisplayPref
    WHERE (display_user_id IS NULL OR display_user_id $u_id )
    AND display_entity='$entity'
    AND display_fieldname='$fieldname'
    ORDER BY display_user_id DESC"; 

  display_debug_msg($query, $cdg_sql, 'get_one_display_pref()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // The query is sorted by user_id, hence default values first
  while ($obm_q->next_record()) {
    $user_id = $obm_q->f('display_user_id');
    $fieldorder = $obm_q->f('display_fieldorder');
    $fieldstatus = $obm_q->f('display_display');
    if (!$user_id > 0)
      $user_id = 0;
    $prefs[$user_id]['order'] = $fieldorder;
    $prefs[$user_id]['status'] = $fieldstatus;
  }

  return $prefs;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Store the new display value for the entity, field
// Parameters:
//   - $params : hash with :
//        entity      : entity affected
//        fieldname   : fieldname to change the display value
//        fieldstatus : new display status value; '' if not updated
//        fieldorder  : new display order; '' if not updated
///////////////////////////////////////////////////////////////////////////////
function update_display_pref($params) {
  global $obm, $cdg_sql;

  $entity = $params['entity'];
  $fieldname = $params['fieldname'];
  $new_status = $params['fieldstatus'];
  $sql_order_field = $params['fieldorder'];

  $uid = $obm['uid'];
  $user_id = sql_parse_id($uid, true);
  $prefs = get_one_display_pref($uid, $entity, $fieldname);
  $default_order = $prefs[0]['order'];
  $default_status = $prefs[0]['status'];
  $user_status = $prefs[$uid]['status'];
  $user_order = $prefs[$uid]['order'];
  if (($user_status != '') && ($user_status >= 0)) {
    $current_status = $user_status;
  } else {
    $current_status = $default_status;
  }
  $current_order = ($user_order > 0 ? $user_order : $default_order);

  // if status not set new_status set to current_status
  if ($new_status == '') {
    $new_status = $current_status;
  }
  // if order is updated, update other field concerned too (with current order)
  if ($sql_order_field != '') {
    run_query_display_pref_level_update2($uid, $entity, $sql_order_field, $current_order);
    return;
  } else {
    $sql_order_field = $current_order;
  }

  // if new field values = default values and user value exists, erase it
  if ($new_status == $default_status) {
    if ($sql_order_field == $default_order) {

      $query = "DELETE FROM DisplayPref
        WHERE display_user_id $user_id 
        AND display_entity='$entity'
        AND display_fieldname='$fieldname'";

      display_debug_msg($query, $cdg_sql, 'update_display_pref()');
      $obm_q = new DB_OBM; 
      $obm_q->query($query);
    } else {

      // if user entry does not exist
      if (! isset($prefs[$uid])) {
        run_query_display_pref_insert($uid, $entity, $fieldname, $new_status, $sql_order_field);
      } else {
        // user entry is different (=> update it)
        run_query_display_pref_update($uid, $entity, $fieldname, $new_status, $sql_order_field);
      }
    }

  } else {  // $new status != default status

    if (isset($prefs[$uid])) {
      // if user entry exists, we update it
      run_query_display_pref_update($uid, $entity, $fieldname, $new_status, $sql_order_field);
    } else {
      // user entry does not exist, we create it
      run_query_display_pref_insert($uid, $entity, $fieldname, $new_status, $default_order);
    }
  }

}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Update a display value for the entity, user, field
// Parameters:
//   - $u_id      : user id
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $status    : field status to set
//   - $order     : [opt] field order to set
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_update($u_id, $entity, $fieldname, $status, $order='') {
  global $cdg_sql;

  $coma = '';
  if ($status != '') {
    $update_status = "$coma
      display_display='$status'";
    $coma = ',';
  }
  if ($order != '') {
    $update_order = "$coma
      display_fieldorder='$order'";
    $coma = ',';
  }

  if ($coma != '') {
    $u_id = sql_parse_id($u_id, true);
    $query = "UPDATE DisplayPref SET
      $update_status
      $update_order
      WHERE display_user_id $u_id 
      AND display_entity='$entity'
      AND display_fieldname='$fieldname'";

    display_debug_msg($query, $cdg_sql, 'run_query_display_pref_update()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Create a display value for the entity, user, field
// Parameters:
//   - $u_id      : user id
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
//   - $status    : field status to set
//   - $order     : field order to set
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_insert($u_id, $entity, $fieldname, $status, $order) {
  global $cdg_sql;

  $u_id = sql_parse_id($u_id);
  $query = "INSERT INTO DisplayPref (
    display_user_id,
    display_entity,
    display_fieldname,
    display_fieldorder,
    display_display)
    VALUES (
      $u_id,
      '$entity',
      '$fieldname',
      $order,
      '$status')";

  display_debug_msg($query, $cdg_sql, 'run_query_display_pref_insert()');
  $obm_q = new DB_OBM; 
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Delete a display value for the entity, user, field
// Parameters:
//   - $u_id      : user id
//   - $entity    : entity affected
//   - $fieldname : fieldname to change the display value
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_delete($u_id, $entity, $fieldname) {
  global $cdg_sql;

  $query = "DELETE FROM DisplayPref
    WHERE display_user_id = '$u_id'
      AND display_entity = '$entity'
      AND display_fieldname = '$fieldname'
    ";

  display_debug_msg($query, $cdg_sql, 'run_query_display_pref_delete()');
  $obm_q = new DB_OBM; 
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// DEPRECATED
// Update the level of the field in the entity given identified by the order
// Parameters:
//   - $u_id       : user id
//   - $entity     : entity affected
//   - $fieldorder : actual position
//   - $new_level  : new position
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_level_update($u_id, $entity, $fieldorder, $new_level) {
  global $cdg_sql;
  
  $uid = sql_parse_id($u_id, true);
  // Get the current field with this order (specific or default)
  $query = "SELECT
    display_user_id,
    display_fieldname,
    display_display
    FROM DisplayPref
    WHERE (display_user_id $uid OR display_user_id IS NULL)
    AND display_entity='$entity'
    AND display_fieldorder='$fieldorder'
    ORDER BY display_user_id DESC";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_display_pref_level_update()');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $user_id = $obm_q->f('display_user_id');
    if ($user_id == '') {
      $user_id = 0;
    }
    $fieldname = $obm_q->f('display_fieldname');
    $fieldstatus = $obm_q->f('display_display');
    $current[$user_id]['name'] = $fieldname;
    $current[$user_id]['status'] = $fieldstatus;
  }

  // Get the default field for the new order (target)
  $query = "SELECT
    display_user_id,
    display_fieldname,
    display_display
    FROM DisplayPref
    WHERE display_user_id IS NULL
    AND display_entity='$entity'
    AND display_fieldorder='$new_level'";

  display_debug_msg($query, $cdg_sql, 'run_query_display_pref_level_update()');
  $obm_q->query($query);
  $obm_q->next_record();
  $target[0]['name'] = $obm_q->f('display_fieldname');
  $target[0]['status'] = $obm_q->f('display_display');

  // If a specific user entry exists
  if (isset($current[$u_id])) {

    // If new values to set same as default values, we drop specific user value
    if ( ($target[0]['name'] == $current[$u_id]['name'])
      && ($target[0]['status'] == $current[$u_id]['status']) ) {

        $query = "DELETE FROM DisplayPref
          WHERE display_user_id $uid 
          AND display_entity='$entity'
          AND display_fieldname='" . $target[0]['name'] . "'
          AND display_display='" . $target[0]['status'] . "'
          AND display_fieldorder='$fieldorder'";

        // Else we update the specific user value
      } else {

        $query = "UPDATE DisplayPref
          SET display_fieldorder='$new_level'
          WHERE display_user_id $uid 
          AND display_entity='$entity'
          AND display_fieldorder='$fieldorder'";
      }

  } else {
    $u_id = sql_parse_id($u_id);
    // Only default values, nothing to drop, we insert the new specific value
    $query = "INSERT INTO DisplayPref(
      display_user_id,
      display_entity,
      display_fieldname,
      display_fieldorder,
      display_display
    ) VALUES (
      $u_id,
      '$entity',
      '".$current[0]['name']."',
      '$new_level',
      '$fieldstatus')";
  }    

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_display_pref_level_update()');
  $obm_q->query($query);

}


///////////////////////////////////////////////////////////////////////////////
// Update the level of the field in the entity given identified by the order
// Parameters:
//   - $u_id       : user id
//   - $entity     : entity affected
//   - $fieldorder : actual position
//   - $new_level  : new position
///////////////////////////////////////////////////////////////////////////////
function run_query_display_pref_level_update2($u_id, $entity, $fieldorder, $new_level) {
  global $cdg_sql;
  
  $query = "SELECT
        display_user_id,
        display_fieldname,
        display_display,
        display_fieldorder
      FROM DisplayPref
      WHERE (display_user_id = '$u_id' OR display_user_id IS NULL)
        AND display_entity = '$entity'
  ";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_display_pref_level_update2()');
  $obm_q->query($query);
  
  $prefs[$u_id] = array();
  $prefs[0] = array();
  while ($obm_q->next_record()) {
  	$user_id = $obm_q->f('display_user_id');
  	if (!$user_id > 0) {
  	  $user_id = 0;
  	}
  	
    $row = array(
      'order' => $obm_q->f('display_fieldorder'),
      'name' => $obm_q->f('display_fieldname'),
      'status' => $obm_q->f('display_display'),
    );
    
    $prefs[$user_id][$obm_q->f('display_fieldorder')] = $row;
    $prefs[$user_id][$obm_q->f('display_fieldname')] = $row;
  }
  
  foreach (array(
    array($fieldorder, $new_level),
    array($new_level, $fieldorder),
  ) as $swap) {
    
    $current = $prefs[$u_id][$swap[0]];
    $default = $prefs[0][$swap[0]];
    
    // If a specific user entry exists
    if (!empty($current)) {
      $default = $prefs[0][$current['name']];
      
      // If new values to set same as default values, we drop specific user value
      if (!empty($default)
      && $current['name'] == $default['name']
  	  && $current['status'] == $default['status']
  	  && $default['order'] == $swap[1] ) {
        run_query_display_pref_delete($u_id, $entity, $current['name']);
  
      // Else we update the specific user value
      } else  {
        run_query_display_pref_update($u_id, $entity, $current['name'], '', $swap[1]);
      }
  
    } else if (!empty($default) && empty($prefs[$u_id][$default['name']])) {
      run_query_display_pref_insert($u_id, $entity, $default['name'], $default['status'], $swap[1]);
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Reset User parameters to default values
// Parameters:
//   - $user_id : user id (if ==  '', reset for all users)
///////////////////////////////////////////////////////////////////////////////
function reset_preferences_to_default($user_id='') {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  //---------------------------------------------------------------------------
  // Default User Preferences (tableUserObmPref)
  // We copy each entry with user id = 0 to the new user
  //---------------------------------------------------------------------------
  $uid = sql_parse_id($user_id, true);
  if ($user_id > 0) {
    $where_user = "userobmpref_user_id $uid ";
  } else if ($user_id == '') {
    $where_user = "userobmpref_user_id IS NOT NULL";
  } else {
    return false;
  }

  //-- Drop current user preferences
  $query = "DELETE FROM UserObmPref WHERE $where_user";
  display_debug_msg($query, $cdg_sql, 'reset_preferences_to_default()');
  $obm_q->query($query);

  //---------------------------------------------------------------------------
  // Default Display Preferences (table DisplayPref) : delete user values
  //---------------------------------------------------------------------------

  if ($user_id > 0) {
    $where_user = "display_user_id $uid ";
  } else if ($user_id == '') {
    $where_user = "display_user_id IS NOT NULL";
  } else {
    return false;
  }

  //-- Drop current user display preferences
  $query = "DELETE FROM DisplayPref WHERE $where_user";
  display_debug_msg($query, $cdg_sql, 'reset_preferences_to_default()');
  $obm_q->query($query);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the User's preferences
// Parameters:
//   - $u_id   : user id to retrieve the pref
// Returns:
//   - $prefs  : prefs[$option] : ['value'], ['notdefault']
///////////////////////////////////////////////////////////////////////////////
function get_user_pref($u_id) {
  global $cdg_sql;

  $query = "SELECT *
    FROM UserObmPref
    WHERE  userobmpref_user_id = $u_id OR (
      userobmpref_user_id IS NULL AND 
      userobmpref_option  NOT IN (SELECT userobmpref_option FROM UserObmPref WHERE userobmpref_user_id = $u_id))";

  display_debug_msg($query, $cdg_sql, 'get_user_pref()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // The query is sorted by user_id, hence default values first
  while ($obm_q->next_record()) {
    $user_id = $obm_q->f('userobmpref_user_id');
    $option = $obm_q->f('userobmpref_option');
    $value = $obm_q->f('userobmpref_value');

    if ($user_id != NULL) {
      // Some options do not have default value (eg: last visited entities)
      // Note if user value different from default value
      if ( ($prefs[$option]['value'] != $value) ) {
        $prefs[$option]['notdefault'] = true;
      }
      $prefs[$option]['value'] = $value;

      // Default values (user id = 0)
    } else {
      $prefs[$option]['value'] = $value;
    }
  }

  return $prefs;
}


///////////////////////////////////////////////////////////////////////////////
// Get One User's preference
// Parameters:
//   - $u_id   : user id to retrieve the pref
//   - $option : option
// Returns:
//   - $prefs  : prefs[user_id] : ['value'], where user_id in (0, $u_id)
///////////////////////////////////////////////////////////////////////////////
function get_one_user_pref($u_id, $option) {
  global $cdg_sql;

  $u_id = sql_parse_id($u_id, true);
  $query = "SELECT *
    FROM UserObmPref
    WHERE (userobmpref_user_id IS NULL OR userobmpref_user_id $u_id )
    AND userobmpref_option='$option'
    ORDER BY userobmpref_user_id"; 

  display_debug_msg($query, $cdg_sql, 'get_one_user_pref()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // The query is sorted by user_id, hence default values first
  while ($obm_q->next_record()) {
    $user_id = $obm_q->f('userobmpref_user_id');
    $value = $obm_q->f('userobmpref_value');
    // To store default preferences (as NULL can not be a key)
    if ($user_id == '') $user_id = 0;
    $prefs[$user_id]['value'] = $value;
  }

  return $prefs;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Store the new preference value
// Parameters:
//   - $user_id  : user Id
//   - $option   : preference name
//   - $value    : new preference value
///////////////////////////////////////////////////////////////////////////////
function update_user_pref($user_id, $option, $new_value='') {
  global $cdg_sql;

  $prefs = get_one_user_pref($user_id, $option);
  $default_value = $prefs[0]['value'];
  $user_value = $prefs[$user_id]['value'];
  $current_value = ($user_value != '' ? $user_value : $default_value);

  // if new_value not set new_value set to current_value
  if ($new_value === '') {
    $new_value = $current_value;
  }

  // if new field values = default values and user value exists, erase it
  if ($new_value == $default_value) {

    $query = "DELETE FROM UserObmPref
      WHERE userobmpref_user_id ".sql_parse_id($user_id, true)."
      AND userobmpref_option='$option'";
    display_debug_msg($query, $cdg_sql, 'update_user_pref()');
    $obm_q = new DB_OBM; 
    $obm_q->query($query);

  } else {

    // if user entry does not exist
    if (! isset($prefs[$user_id])) {
      run_query_user_pref_insert($user_id, $option, $new_value);
    } else {
      // user entry is different (=> update it)
      run_query_user_pref_update($user_id, $option, $new_value);
    }
  }

}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Insert a user preference
// Parameters:
//   - $user_id  : user Id
//   - $option   : preference name
//   - $value    : new preference value
///////////////////////////////////////////////////////////////////////////////
function run_query_user_pref_insert($user_id, $option, $value) {
  global $cdg_sql;
  $user_id = sql_parse_id($user_id);
  $query = "INSERT INTO UserObmPref (
    userobmpref_user_id,
    userobmpref_option,
    userobmpref_value)
    VALUES (
      $user_id,
      '$option',
      '$value')";

  display_debug_msg($query, $cdg_sql, 'run_query_user_pref_insert()');
  $obm_q = new DB_OBM; 
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Update a user preference
// Parameters:
//   - $user_id  : user Id
//   - $option   : preference name
//   - $value    : new preference value
///////////////////////////////////////////////////////////////////////////////
function run_query_user_pref_update($user_id, $option, $value) {
  global $cdg_sql;
  $user_id = sql_parse_id($user_id, true);
  $query = "UPDATE UserObmPref SET
    userobmpref_value='$value'
    WHERE userobmpref_user_id $user_id 
    AND userobmpref_option='$option'";

  display_debug_msg($query, $cdg_sql, 'run_query_user_pref_update()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the user's preferences into session
// This must be called in an open session (between page_open and page_close )
///////////////////////////////////////////////////////////////////////////////
function session_load_user_prefs() {
  global $obm, $last_v,$ccalendar_last_hour,$ccalendar_first_hour;
  global $set_timezone_default;

  // We reset $last_v in case of session persistence (changing user)
  $last_v = array();

  $prefs = get_user_pref($obm['uid']);

  if (is_array($prefs)) 
    while ( list($option, $info) = each($prefs) ) {
      $value = $info['value'];
      global $$option;
      $$option = $value;

      // Last visit management (if last_visit entry, get the associated text)
      if (substr($option, 0, 5) == 'last_') {
        $entity = substr($option, 5);
        $function_get_last_text = "get_last_${entity}_text";
        if (function_exists($function_get_last_text)) {
          $last_v[$entity]['id'] = $$option;
          $last_v[$entity]['text'] = $function_get_last_text($$option);
        }
        // Other preferences
      } else {
        $_SESSION["$option"] = $value;
      }
    }

  $_SESSION['last_v'] = $last_v;
  if (!isset($_SESSION['set_cal_first_hour'])) {
    $_SESSION['set_cal_first_hour'] = $ccalendar_first_hour;
  }
  if (!isset($_SESSION['set_cal_last_hour'])) {
    $_SESSION['set_cal_last_hour'] = $ccalendar_last_hour;
  }
  if(!isset($_SESSION['set_timezone'])) {
    $_SESSION['set_timezone'] = $set_timezone_default;
  }
}


//--- Login / Logout global functions ---------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Update the login date of the user
// Parameters:
//   - $id : UserObm Id 
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_update_lastaccess($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "UPDATE UserObm
    SET userobm_timelastaccess='".date("Y-m-d H:i:s")."'
    WHERE userobm_id $id ";

  display_debug_msg($query, $cdg_sql, 'run_query_userobm_update_lastaccess(2)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Action on ActiveUserObm and UserObm_Session Log during  //
// the logout
///////////////////////////////////////////////////////////////////////////////
function run_query_logout() {
  global $sess, $cdg_sql;

  // get the Active connexion
  $obm_q = new DB_OBM ; 
  $query = "SELECT *
    FROM ActiveUserObm 
    WHERE activeuserobm_sid = '".$sess->id."'";
  display_debug_msg($query, $cdg_sql, 'run_query_logout(1)');
  $obm_q->query($query) ;

  if ($obm_q->next_record()) {
    // Log the connexion in the session log
    run_query_log_session($obm_q);

    // drop the connexion from the active user session table
    $query = "DELETE FROM ActiveUserObm
      WHERE activeuserobm_sid = '".$obm_q->f('activeuserobm_sid')."'";
    display_debug_msg($query, $cdg_sql, 'run_query_logout(2)');
    $obm_q->query($query) ;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution - Insertion or update of the Session logs		     //
// during the logout						  	     //
///////////////////////////////////////////////////////////////////////////////
function run_query_log_session($obm_q) {
  global $cdg_sql;

  $uquery = sprintf("
    update UserObm_SessionLog set 
    userobm_sessionlog_session_name = '%s', 
    userobm_sessionlog_userobm_id = '%s', userobm_sessionlog_timeupdate = '%s',
    userobm_sessionlog_timecreate = '%s', 
    userobm_sessionlog_nb_connexions = userobm_sessionlog_nb_connexions + %d,
    userobm_sessionlog_lastpage = '%s', userobm_sessionlog_ip  = '%s' where
    userobm_sessionlog_sid='%s'",
    $obm_q->f('activeuserobm_session_name'), 
    $obm_q->f('activeuserobm_userobm_id'), $obm_q->f('activeuserobm_timeupdate'),
    $obm_q->f('activeuserobm_timecreate'), 
    $obm_q->f('activeuserobm_nb_connexions'),
    $obm_q->f('activeuserobm_lastpage'), $obm_q->f('activeuserobm_ip'),
    $obm_q->f('activeuserobm_sid'));

  $squery = sprintf("
    select count(*) from UserObm_SessionLog where userobm_sessionlog_sid='%s'",
      $obm_q->f('activeuserobm_sid'));

  $iquery = sprintf("
    insert into UserObm_SessionLog ( userobm_sessionlog_sid,
      userobm_sessionlog_session_name, 
      userobm_sessionlog_userobm_id, userobm_sessionlog_timeupdate,
      userobm_sessionlog_timecreate, userobm_sessionlog_nb_connexions,
      userobm_sessionlog_lastpage, userobm_sessionlog_ip ) 
      values ('%s', '%s', %d, '%s','%s', '%d', '%s', '%s')",
        $obm_q->f('activeuserobm_sid'), $obm_q->f('activeuserobm_session_name'), 
        $obm_q->f('activeuserobm_userobm_id'), $obm_q->f('activeuserobm_timeupdate'),
        $obm_q->f('activeuserobm_timecreate'), 
        $obm_q->f('activeuserobm_nb_connexions'),
        $obm_q->f('activeuserobm_lastpage'), $obm_q->f('activeuserobm_ip'));

  $obm_q2 = new DB_OBM ;
  display_debug_msg($uquery, $cdg_sql, 'run_query_log_session(1)');
  $obm_q2->query($uquery);
  if ( $obm_q2->affected_rows() == 0) {
    display_debug_msg($squery, $cdg_sql, 'run_query_log_session(2)');
    if ( $obm_q2->query($squery)
      && $obm_q2->next_record() && $obm_q2->f(0) == 0 ) {
        display_debug_msg($iquery, $cdg_sql, 'run_query_log_session(3)');
        $obm_q2->query($iquery);
      }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Function that Update a Last visited entry if needed
// Parameters:
//   - $entity : entity
//   - $id     : entity id
//   - $action : action called 
///////////////////////////////////////////////////////////////////////////////
function update_last_visit($entity, $id, $action) {
  global $obm;

  if ( ($id == $_SESSION['last_v'][$entity]['id']) && (strcmp($action,'delete')==0) ) {
    $_SESSION['last_v'][$entity]['id'] = 0;
    $_SESSION['last_v'][$entity]['text'] = 0;
  } else if ( ($id > 0 ) && ($_SESSION['last_v'][$entity]['id'] != $id) ) {
    $_SESSION['last_v'][$entity]['id'] = $id;
    update_user_pref($obm['uid'], "last_$entity", $id);
    $function_get_last_text = "get_last_${entity}_text";
    $_SESSION['last_v'][$entity]['text'] = $function_get_last_text($id);
  }

}


///////////////////////////////////////////////////////////////////////////////
// Get the user text from the user Id
// Parameters:
//   - $id : user id 
///////////////////////////////////////////////////////////////////////////////
function get_last_user_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT userobm_login FROM UserObm WHERE userobm_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_user_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('userobm_login');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Group name from the group Id
// Parameters:
//   - $id : group id 
///////////////////////////////////////////////////////////////////////////////
function get_last_group_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT group_name FROM UGroup WHERE group_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_group_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('group_name');
  }
  return;
}

///////////////////////////////////////////////////////////////////////////////
// Get the organizational chart text from the organizational chart id
// Parameters:
//   - $id : organizational chart id 
///////////////////////////////////////////////////////////////////////////////
function get_last_organizationalchart_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT organizationalchart_name 
      FROM OrganizationalChart WHERE organizationalchart_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_organizationalchart_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('organizationalchart_name');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get Entity infos
// Parameters:
//   - $id  : entity id
//   - $entity : entity type
// Returns:
//   - entity label, id, kind
///////////////////////////////////////////////////////////////////////////////
function get_entity_info($id, $entity) {
  global $cdg_sql, $obm;

  $e = array();
  $obm_q = new DB_OBM ;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, 'userobm_datebegin', 'datebegin');
  if($id == '') {
    $id = $obm['uid'];
    $entity = 'user';
  } elseif ($entiy == '') {
    $entiy = 'user';
  }
  $id = sql_parse_id($id, true);

  if ($entity == 'user') {

    $table_entity = 'UserObm';
    $c_id = 'userobm_id';
    $ctt[0]['type'] = 'field';
    $ctt[0]['value'] = 'userobm_lastname';
    $ctt[1]['type'] = 'string';
    $ctt[1]['value'] = ' ';
    $ctt[2]['type'] = 'field';
    $ctt[2]['value'] = 'userobm_firstname';
    $c_label = sql_string_concat($db_type, $ctt);
    $c_email = 'userobm_email';
    $c_domain = 'userobm_domain_id';
    $entity_table = "Select $c_label as entitydata_label, $c_email as entitydata_email, $c_domain as entitydata_domain_id, $c_id as entitydata_id, 'user' as entity_kind
      FROM UserObm WHERE userobm_id $id";
  } elseif ($entity == 'group') {
    $table_entity = 'UGroup';
    $c_id = 'group_id';
    $c_label = 'group_name';
    $c_email = 'group_email';
    $c_domain = 'group_domain_id';
    $entity_table = "Select $c_label as entitydata_label, $c_email as entitydata_email, $c_domain as entitydata_domain_id,v$c_id as entitydata_id, 'group' as entity_kind
      FROM UGroup WHERE group_id $id";
  } elseif ($entity == 'resource') {
    $table_entity = 'Resource';
    $c_id = 'resource_id';
    $c_label = 'resource_name';
    $c_email = 'NULL';
    $c_domain = 'resource_domain_id';
    $entity_table = "Select $c_label as entitydata_label, $c_email as entitydata_email, $c_domain as entitydata_domain_id, $c_id as entitydata_id, 'resource' as entity_kind
      FROM Resource WHERE resource_id $id";
  } elseif ($entity == 'mailshare') {
    $table_entity = 'MailShare';
    $c_id = 'mailshare_id';
    $c_label = 'mailshare_name';
    $c_email = 'mailshare_email';
    $c_domain = 'mailshare_domain_id';
    $entity_table = "Select $c_label as entitydata_label, $c_email as entitydata_email, $c_domain as entitydata_domain_id, $c_id as entitydata_id, 'mailshare' as entity_kind
      FROM MailShare WHERE mailshare_id $id";
  } elseif ($entity == 'contact') {
    $table_entity = 'Contact';
    $c_id = 'contact_id';
    $ctt[0]['type'] = 'field';
    $ctt[0]['value'] = 'contact_lastname';
    $ctt[1]['type'] = 'string';
    $ctt[1]['value'] = ' ';
    $ctt[2]['type'] = 'field';
    $ctt[2]['value'] = 'contact_firstname';
    $c_label = sql_string_concat($db_type, $ctt);
    $c_email = 'email_address';
    $c_domain = 'contact_domain_id';
    $entity_table = "Select $c_label as entitydata_label, $c_email as entitydata_email, $c_domain as entitydata_domain_id, $c_id as entitydata_id, 'mailshare' as entity_kind
      FROM Contact
      INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
      LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label = 'INTERNET;X-OBM-Ref1'
      WHERE contact_id $id";
  }

  $query = "SELECT Entity.*, domain_name as entity_domain
    FROM($entity_table) as Entity 
    LEFT JOIN Domain ON domain_id = entitydata_domain_id
    WHERE entitydata_id $id ";

  display_debug_msg($query, $cdg_sql, 'get_entity_info()');
  $obm_q->xquery($query);
  $obm_q->next_record();
  if (is_array($obm_q->Record)) {
    foreach ($obm_q->Record as $key => $user) {
      $small_key = str_replace('entitydata_', '', $key);
      $e[$small_key] = $user;
    }
  }  

  return $e;
}


//--- User and group global functions ---------------------------------------//


/**
 * Get private group list for one user
 * @param user id
 * @return array group [ids], [name] (label=>)
 **/
function get_private_groups($user_id) {
  global $cdg_sql;

  if ($user_id > 0) {
    $user_id = sql_parse_id($user_id, true);
    $query = "SELECT *
      FROM UGroup
      WHERE group_usercreate $user_id 
      AND group_privacy = 1";
    display_debug_msg($query, $cdg_sql, 'get_private_groups(user_id)');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $id = $obm_q->f('group_id');
      $name = $obm_q->f('group_name');
      $res[$id] = array('name' => "$name");
    }
  } else {
    $res = '';
  }

  return $res;
}


/**
 * Get users infos from the uid array given
 * Used by: agenda, todo
 * @param users : array of users id
 * @return array [ids], [entity] (label=>)
 **/
function get_userobm_from_ids($users) {
  global $cdg_sql;

  if (is_array($users)) {
    $where = '(0';
    foreach($users as $user) {
      $where .= ", $user";
    }
    $where .= ')';
    $query = "SELECT *
      FROM UserObm
      WHERE userobm_archive = 0
      AND userobm_id IN $where
      ORDER by userobm_lastname";
    display_debug_msg($query, $cdg_sql, 'get_userobm_from_ids()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $id = $obm_q->f('userobm_id');
      $lname = $obm_q->f('userobm_lastname');
      $fname = $obm_q->f('userobm_firstname');
      $res['ids'][] = $id;
      $res['entity'][$id] = array('label' => "$lname $fname");
    }
  } else {
    $res = '';
  }

  return $res;
}

/**
 * Get users infos from the uid array given
 * Used by: agenda
 * @param contacts : array of contacts id
 * @return array [ids], [entity] (label=>, email_address=>)
 **/
function get_contact_from_ids($contacts, $fetch_email = true) {
  global $cdg_sql;

  if (is_array($contacts)) {
    $where = '(0';
    foreach($contacts as $contact) {
      $where .= ", $contact";
    }
    $where .= ')';
    $query = "SELECT *
      FROM Contact
      INNER JOIN ContactEntity ON contactentity_contact_id = contact_id ";
    if ($fetch_email) {
      $query.= "INNER JOIN Email ON email_entity_id = contactentity_entity_id ";
    }
    $query.= "WHERE contact_archive = 0
      AND contact_id IN $where
      ORDER by contact_lastname";
    display_debug_msg($query, $cdg_sql, 'get_contact_from_ids()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $id = $obm_q->f('contact_id');
      $lname = $obm_q->f('contact_lastname');
      $fname = $obm_q->f('contact_firstname');
      $mail  = $obm_q->f('email_address');
      $res['ids'][] = $id;
      $res['entity'][$id] = array('label' => "$lname $fname", 'email_address' => $mail);
    }
  } else {
    $res = '';
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the Id of one User from its login
// Parameters:
//   - $login : login
// Returns:
//   - user's Id
// Used by: user, client import
///////////////////////////////////////////////////////////////////////////////
function get_user_id($login) {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');

  if (isset($login)) {
    $query = "SELECT userobm_id
    FROM UserObm
    WHERE userobm_login='$login' $multidomain";

    display_debug_msg($query, $cdg_sql, 'get_user_id()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('userobm_id');

  } else {
    return '';
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get User infos
// Parameters:
//   - $id  : user id
// Returns:
//   - user hash values
// Used by: time only !!
///////////////////////////////////////////////////////////////////////////////
function get_user_info($id='') {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM ;
  $db_type = $obm_q->type;
  $datebegin = sql_date_format($db_type, 'userobm_datebegin', 'datebegin');

  if ($id == '') $id = $obm['uid'];
  $id = sql_parse_id($id, true);
  $query = "SELECT *,
    domain_name,
    $datebegin
    FROM UserObm
    INNER JOIN Domain ON userobm_domain_id = domain_id      
    WHERE userobm_id $id ";

  display_debug_msg($query, $cdg_sql, 'get_user_info()');
  $obm_q->query($query);
  $obm_q->next_record();
  $match = array();
  if (is_array($obm_q->Record)) {
    foreach ($obm_q->Record as $key => $user) {
      if(!isset($match[$key])) {
        $match[$key] = str_replace('userobm_', '', $key);
      }
      if ($match[$key] == 'email' ) {
        $user = get_entity_email($user, $obm_q->f('domain_name'));
      }      
      $u[$match[$key]] = $user;
    }
  }  
  $u['datebegin'] = array();
  $u['datebegin']['iso'] = $obm_q->f('userobm_datebegin');
  $u['datebegin']['unix_timestamp'] = $obm_q->f('datebegin');

  return $u;
}

/**
 * Updates, then retrieves a trust token from the database.
 * 
 * @param $username The username to get the token for.
 * 
 * @return String The trust token.
 */
function get_trust_token($username){
	global $cdg_sql;
	
	$obm_q = new DB_OBM ;
	$deleteQuery = "DELETE FROM TrustToken WHERE login='$username'";
	$insertQuery = "INSERT INTO TrustToken (login, token) VALUES ('$username', UUID())";
	$query = "SELECT token FROM TrustToken WHERE login='$username'";
	
	display_debug_msg($query, $cdg_sql, 'get_trust_token()');
	
	$obm_q->query($deleteQuery);
	$obm_q->query($insertQuery);
	$obm_q->query($query);
	$obm_q->next_record();

	if (is_array($obm_q->Record)) {
		$token = $obm_q->Record[0];
	}
	
	return $token;
}


/**
 * Get the contact infos
 * @param $con_id : contact id
 * @return : $c hash with contact infos
 **/
function get_contact_info($con_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('contact');
  $con_id = sql_parse_id($con_id, true);
  $query = "SELECT
    contact_company_id,
    contact_usercreate,
    contact_lastname,
    contact_firstname
    FROM Contact
    WHERE contact_id $con_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_contact_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $c['company_id'] = $obm_q->f('contact_company_id');
  $c['usercreate'] = $obm_q->f('contact_usercreate');
  $c['lastname'] = $obm_q->f('contact_lastname');
  $c['firstname'] = $obm_q->f('contact_firstname');

  return $c;
}


/**
 * Get the company infos
 * @param $con_id : company id
 * @return : $c hash with company infos
 **/
function get_company_info($con_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('company');
  $con_id = sql_parse_id($con_id, true);
  $query = "SELECT
    company_id,
    company_usercreate,
    company_name
    FROM Company
    WHERE company_id $con_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_company_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $c['id'] = $obm_q->f('company_id');
  $c['usercreate'] = $obm_q->f('company_usercreate');
  $c['name'] = $obm_q->f('company_name');

  return $c;
}


/**
 * Format entities email. 
 * 
 * @param mixed $mail Brut email(s) (if not set use id and entity to retrieve
 * all informations including domain name)
 * @param mixed $domain current domain name (if not set use obm['domain_name'])
 * @param mixed $first_only return only the first entity email if true, all if
 * false
 * @param string $separator if null return an array, if not null return a 
 * string containing all email, separated by '$separator'
 * @param mixed $id Entity id, needed only if $mail is null 
 * @param string $entity Entity kind, needed only if $mail is null
 * @access public
 * @return array | string
 */
function get_entity_email($mail=null, $domain=null, $first_only=true, $separator=', ', $id=null, $entity='user') {
  global $obm;
  if(is_null($mail)) {
    if(is_null($id)) {
      return false;
    }
    if(is_null($id)) {
      $id = $obm['uid'];
    }
    $e = get_entity_info($id, $entity);
    $mail = $e['email'];
    $domain = $e['domain_name'];
  }

  if(is_null($domain) && !$obm['domain_global']) {
    $domain = $obm['domain_name'];
  }    
  $mail = explode("\r\n",$mail);
  if($first_only && count($mail) > 1) {
    $mail = array(array_shift($mail));
  }
  $emails = array();
  foreach($mail as $key => $email) {
    if(strpos($email,'@') === false && !empty($email)) {
      $emails[] = $email.'@'.$domain;
    } elseif(!empty($email)) {
      $emails[] = $email;
    }
  }
  if(!is_null($separator)) {
    return implode($separator,$emails);
  } else {
    return $emails;
  }
}
///////////////////////////////////////////////////////////////////////////////
// Userobm query execution : All users (or the one for the id given)
// Parameters:
//   - $id (optionnal) : UserObm Id 
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm($id='') {
  global $cdg_sql;

  if ($id) {
    $id = sql_parse_id($id, true);
    $where = "WHERE userobm_id $id ";
  }
  $query = "SELECT *
    FROM UserObm
    $where
    ORDER BY userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_userobm()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Active Userobm query execution : All users not archived
// Parameters:
//   - $ids (optionnal) : array of UserObm Id to add to the result
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_active($ids='') {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');
  if (is_array($ids)) {
    $where_add = '';
    $nb_add = count($ids);
    for ($i=0; $i<$nb_add; $i++) {
      if ($ids[$i] != '') {
        $id = sql_parse_id($ids[$i], true);
        $where_add .= " OR userobm_id $id ";
      }
    }
  }

  $query = "SELECT userobm_id,
    userobm_lastname,
    userobm_firstname
    FROM UserObm
    WHERE userobm_archive != 1 
    $where_add
    $multidomain
    ORDER BY userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_userobm_active()');

  return $obm_q;
}


/**
 * guess_name_from_email 
 * 
 * @param mixed $email 
 * @access public
 * @return void
 */
function guess_name_from_email($email) {
  $first_name = '';
  $last_name  = '';

  if(preg_match('/(.+?)\.(.+?)@/', $email, $match)) {
    $first_name = ucfirst(strtolower($match[1]));
    $last_name  = strtoupper($match[2]);
  } elseif(preg_match('/([^.]+)@/', $email, $match)) {
    $last_name = ucfirst(strtolower($match[1]));
  }

  return array( $first_name, $last_name );
}

///////////////////////////////////////////////////////////////////////////////
// Active Userobm query execution : All users not archived
// Parameters:
//   - $ids (optionnal) : array of UserObm Id to add to the result
// Return:
//   - Database Object : userobm list
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_responsable($table='',$fields='') {
  global $cdg_sql;

  $multidomain = sql_multidomain('userobm');
  if(is_array($fields)) {
    $join = "JOIN $table ON 1=0 ";
    foreach ($fields as $field) {
      $field = sql_parse_id($field, true);
      $join .= "OR userobm_id $field ";
    }
  }
  $query = "SELECT userobm_id,
    userobm_lastname,
    userobm_firstname
    FROM UserObm
    $join
    WHERE
    1=1 
    $multidomain
    GROUP BY  userobm_id,
    userobm_lastname,
    userobm_firstname
    ORDER BY userobm_lastname"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_userobm_responsable()');

  return $obm_q;
}


/**
 * Get the group manager
 * @param $g_id group id
 * @return integer group_manager_id
 * @access public
 */
function run_query_global_group_manager($g_id) {
  global $cdg_sql;

  $g_id = sql_parse_id($g_id, true);
  $query = "SELECT group_manager_id FROM UGroup
    WHERE group_id $g_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_global_group_manager()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $return = $obm_q->f('group_manager_id');

  return $return;
}


///////////////////////////////////////////////////////////////////////////////
// Return the group infos
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function get_group_info($id) {
  global $cdg_sql;

  if ($id == '') {
    return false;
  }
  $multidomain = sql_multidomain('group');
  $id = sql_parse_id($id, true);
  $query = "SELECT * FROM UGroup WHERE group_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_group_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $g['domain_id'] = $obm_q->f('group_domain_id');
  $g['usercreate'] = $obm_q->f('group_usercreate');
  $g['name'] = $obm_q->f('group_name');
  $g['system'] = $obm_q->f('group_system');
  $g['gid'] = $obm_q->f('group_gid');
  $g['privacy'] = $obm_q->f('group_privacy');
  $g['samba'] = $obm_q->f('group_samba');
  $g['delegation'] = $obm_q->f('group_delegation');
  if ($GLOBALS['cgp_use']['service']['mail']) {
    $g['email'] = $obm_q->f('group_email').'@'.$obm_q->f('domain_name');
  } else {
    $g['email'] = $obm_q->f('group_email');
  }
  return $g;
}


///////////////////////////////////////////////////////////////////////////////
// Return the resource infos
// Parameters:
//   - $id : resource id
///////////////////////////////////////////////////////////////////////////////
function get_resource_info($id) {
  global $cdg_sql;

  if ($id == '') {
    return false;
  }
  $multidomain = sql_multidomain('resource');
  $id = sql_parse_id($id, true);
  $query = "SELECT * FROM Resource WHERE resource_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_resource_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $r['domain_id'] = $obm_q->f('resource_domain_id');
  $r['usercreate'] = $obm_q->f('resource_usercreate');
  $r['name'] = $obm_q->f('resource_name');
  $r['delegation'] = $obm_q->f('resource_delegation');
  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Return the resource infos
// Parameters:
//   - $id : resource id
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_info($id) {
  global $cdg_sql;

  if ($id == '') {
    return false;
  }
  $multidomain = sql_multidomain('rgroup');
  $id = sql_parse_id($id, true);
  $query = "SELECT * FROM RGroup WHERE rgroup_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_resourcegroup_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  $r['domain_id'] = $obm_q->f('rgroup_domain_id');
  $r['usercreate'] = $obm_q->f('rgroup_usercreate');
  $r['name'] = $obm_q->f('rgroup_name');
  $r['delegation'] = $obm_q->f('rgroup_delegation');
  return $r;
}

///////////////////////////////////////////////////////////////////////////////
// Get the users ID list from the given group
// Recursive search to handle group trees
// Parameters:
//   - $group_id : Group id
// Returns:
//   array of user ids
///////////////////////////////////////////////////////////////////////////////
function get_all_users_id_from_group($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $users = array();
  $g_id = sql_parse_id($g_id, true);
  // Groups members of this group
  $query = "SELECT groupgroup_child_id as child_id
    FROM GroupGroup
    WHERE groupgroup_parent_id $g_id ";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, 'get_all_users_id_from_group(1)');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $child_id = $obm_q->f('child_id');
    $gusers = get_all_users_id_from_group($child_id);
    $users = array_merge($users, $gusers);
  }

  // Not archived users members of this group
  $join = "LEFT JOIN UserObm on userobm_id=userobmgroup_userobm_id";
  $where_user_not_archived = "AND userobm_archive != 1";
  $query = "SELECT userobmgroup_userobm_id as user_id
     FROM UserObmGroup $join
     WHERE userobmgroup_group_id $g_id $where_user_not_archived";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, 'get_all_users_id_from_group(2)');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $u_id = $obm_q->f('user_id');
    if (! in_array($u_id, $users)) {
      array_push($users, $u_id);
    }
  }
  $users = array_unique($users);

  return $users;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contacts ID list from the given group
// Recursive search to handle group trees
// Parameters:
//   - $group_id : Group id
// Returns:
//   array of contact ids
///////////////////////////////////////////////////////////////////////////////
function get_all_contacts_id_from_group($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $contacts = array();
  $g_id = sql_parse_id($g_id, true);
  // Groups members of this group
  $query = "SELECT groupgroup_child_id as child_id
    FROM GroupGroup
    WHERE groupgroup_parent_id $g_id ";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, 'get_all_contacts_id_from_group(1)');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $child_id = $obm_q->f('child_id');
    $gcontacts = get_all_contacts_id_from_group($child_id);
    $contacts = array_merge($contacts, $gcontacts);
  }

  // Not archived users members of this group
  $query = "SELECT contact_id
    FROM contactgroup
    WHERE group_id $g_id";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, 'get_all_contacts_id_from_group(2)');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $c_id = $obm_q->f('contact_id');
    if (! in_array($c_id, $contacts)) {
      array_push($contacts, $c_id);
    }
  }
  $contacts = array_unique($contacts);

  return $contacts;
}


/**
 * Get all parent groups of a group
 * @param $g_id group id
 * @return false if problem
 * @access public
 **/
function get_all_parent_group_of_group($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $groups = array($g_id);
  $g_id = sql_parse_id($g_id, true);
  // Groups parents of this group
  $query = "SELECT groupgroup_parent_id as parent_id
    FROM GroupGroup
    WHERE groupgroup_child_id $g_id ";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, 'get_all_parent_group_of_group()');
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $parent_id = $obm_q->f('parent_id');
    $pgroups = get_all_parent_group_of_group($parent_id);
    $groups = array_merge($groups, $pgroups);
  }

  return $groups;
}


/**
 * Get all users of a group from the usergroup internal infos 
 * @param $g_id group id
 * @param $info if true, get lastname and firstname too else get only id
 * @param $uids (optionnal) : array of UserObm Id to add to the result
 * @param $sort (optionnal) : sort field
 * @return array of users infos (id=>infos) or array of users ids
 * @access public
 **/
function of_usergroup_get_group_users($g_id, $info=false, $uids='', $order='') {
  global $cdg_sql;

  if (! ($g_id > 0) && empty($uids)) {
    return false;
  }
  $g_id = sql_parse_id($g_id, true);
  if ($info) {
    $select_user = ',
      userobm_login,
      userobm_firstname,
      userobm_lastname,
      userobm_title,
      userobm_phone,
      userobm_mobile,
      userobm_description,
      userobm_email';
  }
  if ($order != '') {
    $order_clause = "ORDER BY $order";
  }
  if (is_array($uids) && count($uids)>0) {
    foreach ($uids as $u_id) {
      $u_id = sql_parse_id($u_id, true);
      $where_uids .= " $or userobm_id $u_id ";
      $or = "OR";
    }
    $union = "UNION
      SELECT userobm_id as user_id
      $select_user
      FROM UserObm
      WHERE $where_uids";
  }

  // Users members of this group
  $query = "SELECT of_usergroup_user_id as user_id
    $select_user
    FROM of_usergroup LEFT JOIN UserObm ON of_usergroup_user_id=userobm_id
    WHERE userobm_archive != 1
    AND of_usergroup_group_id $g_id 
    $union
    $order_clause";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, 'of_usergroup_get_group_users()');
  $obm_q->query($query);

  $users = array();
  while ($obm_q->next_record()) {
    $u_id = $obm_q->f('user_id');
    if ($info) {
      $lastname = $obm_q->f('userobm_lastname');
      $firstname = $obm_q->f('userobm_firstname');
      $login = $obm_q->f('userobm_login');
      $title = $obm_q->f('userobm_title');
      $phone = $obm_q->f('userobm_phone');
      $mobile = $obm_q->f('userobm_mobile');
      $email = $obm_q->f('userobm_email');
      $description = $obm_q->f('userobm_description');
      $users[$u_id] = array('id' => $u_id,
        'login' => $login,
        'lastname' => $lastname,
        'firstname' => $firstname,
        'title' => $title,
        'phone' => $phone,
        'mobile' => $mobile,
        'email' => $email,
        'description' => $description);
    } else {
      array_push($users, $u_id);
    }
  }

  return $users;
}


/**
 * Update one group internal user mapping
 * @param $g_id group id
 * @return false if problem
 * @access public
 **/
function of_usergroup_update_group_node($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $multidomain = sql_multidomain('userobm');
  $users_id = get_all_users_id_from_group($g_id);
  $ret = false;
  $g_id_bis = sql_parse_id($g_id, true);
  // Delete current internal association
  $query = "DELETE FROM of_usergroup
    WHERE of_usergroup_group_id $g_id_bis ";
  display_debug_msg($query, $cdg_sql, 'of_usergroup_update_group_node(delete)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // If group has users, Insert new internal association
  if (is_array($users_id) && count($users_id) > 0) {
    $query = "INSERT INTO of_usergroup (
      of_usergroup_group_id,
      of_usergroup_user_id
    ) VALUES (%d,%d)";
    foreach ($users_id as $u_id) {
      if(is_numeric($g_id) && $g_id > 0 && is_numeric($u_id) && $u_id > 0) {
        display_debug_msg(sprintf($query,$g_id,$u_id), $cdg_sql, 'of_usergroup_update_group_node(insert)');
        $ret = $obm_q->query(sprintf($query,$g_id,$u_id));
      }
    }
  } else {
    $ret = true;
  }

  return $ret;
}


/**
 * Update internal user mapping for a group and its top hierarchy
 * @param $g_id group id
 * @return false if problem
 * @access public
 */
function of_usergroup_update_group_hierarchy($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  // If group not public, we return
  //  $g = get_group_info($g_id);
  //  if ($g['privacy'] != 0) {
  //    return true;
  //  }

  $groups = get_all_parent_group_of_group($g_id);
  $ret = false;
  foreach ($groups as $group_id) {
    $ret = of_usergroup_update_group_node($group_id);
  }

  return $ret;
}


/**
 * Delete a user from of_usergroup.
 * Beware if used under unchecked circuntances this might
 * break the UGroup/of_usergroup sync
 *
 * @param $g_id group id
 * @return false if problem
 * @access public
 */
function of_usergroup_delete_user_hierarchy($u_id, $is_array=false) {
  global $cdg_sql;
  
  
  if (! ($u_id > 0) && !$is_array) {
    return false;
  }
  
  // Delete current internal association
  if (!$is_array) {
    $query = "DELETE FROM of_usergroup WHERE of_usergroup_user_id=$u_id";
  } else {
    $query = "DELETE FROM of_usergroup WHERE of_usergroup_user_id IN (".implode(', ', $u_id).")";
  }
  display_debug_msg($query, $cdg_sql, 'of_usergroup_delete_user_hierarchy(delete)');
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);
  
  return $ret;
}


/**
 * Reset the user hierarchy and rebuild it.
 * This is a performance killer
 *
 * @param $g_id group id
 * @return false if problem
 * @access public
 */
function of_usergroup_update_user_hierarchy($u_id, $is_array=false) {
   global $cdg_sql;
   
   if (! ($u_id > 0) && !$is_array) {
     return false;
   }
   
   if (!$is_array) {
     $query = "SELECT userobmgroup_group_id FROM UserObmGroup WHERE userobmgroup_userobm_id=$u_id";
   } else {
     $query = "SELECT userobmgroup_group_id FROM UserObmGroup WHERE userobmgroup_userobm_id IN (".implode(', ', $u_id).")";
   }
   display_debug_msg($query, $cdg_sql, 'of_usergroup_update_user_hierarchy(delete)');
   $obm_q = new DB_OBM;
   $obm_q->query($query);
   while($obm_q->next_record()) {
     $g_id = $obm_q->f('userobmgroup_group_id');
     of_usergroup_update_group_hierarchy($g_id);
   }
   
   return $ret;
}


/**
 * Update one group internal contact mapping
 * @param $g_id group id
 * @return false if problem
 * @access public
 **/
function of_contactgroup_update_group_node($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $multidomain = sql_multidomain('contact');
  $contacts_id = get_all_contacts_id_from_group($g_id);
  $ret = false;
  $g_id_bis = sql_parse_id($g_id, true);
  // Delete current internal association
  $query = "DELETE FROM _contactgroup
    WHERE group_id $g_id_bis ";
  display_debug_msg($query, $cdg_sql, 'of_contactgroup_update_group_node(delete)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // If group has users, Insert new internal association
  if (is_array($contacts_id) && count($contacts_id) > 0) {
    $query = "INSERT INTO _contactgroup (
      group_id,
      contact_id
    ) VALUES (%d,%d)";
    foreach ($contacts_id as $c_id) {
      if(is_numeric($g_id) && $g_id > 0 && is_numeric($c_id) && $c_id > 0) {
        display_debug_msg(sprintf($query,$g_id,$c_id), $cdg_sql, 'of_contactgroup_update_group_node(insert)');
        $ret = $obm_q->query(sprintf($query,$g_id,$c_id));
      }
    }
  } else {
    $ret = true;
  }

  return $ret;
}

/**
 * Update internal contact mapping for a group and its top hierarchy
 * @param $g_id group id
 * @return false if problem
 * @access public
 */
function of_contactgroup_update_group_hierarchy($g_id) {
  global $cdg_sql;

  if (! ($g_id > 0)) {
    return false;
  }

  $groups = get_all_parent_group_of_group($g_id);
  $ret = false;
  foreach ($groups as $group_id) {
    $ret = of_contactgroup_update_group_node($group_id);
  }

  return $ret;
}


/**
 * Delete a contact from _contactgroup.
 * Beware if used under unchecked circuntances this might
 * break the UGroup/_contactgroup sync
 *
 * @param $g_id group id
 * @return false if problem
 * @access public
 */
function of_contactgroup_delete_contact_hierarchy($c_id, $is_array=false) {
  global $cdg_sql;
  
  
  if (! ($c_id > 0) && !$is_array) {
    return false;
  }
  
  // Delete current internal association
  if (!$is_array) {
    $query = "DELETE FROM _contactgroup WHERE contact_id=$c_id";
  } else {
    $query = "DELETE FROM _contactgroup WHERE contact_id IN (".implode(', ', $c_id).")";
  }
  display_debug_msg($query, $cdg_sql, 'of_contactgroup_delete_contact_hierarchy(delete)');
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);
  
  return $ret;
}


//--- Mail global functions -------------------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Send a Mail to an user set in mime format (one attachment)
// If recipient list is empty, no mail is sent
// Parameters:
//  - $subject    : mail subject
//  - $message    : mail content
//  - $recipients : Id Recipients array
//  - $r_addr     : adresses array to add to recipients
//  - $setting    : recipient setting to check (set_mail, set_mail_participant)
//  - $force      : Mail should be forced (all user even with no set_mail)
//  - $h_file     : Attachments (array of ('name'=> '', 'type=''))
///////////////////////////////////////////////////////////////////////////////
function send_mail($subject, $message, $recipients, $r_addr, $setting='set_mail', $force='', $h_file='') {
  $send_q = run_query_get_sender();
  $sender = get_entity_email($send_q->f('userobm_email'), $send_q->f('domain_name'));
  return send_mail_from($subject, $message, $sender, $recipients, $r_addr, $setting, $force, $h_file);
}


///////////////////////////////////////////////////////////////////////////////
// Send a Mail to an user set in mime format (one attachment)
// If recipient list is empty, no mail is sent
// Parameters:
//  - $subject    : mail subject
//  - $message    : mail content
//  - $sender     : mail from
//  - $recipients : Id Recipients array
//  - $r_addr     : adresses array to add to recipients
//  - $setting    : recipient setting to check (set_mail, set_mail_participant)
//  - $force      : Mail should be forced (all user even with no set_mail)
//  - $h_file     : Attachments (array of ('name'=> '', 'type=''))
///////////////////////////////////////////////////////////////////////////////
function send_mail_from($subject, $message, $sender, $recipients, $r_addr, $setting='set_mail', $force='', $h_file='') {
  global $cgp_mail_enabled;
  global $obm_version, $cgp_use; 

  $message = stripslashes($message);
  if (! $cgp_mail_enabled) {
    return 0;
  }

  if (count($recipients) < 1 && count($r_addr) < 1) {
    return 0;
  }

  $reci_q = run_query_get_recipients($recipients, $setting, $force);


  // Construct recipient list
  if (is_object($reci_q)) {
    while ($reci_q->next_record()) {
      $reci = $reci_q->f('userobm_email');

      // If AliaSuite, construct recipient address.
      $reci = get_entity_email($reci, $reci_q->f('domain_name'));

      if ($reci_list == '') {
        $reci_list = $reci;
      } else {
        $reci_list .= " , $reci";
      }
    }
  }

  // Add addresses given to recipient list
  if (is_array($r_addr)) {
    foreach($r_addr as $reci) {
      if ($reci_list == '') {
        $reci_list = $reci;
      } else {
        $reci_list .= " , $reci";
      }
    }
  }

  // Headers
  $boundary = '--' . md5( uniqid('myboundary') );

  $headers .= 'From: '.$sender."\n";

  // If attachements are provided
  $headers.= "MIME-Version: 1.0\n"; 
  if ($h_file != '') {
    $headers .= "Content-Type: multipart/mixed; boundary=\"$boundary\"\n"; 
  } else {
    // Commented as this breaks mail encoding
    $headers .= "Content-Type: text/plain; charset=UTF-8\n";
    $headers .= "Content-Transfer-Encoding: 8bit\n";
  } 
  $headers .= "X-Mailer: OBM $obm_version\n";

  // Message 
  $type = array(
    'gif'  => 'image/gif',
    'jpg'  => 'image/jpeg',
    'jpeg' => 'image/jpeg',
    'jpe'  => 'image/jpeg',
    'bmp'  => 'image/bmp',
    'png'  => 'image/png',
    'tif'  => 'image/tiff',
    'tiff' => 'image/tiff',
    'swf'  => 'application/x-shockwave-flash',
    'doc'  => 'application/x-msword',
    'xls'  => 'application/x-msexcel',
    'ics'  => 'text/calendar',
    'r'  => '"application/x-unknown-content-type"'
  );
  $priorities = array( '1 (Highest)', '2 (High)', '3 (Normal)', '4 (Low)', '5 (Lowest)' );
  $priority = $priorities[3];
  $body = $message;
  $sep = chr(13) . chr(10);

  // message
  $msg = '';
  if ($h_file !='') {
    $msg .= "\n--$boundary\n";
    $msg .= "Content-Type: text/plain; charset=UTF-8\n";
    $msg .= "Content-Transfer-Encoding: 8bit\n\n";
    $msg .= $body."\n\n";    
    for ($i=0;$i<count($h_file);$i++){
      $filename=$h_file[$i]['filename'];
      $name=$h_file[$i]['name'];
      $ctype=$type[$h_file[$i]['type']];

      $msg .= "--$boundary\n";
      $msg .= "Content-Type: $ctype; charset=UTF-8;name=\"$name\"\n";
      $msg .= "Content-Transfer-Encoding: 8bit\n";
      $msg .= "Content-Disposition: inline; filename=\"$name\"\n\n";
      $linesz = filesize($filename)+1;
      $fp = fopen($filename, 'r');
      $content = fread($fp,$linesz);
      fclose($fp);
      $msg .= $sep.$content;
    }
  } else {
    $msg = $message; 
  }
  if ($reci_list != '') {
    // as we don't have a mean to encode the subject, we drop its accentsr
    mb_internal_encoding('UTF-8');
    $subject = mb_encode_mimeheader($subject, "UTF-8", "Q","\n");
    mail ($reci_list, $subject, $msg, $headers);         
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get user todos 
///////////////////////////////////////////////////////////////////////////////
function run_query_get_todos() {

  $uid = sql_parse_id($GLOBALS['obm']['uid'], true);
  $query = "SELECT
      event_id,
      event_title,
      event_priority,
      eventlink_percent,
      #ADDSECONDS(event_date, event_duration) as event_deadline
    FROM EventLink
      INNER JOIN Event on eventlink_event_id=event_id
      INNER JOIN UserEntity on eventlink_entity_id=userentity_entity_id
    WHERE event_type='VTODO'
      AND userentity_user_id $uid
      AND eventlink_percent < 100
    ORDER BY event_deadline ASC";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get recipients mail addresses
// If no recipient given, no list returned
// Parameters:
//  - $recipients : Id Recipients array
//  - $setting    : recipient setting to check (set_mail, set_mail_participant)
//  - $force      : Mail should be forced (all user even with no set_mail)
// Returns: DB with email recipients, or false if no valid recipients
///////////////////////////////////////////////////////////////////////////////
function run_query_get_recipients($recipients, $setting='set_mail', $force) {
  global $cdg_sql;

  if (($setting == 'set_mail') || ($setting == 'set_mail_participant')) {
    $set_mail = $setting;
  } else {
    $set_mail = 'set_mail';
  }

  if ($force != 1) {
    $mail_filter = "
      AND (up1.userobmpref_value = 'yes' OR
      (up2.userobmpref_value = 'yes' AND up1.userobmpref_value IS NULL))";
    $join = "LEFT JOIN UserObmPref as up1 ON up1.userobmpref_user_id=userobm_id
      AND up1.userobmpref_option = '$set_mail'
      LEFT JOIN UserObmPref as up2 on up2.userobmpref_user_id IS NULL
      AND up2.userobmpref_option = '$set_mail'";
  }
  $coma = '';
  foreach($recipients as $recipient) {
    if ($recipient) {
      $user_list .= $coma.$recipient;
      $coma = ',';
    }
  }
  if ($user_list != '') {
    $user_in = "userobm_id IN ($user_list) AND";
  } else {
    return false;
  }

  $query = "SELECT 
    userobm_email, userobm_lastname, userobm_firstname, domain_name
    FROM UserObm 
    LEFT JOIN Domain on userobm_domain_id = domain_id
    $join
    WHERE $user_in
    userobm_email != ''
    $mail_filter";

  display_debug_msg($query, $cdg_sql, 'run_query_get_recipients()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get resources info 
// Parameters:
//  - $resources : Id Resources array
// Returns: DB with resources info 
///////////////////////////////////////////////////////////////////////////////
function run_query_get_resources_info($resources) {
  global $cdg_sql;

  $coma = '';
  foreach($resources as $resource) {
    if ($resource) {
      $res_list .= $coma.$resource;
      $coma = ',';
    }
  }
  if ($res_list != '') {
    $res_in = "resource_id IN ($res_list)";
  } else {
    return false;
  }

  $query = "SELECT resource_name, resource_description
    FROM Resource 
    WHERE $res_in";

  display_debug_msg($query, $cdg_sql, 'run_query_get_resources_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get sender mail address
///////////////////////////////////////////////////////////////////////////////
function run_query_get_sender() {
  global $obm, $cdg_sql;

  $uid = sql_parse_id($obm['uid'], true);
  $query = "SELECT 
    userobm_email, userobm_lastname, userobm_firstname, domain_name
    FROM UserObm
    LEFT JOIN Domain ON userobm_domain_id = domain_id
    WHERE userobm_id $uid ";

  display_debug_msg($query, $cdg_sql, 'run_query_get_sender()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


//--- Others global functions -----------------------------------------------//


///////////////////////////////////////////////////////////////////////////////
// Get the module list
// Return : $modules : array of modules
// Used by: admin_code, admin_lang
///////////////////////////////////////////////////////////////////////////////
function get_modules_array() {
  global $path;

  // Module dirs to exclude
  $exclude = array('.', '..', 'CVS');
  $modules = array();

  $mod_dir = dir("$path");
  while ($entry=$mod_dir->read()) {
    if (! in_array ($entry, $exclude) && is_dir($mod_dir->path."/".$entry)) {
      array_push($modules, $entry);
    }
  }
  $mod_dir->close();
  sort($modules);

  return $modules;
}


///////////////////////////////////////////////////////////////////////////////
// Create a file with a unique filename 
// Return 
//   - false if file creation is impossible 
//   - unique filename if file is created
///////////////////////////////////////////////////////////////////////////////
function secure_tmpname($postfix = '.tmp', $prefix = 'tmp', $dir = null) {
  // validate arguments
  if (! (isset($postfix) && is_string($postfix))) {
    return false;
  }
  if (! (isset($prefix) && is_string($prefix))) {
    return false;
  }
  if (! isset($dir)) {
    $dir = getcwd();
  }

  // find a temporary name
  $tries = 1;
  do {
    // get a known, unique temporary file name
    $sysFileName = tempnam($dir, $prefix);
    if ($sysFileName === false) {
      return false;
    }

    // tack on the extension
    $newFileName = $sysFileName . $postfix;
    if ($sysFileName == $newFileName) {
      return $sysFileName;
    }

    // move or point the created temporary file to the new filename
    // NOTE: these fail if the new file name exist
    $newFileCreated = (isWindows() ? @rename($sysFileName, $newFileName) : @link($sysFileName, $newFileName));
    if ($newFileCreated) {
      unlink ($sysFileName);
      return $newFileName;
    }
    unlink ($sysFileName);

    $tries++;
  } while ($tries <= 5);

  return false;
}


///////////////////////////////////////////////////////////////////////////////
// Encode string 
// Return 
//   - encoded string 
///////////////////////////////////////////////////////////////////////////////
function encode($in_str, $charset) {
  $out_str = $in_str;
  if ($out_str && $charset) {

    // define start delimimter, end delimiter and spacer
    $end = '?=';
    $start = '=?' . $charset . '?B?';
    $spacer = $end . "\r\n " . $start;

    // determine length of encoded text within chunks
    // and ensure length is even
    $length = 75 - strlen($start) - strlen($end);
    $length = floor($length/2) * 2;

    // encode the string and split it into chunks
    // with spacers after each chunk
    $out_str = base64_encode($out_str);
    $out_str = chunk_split($out_str, $length, $spacer);

    // remove trailing spacer and
    // add start and end delimiters
    $spacer = preg_quote($spacer);
    $out_str = preg_replace('/' . $spacer . "$/", '', $out_str);
    $out_str = $start . $out_str . $end;
  }
  return $out_str;
} 


///////////////////////////////////////////////////////////////////////////////
// Check if OS is Windows 
// Return 
//   - true if os is Windows 
//   - false otherwise 
///////////////////////////////////////////////////////////////////////////////
function isWindows() {
  return (DIRECTORY_SEPARATOR == '\\' ? true : false);
}


/**
 * check rules defined by the user 
 * 
 * @access public
 * @return void
 */
function check_user_defined_rules() {
  global $err, $rules, $params;
  include_once('obminclude/of/of_error.php');
  $return = true;
  if (is_array($rules)) {
    foreach($rules as $field => $field_rules) {
      /* Normalize rules */
      foreach($field_rules as $rule => $content) {
        if (!is_array($content)) {
          $field_rules[$rule] = array('default' => $content);
        }
      }
      $value = $params[$field];
      if ($field_rules['length']['default'] > 0) {
        $value = substr($value,0,$field_rules['length']['default']);
      }
      if (is_array($field_rules['replacement'])) {
        foreach($field_rules['replacement'] as $pattern => $replacement) {
          $value = preg_replace("/($pattern)+/",$replacement,$value);
        }
      }
      if (is_array($field_rules['callback'])) {
        foreach($field_rules['callback'] as $callback) {
          $value = $callback($value);
        }
      }
      if ($return && is_array($field_rules['regexp'])) {
        foreach($field_rules['regexp'] as $name => $regexp) {
          if (!preg_match($regexp, $value)) {
            if (isset($field_rules['message'][$name])) {
              $err['msg'] =  $field_rules['message'][$name];
            } else {
              $err['msg'] = $field_rules['message']['default'];
            }
            $err['field'] = $field;
            OBM_Error::getInstance()->addError($field, $err['msg']);
            $return = false;
          }
        }
      }
      if (is_array($field_rules['check'])) {
        foreach($field_rules['check'] as $callback) {
          if (!$callback($value)) {
            $err['field'] = $field;
            OBM_Error::getInstance()->addError($field, $err['msg']);
            $return = false;
          }
        }
      }
      $params[$field] = $value;
    }
  }

  return $return;
}
if ( !function_exists('sys_get_temp_dir') ) {
  function sys_get_temp_dir() {
    if ( !empty($_ENV['TMP']) ) {
      return realpath( $_ENV['TMP'] );
    } else if ( !empty($_ENV['TMPDIR']) ) {
      return realpath( $_ENV['TMPDIR'] );
    } else if ( !empty($_ENV['TEMP']) ) {
      return realpath( $_ENV['TEMP'] );
    } else {
      $temp_file = tempnam( md5(uniqid(rand(), TRUE)), '' );
      if ( $temp_file)  {
        $temp_dir = realpath( dirname($temp_file) );
        unlink( $temp_file );
        return $temp_dir;
      } else {
        return FALSE;
      }
    }
  }
}


/************************************************************************
 ** Groups of functions that makes a profile list with full data
/************************************************************************/

/**
 * Return all profiles 
 * 
 * @return array of profiles
 **/
function get_all_profiles($cached = true) {
  global $cdg_sql, $obm, $c_profile_properties;
  $profiles = is_array($GLOBALS['profiles'])?$GLOBALS['profiles']:array();
  if(!isset($_SESSION['profiles']) || !$cached) {
    $multidomain = sql_multidomain('profile');
    $obm_q = new DB_OBM;
    $query = "SELECT * FROM Profile LEFT JOIN Domain ON profile_domain_id = domain_id WHERE 1=1 $multidomain OR domain_global = TRUE";
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $ids[] = $obm_q->f('profile_id');
      $profiles[$obm_q->f('profile_name')] = array('id' => $obm_q->f('profile_id'), 'section' => array(), 'module' => array(), 'properties' => array());
    }
    $ids = implode(',',$ids);

    $query = "SELECT * FROM Profile 
      INNER JOIN ProfileModule ON profilemodule_profile_id = profile_id
      WHERE profile_id IN ($ids)";
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $profiles[$obm_q->f('profile_name')]['module'][$obm_q->f('profilemodule_module_name')] = dechex($obm_q->f('profilemodule_right'));
    }

    $query = "SELECT * FROM Profile 
      INNER JOIN ProfileSection ON profilesection_profile_id = profile_id
      WHERE profile_id IN ($ids)";
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $profiles[$obm_q->f('profile_name')]['section'][$obm_q->f('profilesection_section_name')] = $obm_q->f('profilesection_show');
    }  
    $query = "SELECT * FROM Profile
      INNER JOIN ProfileProperty ON profileproperty_profile_id = profile_id
      WHERE profile_id IN ($ids)";
    $obm_q->query($query);
    while($obm_q->next_record()) {
      $value = $obm_q->f('profileproperty_value');
      if($obm_q->f('profileproperty_name') == 'admin_realm' || $obm_q->f('profileproperty_name') == 'access_exceptions') {
        $profiles[$obm_q->f('profile_name')]['properties'][$obm_q->f('profileproperty_name')] = explode(',', $value);
      } elseif($obm_q->f('profileproperty_name') == 'default_right') {
        $value = explode(',', $value);
        $modules = array('calendar', 'mailbox', 'mailshare', 'resource', 'addressbook');
        $rights = array('access', 'read', 'write');        
        $i =0;
        foreach($modules as $module) {
          foreach($rights as $right) {
            if($obm_q->f('profile_name') == $obm['profile']) {
              $GLOBALS['cgp_default_right'][$module]['public'][$right] =  $value[$i];
            }
	    $profiles[$obm_q->f('profile_name')]['properties']['default_right'][$module]['public'][$right] =  $value[$i];
            $i++;
          }
        }
        $_SESSION['cgp_default_right'] = $GLOBALS['cgp_default_right'];
      } else {
        $profiles[$obm_q->f('profile_name')]['properties'][$obm_q->f('profileproperty_name')] = $value;
      }
    }  
    $_SESSION['profiles'] = $profiles;
  } else {
    $profiles = array_merge($_SESSION['profiles'], $profiles);
    $GLOBALS['cgp_default_right'] = $_SESSION['cgp_default_right'];
  }
  return $profiles;    
}

function get_user_profile_id($uid) {
  $obm_q = new DB_OBM;
  $obm_q->query(
    '  SELECT userobm_perms AS profile_id '
    . 'FROM UserObm '
    . 'WHERE userobm_id = #INT(' . $uid . ')');
  return $obm_q->next_record() ? $obm_q->f('profile_id')  : null;
}

/**
 * of_entity_insert 
 * 
 * @param mixed $entity 
 * @param mixed $id 
 * @access public
 * @return void
 */
function of_entity_insert($entity,$id) {
  global $cdg_sql;

  $entity = strtolower($entity);
  $query = 'INSERT INTO Entity (entity_mailing) VALUES (TRUE)';
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $eid = $obm_q->lastid();
  $query = 'INSERT INTO '.ucfirst($entity).'Entity 
    ('.$entity.'entity_entity_id, '.$entity.'entity_'.$entity.'_id )
    VALUES ('.$eid.', '.$id.')';
  display_debug_msg($query, $cdg_sql, 'of_entity_insert()');
  $obm_q->query($query);
  return $eid;
}


/**
 * of_entity_delete 
 * 
 * @access public
 * @return void
 */
function of_entity_delete($entity,$id) {
  global $cdg_sql;

  $entity = strtolower($entity);
  if(is_array($id)) {
    $id = implode(',', $id);
  } else {
    $id = sql_parse_id($id);
  }  
  $obm_q = new DB_OBM;
  $query_select = 'SELECT '.$entity.'entity_entity_id FROM '.ucfirst($entity).'Entity WHERE '.$entity.'entity_'.$entity.'_id IN ('.$id.')';
  display_debug_msg($query_select, $cdg_sql, 'of_entity_delete()');
  $obm_q->query($query_select);
  while($obm_q->next_record()){
    $entity_id[]=$obm_q->f($entity.'entity_entity_id');
  }
  if(is_array($entity_id)){
    $entity_id = implode(',',$entity_id);
    $query = 'DELETE FROM Entity WHERE entity_id IN ('.$entity_id.')';
    display_debug_msg($query, $cdg_sql, 'of_entity_delete()');
    $obm_q->query($query);
  }
}


/**
 * of_entity_get 
 * 
 * @param mixed $entity 
 * @param mixed $id 
 * @access public
 * @return void
 */
function of_entity_get($entity, $id) {
  global $cdg_sql;

  $entity = strtolower($entity);
  $query = 'SELECT '.$entity.'entity_entity_id FROM '.ucfirst($entity).'Entity WHERE '.$entity.'entity_'.$entity.'_id = '.$id;
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'of_entity_get()');
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f($entity.'entity_entity_id');
}


/**
 * of_entity_subquery 
 * 
 * @param mixed $entity 
 * @param mixed $id 
 * @param mixed $not 
 * @access public
 * @return void
 */
function of_entity_subquery($entity, $id = NULL, $not = false) {

  $entity = strtolower($entity);
  if (is_null($id)) {
    $query = '(SELECT '.$entity.'entity_entity_id FROM '.ucfirst($entity).'Entity)';
  } elseif (is_array($id)) {
    if ($not) $NOT = 'NOT';
    $id = sql_implode_id(',', $id);
    $query = '(SELECT '.$entity.'entity_entity_id FROM '.ucfirst($entity).'Entity WHERE '.$entity.'entity_'.$entity.'_id '.$NOT.' IN ('.$id.'))';
  } else {
    $id = sql_parse_id($id, true, $not);
    $query = '(SELECT '.$entity.'entity_entity_id FROM '.ucfirst($entity).'Entity WHERE '.$entity.'entity_'.$entity.'_id '.$NOT.' '.$id.')';
  }
  return $query;
}


/**
 * of_entity_get_service_property
 * 
 * @param mixed $entity
 * @param mixed $id (from the $entity table eg : host_id)
 * @param mixed $service (optionnal)
 * @param mixed $property (optionnal)
 * @access public
 * @return void
 */
function of_entity_get_service_property($entity, $id, $service='', $property='') {

  $entity_id = of_entity_get($entity, $id);
  if (trim($service) != '') {
    $where_service = "AND serviceproperty_service='$service'";
  }
  if (trim($property) != '') {
    $where_property = "AND serviceproperty_property='$property'";
  }

  $obm_q = new DB_OBM;
  $query = "SELECT * FROM ServiceProperty
    WHERE serviceproperty_entity_id=$entity_id
      $where_service
      $where_property
";
  display_debug_msg($query, $cdg_sql, 'of_entity_get_serive_property()');
  $obm_q->query($query);
  $props = array();
  while ($obm_q->next_record()) {
    $s = $obm_q->f('serviceproperty_service');
    $p = $obm_q->f('serviceproperty_property');
    $v = $obm_q->f('serviceproperty_value');
    $props[$s][$p][] = $v;
  }

  return $props;
}


/**
 * of_service_insert 
 * 
 * @param mixed $service 
 * @param mixed $id 
 * @access public
 * @return void
 */
function of_service_insert($service, $id) {
  global $cdg_sql;

  $query = "SELECT service_id FROM Service WHERE service_service = '$service' and service_entity_id = $id";
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'of_service_insert(select)');
  $obm_q->query($query);
  if (!$obm_q->next_record()) {
    $query = "INSERT INTO Service (service_service, service_entity_id) VALUES ('$service', $id)";
    display_debug_msg($query, $cdg_sql, 'of_service_insert(select)');
    $obm_q->query($query);
    $id = $obm_q->lastid();
  } else {
    $id = $obm_q->f('service_id');
  }
  return $id;
}


/**
 * of_service_delete 
 * 
 * @param mixed $service 
 * @param mixed $id 
 * @access public
 * @return void
 */
function of_service_delete($service, $id) {
  global $cdg_sql;

  $query = "DELETE FROM Service WHERE service_service = '$service' and service_entity_id = $id";
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'of_service_delete()');
  $obm_q->query($query);
  of_service_property_delete_all($service, $id);
}


/**
 * of_service_properties 
 * 
 * @param mixed $service 
 * @param mixed $id 
 * @access public
 * @return array of property => value
 */
function of_service_properties($service, $id) {
  global $cdg_sql;

  $id = sql_parse_id($id, true);
  $obm_q = new DB_OBM;
  $query = "SELECT serviceproperty_property, serviceproperty_value FROM ServiceProperty
    WHERE serviceproperty_service = '$service' AND serviceproperty_entity_id $id";
  display_debug_msg($query, $cdg_sql, 'of_service_properties()');
  $obm_q->query($query);

  $properties = array();

  while($obm_q->next_record()) {
    $properties[$obm_q->f('serviceproperty_property')] = $obm_q->f('serviceproperty_value');
  }

  return $properties;
}


/**
 * of_service_property_insert 
 * 
 * @param mixed $service_property 
 * @param mixed $service 
 * @param mixed $id 
 * @param mixed $value 
 * @access public
 * @return void
 */
function of_service_property_insert($service, $service_property, $id, $values) {
  global $cdg_sql;

  if (!is_array($values)) {
    $values = array($values);
  }
  $values = array_unique($values);
  $obm_q = new DB_OBM;  
  $return = true;
  foreach($values as $value) {
    if ($value != '') {
      $query = "INSERT INTO ServiceProperty (serviceproperty_service, serviceproperty_property, serviceproperty_entity_id, serviceproperty_value)
        VALUES ('$service', '$service_property', $id, '$value')";
      display_debug_msg($query, $cdg_sql, 'of_service_property_insert()');
      $return = $return && $obm_q->query($query);
    }
  }
  return $return;
}


/**
 * of_service_property_replace 
 * 
 * @param mixed $service 
 * @param mixed $service_property 
 * @param mixed $id 
 * @param mixed $values 
 * @access public
 * @return void
 */
function of_service_property_replace($service, $service_property, $id, $values) {
  $return = of_service_property_delete($service, $service_property, $id);
  $return = $return && of_service_property_insert($service, $service_property, $id, $values);
  return $return;
}


/**
 * of_service_property_delete 
 * 
 * @param mixed $service 
 * @param mixed $service_property 
 * @param mixed $id 
 * @access public
 * @return void
 */
function of_service_property_delete($service, $service_property, $id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;  
  $query = "DELETE FROM ServiceProperty
    WHERE serviceproperty_service = '$service'
      AND serviceproperty_property = '$service_property'
      AND serviceproperty_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'of_service_property_delete()');
  return $obm_q->query($query);
}

/**
 * of_service_property_delete_all 
 * 
 * @param mixed $service 
 * @param mixed $id 
 * @access public
 * @return void
 */
function of_service_property_delete_all($service, $id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;  
  $query = "DELETE FROM ServiceProperty
    WHERE serviceproperty_service = '$service'
      AND serviceproperty_entity_id = $id";
  display_debug_msg($query, $cdg_sql, 'of_service_property_delete()');
  return $obm_q->query($query);
}

/**
 * of_service_property_unique
 * 
 * @param mixed $service
 * @param mixed $service_property
 * @param mixed $value
 * @param mixed $id
 * @access public
 * @return void
 */
function of_service_property_unique($service, $service_property, $value, $id='') {

  $value = trim($value);
  if ($id) {
    $qid = 'AND domainentity_domain_id != '.$id;
  }
  $obm_q = new DB_OBM;  
  $query = "SELECT * FROM ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    WHERE serviceproperty_service = '$service' AND serviceproperty_property = '$service_property' 
    AND serviceproperty_value = '$value'
    $qid";
  display_debug_msg($query, $cdg_sql, 'of_service_property_unique()');
  $obm_q->query($query);
  return (!$obm_q->next_record()); 
}

/**
 * Generates a world-wide unique event id.
 * @access public
 * @return unique id
 */
//function genUniqueExtEventId() {
function generateUniqueKey($universal=true) {
  if($universal) {
    return get_obm_info('product_id').sha1(uniqid()).sha1($GLOBALS['obm']['domain_name']).sha1(mktime()*rand());
  }
  return sha1($GLOBALS['obm']['domain_name'].uniqid().mktime()*rand());
}

function get_domain_property($domain, $property) {
  $query = "SELECT domainpropertyvalue_value FROM DomainPropertyValue 
    WHERE domainpropertyvalue_property_key = '$property' AND domainpropertyvalue_domain_id = '$domain'";
  $obm_q = new DB_OBM;  
  display_debug_msg($query, $cdg_sql, 'get_domain_property()');
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f('domainpropertyvalue_value');

}
function splitwords($string) {
  $mask = "/[\p{L}0-9][\p{L}\p{Mn}\x{2019}0-9]*/u";
  preg_match_all($mask, $string, $matches);
  if($callback !== null) {
    $matches[0] = array_map($callback,$matches[0]);
  }
  return $matches[0];
}

function of_index_update($entity, $id=null) {
  if($id !== null) {
    $id = (array)$id;
    $wherePattern = 'WHERE id IN ('.implode(',', $id).')';
    $whereUser = 'WHERE userobm_id IN ('.implode(',', $id).')';
  }
  $obm_q = new DB_OBM;
  $obm_q2 = new DB_OBM;
  $query = 'DELETE FROM _userpattern '.$wherePattern;
  $obm_q->query($query);
  $query = 'SELECT userobm_id, userobm_lastname, userobm_firstname, userobm_login, userobm_email FROM UserObm '.$whereUser;
  $obm_q->query($query);
  while($obm_q->next_record()) {
    $words = array();
    $mails = $obm_q->f('userobm_email');
    $words = explode("\r\n",$mails);
    $words = array_merge($words, splitwords($obm_q->f('userobm_lastname'),'strtolower'));
    $words = array_merge($words, splitwords($obm_q->f('userobm_firstname'),'strtolower'));
    $words = array_merge($words, splitwords($obm_q->f('userobm_login'),'strtolower'));
    $words = array_unique($words);
    foreach($words as $word) {
      if(trim($word) != '') {
        $query = "INSERT INTO _userpattern VALUES(".$obm_q->f('userobm_id').", '".$obm_q->escape(trim($word))."')";
        $obm_q2->query($query);
      }
    }
  }  
}
?>
