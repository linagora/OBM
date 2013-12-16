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
// OBM - File : user_display.inc                                             //
//     - Desc : User Display functions File                                  //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames['resource_name'] = $l_name;
if($perm->check_right('resource', $cright_read)) {
  $fieldnames['resource_delegation'] = $l_delegation;
  $fieldnames['resource_description'] = $l_desc;
  $fieldnames['resource_qty'] = $l_qty;
  $fieldnames['resourcetype_label'] = $l_rtype;
  $fieldnames['resource_id'] = $l_update;
}

///////////////////////////////////////////////////////////////////////////////
// Display resource specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_resource(&$OD, $fieldname, $link_ok) {
  global $path, $ico_update, $params;

  if (($fieldname == 'resource_name') && $link_ok) {
    $res['url'] = "$path/resource/resource_index.php?action=detailconsult&amp;resource_id=".$OD->data_set->f('resource_id');
  }
  if (($fieldname == 'data_element') && $params['ext_element'] != '') {
    $res['name'] = "<span id=\"data-resource-".$OD->data_set->f('resource_id')."\" style=\"display:none;\">".
    $OD->data_set->f('resource_name').
    "</span>";
  }
  else if ($fieldname == 'resource_id') {
    $t_id = $OD->data_set->f('resource_id');
    $res['url'] = "$path/resource/resource_index.php?action=detailupdate&amp;resource_id=$t_id";
    $res['name'] = "<img src=\"$ico_update\" alt=\"\" />";
    $res['txt_name'] = ' ';
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display resource search form
// Parameters:
//   - $resource[]   : default form values
//     keys used : name, desc, qty, popup
///////////////////////////////////////////////////////////////////////////////
function html_resource_search_form($resource) {
  global $l_name, $l_desc, $l_qty, $l_find, $l_all, $l_delegation;
  global $l_add_resources;
  global $obm, $display, $c_all, $l_rtype;
  global $l_undef, $c_undef;

  $lname = $resource['name'];
  $ldesc = stripslashes($resource['desc']);
  $qtyinf = $resource['qtyinf'];
  $qtysup = $resource['qtysup'];
  $popup = $resource['popup'];
  $sel_rtype = $resource['rtype'];
  $delegation = $resource['delegation'];

  if ($resource['filter_entity']) {
    $dis_filter_entity = "<input name=\"filter_entity\" type=\"hidden\" value=\"$resource[filter_entity]\">";
    if($resource['filter_pattern'])
      $dis_filter_pattern = "<input name=\"filter_pattern\" type=\"hidden\" value=\"$resource[filter_pattern]\">";
    else
      $dis_filter_pattern = "<input name=\"filter_pattern\" type=\"hidden\" value=\"read\">";

  }

  if ($popup) {
    $ext_action = $resource['ext_action'];
    $ext_title = stripslashes($resource['ext_title']);
    $ext_url = $resource['ext_url'];
    $ext_id = $resource['ext_id'];
    $ext_target = $resource['ext_target'];
    $ext_widget = $resource['ext_widget'];
    $ext_element =  $resource['ext_element'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
	    <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == '') {
      $ext_title = $l_add_resources;
    }
    $display['title'] = $ext_title;
  }

  // Resource Type
  $resourcetype_q = run_query_resourcetype();
  $block_resourcetype = "<select name=\"sel_rtype\" id=\"sel_rtype\">
    <option value=\"$c_undef\">$l_undef</option>";
  while($resourcetype_q->next_record()) {
    $id = $resourcetype_q->f('resourcetype_id');
    $label = $resourcetype_q->f('resourcetype_label');
    if ($id == $sel_rtype) {
      $selected = 'selected';
    } else {
      $selected = '';
    }
    $block_resourcetype .= "<option value=\"$id\" $selected>$label</option>";
  }
  $block_resourcetype .= '</select>';

  $dis_delegation = of_delegation_dis_search_form($delegation);
  $display_pref = get_display_pref($obm['uid'],'resource');

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare('resource_index.php')."\" class=\"search\">

    <label>$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"30\" maxlength=\"32\"
      value=\"$lname\" />
    </label>
    $dis_delegation
    <label>$l_rtype<br />
      $block_resourcetype
    </label>
    <label>&nbsp;&nbsp;<br />
    </label>
    <label>&nbsp;<br />
      <input type=\"text\" name=\"tf_qtyinf\" size=\"4\" maxlength=\"8\" value=\"$qtyinf\" />
    </label>
    <label><br />
      &nbsp;&lt;=&nbsp;$l_qty&nbsp;&lt;=&nbsp;
    </label>
    <label>&nbsp;<br />
      <input type=\"text\" name=\"tf_qtysup\" size=\"4\" maxlength=\"8\" value=\"$qtysup\" />
    </label>
    ";
    
  if ($display_pref['resource_description']['status'] != 0) {
    $block .= "
    <label>$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"30\" maxlength=\"32\"
      value=\"$ldesc\" />
    </label>
    ";
  }
  
  $block .= "
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $dis_filter_entity
      $dis_filter_pattern
      $ext&nbsp;
    </label>
    <p class=\"CL\" />
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the reseource search result
// Parameters:
//   - $resource[]   : resource search criteria
//     keys used : name, desc
///////////////////////////////////////////////////////////////////////////////
function dis_resource_search_list($resource) {
  global $l_found, $l_no_found;
  global $display, $obm;

  $popup = $resource['popup'];
  $filter_entity = $resource['filter_entity'];

  $prefs = get_display_pref($obm['uid'], 'resource');
  $obm_q = run_query_resource_search($resource);
  $nb_resource = $obm_q->num_rows_total();
  if ($nb_resource == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_resource $l_found");
    $block = html_resource_search_list($obm_q, $prefs, $resource, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Resource Search result
// Parameters:
//   - $obm_q    : database result (resource list)
//   - $prefs   : the fields which have to be displayed
//   - $resource[]   : resource search criteria
//     keys used : name, desc
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_resource_search_list($obm_q, $prefs, $resource, $popup) {
  global $l_close, $l_add;

  $lname = urlencode($resource['name']);
  $ldesc = urlencode($resource['desc']);
  $lqtyinf = urlencode($resource['qtyinf']);
  $lqtysup = urlencode($resource['qtysup']);
  $rtype = $resource['rtype'];
  $delegation = $resource['delegation'];

  if ($popup) {
    $ext_action = $resource['ext_action'];
    $ext_url = $resource['ext_url'];
    $ext_id = $resource['ext_id'];
    $ext_target = $resource['ext_target'];
    $ext_widget = $resource['ext_widget'];
    $ext_element =  $resource['ext_element'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget";
  }

  $url = url_prepare("resource_index.php?action=search&amp;tf_name=$lname&amp;tf_delegation=$delegation&amp;tf_desc=$ldesc&amp;tf_qtyinf=$lqtyinf&amp;tf_qtysup=$lqtysup&amp;sel_rtype=$rtype$url_ext");

  $resource_d = new OBM_DISPLAY('DATA', $prefs, 'resource');
  if ($popup) {
    $resource_d->display_link = false;
    $resource_d->data_cb_text = 'X';
    $resource_d->data_idfield = 'resource_id';
    $resource_d->data_cb_name = 'data-r-';
    if ($ext_element == "sel_attendees_id") {
      $resource_d->data_cb_name = "";
      $resource_d->data_form_head = "
      <form onsubmit=\"addFreeBusyEntities(this); return false;\">";
    } elseif ($ext_widget != '') {
      $resource_d->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } elseif ($ext_element != '')  {
      $resource_d->data_cb_name = '';
      $resource_d->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this);return false;\">";
    } else {
      $resource_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }

    $resource_d->data_form_end = "
      <fieldset class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </fieldset>
      </form>";

    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $resource_d->data_set = $obm_q;
  $resource_d->data_header = 'both';
  $resource_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $resource_d->display('dis_data_resource');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Resource Form
// Parameters:
//   - $usr_q     : resource database result
//   - $resource  : default values or updated values (if error)
//     kes used   : id, name, passwd, perms, email
///////////////////////////////////////////////////////////////////////////////
function html_resource_form($usr_q, $resource) {
  global $l_name, $l_desc, $l_qty, $l_rtype, $l_header_new_f;
  global $l_resource, $l_insert, $l_update;
  global $action, $display, $obm, $path;
  global $l_undef, $c_undef,$l_admin_permission;

  // if update mode and first time value are taken from database
  if ($action == 'detailupdate') {
    $id = $usr_q->f('resource_id');
    $name = $usr_q->f('resource_name');
    $desc = $usr_q->f('resource_description');
    $rtype = $usr_q->f('resource_rtype_id');
    $qty = $usr_q->f('resource_qty');
    $delegation = $usr_q->f('resource_delegation');
    $dis_title = $name;
  }

  // If parameters have been given, they supercede the default action value
  if (isset($resource['resource_id'])) { $id = $resource['resource_id']; }
  if (isset($resource['name'])) { $name = $resource['name']; }
  if (isset($resource['qty'])) { $qty = $resource['qty']; }
  if (isset($resource['rtype'])) { $rtype = $resource['rtype']; }
  if (isset($resource['delegation'])) { $delegation = stripslashes($resource['delegation']); }
  if (isset($resource['desc'])) { $desc = stripslashes($resource['desc']); }

  $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);

  // Resource Type
  $resourcetype_q = run_query_resourcetype();
  $block_resourcetype = "<select name=\"sel_rtype\" id=\"sel_rtype\">
    <option value=\"$c_undef\">$l_undef</option>";
  while($resourcetype_q->next_record()) {
    $rtype_id = $resourcetype_q->f('resourcetype_id');
    $label = $resourcetype_q->f('resourcetype_label');
    if ($rtype_id == $rtype) {
      $selected = 'selected';
    } else {
      $selected = '';
    }
    $block_resourcetype .= "<option value=\"$rtype_id\" $selected>$label</option>";
  }
  $block_resourcetype .= '</select>';

  // Buttons
  if (($action == 'new') || ($action == 'insert')) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";

    $right_manager = "
      <fieldset class=\"detail extra\">
      <legend>$l_admin_permission : <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" /></legend>
      <input type=\"hidden\" name=\"sel_accept_admin[]\" value=\"data-user-$obm[uid]\" />
      <div id=\"sel_accept_admin\">
      </div>
      </fieldset>";
    $js = "
    <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search', 'sel_accept_admin', 'userSearch');
    });
    </script>";

  } elseif (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\" />
          <input type=\"hidden\" name=\"resource_id\" value=\"$id\" />
          <input type=\"submit\" value=\"$l_update\" />";
  }

  $display['title'] = $dis_title;

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_resource(this)) return true; else return false;\"
      action=\"" . url_prepare('resource_index.php') . "\">

    <fieldset class=\"detail extra\">
    <legend>$l_resource</legend>
    <table>
    <tr>
      <th>$l_name</th>
      <td><input id=\"tf_name\" name=\"tf_name\" size=\"40\" maxlength=\"32\" value=\"$name\" /></td>
    </tr>$block_delegation<tr>
      <th>$l_rtype</th>
      <td>$block_resourcetype</td>
    </tr><tr>
      <th>$l_qty</th>
      <td><input id=\"tf_qty\" name=\"tf_qty\" size=\"4\" maxlength=\"8\" value=\"$qty\" /></td>
    </tr>
    </table>
    </fieldset>
    $right_manager
    <fieldset class=\"detail extra\">
    <legend>$l_desc</legend>
      <textarea id=\"ta_desc\" name=\"ta_desc\" rows=\"3\" cols=\"80\">$desc</textarea>
    </fieldset>

    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>

    </form>
    $js";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Resource Consultation
// Parameters:
//   - $resource : resource database result
///////////////////////////////////////////////////////////////////////////////
function dis_resource_consult($resource) {
  global $display, $l_err_reference;

  $id = $resource['resource_id'];

  if ($id > 0) {
    $obm_q = run_query_resource_detail($id);
    if ($obm_q->num_rows() == 1) {
      $block = html_resource_consult($obm_q);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Resoiurce Consultation
// Parameters:
//   - $obm_q : Resource database result
///////////////////////////////////////////////////////////////////////////////
function html_resource_consult($obm_q) {
  global $l_resource, $l_name, $l_desc, $l_qty, $l_rtype;
  global $path, $display, $c_yes, $c_no;

  $id = $obm_q->f('resource_id');
  $name = $obm_q->f('resource_name');
  $desc = $obm_q->f('resource_description');
  $qty = $obm_q->f('resource_qty');
  $rtype = ucfirst($obm_q->f('resourcetype_label'));
  $delegation = $obm_q->f('resource_delegation');

  $display['detailInfo'] = display_record_info($obm_q);
  $display['title'] = $name;
  $block_delegation = of_delegation_dis_block_consult($delegation);

  $block = "
  <div class=\"detail extra\" style=\"overflow:hidden\">
  <h1>$l_resource</h1>
    <table>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
    </tr>$block_delegation<tr>
      <th>$l_rtype</th>
      <td>$rtype</td>
    </tr><tr>
      <th>$l_qty</th>
      <td>$qty</td>
    </tr>
    </table>
    </div>

  <div class=\"detail extra\">
  <h1>$l_desc</h1>
      $desc
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the resource can be deleted, and the form
// Parameters:
//   - $p_id : resource id
///////////////////////////////////////////////////////////////////////////////
function dis_resource_can_delete($p_id) {
  global $display, $l_delete, $l_can_delete, $l_back;

  $url = url_prepare('resource_index.php');

  $dis_back = "<form name=\"form_back\" method=\"get\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"resource_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"resource_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display['msg'] .= display_ok_msg($l_can_delete);
  $block .= "
      <div class=\"buttons\">
        $dis_delete
        $dis_back
      </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Resource rights admin form
// Parameters:
//   - $id : resource id
///////////////////////////////////////////////////////////////////////////////
function dis_resource_right_dis_admin($id) {
  global $display, $l_resource;

  $infos = get_resource_infos($id);
  $name = $infos['name'];
  $display['title'] = $name;
  $block = of_right_dis_admin('resource', $id);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Resource Display preference screen
// Parameters:
//   - $prefs : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_resource_display_pref($prefs) {
  global $l_resource_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'resource');
  $dis_pref->pref_title = $l_resource_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


/**
 * Build a JSON array with all search results
 *
 * @param $results array of 'length' => DB query results nb, and 'datas' => DB Resources search query results
 * @access public
 * @return void
 */
function json_search_resources($resource,$results) {
  global $display, $l_qty, $l_ritem,$path;

  $res_q = $results['datas'];

  $form = $resource['form'];

  $resources = array();
  while($res_q->next_record()) {
    $id = $res_q->f('resource_id');
    $resourcetype_id = $res_q->f('resource_rtype_id');
    $label = phpStringToJsString($res_q->f('resource_name'));
    $extra = phpStringToJsString("$l_qty : ".$res_q->f('resource_qty'));
    $items = $res_q->f('items');
    $property = $res_q->f('resourcetype_property');

    if ($form == 'event' && $resourcetype_id != '') {
      // Display resource item
      // TODO : Afficher le lien seulement si des accessoires sont dispo.
      $url_item = "$path/resource/resource_index.php?action=ext_ritem&ext_element=list-item&popup=1&resource_id=$id";
      if($items > 0 || $property != '') {
        $extension = "new Element('div')";
      } else {
        $extension = "''";
      }
      if ($items > 0) {
        $extension .= ".adopt(new Element('a').
          setProperties({'id' : 'a[$id]', 'href' : 'javascript: void(0);'}).
          setProperty('onclick', 'popup(\"$url_item\", \"\")').
          appendText('$l_ritem').adopt(
          new Element('textarea').
            setProperties({'id':'list-item[$id]', 'name':'list-item[$id]', 'readonly':'readonly'}).
            setStyle('display', 'block')).adopt(
          new Element('input').
            setProperties({'id':'list-item-id[$id]', 'name':'list-item-id[$id]', 'type':'hidden'})).adopt(
          new Element('input').
            setProperties({'id':'res-name[$id]', 'name':'res-name[$id]', 'type':'hidden', 'value':'$label'})).adopt(
          new Element('input').
            setProperties({'id':'res-id[$id]', 'name':'res-id[$id]', 'type':'hidden', 'value':'$id'})))";
      }

      // Display resource property if exists
      if ($property != '') {
        $l_property = phpStringToJsString(ucfirst($property));
        $extension .= ".
          adopt(
            new Element('div').
              adopt(new Element('input').
                setProperties(
                  {'type': 'hidden',
                  'name': 'property-name[$id]',
                  'value': '$property'})).
              adopt(new Element('input').
                setProperties(
                  {'type': 'text',
                  'name': 'property[$id]',
                  'value': '$l_property'}).
                addClass('downlight').
                addEvent('click', function(){ if(this.value=='$l_property') { this.value=\"\"; this.removeClass('downlight') }}).
                addEvent('blur', function() { if(this.value == '') { this.addClass('downlight'); this.value = '$l_property';  } })
              )
          )";
      } else {
        $extension .= '';
      }
    } else {
      $extension = "''";
    }
    $resources[] = "{id:'data-resource-$id', label:'$label', extra:'$extra', extension: $extension}";
  }
  $display['json'] = "{length:".$results['length'].", datas:[".implode(',',$resources)."]}";
}


/**
 * Display the resource administration index
 * @param mixed $params
 * @access public
 * @return Resource Admin HTML code
 */
function dis_resource_admin_index($params) {

  $rt_q = run_query_resourcetype();
  $block .= html_resource_resourcetype_form($rt_q);

  $ri_q = run_query_resourceitem();
  $block .= html_resource_resourceitem_form($ri_q);

  return $block;
}


/**
 * Display: Resource Type Code section
 * @param $rt_q
 * @access public
 * @return Resource Type HTML code
 */
function html_resource_resourcetype_form($rt_q) {
  global $l_rtype_manage, $l_label, $l_rtype_property;
  global $l_c_new, $l_rtype_insert, $l_rtype_update, $l_rtype_checkdelete;
  global $ico_delete, $ico_ok;

  $key = 0;
  while($rt_q->next_record()) {
    $id = $rt_q->f('resourcetype_id');
    $label = $rt_q->f('resourcetype_label');
    $property = $rt_q->f('resourcetype_property');
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $key ++;

    $rtype_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"rtype_$id\" value=\"$id\" />
    <a href=\"?action=rtype_checklink&amp;rtype=$id\">
    <img src=\"$ico_delete\"/>
    </a></td>
    <td> <input type=\"text\" id=\"tf_rtype_label_$id\" value=\"$label\" size=\"16\"/></td>
    <td> <input type=\"text\" id=\"tf_rtype_property_$id\" value=\"$property\" size=\"16\"/></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'rtype');\">
    <img src=\"$ico_ok\"/>
    </a>
    </td>
    </tr>";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_rtype_update\" action=\"?action=rtype_update\" method=\"post\">
  <input type=\"hidden\" name=\"rtype\" value=\"\" />
  <input type=\"hidden\" name=\"tf_rtype_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_rtype_property\" value=\"\" />
  </form>
  <h1>$l_rtype_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_label</td>
  <td>$l_rtype_property</td>
  <td>&nbsp;</td>
  </tr>
  </thead>
  <tbody>
  $rtype_list
  </tbody>
  </table>
  </div>
  <form id=\"form_rtype_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category(this.tf_rtype_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  </thead>
  <tbody>
  <tr>
  <td>$l_label</td>
  <td>$l_rtype_property</td>
  </tr>
  <tr>
  <td><input type=\"text\" name=\"tf_rtype_label\" value=\"\" size=\"16\"/></td>
  <td><input type=\"text\" name=\"tf_rtype_property\" value=\"\" size=\"16\"/></td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_rtype_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"rtype_insert\" />
  </form>
  </div>";

  return $block;
}


/**
 * Display: Resource Item Code section
 * @param $ri_q
 * @access public
 * @return Resource Item HTML code
 */
function html_resource_resourceitem_form($ri_q) {
  global $l_ritem_manage, $l_label, $l_desc, $l_rtype;
  global $l_c_new, $l_ritem_insert;
  global $ico_ok, $ico_delete, $l_confirm_delete_item;
  global $l_j_create_type_first;

  $key = 0;
  while($ri_q->next_record()) {
    $id = $ri_q->f('resourceitem_id');
    $label = $ri_q->f('resourceitem_label');
    $desc = $ri_q->f('resourceitem_description');
    $type_id = $ri_q->f('resourceitem_resourcetype_id');

    $rt_q = run_query_resourcetype();
    $sel_type = "<select name=\"sel_rtype\" id=\"sel_rtype_$id\">";
    while($rt_q->next_record()) {
      $rtype_id = $rt_q->f('resourcetype_id');
      $rtype_label = $rt_q->f('resourcetype_label');
      if ($type_id == $rtype_id) {
        $selected = 'selected';
      } else {
        $selected = '';
      }
      $sel_type .= "<option value=\"$rtype_id\" $selected>$rtype_label</option>";

    }
    $sel_type .= '</select>';

    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $key ++;

    $ritem_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"ritem_$id\" value=\"$id\" />
    <a href=\"?action=ritem_delete&amp;ritem=$id\" onclick=\" return confirm('$l_confirm_delete_item');\">
    <img src=\"$ico_delete\"/>
    </a></td>
    <td> <input type=\"text\" id=\"tf_ritem_label_$id\" value=\"$label\" size=\"16\"/></td>
    <td> <textarea name=\"tf_ritem_desc\" id=\"tf_ritem_desc_$id\" rows=\"2\"/>$desc</textarea></td>
    <td> $sel_type </td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'ritem');\">
    <img src=\"$ico_ok\"/>
    </a>
    </td>
    </tr>";
  }

  $rt_q = run_query_resourcetype();

  $disabled = '';
  $onclick = '';
  if ($rt_q->num_rows() == 0) {
    $disabled = "disabled=\"true\"";
    $onclick = "onclick=\"alert('$l_j_create_type_first')\"";
  }

  $sel_type = "<select name=\"sel_rtype\" id=\"sel_rtype_$id\">";
  while($rt_q->next_record()) {
    $rtype_id = $rt_q->f('resourcetype_id');
    $rtype_label = $rt_q->f('resourcetype_label');
    if ($type_id == $rtype_id) {
      $selected = 'selected';
    } else {
      $selected = '';
    }
    $sel_type .= "<option value=\"$rtype_id\">$rtype_label</option>";
  }
  $sel_type .= '</select>';

  // --- HTML Template --------------------------------------------------------
  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_ritem_update\" action=\"?action=ritem_update\" method=\"post\">
  <input type=\"hidden\" name=\"ritem\" value=\"\" />
  <input type=\"hidden\" name=\"tf_ritem_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_ritem_desc\" value=\"\" />
  <input type=\"hidden\" name=\"sel_rtype\" value=\"\" />
  </form>
  <h1>$l_ritem_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_label</td>
  <td>$l_desc</td>
  <td>$l_rtype</td>
  <td>&nbsp;</td>
  </tr>
  </thead>
  <tbody>
  $ritem_list
  </tbody>
  </table>
  </div>
  <form id=\"form_ritem_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category(this.tf_ritem_label,'$jslabel')\" $onclick>
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  </thead>
  <tbody>
  <tr>
  <td>$l_label</td>
  <td>$l_desc</td>
  <td>$l_rtype</td>
  </tr>
  <tr>
  <td><input type=\"text\" name=\"tf_ritem_label\" value=\"\" size=\"16\"/></td>
  <td><textarea name=\"tf_ritem_desc\" value=\"\" rows=\"2\"/></textarea></td>
  <td>$sel_type</td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_ritem_insert\" $disabled/></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"ritem_insert\" />
  </form>
  </div>";

  return $block;
}


/**
 * Display: Resource Links
 * @param mixed $params
 * @param $kind
 * @access public
 * @return Resource link HTML code
 */
function dis_resource_links($params, $kind) {
  global $display;
  global $l_c_cant_delete, $l_c_can_delete, $l_resource_found_error;
  global $l_back, $l_c_delete;
  $l_kind = "l_${kind}";
  global $$l_kind;

  $id = $params["${kind}"];

  if ($kind == 'rtype') {
    $obm_q = run_query_resource_rtype_links($params);
  } else {
    $obm_q = run_query_resource_ritem_links($params);
  }

  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $display['msg'] .= display_warn_msg("${$l_kind} : $l_c_cant_delete : $nb $l_resource_found_error");
  } else {
    $display['msg'] .= display_ok_msg("${$l_kind} : $l_c_can_delete");
    $dis_buttons .= "<a href=\"?action=${kind}_delete&amp;${kind}=$id\">$l_c_delete</a>";
  }

  $dis_buttons .= "<a href=\"?action=admin\">$l_back</a>";

  $block = "
  <div class=\"buttons\">
    $dis_buttons
  </div>";

  return $block;
}


/**
 * Display: Resource Item Popup
 * @param mixed $params
 * @access public
 * @return Resource code
 */
function dis_ritem_popup($params) {

  // ResourceItem
  $r_id = $params['resource_id'];
  $ri_q = run_query_resourceitem($r_id);
  $block .= html_resource_resourceitem_popup($ri_q, $params);

  return $block;
}


/**
 * Display: Resource Item Popup
 * @param $ri_q
 * @param mixed $params
 * @access public
 * @return Resource Item popup HTML code
 */
function html_resource_resourceitem_popup($ri_q, $params) {
  global $display;
  global $l_close, $l_ritem, $l_ritem_add, $l_add;
  global $l_no_item_available;

  $res_id = $params['resource_id'];

  while($ri_q->next_record()) {
    $id = $ri_q->f('resourceitem_id');
    $label = $ri_q->f('resourceitem_label');
    $desc = $ri_q->f('resourceitem_description');
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $key ++;
    $ritem_list .= "
    <tr $class>
    <td id=\"item-$id\">
     <input type=\"checkbox\" id=\"$id\" name=\"$id\" />
     <span id=\"data-ritem-$id\" name=\"data-ritem-$id\" style=\"display: none;\">$label</span>
    </td>
    <td> $label </td>
    <td>".nl2br($desc).'</td>
    </tr>';
  }

  $display['title'] = $l_ritem;

  // If no item, display error message
  if ($ritem_list == '') {
    return display_err_msg($l_no_item_available);
  } else {

  // --- HTML Template --------------------------------------------------------
  $block = "
    <div class=\"detail admin\">
      <form onsubmit=\"select_ritem_fill_from_checkbox(this, '$res_id');return false;\">
      <table>
        $ritem_list
      </table>
      <fieldset class=\"buttons\">
        <input type=\"submit\" value=\"$l_ritem_add\" />
      </fieldset>
      </form>
      <p><a href=\"\" onclick='window.close();'>$l_close</a></p>
    </div>
    <script>
     item_id = window.opener.document.getElementById('list-item-id['+$res_id+']').value;
     item_arr = item_id.split(',');
     item_arr.each(function(item,index) {
       if(item != '') {
         ckbox = window.document.getElementById(item);
         ckbox.checked = 'true';
       }
     });
    </script>";
  }

  return $block;
}

?>
