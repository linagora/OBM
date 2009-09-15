Obm.Menu = new Class({
  
  initialize: function() {
    this.menuItems = new Object();

    var sectionItems = $$('.sectionItem');
    for(i=0;i<sectionItems.length;i++) {
      var item = sectionItems[i];
      item.addEvent('mouseover', function(e){
        this.addClass('hover');
      });  
      item.addEvent('mouseout', function(e){
        this.removeClass('hover');
      });        
    }

  },  

  addItem: function(item) {
    var slide = new Fx.Slide(item +'-items', {duration: 150,onComplete:this.menuListBoxFix});
    var sectionItem = $(item +'-items-wrapper');
    var sectionBlock = $(item);
    sectionItem.style.top = sectionBlock.getTop() + sectionBlock.offsetHeight + 'px';
    sectionItem.style.left = sectionBlock.getLeft() + 'px';

    sectionItem.style.display = 'block';
    slide.boxFix = this.menuListBoxFix.bind(slide);
    slide.xHide = function () {
      this.hide();
      this.boxFix(this.element);
    }
    slide.hiddingTimer = new HideTimer(slide.element,{fn:slide.xHide.bind(slide),elems:[sectionBlock]});

    slide.hide();
    this.menuItems[item] = slide;
    sectionBlock.addEvent('click', function(e){
      obm.menu.toggle(this.id)
    });   
  },

  toggle: function(item) {
    sectionItem = $(item +'-items-wrapper');
    sectionBlock = $(item);
    sectionItem.style.top = sectionBlock.getTop() + sectionBlock.offsetHeight + 'px';
    sectionItem.style.left = sectionBlock.getLeft() + 'px';
    this.menuItems[item].toggle();
    this.hideMenuBut(item);
  },

  hideMenuBut: function(exception) {
    for(var i in this.menuItems) {
      if(exception != i) {
        this.menuItems[i].hide();
      }
    }
  },

  hideMenu: function() {
    for(var i in this.menuItems) {
      this.menuItems[i].hide();
    }
  },
  
  menuListBoxFix: function(element) {
    if (this.wrapper['offset'+this.layout.capitalize()] > 0) {
      overListBoxFix(element,'block');
    } else {
      overListBoxFix(element,'none');
    }
  }
});


Obm.Portlets = new Class({
  
  initialize: function() {
    if(!$('portlets') )
      return false;
    var elements = $$('.portlet');
    if (elements.length == 0) {
      $('portletsPanel').setStyle('display','none');
      $('mainPanel').setStyle('margin-left','0');
      return false;
    }
    var img = new Element('img');
    img.src = obm.vars.images.minus;
    this.close = img.src;
    img.src = obm.vars.images.plus;
    this.open = img.src; 
    
    // 
    this.portlets = new Object();
    
    $('portlets').set('slide', {duration: 'normal',mode: 'horizontal', wrapper: 'portletsPanel'});
    $('mainPanel').set('tween',{property: 'margin-left'});

    $('portletsHandler').set('tween',{property: 'left'});

    this.delta = $('mainPanel').getLeft() - $('portlets').offsetWidth;
    this.width = $('portletsPanel').offsetWidth;

    $('portletsHandler').setStyle('height',($('portletsPanel').offsetHeight - 4) + 'px');
    $('portletsHandler').setStyle('left',(this.width - $('portletsHandler').offsetWidth)  + 'px');
    $('portletsHandler').getFirst().getNext().setStyle('display','none'); ;
    $('portletsHandler').addEvent('click', function(e){
      obm.portlets.toggle();
    });

    if(Cookie.read("portletHidden") == "true") {
      $('portletsHandler').getFirst().setStyle('display','none')
                  .getNext().setStyle('display','inline'); 
      $('portlets').get('slide').hide();
      $('portletsPanel').setStyle('width',this.delta + 'px');
      $('mainPanel').tween(this.delta);
      $('portletsHandler').tween(0);
    }

  
    for(i=0;i<elements.length;i++) {
      var el = elements[i];      
      var title = el.getFirst();
      var content = title.getNext();
      this.portlets[el.id] = new Fx.Slide(content, {duration: 150,wait:false});
      
      var img = new Element('img');
      if(Cookie.read(el.id + "Hidden")  == "true") {
        this.portlets[el.id].hide();
        img.src = this.open;
      } else {
        img.src = this.close;
      }
      title.insertBefore(img,title.firstChild);
      img.className = 'handle';
      img = $(img);
      img.addEvent('click', function(e){
        if(this.getAttribute("src") == obm.portlets.close) 
          this.setAttribute("src", obm.portlets.open);
        else 
          this.setAttribute("src", obm.portlets.close);
        obm.portlets.toggleElement(this.parentNode.parentNode.id);
      });
    }

  },

  toggle: function() {

    if($('portletsHandler').getFirst().getStyle('display') == 'none')
       $('portletsHandler').getFirst().setStyle('display','inline')
                   .getNext().setStyle('display','none');
    else
       $('portletsHandler').getFirst().setStyle('display','none')
                   .getNext().setStyle('display','inline');      

    if($('portlets').get('slide').open) {
      $('mainPanel').tween(this.width + this.delta,this.delta);
      $('portlets').slide('out');
      $('portletsHandler').tween(this.width - $('portletsHandler').offsetWidth,0);
      $('portletsPanel').setStyle('width',this.delta + 'px');
    } else {
      $('mainPanel').tween(this.delta,this.width + this.delta);
      $('portlets').slide('in');
      $('portletsHandler').tween(0,this.width - $('portletsHandler').offsetWidth);
      $('portletsPanel').setStyle('width',this.width + 'px');
    }

    if(Cookie.read("portletHidden")  != "true") {
      Cookie.write("portletHidden", "true",{path: '/'});
    } else {
      Cookie.write("portletHidden", "false",{path: '/'});
    }
  },

  toggleElement: function(item) {
    this.portlets[item].toggle();
    if(Cookie.read(item + "Hidden")  != "true") {
      Cookie.write(item + "Hidden", "true",{path: '/'});
    } else {
      Cookie.write(item + "Hidden", "false",{path: '/'});
    }    
  }

});


