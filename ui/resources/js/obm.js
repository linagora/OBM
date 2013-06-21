/*
 *
 *
 */

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

/*
 *
 *
 */

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
 * A simple wrapper arround mootools Tips.
 *
 */
Obm.Tip = new Class({
  Extends: Tips,

  options: {
    fixed:false,
    click:false
  },

  initialize: function(e, options, klass) {
    this.parent(e, options);
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

  add: function(element, content) {
    if (content) {
      element.set('title', content);
    } else {
      try {
        element_title = eval(element.get('title')); 
        content = element_title.content;
      } catch (ee) {
        content = element.get('title');
      }
    }
    var title = element.retrieve('tip:title', content);
    var text = element.retrieve('tip:text', element.get('rel') || element.get('href'));
    if (this.options.click) { 
      element.addEvent('click', function() {
        if (this.tip.style.visibility == 'hidden') {
          this.elementEnter(this, element);
        } else {
          this.elementLeave(this, element);
        }
        var elementId = element.id.split('_');
        if ($chk($('close_'+elementId[1]))) {
          $('close_'+elementId[1]).addEvent('click', function() {
            this.elementLeave(this, element);
          }.bind(this));
        }
      }.bind(this));
    } else {
      var enter = element.retrieve('tip:enter', this.elementEnter.bindWithEvent(this, element));
      var leave = element.retrieve('tip:leave', this.elementLeave.bindWithEvent(this, element));
      element.addEvents({mouseenter: enter, mouseleave: leave});
    }
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
 * A simple wrapper arround the StickyWinModal.
 *
 */
Obm.Popup = new Class ({
  
  initialize: function() {
    this.popups = new Hash();
    this.isOpen = false;
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
    var sticky = new StickyWinModal({content: content, modalOptions :{hideOnClick:true,
        modalStyle:{
        'background-color':'#eee',
        'opacity':.6}}});
    this.popups.set(id, sticky);
  },

  show: function(id) {
    if(this.popups.get(id)) {
      sticky = this.popups.get(id);
    } else {
      $(id).setStyle('display','block');
      var sticky = new StickyWinModal({content: $(id), modalOptions :{hideOnClick:true,
        modalStyle:{
        'background-color':'#eee',
        'opacity':.6}}});
      this.popups.set(id, sticky);
    }
    this.isOpen = true;
    sticky.show();
  },

  hide: function(id) {
    if(this.popups.get(id)) {
      this.popups.get(id).hide();
    } 
    this.isOpen = false;
  }

});



/*
 *
 * OBM Drag
 *
 * Extends Mootools Drag.Move class
 * Support overflow & x/y units 
 *
 */
Obm.Drag = new Class ({ 

  Extends: Drag.Move,

  options: {
    overflow:false,
    units: {'x':'px', 'y':'px'},
    initialWidth: false
  },

  start: function(event){
    var width = this.element.style.width;

    // Set correct width to initialize limit
    if (this.options.initialWidth) {
      this.element.style.width = this.options.initialWidth.toFloat()+'%';
    }

    this.parent(event);

    // Reset element width
    if (this.options.initialWidth) {
      this.element.style.width = width.toFloat()+'%';
    }

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
      if (this.options.style) {
    	  var value = this.value.now[z] + this.options.units[z];
    	  if(this.options.modifiers[z] == 'left') {
    		  var fixedValue = parseFloat(value).toFixed(2) + '%';
    		  value = fixedValue;
    	  }
    	  this.element.setStyle(this.options.modifiers[z], value);
      } else {
    	  this.element[this.options.modifiers[z]] = this.value.now[z];
      }
    }
    this.fireEvent('drag', this.element);
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
 * Add a periodical check on an element property
 * If the property change it call the onStart function, will changing 
 * it call the onChange  function, and when not changing anymore the
 * onStop function.
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
    if(this.options.property == 'contentHeight') {
      v = this.el.getHeight() + this.el.getScrollHeight();
    } else if(this.options.property == 'contentWidth') {
      v = this.el.getWidth() + this.el.getScrollWidth();      
    } else if (this.options.property == 'innerHeight') {
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

/*
 * Add caching method on the mootools Request
 *
 */

//Request.Cache = {};
//
//Request.prototype._send = Request.prototype.send;
//
//Request.prototype.send = function(options){
//  var url = url = options.url || this.options.url;
//  var data = options.data || this.options.data;
//  var cacheId = JSON.encode($merge(data, {url : url}));
//  //if(Request.Cache[cacheId] && 1==0) {
//  //  this.response = Request.Cache[cacheId];
//  //  this.xhr = {status: 200, readyState: 4};
//  //  this.running = true;
//  //  this.onStateChange();
//  //  this.xhr = new Browser.Request();
//
//  //} else {
//    this._send(options);
//    this.addEvent('complete', function() { Request.Cache[cacheId] = this.response; }); 
//  //}
//  return this;
//}
//

function popup(url,name,height,width) {
  if(!width)
    width = obm.vars.consts.popupWidth;
  if(!height)
    height = obm.vars.consts.popupHeight;
  window.open(url,name,'height='+height+',width='+width+',scrollbars=yes,menubar=yes');
  return false;
}

function showAllInOneMenu(){
  var positionMoreMenuButton = $('moremenu_button').getPosition().x;
  var allinonemenu = $('allinonemenu');

  if ( allinonemenu.isVisible() ){
    allinonemenu.setStyle('display', 'none');
  }else{
    allinonemenu.setStyle('display', 'block');
    allinonemenu.setStyle('left', positionMoreMenuButton);
  }
}

function showProfileMenu(){
  var profileMenu = $('profileMenu');
  if ( profileMenu.isVisible() ){
    profileMenu.setStyle('display', 'none');
  } else {
    profileMenu.setStyle('display', 'block');
  }
}

function showOkMessage(message) {
  showMessage('ok',message);
}

function showWarnMessage(message) {
  showMessage('warning',message);
}

function showErrorMessage(message) {
  showMessage('error',message);
}

function showErrorMessageCustomTimeout(message, timeout) {
	showMessageWithTimeout('error', message, timeout);
}

function showMessageWithTimeout(klass, message, timeout) {
  var content = $('ajaxMessage');
  content.setStyle('display','block');
  new Element('p').addClass('message')
                  .addClass(klass)
                  .set('html', message)
                  .injectInside(content);
  setTimeout(function () {content.innerHTML = ''; content.setStyle('display','none');}, timeout);
}

function showMessage(klass, message) {
  showMessageWithTimeout(klass, message, 3000);
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
    if(overObject.getStyle("zIndex") == 0 || overObject.getStyle("zIndex") == 'auto' ) {
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

function refreshWaitEvent() {
  new Request.JSON({
        url: obm.vars.consts.obmUrl+'/calendar/calendar_index.php',
        secure: false,
        async: true,
        onComplete: function(response) {
            var elem = $('bannerWaitingEvent');
            var count = parseInt(response.msg, 10);
            if(count > 0){
              elem.set('text',count);
              elem.setStyle('display', 'inline-block');
              resizeForBadges(elem, count);
            } else {
              elem.setStyle('display', 'none');
            }
        }
  }).get({ajax : 1,action : 'get_json_waiting_events'});
}

function refreshUnreadMail(){
  getWebmailUnreadMail(displayUnreadMail);
}

function displayUnreadMail(count) {
  var elem = $('bannerUnreadMail');
  if(count > 0){
    elem.set('text', count);
    elem.setStyle('display', 'inline-block');
    resizeForBadges(elem, count);
  } else {
    elem.setStyle('display', 'none');
  }
}

function getWebmailUnreadMail(callback) {
  var r = new Request({
    url: obm.vars.consts.obmUrl+'/webmail/',
    secure: false,
    method: 'get',
    data: {_task: "mail", _action: "unread_plugin"},
    noCache: true,
    async: true,
    onSuccess: function(responseText){
        var count = parseInt(responseText, 10);
        if( isNaN(count)) {
            return ;
        }
        callback(count);
    }
  });
  r.send();
}

function resizeForBadges(elem, count){
  if (count <= 9) {
    elem.getParent('li').setStyle('margin-right', '10px');
  } else if (count > 9 && count < 99) {
    elem.getParent('li').setStyle('margin-right', '15px');
  } else if (count > 99 && count < 999) {
    elem.getParent('li').setStyle('margin-right', '22px');
  } else if (count > 999) {
    elem.getParent('li').setStyle('margin-right', '28px');
  }
}

//used by calendar to go through webkit's bug #18994 (https://bugs.webkit.org/show_bug.cgi?id=18994)
String.prototype.toFloat = function(){
  var value = this;
  value = value.replace(',','.');
  return parseFloat(value);
};

// mootools 1.2.4 browsers
Obm.Browsers = $merge({
  Engines: {
    presto: function(){
      return (!window.opera) ? false : ((arguments.callee.caller) ? 960 : ((document.getElementsByClassName) ? 950 : 925));
    },
    trident: function(){
      return (!window.ActiveXObject) ? false : ((window.XMLHttpRequest) ? ((document.querySelectorAll) ? 6 : 5) : 4);
    },
    webkit: function(){
      return (navigator.taintEnabled) ? false : ((Browser.Features.xpath) ? ((Browser.Features.query) ? 525 : 420) : 419);
    },
    gecko: function(){
      return (!document.getBoxObjectFor && window.mozInnerScreenX == null) ? false : ((document.getElementsByClassName) ? 19 : 18);
    }
  }
});
