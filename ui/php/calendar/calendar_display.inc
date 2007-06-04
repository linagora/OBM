<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : calendar_display.inc                                         //
//     - Desc : Calendar Display File                                        //
// 2002-11-26 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display the navigation bar
// Parameters:
//   - $calendar        : calendar parameters
//   - $sel_entity_id : array of entity Id ["user"] ["resource"]
//     by reference as it can restrict the user selection
//   - $p_view        : view selected "year", "month", "week" or "day"
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_calendar_view($calendar, &$sel_entity_id, $p_view="week") {
  global $display, $path, $ccalendar_max_users, $obm;

  $group_view = $calendar["entity"]["group_view"];
  $entity_readable = get_calendar_entity_readable();
  if ($p_view == "planning") {
    $visible_entities_id = slice_calendar_entities($sel_entity_id, $ccalendar_max_users, $entity_readable);
  } else {
    $visible_entities_id = slice_calendar_entities($sel_entity_id, $ccalendar_max_users,  $entity_readable);
  }

  $sel_entity_id = $visible_entities_id;
  // we restore the selected group for session storage
  $sel_entity_id["group_view"] = $group_view;

  $calendar_entity = store_calendar_entities(run_query_calendar_get_entity_label($sel_entity_id));
  $writable_entity = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write", "", "userobm");
  $display["features"] = html_calendar_planning_bar($calendar, $calendar_entity, $entity_readable);
  if ($p_view == "year") {
    $block .= dis_calendar_year_planning($calendar, $calendar_entity,$writable_entity);
  } else if ($p_view == "month") {
    $block .= dis_calendar_month_planning($calendar, $calendar_entity,$writable_entity);
  } else if ($p_view == "week") {
    $block .= dis_calendar_week_planning($calendar, $calendar_entity,$writable_entity);
  } else if ($p_view == "day") {
    $block .= dis_calendar_day_planning($calendar, $calendar_entity,$writable_entity);
  } else if ($p_view == "list") {
    $block .= dis_calendar_list_planning($calendar, $calendar_entity,$writable_entity);
  }
  $block .= dis_calendar_quick_form($calendar);
  $display["print_url"] = "$path/calendar/calendar_index.php?action=view_$p_view&amp;date=".$calendar["date"];
  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display the navigation bar
// Parameters:
//   - $calendar: calendar parameters
//   - $calendar_entity : array of entities elements to display
//   - $entity_readable : array of readable entities (["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function html_calendar_planning_bar($calendar, $calendar_entity, $entity_readable) {
  global $action;
  global $output_target;

  $output_target = $calendar["output_target"];

  // If come from view (eg: month), keep view for links else set to week
  if ( ($action == "view_day") || ($action == "view_week")
    || ($action == "view_month") || ($action == "view_year")
    || ($action == "planning") || $action == "view_list") {
    $new_action = $action;
  } else {
    $new_action = "view_week";
  }

  $block = html_calendar_bar_legend($new_action, $calendar_entity,$calendar);
  if ($action != "perform_meeting"  && $output_target != "print") {
    $block .= html_calendar_select_calendar_entity($new_action, $calendar, $calendar_entity, $entity_readable);
    $block .= html_calendar_bar_date_selector($calendar);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the navigation bar
// Parameters:
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function html_calendar_bar_legend($new_action, $calendar_entity,$calendar) {
  global $ico_user, $ico_resource;
  global $l_meeting_legend, $l_possible, $l_free, $l_occupied;
  global $l_calendar_displayed, $l_resource_displayed,$c_all;
  global $action,$ico_delete,$output_target;

  // Legend
  $iso_date = $calendar["date"];
  if ($action == "perform_meeting" || $action == "perform_res_meeting") {
    $l_legend = $l_meeting_legend;
    $dis_legend = "
    <li class=\"freeBusy\">$l_possible</li>
    <li class=\"free\">$l_free</li>
    <li class=\"busy\">$l_occupied</li>";
  } else {
    $l_legend = $l_calendar_displayed;
    foreach ($calendar_entity as $type => $entity) {
      foreach ($entity as $id => $data) {      
        $data_id = "data-$type-$id";        
        $dis_legend .= "
          <li class=\"".$data["class"]."\"><img src=\"".${"ico_$type"}."\" alt=\"[$type]\" />
          <input value=\"$data_id\" id=\"$data_id\" name=\"sel_ent[]\" type=\"hidden\" />";
        if ($output_target != "print") {
          $dis_legend .= "<input type=\"image\" src=\"$ico_delete\"
          onclick=\"\$('$data_id').remove(); this.form.submit();\" />";
        }
        $dis_legend .= " &nbsp;".$data["name"]."
          </li>";
      }
    }
  }

  $block = "
  <div class=\"portlet\" id=\"calendarLegend\">
  <h1>$l_legend</h1>
  <form action=\"\" onsubmit=\"return false;\">
  <ul>
    $dis_legend
  </ul>
  <input type=\"hidden\" name=\"date\" value=\"$iso_date\" />
  <input type=\"hidden\" name=\"group_view\" value=\"$c_all\" />
  <input type=\"hidden\" name=\"new_group\" value=\"1\" />
  <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
  <input type=\"hidden\" name=\"new_sel\" value=\"1\" />    
  </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the "View type" navigation bar component (User, Resource)
// Parameters:
//   - $new_action      : action to be passed
//   - $calendar          : hash values
//   - $calendar_entity : array of entities elements to display
//   - $entity_readable : array of readable entities (["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function html_calendar_select_calendar_entity($new_action, $calendar, $calendar_entity, $entity_readable) {
  global $path, $obm, $c_all;
  global $ccalendar_resource;
  global $l_selection, $l_users,$l_resources, $l_validate,$l_search_users,$l_category_filter;
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2;
  global $ico_calendar_user3,$ico_calendar_user4,$ico_calendar_user5,$l_search_resources;
  global $l_group_members, $l_select_group,$l_back_mono, $l_private, $l_public;

  $iso_date = $calendar["date"];
  $this_group = $calendar["entity"]["group_view"];
  $category_filter = $calendar["category_filter"];
  //-- Group selection
  if ($this_group == $c_all) {
    $sel_group = "<option selected=\"selected\">$l_select_group</option>";
  }

  $sel_group .= "<optgroup label=\"$l_private\">";
  $first_public = true;
  while ($entity_readable["group"]->next_record()) {
    $g_id = $entity_readable["group"]->f("group_id");
    $g_priv = $entity_readable["group"]->f("group_privacy");
    $g_name = $entity_readable["group"]->f("group_name");

    if ($first_public && $g_priv != "1") {
      $sel_group .= "</optgroup>
      <optgroup label=\"$l_public\">\n";
      $first_public = false;
    }
    if ($g_id == $this_group) {
      $selected_s = "selected=\"selected\"";
    } else {
      $selected_s = "";
    }
    $sel_group .= "<option value=\"calendar_index.php?action=$new_action&amp;date=$iso_date&amp;group_id=$g_id&amp;new_group=1\" $selected_s>$g_name</option>\n";
  }
  $sel_group .= "</optgroup>
  </select>";
  //-- End Group selection

  // loop through ["user"], ["resource"]
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
      $name = $data["name"];
      $data_id = "data-$type-$id";
      $div_id = "sel_ent-$data_id";
      $sel_ent .= "<input value=\"$data_id\" name=\"sel_ent[]\" type=\"hidden\" />";
    }
  }


  $user_text = phpStringToJsString($l_search_users);

  if ($ccalendar_resource) {
    $resource_text = phpStringToJsString($l_search_resources);
    $dis_block_resource = "
    </fieldset>
    <fieldset class=\"detail extra\">
    <legend>           
      $l_resources
    </legend>       
    <input type=\"text\" name=\"\" value=\"\" id=\"resource\" autocomplete=\"off\" />
    ";
    $resource_js = "new obm.AutoComplete.Search('$path/resource/resource_index.php?action=ext_search&ajax=1', 'sel_ent', 'resource', {restriction:'user', defaultText:'$resource_text'});";
  }
  $cats1 = of_category_get_ordered("calendar", "category1");
  $sel_cat1 = of_category_html_select("calendar", "category1", $cats1, $category_filter,"all_f","onchange=\"this.form.submit()\"","category_filter");

  $dis_back_myview = "<a href=\"calendar_index.php?action=$new_action&amp;sel_category_filter=&amp;new_group=1&amp;group_view=$c_all&amp;sel_user_id[]=$obm[uid]&amp;date=$iso_date\">$l_back_mono</a>";

  $block = "
    <div class=\"portlet\" id=\"calendarSelection\">
    <h1>$l_selection</h1>
    <div>
    <form action=\"\">
    <fieldset class=\"detail extra\">
    <legend>$l_category_filter</legend>
      $sel_cat1 
      <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
      <input type=\"hidden\" name=\"date\" value=\"$iso_date\" />
    </fieldset>
    </form>
    <form action=\"\" onsubmit=\"return false;\">
    <fieldset class=\"detail extra\">
    <legend>$l_group_members</legend>
    <select name=\"action\" class=\"calendarQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
    $sel_group
    </select>
    </fieldset>
    </form>

    <form action=\"calendar_index.php\" method=\"get\">
    <fieldset class=\"detail extra\">
    <legend>
    $l_users
    </legend>
    <input type=\"text\" name=\"\" value=\"\" onfocus=\"\" onblur=\"\" id=\"user\" autocomplete=\"off\" />
    <input type=\"hidden\" name=\"date\" value=\"$iso_date\" />
    <input type=\"hidden\" name=\"group_view\" value=\"$c_all\" />
    <input type=\"hidden\" name=\"new_group\" value=\"1\" />
    <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
    <input type=\"hidden\" name=\"new_sel\" value=\"1\" />
    $dis_block_resource
    </fieldset>
    <div id=\"sel_ent\">
    </div>
    <fieldset class=\"buttons\">
    $sel_ent
    <input type=\"submit\" value=\"$l_validate\" />
    </fieldset>
    $dis_back_myview
    </form>
   </div>
  </div>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_ent', 'user', {restriction:'calendar', defaultText:'$user_text'})
      $resource_js
    });
    </script>    
    
    ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the navigation date selector 
