Obm.Contact = {}
obm.contact = {};

Obm.Contact.AddressBook = new Class ({

  addressbook: null,

  contact: null,

  mycontacts: null,
  
  initialize: function(mycontacts) {
    this.mycontacts = mycontacts;
    $('contactPanel').getElements('div.contactPanelContainer').setStyle('height',window.getHeight() - $('contactPanel').offsetTop - 100);
    new Obm.Observer(new Window(window), {property:'contentHeight', onStop:function() {
      $('contactPanel').getElements('div.contactPanelContainer').setStyle('height',window.getHeight() - $('contactPanel').offsetTop - 100);
    }});
    
    this.addressBookRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('addressBookContainer'),
      onComplete: function(response) {
        $('spinner').hide();
        this.addressbook = $(this.addressbook.get('id'));
        if(!this.addressbook)  this.selectAddressBook($(this.mycontacts));
      }.bind(this),
      onRequest: $('spinner').show.bind($('spinner')),
      onFailure: function (response) {
        Obm.Error.parseStatus(this);
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
        Obm.Error.parseStatus(this);
      }
    });

    this.dataRequest = new Request.HTML({
      url: obm.vars.consts.obmUrl + '/contact/contact_index.php',
      secure : false,
      evalScripts : true,
      update: $('informationGrid'),
      onFailure: function (response) {
        Obm.Error.parseStatus(this);
      },
      onComplete: $('spinner').hide.bind($('spinner')),
      onRequest: $('spinner').show.bind($('spinner')) 
    });    

    this.dataRequest.write = function (options) {
      this.dataRequest.addEvent('success', this.refreshContact.bind(this));
      this.dataRequest.post(options);
    }.bind(this);

    this.dataRequest.read = function (options) {
      this.dataRequest.removeEvents('success');
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
    $('informationGrid').hide(); 
    $('dataGrid').removeClass('shrinked');
    $('dataGrid').addClass('expanded');
    this.contact = null;
  },

  showContact: function() {
      $('informationGrid').show(); 
      $('dataGrid').getElements('td').hide();
      $('dataContainer').removeClass('expanded');
      $('dataContainer').addClass('shrinked');    
  },

  refreshContact: function() {
    this.contactRequest.addEvent('success', function() {
      if(this.contact) {
        this.contact = $(this.contact.get('id'));
        if(this.contact) {
          $('dataGrid').removeClass('expanded');
          $('dataGrid').addClass('shrinked');
          this.contact.addClass('current');
        } else {
          this.hideContact();
        }
      } else {
        this.hideContact();
      }
      this.contactRequest.removeEvents('success');
    }.bind(this));
    this.contactRequest.get({ajax : 1, action : 'search', searchpattern : this.addressbook.retrieve('search'), contactfilter : $('contactfilter').get('value')}); 
  },

  consultContact: function(id) {
    if(id) {
      this.dataRequest.read({ajax : 1, action : 'consult', id : id}); 
      $('informationGrid').show(); 
      $('dataGrid').removeClass('expanded');
      $('dataGrid').addClass('shrinked');
    } else {
      this.hideContact();
    }
  },

  updateContact: function(id, addressbook) {
    $('informationGrid').show(); 
    this.dataRequest.read({ajax : 1, action : 'updateContact', id : id}); 
    $('dataGrid').removeClass('expanded');
    $('dataGrid').addClass('shrinked');
  },  

  addContact: function(id) {
    var id = this.addressbook.get('id').split('-')[1];
    $('informationGrid').show(); 
    this.dataRequest.read({ajax : 1, action : 'updateContact', addressbook: id}); 
    $('dataGrid').removeClass('expanded');
    $('dataGrid').addClass('shrinked');
  },  

  storeContact: function(form, id) {
    $('informationGrid').show(); 
    this.dataRequest.write(form);
    $('dataGrid').removeClass('expanded');
    $('dataGrid').addClass('shrinked');
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
    this.addressbook = $('addressbook-search');
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
      if(elem.retrieve('write') != 1) $('addContact').setStyle('visibility','hidden'); 
      else $('addContact').setStyle('visibility','visible'); 
    }
  },

  storeAddressBook: function(form) {
    if($(form.name).get('value') == '') {
      showErrorMessage(obm.vars.labels.insertError);
    } else {
      this.addressBookRequest.post(form);
      $(form.name).set('value', '');
    }
  },

  deleteAddressBook: function(id, name) {
    if(confirm(obm.vars.labels.confirmDeleteAddressBook+' \''+name+'\' ?')){
      this.addressBookRequest.post({ajax:1, action:'deleteAddressBook', 'addressbook_id':id});
    }
  },

  /*
   * Set true or false AddressBook.sync
   */
  setSyncable: function(id) {
    this.addressBookRequest.post({ajax:1, action:'setSyncable', 'addressbook_id':id});
  },

  /*
   * SynchedAddressBook
   */
  setSubscription: function(id) {
    this.addressBookRequest.post({ajax:1, action:'setSubscription', 'addressbook_id':id});
  }
});
