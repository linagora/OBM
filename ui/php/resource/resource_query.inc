<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : resource_query.inc                                           //
//     - Desc : resource query & db File                                     //
// 2005-08-13 Florent Goalabre : Last Update 2005-08-13                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Resource query search query execution                                     //
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

  $select = "select Resource.*, resource_id as id from Resource";

  $where = " 1=1";

  // If a label indication has been specified, get it
  if (trim($label) != "") {
     $where .= " and resource_label $like '$label%'";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != "") {
     $where .= " and resource_description like '%$desc%'";
  }

  if (trim($where) != "") {
    $whereq = " where $where";
  }

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "resource_label";
  $orderq .= " order by $order $p_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from Resource $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource detail query execution                                           //
// Parameters:
//   - $p_id : resource id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "Resource.resource_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "Resource.resource_timecreate", "timecreate");

  $query = "select Resource.*,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    from Resource 
         left join UserObm as c on Resource.resource_usercreate=c.userobm_id
         left join UserObm as u on Resource.resource_userupdate=u.userobm_id
    where Resource.resource_id='$p_id'";

  $obm_q->query($query);
  $obm_q->next_record();
  display_debug_msg($query, $cdg_sql);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Resource insert query execution                                           //
// Parameters:
//   - $resource[]   : entry values
//     keys used : label, desc 
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($resource) {
  global $auth, $cdg_sql, $ctype_resource;

  $label = $resource["label"];
  $desc = $resource["desc"];
  $qty = $resource["qty"];if ($qty == "") $qty = "0";

  $query = "
   insert into Resource (
     resource_timeupdate,
     resource_timecreate,
     resource_userupdate,
     resource_usercreate,
     resource_label,
     resource_description,
     resource_qty
   ) values (
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
// Query Execution : Insert user Right for one ressource                    //
// Parameters:
//   - $user_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_right_init($resource_id) {
  global $cdg_sql,$cagenda_entities;

  $obm_q = new DB_OBM;
  $line_q = new DB_OBM;

  $query = " DELETE 
    FROM CalendarEntityRight 
    WHERE 
      calendarentityright_entity = '".$cagenda_entities['resource']. "' AND
      calendarentityright_entity_id = " . $resource_id;
  display_debug_msg($query, $cdg_sql);
  $line_q->query($query);

  //---------------------------------------------------------------------------
  // we give public read and right permission
  //---------------------------------------------------------------------------
  $query = "INSERT INTO CalendarEntityRight (
    calendarentityright_entity_id,
    calendarentityright_entity,
    calendarentityright_customer_id,
    calendarentityright_write,
    calendarentityright_read
  ) values (
    '$resource_id',
    '".$cagenda_entities['resource']."',
    '0',
    '1',
    '1'
  )";
  display_debug_msg($query, $cdg_sql);
  $line_q->query($query);

  return true;
}

///////////////////////////////////////////////////////////////////////////////
// Resource update query execution                                           //
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

  $query = "update Resource set
    resource_timeupdate='". date("Y-m-d H:i:s")."',
    resource_userupdate=". $auth->auth["uid"] .",
    resource_label='$label',
    resource_description='$desc',
    resource_qty='$qty'
  where resource_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Resource delete query execution                                           //
// Parameters:
//   - $p_id : resource id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql,$cagenda_entities;

  $query = "delete from Resource where resource_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  if (!$retour) return $retour;

  $query = "delete from CalendarEntityRight 
    where 
      calendarentityright_entity_id='$p_id' AND
      calendarentityright_entity = '".$cagenda_entities['resource']."'
  ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Resource environment checking (same resource exists ?)                    //
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
// Data checking and formatting                                              //
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
  $query = "select * from Resource where resource_label='$label'";
  if ($p_id > 0) {
    $query .= " and resource_id!='$p_id'";
  }
  display_debug_msg($query, $cdg_sql);
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
    $query = "select resource_id from Resource where resource_label='$label'";
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

  $obm_q = new DB_OBM;
  $user_q = get_all_users();
  while ($user_q->next_record()) {
    $query = "INSERT INTO CalendarRight VALUES ($id, ".$user_q->f("userobm_id").",1,1);";
    display_debug_msg($query, $cdg_sql);
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
  $query = "select userobm_id from UserObm";
  display_debug_msg($query, $cdg_sql);
  $retour = $obm_q->query($query) ;
}

</script>
