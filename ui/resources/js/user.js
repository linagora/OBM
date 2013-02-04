/******************************************************************************
 * Mobile device manager
 *****************************************************************************/
Obm.UserMobileDeviceManager = new Class({

  initialize: function() {
    this.devices = new Hash();
  },

  addDevice: function(id, owner, firstSeen, lastSeen, permsDeviceId, delegation) {
    var device = new Obm.UserMobileDevice(id, owner, firstSeen, lastSeen, permsDeviceId, delegation);
    this.devices.set(id, device);
  },

  showInfo: function(id) {
    var device = this.devices.get(id);
    if (device.firstSeen!=null) firstSeen = device.firstSeen;
    $$('tr.mobile').each(function(e) {
     e.set('class', 'mobile'); 
    });
    $('mobile_'+id).set('class', 'mobile info'); 
    $('mobile_info').style.visibility = '';
    $('mobile_first_sync').innerHTML = device.firstSeen;
    if ($('mobile_partnership')) {
      if (device.permsDeviceId) {
        $('mobile_partnership').value = obm.vars.labels.removePartnership;
      } else {
        $('mobile_partnership').value = obm.vars.labels.addPartnership;
      }
      $('mobile_partnership').removeEvents('click');
      $('mobile_partnership').addEvent('click', function() {
        this.togglePartnership(id)
      }.bind(this));
    }
  },

  removeMobile: function(id) {
    if (confirm(obm.vars.labels.unlink)) {
      var device = this.devices.get(id);
      device.unlikMobile(id);
    }
  },

  togglePartnership: function(id) {
    var device = this.devices.get(id);
    if (device.permsDeviceId) {
      device.removePartnership();
    } else {
      device.addPartnership();
    }
  }

});


/******************************************************************************
 * Mobile device
 *****************************************************************************/
Obm.UserMobileDevice = new Class({

  initialize: function(id, owner, firstSeen, lastSeen, permsDeviceId, delegation) {
    this.id = id;
    this.owner = owner;
    this.firstSeen = firstSeen;
    this.lastSeen = lastSeen;
    this.permsDeviceId = permsDeviceId;
    this.delegation = delegation;
  },

  addPartnership: function() {
    if(obm.vars.consts.isSuperman && this.delegation) {
      var eventData = new Object();
      eventData.owner = this.owner;
      eventData.device_id = this.id;
      new Request.JSON({
        url : obm.vars.consts.obmUrl+'/user/user_index.php',
        secure: false,
        onComplete: function(response) {
          if ($('mobile_partnership')) $('mobile_partnership').value = obm.vars.labels.removePartnership;
          if ($('mobile_'+this.id)) $('mobile_'+this.id).set('src', obm.vars.images.sync);
          this.permsDeviceId = eventData.device_id;
          showOkMessage(obm.vars.labels.updateOk);
        }.bind(this)
      }).post($merge({ajax:1, action:'add_partnership'}, eventData));
    } else {
      showErrorMessage(obm.vars.labels.permsError);
    }
  },

  removePartnership: function() {
    if (this.delegation) {
      var eventData = new Object();
      eventData.owner = this.owner;
      eventData.device_id = this.id;
      new Request.JSON({
        url : obm.vars.consts.obmUrl+'/user/user_index.php',
        secure: false,
        onComplete: function(response) {
          if ($('mobile_partnership')) $('mobile_partnership').value = obm.vars.labels.addPartnership;
          if ($('mobile_'+this.id)) $('mobile_'+this.id).set('src', obm.vars.images.desync);
          this.permsDeviceId = '';
          showOkMessage(obm.vars.labels.updateOk);
        }.bind(this)
      }).post($merge({ajax:1, action:'remove_partnership'}, eventData)); 
    } else {
      showErrorMessage(obm.vars.labels.permsError);
    }
  },

  unlikMobile: function() {
    if(obm.vars.consts.isSuperman && this.delegation) {
      var eventData = new Object();
      eventData.owner = this.owner;
      eventData.device_id = this.id;
      new Request.JSON({
        url : obm.vars.consts.obmUrl+'/user/user_index.php',
        secure: false,
        onComplete: function(response) {
          showOkMessage(obm.vars.labels.updateOk);
          if ($('mobile_'+this.id)) $('mobile_'+this.id).destroy();
        }.bind(this)
      }).post($merge({ajax:1, action:'unlink_mobile'}, eventData));
    } else {
      showErrorMessage(obm.vars.labels.permsError);
    }
  }

});

/******************************************************************************
 * User Pattern
 *****************************************************************************/
Obm.UserPattern = {};

