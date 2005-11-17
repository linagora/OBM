<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : project_index.php                                            //
//     - Desc : Project Index File                                           //
// 2003-07-08 Aliacom                                                        //
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
// - advance         -- $param_project -- show the project allocation/progress
// - advance_update  -- form fields    -- update project allocation/progress
// - dashboard       -- $param_project -- dashboard / stats of the project
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_id      -- $ext_params    -- select a deal (return id) 
///////////////////////////////////////////////////////////////////////////////


$path = "..";
$module = "project";
$extra_css = "project.css";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
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
$perm->check_permissions($module, $action);


///////////////////////////////////////////////////////////////////////////////
// External calls
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  $display["search"] = dis_project_search_form($project);
  if ($set_display == "yes") {
    $display["result"] = dis_project_search_list($project);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

///////////////////////////////////////////////////////////////////////////////
// Standard calls
///////////////////////////////////////////////////////////////////////////////
} else if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_project_search_form($project);
  if ($set_display == "yes") {
    $display["result"] = dis_project_search_list($project);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_project_search_form($project);
  $display["result"] = dis_project_search_list($project);

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_project_form($action, "", $project);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  if ($project["id"] > 0) {
    $display["detail"] = dis_project_consult($project["id"]);
  } else {
    $display["msg"] .= display_err_msg($l_query_error);
  }
  
} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if ($project["id"] > 0) {
    $project_q = run_query_detail($project["id"]);
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
      $display["detail"] = dis_project_consult($project["id"]);
    } else {
      $display["msg"] .= display_err_msg("$l_insert_error : $err_msg");
    }
  } else { 
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_project_form($action, "", $project);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_project_form($project["id"], $project)) {
    $retour = run_query_update($project["id"], $project);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
      $display["detail"] = dis_project_consult($project["id"]);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
      $display["detail"] = dis_project_consult($project["id"]);
    }
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_project_form($action, "", $project);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_project($project["id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_project($project["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_project_consult($project["id"]);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_project($project["id"])) {
    $retour = run_query_delete($project["id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
      $display["search"] = dis_project_search_form($project);
      if ($set_display == "yes") {
        $display["result"] = dis_project_search_list($project);
      } else {
        $display["msg"] .= display_info_msg($l_no_display);
      }
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
      $display["detail"] = dis_project_consult($project["id"]);
    }
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_project_consult($project["id"]);
  }

} elseif ($action == "task") {
///////////////////////////////////////////////////////////////////////////////
  $project["name"] = run_query_projectname($project["id"]);
  $tasks_q = run_query_tasks($project["id"]);
  $display["detail"]  = html_project_task_form($tasks_q, $project);
  $display["detail"] .= html_project_tasklist($tasks_q, $project);

} elseif ($action == "task_add") {
///////////////////////////////////////////////////////////////////////////////
  if (check_task_form($project["id"], $project)) {
    $retour = run_query_task_insert($project);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_task_insert_ok);
    } else {
      $display["msg"] .= display_err_msg("$l_task_insert_error");
    }
    $project["name"] = run_query_projectname($project["id"]);
    $tasks_q = run_query_tasks($project["id"]);
    $display["detail"] = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  } else { 
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $project["name"] = run_query_projectname($project["id"]);
    $tasks_q = run_query_tasks($project["id"]);
    $display["detail"]  = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  }

} elseif ($action == "task_update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_task_form($project["id"], $project)) {
    $retour = run_query_task_update($project);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_task_update_ok);
    } else {
      $display["msg"] .= display_err_msg("$l_task_update_error");
    }
    $project["name"] = run_query_projectname($project["id"]);
    $tasks_q = run_query_tasks($project["id"]);
    $display["detail"] = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  } else { 
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $project["name"] = run_query_projectname($project["id"]);
    $tasks_q = run_query_tasks($project["id"]);
    $display["detail"]  = html_project_task_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
  }

} elseif ($action == "task_del") {
///////////////////////////////////////////////////////////////////////////////
  if ($project["tsk_nb"] > 0) {
    $nb = run_query_task_delete($project);
    if ($nb == $project["tsk_nb"]) {
      $display["msg"] .= display_ok_msg("$l_task_delete_ok");
    } else {
      $display["msg"] .= display_warn_msg("$l_task_delete_error");
    }
  } else {
    $display["msg"] .= display_err_msg("$l_no_task_del");
  }
  $project["name"] = run_query_projectname($project["id"]);
  $tasks_q = run_query_tasks($project["id"]);
  $display["detail"] = html_project_task_form($tasks_q, $project);
  $display["detail"] .= html_project_tasklist($tasks_q, $project);
      
} elseif ($action == "member") {
///////////////////////////////////////////////////////////////////////////////
  $project["name"] = run_query_projectname($project["id"]);
  $tasks_q = run_query_tasks($project["id"]);
  $members_q = run_query_members($project["id"]);
  $display["detail"] .= html_project_member_form($members_q, $project );

} elseif ($action == "allocate") {
///////////////////////////////////////////////////////////////////////////////
  if ($project["id"] > 0) {
    $project["name"] = run_query_projectname($project["id"]);
    $tasks_q = run_query_tasks($project["id"]);
    $members_q = run_query_members($project["id"]);
    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else {
      $allo_q = run_query_allocation($project["id"]);
      $display["detail"] = html_project_allocate_form($tasks_q, $members_q, $allo_q, $project); 
    }
  }

} elseif ($action == "advance") {
///////////////////////////////////////////////////////////////////////////////
  if ($project["id"] > 0) {
    $project["name"] = run_query_projectname($project["id"]);
    $tasks_q = run_query_tasks($project["id"]);
    $members_q = run_query_members($project["id"]);
    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_allocation);
    } else {
      $allo_q = run_query_allocation($project["id"]);
      $display["detail"] = html_project_advance_form($tasks_q, $members_q, $allo_q, $project); 
    }
  }

} elseif ($action == "allocate_update") {
///////////////////////////////////////////////////////////////////////////////
//  if (check_member_form($project["id"], $project)) {
  $ins_err = run_query_allocate_update($project);
  // Create an entry in the ProjectStat log
  $retour = run_query_statlog($project["id"]);
  if (!($retour))
    $ins_err = 1;
  if (!($ins_err)) {
    $display["msg"] .= display_ok_msg($l_allocate_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_allocate_update_error);
  }
  $display["detail"] = dis_project_consult($project["id"]);

} elseif ($action == "advance_update") {
///////////////////////////////////////////////////////////////////////////////
//  if (check_member_form($project["id"], $project)) {
  $ins_err = run_query_advance_update($project);
  // Create an entry in the ProjectStat log
  $retour = run_query_statlog($project["id"]);
  if (!($retour))
    $ins_err = 1;
  if (!($ins_err)) {
    $display["msg"] .= display_ok_msg($l_allocate_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_allocate_update_error);
  }
  $display["detail"] = dis_project_consult($project["id"]);

} elseif ($action == "dashboard") {
///////////////////////////////////////////////////////////////////////////////
  if ($project["id"] > 0) {
    $display["detail"] = dis_project_dashboard($project);
  }
  
} elseif ($action == "member_add") {
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

} elseif ($action == "member_del") {
///////////////////////////////////////////////////////////////////////////////
  $pid = $project["id"];
  $project["name"] = run_query_projectname($project["id"]);
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
  $display["detail"] = html_project_member_form($members_q, $project);

} elseif ($action == "member_update") {
///////////////////////////////////////////////////////////////////////////////
  $pid = $project["id"];
  $project["name"] = run_query_projectname($pid);
  $retour = run_query_memberstatus_change($project);
  // gets updated infos
  $members_q = run_query_members($pid);
  $display["detail"] = html_project_member_form($members_q, $project);

} elseif ($action == "document_add") {
///////////////////////////////////////////////////////////////////////////////
  if ($project["doc_nb"] > 0) {
    $nb = run_query_insert_documents($project, "project");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  $display["detail"] = dis_project_consult($project["id"]);

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "project", 1);
  $display["detail"] = dis_project_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "project", 1);
  $display["detail"] = dis_project_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "project", 1);
  $display["detail"] = dis_project_display_pref($prefs);
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
$display["head"] = display_head($l_project);
$display["end"] = display_end();
if (! $project["popup"]) {
  // Update actions url in case some values have been updated (id after insert)
  update_project_action();
  $display["header"] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $company hash
// returns : $company hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_project() {
  global $param_project, $param_user, $param_status,$param_company,$param_deal;
  global $tf_missing, $tf_projected, $tf_datebegin, $tf_dateend;
  global $tf_name, $tf_company_name, $tf_soldtime, $tf_estimated, $tf_tasklabel, $cb_archive;
  global $sel_tt, $sel_manager, $sel_member, $sel_task, $sel_ptask,$deal_label;
  global $ta_com, $tf_datecomment, $sel_usercomment, $ta_add_comment;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;  
  global $ext_widget, $ext_widget_text, $new_order, $order_dir;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;

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
  if (isset ($ta_com)) $project["comment"] = $ta_com;
  if (isset ($tf_datecomment)) $project["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $project["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $project["add_comment"] = trim($ta_add_comment);

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
  if (isset ($popup)) $project["popup"] = $popup;
  if (isset ($ext_action)) $project["ext_action"] = $ext_action;
  if (isset ($ext_url)) $project["ext_url"] = $ext_url;
  if (isset ($ext_id)) $project["ext_id"] = $ext_id;
  if (isset ($ext_id)) $project["id"] = $ext_id;
  if (isset ($ext_target)) $project["ext_target"] = $ext_target;
  if (isset ($ext_title)) $project["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_widget)) $project["ext_widget"] = $ext_widget;
  if (isset ($ext_widget_text)) $project["ext_widget_text"] = $ext_widget_text;

  if (isset ($new_order)) $project["new_order"] = $new_order;
  if (isset ($order_dir)) $project["order_dir"] = $order_dir;

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
      if (strcmp(substr($key, 0, 7),"cb_task") == 0) {
	$nb_tsk++;
        $tsk_num = substr($key, 7);
        $project["tsk$nb_tsk"] = $tsk_num;
      }

      else if (strcmp(substr($key, 0, 4),"cb_u") == 0) {
	$nb_mem++;
        $mem_num = substr($key, 4);
        $project["mem$nb_mem"] = $mem_num;
      } 
    }

    $project["mem_nb"] = $nb_mem;
    $project["tsk_nb"] = $nb_tsk;
  }

  get_global_param_document($project);

  display_debug_param($project);

  return $project;
}


