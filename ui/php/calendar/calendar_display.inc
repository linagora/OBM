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
function dis_calendar_calendar_view($calendar, &$sel_entity_id, $p_view, $p_range) {
  global $display, $path, $ccalendar_max_users, $obm;
  global $l_module_calendar;

  $group_view = $calendar['entity']['group_view'];
  $entity_readable = get_calendar_entity_readable();
  $visible_entities_id = slice_calendar_entities($sel_entity_id, $ccalendar_max_users, $entity_readable);
  $sel_entity_id = $visible_entities_id;
  // we restore the selected group for session storage
  $sel_entity_id['group_view'] = $group_view;

  $calendar_entity = store_calendar_entities(get_calendar_entity_label($sel_entity_id));
  $writable_entity = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
  );
  $my_views = run_query_calendar_get_my_views();
  $calendar_function = 'dis_calendar_'.$p_range.'_'.$p_view;
  
  $display['features'] = html_calendar_planning_bar($calendar, $calendar_entity, $entity_readable, $my_views, $p_view);
  $display['title'] = "<div class=\"title\">$l_module_calendar : ".$GLOBALS["l_view_$p_view"].' '.$GLOBALS["l_range_$p_range"].' </div>';
  if (function_exists($calendar_function)) {
    $block = $calendar_function($calendar, $calendar_entity,$writable_entity,$date);
    $block .= dis_calendar_quick_form($calendar,$calendar_entity);
    $block .= dis_calendar_conflict_popup();
    $calendar_function_pdf = $calendar_function."_pdf";
    if (function_exists($calendar_function_pdf)) {
      $display['print_url'] = "$path/calendar/calendar_index.php?action=pdf_export_form&amp;date=".$calendar['date']->getURL()."&amp;output_target=print";
    }
  } else {
    $block .= dis_calendar_month_planning($calendar, $calendar_entity,$writable_entity);
  }
  return $block;
}


/**
 * dis_calendar_view_bar 
 * 
 * @param mixed $p_kind 
 * @access public
 * @return void
 */
