
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
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setUTCDate: function(UTCDate) {
    this.inputDate.setUTCDate(UTCDate);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setUTCFullYear: function(UTCFullYear) {
    this.inputDate.setUTCFullYear(UTCFullYear);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setUTCHours: function(UTCHours) {
    this.inputDate.setUTCHours(UTCHours);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setUTCMilliseconds: function(UTCMilliseconds) {
    this.inputDate.setUTCMilliseconds(UTCMilliseconds);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setUTCMinutes: function(UTCMinutes) {
    this.inputDate.setUTCMinutes(UTCMinutes);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setUTCMonth: function(UTCMonth) {
    this.inputDate.setUTCMonth(UTCMonth);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setUTCSeconds: function(UTCSeconds) {
    this.inputDate.setUTCSeconds(UTCSeconds);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
    var time = this.inputDate.getTime() + this.inputDate.getTimezoneOffset() * 60 * 1000 + this.offset;     
    this.outputDate.setTime(time);
  },


  setYear: function(Year) {
    this.outputDate.setYear(Year);
    var time = this.outputDate.getTime() - this.outputDate.getTimezoneOffset() * 60 * 1000 - obm.timeZoneParser.getTimeZoneOffset(this.outputDate.getTime());
    this.inputDate.setTime(time);
    this.offset = obm.timeZoneParser.getTimeZoneOffset(this.inputDate.getTime());
  },

  addDays: function(nb) {
    var hours = this.getHours();
    var minutes = this.getMinutes();
    var seconds = this.getSeconds();
//     var tmpDate = new Obm.DateTime(date.getTime());
    this.setNoon();
    for (var i=0;i<nb;i++) {
      this.setTime( (this.getTime() + (24*60*60*1000)) );
      this.setNoon();
    }
    this.setHours(hours);
    this.setMinutes(minutes);
    this.setSeconds(seconds);
    return this;
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

    return pattern.replace(/(Y|m|d|H|i|s|c|O|g|a)/gi,
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
        case 'g':   return d.getHours() % 12 || 12;
        case 'a':   return d.getHours() < 12 ? 'am' : 'pm';
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
  },
  
  setNoon: function() {
    this.setHours(12);
    this.setMinutes(0);
    this.setSeconds(0);
  }
});

Obm.TimeZoneParser = new Class ({
  initialize: function (timeZone) {
    this.lastIndex = 0;
    var request = new Request.JSON({
        url:obm.vars.consts.resourcePath  + '/js/bin/timezone/' + timeZone,
        method: 'get',
        async: false,
        method: 'get',
        secure: false,
        onSuccess: function (response) {
          this.offsets = response;
        }.bind(this)
    });
    request.send();
  },
  
  getTimeZoneOffset: function(time) {
    var ok = false;
    var i = this.lastIndex;
    if (isNaN(time)) {
		throw "Invalid date time";
	}
    time = time/1000;
    while(!ok) {
      if(! this.offsets[i]['from'] || this.offsets[i]['from'] <= time) {
        if(!this.offsets[i]['to'] || this.offsets[i]['to'] > time) {
          ok = true
        } else {
          i++;
        }
      } else {
        i--;
      }
    }
    this.lastIndex = i;
    return this.offsets[i]['offset']*1000;
  }
});

obm.initialize.chain(function () {
  obm.timeZoneParser = new Obm.TimeZoneParser(obm.vars.consts.timeZone);
});
