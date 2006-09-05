<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_display.inc                                           //
//     - Desc : Agenda Display File                                          //
// 2002-11-26 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display the navigation bar
// Parameters:
//   - $agenda        : agenda parameters
//   - $sel_entity_id : array of entity Id ["user"] ["resource"]
//     by reference as it can restrict the user selection
//   - $p_view        : view selected "year", "month", "week" or "day"
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_calendar_view($agenda, &$sel_entity_id, $p_view="week") {
  global $display, $path;

  $group_view = $agenda["entity"]["group_view"];
  $entity_readable = get_agenda_entity_readable();
  $visible_entities_id = slice_agenda_entities($sel_entity_id, $entity_readable);

  $sel_entity_id = $visible_entities_id;
  // we restore the selected group for session storage
  $sel_entity_id["group_view"] = $group_view;

  $calendar_entity = store_agenda_entities(run_query_agenda_get_entity_label($sel_entity_id));
  $display["features"] = html_agenda_planning_bar($agenda, $calendar_entity, $entity_readable);

  if ($p_view == "year") {
    $block = dis_agenda_year_planning($agenda, $calendar_entity);
  } else if ($p_view == "month") {
    $block = dis_agenda_month_planning($agenda, $calendar_entity);
  } else if ($p_view == "week") {
    $block = dis_agenda_week_planning($agenda, $calendar_entity);
  } else if ($p_view == "day") {
    $block = dis_agenda_day_planning($agenda, $calendar_entity);
  }
  $display["print_url"] = "$path/agenda/agenda_index.php?action=view_$p_view&amp;date=".$agenda["date"];


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display the navigation bar
// Parameters:
//   - $agenda: agenda parameters
//   - $calendar_entity : array of entities elements to display
//   - $entity_readable : array of readable entities (["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function html_agenda_planning_bar($agenda, $calendar_entity, $entity_readable) {
  global $action;
  global $output_target;

  // If come from view (eg: month), keep view for links else set to week
  if ( ($action == "view_day") || ($action == "view_week")
    || ($action == "view_month") || ($action == "view_year")
    || ($action == "planning") ) {
    $new_action = $action;
  } else {
    $new_action = "view_week";
  }

  $block = html_agenda_bar_legend($calendar_entity);
  if ($action != "perform_meeting"  && $output_target != "print") {
    $block .= html_agenda_select_calendar_entity($new_action, $agenda, $calendar_entity, $entity_readable);
    $block .= html_agenda_bar_date_selector($agenda);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the navigation bar
// Parameters:
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function html_agenda_bar_legend($calendar_entity) {
  global $set_theme, $ico_user, $ico_resource, $popup_width, $popup_height;
  global $l_meeting_legend, $l_possible, $l_free, $l_occupied;
  global $l_calendar_displayed, $l_resource_displayed;
  global $action;

  // Legend
  if ($action == "perform_meeting" || $action == "perform_res_meeting") {
    $l_legend = $l_meeting_legend;
    $dis_legend = "
    <tr>
      <td class=\"agendaPossible\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
      <td class=\"detailText\">&nbsp;$l_possible</td>
    </tr>
    <tr>
      <td class=\"agendaFree\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
      <td class=\"detailText\">&nbsp;$l_free</td>
    </tr>
    <tr>
      <td class=\"agendaOccupied\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
      <td class=\"detailText\">&nbsp;$l_occupied</td>
    </tr>";
  } else {
    $l_legend = $l_calendar_displayed;
    if (is_array($calendar_entity["user"])) {
      foreach ($calendar_entity["user"] as $id => $data) {
	$dis_legend .= "
      <tr>
       <td style=\"width:10%;\" class=\"".$data["class"]."\" >
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_user\" alt=\"[User]\" />&nbsp;</td>
       <td class=\"detailLabel\">&nbsp;".$data["name"]."</td>
      </tr>";
      }
    }
    if (is_array($calendar_entity["resource"])) {
      foreach ($calendar_entity["resource"] as $id => $data) {
	$dis_legend .= "
      <tr>
       <td style=\"width:10%;\" class=\"".$data["class"]."\" >
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_resource\" alt=\"[resource]\" />&nbsp;</td>
       <td class=\"detailLabel\">&nbsp;".$data["name"]."</td>
      </tr>";
      }
    }
  }

  $block = "
  <div class=\"block\">

  <div class=\"blockTitle\">:: $l_legend</div>
  <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
    $dis_legend
  </table>

  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the "View type" navigation bar component (User, Resource)
// Parameters:
//   - $new_action      : action to be passed
//   - $agenda          : hash values
//   - $calendar_entity : array of entities elements to display
//   - $entity_readable : array of readable entities (["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function html_agenda_select_calendar_entity($new_action, $agenda, $calendar_entity, $entity_readable) {
  global $path, $uid,$c_all, $set_theme, $popup_height, $popup_width, $ico_add;
  global $cagenda_resource;
  global $l_selection, $l_users,$l_resources, $l_validate;
  global $ico_calendar_user0,$ico_calendar_user1,$ico_calendar_user2;
  global $ico_calendar_user3,$ico_calendar_user4,$ico_calendar_user5;
  global $l_group_members, $l_select_group,$l_back_mono, $l_private, $l_public;

  $iso_date = $agenda["date"];
  $this_group = $agenda["entity"]["group_view"];

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
    $sel_group .= "<option value=\"".url_prepare("agenda_index.php?action=$new_action&amp;date=$iso_date&amp;group_id=$g_id&amp;new_group=1")."\" $selected_s>$g_name</option>\n";
  }
  $sel_group .= "</optgroup>
  </select>";
  //-- End Group selection

  // loop through ["user"], ["resource"]
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
      $name = $data["name"];
      $ico = $data["image"];
      $data_id = "data-$type-$id";
      $div_id = "sel_ent-$data_id";
      $sel_ent .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_ent');\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico\"></a>
        $name
        <input value=\"$data_id\" name=\"sel_ent[]\" type=\"hidden\" />
        </div>";
    }
  }

  $url_user = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent&amp;restriction_calendar=calendar";

  if ($cagenda_resource) {
    $url_resource = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent&amp;restriction=user";
    $dis_block_resource = "
    <tr>
      <td class=\"detailLabel\">
      <a href=\"javascript: return false;\" style=\"float:right;\"
      onclick=\"window.open('$url_resource','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add\" alt=\"[Add]\" />
      </a>
      $l_resources
      </td>
    </tr>";
  }

  $dis_back_myview = "<a class=\"agendaLink\" href=\"agenda_index.php?action=$new_action&amp;sel_user_id[]=$uid\">$l_back_mono</a>";

  $block = "
    <div class=\"block\">
    <div class=\"blockTitle\">:: $l_selection</div>

    <table class=\"agendaCal\">
    <tr style=\"text-align: center;\">
      <td>
        $l_group_members
        <form action=\"\" onsubmit=\"return false;\">
        <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
        $sel_group
        </select>
        $dis_back_all_users
        </form>

      </td>
    </tr>
    </table>

    <form action=\"agenda_index.php\" method=\"get\">
    <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
    <input type=\"hidden\" name=\"new_sel\" value=\"1\" />
    <table class=\"agendaCal\">
    <tbody>
    <tr>
      <td class=\"detailLabel\">
      <a href=\"javascript: return false;\" style=\"float:right;\"
      onclick=\"window.open('$url_user','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add\" alt=\"[Add]\" />
      </a>
      $l_users
      </td>    
    </tr>
    $dis_block_resource
    <tr>
      <td class=\"detail\" id=\"sel_ent\">
      $sel_ent
      </td>
    </tr>
    <tr style=\"text-align: center;\">
      <td>
      <input type=\"submit\" value=\"$l_validate\" />
      </td>
    </tr>
    <tr style=\"text-align: center;\">
      <td>
      $dis_back_myview
      </td>
    </tr>
    </tbody>
    </table>
    </form>

    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the navigation date selector 
