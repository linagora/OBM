<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_index.php                                            //
//     - Desc : Company Index File                                           //
// 2003-09-15 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the company search form
// - add                -- search fields  -- show the result set of search
// - delete             --                -- show the new company form
// - delete_unique      --                -- show the new company form
// - update             --                -- show the new company form
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms  Management                                          //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COM";
$menu = "TODO";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("todo_query.inc");
require("todo_display.inc");

if (!(($action == "detailupdate") && ($popup)))
  require("todo_js.inc");

page_close();
if ($action == "") $action = "index";
$uid = $auth->auth["uid"];

$todo = get_param_todo();
get_todo_action();
$perm->check_permissions($menu, $action);


if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $user_q = run_query_userobm();
  $todo_q = run_query_todolist($todo, $new_order, $order_dir);
  $display["result"] = dis_todo_form($todo, $user_q);
  if ($todo_q->nf() != 0)
    $display["result"] .= dis_todo_list($todo, $todo_q);
  else
    $display["msg"] .= display_info_msg($l_no_found);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $todo_q = run_query_detail($todo);
  $display["detailInfo"] = display_record_info($todo_q);
  $display["result"] .= dis_todo_detail($todo, $todo_q);

} else if ($action == "add") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_add($todo);
  $user_q = run_query_userobm();
  $todo_q = run_query_todolist($todo, "", "");

  $display["result"] = dis_todo_form($todo, $user_q);

  if ($todo_q->nf() != 0)
    $display["result"] .= dis_todo_list($todo, $todo_q);
  else
    $display["msg"] = display_info_msg($l_no_found);

} else if ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($HTTP_POST_VARS);
  $user_q = run_query_userobm();
  $todo_q = run_query_todolist($todo, "", "");

  $display["result"] = dis_todo_form($todo, $user_q);

  if ($todo_q->nf() != 0)
    $display["result"] .= dis_todo_list($todo, $todo_q);
  else
    $display["msg"] = display_info_msg($l_no_found);

} else if ($action == "delete_unique") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete_unique($param_todo);
  $user_q = run_query_userobm();
  $todo_q = run_query_todolist($todo, "", "");
  $display["result"] = dis_todo_form($todo, $user_q);

  if ($retour)
    $display["msg"] = display_ok_msg($l_delete_ok);
  else
    $display["msg"] = display_err_msg($l_delete_error);

  if ($todo_q->nf() != 0)
    $display["result"] .= dis_todo_list($todo, $todo_q);
  else
    $display["msg"] .= display_info_msg($l_no_found);

} else if ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
    $user_q = run_query_userobm();
    $todo_q = run_query_detail($todo);

    $display["result"] = dis_todo_form($todo, $user_q, $todo_q);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_update($todo);
    
  if ($popup) {
    $display["result"] .= "
    <script language=\"javascript\">
     window.opener.location.href=\"$path/todo/todo_index.php?action=index\";
     window.close();
    </script>
    ";

  } else if ($uid == $todo["sel_user"]) {
    $todo_q = run_query_detail($todo);
    $display["detailInfo"] = display_record_info($todo_q);
    $display["result"] .= dis_todo_detail($todo, $todo_q);

  } else {
    $action = "index";
    $user_q = run_query_userobm();
    $todo_q = run_query_todolist($todo, "", "");
    $display["result"] = dis_todo_form($todo, $user_q);

    if ($todo_q->nf() != 0)
      $display["result"] .= dis_todo_list($todo, $todo_q);
    else
      $display["msg"] .= display_info_msg($l_no_found);
  }

}  elseif ($action == "display") {
/////////////////////////////////////////////////////////////////////////
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "todo", 1);

  $display["detail"] = dis_todo_display_pref($pref_search_q);

} else if ($action == "dispref_display") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);

  $pref_search_q = run_query_display_pref($auth->auth["uid"], "todo", 1);

  $display["detail"] = dis_todo_display_pref($pref_search_q);

} else if ($action == "dispref_level") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);

  $pref_search_q = run_query_display_pref($auth->auth["uid"], "todo", 1);

  $display["detail"] = dis_todo_display_pref($pref_search_q);
}

///////////////////////////////////////////////////////////////////////////////
// Todo top list (same as the bookmarks : id and titles are registered)
///////////////////////////////////////////////////////////////////////////////
// if a todo was modified for an other user, we update his prefs
if ((in_array($action, array("add", "detailupdate")))
    && ($sel_user != $uid))
  run_query_set_user_todo($sel_user);;

//we update the current user prefs
if (in_array($action, array("add", "detailupdate", "delete", "delete_unique")))
  run_query_set_user_todo($uid);

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////

if (($action != "update") or (!($popup)))
  $display["header"] = generate_menu($menu, $section);
     
$display["head"] = display_head($l_todo);
$display["end"] = display_end();
     
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $company hash
// returns : $company hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_todo() {
  global $uid, $param_todo, $action, $popup;
  global $tf_title, $sel_user, $sel_priority, $tf_deadline, $ta_content;
  global $cdg_param;

  if (isset ($uid)) $todo["uid"] = $uid;
  if (isset ($action)) $todo["action"] = $action;
  if (isset ($popup)) $todo["popup"] = $popup;
  if (isset ($param_todo)) $todo["id"] = $param_todo;

  // Todo form
  if (isset ($tf_title)) $todo["title"] = $tf_title;
  if (isset ($tf_deadline)) $todo["deadline"] = $tf_deadline;
  if (isset ($sel_user)) $todo["sel_user"] = $sel_user;
  if (isset ($sel_priority)) $todo["priority"] = $sel_priority;
  if (isset ($ta_content)) $todo["content"] = $ta_content;

  return $todo;
}


///////////////////////////////////////////////////////////////////////////////
// Company Action 
///////////////////////////////////////////////////////////////////////////////
function get_todo_action() {
  global $todo, $actions, $path;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_header_todo_list, $l_header_delete, $l_header_update;
  global $l_header_admin, $l_header_display;

// Index
  $actions["TODO"]["index"] = array (
    'Name'     => $l_header_todo_list,
    'Url'      => "$path/todo/todo_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["TODO"]["detailconsult"] = array (
    'Url'      => "$path/todo/todo_index.php?action=add",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	 );

// Add a todo
  $actions["TODO"]["add"] = array (
    'Url'      => "$path/todo/todo_index.php?action=add",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	 );

// Delete a list of todo
  $actions["TODO"]["delete"] = array (
    'Url'      => "$path/todo/todo_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["TODO"]["update"]  = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/todo/todo_index.php?action=update&amp;param_todo=". $todo["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate') 
                                      );

// Update
  $actions["TODO"]["detailupdate"]  = array (
    'Url'      => "$path/todo/todo_index.php?action=detailupdate&amp;param_todo=". $todo["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                      );

// Delete a todo
  $actions["TODO"]["delete_unique"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/todo/todo_index.php?action=delete_unique&amp;param_todo=". $todo["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 

                                     );

// Display
   $actions["TODO"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/todo/todo_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Préférences
   $actions["TODO"]["dispref_display"] = array (
    'Url'      => "$path/todo/todo_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
   $actions["TODO"]["dispref_level"]  = array (
    'Url'      => "$path/todo/todo_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

}

</script>