function dis_calendar_view_bar($p_kind, $p_range, $date, $action) {
  global $ico_week_view, $ico_day_view, $ico_month_view, $ico_list_view;
  global $ico_week_view_current, $ico_day_view_current, $ico_month_view_current;
  global $ico_agenda_view,  $ico_planning_view, $ico_list_view_current;
  global $ico_agenda_view_current,  $ico_planning_view_current;
  global $l_cal_range, $l_cal_view;
  global $l_view_agenda_desc,  $l_view_planning_desc, $l_view_list_desc;
  global $l_view_week_desc, $l_view_day_desc, $l_view_month_desc;
  global $path;

  if ($p_kind) {
    ${'ico_'.$p_kind.'_view'} = ${'ico_'.$p_kind.'_view_current'};
  }
  if ($p_range) {
    ${'ico_'.$p_range.'_view'} = ${'ico_'.$p_range.'_view_current'};
  }

  $block = "
  <div style=\"margin-top:-10px;\">
  <div class=\"SB LF NM\" style=\"margin-left:10px;\">
  <a href=\"$path/calendar/calendar_index.php?cal_view=agenda&amp;date=".$date->getURL()."\">
  <img 
  title=\"$l_view_agenda_desc\" 
  src=\"$ico_agenda_view\" alt=\"$l_day\"/> 
  </a>
  <a href=\"$path/calendar/calendar_index.php?cal_view=planning&amp;date=".$date->getURL()."\">
  <img
  title=\"$l_view_planning_desc\" 
  src=\"$ico_planning_view\" alt=\"$l_month\" />
  </a>
  <a href=\"$path/calendar/calendar_index.php?cal_view=list&amp;date=".$date->getURL()."\">
  <img
  title=\"$l_view_list_desc\" 
   src=\"$ico_list_view\" alt=\"$l_list\" />
  </a>  
  </div>
  <div id=\"calendarCalRange\" class=\"SB LF NM\" style=\"margin-left:10px;\">
  <a href=\"$path/calendar/calendar_index.php?cal_range=day&amp;date=".$date->getURL()."\">
  <img
  title=\"$l_view_day_desc\" 
  src=\"$ico_day_view\" alt=\"$l_day\" />
  </a>
  <a href=\"$path/calendar/calendar_index.php?cal_range=week&amp;date=".$date->getURL()."\">
  <img
  title=\"$l_view_week_desc\" 
  src=\"$ico_week_view\" alt=\"$l_week\" />
  </a>
  <a href=\"$path/calendar/calendar_index.php?cal_range=month&amp;date=".$date->getURL()."\">
  <img
  title=\"$l_view_month_desc\" 
  src=\"$ico_month_view\" alt=\"$l_month\" />
  </a>
  </div>
  </div>
  <script type=\"text/javascript\">
    if(!obm.vars.conf.displayRange) {
      $('calendarCalRange').setStyle('display','none');
     }
  </script>
  <p class=\"LC\" />";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display the navigation bar
// Parameters:
//   - $calendar: calendar parameters
//   - $calendar_entity : array of entities elements to display
//   - $entity_readable : array of readable entities (['user'], ['resource']
///////////////////////////////////////////////////////////////////////////////
function html_calendar_planning_bar($calendar, $calendar_entity, $entity_readable, $my_views, $p_view) {
  global $action;
  global $output_target;

  $output_target = $calendar['output_target'];

  $block = html_calendar_bar_legend($p_view, $calendar_entity,$calendar);
  if ($action != 'perform_meeting' && $output_target != 'print') {
    $block .= html_calendar_select_calendar_entity($p_view, $calendar, $calendar_entity, $entity_readable);
    $block .= html_calendar_view_selector($calendar, $my_views);
    $block .= html_calendar_bar_date_selector($calendar);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the navigation bar
// Parameters:
//   - $calendar_entity : array of entities elements to display
///////////////////////////////////////////////////////////////////////////////
function html_calendar_bar_legend($p_view, $calendar_entity,$calendar) {
  global $ico_user, $ico_resource, $obm;
  global $l_meeting_legend, $l_possible, $l_free, $l_occupied;
  global $l_calendar_displayed, $l_resource_displayed,$c_all,$l_event_color_tt;
  global $action,$ico_delete,$output_target,$l_color,$l_users,$l_events;
  global $mono_user;

  $iso = $calendar['date']->getURL();
  if (count($calendar_entity) == 1 && is_array($calendar_entity['user'])
     && count($calendar_entity['user']) == 1) {
    $colorTemplate = 'event';
    $tooltip = $l_event_color_tt;
    $mono_user = true;
  } else {
    $colorTemplate = 'user';
    $mono_user = false;
  }
  ${'select_color_'.$colorTemplate} = "selected=\"selected\"";
  if ($action == 'perform_meeting' || $action == 'perform_res_meeting') {
    $l_legend = $l_meeting_legend;
    $dis_legend = "
    <li class=\"freeBusy\">$l_possible</li>
    <li class=\"free\">$l_free</li>
    <li class=\"busy\">$l_occupied</li>";
  } elseif ( $p_view == 'planning' ) {
    $script_range = 'obm.vars.conf.displayRange = true;';
    $l_legend = $l_meeting_legend;
    $cats1 = of_category_get_ordered('event', 'category1');
    $nb = count($cats1);
    $colors = random_color($nb);
    foreach($cats1 as $key => $cat_info) {
      $dis_legend .= "<li style=\"color:#fff;background-color:rgb($colors[$key])\">$cat_info[label]</li>";
    }
  } else {
    $script_range = 'obm.vars.conf.displayRange = true;';
    $l_legend = $l_calendar_displayed;
    foreach ($calendar_entity as $type => $entity) {
      foreach ($entity as $id => $data) {      
        $data_id = "data-$type-$id";        
        $dis_legend .= "
          <li title=\"$tooltip\" class=\"".$data['class']."\"><img src=\"".${"ico_$type"}."\" alt=\"[$type]\" />
          <input value=\"$data_id\" id=\"$data_id\" name=\"sel_ent[]\" type=\"hidden\" />";
        if ($output_target != 'print') {
          $dis_legend .= "<img src=\"$ico_delete\"
          onclick=\"\$('$data_id').dispose(); $('calendarSelectForm').submit();\" onmouseover=\"this.style.cursor = 'pointer';\"/>";
        }
        $dis_legend .= " &nbsp;".$data['name']."
          </li>";
      }
    }
  }

  $block .= "
  <div class=\"portlet\" id=\"calendarLegend\">
  <h1>$l_legend</h1>
  <form action=\"\" id=\"calendarSelectForm\" onsubmit=\"return false;\">
  <script type=\"text/javascript\">
    obm.vars.conf.calendarColor = '$colorTemplate';
    $script_range 
   </script>      
  <ul>
    $dis_legend
  </ul>
  <input type=\"hidden\" name=\"date\" value=\"".$calendar['date']->getIso()."\" />
  <input type=\"hidden\" name=\"group_view\" value=\"$c_all\" />
  <input type=\"hidden\" name=\"new_group\" value=\"1\" />
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
//   - $entity_readable : array of readable entities (['user'], ['resource'])
///////////////////////////////////////////////////////////////////////////////
function html_calendar_select_calendar_entity($new_action, $calendar, $calendar_entity, $entity_readable) {
  global $path, $obm, $c_all, $ccalendar_resource;
  global $ico_add, $popup_width, $popup_height, $l_today, $ico_add;
  global $l_selection, $l_users,$l_resources, $l_validate,$l_category_filter;
  global $l_search_users, $l_search_resources,$l_private_group_members, $l_public_group_members;
  global $l_group_members, $l_select_group,$l_back_mono, $l_private, $l_public;
  global $ico_group, $l_calendar, $l_add;

  $iso = $calendar['date']->getURL();
  $this_group = $calendar['entity']['group_view'];
  $category_filter = $calendar['category_filter'];
  $group_privacy = $calendar['group_privacy'];


  if (count($entity_readable['user']['ids']) > 1) {
    //-- Private Group selection
    if ($this_group == $c_all) {
      $sel_group = "<option selected=\"selected\">$l_select_group</option>";
    } else {
      $sel_group = "<option value=\"calendar_index.php?sel_category_filter=&amp;new_group=1&amp;group_view=$c_all&amp;sel_user_id[]=$obm[uid]&amp;date=$iso\">$l_select_group</option>";
    }

    while ($entity_readable['group']->next_record()) {
      $g_id = $entity_readable['group']->f('group_id');
      $g_priv = $entity_readable['group']->f('group_privacy');
      $g_name = $entity_readable['group']->f('group_name');
      if ($g_priv) {
        if ($g_id == $this_group) {
          $selected_s = "selected=\"selected\"";
        } else {
          $selected_s = '';
        }
        $sel_group .= "<option value=\"calendar_index.php?date=$iso&amp;group_id=$g_id&amp;new_group=1\" $selected_s>$g_name</option>\n";
      }
    }
    $sel_group .= '</select>';

    if ($this_group != $c_all && empty($selected_s)) {
      $g_public_name = get_last_group_text($this_group);
    } 

    $url_group = "$path/group/group_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget_text=f_entity_group.group_name&amp;ext_widget=f_entity_group.ext_group_id&amp;filter_entity=calendar&amp;ext_action=index&amp;ext_url=".urlencode($path.'/calendar/calendar_index.php')."&amp;ext_target=$l_calendar&amp;iso_date=$iso";
    $group_add_button = "
      <a href=\"javascript: return false;\" 
      onclick=\"popup('$url_group', '');
    return false;\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>";

    $block_group = "
    <form name=\"f_entity_group\" action=\"\" onsubmit=\"return false;\">
    <fieldset class=\"detail extra\">
    <legend>$l_private_group_members</legend>
    <select name=\"action\" class=\"calendarQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
    $sel_group
    </select>
    <div id=\"sel_group\">
    </div>    
    </fieldset>
    <fieldset class=\"detail extra\">
    <legend>$l_public_group_members $group_add_button</legend>
    <input type=\"text\" name=\"group_name\" value=\"$g_public_name\" id=\"group_name\" autocomplete=\"off\" />
    <input type=\"hidden\" name=\"ext_group_id\" id=\"ext_group_id\" />
    </fieldset>
    </form>";

    $user_text = phpStringToJsString($l_search_users);
    $url_user = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent&amp;filter_entity=calendar";
    $user_add_button = "
      <a href=\"javascript: return false;\" 
      onclick=\"popup('$url_user','$user_text');
    return false;\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>";

    $block_user = "<fieldset class=\"detail extra\">
      <legend>
      $l_users
      $user_add_button
      </legend>
      <input type=\"text\" name=\"\" value=\"\" onfocus=\"\" onblur=\"\" id=\"user\" autocomplete=\"off\" />
      <input type=\"hidden\" name=\"date\" value=\"".$calendar['date']->getIso()."\" />
      <input type=\"hidden\" name=\"group_view\" value=\"$c_all\" />
      <input type=\"hidden\" name=\"new_group\" value=\"1\" />
      <input type=\"hidden\" name=\"new_sel\" value=\"1\" />
    </fieldset>";

    $user_complete = "new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_ent', 'user', {filter_entity:'calendar', filter_pattern:'read', defaultText:'$user_text'});";
    $group_complete = "new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search', 'sel_group', 'group_name', {filter_entity:'calendar', filter_pattern:'read', mode: 'mono',
        locked: 'true', resetable: 'true', selectfunction:'sel_public_group(\"$iso\", element, this.inputField.value)'});";
  }
  // loop through ['user'], ['resource']
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
      $name = $data['name'];
      $data_id = "data-$type-$id";
      $div_id = "sel_ent-$data_id";
      $sel_ent .= "<input value=\"$data_id\" name=\"sel_ent[]\" type=\"hidden\" />";
    }

  }


  if ($ccalendar_resource && count($entity_readable['resource']['ids']) > 0) {
    $resource_text = phpStringToJsString($l_search_resources);
    $url_resource = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent&amp;filter_entity=Resource";
    $resource_add_button = "    
      <a href=\"javascript: return false;\"
      onclick=\"popup('$url_resource','');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" /> </a>";
    $dis_block_resource = "
    </fieldset>
    <fieldset class=\"detail extra\">
    <legend>
      $l_resources
      $resource_add_button
    </legend>
    <input type=\"text\" name=\"\" value=\"\" id=\"resource\" autocomplete=\"off\" />";

    $resource_complete = "new obm.AutoComplete.Search('$path/resource/resource_index.php?action=ext_search&ajax=1', 'sel_ent', 'resource', {filter_entity:'Resource', defaultText:'$resource_text'});";
  }
  $cats1 = of_category_get_ordered('event', 'category1');
  $sel_cat1 = of_category_html_select('event', 'category1', $cats1, $category_filter,'all_f',"onchange=\"this.form.submit()\"",'category_filter');
 
  $dis_go_today = "<a href=\"$path/calendar/calendar_index.php?date=\" >$l_today</a>";

  if($block_user || $block_group || $dis_block_resource) {
    $block_entity = "
    $block_group
    <form action=\"calendar_index.php\" method=\"get\">
    $block_user
    $dis_block_resource
    <fieldset class=\"detail extra\">
    <legend>$l_add</legend>
    </fieldset>
    $sel_ent
    <div id=\"sel_ent\">
    </div>
    <fieldset class=\"buttons\">
    <input type=\"submit\" value=\"$l_validate\" />
    </fieldset>
    </form>";

   if(isset($_SESSION['set_cal_default_view'])) {
      $default_view_url_params = run_query_calendar_get_bookmark_property($_SESSION['set_cal_default_view']);
      $dis_back_myview = "<a id=\"my_view\" href=\"calendar_index.php?$default_view_url_params\">$l_back_mono</a> -";
    } else {
      $dis_back_myview = "<a id=\"my_view\" href=\"calendar_index.php?sel_category_filter=&amp;new_sel=1&amp;group_view=$c_all&amp;sel_user_id[]=$obm[uid]&amp;date=$iso\">$l_back_mono</a> -";
    }    
  } 
  $block = "
    <div class=\"portlet\" id=\"calendarSelection\">
    <h1>$l_selection</h1>
    <div>
    <form action=\"\">
    <fieldset class=\"detail extra\">
    <legend>$l_category_filter</legend>
      $sel_cat1 
      <input type=\"hidden\" name=\"date\" value=\"".$calendar['date']->getIso()."\" />
    </fieldset>
    </form>
    $block_entity
    <div class='C'>
    $dis_back_myview
    $dis_go_today
    </div>
   </div>
  </div>
  <script type=\"text/javascript\">
    obm.vars.images.update = '$ico_group';
    obm.initialize.chain(function () {
      $user_complete
      $group_complete
      $resource_complete
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
  global $ccalendar_weekstart,$ico_ok, $path;
  global $l_go, $l_go_to, $l_daysofweekfirst;
  
  $date = $calendar['date'];
  $date_end = clone $date; 
  $date_end->addMonth(9);
  // Week Selection
  $current = clone $date;
  $current->subMonth(3)->setWeekday($ccalendar_weekstart);
  $end = clone $current;
  $end->addDay(6);
  while ($current->compare($date_end) < 0) {
    $label = $current->localize(Of_Date::OBM_DATE_WEEK_JUMP).' - '.$end->localize(Of_Date::OBM_DATE_WEEK_JUMP);
    $iso = $current->getURL();
    if ($current->compareWeek($date) == 0) {
      $sel_week .= "<option value=\"calendar_index.php?cal_range=week&amp;date=$iso\" selected=\"selected\">$label</option>";
    } else {
      $sel_week .= "<option value=\"calendar_index.php?cal_range=week&amp;date=$iso\">$label</option>";
    }
    $current->addWeek(1);
    $end->addWeek(1);
  }

  $current = clone $date;
  $current->subMonth(3);
  // Month Selection
  while($current->compare($date_end) < 0) {
    $label = $current->localize(Of_Date::OBM_DATE_MONTH);
    $iso = $current->getURL();
    if ($current->compareMonth($date) == 0) {
      $sel_month .= "<option value=\"calendar_index.php?cal_range=month&amp;date=$iso\" selected=\"selected\">$label</option>\n";
    } else {
      $sel_month .= "<option value=\"calendar_index.php?cal_range=month&amp;date=$iso\">$label</option>\n";
    }
    $current->addMonth(1);
  }

  $current = clone $date;
  $current->setWeekday($ccalendar_weekstart);
  $end = clone $current;
  $end->addDay(6);
  // Minicalendar Head
  while($current->compare($end) <= 0) {
    $day = $current->localize(Of_Date::WEEKDAY_NARROW);
    $dis_minical_head .= "<td>$day</td>\n";
    $current->addDay(1);
  }

  // Minicalendar
  $current = clone $date;
  $current->setDay(1)->addDay(1)->subWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  while($current->compare($end) < 0) {
    if ($current->compareWeekday($ccalendar_weekstart) == 0) $dis_minical .= "<tr>\n";
    $day = $current->get(Of_Date::DAY);
    $iso = $current->getURL();
    if ($current->isToday()) {
      $klass = 'highlight';
    } elseif ($current->compareMonth($date)!= 0) {
      $klass = 'downlight';
    } else {
      $klass = '';
    }
    $dis_minical .= "<td class=\"$klass\" onclick=\"window.location.href='calendar_index.php?cal_range=day&amp;date=$iso'\"
      onmouseout=\"this.className='$klass'\" onmouseover=\"this.className='hover'\">
      $day
      </td>";
    $current->addDay(1);
    if ($current->compareWeekday($ccalendar_weekstart) == 0) $dis_minical .= "</tr>\n";
  } 
  $label = $date->localize(Of_Date::OBM_DATE_MONTH);
  $current = new Of_Date();
  $block = "
  <div class=\"portlet\" id=\"calendarMiniCal\">
    <h1>
    <a href=\"$path/calendar/calendar_index.php?date=".$current->setDateIso($date)->subYear(1)->getURL()."\">&lt;&lt;</a>
    <a href=\"$path/calendar/calendar_index.php?date=".$current->setDateIso($date)->subMonth(1)->getURL()."\">&lt;</a>
    $label
    <a href=\"$path/calendar/calendar_index.php?date=".$current->setDateIso($date)->addMonth(1)->getURL()."\">&gt;</a>
    <a href=\"$path/calendar/calendar_index.php?date=".$current->setDateIso($date)->addYear(1)->getURL()."\">&gt;&gt;</a>
    </h1>
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


/**
 * HTML for the view selector portlet
 *
 * @param $calendar calendar parameters 
 * @param $my_view user's views
 * @access public
 * @return HTML
*/
function html_calendar_view_selector($calendar, $my_views) {
  global $obm;
  global $l_views, $l_view_label, $l_select, $l_select_view, $l_label_default, $l_delete_default_view, $l_insert_default_view; 
  global $l_view_save, $l_view_delete, $l_validate, $l_fill_view_label;
  global $l_confirm_delete_view, $l_no_sel_view, $l_conflict_view_label, $l_confirm_default_view, $l_no_sel_default_view;
  global $ico_delete, $ico_add, $ico_popup_close;

  $view_id = $calendar['view_id'];
  $iso = $calendar['date']->getURL();
  $default_view_id = $_SESSION['set_cal_default_view'];
  $display = 'none';
  if (isset($default_view_id)) {
    $default_view = $l_delete_default_view;
    $display = 'inline';
  } 
  while($my_views->next_record()) {
    $id = $my_views->f('obmbookmark_id');
    $label = $my_views->f('obmbookmark_label');
    if ($id == $view_id) {
      $selected = " selected=\"selected\"";
      if ($id != $default_view_id) {
        $default_view = $l_insert_default_view;
        $display = 'inline';
        $style_default = '';
      } else {
        $style_default = " default=\"default\"";
      }
    } elseif ($id == $default_view_id) {
      $style_default = " default=\"default\"";
      $selected = '';
    } else {
      $selected = '';
      $style_default = '';
    }
    
    $obmbookmarkproperty = run_query_calendar_get_bookmark_property($id);
    $sel_view .= "
    <option id=\"opt_$id\" value=\"calendar_index.php?new_sel=1&amp;$obmbookmarkproperty\"$style_default$selected>$label</option>";
  }

  // Javascript Strings
  $l_js_confirm_delete_view = phpStringToJsString($l_confirm_delete_view);
  $l_js_delete_default_view = phpStringToJsString($l_delete_default_view);
  $l_js_insert_default_view = phpStringToJsString($l_insert_default_view);
  $l_js_no_sel_view = phpStringToJsString($l_no_sel_view);
  $l_js_no_sel_default_view = phpStringToJsString($l_no_sel_default_view);
  $l_js_conflict_view_label = phpStringToJsString($l_conflict_view_label);
  $l_js_view_save = phpStringToJsString($l_view_save);
  $l_js_confirm_default_view = phpStringToJsString($l_confirm_default_view);

  $block = "
  <div class=\"portlet\" id=\"calendarSelView\">
    <h1>$l_views</h1>
    <div style=\"margin: 2px;\">
    <form action=\"calendar_index.php\" method=\"get\">
      <select id=\"sel_view\" name=\"sel_view\" style=\"width:12em;\"
        onchange=\"change_view(this.options[this.selectedIndex].value);\">
        <option id=\"opt_choix\" value=\"calendar_index.php?sel_category_filter=&amp;new_group=1&amp;group_view=$c_all&amp;sel_user_id[]=$obm[uid]&amp;date=$iso\" >
          $l_select_view
        </option>
        $sel_view
      </select>
      <input type=\"hidden\" id=\"view_id\" name=\"view_id\" value=\"$view_id\" />
      <input type=\"hidden\" id=\"default_view_id\" name=\"default_view_id\" value=\"$default_view_id\" />
      <a href=\"javascript:void(0);\" onclick=\"obm.calendarView.remove();return false;\">
        <img src=\"$ico_delete\" alt=\"[$l_view_delete]\"/>
      </a>
      <a href=\"javascript:void(0);\" onclick=\"obm.calendarView.show();return false;\">
        <img src=\"$ico_add\" alt=\"[$l_view_save]\"/>
      </a>
      <a href=\"javascript:void(0);\" id=\"default\" name=\"default\" onclick=\"obm.calendarView.default_view();return false;\" style=\"display: $display;\">$default_view</a>
    </form>

    </div>
  </div>
  <script type=\"text/javascript\">
    obm.vars.labels.delete_view = '$l_js_confirm_delete_view';
    obm.vars.labels.delete_default_view = '$l_js_delete_default_view';
    obm.vars.labels.insert_default_view = '$l_js_insert_default_view';
    obm.vars.labels.no_sel_view = '$l_js_no_sel_view';
    obm.vars.labels.no_sel_default_view = '$l_js_no_sel_default_view';
    obm.vars.labels.conflict_view_label = '$l_js_conflict_view_label';
    obm.vars.labels.save_view = '$l_js_view_save';
    obm.vars.labels.default_view = '$l_js_confirm_default_view';
    obm.calendarView = new Obm.CalendarView();

  </script>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the month
// Parameters:
//   - $calendar: calendar parameters
//   - $calendar_entity : array of entities to display ['user'] ['resource'] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_month_agenda($calendar, $calendar_entity,$writable_entity) { 
  global $ico_previous, $ico_next, $ico_far_next, $ico_far_previous;
  global $ccalendar_weekstart, $ccalendar_first_hour, $ico_allday;
  global $ico_resize, $ico_periodic, $ico_meeting, $ico_extension, $ico_private;
  global $l_private,$l_daysofweek, $l_previous, $l_next;

  $GLOBALS['js']['vars']['images']['resize'] = $ico_resize;
  $GLOBALS['js']['vars']['images']['periodic'] = $ico_periodic;
  $GLOBALS['js']['vars']['images']['meeting'] = $ico_meeting;
  $GLOBALS['js']['vars']['images']['private'] = $ico_private;
  $GLOBALS['js']['vars']['images']['all_day'] = $ico_allday;
  $GLOBALS['js']['vars']['images']['extension'] = $ico_extension;

  $date = clone $calendar['date'];
  $date->setDay(1)->addDay(1)->subWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $category_filter = $calendar['category_filter'];

  $current = clone $date;
  $end = clone $current;
  $end->addDay(6);
  while($current->compare($end) <= 0) {
    $day = $current->localize(Of_Date::WEEKDAY_NARROW);
    $dis_month_head .= "<td>$day</td>\n";
    $current->addDay(1);
  }

  $current = clone $date;
  $end = clone $calendar['date'];
  $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  while($current->compare($end) < 0) {
    $iso = $current->getURL();
    $day = $current->get(Of_Date::DAY);
    if ($current->compareWeekday($ccalendar_weekstart) == 0) {
      $label = $current->get(Of_Date::WEEK);
      $dis_month_cal .= "
      <tr>
        <th>
        <a href=\"calendar_index.php?cal_range=week&amp;date=$iso\" >$label</a>
        </th>";
    }
    if ($current->isToday()) {
      $klass = 'highlight';
    } elseif ($current->compareMonth($calendar['date']) != 0) {
      $klass = 'downlight';
    } else {
      $klass = '';
    }

    $dis_month_cal .= "<td id=\"day_".($current->getTimestamp() - $date->getTimestamp())."\" class=\"$klass\">
      <h2><a href=\"calendar_index.php?cal_range=day&amp;date=$iso\">$day</a></h2>";
    $current->addDay(1);
    if ($current->compareWeekday($ccalendar_weekstart) == 0) $dis_month_cal .= "</tr>\n";
  } 

  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter);
  
  if (is_array($occurrences)) {
    $week_duration = 86400*7;
    $options = array('xUnit:86400',"yUnit:$week_duration",
                     "context: 'body'");
    foreach($occurrences as $occurrence) {
      $dis_event .= dis_calendar_event($occurrence, 'day',$calendar_entity,$writable_entity,$options);
    }
  }

  $label = $calendar['date']->localize(Of_Date::OBM_DATE_MONTH);
  $current = new Of_Date();

  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subYear(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subMonth(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addMonth(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addYear(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1> 
  <table class=\"calendar month\">
    <colgroup span=\"1\" />
    <colgroup span=\"7\" id=\"calendarEventContext\" />
    <thead>
    <tr><td id=\"calendarHourCol\"></td>$dis_month_head</tr>
    </tr>
    </thead>
    <tbody id=\"calendarBody\" onDblClick=\"obm.calendarQuickForm.compute(event, 'month');\">
    $dis_month_cal
    </tbody>
  </table>
  <script type=\"text/javascript\">
    showWaitingPanel('calendarBody');
    obm.initialize.chain(function () {
      obm.calendarManager = new Obm.CalendarManager(".$date->setHour($ccalendar_first_hour)->getTimestamp().",'".$date->getOption('timezone')."');
      obm.calendarManager.lock();
      $dis_event
      obm.calendarManager.unlock();
      obm.calendarManager.resizeWindow();
      discardWaitingPanel();
    });
  </script>";

  return $block;
}


/**
 * Display the week working view
 *
 * @param array $calendar calendar parameters
 * @param array $calendar_entity : entities to display ['user'] ['resource'] 
 * @param array $writable_entity : entities with write access
 */
function dis_calendar_week_agenda($calendar, $calendar_entity,$writable_entity) {
  global $path, $ico_next, $ico_previous, $l_previous, $l_next;
  global $ccalendar_weekstart, $ico_far_next, $ico_far_previous, $ico_allday;
  global $ico_resize, $ico_periodic, $ico_meeting, $ico_extension,$ico_private;
  global $ccalendar_first_hour, $ccalendar_last_hour;
 
  $GLOBALS['js']['vars']['images']['resize'] = $ico_resize;
  $GLOBALS['js']['vars']['images']['periodic'] = $ico_periodic;
  $GLOBALS['js']['vars']['images']['meeting'] = $ico_meeting;
  $GLOBALS['js']['vars']['images']['private'] = $ico_private;
  $GLOBALS['js']['vars']['images']['all_day'] = $ico_allday;
  $GLOBALS['js']['vars']['images']['extension'] = $ico_extension;
  $GLOBALS['js']['vars']['consts']['timeUnit'] = (3600/$_SESSION['set_cal_interval']);    

  $display_days = $_SESSION['set_cal_display_days'];
  if (empty($display_days)) $display_days = '1111111';
  $display_days = str_split($display_days);
  $category_filter = $calendar['category_filter'];
  $date = clone $calendar['date'];
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);

  $size = 0;
  $current = clone $date;
  while($current->compare($end) < 0) {
    if($display_days[$current->get(Of_Date::WEEKDAY_DIGIT)] == 1) {
      $day = $current->localize(Of_Date::OBM_DATE_WEEK_LIST);
      $iso = $current->getURL();
      $week_day_list .= "<td><a href=\"calendar_index.php?cal_range=day&amp;date=$iso\">$day</a></td>\n";
      $week_day_event_list .= "<th id=\"day_".($current->getTimestamp() - $date->getTimestamp())."\"> </th>";
      $size++;
    }
    $current->addDay(1);
  }

  $width = ceil(98/$size);

  $current->setHour($ccalendar_first_hour);
  // Week Calendar content
  while ($current->compareHour($ccalendar_last_hour) < 0 && $current->compareDateIso($end) <= 0) {
    $current->setDateIso($date);
    if($current->compareMinute(0) == 0) $label = $current->get(Of_Date::TIME_SHORT);
    else $label = '';
    $week_cal .= "<tr onmouseover=\"this.className='hover'\" onmouseout=\"this.className=''\"><th>$label</th>";
    while($current->compareDateIso($end) != 0) {
      if($display_days[$current->get(Of_Date::WEEKDAY_DIGIT)] == 1) {
        $week_cal .= "<td id=\"time_".$current->getTimestamp()."\"></td>";
      }
      $current->addDay(1);
    }
    $current->addMinute(60/$_SESSION['set_cal_interval']);
    if($current->compareMinute(0) == 0) $week_cal .= '</tr>';
  }
  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date,$end,$calendar_entity, $status, $category_filter);
  if (is_array($occurrences)) {
    foreach($occurrences as $occurrence) {
      if ($display_days[$occurrence->date->get(Of_Date::WEEKDAY_DIGIT)] == 1) {
        $dis_event .= dis_calendar_event($occurrence, 'hour', $calendar_entity, $writable_entity);
      }
    }
  }

  $label = $date->localize(Of_Date::OBM_DATE_WEEK) . ' - ' . $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK);
  $current = new Of_Date();

  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subMonth(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subWeek(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addWeek(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addMonth(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1>
  <style>
    .week tbody td{
      width: $width%;
    }
  </style>
  <table class=\"calendar week\">
    <colgroup />
    <colgroup span=\"7\" id=\"calendarEventContext\" />
    <thead onDblClick=\"obm.calendarQuickForm.compute(event, 'week');\">
    <tr>
    <td id=\"calendarHourCol\">".$date->get(Of_Date::WEEK)."</td>
    $week_day_list
    </tr>
    <tr id=\"calendarHead\">
    <td>&nbsp;</td>
    $week_day_event_list
    </tr>    
    </thead>
    <tbody id=\"calendarBody\" onDblClick=\"obm.calendarQuickForm.compute(event, 'week');\">
    $week_cal
    </tbody>
  </table>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      obm.calendarManager = new Obm.CalendarManager(".$date->setHour($ccalendar_first_hour)->getTimestamp().",'".$date->getOption('timezone')."');
      obm.calendarManager.lock();
      $dis_event
      obm.calendarManager.unlock();
      obm.calendarManager.resizeWindow();
    });
  </script>";
  return $block;
}

///////////////////////////////////////////////////////////////////////////////;
// Display the planning for the day
// Parameters:
//   - $calendar         : calendar parameters
//   - $calendar_entity : array of entities to display ['user'] ['resource'] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_day_agenda($calendar, $calendar_entity,$writable_entity) {
  global $ico_previous, $ico_next, $ico_far_next, $ico_far_previous;
  global $ccalendar_first_hour, $ccalendar_last_hour, $ico_allday;
  global $ico_resize, $ico_periodic, $ico_meeting, $ico_extension,$ico_private;
  global $l_private, $l_all_day, $l_previous, $l_next;

  $GLOBALS['js']['vars']['images']['resize'] = $ico_resize;
  $GLOBALS['js']['vars']['images']['periodic'] = $ico_periodic;
  $GLOBALS['js']['vars']['images']['meeting'] = $ico_meeting;
  $GLOBALS['js']['vars']['images']['private'] = $ico_private;
  $GLOBALS['js']['vars']['images']['all_day'] = $ico_allday;
  $GLOBALS['js']['vars']['images']['extension'] = $ico_extension;
  $GLOBALS['js']['vars']['consts']['timeUnit'] = (3600/$_SESSION['set_cal_interval']);    

  $category_filter = $calendar['category_filter'];
  $date = $calendar['date'];
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);
  $current = clone $date;  
  $current->setHour($ccalendar_first_hour);
  // Display Calendar
  while($current->compareHour($ccalendar_last_hour) < 0 && $current->compareDateIso($date) == 0) {
    if($current->compareMinute(0) == 0) $label = $current->get(Of_Date::TIME_SHORT);
    else $label = '';    
    $day_cal .= "\n<tr><th>$label</th><td id=\"time_".$current->getTimestamp()."\"></td></tr>";
    $current->addMinute(60/$_SESSION['set_cal_interval']);
  }

  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter);
  if (is_array($occurrences)) {
    $options = array();
    foreach($occurrences as $occurrence) {
      $dis_event .= dis_calendar_event($occurrence, 'hour',$calendar_entity,$writable_entity,$options);
    }
  }

  $label = $date->localize(Of_Date::OBM_DATE_DAY);
  $current = new Of_Date();

  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subWeek(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subDay(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addDay(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addWeek(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1>    
  <table class=\"calendar day\">
    <colgroup span=\"1\" />
    <colgroup span=\"1\" id=\"calendarEventContext\" />
    <thead onDblClick=\"obm.calendarQuickForm.compute(event, 'day');\">
    <tr id=\"calendarHead\">
    <td id=\"calendarHourCol\">".$date->get(Of_Date::WEEK)."</td><th id=\"day_0\"></th>
    </tr>    
    </thead>
    <tbody id=\"calendarBody\" onDblClick=\"obm.calendarQuickForm.compute(event, 'day');\">
    $day_cal
    </tbody>
  </table>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      obm.calendarManager = new Obm.CalendarManager(".$date->setHour($ccalendar_first_hour)->getTimestamp().",'".$date->getOption('timezone')."');
      obm.calendarManager.lock()
      $dis_event
      obm.calendarManager.unlock();
      obm.calendarManager.resizeWindow();
    });
  </script>";

  return $block;
}

/**
 * Display Event List over one week
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_day_list($calendar, $calendar_entity, $writable_entity) {
  global $ico_previous, $ico_next, $l_previous, $l_next, $ico_far_next, $ico_far_previous;
  global $ccalendar_weekstart, $ccalendar_first_hour, $ccalendar_last_hour;

  $category_filter = $calendar['category_filter'];
  $date = clone $calendar['date'];
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);

  $label = $date->localize(Of_Date::OBM_DATE_WEEK);
  $current = new Of_Date();
  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subMonth(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subWeek(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addWeek(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addMonth(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1>";
  $block .= dis_calendar_list_view($calendar, $calendar_entity, $writable_entity, $date, $end);

  return $block;
}

/**
 * Display Event List over one week
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_week_list($calendar, $calendar_entity, $writable_entity) {
  global $ico_previous, $ico_next, $l_previous, $l_next, $ico_far_next, $ico_far_previous;
  global $ccalendar_weekstart, $ccalendar_first_hour, $ccalendar_last_hour;

  $category_filter = $calendar['category_filter'];
  $date = clone $calendar['date'];
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(6);

  $label = $date->localize(Of_Date::OBM_DATE_WEEK).' - '.$end->localize(Of_Date::OBM_DATE_WEEK);
  $current = new Of_Date();
  $end->addDay(1);
  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subMonth(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subWeek(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addWeek(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addMonth(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1>";
  $block .= dis_calendar_list_view($calendar, $calendar_entity, $writable_entity, $date, $end);

  return $block;
}


/**
 * Display Event List over one month
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_month_list($calendar, $calendar_entity, $writable_entity) {
  global $ico_previous, $ico_next, $l_previous, $l_next, $ico_far_next, $ico_far_previous;
  
  $date = clone $calendar['date'];
  $date->setDay(1)->setHour(0)->setMinute(0)->setSecond(0);
  $category_filter = $calendar['category_filter'];
  $end = clone $date;
  $end->addMonth(1);
  $label = $calendar['date']->localize(Of_Date::OBM_DATE_MONTH);
  $current = new Of_Date();
  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subYear(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subMonth(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addMonth(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addYear(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1>"; 
    
  $block .= dis_calendar_list_view($calendar, $calendar_entity, $writable_entity, $date, $end);
  return $block;
}


/**
 * Display Event List
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @return event list HTML code
 */
function dis_calendar_list_view($calendar, $calendar_entity, $writable_entity, $date, $end) {
  global $obm;
  global $l_daysofweek, $l_monthsofyear, $l_attendees, $l_title, $l_location;
  global $l_desc, $l_category,$l_datebegin, $l_dateend, $l_private, $l_repeat;
  global $l_daily, $l_weekly, $l_monthlybydate, $l_monthlybyday, $l_yearly;
  global $l_date, $l_resources, $l_items, $l_event, $l_owner;
  global $l_expand_all, $l_collapse_all,$ccalendar_last_hour,$ccalendar_weekstart;
  global $ico_periodic_alt, $ico_meeting_alt, $ico_private_alt, $ico_search;
  global $ccalendar_first_hour, $ico_allday_alt;

  $sel_user = $calendar_entity['user'];
  $sel_user_id = array();
  if (is_array($sel_user)) {
    while(list($key, $val) = each($sel_user)) {
     $sel_user_id[$key] = $val['class'];
    }
  }

  $sel_res = $calendar_entity['resource'];
  $sel_res_id = array();
  if (is_array($sel_res)) {
    while(list($key, $val) = each($sel_res)) {
     $sel_res_id[$key] = $val['class'];
    }
  }

  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
       
    }    
  }
  
  $category_filter = $calendar['category_filter'];

  $status = array('ACCEPTED');
  $occurrences = calendar_events_model($date, $end,$calendar_entity, $status, $category_filter,true);
  $today_mark = false;
  $charset = mb_internal_encoding();

  // Sort occurrences by date (cause repeat and no repeat events)
  if (is_array($occurrences)) {
    foreach($occurrences as $occurrence) {
      $d = $occurrence->date->get(Of_Date::DATE_MEDIUM);
      if(!is_array($ocs[$d])) { $ocs[$d] = array();}
      array_push($ocs[$d],$occurrence);
    }
  }
  if (is_array($ocs)) {
    ksort($ocs);
    foreach($ocs as $key => $occurrence) {
      $current = new Of_Date($key);  
      if ($i%2 == 0) $klass = "pair"; else $klass = '';
      $i ++;
      $label = $current->localize(Of_Date::OBM_DATE_WEEK_LONG);
      if ($current->isToday())$klass="highlight";
      $dis_events .= "
        <tr class=\"$klass\">
          <td><a href=\"calendar_index.php?cal_range=day&amp;date=".$current->getURL()."\">$label</a></td>
          <td>";

      foreach($occurrence as $k => $v) {
        $event = $v->event;
        $date = $v->date;
        $hour = $v->date->get(Of_Date::TIME_SHORT);
        $end = clone $v->end;
        $ev_id = $event->id;
        $title = $event->title;
        $location = $event->location;
        $attendee = $event->attendee;
        $category = $event->category1;
        $repeat = $event->repeat;
        if ($repeat == 'none') {
    $dis_repeat = '';
        } else {
    $dis_repeat = ${"l_$repeat"};
        }

        if ($event->privacy == 1 && !(array_key_exists($obm['uid'],$attendee['user']))) {
    $title = $l_private;
    $description = $title;
        } else {
    $title = $event->title;
    $description = $event->description;
          $properties = $event->properties;
          $item_list = '';
          if ($properties != '') {
            $item = "<?xml version='1.0' encoding='$charset'?>$properties";
            $xml = parse_item($item);
            if (is_object($xml)) {
              foreach($xml->resource as $key => $value) {
                if (!empty($value->item)) {
                  if (!empty($value->property_value)) {
        $block_property = "($value->property_name:$value->property_value)";
                  } else {
        $block_property = '';
                  }
                  $item_list .= "<strong>$value->res_name</strong> $block_property<br>".
                    nl2br($value->item);
                }
              }
            }
          }
          if($event->isPrivate()) {
            $title = "<img src=\"$ico_private_alt\" alt=\"[Private]\" /> $title";
          }
          if($event->isMeeting()) {
            $title = "<img src=\"$ico_meeting_alt\" alt=\"[Meeting]\" /> $title";
          }
          if ($event->isPeriodic()) {
            $title = "<img src=\"$ico_periodic_alt\" alt=\"[Periodic]\" /> $title";
          }                  
          if ($event->isAllDay()) {
            $title = "<img src=\"$ico_allday_alt\" alt=\"[All Day]\" /> $title";
          }                  
        }
        $dis_owner = $event->owner_name;
        $dis_attendees = '';
        $br = '';
        $dis_people = '';
        $separator = '';
        if (is_array($attendee['user'])) {
          foreach($attendee['user'] as $at_id => $at_name) {
            if (array_key_exists($at_id, $sel_user_id)) {
              $dis_people .= "$separator$at_name";
              $separator = ', ';
            }
            $dis_attendees .= "$br$at_name";
            $br = '<br />';
          } 
        }

        if (is_array($attendee['contact'])) {
          foreach($attendee['contact'] as $at_id => $at_name) {
            if (array_key_exists($at_id, $sel_user_id)) {
              $dis_people .= "$separator$at_name";
              $separator = ', ';
            }
            $dis_attendees .= "$br$at_name";
            $br = '<br />';
          } 
        }

        $dis_resources = '';
        $dis_res = '';
        $br = '';
        if(is_array($attendee['resource'])) {
          foreach($attendee['resource'] as $at_id => $at_name) {
            if (array_key_exists($at_id, $sel_res_id)) {
              $dis_res .= "$separator$at_name";
              $separator = ', ';
            }            
            $dis_resources .= "$br$at_name";
            $br = '<br />';
          }
        }        
        if ($event->all_day == 1) {
    $date_begin = $date->getOutputDate();
    $date_end = $end->subSecond(1)->getOutputDate(); 
  } else {
    $date_begin = $date->getOutputDateTime();
    $date_end = $end->getOutputDateTime(); 
  }
        $j++;
  $js .= "
  div = $('evt_${date}_$ev_id');
  a = div.getFirst();
        info = $('eventDetail_${date}_$ev_id');
  slides[$j] = new Fx.Slide(info, {duration: 150,wait:false});  
  slides[$j].hide();
  info.setStyle('display','block');  
  a.addEvent('click', function(e){
    slides[$j].toggle();
       });
      ";
      $dis_events .= "<div id=\"evt_${date}_$ev_id\" >
          <a href=\"javascript: void(0);\"><span style=\"float: right;\">$dis_people $dis_res</span>
          $hour - <strong>$title</strong></a>
          <div id=\"eventDetail_${date}_$ev_id\" class=\"eventDetail detail\" style=\"display: none;\">
            <table>
              <tr>
                <th colspan='2'>$date_begin - $date_end</th>
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
                <th>$l_owner</th><td>$dis_owner</td>
              </tr>
              <tr>
                <th>$l_attendees</th><td>$dis_attendees</td>
              </tr>
              <tr>
                <th>$l_resources</th><td>$dis_resources</td>
              </tr>
              <tr>
                <th>$l_items</th><td>$item_list</td>
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
  global $ico_resize, $ico_periodic, $ico_meeting, $ico_private;
  global $l_user, $l_contact, $l_resource, $l_private;
  global $output_target;

  $event = $occurrence->event;
  $owner = $event->owner;
  $owner_name = phpStringToJsString($event->owner_name);

  if ($owner == $obm['uid'] || in_array($owner,$writable_entity['ids'])) {
    $updatable =  true;
    $ed[] = 'updatable:true';
  }
  
  if ($output_target == 'print') {
    $updatable =  false;
    $ed[] = 'updatable:false';
  }

  $attendee = $event->attendee;
  if ($event->privacy == 1 && (is_null($attendee['user'][$obm['uid']])) && $owner != $obm['uid']) {
    $title = phpStringToJsString($l_private);
    $description = $title;
    $location = $title;
    $category = $title;
  } else {
    $title = phpStringToJsString($event->title);
    $description = phpStringToJsString($event->description);
    $properties = $event->properties;
    if ($properties != '') {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset'?>".$properties;
      $xml = parse_item($item);
      if (is_object($xml)) {
        foreach($xml->resource as $key => $value) {
          if (!empty($value->item)) {
            $item_list .= "
              <h4>$value->res_name</h4>
              ".nl2br($value->item);
          }
        }
        $item_list = phpStringToJsString(str_replace("\n\r", ', ', $item_list));
      }
    }
    $location = phpStringToJsString($event->location);
    $category = phpStringToJsString($event->category1);  
  }

  $duration = $event->duration;
  $date = $occurrence->date;
  $end = $occurrence->end;
  $id = $event->id;
  $entity_id = $occurrence->id;
  $entity = $occurrence->entity;
  $entity_state = $occurrence->entity_state;
  $all_day = ($event->all_day)?1:0;
  $entityClass = $calendar_entity[$entity][$entity_id]['class'];
  $eventColor = $event->color;
  $ed[] = "id:$id";
  $ed[] = "entity:'$entity'";
  $ed[] = "entity_id:$entity_id"; 
  $ed[] = "duration:$duration";
  $ed[] = "item:'$item_list'";
  $ed[] = "time:".$date->getTimestamp();
  $ed[] = "title:'$title'";
  $ed[] = "all_day:".$all_day;
  $ed[] = "klass:'$entityClass'"; 
  $ed[] = "description:'$description'";
  $ed[] = "owner_name:'$owner_name'";
  $ed[] = "location:'$location'";
  $ed[] = "category:'$category'";
  $ed[] = "colors: {event:'$eventColor'}";
  $ed[] = "status:'$entity_state'";
  $attendees = array();
  foreach ($attendee as $entity => $entities) {
    $label_entity = phpStringToJsString(${"l_$entity"});
    $ent = array();
    foreach($entities as $at) {
      $ent[] = phpStringToJsString($at);
    }
    $attendees[] = "{label:'".$label_entity."', entities:['".implode("','",$ent)."']}";
  }
  $ed[] = 'attendees:['.implode(',',$attendees).']';
  switch($unit) {
    case 'day' :
      $day = true;
      if ($updatable) {
        $options[] = 'draggable:true';
      }
      break;
    case 'hour' :
      $day = ($all_day) || ($end->compareDateIso($date) != 0);
      $day = $day || ($date->compareHour($ccalendar_first_hour) <  O);
      $day = $day || ($date->compareHour($ccalendar_last_hour) >= 0 );
      if ($updatable) {
        $options[] = 'draggable:true';
        $options[] = 'resizable:true';
      }
      break;
  }

  if ($event->isPrivate()) {
    $ed[] = 'private:true';
  } else {
    $ed[] = 'private:false';
  }
  if ($event->isMeeting()) {
    $ed[] = 'meeting:true';
  } else {
    $ed[] = 'meeting:false';
  }
  if ($event->isPeriodic()) {
    $ed[] = 'periodic:true';
  } else {
    $ed[] = 'periodic:false';
  }
  if ($all_day) {
    $head .= $title;
  } else {
    $head .= $date->get(Of_Date::TIME_SHORT);
  }
  
  $opt = '{'.implode(',',$options).'}';
  $event_data =  '{'.implode(',',$ed).'}';
  if ($day) {
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
//   - $calendar_entity : array of entities to display ['user'] ['resource'] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_month_planning($calendar, $calendar_entity, $writable_entity) { 
  global $obm, $ico_previous,$ico_next,$ico_new_event, $ico_far_next, $ico_far_previous;
  global $ccalendar_weekstart, $l_week, $l_all_day;
  global $l_private,$l_daysofweekfirst,$l_daysofweek, $l_previous, $l_next;

  $date = clone $calendar['date'];
  $date->setDay(1);
  $current = clone $date;
  $current->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addMonth(1);

  $status = array('ACCEPTED');
  $events = calendar_events_model($current,$end, $calendar_entity, $status, $category_filter);
  $of = &OccurrenceFactory::getInstance();

  $category_filter = $calendar['category_filter'];
  $cats1 = of_category_get_ordered('event', 'category1');
  $nb = count($cats1);
  $colors = random_color($nb);
  foreach($cats1 as $key => $cat_info) {
    $cats_color[$cat_info['label']] = $colors[$key];
  }


  $day_amount = 0;
  while($current->compare($end) < 0) {
    $day_amount++;
    $iso = $current->getURL();
    $label = $current->localize(Of_Date::WEEKDAY_NARROW);
    $day = $current->getDay();
    $description = $current->localize(Of_Date::WEEKDAY_NAME);
    $empty = !$of->periodHaveOccurrences($current);
    $line0 .= "<th>$label</th>\n";
    $line1 .= "<th><a href=\"calendar_index.php?cal_range=day&amp;date=$iso\">$day</a></th>\n";
    if($current->compareWeekday($ccalendar_weekstart) == 0) {
      $week = $current->getWeek();  
      $line2 .= "<th colspan=\"7\">$l_week <a href=\"calendar_index.php?cal_range=week&amp;date=$iso\">$week</a></th>";
    }elseif($current->compareDay(1) == 0) {
      $week = $current->getWeek();  
      $span = $current->diffWeekday($ccalendar_weekstart);
      if ($span >= 3) $line2 .= "<th colspan=\"$span\">$l_week <a href=\"calendar_index.php?cal_range=week&amp;date=$iso\">$week</a></th>";
      else $line2 .= "<th colspan=\"$span\"><a href=\"calendar_index.php?cal_range=week&amp;date=$iso\">$week</a></th>";
    }
    foreach ($calendar_entity as $type => $entity) {
      foreach ($entity as $id => $properties) {
        $haveevent = false;
        $line_id = $type.' '.$id;
        if($empty) {
          $line[$line_id] .= "<td onclick=\"window.location.href='calendar_index.php?cal_range=day&date=$iso'\">&nbsp;</td>";
        } else {
          $today_events = array();
          $day_content = '';
          $occurrences = $of->getPeriodOccurrences($current, null,array($type => array($id)));
          if ($occurrences != null) {
            $haveevent = true;
            foreach ($occurrences as $occurrence) {
              $event = &$occurrence->event;
              $category = $event->category1;
              $location = $event->location;
              if($event->all_day) {
                $ad = ": $l_all_day";
              } else {
                $ad = '';
              }

              if ($event->privacy == 1 && $obm['uid'] != $properties['id']) {
                $today_events[] = "<li>$l_private $ad</li>";
              } else {
                if($category != '') {
                  $style = "color:#fff;background-color:rgb(".$cats_color[$category].")";
                  $day_content .= "<td style=\"border:0;width:auto;height:auto;$style;\"></td>";
                } else {
                  $style = '';
                }                
                if ($location != '') {
                  $today_events[] = "<li style=\"$style\">".$event->title." ($location) $ad</li>";
                } else {
                  $today_events[] = "<li style=\"$style\">".$event->title." $ad</li>";
                } 
              }
            }            
          }
          $day_content = "
            <table style=\"border:0;margin:0;width:100%;height:100%;border:0;border-spacing:0;border-collapse:collapse\">
            <tr style=\"height:100%;\">$day_content</tr>
            </tbody>
            </table>";
          if($haveevent) {
            $hidden_div .= "
              <div id=\"$id_$day\" class=\"infoPopup\" style=\"border: 1px solid black;\">
              <h1>$description $day</h1>
               <ul>
               ".implode($today_events)."
               </ul>
              </div>";            
            $line[$line_id] .= "<td onclick=\"window.location.href='calendar_index.php?cal_range=day&date=$iso'\" class=\"eventOwner\"
               onmouseover=\"toggleInfo(this,'$id-$day',1); return true;\"
               onmouseout=\"toggleInfo(this,'$id-$day',0); return true;\">$day_content</td>";
          } else {
            $line[$line_id] .= "<td onclick=\"window.location.href='calendar_index.php?cal_range=day&date=$iso'\"></td>";
          }
        }
      }
    }
    $current->addDay(1); 
  }
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $properties) {
      $line_id = $type.' '.$id;
      $d_l .= '<tr><th>'.$properties['name'].'</th>'.$line[$line_id].'</tr>';
    }  
  } 
  $label = $calendar['date']->localize(Of_Date::OBM_DATE_MONTH);
  $current = new Of_Date();

  $block = "
  <style>
    $css
  </style>
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subYear(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subMonth(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addMonth(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addYear(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1> 
  <table class=\"planning\">
  <colgroup>
  </colgroup>
  <colgroup class=\"main\" span=\"$day_amount\">
  </colgroup> 
  <thead>
  <tr><th></th>$line0</tr>  
  <tr><th></th>$line1</tr>
  <tr><th></th>$line2</tr>
  </thead>
  $d_l
  </table>
  <script type=\"text/javascript\">
    function toggleInfo(container,id,display) {
      var resource = \$(id);
      var container = \$(container);
      if(resource) {
        if(display == 0) {
          resource.setStyle('display','none');       
        } else {
          if(container.getLeft() + resource.getStyle('width').toInt() < window.innerWidth ) {
            resource.setStyle('left',container.getLeft()+'px')   
          } else {
            resource.setStyle('left',container.getLeft() - resource.getStyle('width').toInt() +'px')   
          }
          resource.setStyle('top',(container.getTop()+container.offsetheight)+'px')   
          resource.setStyle('display','block');
        }
      } 
    }
  </script>  
  $hidden_div
";    

  return $block;
}

/**
 * dis_calendar_week_planning 
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_week_planning($calendar, $calendar_entity, $writable_entity) {
  global $path, $ico_next, $ico_previous, $l_previous, $l_next, $l_week;
  global $ccalendar_weekstart, $obm, $ico_far_next, $ico_far_previous;
  global $ccalendar_first_hour;

  // FIXME MAKE IT WORK
  $display_days = $_SESSION['set_cal_display_days'];
  if (empty($display_days)) $display_days = '1111111';

  $category_filter = $calendar['category_filter'];
  $date = clone $calendar['date'];
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);
  
  $dis_planning = dis_calendar_planning_view ($date, $end, $calendar_entity, $category_filter);

  $label = $date->localize(Of_Date::OBM_DATE_WEEK) .' - '. $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK);
  $current = new Of_Date();

  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subMonth(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subWeek(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addWeek(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addMonth(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1>
  $dis_planning 
  ";

  return $block;
}

/**
 * dis_calendar_day_planning
 * 
 * @param mixed $calendar 
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_day_planning($calendar, $calendar_entity,$writable_entity) {
  global $path, $ico_next, $ico_previous, $l_previous, $l_next;
  global $obm, $ico_far_next, $ico_far_previous;

  $category_filter = $calendar['category_filter'];
  $date = $calendar['date'];
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);

  $dis_planning = dis_calendar_planning_day_view ($date, $end, $calendar_entity, $category_filter);

  $label = $date->localize(Of_Date::OBM_DATE_DAY);
  $current = new Of_Date();

  $block = "
  <h1>
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subWeek(1)->getURL()."\"><img src=\"$ico_far_previous\" alt=\"[$l_previous]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->subDay(1)->getURL()."\"><img src=\"$ico_previous\" alt=\"[$l_previous]\" /></a>     
    $label
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addDay(1)->getURL()."\"><img src=\"$ico_next\" alt=\"[$l_next]\" /></a>     
    <a href=\"calendar_index.php?date=".$current->setDateIso($calendar['date'])->addWeek(1)->getURL()."\"><img src=\"$ico_far_next\" alt=\"[$l_next]\" /></a>     
  </h1>    
  $dis_planning";

  return $block;
}
/**
 * ViewRessource (ligne:time column:resource)
 *
 * @param mixed $calendar_entity
 * @param reference $en_tete
 * @return array() $entity_occ
 */
function ViewRessource($calendar_entity,&$en_tete){
  $en_tete.="<tr><th></th>";
  foreach($calendar_entity as $entity => $entities){
    foreach($entities as $id => $data){
      $entity_occ[]=array($entity => array($id),'class'=>$data['class']);
      $en_tete.="<th title='$data[name]' ><div class='NW'>$data[name]</div></th>";
    }
  }
  $en_tete.="</tr>";
  return $entity_occ;
}

/**
 * dis_calendar_planning_view (day and week views)
 * 
 * @param mixed $date 
 * @param mixed $end 
 * @param mixed $calendar_entity 
 * @param mixed $category_filter 
 * @access public
 * @return void
 */
function dis_calendar_planning_day_view($date, $end, $calendar_entity, $category_filter) {
  global $ccalendar_first_hour, $ccalendar_last_hour;

  $status = array('ACCEPTED');
  calendar_events_model($date,$end, $calendar_entity, $status, $category_filter);
  $of = &OccurrenceFactory::getInstance();
  
  $cats1 = of_category_get_ordered('event', 'category1');
  $nb = count($cats1);

  $colors = random_color($nb);
  foreach($cats1 as $key => $cat_info) {
    $cats_color[$cat_info['label']] = $colors[$key];
  }

  $en_tete="";

  $entity_occ=ViewRessource($calendar_entity,$en_tete);
  
  $nb_ressources=count($entity_occ);
  $td_width = ceil(90/$nb_ressources);

  $current_time = clone $date;
  $current_time->addHour($ccalendar_first_hour);

  $event_unit=60/$_SESSION['set_cal_interval'];

  //Permet de savoir si un evenement à déjà etait affiché
  $event=array(); 

  //Display Calendar
  for ($i = $ccalendar_first_hour; $i < $ccalendar_last_hour; $i++) {
    for ($k=0; $k<$_SESSION['set_cal_interval']; $k++) {
      if ($k==0) {
        $ressource_occ .= "\n<tr><th>$i:00</th>"; 
      } else {
        $ressource_occ .= "\n<tr><th></th>"; 
      }

      $end_event=clone $current_time;
      $end_event->addMinute($event_unit);

      foreach($entity_occ as $ress){
        $occurrences = $of->getPeriodOccurrences($current_time,$end_event,$ress);
        if(is_array($occurrences)) {
          $ressource_occ .= "<td style=\"width:$td_width%;\" class='$ress[class]' ><ul>";
          foreach($occurrences as $occurrence) {
            $event_id=$occurrence->event->id;
            $entity_type=$occurrence->entity;
            $ress_id=$ress[$entity_type]['0'];
            if(!isset($event[$event_id][$ress_id][$entity_type])){
              $ressource_occ .= dis_calendar_planning_day_event($occurrence, $calendar_entity, $current_time,$cats_color);
              $event[$event_id][$ress_id][$entity_type]=true;
            }
          }
          $ressource_occ .= '</ul></td>';
        } else {
          list($entity_type,$entity_id) = each($ress);
          $entity_id = $entity_id[0];          
          $goto = "calendar_index.php?action=new&amp;tf_date_begin=".$current_time->getURL()."&amp;new_sel=1&duration=3600&amp;new_group=1&amp;group_view=_ALL_&amp;sel_".$entity_type."_id[]=$entity_id";
          $ressource_occ .= "<td onclick=\"window.location='$goto'\" onmouseover=\"this.className='hover';this.style.cursor = 'pointer';\"
             onmouseout=\"this.className ='';\" style=\"width:$td_width%;\">&nbsp;</td>";        
        }
      }
      $ressource_occ.="</tr>";
      $current_time->addMinute($event_unit);
    }
  }

  unset($event);

  $block .= "
    <table class='spreadSheet planningRessource' style=\"width:100%;height:100%;\">
    <thead>
    $en_tete
    </thead>
    <tbody>
    $ressource_occ
    </tbody>
    </table>
  ";

  return $block;
}

/**
 * dis_calendar_planning_view 
 * 
 * @param mixed $start_time 
 * @param mixed $end_time 
 * @param mixed $calendar_entity 
 * @param mixed $category_filter 
 * @access public
 * @return void
 */
function dis_calendar_planning_view($date, $end, $calendar_entity, $category_filter) {
  global $l_daysofweekfirst,$l_daysofweek;
  
  $status = array('ACCEPTED');
  calendar_events_model($date,$end,$calendar_entity, $status, $category_filter);
  $of = &OccurrenceFactory::getInstance(); 
  $day_duration = 86400;

  $cats1 = of_category_get_ordered('event', 'category1');
  $nb = count($cats1);

  $colors = random_color($nb);
  foreach($cats1 as $key => $cat_info) {
    $cats_color[$cat_info['label']] = $colors[$key];
  }
  
  $day_amount = 0;
  $current = clone $date;
  while ($current->compare($end) < 0) {
    $day_amount++;
    $day = $current->get(Of_Date::DAY);
    $label = $current->localize(Of_Date::WEEKDAY_NAME);
    $iso = $current->getURL();
    $line0 .= "<th>$label</th>\n";
    $line1 .= "<th><a href=\"calendar_index.php?cal_range=day&amp;date=$iso\">$day</a></th>\n";
    $current->addDay(1);
  }

  $td_width = ceil(90/$day_amount);

  // Week Calendar content
  foreach($calendar_entity as $entity => $entities) {
    foreach($entities as $id => $data) {
      $dis_planning_view .= '<tr><th>'.$data['name'].'</th>';
      $current = clone $date;
      $current->setHour(0)->setMinute(0)->setSecond(0);
      while($current->compare($end) < 0) {
        $view = array();
        $occurrences = $of->getPeriodOccurrences($current,null,array($entity => array($id)));
        if(is_array($occurrences)) {
          $dis_planning_view .= "<td style=\"width:$td_width%\"><ul>";
          foreach($occurrences as $occurrence) {
            $dis_planning_view .= dis_calendar_planning_event($occurrence, $calendar_entity, $current ,$cats_color);
          }
          $dis_planning_view .= '</ul></td>';
        } else {
          $dis_planning_view .= "<td style=\"width:$td_width%\">&nbsp;</td>";
        }
        $current->addDay(1);
      }
      $dis_planning_view .= '</tr>';
    }
  }
  
  $block .= "
  <table class='planning'>
  <colgroup>
  </colgroup>
  <colgroup class=\"main\" span=\"$day_amount\">
  </colgroup> 
  <thead>
  <tr><th></th>$line0</tr>
  <tr><th></th>$line1</tr>
  </thead>
  <tbody>
  <tr>
  $dis_planning_view
  </tbody>
  </table>
  ";


  return $block;

}
/**
 * Generate the html code for an event in the planning view 
 * 
 * @param mixed $occurrence 
 * @access public
 * @return void
 */
function dis_calendar_planning_event(&$occurrence, &$calendar_entity, $date, $cats_color) {
  global $obm, $path;
  global $l_private, $l_location, $l_all_day;
  global $l_user, $l_resource, $l_private;
  global $ico_meeting, $ico_periodic, $ico_private;

  $location = '';
  if ($occurrence->event->privacy == 1 && !(array_key_exists($obm['uid'],$occurrence->event->attendee['user']))) {
    $title = $l_private;
  } else {
    $title = $occurrence->event->title;
    if ($occurrence->event->location != '') {
      $location = "<br />$l_location : ".$occurrence->event->location;
    }            
  }
  $entity_id = $occurrence->id;
  $entity = $occurrence->entity;

  $style = "color:rgb(".$cats_color[$occurrence->event->category1].")";  
  if($occurrence->event->all_day) {
    $schedule = $l_all_day; 
  } else {
    if($occurrence->date->compareDateIso($date) != 0) {
      $schedule = $occurrence->date->getOutputDateTime();
    } else {
      $schedule = $occurrence->date->get(Of_Date::TIME_SHORT);
    }
  }

  $event_data = "
    <li style='$style'>
    <a href=\"$path/calendar/calendar_index.php?action=detailconsult&amp;calendar_id=".$occurrence->event->id."\">$schedule - $title</a>
    </li>";

  return $event_data;  
}

/**
 * Generate the html code for an event in the planning day view 
 * 
 * @param mixed $occurrence 
 * @access public
 * @return void
 */
function dis_calendar_planning_day_event(&$occurrence, &$calendar_entity, $date, $cats_color) {
  global $obm, $path;
  global $l_private, $l_location, $l_all_day;
  global $l_user, $l_resource, $l_private;
  global $ico_meeting, $ico_periodic, $ico_private;

  $location = '';
  if ($occurrence->event->privacy == 1 && !(array_key_exists($obm['uid'],$occurrence->event->attendee['user']))) {
    $title = $l_private;
  } else {
    $title = $occurrence->event->title;
    if ($occurrence->event->location != '') {
      $location = "<br />$l_location : ".$occurrence->event->location;
    }            
  }
  $entity_id = $occurrence->id;
  $entity = $occurrence->entity;

  $end=clone($occurrence->date);
  $end->addSecond($occurrence->event->duration);

  $style = "color:rgb(".$cats_color[$occurrence->event->category1].")";  
  if($occurrence->event->all_day) {
    $schedule = "<h1 class='RF hoursTitle'>$l_all_day</h1>"; 
  } else {
    if($occurrence->date->compareDateIso($date) != 0) {
      $schedule_start = $occurrence->date->get(Of_Date::DAY).'/'.$occurrence->date->get(Of_Date::MONTH);
    } else {
      $schedule_start = $occurrence->date->get(Of_Date::TIME_SHORT);
    }
    if($end->compareDateIso($date) != 0) {
      $schedule_end = $end->get(Of_Date::DAY).'/'.$end->get(Of_Date::MONTH);
    } else {
      $schedule_end = $end->get(Of_Date::TIME_SHORT);
    }
    $schedule = "
      <h1 class='RF hoursTitle'>$schedule_start<br/>$schedule_end</h1>  
";
  }


  $event_data = "
    <li style='$style' class='RC'>
    $schedule
    <div>
      <a href=\"$path/calendar/calendar_index.php?action=detailconsult&amp;calendar_id=".$occurrence->event->id."\">$title</a>
    </div>
    </li>";

  return $event_data;  
}


///////////////////////////////////////////////////////////////////////////////
// display conflicts
// parameters:
//   - $calendar : calendar hash
//   - $c_q    : conflict list (events)
///////////////////////////////////////////////////////////////////////////////
function html_calendar_dis_conflict($calendar, $conflicts) {
  global $l_title, $l_location, $l_conflicts, $l_datebegin,$l_dateend, $l_user;
  global $l_force_insert,$l_refuse_insert,$l_cancel_insert,$l_validate;
  global $l_private, $obm;

  $readable['user'] = array_keys(OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'read'));
  $readable['resource'] = array_keys(OBM_Acl::getAllowedEntities($obm['uid'], 'resource', 'read'));

  $datebegin = clone $calendar["date_begin"];
  $dateend = clone $calendar["date_end"];
  $h = $dateend->diffHour($datebegin);
  $m = $dateend->diffMinute($datebegin);
  $time_begin = $datebegin->getHour();
  $min_begin = $datebegin->getMinute();
  $duration = $h+$m/60;
  $d = $datebegin->getOutputDate();

  $entity_id = $calendar['entity_id'];
  $entity_kind = $calendar['entity_kind'];
  $sel_ent = "data-$entity_kind-$entity_id";
  $entity_info = get_entity_info($entity_id, $entity_kind);

  foreach($conflicts as $conflict) {
    $private = true;
    $id = $conflict->event->id;
    $name  = $conflict->event->attendee[$conflict->entity][$conflict->id];
    if (((in_array($conflict->id, $readable[$conflict->entity]) || in_array($conflict->event->owner,$readable['user']))
      && $conflict->event->privacy != '1') || $conflict->event->owner == $obm['uid'] || ($conflict->id == $obm['uid'] && $conflict->entity == 'user')) {
        $private = false;
      }
    if (!$private) {
      $title = $conflict->event->title;
      $location = $conflict->event->location;
    } else {
      $title = $l_private;
      $location = $l_private;
    }

    $all_day = $conflict->event->all_day;
    $begin = $conflict->date;
    $end = $conflict->end;

    if ($all_day) {
      $dis_all_day = "<tr><td colspan=\"2\" class=\"detailLabel\">$l_all_day</td></tr>";
      $begin = $begin->getOutputDate();
      $end = $end->getOutputDate();
    } else {
      $begin = $begin->getOutputDateTime();
      $end = $end->getOutputDateTime();
    }
    $entity = $conflict->entity;
    $dis_conflict .= "
      <tr>
        <td>$title</td>
        <td>$location</td>
        <td>$begin</td>
        <td>$end</td>
        <td>$name <div style=\"display:none;\" class=\"elementRow\" id=\"sel_user_id-$sel_ent\">$entity_info[label]</div></td>
      </tr>";
  }
  
  if (is_array($calendar["user_id"])) {
    foreach($calendar["user_id"] as $user) {
      $sel_user_id .="<input type=\"hidden\" name=\"sel_user_id[]\" value=\"$user\" />";
    }
  }
  if (is_array($calendar["resource_id"])) {
    foreach($calendar["resource_id"] as $res) {
      $sel_res_id .="<input type=\"hidden\" name=\"sel_resource_id[]\" value=\"$res\" />";
    }
  }
  $block = "
  <div class=\"detail extra\">
  <h1>$l_conflicts</h1>
  <h2><a href=\"#\" onclick=\"displayFreeBusyConflict('$duration', '$d')\">$GLOBALS[l_display_entites_freebusy]</a></h2>
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
   <input type='hidden' id='sel_time_begin' value='$time_begin' />
   <input type='hidden' id='sel_min_begin' value='$min_begin' />
  </div>
  <div id=\"fbcContainer\"></div>
  </div>";

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
  
  $event_id = $calendar['calendar_id']; 
  $user_id = $calendar['user_id'];
  $dis_form = "
  <form name=\"f_conflict\" method=\"post\">
   <input type=\"hidden\" name=\"calendar_id\" value=\"$event_id\" />
   <input type=\"hidden\" name=\"action\" value=\"decision\" />
   <input type=\"hidden\" name=\"user_id\" value=\"$user_id\" />
   <input type=\"hidden\" name=\"cba_force\" value=\"1\" />
   <input type=\"hidden\" name=\"rd_decision_event\" value=\"\" />
   <div class=\"detailButton\">
    <input type=\"submit\" value=\"$l_force_insert\" onclick=\"this.form.rd_decision_event.value='ACCEPTED';\"/>
    <input type=\"submit\" value=\"$l_refuse_insert\" onclick=\"this.form.rd_decision_event.value='DECLINED';\"/> 
    <input type=\"submit\" value=\"$l_cancel_insert\" onclick=\"this.form.rd_decision_event.value='NEEDS-ACTION';\"/>   
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
      $alerts = get_one_event_user_alert($id);
      $display['detailInfo'] = display_record_info($eve_q);
      $block = html_calendar_event_consult($eve_q, $exception_q, $entities, $alerts);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML : Display Calendar Event Consult
// Parameters:
//   - $event_q  : event_query result
//   - $entities : entities linked to the event ['user'], ['resource']
//   - $alerts   : alerts array
///////////////////////////////////////////////////////////////////////////////
function html_calendar_event_consult($event_q, $exception_q, $entities, $alerts) {
  global $obm, $display, $cgp_show, $ccalendar_weekstart, $ccalendar_resource;
  global $l_users, $l_resources, $l_title, $l_location, $l_priority,$l_private;
  global $l_low,$l_medium,$l_datebegin,$l_day,$l_month,$l_week,$l_desc,$l_high;
  global $l_dateend,$l_insert,$l_update,$l_repeat, $l_alerts, $l_state, $l_user;
  global $l_change_state,$l_groups,$l_all_day,$l_every,$l_owner;
  global $l_repeatkind,$l_repeatdays,$l_date_repeatend, $l_none, $c_none; 
  global $l_wait, $l_exception, $l_yes, $l_no, $path;
  global $l_carac,$l_module_calendar, $l_items, $ccalendar_alerts;
  global $l_daysofweekshort,$l_update,$l_accept,$l_refuse,$l_repeatfrequency;
  global $l_A, $l_R, $l_W;

  $id = $event_q->f('event_id');
  
  if ($event_q->f('event_privacy') == 0 || 
      $event_q->f('event_owner') == $obm['uid'] ||
      in_array($obm['uid'],$entities['user']['ids'])) {

    $title = $event_q->f('event_title');
    $location = $event_q->f('event_location');
    $cats1 = of_category_get_entitycategories('event', 'category1', $id, 'mono');
    $owner = $event_q->f('event_owner');
    $owner_lastname = $event_q->f('owner_lastname');
    $owner_firstname = $event_q->f('owner_firstname');
    $priority = $event_q->f('event_priority'); 
    $properties = $event_q->f('event_properties');
    $description = nl2br($event_q->f('event_description'));
    if ($properties != '') {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset'?/>".$properties;
    } else {
      $item = '';
    }

    $block_category1 = of_category_dis_block_consult('event', 'category1', $cats1, 'mono');
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
    $display['title'] = $title;
  } else {
    $title = $l_private; 
    $location = $l_private;
    $priority = $l_private;
    $description = $l_private;
    $priority = $l_private;
    $display['title'] = $l_private;

  }
  $all_day = $event_q->f('event_allday');
  $begin = new Of_Date($event_q->f('event_date'), 'GMT');
  $end = clone $begin;
  $end->addSecond($event_q->f('event_duration'));
  if ($all_day) {
    $dis_all_day = "<tr><th>$l_all_day</th><td></td></tr>";
    $end->subSecond(1);
    $begin = $begin->getOutputDate();
    $end = $end->getOutputDate();
  } else {
    $begin = $begin->getOutputDateTime();
    $end = $end->getOutputDateTime();
  }     
  $privacy = $event_q->f('event_privacy') == 1? $l_yes : $l_no;


  if ($event_q->f('event_repeatkind') != 'none') {
    $kind = $GLOBALS['l_'.$event_q->f('event_repeatkind')];
    $kind_unit = $GLOBALS['l_'.$event_q->f('event_repeatkind').'_unit'];
    $repeat_days = $event_q->f('event_repeatdays'); 
    if ($event_q->f('event_endrepeat') != '') {
      $repeat_end = new Of_Date($event_q->f('event_endrepeat'), 'GMT');
      $repeat_end = $repeat_end->getOutputDate();
    } else {
      $repeat_end = '';
    }
    if( $event_q->f('event_repeatfrequence') == 1 ) {
      $repeatfrequency = sprintf($kind_unit,'');
    } else {
      $repeatfrequency = sprintf($kind_unit,$event_q->f('event_repeatfrequence'));
    }

    if($event_q->f('event_repeatkind') == 'weekly') {
      $date = new Of_Date();
      $days = str_split($repeat_days);
      foreach($days as $i => $repeat) {
        if($repeat == 1) $dis_repeat_days .= $date->setWeekday($i)->localize(Of_Date::WEEKDAY_NAME).' ';
      }
      $dis_repeat_days = "
      <tr>
       <th>$l_repeatdays :</th>
       <td>$dis_repeat_days</td>
      </tr>";
    }

    // Exception
    $block_exception_detail = '';
    while($exception_q->next_record()) {
      $date = new Of_Date($exception_q->f('eventexception_date'), 'GMT');
      $block_exception_detail .= $date->getOutputDate()."<br />";
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

  $attendee = $entities['user'];
  if (is_array($attendee['entity'])) {
    foreach ($attendee['entity'] as $u_id => $entity) {
      $label = $entity['label'];
      $state = $entity['state'];
      $ACCEPTED = '';
      ${'NEEDS-ACTION'} = '';
      $DECLINED = '';
      if (OBM_Acl::canWrite($obm['uid'], 'calendar', $u_id)) {
        $$state = "checked=\"checked\"";
        $urld = "$path/calendar/calendar_index.php?action=update_decision&calendar_id=$id&entity_kind=user&entity_id=$u_id";
        $urla = "$path/calendar/calendar_index.php?action=update_alert&calendar_id=$id&user_id=$u_id";
        $sel_alert = '';
        foreach ($ccalendar_alerts as $alert_sec => $alert_label) {
          if($alerts[$u_id] == $alert_sec) {
            $sel_alert .= "<option value=\"$alert_sec\" selected=\"selected\">$alert_label</option>";
          } else {
            $sel_alert .= "<option value=\"$alert_sec\">$alert_label</option>";
          }
        }
        $sel_alert = "<select name=\"sel_alert\" onchange=\"window.location='$urla&sel_alert='+this.options[this.selectedIndex].value\"><option value=\"$c_none\">$l_none</option>$sel_alert</select>";        
        $dis_user_list .= "
        <tr>
        <td>$label</td>
        <td>$sel_alert</td>
        <td class=\"C\">
        <input $ACCEPTED type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /><span>$l_A</span>
        <input ${'NEEDS-ACTION'} type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /><span>$l_W</span>
        <input $DECLINED type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /><span>$l_R</span>
        </td>
        </tr>";
        $$state = '';
      } else {
        $ACCEPTED = 'class="disabled"';
        ${'NEEDS-ACTION'} = 'class="disabled"';
        $DECLINED = 'class="disabled"';
        $$state = '';
        $dis_user_list .= "<tr><td>$label</td><td></td><td class=\"C\">
        <input style='visibility:hidden;' disabled='disabled' type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /><span $ACCEPTED>$l_A</span>
  <input style='visibility:hidden;' disabled='disabled' type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /><span ${'NEEDS-ACTION'}>$l_W</span>
        <input style='visibility:hidden;' disabled='disabled' type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /><span $DECLINED>$l_R</span>
          </td></tr>";
      }
    }
  }

  $attendee = $entities['contact'];
  if (is_array($attendee['entity'])) {
    foreach ($attendee['entity'] as $c_id => $entity) {
      $label = $entity['label'];
      $state = $entity['state'];
      $ACCEPTED = '';
      ${'NEEDS-ACTION'} = '';
      $DECLINED = '';
        $$state = "checked=\"checked\"";
        $urld = "$path/calendar/calendar_index.php?action=update_ext_decision&calendar_id=$id&entity_kind=contact&entity_id=$c_id";

        $dis_contact_list .= "
        <tr>
        <td>$label</td>
        <td class=\"C\">
        <input $ACCEPTED type=\"radio\" name=\"rd_contact_decision_$c_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /><span>$l_A</span>
  <input ${'NEEDS-ACTION'} type=\"radio\" name=\"rd_contact_decision_$c_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /><span>$l_W</span>
        <input $DECLINED type=\"radio\" name=\"rd_contact_decision_$c_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /><span>$l_R</span>
        </td>
        </tr>";
        $$state = '';
    }
  }
  if ($dis_contact_list != "") {
   $dis_contacts_block ="  <div class=\"detail infos\">
    <h1>$GLOBALS[l_contacts]</h1>
    <table  class=\"spreadSheet\">
    <thead>
    <tr>
    <td>$GLOBALS[l_contacts]</td><td>$GLOBALS[l_state]</td>
    </tr>
    </thead>
    $dis_contact_list
    </table>
    </div>";
  }

  if ($ccalendar_resource) {

    // item
    if ($item != '') {
      $xml = parse_item($item);
      if (is_object($xml)) {
        foreach($xml->resource as $key => $value) {
          if ($value->property_name) {
            $property["$value->res_id"] = "($value->property_name : $value->property_value)";
          } else {
            $property["$value->res_id"] = '';
          }
          if (!empty($value->item)) {
            $dis_item_list .= "
            <tr>
              <th>$value->res_name :</th>
              <td>".nl2br($value->item)."</td>
            </tr>";
          }
        }
        $dis_item_list = "<div class=\"detail infos\">
        <h1>$l_items</h1>
        <table>
          $dis_item_list
        </table>
        </div>";        
      }
    }

    $res = $entities['resource'];
    if (is_array($res['entity'])) {
      foreach ($res['entity'] as $r_id => $entity) {
        $label = $entity['label'];
        $state = $entity['state'];
        if (OBM_Acl::canWrite($obm['uid'], 'resource', $r_id)) {
          $$state = "checked=\"checked\"";
          $url= "$path/calendar/calendar_index.php?action=update_decision&calendar_id=$id&entity_kind=resource&entity_id=$r_id";
          $dis_resource_list .= "
          <tr>
          <td>$label $property[$r_id]</td>
          <td class=\"C\">
          <input $ACCEPTED type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"ACCEPTED\" onclick=\"window.location='$url&rd_decision_event=ACCEPTED'\" />$l_A
          <input ${'NEEDS-ACTION'} type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$url&rd_decision_event=NEEDS-ACTION'\" />$l_W
          <input $DECLINED type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"DECLINED\" onclick=\"window.location='$url&rd_decision_event=DECLINED'\" />$l_R
          </td>
          </tr>
        ";
          $$state = '';
        } else {
          $ACCEPTED = 'class="disabled"';
          ${'NEEDS-ACTION'} = 'class="disabled"';
          $DECLINED = 'class="disabled"';
          $$state = '';
          $dis_resource_list .= "<tr><td>$label</td><td class=\"C\">
          <input style='visibility:hidden;' disabled='disabled' type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /><span $ACCEPTED>$l_A</span>
          <input style='visibility:hidden;' disabled='disabled' type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /><span ${'NEEDS-ACTION'}>$l_W</span>
          <input style='visibility:hidden;' disabled='disabled' type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /><span $DECLINED>$l_R</span>
            </td></tr>";          
        }
      }
      $dis_resource_block = "
        <div class=\"detail infos\">
        <h1>$l_resources</h1>
        <table class=\"spreadSheet\">
        <thead>
        <tr>
        <td>$l_resources</td><td>$l_state</td>
        </tr>
        </thead>
        $dis_resource_list
        </table>
        </div>";
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
    <th>$l_owner :</th>
    <td>$owner_lastname $owner_firstname</td>
  </tr>
  <tr>
    <th>$l_location :</th>
    <td>$location</td>
  </tr>

  $block_category1
  </table>
  </div>

  <div class=\"detail infos\">
  <h1>$l_carac</h1>
  <table>
     <tr>
      <th>$l_private :</th>
      <td>$privacy</td>
    </tr>
   $dis_all_day
    <tr>
     <th>$l_datebegin :</th>
     <td>$begin</td>
    </tr>
    <tr>
     <th>$l_dateend :</th>
     <td>$end</td>
    </tr>
    <tr>
     <th>$l_priority :</th>
     <td>$priority</td>
    </tr>
   </table>
   </div>
  $repeat_block
  <div class=\"detail infos\">
  <h1>$l_users</h1>
  <table  class=\"spreadSheet\">
  <thead>
  <tr>
  <td>$l_users</td><td>$l_alerts</td><td>$l_state</td>
  </tr>
  </thead>
  $dis_user_list
  </table>
  </div>

  <p class=\"LC\" />
  $dis_contacts_block
  $dis_resource_block
  $dis_item_list

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
//   - $entities : entities linked (event) or selected ['user'], ['resource']
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_event_form($action, $calendar, $event_q, $entities) {
  global $obm;

  if (($action == 'detailupdate') || ($action == 'duplicate') || ($action == 'conflict_manager') ) {
    $users = $entities['user'];
    $resources = $entities['resource'];
    $contacts = $entities['contact'];
    $exception_q = run_query_get_one_event_exception($calendar['calendar_id']);
    $alerts = get_one_event_user_alert($calendar['calendar_id'], $obm['uid']);
  } else {
    $users_id = $entities['user'];
    $contacts_id = $entities['contact'];
    $res_id = $entities['resource'];
    // If new event, add the optionnal new users (here to get the user's infos)
    if ($action == 'new') {
      if (is_array($calendar['new_user_id'])) {
	      foreach ($calendar['new_user_id'] as $new_user_id) {
	        if ($new_user_id > 0) {
	          $users_id[] = $new_user_id;
	        }
	      }
      }
      if (!isset($calendar['new_meeting'])) { 
       $users_id[] = $obm['uid'];
      }
      if (is_array($calendar['sel_contact_id'])) {
	      foreach ($calendar['sel_contact_id'] as $new_contact_id) {
	        if ($new_contact_id > 0) {
	          $contacts_id[] = $new_contact_id;
	        }
	      }
      }
      if (is_array($calendar['new_resource_id'])) {
	      foreach ($calendar['new_resource_id'] as $new_resource_id) {
	        if ($new_resource_id > 0) {
	          $res_id[] = $new_resource_id;
	        }
	      }
      }
    }
    if (count($users_id > 0)) {
      $users = get_userobm_from_ids($users_id);
    } else {
      $users = array();
    }
    if (is_array($entities['contact'])) $contacts_id = array_merge($contacts_id, $entities['contact']);
    if (count($contacts_id > 0)) {
      $contacts = get_contact_from_ids($contacts_id);
    } else {
      $contacts = array();
    }
    if (is_array($entities['resource'])) $res_id = array_merge($res_id, $entities['resource']);
    if (count($res_id > 0)) {
      $resources = get_calendar_resource_from_ids($res_id);
    } else {
      $resources = array();
    }
    $groups_id = $entities['group'];
    if (count($groups_id > 0)) {
      $groups = get_calendar_group_from_ids($groups_id);
    } else {
      $groups = array();
    }
  }
  $possible_owners = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
  );
  $possible_owners = $possible_owners['entity'];
  $cats1 = of_category_get_ordered('event', 'category1');
  $block = html_calendar_event_form($action, $calendar, $event_q, $exception_q, $possible_owners, $users, $contacts, $resources, $groups, $cats1, $alerts);

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
function dis_calendar_quick_form($calendar,$calendar_entity) {
  global $obm,$ico_popup_close,$popup_queue,$l_validate, $l_edit, $l_edit_full;
  global $l_confirm_delete,$l_c_delete, $l_title, $l_category1, $l_desc;
  global $l_location, $l_owner, $l_members, $l_users, $l_carac, $l_items, $l_details;

  $u = get_user_info(); 
  $entity_view.="<select id=\"calendarViewEntity\">";
  if(is_array($calendar_entity['user'])){
    foreach($calendar_entity['user'] as $user){
      if(OBM_Acl::canAccess($obm['uid'], 'calendar',array($user['id']))) {
        $entity_view.="<option value=\"$user[id]\" />$user[name]</option>";
        if($obm['uid'] == $user["id"]) {
          $uid = true;
        }
      }
    }
  }

  if(!isset($uid)) {
    $entity_view.="<option value=\"$obm[uid]\" />$u[firstname] $u[lastname]</option>";
  }

  $entity_view.="</select>";
  $entity_view.="<input id=\"calendarKindEntity\" type=\"hidden\" value=\"user\" />";
  
  $block = "
    <div id=\"calendarQuickForm\" class=\"obmPopup\">
      <h1>
      <a href=\"\" class=\"closeSticky\" onclick=\"return false;\">
      <img src=\"$ico_popup_close\" alt=\"close\" />
      </a>
      <span id=\"calendarQuickFormDate\"></span>
      </h1>
      <h2>
      <a id=\"calendarQuickFormTitle\" href=\"\" onclick=\"obm.calendarQuickForm.goTo('detailconsult'); return false\"></a>
      </h2> 
     <div class=\"content\">
        <form id=\"calendarQuickFormStore\" onsubmit=\"obm.calendarQuickForm.submit();return false;\">
          <fieldset>
            $l_title : <input type=\"text\" name=\"tf_title\" /> 
          </fieldset>
          <fieldset id=\"calendarListEntity\">
            $l_users : $entity_view
          </fieldset>
          <fieldset class=\"buttons\"> 
            <input class=\"button closeSticky\" type=\"submit\" value=\"$l_validate\" />
            <input type=\"button\" id=\"calendarQuickFormEdit\" onclick=\"obm.calendarQuickForm.goTo();\" />
            <input type=\"button\" id=\"calendarQuickFormDetail\" onclick=\"obm.calendarQuickForm.goTo('detailconsult');\" value=\"$l_details\" />
            <input type=\"button\" class=\"closeSticky\" id=\"calendarQuickFormDelete\" 
              onclick=\"if(confirm('".phpStringToJsString($l_confirm_delete)."')) obm.calendarQuickForm.submit('quick_delete');\"
          value=\"$l_c_delete\" />
          </fieldset>
        </form>
        <div id=\"calendarQuickFormData\">
          <div id=\"calendarQuickFormAttendees\">
            <h3>$l_members</h3>
          </div>
          <div>
            <h3>$l_carac</h3>
            <table>
            <tr>
            <th>$l_owner</th>
            <td id=\"calendarQuickFormOwner\"></td>
            </tr>
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
          <div id=\"calendarQuickFormItem\">
            <h3>$l_items</h3>
          </div>
      </div>
    </div>
    </div>
    <script type=\"text/javascript\">
      obm.calendarQuickForm = new Obm.CalendarQuickForm();
      obm.vars.labels.edit = '".phpStringToJsString($l_edit)."';
      obm.vars.labels.edit_full = '".phpStringToJsString($l_edit_full)."';
    </script>
";

  return $block;
}


/**
 * HTML for the conflict manager popup in the calendar view.
 *  
 * @access public
 * @return HTML
 */
function dis_calendar_conflict_popup() {

  $block = "
    <div id=\"calendarConflictPopup\" class=\"obmPopup\">
      <h1>
        <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
          <img src=\"$GLOBALS[ico_popup_close]\" alt=\"close\" />
        </a>
        <span>$GLOBALS[l_conflicts]</span>
      </h1>
      <div class=\"content\">
        <h2>$GLOBALS[l_conflict_message]</h2>
         <fieldset class=\"buttons\"> 
          <input type=\"button\" id=\"popup_manage\" class=\"closeSticky\" value=\"$GLOBALS[l_conflict_manage]\" /><br/>
          <input type=\"button\" id=\"popup_force\" value=\"$GLOBALS[l_conflict_force]\" /><br/>
          <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$GLOBALS[l_cancel]\" />
         </fieldset>
      </div>
   </div>

  <div id=\"calendarSendMail\" class=\"obmPopup\">
     <h1>
       <a href=\"#\" id=\"popup_sendmail_close\" class=\"closeSticky\">
         <img src=\"$GLOBALS[ico_popup_close]\" alt=\"close\" />
       </a>
       <span>$GLOBALS[l_confirm]</span>
     </h1>
     <div class=\"content\">
        <h2>$GLOBALS[l_conflict_sendmail]</h2>
         <fieldset class=\"buttons\"> 
          <input type=\"button\" id=\"popup_sendmail_yes\" class=\"closeSticky\" value=\"$GLOBALS[l_yes]\" />
          <input type=\"button\" id=\"popup_sendmail_no\" class=\"closeSticky\" value=\"$GLOBALS[l_no]\" />
        </fieldset>
     </div>
   </div>  
    <script type=\"text/javascript\">
      obm.vars.labels.conflict_force = '".phpStringToJsString($GLOBALS['l_conflict_force'])."';
      obm.vars.labels.insert_force = '".phpStringToJsString($GLOBALS['l_force'])."';
    </script>";

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
function html_calendar_event_form($action, $calendar, $event_q, $exception_q, $possible_owners, $users, $contacts, $res, $groups, $cats1, $alerts) {
  global $obm, $path, $display, $cgp_show, $ccalendar_alerts;
  global $ccalendar_resource, $ccalendar_weekstart, $ccalendar_hour_fraction;
  global $ico_mini_cal, $ico_add,$ico_delete, $ico_project, $l_header_new;
  global $ccalendar_first_hour, $ccalendar_last_hour;
  // -- Labels
  global $l_users, $l_attendees, $l_title, $l_location, $l_priority, $l_private, $l_desc;
  global $l_items, $l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_insert,$l_update,$l_repeat, $l_owner;
  global $l_repeatkind,$l_repeatdays2, $l_date_repeatend, $l_daily, $l_weekly;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_module_calendar;
  global $l_none,$l_force,$l_repeat_update,$l_groups,$l_force_disabled;
  global $l_calendar_select_group, $l_hour,$l_all_day,$l_repeatfrequency;
  global $l_resources,$l_daysofweekshort, $l_exception, $l_items;
  global $l_alerts, $l_alerts_time, $l_color ;

  $repeatfrequency = 1;
  $uid = $obm['uid']; 

  if (($action == 'detailupdate') || ($action == 'duplicate') || ($action == 'conflict_manager') ) {
    $title = htmlspecialchars($event_q->f('event_title'));
    $location = htmlspecialchars($event_q->f('event_location'));
    $cat1 = $event_q->f('event_category1_id');
    $owner = $event_q->f('event_owner');
    $owner_lastname = $event_q->f('owner_lastname');
    $owner_firstname = $event_q->f('owner_firstname');
    $priority = $event_q->f('event_priority');
    $properties = $event_q->f('event_properties');
    $desc = $event_q->f('event_description');
    if ($properties != '') {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset'?>$properties";
    } else {
      $item = '';
    }
    if ($action == 'conflict_manager') {
      $begin = $calendar['date_begin'];
      $end = clone $begin;
      $end = $end->addSecond($calendar['duration']);
    } else {
      $begin = new Of_Date($event_q->f('event_date'), 'GMT');
      $end = clone $begin;
      $end = $end->addSecond($event_q->f('event_duration'));
    }
    $all_day = $event_q->f('event_allday');
    if($all_day) {
      $end->subMinute(1);
    }
    $color = $event_q->f('event_color');
    $privacy = $event_q->f('event_privacy');
    $repeat_kind = $event_q->f('event_repeatkind');
    $repeat_days = $event_q->f('event_repeatdays');
    $repeatfrequency = $event_q->f('event_repeatfrequence');
    if ($repeat_kind != 'none') {
      $repeat_end = new Of_Date($event_q->f('event_endrepeat'), 'GMT');
    }
    $id = $event_q->f('event_id');

    while($exception_q->next_record()) {
      $date_exception[] = new Of_Date($exception_q->f('eventexception_date'), 'GMT'); 
    }
    // Alerts
    if (is_array($alerts) && isset($alerts[$owner])) {
      $selected_alert = $alerts[$owner];
    }
    $dis_title = $title;

    if ($action == 'duplicate') {
      $dis_title = "$title ($l_header_new)";
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($calendar['calendar_id'])) { $id = $calendar['calendar_id']; }
  if (isset($calendar['title'])) { $title = stripslashes($calendar['title']); }
  if (isset($calendar['location'])) { $location = stripslashes($calendar['location']); }
  if (isset($calendar['category1'])) { $cat1 = $calendar['category1']; }
  if (isset($calendar['owner'])) {$owner = $calendar['owner'];} elseif(!$owner) {$owner = $uid;}
  if (isset($calendar['priority'])) { $priority = $calendar['priority']; }
  if (isset($calendar['description'])) { $desc = stripslashes($calendar['description']); }
  if (isset($calendar['all_day'])) { $all_day = $calendar['all_day']; }
  if (isset($calendar['color'])) { $color = $calendar['color']; }
  if (isset($calendar['repeatfrequency'])) { $repeatfrequency = $calendar['repeatfrequency']; }  
  if (isset($calendar['item'])) { $item = $calendar['item']; } 
  if (!is_object($event_q)) {$begin = $calendar['date_begin']; }
  if (!isset($begin)) {$begin = new Of_Date(); $begin->setHour($ccalendar_first_hour)->setMinute(0)->setSecond(0);}
  if (!is_object($event_q)) {$end = $calendar['date_end']; }
  if (!isset($end)) {$end = clone $begin; $end->setHour($ccalendar_first_hour + 1);}
  if (isset($calendar['privacy'])) { $privacy = $calendar['privacy']; }
  if (isset($calendar['repeat_kind'])) { $repeat_kind = $calendar['repeat_kind']; }
  if (isset($calendar['repeat_days']) && $calendar['repeat_days'] != '000000') { $repeat_days = $calendar['repeat_days'];}
  if (isset($calendar['repeat_end'])) $repeat_end = $calendar['repeat_end'];
  if(!isset($repeat_end)) {$repeat_end = new Of_Date();}
  if (isset($calendar['force'])) { $force = $calendar['force']; }
  if (isset($calendar['force_disabled'])) { $force_disabled = $calendar['force_disabled']; }
  if ($repeat_kind != 'weekly') {
    $repeat_days = '000000';
    $repeatDaysDisplay = 'none';
  } else {
    $repeatDaysDisplay = "''";
  }
  if (isset($repeat_kind) && $repeat_kind != 'none') {
    $repeatDisplay = "''";
    $frequency_block = sprintf($GLOBALS['l_'.$repeat_kind.'_unit'], "<input type=\"text\" size=\"4\" style=\"width:3em;\" name=\"tf_repeatfrequency\" value=\"$repeatfrequency\" />");
  } else {
    $repeatDisplay = 'none';
    $frequency_block = "<input type=\"text\" size=\"4\" style=\"width:3em;\" id=\"tf_repeatfrequency\" name=\"tf_repeatfrequency\" value=\"$repeatfrequency\" />";
  }
  if (isset($calendar['date_exception'])) { $date_exception = $calendar['date_exception']; }
  if (isset($calendar['list-item'])) { $t_list_item = $calendar['list-item']; }
  if (isset($calendar['list-item-id'])) { $t_list_item_id = $calendar['list-item-id']; }
  if (isset($calendar['property-name'])) { $t_property_name = $calendar['property-name']; }
  if (isset($calendar['property'])) { $t_property_value = $calendar['property']; }
  if (isset($calendar['others_attendees'])) { $others_attendees = $calendar['others_attendees']; }
  if (isset($calendar['add_displayed_users'])) { $add_displayed_users = $calendar['add_displayed_users']; }
  if (isset($calendar['show_attendees_calendar'])) { $show_attendees_calendar = $calendar['show_attendees_calendar']; }
  if (isset($calendar['attendees_notification'])) { $attendees_notification = $calendar['attendees_notification']; }
  if (isset($calendar['alert'])) {
    // handle only one entry for now
    if (is_array($sel_alert)) {
      $selected_alert = $calendar['alert'][0];
    } else {
      $selected_alert = $calendar['alert'];
    }
  }
  

  if (($action == 'detailupdate') || ($action == 'update') || ($action == 'conflict_manager')) {
    if ($attendees_notification) {
      $attendees_notification = "checked=\"checked\"";
    }

    $dis_attendees_notification = "
  <fieldset class=\"detail extra\">    
  <legend>$GLOBALS[l_event_update_notification]</legend>  
  <table>    
  <tr>    
    <th>$GLOBALS[l_sendamail]</th> 
    <td>
      <input style=\"margin:0\" type=\"checkbox\" name=\"cba_attendees_notification\" value=\"1\" $attendees_notification />
    </td>
  </tr>
  </table>
  </fieldset>";

    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"calendar_id\" id=\"calendar_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";
  } elseif (($action == 'new') || ($action == 'insert') || ($action == 'duplicate')) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $$repeat_kind .= "selected=\"selected\" ";
  $dis_sel_kind .= "
    <select onchange=\"repeat_form($(this).get('inputValue'))\" name=\"sel_repeat_kind\">
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
      $tag_low = '';
      $tag_medium = "selected=\"selected\"";
      $tag_high = ''; 
    break;
  } 
  $dis_sel_prio .= "
    <select name=\"sel_priority\">
      <option value=\"1\" $tag_low>$l_low</option>
      <option value=\"2\" $tag_medium>$l_medium</option>
      <option value=\"3\" $tag_high>$l_high</option>
    </select>";

  // eventcategory select
  $block_category1 = of_category_dis_entity_form('event', 'category1', $cats1, 'mono', $cat1);

 // Repetition days
  $week = new Of_Date($ccalendar_weekstart);
  for ($i=0; $i<7; $i++) {
    $day = $week->localize(Of_Date::WEEKDAY_NARROW) ;
    $j = $week->getWeekday();
    if (strcmp(substr($repeat_days,$j,1),'1') ==0) {
      $dis_repeat_day .= "<span class=\"NW\"><input type=\"checkbox\" name=\"cba_repeatday_".$j."\" value=\"1\" checked = \"checked\"/>$day</span> ";
    } else {
      $dis_repeat_day .= "<span class=\"NW\"><input type=\"checkbox\" name=\"cba_repeatday_".$j."\" value=\"1\" />$day</span> ";
    }
    $week->addDay(1);
  }

  if ( $all_day ) {
    $style = 'display:none;';
    $all_day_checked = "checked=\"checked\"";
  }

  if ($action == "new" || $action == "insert") {
    if ( $show_attendees_calendar ) {
      $show_attendees_calendar_checked = "checked=\"checked\"";
    }
    $dis_show_attendees_calendar = "<tr>
     <th>$GLOBALS[l_show_attendees_calendar]</th>
      <td>
       <input type=\"checkbox\" id=\"cba_show_attendees_calendar\" value=\"1\" $show_attendees_calendar_checked name=\"cba_show_attendees_calendar\" />
       </td>
      </tr>";
  }

  if ($action == 'new' && !isset($calendar['new_meeting'])) {
    // If one new user is given in parameters, pre-select it
    if (is_array($calendar['new_user_id']) && count($calendar['new_user_id']) == 1) {
      $u_id = $calendar['new_user_id'][0];
    } else if (is_array($calendar['entity']['user']) && count($calendar['entity']['user'])==1) {
      // Else if calendar displayed was only one user, pre-select him
      $u_id = $calendar['entity']['user'][0];
    } else {
      // Else auto-select the connected user
      $u_id = "$uid";
    }
    $u_name = ($users['entity'][$u_id]['label']);
    $sel_id = "data-user-$u_id";
    $div_id = "sel_user_id-$sel_id";
    $dis_sel_user = "<div class=\"elementRow\" id=\"$div_id\">
      <a href=\"javascript: remove_element('$div_id','sel_user_id');\">
      <img src=\"$ico_delete\" alt=\"[Delete]\" />
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
  } else {
    // user select
    if (is_array($users['entity'])) {
      foreach ($users['entity'] as $u_id => $u_ent) {
        $u_name = $u_ent['label'];
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
  }

  if (sizeof($users['entity']) > 1) {
    $u = array();
    foreach($users['entity'] as $id => $user) {
      $name = phpStringToJsString($user["label"]);
      array_push($u, "'$id' : '$name'");
    }
    $users_js = "{".implode(",", $u)."}";

    if ( $add_displayed_users ) {
      $add_displayed_users_checked = "checked=\"checked\"";
    }
    $block_add_displayed_users = "<tr>
      <th>
      $GLOBALS[l_add_displayed_users]
      </th>
      <td>
        <input type=\"checkbox\" id=\"cba_add_displayed_users\" value=\"1\" $add_displayed_users_checked name=\"cba_add_displayed_users\" onclick=\"add_displayed_users(this.checked, $users_js, $uid)\"/>
      </td>
      </tr>";
  }

  if (is_array($contacts['entity'])) {
    foreach ($contacts['entity'] as $u_id => $u_ent) {
      $u_name = $u_ent['label'];
      $sel_id = "data-contact-$u_id";
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

  $url = "$path/people/people_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id&amp;filter_entity=calendar&amp;filter_pattern=access";
  $user_add_button = "
    <a href=\"javascript: return false;\" 
      onclick=\"popup('$url','');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";
      
  $block_owner = "
    <th>
    $l_owner
    </th>
    <td>
    <select name=\"sel_owner\">";  
  foreach ($possible_owners as $o) {
    $block_owner .= "
      <option value=\"$o[id]\" ".($o['id']==$owner ? "selected=\"selected\"" : "").">$o[label]</option>";
  }
  $block_owner .= "
    </select>
    </td>";
  
  $dis_others_attendees = dis_calendar_mail_field($others_attendees, $class);
  if ($has_access_to_other_calendars = OBM_Acl::hasAllowedEntities($obm['uid'], 'calendar', 'access')) {
    $block_user = "
      <fieldset class=\"detail infos\">
      <legend>$l_attendees</legend>    
      <table>
      $block_add_displayed_users
      <tr>
      <th>
      $l_users / $GLOBALS[l_contacts]
      $user_add_button
      </th>
      <td>
        <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" />
      </td>
      </tr>
      <tr>
      <th>&nbsp;</th>    
      <td id=\"sel_user_id\">$dis_sel_user</td>
      </tr>
      <tr>
      $dis_others_attendees
      </tr>
      $dis_show_attendees_calendar
      </table>
      </fieldset>
      <p class=\"LC\" />";
      $user_complete = "new obm.AutoComplete.Search('$path/people/people_index.php?action=ext_search', 'sel_user_id', 'userSearch', {filter_entity:'calendar',filter_pattern:'access'});";
  } else {
    $block_user = "<fieldset class=\"detail infos H\"><input type='hidden' name='sel_user_id[]' value='data-user-$obm[uid]' /></fieldset>";
  }

  // group select
  if (is_array($groups['entity'])) {
    foreach ($groups['entity'] as $g_id => $g_ent) {
      $g_name = $g_ent['label'];
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

  $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_group_id";
  $group_add_button = "
    <a href=\"javascript: return false;\" 
      onclick=\"popup('$url','');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";

  if ($has_access_to_other_calendars) {
    $block_group = "
      <fieldset class=\"detail infos\">
      <legend>$l_groups</legend>    
      <table>
      <tr>
      <th>
      $l_groups
      $group_add_button
      </th>
      <td>
        <input type=\"text\" name=\"\" value=\"\" id=\"groupSearch\" autocomplete=\"off\" />
      </td>
      </tr>

      <tr>
      <th>&nbsp;</th>
      <td id=\"sel_group_id\">$dis_sel_group</td>
      </tr>
      </table>
      </fieldset>";
    $group_complete = "new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search', 'sel_group_id', 'groupSearch');";
  }
  if ($ccalendar_resource && OBM_Acl::hasAllowedEntities($obm['uid'], 'resource', 'access')) {
    // resource select
    if (is_array($res['entity'])) {
      foreach ($res['entity'] as $r_id => $r_ent) {
        $r_name = $r_ent['label'];
        $sel_id = "data-resource-$r_id";
        $div_id = "sel_resource_id-$sel_id";
        $list_item = '';
        $list_item_id = '';
        $property_value = '';
        $property_name = '';
        // Item & Property
        $url_item = "$path/resource/resource_index.php?action=ext_ritem&ext_element=list-item&popup=1&amp;resource_id=$r_id";

        if ($item != '') {
            $xml = parse_item($item);
        }
        if (is_object($xml) && ($action == 'detailupdate' || $action == 'update'  || $action == 'duplicate' )) {
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
          $property_value = $t_property_value[$r_id];
        }
        if ($property_name == '') {
          $property_name = run_query_calendar_get_resource_property($r_id);
        }
        if ($property_value == '') {
          $property_value = $property_name;
        }        
        if (!empty($property_name)) {
          $block_property = "<div>
              <input type=\"hidden\" name=\"property-name[$r_id]\" id=\"property-name[$r_id]\" value=\"$property_name\" />
              <input type=\"text\" name=\"property[$r_id]\" id=\"property[$r_id]\" value=\"$property_value\" 
                onclick=\"if (this.value=='$property_name') this.value='';\"/>
          </div>";
        } else {
          $block_property = '';
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
            $block_property
          </div>";
      }
    }

    $url = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_resource_id&amp;filter_entity=Resource&amp;filter_pattern=access";
    $resource_add_button = "
    <a href=\"javascript: return false;\" 
      onclick=\"popup('$url','');
      return false;\">
     <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";

    $block_resource = "
    <fieldset class=\"detail infos\">
    <legend>$l_resources</legend>    
    <table>
    <tr>
    <th>
    $l_resources
    $resource_add_button
     </th>    
     <td>
       <input type=\"text\" name=\"\" value=\"\" id=\"resourceSearch\" autocomplete=\"off\" />
     </td>
    </tr>

    <tr>
    <th>&nbsp;</th>    
    <td id=\"sel_resource_id\">$dis_sel_resource</td>
    </tr>
    </table>
    </fieldset>";
    $resource_complete = "new obm.AutoComplete.Search('$path/resource/resource_index.php?action=ext_search&form=event', 'sel_resource_id', 'resourceSearch',{filter_entity:'Resource', filter_pattern:'access'});";
  }

  if ($force_disabled) {
    $dis_force_disabled = 'disabled="disabled" ';
  }
  
  if ($force == 1) {
    $dis_force = "<input type=\"checkbox\" id=\"cba_force\" value=\"1\" checked=\"checked\" name=\"cba_force\" $dis_force_disabled/>";
  } else {
    $dis_force = "<input type=\"checkbox\" id=\"cba_force\" value=\"1\" name=\"cba_force\" $dis_force_disabled/>";
  }
  
  if ($force_disabled) {
    $dis_force .= $l_force_disabled;
  }

  if ($privacy == 1) {
    $dis_privacy = "<input type=\"checkbox\" id=\"cba_privacy\" value=\"1\" checked=\"checked\" name=\"cba_privacy\" />";
  } else {
    $dis_privacy = "<input type=\"checkbox\" id=\"cba_privacy\" value=\"1\" name=\"cba_privacy\" />";
  }

  $dis_hour_b = "<select id=\"sel_time_begin\" name=\"sel_time_begin\" style=\"width:4em;\">";
  $dis_hour_e = "<select id=\"sel_time_end\" name=\"sel_time_end\" style=\"width:4em;\">";
  for ($i=0; $i<24; $i++) {
    $hour =  str_pad($i, 2, '0', STR_PAD_LEFT); 
    if ($begin->compareHour($i) == 0) {
      $dis_hour_b .= "<option value=\"$hour\" selected=\"selected\">$hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$hour\">$hour</option>";
    }
    if ($end->compareHour($i) == 0) {
      $dis_hour_e .= "<option value=\"$hour\" selected=\"selected\">$hour</option>";
    } else {
      $dis_hour_e .= "<option value=\"$hour\">$hour</option>";
    }    
  }
  $dis_hour_b .= '</select>';  
  $dis_hour_e .= '</select>';  

  $dis_min_b = "<select id=\"sel_min_begin\" name=\"sel_min_begin\" style=\"width:4em;\">";
  $dis_min_e = "<select id=\"sel_min_end\" name=\"sel_min_end\" style=\"width:4em;\">";
  $step = 60 / $ccalendar_hour_fraction;
  for ($i=0; $i < 60; $i += $step) {
    $minute =  str_pad($i, 2, '0', STR_PAD_LEFT); 
    if ($begin->compareMinute($i) == 0) {
      $dis_min_b .= "<option value=\"$minute\" selected=\"selected\">$minute</option>";
    } else {
      $dis_min_b .= "<option value=\"$minute\">$minute</option>";
    }
    if ($end->compareMinute($i) == 0) {
      $dis_min_e .= "<option value=\"$minute\" selected=\"selected\">$minute</option>";
    } else {
      $dis_min_e .= "<option value=\"$minute\">$minute</option>";
    }    
  }
  $dis_min_b .= '</select>';
  $dis_min_e .= '</select>';

  $display['title'] = "$dis_title";
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[5]";
  $url2 = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[6]&amp;ext_title=" . urlencode($l_calendar_select_group);

  // Exception
  if(is_array($date_exception)) {
    foreach($date_exception as $key => $exception) {
      $block_exception_detail .= "<div id=\"date_exception-$key\">
         <a href=\"javascript: remove_element('date_exception-$key','exceptionHome');\">
         <img src=\"$ico_delete\" alt=\"[Delete]\">
         </a>
         <input type=\"text\" name=\"tf_date_exception[]\" value=\"".$exception->getInputDate()."\" class=\"datePicker\" />
       </div>";
    }
  }
  $add_date = "<a href=\"javascript: add_exdate();\"><img src=\"$ico_add\" alt=\"[Date]\" /></a>";

  // Alerts
  $sel_alert = "<select name=\"sel_alert\">
        <option value=\"$c_none\">$l_none</option>";
  foreach ($ccalendar_alerts as $alert_sec => $alert_label) {
    $sel_alert .= "<option value=\"$alert_sec\"";
    if ($selected_alert == $alert_sec) {
      $sel_alert .= " selected=\"selected\"";
    }
    $sel_alert .= ">$alert_label</option>";
  }
  $sel_alert .= '</select>';

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"post\" id=\"new_event_form\" name=\"f_entity\"
   onsubmit=\"if (check_calendar_calendar(this)) return true; else return false;\"
   action=\"calendar_index.php\">
  <fieldset class=\"detail infos\">  
  <legend>$l_module_calendar</legend>
  <table>
  <tr>
    <th>$l_title</th>
    <td><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"250\" size=\"25\" value=\"$title\" /></td>
  </tr> 
  <tr>
    $block_owner
  </tr>
  <tr>
    <th>$l_location</th>
    <td><input type=\"text\" id=\"tf_location\" name=\"tf_location\" maxlength=\"60\" size=\"25\" value=\"$location\" /></td>
  </tr> 
    $block_category1
  <tr>
    <th>$l_force</th>
    <td>$dis_force</td>
  </tr>   
  </table>
  </fieldset>
  <fieldset class=\"detail infos\">
  <legend>$l_carac</legend>
  <table>
  <tr>
    <th>$l_private</th>
    <td>$dis_privacy</td>
  </tr> 
  <tr>
    <th>$l_all_day</th>
    <td>
      <input onchange=\"show_hide_calendar_dates(this)\" onclick=\"show_hide_calendar_dates(this)\" type=\"checkbox\" name=\"cba_all_day\" value=\"1\" $all_day_checked />
    </td>
  </tr>   
  <tr>
    <th>$l_datebegin</th>
    <td>
      <input type=\"text\" name=\"tf_date_begin\" id=\"tf_date_begin\" value=\"".$begin->getInputDate()."\" class=\"datePicker\" onchange=\"this.form.tf_date_end.value=this.value\"/>
      <span id=\"hour_begin\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr>
    <th>$l_dateend</th>
    <td>
      <input type=\"text\" name=\"tf_date_end\" id=\"tf_date_end\" value=\"".$end->getInputDate()."\" class=\"datePicker\" />
      <span id=\"hour_end\" style=\"$style\"  class=\"NW\">$l_hour : $dis_hour_e : $dis_min_e</span>
    </td>       
  </tr>
  <tr>
    <th>&nbsp</th>
    <td><a href=\"#\" onclick=\"checkAvailability()\">$GLOBALS[l_freebusy_check_availability]</a></td>
  </tr>
  <tr>
    <th>$l_priority</th>
    <td>$dis_sel_prio</td>
  </tr>     
  <tr>
    <th>$l_color</th>
    <td><input type=\"text\" name=\"tf_color\" value=\"$color\" class=\"colorPicker\" /></td>
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
      <input type=\"text\" name=\"tf_repeat_end\" id=\"tf_repeat_end\" value=\"".$repeat_end->getInputDate()."\" class=\"datePicker\" />
    </td>
  </tr>
  <tr id=\"repeatDays\" style=\"display:$repeatDaysDisplay\">  
    <th>$l_repeatdays2</th>
    <td>$dis_repeat_day</td>
  </tr>
  <tr id=\"repeatException\" style=\"display:$repeatDisplay; white-space:nowrap;\">
    <th> $l_exception $add_date</th>
    <td id=\"exceptionHome\">$block_exception_detail</td>
    </tr>
  <tr>
    <th></th>
    <td>&nbsp;</td>
  </tr>    
  </table>
  </fieldset>
  $block_user
  $block_resource
  $block_group
  <fieldset class=\"detail infos\">
  <legend>$l_alerts</legend>
  <table>    
  <tr>    
    <th>$l_alerts_time</th> 
    <td>$sel_alert</td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail extra\">    
  <legend>$l_desc</legend>  
  <textarea id=\"ta_description\" name=\"ta_description\" rows=\"3\" cols=\"72\">$desc</textarea> 
  </fieldset>
  $dis_attendees_notification
  <fieldset class=\"buttons\">$dis_button $hidden_repeat</fieldset>
  </form>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      $user_complete
      $resource_complete
      $group_complete
    });
  </script>
  <div id=\"fbcContainer\"></div>

"; 

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Waiting events
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_calendar_waiting_events($obm_wait) {
  global $l_users, $l_title, $l_location, $l_priority, $l_category,$l_high;
  global $l_dateend,$l_change_state,$l_validate,$l_user, $l_resource, $l_owner;
  global $l_wait,$l_low,$l_medium,$l_datebegin,$l_daysofweekshort,$l_update;
  global $l_carac,$l_accept,$l_refuse,$l_periodic,$l_yes,$l_no,$path;
   
  $owner_notification = "";
  if ($_GET['owner_notification'] == "true") $owner_notification = "checked=\"checked\"";

  $block = "<div class=\"detail\">
      <input type=\"checkbox\" id=\"owner_notification\" $owner_notification />$GLOBALS[l_owner_notification]
  </div>";

  while ($obm_wait->next_record()) {
    $old_entity = $current_entity;
    $old_entity_entity = $current_entity_entity;
    $old_label = $current_label;
    $current_entity = $obm_wait->f('entity_id');
    $current_entity_entity = $obm_wait->f('entity_entity');
    $current_label = $obm_wait->f('entity_label');
    
    // If event for a new calendar (delegation), display the previous block
    if ($current_entity != $old_entity || $current_entity_entity != $old_entity_entity) {
      $type = ${'l_'.$current_entity_entity};
      // if previous block had events (true except first block)
      if ($block != '') {
        $block .= '</tbody></table>';    
      }
      $block .= "
      <h1>$type : $current_label</h1>
      <table class=\"spreadSheet\">
      <thead>
       <tr>
        <th>$l_title</th>
        <th>$l_periodic</th>
        <th>$l_location</th>
        <th>$l_category</th>
        <th>$l_priority</th>
        <th>$l_owner</th>
        <th>$l_datebegin</th>
        <th>$l_dateend</th>
        <th></th>
      </tr>
      </thead>
      <tbody>";
    }

    switch ($obm_wait->f('event_priority') ) {;
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
  
    if ($obm_wait->f('event_repeatkind') != 'none') {
      $periodic = $l_yes;
    } else {
      $periodic = $l_no;
    }

    $user_create = $obm_wait->f('usercreate_firstname').' '.$obm_wait->f('usercreate_lastname');
    $begin = new Of_Date($obm_wait->f('event_date'), 'GMT');
    $end = clone $begin;
    $end->addSecond($obm_wait->f('event_duration'));
    $date_begin = urldecode($begin->getURL());
    $date_end = urldecode($end->getURL());
    $time_end = $end->getHour();
    $min_end = $end->getMinute();
    $block .= "
    <tr>
     <td><a href=\"$path/calendar/calendar_index.php?action=detailconsult&amp;calendar_id=".$obm_wait->f('event_id')."\">".$obm_wait->f('event_title')."</a></td>
     <td>$periodic</td> 
     <td>".$obm_wait->f('event_location')."</td>
     <td>".$obm_wait->f('eventcategory1_label')."</td>
     <td>$priority</td>
     <td>$user_create</td>
     <td>".$begin->getOutputDateTime()."</td>
     <td>".$end->getOutputDateTime()."</td>
     <td>
      <form method=\"get\" action=\"calendar_index.php\">
       <input type=\"hidden\" name=\"action\" value=\"decision\" />      
       <input type=\"hidden\" name=\"calendar_id\" value=\"".$obm_wait->f('event_id')."\" />
       <input type=\"hidden\" name=\"entity_kind\" value=\"$current_entity_entity\" />
       <input type=\"hidden\" name=\"entity_id\" value=\"$current_entity\" />
       <input type=\"hidden\" name=\"owner_notification\" value=\"false\" />
       <input type=\"hidden\" name=\"date_begin\" value=\"$date_begin\" />
       <input type=\"hidden\" name=\"date_end\" value=\"$date_end\" />
       <input type=\"hidden\" name=\"time_end\" value=\"$time_end\" />
       <input type=\"hidden\" name=\"min_end\" value=\"$min_end\" />
       <span class=\"NW\"><input type=\"radio\" name=\"rd_decision_event\" value=\"ACCEPTED\"  onclick=\"submitWaiting(this.form)\" />$l_accept</span><br />
       <span class=\"NW\"><input type=\"radio\" name=\"rd_decision_event\" value=\"NEEDS-ACTION\" checked=\"checked\" onclick=\"submitWaiting(this.form)\" />$l_wait</span><br />
       <span class=\"NW\"><input type=\"radio\" name=\"rd_decision_event\" value=\"DECLINED\" onclick=\"submitWaiting(this.form)\" />$l_refuse</span>
      </form>
     </td>
    </tr>";
  }

  if($block != '') {
    $block .= '</tbody></table>';
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
  global $display, $l_confirm_delete_all,$l_confirm,$l_cancel,$l_confirm_mail;
  global $l_event_cant_delete, $module, $cright_write_admin;

  $iso = $calendar['date']->getURL();
  $event_id = $calendar['calendar_id'];

  $block = "
   <h1>$l_confirm_delete_all</h1>
   <div class=\"C\">
    <a href=\"calendar_index.php?action=delete&calendar_id=$event_id\"><img src=\"$ico_validate\" alt=\"[Validate]\"/></a>
     $l_confirm
    <a href=\"calendar_index.php?action=detailconsult&calendar_id=$event_id&date=$iso\"><img src=\"$ico_cancel\" alt=\"[Cancel]\" /></a>
     $l_cancel
   </div>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $calendar        : calendar parameters
//   - $sel_entity_id : array of entity Id ['user'] ['resource']
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_meeting_form($calendar, $sel_entity_id) {
  global $display, $path;
  global $ico_add, $ico_delete, $ico_mini_cal;
  global $ccalendar_resource, $obm;
  global $l_users,$l_datebegin,$l_dateend,$l_header_meeting,$l_event_duration,$l_meeting_perform; 
  global $l_groups,$l_resources,$l_module_resourcegroup, $l_module_calendar;
  global $l_resources_one_freebusy,$l_resources_all_freebusy,$l_resource_group_search;

  $date = $calendar['date'];
  $hourbegin = $calendar['time_duration'];
  $minbegin = $calendar['min_duration'];
  if (($hourbegin == '') || ($hourbegin <= 0)) {
    $hourbegin = '1';
  }
  $dis_button = "
     <input type=\"hidden\" name=\"calendar_id\" id=\"calendar_id\" value=\"$id\" />
     <input type=\"hidden\" name=\"action\" id=\"action\" value=\"perform_meeting\" />
     <input type=\"submit\" value=\"$l_meeting_perform\" />";

  $users_id = $sel_entity_id['user'];
  if (count($users_id > 0)) {
    $users = get_userobm_from_ids($users_id);
  } else {
    $users = array();
  }
  $res_id = $sel_entity_id['resource'];
  if (count($res_id > 0)) {
    $res = get_calendar_resource_from_ids($res_id);
  } else {
    $res = array();
  }
  $groups_id = $sel_entity_id['group'];
  if (count($groups_id > 0)) {
    $groups = get_calendar_group_from_ids($groups_id);
  } else {
    $groups = array();
  }

  // user select
  if (is_array($users['entity'])) {
    foreach ($users['entity'] as $u_id => $u_ent) {
      $u_name = $u_ent['label'];
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

  $url = "$path/people/people_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id&amp;filter_entity=calendar&amp;filter_pattern=access";
  $user_add_button = "
    <a href=\"javascript: return false;\"
      onclick=\"popup('$url','');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";
  $block_user = "
    <th>
    $GLOBALS[l_users]/$GLOBALS[l_contacts]
    $user_add_button
    </th>
    <td id=\"sel_user_id\">
      <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" />
      $dis_sel_user
    </td>";
    $user_js = "
    new obm.AutoComplete.Search('$path/people/people_index.php?action=ext_search', 'sel_user_id', 'userSearch', {filter_entity:'calendar',filter_pattern:'access'});
    new obm.AutoComplete.Search('$path/people/people_index.php?action=ext_search', 'sel_user_id', 'userSearch');
    new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search', 'sel_group_id', 'groupSearch');";

  if ($ccalendar_resource && OBM_Acl::hasAllowedEntities($obm['uid'], 'resource', 'access')) {
    if (is_array($res['entity'])) {
      foreach ($res['entity'] as $r_id => $r_ent) {
  $r_name = $r_ent['label'];
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

    $url_resource = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_resource_id";
    $resource_add_button = "
      <a href=\"javascript: return false;\"
      onclick=\"popup('$url_resource','');
      return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";

    $dis_block_resource = "
    <tr>
      <th>
      $l_resources
      $resource_add_button
      </th>
     <td id=\"sel_resource_id\">
      <input type=\"text\" name=\"\" value=\"\" id=\"resourceSearch\" autocomplete=\"off\" />
       $dis_sel_resource
     </td>
    </tr>";
  
    if (is_array($res_grp['entity'])) {
      foreach ($res_grp['entity'] as $rg_id => $rg_ent) {
        $rg_name = $rg_ent['label'];
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

    $url_resource_group = "$path/resourcegroup/resourcegroup_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_resource_group_id";
    $rgroup_add_button = "
      <a href=\"javascript: return false;\"
      onclick=\"popup('$url_resource_group','');
      return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";

    $dis_block_resource_group = "
    <tr>
      <th>
      $l_module_resourcegroup
      $rgroup_add_button
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
    $dis_block_resource = "
      <fieldset class=\"detail infos\">
      <legend>$l_resources</legend>  
      <table>
      $dis_block_resource 
      $dis_block_resource_group
      <input type='hidden' name='resource_group_search' value='all' />
      </table>
      </fieldset>";    
      //  <tr><th>$l_resource_group_search</th><td>
      //  <select name='resource_group_search'>
      //  <option value='one'>$l_resources_one_freebusy</option>
      //  <option value='all'>$l_resources_all_freebusy</option>
      //  </select>
      //  </td></tr>        
  }
  
  // group select
  if (is_array($groups['entity'])) {
    foreach ($groups['entity'] as $g_id => $g_ent) {
      $g_name = $g_ent['label'];
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

  $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_group_id";
  $group_add_button = "
    <a href=\"javascript: return false;\"
      onclick=\"popup('$url','');
      return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";

  $block_group = "
    <th>
    $l_groups
    $group_add_button
    </th>
    <td id=\"sel_group_id\">
      <input type=\"text\" name=\"\" value=\"\" id=\"groupSearch\" autocomplete=\"off\" />
      $dis_sel_group
  </td>";
  if (OBM_Acl::hasAllowedEntities($obm['uid'], 'calendar', 'access')) {
    $dis_block_user = "<fieldset class=\"detail infos\">  
      <legend>$GLOBALS[l_attendees]</legend>
      <table>
      <tr>
        $block_user
      </tr>
      <tr>
        $block_group
        </tr>
      </table>
      </fieldset> ";
  }

  $dis_hour_d = "<select style='width:4em;' id=\"sel_time_duration\" name=\"sel_time_duration\">";
  for ($i=0;$i<24;$i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourbegin){
      $dis_hour_d .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_d .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_d .= '</select>';
 
  $dis_min_d = "<select style='width:4em;' id=\"sel_min_duration\" name=\"sel_min_duration\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr('0'.(60 - (15*$i)),-2);
    if ($current_min  == $mindur){
      $dis_min_d .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_d .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_d .= '</select>';
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[0]";

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"post\" name=\"f_entity\"
   onsubmit=\"if (calendar_check_meeting(this)) return true; else return false;\"
   action=\"calendar_index.php\">
  </fieldset>
  <fieldset class=\"detail infos\">  
  <legend>$l_header_meeting</legend>
  <table>
  <tr>
    <th>$l_datebegin</th>
    <td>
      <input type=\"text\" id=\"tf_date\" name=\"tf_date\" value=\"".$date->getInputDate()."\" class=\"datePicker\" />
    </td>
  </tr>
  <tr>    
    <th>$l_event_duration</th>
    <td>$dis_hour_d : $dis_min_d</td>
  </tr>
  </table>
  </fieldset>   
    $dis_block_user 
    $dis_block_resource
  <fieldset class=\"buttons\">
    <input type=\"button\" onclick=\"performMeeting();\" value=\"$GLOBALS[l_meeting_perform]\" />
  </fieldset>    
  </form>
  <div id=\"fbcContainer\"></div>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      $user_js
      $resource_js
    });
  </script>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Free meeting performing and display
// Free/Busy interface
// Parameters:
//   - $calendar       : calendar parameters
//   - $entity_store : array of entities to display
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_free_interval($calendar) {
  global $obm, $display;

  $date = $calendar['date'];
  $date_ts = $date->setHour($GLOBALS['ccalendar_first_hour'])->setMinute(0)->setSecond(0)->getTimestamp();

  $date->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);  

  $time_unit_sec = 3600 / $_SESSION['set_cal_interval']; 
  $unit = 3600/$time_unit_sec;
  $colWidth = 40/$unit - 2; // -2 => due to border !

  // Days headers 
  $current = clone $date;
  $colspan = ($GLOBALS['ccalendar_last_hour'] - $GLOBALS['ccalendar_first_hour']) * $unit;
  while($current->compare($end) < 0) {
    $current->setHour($GLOBALS['ccalendar_first_hour']);
    $day = $current->localize(Of_Date::OBM_DATE_DAY);
    $week_day_list .= "<td colspan='$colspan' class='freeBusyCalendarDay'>$day</td>\n";
    $end_day = clone $current;
    $end_day->setHour($GLOBALS['ccalendar_last_hour']);
    // Hour
    while($current->compare($end_day) < 0) {
      $label = "";
      $class= "";
      if($current->compareMinute(0) == 0) {
        $label = $current->get(Of_Date::TIME_SHORT);
        $class = "freeBusyCalendarHourDisplayed";
        $day_hours_list .= "<td colspan='$unit' class='$class freeBusyCalendarHour'><span>$label</span></td>";
      }
      $current->addSecond($time_unit_sec);
    }
    $current->addDay(1);
  }

  $url = "$path/people/people_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_attendees_id&amp;filter_entity=calendar&amp;filter_pattern=access";
  $user_add_button = "
    <a href='javascript: return false;' onclick='popup(\"$url\",\"\"); return false;'>
      <img src='$GLOBALS[ico_add]' alt='[Add]' />
    </a>";

  $url_resource = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_attendees_id";
  $resource_add_button = "
    <a href='javascript: return false;' onclick='popup(\"$url_resource\",\"\"); return false;'>
     <img src='$GLOBALS[ico_add]' alt='[Add]' />
    </a>";

  $attendeesBar = "<tr>";
  $time_slots = array();
  $current = clone $date;
  while($current->compare($end) < 0) {
    $current->setHour($GLOBALS['ccalendar_first_hour']);
    $end_day = clone $current;
    $end_day->setHour($GLOBALS['ccalendar_last_hour']);
    while($current->compare($end_day) < 0) {
      $end_slot = clone $current;
      $end_slot->addSecond($time_unit_sec);
      $attendeesBar .= "<td class='timeSlot'><div id='".$current->getTimestamp()."' style='width:${colWidth}px;'></div></td>";
      array_push($time_slots, $current->get(Of_Date::TIMESTAMP));
      $current->addSecond($time_unit_sec);
    }
    $current->addDay(1);
  }
  $attendeesBar .= "</tr>";

  $js_time_slots = phpArrayToJsArray($time_slots);
  $l_calendar_not_available = phpStringToJsString($GLOBALS['l_calendar_not_available']);
  $l_allday = phpStringToJsString($GLOBALS['l_all_day']);
  $l_invalid_email = phpStringToJsString($GLOBALS['l_invalid_email']);
  // Navigation
  $current = clone $date;
  $prev_month = $current->setDateIso($calendar['date'])->subMonth(1)->getDateIso();
  $prev_week = $current->setDateIso($calendar['date'])->subWeek(1)->getDateIso();
  $next_week = $current->setDateIso($calendar['date'])->addWeek(1)->getDateIso();
  $next_month = $current->setDateIso($calendar['date'])->addMonth(1)->getDateIso();
  $label = $date->localize(Of_Date::OBM_DATE_DAY)." - ".$date->addDay(6)->localize(Of_Date::OBM_DATE_DAY);

  $block = "
  <div id='fbc' class='freeBusyContent obmPopup'>
    <h1>
    <a href='' id='closeFreeBusy' class='closeSticky' onclick='return false;'>
    <img src='$GLOBALS[ico_popup_close]' alt='close' />
    </a>
    <span>$GLOBALS[l_header_meeting]</span>
    </h1>
    <h2>
      <a href='#' onclick='changePeriod(\"$prev_month\")'><img src='$GLOBALS[ico_far_previous]' alt='[$GLOBALS[l_previous]'] /></a>
      <a href='#' onclick='changePeriod(\"$prev_week\")'><img src='$GLOBALS[ico_previous]' alt='[$GLOBALS[l_previous]'] /></a>
      $label
      <a href='#' onclick='changePeriod(\"$next_week\")'><img src='$GLOBALS[ico_next]' alt='[$GLOBALS[l_next]'] /></a>
      <a href='#' onclick='changePeriod(\"$next_month\")'><img src='$GLOBALS[ico_far_next]' alt='[$GLOBALS[l_next]'] /></a>
    </h2>
    <div class='freeBusyEntities'>
      <form method='POST' id='entities_form' action='calendar_index.php'>
        <table id='freeBusyEntitiesList'> 
          <tr>
            <td>&nbsp;</td>
          </tr>  
          <tr>
            <td><img id='spinner' style='display:none;' src='$GLOBALS[ico_spinner]' /></td>
          </tr>  
          <tr>
            <td><div class='allAttendees'>$GLOBALS[l_attendees]</div></td>
          </tr>  
          <tr>
            <td id='sel_attendees_id'></td>
          </tr>
          <tbody>
            <tr>
              <td>$GLOBALS[l_users]/$GLOBALS[l_contacts] $user_add_button</td>
            </tr>
            <tr>
              <td><input type='text' name='' value='' id='freeBusyUserSearch' autocomplete='off' /></td>
            </tr>
            <tr>
              <td>$GLOBALS[l_resources] $resource_add_button</td>
            </tr>
            <tr>
              <td><input type='text' name='' value='' id='freeBusyResourceSearch' autocomplete='off' /></td>
            </tr>
            <tr>
              <td>$GLOBALS[l_others_attendees]</td>
            </tr>
            <tr>
              <td><input style='width:14em;' type='text' name='external_contact' value='' id='external_contact' />
                <img src='$GLOBALS[ico_add]' alt='[Add]' onclick='obm.calendarFreeBusy.addOtherAttendee();'/></td>
            </tr>
          </tbody>
        </table>
      </form>
    </div>
    <div title='dummy div for IE hack'></div>
    <div class='freeBusyForm'>
      <div id='scrollLeft' >«</div>
      <div title='dummy div for IE hack'></div>
      <div id='scrollRight' >»</div>
      <div class='freeBusyCalendar'>
        <div id='calendarFreeBusyScroll'>
          <table id='calendarFreeBusyTable'> 
            <colgroup span='$colspan' class='freeBusyCalendarColDay'>
            <colgroup span='$colspan' class='freeBusyCalendarColDay'>
            <colgroup span='$colspan' class='freeBusyCalendarColDay'>
            <colgroup span='$colspan' class='freeBusyCalendarColDay'>
            <colgroup span='$colspan' class='freeBusyCalendarColDay'>
            <colgroup span='$colspan' class='freeBusyCalendarColDay'>
            <colgroup span='$colspan' class='freeBusyCalendarColDay'>
            <thead id='calendarHead'>
              <tr>$week_day_list</tr>
              <tr>$day_hours_list</tr>
            </thead>
            <tbody id='calendarFreeBusyGrid'>
              $attendeesBar
            </tbody>
          </table>
          <div id='calendarFreeBusyMeeting' class='meeting'>
            <div id='calendarFreeBusyResizeMeeting' class='meetingResize'></div>
          </div>
        </div>
      </div>

      <div class='freeBusyButtons'>
        <input type='button' id='pickPrev' value='$GLOBALS[l_freebusy_autopick_prev]' onclick='obm.calendarFreeBusy.autoPickPrev();' />
        <input type='button' id='pickNext' value='$GLOBALS[l_freebusy_autopick_next]' onclick='obm.calendarFreeBusy.autoPickNext();' />
      </div>

      <div id='meetingInfo' class='detail'>
        <table class='infos'>
          <tr>
            <th>$GLOBALS[l_datebegin]</th>
            <td id='meeting_start'></td>
          </tr>
          <tr>
            <th>$GLOBALS[l_dateend]</th>
            <td id='meeting_end'></td>
          </tr>
        </table>
      </div>

      <div class='buttons'>
        <form id='freeBusyFormId' method='POST' action='$GLOBALS[path]/calendar/calendar_index.php'>
          <input id='submitMeeting' type='submit' value='$GLOBALS[l_validate]' />
          <input type='hidden' name='date_begin'id='date_begin' value='".urldecode($date->setHour($GLOBALS['ccalendar_first_hour'])->getURL())."' />
          <input type='hidden' name='duration' id='duration' value='' />
          <input type='hidden' name='time_begin' id='time_begin' value='' />
          <input type='hidden' name='min_begin' id='min_begin' value='' />
          <input type='hidden' name='new_meeting' value='1'>
          <input type='hidden' name='action' value='new'>
        </form>
      </div>
    </div>

  </div>";

  $js = "
    timestamp = $date_ts;
    if ($('sel_time_begin') && $('sel_min_begin')) {
      hour = parseInt($('sel_time_begin').value, 10);
      min = parseInt($('sel_min_begin').value, 10);
      d = new Obm.DateTime(timestamp*1000);
      d.setHours(hour);
      d.setMinutes(min);
      timestamp = d.getTime()/1000;
    }
    obm.calendarFreeBusy = new Obm.CalendarFreeBusy($js_time_slots, $unit, timestamp);
    obm.vars.labels.calendar_not_available = '$l_calendar_not_available';
    obm.vars.labels.allday = '$l_allday';
    obm.vars.labels.invalidEmail = '$l_invalid_email';
    obm.vars.images.ico_user = '$GLOBALS[ico_user]';
    obm.vars.images.ico_resource = '$GLOBALS[ico_resource]';
    obm.vars.images.ico_contact = '$GLOBALS[ico_contact]';
    obm.vars.images.ico_meeting = '$GLOBALS[ico_meeting_alt]';
    obm.vars.images.ico_private = '$GLOBALS[ico_private_alt]';
    obm.vars.images.ico_allday = '$GLOBALS[ico_allday_alt]';
    obm.vars.images.ico_periodic = '$GLOBALS[ico_periodic_alt]';
    new obm.AutoComplete.Search('$GLOBALS[path]/people/people_index.php?action=ext_search&ajax=1', 'sel_attendees_id', 'freeBusyUserSearch',
      {filter_entity:'calendar', filter_pattern:'read', selectfunction:'obm.calendarFreeBusy.addAttendee(id);'});
    new obm.AutoComplete.Search('$GLOBALS[path]/resource/resource_index.php?action=ext_search', 'sel_attendees_id', 'freeBusyResourceSearch',
      {filter_entity:'calendar', filter_pattern:'read', selectfunction:'obm.calendarFreeBusy.addAttendee(id);'});";

  $display['json'] = "html:'".phpStringToJsString($block)."',js:'".phpStringToJsString($js)."'";
  
  return;
}

/**
 * dis_calendar_right_dis_admin
 * Display calendar rights admin form
 *
 * @param mixed $id calendar(user) id to admin
 * @return mixed xhtml display
 */
function dis_calendar_right_dis_admin($id='') {
  global $obm, $display, $l_rights, $l_calendar, $l_rights_on_my, $l_user_right;

  if ($id == '') {
    $id = $obm['uid'];
  }
  $infos = get_user_info($id);
  $name = $infos['firstname'] . ' ' . $infos['lastname'];
  $display['title'] = $name;

  if ($id == $obm['uid']) {
    $block = "<h1>$l_rights_on_my</h1>";
  } else {
    $block = "<h1>$l_user_right $name</h1>";
  }
  $block .= of_right_dis_admin('calendar', $id);
//  $block .= "<h1>$l_rights_my_on</h1>";
//
//  $allowed = OBM_Acl::getAllowedEntities($id, 'calendar', 'access');
  //  $allowed = OBM_Acl::getRights($id, 'calendar', null);
// Get all actions for this ? update getactionclause() ?
//  print_r($allowed);

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
  $class = 'B';
  foreach($resource_group as $resource) {
    $ret .= "<li class=\"$class\" >  ".$resource['name'].'</li>';
    $class = '';
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

  $cats1 = of_category_get_ordered('event', 'category1');
  $block = of_category_dis_admin_form('category1', $cats1);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Perform the export to the vCalendar format
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_export_handle($calendar) {
  global $obm, $obm_version, $ccalendar_weekstart;

  $date = $calendar['date'];

  $start_time = strtotime('-10 year', $ts_date);
  $end_time = strtotime('+10 year', $ts_date);
  $start = clone $date;
  $start = $start->subYear(10);
  $end = clone $date;
  $end->addYear(10);
  include_once('obminclude/of/vcalendar/writer/ICS.php');
  include_once('obminclude/of/vcalendar/reader/OBM.php');

  $calendar_user['user'] = array ($obm['uid'] => 'dummy');
  
  $reader = new Vcalendar_Reader_OBM($calendar_user,NULL,$start,$end);
  $document = $reader->getDocument();
  $writer = new Vcalendar_Writer_ICS();  
  $writer->writeDocument($document);
  header('Content-Type: text/x-vCalendar');
  header('Content-Disposition: inline; filename=ObmCalendar.ics');
  header('Cache-Control: maxage=3600'); 
  header('Pragma: public');  
  echo $writer->buffer;
}

/**
 * dis_calendar_reset 
 * 
 * @access public
 * @return void
 */
function dis_calendar_reset() {
  global $path, $l_confirm_reset, $l_cancel, $l_confirm,$l_js_confirm_reset;
  $block = "
    
    <div class=\"buttons\">
      <h1>$l_confirm_reset</h1>
      <a href='$path/calendar/calendar_index.php'>$l_cancel</a>
      <a href='$path/calendar/calendar_index.php?action=reset&amp;force=1' onclick=\"return confirm('$l_js_confirm_reset');\">$l_confirm</a>
    </div>
    <p class=\"LC\" />" ;

  return $block ;
}
///////////////////////////////////////////////////////////////////////////////
// Import iCalendar file
///////////////////////////////////////////////////////////////////////////////
function dis_icalendar_import($params) {
  global $display ;
  global $l_ics, $l_ics_file, $l_ics_insert, $l_insert, $l_calendar ;

  // --- HTML Template --------------------------------------------------------
  $dis_button = "<input type=\"hidden\" name=\"action\" value=\"ics_insert\">
          <input type=\"submit\" value=\"$l_ics_insert\">";

  $block = "
    <form method=\"post\" name=\"f_entity\" enctype=\"multipart/form-data\"
      action=\"calendar_index.php\">
    <fieldset class=\"detail extra\">
      <legend></legend>
      <table class=\"detail\">
      <tr>
        <th class=\"detailLabel\">$l_ics_file</th>
        <td><input name=\"fi_ics\" type=\"file\" value=\"\"/></td>
      </tr>
      </table>
    </fieldset>
    <fieldset class=\"buttons\">
      $dis_button
    </fieldset>
    </form>
    <p class=\"LC\" />" ;

  return $block ;
}

///////////////////////////////////////////////////////////////////////////////
// Import iCalendar file
///////////////////////////////////////////////////////////////////////////////
function dis_icalendar_insert($result) {
  global $l_ics_import_ok;

  $block = '<div>'.$result[0]." ".$l_ics_import_ok."</div>" ;
  return $block ;
}

///////////////////////////////////////////////////////////////////////////////
/**
 * JSON ok Message. 
 * 
 * @access public
 * @return void
 */
function json_ok_msg($msg) {
  global $display;
  $msg = phpStringToJsString($msg);
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

  $msg = phpStringToJsString($msg);
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
  global $l_user, $l_resource;

  $context = $calendar['context'];
  $element_id = $calendar['element_id'];
  $ent_q = run_query_get_events_attendee(array($id));
  $entities = get_calendar_event_entity($id);
  $all_day = ($calendar['all_day'])?1:0;
  $duration = $calendar['duration'];
  $date = $calendar['date_begin'];
  $end = clone $date;
  $end->addSecond($duration);
  $title = $calendar['title'];
  while($ent_q->next_record()) {
    $entity = $ent_q->f('eventlink_entity');
    $entity_id = $ent_q->f('eventlink_entity_id');
    $name = $ent_q->f('eventlink_label');
    $attendee[$entity][$entity_id] .= $name.'('.$GLOBALS['l_'.strtolower($ent_q->f('eventlink_state'))].')';
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

  $ed[] = 'id:'.$id;
  $ed[] = 'duration:'.$duration;
  $ed[] = 'time:'.$date->getTimestamp();
  $ed[] = 'all_day:'.$all_day;
  $ed[] = "title:'$title'";
  $ed[] = "attendees:[".implode(',',$attendees)."]";
  $ed[] = 'updatable:true';
  $ed[] = "description:''";
  $ed[] = "location:''";
  $ed[] = "category:''";
  $ed[] = "colors:{}";
  
  $entity_readable = get_calendar_entity_readable();
  $sel_entity_id = slice_calendar_entities($cal_entity_id, $ccalendar_max_users, $entity_readable);
  $calendar_entity = store_calendar_entities(get_calendar_entity_label($cal_entity_id));
  
  switch($context)  {
    case 'day' :
      $day = ($all_day) || ($end->compareDateIso($date) != 0);
      $day = $day || ($date->compareHour($ccalendar_first_hour) <  O);
      $day = $day || ($date->compareHour($ccalendar_last_hour) >= 0 );
      $options = array(); 
      break;
    case 'week' :
      $day = ($all_day) || ($end->compareDateIso($date) != 0);
      $day = $day || ($date->compareHour($ccalendar_first_hour) <  O);
      $day = $day || ($date->compareHour($ccalendar_last_hour) >= 0 );
      $options = array();
      break;
    case 'month' :
      $day = true;
      $week_duration = 86400*7;
      $options = array('xUnit:86400',"yUnit:$week_duration",
                       "context: 'body'");      
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
      if($entities[$entity]['entity'][$entity_id]) {
        $info = $ed;
        $opt = $options;
        $info[] = "entity:'$entity'";
        $info[] = "entity_id:$entity_id";
        $info[] = "klass:'".$entity_data['class']."'";
        $info[] = "status:'".$entities[$entity]['entity'][$entity_id]['state']."'";
        if($entity_id = $obm['uid'] && $entity == 'user') {
          $opt = array_merge($opt,$updatable);
        }
        if($json != '') {
          $json .= ',';
        }
        $json .= "{event:{".implode(',',$info)."},options:{".implode(',',$opt)."}}";
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
  if ($item == '') {
    return false;
  }
  $p = xml_parser_create();
  xml_parser_set_option($p, XML_OPTION_TARGET_ENCODING, 'ISO-8859-1');
  xml_parse_into_struct($p, $item, $vals, $index);
  xml_parser_free($p);

  $match_attribut = array('resource', 'res_id', 'res_name', 'item', 'item_id', 'property_name', 'property_value');

  foreach($vals as $key => $value) {
    $att = strtolower($value['tag']);
    if (in_array($att, $match_attribut)) { 
      $${att} = $value['value'];
      if ($att == 'resource' && $value['type'] == 'close') {
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


/**
 * Calendar PDF export options
 * ex: page layout portrait|landscape
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_pdf_options($calendar, $p_range, $p_view) { 
  global $l_print, $l_pagelayout, $l_pagelayout_portrait, $l_pagelayout_landscape;
  global $c_pagelayout_portrait, $c_pagelayout_landscape;
  global $ccalendar_first_hour, $ccalendar_last_hour, $ccalendar_weekstart;
  global $l_first_hour, $l_last_hour, $l_daysofweekshort, $l_display_details;
  global $l_one_day_per_page, $l_display_days ;

  if ($p_view == 'list') {
    $block_detail = "
    <tr>
      <th>$l_display_details</th>
      <td>
        <input type=\"checkbox\" name=\"cba_showdetails\" value=\"1\">
      </td>
    </tr><tr>
      <th>$l_one_day_per_page</th>
      <td>
        <input type=\"checkbox\" name=\"cba_onedayperpage\" value=\"1\">
      </td>
    </tr>"; 
  }

  if ($p_view == 'agenda' && $p_range != 'month') {
    $dis_hour_b = "<select name=\"cal_first_hour\" id=\"cal_first_hour\" style=\"width:4em;\">";
    for ($current_hour=0; $current_hour<24; $current_hour++) {
      if ($current_hour == $_SESSION['set_cal_first_hour']) {
        $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
      } else {
        $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
      }
    }
    $dis_hour_b .= "</select>"; 

    $dis_hour_e = "<select name=\"cal_last_hour\" id=\"cal_last_hour\" style=\"width:4em;\">";
    for ($current_hour=1; $current_hour<=24; $current_hour++) {
      if ($current_hour == $_SESSION['set_cal_last_hour']) {
        $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
      } else {
        $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
      }
    }
    $dis_hour_e .= "</select>";  

    // Week options
    if ($p_range == 'week'){
      $display_days = $_SESSION['set_cal_display_days'];
      if (empty($display_days)) $display_days = '1111111';

      $start_week_day = strtotime($ccalendar_weekstart);
      for ($i=0; $i<7; $i++) {
        $day_num = date('w', $start_week_day);
        $day = $l_daysofweekshort[$day_num];
        $cba_id = "cba_repeatday_$i";
        $dis_list_day .= "<input type=\"checkbox\" id=\"$cba_id\" name=\"$cba_id\" value=\"1\"";
        if (strcmp(substr($display_days,$i,1),'1')==0) {
          $dis_list_day .= " checked = \"checked\"";
        }
        $dis_list_day .= " /> $day";

        $start_week_day = strtotime('+1 day', $start_week_day); 
      }

      $block_week = "
      <tr>
        <th>$l_one_day_per_page</th>
        <td>
          <input type=\"checkbox\" name=\"cba_onedayperpage\" value=\"1\">
        </td>
      </tr><tr id=\"display_days\">  
        <th>$l_display_days</th>
        <td>$dis_list_day</td>
      </tr>";
    }

    $block_detail = "
      <tr>
        <th>$l_first_hour</th>
        <td>$dis_hour_b</td>
      </tr><tr>
        <th>$l_last_hour</th>
        <td>$dis_hour_e</td>
      </tr>
      $block_week";
  }

  $date = $calendar["date"];

  $block = "<form action=\"\" method=\"get\" onsubmit=\"return check_calendar_pdf_options();\">
    <div class=\"detail extra\">
      <h1>Options</h1>
      <table>
        <tr>
          <th>$l_pagelayout</th>
          <td>
            <select name=\"sel_pagelayout\">
              <option value=\"$c_pagelayout_portrait\">$l_pagelayout_portrait</option>
              <option value=\"$c_pagelayout_landscape\">$l_pagelayout_landscape</option>
            </select>
          </td>
        </tr>
        $block_detail
      </table>
    </div>
    <div class=\"buttons\">
      <input type=\"hidden\" name=\"action\" value=\"pdf_export\">
      <input type=\"hidden\" name=\"date\" value=\"".$date->getIso()."\">
      <input type=\"submit\" value=\"$l_print\"/>
    </div>
  </form>";

  return $block;
}


/**
 * Calendar PDF Export
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_pdf_view($calendar, &$sel_entity_id, $p_range, $p_view) {
  global $display, $path, $ccalendar_max_users, $obm;
  global $l_module_calendar;

  $group_view = $calendar['entity']['group_view'];
  $entity_readable = get_calendar_entity_readable();
  $visible_entities_id = slice_calendar_entities($sel_entity_id, $ccalendar_max_users, $entity_readable);

  $sel_entity_id = $visible_entities_id;
  // we restore the selected group for session storage
  $sel_entity_id['group_view'] = $group_view;

  $calendar_entity = store_calendar_entities(get_calendar_entity_label($sel_entity_id));
  $calendar_function = 'dis_calendar_'.$p_range.'_'.$p_view.'_pdf';

  $calendar_function($calendar, $calendar_entity);

}


/**
 * Calendar Month PDF
 * view agenda
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_month_agenda_pdf($calendar, $calendar_entity) {
  global $ccalendar_weekstart, $ccalendar_first_hour;

  $date = clone $calendar['date'];
  $date->setDay(1)->addDay(1)->subWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $calendar['date'];
  $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $category_filter = $calendar['category_filter'];
  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter);

  foreach($occurrences as $occurrence){
    $c = clone $occurrence->date;
    $c->setHour(0)->setMinute(0)->setSecond(0);
    $id = $c->getTimestamp();
    if(!is_array($updated[$id])) $updated[$id] = array();
    array_push($updated[$id], $occurrence);
  }

  $pdf = new CalendarPdfMonthAgenda($calendar, $calendar_entity, $updated);
  $pdf->drawCalendar();
  $pdf->download();

}


/**
 * Calendar Week PDF
 * view agenda
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_week_agenda_pdf($calendar, $calendar_entity) {
  global $ccalendar_first_hour, $ccalendar_last_hour, $ccalendar_weekstart;

  $date = clone $calendar['date'];
  
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);

  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $category_filter = $calendar['category_filter'];

  $occurrences = calendar_events_model($date,$end,$calendar_entity, $status, $category_filter);

  while($date->compare($end) < 0) {
    $columns[$date->getTimestamp()] = array();
    $date->addDay(1);
  }

  $time_unit = 60 / $_SESSION['set_cal_interval']; 

  // events per columns
  foreach($occurrences as $occurrence) {
    $iso = clone $occurrence->date;
    $iso->setHour(0)->setMinute(0)->setSecond(0);
    if($occurrence->event->all_day == 1) {
      $columns[$iso->getTimestamp()]['allday'][] = $occurrence;
    } else {
      $begin = clone $occurrence->date;
      $begin->setMinute($begin->getMinute() - ($begin->getMinute()%$time_unit))->setSecond(0);
      while($begin->compare($occurrence->end->getTimestamp()) < 0) {
        $columns[$iso->getTimestamp()][$begin->getIso()][] = $occurrence;
        $begin->addMinute($time_unit);
      }
    }
  }

  $updated = array();
  foreach($columns as $date => $column){
    $updated[$date] = check_conflict($column);
  }
  $pdf = new CalendarPdfDayAgenda('week', $updated, $calendar_entity, $calendar);
  $pdf->drawCalendar();
  $pdf->download();
}


/**
 * Calendar Day PDF
 * view agenda
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_day_agenda_pdf($calendar, $calendar_entity) {
  
  $date = $calendar['date'];
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);

  $category_filter = $calendar['category_filter'];
  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date,$end, $calendar_entity, $status, $category_filter);
  $id = $date->getTimestamp();
  $columns[$id] = array();

  $time_unit = 60 / $_SESSION['set_cal_interval']; 
  foreach($occurrences as $occurrence) {
    if($occurrence->event->all_day == 1) {
      $columns[$id]['allday'][] = $occurrence;
    } else {
      $begin = clone $occurrence->date;
      $begin->setMinute($begin->getMinute() - ($begin->getMinute()%$time_unit))->setSecond(0);
      while($begin->compare($occurrence->end->getTimestamp()) < 0) {
        $columns[$id][$begin->getIso()][] = $occurrence;
        $begin->addMinute($time_unit);
      }
    }
  }

  $updated = array();
  foreach($columns as $date => $column){
    $updated[$date] = check_conflict($column);
  }

  $pdf = new CalendarPdfDayAgenda('day', $updated, $calendar_entity, $calendar);
  $pdf->drawCalendar();
  $pdf->download();

}


/**
 * Calendar Month PDF
 * view list
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_month_list_pdf($calendar, $calendar_entity) {
  global $ccalendar_weekstart, $ccalendar_first_hour;

  $date = clone $calendar['date'];
  $date->setDay(1)->setHour($ccalendar_first_hour)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addMonth(1);

  $category_filter = $calendar['category_filter'];
  $status = array('ACCEPTED');

  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter, true);
  $pdf = new CalendarPdfList('month', $calendar, $calendar_entity, $occurrences);
  $pdf->drawCalendar();
  $pdf->download();

}


/**
 * Calendar Week PDF
 * view list
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_week_list_pdf($calendar, $calendar_entity) {
  global $ccalendar_first_hour, $ccalendar_weekstart;

  $date = clone $calendar['date'];
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);
  $category_filter = $calendar['category_filter'];
  $status = array('ACCEPTED');

  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter, true);
  $pdf = new CalendarPdfList('week', $calendar, $calendar_entity, $occurrences);
  $pdf->drawCalendar();
  $pdf->download();
}


/**
 * Calendar Day PDF
 * view list
 * 
 * @param 
 * @access public
 * @return void
 */

function dis_calendar_day_list_pdf($calendar, $calendar_entity) {
  global $ccalendar_first_hour;

  $date = $calendar['date'];
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);

  $category_filter = $calendar['category_filter'];
  $status = array('ACCEPTED');

  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter, true);

  $pdf = new CalendarPdfList('day', $calendar, $calendar_entity, $occurrences);
  $pdf->drawCalendar();
  $pdf->download();

}


/**
 * Calculate events conflicts 
 * 
 * @param 
 * @access public
 * @return void
 */
function check_conflict($column){
  //Evénement à mettre à jour
  $updated = array();
  foreach($column as $date => $cell) {
    //Positions utilisées dans le groupe d'événement en cours de traitement
    $usedPositions = array();
    //Position de l'événement en cours de traitement
    $position = 0;
    //Pour chaque case
    foreach ($cell as $key => $occurrence) {
      $event = $occurrence->event;
        // Coordonnée de l'événement en cours de traitement
        unset($coordinate);
        // Si cette événement n'est pas déjà taité
        if(!($coordinate = &$updated[$event->id .'-'.$occurrence->entity . '-' . $occurrence->id])) {
          // Si il n'y a aucune position utilisé, on définit l'unité de fractionnement à 100 % de la cellule
          if(empty($usedPositions))  {
            unset($unit);
            $unit = array('value' => 1);
          }
          // Si il y a des position utilisé, on cherche la première position libre
          while(isset($usedPositions[$position])) $position ++;
        
          // Par défaut la fin de l'événement = position (n'occupe qu'une seule unité de fractionnement de la céllule).
          unset($end);
          $end = array('value' => $position);
          // Si on est en train de traiter le dernier élément de la céllule
          if(count($cell) == ($key + 1)) {
            // Définis la taille occupé par l'événement comme étant le nombre de position libre a la suite
            // (en vérifiant que ce nombre est inféreur au nombre de position totale).
            while($end['value'] < $unit['value'] && !isset($usedPositions[$end['value']])) $end['value']++;
          }
          // Si l'événement s'étend jusqu'a la dernière position, on fait en sorte que si le nombre
          // de position libre augmente il s'agrandit également.
          if($end['value'] == $unit['value']) $end = &$unit;
          // On définis les coordonnées de l'événement
          unset($coordinate);
          $coordinate = array('position' => $position, 'unit' => &$unit, 'end' => &$end, 'occurrence' => $occurrence);
          $updated[$event->id .'-'.$occurrence->entity . '-' . $occurrence->id] = &$coordinate;
        }
        $usedPositions[$coordinate['position']] = true;        
        // On peuple Updated
        // Si la l'unité de fractionnement est inférieur au nombre d'événement dans la cellule, on augmente la taille de la cellule
        if(count($cell) > $unit['value']) $unit['value'] = count($cell);
        // Si un événement viens se placer après l'événement en cours on 'détache' la fin de l'événement
        // du nombre de position disponible
        if(($coordinate['position'] + 1) < count($cell) && ($key + 1) < count($cell)) {
          unset($coordinate['end']);
          $coordinate['end'] = array('value' => $coordinate['position'] + 1);      
  }
    }
  }
  unset($coordinate);
  $return = array();
  foreach($updated as $id => $coordinate) {
    $size = $coordinate['end']['value'] - $coordinate['position'];
    $coordinate['occurrence']->position = $coordinate['position'];
    $coordinate['occurrence']->size = $size / $coordinate['unit']['value'];
    $coordinate['occurrence']->unit = $coordinate['unit']['value'];
    $return[] = $coordinate['occurrence'];

  }

  return $return;
}


/**
 * CalendarPdf Class
 * 
 * @param 
 * @access public
 * @return void
 */
Class CalendarPdf{

  public $pdf, $page, $page_orientation, $margin;
  public $waiting_alpha, $class, $font, $font_size, $obm_logo;
  public $content_x0, $content_y0;
  public $content_x1, $content_y1;
  public $calendar_weekstart;
  public $tmp_filename;
  public $calendar, $calendar_entity;
  public $editor;

  function __construct($calendar, $calendar_entity){
    global $ccalendar_weekstart, $ico_obm_org, $path;

    // Page orientation
    $page_layout = $calendar['pagelayout'];

    if ($page_layout == 'portrait'){
      $page_size = explode(':', Zend_Pdf_Page::SIZE_A4);
      $this->page_orientation = 'A4';
    } else {
      $page_size = explode(':', Zend_Pdf_Page::SIZE_A4_LANDSCAPE);
      $this->page_orientation = 'A4-LANDSCAPE';
    }

    // Zend PDF
    $this->pdf = new Zend_Pdf();
    $this->margin = 30;

    // Page Coord.
    $page_x = $page_size[0];
    $page_y = $page_size[1];
    $this->content_x0 = $this->margin;
    $this->content_y0 = $this->margin;
    $this->content_x1 = $page_x - $this->margin;
    $this->content_y1 = $page_y - $this->margin; 

    // Calendar top (2*$this->margin = header height);
    $this->calendar_y1 = $this->content_y1 - 2*$this->margin; 

    // Style
    $this->font = Zend_Pdf_Font::fontWithName(Zend_Pdf_Font::FONT_HELVETICA_BOLD);
    $this->font_size = 6;
    $this->font_height = 1.25 * 6;
    $this->class = array(
      "eventOwner" => array("title" => "#2952A3", "background" => "#668CD9"),
      "eventOwner0" => array("title" => "#A32929", "background" => "#D86666"),
      "eventOwner1" => array("title" => "#5229A3", "background" => "#8C66D9"),
      "eventOwner2" => array("title" => "#28754E", "background" => "#65AD89"),
      "eventOwner3" => array("title" => "#88880E", "background" => "#BFBF4D"),
      "eventOwner4" => array("title" => "#B1440E", "background" => "#E6804D"),
      "eventOwner5" => array("title" => "#4E5D6C", "background" => "#8997A5"),
      "eventOwner6" => array("title" => "#6E6E41", "background" => "#A7A77D"),
      "eventOwner7" => array("title" => "#B1365F", "background" => "#E67399"),
      "eventOwner8" => array("title" => "#29527A", "background" => "#668CB3"),
      "eventOwner9" => array("title" => "#0D7813", "background" => "#4CB052"),
      "eventOwner10" => array("title" => "#AB8B00", "background" => "#E0C240"),
      "eventOwner11" => array("title" => "#865A5A", "background" => "#BE9494"),
      "eventOwner12" => array("title" => "#5A6986", "background" => "#94A2BE"),
      "eventOwner13" => array("title" => "#8D6F47", "background" => "#C4A883"),
      "eventOwner14" => array("title" => "#7A367A", "background" => "#B373B3"),
      "eventOwner15" => array("title" => "#1B887A", "background" => "#59BFB3"),
      "eventOwner16" => array("title" => "#528800", "background" => "#8CBF40"),
      "eventOwner17" => array("title" => "#BE6D00", "background" => "#F2A640"),
      "eventOwner18" => array("title" => "#705770", "background" => "#A992A9"),
      "eventOwner19" => array("title" => "#4A716C", "background" => "#85AAA5")
    ); 
    $this->waiting_alpha = 0.5;

    // FIXME : obm_logo path $ico_obm_org !!
    $this->obm_logo = Zend_Pdf_Image::imageWithPath("$path/../resources/themes/default/images/logo_obm_org.jpg");

    $this->calendar_weekstart = $ccalendar_weekstart;
    $this->tmp_filename = tempnam("/tmp", "obm_") ;

    $this->calendar = $calendar;
    $this->calendar_entity = $calendar_entity;

    $userinfo = get_entity_info($obm['uid'], 'user');
    $this->editor = $userinfo['label'];

  }


  /**
   * add a new page
   * with header & legend
   */
  function addPage($header=true, $legend=true) {
    $this->pdf->pages[] = $this->pdf->newPage($this->page_orientation);
    $this->page = $this->pdf->pages[sizeof($this->pdf->pages)-1];
    $this->page->setFont($this->font, $this->font_size);
    if ($header) $this->setHeader();
    if ($legend) $this->setLegend();
  }


  /**
   * draw pdf header at top left
   */
  function setHeader() {
    global $l_calendar, $l_category, $l_all_f, $obm;


    // OBM Logo
    $this->page->drawImage(
      $this->obm_logo,
      $this->content_x0,
      $this->content_y1-34,
      $this->content_x0+85,
      $this->content_y1+14);
    $this->page->setFillColor(new Zend_Pdf_Color_Html('#3D3D3D'));

    // PDF Title
    $this->page->drawText(
      "$l_calendar $this->display_date",
      $this->content_x0+90,
      $this->content_y1,
      'UTF-8');

    // Editor
    $this->page->drawText(
      $this->editor,
      $this->content_x0+90,
      $this->content_y1-$this->font_height,
      'UTF 8');

    // Date creation
    $today = new Of_Date();
    $today->getDateIso();
    $this->page->drawText(
      $today,
      $this->content_x0+90,
      $this->content_y1-2*$this->font_height,
      'UTF-8');

    // Category
    if (!empty($this->calendar["category_filter"])){
      $category = of_category_get_label("event", "category1", $this->calendar["category_filter"]);
    } else {
      $category = $l_all_f; 
    }
    $display_category = "$l_category: $category";
    $this->page->drawText(
      $display_category,
      $this->content_x0+90,
      $this->content_y1-3*$this->font_height,
      'UTF-8');
  }


  /**
   * draw pdf legend at bottom left
   */
  function setLegend(){
    $i=0;
    foreach($this->calendar_entity as $users) {
      foreach($users as $user){
        $username = $user["name"];
        $class = $this->class[$user["class"]];
        $this->page->setFillColor(new Zend_Pdf_Color_Html($class["title"]));
        $x0 = $this->content_x0 +$i;
        $y0 = $this->content_y0;
        $x1 = $x0 + 90;
        $y1 = $y0 + $this->font_height;
        $this->page->drawRectangle($x0+2 , $y0-1, $x1, $y1+1,Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        $this->page->drawText($username, $x0+4, $y0, 'UTF-8');
        $i+=90;
      }
    }
  }


  /**
   * set PDF metadata
   * available : Title, Author, Subject, Keywords,
   *             Creator, Producer, CreationDate,
   *             ModDate, Trapped
   *  
   */
  function setMetadata() {
    global $l_calendar, $l_obm_title, $obm_version;

    $this->pdf->properties['Title'] = $l_calendar;
    $this->pdf->properties['Subject'] = $this->display_date;
    $this->pdf->properties['Author'] = $this->editor;
    $this->pdf->properties['Creator'] = $this->editor;
    $this->pdf->properties['Producer'] = "$l_obm_title $obm_version";
    $this->pdf->properties['CreationDate'] = "D:".date('YmdHis');
  }


  /**
   * draw page number at bottom right
   */
  function setPageNb() {
    $total = sizeof($this->pdf->pages);
    foreach($this->pdf->pages as $index => $page) {
      $cur = $index+1;
      $this->pdf->pages[$index]->setFillColor(new Zend_Pdf_Color_Html('#3D3D3D'));
      $this->pdf->pages[$index]->drawText("$cur/$total", $this->content_x1, $this->content_y0, 'UTF-8');
    }
  }


  /**
   * Download PDF
   */
  function download() {
    global $l_obm_title, $l_calendar;

    $filename = str_replace(" ", "_", "$l_obm_title $l_calendar $this->display_date.pdf");

    $this->setPageNb();
    $this->setMetadata();

    $this->pdf->save($this->tmp_filename);
    $file = fopen($this->tmp_filename, "r");
    header("Content-Type: application/pdf") ;
    header("Content-Disposition: attachment; filename=\"$filename\";");
    header("Cache-Control: maxage=3600");
    header('Pragma: public');
    fpassthru($file) ;
    fclose($file) ;
  
    unlink($this->tmp_filename);
  }

  /**
   * Return string width
   */
  function getStringWidth($string) {
    $drawingString = iconv('UTF-8', 'UTF-16BE//IGNORE', $string);
    $characters = array();
    for ($i = 0; $i < strlen($drawingString); $i++) {
      $characters[] = (ord($drawingString[$i++]) << 8) | ord($drawingString[$i]);
    }
    $glyphs = $this->font->glyphNumbersForCharacters($characters);
    $widths = $this->font->widthsForGlyphs($glyphs);
    $stringWidth = (array_sum($widths) / $this->font->getUnitsPerEm()) * $this->font_size;

    return $stringWidth;
  }

}


/**
 * CalendarPdfMonth Class
 * 
 * @param 
 * @access public
 * @return void
 */
Class CalendarPdfMonthAgenda Extends CalendarPdf{

  var $calendar_x0;
  var $calendar_y0;
  var $calendar_x1;
  var $calendar_y1;
  var $col_width;
  var $min_col_height;
  var $col_height;
  var $current_y;
  var $updated;
  var $daysofweek;
  var $calendar_first_hour;
  var $usedPosition;

  function __construct($calendar, $calendar_entity, $updated){
    global $l_daysofweekl, $ccalendar_first_hour;

    parent::__construct($calendar, $calendar_entity);

    $this->calendar_x0 = $this->content_x0 + 10;
    $this->calendar_y0 = $this->content_y0 + 18;
    $this->calendar_x1 = $this->content_x1;
    $this->col_width = ($this->calendar_x1 - $this->calendar_x0)/7;
    $this->min_col_height = ($this->calendar_y1 - $this->calendar_y0)/5;
    $this->col_height = $this->min_col_height;
    $this->current_y = 0;

    // Display date
    $this->display_date = $this->calendar['date']->localize(Of_Date::OBM_DATE_MONTH);

    $this->calendar_first_hour = $ccalendar_first_hour;
    $this->updated = $updated;
    $this->daysofweek = $l_daysofweek;
    $this->calendar_entity = $calendar_entity;

    $this->addPage();

  }


  /**
   * draw days label + background
   */
  function drawDays(){

    $this->page->setFillColor(new Zend_Pdf_Color_Html('#EEEEEE'));
    $this->page->drawRectangle(
    $this->calendar_x0,
    $this->calendar_y1+$this->font_height,
    $this->calendar_x1,
    $this->calendar_y1+2*$this->font_height+2,
    Zend_Pdf_Page::SHAPE_DRAW_FILL);

    $date = clone $this->calendar['date'];
    $date->setWeekday($this->calendar_weekstart)->setHour($this->calendar_first_hour)->setMinute(0)->setSecond(0);
    $end = clone $date;
    $end->addDay(7);
    $current = clone $date;

    $i=0;

    while($current->compare($end) < 0) {
      $day = $current->localize(Of_Date::WEEKDAY);
      $this->page->setFillColor(new Zend_Pdf_Color_Html('#3D3D3D'));
      $this->page->drawText(
        $day,
        $this->calendar_x0+$i*$this->col_width+$this->col_width/2-($this->getStringWidth($day)/2),
        $this->calendar_y1+$this->font_height+2,
        'UTF-8');
      $i++;
      $current->addDay(1);
    }
  }


  /**
   * draw calendar grid + events
   */
  function drawCalendar(){

    $date = clone $this->calendar['date'];
    $date->setDay(1)->addDay(1)->subWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
    $end = clone $this->calendar['date'];
    $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
    $current = clone $date;

    // days bar
    $this->drawDays();

    $i = 0;
    $w = 0;
    while($current->compare($end) < 0) {

      // Style
      if ($current->isToday()) {
        $bgcolor = new Zend_Pdf_Color_Html('#FDDAA4');
      } elseif ($current->compareMonth($this->calendar['date']) != 0) {
        $bgcolor = new Zend_Pdf_Color_Html('#EFF0F2');
      } else {
        $bgcolor = new Zend_Pdf_Color_Html('#FFFFFF');
      }

      // draw one week
      if ($current->compareWeekday($this->calendar_weekstart) == 0) {
        $nb_events=0;
        $end_week_time = $current->getTimestamp() + ((6 * 24 + $this->calendar_last_hour) * 3600);

        for($s=$current->getTimestamp();$s<=$end_week_time;$s+=86400){
          $nb_events = max($nb_events, sizeof($this->updated[$s])); // max events per day
        }

        $this->col_height = $this->font_height*($nb_events+2);
        if ($this->col_height< $this->min_col_height){
          $this->col_height = $this->min_col_height;
        }
        $this->current_y += $this->col_height;

        $endofday = $this->calendar_y1-$this->current_y;
        if ($endofday < ($this->content_y0+$this->font_height)) {
          $this->addPage();
          $this->drawDays();
          $this->current_y = $this->col_height;
        }

        // week num
        $week_num = $current->get(Of_Date::WEEK);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#368397'));
        $this->page->drawText(
          $week_num,
          $this->content_x0, 
          $this->calendar_y1-$this->current_y+$this->col_height-($this->col_height/2),
          'UTF-8');
        $w++;

      }

      // one day
      $this->page->setFillColor($bgcolor);
      $this->page->setLineColor(new Zend_Pdf_Color_Html('#DEDEDE'));
      $x0 = $this->calendar_x0+$i*$this->col_width;
      $y0 = $this->calendar_y1-$this->current_y;
      $x1 = $x0 + $this->col_width;
      $y1 = $y0 + $this->col_height;
      $this->page->drawRectangle($x0, $y0, $x1, $y1);
      // $this->page->drawLine($x1, $y0, $x1, $y1);

      // day label
      $day = $current->get(Of_Date::DAY);
      $this->page->setFillColor(new Zend_Pdf_Color_Html('#CCCCCC'));
      $this->page->drawRectangle(
        $this->calendar_x0+$i*$this->col_width,
        $this->calendar_y1-$this->current_y+$this->col_height+$this->font_height,
        $this->calendar_x0+$i*$this->col_width+$this->col_width,
        $this->calendar_y1-$this->current_y+$this->col_height-2,
        Zend_Pdf_Page::SHAPE_DRAW_FILL);
      $this->page->setFillColor(new Zend_Pdf_Color_Html('#368397'));
      $this->page->drawText(
        $day,
        $this->calendar_x0+$i*$this->col_width + 2,
        $this->calendar_y1-$this->current_y+$this->col_height,
        'UTF-8');

      $i++;

      $current->addDay(1);

      if ($current->compareWeekday($this->calendar_weekstart) == 0) {
        $i=0;
      }
    }

    $this->drawEvents();

  }


  function drawEvents() {

    $this->current_y = 0;
    $page = 0;
    $this->page = $this->pdf->pages[$page];
    $date = clone $this->calendar['date'];
    $date->setDay(1)->addDay(1)->subWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
    $end = clone $this->calendar['date'];
    $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
    $current = clone $date;

    $w = 0;
    while($current->compare($end) < 0) {
      if ($current->compareWeekday($this->calendar_weekstart) == 0) {
        $nb_events=0;
        $end_week_time = $current->getTimestamp() + ((6 * 24 + $this->calendar_last_hour) * 3600);

        for($s=$current->getTimestamp();$s<=$end_week_time;$s+=86400){
          $nb_events = max($nb_events, sizeof($this->updated[$s])); // max events per day
        }

        $this->col_height = $this->font_height*($nb_events+2);
        if ($this->col_height< $this->min_col_height){
          $this->col_height = $this->min_col_height;
        }
        $this->current_y += $this->col_height;

        $endofday = $this->calendar_y1-$this->current_y;
        if ($endofday < ($this->content_y0+$this->font_height)) {
          $page++;
          $this->page = $this->pdf->pages[$page];
          $this->current_y = $this->col_height;
        }

        // Draw current week events
        $day=0;
        $this_w = clone $current;
        $end_week_time = clone $current;
        $end_week_time->addDay(6);

        for($s=$this_w->getTimestamp();$s<=$end_week_time->getTimestamp();$s+=86400){
          if(is_array($this->updated[$s])){
            $this->addEvents($this->updated[$s], $day);
          }
          $day++;
        }
        $w++;

      }

      $current->addDay(1);
    }


  }


  /**
   * add an event
   */
  function addEvents($occurrences, $day){
    global $l_private, $obm;
    $cur = 0;

    foreach($occurrences as $occurrence){
      $title = "";
      $id = $occurrence->id;
      $color = $occurrence->event->color;
      $entity = $this->calendar_entity[$occurrence->entity][$id];
      $class = $this->class[$entity["class"]];
      $attendee = $occurrence->event->attendee;

      if ($occurrence->event->isMeeting()){
        $title = "ii ";
      }
      if (!$occurrence->event->all_day){
        $title .= $occurrence->date->get(Of_Date::TIME_SHORT).' - ';
      }

      if ($occurrence->event->privacy && !(in_array($obm['uid'],$attendee['user']))) {
        $title = $l_private;
      } else {
        $title .= $occurrence->event->title;
      }

      if (!empty($color) && sizeof($this->calendar_entity)==1) {
        $title_color = $color;
      } else {
        $title_color = $class["title"];
      }

      $curday = $occurrence->date->getDay();

      if (is_array($this->usedPosition[$curday])) {
        while (in_array($cur, $this->usedPosition[$curday])) {
          $cur++;
        }
      }

      $this->page->setFillColor(new Zend_Pdf_Color_Html($title_color));
      $x0 = $this->calendar_x0+$day*$this->col_width;
      $y0 = $this->calendar_y1-$this->current_y+$this->col_height-$this->font_height*$cur-2;
      $x1 = $x0+$this->col_width;

      if ($this->is_multi_days_event($occurrence)) {
        $begin = clone $occurrence->date;
        while($begin->compare($occurrence->end) < 0) {
          $x1 +=$this->col_width;
          $begin->addDay(1);
          if (!is_array($this->usedPosition[$begin->getDay()])) {
            $this->usedPosition[$begin->getDay()] = array();
          }
          array_push($this->usedPosition[$begin->getDay()], $cur);
        }
      }



      if ($occurrence->entity_state == "NEEDS-ACTION") {
        $this->page->setAlpha($this->waiting_alpha);
      } else {
        $this->page->setAlpha(1);
      }

      while($this->getStringWidth($title) > $x1-$x0) {
         $title = substr($title, 0, -1); 
      }
      if ($x1 > $this->calendar_x1) $x1 = $this->calendar_x1;

      $this->page->drawRectangle($x0,$y0,$x1,$y0-$this->font_height,Zend_Pdf_Page::SHAPE_DRAW_FILL);
      $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
      
      $this->page->drawText($title,
        $this->calendar_x0+$day*$this->col_width+2, 
        $y0-$this->font_height+1, 'UTF-8');
      $cur++;
    }
  }

  function is_multi_days_event($occurrence) { 
    $day_begin = $occurrence->date;
    $day_end = $occurrence->end;
    if ($day_begin->getDay() != $day_end->getDay() 
      && $day_end->get(Of_Date::TIME_SHORT) != "00:00") {
      return true;
    }

    return false;
  }

}


/**
 * CalendarPdfDay Class
 * 
 * @param 
 * @access public
 * @return void
 */
Class CalendarPdfDayAgenda Extends CalendarPdf{

  var $col_width;
  var $range;
  var $updated;
  var $calendar_entity;
  var $calendar_first_hour;
  var $calendar_last_hour;
  var $calendar_nb_hours;
  var $week_num;
  var $nb_out_events;
  var $nbcol;
  var $current_out_event;
  var $usedPosition;

  function __construct($range, $updated, $calendar_entity, $calendar){

    parent::__construct($calendar, $calendar_entity);

    // Page Coord.
    $this->calendar_x0 = $this->content_x0 + $this->margin;
    $this->calendar_y0 = $this->content_y0 + 2*$this->font_height;
    $this->calendar_x1 = $this->content_x1;

    // Calendar
    $this->updated = $updated;
    $this->range = $range;
    $this->calendar_first_hour = $calendar['cal_first_hour'];
    $this->calendar_last_hour = $calendar['cal_last_hour'];
    $this->calendar_nb_hours = $this->calendar_last_hour - $this->calendar_first_hour;
    $this->calendar_entity = $calendar_entity;

    $date = clone $calendar['date'];

    if ($this->range == 'week') {
      $begin = clone $date;
      $begin->setWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
      $end = clone $begin;
      $end->addDay(7);
      $this->display_date = $begin->localize(Of_Date::OBM_DATE_WEEK).' - '
        .$end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK);
    } else { 
      $end = clone $date;
      $end->addDay(1);
      $this->display_date = $date->localize(Of_Date::OBM_DATE_DAY);
    }

    $this->week_num = $date->get(Of_Date::WEEK); 

    $this->getNbOutEvents();
    $this->allday_height = (1+$this->nb_out_events) * $this->font_height;
    $this->onedayperpage = $calendar['onedayperpage'];

    if ($this->range == "week") {
      $display_days = $calendar['repeat_days'];
      $i=0;
      foreach($this->updated as $date => $column){
        if (!$display_days[$i]) unset($this->updated[$date]);
        $i++;
      }

      if ($this->onedayperpage){
        $this->nbcol = 1;
      } else {
        $this->nbcol = sizeof($this->updated);
      }

    } elseif ($this->range == "day") {
      $this->nbcol = 1;
    }

    $this->col_width = ($this->calendar_x1 - $this->calendar_x0)/$this->nbcol;
    $this->col_height = ($this->calendar_y1-$this->calendar_y0-$this->allday_height)/$this->calendar_nb_hours;

    $this->addPage();

  }


  /**
   * draw calendar
   */
  function drawCalendar() {

    $this->day_x=0;
    $this->drawGrid();
    $current = new Of_Date();

    foreach($this->updated as $date => $column){
      $this->current_out_event=0;

      $d = clone $current;
      $d->setTimestamp($date);

      // Day label
      if ($this->onedayperpage || $this->range == "day") {
        // long title
        $this_date_l = $d->localize(Of_Date::OBM_DATE_DAY);
      } else {
        // short title
        $this_date_l = $d->localize(Of_Date::OBM_DATE_WEEK_LIST);
      }

      $this->page->setFillColor(new Zend_Pdf_Color_Html('#368397'));
      $this->page->drawText($this_date_l, $this->calendar_x0+$this->day_x+10, $this->calendar_y1+2, 'UTF-8');

      // draw events
      $this->addEvents($column);
      $this->day_x+=$this->col_width;

      if ($this->onedayperpage && sizeof($this->pdf->pages)<sizeof($this->updated)) {
        $this->day_x = 0;
        $this->addPage();
        $this->drawGrid();
      }

    }
  }


  /**
   * draw calendar grid
   */
  function drawGrid(){

    // Cal. borders
    // ligth gray on hour label and day label
    $this->page->setFillColor(new Zend_Pdf_Color_Html('#F5F5F5'));
    $this->page->drawRectangle(
      $this->content_x0, $this->calendar_y0,
      $this->calendar_x0, $this->calendar_y1+$this->font_height*2,
      Zend_Pdf_Page::SHAPE_DRAW_FILL);
    $this->page->drawRectangle(
      $this->content_x0, $this->calendar_y1,
      $this->content_x1, $this->calendar_y1+$this->font_height*2,
      Zend_Pdf_Page::SHAPE_DRAW_FILL);

    // day label y separator
    $this->page->setLineColor(new Zend_Pdf_Color_Html('#DEDEDE'));
    $this->page->drawLine(
      $this->content_x0, $this->calendar_y1,
      $this->calendar_x1, $this->calendar_y1);

    // Display hours
    for($i=0;$i<=$this->calendar_nb_hours;$i++){

      // coords
      $y = $this->calendar_y0+$i*$this->col_height;

      // hour
      $this->page->setLineColor(new Zend_Pdf_Color_Html('#DEDEDE'));
      $this->page->drawLine($this->content_x0, $y, $this->calendar_x1, $y);

      // 1/2 hour
      if ($this->calendar_y0+$i*$this->col_height+$this->col_height/2 < $this->calendar_y1-$this->allday_height){
        $this->page->setLineColor(new Zend_Pdf_Color_Html('#EEEEEE'));
        $this->page->drawLine($this->calendar_x0-10, $y+$this->col_height/2, $this->calendar_x1, $y+$this->col_height/2);
      }
      // label
      if ($this->calendar_y0+$i*$this->col_height-$this->font_height > $this->calendar_y0){
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#3D3D3D'));
        $this->page->drawText($this->calendar_last_hour-$i.":00", $this->content_x0, $y-$this->font_height, 'UTF-8');
      }
    }

    // Display day y separator
    for($i=0;$i<=$this->nbcol;$i++){
      $this->page->setLineColor(new Zend_Pdf_Color_Html('#DEDEDE'));
      $this->page->drawLine(
        $this->calendar_x0+$i*$this->col_width, $this->calendar_y0,
        $this->calendar_x0+$i*$this->col_width, $this->calendar_y1);
    }

    // Display week num
    $this->page->setFillColor(new Zend_Pdf_Color_Html('#3D3D3D'));
    $this->page->drawText($this->week_num, $this->content_x0+2, $this->calendar_y1+2, 'UTF-8');

  }


  /**
   * add an event to the PDF
   */
  function addEvents($events) {
    global $l_private, $obm;

    foreach($events as $event){
      $id = $event->id;
      $all_day = $event->event->all_day;
      $entity = $this->calendar_entity[$event->entity][$id];
      $class = $this->class[$entity["class"]];
      $background = $class["background"];
      $duration = $event->event->duration;
      $date = $event->date->get(Of_Date::TIME_SHORT);
      $color = $event->event->color;
      $attendee = $event->event->attendee;
      
      if (!empty($color) && sizeof($this->calendar_entity)==1) {
        $bg_color = $color;
        $title_color = $color;
      } else {
        $bg_color = $class["background"];
        $title_color = $class["title"];
      } 

      if ($event->event->privacy && !(in_array($obm['uid'],$attendee['user']))) {
        $title = $l_private;
      } else {
        $title = $event->event->title;
      }

      if ($event->entity_state == "NEEDS-ACTION") {
        $this->page->setAlpha($this->waiting_alpha);
      } else {
        $this->page->setAlpha(1);
      }

      if ($all_day || $this->isOutOfTimeInterval($event)) {
        $x0 = $this->calendar_x0+$this->day_x;

        $curday = $event->date->getDay();
        if (is_array($this->usedPosition[$curday])) {
          while (in_array($this->current_out_event, $this->usedPosition[$curday])) {
            $this->current_out_event++;
          }
        }

        $x1 = $x0 + $this->col_width;
        $y0 = $this->calendar_y1-($this->current_out_event)*$this->font_height;
        $y1 = $y0 - $this->font_height;

        if ($this->isOutOfTimeInterval($event)) $title = $event->date->get(Of_Date::TIME_SHORT)." $title";

        // event width on multiple days
        // only if next days are displayed
        if (!$this->onedayperpage){
         $begin = clone $event->date;
         while($begin->compare($event->end) < 0) {
           $begin->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);
           if (array_key_exists($begin->getTimestamp(), $this->updated)) {
             if ($this->range != "day" && $event->end->getTimestamp() > $begin->getTimestamp()) {
               $x1 +=$this->col_width;
               if (!is_array($this->usedPosition[$begin->getDay()])) {
                 $this->usedPosition[$begin->getDay()] = array();
               }
               array_push($this->usedPosition[$begin->getDay()], $this->current_out_event);
             }
           }
         }
        }

        while($this->getStringWidth($title) > $x1-$x0) {
          $title = substr($title, 0, -1); 
        }
        $this->page->setFillColor(new Zend_Pdf_Color_Html($title_color));
        $this->page->drawRectangle($x0, $y0, $x1, $y1, Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        $this->page->drawText($title, $x0+2, $y1+1, 'UTF-8');
        $this->current_out_event++;

      } else {
        $margin = $event->position * $this->col_width/$event->unit;
        $x0 = $this->calendar_x0+$this->day_x+$margin;
        $x1 = $x0 + $this->col_width*$event->size;

        // place le début de l'événement à la minute près
        //$begin = $event->date->getHour()+$event->date->getMinute()/60; // ex 08h30 == 8.5

        $time_unit = 60 / $_SESSION['set_cal_interval']; 

        $datebegin = clone $event->date;
        $datebegin->setMinute($datebegin->getMinute() - ($datebegin->getMinute()%$time_unit))->setSecond(0);
        $begin = $datebegin->getHour()+$datebegin->getMinute()/60; 

        $dateend = clone $event->end;
        $dateend->setMinute($dateend->getMinute() + ($dateend->getMinute()%$time_unit))->setSecond(0);
        $end = $dateend->getHour()+$dateend->getMinute()/60; 
        if($end > $this->calendar_last_hour) $end = $this->calendar_last_hour;

        $y1 = $this->calendar_y1-$this->allday_height-($begin - $this->calendar_first_hour)*$this->col_height;
        $y0 = $y1-($end-$begin)*$this->col_height;

        if ($event->event->duration < 2700) {
          $dis_header = "$date $title";
          $dis_title = "";
        } else {
          $dis_header = $date;
          $dis_title = $title;
        }

        // event title
        $this->page->setFillColor(new Zend_Pdf_Color_Html($bg_color));
        $this->page->drawRectangle($x0,$y0,$x1,$y1,Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        $cell = new Zend_Pdf_Cell($this->page, Zend_Pdf_Cell::POSITION_LEFT,
                                  $x1 - $x0 - 2, ($y1-2*$this->font_height) - $y0);
        $cell->setLocation($x0+2, $y1-2*$this->font_height);
        $cell->addText($dis_title, Zend_Pdf_Cell::ALIGN_LEFT, 0, 'UTF-8');
        $cell->write();

        // event header
        while($this->getStringWidth($dis_header) > $event->size*$this->col_width) {
          $dis_header = substr($dis_header, 0, -1); 
        }
        $this->page->setFillColor(new Zend_Pdf_Color_Html($title_color));
        $this->page->drawRectangle($x0,$y1-$this->font_height-2,$x1,$y1,Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        $this->page->drawText($dis_header, $x0+2, $y1-$this->font_height, 'UTF-8');
      }

    }
  }


  /**
   * count max allday + out of time interval occurrence 
   */
  function getNbOutEvents() {
    foreach($this->updated as $date => $column){
      $allday = 0;
      foreach($column as $key => $occurrence){
  if($occurrence->event->all_day || $this->isOutOfTimeInterval($occurrence)){
          $allday++;
        }
      }
      $this->nb_out_events = max($this->nb_out_events, $allday);
    }
  }


  /**
   * 
   */
  function isOutOfTimeInterval($occurrence){ 
    if ($occurrence->event->all_day) return false;

    // out of time interval
    $hour_begin = $occurrence->date->getHour();
    if ($hour_begin < $this->calendar_first_hour || $hour_begin >= $this->calendar_last_hour) {
      return true;
    }

    // event on x days
    $day_begin = $occurrence->date->getDay();
    $d = clone $occurrence->date;
    $d->addTimestamp($occurrence->event->duration);
    $day_end = $d->getDay();

    if ($day_begin != $day_end) {
      return true;
    }

    return false;
  
  }
}


/**
 * CalendarPdfList Class
 * 
 * @param 
 * @access public
 * @return void
 */
Class CalendarPdfList Extends CalendarPdf{

  var $calendar_x0, $calendar_y0;
  var $calendar_x1, $calendar_y1;
  var $occurrences;

  function __construct($range, $calendar, $calendar_entity, $occurrences){
    global $ccalendar_first_hour;

    parent::__construct($calendar, $calendar_entity);

    $this->calendar_x0 = $this->content_x0;
    $this->calendar_y0 = $this->content_y0;
    $this->calendar_x1 = $this->content_x1;
    $this->occurrences = $occurrences;
    $this->calendar_entity = $calendar_entity;
    $date = $calendar['date'];
    $end = clone $date;

    if ($range == 'week') {
      $end->addDay(7);
      $this->display_date = $date->localize(Of_Date::OBM_DATE_WEEK).' - '.
        $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK);
    } else {
      $this->display_date = $date->localize(constant("Of_Date::OBM_DATE_".strtoupper($range)));
    }

    $this->onedayperpage = $calendar['onedayperpage'];
    $this->showdetails = $calendar['showdetails'];

    $this->addPage(true, false);
  }


  /**
   * draw calendar grid + events 
   */
  function drawCalendar(){

    $this->drawHeader();
    $ocs = $this->sortOccurrences();
    $d = new Of_Date();

    if (is_array($ocs)) {
      ksort($ocs);
      foreach($ocs as $key => $occurrence) {
        $d->setTimestamp($key);

        // background color
        if ($this->onedayperpage){
          $color = "#FFFFFF";
        } else {
          if ($i%2 == 0) {
            $color = "#DEDEDE";
          } else {
            $color = '#FFFFFF';
          }
          $i ++;
          if ($d->isToday()) {
            $color = '#F4F3C5';
          }
        } 

        $day_text_evt = $d->localize(Of_Date::OBM_DATE_MONTH_LIST);

        $col_height = (sizeof($occurrence) + 1)*$this->font_height;

        if ($this->showdetails) {           
          foreach($occurrence as $blocks) {
            $datas = array(
              ($blocks->event->repeat == 'none')?'':$blocks->event->repeat, 
              $blocks->event->category1,
              $blocks->event->location, 
              $blocks->event->description,
              (is_array($blocks->event->attendee['resource']))?implode(',',$blocks->event->attendee['resource']):'', 
              (is_array($blocks->event->attendee['user']))?implode(',',$blocks->event->attendee['user']):'',
              'dummy'); 
            $datas = array_filter($datas);
            $col_height += (sizeof($datas,COUNT_RECURSIVE) - 1  ) * $this->font_height;
          }
        }

  $endofday = $this->current_y-$col_height;
        if ($endofday<$this->content_y0+$this->font_height || $this->onedayperpage) {
          $this->addPage(true, false);
          $this->drawHeader();
          $this->current_y = $this->calendar_y1-$this->font_height-1 ;
          $endofday = $this->content_y0+$this->font_height;
        }

        // one day
        $this->page->setFillColor(new Zend_Pdf_Color_Html($color));
        $this->page->drawRectangle(
          $this->content_x0,
          $this->current_y+$this->font_height,
          $this->content_x1,
          $endofday,
          Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $this->page->setFillColor(new Zend_Pdf_Color_Html("#368397"));
        $this->page->drawText(
          $day_text_evt,
          $this->content_x0+2,
          $this->current_y+1,
          'UTF-8');

        // draw events
        $this->addEvents($occurrence);

        // new y
        $this->current_y -= $col_height +$this->font_height;

      }
    }
  }


  /**
   * Sort occurrences by date (cause repeat and no repeat events)
   */
  function sortOccurrences(){

    if (is_array($this->occurrences)) {
      foreach($this->occurrences as $occurrence) {
        $id = clone $occurrence->date;
        $id->setHour(0)->setMinute(0)->setSecond(0);
        if(!is_array($ocs[$id->getTimestamp()])) { $ocs[$id->getTimestamp()] = array();}
        array_push($ocs[$id->getTimestamp()],$occurrence);
      }
    }

    return $ocs;
  }


  /**
   * draw list header
   */
  function drawHeader(){
    global $l_date, $l_event;

    // calendar header
    $this->page->setFillColor(new Zend_Pdf_Color_Html("#C2C2C2"));
    $this->page->drawRectangle(
      $this->content_x0,
      $this->calendar_y1-1,
      $this->content_x1,
      $this->calendar_y1+$this->font_height+1,
      Zend_Pdf_Page::SHAPE_DRAW_FILL);

    $this->page->setFillColor(new Zend_Pdf_Color_Html("#3D3D3D"));
    $this->page->drawText($l_date, $this->content_x0+2, $this->calendar_y1+1, 'UTF-8');
    $this->page->drawText($l_event, $this->content_x0+150, $this->calendar_y1+1, 'UTF-8');

    $this->current_y = $this->calendar_y1-$this->font_height-1;

  }
  

  /**
   * add an event to the PDF
   */
  function addEvents($occurrences){
    global $obm, $l_private;
    global $l_daily, $l_weekly, $l_monthlybydate, $l_monthlybyday, $l_yearly;
    global $l_category,$l_datebegin, $l_dateend, $l_repeat,$l_attendees, $l_title, $l_location;;
    global $l_resources, $l_items, $l_desc;

    $sel_user = $this->calendar_entity['user'];
    $sel_user_id = array();
    if (is_array($sel_user)) {
      while(list($key, $val) = each($sel_user)) {
       $sel_user_id[$key] = $val['class'];
      }
    }

    $sel_res = $this->calendar_entity['resource'];
    $sel_res_id = array();
    if (is_array($sel_res)) {
      while(list($key, $val) = each($sel_res)) {
       $sel_res_id[$key] = $val['class'];
      }
    }

    $evt_y = $this->current_y;
    foreach($occurrences as $key => $occurrence){
      $event = $occurrence->event;
      $date = $occurrence->date;
      $title = $event->title;
      if($event->all_day) {
        $end = clone $occurrence->end;
        if($end->subSecond(1)->compareDateIso($date) != 0) {
          $diff = ceil($occurrence->end->diff($occurrence->date)/86400);
          $head =  $GLOBALS['l_all_day']. " ($diff $GLOBALS[l_days])";
        } else {
          $head = $GLOBALS['l_all_day'];
        } 
        $margin_left = $this->getStringWidth($head);
        $head .= ' - '.$title;
      } else {
        if($occurrence->end->compareDateIso($date) != 0) {
          $head = $occurrence->date->getOutputDateTime(). ' - '.$occurrence->end->getOutputDateTime();
        } else {
          $head = $occurrence->date->get(Of_Date::TIME_SHORT). ' - '.$occurrence->end->get(Of_Date::TIME_SHORT);
        }        
        $margin_left = $this->getStringWidth($head);
        $head .= ' - '. $title;
      }
      $end = clone $occurrence->end;
      $attendee = $event->attendee;
      $properties = $event->properties;
      $dis_attendees = array();
      $dis_people = "";
      $dis_res = "";
      $dis_resources = array();
      $separator = "";

      if ($event->privacy && !(in_array($obm['uid'],$attendee['user']))) {
        $title = $l_private;
      }

      if ($event->isMeeting()) {
        $title = "&& $title";
      }

      if ($event->isPeriodic()) {
        $title = "R $title";
      } 

      if (is_array($attendee['user']))
      foreach($attendee['user'] as $at_id => $at_name) {
        if (array_key_exists($at_id, $sel_user_id)) {
          $dis_people .= "$separator$at_name";
          $separator = ', ';
        }
        array_push($dis_attendees, $at_name);
      }
      if (is_array($attendee['contact']))
      foreach($attendee['contact'] as $at_id => $at_name) {
        if (array_key_exists($at_id, $sel_user_id)) {
          $dis_people .= "$separator$at_name";
          $separator = ', ';
        }
        array_push($dis_attendees, $at_name);
      }

      if(is_array($attendee['resource'])) {
        foreach($attendee['resource'] as $at_id => $at_name) {
          if (array_key_exists($at_id, $sel_res_id)) {
            $dis_res .= "$separator$at_name";
            $separator = ', ';
          }             
          array_push($dis_resources, $at_name);
        }
      }

      if ($properties != '') {
        $charset = mb_internal_encoding();
        $dom = new DOMDocument("1.0", "$charset");
        $item = $dom->saveXML()."$properties";
        $xml = parse_item($item);
        if (is_object($xml)) {
          foreach($xml->resource as $value) {
            if (!empty($value->item)) {
              if (!empty($value->property_value)) {
                 $block_property = "($value->property_name:$value->property_value)";
              } else {
                 $block_property = '';
              }
              $item_list .= "$value->res_name $block_property".
                nl2br($value->item);
            }
          }
        }
      }

      if ($evt_y-$key*$this->font_height < $this->content_y0+$this->font_height) {
        $this->addPage(true, false);
        $this->drawHeader();
        $evt_y = $this->current_y+$key*$this->font_height;
      }

      // Event title
      $this->page->setFillColor(new Zend_Pdf_Color_Html("#368397"));
      $cellt = new Zend_Pdf_Cell($this->page, Zend_Pdf_Cell::POSITION_LEFT,
                                 $this->calendar_x1-$this->getStringWidth($dis_people.$dis_res)
                                 - ($this->content_x0 + 169 - $margin_left));
      $cellt->setLocation($this->content_x0 + 169 - $margin_left,
                          $evt_y-($key-1)*$this->font_height);
      $cellt->addText($head, Zend_Pdf_Cell::ALIGN_LEFT, 0, 'UTF-8');
      $cellt->write();

      // Event attendees + resources
      $celld = new Zend_Pdf_Cell($this->page, Zend_Pdf_Cell::POSITION_LEFT,
                                 $this->getStringWidth($dis_people.$dis_res) + 10);
      $celld->setLocation($this->calendar_x1-$this->getStringWidth($dis_people.$dis_res),
                          $evt_y-($key-1)*$this->font_height);
      $celld->addText($dis_people.$dis_res, Zend_Pdf_Cell::ALIGN_LEFT, 0, 'UTF-8');
      $celld->write();

      // Add lines cut by each cell
      $evt_y -= max( $cellt->getLineNumber()*$this->font_height,
                     $celld->getLineNumber()*$this->font_height  );

      if ($this->showdetails) {
        $repeat = $event->repeat;
        if ($repeat == 'none') {
    $dis_repeat = '';
        } else {
    $dis_repeat = ${"l_$repeat"};
        }
        $location = $event->location;
        $category = $event->category1;
        $description = $event->description;
        
        $details = array(
          "$l_repeat" => "$dis_repeat", 
          "$l_category" => "$category",
          "$l_location" => "$location", 
          "$l_desc" => "$description",
          "$l_attendees" => $dis_attendees, 
          "$l_resources" => $dis_resources,
          "$l_items" => "$item_list"); 
        $details = array_filter($details);
        $details_height = (sizeof($details,COUNT_RECURSIVE) - 2 )*$this->font_height;

        // left bar
        $x0 = $this->content_x0+170;
        $y0 = $evt_y-$key*$this->font_height;
        $x1 = $x0+3;
        $y1 = $y0-$this->font_height-$details_height;

        $this->page->setFillColor(new Zend_Pdf_Color_Html("#B3B3B3"));
        $this->page->drawRectangle($x0, $y0, $x1, $y1, Zend_Pdf_Page::SHAPE_DRAW_FILL);

        // details list
        $this->page->setFillColor(new Zend_Pdf_Color_Html("#3D3D3D"));
        $i=0;
        foreach($details as $label => $value){
          $this->page->drawText(
            $label,
            $x0+5,
            $y0-$this->font_height - $i*$this->font_height,
            'UTF-8');
          if (is_array($value)){
            if (sizeof($value)==0) $i++;
            foreach($value as $array_value){
              $this->page->drawText(
                $array_value,
                $x0+80,
                $y0-$this->font_height-$i*$this->font_height,
                'UTF-8');
              $i++;
            }
          } else {
            $this->page->drawText(
              $value,
              $x0+80,
              $y0-$this->font_height-$i*$this->font_height,
              'UTF-8');
             $i++;
          }
        }
        $evt_y -= $i*$this->font_height;
      }
    }
  }

}

/**
 * Generate email field  
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_calendar_mail_field($emails, $class) {

  if(!empty($emails) && !is_array($emails)) {
    $emails = explode("\r\n",$emails);
  }

  $count = 0;
  if(is_array($emails)) {
    foreach($emails as $key => $email) {
      $email_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'calendarMailHome');return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>        
        <input name='tf_others_attendees[]' value=\"$email\" />
        </div>";
      $count ++;
    }
  }

  $email_block .= "
    <div class=\"multiple\">
    <a onclick=\"remove_element(this.parentNode,'calendarMailHome');return false\" href=\"\">
     <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
    </a>        
    <input name='tf_others_attendees[]' value='' />  
    </div>         
    ";  
  $add_button = "<a id='addMailButton' href='' onclick=\"add_email_field();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  
  $email_block = "
  <th class=\"$class[email]\" id='calendarMailLabel'>
  $GLOBALS[l_others_attendees]
  $add_button
  </th>
  <td id='calendarMailHome'>
  $email_block
  </td>
  ";

  return $email_block;
}
?>
