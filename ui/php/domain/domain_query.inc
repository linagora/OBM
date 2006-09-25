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
//   - $p_new_order : infos for order clause
//   - $p_order_dir : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_search($domain, $p_new_order, $p_order_dir, $restriction="") {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $label = sql_search_text_parse($domain["label"]);
  $desc = sql_search_text_parse($domain["desc"]);
  $archive = $domain["archive"];
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  
  $where = "";

  // If a login indication has been specified, get it
  if (trim($label) != "") {
     $where .= " domain_label $like '$label%'";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != "") {
     if (trim($where) != "") $where .= " AND";
     $where .= " domain_description $like '%$desc%'";
  }

  if ($archive != "1") {
    if (trim($where) != "") $where .= " AND";
    $where .= " domain_archive = '0'";
  }

  if ((trim($where) != "") || (trim($where_restriction) != "")) {
    $whereq = " WHERE $where $where_restriction";
  }

  $select = "SELECT distinct Domain.*,
      domain_id as id
    FROM Domain";


  // ORDER construction
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "domain_label";
  $orderq .= " ORDER BY $order $p_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct domain_id) FROM Domain $join_grp $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_domain_search()");
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

  $query = "SELECT Domain.*
    FROM Domain
    WHERE domain_id='$p_id'";

  $obm_q->query($query);
  $obm_q->next_record();
  
  display_debug_msg($query, $cdg_sql, "run_query_domain_detail()");

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
  global $uid, $cdg_sql, $c_cr;
  

  $label = $domain["label"];
  $archive = ($domain["archive"] ? "1" : "0");
  $desc = $domain["desc"];
  
  $query = "INSERT INTO Domain (domain_timeupdate,
    domain_timecreate,
    domain_userupdate,
    domain_usercreate,
    domain_label,
    domain_description,
    domain_archive)
  VALUES (
    null,
    '" . date("Y-m-d H:i:s") . "',
    null,
    '$uid',
    '$label',
    '$desc',
    '$archive')";
    
  display_debug_msg($query, $cdg_sql, "run_query_domain_insert(1)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    // Get the domain id
    $query = "SELECT domain_id as id
    FROM Domain
    WHERE domain_label = '$label'";
  
    display_debug_msg($query, $cdg_sql, "run_query_domain_detail(2)");
    
    $obm_q->query($query);
    $obm_q->next_record();
    $id = $obm_q->f("id");
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// domain update query execution
// Parameters:
//   - $p_id     : domain id
//   - $domain[]   : entry values
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_update($d_id, $domain) {
  global $uid, $cdg_sql, $password_encryption;

  $label = $domain["label"];
  $archive = ($domain["archive"] ? "1" : "0");
  $desc = $domain["desc"];
  
  $query = "UPDATE Domain SET
    domain_timeupdate='". date("Y-m-d H:i:s")."',
    domain_userupdate='$uid',
    domain_label='$label',
    domain_archive='$archive',
    domain_description='$desc'
  WHERE domain_id='$d_id'";

  display_debug_msg($query, $cdg_sql, "run_query_domain_detail()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// domain delete query execution
// Parameters:
//   - $p_id : domain id
///////////////////////////////////////////////////////////////////////////////
function run_query_domain_delete($p_id) {
  global $cdg_sql, $c_use_connectors;

  $query = "DELETE FROM Domain WHERE domain_id='$p_id'";
  
  display_debug_msg($query, $cdg_sql, "run_query_domain_delete()");
  $obm_q = new DB_OBM;
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
  global $err_msg, $l_label_error;

  $label = $domain["label"];

  // MANDATORY: Label
  if (trim($label) == "") {
    $err_msg = $l_label_error." : ". $label;
    return false;
  }
  
  
  return true;
}

///////////////////////////////////////////////////////////////////////////////
// Check if the domain can be deleted
// Parameters:
//   - $d_id : domain_id
// Returns:
//   true if the domain can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_domain_can_delete($p_id) {
  global $err_msg, $ok_msg;
  global $l_link_timetask, $l_link_timetask_no;

  $delete_ok = true;

  // Links from Time management (referencing projects)
  $nb = get_linked_user_domain($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_timetask";
  } else {
    $ok_msg .= "$l_link_timetask_no";
  }

  return $delete_ok;
}

///////////////////////////////////////////////////////////////////////////////
// Count number of domain in a domain
// Parameters:
//   - $domain_id : domain_id
// Returns:
//   Number of users in a domain
///////////////////////////////////////////////////////////////////////////////
function get_linked_user_domain($domain_id) {
  $nb = get_query_count("SELECT count(userobm_id) as nb FROM UserObm WHERE userobm_domain_id='$domain_id'");
  return $nb;
}



?>