Obm.UserPattern.Field = new Class ({

  initialize: function(form,field) {
    this.form = form;
    this.field = field;
    this.field.addEvent('keyup', this.reloadPattern.bindWithEvent(this))
              .addEvent('input', this.reloadPattern.bindWithEvent(this))
              .addEvent('paste', this.reloadPattern.bindWithEvent(this));
    this.lastReloadValue = this.getValue();
  },

  resetValue: function() {
  },

  setValue: function(value) {
  },

  getValue: function() {
    return this.field.value;
  },

  changed: function() {
    return true;
  },

  empty: function() {
    return ((this.field.value==undefined) || (this.field.value==''));
  },

  reloadPattern: function() {
    if (this.lastReloadValue != this.getValue()) {
      this.lastReloadValue = this.getValue();
      this.form.autoReload();
    }
  }

});

Obm.UserPattern.StringField = new Class ({
  Extends: Obm.UserPattern.Field,

  initialize: function(form,field) {
    this.parent(form,field);
    this.value = field.value;
    this.originalValue = field.value;
    this.lock = false;
  },

  resetValue: function() {
	this.lock = true;
	this.setValue(this.originalValue); 
	this.lock = false;
  },

  setValue: function(value) {
    this.value = value;
    this.field.value = value;
    if(!this.lock) {
      if(this.field.onchange) {
	    this.field.onchange();
	  }
	  this.field.fireEvent('change');    
    }
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
    this.lock = false;
  },

  resetValue: function() {
	this.lock = true;
    this.setValue(this.originalChecked);
    this.lock = false;
  },

  setValue: function(value) {
    if (((this.field.checked) && (value!=this.field.value)) || ((!this.field.checked) && (value==this.field.value))) {
      this.checked = this.field.checked;
      if(!this.lock) {
        if(this.field.onchange) {
		  this.field.onchange();
	    }
	    this.field.click();
	    this.field.fireEvent('change');
	  }
    }
  },

  getValue: function() {
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
	return false;
    return (!this.field.checked);
  }

});

Obm.UserPattern.PasswordField = new Class ({
  Extends: Obm.UserPattern.StringField,

  setValue: function(value) {
    this.parent(value);
    this.field = $(this.field.id);
    this.field
    var father = this.field.getParent();
    var input = new Element('input', {name: this.field.name}).setProperties(this.field.getProperties('size', 'maxlength'));
    var name = this.field.name;
    this.field.dispose();
    input.setProperties({'type': 'text', 'id' : 'passwd', 'value' : this.field.value, 'name' : name}).injectTop(father);
    this.field = input;
  }

});

Obm.UserPattern.ChoiceField = new Class ({
  Extends: Obm.UserPattern.StringField,

  initialize: function(form,field) {
    this.parent(form,field);
    this.value = field.options[this.field.selectedIndex].value;
    this.originalValue = this.value;
    this.lock = false;
  },

  resetValue: function() {
	this.lock = true;
    this.setValue(this.originalValue);
    this.lock = false;
  },

  setValue: function(value) {
    for (var i=0; i<this.field.options.length; i++) {
      if (this.field.options[i].value == value) {
        this.field.selectedIndex = i;
        this.value = value;
        if(!this.lock) {
		  if(this.field.onchange) {
		    this.field.onchange();
		  }
		  this.field.fireEvent('change');
		}
      }
    }
  },

  changed: function() {
    return (this.value != this.field.options[this.field.selectedIndex].value);
  }

});

/* very specific field */
Obm.UserPattern.NomadeField = new Class ({
  Extends: Obm.UserPattern.Field,

  initialize: function(form) {
    this.form = form;
    this.value = [];
    var mail_fields = $$('td#nomadeMailHome input');
    for (var i=0; i<mail_fields.length; i++) {
      this.value[this.value.length] = mail_fields[i].value;
    }
    if ((this.value==undefined) || (this.value=='') || (this.value.join('-')=='')) {
      this.value = [''];
    }
    this.originalValue = this.value;
    this.lastReloadValue = this.value;
  },

  resetValue: function() {
    var fields = $$('td#nomadeMailHome div');
    for (var i=0; i<fields.length; i++) {
      remove_element(fields[i],'nomadeMailHome');
      show_hide_nomade_add_button();
    }
    for (var i=0; i<this.originalValue.length; i++) {
      add_nomade_email_field(aliasSelectTemplate);
      show_hide_nomade_add_button();
    }
    var mail_fields = $$('td#nomadeMailHome input');
    for (var i=0; i<this.originalValue.length; i++) {
      mail_fields[i].value = this.originalValue[i];
    }
  },

  setValue: function(value) {
    if ((value==undefined) || (value=='') || (value.join('-')=='')) {
      value = [''];
    }
    this.value = value;
    var fields = $$('td#nomadeMailHome div');
    for (var i=0; i<fields.length; i++) {
      remove_element(fields[i],'nomadeMailHome');
      show_hide_nomade_add_button();
    }
    for (var i=0; i<value.length; i++) {
      add_nomade_email_field(aliasSelectTemplate);
      show_hide_nomade_add_button();
    }
    var mail_fields = $$('td#nomadeMailHome input');
    for (var i=0; i<value.length; i++) {
      mail_fields[i].value = value[i];
    }
  },

  getValue: function() {
    var value = [];
    var mail_fields = $$('td#nomadeMailHome input');
    for (var i=0; i<mail_fields.length; i++) {
      value[value.length] = mail_fields[i].value;
    }
    return value;
  },

  changed: function() {
    return (this.value.join('-')!=this.getValue().join('-'));
  },

  empty: function() {
    return (this.getValue().join('-')=='');
  }

});

