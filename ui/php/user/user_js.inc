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
function confirm_del(form) {
  return confirm('$l_delete_confirm');
}

function check_user(form) {

  // MANDATORY : Check that Login is not empty
  if (trim(form.tf_login.value) == \"\") {
    alert (\"$l_fill_login\");
    return false;
  }

  // UPDATE : check if login change
  if ((form.tf_old_login !== undefined) && (form.tf_old_login.value != \"\") && (form.tf_login.value != form.tf_old_login.value)) {
    if (!confirm(\"$l_change_login_confirm\")) {
      return false;
    }
  }

  return true;
}

// Check if phone and phone2 fields haven't the same number
function check_phoneFax_number(form) {
  var errMsg ='';
  var ret = true;
  
  var phone1 = form.tf_phone.value;
  var phone2 = form.tf_phone2.value;
  var fax1 = form.tf_fax.value;
  var fax2 = form.tf_fax2.value;

  // Check phones number
  if (phone1 != '' && phone2 != '' && phone1 == phone2) {
    errMsg += '$l_j_coord_same_phone_error\\n';
    ret = false
  }
  
  // Check faxes number
  if (fax1 != '' && fax2 != '' && fax1 == fax2) {
    errMsg += '$l_j_coord_same_fax_error';
    ret = false
  }
  
  if (!ret) {
    alert(errMsg);
  }
  
  return ret;
}


function delete_user_photo() {
  $('block_photo_detail').dispose();

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

function add_nomade_email_field() {
  var mailHome = $('nomadeMailHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'nomadeMailHome');
              show_hide_nomade_add_button();
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  mailHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').addClass('emailNomade').setProperty('name','tf_email_nomade[]').setProperty('type','text'));
  
}

function show_hide_nomade_add_button() {
  var button = $('addMailNomadeButton'); 
  var parent = $('nomadeMailHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_email_nomade] > 0) {
    if(childs.length >= $GLOBALS[c_max_email_nomade] && button) {
      button.dispose();
   } else if(childs.length < $GLOBALS[c_max_email_nomade] && !button) {
      $('nomadeMailLabel').adopt((new Element('a').addEvent('click', function () {
                add_nomade_email_field();
                show_hide_nomade_add_button();
              }).setProperty('id','addMailNomadeButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
    }
  }
}

function show_hide_add_button() {
  var button = $('addMailButton'); 
  var parent = $('userMailHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_user_alias] > 0) {
    if(childs.length >= $GLOBALS[c_max_user_alias] && button) {
      button.dispose();
    } else if(childs.length < $GLOBALS[c_max_user_alias] && !button) {
      $('userMailLabel').adopt((new Element('a').addEvent('click', function () {
                add_email_field(aliasSelectTemplate);
                show_hide_add_button();
              }).setProperty('id','addMailButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
    }
  }
}

function fill_coords(element) {
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
        var link = new Element('a').setProperty('id', 'button' + name).setStyle('cursor','pointer').adopt(new Element('img').setProperties({'src': ico, 'class': 'icons'}));
        link.addEvent('click', function(evt) {
            disp_addRemove_button(this);
            set_toUpdate(this);
          }.bind(link));          
        th_element.adopt(link);
      }
    }
  }
}

function disp_addRemove_button(link) {
  var img = link.getElement('img');
  if (img.getProperty('src') == '$ico_add') {
    img.setProperty('src', '$ico_delete');
  } else {
    img.setProperty('src', '$ico_add');
  }
}

function set_toUpdate(link) {
  var td_element = link.getParent().getNext();
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

function switch_mail_mode() {
  if($('userMailActive').checked == true) {
    external.dispose();
    external.addClass('H');
    internal.removeClass('H');
    internal.injectInside($('userMail'));
  } else {
    //$('userMailHome').set('html','');
    //add_email_field(aliasSelectTemplate); 
    //show_hide_add_button();    
    internal.dispose();
    internal.addClass('H');
    external.removeClass('H');
    external.getElement('input').setProperty('disabled',false);
    external.injectInside($('userMail'));
  }
}


function switch_samba_mode() {
  if($('userSambaActive').checked == true) {
    samba.removeClass('H');
    samba.injectInside($('userSamba'));

  } else {
    samba.addClass('H');
  }
}

function sambaHomeCompute() {
  var login = $('userLogin').value;
  var hidden = $('userSambaHiddenHome').value;
  $('userSambaHome').value = hidden.replace(/%u/g, login);
}

function random_password() {
  var mdp = '';
  var length = 10;
  var i = 0;
  var charset = new Array();
  charset[0] = new Array('a','b','c','d','e','f','g','h','j','k','m','n','p','q','r','s','t','u','v','w','x','y','z');
  charset[1] = new Array('A','B','C','D','E','F','G','H','J','K','L','M','N','P','Q','R','S','T','U','V','W','X','Y','Z');
  charset[2] = new Array('0','1','2','3','4','5','6','7','8','9');
  charset[3] = new Array('[','-','&','~','#','{','(','[','|','`','_','^','@',')',']','=','+','}','$','%','>',',','?',';','.',':','!','/',']');
  var rules = Array();
  var ruleIndex = Math.floor(Math.random() * 5);
  rules[ruleIndex] = 2;
  ruleIndex = Math.floor(Math.random() * 3) + 5;
  rules[ruleIndex] = 0;
  ruleIndex = Math.floor(Math.random() * 3) + 8;
  rules[ruleIndex] = 1;

  while(i <= length ) {
    var charsetIndex;
    if(rules[i]) {
      charsetIndex = rules[i];
    } else {
      charsetIndex = Math.floor(Math.random() * 4);
    }
    mdp += charset[charsetIndex][Math.floor(Math.random() * charset[charsetIndex].length)];
    i++;
  }
  return mdp;
}

function generate_passwd() {
  var mdp = random_password();
  var input = new Element('input', {name: $('passwd').name}).setProperties($('passwd').getProperties('size', 'maxlength'));
  $('passwd').dispose();
  input.setProperties({'type': 'text', 'id' : 'passwd', 'value' : mdp}).inject($('td_passwd'), 'top');
}

function modif_password() {
  var passwd = $('passwd').value;
  if (passwd == '') {
    generate_passwd();
  } else {
    obm.popup.show('popupModifPassword');
  }
};

function get_profile_quota(field) {
  var val = field.options[field.selectedIndex].value;
  var userpattern = this.theUserForm.userpattern_id;
  new Request.JSON({
        url: obm.vars.consts.obmUrl+'/user/user_index.php',
        secure: false,
        async: true,
        onComplete: function(response) {
            var elem = $('tf_mail_quota');
            if(elem){
              if(response.quota != '') {
                elem.value = response.quota; 
              } else {
                elem.value = obm.vars.consts.domaindefaultquota;
              }
            }
        }
  }).get({profile_name : val, action : 'profile_quota', user_pattern : userpattern});
}

";

?>
