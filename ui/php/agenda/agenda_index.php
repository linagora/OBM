<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_index.php                                             //
//     - Desc : Agenda Index File                                            //
// 2002-11-26 - Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- this week for this user.
// - decision
// - view_day
// - view_week
// - view_month
// - view_year
// - new
// - insert
// - insert_conflict
// - detailconsult
// - detailupdate
// - update
// - update_decision
// - rights_admin    -- access rights screen
// - rights_update   -- Update agenda access rights
///////////////////////////////////////////////////////////////////////////////
$time = (time() + microtime());
$www = "   <p class=\"messageInfo\">
    	<a href=\"http://validator.w3.org/check/referer\"><img
        src=\"http://www.w3.org/Icons/valid-xhtml10\"
        alt=\"Valid XHTML 1.0!\" height=\"31\" width=\"88\" /></a>
	<a href=\"http://jigsaw.w3.org/css-validator/\">
 	 <img style=\"border:0;width:88px;height:31px\"
       src=\"http://jigsaw.w3.org/css-validator/images/vcss\" 
       alt=\"Valid CSS!\" />²
	 </a>
  	</p>";
///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COM";
$module = "agenda";
$extra_css = "calendar.css";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
$uid = $auth->auth["uid"];

require("agenda_query.inc");
require("agenda_display.inc");
if (count($sel_user_id) != 0 ) {
  $agenda_user_view = $sel_user_id;
}
$sess->register("agenda_user_view");
page_close();
$sel_user_id = $agenda_user_view;

if ($action == "") $action = "index";
$agenda = get_param_agenda();
get_agenda_action();
$perm->check_permissions($module, $action);

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($popup) {
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
  if ($action == "calendar") {
    display_head($l_agenda);
    require("$obminclude/calendar.inc");
    display_end();
  }
  elseif ($action == "export") {
    dis_export_handle($agenda);
  }
  exit();
}
//////////////////////////////////////////////////////////////////////////////

