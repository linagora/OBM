<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
//   - $current_view        : CalendarView
///////////////////////////////////////////////////////////////////////////////

require_once(dirname(__file__)."/../document/document_display.inc");

function dis_calendar_calendar_view($calendar, &$current_view) {
  global $display, $path, $ccalendar_max_users, $obm;
  global $l_module_calendar;

  $date = $current_view->get_date();
  $p_view = $current_view->get_cal_view();
  $p_range = $current_view->get_cal_range();
  if ($p_range == 'custom') {
    $p_range = 'week';
    $current_view->set_cal_range('week');
  }

  $calendar_entity = $current_view->get_entities();

  $GLOBALS['js']['vars']['consts']['calendarUrl'] = $GLOBALS['path'].'/calendar/'.basename($_SERVER['SCRIPT_NAME']);
  $display_days = $_SESSION['set_cal_display_days'];
  if (empty($display_days)) $display_days = '1111111';

  $ndays = substr_count($display_days, 1);
  $GLOBALS['js']['vars']['consts']['daysByWeek'] = $ndays;
  if ($current_view->entities_count() == 0) {
    $current_view->set_users($obm['uid']);
    $calendar_entity = $current_view->get_entities();
  }

  $writable_entity = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
  );

  $entity_readable['group'] = run_query_get_allowed_groups();
  $entity_readable['group'] = $entity_readable['group']['entity'];
  $my_views = run_query_calendar_get_my_views();
  $calendar_function = 'dis_calendar_'.$p_range.'_'.$p_view;
  $display['features'] = html_calendar_planning_bar($calendar, $current_view, $calendar_entity, $entity_readable, $my_views, $p_view);
  $display['title'] = "<h1 id='calendarTitle' class=\"title\">$l_module_calendar : ".$GLOBALS["l_view_$p_view"].' '.$GLOBALS["l_range_$p_range"].' </h1>';
  if ($calendar['action'] != 'portlet') {
    $display['search'] = dis_calendar_view_bar($current_view, $calendar);
  }
  if (function_exists($calendar_function)) {
    $block = $calendar_function($current_view, $calendar_entity, $writable_entity);
    $block .= dis_calendar_quick_form($calendar_entity);
    $block .= dis_calendar_conflict_popup();
    $block .= dis_calendar_occurency_popup();
    $calendar_function_pdf = $calendar_function."_pdf";
    if (function_exists($calendar_function_pdf)) {
      $display['print_url'] = "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=pdf_export_form&amp;output_target=print";
    }
  } else {
    $block .= dis_calendar_month_planning($current_view, $calendar_entity, $writable_entity);
  }
  return $block;
}


/**
 *  dis_calendar_search_result
 * 
 * @param mixed $p_kind 
 * @access public
 * @return void
 */
function dis_calendar_search_result($calendar, $current_view) {
  global $obm;

  $calendar_entity = $current_view->get_entities();
  $readable_entity = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'read', $current_view->get_users())
  );
  $writable_entity = OBM_Acl_Utils::expandEntitiesArray(
      OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write', $current_view->get_users())
  );

  $events = null;

  if ($calendar['searchType'] == 'basic') {
    // Set date_begin == 1970-01-01 and date_end == today +5 years
    $begin = new Of_Date(0);
    $end = new Of_Date();
    $end->addYear(5);
    $pattern = format_field_for_solr_query($calendar['pattern']);
  } else {
    if ($calendar['date_begin'] == null) {
      $begin = new Of_Date(0);
    } else {
      $begin = clone $calendar['date_begin'];
    }
    $begin->setHour(0);
    $begin->setMinute(0);
    $begin->setSecond(0);

    if ($calendar['date_end'] == null) {
      $end = new Of_Date();
      $end->addYear(5);
    } else {
      $end = clone $calendar['date_end'];
    }
    $end->setHour(0);
    $end->setMinute(0);
    $end->setSecond(0);
    $end->addDay(1);
    
    $solrBegan = $begin->get(Of_Date::SOLR);
    $solrEnd = $end->get(Of_Date::SOLR);
    $pattern = "+(date:[$solrBegan TO $solrEnd] OR is:periodic)";

    if (isset($calendar['title']) && $calendar['title'] != '') {
      $pattern .= " title:(".format_field_for_solr_query($calendar['title']).')';
    }    
    if (isset($calendar['location']) && $calendar['location'] != '') {
      $pattern .= " location:(".format_field_for_solr_query($calendar['location']).')';
    }    
    if (isset($calendar['desc']) && $calendar['desc'] != '') {
      $pattern .= " description:(".format_field_for_solr_query($calendar['desc']).')';
    }   
    if (isset($calendar['owner']) && $calendar['owner'] != '' && $calendar['owner'] != $obm['uid']) {
      $pattern .= " owner:(".format_field_for_solr_query($calendar['owner']).')';
    }
    if (isset($calendar['attendees']) && $calendar['attendees'] != '') {
      $pattern .= " with:(".format_field_for_solr_query($calendar['attendees']).')';
    }   
    if (isset($calendar['tag']) && $calendar['tag'] != '') {
      $pattern .= " tag:($calendar[tag])";
    }
    if (isset($calendar['category']) && $calendar['category'] != $GLOBALS['c_all']) {
      $pattern .= " category:($calendar[category])";
    }
  }
  
  // OBMFULL-4430
  // Get private events only if we have write access to the calendar of the event owner
  // If this is triggered as part of a public calendar share, assume we don't have write access
  // as we absolutely cannot rely on Of_ACL!
  if ($obm['uid'] == 0) {
    $patternToSearch = $pattern . ' -is:private';
  } else {
    $patternToSearch = $pattern . ' -(is:private AND -ownerId:(' . implode(" OR ", $writable_entity['ids']) . '))';
  }

  $events = OBM_Search::search('event', $patternToSearch, 0, 10000, array());
  $GLOBALS['display']['search'] = dis_calendar_html_search_form($calendar, $calendar['pattern']);
  $GLOBALS['display']['search'] .= dis_back_to_calendar($action);

  $block = "<h1>$GLOBALS[l_search_result]</h1>";
  $block .= dis_calendar_list_view($current_view, $calendar_entity, $readable_entity, $begin, $end, $events, false);

  return $block;
}

function format_field_for_solr_query($string){
  $string = trim($string);
  $string = strtolower($string);
  $string = preg_replace('/([^\d\s][^\s])([0-9])/', '\1 \2', $string);
  $words = split(' ', $string);
  foreach ($words as $word){
    $formated_word = ( substr($word, -1) == '*' ) ? $word : $word.'*';
    $formated_string .= ( end($words) == $word ) ? $formated_word : $formated_word.' ';
  }
  return $formated_string;
}

/**
 * dis_calendar_view_bar 
 * 
 * @param mixed $p_kind 
 * @access public
 * @return void
 */
function dis_calendar_view_bar($current_view, $calendar) {
  global $ico_week_view, $ico_day_view, $ico_month_view, $ico_list_view;
  global $ico_week_view_current, $ico_day_view_current, $ico_month_view_current;
  global $ico_agenda_view,  $ico_planning_view, $ico_list_view_current;
  global $ico_agenda_view_current,  $ico_planning_view_current;
  global $l_cal_range, $l_cal_view;
  global $l_view_agenda_desc,  $l_view_planning_desc, $l_view_list_desc;
  global $l_view_week_desc, $l_view_day_desc, $l_view_month_desc;
  global $path;

  $p_kind = $current_view->get_cal_view();
  $p_range = $current_view->get_cal_range();

  if ($p_kind) {
    ${'ico_'.$p_kind.'_view'} = ${'ico_'.$p_kind.'_view_current'};
  }

  if ($p_range) {
    ${'ico_'.$p_range.'_view'} = ${'ico_'.$p_range.'_view_current'};
  }

  $search_form = dis_calendar_html_search_form($calendar);
  $navbar = dis_calendar_navbar($current_view);

  if ($p_kind == 'agenda') {
    $day_view = "href='javascript: void(0);' onclick='obm.calendarManager.showDay();'";
    $week_view = "href='javascript: void(0);' onclick='obm.calendarManager.showWeek();'";
    $month_view = "href='javascript: void(0);' onclick='obm.calendarManager.showMonth();'";
  } else {
    $day_view = "href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?cal_range=day\"";
    $week_view = "href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?cal_range=week\"";
    $month_view = "href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?cal_range=month\"";
  }

  $block = "
  <div id=\"calendarToolbar\">
    <div class=\"LF NM\" style=\"margin-left:10px;\">
      <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?cal_view=agenda\">
      <img title=\"$l_view_agenda_desc\" src=\"$ico_agenda_view\" alt=\"$l_day\"/> 
      </a>
      <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?cal_view=planning\">
      <img title=\"$l_view_planning_desc\" src=\"$ico_planning_view\" alt=\"$l_month\" />
      </a>
      <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?cal_view=list\">
      <img title=\"$l_view_list_desc\" src=\"$ico_list_view\" alt=\"$l_list\" />
      </a>  
    </div>
    <div id=\"calendarCalRange\" class=\"LF NM\" style=\"margin-left:10px;\">
      <a $day_view><img id='ico_day_view' title=\"$l_view_day_desc\" src=\"$ico_day_view\" alt=\"$l_day\" /></a>
      <a $week_view><img id='ico_week_view' title=\"$l_view_week_desc\" src=\"$ico_week_view\" alt=\"$l_week\" /></a>
      <a $month_view><img id='ico_month_view' title=\"$l_view_month_desc\" src=\"$ico_month_view\" alt=\"$l_month\" /></a>
    </div>
    $search_form
  </div>
  <p class=\"LC\"></p>
  $navbar 
  <script type=\"text/javascript\">
    if(!obm.vars.conf.displayRange) {
      $('calendarCalRange').setStyle('display','none');
     }
  </script>

  <p class=\"LC\"></p>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display tag legend portlet 
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_calendar_tag_legend($tags) {

  $block = "<h1>$GLOBALS[l_tags]</h1>";
  $tags_q = run_query_calendar_get_alltags();
  while($tags_q->next_record()) {
    if (in_array($tags_q->f('eventtag_id'), $tags)) {
      $block .= "<div class='tagLegend' style='background:".$tags_q->f('eventtag_color')."'>".$tags_q->f('eventtag_label')."</div>";
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display the navigation bar
// Parameters:
//   - $calendar: calendar parameters
//   - $calendar_entity : array of entities elements to display
//   - $entity_readable : array of readable entities (['user'], ['resource']
///////////////////////////////////////////////////////////////////////////////
function html_calendar_planning_bar($calendar, $current_view, $calendar_entity, $entity_readable, $my_views, $p_view) {
  global $action;
  global $output_target;

  $output_target = $calendar['output_target'];
  $block = html_calendar_minical($current_view);
  if ($p_view == 'planning') {
    $block.= html_calendar_legend();
  }
  $block .= html_calendar_displayed_calendars($p_view, $calendar_entity);
  if ($action != 'perform_meeting' && $output_target != 'print') {
    $block .= html_calendar_select_calendar_entity($p_view, $current_view, $calendar_entity, $entity_readable);
    $block .= html_calendar_view_selector($calendar, $current_view, $my_views);
  }

  return $block;
}

function html_calendar_legend() {
  global $l_meeting_legend;
  
  $colorTemplate = 'user';
  $script_range = 'obm.vars.conf.displayRange = true;';
  $cats1 = of_category_get_ordered('event', 'category1');
  $nb = count($cats1);
  $colors = random_color($nb);
  foreach($cats1 as $key => $cat_info) {
    $dis_legend .= "<li style=\"color:#fff;background-color:rgb($colors[$key])\">$cat_info[label]</li>";
  }
  # Adding legend color for event without category
  $dis_legend .= "<li style=\"color:#fff;background-color: rgb(99, 100, 102)\">{$GLOBALS['l_cat_none']}</li>";
  return "
    <div class=\"portlet\" id=\"calendarLegend\">
    <h1>$l_meeting_legend</h1>
    <ul>
      $dis_legend
    </ul>
    </div>";
}

function html_calendar_displayed_calendars($p_view, $calendar_entity) {
  global $ico_user, $ico_resource, $l_meeting_legend, $ico_contact;
  global $l_calendar_displayed, $l_resource_displayed,$c_all,$l_event_color_tt;
  global $ico_delete,$output_target,$l_color,$l_users,$l_events;
  global $mono_user;

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
  
  $script_range = 'obm.vars.conf.displayRange = true;';
  $l_legend = $l_calendar_displayed;
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
      $data_id = "data-$type-$id";
      $warn = "";
      $class = "class='colorChooser'";
      if ($type == 'contact') {
        $contact = OBM_Contact::get($id);
        if (!$contact->getCalendar()) {
          $warn = "<span class='NW' style='float:right'><img src='$GLOBALS[ico_warning]' title=\"$GLOBALS[l_calendar_not_available]\" /></span>";
          $class = "";
        }
      }

      $dis_legend .= "
        <li title=\"$tooltip\" class=\"$data[class]Legend\"><input $class name=\"class-$type-$id\" type=\"hidden\" /><img src=\"".${"ico_$type"}."\" alt=\"[$type]\" />
        <input value=\"$data_id\" id=\"$data_id\" name=\"sel_ent[]\" type=\"hidden\" />";
      if ($output_target != 'print') {
        $dis_legend .= "<img src=\"$ico_delete\"
        onclick=\"\$('$data_id').dispose(); $('calendarSelectForm').submit();\" onmouseover=\"this.style.cursor = 'pointer';\"/>";
      }
      $dis_legend .= "&nbsp;$data[name]&nbsp;$warn</li>";
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
//   - $current_view    : current calendar view
//   - $calendar_entity : array of entities elements to display
//   - $entity_readable : array of readable entities (['user'], ['resource'])
///////////////////////////////////////////////////////////////////////////////
function html_calendar_select_calendar_entity($new_action, $current_view, $calendar_entity, $entity_readable) {
  global $path, $obm, $c_all, $ccalendar_resource;
  global $ico_add, $popup_width, $popup_height,$ico_add;
  global $l_selection, $l_users,$l_resources, $l_validate,$l_category_filter;
  global $l_private_group_members, $l_public_group_members;
  global $l_group_members, $l_select_group,$l_back_mono, $l_private, $l_public;
  global $ico_group, $l_calendar, $l_add, $l_add_shared_calendar;

  $this_group = $current_view->get_group();
  $category_filter = $current_view->get_category();

  if (OBM_Acl::hasAllowedEntities($obm['uid'],'calendar','read')) {
    //-- Private Group selection
    if ($this_group == $c_all) {
      $sel_group = "<option selected=\"selected\">$l_select_group</option>";
    } else {
      $sel_group = "<option value=\"".basename($_SERVER['SCRIPT_NAME'])."?sel_category_filter=&amp;new_group=1&amp;group_view=$c_all&amp;sel_user_id[]=$obm[uid]\">$l_select_group</option>";
    }

    if(is_array($entity_readable['group']))
    foreach($entity_readable['group'] as $id => $group) {
      $g_id = $id;
      $g_priv = $group['privacy'];
      $g_name = $group['label'];
      if ($g_priv) {
        if ($g_id == $this_group) {
          $selected_s = "selected=\"selected\"";
        } else {
          $selected_s = '';
        }
        $sel_group .= "<option value=\"".basename($_SERVER['SCRIPT_NAME'])."?group_id=$g_id&amp;new_group=1\" $selected_s>$g_name</option>\n";
        $dis_mygroup = true;
      }
    }
    if($dis_mygroup) {
      $dis_mygroup = "<fieldset class=\"detail extra\">
      <legend>$l_private_group_members</legend>
      <select name=\"action\" class=\"calendarQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
      $sel_group
      </select>
      </fieldset>";
    }
    if ($this_group != $c_all && empty($selected_s)) {
      $g_public_name = get_last_group_text($this_group);
    } 

    $url_group = "$path/group/group_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget_text=f_entity_group.group_name&amp;ext_widget=f_entity_group.ext_group_id&amp;filter_entity=calendar&amp;ext_action=index&amp;ext_url=".urlencode($path.'/calendar/'.basename($_SERVER['SCRIPT_NAME']))."&amp;ext_target=calendar&amp;sel_privacy=__PUBLIC__";
    $group_add_button = "
      <a href=\"javascript: return false;\" 
      onclick=\"popup('$url_group', '');
    return false;\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>";

    $block_group = "
    <form name=\"f_entity_group\" action=\"\" onsubmit=\"return false;\">
    $dis_mygroup
    <fieldset class=\"detail extra\">
    <legend>$l_public_group_members $group_add_button</legend>
    <input type=\"text\" name=\"group_name\" value=\"$g_public_name\" id=\"group_name\" autocomplete=\"off\" />
    <input type=\"hidden\" name=\"ext_group_id\" id=\"ext_group_id\" />
    <div id=\"sel_group\">
    </div>    
    </fieldset>
    </form>";

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
      <input type=\"hidden\" name=\"group_view\" value=\"$c_all\" />
      <input type=\"hidden\" name=\"new_group\" value=\"1\" />
    </fieldset>";

    $user_complete = "new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_ent', 'user', {filter_entity:'calendar', filter_pattern:'read'});";
    $group_complete = "new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search', 'sel_group', 'group_name', {filter_entity:'calendar', filter_pattern:'read', mode: 'mono',
        locked: 'true', selectfunction:'sel_public_group(element, this.inputField.value)'});";
  }
  // loop through ['user'], ['resource']
  $count = 0;
  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
      $name = $data['name'];
      $data_id = "data-$type-$id";
      $div_id = "sel_ent-$data_id";
      $sel_ent .= "<input value=\"$data_id\" name=\"sel_ent[$count]\" type=\"hidden\" />
                   <input value=\"".$data['class']."\" name=\"sel_ent_class[$count]\" type=\"hidden\" />";
      $count++;
    }
  }

  if ($ccalendar_resource && OBM_Acl::hasAllowedEntities($obm['uid'],'resource','read')) {
    $url_resource = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_ent&amp;filter_entity=Resource";
    $resource_add_button = "    
      <a href=\"javascript: return false;\"
      onclick=\"popup('$url_resource','');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" /> </a>";
    $block_resource = "
    <fieldset class=\"detail extra\">
    <legend>
      $l_resources
      $resource_add_button
    </legend>
    <input type=\"text\" name=\"\" value=\"\" id=\"resource\" autocomplete=\"off\" />
    </fieldset>";

    $resource_complete = "
      new obm.AutoComplete.Search('$path/resource/resource_index.php?action=ext_search&ajax=1', 'sel_ent', 'resource', {filter_entity:'Resource'});";
  }
  $cats1 = of_category_get_ordered('event', 'category1');
  $sel_cat1 = of_category_html_select('event', 'category1', $cats1, $category_filter,'all_f',"onchange=\"this.form.submit()\"",'category_filter');

  // Contacts
  $block_contact = "<fieldset class='detail extra'>
      <legend>$GLOBALS[l_contacts]</legend>
      <input type='text' name='' value='' id='contactSearch' autocomplete='off' />
    </fieldset>";
  $contact_complete = "
    new obm.AutoComplete.Search('$GLOBALS[path]/contact/contact_index.php?action=ext_search_mail&fromMyContacts=1&withCalendar=1', 'sel_ent', 'contactSearch')";

  $block_entity = "
  $block_group
  <form action=\"".basename($_SERVER['SCRIPT_NAME'])."\" method=\"get\">
  $block_user
  $block_resource
  $block_contact
  $sel_ent
  <div id=\"sel_ent\">
  </div>
  <fieldset class=\"buttons\">
  <input type=\"submit\" value=\"$l_validate\" />
  <input type=\"hidden\" name=\"new_sel\" value=\"1\" />
  </fieldset>
  </form>";

  if(isset($_SESSION['set_cal_default_view'])) {
    $default_view_url_params = "set_cal_view_id=".$_SESSION['set_cal_default_view'];
    $dis_back_myview = "<a id=\"my_view\" href=\"".basename($_SERVER['SCRIPT_NAME'])."?$default_view_url_params\">$l_back_mono</a>";
  } else {
    $dis_back_myview = "<a id=\"my_view\" href=\"".basename($_SERVER['SCRIPT_NAME'])."?sel_category_filter=&amp;new_sel=1&amp;new_group=1&amp;group_view=$c_all&amp;sel_user_id[]=$obm[uid]&amp;set_cal_view_id=0\">$l_back_mono</a>";
  }    

  $block_entities = "
  <div id='calendarEntities'>
    <div>
      <img src='$GLOBALS[ico_user]'/>
    </div>
    <div>
      <img src='$GLOBALS[ico_group]'/>
    </div>
    <div>
      <img src='$GLOBALS[ico_contact]'/>
    </div>
  </div>";
  
  $link_add_shared_calendar = "
  <div class='C'>
    <a href='#' onclick='obm.popup.show(\"addSharedCalendar\");'>$l_add_shared_calendar</a>
  </div>";
  
  $popup_add_shared_calendar = "
  <div id='addSharedCalendar' class='obmPopup'>
      <h1>
      <a href='#' id='popup_form_close' class='closeSticky' onclick='return false;'>
      <img src='$GLOBALS[ico_popup_close]' alt='close' />
      </a>
      $l_add_shared_calendar
      </h1>
     <div>
      <form action='$path/calendar/calendar_index.php?action=add_shared_calendar' method='post'>
        <fieldset style='padding: 10px; border:0;'>
          URL : <input type='text' id='tf_shared_calendar_url' name='tf_shared_calendar_url' size='80' />
        </fieldset>
        <fieldset class='buttons'>
          <input class='closeSticky' type='submit' value='Valider' />
        </fieldset>
      </form>
     </div>
  </div>";

  $block = "
  <div class=\"portlet\" id=\"calendarSelection\">
    <h1>$l_selection</h1>
    <div>
    <form action=\"\">
    <fieldset class=\"detail extra\">
    <legend>$l_category_filter</legend>
      $sel_cat1 
    </fieldset>
    </form>
    $block_entity
    <div class='C'>
    $dis_back_myview
    </div>
    <fieldset class=\"detail extra\">
    <legend>$l_add</legend>
    $link_add_shared_calendar
    </fieldset>
   </div>
  </div>
  <script type=\"text/javascript\">
    obm.vars.images.update = '$ico_group';
    obm.initialize.chain(function () {
      // new Obm.TabbedPane($('calendarEntities'));
      $user_complete
      $group_complete
      $resource_complete
      $contact_complete
    });
  </script>
  $popup_add_shared_calendar";

  return $block;
}


/*
 * Portlet Mini Calendar
 */
function html_calendar_minical($current_view) {
  $date = $current_view->get_date();
  $view = $current_view->get_cal_view();
  $m = $date->getMonth()-1;
  $y = $date->getYear();
  if ($view != 'agenda') {
    $js = "obm.miniCalendar.draw($y, $m);";
    $date_range = $current_view->get_date_range();
    $begin = clone $date_range['begin'];
    $end = clone $date_range['end'];
    while($begin->compare($end) < 0){
      $id = $begin->getDateIso();
      $js .= "obm.miniCalendar.select('$id');";
      $begin->addDay(1);
    }
  }
  $block = "
  <div class='miniCal' id='obmMiniCalendar'>
  </div>
  <div id='obmLegend'></div>
  <script type='text/javascript'>
    obm.initialize.chain(function () {
      obm.miniCalendar = new Obm.MiniCalendar(\"$view\");
      $js
    });
  </script>";
  return $block;
}


/**
 * HTML for the view selector portlet
 *
 * @param $current_view current calendar view
 * @param $my_view user's views
 * @access public
 * @return HTML
*/
function html_calendar_view_selector($calendar, $current_view, $my_views) {

  $uid = $GLOBALS['obm']['uid'];

  $view_id = $current_view->get_id();
  if ($calendar['set_cal_view_id'] === 0) $view_id = 0;
  $default_view_id = $_SESSION['set_cal_default_view'];
  $display = 'none';
  if (isset($default_view_id)) {
    $default_view = $GLOBALS['l_delete_default_view'];
    $display = 'inline';
  } 
  while($my_views->next_record()) {
    $id = $my_views->f('obmbookmark_id');
    $label = $my_views->f('obmbookmark_label');
    if ($id == $view_id) {
      $selected = " selected='selected'";
      if ($id != $default_view_id) {
        $default_view = $GLOBALS[l_insert_default_view];
        $display = 'inline';
        $style_default = '';
      } else {
        $style_default = " default='default'";
      }
    } elseif ($id == $default_view_id) {
      $style_default = " default='default'";
      $selected = '';
    } else {
      $selected = '';
      $style_default = '';
    }
    
    $obmbookmarkproperty = "set_cal_view_id=$id";
    $sel_view .= "
    <option id='opt_$id' value=\"".basename($_SERVER['SCRIPT_NAME'])."?new_sel=0&amp;$obmbookmarkproperty\"$style_default$selected>$label</option>";
  }

  // Javascript Strings
  $l_js_confirm_delete_view = phpStringToJsString($GLOBALS['l_confirm_delete_view']);
  $l_js_delete_default_view = phpStringToJsString($GLOBALS['l_delete_default_view']);
  $l_js_insert_default_view = phpStringToJsString($GLOBALS['l_insert_default_view']);
  $l_js_no_sel_view = phpStringToJsString($GLOBALS['l_no_sel_view']);
  $l_js_no_sel_default_view = phpStringToJsString($GLOBALS['l_no_sel_default_view']);
  $l_js_conflict_view_label = phpStringToJsString($GLOBALS['l_conflict_view_label']);
  $l_js_view_save = phpStringToJsString($GLOBALS['l_view_save']);
  $l_js_confirm_default_view = phpStringToJsString($GLOBALS['l_confirm_default_view']);

  $block = "
  <div class='portlet' id='calendarSelView'>
    <h1>$GLOBALS[l_views]</h1>
    <div style='margin: 2px;'>
    <form action=\"".basename($_SERVER['SCRIPT_NAME'])."\" method='get'>
      <select id='sel_view' name='sel_view' style='width:12em;' onchange='change_view(this.options[this.selectedIndex].value);'>
        <option id='opt_choix' value=\"".basename($_SERVER['SCRIPT_NAME'])."?sel_category_filter=&amp;new_sel=1&amp;new_group=1&amp;group_view=$GLOBALS[c_all]&amp;sel_user_id[]=$uid&amp;set_cal_view_id=0\" >
          $GLOBALS[l_select_view]
        </option>
        $sel_view
      </select>
      <input type='hidden' id='view_id' name='view_id' value='$view_id' />
      <input type='hidden' id='default_view_id' name='default_view_id' value='$default_view_id' />
      <a href='javascript:void(0);' onclick='obm.calendarView.remove();return false;'>
        <img src='$GLOBALS[ico_delete]' alt='$GLOBALS[l_view_delete]' title='$GLOBALS[l_view_delete]' />
      </a>
      <a href='javascript:void(0);' onclick='obm.calendarView.show();return false;'>
        <img src='$GLOBALS[ico_add]' alt='$GLOBALS[l_view_save]' title='$GLOBALS[l_view_save]' />
      </a>
      <a href='javascript:void(0);' id='default' name='default' onclick='obm.calendarView.default_view();return false;' style='display: $display;'>$default_view</a>
    </form>

    </div>
  </div>
  <script type='text/javascript'>
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


/**
 * Display the HTML day view
 *
 * @param CalendarView $current_view : current calendar view
 * @param array $calendar_entity : entities to display ['user'] ['resource'] 
 * @param array $writable_entity : entities with write access
 */
function dis_calendar_day_agenda($current_view, $calendar_entity, $writable_entity) {

  $date = clone $current_view->get_date();
  $date->setHour(0)->setMinute(0)->setSecond(0);

  $end = clone $date;
  $end->addDay(1);

  $block = dis_calendar_html_agenda($current_view, $calendar_entity, $writable_entity, $date, $end, 1);

  return "<div id='calendarLayout'>$block</div>";
}

/**
 * Returns the number of displayed days in the week view, depending on the user preferences.
 */
function get_number_of_displayed_days_in_weekview() {
  $display_days = $_SESSION['set_cal_display_days'];
  
  return empty($display_days) ? 7 : substr_count($display_days, 1);
}

/**
 * Display the HTML week view
 *
 * @param CalendarView $current_view : current calendar view
 * @param array $calendar_entity : entities to display ['user'] ['resource'] 
 * @param array $writable_entity : entities with write access
 */
function dis_calendar_week_agenda($current_view, $calendar_entity, $writable_entity) {
  $ndays = get_number_of_displayed_days_in_weekview();
  $date = clone $current_view->get_date();
  $date->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);

  $end = clone $date;
  $end->addDay($ndays);

  $block = dis_calendar_html_agenda($current_view, $calendar_entity, $writable_entity, $date, $end, $ndays);

  return "<div id='calendarLayout'>$block</div>";
}


/**
 * Display the HTML month view
 *
 * @param CalendarView $current_view : current calendar view
 * @param array $calendar_entity : entities to display ['user'] ['resource'] 
 * @param array $writable_entity : entities with write access
 */
function dis_calendar_month_agenda($current_view, $calendar_entity, $writable_entity) {

  $date = clone $current_view->get_date();
  $date->setDay(1)->addDay(1)->subWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);

  $end = clone $current_view->get_date();
  $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);

  $block = dis_calendar_html_agenda($current_view, $calendar_entity, $writable_entity, $date, $end, 30);
  return "<div id='calendarLayout'>$block</div>";
}


/**
 * Display the calendar form:
 *   date interval selector
 *   view selector
 *   today link
 *   search field
 */
function dis_calendar_navbar($current_view) {
 
  $label = dis_calendar_navbar_label($current_view);

  if ($current_view->get_cal_view() == 'agenda') {
    $block = "<div id='calendarNavBar'>
    <a href='javascript: void(0);' onclick='obm.calendarManager.showPrev();'>
      <img src=\"$GLOBALS[ico_previous]\" alt=\"[$GLOBALS[l_previous]]\" /></a>
    <a href='javascript: void(0);' onclick='obm.calendarManager.showNext();'>
      <img src=\"$GLOBALS[ico_next]\" alt=\"[$GLOBALS[l_next]]\" /></a>
    <span class='lnk'>
      <a href='javascript: void(0);' 
        onclick='obm.calendarManager.showToday()'>$GLOBALS[l_today]</a>
    </span>
    <span id='navBarLabel' class='label'>$label</span>
    <span class='lnk'>
      <a href='javascript: void(0);'
        onclick='obm.calendarManager.reloadCalendar()'>$GLOBALS[l_refresh]</a> 
    </span>
    </div>";
  } else {
    $current = clone $current_view->get_date();
    $date = clone $current_view->get_date();
    switch($current_view->get_cal_range()) {
      case 'day':
        $url_prev = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->subDay(1)->getURL();
        $url_next = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->addDay(1)->getURL();
        $url_far_prev = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->subWeek(1)->getURL();
        $url_far_next = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->addWeek(1)->getURL();
        break;
      case 'week':
        $url_prev = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->subWeek(1)->getURL();
        $url_next = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->addWeek(1)->getURL();
        $url_far_prev = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->subMonth(1)->getURL();
        $url_far_next = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->addMonth(1)->getURL();
        break;
      case 'month':
        $url_prev = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->subMonth(1)->getURL();
        $url_next = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->addMonth(1)->getURL();
        $url_far_prev = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->subYear(1)->getURL();
        $url_far_next = "".basename($_SERVER['SCRIPT_NAME'])."?date=".$current->setDateIso($date)->addYear(1)->getURL();
        break;
    }
    $today = new Of_Date();
    $block = "<div id='calendarNavBar'>
      <a href='$url_prev'><img src=\"$GLOBALS[ico_previous]\" alt=\"[$GLOBALS[l_previous]]\" /></a>     
      <a href='$url_next'><img src=\"$GLOBALS[ico_next]\" alt=\"[$GLOBALS[l_next]]\" /></a>
      <span class='lnk'><a href='".basename($_SERVER['SCRIPT_NAME'])."?date=".$today->getURL()."'>$GLOBALS[l_today]</a></span>     
      <span id='navBarLabel' class='label'>$label</span>
    </div>";
  }

  return $block;
}


/**
 *
 * display navigation bar label
 */
function dis_calendar_navbar_label($current_view,$ndays=-1) {
  $range = $current_view->get_cal_range();
  $date = clone $current_view->get_date();
  if ($ndays == 1) $range = 'day';
  else if ($ndays == -1) {
    $ndays = get_number_of_displayed_days_in_weekview();
  }

  switch($range) {
    case 'day':
      $label = $date->localize(Of_Date::OBM_DATE_DAY);
      break;
    case 'week':
      $date->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
      $end = clone $date;
      $end->addDay($ndays);
      if ($date->compareMonth($end) == 0) {
        $label_begin = $date->localize(Of_Date::DAY_SHORT);
        $label_end = $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK_JUMP);
      } else {
        $label_begin = $date->localize(Of_Date::OBM_DATE_WEEK_JUMP);
        $label_end = $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK_JUMP);
      }
      if ($date->compareYear($end) == 0) {
        $label_end .= " ".$date->localize(Of_Date::YEAR);
      } else {
        $label_begin .= " ".$date->localize(Of_Date::YEAR);
        $label_end .= " ".$end->localize(Of_Date::YEAR);
      }
      $label = "$label_begin - $label_end";
      break;
    case 'month':
      $label = $date->localize(Of_Date::OBM_DATE_MONTH);
      break;
    case 'custom':
      $end = clone $date;
      $end->addDay($ndays);
      if ($date->compareMonth($end) == 0) {
        $label_begin = $date->localize(Of_Date::DAY_SHORT);
        $label_end = $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK_JUMP);
      } else {
        $label_begin = $date->localize(Of_Date::OBM_DATE_WEEK_JUMP);
        $label_end = $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK_JUMP);
      }
      if ($date->compareYear($end) == 0) {
        $label_end .= " ".$date->localize(Of_Date::YEAR);
      } else {
        $label_begin .= " ".$date->localize(Of_Date::YEAR);
        $label_end .= " ".$end->localize(Of_Date::YEAR);
      }
      $label = "$label_begin - $label_end";
      break;
  }
  return $label;
}


/**
 * Event search form
 * Basic and advanced form
 */
function dis_calendar_html_search_form($calendar, $p="") {
  global $obm;

  if ($_GET['action'] == 'search') {
    $pattern = stripslashes($calendar['pattern']);
    $title = stripslashes($calendar['title']);
    $owner = stripslashes($calendar['owner']);
    $location = stripslashes($calendar['location']);
    $desc = stripslashes($calendar['desc']);
    $attendees = stripslashes($calendar['attendees']);
    $category = $calendar['category'];
    $period = $calendar['period'];
    $tag = $calendar['tag'];
  }
  
  if ($owner == $obm['uid']) $owner = '';
  
  if (!empty($p)) $pattern = $p;
  if (!isset($period)) $period = 'today';
  if (!isset($calendar['searchType']) || $calendar['searchType'] == 'basic') {
    $basic = '';
    $advanced = 'none';
  } else {
    $basic = 'none';
    $advanced = '';
    $calendar['pattern'] = "$title $owner $location $description";
  }

  $cats1 = of_category_get_ordered('event', 'category1');
  $sel_cat1 = of_category_html_select('event', 'category1', $cats1, $category,'all_f',"",'category');

  $date_begin = new Of_Date();

  $today_d = clone $date_begin;
  $GLOBALS['js']['vars']['consts']['today_begin'] = $today_d->getInputDate();
  $GLOBALS['js']['vars']['consts']['today_end'] = $today_d->getInputDate();

  $tomorrow_b = clone $date_begin;
  $GLOBALS['js']['vars']['consts']['tomorrow_begin'] = $tomorrow_b->addDay(1)->getInputDate();
  $GLOBALS['js']['vars']['consts']['tomorrow_end'] = $tomorrow_b->getInputDate();

  $this_week_d = clone $date_begin;
  $GLOBALS['js']['vars']['consts']['this_week_begin'] = $this_week_d->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0)->getInputDate();
  $GLOBALS['js']['vars']['consts']['this_week_end'] = $this_week_d->addDay(6)->getInputDate();

  $this_next_week_d = clone $date_begin;
  $GLOBALS['js']['vars']['consts']['next_week_begin'] = $this_next_week_d->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0)->addWeek(1)->getInputDate();
  $GLOBALS['js']['vars']['consts']['next_week_end'] = $this_next_week_d->addDay(6)->getInputDate();

  $this_month_d = clone $date_begin;
  $GLOBALS['js']['vars']['consts']['this_month_begin'] = $this_month_d->setDay(1)->setHour(0)->setMinute(0)->setSecond(0)->getInputDate();
  $GLOBALS['js']['vars']['consts']['this_month_end'] = $this_month_d->addMonth(1)->subDay(1)->getInputDate();

  $this_next_month_d = clone $date_begin;
  $GLOBALS['js']['vars']['consts']['next_month_begin'] = $this_next_month_d->setDay(1)->setHour(0)->setMinute(0)->setSecond(0)->addMonth(1)->getInputDate();
  $GLOBALS['js']['vars']['consts']['next_month_end'] = $this_next_month_d->addMonth(1)->subDay(1)->getInputDate();

  $date_begin = $calendar['date_begin'];
  if (!$date_begin) {
    if (!$today_d) {
      $tf_date_begin = "";
    } else {
      $tf_date_begin = $today_d->getInputDate();
    }
  } else {
    $tf_date_begin = $date_begin->getInputDate();
  }
  $date_end = $calendar['date_end'];
  if (!$date_end) {
    if (!$today_d) {
      $tf_date_end = "";
    } else {
      $tf_date_end = $today_d->getInputDate();
    }
  } else {
    $tf_date_end = $date_end->getInputDate();
  }

  // Tag selection
  $tags_q = run_query_calendar_get_alltags($obm['uid']);
  $sel_tags = "<select id='sel_tag' name='sel_tag'>
    <option value='".$GLOBALS['cnone']."'>$GLOBALS[l_none]</option>";
  while($tags_q->next_record()) {
    $selected = "";
    if ($tags_q->f('eventtag_id') == $tag) $selected = "selected";
    $sel_tags .= "<option value='".$tags_q->f('eventtag_id')."' $selected>".$tags_q->f('eventtag_label')."</option>";
  }
  $sel_tags .= "</select>";

  $block = "<div id='searchHolder'>

    <div id='basicSearch' style='display:$basic;'>
      <form id='eventSearchForm' action='".basename($_SERVER['SCRIPT_NAME'])."' onsubmit='return checkSearchForm();'>
        <input type='text' id='tf_pattern' name='tf_pattern' value=\"$pattern\" />
        <input type='submit' value='$GLOBALS[l_search]' />
        <input type='hidden' name='searchType' value='basic' />
        <input type='hidden' name='action' value='search' />
        <a href=\"#\" onclick=\"toggleSearchForm('advancedSearch', 'basicSearch');\">$GLOBALS[l_advanced_search]</a>
      </form>
    </div>

    <div id='advancedSearch' style='display:$advanced;'>
      <div class='header'>
        <span class='link'><a href=\"#\" onclick=\"toggleSearchForm('basicSearch', 'advancedSearch');\">$GLOBALS[l_hide_advanced_search]</a></span>
        <span class='title'>$GLOBALS[l_advanced_search]</span>
      </div>
      <div class='content'>
      <form id='advancedEventSearchForm' action='".basename($_SERVER['SCRIPT_NAME'])."'>
        <table>
        <tr>
          <th><label for=\"tf_title\">$GLOBALS[l_title]</label></th>
          <td><input type='text' id='tf_title' name='tf_title' value=\"$title\" /></td>
          <th><label for=\"tf_owner\">$GLOBALS[l_owner]</label></th>
          <td><input type='text' id='tf_owner' name='tf_owner' value=\"$owner\" /></td>
          <th><label for=\"tf_location\">$GLOBALS[l_location]</label></th>
          <td><input type='text' id='tf_location' name='tf_location' value=\"$location\" /></td>
        </tr><tr>
          <th><label for=\"sel_category\">$GLOBALS[l_category]</label></th>
          <td>$sel_cat1</td>
          <th><label for=\"tf_desc\">$GLOBALS[l_desc]</label></th>
          <td><input type='text' id='tf_desc' name='tf_desc' value=\"$desc\" /></td>
          <th><label for=\"tf_attendees\">$GLOBALS[l_attendees]</label></th>
          <td><input type='text' id='tf_attendees' name='tf_attendees' value=\"$attendees\" /></td>
        </tr><tr>
          <th><label for=\"sel_tag\">$GLOBALS[l_tag]</label></th>
          <td>$sel_tags</td>
          <th><label for=\"tf_date_begin\">$GLOBALS[l_datebegin]</label></th>
          <td><input type='text' id='tf_date_begin' name='tf_date_begin' value='$tf_date_begin' class='datePicker' /></td>
          <td>
            <input type='radio' id='rd_today' name='rd_period' value='today' onclick='setAdvancedSearchDate(this);' ". ($period=="today" ? "checked" : "") .">
            <label for='rd_today'>$GLOBALS[l_today]</label>
          </td>
          <td>
            <input type='radio' id='rd_tomorrow' name='rd_period' value='tomorrow' onclick='setAdvancedSearchDate(this);' ". ($period=="tomorrow" ? "checked" : "") .">
            <label for='rd_tomorrow'>$GLOBALS[l_tomorrow]</label>
          </td>
        </tr><tr>
          <td></td>
          <td></td>
          <th><label for=\"tf_date_end\">$GLOBALS[l_dateend]</label></th>
          <td><input type='text' id='tf_date_end' name='tf_date_end' value='$tf_date_end' class='datePicker' /></td>
          <td>
            <input type='radio' id='rd_this_week' name='rd_period' value='this_week' onclick='setAdvancedSearchDate(this);' ". ($period=="this_week" ? "checked" : "") .">
            <label for='rd_this_week'>$GLOBALS[l_this_week]</label>
          </td>
          <td>
            <input type='radio' id='rd_next_week' name='rd_period' value='next_week' onclick='setAdvancedSearchDate(this);' ". ($period=="next_week" ? "checked" : "") .">
            <label for='rd_next_week'>$GLOBALS[l_next_week]</label>
          </td>
        </tr><tr>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td>
            <input type='radio' id='rd_this_month' name='rd_period' value='this_month' onclick='setAdvancedSearchDate(this);' ". ($period=="this_month" ? "checked" : "") .">
            <label for='rd_this_month'>$GLOBALS[l_this_month]</label>
          </td>
          <td>
            <input type='radio' id='rd_next_month' name='rd_period' value='next_month' onclick='setAdvancedSearchDate(this);' ". ($period=="next_month" ? "checked" : "") .">
            <label for='rd_next_month'>$GLOBALS[l_next_month]</label>
          </td>
        </tr>
        </table>

        <input type='submit' value='$GLOBALS[l_search]' />
        <input type='hidden' name='searchType' value='advanced' />
        <input type='hidden' name='action' value='search' />
        <input type='button' onclick=\"toggleSearchForm('basicSearch', 'advancedSearch');\" value='$GLOBALS[l_cancel]'>
      </form>
      </div>
    </div>
    </div>";

  return $block;
}

