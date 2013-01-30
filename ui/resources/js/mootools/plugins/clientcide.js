/*
Script: Clientcide.js
  The Clientcide namespace.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
var Clientcide = {
  version: '805',
  setAssetLocation: function(baseHref) {
    if (window.StickyWin && StickyWin.ui) {
      StickyWin.UI.refactor({
        options: {
          baseHref: baseHref + '/stickyWinHTML/'
        }
      });
      if (StickyWin.alert) {
        var CGFsimpleErrorPopup = StickyWin.alert.bind(window);
        StickyWin.alert = function(msghdr, msg, base) {
            return CGFsimpleErrorPopup(msghdr, msg, base||baseHref + "/simple.error.popup");
        };
      }
    }
    if (window.TagMaker) {
      TagMaker = TagMaker.refactor({
          options: {
              baseHref: baseHref + '/tips/'
          }
      });
    }
    if (window.ProductPicker) {
      ProductPicker.refactor({
          options:{
              baseHref: baseHref + '/Picker'
          }
      });
    }

    if (window.Autocompleter) {
      var AcClientcide = {
          options: {
            baseHref: baseHref + '/autocompleter/'
          }
      };
      Autocompleter.Base.refactor(AcClientcide);
      if (Autocompleter.Ajax) {
        ["Base", "Xhtml", "Json"].each(function(c){
          if(Autocompleter.Ajax[c]) Autocompleter.Ajax[c].refactor(AcClientcide);
        });
      }
      if (Autocompleter.Local) Autocompleter.Local.refactor(AcClientcide);
      if (Autocompleter.JsonP) Autocompleter.JsonP.refactor(AcClientcide);
    }

    if (window.Lightbox) {
      Lightbox.refactor({
          options: {
              assetBaseUrl: baseHref + '/slimbox/'
          }
      });
    }

    if (window.Waiter) {
      Waiter.refactor({
        options: {
          baseHref: baseHref + '/waiter/'
        }
      });
    }
  },
  preLoadCss: function(){
    if (window.DatePicker) new DatePicker();
    if (window.ProductPicker) new ProductPicker();
    if (window.TagMaker) new TagMaker();
    if (window.StickyWin && StickyWin.ui) StickyWin.ui();
    if (window.StickyWin && StickyWin.pointy) StickyWin.pointy();
    Clientcide.preloaded = true;
    return true;
  },
  preloaded: false
};
(function(){
  if (!window.addEvent) return;
  var preload = function(){
    if (window.dbug) dbug.log('preloading clientcide css');
    if (!Clientcide.preloaded) Clientcide.preLoadCss();
  };
  window.addEvent('domready', preload);
  window.addEvent('load', preload);
})();
setCNETAssetBaseHref = Clientcide.setAssetLocation;

/*
Script: dbug.js
  A wrapper for Firebug console.* statements.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
var dbug = {
  logged: [],  
  timers: {},
  firebug: false, 
  enabled: false, 
  log: function() {
    dbug.logged.push(arguments);
  },
  nolog: function(msg) {
    dbug.logged.push(arguments);
  },
  time: function(name){
    dbug.timers[name] = new Date().getTime();
  },
  timeEnd: function(name){
    if (dbug.timers[name]) {
      var end = new Date().getTime() - dbug.timers[name];
      dbug.timers[name] = false;
      dbug.log('%s: %s', name, end);
    } else dbug.log('no such timer: %s', name);
  },
  enable: function(silent) { 
    var con = window.firebug ? firebug.d.console.cmd : window.console;

    if((!!window.console && !!window.console.warn) || window.firebug) {
      try {
        dbug.enabled = true;
        dbug.log = function(){
            (con.debug || con.log).apply(con, arguments);
        };
        dbug.time = function(){
          con.time.apply(con, arguments);
        };
        dbug.timeEnd = function(){
          con.timeEnd.apply(con, arguments);
        };
        if(!silent) dbug.log('enabling dbug');
        for(var i=0;i<dbug.logged.length;i++){ dbug.log.apply(con, dbug.logged[i]); }
        dbug.logged=[];
      } catch(e) {
        dbug.enable.delay(400);
      }
    }
  },
  disable: function(){ 
    if(dbug.firebug) dbug.enabled = false;
    dbug.log = dbug.nolog;
    dbug.time = function(){};
    dbug.timeEnd = function(){};
  },
  cookie: function(set){
    var value = document.cookie.match('(?:^|;)\\s*jsdebug=([^;]*)');
    var debugCookie = value ? unescape(value[1]) : false;
    if((!$defined(set) && debugCookie != 'true') || ($defined(set) && set)) {
      dbug.enable();
      dbug.log('setting debugging cookie');
      var date = new Date();
      date.setTime(date.getTime()+(24*60*60*1000));
      document.cookie = 'jsdebug=true;expires='+date.toGMTString()+';path=/;';
    } else dbug.disableCookie();
  },
  disableCookie: function(){
    dbug.log('disabling debugging cookie');
    document.cookie = 'jsdebug=false;path=/;';
  }
};

(function(){
  var fb = !!window.console || !!window.firebug;
  var con = window.firebug ? window.firebug.d.console.cmd : window.console;
  var debugMethods = ['debug','info','warn','error','assert','dir','dirxml'];
  var otherMethods = ['trace','group','groupEnd','profile','profileEnd','count'];
  function set(methodList, defaultFunction) {
    for(var i = 0; i < methodList.length; i++){
      dbug[methodList[i]] = (fb && con[methodList[i]])?con[methodList[i]]:defaultFunction;
    }
  };
  set(debugMethods, dbug.log);
  set(otherMethods, function(){});
})();
if ((!!window.console && !!window.console.warn) || window.firebug){
  dbug.firebug = true;
  var value = document.cookie.match('(?:^|;)\\s*jsdebug=([^;]*)');
  var debugCookie = value ? unescape(value[1]) : false;
  if(window.location.href.indexOf("jsdebug=true")>0 || debugCookie=='true') dbug.enable();
  if(debugCookie=='true')dbug.log('debugging cookie enabled');
  if(window.location.href.indexOf("jsdebugCookie=true")>0){
    dbug.cookie();
    if(!dbug.enabled)dbug.enable();
  }
  if(window.location.href.indexOf("jsdebugCookie=false")>0)dbug.disableCookie();
}


/*
Script: Occlude.js
  Prevents a class from being applied to a DOM element twice.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
var Occlude = new Class({
  // usage: if(this.occlude()) return this.occluded;
  occlude: function(property, element) {
    element = $(element || this.element);
    var instance = element.retrieve(property || this.property);
    if (instance && (this.occluded === null || this.occluded)) {
      this.occluded = instance; 
    } else {
      this.occluded = false;
      element.store(property || this.property, this);
    }
    return this.occluded||false;
  }
});

/*
Script: ToElement.js
  Defines the toElement method for a class.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
var ToElement = new Class({
  toElement: function(){
    return this.element;
  }
});

/*
Script: IframeShim.js
  Defines IframeShim, a class for obscuring select lists and flash objects in IE.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/  
var IframeShim = new Class({
  Implements: [Options, Events, Occlude, ToElement],
  options: {
    className:'iframeShim',
    display:false,
    zindex: null,
    margin: 0,
    offset: {
      x: 0,
      y: 0
    },
    browsers: (Browser.Engine.trident4 || (Browser.Engine.gecko && !Browser.Engine.gecko19 && Browser.Platform.mac))
  },
  property: 'IframeShim',
  initialize: function (element, options){
    this.element = $(element);
    if (this.occlude()) return this.occluded;
    this.setOptions(options);
    this.makeShim();
    return;
  },
  makeShim: function(){
    this.shim = new Element('iframe').store('IframeShim', this);
    if(!this.options.browsers) return;
    var pos = this.element.getStyle('position');
    if (pos == "static" || !pos) this.element.setStyle('position', 'relative');
    if(this.element.getStyle('z-Index').toInt()<1 || isNaN(this.element.getStyle('z-Index').toInt()))
      this.element.setStyle('z-Index',5);
    var z = this.element.getStyle('z-Index')-1;
    
    if($chk(this.options.zindex) && 
       this.element.getStyle('z-Index').toInt() > this.options.zindex)
       z = this.options.zindex;
      
     this.shim.set({
      src: (window.location.protocol.contains('https')) ? 'javascript:"<html></html>"' : 'javascript:void(0)',
      frameborder:'0',
      scrolling:'no',
      styles: {
        position: 'absolute',
        zIndex: z,
        border: 'none',
        filter: 'progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0)'
      },
      'class':this.options.className
    });

    var inject = function(){
      this.shim.inject(this.element, 'after');
      if(this.options.display) this.show();
      else this.hide();
      this.fireEvent('onInject');
    };
    if(Browser.Engine.trident && !IframeShim.ready) window.addEvent('load', inject.bind(this));
    else inject.run(null, this);
  },
  position: function(shim){
    if(!this.options.browsers || !IframeShim.ready) return this;
    var size = this.element.measure(function(){ return this.getSize(); });
    if($type(this.options.margin)){
      size.x = size.x-(this.options.margin*2);
      size.y = size.y-(this.options.margin*2);
      this.options.offset.x += this.options.margin; 
      this.options.offset.y += this.options.margin;
    }
     this.shim.set({
      'width': size.x,
      'height': size.y
    }).setPosition({
      relativeTo: this.element,
      offset: this.options.offset
    });
    return this;
  },
  hide: function(){
    if(this.options.browsers) this.shim.hide();
    return this;
  },
  show: function(){
    if(!this.options.browsers) return this;
    this.shim.show();
    return this.position();
  },
  dispose: function(){
    if(this.options.browsers) this.shim.dispose();
    return this;
  }
});
window.addEvent('load', function(){
  IframeShim.ready = true;
});


/*
Script: Element.Forms.js
  Extends the Element native object to include methods useful in managing inputs.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
Element.implement({
  tidy: function(){
    this.set('value', this.get('value').tidy());
  },
  getTextInRange: function(start, end) {
    return this.get('value').substring(start, end);
  },
  getSelectedText: function() {
    if(Browser.Engine.trident) return document.selection.createRange().text;
    return this.get('value').substring(this.getSelectionStart(), this.getSelectionEnd());
  },
  getIERanges: function(){
    this.focus();
    var range = document.selection.createRange();
    var re = this.createTextRange();
    var dupe = re.duplicate();
    re.moveToBookmark(range.getBookmark());
    dupe.setEndPoint('EndToStart', re);
    return { start: dupe.text.length, end: dupe.text.length + range.text.length, length: range.text.length, text: range.text };
  },
  getSelectionStart: function() {
    if(Browser.Engine.trident) return this.getIERanges().start;
    return this.selectionStart;
  },
  getSelectionEnd: function() {
    if(Browser.Engine.trident) return this.getIERanges().end;
    return this.selectionEnd;
  },
  getSelectedRange: function() {
    return {
      start: this.getSelectionStart(),
      end: this.getSelectionEnd()
    }
  },
  setCaretPosition: function(pos) {
    if(pos == 'end') pos = this.get('value').length;
    this.selectRange(pos, pos);
    return this;
  },
  getCaretPosition: function() {
    return this.getSelectedRange().start;
  },
  selectRange: function(start, end) {
    this.focus();
    if(Browser.Engine.trident) {
      var range = this.createTextRange();
      range.collapse(true);
      range.moveStart('character', start);
      range.moveEnd('character', end - start);
      range.select();
      return this;
    }
    this.setSelectionRange(start, end);
    return this;
  },
  insertAtCursor: function(value, select) {
    var start = this.getSelectionStart();
    var end = this.getSelectionEnd();
    this.set('value', this.get('value').substring(0, start) + value + this.get('value').substring(end, this.get('value').length));
     if($pick(select, true)) this.selectRange(start, start + value.length);
    else this.setCaretPosition(start + value.length);
    return this;
  },
  insertAroundCursor: function(options, select) {
    options = $extend({
      before: '',
      defaultMiddle: 'SOMETHING HERE',
      after: ''
    }, options);
    value = this.getSelectedText() || options.defaultMiddle;
    var start = this.getSelectionStart();
    var end = this.getSelectionEnd();
    if(start == end) {
      var text = this.get('value');
      this.set('value', text.substring(0, start) + options.before + value + options.after + text.substring(end, text.length));
      this.selectRange(start + options.before.length, end + options.before.length + value.length);
      text = null;
    } else {
      text = this.get('value').substring(start, end);
      this.set('value', this.get('value').substring(0, start) + options.before + text + options.after + this.get('value').substring(end, this.get('value').length));
      var selStart = start + options.before.length;
      if($pick(select, true)) this.selectRange(selStart, selStart + text.length);
      else this.setCaretPosition(selStart + text.length);
    }  
    return this;
  }
});


Element.Properties.inputValue = {
 
    get: function(){
       switch(this.get('tag')) {
         case 'select':
          vals = this.getSelected().map(function(op){ 
            var v = $pick(op.get('value'),op.get('text')); 
            return (v=="")?op.get('text'):v;
          });
          return this.get('multiple')?vals:vals[0];
        case 'input':
          switch(this.get('type')) {
            case 'checkbox':
              return this.get('checked')?this.get('value'):false;
            case 'radio':
              var checked;
              if (this.get('checked')) return this.get('value');
              $(this.getParent('form')||document.body).getElements('input').each(function(input){
                if (input.get('name') == this.get('name') && input.get('checked')) checked = input.get('value');
              }, this);
              return checked||null;
          }
         case 'input': case 'textarea':
          return this.get('value');
        default:
          return this.get('inputValue');
       }
    },
 
    set: function(value){
      switch(this.get('tag')){
        case 'select':
          this.getElements('option').each(function(op){
            var v = $pick(op.get('value'), op.get('text'));
            if (v=="") v = op.get('text');
            op.set('selected', $splat(value).contains(v));
          });
          break;
        case 'input':
          if (['radio','checkbox'].contains(this.get('type'))) {
            this.set('checked', $type(value)=="boolean"?value:$splat(value).contains(this.get('value')));
            break;
          }
        case 'textarea': case 'input':
          this.set('value', value);
          break;
        default:
          this.set('inputValue', value);
      }
      return this;
    },
    
  erase: function() {
    switch(this.get('tag')) {
      case 'select':
        this.getElements('option').each(function(op) {
          op.erase('selected');
        });
        break;
      case 'input':
        if (['radio','checkbox'].contains(this.get('type'))) {
          this.set('checked', false);
          break;
        }
      case 'input': case 'textarea':
        this.set('value', '');
        break;
      default:
        this.set('inputValue', '');
    }
    return this;
  }

};


/*
Script: Element.Measure.js
  Extends the Element native object to include methods useful in measuring dimensions.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/

Element.implement({

  // Daniel Steigerwald - MIT licence
  measure: function(fn) {
    var restore = this.expose();
    var result = fn.apply(this);
    restore();
    return result;
  },

  expose: function(){
    if (this.getStyle('display') != 'none') return $empty;
    var before = {};
    var styles = { visibility: 'hidden', display: 'block', position:'absolute' };
    //use this method instead of getStyles 
    $each(styles, function(value, style){
      before[style] = this.style[style]||'';
    }, this);
    //this.getStyles('visibility', 'display', 'position');
    this.setStyles(styles);
    return (function(){ this.setStyles(before); }).bind(this);
  },
  
  getDimensions: function(options) {
    options = $merge({computeSize: false},options);
    var dim = {};
    function getSize(el, options){
      return (options.computeSize)?el.getComputedSize(options):el.getSize();
    };
    if(this.getStyle('display') == 'none'){
      var restore = this.expose();
      dim = getSize(this, options); //works now, because the display isn't none
      restore(); //put it back where it was
    } else {
      try { //safari sometimes crashes here, so catch it
        dim = getSize(this, options);
      }catch(e){}
    }
    return $chk(dim.x)?$extend(dim, {width: dim.x, height: dim.y}):$extend(dim, {x: dim.width, y: dim.height});
  },
  
  getComputedSize: function(options){
    options = $merge({
      styles: ['padding','border'],
      plains: {height: ['top','bottom'], width: ['left','right']},
      mode: 'both'
    }, options);
    var size = {width: 0,height: 0};
    switch (options.mode){
      case 'vertical':
        delete size.width;
        delete options.plains.width;
        break;
      case 'horizontal':
        delete size.height;
        delete options.plains.height;
        break;
    };
    var getStyles = [];
    //this function might be useful in other places; perhaps it should be outside this function?
    $each(options.plains, function(plain, key){
      plain.each(function(edge){
        options.styles.each(function(style){
          getStyles.push((style=="border")?style+'-'+edge+'-'+'width':style+'-'+edge);
        });
      });
    });
    var styles = this.getStyles.apply(this, getStyles);
    var subtracted = [];
    $each(options.plains, function(plain, key){ //keys: width, height, plains: ['left','right'], ['top','bottom']
      size['total'+key.capitalize()] = 0;
      size['computed'+key.capitalize()] = 0;
      plain.each(function(edge){ //top, left, right, bottom
        size['computed'+edge.capitalize()] = 0;
        getStyles.each(function(style,i){ //padding, border, etc.
          //'padding-left'.test('left') size['totalWidth'] = size['width']+[padding-left]
          if(style.test(edge)) {
            styles[style] = styles[style].toInt(); //styles['padding-left'] = 5;
            if(isNaN(styles[style]))styles[style]=0;
            size['total'+key.capitalize()] = size['total'+key.capitalize()]+styles[style];
            size['computed'+edge.capitalize()] = size['computed'+edge.capitalize()]+styles[style];
          }
          //if width != width (so, padding-left, for instance), then subtract that from the total
          if(style.test(edge) && key!=style && 
            (style.test('border') || style.test('padding')) && !subtracted.contains(style)) {
            subtracted.push(style);
            size['computed'+key.capitalize()] = size['computed'+key.capitalize()]-styles[style];
          }
        });
      });
    });
    if($chk(size.width)) {
      size.width = size.width+this.offsetWidth+size.computedWidth;
      size.totalWidth = size.width + size.totalWidth;
      delete size.computedWidth;
    }
    if($chk(size.height)) {
      size.height = size.height+this.offsetHeight+size.computedHeight;
      size.totalHeight = size.height + size.totalHeight;
      delete size.computedHeight;
    }
    return $extend(styles, size);
  }
});


/*
Script: Element.Pin.js
  Extends the Element native object to include the pin method useful for fixed positioning for elements.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/

window.addEvent('domready', function(){
  var test = new Element('div').setStyles({
    position: 'fixed',
    top: 0,
    right: 0
  }).inject(document.body);
  var supported = (test.offsetTop === 0);
  test.dispose();
  Browser.supportsPositionFixed = supported;
});

Element.implement({
  pin: function(enable){
    if(!Browser.loaded) dbug.log('cannot pin ' + this + ' natively because the dom is not ready');
    if (this.getStyle('display') == 'none') {
      dbug.log('cannot pin ' + this + ' because it is hidden');
      return;
    }
    if(enable!==false) {
      var p = this.getPosition();
      if(!this.retrieve('pinned')) {
        var pos = {
          top: (p.y - window.getScroll().y),
          left: (p.x - window.getScroll().x)
        };
        if(Browser.supportsPositionFixed) {
          this.setStyle('position','fixed').setStyles(pos);
        } else {
          this.store('pinnedByJS', true);
          this.setStyles({
            position: 'absolute',
            top: p.y,
            left: p.x
          });
          this.store('scrollFixer', function(){
            if(this.retrieve('pinned')) {
              var to = {
                top: (pos.top.toInt() + window.getScroll().y),
                left: (pos.left.toInt() + window.getScroll().x)
              };
              this.setStyles(to);
            }
          }.bind(this));
          window.addEvent('scroll', this.retrieve('scrollFixer'));
        }
        this.store('pinned', true);
      }
    } else {
      var op;
      if (!Browser.Engine.trident) {
        if (this.getParent().getComputedStyle('position') != 'static') op = this.getParent();
        else op = this.getParent().getOffsetParent();
      }
      var p = this.getPosition(op);
      this.store('pinned', false);
      var reposition;
      if (Browser.supportsPositionFixed && !this.retrieve('pinnedByJS')) {
        reposition = {
          top: (p.y + window.getScroll().y),
          left: (p.x + window.getScroll().x)
        };
      } else {
        this.store('pinnedByJS', false);
        window.removeEvent('scroll', this.retrieve('scrollFixer'));
        reposition = {
          top: (p.y),
          left: (p.x)
        };
      }
      this.setStyles($merge(reposition, {position: 'absolute'}));
    }
    return this.addClass('isPinned');
  },
  unpin: function(){
    return this.pin(false).removeClass('isPinned');
  },
  togglepin: function(){
    this.pin(!this.retrieve('pinned'));
  }
});


/*
Script: Element.Position.js
  Extends the Element native object to include methods useful positioning elements relative to others.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/

Element.Properties.position = {

  set: function(options){
    this.setPosition(options);
  },

  get: function(options){
    if (options) this.setPosition(options);
    return this.getPosition();
  }

};

Element.implement({

  setPosition: function(options){
    $each(options||{}, function(v, k){ if (!$defined(v)) delete options[k]; });
    options = $merge({
      relativeTo: document.body,
      position: {
        x: 'center', //left, center, right
        y: 'center' //top, center, bottom
      },
      edge: false,
      offset: {x: 0, y: 0},
      returnPos: false,
      relFixedPosition: false,
      ignoreMargins: false,
      allowNegative: false
    }, options);
    //compute the offset of the parent positioned element if this element is in one
    var parentOffset = {x: 0, y: 0};
    var parentPositioned = false;
    /* dollar around getOffsetParent should not be necessary, but as it does not return
     * a mootools extended element in IE, an error occurs on the call to expose. See:
     * http://mootools.lighthouseapp.com/projects/2706/tickets/333-element-getoffsetparent-inconsistency-between-ie-and-other-browsers */
    var offsetParent = this.measure(function(){
      return $(this.getOffsetParent());
    });
    if (offsetParent && offsetParent != this.getDocument().body){
      parentOffset = offsetParent.measure(function(){
        return this.getPosition();
      });
      parentPositioned = true;
      options.offset.x = options.offset.x - parentOffset.x;
      options.offset.y = options.offset.y - parentOffset.y;
    }
    //upperRight, bottomRight, centerRight, upperLeft, bottomLeft, centerLeft
    //topRight, topLeft, centerTop, centerBottom, center
    var fixValue = function(option){
      if ($type(option) != "string") return option;
      option = option.toLowerCase();
      var val = {};
      if (option.test('left')) val.x = 'left';
      else if (option.test('right')) val.x = 'right';
      else val.x = 'center';
      if (option.test('upper') || option.test('top')) val.y = 'top';
      else if (option.test('bottom')) val.y = 'bottom';
      else val.y = 'center';
      return val;
    };
    options.edge = fixValue(options.edge);
    options.position = fixValue(options.position);
    if (!options.edge){
      if (options.position.x == 'center' && options.position.y == 'center') options.edge = {x:'center', y:'center'};
      else options.edge = {x:'left', y:'top'};
    }

    this.setStyle('position', 'absolute');
    var rel = $(options.relativeTo) || document.body;
    var calc = rel == document.body ? window.getScroll() : rel.getPosition();
    var top = calc.y;
    var left = calc.x;

    if (Browser.Engine.trident){
      var scrolls = rel.getScrolls();
      top += scrolls.y;
      left += scrolls.x;
    }

    var dim = this.getDimensions({computeSize: true, styles:['padding', 'border','margin']});
    if (options.ignoreMargins){
      options.offset.x = options.offset.x - dim['margin-left'];
      options.offset.y = options.offset.y - dim['margin-top'];
    }
    var pos = {};
    var prefY = options.offset.y;
    var prefX = options.offset.x;
    var winSize = window.getSize();
    switch(options.position.x){
      case 'left':
        pos.x = left + prefX;
        break;
      case 'right':
        pos.x = left + prefX + rel.offsetWidth;
        break;
      default: //center
        pos.x = left + ((rel == document.body ? winSize.x : rel.offsetWidth)/2) + prefX;
        break;
    };
    switch(options.position.y){
      case 'top':
        pos.y = top + prefY;
        break;
      case 'bottom':
        pos.y = top + prefY + rel.offsetHeight;
        break;
      default: //center
        pos.y = top + ((rel == document.body ? winSize.y : rel.offsetHeight)/2) + prefY;
        break;
    };

    if (options.edge){
      var edgeOffset = {};

      switch(options.edge.x){
        case 'left':
          edgeOffset.x = 0;
          break;
        case 'right':
          edgeOffset.x = -dim.x-dim.computedRight-dim.computedLeft;
          break;
        default: //center
          edgeOffset.x = -(dim.x/2);
          break;
      };
      switch(options.edge.y){
        case 'top':
          edgeOffset.y = 0;
          break;
        case 'bottom':
          edgeOffset.y = -dim.y-dim.computedTop-dim.computedBottom;
          break;
        default: //center
          edgeOffset.y = -(dim.y/2);
          break;
      };
      pos.x = pos.x + edgeOffset.x;
      pos.y = pos.y + edgeOffset.y;
    }
    pos = {
      left: ((pos.x >= 0 || parentPositioned || options.allowNegative) ? pos.x : 0).toInt(),
      top: ((pos.y >= 0 || parentPositioned || options.allowNegative) ? pos.y : 0).toInt()
    };
    if (rel.getStyle('position') == "fixed" || options.relFixedPosition){
      var winScroll = window.getScroll();
      pos.top = pos.top.toInt() + winScroll.y;
      pos.left = pos.left.toInt() + winScroll.x;
    }

    if (options.returnPos) return pos;
    else this.setStyles(pos);
    return this;
  }

});


