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

include("agenda_functions.inc");
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
  $p_user_array = array($auth->auth["uid"],6);
  $obm_q = run_query_week_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  dis_week_planning($agenda,$obm_q,$user_q);
}
elseif ($action == "view_day") {
///////////////////////////////////////////////////////////////////////////////
  $p_user_array = array($auth->auth["uid"],6);
  $obm_q = run_query_day_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  dis_day_planning($agenda,$obm_q,$user_q);
}
elseif ($action == "view_week") {
///////////////////////////////////////////////////////////////////////////////
  $p_user_array = array($auth->auth["uid"],6);
  $obm_q = run_query_week_event_list($agenda,$p_user_array);
  $user_q = run_query_get_user_name($p_user_array);
  dis_week_planning($agenda,$obm_q,$user_q);
}
elseif ($action == "view_month") {
///////////////////////////////////////////////////////////////////////////////

  dis_month_planning($agenda);
}
elseif ($action == "view_year") {
///////////////////////////////////////////////////////////////////////////////

  dis_year_planning($agenda);
}




///////////////////////////////////////////////////////////////////////////////
// Stores in $agenda hash, Agenda parameters transmited
// returns : $agenda hash with parameters set
///////////////////////////////////////////////////////////////////////////////

function get_param_agenda() {
  global $param_date;

  global $cdg_param;


  // Deal fields
  if (isset ($param_date)) $agenda["date"] = $param_date; 
  else $agenda["date"] = date("Ymd",time());

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
                         view_month','view_week','view_day','view_year') 
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