// Parameters:
//   - $calendar: calendar parameters
///////////////////////////////////////////////////////////////////////////////
function html_calendar_bar_date_selector($calendar) {
  global $ccalendar_weekstart,$ico_ok;
  global $l_go, $l_go_to, $l_daysofweekfirst;

  $iso_date = $calendar["date"];
  $ts_date = strtotime($iso_date);

  $this_month = of_date_get_month($ts_date);
  $this_year = of_date_get_year($ts_date);

  $check_week = $ts_date;
  $start_week_time = get_calendar_date_day_of_week(strtotime("$this_year-01-01"), $ccalendar_weekstart);
  $end_week_time = $start_week_time + (6 * 25 * 60 * 60);

  $month_time = strtotime("$this_year-01-01");
  $year_time = strtotime(($this_year-3)."-01-01");
  $dis_month = calendar_localizeDate ("month", $ts_date);
  $start_week_day = strtotime($ccalendar_weekstart);
  $start_day = get_calendar_date_day_of_week(strtotime("$this_year-$this_month-01"), $ccalendar_weekstart);

  $whole_month = TRUE;

  // Week Selection
  do {
    $weekdate = of_isodate_format($start_week_time);
    $select_week1 = calendar_localizeDate("week_jump", $start_week_time);
    $select_week2 = calendar_localizeDate("week_jump", $end_week_time);
    if (($check_week >= $start_week_time) && ($check_week <= $end_week_time)) {
      $sel_week .= "
        <option value=\"calendar_index.php?action=view_week&amp;date=$weekdate\" selected=\"selected\">$select_week1 - $select_week2</option>";
    } else {
      $sel_week .= "
        <option value=\"calendar_index.php?action=view_week&amp;date=$weekdate\">$select_week1 - $select_week2</option>";
    }
    $start_week_time = strtotime ("+1 week", $start_week_time);
    $end_week_time = $start_week_time + (6 * 25 * 60 * 60);
  } while (of_date_get_year($start_week_time) <= $this_year);

  // Month Selection
  for ($i=0; $i<12; $i++) {
    $monthdate = of_isodate_format($month_time);
    $month_month = date("m", $month_time);
    $select_month = calendar_localizeDate("month", $month_time);
    if ($month_month == $this_month) {
      $sel_month .= "<option value=\"calendar_index.php?action=view_month&amp;date=$monthdate\" selected=\"selected\">$select_month</option>\n";
    } else {
      $sel_month .= "<option value=\"calendar_index.php?action=view_month&amp;date=$monthdate\">$select_month</option>\n";
    }
    $month_time = strtotime ("+1 month", $month_time);
  }

  // Minicalendar Head
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekfirst[$day_num];
    $dis_minical_head .= "<td>$day</td>\n";
    $start_week_day = strtotime("+1 day", $start_week_day);
  }

  // Minicalendar
  $i = 0;
  do {
    if ($i == 0) $dis_minical .= "<tr>\n";
    $day = date ("j", $start_day);
    $iso_day = of_isodate_format($start_day);
    $check_month = of_date_get_month($start_day);

    if ($check_month != $this_month) {
      $dis_minical .= "<td class=\"downlight\" onclick=\"window.location.href='calendar_index.php?action=view_day&amp;date=$iso_day'\"
        onmouseout=\"this.className='downlight'\" onmouseover=\"this.className='hover'\">
        $day
        </td>";
    } else {
      if (of_isodate_format() == $iso_day) {
        $dis_minical .= "<td class=\"highlight\" onclick=\"window.location.href='calendar_index.php?action=view_day&amp;date=$iso_day'\" 
          onmouseout=\"this.className='highlight'\" onmouseover=\"this.className='hover'\">
          $day
          </td>";
      } else {
        $dis_minical .= "<td onclick=\"window.location.href='calendar_index.php?action=view_day&amp;date=$iso_day'\" 
          onmouseout=\"this.className=''\" onmouseover=\"this.className='hover'\">
          $day
          </td>";
      }
    }
    $start_day = strtotime("+1 day", $start_day);
    $i++;
    if ($i == 7) {
      $dis_minical .= "</tr>\n";
      $i = 0;
      $checkagain = of_date_get_month($start_day);
      if ($checkagain != $this_month) $whole_month = FALSE;
    }
  } while ($whole_month == TRUE);

  $block = "
  <div class=\"portlet\" id=\"calendarMiniCal\">
    <h1>$dis_month</h1>
    <div>
      <table class=\"miniCalendar\">
      <thead>
      <tr>$dis_minical_head</tr>
      </thead>
      <tbody>
      $dis_minical
      </tbody>
      </table>
    </div>
  </div>

  <div class=\"portlet\" id=\"calendarGoTo\">
  <h1>$l_go_to</h1>
  <form action=\"\" class=\"detail\" onsubmit=\"return false;\">
    <select name=\"actionWeek\" class=\"calendarQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
    $sel_month
    </select>
    <input type=\"image\" src=\"$ico_ok\" value=\"$l_go\" onclick=\"window.location=(this.form.actionWeek.options[this.form.actionWeek.selectedIndex].value);return false;\" />
    <br />
    <select name=\"actionMonth\" class=\"calendarQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
    $sel_week
    </select>
    <input type=\"image\" src=\"$ico_ok\" value=\"$l_go\" onclick=\"window.location=(this.form.actionMonth.options[this.form.actionMonth.selectedIndex].value);return false;\" />
  </form>
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
// Parameters:
//   - $calendar          : calendar parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_year_planning($calendar, $calendar_entity,$writable_entity) {
  global $ico_left_day,$ico_right_day;
  global $ccalendar_weekstart;
  global $l_daysofweek,$l_daysofweekshort,$ico_calendar_nouser;  
  global $l_private, $l_previous, $l_next;

  $iso_date = $calendar["date"];
  $category_filter = $calendar["category_filter"];
  $ts_date = strtotime($iso_date);  
  $this_year = of_date_get_year($ts_date);
  $start_time = strtotime("$this_year-01-01", $ts_date);
  $end_time = strtotime("+1 year",$start_time);
  $prev_year = of_isodate_format(strtotime("-1 year", $start_time));
  $next_year = of_isodate_format($end_time);
  $start_day = strtotime($ccalendar_weekstart);
   
  $display_date = calendar_localizeDate("month", $ts_date);  
  
  $events = calendar_events_model($start_time,$end_time, $calendar_entity,$category_filter);
  
  for ($i=0; $i<7; $i++) {
    $day_num = date("w","$start_day");
    $day = $l_daysofweekshort[$day_num];
    $dis_day .= "<td class=\"calendarHead2\" style=\"width: 30px;\">$day</td>";
    $start_day = strtotime("+1 day", $start_day); 
  }
  
  $m = 0;	
  $n = 0;
  $ts_minical = $start_time;

  do {
    $minical_month = of_date_get_month($ts_minical);
    $minical_year = of_date_get_year($ts_minical);
    $ts_month = strtotime("$minical_year-$minical_month-01");
    $iso_month = of_isodate_format($ts_month);
    $start_day = get_calendar_date_day_of_week($ts_month, $ccalendar_weekstart);
    
    $i = 0;
    $whole_month = TRUE;
    $num_week = 0;
    $dis_calendar .= "
    <td style=\"width:210px\">
      <table class=\"calendarCal\">
      <tr>
        <td colspan=\"7\" class=\"calendarHead2\">
          <a class=\"calendarLink\" href=\"calendar_index.php?action=view_month&amp;date=$iso_month\">
      " . calendar_localizeDate ("month", $ts_minical) . "
          </a>
        </td>
      </tr>
      <tr>
        $dis_day
      </tr>
      <tr>
        <td colspan=\"7\">
        <table class=\"calendarYear\">";
    do {
      $day = date ("j", $start_day);
      $iso_day = of_isodate_format($start_day);
      $check_month = of_date_get_month($start_day);
      $dis_event_cal = "";

      if (isset($events[$iso_day])) {
	$dayObj = $events[$iso_day];
	$dis_event_cal ="<table class=\"calendarEventYear\"><tr>";
	$tr_users = 0;

	// loop through ["user"], ["resource"]
	foreach ($calendar_entity as $type => $entity) {
	  foreach ($entity as $id => $data) {
	    $events_data = $dayObj->get_events($id,$type);
	    $days_events = $dayObj->day_events;
	    if ($tr_users == 3) {
	      $dis_event_cal .= "</tr><tr>";   
	    }
	    if (is_array($events_data) || is_array($days_events[$type][$id])) {
	      $dis_event_cal .= "<td class=\"calendarEventYear\">";
	      $dis_event_cal .= "<img src=\"".$data["image"]."\" alt=\"\"/></td>";
	    } else {
	      $dis_event_cal .= "<td class=\"calendarEventYear\"><img src=\"$ico_calendar_nouser\" alt=\"\"/></td>";		
	    }
	    $tr_users++;	   
	  }
	}
	$dis_event_cal .="</tr></table>";	

      }

      if ($check_month != $minical_month) $day = "$day";
      if ($i == 0) $dis_calendar .= "<tr>";
      if ($iso_day == of_isodate_format()) {
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"calendarMonthOn\"
              onmouseover=\"this.style.backgroundColor='#dddddd'\" 
	      onmouseout=\"this.style.backgroundColor='#f4e5a9'\"
	      onclick=\"window.location.href='calendar_index.php?action=view_day&amp;date=$iso_day'\">
	  <a class=\"calendarLink\" href=\"calendar_index.php?action=view_day&amp;date=$iso_day\">$day</a>$dis_event_cal
          </td>";
      }	
      elseif ($check_month == $minical_month) {	   
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"calendarMonthReg\" 
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#ffffff'\" 
	      onclick=\"window.location.href='calendar_index.php?action=view_day&amp;date=$iso_day'\">
          <a class=\"calendarLink\" href=\"calendar_index.php?action=view_day&amp;date=$iso_day\">$day</a>$dis_event_cal
          </td>";
      } else {
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"calendarMonthOff\"
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#f2f2f2'\"
	      onclick=\"window.location.href='calendar_index.php?action=view_day&amp;date=$iso_day'\">
	  <a class=\"calendarLink2\" href=\"calendar_index.php?action=view_day&amp;date=$iso_day\">$day</a>$dis_event_cal
          </td>";
      }
      $start_day = strtotime("+1 day", $start_day); 
      $i++;
      if ($i == 7) { 
	$num_week++; 
	$dis_calendar .= "</tr>";
        $i = 0;
        $checkagain = of_date_get_month($start_day);
        if ($checkagain != $minical_month) $whole_month = FALSE;	
      }
    } while ($whole_month == TRUE);

    if ($num_week < 6) {
      $dis_calendar .= "<tr><td style=\"height:30px;\" class=\"calendarMonthOff\" colspan=\"7\"></td></tr>";
    }
    $ts_minical = strtotime ("+1 month", $ts_minical);
    $dis_calendar .= "</table></td></tr></table></td>\n";
    if ($m < 2) $dis_calendar .= "<td style=\"width: 20px;\" ></td>";
    $m++;
    $n++;
    if (($m == 3) && ($n < 12)) {
      $m = 0;
      $dis_calendar .= "</tr><tr><td style=\"height: 10px;\" colspan=\"5\"></td></tr><tr>\n";
    }
  } while (($m < 3) && ($n < 12)); 
  
  
  $block = "
  <div class=\"calendarMain\">
  <table class=\"calendarCalYear\">
  <tr>
    <td class=\"calendarHead\">
      <a href=\"calendar_index.php?action=view_year&amp;date=$prev_year\"><img src=\"$ico_left_day\" alt=\"$l_previous\" /></a>
    </td>
    <td class=\"calendarHead\" colspan=\"3\">
      $this_year
    </td>
    <td class=\"calendarHead\">
      <a href=\"calendar_index.php?action=view_year&amp;date=$next_year\"><img src=\"$ico_right_day\" alt=\"$l_next\" /></a>
    </td>
  </tr> 
  <tr>
    <td style=\"height: 10px;\" colspan=\"5\"></td>
  </tr>
  <tr>
    $dis_calendar
  </tr>
  </table>  
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the month
// Parameters:
//   - $calendar: calendar parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_month_planning($calendar, $calendar_entity,$writable_entity) { 
  global $ico_previous, $ico_next;
  global $ccalendar_weekstart;
  global $ico_resize, $ico_periodic, $ico_meeting;
  global $ccalendar_first_hour, $ccalendar_last_hour;
  global $l_private,$l_daysofweek, $l_previous, $l_next;

  $iso_date = $calendar["date"];
  $category_filter = $calendar["category_filter"];
  $ts_date = strtotime($iso_date);
  $this_month = of_date_get_month($ts_date);
  $this_year = of_date_get_year($ts_date);
  $start_month = strtotime("$this_year-$this_month-01");
  $start_time = get_calendar_date_day_of_week($start_month, $ccalendar_weekstart);
  // days to add to first day displayed to complete month view
  // 6 (to have complete month) + first days of next month
  $next_month = of_isodate_format(strtotime("+1 month",$ts_date));
  $prev_month = of_isodate_format(strtotime("-1 month",$ts_date));

  $display_date = calendar_localizeDate("month", $ts_date);
  $start_week_day = strtotime($ccalendar_weekstart);
  $whole_month = TRUE;
  $nb_user = count($calendar_entity["user"]);
  $nb_res = count($calendar_entity["resource"]);
  $nb_ent = $nb_user + $nb_res;
  $td_width = floor(100 / $nb_ent)."px";


  // Display the day of a week
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweek[$day_num];
    $dis_day_head .= "<td>$day</td>";
    $start_week_day = strtotime("+1 day", $start_week_day);
  }

  $current_time = $start_time;
  $i = 0;
  do {
    $this_day = date("j", $current_time);
    $iso_day = of_isodate_format($current_time);
    $check_month = of_date_get_month ($current_time);
    if ($check_month != $this_month) {
      $bgclass="class=\"downlight\"";
    } elseif (of_isodate_format(time()) == $iso_day) {
      $bgclass="class=\"highlight\"";
    } else {
      $bgclass="";
    } 
    if ($i == 0) {
      $week_num = get_calendar_week_num($current_time); 
      $dis_month_cal .= "
      <tr>
        <th>
        <a href=\"calendar_index.php?action=view_week&amp;date=$iso_day\" >$week_num</a>
        </th>";
    }

    $dis_month_cal .= "<td id=\"day-".($current_time - $start_time)."\" $bgclass>
      <h2><a href=\"calendar_index.php?action=view_day&amp;date=$iso_day\">$this_day</a></h2>";
    $dis_month_cal .= "</td>";
    $current_time = strtotime("+1 day", $current_time); 
    $i++;
    if ($i == 7) { 
      $dis_month_cal .= "</tr>\n";
      $i = 0;
      $checkagain = of_date_get_month($current_time);
      if ($checkagain != $this_month) $whole_month = FALSE;	
    }
  } while ($whole_month == TRUE);   

  $occurrences = calendar_events_model($start_time, $current_time, $calendar_entity,$category_filter);
  
  if (is_array($occurrences)) {
    $week_duration = 86400*7;
    $options = array("xUnit:86400","yUnit:$week_duration",
                     "context:obm.calendarManager.bodyContext");
    foreach($occurrences as $occurrence) {
      $dis_event .= dis_calendar_event($occurrence, "day",$calendar_entity,$writable_entity,$options);
    }
  } 

  $block = "
  <h1>
    <a href=\"calendar_index.php?action=view_month&amp;date=$prev_month\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $display_date
    <a href=\"calendar_index.php?action=view_month&amp;date=$next_month\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
  </h1> 
  <table class=\"calendar month\">
    <colgroup span=\"1\" />
    <colgroup span=\"7\" id=\"calendarEventContext\" />
    <thead>
    <tr><td></td>$dis_day_head</tr>
    </tr>    
    </thead>
    <tbody id=\"calendarBody\"  onmouseup=\"obm.calendarQuickForm.compute(event, 'month');\">
    $dis_month_cal
    </tbody>
  </table>
  <script type=\"text/javascript\">
    obm.vars.images.resize = '$ico_resize';
    obm.vars.images.periodic = '$ico_periodic';
    obm.vars.images.meeting = '$ico_meeting';
    showWaitingPanel('calendarBody');
    obm.initialize.chain(function () {
      obm.calendarManager = new Obm.CalendarManager($start_time);
      obm.calendarManager.lock();
      $dis_event
      obm.calendarManager.unlock();
      obm.calendarManager.resizeWindow();
      discardWaitingPanel();
    });
  </script>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
// Parameters:
//    - $calendar          : calendar parameters
//    - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_week_planning($calendar, $calendar_entity,$writable_entity) {
  global $path, $ico_next, $ico_previous, $l_previous, $l_next;
  global $ccalendar_weekstart, $set_cal_interval,$ico_meeting;
  global $ico_resize, $ico_periodic, $ico_meeting;
  global $ccalendar_first_hour, $ccalendar_last_hour;
 
  $ts_date = strtotime($calendar["date"]);
  $category_filter = $calendar["category_filter"];
  $time_unit = 60 / $set_cal_interval;
  $time_unit_sec = 3600 / $set_cal_interval;
  $week_num = get_calendar_week_num($ts_date);
  $next_week = of_isodate_format(strtotime("+1 week", $ts_date));
  $prev_week = of_isodate_format(strtotime("-1 week", $ts_date));
  
  $start_week_day = get_calendar_date_day_of_week($ts_date, $ccalendar_weekstart);
  $end_week_time = $start_week_day + ((6 * 24 + $ccalendar_last_hour) * 3600);
  $start_week_time = strtotime("+$ccalendar_first_hour hours",$start_week_day);
  
  $start_week = calendar_localizeDate("week", $start_week_time);
  $end_week = calendar_localizeDate("week", $end_week_time);
  
  $display_date = "$start_week - $end_week"; 
  
  $day_duration = 86400;
  $k = 0; 
  // Week Days headers : loop day by day
  for ($ts = $start_week_time; $ts < $end_week_time; $ts = strtotime("+1 day",$ts) ) {
    $iso_date = of_isodate_format($ts);
    $this_date_l = calendar_localizeDate("week_list", $ts);
    $week_day_list .= "
    <td>
      <a href=\"calendar_index.php?action=view_day&amp;date=$iso_date\">$this_date_l</a>
    </td>";
    $week_day_event_list .= "
    <th id=\"day-".($ts - $start_week_time)."\">
    </th>
      ";
    $k++;
  }

  // Week Calendar content
  for ($j = $ccalendar_first_hour; $j < $ccalendar_last_hour; $j++) {
    
    $delta = $j - $ccalendar_first_hour;
    for ($k=0; $k<$set_cal_interval; $k++) {
      if ($k==0) {
        $week_cal .= "\n<tr><th>".($j).":00</th>";
      } else {
        $week_cal .= "\n<tr><th></th>";
      }
      $delta2 = $k * $time_unit;
      for ($i=0; $i<7; $i ++) {
        $current_time = strtotime("+$i days +$delta hours +$delta2 minutes",$start_week_time);
        $goto = "calendar_index.php?action=new&amp;tf_date_begin=".of_isodate_format($current_time,"",1)."&amp;tf_date_end=".of_isodate_format($current_time + $time_unit_sec,"",1);
        $iso_date = of_isodate_format($current_time);
        $week_cal .= "<td id=\"time-$current_time\">$display_events&nbsp;</td>";
        $display_events = '';
      }
      $week_cal .= "</tr>";
    }
  }
  $occurrences = calendar_events_model($start_week_day,strtotime("+7 days",$start_week_day),$calendar_entity,$category_filter);
  
  if(is_array($occurrences)) {
    foreach($occurrences as $occurrence) {
      $dis_event .= dis_calendar_event($occurrence, "hour",$calendar_entity,$writable_entity);
    }
  }
  
  $block = "
  <h1>
    <a href=\"calendar_index.php?action=view_week&amp;date=$prev_week\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $display_date
    <a href=\"calendar_index.php?action=view_week&amp;date=$next_week\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
  </h1> 
  <table class=\"calendar week\">
    <colgroup />
    <colgroup span=\"7\" id=\"calendarEventContext\" />
    <thead onmouseup=\"obm.calendarQuickForm.compute(event, 'week');\">
    <tr>
    <td>$week_num</td>
    $week_day_list
    </tr>
    <tr id=\"calendarHead\">
    <td>&nbsp;</td>
    $week_day_event_list
    </tr>    
    </thead>
    <tbody id=\"calendarBody\" onmouseup=\"obm.calendarQuickForm.compute(event, 'week');\">
    $week_cal
    </tbody>
  </table>
  <script type=\"text/javascript\">
    obm.vars.images.resize = '$ico_resize';
    obm.vars.images.periodic = '$ico_periodic';
    obm.vars.images.meeting = '$ico_meeting';
    obm.vars.consts.timeUnit = $time_unit_sec;
    obm.initialize.chain(function () {
      obm.calendarManager = new Obm.CalendarManager($start_week_time);
      obm.calendarManager.lock();
      $dis_event
      obm.calendarManager.unlock();
      obm.calendarManager.resizeWindow();
    });
  </script>";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the day
