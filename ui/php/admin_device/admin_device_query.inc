<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

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




/**
 * Search query
 */
function run_query_admin_device_search($device) {
  global $cdg_sql, $obm;

  $user = $device['user'];
  $type = $device['type'];
  $partnership = $device['partnership'];
  $where = array();
  if (isset($user) && $user != '') {
    array_push($where, "(userobm_login #LIKE '%$user%' OR userobm_lastname #LIKE '%$user%' OR userobm_firstname #LIKE '%$user%')");
  }

  if (isset($type) && $type != $GLOBALS['c_none']) {
    array_push($where, "type='$type'");
  }

  if ($partnership == $GLOBALS['c_yes']) {
    array_push($where, "opush_sync_perms.device_id IS NOT NULL");
  } else if ($partnership == $GLOBALS['c_no']) {
    array_push($where, "opush_sync_perms.device_id IS NULL");
  }

  array_push($where, "userobm_domain_id = " . $obm["domain_id"]);

  if (sizeof($where) >= 1)
    $where_clause = "WHERE ".implode(' AND ', $where);

  $ORDER_BY = "ORDER BY userobm_lastname";
  if (isset($device['sql_order_field'])) {
    $order = $device['sql_order_field'];
    if ($device['sql_order_field'] == 'user') $order = 'userobm_lastname';
    $ORDER_BY = "ORDER BY $order $device[sql_order_dir]";
  }

  $obm_q = new DB_OBM;
  $query = "SELECT 
    userobm_id,
    userobm_login,
    userobm_delegation,
    #CONCAT(userobm_lastname, ' ', userobm_firstname) as user,
    opush_device.id,
    opush_device.owner,
    opush_device.identifier,
    opush_device.type, 
    MIN(f.last_sync) as first_sync,
    MAX(f.last_sync) as last_sync,
    opush_sync_perms.device_id as partnership, 
    opush_sync_perms.policy 
    FROM opush_device 
    LEFT JOIN opush_sync_state f ON f.device_id=opush_device.id
    LEFT JOIN opush_sync_perms ON opush_sync_perms.device_id = opush_device.id
    LEFT JOIN UserObm ON userobm_id = opush_device.owner
    $where_clause
    GROUP BY 
      opush_device.id, opush_device.owner, opush_device.identifier,opush_device.type,
      opush_sync_perms.device_id, opush_sync_perms.policy,
      userobm_id, userobm_login, userobm_delegation, userobm_lastname, userobm_firstname
    $ORDER_BY";

  display_debug_msg($query, $cdg_sql, 'run_query_admin_device_search()');
  $obm_q->xquery($query);

  return $obm_q;
}


/**
 * Get device types 
 */
function run_query_admin_device_get_type() {
  global $cdg_sql, $obm;

  $domain_id = $obm['domain_id'];

  $query = "SELECT DISTINCT(type) FROM opush_device LEFT JOIN UserObm ON userobm_id=owner WHERE userobm_domain_id='$domain_id' ORDER BY type";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_admin_device_get_type()');

  return $obm_q;

}

?>
