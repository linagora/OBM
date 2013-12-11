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
// OBM - File : resource_query.inc                                           //
//     - Desc : resource query & db File                                     //
// 2005-08-13 Florent Goalabre : Last Update 2005-08-13                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Resource query search query execution
// Parameters:
//   - $resource[] : resource search criteria
//     keys used  : name, perms
//   - $sql_order_field : infos for order clause
//   - $sql_order_dir : direction for order clause (asc, desc)
//   - $filter_entity : if and which restriction to set (eg resource (who can read))
///////////////////////////////////////////////////////////////////////////////

require_once(dirname(__file__)."/../share/share_query.inc");

function run_query_resource_search($resource) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $name = sql_search_text_parse($resource['name']);
  $desc = sql_search_text_parse($resource['desc']);
  $rtype = $resource['rtype'];
  $qtyinf = $resource['qtyinf'];
  $qtysup = $resource['qtysup'];
  $delegation = sql_search_text_parse($resource['delegation']);
 
  $sql_order_dir = $resource['sql_order_dir'];
  $sql_order_field = $resource['sql_order_field'];
  
  $filter_entity = $resource['filter_entity'];
  $filter_pattern = $resource['filter_pattern'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);
  $multidomain = sql_multidomain('resource');
	
  if ($filter_entity == 'Resource') {
    $where_filter_entity = 'AND (1=0';
    $f_resources = OBM_Acl::getAllowedEntities($obm['uid'], 'resource', $filter_pattern);
    $f_resources_ids = array_keys($f_resources);
    if ((is_array($f_resources_ids)) && (count($f_resources_ids) > 0)) {
      $where_filter_entity .= " OR resource_id IN (";
      foreach($f_resources_ids as $r_id) {
	    $where_filter_entity .= "$coma'$r_id'";
	    $coma = ',';
      }
      $where_filter_entity .= ')';
    }
    $where_filter_entity .= ')';
  }

  $select = "SELECT Resource.*, resource_id as id, resourcetype_label
    FROM Resource
    LEFT JOIN ResourceType ON resourcetype_id = resource_rtype_id";

  $where = " 1=1 $multidomain";

  // If a name indication has been specified, get it
  if (trim($name) != '') {
     $where .= " AND resource_name #LIKE '".$obm_q->escape($name)."%'";
  }

  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND resource_delegation #LIKE '".$obm_q->escape($delegation)."%'";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != '') {
     $where .= " AND resource_description #LIKE '%".$obm_q->escape($desc)."%'";
  }
  
  // If a resource type has been specified, get it
  if (!empty($rtype)) {
    $rtype = sql_parse_id($rtype, true);
    $where .= " AND resource_rtype_id $rtype ";   
  }

  // If a quantity sup has been specified, get it
  if ($qtysup != '') {
    $qtysup = sql_parse_int($qtysup);
    $where .= " AND resource_qty<='$qtysup'";   
  }

  // If a quantity inf has been specified, get it
  if ($qtyinf != '') {
    $qtyinf = sql_parse_int($qtyinf);
    $where .= " AND resource_qty>='$qtyinf'";   
  }


  if ((trim($where) != '') || (trim($where_filter_entity) != '')) {
    $whereq = " WHERE $where $where_filter_entity";
  }

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'resource_name';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Resource $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_resource_search()');
    $obm_q->xquery($query);
  }

  return $obm_q;
}


/**
 * Search resources from a single field 
 * 
 * @param mixed $resource 
 * @access public
 * @return array of 'length' => DB query results nb, and 'datas' => DB Search results
 */