function dis_calendar_select_template_form($action, $calendar) {
  global $obm;
  
  $template_q = run_query_calendar_get_alltemplates($obm['uid']);
  $t_id = $calendar['template_id'];
  while ($template_q->next_record()) {
    $id = $template_q->f('eventtemplate_id');
    $name = $template_q->f('eventtemplate_name');
    $selected = "";
    if (isset($t_id) && $t_id == $id) {
      $selected = "selected=\"selected\"";
    }
    if (strlen($name) > 30) {
      $title = "title=\"$name\"";
    }
    $select.= "<option value=\"$id\" $title $selected>$name</option>";
  }
  return "
    <div id=\"templateSelect\">
      <form id=\"templateSelectForm\" action=\"".basename($_SERVER['SCRIPT_NAME'])."\">
        $GLOBALS[l_create_from_template]
        <select name=\"template_id\">
          <option value='$GLOBALS[c_none]'>$GLOBALS[l_undef]</option>
          $select
        </select>
        <input type=\"hidden\" name=\"action\" value=\"new\" />
        <input type=\"submit\" value=\"$GLOBALS[l_ok]\" />
      </form>
    </div>";
}


function dis_calendar_draw($current_view, $ndays) {

  $date = clone $current_view->get_date();
  $date->setHour(0)->setMinute(0)->setSecond(0);

  switch($current_view->get_cal_range()) {
    case 'day':
      $end = clone $date;
      $end->addDay(1);
      break;
    case 'week':
      $date->setWeekday($GLOBALS['ccalendar_weekstart']);
      $end = clone $date;
      $end->addDay($ndays);
      break;
    case 'month':
      $end = clone $date;
      $date->setDay(1)->addDay(1)->subWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
      $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
      break;
    case 'custom':
      $end = clone $date;
      $end->addDay($ndays);
      break;
  }

  $writable_entity = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($GLOBALS['obm']['uid'], 'calendar', 'write')
  );

  $calendar_entity = $current_view->get_entities();

  return dis_calendar_html_agenda($current_view, $calendar_entity, $writable_entity, $date, $end, $ndays, true);
}

/**
 * Tests if the currently logged in user can write in a given calendar.
 */
function can_write_in_calendar($calendar_entity) {
    $can_write_in_calendar = false;
    foreach ($calendar_entity as $type => $entity) {
      foreach ($entity as $id => $data) {
        if (OBM_Acl::canWrite($GLOBALS[obm][uid], 'calendar', $id)) {
            $can_write_in_calendar = true;
            break;
        }
      }
    }
    return $can_write_in_calendar;
}

/**
 * Display the HTML calendar view
 *
 * @param array $current_view current calendar view
 * @param array $calendar_entity : entities to display ['user'] ['resource'] 
 * @param array $writable_entity : entities with write access
 * @param Of_Date $date : date begin
 * @param int $display_days: nb days to display 
 */
function dis_calendar_html_agenda($current_view, $calendar_entity, $writable_entity, $date, $end, $display_days, $ajax=false) {
  if ($display_days > 14) {
    $block .= dis_calendar_html_month_agenda($current_view, $calendar_entity,$writable_entity, $date, $end, $ajax);
    return $block;
  }

  $category_filter = $current_view->get_category_filter();
  $week_num = $date->get(Of_Date::WEEK);
  $start_time = $date->getTimestamp();
  $action = $GLOBALS['action'];

  $width = round(100/$display_days, 2);
  $height = 20;
  $today_marker = "";
  $current = clone $date;
  $step = 0;
  $range = $current_view->get_cal_range();
  if ($range == 'day') {
    $day_js = "obm.calendarManager.showWeek";
  } else {
    $day_js = "obm.calendarManager.showDay";
  }

  // days header
  $dayContainer = array();
  while($current->compare($end) < 0) {
    $date_iso = $current->getDateIso();
    $ts = $current->getTimestamp();
    $week_day_left = $step*$width;
    $today = "";
    if ($current->isToday()) {
      $hour_marker = "<div id='hourMarker'></div>";
      $today_marker = "<div id='today' style='width:$width%;left:$week_day_left%;'></div>
        <div id='todayHourMarker' style='width:$width%;left:$week_day_left%;'></div>";
      $today = "background:#FFFFCC;";
    }
    if ($action == 'portlet') {
      $week_day_list .= "<td class='dayLabel'>".$current->localize(Of_Date::OBM_DATE_WEEK_LIST)."</td>";
    } else {
      $week_day_list .= "<td class='dayLabel'>
        <a href='javascript: void(0)' onclick='$day_js($ts);'>".$current->localize(Of_Date::OBM_DATE_WEEK_LIST)."</a>
      </td>";
    }
    $div_id = "dayContainer_{$step}_{$date_iso}";
    array_push($dayContainer, $div_id); 
    $week_day_event_list .= "
    <div id='$div_id' class='alldayContainer' style='height:$height%;width:$width%;left:$week_day_left%;$today'>
      <div id='allday_$date_iso' class='alldayCell'></div>
    </div>";
    $week_day_col .= "<div id='day_$date_iso' class='dayCol' style='left:$week_day_left%;'></div>";
    $current->addDay(1);
    $step++;
  }

  // calendar grid
  $current = clone $date;
  $ts = $current->getTimestamp();
  $current->setHour(0);
  $current->setMinute(0);
  $unit = 60/$_SESSION['set_cal_interval'];
  while($current->compareHour(24) < 0 && $current->compareDateIso($date) == 0) {
    $id = $current->getTimestamp() - $ts;
    $background = "";
    if($current->compareHour($GLOBALS['ccalendar_first_hour']) < 0 || $current->compareHour($GLOBALS['ccalendar_last_hour']) >= 0) {
      $background="background:#EEE;";
    }
    if($current->compareMinute(0) == 0) {
      if (Of_Date::getOption('timeformat') == '12H') {
        $label = "<span class='hourLabel' style='padding-right:2px;'>".$current->getOutputHour()."</span>";
      } else {
        $label = "<span class='hourLabel'>".$current->getOutputHour()."</span><span class='minuteLabel'>".$current->getMinute()."</span>";
      }
      $week_hour .= "<div class='hourFull' style='$background height:{$height}px;'>$label</div>";
      $week_time .= "<div id='time_$id' class='hourFull' style='$background height:{$height}px;'></div>";
    } else {
      $label = "";
      if ($current->compareMinute(30) == 0 && $_SESSION['set_cal_interval'] == 4) {
        $label = "<span class='hourLabel'></span><span class='minuteLabel'>".$current->getMinute()."</span>";
      }
      $week_hour .= "<div class='hourPartial' style='$background height:{$height}px;width:50%;margin-left:50%;'>$label</div>";
      $week_time .= "<div id='time_$id' class='hourPartial' style='$background height:{$height}px;'></div>";
    }
    $current->addMinute($unit);
  }

  // Get events
  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date,$end,$calendar_entity, $status, $category_filter);
  $tags = array();  
  if (is_array($occurrences)) {
    foreach($occurrences as $occurrence) {
      if ($occurrence->event->tag) array_push($tags, $occurrence->event->tag);
      $dis_event .= dis_calendar_event($occurrence, 'hour', $calendar_entity, $writable_entity);
    }
  }

  $tag_legend = "";
  if (sizeof($tags) > 0 && $current_view->entities_count() == 1) {
    $tag_legend = html_calendar_tag_legend($tags);
  }

  $ndays = $_SESSION['set_cal_display_days'];
  if (empty($ndays)) $ndays = '1111111';

  $ndays = substr_count($ndays, 1);
  $day = "ico_day_view";
  $week = "ico_week_view";
  if ($display_days == 1) {
    $range = 'day';
    $day = 'ico_day_view_current';
  } else if ($display_days == $ndays) {
    $week = "ico_week_view_current";
  }

  if ($action == 'portlet') {
    $dis_week = $week_num;
    $dis_header_action = "";
    $dis_grid_action = "";
  } else {
    $dis_week = "<a href='javascript: void(0)' onclick='obm.calendarManager.showWeek($ts)'>$week_num</a>";

    $can_write_in_calendar = can_write_in_calendar($calendar_entity);
    if ($can_write_in_calendar) {
        $dis_grid_action = "
          onmousedown='obm.calendarManager.newDummyEvent(event);' 
          ondblclick='obm.calendarQuickForm.compute(event);'";
        $dis_header_action = " 
          onmouseover='if(obm.calendarManager) obm.calendarManager.newAlldayDummyEventHighlight(event);'
          onmousedown='obm.calendarManager.newAlldayDummyEvent(event);'
          onmouseup='obm.calendarManager.newAlldayDummyEvent(event);'
          ondblclick='obm.calendarQuickForm.compute(event);'";
    }
  }
  $block .= "
  <div id='calendarHeader'>
    <table class='calendarTable'>
      <colgroup style='background:#EFF0F2;text-align:right;'></colgroup>
      <colgroup></colgroup>
      <tr>
        <td class='hourPanel'></td>
        $week_day_list
      </tr>
      <tr>
        <td class='hourPanel' style='text-align:center;'>$dis_week</td>
        <td colspan='$display_days' style='border-right:1px solid #AAA;'>
          <div id='calendarHeaderGrid' $dis_header_action>
              $week_day_event_list
          </div>
        </td>
      </tr>
    </table>
  </div>
  <div id='calendarBody'>
    <table id='calendarBodyTable' class='calendarTable'>
    <tr>
      <td style='background:#EFF0F2;' class='hourPanel'>
          $week_hour
      </td><td>
        <div id='calendarGridContainer'>
          $today_marker
          <div id='calendarGrid' $dis_grid_action>
            $week_day_col
            $week_time
          </div>
        </div>
      </td>
    </tr>
    </table>
  </div>";

  $js = "
  obm.vars.consts.startTime = new Obm.DateTime($start_time * 1000);
  obm.calendarManager = new Obm.CalendarManager(".$date->setHour(0)->getTimestamp().", ".((Perm::get_module_rights('calendar') & $GLOBALS['cright_write']) == $GLOBALS['cright_write']?'true':'false').");
  $dis_event
  obm.calendarManager.redrawGrid();
  obm.calendarManager.redrawAllDayGrid();
  obm.calendarManager.forceRedraw = true;
  obm.calendarManager.resizeGrid();
  obm.calendarManager.scrollToFirstHour();
  if (\$chk($('ico_day_view'))) $('ico_day_view').setProperty('src', '$GLOBALS[$day]');
  if (\$chk($('ico_week_view'))) $('ico_week_view').setProperty('src', '$GLOBALS[$week]');
  if (\$chk($('ico_month_view'))) $('ico_month_view').setProperty('src', '$GLOBALS[ico_month_view]');
  obm.calendarManager.setLegendBar('".phpStringToJsString($tag_legend)."');
  obm.calendarManager.dayContainers = ".phpArrayToJsArray($dayContainer).";";

  if ($ajax) {
    $view = $current_view->get_cal_view();
    $title = phpStringToJsString("$GLOBALS[l_module_calendar] : ".$GLOBALS["l_view_$view"].' '.$GLOBALS["l_range_$range"]);
    $label = dis_calendar_navbar_label($current_view, $display_days);
    return "$block
      <script type=\"text/javascript\">
        obm.vars.consts.calendarRange = '$range';
        obm.vars.consts.cellWidth = '$width';
        $('calendarTitle').innerHTML = '$title'
        $js
        $('navBarLabel').innerHTML = '".phpStringToJsString($label)."';
      </script>";
  }

  return "$block
  <script type=\"text/javascript\">
    obm.vars.consts.calendarRange = '$range';
    obm.vars.consts.cellWidth = '$width';
    obm.vars.consts.nbDisplayedDays = '$display_days';
    obm.vars.labels.newEvent = '".phpStringToJsString($GLOBALS['l_header_new_event'])."';
    obm.vars.images.resize = '$GLOBALS[ico_resize]';
    obm.vars.images.periodic = '$GLOBALS[ico_periodic]';
    obm.vars.images.meeting = '$GLOBALS[ico_meeting]';
    obm.vars.images.private = '$GLOBALS[ico_private]';
    obm.vars.images.all_day = '$GLOBALS[ico_allday]';
    obm.vars.images.extension_left = '$GLOBALS[ico_extension_left]';
    obm.vars.images.extension_right = '$GLOBALS[ico_extension_right]';
    obm.vars.consts.timeUnit = (3600/$_SESSION[set_cal_interval]);    
    obm.vars.consts.userStyle = '".$current_view->get_class($GLOBALS[obm][uid], 'user')."'; 
    obm.vars.consts.userId = '".$GLOBALS['obm']['uid']."';
    obm.vars.consts.calendarUrl = '".$GLOBALS[path].'/calendar/'.basename($_SERVER['SCRIPT_NAME'])."';
    obm.vars.consts.calendarDetailconsultExtURL = '".$GLOBALS[path]."/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsultExt&calendar_id='; 
    obm.vars.consts.calendarDetailconsultURL = '".$GLOBALS[path]."/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&calendar_id='; 
    obm.vars.consts.action = '".$GLOBALS['action']."';
    obm.initialize.chain(function () {
      obm.vars.consts.oldDate = new Obm.DateTime($start_time * 1000);
      $js
      obm.calendarManager.setObserver();
      // window.addEvent('keydown', obm.calendarManager.keyboardListener.bindWithEvent(this));
    });
  </script>";
}


/**
 * Display the HTML calendar month view
 *
 * @param array $current_view current calendar view
 * @param array $calendar_entity : entities to display ['user'] ['resource'] 
 * @param array $writable_entity : entities with write access
 * @param Of_Date $date : date begin
 * @param Of_Date $end : date end
 */
function dis_calendar_html_month_agenda($current_view, $calendar_entity,$writable_entity, $date, $end,$ajax=false) {
  $display_days = floor(($end->getTimestamp() - $date->getTimestamp())/86400);
 
  $category_filter = $current_view->get_category_filter();
  $status = array('ACCEPTED', 'NEEDS-ACTION');
  calendar_events_model($date,$end,$calendar_entity, $status, $category_filter);
  $of = &OccurrenceFactory::getInstance();
  $action = $GLOBALS['action'];

  $start_time = $date->getTimestamp();
  $ico_close = $GLOBALS['icons']['close'];
  $width = round(100/7, 2);
  $current = clone $date;
  $dis_week_time = "obm.vars.consts.weekTime = new Array();";
  $dis_weekbegin_time = "obm.vars.consts.weekBegin = new Array();";
  $tags = array();
  // calendar grid
  $weeks = 100/ceil((($end->getTimestamp() - $date->getTimestamp())/86400)/7);

  $d=0;
  $dayContainer = array();
  while($current->compare($end) < 0) {
    $step = 0;
    $week_day_event_list .= "<div class='monthRow' style='height:$weeks%'>";
    $endWeek = clone $current;
    $endWeek->addDay(7);

    $dis_week_time .= "obm.vars.consts.weekTime[".$current->getWeek()."] = new Array('".$current->getTimestamp()."','".$endWeek->getTimestamp()."');";
    $dis_weekbegin_time .= "obm.vars.consts.weekBegin.push(".$current->getTimestamp().");";
    $occurrences = $of->getPeriodOccurrences($current,$endWeek);

    if (is_array($occurrences)) {
      foreach($occurrences as $occurrence) {
        if ($occurrence->event->tag) array_push($tags, $occurrence->event->tag);
        $occurrence->index = $occurrence->date;
        $occurrence->left = false;
        $occurrence->right = false;

        // Left extension
        if ($occurrence->index->compare($current) < 0) {
           $occurrence->index = $current;
           $occurrence->left = true;
        }

        // Right extension
        if ($occurrence->end->compare($endWeek) > 0) {
           $occurrence->right = true;
        }

        $dis_event .= dis_calendar_event($occurrence, 'day', $calendar_entity, $writable_entity);
      }
    }

    while($current->compare($endWeek) < 0) {
      $today = "";
      if ($current->isToday()) {
        $today = "background:#FFFFCC;";
      } elseif ($current->compareMonth($current_view->get_date()) != 0) {
        $klass = 'downlight';
      } else {
        $klass = '';
      }
      $day = $current->localize(Of_Date::OBM_DATE_WEEK_JUMP);
      $tip_title = $current->localize(Of_Date::OBM_DATE_DAY);
      $date_iso = $current->getDateIso();
      $ts = $current->getTimestamp();
      $day_number = $current->format("N");
      $week_widget = "";
      if ( $day_number == 1 ) { // monday https://obmhost/calendar/calendar_index.php?cal_range=week&date=2012-04-30
	$week_url = basename($_SERVER['SCRIPT_NAME'])."?cal_range=week&date=".$current->format("Y-m-d");
	$week_widget = "<span class=\"week\"><a href=\"".$week_url."\">".$current->format("W")."</a></span>";
      }
      $div_id = "dayContainer_{$d}_{$date_iso}";
      array_push($dayContainer, $div_id); 
      $week_day_left = $step*$width;
      $more_title = "<h1>$tip_title <img id=\"close_$date_iso\" src=\"$ico_close\"/> </h1>";
      if ($action == 'portlet') {
        $dis_day = $day;
      } else {
        $dis_day = "<a href='javascript: void(0);' onclick='obm.calendarManager.showDay($ts);'>$day</a>";
      }
      $week_day_event_list .= "
        <div id='$div_id' class='alldayContainer $klass' style='height:$weeks%;width:$width%;left:$week_day_left%;$today'>
          <div id='dayMonthLabel_$date_iso' class='dayMonthLabel'>".$week_widget.$day."</div>
          <div id='allday_$date_iso' class='alldayCell'>
            <div id='more_{$date_iso}' class='calTip' title='$more_title' 
              style='text-align:center;position:absolute;display:none;width:100%;'></div>
          </div>
        </div>";
      $step++;
      $d++;
      $current->addDay(1);
    }
    $week_day_event_list .= "</div>";
  }

  $tag_legend = "";
  if (sizeof($tags) > 0 && $current_view->entities_count() == 1) {
   $tag_legend =  html_calendar_tag_legend($tags);
  }

  // days header
  $current = clone $date;
  $end = clone $date;
  $end->addDay(7);
  while($current->compare($end) < 0) {
    $week_day_list .= "<td class='dayLabel'>".$current->localize(Of_Date::WEEKDAY)."</td>";
    $current->addDay(1);
  }

  if ($action == 'portlet') {
    $dis_header_action = "";
  } else {
    $can_write_in_calendar = can_write_in_calendar($calendar_entity);
    if ($can_write_in_calendar) {
        $dis_header_action = "onmouseover='if(obm.calendarManager) obm.calendarManager.newAlldayDummyEventHighlight(event);'
          onmousedown='obm.calendarManager.newAlldayDummyEvent(event);'
          onmouseup='obm.calendarManager.newAlldayDummyEvent(event);'
          ondblclick='obm.calendarQuickForm.compute(event);'";
    }
  }

  $block = "
  <div id='calendarHeader' style='padding:0;'>
    <table class='calendarTable'>
      <tr class=''>
        $week_day_list
      </tr>
      <tr>
        <td colspan='7'>
          <div id='calendarHeaderGrid' style='height:200px' $dis_header_action>
            $week_day_event_list
          </div>
        </td>
      </tr>
    </table>
  </div>";

  $js = "
  obm.vars.consts.nbDisplayedDays = '$display_days';
  obm.vars.consts.currentMonth = new Obm.DateTime(".$current_view->get_date()->getTimestamp()."*1000);
  obm.vars.consts.startTime = new Obm.DateTime($start_time * 1000);
  obm.calendarManager = new Obm.CalendarManager(".$date->setHour(0)->getTimestamp().", ".((Perm::get_module_rights('calendar') & $GLOBALS['cright_write']) == $GLOBALS['cright_write']?'true':'false').");
  $dis_event
  obm.calendarManager.redrawAllDayGrid();
  obm.calendarManager.forceRedraw = true;
  $('ico_day_view').setProperty('src', '$GLOBALS[ico_day_view]');
  $('ico_week_view').setProperty('src', '$GLOBALS[ico_week_view]');
  $('ico_month_view').setProperty('src', '$GLOBALS[ico_month_view_current]');
  obm.calendarManager.setLegendBar('".phpStringToJsString($tag_legend)."');
  obm.calendarManager.dayContainers = ".phpArrayToJsArray($dayContainer).";";

  if ($ajax) {
    $title = phpStringToJsString("$GLOBALS[l_module_calendar] : ".$GLOBALS["l_view_agenda"].' '.$GLOBALS["l_range_month"]);
    $label = dis_calendar_navbar_label($current_view);
    return "$block
      <script type=\"text/javascript\">
        obm.vars.consts.calendarRange = 'month';
        obm.vars.consts.cellWidth = '$width';
        obm.vars.labels.more = '".phpStringToJsString($GLOBALS['l_more'])."';
        $dis_week_time
        $dis_weekbegin_time
        $js
        $('calendarTitle').innerHTML = '$title';
        $('navBarLabel').innerHTML = '".phpStringToJsString($label)."';
      </script>";
  }

  return "$block
    <script type=\"text/javascript\">
    obm.vars.consts.calendarRange = 'month';
    obm.vars.consts.cellWidth = '$width';
    obm.vars.labels.newEvent = '".phpStringToJsString($GLOBALS['l_header_new_event'])."';
    obm.vars.images.resize = '$GLOBALS[ico_resize]';
    obm.vars.images.periodic = '$GLOBALS[ico_periodic]';
    obm.vars.images.meeting = '$GLOBALS[ico_meeting]';
    obm.vars.images.private = '$GLOBALS[ico_private]';
    obm.vars.images.all_day = '$GLOBALS[ico_allday]';
    obm.vars.images.extension_left = '$GLOBALS[ico_extension_left]';
    obm.vars.images.extension_right = '$GLOBALS[ico_extension_right]';
    obm.vars.consts.timeUnit = (3600/$_SESSION[set_cal_interval]);    
    obm.vars.consts.userStyle = '".$current_view->get_class($GLOBALS[obm][uid], 'user')."'; 
    obm.vars.consts.userId = '$GLOBALS[obm][uid]';
    obm.vars.consts.calendarUrl = '".$GLOBALS[path].'/calendar/'.basename($_SERVER['SCRIPT_NAME'])."';
    obm.vars.consts.calendarDetailconsultExtURL = '".$GLOBALS[path]."/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsultExt&calendar_id='; 
    obm.vars.consts.calendarDetailconsultURL = '".$GLOBALS[path]."/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&calendar_id='; 
    obm.vars.consts.action = '".$GLOBALS['action']."';
    obm.vars.labels.more = '".phpStringToJsString($GLOBALS['l_more'])."';
    $dis_week_time
    $dis_weekbegin_time
    obm.initialize.chain(function () {
      $js
      obm.calendarManager.setObserver();
      // window.addEvent('keydown', obm.calendarManager.keyboardListener.bindWithEvent(this));
    });
  </script>";
}


/**
 * Display Event List over one week
 * 
 * @param CalendarView $current_view : current calendar view
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_day_list($current_view, $calendar_entity, $writable_entity) {
  global $ccalendar_weekstart, $ccalendar_first_hour, $ccalendar_last_hour;

  $date = clone $current_view->get_date();
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);
  $block = dis_calendar_list_view($current_view, $calendar_entity, $writable_entity, $date, $end);

  return $block;
}

/**
 * Display Event List over one week
 * 
 * @param CalendarView $current_view : current calendar view
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_week_list($current_view, $calendar_entity, $writable_entity) {
  global $ccalendar_weekstart, $ccalendar_first_hour, $ccalendar_last_hour;

  $date = clone $current_view->get_date();
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(6);
  $block = dis_calendar_list_view($current_view, $calendar_entity, $writable_entity, $date, $end);

  return $block;
}


/**
 * Display Event List over one month
 * 
 * @param CalendarView $current_view : current calendar view
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_month_list($current_view, $calendar_entity, $writable_entity) {
  
  $date = clone $current_view->get_date();
  $date->setDay(1)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addMonth(1);
  $block = dis_calendar_list_view($current_view, $calendar_entity, $writable_entity, $date, $end);

  return $block;
}


/**
 * Display Event List
 * 
 * @param mixed $calendar_entity 
 * @return event list HTML code
 */
