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
// - insert          -- form fields    -- insert the project
// - init            -- form fields    -- show the init project form
// - create          -- form fields    -- create the project
// - detailconsult   -- $param_project -- show the project detail
// - detailupdate    -- $param_project -- show the project detail form
// - d_update        -- form fields    -- update the project
// - close           -- $param_project -- close the project
// - advanceupdate   -- $param_project -- show the project advance form
// - a_update        -- form fields    -- update the project advance
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
// if ( ($param_project == $last_project) && (strcmp($action,"delete")==0) ) {
//   $last_project = $last_project_default;
// } else if ( ($param_project > 0 ) && ($last_project != $param_project) ) {
//   $last_project = $param_project;
//   run_query_set_user_pref($auth->auth["uid"],"last_project",$param_project);
//   $last_project_name = run_query_global_project_name($last_project);
// }

page_close();
if($action == "") $action = "index";

$project = get_param_project();
get_project_action();
$perm->check();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_project);     // Head & Body


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
generate_menu($menu, $section); // Menu

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $tt_q = run_query_projecttype();
  $manager_q = run_query_manager();
  $member_q = run_query_member();
  
  html_project_search_form($tt_q, $manager_q, $member_q, $project);

  dis_project_new_list($project);

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  $tt_q = run_query_projecttype();
  $manager_q = run_query_manager();
  $member_q = run_query_member();
  
  html_project_search_form($tt_q, $manager_q, $member_q, $project);

  dis_project_search_list($project);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  $tt_q = run_query_projecttype('intern');

  html_project_form($action, "", $tt_q, $project);

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  $pid = run_query_insert($project);

  if ($pid) {
    display_ok_msg($l_insert_ok);

    $project["id"] = $pid;
    $project_q = run_query_detail($pid);

    html_project_infos($project_q, $project);
    html_project_memberadd_form($project);
    html_project_memberlist();
  }
  else {
    display_err_msg("$l_insert_error : $err_msg");
  }

} elseif ($action == "init")  {
///////////////////////////////////////////////////////////////////////////////
  $project_q = run_query_detail($param_project);

  html_project_init_form($action, $project_q, $project);

} elseif ($action == "create")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_create($project);

  if ($retour) {
    display_ok_msg($l_insert_ok);

    $project_q = run_query_detail($param_project);

    html_project_infos($project_q, $project);
    html_project_memberadd_form($project);
    html_project_memberlist();
  } else {
    display_err_msg("$l_insert_error : $err_msg");
  }

