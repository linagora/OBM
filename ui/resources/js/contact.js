Obm.Contact = {}
obm.contact = {};
//
Obm.Contact.syncContact = function(id, tag) {
  tag = $(tag);
  if(tag.getProperty('x-obm-sync') == '1') {
    act = 'desync';
  } else {
    act = 'sync';
  }
  new Request.JSON({
    url : 'contact_index.php',
    secure : false,
    onComplete : function(response) {
      if(!response.error) {
        showOkMessage(response.message);
        if(response.newact == 'sync') {
          tag.setProperty('value', obm.vars.labels.sync);
          tag.setProperty('title', obm.vars.labels.sync);
          tag.setProperty('x-obm-sync', 0);
          if(tag.getElement('img'))
            tag.getElement('img').setAttribute('src', obm.vars.images.desync);
        } else {
          tag.setProperty('value', obm.vars.labels.desync);
          tag.setProperty('title', obm.vars.labels.desync);
          tag.setProperty('x-obm-sync', 1);
          if(tag.getElement('img'))
            tag.getElement('img').setAttribute('src', obm.vars.images.sync);
        }
      } else {
        showErrorMessage(response.message);
      }
    }.bind(this)
  }).post({ajax : 1, action : act, contact_id : id});
};

Obm.Contact.AddressBook = new Class ({

  initialize: function() {
    $('contactPanel').getElements('div.contactPanelContainer > table > tbody').setStyle('height',window.innerHeight - $('contactPanel').offsetTop - 100);
    new Obm.Observer(new Window(window), {property:'innerHeight', onStop:function() {
      $('contactPanel').getElements('div.contactPanelContainer > table > tbody').setStyle('height',window.innerHeight - $('contactPanel').offsetTop - 100);
    }});

    this.addressBookRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('addressBookContainer'),
    });
    this.addressBookRequest.addEvent('complete', function () {
      $('contactPanel').getElements('div.contactPanelContainer > table > tbody').setStyle('height',window.innerHeight - $('contactPanel').offsetTop - 100);
    }.bind(this));

    this.contactRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('dataContainer'),
    });
    this.contactRequest.addEvent('complete', function () {
      $('contactPanel').getElements('div.contactPanelContainer > table > tbody').setStyle('height',window.innerHeight - $('contactPanel').offsetTop - 100);
    }.bind(this));

    this.dataRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('informationContainer'),
      onFailure: function (response) {
        var errors = JSON.decode(response.responseText, false);
        errors.error = new Hash(errors.error);
        errors.warning = new Hash(errors.warning);
        Obm.Error.formUpdate(errors, this.dataRequest);
      }.bind(this)
    });    
    this.dataRequest.addEvent('complete', function () {
      $('contactPanel').getElements('div.contactPanelContainer > table > tbody').setStyle('height',window.innerHeight - $('contactPanel').offsetTop - 100);
    }.bind(this));

  },

  selectContact: function(elem) {
    if(elem.hasClass('current')) {
      this.hideContact();
    } else {
      var id = elem.get('id').split('-')[1];
      $('dataContainer').getElements('tr.current').removeClass('current');
      elem.addClass('current');
      this.consultContact(id);
    }
  },

  hideContact: function() {
    $('dataContainer').getElements('tr.current').removeClass('current');
    $('dataGrid').getElements('td').setStyle('display','');
    $('informationGrid').setStyle('display','none'); 
    $('dataContainer').removeClass('shrinked');
    $('dataContainer').addClass('expanded');
    $('dataGrid').setStyle('width', 'auto');
  },

  consultContact: function(id) {
    $('informationGrid').setStyle('display',''); 
    this.dataRequest.get({ajax : 1, action : 'consult', id : id}); 
    $('dataGrid').getElements('td').setStyle('display','none');
    $('dataContainer').removeClass('expanded');
    $('dataContainer').addClass('shrinked');    
    $('dataGrid').setStyle('width', '20em');
  },

  selectAddressBook: function(elem) {
    if(!elem.hasClass('current')) {
      var id = elem.get('id').split('-')[1];
      this.hideContact();
      this.contactRequest.get({ajax : 1, action : 'list', id : id}); 
      $('addressBookGrid').getElements('td.current').removeClass('current');
      elem.addClass('current');
    }
  },

  updateContact: function(id) {
    $('informationGrid').setStyle('display',''); 
    this.dataRequest.get({ajax : 1, action : 'updateContact', id : id}); 
    $('dataContainer').getElements('td').setStyle('display','none');
    $('dataContainer').removeClass('expanded');
    $('dataContainer').addClass('shrinked');
  },  

  addContact: function(id) {
    $('informationGrid').setStyle('display',''); 
    this.dataRequest.get({ajax : 1, action : 'updateContact'}); 
    $('dataContainer').getElements('td').setStyle('display','none');
    $('dataContainer').removeClass('expanded');
    $('dataContainer').addClass('shrinked');
  },  

  storeContact: function(form, id) {
    $('informationGrid').setStyle('display',''); 
    if(id) {
      this.dataRequest.post(form); 
    } else {
      this.dataRequest.post(form);
    }
    $('dataContainer').getElements('td').setStyle('display','none');
    $('dataContainer').removeClass('expanded');
    $('dataContainer').addClass('shrinked');
  },    

  searchContact: function(form) {
    this.hideContact();
    this.contactRequest.get(form); 
    $('addressBookGrid').getElements('td.current').removeClass('current');
  },

  addAddressBook: function() {
    // Toggle form visibility
    if ($('f_addressbook').style.visibility == 'visible') {
      $('f_addressbook').style.visibility = 'hidden';
    } else {
      $('f_addressbook').style.visibility = 'visible';
    }
    $('tf_addressbook').value = '';
  },

  updateAddressBook: function(id) {
    $('addressbook_'+id).style.display='none';
    $('tf_addressbook_'+id).style.display='';
  },

  storeAddressBook: function(id) {
    if (!$chk(id)) {
      if (!$chk($('tf_addressbook').value)) {
        showErrorMessage(obm.vars.labels.insertError);
      } else {
        this.addressBookRequest.addEvent('complete', function() {
          showOkMessage(obm.vars.labels.insertOk);
          $('tf_addressbook').value = '';
          $('f_addressbook').style.visibility = 'hidden';
        });
        this.addressBookRequest.post({ajax:1, action:'storeAddressBook', 'tf_name':$('tf_addressbook').value});
      }
    } else {
      if (!$chk($('tf_addressbook_'+id).value)) {
        showErrorMessage(obm.vars.labels.updateError);
      } else {
        this.addressBookRequest.addEvent('complete', function() {
          showOkMessage(obm.vars.labels.updateOk);
        });
        this.addressBookRequest.post({ajax:1, action:'updateAddressBook', 'id':id, 'name':$('tf_addressbook_'+id).value});
      }
    }
  },

  deleteAddressBook: function(id, name) {
    if(confirm(obm.vars.labels.confirmDeleteAddressBook+'\''+name+'\' ?')){
      this.addressBookRequest.addEvent('complete', function() {
        showOkMessage(obm.vars.labels.deleteOk);
      });      
      this.addressBookRequest.post({ajax:1, action:'deleteAddressBook', 'addressbook_id':id});
    }
  },

  toggleSync: function(id) {
    this.addressBookRequest.addEvent('complete', function() {
      showOkMessage(obm.vars.labels.updateOk);
    });
    this.addressBookRequest.post({ajax:1, action:'toggleSync', 'id':id});
  }
});


