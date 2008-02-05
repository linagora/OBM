<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm.php                                                      //
//     - Desc : OBM Home Page (Login / Logout)                               //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// $module and $action defined for ActiveUser stats
$module = 'obm';
$path = '.';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_obm_params();
include_once('obm_query.inc');
require("$obminclude/of/of_right.inc");

$OBM_Session = $params['OBM_Session'];
if ($action == '') { $action = 'home'; }
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));

if ($action == 'logout') {
  include("$obminclude/global_pref.inc");
  run_query_logout();
  if($auth_kind == "CAS") {
    $auth->logout();
  }
  $sess->delete();
  $display["head"] = display_head("OBM Version $obm_version");
  $display["end"] = display_end();
  $display["detail"] = dis_logout_detail();
  $_SESSION['obm'] = '';
  $sess->delete();
  $action = '';
  display_page($display);
  exit;
} else {
  include("$obminclude/global_pref.inc");
  $uid = $obm['uid'];
}

$extra_css[] = $css_portal;

page_close();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
// If home page has a redirection
if ($c_home_redirect != '' && !$params['error']) {
  header('Status: 301 OK');
  header("Location: $c_home_redirect");
  exit();
}

$display['head'] = display_head("$l_obm_title Version $obm_version");
$display['header'] = display_menu('');
switch($params['error']) {
  case 'perms' :
    $error_msg = $l_permission_denied;
    break;
  default: 
    $error_msg = '';
}
if($error_msg) {
  $display['msg'] = display_err_msg($error_msg);
}

$display['title'] = "
<h1 class=\"title\">
$l_obm_title version $obm_version - " . date('Y-m-d H:i:s') . "
</h1>";

if ($cgp_show['module']['calendar'] && $perm->check_right('calendar', $cright_read)) { 
  require("$path/calendar/calendar_query.inc");
  $block .= dis_calendar_portal();
}

if ($cgp_show['module']['time'] && $perm->check_right('time', $cright_write)) { 
  $block .= dis_time_portal();
}

if ($cgp_show['module']['lead'] && $perm->check_right('lead', $cright_write)) { 
  require_once("$path/lead/lead_query.inc");
  $block .= dis_lead_portal();
}

if ($cgp_show['module']['deal'] && $perm->check_right('deal', $cright_write)) { 
  require_once("$path/deal/deal_query.inc");
  $block .= dis_deal_portal();
}

if ($cgp_show['module']['project'] && $perm->check_right('project', $cright_read)) { 
  $block .= dis_project_portal();
}

if ($cgp_show['module']['incident'] && $perm->check_right('incident', $cright_write)) { 
  $block .= dis_incident_portal();
}

if ($cgp_show['module']['contract'] && $perm->check_right('contract', $cright_write)) { 
  $block .= dis_contract_portal();
}

if ($cgp_show['module']['invoice'] && $perm->check_right('invoice', $cright_read_admin)) { 
  require_once("$path/invoice/invoice_query.inc");
  $block .= dis_invoice_portal();
}

if ($cgp_show['module']['settings'] && $perm->check_right('settings', $cright_read)) { 
  $block .= dis_my_portal();
}

$display['result'] = "
$block
<p style=\"clear:both;\"/>  
";

