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
// OBM - File : of_select.inc                                                //
//     - Desc : OBM Framework Rich Select helpers functions                  //
// 2005-10-04 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

if (($params["ext_element"] != "")
    || (true)) {
  $extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Fill the of_select from checked checkboxes from a form result set
// Parameters:
//   - int_form : source form where to get values (eg: from popup windows)
//   - entity   : [optionnal] entity to select [default to $module]
///////////////////////////////////////////////////////////////////////////////
function of_select_fill_from_checkbox(int_form, entity) {
  if (typeof(entity) == 'undefined') {
    entity = '$module';
  }
  var container = '$params[ext_container]';
  size = int_form.length;
  for (i=0; i <size; i++) {
    // For each checkbox of the form, if filled we send the value
    if (int_form.elements[i].type == 'checkbox') {
      if (int_form.elements[i].checked == true) {
        id = int_form.elements[i].name;
        div_id = '$params[ext_element]'+'-data-'+entity+'-'+id;
        // If the element not already present in the caller window
        if (! get_elem(window.opener.document, div_id )) {
          sel_id = 'data-'+entity+'-'+id;
          // Get the label associated to the id from the int_form row
          // hidden in an hidden span
          span = get_elem(window.document, sel_id);
          if (container == '' || container == 'div') {
            of_select_add_element(window.opener.document, '$params[ext_element]', sel_id, span.innerHTML);
          }
          if (container == 'table') {
            of_select_add_element_table(window.opener.document, '$params[ext_element]', sel_id, span.innerHTML);
          }
        }
      }
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Add an element (div with img, name and hidden input) target given
// Parameters:
//   - element_name : parent element where to add the div, an input name []
//   - sel_id       : id to register in the input field value
//   - fieldLabel   : Label to display
///////////////////////////////////////////////////////////////////////////////
function of_select_add_element(target, element_name, sel_id, fieldLabel) {

  container = get_elem(target, element_name);

  // Create the div row
  row = target.createElement('div');
  row.id = element_name+'-'+sel_id;
  row.className = 'elementRow';

  // Create the input field
  field = target.createElement('input');
  field.type = 'hidden';
  field.name = element_name+'[]';
  field.value = sel_id;
  field.text = fieldLabel;

  // Create the delete link associated to the div
  link = target.createElement('a');
  link.href = 'javascript: remove_element(\''+element_name+'-'+sel_id+'\',\''+element_name+'\');';
  link.onclick = '';

  // Create the icon image
  image = target.createElement('img');
  image.src = '$ico_delete';
  link.appendChild(image);
  l_ritem= document.createTextNode(\"$l_ritem \");

  // Create the label
  content = target.createTextNode(fieldLabel);

  // Add these elements to the div row
  row.appendChild(link);
  row.appendChild(content);
  row.appendChild(field);

  // Add the div row to the container
  container.appendChild(row);
} 

///////////////////////////////////////////////////////////////////////////////
// Add a table row element (td with img, name and multiple td for each perm) target given
// Parameters:
//   - element_name : parent element where to add the row, an input name []
//   - sel_id       : id to register in the input field value
//   - fieldLabel   : Label to display
///////////////////////////////////////////////////////////////////////////////
function of_select_add_element_table(target, element_name, sel_id, fieldLabel) {
  container = get_elem(target, element_name);
  
  // Create the table row
  row = target.createElement('tr');
  row.id = element_name+'-'+sel_id;

  // Create the delete link
  var link = $(target.createElement('a'));
  link.href = 'javascript: remove_element(\''+element_name+'-'+sel_id+'\',\''+element_name+'\');';

  // Create the icon image
  var image = $(target.createElement('img'));
  image.src = obm.vars.images.del;
  link.appendChild(image);
    
  // Create the consumer cell
  var con_cell = $(target.createElement('td'));
  con_cell.appendChild(link);
  con_cell.appendText(fieldLabel);  
  row.appendChild(con_cell);

  if($(target.getElementById('public_perms'))) {
    // Create the select all link
    var sel_all_link = $(target.createElement('a'));
    sel_all_link.href = '#';
    sel_all_link.setProperty('onclick', 'select_all_perms(\''+element_name+'-'+sel_id+'\')');
    sel_all_link.appendText('$l_select_all');
    
    // Create the unselect all link
    unsel_all_link = $(target.createElement('a'));
    unsel_all_link.href = '#';
    unsel_all_link.setProperty('onclick', 'unselect_all_perms(\''+element_name+'-'+sel_id+'\')');
    unsel_all_link.appendText('$l_unselect_all');
    
    // Create the access perm checkbox
    var cb_access_public = $(target.getElementById('cb_access_public'));
    var accept_access_disabled = false;
    var accept_access_style = 'visible';
    if ( (cb_access_public !== null && cb_access_public !== false) && cb_access_public.getElement('input').checked) {
      accept_access_disabled = true;
      accept_access_style = 'hidden';
    }
    var cb_access = $(target.createElement('input'));
    cb_access.type = 'checkbox';
    cb_access.name = 'cb_accept_access[]';
    cb_access.value = sel_id;
    cb_access.disabled = accept_access_disabled;
    cb_access.style.visibility = accept_access_style;
    
    // Create the read perm checkbox
    var cb_read_public = $(target.getElementById('cb_read_public'));
    var accept_read_disabled = false;
    var accept_read_style = 'visible';
    if ( (cb_read_public !== null && cb_read_public !== false) && cb_read_public.getElement('input').checked) {
      accept_read_disabled = true;
      accept_read_style = 'hidden';
    }
    var cb_read = $(target.createElement('input'));
    cb_read.type = 'checkbox';
    cb_read.name = 'cb_accept_read[]';
    cb_read.value = sel_id;
    cb_read.disabled = accept_read_disabled;
    cb_read.style.visibility = accept_read_style;
    cb_read.setProperty('onchange', 'apply_checked_access(this, \''+element_name+'-'+sel_id+'\')');
    cb_read.setProperty('onclick', 'apply_checked_access(this, \''+element_name+'-'+sel_id+'\')');

    
    // Create the write perm checkbox
    var cb_write_public = $(target.getElementById('cb_write_public'));
    var accept_write_disabled = false;
    var accept_write_style = 'visible';
    if ( (cb_write_public !== false && cb_write_public !== null) && cb_write_public.getElement('input').checked) {
      accept_write_disabled = true;
      accept_write_style = 'hidden';
    }
    var cb_write = $(target.createElement('input'));
    cb_write.type = 'checkbox';
    cb_write.name = 'cb_accept_write[]';
    cb_write.value = sel_id;
    cb_write.disabled = accept_write_disabled;
    cb_write.style.visibility = accept_write_style;
    cb_write.setProperty('onchange', 'apply_checked_read(this, \''+element_name+'-'+sel_id+'\')');
    cb_write.setProperty('onclick', 'apply_checked_read(this, \''+element_name+'-'+sel_id+'\')');
    
    // Create the admin perm checkbox
    var cb_admin = $(target.createElement('input'));
    cb_admin.type = 'checkbox';
    cb_admin.name = 'cb_accept_admin[]';
    cb_admin.value = sel_id;
    
    // Create the select/unselect cell
    sel_cell = $(target.createElement('td'));
    sel_cell.appendChild(sel_all_link);
    sel_cell.appendText('/');
    sel_cell.appendChild(unsel_all_link);
    
    // Create the access perm cell
    var access_cell = $(target.createElement('td'));
    access_cell.addClass('cell_perms_access');
    access_cell.appendChild(cb_access);
    
    // Create the read perm cell
    var read_cell = $(target.createElement('td'));
    read_cell.addClass('cell_perms_read');
    read_cell.appendChild(cb_read);
    
    // Create the write cell
    var write_cell = $(target.createElement('td'));
    write_cell.addClass('cell_perms_write');
    write_cell.appendChild(cb_write);
    
    // Create the admin cell
    var admin_cell = $(target.createElement('td'));
    admin_cell.appendChild(cb_admin);

    // Add these elements to the table row
    row.appendChild(sel_cell);
    row.appendChild(access_cell);
    row.appendChild(read_cell);
    row.appendChild(write_cell);
    row.appendChild(admin_cell);

  } else {
    var val_cell = $(target.createElement('td'));
    field = target.createElement('input');
    field.type = 'hidden';
    field.name = sel_id;
    field.value = 1;
    field.text = fieldLabel;    
    val_cell.appendChild(field);
    row.appendChild(val_cell);
  }
  // Add the div row to the container
  container.appendChild(row);
  
  // Apply class pair
  if (container.getChildren().length % 2 != 0) {
    row.addClass('pair');
  }
} 


///////////////////////////////////////////////////////////////////////////////
// Add an element (div with img, name and hidden input) target given
// Parameters:
//   - element_name : parent element where to add the div, an input name []
//   - sel_id       : id to register in the input field value
//   - fieldLabel   : Label to display
///////////////////////////////////////////////////////////////////////////////
function of_select_set_tree(target, element_name, value) {

  checkbox = get_elem(target, element_name+'_tree');

  checkbox.checked = value;
} 


///////////////////////////////////////////////////////////////////////////////
// Return the of_select element list in an array
// Returns:
// el[cb] = checkbox status, el[0]=item0 ([id],[text])
///////////////////////////////////////////////////////////////////////////////
function of_select_get_element_array(elementName) {

  target = window.document;
  cb = target.getElementById(elementName+'_tree');

  el = Array();
  try {
    el[cb] = cb.checked;
  } catch(e) {
    el[cb] = false;
  }

  container = target.getElementById(elementName);

  nb_child = 0;
  while (container.firstChild) {
    div = container.firstChild;
    while (div.firstChild) {
      if (div.firstChild.name == elementName+'[]') {
        el[nb_child] = new Array();
        el[nb_child]['id'] = div.firstChild.value;
        el[nb_child]['text'] = div.firstChild.text;
        nb_child++;
      }
      div.removeChild(div.firstChild);
    }
    container.removeChild(container.firstChild);
  }

  return el;
} 

";
}


//=============================================================================
// OF Select API
//=============================================================================

// Display functions ----------------------------------------------------------

///////////////////////////////////////////////////////////////////////////////
// Display: global select block 
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
//   - $url        : url to access external select data
//   - $tree       : display the tree option
///////////////////////////////////////////////////////////////////////////////
function of_select_dis_search_select($data_array, $data_name, $url, $tree=false) {
  global  $ico_add, $ico_delete, $l_cat_tree;

  $l_name = "l_${data_name}"; 
  global $$l_name;

  $sel = _of_select_select($data_array, $data_name);

  if ($tree) {
    $dis_tree = "<input type=\"checkbox\" id=\"sel_${data_name}_tree\" name=\"cb_$data_name\" value=\"1\">$l_cat_tree</input>";
  }

  $block = "
    <div class=\"searchLabel\">${$l_name}
      <a href=\"javascript: return false;\"
      onclick=\"popup('$url','','360','240'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" /></a>
      &nbsp;$dis_tree&nbsp;
      <a href=\"javascript: return false;\"
        onclick=\"remove_all_element('sel_${data_name}');
        return false;\">
        <img src=\"$ico_delete\" alt=\"[Clear]\" /></a>
      <br />
      <div class=\"elementRow\" id=\"sel_$data_name\"><div class=\"elementRowEmpty\"></div>$sel</div>
    </div>";

  return $block;
}


//=============================================================================
// OF Select internal helper API
//=============================================================================


///////////////////////////////////////////////////////////////////////////////
// Display: category html select component
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
///////////////////////////////////////////////////////////////////////////////
function _of_select_select($data_array, $data_name) {
  global  $ico_delete;

  if (is_array($data_array)) {
    foreach ($data_array as $id => $one_data) {
      $data_label = htmlentities($one_data["label"], ENT_COMPAT, 'UTF-8');
      $data_id = "data-$data_name-$id";
      $div_id = "sel_$data_name-$data_id";
      $sel_data .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_$data_name');\"><img src=\"$ico_delete\"></a>
        $data_label
        <input value=\"$data_id\" name=\"sel_${data_name}[]\" type=\"hidden\" />
        </div>";
    }
  }

  return $sel_data;
}


?>