/*
 *
 * OBM Tip 
 *
 */
Obm.Tip = new Class({
  Extends: Tips,

  initialize: function(e, klass) {
    this.parent(e);
    if (klass) {
      this.tip.addClass(klass);
    } else {
      this.tip.addClass('obmTip');
    }
  },

  attach: function(elements) {
    $$(elements).each(function(element){
      this.add(element);
    }, this);
    return this;
  },

  add: function(element) {
    try {
      element_title = eval(element.get('title')); 
      content = element_title.content;
    } catch (ee) {
      content = element.get('title');
    }
    var title = element.retrieve('tip:title', content);
    var text = element.retrieve('tip:text', element.get('rel') || element.get('href'));
    var enter = element.retrieve('tip:enter', this.elementEnter.bindWithEvent(this, element));
    var leave = element.retrieve('tip:leave', this.elementLeave.bindWithEvent(this, element));
    element.addEvents({mouseenter: enter, mouseleave: leave});
    if (!this.options.fixed){
      var move = element.retrieve('tip:move', this.elementMove.bindWithEvent(this, element));
      element.addEvent('mousemove', move);
    }
    element.store('tip:native', element.get('title'));
    element.erase('title');
  }

});


/*
 *
 * OBM Popup
 *
 */
Obm.Popup = new Class ({
  
  initialize: function() {
    this.popups = new Hash();
  },
  
  add: function(id, content, buttons) {
    if(content != undefined) {
      var content = StickyWin.ui('the caption', 'this is the body', {
                      width: '400px',
                      buttons: buttons
                    })

    } else {
      var content = $(id);
    }
    var sticky = new StickyWinModal({content: content, modalOptions :{hideOnClick:false}});
    this.popups.set(id, sticky);
  },

  show: function(id) {
    if(this.popups.get(id)) {
      this.popups.get(id).show();
    } else {
      $(id).setStyle('display','block');
      var sticky = new StickyWinModal({content: $(id), modalOptions :{hideOnClick:false}});
      this.popups.set(id, sticky);
      sticky.show();
    }
  },

  hide: function(id) {
    if(this.popups.get(id)) {
      this.popups.get(id).hide();
    } 
  }

});



/*
 *
 * OBM Drag
 *
 * Extends Mootools Drag.Move class
 * Support overflow & x/y units 
 * Add style on overed elements // FIXME: this.checkDroppables is too slow
 *
 */
