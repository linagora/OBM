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
// - task_fill       -- $param_project -- 
// - member_fill     -- $param_project -- 
// - membertime_fill -- $param_project -- 
// - validate        -- $param_project -- 
// - advanceupdate   -- $param_project -- show the project advance form
// - a_update        -- form fields    -- update the project advance
// - task_add        -- form fields    -- 
// - task_del        -- form fields    -- 
// - member_add      -- form fields    -- 
// - member_del      -- form fields    -- 
// - member_change   -- form fields    -- 
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
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("project_query.inc");
require("project_display.inc");
require("project_js.inc");

$uid = $auth->auth["uid"];
$perms = $auth->auth["perm"];

// updating the project bookmark : 
if ( ($param_project == $last_project) && (strcmp($action,"delete")==0) ) {
  $last_project = $last_project_default;
} else if ( ($param_project > 0 ) && ($last_project != $param_project) ) {
  $last_project = $param_project;
  run_query_set_user_pref($uid,"last_project",$param_project);
  $last_project_name = run_query_global_project_name($last_project);
}

page_close();
if ($action == "") $action = "index";
$project = get_param_project();
$perm->check();


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
    $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
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
  $retour = run_query_delete($hd_company_id);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $display["search"] = dis_project_search_form($project);
  $display["msg"] .= display_info_msg($l_no_display);

} elseif ($action == "task_fill")  {
///////////////////////////////////////////////////////////////////////////////
  $project["name"] = run_query_projectname($param_project);
  $tasks_q = run_query_tasks($param_project);
  $display["detail"]  = html_project_taskadd_form($tasks_q, $project);
  $display["detail"] .= html_project_tasklist($tasks_q, $project);

} elseif ($action == "member_fill")  {
///////////////////////////////////////////////////////////////////////////////
  $project["name"] = run_query_projectname($param_project);
  $tasks_q = run_query_tasks($param_project);
  $members_q = run_query_members($param_project);
  $display["detail"] .= html_project_member_form($members_q, $project );

} elseif ($action == "membertime_fill")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $membertime_q = run_query_membertime($param_project);

    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_tasks);

    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {
      $display["msg"] = display_warn_msg($l_no_members);

    } else {
      $display["detail"] = html_project_membertime_form($tasks_q, $members_q, $membertime_q, $project); 
    }
  }

} elseif ($action == "validate")  {
///////////////////////////////////////////////////////////////////////////////
//  if (check_member_form($param_project, $project)) {

  $ins_err = run_query_projectupdate($project);

  // Create an entry in the ProjectStat log
  $retour = run_query_statlog($param_project);
    
  if (!($retour))
    $ins_err = 1;
    
  if (!($ins_err)) {
    $display["msg"] .= display_ok_msg($l_adv_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_adv_update_error);
  }
  dis_project_consult($param_project);

} elseif ($action == "advanceupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $membertime_q = run_query_membertime($param_project);

    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_tasks);

    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_members);

    } else if (($membertime_q == 0) or ($membertime_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_membertime);

    } else {
   
      $display["detail"] = html_project_advance($members_q, $membertime_q, $tasks_q, $project);
    }
  }
  
} elseif ($action == "a_update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_advance_form($project)) {
    $ins_err = run_query_advanceupdate($project);
  
    // Create an entry in the ProjectStat log
    $retour = run_query_statlog($param_project);
  
    if (!($retour))
      $ins_err = 1;
    
    if (!($ins_err)) {
      $display["msg"] .= display_ok_msg($l_adv_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_adv_update_error);
    }
    dis_project_consult($param_project);
  }

} elseif ($action == "task_add")  {
  ///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

    if (check_taskadd_form($param_project, $project)) {
      $retour = run_query_tasklist_insert($project);
    
      if ($retour) 
	$display["msg"] .= display_ok_msg($l_insert_ok);
      else
	$display["msg"] .= display_err_msg("$l_insert_error");
      
      $project["name"] = run_query_projectname($param_project);
      $tasks_q = run_query_tasks($param_project);
      
      $display["detail"] = html_project_taskadd_form($tasks_q, $project);
      $display["detail"] .= html_project_tasklist($tasks_q, $project);
   
    } else { 
      $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
      dis_project_consult($param_project);
    }
  }

} elseif ($action == "task_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

    if ($project["tsk_nb"] > 0) {
      $nb = run_query_tasklist_delete($project);
      $display["msg"] .= display_ok_msg("$nb $l_task_removed");

      if ($nb != $project["tsk_nb"])
	$display["msg"] .= display_warn_msg("$l_taskdelete_error");
    } else {
      $display["msg"] .= display_err_msg("$l_no_task_del");
    }
    
    $project["name"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    
    $display["detail"] = html_project_taskadd_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
    
  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }
      
} elseif ($action == "member_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

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
  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }

} elseif ($action == "member_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

    $pid = $project["id"];
    $project["name"] = run_query_projectname($param_project);

    if ($project["mem_nb"] > 0) {
      $nb = run_query_memberlist_delete($project);
      $display["msg"] .= display_ok_msg("$nb $l_member_removed");

      if ($nb != $project["mem_nb"])
	$display["msg"] .= display_warn_msg("$l_memberdelete_error");
    } else {
      $display["msg"] .= display_err_msg("$l_no_member_del");
    }

    // gets updated infos
    $members_q = run_query_members($pid);
      
    $display["detail"] = html_project_member_form($members_q, $project );

  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }

} elseif ($action == "member_change")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

    $pid = $project["id"];
    $project["name"] = run_query_projectname($pid);

    $retour = run_query_memberstatus_change($project);

    // gets updated infos
    $members_q = run_query_members($pid);

    $display["detail"] = html_project_member_form($members_q, $project);
  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }

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
$consult_actions = Array('detailconsult', 'consultnoproj', 'consultnoadv',
                         'update', 'a_update', 'validate');

