<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : project_index.php                                            //
//     - Desc : Project Index File                                           //
// 2003-07-08 Bastien Continsouzas
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions           -- Parameter
// - index (default) -- search fields  -- show the project search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new project form
// - detailconsult   -- $param_project -- show the project detail
// - detailupdate    -- $param_project -- show the project detail form
// - insert          -- form fields    -- insert the project
// - update          -- form fields    -- update the project
// - check_delete    -- $param_project -- check links before delete
// - delete          -- $param_project -- delete the project
// - task            -- $param_project -- show project tasks main screen
// - task_add        -- form fields    -- 
// - task_update     -- form fields    -- 
// - task_del        -- form fields    -- 
// - member          -- $param_project -- show project members main screen
// - sel_member      -- ext: user
// - member_add      -- form fields    --
// - member_del      -- form fields    -- 
// - member_update   -- form fields    -- 
// - allocate        -- $param_project -- 
// - allocate_update -- $param_project -- 
// - progress        -- $param_project -- show the project progress form
// - progress_update -- form fields    -- update the project progress
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms  Management                                          //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "PROD";
$menu = "PROJECT";
$extra_css = "project.css";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("project_query.inc");
require("project_display.inc");
require("project_js.inc");

$uid = $auth->auth["uid"];
$perms = $auth->auth["perm"];

update_last_visit("project", $param_project, $action);

page_close();
if ($action == "") $action = "index";
$project = get_param_project();
get_project_action();
$perm->check_permissions($menu, $action);