/*
Script: Element.Shortcuts.js
  Extends the Element native object to include some shortcut methods.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/

Element.implement({
  isVisible: function() {
    return this.getStyle('display') != 'none';
  },
  toggle: function() {
    return this[this.isVisible() ? 'hide' : 'show']();
  },
  hide: function() {
    var d;
    try {
      //IE fails here if the element is not in the dom
      if ('none' != this.getStyle('display')) d = this.getStyle('display');
    } catch(e){}
    this.store('originalDisplay', d||''); 
    this.setStyle('display','none');
    return this;
  },
  show: function(display) {
    original = this.retrieve('originalDisplay')?this.retrieve('originalDisplay'):this.get('originalDisplay');
    this.setStyle('display',(display || original || ''));
    return this;
  },
  swapClass: function(remove, add) {
    return this.removeClass(remove).addClass(add);
  },
  //TODO
  //DO NOT USE THIS METHOD
  //it is temporary, as Mootools 1.1 will negate its requirement
  fxOpacityOk: function(){
    return !Browser.Engine.trident4;
  } 
});


//returns a collection given an id or a selector
$G = function(elements) {
  return $splat($(elements)||$$(elements));
};

/*
Script: modalizer.js
  Defines Modalizer: functionality to overlay the window contents with a semi-transparent layer that prevents interaction with page content until it is removed

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
var Modalizer = new Class({
  defaultModalStyle: {
    display:'block',
    position:'fixed',
    top:0,
    left:0,  
    'z-index':5000,
    'background-color':'#333',
    opacity:0.8
  },
  setModalOptions: function(options){
    this.modalOptions = $merge({
      width:(window.getScrollSize().x),
      height:(window.getScrollSize().y),
      elementsToHide: 'select, embed, object',
      hideOnClick: true,
      modalStyle: {},
      updateOnResize: true,
      layerId: 'modalOverlay',
      onModalHide: $empty,
      onModalShow: $empty
    }, this.modalOptions, options);
    return this;
  },
  layer: function(){
    if (!this.modalOptions.layerId) this.setModalOptions();
    return $(this.modalOptions.layerId) || new Element('div', {id: this.modalOptions.layerId}).inject(document.body);
  },
  resize: function(){
    if (this.layer()) {
      this.layer().setStyles({
        width:(window.getScrollSize().x),
        height:(window.getScrollSize().y)
      });
    }
  },
  setModalStyle: function (styleObject){
    this.modalOptions.modalStyle = styleObject;
    this.modalStyle = $merge(this.defaultModalStyle, {
      width:this.modalOptions.width,
      height:this.modalOptions.height
    }, styleObject);
    if (this.layer()) this.layer().setStyles(this.modalStyle);
    return(this.modalStyle);
  },
  modalShow: function(options){
    this.setModalOptions(options);
    this.layer().setStyles(this.setModalStyle(this.modalOptions.modalStyle));
    if (Browser.Engine.trident4) this.layer().setStyle('position','absolute');
    this.layer().removeEvents('click').addEvent('click', function(){
      this.modalHide(this.modalOptions.hideOnClick);
    }.bind(this));
    this.bound = this.bound||{};
    if (!this.bound.resize && this.modalOptions.updateOnResize) {
      this.bound.resize = this.resize.bind(this);
      window.addEvent('resize', this.bound.resize);
    }
    if ($type(this.modalOptions.onModalShow)  == "function") this.modalOptions.onModalShow();
    this.togglePopThroughElements(0);
    this.layer().setStyle('display','block');
    return this;
  },
  modalHide: function(override, force){
    if (override === false) return false; //this is internal, you don't need to pass in an argument
    this.togglePopThroughElements(1);
    if ($type(this.modalOptions.onModalHide) == "function") this.modalOptions.onModalHide();
    this.layer().setStyle('display','none');
    if (this.modalOptions.updateOnResize) {
      this.bound = this.bound||{};
      if (!this.bound.resize) this.bound.resize = this.resize.bind(this);
      window.removeEvent('resize', this.bound.resize);
    }
    return this;
  },
  togglePopThroughElements: function(opacity){
    if (Browser.Engine.trident4 || (Browser.Engine.gecko && Browser.Platform.mac)) {
      $$(this.modalOptions.elementsToHide).each(function(sel){
        sel.setStyle('opacity', opacity);
      });
    }
  }
});

/*
Script: StyleWriter.js

Provides a simple method for injecting a css style element into the DOM if it's not already present.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/

var StyleWriter = new Class({
  createStyle: function(css, id) {
    window.addEvent('domready', function(){
      try {
        if($(id) && id) return;
        var style = new Element('style', {id: id||''}).inject($$('head')[0]);
        if (Browser.Engine.trident) style.styleSheet.cssText = css;
        else style.set('text', css);
      }catch(e){dbug.log('error: %s',e);}
    }.bind(this));
  }
});

/*
Script: StickyWin.js

Creates a div within the page with the specified contents at the location relative to the element you specify; basically an in-page popup maker.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/

var StickyWin = new Class({
  Implements: [Options, Events, StyleWriter, ToElement],
  options: {
//    onDisplay: $empty,
//    onClose: $empty,
    closeClassName: 'closeSticky',
    pinClassName: 'pinSticky',
    content: '',
    zIndex: 10000,
    className: '',
//    id: ... set above in initialize function
/*    these are the defaults for setPosition anyway
    ************************************************
    edge: false, //see Element.setPosition
    position: 'center', //center, corner == upperLeft, upperRight, bottomLeft, bottomRight
    offset: {x:0,y:0},
    relativeTo: document.body, */
    width: false,
    height: false,
    timeout: -1,
    allowMultipleByClass: false,
    allowMultiple: true,
    showNow: true,
    useIframeShim: true,
    iframeShimSelector: '',
    inject: {
      where: 'bottom' 
    }
  },
  css: '.SWclearfix:after {content: "."; display: block; height: 0; clear: both; visibility: hidden;}'+
       '.SWclearfix {display: inline-table;}'+
       '* html .SWclearfix {height: 1%;}'+
       '.SWclearfix {display: block;}',
  initialize: function(options){
    this.setOptions(options);
    this.options.inject.target = this.options.inject.target || document.body;
    this.id = this.options.id || 'StickyWin_'+new Date().getTime();
    this.makeWindow();

    if(this.options.content) this.setContent(this.options.content);
    if(this.options.timeout > 0) {
      this.addEvent('onDisplay', function(){
        this.hide.delay(this.options.timeout, this)
      }.bind(this));
    }
    if(this.options.showNow) this.show();
    //add css for clearfix
    this.createStyle(this.css, 'StickyWinClearFix');
  },
  makeWindow: function(){
    this.destroyOthers();
    if(!$(this.id)) {
      this.win = new Element('div', {
        id:    this.id
      }).addClass(this.options.className).addClass('StickyWinInstance').addClass('SWclearfix').setStyles({
         display:'none',
        position:'absolute',
        zIndex:this.options.zIndex
      }).inject(this.options.inject.target, this.options.inject.where).store('StickyWin', this);      
    } else this.win = $(this.id);
    this.element = this.win;
    if(this.options.width && $type(this.options.width.toInt())=="number") this.win.setStyle('width', this.options.width.toInt());
    if(this.options.height && $type(this.options.height.toInt())=="number") this.win.setStyle('height', this.options.height.toInt());
    return this;
  },
  show: function(suppressEvent){
    this.showWin();
    if (!suppressEvent) this.fireEvent('onDisplay');
    if(this.options.useIframeShim) this.showIframeShim();
    this.visible = true;
    return this;
  },
  showWin: function(){
    if(!this.positioned) this.position();
    this.win.show();
  },
  hide: function(suppressEvent){
    if(!suppressEvent) this.fireEvent('onClose');
    this.hideWin();
    if(this.options.useIframeShim) this.hideIframeShim();
    this.visible = false;
    return this;
  },
  hideWin: function(){
    this.win.setStyle('display','none');
  },
  destroyOthers: function() {
    if(!this.options.allowMultipleByClass || !this.options.allowMultiple) {
      $$('div.StickyWinInstance').each(function(sw) {
        // weird, on FF18+, sw.hasClass("") === true (OBMFULL-4567)
        var hasClassName = this.options.className ? true : false;
        if(!this.options.allowMultiple || (!this.options.allowMultipleByClass && hasClassName && sw.hasClass(this.options.className)))
          sw.retrieve('StickyWin').destroy();
      }, this);
    }
  },
  setContent: function(html) {
    if(this.win.getChildren().length>0) this.win.empty();
    if($type(html) == "string") this.win.set('html', html);
    else if ($(html)) this.win.adopt(html);
    this.win.getElements('.'+this.options.closeClassName).each(function(el){
      el.addEvent('click', this.hide.bind(this));
    }, this);
    this.win.getElements('.'+this.options.pinClassName).each(function(el){
      el.addEvent('click', this.togglepin.bind(this));
    }, this);
    return this;
  },  
  position: function(options){
    this.positioned = true;
    this.setOptions(options);
    this.win.setPosition({
      allowNegative: true,
      relativeTo: this.options.relativeTo,
      position: this.options.position,
      offset: this.options.offset,
      edge: this.options.edge
    });
    if(this.shim) this.shim.position();
    return this;
  },
  pin: function(pin) {
    if(!this.win.pin) {
      dbug.log('you must include element.pin.js!');
      return this;
    }
    this.pinned = $pick(pin, true);
    this.win.pin(pin);
    return this;
  },
  unpin: function(){
    return this.pin(false);
  },
  togglepin: function(){
    return this.pin(!this.pinned);
  },
  makeIframeShim: function(){
    if(!this.shim){
      var el = (this.options.iframeShimSelector)?this.win.getElement(this.options.iframeShimSelector):this.win;
      this.shim = new IframeShim(el, {
        display: false,
        name: 'StickyWinShim'
      });
    }
  },
  showIframeShim: function(){
    if(this.options.useIframeShim) {
      this.makeIframeShim();
      this.shim.show();
    }
  },
  hideIframeShim: function(){
    if(this.shim) this.shim.hide();
  },
  destroy: function(){
    if (this.win) this.win.dispose();
    if(this.options.useIframeShim && this.shim) this.shim.dispose();
    if($('modalOverlay'))$('modalOverlay').dispose();
  }
});


