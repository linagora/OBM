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
function run_query_domain_search($domain, $restriction="") {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $domain["sql_order_dir"];
  $sql_order_field = $domain["sql_order_field"];
  $text = sql_search_text_parse($incident["text"]);
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  
  $where = "";

  // If a login indication has been specified, get it
  if (trim($text) != "") {
     $where .= " domain_label $like '$text%'
       OR domain_description like '$text%'";
  }

  if ((trim($where) != "") || (trim($where_restriction) != "")) {
    $whereq = " WHERE $where $where_restriction";
  }

  $select = "SELECT distinct Domain.*,
      domain_id as id
    FROM Domain";


  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "domain_label";
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct domain_id) FROM Domain $whereq");
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

  display_debug_msg($query, $cdg_sql, "run_query_domain_detail()");
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
  global $uid, $cdg_sql, $c_cr;
  
  $now = date("Y-m-d H:i:s");
  $label = $domain["label"];
  $desc = $domain["desc"];
  $domain_name = $domain["domain_name"];
  $alias = $domain["alias"];
  
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
    '$now',
    null,
    '$uid',
    '$label',
    '$desc',
    '$domain_name',
    '$alias')";
    
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
  global $uid, $cdg_sql;

  $now = date("Y-m-d H:i:s");
  $label = $domain["label"];
  $desc = $domain["desc"];
  $domain_name = $domain["domain_name"];
  $alias = $domain["alias"];
  
  $query = "UPDATE Domain SET
    domain_timeupdate='$now',
    domain_userupdate='$uid',
    domain_label='$label',
    domain_description='$desc',
    domain_name='$domain_name',
    domain_alias='$alias'
  WHERE domain_id='$d_id'";

  display_debug_msg($query, $cdg_sql, "run_query_domain_update()");
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
  global $err, $l_label_error;

  $label = $domain["label"];

  // MANDATORY: Label
  if (trim($label) == "") {
    $err["msg"] = $l_label_error." : ". $label;
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
  global $err, $ok_msg;
  global $l_link_user, $l_link_user_no;

  $delete_ok = true;

  // Links from Users
  $nb = get_linked_user_domain($p_id);
  if ($nb > 0) {
    $delete_ok = false;
    $err["msg"] .= "$l_link_user";
  } else {
    $ok_msg .= "$l_link_user_no";
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

  $nb = get_query_count("SELECT count(userobm_id) as nb
    FROM UserObm
    WHERE userobm_domain_id='$domain_id'");

  return $nb;
}


?>