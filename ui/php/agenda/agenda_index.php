<?
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_index.php                                             //
//     - Desc : Agenda Index File                                            //
// created 2002-11-26 by Mehdi Rande                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- this week for this user.
///////////////////////////////////////////////////////////////////////////////


$www ="   <p class=\"messageInfo\">
    	<a href=\"http://validator.w3.org/check/referer\"><img
        src=\"http://www.w3.org/Icons/valid-xhtml10\"
        alt=\"Valid XHTML 1.0!\" height=\"31\" width=\"88\" /></a>
	<a href=\"http://jigsaw.w3.org/css-validator/\">
 	 <img style=\"border:0;width:88px;height:31px\"
       src=\"http://jigsaw.w3.org/css-validator/images/vcss\" 
       alt=\"Valid CSS!\" />
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


page_close();

if ($action == "") $action = "index"; 
$agenda = get_param_agenda();
get_agenda_action();
$perm->check();
///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_agenda);     // Head & Body
generate_menu($menu,$section);      // Menu
display_bookmarks();       // Links to last visited contact,and company
//////////////////////////////////////////////////////////////////////////////
if($action == "") $action = "index";

if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $p_user_array =  array($auth->auth["uid"]);
  $obm_q = run_query_week_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}
elseif ($action == "view_day") {
///////////////////////////////////////////////////////////////////////////////
  if(count($sel_user_id) != 0){
    $p_user_array =  $sel_user_id;
  }
  else {
    $p_user_array =  array($auth->auth["uid"]);
  }
  $obm_q = run_query_day_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_day_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}