Obm.Contact.PhoneWidget = new Class ({
  kind : 'phones',
  structure : {
    label: { kind: 'label', value: 'CELL_VOICE', newLine : true, label : obm.vars.labels.phoneLabel.WORK_VOICE}, 
    number: { kind: 'text', value: '', newCell : true, label : obm.vars.labels.phoneNumber},
  },

  options: {container: 'phoneHolder'},
  
  newId: function() {if(!Obm.Contact.PhoneWidget.phoneId) Obm.Contact.PhoneWidget.phoneId = 0; return Obm.Contact.PhoneWidget.phoneId++;},

  Extends: Obm.CoordonateWidget
});

Obm.Contact.EmailWidget = new Class ({
  kind : 'emails',
  structure : {
    label: { kind: 'label', value: 'INTENET', newLine : true, label : obm.vars.labels.emailLabel.INTERNET},
    address: { kind: 'text', value: '', newCell : true, label : obm.vars.labels.emailAddress}
  },

  options: {container: 'emailHolder'},

  newId: function() {if(!Obm.Contact.EmailWidget.emailId) Obm.Contact.EmailWidget.emailId = 0; return Obm.Contact.EmailWidget.emailId++;},

  Extends: Obm.CoordonateWidget
});

Obm.Contact.AddressWidget = new Class ({
  kind : 'addresses',
  structure : {
    label: { kind: 'label', value: 'WORK', newLine : true, label : obm.vars.labels.addressLabel.WORK}, 
    street: { kind: 'textarea', value: '', newCell : true, rows: 3, label : obm.vars.labels.addressStreet},
    zipcode: { kind: 'text', value: '' , newCell : true, label : obm.vars.labels.addressZipcode},
    town: { kind: 'text', value: '', label : obm.vars.labels.addressTown },
    expresspostal: { kind: 'text', value: '', label : obm.vars.labels.addressExpressPostal },
    country: { kind: 'select', value: '', token: obm.vars.labels.countries, label : obm.vars.labels.addressCountry }
  },

  options: {container: 'addressHolder'},

  newId: function() {if(!Obm.Contact.AddressWidget.addressId) Obm.Contact.AddressWidget.addressId = 0; return Obm.Contact.AddressWidget.addressId++;},

  Extends: Obm.CoordonateWidget
});

Obm.Contact.WebsiteWidget = new Class ({
  kind : 'websites',
  structure : {
    label: { kind: 'label', value: 'HOMEPAGE', newLine : true, label: obm.vars.labels.websiteLabel.HOMEPAGE},
    url: { kind: 'text', value: '', newCell : true, label: obm.vars.labels.websiteUrl}
  },

  options: {container: 'websiteHolder'},

  newId: function() {if(!Obm.Contact.WebsiteWidget.websiteId) Obm.Contact.WebsiteWidget.websiteId = 0; return Obm.Contact.WebsiteWidget.websiteId++;},

  Extends: Obm.CoordonateWidget
});

Obm.Contact.IMWidget = new Class ({

  kind : 'ims',
  structure : {
    protocol: { kind: 'label', value: 'JABBER', newLine : true, label: obm.vars.labels.imLabel.JABBER}, 
    address: { kind: 'text', value: '', newCell : true, label: obm.vars.labels.imAddress}
  },

  options: {container: 'imHolder'},
  
  newId: function() {if(!Obm.Contact.IMWidget.imId) Obm.Contact.IMWidget.imId = 0; return Obm.Contact.IMWidget.imId++;},

  Extends: Obm.CoordonateWidget
});