if (in_array($action, $consult_actions))
     $action = (manager_rights($uid, $project, $project_q)) ? $action : "consultnoright";


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Actions menus are retrieved here as $project values can be set in process
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
  global $tf_missing, $tf_projected, $hd_name;
  global $tf_name, $tf_company_name, $tf_soldtime, $tf_tasklabel, $cb_archive;
  global $sel_tt, $sel_manager, $sel_member, $sel_ptask, $param_ext, $cb_new;
  global $company_name, $deal_label;
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
  if (isset ($sel_ptask)) $project["ptask"] = $sel_ptask;
  if (isset ($tf_tasklabel)) $project["tasklabel"] = $tf_tasklabel;
  if (isset ($tf_missing)) $project["missing"] = $tf_missing;
  if (isset ($tf_projected)) $project["projected"] = $tf_projected;

  // Helper fields (for performance)
  if (isset ($deal_label)) $project["deal_label"] = $deal_label;
 
  // Search fields
  if (isset ($tf_name)) $project["name"] = $tf_name;
  if (isset ($hd_name)) $project["name"] = $hd_name;
  if (isset ($tf_company_name)) $project["company_name"] = $tf_company_name;
  if (isset ($sel_tt)) $project["tt"] = $sel_tt;
  if (isset ($sel_manager)) $project["manager"] = $sel_manager;
  if (isset ($sel_member)) $project["member"] = $sel_member;
  if (isset ($cb_new)) $project["newlist"] = $cb_new;
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
  global $project_read, $project_write, $project_admin_read, $project_admin_write;
  global $l_header_man_task, $l_header_man_member, $l_header_man_advance;
  global $l_header_man_affect, $l_header_consult;

