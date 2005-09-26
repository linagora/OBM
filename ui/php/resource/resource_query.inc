<script language="php">
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
//   - $p_new_order : infos for order clause
//   - $p_order_dir : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($resource, $p_new_order, $p_order_dir) {
  global $c_all, $cdg_sql, $ctu_sql_limit;
  global $ctype_resource;

  $label = sql_search_text_parse($resource["label"]);
  $desc = sql_search_text_parse($resource["description"]);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $select = "SELECT Resource.*, resource_id as id
    FROM Resource";

  $where = " 1=1";

  // If a label indication has been specified, get it
  if (trim($label) != "") {
     $where .= " AND resource_label $like '$label%'";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != "") {
     $where .= " AND resource_description like '%$desc%'";
  }

  if (trim($where) != "") {
    $whereq = " WHERE $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "resource_label";
  $orderq .= " ORDER BY $order $p_order_dir";

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
function run_query_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "Resource.resource_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "Resource.resource_timecreate", "timecreate");

  $query = "SELECT Resource.*,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Resource 
         LEFT JOIN UserObm as c ON Resource.resource_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON Resource.resource_userupdate=u.userobm_id
    WHERE Resource.resource_id='$p_id'";

  $obm_q->query($query);
  $obm_q->next_record();
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource insert query execution
// Parameters:
//   - $resource[]   : entry values
//     keys used : label, desc 
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($resource) {
  global $auth, $cdg_sql, $ctype_resource;

  $label = $resource["label"];
  $desc = $resource["desc"];
  $qty = $resource["qty"];if ($qty == "") $qty = "0";

  $query = "INSERT INTO Resource (
     resource_timeupdate,
     resource_timecreate,
     resource_userupdate,
     resource_usercreate,
     resource_label,
     resource_description,
     resource_qty
   ) VALUES (
    null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    ". $auth->auth["uid"] . ",
    '$label',
    '$desc',
    '$qty'
   )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    $id=get_resource_id($label);
    run_query_right_init($id);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Insert user Right for one ressource
// Parameters:
//   - $user_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_right_init($resource_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $line_q = new DB_OBM;

  $query = " DELETE
    FROM EntityRight 
    WHERE 
      entityright_entity = 'resource'
      AND entityright_entity_id = '$resource_id'";
  display_debug_msg($query, $cdg_sql);
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

  display_debug_msg($query, $cdg_sql);
  $line_q->query($query);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Resource update query execution
// Parameters:
//   - $p_id     : resource id
//   - $resource[]   : entry values
//     keys used : label, desc 
///////////////////////////////////////////////////////////////////////////////
function run_query_update($p_id, $resource) {
  global $auth, $cdg_sql, $ctype_resource;

  $label = $resource["label"];
  $desc = $resource["desc"];
  $qty = $resource["qty"];

  $query = "UPDATE Resource SET
    resource_timeupdate='". date("Y-m-d H:i:s")."',
    resource_userupdate=". $auth->auth["uid"] .",
    resource_label='$label',
    resource_description='$desc',
    resource_qty='$qty'
  WHERE resource_id='$p_id'";

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
function run_query_delete($p_id) {
  global $cdg_sql;

  $query = "DELETE FROM Resource WHERE resource_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  if (!$retour) return $retour;

  $query = "DELETE FROM EntityRight 
    WHERE
      entityright_entity_id='$p_id'
      AND entityright_entity = 'resource'
";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

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

  $label = $resource["label"];
  $desc  = $resource["desc"];

  // if a resource with same name exists, return false
  $co_q = run_query_check_resource($p_id, $label);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $p_id     : resource id
//   - $resource[]   : values checked
//     keys used : label, resource 
///////////////////////////////////////////////////////////////////////////////
function check_data_form($p_id, $resource) {
  global $l_exist_error, $l_label_error, $l_desc_error;
  global $l_lname_error;
  global $err_msg, $action, $cdg_sql;

  $id = $resource["id"];
  $label = $resource["label"];
  $desc = $resource["desc"];

  // MANDATORY: label 
  if (trim($label) == "") {
    $err_msg = $l_label_error." : ". $label;
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // if a resource with the same name already exists  return false           //
  /////////////////////////////////////////////////////////////////////////////
  $query = "SELECT * FROM Resource WHERE resource_label='$label'";
  if ($p_id > 0) {
    $query .= " AND resource_id!='$p_id'";
  }
  display_debug_msg($query, $cdg_sql, "check_data_form()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  if ($obm_q->num_rows() > 0) {
    $err_msg = "$label : $l_exist_error";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the Id of one Ressource from its label
// Parameters:
//   - $label  : label
// Returns:
//   - resource's Id
///////////////////////////////////////////////////////////////////////////////
function get_resource_id($label) {
  global $cdg_sql;

  $obm_q = new DB_OBM ;
  if (isset($label)) {
    $query = "SELECT resource_id FROM Resource WHERE resource_label='$label'";
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query) ;
    $obm_q->next_record() ;
    return $obm_q->f("resource_id") ;
  } else {
    return "" ;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : set the Ressource calendar Rights
// Parameters:
//   - $id  : id 
// Returns:
//   - resource's Id
///////////////////////////////////////////////////////////////////////////////
function set_resource_calendar_rights($id) {
  global $cdg_sql;

  $user_q = get_all_users();
  while ($user_q->next_record()) {
    $u_id = $user_q->f("userobm_id");
    $query = "INSERT INTO EntityRight (
        entityright_entity,
        entityright_entity_id,
        entityright_consumer,
        entityright_consumer_id,
        entityright_read,
        entityright_write
      VALUES (
        'resource',
        '$id',
        'user',
        '$u_id',
        '1',
        '1');";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query) ;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get all users id 
// Parameters:
// Returns:
//   - users Id
///////////////////////////////////////////////////////////////////////////////
function get_all_users() {
  global $cdg_sql,$ctype_resource;

  $obm_q = new DB_OBM;
  $query = "SELECT userobm_id FROM UserObm";
  display_debug_msg($query, $cdg_sql);
  $retour = $obm_q->query($query) ;
}

</script>
