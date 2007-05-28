<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_query.inc                                               //
//     - Desc : todo query File                                              //
// 2003-15-09 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Todo search query execution
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_todolist($todo) {
  global $obm, $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $todo["sql_order_dir"];
  $sql_order_field = $todo["sql_order_field"];

  $uid = $obm["uid"];;
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "todo_priority";
  $sql_order_dir = (strcmp($sql_order_dir,"") != 0) ? $sql_order_dir : "desc";

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, "todo_date", "date_todo");
  $deadline = sql_date_format($db_type, "todo_deadline", "date_deadline");
	$multidomain = sql_multidomain("todo");
  $whereq = "WHERE todo_user = '$uid' $multidomain";

  // Order exceptions (order on calculated rows)
  if (strcmp($sql_order_field, "todo_update") == 0) {
    $order = "todo_priority";
  } else if (strcmp($sql_order_field,"") == 0) {
    $order = "todo_priority";
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $query = "SELECT
    todo_id as id,
    todo_id,
    $date,
    $deadline,
    todo_priority,
    todo_percent,
    todo_title
  FROM Todo
  $whereq
  $orderq
  $limit
  ";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from Todo $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "todo_search()");
    $obm_q->query($query);
  }
  
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Todo detail query execution
// Parameters:
//   - $todo[]       : company search criteria
//     keys used       id
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_detail($todo) {
  global $cdg_sql;

  $t_id = $todo["todo_id"];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "todo_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "todo_timecreate", "timecreate");
  $date = sql_date_format($db_type, "todo_date", "date_todo");
  $deadline = sql_date_format($db_type, "todo_deadline", "date_deadline");
	$multidomain = sql_multidomain("todo");
	
  $query = "
  SELECT
    Todo.*,
    uu.userobm_login as userupdate,
    uc.userobm_login as usercreate,
    $timeupdate,
    $timecreate,
    $date,
    $deadline,
    u.userobm_lastname as user_firstname,
    u.userobm_firstname as user_lastname,
    Author.userobm_lastname as author_firstname,
    Author.userobm_firstname as author_lastname
  FROM Todo
       LEFT JOIN UserObm as uc ON todo_usercreate = uc.userobm_id
       LEFT JOIN UserObm as uu ON todo_userupdate = uu.userobm_id
       LEFT JOIN UserObm as u ON todo_user = u.userobm_id,
     UserObm as Author
  WHERE todo_id = $t_id $multidomain
    AND Author.userobm_id = todo_usercreate";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Todo insert query execution
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid, title, sel_user, priority, deadline, content
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_insert($todo) {
  global $cdg_sql, $obm;

  $now = date("Y-m-d H:i:s");
  $uid = $obm["uid"];
  $title = $todo["title"];
  $users = $todo["userid"];
  $priority = $todo["priority"];
  $percent = $todo["percent"];
  if ($percent == "") {
    $percent = "0";
  }
  $deadline = of_isodate_convert($todo["deadline"],true);
  $content = $todo["content"];
	$domain_id = $obm["domain_id"];

  $obm_q = new DB_OBM;

  if (is_array($users)) {
    foreach ($users as $user) {

      $query = "INSERT INTO Todo (
        todo_timeupdate,
        todo_timecreate,
        todo_userupdate,
        todo_usercreate,
        todo_domain_id,
        todo_user,
        todo_date,
        todo_deadline,
        todo_priority,
        todo_percent,
        todo_title,
        todo_content)
      VALUES (
        '$now',
        '$now',
        '$uid',
        '$uid',
        '$domain_id',
        '$user',
        '$now',
        '$deadline',
        '$priority',
        '$percent',
        '$title',
        '$content'
      )";
      display_debug_msg($query, $cdg_sql);
      $retour = $obm_q->query($query);
    }
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Todo update query execution
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       id, uid, title, sel_user, priority, deadline, content
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_update($todo) {
  global $obm, $cdg_sql;

  $now = date("Y-m-d H:i:s");

  $t_id = $todo["todo_id"];
  $uid = $obm["uid"];
  $title = $todo["title"];
  $users = $todo["userid"];
  $user = $users[0];
  $priority = $todo["priority"];
  $deadline = of_isodate_convert($todo["deadline"],true);
  $content = $todo["content"];
  $percent = $todo["percent"];
  if ($percent == "") {
    $percent = "0";
  }

  $multidomain = sql_multidomain("todo");
		
  $query = "UPDATE Todo SET
    todo_timeupdate = '$now',
    todo_userupdate = '$uid',
    todo_user = '$user',
    todo_deadline = '$deadline',
    todo_priority = '$priority',
    todo_percent = '$percent',
    todo_title = '$title',
    todo_content = '$content'
  WHERE todo_id = '$t_id' $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Delete todo pass in $params
// Parameters:
//   - $parms : correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_delete($parms) {
  global $cdg_sql, $c_use_connectors;
  
	$multidomain = sql_multidomain("todo");
	
  foreach($parms as $key => $val) {
    $id_todo = strstr($key,"_");

    if ($id_todo != "") {
      $id_todo = substr($id_todo, 1);
    
      if ($val == "on") {
        $query = "DELETE FROM Todo WHERE todo_id = '$id_todo' $multidomain";
	display_debug_msg($query, $cdg_sql, "run_query_todo_delete");
        $obm_q = new DB_OBM;
        $obm_q->query($query);

	// If connectors in use
	if ($c_use_connectors) {
	  $now = date("Y-m-d H:i:s");
	  $query = "INSERT INTO
            DeletedTodo (deletedtodo_todo_id, deletedtodo_timestamp)
            VALUES ('$id_todo', '$now')";
	  display_debug_msg($query, $cdg_sql);
	  $retour = $obm_q->query($query);
	}
      }    
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Delete one todo
// Parameters:
//   - $t_id = todo id
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_delete_unique($t_id) {
  global $cdg_sql, $c_use_connectors;

  $multidomain = sql_multidomain("todo");

  $query = "DELETE FROM Todo WHERE todo_id = '$t_id' $multidomain";
  
  display_debug_msg($query, $cdg_sql, "run_query_todo_delete_unique");
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $now = date("Y-m-d H:i:s");
    $query = "INSERT INTO
      DeletedTodo (deletedtodo_todo_id, deletedtodo_timestamp)
      VALUES ('$t_id', '$now')";

    display_debug_msg($query, $cdg_sql);
    $retour = $obm_q->query($query);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Todo Form Data checking and formatting
// Parameters:
//   - $todo[] : values checked
///////////////////////////////////////////////////////////////////////////////
function check_todo_data_form($todo) {
  global $php_regexp_percent, $l_err_percent, $l_j_fill_title;
  global $err, $obm;

  $t_id = $todo["todo_id"];
  $uid = $obm["uid"];
  $title = $todo["title"];
  $users = $todo["userid"];
  $user = $users[0];
  $priority = $todo["priority"];
  $deadline = $todo["deadline"];
  $content = $todo["content"];
  $percent = $todo["percent"];

  // MANDATORY: Todo title
  if (trim($title) == "") {
    $err["msg"] = $l_j_fill_title;
    return false;
  }

  // Percent
  if (($percent != "") && (preg_match($php_regexp_percent, $percent) == 0)) {
    $err["msg"] = $l_err_percent . " : $percent";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the todo can be deleted
// Parameters:
//   - $p_id : todo id
// Returns:
//   true if the todo can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_todo_can_delete($p_id) {
  global $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}

?>
