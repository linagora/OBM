<?
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
// - 
///////////////////////////////////////////////////////////////////////////////

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
$menu="AGENDA";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("agenda_query.inc");
require("agenda_display.inc");
if(count($sel_user_id) != 0 ) {
  $agenda_user_view = $sel_user_id;
}
$sess->register("agenda_user_view");
page_close();
$sel_user_id = $agenda_user_view;
$agenda = get_param_agenda();
get_agenda_action();
$perm->check();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
if ($action == "") $action = "index";
display_head($l_agenda);     // Head & Body
generate_menu($menu,$section);      // Menu
//////////////////////////////////////////////////////////////////////////////

if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_waiting_events();
  if($obm_wait->nf() != 0) {
    display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    html_waiting_events($obm_wait);
  }
  else {
    if(count($sel_user_id) != 0){
      $p_user_array =  $sel_user_id;
    }
    else {
      $p_user_array =  array($auth->auth["uid"]);
    }
    $obm_q = run_query_week_event_list($agenda,$p_user_array);
    $user_q = run_query_get_user_name($p_user_array);
    $user_obm = run_query_userobm();  
    dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
  }
}

elseif($action == "decision") {
///////////////////////////////////////////////////////////////////////////////
  $conflict = run_query_insert_decision($agenda);
  if (count($conflict) == 0) {
    display_ok_msg($l_update_ok);  
    $p_user_array = array($auth->auth["uid"]);
    $obm_q = run_query_week_event_list($agenda,$p_user_array);
    $user_q = run_query_get_user_name($p_user_array);
    $user_obm = run_query_userobm();
    dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
  }
  else {
    require("agenda_js.inc");
    display_warn_msg($l_update_warning);      
    html_dis_conflict($agenda,$conflict,$agenda["id"],1);
  }
}

elseif ($action == "view_day") {
///////////////////////////////////////////////////////////////////////////////
  if (count($sel_user_id) != 0) {
    $p_user_array = $sel_user_id;
  }
  else {
    $p_user_array = array($auth->auth["uid"]);
  }
  $obm_q = run_query_day_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_day_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}

elseif ($action == "view_week") {
///////////////////////////////////////////////////////////////////////////////
  if (count($sel_user_id) != 0) {
    $p_user_array =  $sel_user_id;
  }
  else {
    $p_user_array =  array($auth->auth["uid"]);
  }
  $obm_q = run_query_week_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}

elseif ($action == "view_month") {
///////////////////////////////////////////////////////////////////////////////
 if (count($sel_user_id) != 0) {
    $p_user_array = $sel_user_id;
  }
  else {
    $p_user_array = array($auth->auth["uid"]);
  }
  $obm_q = run_query_month_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_month_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}

elseif ($action == "view_year") {
///////////////////////////////////////////////////////////////////////////////
  if (count($sel_user_id) != 0) {
    $p_user_array = $sel_user_id;
  }
  else {
    $p_user_array = array($auth->auth["uid"]);
  }
  $obm_q = run_query_year_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_year_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}

elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $user_obm = run_query_userobm();
  $cat_event = run_query_get_eventcategories();
  $p_user_array = array($auth->auth["uid"]);
  $user_obm = run_query_userobm();  
  dis_event_form($action, $agenda, NULL, $user_obm, $cat_event, $p_user_array);
}

elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($agenda)){    
    $conflict = run_query_add_event($agenda,$sel_user_id,$event_id);
    if (count($conflict) == 0) {
      $p_user_array = array($auth->auth["uid"]);
      $obm_q = run_query_week_event_list($agenda,$p_user_array);
      $user_q = run_query_get_user_name($p_user_array);
      $user_obm = run_query_userobm();
      display_ok_msg($l_insert_ok);
      dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
    }
    elseif($agenda["force"] == 1) {
      require("agenda_js.inc");
      display_warn_msg($l_insert_warning);
      html_dis_conflict($agenda,$conflict,$event_id,1);
    }
    else{
      display_err_msg($l_insert_error);
      html_dis_conflict($agenda,$conflict,'',0);
      $p_user_array = array($auth->auth["uid"]);
      $obm_q = run_query_week_event_list($agenda,$p_user_array);
      $user_q = run_query_get_user_name($p_user_array);
      $user_obm = run_query_userobm();
      dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
    }
  }
  else {
    require("agenda_js.inc");
    display_warn_msg($l_invalid_data . " : " . $err_msg);
    $user_obm = run_query_userobm();
    $cat_event = run_query_get_eventcategories();
    dis_event_form($action, $agenda, NULL, $user_obm, $cat_event, $sel_user_id);
  }
}