// Parameters:
//   - $agenda: agenda parameters
///////////////////////////////////////////////////////////////////////////////
function html_agenda_bar_date_selector($agenda) {
  global $set_theme, $cagenda_weekstart, $ico_spacer;
  global $l_go, $l_go_to, $l_daysofweekfirst;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);

  $this_month = get_month($ts_date);
  $this_year = get_year($ts_date);

  $check_week = $ts_date;
  $start_week_time = get_agenda_date_day_of_week(strtotime("$this_year-01-01"), $cagenda_weekstart);
  $end_week_time = $start_week_time + (6 * 25 * 60 * 60);

  $month_time = strtotime("$this_year-01-01");
  $year_time = strtotime(($this_year-3)."-01-01");
  $dis_month = agenda_localizeDate ("month", $ts_date);
  $start_week_day = strtotime($cagenda_weekstart);
  $start_day = get_agenda_date_day_of_week(strtotime("$this_year-$this_month-01"), $cagenda_weekstart);

  $whole_month = TRUE;

  // Week Selection
  do {
    $weekdate     = isodate_format($start_week_time);
    $select_week1 = agenda_localizeDate("week_jump", $start_week_time);
    $select_week2 = agenda_localizeDate("week_jump", $end_week_time);
    if (($check_week >= $start_week_time) && ($check_week <= $end_week_time)) {
      $sel_week .= "
<option value=\"".url_prepare("agenda_index.php?action=view_week&amp;date=$weekdate")."\" selected=\"selected\">$select_week1 - $select_week2</option>";
    } else {
      $sel_week .= "
<option value=\"".url_prepare("agenda_index.php?action=view_week&amp;date=$weekdate")."\">$select_week1 - $select_week2</option>";
    }
    $start_week_time = strtotime ("+1 week", $start_week_time);
    $end_week_time = $start_week_time + (6 * 25 * 60 * 60);
  } while (get_year($start_week_time) <= $this_year);

  // Month Selection
  for ($i=0; $i<12; $i++) {
    $monthdate = isodate_format($month_time);
    $month_month = date("m", $month_time);
    $select_month = agenda_localizeDate("month", $month_time);
    if ($month_month == $this_month) {
      $sel_month .= "<option value=\"".url_prepare("agenda_index.php?action=view_month&amp;date=$monthdate")."\" selected=\"selected\">$select_month</option>\n";
    } else {
      $sel_month .= "<option value=\"".url_prepare("agenda_index.php?action=view_month&amp;date=$monthdate")."\">$select_month</option>\n";
    }
    $month_time = strtotime ("+1 month", $month_time);
  }

  // Year Selection
  for ($i=-3; $i < (3); $i++) {
    $year_year = get_year($year_time);
    $yeardate = isodate_format($year_time);
    if ($year_year == $this_year) {
      $sel_year .= "<option value=\"".url_prepare("agenda_index.php?action=view_year&amp;date=$yeardate")."\" selected=\"selected\">$year_year</option>\n";
    } else {
      $sel_year .= "<option value=\"".url_prepare("agenda_index.php?action=view_year&amp;date=$yeardate")."\">$year_year</option>\n";
    }
    $year_time = strtotime ("+1 year", $year_time);
  }

  // Minicalendar Head
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekfirst[$day_num];
    $dis_minical_head .= "<td align=\"center\" class=\"agendaHead2\">$day</td>\n";
    $start_week_day = strtotime("+1 day", $start_week_day);
  }

  // Minicalendar
  $i = 0;
  do {
    $day = date ("j", $start_day);
    $iso_day = isodate_format($start_day);
    $check_month = get_month($start_day);
    if ($check_month != $this_month) {
      $day = "<a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_day")."\">$day</a>";
    } else {
      if (isodate_format() == $iso_day) {
        $day = "<a class=\"agendaLink3\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_day")."\">$day</a>";
      } else {
        $day = "<a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_day")."\">$day</a>";
      }
    }
    if ($i == 0) $dis_minical .= "<tr>\n";
    $dis_minical .= "<td class=\"agendaCell\">$day</td>\n";
    $start_day = strtotime("+1 day", $start_day);
    $i++;
    if ($i == 7) {
      $dis_minical .= "</tr>\n";
      $i = 0;
      $checkagain = get_month($start_day);
      if ($checkagain != $this_month) $whole_month = FALSE;
    }
  } while ($whole_month == TRUE);

  $block = "
  <div class=\"block\">
  <div class=\"blockTitle\">:: $dis_month</div>
  <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
  <tr>
    $dis_minical_head
  </tr>
    $dis_minical
  </table>
  </div>

  <div class=\"block\">
  <div class=\"blockTitle\">:: $l_go_to</div>
  <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
  <tr>
    <td>
      <form action=\"\" onsubmit=\"return false;\">
      <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
      $sel_year
      </select>
      <input type=\"submit\" value=\"$l_go\" onclick=\"window.location=(this.form.action.options[this.form.action.selectedIndex].value);\" />
      </form>
    </td>
  </tr>
  <tr>
    <td>
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\" /></td>
  </tr>
  <tr>
    <td >
      <form action=\"\">
      <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
      $sel_month
      </select>
      <input type=\"submit\" value=\"$l_go\" onclick=\"window.location=(this.form.action.options[this.form.action.selectedIndex].value);return false;\" />
      </form>
    </td>
  </tr>
  <tr>
    <td>
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\" /></td>
  </tr>
  <tr>
    <td >
      <form action=\"\">
      <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
      $sel_week
      </select>
      <input type=\"submit\" value=\"$l_go\" onclick=\"window.location=(this.form.action.options[this.form.action.selectedIndex].value);return false;\" />
      </form>
    </td>
  </tr>
  </table>
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
// Parameters:
//   - $agenda          : agenda parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_year_planning($agenda, $calendar_entity) {
  global $set_theme,$ico_left_day,$ico_right_day;
  global $cagenda_weekstart;
  global $l_daysofweek,$l_daysofweekshort,$ico_calendar_nouser;  
  global $l_private;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
  $this_year = get_year($ts_date);
  $start_time = strtotime("$this_year-01-01", $ts_date);
  $end_time = strtotime("+1 year", $start_time);
  $prev_year = isodate_format(strtotime("-1 year", $start_time));
  $next_year = isodate_format($end_time);
  $start_day = strtotime($cagenda_weekstart);
   
  $display_date = agenda_localizeDate("month", $ts_date);  
  
  $events = agenda_events_model($start_time,$end_time, $calendar_entity);
  
  for ($i=0; $i<7; $i++) {
    $day_num = date("w","$start_day");
    $day = $l_daysofweekshort[$day_num];
    $dis_day .= "<td class=\"agendaHead2\" style=\"width: 30px;\">$day</td>";
    $start_day = strtotime("+1 day", $start_day); 
  }
  
  $m = 0;	
  $n = 0;
  $ts_minical = $start_time;

  do {
    $minical_month = get_month($ts_minical);
    $minical_year = get_year($ts_minical);
    $ts_month = strtotime("$minical_year-$minical_month-01");
    $iso_month = isodate_format($ts_month);
    $start_day = get_agenda_date_day_of_week($ts_month, $cagenda_weekstart);
    
    $i = 0;
    $whole_month = TRUE;
    $num_week = 0;
    $dis_calendar .= "
    <td style=\"width:210px\">
      <table class=\"agendaCal\">
      <tr>
        <td colspan=\"7\" class=\"agendaHead2\">
          <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_month&amp;date=$iso_month")."\">
      " . agenda_localizeDate ("month", $ts_minical) . "
          </a>
        </td>
      </tr>
      <tr>
        $dis_day
      </tr>
      <tr>
        <td colspan=\"7\">
        <table class=\"agendaYear\">";
    do {
      $day = date ("j", $start_day);
      $iso_day = isodate_format($start_day);
      $check_month = get_month($start_day);
      $dis_event_cal = "";

      if (isset($events[$iso_day])) {
	$dayObj = $events[$iso_day];
	$dis_event_cal ="<table class=\"agendaEventYear\"><tr>";
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
	      $dis_event_cal .= "<td class=\"agendaEventYear\">";
	      $dis_event_cal .= "<img src=\"".C_IMAGE_PATH."/$set_theme/".$data["image"]."\" alt=\"\"/></td>";
	    } else {
	      $dis_event_cal .= "<td class=\"agendaEventYear\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_calendar_nouser\" alt=\"\"/></td>";		
	    }
	    $tr_users++;	   
	  }
	}
	$dis_event_cal .="</tr></table>";	

      }

      if ($check_month != $minical_month) $day = "$day";
      if ($i == 0) $dis_calendar .= "<tr>";
      if ($iso_day == isodate_format()) {
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"agendaMonthOn\"
              onmouseover=\"this.style.backgroundColor='#dddddd'\" 
	      onmouseout=\"this.style.backgroundColor='#f4e5a9'\"
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;date=$iso_day'\">
	  <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_day")."\">$day</a>$dis_event_cal
          </td>";
      }	
      elseif ($check_month == $minical_month) {	   
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"agendaMonthReg\" 
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#ffffff'\" 
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;date=$iso_day'\">
          <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_day")."\">$day</a>$dis_event_cal
          </td>";
      } else {
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"agendaMonthOff\"
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#f2f2f2'\"
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;date=$iso_day'\">
	  <a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_day")."\">$day</a>$dis_event_cal
          </td>";
      }
      $start_day = strtotime("+1 day", $start_day); 
      $i++;
      if ($i == 7) { 
	$num_week++; 
	$dis_calendar .= "</tr>";
        $i = 0;
        $checkagain = get_month($start_day);
        if ($checkagain != $minical_month) $whole_month = FALSE;	
      }
    } while ($whole_month == TRUE);

    if ($num_week < 6) {
      $dis_calendar .= "<tr><td style=\"height:30px;\" class=\"agendaMonthOff\" colspan=\"7\"></td></tr>";
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
  <div class=\"agendaMain\">
  <table class=\"agendaCalYear\">
  <tr>
    <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_year&amp;date=".$prev_year)."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"Previous Month\" /></a>
    </td>
    <td class=\"agendaHead\" colspan=\"3\">
      $this_year
    </td>
    <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_year&amp;date=".$next_year)."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"Next Month\" /></a>
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
//   - $agenda: agenda parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_month_planning($agenda, $calendar_entity) { 
  global $set_theme,$ico_left_day,$ico_right_day;
  global $cagenda_weekstart, $ico_new_event;
  global $cagenda_first_hour, $cagenda_last_hour;
  global $l_private,$l_daysofweek;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
  $this_month = get_month($ts_date);
  $this_year = get_year($ts_date);
  $start_time = get_agenda_date_day_of_week(strtotime("$this_year-$this_month-01"), $cagenda_weekstart);
  $end_time = strtotime("+1 month +6 days", $start_time);
  $next_month = isodate_format(strtotime("+1 month",$ts_date));
  $prev_month = isodate_format(strtotime("-1 month",$ts_date));

  $display_date = agenda_localizeDate("month", $ts_date);  
  $start_week_day = strtotime($cagenda_weekstart);
  $whole_month = TRUE;
  $nb_user = count($calendar_entity["user"]);
  $nb_res = count($calendar_entity["resource"]);
  $nb_ent = $nb_user + $nb_res;
  $td_width = floor(100 / $nb_ent)."px";
   
  $events = agenda_events_model($start_time,$end_time, $calendar_entity);

  // Display the day of a week
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweek[$day_num];
    $dis_day_head .= "
      <td style=\"width:105px;\" class=\"agendaHead2\">$day</td>";
    $start_week_day = strtotime("+1 day", $start_week_day);
  }

  $current_time = $start_time;
  $i = 0;
  do {
    $this_day = date("j", $current_time);
    $iso_day = isodate_format($current_time);
    $check_month = get_month ($current_time);
    if ($check_month != $this_month) {
      $bgclass="class=\"agendaMonthOff\"";
    } elseif (isodate_format(time()) == $iso_day) {
      $bgclass="class=\"agendaMonthOn\"";
    } else {
      $bgclass="class=\"agendaMonthReg\"";
    }
    if ($i == 0) {
      $week_num = get_agenda_week_num($current_time); 
      $dis_month_cal .= "
      <tr>
        <td class=\"agendaHead3\">
        <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_week&amp;date=$iso_day")."\" >$week_num</a>
        </td>";
    }
    $daylink_begin = urlencode(isodate_format(strtotime("+$cagenda_first_hour hours",$current_time),"",1));
    $daylink_end = urlencode(isodate_format(strtotime("+$cagenda_last_hour hours",$current_time),"",1));

    $dis_month_cal .= "
      <td $bgclass style=\"width:105px;height:105px\">
      <a style=\"float:right;\" class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_day")."\">$this_day</a>
      <a href=\"".url_prepare("agenda_index.php?action=new&amp;tf_date_begin=$daylink_begin&amp;tf_date_end=$daylink_end")."\" />
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_new_event\" alt=\"[New]\" />
      </a>";

    if (isset($events[$iso_day])) {      
      $dis_month_cal .= "<table style=\"height:100%;width:100%;\"><tr>";
      $day = $events[$iso_day];
      $days_events = $day->day_events;

      // loop through ["user"], ["resource"]
      foreach ($calendar_entity as $type => $entity) {
	foreach ($entity as $id => $properties) {
	  $dis_month_cal .= "
            <td><table style=\"height:10%;width:$td_width;\">";
	  $list_events = $days_events[$type][$id];
	  if ($list_events != null) {
	    $dis_month_cal .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	    foreach ($list_events as $event) {
	      dis_agenda_event_allday_planning($dis_month_cal, $hidden_div,$event,$nb_ent,$properties);
	    }
	    $dis_month_cal .= "</td>";
	  }
	  $events_data = $day->get_events($id,$type);
	  if (is_array($events_data)) {
	    foreach ($events_data as $event) {
	      $dis_month_cal .= "<tr><td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	      dis_agenda_event_planning("month", $dis_month_cal,$hidden_div,$event,$nb_ent,$properties);	   
	      $dis_month_cal .= "</td></tr>";
	    }
	    
	  } else {
	    $dis_month_cal .= "<tr><td width=\"$td_width\">&nbsp;</td></tr>";
	  }
	  $dis_month_cal .= "</table></td>";	
	}
      }
      $dis_month_cal .= "</tr></table>";
    }

    $dis_month_cal .= "</td>";
    $current_time = strtotime("+1 day", $current_time); 
    $i++;
    if ($i == 7) { 
      $dis_month_cal .= "</tr>\n";
      $i = 0;
      $checkagain = get_month($current_time);
      if ($checkagain != $this_month) $whole_month = FALSE;	
    }
  } while ($whole_month == TRUE);    
  
  $block = "
  <div class=\"agendaMain\">
   <table class=\"agendaCalMonth agendaMonth\">
    <tr>
     <td colspan=\"2\" class=\"agendaHead\">  	 
      <a href=\"".url_prepare("agenda_index.php?action=view_month&amp;date=".$prev_month)."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Month]\" /></a>
     </td>
     <td colspan=\"5\" class=\"agendaHead\">
      $display_date
     </td>
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_month&amp;date=".$next_month)."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Month]\" /></a>
     </td>
    </tr>
    <tr>
     <td  class=\"agendaHead2\">
     </td>
     $dis_day_head 
    </tr>
    <tr>
     <td>
       $dis_month_cal
     </td>
    </tr>
   </table>
  </div>
  $hidden_div
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
// Parameters:
//    - $agenda          : agenda parameters
//    - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_week_planning($agenda, $calendar_entity) {
  global $path, $set_theme, $ico_left_day, $ico_right_day, $ico_new_event;
  global $ico_agenda;
  global $ico_cal_user1, $ico_cal_user2, $ico_cal_user3, $ico_cal_user4;
  global $ico_cal_user5, $ico_cal_user6;
  global $ico_cal_resource1, $ico_cal_resource2, $ico_cal_resource3;
  global $ico_cal_resource4, $ico_cal_resource5, $ico_cal_resource6;
  global $cagenda_weekstart, $set_cal_interval;
  global $cagenda_first_hour, $cagenda_last_hour;
  global $l_private, $l_all_day;

  $cell_width = "72"; // calendar.css update needed too if changed
 
  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);
  $time_unit = 60 / $set_cal_interval;
  $time_unit_sec = 3600 / $set_cal_interval;
  $week_num = get_agenda_week_num($ts_date);
  $next_week = isodate_format(strtotime("+1 week", $ts_date));
  $prev_week = isodate_format(strtotime("-1 week", $ts_date));
  
  $start_week_day = get_agenda_date_day_of_week($ts_date, $cagenda_weekstart);
  $end_week_time = $start_week_day + ((6 * 24 + $cagenda_last_hour) * 3600);
  $start_week_time = strtotime("+$cagenda_first_hour hours",$start_week_day);
  $dis_day_second = ($cagenda_last_hour - $cagenda_first_hour) * 3600;
  
  $start_week = agenda_localizeDate("week", $start_week_time);
  $end_week = agenda_localizeDate("week", $end_week_time);
  
  $display_date = "$start_week - $end_week"; 
  $events = agenda_events_model($start_week_day,$end_week_time,$calendar_entity);

  $nb_user = count($calendar_entity["user"]);
  $nb_res = count($calendar_entity["resource"]);
  $nb_ent = $nb_user + $nb_res;
  $td_width = floor(($cell_width-2)/$nb_ent)."px"; 
 
  $day_duration = 86400; 
  // Week Days headers : loop day by day
  for ($ts = $start_week_time; $ts < $end_week_time; $ts += $day_duration ) {
    $iso_date = isodate_format($ts);
    $this_date_l = agenda_localizeDate("week_list", $ts);
    $daylink_begin = urlencode(isodate_format($ts,"",1));
    $daylink_end = urlencode(isodate_format(strtotime("+$cagenda_last_hour hours",$ts),"",1));
    $daylink_end = urlencode(isodate_format($ts + $dis_day_second,"",1));
    $week_day_list .= "
    <td class=\"agendaCell\" colspan=\"$nb_ent\" style=\"width:${cell_width}px\">
      <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$iso_date")."\">$this_date_l</a>
      <br />
      <a href=\"agenda_index.php?action=new&amp;tf_date_begin=$daylink_begin&amp;tf_date_end=$daylink_end\" >
       <img align=\"middle\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_new_event\" alt=\"[New]\" />
      </a>
     </td>";

    $day = $events[$iso_date];
    $days_events = $day->day_events;
    // loop through ["user"], ["resource"]
    $indice = 0;
    foreach ($calendar_entity as $type => $entity) {
      foreach($entity as $id => $properties) {
	$indice++;
	$ico = "ico_cal_${type}$indice";
        $list_events = $days_events[$type][$id];
	if ($list_events != null) {
	  // we store the ALL DAY event for further display
	  $all_days[$iso_date][$type][$id] = true;
	  $days_cal .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	  foreach($list_events as $event) {
	    dis_agenda_event_allday_planning($days_cal, $hidden_div, $event, $nb_ent, $properties);
	  }
	  $days_cal .= "</td>";
	} else {
	  $days_cal .= "<td style=\"width:$td_width;\" class=\"agendaHead\"><img src=\"".C_IMAGE_PATH."/$set_theme/${$ico}\" alt=\"$type\" /></td>";
	}
      }
    }
  }

  $week_cal .= "
    <tr>
      <td class=\"agendaHour\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_agenda\" alt=\"$l_all_day\" /></td>
      $days_cal
    </tr>";

  // Week Calendar content
  for ($j = $cagenda_first_hour; $j < $cagenda_last_hour; $j++) {
    $delta = $j - $cagenda_first_hour;
    for ($k=0; $k<$set_cal_interval; $k++) {
      if ($k==0) {
        $week_cal .= "\n<tr><td rowspan=\"$set_cal_interval\" class=\"agendaHour\" style=\"height:".($set_cal_interval*18)."px;\" >".($j).":00</td>";
      } else {
        $week_cal .= "\n<tr>";
      }
      $delta2 = $k * $time_unit;
      for ($i=0; $i<7; $i ++) {
        $current_time = strtotime("+$i days +$delta hours +$delta2 minutes",$start_week_time);
        $goto = "$path/agenda/agenda_index.php?action=new&amp;tf_date_begin=".isodate_format($current_time,"",1)."&amp;tf_date_end=".isodate_format($current_time + $time_unit_sec,"",1);
        $iso_date = isodate_format($current_time);
        if (isset($events[$iso_date])) {
          $day = $events[$iso_date];

	  // loop through ["user"], ["resource"]
	  $indice = 0;
	  foreach ($calendar_entity as $type => $entity) {
	    foreach($entity as $id => $properties) {
              $indice ++;
              if ($j == $cagenda_first_hour && $delta2 == 0) {
                $events_data =  $day->get_events_before($current_time,$current_time + $time_unit_sec , $id,$type);
	      } else {
		$events_data = $day->get_events_between($current_time, $current_time + $time_unit_sec, $id,$type);
              }
              $duration = $events_data["duration"];

              $delta_duration = $duration % $time_unit_sec;
              $stime = $current_time + $duration;
              $etime = $stime + $delta_duration;    
              while($delta_duration != 0) {
                $temp_data = $day->get_events_between($stime, $etime, $id,$type);
                if($temp_data == NULL) {
                  break;
                }
                $duration = $events_data["duration"] + $temp_data["duration"];
                $events_data["duration"] = $duration;
                $events_data["events"] = array_merge( $events_data["events"], $temp_data["events"]);
                $delta_duration = $duration % $time_unit_sec;
                $stime = $stime + $duration;
                $etime = $stime + $delta_duration;                
              }          
              if ($events_data != null) { 
		if (is_array($events_data)) {
		  $duration = $events_data["duration"];
		  $list_events = $events_data["events"];

		  $week_cal .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;text-align:left;\" rowspan=\"".ceil($duration/($time_unit_sec))."\">";
		  foreach($list_events as $event) {
		    dis_agenda_event_planning("week", $week_cal,$hidden_div,$event,$nb_ent,$properties);	
		  }
		  $week_cal .= "</td>";
		}
	      } else {
		if ($indice == 1) {
		  $class = " border";
		} else {
		  $class = "";
		}
		if ($all_days[$iso_date][$type][$id]) {
		  $cell_class = "agendaNoEvent$class agendaAllDay$indice";
		} else {
		  $cell_class = "agendaNoEvent$class";
		}
		$week_cal .= "<td width=\"$td_width\" class=\"$cell_class\" onclick=\"window.location='$goto'\"
                onmouseover=\"this.className='agendaNoEventHover$class';this.style.cursor = 'pointer'\"
                onmouseout=\"this.className ='$cell_class'\">&nbsp;</td>";
	      }
	    }
	  }

	} else {
          $week_cal .= "<td class=\"agendaNoEvent border\"  onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='agendaNoEventHover border';this.style.cursor = 'pointer'\"
             onmouseout=\"this.className ='agendaNoEvent border'\" colspan=\"$nb_ent\">&nbsp;</td>";
        }
      }
      $week_cal .= "</tr>";
    }
  }

  $col_span = (7 * $nb_ent) - 1;
  $block = "
  <div class=\"agendaMain\">
   <table class=\"agendaCalWeek\">
    <tr>
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;date=$prev_week")."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" /></a>     
     </td>
     <td colspan=\"$col_span\" class=\"agendaHead\">
      $display_date
     </td>  
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;date=$next_week")."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Week]\" /></a>     
     </td>
    </tr>
    <tr>
    <td class=\"agendaCell\">$week_num</td>
    $week_day_list
    </tr>
    $week_cal
   </table>
  <div>
  $hidden_div";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the day
