<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm.php                                                      //
//     - Desc : OBM Home Page (Login / Logout)                               //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
$section = "";
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
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));

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

  $block = "
<table width=\"100%\">
<tr>
  <td width=\"20%\">
    <a href=\"http://www.aliacom.fr/\"><img align=\"middle\" border=\"0\" src=\"/images/standard/standard.jpg\"></a>$obm_version</td>
  <td width=\"5%\">&nbsp;</td>
  <td width=\"50%\" align=\"center\">
    <h1>OBM CONNECTION CLOSED</h1></td>
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
<a href=\"obm.php\">click here to Login</a>
</center>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The calendar specific portal layer.                               //
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_portal() {
  global $ico_agenda_portal,$set_theme;
  global $l_header_agenda,$l_daysofweekreallyshort,$l_youre_agenda,$l_waiting_events;
  global $auth,$set_weekstart_default;

  $agenda_q = run_query_month_event_list() ;
  $num = run_query_waiting_events() ;

  $events_list = store_daily_events($agenda_q);
  $minical_month = date("m");
  $minical_year = date("Y");
  $first_of_month = $minical_year.$minical_month."01";
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
    else
      if(is_array($events_list[$daylink])) {  
	$day= "<a class=\"agendaLink3\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
      } else {
	$day= "<a class=\"agendaLink\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
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
    $day = $l_daysofweekreallyshort[$day_num];
    $dis_minical_head .= "<td class=\"agendaHead\">$day</td>\n";
    $start_day = strtotime("+1 day", $start_day); 
  } 
  $block = "
    <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"/images/$set_theme/$ico_agenda_portal\" />
   </div>
   <div class=\"portalTitle\">$l_header_agenda</div>
   <div class=\"portalContent\">
    <table class=\"agendaCalendar\" >
     <tr>
      $dis_minical_head
     </tr>
     $dis_minical
    </table>  
    $num $l_waiting_events
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("agenda/agenda_index.php")."\">$l_youre_agenda</a></div>
  </div>
  ";
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display The Time Managemebt specific portal layer.                       //
///////////////////////////////////////////////////////////////////////////////
function dis_time_portal() {
  global $ico_time_portal,$set_theme;
  global $l_header_time,$l_youre_time, $l_unfilled;
  global $auth;

  $num = run_query_days_unfilled();
  $block = "
  <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"/images/$set_theme/$ico_time_portal\" />
   </div>
   <div class=\"portalTitle\">$l_header_time</div>
   <div class=\"portalContent\">
    <div class=\"timeWarn\">
    $num $l_unfilled
    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("time/time_index.php")."\">$l_youre_time</a></div>
  </div>
  ";
  return $block;
}

</script>