elseif ($action == "insert_conflict") {
///////////////////////////////////////////////////////////////////////////////
  run_query_manage_conflict($agenda);  
  $p_user_array =  array($auth->auth["uid"]);
  $obm_q = run_query_week_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();
  display_ok_msg($l_insert_ok);
  dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}

elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  if ($param_event > 0) {
    $eve_q = run_query_detail($param_event,$agenda["date"]);
    $cust_q = run_query_event_customers($param_event,$agenda["date"]);
    display_record_info($eve_q->f("calendarevent_usercreate"),$eve_q->f("calendarevent_userupdate"),$eve_q->f("calendarevent_timecreate"),$eve_q->f("calendarevent_timeupdate"));
    html_calendar_consult($eve_q, $cust_q);
  }
}

elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
if ($param_event > 0) {  
  require("agenda_js.inc");
  $user_obm = run_query_userobm();
  $cat_event = run_query_get_eventcategories();
  $user_obm = run_query_userobm();
  $cat_event = run_query_get_eventcategories();
  $eve_q = run_query_detail($param_event,$agenda["date"]);  
  $p_user_array = run_query_event_customers_array($param_event,$agenda["date"]);
  display_record_info($eve_q->f("calendarevent_usercreate"),$eve_q->f("calendarevent_userupdate"),$eve_q->f("calendarevent_timecreate"),$eve_q->f("calendarevent_timeupdate"));
  dis_event_form($action, $agenda,$eve_q, $user_obm, $cat_event, $p_user_array);
  }
}

elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($agenda)) {
    $conflict = run_query_modify_event($agenda,$sel_user_id,$event_id);
    if(count($conflict) == 0) {
      display_ok_msg($l_update_ok);
      $p_user_array =  array($auth->auth["uid"]);
      $obm_q = run_query_week_event_list($agenda,$p_user_array);
      $user_q = run_query_get_user_name($p_user_array);
      $user_obm = run_query_userobm();
      dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
    }
    elseif($agenda["force"] == 1) {
      require("agenda_js.inc");
      display_warn_msg($l_update_warning);      
      html_dis_conflict($agenda,$conflict,$event_id,1);
    }
    else{
      display_error_msg($l_update_error); 
      html_dis_conflict($agenda,$conflict,'',0);
      $p_user_array =  array($auth->auth["uid"]);
      $obm_q = run_query_week_event_list($agenda,$p_user_array);
      $user_q = run_query_get_user_name($p_user_array);
      $user_obm = run_query_userobm();
      dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
    }
  }
  else {
    require("agenda_js.inc");
    display_warn_msg($l_invalid_data . " : " . $err_msg);
    $user_obm = run_query_userobm();
    $cat_event = run_query_get_eventcategories();
    dis_event_form($action, $agenda, NULL, $user_obm, $cat_event, $sel_user_id);
  }
}

elseif ($action == "update_decision") {
///////////////////////////////////////////////////////////////////////////////
  $conflict = run_query_change_decision($agenda);
  if(count($conflict) == 0) {
    display_ok_msg($l_update_ok);  
    $p_user_array =  array($auth->auth["uid"]);
    $obm_q = run_query_week_event_list($agenda,$p_user_array);
    $user_q = run_query_get_user_name($p_user_array);
    $user_obm = run_query_userobm();
    dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
  }
  else {
    require("agenda_js.inc");
    display_warn_msg($l_update_warning);      
    html_dis_conflict($agenda,$conflict,$agenda["id"],1);
  }
}

elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if ($param_event > 0) {
    html_dis_delete($agenda);
  }

}
elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if ($param_event > 0) {
     run_query_delete($agenda);
  }
  if (count($sel_user_id) != 0) {
    $p_user_array =  $sel_user_id;
  }
  else {
    $p_user_array =  array($auth->auth["uid"]);
  }
  $obm_q = run_query_week_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);     

}


///////////////////////////////////////////////////////////////////////////////
// Stores in $agenda hash, Agenda parameters transmited
// returns : $agenda hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_agenda() {
  global $param_date,$param_event,$tf_title,$sel_category_id,$sel_priority,$ta_event_description;
  global $set_start_time, $set_stop_time,$tf_date_begin,$sel_time_begin,$sel_min_begin,$sel_time_end,$sel_min_end;
  global $tf_date_end,$sel_repeat_kind,$hd_conflict_end,$hd_old_end,$hd_old_begin;
  global $cdg_param,$cb_repeatday_0,$cb_repeatday_1,$cb_repeatday_2,$cb_repeatday_3,$cb_repeatday_4,$cb_repeatday_5;
  global $cb_repeatday_6,$cb_repeatday_7,$tf_repeat_end,$cb_force,$cb_privacy,$cb_repeat_update,$rd_conflict_event;
  global $hd_date_begin, $hd_date_end,$rd_decision_event;

	
  // Deal fields
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
  
  if (isset($tf_repeat_end)){
    ereg ("([0-9]{4}).([0-9]{2}).([0-9]{2})",$tf_repeat_end , $day_array1);
    $agenda["repeat_end"] =  $day_array1[1].$day_array1[2].$day_array1[3];
    echo $tf_repeat_end;
   }
  if (isset($cb_repeat_update)) $agenda["repeat_update"] = 1;
  if (isset($tf_date_begin)) {
    ereg ("([0-9]{4}).([0-9]{2}).([0-9]{2})",$tf_date_begin , $day_array2);
    $agenda["date_begin"] .=  $day_array2[1].$day_array2[2].$day_array2[3];
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

  if (debug_level_isset($cdg_param)) {
    if ( $agenda ) {
      while ( list( $key, $val ) = each( $agenda ) ) {
        echo "<br />agenda[$key]=$val";
      }
    }
  }

  return $agenda;
}


