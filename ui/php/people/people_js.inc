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
// OBM - File  : user_js.inc                                                 //
//     - Desc  : User javascript functions File                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id: user_js.inc 2865 2008-04-09 14:27:51Z mehdi $ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget is not defined
if ($params["ext_widget"] != "") {
  $extra_js .= "

  function fill_ext_form(int_form) {
     size = int_form.length;
     ext_field = window.opener.document.$params[ext_widget];
     for(i=0; i <size ; i++) {
       if(int_form.elements[i].type == 'checkbox'){
         if(int_form.elements[i].checked == true) {
  	 ext_size = ext_field.length;
  	 for(j=0; j< ext_size; j++) {
  	   if('data-user-' + ext_field.options[j].value == int_form.elements[i].name) {
  	     window.opener.document.$params[ext_widget].options[j].selected =true;
  	   }
  	 }
         }
       }
     }
  }";

}

if ($params["ext_widget"] != "" && $params["ext_widget_text"] != ""){

  $extra_js .= "
  function check_user_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_user\");
      return false;
    } else {
      window.opener.document.$params[ext_widget].value=valeur;
      window.opener.document.$params[ext_widget_text].value=text;
      window.close();
      return true;
    }
  }";
}

$extra_js .="


  function select_fill_from_checkbox(int_form) {
  
    var container = '$params[ext_container]';
    size = int_form.length;
    for (i=0; i <size; i++) {
      // For each checkbox of the form, if filled we send the value
      if (int_form.elements[i].type == 'checkbox') {
        if (int_form.elements[i].checked == true) {
          id = $(int_form.elements[i]).getNext().id;
          div_id = '$params[ext_element]-'+id;
          // If the element not already present in the caller window
          if (! get_elem(window.opener.document, div_id )) {
   	        sel_id = 'data-'+id;
            // Get the label associated to the id from the int_form row
            // hidden in an hidden span
  	        span = get_elem(window.document, id);
  	        if (container == '' || container == 'div') {
  	          of_select_add_element(window.opener.document, '$params[ext_element]', id, span.innerHTML);
            }
            if (container == 'table') {
              of_select_add_element_table(window.opener.document, '$params[ext_element]', id, span.innerHTML);
            }
          }
        }
      }
    }
  }


  function addFreeBusyEntities(int_form) {
    var people = new Array();
    for (i=0; i < int_form.length; i++) {
      if (int_form.elements[i].type == 'checkbox') {
        if (int_form.elements[i].checked == true) {
          people.push($(int_form.elements[i]).getNext().id);
        }
      }
    }
    if (people.length > 0) {
      window.opener.obm.calendarFreeBusy.addAttendee(people);
    }
  }

  function check_user_get_id_url(p_url, valeur) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_user\");
      return false;
    } else {
      new_url = p_url + valeur;
      window.opener.location.href=new_url;
      window.close();
      return true;
    }
  }
";


if ($params["ext_element"] != "") {
  require_once("$obminclude/of/of_select.inc");
}


$extra_js .= "
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function check_user(form) {

  // MANDATORY : Check that Login is not empty
  if (trim(form.tf_login.value) == \"\") {
    alert (\"$l_fill_login\");
    return false;
  }

  // UPDATE : check if login change
  if ((form.tf_old_login.value != \"\") && (form.tf_login.value != form.tf_old_login.value)) {
    return confirm(\"$l_change_login_confirm\");
  }

  // Check the email
  lemail = form.tf_email.value;
  if ((lemail != \"\") && (check_email(lemail) == null)) {
    alert (\"$l_j_check_email\");
    return false;
  }

  return true;
}

function delete_user_photo() {
  $('block_photo_detail').remove();

  return false;
} 

function add_email_field(template) {

  var mailHome = $('userMailHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'userMailHome');
              show_hide_add_button();
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  mailHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').setProperty('name','tf_email[]').setProperty('type','text'));
  div.appendText(' @ ');
  div.adopt(template.clone());
  
}

function show_hide_add_button() {
  var button = $('addMailButton'); 
  var parent = $('userMailHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_user_alias] > 0) {
    if(childs.length >= $GLOBALS[c_max_user_alias] && button) {
      button.remove();
    } else if(childs.length < $GLOBALS[c_max_user_alias] && !button) {
      $('userMailLabel').adopt((new Element('a').addEvent('click', function () {
                add_email_field(aliasSelectTemplate);
                show_hide_add_button();
              }).setProperty('id','addMailButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
    }
  }
}
";

?>