Obm.UserPattern.Form = new Class ({
  Implements: Options,

  options: {
    url: '',
    delay: 400                      // delay before the last key pressed and the request
  },

  initialize: function(options) {
    this.setOptions(options);
    this.form = $('dataUser');
    this.userpattern_id = 0;
    this.request_id = 0;
    this.fields = {};
    this.addField('kind','userKind','Field');
    this.addField('lastname','userLastname','Field');
    this.addField('firstname','userFirstname','Field');
    this.addField('login','userLogin');
    this.addField('commonname','userCommonname');
    this.addField('passwd','passwd','PasswordField');
    this.addField('hidden','cba_hidden','BooleanField');
    this.addField('profile','sel_profile', 'ChoiceField');
    this.addField('delegation','delegationField');
    this.addField('delegation_target','delegationTargetField');
    this.addField('title','userTitle');
    this.addField('datebegin','userDatebegin');
    this.addField('noexperie','noexperie','BooleanField');
    this.addField('dateexp','userDateexp');
    this.addField('phone','userPhone');
    this.addField('phone2','userPhone2');
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
    if ($('nomadeMailHome')) {
      this.addField('nomade_perms','cb_nomade_perms','BooleanField');
      this.addField('nomade_enable','cb_nomade_enable','BooleanField');
      this.addField('nomade_local_copy','cb_nomade_local_copy','BooleanField');
      this.fields['email_nomade'] = new Obm.UserPattern.NomadeField(this);
    }
  },

  addField: function(name,id,type) {
    if (!type)
      type = 'StringField';
    var field = $(id);
    if (field) {
      this.fields[name] = new Obm.UserPattern[type](this,field);
    }
  },

  getField: function(name) {
    return this.fields[name];
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
      this.resetForm();
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

  resetForm: function() {
    for (var attr in this.fields) {
      var field = this.fields[attr];
      if (!field.changed()) {
        field.resetValue();
      }
    }
  },

  applyPattern: function(attributes) {
    this.resetForm();
    if (attributes) {
      for (var attr in attributes) {
          var field = this.fields[attr];
          if (field && ((!field.changed()) || (field.empty()))) {
            if (attr == 'login' ) {
              attributes[attr] = attributes[attr].replace(/(\s+)/g,'-');
            }
            field.setValue(attributes[attr].trim());
          } 
      }
      /* mail block specific */
     
      if (typeof this.fields['mail_perms']!= "undefined") {
        if (this.fields['mail_perms'].getValue()==0) {
          if (attributes['email']) {
            $('externalEmailField').value = attributes['email'];
          }
        } else {
          if (attributes['mail_server_id']) {
            var field = $('sel_mail_server_id');
            for (var i=0; i<field.options.length; i++) {
              if (field.options[i].value == attributes['mail_server_id']) {
                field.selectedIndex = i;
              }
            }
          }
          if (attributes['email']) {
            var mails = attributes['email'];
            var mail_fields = $$('td#userMailHome input');
            var count = mails.length;
            for (var i=mail_fields.length; i<mails.length; i++) {
              add_email_field(aliasSelectTemplate);
              show_hide_add_button();
            }
            mail_fields = $$('td#userMailHome input');
            var alias_fields = $$('td#userMailHome select');
            for (var i=0; i<mails.length; i++) {
              var tmp = mails[i].split("@");
              tmp[0] = tmp[0].replace(/(\s+)/g,'-');
              mail_fields[i].value = tmp[0];
              for (var j=0; j<alias_fields[i].options.length; j++) {
                if (alias_fields[i].options[j].value == tmp[1]) {
                  alias_fields[i].selectedIndex = j;
                }
              }
            }
          }
        }
      } 
    }
  }

});

