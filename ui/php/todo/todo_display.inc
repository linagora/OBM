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
// OBM - File : todo_display.inc                                             //
//     - Desc : Todo Display File                                            //
// 2003-09-15 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
$fieldnames['event_date'] = $l_date;
$fieldnames['date_create'] = $l_date_create;
$fieldnames['event_priority'] = $l_priority;
$fieldnames['eventlink_percent'] = $l_percent;
$fieldnames['event_title'] = $l_title;
$fieldnames['event_update'] = $l_update;


///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_todo(&$OD, $fieldname, $link_ok) {
  global $path, $ico_update;

  if ($fieldname == 'date_create') {
    $datecreate = $OD->data_set->f('date_create');
    $res['name'] = ($datecreate) ? of_date_format($datecreate) : ' ';
    $res['align'] = 'center';
  }

  else if ($fieldname == 'event_date') {
    $date = $OD->data_set->f('event_date');
    $res['name'] = ($date) ? of_date_format($date) : ' ';
    $res['align'] = 'center';
  }

  else if ($fieldname == 'event_title') {
    $t_id = $OD->data_set->f('event_id');
    $res['url'] = "todo_index.php?action=detailconsult&amp;todo_id=$t_id";
  }

  else if ($fieldname == 'event_priority') {
    $res['align'] = 'center';
  }

  else if ($fieldname == 'eventlink_percent') {
    $res['align'] = 'center';
    $percent = $OD->data_set->f('eventlink_percent');
    if ($percent != '') {
      $res['name'] = "$percent %";
    }
  }

  else if ($fieldname == 'event_update') {
    $t_id = $OD->data_set->f('event_id');
    $res['popup'] = true;
    $res['popup_width'] = 700;
    $res['popup_height'] = 260;
    $res['url'] = "todo_index.php?action=detailupdate&amp;todo_id=$t_id&amp;popup=1";
    $res['name'] = "<img src=\"$ico_update\" alt=\"\" />";
    $res['txt_name'] = ' ';
    $res['align'] = 'center';
  }

  return $res;
}


/**
 * Display Todo search form
 *
 * @param $params[] : hash with search values
 **/
function dis_todo_search_form($params='') {
  global $cgp_hide;

  $block .= html_todo_search_form($params);

  return $block;
}


/**
 * Display: XHTML Todo search Form
 *
 * @param $prams[] : default form values
 **/
