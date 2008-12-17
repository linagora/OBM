Obm.CoordonateWidget = new Class({
  Implements: Options,

  newId: function() { return 0;},

  setValues: function() {
    this.structure = $merge.run([this.structure].extend(arguments));
  },

  initialize: function(fields, options) {
    this.id = this.newId();
    this.setOptions(options);
    this.setValues(fields);
    this.element = new Element('table');
    this.container = $(this.options.container);
    this.displayForm(); 
    this.container.adopt(this.element);
    OverText.update();
  },    

  displayForm: function() {
    for (var field in this.structure){
      var data = this.structure[field]
      if(data.newLine == true) {line =  new Element('tr'); this.element.adopt(line);}
      if(data.newCell == true || data.newLine == true) {cell = new Element('td');line.adopt(cell);}
      if(!data.newCell && !data.newLine) cell.adopt(new Element('br'));
      cell.adopt(this.makeField(field, data));
      new OverText(cell.getElements('input, textarea'));
    }
    line.adopt(new Element('td').adopt(
      new Element('a').appendText(obm.vars.labels.remove)
        .addEvent('click', function() {this.element.dispose();OverText.update();}.bind(this))
        .setStyle('cursor','pointer')
      )
    );
  },

  makeField: function(fieldName, field) {
    switch (field.kind) {
      case 'text' :
        var element = new Element('input').setProperties({
          'type' : 'text',
          'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']',
          'alt' : field.label,
          'title' : field.label
        }).set('inputValue',field.value);
        break;
      case 'select' :
        var element = new Element('select').setProperties({
          'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']',
          'alt' : field.label,
          'title' : field.label
        });
        for (var option in field.token) {
          element.adopt(new Element('option').appendText(field.token[option]).setProperty('value', option));
        }
        element.set('inputValue', field.value);
        break;
      case 'autocomplete.local' :
        var element = new Element('input').setProperties({'type' : 'text','value' : field.value});
        new Autocompleter.Local(element, field.token, {
          'minLength': 0, 
          'overflow': true,
          'selectMode': 'type-ahead',
          'multiple': field.multiple      
          });
        break;        
      case 'textarea' :
        var element = new Element('textarea').setProperties({
          'rows' : field.rows,
          'name' : this.kind + '[' + this.id + ']' + '[' + fieldName + ']',
          'alt' : field.label,
          'title' : field.label
        }).set('inputValue',field.value);
        break;        
    }

    return element;
  }
});

Obm.PhoneWidget = new Class ({
  kind : 'phones',
  structure : {
    number: { kind: 'text', value: '', newLine : true, label : obm.vars.labels.phoneNumber},
    label: { kind: 'select', value: 'WORK',token: obm.vars.labels.phoneLabel, newCell : true, label : ''}      
  },

  options: {container: 'phoneHolder'},
  
  newId: function() {if(!Obm.PhoneWidget.phoneId) Obm.PhoneWidget.phoneId = 0; return Obm.PhoneWidget.phoneId++;},

  Extends: Obm.CoordonateWidget
});

Obm.EmailWidget = new Class ({
  kind : 'emails',
  structure : {
    address: { kind: 'text', value: '', newLine : true, label : obm.vars.labels.emailAddress},
    label: { kind: 'select', value: 'WORK',token: obm.vars.labels.emailLabel, newCell : true, label : ''}      
  },

  options: {container: 'emailHolder'},

  newId: function() {if(!Obm.EmailWidget.phoneId) Obm.EmailWidget.phoneId = 0; return Obm.EmailWidget.phoneId++;},

  Extends: Obm.CoordonateWidget
});