if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_project_search_form($project);
  $display["msg"] .= display_info_msg($l_no_display);

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_project_search_form($project);
  $display["result"] = dis_project_search_list($project);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_project_form($action, "", $project);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    dis_project_consult($param_project);
  } else {
    $display["msg"] .= display_err_msg($l_query_error);
  }
  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project_q = run_query_detail($param_project);
    $display["detailInfo"] = display_record_info($project_q);
    $display["detail"] = html_project_form($action, $project_q, $project);
  } else {
    $display["msg"] .= display_err_msg($l_query_error);
  }

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_project_form("", $project)) {
    $project["id"] = run_query_insert($project);
    if ($project["id"]) {
      $display["msg"] .= display_ok_msg($l_insert_ok);
      dis_project_consult($project["id"]);
    } else {
      $display["msg"] .= display_err_msg("$l_insert_error : $err_msg");
    }
  } else { 
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_project_form($action, "", $project);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_project_form($param_project, $project)) {
    $retour = run_query_update($param_project, $project);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
      dis_project_consult($param_project);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
      dis_project_consult($param_project);
    }
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_project_form($action, "", $project);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_check_links($param_project);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($param_project);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
    $display["search"] = dis_project_search_form($project);
    $display["msg"] .= display_info_msg($l_no_display);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
    dis_project_consult($param_project);
  }

} elseif ($action == "task")  {
///////////////////////////////////////////////////////////////////////////////
  $project["name"] = run_query_projectname($param_project);
  $tasks_q = run_query_tasks($param_project);
  $display["detail"]  = html_project_task_form($tasks_q, $project);
  $display["detail"] .= html_project_tasklist($tasks_q, $project);

} elseif ($action == "task_add")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_task_form($param_project, $project)) {
    $retour = run_query_task_insert($project);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_task_insert_ok);
    } else {
      $display["msg"] .= display_err_msg("$l_task_insert_error");
    }
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $display["detail"] = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  } else { 
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $display["detail"]  = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  }

} elseif ($action == "task_update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_task_form($param_project, $project)) {
    $retour = run_query_task_update($project);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_task_update_ok);
    } else {
      $display["msg"] .= display_err_msg("$l_task_update_error");
    }
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $display["detail"] = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  } else { 
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $display["detail"]  = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  }

} elseif ($action == "task_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($project["tsk_nb"] > 0) {
    $nb = run_query_task_delete($project);
    if ($nb == $project["tsk_nb"]) {
      $display["msg"] .= display_ok_msg("$l_task_delete_ok");
    } else {
      $display["msg"] .= display_warn_msg("$l_task_delete_error");
    }
  } else {pr
    $display["msg"] .= display_err_msg("$l_no_task_del");
  }
  $project["name"] = run_query_projectname($param_project);
  $tasks_q = run_query_tasks($param_project);
  $display["detail"] = html_project_task_form($tasks_q, $project);
  $display["detail"] .= html_project_tasklist($tasks_q, $project);
      
} elseif ($action == "member")  {
///////////////////////////////////////////////////////////////////////////////
  $project["name"] = run_query_projectname($param_project);
  $tasks_q = run_query_tasks($param_project);
  $members_q = run_query_members($param_project);
  $display["detail"] .= html_project_member_form($members_q, $project );

} elseif ($action == "allocate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else {
      $allo_q = run_query_allocation($param_project);
      $display["detail"] = html_project_allocate_form($tasks_q, $members_q, $allo_q, $project); 
    }
  }

} elseif ($action == "allocate_update")  {
///////////////////////////////////////////////////////////////////////////////
//  if (check_member_form($param_project, $project)) {
  $ins_err = run_query_allocate_update($project);
  // Create an entry in the ProjectStat log
  $retour = run_query_statlog($param_project);
  if (!($retour))
    $ins_err = 1;
  if (!($ins_err)) {
    $display["msg"] .= display_ok_msg($l_allocate_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_allocate_update_error);
  }
  dis_project_consult($param_project);

} elseif ($action == "progress")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $allo_q = run_query_allocation($param_project);
    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else if (($allo_q == 0) or ($allo_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else {
      $display["detail"] = html_project_advance($members_q, $allo_q, $tasks_q, $project);
    }
  }
  
} elseif ($action == "progress_update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_advance_form($project)) {
    $ins_err = run_query_progress($project);
    // Create an entry in the ProjectStat log
    $retour = run_query_statlog($param_project);
    if (!($retour))
      $ins_err = 1;
    if (!($ins_err)) {
      $display["msg"] .= display_ok_msg($l_progress_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_progress_update_error);
    }
    dis_project_consult($param_project);
  }

} elseif ($action == "member_add")  {
///////////////////////////////////////////////////////////////////////////////
  $pid = $project["ext_id"];
  $project["id"] = $pid;
  $project["name"] = run_query_projectname($pid);
  if ($project["mem_nb"] > 0) {
    $nb = run_query_memberlist_insert($project);
    $display["msg"] .= display_ok_msg("$nb $l_member_added");
  } else {
    $display["msg"] .= display_err_msg("$l_no_member_add");
  }
  // gets updated infos
  $members_q = run_query_members($pid);
  $display["detail"] = html_project_member_form($members_q, $project);

} elseif ($action == "member_del")  {
///////////////////////////////////////////////////////////////////////////////
  $pid = $project["id"];
  $project["name"] = run_query_projectname($param_project);
  if ($project["mem_nb"] > 0) {
    $nb = run_query_memberlist_delete($project);
    $display["msg"] .= display_ok_msg("$nb $l_member_removed");
    if ($nb != $project["mem_nb"])
      $display["msg"] .= display_warn_msg("$l_member_delete_error");
  } else {
    $display["msg"] .= display_err_msg("$l_no_member_del");
  }
  // gets updated infos
  $members_q = run_query_members($pid);
  $display["detail"] = html_project_member_form($members_q, $project );

} elseif ($action == "member_update")  {
///////////////////////////////////////////////////////////////////////////////
  $pid = $project["id"];
  $project["name"] = run_query_projectname($pid);
  $retour = run_query_memberstatus_change($project);
  // gets updated infos
  $members_q = run_query_members($pid);
  $display["detail"] = html_project_member_form($members_q, $project);

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_search_q = run_query_display_pref($uid, "project", 1);
  $display["detail"] = dis_project_display_pref($pref_search_q);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);
  $pref_search_q = run_query_display_pref($uid, "project", 1);
  $display["detail"] = dis_project_display_pref($pref_search_q);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_search_q = run_query_display_pref($uid, "project", 1);
  $display["detail"] = dis_project_display_pref($pref_search_q);
}


///////////////////////////////////////////////////////////////////////////////
// Update right for projectmanagers and admins
///////////////////////////////////////////////////////////////////////////////

