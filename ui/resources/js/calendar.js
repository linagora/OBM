Obm.CalendarEvent = new Class({
  
  initialize: function(el,id,time,duration,title) {
    this.element = $(el);
    this.element.insert
    this.id = id;
    this.title = title;
    this.time = time;
    this.duration = duration;
    var dragOptions = {
      handle: $E('h1',this.element),
      xMin: obm.calendarManager.context.left,
      xMax: obm.calendarManager.context.right - obm.calendarManager.defaultWidth,
      yMin: obm.calendarManager.context.top,
      yMax: obm.calendarManager.context.bottom - this.element.offsetHeight,

      onStart:function() {
        this.element.setStyle('width', obm.calendarManager.defaultWidth + 'px');
        this.element.setStyle('margin-left', '0');
        this.element.setOpacity(.7);
      },

      onComplete:function() {
        this.element.setOpacity(1);
        obm.calendarManager.moveEventTo(this.element.id,this.element.getLeft(),this.element.getTop());
      }     

    };   

    var resizeOptions = {
      handle: $E('img',this.element),
      xMin: obm.calendarManager.defaultWidth,
      xMax: obm.calendarManager.defaultWidth,
      yMin: obm.calendarManager.defaultHeight,
      yMax: obm.calendarManager.context.bottom - this.element.getTop(),

      onStart:function() {
        this.element.setStyle('width', obm.calendarManager.defaultWidth + 'px');
        this.element.setStyle('margin-left', '0');
        this.element.setOpacity(.7);
      },

      onComplete:function() {
        this.element.setOpacity(1);
        obm.calendarManager.resizeEventTo(this.element.id,this.element.offsetHeight);
      }     

    };       
    this.drag = this.element.makeDraggable(dragOptions);
    this.resize = this.element.makeResizable(resizeOptions);
  },

  setTime: function(time) {
    this.time = time;
  },

  setDuration: function(duration) {
    this.duration = duration;
  },

  setSize: function(size) {
    this.size = size;
    height = size * obm.calendarManager.defaultHeight;
    this.element.setStyle('height',height + 'px');
    this.resize.options.yMax = obm.calendarManager.context.bottom - this.element.getTop();
    this.drag.options.yMax = obm.calendarManager.context.bottom - this.element.offsetHeight;
  },

  setOrigin: function(origin) {
    this.origin = origin;
    hr = $('time-'+origin);
    this.element.setStyles({
      'top':  hr.getTop() + 'px',
      'left': hr.getLeft() + 'px'
    });
    this.resize.options.yMax = obm.calendarManager.context.bottom - this.element.getTop();
  },

  setWidth: function(width) {
    this.element.setStyle('width',width + 'px');
  },

  setMargin: function(margin) {
    this.element.setStyle('margin-left',margin + 'px');
  },

  show: function() {
    this.element.setStyle('display','block');
  },

  hide: function() {
    this.element.setStyle('display','none');
  }
  
})