elseif ($action == "view_week") {
///////////////////////////////////////////////////////////////////////////////
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
elseif ($action == "view_month") {
///////////////////////////////////////////////////////////////////////////////
var_dump($sel_user_id);
 if(count($sel_user_id) != 0){
    $p_user_array =  $sel_user_id;
  }
  else {
    $p_user_array =  array($auth->auth["uid"]);
  }
  $obm_q = run_query_month_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_month_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
}
elseif ($action == "view_year") {
///////////////////////////////////////////////////////////////////////////////
  if(count($sel_user_id) != 0){
    $p_user_array =  $sel_user_id;
  }
  else {
    $p_user_array =  array($auth->auth["uid"]);
  }
  $obm_q = run_query_year_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  $user_obm = run_query_userobm();  
  dis_year_planning($agenda,$obm_q,$user_q,$user_obm);
}
elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $user_obm = run_query_userobm();
  $cat_event = run_query_get_eventcategories();
  if(count($sel_user_id) != 0){
    $p_user_array =  $sel_user_id;
  }
  else {
    $p_user_array =  array($auth->auth["uid"]);
  }
  $user_obm = run_query_userobm();  
  dis_event_form($action, $agenda, NULL, $user_obm, $cat_event, $p_user_array);
}
elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if(check_data_form($agenda)){    
    $conflict = run_query_add_event($agenda,$sel_user_id);
    if(count($conflict) == 0) {
      $p_user_array =  array($auth->auth["uid"]);
      $obm_q = run_query_week_event_list($agenda,$p_user_array);
      $user_q = run_query_get_user_name($p_user_array);
      $user_obm = run_query_userobm();
      dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
    }
    else{
      //html_dis_conflict($agenda,$conflict);
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
  if(check_data_form($agenda)){    
    $conflict = run_query_add_event($agenda,$sel_user_id);
    $p_user_array =  array($auth->auth["uid"]);
    $obm_q = run_query_week_event_list($agenda,$p_user_array);
    $user_q = run_query_get_user_name($p_user_array);
    $user_obm = run_query_userobm();    
    dis_week_planning($agenda,$obm_q,$user_q,$user_obm,$p_user_array);
  }
  else {
    require("agenda_js.inc");
    display_warn_msg($l_invalid_data . " : " . $err_msg);
    $user_obm = run_query_userobm();
    $cat_event = run_query_get_eventcategories();
    dis_event_form($action, $agenda, NULL, $user_obm, $cat_event, $sel_user_id);
  }
}

///////////////////////////////////////////////////////////////////////////////
// Stores in $agenda hash, Agenda parameters transmited
// returns : $agenda hash with parameters set
///////////////////////////////////////////////////////////////////////////////

function get_param_agenda() {
  global $param_date,$param_event,$tf_title,$sel_category_id,$sel_priority,$ta_event_description;
  global $set_start_time, $set_stop_time,$tf_date_begin,$tf_time_begin,$tf_time_end,$tf_date_end,$sel_repeat_kind;
  global $cdg_param,$cb_repeatday_0,$cb_repeatday_1,$cb_repeatday_2,$cb_repeatday_3,$cb_repeatday_4,$cb_repeatday_5;
  global $cb_repeatday_6,$cb_repeatday_7,$tf_repeat_end,$cb_force,$cb_privacy,$cb_repeat_update;


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
  if (isset($tf_repeat_end)) $agenda["repeat_end"] = $tf_repeat_end;  
  if (isset($cb_repeat_update)) $agenda["repeat_update"] = 1;
  if (isset($tf_date_begin)) {
    ereg ("([0-9]{4}).([0-9]{2}).([0-9]{2})",$tf_date_begin , $day_array2);
    $agenda["date_begin"] .=  $day_array2[1].$day_array2[2].$day_array2[3];
    if (isset($tf_time_begin)) {
      $agenda["date_begin"] = $agenda["date_begin"].substr($tf_time_begin,0,2).substr($tf_time_begin,3,2);
    }
    else {
      $agenda["date_begin"] = date("YmdHi",strtotime("+$set_start_time hours", $agenda["date_begin"]));
    }
  }
  else {
    $agenda["date_begin"] = date("YmdHi",strtotime("+$set_start_time hours",strtotime(date("Ymd"))));
  }
  if (isset($tf_date_end)) {
    ereg ("([0-9]{4}).([0-9]{2}).([0-9]{2})",$tf_date_end , $day_array);
    $agenda["date_end"] .=  $day_array[1].$day_array[2].$day_array[3];
    if (isset($tf_time_end)) {
      $agenda["date_end"] =  $agenda["date_end"].substr($tf_time_end,0,2).substr($tf_time_end,3,2);
    }
    else {
      $agenda["date_end"] = date("YmdHi",strtotime("+$set_stop_time hours",$agenda["date_end"]));
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
  global $actions;
  global $l_header_find,$l_header_new_f,$l_header_modify,$l_header_delete;
  global $l_header_display,$l_header_day,$l_header_week,$l_header_year,$l_header_admin;
  global $l_header_month,$l_header_new_event,$param_event;
  global $agenda_read, $agenda_write, $agenda_admin_read, $agenda_admin_write;

  //Index

  $actions["AGENDA"]["index"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=index",
    'Right'    => $agenda_read,
    'Condition'=> array ('None') 
                                      );

//Approve

  $actions["AGENDA"]["approve"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=approve",
    'Right'    => $agenda_read,
    'Condition'=> array ('None') 
                                         );

//New   
 
  $actions["AGENDA"]["new"] = array (
    'Name'     => $l_header_new_event,
    'Url'      => "$path/agenda/agenda_index.php?action=new",
    'Right'    => $agenda_write,
    'Condition'=> array ('index','detailconsult','
                         view_month','view_week','view_day','view_year','view_month','insert') 
                                    );


//Insert

  $actions["AGENDA"]["insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=insert",
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

//Detail Update

  $actions["AGENDA"]["detailupdate"] = array (
    'Name'     => $l_header_modify,
    'Url'      => "$path/agenda/agenda_index.php?action=detailupdate&amp;param_event=".$param_event."",
    'Right'    => $agenda_write,
    'Condition'=> array ('detailconsult') 
                                     		 );

//Update

  $actions["AGENDA"]["update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=update",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );

//Cancel Update

  $actions["AGENDA"]["cancel_update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=cancel_update",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );

//Confirm Update

  $actions["AGENDA"]["confirm_update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=confirm_update",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                         );

//Delete

  $actions["AGENDA"]["delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/agenda/agenda_index.php?action=delete&amp;param_event=".$param_event."",
    'Right'    => $agenda_write,
    'Condition'=> array ('detailconsult') 
                                        );

//Delete All

  $actions["AGENDA"]["delete_all"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=delete_all",
    'Right'    => $agenda_write,
    'Condition'=> array ('None') 
                                        );

}
  
///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();

?>