// To be reviewed !!!
//$consult_actions = Array('detailconsult',
//                         'update', 'progress_update', 'allocate_update');
//if (in_array($action, $consult_actions))
//     $action = (manager_rights($uid, $project, $project_q)) ? $action : "consultnoright";


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Action menus are retrieved here too as $project values can be set in process
// XXXXX to optimize here (update_project_action) with only needed action
get_project_action();
if (! $contact["popup"]) {
  $display["header"] = generate_menu($menu,$section);
}

$display["head"] = display_head($l_project);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $company hash
// returns : $company hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_project() {
  global $param_project, $param_user, $param_status, $param_company, $param_deal;
  global $tf_missing, $tf_projected, $tf_datebegin, $tf_dateend;
  global $tf_name, $tf_company_name, $tf_soldtime, $tf_estimated, $tf_tasklabel, $cb_archive;
  global $sel_tt, $sel_manager, $sel_member, $sel_task, $sel_ptask, $param_ext;
  global $deal_label;
  global $action, $ext_action, $ext_url, $ext_id, $ext_target, $title;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;
  global $cdg_param;

  if (isset ($param_project)) $project["id"] = $param_project;
  if (isset ($param_user)) $project["user_id"] = $param_user;
  if (isset ($param_status)) $project["user_status"] = $param_status;
  if (isset ($param_company)) $project["company"] = $param_company;
  if (isset ($param_deal)) $project["deal"] = $param_deal;

  // Project fields
  if (isset ($tf_soldtime)) $project["soldtime"] = $tf_soldtime;
  if (isset ($tf_estimated)) $project["estimated"] = $tf_estimated;
  if (isset ($sel_task)) $project["task"] = $sel_task;
  if (isset ($sel_ptask)) $project["ptask"] = $sel_ptask;
  if (isset ($tf_tasklabel)) $project["tasklabel"] = $tf_tasklabel;
  if (isset ($tf_missing)) $project["missing"] = $tf_missing;
  if (isset ($tf_projected)) $project["projected"] = $tf_projected;
  if (isset ($tf_datebegin)) $project["datebegin"] = $tf_datebegin;
  if (isset ($tf_dateend)) $project["dateend"] = $tf_dateend;

  // Helper fields (for performance)
  if (isset ($deal_label)) $project["deal_label"] = $deal_label;
 
  // Search fields
  if (isset ($tf_name)) $project["name"] = $tf_name;
  if (isset ($tf_company_name)) $project["company_name"] = $tf_company_name;
  if (isset ($sel_tt)) $project["tt"] = $sel_tt;
  if (isset ($sel_manager)) $project["manager"] = $sel_manager;
  if (isset ($sel_member)) $project["member"] = $sel_member;
  if (isset ($cb_archive)) $project["archive"] = $cb_archive;

  // External param
  if (isset ($ext_action)) $project["ext_action"] = $ext_action;
  if (isset ($ext_url)) $project["ext_url"] = $ext_url;
  if (isset ($ext_id)) $project["ext_id"] = $ext_id;
  if (isset ($ext_id)) $project["id"] = $ext_id;
  if (isset ($ext_target)) $project["ext_target"] = $ext_target;
  if (isset ($title)) $project["title"] = stripslashes($title);

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    $nb_mem = 0;
    $nb_tsk = 0;

    while ( list( $key ) = each( $http_obm_vars ) ) {

      // cb_u is likely to be called cb_user
      if (strcmp(substr($key, 0, 4),"cb_u") == 0) {
	$nb_mem++;
        $mem_num = substr($key, 4);

        $project["mem$nb_mem"] = $mem_num;
      } 

      if (strcmp(substr($key, 0, 7),"cb_task") == 0) {
	$nb_tsk++;
        $tsk_num = substr($key, 7);
        $project["tsk$nb_tsk"] = $tsk_num;
      } 
    }

    $project["mem_nb"] = $nb_mem;
    $project["tsk_nb"] = $nb_tsk;
  }

  if (debug_level_isset($cdg_param)) {
    if ( $project ) {
      while ( list( $key, $val ) = each( $project ) ) {
        echo "<br />project[$key]=$val";
      }
    }
  }

  return $project;
}


