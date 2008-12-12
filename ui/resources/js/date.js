
Obm.DateTime = new Class({
  
  initialize: function(ms) {
    this.inputDate = new Date(ms);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset; 
    this.outputDate = new Date(time);
  },

  getDate: function() {
    return this.outputDate.getDate();
  },


  getDay: function() {
    return this.outputDate.getDay();
  },


  getFullYear: function() {
    return this.outputDate.getFullYear();
  },


  getHours: function() {
    return this.outputDate.getHours();
  },


  getMilliseconds: function() {
    return this.outputDate.getMilliseconds();
  },


  getMinutes: function() {
    return this.outputDate.getMinutes();
  },


  getMonth: function() {
    return this.outputDate.getMonth();
  },


  getSeconds: function() {
    return this.outputDate.getSeconds();
  },


  getTime: function() {
    return this.inputDate.getTime();
  },


  getTimezoneOffset: function() {
    return this.offset / 60000;
  },


  getUTCDate: function() {
    return this.inputDate.getUTCDate();
  },


  getUTCDay: function() {
    return this.inputDate.getUTCDay();
  },


  getUTCFullYear: function() {
    return this.inputDate.getUTCFullYear();
  },


  getUTCHours: function() {
    return this.inputDate.getUTCHours();
  },


  getUTCMilliseconds: function() {
    return this.inputDate.getUTCMilliseconds();
  },


  getUTCMinutes: function() {
    return this.inputDate.getUTCMinutes();
  },


  getUTCMonth: function() {
    return this.inputDate.getUTCMonth();
  },


  getUTCSeconds: function() {
    return this.inputDate.getUTCSeconds();
  },


  getYear: function() {
    return this.outputDate.getYear();
  },


  setDate: function(Date) {
    this.outputDate.setDate(Date);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },


  setFullYear: function(FullYear) {
    this.outputDate.setFullYear(FullYear);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },


  setHours: function(Hours) {
    this.outputDate.setHours(Hours);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },


  setMilliseconds: function(Milliseconds) {
    this.outputDate.setMilliseconds(Milliseconds);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },


  setMinutes: function(Minutes) {
    this.outputDate.setMinutes(Minutes);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },


  setMonth: function(Month) {
    this.outputDate.setMonth(Month);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },


  setSeconds: function(Seconds) {
    this.outputDate.setSeconds(Seconds);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },


  setTime: function(Time) {
    this.inputDate.setTime(Time);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setUTCDate: function(UTCDate) {
    this.inputDate.setUTCDate(UTCDate);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setUTCFullYear: function(UTCFullYear) {
    this.inputDate.setUTCFullYear(UTCFullYear);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setUTCHours: function(UTCHours) {
    this.inputDate.setUTCHours(UTCHours);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setUTCMilliseconds: function(UTCMilliseconds) {
    this.inputDate.setUTCMilliseconds(UTCMilliseconds);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setUTCMinutes: function(UTCMinutes) {
    this.inputDate.setUTCMinutes(UTCMinutes);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setUTCMonth: function(UTCMonth) {
    this.inputDate.setUTCMonth(UTCMonth);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setUTCSeconds: function(UTCSeconds) {
    this.inputDate.setUTCSeconds(UTCSeconds);
    var time = this.inputDate.getTime() - this.inputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    this.outputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
  },


  setYear: function(Year) {
    this.outputDate.setYear(Year);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },



  toDateString: function() {
    return this.outputDate.toDateString();
  },


  toGMTString: function() {
    return this.outputDate.toGMTString();
  },



  toLocaleDateString: function() {
    return this.outputDate.toLocaleDateString();
  },


  toLocaleFormat: function() {
    return this.outputDate.toLocaleFormat();
  },



  toLocaleString: function() {
    return this.outputDate.toLocaleString();
  },


  toLocaleTimeString: function() {
    return this.outputDate.toLocaleTimeString();
  },


  toSource: function() {
    return this.outputDate.toSource();
  },



  toString: function() {
    return this.outputDate.toString();
  },


  toTimeString: function() {
    return this.inputDate.toTimeString();
  },


  toUTCString: function() {
    return this.inputDate.toUTCString();
  },


  valueOf: function() {
    return this.outputDate.valueOf();
  },


  format: function(pattern) {    
    if (!this.valueOf())
      return ;
    var d = this;

    return pattern.replace(/(Y|m|d|H|i|s|c|O)/gi,
      function($1) {
        switch ($1.toLowerCase()) {
        case 'y':   return d.getFullYear();
        case 'm':   return (d.getMonth() + 1).pad(2,'0');
        case 'd':   return d.getDate().pad(2,'0');
        case 'h':   return h = d.getHours().pad(2,'0');
        case 'i':   return d.getMinutes().pad(2,'0');
        case 's':   return d.getSeconds().pad(2,'0');
        case 'c':   return d.format('y-m-dTh:i:s O');
        case 'o':   return d.getTimezone();
      }
    });
  },

  getTimezone: function() {    
    var offset = this.offset/36000;
    if(offset >= 0) {
      offset = offset.pad(4,'0');
      offset = '+' + offset
    } else {
      offset = offset.pad(4,'0');
    }
    return offset; 
  }
});

Obm.TimeZoneParser = new Class ({
  initialize: function (timeZone) {
    var request = new Request({
        url:obm.vars.consts.resourcePath  + '/js/bin/timezone/' + timeZone,
        async: false
    });
    request.xhr.overrideMimeType('text/plain; charset=x-user-defined');
    request.send();
    var fileContent = request.response.text;
    var fileSize = fileContent.length;
    var bytes = [];
    for(var i=0; i < fileSize; i++) {
      bytes[i] = fileContent.charCodeAt(i) & 0xff;
    }
    window.parseTimeZoneData(bytes);
  },
  
  getTimeZoneOffset: function(time) {
    return window.getTimeZoneOffset('' + time + '');
  }
});

obm.initialize.chain(function () {
  obm.timeZoneParser = new Obm.TimeZoneParser(obm.vars.consts.timeZone);
});
