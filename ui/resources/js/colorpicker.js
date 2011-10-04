
obm.vars.consts.colors = ['238, 136, 0','221, 68, 119 ','204, 51, 51','153, 68, 153','102, 51, 204','51, 102, 153','51, 102, 204','34, 170, 153','50, 146, 98','16, 150, 24','102, 170, 0','170, 170, 17','214, 174, 0','221, 85, 17','168, 112, 112','140, 109, 140','98, 116, 135','112, 131, 168','92, 141, 135','137, 137, 81','176, 139, 89'];

Obm.ColorPicker = new Class({
  
  add: function(el) {
    var element = $(el);
    element.setProperty('autocomplete','off');
    element.setProperty('maxlenght','7');
    var span = new Element('span').injectBefore(element).addClass('NW');
    element.dispose();
    element.injectInside(span);
    if(element.value != '') {
      element.setStyles({
        'color': element.value,
        'backgroundColor': element.value
      });
    }
    img = new Element('img');
    img.setAttribute("src", obm.vars.images.colorPicker);
    img.injectInside(span);
    img.addEvent('click', function(e){
      this.toggle(element);
    }.bind(this));

  },

  toggle: function(element) {
    this.currentElement = element;
    if(!$('colorPickerWidget')) {
      this.buildWidget();
      this.timer = new HideTimer($('colorPickerWidget'));
    }
    var x = element.getLeft();
    var y = element.getTop() + element.offsetHeight ;
    $('colorPickerWidget').setStyles({
      'left' : x+'px',
      'top' : y+'px'
    });
    $('colorPickerWidget').setStyle('display','');
    $('colorPickerWidget').setStyle('visibility','');
    this.timer.initTimer();
    overListBoxFix("colorPickerWidget");
  },

  buildWidget: function() {
    var widget = new Element('div').setProperty('id','colorPickerWidget').setStyle('display','none');
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
          'width' : '90%'
        }).setProperty('href','#')
          .addEvent('click',this.setElementColor.bind(this))
          .appendText(obm.vars.labels.transparent)
      )    
    );
    obm.vars.consts.colors.each(function(color) {
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
            'width' : '13px',
            'color' : 'rgb('+color+')',
            'backgroundColor' : 'rgb('+color+')'
          }).addEvent('click',this.setElementColor.bind(this))
        )
      )
    }.bind(this));
    widget.injectInside(document.body);
  },

  setElementColor: function(evt) {
    var event = new Event(evt);                     
    if(this.currentElement) {
      var color = event.target;
      if(color.getStyle('backgroundColor') == 'transparent') {
        bgColor = '';
      } else {
        bgColor = color.getStyle('backgroundColor');
      }
      this.currentElement.setStyles({
        'color':color.getStyle('color'),
        'backgroundColor': bgColor
      }).setProperty('value',bgColor);
    }
    overListBoxFix($('colorPickerWidget'),'none');
    $('colorPickerWidget').setStyle('display','none');
  }

});


function colorPickerGenerator() {
  var colorPicker = new Obm.ColorPicker();
  var elements = $$('.colorPicker');
  elements.each(function(element){
    colorPicker.add(element);
  });
}

obm.initialize.chain(colorPickerGenerator);

