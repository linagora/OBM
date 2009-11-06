Obm.Contact = {}
obm.contact = {};

Obm.Contact.AddressBook = new Class ({

  addressbook: null,

  contact: null,

  mycontacts: null,
  
  initialize: function(mycontacts) {
    this.mycontacts = mycontacts;
    $('contactPanel').getElements('div.contactPanelContainer').setStyle('height',window.innerHeight - $('contactPanel').offsetTop - 80);
    new Obm.Observer(new Window(window), {property:'contentHeight', onStop:function() {
      $('contactPanel').getElements('div.contactPanelContainer').setStyle('height',window.innerHeight - $('contactPanel').offsetTop - 80);
    }});
    
    this.addressBookRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('addressBookContainer'),
      onComplete: function(response) {
        this.addressbook = $(this.addressbook.get('id'));
        if(!this.addressbook) this.addressbook = $(this.mycontacts);
        this.addressbook.addClass('current');
        $('spinner').hide();
      }.bind(this),
      onRequest: $('spinner').show.bind($('spinner')),
      onFailure: function (response) {
        Obm.Error.parseStatus(this.addressBookRequest.status);
      }
    });

    this.contactRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('dataContainer'),
      onComplete: $('spinner').hide.bind($('spinner')),
      onRequest: $('spinner').show.bind($('spinner')),
      onFailure: function (response) {
        Obm.Error.parseStatus(this.contactRequest.status);
      }
    });

    this.dataRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('informationGrid'),
      onFailure: function (response) {
        Obm.Error.parseStatus(this.dataRequest.status);
        var errors = JSON.decode(response.responseText, false);
        errors.error = new Hash(errors.error);
        errors.warning = new Hash(errors.warning);
        Obm.Error.formUpdate(errors, this.dataRequest);
      }.bind(this),
      onComplete: $('spinner').hide.bind($('spinner')),
      onRequest: $('spinner').show.bind($('spinner')) 
    });    

    this.dataRequest.write = function (options) {
      this.dataRequest.setOptions({onSuccess : function() {this.refreshContact();}.bind(this)});
      this.dataRequest.post(options);
    }.bind(this);

    this.dataRequest.read = function (options) {
      this.dataRequest.setOptions({onSuccess : $empty});
      this.dataRequest.get(options);
    }.bind(this);
    
    this.addressbook = $('addressBookContainer').getElement('.current');
  },

  selectContact: function(elem) {
    if(elem.hasClass('current')) {
      this.hideContact();
    } else {
      var id = elem.get('id').split('-')[1];
      $('dataContainer').getElements('tr.current').removeClass('current');
      elem.addClass('current');
      this.contact = elem;
      this.consultContact(id);
    }
  },

  hideContact: function() {
    $('dataContainer').getElements('tr.current').removeClass('current');
    $('dataGrid').getElements('td').show();
    $('informationGrid').hide(); 
    $('dataContainer').removeClass('shrinked');
    $('dataContainer').addClass('expanded');
    $('dataGrid').setStyle('width', 'auto');
    this.contact = null;
  },

  refreshContact: function() {
    this.contactRequest.onSuccess = function() {
      this.contact = $(this.contact.get('id'));
      if(this.contact) {
        $('dataGrid').getElements('td').hide();
        $('dataContainer').removeClass('expanded');
        $('dataContainer').addClass('shrinked');    
        $('dataGrid').setStyle('width', '20em');
        this.contact.addClass('current');
      } else {
        this.hideContact();
      }
      this.contactRequest.onSuccess = $empty;
    }.bind(this);
    this.contactRequest.get({ajax : 1, action : 'search', searchpattern : this.addressbook.retrieve('search'), contactfilter : $('contactfilter').get('value')}); 

  },

  consultContact: function(id) {
    if(id) {
      $('informationGrid').show(); 
      this.dataRequest.read({ajax : 1, action : 'consult', id : id}); 
      $('dataGrid').getElements('td').hide();
      $('dataContainer').removeClass('expanded');
      $('dataContainer').addClass('shrinked');    
      $('dataGrid').setStyle('width', '20em');
    } else {
      this.hideContact();
    }
  },

  updateContact: function(id, addressbook) {
    $('informationGrid').show(); 
    this.dataRequest.read({ajax : 1, action : 'updateContact', id : id}); 
    $('dataContainer').getElements('td').hide();
    $('dataContainer').removeClass('expanded');
    $('dataContainer').addClass('shrinked');
    $('dataGrid').setStyle('width', '20em');
  },  

  addContact: function(id) {
    var id = this.addressbook.get('id').split('-')[1];
    $('informationGrid').show(); 
    this.dataRequest.read({ajax : 1, action : 'updateContact', addressbook: id}); 
    $('dataContainer').getElements('td').hide();
    $('dataContainer').removeClass('expanded');
    $('dataContainer').addClass('shrinked');
    $('dataGrid').setStyle('width', '20em');
  },  

  storeContact: function(form, id) {
    $('informationGrid').show(); 
    this.dataRequest.write(form);
    $('dataContainer').getElements('td').hide();
    $('dataContainer').removeClass('expanded');
    $('dataContainer').addClass('shrinked');
    $('dataGrid').setStyle('width', '20em');
  },    

  deleteContact: function(id, name) {
    if(confirm(obm.vars.labels.confirmDeleteContact+' \''+name+'\' ?')){
      //FIXME
      //this.addressBookRequest.addEvent('success', function() {
      //  showOkMessage(obm.vars.labels.deleteOk);
      //});      
      this.contactRequest.post({ajax:1, action:'deleteContact', 'id':id, searchpattern : this.addressbook.retrieve('search'), contactfilter : $('contactfilter').get('value')});
      this.hideContact();
    }
  },

  copyContact: function(contact, addressbook) {
    this.dataRequest.write({ajax:1, action:'copyContact', 'id':contact, 'addressbook':addressbook});
  },

  searchContact: function(form) {
    $('contactfilter').set('value','');
    this.hideContact();
    if(form.get('id') == 'advancedSearchForm') {
      var searchpattern='';
      form.getElements('input').each(function (elem) {
        if(elem.get('type') != 'submit' && elem.get('type') != 'button' && elem.get('inputValue') != '') {
          searchpattern += elem.get('name') + ':(' + elem.get('inputValue') + ') ';
        }
      });
      $('searchpattern').set('inputValue',  searchpattern)
      form = $('searchForm');
    }
    this.contactRequest.get(form); 
    $('addressBookGrid').getElements('td.current').removeClass('current');
    // Display "search results" folder
    $('addressbook-search').getParent().show();
    $('addressbook-search').set('class', 'current');
    $('addressbook-search').store('search',$('searchpattern').value)
  },

  filterContact: function(form) {
    this.hideContact();
    this.contactRequest.get({ajax:1, action:'search', searchpattern : this.addressbook.retrieve('search'), contactfilter : $('contactfilter').get('value')});
  },

  moreContact: function(offset) {
    this.hideContact();
    this.contactRequest.get({ajax : 1, action : 'search', searchpattern : this.addressbook.retrieve('search'), contactfilter : $('contactfilter').get('value'), offset: offset});
  },

  selectAddressBook: function(elem) {
    if(!elem.hasClass('current')) {
      this.hideContact();
      $('contactfilter').set('value','');
      this.contactRequest.get({ajax : 1, action : 'search', searchpattern : elem.retrieve('search')}); 
      $('addressBookGrid').getElements('td.current').removeClass('current');
      elem.addClass('current');
      this.addressbook = elem;
      if(elem.retrieve('write') != 1) $('addContact').addClass('H'); 
      else $('addContact').removeClass('H');
    }
  },

  storeAddressBook: function(form) {
    if($(form).name.get('value') == '') {
      showErrorMessage(obm.vars.labels.insertError);
    } else {
      this.addressBookRequest.post(form);
      $(form).name.set('value', '');
    }
  },

  deleteAddressBook: function(id, name) {
    if(confirm(obm.vars.labels.confirmDeleteAddressBook+'\''+name+'\' ?')){
      this.addressBookRequest.post({ajax:1, action:'deleteAddressBook', 'addressbook_id':id});
    }
  },

  /*
   * Set true or false AddressBook.sync
   */
  setSyncable: function(id) {
    this.addressBookRequest.post({ajax:1, action:'setSyncable', 'id':id});
  },

  /*
   * SynchedAddressBook
   */
  setSubscription: function(id) {
    this.addressBookRequest.post({ajax:1, action:'setSubscription', 'id':id});
  }
});




