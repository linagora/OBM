<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm.php                                                      //
//     - Desc : OBM Home Page (Login / Logout)                               //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// $module and $action defined for ActiveUser stats
$module = "obm";
if ($action == "") { $action = "home"; }
$path = ".";
$extra_css = "portal.css";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
include_once("obm_query.inc");
require("$obminclude/lib/right.inc");

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
  $uid = $auth->auth["uid"];
}

page_close();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
// If home page has a redirection
if ($c_home_redirect != "") {
  header("Status: 301 OK");
  header("Location: $c_home_redirect");
  exit();
 }

$display["head"] = display_head("OBM Version $obm_version");
$display["header"] = display_menu("");
$display["title"] = "
<div class=\"title\">
<b>OBM</b> version $obm_version - " . date("Y-m-d H:i:s") . "
</div>";

if ($cgp_show["module"]["agenda"] && $perm->check_right("agenda", $cright_read)) { 
  require("$path/agenda/agenda_query.inc");
  $block .= dis_calendar_portal();
}

if ($cgp_show["module"]["time"] && $perm->check_right("time", $cright_read)) { 
  $block .= dis_time_portal();
}

if ($cgp_show["module"]["deal"] && $perm->check_right("deal", $cright_read)) { 
  require_once("$path/deal/deal_query.inc");
  $block .= dis_deal_portal();
}

if ($cgp_show["module"]["project"] && $perm->check_right("project", $cright_read)) { 
  $block .= dis_project_portal();
}

if ($cgp_show["module"]["incident"] && $perm->check_right("incident", $cright_read)) { 
  $block .= dis_incident_portal();
}

if ($cgp_show["module"]["contract"] && $perm->check_right("contract", $cright_read)) { 
  $block .= dis_contract_portal();
}

if ($cgp_show["module"]["invoice"] && $perm->check_right("invoice", $cright_read)) { 
  require_once("$path/invoice/invoice_query.inc");
  $block .= dis_invoice_portal();
}