// Parameters:
//   - $agenda         : agenda parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_day_planning($agenda, $calendar_entity) {
  global $set_theme,$ico_left_day,$ico_right_day, $ico_new_event;
  global $ico_agenda;
  global $ico_cal_user1, $ico_cal_user2, $ico_cal_user3, $ico_cal_user4;
  global $ico_cal_user5, $ico_cal_user6;
  global $ico_cal_resource1, $ico_cal_resource2, $ico_cal_resource3;
  global $ico_cal_resource4, $ico_cal_resource5, $ico_cal_resource6;
  global $cagenda_first_hour, $cagenda_last_hour,$set_cal_interval;
  global $l_private, $l_all_day;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);

  $tomorrows_date = isodate_format(strtotime("+1 day", $ts_date));
  $yesterdays_date = isodate_format(strtotime("-1 day", $ts_date));

  $time_unit = 60 / $set_cal_interval;
  $time_unit_sec = 3600 / $set_cal_interval;
  $week_num = get_agenda_week_num($ts_date);

  $start_time = strtotime("+$cagenda_first_hour hours",$ts_date);
  $end_time = strtotime("+$cagenda_last_hour hours",$ts_date);

  $display_date = agenda_localizeDate("day", $ts_date);

  $events = agenda_events_model($start_time,$end_time, $calendar_entity);
  $nb_user = count($calendar_entity["user"]);
  $nb_res = count($calendar_entity["resource"]);
  $nb_ent = $nb_user + $nb_res;
  
  $day = $events[$iso_date];

  $td_width = floor(460 / $nb_ent)."px";
  $days_events = $day->day_events;

  // Display Header row (all day)
  $hour_day_list .= "<tr><td class=\"agendaHour\" ><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_agenda\" alt=\"$l_all_day\" /></td>";

  // loop through ["user"], ["resource"]
  $indice = 0;
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $properties) {
      $indice++;
      $ico = "ico_cal_${type}$indice";
      $list_events = $days_events[$type][$id];
      if ($list_events != null) {
	// we store the ALL DAY event for further display
	$all_days[$iso_date][$type][$id] = true;
	$hour_day_list .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	foreach ($list_events as $event) {
	  dis_agenda_event_allday_planning($hour_day_list, $hidden_div,$event,$nb_ent,$properties);	
	}
	$hour_day_list .= "</td>";
      } else {
	$hour_day_list .= "<td style=\"width:$td_width;\" class=\"agendaHead\"><img src=\"".C_IMAGE_PATH."/$set_theme/${$ico}\" alt=\"$type\" /></td>";
      }
    }
  }
  $hour_day_list .= "</tr>";

  // Display Calendar
  for ($i = $cagenda_first_hour; $i < $cagenda_last_hour; $i++) {
    for ($k=0; $k<$set_cal_interval; $k++) {
      if ($k==0) {
	$hour_day_list .= "\n<tr><td class=\"agendaHour\"  style=\"height:".($set_cal_interval*18)."px;\"  rowspan=\"$set_cal_interval\" >$i:00</td>"; 
      } else {
	$hour_day_list .= "\n<tr>"; 
      }
      $delta2 = $k * $time_unit;
      $current_time = strtotime("+$i hours +$delta2 minutes",$ts_date);   

      if (is_object($day)) {
	$indice = 0;
	// loop through ["user"], ["resource"]
	foreach ($calendar_entity as $type => $entity) {
	  foreach ($entity as $id => $properties) {
	    $indice ++;
	    if ($i == $cagenda_first_hour && $delta2 == 0) {
	      $events_data = $day->get_events_before($current_time,$current_time + $time_unit_sec, $id, $type);
	    } else {
	      $events_data = $day->get_events_between($current_time, $current_time + $time_unit_sec, $id,$type);	  ;
            }
            $duration = $events_data["duration"];

            $delta_duration = $duration % $time_unit_sec;
            $stime = $current_time + $duration;
            $etime = $stime + $delta_duration;    
            while($delta_duration != 0) {
              $temp_data = $day->get_events_between($stime, $etime, $id,$type);
              if($temp_data == NULL) {
                break;
              }
              $duration = $events_data["duration"] + $temp_data["duration"];
              $events_data["duration"] = $duration;
              $events_data["events"] = array_merge( $events_data["events"], $temp_data["events"]);
              $delta_duration = $duration % $time_unit_sec;
              $stime = $stime + $duration;
              $etime = $stime + $delta_duration;                
            }               
	    if ($events_data != null) {
	      if (is_array($events_data)) {
		$duration = $events_data["duration"];
		$list_events = $events_data["events"];
		$hour_day_list .= "<td  class=\"".$properties["class"]."\" style=\"width:$td_width;\" rowspan=\"".ceil($duration/($time_unit_sec))."\" >";
		foreach ($list_events as $event) {
		  dis_agenda_event_planning("day", $hour_day_list,$hidden_div,$event,$nb_ent,$properties);	
		}
		$hour_day_list .= "</td>";
	      }
	    } else {
	      if ($all_days[$iso_date][$type][$id]) {
		$cell_class = "agendaNoEvent agendaAllDay$indice";
	      } else {
		$cell_class = "agendaNoEvent";
              }
	      $hour_day_list .= "<td style=\"width:$td_width;\" class=\"$cell_class\">&nbsp;</td>";
	    }
	  }
        }
      } else {
	$hour_day_list .= "<td class=\"agendaNoEvent\" colspan=\"$nb_ent\">&nbsp;</td>";
      }      
      $hour_day_list .= "</tr>";
    }
  }

  $daylink_begin = urlencode(isodate_format(strtotime("+$cagenda_first_hour hours",$ts_date),"",1));
  $daylink_end = urlencode(isodate_format(strtotime("+$cagenda_last_hour hours",$ts_date),"",1));
  $block = "

  <div class=\"agendaMain\">
  <table class=\"agendaCalDay\">
  <tr>
    <td colspan=\"".($nb_ent +1)."\" class=\"agendaHead\">
      <a style=\"float:left;\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$yesterdays_date")."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Yesterday]\" /></a>     
      <a style=\"float:right;\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;date=$tomorrows_date")."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Tomorrow]\" /></a>      
      $display_date
    </td>
  </tr>
  <tr>
    <td class=\"agendaHead3\">
      <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;date=$iso_date")."\" class=\"agendaLink\">
      $week_num
      </a>
    </td>
    <td style=\"width:460px;\" colspan=\"$nb_ent\" class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=new&amp;tf_date_begin=$daylink_begin&amp;tf_date_end=$daylink_end")."\" >
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_new_event\" alt=\"[New]\" />
      </a>
    </td>
  </tr> 
  $hour_day_list
  </table>
  </div>