Obm.CalendarManager = new Class({

  initialize: function(startTime) {
    this.startTime = startTime;
    this.defaultWidth = $('time-'+this.startTime).clientWidth;
    this.defaultHeight = $('time-'+this.startTime).offsetHeight;
    this.events = new Hash();
    this.times = new Hash();
    this.context = new Object();
    ctx = $S('.calendar tbody')[0];
    this.context.top = ctx.getTop();
    this.context.right = ctx.getLeft() + ctx.offsetWidth;
    this.context.left = $E('td', ctx).getLeft();
    this.context.bottom = ctx.getTop() + ctx.offsetHeight;
  },

    
  compareEvent: function(event1, event2) {
      return event1.origin - event2.origin ;
  },

  newEvent: function(id,time,duration,title) {
    delta = (time - this.startTime)%obm.vars.consts.timeUnit;
    delta = time - delta;
    size = Math.floor(duration/obm.vars.consts.timeUnit);
    elId = 'event-'+id+'-'+time;
    obmEvent = new Obm.CalendarEvent(elId,id,time,duration,title);
    obmEvent.setOrigin(delta);
    obmEvent.setSize(size);
    obmEvent.show();
    this.events.put(elId,obmEvent);
    this.register(elId);
  },

  moveEventTo: function(id,left,top) {
    xDelta = Math.round((left-this.context.left)/this.defaultWidth);
    yDelta = Math.round((top-this.context.top)/this.defaultHeight);
    time = this.startTime + xDelta*3600*24 + yDelta*obm.vars.consts.timeUnit;
    event = this.events.get(id);
    oldTime = event.time
    this.unregister(id);
    event.setTime(time);
    event.setOrigin(time);
    this.register(id);    
    this.redrawDayEvents(event.time);

  },

  resizeEventTo: function(id,size) {
    size = Math.round(size/this.defaultHeight);
    event = this.events.get(id);
    event.setSize(size);
    this.unregister(id);
    event.setDuration(size*obm.vars.consts.timeUnit);
    this.register(id);   
    this.redrawDayEvents(event.time);
  },

  register: function(id) {
    event = this.events.get(id);
    delta = (event.time - this.startTime)%obm.vars.consts.timeUnit;
    delta = event.time - delta;
    size = Math.floor(event.duration/obm.vars.consts.timeUnit);
    for(i=0;i < size;i++) {
      t = delta + i*obm.vars.consts.timeUnit;
      if(!this.times.get(t)) {
        this.times.put(t,new Array());
      }
      this.times.get(t).push(event);
    }    
  },

  unregister: function(id) {
    delta = (event.time - this.startTime)%obm.vars.consts.timeUnit;
    delta = event.time - delta;
    size = Math.floor(event.duration/obm.vars.consts.timeUnit);
    for(i=0;i < size;i++) {
      t = delta + i*obm.vars.consts.timeUnit;
      this.times.get(t).remove(event);
    }
  },

  redrawAllEvents: function() {
    keys = this.times.keys().sort();
    this.redrawEvents(keys);
  }, 

  redrawDayEvents: function(dayStart) {
    //TODO Ca doit pouvoir s'ameliorer

    date = new Date();
    date.setTime(dayStart * 1000);
    date.setHours(8);
    date.setMinutes(0);
    date.setSeconds(0);
    ts = date.getTime()/1000;
    date.setHours(20)
    te = date.getTime()/1000;

    keys = this.times.keys().sort();
    while(keys[0] < ts) {
      keys.shift();
    }
    k = 0;
    while(keys[(keys.length -1)] > te) {
      keys.pop();
    }
    this.redrawEvents(keys);
  },

  redrawEvents: function(key) {
    resize = new Object();
    end = 0;
    
    for(k=0;k < keys.length; k++) {
      time = this.times.get(keys[k]);
      if(time.length == 0)
        continue;

      if(end == 0) {
        unit = new Object()
        unit.size = 1;
      }
      if(unit.size < time.length) {
        unit.size = time.length;
      }
      time.sort(this.compareEvent);
      //TODO Revoir la gestion de la premiere 
      //place disponible
      free = new Array;
      for(i=0;i<time.length;i++) {
        free.push(i);
      }
      
      // PERFORMING Event position
      for(i=0;i<time.length;i++) {
        event = time[i];
        id = event.element.id;        
        if(keys[k] == event.origin) {
          size = Math.floor(event.duration/obm.vars.consts.timeUnit);
          resize[id] = new Object;
          resize[id].position = free.splice(0, 1)[0];
          resize[id].unit = unit;       
          if(size > end) {
            end = size;
          }
        } else  {
          free.remove(resize[id].position);
        }
      }
      end --;
    }
    // REDRAWING EVENTS
    for(i in resize) {
      event = this.events.get(i);
      if(resize[i].unit.size > 1)
        event.setWidth(this.defaultWidth/resize[i].unit.size);
      else
        event.setWidth(this.defaultWidth);
      event.setMargin((this.defaultWidth/resize[i].unit.size)*resize[i].position);
    }
  }

});

