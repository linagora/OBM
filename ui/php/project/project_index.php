<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : project_index.php                                            //
//     - Desc : Project Index File                                           //
// 2003-07-08 Bastien Continsouzas
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default) -- search fields  -- show the project search form
// - search          -- search fields  -- show the result set of search
// - new             -- $param_company -- show the new project form
// - init            -- form fields    -- show the init project form
// - task_fill       -- $param_project -- 
// - member_fill     -- $param_project -- 
// - detailconsult   -- $param_project -- show the project detail
// - detailupdate    -- $param_project -- show the project detail form
// - d_update        -- form fields    -- update the project
// - advanceupdate   -- $param_project -- show the project advance form
// - a_update        -- form fields    -- update the project advance
// - member_add      -- form fields    -- 
// - member_del      -- form fields    -- 
// // - check_delete    -- $param_project -- check links before delete
// // - delete          -- $param_project -- delete the project
// // - admin           --                -- admin index (kind)
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

// updating the project bookmark : 
if ( ($param_project == $last_project) && (strcmp($action,"delete")==0) ) {
  $last_project = $last_project_default;
} else if ( ($param_project > 0 ) && ($last_project != $param_project) ) {
  $last_project = $param_project;
  run_query_set_user_pref($auth->auth["uid"],"last_project",$param_project);
  $last_project_name = run_query_global_project_label($last_project);
}

page_close();
if($action == "") $action = "index";

$project = get_param_project();
// get_project_action();
$perm->check();