// Index
  $actions["PROJECT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/project/project_index.php?action=index",
    'Right'    => $project_read,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["PROJECT"]["search"] = array (
    'Url'      => "$path/project/project_index.php?action=search",
    'Right'    => $project_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["PROJECT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/project/project_index.php?action=new",
    'Right'    => $project_write,
    'Condition'=> array ('index', 'search', 'admin', 'display') 
    );

// Detail Consult
  $actions["PROJECT"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/project/project_index.php?action=detailconsult&amp;param_project=".$project["id"]."",
    'Right'    => $project_read,
    'Condition'=> array ('detailupdate', 'update', 'task_fill', 'task_add', 'task_del', 'member_fill', 'member_add', 'member_del', 'member_change', 'membertime_fill', 'advanceupdate') 
    );

// Detail Update
  $actions["PROJECT"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/project/project_index.php?action=detailupdate&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoproj', 'consultnoadv', 'update', 'a_update', 'validate') 
    );

// Insert
  $actions["PROJECT"]["insert"] = array (
    'Url'      => "$path/project/project_index.php?action=insert",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                     	 );

// Update
  $actions["PROJECT"]["update"] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $project_write,
    'Condition'=> array ('None')  
                                    	 );

// Check Delete
  $actions["PROJECT"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/project/project_index.php?action=check_delete&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions["PROJECT"]["delete"] = array (
    'Url'      => "$path/project/project_index.php?action=delete",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                     	 );

// Task Fill
  $actions["PROJECT"]["task_fill"] = array (
    'Name'     => $l_header_man_task,
    'Url'      => "$path/project/project_index.php?action=task_fill&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoproj', 'consultnoadv', 'update', 'a_update', 'validate', 'member_fill', 'member_add', 'member_del', 'member_change', 'membertime_fill', 'advanceupdate') 
    );

// Member Fill
  $actions["PROJECT"]["member_fill"] = array (
    'Name'     => $l_header_man_member,
    'Url'      => "$path/project/project_index.php?action=member_fill&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoproj', 'consultnoadv', 'update', 'a_update', 'validate', 'task_fill', 'task_add', 'task_del', 'membertime_fill', 'advanceupdate') 
                                     );

// MemberTime Fill
  $actions["PROJECT"]["membertime_fill"] = array (
    'Name'     => $l_header_man_affect,
    'Url'      => "$path/project/project_index.php?action=membertime_fill&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoadv', 'update', 'a_update', 'validate', 'advanceupdate', 'member_fill', 'member_add', 'member_del', 'member_change','task_fill', 'task_add', 'task_del') 
                                     );

// Validate
  $actions["PROJECT"]["validate"] = array (
    'Url'      => "$path/project/project_index.php?action=validate&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                        );

// Advance Update
  $actions["PROJECT"]["advanceupdate"] = array (
    'Name'     => $l_header_man_advance,
    'Url'      => "$path/project/project_index.php?action=advanceupdate&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'update', 'a_update', 'validate', 'membertime_fill', 'member_fill', 'member_add', 'member_del', 'member_change','task_fill', 'task_add', 'task_del') 
                                     	 );

// Update
  $actions["PROJECT"]["a_update"] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                     	 );

// Select members : Lists selection
  $actions["PROJECT"]["sel_member"] = array (
    'Name'     => $l_header_add_member,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_member)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=".$project["id"]."&amp;ext_target=$l_project",
    'Right'    => $project_write,
    'Popup'    => 1,
    'Target'   => $l_project,
    'Condition'=> array ('member_fill', 'member_add','member_del', 'member_change') 
                                    	  );

// Add a task
  $actions["PROJECT"]["task_add"] = array (
    'Url'      => "$path/project/project_index.php?action=task_add",
    'Right'    => $project_write,
    'Condition'=> array ('None')
                                       );

// Remove a task
  $actions["PROJECT"]["task_del"] = array (
    'Url'      => "$path/project/project_index.php?action=task_del",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Add a member
  $actions["PROJECT"]["member_add"] = array (
    'Url'      => "$path/project/project_index.php?action=member_add",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Remove a member
  $actions["PROJECT"]["member_del"] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Change member status
  $actions["PROJECT"]["member_change"] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Display
   $actions["PROJECT"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/project/project_index.php?action=display",
     'Right'    => $project_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Préférences
  $actions["PROJECT"]["dispref_display"] = array (
    'Url'      => "$path/project/project_index.php?action=dispref_display",
    'Right'    => $project_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["PROJECT"]["dispref_level"]  = array (
    'Url'      => "$path/project/project_index.php?action=dispref_level",
    'Right'    => $project_read,
    'Condition'=> array ('None') 
                                     		 );

}

</script>