///////////////////////////////////////////////////////////////////////////////
// Project Action 
///////////////////////////////////////////////////////////////////////////////
function get_project_action() {
  global $project, $actions, $path, $l_project;
  global $l_header_find,$l_header_new,$l_header_update, $l_header_delete;
  global $l_header_display, $l_header_add_member, $l_add_member;
  global $l_header_man_task, $l_header_man_member, $l_header_man_advance;
  global $l_header_man_affect, $l_header_consult;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["PROJECT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/project/project_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["PROJECT"]["search"] = array (
    'Url'      => "$path/project/project_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["PROJECT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/project/project_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('index', 'search', 'insert', 'detailconsult', 'update', 'delete', 'admin', 'display') 
    );

// Detail Consult
  $actions["PROJECT"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/project/project_index.php?action=detailconsult&amp;param_project=".$project["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'update', 'task', 'task_add', 'task_update', 'task_del', 'member', 'member_add', 'member_del', 'member_update', 'allocate', 'progress', 'progress_update') 
    );

// Detail Update
  $actions["PROJECT"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/project/project_index.php?action=detailupdate&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update', 'insert', 'progress_update', 'allocate_update') 
    );

// Insert
  $actions["PROJECT"]["insert"] = array (
    'Url'      => "$path/project/project_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Update
  $actions["PROJECT"]["update"] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')  
                                    	 );

// Check Delete
  $actions["PROJECT"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/project/project_index.php?action=check_delete&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions["PROJECT"]["delete"] = array (
    'Url'      => "$path/project/project_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Task list
  $actions["PROJECT"]["task"] = array (
    'Name'     => $l_header_man_task,
    'Url'      => "$path/project/project_index.php?action=task&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'task', 'task_add', 'task_update', 'task_del', 'progress_update', 'allocate_update', 'member', 'member_add', 'member_del', 'member_update', 'allocate', 'progress') 
    );

// Add a task
  $actions["PROJECT"]["task_add"] = array (
    'Url'      => "$path/project/project_index.php?action=task_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Update a task
  $actions["PROJECT"]["task_update"] = array (
    'Url'      => "$path/project/project_index.php?action=task_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Remove a task
  $actions["PROJECT"]["task_del"] = array (
    'Url'      => "$path/project/project_index.php?action=task_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Member list
  $actions["PROJECT"]["member"] = array (
    'Name'     => $l_header_man_member,
    'Url'      => "$path/project/project_index.php?action=member&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'progress_update', 'allocate_update', 'task', 'task_add', 'task_update', 'task_del', 'allocate', 'progress') 
                                     );

// Select members : Lists selection
  $actions["PROJECT"]["sel_member"] = array (
    'Name'     => $l_header_add_member,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_member)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=".$project["id"]."&amp;ext_target=$l_project",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_project,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'member', 'member_add','member_del', 'member_update') 
                                    	  );

// Add a member
  $actions["PROJECT"]["member_add"] = array (
    'Url'      => "$path/project/project_index.php?action=member_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Remove a member
  $actions["PROJECT"]["member_del"] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Update member status
  $actions["PROJECT"]["member_update"] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Time Allocation
  $actions["PROJECT"]["allocate"] = array (
    'Name'     => $l_header_man_affect,
    'Url'      => "$path/project/project_index.php?action=allocate&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'progress_update', 'allocate_update', 'progress', 'member', 'member_add', 'member_del', 'member_update','task', 'task_add', 'task_update', 'task_del') 
                                     );

// Time allocation Update
  $actions["PROJECT"]["allocate_update"] = array (
    'Url'      => "$path/project/project_index.php?action=allocate_update&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                        );

// Progress detail update
  $actions["PROJECT"]["progress"] = array (
    'Name'     => $l_header_man_advance,
    'Url'      => "$path/project/project_index.php?action=progress&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update', 'progress_update', 'allocate', 'allocate_update', 'member', 'member_add', 'member_del', 'member_update','task', 'task_add', 'task_update', 'task_del') 
                                     	 );

// Update progress
  $actions["PROJECT"]["progress_update"] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Display
   $actions["PROJECT"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/project/project_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Préférences
  $actions["PROJECT"]["dispref_display"] = array (
    'Url'      => "$path/project/project_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["PROJECT"]["dispref_level"]  = array (
    'Url'      => "$path/project/project_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

}

</script>