function run_query_resource_ext_search($resource) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = $resource['pattern'];
  $filter_entity = $resource['filter_entity'];
  $filter_pattern = $resource['filter_pattern'];
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('Resource');

  $limit = $resource['limit'];
  if(isset($resource['first_row'])) $first_row = $resource['first_row'] - 1;
  else $first_row = 0;

  if ($filter_entity == 'Resource') {
    $where_filter_entity = 'AND (1=0';
    $f_resources = OBM_Acl::getAllowedEntities($obm['uid'], 'resource', $filter_pattern);
    $f_resources_ids = array_keys($f_resources);
    if ((is_array($f_resources_ids)) && (count($f_resources_ids) > 0)) {
      $where_filter_entity .= " OR resource_id IN (";
      foreach($f_resources_ids as $r_id) {
	    $where_filter_entity .= "$coma'$r_id'";
	    $coma = ',';
      }
      $where_filter_entity .= ')';
    }
    $where_filter_entity .= ')';
  }

  $query = "SELECT 
      count(*) as resultscount
    FROM Resource
    LEFT JOIN ResourceType ON resource_rtype_id = resourcetype_id
    WHERE 
      resource_name #LIKE '".$obm_q->escape($pattern)."%' 
      $multidomain
      $where_filter_entity";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_ext_search()');
  $obm_q->xquery($query);
  $obm_q->next_record();
  $resultsCount = $obm_q->f('resultscount')+0;
  $query = "SELECT 
      resource_id,
      resource_name,
      resource_qty,
      resource_rtype_id,
      resourcetype_property,
      resourcetype_pkind,
      count(resourceitem_id) as items
    FROM Resource
    LEFT JOIN ResourceType ON resource_rtype_id = resourcetype_id
    LEFT JOIN ResourceItem ON resourceitem_resourcetype_id = resourcetype_id
    WHERE 
      resource_name #LIKE '".$obm_q->escape($pattern)."%' 
      $multidomain
      $where_filter_entity
    GROUP BY resource_id, resource_name, resource_qty, resource_rtype_id, resourcetype_property, resourcetype_pkind 
    ORDER BY resource_name
    LIMIT $limit OFFSET $first_row";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_ext_search()');
  $obm_q->xquery($query);
  return array('length' => $resultsCount, 'datas' => $obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Resource detail query execution
// Parameters:
//   - $p_id : resource id
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, 'Resource.resource_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'Resource.resource_timecreate', 'timecreate');
  $multidomain = sql_multidomain('resource');
  $p_id = sql_parse_id($p_id, true);
	
  $query = "SELECT Resource.*,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      resourcetype_label
    FROM Resource 
         LEFT JOIN UserObm as c ON Resource.resource_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON Resource.resource_userupdate=u.userobm_id
         LEFT JOIN ResourceType ON resource_rtype_id=resourcetype_id
    WHERE Resource.resource_id $p_id $multidomain";

  $obm_q->query($query);
  $obm_q->next_record();
  display_debug_msg($query, $cdg_sql, 'run_query_resource_detail()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource insert query execution
// Parameters:
//   - $resource[]   : entry values
//     keys used : name, desc 
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_insert($resource) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  // Hook : Pre
  if (function_exists('hook_pre_run_query_resource_insert')) {
    if (! hook_pre_run_query_resource_insert($resource)) {
      return false;
    }
  }

  $name = $resource['name'];
  $desc = $resource['desc'];
  $qty = $resource['qty'];
  $rtype = sql_parse_id($resource['rtype']);
  if ($qty == '') {
    $qty = '0';
  }
  $domain_id = sql_parse_id($obm['domain_id']);
	
  $q_delegation = of_delegation_query_insert_clauses('resource_delegation', stripslashes($resource['delegation']));
  $query = "INSERT INTO Resource (
     resource_timeupdate,
     resource_timecreate,
     resource_userupdate,
     resource_usercreate,
     resource_domain_id,
     resource_rtype_id,
     resource_name
     $q_delegation[field],
     resource_description,
     resource_qty,
     resource_email
   ) VALUES (
    null,
    '".date('Y-m-d H:i:s')."',
    null,
    $obm[uid],
    $domain_id,
    $rtype,
    '".$obm_q->escape($name)."'
    $q_delegation[value],
    '".$obm_q->escape($desc)."',
    '".$obm_q->escape($qty)."',
    NULL
   )";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_insert()');
  $retour = $obm_q->query($query);
error_log("GOT RETOUR: $retour");
  if ($retour) {
    $id = $obm_q->lastid();

    $obm_q->query("UPDATE Resource r SET resource_email=".$obm_q->CONCAT(
            array("'res-$id@'",
            "(SELECT d.domain_name FROM Domain d WHERE d.domain_id=r.resource_domain_id)")).
        "WHERE resource_id=$id");

    $entity_id = of_entity_insert($GLOBALS['module'], $id);    
    $resource['entity_id'] = $id;
    
    $default_public_rights = OBM_Acl_Utils::getDefaultPublicRights('resource');
    OBM_Acl::setPublicRights('resource', $id, $default_public_rights);
    $default_rights = OBM_Acl_Utils::parseRightsParams($resource);
    foreach ($default_rights['user'] as $user_id => $rights) {
      OBM_Acl::setRights($user_id, 'resource', $id, $rights);
    }

    // Hook : Post
    if (function_exists('hook_post_run_query_resource_insert')) {
      if (! hook_post_run_query_resource_insert($resource)) {
        return false;
      }
    }

  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Resource update query execution
// Parameters:
//   - $p_id     : resource id
//   - $resource[]   : entry values
//     keys used : name, desc 
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_update($p_id, $resource) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  // Hook : Pre
  if (function_exists('hook_pre_run_query_resource_update')) {
    if (! hook_pre_run_query_resource_update($resource)) {
      return false;
    }
  }

  $name = $resource['name'];
  $desc = $resource['desc'];
  $qty = $resource['qty'];
  $rtype = sql_parse_id($resource['rtype']);
  $p_id = sql_parse_id($p_id, true);

  $multidomain = sql_multidomain('resource');
	
  $q_delegation = of_delegation_query_update_clause('resource_delegation', stripslashes($resource['delegation']));

  $query = "UPDATE Resource SET
    resource_timeupdate='". date('Y-m-d H:i:s')."',
    resource_userupdate=$obm[uid],
    resource_name='".$obm_q->escape($name)."',
    resource_description='".$obm_q->escape($desc)."',
    resource_qty='".$obm_q->escape($qty)."',
    resource_rtype_id=$rtype
    $q_delegation
  WHERE resource_id $p_id 
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_update()');
  $retour = $obm_q->query($query);

	if ($retour) {
    // Hook : Post
    if (function_exists('hook_post_run_query_resource_update')) {
      if (! hook_post_run_query_resource_update($resource)) {
        return false;
      }
    }
 	}

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource delete query execution
// Parameters:
//   - $p_id : resource id
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_delete($p_id) {
  global $cdg_sql;

  // Hook : Pre
  if (function_exists('hook_pre_run_query_resource_delete')) {
    if (! hook_pre_run_query_resource_delete($p_id)) {
      return false;
    }
  }

  of_entity_delete($GLOBALS['module'], $p_id);
  $p_id = sql_parse_id($p_id, true);

  // Delete the resource
  $multidomain = sql_multidomain('resource');
  $query = "DELETE FROM Resource WHERE resource_id $p_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_resource_delete(resource)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Hook : Post
  if (function_exists('hook_post_run_query_resource_delete')) {
    if (! hook_post_run_query_resource_delete($p_id)) {
      return false;
    }
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource environment checking (same resource exists ?)
// Parameters:
//   - $p_id   : resource id
//   - $resource[] : resource's values
// Returns:
//   -resource Database object with list of similar resources
///////////////////////////////////////////////////////////////////////////////
function check_resource_context($p_id, $resource) {

  $name = $resource['name'];
  $desc  = $resource['desc'];

  // if a resource with same name exists, return false
  $co_q = run_query_check_resource($p_id, $name);
  return $co_q;
}


/*
 * Resource rights checking
 * Parameters:
 * @param $params : parameters values
 * @param $g[]    : current group values
 * @return true if OK, else false
 */
function check_resource_update_rights($resource) {
  global $err, $l_error_delegation;

  $r = get_resource_info($resource['resource_id']);
  if (!of_delegation_check_update_rights($resource, $r['delegation'])) {
    $err['msg'] .= $l_error_delegation;
    return false;
  }

  return true;
}




///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $p_id     : resource id
//   - $resource[]   : values checked
//     keys used : name, resource 
///////////////////////////////////////////////////////////////////////////////
function check_resource_data_form($p_id, &$resource) {
  global $l_exist_error, $l_name_error, $l_desc_error;
  global $l_lname_error, $cgp_use;
  global $err, $action, $cdg_sql;

  $obm_q = new DB_OBM;
  // Hook : Validation
  if (function_exists('hook_pre_check_resource_data_form')) {
    if (! hook_pre_check_resource_data_form($resource)) {
      return false;
    }
  }

  // Les contraintes sur la delegation ne sont pas valables pour le domaine global
  if ($cgp_use['property']['delegation']) {
    if(!of_delegation_check_data($resource['delegation'])) {
      $err['field'] = 'delegation';
      return false;
    }
  } 

  $id = $resource['resource_id'];
  $name = $resource['name'];
  $desc = $resource['desc'];

  // MANDATORY: name 
  if (trim($name) == '') {
    $err['msg'] = $l_name_error.' : '. $name;
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // if a resource with the same name already exists  return false           //
  /////////////////////////////////////////////////////////////////////////////
  $multidomain = sql_multidomain('resource');
  $query = "SELECT * FROM Resource WHERE resource_name='".$obm_q->escape($name)."' $multidomain";
  if ($p_id > 0) {
    $p_id = sql_parse_id($p_id, true, true);
    $query .= " AND resource_id $p_id ";
  }
  display_debug_msg($query, $cdg_sql, 'check_resource_data_form()');
  $obm_q->query($query);
  if ($obm_q->num_rows() > 0) {
    $err['msg'] = "$name : $l_exist_error";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the resource can be deleted
// Parameters:
//   - $id : resource id
// Returns:
//   true if the resource can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_resource_can_delete($params) {
  global $ok_msg;

  // Hook : Pre
  if (function_exists('hook_pre_check_resource_can_delete')) {
    if (! hook_pre_check_resource_can_delete($params)) {
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the Id of one Ressource from its name
// Parameters:
//   - $name  : name
// Returns:
//   - resource's Id
///////////////////////////////////////////////////////////////////////////////
function get_resource_id($name) {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  $multidomain = sql_multidomain('resource');
  if (isset($name)) {
    $query = "SELECT resource_id FROM Resource WHERE resource_name='".$obm_q->escape($name)."' $multidomain";
    display_debug_msg($query, $cdg_sql, 'get_resource_id()');
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('resource_id');
  } else {
    return '';
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get resources infos from its id
// Parameters:
//   - $id : resource id
// Returns:
//   - array
///////////////////////////////////////////////////////////////////////////////
function get_resource_infos($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  $multidomain = sql_multidomain('resource');
  if (isset($id)) {
    $id = sql_parse_id($id, true);
    $query = "SELECT *
      FROM Resource
      WHERE resource_id $id $multidomain";
    display_debug_msg($query, $cdg_sql, 'get_resource_infos()');
    $obm_q->query($query);
    $obm_q->next_record();
    $ret['name'] = $obm_q->f('resource_name');
    $ret['description'] = $obm_q->f('resource_description');
    return $ret;
  } else {
    return '';
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get resource type infos
// Parameters:
// Returns:
//   - array
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcetype () {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  $multidomain = sql_multidomain('resourcetype');
  $query = "SELECT * FROM ResourceType WHERE 1=1 $multidomain ORDER BY resourcetype_label";
  display_debug_msg($query, $cdg_sql, 'run_query_resourcetype()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get resource item infos
// Parameters:
// Returns:
//   - array
///////////////////////////////////////////////////////////////////////////////
function run_query_resourceitem ($r_id='') {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  $multidomain = sql_multidomain('resourceitem');

  if ($r_id != '') {
    $r_id = sql_parse_id($r_id, true);
    $left_join = "LEFT JOIN Resource ON resource_rtype_id=resourceitem_resourcetype_id";
    $where = "AND resource_id $r_id ";
  }

  $query = "SELECT * FROM ResourceItem $left_join WHERE 1=1 $multidomain $where ORDER BY resourceitem_label";
  display_debug_msg($query, $cdg_sql, 'run_query_resourceitem()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type insertion query construction and execution
// Parameters:
//   - $params       : hash
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_rtype_insert($params) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $domain_id = sql_parse_id($obm['domain_id']);
  $label = $params['rtype_label'];
  $property = $params['rtype_property'];

  $query = "INSERT INTO ResourceType (
    resourcetype_domain_id,
    resourcetype_label,
    resourcetype_property) VALUES (
    $domain_id, 
    '".$obm_q->escape($label)."', 
    '".$obm_q->escape($property)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_rtype_insert()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type insertion query construction and execution
// Parameters:
//   - $params       : hash
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_ritem_insert($params) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $domain_id = sql_parse_id($obm['domain_id']);
  $label = $params['ritem_label'];
  $desc = $params['ritem_desc'];
  $type = $params['rtype'];

  $query = "INSERT INTO ResourceItem (
    resourceitem_domain_id,
    resourceitem_label,
    resourceitem_resourcetype_id,
    resourceitem_description) VALUES (
    $domain_id, 
    '".$obm_q->escape($label)."', 
    '".$obm_q->escape($type)."',
    '".$obm_q->escape($desc)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_ritem_insert()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type insertion query construction and execution
// Parameters:
//   - $params       : hash
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_rtype_update($params) {
  global $cdg_sql, $obm;
  
  $obm_q = new DB_OBM;
  $id = sql_parse_id($params['rtype'], true);
  $label = $params['rtype_label'];
  $property = $params['rtype_property'];
  $multidomain = sql_multidomain('resourcetype');

  $query = "UPDATE ResourceType SET 
    resourcetype_label = '".$obm_q->escape($label)."',
    resourcetype_property = '".$obm_q->escape($property)."'
    WHERE resourcetype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_rtype_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type insertion query construction and execution
// Parameters:
//   - $params       : hash
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_ritem_update($params) {
  global $cdg_sql, $obm;
  
  $obm_q = new DB_OBM;
  $domain_id = $obm['domain_id'];
  $id = sql_parse_id($params['ritem'], true);
  $label = $params['ritem_label'];
  $desc = $params['ritem_desc'];
  $rtype_id = sql_parse_id($params['rtype']);
  $multidomain = sql_multidomain('resourceitem');

  $query = "UPDATE ResourceItem SET 
    resourceitem_label = '".$obm_q->escape($label)."',
    resourceitem_description = '".$obm_q->escape($desc)."',
    resourceitem_resourcetype_id = $rtype_id
    WHERE resourceitem_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_ritem_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type - Resource links query execution
// Parameters:
// Return:
//   - database handle
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_rtype_links($params) {
  global $cdg_sql;

  $id = sql_parse_id($params['rtype'], true);
  $multidomain = sql_multidomain('resource');

  $query = "SELECT resource_id FROM Resource WHERE resource_rtype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_rtype_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type deletion query construction and execution
// Parameters:
//   - $params       : hash
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_rtype_delete($params) {
  global $cdg_sql, $obm;
  
  $id = sql_parse_id($params['rtype'], true);
  $multidomain = sql_multidomain('resourcetype');

  $query = "DELETE FROM ResourceType
    WHERE resourcetype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_rtype_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type - Resource links query execution
// Parameters:
// Return:
//   - database handle
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_ritem_links($params) {
  global $cdg_sql;

  $id = sql_parse_id($params['ritem'], true);
  $multidomain = sql_multidomain('resource');

  $query = "SELECT resource_id FROM Resource WHERE resource_rtype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_rtype_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource Type deletion query construction and execution
// Parameters:
//   - $params       : hash
// Return:
//   - execution code
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_ritem_delete($params) {
  global $cdg_sql, $obm;
  
  $id = sql_parse_id($params['ritem'], true);
  $multidomain = sql_multidomain('resourceitem');

  $query = "DELETE FROM ResourceItem
    WHERE resourceitem_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_resource_ritem_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

?>
