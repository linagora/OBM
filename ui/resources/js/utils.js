var Hash = new Class({
    length: 0,
    initialize: function(obj) {
        this.obj = {};
        for (var property in obj) {
            this.obj[property] = obj[property];
            this.length++;
        }
    },
    get: function(key) {
        return this.obj[key];
    },
    put: function(key, value) {
        if (value == null) throw 'Cannot put null values in the map';
        if (this.obj[key] == undefined) this.length++;
        this.obj[key] = value;
        return this;
    },
    remove: function(key) {
        if (this.obj[key] == undefined) return;
        var obj = {};
        this.length--;
        for (var property in this.obj)
            if (property != key) obj[property] = this.obj[property];
        this.obj = obj;
        return this;
    },
    each: function(fn, bind) {
        for (var property in this.obj)
            fn.call(bind, property, this.obj[property]);
    },
    empty: function() {
        return (this.length == 0);
    },
    keys: function() {
        var keys = [];
        for (var property in this.obj)
            keys.push(property);
        return keys;
    },
    values: function() {
        var values = [];
        for (var property in this.obj)
            values.push(this.obj[property]);
        return values;
    }
});

function $H(obj) {
    return new Hash(obj);
}
    

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
      fn: this.hideElement
    }, options || {});

  },

  initialize: function(el,options) {
    this.setOptions(options);
    this.el = $(el);
    this.timer = null;
    this.el.addEvent('mouseover',this.clearTimer.bind(this));
    this.el.addEvent('mouseout',this.initTimer.bind(this));
  },
  
  initTimer: function() {
    if(this.el.getStyle('display') != 'none') {
      if(this.timer != null ) {
        this.clearTimer();
      }
      this.timer = this.options.fn.delay(this.options.duration);
    }
  },

  clearTimer: function() {
    if(this.timer != null) {
      this.timer = $clear(this.timer);
    }
  },
  
  toggleTimer: function() {
    if(this.timer == null) {
      
    }
  },

  hideElement: function() {
    this.el.setStyle('display','none');
    this.el.setStyle('visibility','hidden');
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