// } elseif ($action == "affect")  {
///////////////////////////////////////////////////////////////////////////////

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project_q = run_query_detail($param_project);
    $members_q = run_query_memberstime($param_project);

    if (($project_q->f("project_visibility")==0) ||
        ($project_q->f("usercreate")==$uid) ) {
      display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));

      html_project_consult($project_q, $members_q, $project);
    } else {
      // this project's page has "private" access
      display_err_msg($l_error_visibility);
    } 	
  }

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $project_q = run_query_detail($param_project);
  
  if ($project_q->f("project_state")==1) {
    html_project_init_form($action, $project_q, $project);
  }
  elseif ($project_q->f("project_state") == 2) {
    $tt_q = run_query_projecttype('intern');
    html_project_form($action, $project_q, $tt_q, $project);
  } 
  else
    display_err_msg($l_state_error);

} elseif ($action == "d_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_update($param_project, $project);

  if ($retour) {
    display_ok_msg($l_update_ok);
  } else {
    display_err_msg($l_update_error);
  }

  $project_q = run_query_detail($param_project);
  $members_q = run_query_memberstime($param_project);
  
  if (($project_q->f("project_visibility")==0) ||
      ($project_q->f("usercreate")==$uid) ) {
    display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
    
    html_project_consult($project_q, $members_q, $project);
  } else {
    // this project's page has "private" access
    display_err_msg($l_error_visibility);
  } 	
  

} elseif ($action == "close")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_close($param_project);

  if ($retour) {
    display_ok_msg($l_close_ok);
  } else {
    display_err_msg($l_close_error);
  }

} elseif ($action == "advanceupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_project > 0) {
    $project_q = run_query_detail($param_project);
    $members_q = run_query_memberstime($param_project);
 
    if (($project_q->f("project_visibility")==0) ||
        ($project_q->f("usercreate")==$uid) ) {
      display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));

      html_project_advance($members_q, $project);
    } else {
      // this project's page has "private" access
      display_err_msg($l_error_visibility);
    } 	
  }

} elseif ($action == "a_update")  {
///////////////////////////////////////////////////////////////////////////////
//   if (check_data_form($param_company, $company)) {

  // Update project estimated missing times
  while ( list($m_id, $misstime) = each($project["missing"]) ) {
    if ($misstime != "") {
      $retour = run_query_advanceupdate($param_project, $m_id, $misstime);
      
      if (!($retour))
	$ins_err = 1;
    }
  }
  
  // Create an entry in the ProjectStat log
  $retour = run_query_statlog($param_project);
  
  if (!($retour))
    $ins_err = 1;
  
  if (!($ins_err)) {
    display_ok_msg($l_adv_update_ok);
  } else {
    display_err_msg($l_adv_update_error);
  }

  // gets updated infos
  $project_q = run_query_detail($param_project);
  $members_q = run_query_memberstime($param_project);

  // Displays new infos
  if (($project_q->f("project_visibility")==0) ||
      ($project_q->f("usercreate")==$uid) ) {
    display_record_info($project_q->f("usercreate"),$project_q->f("userupdate"),$project_q->f("timecreate"),$project_q->f("timeupdate"));
    
    html_project_consult($project_q, $members_q, $project);
  } else {
    // this project's page has "private" access
    display_err_msg($l_error_visibility);
  } 	
  
} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////

} elseif ($action == "member_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    $pid = $project["ext_id"];
    $project["id"] = $pid;

    if ($project["mem_nb"] > 0) {
      $nb = run_query_memberlist_insert($project);
      display_ok_msg("$nb $l_member_added");
    } else {
      display_err_msg("no contact to add");
    }

    $project_q = run_query_detail($pid);
    $members_q = run_query_memberstime($pid);

    html_project_infos($project_q, $project);
    html_project_memberlist($members_q);

//     html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    display_err_msg($l_error_authentification);
  }

} elseif ($action == "member_del")  {
///////////////////////////////////////////////////////////////////////////////
  if ($perm->have_perm("editor")) {
    $pid = $project["ext_id"];
    $project["id"] = $pid;

    if ($project["mem_nb"] > 0) {
      $nb = run_query_memberlist_delete($project);
      display_ok_msg("$nb $l_contact_removed");
    } else {
      display_err_msg("no contact to delete");
    }

    $project_q = run_query_detail($pid);
    $members_q = run_query_memberstime($pid);

    html_project_infos($project_q, $project);
    html_project_memberlist($members_q);

//     html_list_consult($list_q, $pref_con_q, $con_q);
  } else {
    display_err_msg($l_error_authentification);
  }

}  elseif ($action == "display") {
/////////////////////////////////////////////////////////////////////////
  $pref_new_q = run_query_display_pref($auth->auth["uid"], "project_new", 1);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "project", 1);

  dis_project_display_pref($pref_new_q, $pref_search_q);

} else if ($action == "dispref_display") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);

  $pref_new_q = run_query_display_pref($auth->auth["uid"], "project_new", 1);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "project", 1);

  dis_project_display_pref($pref_new_q, $pref_search_q);

} else if ($action == "dispref_level") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);

  $pref_new_q = run_query_display_pref($auth->auth["uid"], "project_new", 1);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "project", 1);

  dis_project_display_pref($pref_new_q, $pref_search_q);
}


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $company hash
// returns : $company hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_project() {

  global $param_project, $tf_missing, $hd_state;
  global $tf_name, $tf_company_name, $tf_soldtime;
  global $sel_tt, $sel_manager, $sel_member, $param_ext;
  global $action, $ext_action, $ext_url, $ext_id, $ext_target, $title;
  global $HTTP_POST_VARS, $HTTP_GET_VARS, $ses_list;
  global $cdg_param;

  // Project fields
  if (isset ($param_project)) $project["id"] = $param_project;
  if (isset ($tf_soldtime)) $project["soldtime"] = $tf_soldtime;
  if (isset ($tf_missing)) $project["missing"] = $tf_missing;
  if (isset ($hd_state)) $project["state"] = $hd_state;
