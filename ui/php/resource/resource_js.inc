<?php
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

";

?>