Obm.AddressWidget = new Class ({
  kind : 'addresses',
  structure : {
    street: { kind: 'textarea', value: '', rows: 3, newLine : true, label : obm.vars.labels.addressStreet},
    label: { kind: 'select', value: 'WORK',token: obm.vars.labels.addressLabel, label : ''}, 
    zipcode: { kind: 'text', value: '' , newCell : true, label : obm.vars.labels.addressZipcode},
    town: { kind: 'text', value: '', label : obm.vars.labels.addressTown },
    expresspostal: { kind: 'text', value: '', label : obm.vars.labels.addressExpressPostal },
    country: { kind: 'select', value: '', token: obm.vars.labels.countries, label : obm.vars.labels.addressCountry }
  },

  options: {container: 'addressHolder'},

  newId: function() {if(!Obm.AddressWidget.phoneId) Obm.AddressWidget.phoneId = 0; return Obm.AddressWidget.phoneId++;},

  Extends: Obm.CoordonateWidget
});

Obm.WebsiteWidget = new Class ({
  kind : 'websites',
  structure : {
    url: { kind: 'text', value: '', newLine : true, label: obm.vars.labels.websiteUrl},
    label: { kind: 'select', value: 'HOMEPAGE',token: obm.vars.labels.websiteLabel, newCell : true, label: ''}      
  },

  options: {container: 'websiteHolder'},

  newId: function() {if(!Obm.WebsiteWidget.phoneId) Obm.WebsiteWidget.phoneId = 0; return Obm.WebsiteWidget.phoneId++;},

  Extends: Obm.CoordonateWidget
});

Obm.IMWidget = new Class ({
  kind : 'ims',
  structure : {
    address: { kind: 'text', value: '', newLine : true, label: obm.vars.labels.imAddress},
    protocol: { kind: 'select', value: 'JABBER', token : {'XMPP' : 'Jabber', 'X-GTALK' : 'Google Talk', 'AIM' : 'AIM', 'YMSGR' : 'Yahoo', 'MSN' : 'MSN', 'X-ICQ' : 'ICQ'}, newCell : true, label: ''}      
  },

  options: {container: 'imHolder'},
  
  newId: function() {if(!Obm.IMWidget.phoneId) Obm.IMWidget.phoneId = 0; return Obm.IMWidget.phoneId++;},

  Extends: Obm.CoordonateWidget
});
/*  Implements: Options,

  options: {
    mode: 'update'
  },

  initialize: function(label, value, options) {
    this.setOptions(options)
    document.fireEvent('widgetadded');
    this.label = label;
    this.value = value;
    this.element = new Element('table');
    if(this.options.mode == 'consult') {

    } else {
      this.updateMode(); 
    }
    $('phoneHolder').adopt(this.element);
    document.addEvent('widgetadded', this.consultMode.bind(this));
  },

  updateMode: function() {
    this.element.set('html','');
    this.element.adopt(
      new Element('tr').adopt(
        new Element('th').appendText('Label')
      ).adopt(
        new Element('td').adopt(
          new Element('input').setProperties({
            'type' : 'text',
            'value' : this.label,
            'name' : 'phone_label[]'
          })
        )
      )
    ).adopt(
      new Element('tr').adopt(
        new Element('th').appendText('Numéros')
      ).adopt(
        new Element('td').adopt(
          new Element('input').setProperties({
            'type' : 'text',
            'value' : this.value,
            'name' : 'phone_number[]'
          }).addEvent('enter', this.consultMode.bind(this))
        )
      ).adopt(
        new Element('td').setProperty('colspan', 2).adopt(          
          new Element('input').setProperties({
            'type' : 'button',
            'value' : 'Valider'
          }).addEvent('click', this.consultMode.bind(this))
        )
      )
    )
  },

  consultMode: function() {
    this.element.set('html','');
    this.element.adopt(
      new Element('tr').adopt(
        new Element('th').appendText(this.label).addEvent('click', this.updateMode.bind(this))
      ).adopt(
        new Element('td').appendText(this.value)
          .addEvent('mouseover', function() {
            this.addClass('editable')
          }).addEvent('mouseout', function () {
            this.removeClass('editable')
          })        
          .addEvent('click', this.updateMode.bind(this))
      )


    );
  }
});*/
