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

function switch_mail_mode() {
  if($('userMailActive').checked == true) {
    external.dispose();
    external.addClass('H');
    internal.removeClass('H');
    internal.injectInside($('userMail'));

  } else {
    $('userMailHome').set('html','');
    add_email_field(aliasSelectTemplate); 
    show_hide_add_button();    
    internal.dispose();
    internal.addClass('H');
    external.removeClass('H');
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
  var i = 0;
  var length = 10;
  var allowed_char = 'abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ0123456789[-&~#{(\[|`_^@)\]=+}$%>,?;.:!\/])';
  var test = false;
  while(i<length) {
    var nb = Math.floor(Math.random() * allowed_char.length);
    var carac = allowed_char[nb];
    mdp += carac;
    i++;
  }

  return mdp;
}

function generate_passwd() {
  /*var valid_carac = new RegExp(\"/^([a-z]|[0-9]|[A-Z]|[-&~#{(\[|`_^@)\]=+}$%>,?;.:!\/])*$/\");
  var valid_type_carac = new RegExp(\"/^([a-z]|[A-Z]){0,4}([0-9]|[-&~#{(\[|`_^@)\]=+}$%>,?;.:!\/])([a-z]|[0-9]|[A-Z]|[-&~#{(\[|`_^@)\]=+}$%>,?;.:!\/])*$/\");
  var valid_cinq = new RegExp(\"/^(?(?=.*[-&~#{(\[|`_^@)\]=+}$%>,?;.:!\/].*)(?(?=.*[A-Z].*).*([0-9]|[a-z]).*|(?=.*[0-9].*).*[a-z].*)|(?=.*[A-Z].*)(?=.*[0-9].*).*[a-z].*)$/\")
  var test = true;*/
  var i=0;
  //while(test = true && i<1000){
    var mdp = random_password();
    /*test = valid_carac.test(mdp);
    test = valid_type_carac(mdp);
    i++;
  }*/
  $('passwd').setProperty('type','text');
  $('passwd').value=mdp;
}

function modif_password() {
  var passwd = $('passwd').value;
  if (passwd == '') {
    generate_passwd();
  } else {
    obm.popup.show('popupModifPassword');
  }
}
";

?>