//   if (isset ($sel_state)) $project["state"] = $sel_state;
  if (isset ($cb_archive)) {
    $project["archive"] = $cb_archive;
  } else {
    $project["archive"] = "0";
  }

  // Search fields
  if (isset ($tf_name)) $project["name"] = $tf_name;
  if (isset ($tf_company_name)) $project["company_name"] = $tf_company_name;
  if (isset ($sel_tt)) $project["tt"] = $sel_tt;
  if (isset ($sel_manager)) $project["manager"] = $sel_manager;
  if (isset ($sel_member)) $project["member"] = $sel_member;

  // External param
  if (isset ($ext_action)) $project["ext_action"] = $ext_action;
  if (isset ($ext_url)) $project["ext_url"] = $ext_url;
//   if (isset ($ext_id)) $project["ext_id"] = $ext_id;
  if (isset ($param_ext)) $project["ext_id"] = $param_ext;
  if (isset ($ext_target)) $project["ext_target"] = $ext_target;
  if (isset ($title)) $project["title"] = stripslashes($title);

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    $nb_mem = 0;
//     $nb_list = 0;
    while ( list( $key ) = each( $http_obm_vars ) ) {

//       echo "--dtc--$key--";

      if (strcmp(substr($key, 0, 7),"cb_user") == 0) {
	$nb_mem++;
        $mem_num = substr($key, 7);

// 	echo "--smlp--$mem_num--";

        $project["mem$nb_mem"] = $mem_num;
      } 
// elseif (strcmp(substr($key, 0, 7),"cb_user") == 0) {
// 	$nb_list++;
//         $project_num = substr($key, 7);
//         $project["list_$nb_list"] = $project_num;
// 	// register the list in the list session array
// 	$ses_list[$project_num] = $project_num;
//       }
    }
    $project["mem_nb"] = $nb_mem;
//     $project["list_nb"] = $nb_list;
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

// Insert
  $actions["PROJECT"]["insert"] = array (
    'Url'      => "$path/project/project_index.php?action=insert",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                        );

// Init
  $actions["PROJECT"]["affect"] = array (
    'Url'      => "$path/project/project_index.php?action=affect",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
                                        );

// Detail Consult
  $actions["COMPANY"]["detailconsult"]  = array (
    'Url'      => "$path/company/company_index.php?action=detailconsult",
    'Right'    => $company_read,
    'Condition'=> array ('None') 
                                     		 );

// Detail Update
  $actions["PROJECT"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/project/project_index.php?action=detailupdate&amp;param_project=".$project["id"]."",
    'Right'    => $project_write,
    'Condition'=> array ('detailconsult', 'update') 
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
    'Url'      => "$path/project/project_index.php?action=advanceupdate",
    'Right'    => $project_write,
    'Condition'=> array ('None') 
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
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;title=".urlencode($l_add_contact)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path."/project/project_index.php")."&amp;ext_id=".$list["id"]."&amp;ext_target=$l_list",
    'Right'    => $user_write,
    'Popup'    => 1,
    'Target'   => $l_list,
    'Condition'=> array ('insert', 'create', 'member_add','member_del') 
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
  $actions["PROJECT"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/project/project_index.php?action=admin",
    'Right'    => $project_admin_read,
    'Condition'=> array ('all') 
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
///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();


</SCRIPT>
