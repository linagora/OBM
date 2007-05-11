Element.extend({
  observe: function(options) {
    return new Observer(this, options); 
  } 
}); 

var Observer = new Class({ 

  setOptions: function(options) {
    this.options = Object.extend({
      property: 'width',
      frequency: '500',
      onStart: Class.empty,
      onChange: Class.empty,
      onStop: Class.empty
    }, options || {});

  },

  initialize: function(el, options) {
    this.setOptions(options);
    this.el = $(el);
    this.lastValue = this.setValue();
    this.change = false; 
    this.timer = this.check.periodical(this.options.frequency, this);
  },
  
  setValue: function() {
    v = this.el[this.options.property];
    if(!v) 
      v = this.el.getStyle(this.options.property);
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
    this.options.onChange(this.el, v);
  }
});


HideTimer = new Class({

  setOptions: function(options) {
    this.options = Object.extend({
      duration: 1000,
      fn: this.hideElement.bind(this),
      elems: new Array()
    }, options || {});

  },

  initialize: function(el,options) {
    this.setOptions(options);
    this.el = $(el);
    this.timer = null;
    if(this.options.elems.length > 0) {
      this.options.elems.each(function (element) {
        element.addEvent('mouseover',this.clearTimer.bind(this));
        element.addEvent('mouseout',this.initTimer.bind(this));
      }.bind(this));
    }
    this.el.addEvent('mouseover',this.clearTimer.bind(this));
    this.el.addEvent('mouseout',this.initTimer.bind(this));
  },
  
  initTimer: function() {
    if(this.el.getStyle('display') != 'none') {
      if(this.timer != null ) {
        this.clearTimer();
      }
      this.timer = this.exec.bind(this).delay(this.options.duration);
    }
  },

  clearTimer: function() {
    if(this.timer != null) {
      $clear(this.timer);
      this.timer = null;
    }
  },
  
  toggleTimer: function() {
    if(this.timer == null) {
      this.initTimer();
    } else {
      this.clearTimer();
    }
  },

  hideElement: function() {
    this.el.setStyle('display','none');
    this.el.setStyle('visibility','hidden');
    overListBoxFix(this.el,'none');
  },
  
  exec: function() {
    this.options.fn.delay(1);
    this.timer = null;
  }
});

String.prototype.pad = function(l, s, t){
    return s || (s = " "), (l -= this.length) > 0 ? (s = new Array(Math.ceil(l / s.length)
        + 1).join(s)).substr(0, t = !t ? l : t == 1 ? 0 : Math.ceil(l / 2))
        + this + s.substr(0, l - t) : this;
};


Number.prototype.pad = function(l,s,t) { 
  return this.toString().pad(l,s,t);
}

Date.prototype.format = function(pattern) {    
  if (!this.valueOf())
    return ;
  var d = this;

  return pattern.replace(/(Y|m|d|H|i|s)/gi,
    function($1) {
      switch ($1.toLowerCase()) {
      case 'y':   return d.getFullYear();
      case 'm':   return (d.getMonth() + 1).pad(2,'0');
      case 'd':   return d.getDate().pad(2,'0');
      case 'h':   return h = d.getHours().pad(2,'0');
      case 'i':   return d.getMinutes().pad(2,'0');
      case 's':   return d.getSeconds().pad(2,'0');
    }
  });
}