Obm.Drag = new Class ({ 

  Extends: Drag.Move,

  options: {
    overflow:false,
    units: {'x':'px', 'y':'px'},
    overStyle: null
  },

  start: function(event){
    this.parent(event);
    // this.overed = null;

    this.pixelUnitSize = new Object();
    if (this.options.preventDefault) event.preventDefault();
    this.fireEvent('beforeStart', this.element);
    this.mouse.start = event.page;
    var limit = this.options.limit;
    this.limit = {'x': [], 'y': []};
    for (var z in this.options.modifiers){
      if (!this.options.modifiers[z]) continue;
      if(this.element.getStyle(this.options.modifiers[z]).toFloat() == 0) {
        this.element.setStyle(this.options.modifiers[z], '1' + this.options.units[z]);
        this.pixelUnitSize[z] = this.element[('offset-'+this.options.modifiers[z]).camelCase()] / this.element.getStyle(this.options.modifiers[z]).toFloat();
        this.element.setStyle(this.options.modifiers[z], '0' + this.options.units[z]);
      } else {
        this.pixelUnitSize[z] = this.element[('offset-'+this.options.modifiers[z]).camelCase()] / this.element.getStyle(this.options.modifiers[z]).toFloat();
      }
      if (this.options.style) this.value.now[z] = this.element.getStyle(this.options.modifiers[z]).toInt();
      else this.value.now[z] = this.element[this.options.modifiers[z]];
      if (this.options.invert) this.value.now[z] *= -1;
      this.mouse.pos[z] = event.page[z] - (this.value.now[z] * this.pixelUnitSize[z]);
      if (this.options.overflow) {
        this.mouse.pos[z] += this.options.overflow.getScroll()[z];
      }
      if (limit && limit[z]){
        for (var i = 2; i--; i){
          if ($chk(limit[z][i])) this.limit[z][i] = $lambda(limit[z][i])();
        }
      }
    }
    if ($type(this.options.grid) == 'number') this.options.grid = {'x': this.options.grid, 'y': this.options.grid};
    this.document.addEvents({mousemove: this.bound.check, mouseup: this.bound.cancel});
    this.document.addEvent(this.selection, this.bound.eventStop);
  },

  drag: function(event) {

    // if (this.overed && this.options.overStyle) {
    //   this.overed.removeClass(this.options.overStyle);
    // }

    if (this.options.preventDefault) event.preventDefault();
    this.mouse.now = event.page;
    for (var z in this.options.modifiers){
      if (!this.options.modifiers[z]) continue;
      this.value.now[z] = this.mouse.now[z] - this.mouse.pos[z];
      if (this.options.overflow) {
        this.value.now[z] += this.options.overflow.getScroll()[z];
      }
      if (this.options.invert) this.value.now[z] *= -1;
      if (this.options.limit && this.limit[z]){
        if ($chk(this.limit[z][1]) && (this.value.now[z] > this.limit[z][1])){
          this.value.now[z] = this.limit[z][1];
        } else if ($chk(this.limit[z][0]) && (this.value.now[z] < this.limit[z][0])){
          this.value.now[z] = this.limit[z][0];
        }
      }

      this.value.now[z] = this.value.now[z]/this.pixelUnitSize[z];
      if (this.options.grid[z]) this.value.now[z] -= (this.value.now[z] % this.options.grid[z]);
      if (this.options.style) this.element.setStyle(this.options.modifiers[z], this.value.now[z] + this.options.units[z]);
      else this.element[this.options.modifiers[z]] = this.value.now[z];
    }
    this.fireEvent('drag', this.element);

		// this.checkDroppables();
    // if (this.overed && this.options.overStyle) {
    //   this.overed.addClass(this.options.overStyle);
    // }

  },

	stop: function(event){
    this.checkDroppables();
		this.fireEvent('drop', [this.element, this.overed]);
    this.overedElement = this.overed;
    // if (this.overed && this.options.overStyle) {
    //   this.overed.removeClass(this.options.overStyle);
    // }
		return this.parent(event);
	}
});


/*
 *
 * OBM Scroller 
 *
 * Extends Mootools Scroller class
 * Fix mootools bug
 *   see https://mootools.lighthouseapp.com/projects/2706/tickets/94-scroller-doesn-t-work-in-1-2dev-1555
 *
 */
Obm.Scroller = new Class({

  Extends: Scroller,

  getCoords: function(event){
    this.page = (this.listener.get('tag') == 'body') ? event.client : event.page;
    if (!this.timer) this.timer = this.scroll.pass([event], this).periodical(50, this);
  },

  scroll: function(event){
    var size = this.element.getSize(), scroll = this.element.getScroll(), pos = this.element.getOffsets(), scrollSize = this.element.getScrollSize(), change = {'x': 0, 'y': 0};
    for (var z in this.page){
      if (this.page[z] < (this.options.area + pos[z]) && scroll[z] != 0)
        change[z] = (this.page[z] - this.options.area - pos[z]) * this.options.velocity;
      else if (this.page[z] + this.options.area > (size[z] + pos[z]) && scroll[z] + size[z] != scrollSize[z])
        change[z] = (this.page[z] - size[z] + this.options.area - pos[z]) * this.options.velocity;
    }
    if (change.y || change.x) this.fireEvent('change', [scroll.x + change.x, scroll.y + change.y, event]);
  }

});


