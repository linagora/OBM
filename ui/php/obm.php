<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/


?>
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
include_once("$obminclude/global.inc");
$params = get_obm_params();
include_once('obm_query.inc');
require_once("$obminclude/of/of_right.inc");

$OBM_Session = $params['OBM_Session'];
if ($action == '') { $action = 'home'; }
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));

if ($action == 'logout') {
  include_once("$obminclude/global_pref.inc");

  run_query_logout();
  if($auth_kind == "CAS" || strcasecmp($auth_kind,"LemonLDAP") == 0) {
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
} elseif($action == 'ext_get_entities') {

  $entity = strtolower($params['entity']);
  $entities = searchWritablesEntities($entity, $obm['uid'], $params['pattern'], true);
  $users = array();
  foreach($entities as $id => $data) {
    $label = $data['label'];
    $extra = $data['extra'];
    if (OBM_Acl::isSpecialEntity($entity))
      $extra = phpStringToJsString(get_entity_email($extra));
    $users[] = "{id:'$id', label:'$label', extra:'{$extra}'}";
  }
  $display['json'] = "{length:".count($entities).", datas:[".implode(',',$users)."]}";  
  echo $display['json'];
  exit();
} else {
  include_once("$obminclude/global_pref.inc");
  $uid = $obm['uid'];
}

$extra_css[] = $css_portal;

page_close();
///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
// If home page has a redirection
if ($c_home_redirect != '' && !$params['error'] && $_GET['redirect'] != 'false' && !is_global_admin()) {
  header('Status: 301 OK');
  header("Location: $c_home_redirect" . (count($_GET) > 0 ? "?" . http_build_query($_GET) : ""));
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
$date = new Of_Date();
$display['title'] = "
<h1 class=\"title\">$l_obm_title version $obm_version - ".$date->getOutputDate()."</h1>";

if ($cgp_show['module']['calendar'] && $perm->check_right('calendar', $cright_read)) { 
  require_once("$path/calendar/calendar_query.inc");
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

$block .= dis_portlets_menu();

$display['result'] = "
$block
<p style=\"clear:both;\"></p>";

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
  if (function_exists("horde_logout")) {
    horde_logout() ;
  }
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
  global $ico_big_calendar,$path,$ccalendar_first_hour;
  global $l_module_calendar,$l_daysofweekfirst,$l_my_calendar,$l_waiting_events;
  global $obm, $ccalendar_weekstart, $day_duration, $modules;

  $obm_q = run_query_calendar_waiting_events() ;
  $num = $obm_q->num_rows();

  $date = new Of_Date();
  $start = clone $date;
  $start->setDay(1)->setWeekday($ccalendar_weekstart)->setHour($ccalendar_first_hour)->setMinute(0)->setSecond(0);
  $end = clone $start;
  $end->addMonth(1)->setWeekday($ccalendar_weekstart)->addWeek(1)->setHour(0)->setMinute(0)->setSecond(0);
  $calendar_entity['user'] = array($obm['uid'] => array('dummy'));
  calendar_events_model($start,$end, $calendar_entity);
  $of = &OccurrenceFactory::getInstance();

  // Minicalendar
  // Minicalendar
  $current = clone $start;
  $current->setHour(0)->setMinute(0)->setSecond(0);
  while($current->compare($end) < 0) {
    if ($current->compareWeekday($ccalendar_weekstart) == 0) $dis_minical .= "<tr>\n";
    $day = $current->get(Of_Date::DAY);
    $iso = $current->getURL();
    $have_occurrence = $of->periodHaveOccurrences($current);
    if ($have_occurrence) {
      $klass = 'hyperlight';
    } else {
      $klass = '';
    }    
    if ($current->isToday()) {
      $klass .= ' highlight';
    } elseif ($current->compareMonth($date)!= 0) {
      $klass .= ' downlight';
    }
    $dis_minical .= "<td class=\"$klass\" onclick=\"window.location.href='$path/calendar/calendar_index.php?cal_range=day&amp;date=$iso'\"
      onmouseout=\"this.className='$klass'\" onmouseover=\"this.className='hover'\">
      $day
      </td>";
    $current->addDay(1);
    if ($current->compareWeekday($ccalendar_weekstart) == 0) $dis_minical .= "</tr>\n";
  } 

  $current = clone $start;
  $current->setWeekday($ccalendar_weekstart);
  $end = clone $current;
  $end->addDay(6);
  // Minicalendar Head
  while($current->compare($end) <= 0) {
    $day = $current->localize(Of_Date::WEEKDAY_NARROW);
    $dis_minical_head .= "<td>$day</td>\n";
    $current->addDay(1);
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
  global $l_section_my, $l_module_settings, $cright_write;

  $my = array (
    'settings'    => array("$path/settings/settings_index.php",       $GLOBALS['l_module_settings'],    $cright_read),
    'password'    => array("$path/password/password_index.php",       $GLOBALS['l_module_password'],    $cright_read),
    'mailforward' => array("$path/mailforward/mailforward_index.php", $GLOBALS['l_module_mailforward'], $cright_read),
    'vacation'    => array("$path/vacation/vacation_index.php",       $GLOBALS['l_module_vacation'],    $cright_read),
    'mailbox'     => array("$path/mailbox/mailbox_index.php",         $GLOBALS['l_module_mailbox'],     $cright_read),
    'calendar'    => array("$path/calendar/calendar_index.php?action=rights_admin", $GLOBALS['l_my_calendar_share'], $cright_write)
  );

  foreach ($my as $mod => $params) {
    list($url, $title, $perms) = $params;
    if ($cgp_show['module']["$mod"] && $perm->check_right("$mod", $perms)) {
      $dis_my .= "\n<a class=\"link\" href=\"$url\">$title</a><br />";
    }
  }

  $block = "
  <div class=\"summaryBox\"> 
  <h1><img src=\"$ico_big_settings\" alt=\"$l_section_my\" />$l_section_my</h1>
  $dis_my
  </div>";

  return $block;
}

function dis_portlets_menu() {
  global $uid, $ico_big_settings, $path, $cgp_show, $perm, $cright_read;
  global $l_module_settings, $cright_write, $sections;

  //var_dump($sections);

  foreach($cgp_show['section'] as $section => $value){
    //var_dump($value['url']);
    $portlet_need = array('admin', 'user');
    if( in_array($section, $portlet_need) && ( isset($value['url']) || $value['url'] != "./admin_ref/admin_ref_index.php?mode=html")){
      $portlet_title = "l_section_".$section;
      $block .= "
       <div class=\"summaryBox\"> 
      <h1>".$sections[$section]['Name']."</h1>
      ";
      $block .= display_modules($section, null, true);
      $block.= "</div>";
    }
  }

  return $block;
}
</script>
