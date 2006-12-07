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
      frequency: '1000',
      onStart: Class.empty,
      onChange: Class.empty,
      onStop: Class.empty,
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
    console.log(this.options.property + " : " +this.lastValue);
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
