<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_query.inc                                               //
//     - Desc : todo query File                                              //
// 2003-15-09 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Todo search query execution                                               //
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_todolist($todo, $p_new_order, $p_order_dir) {

  $uid = $todo["uid"];

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "todo_priority";
  $p_order_dir = (strcmp($p_order_dir,"") != 0) ? $p_order_dir : "desc";

  $query = "select
    todo_id,
    UNIX_TIMESTAMP(todo_date) as date_todo,
    UNIX_TIMESTAMP(todo_deadline) as date_deadline,
    todo_priority,
    todo_title
  from Todo
  where todo_user = $uid
  order by $order $p_order_dir
  ";

  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  
  return $obm_db;
}

///////////////////////////////////////////////////////////////////////////////
// Todo search query execution                                               //
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       
//   - $p_new_order  : infos for order clause
//   - $p_order_dir  : direction for order clause (asc, desc)
///////////////////////////////////////////////////////////////////////////////
function run_query_add($todo) {
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
    '". date("Y-m-d H:i:s") ."',
    null,
    $uid,
    $user,
    '". date("Y-m-d H:i:s") ."',
    '$deadline',
    $priority,
    '$title',
    '$content'
  )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Delete task pass in $params                                              //
// Parameters:
//     -$parms = correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($todo) {

  $t_id = $todo["id"];

  $query = "
  select
    Todo.*,
    UNIX_TIMESTAMP(todo_date) as date_todo,
    UNIX_TIMESTAMP(todo_deadline) as date_deadline,
    User.userobm_lastname as user_firstname,
    User.userobm_firstname as user_lastname,
    Author.userobm_lastname as author_firstname,
    Author.userobm_firstname as author_lastname
  from Todo, UserObm User, UserObm Author
  where todo_id = $t_id
    and User.userobm_id = todo_user
    and Author.userobm_id = todo_usercreate
  ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Delete task pass in $params                                              //
// Parameters:
//     -$parms = correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($parms) {

  global $cdg_sql;

  foreach($parms as $key => $val) {
    $id_todo = strstr($key,"_"); 
    if( $id_todo != "" ) {//if we have a id todo
      $id_todo = substr($id_todo, 1);  
    
      if($val == "on"){ 
        $query = "delete from Todo where todo_id = $id_todo";

	display_debug_msg($query, $cdg_sql, "run_query_delete");
        $delete_query = new DB_OBM;
        $delete_query->query($query);
      }    
    }
  }
}

