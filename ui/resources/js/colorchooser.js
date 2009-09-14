

Obm.ColorChooser = new Class({
  
  add: function(el) {
    var element = $(el);
    element.setProperty('autocomplete','off');
    element.setProperty('maxlenght','7');
    var span = new Element('span').injectBefore(element).addClass('NW');
    span.setStyles({
      'float': 'right'
    });
    element.dispose();
    element.injectInside(span);
    if(element.value != '') {
      element.setStyles({
        'color': element.value,
        'backgroundColor': element.value
      });
    }
    var data = element.get('name').split('-');
    img = new Element('img');
    img.setAttribute("src", obm.vars.images.colorPicker);
    img.injectInside(span);
    img.addEvent('click', function(e){
      this.toggle(span,data[1],data[2]);
    }.bind(this));

  },

  toggle: function(element,entity,entity_id) {
    this.currentElement = element.getParent();
    this.currentEntity = entity;
    this.currentEntityId = entity_id;
    if(!$('colorChooserWidget')) {
      this.buildWidget();
      this.timer = new HideTimer($('colorChooserWidget'));
    }
    var x = element.getLeft();
    var y = element.getTop() + element.offsetHeight ;
    $('colorChooserWidget').setStyles({
      'left' : x+'px',
      'top' : y+'px'
    });
    $('colorChooserWidget').setStyle('display','');
    $('colorChooserWidget').setStyle('visibility','');
    this.timer.initTimer();
    overListBoxFix("colorChooserWidget");
  },

  buildWidget: function() {
    var widget = new Element('div').setProperty('id','colorChooserWidget').setStyles({
      'display': 'none',
      'z-index': '1000'
    });
    var container = new Element('ul')
      .setStyles ({
        'float' : 'left',
        'padding' : '3px 0',
        'margin' : '5px',
        'position' : 'relative',
        'width' : '100%'
      }).injectInside(widget);
      container.adopt (
        new Element('li').setStyles({
          'display' : 'inline',
          'fontSize' : '1px',
          'lineHeight' : '1px'
        }).adopt (
          new Element('a').setStyles({
            'border' : '1px solid #FFFFFF',
            'display' : 'block',
            'float' : 'left',
            'height' : '13px',
            'margin': '0pt 4px 4px 0pt',
            'textDecoration' : 'none',
            'width' : '13px'
          }).set('class','eventOwner').addEvent('click',this.setElementColor.bind(this))
        )
      );
    for (var i=0; i<20; i++) {
      container.adopt (
        new Element('li').setStyles({
          'display' : 'inline',
          'fontSize' : '1px',
          'lineHeight' : '1px'
        }).adopt (
          new Element('a').setStyles({
            'border' : '1px solid #FFFFFF',
            'display' : 'block',
            'float' : 'left',
            'height' : '13px',
            'margin': '0pt 4px 4px 0pt',
            'textDecoration' : 'none',
            'width' : '13px'
          }).set('class','eventOwner'+i).addEvent('click',this.setElementColor.bind(this))
        )
      );
    }
    widget.injectInside(document.body);
  },

  setElementColor: function(evt) {
    var event = new Event(evt);
    klass = event.target.get('class');
    this.currentElement.set('class',klass);
    if (obm.calendarManager) obm.calendarManager.setEventsClass(this.currentEntity, this.currentEntityId, klass);
    setEventsColors(this.currentEntity, this.currentEntityId, klass);
    overListBoxFix($('colorChooserWidget'),'none');
    $('colorChooserWidget').setStyle('display','none');
  }

});


function colorChooserGenerator() {
  var colorChooser = new Obm.ColorChooser();
  var elements = $$('.colorChooser');
  elements.each(function(element){
    colorChooser.add(element);
  });
}

obm.initialize.chain(colorChooserGenerator);