$hidden_div
";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the planning for the month
// Parameters:
//   - $agenda: agenda parameters
//   - $calendar_entity : array of entities to display ["user"] ["resource"] 
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_plain_month_planning($agenda, $calendar_entity) { 
  global $set_theme,$ico_left_day,$ico_right_day;
  global $cagenda_weekstart, $ico_new_event,$l_all_day;
  global $cagenda_first_hour, $cagenda_last_hour;
  global $l_private,$l_daysofweekfirst,$l_daysofweek;
  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
  $this_month = get_month($ts_date);
  $this_year = get_year($ts_date);
  $start_time = strtotime("$this_year-$this_month-01");
  $end_time = strtotime("+1 month", $start_time);
  $next_month = isodate_format(strtotime("+1 month",$ts_date));
  $prev_month = isodate_format(strtotime("-1 month",$ts_date));

  $display_date = agenda_localizeDate("month", $ts_date);  
  $start_week_day = strtotime($cagenda_weekstart);
  
  $nb_user = count($calendar_entity["user"]);
  $nb_res = count($calendar_entity["resource"]);
  
  $nb_ent = $nb_user + $nb_res;
   
  $events = agenda_events_model($start_time,$end_time, $calendar_entity);
  $current_time = $start_time;
  $day_amount = 0;
  while($current_time < $end_time) {
    $day_amount++;
    $day_num = date("w", $current_time);
    $this_day = date("j", $current_time);
    $day_label = $l_daysofweekfirst[$day_num];
    $day_long_label = $l_daysofweek[$day_num];
    $iso_day = isodate_format($current_time);
    if (isset($events[$iso_day])) {
      $empty = false;
      $day = $events[$iso_day];
      $days_events = $day->day_events;
    } else {
      $empty = true;
    }
    $line0 .= "<th>$day_label</th>\n";
    $line1 .= "<td>$this_day</td>\n";
    foreach ($calendar_entity as $type => $entity) {
      foreach ($entity as $id => $properties) {
        $line_id = $type." ".$id;
        $haveevent = false;
        if($empty) {
          $line[$line_id] .= "<td></td>";
        } else {
          $today_events = array();
          $list_events = $days_events[$type][$id];
          $events_data = $day->get_events($id,$type);
          if ($list_events != null) {
            $haveevent = true;
            foreach ($list_events as $event) {
              if ($event->privacy == 1 && $auth->auth["uid"] != $properties["id"]) {
                $today_events[] = "$l_private : $l_all_day";
              } else {
                $today_events[] = $event->title." : $l_all_day";
              }
            }            
          }
          if(is_array($events_data)) {
            $haveevent = true;
            foreach ($events_data as $event) {
              if ($event->privacy == 1 && $auth->auth["uid"] != $id) {
                $today_events[] = "$l_private : ".date("H:i",$event["date"])."-".date("H:i",$event["date"] + $event["event"]->duration);
              } else {
                $today_events[] = $event["event"]->title." : ".date("H:i",$event["date"])."-".date("H:i",$event["date"] + $event["event"]->duration);
              }
            }
          }
          if($haveevent) {
            $hidden_div .= "
              <div id=\"$id-$this_day\" class=\"eventData\" style=\"display:none;position:absolute;z-index:1000;\">
               <div class=\"blockTitle\">::$day_long_label $this_day</div>
               <ul class=\"blockItem\">
               <li class=\"blockItem\">
               ".implode("</li><li class=\"blockItem\">",$today_events)."
                </li>
               </ul>
              </div>";            
            $line[$line_id] .= "<td class=\"".$properties["class"]."\"
                                    onMouseOver=\"agenda_show(event,'$id-$this_day'); return true;\"
                                    onMouseOut=\"agenda_hide('$id-$this_day'); return true;\"></td>";
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
  <div class=\"agendaMain\">
   <table class=\"agendaCalMonth agendaMonth\">

   </table>  
  <table class=\"planning\">
  <colgroup>
  </colgroup>
  <colgroup class=\"main\" span=\"$day_amount\">
  </colgroup> 
  <colgroup class=\"main\" span=\"2\">
  </colgroup>    
  <thead>
    <tr>
     <th>  	 
     <a href=\"agenda_index.php?action=planning&amp;date=$prev_month\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Month]\" />
     </a>
     </th>
     <th colspan=\"$day_amount\" >
      $display_date
     </th>
     <th colspan=\"2\">
     <a href=\"agenda_index.php?action=planning&amp;date=$next_month\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Month]\" />
     </a>
     </th>
     </tr>  
  <tr><th></th>$line0</tr>  
  <tr><th></th>$line1</tr>
  </thead>
  $d_l
  </table>
  <div>
  $hidden_div
    ";    

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display an individual allday event (with popup) 
// Parameters:
//   - $dis_cal    : [return] agenda parameters
//   - $hidden_div : [return] popup hidden div to add
//   - $event      : event object 
//   - $nb_ent     : number of entities to display
//   - $properties :
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_event_allday_planning(&$dis_cal, &$hidden_div,&$event,$nb_user,&$properties) {
  global $path, $auth, $set_theme, $ico_event, $ico_calendar_day, $l_category1;
  global $l_location, $l_private, $l_private_description, $l_begin,$l_end,$l_resource,$l_task,$l_user;

  $id = $event->id;
  $usr_id = $properties["id"];
  $user_info = $properties["name"];  
  if ($event->privacy == 1 && $auth->auth["uid"] != $properties["id"]) {
    $title = $l_private;
    $description = $l_private_description;
    $link = "javascript:return false;";
    $category1 = $l_private;    
  } else {
    $title = $event->title;
    $location = $event->location;
    $link = "$path/agenda/agenda_index.php?action=detailconsult&agenda_id=$id";
    $description = $event->description;
    $category1 = $event->category1;
    $attendee = $event->attendee;
    foreach($attendee as $entity => $entities) {
      $attendee_list .= "<li class=\"blockItem\">".${"l_".$entity}." : ".implode(", ",$entities)."</li>";
    }    
  }    
  $event_head_display = "<img class=\"agendaEvent\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_calendar_day\" alt=\"[D]\" />";

  $dis_cal .= "<a class=\"agendaLink\" onMouseOver=\"agenda_show(event,'$id-$usr_id'); return true;\" onMouseOut=\"agenda_hide('$id-$usr_id'); return true;\" href=\"$link\">$event_head_display</a>";

  $hidden_div .= "
  <div id=\"$id-$usr_id\" class=\"block\" style=\"display:none;position:absolute;z-index:1000;\">
   <div class=\"blockTitle\">:: $title<br />($user_info)</div>
   <ul class=\"blockItem\">
    <li class=\"blockItem\">$l_location : $location</li>
    <li class=\"blockItem\">$l_category1 : $category1</li>
    <li class=\"blockItem\">$description</li>
    $attendee_list
   </ul>
  </div>";
}


///////////////////////////////////////////////////////////////////////////////
// Display an individual event (with popup) 
// Parameters:
//   - $view       : "allday", "week", "month", "day"
//   - $dis_cal    : [return] agenda parameters
//   - $hidden_div : [return] popup hidden div to add
//   - $event      : event array
//   - $nb_ent     : number of entities to display
//   - $properties :
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_event_planning($view, &$dis_cal, &$hidden_div, &$event,$nb_ent,&$properties) {
  global $path, $auth, $set_theme, $ico_event, $ico_calendar_day, $l_category1,$l_resource,$l_task;
  global $l_location, $l_private, $l_private_description, $l_begin,$l_end,$l_user;

  $id = $event["event"]->id;
  $usr_id = $properties["id"];
  $user_info = $properties["name"];
  if ($event["event"]->privacy == 1 && $auth->auth["uid"] != $usr_id) {
    $title = $l_private;
    $description = $l_private_description;
    $link = "javascript:return false;";
    $category1 = $l_private;    
  } else {
    $title = $event["event"]->title;
    $location = $event["event"]->location;
    $link = "$path/agenda/agenda_index.php?action=detailconsult&agenda_id=$id";
    $description = $event["event"]->description;
    $category1 = $event["event"]->category1;
    $attendee = $event["event"]->attendee;
    foreach($attendee as $entity => $entities) {
      $attendee_list .= "<li class=\"blockItem\">".${"l_".$entity}." : ".implode(", ",$entities)."</li>";
    }
  }

  if ($view == "week") {
    if ($nb_ent == 1) {	      
      $event_head_display = "&#8226; $title";
    } else {
      $event_head_display = "<img class=\"agendaEvent\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_event\" alt=\"[D]\" />";
    }
  } else if ($view == "month") {
    if ($nb_ent == 1) {	      
      $event_head_display = date("H:i",$event["date"])."->".date("H:i",$event["date"] + $event["event"]->duration);
    } else {
      $event_head_display = "<img class=\"agendaEvent\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_event\" alt=\"[D]\" />";
    }
  } else if ($view == "day") {
    if ($nb_ent == 1) {
      $event_head_display = "&#8226;
    ".date("H:i:s",$event["date"])." -> ".date("H:i:s",$event["date"] + $event["event"]->duration)."
    ".$title;
    } else {
      $event_head_display = "&#8226;$title";
    }
  } else if ($view == "allday") {
    $event_head_display = "<img class=\"agendaEvent\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_calendar_day\" alt=\"[D]\" />";
  }

  $dis_cal .= "<a class=\"agendaLink\"
    onMouseOver=\"agenda_show(event,'$id-$usr_id'); return true;\"
    onMouseOut=\"agenda_hide('$id-$usr_id'); return true;\"
    href=\"$link\">$event_head_display</a><br />";

  $hidden_div .= "
  <div id=\"$id-$usr_id\" class=\"eventData\" style=\"display:none;position:absolute;z-index:1000;\">
   <div class=\"blockTitle\">:: $title <br />($user_info)</div>
   <ul class=\"blockItem\">
    <li class=\"blockItem\">$l_location : $location</li>
    <li class=\"blockItem\">$l_category1 : $category1</li>
    <li class=\"blockItem\">$l_begin : ".date("Y-m-d H:i:s",$event["date"])." </li>
    <li class=\"blockItem\">$l_end : ".date("Y-m-d H:i:s",$event["date"] + $event["event"]->duration)."</li>
    <li class=\"blockItem\">$description</li>
    $attendee_list
   </ul>
  </div>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Conflicts
// Parameters:
//   - $agenda : agenda hash
//   - $c_q    : conflict list (events)
///////////////////////////////////////////////////////////////////////////////
function html_agenda_dis_conflict($agenda, $c_q) {
  global $set_theme;
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
      $date_b = isodate_format($date_begin);
      $date_e = isodate_format($date_end);
    } else {
      $date_b = datetime_format($date_begin);
      $date_e = datetime_format($date_end);
    }       
    $dis_conflict .= "
      <tr>
        <td class=\"detailForm\">$title</td>
        <td class=\"detailForm\">$location</td>
        <td class=\"detailForm\">$date_b</td>
        <td class=\"detailForm\">$date_e</td>
        <td class=\"detailForm\">$name</td>";

     $dis_conflict .= "</tr>";
  }
  $block = "
  $dis_form_begin
  <div class=\"detailHead\">$l_conflicts</div>
  <table class=\"detail\">
   <tr>
    <td class=\"detailText\">$l_title</td>
    <td class=\"detailText\">$l_location</td>
    <td class=\"detailText\">$l_datebegin</td>
    <td class=\"detailText\">$l_dateend</td>
    <td class=\"detailText\">$l_user</td>    
   </tr>
   $dis_conflict
  </table>
  <div class=\"detailButton\">
   <p class=\"detailButtons\">$dis_form_button</p>
  </div>

  $dis_form_end";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Conflicts
// Parameters:
//   - $agenda:
//   - $conflict:
//   - $event_id: 
//   - $force: 
///////////////////////////////////////////////////////////////////////////////
function html_agenda_conflict_form($agenda) {
  global $l_force_insert, $l_refuse_insert, $l_cancel_insert;
  
  $event_id = $agenda["agenda_id"]; 
  $user_id = $agenda["user_id"];
  $dis_form = "
  <form name=\"f_conflict\" method=\"post\">
   <input type=\"hidden\" name=\"agenda_id\" value=\"$event_id\" />
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
function dis_agenda_event_consult($id) {
  global $display, $l_err_reference;

  if ($id > 0) {
    $eve_q = run_query_agenda_detail($id);
    if ($eve_q->num_rows() == 1) {
      $entities = get_agenda_event_entity($id);
      $display["detailInfo"] = display_record_info($eve_q);
      $block = html_agenda_event_consult($eve_q, $entities);
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
function html_agenda_event_consult($event_q, $entities) {
  global $auth, $display, $set_theme, $cagenda_weekstart, $cagenda_resource;
  global $l_users, $l_resources, $l_title, $l_location, $l_priority, $l_private, $l_desc,$l_high;
  global $l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_update,$l_repeat;
  global $l_change_state,$l_groups,$l_all_day;
  global $l_repeatkind,$l_repeatdays,$l_date_repeatend, $l_daily, $l_weekly;
  global $l_wait,$l_task;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_module_agenda;
  global $l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse,$l_repeatfrequency;

  $id = $event_q->f("calendarevent_id");
  $title = $event_q->f("calendarevent_title");
  $location = $event_q->f("calendarevent_location");
  $cats1 = of_category_get_entitycategories("calendar", "category1", $id, "mono");
  $priority = $event_q->f("calendarevent_priority"); 
  $description = $event_q->f("calendarevent_description");
  
  $repeatfrequency = $event_q->f("calendarevent_repeatfrequence");
  $all_day = $event_q->f("calendarevent_allday");
  if ($all_day) {
    $dis_all_day = "<tr><td colspan=\"2\" class=\"detailLabel\">$l_all_day</td></tr>";
    $datebegin = isodate_format($event_q->f("calendarevent_date"));
    $dateend = isodate_format($event_q->f("calendarevent_date") + $event_q->f("calendarevent_duration"));
  } else {
    $datebegin = datetime_format($event_q->f("calendarevent_date"));
    $dateend = datetime_format($event_q->f("calendarevent_date") + $event_q->f("calendarevent_duration"));
  }
  $privacy = $event_q->f("calendarevent_privacy");
  $kind = ${"l_".$event_q->f("calendarevent_repeatkind")};
  $repeat_days =$event_q->f("calendarevent_repeatdays"); 
  if ($kind != "none") {
    $repeat_end = date_format($event_q->f("calendarevent_endrepeat"));
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
 
  $start_week_day = strtotime($cagenda_weekstart);
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekshort[$day_num];
    if (strcmp(substr($repeat_days,$i,1),"1")==0) {
      $dis_repeat_days .= "$day ";
    }
    $start_week_day = strtotime("+1 day", $start_week_day); 
  }

  $attendee = $entities["user"];
  if (is_array($attendee["entity"])) {
    foreach ($attendee["entity"] as $u_id => $entity) {
      $label = $entity["label"];
      $state = $entity["state"];
      if ($auth->auth["uid"] == $u_id) {
	$$state = "checked=\"checked\"";
	$dis_decision_radio = "  
      <div class=\"detailHead\">$l_change_state</div>
      <table class=\"detail\">  
      <tr>
        <td colspan=\"2\" class=\"detailText\">
	<input type=\"hidden\" name=\"action\" value=\"update_decision\" />
	<input type=\"hidden\" name=\"agenda_id\" value=\"$id\" />
	<input $A type=\"radio\" name=\"rd_decision_event\" value=\"A\" onclick=\"this.form.submit()\" />$l_accept
	<input $W type=\"radio\" name=\"rd_decision_event\" value=\"W\" onclick=\"this.form.submit()\" />$l_wait
	<input $R type=\"radio\" name=\"rd_decision_event\" value=\"R\" onclick=\"this.form.submit()\" />$l_refuse
        </td>
      </tr>
      </table>";
      }
      $dis_user_list .= "$label ($state)<br />";
    }
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
        <td class=\"detailLabel\">$l_task</td>
        <td class=\"detailText\">$dis_task_list</td>
      </tr>
    ";
  }

  if ($cagenda_resource) {
    $res = $entities["resource"];
    if (is_array($res["entity"])) {
      foreach ($res["entity"] as $r_id => $entity) {
	$label = $entity["label"];
	$state = $entity["state"];
	$dis_resource_list .= "$label<br />";
      }
    }
    $dis_resource_block = "
  <tr>
    <td class=\"detailLabel\">$l_resources</td>
    <td class=\"detailText\">$dis_resource_list</td>
  </tr>
";
  }

  $display["title"] = "<div class=\"title\">$l_module_agenda : $title</div>";
  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"get\" name=\"f_mod_decision\">
  <div class=\"detailHead\">$l_module_agenda</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_title</td>
    <td class=\"detailText\">$title</td>
  </tr>  
  <tr>
    <td class=\"detailLabel\">$l_location</td>
    <td class=\"detailText\">$location</td>
  </tr>  
  <tr>
    <td class=\"detailLabel\">$l_private</td>
    <td class=\"detailText\">$privacy</td>
  </tr> 
  $block_category1
  <tr>
    <td class=\"detailLabel\">$l_users</td>
    <td class=\"detailText\">$dis_user_list</td>
  </tr>
  $dis_resource_block
  $dis_task_block
  </table>
  <div class=\"detailHead\">$l_carac</div>
   <table class=\"detail\">   
   $dis_all_day
    <tr>
     <td class=\"detailLabel\">$l_datebegin</td>
     <td class=\"detailText\">$datebegin</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_dateend</td>
     <td class=\"detailText\">$dateend</td>       
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_priority</td>
     <td class=\"detailText\">$priority</td>
    </tr>      
   </table>
   <div class=\"detailHead\">$l_repeat</div>
   <table class=\"detail\">   
    <tr>    
     <td class=\"detailLabel\">$l_repeatkind</td> 
     <td class=\"detailText\">$kind</td>
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_repeatfrequency</td> 
     <td class=\"detailText\">$repeatfrequency</td>
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_date_repeatend</td> 
     <td class=\"detailText\">$repeat_end</td>
    </tr>
    <tr>  
     <td class=\"detailLabel\">$l_repeatdays</td>
     <td class=\"detailText\">$dis_repeat_days</td>
    </tr>
   </table>

   <div class=\"detailHead\">$l_desc</div>
   <table class=\"detail\">   
   <tr>    
     <td colspan=\"2\" class=\"detailText\">$description</td>
   </tr>   
   </table>

    $dis_decision_radio
  </form>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the event form
// Parameters:
//   - $action   : action called
//   - $agenda   : agenda parameters
//   - $event_q  : DBO : event result
//   - $entities : entities linked (event) or selected ["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_event_form($action, $agenda, $event_q, $entities) {

  if ($action == "detailupdate") {
    $users = $entities["user"];
    $resources = $entities["resource"];
    $tasks = $entities["task"];
  } else {
    $users_id = $entities["user"];
    if (count($users_id > 0)) {
      $users = get_userobm_from_ids($users_id);
    } else {
      $users = array();
    }
    $res_id = $entities["resource"];
    if (count($res_id > 0)) {
      $resources = get_agenda_resource_from_ids($res_id);
    } else {
      $resources = array();
    }
    $groups_id = $entities["group"];
    if (count($groups_id > 0)) {
      $groups = get_agenda_group_from_ids($groups_id);
    } else {
      $groups = array();
    }
  }

  $cats1 = of_category_get_ordered("calendar", "category1");
  $block = html_agenda_event_form($action, $agenda, $event_q, $users, $resources, $groups,$tasks, $cats1);
  //$block = html_agenda_event_form($action, $agenda, $event_q, $users, $groups, $cats1);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $action       : action called
//   - $agenda       : agenda parameters
//   - $event_q      : DBO : event result
//   - $users        : Array : Event users or selected users [ids] [entity]
//   - $res          : Array : Event resources or selected res [ids] [entity]
//   - $groups       : Array : selected groups [ids] [entity]
//   - $cats1        : Array : Calendar categories1
///////////////////////////////////////////////////////////////////////////////
function html_agenda_event_form($action, $agenda, $event_q, $users, $res, $groups,$tasks, $cats1) {
  global $path, $display, $set_theme,$auth;
  global $cagenda_resource, $cagenda_weekstart, $set_cal_interval,$ico_project;
  global $ico_mini_cal, $ico_add_user, $ico_add_resource, $ico_add_group,$ico_delete;
  global $cagenda_first_hour, $cagenda_last_hour,$ico_project;
  // -- Labels
  global $l_users, $l_title, $l_location, $l_priority, $l_private, $l_desc;
  global $l_high,$l_low,$l_medium,$l_datebegin,$popup_height,$popup_width;
  global $l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_update,$l_repeat;
  global $l_repeatkind,$l_repeatdays, $l_date_repeatend, $l_daily, $l_weekly;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_module_agenda;
  global $l_none,$l_force,$l_repeat_update,$l_sendamail,$l_groups,$l_task;
  global $l_agenda_select_group, $l_hour,$l_all_day,$l_repeatfrequency;
  global $l_resources,$l_format,$l_daysofweekshort,$l_task;

  $time_unit = 60 / $set_cal_interval;  
  $repeatfrequency = 1;

  if ($action == "detailupdate") {
    $title = htmlspecialchars($event_q->f("calendarevent_title"));
    $location = htmlspecialchars($event_q->f("calendarevent_location"));
    $cat1 = $event_q->f("calendarevent_category1_id");
    $priority = $event_q->f("calendarevent_priority");
    $desc = $event_q->f("calendarevent_description");
    $datebegin = $event_q->f("calendarevent_date");
    $hourbegin = date("H",$datebegin);
    $minbegin = date("i",$datebegin);
    $datebegin = isodate_format($datebegin);
    $dateend = $event_q->f("calendarevent_date") + $event_q->f("calendarevent_duration");
    $hourend = date("H",$dateend);
    $minend = date("i",$dateend);
    $dateend = isodate_format($dateend);
    $all_day = $event_q->f("calendarevent_allday");
    $privacy = $event_q->f("calendarevent_privacy");
    $repeat_kind = $event_q->f("calendarevent_repeatkind");
    $repeat_days = $event_q->f("calendarevent_repeatdays");
    $repeatfrequency = $event_q->f("calendarevent_repeatfrequence");
    if ($event_q->f("calendarevent_endrepeat")) {
      $repeat_end = isodate_format($event_q->f("calendarevent_endrepeat"));
    }
    $t_id = $tasks["ids"][0];
    $t_name = $tasks["entity"][$t_id]["label"];
    $id = $event_q->f("calendarevent_id");

  } elseif ($action == "new") {
    $repeat_end = isodate_format(strtotime("+1 year"));
  }

  // If parameters have been given, they supercede the default action value
  if (isset($agenda["agenda_id"])) { $id = $agenda["agenda_id"]; }
  if (isset($agenda["title"])) { $title = stripslashes($agenda["title"]); }
  if (isset($agenda["location"])) { $location = stripslashes($agenda["location"]); }
  if (isset($agenda["category1"])) { $cat1 = $agenda["category1"]; }
  if (isset($agenda["priority"])) { $priority = $agenda["priority"]; }
  if (isset($agenda["description"])) { $desc = stripslashes($agenda["description"]); }
  if (isset($agenda["allday"])) { $all_day = $agenda["allday"]; }
  if (isset($agenda["repeatfrequency"])) { $repeatfrequency = $agenda["repeatfrequency"]; }  
  if (isset($agenda["date_begin"])) {
    $ts = isodatetime_to_timestamp($agenda["date_begin"]);
    $datebegin = isodate_format($ts);
    $hourbegin = get_hour($ts);
    $minbegin = date("i", $ts);
  }
  if (isset($agenda["date_end"])) {
    $ts = isodatetime_to_timestamp($agenda["date_end"]);
    $dateend = isodate_format($ts);
    $hourend = get_hour($ts);
    $minend = date("i", $ts);
  }
  if (isset($agenda["privacy"])) { $privacy = $agenda["privacy"]; }
  if (isset($agenda["repeat_kind"])) { $repeat_kind = $agenda["repeat_kind"]; }
  if (isset($agenda["repeat_days"]) && $agenda["repeat_days"] != "000000") { $repeat_days = $agenda["repeat_days"];}
  if (isset($agenda["repeat_end"])) {
    $repeat_end = $agenda["repeat_end"];
  }
  if (isset($agenda["force"])) { $force = $agenda["force"]; }
  if ($repeat_kind != "weekly") {
    $repeat_days = "000000";
  }
  if (isset($agenda["task_id"])) { $t_id = $agenda["task_id"]; }
  if (isset($agenda["task_name"])) { $t_name = stripslashes($agenda["task_name"]); }
  if (isset($agenda["task_new_id"])) { $t_new_id = $agenda["task_new_id"]; }
  if (isset($agenda["task_new_name"])) { $t_new_name = stripslashes($agenda["task_new_name"]); }

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"agenda_id\" id=\"agenda_id\" value=\"$id\" />
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
    <select name=\"sel_repeat_kind\">
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
  $start_week_day = strtotime($cagenda_weekstart);

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
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }

  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id";
   
  $block_user = "
    <td class=\"detailLabel\">
    $l_users
    <a href=\"javascript: return false;\" 
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_user\" alt=\"[Add]\" />
      </a>
     </td>
     <td class=\"detailForm\" id=\"sel_user_id\">
       $dis_sel_user
     </td>";

  // group select
  if (is_array($groups["entity"])) {
    foreach ($groups["entity"] as $g_id => $g_ent) {
      $g_name = $g_ent["label"];
      $sel_id = "data-group-$g_id";
      $div_id = "sel_group_id-$sel_id";
      $dis_sel_group .= "
        <div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_group_id');\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
        </a>
        $g_name
        <input value=\"$sel_id\" name=\"sel_group_id[]\" type=\"hidden\" />
        </div>";
    }
  }

  $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_group_id";

  $block_group = "
    <td class=\"detailLabel\">
    $l_groups
    <a href=\"javascript: return false;\" 
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_group\" alt=\"[Add]\" />
      </a>
     </td>    
     <td class=\"detailForm\" id=\"sel_group_id\">
       $dis_sel_group
     </td>
";

  if ($cagenda_resource) {
    // resource select
    if (is_array($res["entity"])) {
      foreach ($res["entity"] as $r_id => $r_ent) {
	$r_name = $r_ent["label"];
	$sel_id = "data-resource-$r_id";
	$div_id = "sel_resource_id-$sel_id";
	$dis_sel_resource .= "
          <div class=\"elementRow\" id=\"$div_id\">
          <a href=\"javascript: remove_element('$div_id','sel_resource_id');\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
          </a>
          $r_name
          <input value=\"$sel_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
          </div>";
      }
    }

    $url = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_resource_id";

    $block_resource = "
    <td class=\"detailLabel\">
    $l_resources
    <a href=\"javascript: return false;\" 
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_resource\" alt=\"[Add]\" />
      </a>
     </td>    
     <td class=\"detailForm\" id=\"sel_resource_id\">
       $dis_sel_resource
     </td>
";
  }
  
  $block_task = "
    <td class=\"detailLabel\">$l_task</td>
    <td class=\"detailForm\">
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
        uri = '&user_ids[]=".$auth->auth["uid"]."';
      return uri;
    }
    </script>
      $t_name
      <input type=\"hidden\" name=\"task_id\" value=\"$t_id\" />
      <input type=\"hidden\" name=\"task_name\" value=\"$t_name\" />
      <input type=\"hidden\" name=\"task_new_id\" value=\"$t_new_id\" />
      <a href=\"\" onclick=\"url = get_selected_users_url();window.open('$path/project/project_index.php?action=ext_get_task_id&amp;popup=1&amp;ext_widget=f_entity.task_new_id&amp;ext_widget_text=f_entity.task_new_name'+url,'Project','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project\" alt=\"\" /></a>
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

  $dis_hour_b = "<select name=\"sel_time_begin\">";
  for ($i=$cagenda_first_hour; $i<$cagenda_last_hour; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourbegin) {
      $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_b .= "</select>";  
 
  $dis_hour_e = "<select name=\"sel_time_end\">";
  for ($i=$cagenda_first_hour; $i<=$cagenda_last_hour; $i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourend) {
      $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_e .= "</select>";  

  $dis_min_b = "<select name=\"sel_min_begin\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if ($current_min  == $minbegin) {
      $dis_min_b .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_b .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_b .= "</select>";  

 $dis_min_e = "<select name=\"sel_min_end\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if ($current_min  == $minend) {
      $dis_min_e .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_e .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_e .= "</select>";
  $display["title"] = "<div class=\"title\">$l_module_agenda : $title</div>";
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[5]";
  $url2 = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[6]&amp;ext_title=" . urlencode($l_agenda_select_group);


  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"get\" name=\"f_entity\"
   onsubmit=\"if(this.tf_title.value == '')this.tf_title.value = this.task_new_name.value; if (check_agenda_calendar(this)) return true; else return false;\"
   action=\"".url_prepare("agenda_index.php")."\">

  <div class=\"detailHead\">$l_module_agenda</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_title</td>
    <td class=\"detailForm\"><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"50\" size=\"25\" value=\"$title\" /></td>
  </tr> 
  <tr>
    <td class=\"detailLabel\">$l_location</td>
    <td class=\"detailForm\"><input type=\"text\" id=\"tf_location\" name=\"tf_location\" maxlength=\"60\" size=\"25\" value=\"$location\" /></td>
  </tr> 
  <tr>
    <td class=\"detailLabel\">$l_sendamail</td>
    <td class=\"detailForm\"><input type=\"checkbox\" id=\"cba_mail\" value=\"1\" name=\"cba_mail\" /></td>
  </tr>       
  <tr>
    <td class=\"detailLabel\">$l_force</td>
    <td class=\"detailForm\">$dis_force</td>
  </tr>   
  <tr>
    <td class=\"detailLabel\">$l_private</td>
    <td class=\"detailForm\">$dis_privacy</td>
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

  <div class=\"detailHead\">$l_carac</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_all_day</td>
    <td class=\"detailForm\">
      <input onchange=\"show_hide_agenda_dates(this)\" onclick=\"show_hide_agenda_dates(this)\" type=\"checkbox\" name=\"cba_all_day\" value=\"1\" $all_day_checked />
    </td>
  </tr>   
  <tr>
    <td class=\"detailLabel\">$l_datebegin</td>
    <td class=\"detailForm\">
      <script>calendar('tf_date_begin','$datebegin', 'tf_date_end')</script>
      <span id=\"hour_begin\" style=\"$style\" >$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_dateend</td>
    <td class=\"detailForm\">
      <script>calendar('tf_date_end','$dateend')</script>
      <span id=\"hour_end\" style=\"$style\" >$l_hour : $dis_hour_e : $dis_min_e</span>
    </td>       
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_priority</td>
    <td class=\"detailForm\">$dis_sel_prio</td>
  </tr>      
  </table>

  <div class=\"detailHead\">$l_repeat</div>
  <table class=\"detail\">    
  <tr>    
    <td class=\"detailLabel\">$l_repeatkind</td> 
    <td class=\"detailForm\">$dis_sel_kind</td>
    </tr>
  <tr>    
    <td class=\"detailLabel\">$l_repeatfrequency</td> 
    <td class=\"detailForm\"><input type=\"text\" size=\"4\" name=\"tf_repeatfrequency\" value=\"$repeatfrequency\" /></td>
  </tr>    
  <tr>    
    <td class=\"detailLabel\">$l_date_repeatend</td> 
    <td class=\"detailForm\">
      <script>calendar('tf_repeat_end','$repeat_end')</script>
    </td>
  </tr>
  <tr>  
    <td class=\"detailLabel\">$l_repeatdays</td>
    <td class=\"detailForm\">$dis_repeat_day</td>
  </tr>
  </table>
  <div class=\"detailHead\">$l_desc</div>
  <table class=\"detail\">     
  <tr>    
    <td colspan=\"2\" class=\"detailLabel\"><textarea name=\"ta_event_description\" rows=\"3\" cols=\"72\">$desc</textarea></td> 
  </tr>     
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    $hidden_repeat
  </form>
";

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Waiting events
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_agenda_waiting_events($obm_wait) {
  global $l_users, $l_title, $l_location, $l_priority, $l_category1,$l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin2,$l_timeend2,$l_repeat,$l_change_state,$l_validate,$l_from;
  global $l_repeatkind,$l_repeatdays2,$l_date_repeatend,$l_daily,$l_weekly,$cagenda_weekstart,$l_wait;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse;
   
  while ($obm_wait->next_record()) {
    $current_user = $obm_wait->f("userobm_id");
    $current_lname = $obm_wait->f("userobm_lastname");
    $current_fname = $obm_wait->f("userobm_firstname");
    if ($current_user != $old_user) {
      $old_user = $current_user;
      if ($dis_events != "") {
	if($is_repeat) {
	  $dis_kind = $l_repeatkind;
	  $dis_day = $l_repeatdays2;
	  $dis_end = $l_date_repeatend;
	} else {
	  $dis_kind = "";
	  $dis_day = ""; 
	  $dis_end = "";
	}
        $block .= "
	<p/>
	<div class=\"detailHead\">$current_fname $current_lname :</div>
	<table class=\"detail\">
	 <tr>
	  <td class=\"detailText\">$l_title</td>
	  <td class=\"detailText\">$l_location</td>
	  <td class=\"detailText\">$l_category1</td>
	  <td class=\"detailText\">$l_priority</td>
	  <td class=\"detailText\">$l_from</td>
	  <td class=\"detailText\">$dis_kind</td>
	  <td class=\"detailText\">$dis_day</td>
	  <td class=\"detailText\">$dis_end</td>     
	  <td class=\"detailText\">$l_datebegin</td>
	  <td class=\"detailText\">$l_dateend</td>
	  <td></td>
	</tr>
	$dis_events
	</table>";
      }
      $is_repeat = false;
      $dis_events = "";
    }
    $kind = ${"l_".$obm_wait->f("calendarevent_repeatkind")};
    switch ($obm_wait->f("calendarevent_priority") ) {
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
	
    $start_week_day = strtotime($cagenda_weekstart);
    $dis_repeat_days = "";
    for ($i=0; $i<7; $i++) {
      $day_num = date("w", $start_week_day);
      $day = $l_daysofweekshort[$day_num];
      if (strcmp(substr($obm_wait->f("calendarevent_repeatdays"),$i,1),"1")==0) {
	$dis_repeat_days .= "$day ";
      }
      $start_week_day = strtotime("+1 day", $start_week_day); 
    }
    $user_create = $obm_wait->f("usercreate_firstname")." ".$obm_wait->f("usercreate_lastname");
    $date_begin = datetime_format($obm_wait->f("calendarevent_date"));
    $date_end = datetime_format($obm_wait->f("calendarevent_date") + $obm_wait->f("calendarevent_duration"));
    $date_repeat = date_format($obm_wait->f("calendarevent_endrepeat"));
    if ($obm_wait->f("calendarevent_repeatkind") != "none") {$is_repeat = true;}
    $dis_events .= "
    <tr>
     <td class=\"detailForm\">".$obm_wait->f("calendarevent_title")."</td>
     <td class=\"detailForm\">".$obm_wait->f("calendarevent_location")."</td>
     <td class=\"detailForm\">".$obm_wait->f("calendarcategory1_label")."</td>
     <td class=\"detailForm\">$priority</td>
     <td class=\"detailForm\">$user_create</td>
     <td class=\"detailForm\">$kind</td>
     <td class=\"detailForm\">$dis_repeat_days</td>
     <td class=\"detailForm\">$date_repeat</td>
     <td class=\"detailForm\">$date_begin</td>
     <td class=\"detailForm\">$date_end</td>
     <td class=\"detailForm\">
      <form method=\"get\" action=\"agenda_index.php\">
       <input type=\"hidden\" name=\"action\" value=\"decision\" />      
       <input type=\"hidden\" name=\"agenda_id\" value=\"".$obm_wait->f("calendarevent_id")."\" />
       <input type=\"hidden\" name=\"user_id\" value=\"$current_user\" />
       <input type=\"radio\" name=\"rd_decision_event\" value=\"A\"  onclick=\"this.form.submit()\" />$l_accept<br />
       <input type=\"radio\" name=\"rd_decision_event\" value=\"W\" checked=\"checked\" onclick=\"this.form.submit()\" />$l_wait<br />
       <input type=\"radio\" name=\"rd_decision_event\" value=\"R\" onclick=\"this.form.submit()\" />$l_refuse
      </form>
     </td>
    </tr>";
  }
  if ($is_repeat) {
    $dis_kind = $l_repeatkind;
    $dis_day = $l_repeatdays2;
    $dis_end = $l_date_repeatend;
  } else {
    $dis_kind = "";
    $dis_day = ""; 
    $dis_end = "";
  }
  // --- HTML Template --------------------------------------------------------
  $block .= "
  <p/>
  <div class=\"detailHead\">$current_fname $current_lname :</div>
  <table class=\"detail\">
   <tr>
    <td class=\"detailText\">$l_title</td>
    <td class=\"detailText\">$l_location</td>
    <td class=\"detailText\">$l_category1</td>
    <td class=\"detailText\">$l_priority</td>
    <td class=\"detailText\">$l_from</td>
    <td class=\"detailText\">$dis_kind</td>
    <td class=\"detailText\">$dis_day</td>
    <td class=\"detailText\">$dis_end</td>     
    <td class=\"detailText\">$l_datebegin</td>
    <td class=\"detailText\">$l_dateend</td>
    <td></td>
  </tr>
  $dis_events
  </table>";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Delete Options
// Parameters:
//   - $agenda:
///////////////////////////////////////////////////////////////////////////////
function html_agenda_dis_delete($agenda) {
  global $uid, $auth, $set_theme, $ico_validate,$ico_cancel,$ico_confirm_mail;
  global $display, $l_confirm_delete,$l_confirm,$l_cancel,$l_confirm_mail;
  global $l_event_cant_delete, $perms_admin;

  $perms = $auth->auth["perm"];
  $iso_date = $agenda["date"];
  $event_id = $agenda["agenda_id"];
  $e = get_agenda_event_info($event_id);
  $usercreate = $e["usercreate"];

  if (($uid != $usercreate) && ($perms != $perms_admin)) {
    $display["msg"] = display_warn_msg($l_event_cant_delete);
    return;
  }

  $block = "
  <div class=\"detail\">
   <div class=\"messageWarning\">$l_confirm_delete</div>
   <div class=\"agendaDelete\">
    <a href=\"agenda_index.php?action=delete&agenda_id=$event_id&cba_mail=1\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_confirm_mail\" alt=\"[ValidateMail]\"/></a>
     $l_confirm_mail
    <a href=\"agenda_index.php?action=delete&agenda_id=$event_id\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_validate\" alt=\"[Validate]\"  width=\"16\"/></a>
     $l_confirm
    <a href=\"agenda_index.php?action=detailconsult&agenda_id=$event_id&date=$iso_date\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_cancel\" alt=\"[Cancel]\" width=\"16\" /></a>
     $l_cancel
   </div>
  </div>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $agenda        : agenda parameters
//   - $sel_entity_id : array of entity Id ["user"] ["resource"]
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_meeting_form($agenda, $sel_entity_id) {
  global $display, $path, $set_theme, $set_cal_interval;
  global $ico_delete, $ico_mini_cal, $ico_add_user, $ico_add_group,$ico_add_resource;
  global $popup_height,$popup_width,$cagenda_resource,$ico_add_resourcegroup;
  // -- Labels
  global $l_users,$l_datebegin,$l_dateend,$l_format,$l_header_meeting,$l_event_duration,$l_meeting_perform; 
  global $l_groups,$l_resources,$l_module_resourcegroup;

  $time_unit = 60 / $set_cal_interval;
  $datebegin = $agenda["date"];
  $dis_button = "
     <input type=\"hidden\" name=\"agenda_id\" id=\"agenda_id\" value=\"$id\" />
     <input type=\"hidden\" name=\"action\" id=\"action\" value=\"perform_meeting\" />
     <input type=\"submit\" value=\"$l_meeting_perform\" /> 
";

  $users_id = $sel_entity_id["user"];
  if (count($users_id > 0)) {
    $users = get_userobm_from_ids($users_id);
  } else {
    $users = array();
  }
  $res_id = $sel_entity_id["resource"];
  if (count($res_id > 0)) {
    $res = get_agenda_resource_from_ids($res_id);
  } else {
    $res = array();
  }
  $groups_id = $sel_entity_id["group"];
  if (count($groups_id > 0)) {
    $groups = get_agenda_group_from_ids($groups_id);
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
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }

  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id";
   
  $block_user = "
    <td class=\"detailLabel\">
    $l_users
    <a href=\"javascript: return false;\" 
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_user\" alt=\"[Add]\" />
    </a>
    </td>    
    <td class=\"detailForm\" id=\"sel_user_id\">
      $dis_sel_user
    </td>
  ";
  if ($cagenda_resource) {
    if (is_array($res["entity"])) {
      foreach ($res["entity"] as $r_id => $r_ent) {
	$r_name = $r_ent["label"];
	$sel_id = "data-resource-$r_id";
	$div_id = "sel_resource_id-$sel_id";
	$dis_sel_resource .= "
          <div class=\"elementRow\" id=\"$div_id\">
          <a href=\"javascript: remove_element('$div_id','sel_resource_id');\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
          </a>
          $r_name
          <input value=\"$sel_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
          </div>";
      }
    }  
    $url_resource = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_resource_id";
    $dis_block_resource = "
    <tr>
      <td class=\"detailLabel\">
      $l_resources
      <a href=\"javascript: return false;\" 
      onclick=\"window.open('$url_resource','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_resource\" alt=\"[Add]\" />
      </a>      
      </td>
     <td class=\"detailForm\" id=\"sel_resource_id\">
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
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
          </a>
          $r_name
          <input value=\"$sel_id\" name=\"sel_resource_group_id[]\" type=\"hidden\" />
          </div>";
      }
    }  
    $url_resource_group = "$path/resourcegroup/resourcegroup_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_resource_group_id";
    $dis_block_resource_group = "
    <tr>
      <td class=\"detailLabel\">
      $l_module_resourcegroup
      <a href=\"javascript: return false;\" 
      onclick=\"window.open('$url_resource_group','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_resourcegroup\" alt=\"[Add]\" />
      </a>      
      </td>
     <td class=\"detailForm\" id=\"sel_resource_group_id\">
       $dis_sel_resource_group
     </td>      
    </tr>";
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
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
        </a>
        $g_name
        <input value=\"$sel_id\" name=\"sel_group_id[]\" type=\"hidden\" />
        </div>";
    }
  }

  $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_group_id";

  $block_group = "
    <td class=\"detailLabel\">
    $l_groups
    <a href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_group\" alt=\"[Add]\" />
      </a>
     </td>
     <td class=\"detailForm\" id=\"sel_group_id\">
       $dis_sel_group
     </td>
";


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
  $display["title"] = "<div class=\"title\">$l_header_meeting</div>";
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[0]";

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"post\" name=\"f_entity\"
   onsubmit=\"if (agenda_check_meeting(this)) return true; else return false;\"
   action=\"".url_prepare("agenda_index.php")."\">
  <div class=\"detailHead\">$l_header_meeting</div>
  <table class=\"detail\">
  <tr>
    $block_user
  </tr>
  <tr>
    $block_group
  </tr>
  $dis_block_resource  
  $dis_block_resource_group
  <tr>
    <td class=\"detailLabel\">$l_datebegin $l_format</td>
    <td class=\"detailForm\">
  		<script>calendar('tf_date_begin','$datebegin')</script>
    </td>
  </tr>
  <tr>    
    <td class=\"detailLabel\">$l_event_duration</td>
    <td class=\"detailForm\">$dis_hour_d : $dis_min_d</td>
  </tr>
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
  </div>    
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Free meeting performing and display
// Parameters:
//   - $agenda       : agenda parameters
//   - $entity_store : array of entities to display
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_free_interval($agenda, $entity_store) {
  global $set_theme,$ico_left_day,$ico_right_day;
  global $set_cal_interval,$cal_entity_id;
  global $cagenda_weekstart, $cagenda_first_hour, $cagenda_last_hour;
  global $l_private,$l_private_description,$display,$l_free_resources,$l_all_f;              

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);
  $time_unit = 60 / $set_cal_interval; 
  $time_unit_sec = 3600 / $set_cal_interval;; 
  $duration = $agenda["meeting_duration"] * 3600;
  $param_duration = $agenda["meeting_duration"];

  $nb_group = count($cal_entity_id["resource_group"]);
  $week_num = get_agenda_week_num($ts_date); 
  $next_week = isodate_format(strtotime("+1 week", $ts_date));
  $prev_week = isodate_format(strtotime("-1 week", $ts_date));
  
  $start_week_day = get_agenda_date_day_of_week($ts_date, $cagenda_weekstart);
  $end_week_time = $start_week_day + ((6 * 24 + $cagenda_last_hour) * 3600);
  $start_week_time = strtotime("+$cagenda_first_hour hours",$start_week_day);

  if(is_array($entity_store["resourcegroup"]) && count($entity_store["resourcegroup"]) > 0) {
    $have_resource_grp = true;
  } else {
    $have_resource_grp = false;
  }

  $start_week = agenda_localizeDate("week", $start_week_time);
  $end_week = agenda_localizeDate("week", $end_week_time);
  $display_date = "$start_week - $end_week"; 
  $events = agenda_events_model($start_week_day,$end_week_time,$entity_store);
  $day_duration = 86400; 
  $resource_div .= "
  <div id=\"ressource-default\" class=\"block\" style=\"display:none;position:absolute;z-index:1000;\">
  <div class=\"blockTitle\">:: $l_free_resources $l_all_f</div>
  </div>";  
  /* Week Days headers */
  for ($i = $start_week_time; $i < $end_week_time; $i += $day_duration ) {
    $this_date_l = agenda_localizeDate("week_list", $i);
    $week_day_list .= "
     <td class=\"agendaCell\" colspan=\"$nb_user\" style=\"width:72px\">
      $this_date_l 
     </td>";
  } 
  /* Events of the week */
  for ($j = $cagenda_first_hour; $j < $cagenda_last_hour; $j++) {
    $delta = $j - $cagenda_first_hour;
    for ($k=0; $k<$set_cal_interval; $k++) {
      if ($k==0) {
	$week_cal .= "\n<tr><td rowspan=\"$set_cal_interval\" class=\"agendaHour\" >".($j).":00</td>";
      } else {
	$week_cal .= "\n<tr>";
      }
      $delta2 = $k * $time_unit;
      for ($i = 0; $i <7; $i ++) {
	$current_time = strtotime("+$i days +$delta hours +$delta2 minutes",$start_week_time);
        $dayLink = isodate_format($current_time);
        $end_time = $current_time + $duration;
	$begin_date = urlencode(isodate_format($current_time,"",1));
	$end_date = urlencode(isodate_format($end_time,"",1));
        $goto = "agenda_index.php?action=new&amp;tf_date_begin=$begin_date&amp;tf_date_end=$end_date";
	if (isset($events[$dayLink])) {
          $day = $events[$dayLink];
	  $have_event = $day->have_events_between($current_time, $current_time + $time_unit_sec,"user");
          $will_have_event = $day->have_events_between($current_time,$end_time,"user");
          if(!$have_event) {
            $have_event = $have_event | $day->have_events_between($current_time, $current_time + $time_unit_sec,"resource");
            $will_have_event = $will_have_event | $day->have_events_between($current_time,$end_time,"resource");
          }
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
              $grp_have_event = $day->entity_have_events_between($current_time, $current_time + $time_unit_sec,$grid,"resourcegroup");
              $grp_will_have_event =   $day->entity_have_events_between($current_time,$end_time,$grid,"resourcegroup");
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
                  $resource_ul .= html_agenda_ressource_list($group[0]["groupLabel"],$possible_ressources[$groupId]);
                  $resource_url .= "&amp;sel_resource_id[]=".$possible_ressources[$groupId][0]["id"];
                }else {
                  $resource_ul .= html_agenda_ressource_list($group[0]["groupLabel"],$group,true);
                  $resource_url .= "&amp;sel_resource_id[]=".$free_ressources[$groupId][0]["id"];
                }
            }
              
             $resource_div .= "
              <div id=\"ressource-$current_time\" class=\"block\" style=\"display:none;position:absolute;z-index:1000;\">
              <div class=\"blockTitle\">:: $l_free_resources </div>
               <br />
               $resource_ul
              </div>";              
            }
          }

          
	  $goto = "agenda_index.php?action=new&amp;tf_date_begin=$begin_date&amp;tf_date_end=$end_date".$resource_url;
	  if ($have_event) {
	    $week_cal .= "<td width=\"$td_width\" class=\"agendaOccupied\">&nbsp;</td>\n";
          } elseif ($will_have_event) {
            if($have_resource_grp) {
              $week_cal .= "<td class=\"agendaFree\" onclick=\"window.location='$goto'\"
               onmouseover=\"this.className='agendaFreeHover';this.style.cursor = 'pointer';agenda_show(event,'ressource-$current_time');\"
               onmouseout=\"this.className ='agendaFree';agenda_hide('ressource-$current_time');\">&nbsp;</td>\n";
            } else {
              $week_cal .= "<td class=\"agendaFree\" onclick=\"window.location='$goto'\"
               onmouseover=\"this.className='agendaFreeHover';this.style.cursor = 'pointer';\"
               onmouseout=\"this.className ='agendaFree';\">&nbsp;</td>\n";
            }
          } else {
            if($have_resource_grp) {
              $week_cal .= "<td class=\"agendaPossible\" onclick=\"window.location='$goto'\"
               onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer';agenda_show(event,'ressource-$current_time');\"
               onmouseout=\"this.className='agendaPossible';agenda_hide('ressource-$current_time');\">&nbsp;</td>\n";
            } else {
              $week_cal .= "<td class=\"agendaPossible\" onclick=\"window.location='$goto'\"
               onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer';\"
               onmouseout=\"this.className='agendaPossible';\">&nbsp;</td>\n";
            }            
	  }
        } else {
          if($have_resource_grp) {    
            $week_cal .= "<td class=\"agendaPossible\" onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer';agenda_show(event,'ressource-default');\"
             onmouseout=\"this.className='agendaPossible';agenda_hide('ressource-default');\">&nbsp;</td>\n";
          } else {
            $week_cal .= "<td class=\"agendaPossible\" onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer';\"
             onmouseout=\"this.className='agendaPossible';\">&nbsp;</td>\n";
          }
        }
      }
      $week_cal .= "</tr>";
    }
  }
  $col_span = 6;
  $block = "
  <div class=\"agendaMain\">
   <table class=\"agendaCalWeek\">
    <tr>
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;date=$prev_week&amp;meeting_duration=$param_duration")."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" /></a>
     </td>
     <td colspan=\"$col_span\" class=\"agendaHead\">
      $display_date
     </td>
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;date=$next_week&amp;meeting_duration=$param_duration")."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Week]\" /></a>     
     </td>
    </tr>
    <tr>
    <td class=\"agendaCell\">$week_num</td>
    $week_day_list
    </tr>
    $week_cal
   </table>
   <div>
   $resource_div
";

  return  $block;
}


/**
 * dis_agenda_right_dis_admin
 * Display agenda rights admin form
 *
 * @param mixed $id agenda(user) id to admin
 * @access public
 * @return mixed xhtml display
 */
function dis_agenda_right_dis_admin($id="") {
  global $uid, $display, $l_agenda;

  if ($id == "") {
    $id = $uid;
  }
  $infos = get_user_info($id);
  $name = $infos["firstname"] . " " . $infos["lastname"];
  $display["title"] = "<div class=\"title\">$l_agenda : $name</div>";
  $block = of_right_dis_admin("calendar", $id);

  return $block;
}


/**
 * html_agenda_ressource_list
 * Build the resources pop up 
 * 
 * @param mixed $groupLabel 
 * @param mixed $resource_group 
 * @param mixed $free 
 * @access public
 * @return void
 */
function html_agenda_ressource_list($groupLabel,$resource_group,$free=false) {
  global $l_free_resources,$ico_warning,$set_theme;
  
  if($free) {
    $class= "agendaFree";
    $ico = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_warning\" alt=\"[Restricted]\" />";
  } else {
    $class = "agendaPossible";
  }
  $style ="style=\"font-weight:bold;\"";
  foreach($resource_group as $resource) {
    $ret .= "<li class=\"blockItem\" $style>  ".$resource["name"]."</li>";
    $style = "";
  }
  return "
  <div class=\"blockTitle\">::$ico $groupLabel</div>
  <ul class=\"blockItem resourceBlock $class\">
  $ret
    </ul>
";
}


///////////////////////////////////////////////////////////////////////////////
// Display the agenda administration index
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_admin_index() {

  $cats1 = of_category_get_ordered("calendar", "category1");
  $block = of_category_dis_admin_form("calendar", "category1", $cats1);;

  return $block;

}


///////////////////////////////////////////////////////////////////////////////
// Perform the export to the vCalendar format
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_agenda_export_handle($agenda) {
  global $obm_version,$cagenda_weekstart,$auth;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
 
  $start_time = strtotime("-1 year", $ts_date);
  $end_time = strtotime("+1 year", $ts_date);

  $calendar_user["user"] = array ($auth->auth["uid"] => "dummy");  
  $ev_q = run_query_agenda_no_repeat_events($start_time,$end_time,$calendar_user);

  header("Content-Type: text/x-vCalendar");
  header("Content-Disposition: inline; filename=ObmCalendar.ics");
  echo "
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//OBM/OBM V$obm_version Calendar//EN
METHOD:PUBLISH";
  while($ev_q->next_record()) {
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
  
  $ev_q = run_query_agenda_repeat_events($start_time,$end_time,$calendar_user);
  
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
      $start_week_day = strtotime($cagenda_weekstart);
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
