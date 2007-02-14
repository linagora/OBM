<?php
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
//   - $restriction : if and which restriction to set (eg user (who can read))
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_search($resource) {
  global $auth, $c_all, $cdg_sql, $ctu_sql_limit;

  $name = sql_search_text_parse($resource["name"]);
  $desc = sql_search_text_parse($resource["desc"]);
  
  $sql_order_dir = $resource["sql_order_dir"];
  $sql_order_field = $resource["sql_order_field"];
  
  $restriction = $resource["restriction"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
	$multidomain = sql_multidomain("resource");
	
  if ($restriction == "user") {
    $uid = $auth->auth["uid"];
    $rs = of_right_entity_for_consumer("Resource", "user", $uid, "read");
    if ((is_array($rs["ids"])) && (count($rs["ids"]) > 0)) {
      $where_restriction = " AND (resource_id IN (";
      foreach($rs["ids"] as $r_id) {
	$where_restriction .= "$coma'$r_id'";
	$coma = ",";
      }
      $where_restriction .= "))";
    }
  }

  $select = "SELECT Resource.*, resource_id as id
    FROM Resource";

  $where = " 1=1 $multidomain";

  // If a name indication has been specified, get it
  if (trim($name) != "") {
     $where .= " AND resource_name $like '$name%'";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != "") {
     $where .= " AND resource_description like '%$desc%'";
  }

  if ((trim($where) != "") || (trim($where_restriction) != "")) {
    $whereq = " WHERE $where $where_restriction";
  }

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "resource_name";
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
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  }

  return $obm_q;
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

  $timeupdate = sql_date_format($db_type, "Resource.resource_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "Resource.resource_timecreate", "timecreate");
	$multidomain = sql_multidomain("resource");
	
  $query = "SELECT Resource.*,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Resource 
         LEFT JOIN UserObm as c ON Resource.resource_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON Resource.resource_userupdate=u.userobm_id
    WHERE Resource.resource_id='$p_id' $multidomain";

  $obm_q->query($query);
  $obm_q->next_record();
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource insert query execution
// Parameters:
//   - $resource[]   : entry values
//     keys used : name, desc 
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_insert($resource) {
  global $auth, $cdg_sql, $obm;

  $name = $resource["name"];
  $desc = $resource["desc"];
  $qty = $resource["qty"];
  if ($qty == "") {
    $qty = "0";
  }
	$domain_id = $obm["domain_id"];
	
  $query = "INSERT INTO Resource (
     resource_timeupdate,
     resource_timecreate,
     resource_userupdate,
     resource_usercreate,
		 resource_domain_id,
     resource_name,
     resource_description,
     resource_qty
   ) VALUES (
    null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    ". $auth->auth["uid"] . ",
		'$domain_id',
    '$name',
    '$desc',
    '$qty'
   )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $id = get_resource_id($name);
    run_query_resource_right_init($id);
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Insert user Right for one ressource
// Parameters:
//   - $user_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_right_init($resource_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $line_q = new DB_OBM;
	
  $query = " DELETE
    FROM EntityRight 
    WHERE 
      entityright_entity = 'resource'
      AND entityright_entity_id = '$resource_id'";
  display_debug_msg($query, $cdg_sql, "run_query_resource_right_init(1)");
  $line_q->query($query);

  //---------------------------------------------------------------------------
  // we give public read and right permission
  //---------------------------------------------------------------------------
  $query = "INSERT INTO EntityRight (
    entityright_entity,
    entityright_entity_id,
    entityright_consumer,
    entityright_consumer_id,
    entityright_read,
    entityright_write
  ) values (
    'resource',
    '$resource_id',
    'user',
    '0',
    '1',
    '1'
  )";

  display_debug_msg($query, $cdg_sql, "run_query_resource_right_init(2)");
  $line_q->query($query);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Resource update query execution
// Parameters:
//   - $p_id     : resource id
//   - $resource[]   : entry values
//     keys used : name, desc 
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_update($p_id, $resource) {
  global $auth, $cdg_sql;

  $name = $resource["name"];
  $desc = $resource["desc"];
  $qty = $resource["qty"];
	$multidomain = sql_multidomain("resource");
	
  $query = "UPDATE Resource SET
    resource_timeupdate='". date("Y-m-d H:i:s")."',
    resource_userupdate=". $auth->auth["uid"] .",
    resource_name='$name',
    resource_description='$desc',
    resource_qty='$qty'
  WHERE resource_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Resource delete query execution
// Parameters:
//   - $p_id : resource id
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_delete($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Delete ResourceGroup
  $query = "DELETE FROM ResourceGroup 
    WHERE
      resourcegroup_resource_id='$p_id'";
  display_debug_msg($query, $cdg_sql, "run_query_resource_delete(resourcegroup)");
  $retour = $obm_q->query($query);

  // Delete Resource rights
  $query = "DELETE FROM EntityRight 
    WHERE
      entityright_entity_id='$p_id'
      AND entityright_entity = 'resource'";
  display_debug_msg($query, $cdg_sql, "run_query_resource_delete(entityright)");
  $retour = $obm_q->query($query);

  // Delete Resource events
  $query = "DELETE FROM EventEntity 
    WHERE
      evententity_entity_id='$p_id'
      AND evententity_entity = 'resource'";
  display_debug_msg($query, $cdg_sql, "run_query_resource_delete(resource event)");
  $retour = $obm_q->query($query);

  // Delete the resource
  $multidomain = sql_multidomain("resource");
  $query = "DELETE FROM Resource WHERE resource_id='$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_resource_delete(resource)");
  $retour = $obm_q->query($query);

  if (!$retour) return $retour;

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

  $name = $resource["name"];
  $desc  = $resource["desc"];

  // if a resource with same name exists, return false
  $co_q = run_query_check_resource($p_id, $name);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $p_id     : resource id
//   - $resource[]   : values checked
//     keys used : name, resource 
///////////////////////////////////////////////////////////////////////////////
function check_resource_data_form($p_id, $resource) {
  global $l_exist_error, $l_name_error, $l_desc_error;
  global $l_lname_error;
  global $err_msg, $action, $cdg_sql;

  $id = $resource["resource_id"];
  $name = $resource["name"];
  $desc = $resource["desc"];

  // MANDATORY: name 
  if (trim($name) == "") {
    $err_msg = $l_name_error." : ". $name;
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // if a resource with the same name already exists  return false           //
  /////////////////////////////////////////////////////////////////////////////
  $multidomain = sql_multidomain("resource");
  $query = "SELECT * FROM Resource WHERE resource_name='$name' $multidomain";
  if ($p_id > 0) {
    $query .= " AND resource_id!='$p_id'";
  }
  display_debug_msg($query, $cdg_sql, "check_resource_data_form()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  if ($obm_q->num_rows() > 0) {
    $err_msg = "$name : $l_exist_error";
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
function check_resource_can_delete($id) {
  global $err_msg, $ok_msg;

  $delete_ok = true;

  return $delete_ok;
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
  $multidomain = sql_multidomain("resource");
  if (isset($name)) {
    $query = "SELECT resource_id FROM Resource WHERE resource_name='$name' $multidomain";
    display_debug_msg($query, $cdg_sql, "get_resource_id()");
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f("resource_id");
  } else {
    return "";
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
  $multidomain = sql_multidomain("resource");
  if (isset($id)) {
    $query = "SELECT *
      FROM Resource
      WHERE resource_id='$id' $multidomain";
    display_debug_msg($query, $cdg_sql, "get_resource_infos()");
    $obm_q->query($query);
    $obm_q->next_record();
    $ret["name"] = $obm_q->f("resource_name");
    $ret["description"] = $obm_q->f("resource_description");
    return $ret;
  } else {
    return "";
  }
}


?>