$display['end'] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_obm_params() {

  // Get global params
  $params = get_global_params('Obm');
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Display detail of logout page
///////////////////////////////////////////////////////////////////////////////
function dis_logout_detail() {
  global $l_connection_end, $l_reconnect, $l_obm_title, $obm_version, $path, $cgp_host;
  header("location: $path/obm.php");
  $block = "
<table width=\"100%\">
<tr>
  <td width=\"20%\">
    <a href=\"http://www.aliacom.fr/\">$obm_version</a></td>
  <td width=\"5%\">&nbsp;</td>
  <td width=\"50%\" align=\"center\">
    <h1>$l_obm_title : $l_connection_end</h1></td>
  <td width=\"25%\" align=\"center\">&nbsp;</td>
</tr>
<tr>
  <td align=\"center\">&nbsp;</td>
</tr>
<tr>
  <td align=\"center\" colspan=\"4\"><hr /></td>
</tr>
</table>

<P>
<center>
<a href=\"obm.php\">$l_obm_title : $l_reconnect</a>
</center>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The calendar specific portal layer.                               //
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_portal() {
  global $ico_big_calendar,$path;
  global $l_module_calendar,$l_daysofweekfirst,$l_my_calendar,$l_waiting_events;
  global $obm, $ccalendar_weekstart, $day_duration, $modules;

  $obm_q = run_query_calendar_waiting_events() ;
  $num = $obm_q->num_rows();

  $ts_date = time();  
  $this_month = of_date_get_month($ts_date);
  $this_year = of_date_get_year($ts_date);
  $start_time = get_calendar_date_day_of_week(strtotime("$this_year-$this_month-01"), $ccalendar_weekstart);
  $end_time = strtotime('+1 month +6 days', $start_time);

  $current_time = $start_time; 
  $calendar_entity['user'] = array($obm['uid'] => array('dummy'));
  calendar_events_model($start_time,$end_time, $calendar_entity);
  $of = &OccurrenceFactory::getInstance();
  $whole_month = TRUE;
  $num_of_events = 0;

  // Minicalendar
  $i = 0;
  do {
    if ($i == 0) $dis_minical .= '<tr>';
    $day = date ('j', $current_time);
    $iso_day = of_isodate_format($current_time);
    $check_month = of_date_get_month($current_time);
    $have_occurrence = $of->periodHaveOccurrences(strtotime($iso_day), $day_duration);
    if ($have_occurrence) {
      $klass = 'hyperlight';
    } else {
      $klass = '';
    }
    if ($check_month != $this_month) {
      $dis_minical .= "<td class=\"downlight $klass\" onclick=\"window.location.href='$path/calendar/calendar_index.php?cal_range=day&amp;date=$iso_day'\"
        onmouseout=\"this.className='downlight $klass'\" onmouseover=\"this.className='hover'\">
        $day
        </td>";
    } else {
      if (of_isodate_format() == $iso_day) {
        $dis_minical .= "<td class=\"highlight $klass\" onclick=\"window.location.href='$path/calendar/calendar_index.php?cal_range=day&amp;date=$iso_day'\" 
          onmouseout=\"this.className='highlight $klass'\" onmouseover=\"this.className='hover'\">
          $day
          </td>";
      } else {
        $dis_minical .= "<td class=\"$klass\" onclick=\"window.location.href='$path/calendar/calendar_index.php?cal_range=day&amp;date=$iso_day'\" 
          onmouseout=\"this.className='$klass'\" onmouseover=\"this.className='hover'\">
          $day
          </td>";
      }
    }
    $current_time = strtotime('+1 day', $current_time);
    $i++;
    if ($i == 7) {
      $dis_minical .= "</tr>\n";
      $i = 0;
      $checkagain = of_date_get_month($current_time);
      if ($checkagain != $this_month) $whole_month = FALSE;
    }
  } while ($whole_month == TRUE);
  
  // Minicalendar Head
  for ($i=0; $i<7; $i++) {
    $day_num = date('w', $current_time);
    $day = $l_daysofweekfirst[$day_num];
    $dis_minical_head .= "<td class=\"calendarHead\">$day</td>\n";
    $current_time = strtotime('+1 day', $current_time); 
  } 
  $block = "
   <div class=\"summaryBox\"> 
   <h1><img src=\"$ico_big_calendar\" alt=\"Calendar\" />$l_module_calendar</h1>
   <table class=\"miniCalendar\" >
   <thead>
   <tr>$dis_minical_head</tr>
   </thead>
   <tbody>
   $dis_minical
   </tbody>
   </table>  
   $num $l_waiting_events
   <a class=\"link\" href=\"".$modules['calendar']['Url']."\">$l_my_calendar</a>
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Time Management specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_time_portal() {
  global $ico_big_time, $path;
  global $l_module_time, $l_my_time, $l_unfilled;

  $num = run_query_days_unfilled();
  if ( $num <= 1 ) {
    $class = 'info';
  } else {
    $class = 'error';
  }
  $block = "
  <div class=\"summaryBox\"> 
    <h1><img src=\"$ico_big_time\" alt=\"Time\" />$l_module_time</h1>
    <h2 class=\"$class\">$num $l_unfilled</h2>
   <a class=\"link\" href=\"$path/time/time_index.php\">$l_my_time</a>
  </div>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Lead specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_lead_portal() {
  global $uid, $ico_big_lead, $c_null,$path;
  global $l_module_lead, $l_my_lead, $l_days, $l_alarm, $l_late, $l_without;

  $today = date('Y-m-d');
  $ts_today = strtotime($today);
  $ts_7 = strtotime('-7 day', $ts_today);
  $ts_14 = strtotime('-14 day', $ts_today);
  $ts_30 = strtotime('-30 day', $ts_today);
  $ts_90 = strtotime('-90 day', $ts_today);
  $iso_7 = of_isodate_format($ts_7);
  $iso_14 = of_isodate_format($ts_14);
  $iso_30 = of_isodate_format($ts_30);
  $iso_90 = of_isodate_format($ts_90);
  $date_ranges = array(array("$iso_7", "$today"),
		       array("$iso_14", "$today"),
		       array("$iso_30", "$today"),
		       array("$iso_90", "$today"),
);

  $leads = run_query_lead_time_range($date_ranges, array($uid));
  $leads_date = $leads['date'];
  $block = "
  <div class=\"summaryBox\">
   <h1>
    <img src=\"$ico_big_lead\" alt=\"Lead\" />$l_module_lead
   </h1>
   <dl>
   <dt><a href=\"$path/lead/lead_index.php?action=search&amp;sel_manager=$uid\">$l_my_lead</a></dt>
   <dd>$leads[total]</dd>
   <dt>- 7 $l_days</dt>
   <dd>$leads_date[0]</dd>
   <dt>- 14 $l_days</dt>
   <dd>$leads_date[1]</dd>
   <dt>- 30 $l_days</dt>
   <dd>$leads_date[2]</dd>   
   <dt>- 90 $l_days</dt>
   <dd>$leads_date[3]</dd>   
   <dt>$l_alarm
   (<a href=\"$path/lead/lead_index.php?action=search&amp;sel_manager=$uid&amp;date_field=datealarm&amp;tf_date_before=$today\">$l_late</a> /
   <a href=\"$path/lead/lead_index.php?action=search&amp;sel_manager=$uid&amp;tf_date_field=datealarm&amp;tf_date_after=$c_null\">$l_without</a>)
   </dt>
   <dd>$leads[alarm] / $leads[no_alarm]</dd>
   </dl>
   <a class=\"link\" href=\"$path/lead/lead_index.php?action=search&amp;sel_manager=$uid\">$l_my_lead</a>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Deal specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_deal_portal() {
  global $uid, $ico_big_deal,$path, $l_my_deal_current;
  global $l_deal_total, $l_module_deal, $l_my_deal, $l_deal_balanced;

  $potential = run_query_deal_potential(array($uid));
  $m_amount = number_format($potential['market']["$uid"]['amount']);
  $m_balanced = number_format($potential['market']["$uid"]['amount_balanced']);
  $m_nb_potential = $potential['market']["$uid"]['number'];
  if ($m_nb_potential == '') {
    $m_nb_potential = '0';
  }
  $t_nb_potential = $potential['tech']["$uid"]['number'];
  if ($t_nb_potential == '') {
    $t_nb_potential = '0';
  }

  $deals = run_query_deal_status($uid);
  if (count($deals) > 0) {
    foreach ($deals as $status => $nb) {
      $dis_status .= "
      <dt>$status</dt>
      <dd>$nb</dd>";
    }
  }

  $block = "
  <div class=\"summaryBox\"> 
  <h1>
   <img src=\"$ico_big_deal\" alt=\"Deal\" />$l_module_deal
  </h1>
  <dl>
  <dt><a href=\"$path/deal/deal_index.php?action=dashboard&amp;sel_manager=$uid\">$l_my_deal_current</a></dt>
  <dd>$m_nb_potential / $t_nb_potential</dd>
  <dt>$l_deal_total</dt>
  <dd>$m_amount</dd>
  <dt>$l_deal_balanced</dt>
  <dd>$m_balanced</dd>
  </dl>
  <a class=\"link\" href=\"$path/deal/deal_index.php?action=search&amp;sel_manager=$uid\">$l_my_deal</a>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Project specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_project_portal() {
  global $uid, $ico_big_project, $path;
  global $l_total, $l_module_project, $l_project_manager, $l_member;
  global $l_my_project, $l_my_project_current;

  $proj = run_query_project_memberstatus($uid);
  if (count($proj) > 0) {
    $dis_project .= "
    <dt><a href=\"$path/project/project_index.php?action=search&amp;sel_manager=$uid\">$l_project_manager</a></dt>
    <dd>$proj[1]</dd>
    <dt><a href=\"$path/project/project_index.php?action=search&amp;sel_member=$uid\">$l_member</a></dt>
    <dd>$proj[0]</dd>";
  }

  $block = "
  <div class=\"summaryBox\"> 
   <h1><img src=\"$ico_big_project\" alt=\"Project\" />$l_module_project</h1>
   <dl>
   <dt>$l_my_project_current</dt>
   <dd>$proj[total]</dd>
    $dis_project
   </dl>
   <a class=\"link\" href=\"$path/project/project_index.php?action=search&amp;sel_member=$uid\">$l_my_project</a>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Incident specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_incident_portal() {
  global $uid, $ico_big_incident, $path;
  global $l_total, $l_module_incident, $l_my_incident, $l_my_incident_current;

  $incs = run_query_incident_status($uid);
  if (count($incs) > 0) {
    foreach ($incs as $status => $nb) {
      if ($status != '0') {
        $dis_status .= "
        <dt>$status</dt>
        <dd>$nb</dd>";
      }
    }
  }

  $block = "
  <div class=\"summaryBox\"> 
    <h1>
    <img src=\"$ico_big_incident\" alt=\"Incident\" />$l_module_incident
    </h1>
    <dl>
    <dt>$l_my_incident_current</dt>
    <dd>$incs[0]</dd>
    $dis_status
    </dl>
   <a class=\"link\" href=\"$path/incident/incident_index.php?action=search&amp;sel_owner=$uid\">$l_my_incident</a>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Contract specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_contract_portal() {
  global $uid, $ico_big_contract ;
  global $l_total, $l_module_contract, $l_my_contract, $l_my_contract_current;
  global $l_cr_date, $cr_date_ended, $path;
  global $cr_date_tosign, $cr_date_tobegin, $cr_date_current, $cr_date_torenew;

  $conts = run_query_contract_range($uid);
  if (count($conts) > 0) {
    foreach ($conts as $range => $nb) {
      $dis_range .= "
      <dt>$l_cr_date[$range]</dt>
      <dd >$nb</dd>";
    }
  }

  $total = array_sum($conts);
  $block = "
  <div class=\"summaryBox\"> 
    <h1>
    <img src=\"$ico_big_contract\" alt=\"Contract\" />
    $l_module_contract
    </h1>
    <dl>
    <dt>$l_my_contract_current</dt>
    <dd>$total</dd>
    $dis_range
    </dl>
    <a class=\"link\" href=\"$path/contract/contract_index.php?action=search&amp;sel_manager=$uid\">$l_my_contract</a>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Invoice specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_portal() {
  global $uid, $ico_big_invoice;
  global $l_total, $l_module_invoice, $l_billed, $l_order_no_bill;
  global $l_header_dashboard, $path;

  $year = date('Y');
  $month = date('m');
  $year_month = "$year-$month";
  $month_end = date('Y-m-d', mktime(0,0,0,$month+1, 0, $year));
  $date_ranges = array(array("$year-$month-01", "$month_end"));

  $inv = run_query_invoice_amounts($date_ranges);

  // Invoice created
  if (is_array($inv["$year_month"]['billed'])) {
    foreach ($inv["$year_month"]['billed'] as $status => $status_info) {
      if ($status != 'total') {
	$label = $status_info['label'];
	$amount_ht = $status_info['amount_ht'];
	$nb = $status_info['nb'];
	$dis_created .= "
        <dt>$label</dt>
        <dd>$amount_ht / $nb</dd>";
      }
    }
  }

  // Invoice potential
  if (is_array($inv["$year_month"]['potential'])) {
    foreach ($inv["$year_month"]['potential'] as $status => $status_info) {
      if ($status != 'total') {
	$label = $status_info['label'];
	$amount_ht = $status_info['amount_ht'];
	$nb = $status_info['nb'];
	$dis_potential .= "          
        <dt>$label</dt>
        <dd>$amount_ht / $nb</dd>";
      }
    }
  }

  $block = "
  <div class=\"summaryBox\">
   <h1><img src=\"$ico_big_invoice\" alt=\"Invoice\" />
   $l_module_invoice</h1>
   <dl>
   <dt>$l_billed</dt>
   <dd>".$inv["$year_month"]['billed']['total']['amount_ht']. ' / '.$inv["$year_month"]['billed']['total']['nb']."</dd>
    $dis_created
    <dt>$l_order_no_bill</dt>
    <dd>".$inv["$year_month"]['potential']['total']['amount_ht']. " / ".$inv["$year_month"]['potential']['total']['nb']."</dd>
    $dis_potential
    </dl>
   <a class=\"link\" href=\"$path/invoice/invoice_index.php?action=dashboard\">$l_header_dashboard</a>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The 'My' specific portal layer
///////////////////////////////////////////////////////////////////////////////
function dis_my_portal() {
  global $uid, $ico_big_settings, $path, $cgp_show, $perm, $cright_read;
  global $l_section_my, $l_module_settings;

  $my = array ('password', 'mailforward', 'vacation', 'mailbox');

  foreach ($my as $mod) {
    if ($cgp_show['module']["$mod"] && $perm->check_right("$mod", $cright_read)) { 
      $l_mod = "l_module_$mod";
      global $$l_mod;
      $dis_my .= "
   <a class=\"link\" href=\"$path/$mod/${mod}_index.php\">${$l_mod}</a><br />";
    }
  }

  $block = "
  <div class=\"summaryBox\"> 
  <h1><img src=\"$ico_big_settings\" alt=\"$l_section_my\" />$l_section_my</h1>
  <a class=\"link\" href=\"$path/settings/settings_index.php\">$l_module_settings</a><br />
  $dis_my
  </div>";

  return $block;
}


</script>