if ($action == "index") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $sel_user_id = slice_user($sel_user_id);
  $obm_wait = run_query_waiting_events();
  if($obm_wait->nf() != 0) {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = html_waiting_events($obm_wait);
  }
  else {
    if(count($sel_user_id) != 0){
      $p_user_array =  $sel_user_id;
    }
    else {
      $p_user_array =  array($uid);
    }
    $user_q = store_users(run_query_get_user_name($p_user_array));
    $user_obm = run_query_userobm_readable();  
    require("agenda_js.inc");
    $display["result"] = dis_week_planning($agenda,$user_q,$user_obm);
    $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
  }
} elseif($action == "decision") {
/////////////////////////TODO : CONFLICTS//////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $sel_user_id = slice_user($sel_user_id);
  $conflict = run_query_insert_decision($agenda);
  $obm_wait = run_query_waiting_events();
  if($obm_wait->nf() != 0) {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = html_waiting_events($obm_wait);
  }
  else {
    require("agenda_js.inc");
    $display["msg"] .= display_ok_msg($l_update_ok); 
    $sel_user_id = array($uid);
    $p_user_array = $sel_user_id ;
    $user_q = store_users(run_query_get_user_name($p_user_array));
    $user_obm = run_query_userobm_readable();  
    $display["result"] = dis_week_planning($agenda,$user_q,$user_obm);
    $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
  }
} elseif ($action == "view_day") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $sel_user_id = slice_user($sel_user_id);
  require("agenda_js.inc");
  if (count($sel_user_id) != 0) {
    $p_user_array = $sel_user_id;
  }
  else {
    $p_user_array = array($uid);
  }
  $user_q = store_users(run_query_get_user_name($p_user_array));
  $user_obm = run_query_userobm_readable();  
  $display["result"] = dis_day_planning($agenda,$user_q,$user_obm);
  $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);

} elseif ($action == "view_week") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
    $sel_user_id = slice_user($sel_user_id);
    require("agenda_js.inc");
    if (count($sel_user_id) != 0) {
      $p_user_array =  $sel_user_id;
    }
    else {
      $p_user_array =  array($uid);
    }
    $user_q = store_users(run_query_get_user_name($p_user_array));
    $user_obm = run_query_userobm_readable();  
    $display["result"] = dis_week_planning($agenda,$user_q,$user_obm);
    $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
} elseif ($action == "view_month") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $sel_user_id = slice_user($sel_user_id);
  require("agenda_js.inc");
  if (count($sel_user_id) != 0) {
    $p_user_array = $sel_user_id;
  }
  else {
    $p_user_array = array($uid);
  }
  $user_q = store_users(run_query_get_user_name($p_user_array));
  $user_obm = run_query_userobm_readable();  
  $display["result"] = dis_month_planning($agenda,$user_q,$user_obm);
  $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
} elseif ($action == "view_year") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $sel_user_id = slice_user($sel_user_id);
  if (count($sel_user_id) != 0) {
    $p_user_array = $sel_user_id;
  }
  else {
    $p_user_array = array($uid);
  }
  $user_q = store_users(run_query_get_user_name($p_user_array));
  $user_obm = run_query_userobm_readable();  
  $display["result"] = dis_year_planning($agenda,$user_q,$user_obm);
  $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
} elseif ($action == "new") {
/////////////////////////TODO : CONFLICTS//////////////////////////////////////  
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  require("$obminclude/calendar.js");
  $grp_obm = run_query_group_writable();
  $cat_event = run_query_get_eventcategories();
  if($p_user_meeting==1) {
    $p_user_array = $sel_user_id;
  }else {
    $p_user_array = array($uid);
  }
  $user_obm = run_query_userobm_in($p_user_array);
  $display["detail"] = dis_event_form($action, $agenda, NULL, $user_obm,$grp_obm, $cat_event, $p_user_array);
} elseif ($action == "insert") {
/////////////////////////TODO : CONFLICTS//////////////////////////////////////  
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($agenda)){    
    $conflict = run_query_add_event($agenda,$sel_user_id,$event_id);
    require("agenda_js.inc");
    $sel_user_id = array($uid);      
    $p_user_array = $sel_user_id ;
    $display["msg"] .= display_ok_msg($l_insert_ok);
    $user_q = store_users(run_query_get_user_name($p_user_array));
    $user_obm = run_query_userobm_readable();  
    $display["result"] = dis_week_planning($agenda,$user_q,$user_obm);
    $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
  }
  else {
    require("$obminclude/calendar.js");
    require("agenda_js.inc");
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $user_obm = run_query_userobm_in($sel_user_id);
    $grp_obm = run_query_group_writable();
    $cat_event = run_query_get_eventcategories();
    $display["detail"] = dis_event_form($action, $agenda, NULL, $user_obm,$grp_obm, $cat_event, $sel_user_id);
  }
} elseif ($action == "detailconsult") {
////////////////////////////////////DONE///////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  if ($param_event > 0) {
    $eve_q = run_query_detail($param_event);
    $cust_q = run_query_event_customers($param_event);
    $display["detailInfo"] = display_record_info($eve_q);
    $display["detail"] = html_calendar_consult($eve_q, $cust_q,$obm_q_grp);
  }
} elseif ($action == "detailupdate") {
////////////////////////////////////DONE///////////////////////////////////////  
///////////////////////////////////////////////////////////////////////////////
if ($param_event > 0) {  
  $sel_user_id = slice_user($sel_user_id);
  require("$obminclude/calendar.js");
  require("agenda_js.inc");  
  $grp_obm = run_query_group_writable();
  $cat_event = run_query_get_eventcategories();
  $eve_q = run_query_detail($param_event);  
  $p_user_array = run_query_event_customers_array($param_event);
  $user_obm = run_query_userobm_in($p_user_array);  
  $display["detailInfo"] = display_record_info($eve_q);
  $display["detail"] = dis_event_form($action, $agenda,$eve_q, $user_obm,$grp_obm, $cat_event, $p_user_array);
  }
} elseif ($action == "update") {
/////////////////////////TODO : CONFLICTS//////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($agenda)) {
    run_query_modify_event($agenda,$sel_user_id,$event_id);
    require("agenda_js.inc");
    $display["msg"] .= display_ok_msg($l_update_ok);
    $sel_user_id = array($uid);
    $p_user_array = $sel_user_id ;
    $user_q = store_users(run_query_get_user_name($p_user_array));
    $user_obm = run_query_userobm_readable();  
    $display["result"] = dis_week_planning($agenda,$user_q,$user_obm);
    $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
  }
  else {
    require("agenda_js.inc");
    require("$obminclude/calendar.js");    
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $user_obm = run_query_userobm_in($sel_user_id);
    $grp_obm = run_query_group_writable();
    $cat_event = run_query_get_eventcategories();
    $display["detail"] = dis_event_form($action, $agenda, NULL, $user_obm,$grp_obm, $cat_event, $sel_user_id);
  }
} elseif ($action == "update_decision") {
/////////////////////////TODO : CONFLICTS//////////////////////////////////////  
///////////////////////////////////////////////////////////////////////////////
  run_query_update_occurence_state($agenda["id"],$auth->auth["uid"],$agenda["decision_event"]);
  require("agenda_js.inc");
  $display["msg"] .= display_ok_msg($l_update_ok);
  $sel_user_id = array($uid);
  $p_user_array = $sel_user_id ;
  $user_q = store_users(run_query_get_user_name($p_user_array));
  $user_obm = run_query_userobm_readable();  
  $display["result"] = dis_week_planning($agenda,$user_q,$user_obm);
  $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
} elseif ($action == "check_delete") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  if ($param_event > 0) {
    $display["detail"] = html_dis_delete($agenda);
  }
} elseif ($action == "delete") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  if ($param_event > 0) {
     run_query_delete($agenda);
  }
  if (count($sel_user_id) != 0) {
    $p_user_array =  $sel_user_id;
  }
  else {
    $p_user_array =  array($uid);
  }
  $user_q = store_users(run_query_get_user_name($p_user_array));
  $user_obm = run_query_userobm_readable();  
  $display["result"] = dis_week_planning($agenda,$user_q,$user_obm);
  $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);
} elseif ($action == "rights_admin") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_right_admin($agenda);

} elseif ($action == "rights_update") {
////////////////////////////////////DONE///////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  run_query_update_right($agenda);
  $display["detail"] = dis_right_admin($agenda);

} elseif ($action == "new_meeting")  {
////////////////////////////////////DONE///////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  require("$obminclude/calendar.js");
  $p_user_array = array($uid);  
  $user_obm = run_query_userobm_in($p_user_array);
  $display["detail"] = dis_meeting_form($agenda, $user_obm, $p_user_array);

} elseif ($action == "perform_meeting")  {
////////////////////////////////////DONE///////////////////////////////////////  
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  if (count($agenda["user_meeting"]) != 0) {
    $p_user_array =  $agenda["user_meeting"] ;
  } else {
    $p_user_array =  array($uid);
  }
  $user_q = store_users(run_query_get_user_name($p_user_array));
  $user_obm = run_query_userobm_in($p_user_array);      
  $display["features"] = html_planning_bar($agenda,$user_obm, $p_user_array,$user_q);  
  $display["detail"] = dis_free_interval($agenda,$user_q,$user_obm);

} elseif ($action == "admin")  {
////////////////////////////////////DONE///////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "category_insert")  {
////////////////////////////////////DONE///////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_category_insert($agenda);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_category_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_category_insert_error);
  }
  require("agenda_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "category_update")  {
////////////////////////////////////DONE///////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_category_update($agenda);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_category_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_category_update_error);
  }
  require("agenda_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "category_checklink")  {
////////////////////////////////////DONE///////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_category_links($agenda);//$sel_category);

} elseif ($action == "category_delete")  {
////////////////////////////////////DONE///////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_category_delete($agenda);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_category_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_category_delete_error);
  }
  require("agenda_js.inc");
  $display["detail"] = dis_admin_index();
}
if (count($sel_user_id) != 0 ) {
  $agenda_user_view = $sel_user_id;
}
$display["head"] = display_head($l_agenda);
$display["header"] = generate_menu($module,$section);      
$display["end"] = display_end();

