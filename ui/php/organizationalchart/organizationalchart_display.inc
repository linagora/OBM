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
// OBM - File : organizationalchart_display.inc                              //
//     - Desc : Organizational Chart Display File                            //
// 2007-02-26 David PHAN                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id: organizationalchart_display.inc,v 1.198 2007/02/20 15:16:10 mehdi Exp $ //
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames['organizationalchart_name'] = $l_name;
$fieldnames['organizationalchart_description'] = $l_desc;
$fieldnames['organizationalchart_archive'] = $l_archive;


/**
 * Display Organizational Chart specific dataset fields
 * @param $OD OBM_DISPLAY object (passed by reference)
 * @param $fieldname field to display
 * @param $link_ok true if links must be displayed
 * @access public
 * @return $res hash with 'name', 'url' values
 */
function dis_data_organizationalchart(&$OD, $fieldname, $link_ok) {

  if ($fieldname == 'organizationalchart_name') {
    $res['url'] = "$GLOBALS[path]/organizationalchart/organizationalchart_index.php";
    $res['url'] .= "?action=detailconsult&amp;organizationalchart_id=".$OD->data_set->f('organizationalchart_id');
  }  else if ($fieldname == 'organizationalchart_archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = "&nbsp;";
      $res['txt_name'] = ' ';
    }
  }
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Organizational Chart search form
// Parameters:
//   - $organizationalchart[] : hash with company values
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_search_form($organizationalchart) {

  $block = html_organizationalchart_search_form($organizationalchart);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Organizational Chart search Form
// Parameters:
//   - $organizationalchart[] : default form values
///////////////////////////////////////////////////////////////////////////////
function html_organizationalchart_search_form($organizationalchart) {
  global $action, $display;
  global $l_find, $l_name, $l_user, $l_desc, $l_archive;

  $popup = $organizationalchart['popup'];
  $name = stripslashes($organizationalchart['name']);
  $user = stripslashes($organizationalchart['user']);
  $desc = stripslashes($organizationalchart['desc']);
  $archive = ($organizationalchart['archive'] == '1' ? "checked = \"checked\"" : '');

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" action=\"$url\">
    <label>$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"24\"  value=\"$name\" />
    </label>
    <label>$l_user<br />
      <input type=\"text\" name=\"tf_user\" size=\"24\"  value=\"$user\" />
    </label>
    <label>$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"24\"  value=\"$desc\" />
    </label>
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
} 


///////////////////////////////////////////////////////////////////////////////
// Display the Organizational Chart search result
// Parameters:
//   - $organizationalchart[] : company search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_search_list($organizationalchart) {
  global $display, $l_found, $l_no_found, $obm;

  $popup = $organizationalchart['popup'];
  $prefs = get_display_pref($obm['uid'], 'organizationalchart');
  $obm_q = run_query_organizationalchart_search($organizationalchart);
  $nb_organizationalchart = $obm_q->num_rows_total();
  if ($nb_organizationalchart == 0) {
    $display['msg'] = display_warn_msg($l_no_found);
  } else {
    $display['msg'] = display_info_msg("$nb_organizationalchart $l_found");
    $block = html_organizationalchart_search_list($obm_q, $prefs, $organizationalchart, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $organizationalchart_q    : list of organizational chart
//   - $prefs     : Display preferences
//   - $organizationalchart[] : company search criteria
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_organizationalchart_search_list($organizationalchart_q, $prefs, $organizationalchart, $popup) {

  if ($popup) {
    $ext_action = $organizationalchart['ext_action'];
    $ext_url = urlencode($organizationalchart['ext_url']);
    $ext_id = $organizationalchart['ext_id'];
    $ext_target = $organizationalchart['ext_target'];
    $ext_widget = $organizationalchart['ext_widget'];
    $ext_widget_text = $organizationalchart['ext_widget_text'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  // we urlencode to avoid breakage for space char
  $name = urlencode(stripslashes($organizationalchart['name']));
  $archive = $organizationalchart['archive'];
  $user = stripslashes($organizationalchart['user']);

  $url = url_prepare("organizationalchart_index.php?action=search&amp;tf_name=$name&amp;tf_user=$user&amp;cb_archive=$archive$url_userdata$url_ext");
  $orgchart_d = new OBM_DISPLAY('DATA', $prefs, 'organizationalchart');
  if ($popup) {
    $orgchart_d->display_link = false;
    if ($ext_url != '') {
      $orgchart_d->display_ext = 'get_id_url';
    } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $orgchart_d->display_ext = 'get_id';
    }
    $display_popup_end = "<a href=\"\" onclick='window.close();'>$l_close</a>";
  }
  
  $orgchart_d->data_set = $organizationalchart_q;
  $orgchart_d->data_url = $url;
  $orgchart_d->data_header = 'both';
  $block = $orgchart_d->display('dis_data_organizationalchart');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Organizational Chart Form
// Parameters:
//   - $action    : action called
//   - $organizationalchart[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_organizationalchart_form($action, $organizationalchart) {
  global $display, $obm, $l_err_reference;

  $id = $organizationalchart['organizationalchart_id'];
  if ($id > 0) {
    $oc_q = run_query_organizationalchart_detail($id);
    $root_node = run_query_organizationalchart_root_node($id);
    if ($oc_q->num_rows() == 1) {
      $display['detailInfo'] = display_record_info($oc_q);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  }
  
  $block = html_organizationalchart_form($action, $oc_q, $organizationalchart, $root_node);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Organizational Chart Form
// Parameters:
//   - $action    : action called
//   - $organizationalchart : default values
///////////////////////////////////////////////////////////////////////////////
function html_organizationalchart_form($action, $oc_q, $organizationalchart, $root_node) {
  global $obm, $display;
  global $ico_add, $ico_delete, $ico_group, $ico_user;
  global $l_organizationalchart, $l_desc, $l_archive, $l_update, $l_insert, $l_title;
  global $l_name, $l_detail, $l_header_new, $l_add_child_tooltip;

  if ($action == 'detailupdate' || $action == 'update') {
    $id = $oc_q->f('organizationalchart_id');
    $name = $oc_q->f('organizationalchart_name');
    $desc = $oc_q->f('organizationalchart_description');
    $archive = $oc_q->f('organizationalchart_archive') == '1' ? 'checked' : '';
    $nb_child = ($oc_q->f('node') + 1);
    $dis_title = $name;
    $block_detail = dis_organizationalchart_draw_form($root_node);
    $block_detail .= "<input type=\"hidden\" id=\"nb_child\" name=\"nb_child\" value=\"$nb_child\" />
      <input type=\"hidden\" id=\"organizationalchart_id\" name=\"organizationalchart_id\" value=\"$id\" />";
  } else {
    $block_detail = "
      <div id=\"1\" class=\"organizationalChartNode\">
        <div class=\"organizationalChartWrap\">
          <input type=\"text\" class=\"mandatory\" id=\"tf_ogroup[1]\" name=\"tf_ogroup[1]\" maxlength=\"32\" size=\"18\" value=\"$ogroup\" />
          <input type=\"hidden\" id=\"tf_parent_level[1]\" name=\"tf_parent_level[1]\" value=\"0\" />
          <div><img src=\"$ico_user\" /><input type=\"text\" name=\"\" value=\"\" id=\"user1\" autocomplete=\"off\" /></div>
          <div><img src=\"$ico_group\" /><input type=\"text\" name=\"\" value=\"\" id=\"group1\" autocomplete=\"off\" /></div>
          <input type=\"hidden\" name=\"nb_child_1\" id=\"nb_child_1\" value=\"0\" />
          <div id=\"sel_ent1\">
            <!-- Entities -->
          </div>
          <div class=\"newButtons\"> 
            <a href=\"javascript: add_entity('1');\"><img src=\"$ico_add\" alt=\"[Add]\" title=\"$l_add_child_tooltip\" /></a>
          </div>
        </div>
      <div id=\"child_1\"></div>
      </div>
      <script type=\"text/javascript\">
        obm.initialize.chain(function () {
          new obm.AutoComplete.Search('$GLOBALS[path]/user/user_index.php?action=ext_search&ajax=1', 'sel_ent1', 'user1', {defaultText:'$user_text'})
          new obm.AutoComplete.Search('$GLOBALS[path]/group/group_index.php?action=ext_search&ajax=1', 'sel_ent1', 'group1', {defaultText:'$user_text'})
        });
      </script>";
  }

  // If parameters have been given, they supercede the default action value
  if (isset($organizationalchart['oc_name'])) { $name = stripslashes($organizationalchart['oc_name']); }
  if (isset($organizationalchart['desc'])) { $desc = stripslashes($organizationalchart['desc']); }
  if (isset($organizationalchart['archive'])) { $archive = ($organizationalchart['archive'] == 1 ? 'checked' : ''); }

  // Buttons
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button = "
    <!-- Update button -->
    <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
    <input type=\"submit\" value=\"$l_update\" />";
  } elseif (($action == 'new') || ($action == 'insert')) {
    $dis_button = "
    <!-- Insert button -->
    <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
    <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display['title'] = $dis_title;

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"post\" name=\"f_entity\"
    onsubmit=\"if (check_form(this)) return true; else return false;\"
    action=\"".url_prepare('organizationalchart_index.php')."\">
    <fieldset class=\"detail extra\">  
      <legend>$l_organizationalchart</legend>
      <table>
        <tr>
          <th>$l_name</th>
	  <td><input type=\"text\" id=\"tf_oc_name\" name=\"tf_oc_name\" maxlength=\"32\" size=\"50\" value=\"$name\" /></td>
        </tr><tr>
          <th>$l_desc</th>
          <td><input type=\"text\" id=\"tf_desc\" name=\"tf_desc\" maxlength=\"255\" size=\"50\" value=\"$desc\" /></td>
        </tr><tr>
          <th><label for=\"cba_archive\">$l_archive</label></th>
          <td><input type=\"checkbox\" id=\"cba_archive\" name=\"cba_archive\" value=\"1\" $archive /></td>
        </tr>
      </table>
    </fieldset>
    <!-- Organizational Chart -->
    <fieldset class=\"detail extra\">  
      <legend>$l_detail</legend>
        $block_detail
    </fieldset>
    <fieldset class=\"buttons\">$dis_button</fieldset>
  </form>";

  return $block;
}


/**
 * Display Organizational Chart Detail
 * @param $organizationalchart 
 * @access public
 * @return HTML
 */
function dis_organizationalchart_consult($organizationalchart, $view) {
  global $display, $action;

  $id = $organizationalchart['organizationalchart_id'];
  if ($id) {
    $display['print_url'] = "$GLOBALS[path]/organizationalchart/organizationalchart_index.php?action= $action&amp;organizationalchart_id=$id";
    $oc_q = run_query_organizationalchart_detail($id);
    $root_node = run_query_organizationalchart_root_node($id);
    $block = html_organizationalchart_consult($organizationalchart, $view, $oc_q, $root_node); 
  }

  return $block;
}


/**
 * XHTML Display Organizational Chart Detail
 * @param $organizationalchart 
 * @param $oc_q ogroup database result
 * @param $root_node database result
 * @access public
 * @return HTML
 */
function html_organizationalchart_consult($organizationalchart, $view, $oc_q, $root_node) {
  global $display;
  global $l_organizationalchart, $l_detail, $l_zoom_big, $l_zoom_small, $l_zoom;
  global $l_name, $l_desc, $l_archive, $l_yes, $l_no,  $l_outline, $action;
  global $ico_organizationalchart_view, $ico_organizationalchart_view_current;
  global $ico_outline_view, $ico_outline_view_current;

  $oc_name = $oc_q->f('organizationalchart_name');
  $oc_id = $oc_q->f('organizationalchart_id');
  $desc = $oc_q->f('organizationalchart_description');
  $archive = $oc_q->f('organizationalchart_archive') == 1 ? $l_yes : $l_no;

  // Params
  $output = $organizationalchart['output_target'];

  $ico_organizationnalchart = $ico_organizationalchart_view;
  $ico_outline = $ico_outline_view;

  if ($view == 'chart') {
    // Zoom
    $dis_zoom = "<div class=\"LF MN\" style=\"height: 24px; margin-left: 10px;\">
    <form action=\"\" method=\"\" onsubmit=\"return false;\">
      $l_zoom : <select onchange=\"zoom(this.options[this.selectedIndex].value);\">
        <option value=\"big\">$l_zoom_big</option>
        <option value=\"small\">$l_zoom_small</option>
      </select>
    </form></div>";
    $block_detail = dis_organizationalchart_consult_chart($root_node, $output);
    $block_popup = dis_organizationalchart_userdetail();
    $ico_organizationnalchart = $ico_organizationalchart_view_current;
  } elseif ($view == 'outline') {
    $block_detail = dis_organizationalchart_consult_outline($root_node, $output);
    $block_popup = '';
    $ico_outline = $ico_outline_view_current;
  }

  // View select
  $dis_view = "
    <div class=\"LF MN\" style=\"margin-left: 10px;\">
    <a href=\"$GLOBALS[path]/organizationalchart/organizationalchart_index.php?action=detailconsult&amp;organizationalchart_id=$oc_id&amp;view=chart\">
      <img title=\"$l_organizationalchart\" src=\"$ico_organizationnalchart\" alt=\"$l_organizationalchart\"/> 
    </a>
    <a href=\"$GLOBALS[path]/organizationalchart/organizationalchart_index.php?action=detailconsult&amp;organizationalchart_id=$oc_id&amp;view=outline\">
      <img title=\"$l_outline\" src=\"$ico_outline\" alt=\"$l_outline\"/> 
    </a>
    </div>";

  // Printable view
  if ($output == 'print') {
    $dis_zoom = '';
    $dis_view= '';
    $print = 'Printable';
  }

  $display['detailInfo'] = display_record_info($oc_q);
  $display['title'] = $oc_name;

  // --- HTML Template --------------------------------------------------------
  $block = "
  <div class=\"detail extra\">
    <h1>$l_organizationalchart</h1>
      <table>
        <tr>
          <th>$l_name</th>
          <td>$oc_name</td>
        </tr>
        <tr>
          <th>$l_desc</th>
          <td>$desc</td>
        </tr>
        <tr>
          <th>$l_archive</th>
          <td>$archive</td>
        </tr>
       </table>
    <h1>$l_detail</h1>
    $dis_view
    $dis_zoom
    <div id=\"container\" class=\"container$print\">
      $block_detail
    </div>
  </div>
  $block_popup";
 
  return $block;
}


/**
 * Display the organizational chart delete validation screen
 * @param $id organizational chart id 
 * @access public
 * @return HTML
 */
function dis_can_delete_organizationalchart($id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;
  
  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
  <form action=\"$GLOBALS[path]/organizationalchart/organizationalchart_index.php\" method=\"post\" onsubmit=\"return confirm_del()\">
  <input type=\"hidden\" name=\"action\" value=\"delete\" />
  <input type=\"hidden\" name=\"organizationalchart_id\" value=\"$id\" />
  <div class=\"buttons\">
  <input type=\"submit\" value=\"$l_delete\" />
  <a href=\"$GLOBALS[path]/organizationalchart/organizationalchart_index.php?action=detailconsult&amp;organizationalchart_id=$id\">$l_back</a>
  </div>
  </form>";

  return $block;
}


/**
 * Sort node detail by user lastname
 */
function sort_node_detail($a, $b) {
    return strcmp(strtolower($a['lastname']), strtolower($b['lastname']));
}


/**
 * Draw node detail 
 * @param $node_q
 * @access public
 * @return HTML
 */
function dis_organizationalchart_consult_chart($node_q, $output) {
  global $f, $l; 
  global $l_title, $l_phone, $l_mphone;
  global $ico_manager, $ico_nophoto;

  $node_name = $node_q->f('ogroup_name');
  $node_id = $node_q->f('ogroup_id');
  $parent_node_id = $node_q->f('ogroup_parent_id');
  $level = $node_q->f('ogroup_level');

  if (!isset($l)) {
    $l = array();
  }

  if (!isset($f)) {
    $f = array();
  }

  $wrap_width = 150;
  if ($output == 'print') {
    $wrap_width = 40;
    $print = 'Printable';
  }

  // Get node detail
  $users = run_query_organizationalchart_ogrouplink_detail($node_id);
  if (is_array($users)) {
    uasort($users, 'sort_node_detail');
    foreach ($users as $user_id => $one_user) {
      $userobm_url = "$GLOBALS[path]/people/people_index.php?action=detailconsult&amp;user_id=$user_id";
      $userobm_name = $one_user['lastname'].' '.$one_user['firstname'];
      $is_manager = $one_user['manager'];
      $item_id = "item-${level}-${user_id}";
      if ($output != 'print') {
        if (!$is_manager) {
           $block_detail_entities .= "<li id=\"item-${level}-${user_id}\">
             <a href=\"$userobm_url\" 
               onmouseover=\"obm.userDetail.compute($user_id, event, 'item-${level}-${user_id}');return false;\"
               onmouseout=\"obm.userDetail.compute($user_id, event, 'item-${level}-${user_id}');return false;\">$userobm_name</a>
             </li>";
        } else {
          $manager = "<img src=\"$ico_manager\" />";
          $block_detail_entities_boss .= "<li id=\"item-${level}-${user_id}\">
          <a href=\"$userobm_url\"
            onmouseover=\"obm.userDetail.compute($user_id, event, 'item-${level}-${user_id}');return false;\"
            onmouseout=\"obm.userDetail.compute($user_id, event, 'item-${level}-${user_id}');return false;\">$manager&nbsp;<b>$userobm_name</b></a>
          </li>";
        }
      } else {
        $block_detail_entities .= "<li>$userobm_name</li>";
      }
    }
  }
  // }
 
  // Get node children
  // TODO: Optimized
  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);
  $brother = run_query_organizationalchart_node_child($parent_node_id);
  $nb_brother = sizeof($brother) - 1;

  if ($nb_children) {
    $nb_leaves = run_query_organizationalchart_get_leaves($node_id, true);
    $wrap_width = $nb_leaves * $wrap_width;
    $node_queue = "<div class=\"nodeLnk\"></div>";
    foreach($t_child as $key => $child_id){
      if ($nb_children >= 2) {
        if ($key == 0){
          // first child
          $f[] = $child_id;
        } else if ($key == ($nb_children-1)){
          // last child
          $l[] = $child_id;
        }
      } else {
        $node_queue = '';
      }
      $child_q = run_query_organizationalchart_node_infos($child_id);
      $t_draw = dis_organizationalchart_consult_chart($child_q, $output);
      $block_detail_child .= $t_draw;
    }
  } 

  if ($parent_node_id) { // If not root node
    $style = "border-top: 1px solid #646567";
    $node_head = "<div class=\"nodeLnk\"></div>";
    if (in_array($node_id, $f)) {  // is first child ?
      $node_edge = "<div class=\"nodeStart$print\" style=\"width:".(ceil($wrap_width/2))."px;\"></div>";
    } else if (in_array($node_id, $l)) {  // is last child ?
      $node_edge = "<div class=\"nodeEnd$print\" style=\"width:".(ceil($wrap_width/2))."px;\"></div>";
    }
  } 

  // has brother? 
  if (!$nb_brother) {
    $style = '';
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <div id=\"node-wrap-$node_id\" class=\"nodeWrap\"
      style=\"width: ${wrap_width}px; $style\">
      $node_edge
      $node_head
      <div id=\"node-$node_id\" class=\"node$print\">
        <h1>$node_name</h1>
        <div class=\"nodeContent\">
          <ul>
            $block_detail_entities_boss
            $block_detail_entities
          </ul>
        </div>
      </div>
      $node_queue
      $block_detail_child
    </div>
  ";

  return $block;
}


/**
 * Draw node outline
 * @param $node_q
 * @access public
 * @return HTML
 */
function dis_organizationalchart_consult_outline($node_q) {
  global $ico_manager;

  $node_name = $node_q->f('ogroup_name');
  $node_id = $node_q->f('ogroup_id');
  $level = $node_q->f('ogroup_level');
  $flour = strlen($level) - strlen(str_replace('.', '', $level));  
  $color = 'color'.($flour)%7;

  // Get node detail
  $users = run_query_organizationalchart_ogrouplink_detail($node_id);
 
  // Users list
  if (is_array($users)) {
    usort($users, 'sort_node_detail');
    foreach ($users as $user_id => $one_user) {
      $userobm_name = $one_user['lastname'].' '.$one_user['firstname'];
      $userobm_title = empty($one_user['title'])? '': '('.$one_user['title'].')';
      $is_manager = $one_user['manager'];
      if ($is_manager) {
	$block_detail_entities_boss = "<li><img src=\"$ico_manager\"> <b>$userobm_name</b> $userobm_title</li>";
      } else {
	$block_detail_entities .= "<li><b>$userobm_name</b> $userobm_title</li>";
      }
    }
  }

  // Get node children 
  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);
  if ($nb_children > 0) {
    foreach($t_child as $key => $child_id){
      $child_q = run_query_organizationalchart_node_infos($child_id);
      $t_draw = dis_organizationalchart_consult_outline($child_q);
      $block_detail_child .= $t_draw;
    }
  } 

  $block = "
    <div id=\"$level\" class=\"organizationalChartNode\" style=\"margin-left: 3em;\">
      <div style=\"float:left;\" class=\"B\">:: $node_name</div>
      <div class=\"organizationalChartWrapOutline $color\">
        <ul>
        $block_detail_entities_boss
        $block_detail_entities
        <ul>
      </div>
      $block_detail_child
    </div>";

  return $block;
}


/**
 * Draw node form
 * @param $node_q
 * @access public
 * @return HTML
 */
function dis_organizationalchart_draw_form($node_q) {
  global $ico_delete, $ico_add, $ico_user, $ico_group, $ico_plus;
  global $l_add_child_tooltip, $l_name, $l_delete, $l_autocomplete_field;

  $node_name = $node_q->f('ogroup_name');
  $node_id = $node_q->f('ogroup_id');
  $parent_node_id = $node_q->f('ogroup_parent_id');
  $parent_level = $node_q->f('parent_level');
  $level = $node_q->f('ogroup_level');

  // Get node detail
  $ogroupentity_q = run_query_organizationalchart_ogrouplink_entities($node_id);
  $block_detail_entities = '';
  while($ogroupentity_q->next_record()) {
    $entity = $ogroupentity_q->f('entity');
    $entity_id = $ogroupentity_q->f('entity_id');
    $name = $ogroupentity_q->f('entity_name');
    $block_detail_entities .= "
      <div class=\"elementRow\" id=\"sel_ent${level}-data-${entity}-${entity_id}\">
        <a href=\"javascript: remove_element('sel_ent${level}-data-${entity}-${entity_id}','sel_ent${level}');\">
         <img src=\"$ico_delete\">
        </a>
        $name 
        <input value=\"data-${entity}-${entity_id}\" name=\"sel_ent${level}[]\" type=\"hidden\" />
      </div>";
  }

  // Get node children 
  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);
  if ($nb_children > 0) {
    $ico_toggle = "<a href=\"javascript: node_toggle('$level');\">
      <img id=\"img_$level\" src=\"$ico_plus\" style=\"float:right;\"/>
    </a>";
    foreach($t_child as $key => $child_id){
      $child_q = run_query_organizationalchart_node_infos($child_id);
      $t_draw = dis_organizationalchart_draw_form($child_q);
      $block_detail_child .= $t_draw;
    }
  }

  if ($level > 1) { 
    $ico_del_entity = "<a href='javascript: del_entity(\"$level\");'>
      <img id=\"trash_$level\" src=\"$ico_delete\" alt=\"[Delete]\" title=\"$l_delete\">
    </a>";
    $flour = strlen($level) - strlen(str_replace('.', '', $level));  
    $color = 'color'.($flour)%7;
  } else {
    $dis_js = "<script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$GLOBALS[path]/user/user_index.php?action=ext_search&ajax=1', 'sel_ent$level', 'user$level', {defaultText:'$user_text'});
      new obm.AutoComplete.Search('$GLOBALS[path]/group/group_index.php?action=ext_search&ajax=1', 'sel_ent$level', 'group$level', {defaultText:'$user_text'});
    });
    </script>";
  }

  $block = "
    <div id=\"$level\" class=\"organizationalChartNode\" style=\"margin-left: 3em;\">
      <div class=\"organizationalChartWrap $color\">
        $ico_toggle
        <input type=\"text\" class=\"mandatory\" id=\"tf_ogroup[$level]\" name=\"tf_ogroup[$level]\" 
          maxlength=\"32\" size=\"18\" value=\"$node_name\" />
        $ico_del_entity
        <input type=\"hidden\" name=\"nb_child_${level}\" id=\"nb_child_${level}\" value=\"$nb_children\" />
        <input type=\"hidden\" id=\"tf_parent_level[$level]\" name=\"tf_parent_level[$level]\" value=\"$parent_level\" />
        <div><img src=\"$ico_user\" /><input type=\"text\" name=\"\" value=\"\" id=\"user$level\" autocomplete=\"off\" class=\"input_user\" /></div>
        <div><img src=\"$ico_group\" /><input type=\"text\" name=\"\" value=\"\" id=\"group$level\" autocomplete=\"off\" /></div>
        <div id=\"sel_ent$level\">
          <!-- Entities -->
          $block_detail_entities
        </div>
        <div class=\"newButtons\"> 
          <a href=\"javascript: add_entity('$level');\"><img src=\"$ico_add\" alt=\"[Add]\" title=\"$l_add_child_tooltip\" /></a>
        </div>
      </div>
      <div id=\"child_$level\" style=\"display:none;\">
        $block_detail_child
      </div>
    </div>
    $dis_js";

  return $block;
}


/**
 * Export OrganizationalChart to LDIF
 * @params $organizationalchart
 * @access public
 * @return HTML
 */
function dis_organizationalchart_export_ldif($organizationalchart) {
  global $obm;

  $id = $organizationalchart['organizationalchart_id'];
  if ($id) {
    $oc_q = run_query_organizationalchart_detail($id);
    $oc_name = trim($oc_q->f('organizationalchart_name'));
    $root_node = run_query_organizationalchart_root_node($id);
    $ldif = export_ldif($root_node);

    // Ldif construction
    $block = trim($GLOBALS['organization']);
    if (is_array($GLOBALS['units'])) {
      foreach($GLOBALS['units'] as $u) {
        $block .= $u;
      }
    }
    if (is_array($GLOBALS['persons'])) {
      foreach($GLOBALS['persons'] as $p) {
        $block .= $p;
      }
    }

    // LDIF filename
    $filename = "$oc_name.ldif";
 
    // Write LDIF in tmp file
    $tmpfname = tempnam('/tmp', 'obm_') ;
    $file = fopen($tmpfname, 'w');
    fwrite($file, $block);
    fclose($file);

    // Download file
    $file = fopen($tmpfname, 'r');
    header('Content-Type: application/octet-stream') ;
    header("Content-Disposition: attachment; filename=\"$filename\";") ;
    header('Cache-Control: maxage=3600');
    header('Pragma: public');
    fpassthru($file) ;
    fclose($file) ;

    // Delete tmp file
    unlink($tmpfname) ; 
  }
}


/**
 * Export OrganizationalChart to SVG 
 * @params
 * @access public
 * @return XML 
 */
function organizationalchart_export_svg($organizationalchart, $view) {
  global $l_organizationalchart;

  $id = $organizationalchart['organizationalchart_id'];
  if ($id) {
    $mode = $organizationalchart['mode'];
    $oc_q = run_query_organizationalchart_detail($id);
    $oc_name = trim($oc_q->f('organizationalchart_name'));
    $root_node = run_query_organizationalchart_root_node($id);

    // Build XML
    $xml = new DOMDocument('1.0', 'iso8859-1');
    $svg = $xml->createElement('svg');
    $svg->setAttribute('xmlns', 'http://www.w3.org/2000/svg');

    if ($view == 'chart') {
      // gradient
      $linearGradient = $xml->createElement('linearGradient');
      $linearGradient->setAttribute('id', 'NodeTitleStyle');
      $linearGradient->setAttribute('x1', '0%');
      $linearGradient->setAttribute('y1', '0%');
      $linearGradient->setAttribute('x2', '0%');
      $linearGradient->setAttribute('y2', '100%');
      
      // top color
      $top = $xml->createElement('stop');
      $top->setAttribute('offset', '35%'); 
      $top->setAttribute('stop-color', '#509CBC'); 
      $linearGradient->appendChild($top);
 
      // bottom color
      $bottom = $xml->createElement('stop');
      $bottom->setAttribute('offset', '65%'); 
      $bottom->setAttribute('stop-color', '#378299'); 
      $linearGradient->appendChild($bottom);   

      $svg->appendChild($linearGradient);
    }
   
    // OrganizationalChart name
    $name = $xml->createElement('text');
    $name->setAttribute('style', 'font-size:22px;font-weight:bold;fill:#636466');
    $name->setAttribute('x', 10);
    $name->setAttribute('y', 30);
    $name->appendChild($xml->createTextNode("$l_organizationalchart : $oc_name"));
    $svg->appendChild($name);

    $function_export = "export_svg_${view}";
    $export = $function_export($root_node, $xml, $svg);

    $xml->appendChild($export);

    if ($mode == 'html') {
      // Display XML on browser
      header('Content-Type: text/xml');
      echo $xml->saveXML();
    } else {
      //// Download file
      header('Content-Type: image/svg+xml') ;
      header("Content-Disposition: attachment; filename=\"$oc_name.svg\";") ;
      header('Cache-Control: maxage=3600');
      header('Pragma: public');
      echo $xml->saveXML();
    }
  }
}


/**
 * Export OrganizationalChart to SVG 
 * @params
 * @access public
 * @return XML 
 */
function organizationalchart_export_pdf($organizationalchart, $view) {
  global $l_organizationalchart;

  $id = $organizationalchart['organizationalchart_id'];
  if ($id) {
    $mode = $organizationalchart['mode'];
    $oc_q = run_query_organizationalchart_detail($id);
    $oc_name = trim($oc_q->f('organizationalchart_name'));
    $root_node = run_query_organizationalchart_root_node($id);

    // Build XML
    $xml = new DOMDocument('1.0', 'iso8859-1');
    $svg = $xml->createElement('svg');
    $svg->setAttribute('xmlns', 'http://www.w3.org/2000/svg');

    if ($view == 'chart') {
      // gradient
      $linearGradient = $xml->createElement('linearGradient');
      $linearGradient->setAttribute('id', 'NodeTitleStyle');
      $linearGradient->setAttribute('x1', '0%');
      $linearGradient->setAttribute('y1', '0%');
      $linearGradient->setAttribute('x2', '0%');
      $linearGradient->setAttribute('y2', '100%');
      
      // top color
      $top = $xml->createElement('stop');
      $top->setAttribute('offset', '35%'); 
      $top->setAttribute('stop-color', '#509CBC'); 
      $linearGradient->appendChild($top);
 
      // bottom color
      $bottom = $xml->createElement('stop');
      $bottom->setAttribute('offset', '65%'); 
      $bottom->setAttribute('stop-color', '#378299'); 
      $linearGradient->appendChild($bottom);   

      $svg->appendChild($linearGradient);
    }
   
    // OrganizationalChart name
    $name = $xml->createElement('text');
    $name->setAttribute('style', 'font-size:22px;font-weight:bold;fill:#636466');
    $name->setAttribute('x', 10);
    $name->setAttribute('y', 30);
    $name->appendChild($xml->createTextNode("$l_organizationalchart : $oc_name"));
    $svg->appendChild($name);

    $function_export = "export_svg_${view}";
    $export = $function_export($root_node, $xml, $svg);

    $xml->appendChild($export);

    if ($mode == 'html') {
      // Display XML on browser
      header('Content-Type: text/xml');
      echo $xml->saveXML();

    } else {
      // Download file
      header('Content-Type: application/pdf') ;
      header("Content-Disposition: attachment; filename=\"$oc_name.pdf\";") ;
      header('Cache-Control: maxage=3600');
      header('Pragma: public');
      $pdf = new Imagick();
      $pdf->readImageBlob($xml->saveXML());
      $pdf->setImageFormat('pdf');
      echo $pdf->getImageBlob();
    }
  }
}
/**
 * User detail popup
 * @params
 * @access public
 * @return HTML
 */
function dis_organizationalchart_userdetail() {
  global $l_title, $l_phone, $l_mphone, $l_email;
  global $ico_nophoto, $ico_load; 

  $block = "
    <div id=\"userdetail\" class=\"userdetail\">
      <div id=\"userdetail_photo\" class=\"userphoto\">$block_photo</div>
      <div id=\"userdetail_name\" class=\"userdetailName\"></div>
      <table>
        <tr class=\"blockItem\">
          <th>$l_title</th>
          <td id=\"userdetail_title\"></td>
        </tr>
        <tr class=\"blockItem\">
          <th>$l_phone</th>
          <td id=\"userdetail_phone\"></td>
        </tr>
        <tr class=\"blockItem\">
          <th>$l_mphone</th>
          <td id=\"userdetail_mphone\"></td>
        </tr>
        <tr class=\"blockItem\">
          <th>$l_email</th>
          <td id=\"userdetail_email\"></td>
        </tr>
      </table>
    </div>
    <script type=\"text/javascript\">
      obm.userDetail = new Obm.UserDetail();
      obm.vars.path = '$GLOBALS[path]';
      obm.vars.images.nophoto = '$ico_nophoto';
      obm.vars.images.load = '$ico_load';
    </script>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Generate LDIF from OrganizationalChart 
///////////////////////////////////////////////////////////////////////////////
function export_ldif($node_q, $ou_arr=array()) {
  global $obm, $organization, $units, $persons;
  global $php_regexp_login;

  $eol = "\n";

  $ldif_attributes = array(
    'dn' => 'login',
    'uid' => 'login',
    'objectClass' => array('top', 'person', 'organizationalPerson', 'inetOrgPerson'),
    'sn' => 'lastname',
    'givenName' => 'firstname',
    'cn' => array('lastname', 'firstname'),
    'displayName' => array('firstname', 'lastname'),
    'title' => 'title',
    'telephoneNumber' => 'phone',
    'mobile' => 'mobile',
    'description' => 'description',
    'mail' => 'email',
  );

  $node_name = $node_q->f('ogroup_name');
  $node_id = $node_q->f('ogroup_id');
  $domain = $obm['domain_name'];
  if (empty($domain)) {
    $domain = get_last_domain_text($node_q->f('ogroup_domain_id'));
  } 

  array_unshift($ou_arr, $node_name);
  $ou = 'ou='.implode($ou_arr, ',ou=');
  $cn_arr = explode('.', $domain);
  $oc = "dc=$cn_arr[0],dc=$cn_arr[1]";

  if (!isset($units)) {
    $units = array();
  }
  if (!isset($persons)) {
    $persons = array();
  }

  // Header + Organization
  if (!isset($organization)) {
    $topdn = $oc;
    if (contains_ascii($topdn)) {
      $topdn = 'dn:: '.base64_encode($topdn);
    } else {
      $topdn = "dn: $topdn";
    }
    $o = $cn_arr[0];
    if (contains_ascii($o)) {
      $o = 'o:: '.base64_encode($o);
    } else {
      $o = "o: $o";
    }
    $organization =  "# extended LDIF$eol";
    $organization .= "#$eol";
    $organization .= "# LDAPv3$eol";
    $organization .= "# base <> with scope subtree$eol";
    $organization .= "# filter: (objectclass=*)$eol";
    $organization .= "# requesting: ALL$eol";
    $organization .= "#$eol";
    $organization .= "# $cn_arr[0], $cn_arr[1]$eol";
    $organization .= "$topdn$eol";
    $organization .= "objectClass: top$eol";
    $organization .= "objectClass: organization$eol";
    $organization .= "$o$eol";
  }
  
  // Add Unit
  $org_dn = "$ou,$oc";
  if (contains_ascii($org_dn)) {
     $org_dn = 'dn:: '.base64_encode($org_dn);
  } else {
    $org_dn = "dn: $org_dn";
  }
  $org_ou = $node_name;
  if (contains_ascii($org_ou)) {
    $org_ou = 'ou:: '.base64_encode($org_ou);
  } else {
    $org_ou = "ou: $org_ou";
  }
  $unit = "$eol# ".implode($ou_arr, ', ').", $cn_arr[0], $cn_arr[1]$eol";
  $unit .= "$org_dn$eol";
  $unit .= "objectClass: organizationalUnit$eol";
  $unit .= "$org_ou$eol";
  array_push($units, $unit);

  // Get node detail
  $users = run_query_organizationalchart_ogrouplink_detail($node_id);
  foreach ($users as $user_id => $one_user) {
    $uid = $one_user['login'];
    $person = "$eol# $uid, ".implode($ou_arr, ', ').", $cn_arr[0], $cn_arr[1]$eol";
    foreach($ldif_attributes as $k_ldif => $k_obm) {
        switch($k_ldif) {
          case 'dn':
            $dn = "uid=$one_user[$k_obm],$ou,$oc";
            if (contains_ascii($dn)) {
              $dn = base64_encode($dn);
              $person .= "$k_ldif:: $dn$eol";                                
            } else {
              $person .= "$k_ldif: $dn$eol";              
            }
            break;
          case 'objectClass':
            foreach($ldif_attributes[$k_ldif] as $key) {
              $person .= "$k_ldif: $key$eol";
            }              
            break;
          case 'cn':
            $cn = '';
            foreach($ldif_attributes[$k_ldif] as $key) {
              $cn .= $one_user[$key].' ';
            }
            if (!empty($cn)) {
              if (contains_ascii($cn)) {
                $cn = base64_encode($cn);
                $person .= "$k_ldif:: $cn$eol";                                
              } else {
                $person .= "$k_ldif: $cn$eol";              
              }
            }
            break;
          case 'displayName':
            $displayName = '';
            foreach($ldif_attributes[$k_ldif] as $key) {
              $displayName .= $one_user[$key].' ';
            }
            if (!empty($displayName)) {
              if (contains_ascii($displayName)) {
                $displayName = base64_encode($displayName);
                $person .= "$k_ldif:: $displayName$eol";
              } else {
                $person .= "$k_ldif: $displayName$eol";
              }
            }
            break;
          case 'mail':
            $emails = get_entity_email($one_user[$k_obm], $domain, false, null);
            foreach($emails as $email) {
              if (contains_ascii($email)) {
                $email = base64_encode($email);
                $person .= "$k_ldif:: $email$eol";                                
              } else {
                $person .= "$k_ldif: $email$eol";              
              }
            }
            break;
          default:
            $val = $one_user[$k_obm];
            if (!empty($val)) {
              if (contains_ascii($val)) {
                $val = base64_encode($val);
                $person .= "$k_ldif:: $val$eol";                                
              } else {
                $person .= "$k_ldif: $val$eol";                
              }
            }
            break;
        }
    }
    // Add Person
    array_push($persons, "$person");
  }
 
  // Get node children 
  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);

  if ($nb_children > 0) {
    foreach($t_child as $key => $child_id) {
      $child_q = run_query_organizationalchart_node_infos($child_id);
      $t_draw = export_ldif($child_q, $ou_arr);
    }
  } 

  return ;
}


/**
 * Build XML for Outline SVG 
 * 
 * @param $node_q 
 * @param &$xml
 * @param &$svg
 * @access public
 * @return DOM Object 
 */
function export_svg_outline($node_q, &$xml, &$svg) {
  global $y;
 
  // Properties
  $node_width = 300;
  $margin_top = 50;
  $style = "
    font-size:10px;
    font-weight:bold;
    fill:#636466;
    font-family:Bitstream Vera Sans";
  $t_color = array('#D2D2D2', '#F9CF8B', '#F7BBC8', '#BBD595', '#F6DFDA', '#DFD9E3', '#CFD8F2');

  $node_name = $node_q->f('ogroup_name');
  $node_id = $node_q->f('ogroup_id');
  $level = $node_q->f('ogroup_level');
  $flour = strlen($level) - strlen(str_replace('.', '', $level));  

  $color = $t_color[$flour%7];
  $x = 50 * $flour + 10;
  if (!isset($y)) $y=$margin_top;

  // Get node detail
  $users = run_query_organizationalchart_ogrouplink_detail($node_id);

  $text = $xml->createElement('text');
  $text->setAttribute('style', $style);
  $text->setAttribute('x', $x);
  $text->setAttribute('y', $y);
  $text->appendChild($xml->createTextNode(":: $node_name"));
  $svg->appendChild($text);
  $y += 2; // margin-bottom between nodes

  // Node
  $node_height = sizeof($users)*12+4;
  $rectNode = $xml->createElement('rect');
  $rectNode->setAttribute('x', $x);
  $rectNode->setAttribute('y', $y);
  $rectNode->setAttribute('width', $node_width);
  $rectNode->setAttribute('height', $node_height);
  $rectNode->setAttribute('style', "fill:$color;");
  $svg->appendChild($rectNode);

  // Users list
  if (is_array($users)) {
    usort($users, 'sort_node_detail');
    foreach ($users as $user_id => $one_user) {
      $y += 12; // new line
      $userobm_name = $one_user['lastname'].' '.$one_user['firstname'];
      $userobm_title = empty($one_user['title'])? '': '('.$one_user['title'].')';
      $text = $xml->createElement('text');
      $text->setAttribute('style', $style);
      $text->setAttribute('x', $x+10); // $x+10: margin-left
      $text->setAttribute('y', $y);
      $text->appendChild($xml->createTextNode("$userobm_name $userobm_title"));
      $svg->appendChild($text);
    }
  }


  // Get node children 
  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);
  if ($nb_children > 0) {
    foreach($t_child as $key => $child_id){
      $y+=20;
      $child_q = run_query_organizationalchart_node_infos($child_id);
      export_svg_outline($child_q, $xml, $svg);
    }
  } 

  return $svg;
}


/**
 * Build XML for Chart SVG 
 * 
 * @param $node_q 
 * @param &$xml
 * @param &$svg
 * @access public
 * @return DOM Object 
 */
function export_svg_chart($node_q, &$xml, &$svg) {
  global $l, $x, $t_x, $t_y; 

  if (!isset($l)) $l = array();
  if (!isset($t_x)) $t_x = array();

  // Properties
  $node_width = 200;
  $node_space = $node_width+5;
  $node_link = 15;
  $font_size = 12;
  $line_height = $font_size+2;
  $margin_top = 50;
  $style = "
    font-size:${font_size}px;
    font-weight:bold;
    font-family:Arial";

  $node_name = $node_q->f('ogroup_name');
  $node_id = $node_q->f('ogroup_id');
  $parent_node_id = $node_q->f('ogroup_parent_id');
  $level = $node_q->f('ogroup_level');
  $flour = strlen($level) - strlen(str_replace('.', '', $level));  

  $nb_leaves = run_query_organizationalchart_get_leaves($node_id, true);
  if($nb_leaves) {
    $node_x = $node_space/2*$nb_leaves+$x;
  }

  $t_child = run_query_organizationalchart_node_child($node_id);
  $nb_children = sizeof($t_child);
  $brother = run_query_organizationalchart_node_child($parent_node_id);
  $nb_brother = sizeof($brother) - 1;

  // Get node detail
  $users = run_query_organizationalchart_ogrouplink_detail($node_id);

  // node height
  $nb_users = sizeof($users);
  $node_height = ($nb_users+2)*$line_height; 

  // y coord
  if ($parent_node_id) {
    $node_y = $t_y[$parent_node_id]+$node_link;
  } else {
    $node_y = $flour*$node_height*2+$margin_top;
  }

  // node
  $rectNode = $xml->createElement('rect');
  $rectNode->setAttribute('x', $node_x);
  $rectNode->setAttribute('y', $node_y);
  $rectNode->setAttribute('width', $node_width);
  $rectNode->setAttribute('height', $node_height);
  $rectNode->setAttribute('style', 'fill:#FFF;stroke:#000;stroke-width:1;');
  $svg->appendChild($rectNode);

  // node title
  $rectTitle = $xml->createElement('rect');
  $rectTitle->setAttribute('x', $node_x);
  $rectTitle->setAttribute('y', $node_y);
  $rectTitle->setAttribute('width', $node_width);
  $rectTitle->setAttribute('height', $line_height);
  $rectTitle->setAttribute('style', "fill:url(#NodeTitleStyle);stroke:#000;stroke-width:1;");
  $svg->appendChild($rectTitle);
  $text = $xml->createElement('text');
  $text->setAttribute('style', $style);
  $text->setAttribute('fill', '#FFF');
  $text->setAttribute('x', $node_x+20);             // +20: margin-left
  $text->setAttribute('y', $node_y+$line_height-2); // -2: margin-bottom
  $text->appendChild($xml->createTextNode($node_name));
  $svg->appendChild($text);

  if (is_array($users)) {
    uasort($users, 'sort_node_detail');
    $user_y = $node_y+$line_height;
    foreach ($users as $user_id => $one_user) {
      // build users list
      $userobm_name = $one_user['lastname'].' '.$one_user['firstname'];
      if ($one_user['manager']) {
        $userobm_name = "* $userobm_name";
      }
      $user_y += $line_height; // new line
      $text = $xml->createElement('text');
      $text->setAttribute('style', $style);
      $text->setAttribute('fill', '#636466');
      $text->setAttribute('x', $node_x+5);
      $text->setAttribute('y', $user_y);
      $text->appendChild($xml->createTextNode($userobm_name));
      $svg->appendChild($text);
    }
  }

  if ($nb_children) {
    // node tail
    $nodeTail = $xml->createElement('rect');
    $nodeTail->setAttribute('x', $node_x+$node_width/2);
    $nodeTail->setAttribute('y', $node_y+$node_height);
    $nodeTail->setAttribute('width', 1);
    $nodeTail->setAttribute('height', $node_link);
    $svg->appendChild($nodeTail);

    // node y coord.
    $t_y[$node_id] = $node_y+$node_height+$node_link;
 
    foreach($t_child as $key => $child_id){
      if ($nb_children >= 2) {
        if ($key == 0){ // first child
          // node x coord.
          $child_leaves = run_query_organizationalchart_get_leaves($child_id, true);
          $child_x = $x+($node_space*$child_leaves+$node_width)/2;
          $t_x[$child_id] = $child_x;
        } else if ($key == ($nb_children-1)){ // last child
          $l[] = $child_id;
        }
      }
      $child_q = run_query_organizationalchart_node_infos($child_id);
      export_svg_chart($child_q, $xml, $svg);
    }
  } 

  if ($parent_node_id) { // If not root node
    // node head
    $nodeHead = $xml->createElement('rect');
    $nodeHead->setAttribute('x', $node_x+$node_width/2);
    $nodeHead->setAttribute('y', $t_y[$parent_node_id]);
    $nodeHead->setAttribute('width', 1);
    $nodeHead->setAttribute('height', $node_link);
    $svg->appendChild($nodeHead);
    if (in_array($node_id, $l)) {  // is last child ?
      $first_x = $t_x[$brother[0]];
      $parent_y = $t_y[$parent_node_id];
      // line
      $line = $xml->createElement('line');
      $line->setAttribute('x1', $first_x);
      $line->setAttribute('y1', $parent_y);
      $line->setAttribute('x2', $node_x+$node_width/2);
      $line->setAttribute('y2', $parent_y);
      $line->setAttribute('style', 'stroke:#000000;stroke-width:1;');
      $svg->appendChild($line);
    }
  } 

  // has brother? 
  if ($nb_brother) {
    $x += $node_space;
  }

  return $svg;
}


/**
 * Build a JSON message with all the event data
 * the respone is a table of event objects.
 * 
 * @param mixed $usr_q 
 * @access public
 * @return void
 */
function organizationalchart_json_event($usr_q) {
  global $display, $obm, $cgp_use;

  $userobm_email = $usr_q->f('userobm_email');
  // $email = get_entity_email($usr_q->f('userobm_email'));

  if( get_last_domain_text($usr_q->f('userobm_domain_id')) != '') {
    $main_domain = get_last_domain_text($usr_q->f('userobm_domain_id'));
  } else {
    $main_domain = $obm['domain_name'];
  }  
  $email = get_entity_email($usr_q->f('userobm_email'),$main_domain, false);

  if (strlen($email) > 30 ) {
    $email = substr($email, 0, 30).'...';
  }

  $name = phpStringToJsString($usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname'));
  $title = phpStringToJsString($usr_q->f('userobm_title'));
  $phone = phpStringToJsString($usr_q->f('userobm_phone'));
  $mphone = phpStringToJsString($usr_q->f('userobm_mobile'));
  $email = phpStringToJsString($email);

  $info[] = 'id:'.$usr_q->f('userobm_id');
  $info[] = "name:'$name'";
  $info[] = "photo:'".$usr_q->f('userobm_photo_id')."'";
  $info[] = "title:'$title'";
  $info[] = "phone:'$phone'";
  $info[] = "mphone:'$mphone'";
  $info[] = "email:'$email'";

  $json = implode(',',$info);
  $display['json'] =  "$json,error:0";
}

/**
 * Display OrganizationalChart Display preference screen 
 * @param $prefs Display preferences 
 * @access public
 * @return HTML
 */
function dis_organizationalchart_display_pref($prefs) {
  global $l_organizationalchart_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'organizationalchart');
  $dis_pref->pref_title = $l_organizationalchart_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}

?>