// Parameters:
//   - $calendar         : calendar parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_day_planning($calendar, $calendar_entity,$writable_entity) {
  global $ico_previous, $ico_next,$ico_resize,$ico_meeting;
  global $ccalendar_first_hour, $ccalendar_last_hour,$set_cal_interval;
  global $ico_resize, $ico_periodic, $ico_meeting;
  global $l_private, $l_all_day, $l_previous, $l_next;

  $iso_date = $calendar["date"];
  $category_filter = $calendar["category_filter"];
  $ts_date = strtotime($iso_date);

  $tomorrow_date = of_isodate_format(strtotime("+1 day", $ts_date));
  $yesterday_date = of_isodate_format(strtotime("-1 day", $ts_date));

  $time_unit = 60 / $set_cal_interval;
  $time_unit_sec = 3600 / $set_cal_interval;
  $week_num = get_calendar_week_num($ts_date);

  $start_time = strtotime("+$ccalendar_first_hour hours",$ts_date);
  $end_time = strtotime("+$ccalendar_last_hour hours",$ts_date);

  $display_date = calendar_localizeDate("day", $ts_date);

  $nb_user = count($calendar_entity["user"]);
  $nb_res = count($calendar_entity["resource"]);
  $nb_ent = $nb_user + $nb_res;
  
  $day = $events[$iso_date];

  $days_events = $day->day_events;

  // Display Calendar
  for ($i = $ccalendar_first_hour; $i < $ccalendar_last_hour; $i++) {
    for ($k=0; $k<$set_cal_interval; $k++) {
      if ($k==0) {
	$day_cal .= "\n<tr><th>$i:00</th>"; 
      } else {
	$day_cal .= "\n<tr><th></th>"; 
      }
      $delta2 = $k * $time_unit;
      $current_time = strtotime("+$i hours +$delta2 minutes",$ts_date);   

      $day_cal .= "<td id=\"time-$current_time\"></td></tr>";
    }
  }

  $occurrences = calendar_events_model($ts_date,strtotime("+1 day", $ts_date), $calendar_entity,$category_filter);
  if(is_array($occurrences)) {
    $options = array();
    foreach($occurrences as $occurrence) {
      $dis_event .= dis_calendar_event($occurrence, "hour",$calendar_entity,$writable_entity,$options);
    }
  }
  $block = "
  <h1>
    <a href=\"calendar_index.php?action=view_day&amp;date=$yesterday_date\"><img src=\"$ico_previous\" alt=\"[$l_Previous]\" /></a>     
    $display_date
    <a href=\"calendar_index.php?action=view_day&amp;date=$tomorrow_date\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
  </h1> 
  <table class=\"calendar day\">
    <colgroup span=\"1\" />
    <colgroup span=\"1\" id=\"calendarEventContext\" />
    <thead onmouseup=\"obm.calendarQuickForm.compute(event, 'day');\">
    <tr id=\"calendarHead\">
    <td>$week_num</td><th id=\"day-0\"></th>
    </tr>    
    </thead>
    <tbody id=\"calendarBody\" onmouseup=\"obm.calendarQuickForm.compute(event, 'day');\">
    $day_cal
    </tbody>
  </table>
  <script type=\"text/javascript\">
    obm.vars.images.resize = '$ico_resize';
    obm.vars.images.periodic = '$ico_periodic';
    obm.vars.images.meeting = '$ico_meeting';
    obm.vars.consts.timeUnit = $time_unit_sec;
    obm.initialize.chain(function () {
      obm.calendarManager = new Obm.CalendarManager($start_time);
      obm.calendarManager.lock()
      $dis_event
      obm.calendarManager.unlock();
      obm.calendarManager.resizeWindow();
    });
  </script>";

  return $block;
}


/**
 * Display Event List
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @return event list HTML code
 */
function dis_calendar_list_planning($calendar, $calendar_entity, $writable_entity) {
  global $ico_previous, $ico_next, $l_previous, $l_next, $l_desc;
  global $l_daysofweek, $l_monthsofyear, $l_attendees, $l_title, $l_location;
  global $l_category,$l_datebegin, $l_dateend, $l_private, $l_repeat;
  global $l_daily, $l_weekly, $l_monthlybydate, $l_monthlybyday, $l_yearly;
  global $l_date, $l_resource, $l_event, $ico_search;
  global $l_expand_all, $l_collapse_all;

  $sel_user = $calendar_entity["user"];
  $sel_user_id = array();
  if (is_array($sel_user)) {
    while(list($key, $val) = each($sel_user)) {
     $sel_user_id[$key] = $val["class"];
    }
  }

  $sel_res = $calendar_entity["resource"];
  $sel_res_id = array();
  if (is_array($sel_res)) {
    while(list($key, $val) = each($sel_res)) {
     $sel_res_id[$key] = $val["class"];
    }
  }

  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
       
    }    
  }

  $iso_date = $calendar["date"];
  $category_filter = $calendar["category_filter"];
  $ts_date = strtotime($iso_date);
  $this_month = of_date_get_month($ts_date);
  $this_year = of_date_get_year($ts_date);
  $start_month = strtotime("$this_year-$this_month-01");
  $end_month = strtotime("+1 month", $start_month);
  $start_time = $start_month;

  $next_month = of_isodate_format(strtotime("+1 month",$ts_date));
  $prev_month = of_isodate_format(strtotime("-1 month",$ts_date));

  $list_title = calendar_localizeDate("month", $ts_date);
  $occurrences = calendar_events_model($start_month, $end_month,$calendar_entity,$category_filter, true);
  $today_mark = false;
  $charset = mb_internal_encoding();

  // Sort occurrences by date (cause repeat and no repeat events)
  if (is_array($occurrences)) {
    foreach($occurrences as $occurrence) {
      $d = strtotime(date("Y-m-d", $occurrence->date));
      if(!is_array($ocs[$d])) { $ocs[$d] = array();}
      array_push($ocs[$d],$occurrence);
    }
  }
  if (is_array($ocs)) {
    ksort($ocs);
    foreach($ocs as $key => $occurrence) {
      if ($i%2 == 0) {
        $class = "class=\"pair\"";
      } else {
        $class = "";
      }
      $i ++;
      
      $day_text_evt = of_date_in_letter($key);
      $day_evt = date("Y-m-d", $key);
      $today = strtotime(date("Y-m-d"));
      if ($today == $key) {
        $class="class=\"highlight\"";
      }

      $dis_events .= "
        <tr $class>
          <td><a href=\"calendar_index.php?action=view_day&date=$day_evt\">$day_text_evt</a></td>
          <td>";

      foreach($occurrence as $k => $v) {
        $event = $v->event;
        $date = $v->date;
        $hour_begin = of_date_get_full_hour($date);
        $ev_id = $event->id;
	      $title = $event->title;
        $duration = $event->duration;
        $location = $event->location;
        $attendee = $event->attendee;
        $category = $event->category1;
        $repeat = $event->repeat;
        if ($repeat == "none") {
	        $dis_repeat = "";
        } else {
	        $dis_repeat = ${"l_$repeat"};
        }

        if ($event->privacy == 1 && !($updatable || in_array($obm["uid"],$attendee["user"]))) {
	        $title = $l_private;
	        $description = $title;
        } else {
	        $title = $event->title;
	        $explode_desc = explode("|", $event->description);
          $description = $explode_desc[0];
          $item_list = "";
          $dis_res = "";
          $separator = "";
          if ($explode_desc[1] != "") {
            $item = "<?xml version='1.0' encoding='$charset'?>".$explode_desc[1];
            $xml = parse_item($item);
            // $xml = simplexml_load_string($item);
            if (is_object($xml)) {
              foreach($xml->resource as $key => $value) {
                if (array_key_exists($value->res_id, $sel_res_id)) {
                  $dis_res = "$separator$value->res_name";
                  $separator = ", ";
                  // $dis_res .= "<div style=\"width: 11px; height: 11px; float: left;\" class=\"".$sel_res_id[$value->res_id]."\">&nbsp;</div>";
                }
                $item_list .= "<b>$value->res_name:</b><br/>".nl2br($value->item)."$value->property_name:$value->property_value";
              }
            }
          }
        }
        $dis_attendees = "";
        $br = "";
        $dis_user = "";
        $separator = "";
        foreach($attendee["user"] as $at_id => $at_name) {
          if (array_key_exists($at_id, $sel_user_id)) {
            $dis_user .= "$separator$at_name";
            $separator = ", ";
            // $dis_user .= "<div style=\"width: 11px; height: 11px; float: left;\" class=\"".$sel_user_id[$at_id]."\">&nbsp;</div>";
          }
	        $dis_attendees .= "$br$at_name";
	        $br = "<br />";
        }
        $date_begin = of_datetime_format($date);
        $date_end = of_datetime_format($date+$duration); 
        $j++;
	$js .= "
	div = $('evt-$date-$ev_id');
	a = div.getFirst();
	info = $('eventDetail-$date-$ev_id');
	slides[$j] = new Fx.Slide(info, {duration: 150,wait:false});	
	slides[$j].hide();
	info.setStyle('display','block');	
	a.addEvent('click', function(e){
	  slides[$j].toggle();
     	});
				";
        $dis_events .= "<div id=\"evt-$date-$ev_id\" >
          <a href=\"javascript: void(0);\"><span style=\"float: right;\">$dis_user $dis_res</span>
          $hour_begin - <b>$title</b></a>
          <div id=\"eventDetail-$date-$ev_id\" class=\"eventDetail detail\" style=\"display: none;\">
            <table>
              <tr>
                <th>$l_datebegin</th><td>$date_begin</td>
              </tr>
              <tr>
                <th>$l_dateend</th><td>$date_end</td>
              </tr>
              <tr>
                <th>$l_repeat</th><td>$dis_repeat</td>
              </tr>
              <tr>
                <th>$l_category</th><td>$category</td>
              </tr>
              <tr>
                <th>$l_location</th><td>$location</td>
              </tr>
              <tr>
                <th>$l_desc</th><td>$description</td>
              </tr>
              <tr>
                <th>$l_attendees</th><td>$dis_attendees</td>
              </tr>
              <tr>
                <th>$l_resource</th><td>$item_list</td>
              </tr>
            </table>
            <a href=\"calendar_index.php?action=detailconsult&amp;calendar_id=$ev_id\"><img src=\"$ico_search\" title=\"[Info]\"/></a>
          </div>
          </div>";
      }
      $dis_events .= " </td>
      </tr>";
    }
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <h1>
    <a href=\"calendar_index.php?action=view_list&amp;date=$prev_month\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $list_title
    <a href=\"calendar_index.php?action=view_list&amp;date=$next_month\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
  </h1>
  <a href=\"#\" onclick=\"display_list_detail('show'); return false;\">$l_expand_all</a> - 
  <a href=\"#\" onclick=\"display_list_detail('hide'); return false;\">$l_collapse_all</a>
  <table class=\"spreadSheet eventList\">
    <thead>
    <tr>
      <th>$l_date</th>
      <th>$l_event</th>
    </tr>
    </thead>
    $dis_events
  </table>
	<script type='text/javascript'>
		var slides = new Array();
		$js
	</script>
";

  return $block;
}


/**
 * Perform HTML and javascript code for events. 
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @access public
 * @return event HTML and javascript code
 */