display_page($display);
$time = (time() + microtime()) - $time;
echo $time;
///////////////////////////////////////////////////////////////////////////////
// Stores in $agenda hash, Agenda parameters transmited
// returns : $agenda hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_agenda() {
  global $param_date,$param_event,$tf_title,$sel_category_id,$sel_priority,$ta_event_description;
  global $set_start_time, $set_stop_time,$tf_date_begin,$sel_time_begin,$sel_min_begin,$sel_time_end,$sel_min_end;
  global $tf_date_end,$sel_repeat_kind,$hd_conflict_end,$hd_old_end,$hd_old_begin,$action,$param_user;
  global $cdg_param,$cb_repeatday_0,$cb_repeatday_1,$cb_repeatday_2,$cb_repeatday_3,$cb_repeatday_4,$cb_repeatday_5;
  global $cb_repeatday_6,$cb_repeatday_7,$tf_repeat_end,$cb_force,$cb_privacy,$cb_repeat_update,$rd_conflict_event;
  global $hd_date_begin, $hd_date_end,$rd_decision_event,$param_date_begin,$param_date_end,$cb_mail,$param_duration;
  global $sel_accept_write,$sel_deny_write,$sel_deny_read,$sel_accept_read,$sel_time_duration,$sel_min_duration;
  global $hd_category_label,$tf_category_upd, $sel_category,$tf_category_new,$sel_group_id,$sel_user_meeting_id;

  // Agenda fields
  if (isset($tf_category_new)) $agenda["category_label"] = $tf_category_new;
  if (isset($hd_category_label)) $agenda["category_label"] = $hd_category_label;
  if (isset($tf_category_upd)) $agenda["category_label"] = $tf_category_upd;
  if (isset($sel_category)) $agenda["category_id"] = $sel_category;
  if (isset ($param_date))
    $agenda["date"] = $param_date; 
  else 
    $agenda["date"] = date("Ymd",time());
  if (isset($param_event)) $agenda["id"] = $param_event;
  if (isset($tf_title)) $agenda["title"] = $tf_title;
  if (isset($sel_category_id)) $agenda["category"] = $sel_category_id;
  if (isset($sel_priority)) $agenda["priority"] = $sel_priority;
  if (isset($ta_event_description)) $agenda["description"] = $ta_event_description;
  if (isset($cb_force))  $agenda["force"] = $cb_force;
  if (isset($cb_privacy))  $agenda["privacy"] = $cb_privacy;
  if (is_array($rd_conflict_event)) $agenda["conflict_event"] = $rd_conflict_event;
  if (is_array($hd_conflict_end)) $agenda["conflict_end"] = $hd_conflict_end;
  if (isset($hd_old_begin)) $agenda["old_begin"] = $hd_old_begin;
  if (isset($hd_old_end)) $agenda["old_end"] = $hd_old_end;
  if (isset($cb_mail)) $agenda["mail"] = $cb_mail;
  if (is_array($sel_accept_write)) $agenda["accept_w"] = $sel_accept_write;
  if (is_array($sel_deny_write)) $agenda["deny_w"] = $sel_deny_write;
  if (is_array($sel_deny_read)) $agenda["deny_r"] = $sel_deny_read;
  if (is_array($sel_accept_read)) $agenda["accept_r"] = $sel_accept_read;
  if (is_array($sel_user_meeting_id)) $agenda["user_meeting"] = $sel_user_meeting_id;

  if (isset($sel_time_duration)) {
    $agenda["duration"] = $sel_time_duration;
    if(isset($sel_min_duration)) {
      $agenda["duration"] +=  $sel_min_duration/60;
    }
  }
  if(isset($param_user)) $agenda["user_id"] = $param_user;
  if(isset($param_duration)) $agenda["duration"] = $param_duration;
  if (isset($tf_repeat_end)){
    ereg ("([0-9]{4}).([0-9]{2}).([0-9]{2})",$tf_repeat_end , $day_array1);
    $agenda["repeat_end"] =  $day_array1[1].$day_array1[2].$day_array1[3];
   }
  if (isset($cb_repeat_update)) $agenda["repeat_update"] = 1;
  if (isset($tf_date_begin)) {
    ereg ("([0-9]{4}).([0-9]{2}).([0-9]{2})",$tf_date_begin , $day_array2);
    $agenda["date_begin"] .=  $day_array2[1].$day_array2[2].$day_array2[3];
    $agenda["date"] = $agenda["date_begin"];
    if (isset($sel_time_begin) && isset($sel_min_begin)) {
      $agenda["date_begin"] = $agenda["date_begin"].$sel_time_begin.$sel_min_begin;
    }
    else {
      $agenda["date_begin"] = date("YmdHi",strtotime("+$set_start_time hours",strtotime($agenda["date_begin"])));
    }
  }
  else {
    $agenda["date_begin"] = date("YmdHi",strtotime("+$set_start_time hours",strtotime(date("Ymd"))));
  }
  if (isset($tf_date_end)) {
    ereg ("([0-9]{4}).([0-9]{2}).([0-9]{2})",$tf_date_end , $day_array);
    $agenda["date_end"] =  $day_array[1].$day_array[2].$day_array[3];
    if (isset($sel_time_end) && isset($sel_min_end)) {
      $agenda["date_end"] =  $agenda["date_end"].$sel_time_end.$sel_min_end;
    }
    else {
      $agenda["date_end"] = date("YmdHi",strtotime("+$set_stop_time hours",strtotime($agenda["date_end"])));
    }
  }
  else {
    $agenda["date_end"] = date("YmdHi",strtotime("+$set_stop_time hours",strtotime(date("Ymd"))));
  }
  if (isset($param_date_begin)) { 
    $agenda["date_begin"] = $param_date_begin;
  }
  if (isset($param_date_end))   
    $agenda["date_end"] = $param_date_end;
  if (isset($sel_repeat_kind)) $agenda["kind"] = $sel_repeat_kind;
  for ($i=0; $i<7; $i++) {
    if (isset(${"cb_repeatday_".$i}))  {
      $agenda["repeat_days"] .= '1';
    }
    else {
      $agenda["repeat_days"] .= '0';
    }
      
  }  
  if (isset($hd_date_begin)) $agenda["date_begin"] = $hd_date_begin;
  if (isset($hd_date_end)) $agenda["date_end"] = $hd_date_end;
  if (isset($rd_decision_event)) $agenda["decision_event"] = $rd_decision_event;
  if (is_array($sel_group_id)) $agenda["group"] = $sel_group_id;


  if (debug_level_isset($cdg_param)) {
    if ( $agenda ) {
      while ( list( $key, $val ) = each( $agenda ) ) {
        echo "<br />agenda[$key]=";var_dump($val);
      }
    }
    echo "<br />action = $action";
  }

  return $agenda;
}


