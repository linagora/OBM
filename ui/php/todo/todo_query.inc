<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_query.inc                                               //
//     - Desc : todo query File                                              //
// 2003-15-09 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id:
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
  $cgd_sql;

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
// Todo add query execution                                                  //
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid, title, sel_user, priority, deadline, content
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
// Todo update query execution                                               //
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       id, uid, title, sel_user, priority, deadline, content
///////////////////////////////////////////////////////////////////////////////
function run_query_update($todo) {
  $t_id = $todo["id"];
  
  $uid = $todo["uid"];
  $title = $todo["title"];
  $user = $todo["sel_user"];
  $priority = $todo["priority"];
  $deadline = $todo["deadline"];
  $content = $todo["content"];

  $query = "update Todo set
    todo_timeupdate = '". date("Y-m-d H:i:s") ."',
    todo_userupdate = $uid,
    todo_user = $user,
    todo_deadline = '$deadline',
    todo_priority = $priority,
    todo_title = '$title',
    todo_content = '$content'
  where todo_id = $t_id
  ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}

///////////////////////////////////////////////////////////////////////////////
// Todo detail query execution                                               //
// Parameters:
//   - $todo[]       : company search criteria
//     keys used       id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($todo) {

  $t_id = $todo["id"];

  $query = "
  select
    Todo.*,
    todo_userupdate as userupdate,
    todo_usercreate as usercreate,
    UNIX_TIMESTAMP(todo_timeupdate) as timeupdate,
    UNIX_TIMESTAMP(todo_timecreate) as timecreate,
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
// Delete todo pass in $params                                               //
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

///////////////////////////////////////////////////////////////////////////////
// Delete one todo                                                           //
// Parameters:
//     -$t_id = todo id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete_unique($t_id) {
  global $cdg_sql;

  $query = "delete from Todo where todo_id = $t_id";
  
  display_debug_msg($query, $cdg_sql, "run_query_delete_unique");
  $delete_query = new DB_OBM;
  return $delete_query->query($query);
}

///////////////////////////////////////////////////////////////////////////////
// Delete task pass in $params                                              //
// Parameters:
//     -$parms = correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_admin_update($todo) {
  global $cdg_sql, $todo_order;

  $uid = $todo["uid"];
  $todo_order = $todo["sel_order"];

  $query = "
    update UserObmPref
    set userobmpref_value = '$todo_order'
    where userobmpref_option = 'todo_order'
    and userobmpref_user_id = $uid
    ";
  
  display_debug_msg($query, $cdg_sql, "run_query_admin_update");
  $obm_q = new DB_OBM;
  return $obm_q->query($query);
}

///////////////////////////////////////////////////////////////////////////////
// Query execution : Update the user top priority list
// Parameters:
//   - $user_id  : user Id
///////////////////////////////////////////////////////////////////////////////
function run_query_set_user_todo($user_id) {
  global $cdg_sql, $todo_order;
  global $todo_1_id, $todo_1_title, $todo_2_id, $todo_2_title;
  global $todo_3_id, $todo_3_title, $todo_4_id, $todo_4_title;
  global $todo_5_id, $todo_5_title;

  // create the todo list
  $query = "
    select
      todo_id,
      todo_title
    from Todo
    where todo_user = $user_id
    order by $todo_order desc
  ";

  display_debug_msg($query, $cdg_sql);
  $todolist_q = new DB_OBM;
  $todolist_q->query($query);

  $nb_todo = $todolist_q->nf();

  for ($rank = 1; $rank <= 5; $rank++) {

    $option = "todo_". $rank ."_id";

    if ($rank <= $nb_todo) {
      $todolist_q->next_record();

      $value = $todolist_q->f("todo_id");
      $title = $todolist_q->f("todo_title");
    } else {
      $value = 0;
      $title = 0;
    }

    if ($rank == 1) {
      $todo_1_id = $value;
      $todo_1_title = $title;
    } else if ($rank == 2) {
      $todo_2_id = $value;
      $todo_2_title = $title;
    } else if ($rank == 3) {
      $todo_3_id = $value;
      $todo_3_title = $title;
    } else if ($rank == 4) {
      $todo_4_id = $value;
      $todo_4_title = $title;
    } else if ($rank == 5) {
      $todo_5_id = $value;
      $todo_5_title = $title;
    }

    $query = "update UserObmPref
            set userobmpref_value='$value'
            where userobmpref_user_id ='$user_id'
              and userobmpref_option = '$option'";   

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM ;
    $ret = $obm_q->query($query);
  }

  return $ret;
}