/*
Script: StickyWin.Modal.js

This script extends StickyWin and StickyWin.Fx classes to add Modalizer functionality.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
(function(){
var modalWinBase = function(extend){
  return {
    Extends: extend,
    initialize: function(options){
      options = options||{};
      this.setModalOptions($merge(options.modalOptions||{}, {
        onModalHide: function(){
            this.hide(false);
          }.bind(this)
        }));
      this.parent(options);
    },
    show: function(showModal){
      if($pick(showModal, true)) {
        this.modalShow();
        if (this.modalOptions.elementsToHide) this.win.getElements(this.modalOptions.elementsToHide).setStyle('opacity', 1);
      }
      this.parent();
    },
    hide: function(hideModal){
      if($pick(hideModal, true)) this.modalHide();
      else this.parent();
    }
  }
};

StickyWin.Modal = new Class(modalWinBase(StickyWin));
StickyWin.Modal.implement(new Modalizer());
if (StickyWin.Fx) StickyWin.Fx.Modal = new Class(modalWinBase(StickyWin.Fx));
try { StickyWin.Fx.Modal.implement(new Modalizer()); }catch(e){}
})();
//legacy
var StickyWinModal = StickyWin.Modal;
if (StickyWin.Fx) var StickyWinFxModal = StickyWin.Fx.Modal;

/*
Script: OverText.js
  Shows text over an input that disappears when the user clicks into it. The text remains hidden if the user adds a value.

License:
  http://www.clientcide.com/wiki/cnet-libraries#license
*/
var OverText = new Class({
  Implements: [Options, Events],
  options: {
//  textOverride: null,
    positionOptions: {
      position:"upperLeft",
      edge:"upperLeft",
      offset: {
        x: 4,
        y: 2
      }
    },
    poll: false,
    pollInterval: 250
//  onTextHide: $empty,
//  onTextShow: $empty
  },
  overTxtEls: [],
  initialize: function(inputs, options) {
    this.setOptions(options);
    $G(inputs).each(this.addElement, this);
    OverText.instances.push(this);
    if (this.options.poll) this.poll();
  },
  addElement: function(el){
    if (el.retrieve('OverText')) return;
    var val = this.options.textOverride || el.get('alt') || el.get('title');
    if (!val) return;
    this.overTxtEls.push(el);
    var txt = new Element('div', {
      'class': 'overTxtDiv',
      styles: {
        lineHeight: 'normal',
        position: 'absolute'
      },
      html: val,
                  title: val,
      events: {
        click: this.hideTxt.pass([el, true], this)
      }
    }).inject(el, 'after');
    el.addEvents({
      focus: this.hideTxt.pass([el, true], this),
      blur: this.testOverTxt.pass(el, this),
      change: this.testOverTxt.pass(el, this)
    }).store('OverTextDiv', txt).store('OverText', this);
    window.addEvent('resize', this.repositionAll.bind(this));
    this.testOverTxt(el);
    this.repositionOverTxt(el);
  },
  startPolling: function(){
    this.pollingPaused = false;
    return this.poll();
  },
  poll: function(stop) {
    //start immediately
    //pause on focus
    //resumeon blur
    if (this.poller && !stop) return this;
    var test = function(){
      if (this.pollingPaused == true) return;
      this.overTxtEls.each(function(el){
        if (el.retrieve('ot_paused')) return;
        this.testOverTxt(el);
      }, this);
    }.bind(this);
    if (stop) $clear(this.poller);
    else this.poller = test.periodical(this.options.pollInterval, this);
    return this;
  },
  stopPolling: function(){
    this.pollingPaused = true;
    return this.poll(true);
  },
  hideTxt: function(el, focus){
    var txt = el.retrieve('OverTextDiv');
    if (txt && txt.isVisible() && !el.get('disabled')) {
      txt.hide(); 
      try {
        if (focus) el.fireEvent('focus').focus();
      } catch(e){} //IE barfs if you call focus on hidden elements
      this.fireEvent('onTextHide', [txt, el]);
      el.store('ot_paused', true);
    }
    return this;
  },
  showTxt: function(el){
    var txt = el.retrieve('OverTextDiv');
    if (txt && !txt.isVisible()) {
      txt.show();
      this.fireEvent('onTextShow', [txt, el]);
      el.store('ot_paused', false);
    }
    return this;
  },
  testOverTxt: function(el){
    if (el.get('value')) this.hideTxt(el);
    else this.showTxt(el);  
  },
  repositionAll: function(){
    this.overTxtEls.each(this.repositionOverTxt.bind(this));
    return this;
  },
  repositionOverTxt: function (el){
    if (!el) return;
    try {
      var txt = el.retrieve('OverTextDiv');
      if (!txt || !el.getParent()) return;
      this.testOverTxt(el);
      txt.setPosition($merge(this.options.positionOptions, {relativeTo: el}));
                        txt.setStyle('width', (el.getWidth() - this.options.positionOptions.offset.x) + 'px');
      if (el.offsetHeight) this.testOverTxt(el);
      else this.hideTxt(el);
    } catch(e){
      dbug.log('overTxt error: ', e);
    }
    return this;
  }
});
OverText.instances = [];
OverText.update = function(){
  return OverText.instances.map(function(ot){
    return ot.repositionAll();
  });
};

