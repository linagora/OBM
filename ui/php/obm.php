<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm.php                                                      //
//     - Desc : OBM Home Page (Login / Logout)                               //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
/////////////////////////////////////////////////////////////////////////////
$module = "";
$path = ".";
$extra_css = "portal.css";
///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
include_once("obm_query.inc"); 


page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));

if ($action == "logout") {
  include("$obminclude/global_pref.inc");
  $display["head"] = display_head("OBM Version $obm_version");
  $display["end"] = display_end();
  $display["detail"] = dis_logout_detail();
  run_query_logout();
  $auth->logout();
  $sess->delete();
  $action = "";
  display_page($display);
  exit;

} else {
  include("$obminclude/global_pref.inc");
}

page_close();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head("OBM Version $obm_version");
$display["header"] = generate_menu("","");
$display["title"] = "
<div class=\"title\">
<b>OBM</b> version $obm_version - " . date("Y-m-d H:i:s") . "
</div>";
if ($cgp_show["module"]["agenda"]) { 
  $block .= dis_calendar_portal();
}

if ($cgp_show["module"]["time"]) { 
  $block .= dis_time_portal();
}

$display["detail"] = "
<div class=\"detail\">
 <div class=\"portal\">
 $block
 <p style=\"clear:both;\"/>  
 </div>

</div>";

$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Display detail of logout page                                             //
///////////////////////////////////////////////////////////////////////////////
function dis_logout_detail() {
  global $l_connection_end, $l_reconnect;

  $block = "
<table width=\"100%\">
<tr>
  <td width=\"20%\">
    <a href=\"http://www.aliacom.fr/\"><img align=\"middle\" border=\"0\" src=\"".C_IMAGE_PATH."/standard/standard.jpg\"></a>$obm_version</td>
  <td width=\"5%\">&nbsp;</td>
  <td width=\"50%\" align=\"center\">
    <h1>OBM : $l_connection_end</h1></td>
  <td width=\"25%\" align=\"center\">&nbsp;</td>
</tr>
<tr>
  <td align=\"center\">&nbsp;</td>
</tr>
<tr>
  <td align=\"center\" colspan=\"4\"><hr></td>
</tr>
</table>

<P>
<center>
<a href=\"obm.php\">OBM : $l_reconnect</a>
</center>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The calendar specific portal layer.                               //
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_portal() {
  global $ico_agenda_portal,$set_theme;
  global $l_module_agenda,$l_daysofweekfirst,$l_your_agenda,$l_waiting_events;
  global $auth,$set_weekstart_default;

  $num = run_query_waiting_events() ;

  $unix_time = time();  
 
  $first_of_month = date("Ym01",$unix_time);
  $next_month = date( "Ym01", strtotime("+1 month",  $unix_time));
  $start_month_day = dateOfWeek($first_of_month,$set_weekstart_default);  
  $start_time = strtotime($start_month_day);
  $end_time = strtotime($next_month);
  $calendar_user = array ($auth->auth["uid"] => "dummy"); 
  $events_list = events_model($start_time,$end_time,$calendar_user);
  $minical_month = date("m");
  $minical_year = date("Y");
  $start_day = strtotime(dateOfWeek($first_of_month, $set_weekstart_default));
  $whole_month = TRUE;
  $num_of_events = 0;
  $i = 0;
  do {
    $day = date ("j", $start_day);
    $daylink = date ("Ymd", $start_day);
    $check_month = date ("m", $start_day);
    if ($check_month != $minical_month) 
      $day= "<a class=\"agendaLink2\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
    else {
      if(isset($events_list[$daylink]) && $dayObj = $events_list[$daylink]) { 
	$events_data = $dayObj->get_events($id);
	  $day= "<a class=\"agendaLink3\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
	} else {
	  $day= "<a class=\"agendaLink\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
	}
    }
    if ($i == 0) $dis_minical .=  "<tr>\n";
    $dis_minical .= "<td class=\"agendaCell\">\n";
    $dis_minical .=  "$day\n";
    $dis_minical .=  "</td>\n";
    $start_day = strtotime("+1 day", $start_day); 
    $i++;
    if ($i == 7) { 
      $dis_minical .=  "</tr>\n";
      $i = 0;
      $checkagain = date ("m", $start_day);
      if ($checkagain != $minical_month) $whole_month = FALSE;	
    }
  } while ($whole_month == TRUE);

  
 // Minicalendar Head

  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_day);
    $day = $l_daysofweekfirst[$day_num];
    $dis_minical_head .= "<td class=\"agendaHead\">$day</td>\n";
    $start_day = strtotime("+1 day", $start_day); 
  } 
  $block = "
    <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_agenda_portal\" />
   </div>
   <div class=\"portalTitle\">$l_module_agenda</div>
   <div class=\"portalContent\">
    <table class=\"agendaCalendar\" >
     <tr>
      $dis_minical_head
     </tr>
     $dis_minical
    </table>  
    $num $l_waiting_events
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("agenda/agenda_index.php")."\">$l_your_agenda</a></div>
  </div>
  ";
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display The Time Managemebt specific portal layer.                       //
///////////////////////////////////////////////////////////////////////////////
function dis_time_portal() {
  global $ico_time_portal,$set_theme;
  global $l_module_time,$l_your_time, $l_unfilled;

  $num = run_query_days_unfilled();
  $block = "
  <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_time_portal\" />
   </div>
   <div class=\"portalTitle\">$l_module_time</div>
   <div class=\"portalContent\">
    <div class=\"timeWarn\">
    $num $l_unfilled
    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("time/time_index.php")."\">$l_your_time</a></div>
  </div>
  ";
  return $block;
}

</script>