function html_todo_search_form($params) {
  global $action, $max_priority, $l_find, $popup, $display, $c_all, $l_all;
  global $l_date, $l_date_create;;
  global $l_title, $l_percent, $l_priority, $l_text, $l_after, $l_before;

  $text = stripslashes($params['text']);
  $priority = $params['priority'];
  $percent = $params['percent'];
  $date_field = ($params['date_field'] ? $params['date_field'] : 'date');
  $dateafter = of_date_upd_format(stripslashes($params['date_after']),true);
  $datebefore = of_date_upd_format(stripslashes($params['date_before']),true);
  $archive = ($params['archive'] == '1' ? "checked = \"checked\"" : '');

  // Selected date field
  $s_date_field[$date_field] = "selected=\"selected\"";

  // Priority select
  $sel_priority = "<select id=\"sel_priority\" name=\"sel_priority\">
    <option value=\"$c_all\">$l_all</option>";
  for ($i=1; $i<=$max_priority; $i++) {
    $selected = ($i == $priority) ? "selected=\"selected\"" : '';
    $sel_priority .= "<option value=\"$i\" $selected>$i</option>";
  }
  $sel_priority .= '</select>';
  
  if ($popup) {
    $ext_action = $params['ext_action'];
    $ext_type = $params['ext_type'];
    $ext_url = $params['ext_url'];
    $ext_id = $params['ext_id'];
    $ext_title = $params['ext_title'];
    $ext_target = $params['ext_target'];
    $ext_widget = $params['ext_widget'];
    $ext_widget_text = $params['ext_widget_text'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_type\" type=\"hidden\" value=\"$ext_type\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display['title'] = $ext_title;
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" 
  action=\"todo_index.php\">
    <label>$l_text<br />
      <input type=\"text\" name=\"tf_text\" size=\"16\" value=\"$text\" />
    </label>
    <label>$l_priority<br />
      $sel_priority
    </label>
    <label>$l_percent<br />
      <input type=\"text\" name=\"tf_percent\" size=\"12\" value=\"$percent\" maxlength=\"2\"/>
    </label>
    <label>$l_date<br />
      <select name=\"sel_date_field\">
        <option value=\"date\" $s_date_field[date]>$l_date</option>
        <option value=\"date_create\" $s_date_field[date_create]>$l_date_create</option>
      </select>
    </label>
    <label>$l_after<br />
      <input type=\"text\" name=\"tf_date_after\" class=\"datePicker\" value=\"$dateafter\" />
    </label>
    <label>$l_before<br />
      <input type=\"text\" name=\"tf_date_before\" class=\"datePicker\" value=\"$datebefore\" />
    </label>
<!--    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>-->
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext &nbsp;
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Todo search result
// Parameters:
//   - $todo[]    : todo search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_todo_search_list($todo) {
  global $display, $obm, $l_found, $l_no_found;

  $todo_q = run_query_todo_search($todo);
  $nb_todo = $todo_q->num_rows_total();
  if ($nb_todo == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $prefs = get_display_pref($obm['uid'], 'todo', 0);
    $display['msg'] .= display_info_msg("$nb_todo $l_found");
    $display['result'] .= html_todo_search_list($todo, $todo_q, $prefs);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Full list
// Parameters:
//   - $todo[] : hash values
//   - $todo_q : todo list
//   - $prefs  : fields to display in the todo list
///////////////////////////////////////////////////////////////////////////////
function html_todo_search_list($todo, $todo_q, $prefs) {
  global $obm, $l_del, $l_delete;

  $uid = $obm['uid'];
  $text = stripslashes($todo['text']);
  $users = $todo['user_id'];
  $user = $users[0];
  $priority = $todo['priority'];
  $percent = $todo['percent'];
  $date_field = $todo['date_field'];
  $date_after = $todo['date_after'];
  $date_before = $todo['date_before'];

  $url = url_prepare("todo_index.php?action=search&amp;tf_text=$text&amp;tf_user=$user&amp;sel_priority=$priority&amp;tf_percent=$percent&amp;date_field=$date_field&amp;date_after=$date_after&amp;date_before=$date_before");

  $todo_d = new OBM_DISPLAY('DATA', $prefs, 'todo');
  $todo_d->data_set = $todo_q;
  $todo_d->data_url = $url;
  $todo_d->data_idfield = 'event_id';
  $todo_d->data_cb_text = "$l_del";
  $todo_d->data_cb_side = 'left';
  $todo_d->data_cb_name = 'delete_';
  $todo_d->data_cb_show = 1;
  $todo_d->data_header = 'both';
  $todo_d->data_form_head = "
     <form method=\"post\" name=\"f_todo_del\" action=\"".url_prepare('todo_index.php').
    "\" onsubmit=\"if (confirm_del()) return true; else return false\" >";

  $todo_d->data_form_end = "
  <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_delete\" />
  </fieldset>
  </form>";

  $block .= $todo_d->display('dis_data_todo');

  return $block;
}


/**
 * Display Todo detail
 *
 * @param $params[] : todo values (id)
 **/
function dis_todo_consult($params) {
  global $display, $l_no_found, $path, $l_err_reference;

  $id = $params['todo_id'];
  $todo_q = run_query_todo_detail($params);
  if ($todo_q->num_rows() == 0) {
    $display['msg'] .= display_err_msg($l_err_reference);
  } else {
    $users = get_todo_users($id);
    $display['detailInfo'] = display_record_info($todo_q);
    $block .= html_todo_consult($todo_q, $users);
  }
  return $block;
}


/**
 * HTML Display Todo detail
 *
 * @param $todo_q : todo database values
 * @param $users : users array
 **/
function html_todo_consult($todo_q, $users) {
  global $display;
  global $l_todo, $l_todotitle, $l_user, $l_author, $l_priority, $l_date_create;
  global $l_date, $l_desc, $l_percent, $l_title;

  $title = $todo_q->f('event_title');
  $author = $todo_q->f('author_firstname') .' '. $todo_q->f('author_lastname');
  $priority = $todo_q->f('event_priority');

  $date = of_date_format($todo_q->f('event_date'));
  $datecreate = of_date_format($todo_q->f('timecreate'));
  //  $date = ($date != 0) ? of_date_format($date) : $l_no_date;
  $description = nl2br($todo_q->f('event_description'));

  foreach ($users as $u_id => $one_user) {
    $percent = '';
    if ($one_user['percent'] > 0) {
      $percent = "($one_user[percent] %)";
    }
    $dis_user .= "<tr>
      <th>$l_user</th>
      <td>$one_user[firstname] $one_user[lastname] $percent</td>
    </tr>";
  }

  $display['title'] = $title;

  $block = "
  <div class=\"detail infos\">
    <h1>$l_todo</h1>
    <table>
    <tr>
      <th>$l_title</th>
      <td>$title</td>
    </tr>
    <tr>
      <th>$l_author</th>
      <td>$author</td>
    </tr>
    <tr>
      <th>$l_priority</th>
      <td>$priority</td>
    </tr>
    <tr>
      <th>$l_percent</th>
      <td>$percent</td>
    </tr>
    <tr>
      <th>$l_date_create</th>
      <td>$datecreate</td>
    </tr>
    <tr>
      <th>$l_date</th>
      <td>$date</td>
    </tr>
    </table>
  </div>

  <div class=\"detail infos\">
    <h1>$l_user</h1>
    <table>
    $dis_user
    </table>
   </div>
  
  <div class=\"detail extra\">
    <h1>$l_desc</h1> 
      $description
  </div>
";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Form
// Parameters:
//   - $todo[] : default values
//   - $todo_q : default values
//   - $users : users array
///////////////////////////////////////////////////////////////////////////////
function dis_todo_form($todo, $todo_q = 0) {
  global $path, $obm, $max_priority;
  global $ico_mini_cal, $ico_add, $ico_delete;
  global $l_add, $l_update, $l_close,$display,$l_todo,$l_user;
  global $l_date, $l_priority, $l_title, $l_percent, $l_desc;

  $action = $todo['action'];
  $popup = $todo['popup'];
 
  if ($action == 'detailupdate') {
    $t_id = $todo['todo_id'];
    $users = get_todo_users($t_id);
    $title = htmlentities($todo_q->f('event_title'), ENT_COMPAT, 'UTF-8');
    $description = htmlentities($todo_q->f('event_description'), ENT_COMPAT, 'UTF-8');
    $priority = $todo_q->f('event_priority');
    $percent = $todo_q->f('eventlink_percent');
    $date = $todo_q->f('event_date');
    $date = ($date != 0) ? of_date_upd_format($date, true) : $l_no_date;
  } else {
    $u_id = $obm['uid'];
    $priority = intval(($max_priority+1)/2);
    $date = of_date_upd_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($todo['todo_id'])) { $t_id = $todo['todo_id']; }
  if (isset($todo['title'])) { $title = $todo['title']; }
  if (isset($todo['priority'])) { $priority = $todo['priority']; }
  if (isset($todo['percent'])) { $percent = $todo['percent']; }
  if (isset($todo['date'])) { $date = of_date_upd_format($todo['date'],true); };
  if (isset($todo['description'])) { $description = $todo['description']; }
  if (is_array($todo['user_ids'])) {
    $p_user_array = $todo['user_ids'];
  } else if (is_array($users) && (count($users)>0)) {
    $p_user_array = array_keys($users);
  } else {
    $p_user_array = array($obm['uid']);
  }

  $users = get_userobm_from_ids($p_user_array);
  // user select
  if (is_array($users['entity'])) {
    foreach ($users['entity'] as $u_id => $u_ent) {
      $u_name = $u_ent['label'];
      $sel_id = "data-user-$u_id";
      $div_id = "sel_user_id-$sel_id";
      $dis_sel_user .= "<div class=\"elementRow\" id=\"$div_id\">
      <a href=\"javascript: remove_element('$div_id','sel_user_id');\">
      <img src=\"$ico_delete\" alt=\"[delete]\" />
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }

  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id";
   
  $block_user = "
  <tr>
    <th>
      $l_users
      <a href=\"javascript: return false;\" 
      onclick=\"popup('$url',''); return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>
    </th>    
    <td id=\"sel_user_id\">
      $dis_sel_user
    </td></tr>";

  $display['title'] = $title;

  // Priority select
  $sel_priority = "<select id=\"sel_priority\" name=\"sel_priority\">";
  for ($i=1; $i<=$max_priority; $i++) {
    $selected = ($i == $priority) ? "selected=\"selected\"" : '';
    $sel_priority .= "<option value=\"$i\" $selected>$i</option>";
  }
  $sel_priority .= '</select>';

  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button = "
      <input type=\"hidden\" id=\"todo_id\" name=\"todo_id\" value=\"$t_id\" />
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" id=\"popup\" name=\"popup\" value=\"$popup\" />
      <input type=\"submit\" value=\"$l_update\" />";
  } else {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_add\" />";
  }

  if ($popup)
    $dis_button .=  "<input type=\"submit\" value=\"$l_close\" onclick=\"window.close();\" />";

  $block = "
 
  <form method=\"post\" name=\"f_entity\"
    onsubmit=\"if (check_addtodoform(this)) return true; else return false;\"
    action=\"".url_prepare('todo_index.php')."\">
 
 <fieldset class=\"detail infos\">
 <legend>$l_todo</legend>
  <table>
  <tr>
    <th>$l_title</th>
    <td><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"100\" size=\"100\" value=\"$title\" /></td>
  </tr><tr>
    <th>$l_priority</th>
    <td>$sel_priority</td>
  </tr><tr>
    <th>$l_percent</th>
    <td><input type=\"text\" id=\"tf_percent\" name=\"tf_percent\" maxlength=\"3\" size=\"3\" value=\"$percent\" /></td>
  </tr><tr>
    <th>$l_date</th>
    <td><input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/></td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_user</legend>
  <table>
  $block_user
  </table>
  </fieldset>

  <fieldset class=\"detail extra\">
  <legend>$l_desc</legend>
    <textarea id=\"ta_description\" name=\"ta_description\" rows=\"10\" cols=\"80\">$description</textarea>
  </fieldset>
  
  <fieldset class=\"buttons\">
    $dis_button
  </fieldset>
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Todo display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_todo_display_pref($prefs) {
  global $l_todo_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'todo');
  $dis_pref->pref_title = $l_todo_display;

  $block .= '<td>';
  $block .= $dis_pref->display();
  $block .= '</td></tr></table>';

  $block .= $dis_pref->dis_pref_help();
  return $block;
}

?>