/**
 * Autocompleter
 *
 * @version    1.1.1
 *
 * @todo: Caching, no-result handling!
 *
 *
 * @license    MIT-style license
 * @author    Harald Kirschner <mail [at] digitarald.de>
 * @copyright  Author
 */
var Autocompleter = {};

var OverlayFix = IframeShim;

Autocompleter.Base = new Class({
  
  Implements: [Options, Events],
  
  options: {
    minLength: 1,
    markQuery: true,
    width: 'inherit',
    maxChoices: 10,
//    injectChoice: null,
//    customChoices: null,
    className: 'autocompleter-choices',
    zIndex: 42,
    delay: 400,
    observerOptions: {},
    fxOptions: {},
//    onSelection: $empty,
//    onShow: $empty,
//    onHide: $empty,
//    onBlur: $empty,
//    onFocus: $empty,

    autoSubmit: false,
    overflow: false,
    overflowMargin: 25,
    selectFirst: false,
    filter: null,
    filterCase: false,
    filterSubset: false,
    forceSelect: false,
    selectMode: true,
    choicesMatch: null,

    multiple: false,
    separator: ', ',
    separatorSplit: /\s*[,;]\s*/,
    autoTrim: true,
    allowDupes: false,

    cache: true,
    relative: false
  },

  initialize: function(element, options) {
    this.element = $(element);
    this.setOptions(options);
    this.build();
    this.observer = new Observer(this.element, this.prefetch.bind(this), $merge({
      'delay': this.options.delay
    }, this.options.observerOptions));
    this.queryValue = null;
    if (this.options.filter) this.filter = this.options.filter.bind(this);
    var mode = this.options.selectMode;
    this.typeAhead = (mode == 'type-ahead');
    this.selectMode = (mode === true) ? 'selection' : mode;
    this.cached = [];
  },

  /**
   * build - Initialize DOM
   *
   * Builds the html structure for choices and appends the events to the element.
   * Override this function to modify the html generation.
   */
  build: function() {
    if ($(this.options.customChoices)) {
      this.choices = this.options.customChoices;
    } else {
      this.choices = new Element('ul', {
        'class': this.options.className,
        'styles': {
          'zIndex': this.options.zIndex
        }
      }).inject(document.body);
      this.relative = false;
      if (this.options.relative) {
        this.choices.inject(this.element, 'after');
        this.relative = this.element.getOffsetParent();
      }
      this.fix = new OverlayFix(this.choices);
    }
    if (!this.options.separator.test(this.options.separatorSplit)) {
      this.options.separatorSplit = this.options.separator;
    }
    this.fx = (!this.options.fxOptions) ? null : new Fx.Tween(this.choices, $merge({
      'property': 'opacity',
      'link': 'cancel',
      'duration': 200
    }, this.options.fxOptions)).addEvent('onStart', Chain.prototype.clearChain).set(0);
    this.element.setProperty('autocomplete', 'off')
      .addEvent((Browser.Engine.trident || Browser.Engine.webkit) ? 'keydown' : 'keypress', this.onCommand.bind(this))
      .addEvent('click', this.onCommand.bind(this, [false]))
      .addEvent('focus', this.toggleFocus.create({bind: this, arguments: true, delay: 100}));
      //.addEvent('blur', this.toggleFocus.create({bind: this, arguments: false, delay: 100}));
    document.addEvent('click', function(e){
      if (e.target != this.choices) this.toggleFocus(false);
    }.bind(this));
  },

  destroy: function() {
    if (this.fix) this.fix.dispose();
    this.choices = this.selected = this.choices.destroy();
  },

  toggleFocus: function(state) {
    this.focussed = state;
    if (!state) this.hideChoices(true);
    this.fireEvent((state) ? 'onFocus' : 'onBlur', [this.element]);
  },

  onCommand: function(e) {
    if (!e && this.focussed) return this.prefetch();
    if (e && e.key && !e.shift) {
      switch (e.key) {
        case 'enter':
          if (this.element.value != this.opted) return true;
          if (this.selected && this.visible) {
            this.choiceSelect(this.selected);
            return !!(this.options.autoSubmit);
          }
          break;
        case 'up': case 'down':
          if (!this.prefetch() && this.queryValue !== null) {
            var up = (e.key == 'up');
            this.choiceOver((this.selected || this.choices)[
              (this.selected) ? ((up) ? 'getPrevious' : 'getNext') : ((up) ? 'getLast' : 'getFirst')
            ](this.options.choicesMatch), true);
          }
          return false;
        case 'esc': case 'tab':
          this.hideChoices(true);
          break;
      }
    }
    return true;
  },

  setSelection: function(finish) {
    var input = this.selected.inputValue, value = input;
    var start = this.queryValue.length, end = input.length;
    if (input.substr(0, start).toLowerCase() != this.queryValue.toLowerCase()) start = 0;
    if (this.options.multiple) {
      var split = this.options.separatorSplit;
      value = this.element.value;
      start += this.queryIndex;
      end += this.queryIndex;
      var old = value.substr(this.queryIndex).split(split, 1)[0];
      value = value.substr(0, this.queryIndex) + input + value.substr(this.queryIndex + old.length);
      if (finish) {
        var space = /[^\s,]+/;
        var tokens = value.split(this.options.separatorSplit).filter(space.test, space);
        if (!this.options.allowDupes) tokens = [].combine(tokens);
        var sep = this.options.separator;
        value = tokens.join(sep) + sep;
        end = value.length;
      }
    }
    this.observer.setValue(value);
    this.opted = value;
    if (finish || this.selectMode == 'pick') start = end;
    this.element.selectRange(start, end);
    this.fireEvent('onSelection', [this.element, this.selected, value, input]);
  },

  showChoices: function() {
    var match = this.options.choicesMatch, first = this.choices.getFirst(match);
    this.selected = this.selectedValue = null;
    if (this.fix) {
      var pos = this.element.getCoordinates(this.relative), width = this.options.width || 'auto';
      this.choices.setStyles({
        'left': pos.left,
        'top': pos.bottom,
        'width': (width === true || width == 'inherit') ? pos.width : width
      });
    }
    if (!first) return;
    if (!this.visible) {
      this.visible = true;
      this.choices.setStyle('display', '');
      if (this.fx) this.fx.start(1);
      this.fireEvent('onShow', [this.element, this.choices]);
    }
    if (this.options.selectFirst || this.typeAhead || first.inputValue == this.queryValue) this.choiceOver(first, this.typeAhead);
    var items = this.choices.getChildren(match), max = this.options.maxChoices;
    var styles = {'overflowY': 'hidden', 'height': ''};
    this.overflown = false;
    if (items.length > max) {
      var item = items[max - 1];
      styles.overflowY = 'scroll';
      styles.height = item.getCoordinates(this.choices).bottom;
      this.overflown = true;
    };
    this.choices.setStyles(styles);
    this.fix.show();
  },

  hideChoices: function(clear) {
    if (clear) {
      var value = this.element.value;
      if (this.options.forceSelect) value = this.opted;
      if (this.options.autoTrim) {
        value = value.split(this.options.separatorSplit).filter($arguments(0)).join(this.options.separator);
      }
      this.observer.setValue(value);
    }
    if (!this.visible) return;
    this.visible = false;
    this.observer.clear();
    var hide = function(){
      this.choices.setStyle('display', 'none');
      this.fix.hide();
    }.bind(this);
    if (this.fx) this.fx.start(0).chain(hide);
    else hide();
    this.fireEvent('onHide', [this.element, this.choices]);
  },

  prefetch: function() {
    var value = this.element.value, query = value;
    if (this.options.multiple) {
      var split = this.options.separatorSplit;
      var values = value.split(split);
      var index = this.element.getCaretPosition();
      var toIndex = value.substr(0, index).split(split);
      var last = toIndex.length - 1;
      index -= toIndex[last].length;
      query = values[last];
    }
    if (query.length < this.options.minLength) {
      this.hideChoices();
    } else {
      if (query === this.queryValue || (this.visible && query == this.selectedValue)) {
        if (this.visible) return false;
        this.showChoices();
      } else {
        this.queryValue = query;
        this.queryIndex = index;
        if (!this.fetchCached()) this.query();
      }
    }
    return true;
  },

  fetchCached: function() {
    return false;
    if (!this.options.cache
      || !this.cached
      || !this.cached.length
      || this.cached.length >= this.options.maxChoices
      || this.queryValue) return false;
    this.update(this.filter(this.cached));
    return true;
  },

  update: function(tokens) {
    this.choices.empty();
    this.cached = tokens;
    if (!tokens || !tokens.length) {
      this.hideChoices();
    } else {
      if (this.options.maxChoices < tokens.length && !this.options.overflow) tokens.length = this.options.maxChoices;
      tokens.each(this.options.injectChoice || function(token){
        var choice = new Element('li', {'html': this.markQueryValue(token)});
        choice.inputValue = token;
        this.addChoiceEvents(choice).inject(this.choices);
      }, this);
      this.showChoices();
    }
  },

  choiceOver: function(choice, selection) {
    if (!choice || choice == this.selected) return;
    if (this.selected) this.selected.removeClass('autocompleter-selected');
    this.selected = choice.addClass('autocompleter-selected');
    this.fireEvent('onSelect', [this.element, this.selected, selection]);
    if (!selection) return;
    this.selectedValue = this.selected.inputValue;
    if (this.overflown) {
      var coords = this.selected.getCoordinates(this.choices), margin = this.options.overflowMargin,
        top = this.choices.scrollTop, height = this.choices.offsetHeight, bottom = top + height;
      if (coords.top - margin < top && top) this.choices.scrollTop = Math.max(coords.top - margin, 0);
      else if (coords.bottom + margin > bottom) this.choices.scrollTop = Math.min(coords.bottom - height + margin, bottom);
    }
    if (this.selectMode) this.setSelection();
  },

  choiceSelect: function(choice) {
    if (choice) this.choiceOver(choice);
    this.setSelection(true);
    this.queryValue = false;
    this.hideChoices();
  },

  filter: function(tokens) {
    var regex = new RegExp(((this.options.filterSubset) ? '' : '^') + this.queryValue.escapeRegExp(), (this.options.filterCase) ? '' : 'i');
    return (tokens || this.tokens).filter(regex.test, regex);
  },

  /**
   * markQueryValue
   *
   * Marks the queried word in the given string with <span class="autocompleter-queried">*</span>
   * Call this i.e. from your custom parseChoices, same for addChoiceEvents
   *
   * @param    {String} Text
   * @return    {String} Text
   */
  markQueryValue: function(str) {
    return (!this.options.markQuery || !this.queryValue) ? str
      : str.replace(new RegExp('(' + ((this.options.filterSubset) ? '' : '^') + this.queryValue.escapeRegExp() + ')', (this.options.filterCase) ? '' : 'i'), '<span class="autocompleter-queried">$1</span>');
  },

  /**
   * addChoiceEvents
   *
   * Appends the needed event handlers for a choice-entry to the given element.
   *
   * @param    {Element} Choice entry
   * @return    {Element} Choice entry
   */
  addChoiceEvents: function(el) {
    return el.addEvents({
      'mouseover': this.choiceOver.bind(this, [el]),
      'click': this.choiceSelect.bind(this, [el])
    });
  }
});


