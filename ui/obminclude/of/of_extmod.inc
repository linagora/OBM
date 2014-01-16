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
// OBM - File : of_extmod.inc                                                //
//     - Desc : OBM Framework External modules call helpers functions        //
// 2005-10-04 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


if ($params["ext_element"] != "") {
  $extra_js .= "

///////////////////////////////////////////////////////////////////////////////
// Fill the source form with checked values from a result set
// Parameters:
//   - int_form : source form where to get values (eg: from popup windows)
//   - entity   : [optionnal] entity to select [default to $module]
///////////////////////////////////////////////////////////////////////////////
function of_select_fill_from_checkbox(int_form, entity) {
  
  if (typeof(entity) == 'undefined') {
    entity = '$module';
  }
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
	  of_extmod_add_element('$params[ext_element]', sel_id, span.innerHTML);
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
function of_extmod_add_element(element_name, sel_id, fieldLabel) {
  target = window.opener.document;
  container = get_elem(target, '$params[ext_element]');

  // Create the div row
  row = target.createElement('div');
  row.id = '$params[ext_element]-'+sel_id;
  row.className = 'elementRow';

  // Create the input field
  field = target.createElement('input');
  field.type = 'hidden';
  field.name = element_name+'[]';
  field.value = sel_id;

  // Create the link associated to the div
  link = target.createElement('a');
  link.href = 'javascript: remove_element(\'$params[ext_element]-'+sel_id+'\',\'$params[ext_element]\');';
//alert (link.href);
  link.onclick = '';

  // Create the icon image
  image = target.createElement('img');
  image.src = '$ico_delete';
  link.appendChild(image);

  // Create the label
  content = target.createTextNode(fieldLabel);

  // Add these elements to the div row
  row.appendChild(link);
  row.appendChild(content);
  row.appendChild(field);

  // Add the div row to the container
  container.appendChild(row);
} 

";
}


//=============================================================================
// OF Extmod API
//=============================================================================

// Display functions ----------------------------------------------------------

///////////////////////////////////////////////////////////////////////////////
// Display: extmod select block 
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
//   - $url        : url to access external select data
///////////////////////////////////////////////////////////////////////////////
function of_extmod_dis_search_select($data_array, $data_name, $url) {
  global  $ico_add, $ico_delete;

  $l_name = "l_${data_name}"; 
  global $$l_name;

  $sel = _of_extmod_select($data_array, $data_name);

  $block = "
    <div class=\"searchLabel\">${$l_name}
      <a href=\"javascript: return false;\"
      onclick=\"popup('$url','','360','240'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" /></a>
      &nbsp;
      <a href=\"javascript: return false;\"
        onclick=\"remove_all_element('sel_${data_name}');
        return false;\">
        <img src=\"$ico_delete\" alt=\"[Clear]\" /></a>
      <br />
      <div class=\"elementRow\" id=\"sel_$data_name\">$sel</div>
    </div>";

  return $block;
}


//=============================================================================
// OF Extmod internal helper API
//=============================================================================


///////////////////////////////////////////////////////////////////////////////
// Display: category html select component
// Parameters:
//   - $data_array : data array $data_array[id]=[label]
//   - $data_name  : data name to construct the fields
///////////////////////////////////////////////////////////////////////////////
function _of_extmod_select($data_array, $data_name) {
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
