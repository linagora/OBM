<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_query.inc                                               //
//     - Desc : todo query File                                              //
// 2003-15-09 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Todo search query execution                                               //
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_todolist($todo, $p_new_order, $p_order_dir) {
  global $cdg_sql, $ctu_sql_limit;

  $uid = $todo["uid"];
  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "todo_priority";
  $p_order_dir = (strcmp($p_order_dir,"") != 0) ? $p_order_dir : "desc";

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, "todo_date", "date_todo");
  $deadline = sql_date_format($db_type, "todo_deadline", "date_deadline");

  $whereq = "where todo_user = '$uid'";

  $query = "select
    todo_id as id,
    todo_id,
    $date,
    $deadline,
    todo_priority,
    todo_title
  from Todo
  $whereq
  order by $order $p_order_dir
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
// Todo detail query execution                                               //
// Parameters:
//   - $todo[]       : company search criteria
//     keys used       id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($todo) {
  global $cdg_sql;

  $t_id = $todo["id"];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, "todo_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "todo_timecreate", "timecreate");
  $date = sql_date_format($db_type, "todo_date", "date_todo");
  $deadline = sql_date_format($db_type, "todo_deadline", "date_deadline");

  $query = "
  select
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
  from Todo
       left join UserObm as uc on todo_usercreate = uc.userobm_id
       left join UserObm as uu on todo_userupdate = uu.userobm_id
       left join UserObm as u on todo_user = u.userobm_id,
     UserObm as Author
  where todo_id = $t_id
    and Author.userobm_id = todo_usercreate
  ";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Todo add query execution                                                  //
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid, title, sel_user, priority, deadline, content
///////////////////////////////////////////////////////////////////////////////
function run_query_add($todo) {
  global $cdg_sql;

  $now = date("Y-m-d H:i:s");
  $uid = $todo["uid"];
  $title = $todo["title"];
  $user = $todo["sel_user"];
  $priority = $todo["priority"];
  $deadline = $todo["deadline"];
  $content = $todo["content"];

  $query = "insert into Todo (
    todo_timeupdate,
    todo_timecreate,
    todo_userupdate,
    todo_usercreate,
    todo_user,
    todo_date,
    todo_deadline,
    todo_priority,
    todo_title,
    todo_content)
  values (
    null,
    '$now',
    null,
    '$uid',
    '$user',
    '$now',
    '$deadline',
    '$priority',
    '$title',
    '$content'
  )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Todo update query execution                                               //
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       id, uid, title, sel_user, priority, deadline, content
///////////////////////////////////////////////////////////////////////////////
function run_query_update($todo) {
  global $cdg_sql;

  $now = date("Y-m-d H:i:s");

  $t_id = $todo["id"];
  $uid = $todo["uid"];
  $title = $todo["title"];
  $user = $todo["sel_user"];
  $priority = $todo["priority"];
  $deadline = $todo["deadline"];
  $content = $todo["content"];

  $query = "update Todo set
    todo_timeupdate = '$now',
    todo_userupdate = '$uid',
    todo_user = '$user',
    todo_deadline = '$deadline',
    todo_priority = '$priority',
    todo_title = '$title',
    todo_content = '$content'
  where todo_id = '$t_id'
  ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Delete todo pass in $params                                               //
// Parameters:
//   - $parms : correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($parms) {
  global $cdg_sql;

  foreach($parms as $key => $val) {
    $id_todo = strstr($key,"_"); 
    if ($id_todo != "") {
      $id_todo = substr($id_todo, 1);  
    
      if ($val == "on"){ 
        $query = "delete from Todo where todo_id = '$id_todo'";

	display_debug_msg($query, $cdg_sql, "run_query_delete");
        $delete_query = new DB_OBM;
        $delete_query->query($query);
      }    
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Delete one todo                                                           //
// Parameters:
//     -$t_id = todo id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_unique($t_id) {
  global $cdg_sql;

  $query = "delete from Todo where todo_id = '$t_id'";
  
  display_debug_msg($query, $cdg_sql, "run_query_delete_unique");
  $delete_query = new DB_OBM;
  return $delete_query->query($query);
}