$display["detail"] = "
<div class=\"portal\">
$block
<p style=\"clear:both;\"/>  
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
  global $l_module_agenda,$l_daysofweekfirst,$l_my_agenda,$l_waiting_events;
  global $auth, $cagenda_weekstart;

  $obm_q = run_query_agenda_waiting_events() ;
  $num = $obm_q->num_rows();

  $ts_date = time();  
  $this_month = get_month($ts_date);
  $this_year = get_year($ts_date);
  $start_time = get_agenda_date_day_of_week(strtotime("$this_year-$this_month-01"), $cagenda_weekstart);
  $end_time = strtotime("+1 month +6 days", $start_time);

  $current_time = $start_time; 
  $calendar_entity["user"] = array($auth->auth["uid"] => array("dummy"));
  $events_list = agenda_events_model($start_time,$end_time, $calendar_entity);
  $whole_month = TRUE;
  $num_of_events = 0;
  $i = 0;
  do {
    $day = date ("j", $current_time);
    $iso_day = isodate_format($current_time);
    $check_month = get_month($current_time);
    if ($check_month != $this_month) {
      $day = "<a class=\"agendaLink2\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$iso_day)."\">$day</a>";
    } else {
      if (isset($events_list[$iso_day]) && $dayObj = $events_list[$iso_day]) {
	$events_data = $dayObj->get_events($id);
        $day = "<a class=\"agendaLink3\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$iso_day)."\">$day</a>";
      } else {
        $day = "<a class=\"agendaLink\" href=\"".url_prepare("agenda/agenda_index.php?action=view_day&amp;param_date=".$iso_day)."\">$day</a>";
      }
    }
    if ($i == 0) {
      $dis_minical .= "<tr>\n";
    }
    $dis_minical .= "<td class=\"agendaCell\">$day</td>\n";
    $current_time = strtotime("+1 day", $current_time); 
    $i++;
    if ($i == 7) { 
      $dis_minical .= "</tr>\n";
      $i = 0;
      $checkagain = get_month($current_time);
      if ($checkagain != $this_month) $whole_month = FALSE;	
    }
  } while ($whole_month == TRUE);

  
  // Minicalendar Head
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $current_time);
    $day = $l_daysofweekfirst[$day_num];
    $dis_minical_head .= "<td class=\"agendaHead\">$day</td>\n";
    $current_time = strtotime("+1 day", $current_time); 
  } 
  $block = "
   <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_agenda_portal\" alt=\"\" />
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
   <div class=\"portalLink\"><a href=\"".url_prepare("agenda/agenda_index.php")."\">$l_my_agenda</a></div>
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Time Management specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_time_portal() {
  global $ico_time_portal, $set_theme;
  global $l_module_time, $l_my_time, $l_unfilled;

  $num = run_query_days_unfilled();
  $block = "
  <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_time_portal\" alt=\"\" />
   </div>
   <div class=\"portalTitle\">$l_module_time</div>
   <div class=\"portalContent\">
    <div class=\"timeWarn\">
    $num $l_unfilled
    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("time/time_index.php")."\">$l_my_time</a></div>
  </div>
  ";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Deal specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_deal_portal() {
  global $uid, $ico_deal_portal, $set_theme;
  global $l_deal_total, $l_module_deal, $l_my_deal, $l_my_deal_current, $l_deal_balanced;

  $potential = run_query_deal_potential(array($uid));
  $m_amount = number_format($potential["market"]["$uid"]["amount"]);
  $m_balanced = number_format($potential["market"]["$uid"]["amount_balanced"]);
  $m_nb_potential = $potential["market"]["$uid"]["number"];
  if ($m_nb_potential == "") {
    $m_nb_potential = "0";
  }
  $t_nb_potential = $potential["tech"]["$uid"]["number"];
  if ($t_nb_potential == "") {
    $t_nb_potential = "0";
  }

  $deals = run_query_deal_status($uid);
  if (count($deals) > 0) {
    foreach ($deals as $status => $nb) {
      $dis_status .= "
    <tr>
      <td>$status</td>
      <td class=\"number\">$nb</td>
    </tr>";
    }
  }

  $block = "
  <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal_portal\" alt=\"\" />
   </div>
   <div class=\"portalTitle\">$l_module_deal</div>
   <div class=\"portalContent\">
    <div>
    <table>
    <tr>
      <td><a href=\"".url_prepare("deal/deal_index.php?action=stats&amp;sel_manager=$uid")."\">$l_my_deal_current</a></td>
      <td class=\"number\">$m_nb_potential / $t_nb_potential</td>
    </tr><tr>
      <td>$l_deal_total</td>
      <td class=\"number\">&nbsp; $m_amount</td>
    </tr><tr>
      <td>$l_deal_balanced</td>
      <td class=\"number\">$m_balanced</td>
    </tr>
    <tr><td>&nbsp;</td><td></td></tr>
    $dis_status
    </table>

    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("deal/deal_index.php?action=search&amp;sel_manager=$uid")."\">$l_my_deal</a></div>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Project specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_project_portal() {
  global $uid, $ico_project_portal, $set_theme;
  global $l_total, $l_module_project, $l_project_manager, $l_member;
  global $l_my_project, $l_my_project_current;

  $proj = run_query_project_memberstatus($uid);
  if (count($proj) > 0) {
    $dis_project .= "
      <tr>
        <td><a href=\"".url_prepare("project/project_index.php?action=search&amp;sel_manager=$uid")."\">$l_project_manager</a></td>
        <td class=\"number\">$proj[1]</td>
      </tr>
      <tr>
        <td><a href=\"".url_prepare("project/project_index.php?action=search&amp;sel_member=$uid")."\">$l_member</a></td>
        <td class=\"number\">$proj[0]</td>
      </tr>";
  }

  $block = "
  <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project_portal\" alt=\"\" />
   </div>
   <div class=\"portalTitle\">$l_module_project</div>
   <div class=\"portalContent\">
    <div>
    <table>
    <tr>
      <td>$l_my_project_current &nbsp;</td>
      <td class=\"number\">$proj[total]</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td></td>
    </tr>
    $dis_project
    </table>

    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("project/project_index.php?action=search&amp;sel_member=$uid")."\">$l_my_project</a></div>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Incident specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_incident_portal() {
  global $uid, $ico_incident_portal, $set_theme;
  global $l_total, $l_module_incident, $l_my_incident, $l_my_incident_current;

  $incs = run_query_incident_status($uid);
  if (count($incs) > 0) {
    foreach ($incs as $status => $nb) {
      if ($status != "0") {
        $dis_status .= "
      <tr>
        <td>$status</td>
        <td class=\"number\">$nb</td>
      </tr>";
      }
    }
  }

  $block = "
  <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_incident_portal\" alt=\"\" />
   </div>
   <div class=\"portalTitle\">$l_module_incident</div>
   <div class=\"portalContent\">
    <div>
    <table>
    <tr>
      <td>$l_my_incident_current &nbsp;</td>
      <td class=\"number\">$incs[0]</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td></td>
    </tr>
    $dis_status
    </table>

    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("incident/incident_index.php?action=search&amp;sel_owner=$uid")."\">$l_my_incident</a></div>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Contract specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_contract_portal() {
  global $uid, $ico_contract_portal, $set_theme;
  global $l_total, $l_module_contract, $l_my_contract, $l_my_contract_current;
  global $l_cr_date;
  global $cr_date_tosign, $cr_date_tobegin, $cr_date_current, $cr_date_torenew, $cr_date_ended;

  $conts = run_query_contract_range($uid);
  if (count($conts) > 0) {
    foreach ($conts as $range => $nb) {
      $dis_range .= "
    <tr>
      <td>$l_cr_date[$range]</td>
      <td class=\"number\">$nb</td>
    </tr>";
    }
  }

  $total = array_sum($conts);
  $block = "
  <div class=\"portalModule\"> 
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract_portal\" alt=\"\" />
   </div>
   <div class=\"portalTitle\">$l_module_contract</div>
   <div class=\"portalContent\">
    <div>
    <table>
    <tr>
      <td>$l_my_contract_current &nbsp;</td>
      <td class=\"number\">$total</td>
    </tr>
    <tr><td>&nbsp;</td><td></td></tr>
    $dis_range
    </table>

    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("contract/contract_index.php?action=search&amp;sel_manager=$uid")."\">$l_my_contract</a></div>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Invoice specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_portal() {
  global $uid, $ico_invoice_portal, $set_theme;
  global $l_total, $l_module_invoice, $l_billed, $l_order_no_bill;
  global $l_header_dashboard;

  $year = date("Y");
  $month = date("m");
  $year_month = "$year-$month";
  $month_end = date("Y-m-d", mktime(0,0,0,$month+1, 0, $year));
  $date_ranges = array(array("$year-$month-01", "$month_end"));

  $inv = run_query_invoice_amounts($date_ranges);

  // Invoice created
  if (is_array($inv["$year_month"]["billed"])) {
    foreach ($inv["$year_month"]["billed"] as $status => $status_info) {
      if ($status != "total") {
	$label = $status_info["label"];
	$amount_ht = $status_info["amount_ht"];
	$nb = $status_info["nb"];
	$dis_created .= "
      <tr>
        <td>$label</td>
        <td class=\"number\">$amount_ht / </td>
        <td class=\"number\">$nb</td>
      </tr>";
      }
    }
  }

  // Invoice potential
  if (is_array($inv["$year_month"]["potential"])) {
    foreach ($inv["$year_month"]["potential"] as $status => $status_info) {
      if ($status != "total") {
	$label = $status_info["label"];
	$amount_ht = $status_info["amount_ht"];
	$nb = $status_info["nb"];
	$dis_potential .= "
      <tr>
        <td>$label</td>
        <td class=\"number\">$amount_ht / </td>
        <td class=\"number\">$nb</td>
      </tr>";
      }
    }
  }

  $block = "
  <div class=\"portalModule\">
   <div class=\"portalModuleLeft\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_invoice_portal\" alt=\"\" />
   </div>
   <div class=\"portalTitle\">$l_module_invoice</div>
   <div class=\"portalContent\">
    <div>
    <table>
    <tr>
      <td>$l_billed &nbsp;</td>
      <td class=\"number\">".$inv["$year_month"]["billed"]["total"]["amount_ht"]. " / </td>
      <td class=\"number\">".$inv["$year_month"]["billed"]["total"]["nb"]."</td>
    </tr>
    $dis_created
    <tr>
      <td>&nbsp;</td>
      <td></td>
    </tr>
    <tr>
      <td>$l_order_no_bill &nbsp;</td>
      <td class=\"number\">".$inv["$year_month"]["potential"]["total"]["amount_ht"]. " / </td>
      <td class=\"number\">".$inv["$year_month"]["potential"]["total"]["nb"]."</td>
    </tr>
    $dis_potential
    </table>

    </div>
   </div>
   <div class=\"portalLink\"><a href=\"".url_prepare("invoice/invoice_index.php?action=dashboard")."\">$l_header_dashboard</a></div>
  </div>";

  return $block;
}


</script>