Obm.Contact.PhoneWidget = new Class ({
  kind : 'phones',
  structure : {
    label: { kind: 'label', value: 'CELL_VOICE', newLine : true, label : obm.vars.labels.phoneLabel.WORK_VOICE}, 
    number: { kind: 'text', value: '', newCell : true, label : obm.vars.labels.phoneNumber}
  },

  options: {container: 'phoneHolder'},
  
  newId: function() {if(!Obm.Contact.PhoneWidget.phoneId) Obm.Contact.PhoneWidget.phoneId = 0; return Obm.Contact.PhoneWidget.phoneId++;},

  Extends: Obm.CoordonateWidget
});

Obm.Contact.EmailWidget = new Class ({
  kind : 'emails',
  structure : {
    label: { kind: 'label', value: 'INTERNET', newLine : true, label : obm.vars.labels.emailLabel.INTERNET},
    address: { kind: 'text', value: '', newCell : true, label : obm.vars.labels.emailAddress}
  },

  options: {container: 'emailHolder'},

  newId: function() {if(!Obm.Contact.EmailWidget.emailId) Obm.Contact.EmailWidget.emailId = 0; return Obm.Contact.EmailWidget.emailId++;},

  Extends: Obm.CoordonateWidget
});

Obm.Contact.AddressWidget = new Class ({
  kind : 'addresses',
  structure : {
    label: { kind: 'label', value: 'WORK', newLine : true, label : obm.vars.labels.addressLabel.WORK, properties: {rowspan: 3}}, 
    street: { kind: 'textarea', value: '', newCell : true,  rows: 3, label : obm.vars.labels.addressStreet, properties: {colspan: 2}},
    zipcode: { kind: 'text', value: '' , newLine : true, label : obm.vars.labels.addressZipcode},
    town: { kind: 'text', value: '',  newCell : true, label : obm.vars.labels.addressTown },
    expresspostal: { kind: 'text', value: '', newLine : true, label : obm.vars.labels.addressExpressPostal },
    country_iso3166: { kind: 'select', newCell : true, value: '', token: obm.vars.labels.countries, label : obm.vars.labels.addressCountry }
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