/**
 * Autocompleter.Local
 *
 * @version    1.1.1
 *
 * @todo: Caching, no-result handling!
 *
 *
 * @license    MIT-style license
 * @author    Harald Kirschner <mail [at] digitarald.de>
 * @copyright  Author
 */
Autocompleter.Local = new Class({

  Extends: Autocompleter.Base,

  options: {
    minLength: 0,
    delay: 200
  },

  initialize: function(element, tokens, options) {
    this.parent(element, options);
    this.tokens = tokens;
  },

  query: function() {
    this.update(this.filter());
  }

});


/**
 * Autocompleter.Remote
 *
 * @version    1.1.1
 *
 * @todo: Caching, no-result handling!
 *
 *
 * @license    MIT-style license
 * @author    Harald Kirschner <mail [at] digitarald.de>
 * @copyright  Author
 */

Autocompleter.Ajax = {};

Autocompleter.Ajax.Base = new Class({

  Extends: Autocompleter.Base,

  options: {
    postVar: 'value',
    postData: {},
    ajaxOptions: {},
    onRequest: $empty,
    onComplete: $empty
  },

  initialize: function(element, options) {
    this.parent(element, options);
    var indicator = $(this.options.indicator);
    if (indicator) {
      this.addEvents({
        'onRequest': indicator.show.bind(indicator),
        'onComplete': indicator.hide.bind(indicator)
      }, true);
    }
  },

  query: function(){
    var data = $unlink(this.options.postData);
    data[this.options.postVar] = this.queryValue;
    this.fireEvent('onRequest', [this.element, this.request, data, this.queryValue]);
    this.request.send({'data': data});
  },

  /**
   * queryResponse - abstract
   *
   * Inherated classes have to extend this function and use this.parent(resp)
   *
   * @param    {String} Response
   */
  queryResponse: function() {
    this.fireEvent('onComplete', [this.element, this.request, this.response]);
  }

});

