<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_index.php                                            //
//     - Desc : Company Index File                                           //
// 2003-09-15 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the company search form
// - add                -- search fields  -- show the result set of search
// - delete             --                -- show the new company form
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
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("todo_query.inc");
require("todo_display.inc");
require("todo_js.inc");

page_close();
if ($action == "") $action = "index";
$uid = $auth->auth["uid"];

$todo = get_param_todo();
get_todo_action();
$perm->check();


if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $user_q = run_query_userobm();
  $todo_q = run_query_todolist($todo, "", "");

  $display["result"] = dis_todo_form($todo, $user_q);

  if ($todo_q->nf() != 0)
    $display["result"] .= dis_todo_list($todo, $todo_q);
  else
    $display["msg"] .= display_info_msg($l_no_found);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $todo_q = run_query_detail($todo);

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

} else if ($action == "update") {
///////////////////////////////////////////////////////////////////////////////

}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["header"] = generate_menu($menu, $section); // Menu
$display["head"] = display_head($l_todo);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $company hash
// returns : $company hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_todo() {
  global $uid, $param_todo;
  global $tf_title, $sel_user, $sel_priority, $tf_deadline, $ta_content;
  global $cdg_param;

  if (isset ($uid)) $todo["uid"] = $uid;
  if (isset ($param_todo)) $todo["id"] = $param_todo;

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
  global $todo_read, $todo_write, $todo_admin_read, $todo_admin_write;
  global $l_header_todo_list;

// Index
  $actions["TODO"]["index"] = array (
    'Name'     => $l_header_todo_list,
    'Url'      => "$path/todo/todo_index.php?action=index",
    'Right'    => $todo_read,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["TODO"]["detailconsult"] = array (
    'Url'      => "$path/todo/todo_index.php?action=add",
    'Right'    => $todo_read,
    'Condition'=> array ('None') 
                                    	 );

// Search
  $actions["TODO"]["add"] = array (
    'Url'      => "$path/todo/todo_index.php?action=add",
    'Right'    => $todo_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["TODO"]["delete"] = array (
    'Url'      => "$path/todo/todo_index.php?action=delete",
    'Right'    => $todo_write,
    'Condition'=> array ('None') 
                                     );

// Detail Consult
  $actions["TODO"]["update"]  = array (
    'Url'      => "$path/todo/todo_index.php?action=update",
    'Right'    => $todo_write,
    'Condition'=> array ('None') 
                                      );
}
</script>
