<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/


/**
 * Search query
 */
function run_query_admin_device_search($device) {
  global $cdg_sql;

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

  if (sizeof($where) >= 1)
    $where_clause = "WHERE ".implode('AND ', $where);

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
    LEFT JOIN opush_sync_state l ON l.device_id=opush_device.id
    LEFT JOIN opush_sync_perms ON opush_sync_perms.device_id = opush_device.id
    LEFT JOIN UserObm ON userobm_id = opush_device.owner
    $where_clause
    GROUP BY 
      opush_device.id, opush_device.owner, opush_device.identifier,opush_device.type,
      opush_sync_perms.device_id, opush_sync_perms.policy,
      userobm_id, userobm_login, userobm_lastname, userobm_firstname
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