Autocompleter.Ajax.Json = new Class({

  Extends: Autocompleter.Ajax.Base,

  initialize: function(el, url, options) {
    this.parent(el, options);
    this.request = new Request.JSON($merge({
      'url': url,
      'link': 'cancel'
    }, this.options.ajaxOptions)).addEvent('onComplete', this.queryResponse.bind(this));
  },

  queryResponse: function(response) {
    this.parent();
    this.update(response);
  }

});

Autocompleter.Ajax.Xhtml = new Class({

  Extends: Autocompleter.Ajax.Base,

  initialize: function(el, url, options) {
    this.parent(el, options);
    this.request = new Request.HTML($merge({
      'url': url,
      'link': 'cancel',
      'update': this.choices
    }, this.options.ajaxOptions)).addEvent('onComplete', this.queryResponse.bind(this));
  },

  queryResponse: function(tree, elements) {
    this.parent();
    if (!elements || !elements.length) {
      this.hideChoices();
    } else {
      this.choices.getChildren(this.options.choicesMatch).each(this.options.injectChoice || function(choice) {
        var value = choice.innerHTML;
        choice.inputValue = value;
        this.addChoiceEvents(choice.set('html', this.markQueryValue(value)));
      }, this);
      this.showChoices();
    }

  }

});


/**
 * Observer - Observe formelements for changes
 *
 * @version    1.0rc3
 *
 * @license    MIT-style license
 * @author    Harald Kirschner <mail [at] digitarald.de>
 * @copyright  Author
 */
