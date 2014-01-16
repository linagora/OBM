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



///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_display.php                                             //
//     - Desc : Time Display File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
// Stat entries
$fieldnames['project_name'] = $l_project;
$fieldnames['tasktype_label'] = $l_tt;
$fieldnames['projecttask_label'] = $l_task;
$fieldnames['timetask_label'] = $l_comment;
$fieldnames['company_name'] = $l_company;
$fieldnames['total_length'] = $l_total_period;
$fieldnames['total_spent'] = $l_total_spent;
$fieldnames['total_before'] = $l_total_before;
$fieldnames['total_after'] = $l_total_after;

// TimeTask entries
$fieldnames['date_task'] = $l_date_task;
$fieldnames['timetask_project_name'] = $l_project;
$fieldnames['timetask_company_name'] = $l_company;
$fieldnames['timetask_length'] = $l_length;
$fieldnames['timetask_id'] = '';


///////////////////////////////////////////////////////////////////////////////
// Display Time specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_time_data(&$OD, $fieldname, $link_ok) {
  global $path,  $params, $ico_update;

  $res['style'] = '';
  
  if (($fieldname == 'row_style') && ($OD->data_set->f('timetask_id'))) {
    (date('d',$OD->data_set->f('date_task')) % 2 == 0)?$parity = 'pair':$parity = '';

    if ($OD->data_set->f('timetask_status') == 0)
      $res['style'] = "class=\"highlight\"";
    else
      $res['style'] = "class=\"$parity\"";
  }

  if ($fieldname == 'timetask_project_name') {
    $label = ($OD->data_set->f('timetask_project_name'));
    $res['url'] = "$path/project/project_index.php?action=detailconsult&project_id=".$OD->data_set->f('project_id');

    if (strlen($label) > 30)
      $res['name'] = substr($label,0,30) . '...';
  }

  else if ($fieldname == 'timetask_id') {

    if ($OD->data_set->f('test_status') != 0) {
      $res['popup'] = true;
      $res['popup_width'] = 700;
      $res['popup_height'] = 210;
      $res['url'] = "time_index.php?action=detailupdate&amp;popup=1&amp;task_id=".$OD->data_set->f('timetask_id')."&amp;user_id=".$OD->data_set->f('timetask_user_id')."&amp;date=".$params['date'];
      $res['name'] = "<img border=\"0\" src=\"$ico_update\" alt=\"\" />";
    } else {
      $res['name'] = "&nbsp;";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display the TimeManager index page
// Parameters:
//   - $time[] : time search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_time_index($time) {
  global $display, $perm, $cright_write_admin, $cright_read_admin;

  $time['interval'] = 'week';
  $display['result'] = dis_time_nav_date($time);
  $display['result'] .= dis_time_planning($time);

  $val_days = run_query_time_valid_search($time);
  $display['result'] .= dis_time_form_addtask($time, $val_days); 

  $display['result'] .= dis_time_task_list($time);

  // Allow user select for write admin and read admin 
  if (($perm->check_right('time', $cright_write_admin))
      || (($perm->check_right('time', $cright_read_admin)))) {
    $display['features'] .= dis_user_select($time, run_query_userobm_active());
  }
}


/**
 * Display Time navigation by date
 *
 * @param mixed $time : parameter hash
 **/
function dis_time_nav_date($time) {
  global $cdg_param, $c_dst_err, $l_monthsofyear;
  global $ico_previous, $ico_next;

  $action = $time['action'];
  $interval = $time['interval'];

  $no_user = array('stats','globalview','validate','unvalidate','viewmonth');
  if (in_array($action, $no_user)) {
    $link_action = $action;
  } else {
    $user_id = $time['user_id'];
    $u = get_user_info($user_id);
    $user = $u['firstname']. ' ' . $u['lastname'] . ' :';
    if ($action == 'viewmonth') {
      $link_action = 'viewmonth';
    } else {
      $link_action = 'index';
    }
  }

  $iso_date = $time['date'];
  $ts_date = strtotime($iso_date);

  if ($interval == 'week') {

    // get the current start and end of week
    $d_start_week = of_date_get_first_day_week($iso_date);
    $d_end_week = $d_start_week+6*86400;
    $hum_date_start = of_date_format($d_start_week);
    $hum_date_end = of_date_format($d_end_week);
    $title = "$user $hum_date_start - $hum_date_end";

    // get the previous and next beginning of week
    $d_prev = of_isodate_format($d_start_week-7*86400);
    $d_next = of_isodate_format($d_start_week+7*86400+$c_dst_err); // prevent DST bug

  } else if ($interval == 'month') {

    $month = of_date_get_month($ts_date);
    $year = of_date_get_year($ts_date);
    $d_prev = of_isodate_format(mktime(0,0,0,$month-1,1,$year));
    $d_next = of_isodate_format(mktime(0,0,0,$month+1,1,$year));
    $title = "$user ". $l_monthsofyear[$month-1] . " $year";
  }

  $url_prev = url_prepare("time_index.php?action=$link_action&amp;date=$d_prev");
  $url_next = url_prepare("time_index.php?action=$link_action&amp;date=$d_next");

  $block = "
  <h1>
    <a href=\"$url_prev\"><img src=\"$ico_previous\" alt=\"[Previous Week]\" /></a>
    $title
    <a href=\"$url_next\"><img src=\"$ico_next\" alt=\"[Next Week]\" /></a>
  </h1>";

  return $block;
}


/**
 * Display Time navigation date full menu (month, week, specific date select)
 *
 * @param mixed $time : parameter hash
 **/
function dis_time_date_menu($params) {
  global $cdg_param, $c_dst_err, $l_monthsofyear, $l_date, $l_execute;
  global $ico_previous, $ico_next;

  $action = $params['action'];
  $interval = $params['interval'];

  $iso_date_begin = $params['date_begin'];
  $iso_date_end = $params['date_end'];
  $ts_date_begin = strtotime($iso_date_begin);
  $ts_date_end = strtotime($iso_date_end);

  // Calculate infos for Week menu
  // get the current start and end of week
  $d_start_week = of_date_get_first_day_week($iso_date_begin);
  $d_end_week = $d_start_week+6*86400;
  $hum_date_start = of_date_format($d_start_week);
  $hum_date_end = of_date_format($d_end_week);
  $week_title = "$user $hum_date_start - $hum_date_end";
  // get the previous and next beginning of week
  $d_week_prev = of_isodate_format($d_start_week-7*86400);
  $d_week_next = of_isodate_format($d_start_week+7*86400+$c_dst_err); // prevent DST bug
  
  // Calculate infos for month menu
  $month = of_date_get_month($ts_date_begin);
  $year = of_date_get_year($ts_date_begin);
  $d_month_prev = of_isodate_format(mktime(0,0,0,$month-1,1,$year));
  $d_month_next = of_isodate_format(mktime(0,0,0,$month+1,1,$year));
  $month_title = "$user ". $l_monthsofyear[$month-1] . " $year";

  // Calculate infos for dates menu
  $date_begin = of_date_upd_format($iso_date_begin, true);
  $date_end = of_date_upd_format($iso_date_end, true);

  // Title (date range)
  $dis_date_begin = of_date_format($ts_date_begin);
  $dis_date_end = of_date_format($ts_date_end);

  $url_week_prev = url_prepare("time_index.php?action=$action&amp;interval=week&amp;date=$d_week_prev");
  $url_week_next = url_prepare("time_index.php?action=$action&amp;interval=week&amp;date=$d_week_next");
  $url_month_prev = url_prepare("time_index.php?action=$action&amp;interval=month&amp;date=$d_month_prev");
  $url_month_next = url_prepare("time_index.php?action=$action&amp;interval=month&amp;date=$d_month_next");

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" action=\"$url\">
    <a href=\"$url_month_prev\"><img src=\"$ico_previous\" alt=\"[Previous Week]\" /></a>
    $month_title
    <a href=\"$url_month_next\"><img src=\"$ico_next\" alt=\"[Next Week]\" /></a>

    <a href=\"$url_week_prev\"><img src=\"$ico_previous\" alt=\"[Previous Week]\" /></a>
    $week_title
    <a href=\"$url_week_next\"><img src=\"$ico_next\" alt=\"[Next Week]\" /></a>

    $l_date
      <input type=\"text\" name=\"tf_date\" value=\"$date_begin\" class=\"datePicker\"/>
      <input type=\"text\" name=\"tf_date_end\" value=\"$date_end\" class=\"datePicker\"/>
      <input name=\"action\" type=\"hidden\" value=\"stats\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
  </form>
    <h1> $dis_date_begin - $dis_date_end </h1>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for a week
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $time[] : time hash
///////////////////////////////////////////////////////////////////////////////
function dis_time_planning_week($obm_q,$time) {
  global $c_working_days, $c_day_fraction, $c_dst_err, $l_daysofweekshort;
  global $l_monthsofyearshort;

  while ($obm_q->next_record())  {
    $date = $obm_q->f('date');
    $wday = date('w', $date);
    $result[$wday] = $obm_q->f('total_length');
  }
  
  $nb_worked_days = array_sum($c_working_days);
  $tot_width = 500;
  $col_width = $tot_width / $nb_worked_days;

  $first_day = of_date_get_first_day_week($time['date']);
  $curr_day = $first_day + $c_dst_err;
  $last_day = $first_day + 7*86400;

  // we loop through days
  while ($curr_day < $last_day) {
    $wday = date('w', $curr_day);
    $day = date('d', $curr_day);
    $hum_wday = $l_daysofweekshort[$wday];
    $month = date('m', $curr_day);
    $hum_month = $l_monthsofyearshort[$month-1];

    if (!(isset($result[$wday]))) {
      $result[$wday] = 0;
    }

    if ($c_working_days[$wday] != 0) {
      $line1 .= "
        <td>$hum_wday $day $hum_month</td>";
      if ($result[$wday] == $c_day_fraction) {
        $line2 .= "
        <td class=\"downlight\">$c_day_fraction/$c_day_fraction</td>";
      } else {
	$line2 .= "
        <td class=\"highlight\">".$result[$wday]."/$c_day_fraction</td>";
      }
    }
    
    $curr_day += 86400;        
  }
  
  $block = "
  <table class=\"miniCalendar\">
  <thead>
    <tr>
      $line1
    </tr>
    </thead>
    <tbody>
    <tr>
      $line2
    </tr>
    </tbody>
    </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning according to the interval given
// Parameters:
//   - $time[] : time hash
///////////////////////////////////////////////////////////////////////////////
function dis_time_planning($time) {
  global $cdg_param;

  if ($time['interval'] == 'week') {
    $task_q = run_query_time_task_one_week($time, '');
    $block = dis_time_planning_week($task_q, $time);

  } else if ($time['interval'] == 'month') {
    $obm_q = run_query_time_user_month_planning($time);
    $block = dis_time_user_planning($obm_q, $time);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for a month
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $time[] : time hash
///////////////////////////////////////////////////////////////////////////////
function dis_time_user_planning($obm_q, $time) {
  global $path, $c_working_days, $c_week_first_day, $c_day_fraction, $l_fill;
  global $l_total, $l_daysofweekfirst;

  $col_nwday = 'A0A0A0';
  $iso_date = $time['date'];
  $ts_date = strtotime($iso_date);
  $month = of_date_get_month($ts_date);
  $year = of_date_get_year($ts_date);

  // first day of the month in second
  $first_day_sec = mktime(0,0,0,$month, 1, $year);
  $last_day_sec = mktime(23,0,0,$month+1, 0, $year);
  $day_in_sec = $first_day_sec;
  $dow = date('w', $day_in_sec);
  $obm_dow = ($dow - $c_week_first_day + 7) % 7;

  // Fill the day array -------------------------------------------------------
  $day = 1;
  while ($day_in_sec < $last_day_sec) {
    $fday = ($day>9) ? "$day" : "0$day";
    $days["$fday"]['dow'] = $dow;
    $days["$fday"]['obm_dow'] = $obm_dow;
    $day++;

    $day_in_sec = strtotime("+1 day", $day_in_sec);
    $dow = date('w', $day_in_sec);
    $obm_dow = ($dow - $c_week_first_day + 7) % 7;
  }
  $nb_days = $day - 1;
  $planning = array();
  $task_days = array();
  $line = array();
  $css = array();
  while ($obm_q->next_record()) {
    $day = date('d', $obm_q->f('date'));
    $project_id = $obm_q->f('id');
    $u_id = $obm_q->f('userobm_id');
    $type = $obm_q->f('type');
    $shortname = $obm_q->f('shortname');
    $label = $obm_q->f('label');
    if (mb_strlen($label) > 15) {
      $dis_label = substr($label,0,15) . '..';
    } else {
      $dis_label = $label;
    }
    if ($shortname == '') {
      $shortname = $dis_label;
    }
    if ($type == 'project') {
      $projects[$type.$project_id] = "<a href=\"$path/project/project_index.php?action=detailconsult&project_id=$project_id\" title=\"$label\">$shortname</a>";
    } else {
      $projects[$type.$project_id] = "<abbr title=\"$label\">$shortname</abbr>";
    }
    $users[$u_id] = $obm_q->f('userobm_lastname').' '. $obm_q->f('userobm_firstname');
    $planning[$u_id][$type.$project_id]["$day"] = $obm_q->f('timetask_length')/$c_day_fraction;
    $planning[$u_id][$type.$project_id]['length'] += $obm_q->f('timetask_length')/$c_day_fraction;
  }

  // Loop through each month days
  while (list($day,$val) = each($days)) {
    // Display the month planning
    $lday = $l_daysofweekfirst[$val['dow']];
    $line0 .= "<th><a href=\"$path/time/time_index.php?action=index&amp;date=$year-$month-$day<SELECTEDUSER>\">$lday</a></th>\n";
    if (! $c_working_days[$val['dow']]) {
      $line1 .= "<td style=\"background-color: #$col_nwday; text-align: center\">$day</td>\n";
    } else {
      $line1 .= "<td style=\"background-color: #$col_nwday; text-align: center; color: #FFFFFF\">$day</td>\n";
    }

    // Fill the day cell for each user
    foreach($planning as $u_id => $user_planning) {
      foreach($user_planning as $project_id => $work) {
        if (isset($work[$day])) {
          $length = $work[$day]*100;
          $line[$u_id][$project_id] .= "<td title=\"$length%\"><div style=\"width:$length%;\">&nbsp;</div></td>";
        } else {
	  if (! $c_working_days[$days[$day]['dow']]) {
	    $line[$u_id][$project_id] .= "<td style=\"background-color: #$col_nwday;\">&nbsp;</td>";
	  } else {
	    $line[$u_id][$project_id] .= '<td></td>';
	  }
        }
      }
    }
  }

  // Fill the displayed rows
  foreach($planning as $u_id => $user_planning) {
    $total_length = 0;
    foreach($user_planning as $project_id => $work) {
      $line[$u_id][$project_id] = "<tr class=\"$project_id\">
        <th>".$projects[$project_id].'</th>'.$line[$u_id][$project_id]."
        <th>&nbsp;</th><td>".$work['length'].'</td></tr>';
      $color = randomColor();
      $css[$project_id] .= ".$project_id th {color:$color;} \n.$project_id div {background-color:$color;}";
      $total_length += $work['length'];
    }
    $users_length[$u_id] = $total_length;
  }

  foreach($line as $u_id => $l) {
    if (isset($u_id)) {
      $replace = "&amp;sel_user_id=".$u_id;
    } else {
      $replace = "";
    }
    $currentLine0 = preg_replace('/<SELECTEDUSER>/', $replace, $line0);

    $d_l .= '<tr><th
    colspan=\"'.($nb_days+2).'\"><br/>'.$users[$u_id].'<br/>&nbsp;</th><th>'.$users_length[$u_id].'</th></tr><tr><th>&nbsp;</th>'.$currentLine0.'<tr></tr><th>&nbsp;</th>'.$line1.'</tr>'.
    implode($l);
  }
  $css = implode("\n",$css);
  $block = "
  <style>
    $css
  </style>
  <table class=\"planning\">
  <colgroup>
  </colgroup>
  <colgroup class=\"main\" span=\"$nb_days\">
  </colgroup> 
  <colgroup class=\"total\" span=\"2\">
  </colgroup>    
  <body>
  $d_l
  </body>
  </table>
";

  return $block;
}


/**
 * Display TimeManager User search Form
 * @param $time[] default form values
 * @param $usr_q  DBO : UserObm list
 * @param $multi if false, mono select else select multi
 **/
function dis_user_select($time, $usr_q, $multi=false) {
  global $l_see, $l_user, $l_all, $c_all, $c_none;

  $sel_user_ids = $time['user_ids'];

  if ($multi) {
    $size = 12;
    $type = "multiple=\"multiple\"";
    $alt = "[]";
    $dis_button_all = "
      <input type=\"submit\" name=\"submit\" value=\"$l_all\"
             onclick=\"select_all(this.form)\" />";
  } else {
    $size = 6;
  }
  
  // UserObm Select
  $sel_user = "<select id=\"sel_user_id\" name=\"sel_user_id$alt\" $type>";
  $selected = false;
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f('userobm_id');
    $lname = $usr_q->f('userobm_lastname');
    $fname = $usr_q->f('userobm_firstname');
    $sel_user .= "\n<option value=\"$u_id\" ";
    
    if (in_array($u_id, $sel_user_ids)) {
      $sel_user .= "selected=\"selected\"";
    }
    
    $sel_user .= " >$lname $fname</option>";
  }
  $sel_user .= '</select>';
  
  // Define the action to follow
  if ($time['action'] == 'stats')
    $act = 'stats';
  else if ($time['action'] == 'viewmonth')
    $act = 'viewmonth';
  else 
    $act = 'index';
  
  $url = url_prepare('time_index.php');
  
  $block = "
    <div class=\"portlet\">
    <h1>$l_user</h1>
    <form method=\"post\" name=\"f_time\" action=\"$url\">
    <fieldset class=\"detail extra\">
      $sel_user
    </fieldset>
    <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"date\" value=\"".$time['date']."\" />
      <input type=\"hidden\" name=\"group_id\" value=\"$c_none\" />
      <input type=\"hidden\" name=\"action\" value=\"$act\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_see\" />
      $dis_button_all
    </fieldset>
    </form>
    </div>
";

  return $block;
}


/**
 * Display TimeManager Group selection Form
 * @param $time[] default form values
 * @param $usr_q  DBO : UserObm list
 * @param $multi if false, mono select else select multi
 **/
function dis_time_group_select($time, $groups, $multi=false) {
  global $obm, $path, $c_none, $ico_add, $l_time, $l_group;
  global $l_select_group, $l_public_group_members, $l_private_group_members;

  $iso_date = $time['date'];
  $sel_group_id = $time['group_id'];

  // Define the action to follow
  if ($time['action'] == 'stats')
    $action = 'stats';
  else if ($time['action'] == 'viewmonth')
    $action = 'viewmonth';
  else 
    $action = 'index';

  // private group Select
  if ($sel_group_id == $c_none) {
    $sel_group = "<option selected=\"selected\">$l_select_group</option>";
  } else {
    $sel_group = "<option value=\"time_index.php?action=$action&amp;group_id=$c_none&amp;sel_user_id[]=$obm[uid]&amp;date=$iso_date\">$l_select_group</option>";
  }
  $selected = false;
  if (isset($groups)) {
    // Don't compute private group if none defined
    foreach ($groups as $g_id => $one_group) {
      $gname = $one_group['name'];
      if ($g_id == $sel_group_id) {
        $selected = " selected=\"selected\"";
      } else {
        $selected = '';
      }
      $sel_group .= "\n<option value=\"time_index.php?action=$action&amp;sel_group_id=$g_id&amp;date=$iso_date\" $selected>$gname</option>";
    }
  }

  if (($sel_group_id > 0) && ($sel_group_id != $c_none)) {
    $g_public_name = get_last_group_text($sel_group_id);
  } 

  $url_group = "$path/group/group_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget_text=f_entity_group.group_name&amp;ext_widget=f_entity_group.ext_group_id&amp;ext_action=$action&amp;ext_url=".urlencode($path.'/time/time_index.php')."&amp;ext_target=$l_time&amp;iso_date=$iso_date";
  $group_add_button = "
    <a href=\"javascript: return false;\" 
    onclick=\"popup('$url_group', '');
  return false;\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>";

  $block = "
  <div class=\"portlet\" id=\"userSelection\">
    <h1>$l_group</h1>
    <div>

    <form name=\"f_entity_group\" action=\"\" onsubmit=\"return false;\">
    <fieldset class=\"detail extra\">
    <legend>$l_private_group_members</legend>
    <select name=\"action\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
    $sel_group
    </select>
    </fieldset>
    <fieldset class=\"detail extra\">
    <legend>$l_public_group_members $group_add_button</legend>
    <input type=\"text\" name=\"group_name\" value=\"$g_public_name\" id=\"group_name\" autocomplete=\"off\" />
    <input type=\"hidden\" name=\"ext_group_id\" id=\"ext_group_id\" />
    <div id=\"sel_group\">
    </div>
    </fieldset>
    </form>
    </div>
  </div>

  <script type=\"text/javascript\">
    obm.vars.images.update = '$ico_group';
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search', 'sel_group', 'group_name', {mode: 'mono',
        locked: 'true', resetable: 'true', selectfunction:'sel_public_group(\"$iso_date\", element, \"$action\")'});
    });
  </script>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the full TimeManager result page
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_task_list($time) {
  global $obm, $display;

  $task_q = run_query_time_task_list($time);
  $nb_task = $task_q->num_rows();

  if ($nb_task != 0) {
    $prefs = get_display_pref($obm['uid'], 'time');
    $block .= dis_time_list_result($task_q, $prefs, $time);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Task  result
// Parameters : 
//   - $task_q : list of tasks to be displayed
//   - $prefs  : the fields which have to be displayed
//   - $time[] : time search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_time_list_result($task_q,$prefs,$time) {
  global $l_delete, $l_task_del, $l_task_list;

  $url = url_prepare('time_index.php');
 
  $block .= "
     <form method=\"post\" name=\"f_task_del\" action=\"$url\"
       onsubmit=\"if (confirm_del()) return true; else return false\" >";

  $dis_task_list = new OBM_DISPLAY('DATA', $prefs, 'time');
  $dis_task_list->data_set = $task_q;
  $dis_task_list->data_header = 'top';
  $dis_task_list->data_page = false;
  $dis_task_list->data_cb_text = $l_task_del;
  $dis_task_list->data_idfield = 'timetask_id'; 
  $dis_task_list->data_order = false; 
  $dis_task_list->data_cb_side = 'left';
  $dis_task_list->data_cb_name = 'delete_';
  $dis_task_list->data_cb_show = 'test_status';

  $block .= "<h1>$l_task_list</h1>\n";
  $block .= $dis_task_list->display('dis_time_data');

  $dis_button = "
    <input type=\"hidden\" name=\"sel_user_id\" value=\"".$time['user_id']."\" />
    <input type=\"hidden\" name=\"date\" value=\"".$time['date']."\" />
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input name=\"submit\" type=\"submit\" value=\"$l_delete\" />";

  $block .= "
    <fieldset class=\"buttons\" >
     $dis_button
    </fieldset>

    </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Time Entry Form
//   - the days already filled are not shown
//   - check box allows to select more than one day at once
//   - if all days in the week are validated, does not display anything
// Parameters:
//   - $time[] : time hash values
//   - $validate_days : array of validated days (not to be shown)
///////////////////////////////////////////////////////////////////////////////
function dis_time_form_addtask($time, $validate_days) {
  global $extra_js, $l_add, $l_update, $c_undef, $l_undef, $l_close;
  global $l_fill_addtask, $l_comment, $l_date, $l_project, $l_duration;
  global $l_header_tasktype, $l_intern;
  global $c_day_fraction, $c_working_days, $c_week_first_day;
  global $ctt_sales, $ctt_research, $ctt_others;
  global $l_tt_sales, $l_tt_research, $l_tt_others;

  $action = $time['action'];
  $user_id = $time['user_id'];
  $project_q = run_query_time_project($time);
  $projecttask_q = run_query_time_projecttask($time);
  $tasktype_q = run_query_time_used_tasktype($time['user_id']);
  $nb_worked_days = array_sum($c_working_days);
  $d_start_week = of_date_get_first_day_week($time['date']);  
  $work_day_dates = get_time_week_working_days_dates($d_start_week);

  $cpt_end = $c_week_first_day + 7;
  
  // get different attribute for task to be modify
  if ($action == 'detailupdate') {
    $obm_action = 'update';
    $task_id = $time['task_id'];
    $task_q = run_query_time_get_task($task_id);
    //    $is_day = substr($task_q->f('timetask_date'),0,8);
    $is_day = date('Ymd', $task_q->f('date_task'));
    $is_tt = $task_q->f('timetask_tasktype_id');
    $is_proj = $task_q->f('timetask_project_id');
    $is_projtask = $task_q->f('timetask_projecttask_id');
    $is_time = $task_q->f('timetask_length');
    $time_label = $task_q->f('timetask_label');

    // Day radio select (for update)
    for ($day = $c_week_first_day; $day < $cpt_end; $day++) {
      if (isset($work_day_dates[$day])) {
	$dlabel = $work_day_dates[$day][0];
	$dvalue = $work_day_dates[$day][1]; 
	$rd_day .= "<input type=\"radio\" name=\"rd_day\" value=\"$dvalue\"";
	if ($work_day_dates[$day][1] == $is_day) {
	  $rd_day .= " checked=\"checked\"";
	}
	$rd_day .= " />$dlabel\n";
      }
    }

  } else {
    $obm_action = 'insert';
    $time_label = '';
    $is_time = $c_day_fraction;

    // Day checkboxes construction
    // Precheck current day
    $day_checked = -1;
    $today = date('Ymd');
    for ($day=$c_week_first_day; $day < $cpt_end; $day++) {
      if ($work_day_dates[$day][1] == $today) {
	$day_checked = $day;
      }
    }

    for ($day=$c_week_first_day; $day < $cpt_end; $day++) {
      // If it is a working day
      if (isset($work_day_dates[$day])) {
	$dlabel = $work_day_dates[$day][0];
	$ddate = $work_day_dates[$day][1]; 

	// If day is already filled
	$dis_attr = '';
	if (isset($validate_days[$ddate])) {
	  $dis_attr = 'disabled';

	  // not this week, we pre check the first day
	} else if ($day_checked == -1) {
	  $dis_attr = " checked=\"checked\"";
	  $day_checked = $day;

	  // we pre check today if current week
	} else if ($day == $day_checked) {
	  $dis_attr = " checked=\"checked\"";
	}

        $ch_day .= "<input type=\"checkbox\" id=\"lbl_$day\" name=\"cb_day[$ddate]\" $dis_attr /><label for=\"lbl_$day\">$dlabel</label>\n";
      }
    }
  }

  // Javascript array construction for the Project dynamic Select list
  // used by fill_project()
  if ($project_q->num_rows() != 0) {
    $previous_tt = 0;

    while ($project_q->next_record()) {
      $proj_tt = $project_q->f('project_tasktype_id');
      $proj_id = $project_q->f('project_id');
      $company = $project_q->f('company_name');
      if ($company == '') $company = $l_intern;
      $proj_name = addslashes($project_q->f('project_name'));
      $proj_name = (strlen($proj_name) > 35) ? substr($proj_name,0,35).'...' : $proj_name;
      $proj_shortname = addslashes($project_q->f('project_shortname'));
      $proj_label = "\" $proj_shortname | $company | $proj_name\"";

      if ($previous_tt != $proj_tt) {
	if ($previous_tt != 0) {
	  $proj_data .= "\n),";
	}
        $proj_data .= "\n'". $proj_tt ."': new Array(\n";
	$previous_tt = $proj_tt;
      } else {
	$proj_data .= ",\n";
      }
      $proj_data .= "new Object({ 'label' : $proj_label, 'id': $proj_id})";  
    }
    $proj_data .= "\n)\n";
  }

  // Javascript array construction for the ProjectTask dynamic Select list
  // used by fill_projecttask()
  if ($projecttask_q->num_rows() != 0) {
    $previous_pid = 0;

    while ($projecttask_q->next_record()) {
      $projtask_pid  = $projecttask_q->f('projecttask_project_id');
      $projtask_id = $projecttask_q->f('projecttask_id');
    
      $projtask_name = addslashes($projecttask_q->f('projecttask_label'));
      $projtask_name = (strlen($projtask_name) > 64) ? substr($projtask_name,0,64).'...' : $projtask_name;

      $projtask_label = "\"". $projtask_name ."\"";

      if ($previous_pid != $projtask_pid) {
	if ($previous_pid != 0) {
	  $projtask_data .= "\n),";
	}
        $projtask_data .= "\n'". $projtask_pid ."': new Array (\n";
	$previous_pid = $projtask_pid;
      } else {
	$projtask_data .= ",\n";
      }
      $projtask_data .= "new Object({'label':$projtask_label,'id': $projtask_id} )";
    }
    $projtask_data .= "\n)\n";
  }

  $extra_js .= " project = new Object ({
   $proj_data
  });

  projecttask = new Object({
      $projtask_data
      })
";

  // TaskType select construction
  $task_list = array($ctt_sales=>$l_tt_sales,
		     $ctt_research=>$l_tt_research,
		     $ctt_others=>$l_tt_others);

  $type_prec = -1;
  $sel_tt = "
    <select name=\"sel_tasktype\"
      onchange=\"fill_project(this.form.sel_project, this.form.sel_tasktype.value,this.form.sel_projecttask, -1) \">
      <option value=\"$c_undef\">$l_undef</option>";
  for ($i=0; $i<2; $i++) {
    $tt_q = $tasktype_q[$i];
    while($tt_q->next_record()) {
      $tt_id = $tt_q->f('tasktype_id');
      $tt_label = $tt_q->f('tasktype_label');
      $tt_internal = $tt_q->f('tasktype_internal');
      //put menu titles
      if ($tt_internal != $type_prec) {
	$sel_tt .= "<option disabled=\"disabled\">".$task_list[$tt_internal]."</option>";
	$type_prec = $tt_internal;
      }
      $sel_tt .= "\n<option value=\"$tt_id\"";
      if ($is_tt == $tt_id) $sel_tt .= " selected=\"selected\""; 
      $sel_tt .= ">$tt_label</option>\n";
    }
  }
  $sel_tt .= '</select>';

  // Project select construction
  $sel_proj = "
    <select name=\"sel_project\"
      onchange=\"fill_tasktype(this.form.sel_tasktype, this.form.sel_project.value);
      fill_project(this.form.sel_project, this.form.sel_tasktype.value, this.form.sel_projecttask, this.form.sel_project.value);return true;\">
      <option value=\"$c_undef\">$l_undef</option>
    </select>";

  // ProjectTask select construction
  $sel_projtask = "
    <select name=\"sel_projecttask\">
      <option value=\"$c_undef\">$l_undef</option>
    </select>";

  // Project time length
  $sel_time="<select name=\"sel_time\">";
  for ($t=1; $t<=$c_day_fraction; $t++) {
    $dlabel = "$t/$c_day_fraction";
    $sel_time .= "<option value=\"$t\"";
    if ($is_time == $t) $sel_time .= " selected=\"selected\""; 
    $sel_time .= ">$dlabel</option>\n";
  }
  $sel_time .= '</select>';

  // Update timetask ----------------------------------------------------------
  if ($action == 'detailupdate') {
    $dis_date .= "$rd_day";

    // Action button
    $dis_button .= "
       <input type=\"hidden\" name=\"date\" value=\"".$time['date']."\" />
       <input type=\"hidden\" name=\"task_id\" value=\"$task_id\" />
       <input type=\"submit\" value=\"$l_update\" />";

    // Script that prefill values (only if tasktype has project and tasks)
    if ($is_proj > 0) {
      $dis_select_defaults = "
        fill_projecttask(document.f_add_task.sel_projecttask, \"$is_proj\");
        select_default(document.f_add_task.sel_project, $is_proj);
        select_default(document.f_add_task.sel_projecttask, \"$is_projtask\");";
    }
    $dis_script .= "
      <input type=\"submit\" value=\"$l_close\" onclick=\"window.close();\" />
      <script type=\"text/javascript\">
        fill_project(document.f_add_task.sel_project,
          document.f_add_task.sel_tasktype.value,
          document.f_add_task.sel_projecttask);
        $dis_select_defaults
      </script>";

    // New timetask -----------------------------------------------------------
  } else {
    $dis_date .= "$ch_day";

    // Action button
    $dis_button .= "
       <input type=\"hidden\" name=\"date\" value=\"".$time['date']."\" />
       <input type=\"submit\" value=\"$l_add\" />";

    // Script that prefill values
    $dis_script .= "
    <script type=\"text/javascript\">
     fill_projectall(document.f_add_task.sel_project);
    </script>";
  }
  
  $url = url_prepare('time_index.php');

  $block .= "
  <form method=\"post\" name=\"f_add_task\" action=\"$url\"
    onsubmit=\"if (check_addtaskform(this)) return true; else return false;\">
    <input type=\"hidden\" name=\"action\" value=\"$obm_action\" />
    <input type=\"hidden\" name=\"popup\" value=\"0\" />
    <input type=\"hidden\" name=\"user_id\" value=\"$user_id\" />
  <fieldset class=\"detail extra\">
  <legend>$l_fill_addtask</legend>
  <table class=\"spreadSheet\">
    <tr>
      <th>$l_date :</th>
      <td>$dis_date</td>
      <th>$l_duration :</th>
      <td>$sel_time</td>
    </tr>
    <tr>
      <th>$l_header_tasktype :</th>
      <td colspan=\"3\">$sel_tt</td>
    </tr>
    <tr>
      <th>$l_project :</th>
      <td>$sel_proj</td>
      <td colspan=\"2\">$sel_projtask</td>
    </tr>
    <tr>
      <th>$l_comment :</th>
      <td colspan=\"3\">
      <input type=\"text\" name=\"tf_label\" size=\"30\" value=\"$time_label\" />
      </td>
    </tr>
  </table>
  </fieldset>
  <fieldset class=\"buttons\">
  $dis_button
  </fieldset>
  $dis_script
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the month total page (with all users)
// Parameters : 
//   - $time : parameter hash
///////////////////////////////////////////////////////////////////////////////
function dis_time_month_users_total($time) {
  global $display, $l_admin_empty;

  $month_q = run_query_time_short_month($time);

  if ($month_q->nf()==0) {
    $block .= display_warn_msg($l_admin_empty);
  } else {
    $cpt = 0;
    while ($month_q->next_record())  {
      $f_name = $month_q->f('userobm_firstname');
      $l_name = $month_q->f('userobm_lastname');
      $array_res[$cpt]['id'] = $month_q->f('userid');
      $array_res[$cpt]['name'] = $l_name . ' ' . $f_name;
      $array_res[$cpt]['status'] = $month_q->f('status');
      $array_res[$cpt]['length'] = $month_q->f('total_length');
      $cpt++;
    }

    $block .= html_time_month_users_total($time, $array_res);
  }
        
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the month total page (with all users)
// Parameters : 
//   - $time : parameter hash
//   - 
///////////////////////////////////////////////////////////////////////////////
function html_time_month_users_total($time, $array_res) {
  global $c_working_days, $c_day_fraction;
  global $l_user, $l_total, $l_see, $l_fill, $l_valid, $l_unvalid, $l_admin_valid;
  global $ico_update,$ico_nok,$ico_ok;

  $date = $time['date'];
  $tot_workingdays = get_time_nb_working_days($date);

  while (list($key) = each($array_res)) {	
    $uid = $array_res[$key]['id'];
    $user_title = $array_res[$key]['name'];
    // XXXX status can be different from different ttasks ? should not global validatio  or unvalidation !
    $user_status = $array_res[$key]['status'];
    $user_total = $array_res[$key]['length'];

    // we display a recap for this user
    $fract = $user_total/$c_day_fraction;

    if ($user_status == 2) {
      $dis_validlink = "
       <a href=\"time_index.php?action=unvalidate&amp;date=$date&amp;sel_user_id=$uid\">
       <img src=\"$ico_nok\" alt=\"$l_unvalid\" /> $l_unvalid</a>";
      $link_act = "$l_see";
    } else if ($fract != $tot_workingdays) {
      $dis_validlink = '';
      $link_act = "$l_fill";
    } else {
      $class = 'timeValfilled';
      $dis_validlink = "
       <a href=\"time_index.php?action=validate&amp;date=$date&amp;sel_user_id=$uid\">
       <img src=\"$ico_ok\" alt=\"$l_val\" /> $l_valid</a>";
      $link_act = "$l_see";
    }

    $fract = ($fract != number_format($fract, 0)) ? number_format($fract, 1) : $fract;
    $dis_tot= ($fract != $tot_workingdays) ? $class='' : $class='info';

    $dis_months .= "
     <tr>
      <th>$user_title</th>
      <td class=\"$class\">$fract / $tot_workingdays</td>
      <th>
       <a href=\"time_index.php?action=viewmonth&amp;date=$date&amp;sel_user_id=$uid\">
       <img src=\"$ico_update\" alt=\"Update\" /> $link_act</a>
      </th>
      <td>$dis_validlink</td>
     </tr>";
  }

  $block = "
  <h1>$l_admin_valid</h1>
  <table class=\"spreadSheet\">
  <thead>
    <tr>
    <th><a href=\"time_index.php?action=globalview&amp;date=$date&amp;order=user\">$l_user</th>
    <th><a href=\"time_index.php?action=globalview&amp;date=$date&amp;order=activity\">$l_total</th>
    </tr>
  </thead>
  <tbody>
    $dis_months
  </tbody>
  </table>";

  return $block;
}


/**
 * Display Projects activity stats
 *
 * @param mixed $statp : project tasktime infos array
 * @param mixed $time[] : time parameters
 */
function dis_time_stats_project($statp, $time) {
  global $cdg_param, $obm, $path;
  global $l_proj_stats, $l_no_proj_stats, $l_project, $l_tt, $l_company;
  global $l_total,$l_total_period, $l_total_before, $l_total_after;
  global $l_all, $l_selection, $l_missing;

  if (is_array($statp)) {
    foreach($statp as $p_id => $p) {
      if ($p_id != '0') {
        $cpt++;
        if($cpt % 2 == 0) {
          $class = "class=\"pair\"";
        } else {
          $class = '';
        }
	$name = $p['project'];
	$p_url = "<a href=\"$path/project/project_index.php?action=detailconsult&amp;project_id=$p_id\">$name</a>";
	$tasktype = $p['tasktype'];
	$company = $p['company'];
	$time = $p['time'];
	$time_before = $p['time_before'];
	$time_after = $p['time_after'];
	$missing = $p['missing'];
	$all_time = $p['all_time'];
	$all_time_before = $p['all_time_before'];
	$all_time_after = $p['all_time_after'];
	$dis_p_array .= "
      <tr $class>
        <td>$p_url</td>
        <td>$tasktype</td>
        <td>$company</td>
        <td style=\"text-align: right;\">$missing&nbsp;</td>
        <td style=\"text-align: right;\">$time&nbsp;</td>
        <td style=\"text-align: right;\">$all_time</td>
        <td style=\"text-align: right;\">$time_before&nbsp;</td>
        <td style=\"text-align: right;\">$all_time_before</td>
        <td style=\"text-align: right;\">$time_after&nbsp;</td>
        <td style=\"text-align: right;\">$all_time_after</td>
      </tr>";
      }
    }

    $total_time = $statp[0]['time_total'];
    $total_time_before = $statp[0]['time_before_total'];
    $total_time_after = $statp[0]['time_after_total'];

    $all_total_time = $statp[0]['all_time_total'];
    $all_total_time_before = $statp[0]['all_time_before_total'];
    $all_total_time_after = $statp[0]['all_time_after_total'];
    
    $dis_stat_p = "
    <h1>$l_proj_stats</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th rowspan=\"2\">$l_project</th>
      <th rowspan=\"2\">$l_tt</th>
      <th rowspan=\"2\">$l_company</th>
      <th rowspan=\"2\">$l_missing</th>
      <th colspan=\"2\">$l_total_period</th>
      <th colspan=\"2\">$l_total_before</th>
      <th colspan=\"2\">$l_total_after</th>
    </tr>
    <tr>
      <th>$l_selection</th>
      <th>$l_all</th>
      <th>$l_selection</th>
      <th>$l_all</th>
      <th>$l_selection</th>
      <th>$l_all</th>
    </tr>
    </thead>
    $dis_p_array
    <tfoot>
    <tr>
      <td colspan=\"4\">$l_total</td>
      <td>$total_time&nbsp;</td>
      <td>$all_total_time</td>
      <td>$total_time_before&nbsp;</td>
      <td>$all_total_time_before</td>
      <td>$total_time_after&nbsp;</td>
      <td>$all_total_time_after</td>
    </tr>
    </tfoot>
    </table>";
  } else {
    $dis_stat_p = "
    <h1>$l_no_proj_stats</h1>";
  }

  $block .= $dis_stat_p;
  
  return $block;
}


/**
 * Display TimeTask stats
 *
 * @param mixed $stattt : tasktype infos array
 * @param mixed $time[] : time parameters
 */
function dis_time_stats_tasktype($stattt, $time) {
  global $cdg_param, $obm;
  global $l_tt_stats, $l_no_tt_stats, $l_tt, $l_tt_others;
  global $l_total, $l_total_period, $l_total_before, $l_total_after;

  if (is_array($stattt)) {
    $prec_type = '';
    foreach($stattt as $tt_id => $tt) {
      if ($tt_id != '0') {
        $cpt++;
        if ($cpt % 2 == 0) {
          $class = "class=\"pair\"";
        } else {
          $class = '';
        }
	$label = $tt['label'];
	$time = number_format($tt['time'],3);
	$time_before = number_format($tt['time_before'],3);
	$time_after = number_format($tt['time_after'],3);
	$type = $tt['type'];
	if (($type != $prec_type) && ($prec_type != '')) {
	  $title = ${"l_$prec_type"};
	  $dis_tt_array .= "
      <tr>
        <th>$l_total : $title</th>
        <th>$type_time&nbsp;</th>
        <th>$type_time_before&nbsp;</th>
        <th>$type_time_after&nbsp;</th>
      </tr>";
	  $type_time = 0;
	  $type_time_before = 0;
	  $type_time_after = 0;
	}
	$prec_type = $type;
	$type_time += $tt['time'];
	$type_time_before += $tt['time_before'];
	$type_time_after += $tt['time_after'];
	$dis_tt_array .= "
      <tr $class>
        <td>$label</td>
        <td style=\"text-align: right;\">$time&nbsp;</td>
        <td style=\"text-align: right;\">$time_before&nbsp;</td>
        <td style=\"text-align: right;\">$time_after&nbsp;</td>
      </tr>";
      }
    }

    $dis_tt_array .= "
      <tr>
        <th>$l_total</th>
        <th>$type_time&nbsp;</th>
        <th>$type_time_before&nbsp;</th>
        <th>$type_time_after&nbsp;</th>
      </tr>";

    $total_time = $stattt[0]['time_total'];
    $total_time_before = $stattt[0]['time_before_total'];
    $total_time_after = $stattt[0]['time_after_total'];
    
    $dis_stat_tt = "
    <h1>$l_tt_stats</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>$l_tt</th>
      <th>$l_total_period</th>
      <th>$l_total_before</th>
      <th>$l_total_after</th>
    </tr>
    </thead>
    $dis_tt_array
    <tfoot>
    <tr>
      <td>$l_total</td>
      <td>$total_time&nbsp;</td>
      <td>$total_time_before&nbsp;</td>
      <td>$total_time_after&nbsp;</td>
    </tr>
    </tfoot>
    </table>";
  } else {
    $dis_stat_tt = "
    <h1>$l_no_tt_stats</h1>";
  }

  $block .= $dis_stat_tt;
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Time Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_time_display_pref($prefs) {
  global $l_searchtime_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'time');
  $dis_pref->pref_title = $l_searchtime_display;

  $block .= '<td>';
  $block .= $dis_pref->display();
  $block .= '</td></tr></table>';

  $block .= $dis_pref->dis_pref_help();
  return $block;
}


//////////////////////////////////////////////////////////////////////////
//  Display the Activity Report Form
//  Parameters:
//    - $ra_values : form values
//////////////////////////////////////////////////////////////////////////
function dis_time_form_ra($time)
{
  global $display;
  global $c_undef, $l_undef, $l_intern, $l_comment;
  global $l_projects, $l_week, $l_month, $l_period, $l_date;
  global $l_pdf_settings, $l_generate;
  global $l_monthsofyear, $l_monthsofyearshort;
  global $c_working_days, $c_week_first_day;
  global $c_dst_err;

  if (!isset($display['msg']) && isset($time['pdf_form'])) {
    $pdf = new TimeSheetPDF();
    $pdf->AliasNbPages();
    $pdf->AddPage();
    $pdf->Init($time);
    $pdf->Body();
    $pdf->Output($pdf->filename, 'I');
    exit();
  }

  $proj_q = run_query_time_project($time);

  $sel_proj  = '<select name="sel_projects[]" multiple="multiple" size="5">';
  if ($proj_q->num_rows() != 0) {
    while ($proj_q->next_record()) {
      $proj_id = $proj_q->f("project_id");
      $company = $proj_q->f("company_name");
      $company = ($company != "") ? $company : $l_intern;
      $proj_name = addslashes($proj_q->f("project_name"));
      $proj_name = (strlen($proj_name) > 35) ? substr($proj_name,0,35)."..." : $proj_name;
      $proj_label = "$company -- $proj_name";
      if (isset($time['projects']))
        $selected = in_array($proj_id, $time['projects']) ? ' selected="selected" ' : '';
      $sel_proj .= "<option value=\"$proj_id\"$selected>$proj_label</option>";
    }
  }
  $sel_proj .= '</select>';

  $sel_month  = '<select name="sel_month" onChange="selectRadio(\'int_month\')">';
  foreach ($l_monthsofyear as $key => $value) {
    $key++;
    if (isset($time['month']))
        $selected = $key == $time['month'] ? ' selected="selected" ' : '';
    else
        $selected = $key == date('m') ? ' selected="selected" ' : '';
    $key = ($key > 9 ? "$key" : "0$key");
    $sel_month .= '<option value="'.$key.'"'.$selected.'>'.$value.'</option>';
  }
  $sel_month .= '</select>';

  $d_start_week = of_date_get_first_day_week($time["date"]);
  $d_week = $d_start_week - (7 * (date("W") - 1) * 86400);

  $sel_week  = '<select name="sel_week" onChange="selectRadio(\'int_week\')">';
  $week_selected_{$time['week']} = 'selected="selected"';
  for ($i = 1; $i < 53; $i++)	{
    $d_week_mon = date('d/m', $d_week);
    $d_week_sun = date('d/m', $d_week + 6 * 86400 + $c_dst_err);
    $val = date('Ymd', $d_week);
    if (isset($time['week']))
        $selected = $val == $time['week'] ? ' selected="selected"' : '';
    else
        $selected = date('W', $d_week) == date('W') ? ' selected="selected" ' : '';

    $sel_week .= '<option value="'.$val.'"'.$selected.'>s'.date('W', $d_week).': '.$d_week_mon.' - '.$d_week_sun.'</option>';
    $d_week += 7 * 86400 + $c_dst_err;
  }
  $sel_week .= '</select>';

  $sel_int  = '<select name="sel_int_type" onChange="selectRadio(\'int_date\')">';
  $sel_int .= '<option value="week">'.$l_week.'</option>';
  $sel_int .= '<option value="month">'.$l_month.'</option>';
  $sel_int .= '</select>';

  if (isset($time['interval']))
    ${'chk_' . $time['interval']} = 'checked="checked"';
  else
    $chk_week = 'checked="checked"';

  $html_content = '
  <script type="text/javascript">
    function selectRadio(radio)
    {
      document.getElementById(radio).checked = "checked";
    }
  </script>

  <form action="time_index.php?action=activity_report" method="post">
    <fieldset class="detail">
      <legend>'.$l_pdf_settings.'</legend>
      <table>
        <tr>
          <th>'.$l_projects.'</th>
          <td>'.$sel_proj.'</td>
        </tr>
        <tr>
          <th>'.$l_period.'</th>
          <td>
            <label><input type="radio" name="interval" id="int_week" value="week" '.$chk_week.' />'.$l_week.': '.$sel_week.'</label>
            <label><input type="radio" name="interval" id="int_month" value="month" '.$chk_month.' />'.$l_month.': '.$sel_month.'</label>
            <br />
            <label><input type="radio" name="interval" id="int_date" value="date" '.$chk_date.' />'.$l_date.':</label>
            <input type="text" name="tf_int_date" onChange="selectRadio(\'int_date\')" class="datePicker" />
            '.$sel_int.'
          </td>
        </tr>
        <tr>
          <th>'.$l_comment.'</th>
          <td>
            <textarea name="ta_comment">'.$time['comment'].'</textarea>
          </td>
        </tr>
      </table>
    </fieldset>
    <fieldset class="buttons">
      <input name="pdf_form" type="submit" value="'.$l_generate.' PDF" />
    </fieldset>
  </form>';

  return $html_content;
}

?>
