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
  charset[0] = 'abcdefghjkmnpqrstuvwxyz';
  charset[1] = 'ABCDEFGHJKLMNPQRSTUVWXYZ';
  charset[2] = '0123456789';
  charset[3] = '[-&~#{(\[|`_^@)\]=+}$%>,?;.:!\/])';
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
  var father = $('passwd').getParent();
  var input = new Element('input').setProperties($('passwd').getProperties('name', 'size', 'maxlength'));
  $('passwd').dispose();
  input.setProperties({'type': 'text', 'id' : 'passwd', 'value' : mdp}).injectTop(father);
}

function modif_password() {
  var passwd = $('passwd').value;
  if (passwd == '') {
    generate_passwd();
  } else {
    obm.popup.show('popupModifPassword');
  }
};

Obm.vars.consts.userPatternUrl = '$path/userpattern/userpattern_index.php';

Obm.UserPattern = {};

Obm.UserPattern.Field = new Class ({

  initialize: function(form,field) {
    this.form = form;
    this.field = field;
    this.field.addEvent('keyup', this.form.autoReload.bindWithEvent(this.form))
              .addEvent('input', this.form.autoReload.bindWithEvent(this.form))
              .addEvent('paste', this.form.autoReload.bindWithEvent(this.from));
  },

  resetValue: function() {
  },

  setValue: function(value) {
  },

  getValue: function(value) {
    return this.field.value;
  },

  changed: function() {
    return true;
  },

  empty: function() {
    return ((this.field.value==undefined) || (this.field.value==''));
  }

});

Obm.UserPattern.StringField = new Class ({
  Extends: Obm.UserPattern.Field,

  initialize: function(form,field) {
    this.parent(form,field);
    this.value = field.value;
    this.originalValue = field.value;
  },

  resetValue: function() {
    this.field.value = this.originalValue;
  },

  setValue: function(value) {
    this.value = value;
    this.field.value = value;
  },

  changed: function() {
    return (this.value!=this.field.value);
  }

});

Obm.UserPattern.BooleanField = new Class ({
  Extends: Obm.UserPattern.Field,

  initialize: function(form,field) {
    this.parent(form,field);
    this.checked = field.checked;
    this.originalChecked = field.checked;
  },

  resetValue: function() {
    this.field.checked = this.originalChecked
  },

  setValue: function(value) {
    if (((this.field.checked) && (value!=this.field.value)) || ((!this.field.checked) && (value==this.field.value))) {
      this.field.click();
      this.checked = this.field.checked;
    }
  },

  getValue: function(value) {
    if (this.field.checked) {
      return this.field.value;
    } else {
      return 0;
    }
  },

  changed: function() {
    return (this.checked!=this.field.checked);
  },

  empty: function() {
    return (!this.field.checked);
  }

});

Obm.UserPattern.PasswordField = new Class ({
  Extends: Obm.UserPattern.StringField,

  setValue: function(value) {
    this.parent(value);
    this.field.type = 'text';
  }

});

Obm.UserPattern.Form = new Class ({
  Implements: Options,

  options: {
    url: Obm.vars.consts.userPatternUrl,
    delay: 400                      // delay before the last key pressed and the request
  },

  initialize: function(options) {
    this.setOptions(options);
    this.form = $('dataUser');
    this.userpattern_id = 0;
    this.request_id = 0;
    this.fields = {
      'kind':  new Obm.UserPattern.Field(this,$('userKind')),
      'lastname': new Obm.UserPattern.Field(this,$('userLastname')),
      'firstname': new Obm.UserPattern.Field(this,$('userFirstname'))
    };
    this.addField('login','userLogin');
    this.addField('passwd','passwd','PasswordField');
    this.addField('hidden','cba_hidden','BooleanField');
    /* this.addField('profil',''); FIXME: profil */
    this.addField('delegation','delegationField');
    this.addField('delegation_target','delegationTargetField');
    this.addField('title','userTitle');
    this.addField('datebegin','userDatebegin');
    this.addField('noexperie','noexperie','BooleanField');
    this.addField('dateexp','userDateexp');
    this.addField('phone','userPhone');
    this.addField('phone2','userPhone');
    this.addField('mobile','userMobile');
    this.addField('fax','userFax');
    this.addField('fax2','userFax2');
    this.addField('company','userCompany');
    this.addField('direction','userDirection');
    this.addField('service','userService');
    this.addField('ad1','userAd1');
    this.addField('ad2','userAd2');
    this.addField('ad3','userAd3');
    this.addField('zip','userZip');
    this.addField('town','userTown');
    this.addField('cdx','userCdx');
    this.addField('desc','userDesc');
    this.addField('web_perms','cb_web_perms','BooleanField');
    this.addField('mail_perms','userMailActive','BooleanField');
    /*this.addField('mail_server_id',''); FIXME: mail_server_id */
    /*this.addField('email',''); FIXME: mail_server_id */
    this.addField('mail_quota','tf_mail_quota');
    this.addField('email_nomade','tf_email_nomade');
    this.addField('nomade_perms','cb_nomade_perms','BooleanField');
    this.addField('nomade_enable','cb_nomade_enable','BooleanField');
    this.addField('nomade_local_copy','cb_nomade_local_copy','BooleanField');
  },

  addField: function(name,id,type) {
    if (!type)
      type = 'StringField';
    var field = $(id)
    if (field) {
      this.fields[name] = new Obm.UserPattern[type](this,field);
    }
  },

  setPattern: function(userpattern_id) {
    this.userpattern_id = userpattern_id;
    this.loadPattern();
  },

  loadPattern: function() {
    if (this.userpattern_id) {
      var params = {};
      for (var attr in this.fields) {
        var field = this.fields[attr];
        if ((field.changed()) && (!field.empty())) {
          params[attr] = field.getValue();
        }
      }
      this.request_id++;
      new Request.JSON({
        url : this.options.url,
        secure : false,
        onSuccess:this.onQuerySuccess.bindWithEvent(this,[this.request_id])
      }).post({
        action: 'apply',
        userpattern_id: this.userpattern_id, 
        attributes: params
      });
    } else {
      for (var attr in this.fields) {
        var field = this.fields[attr];
        if (!field.changed()) {
          field.resetValue();
        }
      }
    }
  },

  onQuerySuccess: function(response, response_id) {
    if (response_id==this.request_id) {
      if (!response.err) {
        this.applyPattern(response.attributes);
      }
    }
  },

  autoReload: function() {
    if (this.fetchDelay) {
      this.fetchDelay = $clear(this.fetchDelay);
    }
    this.fetchDelay = this.loadPattern.delay(this.options.delay,this);
  },

  applyPattern: function(attributes) {
    for (var attr in attributes) {
      var field = this.fields[attr];
      if (field && ((!field.changed()) || (field.empty()))) {
        field.setValue(attributes[attr]);
      }
    }
  }

});


function get_profile_quota(field) {
  var val = field.options[field.selectedIndex].value;;
  new Request.JSON({
        url: obm.vars.consts.obmUrl+'/user/user_index.php',
        secure: false,
        async: true,
        onComplete: function(response) {
            var elem = $('tf_mail_quota');
            if(elem){
              elem.value = response.quota; 
            }
        }
  }).get({profile_name : val, action : 'profile_quota'});
}

";

?>
