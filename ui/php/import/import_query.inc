<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : import_query.inc                                             //
//     - Desc : Import query File                                            //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Import Search query execution 
// Parameters :
//   - $import[]     : import search criteria
//     keys used     : name, contact
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_search($import, $p_new_order, $p_order_dir) {
  global $cdg_sql;
  
  $name = $import["name"];
  $contact = $list["contact"];

  $obm_q = new DB_OBM;
  $type = $obm_q->type;
  $like = sql_casei_like($type);

  $timeupdate = sql_date_format($type, "import_timeupdate", "timeupdate");
  $timecreate = sql_date_format($type, "import_timecreate", "timecreate");

  if ($email != '') {
    $where .= " and list_email $like '%$email%'";
  }

  $query = "select distinct import_id as Id,
    import_id,
    import_name,
    import_separator,
    import_enclosed,
    $timecreate,
    $timeupdate,
    A.userobm_login as usercreate,
    B.userobm_login as userupdate,
    datasource_name as import_datasource
  from 
    Import LEFT JOIN DataSource ON import_datasource_id=datasource_id
    LEFT JOIN UserObm as A ON Import.import_usercreate=A.userobm_id
    LEFT JOIN UserObm as B ON Import.import_userupdate=B.userobm_id
  where import_name $like '$name%'
    $where";

  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "import_name";
  $query .= " order by $order $p_order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Import detail query execution                                             //
// Parameters:
//   - $id : deal id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;

  $timeupdate = sql_date_format($type, "import_timeupdate", "timeupdate");
  $timecreate = sql_date_format($type, "import_timecreate", "timecreate");

  $query = "select *,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      datasource_name
  from Import
       LEFT JOIN DataSource on import_datasource_id = datasource_id
       LEFT JOIN UserObm as c on import_usercreate=c.userobm_id
       LEFT JOIN UserObm as u on import_userupdate=u.userobm_id
  where import_id = '$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query) ;
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : import insertion
// Parameters:
//   - $import[] : import hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($import) {
  global $auth, $cdg_sql;

  $id = $import["id"];
  $name = $import["name"];
  $dsrc = $import["datasource"];
  $sep = $import["sep"];
  $enc = $import["enclosed"];
  $desc = get_import_desc($import);

  $query = "insert into Import (import_timeupdate,
    import_timecreate,
    import_userupdate,
    import_usercreate,
    import_name,
    import_datasource_id,
    import_separator,
    import_enclosed,
    import_desc)
  values (null,
    '" . date("Y-m-d H:i:s") ."',
    null,
    '" . $auth->auth["uid"] . "',
    '$name',
    '$dsrc',
    '$sep',
    '$enc',
    '$desc')";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Import Update query execution                                             //
// Parameters:
//   - $import[] : import hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($import) {
  global $auth, $cdg_sql;

  $id = $import["id"];
  $name = $import["name"];
  $dsrc = $import["datasource"];
  $sep = $import["sep"];
  $enc = $import["enclosed"];
  $desc = get_import_desc($import);
  
  $query = "update Import set import_timeupdate='". date("Y-m-d H:i:s")."',
    import_userupdate='".$auth->auth["uid"]."',
    import_name='$name',
    import_datasource_id='$dsrc',
    import_separator='$sep',
    import_enclosed='$enc',
    import_desc='$desc'
  where import_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : import id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  // Delete the Import
  $query = "delete from Import where import_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a import (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : import id to exclude
// Returns:
//   - true (if an import exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_import_name_exists($name, $id="") {
  global $cdg_sql;

  if ($id != "") {
    $where_id = "and import_id != '$id'";
  }

  $query = "select import_id, import_name
    from Import
    where import_name='$name'
      $where_id";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the imports which matches the name or the datasource
// except the one given (update mode)
// Parameters:
//   - $import[] : import hash info : keys used : id, name, datasource
///////////////////////////////////////////////////////////////////////////////
function run_query_check_import($import) {
  global $cdg_sql;

  $id = $import["id"];
  $name = $import["name"];
  $dsrc = $import["datasource"];

  $obm_q = new DB_OBM;
  $type = $obm_q->type;
  $like = sql_casei_like($type);

  if ($id != "") {
    $where_id = "import_id != '$id' and ";
  }

  // We search for a similar name (as same name is already excluded)
  $wname = "import_name $like '%$name%'";

  // We search for the same datasource
  $wdsrc = "import_datasource_id = '$dsrc'";

  $query = "select distinct import_id, import_name, import_datasource_id,
       datasource_name
     from Import
          left join DataSource on import_datasource_id=datasource_id
     where $where_id
       ($wname and $wdsrc)";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Import context checking (same import exists ?)
// Parameters:
//   - $id       : import id
//   - $import[] : import values
//     keys used : name, datasource
// Returns:
//   - Import Database object with list of similar imports
///////////////////////////////////////////////////////////////////////////////
function check_import_context($id, $import) {
  global $cdg_sql;

  // return the imports with similar name and same datasource
  $import_q = run_query_check_import($import);

  return $import_q;
}


///////////////////////////////////////////////////////////////////////////////
// List Form Data checking and formatting
// Parameters:
//   - $id       : import id (empty on insertion)
//   - $import[] : values checked
//     keys used : name, datasource
///////////////////////////////////////////////////////////////////////////////
function check_data_form($id, $import) {
  global $l_import_exists, $l_fill_name;
  global $err_msg;

  $name = $import["name"];
  $dsrc = $import["datasource"];
  $file = $import["file"];

  // MANDATORY: Import name not empty
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // MANDATORY: Import name unique
  if (get_import_name_exists($name, $id)) {
    $err_msg = "$l_import_exists ($name)";
    return false;
  }

  return true;
}


</script>
