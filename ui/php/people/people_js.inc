<?php
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
  	   if('data-u-' + ext_field.options[j].value == int_form.elements[i].name) {
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
