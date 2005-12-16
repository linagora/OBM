<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_index.php                                            //
//     - Desc : Company Index File                                           //
// 2003-09-15 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the todo index
// - insert             -- search fields  -- insert a new todo
// - delete             --                -- delete selected todos
// - delete_unique      --                -- delete one todo
// - update             --                -- update a todo
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "todo";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("todo_query.inc");
require("todo_display.inc");

if (!(($action == "detailupdate") && ($popup)))
  require("todo_js.inc");

if ($action == "") $action = "index";
$uid = $auth->auth["uid"];

$todo = get_param_todo();
get_todo_action();
$perm->check_permissions($module, $action);

page_close();


if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["result"] = dis_todo_form($todo);
  $display["result"] .= dis_todo_search_list($todo);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $todo_q = run_query_detail($todo);
  $display["detailInfo"] = display_record_info($todo_q);
  $display["detail"] .= dis_todo_detail($todo, $todo_q);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_todo_data_form($todo)) {
    $retour = run_query_insert($todo);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_insert_ok);
    } else {
      $display["msg"] .= display_err_msg($l_insert_error);
    }
    $display["result"] = dis_todo_form("");
    $display["result"] .= dis_todo_search_list($todo);
  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_todo_form($todo);
  }

} else if ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($HTTP_POST_VARS);
  $display["result"] = dis_todo_form($todo);
  $display["result"] .= dis_todo_search_list($todo);

} else if ($action == "delete_unique") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_todo($todo["id"])) {
    $retour = run_query_delete_unique($todo["id"]);
    if ($retour) {
      $display["msg"] = display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] = display_err_msg($l_delete_error);
    }
    $display["result"] = dis_todo_form($todo);
    $display["result"] .= dis_todo_search_list($todo);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $todo_q = run_query_detail($todo);
    $display["detail"] .= dis_todo_detail($todo, $todo_q);
  }

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $todo_q = run_query_detail($todo);
  $display["result"] = dis_todo_form($todo, $todo_q);

} else if ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_todo_data_form($todo)) {
    $retour = run_query_update($todo);

    if ($popup) {
      $display["result"] .= "
      <script language=\"javascript\">
       window.opener.location.href=\"$path/todo/todo_index.php?action=index\";
       window.close();
      </script>";
    } else {
      $action = "index";
      $display["result"] = dis_todo_form("");
      $display["result"] .= dis_todo_search_list($todo);
    }

    // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_todo_form($todo);
  }

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "todo", 1);
  $display["detail"] = dis_todo_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "todo", 1);
  $display["detail"] = dis_todo_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "todo", 1);
  $display["detail"] = dis_todo_display_pref($prefs);
}

///////////////////////////////////////////////////////////////////////////////
// Todo top list (same as the bookmarks : id and titles are registered)
///////////////////////////////////////////////////////////////////////////////
// If the todo list was updated, we reload the todo in session
if (in_array($action, array("insert", "detailupdate", "delete", "delete_unique")))
  session_load_user_todos();


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = display_menu($module);
}
$display["head"] = display_head($l_todo);
$display["end"] = display_end();
     
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $company hash
// returns : $company hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_todo() {
  global $uid, $param_todo, $action, $popup, $new_order, $order_dir;
  global $tf_title, $sel_user_id, $sel_priority, $tf_deadline, $ta_content;
  global $tf_percent;

  if (isset ($uid)) $todo["uid"] = $uid;
  if (isset ($action)) $todo["action"] = $action;
  if (isset ($popup)) $todo["popup"] = $popup;
  if (isset ($param_todo)) $todo["id"] = $param_todo;
  if (isset ($new_order)) $todo["new_order"] = $new_order;
  if (isset ($order_dir)) $todo["order_dir"] = $order_dir;

  // Todo form
  if (isset ($tf_title)) $todo["title"] = $tf_title;
  if (isset ($tf_deadline)) $todo["deadline"] = $tf_deadline;
  if (isset ($sel_priority)) $todo["priority"] = $sel_priority;
  if (isset ($tf_percent)) $todo["percent"] = $tf_percent;
  if (isset ($ta_content)) $todo["content"] = $ta_content;

  // sel_user_id can be filled by sel_user_id or sel_ent (see below)
  if (is_array($sel_user_id)) {
    while ( list( $key, $value ) = each( $sel_user_id ) ) {
      // sel_user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 10),"data-user-") == 0) {
	$data = explode("-", $value);
	$id = $data[2];
	$todo["sel_user_id"][] = $id;
      } else {
	// direct id
	$todo["sel_user_id"][] = $value;
      }
    }
  }
  display_debug_param($todo);

  return $todo;
}


///////////////////////////////////////////////////////////////////////////////
// Company Action 
///////////////////////////////////////////////////////////////////////////////
function get_todo_action() {
  global $todo, $actions, $path;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_header_list, $l_header_delete, $l_header_update;
  global $l_header_consult, $l_header_admin, $l_header_display;

// Index
  $actions["todo"]["index"] = array (
    'Name'     => $l_header_list,
    'Url'      => "$path/todo/todo_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["todo"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/todo/todo_index.php?action=detailconsult&amp;param_todo=". $todo["id"],
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
                                    	 );

// Insert a todo
  $actions["todo"]["insert"] = array (
    'Url'      => "$path/todo/todo_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    	 );

// Delete a list of todo
  $actions["todo"]["delete"] = array (
    'Url'      => "$path/todo/todo_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["todo"]["update"]  = array (
    'Url'      => "$path/todo/todo_index.php?action=update&amp;param_todo=". $todo["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                      );

// Update
  $actions["todo"]["detailupdate"]  = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/todo/todo_index.php?action=detailupdate&amp;param_todo=". $todo["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate') 
                                      );

// Delete a todo
  $actions["todo"]["delete_unique"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/todo/todo_index.php?action=delete_unique&amp;param_todo=". $todo["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 

                                     );

// Display
   $actions["todo"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/todo/todo_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Préférences
   $actions["todo"]["dispref_display"] = array (
    'Url'      => "$path/todo/todo_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
   $actions["todo"]["dispref_level"]  = array (
    'Url'      => "$path/todo/todo_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

}


</script>
