<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : user_js.inc                                                 //
//     - Desc  : User javascript functions File                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
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

function fill_coords() {
  var element = $('coord');
  var childs = element.getChildren()[0].getChildren();
  
  for (var i=0; i<childs.length; i++) {
    var child = childs[i];
    var name = child.getProperty('id');
    
    if (name != '') {
      var th_element = child.getElement('th');
      var td_element = child.getElement('td');
      
      if (th_element != null) {
      
        var ico;
        
        for (var j=0; j<td_element.getChildren().length; j++) {
          var temp_element = td_element.getChildren()[j];
          var hidden_element;
          if (temp_element.getProperty('type') == 'hidden') {
            hidden_element = temp_element;
            if (temp_element.value == 'false') {
              ico = '$ico_add';
            } else {
              ico = '$ico_delete';
            }
          } else if (temp_element.getProperty('type') == 'text') {
            if (hidden_element.value == 'false') {
              temp_element.style.display = 'none';
            } else {
              temp_element.style.display = 'block';
            }
          }
        }
          
        th_element.adopt(new Element('a').setProperties(
        {
          'onclick' : 'disp_addRemove_button(\''+name+'\'); set_toUpdate(\''+name+'\');',
          'id'      : 'button'+name
        }).adopt(new Element('img').setProperties({'src': ico, 'class': 'icons'})));
      }
    }
  }
}

function disp_addRemove_button(name) {
  var img = $('button'+name).getElement('img');
  if (img.getProperty('src') == '$ico_add') {
    img.setProperty('src', '$ico_delete');
  } else {
    img.setProperty('src', '$ico_add');
  }
}

function set_toUpdate(name) {
  var td_element = $(name).getElement('td');
  for (var j=0; j<td_element.getChildren().length; j++) {
    var temp_element = td_element.getChildren()[j];
    if (temp_element.getProperty('type') == 'hidden') {
      if (temp_element.value == 'false') {
        temp_element.value = 'true';
      } else {
        temp_element.value = 'false';
      }
    } else if (temp_element.getProperty('type') == 'text') {
      if (temp_element.style.display == 'none') {
        temp_element.style.display = 'block';
      } else {
        temp_element.style.display = 'none';
      }
    }
  }
}
";

?>