/*
 *
 * OBM Observer
 *
 * Add element property observer
 *
 */
Obm.Observer = new Class({ 

  Implements: Options,   

  options: {
    property: 'width',
    frequency: '500',
    onStart: $empty,
    onChange: $empty,
    onStop: $empty
  },

  initialize: function(el, options) {
    this.setOptions(options);
    this.el = $(el);
    this.lastValue = this.setValue();
    this.change = false; 
    this.timer = this.check.periodical(this.options.frequency, this);
  },
  
  setValue: function() {
    var v;
    if (this.options.property == 'innerHeight') {
      v = this.el.getHeight();
    } else if (this.options.property == 'innerWidth') {
      v = this.el.getWidth();
    } else {
      v = this.el[this.options.property];
      if(!v) 
        v = this.el.getStyle(this.options.property);
    }
    return v;
  },
  
  check: function() {
    var v = this.setValue(); 
    if(this.lastValue == v) {
      if(this.change) {
        this.options.onStop(this.el, v);
        this.change = false;
      }
      return;
    }
    this.lastValue = v; 
    if(!this.change) {
      this.options.onStart(this.el, v);
      this.change = true;
    }
    if(this.change) {
      this.options.onChange(this.el, v);
    }
  }
});


function popup(url,name,height,width) {
  if(!width)
    width = obm.vars.consts.popupWidth;
  if(!height)
    height = obm.vars.consts.popupHeight;
  window.open(url,name,'height='+height+',width='+width+',scrollbars=yes,menubar=yes');
  return false;
}

function showOkMessage(message) {
  showMessage('ok',message);
}

function showErrorMessage(message) {
  showMessage('error',message);
}

function showMessage(klass, message) {
  var content = $('ajaxMessage');
  content.setStyle('display','block');
  new Element('p').addClass('message')
                  .addClass(klass)
                  .appendText(message)
                  .injectInside(content);
  setTimeout(function () {content.innerHTML = ''; content.setStyle('display','none');}, 1500);
}

function overListBoxFix(overObject, forceDisplay) {
  
  if (navigator.userAgent.toLowerCase().indexOf("msie") == -1)
    return;
  
  try {
    if (!$('listBoxHider')) {
      
      new Element("iframe").setProperty('id','listBoxHider')
        .setProperty("src", "javascript:false;")
        .setProperty("scrolling", "no")
        .setProperty ("frameborder", "0")
        .injectInside($(document.body));
    }
    overObject = $(overObject);
    if(overObject.getStyle("zIndex") == 0 || overObject.getStyle("zIndex") == 'auto' ) {
      overObject.setStyle("zIndex","1000");
    }
    $('listBoxHider').setStyles({
      position : "absolute",
      width : overObject.offsetWidth ,
      height : overObject.offsetHeight,
      top : overObject.getTop(),
      left : overObject.getLeft(),
      zIndex : overObject.getStyle("zIndex") - 1,
      visibility : overObject.getStyle("visibility") == "hidden" ? "hidden" : "visible",
      display : (forceDisplay) ? forceDisplay : overObject.getStyle("display") 
    });
  } catch (ee) {
    alert(ee);
  }

}

function showWaitingPanel(el) {
  return false; 
  el = $(el);
  obm.waitingPanel = new Element('div')
                      .setStyles({
                          'backgroundColor' : 'gray',
                          'opacity' : '0.5',
                          'position' : 'absolute',
                          'left' : el.getLeft() + 'px',
                          'top' : el.getTop() + 'px',
                          'height' : el.offsetHeight + 'px',
                          'width' : el.offsetWidth + 'px'})
                        .injectInside(document.body)
                        .adopt(new Element('img').setProperty('src',''));
}

function discardWaitingPanel() {;
  if(obm.waitingPanel) {
    obm.waitingPanel.dispose();
  }
}

function selectAllCb(container) {
  var container = $(container);
  var chks = container.getElements('input[type=checkbox]');
  chks.each(function (chk) {
    chk.checked = true;
  })
}
function unSelectAllCb(container) {
  container = $(container);
  var chks = container.getElements('input[type=checkbox]');
  chks.each(function (chk) {
    chk.checked = false;
  })          
}

function change_view(url) {
  Cookie.write('calendar_view', url,{path: '/'});
  window.location=url;
}

function last_visit_calendar_view() {
  url = obm.vars.consts.calendarUrl;
  if (Cookie.read('calendar_view') != null) {
    url = '/calendar/'+Cookie.read('calendar_view');
  }
  window.location=url;
}
