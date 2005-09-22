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
//   - $sel_entity_id : array of entity Id
//   - $p_view        : view selected "year", "month", "week" or "day"
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_view($agenda, $sel_entity_id, $p_view="week") {
  global $display;

  $entity_readable = run_query_entity_readable();
  $visible_entities_id = slice_entities($sel_entity_id["user"], $sel_entity_id["resource"], $entity_readable);
  $entity_store = store_entities(run_query_get_entity_label($visible_entities_id));
  $display["features"] = html_planning_bar($agenda, $visible_entities_id, $entity_store, $entity_readable);

  if ($p_view == "year") {
    $block = dis_year_planning($agenda, $entity_store);
  } else if ($p_view == "month") {
    $block = dis_month_planning($agenda, $entity_store);
  } else if ($p_view == "week") {
    $block = dis_week_planning($agenda, $entity_store);
  } else if ($p_view == "day") {
    $block = dis_day_planning($agenda, $entity_store);
  }
  
  return $block;    
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display the navigation bar
// Parameters:
//   - $agenda: agenda parameters
//   - $sel_entity      : array of entity Id
//   - $entity_store    : array of entities elements to display
//   - $entity_readable : array of readable entities (["user"], ["resource"]
///////////////////////////////////////////////////////////////////////////////
function html_planning_bar($agenda, $sel_entity, $entity_store,$entity_readable) {
  global $set_theme, $cagenda_weekstart, $set_cal_interval, $ico_spacer;
  global $l_go_to, $l_calendar_visible, $l_calendar_displayed, $l_back_mono;
  global $l_free,$l_possible,$l_go,$l_occupied, $l_private, $l_public;
  global $l_daysofweekfirst,$l_validate,$l_meeting_legend;
  global $c_all, $l_select_group, $l_everybody, $l_group_members,$l_everyresources;
  global $l_entity,$l_users,$l_resources;
  global $l_resource_visible, $l_resource_displayed, $l_back_mono;
  global $ico_add_resource, $popup_width, $popup_height;
  global $agenda_view_type;
  global $action, $uid;
 
  $iso_date = $agenda["date"];
  $this_group = ($agenda["agenda_group"] ? $agenda["agenda_group"] : $c_all);

  // Dealing with Entities
  if ($agenda_view_type == 'U') {
    $nb_entity = count($entity_store);
    $l_entity = "user";
  } elseif ($agenda_view_type == 'R') {
    $nb_entity = count($entity_store);
    $l_entity = "resource";
  }

  // Entity Selection
  if (($action == "perform_meeting") || ($action == "find_resource")) {
    $duration = $agenda["duration"];     
    $dis_duration = "<input type=\"hidden\" name=\"param_duration\" value=\"$duration\" />";
    $new_action = "$action";
    $p_duration = "&amp;param_duration=$duration";
  } elseif ( ($action != "view_day") && ($action != "view_week") && ($action != "view_month") && ($action != "view_year") ) {
    $new_action = "view_week";
    $check_sel_user = "onsubmit=\"if (check_count_user(this)) return true; else return false;\"";
    $check_sel_resource = "onsubmit=\"if (check_count_resource(this)) return true; else return false;\"";
  } else {
    $new_action = $action;
    $check_sel_user = "onsubmit=\"if (check_count_user(this)) return true; else return false;\"";
    $check_sel_resource = "onsubmit=\"if (check_count_resource(this)) return true; else return false;\"";
  }

  //-- User selection 
  if (count($sel_entity["user"])>1 || !in_array($uid, $sel_entity["user"])) {
    $dis_back_monoview = "<br/><a class=\"agendaLink\" href=\"agenda_index.php?action=$new_action$p_duration&amp;param_group=$c_all&amp;sel_user_id[]=$uid\" >$l_back_mono</a>";
  }

  $dis_sel_user = "<select name=\"sel_user_id[]\" size=\"6\" multiple=\"multiple\" style=\"font-size:10px\">";
  if (is_array($entity_readable["user"]["entity"])) {
    foreach ($entity_readable["user"]["entity"] as $one_entity) {
      $u_id = $one_entity["id"];
      $u_label = $one_entity["label"];
      $dis_sel_user .= "\n<option value=\"$u_id\"";
      
      if (count($sel_entity["user"]) > 0) {
	$tag_user = "";
	while ( (list($key, $user_id) = each($sel_entity["user"]))
		&& ($tag_user == "") ) {
	  if ($u_id == $user_id ) {
	    $tag_user = " selected=\"selected\"";
	  }
	}
	reset($sel_entity["user"]);
      }
      
      $dis_sel_user .= " $tag_user>$u_label</option>";
    } 
  }
  $dis_sel_user .= "</select>
    <br />
    <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
    $dis_duration
    <input type=\"submit\" value=\"$l_validate\" />";
  //-- End User selection 

  //-- Resource selection
  $dis_sel_resource = "<select name=\"sel_resource_id[]\" size=\"6\" multiple=\"multiple\" style=\"font-size:10px\">";
  if (is_array($entity_readable["resource"]["entity"])) {
    foreach ($entity_readable["resource"]["entity"] as $one_entity) {
      $r_id = $one_entity["id"];
      $r_label = $one_entity["label"];
      $dis_sel_resource .= "\n<option value=\"$r_id\"";

      if (count($sel_entity["resource"])>0) {
	$tag_res = "";
	while ( (list($key, $res_id) = each($sel_entity["resource"]) )
		&& ($tag_er == "") ) {
	  if ($r_id == $res_id ) {
	    $tag_res = " selected=\"selected\"";
	  }
	}
	reset($sel_entity["resource"]);
      }
      
      $dis_sel_resource .= " $tag_res>$r_label</option>";
    }
  }
  $dis_sel_resource .= "</select>
    <br />
    <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
    $dis_duration
    <input type=\"submit\" value=\"$l_validate\" />";

  $url = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_resource_id[]&amp;ext_widget=frm_sel_resource.elements[0]";
  $block_resource = "
    <!--
    $l_resources
    <a href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_resource\" alt=\"[Add]\" />
    </a>
    -->
   ";
  //-- End Resource selection 

  //-- Group selection
  if ($this_group == $c_all) {
    $sel_group = "<option selected=\"selected\">$l_select_group</option>";
  } else {
    $dis_back_all_users = "<br /><a class=\"agendaLink\" href=\"agenda_index.php?action=$new_action$p_duration&amp;param_group=$c_all&amp;sel_user_id[]=$uid\" >$l_everybody</a>";
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
    $sel_group .= "<option value=\"".url_prepare("agenda_index.php?action=$new_action&amp;param_date=$iso_date&amp;param_group=$g_id&amp;new_group=1")."\" $selected_s>$g_name</option>\n";
  }
  $sel_group .= "</optgroup>
  </select>";
  //-- End Group selection

  //-- Resource Group selection
  if ($this_group == $c_all) {
    $sel_grpres = "<option selected=\"selected\">$l_select_group</option>";
  } else {
    $dis_back_all_resources = "<br /><a class=\"agendaLink\" href=\"agenda_index.php?action=$new_action$p_duration&amp;param_group=$c_all&amp;sel_resource_id[]=$uid\" >$l_everyresources</a>";
  }

  while ($entity_readable["resourcegroup"]->next_record()) {
    $g_id = $entity_readable["resourcegroup"]->f("rgroup_id");
    $g_title = $entity_readable["resourcegroup"]->f("rgroup_name");

    if ($g_id == $this_group) {
      $selected_s = "selected=\"selected\"";
    } else {
      $selected_s = "";
    }
    $sel_grpres .=  "<option value=\"".url_prepare("agenda_index.php?action=$new_action&amp;param_date=".$getdate."&amp;param_group=$g_id")."\" $selected_s>$g_title</option>\n";
  }
  //-- End Resource Group selection


  // DISPLAY BLOCK
  //-- Legend
  $block = html_bar_legend($entity_store);
  $block .= html_bar_view_type($new_action);

  if ($agenda_view_type == "U") {
    $block .= "
<!-- User select -->
    <div class=\"last\">

    <div class=\"lastTitle\">:: $l_calendar_visible</div>
    <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
    <tr align=\"center\">
      <td>
        $l_group_members
        <form action=\"\" onsubmit=\"return false;\">
        <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
        $sel_group
        </select>
        $dis_back_all_users
        </form>

        <form action=\"agenda_index.php\" method=\"get\" $check_sel_user>
        $dis_sel_user
        <input type=\"hidden\" name=\"param_date\" value=\"$iso_date\" />
        $dis_back_monoview
        </form>
      </td>
    </tr>
    </table>
    </div>
    ";
  } elseif ($agenda_view_type == "R") {
    $block .= "
<!-- Resource select -->
    <div class=\"last\">

     <div class=\"lastTitle\">:: $l_resource_visible</div>
       <tr>
      <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
       <tr align=\"center\">
        <td class=\"detailLabel\">
        $l_groups
         <form action=\"\" onsubmit=\"return false;\">
          <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
            $sel_grpres
          </select>
         </form>
         <form name=\"frm_sel_resource\" action=\"agenda_index.php\" method=\"get\" $check_sel_resource>
          $dis_sel_resource
          <input type=\"hidden\" name=\"param_date\" value=\"$getdate\" />
          $dis_back_resource_reset
         </form>
         $block_resource
        </td>
       </tr>
      </table>
     </div>
     ";
  }

  $block .=  html_bar_date_selector($agenda);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the navigation date selector 
// Parameters:
//   - $agenda: agenda parameters
///////////////////////////////////////////////////////////////////////////////
function html_bar_date_selector($agenda) {
  global $set_theme, $cagenda_weekstart, $ico_spacer;
  global $l_go, $l_go_to, $l_daysofweekfirst;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);

  $this_month = get_month($ts_date);
  $this_year = get_year($ts_date);

  $check_week = $ts_date;
  $start_week_time = get_date_day_of_week(strtotime("$this_year-01-01"), $cagenda_weekstart);
  $end_week_time = $start_week_time + (6 * 25 * 60 * 60);

  $month_time = strtotime("$this_year-01-01");
  $year_time = strtotime(($this_year-3)."-01-01");
  $dis_month = localizeDate ("month", $ts_date);
  $start_week_day = strtotime($cagenda_weekstart);
  $start_day = get_date_day_of_week(strtotime("$this_year-$this_month-01"), $cagenda_weekstart);

  $whole_month = TRUE;
  $nb_user = count($calendar_user);


  // Week Selection
  do {
    $weekdate     = isodate_format($start_week_time);
    $select_week1 = localizeDate("week_jump", $start_week_time);
    $select_week2 = localizeDate("week_jump", $end_week_time);
    if (($check_week >= $start_week_time) && ($check_week <= $end_week_time)) {
      $sel_week .= "
<option value=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$weekdate")."\" selected=\"selected\">$select_week1 - $select_week2</option>";
    } else {
      $sel_week .= "
<option value=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$weekdate")."\">$select_week1 - $select_week2</option>";
    }
    $start_week_time = strtotime ("+1 week", $start_week_time);
    $end_week_time = $start_week_time + (6 * 25 * 60 * 60);
  } while (get_year($start_week_time) <= $this_year);

  // Month Selection
  for ($i=0; $i<12; $i++) {
    $monthdate = isodate_format($month_time);
    $month_month = date("m", $month_time);
    $select_month = localizeDate("month", $month_time);
    if ($month_month == $this_month) {
      $sel_month .= "<option value=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=$monthdate")."\" selected=\"selected\">$select_month</option>\n";
    } else {
      $sel_month .= "<option value=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=$monthdate")."\">$select_month</option>\n";
    }
    $month_time = strtotime ("+1 month", $month_time);
  }

  // Year Selection
  for ($i=-3; $i < (3); $i++) {
    $year_year = get_year($year_time);
    $yeardate = isodate_format($year_time);
    if ($year_year == $this_year) {
      $sel_year .= "<option value=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=$yeardate")."\" selected=\"selected\">$year_year</option>\n";
    } else {
      $sel_year .= "<option value=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=$yeardate")."\">$year_year</option>\n";
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
      $day = "<a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_day")."\">$day</a>";
    } else {
      if (isodate_format() == $iso_day) {
        $day = "<a class=\"agendaLink3\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_day")."\">$day</a>";
      } else {
        $day = "<a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_day")."\">$day</a>";
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
  <div class=\"last\">
  <div class=\"lastTitle\">:: $dis_month</div>
  <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
  <tr>
    $dis_minical_head
  </tr>
    $dis_minical
  </table>
  </div>

  <div class=\"last\">
  <div class=\"lastTitle\">:: $l_go_to</div>
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
// Display the navigation bar
// Parameters:
//   - $agenda: agenda parameters
///////////////////////////////////////////////////////////////////////////////
function html_bar_legend($entity_store) {
  global $l_meeting_legend, $l_possible, $l_free, $l_occupied; 
  global $l_calendar_displayed, $l_resource_displayed;
  global $agenda_view_type, $action;

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
  } elseif ($agenda_view_type == "U") {
    $l_legend = $l_calendar_displayed;
    foreach ($entity_store as $id => $data) {
      $dis_legend .= "
      <tr>
       <td style=\"width:10%;\" class=\"".$data["class"]."\" >&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
       <td class=\"detailLabel\">&nbsp;&nbsp;".$data["name"]."</td>
      </tr>";
    }
  } elseif ($agenda_view_type == "R") {
    $l_legend = $l_resource_displayed;
    foreach ($entity_store as $id => $data) {
      $dis_legend .="
      <tr>
       <td style=\"width:10%;\" class=\"".$data["class"]."\" >&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
       <td class=\"detailLabel\">&nbsp;&nbsp;".$data["name"]."</td>
      </tr>";
    }
  }

  $block = "
  <div class=\"last\">

  <div class=\"lastTitle\">:: $l_legend</div>
  <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
    $dis_legend
  </table>

  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the "View type" navigation bar component (User, Resource)