///////////////////////////////////////////////////////////////////////////////
// Project Action 
///////////////////////////////////////////////////////////////////////////////
function get_project_action() {
  global $project, $actions, $path, $l_project;
  global $l_header_find,$l_header_new,$l_header_update, $l_header_delete;
  global $l_header_display, $l_header_add_member, $l_add_member;
  global $l_header_man_task, $l_header_man_member;
  global $l_header_advance, $l_header_man_affect, $l_header_consult;
  global $l_header_dashboard;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// External call : select one deal
  $actions["project"]["ext_get_id"] = array (
    'Url'      => "$path/project/project_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     );

// Index
  $actions["project"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/project/project_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    	 );

// Search
  $actions["project"]["search"] = array (
    'Url'      => "$path/project/project_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                    	 );

// New
  $actions["project"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/project/project_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('index', 'search', 'insert', 'detailconsult', 'update', 'delete', 'admin', 'display')
    );

// Detail Consult
  $actions["project"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/project/project_index.php?action=detailconsult&amp;param_project=".$project["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'update', 'task', 'task_add', 'task_update', 'task_del', 'member', 'member_add', 'member_del', 'member_update', 'allocate', 'progress', 'progress_update', 'advance', 'advance_update', 'dashboard')
    );

// Detail Update
  $actions["project"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/project/project_index.php?action=detailupdate&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update', 'insert', 'progress_update', 'allocate_update', 'advance_update', 'dashboard')
    );

// Insert
  $actions["project"]["insert"] = array (
    'Url'      => "$path/project/project_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Update
  $actions["project"]["update"] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                    	 );

// Check Delete
  $actions["project"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/project/project_index.php?action=check_delete&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'insert')
                                     	      );

// Delete
  $actions["project"]["delete"] = array (
    'Url'      => "$path/project/project_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Task list
  $actions["project"]["task"] = array (
    'Name'     => $l_header_man_task,
    'Url'      => "$path/project/project_index.php?action=task&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'task', 'task_add', 'task_update', 'task_del', 'advance', 'advance_update', 'allocate_update', 'member', 'member_add', 'member_del', 'member_update', 'allocate', 'dashboard')
    );

// Add a task
  $actions["project"]["task_add"] = array (
    'Url'      => "$path/project/project_index.php?action=task_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Update a task
  $actions["project"]["task_update"] = array (
    'Url'      => "$path/project/project_index.php?action=task_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Remove a task
  $actions["project"]["task_del"] = array (
    'Url'      => "$path/project/project_index.php?action=task_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Member list
  $actions["project"]["member"] = array (
    'Name'     => $l_header_man_member,
    'Url'      => "$path/project/project_index.php?action=member&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'task', 'task_add', 'task_update', 'task_del', 'allocate', 'allocate_update', 'advance', 'advance_update', 'dashboard')
                                     );

// Select members : Lists selection
  $actions["project"]["sel_member"] = array (
    'Name'     => $l_header_add_member,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_member)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=".$project["id"]."&amp;ext_target=$l_project",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_project,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'member', 'member_add','member_del', 'member_update')
                                    	  );

// Add a member
  $actions["project"]["member_add"] = array (
    'Url'      => "$path/project/project_index.php?action=member_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Remove a member
  $actions["project"]["member_del"] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Update member status
  $actions["project"]["member_update"] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Time Allocation
  $actions["project"]["allocate"] = array (
    'Name'     => $l_header_man_affect,
    'Url'      => "$path/project/project_index.php?action=allocate&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'progress_update', 'allocate_update', 'progress', 'advance_update', 'advance', 'member', 'member_add', 'member_del', 'member_update','task', 'task_add', 'task_update', 'task_del')
                                     );

// Time allocation Update
  $actions["project"]["allocate_update"] = array (
    'Url'      => "$path/project/project_index.php?action=allocate_update&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                        );

// Advance
  $actions["project"]["advance"] = array (
    'Name'     => $l_header_advance,
    'Url'      => "$path/project/project_index.php?action=advance&amp;param_project=".$project["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'progress_update', 'allocate', 'allocate_update', 'advance', 'advance_update', 'member', 'member_add', 'member_del', 'member_update','task', 'task_add', 'task_update', 'task_del')
                                     );

// Advance Update
  $actions["project"]["advance_update"] = array (
    'Url'      => "$path/project/project_index.php?action=advance_update&amp;param_project=".$project["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                        );

// Dashboard
   $actions["project"]["dashboard"] = array (
     'Name'     => $l_header_dashboard,
     'Url'      => "$path/project/project_index.php?action=dashboard&amp;param_project=".$project["id"],
     'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'insert', 'update', 'progress_update', 'allocate', 'allocate_update', 'advance', 'advance_update', 'member', 'member_add', 'member_del', 'member_update','task', 'task_add', 'task_update', 'task_del')
                                       	 );

// Display
   $actions["project"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/project/project_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all')
                                       	 );

// Display Préférences
  $actions["project"]["dispref_display"] = array (
    'Url'      => "$path/project/project_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

// Display Level
  $actions["project"]["dispref_level"]  = array (
    'Url'      => "$path/project/project_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

// Document add
  $actions["project"]["document_add"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );

}


///////////////////////////////////////////////////////////////////////////////
// Project Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_project_action() {
  global $project, $actions, $path, $l_add_member, $l_project;

  // Detail Consult
  $actions["project"]["detailconsult"]["Url"] = "$path/project/project_index.php?action=detailconsult&amp;param_project=".$project["id"];

  // Detail Update
  $actions["project"]["detailupdate"]['Url'] = "$path/project/project_index.php?action=detailupdate&amp;param_project=".$project["id"];

  // Check Delete
  $actions["project"]["check_delete"]['Url'] = "$path/project/project_index.php?action=check_delete&amp;param_project=".$project["id"];

  // Tasks
  $actions["project"]["task"]['Url'] = "$path/project/project_index.php?action=task&amp;param_project=".$project["id"];

  // Members
  $actions["project"]["member"]['Url'] = "$path/project/project_index.php?action=member&amp;param_project=".$project["id"];

  // Select Member
  $actions["project"]["sel_member"]['Url'] = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_member)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=".$project["id"]."&amp;ext_target=$l_project";

  // Advance
  $actions["project"]["advance"]['Url'] = "$path/project/project_index.php?action=advance&amp;param_project=".$project["id"];

  // Dashboard
  $actions["project"]["dashboard"]['Url'] = "$path/project/project_index.php?action=dashboard&amp;param_project=".$project["id"];

}

</script>