///////////////////////////////////////////////////////////////////////////////
//  Agenda Action 
///////////////////////////////////////////////////////////////////////////////
function get_agenda_action() {
  global $actions, $path;
  global $l_header_update;
  global $l_header_day,$l_header_week,$l_header_year,$l_header_delete;
  global $l_header_month,$l_header_new_event,$param_event,$param_date;
  global $agenda_read, $agenda_write, $agenda_admin_read, $agenda_admin_write;

//Index

  $actions["AGENDA"]["index"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=index",
    'Right'    => $agenda_read,
    'Condition'=> array ('None') 
  );
  
//Decision

  $actions["AGENDA"]["decision"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=decision",
    'Right'    => $agenda_read,
    'Condition'=> array ('None') 
                                         );

//New   
 
  $actions["AGENDA"]["new"] = array (
    'Name'     => $l_header_new_event,
    'Url'      => "$path/agenda/agenda_index.php?action=new",
    'Right'    => $agenda_write,
    'Condition'=> array ('index','detailconsult',
                  'view_month','view_week','view_day','view_year',
                  'insert','insert_conflict','cancel_insert','confirm_insert',
		  'update_decision','decision') 
                                    );


//Insert

  $actions["AGENDA"]["insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=insert",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );
//Insert Conflict

  $actions["AGENDA"]["insert_conflict"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=insert_conflict",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );

//Cancel Insert

  $actions["AGENDA"]["cancel_insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=cancel_insert",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );

//Confirm Insert

  $actions["AGENDA"]["confirm_insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=confirm_insert",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );

//View Year

  $actions["AGENDA"]["view_year"] = array (
    'Name'     => $l_header_year,
    'Url'      => "$path/agenda/agenda_index.php?action=view_year",
    'Right'    => $agenda_read,  
    'Condition'=> array ('all') 
                                    	    );

//View Month

  $actions["AGENDA"]["view_month"] = array (
    'Name'     => $l_header_month,
    'Url'      => "$path/agenda/agenda_index.php?action=view_month",
    'Right'    => $agenda_read,  
    'Condition'=> array ('all') 
                                    	    );

//View Week

  $actions["AGENDA"]["view_week"] = array (
    'Name'     => $l_header_week,
    'Url'      => "$path/agenda/agenda_index.php?action=view_week",
    'Right'    => $agenda_read, 
    'Condition'=> array ('all') 
                                    	  );

//View Day

  $actions["AGENDA"]["view_day"] = array (
    'Name'     => $l_header_day,
    'Url'      => "$path/agenda/agenda_index.php?action=view_day",
    'Right'    => $agenda_read, 
    'Condition'=> array ('all') 
                                    	 );

//Detail Consult

  $actions["AGENDA"]["detail_consult"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=detail_consult",
    'Right'    => $agenda_read,
    'Condition'=> array ('None') 
                                         );

//Check Delete

  $actions["AGENDA"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/agenda/agenda_index.php?action=check_delete&amp;param_event=".$param_event."&amp;param_date=$param_date",
    'Right'    => $agenda_admin_write,
    'Condition'=> array ('detailconsult') 
                                     		 );

//Delete
  $actions["AGENDA"]["delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/agenda/agenda_index.php?action=delete&amp;param_event=".$param_event."&amp;param_date=$param_date",
    'Right'    => $agenda_admin_write,
    'Condition'=> array ('None') 
                                     		 );
						 
//Detail Update

  $actions["AGENDA"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/agenda/agenda_index.php?action=detailupdate&amp;param_event=".$param_event."&amp;param_date=$param_date",
    'Right'    => $agenda_write,
    'Condition'=> array ('detailconsult') 
                                     		 );

//Update

  $actions["AGENDA"]["update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=update",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );
					 
//Update

  $actions["AGENDA"]["update_decision"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=update",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );


}
  
///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();

?>
