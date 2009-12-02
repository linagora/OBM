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
  }

});