function dis_calendar_event($occurrence, $unit, $calendar_entity,$writable_entity, $options=array()) {
  global $obm, $ccalendar_first_hour, $ccalendar_last_hour;
  global $ico_resize, $ico_periodic, $ico_meeting;
  global $l_user, $l_resource, $l_task, $l_private;
  global $output_target;

  $event = $occurrence->event;
  $owner = $event->owner;

  if ($owner == $obm["uid"] || in_array($owner,$writable_entity["ids"])) {
    $updatable =  true;
    $ed[] = "updatable:true";
  }
  
  if ($output_target == "print") {
    $updatable =  false;
    $ed[] = "updatable:false";
  }

  $attendee = $event->attendee;
  if ($event->privacy == 1 && !($updatable || in_array($obm["uid"],$attendee["user"]))) {
    $title = phpStringToJsString($l_private);
    $description = $title;
    $location = $title;
    $category = $title;      
  } else {
    $title = phpStringToJsString($event->title);
    $explode_desc = explode("|", $event->description);
    $description = phpStringToJsString($explode_desc[0]);
    if ($explode_desc[1] != "") {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset'?>".$explode_desc[1];
      $xml = parse_item($item);
//    $xml = simplexml_load_string($item);
      if (is_object($xml)) {
        foreach($xml->resource as $key => $value) {
          $item_list .= "
            <b>$value->res_name: </b><br/>
            ".nl2br($value->item)."<br/>";
        }
      }
      $item_list = phpStringToJsString(str_replace("\n\r", "<br>", $item_list));
    }
    $location = phpStringToJsString($event->location);
    $category = phpStringToJsString($event->category1);  
  }

  $duration = $event->duration;
  $date = $occurrence->date;
  $id = $event->id;
  $entity_id = $occurrence->id;
  $entity = $occurrence->entity;
  $all_day = $event->all_day;
  $entityClass = $calendar_entity[$entity][$entity_id]["class"];

  $ed[] = "id:$id";
  $ed[] = "entity:'$entity'";
  $ed[] = "entity_id:$entity_id"; 
  $ed[] = "duration:$duration";
  $ed[] = "item:'$item_list'";
  $ed[] = "time:$date";
  $ed[] = "title:'$title'";
  $ed[] = "all_day:$all_day";
  $ed[] = "klass:'$entityClass'"; 
  $ed[] = "description:'$description'";
  $ed[] = "location:'$location'";
  $ed[] = "category:'$category'";
  
  $attendees = array();
  foreach ($attendee as $entity => $entities) {
    $label_entity = phpStringToJsString(${"l_$entity"});
    $ent = array();
    foreach($entities as $at) {
      $ent[] = phpStringToJsString($at);
    }
    $attendees[] = "{label:'".$label_entity."', entities:['".implode("','",$ent)."']}"; 
  }
  $ed[] = "attendees:[".implode(',',$attendees)."]";

  switch($unit)  {
    case "day" :
      $day = true;
      if($updatable) {
        $options[] = "draggable:true"; 
      }
      break;
    case "hour" :
      $day = ($all_day == 1) || (date('Ymd',$date + $duration) != date('Ymd',$date));
      $day = $day || (date('H',$date) <  $ccalendar_first_hour);
      $day = $day || (date('H',$date) >  $ccalendar_last_hour );
      if($updatable) {
        $options[] = "draggable:true"; 
        $options[] = "resizable:true"; 
      }
      break;
  }

  if ($event->isMeeting()) {
    $ed[] = "meeting:true";
  } else {
    $ed[] = "meeting:false";
  }
  if ($event->isPeriodic()) {
    $ed[] = "periodic:true";
  } else {
    $ed[] = "periodic:false";
  }
  if ($all_day) {
    $head .= $title;
  } else {
    $head .= date("H:i",$date);
  }
  
  $opt = "{".implode(',',$options)."}";
  $event_data =  "{".implode(',',$ed)."}";
  if($day) {
    $display_events = "        
        obm.calendarManager.newDayEvent($event_data,$opt);
      ";
  } else {
    $display_events = "
        obm.calendarManager.newEvent($event_data,$opt);
      ";
  }
  return $display_events;
} 


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the month
// Parameters:
//   - $calendar: calendar parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_plain_month_planning($calendar, $calendar_entity) { 
  global $ico_previous,$ico_next;
  global $ccalendar_weekstart, $ico_new_event,$l_all_day;
  global $ccalendar_first_hour, $ccalendar_last_hour;
  global $l_private,$l_daysofweekfirst,$l_daysofweek, $l_previous, $l_next;

  $iso_date = $calendar["date"];
  $category_filter = $calendar["category_filter"];
  $ts_date = strtotime($iso_date);  
  $this_month = of_date_get_month($ts_date);
  $this_year = of_date_get_year($ts_date);
  $start_time = strtotime("$this_year-$this_month-01");
  $end_time = strtotime("+1 month", $start_time);
  $next_month = of_isodate_format(strtotime("+1 month",$ts_date));
  $prev_month = of_isodate_format(strtotime("-1 month",$ts_date));

  $display_date = calendar_localizeDate("month", $ts_date);  
  $start_week_day = strtotime($ccalendar_weekstart);

  $nb_user = count($calendar_entity["user"]);
  $nb_res = count($calendar_entity["resource"]);
  
  $nb_ent = $nb_user + $nb_res;
   
  $day_duration = 86400;

  $events = calendar_events_model($start_time,$end_time, $calendar_entity,$category_filter);
  $of = &OccurrenceFactory::getInstance();

  $current_time = $start_time;
  $day_amount = 0;
  while($current_time < $end_time) {
    $day_amount++;
    $day_num = date("w", $current_time);
    $this_day = date("j", $current_time);
    $day_label = $l_daysofweekfirst[$day_num];
    $day_long_label = $l_daysofweek[$day_num];
    $iso_day = of_isodate_format($current_time);

    $empty = !$of->periodHaveOccurrences($current_time,$day_duration);
    $line0 .= "<th>$day_label</th>\n";
    $line1 .= "<th><a href=\"calendar_index.php?action=view_day&amp;date=$iso_day\">$this_day</a></th>\n";
    foreach ($calendar_entity as $type => $entity) {
      foreach ($entity as $id => $properties) {
        $haveevent = false;
        $line_id = $type." ".$id;
        if($empty) {
          $line[$line_id] .= "<td></td>";
        } else {
          $today_events = array();
          $occurences = $of->getPeriodOccurrences($current_time,$day_duration,array($type => array($id)));
          if ($occurences != null) {
            $haveevent = true;
            foreach ($occurences as $occurence) {
              $event = &$occurence->event;
              if($event->all_day) {
                $ad = ": $l_all_day";
              } else {
                $ad = "";
              }
              if ($event->privacy == 1 && $obm["uid"] != $properties["id"]) {
                $today_events[] = "$l_private $ad";
              } else {
                $today_events[] = $event->title." $ad";
              }
            }            
          }
          if($haveevent) {
            $hidden_div .= "
              <div id=\"$id-$this_day\" class=\"infoPopup\">
               <h1>$day_long_label $this_day</h1>
               <ul>
               <li>
               ".implode("</li><li>",$today_events)."
                </li>
               </ul>
              </div>";            
            $line[$line_id] .= "<td class=\"".$properties["class"]."\"
               onmouseover=\"toggleInfo(this,'$id-$this_day'); return true;\"
               onmouseout=\"toggleInfo(this,'$id-$this_day'); return true;\"></td>";
          } else {
            $line[$line_id] .= "<td></td>";
          }
        }
      }
    }
    $current_time = strtotime("+1 day", $current_time); 
  }
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $properties) {
      $line_id = $type." ".$id;
      $d_l .= "<tr><th>".$properties["name"]."</th>".$line[$line_id]."</tr>";
    }  
  } 

  $block = "
  <style>
    $css
  </style>
  <h1>
    <a href=\"calendar_index.php?action=planning&amp;date=$prev_month\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $display_date
    <a href=\"calendar_index.php?action=planning&amp;date=$next_month\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
  </h1>   
  <table class=\"planning\">
  <colgroup>
  </colgroup>
  <colgroup class=\"main\" span=\"$day_amount\">
  </colgroup> 
  <colgroup class=\"main\" span=\"2\">
  </colgroup>    
  <thead>
  <tr><th></th>$line0</tr>  
  <tr><th></th>$line1</tr>
  </thead>
  $d_l
  </table>
  <script type=\"text/javascript\">
    function toggleInfo(container,id) {
      var resource = \$(id);
      var container = \$(container);
      if(resource) {
        if(resource.getStyle('display') == 'block') {
          resource.setStyle('display','none');       
        } else {
          resource.setStyle('left',(container.getLeft()+container.offsetWidth)+'px')   
          resource.setStyle('top',(container.getTop()+container.offsetHeight)+'px')   
          resource.setStyle('display','block');
        }
      } 
    }
  </script>  
  $hidden_div
    ";    

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Conflicts
// Parameters:
//   - $calendar : calendar hash
//   - $c_q    : conflict list (events)
///////////////////////////////////////////////////////////////////////////////
function html_calendar_dis_conflict($calendar, $c_q) {
  global $l_title, $l_location, $l_conflicts, $l_datebegin,$l_dateend, $l_user;
  global $l_force_insert,$l_refuse_insert,$l_cancel_insert,$l_validate;

  while ($c_q->next_record()) {
    $id = $c_q->f("calendarevent_id"); 
    $title = $c_q->f("calendarevent_title");
    $location = $c_q->f("calendarevent_location");
    $entity = $c_q->f("evententity_entity");
    if ($entity == "user") {
      $name = $c_q->f("userobm_lastname")." ".$c_q->f("userobm_firstname");
    } else if ($entity == "resource") {
      $name = $c_q->f("resource_name");
    }
    $date_begin = $c_q->f("calendarevent_date");
    $all_day = $c_q->f("calendarevent_allday");
    $date_end = $c_q->f("calendarevent_date") + $c_q->f("calendarevent_duration");

    if ($all_day) {
      $dis_all_day = "<tr><td colspan=\"2\" class=\"detailLabel\">$l_all_day</td></tr>";
      $date_b = of_isodate_format($date_begin);
      $date_e = of_isodate_format($date_end);
    } else {
      $date_b = of_datetime_format($date_begin);
      $date_e = of_datetime_format($date_end);
    }       
    $dis_conflict .= "
      <tr>
        <td>$title</td>
        <td>$location</td>
        <td>$date_b</td>
        <td>$date_e</td>
        <td>$name</td>";

     $dis_conflict .= "</tr>";
  }
  $block = "
  $dis_form_begin
  <div class=\"detail extra\">
  <h1>$l_conflicts</h1>
  <table class=\"spreadSheet\">
  <thead>
   <tr>
    <th>$l_title</th>
    <th>$l_location</th>
    <th>$l_datebegin</th>
    <th>$l_dateend</th>
    <th>$l_user</th>    
    </tr>
    </thead>
    <tbody>
    $dis_conflict
    </tbody>
  </table>
  <div class=\"buttons\">
   $dis_form_button
  </div>

  $dis_form_end";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Conflicts
// Parameters:
//   - $calendar:
//   - $conflict:
//   - $event_id: 
//   - $force: 
///////////////////////////////////////////////////////////////////////////////
function html_calendar_conflict_form($calendar) {
  global $l_force_insert, $l_refuse_insert, $l_cancel_insert;
  
  $event_id = $calendar["calendar_id"]; 
  $user_id = $calendar["user_id"];
  $dis_form = "
  <form name=\"f_conflict\" method=\"post\">
   <input type=\"hidden\" name=\"calendar_id\" value=\"$event_id\" />
   <input type=\"hidden\" name=\"action\" value=\"decision\" />
   <input type=\"hidden\" name=\"user_id\" value=\"$user_id\" />
   <input type=\"hidden\" name=\"cba_force\" value=\"1\" />
   <input type=\"hidden\" name=\"rd_decision_event\" value=\"\" />
   <div class=\"detailButton\">
    <input type=\"submit\" value=\"$l_force_insert\" onclick=\"this.form.rd_decision_event.value='A';\"/>
    <input type=\"submit\" value=\"$l_refuse_insert\" onclick=\"this.form.rd_decision_event.value='R';\"/> 
    <input type=\"submit\" value=\"$l_cancel_insert\" onclick=\"this.form.rd_decision_event.value='W';\"/>   
   </div>
  </form>";

  return $dis_form; 
}


///////////////////////////////////////////////////////////////////////////////
// Display Event Consultation
// Parameters:
//   - $id : event id
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_event_consult($id) {
  global $display, $l_err_reference;

  if ($id > 0) {
    $eve_q = run_query_calendar_detail($id);
    if ($eve_q->num_rows() == 1) {
      $entities = get_calendar_event_entity($id);
      $exception_q = run_query_get_one_event_exception($id);
      $display["detailInfo"] = display_record_info($eve_q);
      $block = html_calendar_event_consult($eve_q, $exception_q, $entities);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML : Display Calendar Event Consult
// Parameters:
//   - $event_q  : event_query result
//   - $entities : entities linked to the event ["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function html_calendar_event_consult($event_q, $exception_q, $entities) {
  global $obm, $display, $ccalendar_weekstart, $ccalendar_resource, $l_items;
  global $l_users, $l_resources, $l_title, $l_location, $l_priority, $l_private, $l_desc,$l_high;
  global $l_low,$l_medium,$l_datebegin,$l_day,$l_month,$l_week;
  global $l_dateend,$l_insert,$l_update,$l_repeat;
  global $l_change_state,$l_groups,$l_all_day,$l_every;
  global $l_repeatkind,$l_repeatdays,$l_date_repeatend; 
  global $l_wait, $l_task, $l_exception, $l_yes, $l_no;
  global $l_carac,$l_module_calendar;
  global $l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse,$l_repeatfrequency;

  $id = $event_q->f("calendarevent_id");
  $writable_users = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write", "", "userobm");
  $writable_resources = of_right_entity_for_consumer("Resource", "user", $obm["uid"], "write");
  
  if ($event_q->f("calendarevent_privacy") == 0 || 
      $event_q->f("calendarevent_owner") == $obm["uid"] ||
      in_array($event_q->f("calendarevent_owner"),$writable_users["ids"])) {

    $title = $event_q->f("calendarevent_title");
    $location = $event_q->f("calendarevent_location");
    $cats1 = of_category_get_entitycategories("calendar", "category1", $id, "mono");
    $priority = $event_q->f("calendarevent_priority"); 
    $explode_desc = explode("|", $event_q->f("calendarevent_description"));
    $description = $explode_desc[0];
    if ($explode_desc[1] != "") {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset'?>".$explode_desc[1];
    } else {
      $item = "";
    }

    $block_category1 = of_category_dis_block_consult("calendar", "category1", $cats1, "mono");
    switch ($priority ) {
      case 1 :
        $priority = $l_low;
        break;
      case 2 :
        $priority = $l_medium;
        break;
      case 3 :
        $priority = $l_high;
        break;
    }  
    $tasks = $entities["task"];
    if (is_array($tasks["entity"])) {
      foreach ($tasks["entity"] as $u_id => $entity) {
        $label = $entity["label"];
        $state = $entity["state"];
        $dis_task_list .= "$label<br />";
      }
      $dis_task_block = "
        <tr>
          <th>$l_task :</th>
          <td>$dis_task_list</td>
        </tr>
      ";
    }
    $display["title"] = "<div class=\"title\">$l_module_calendar : $title</div>";
  } else {
    $title = $l_private; 
    $location = $l_private;
    $priority = $l_private;
    $description = $l_private;
    $priority = $l_private;
    $display["title"] = "<div class=\"title\">$l_module_calendar : $l_private</div>";

  }

  $all_day = $event_q->f("calendarevent_allday");
  if ($all_day) {
    $dis_all_day = "<tr><th>$l_all_day</th><td></td></tr>";
    $datebegin = of_isodate_format($event_q->f("calendarevent_date"));
    $dateend = of_isodate_format($event_q->f("calendarevent_date") + $event_q->f("calendarevent_duration"));
  } else {
    $datebegin = of_datetime_format($event_q->f("calendarevent_date"));
    $dateend = of_datetime_format($event_q->f("calendarevent_date") + $event_q->f("calendarevent_duration"));
  }
  $privacy = $event_q->f("calendarevent_privacy") == 1? $l_yes : $l_no;


  if($event_q->f("calendarevent_repeatkind") != "none") {
    $kind = $GLOBALS["l_".$event_q->f("calendarevent_repeatkind")];
    $kind_unit = $GLOBALS["l_".$event_q->f("calendarevent_repeatkind")."_unit"];
    $repeat_days = $event_q->f("calendarevent_repeatdays"); 
    $repeat_end = of_date_format($event_q->f("calendarevent_endrepeat"));
    if( $event_q->f("calendarevent_repeatfrequence") == 1 ) {
      $repeatfrequency = sprintf($kind_unit,"");
    } else {
      $repeatfrequency = sprintf($kind_unit,$event_q->f("calendarevent_repeatfrequence"));
    }

    while($exception_q->next_record()) {
      $date_exception[] = of_isodate_format($exception_q->f("calendarexception_date")); 
    }
    if($event_q->f("calendarevent_repeatkind") == "weekly") {
      $start_week_day = strtotime($ccalendar_weekstart);
      for ($i=0; $i<7; $i++) {
        $day_num = date("w", $start_week_day);
        $day = $l_daysofweekshort[$day_num];
        if (strcmp(substr($repeat_days,$i,1),"1")==0) {
          $dis_repeat_days .= "$day ";
        }
        $start_week_day = strtotime("+1 day", $start_week_day); 
      }
      $dis_repeat_days = "
      <tr>  
       <th>$l_repeatdays :</th>
       <td>$dis_repeat_days</td>
      </tr>        
      ";
    }
    $repeat_block = "
    <div class=\"detail infos\">
     <h1>$l_repeat</h1>
     <table>   
      <tr>    
       <th>$l_repeatkind :</th> 
       <td>$kind</td>
      </tr>
      <tr>    
       <th>$l_repeatfrequency :</th> 
       <td>$repeatfrequency</td>
      </tr>
      <tr>    
       <th>$l_date_repeatend :</th> 
       <td>$repeat_end</td>
      </tr>
      $dis_repeat_days
      <tr>
        <th>$l_exception :</th>
        <td>$block_exception_detail</td>
      </tr>
     </table>
     </div>";
  }
  $attendee = $entities["user"];
  if (is_array($attendee["entity"])) {
    foreach ($attendee["entity"] as $u_id => $entity) {
      $label = $entity["label"];
      $state = $entity["state"];
      if ($obm["uid"] == $u_id || in_array($u_id,$writable_users["ids"])) {
        $$state = "checked=\"checked\"";
        $url= "$path/calendar/calendar_index.php?action=update_decision&calendar_id=$id&entity_kind=user&entity_id=$u_id";
        $dis_user_list .= "
        <span class=\"B I RF\">
        <input $A type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"A\" onclick=\"window.location='$url&rd_decision_event=A'\" />A
	<input $W type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"W\" onclick=\"window.location='$url&rd_decision_event=W'\" />W
        <input $R type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"R\" onclick=\"window.location='$url&rd_decision_event=R'\" />R
        </span>
        <span class=\"RC\">$label</span>
        <hr />
        ";
        $$state = "";
      } else {
        $dis_user_list .= "<span class=\"B I RF\">$state</span><span class=\"RC\">$label</span><hr />";
      }
    }
  }


  if ($ccalendar_resource) {


    // item
    if ($item != "") {
        $xml = parse_item($item);
//      $xml = simplexml_load_string($item);
      if (is_object($xml)) {
        foreach($xml->resource as $key => $value) {
          $property["$value->res_id"] = "$value->property_name : $value->property_value";
          $dis_item_list .= "
          <tr>
            <th>$value->res_name :</th>
            <td>".nl2br($value->item)."</td>
          </tr>";
        }
      }
    }

    $res = $entities["resource"];
    if (is_array($res["entity"])) {
      foreach ($res["entity"] as $r_id => $entity) {
        $label = $entity["label"];
        $state = $entity["state"];
        if (in_array($r_id,$writable_resources["ids"])) {
          $$state = "checked=\"checked\"";
          $url= "$path/calendar/calendar_index.php?action=update_decision&calendar_id=$id&entity_kind=resource&entity_id=$r_id";
          $dis_resource_list .= "
          <span class=\"B I RF\">
          <input $A type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"A\" onclick=\"window.location='$url&rd_decision_event=A'\" />A
          <input $W type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"W\" onclick=\"window.location='$url&rd_decision_event=W'\" />W
          <input $R type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"R\" onclick=\"window.location='$url&rd_decision_event=R'\" />R
          </span>
          <span class=\"RC\">$label($property[$r_id])</span>
          <hr />
        ";
          $$state = "";
        } else {
          $dis_resource_list .= "<span class=\"B I RF\">$state</span><span class=\"RC\">$label($property[$r_id])</span><hr />";
        }
      }
    }
    $dis_resource_block = "
      <tr>
        <th>$l_resources :</th>
        <td>$dis_resource_list</td>
      </tr>";
  }

  // Exception
  if(is_array($date_exception)) {
    foreach($date_exception as $key => $value) {
     $block_exception_detail .= "$value <br />";
    }
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
  <div class=\"detail infos\">
  <h1>$l_module_calendar</h1>
  <table>
  <tr>
    <th>$l_title :</th>
    <td>$title</td>
  </tr>  
  <tr>
    <th>$l_location :</th>
    <td>$location</td>
  </tr>  
  <tr>
    <th>$l_private :</th>
    <td>$privacy</td>
  </tr> 
  $block_category1
  <tr>
    <th>$l_users :</th>
    <td>$dis_user_list</td>
  </tr>
  $dis_resource_block
  $dis_task_block
  </table>
  </div>
  <div class=\"detail infos\">
  <h1>$l_items</h1>
  <table>
    $dis_item_list
  </table>
  </div>
  <div class=\"detail infos\">
  <h1>$l_carac</h1>
   <table>   
   $dis_all_day
    <tr>
     <th>$l_datebegin :</th>
     <td>$datebegin</td>
    </tr>
    <tr>
     <th>$l_dateend :</th>
     <td>$dateend</td>       
    </tr>
    <tr>
     <th>$l_priority :</th>
     <td>$priority</td>
    </tr>      
   </table>
  </div>
  $repeat_block

   <div class=\"detail extra\">
   <h1>$l_desc</h1>
   $description
   </div>
    $dis_decision_radio
  </form>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the event form
// Parameters:
//   - $action   : action called
//   - $calendar   : calendar parameters
//   - $event_q  : DBO : event result
//   - $entities : entities linked (event) or selected ["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_event_form($action, $calendar, $event_q, $entities) {

  if ($action == "detailupdate") {
    $users = $entities["user"];
    $resources = $entities["resource"];
    $tasks = $entities["task"];
    $exception_q = run_query_get_one_event_exception($calendar["calendar_id"]);
  } else {
    $users_id = $entities["user"];
    if (count($users_id > 0)) {
      $users = get_userobm_from_ids($users_id);
    } else {
      $users = array();
    }
    $res_id = $entities["resource"];
    if (count($res_id > 0)) {
      $resources = get_calendar_resource_from_ids($res_id);
    } else {
      $resources = array();
    }
    $groups_id = $entities["group"];
    if (count($groups_id > 0)) {
      $groups = get_calendar_group_from_ids($groups_id);
    } else {
      $groups = array();
    }
  }

  $cats1 = of_category_get_ordered("calendar", "category1");
  $block = html_calendar_event_form($action, $calendar, $event_q, $exception_q, $users, $resources, $groups,$tasks, $cats1);

  return $block;
}


/**
 * HTML for the quick form popup in the calendar view.
 * The quick form popup is set to quickly add, update or consult
 * calendar events.
 *  
 * @param mixed $calendar calendar parameters 
 * @access public
 * @return HTML
 */
function dis_calendar_quick_form($calendar) {
  global $ico_popup_close,$l_validate,$popup_queue,$l_title;
  global $l_desc,$l_confirm_delete,$l_c_delete,$l_category1;
  global $l_location,$l_members,$l_carac, $l_items,$l_details;

  $block = "
    <div id=\"calendarQuickForm\">
      <h1>
      <a href=\"\" onclick=\"obm.calendarQuickForm.hide();return false;\">
      <img src=\"$ico_popup_close\" alt=\"close\" />
      </a>
      <span id=\"calendarQuickFormDate\"></span>
      </h1>
      <h2>
      <a id=\"calendarQuickFormTitle\" href=\"\" onclick=\"obm.calendarQuickForm.goTo('detailconsult'); return false\">
      </a>
      </h2> 
      <form id=\"calendarQuickFormStore\" onsubmit=\"obm.calendarQuickForm.submit();return false;\">
      <fieldset>
        $l_title : <input type=\"text\" name=\"tf_title\" /> <input class=\"button\" type=\"submit\" value=\"$l_validate\" />
      </fieldset> 
      <fieldset class=\"buttons\"> 
        <input type=\"button\" onclick=\"obm.calendarQuickForm.goTo();\" value=\"$l_details\" />
      <input type=\"button\" 
      onclick=\"if(confirm('$l_confirm_delete')) obm.calendarQuickForm.submit('quick_delete');\"
      value=\"$l_c_delete\" />
      </fieldset>
      </form>
      <div id=\"calendarQuickFormData\">
      <div  id=\"calendarQuickFormAttendees\">
      <h3>$l_members</h3>
      </div>
      <div>
        <h3>$l_carac</h3>
        <table>
        <tr>
        <th>$l_location</th>
        <td id=\"calendarQuickFormLocation\"></td>
        </tr>
        <tr>
        <th>$l_category1</th>
        <td id=\"calendarQuickFormCategory\"></td>
        </tr>
        </table>
      </div>
      <div>
        <h3>$l_desc</h3>
        <p id=\"calendarQuickFormDescription\"></p>
      </div>
      <div>
        <h3>$l_items</h3>
        <p id=\"calendarQuickFormItem\"></p>
      </div>
      </div>
      <img src=\"$popup_queue\" alt=\"\"/>
    </div>
    <script type=\"text/javascript\">
      obm.calendarQuickForm = new Obm.CalendarQuickForm();
    </script>
    ";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $action       : action called
//   - $calendar       : calendar parameters
//   - $event_q      : DBO : event result
//   - $users        : Array : Event users or selected users [ids] [entity]
//   - $res          : Array : Event resources or selected res [ids] [entity]
//   - $groups       : Array : selected groups [ids] [entity]
//   - $cats1        : Array : Calendar categories1
///////////////////////////////////////////////////////////////////////////////
function html_calendar_event_form($action, $calendar, $event_q, $exception_q, $users, $res, $groups,$tasks, $cats1) {
  global $obm, $path, $display;
  global $ccalendar_resource, $ccalendar_weekstart, $set_cal_interval,$ico_project;
  global $ico_mini_cal, $ico_add,$ico_delete, $l_items;
  global $ccalendar_first_hour, $ccalendar_last_hour,$ico_project;
  // -- Labels
  global $l_users, $l_title, $l_location, $l_priority, $l_private, $l_desc;
  global $l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_insert,$l_update,$l_repeat;
  global $l_repeatkind,$l_repeatdays2, $l_date_repeatend, $l_daily, $l_weekly;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_module_calendar;
  global $l_none,$l_force,$l_repeat_update,$l_sendamail,$l_groups,$l_task;
  global $l_calendar_select_group, $l_hour,$l_all_day,$l_repeatfrequency;
  global $l_resources,$l_format,$l_daysofweekshort,$l_task, $l_exception, $l_items;

  $time_unit = 60 / $set_cal_interval;  
  $repeatfrequency = 1;

  if ($action == "detailupdate") {
    $title = htmlspecialchars($event_q->f("calendarevent_title"));
    $location = htmlspecialchars($event_q->f("calendarevent_location"));
    $cat1 = $event_q->f("calendarevent_category1_id");
    $priority = $event_q->f("calendarevent_priority");
    $explode_desc = explode("|", $event_q->f("calendarevent_description"));
    $desc = $explode_desc[0];
    if ($explode_desc[1] != "") {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset'?>".$explode_desc[1];
    } else {
      $item = "";
    }
    $datebegin = $event_q->f("calendarevent_date");
    $hourbegin = date("H",$datebegin);
    $minbegin = date("i",$datebegin);
    $datebegin = of_date_upd_format($datebegin);
    $dateend = $event_q->f("calendarevent_date") + $event_q->f("calendarevent_duration");
    $hourend = date("H",$dateend);
    $minend = date("i",$dateend);
    $dateend = of_date_upd_format($dateend);
    $all_day = $event_q->f("calendarevent_allday");
    $privacy = $event_q->f("calendarevent_privacy");
    $repeat_kind = $event_q->f("calendarevent_repeatkind");
    $repeat_days = $event_q->f("calendarevent_repeatdays");
    $repeatfrequency = $event_q->f("calendarevent_repeatfrequence");
    if ($event_q->f("calendarevent_endrepeat")) {
      $repeat_end = of_date_upd_format($event_q->f("calendarevent_endrepeat"));
    }
    $t_id = $tasks["ids"][0];
    $t_name = $tasks["entity"][$t_id]["label"];
    $id = $event_q->f("calendarevent_id");

    while($exception_q->next_record()) {
      $date_exception[] = of_date_upd_format($exception_q->f("calendarexception_date")); 
    }

  } elseif ($action == "new") {
    $repeat_end = of_date_upd_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($calendar["calendar_id"])) { $id = $calendar["calendar_id"]; }
  if (isset($calendar["title"])) { $title = stripslashes($calendar["title"]); }
  if (isset($calendar["location"])) { $location = stripslashes($calendar["location"]); }
  if (isset($calendar["category1"])) { $cat1 = $calendar["category1"]; }
  if (isset($calendar["priority"])) { $priority = $calendar["priority"]; }
  if (isset($calendar["description"])) { $desc = stripslashes($calendar["description"]); }
  if (isset($calendar["all_day"])) { $all_day = $calendar["all_day"]; }
  if (isset($calendar["repeatfrequency"])) { $repeatfrequency = $calendar["repeatfrequency"]; }  
  if (isset($calendar["item"])) { $item = $calendar["item"]; }  
  if (isset($calendar["date_begin"])) {
    $ts = isodatetime_to_timestamp($calendar["date_begin"]);
    $datebegin = of_date_upd_format($ts);
    $hourbegin = of_date_get_hour($ts);
    $minbegin = date("i", $ts);
  }
  if (isset($calendar["date_end"])) {
    $ts = isodatetime_to_timestamp($calendar["date_end"]);
    $dateend = of_date_upd_format($ts);
    $hourend = of_date_get_hour($ts);
    $minend = date("i", $ts);
  }
  if (isset($calendar["privacy"])) { $privacy = $calendar["privacy"]; }
  if (isset($calendar["repeat_kind"])) { $repeat_kind = $calendar["repeat_kind"]; }
  if (isset($calendar["repeat_days"]) && $calendar["repeat_days"] != "000000") { $repeat_days = $calendar["repeat_days"];}
  if (isset($calendar["repeat_end"])) {
    $repeat_end = of_date_upd_format($calendar["repeat_end"]);
  }
  if (isset($calendar["force"])) { $force = $calendar["force"]; }
  if ($repeat_kind != "weekly") {
    $repeat_days = "000000";
    $repeatDaysDisplay = "none";
  } else {
    $repeatDaysDisplay = "''";
  }
  if (isset($repeat_kind) && $repeat_kind != "none") {
    $repeatDisplay = "''";
    $frequency_block = sprintf($GLOBALS["l_".$repeat_kind."_unit"], "<input type=\"text\" size=\"4\" style=\"width:3em;\" name=\"tf_repeatfrequency\" value=\"$repeatfrequency\" />");
  } else {
    $repeatDisplay = "none";
    $frequency_block = "<input type=\"text\" size=\"4\" style=\"width:3em;\" name=\"tf_repeatfrequency\" value=\"$repeatfrequency\" />";
  }
  if (isset($calendar["task_id"])) { $t_id = $calendar["task_id"]; }
  if (isset($calendar["task_name"])) { $t_name = stripslashes($calendar["task_name"]); }
  if (isset($calendar["task_new_id"])) { $t_new_id = $calendar["task_new_id"]; }
  if (isset($calendar["task_new_name"])) { $t_new_name = stripslashes($calendar["task_new_name"]); }
  if (isset($calendar["date_exception"])) { $date_exception = $calendar["date_exception"]; }
  if (isset($calendar["list-item"])) { $t_list_item = $calendar["list-item"]; }
  if (isset($calendar["list-item-id"])) { $t_list_item_id = $calendar["list-item-id"]; }
  if (isset($calendar["property-name"])) { $t_property_name = $calendar["property-name"]; }
  if (isset($calendar["property"])) { $t_property_value = $calendar["property"]; }
  

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"calendar_id\" id=\"calendar_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $$repeat_kind .= "selected=\"selected\" ";
  $dis_sel_kind .= "
    <select onchange=\"repeat_form($(this).getValue())\" name=\"sel_repeat_kind\">
      <option value=\"none\">$l_none</option>
      <option value=\"daily\" $daily>$l_daily</option>
      <option value=\"weekly\" $weekly>$l_weekly</option>
      <option value=\"monthlybydate\" $monthlybydate>$l_monthlybydate</option>
      <option value=\"monthlybyday\" $monthlybyday>$l_monthlybyday</option>
      <option value=\"yearly\" $yearly>$l_yearly</option>
    </select>";
  switch($priority) {
    case 1 : $tag_low = "selected=\"selected\""; break;
    case 2 : $tag_medium = "selected=\"selected\""; break;
    case 3 : $tag_high = "selected=\"selected\""; break; 
    default :
      $tag_low = "";
      $tag_medium = "selected=\"selected\"";
      $tag_high = ""; 
    break;
  } 
  $dis_sel_prio .= "
    <select name=\"sel_priority\">
      <option value=\"1\" $tag_low>$l_low</option>
      <option value=\"2\" $tag_medium>$l_medium</option>
      <option value=\"3\" $tag_high>$l_high</option>
    </select>";

  // eventcategory select
  $block_category1 = of_category_dis_entity_form("calendar", "category1", $cats1, "mono", $cat1);

 // Repetition days
  $start_week_day = strtotime($ccalendar_weekstart);

  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekshort[$day_num];
    $dis_repeat_day .= "<input type=\"checkbox\" name=\"cba_repeatday_".$i."\" value=\"1\"";
    if (strcmp(substr($repeat_days,$i,1),"1")==0) {
      $dis_repeat_day .= " checked = \"checked\"";
    }
    $dis_repeat_day .= " /> $day";

    $start_week_day = strtotime("+1 day", $start_week_day); 
  }

  if ( $all_day == "1" ) {
    $style = "display:none;";
    $all_day_checked = "checked=\"checked\"";
  }
  
  // userobm select
  if (is_array($users["entity"])) {
    foreach ($users["entity"] as $u_id => $u_ent) {
      $u_name = $u_ent["label"];
      $sel_id = "data-user-$u_id";
      $div_id = "sel_user_id-$sel_id";
      $dis_sel_user .= "<div class=\"elementRow\" id=\"$div_id\">
      <a href=\"javascript: remove_element('$div_id','sel_user_id');\">
      <img src=\"$ico_delete\" alt=\"[Delete]\" />
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }
  if( $datebegin == '' ) {
    $datebegin =  of_date_upd_format(time());
  }
  if( $dateend == '' ) {
    $dateend = $datebegin;
  }
  if( $hourend == '' ) {
    $hourend = $ccalendar_first_hour + 1;
  }

  $block_user = "
    <th>
    $l_users
     </th>
     <td>
       <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" />
     </td>
     </tr>
     <tr>
     <th>&nbsp;</th>    
     <td id=\"sel_user_id\">$dis_sel_user</td>";

  // group select
  if (is_array($groups["entity"])) {
    foreach ($groups["entity"] as $g_id => $g_ent) {
      $g_name = $g_ent["label"];
      $sel_id = "data-group-$g_id";
      $div_id = "sel_group_id-$sel_id";
      $dis_sel_group .= "
        <div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_group_id');\">
        <img src=\"$ico_delete\" alt=\"[Delete]\" />
        </a>
        $g_name
        <input value=\"$sel_id\" name=\"sel_group_id[]\" type=\"hidden\" />
        </div>";
    }
  }

  $block_group = "
    <th>
    $l_groups
     </th>    
     <td>
       <input type=\"text\" name=\"\" value=\"\" id=\"groupSearch\" autocomplete=\"off\" />       
     </td>
     </tr>

     <tr>
     <th>&nbsp;</th>    
     <td id=\"sel_group_id\">$dis_sel_group</td>";

  if ($ccalendar_resource) {
    // resource select
    if (is_array($res["entity"])) {
      foreach ($res["entity"] as $r_id => $r_ent) {
	      $r_name = $r_ent["label"];
	      $sel_id = "data-resource-$r_id";
	      $div_id = "sel_resource_id-$sel_id";

        // Item & Property
        $url_item = "$path/resource/resource_index.php?action=ext_ritem&ext_element=list-item&popup=1&amp;resource_id=$r_id";

        if ($item != "") {
            $xml = parse_item($item);
//          $xml = simplexml_load_string($item);
        }
        if (is_object($xml) && ($action == "detailupdate" || $action == "update" )) {
          foreach($xml->resource as $key => $value) {
            if ($value->res_id == $r_id) {
              $list_item = $value->item;
              $list_item_id = $value->item_id;
              $property_name = $value->property_name;
              $property_value = $value->property_value;
            }
          }
        } else {
          $list_item = $t_list_item[$r_id]; 
          $list_item_id = $t_list_item_id[$r_id]; 
          $property_name = $t_property_name[$r_id];
          if ($property_name == "") {
            $property_name = run_query_calendar_get_resource_property($r_id);
          }
          $property_value = $t_property_value[$r_id];
          if ($property_value == "") {
            $property_value = $property_name;
          }
        }
	      $dis_sel_resource .= "
          <div class=\"elementRow\" id=\"$div_id\">
            <a href=\"javascript: remove_element('$div_id','sel_resource_id');\">
            <img src=\"$ico_delete\" alt=\"[Delete]\" />
            </a>
            $r_name
            <input value=\"$sel_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
            <div>
              <a id=\"a[$r_id]\" href=\"javascript: void(0);\" onclick=\"popup('$url_item', '')\"> 
              $l_items</a><br />
              <textarea id=\"list-item[$r_id]\" name=\"list-item[$r_id]\" readonly>$list_item</textarea>
              <input type=\"hidden\" id=\"list-item-id[$r_id]\" name=\"list-item-id[$r_id]\" value=\"$list_item_id\" />
              <input type=\"hidden\" id=\"res-name[$r_id]\" name=\"res-name[$r_id]\" value=\"$r_name\" />
              <input type=\"hidden\" id=\"res-id[$r_id]\" name=\"res-id[$r_id]\" value=\"$r_id\" />
            </div>
            <div>
              <input type=\"hidden\" name=\"property-name[$r_id]\" id=\"property-name[$r_id]\" value=\"$property_name\" />
              <input type=\"text\" name=\"property[$r_id]\" id=\"property[$r_id]\" value=\"$property_value\" onclick=\"if (this.value=='$property_name') this.value='';\"/>
            </div>
          </div>";
      }
    }

    $block_resource = "
    <th>
    $l_resources
     </th>    
     <td>
       <input type=\"text\" name=\"\" value=\"\" id=\"resourceSearch\" autocomplete=\"off\" />
     </td>
    </tr>

    <tr>
    <th>&nbsp;</th>    
    <td id=\"sel_resource_id\">$dis_sel_resource</td>";
  }
  
  $block_task = "
    <th>$l_task     
    <a href=\"\" onclick=\"url = get_selected_users_url();popup('$path/project/project_index.php?action=ext_get_task_id&amp;popup=1&amp;ext_widget=f_entity.task_new_id&amp;ext_widget_text=f_entity.task_new_name'+url,'Project'); return false;\">
    <img src=\"$ico_project\" alt=\"[Add]\" /></a>
    </th>
    <td>
    <script type=\"text/javascript\">
    function get_selected_users_url() {
      var uri = '';
      users = document.getElementById('sel_user_id').childNodes;
      for(i=0; i<users.length;i++) {
        if(users[i].nodeName == \"DIV\") {
          uri += '&user_ids[]=' + users[i].id.substr(22);
        }
      }
      if(uri == '') 
        uri = '&user_ids[]=".$obm["uid"]."';
      return uri;
    }
    </script>
      $t_name
      <input type=\"hidden\" name=\"task_id\" value=\"$t_id\" />
      <input type=\"hidden\" name=\"task_name\" value=\"$t_name\" />
      <input type=\"hidden\" name=\"task_new_id\" value=\"$t_new_id\" />
      <input type=\"text\" size=\"32\" name=\"task_new_name\" value=\"$p_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
      </td>";

  if ($force == 1) {
    $dis_force = "<input type=\"checkbox\" id=\"cba_force\" value=\"1\" checked=\"checked\" name=\"cba_force\" />";
  } else {
    $dis_force = "<input type=\"checkbox\" id=\"cba_force\" value=\"1\" name=\"cba_force\" />";
  }
  
  if ($privacy == 1) {
    $dis_privacy = "<input type=\"checkbox\" id=\"cba_privacy\" value=\"1\" checked=\"checked\" name=\"cba_privacy\" />";
  } else {
    $dis_privacy = "<input type=\"checkbox\" id=\"cba_privacy\" value=\"1\" name=\"cba_privacy\" />";
  }

  $dis_hour_b = "<select name=\"sel_time_begin\" style=\"width:4em;\">";
  for ($i=$ccalendar_first_hour; $i<$ccalendar_last_hour; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourbegin) {
      $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_b .= "</select>";  
 
  $dis_hour_e = "<select name=\"sel_time_end\" style=\"width:4em;\">";
  for ($i=$ccalendar_first_hour; $i<=$ccalendar_last_hour; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourend) {
      $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_e .= "</select>";  

  $dis_min_b = "<select name=\"sel_min_begin\" style=\"width:4em;\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if ($current_min  == $minbegin) {
      $dis_min_b .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_b .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_b .= "</select>";  

 $dis_min_e = "<select name=\"sel_min_end\" style=\"width:4em;\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if ($current_min  == $minend) {
      $dis_min_e .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_e .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_e .= "</select>";
  $display["title"] = "<div class=\"title\">$l_module_calendar : $title</div>";
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[5]";
  $url2 = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[6]&amp;ext_title=" . urlencode($l_calendar_select_group);

  // Exception
  if(is_array($date_exception)) {
    foreach($date_exception as $key => $value) {
      $value = of_date_upd_format($value,true);
      $block_exception_detail .= "<div class=\"elementRow\" id=\"date_exception-$key\">
         <a href=\"javascript: remove_element(this.parentNode,'exceptionHome');\">
         <img src=\"$ico_delete\" alt=\"[Delete]\">
         </a>
         <input type=\"text\" name=\"tf_date_exception[]\" value=\"$value\" class=\"datePicker\" />
       </div>";
    }
  }
  $add_date = "<a href=\"javascript: add_exdate();\"><img src=\"$ico_add\" alt=\"[Date]\" /></a>";

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"get\" name=\"f_entity\"
   onsubmit=\"if(this.tf_title.value == '')this.tf_title.value = this.task_new_name.value; if (check_calendar_calendar(this)) return true; else return false;\"
   action=\"calendar_index.php\">
  <fieldset class=\"detail infos\">  
  <legend>$l_module_calendar</legend>
  <table>
  <tr>
    <th>$l_title</th>
    <td><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"50\" size=\"25\" value=\"$title\" /></td>
  </tr> 
  <tr>
    <th>$l_location</th>
    <td><input type=\"text\" id=\"tf_location\" name=\"tf_location\" maxlength=\"60\" size=\"25\" value=\"$location\" /></td>
  </tr> 
  <tr>
    <th>$l_sendamail</th>
    <td><input type=\"checkbox\" id=\"cba_mail\" value=\"1\" name=\"cba_mail\" /></td>
  </tr>       
  <tr>
    <th>$l_force</th>
    <td>$dis_force</td>
  </tr>   
  <tr>
    <th>$l_private</th>
    <td>$dis_privacy</td>
  </tr> 
    $block_category1
  <tr>
    $block_user
  </tr>
  <tr>
    $block_group
  </tr>    
  <tr>
    $block_resource
  </tr>
  <tr>
    $block_task
  </tr>    
  </table>
  </fieldset>
  <fieldset class=\"detail infos\">
  <legend>$l_carac</legend>
  <table>
  <tr>
    <th>$l_all_day</th>
    <td>
      <input onchange=\"show_hide_calendar_dates(this)\" onclick=\"show_hide_calendar_dates(this)\" type=\"checkbox\" name=\"cba_all_day\" value=\"1\" $all_day_checked />
    </td>
  </tr>   
  <tr>
    <th>$l_datebegin</th>
    <td>
      <input type=\"text\" name=\"tf_date_begin\" value=\"$datebegin\" class=\"datePicker\" onchange=\"this.form.tf_date_end.value=this.value\"/>
      <span id=\"hour_begin\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr>
    <th>$l_dateend</th>
    <td>
      <input type=\"text\" name=\"tf_date_end\" value=\"$dateend\" class=\"datePicker\" />
      <span id=\"hour_end\" style=\"$style\"  class=\"NW\">$l_hour : $dis_hour_e : $dis_min_e</span>
    </td>       
  </tr>
  <tr>
    <th>$l_priority</th>
    <td>$dis_sel_prio</td>
  </tr>      
  </table>
  </fieldset>
  <fieldset class=\"detail infos\">
  <legend>$l_repeat</legend>
  <table>    
  <tr>    
    <th>$l_repeatkind</th> 
    <td>$dis_sel_kind</td>
  </tr>
  <tr id=\"repeatFrequency\" style=\"display:$repeatDisplay\">    
    <th>$l_repeatfrequency</th> 
    <td>$frequency_block</td>
  </tr>    
  <tr id=\"repeatEnd\" style=\"display:$repeatDisplay\">    
    <th>$l_date_repeatend</th> 
    <td>
      <input type=\"text\" name=\"tf_repeat_end\" value=\"$repeat_end\" class=\"datePicker\" />
    </td>
  </tr>
  <tr id=\"repeatDays\" style=\"display:$repeatDaysDisplay\">  
    <th>$l_repeatdays2</th>
    <td>$dis_repeat_day</td>
  </tr>
  <tr id=\"repeatException\" style=\"display:$repeatDisplay\">
    <th> $l_exception $add_date</th>
    <td id=\"exceptionHome\">$block_exception_detail</td>
  </tr>
  </table>
  </fieldset>
  <fieldset class=\"detail extra\">    
  <legend>$l_desc</legend>  
  <textarea id=\"ta_description\" name=\"ta_description\" rows=\"3\" cols=\"72\">$desc</textarea> 
  </fieldset>
  <fieldset class=\"buttons\">$dis_button $hidden_repeat</fieldset>
  </form>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search', 'sel_user_id', 'userSearch');
      new obm.AutoComplete.Search('$path/resource/resource_index.php?action=ext_search&form=event', 'sel_resource_id', 'resourceSearch');
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search', 'sel_group_id', 'groupSearch');
    });
  </script>"; 

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Waiting events
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_calendar_waiting_events($obm_wait) {
  global $l_users, $l_title, $l_location, $l_priority, $l_category,$l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_repeat,$l_change_state,$l_validate,$l_from, $l_user, $l_resource;
  global $l_repeatkind,$l_repeatdays2,$l_date_repeatend,$l_daily,$l_weekly,$ccalendar_weekstart,$l_wait;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse;
   
  while ($obm_wait->next_record()) {
    $old_entity = $current_entity;
    $old_entity_entity = $current_entity_entity;
    $old_label = $current_label;
    $current_entity = $obm_wait->f("entity_id");
    $current_entity_entity = $obm_wait->f("entity_entity");
    
    // If event for a new calendar (delegation), display the previous block
    if ($current_entity != $old_entity || $current_entity_entity != $old_entity_entity) {
      $type = ${"l_".$current_entity_entity};
      // if previous block had events (true except first block)
      if ($dis_events != "") {

	// set title row according to event content
        if ($is_periodic) {
	  $dis_kind = "<th>$l_repeatkind</th>";
	  $dis_day = "<th>$l_repeatdays2</th>";
          $dis_end = "<th>$l_date_repeatend</th>";
          $dis_events = str_replace("__NO_REPEAT__","<td></td><td></td><td></td>",$dis_events);
        } else {
	  $dis_kind = "";
	  $dis_day = ""; 
          $dis_end = "";
          $dis_events = str_replace("__NO_REPEAT__","",$dis_events);
          $dis_events = str_replace("__NO_REPEAT__","",$dis_events);
	}

        $block .= "
        <h1>$type : $current_label</h1>
        <table class=\"spreadSheet\">
        <thead>
	 <tr>
	  <th>$l_title</th>
	  <th>$l_location</th>
	  <th>$l_category</th>
	  <th>$l_priority</th>
	  <th>$l_from</th>
	  $dis_kind
	  $dis_day
	  $dis_end     
	  <th>$l_datebegin</th>
	  <th>$l_dateend</th>
	  <th></th>
        </tr>
        </thead>
        <tbody>
        $dis_events
        </tbody>
        </table>";
      }
      $is_periodic = false;
      $dis_events = "";
    }

    $current_label = $obm_wait->f("entity_label");
    switch ($obm_wait->f("calendarevent_priority") ) {;
      case 1 : 
	$priority = $l_low;
	break;
      case 2 :
	$priority = $l_medium;
	break;
      case 3 :
	$priority = $l_high;
	break;
    }
	
    $start_week_day = strtotime($ccalendar_weekstart);

    if ($obm_wait->f("calendarevent_repeatkind") != "none") {
      $is_periodic = true;
      $repeat = true;
    } else {
      $repeat = false;
    }
    if($repeat) {
      for ($i=0; $i<7; $i++) {
        $day_num = date("w", $start_week_day);
        $day = $l_daysofweekshort[$day_num];
        if (strcmp(substr($obm_wait->f("calendarevent_repeatdays"),$i,1),"1")==0) {
          $dis_repeat_days .= "$day ";
        }
        $start_week_day = strtotime("+1 day", $start_week_day); 
      }
      $dis_repeat_days = "<td>$dis_repeat_days</td>";
      $date_repeat = "<td>".of_date_format($obm_wait->f("calendarevent_endrepeat"))."</td>";
      $kind = "<td>".${"l_".$obm_wait->f("calendarevent_repeatkind")}."</td>";
    } else {
      $date_repeat = "__NO_REPEAT__";
      $dis_repeat_days = "";
      $kind = "";
    }

    $user_create = $obm_wait->f("usercreate_firstname")." ".$obm_wait->f("usercreate_lastname");
    $date_begin = of_datetime_format($obm_wait->f("calendarevent_date"));
    $date_end = of_datetime_format($obm_wait->f("calendarevent_date") + $obm_wait->f("calendarevent_duration"));
    $dis_events .= "
    <tr>
     <td>".$obm_wait->f("calendarevent_title")."</td>
     <td>".$obm_wait->f("calendarevent_location")."</td>
     <td>".$obm_wait->f("calendarcategory1_label")."</td>
     <td>$priority</td>
     <td>$user_create</td>
     $kind
     $dis_repeat_days
     $date_repeat
     <td>$date_begin</td>
     <td>$date_end</td>
     <td>
      <form method=\"get\" action=\"calendar_index.php\">
       <input type=\"hidden\" name=\"action\" value=\"decision\" />      
       <input type=\"hidden\" name=\"calendar_id\" value=\"".$obm_wait->f("calendarevent_id")."\" />
       <input type=\"hidden\" name=\"entity_kind\" value=\"$current_entity_entity\" />
       <input type=\"hidden\" name=\"entity_id\" value=\"$current_entity\" />
       <input type=\"radio\" name=\"rd_decision_event\" value=\"A\"  onclick=\"this.form.submit()\" />$l_accept<br />
       <input type=\"radio\" name=\"rd_decision_event\" value=\"W\" checked=\"checked\" onclick=\"this.form.submit()\" />$l_wait<br />
       <input type=\"radio\" name=\"rd_decision_event\" value=\"R\" onclick=\"this.form.submit()\" />$l_refuse
      </form>
     </td>
    </tr>";
  }

  // --- HTML Template --------------------------------------------------------

  // Display the last block
  if ($dis_events != "") {

    if ($is_periodic) {
      $dis_kind = "<th>$l_repeatkind</th>";
      $dis_day = "<th>$l_repeatdays2</th>";
      $dis_end = "<th>$l_date_repeatend</th>";
      $dis_events = str_replace("__NO_REPEAT__","<td></td><td></td><td></td>",$dis_events);
    } else {
      $dis_kind = "";
      $dis_day = ""; 
      $dis_end = "";
      $dis_events = str_replace("__NO_REPEAT__","",$dis_events);
    }
    
    $block .= "
      <h1>$type : $current_label</h1>
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_title</th>
        <th>$l_location</th>
        <th>$l_category</th>
        <th>$l_priority</th>
        <th>$l_from</th>
        $dis_kind
        $dis_day
        $dis_end
        <th>$l_datebegin</th>
        <th>$l_dateend</th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      $dis_events
      </tbody>
      </table>";
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Delete Options
// Parameters:
//   - $calendar:
///////////////////////////////////////////////////////////////////////////////
function html_calendar_dis_delete($calendar) {
  global $obm, $perm, $ico_validate,$ico_cancel,$ico_confirm_mail;
  global $display, $l_confirm_delete,$l_confirm,$l_cancel,$l_confirm_mail;
  global $l_event_cant_delete, $module, $cright_write_admin;

  $iso_date = $calendar["date"];
  $event_id = $calendar["calendar_id"];
  $e = get_calendar_event_info($event_id);
  $usercreate = $e["usercreate"];

  if (($obm["uid"] != $usercreate) && (! $perm->check_right($module, $cright_write_admin))) {
    $display["msg"] = display_warn_msg($l_event_cant_delete);
    return;
  }

  $block = "
   <h1>$l_confirm_delete</h1>
   <div class=\"C\">
    <a href=\"calendar_index.php?action=delete&calendar_id=$event_id&cba_mail=1\"><img src=\"$ico_validate\" alt=\"[ValidateMail]\"/></a>
     $l_confirm_mail
    <a href=\"calendar_index.php?action=delete&calendar_id=$event_id\"><img src=\"$ico_validate\" alt=\"[Validate]\"/></a>
     $l_confirm
    <a href=\"calendar_index.php?action=detailconsult&calendar_id=$event_id&date=$iso_date\"><img src=\"$ico_cancel\" alt=\"[Cancel]\" /></a>
     $l_cancel
   </div>
  </div>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $calendar        : calendar parameters
//   - $sel_entity_id : array of entity Id ["user"] ["resource"]
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_meeting_form($calendar, $sel_entity_id) {
  global $display, $path, $set_cal_interval;
  global $ico_delete, $ico_mini_cal;
  global $ccalendar_resource;
  // -- Labels
  global $l_users,$l_datebegin,$l_dateend,$l_format,$l_header_meeting,$l_event_duration,$l_meeting_perform; 
  global $l_groups,$l_resources,$l_module_resourcegroup;

  $time_unit = 60 / $set_cal_interval;
  $datebegin = of_date_upd_format($calendar["date"]);
  $dis_button = "
     <input type=\"hidden\" name=\"calendar_id\" id=\"calendar_id\" value=\"$id\" />
     <input type=\"hidden\" name=\"action\" id=\"action\" value=\"perform_meeting\" />
     <input type=\"submit\" value=\"$l_meeting_perform\" />";

  $users_id = $sel_entity_id["user"];
  if (count($users_id > 0)) {
    $users = get_userobm_from_ids($users_id);
  } else {
    $users = array();
  }
  $res_id = $sel_entity_id["resource"];
  if (count($res_id > 0)) {
    $res = get_calendar_resource_from_ids($res_id);
  } else {
    $res = array();
  }
  $groups_id = $sel_entity_id["group"];
  if (count($groups_id > 0)) {
    $groups = get_calendar_group_from_ids($groups_id);
  } else {
    $groups = array();
  }

  // user select
  if (is_array($users["entity"])) {
    foreach ($users["entity"] as $u_id => $u_ent) {
      $u_name = $u_ent["label"];
      $sel_id = "data-user-$u_id";
      $div_id = "sel_user_id-$sel_id";
      $dis_sel_user .= "<div class=\"elementRow\" id=\"$div_id\">
      <a href=\"javascript: remove_element('$div_id','sel_user_id');\">
      <img src=\"$ico_delete\" alt=\"[Delete]\" />
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }

  $block_user = "
    <th>
    $l_users
    </th>    
    <td id=\"sel_user_id\">
      <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" />
      $dis_sel_user
    </td>
  ";
  if ($ccalendar_resource) {
    if (is_array($res["entity"])) {
      foreach ($res["entity"] as $r_id => $r_ent) {
	$r_name = $r_ent["label"];
	$sel_id = "data-resource-$r_id";
	$div_id = "sel_resource_id-$sel_id";
	$dis_sel_resource .= "
          <div class=\"elementRow\" id=\"$div_id\">
          <a href=\"javascript: remove_element('$div_id','sel_resource_id');\">
          <img src=\"$ico_delete\"  alt=\"[Delete]\" />
          </a>
          $r_name
          <input value=\"$sel_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
          </div>";
      }
    }  
    $dis_block_resource = "
    <tr>
      <th>
      $l_resources
      </th>
     <td id=\"sel_resource_id\">
      <input type=\"text\" name=\"\" value=\"\" id=\"resourceSearch\" autocomplete=\"off\" />
       $dis_sel_resource
     </td>      
    </tr>";
  
    if (is_array($res_grp["entity"])) {
      foreach ($res_grp["entity"] as $rg_id => $rg_ent) {
	$rg_name = $rg_ent["label"];
	$sel_id = "data-resourcegroup-$r_id";
	$div_id = "sel_resource_group_id-$sel_id";
	$dis_sel_resource_group .= "
          <div class=\"elementRow\" id=\"$div_id\">
          <a href=\"javascript: remove_element('$div_id','sel_resource_group_id');\">
          <img src=\"$ico_delete\" alt=\"[Delete]\" />
          </a>
          $r_name
          <input value=\"$sel_id\" name=\"sel_resource_group_id[]\" type=\"hidden\" />
          </div>";
      }
    }  
    $dis_block_resource_group = "
    <tr>
      <th>
      $l_module_resourcegroup
      </th>
      <td id=\"sel_resource_group_id\">
      <input type=\"text\" name=\"\" value=\"\" id=\"resourcegroupSearch\" autocomplete=\"off\" />

       $dis_sel_resource_group
     </td>      
     </tr>";
    $resource_js = "
      new obm.AutoComplete.Search('$path/resource/resource_index.php?action=ext_search', 'sel_resource_id', 'resourceSearch');
      new obm.AutoComplete.Search('$path/resourcegroup/resourcegroup_index.php?action=ext_search', 'sel_resource_group_id', 'resourcegroupSearch');  
    ";
  }
  
  // group select
  if (is_array($groups["entity"])) {
    foreach ($groups["entity"] as $g_id => $g_ent) {
      $g_name = $g_ent["label"];
      $sel_id = "data-group-$g_id";
      $div_id = "sel_group_id-$sel_id";
      $dis_sel_group .= "
        <div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_group_id');\">
        <img src=\"$ico_delete\" alt=\"[Delete]\" />
        </a>
        $g_name
        <input value=\"$sel_id\" name=\"sel_group_id[]\" type=\"hidden\" />
        </div>";
    }
  }

  $block_group = "
    <th>
    $l_groups
     </th>
     <td id=\"sel_group_id\">
       <input type=\"text\" name=\"\" value=\"\" id=\"groupSearch\" autocomplete=\"off\" />
       $dis_sel_group
     </td>";


  $dis_hour_d = "<select name=\"sel_time_duration\">";
  for ($i=0;$i<24;$i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourbegin){
      $dis_hour_d .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_d .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_d .= "</select>";
 
  $dis_min_d = "<select name=\"sel_min_duration\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if ($current_min  == $mindur){
      $dis_min_d .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_d .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_d .= "</select>";
  $display["title"] = "<h1 class=\"title\">$l_header_meeting</h1>";
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[0]";

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"post\" name=\"f_entity\"
   onsubmit=\"if (calendar_check_meeting(this)) return true; else return false;\"
   action=\"calendar_index.php\">
  <fieldset class=\"detail extra\">  
  <legend>$l_header_meeting</legend>
  <table>
  <tr>
    $block_user
  </tr>
  <tr>
    $block_group
  </tr>
  $dis_block_resource  
  $dis_block_resource_group
  <tr>
    <th>$l_datebegin $l_format</th>
    <td>
      <input type=\"text\" name=\"tf_date_begin\" value=\"$datebegin\" class=\"datePicker\" />
    </td>
  </tr>
  <tr>    
    <th>$l_event_duration</th>
    <td>$dis_hour_d : $dis_min_d</td>
  </tr>
  </table>
  </fieldset>
  <fieldset class=\"buttons\">
    $dis_button
  </fieldset>    
  </form>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search', 'sel_user_id', 'userSearch');
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search', 'sel_group_id', 'groupSearch');
      $resource_js
    });
  </script>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Free meeting performing and display
// Parameters:
//   - $calendar       : calendar parameters
//   - $entity_store : array of entities to display
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_free_interval($calendar, $entity_store) {
  global $ico_left_day,$ico_right_day;
  global $set_cal_interval,$cal_entity_id,$ico_previous,$ico_next;
  global $ccalendar_weekstart, $ccalendar_first_hour, $ccalendar_last_hour;
  global $l_private,$l_private_description,$display,$l_free_resources,$l_all_f;
  global $l_previous, $l_next;

  $iso_date = $calendar["date"];
  $ts_date = strtotime($iso_date);
  $time_unit = 60 / $set_cal_interval; 
  $time_unit_sec = 3600 / $set_cal_interval;; 
  $duration = $calendar["meeting_duration"] * 3600;
  $param_duration = $calendar["meeting_duration"];

  $nb_group = count($cal_entity_id["resource_group"]);
  $week_num = get_calendar_week_num($ts_date); 
  $next_week = of_isodate_format(strtotime("+1 week", $ts_date));
  $prev_week = of_isodate_format(strtotime("-1 week", $ts_date));
  
  $start_week_day = get_calendar_date_day_of_week($ts_date, $ccalendar_weekstart);
  $end_week_time = $start_week_day + ((6 * 24 + $ccalendar_last_hour) * 3600);
  $start_week_time = strtotime("+$ccalendar_first_hour hours",$start_week_day);

  if (is_array($entity_store["resourcegroup"]) && count($entity_store["resourcegroup"]) > 0) {
    $have_resource_grp = true;
  } else {
    $have_resource_grp = false;
  }

  $start_week = calendar_localizeDate("week", $start_week_time);
  $end_week = calendar_localizeDate("week", $end_week_time);
  $display_date = "$start_week - $end_week"; 
  calendar_events_model($start_week_day,$end_week_time,$entity_store);
  $of = &OccurrenceFactory::getInstance();
  $day_duration = 86400; 
  /* Week Days headers */
  for ($i = $start_week_time; $i < $end_week_time; $i += $day_duration ) {
    $this_date_l = calendar_localizeDate("week_list", $i);
    $week_day_list .= "
     <td>
      $this_date_l 
     </td>";
  } 
  /* Events of the week */
  for ($j = $ccalendar_first_hour; $j < $ccalendar_last_hour; $j++) {
    $delta = $j - $ccalendar_first_hour;
    for ($k=0; $k<$set_cal_interval; $k++) {
      if ($k==0) {
	$week_cal .= "\n<tr><th>".($j).":00</td>";
      } else {
	$week_cal .= "\n<tr><th></th>";
      }
      $delta2 = $k * $time_unit;
      for ($i = 0; $i <7; $i ++) {
	$current_time = strtotime("+$i days +$delta hours +$delta2 minutes",$start_week_time);
        $dayLink = of_isodate_format($current_time);
        $end_time = $current_time + $duration;
	$begin_date = urlencode(of_isodate_format($current_time,"",1));
	$end_date = urlencode(of_isodate_format($end_time,"",1));
        $goto = "calendar_index.php?action=new&amp;tf_date_begin=$begin_date&amp;tf_date_end=$end_date";


        $have_event = $of->periodHaveOccurrences($current_time,$time_unit_sec, array("user" => array(), "resource" => array()));
        $will_have_event = $of->periodHaveOccurrences($current_time,$duration, array("user" => array(), "resource" => array()));
        // Gestion des groupes de ressources
        if($have_resource_grp && !$have_event ) {
          $possible_ressources = array();
          $free_ressources = array();
          
          $resource_ul = "";
          $resource_url = "";  
          foreach($entity_store["resourcegroup"] as $groupedresource) {
            $grid = $groupedresource["id"]; 
            $grlabel = $groupedresource["name"];
            $rgid = $groupedresource["group"];
            $grp_have_event = $of->periodHaveOccurrences($current_time, $time_unit_sec,array("resourcegroup" => array($grid)));
            $grp_will_have_event = $of->periodHaveOccurrences($current_time, $duration,array("resourcegroup" => array($grid)));
             if(!$grp_will_have_event) {
               $possible_ressources[$rgid][] = $groupedresource;
            } 
            if (!$grp_have_event) {
              $free_ressources[$rgid][] = $groupedresource;
            }
          }
          if(count($free_ressources) != $nb_group) {
            $have_event = true;
          }
          if(count($possible_ressources) != $nb_group) {
            $will_have_event = true;
          }
          
          if(count($free_ressources) > 0 && !$have_event) {          
            foreach($free_ressources as $groupId => $group) {
              if(is_array($possible_ressources[$groupId])) {
                $resource_ul .= html_calendar_ressource_list($group[0]["groupLabel"],$possible_ressources[$groupId]);
                $resource_url .= "&amp;sel_resource_id[]=".$possible_ressources[$groupId][0]["id"];
              }else {
                $resource_ul .= html_calendar_ressource_list($group[0]["groupLabel"],$group,true);
                $resource_url .= "&amp;sel_resource_id[]=".$free_ressources[$groupId][0]["id"];
              }
            }
            $resource_div .= "
            <div class=\"infoPopup\" id=\"resource-$current_time\">
            <h1>$l_free_resources </h1>
             $resource_ul
            </div>";              
          }
        } 
          
        $goto = "calendar_index.php?action=new&amp;tf_date_begin=$begin_date&amp;tf_date_end=$end_date".$resource_url;
        if ($have_event) {
          $week_cal .= "<td class=\"busy\">&nbsp;</td>\n";
        } elseif ($will_have_event) {
          if($have_resource_grp) {
            $week_cal .= "<td class=\"free\" onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='hover';this.style.cursor = 'pointer';toggleResource(this,'$current_time');\"
             onmouseout=\"this.className ='free';toggleResource(this,'$current_time');\">&nbsp;</td>\n";
          } else {
            $week_cal .= "<td class=\"free\" onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='hover';this.style.cursor = 'pointer';\"
             onmouseout=\"this.className ='free';\">&nbsp;</td>\n";
          }
        } else {
          if($have_resource_grp) {
            $week_cal .= "<td  onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='hover';this.style.cursor = 'pointer';toggleResource(this,'$current_time');\"
             onmouseout=\"this.className='';toggleResource(this,'$current_time');\">&nbsp;</td>\n";
          } else {
            $week_cal .= "<td onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='hover';this.style.cursor = 'pointer';\"
             onmouseout=\"this.className='';\">&nbsp;</td>\n";
          }            
        }
      }
      $week_cal .= "</tr>";
    }
  }

  $block = "  
  <h1>
    <a href=\"calendar_index.php?action=perform_meeting&amp;date=$prev_week&amp;meeting_duration=$param_duration\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $display_date
    <a href=\"calendar_index.php?action=perform_meeting&amp;date=$next_week&amp;meeting_duration=$param_duration\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
  </h1> 
  <table class=\"calendar freeBusy\">
    <thead>
    <tr>
    <td>$week_num</td>
    $week_day_list
    </tr>
    </thead>
    <tbody>
    $week_cal
    </tbody>
  </table>
  <script type=\"text/javascript\">
    function toggleResource(container,time) {
      resource = \$('resource-'+time);
      container = \$(container);
      if(resource) 
        if(resource.getStyle('display') == 'block')
          resource.setStyle('display','none');
        else {
          resource.setStyle('left',(container.getLeft()+container.offsetWidth)+'px')   
          resource.setStyle('top',(container.getTop()+container.offsetHeight)+'px')
          resource.setStyle('display','block');
        } 
    }
  </script>
  <div>
  $resource_div
  </div>";
  return  $block;
}


/**
 * dis_calendar_right_dis_admin
 * Display calendar rights admin form
 *
 * @param mixed $id calendar(user) id to admin
 * @return mixed xhtml display
 */
function dis_calendar_right_dis_admin($id="") {
  global $obm, $display, $l_calendar;

  if ($id == "") {
    $id = $obm["uid"];
  }
  $infos = get_user_info($id);
  $name = $infos["firstname"] . " " . $infos["lastname"];
  $display["title"] = "<h1 class=\"title\">$l_calendar : $name</h1>";
  $block = of_right_dis_admin("calendar", $id, "userobm");

  return $block;
}


/**
 * html_calendar_ressource_list
 * Build the resources pop up 
 * 
 * @param mixed $groupLabel 
 * @param mixed $resource_group 
 * @param mixed $free 
 * @access public
 * @return void
 */
function html_calendar_ressource_list($groupLabel,$resource_group,$free=false) {
  global $l_free_resources,$ico_warning;
  
  if($free) {
    $ico = "<img src=\"$ico_warning\" alt=\"[Restricted]\" />";
  }
  $class ="B";
  foreach($resource_group as $resource) {
    $ret .= "<li class=\"$class\" >  ".$resource["name"]."</li>";
    $class = "";
  }
  return "
  <h2>$ico $groupLabel</h2>
  <ul class=\"list\">
  $ret
  </ul>
";
}


///////////////////////////////////////////////////////////////////////////////
// Display the calendar administration index
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_admin_index() {

  $cats1 = of_category_get_ordered("calendar", "category1");
  $block = of_category_dis_admin_form("category1", $cats1);;

  return $block;

}


///////////////////////////////////////////////////////////////////////////////
// Perform the export to the vCalendar format
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_export_handle($calendar) {
  global $obm, $obm_version, $ccalendar_weekstart;

  $iso_date = $calendar["date"];
  $ts_date = strtotime($iso_date);

  $start_time = strtotime("-1 year", $ts_date);
  $end_time = strtotime("+1 year", $ts_date);

  $calendar_user["user"] = array ($obm["uid"] => "dummy");
  $ev_q = run_query_calendar_no_repeat_events($start_time,$end_time,$calendar_user);

  header("Content-Type: text/x-vCalendar");
  header("Content-Disposition: inline; filename=ObmCalendar.ics");
  echo "BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//OBM/OBM V$obm_version Calendar//EN
METHOD:PUBLISH";
  while ($ev_q->next_record()) {
    $id = $ev_q->f("calendarevent_id");
    $title = "SUMMARY:" . $ev_q->f("calendarevent_title");
    $title = wordwrap($title,74,"\n ",1);
    $location = $ev_q->f("calendarevent_location");
    $priority = $ev_q->f("calendarevent_priority");
    $category1 = strtoupper($ev_q->f("calendarcategory1_label")); 
    $date_ts = $ev_q->f("calendarevent_timeupdate");
    $date_ts = gmdate("Ymd\THis\Z",$date_ts);
    if ($ev_q->f("calendarevent_description") != "") {
      $description = preg_replace("/\\r?\\n/","\\n",$ev_q->f("calendarevent_description"));
      $description = "DESCRIPTION:" . $description;
      $description = "\n" . wordwrap($description,74,"\n ",1);
    } else {
      $description = "";
    }
    $date_b = $ev_q->f("calendarevent_date");
    $date_b = gmdate("Ymd\THis\Z",$date_b);
    $date_e = $ev_q->f("calendarevent_date") + $ev_q->f("calendarevent_duration") ;
    $date_e = gmdate("Ymd\THis\Z",$date_e);
    switch($priority) {
      case 1 : $priority = 3;
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $ev_q->f("calendarevent_privacy");
    switch($private) {
      case 0 : $private = "PUBLIC";
	break;
      case 1 : $private = "PRIVATE";
	break;
    }        
    echo "
BEGIN:VEVENT
UID:$id    
$title
CLASS:$private
LOCATION:$location
CATEGORIES:$category
STATUS:CONFIRMED$description
DTSTART:$date_b
DTEND:$date_e
DTSTAMP:$date_ts
END:VEVENT";
  }
  
  $ev_q = run_query_calendar_repeat_events($start_time,$end_time,$calendar_user);
  
  while($ev_q->next_record()) {
    $id = $ev_q->f("calendarevent_id");
    $title = $ev_q->f("calendarevent_title");
    $location = $ev_q->f("calendarevent_location");
    $priority = $ev_q->f("calendarevent_priority");
    $category = strtoupper($ev_q->f("calendarcategory1_label"));
    if ($ev_q->f("calendarevent_description") != "") {
      $description = "\nDESCRIPTION:".$ev_q->f("calendarevent_description");
    } else {
      $description = "";
    }
    $date_b = $ev_q->f("calendarevent_date");
    $date_b = date("Ymd",$date_b)."T".date("His",$date_b);
    $start_date = $date_b;
    $date_e = $ev_q->f("calendarevent_date") + $ev_q->f("calendarevent_duration");
    $date_e = date("Ymd",$date_e)."T".date("His",$date_e);
    $kind = $ev_q->f("calendarevent_repeatkind");
    $end = $ev_q->f("calendarevent_endrepeat");
    $end = date("Ymd",$end)."T".date("His",$end);
    $repeat_days = $ev_q->f("calendarevent_repeatdays");
    $repeat_frequence = $ev_q->f("calendarevent_repeatfrequence");
    switch($priority) {
      case 1 : $priority = 3;
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $ev_q->f("calendarevent_privacy");
    switch($private) {
      case 0 : $private = "PUBLIC";
	break;
      case 1 : $private = "PRIVATE";
	break;
    }   
    if ($kind == "daily") {
      $repeat = "FREQ=DAILY;UNTIL=$end;INTERVAL=$repeat_frequence";
    } elseif($kind == "weekly") {
      $l_day_repeat = array("SU","MO","TU","WE","TH","FR","SA");
      $start_week_day = strtotime($ccalendar_weekstart);
      for ($i=0; $i<7; $i++) {
      	$day_num = date("w", $start_week_day);
      	$day = $l_day_repeat[$day_num];
	if (strcmp(substr($repeat_days,$i,1),"1")==0) {
	  if ($i!=0) {
	    $dis_repeat_days .= ",";
	  }
	  $dis_repeat_days .= "$day";
	}
       	$start_week_day = strtotime("+1 day", $start_week_day); 
      } 
      $repeat = "FREQ=WEEKLY;UNTIL=$end;INTERVAL=1;BYDAY=$dis_repeat_days";
    }
    elseif ($kind == "monthlybydate") {
      $day = date("d",$start_date);
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYDAY=$day"; 
    }
    elseif ($kind == "monthlybyday") {
      $start_week_day = date("w",$start_date);
      $daypos = ceil(substr($start_date,6,2)/7);
      $day_num = date("w",strtotime("+ $start_week_day days",$start_date));
      $day = $l_day_repeat[$day_num];
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYDAY=$daypos$day";
    } elseif ($kind == "yearly") {
      $monthpos = date("m",$start_date);
      $repeat = "FREQ=YEARLY;UNTIL=$end;INTERVAL=$repeat_frequence;BYMONTH=$monthpos";
    }
    echo "
BEGIN:VEVENT
UID:$id    
SUMMARY:$title
CLASS:$private
LOCATION:$location
CATEGORIES:$category
STATUS:CONFIRMED$description
DTSTART:$date_b
DTEND:$date_e
RRULE:$repeat
END:VEVENT";
  }
  echo "
END:VCALENDAR";
}

/**
 * JSON ok Message. 
 * 
 * @access public
 * @return void
 */
function json_ok_msg($msg) {
  global $display;

  if($display['json'] != '') {
    $display['json'] .= ',';
  }
  $display['json'] .= "error:0,message:'$msg'";
}

/**
 * JSON Error Message.
 * 
 * @param mixed $msg 
 * @access public
 * @return void
 */
function json_error_msg($msg) {
  global $display;

  if($display['json'] != '') {
    $display['json'] .= ',';
  }
  $display['json'] .= "error:1,message:'$msg'";

}


/**
 * Build a JSON message with all the event data
 * the respone is a table of event objects.
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function json_event_data($id,$calendar) {
  global $obm, $display,$ccalendar_first_hour,$ccalendar_last_hour;
  global $ccalendar_max_users, $cal_entity_id;
  global $l_user, $l_resource, $l_task;

  $context = $calendar["context"];
  $element_id = $calendar["element_id"];
  $ent_q = run_query_get_events_attendee(array($id));
  $entities = get_calendar_event_entity($id);
  $all_day = $calendar['all_day'];
  $date = strtotime($calendar['date_begin']);
  $duration = $calendar['duration'];
  $title = $calendar['title'];

  while($ent_q->next_record()) {
    $entity = $ent_q->f("evententity_entity");
    $entity_id = $ent_q->f("evententity_entity_id");
    switch($entity) {
      case "user":
        $name = $ent_q->f("userobm_lastname")." ".$ent_q->f("userobm_firstname");
      break;
      case "resource":
        $name = $ent_q->f("resource_name");
      break;
      default: break;
    }
    $attendee[$entity][$entity_id] .= $name;
  }
  if(is_array($attendee)) { 
    foreach ($attendee as $att_entity => $att_entities) {
      $label_entity = phpStringToJsString(${"l_$att_entity"});
      $ent = array();
      foreach($att_entities as $at) {
        $ent[] = phpStringToJsString($at);
      }
      $attendees[] = "{label:'".$label_entity."', entities:['".implode("','",$ent)."']}"; 
    }
  }

  $ed[] = "id:".$id;
  $ed[] = "duration:".$duration;
  $ed[] = "time:".$date;
  $ed[] = "all_day:".$all_day;
  $ed[] = "title:'".$title."'";
  $ed[] = "attendees:[".implode(',',$attendees)."]";
  $ed[] = "updatable:true";
  $ed[] = "description:''";
  $ed[] = "location:''";
  $ed[] = "category:''";
  
  $entity_readable = get_calendar_entity_readable();
  $sel_entity_id = slice_calendar_entities($cal_entity_id, $ccalendar_max_users, $entity_readable);
  $calendar_entity = store_calendar_entities(run_query_calendar_get_entity_label($cal_entity_id));
  
  switch($context)  {
    case "day" :
      $day = ($all_day == 1) || (date('Ymd',$date + $duration) != date('Ymd',$date));
      $day = $day || (date('H',$date + $duration) <  $ccalendar_first_hour);
      $day = $day || (date('H',$date) >  $ccalendar_last_hour );
      $options = array(); 
      break;
    case "week" :
      $day = ($all_day == 1) || (date('Ymd',$date + $duration) != date('Ymd',$date));
      $day = $day || (date('H',$date + $duration) <  $ccalendar_first_hour);
      $day = $day || (date('H',$date) >  $ccalendar_last_hour );
      $options = array();
      break;
    case "month" :
      $day = true;
      $week_duration = 86400*7;
      $options = array("xUnit:86400","yUnit:$week_duration",
                       "context:obm.calendarManager.bodyContext");      
      break;     
    default :
      $options = array();
      break; 
  }

  $updatable[] = "draggable:'true'"; 
  if(!$day) {
    $updatable[] = "resizable:'true'"; 
  }
  foreach ($calendar_entity as $entity => $data) {
    foreach ($data as $entity_id => $entity_data) {
      if($entities[$entity]["entity"][$entity_id]) {
        $info = $ed;
        $opt = $options;
        $info[] = "entity:'$entity'";
        $info[] = "entity_id:$entity_id";
        $info[] = "klass:'".$entity_data["class"]."'";
        $info[] = "state:'".$entities[$entity]["entity"][$entity_id]["state"]."'";
        if($entity_id = $obm["uid"] && $entity == 'user') {
          $opt = array_merge($opt,$updatable);
        }
        if($json != '') {
          $json .= ',';
        }
        $json .= "{event:{".implode(",",$info)."},options:{".implode(",",$opt)."}}";
      }
    }
  }

  if($display['json'] != '') {
    $display['json'] .= ',';
  }
  if($day) {
    $display['json'] .= "day:'$day',";
  }
  if($element_id != '') {
    $display['json'] .= "elementId:'$element_id',";
  }
  $display['json'] .=  "eventsData:[$json]";
}



function parse_item($item) {
  if ($item == "") {
    return false;
  }

  $p = xml_parser_create();
  xml_parser_set_option($p, XML_OPTION_TARGET_ENCODING, "ISO-8859-1");
  xml_parse_into_struct($p, $item, $vals, $index);
  xml_parser_free($p);

  $match_attribut = array("resource", "res_id", "res_name", "item", "item_id", "property_name", "property_value");

  foreach($vals as $key => $value) {
    $att = strtolower($value["tag"]);
    if (in_array($att, $match_attribut)) { 
      // $item_detail->resource->$att = $value["value"];
      $${att} = $value["value"];
    }
    if (isset($res_id) && isset($res_name) && isset($item) && isset($item_id) && isset($property_name) && isset($property_value)) {
      $item_detail = new Item($res_id, $res_name, $item, $item_id, $property_name, $property_value);
      $item_list->resource[] =  $item_detail;
      unset($res_id);
      unset($res_name);
      unset($item);
      unset($item_id);
      unset($property_name);
      unset($property_value);
    }
  }

  return $item_list;
}

Class Item {
  var $res_id;
  var $res_name;
  var $item;
  var $item_id;
  var $property_name;
  var $property_value;

  function Item($res_id, $res_name, $item, $item_id, $property_name, $property_value) {
    $this->res_id = $res_id;
    $this->res_name = $res_name;
    $this->item = $item;
    $this->item_id = $item_id;
    $this->property_name = $property_name;
    $this->property_value = $property_value;
  }
}
?>