// Parameters:
//   - $agenda: agenda parameters
///////////////////////////////////////////////////////////////////////////////
function html_bar_view_type($new_action) {
  global $l_view_type,$l_users,$l_resources,$dis_duration,$new_action;
  global $agenda_view_type;

  //-- View type
  $block = "
      <div class=\"last\">
      <div class=\"lastTitle\">:: $l_view_type</div>
      <table class=\"agendaCal\" cellpadding=\"0\" cellspacing=\"0\">
      <tbody>
      <tr>
       <td class=\"detailLabel\">
         <form action=\"\">
           <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
           $dis_duration
           <input";
  if ($agenda_view_type == "U")
    $block .= " checked=\"checked\"";
  $block .= " name=\"rd_view_type\" value=\"U\" onclick=\"this.form.submit()\" type=\"radio\">$l_users<br>
           <input";
  if ($agenda_view_type == "R")
    $block .= " checked=\"checked\"";
  $block .= " name=\"rd_view_type\" value=\"R\" onclick=\"this.form.submit()\" type=\"radio\">$l_resources
         </form>
       </td>
      </tr>
      </tbody>
     </table>
     </div>
  ";

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
function html_dis_conflict($agenda,$conflict) {
  global $set_theme,$auth;
  global $l_title, $l_location, $l_conflicts, $l_datebegin,$l_dateend, $l_user;
  global $l_force_insert,$l_refuse_insert,$l_cancel_insert,$l_validate;

  $user_id = $agenda["user_id"];
  while ($conflict->next_record()) {
    $id = $conflict->f("calendarevent_id"); 
    $title =  $conflict->f("calendarevent_title");
    $location =  $conflict->f("calendarevent_location");
    $user_id =  $conflict->f("calendaruser_id");
    $user_name =  $conflict->f("userobm_lastname")." ".$conflict->f("userobm_firstname");
    $date_begin =  $conflict->f("calendarevent_date");
    $all_day =  $conflict->f("calendarevent_allday");
    $date_end =  $conflict->f("calendarevent_date") + $conflict->f("calendarevent_duration");

    if ($all_day) {
      $dis_all_day = "<tr><td colspan=\"2\" class=\"detailLabel\">$l_all_day</td></tr>";
      $date_b = isodate_format($date_begin);
      $date_e =  isodate_format($date_end);
    } else {
      $date_b = datetime_format($date_begin);
      $date_e =  datetime_format($date_end);
    }       
    $dis_conflict .= "
      <tr>
        <td class=\"detailForm\">$title</td>
        <td class=\"detailForm\">$location</td>
        <td class=\"detailForm\">$date_b</td>
        <td class=\"detailForm\">$date_e</td>
        <td class=\"detailForm\">$user_name</td>";

     $dis_conflict .= "</tr>";
  }
  $block= "
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

  $dis_form_end
  ";
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
function html_conflict_form($agenda) {
  global $l_force_insert, $l_refuse_insert, $l_cancel_insert;
  
  $event_id = $agenda["id"]; 
  $user_id = $agenda["user_id"];
  $dis_form = "
  <form name=\"f_conflict\" method=\"post\">
   <input type=\"hidden\" name=\"param_event\" value=\"$event_id\" />
   <input type=\"hidden\" name=\"action\" value=\"decision\" />
   <input type=\"hidden\" name=\"param_user\" value=\"$user_id\" />
   <input type=\"hidden\" name=\"cb_force\" value=\"1\" />
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
// Display the event form
// Parameters:
//   - $action         : action called
//   - $agenda         : agenda parameters
//   - $event_q        : DBO : event result
//   - sel_entity_id   : array of entities id selected ["user"], ["resource"]..
///////////////////////////////////////////////////////////////////////////////
function dis_event_form($action, $agenda, $event_q, $sel_entity_id) {

  $user_q = run_query_userobm_in($sel_entity_id["user"]);
  $group_q = run_query_group_in($sel_entity_id["group"]);
  $cat_q = run_query_get_eventcategories();
  $block = html_event_form($action, $agenda, $event_q, $user_q, $group_q, $cat_q, $sel_entity_id);      

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $action       : action called
//   - $agenda       : agenda parameters
//   - $event_q      : DBO : event result
//   - $usr_q        : DBO : User listresult
//   - $grp_q        : DBO : Group list
//   - $cat_q        : DBO : Category list
//   - sel_entity_id : array of entities id selected ["user"], ["resource"]..
///////////////////////////////////////////////////////////////////////////////
function html_event_form($action, $agenda, $event_q, $usr_q, $grp_q, $cat_q, $sel_entity_id) {
  global $path, $display, $set_theme,$cagenda_weekstart, $set_cal_interval;
  global $ico_mini_cal, $ico_add_user, $ico_add_resource, $ico_add_group,$ico_delete;
  global $cagenda_first_hour, $cagenda_last_hour;
  // -- Labels
  global $l_users, $l_title, $l_location, $l_priority, $l_private, $l_cat, $l_desc, $l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_update,$l_repeat,$popup_height,$popup_width;
  global $l_repeatkind,$l_repeatdays, $l_date_repeatend, $l_daily, $l_weekly;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_module_agenda,$l_format,$l_daysofweekshort;
  global $l_none,$l_force,$l_repeat_update,$l_sendamail,$l_groups;
  global $l_agenda_select_group, $l_hour,$l_all_day;
  global $l_resources;

  $time_unit = 60 / $set_cal_interval;  

  if ($action == "detailupdate") {
    $title = htmlspecialchars($event_q->f("calendarevent_title"));
    $location = htmlspecialchars($event_q->f("calendarevent_location"));
    $category_id = $event_q->f("calendarevent_category_id");
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
    $repeat_days =$event_q->f("calendarevent_repeatdays");
    if ($event_q->f("calendarevent_endrepeat")) {
      $repeat_end = isodate_format($event_q->f("calendarevent_endrepeat"));
    }
    $id = $event_q->f("calendarevent_id");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($agenda["id"])) { $id = $agenda["id"]; }
  if (isset($agenda["title"])) { $title = $agenda["title"]; }
  if (isset($agenda["location"])) { $location = $agenda["location"]; }
  if (isset($agenda["category"])) { $category_id = $agenda["category"]; }
  if (isset($agenda["priority"])) { $priority = $agenda["priority"]; }
  if (isset($agenda["force"])) { $desc = $agenda["force"]; }
  if (isset($agenda["description"])) { $desc = $agenda["description"]; }
  if (isset($agenda["allday"])) { $all_day = $agenda["allday"]; }
  if (isset($agenda["datebegin"])) {
    $ts = isodate_to_timestamp($agenda["datebegin"]);
    $datebegin = isodate_format($ts);
    $hourbegin = get_hour($ts);
    $minbegin = date("i", $ts);
  }
  if (isset($agenda["dateend"])) {
    $ts = isodate_to_timestamp($agenda["dateend"]);
    $dateend = isodate_format($ts);
    $hourend = get_hour($ts);
    $minend = date("i", $ts);
  }
  if (isset($agenda["privacy"])) { $privacy = $agenda["privacy"]; }
  if (isset($agenda["kind"])) { $repeat_kind = $agenda["kind"]; }
  if (isset($agenda["repeat_days"]) && $agenda["repeat_days"] != "000000") { $repeat_days = $agenda["repeat_days"];}
  if (isset($agenda["repeat_end"])) {
    $repeat_end = $agenda["repeat_end"];
  } else {
    $repeat_end = isodate_format(strtotime("+1 year"));
  }
  if (isset($agenda["force"])) { $force = $agenda["force"]; }
  if ($repeat_kind != "weekly") {
    $repeat_days = "000000";
  }
  
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"param_event\" id=\"param_event\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />
      ";
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
  $sel_cat = "<select name=\"sel_category_id\">"; 
  while ($cat_q->next_record()) {
    $c_id = $cat_q->f("calendarcategory_id");
    $c_label = $cat_q->f("calendarcategory_label");
    $sel_cat .= "<option value=\"$c_id\"";
    if ($c_id == $category_id) {
      $sel_cat .= " selected=\"selected\"";
    }
    $sel_cat .= ">$c_label</option>";
  }
  $sel_cat .= "</select>";

 // Repetition days
  $start_week_day = strtotime($cagenda_weekstart);

  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekshort[$day_num];
    $dis_repeat_day .= "<input type=\"checkbox\" name=\"cb_repeatday_".$i."\" value=\"1\"";
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
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $u_lname = $usr_q->f("userobm_lastname");
    $u_fname = $usr_q->f("userobm_firstname");
    if (in_array($u_id, $sel_entity_id["user"])) {
      $dis_sel_user .= "<div class=\"elementRow\" id=\"sel_user_id$u_id\">
      <a href=\"javascript: remove_element('sel_user_id$u_id','sel_user_id');\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
      </a>
      $u_lname $u_fname
      <input value=\"$u_id\" name=\"sel_user_id[]\" type=\"hidden\" />
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

    // group select
  if (is_object($grp_q)) {
    while ($grp_q->next_record()) {
      $g_id = $grp_q->f("group_id");
      $g_lname = $grp_q->f("group_name");
      if (in_array($g_id, $sel_entity_id["group"])) {
	$dis_sel_group .= "
          <div class=\"elementRow\" id=\"sel_group_id$g_id\">
          <a href=\"javascript: remove_element('sel_group_id$g_id','sel_group_id');\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
          </a>
          $g_lname
          <input value=\"$g_id\" name=\"sel_group_id[]\" type=\"hidden\" />
          </div>";
      }
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

    // resource select
/*
    while ($res_q->next_record()) {
      $r_id = $res_q->f("userobm_id");
      $r_llabel = $res_q->f("userobm_login");
      if (in_array($r_id, $p_resource_array)) {
        $dis_sel_resource .= "<div class=\"elementRow\" id=\"sel_resource_id$r_id\">
      <a href=\"javascript: remove_element('sel_resource_id$r_id','sel_resource_id');\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
      </a>
      $r_llabel
      <input value=\"$r_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
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
*/

  if ($force == 1) {
    $dis_force = "<input type=\"checkbox\" id=\"cb_force\" value=\"1\" checked=\"checked\" name=\"cb_force\" />";
  } else {
    $dis_force = "<input type=\"checkbox\" id=\"cb_force\" value=\"1\" name=\"cb_force\" />";
  }
  
  if ($privacy == 1){
    $dis_privacy = "<input type=\"checkbox\" id=\"cb_privacy\" value=\"1\" checked=\"checked\" name=\"cb_privacy\" />";
  } else {
    $dis_privacy = "<input type=\"checkbox\" id=\"cb_privacy\" value=\"1\" name=\"cb_privacy\" />";
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
  <form method=\"get\" name=\"f_mod_calendar\"
   onsubmit=\"if (check_calendar(this)) return true; else return false;\"
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
    <td class=\"detailForm\"><input type=\"checkbox\" id=\"cb_mail\" value=\"1\" name=\"cb_mail\" /></td>
  </tr>       
  <tr>
    <td class=\"detailLabel\">$l_force</td>
    <td class=\"detailForm\">$dis_force</td>
  </tr>   
  <tr>
    <td class=\"detailLabel\">$l_private</td>
    <td class=\"detailForm\">$dis_privacy</td>
  </tr> 
  <tr>
    <td class=\"detailLabel\">$l_cat</td>
    <td class=\"detailForm\">$sel_cat</td>
  </tr>    
  <tr>
    $block_user
  </tr>
  <tr>
    $block_group
  </tr>    
  </table>

  <div class=\"detailHead\">$l_carac</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_all_day</td>
    <td class=\"detailForm\">
      <input onchange=\"show_hide_dates(this)\" onclick=\"show_hide_dates(this)\" type=\"checkbox\" name=\"ch_all_day\" value=\"1\" $all_day_checked />
    </td>
  </tr>   
  <tr>
    <td class=\"detailLabel\">$l_datebegin $l_format </td>
    <td class=\"detailForm\">
      <input onchange=\"changeDateEnd(this.form)\" type=\"text\" id=\"tf_date_begin\" name=\"tf_date_begin\" maxlength=\"10\" size=\"12\" value=\"$datebegin\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_date_begin,'agenda_index.php?action=calendar&popup=1') ;\">
        <img style=\"vertical-align:middle;\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
      <span id=\"hour_begin\" style=\"$style\" >$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_dateend $l_format</td>
    <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_date_end\" name=\"tf_date_end\" maxlength=\"10\" size=\"12\" value=\"$dateend\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_date_end,'agenda_index.php?action=calendar&popup=1');\">
        <img style=\"vertical-align:middle;\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
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
    <td class=\"detailLabel\">$l_date_repeatend</td> 
    <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_repeat_end\" name=\"tf_repeat_end\" maxlength=\"10\" size=\"12\" value=\"$repeat_end\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_repeat_end,'agenda_index.php?action=calendar&popup=1');\">
        <img style=\"vertical-align:middle;\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>     
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
function html_waiting_events($obm_wait) {
  global $l_users, $l_title, $l_location, $l_priority, $l_cat,$l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin2,$l_timeend2,$l_repeat,$auth,$l_change_state,$l_validate,$l_from;
  global $l_repeatkind,$l_repeatdays2,$l_date_repeatend,$l_daily,$l_weekly,$cagenda_weekstart,$l_wait;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse;
   
  while ($obm_wait->next_record()) {
    $current_user = $obm_wait->f("userobm_id");
    $current_lname = $obm_wait->f("userobm_lastname");
    $current_fname = $obm_wait->f("userobm_firstname");
    if($current_user != $old_user) {
      $old_user = $current_user;
      if($dis_events != "") {
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
	  <td class=\"detailText\">$l_cat</td>
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
     <td class=\"detailForm\">".$obm_wait->f("calendarcategory_label")."</td>
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
       <input type=\"hidden\" name=\"param_event\" value=\"".$obm_wait->f("calendarevent_id")."\" />
       <input type=\"hidden\" name=\"param_user\" value=\"$current_user\" />
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
    <td class=\"detailText\">$l_cat</td>
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
// Display Calendar Consult
// Parameters:
//   - $event_q    : event_query result
//   - $$attendee_q : attendee query result
///////////////////////////////////////////////////////////////////////////////
function html_calendar_consult($event_q, $attendee_q) {
  global $display, $set_theme,$cagenda_weekstart;
  // -- Labels
  global $l_users, $l_title, $l_location, $l_priority, $l_private, $l_cat, $l_desc,$l_high;
  global $l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_update,$l_repeat;
  global $auth,$l_change_state,$l_groups,$l_all_day;
  global $l_repeatkind,$l_repeatdays,$l_date_repeatend, $l_daily, $l_weekly;
  global $l_wait,$l_none;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_module_agenda;
  global $l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse;
  
  $title = $event_q->f("calendarevent_title");
  $location = $event_q->f("calendarevent_location");
  $category = $event_q->f("calendarcategory_label");
  $priority = $event_q->f("calendarevent_priority"); 
  $description = $event_q->f("calendarevent_description");
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
  $id = $event_q->f("calendarevent_id");

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
    
  while ($attendee_q->next_record()) {
    $lname = $attendee_q->f("userobm_lastname");
    $fname = $attendee_q->f("userobm_firstname");
    $state = $attendee_q->f("state");
    if ($auth->auth["uid"] == $attendee_q->f("userobm_id")) {
      $$state = "checked=\"checked\"";
      $dis_decision_radio = "  
      <div class=\"detailHead\">$l_change_state</div>
      <table class=\"detail\">  
      <tr>
        <td colspan=\"2\" class=\"detailText\">
	<input type=\"hidden\" name=\"action\" value=\"update_decision\" />
	<input type=\"hidden\" name=\"param_event\" value=\"$id\" />
	<input $A type=\"radio\" name=\"rd_decision_event\" value=\"A\" onclick=\"this.form.submit()\" />$l_accept
	<input $W type=\"radio\" name=\"rd_decision_event\" value=\"W\" onclick=\"this.form.submit()\" />$l_wait
	<input $R type=\"radio\" name=\"rd_decision_event\" value=\"R\" onclick=\"this.form.submit()\" />$l_refuse
        </td>
      </tr>
      </table>";
    }
    $dis_user_list .= "$lname $fname ($state)<br />";
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
    <tr>
     <td class=\"detailLabel\">$l_cat</td>
     <td class=\"detailText\">$category</td>
    </tr>    
    <tr>
     <td class=\"detailLabel\">$l_users</td>
     <td class=\"detailText\">$dis_user_list</td>
    </tr>
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
// Display Delete Options
// Parameters:
//   - $agenda:
///////////////////////////////////////////////////////////////////////////////
function html_dis_delete($agenda) {
  global $uid, $auth, $set_theme, $ico_validate,$ico_cancel,$ico_confirm_mail;
  global $display, $l_confirm_delete,$l_confirm,$l_cancel,$l_confirm_mail;
  global $l_event_cant_delete, $perms_admin;

  $perms = $auth->auth["perm"];
  $iso_date = $agenda["date"];
  $event_id = $agenda["id"];
  $e = get_event_info($event_id);
  $usercreate = $e["usercreate"];

  if (($uid != $usercreate) && ($perms != $perms_admin)) {
    $display["msg"] = display_warn_msg($l_event_cant_delete);
    return;
  }

  $block = "
  <div class=\"detail\">
   <div class=\"messageWarning\">$l_confirm_delete</div>
   <div class=\"agendaDelete\">
    <a href=\"agenda_index.php?action=delete&param_event=$event_id&cb_mail=1\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_confirm_mail\" alt=\"[ValidateMail]\"/></a>
     $l_confirm_mail
    <a href=\"agenda_index.php?action=delete&param_event=$event_id\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_validate\" alt=\"[Validate]\"  width=\"16\"/></a>
     $l_confirm
    <a href=\"agenda_index.php?action=detailconsult&param_event=$event_id&param_date=$iso_date\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_cancel\" alt=\"[Cancel]\" width=\"16\" /></a>
     $l_cancel
   </div>
  </div>
  ";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $agenda : agenda parameters
//   - $usr_q  : array of selected contacts 
//   - $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_meeting_form($agenda, $usr_q, $p_user_array) {
  global $display, $path,$ico_delete, $ico_mini_cal, $ico_add_user;
  global $set_theme, $set_cal_interval;
  global $popup_height,$popup_width;
  // -- Labels
  global $l_users,$l_datebegin,$l_dateend,$l_format,$l_header_meeting,$l_event_duration,$l_meeting_perform; 
  global $l_groups;

  $time_unit = 60 / $set_cal_interval;
  $datebegin = $agenda["date"];
  $dis_button = "
     <input type=\"hidden\" name=\"param_event\" id=\"param_event\" value=\"$id\" />
     <input type=\"hidden\" name=\"action\" id=\"action\" value=\"perform_meeting\" />
     <input type=\"submit\" value=\"$l_meeting_perform\" /> 
     ";

  // userobm select
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $u_lname = $usr_q->f("userobm_lastname");
    $u_fname = $usr_q->f("userobm_firstname");
    if (in_array($u_id, $p_user_array)) {
      $dis_sel_user .= "
      <div class=\"elementRow\" id=\"sel_user_id$u_id\">
        <a href=\"javascript: remove_element('sel_user_id$u_id','sel_user_id');\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
        </a>
        $u_lname $u_fname
        <input value=\"$u_id\" name=\"sel_user_id[]\" type=\"hidden\" />
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

  // group select
  $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_group_id";

  $block_group = "
    <td class=\"detailLabel\">
    $l_groups
    <a href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_user\" alt=\"[Add]\" />
    </a>
    </td>
    <td class=\"detailForm\" id=\"sel_group_id\"></td>
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
  <form method=\"get\" name=\"f_mod_calendar\"
   onsubmit=\"if (check_meeting(this)) return true; else return false;\"
   action=\"".url_prepare("agenda_index.php")."\">
  <div class=\"detailHead\">$l_header_meeting</div>
  <table class=\"detail\">
  <tr>
    $block_user
  </tr>
  <tr>
    $block_group
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_datebegin $l_format</td>
    <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_date_begin\" name=\"tf_date_begin\" maxlength=\"10\" size=\"12\" value=\"$datebegin\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_date_begin,'agenda_index.php?action=calendar&popup=1');\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
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
//   - $agenda        : agenda parameters
//   - $calendar_user : array of selected users 
///////////////////////////////////////////////////////////////////////////////
function dis_free_interval($agenda, $calendar_user) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer ,$ico_event,$auth;
  global $set_cal_interval,$ico_new_event;
  global $cagenda_weekstart, $cagenda_first_hour, $cagenda_last_hour;
  global $l_private,$l_private_description,$display;              

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);
  $time_unit = 60 / $set_cal_interval; 
  $time_unit_sec = 3600 / $set_cal_interval;; 
  $duration = $agenda["duration"] * 3600;
  $param_duration = $agenda["duration"];

  $week_num = get_week_num($ts_date); 
  $next_week = isodate_format(strtotime("+1 week", $ts_date));
  $prev_week = isodate_format(strtotime("-1 week", $ts_date));
  
  $start_week_day = get_date_day_of_week($ts_date, $cagenda_weekstart);
  $end_week_time = $start_week_day + ((6 * 24 + $cagenda_last_hour) * 3600);
  $start_week_time = strtotime("+ $cagenda_first_hour hours",$start_week_day);
  
  $start_week = localizeDate("week", $start_week_time);
  $end_week = localizeDate("week", $end_week_time);
  $display_date = "$start_week - $end_week"; 
  $events = events_model($start_week_day,$end_week_time,$calendar_user);
  $day_duration = 86400; 

  /* Week Days headers */
  for ($i = $start_week_time; $i < $end_week_time; $i += $day_duration ) {
    $this_date_l = localizeDate("week_list", $i);
    $week_day_list .= "
     <td class=\"agendaCell\" colspan=\"$nb_user\" style=\"width:72px\">
      $this_date_l 
     </td>";
  } 
  /* Events of the week */
  for ($j = $cagenda_first_hour; $j < $cagenda_last_hour; $j++) {
    $delta = $j - $cagenda_first_hour;
    for ($k=0;$k<$set_cal_interval;$k++) {
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
	$goto = "agenda_index.php?action=new&amp;tf_date_begin=$begin_date&amp;tf_date_end=$end_date$param_user$param_group";
	if (isset($events[$dayLink])) {
	  $day = $events[$dayLink];
	  $have_event = $day->have_events_between($current_time, $current_time + $time_unit_sec);
	  $will_have_event = $day->have_events_between($current_time,$end_time);
	  $goto = "agenda_index.php?action=new&amp;tf_date_begin=$begin_date&amp;tf_date_end=$end_date$param_user$param_group";
	  if ($have_event) {
	    $week_cal .= "<td width=\"$td_width\" class=\"agendaOccupied\">&nbsp;</td>\n";
          } elseif ($will_have_event) {
	    $week_cal .= "<td class=\"agendaFree\" onclick=\"window.location='$goto'\"
	     onmouseover=\"this.className='agendaFreeHover';this.style.cursor = 'pointer'\"
	     onmouseout=\"this.className ='agendaFree'\">&nbsp;</td>\n";
	  } else {
	    $week_cal .= "<td class=\"agendaPossible\" onclick=\"window.location='$goto'\"
	     onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer'\"
	     onmouseout=\"this.className='agendaPossible'\">&nbsp;</td>\n";
	  }
	} else {
	  $week_cal .= "<td class=\"agendaPossible\" onclick=\"window.location='$goto'\"
	   onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer'\"
	   onmouseout=\"this.className='agendaPossible'\">&nbsp;</td>\n";
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
      <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;param_date=$prev_week$param_user$param_group&amp;param_duration=$param_duration")."\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" />
      </a>     
     </td>
     <td colspan=\"$col_span\" class=\"agendaHead\">
      $display_date
     </td>  
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;param_date=$next_week$param_user$param_group&amp;param_duration=$param_duration")."\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Week]\" />
      </a>     
     </td>
    </tr>
    <tr>
    <td class=\"agendaCell\">$week_num</td>
    $week_day_list
    </tr>
    $week_cal
   </table>
  <div>
";

  return  $block;
}


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Free meeting performing and display
// Parameters:
//   - $agenda         : agenda parameters
//   - $event_qs   : list of event's records 
//   - $contacts_array : array of selected contacts 
//   - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_free_ressource($agenda,$calendar_user){
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer ,$ico_event,$auth;
  global $cagenda_weekstart, $set_cal_interval,$ico_new_event;
  global $cagenda_first_hour, $cagenda_last_hour;
  global $l_private,$l_private_description,$display;              

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);
  $time_unit = 60 / $set_cal_interval; 
  $time_unit_sec =  3600 / $set_cal_interval;; 
  $duration = $agenda["duration"] * 3600;
  $param_duration = $agenda["duration"];

  $week_num = get_week_num($ts_date);
  $next_week = isodate_format(strtotime("+1 week", $ts_date));
  $prev_week = isodate_format(strtotime("-1 week", $ts_date));
  
  $start_week_day = get_date_day_of_week($ts_date, $cagenda_weekstart);
  $end_week_time = $start_week_day + ((6 * 24 + $cagenda_last_hour) * 3600);
  $start_week_time = strtotime("+ $cagenda_first_hour hours",$start_week_day);
  
  $start_week = localizeDate("week", $start_week_time);
  $end_week = localizeDate("week", $end_week_time);
  $display_date = "$start_week - $end_week"; 
  $events = events_model($start_week_day,$end_week_time,$calendar_user);
  $day_duration = 86400; 

  /* Week Days headers */
  for ($i = $start_week_time; $i < $end_week_time; $i += $day_duration ) {
    $this_date_l = localizeDate("week_list", $i);
    $week_day_list .= "
     <td class=\"agendaCell\" colspan=\"$nb_user\" style=\"width:72px\">
      $this_date_l 
     </td>";
  } 
  /* Events of the week */
  for ($j = $cagenda_first_hour; $j < $cagenda_last_hour; $j++) {
    $delta = $j - $cagenda_first_hour;
    for ($k=0;$k<$set_cal_interval;$k++) {
      if ($k==0) {
	$week_cal .= "\n<tr><td rowspan=\"$set_cal_interval\" class=\"agendaHour\" >".($j).":00</td>";
      } else {	
	$week_cal .= "\n<tr>";
      }
      $delta2 = $k * $time_unit;
      for ($i = 0; $i <7; $i ++) {
	$current_time = strtotime("+$i days +$delta hours +$delta2 minutes",$start_week_time);
        $iso_day = isodate_format($current_time);
        $end_time = $current_time + $duration;
	if (isset($events[$iso_day])) {
	  $restricted = array();
	  $free = array();
	  $day = $events[$iso_day];
	  $begin_date = isodate_format($current_time,"",1);
	  $end_date = isodate_format($end_time,"",1);
	  foreach ($calendar_user as $id => $data ) {
	    $have_event = $day->user_have_events_between($current_time, $current_time + $time_unit_sec,$id);
            $will_have_event = $day->user_have_events_between($current_time, $end_time,$id);
	    if (!$have_event) {
	      if ($will_have_event) {
		$restricted[] = $data;
	      } else {
		$free[] = $data;
	      }
	    }
	  }
	  $goto = "agenda_index.php?action=new&amp;tf_date_begin=$begin_date&amp;tf_date_end=$end_date";	  
	  if (count($free) != 0) {
	    $week_cal .= "<td class=\"agendaPossible\" onclick=\"window.location='$goto&amp;sel_user_id[]=".$free[0]["id"]."'\"
	     onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer';show(event,'$current_time')\";
	     onmouseout=\"this.className='agendaPossible';hide('$current_time');\">&nbsp;</td>\n";
	     $hidden_div .= ressources_free_display($free,$restricted,$current_time);
	   } elseif(count($restricted) != 0) {
	    $week_cal .= "<td class=\"agendaFree\" onclick=\"window.location='$goto&amp;sel_user_id[]=".$restricted[0]["id"]."'\" 
	     onmouseover=\"this.className='agendaFreeHover';this.style.cursor = 'pointer';show(event,'$current_time')\"
	     onmouseout=\"this.className ='agendaFree';hide('$current_time');\">&nbsp;</td>\n";
	     $hidden_div .= ressources_free_display($free,$restricted,$current_time);
	  } else {
	    $week_cal .= "<td width=\"$td_width\" class=\"agendaOccupied\">&nbsp;</td>\n";
	  }
	} else {
	  $first = reset($calendar_user);
	  $week_cal .=  "<td class=\"agendaPossible\" onclick=\"window.location='$goto&amp;sel_user_id[]=".$first["id"]."'\"
	   onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer';show(event,'$current_time')\"
	   onmouseout=\"this.className='agendaPossible';hide('$current_time');\">&nbsp;</td>\n";
	   $restricted=array();
	   $hidden_div .= ressources_free_display($calendar_user,$restricted,$current_time);
        }
      }
      $week_cal .=  "</tr>";
    }
  }
  $col_span =  6;
  $block = "
  <div class=\"agendaMain\">
   <table class=\"agendaCalWeek\">
    <tr>
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;param_date=$prev_week$param_user&amp;param_duration=$param_duration")."\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" />
      </a>     
     </td>
     <td colspan=\"$col_span\" class=\"agendaHead\">
      $display_date
     </td>  
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;param_date=$next_week$param_user&amp;param_duration=$param_duration")."\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Week]\" />
      </a>     
     </td>
    </tr>
    <tr>
    <td class=\"agendaCell\">$week_num</td>
    $week_day_list
    </tr>
    $week_cal
   </table>
  <div>
  $hidden_div
  ";

  return  $block;
}


///////////////////////////////////////////////////////////////////////////////
// Make the event display 
// Parameters:
//    - $agenda         : agenda parameters
//    - $event_qs   : list of event's records 
//    - $contacts_array : array of selected contacts 
//    - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function ressources_free_display($free, $restrected, $current_time) {

  $dis_free = "<ul>";
  foreach($free as $ressource) {
    $dis_free .= "<li>".$ressource["name"]."</li>";
  }
  $dis_free .= "</ul>";
  $dis_restricted = "<ul>";
  foreach($restrected as $ressource) {
    $dis_restricted .= "<li>".$ressource["name"]." (restricted)</li>";
  }
  $dis_restricted .= "</ul>";
  $hidden_div .= "
  <div id=\"$current_time\" class=\"last\" style=\"display:none;\"> 
   $dis_free
   $dis_restricted
  </div>  ";	   

  return $hidden_div;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display the agenda administration index
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $category_q = run_query_get_eventcategories();
  return html_agenda_category_form($category_q);
}


///////////////////////////////////////////////////////////////////////////////
// Company Category section
// Parameters:
//   - $category_q : Category list database object
///////////////////////////////////////////////////////////////////////////////
function html_agenda_category_form($category_q) {
  global $l_category_manage,$l_category_exist;
  global $l_category_checkdelete,$l_category_update,$l_category_new,$l_category_insert;

  $sel_category = "<select name=\"sel_category\" size=\"4\" >";
  while ($category_q->next_record()) {
    $label = $category_q->f("calendarcategory_label");
    $id = $category_q->f("calendarcategory_id");
    $sel_category .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_category .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_category_manage
      </td>
    </tr><tr>
      <td>

<!-- Category Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_admin_category_delete\" method=\"post\"
              action=\"" . url_prepare("agenda_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_category_exist</td>
              <td class=\"adminLabel\">$sel_category</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"category_checklink\" />
              <input type=\"submit\" name=\"sub_category\" value=\"$l_category_checkdelete\" onclick=\"if (check_category_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_admin_category_update\" method=\"get\"
              action=\"" . url_prepare("agenda_index.php") . "\">
            <input type=\"hidden\" name=\"sel_category\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"category_update\" />
            <input type=\"text\" name=\"tf_category_upd\" />
            <input type=\"submit\" name=\"sub_category\" value=\"$l_category_update\"
              onclick=\"if (check_category_upd(this.form,document.form_admin_category_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category Section -->

        <form name=\"form_admin_category_new\" method=\"get\"
          action=\"" . url_prepare("agenda_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_category_new :
            <input name=\"tf_category_new\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"category_insert\" />
            <input type=\"submit\" name=\"sub_category\" value=\"$l_category_insert\"
              onclick=\"if (check_category_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>
";
return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Return the category delete display form (button)
// Parameters:
//   - $label : category label
// Retuns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function dis_category_delete_form($agenda) {
  global $l_category_delete;

  $id = $agenda["category_id"];
  $r = "<form name=\"form_category_delete\"
          method=post action=\"" . url_prepare("agenda_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"category_delete\">
        <input type=\"hidden\" name=\"sel_category\" value=\"$id\">
        <input type=\"submit\" name=\"sub_category\" value=\"$l_category_delete\">
        </form>";

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the category links
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_category_links($agenda) {
  global $l_category_link_agenda, $l_category_link_agenda_no;
  global $l_category_can_delete, $l_category_cant_delete;
  global $l_back,$l_link_events,$display;

  $label = get_category_label($agenda["category_id"]);
  $obm_q = run_query_category_links($agenda["category_id"]);
  $del_form = dis_category_delete_form($agenda["category_id"]);
  
  $obm_q->next_record();
  if ($obm_q->f("numlink") > 0) {
    $display["msg"] = display_warn_msg($l_category_cant_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : ".$obm_q->f("numlink")." $l_link_events</td>
    </tr>
    </table>
    <div class=\"detailButton\">
        <p class=\"detailButtons\">
        <form name=\"form_back\" method=\"post\"
          action=\"" .url_prepare("agenda_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
	
	</p>
    </div>
";
  } else {
    $display["msg"] = display_ok_msg($l_category_can_delete);
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        $l_category_link_agenda_no '$label' 
      </td>
    </tr>
    </table>
    <div class=\"detailButton\">
        <p class=\"detailButtons\">
      $del_form
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("agenda_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
	</p>
    </div>
";

  }
  return "$dis_link";
}


///////////////////////////////////////////////////////////////////////////////
// Perform the export to the vCalendar format
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_export_handle($agenda) {
  global $obm_version,$cagenda_weekstart,$auth;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
 
  $start_time = strtotime("-1 year", $ts_date);
  $end_time = strtotime("+1 year", $ts_date);

  $calendar_user = array ($auth->auth["uid"] => "dummy");  
  $obm_event = run_query_no_repeat_events($start_time,$end_time,$calendar_user);

  header("Content-Type: text/x-vCalendar");
  header("Content-Disposition: inline; filename=ObmCalendar.ics");
  echo "
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//OBM/OBM V$obm_version Calendar//EN
METHOD:PUBLISH";
  while($obm_event->next_record()) {
    $id = $obm_event->f("calendarevent_id");
    $title = "SUMMARY:" . $obm_event->f("calendarevent_title");
    $title = wordwrap($title,74,"\n ",1);
    $location = $obm_event->f("calendarevent_location");
    $priority = $obm_event->f("calendarevent_priority");
    $category = strtoupper($obm_event->f("calendarcategory_label")); 
    $date_ts = $obm_event->f("calendarevent_timeupdate");
    $date_ts = gmdate("Ymd\THis\Z",$date_ts);
    if ($obm_event->f("calendarevent_description") != "") {
      $description = preg_replace("/\\r?\\n/","\\n",$obm_event->f("calendarevent_description"));
      $description = "DESCRIPTION:" . $description;
      $description = "\n" . wordwrap($description,74,"\n ",1);
    } else {
      $description = "";
    }
    $date_b = $obm_event->f("calendarevent_date");
    $date_b = gmdate("Ymd\THis\Z",$date_b);
    $date_e = $obm_event->f("calendarevent_date") + $obm_event->f("calendarevent_duration") ;
    $date_e = gmdate("Ymd\THis\Z",$date_e);
    switch($priority) {
      case 1 : $priority = 3;
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $obm_event->f("calendarevent_privacy");
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
  
  $obm_event = run_query_repeat_events($start_time,$end_time,$calendar_user);
  
  while($obm_event->next_record()) {
    $id = $obm_event->f("calendarevent_id");
    $title = $obm_event->f("calendarevent_title");
    $location = $obm_event->f("calendarevent_location");
    $priority = $obm_event->f("calendarevent_priority"); 
    $category = strtoupper($obm_event->f("calendarcategory_label")); 
    if ($obm_event->f("calendarevent_description") != "") {
      $description = "\nDESCRIPTION:".$obm_event->f("calendarevent_description");
    } else {
      $description = "";
    }
    $date_b = $obm_event->f("calendarevent_date");
    $date_b = date("Ymd",$date_b)."T".date("His",$date_b);
    $start_date = $date_b;
    $date_e = $obm_event->f("calendarevent_date") + $obm_event->f("calendarevent_duration") ;
    $date_e = date("Ymd",$date_e)."T".date("His",$date_e);
    $kind = $obm_event->f("calendarevent_repeatkind");
    $end = $obm_event->f("calendarevent_endrepeat");
    $end = date("Ymd",$end)."T".date("His",$end);
    $repeat_days = $obm_event->f("calendarevent_repeatdays");
    $repeat_frequence = $obm_event->f("calendarevent_repeatfrequence");
    switch($priority) {
      case 1 : $priority = 3;
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $obm_event->f("calendarevent_privacy");
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


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
// Parameters:
//    - $agenda         : agenda parameters
//    - $event_qs   : list of event's records 
//    - $contacts_array : array of selected contacts 
//    - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_year_planning($agenda,$calendar_user) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer ,$ico_event,$auth;
  global $cagenda_weekstart, $set_cal_interval,$ico_new_event;
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
   
  $display_date = localizeDate("month", $ts_date);  
  
  $td_heigth = 8;
  $td_width = 8;
  
  $events = events_model($start_time,$end_time,$calendar_user);
  
  for ($i=0; $i<7; $i++) {
    $day_num = date("w","$start_day");
    $day = $l_daysofweekshort[$day_num];
    $dis_day .= "<td class=\"agendaHead2\" style=\"width: 30px;\">$day</td>";
    $start_day = strtotime("+1 day", $start_day); 
  }
  
  $m=0;	
  $n=0;

  $ts_minical = $start_time;

  do {
    $minical_month = get_month($ts_minical);
    $minical_year = get_year($ts_minical);
    $ts_month = strtotime("$minical_year-$minical_month-01");
    $iso_month = isodate_format($ts_month);
    $start_day = get_date_day_of_week($ts_month, $cagenda_weekstart);
    
    $i = 0;
    $whole_month = TRUE;
    $num_week = 0;
    $dis_calendar .= "
    <td style=\"width:210px\">
      <table class=\"agendaCal\">
      <tr>
        <td colspan=\"7\" class=\"agendaHead2\">
          <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=$iso_month")."\">
      " . localizeDate ("month", $ts_minical) . "
          </a>
        </td>
      </tr>
      <tr>
        $dis_day
      </tr>
      <tr>
        <td colspan=\"7\">
        <table class=\"agendaYear\">\n";
    do {
      $day = date ("j", $start_day);
      $iso_day = isodate_format($start_day);
      $check_month = get_month($start_day);
      $dis_event_cal = "";
      if (isset($events[$iso_day])) {
	$dayObj = $events[$iso_day];
	$dis_event_cal ="<table class=\"agendaEventYear\"><tr>";
	$tr_users = 0;
	foreach($calendar_user as $id => $data ) {
	  $events_data = $dayObj->get_events($id);
	  $days_events = $dayObj->day_events;
	  if ($tr_users == 3) {
	    $dis_event_cal .= "</tr><tr>";   
	  }
	  if (is_array($events_data) || is_array($days_events[$id])) {
	    $dis_event_cal .= "<td class=\"agendaEventYear\">";
	    $dis_event_cal .= "<img src=\"".C_IMAGE_PATH."/$set_theme/".$data["image"]."\" alt=\"\"/></td>";
	  } else {
	    $dis_event_cal .= "<td class=\"agendaEventYear\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_calendar_nouser\" alt=\"\"/></td>";		
	  }
	  $tr_users++;	   
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
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=$iso_day'\">
	  <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_day")."\">$day</a>$dis_event_cal
          </td>";
      }	
      elseif ($check_month == $minical_month) {	   
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"agendaMonthReg\" 
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#ffffff'\" 
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=$iso_day'\">
          <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_day")."\">$day</a>$dis_event_cal
          </td>";
      } else {
	$dis_calendar .= "
          <td style=\"height:30px;width: 30px;\" class=\"agendaMonthOff\"
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#f2f2f2'\"
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=$iso_day'\">
	  <a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_day")."\">$day</a>$dis_event_cal
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
      <a href=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=".$prev_year)."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"Previous Month\" />
      </a>
    </td>
    <td class=\"agendaHead\" colspan=\"3\">
      $this_year
    </td>
    <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=".$next_year)."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"Next Month\" />
       </a>
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
//   - $contacts_array : array of selected contacts 
//   - $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_month_planning($agenda,$calendar_user) { 
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer ,$ico_event,$auth;
  global $cagenda_weekstart, $set_cal_interval,$ico_new_event;
  global $cagenda_first_hour, $cagenda_last_hour;
  global $l_private,$l_daysofweek;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);  
  $this_month = get_month($ts_date);
  $this_year = get_year($ts_date);
  $start_time = get_date_day_of_week(strtotime("$this_year-$this_month-01"), $cagenda_weekstart);
  $end_time = strtotime("+1 month +6 days", $start_time);
  $next_month = isodate_format(strtotime("+1 month",$ts_date));
  $prev_month = isodate_format(strtotime("-1 month",$ts_date));

  $display_date = localizeDate("month", $ts_date);  
  $start_week_day = strtotime($cagenda_weekstart);
  $whole_month = TRUE;
  $nb_user = count($calendar_user);
  $td_width = floor(100/$nb_user)."px";
   
  $events = events_model($start_time,$end_time,$calendar_user);

  // Display the day of a week
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweek[$day_num];
    
    $dis_day_head .= "<td style=\"width:105px;\" class=\"agendaHead2\">$day</td>";
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
      $week_num = get_week_num($current_time); 
      $dis_month_cal .= "
      <tr>
        <td class=\"agendaHead\">
        <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$iso_day")."\" >$week_num</a>
        </td>";
    }
    $daylink_begin = urlencode(isodate_format(strtotime("+$cagenda_first_hour hours",$current_time),"",1));
    $daylink_end = urlencode(isodate_format(strtotime("+$cagenda_last_hour hours",$current_time),"",1));

    $dis_month_cal .= "
      <td $bgclass style=\"width:105px;height:105px\">
      <a style=\"float:right;\" class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_day")."\">$this_day</a>
      <a href=\"".url_prepare("agenda_index.php?action=new&amp;tf_date_begin=$daylink_begin&amp;tf_date_end=$daylink_end")."\" />
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_new_event\" alt=\"[New]\" />
      </a>";
    if (isset($events[$iso_day])) {      
      $dis_month_cal .= "<table style=\"heigth:100%;width:100%;\"><tr>";
      $day = $events[$iso_day];
      $user_indice = 0;
      $days_events = $day->day_events;
      foreach ($calendar_user as $id => $properties) {
	$dis_month_cal .= "<td><table style=\"heigth:100%;width:$td_width;\">";
        $list_events = $days_events[$id];
	if ($list_events != null) {
	  $have_day_events = true;
	  $dis_month_cal .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	  foreach ($list_events as $event) {
	    $dis_month_cal .= allday_event_display(&$hidden_div,$event,$nb_user,$properties);
	  }
          $dis_month_cal .= "</td>";
        }
	$events_data = $day->get_events($id);
	if (is_array($events_data)) {
	  $list_events = $events_data["events"];
	  foreach ($events_data as $event) {
	    $dis_month_cal .= "<tr><td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	    month_event_display(&$dis_month_cal,&$hidden_div,$event,$nb_user,$properties);	   
	    $dis_month_cal .= "</td></tr>";
	  }
	} else {
	  $dis_month_cal .= "<tr><td width=\"$td_width\">&nbsp;</td></tr>";
	}
	$dis_month_cal .= "</table></td>";	
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
      <a href=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=".$prev_month)."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Month]\" />
      </a>
     </td>
     <td colspan=\"5\" class=\"agendaHead\">
      $display_date
     </td>
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=".$next_month)."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Month]\" />
      </a>
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
// Make the event display 
// Parameters:
//    - $agenda         : agenda parameters
//    - $contacts_array : array of selected contacts 
//    - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function month_event_display(&$week_cal,&$hidden_div,$event,$nb_user,$properties) {
  global $l_duration, $path, $l_private, $l_private_description, $set_theme,$ico_event;
  global $auth,$l_begin,$l_end;

  $id = $event["event"]->id;
  $usr_id = $properties["id"];
  if ($event["event"]->privacy == 1 && $auth->auth["uid"] != $properties["id"]) {
    $title = $l_private;
    $description = $l_private_description;
    $link = "javascript:return false;";
    $category = $l_private;    
  } else {
    $title = $event["event"]->title;
    $link = "$path/agenda/agenda_index.php?action=detailconsult&param_event=$id";
    $description = $event["event"]->description;
    $category = $event["event"]->category;
  }  
  if ($nb_user == 1) {	      
    $event_head_display = date("H:i",$event["date"])."->".date("H:i",$event["date"] + $event["event"]->duration);
  } else {
    $event_head_display = "<img class=\"agendaEvent\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_event\" alt=\"[D]\" />";
  }  
  $week_cal .= "<a class=\"agendaLink\"
    onMouseOver=\"show(event,'$id-$usr_id'); return true;\"
    onMouseOut=\"hide('$id-$usr_id'); return true;\"
    href=\"$link\">$event_head_display</a><br />";
  $hidden_div .= "
  <div id=\"$id-$usr_id\" class=\"last\" style=\"display:none;position:absolute;z-index:1000;\">
   <div class=\"lastTitle\">:: $title</div>
   <ul class=\"lastList\">
    <li class=\"lastList\">$category</li>
    <li class=\"lastList\">$l_begin : ".date("Y-m-d H:i:s",$event["date"])." </li>
    <li class=\"lastList\">$l_end : ".date("Y-m-d H:i:s",$event["date"] + $event["event"]->duration)."</li>
    <li class=\"lastList\">$description</li>
   </ul>
  </div>";
}

///////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
// Parameters:
//    - $agenda          : agenda parameters
//    - $calendar_entity : array of selected contacts 
///////////////////////////////////////////////////////////////////////////////
function dis_week_planning($agenda, $calendar_user) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer ,$ico_event,$auth;
  global $cagenda_weekstart, $set_cal_interval,$ico_new_event;
  global $cagenda_first_hour, $cagenda_last_hour;
  global $l_private,$l_all_day, $ico_agenda,$path;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);
  $time_unit = 60 / $set_cal_interval; 
  $time_unit_sec = 3600 / $set_cal_interval;; 
  $week_num = get_week_num($ts_date); 
  $next_week = isodate_format(strtotime("+1 week", $ts_date));
  $prev_week = isodate_format(strtotime("-1 week", $ts_date));
  
  $start_week_day = get_date_day_of_week($ts_date, $cagenda_weekstart);
  $end_week_time = $start_week_day + ((6 * 24 + $cagenda_last_hour) * 3600);
  $start_week_time = strtotime("+ $cagenda_first_hour hours",$start_week_day);
  $dis_day_second = ($cagenda_last_hour - $cagenda_first_hour) * 3600;
  
  $start_week = localizeDate("week", $start_week_time);
  $end_week = localizeDate("week", $end_week_time);
  
  $display_date = "$start_week - $end_week"; 
  $events = events_model($start_week_day,$end_week_time,$calendar_user);

  $nb_user = count($calendar_user);
  $td_width = floor(70/$nb_user)."px"; 
 
  $day_duration = 86400; 
  /* Week Days headers */
  for ($ts = $start_week_time; $ts < $end_week_time; $ts += $day_duration ) {
    $iso_date = isodate_format($ts);
    $this_date_l = localizeDate("week_list", $ts);
    $daylink_begin = urlencode(isodate_format($ts,"",1));
    $daylink_end = urlencode(isodate_format(strtotime("+$cagenda_last_hour hours",$ts),"",1));
    $daylink_end = urlencode(isodate_format($ts + $dis_day_second,"",1));
    $week_day_list .= "
     <td class=\"agendaCell\" colspan=\"$nb_user\" style=\"width:72px\">
      <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$iso_date")."\">$this_date_l</a>
      <br />
      <a href=\"agenda_index.php?action=new&amp;tf_date_begin=$daylink_begin&amp;tf_date_end=$daylink_end\" > 
       <img align=\"middle\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_new_event\" alt=\"[New]\" />
      </a>	
     </td>";
    $day = $events[$iso_date];
    if (is_object($day) && is_array($day->day_events)) {
      $days_events = $day->day_events;
      foreach($calendar_user as $id => $properties) {
	$user_indice ++;
	$list_events = $days_events[$id];
	if ($list_events != null)  {
	  $have_day_events = true;
	  $days_cal .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	  foreach($list_events as $event) {
	    $days_cal .= allday_event_display(&$hidden_div,$event,$nb_user,$properties);	
	  }
	  $days_cal .= "</td>";
	} else {
	  $days_cal .= "<td style=\"width:$td_width;\" class=\"agendaHead\">&nbsp;</td>";
	}
      }
    } else {
      $days_cal .= "<td colspan=\"$nb_user\" style=\"width:$td_width;\" class=\"agendaHead\">&nbsp;</td>";
    }
  }

  if ($have_day_events) {
    $week_cal .= "
    <tr>
      <td class=\"agendaHour\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_agenda\" alt=\"$l_all_day\" /></td>
      $days_cal
    </tr>";
  }

  /* Events of the week */
  for ($j = $cagenda_first_hour; $j < $cagenda_last_hour; $j++) {
    $delta = $j - $cagenda_first_hour;
    for ($k=0; $k<$set_cal_interval; $k++) {
      if ($k==0) {
        $week_cal .= "\n<tr><td rowspan=\"$set_cal_interval\" class=\"agendaHour\"  style=\"height:".($set_cal_interval*18)."px;\" >".($j).":00</td>";
      } else {
        $week_cal .= "\n<tr>";
      }
      $delta2 = $k * $time_unit;
      for($i = 0; $i <7; $i ++) {
        $current_time = strtotime("+$i days +$delta hours +$delta2 minutes",$start_week_time);
        $goto = "$path/agenda/agenda_index.php?action=new&amp;tf_date_begin=".isodate_format($current_time,"",1)."&amp;tf_date_end=".isodate_format($current_time + $time_unit_sec,"",1);
        $iso_date = isodate_format($current_time);
        if (isset($events[$iso_date])) {
          $day = $events[$iso_date];
          $user_indice = 0;
          foreach($calendar_user as $id => $properties) {
            $user_indice ++;
            if ($j == $cagenda_first_hour && $delta2 == 0) {
              $events_data = $day->get_events_before($current_time + $time_unit_sec, $id, $current_time);
            } else {
              $events_data = $day->get_events_between($current_time, $current_time + $time_unit_sec, $id);
            }
            if ($events_data != null) {
              if (is_array($events_data)) {
		$duration = $events_data["duration"];
		$list_events = $events_data["events"];
		$week_cal .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;text-align:left;\" rowspan=\"".ceil($duration/($time_unit_sec))."\">";
		foreach($list_events as $event) {
		  week_event_display(&$week_cal,&$hidden_div,$event,$nb_user,$properties);	
		}
		$week_cal .= "</td>";
	      }
	    } else {
	      if( $user_indice == 1) {
		$class = " border";
	      } else {
		$class = "";
	      }
              $week_cal .= "<td width=\"$td_width\" class=\"agendaNoEvent$class\" onclick=\"window.location='$goto'\"
                onmouseover=\"this.className='agendaNoEventHover$class';this.style.cursor = 'pointer'\"
                onmouseout=\"this.className ='agendaNoEvent$class'\">&nbsp;</td>";
	    }
	  }
	} else {
          $week_cal .= "<td class=\"agendaNoEvent border\"  onclick=\"window.location='$goto'\"
             onmouseover=\"this.className='agendaNoEventHover border';this.style.cursor = 'pointer'\"
             onmouseout=\"this.className ='agendaNoEvent border'\" colspan=\"$nb_user\">&nbsp;</td>";
        }
      }
      $week_cal .= "</tr>";
    }
  }
  $col_span = (7 * $nb_user) - 1;
  $block = "
  <div class=\"agendaMain\">
   <table class=\"agendaCalWeek\">
    <tr>
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$prev_week")."\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" />
      </a>     
     </td>
     <td colspan=\"$col_span\" class=\"agendaHead\">
      $display_date
     </td>  
     <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$next_week")."\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Next Week]\" />
      </a>     
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
// Make the event display 
// Parameters:
//    - $agenda         : agenda parameters
//    - $contacts_array : array of selected contacts 
//    - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function week_event_display(&$week_cal,&$hidden_div,$event,$nb_user,$properties) {
  global $l_duration, $path, $l_private, $l_private_description, $set_theme,$ico_event;
  global $auth,$l_begin,$l_end;

  $id = $event["event"]->id;
  $usr_id = $properties["id"];
  if($event["event"]->privacy == 1 && $auth->auth["uid"] != $properties["id"]) {
    $title = $l_private;
    $description = $l_private_description;
    $link = "javascript:return false;";
    $category = $l_private;    
  }else {
    $title = $event["event"]->title;
    $link = "$path/agenda/agenda_index.php?action=detailconsult&param_event=$id";
    $description = $event["event"]->description;
    $category = $event["event"]->category;
  }  
  if ($nb_user == 1) {	      
    $event_head_display = "&#8226; $title";
  }else {
    $event_head_display = "<img class=\"agendaEvent\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_event\" alt=\"[D]\" />";
  } 
  $week_cal .=  "<a class=\"agendaLink\" 
  onMouseOver=\"show(event,'$id-$usr_id'); return true;\"
  onMouseOut=\"hide('$id-$usr_id'); return true;\"		
  href=\"$link\">$event_head_display</a><br />";
  $hidden_div .= "
  <div id=\"$id-$usr_id\" class=\"last\" style=\"display:none;position:absolute;z-index:1000;\"> 
   <div class=\"lastTitle\">:: $title</div>
   <ul class=\"lastList\">  
    <li class=\"lastList\">$category</li>
    <li class=\"lastList\">$l_begin : ".date("Y-m-d H:i:s",$event["date"])." </li>
    <li class=\"lastList\">$l_end : ".date("Y-m-d H:i:s",$event["date"] + $event["event"]->duration)."</li>        
    <li class=\"lastList\">$description</li>
   </ul>
  </div>  ";	  
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the day
// Parameters:
//   - $agenda         : agenda parameters
//   - $contacts_array : array of selected contacts 
//   - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_day_planning($agenda,$calendar_user) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer,$ico_new_event;
  global $cagenda_first_hour, $cagenda_last_hour,$set_cal_interval;
  global $l_private,$auth,$l_all_day,$ico_agenda;

  $iso_date = $agenda["date"];
  $ts_date = strtotime($iso_date);

  $tomorrows_date = isodate_format(strtotime("+1 day", $ts_date));
  $yesterdays_date = isodate_format(strtotime("-1 day", $ts_date));

  $time_unit = 60 / $set_cal_interval; 
  $time_unit_sec = 3600 / $set_cal_interval;; 
  $week_num = get_week_num($ts_date); 
 
  $start_time = strtotime("+$cagenda_first_hour hours",$ts_date);
  $end_time = strtotime("+$cagenda_last_hour hours",$ts_date);
  
  $display_date = localizeDate("day", $ts_date);
   
  $events = events_model($start_time,$end_time,$calendar_user);
  $nb_user = count($calendar_user);
  
  $day = $events[$iso_date];
  
  $td_width = floor(460/$nb_user)."px";
  $days_events = $day->day_events;
  
  if (is_array($days_events)) {
    $hour_day_list .= "<tr><td class=\"agendaHour\" ><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_agenda\" alt=\"$l_all_day\" /></td>";
    foreach($calendar_user as $id => $properties) {
      $user_indice ++;
      $list_events = $days_events[$id];
      if ($list_events != null) {
	$hour_day_list .= "<td class=\"".$properties["class"]."\" style=\"width:$td_width;\">";
	foreach($list_events as $event) {
	  $hour_day_list .= allday_event_display(&$hidden_div,$event,$nb_user,$properties);	
	}
	$hour_day_list .= "</td>";
      } else {
	$hour_day_list .= "<td style=\"width:$td_width;\" class=\"agendaHead\">&nbsp;</td>";
      }
    }
    $hour_day_list .= "</tr>";
  }

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
	$user_indice = 0;
	foreach ($calendar_user as $id => $properties) {
	  $user_indice ++;
	  if ($i == $cagenda_first_hour && $delta2 == 0) {
	    $events_data = $day->get_events_before($current_time + $time_unit_sec, $id, $current_time);
	  } else {
	    $events_data = $day->get_events_between($current_time, $current_time + $time_unit_sec, $id);	  
	  }
	  if ($events_data != null) {
	    if (is_array($events_data)) {
	      $duration = $events_data["duration"];
	      $list_events = $events_data["events"];
	      $hour_day_list .= "<td  class=\"".$properties["class"]."\" style=\"width:$td_width;\" rowspan=\"".ceil($duration/($time_unit_sec))."\" >";
	      foreach ($list_events as $event) {
		day_event_display(&$hour_day_list,&$hidden_div,$event,$nb_user,$properties);	
	      }
	      $hour_day_list .= "</td>";
	    }
	  } else {
	    $hour_day_list .= "<td style=\"width:$td_width;\" class=\"agendaNoEvent\">&nbsp;</td>";
	  }
        }
      } else {
	$hour_day_list .= "<td class=\"agendaNoEvent\">&nbsp;</td>";
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
    <td colspan=\"".($nb_user +1)."\" class=\"agendaHead\">
      <a style=\"float:left;\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$yesterdays_date")."\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_left_day\" alt=\"[Yesterday]\" />
      </a>     
      <a style=\"float:right;\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$tomorrows_date")."\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_right_day\" alt=\"[Tomorrow]\" />
      </a>      
      $display_date
    </td>
  </tr>
  <tr>
    <td class=\"agendaHead\">
      <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$iso_date")."\" class=\"agendaLink\">
      $week_num
      </a>
    </td>
    <td style=\"width:460px;\" colspan=\"$nb_user\" class=\"agendaHead\">
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
// Make the event display 
// Parameters:
//    - $agenda         : agenda parameters
//    - $event_qs   : list of event's records 
//    - $contacts_array : array of selected contacts 
//    - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function day_event_display(&$week_cal,&$hidden_div,$event,$nb_user,$properties) {
  global $l_duration, $path, $l_private, $set_theme,$ico_event;
  global $auth,$l_begin,$l_end;
  $id = $event["event"]->id;
  $usr_id = $properties["id"];
  if($event["event"]->privacy == 1 && $auth->auth["uid"] != $properties["id"]) {
    $title = $l_private;
    $description = $l_private_description;
    $link = "javascript:return false;";
    $category = $l_private;    
  }else {
    $title = $event["event"]->title;
    $link = "$path/agenda/agenda_index.php?action=detailconsult&param_event=$id";
    $description = $event["event"]->description;
    $category = $event["event"]->category;
  }    
  if($nb_user == 1) {	      
    $event_head_display = "&#8226;
    ".date("H:i:s",$event["date"])." -> ".date("H:i:s",$event["date"] + $event["event"]->duration)."
    ".$title;
  }else {  
    $event_head_display = "&#8226;$title";
  }  
  $week_cal .=  "<a class=\"agendaLink\" 
  onMouseOver=\"show(event,'$id-$usr_id'); return true;\"
  onMouseOut=\"hide('$id-$usr_id'); return true;\"		
  href=\"$link\">$event_head_display</a><br />";
  $hidden_div .= "
  <div id=\"$id-$usr_id\" class=\"last\" style=\"display:none;\"> 
   <div class=\"lastTitle\">:: $title</div>
   <ul class=\"lastList\">  
    <li class=\"lastList\">$category</li>
    <li class=\"lastList\">$l_begin : ".date("Y-m-d H:i:s",$event["date"])." </li>
    <li class=\"lastList\">$l_end : ".date("Y-m-d H:i:s",$event["date"] + $event["event"]->duration)."</li>    
    <li class=\"lastList\">$description</li    >
   </ul>
  </div>  ";	   
}
///////////////////////////////////////////////////////////////////////////////
// Make the event display 
// Parameters:
//    - $agenda         : agenda parameters
//    - $event_q   : list of event's records 
//    - $contacts_array : array of selected contacts 
//    - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function allday_event_display(&$hidden_div,$event,$nb_user,$properties) {
  global $path, $l_private, $set_theme,$ico_calendar_day;
  
  global $auth,$l_begin,$l_end;
  $id = $event->id;
  $usr_id = $properties["id"];
  if($event->privacy == 1 && $auth->auth["uid"] != $properties["id"]) {
    $title = $l_private;
    $description = $l_private_description;
    $link = "javascript:return false;";
    $category = $l_private;    
  }else {
    $title = $event->title;
    $link = "$path/agenda/agenda_index.php?action=detailconsult&param_event=$id";
    $description = $event->description;
    $category = $event->category;
  }    
  $event_head_display = "<img class=\"agendaEvent\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_calendar_day\" alt=\"[D]\" />";
  $dis_calendar .=  "<a class=\"agendaLink\" 
  onMouseOver=\"show(event,'$id-$usr_id'); return true;\"
  onMouseOut=\"hide('$id-$usr_id'); return true;\"		
  href=\"$link\">$event_head_display</a>";
  $hidden_div .= "
  <div id=\"$id-$usr_id\" class=\"last\" style=\"display:none;position:absolute;z-index:1000;\"> 
   <div class=\"lastTitle\">:: $title</div>
   <ul class=\"lastList\">  
    <li class=\"lastList\">$category</li>
    <li class=\"lastList\">$description</li>
   </ul>
  </div>  ";	   

  return $dis_calendar;
}
