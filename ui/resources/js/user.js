/******************************************************************************
 * Mobile device manager
 *****************************************************************************/
Obm.UserMobileDeviceManager = new Class({

  initialize: function() {
    this.devices = new Hash();
  },

  addDevice: function(id, owner, firstSeen, lastSeen, permsDeviceId) {
    var device = new Obm.UserMobileDevice(id, owner, firstSeen, lastSeen, permsDeviceId);
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

  initialize: function(id, owner, firstSeen, lastSeen, permsDeviceId) {
    this.id = id;
    this.owner = owner;
    this.firstSeen = firstSeen;
    this.lastSeen = lastSeen;
    this.permsDeviceId = permsDeviceId;
  },

  addPartnership: function() {
    var eventData = new Object();
    eventData.owner = this.owner;
    eventData.device_id = this.id;
    new Request.JSON({
      url : '/user/user_index.php',
      secure: false,
      onComplete: function(response) {
        $('mobile_partnership').value = obm.vars.labels.removePartnership;
        var device = obm.userMobileDeviceManager.devices.get(this.id);
        device.permsDeviceId = eventData.device_id;
      }.bind(this)
    }).post($merge({ajax:1, action:'add_partnership'}, eventData)); 
  },

  removePartnership: function() {
    var eventData = new Object();
    eventData.owner = this.owner;
    eventData.device_id = this.id;
    new Request.JSON({
      url : '/user/user_index.php',
      secure: false,
      onComplete: function(response) {
        $('mobile_partnership').value = obm.vars.labels.addPartnership;
        var device = obm.userMobileDeviceManager.devices.get(this.id);
        device.permsDeviceId = '';
      }.bind(this)
    }).post($merge({ajax:1, action:'remove_partnership'}, eventData)); 

  }

});