// if (! $contact["popup"]) {
//   $display["header"] = generate_menu($menu,$section);
// }

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
  $tt_q = run_query_projecttype('intern');
  $display["detail"] = html_project_form($action, "", $tt_q, $project);

} elseif ($action == "init")  {
///////////////////////////////////////////////////////////////////////////////
  $project_q = run_query_detail($param_project);

  $display["detail"] = html_project_init_form($action, $project_q, $project);

} elseif ($action == "task_fill")  {
/////////////////////////////////////////////////////////////////////////////// 
  $project["label"] = run_query_projectname($param_project);
  $tasks_q = run_query_tasks($param_project);
  
  $display["detail"]  = html_project_taskadd_form($tasks_q, $project);
  $display["detail"] .= html_project_tasklist($tasks_q, $project);

} elseif ($action == "member_fill")  {
///////////////////////////////////////////////////////////////////////////////
  $project["label"] = run_query_projectname($param_project);
  $tasks_q = run_query_tasks($param_project);
  $members_q = run_query_members($param_project);

  $display["detail"] .= html_project_member_form($members_q, $project );

} elseif ($action == "membertime_fill")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project["label"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $membertime_q = run_query_membertime($param_project);

    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_tasks);
      $display["detail"]  = html_project_taskadd_form(0, $project);
      $display["detail"] .= html_project_tasklist(0, $project);

    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_members);
      $display["detail"]  = html_project_memberadd_form($project);
      $display["detail"] .= html_project_member_form($members_q, $project );

      $action = "member_fill";

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
    
    // gets updated infos
    $project_q = run_query_detail($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $membertime_q = run_query_membertime($param_project);
    
    // Displays new infos
    if (($project_q->f("project_visibility")==0) ||
	($project_q->f("usercreate")==$uid) ) {
      $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
      
      $display["detail"] = html_project_consult($project_q, $tasks_q, $members_q, $membertime_q, $project);
    } else {
      // this project's page has "private" access
      $display["msg"] .= display_err_msg($l_error_visibility);
    }
 	
    //  } else {
    
    //}

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $origin = $project["origin"];

  if ($origin == "init") {
    
    if (check_init_form($param_project, $project)) {
      $retour = run_query_create($project);
      
      if ($retour) {
	$display["msg"] .= display_ok_msg($l_insert_ok);
	
	$project_q = run_query_detail($param_project);
	$tasks_q = run_query_tasks($param_project);
	$members_q = run_query_members($param_project);
	$membertime_q = run_query_membertime($param_project);
	
      $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
	$display["detail"] = html_project_consult($project_q, $tasks_q, $members_q, $membertime_q, $project);
      } else {
	$display["msg"] .= display_err_msg("$l_insert_error : $err_msg");
      }
      
    } else { 
      $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
      
      $project_q = run_query_detail($param_project);
      $display["detail"] = html_project_init_form($action, $project_q, $project);
    }

  } else if ($origin == "new") {

    if (check_new_form($param_project, $project)) {
      $param_project = run_query_insert($project);
      
      if ($param_project) {
	$project["id"] = $param_project;
	
	$project_q = run_query_detail($param_project);
	$tasks_q = run_query_tasks($param_project);

      $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
	$display["detail"] = html_project_consult($project_q, $tasks_q, $members_q, $membertime_q, $project);
      }
      else {
	$display["msg"] .= display_err_msg("$l_insert_error : $err_msg");
      }
      
    } else { 
      $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
      
      $tt_q = run_query_projecttype('intern');
      $display["detail"] = html_project_form($action, "", $tt_q, $project);
    }

  } else if ($param_project > 0) {
    $project_q = run_query_detail($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $membertime_q = run_query_membertime($param_project);

    if ($project_q->num_rows() != 1) {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $project_q->query . " !");
    }

    if (($project_q->f("project_visibility")==0) ||
        ($project_q->f("usercreate")==$uid) ) {
      $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
      
      $display["detail"] = html_project_consult($project_q, $tasks_q, $members_q, $membertime_q, $project);
    } else {
      // this project's page has "private" access
      $display["msg"] .= display_err_msg($l_error_visibility);
      $action = "";
    }

  } else {
    $display["msg"] .= display_err_msg($l_error_visibility);
    $action = "";
  }
  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project_q = run_query_detail($param_project);
    
    $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
    
    if ($project_q->f("project_status")==1) {
      $display["detail"] = html_project_init_form($action, $project_q, $project);
    }
    elseif ($project_q->f("project_status") == 2) {
      $tt_q = run_query_projecttype('intern');
      $display["detail"] = html_project_form($action, $project_q, $tt_q, $project);
    } 
    else {
      $display["msg"] .= display_err_msg($l_status_error);
    }
  }

} elseif ($action == "d_update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_init_form($param_project, $project)) {
    $retour = run_query_update($param_project, $project);
    
    if ($retour) {

      $project_q = run_query_detail($param_project);
      $tasks_q = run_query_tasks($param_project);
      $members_q = run_query_members($param_project);
      $membertime_q = run_query_membertime($param_project);
     
      if (($project_q->f("project_visibility")==0) ||
	  ($project_q->f("usercreate")==$uid) ) {
	$display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
	
	$display["detail"] = html_project_consult($project_q, $tasks_q, $members_q, $membertime_q, $project);
      } else {
	// this project's page has "private" access
	$display["msg"] .= display_err_msg($l_error_visibility);
      } 	
    
    } else { 
      $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
      
      $project_q = run_query_detail($param_project);
      $display["detail"] = html_project_init_form($action, $project_q, $project);
    }
  }
  
} elseif ($action == "close")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_close($param_project);

  if ($retour) {
    $display["msg"] .= display_ok_msg($l_close_ok);
  } else {
    $display["msg"] .= display_err_msg($l_close_error);
  }

} elseif ($action == "advanceupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project["label"] = run_query_projectname($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $membertime_q = run_query_membertime($param_project);

    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_tasks);
      $display["detail"]  = html_project_taskadd_form(0, $project);
      $display["detail"] .= html_project_tasklist(0, $project);

    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_members);
      $display["detail"]  = html_project_memberadd_form($project);
      $display["detail"] .= html_project_member_form($members_q, $project );

      $action = "member_fill";

    } else if (($membertime_q == 0) or ($membertime_q->num_rows() == 0)) {

      $display["msg"] = display_warn_msg($l_no_membertime);
      $display["detail"] = html_project_membertime_form($tasks_q, $members_q, $membertime_q, $project); 

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
    
    // gets updated infos
    $project_q = run_query_detail($param_project);
    $tasks_q = run_query_tasks($param_project);
    $members_q = run_query_members($param_project);
    $membertime_q = run_query_membertime($param_project);

    // Displays new infos
    if (($project_q->f("project_visibility")==0) ||
	($project_q->f("usercreate")==$uid) ) {
      $display["detailInfo"] = display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
    
      $display["detail"] = html_project_consult($project_q, $tasks_q, $members_q, $membertime_q, $project);
    } else {
      // this project's page has "private" access
      $display["msg"] .= display_err_msg($l_error_visibility);
    } 	
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////

} elseif ($action == "task_add")  {
  ///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

    if (check_taskadd_form($param_project, $project)) {
      $retour = run_query_tasklist_insert($project);
    
      if ($retour) 
	$display["msg"] .= display_ok_msg($l_insert_ok);
      else
	$display["msg"] .= display_err_msg("$l_insert_error");
      
      $project["label"] = run_query_projectname($param_project);
      $tasks_q = run_query_tasks($param_project);
      
      $display["detail"] = html_project_taskadd_form($tasks_q, $project);
      $display["detail"] .= html_project_tasklist($tasks_q, $project);
   
    } else { 
      $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
      
      $project["label"] = run_query_projectname($param_project);
      $project_q = run_query_detail($param_project);
      $display["detail"] = html_project_init_form($action, $project_q, $project);
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
    
    $tasks_q = run_query_tasks($param_project);
    
    $display["detail"] = html_project_taskadd_form($tasks_q, $project);
    $display["detail"] .= html_project_tasklist($tasks_q, $project);
    
  } else {
    $display["msg"] .= display_err_msg($l_error_authentification);
  }
      
} elseif ($action == "member_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

    $pid = $project["ext_id"];
    $project["id"] = $pid;
    $project["label"] = run_query_projectname($pid);

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
    $display["msg"] .= display_err_msg($l_error_authentification);
  }

} elseif ($action == "member_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {

    $pid = $project["id"];
    $project["label"] = run_query_projectname($param_project);

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
    $display["msg"] .= display_err_msg($l_error_authentification);
  }

}  elseif ($action == "display") {
/////////////////////////////////////////////////////////////////////////
  $pref_new_q = run_query_display_pref($auth->auth["uid"], "project_new", 1);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "project", 1);

  $display["detail"] = dis_project_display_pref($pref_new_q, $pref_search_q);

} else if ($action == "dispref_display") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);

  $pref_new_q = run_query_display_pref($auth->auth["uid"], "project_new", 1);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "project", 1);

  $display["detail"] = dis_project_display_pref($pref_new_q, $pref_search_q);

} else if ($action == "dispref_level") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);

  $pref_new_q = run_query_display_pref($auth->auth["uid"], "project_new", 1);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "project", 1);

  $display["detail"] = dis_project_display_pref($pref_new_q, $pref_search_q);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
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

  global $param_project, $param_origin, $tf_missing, $tf_projected, $hd_status;
  global $tf_name, $tf_company_name, $tf_soldtime, $tf_tasklabel, $cb_archive;
  global $sel_tt, $sel_manager, $sel_member, $sel_ptask, $param_ext, $cb_new;
  global $action, $ext_action, $ext_url, $ext_id, $ext_target, $title;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;
  global $cdg_param;

  if (isset ($param_project)) $project["id"] = $param_project;
  if (isset ($param_origin)) $project["origin"] = $param_origin;

  // Project fields
  if (isset ($tf_soldtime)) $project["soldtime"] = $tf_soldtime;
  if (isset ($sel_ptask)) $project["ptask"] = $sel_ptask;
  if (isset ($tf_tasklabel)) $project["tasklabel"] = $tf_tasklabel;
  if (isset ($tf_missing)) $project["missing"] = $tf_missing;
  if (isset ($tf_projected)) $project["projected"] = $tf_projected;
  if (isset ($hd_status)) $project["status"] = $hd_status;

  // Search fields
  if (isset ($tf_name)) $project["name"] = $tf_name;
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
//  Company Action 
///////////////////////////////////////////////////////////////////////////////
function get_project_action() {
  global $project, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete, $l_list;
  global $l_header_close,$l_header_display,$l_header_admin, $l_header_add_member;
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

// Init
  $actions["PROJECT"]["init"] = array (
    'Url'      => "$path/project/project_index.php?action=init",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                     );

// Detail Update
  $actions["PROJECT"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/project/project_index.php?action=detailupdate&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoproj', 'consultnoadv', 'd_update', 'a_update', 'validate') 
                                     	      );

// Task Fill
  $actions["PROJECT"]["task_fill"] = array (
    'Name'     => $l_header_man_task,
    'Url'      => "$path/project/project_index.php?action=task_fill&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoproj', 'consultnoadv', 'd_update', 'a_update', 'validate') 
                                     );

// Member Fill
  $actions["PROJECT"]["member_fill"] = array (
    'Name'     => $l_header_man_member,
    'Url'      => "$path/project/project_index.php?action=member_fill&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoproj', 'consultnoadv', 'd_update', 'a_update', 'validate') 
                                     );

// MemberTime Fill
  $actions["PROJECT"]["membertime_fill"] = array (
    'Name'     => $l_header_man_affect,
    'Url'      => "$path/project/project_index.php?action=membertime_fill&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'consultnoadv', 'd_update', 'a_update', 'validate') 
                                     );

// Validate
  $actions["PROJECT"]["validate"] = array (
    'Url'      => "$path/project/project_index.php?action=validate&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                        );

// Detail Consult
  $actions["PROJECT"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/project/project_index.php?action=detailconsult&amp;param_project=".$project["id"]."",
    'Right'    => $project_read,
    'Condition'=> array ('detailupdate', 'd_update', 'task_fill', 'task_add', 'task_del', 'member_fill', 'member_add',
                         'member_del', 'membertime_fill', 'advanceupdate') 
                                     		 );

// Update
  $actions["PROJECT"]["d_update"] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
 
                                    	 );
// Close
  $actions["PROJECT"]["close"] = array (
    'Name'     => $l_header_close,
    'Url'      => "$path/project/project_index.php?action=close&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailupdate') 
                                     	 );

// Advance Update
  $actions["PROJECT"]["advanceupdate"] = array (
    'Name'     => $l_header_man_advance,
    'Url'      => "$path/project/project_index.php?action=advanceupdate&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'd_update', 'a_update', 'validate') 
                                     	 );

// Update
  $actions["PROJECT"]["a_update"] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                     	 );

// Ext get Ids : Lists selection
  $actions["PROJECT"]["ext_get_ids"] = array (
    'Name'     => $l_header_add_member,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;title=".urlencode($l_add_contact)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=".$project["id"]."&amp;ext_target=$l_list",
    'Right'    => $user_write,
    'Popup'    => 1,
    'Target'   => $l_list,
    'Condition'=> array ('member_fill', 'member_add','member_del') 
                                    	  );

// Display
  $actions["PROJECT"]["task_add"] = array (
    'Url'      => "$path/project/project_index.php?action=task_add",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Display
  $actions["PROJECT"]["task_del"] = array (
    'Url'      => "$path/project/project_index.php?action=task_del",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Display
  $actions["PROJECT"]["member_add"] = array (
    'Url'      => "$path/project/project_index.php?action=member_add",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Display
  $actions["PROJECT"]["member_del"] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                       );

// Admin
//   $actions["PROJECT"]["admin"] = array (
//     'Name'     => $l_header_admin,
//     'Url'      => "$path/project/project_index.php?action=admin",
//     'Right'    => $project_admin_read,
//     'Condition'=> array ('all') 
//                                        );

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

</SCRIPT>
