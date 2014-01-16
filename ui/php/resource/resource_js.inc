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
// OBM - File  : resource_js.inc                                             //
//     - Desc  : Resource javascript functions File                          //
// 2005-08-01 Aliacom - Florent Goalabre                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget is not defined
if ($params["ext_widget"] != "") {
  $extra_js .= "

function fill_ext_form(int_form) {
   size = int_form.length;
   target = window.opener.document;
   ext_field = window.opener.document.$params[ext_widget];
   ext_form = window.opener.document.frm_sel_resource;
   for (i=0; i <size ; i++) {
     if (int_form.elements[i].type == 'checkbox'){
       if (int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
         elemid = '$params[ext_element]'+int_form.elements[i].name;
 	 elemid = 'data'+int_form.elements[i].name.substring(4);
	 span = window.document.getElementById(elemid);
         j = ext_size; is_present = false;
	 for(j=0; j< ext_size; j++) {
	   if (ext_field.options[j].value == int_form.elements[i].name) {
             is_present = true;
	   }
	 }
         if (! is_present) {
	     ext_field.options[j] = new Option(span.innerHTML,int_form.elements[i].name.substring(4));
             field = target.createElement('input');
             field.type = 'hidden';
             field.name = 'hd_resource_store[]';
             field.value = int_form.elements[i].name.substring(4);
             ext_form.appendChild(field);
         }
       }
     }
   }
   window.opener.document.frm_sel_resource.submit();
}
";
}


if ($params["ext_element"] != "") {
   require_once("$obminclude/of/of_select.inc");

$extra_js .= "
///////////////////////////////////////////////////////////////////////////////
// Fill the of_select from checked checkboxes from a form result set
// Parameters:
//   - int_form : source form where to get values (eg: from popup windows)
//   - entity   : [optionnal] entity to select [default to $module]
///////////////////////////////////////////////////////////////////////////////
function select_ritem_fill_from_checkbox(int_form, resource_id) {

  target = window.opener.document;

  var list_item = target.getElementById('list-item['+resource_id+']');
  var list_item_id = target.getElementById('list-item-id['+resource_id+']');
  var item_link = target.getElementById('a['+resource_id+']');

  // reset actual value
  list_item.value = '';
  list_item_id.value = '';
  item_link.title = '';

  size = int_form.length;
  for (i=0; i <size; i++) {
    // For each checkbox of the form, if filled we send the value
    if (int_form.elements[i].type == 'checkbox') {
      if (int_form.elements[i].checked == true) {
        id = int_form.elements[i].name;
        div_id = '$params[ext_element]'+'-data-ritem-'+id;
        // If the element not already present in the caller window
        if (! get_elem(window.opener.document, div_id )) {
          sel_id = 'data-ritem-'+id;
	        span = get_elem(window.document, sel_id);
          list_item_id.value += id +',';
          list_item.value += span.innerHTML+'\\n';
          item_link.title += span.innerHTML+', ';
        }
      }
    }
  }
}
";

}


$extra_js .= "
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function check_resource(form) {
  // MANDATORY : Check that name is not empty
  if (trim(form.tf_name.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }

  // Check the phone number
  lqty = form.tf_qty.value;
  if ((lqty != \"\") && (check_number(lqty) == null)) {
    alert (\"$l_j_check_number\");
    return false;
  }

  return true;
}


  function addFreeBusyEntities(int_form) {
    var resources = new Array();
    for (i=0; i < int_form.length; i++) {
      if (int_form.elements[i].type == 'checkbox') {
        if (int_form.elements[i].checked == true) {
          resources.push($(int_form.elements[i]).getNext().id);
        }
      }
    }
    if (resources.length > 0) {
      window.opener.obm.calendarFreeBusy.addAttendee(resources);
    }
  }










";

?>
