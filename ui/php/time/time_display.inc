<?php
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
$fieldnames['tasktype_label'] = $l_cat;
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


///////////////////////////////////////////////////////////////////////////////
// Display Time navigation by date
// Parameters:
//   - $time : parameter hash
///////////////////////////////////////////////////////////////////////////////
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

  if ($interval == 'week') {

    $tot_width = 500;

    // get the current start and end of week
    $d_start_week = time_first_day_week($time['date']);
    $d_end_week = $d_start_week+6*86400;
    $hum_date_start = dis_time_human_date(date('Ymd', $d_start_week));
    $hum_date_end = dis_time_human_date(date('Ymd', $d_end_week));
    $title = "$user $hum_date_start - $hum_date_end";

    // get the previous and next beginning of week
    $d_prev = date('Ymd',$d_start_week-7*86400);
    $d_next = date('Ymd',$d_start_week+7*86400+$c_dst_err); // prevent DST bug

  } else if ($interval == 'month') {

    $tot_width = (in_array($action, $no_user)) ? 200 : 400;

    // This should work for january also (month 0 = december) 
    $month = substr($time['date'],4,2);
    $year = substr($time['date'],0,4);
    $d_prev = date('Ymd', mktime(0,0,0,$month-1,1,$year));
    $d_next = date('Ymd', mktime(0,0,0,$month+1,1,$year));
    $title = "$user ". $l_monthsofyear[$month-1] . " $year";;
  }

    $url_prev = url_prepare("time_index.php?action=$link_action&amp;date=$d_prev");
    $url_next = url_prepare("time_index.php?action=$link_action&amp;date=$d_next");

    $block = "
    <h1>
    <a href=\"$url_prev\"><img src=\"$ico_previous\" alt=\"[Previous Week]\" /></a>     
    $title
    <a href=\"$url_next\"><img src=\"$ico_next\" alt=\"[Next Week]\" /></a>     
    </h1> 
    ";    

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for a week
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $time[] : time hash
///////////////////////////////////////////////////////////////////////////////
function dis_time_planning_week($obm_q,$time) {;
  global $c_working_days, $c_day_fraction, $c_dst_err, $l_daysofweekshort;

  while ($obm_q->next_record())  {
    $date = $obm_q->f('date');
    $wday = date('w', $date);
    $result[$wday] = $obm_q->f('total_length');
  }
  
  $nb_worked_days = array_sum($c_working_days);
  $tot_width = 500;
  $col_width = $tot_width / $nb_worked_days;

  $first_day = time_first_day_week($time['date']);
  $curr_day = $first_day + $c_dst_err;
  $last_day = $first_day + 7*86400;

  // we loop through days
  while ($curr_day < $last_day) {
    $wday = date('w', $curr_day);
    $day = date('d', $curr_day);
    $hum_wday = $l_daysofweekshort[$wday];

    if (!(isset($result[$wday]))) {
      $result[$wday] = 0;
    }

    if ($c_working_days[$wday] != 0) {
      $line1 .= "
        <td>$hum_wday $day</td>";
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
// Display the planning for a month
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $time[] : time hash
///////////////////////////////////////////////////////////////////////////////
function dis_time_planning_month($obm_q, $time) {
  global $c_working_days, $c_week_first_day, $c_day_fraction, $l_fill;

  $col_width = 20;
  $time_length = 0;
  $time_worked = 0;
  $span = 0;
  $nb_worked_days = array_sum($c_working_days);

  $year = substr($time['date'],0,4);
  $month = substr($time['date'],4,2);

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
    $array_res["$fday"]['length'] = 0;
    $array_res["$fday"]['dow'] = $dow;
    $array_res["$fday"]['obm_dow'] = $obm_dow;
    $day++;

    $day_in_sec += 86400;
    $dow = date('w', $day_in_sec);
    $obm_dow = ($dow - $c_week_first_day + 7) % 7;
  }

  while ($obm_q->next_record())  {
    $day = date('d', $obm_q->f('date'));
    $status = $obm_q->f('timetask_status');
    $array_res["$day"]['length'] = $obm_q->f('total_length');
  }

  // Display ------------------------------------------------------------------
  $month_length = count($array_res);

  // Display the array
  while (list($day,$val) = each($array_res)) {
    // Not working days
    if ($c_working_days[$val['dow']] == 0) {
      $line1 .= "<td width=\"$col_width\" class=\"timeOffday\">$day</td>\n";
      $line2 .= "<td class=\"timeOffday\">&nbsp;</td>\n";

    } else {
      // Working days
      $line1 .= "<td width=\"$col_width\" class=\"timeWorkday\">$day</td>\n";

      if ($val['length'] == $c_day_fraction) {
	$class = "class=\"timeValid\"";
      } else {
	$class = "class=\"timeUnvalid\"";
      }
      $line2 .= "
        <td $class>".$val['length']."/$c_day_fraction</td>";
    }

    // handle the 3rd line
    if ($val['obm_dow'] == 0) {
      $time_length = 0;
      $time_worked = 0;
      $span = 1;
    } else {
      if ($c_working_days[$val['dow']] == 1) {
	$time_length += $c_day_fraction;
      }
      $time_worked += $val['length'];
      $span += 1;
    }

    // last day of the week 
    if (($val['obm_dow'] == 6) or ($day == $month_length)) {
      $week = get_time_week_point($year.$month.$day);
      $url = url_prepare("time_index.php?action=index&amp;date=".$week[0]);
      
      if ($time_length != $time_worked) {
	$class = "class=\"timeUnvalid\"";
      } else {
	$class = "class=\"timeValid\"";
      }
      
      $line3 .= "
          <td $class colspan=\"$span\"><a href=\"$url\">$l_fill</a></td>";
    }
  }
  
  $block = "
  <div class=\"timeCenter\">
    <table class=\"timeMonth\">
    <tr>
      $line1
    </tr>
    <tr>
      $line2
    </tr>
    <tr>
      $line3
    </tr>
    </table>
  <div>";

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
    //$obm_q = run_query_time_project_month_planning($time);
    //$block =  dis_time_project_planning($obm_q, $time) ;
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
  global $l_daysofweekfirst;

  $year = substr($time['date'],0,4);
  $month = substr($time['date'],4,2);

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
    $array_res["$fday"]['length'] = 0;
    $array_res["$fday"]['dow'] = $dow;
    $array_res["$fday"]['obm_dow'] = $obm_dow;
    $day++;

    $day_in_sec += 86400;
    $dow = date('w', $day_in_sec);
    $obm_dow = ($dow - $c_week_first_day + 7) % 7;
  }
  $day_amount = $day - 1;
  $planning = array();
  $task_days = array();
  $line = array();
  $css = array();
  while ($obm_q->next_record())  {
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
    $projects[$type.$project_id] = "<abbr title=\"$label\">$shortname</abbr>";
    $users[$u_id] = $obm_q->f('userobm_firstname').' '. $obm_q->f('userobm_lastname');
    $planning[$u_id][$type.$project_id]["$day"] = $obm_q->f('timetask_length')/$c_day_fraction;
    $planning[$u_id][$type.$project_id]['length'] += $obm_q->f('timetask_length')/$c_day_fraction;
  }

  while (list($day,$val) = each($array_res)) {
    $lday = $l_daysofweekfirst[$val['dow']];
    
    $line0 .= "<th><a href=\"$path/time/time_index.php?action=index&amp;date=$year-$month-$day\">$lday</a></th>\n";
    $line1 .= "<td>$day</td>\n";
    
    foreach($planning as $u_id => $user_planning) {
      foreach($user_planning as $project_id => $work) {
        if(isset($work[$day])) {
          $length = $work[$day]*100;
          $line[$u_id][$project_id] .= "<td title=\"$length%\"><div style=\"width:$length%;\">&nbsp;</div></td>";
        } else {
          $line[$u_id][$project_id] .= '<td></td>';
        }
      }
    }
  }
  foreach($planning as $u_id => $user_planning) {
    foreach($user_planning as $project_id => $work) {
      $line[$u_id][$project_id] = "<tr class=\"$project_id\">
        <th>".$projects[$project_id].'</th>'.$line[$u_id][$project_id]."
        <th>&nbsp;</th><td>".$work['length'].'</td></tr>';
      $color = randomColor();
      $css[$project_id] .= ".$project_id th {color:$color;} \n.$project_id div {background-color:$color;}";
    }  
  }
  foreach($line as $u_id => $l) {
    $d_l .= "<tr><th colspan=\"".($day_amount+3)."\"><br/>".$users[$u_id]."<br/>&nbsp;</th></tr>".
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
  <colgroup class=\"main\" span=\"$day_amount\">
  </colgroup> 
  <colgroup class=\"total\" span=\"2\">
  </colgroup>    
  <thead>
  <tr><th></th>$line0</tr>  
  <tr><th></th>$line1</tr>
  </thead>
  $d_l
  </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display TimeManager User search Form
// Parameters:
//   - $time[] : default form values
//   - $usr_q  : DBO : UserObm list
//   - $multi  : if false, mono select else select multi
///////////////////////////////////////////////////////////////////////////////
function dis_user_select($time, $usr_q, $multi=false) {
  global $l_see, $l_user, $l_all;

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
        <input type=\"hidden\" name=\"action\" value=\"$act\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_see\" />
        $dis_button_all
    </fieldset>
    </form>
    </div>
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
  $d_start_week = time_first_day_week($time['date']);  
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

	$ch_day .= "<input type=\"checkbox\" name=\"cb_day[$ddate]\" $dis_attr />$dlabel\n";
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
       <input type=\"submit\" value=\"$l_update\" />
      ";

    // Script that prefill values (only if tasktype has project and tasks)
    if ($is_proj > 0) {
      $dis_select_defaults = "
        fill_projecttask(document.f_add_task.sel_projecttask, \"$is_proj\");
        select_default(document.f_add_task.sel_project, $is_proj);
        select_default(document.f_add_task.sel_projecttask, \"$is_projtask\");
";
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
      $array_res[$cpt]['name'] = $f_name . ' ' . $l_name;
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
  global  $c_working_days, $c_day_fraction;
  global $l_see, $l_fill, $l_valid, $l_unvalid, $l_admin_valid;
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
    <tbody>
    $dis_months
    </tbody>
    </table>";

  return $block;
}





///////////////////////////////////////////////////////////////////////////////
// Display the Stats TimeManager result page
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_statsuser($statproj_q, $stattt, $time) {
  global $cdg_param, $obm;
  global $l_proj_stats, $l_no_proj_stats;
  global $l_tt_stats, $l_no_tt_stats;
  global $l_tasktype, $l_total,$l_total_period, $l_total_before, $l_total_after;

  if ($statproj_q->num_rows() != 0) {
    $prefs_p = get_display_pref($obm['uid'], 'time_proj');

    $dis_projuser_list = new OBM_DISPLAY('DATA', $prefs_p, 'time', 'time_proj');
    $dis_projuser_list->data_set = $statproj_q;
    $dis_projuser_list->data_order = false;
    $dis_projuser_list->data_page = false;
    $dis_projuser_list->data_header = 'top';

    $block .= "<h1>$l_proj_stats</h1>";
    $block .= $dis_projuser_list->display('dis_time_data');
  } else {
    $block .= display_warn_msg($l_no_proj_stats);
  }
  
  /*  if ($stattt_q->num_rows() != 0) {
    $prefs_t = get_display_pref($obm['uid'], 'time_tt');

    $dis_ttuser_list = new OBM_DISPLAY('DATA', $prefs_t, 'time', 'time_tt');
    $dis_ttuser_list->data_set = $stattt_q;
    $dis_ttuser_list->data_order = false;
    $dis_ttuser_list->data_page = false;
    $dis_projuser_list->data_header = 'top';

    $block .= "<h1>$l_tt_stats</h1>";
    $block .= $dis_ttuser_list->display('dis_time_data');
  } else {
    $block .= display_warn_msg($l_no_tt_stats);
    }*/

  if (is_array($stattt)) {
    foreach($stattt as $tt_id => $tt) {
      if ($tt_id != '0') {
        $cpt++;
        if($cpt % 2 == 0) {
          $class = "class=\"pair\"";
        } else {
          $class = '';
        }
	$label = $tt['label'];
	$time = $tt['time'];
	$time_before = $tt['time_before'];
	$time_after = $tt['time_after'];
	// $amount = number_format($pu['amount']);
	$dis_tt_array .= "
      <tr $class>
        <td>$label</td>
        <td style=\"text-align: right;\">$time&nbsp;</td>
        <td style=\"text-align: right;\">$time_before&nbsp;</td>
        <td style=\"text-align: right;\">$time_after&nbsp;</td>
      </tr>";
      }
    }

    $total_time = $stattt[0]['time_total'];
    $total_time_before = $stattt[0]['time_before_total'];
    $total_time_after = $stattt[0]['time_after_total'];
    
    $dis_stat_tt = "
    <h1>$l_tt_stats</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>$l_tasktype</th>
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
// Show a date in human format
//  params : $p_date : date in yyyymmdd format
//////////////////////////////////////////////////////////////////////////
function dis_time_human_date($p_date) {
  global $cdg_sql, $l_monthsofyear;
    
  $p_year=substr($p_date,0,4);
  $p_month=substr($p_date,4,2);
  $p_day=substr($p_date,6,2);
   
  $mois = $l_monthsofyear[$p_month-1];

  return "$p_day $mois $p_year";

}


?>