var Observer = new Class({

  Implements: [Options, Events],

  options: {
    periodical: false,
    delay: 1000
  },

  initialize: function(el, onFired, options){
    this.setOptions(options);
    this.addEvent('onFired', onFired);
    this.element = $(el) || $$(el);
    /* Clientcide change */
    this.boundChange = this.changed.bind(this);
    this.resume();
  },

  changed: function() {
    var value = this.element.get('value');
    if ($equals(this.value, value)) return;
    this.clear();
    this.value = value;
    this.timeout = this.onFired.delay(this.options.delay, this);
  },

  setValue: function(value) {
    this.value = value;
    this.element.set('value', value);
    return this.clear();
  },

  onFired: function() {
    this.fireEvent('onFired', [this.value, this.element]);
  },

  clear: function() {
    $clear(this.timeout || null);
    return this;
  },
  /* Clientcide change */
  pause: function(){
    $clear(this.timeout);
    $clear(this.timer);
    this.element.removeEvent('keyup', this.boundChange);
    return this;
  },
  resume: function(){
    this.value = this.element.get('value');
    if (this.options.periodical) this.timer = this.changed.periodical(this.options.periodical, this);
    else this.element.addEvent('keyup', this.boundChange);
    return this;
  }

});

var $equals = function(obj1, obj2) {
  return (obj1 == obj2 || JSON.encode(obj1) == JSON.encode(obj2));
};