function dis_calendar_list_view($current_view, $calendar_entity, $writable_entity, $date, $end, $events=null, $occurences=true) {
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
  
  $sel_contact = $calendar_entity['contact'];
  $sel_contact_id = array();
  if (is_array($sel_contact)) {
    while(list($key, $val) = each($sel_contact)) {
     $sel_contact_id[$key] = $val['class'];
    }
  }

  foreach ($calendar_entity as $type => $entity) {
    foreach ($entity as $id => $data) {
       
    }    
  }
  
  $category_filter = $current_view->get_category_filter();

  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date, $end,$calendar_entity, $status, $category_filter,true,$events,null,$occurences);
  $today_mark = false;
  $charset = mb_internal_encoding();
  $target = "";
  if ($GLOBALS['action'] == 'portlet') $target = "target='_blank'";

  // Sort occurrences by date (cause repeat and no repeat events)
  if (is_array($occurrences)) {
    foreach($occurrences as $occurrence) {
      $id = clone $occurrence->date;
      $id->setHour(0)->setMinute(0)->setSecond(0);
      if(!is_array($ocs[$id->getTimestamp()])) { $ocs[$id->getTimestamp()] = array();}
      array_push($ocs[$id->getTimestamp()],$occurrence);
    }
  }
  if (is_array($ocs)) {
    ksort($ocs);
    foreach($ocs as $key => $occurrence) {
      $current = new Of_Date($key);  
      if ($i%2 == 0) $klass = "pair"; else $klass = '';
      $i ++;
      $label = $current->localize(Of_Date::OBM_DATE_DAY);
      if ($current->isToday()) $klass="highlight";
      $dis_events .= "
        <tr class=\"$klass\">
          <td><a $target href=\"".basename($_SERVER['SCRIPT_NAME'])."?cal_range=day&amp;date=".$current->getURL()."\">$label</a></td>
          <td>";

      foreach($occurrence as $k => $v) {
        $event = $v->event;
        $date = $v->date;
        $hour = $v->date->get(Of_Date::TIME_SHORT);
        $end = clone $v->end;
        $hour_end = $end->get(Of_Date::TIME_SHORT);
        $ev_id = $event->id;
        $title = $event->title;
        $tags_q = run_query_calendar_get_alltags($event->owner, $event->tag);
        $tags_q->next_record();
        $tag = $tags_q->f('eventtag_label');
        $item_list = phpStringToJsString(str_replace("\n\r", ', ', $item_list));
        $location = $event->location;
        $class = '';
        $needs_action = '';
        if ($v->entity_state == 'NEEDS-ACTION') {
          $class = 'needsAction';
          $needs_action = "<i> - ".$GLOBALS['l_wait']."</i>";
        }
        $attendee = $event->attendee;
        $category = $event->category1;
        $repeat = $event->repeat;
        if ($repeat == 'none') {
          $dis_repeat = '';
        } else {
          $dis_repeat = ${"l_$repeat"};
        }
        
        $privatized_event = $event->privacy == 1 && !(array_key_exists($obm['uid'],$attendee['user']));
        if ($privatized_event) {
          $title = $l_private;
          $description = $title;
        } else {
          $title = htmlspecialchars($event->title);
          if($location != '') $title .= " ($l_location : ".htmlspecialchars($location).")";
          $description = $event->description;
          $properties = $event->properties;
          $item_list = '';

          if ($properties != '') {
            $item = "<?xml version='1.0' encoding='$charset' ?".">$properties";
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
            $title = "<img src=\"$ico_private_alt\" alt=\"[Private]\" /> ".$title;
          }
          if($event->isMeeting()) {
            $title = "<img src=\"$ico_meeting_alt\" alt=\"[Meeting]\" /> ".$title;
          }
          if ($event->isPeriodic()) {
            $title = "<img src=\"$ico_periodic_alt\" alt=\"[Periodic]\" /> ".$title;
          }                  
          if ($event->isAllDay()) {
            $title = "<img src=\"$ico_allday_alt\" alt=\"[All Day]\" /> ".$title;
          }                  
        }
        $dis_owner = $event->owner_name;
        $dis_attendees = '';
        $br = '';
        $dis_people = '';
        $separator = '';
        $single = true;
        if (is_array($attendee['user'])) {
          foreach($attendee['user'] as $at_id => $at_name) {
            if($at_id != $obm['uid']) $single = false;
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
            $single = false;
            if (array_key_exists($at_id, $sel_contact_id)) {
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
            $single = false;
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
        if($category) $dis_category = "<tr><th>$l_category</th><td>$category</td></tr>";
        else $dis_category = '';
        if($single) $dis_attendees = '';
        else  $dis_attendees = "<tr><th>$l_attendees</th><td>$dis_attendees</td></tr>";
        $event_details = '';
        if (!$privatized_event) {
          $event_details = "<table>
              <tr>
                <th>$GLOBALS[l_tag]</th><td>$tag</td>
              </tr>
              <tr>
                <th>$l_repeat</th><td>$dis_repeat</td>
              </tr>
                $dis_category
              <tr>
                <th>$l_location</th><td>".htmlspecialchars($location)."</td>
              </tr>
              <tr>
                <th>$l_desc</th><td>".htmlspecialchars($description)."</td>
              </tr>
              <tr>
                <th>$l_owner</th><td>$dis_owner</td>
              </tr>
              $dis_attendees
              <tr>
                <th>$l_resources</th><td>$dis_resources</td>
              </tr>
              <tr>
                <th>$l_items</th><td>$item_list</td>
              </tr>
            </table>";
          if ($event->isExternal()) {
            $event_details .= "<a $target href=\"".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsultExt&amp;calendar_id=$ev_id&amp;contact_id=$event->owner\"><img src=\"$ico_search\" title=\"[Info]\"/></a>";
          } else {
            $event_details .= "<a $target href=\"".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=$ev_id\"><img src=\"$ico_search\" title=\"[Info]\"/></a>";
          }
        }
        if($event->all_day) {
          if($end->subSecond(1)->compareDateIso($date) != 0) {
            $diff = floor($event->duration/86400);
            $schedule = $GLOBALS['l_all_day']." ($diff $GLOBALS[l_days])";
          } else {
            $schedule = $GLOBALS['l_all_day']; 
          } 
        } else {
          if($date->compareDateIso($end) != 0) {
            $schedule = "$date_begin - $date_end";
          } else {
            $schedule = "$hour - $hour_end";
          }
        }

        $dis_events .= "<div id=\"evt_${date}_$ev_id\" >
          <a href=\"javascript: void(0);\"><span style=\"float: right;\" class='$class'>$dis_people $dis_res</span>
          <span class='$class'>$schedule - <strong>$title</strong> $needs_action </span></a>
          <div id=\"eventDetail_${date}_$ev_id\" class=\"eventDetail detail\" style=\"display: none;\">
            $event_details
          </div>
          </div>";
      }
      $dis_events .= " </td>
      </tr>";
    }
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <span style='float:left;width:100%;padding-top:5px;'><a href=\"#\" onclick=\"display_list_detail('show'); return false;\">$l_expand_all</a> - 
  <a href=\"#\" onclick=\"display_list_detail('hide'); return false;\">$l_collapse_all</a></span>
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
  </script>";

  return $block;
}


/**
 * Perform HTML and javascript code for events. 
 * 
 * @param Occurrence $occurence
 * @param String $unit display unit (hour day)
 * @param Array $calendar_entity array of calendars entities we create display ( ["user"] => Array ( [ user_id ] => array(["name"] => "joe", ["class"] => "eventOwner|eventOwner0...", ["type"] => "user", ["id"] => 9) ) )
 * @param Array $writable_entity array of entities we ($obm["uid"]) can write to ( ["ids"] => array(9,15), ["entity"] => array ( [ user_id ] => array ( ["id"] => 9, ["label"] => "joe" ), array(...) ) )
 * @access public
 * @return event HTML and javascript code
 */
function dis_calendar_event($occurrence, $unit, $calendar_entity,$writable_entity) {
  global $obm, $ccalendar_first_hour, $ccalendar_last_hour;
  global $ico_resize, $ico_periodic, $ico_meeting, $ico_private;
  global $l_user, $l_contact, $l_resource, $l_private;
  global $output_target, $action;

  if($occurrence->entity_state == "DECLINED"){
    return null;
  }
  $event_data = array();
  $options=new stdClass();
  $event = $occurrence->event;
  $owner = $event->owner;
  $owner_name = phpStringToJsString($event->owner_name);
  $organizer_name = phpStringToJsString($event->organizer_name);
  $attendee = $event->attendee;
  if ($event->privacy == 1 && (is_null($attendee['user'][$obm['uid']])) && $owner != $obm['uid']) {
    $title = phpStringToJsString($l_private);
    $description = $title;
    $location = $title;
    $category = $title;
    $attendees = $title;
  } else {
    $title = $event->title;
    $description = $event->description;
    $properties = $event->properties;
    if ($properties != '') {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset' ?".">".$properties;
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
    if (is_array($event->category1)) {
      $event->category1 = implode(' ', $event->category1);
    }
    $category = phpStringToJsString($event->category1);  
    $attendees = array();
    foreach ($attendee as $entity => $entities) {
      $label_entity = phpStringToJsString(${"l_$entity"});
      $ent = array();
      foreach($entities as $at) {
        $ent[] = phpStringToJsString($at);
      }
      $attendees[] = array("label"=>$label_entity, "entities"=>$ent);
    }
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
  $opacity = strtolower($occurrence->event->opacity);
  $eventColor = get_color_data($event->color);
  $updatable = false;
  if ( $event->isOrganizerAnObmUser() && $entity_id == $event->owner && ( $owner == $obm['uid'] || in_array($owner,$writable_entity['ids']) ) ) {
    $updatable =  true;
    $event_data["updatable"] = true;
  }
  
  if ($output_target == 'print' || $action == 'portlet') {
    $updatable =  false;
    $event_data["updatable"] = false;
  }

  $event_data["id"] = "$id";
  $event_data["opacity"] = "$event->opacity";
  $event_data["entity"] = "$entity";
  $event_data["entity_id"] = $entity_id; 
  $event_data["duration"] = $duration;
  settype($event_data["duration"],"float");
  $event_data["item"] = "$item_list";
  $event_data["time"] = $date->getTimestamp();
  settype($event_data["time"],"float");
  $event_data["title"] = htmlspecialchars($title);
  $event_data["all_day"] = $all_day;
  $event_data["klass"] = $entityClass; 
  $event_data["description"] = htmlspecialchars($description);
  $event_data["owner_name"] = $owner_name;
  $event_data["location"] = htmlspecialchars($location);
  $event_data["category"] = $category;
  $event_data["colors"] = array(
    "event"=>array(
	"body"=>$eventColor[body], 
	"header"=> $eventColor[header]
      )
    );

  $event_data["status"] = $entity_state;
  $event_data["organizer_name"] = $organizer_name;
  $event_data["attendees"] = $attendees;

  switch($unit) {
    case 'day' :
      $day = true;
      $index = $occurrence->index->getTimestamp();
      $left = $occurrence->left;
      $right = $occurrence->right;
      $event_data["index"] =$index;
      $event_data["left"] = $left;
      $event_data["right"] = $right;
      if ($updatable) {
        $options->draggable = true;
      }
      break;
    case 'hour' :
      $cend = clone $end;
      if ($cend->getHour()==0 || $cend->getMinute()==0 || $cend->getSecond()==0) {
        $cend->subSecond(1);
      }
      $day = ($all_day) || ($cend->compareDateIso($date)!=0);
      if ($updatable) {
        $options->draggable = true;
        $options->resizable = true;
      }
      break;
  }

  if ($event->isPrivate()) {
    $event_data["private"] = true;
  } else {
    $event_data["private"] = false;
  }
  if ($event->isMeeting()) {
    $event_data["meeting"] = true;
  } else {
    $event_data["meeting"] = false;
  }
  if ($event->isPeriodic()) {
    $event_data["periodic"] = true;
  } else {
    $event_data["periodic"] = false;
  }
  if ($all_day) {
    $head .= $title;
  } else {
    $head .= $date->get(Of_Date::TIME_SHORT);
  }
  
  $opt = json_encode($options);
  if ($day) {
    $display_events = str_replace("'","\'","obm.calendarManager.newDayEvent(".json_encode($event_data).",$opt);");
  } else {
    $display_events = str_replace("'","\'","obm.calendarManager.newEvent(".json_encode($event_data).",$opt);");
  }

  return $display_events;
} 


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the month
// Parameters:
//   - $current_view : current calendar view
//   - $calendar_entity : array of entities to display ['user'] ['resource'] 
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_month_planning($current_view, $calendar_entity, $writable_entity) { 
  global $obm, $ico_new_event;
  global $ccalendar_weekstart, $l_week, $l_all_day;
  global $l_private,$l_daysofweekfirst,$l_daysofweek;

  $category_filter = $current_view->get_category_filter();
  $date = clone $current_view->get_date();
  $date->setDay(1);
  $current = clone $date;
  $current->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addMonth(1);

  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $events = calendar_events_model($current,$end, $calendar_entity, $status, $category_filter);
  $of = &OccurrenceFactory::getInstance();

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
    $line1 .= "<th><a href=\"".basename($_SERVER['SCRIPT_NAME'])."?cal_range=day&amp;date=$iso\">$day</a></th>\n";
    if($current->compareWeekday($ccalendar_weekstart) == 0) {
      $week = $current->getWeek();  
      $line2 .= "<th colspan=\"7\">$l_week <a href=\"".basename($_SERVER['SCRIPT_NAME'])."?cal_range=week&amp;date=$iso\">$week</a></th>";
    }elseif($current->compareDay(1) == 0) {
      $week = $current->getWeek();  
      $span = $current->diffWeekday($ccalendar_weekstart);
      if ($span >= 3) $line2 .= "<th colspan=\"$span\">$l_week <a href=\"".basename($_SERVER['SCRIPT_NAME'])."?cal_range=week&amp;date=$iso\">$week</a></th>";
      else $line2 .= "<th colspan=\"$span\"><a href=\"".basename($_SERVER['SCRIPT_NAME'])."?cal_range=week&amp;date=$iso\">$week</a></th>";
    }
    foreach ($calendar_entity as $type => $entity) {
      foreach ($entity as $id => $properties) {
        $haveevent = false;
        $line_id = $type.' '.$id;
        if($empty) {
          $line[$line_id] .= "<td onclick=\"window.location.href='".basename($_SERVER['SCRIPT_NAME'])."?cal_range=day&date=$iso'\">&nbsp;</td>";
        } else {
          $today_events = array();
          $day_content = '';
          $occurrences = $of->getPeriodOccurrences($current, null,array($type => array($id)));
          if ($occurrences != null) {
            $haveevent = true;
            foreach ($occurrences as $occurrence) {
              $event = &$occurrence->event;
              $category = $event->category1;
              if(!is_string($category) || !isset($cats_color[$category])){
                $category = '';
              }
              $location = htmlspecialchars($event->location);
	      $title = htmlspecialchars($event->title);
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
                  $today_events[] = "<li style=\"$style\">".$title." ($location) $ad</li>";
                } else {
                  $today_events[] = "<li style=\"$style\">".$title." $ad</li>";
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
              <div id=\"$id-$day\" class=\"infoPopup\" style=\"border: 1px solid black;\">
              <h1>$description $day</h1>
               <ul>
               ".implode($today_events)."
               </ul>
              </div>";            
            $line[$line_id] .= "<td onclick=\"window.location.href='".basename($_SERVER['SCRIPT_NAME'])."?cal_range=day&date=$iso'\" class=\"eventOwner\"
               onmouseover=\"toggleInfo(this,'$id-$day',1); return true;\"
               onmouseout=\"toggleInfo(this,'$id-$day',0); return true;\">$day_content</td>";
          } else {
            $line[$line_id] .= "<td onclick=\"window.location.href='".basename($_SERVER['SCRIPT_NAME'])."?cal_range=day&date=$iso'\"></td>";
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

  $block = "
  <style>
    $css
  </style>
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
 * @param CalendarView $current_view : current calendar view
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_week_planning($current_view, $calendar_entity, $writable_entity) {
  global $path,$l_week;
  global $ccalendar_weekstart, $obm;
  global $ccalendar_first_hour;

  // FIXME MAKE IT WORK
  $display_days = $_SESSION['set_cal_display_days'];
  if (empty($display_days)) $display_days = '1111111';

  $category_filter = $current_view->get_category_filter();
  $date = clone $current_view->get_date();
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);
  
  $block = dis_calendar_planning_view ($date, $end, $calendar_entity, $category_filter);

  return $block;
}

/**
 * dis_calendar_day_planning
 * 
 * @param CalendarView $current_view : current calendar view
 * @param mixed $calendar_entity 
 * @param mixed $writable_entity 
 * @access public
 * @return void
 */
function dis_calendar_day_planning($current_view, $calendar_entity,$writable_entity) {
  global $path, $obm;

  $category_filter = $current_view->get_category_filter();
  $date = clone $current_view->get_date();
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);

  $block = dis_calendar_planning_day_view ($date, $end, $calendar_entity, $category_filter);

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

  $status = array('ACCEPTED', 'NEEDS-ACTION');
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
          $ressource_occ .= "<td class=\"asEvent\" style=\"width:$td_width%;\" class='$ress[class]' ><ul>";
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
          $goto = "".basename($_SERVER['SCRIPT_NAME'])."?action=new&amp;tf_date_begin=".$current_time->getURL()."&amp;new_sel=1&duration=3600&amp;new_group=1&amp;group_view=_ALL_&amp;sel_".$entity_type."_id[]=$entity_id";
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
  
  $status = array('ACCEPTED', 'NEEDS-ACTION');
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
    $line1 .= "<th><a href=\"".basename($_SERVER['SCRIPT_NAME'])."?cal_range=day&amp;date=$iso\">$day</a></th>\n";
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
    $title = htmlspecialchars($occurrence->event->title);
    if ($occurrence->event->location != '') {
      $location = "<br />$l_location : ".$occurrence->event->location;
    }            
  }
  $entity_id = $occurrence->id;
  $entity = $occurrence->entity;

  if(is_string($occurrence->event->category1) && isset($cats_color[$occurrence->event->category1])){
    $style = "background-color:rgb(".$cats_color[$occurrence->event->category1].")";
  } else {
    $style = "background-color:rgb(99, 100, 102)";
  }

  if($occurrence->event->all_day) {
    $schedule = $l_all_day; 
  } else {
    if($occurrence->date->compareDateIso($date) != 0) {
      $schedule = $occurrence->date->getOutputDateTime();
    } else {
      $schedule = $occurrence->date->get(Of_Date::TIME_SHORT);
    }
  }
  if ($occurrence->event->isExternal()) {
    $event_data = "
      <li style='$style'>
      <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsultExt&amp;calendar_id=".$occurrence->event->id."&amp;contact_id=".$occurrence->event->owner."\">$schedule - $title</a>
      </li>";
  } else {
    $event_data = "
      <li style='$style'>
      <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=".$occurrence->event->id."\">$schedule - $title</a>
      </li>";
  }
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
    $title = htmlspecialchars($occurrence->event->title);
    if ($occurrence->event->location != '') {
      $location = "<br />$l_location : ".$occurrence->event->location;
    }            
  }
  $entity_id = $occurrence->id;
  $entity = $occurrence->entity;

  $end=clone($occurrence->date);
  $end->addSecond($occurrence->event->duration);

  if(is_string($occurrence->event->category1) && isset($cats_color[$occurrence->event->category1])){
    $style = "background-color:rgb(".$cats_color[$occurrence->event->category1].")";
  } else {
    $style = "background-color:rgb(99, 100, 102)";
  }

  if($occurrence->event->all_day) {
    $schedule = "<h1 style='$style' class='RF hoursTitle'>$l_all_day</h1>"; 
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
      <h1 style='$style' class='RF hoursTitle'>$schedule_start<br/>$schedule_end</h1>  
";
  }


  $event_data = "
    <li class='RC'>
    $schedule
    <div>";
  if ($occurrence->event->isExternal()) {
     $event_data .= "<a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsultExt&amp;calendar_id=".$occurrence->event->id."&amp;contact_id=".$occurrence->event->owner."\">$title</a>";

  } else { 
     $event_data .= "<a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=".$occurrence->event->id."\">$title</a>";
  }
  $event_data .= "</div>
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
  if (!empty($calendar['sel_user_id'])) {
    $readable['user'] = array_keys(OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'read', $calendar['sel_user_id']));
  } else {
    $readable['user'] = array();
  }
  if (!empty($calendar['sel_user_id'])) {
    $readable['resource'] = array_keys(OBM_Acl::getAllowedEntities($obm['uid'], 'resource', 'read', $calendar['sel_resource_id']));
  } else {
    $readable['ressource'] = array();
  }

  $datebegin = clone $calendar["date_begin"];
  $dateend = clone $calendar["date_end"];
  $h = $dateend->diffHour($datebegin);
  $m = $dateend->diffMinute($datebegin);
  $time_begin = $datebegin->getHour();
  $min_begin = $datebegin->getMinute();
  $duration = ($dateend->getTimestamp() - $datebegin->getTimestamp())/3600;
  $d = $datebegin->getTimestamp();
  
  $entity_id = $calendar['entity_id'];
  $entity_kind = $calendar['entity_kind'];
  $sel_ent = "data-$entity_kind-$entity_id";
  $entity_info = get_entity_info($entity_id, $entity_kind);

  $GLOBALS['js']['vars']['consts']['calendarUrl'] = $GLOBALS['path'].'/calendar/'.basename($_SERVER['SCRIPT_NAME']);

  foreach($conflicts as $conflict) {
    $private = true;
    $id = $conflict->event->id;
    $name  = $conflict->event->attendee[$conflict->entity][$conflict->id];
    if (((in_array($conflict->id, $readable[$conflict->entity]) || in_array($conflict->event->owner,$readable['user']))
      && $conflict->event->privacy != '1') || $conflict->event->owner == $obm['uid'] || ($conflict->id == $obm['uid'] && $conflict->entity == 'user')) {
        $private = false;
      }
    if (!$private) {
      $title = htmlspecialchars($conflict->event->title);
      $location = htmlspecialchars($conflict->event->location);
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
    $freeBusy_read_only = false;
  if($calendar['action'] == 'decision') {
    $owner_notification = "checked";
    $block = "<div class=\"detail\">
    <input type=\"checkbox\" id=\"cb_notification\" name=\"cb_notification\" $owner_notification onchange=\"$('owner_notification').value = $('cb_notification').checked;\" /><label for=\"cb_notification\">$GLOBALS[l_owner_notification]</label></div>";
    $freeBusy_read_only = true;
  }

  $freebusy = html_freebusy_header();
  $block .= "
  <div class=\"detail extra\">
  <h1>$l_conflicts</h1>
  <h2><a href=\"#\" onclick=\"displayFreeBusyConflict('$duration', '$d', '$freeBusy_read_only')\">$GLOBALS[l_display_entites_freebusy]</a></h2>
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
   <input type='hidden' id='sel_time_begin' value='$time_begin' />
   <input type='hidden' id='sel_min_begin' value='$min_begin' />
  </div>
  $freebusy
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
  $entity_id = $calendar['entity_id'];
  $entity_kind = $calendar['entity_kind'];
  $dis_form = "
  <form name=\"f_conflict\" method=\"post\">
   <input type=\"hidden\" name=\"calendar_id\" value=\"$event_id\" />
   <input type=\"hidden\" name=\"action\" value=\"decision\" />
   <input type=\"hidden\" name=\"entity_id\" value=\"$entity_id\" />
   <input type=\"hidden\" name=\"entity_kind\" value=\"$entity_kind\" />
   <input type=\"hidden\" name=\"cba_force\" value=\"1\" />
   <input type=\"hidden\" name=\"rd_decision_event\" value=\"\" />
   <input type=\"hidden\" id=\"owner_notification\" name=\"owner_notification\" value=\"true\" />
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
function dis_calendar_event_consult($id, $date_edit_occurrence = null) {
  global $display, $l_err_reference;

  if ($id > 0) {
    $eve_q = run_query_calendar_detail($id);
    if ($eve_q->num_rows() == 1) {
      $entities = get_calendar_event_entity($id);
      $exception_q = run_query_get_one_event_exception($id);
      $alerts = get_one_event_user_alert($id);
      $display['detailInfo'] = display_record_info($eve_q);
      $block = html_calendar_event_consult($eve_q, $exception_q, $entities, $alerts, $date_edit_occurrence);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display External Event Consultation
// Parameters:
//   - $id : event id
//	 - $contact_id : contact id
/////////////////////////////////////////////////////////////////////////////// 
function dis_calendar_event_consult_ext( $contact_id, $id ) {
	global $display, $l_err_reference; 
	if ( !$id ) {
		$display['msg'] .= display_err_msg( $l_err_reference );
		return $block;
	}
	$uid = _get_ext_event_uid_and_suffix( $id );
	$contact = OBM_Contact::get( $contact_id );
	$contact_calendar = $contact->getCalendar();
	$events = $contact_calendar->getVevents();
	if ( _is_exception( $uid['suffix'] ) ) {
		return _exception_event_consult_ext( $events, $uid['searched_uid'], $uid['suffix'] );
	}
	return _standard_event_consult_ext( $events, $uid['searched_uid'] );
}
function _get_ext_event_uid_and_suffix( $id ){
	$searched_uid = substr( $id,4 ); 
	$suffix = '';
	$extract_uid_and_suffix_from_url =  '/(.+)-(\d+)$/';
	if(preg_match($extract_uid_and_suffix_from_url, $searched_uid, $matches )) {
		$searched_uid = $matches[1];
		$suffix = $matches[2];
	}
	return array( 'searched_uid' => $searched_uid, 'suffix' => $suffix );
}
function _is_exception( $suffix ){
	return !empty( $suffix );
}
function _exception_event_consult_ext( $events, $searched_uid, $searched_suffix ){
	$i = 0;
	$searched_suffix += 0;
	foreach ($events as $event) {
		$recurrenceId =  $event->getExceptionRecurrenceId();
		if  ( $event->getUid() == $searched_uid && !is_null( $recurrenceId ) ) {
			if( $i == $searched_suffix ){
				$event_q = new Vpdi_Icalendar_Event($event);
				$attendees =  $event_q->getAttendees();
				$mem_date  = $event->getDtstart();
				$exception_q = $event->getExceptionRecurrenceIdProperty();
				$block = html_calendar_event_consult_ext($event_q, $exception_q, $attendees, "");
				return $block;
			}
			$i++;
		}
	}
	return '';
}
function _standard_event_consult_ext($events, $searched_uid){
	foreach ( $events as $event ) {
		if  ( $event->getUid() == $searched_uid) {
			$event_q = new Vpdi_Icalendar_Event($event);
			$attendees =  $event_q->getAttendees();
			$exdate_q = $event->getCanceledExceptionExtEvents();
			$draganddrop_q = _drag_and_drop_exception_search($events, $searched_uid, $event);
			$unsort_tab_exception = array_merge($exdate_q, $draganddrop_q);
			$exception_q =  _sort_exceptions_table($unsort_tab_exception);
			$block = html_calendar_event_consult_ext($event_q, $exception_q, $attendees, "");
			return $block;
		}
	}
	return '';
}
function _drag_and_drop_exception_search( $events, $searched_uid, $event_mem ){
	$draganddrop_q= array();
	$value_mem = $event_mem->extractDateValue();
	foreach ( $events as $event ) {
		$event_exceptions = $event->getMovedExceptionExtEvents();
		$value_event =  $event->extractDateValue();
		$event_uid = $event->getUid();
		if ( $value_event != $value_mem  && $searched_uid == $event_uid) {
			$draganddrop_q = array_merge( $draganddrop_q, $event_exceptions );
		}
	}
	return $draganddrop_q;
}
function _sort_exceptions_table($table){
	$sort_table = array();
	foreach($table as $unsort){
		$date_value =  $unsort->value();
		$sort_table[$date_value] = $unsort;
	}
	ksort($sort_table);
	return $sort_table;
}
function event_details_access_rights() {
  $access_type = 'rw';
  if ( isset($GLOBALS['token']) && ( $GLOBALS['token']['type'] == 'public' )) {
    $access_type = 'r';
  }
  return $access_type;
}
///////////////////////////////////////////////////////////////////////////////
// XHTML : Display Calendar Event Consult
// Parameters:
//   - $event_q  : event_query result
//   - $entities : entities linked to the event ['user'], ['resource']
//   - $alerts   : alerts array
///////////////////////////////////////////////////////////////////////////////
function html_calendar_event_consult($event_q, $exception_q, $entities, $alerts, $date_edit_occurrence=null) {
  global $obm, $display, $cgp_show, $ccalendar_weekstart, $ccalendar_resource;
  global $l_users, $l_resources, $l_title, $l_location, $l_priority,$l_private;
  global $l_low,$l_medium,$l_datebegin,$l_day,$l_month,$l_week,$l_desc,$l_high;
  global $l_dateend,$l_insert,$l_update,$l_repeat, $l_alerts, $l_state, $l_user;
  global $l_change_state,$l_groups,$l_all_day,$l_every,$l_owner;
  global $l_opacity, $l_opacity_free, $l_opacity_busy;
  global $l_repeatkind,$l_repeatdays,$l_date_repeatend, $l_none, $c_none; 
  global $l_wait, $l_exception, $l_yes, $l_no, $path;
  global $l_carac,$l_module_calendar, $l_items, $ccalendar_alerts;
  global $l_daysofweekshort,$l_update,$l_accept,$l_refuse,$l_repeatfrequency;
  global $l_documents,$l_attach_documents,$l_add_documents,$l_title,$l_name;
  global $l_accepted, $l_needs_action, $l_declined;
  global $ico_add, $ico_delete, $ico_spinner;
  global $l_comments, $l_edit, $l_add, $l_add_alarm;
  global $l_motivation;

  $id = $event_q->f('event_id');
  $is_exception = $date_edit_occurrence != null || $event_q->f('event_is_exception');

  $organizer_display_name = null;
  foreach($entities as $entityType => $entitiesLists){
    if($entityType == "user" || $entityType == "contact"){
      foreach ($entitiesLists['entity'] as $entity){
        if($entity['is_organizer']){
          $organizer_display_name = $entity['label'];
          break;
        }
      }
      if($organizer_display_name) break;
    }
  }

  if ($event_q->f('event_privacy') == 0 || 
      $event_q->f('event_owner') == $obm['uid'] ||
      in_array($obm['uid'],$entities['user']['ids'])) {

    $title = htmlspecialchars($event_q->f('event_title'));
    $location = htmlspecialchars($event_q->f('event_location'));
    $cats1 = of_category_get_entitycategories('event', 'category1', $id, 'mono');
    $owner = $event_q->f('event_owner');
    $owner_lastname = $event_q->f('owner_lastname');
    $owner_firstname = $event_q->f('owner_firstname');
    $priority = $event_q->f('event_priority'); 
    $properties = $event_q->f('event_properties');
    $description = nl2br(htmlspecialchars($event_q->f('event_description')));
    if ($properties != '') {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset' ?".">".$properties;
    } else {
      $item = '';
    }

    $block_category1 = of_category_dis_block_consult('event', 'category1', $cats1, 'mono');
    switch ($priority) {
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
  $begin = ( $date_edit_occurrence != null ) ? new Of_Date($date_edit_occurrence, 'GMT') : new Of_Date($event_q->f('event_date'), 'GMT');
  $end = clone $begin;
  $end->addSecond($event_q->f('event_duration'));
  if ($all_day) {
    $dis_all_day = "<tr><th>$l_all_day</th><td>$GLOBALS[l_yes]</td></tr>";
    $end->subSecond(1);
    $begin = $begin->getOutputDate();
    $end = $end->getOutputDate();
  } else {
    $begin = $begin->getOutputDateTime();
    $end = $end->getOutputDateTime();
  }     
  $privacy = $event_q->f('event_privacy') == 1? $l_yes : $l_no;
  
  if($event_q->f('event_opacity') == "OPAQUE"){
    $opacity = $l_opacity_busy;
  } else {
    $opacity = $l_opacity_free;
  }

  $allow_documents = ($event_q->f('event_allow_documents') == 1);

  if ($is_exception) {
    $parent_event_id = $event_q->f('event_eventexception_parent_id') ? $event_q->f('event_eventexception_parent_id') : $id;
    $parent_event_url = "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&calendar_id=$parent_event_id";
    $show_parent_link = "<a href=\"$parent_event_url\" title=\"$GLOBALS[l_parent_event]\">$GLOBALS[l_show_parent_event]</a>";
    $repeat_block = "
    <div class=\"detail infos\">
     <h1>$l_repeat</h1>
     <table>   
      <tr>    
       <th>$GLOBALS[l_exception] :</th> 
       <td>$GLOBALS[l_event_is_exception]</td>
      </tr>
      <tr>    
       <th>$GLOBALS[l_parent_event] :</th> 
       <td>$show_parent_link</td>
      </tr>
     </table>
    </div>";
  } elseif ($event_q->f('event_repeatkind') != 'none') {
    $kind = $GLOBALS['l_'.$event_q->f('event_repeatkind')];
    $kind_unit = $GLOBALS['l_'.$event_q->f('event_repeatkind').'_unit'];
    $repeat_days = $event_q->f('event_repeatdays'); 
    if ($event_q->f('event_endrepeat') != '') {
      $repeat_end = new Of_Date($event_q->f('event_endrepeat'), 'GMT');
      $repeat_end = $repeat_end->getOutputDate();
    } else {
      $repeat_end = $GLOBALS['l_no_end_repeat'];
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
      $exception_child_id = $exception_q->f('eventexception_child_id');
      if ($exception_child_id) {
        $exception_date = new Of_Date($exception_q->f('eventexception_event_date'), 'GMT');
        $exception_url = "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&calendar_id=$exception_child_id";
        $exception_link = "<a href=\"$exception_url\">".$exception_date->getOutputDateTime()."</a>";
        $exception_desc = sprintf($GLOBALS['l_eventexception_moved_to'],$exception_link);
      } else {
        $exception_desc = $GLOBALS['l_eventexception_canceled'];
      }
      $block_exception_detail .= $date->getOutputDate()." ($exception_desc)<br />";
    }

    $fieldset_begin = $fieldset_end = '';
    if( $date_edit_occurrence != null ){
      $fieldset_begin = '<fieldset style="border:1px dashed #D2D2D2;padding-top:10px;"><legend>'.$GLOBALS[l_is_occurrence_of_parent_event].' </legend>';
      $fieldset_end = '</fieldset>';
    }

    $repeat_block = "
    <div class=\"detail infos\">
     <h1>$l_repeat</h1>
     $fieldset_begin
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
     $fieldset_end
     </div>";
  }

  $ie6_radio_fix = "style='border:none; padding:0;margin:0;width:auto;'";
  $access_rights =  event_details_access_rights();
  if ($event_q->f('event_privacy') == 0 ||
      $event_q->f('event_owner') == $obm['uid'] ||
      in_array($obm['uid'],$entities['user']['ids'])) {
    $attendee = $entities['user'];
    if (is_array($attendee['entity'])) {
      foreach ($attendee['entity'] as $u_id => $entity) {
        $label = $entity['label'];
        $state = $entity['state'];
        $is_organizer = $entity['is_organizer'];
        $comment = $entity['comment'];
        
        if ($is_organizer && $state == 'DECLINED') continue;
        $comment_style = 'text-align:justify;';

        $ACCEPTED = '';
        ${'NEEDS-ACTION'} = '';
        $DECLINED = '';

        $owner_login = $event_q->f('usercreate');
        $block_status = isset($GLOBALS['ccalendar_block_status'])
                          && $GLOBALS['ccalendar_block_status']
                          && (strcasecmp($owner_login, $GLOBALS['block_status_str']) == 0);
        if (OBM_Acl::canWrite($obm['uid'], 'calendar', $u_id) && $access_rights =='rw' && $block_status == false) {
          $user_alert = $ccalendar_alerts;
          $bell_alert_class = $bell_alert_onclick = $bell_alert_title ='';
          if($alerts[$u_id] > 0 && !($user_alert[$alerts[$u_id]])) {
            $user_alert[$alerts[$u_id]] = Of_Date::secondToString($alerts[$u_id]);
            $bell_alert_class = 'bellalertexist';
            $bell_alert_onclick = " onclick=\"obm.calendarAlarmPopup.compute($alerts[$u_id], $u_id, $id);\" ";
            $bell_alert_title = " title=\"".$user_alert[$alerts[$u_id]]."\" ";
          }else{
            $bell_alert_class = 'bellalert';
            $bell_alert_onclick ="onclick=\"obm.calendarAlarmPopup.compute(-1, $u_id, $id);\"";
            $bell_alert_title = " title=\"$l_add_alarm\" ";
          }
          $$state = "checked=\"checked\" disabled";
          $urld = "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?calendar_id=$id&entity_kind=user&entity_id=$u_id&action=update_decision";
	  
          $bell_alert_icon = "<a class=\"$bell_alert_class\" $bell_alert_onclick $bell_alert_title></a>";
          
          // comment  display
          $saved_checked = $state;
          $escaped_title = phpStringToJsString(str_replace("\n\r", ', ', $title));
          if($comment == '' ){
            $comment_style = 'text-align:center;';
            $user_comment = $GLOBALS[l_add_comment];
            $user_comment_display = "<a class=\"editcomment\" name=\"rd_contact_decision\" value=\"ACCEPTED\" onclick=\"obm.commentedDecisionPopup.compute($u_id, $id,'ACCEPTED', '$saved_checked', 'user', '$user_comment', '$escaped_title', 'detailconsult', 'placeholder','$date_edit_occurrence')\" />";
          }else{
            $user_comment = phpStringToJsString(str_replace("\n\r", ', ', $comment));
            $user_comment_display =  $comment." <a class=\"editcomment\" name=\"rd_contact_decision\" value=\"ACCEPTED\" onclick=\"obm.commentedDecisionPopup.compute($u_id, $id,'ACCEPTED', '$saved_checked', 'user', '$comment', '$escaped_title', 'detailconsult', 'value', '$date_edit_occurrence')\" />";
          }
	  if ( $date_edit_occurrence ) {
	    $attrs = "data-entity-id=\"$u_id\" data-event-id=\"$id\" data-occurrence-date=\"$date_edit_occurrence\"";
	    $decisionCheckboxes = "<label><input $ACCEPTED $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"ACCEPTED\" onclick=\"Obm.decisionCBHandler.call(this);\" $attrs /><span> $l_accepted</span></label>";
	    $decisionCheckboxes .= "<label><input ${'NEEDS-ACTION'} $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"NEEDS-ACTION\" onclick=\"Obm.decisionCBHandler.call(this);\" $attrs  /><span> $l_needs_action</span></label>";
	    $decisionCheckboxes .= "<label><input $DECLINED type=\"radio\" $ie6_radio_fix name=\"rd_user_decision_$u_id\" value=\"DECLINED\" onclick=\"Obm.decisionCBHandler.call(this);\" $attrs  /><span> $l_declined</span></label>";
	  } else {
	    $decisionCheckboxes = "<label><input $ACCEPTED $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /><span> $l_accepted</span></label>
	      <label><input ${'NEEDS-ACTION'} $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$u_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /><span> $l_needs_action</span></label>
	      <label><input $DECLINED type=\"radio\" $ie6_radio_fix name=\"rd_user_decision_$u_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /><span> $l_declined</span></label>";
	  }

          $dis_user_list .=
          "<tr>
           <td>$label</td>
           <td>
            $decisionCheckboxes
           </td>
           <td style=\"$comment_style\"> $user_comment_display </td>
           <td style=\"text-align:center;\">$bell_alert_icon</td>
          </tr>";
          $user_comment_display = $$state = '';
        } else {
          $ACCEPTED = 'class="disabled"';
          ${'NEEDS-ACTION'} = 'class="disabled"';
          $DECLINED = 'class="disabled"';
          $$state = '';  
          $dis_user_list .= "
                <tr>
                <td>$label</td>
                <td style=\"padding-left:5px;\">
                <span $ACCEPTED> $l_accepted</span>    
                <span ${'NEEDS-ACTION'}> $l_needs_action</span>
                <span $DECLINED> $l_declined</span>
                </td>
                <td><span style=\"$comment_style\">$comment</span></td>
                <td></td>
                </tr>";
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
          $$state = "checked=\"checked\" disabled";
          $urld = "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=update_ext_decision&calendar_id=$id&entity_kind=contact&entity_id=$c_id";

          $dis_contact_list .= "
          <tr>
          <td>$label</td>
          <td class=\"C\">
          <label><input $ACCEPTED $ie6_radio_fix type=\"radio\" name=\"rd_contact_decision_$c_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /> <span>$l_accepted</span></label>
          <label><input ${'NEEDS-ACTION'} $ie6_radio_fix type=\"radio\" name=\"rd_contact_decision_$c_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /> <span>$l_needs_action</span></label>
          <label><input $DECLINED $ie6_radio_fix type=\"radio\" name=\"rd_contact_decision_$c_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /> <span>$l_declined</span></label>
          </td>
          </tr>";
          $$state = '';
      }
    }
  }

  if ($dis_user_list != "") {
    $dis_users_block ="
      <div class=\"detail infos\">
      ".dis_event_alarm_popup()."
      ".dis_event_commented_decision_popup(true)."
      <script type=\"text/javascript\"> 
      obm.vars.consts.calendarUrl = '".$GLOBALS[path].'/calendar/'.basename($_SERVER['SCRIPT_NAME'])."';
      </script>
      <h1>$l_users</h1>
      <table class=\"spreadSheet\">
      <thead>
      <tr>
      <td>$l_users</td><td>$l_state</td><td>$l_motivation</td><td>$l_alerts</td>
      </tr>
      </thead>
      $dis_user_list
      </table>
      </div>";
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
        $label = '';
        $label .= $entity['label'];
        $state = $entity['state'];
        
        $comment = $entity['comment'];
        $resource_comment = phpStringToJsString(str_replace("\n\r", ', ', $comment));
        if (OBM_Acl::canWrite($obm['uid'], 'resource', $r_id)) {
          $$state = "checked=\"checked\" disabled";
          $url= "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=update_decision&calendar_id=$id&entity_kind=resource&entity_id=$r_id";
    
         if($resource_comment != ''){
          $comment_style = 'text-align:justify;';
          $resource_comment_display = $resource_comment." <a class=\"editcomment\" name=\"rd_contact_decision\" value=\"ACCEPTED\" onclick=\"obm.commentedDecisionPopup.compute($r_id, $id,'ACCEPTED', '$state', 'resource', '$resource_comment', '$escaped_title', 'detailconsult', 'value')\" />";
         }else{
           $comment_style = 'text-align:center;';
           $resource_comment_display =  "
           <a class=\"editcomment\" name=\"rd_contact_decision\" value=\"ACCEPTED\" onclick=\"obm.commentedDecisionPopup.compute($r_id, $id,'ACCEPTED', '$state', 'resource', '$GLOBALS[l_add_comment]...', '$escaped_title', 'detailconsult', 'placeholder')\" />
           <a class=\"editcomment\" onclick=\"obm.calendarCommentPopup.compute($r_id, $id , '$GLOBALS[l_add_comment]...', 'resource', true);\" title=\"$l_add\"></a>";
         }
          $escaped_title = phpStringToJsString(str_replace("\n\r", ', ', $title));

          $url= "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=update_decision&calendar_id=$id&entity_kind=resource&entity_id=$r_id";
          $label = $entity['label'];
          $state = $entity['state'];
          $ACCEPTED = '';
          ${'NEEDS-ACTION'} = '';
          $DECLINED = '';
          $$state = "checked=\"checked\" disabled";

          $dis_resource_list .= "
          <tr>
          <td>$label $property[$r_id]</td>
          <td>
          <input $ACCEPTED $ie6_radio_fix type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"ACCEPTED\" onclick=\"window.location='$url&rd_decision_event=ACCEPTED'\" /> $l_accepted
          <input ${'NEEDS-ACTION'} $ie6_radio_fix type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$url&rd_decision_event=NEEDS-ACTION'\" /> $l_needs_action
          <input $DECLINED $ie6_radio_fix type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"DECLINED\" onclick=\"window.location='$url&rd_decision_event=DECLINED'\" /> $l_declined
          </td>
          <td style=\"$comment_style\">$resource_comment_display</td>
          </tr>";
          $$state = '';
        } else {
        
          $admins = OBM_Acl::getEntityWriters('resource', $r_id);
          if (is_array($admins)) {
            $emails = array();
            foreach($admins as $admin) {
              $user = get_user_info($admin['id'], 'user'); 
              if ($user['email'] != '') {
                array_push($emails, $user['email']);
              }
            }
            if (sizeof($emails) > 0) {
              $label = "<a href='mailto:".implode(',', $emails)."'><img src='$GLOBALS[ico_mail]' />$entity[label]</a> ";
            }
          }          
          $ACCEPTED = 'class="disabled"';
          ${'NEEDS-ACTION'} = 'class="disabled"';
          $DECLINED = 'class="disabled"';
          $$state = '';
          $dis_resource_list .= "<tr><td>$label</td><td>
          <input style='visibility:hidden;' disabled='disabled' $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /><span $ACCEPTED>$l_accepted</span>
          <input style='visibility:hidden;' disabled='disabled' $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /><span ${'NEEDS-ACTION'}>$l_needs_action</span>
          <input style='visibility:hidden;' disabled='disabled' $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /><span $DECLINED>$l_declined</span>
            </td><td>$comment</td>
            </tr>";          
        }
      }
      $dis_resource_block = "
        <div class=\"detail infos\">
        <h1>$l_resources</h1>
        <table class=\"spreadSheet\">
        <thead>
        <tr>
        <td>$l_resources</td><td>$l_state</td><td>$l_comments</td>
        </tr>
        </thead>
        $dis_resource_list
        </table>
        </div>";
    }
  }
  
  if ($access_rights == 'rw' && ($event_q->f('event_privacy') == 0 ||
      $event_q->f('event_owner') == $obm['uid'] ||
      in_array($obm['uid'],$entities['user']['ids']))) {
  
    $display_documents_form = !isset($GLOBALS['token']) && ($event_q->f('event_owner') == $obm['uid'] 
      || ($allow_documents && in_array($obm['uid'], $entities['user']['ids'])));
    
    $docs = $entities['document'];
    if (is_array($docs['entity'])) {
      $dis_document_list = '';
      $event_entity_id = $event_q->f('event_entity_id');
      foreach ($docs['entity'] as $d_id => $entity) {
        if (!$entity['private'] || $entity['user'] == $obm['uid'] || in_array($obm['uid'], $entities['user']['ids'])) {
          $content = "<a href=\"".get_document_attachments_url($d_id, $entity['private'])."\">$entity[title]</a>";
          if ($event_q->f('event_owner') == $obm['uid'] || $entity['user'] == $obm['uid']) {
            $content = "
              <a onclick=\"detachDocument('$d_id', '$event_entity_id', this);\" id='doc$d_id'>
                <img src=\"$ico_delete\" alt=\"[Delete]\" />
              </a>".$content;
          }
          $dis_document_list.= "<li>$content</li>";
        }
      }
    }
      
    if ($display_documents_form) {
      $dis_document_form = "
        <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_entity\" action=\"".basename($_SERVER['SCRIPT_NAME'])."\">
        <fieldset class=\"detail\">
        <legend>$l_add_documents</legend>
        <table>
          ".html_calendar_documents_form_fieldset()."
        </table>
        <script type=\"text/javascript\">
          obm.initialize.chain(function () {
            new obm.AutoComplete.Search('$path/document/document_index.php?action=ext_search', 'sel_document_id', 'documentSearch');
          });
        </script>
        </fieldset>
        <fieldset class=\"buttons\">
          <input type=\"hidden\" name=\"calendar_id\" id=\"calendar_id\" value=\"$id\" />
          <input type=\"hidden\" name=\"action\" id=\"action\" value=\"attach_documents\" />
          <input type=\"submit\" value=\"$l_attach_documents\" />
        </fieldset>
        </form>";
    }

    $dis_document_block = "
      <div class=\"detail infos\">
      <h1>$l_documents</h1>
      <ul>
      $dis_document_list
      </ul>
      $dis_document_form
      </div>";
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
    <th>".__("Organizer")." :</th>
    <td>$organizer_display_name</td>
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
    <tr>
      <th>$l_opacity :</th>
      <td>$opacity</td>
    </tr>
   </table>
   </div>
  $repeat_block
  $dis_users_block
  
  $dis_document_block

  <p class=\"LC\"></p>
  $dis_contacts_block
  $dis_resource_block
  $dis_item_list

  <div class=\"detail extra\">
  <h1>$l_desc</h1>
  $description
  </div>
  $dis_decision_radio";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML : Display External Calendar Event Consult 
// Parameters:
//   - $event  : event object
//   - $attendees : event's attendees objects
//   - $alerts   : TODO
//   - $exception_q : TODO
///////////////////////////////////////////////////////////////////////////////
function html_calendar_event_consult_ext($event, $exception_q, $attendees, $alerts) {
  global $obm, $display, $cgp_show, $ccalendar_weekstart, $ccalendar_resource;
  global $l_users, $l_resources, $l_title, $l_location, $l_priority,$l_private;
  global $l_low,$l_medium,$l_datebegin,$l_day,$l_month,$l_week,$l_desc,$l_high;
  global $l_dateend,$l_insert,$l_update,$l_repeat, $l_alerts, $l_state, $l_user;
  global $l_change_state,$l_groups,$l_all_day,$l_every,$l_owner;
  global $l_opacity, $l_opacity_free, $l_opacity_busy;
  global $l_repeatkind,$l_repeatdays,$l_date_repeatend, $l_none, $c_none; 
  global $l_wait, $l_exception, $l_yes, $l_no, $path;
  global $l_carac,$l_module_calendar, $l_items, $ccalendar_alerts;
  global $l_daysofweekshort,$l_update,$l_accept,$l_refuse,$l_repeatfrequency;
  global $l_documents,$l_attach_documents,$l_title,$l_name;
  global $l_accepted, $l_needs_action, $l_declined;
  global $ico_add, $ico_delete, $ico_spinner;

  $id = $event->getUid();
  
  if (!$event->isPrivate()) {

    $title = htmlspecialchars($event->getSummary());
    $location = htmlspecialchars($event->getLocation());
  //FIXME  $cats1 = of_category_get_entitycategories('event', 'category1', $id, 'mono');
    $description = nl2br(preg_replace('@(https?://([-\w\.]+)+(:\d+)?(/([\w/_\.]*(\?\S+)?)?)?)@', '<a href="$1">$1</a>', htmlspecialchars($event->getDescription())));
    $owner = htmlspecialchars($event->getOrganizer()->cn);
    $owner_lastname = $owner;
    $priority = $event->getPriority(); 

  //  $block_category1 = of_category_dis_block_consult('event', 'category1', $cats1, 'mono');
    switch ($priority) {
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
  $all_day = $event->isAllDay();
  $begin = new Of_Date($event->getDtstart(), 'GMT');
  $end = clone $begin;
  $end->addSecond($event->getDuration());
  if ($all_day) {
    $dis_all_day = "<tr><th>$l_all_day</th><td>$GLOBALS[l_yes]</td></tr>";
    $end->subSecond(1);
    $begin = $begin->getOutputDate();
    $end = $end->getOutputDate();
  } else {
    $begin = $begin->getOutputDateTime();
    $end = $end->getOutputDateTime();
  }     
  $privacy = $event->isPrivate() == 1? $l_yes : $l_no;
  
  if($event->getTransparency() == "OPAQUE"){
    $opacity = $l_opacity_busy;
  } else {
    $opacity = $l_opacity_free;
  }
  
	// Exception 
	$block_exception_detail = '';
	if(!empty($exception_q)){
		$exception_cancel_desc = $GLOBALS['l_eventexception_canceled'];
		$exception_moved_desc = $GLOBALS['l_eventexception_moved_to'];
		$event_is_exception = $GLOBALS['l_event_is_exception'];
		$block_exception_detail  .= "<tr><th>$l_exception : </th><td>";
		foreach($exception_q as $exception){ 
			$str_date = strtotime( $exception->value() );
			$date = new Of_Date($str_date, 'GMT');
			$exception_date = $date->getOutputDate();
			$exception_date_time = $date->getOutputDateTime();
			if ( $exception->isMovedException() ){
				$dates .= $event_is_exception;
				$block_exception_detail  .= $dates."</td></tr>";
				$repeat_block = "
					<div class=\"detail infos\">
					<h1>$l_repeat</h1>
					<table>   
					$block_exception_detail
					</table>
					</div>
				";
			}
			if( $exception->isCanceledException() ){
				$dates .= $exception_date." ($exception_cancel_desc) <br/>";
			}else{
				$sprintf = sprintf($GLOBALS['l_eventexception_moved_to'], $exception_date_time);
				$dates .= "$exception_date ($sprintf)<br/>";
			}
		}
	}
  $block_exception_detail  .= $dates."</td></tr>";
  if ($repeats=$event->getRrule()) {
    $kind = $GLOBALS['l_'.$repeats['freq']];
    $kind_unit = $GLOBALS['l_'.$repeats['freq'].'_unit'];
    $repeat_days = $repeats['interval']; 
    if ($repeats['until'] != '') {
      $repeat_end = new Of_Date($repeats['until'], 'GMT');
      $repeat_end = $repeat_end->getOutputDate();
    } else {
      $repeat_end = $GLOBALS['l_no_end_repeat'];
    }
    if($repeats['interval'] == 1 ) {
      $repeatfrequency = sprintf($kind_unit,'');
    } else {
      $repeatfrequency = sprintf($kind_unit, $repeats['interval']);
    }

    if($repeats['freq'] == 'weekly') {
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
      $block_exception_detail
     </table>
     </div>";
  } 

  $ie6_radio_fix = "style='border:none; padding:0;margin:0;width:auto;'";

  if (!$event->isPrivate()) {
	foreach ($attendees as $attendee) {
        $label = $attendee->cn ? htmlspecialchars($attendee->cn) : str_replace('MAILTO:', '', $attendee->uri);
        $state = htmlspecialchars($attendee->partstat);
        $ACCEPTED = 'class="disabled"';
        ${'NEEDS-ACTION'} = 'class="disabled"';
        $DECLINED = 'class="disabled"';
        $$state = "";
          
          $dis_contact_list .= "
          <tr>
          <td>$label</td>
          <td class=\"C\">
          <label><span $ACCEPTED >$l_accepted</span></label>
          <label><span ${'NEEDS-ACTION'} >$l_needs_action</span></label>
          <label><span $DECLINED >$l_declined</span></label>
          </td>
          </tr>";
          $$state = '';
      }
    }

  if ($dis_contact_list != "") {
   $dis_contacts_block ="  <div class=\"detail infos\">
    <h1>$GLOBALS[l_users]</h1>
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

  // FIX ME display resources
  /* if ($ccalendar_resource) {
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
        $label = '';

        $label .= $entity['label'];
        $state = $entity['state'];
        if (OBM_Acl::canWrite($obm['uid'], 'resource', $r_id)) {
          $$state = "checked=\"checked\"";
          $url= "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=update_decision&calendar_id=$id&entity_kind=resource&entity_id=$r_id";
          $dis_resource_list .= "
          <tr>
          <td>$label $property[$r_id]</td>
          <td class=\"C\">
          <input $ACCEPTED $ie6_radio_fix type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"ACCEPTED\" onclick=\"window.location='$url&rd_decision_event=ACCEPTED'\" />$l_accepted
          <input ${'NEEDS-ACTION'} $ie6_radio_fix type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$url&rd_decision_event=NEEDS-ACTION'\" />$l_needs_action
          <input $DECLINED $ie6_radio_fix type=\"radio\" name=\"rd_resource_decision_$r_id\" value=\"DECLINED\" onclick=\"window.location='$url&rd_decision_event=DECLINED'\" />$l_declined
          </td>
          </tr>";
          $$state = '';
        } else {
        
          $admins = OBM_Acl::getEntityWriters('resource', $r_id);
          if (is_array($admins)) {
            $emails = array();
            foreach($admins as $admin) {
              $user = get_user_info($admin['id'], 'user'); 
              if ($user['email'] != '') {
                array_push($emails, $user['email']);
              }
            }
            if (sizeof($emails) > 0) {
              $label = "<a href='mailto:".implode(',', $emails)."'><img src='$GLOBALS[ico_mail]' />$entity[label]</a> ";
            }
          }          
          $ACCEPTED = 'class="disabled"';
          ${'NEEDS-ACTION'} = 'class="disabled"';
          $DECLINED = 'class="disabled"';
          $$state = '';
          $dis_resource_list .= "<tr><td>$label</td><td class=\"C\">
          <input style='visibility:hidden;' disabled='disabled' $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"ACCEPTED\" onclick=\"window.location='$urld&rd_decision_event=ACCEPTED'\" /><span $ACCEPTED>$l_accepted</span>
          <input style='visibility:hidden;' disabled='disabled' $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"NEEDS-ACTION\" onclick=\"window.location='$urld&rd_decision_event=NEEDS-ACTION'\" /><span ${'NEEDS-ACTION'}>$l_needs_action</span>
          <input style='visibility:hidden;' disabled='disabled' $ie6_radio_fix type=\"radio\" name=\"rd_user_decision_$r_id\" value=\"DECLINED\" onclick=\"window.location='$urld&rd_decision_event=DECLINED'\" /><span $DECLINED>$l_declined</span>
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
  }*/

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
    <tr>
      <th>$l_opacity :</th>
      <td>$opacity</td>
    </tr>
   </table> 
   </div>
  $repeat_block
  $dis_users_block
  $dis_contacts_block 
  $dis_resource_block
  $dis_item_list

  <div class=\"detail extra\">
  <h1>$l_desc</h1>
  $description
  </div>
  $dis_decision_radio";

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
function dis_calendar_event_form($action, $calendar, $event_q, $entities, $current_view, $organizer_id=null, $date_edit_occurrence=null) {
  global $obm;
  if (($action == 'detailupdate') || ($action == 'duplicate') || ($action == 'conflict_manager') ) {
    $users = $entities['user'];
    $resources = $entities['resource'];
    $contacts = $entities['contact'];
    $documents = $entities['document'];
    $exception_q = run_query_get_one_event_exception($calendar['calendar_id']);
    $alerts = get_one_event_user_alert($calendar['calendar_id'], $obm['uid']);
   
    // Entities from Freebusy
    if (isset($calendar['new_meeting']) && $calendar['new_meeting'] == 1) {
      $users = (is_array($calendar['new_user_id'])) ? get_userobm_from_ids($calendar['new_user_id']) : array();
      $groups = (is_array($calendar['new_group_id'])) ? get_calendar_group_from_ids($calendar['new_group_id']) : array();
      $resources = (is_array($calendar['new_resource_id'])) ? get_calendar_resource_from_ids($calendar['new_resource_id']) : array();
      $contacts = (is_array($calendar['new_contact_id'])) ? get_contact_from_ids($calendar['new_contact_id'], false) : array();
    }
    
  } else {
    $contacts_id = is_array($entities['contact']) ? $entities['contact'] : array();
    $users_id = is_array($entities['user']) ? $entities['user'] : array();
    $res_id = is_array($entities['resource']) ? $entities['resource'] : array();
    $groups_id = is_array($entities['group']) ? $entities['group'] : array();
    $documents_id = is_array($entities['document']) ? $entities['document'] : array();

    // If new event, add the optionnal new users (here to get the user's infos)
    if ($action == 'new') {      
      // Entities from Freebusy OR from the calendar select in the quick form popup
      if (is_array($calendar['new_user_id'])) $users_id = $calendar['new_user_id'];
      if (is_array($calendar['new_group_id'])) $groups_id = $calendar['new_group_id'];
      if (is_array($calendar['new_resource_id'])) $res_id = $calendar['new_resource_id'];
      if (is_array($calendar['new_contact_id'])) $contacts_id = $calendar['new_contact_id'];

      if (isset($calendar['organizer'])) $organizer_id = $calendar['organizer'];
      if (isset($calendar['template_id'])) {
        list($event_q, $template_entities) = run_query_calendar_get_template($calendar['template_id']);
        if(     check_loggued_user_is_the_only_attendee($users_id)
            &&  $event_q->f('event_organizer') != $obm['uid']
            &&  OBM_Acl::canWrite($obm['uid'], 'calendar',$event_q->f('event_organizer'))
        ){
          $users_id = array();
        }
        $users_id = array_merge($users_id, $template_entities['user']);
        $contacts_id = array_merge($contacts_id, $template_entities['contact']);
        $res_id = array_merge($res_id, $template_entities['resource']);
        $groups_id = array_merge($groups_id, $template_entities['group']);
        $documents_id = array_merge($documents_id, $template_entities['document']);
        if($event_q->f('event_organizer')){
          $calendar['organizer'] =  $event_q->f('event_organizer');
        }
      }

      if($action == 'insert') {
        if (isset($calendar['organizer'])) $organizer_id = $calendar['organizer'];  
      }
    }

    $users = (count($users_id) > 0) ? get_userobm_from_ids($users_id) : array();
    $contacts = (count($contacts_id) > 0) ? get_contact_from_ids($contacts_id, false) : array();
    $resources = (count($res_id) > 0) ? get_calendar_resource_from_ids($res_id) : array();
    $groups = (count($groups_id) > 0) ? get_calendar_group_from_ids($groups_id) : array();
    $documents = (count($documents_id) > 0) ? get_calendar_document_from_ids($documents_id) : array();
  }

  if($organizer_id == null)
    $organizer_id = getOrganizerId($users, $contacts);
  $calendar['organizer'] = $organizer_id;
  
  $possible_owners = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
  );
  if ($organizer_id != null) {
      $possible_owners2 = OBM_Acl_Utils::expandEntitiesArray(
        OBM_Acl::getAllowedEntities($calendar['organizer'], 'calendar', 'write')
      );
      foreach($possible_owners2['ids'] as $possibleOwnerId){
        if(!in_array($possibleOwnerId, $possible_owners['ids'])){
          $possible_owners['entity'][$possibleOwnerId] = $possible_owners2['entity'][$possibleOwnerId];
        }
      }
  }
  $possible_owners = $possible_owners["entity"];
  $cats1 = of_category_get_ordered('event', 'category1');

  $block = html_calendar_event_form($action, $calendar, $event_q, $exception_q, $possible_owners, $users, $contacts, $resources, $groups, $cats1, $alerts, $tags_q, $documents, $current_view, $template_entities, $date_edit_occurrence);

  if ($action == 'new' || $action == 'new_meeting' || $action == 'save_as_template') {
    $GLOBALS[display][search] = dis_calendar_select_template_form($action, $calendar);
  }

  $GLOBALS[display][search] .= dis_back_to_calendar($action);

  return $block;
}

function getOrganizerId($users, $contacts){
  foreach($users['entity'] as $organizer_id=>$user_entity){
    if($user_entity["is_organizer"]){
      return $organizer_id;
    }
  }
  if(empty($organizer_name) && is_array($contacts['entity'])){
    foreach($contacts['entity'] as $organizer_id=>$contact_entity){
      if($contact_entity["is_organizer"]){
        return $organizer_id;
      }
    }
  }

  return null;
}


function check_loggued_user_is_the_only_attendee($attendees){
  if(count($attendees) == 1 && $attendees[0]==$GLOBALS['obm']['uid']){
    return true;
  }
  return false;
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
function dis_calendar_quick_form($calendar_entity) {
  $user_id = $GLOBALS["obm"]["uid"];
  $user_entities = array();
  if ( $calendar_entity['user'] && is_array($calendar_entity['user']) ) {
    foreach ( $calendar_entity['user'] as $key => $val ) {
      $user_entities[$key] = $val;
    }
  }

  // add the current logged user to the list of selectable calendars if he's not already present
  if ( ! array_key_exists($user_id, $user_entities) ) {
    $session_user_infos = get_user_info($user_id);
    $user_entities[$user_id] = array(
      "name" => $session_user_infos["lastname"]." ".$session_user_infos["firstname"],
      "class" => "eventOwner",
      "type" => "user",
      "id" => $user_id
    );
  }

  if( sizeof($user_entities) > 1 ){
    $entity_view.="<select id=\"calendarViewEntity\">";
    foreach($user_entities as $user){
      if(OBM_Acl::canWrite($user_id, 'calendar',array($user['id']))) {
        $entity_view.="<option id='calendarViewEntity$user[id]' value='$user[id]'>$user[name]</option>";
      }
    }
    $entity_view.="</select>";
    $entity_view.="<input id='calendarKindEntity' type='hidden' value='user' />";
    $calendar_list = "
      <th>$GLOBALS[l_calendar]:</th>
      <td>$entity_view</td>";
  } else {
    $user = array_values($user_entities);
    $calendar_inputs ="<input id=\"calendarViewEntity\" type='hidden' value='".$user[0]['id']."' />";
    $calendar_inputs .="<input id=\"calendarKindEntity\" type=\"hidden\" value=\"user\" />";
  }

  // template form
  $templates_q = run_query_calendar_get_alltemplates($user_id);
  $eventtemplate_title = array();
  if ($templates_q->nf() > 0) {
    array_push($eventtemplate_title, "");
    while ($templates_q->next_record()) {
      $id = $templates_q->f('eventtemplate_id');
      $name = $templates_q->f('eventtemplate_name');
      array_push($eventtemplate_title, phpStringToJsString($templates_q->f('eventtemplate_title')));
      if (strlen($name) > 30) {
        $name2 = preg_replace("/(.{80})/","$1 ",$name);
        $title = "title=\"$name2\"";
      }
      $select.= "<option value=\"$id\" $title>$name</option>";
    }

    $block_template = "
      <th>$GLOBALS[l_template]:</th>
      <td><select id='template_id' name='template_id' onchange='setQuickTitle();'>
        <option value='$GLOBALS[c_none]'>$GLOBALS[l_undef]</option>
        $select
      </select></td>";
  }
  $js_templatetitle = phpArrayToJsArray($eventtemplate_title);

  $block = "
    <div id='calendarQuickForm' class='obmPopup'>
      <h1>
      <a href='#' id='popup_form_close' class='closeSticky' onclick='return false;'>
      <img src='$GLOBALS[ico_popup_close]' alt='close' />
      </a>
      <span id='calendarQuickFormDate'></span>
      </h1>
      <h2>
      <b id='extEventTitle'></b>
      <a id='calendarQuickFormTitle' href='#' onclick=\"obm.calendarQuickForm.goTo('detailconsult'); return false;\"></a>
      </h2> 
     <div class='content'>
        <form id='calendarQuickFormStore' onsubmit='obm.calendarQuickForm.submit();return false;'>
          $txt
          <table style='width: 90%'>
            <tr>
              <th>$GLOBALS[l_title]:</th>
              <td><input type=\"text\" maxlength='255' name=\"tf_title\" id=\"tf_quick_title\" /></td>
            </tr>
            <tr id='sel_template'>
              $block_template
            </tr>
            <tr id=\"calendarListEntity\">
              $calendar_list
            </tr>
          </table>
          $calendar_inputs
          <fieldset class='buttons'> 
            <input class='closeSticky' type='submit' value='$GLOBALS[l_validate]' />
            <input type='button' id='calendarQuickFormAdvanced' onclick='obm.calendarQuickForm.goTo();' />
            <input type='button' id='calendarQuickFormEdit' onclick=\"obm.calendarQuickForm.submit('detailupdate');\" />
            <input type='button' id='calendarQuickFormDetail' onclick=\"obm.calendarQuickForm.goTo('detailconsult');\" value='$GLOBALS[l_details]' />
            <input type='button' class='closeSticky' id='calendarQuickFormDelete' 
              onclick=\"obm.calendarQuickForm.submit('quick_delete');\" value='$GLOBALS[l_c_delete]' />
          </fieldset>
        </form>
        <div id='calendarQuickFormData'>
          <div id='calendarQuickFormAttendees'>
            <h3>$GLOBALS[l_members]</h3>
          </div>
          <div>
            <h3>$GLOBALS[l_carac]</h3>
            <table>
            <tr>
            <th>".__("Organizer")."</th>
            <td id=\"calendarQuickFormOrganizer\"></td>
            </tr>
            <tr>
            <th>$GLOBALS[l_location]</th>
            <td id='calendarQuickFormLocation'></td>
            </tr>
            <tr>
            <th>$GLOBALS[l_category1]</th>
            <td id='calendarQuickFormCategory'></td>
            </tr>
            </table>
          </div>
          <div>
            <h3>$GLOBALS[l_desc]</h3>
            <p id='calendarQuickFormDescription'></p>
          </div>
          <div id='calendarQuickFormItem'>
            <h3>$GLOBALS[l_items]</h3>
          </div>
      </div>
    </div>
    </div>
    <script type=\"text/javascript\">
      obm.calendarQuickForm = new Obm.CalendarQuickForm();
      obm.vars.labels.edit = '".phpStringToJsString($GLOBALS['l_edit'])."';
      obm.vars.labels.edit_full = '".phpStringToJsString($GLOBALS['l_edit_full'])."';
      obm.vars.labels.fill_title = '".phpStringToJsString($GLOBALS['l_fill_title'])."';
      obm.vars.consts.obm_uid = '$user_id';
      obm.vars.consts.template_title = $js_templatetitle;
    </script>";

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


/**
 * HTML for the occurency modification popup in the calendar view.
 *  
 * @access public
 * @return HTML
 */
function dis_calendar_occurency_popup() {
  $ico_popup_close = $GLOBALS['ico_popup_close'];
  $l_edit_occurrence = $GLOBALS['l_edit_occurrence'];
  $l_edit_event_occurrence = $GLOBALS['l_edit_event_occurrence'];
  $l_edit_all_occurrences = $GLOBALS['l_edit_all_occurrences'];
  $l_edit_occurrence_message = $GLOBALS['l_edit_occurrence_message'];
  $l_cancel = $GLOBALS['l_cancel'];

  $block = "
    <div id=\"calendarConfirmDelete\" class=\"obmPopup\">
      <h1>
        <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
          <img src=\"$ico_popup_close\" alt=\"close\" />
        </a>
        <span>$GLOBALS[l_confirm]</span>
      </h1>
      <div class=\"content\">
        <h2>$GLOBALS[l_delete_event_message]</h2>
         <fieldset class=\"buttons\"> 
          <input type=\"button\" id=\"popup_delete\" class=\"closeSticky\" value=\"$GLOBALS[l_yes]\" />
          <input type=\"button\" id=\"popup_cancel_delete\" class=\"closeSticky\" value=\"$GLOBALS[l_no]\" />
         </fieldset>
      </div>
    </div>

    <div id=\"calendarOccurencyUpdate\" class=\"obmPopup\">
      <h1>
        <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
          <img src=\"$ico_popup_close\" alt=\"close\" />
        </a>
        <span>$GLOBALS[l_update_event_occurrence]</span>
      </h1>
      <div class=\"content\">
        <h2>$GLOBALS[l_update_occurrence_message]</h2>
         <fieldset class=\"buttons\"> 
          <input type=\"button\" id=\"popup_update_one\" class=\"closeSticky\" value=\"$GLOBALS[l_update_occurrence]\" /><br/>
          <input type=\"button\" id=\"popup_update_all\" class=\"closeSticky\" value=\"$GLOBALS[l_update_all_occurrences]\" /><br/>
          <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$l_cancel\" />
         </fieldset>
      </div>
    </div>

    <div id=\"calendarOccurenceEdit\" class=\"obmPopup\">
      <h1>
        <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
          <img src=\"$ico_popup_close\" alt=\"close\" />
        </a>
        <span>$l_edit_event_occurrence</span>
      </h1>
      <div class=\"content\">
        <h2>$l_edit_occurrence_message</h2>
         <fieldset class=\"buttons\">
          <input type=\"button\" id=\"popup_edit_one\" class=\"closeSticky\" value=\"$l_edit_occurrence\" onclick=\"obm.calendarOccurenceEditPopup.editOne(null);\"/><br/>
          <input type=\"button\" id=\"popup_consult_all\" class=\"closeSticky\" value=\"$l_edit_all_occurrences\" onclick=\"obm.calendarOccurenceEditPopup.editAll(null);\"/><br/>
          <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$l_cancel\" />
         </fieldset>
      </div>
    </div>
    <script type=\"text/javascript\">
    obm.calendarOccurenceEditPopup = new Obm.calendarOccurenceEditPopup();
    </script>

    <div id=\"calendarOccurencyDelete\" class=\"obmPopup\">
      <h1>
        <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
          <img src=\"$ico_popup_close\" alt=\"close\" />
        </a>
        <span>$GLOBALS[l_delete_event_occurrence]</span>
      </h1>
      <div class=\"content\">
        <h2>$GLOBALS[l_delete_occurrence_message]</h2>
         <fieldset class=\"buttons\"> 
          <input type=\"button\" id=\"popup_delete_one\" class=\"closeSticky\" value=\"$GLOBALS[l_delete_occurrence]\" /><br/>
          <input type=\"button\" id=\"popup_delete_all\" class=\"closeSticky\" value=\"$GLOBALS[l_delete_all_occurrences]\" /><br/>
          <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$l_cancel\" />
         </fieldset>
      </div>
    </div>";

  return $block;
}

/**
 * HTML for Alarm popup in the event consult view.
 *  
 * @access public
 * @return HTML
 */
function dis_event_alarm_popup() {
	$aler_radio_array = array( -1, 60, 300, 600, 900, 1800, 3600, 7200, 18000, 43200, 86400, 172800, 604800 );
	foreach($aler_radio_array as $radiovalue){
		$alert_label = Of_Date::secondToString($radiovalue);
		if($radiovalue == -1){
			$radio_button .= "<label style=\"display:inline;\"><input type=\"radio\" name=\"sel_alert\"
			value=\"$radiovalue\">$GLOBALS[l_none]</label>";
		}else{
			if($radiovalue == 3600 || $radiovalue == 86400) $radio_button .= '<br/><br/>';
			$radio_button .= "<label style=\"display:inline;\"><input type=\"radio\" name=\"sel_alert\"
			value=\"$radiovalue\">$alert_label</label>";
		}
	}
	$block = "
	<div id=\"calendarAlarmPopup\" class=\"obmPopup\">
		<h1>
			<a href='#' id='popup_form_close' class='closeSticky' onclick='return false;'>
				<img src='$GLOBALS[ico_popup_close]' alt='close' />
			</a>
			<span>$GLOBALS[l_addedit_alarm]</span>
		</h1>
		<div class=\"content\">
			<form>
				<span class=\"radioAlarm\">$radio_button </span>
				<fieldset class=\"buttons\">
					<a href='' onclick=\"obm.calendarAlarmPopup.updateAlarm(); return false;\" class=\"closeSticky\">$GLOBALS[l_validate]</a>
					<a href='' id='popup_form_close' class='closeSticky' onclick='return false;'>$GLOBALS[l_cancel]</a>
				</fieldset>
			</form>
		</div>
	</div>
	<script type=\"text/javascript\">
		obm.calendarAlarmPopup = new Obm.CalendarAlarmPopup();
	</script>
	";
	return $block;
}

function dis_event_commented_decision_popup( $display_wait ) {
  $display_input = '';
  $marging = '23';
  if ( !$display_wait ){
    $display_input = " type=\"hidden\" ";
    $marging = '32';
  }

  $block = "
  <div id=\"commentedDecisionPopup\" class=\"obmPopup\">
    <h1>
      <a href='' id='popup_form_close' class='closeSticky' onclick='obm.commentedDecisionPopup.hide();return false;'>
      <img src='$GLOBALS[ico_popup_close]' alt='close' />
      </a>
      <span>$GLOBALS[l_change_state]</span>
    </h1>
    <div class=\"decisionOptions\">
      <form>
        <p><img src='$GLOBALS[ico_comment]' alt='comment:' /> $GLOBALS[l_comment_decision]
        <textarea id=\"comment\" placeholder=\"\" maxlength=\"255\"
          onKeyPress=\"obm.commentedDecisionPopup.displayCharLimit();\" onKeyUp=\"obm.commentedDecisionPopup.displayCharLimit();\" onKeyDown=\"obm.commentedDecisionPopup.displayCharLimit();\"></textarea>
        <span style=\"text-align:left;font-size:80%;\"><span id=\"charCountForDecision\" style=\"font-weight:bold;font-size:80%;\">255</span>&nbsp;$GLOBALS[l_remainingChar]</span>
        </p>
        <fieldset class=\"buttons\" style=\"margin-right: $marging%;\">
        <input type=\"submit\" href='' onclick=\"obm.commentedDecisionPopup.Accept(); return false;\" class=\"closeSticky\" value=\"$GLOBALS[l_accept]\"/>
        <input $display_input type=\"submit\" href='' onclick=\"obm.commentedDecisionPopup.Wait(); return false;\" class=\"closeSticky\" value=\"$GLOBALS[l_wait]\"/>
        <input type=\"submit\" href='' onclick=\"obm.commentedDecisionPopup.Refuse(); return false;\" class=\"closeSticky\" value=\"$GLOBALS[l_refuse]\"/>
        </fieldset>
      </form>
      <h2><a href='' id='popup_form_close' class='closeSticky' onclick='obm.commentedDecisionPopup.hide();return false;'>$GLOBALS[l_cancel]</a></h2>
    </div>
  </div>
  <script type=\"text/javascript\"> obm.commentedDecisionPopup = new Obm.CommentedDecisionPopup();</script>
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
//   - $documents    : Array : Attached documents
///////////////////////////////////////////////////////////////////////////////
function html_calendar_event_form($action, $calendar, $event_q, $exception_q, $possible_owners, $users, $contacts, $res, $groups, $cats1, $alerts, $tags_q, $documents, $current_view, $template_entities=null, $date_edit_occurrence=null) {
  global $obm, $path, $display, $cgp_show, $ccalendar_alerts;
  global $ccalendar_resource, $ccalendar_weekstart, $ccalendar_hour_fraction;
  global $ico_mini_cal, $ico_add,$ico_delete, $ico_project, $l_header_new;
  global $ico_cancel;
  global $ccalendar_first_hour, $ccalendar_last_hour;
  // -- Labels
  global $l_users, $l_attendees, $l_title, $l_location, $l_priority, $l_private, $l_desc;
  global $l_items, $l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_insert,$l_update,$l_repeat, $l_owner;
  global $l_opacity, $l_opacity_free, $l_opacity_busy;
  global $l_repeatkind,$l_repeatdays2, $l_date_repeatend, $l_no_end_repeat;
  global $l_daily, $l_weekly, $l_monthlybydate,$l_monthlybyday,$l_yearly;
  global $l_carac,$l_module_calendar;
  global $l_none, $l_none_f, $l_force,$l_repeat_update,$l_groups,$l_force_disabled;
  global $l_calendar_select_group, $l_hour,$l_all_day,$l_repeatfrequency;
  global $l_resources,$l_daysofweekshort, $l_exception, $l_items;
  global $l_alerts, $l_alerts_time, $l_color;
  global $l_documents, $l_allow_documents;
  global $l_save_as_template, $l_save_template;
  global $l_tag ;
  global $c_none ;
  $repeatfrequency = 1;
  $uid = $obm['uid'];
  $uid_infos = get_user_info($uid);
  $uid_label = $uid_infos['lastname'].' '.$uid_infos['firstname'];
  $tagcolor = "#eff0f2" ; // backcolor by default
  $organizer_name = null;
  $organizer_id = null;

  if ( isset($calendar['organizer']) ) {
    $organizer_id = $calendar['organizer'];
  } else {
    if(is_array($users['entity'])){
      foreach($users['entity'] as $user_id=>$user_entity){
	if($user_entity["is_organizer"]){
	  $organizer_name = $user_entity["label"];
	  $organizer_id = $user_id;
	  break;
	}
      }
    }
    if(empty($organizer_id) && is_array($contacts['entity'])){
      foreach($contacts['entity'] as $contact_id=>$contact_entity){
	if($contact_entity["is_organizer"]){
	  $organizer_name = $contact_entity["label"];
	  $organizer_id = $contact_id;
	  break;
	}
      }
    }
    if ( empty($organizer_id) ) {
      $write_allowed_entities = OBM_Acl_Utils::expandEntitiesArray(OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write', $current_view->get_users()));
      if(sizeof($write_allowed_entities['ids']) == 1 ) {
	  $organizer_id = $write_allowed_entities['ids']['0'];
      }
    }
    if(empty($organizer_id) ) {
      $organizer_id = $uid;
    }
  }




  $displayedPeople = array();
  if ($current_view->users_count() > 0) {
    $displayed_users = OBM_Acl_Utils::expandEntitiesArray(OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'read', $current_view->get_users()));
    foreach($displayed_users['entity'] as $id => $entity) {
      $name = phpStringToJsString($entity['label']);
      $displayedPeople[$id] = $name;
    }
  }

  //always add current user to the list of users that can be organizers
  if ( !array_key_exists($uid, $displayedPeople) ) {
    $displayedPeople[$uid] = phpStringToJsString($uid_label);
  }

  $possible_organizers = array();
  if(is_array($displayedPeople)){
    foreach($displayedPeople as $displayed_person_id=>$displayed_person_name){
      if(OBM_Acl::canWrite($uid, 'calendar',$displayed_person_id)) {
        $possible_organizers[$displayed_person_id] = $displayed_person_name;
      }
    }
    if(!array_key_exists($organizer_id, $possible_organizers)) $organizer_id = $uid;
  }
  else              {
      if (!OBM_Acl::canWrite($uid, 'calendar',$organizer_id)) {
          error_log("The current user of id $uid doesn't have write rights on the $calendar of user $organizer_id");
          # Redirect to calendar index
          header("Location: $GLOBALS[path]/calendar/calendar_index.php");
          exit;
      }
  }

  if (($action == 'detailupdate') || ($action == 'duplicate') || ($action == 'conflict_manager') || ($action == 'new' && $event_q != '') || ($action == 'edit_template')) {
    $is_exception = $date_edit_occurrence != null || $calendar['is_exception'] || $event_q->f('event_is_exception');
    $title = htmlspecialchars($event_q->f('event_title'));
    $location = htmlspecialchars($event_q->f('event_location'));
    $cat1 = $event_q->f('event_category1_id');
    $tagid = $event_q->f('event_tag_id');
    $taglabel = htmlspecialchars($event_q->f('eventtag_label'));
    $tagcolor = $event_q->f('eventtag_color');
    $owner = $event_q->f('event_owner');
    $owner_lastname = $event_q->f('owner_lastname');
    $owner_firstname = $event_q->f('owner_firstname');
    $opacity = $event_q->f('event_opacity');
    $priority = $event_q->f('event_priority');
    $properties = $event_q->f('event_properties');
    $desc = $event_q->f('event_description');
    $force_insertion = $event_q->f('event_force_insertion');
    if($force_insertion){
      $calendar['force'] = $force_insertion;
    }
    $show_user_calendar = $event_q->f('event_show_user_calendar');
    if($show_user_calendar){
      $calendar['show_user_calendar'] = $show_user_calendar;
    }
    $show_resource_calendar = $event_q->f('event_show_resource_calendar');
    if($show_resource_calendar){
      $calendar['show_resource_calendar'] = $show_resource_calendar;
    }
    
    if ($properties != '') {
      $charset = mb_internal_encoding();
      $item = "<?xml version='1.0' encoding='$charset' ?".">$properties";
    } else {
      $item = '';
    }
    if ($action == 'conflict_manager') {
      $begin = $calendar['date_begin'];
      $end = clone $begin;
      $end = $end->addSecond($calendar['duration']);
    } else {
      $begin = $date_edit_occurrence == null ? new Of_Date($event_q->f('event_date'), 'GMT') : new Of_Date($date_edit_occurrence);
      if (isset($calendar['template_id']) && $action == 'new') {
        $today = isset($calendar['date_begin']) ? $calendar['date_begin'] : new Of_Date(date("Y-m-d"), 'GMT');
        $begin->setDay($today->getDay());
        $begin->setMonth($today->getMonth());
        $begin->setYear($today->getYear());
      }
      $end = clone $begin;
      $end = $end->addSecond($event_q->f('event_duration'));
    }
    $all_day = $event_q->f('event_allday');
    if($all_day) {
      $end->subMinute(1);
    }
    $privacy = $event_q->f('event_privacy');
    $allow_documents = $event_q->f('event_allow_documents');
    if ($date_edit_occurrence == null) {
	    $repeat_kind = $event_q->f('event_repeatkind');
	    $repeat_days = $event_q->f('event_repeatdays');
	    $repeatfrequency = $event_q->f('event_repeatfrequence');
	    if ($repeat_kind != 'none') {
	      $event_endrepeat = $event_q->f('event_endrepeat');
	      if (!empty($event_endrepeat))
	        $repeat_end = new Of_Date($event_q->f('event_endrepeat'), 'GMT');
	    }
	    $id = $event_q->f('event_id');

	    if (isset($exception_q)) {
	      $index=0;
	      while($exception_q->next_record()) {
	        $date_exception[$index]['date'] = new Of_Date($exception_q->f('eventexception_date'), 'GMT');
	        $date_exception[$index]['childId'] = $exception_q->f('eventexception_child_id');
	        $index++;
	      }
	    }
    } else {
		$repeat_kind = 'none';
    }
    // Alerts
    if (is_array($alerts) && isset($alerts[$organizer_id])) {
      $selected_alert = $alerts[$organizer_id];
    } elseif (isset($calendar['template_id'])) {
      $selected_alert = $event_q->f('event_alert');
    }

    if ($action == 'duplicate') {
      $dis_title = "$title ($l_header_new)";
    } elseif ($action == 'edit_template') {
      $dis_title = $event_q->f('eventtemplate_name');
    } else {
      $dis_title = $title;
    }
  }
  if (isset($calendar['new_meeting'])) $event_q = '';

  if ($event_q=='') { $is_exception = $date_edit_occurrence != null || $calendar['is_exception']; }
  // If parameters have been given, they supercede the default action value
  if (isset($calendar['calendar_id'])) { $id = $calendar['calendar_id']; }
  if (isset($calendar['title'])) { $title = stripslashes(str_replace('"',"'",$calendar['title'])); }
  if (isset($calendar['location'])) { $location = stripslashes($calendar['location']); }
  if (isset($calendar['category1'])) { $cat1 = $calendar['category1']; }
  if (isset($calendar['owner'])) {$owner = $calendar['owner'];} elseif(!$owner) {$owner = $uid;}
  if (isset($calendar['priority'])) { $priority = $calendar['priority']; }
  if (isset($calendar['description'])) { $desc = stripslashes($calendar['description']); }
  if (isset($calendar['all_day'])) { $all_day = $calendar['all_day']; }
  if (isset($calendar['tag_id'])) { $tagid = $calendar['tag_id']; }
  if (isset($calendar['tag_label'])) { $taglabel = $calendar['tag_label']; }
  if (isset($calendar['tag_color'])) { $tagcolor = $calendar['tag_color']; }
  if (isset($calendar['repeatfrequency'])) { $repeatfrequency = $calendar['repeatfrequency']; }  
  if (isset($calendar['item'])) { $item = $calendar['item']; } 
  if (!is_object($event_q)) {$begin = $calendar['date_begin']; }
  //if (!isset($begin)) {$begin = new Of_Date(); $begin->setHour($ccalendar_first_hour)->setMinute(0)->setSecond(0);}
  if (!isset($begin)) {$begin = $current_view->get_day(); $begin->setHour($ccalendar_first_hour)->setMinute(0)->setSecond(0);}
  if (!is_object($event_q)) {$end = $calendar['date_end']; }
  if (!isset($end)) {$end = clone $begin; $end->setHour($ccalendar_first_hour + 1);}
  if (isset($calendar['privacy'])) { $privacy = $calendar['privacy']; }
  if (isset($calendar['repeat_kind'])) { $repeat_kind = $calendar['repeat_kind']; }
  if (isset($calendar['repeat_days']) && $calendar['repeat_days'] != '000000') { $repeat_days = $calendar['repeat_days'];}
  if (isset($calendar['repeat_end'])) $repeat_end = $calendar['repeat_end'];
  if (isset($calendar['force'])) { $force = $calendar['force']; }
  if (isset($calendar['force_disabled'])) { $force_disabled = $calendar['force_disabled']; }
  if (isset($calendar['opacity'])) { $opacity = $calendar['opacity']; }
  if ($repeat_kind != 'weekly') {
    $repeat_days = '000000';
    $repeatDaysDisplay = 'none';
  } else {
    $repeatDaysDisplay = "''";
  }
  if (isset($repeat_kind) && $repeat_kind != 'none') {
    $repeatDisplay = "''";
    $frequency_block = sprintf($GLOBALS['l_'.$repeat_kind.'_unit'], "<input type=\"text\" size=\"4\" style=\"width:3em;\" id=\"tf_repeatfrequency\" name=\"tf_repeatfrequency\" value=\"$repeatfrequency\" />");
  } else {
    $repeatDisplay = 'none';
    $frequency_block = "<input type=\"text\" size=\"4\" style=\"width:3em;\" id=\"tf_repeatfrequency\" name=\"tf_repeatfrequency\" value=\"$repeatfrequency\" />";
  }

  $attendees_notification  = 'checked';
  if (isset($calendar['date_exception'])) { $date_exception = null; }
  if (isset($calendar['list-item'])) { $t_list_item = $calendar['list-item']; }
  if (isset($calendar['list-item-id'])) { $t_list_item_id = $calendar['list-item-id']; }
  if (isset($calendar['property-name'])) { $t_property_name = $calendar['property-name']; }
  if (isset($calendar['property'])) { $t_property_value = $calendar['property']; }
  if (isset($calendar['others_attendees'])) { $others_attendees = $calendar['others_attendees']; }
  if (isset($calendar['add_displayed_users'])) { $add_displayed_users = $calendar['add_displayed_users']; }
  if (isset($calendar['show_user_calendar'])) { $show_user_calendar = ($calendar['show_user_calendar'] == 1 ? 'checked' : ''); }
  if (isset($calendar['show_resource_calendar'])) { $show_resource_calendar = ($calendar['show_resource_calendar'] == 1 ? 'checked' : ''); }
  if (isset($calendar['attendees_notification'])) { $attendees_notification = ($calendar['attendees_notification'] == 1 ? 'checked' : ''); }
  if (isset($calendar['alert'])) {
    // handle only one entry for now
    if (is_array($sel_alert)) {
      $selected_alert = $calendar['alert'][0];
    } else {
      $selected_alert = $calendar['alert'];
    }
  } else {
    if (isset($_SESSION['set_cal_alert']) && !isset($selected_alert)) {
      $selected_alert = $_SESSION['set_cal_alert'];
    }
  }
  
  if ($tagid == '') {
    $tagid = $c_none ;
    $tagcolor = "#eff0f2" ;
  }
  
  if ($action == "new" && $all_day) {
    $opacity = $_SESSION["set_allday_opacity"];
  }

  if ($opacity == 'TRANSPARENT') {
    $l_check_free = "checked='checked'";
  } else {
    $l_check_busy = "checked='checked'";
  }

  if ($action != 'edit_template' && $action != 'update_template') {
    $dis_attendees_notification = "
    <fieldset class=\"detail extra\">    
    <legend>$GLOBALS[l_notification]</legend>  
    <table>    
    <tr>    
      <th><label for=\"cba_attendees_notification\">$GLOBALS[l_sendamail]</label></th> 
      <td>
        <input style=\"margin:0\" type=\"checkbox\" id=\"cba_attendees_notification\" name=\"cba_attendees_notification\" value=\"1\" $attendees_notification />
      </td>
    </tr>
    </table>
    </fieldset>";
  }

  if (($action == 'detailupdate') || ($action == 'update') || ($action == 'conflict_manager')) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"calendar_id\" id=\"calendar_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" id=\"updateBtn\" value=\"$l_update\" />";
    if ($date_edit_occurrence !== null) {
      $dis_button .= "<input type=\"hidden\" name=\"date_edit_occurrence\" value=\"".htmlspecialchars($date_edit_occurrence)."\"/>";
    }
  } elseif (($action == 'new') || ($action == 'insert') || ($action == 'duplicate') || ($action == 'save_as_template')) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" id=\"insertBtn\" value=\"$l_insert\" />
      <input type=\"submit\" id=\"saveTemplateBtn\" onclick=\"if (check_calendar_calendar($('new_event_form'))) { displayTemplateCreate(); } return false;\" value=\"$l_save_as_template\" />";
  } elseif (($action == 'edit_template') || ($action == 'update_template')) {
      $template_id = isset($calendar['template_id']) ? $calendar['template_id'] : $event_q->f('eventtemplate_id');
      $dis_button = "
      <input type=\"hidden\" name=\"template_id\" id=\"template_id\" value=\"$template_id\" />
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"update_template\" />
      <input type=\"submit\" id=\"saveTemplateBtn\" value=\"$l_save_template\" />";
  }

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
  $block_category1 = of_category_dis_entity_form('event', 'category1', $cats1, 'mono', $cat1, 'none_f');

 // Repetition days
  $week = new Of_Date($ccalendar_weekstart);
  for ($i=0; $i<7; $i++) {
    $day = $week->localize(Of_Date::WEEKDAY_NARROW) ;
    $j = $week->getWeekday();
    if (strcmp(substr($repeat_days,$j,1),'1') ==0) {
      $dis_repeat_day .= "<span class=\"NW\"><label><input type=\"checkbox\" name=\"cba_repeatday_".$j."\" value=\"1\" checked = \"checked\"/>$day</label></span>&nbsp;&nbsp; ";
    } else {
      $dis_repeat_day .= "<span class=\"NW\"><label><input type=\"checkbox\" name=\"cba_repeatday_".$j."\" value=\"1\" />$day</label></span>&nbsp;&nbsp; ";
    }
    $week->addDay(1);
  }

  if ( $all_day ) {
    $style = 'display:none;';
    $all_day_checked = "checked=\"checked\"";
  }

  $dis_show_user_calendar = "<tr>
   <th><label for=\"cba_show_user_calendar\">$GLOBALS[l_show_user_calendar]</label></th>
    <td>
     <input type=\"checkbox\" id=\"cba_show_user_calendar\" value=\"1\" $show_user_calendar name=\"cba_show_user_calendar\" />
     </td>
    </tr>";

  // user select
  if (is_array($users['entity'])) {
    foreach ($users['entity'] as $u_id => $u_ent) {
      if (isset($u_ent['state']) && isset($u_ent['is_organizer']) 
          && $u_ent['state'] == 'DECLINED' && $u_ent['is_organizer'] === true)
          continue;
      
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

  if ($current_view->users_count() + $current_view->contacts_count() > 0) {
    
    $u = array();
    if ($current_view->users_count() > 0) {
      $displayed_users = OBM_Acl_Utils::expandEntitiesArray(OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'read', $current_view->get_users()));
      foreach($displayed_users['entity'] as $id => $entity) {
        $name = phpStringToJsString($entity['label']);
        array_push($u, "'$id' : '$name'");
      }
    }
    $users_js = "{".implode(",", $u)."}";

    $c = array();
    if ($current_view->contacts_count() > 0) {
      $displayed_contacts = get_contact_from_ids($current_view->get_contacts(), false);
      foreach($displayed_contacts['entity'] as $id => $entity) {
        $name = phpStringToJsString($entity['label']);
        array_push($c, "'$id' : '$name'");
      }
    }
    $contact_js = "{".implode(",", $c)."}";

    if ($add_displayed_users) {
      $add_displayed_users_checked = "checked=\"checked\"";
    }
    $block_add_displayed_users = "<tr>
      <th>
      <label for='cba_add_displayed_users'> $GLOBALS[l_add_displayed_users]</label>
      </th>
      <td>
        <input type='checkbox' id='cba_add_displayed_users' value='1' $add_displayed_users_checked 
          name='cba_add_displayed_users' 
          onclick=\"add_displayed_attendee(this.checked, $users_js, $uid, 'user', 'sel_user_id'); 
          add_displayed_attendee(this.checked, $contact_js, $uid, 'contact', 'sel_contact_id'); \"/>
      </td>
      </tr>";
  }
  if (is_array($contacts['entity'])) {
    foreach ($contacts['entity'] as $u_id => $u_ent) {
      if($template_entities == null || (is_array($template_entities['contact']) && in_array($u_id, $template_entities['contact'])))
      {
        $u_name = $u_ent['label'];
        $sel_id = "data-contact-$u_id";
        $div_id = "sel_contact_id-$sel_id";
        $dis_sel_contact .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_contact_id');\">
        <img src=\"$ico_delete\" alt=\"[Delete]\" />
        </a>
        $u_name
        <input value=\"$sel_id\" name=\"sel_contact_id[]\" type=\"hidden\" />
        </div> ";
      }
    }
  }

  $url = "&amp;action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id&amp;filter_entity=calendar&amp;filter_pattern=access";
  $user_add_button = "
    <a href=\"javascript: return false;\" 
      onclick=\"popup(\$('userSearchSource').value+'$url','');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";
  foreach ($possible_owners as $o) {
    if(empty($organizer_name) && $o['id']==$organizer_id) $organizer_name = $o[label];
  }

  $block_organizer = "
    <th>
      ".__("Organizer")."
    </th>
    <td>";
      if( count($displayedPeople) == 1 && $organizer_id == $uid){
        $block_organizer .= "
        <span>$organizer_name</span>
        <input type=\"hidden\" name=\"sel_organizer\" value=\"".$organizer_id."\" />";
      }
      else{
        $block_organizer .= "
        <select name=\"sel_organizer\">";
        foreach($possible_organizers as $displayed_person_id=>$displayed_person_name){
          $selected_person = $displayed_person_id == $organizer_id ? "selected=selected" : "";
          $block_organizer .= "<option $selected_person
                                      value=\"".$displayed_person_id."\">".
                                $displayed_person_name.
                              "</option>";
        }
      }
      $block_organizer .=
  "</td>";
  
  $dis_others_attendees = dis_calendar_mail_field($others_attendees, $class);
  if ($has_access_to_other_calendars = OBM_Acl::hasAllowedEntities($obm['uid'], 'calendar', 'access') || true) { // FIXME
    $addressbooks = OBM_Addressbook::search();
    foreach($addressbooks as $id => $addressbook) {
      $addressbook_options .= "<option value=\"$path/contact/contact_index.php?from=calendar&amp;addressbook_id=".$addressbook->id."\">".$addressbook->displayname."</option>";
    }    
    $block_user = "
      <fieldset class=\"detail infos\">
      <legend>$l_attendees</legend>    
      <table>
      $block_add_displayed_users
      <tr>
      <th>
      $l_users / $GLOBALS[l_contacts]
      </th>
      <td class=\"autocompleteField\">
      <select id=\"userSearchSource\" class=\"autocompleteSource\" name=\"\" style=\"float: right;\" onchange=\"set_autocomplete_container(this.value);\">
        <option value=\"$path/user/user_index.php?from=calendar\">$GLOBALS[l_users]</option>
        <optgroup label=\"$GLOBALS[l_addressbook]\">
          $addressbook_options
        </optgroup>
      </select>
      <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" />
      <input type='hidden' id='userSearchContainer' value='sel_user_id'/>
      </td>
      </tr>
      <tr>
        <th>&nbsp;</th>    
        <td class='autocompleteResult'><span class='legend'>$GLOBALS[l_user]</span>
        <div id='sel_user_id'>$dis_sel_user</div><span class='legend'>$GLOBALS[l_contact]</span><div id='sel_contact_id'>$dis_sel_contact</div>
        </td>
      </tr>
      <tr>
      $dis_others_attendees
      </tr>
      $dis_show_user_calendar
      </table>
      </fieldset>
      <p class=\"LC\"></p>";
      $user_complete = "obm.vars.consts.autocomplete_ext = new obm.AutoComplete.ExtSearch(
        function() { return \$('userSearchSource').value+'&action=ext_search'; },
        'sel_user_id', 'userSearch', {filter_entity:'calendar',filter_pattern:'access'});";
  } else {
    $block_user = "<fieldset class=\"detail infos H\">
      <div class='elementRow' id='sel_user_id-data-user-$obm[uid]'>
      <input type='hidden' name='sel_user_id[]' value='data-user-$obm[uid]' />".$users['entity'][$obm['uid']]['label']."
      </div></fieldset>";
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
  
  if ($allow_documents == 1) {
    $dis_allow_documents = "<input type=\"checkbox\" id=\"cba_allow_documents\" value=\"1\" checked=\"checked\" name=\"cba_allow_documents\" />";
  } else {
    $dis_allow_documents = "<input type=\"checkbox\" id=\"cba_allow_documents\" value=\"1\" name=\"cba_allow_documents\" />";
  }
  
  $block_document = "
    <fieldset class=\"detail infos\">
    <legend>$l_documents</legend>    
    <table>
    <tr>
      <th><label for=\"cba_allow_documents\">$l_allow_documents</label>
      </th>
      <td>
        $dis_allow_documents
      </td>
    </tr>
    ".html_calendar_documents_form_fieldset($documents['entity'])."
    </table>
    </fieldset>";
    
  $document_complete = "new obm.AutoComplete.Search('$path/document/document_index.php?action=ext_search', 'sel_document_id', 'documentSearch');";
  
  if ($ccalendar_resource && OBM_Acl::hasAllowedEntities($obm['uid'], 'resource', 'access')) {

    $dis_show_resource_calendar = "<tr>
     <th><label for=\"cba_show_resource_calendar\">$GLOBALS[l_show_resource_calendar]</label></th>
      <td>
       <input type=\"checkbox\" id=\"cba_show_resource_calendar\" value=\"1\" $show_resource_calendar name=\"cba_show_resource_calendar\" />
       </td>
      </tr>";


    // resource select
    if ($action == 'new' && !isset($calendar['new_meeting'])) {
      if ($current_view->resources_count() > 0) {
        $u = array();
        foreach($res['entity'] as $id => $resource) {
          $name = phpStringToJsString($resource["label"]);
          array_push($u, "'$id' : '$name'");
        }
        $res_js = "{".implode(",", $u)."}";
        if ( $add_displayed_res ) {
          $add_displayed_res_checked = "checked='checked'";
        }
        $block_add_displayed_res = "<tr>
          <th>
          <label for='cba_add_displayed_res'>$GLOBALS[l_add_displayed_resources]</label>
          </th>
          <td>
            <input type='checkbox' id='cba_add_displayed_res' value='1' $add_displayed_res_checked name='cba_add_displayed_res'
              onclick=\"add_displayed_attendee(this.checked, $res_js, $uid, 'resource', 'sel_resource_id')\"/>
          </td>
          </tr>";
      }
      
      if (is_array($res['entity'])) {
        foreach ($res['entity'] as $u_id => $u_ent) {
          if(     is_array($template_entities['resource'])
             &&   in_array($u_id, $template_entities['resource']))
          {
            $u_name = $u_ent['label'];
            $sel_id = "data-resource-$u_id";
            $div_id = "sel_resource_id-$sel_id";
            $dis_sel_resource .= "<div class=\"elementRow\" id=\"$div_id\">
            <a href=\"javascript: remove_element('$div_id','sel_resource_id');\">
            <img src=\"$ico_delete\" alt=\"[Delete]\" />
            </a>
            $u_name
            <input value=\"$sel_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
            </div> ";
          }
        }
      }

      if (is_array($template_res_id)) {
        $resources = get_calendar_resource_from_ids($template_res_id);
        $res = $resources['entity'];
        if (is_array($res)) {
          foreach($res as $id => $res) {
            $infos = get_resource_info($id[2]);
            $u_name = $res['label'];
            $sel_id = "data-resource-$id";
            $div_id = "sel_resource_id-$sel_id";
            $dis_sel_resource .= "<div class=\"elementRow\" id=\"$div_id\">
            <a href=\"javascript: remove_element('$div_id','sel_resource_id');\">
            <img src=\"$ico_delete\" alt=\"[Delete]\" />
            </a>
            $u_name
            <input value=\"$sel_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
            </div>";
          }
        }
      } 
    } else if ($action == 'detailupdate' && isset($calendar['new_meeting']) && $calendar['new_meeting']==1) {
      $res = $calendar['resource_id'];
      if (is_array($res)) {
        foreach($res as $u) {
          $id = explode("-", $u);
          $infos = get_resource_info($id[2]);
          $u_name = $infos['name'];
          $sel_id = $u;
          $div_id = "sel_resource_id-$sel_id";
          $dis_sel_resource .= "<div class=\"elementRow\" id=\"$div_id\">
          <a href=\"javascript: remove_element('$div_id','sel_resource_id');\">
          <img src=\"$ico_delete\" alt=\"[Delete]\" />
          </a>
          $u_name
          <input value=\"$sel_id\" name=\"sel_resource_id[]\" type=\"hidden\" />
          </div>";

        }
      }
    } else {
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
          if (is_object($xml) && ($action == 'detailupdate' || $action == 'update'  || $action == 'duplicate' || ($action == 'new' && isset($calendar['template_id'])))) {
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
      $block_add_displayed_res
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
    $dis_show_resource_calendar
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

  // the following code is perhaps a little complicated to just do a 0 to 23 loop !!!
  $hour_size = "4em";
  if ($_SESSION['set_timeformat'] == "12H") $hour_size = "5em";
  $dis_hour_b = "<select id='sel_time_begin' name='sel_time_begin' style='width:$hour_size;'>";
  $dis_hour_e = "<select id='sel_time_end' name='sel_time_end' style='width:$hour_size;'>";
  $cur_hour_b = new Of_Date();
  $cur_hour_b->setHour(0);
  $cur_hour_b->setMinute(0);
  $cur_hour_b->setSecond(0);
  $cur_hour_e = clone $cur_hour_b;
  $cur_hour_e->addDay(1);

  while($cur_hour_b->compare($cur_hour_e) < 0) {
    $hour = $cur_hour_b->getOutputHour();
    $formatedHour = $cur_hour_b->getHour();
    if ($begin->compareHour($cur_hour_b) == 0) {
      $dis_hour_b .= "<option value=\"$formatedHour\" selected=\"selected\">$hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$formatedHour\">$hour</option>";
    }
   if ($end->compareHour($cur_hour_b) == 0) {
     $dis_hour_e .= "<option value=\"$formatedHour\" selected=\"selected\">$hour</option>";
   } else {
     $dis_hour_e .= "<option value=\"$formatedHour\">$hour</option>";
   }    

    $cur_hour_b->addHour(1);
  }
  $dis_hour_b .= '</select>';  
  $dis_hour_e .= '</select>';  
  // end of the crapy complicated code

  // the following code is perhaps a little complicated to just do a 0 to 59 loop (with varying pad) !!!
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
  // end of the crapy complicated code

  $display['title'] = "$dis_title";
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[5]";
  $url2 = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[6]&amp;ext_title=" . urlencode($l_calendar_select_group);
  // Exception
  $repeat_end_value = isset($repeat_end) ? $repeat_end->getInputDate() : '';
  $$repeat_kind .= "selected=\"selected\" ";
  $dis_sel_kind .= "
      <select onchange=\"repeat_form($(this).get('inputValue'))\" name=\"sel_repeat_kind\" id=\"sel_repeat_kind\">
        <option value=\"none\">$l_none</option>
        <option value=\"daily\" $daily>$l_daily</option>
        <option value=\"weekly\" $weekly>$l_weekly</option>
        <option value=\"monthlybydate\" $monthlybydate>$l_monthlybydate</option>
        <option value=\"monthlybyday\" $monthlybyday>$l_monthlybyday</option>
        <option value=\"yearly\" $yearly>$l_yearly</option>
      </select>";
  if(is_array($date_exception)) {
    foreach($date_exception as $key => $exception) {
      $block_exception_detail .= "<div id=\"date_exception-$key\">";
      if (!$exception['childId']) {
        $block_exception_detail .= "<a href=\"javascript: remove_element('date_exception-$key','exceptionHome');\">";
        $exception_desc = $GLOBALS['l_eventexception_canceled'];
      } else {
        $block_exception_detail .= "<a style=\"visibility:hidden\" href=\"javascript: remove_element('date_exception-$key','exceptionHome');\">";
        $exception_url = "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&calendar_id=".$exception['childId'];
        $exception_link = "<a href=\"$exception_url\">".$exception['date']->getOutputDateTime()."</a>";
        $exception_desc = sprintf($GLOBALS['l_eventexception_moved_to'],$exception_link);
      }
      $block_exception_detail .= "<img src=\"$ico_delete\" alt=\"[Delete]\">
								  </a>";
      $block_exception_detail .= "<input type=\"hidden\" name=\"tf_date_exception[]\" value=\"".$exception['date']->getInputDate()."\" />
								 ".$exception['date']->getInputDate()." ($exception_desc)
								 </div>";
    }
  }
  $add_date = "<a href=\"javascript: add_exdate();\"><img src=\"$ico_add\" alt=\"[Date]\" /></a>";

  if ($is_exception) {
    $parent_event_id = $event_q->f('event_eventexception_parent_id') ? $event_q->f('event_eventexception_parent_id') : $calendar['calendar_id'];
    $parent_event_url = "$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&calendar_id=$parent_event_id";
    $show_parent_link = "<a href=\"$parent_event_url\" title=\"$GLOBALS[l_parent_event]\">$GLOBALS[l_show_parent_event]</a>";
    $block_repeat = "
    <div class=\"detail infos\">
     <h1>$l_repeat</h1>
     <input type=\"hidden\" name=\"tf_repeatfrequency\" value=\"1\" />
     <table>
      <tr>
       <th>$GLOBALS[l_exception] :</th>
       <td>$GLOBALS[l_event_is_exception]</td>
      </tr>
      <tr>
       <th>$GLOBALS[l_parent_event] :</th>
       <td>$show_parent_link</td>
      </tr>
     </table>
    </div>";
  } else {
    $block_repeat = "
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
    <tr id=\"noRepeatEnd\">    
      <th><label for=\"cbx_no_repeatend\">$l_no_end_repeat</label></th> 
      <td><input type=\"checkbox\" id=\"cbx_no_repeatend\" onclick=\"toggleDisplay($('repeatEnd'));\" /></td>
    </tr>
    <tr id=\"repeatEnd\" style=\"display:$repeatDisplay\">    
      <th>$l_date_repeatend</th> 
      <td>
        <input type=\"text\" name=\"tf_repeat_end\" id=\"tf_repeat_end\" value=\"$repeat_end_value\" class=\"datePicker\" />
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
    </fieldset>";
    $js_event_repeat = "
    if ($('tf_repeat_end').value == '') {
      $('cbx_no_repeatend').checked = true;
    }
    repeat_form($('sel_repeat_kind').value);";
  }

  // Alerts
  $sel_alert = "<select name=\"sel_alert\">
        <option value=\"$c_none\">$l_none_f</option>";
  $user_alert = $ccalendar_alerts;
  if($selected_alert && ($selected_alert!= $c_none) && !($user_alert[$selected_alert])) {
    $user_alert[$selected_alert] = Of_Date::secondToString($selected_alert);
  }  
  foreach ($user_alert as $alert_sec => $alert_label) {
    if(!$alert_label) $alert_label = Of_Date::secondToString($alert_sec);
    $sel_alert .= "<option value=\"$alert_sec\"";
    if ($selected_alert == $alert_sec) {
      $sel_alert .= " selected=\"selected\"";
    }
    $sel_alert .= ">$alert_label</option>";
  }
  $sel_alert .= '</select>';
  
  // rewrite date_edit_occurrence parameter if set (eg in case we're here bc of a conflict)
  $date_edit_occurrence = "";
  if ( $calendar["date_edit_occurrence"] ) {
    $date_edit_occurrence = '<input type="hidden" name="date_edit_occurrence" value="'.str_replace('"','&quot;',$calendar["date_edit_occurrence"]).'">';
  }

  // Template name popup
  $template_popup = html_calendar_template_name_popup();

  $GLOBALS['js']['vars']['consts']['calendarUrl'] = $GLOBALS[path].'/calendar/'.basename($_SERVER['SCRIPT_NAME']);
  $freebusy = html_freebusy_header();

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method='post' enctype='multipart/form-data' id='new_event_form' name='f_entity'
   onsubmit='return check_calendar_calendar(this);' action=\"".basename($_SERVER['SCRIPT_NAME'])."\">
$date_edit_occurrence
  <fieldset class='detail infos'>  
  <legend>$l_module_calendar</legend>
  <table>
  <tr>
    <th>$l_title</th>
    <td><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"250\" size=\"25\" value=\"$title\" /></td>
  </tr> 
  <tr>
    $block_organizer
  </tr>
  <tr>
    <th>$l_location</th>
    <td><input type=\"text\" id=\"tf_location\" name=\"tf_location\" maxlength=\"60\" size=\"25\" value=\"$location\" /></td>
  </tr> 
    $block_category1
  <tr>
    <th><label for=\"cba_force\">$l_force</label></th>
    <td>$dis_force</td>
  </tr>   
  </table>
  </fieldset>
  <fieldset class=\"detail infos\">
  <legend>$l_carac</legend>
  <table>
  <tr>
    <th><label for=\"cba_privacy\">$l_private</label></th>
    <td>$dis_privacy</td>
  </tr> 
  <tr>
    <th><label for=\"cba_all_day\">$l_all_day</label></th>
    <td>
      <input onchange=\"show_hide_calendar_dates(this, '" . $_SESSION["set_allday_opacity"] . "')\" onclick=\"show_hide_calendar_dates(this, '" . $_SESSION["set_allday_opacity"] . "');\" type=\"checkbox\" id=\"cba_all_day\" name=\"cba_all_day\" value=\"1\" $all_day_checked />
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
      <input type=\"text\" name=\"tf_date_end\" id=\"tf_date_end\" value=\"".$end->getInputDate()."\" class=\"datePicker\" onkeypress=\"repeatEndAuto=false;\" />
      <span id=\"hour_end\" style=\"$style\"  class=\"NW\">$l_hour : $dis_hour_e : $dis_min_e</span>
    </td>       
  </tr>
  <tr>
    <th>$l_opacity</th>
    <td>
      <span class=\"NW\">
        <input type=\"radio\" name=\"rd_opacity\" id=\"rd_opacity_busy\" $l_check_busy value=\"OPAQUE\" />
        <label for='rd_opacity_busy'>$l_opacity_busy</label>
      </span>
      <span class=\"NW\">
        <input type=\"radio\" name=\"rd_opacity\" id=\"rd_opacity_free\" $l_check_free value=\"TRANSPARENT\" />
        <label for='rd_opacity_free'>$l_opacity_free</label>
      </span>
    </td>
  </tr>
  <tr>
    <th>&nbsp</th>
    <td><a href='javascript: void(0);' onclick='checkAvailability();'>$GLOBALS[l_freebusy_check_availability]</a></td>
  </tr>
  <tr>
    <th>$l_priority</th>
    <td>$dis_sel_prio</td>
  </tr>     
  <tr>
    <th>$l_tag</th>
    <td>
      <div id=\"tag_wrapper\">
        <input type=\"text\" value=\"$taglabel\" name=\"tf_tag_label\" id=\"tf_tag_label\" style=\"width:70%;\">
        <div id=\"display_tag_color\">&nbsp;</div>
        <a href=\"#\" onclick=\"tag_reset();return false\"><img alt=\"reset\" title=\"reset\" src=\"$ico_cancel\"></a>
      </div>
      <script type=\"text/javascript\" langage=\"javascript\">
        obm.initialize.chain(function () {
          $('display_tag_color').setStyle('background-color', '$tagcolor') ;
          obm.calendarColorPicker.manualSet('$tagcolor', 'hex') ;
          add_label_autocomplete('tf_tag_label');
        });
      </script>
      <input type=\"hidden\" name=\"tf_tag_color\" id=\"tf_tag_color\" value=\"$tagcolor\">
      <input type=\"hidden\" name=\"tf_tag_id\" id=\"tf_tag_id\" value=\"$tagid\">
    </td>
  </tr>     
  </table>
  </fieldset>
  $block_repeat
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
  $block_document

  <fieldset class=\"detail extra\">    
  <legend>$l_desc</legend>  
  <textarea id=\"ta_description\" name=\"ta_description\" rows=\"3\" cols=\"72\">$desc</textarea> 
  </fieldset>
  $dis_attendees_notification
  <fieldset class=\"buttons\">$dis_button $hidden_repeat</fieldset>
  </form>
  $freebusy

  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      $user_complete
      $resource_complete
      $group_complete
      $document_complete
    });
    $js_event_repeat
  </script>
  $template_popup
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
  global $l_motivation, $l_state;
  
   
  $owner_notification = "checked";
  if (isset($_GET['owner_notification'])) {$owner_notification =  ($_GET['owner_notification'] == "true" ? "checked":"");}
  
  $block = "<div class=\"detail \">
      <input type=\"checkbox\" id=\"owner_notification\" $owner_notification /><label for=\"owner_notification\">$GLOBALS[l_owner_notification]</label>
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
      ".dis_event_commented_decision_popup(false)."
      <script type=\"text/javascript\"> 
      obm.vars.consts.calendarUrl = '".$GLOBALS[path].'/calendar/'.basename($_SERVER['SCRIPT_NAME'])."';
      </script>
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
        <th>$l_motivation</th>
        <th>$l_state</th>
      </tr>
      </thead>
      <tbody>";
    }

    switch ($obm_wait->f('event_priority') ) {
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
    
    $current_entity_comment = "$GLOBALS[l_add_comment]...";
    $title_unescaped = htmlspecialchars($obm_wait->f('event_title'));
    $escaped_title = phpStringToJsString(str_replace("\n\r", ', ', $title_unescaped));
    $block .= "
    <tr style=\"text-align:center;\">
     <td style=\"text-align:left;\">
       <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=".$obm_wait->f('event_id')."\">".htmlspecialchars($obm_wait->f('event_title'))."</a>
       <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=waiting_events&show_calendar=1&date=$begin\"><img src=\"$GLOBALS[ico_calendar]\" /></a>
     </td>
     <td>$periodic</td> 
     <td>".htmlspecialchars($obm_wait->f('event_location'))."</td>
     <td>".$obm_wait->f('eventcategory1_label')."</td>
     <td>$priority</td>
     <td>$user_create</td>
     <td>".$begin->getOutputDateTime()."</td>
     <td>".$end->getOutputDateTime()."</td>
     <td>
      <a  class=\"editcomment\" name=\"rd_contact_decision\" value=\"ACCEPTED\" onclick=\"obm.commentedDecisionPopup.compute($current_entity, ".$obm_wait->f('event_id').", 'ACCEPTED', 'NEEDS-ACTION', '$current_entity_entity', '$current_entity_comment', '$escaped_title', 'waiting_events', 'true')\" />
     </td>
     <td>
      <form method=\"post\" action=\"".basename($_SERVER['SCRIPT_NAME'])."\">
        <input type=\"hidden\" name=\"action\" value=\"decision\" />
        <input type=\"hidden\" name=\"calendar_id\" value=\"".$obm_wait->f('event_id')."\" />
        <input type=\"hidden\" name=\"entity_kind\" value=\"$current_entity_entity\" />
        <input type=\"hidden\" name=\"entity_id\" value=\"$current_entity\" />
        <input type=\"hidden\" name=\"owner_notification\" value=\"true\" />
        <input type=\"hidden\" name=\"date_begin\" value=\"$date_begin\" />
        <input type=\"hidden\" name=\"date_end\" value=\"$date_end\" />
        <input type=\"hidden\" name=\"time_end\" value=\"$time_end\" />
        <input type=\"hidden\" name=\"min_end\" value=\"$min_end\" />
        <input type=\"hidden\" name=\"uriAction\" value=\"waiting_events\" />
        <span class=\"NW\"><input type=\"radio\" name=\"rd_decision_event\" value=\"ACCEPTED\"  onclick=\"submitWaiting(this.form)\" />$l_accept</span>
        <br />
        <span class=\"NW\"><input type=\"radio\" name=\"rd_decision_event\" value=\"DECLINED\" onclick=\"submitWaiting(this.form)\" />$l_refuse</span>
      </form>
     </td>
    </tr>";
  }

  if($block != '') {
    $block .= '</tbody></table>';
  }
  
  $block = "<div id='waitingEventsContainer'>$block</div>";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Delete Options
// Parameters:
//   - $current_view
//   - $event_id
///////////////////////////////////////////////////////////////////////////////
function html_calendar_dis_delete($current_view, $event_id, $exception_date) {
  $ent_q = run_query_get_events_attendee(array($event_id));

  if ($ent_q->nf() > 1 || ($ent_q->next_record() &&  $ent_q->f('eventlink_entity') == 'user' && $ent_q->f('eventlink_entity_id') != $GLOBALS['obm']['uid'])) {
    $mail = "<input id='send_mail' name='send_mail' value='1' type='checkbox' checked='checked'><label for='send_mail'>$GLOBALS[l_sendamail]</label>";
  }

  $event_details_q = run_query_calendar_detail($event_id);

  $is_recurrence = ($event_details_q->f('event_repeatkind') != 'none') && !isset($exception_date);
  $message = $is_recurrence ? $GLOBALS['l_confirm_delete_all'] :
      $GLOBALS['l_confirm_delete_single'];
  $block = "
   <h1>$message</h1>
   <div class=\"C\">
    <form method='POST' id='deleteForm' action='$GLOBALS[path]/calendar/calendar_index.php'>
      <input type='hidden' name='action' value='delete' />
      <input type='hidden' name='calendar_id' value='$event_id' />
      <input type='hidden' name='exception_date' value='$exception_date' />
      $mail
    </form>
    <input type='button' onclick=\"$('deleteForm').submit();\" value='$GLOBALS[l_yes]'/>
    <input type='button' onclick=\"window.location.href='".basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&calendar_id=$event_id'\" value='$GLOBALS[l_no]'/>
   </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $calendar        : calendar parameters
//   - $sel_entity_id : array of entity Id ['user'] ['resource']
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_meeting_form($current_view, $calendar) {
  global $display, $path;
  global $ico_add, $ico_delete, $ico_mini_cal;
  global $ccalendar_resource, $obm;
  global $l_users,$l_datebegin,$l_dateend,$l_header_meeting,$l_event_duration,$l_meeting_perform; 
  global $l_groups,$l_resources,$l_module_resourcegroup, $l_module_calendar;
  global $l_resources_one_freebusy,$l_resources_all_freebusy,$l_resource_group_search;

  $date = $current_view->get_day();
  $hourbegin = $calendar['time_duration'];
  $minbegin = $calendar['min_duration'];
  if (($hourbegin == '') || ($hourbegin <= 0)) {
    $hourbegin = '1';
  }
  $dis_button = "<input type=\"button\" onclick=\"performMeeting();\" value=\"$GLOBALS[l_meeting_perform]\" />";
  $calendar_entity = $current_view->get_entities();

  if (isset($calendar['template_id'])) {
    $dis_button.= "<input type=\"hidden\" name=\"template_id\" id=\"template_id\" value=\"$calendar[template_id]\" />";
    list($event_q, $template_entities) = run_query_calendar_get_template($calendar['template_id']);
    $users_id = $template_entities['user'];
    $res_id = $template_entities['resource'];
    $groups_id = $template_entities['group'];
    $contacts_id = $template_entities['contact'];
  } else {
    $users_id = $current_view->get_users();
    $res_id = $current_view->get_resources();
    $group = $current_view->get_group();
    $contacts_id = $current_view->get_contacts();
    $groups_id = ( (empty($group) || $group==$GLOBALS['c_all']) ? array($group) : array() );
  }
  
  if (count($users_id) > 0) {
    $users = get_userobm_from_ids($users_id);
  } else {
    $users = array();
  }
  
  if (count($res_id) > 0) {
    $res = get_calendar_resource_from_ids($res_id);
  } else {
    $res = array();
  }

  if (count($contacts_id) > 0) {
    $contacts = get_contact_from_ids($contacts_id, false);
  } else {
    $contacts = array();
  }

  if (count($groups_id) > 0) {
//    $groups = get_calendar_group_from_ids($groups_id);
  } else {
    $groups = array();
  }
  
  // we check if all template entities have been successfully loaded ; if not (deleted ? ACL ?), we notice the user
  if (isset($calendar['template_id'])) {
    $loaded['user'] = isset($users['ids']) ? $users['ids'] : array();
    $loaded['resource'] = isset($res['ids']) ? $res['ids'] : array();
    $loaded['group'] = isset($groups['ids']) ? $groups['ids'] : array();
    foreach ($loaded as $type => $loaded_ids) {
      if (count(array_diff($template_entities[$type], $loaded_ids)) > 0) {
        $GLOBALS['display']['msg'] .= display_warn_msg($GLOBALS['l_template_entities_loading_warning']);
        break;
      }
    }
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

  // contact select
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
      </div> ";
    }
  }

  $url = "&amp;action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id&amp;filter_entity=calendar&amp;filter_pattern=access";
  $user_add_button = "
    <a href=\"javascript: return false;\" 
      onclick=\"popup(\$('userSearchSource').value+'$url','');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";
    $addressbooks = OBM_Addressbook::search();
    foreach($addressbooks as $id => $addressbook) {
      $addressbook_options .= "<option value=\"$path/contact/contact_index.php?from=calendar&amp;addressbook_id=".$addressbook->id."\">".$addressbook->displayname."</option>";
    }    
    $block_user = "
      <tr>
      <th>
      $GLOBALS[l_users] / $GLOBALS[l_contacts]
      </th>
      <td class=\"autocompleteField\">
      <select id=\"userSearchSource\" class=\"autocompleteSource\" name=\"\" style=\"float: right;\">
        <option value=\"$path/user/user_index.php?from=calendar\">$GLOBALS[l_users]</option>
        <option value=\"$path/contact/contact_index.php?from=calendar\">$GLOBALS[l_contacts]</option>
        <optgroup label=\"$GLOBALS[l_addressbook]\">
          $addressbook_options
        </optgroup>
      </select>
      <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" />
      </td>
      </tr>
      <tr>
      <th>&nbsp;</th>    
      <td id=\"sel_user_id\" class=\"autocompleteResult\">$dis_sel_user</td>
      </tr>";

    $user_js = "
    new obm.AutoComplete.ExtSearch(
        function() { return \$('userSearchSource').value+'&action=ext_search'; },
        'sel_user_id', 'userSearch', {filter_entity:'calendar',filter_pattern:'access'});
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
      </table>
      <input type='hidden' name='resource_group_search' value='all' />
      </fieldset>";    
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
  if (OBM_Acl::hasAllowedEntities($obm['uid'], 'calendar', 'access') || true) { // FIXME
    $dis_block_user = "<fieldset class=\"detail infos\">  
      <legend>$GLOBALS[l_attendees]</legend>
      <table>
        $block_user
      <tr>
        $block_group
        </tr>
      </table>
      </fieldset> ";
  } else {
    $dis_block_user = "<fieldset style='display:none;' 'class=\"detail infos\">  
      <legend>$GLOBALS[l_attendees]</legend>
      <table>
        $block_user
      <tr>
        $block_group
        </tr>
      </table>
      </fieldset> ";
    $user_js = '';
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

  $GLOBALS['js']['vars']['consts']['calendarUrl'] = $GLOBALS[path].'/calendar/'.basename($_SERVER['SCRIPT_NAME']);
  $freebusy = html_freebusy_header();

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"post\" name=\"f_entity\"
   onsubmit=\"if (calendar_check_meeting(this)) return true; else return false;\"
   action=\"".basename($_SERVER['SCRIPT_NAME'])."\">
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
    $dis_button
  </fieldset>    
  </form>
  $freebusy

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
//   - $current_view : current calendar view
//   - $curdate : current date
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_free_interval($current_view, $calendar) {
  global $obm;
  if ($calendar['date_begin']) {
    $date = clone $calendar['date_begin'];
  } else {
    $date = clone $current_view->get_date();
  }

  $date_ts = $date->setHour($GLOBALS['ccalendar_first_hour'])->setMinute(0)->setSecond(0)->getTimestamp();

  $date->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);  

  $time_unit_sec = 3600 / $_SESSION['set_cal_interval']; 
  $unit = 3600/$time_unit_sec;
  $colWidth = 40/$unit - 2; // -2 => due to border !

  $of = &OccurrenceFactory::getInstance();
  $entities = array();
  $entities['user'] = is_array($calendar['sel_user_id']) ? $calendar['sel_user_id'] : array();
  $entities['group'] = $params['sel_group_id'];
  $entities['user'] = run_query_calendar_merge_groups($calendar['group_id'],$entities['user']);
  $entities['resource'] = is_array($calendar['sel_resource_id']) ? $calendar['sel_resource_id'] : array();
  $entities['contact'] = is_array($calendar['contact_id']) ? $calendar['contact_id'] : array(); //add for occurences on freebusy
  if ((!is_array($entities['user']) || count($entities['user'])==0) && (!is_array($entities['resource']) || count($entities['resource']) == 0) ) $entities['user'] = array($GLOBALS['obm']['uid']);

  $ret = get_calendar_entity_label($entities);
  $ret['resourcegroup'] = run_query_resource_resourcegroup($calendar['sel_resource_group_id']);
  $entity_store = store_calendar_entities($ret);

  if ( $entity_store["contact"] && is_array($entity_store["contact"]) ) {
    $obmSyncIP = null;
    $obmSyncServer = of_domain_get_domain_syncserver($obm['domain_id']);
    if(count($obmSyncServer)){
      $iterator = new ArrayIterator($obmSyncServer);
      $obmSyncServer = $iterator->current();
      $obmSyncIP = $obmSyncServer[0]["ip"];
    }
    if ( $obmSyncIP ) {
      foreach ( $entity_store["contact"] as $key => $contact ) {
	$entity_store["contact"][$key]["obmsyncServer"] = $obmSyncIP;
      }
    }
  }

  calendar_events_model($date,$end, $entity_store, array('ACCEPTED', 'NEEDS-ACTION'), '', false, null, 'OPAQUE');
  if (!is_array($entity_store['resource'])) $entity_store['resource'] = array();
  if (!is_array($entity_store['resourcegroup'])) $entity_store['resourcegroup'] = array();

  $entities_resources = array_merge($entity_store['resource'], $entity_store['resourcegroup']);
  $contacts = array();
  if (!empty($entity_store['contact'])){
	  foreach(array_keys($entity_store['contact']) as $id) {
		$contacts[$id] = (array)$of->getPeriodOccurrences($date, $end, array('contact' => array($id)));	
      }
  }
 /* if (is_array($calendar['contact_id'])) {
    foreach($calendar['contact_id'] as $contact_id) {
      $contact = OBM_Contact::get($contact_id);
      $contacts[$contact_id] = $contact->getEventsInInterval($date, $end);
    }
  }*/

  $users = array();
  if (!empty($entity_store['user'])) {
    $userReadableList = OBM_Acl::getAllowedEntities($GLOBALS['obm']['uid'], 'calendar', 'read',array_keys($entity_store['user']));
    $accessList = OBM_Acl::getAllowedEntities($GLOBALS['obm']['uid'], 'calendar', 'access',array_keys($entity_store['user']));
    foreach(array_keys($entity_store['user']) as $id) {
      if(isset($accessList[$id])) {
        $users[$id] = (array)$of->getPeriodOccurrences($date, $end, array('user' => array($id)));
      }
      else
      {
		$users[$id] = false;
	  } 
    }
  }
  

  $resources = array();
  if(is_array($entities_resources)) {
    foreach($entities_resources as $index => $data) {
      $resources[$data['id']] = $data;
    }
  }
  $entities_resources = $resources;
  $resources = array();
  if (!empty($entities_resources)) {
    $accessList = OBM_Acl::getAllowedEntities($GLOBALS['obm']['uid'], 'resource', 'access', array_keys($entities_resources));
    $resourceReadableList = OBM_Acl::getAllowedEntities($GLOBALS['obm']['uid'], 'resource', 'read',array_keys($entities_resources));
    foreach(array_keys($entities_resources) as $id) {
      if(isset($accessList[$id])) {
        $resources[$id] = (array)$of->getPeriodOccurrences($date, $end, array('resource' => array($id)));
      }
    }
  }
  // Days headers 
  $current = clone $date;
  $colspan = ($GLOBALS['ccalendar_last_hour'] - $GLOBALS['ccalendar_first_hour']) * $unit;
  $time_slots = array();
  $busy_time_slots = array();
  while($current->compare($end) < 0) {
    $current->setHour($GLOBALS['ccalendar_first_hour']);
    $day = $current->localize(Of_Date::OBM_DATE_DAY);
    $week_day_list .= "<td colspan='$colspan' class='freeBusyCalendarDay'>$day</td>\n";
    $end_day = clone $current;
    $end_day->setHour($GLOBALS['ccalendar_last_hour']);

    // Hour
    $d = clone $current;
    $current->addDay(1);
    while($d->compare($end_day) < 0) {
      $is_busy = false;
      if($d->compareMinute(0) == 0) {
        $label = $d->get(Of_Date::TIME_SHORT);
        $day_hours_list .= "<td colspan='$unit' class='freeBusyCalendarHourDisplayed freeBusyCalendarHour'><span>$label</span></td>";
      }
      $slots = sizeof($time_slots);
      array_push($time_slots, $d->get(Of_Date::TIMESTAMP));
      $e = clone $d;
      $e->addSecond($time_unit_sec);
      
      // Users
      foreach($users as $id => &$ocs) {
        $busy = false;
        $tips = array();
        if (is_array($ocs)) {
          foreach($ocs as $index => $oc) {
            if($oc->date->compare($e) >= 0) break;
            if($oc->end->compare($d) <= 0) unset($ocs[$index]);
            elseif ($oc->date->compare($e) < 0 && $oc->end->compare($d) > 0) {
              array_push($tips, $oc);
              $busy = true;
            }
          } 
          if ($busy) {
            $is_busy = true;
            array_push($busy_time_slots, $slots);
            if (isset($userReadableList[$id])) {
              $tip = html_freebusy_tip($tips);
            } else {
              $tip = $GLOBALS['l_occupied'];
            }
            $td_id = "user_".$id."_".$d->getTimestamp();
            $u[$id] .= "<td class='timeSlot' id='$td_id'><div class='haveEvent'></div></td>";
            $js_tips .= "obm.tip.add($('$td_id'), \"".phpStringToJsString($tip)."\");";
          } else {
            $u[$id] .= "<td class='timeSlot'></td>";
          }
        } else {
          $u[$id] .= "<td class='timeSlotNoCalendar' title=\"$GLOBALS[l_calendar_not_available]\"></td>";
        }
      }

      // Resources
      foreach($resources as $id => &$ocs) {
        $busy = false;
        $tips = array();
        if (is_array($ocs)) {
          foreach($ocs as $oc) {
            if($oc->date->compare($e) >= 0) break;
            if($oc->end->compare($d) <= 0) unset($ocs[$index]);
            if ($oc->date->compare($e) < 0 && $oc->end->compare($d) > 0) {
              array_push($tips, $oc);
              $busy = true;
            }
          } 
          if ($busy) {
            $is_busy = true;
            array_push($busy_time_slots, $slots);
            if ($resourceReadableList[$id]) {
              $tip = html_freebusy_tip($tips);
            } else {
              $tip = $GLOBALS['l_occupied'];
            }
            $td_id = "res_".$id."_".$d->getTimestamp();
            $r[$id] .= "<td class='timeSlot' id='$td_id'><div class='haveEvent'></div></td>";
            $js_tips .= "obm.tip.add($('$td_id'), \"".phpStringToJsString($tip)."\");";
          } else {
            $r[$id] .= "<td class='timeSlot'></td>";
          }
        } else {
          $r[$id] .= "<td class='timeSlotNoCalendar' title=\"$GLOBALS[l_calendar_not_available]\"></td>";
        }

      }

      // Contacts
   /*   foreach($contacts as $contact => $evts) {
        if (is_array($evts)) {
          $busy = false;
          foreach($evts as $evt) {
            if ($evt->start->compare($e) < 0 && $evt->end->compare($d) > 0 && $evt->getTransparency() == 'OPAQUE') {
              $busy = true;
            }
          }
          if ($busy) {
            $is_busy = true;
            array_push($busy_time_slots, $slots);
            $td_id = "contact_".$contact."_".$d->getTimestamp();
            $c[$contact] .= "<td class='timeSlot' id='$td_id' title=\"$GLOBALS[l_occupied]\"><div class='haveEvent'></div></td>";
          } else {
            $c[$contact] .= "<td class='timeSlot'></td>";
          }
        }  else {
          $c[$contact] .= "<td class='timeSlotNoCalendar' title=\"$GLOBALS[l_calendar_not_available]\"></td>";
        }
      }*/
		foreach($contacts as $id => &$ocs) {		
			$busy = false;
			$tips = array();
			if (is_array($ocs)) {
			  foreach($ocs as $index => $oc) {
				  if($oc->date->compare($e) >= 0) break;
				  if($oc->end->compare($d) <= 0) unset($ocs[$index]);
				  elseif ($oc->date->compare($e) < 0 && $oc->end->compare($d) > 0 && $oc->event->isOpaque()) {
				    array_push($tips, $oc);
				    $busy = true;
				  }
			  } 
			  if ($busy) {
				$is_busy = true;
				array_push($busy_time_slots, $slots);
				$tip = html_freebusy_tip($tips);
        if (!$tip) $tip = $GLOBALS['l_occupied'];				
				$td_id = "contact_".$id."_".$d->getTimestamp();
				$c[$id] .= "<td class='timeSlot' id='$td_id'><div class='haveEvent'></div></td>";
				$js_tips .= "obm.tip.add($('$td_id'), \"".phpStringToJsString($tip)."\");";
			  } else {
				$c[$id] .= "<td class='timeSlot'></td>";
			  }
			} else {
			  $c[$id] .= "<td class='timeSlotNoCalendar' title=\"$GLOBALS[l_calendar_not_available]\"></td>";
			}
		  }
      
      
      

      // "all attendees" bar
      if ($is_busy) {
        $attendeesBar .= "<td class='timeSlot'><div id='".$d->getTimestamp()."' class='haveEventAll' style='width:${colWidth}px;'></div></td>";
      } else {
        $attendeesBar .= "<td class='timeSlot'><div id='".$d->getTimestamp()."' style='width:${colWidth}px;'></div></td>";
      }
      $timeSlotNoCalendar .= "<td class='timeSlotNoCalendar'></td>";
      $d->addSecond($time_unit_sec);
    }
  }


  // form
  $form_entities = "";
  if (is_array($calendar['sel_user_id'])) {
    foreach($calendar['sel_user_id'] as $id) {
      $form_entities .=  "<input type='hidden' name='new_user_id[]' value='$id' />";
    }
  }

  if (is_array($calendar['group_id'])) {
    foreach($calendar['group_id'] as $id) {
      $form_entities .=  "<input type='hidden' name='new_group_id[]' value='$id' />";
    }
  }

  // Attendees panel
  if (is_array($entity_store['user'])) {
    foreach($entity_store['user'] as $user) {
      $id = $user[id];
      $name = $user[name];
      $attendees .= "<div class='elementRow'>
        <img src='$GLOBALS[ico_user]'/>
        <img src='$GLOBALS[ico_delete]' class='readable' onclick='obm.calendarFreeBusy.removeAttendee(\"data-user-$id\")'/>
        $name
        </div>";
    }
  }
  if (is_array($u)) {
    foreach($u as $e) {
      $b .= "<tr>$e</tr>";
    }
  }

  if (is_array($contacts)) {
    foreach($contacts as $contact => $evts) {
      $cinfo = get_entity_info($contact, 'contact');
      $form_entities .=  "<input type='hidden' name='new_contact_id[]' value='$contact' />";
      $attendees .= "<div class='elementRow'>
        <img src='$GLOBALS[ico_contact]'/>
        <img src='$GLOBALS[ico_delete]' class='readable' onclick='obm.calendarFreeBusy.removeAttendee(\"data-contact-$contact\")'/>
        $cinfo[label]
        </div>";
    }
  }
  if (is_array($c)) {
    foreach($c as $e) {
      $b .= "<tr>$e</tr>";
    }
  }

  if (is_array($calendar['others_attendees'])) {
    foreach($calendar['others_attendees'] as $contact) {
      $form_entities .= "<input type='hidden' name='others_attendees[]' value='$contact' />";
      $attendees .= "<div class='elementRow'>
        <img src='$GLOBALS[ico_contact]'/>
        <img src='$GLOBALS[ico_delete]' class='readable' onclick='obm.calendarFreeBusy.removeAttendee(\"data-others_attendees-$contact\")'/>
        $contact
        </div>";
       $b .= "<tr>$timeSlotNoCalendar</tr>"; 
    }
  }

  foreach($entities_resources as $resource) {
    $id = $resource[id];
    $name = $resource[name];
    $form_entities .=  "<input type='hidden' name='new_resource_id[]' value='$id' />";
    $attendees .= "<div class='elementRow'>
      <img src='$GLOBALS[ico_resource]'/>
      <img src='$GLOBALS[ico_delete]' class='readable' onclick='obm.calendarFreeBusy.removeAttendee(\"data-resource-$id\")'/>
      $name
      </div>";
  }
  if (is_array($r)) {
    foreach($r as $e) {
      $b .= "<tr>$e</tr>";
    }
  }

  $url = "$GLOBALS[path]/people/people_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_attendees_id&amp;filter_entity=calendar&amp;filter_pattern=access";
  $user_add_button = "
    <a href='javascript: return false;'
      onclick='popup(\$(\"freeBusySearchSource\").value+\"$url\",\"\"); return false;'>
      <img src='$GLOBALS[ico_add]' alt='[Add]' />
    </a>";

  $url_resource = "$GLOBALS[path]/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_attendees_id";
  $resource_add_button = "
    <a href='javascript: return false;' onclick='popup(\"$url_resource\",\"\"); return false;'>
     <img src='$GLOBALS[ico_add]' alt='[Add]' />
    </a>";

  $js_time_slots = phpArrayToJsArray($time_slots);
  $js_busy_time_slots = phpArrayToJsArray($busy_time_slots);
  $js_entities_user = phpArrayToJsArray($calendar['sel_user_id']);
  $js_entities_resource = phpArrayToJsArray($calendar['sel_resource_id']);
  $js_entities_group = phpArrayToJsArray($calendar['sel_group_id']);
  $js_entities_resourcegroup = phpArrayToJsArray($calendar['sel_resource_group_id']);
  $js_entities_contact = phpArrayToJsArray($calendar['contact_id']);
  $js_entities_others_attendees = phpArrayToJsArray($calendar['others_attendees']);

  $label = $date->localize(Of_Date::OBM_DATE_DAY)." - ".$date->addDay(6)->localize(Of_Date::OBM_DATE_DAY);
  if (!OBM_Acl::hasAllowedEntities($GLOBALS['obm']['uid'], 'calendar', 'access')) {
    $visiblityU = "style='visibility:hidden;'";
  }
  if (!OBM_Acl::hasAllowedEntities($GLOBALS['obm']['uid'], 'resource', 'access')) {
    $visiblityR = "style='visibility:hidden;'";
  }

  //addressbooks
  $addressbooks = OBM_Addressbook::search();
  foreach($addressbooks as $id => $addressbook) {
    $addressbook_options .= "
      <option value='$GLOBALS[path]/contact/contact_index.php?from=calendar&amp;addressbook_id=$addressbook->id'>$addressbook->displayname</option>";
  }

  $block = "
  <div style='position:relative;'>
    <h2>
      <a href='#' class='readable' onclick='obm.calendarFreeBusy.showPrev();'>
        <img src='$GLOBALS[ico_previous]' alt='[$GLOBALS[l_previous]'] /></a>
      <a href='#' class='readable' onclick='obm.calendarFreeBusy.showNext();'>
        <img src='$GLOBALS[ico_next]' alt='[$GLOBALS[l_next]'] /></a>
      <span class='B'>$label</span>
    </h2>
  </div>

  <div class='freeBusyEntities'>
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
        <td id='sel_attendees_id'>$attendees</td>
      </tr>
    </table>
    <table>
      <tbody class='readable' $visiblityU>
        <tr>
        <td>&nbsp;</td>
        </tr>  
        <tr>
          <td>$GLOBALS[l_users]/$GLOBALS[l_contacts]</td>
        </tr>
        <tr>
          <td class='autocompleteField'>
          <select id='freeBusySearchSource' class='autocompleteSource' name='' style='display:inline-block;'>
            <option value='$GLOBALS[path]/user/user_index.php?from=calendar'>$GLOBALS[l_users]</option>
            <optgroup label=\"$GLOBALS[l_addressbook]\">
              $addressbook_options
            </optgroup>
          </select>
          <input type='text' name='' value='' id='freeBusyUserSearch' autocomplete='off' />
          </td>
        </tr>
        <tr>
          <td>$GLOBALS[l_others_attendees]</td>
        </tr>
        <tr>
          <td>
            <input style='width:14em;display:inline;' type='text' name='external_contact' value='' id='external_contact'/>
            <input type='submit' value='$GLOBALS[l_add]' onclick='obm.calendarFreeBusy.addAttendee(\"data-others_attendees-\"+$(\"external_contact\").value);'/>
          </td>
        </tr>            
      </tbody>
      <tbody class='readable' $visiblityR>
        <tr>
          <td>$GLOBALS[l_resources] $resource_add_button</td>
        </tr>
        <tr>
          <td><input type='text' name='' value='' id='freeBusyResourceSearch' autocomplete='off' /></td>
        </tr>
      </tbody>
    </table>
  </div>
  <div ></div>
  <div id='freeBusyForm' class='freeBusyForm'>
    <div class='freeBusyCalendar'>
    <div class='readable' id='scrollLeft'>«</div>
    <div ></div>
      <div id='calendarFreeBusyScroll'>
        <table id='calendarFreeBusyTable'> 
          <colgroup span='$colspan' class='freeBusyCalendarColDay'></colgroup>
          <colgroup span='$colspan' class='freeBusyCalendarColDay'></colgroup>
          <colgroup span='$colspan' class='freeBusyCalendarColDay'></colgroup>
          <colgroup span='$colspan' class='freeBusyCalendarColDay'></colgroup>
          <colgroup span='$colspan' class='freeBusyCalendarColDay'></colgroup>
          <colgroup span='$colspan' class='freeBusyCalendarColDay'></colgroup>
          <colgroup span='$colspan' class='freeBusyCalendarColDay'></colgroup>
          <thead id='calendarHead'>
            <tr>$week_day_list</tr>
            <tr>$day_hours_list</tr>
          </thead>
          <tbody id='calendarFreeBusyGrid'>
            <tr>$attendeesBar</tr>
            $b
          </tbody>
        </table>
        <div id='calendarFreeBusyMeeting' class='meeting'>
          <div id='calendarFreeBusyResizeMeeting' class='meetingResize'></div>
        </div>
      </div>
    <div class='readable' id='scrollRight'>»</div>
    </div>
      <div class='freeBusyButtons readable' style='text-align:center;'>
        <input type='button' id='pickPrev' value='$GLOBALS[l_freebusy_autopick_prev]' onclick='obm.calendarFreeBusy.autoPickPrev();' />
        <input type='button' id='pickNext' value='$GLOBALS[l_freebusy_autopick_next]' onclick='obm.calendarFreeBusy.autoPickNext();' />
      </div>

      <div id='meetingInfo'>
        <table class='details'>
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
        <form id='freeBusyFormId' method='POST' action='$GLOBALS[path]/calendar/".basename($_SERVER['SCRIPT_NAME'])."'>
          <input class='close readable' id='submitMeeting' type='submit' value='$GLOBALS[l_validate]' />
          <input class='close' id='formClose' type='button' value='$GLOBALS[l_cancel]' onclick='obm.popup.hide(\"fbcContainer\");'/>
          <input type='hidden' name='date_begin'id='date_begin' value='".urldecode($date->setHour($GLOBALS['ccalendar_first_hour'])->getURL())."' />
          <input type='hidden' name='duration' id='duration' value='' />
          <input type='hidden' name='time_begin' id='time_begin' value='' />
          <input type='hidden' name='min_begin' id='min_begin' value='' />
          <input type='hidden' name='new_meeting' value='1'>
          <input type='hidden' id='fb_title' name='tf_title' value=''>
          <input type='hidden' id='fb_owner' name='owner' value=''>
          <input type='hidden' name='template_id' value='$_POST[template_id]'>
          $form_entities
          <input type='hidden' id='freebusy_action' name='action' value='new'>
        </form>
      </div>
  </div>
  <div id='dummy' style='display:none;'/>
  <script type='text/javascript'>
    obm.vars.consts.begin_timestamp = $date_ts;

    if ($('sel_time_begin') && $('sel_min_begin')) {
      // New event form
      // get date begin, date end, duration
      var d = new Obm.DateTime(obm.vars.consts.begin_timestamp*1000);
      d.setHours(parseInt($('sel_time_begin').value, 10));
      d.setMinutes(parseInt($('sel_min_begin').value, 10));
      obm.vars.consts.begin_timestamp = d.getTime()/1000;
    }

    obm.calendarFreeBusy = new Obm.CalendarFreeBusy($js_time_slots, $unit, '$GLOBALS[ccalendar_first_hour]');
    obm.calendarFreeBusy.busySlots = $js_busy_time_slots;
    obm.calendarFreeBusy.entities = new Array(); 
    obm.calendarFreeBusy.entities.sel_user_id = $js_entities_user;
    obm.calendarFreeBusy.entities.sel_resource_id = $js_entities_resource;
    obm.calendarFreeBusy.entities.sel_resource_group_id = $js_entities_resourcegroup;
    obm.calendarFreeBusy.entities.sel_group_id = $js_entities_group;
    obm.calendarFreeBusy.entities.sel_contact_id = $js_entities_contact;
    obm.calendarFreeBusy.entities.others_attendees = $js_entities_others_attendees;
    $js_tips
    new obm.AutoComplete.ExtSearch(
      function() { return \$('freeBusySearchSource').value+'&action=ext_search&ajax=1'; },
      'dummy', 'freeBusyUserSearch',
      {filter_entity:'calendar', filter_pattern:'access', selectfunction:'obm.calendarFreeBusy.addAttendee(id);'});
    new obm.AutoComplete.Search('$GLOBALS[path]/resource/resource_index.php?action=ext_search', 'dummy', 'freeBusyResourceSearch',
      {filter_entity:'Resource', filter_pattern:'access', selectfunction:'obm.calendarFreeBusy.addAttendee(id);'});
  </script>";
  
  return $block;
}


/*
 * Build freebusy popup header
 */
function html_freebusy_header() {

  $block = "
  <div id='fbcContainer' class='obmPopup'>
    <div style='position:relative;'>
      <h1>
      <a href='#' id='closeFreeBusy' class='close closeSticky' onclick='return false;'>
        <img src='$GLOBALS[ico_popup_close]' alt='close' /></a>
      <span>$GLOBALS[l_header_meeting]</span>
      </h1>
    </div>
    <div id='fbc' class='freeBusyContent'></div>
  </div>";
  return $block;

}


/*
 * Build freebusy tip 
 */
function html_freebusy_tip($ocs) {
  $tip = '<table>';
  foreach($ocs as $oc) {
    if ($oc->event->isMeeting()) $title .= "<img src='$GLOBALS[ico_meeting_alt]'/>";
    if ($oc->event->isPrivate()) $title .= "<img src='$GLOBALS[ico_private_alt]'/>";
    if ($oc->event->isPeriodic()) $title .= "<img src='$GLOBALS[ico_periodic_alt]'/>";
    if ($oc->event->isAllDay()) {
      $title .= "<img src='$GLOBALS[ico_allday_alt]'/>";
      $time = $GLOBALS['l_all_day'];
    } else {
      $end = clone $oc->date;
      $end->addTimestamp($oc->event->duration);
      $time = $oc->date->format(Of_Date::TIME_SHORT)." - ".$end->format(Of_Date::TIME_SHORT);
    }
    if ($oc->event->isPrivate() && (is_null($oc->event->attendee['user'][$GLOBALS[obm][uid]]) && $oc->event->owner != $GLOBALS[obm][uid])) {
      $title .= htmlspecialchars($GLOBALS[l_private]);
    } else {
      $title .= htmlspecialchars($oc->event->title);
    }
    $tip .= "<tr><td class='B'>$time</td></tr>";
    $tip .= "<tr><td>&nbsp;&nbsp;$title</td></tr>";
  }
  $tip .= '</table>';
  return $tip;
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
  global $l_html, $l_ics;
  global $l_private, $l_public, $l_share_mail_search;

  if ($id == '') {
    $id = $obm['uid'];
  }
  $infos = get_user_info($id);
  $name = $infos['firstname'] . ' ' . $infos['lastname'];
  $display['title'] = $name;

  $hide_user = null;
  if ($id == $obm['uid']) {
    $block = "<h1>$l_rights_on_my</h1>";
    $hide_user = true;
  } else {
    $block = "<h1>$l_user_right $name</h1>";
    $hide_user = false;
  }
  $block .= of_right_dis_admin('calendar', $id, $hide_user);
  $block .= html_share_calendar($id,$name);
  if ($GLOBALS['c_calendar_display_my_rights']) $block .= html_calendar_my_rights();

  return $block;
}


/*
 * dis_calendar_my_rights
 * @return html
 */ 
function html_calendar_my_rights() {

  $limit = 50;

  $accessEntities = OBM_Acl::getAllowedEntities($GLOBALS[obm][uid], 'calendar', 'access'); 
  sort($accessEntities);
  $js_accessEntities = phpArrayToJsArray($accessEntities);
  $dis_access = dis_calendar_my_rights_detail($accessEntities, $limit, 'access');

  $readEntities = OBM_Acl::getAllowedEntities($GLOBALS[obm][uid], 'calendar', 'read'); 
  sort($readEntities);
  $js_readEntities = phpArrayToJsArray($readEntities);
  $dis_read = dis_calendar_my_rights_detail($readEntities, $limit, 'read');

  $writeEntities = OBM_Acl::getAllowedEntities($GLOBALS[obm][uid], 'calendar', 'write');
  sort($writeEntities);
  $js_writeEntities = phpArrayToJsArray($writeEntities);
  $dis_write = dis_calendar_my_rights_detail($writeEntities, $limit, 'write');

  $adminEntities = OBM_Acl::getAllowedEntities($GLOBALS[obm][uid], 'calendar', 'admin'); 
  sort($adminEntities);
  $js_adminEntities = phpArrayToJsArray($adminEntities);
  $dis_admin = dis_calendar_my_rights_detail($adminEntities, $limit, 'admin');
  
  $block = "
  <h1>$GLOBALS[l_rights_my_on]</h1>
  <table class='spreadSheet rights'>
    <thead>
      <tr>
        <th>$GLOBALS[l_access_permission]</th>
        <th>$GLOBALS[l_read_permission]</th>
        <th>$GLOBALS[l_write_permission]</th>
        <th>$GLOBALS[l_admin_permission]</th>
      </tr>
    </thead>
    <tbody style='vertical-align:top;'>
      <tr>
        <td>
          $dis_access
        </td>
        <td>
          $dis_read
        </td>
        <td>
          $dis_write
        </td>
        <td>
          $dis_admin
        </td>
      </tr>
    </tbody>
  </table>
  <script type=\"text/javascript\">
    obm.vars.consts.accessEntities = $js_accessEntities;
    obm.vars.consts.readEntities = $js_readEntities;
    obm.vars.consts.writeEntities = $js_writeEntities;
    obm.vars.consts.adminEntities = $js_adminEntities;
    obm.vars.consts.accessOffset = $limit;
    obm.vars.consts.readOffset = $limit;
    obm.vars.consts.writeOffset = $limit;
    obm.vars.consts.adminOffset = $limit;
    obm.vars.consts.rightLimit = $limit;
  </script>";

  return $block;
}


/*
 * dis_calendar_my_rights_detail
 * @return html
 */ 
function dis_calendar_my_rights_detail($entities, $limit, $right) {
  if (sizeof($entities) > $limit) {
    for($i=0;$i<$limit;$i++) {
      $list .= "<li>$entities[$i]</li>";
    }
    $nav = "
      <a href='#' id='{$right}Prev' style='display:none;' onclick='showPrevRights(\"$right\", this);'>« $GLOBALS[l_previous]</a>
      <a href='#' id='{$right}Next' style='float:right;margin-right:2px;' onclick='showNextRights(\"$right\", this);'>$GLOBALS[l_next] »</a>";
  } else {
    foreach($entities as $entity) {
      $list .= "<li>$entity</li>";
    }
  }
  $container = $right."Right";
  $ret = "
  <div style='overflow-y:scroll;height:300px;'>
    <ul id='$container'>$list</ul>
    $nav
  </div>";

  return $ret;
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
  </ul>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the calendar administration index
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_admin_index($tags_q) {
  global $ico_delete, $ico_ok ;
  global $l_label, $l_c_new, $l_c_checkdelete, $l_c_update, $l_c_insert ;
  global $l_tag_management ;

  // category1
  $cats1 = of_category_get_ordered('event', 'category1');
  $block = of_category_dis_admin_form('category1', $cats1);

  // tags , poubelle et input
  if ($tags_q->num_rows() > 0) {
  $block .= "<div class=\"detail admin\" >
    <form id=\"form_tags_update\" action=\"?action=tags_update\" method=\"post\">
    <input type=\"hidden\" name=\"tf_tag_id\" value=\"\" />
  <input type=\"hidden\" name=\"tf_tag_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_tag_color\" value=\"\" />
    </form>    
    <h1>$l_tag_management</h1>
    <div class=\"overflow\">
    <table>
      <thead>
        <tr>
        <td>&nbsp;</td>
        <td>$l_label</td>
        <td>&nbsp;</td>        
      </tr>    
      </thead>
      <tbody>" ;
  $flip = 1 ;
    while ($tags_q->next_record()) {
    $class = "" ;
    if ($flip == 1) {
      $class = "class=\"pair\"" ;
    }
    $flip *= -1 ;
    $tagid = $tags_q->f('eventtag_id') ;
    $taglabel = $tags_q->f('eventtag_label') ;
    $tagcolor = $tags_q->f('eventtag_color') ;
    $block .= "<tr $class>
          <td>
          <input type=\"hidden\" id=\"tf_tag_id_$tagid\" value=\"$tagid\" />
          <a href=\"?action=tag_delete&amp;tag_id=$tagid\">
          <img src=\"$ico_delete\" alt=\"$l_c_checkdelete\" title=\"$l_c_checkdelete\" />
          </a></td>
          <td> 
      
      <div class=\"extColorPickerWrapper\">
      <input type=\"text\" id=\"tf_tag_label_$tagid\" value=\"$taglabel\" />
      
      <div id=\"cp_$tagid\" class=\"extColorPicker\">
        <div id=\"cp_back_$tagid\">&nbsp;</div>
      <input type=\"hidden\" id=\"tf_tag_color_$tagid\" value=\"$tagcolor\">
      <img src=\"/images/themes/default/images/ext_pick.gif\" alt=\"\" title=\"\" />
      </div>
      
      </div>
      
      </td>
          <td>
          <a href=\"#\" onclick=\"updateLine($tagid,'tags')\">
          <img src=\"$ico_ok\" alt=\"$l_c_update\" title=\"$l_c_update\" />
          </a>
        </td>
        </tr>" ;
  } ;
  // tag, ajout
  $block .= "</tbody>
    </table>
    </div>
    <form id=\"form_tag_new\" action=\"\" method=\"post\">
    <table>
    <thead>
      <tr>
        <td colspan=\"2\">$l_c_new</td>
      </tr>
    <tbody>   
      <tr>
        <td>$l_label</td>    
      </tr>       
      <tr>
        <td>
    
      <div class=\"extColorPickerWrapper\">
      <input type=\"text\" name=\"tf_tag_label\" id=\"tf_tag_label\" value=\"\" />
      
      <div id=\"cp\" class=\"extColorPicker\">
        <div id=\"cp_back\">&nbsp;</div>
      <input type=\"hidden\" name=\"tf_tag_color\" id=\"tf_tag_color\" value=\"#eff0f2\">
      <img src=\"/images/themes/default/images/ext_pick.gif\" alt=\"\" title=\"\" />
      </div>
      
      </div>
      
    </td>    
      </tr>  
    </tbody>
    <tfoot>
      <tr>
        <td colspan=\"2\"><input type=\"submit\" value=\"$l_c_insert\" /></td>
      </tr>
    </tfoot>
    </table>
    <input type=\"hidden\" name=\"action\" value=\"tag_insert\" />
    <input type=\"hidden\" name=\"category\" value=\"tag_name\" />
    </form>
    </div>" ;
  }

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the event template form
// Parameters:
//   - $action   : action called
//   - $params   : parameters
//   - $template_q  : DBO : event result
//   - $entity_ids : linked entity ids
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_template_form($action, $params, $template_q, $entity_ids, $current_view) {
  global $obm;
  if (empty($entity_ids)) {
    $entity_ids = array('user' => array(), 'contact' => array(), 'resource' => array(), 'group' => array(), 'document' => array());
  }

  if($template_q->f('event_organizer')){
    $params['organizer'] = $template_q->f('event_organizer');
  }
  $users_id = is_array($params['sel_user_id']) ? $params['sel_user_id'] : array();
  $contacts_id = is_array($params['sel_contact_id']) ? $params['sel_contact_id'] : array();
  $res_id = is_array($params['sel_resource_id']) ? $params['sel_resource_id'] : array();
  $groups_id = is_array($params['sel_group_id']) ? $params['sel_group_id'] : array();
  $documents_id = is_array($params['sel_document_id']) ? $params['sel_document_id'] : array();

  $users_id = array_merge($users_id, $entity_ids['user']);
  $contacts_id = array_merge($contacts_id, $entity_ids['contact']);
  $res_id = array_merge($res_id, $entity_ids['resource']);
  $groups_id = array_merge($groups_id, $entity_ids['group']);
  $documents_id = array_merge($documents_id, $entity_ids['document']);
  
  $users = (count($users_id) > 0) ? get_userobm_from_ids($users_id) : array();
  $contacts = (count($contacts_id) > 0) ? get_contact_from_ids($contacts_id, false) : array();
  $resources = (count($res_id) > 0) ? get_calendar_resource_from_ids($res_id) : array();
  $groups = (count($groups_id) > 0) ? get_calendar_group_from_ids($groups_id) : array();
  $documents = (count($documents_id) > 0) ? get_calendar_document_from_ids($documents_id) : array();
  
  $possible_owners = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
  );
  $possible_owners2 = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($params['organizer'], 'calendar', 'write')
  );
  foreach($possible_owners2['ids'] as $possibleOwnerId){
    if(!in_array($possibleOwnerId, $possible_owners['ids'])){
      $possible_owners['entity'][$possibleOwnerId] = $possible_owners2['entity'][$possibleOwnerId];
    }
  }
  $possible_owners = $possible_owners["entity"];
  $cats1 = of_category_get_ordered('event', 'category1');
  
  $block = html_calendar_event_form($action, $params, $template_q, $exception_q, $possible_owners, $users, $contacts, $resources, $groups, $cats1, $alerts, $tags_q, $documents, $current_view);

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display event templates list
// Parameters:
//   - $templates_q : DBO
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_templates_list($templates_q) {
  global $ico_popup_close, $ico_edit, $ico_backup, $l_name, $l_duplicate_template, $l_template_name,
    $l_validate, $l_cancel, $l_copy_of, $l_no_template, $l_export_templates,
    $l_import_template, $path, $ico_add, $ico_delete;
  
  $block = "
    <div class=\"detail\">
      <form style=\"display: inline\" method=\"post\" enctype=\"multipart/form-data\" >
        <label for=\"template_file\">$l_import_template :</label>
        <input type=\"file\" name=\"template_file\" size=\"12\" />
        <input type='hidden' name='action' value='import_template' />
        <input type=\"submit\" value=\"$GLOBALS[l_ok]\" />
      </form>
    </div>";
  
  if ($templates_q->num_rows() === 0) {
    return $block."<div class=\"detail\"><p class=\"emptyset\">$l_no_template</p></div>";
  }
  
  $block.= "
    <div class=\"detail admin\">
    <p><a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=export_all_templates\">$l_export_templates</a></p>
    <table>
      <thead>
        <tr><td>$l_name</td><td>$GLOBALS[l_edit]</td><td>$GLOBALS[l_export]</td><td>$GLOBALS[l_delete]</td><td>$GLOBALS[l_duplicate_template]</td></tr>
      </thead>
      <tbody style=\"text-align: left;\">
      ";
  while ($templates_q->next_record()) {
    $id = $templates_q->f('eventtemplate_id');
    $name = $templates_q->f('eventtemplate_name');
    $block.= "<tr>
        <td><span class=\"editable\" rel=\"$id\">$name</span></td>
        <td>
          <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=edit_template&template_id=$id\">
            <img title='$GLOBALS[l_edit]' src=\"$ico_edit\" />
          </a>
        </td>
        <td>
          <a href=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=export_template&template_id=$id\">
            <img title='$GLOBALS[l_export]' src=\"$ico_backup\" />
          </a>
        </td>
        <td>
          <form id=\"deleteButtonForm$id\" method=\"post\" action=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=delete_template&template_id=$id\">
            <div>
              <input type=\"image\" src=\"$ico_delete\" style='border:none;' onclick=\"return confirm('$GLOBALS[l_delete_template]');\" />
            </div>
          </form>
        </td>
        <td>
          <form id=\"duplicateButtonForm$id\" method=\"post\" action=\"$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=duplicate_template&template_id=$id\">
            <div>
              <input type=\"image\" src=\"$ico_add\" style='border:none;' onclick=\"displayTemplateDuplicate(this.form, '$l_copy_of $name'); return false;\" />
            </div>
          </form>
        </td>
      </tr>";
  }
  $block.= "</tbody>
      </table>
    </div>
    <div id=\"duplicateTemplatePopup\" class=\"obmPopup\">
      <h1>
        <a href=\"\" class=\"closeSticky\" onclick=\"return false;\">
          <img src=\"$ico_popup_close\" alt=\"close\" />
        </a>
        <span>$l_duplicate_template</span>
      </h1>
      <div>
        <form id=\"duplicateTemplatePopupForm\">
          <fieldset style=\"border:none;\">
            $l_template_name : <input type=\"text\" maxlength=\"255\" name=\"name\" />
          </fieldset>
          <fieldset class=\"buttons\">
            <input class=\"button closeSticky\" type=\"submit\" value=\"$l_validate\" />
            <input class=\"button closeSticky\" onclick=\"return false;\" type=\"submit\" value=\"$l_cancel\" />
          </fieldset>
        </form>
      </div>
    </div>
    <script type=\"text/javascript\">
      // WTF ??????
      // obm.initialize.chain(function () {
      //   new obm.InPlaceEditor('$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=set_template_name');
      // });
    </script>";
  
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Export event templates to XML
// Parameters:
//   - $templates_id (optional)
///////////////////////////////////////////////////////////////////////////////
function xml_calendar_export_templates($template_id = null) {
  $columns = array('name','title','owner','date','description','properties','location',
    'category1_id','priority','privacy','duration','repeatkind','repeatfrequence',
    'repeatdays','allday','tag_id','allow_documents','alert','endrepeat','user_ids',
    'contact_ids','resource_ids','group_ids','document_ids');
  
  $template_q = run_query_calendar_get_templates_details($columns, $template_id);
  $only_one = ($template_q->num_rows() == 1);
  
  $xml = new XMLWriter();
  $xml->openMemory();
  $xml->setIndent(true);
  $xml->setIndentString(' ');
  $xml->startDocument('1.0', 'UTF-8');
  if (!$only_one) $xml->startElement('event_templates');
  while ($template_q->next_record()) {
    if ($only_one) {
      $template_name = $template_q->f('eventtemplate_name');
    }
    $xml->startElement('event_template');
    foreach ($columns as $column) {
      if ($column == 'properties') continue;
      $xml->writeElement($column, $template_q->f('eventtemplate_'.$column));
    }
    $xml->startElement('properties');
    $xml->startElement('extended_desc');
    $desc = new SimpleXMLElement($template_q->f('eventtemplate_properties'));
    foreach ($desc->resource as $resource) {
      $xml->startElement('resource');
      $xml->writeElement('res_name', $resource->res_name);
      $xml->writeElement('res_id', $resource->res_id);
      $xml->writeElement('item', $resource->item);
      $xml->writeElement('item_id', $resource->item_id);
      $xml->writeElement('property_name', $resource->property_name);
      $xml->writeElement('property_value', $resource->property_value);
      $xml->endElement();
    }
    $xml->endElement();
    $xml->endElement();
    $xml->endElement();
  }
  if (!$only_one) $xml->endElement();
  
  if (!$only_one) {
    return $xml;
  } else {
    return array($template_name, $xml);
  }
}

///////////////////////////////////////////////////////////////////////////////
// Perform the export to the vCalendar format
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_calendar_export_handle($calendar, $entity = null, $entity_id = null, $private = false) {
  global $obm, $obm_version, $ccalendar_weekstart;

  $exportType = $calendar['export'];
  
  if ($exportType == "date") {
    $date = $calendar['event_before_date'];
    $start = clone $date;
    $start = $start->subYear(100);
    $end = clone $date;
  } else if ($exportType == "postdate") {
    $date = $calendar['event_after_date'];
    $start = clone $date;
    $end = clone $date;
    $end->addYear(100);
  } else {
    $date = new Of_date() ;
    $start = clone $date;
    $start = $start->subYear(100);
    $end = clone $date;
    $end->addYear(100);
  }
  
  include_once('obminclude/of/vcalendar/writer/ICS.php');
  include_once('obminclude/of/vcalendar/reader/OBM.php');
  
  if ($entity != null && $entity_id != null) {
    $calendar_user[$entity] = array ($entity_id => 'dummy');
  } else {
    $calendar_user['user'] = array ($obm['uid'] => 'dummy');
  }
  
  // 'ACCEPTED' enables filtering of events upon user participation state. Fix M#3005
  // 'NEEDS-ACTION' events should popup too. Fix OBMFULL-3022
  $reader = new Vcalendar_Reader_OBM($calendar_user, NULL, $start, $end, array("ACCEPTED", "NEEDS-ACTION")); 
  $document = $reader->getDocument('PUBLISH', $private);
  $writer = new Vcalendar_Writer_ICS();  
  $writer->writeDocument($document);
  $document->destroy();
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
      <a href='$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."'>$l_cancel</a>
      <a href='$path/calendar/".basename($_SERVER['SCRIPT_NAME'])."?action=reset&amp;force=1' onclick=\"return confirm('$l_js_confirm_reset');\">$l_confirm</a>
    </div>
    <p class=\"LC\"></p>" ;

  return $block ;
}

///////////////////////////////////////////////////////////////////////////////
// Export iCalendar file
///////////////////////////////////////////////////////////////////////////////
function dis_icalendar_export($params) {
  global $display;
  global $l_all_calendar, $l_event_before_date, $l_delete_meeting, $l_delete_event;
  global $l_ics_export_ok, $l_insert, $l_calendar , $l_validate, $l_purge_popup_title;
  global $l_export, $l_cancel, $l_confirm, $l_confirm_delete_export, $ico_warning;

  $date =new Of_Date();

  // --- HTML Template --------------------------------------------------------
  $dis_combo_box = "
    <tr>
      <th><input id=\"rd_all_calendar\" type=\"radio\" checked=\"checked\" onClick=\"$(tf_event_before_date).disabled=true\" value=\"all\" name=\"rd_export\" /></th>
      <td>$l_all_calendar</td>
    </tr>
    <tr>
      <th><input id=\"rd_date_post_event\" type=\"radio\" value=\"date\" onClick=\"$(tf_event_before_date).disabled=false\" name=\"rd_export\" /></th>
      <td>$l_event_before_date</td>
    </tr>
    <tr>
      <th></th>
      <td><input disabled type=\"text\" name=\"tf_event_before_date\" id=\"tf_event_before_date\" value=\"".$date->getInputDate()."\" class=\"datePicker\" /></td>
    </tr>
    ";

  $dis_checkbox = "
    <tr>
      <th><input type=\"checkbox\" id=\"cb_delete_event\" onClick=\"$('cb_delete_meeting').checked=false; disable('cb_delete_meeting')\" name=\"cb_delete_event\" /></th>
      <td><label for=\"cb_delete_event\">$l_delete_event</label></td>
    </tr>
    <tr>
      <th><input disabled=\"disabled\" type=\"checkbox\" id=\"cb_delete_meeting\" name=\"cb_delete_meeting\" /></th>
      <td><label for=\"cb_delete_meeting\">$l_delete_meeting</label></td>
    </tr>
    ";

  $dis_popup = "
    <div id=\"exportPopupCalendar\" class=\"obmPopup\">
      <h1>
        <img style=\"position:relative;\" src=\"$ico_warning\" />
        <span>$l_purge_popup_title</span>
      </h1>
      <h2>
        $l_ics_export_ok
        <br/>
        $l_confirm_delete_export
        <br/>
      </h2>
      <fieldset class=\"buttons\">
        <input id=\"bt_cancel\" type=\"submit\" onclick=\"obm.popup.hide('exportPopupCalendar')\" value=\"$l_cancel\"/>
        <input id=\"bt_submit\" type=\"submit\" onclick=\"submitResetExportForm();\" value=\"$l_confirm\"/>
      </fieldset>
    </div>
    ";

  // --- HTML Template --------------------------------------------------------
  $dis_button = "<input id=\"export_action\" type=\"hidden\" name=\"action\" value=\"ics_export\">
          <input id=\"export_popup\" type=\"hidden\" name=\"popup\" value=\"1\">
          <input id=\"export_force\" type=\"hidden\" name=\"force\" value=\"0\">
          <input type=\"submit\" onclick=\"$('cb_delete_event').checked?obm.popup.show('exportPopupCalendar'):0;\" value=\"$l_export\">";

  $block = "
    <form id=\"f_entity\" method=\"post\" name=\"f_entity\" enctype=\"multipart/form-data\"
      action=\"".basename($_SERVER['SCRIPT_NAME'])."\">
    <fieldset class=\"detail\">
      <legend></legend>
      <table>
      <tbody>
        $dis_combo_box
        $dis_date_before
        $dis_checkbox
      </tbody>
      </table>
    </fieldset>
    <fieldset class=\"buttons\">
      $dis_button
    </fieldset>
    </form>
    $dis_popup
    <p class=\"LC\"></p>";

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
      action=\"".basename($_SERVER['SCRIPT_NAME'])."\">
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
    <p class=\"LC\"></p>" ;

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

function json_tag_search($tags_q) {
  $json_tags = "" ;
  if ($tags_q->num_rows() > 0) {
    while($tags_q->next_record()) {
      $json_tags .= "{'id':".$tags_q->f('eventtag_id').",'label':'".$tags_q->f('eventtag_label')."','color':'".$tags_q->f('eventtag_color')."'}," ;
    }
    $json_tags = rtrim($json_tags, ',') ;
  }
  return "[".$json_tags."]" ;
}


/**
 * Build html event 
 */
function json_build_html_event($calendar, $current_view, $id) {
  global $display, $obm;

  $status = array('NEEDS-ACTION', 'ACCEPTED');
  $calendar_entity = $current_view->get_entities();
  $category_filter = $current_view->get_category_filter();
  $unit = "hour";
  if ($current_view->get_cal_range() == "month" || $calendar['all_day'] == 1) $unit = "day";
  $isMultiWeek = false;
  $current_view->set_date($calendar['date_begin']);
  if ($calendar['periodic'] && $current_view->get_cal_range() == "week") {
    $date = $current_view->get_date_range();
    $begin = $date['begin'];
    $end = $date['end'];
  } else {
    $begin = $calendar['date_begin'];
    $end = $calendar['date_end'];
    if ($begin->format('W') != $end->format('W')) {
      $isMultiWeek = true;
    }
  }

  $writable_entity = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
  );

  $tags = array();

  if ($isMultiWeek) {
    $date = $current_view->get_date_range();
    $current = clone $date['begin'];
    calendar_events_model($begin,$end,$calendar_entity, $status, $category_filter);
    $of = &OccurrenceFactory::getInstance();
    while($current->compare($end) < 0) {
      $endWeek = clone $current;
      $endWeek->addDay(7);
      $occurrences = $of->getPeriodOccurrences($current,$endWeek);
      if (is_array($occurrences)) {
        foreach($occurrences as $occurrence) {
          if ($occurrence->event->id == $id) {
            $occurrence->index = $occurrence->date;
            $occurrence->left = false;
            $occurrence->right = false;

            // Left extension
            if ($occurrence->index->compare($current) < 0) {
               $occurrence->index = $current;
               $occurrence->left = true;
            }
            // Right extension
            if ($occurrence->end->compare($endWeek) > 0) {
               $occurrence->right = true;
            }
            $dis_events[] = "'".dis_calendar_event($occurrence, "day", $calendar_entity, $writable_entity)."'";
          }
        }
      }
      $current->addDay(7);
    }
  } else {
    calendar_events_model($begin,$end,$calendar_entity, $status, $category_filter);
    $of = &OccurrenceFactory::getInstance();
    $occurrences = $of->getOccurrences();
    if (is_array($occurrences)) {
      foreach($occurrences as $occurrence) {
        if ($occurrence->event->id == $id) {
          $occurrence->index = $occurrence->date;
          $dis_events[] = "'".dis_calendar_event($occurrence, $unit, $calendar_entity, $writable_entity)."'";
        }
      }
    }
  }

  $display['json'] =  "eventId:$id, events:[".implode(',', $dis_events)."]";

}


// TODO: clean  json_insert_event_data && json_delete_event_data
/**
 * Build a JSON message with all the event data
 * the respone is a table of event objects.
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function json_insert_event_data($id,$calendar,$current_view) {
  global $obm;
  
  $duration = $calendar['duration'];
  $all_day = ($calendar['all_day'])?true:false;
  $date_range = $current_view->get_date_range();
  $date = clone $date_range['begin'];
  $end = clone $date_range['end'];

  $calendar_entity = $current_view->get_entities();
  $writable_entity = OBM_Acl_Utils::expandEntitiesArray(
    OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
  );
  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $category_filter = $current_view->get_category_filter();
  $occurrences = calendar_events_model($date,$end,$calendar_entity, $status, $category_filter);
  $of = &OccurrenceFactory::getInstance();

  if ($all_day) {
    $current = clone $date;
    while($current->compare($end) < 0) {
      $endWeek = clone $current;
      $endWeek->addDay(7);
      $ocs = $of->getPeriodOccurrences($current,$endWeek);
      if (is_array($ocs)) {
        foreach($ocs as $occurrence) {
          if ($occurrence->event->id == $id) {
            $occurrence->index = $occurrence->date;
            $occurrence->left = false;
            $occurrence->right = false;

            // Left extension
            if ($occurrence->index->compare($current) < 0) {
               $occurrence->index = $current;
               $occurrence->left = true;
            }

            // Right extension
            if ($occurrence->end->compare($endWeek) > 0) {
               $occurrence->right = true;
            }

            $dis_event .= dis_calendar_event($occurrence, 'day', $calendar_entity, $writable_entity);
          }
        }
      }
      $current->addDay(7);
    }
  } else {
    if (is_array($occurrences)) {
      foreach($occurrences as $occurrence) {
        if ($occurrence->event->id == $id) {
          $dis_event .= dis_calendar_event($occurrence, 'hour', $calendar_entity, $writable_entity);
        }
      }
    }
  }

  $GLOBALS['display']['json'] .= "id:$id,";
  $GLOBALS['display']['json'] .= "event:['".$dis_event."']";
}


function json_delete_event_data($id,$calendar,$current_view) {

  global $obm, $display,$ccalendar_first_hour,$ccalendar_last_hour;
  global $ccalendar_max_users;
  global $l_user, $l_resource;
  $attendees = array();
  $event_data = array();
  $json = array();
  $events = array();
  $element_id = $calendar['element_id'];
  $ent_q = run_query_get_events_attendee(array($id));
  $entities = get_calendar_event_entity($id);
  $all_day = ($calendar['all_day'])?1:0;
  $opacity = $calendar['opacity'];
  $duration = $calendar['duration'];
  $date = $calendar['date_begin'];
  $end = clone $date;
  $end->addSecond($duration);
  $title = $calendar['title'];
  $all = isset($calendar['all']) ? 1:0;
  $periodic = isset($calendar['periodic'])?$calendar['periodic']:'false';
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
      $attendees[] = array("label"=> $label_entity, "entities" => $ent);
    }
  }
  $event_data["id"] = $id;
  $event_data["opacity"] = $opacity;
  $event_data["duration"] = $duration;
  settype($event_data["duration"],"float");
  $event_data["time"] = $date->getTimestamp();
  settype($event_data["time"],"float");
  $event_data["all_day"] = $all_day;
  $event_data["title"] = htmlspecialchars($title);
  $event_data["attendees"] = $attendees;
  $event_data["updatable"] = true;
  $event_data["description"] = "";
  $event_data["location"] = "";
  $event_data["category"] = "";
  $event_data["colors"] = new stdClass();
  $event_data["all"] = $all;
 
  $calendar_entity = $current_view->get_entities();
  foreach ($calendar_entity as $entity => $data) {
    foreach ($data as $entity_id => $entity_data) {
      if($entities[$entity]['entity'][$entity_id]) {
        $info = $event_data;
        $info["entity"] = $entity;
        $info["entity_id"] = $entity_id;
        $info["klass"] = $entity_data['class'];
        $info["status"] = $entities[$entity]['entity'][$entity_id]['state'];
        $events[] = array("event" => $info);
      }
    }
  }

  $json["day"] = $all_day;
  $json["elementId"] = $element_id;
  $json["isPeriodic"] = $periodic;
  $json["all"] = $all;
  $json["eventsData"] = $events;
  return $json;
}

function parse_item($item) {
  if ($item == '') {
    return false;
  }
  $p = xml_parser_create();
  xml_parser_set_option($p, XML_OPTION_TARGET_ENCODING, 'UTF-8');
  $return = xml_parse_into_struct($p, $item, $vals, $index);
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
function dis_calendar_pdf_options($current_view) { 
  global $l_print, $l_pagelayout, $l_pagelayout_portrait, $l_pagelayout_landscape;
  global $c_pagelayout_portrait, $c_pagelayout_landscape;
  global $ccalendar_first_hour, $ccalendar_last_hour, $ccalendar_weekstart;
  global $l_first_hour, $l_last_hour, $l_daysofweekshort, $l_display_details;
  global $l_one_day_per_page, $l_display_days ;
  global $l_fontSize, $l_fontSize_small, $l_fontSize_medium, $l_fontSize_big;
 
  $date = $current_view->get_date();
  $p_view = $current_view->get_cal_view();
  $p_range = $current_view->get_cal_range();

  if ($p_view == 'list') {
    $block_detail = "
    <tr>
      <th><label for=\"cba_showdetails\">$l_display_details</label></th>
      <td>
        <input type=\"checkbox\" id=\"cba_showdetails\" name=\"cba_showdetails\" value=\"1\">
      </td>
    </tr><tr>
      <th><label for=\"cba_onedayperpage\">$l_one_day_per_page</label></th>
      <td>
        <input type=\"checkbox\" id=\"cba_onedayperpage\" name=\"cba_onedayperpage\" value=\"1\">
      </td>
    </tr>";
    
  } else if ($p_range != 'month') {
    $block_detail = "
      <tr>
      <th><label for=\"cba_showdetails\">$l_display_details</label></th>
      <td>
        <input type=\"checkbox\" id=\"cba_showdetails\" name=\"cba_showdetails\" value=\"1\">
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
      $selected_pagelayout = "selected=\"selected\"";
      if (empty($display_days)) $display_days = '1111111';

      $start_week_day = strtotime($ccalendar_weekstart);
      for ($i=0; $i<7; $i++) {
        $day_num = date('w', $start_week_day);
        $day = $l_daysofweekshort[$day_num];
        $cba_id = "cba_repeatday_$i";
        $dis_list_day .= "<span class=\"NW\"><input type=\"checkbox\" id=\"$cba_id\" name=\"$cba_id\" value=\"1\"";
        if (strcmp(substr($display_days,$day_num,1),'1')==0) {
          $dis_list_day .= " checked = \"checked\"";
        }
        $dis_list_day .= " /><label for=\"$cba_id\">$day</label></span>&nbsp;&nbsp; ";

        $start_week_day = strtotime('+1 day', $start_week_day); 
      }

      $block_week = "
      <tr>
        <th><label for=\"cba_onedayperpage\">$l_one_day_per_page</label></th>
        <td>
          <input type=\"checkbox\" id=\"cba_onedayperpage\" name=\"cba_onedayperpage\" value=\"1\">
        </td>
      </tr><tr id=\"display_days\">  
        <th>$l_display_days</th>
        <td>$dis_list_day</td>
      </tr>";
    }

    $block_detail .= "
      <tr>
        <th>$l_first_hour</th>
        <td>$dis_hour_b</td>
      </tr><tr>
        <th>$l_last_hour</th>
        <td>$dis_hour_e</td>
      </tr>
      $block_week";
  }

  $block = "<form action=\"\" method=\"get\" onsubmit=\"return check_calendar_pdf_options();\">
    <div class=\"detail extra\">
      <h1>Options</h1>
      <table>
        <tr>
          <th>$l_pagelayout</th>
          <td>
            <select name=\"sel_pagelayout\">
              <option value=\"$c_pagelayout_portrait\">$l_pagelayout_portrait</option>
              <option $selected_pagelayout value=\"$c_pagelayout_landscape\">$l_pagelayout_landscape</option>
              </select>
          </td>
        </tr>
        <tr>
          <th>$l_fontSize</th>
          <td>
            <select name=\"sel_fontSize\">
              <option value=\"6\">$l_fontSize_small</option>
              <option value=\"8\">$l_fontSize_medium</option>
              <option value=\"10\">$l_fontSize_big</option>
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
function dis_calendar_pdf_view($calendar, $current_view) {
  global $display, $path, $ccalendar_max_users, $obm;
  global $l_module_calendar;

  $p_view = $current_view->get_cal_view();
  $p_range = $current_view->get_cal_range();

  $calendar_entity = $current_view->get_entities();

  $calendar_function = 'dis_calendar_'.$p_range.'_'.$p_view.'_pdf';
  $calendar_function($calendar, $current_view, $calendar_entity);
}


/**
 * Calendar Month PDF
 * view agenda
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_month_agenda_pdf($calendar, $current_view, $calendar_entity) {
  global $ccalendar_weekstart, $ccalendar_first_hour;

  $date = clone $current_view->get_date();
  $date->setDay(1)->addDay(1)->subWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $date_timestamp = $date->getTimestamp();
  $end = clone $current_view->get_date();
  $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $category_filter = $current_view->get_category_filter();
  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter);

  foreach($occurrences as $occurrence){
    $c = clone $occurrence->date;
    $c->setHour(0)->setMinute(0)->setSecond(0);
    $id = $c->getTimestamp();
    if ($id < $date_timestamp) {
      $id = $date_timestamp;
      $occurrence->date->set($date, Of_Date::TIMESTAMP);
    }
    if(!is_array($updated[$id])) $updated[$id] = array();
    array_push($updated[$id], $occurrence);
  }

  $pdf = new CalendarPdfMonthAgenda($current_view, $calendar, $calendar_entity, $updated);
  $pdf->drawCalendar();
  $pdf->download();

}


/**
 * Calendar Custom PDF
 * view agenda
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_custom_agenda_pdf($calendar, $current_view, $calendar_entity) {
  global $ccalendar_first_hour, $ccalendar_last_hour, $ccalendar_weekstart;
  if($current_view->get_ndays() > 14) {
    dis_calendar_month_agenda_pdf($calendar, $current_view, $calendar_entity);
  } else {
    $range  = $current_view->get_date_range();
    $date = $range['begin'];
    $end = $range['end'];
    
    $status = array('ACCEPTED', 'NEEDS-ACTION');
    $category_filter = $current_view->get_category_filter();

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
    $pdf = new CalendarPdfDayAgenda($current_view, 'custom', $updated, $calendar_entity, $calendar);
    $pdf->drawCalendar();
    $pdf->download();
  }
}



/**
 * Calendar Week PDF
 * view agenda
 * 
 * @param 
 * @access public
 * @return void
 */
function dis_calendar_week_agenda_pdf($calendar, $current_view, $calendar_entity) {
  global $ccalendar_first_hour, $ccalendar_last_hour, $ccalendar_weekstart;

  $date = clone $current_view->get_date();
  
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);

  $status = array('ACCEPTED', 'NEEDS-ACTION');
  $category_filter = $current_view->get_category_filter();

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
  $pdf = new CalendarPdfDayAgenda($current_view, 'week', $updated, $calendar_entity, $calendar);
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
function dis_calendar_day_agenda_pdf($calendar, $current_view, $calendar_entity) {
  
  $date = clone $current_view->get_date();
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);

  $category_filter = $current_view->get_category_filter();
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

  $pdf = new CalendarPdfDayAgenda($current_view, 'day', $updated, $calendar_entity, $calendar);
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
function dis_calendar_month_list_pdf($calendar, $current_view, $calendar_entity) {
  global $ccalendar_weekstart, $ccalendar_first_hour;

  $date = clone $current_view->get_date();
  $date->setDay(1)->setHour($ccalendar_first_hour)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addMonth(1);

  $category_filter = $current_view->get_category_filter();
  $status = array('ACCEPTED');

  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter, true);
  $pdf = new CalendarPdfList($current_view, 'month', $calendar, $calendar_entity, $occurrences);
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
function dis_calendar_week_list_pdf($calendar, $current_view, $calendar_entity) {
  global $ccalendar_first_hour, $ccalendar_weekstart;

  $date = clone $current_view->get_date();
  $date->setWeekday($ccalendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(7);
  $category_filter = $current_view->get_category_filter();
  $status = array('ACCEPTED');

  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter, true);
  $pdf = new CalendarPdfList($current_view, 'week', $calendar, $calendar_entity, $occurrences);
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

function dis_calendar_day_list_pdf($calendar, $current_view, $calendar_entity) {
  global $ccalendar_first_hour;

  $date = clone $current_view->get_date();
  $date->setHour(0)->setMinute(0)->setSecond(0);
  $end = clone $date;
  $end->addDay(1);

  $category_filter = $current_view->get_category_filter();
  $status = array('ACCEPTED');

  $occurrences = calendar_events_model($date, $end, $calendar_entity, $status, $category_filter, true);

  $pdf = new CalendarPdfList($current_view, 'day', $calendar, $calendar_entity, $occurrences);
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
  $updated = array();
  foreach($column as $date => $cell) {
    $usedPositions = array();
    $position = 0;
    foreach ($cell as $key => $occurrence) {
      $event = $occurrence->event;
        unset($coordinate);
        if(!($coordinate = &$updated[$event->id .'-'.$occurrence->entity . '-' . $occurrence->id])) {
          if(empty($usedPositions))  {
            unset($unit);
            $unit = array('value' => 1);
          }
          while(isset($usedPositions[$position])) $position ++;
        
          unset($end);
          $end = array('value' => $position);
          if(count($cell) == ($key + 1)) {
            while($end['value'] < $unit['value'] && !isset($usedPositions[$end['value']])) $end['value']++;
          }
          if($end['value'] == $unit['value']) $end = &$unit;
          unset($coordinate);
          $coordinate = array('position' => $position, 'unit' => &$unit, 'end' => &$end, 'occurrence' => $occurrence);
          $updated[$event->id .'-'.$occurrence->entity . '-' . $occurrence->id] = &$coordinate;
        }
        $usedPositions[$coordinate['position']] = true;        
        if(count($cell) > $unit['value']) $unit['value'] = count($cell);
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

function html_share_calendar($id,$name) {
  global $l_html, $l_share_ics, $l_public, $l_private, $l_share_mail_search;
  global $ico_popup_close, $ico_html, $ico_ical; 
  global $prevent_public_agenda_sharing_by_url, $prevent_public_agenda_sharing_by_email;
  $entity_type = "user";

  if ($id == $GLOBALS['obm']['uid']) {
    $user = "<h1>".$GLOBALS['l_my_share_calendar']."</h1>";
  } else {
    $user = "<h1>".$GLOBALS['l_user_share_calendar']." $name</h1>";
  }
  
  $url_display_css = $mail_display_css = $tr_display_css = 'display:visible;';
  if ( $prevent_public_agenda_sharing_by_url == true ) $url_display_css = 'display:none;';                                                                  
  if ( $prevent_public_agenda_sharing_by_email == true ) $mail_display_css = 'display:none;';                                                               
  if ( $prevent_public_agenda_sharing_by_email == true && $prevent_public_agenda_sharing_by_url == true ) $tr_display_css = 'display:none;';

  //$params = get_calendar_entity_share($id,$entity_type);
  $share_pop_up = html_share_popup($params['accept_public']); 
  //get the Calendar public freebusy preferences of the current user
  $user_pref = get_one_user_pref($id, 'set_public_fb');
  $block = "
      $user
      <table class=\"spreadSheet share\">
        <thead>
          <tr>
            <th>
            </th>
            <th>
              $l_share_ics
            </th>
            <th>
              $l_html
            </th>
            <th>
            </th>
          </tr>
        </thead>
        <tbody> ";
  
  // test if the OBM freebusy information charing is permitted for the current user
  if($user_pref[$id]['value'] == 'yes'){
  	$block .= "
    	      <tr>
              <th>$GLOBALS[l_share_freebusy]
              <div class=\"sharedesc\">
                  <a id=\"tooltip_freebusy\" class=\"shareTip\">$GLOBALS[l_know_more]</a>
                </div>
              </th>
              <td>
                <a href=\"javascript: getShareUrl('freebusy','Ics');\" title=\"$GLOBALS[l_share_calendar_freebusy]\"><img src=\"$GLOBALS[ico_ref]\"></a>
                <a href=\"javascript: sendSharePopup('freebusy','Freebusy');\" title=\"$GLOBALS[l_send_calendar_freebusy]\"><img src=\"$GLOBALS[ico_mailforward]\"></a>
              </td><td></td><td></td>
            </tr>";
  }
  $block .="
          <tr style=\"$tr_display_css\">
            <th>
              $GLOBALS[l_share_calendar_public]
              <div class=\"sharedesc\">
                <a id=\"tooltip_public\" class=\"shareTip\">$GLOBALS[l_know_more]</a>
              </div>
            </th>
            <td>
              <a href=\"javascript: getShareUrl('public','Ics');\" title=\"$GLOBALS[l_share_calendar_public]\" style=\"$url_display_css\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('public','Ics');\" title=\"$GLOBALS[l_send_calendar_public]\" style=\"$mail_display_css\"><img src=\"$GLOBALS[ico_mailforward]\"></a>               
            </td>
            <td>
              <a href=\"javascript: getShareUrl('public','Html');\" title=\"$GLOBALS[l_share_calendar_public]\" style=\"$url_display_css\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('public','Html');\" title=\"$GLOBALS[l_send_calendar_public]\" style=\"$mail_display_css\"><img src=\"$GLOBALS[ico_mailforward]\"></a>               
            </td>
            <td>
              <a href=\"javascript: reinitSharePopup('public');\"><label>$GLOBALS[l_share_reinit_public]</label></a>
            </td>
          </tr>
          <tr>
            <th>
              $GLOBALS[l_share_calendar_private]
              <div class=\"sharedesc\">
                <a id=\"tooltip_private\" class=\"shareTip\">$GLOBALS[l_know_more]</a>
              </div>
            </th>
            <td>
              <a href=\"javascript: getShareUrl('private','Ics');\" title=\"$GLOBALS[l_share_calendar_private]\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('private','Ics');\" title=\"$GLOBALS[l_send_calendar_private]\"><img src=\"$GLOBALS[ico_mailforward]\"></a>               
            </td>
            <td>
              <a href=\"javascript: getShareUrl('private','Html');\" title=\"$GLOBALS[l_share_calendar_private]\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('private','Html');\" title=\"$GLOBALS[l_send_calendar_private]\"><img src=\"$GLOBALS[ico_mailforward]\"></a>               
            </td>
            <td>
              <a href=\"javascript: reinitSharePopup('private');\"><label>$GLOBALS[l_share_reinit_private]</label></a>               
            </td>
          </tr>";
  
  $block .="
        </tbody>
      </table>
      <input id=\"share_entity_id\" type=\"hidden\" value=\"$id\"/>
      <input id=\"share_entity_type\" type=\"hidden\" value=\"$entity_type\"/>
      <script>
        window.addEvent('domready', function() {
          $('tooltip_public').store('tip:text',\"<label>$GLOBALS[l_desc_share_adresse_public]</label>\");
          $('tooltip_private').store('tip:text',\"<label>$GLOBALS[l_desc_share_adresse_private]</label>\");
          new Obm.Tip($('tooltip_public'), {fixed:false, click:false}, 'shareTip');
          new Obm.Tip($('tooltip_private'), {fixed:false, click:false}, 'shareTip');
          var freebusyContainer = $('tooltip_freebusy');
          if ( freebusyContainer ) {
            freebusyContainer.store('tip:text',\"<label>$GLOBALS[l_desc_share_adresse_freebusy]</label>\");
            new Obm.Tip(freebusyContainer, {fixed:false, click:false}, 'shareTip');
          }

        });
      </script>
      $share_pop_up
    ";
  return $block;
}

function html_share_popup($token) {
  global $cgp_host, $ico_popup_close;
  global $l_share_public;

  $disp_url = "<div id=\"lnk_public_url\"></div>";

  $block = "
    <div id=\"calendarSharePopup\">
    </div>
  ";
  return $block;
}

/**
 * Used to keep a growing description of something that will be draw.
 * It's used to store as many data as we want, without drawing it,
 * and then to write it all in a row. It takes care about pages changes
 * and header drawing, and relative positions.
 */
Class PdfStub {
  const POS_RIGHT  = 1;
  const POS_BOTTOM = 2;

  private $data;
  private $pdf;
  private $current_x = 0;
  private $current_y = 0;
  private $current_color;
  private $origin_x;
  private $origin_y;
  private $pages_size;
  public $addpage_args;

  public function __construct($pdf, $origin_x=0, $origin_y=0) {
    $this->pdf = $pdf;
    $this->setOrigin($origin_x, $origin_y);
    $this->data = $this->pages_size = array();
    $this->addpage_args = array(true, false);
  }

  public function setOrigin($x=0, $y=0) {
    $this->origin_x = $x;
    $this->origin_y = $y;
  }

  public function setPosition($x=0, $y=0) {
    $this->current_x = $x;
    $this->current_y = $y;
  }
  public function setX($x) {
    $this->current_x = $x;
  }
  public function setY($y) {
    $this->current_y = $y;
  }
  public function addX($x=0) {
    $this->current_x += $x;
  }
  public function subY($y=0) {
    $this->current_y -= $y;
  }
  public function getX() {
    return $this->current_x;
  }
  public function getY() {
    return $this->current_y;
  }
  public function getPosition() {
    return array($this->current_x, $this->current_y);
  }
  public function setFillColor($color) {
    $this->current_color = $color;
  }


  /* Pages */
  public function addPage() {
    return $this->addElement(-1, 'writePage');
  }
  private function writePage() {
    $this->pages_size[] = -$this->current_y;
    call_user_func_array(array(&$this->pdf, 'addPage'), $this->addpage_args);
    $this->pdf->drawHeader();
    $this->setOrigin($this->pdf->content_x0, $this->pdf->current_y);
    $this->current_y = 0;
  }

  /* Image */
  public function addImage($width, $height, $image) {
    return $this->insertImage(-1, $width, $height, $image);
  }
  public function insertImage($idx, $width, $height, $image) {
    return $this->addElement($idx, 'writeImage', array($width, $height, $image),
                      array('width' => $width, 'height' => $height));
  }
  private function writeImage($width, $height, $image) {
    $x0 = $this->origin_x + $this->current_x;
    $y0 = $this->origin_y + $this->current_y -1;
    $this->pdf->page->drawImage($image, $x0, $y0, $x0 + $width, $y0 + $height);
  }

  /* Rectangle */
  public function addRectangle($width, $height, $flags=0) {
    return $this->insertRectangle(-1, $width, $height, $flags);
  }
  public function insertRectangle($idx, $width, $height, $flags=0) {
    return $this->addElement($idx, 'writeRectangle', array($width, $height, $flags),
                      array('width' => $width, 'height' => $height));
  }
  private function writeRectangle($width, $height, $flags) {
    $x0 = $this->origin_x + $this->current_x;
    $y0 = $this->origin_y + $this->current_y;
    $this->pdf->page->drawRectangle($x0, $y0, $x0 + $width, $y0 + $height, $flags);
  }

  /* Bounded text */
  public function addText($width, $height, $text, $enc,
                          $pflags=Zend_Pdf_Cell::POSITION_LEFT, $aflags=Zend_Pdf_Cell::ALIGN_LEFT) {
    // We use a dummy page for our text sizes calculations, beacause addElement() may
    // detect an overflow and put it in the next page.
    // Real page will be set in writeText().
    $page = new Zend_Pdf_Page($this->pdf->page);
    $page->setFont($this->pdf->font, $this->pdf->font_size);
    $cell = new Zend_Pdf_Cell($page, $pflags, $width, $height);
    $cell->setLocation(0, 0);
    $cell->addText($text, $aflags, 0, $enc);

    if($height == 0) {
      $height = (1 + $cell->getLineNumber()) * $this->pdf->font_height;
    }
    if($width == 0) {
      $width = $this->pdf->getStringWidth($text);
    }
    return $this->addElement(-1, 'writeText', array($cell),
                             array('width' => $width, 'height' => $height));
  }
  private function writeText($cell) {
    $cell->setLocation($this->origin_x + $this->current_x, $this->origin_y + $this->current_y);
    $cell->setPage($this->pdf->page);
    $cell->write();
  }

  private function addElement($idx, $func, $args = array(), $pos = array()) {
    if($idx == -1)
      $idx = count($this->data);

    $params = array_merge($pos, array('action' => $func, 'args' => $args,
                                      'x' => $this->current_x, 'y' => $this->current_y));

    if(isset($this->current_color)) {
      $params['color'] = $this->current_color;
    }

    array_splice($this->data, $idx, 0, array($params));
    return $idx;
  }

  private function getLastElement($back = 1) {
    $i = count($this->data) - $back;
    if($i < 0)
      return null;
    else
      return $this->data[$i];
  }

  public function getLastElementHeight() {
    $i = 0;
    do {
      $last = $this->getLastElement(++$i);
    } while($last != null && !isset($last['height']));
    return $last['height'];
  }

  public function gotoNextSpacePosition($direction, $back=1) {
    $i = count($this->data);
    if($i == 0)
      return;

    do {
      $prev = $this->data[ --$i ];
      if(   (isset($prev['x']) && isset($prev['width'])  && $direction & PdfStub::POS_RIGHT)
         || (isset($prev['y']) && isset($prev['height']) && $direction & PdfStub::POS_BOTTOM))
        break;
    } while($i >= 0);

    if($direction & PdfStub::POS_RIGHT) {
      $this->current_x += $prev['width'];
    }
    if($direction & PdfStub::POS_BOTTOM) {
      $this->current_y -= $prev['height'];
    }
  }

  public function write() {
    foreach($this->data as $stub) {
      if(isset($stub['y'])) {
        $this->current_y = $stub['y'];
        $this->current_y += array_sum($this->pages_size); // Convert to absolute coords
        if($this->origin_y + $this->current_y - abs($stub['height']) < $this->pdf->content_y0) {
          $this->current_y -= $this->pdf->font_height;
          $this->writePage();
        }
      }

      if(isset($stub['x']))
        $this->current_x = $stub['x'];

      if(isset($stub['color']))
        $this->pdf->page->setFillColor($stub['color']);

      call_user_func_array(array(&$this, $stub['action']), $stub['args']);
    }
  }
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
  public $current_view, $calendar_entity;
  public $editor;

  function __construct($current_view, $calendar, $calendar_entity){
    global $ccalendar_weekstart, $ico_pdf_logo, $path;
    global $ico_pdf_private, $ico_pdf_meeting, $ico_pdf_periodic, $ico_pdf_allday;

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
    $font_size = intval($calendar['fontSize']);

    $this->font = Zend_Pdf_Font::fontWithName(Zend_Pdf_Font::FONT_HELVETICA_BOLD);
    $this->font_size = ($font_size>=6 && $font_size<=12) ? $font_size : 6;
    $this->font_height = ($this->font->getLineHeight()/$this->font->getUnitsPerEm()) * $this->font_size;
    $this->line_gap = ($this->font->getLineGap()/$this->font->getUnitsPerEm()) * $this->font_height;
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

    $this->obm_logo = Zend_Pdf_Image::imageWithPath($ico_pdf_logo);
    $this->icon_size = 6;
    $this->ico_private = Zend_Pdf_Image::imageWithPath($ico_pdf_private);
    $this->ico_meeting = Zend_Pdf_Image::imageWithPath($ico_pdf_meeting);
    $this->ico_periodic = Zend_Pdf_Image::imageWithPath($ico_pdf_periodic);
    $this->ico_allday = Zend_Pdf_Image::imageWithPath($ico_pdf_allday);

    $this->calendar_weekstart = $ccalendar_weekstart;
    $this->tmp_filename = tempnam("/tmp", "obm_") ;

    $this->current_view = $current_view;
    $this->calendar_entity = $calendar_entity;

    $userinfo = get_entity_info($obm['uid'], 'user');
    $this->editor = $userinfo['label'];
    $this->showdetails = $calendar['showdetails'];
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
      $this->content_y1+$this->line_gap,
      'UTF-8');

    // Editor
    $this->page->drawText(
      $this->editor,
      $this->content_x0+90,
      $this->content_y1-$this->font_height+$this->line_gap,
      'UTF 8');

    // Date creation
    $today = new Of_Date();
    $this->page->drawText(
      $today->getOutputDateTime(),
      $this->content_x0+90,
      $this->content_y1-2*$this->font_height+$this->line_gap,
      'UTF-8');

    // Category
    $category_filter = $this->current_view->get_category_filter();
    if (!empty($category_filter)){
      $category = of_category_get_label("event", "category1", $category_filter);
    } else {
      $category = $l_all_f; 
    }
    $display_category = "$l_category: $category";
    $this->page->drawText(
      $display_category,
      $this->content_x0+90,
      $this->content_y1-3*$this->font_height+$this->line_gap,
      'UTF-8');
  }


  /**
   * draw pdf legend at bottom left
   */
  function setLegend(){
    $i=0;
    foreach($this->calendar_entity as $users) {
      foreach($users as $user){
        $username = $this->truncateToWidth($user["name"],$x1-($x0+4));
        $class = $this->class[$user["class"]];
        $this->page->setFillColor(new Zend_Pdf_Color_Html($class["title"]));
        $x0 = $this->content_x0 +$i;
        $y0 = $this->content_y0;
        $x1 = $x0 + 90;
        $y1 = $y0 + $this->font_height;
        $this->page->drawRectangle($x0+2 , $y0, $x1, $y1+2,Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        $this->page->drawText($username, $x0+4, $y0+$this->line_gap, 'UTF-8');
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
      $this->pdf->pages[$index]->drawText("$cur/$total", $this->content_x1, $this->content_y0+$this->line_gap, 'UTF-8');
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

  /**
   * Return the string truncated to the correct width
   */
  function truncateToWidth($string, $max_width) {
    $drawingString = iconv('UTF-8', 'UTF-16BE//IGNORE', $string);
    $characters = array();
    $stringLength = strlen($drawingString);
    for ($i = 0; $i < $stringLength; $i++) {
      $characters[] = (ord($drawingString[$i++]) << 8) | ord($drawingString[$i]);
    }
    $glyphs = $this->font->glyphNumbersForCharacters($characters);
    $widths = $this->font->widthsForGlyphs($glyphs);

    $width = 0;
    $unitsPerEm = $this->font->getUnitsPerEm();
    for ($i = 0; ($i < $stringLength) && ($width < $max_width); $i++) {
      $width += ($widths[$i] / $unitsPerEm) * $this->font_size;
    }
    if ($width > $max_width)
      $i--;

    return substr($string,0,$i);
  }
  
  function cutLongWords ($string, $max_width, $depth = 0) {
    // infinite recursive loop protection
    $depth += 1;
    if ($depth > 20) return $string;
    
    $vowels = '/[aeiouy]/';
    
    $words = split(' ', $string);
    foreach ($words as $k => $v) {
      if ($this->getStringWidth($v) > $max_width) {
        $letters = str_split($v, 1);
        $insert_pos = -1;
        for ($i = round(count($letters) / 2), $n = count($letters); $i < $n - 1; $i += 1) {
          if (preg_match($vowels, $letters[$i]) && !preg_match($vowels, $letters[$i + 1])) {
            $insert_pos = $i - 1;
            break;
          }
        }
        
        if ($insert_pos == -1) {
          $insert_pos = round(count($letters) / 2);
        }
        
        $words[$k] = $this->cutLongWords(substr($v, 0, $insert_pos), $max_width, $depth). '- '
          . $this->cutLongWords(substr($v, $insert_pos), $max_width, $depth);
      }
    }
    
    return join(' ', $words);
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

  function __construct($current_view, $calendar, $calendar_entity, $updated){
    global $l_daysofweekl, $ccalendar_first_hour;

    parent::__construct($current_view, $calendar, $calendar_entity);

    $this->calendar_x0 = $this->content_x0 + 10;
    $this->calendar_y0 = $this->content_y0 + 18;
    $this->calendar_x1 = $this->content_x1;
    $this->col_width = ($this->calendar_x1 - $this->calendar_x0)/7;
    $this->min_col_height = ($this->calendar_y1 - $this->calendar_y0)/5;
    $this->col_height = $this->min_col_height;
    $this->current_y = 0;

    // Display date
    $this->display_date = $this->current_view->get_date()->localize(Of_Date::OBM_DATE_MONTH);

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

    $date = clone $this->current_view->get_date();
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
        $this->calendar_y1+$this->font_height+$this->line_gap,
        'UTF-8');
      $i++;
      $current->addDay(1);
    }
  }


  /**
   * draw calendar grid + events
   */
  function drawCalendar(){

    $date = clone $this->current_view->get_date();
    $date->setDay(1)->addDay(1)->subWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
    $end = clone $this->current_view->get_date();
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
      } elseif ($current->compareMonth($this->current_view->get_date()) != 0) {
        $bgcolor = new Zend_Pdf_Color_Html('#EFF0F2');
      } else {
        $bgcolor = new Zend_Pdf_Color_Html('#FFFFFF');
      }

      // draw one week
      if ($current->compareWeekday($this->calendar_weekstart) == 0) {
        $nb_events=0;
        $end_week_time = $current->getTimestamp() + ((6 * 24 + $this->calendar_last_hour) * 3600);
        
        for($s = clone $current; $s->getTimestamp() <= $end_week_time; $s->addDay(1)){
          $nb_events = max($nb_events, sizeof($this->updated[$s->getTimestamp()])); // max events per day
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
          $this->calendar_y1-$this->current_y+$this->col_height-($this->col_height/2)+$this->line_gap,
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
        $this->calendar_y1-$this->current_y+$this->col_height+$this->line_gap,
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
    $date = clone $this->current_view->get_date();
    $date->setDay(1)->addDay(1)->subWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
    $end = clone $this->current_view->get_date();
    $end->setDay(1)->addMonth(1)->subDay(1)->addWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
    $current = clone $date;

    while($current->compare($end) < 0) {
      if ($current->compareWeekday($this->calendar_weekstart) == 0) {
        $nb_events=0;
        $end_week_time = clone $current;
        $end_week_time->addDay(6);

        for($s = clone $current; $s->compare($end_week_time) <= 0; $s->addDay(1)){
          $nb_events = max($nb_events, sizeof($this->updated[$s->getTimestamp()])); // max events per day
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
        $next_week = clone $end_week_time;
        $next_week->addDay(1);
        for($s = clone $current; $s->compare($end_week_time) <= 0; $s->addDay(1)){
          $pending = $this->_get_pending_multi_days_events($s->getTimestamp());
          if ($pending) {
            $this->addEvents($pending, $day, $next_week->getTimestamp());
          }
          if(is_array($this->updated[$s->getTimestamp()])){
            $this->addEvents($this->updated[$s->getTimestamp()], $day, $next_week->getTimestamp());
          }
          $day++;
        }
      }

      $current->addDay(1);
    }

  }


  /**
   * add an event
   */
  function addEvents($occurrences, $day, $ts_start_next_week){
    global $l_private, $obm;
    $cur = 0;

    foreach($occurrences as $occurrence){
      $event = $occurrence->event;
      $title = "";
      $id = $occurrence->id;
      $color = $occurrence->event->color;
      $entity = $this->calendar_entity[$occurrence->entity][$id];
      $class = $this->class[$entity["class"]];
      $attendee = $occurrence->event->attendee;

      $icons = array();
      if($event->isPrivate()) {
        $icons[] = $this->ico_private;
      }
      if($event->isMeeting()) {
        $icons[] = $this->ico_meeting;
      }
      if ($event->isPeriodic()) {
        $icons[] = $this->ico_periodic;
      }
      if ($event->isAllDay()) {
        $icons[] = $this->ico_allday;
      }

      if (!$occurrence->event->all_day){
        $title .= $occurrence->date->get(Of_Date::TIME_SHORT).' ';
      }
      
      if ($occurrence->event->privacy && !(array_key_exists($obm['uid'],$attendee['user']))) {
        $title = $l_private;
      } else {
        $title .= $occurrence->event->title;
      }

      if (!empty($color) && sizeof($this->calendar_entity)==1) {
        $title_color = $color;
      } else {
        $title_color = $class["title"];
      }

      $curday = $occurrence->date->getDateIso();

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
        if (!is_array($this->usedPosition[$begin->getDateIso()])) {
          $this->usedPosition[$begin->getDateIso()] = array();
        }
        array_push($this->usedPosition[$begin->getDateIso()], $cur);
        $begin->addDay(1);
        while(($begin->compare($occurrence->end) < 0) && ($begin->getTimeStamp() < $ts_start_next_week)) {
          $x1 +=$this->col_width;
          if (!is_array($this->usedPosition[$begin->getDateIso()])) {
            $this->usedPosition[$begin->getDateIso()] = array();
          }
          array_push($this->usedPosition[$begin->getDateIso()], $cur);
          $begin->addDay(1);
        }
        if ($occurrence->end->getTimestamp()>$ts_start_next_week) {
          $pending_event = clone $occurrence;
          $pending_event->date->set($ts_start_next_week,Of_Date::TIMESTAMP);
          $this->_add_pending_multi_days_event($pending_event,$ts_start_next_week);
        }
      }

      if ($occurrence->entity_state == "NEEDS-ACTION") {
        $this->page->setAlpha($this->waiting_alpha);
      } else {
        $this->page->setAlpha(1);
      }

      $icon_width = count($icons)*$this->icon_size;
      while($this->getStringWidth($title) > $x1-$x0 -$icon_width) {
         $title = substr($title, 0, -1); 
      }
      if ($x1 > $this->calendar_x1) $x1 = $this->calendar_x1;

      $this->page->drawRectangle($x0,$y0,$x1,$y0-$this->font_height,Zend_Pdf_Page::SHAPE_DRAW_FILL);
      $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));

      foreach ($icons as $i => $ico) {
        $x = $x0 + $i*$this->icon_size;
        $y = $y0 - ($this->font_height + $this->icon_size)/2;
        $this->page->drawImage($ico, $x, $y, $x + $this->icon_size, $y + $this->icon_size);
      }
      $this->page->drawText($title,
        $this->calendar_x0+$day*$this->col_width+1 + $icon_width, 
        $y0-$this->font_height+$this->line_gap, 'UTF-8');
      $cur++;
    }
  }

  function is_multi_days_event($occurrence) { 
    $multiday_limit = clone ($occurrence->date);
    $multiday_limit->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);
    $day_end = $occurrence->end;
    if($day_end->getTimestamp() > $multiday_limit->getTimestamp()) {
      return true;
    }

    return false;
  }

  private function _add_pending_multi_days_event($occurence,$day) {
    if (!is_array($this->pending_multi_days))
      $this->pending_multi_days = array();
    if (!is_array($this->pending_multi_days[$day]))
      $this->pending_multi_days[$day] = array();
    $this->pending_multi_days[$day][] = $occurence;
  }

  private function _get_pending_multi_days_events($day) {
    if (is_array($this->pending_multi_days) && is_array($this->pending_multi_days[$day]))
      return $this->pending_multi_days[$day];
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

  function __construct($current_view, $range, $updated, $calendar_entity, $calendar){

    parent::__construct($current_view, $calendar, $calendar_entity);

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

    $date = clone $current_view->get_date();

    if ($this->range == 'week') {
      $begin = clone $date;
      $begin->setWeekday($this->calendar_weekstart)->setHour(0)->setMinute(0)->setSecond(0);
      $end = clone $begin;
      $end->addDay(7);
      $this->display_date = $begin->localize(Of_Date::OBM_DATE_WEEK).' - '
        .$end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK);

      foreach ($this->updated as $col_date => $column) {
        if ($col_date < $begin->getTimestamp()) {
          $this->updated[$begin->getTimestamp()] = array_merge($this->updated[$begin->getTimestamp()], $column);
          unset($this->updated[$col_date]);
        }
      }
    } elseif ($this->range == 'day') { 
      $end = clone $date;
      $end->addDay(1);
      $this->display_date = $date->localize(Of_Date::OBM_DATE_DAY);
    } elseif ($this->range == 'custom') {
      $range = $current_view->get_date_range();
      $begin = $range['begin'];
      $end = $range['end'];
      $this->display_date = $begin->localize(Of_Date::OBM_DATE_WEEK).' - '
        .$end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK);
    }

    $this->week_num = $date->get(Of_Date::WEEK); 

    $this->getNbOutEvents();
    $this->allday_height = (1+$this->nb_out_events) * $this->font_height;
    $this->onedayperpage = $calendar['onedayperpage'];

    if ($this->range == "week") {
      $display_days= '';
      for ($i=0; $i<7; $i++) {
          $display_days .= isset($calendar["repeatday_$i"]) ? '1' : '0';
      }
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
    } elseif ($this->range == 'custom') {
      $this->nbcol = $current_view->get_ndays(); 
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
      $this->addEvents($column, $date);
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
  function addEvents($events, $column_ts) {
    global $l_private, $obm;

    foreach($events as $event){
      $id = $event->id;
      $all_day = $event->event->all_day;
      $entity = $this->calendar_entity[$event->entity][$id];
      $class = $this->class[$entity["class"]];
      $background = $class["background"];
      $duration = $event->event->duration;
      $date = $event->date->get(Of_Date::TIME_SHORT);
      $color = get_color_data($event->event->color);
      $attendee = $event->event->attendee;
      
      if (!empty($color) && sizeof($this->calendar_entity)==1) {
        $bg_color = $color['body'];
        $title_color = $color['header'];
      } else {
        $bg_color = $class["background"];
        $title_color = $class["title"];
      } 
      
      $icons = array();
      if($event->event->isPrivate()) {
        $icons[] = $this->ico_private;
      }
      if($event->event->isMeeting()) {
        $icons[] = $this->ico_meeting;
      }
      if ($event->event->isPeriodic()) {
        $icons[] = $this->ico_periodic;
      }
      if ($event->event->isAllDay()) {
        $icons[] = $this->ico_allday;
      }

      $privatized_event = $event->event->privacy && !(array_key_exists($obm['uid'],$attendee['user']));
      
      if ($privatized_event) {
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

        $curdate = clone $event->date;
        $curdate->set($column_ts,Of_date::TIMESTAMP);
        $curday = $curdate->getDateIso();
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
         $begin = clone $curdate;
         while($begin->compare($event->end) < 0) {
           $begin->addDay(1)->setHour(0)->setMinute(0)->setSecond(0);
           if (array_key_exists($begin->getTimestamp(), $this->updated)) {
             if ($this->range != "day" && $event->end->getTimestamp() > $begin->getTimestamp()) {
               $x1 +=$this->col_width;
               if (!is_array($this->usedPosition[$begin->getDateIso()])) {
                 $this->usedPosition[$begin->getDateIso()] = array();
               }
               array_push($this->usedPosition[$begin->getDateIso()], $this->current_out_event);
             }
           }
         }
        }

        $icon_width = count($icons)*$this->icon_size;
        $title = $this->truncateToWidth($title,$x1-$x0 -$icon_width);
        $this->page->setFillColor(new Zend_Pdf_Color_Html($title_color));
        $this->page->drawRectangle($x0, $y0, $x1, $y1, Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        foreach ($icons as $i => $ico) {
          $x = $x0 + $i*$this->icon_size;
          $y = $y0 - ($this->font_height + $this->icon_size)/2;
          $this->page->drawImage($ico, $x, $y, $x + $this->icon_size, $y + $this->icon_size);
        }
        $this->page->drawText($title, $x0+1 +$icon_width, $y1+$this->line_gap, 'UTF-8');
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
        $end = $dateend->getHour()+($dateend->getMinute() + ($dateend->getMinute()%$time_unit))/60; 
        if($end > $this->calendar_last_hour) $end = $this->calendar_last_hour;

        $y1 = $this->calendar_y1-$this->allday_height-($begin - $this->calendar_first_hour)*$this->col_height;
        $height = ($end-$begin)*$this->col_height;
        $y0 = $y1-$height;

        $event_body = '';
        if ($height < $this->font_height*2) {
          $dis_header = "$date $title";
          if (!$privatized_event) {
            if (!empty($event->event->location)) {
              $dis_header .= ' ('. $event->event->location. ')';
            }
            
            if ($this->showdetails && !empty($event->event->description)) {
              $dis_header .= " : ". $event->event->description;
            }
          }          
        } else {
          $dis_header = $date;
          $event_body = $title;
          
          if (!$privatized_event) {
            if (!empty($event->event->location)) {
              $event_body .= ' ('. $event->event->location. ')';
            }
            
            if ($this->showdetails && !empty($event->event->description)) {
              $event_body .= " : ". $event->event->description;
            }
          }
        }
        $dis_header = html_entity_decode($dis_header, ENT_COMPAT,'UTF-8');
        $event_body = html_entity_decode($event_body, ENT_COMPAT,'UTF-8');
        // event title
        $this->page->setLineColor(new Zend_Pdf_Color_Html($title_color));
        $this->page->setFillColor(new Zend_Pdf_Color_Html($bg_color));
        $this->page->drawRectangle($x0,$y0,$x1,$y1);
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        
        $text_cell_width = ($x1 - $x0 - 2);
        $cell = new Zend_Pdf_Cell($this->page, Zend_Pdf_Cell::POSITION_LEFT,
                                  $text_cell_width, ($y1-2*$this->font_height) - $y0);
        $cell->setLocation($x0+2, $y1-2*$this->font_height);
        
        // if $dis_title contains long words, Zend_Pdf_Cell do not display them
        $event_body = $this->cutLongWords($event_body, $text_cell_width - 2);
        
        $cell->addText($event_body, Zend_Pdf_Cell::ALIGN_LEFT, 0, 'UTF-8');
        $cell->write();

        // event header
        $icon_width = count($icons)*$this->icon_size;
        // #949 ... WTF ?
        // while($this->getStringWidth($dis_header) > $event->size*$this->col_width -$icon_width) {
        //   $dis_header = substr($dis_header, 0, -1); 
        // }
        $dis_header = $this->truncateToWidth($dis_header,$event->size*$this->col_width -$icon_width);
        $this->page->setFillColor(new Zend_Pdf_Color_Html($title_color));
        $this->page->drawRectangle($x0,$y1-$this->font_height,$x1,$y1,Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $y = $y1 - ($this->font_height + $this->icon_size)/2;
        foreach ($icons as $i => $ico) {
          $x = $x0 + $i*$this->icon_size;
          $this->page->drawImage($ico, $x, $y, $x + $this->icon_size, $y + $this->icon_size);
        }
        $this->page->setFillColor(new Zend_Pdf_Color_Html('#FFFFFF'));
        $this->page->drawText($dis_header, $x0+1 +$icon_width, $y1-$this->font_height+$this->line_gap, 'UTF-8');
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
    $day_begin = $occurrence->date->getDateIso();
    $d = clone $occurrence->date;
    $d->addTimestamp($occurrence->event->duration);
    $day_end = $d->getDateIso();

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
  var $max_length_details;

  function __construct($current_view, $range, $calendar, $calendar_entity, $occurrences){
    global $ccalendar_first_hour;
    global $ico_pdf_private_alt, $ico_pdf_meeting_alt, $ico_pdf_periodic_alt;
    global $ico_pdf_allday_alt;

    parent::__construct($current_view, $calendar, $calendar_entity);

    $this->max_length_details = array("A4-LANDSCAPE" => array(6 => 185, 8 => 135, 10 => 107),
                                      "A4" => array(6 => 96, 8 => 70, 10 => 55));
    $this->ico_private = Zend_Pdf_Image::imageWithPath($ico_pdf_private_alt);
    $this->ico_meeting = Zend_Pdf_Image::imageWithPath($ico_pdf_meeting_alt);
    $this->ico_periodic = Zend_Pdf_Image::imageWithPath($ico_pdf_periodic_alt);
    $this->ico_allday = Zend_Pdf_Image::imageWithPath($ico_pdf_allday_alt);

    $this->calendar_x0 = $this->content_x0;
    $this->calendar_y0 = $this->content_y0;
    $this->calendar_x1 = $this->content_x1;
    $this->occurrences = $occurrences;
    $this->calendar_entity = $calendar_entity;
    $date = $this->current_view->get_date();
    $end = clone $date;

    if ($range == 'week') {
      $end->addDay(7);
      $this->display_date = $date->localize(Of_Date::OBM_DATE_WEEK).' - '.
        $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK);
    } else {
      $this->display_date = $date->localize(constant("Of_Date::OBM_DATE_".strtoupper($range)));
    }

    $this->onedayperpage = $calendar['onedayperpage'];

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
      $stub = new PdfStub($this, $this->content_x0, $this->current_y);
      $page = 0;
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

        if ($this->onedayperpage && $page > 0) {
          $stub->addPage();
        }

        list($x0, $y0) = $stub->getPosition();
        $stub->addX(2);
        $stub->subY(1);
        $stub->setFillColor(new Zend_Pdf_Color_Html("#368397"));
        $day_rect = $stub->addText(0, 0, $day_text_evt, 'UTF-8');
        $stub->setPosition($x0, $y0);

        // draw events
        $this->addEvents($occurrence, $stub);
        $occ_y = $stub->getY() - $y0;

        // one day
        $stub->setPosition($x0, $y0 + $this->font_height);
        $stub->setFillColor(new Zend_Pdf_Color_Html($color));
        $stub->insertRectangle($day_rect, $this->content_x1 - $this->content_x0,
                               $occ_y - $this->font_height*2,
                               Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $stub->setPosition($x0, $y0 + $occ_y - $this->font_height*2);

        $page++;
      }
      $stub->write($this);
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
    $this->page->drawText($l_date, $this->content_x0+2, $this->calendar_y1+$this->line_gap, 'UTF-8');
    $this->page->drawText($l_event, $this->content_x0+150, $this->calendar_y1+$this->line_gap, 'UTF-8');

    $this->current_y = $this->calendar_y1-$this->font_height-1;

  }
  
  /**
   * add an event to the PDF, in the PdfStub $stub object
   * which describes the drawing instructions.
   */
  function addEvents($occurrences, $stub) {
    global $obm, $l_private;
    global $l_daily, $l_weekly, $l_monthlybydate, $l_monthlybyday, $l_yearly;
    global $l_category,$l_datebegin, $l_dateend, $l_repeat,$l_attendees, $l_title, $l_location;;
    global $l_resources, $l_items, $l_desc;

    $uid = $obm['uid'];

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
      
      // bug on privacy fix
      $title = $head;
      
      $end = clone $occurrence->end;
      $attendee = $event->attendee;
      $properties = $event->properties;
      $dis_attendees = array();
      $dis_people = "";
      $dis_res = "";
      $dis_resources = array();
      $separator = "";
      
      $private_event = $event->privacy && !(array_key_exists($obm['uid'],$attendee['user']));
      
      if ($private_event) {
        $title = $l_private;
      }

      $icons = array();
      if($event->isPrivate()) {
        $icons[] = $this->ico_private;
      }
      if($event->isMeeting()) {
        $icons[] = $this->ico_meeting;
      }
      if ($event->isPeriodic()) {
        $icons[] = $this->ico_periodic;
      }
      if ($event->isAllDay()) {
        $icons[] = $this->ico_allday;
      }
      
      // bug on privacy fix
      $head = $title;
      if (!$this->showdetails && !$private_event && $event->location != '') {
        $head .= " ($l_location : $event->location)";
      }

      if ( is_array($attendee['user']) &&
           ( (count($attendee['user']) > 1) || (count($attendee['contact']) > 0) || !array_key_exists($obm['uid'],$attendee['user']) )
         )
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

      // Event title
      foreach ($icons as $i => $ico) {
        $stub->setX(168 - $margin_left - $this->icon_size*($i+1));
        $stub->addImage($this->icon_size, $this->icon_size, $ico);
      }
      $stub->setFillColor(new Zend_Pdf_Color_Html("#368397"));
      $stub->setX(169 - $margin_left);
      $stub->addText($this->calendar_x1 - $this->content_x0 - (169 - $margin_left)
                     - $this->getStringWidth($dis_people.$dis_res), 0, $head, 'UTF-8');
      $title_h = $stub->getLastElementHeight();

      // Event attendees + resources
      $stub->gotoNextSpacePosition(PdfStub::POS_RIGHT);
      $bar_idx = $stub->addText($this->getStringWidth($dis_people.$dis_res) + 10,
                                0, $dis_people.$dis_res, 'UTF-8');
      $stub->subY($title_h);
      
      if ($this->showdetails && !$private_event) {
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
        // left bar
        $x0 = 170;

        // details list
        $y0_bar = $stub->getY();
        $stub->setFillColor(new Zend_Pdf_Color_Html("#3D3D3D"));
        foreach($details as $label => $value){
          $stub->setX($x0+5);
          $stub->addText(0, 0, $label, 'UTF-8');
          if (!is_array($value)) {
            $value = array($value);
          }
          if (sizeof($value)==0) $stub->subY($this->font_height);
          foreach($value as $array_value){
            $lines = $this->cut_up_line($array_value);
            foreach($lines as $line) {
              $stub->setX($x0+80);
              $stub->addText($this->calendar_x1 - ($x0+80) - $this->calendar_x0,
                             0, $line, 'UTF-8');
              $stub->gotoNextSpacePosition(PdfStub::POS_BOTTOM);
            }
          }
        
        }
        $stub->setFillColor(new Zend_Pdf_Color_Html("#B3B3B3"));
        $stub->setX(170);
        $desc_y1 = $stub->getY();
        $stub->setY($y0_bar + $this->font_height - $this->line_gap);
        $stub->insertRectangle($bar_idx, 3, $desc_y1 - $y0_bar, Zend_Pdf_Page::SHAPE_DRAW_FILL);
        $stub->setY($desc_y1);
      }
    }
  }

 function cut_up_line($text) {
    $line_length = $this->max_length_details[$this->page_orientation][$this->font_size];
    $text = trim($text);
    $array_line = explode("\n", $text);
    $i = 0;
    foreach($array_line as $line){
      $array_word = explode(" ", $line);
      foreach($array_word as $word){
        if(strlen($word) > $line_length) {
   	  $array_cut_word = str_split($word, $line_length - 10);
          foreach($array_cut_word as $cut_word){
	    if( (strlen($ret[$i]) + strlen($cut_word)) > $line_length){
              $i = $i + 1;
            }
            $ret[$i] .= $cut_word." ";
          }
        
        } else {
          if(((strlen($ret[$i]) + strlen($word)) > $line_length)){
            $i = $i + 1;
          }
          $ret[$i] .= trim($word)." ";
        }
      }
      $i = $i + 1;
    }
    return $ret;
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
        <input class='otherAttendee' name='tf_others_attendees[]' value=\"$email\" />
        </div>";
      $count ++;
    }
  }

  $email_block .= "
    <div class=\"multiple\">
    <a onclick=\"remove_element(this.parentNode,'calendarMailHome');return false\" href=\"\">
     <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
    </a>        
    <input  class='otherAttendee' name='tf_others_attendees[]' value='' />  
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

function html_calendar_template_name_popup() {
  global $ico_popup_close, $l_save_as_template, $l_template_name,
  $l_validate, $l_cancel;
  
  return "<div id=\"createTemplatePopup\" class=\"obmPopup\">
    <h1>
      <a href=\"\" class=\"closeSticky\" onclick=\"return false;\">
        <img src=\"$ico_popup_close\" alt=\"close\" />
      </a>
      <span>$l_save_as_template</span>
    </h1>
    <div>
      <form id=\"createTemplatePopupForm\" onsubmit=\"setTemplateName(this);return false;\">
        <fieldset style=\"border:none;\">
          $l_template_name : <input type=\"text\" maxlength=\"255\" id=\"tf_template_name\" name=\"name\" />
        </fieldset>
        <fieldset class=\"buttons\">
          <input class=\"button closeSticky\" type=\"submit\" value=\"$l_validate\" />
          <input class=\"button closeSticky\" onclick=\"return false;\" type=\"submit\" value=\"$l_cancel\" />
        </fieldset>
      </form>
    </div>
  </div>";
}

/**
 * Generate file upload block
 * 
 * @return string
 */
function html_calendar_file_block() {
  global $c_quota_private_document, $l_space_left, $obm;

  $nbdocs = $c_quota_private_document - $nb_private;
  $GLOBALS['js']['vars']['consts']['nbdocs'] = $nbdocs-1;
  $style = "";
  if ($nbdocs <= 1) {
    $style = "style='display:none;'";
  }

  $ini_max_filesize = upload_max_filesize_to_bytes(ini_get('upload_max_filesize'));
  $space_left_in_bytes = $c_quota_private_document - run_query_calendar_user_quota($obm['uid']);

  return "<th class='' id='calendarFileBlock'>
    $GLOBALS[l_other_files]
    <a id='addFileButton' href='#' $style onclick=\"add_file_field();return false;\">
      <img src=\"$GLOBALS[ico_add]\" alt=\"[Add file field]\" />
    </a>
  </th>

  <td id='calendarFileHome'>
    <div>
       $GLOBALS[l_other_files_maxsize]" . dis_filesize(min($ini_max_filesize, $c_quota_private_document)) . " (" . dis_filesize($space_left_in_bytes) . " $l_space_left) $test
    </div>
    <div class=\"multiple\">
      <a onclick=\"remove_element(this.parentNode,'calendarFileHome');obm.vars.consts.nbdocs++;$('addFileButton').style.display = '';return false\" href=\"\">
       <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
    </a>
      <input type=\"file\" size=\"7\" class=\"otherFile\" name=\"fi_other_files[]\" />
    </div>
  </td>";
}

/**
 * Generate document attachment form block
 * 
 * @return string
 */
function html_calendar_documents_form_fieldset($documents = null) {
  global $ico_add, $ico_delete, $l_documents;
  
  if (is_array($documents)) {
    foreach ($documents as $d_id => $d_ent) {
      $d_name = $d_ent['label'];
      $sel_id = "data-document-$d_id";
      $div_id = "sel_document_id-$sel_id";
      $dis_sel_document .= "
        <div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_document_id');\">
        <img src=\"$ico_delete\" alt=\"[Delete]\" />
        </a>
        $d_name
        <input value=\"$sel_id\" name=\"sel_document_id[]\" type=\"hidden\" />
        </div>";
    }
  }
  
  return "
    <tr>
      <th>
      $l_documents
        <a href=\"javascript: return false;\" 
          onclick=\"popup('$GLOBALS[path]/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_document_id','');
          return false;\">
          <img src=\"$ico_add\" alt=\"[Add]\" />
        </a>
      </th>
      <td>
        <input type=\"text\" name=\"\" value=\"\" id=\"documentSearch\" autocomplete=\"off\" />
      </td>
    </tr>
    <tr>
      <th>&nbsp;</th>
      <td id=\"sel_document_id\">$dis_sel_document</td>
    </tr>
    <tr>".html_calendar_file_block()."</tr>";
}


/**
 * 
 * Portlet: Waiting events
 * 
 */
function dis_calendar_waiting_portlet() {
  $data = run_query_calendar_waiting_events();
  while ($data->next_record()) {
    $evt = $data->Record;
    $evt_begin = new Of_Date($evt['event_date'], 'GMT');
    $id = $evt['event_id'];
    $url = basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=$id";
    $evt_end = clone $evt_begin;
    $evt_end->addSecond($evt['event_duration']);
    $title = htmlspecialchars($evt[event_title]);
    $dis_waiting .= "<tr>
      <td><a href='$url' target='_blank'>$title</a></td>
      <td>$evt[entity_label]</td>
      <td>".$evt_begin->getOutputDateTime()."</td>
      <td>".$evt_end->getOutputDateTime()."</td>
    </tr>"; 
  }
  $block="
   <table class=\"miniCalendar\" >
   <thead>
   <tr>
     <td>$GLOBALS[l_title]</td>
     <td>$GLOBALS[l_owner]</td>
     <td>$GLOBALS[l_datebegin]</td>
     <td>$GLOBALS[l_dateend]</td>
    </tr>
   </thead>
   <tbody>
   $dis_waiting
   </tbody>
   </table>";

  return $block;
}


/**
 * 
 * Portlet: Tasks
 * 
 */
function dis_calendar_task_portlet() {
  $data = run_query_get_todos();
  while ($data->next_record()) {
    $id = $data->f('event_id');
    $title = htmlspecialchars($data->f('event_title'));
    $priority = $data->f('event_priority');
    $percent = $data->f('eventlink_percent');
    $deadline = new Of_Date($data->f('event_deadline'));
    $url = $GLOBALS['path']."/todo/todo_index.php?action=detailconsult&amp;todo_id=$id";

    $now = new Of_Date();
    $now->setHour(0)->setMinute(0)->setSecond(0);
    $late = "";
    $status = $deadline->compare($now);
    if ($status == -1) {
      $style = "color:red;font-weight:bold;";
    } else if ($status == 0) {
      $style = "color:orange;";
    } else {
      $style = "color:green;";
    }

    $dis_task .= "<tr>
      <td><a href='$url' target='_blank'>$title</a></td>
      <td>$priority</td>
      <td>$percent %</td>
      <td style='$style'>".$deadline->getOutputDate()."$evt[entity_label]</td>
    </tr>"; 
  }
  $block="
   <table class=\"miniCalendar\" >
   <thead>
   <tr>
     <td>$GLOBALS[l_title]</td>
     <td>$GLOBALS[l_priority]</td>
     <td>$GLOBALS[l_header_advance]</td>
     <td>$GLOBALS[l_date]</td>
    </tr>
   </thead>
   <tbody>
   $dis_task
   </tbody>
   </table>";
  return $block;

}


/**
 * 
 * Portlet: Nav bar
 * 
 */
function dis_portlet_navbar($calendar, $current_view) {
 
  $range = $current_view->get_cal_range();
  $current = clone $current_view->get_date();
  $date = clone $current_view->get_date();

  switch($range) {
    case 'day':
      $url_prev = "calendar_index.php?cal_range=day&amp;list=1&amp;date=".$current->setDateIso($date)->subDay(1)->getURL();
      $url_next = "calendar_index.php?cal_range=day&amp;list=1&amp;date=".$current->setDateIso($date)->addDay(1)->getURL();
      $label = $date->localize(Of_Date::OBM_DATE_DAY);
      $day = "tabSelected";
      $obm_url = "$GLOBALS[path]/calendar/calendar_index.php?cal_range=day&amp;cal_view=list";
      break;
    case 'week':
      $url_prev = "calendar_index.php?cal_range=week&amp;date=".$current->setDateIso($date)->subWeek(1)->getURL();
      $url_next = "calendar_index.php?cal_range=week&amp;date=".$current->setDateIso($date)->addWeek(1)->getURL();
      $date->setWeekday($GLOBALS['ccalendar_weekstart'])->setHour(0)->setMinute(0)->setSecond(0);
      $end = clone $date;
      $end->addDay(7);
      if ($date->compareMonth($end) == 0) {
        $label_begin = $date->localize(Of_Date::DAY_SHORT);
        $label_end = $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK_JUMP);
      } else {
        $label_begin = $date->localize(Of_Date::OBM_DATE_WEEK_JUMP);
        $label_end = $end->subDay(1)->localize(Of_Date::OBM_DATE_WEEK_JUMP);
      }
      if ($date->compareYear($end) == 0) {
        $label_end .= " ".$date->localize(Of_Date::YEAR);
      } else {
        $label_begin .= " ".$date->localize(Of_Date::YEAR);
        $label_end .= " ".$end->localize(Of_Date::YEAR);
      }
      $label = "$label_begin - $label_end";
      $week = "tabSelected";
      $obm_url = "$GLOBALS[path]/calendar/calendar_index.php?cal_range=week";
      break;
    case 'month':
      $url_prev = "calendar_index.php?cal_range=month&amp;date=".$current->setDateIso($date)->subMonth(1)->getURL();
      $url_next = "calendar_index.php?cal_range=month&amp;date=".$current->setDateIso($date)->addMonth(1)->getURL();
      $label = $date->localize(Of_Date::OBM_DATE_MONTH);
      $month = "tabSelected";
      $obm_url = "$GLOBALS[path]/calendar/calendar_index.php?cal_range=month";
      break;
    default:
      break;
  }
  $portlet_action = "&amp;action=portlet";
  $nav = "<div id='calendarNavBar'>
    <a href='$url_prev$portlet_action'><input type='button' value='«' /></a>     
    <a href='$url_next$portlet_action'><input type='button' value='»' /></a>     
    <span style='font-size:90%;'>$label</span>
  </div>";

  if (isset($calendar['waiting'])) {
    $obm_url = "$GLOBALS[path]/calendar/calendar_index.php?action=waiting_events";
    $nav = "";
    $day = "";
    $week = "";
    $month = "";
    $waiting = "tabSelected";
  }
  if (isset($calendar['task'])) {
    $obm_url = "$GLOBALS[path]/todo/todo_index.php";
    $nav = "";
    $day = "";
    $week = "";
    $month = "";
    $waiting = "";
    $task = "tabSelected";
  }

  $block = "
  <div class='tab'>
    <div style='float:right;padding-right:10px;'><a target='_blank' href='$obm_url'>»OBM</a></div>
    <label id='lbl_cb_day' class='tabLabel $day'><a href='calendar_index.php?action=portlet&amp;list=1&cal_range=day&cal_view=list'>$GLOBALS[l_day]</a></label>
    <label id='lbl_cb_week' class='tabLabel $week'><a href='calendar_index.php?action=portlet&amp;cal_range=week&cal_view=agenda'>$GLOBALS[l_week]</a></label>
    <label id='lbl_cb_month' class='tabLabel $month'><a href='calendar_index.php?action=portlet&amp;cal_range=month&cal_view=agenda'>$GLOBALS[l_month]</a></label>
    <label id='lbl_cb_waiting' class='tabLabel $waiting'><a href='calendar_index.php?action=portlet&amp;waiting=1'>$GLOBALS[l_waiting_events]</a></label>
    <label id='lbl_cb_task' class='tabLabel $task'><a href='calendar_index.php?action=portlet&amp;task=1'>$GLOBALS[l_task]</a></label>
  </div>
  $nav";

  return $block;
}


/*
 * Back to calendar link
 */
function dis_back_to_calendar($action) {
  if ($action != 'pdf_export_form') {
    $block = "
    <div id='backToCalendar'>
      <a class='B' href='$GLOBALS[path]/calendar/calendar_index.php'>$GLOBALS[l_back_to_calendar]</a>
    </div>";
    return $block;
  }
}

/**
 *
 */
function dis_calendar_share_public($token, $email) {
  global $display;
  $display['json'] = "token :'".phpStringToJsString($token)."',email :'".phpStringToJsString($email)."'";
}
?>