///////////////////////////////////////////////////////////////////////////////
//  Agenda Action 
///////////////////////////////////////////////////////////////////////////////
function get_agenda_action() {
  global $actions, $path;
  global $l_header_update,$l_header_right,$l_header_meeting;
  global $l_header_day,$l_header_week,$l_header_year,$l_header_delete;
  global $l_header_month,$l_header_new_event,$param_event,$param_date,$l_header_admin, $l_header_export;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  // Index
  $actions["agenda"]["index"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
  
  // Decision
  $actions["agenda"]["decision"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=decision",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                         );

  // Decision
  $actions["agenda"]["calendar"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=calendar",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                         );
  // New   
  $actions["agenda"]["new"] = array (
    'Name'     => $l_header_new_event,
    'Url'      => "$path/agenda/agenda_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('index','detailconsult','insert','insert_conflict',
		  'update_decision','decision','update','delete',
                  'view_month','view_week','view_day','view_year',
		  'rights_admin','rights_update')
		);
		
//Detail Update

  $actions["agenda"]["detailconsult"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/agenda/agenda_index.php?action=detailconsult&amp;param_event=".$param_event."&amp;param_date=$param_date",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

		
//Detail Update

  $actions["agenda"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/agenda/agenda_index.php?action=detailupdate&amp;param_event=".$param_event."&amp;param_date=$param_date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

//Check Delete

  $actions["agenda"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/agenda/agenda_index.php?action=check_delete&amp;param_event=".$param_event."&amp;param_date=$param_date",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult') 
                                     		 );


//Delete
  $actions["agenda"]["delete"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=delete&amp;param_event=".$param_event."&amp;param_date=$param_date",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		 );

						 
//Insert

  $actions["agenda"]["insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );


//View Year

  $actions["agenda"]["view_year"] = array (
    'Name'     => $l_header_year,
    'Url'      => "$path/agenda/agenda_index.php?action=view_year",
    'Right'    => $cright_read,  
    'Condition'=> array ('all') 
                                    	    );

//View Month

  $actions["agenda"]["view_month"] = array (
    'Name'     => $l_header_month,
    'Url'      => "$path/agenda/agenda_index.php?action=view_month",
    'Right'    => $cright_read,  
    'Condition'=> array ('all') 
                                    	    );

//View Week

  $actions["agenda"]["view_week"] = array (
    'Name'     => $l_header_week,
    'Url'      => "$path/agenda/agenda_index.php?action=view_week",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
                                    	  );

//View Day

  $actions["agenda"]["view_day"] = array (
    'Name'     => $l_header_day,
    'Url'      => "$path/agenda/agenda_index.php?action=view_day",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
                                    	 );

//Update

  $actions["agenda"]["update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );
					 
//Update

  $actions["agenda"]["update_decision"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );
					 
//Meeting managment.					 
  $actions["agenda"]["new_meeting"] = array (
    'Name'     => $l_header_meeting,
    'Url'      => "$path/agenda/agenda_index.php?action=new_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                         );

//Meeting managment.					 
  $actions["agenda"]["perform_meeting"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=perform_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

  // Right admin.					 
  $actions["agenda"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/agenda/agenda_index.php?action=rights_admin",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                         );

  // Update Right
  $actions["agenda"]["rights_update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=rights_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

// Admin
  $actions["agenda"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/agenda/agenda_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );
				       
// Kind Insert
  $actions["agenda"]["category_insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions["agenda"]["category_update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions["agenda"]["category_checklink"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions["agenda"]["category_delete"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
					       
// Export
  $actions["agenda"]["export"] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/agenda/agenda_index.php?action=export&amp;popup=1",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                       );

}
  
///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();

?>
