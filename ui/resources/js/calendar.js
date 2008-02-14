

Obm.CalendarDayEventExtension = new Class({
  initialize: function(parentEvent,size,origin) {
    this.event = parentEvent.event;
    this.options = parentEvent.options;
    this.parentEvent = parentEvent;
    this.buildExtension();
    this.size = size;
    this.setOrigin(origin);
    this.setSize(size);
    if(this.options.draggable) {
      this.dragHandler.addEvent('mousedown',this.parentEvent.drag.bound.start)
    }
  },

  setOrigin: function(origin) {
    var hr = $(this.options.type+'-'+origin);
    this.origin = origin;
    this.element.remove();
    hr.adopt(this.element);
    this.redraw();
    return true;
  },

  setSize: function() {
    this.setWidth(this.size * (obm.calendarManager.defaultWidth +1) - 1);
  },


  buildExtension: function() {
    id = this.parentEvent.element.id+'-extension-'+origin;
    this.element = new Element('div').addClass('event')
                                     .addClass(this.event.klass)
                                     .setProperty('id',id)
                                     .setProperty('title', this.event.title)
                                     .injectInside(document.body);
    this.dragHandler = new Element('h1')
                                 .injectInside(this.element);     
    if(this.event.meeting) {                                 
      this.meeting = new Element('img').setProperty('src',obm.vars.images.meeting)
                        .injectInside(this.dragHandler);
    }
    this.setPeriodicity();
    this.titleContainer = new Element('a').setProperty('href','calendar_index.php?action=detailconsult&calendar_id='+this.event.id)
                                          .injectInside(this.dragHandler);
    this.titleContainer.onclick = function () { if(obm.calendarManager.redrawLock) return false;};
    this.resetTitle();
  },

  resetTitle: function() {
    var title = this.event.title;
    if(this.event.all_day == 0) {
      title = new Date(this.event.time * 1000).format("H:i") + ' ' + title; 
    }
    this.element.setProperty('title', this.event.title)
    this.titleContainer.setHTML(title);
  },

  setPeriodicity: function() {
    if(this.event.periodic) {                                 
      this.periodic = new Element('img').setProperty('src',obm.vars.images.periodic)
                        .injectInside(this.dragHandler);
    }else if(this.periodic) {
      this.periodic.remove();
    }
  },

  setWidth: function(width) {
    if( (this.element.offsetLeft + width) > this.options.context.right ) {
      width = this.options.context.right - this.element.offsetLeft;
    }
    this.element.setStyle('width',width + 'px');
  },

  redraw: function() {
    hr = $(this.element.parentNode);
    this.element.setStyles({
      'top':  hr.getTop() + hr.getStyle('padding-top').toInt() + 'px',
      'left': hr.getLeft() + 'px'
    });
    this.setWidth(this.size * (obm.calendarManager.defaultWidth +1) - 1);
  },

  destroy: function() {
    this.element.remove();
  },        

  conflict: function(size, position) {
    this.element.setStyle('margin-top',position * this.element.offsetHeight + 'px');
  },

  setColor: function(color) {
    if(color) {
      this.element.setStyle('backgroundColor',color);
      this.dragHandler.setStyle('backgroundColor',color);
    } else {
      this.element.setStyle('backgroundColor','');
      this.dragHandler.setStyle('backgroundColor','');
    }
  }
   

})
/******************************************************************************
 * Calendar event object, can be dragged but have no graphical representation
 * of it's duration.
 ******************************************************************************/
Obm.CalendarDayEvent = new Class({
  
  setOptions: function(options){
    this.options = Object.extend({
      draggable: false,
      type: 'day',
      yUnit: 0,
      xUnit: 24*3600,
      unit : 24*3600,
      context: obm.calendarManager.headContext
    }, options || {});
  },

  
  initialize: function(eventData,options) {
    this.setOptions(options);
    this.event = eventData;
    this.size = 1;
    this.length = 1;
    this.hidden = 0;
    this.extensions = new Array();
    this.buildEvent();
    if(this.options.draggable)
      this.makeDraggable();    
    this.setTime(this.event.time);
    this.setDuration(this.event.duration);
    this.switchColor(obm.vars.conf.calendarColor);
  },

  makeDraggable: function() {
    var dragOptions = {
      handle: this.dragHandler,
      limit: {
        'x': [this.options.context.left,this.options.context.right - obm.calendarManager.defaultWidth],
        'y': [this.options.context.top,this.options.context.bottom]
      },

      onSnap:function() {
        obm.calendarManager.lock();
        this.drag.mouse.pos = {x: obm.calendarManager.defaultWidth/2, y: 10};
        this.element.setStyles({
          'width' : obm.calendarManager.defaultWidth + 'px',
          'margin-left' : 0
        });
        this.element.setOpacity(.7);
        this.trashExtensions();
      }.bind(this),

      onComplete:function() {
        if(obm.calendarManager.redrawLock) {
          this.element.setOpacity(1);
          obm.calendarManager.unlock();
          obm.calendarManager.moveEventTo(this.element.id,this.element.getLeft(),this.element.getTop());
        }
      }.bind(this)     
    };   

    this.drag = this.element.makeDraggable(dragOptions);
  },

  buildEvent: function() {
    id = this.event.id+'-'+this.event.entity+'-'+this.event.entity_id+'-'+this.event.time;
    this.element = new Element('div').addClass('event')
                                     .addClass(this.event.klass)
                                     .setProperty('id','event-'+id)
                                     .setProperty('title', this.event.title)
                                     .injectInside(document.body);
    this.dragHandler = new Element('h1')
                                 .injectInside(this.element);     
    if(this.event.meeting) {                                 
      this.meeting = new Element('img').setProperty('src',obm.vars.images.meeting)
                        .injectInside(this.dragHandler);
    }
    this.setPeriodicity();
    this.titleContainer = new Element('a').setProperty('href','calendar_index.php?action=detailconsult&calendar_id='+this.event.id)
                                          .injectInside(this.dragHandler);
    this.titleContainer.onclick = function () { if(obm.calendarManager.redrawLock) return false;};
    this.resetTitle();

  },
 
  setPeriodicity: function() {
    if(this.event.periodic) {                                 
      this.periodic = new Element('img').setProperty('src',obm.vars.images.periodic)
                        .injectInside(this.dragHandler);
    }else if(this.periodic) {
      this.periodic.remove();
    }
  },

  resetTitle: function() {
    var title = this.event.title;
    if(this.event.all_day == 0) {
      title = new Date(this.event.time * 1000).format("H:i") + ' ' + title; 
    }
    this.element.setProperty('title', this.event.title)
    this.titleContainer.setHTML(title);    
  },

  setTitle: function(title) {
    this.event.title = title;
    this.resetTitle();
    this.extensions.each(function (extension) {
      extension.resetTitle();
    })
  },

  setTime: function(time) {
    var myDate = new Date(time * 1000);
    var startDate = new Date(obm.calendarManager.startTime * 1000);
    myDate.setHours(startDate.getHours());
    myDate.setMinutes(startDate.getMinutes());
    myDate.setSeconds(startDate.getSeconds());
    origin = Math.floor((myDate.getTime() - startDate.getTime())/1000);
    if(this.setOrigin(origin)) {
      this.event.time = this.guessEventTime(time);
    } else {
      if(obm.calendarManager.lock()) {
        this.redraw();
        obm.calendarManager.unlock();
      }
    }
  },
  
  guessEventTime: function(time) {
    if(this.event.time) {
      var time = new Date(time * 1000);
      d = new Date(this.event.time * 1000);
      time.setHours(d.getHours());
      time.setMinutes(d.getMinutes());
      time = Math.floor(time.getTime()/1000);
    } 
    return time;
  },


  setOrigin: function(origin) {
    if(origin < 0) {   
      this.hidden = -(origin / this.options.xUnit);
      this.hidden = Math.ceil(this.hidden);
      origin = 0;
    } else if(this.hidden != 0) {
        this.hidden = 0;
        this.setSize(this.size);
    }
    var hr = $(this.options.type+'-'+origin);
    this.origin = origin;
    this.element.remove();
    hr.adopt(this.element);
    if(obm.calendarManager.lock()) {
      this.redraw();
      obm.calendarManager.unlock();
      if(this.resizeLine()) {
        obm.calendarManager.resizeWindow();
      }      
    }
    return true;
  },

  resizeLine: function(lineElem) {
      if(!$(lineElem)) {
        var hr = $(this.options.type + '-' + this.origin);
        var lineElem = hr.parentNode;
      }
      var thead = $(lineElem).getFirst();
      var size = 0;
      do {
        if(thead.childNodes.length > size) size = thead.childNodes.length;
      } while(thead = thead.getNext());
      size = size * this.element.offsetHeight + 5;
      if(hr.parentNode.offsetHeight != size) {
        hr.parentNode.setStyle('height', size + 'px');    
        return true
      }        
      return false;
  },

  setDuration: function(duration) {
    this.event.duration = duration;
    startTime = new Date(this.event.time * 1000);
    startTime.setHours(0);
    startTime = startTime.getTime()/1000;
    endTime = new Date((this.event.time + this.event.duration)*1000);
    endTime = endTime.getTime()/1000;
    var dayDuration = Math.ceil((endTime - startTime) / 86400);
    this.setSize(dayDuration);
  },

  setSize: function(size) {
    this.trashExtensions();
    this.size = size - this.hidden;
    this.length = this.size;
    this.drawExtensions();
    if(obm.calendarManager.lock()) {
      this.setWidth(this.size * (obm.calendarManager.defaultWidth +1) - 1);
      if(this.drag) {
        this.drag.options.xMax = this.options.context.right - obm.calendarManager.defaultWidth;
      }
      obm.calendarManager.unlock();
    }
  },

  redraw: function() {
    hr = $(this.element.parentNode);
    this.element.setStyles({
      'top':  hr.getTop() + hr.getStyle('padding-top').toInt() + 'px',
      'left': hr.getLeft() + 'px'
    });
    this.setWidth(this.size * (obm.calendarManager.defaultWidth +1) - 1);
    if(this.options.draggable) {
      this.drag.options.limit = {
        'x': [this.options.context.left,this.options.context.right - obm.calendarManager.defaultWidth],
        'y': [this.options.context.top,this.options.context.bottom - this.element.offsetHeight]};      
    }   
    this.extensions.each( function (extension) {
      extension.redraw();
    });
  },

  setWidth: function(width) {
    if( (this.element.offsetLeft + width) > this.options.context.right ) {
      width = this.options.context.right - this.element.offsetLeft;
    }
    this.element.setStyle('width',width + 'px');
  },

  conflict: function(size, position) {
    this.element.setStyle('margin-top',position * this.element.offsetHeight + 'px');
    this.extensions.each( function (extension) {
      extension.conflict(size, position);
    }.bind(this));
  },

  toQueryString: function() {
    date_begin = new Date(this.event.time * 1000);
    date_end = new Date(this.event.time * 1000 + this.event.duration * 1000);    
    query = 'calendar_id=' + this.event.id;
    query += '&date_begin=' + date_begin.format('Y-m-d H:i:s');
    query += '&duration=' + this.event.duration;
    query += '&title=' + this.event.title;
    return query;
  },
  
  drawExtensions: function() {
    if(this.size != 1) {
      dayBegin = (new Date(this.event.time * 1000).getDay() + this.hidden - obm.vars.consts.weekStart + 7) % 7; 
      dayEnd = (dayBegin + this.size + 6) % 7;
      var startDate = new Date((obm.calendarManager.startTime + this.origin) * 1000);
      while(dayEnd < dayBegin || this.size > 7) {
        var extensionSize = dayEnd + 1;
        this.size -= extensionSize;
        var extensionDate = new Date(startDate.getTime());
        extensionDate.setDate(startDate.getDate() + this.size);
        var extensionOrigin = Math.floor(extensionDate.getTime()/1000) - obm.calendarManager.startTime;
        dayEnd = 6;
        if($(this.options.type+'-'+extensionOrigin)) {
          this.extensions.push(new Obm.CalendarDayEventExtension(this,extensionSize, extensionOrigin));
        } else {
          this.length -= extensionSize;
        }
      }
    }
  },
                 
  trashExtensions: function() {
    this.extensions.each( function (extension) {
      extension.destroy();
      delete extension;
    }.bind(this))
    this.extensions = new Array();
  },

  destroy: function() {
    this.element.remove();
    this.trashExtensions();
  },

  switchColor: function(colorTemplate) {
    switch(colorTemplate) {
      case "event":
        this.setColor(this.event.colors.event);
        break;
      case "priority" :
        this.setColor(this.event.colors.priority);
        break;
      case "category" :
        this.setColor(this.event.colors.category);
        break;
      case "user" :
        this.setColor();
        break;        
      default :
        this.setColor(this.event.colors.event);
    }
  },

  setColor: function(color) {
    if(color) {
      this.element.setStyle('backgroundColor',color);
      this.dragHandler.setStyle('backgroundColor',color);
    } else {
      this.element.setStyle('backgroundColor','');
      this.dragHandler.setStyle('backgroundColor','');
    }
    this.extensions.each(function (extension) {
      extension.setColor(color);
    })
  }
})

/******************************************************************************
 * Extended event with graphical representation of it's duration and which
 * could be resize
 *****************************************************************************/

Obm.CalendarEvent = Obm.CalendarDayEvent.extend({

  setOptions: function(options){
    this.options = Object.extend({
      resizable: false,
      type: 'time',
      yUnit: obm.vars.consts.timeUnit,
      xUnit: 3600*24,
      unit : obm.vars.consts.timeUnit,
      context: obm.calendarManager.bodyContext
    }, options || {});
  },

  initialize: function(eventData,options) {
    this.setOptions(options);
    this.event = eventData;
    this.extensions = new Array();
    this.size = 1;
    this.length = 1;
    this.buildEvent();
    this.switchColor(obm.vars.conf.calendarColor);
    if(this.options.resizable) 
      this.makeResizable(options);
    if(this.options.draggable)
      this.makeDraggable();      
    this.setTime(this.event.time);
    this.setDuration(this.event.duration);          
  },
  
  buildEvent: function() {
    id = this.event.id+'-'+this.event.entity+'-'+this.event.entity_id+'-'+this.event.time;
    this.element = new Element('div').addClass('event')
                                     .addClass(this.event.klass)
                                     .setProperty('id','event-'+id)
                                     .setProperty('title', this.event.title)
                                     .injectInside(document.body);
    this.dragHandler = new Element('h1')
                                 .injectInside(this.element);     
    if(this.event.meeting) {                                 
      new Element('img').setProperty('src',obm.vars.images.meeting)
                        .injectInside(this.dragHandler);
    }
    this.setPeriodicity();
    if(this.options.resizable) {
      this.resizeHandler = new Element('img').setProperty('src',obm.vars.images.resize)
                                             .addClass('handle')
                                             .injectInside(this.element);
    }
    this.titleContainer = new Element('span').injectInside(this.element);
    this.timeContainer = new Element('a').setProperty('href','calendar_index.php?action=detailconsult&calendar_id='+this.event.id)
                                         .injectInside(this.dragHandler);
    this.timeContainer.onclick = function () {if(obm.calendarManager.redrawLock) return false;};
    this.resetTitle();

  },

  resetTitle: function() {
    var title = this.event.title;
    var time = new Date(this.event.time * 1000).format("H:i");
    if(this.event.duration <= this.options.unit) {
      time = new Date(this.event.time * 1000).format("H:i") + ' ' + title; 
      title = '';
    }
    this.element.setProperty('title', this.event.title)
    this.timeContainer.setHTML(time);
    this.titleContainer.setHTML(title);
  },

  setDuration: function(duration) {
    this.event.duration = duration;
    size = Math.ceil(duration/this.options.yUnit);
    this.setSize(size);
  },

  setSize: function(size) {
    this.size = size;
    this.length = size;
    if(obm.calendarManager.lock()) {  
      height = size * obm.calendarManager.defaultHeight;
      this.setHeight(height);
      if(this.resize) {
        this.resize.options.yMax = this.options.context.bottom - this.element.getTop();
      }
      if(this.drag) {
        this.drag.options.yMax = this.options.context.bottom - this.element.offsetHeight;
      }
      obm.calendarManager.unlock();
    }
  },

  setHeight: function(height) {
    if((this.element.getTop() + height) > this.options.context.bottom) {
      height = this.options.context.bottom - this.element.getTop();
    }
    this.element.setStyle('height',height + 'px');
  },

  makeResizable: function() {
    var resizeOptions = {
      handle: this.resizeHandler,
      limit: {
        'x': [obm.calendarManager.defaultWidth,obm.calendarManager.defaultWidth],
        'y': [obm.calendarManager.defaultHeight,this.options.context.bottom]
      },

      onStart:function() {
        obm.calendarManager.lock();
        this.element.setStyle('margin-left', '0');
        this.element.setOpacity(.7);
      },
      
      onComplete:function() {
        this.element.setOpacity(1);
        obm.calendarManager.resizeEventTo(this.element.id,this.element.offsetHeight);
        obm.calendarManager.unlock();
      }     
    };       

    this.resize = this.element.makeResizable(resizeOptions);
  },

  setMargin: function(margin) {
    this.element.setStyle('margin-left',margin + 'px');
  },
 

  setTime: function(time) {
    origin = time - (time - obm.calendarManager.startTime)%this.options.yUnit;
    if(this.setOrigin(origin)) {
      this.event.time = this.guessEventTime(time);
      this.resetTitle(); 
    } else {
      if(obm.calendarManager.lock()) {
        this.redraw();
        obm.calendarManager.unlock();
      }
    }
  },

  guessEventTime: function(time) {
    return time;
  },

  setOrigin: function(origin) {
    hr = $(this.options.type+'-'+origin);
    if(hr) {
      this.origin = origin;
      this.element.remove();
      hr.adopt(this.element);
      if(obm.calendarManager.lock()) {
        this.redraw();
        obm.calendarManager.unlock();
      }
      return true;
    } else {
      return false;
    }
  },

  resizeLine: function(lineElem) {
    return false;
  },

  redraw: function() {
    hr = $(this.element.parentNode);
    this.element.setStyles({
      'top':  hr.getTop() + 'px',
      'left': hr.getLeft()  + 'px'      
    });
    this.setHeight(this.size * obm.calendarManager.defaultHeight);
    if(this.options.draggable) {
      this.drag.options.limit = {
        'x': [this.options.context.left,this.options.context.right - obm.calendarManager.defaultWidth],
        'y': [this.options.context.top,this.options.context.bottom - this.element.offsetHeight]};         
    }    
    if(this.options.resizable) {
      this.resize.options.limit = {
        'x': [obm.calendarManager.defaultWidth,obm.calendarManager.defaultWidth],
        'y': [obm.calendarManager.defaultHeight,this.options.context.bottom - this.element.getTop()]};      
    }
  },

  conflict: function(size, position) {
    if(size > 1)
      this.setWidth(obm.calendarManager.defaultWidth/size);
    else
      this.setWidth(obm.calendarManager.defaultWidth);
    this.setMargin((obm.calendarManager.defaultWidth/size)*position);
  },

  setColor: function(color) {
    if(color) {
      this.element.setStyle('backgroundColor',color);
      this.dragHandler.setStyle('backgroundColor',color);
    } else {
      this.element.setStyle('backgroundColor','');
      this.dragHandler.setStyle('backgroundColor','');
    }
  }


});

/******************************************************************************
 * Calendar Manager which redraw all events, and is a kind of home for the
 * events objects.
 ******************************************************************************/

Obm.CalendarManager = new Class({

  initialize: function(startTime) {
    this.startTime = startTime;
    this.events = new Hash();
    this.times = new Hash();
    this.redrawLock = false;

    head = $('calendarHead');
    body = $('calendarBody');
    zoneWidth = $E('table.calendar').offsetWidth - $('calendarHourCol').offsetWidth ;//$('calendarEventContext').offsetWidth;
    
    this.evidence = $E('td',body);

    this.headContext = new Object();
    if(head) {
      this.headContext.top = head.getTop();
      this.headContext.right = this.evidence.getLeft() + zoneWidth;
      this.headContext.left = this.evidence.getLeft();
      this.headContext.bottom = head.getTop() + head.offsetHeight;    
    }

    this.bodyContext = new Object();
    this.bodyContext.top = body.getTop();
    this.bodyContext.right = this.evidence.getLeft() + zoneWidth;
    this.bodyContext.left = this.evidence.getLeft();
    this.bodyContext.bottom = body.getTop() + body.offsetHeight;
  
    this.evidence.observe({onStop:this.resizeWindow.bind(this), property:'offsetWidth'});
    this.evidence.observe({onStop:this.resizeWindow.bind(this), property:'offsetTop'});

    this.defaultWidth = this.evidence.clientWidth;
    this.defaultHeight = this.evidence.offsetHeight; 
  },
  
  lock: function() {
    if(!this.redrawLock)
      return (this.redrawLock = true);
    else 
      return false;
  },

  unlock: function() {
    this.redrawLock = false;
  },

  newDayEvent: function(eventData,options) {
    obmEvent = new Obm.CalendarDayEvent(eventData,options);
    this.events.set(obmEvent.element.id,obmEvent);
    this.register(obmEvent.element.id);
    return obmEvent;
  },

  newEvent: function(eventData,options) {
    obmEvent = new Obm.CalendarEvent(eventData,options);
    this.events.set(obmEvent.element.id,obmEvent);
    this.register(obmEvent.element.id);
    return obmEvent;
  },

  resizeWindow: function() {
    if(this.lock()) {

      head = $('calendarHead');
      body = $('calendarBody');
      zoneWidth = $E('table.calendar').offsetWidth - $('calendarHourCol').offsetWidth ;//$('calendarEventContext').offsetWidth;

      if(head) {
        this.headContext.top = head.getTop();
        this.headContext.right = this.evidence.getLeft() + zoneWidth;
        this.headContext.left = this.evidence.getLeft();
        this.headContext.bottom = head.getTop() + head.offsetHeight;    
      }

      this.bodyContext.top = body.getTop();
      this.bodyContext.right = this.evidence.getLeft() + zoneWidth;
      this.bodyContext.left = this.evidence.getLeft();
      this.bodyContext.bottom = body.getTop() + body.offsetHeight;      
  
      
      this.defaultWidth = this.evidence.clientWidth;
      this.defaultHeight = this.evidence.offsetHeight;    

      this.events.each(function(evt, key) {
        evt.resizeLine(); 
      });

      this.events.each(function(evt, key) {
        evt.redraw(); 
      });
      
      this.redrawAllEvents();
      this.unlock();
   }
  },


  compareEvent: function(event1, event2) {
    diff = event1.event.time - event2.event.time;
    if(diff != 0)
      return diff;
    diff = event1.event.id - event2.event.id;
    if(diff != 0)
      return diff;
    return event1.event.entity_id - event2.event.entity_id;
  },

  compareTime: function(time1, time2) {
    return time1.toInt() - time2.toInt();
  },

  moveEventTo: function(id,left,top) {
    var evt = this.events.get(id);
    var xDelta = Math.round((left-evt.options.context.left)/this.defaultWidth);
    var yDelta = Math.floor((top-evt.options.context.top)/this.defaultHeight);
    var secDelta = xDelta*evt.options.xUnit + yDelta*evt.options.yUnit;
    var dayDelta = Math.floor(secDelta / 86400);
    secDelta = secDelta - (dayDelta * 86400);
    var hourDelta = Math.floor(secDelta / 3600);
    secDelta = secDelta - (hourDelta * 3600);
    var minDelta = Math.floor(secDelta / 60);
    secDelta = secDelta - (minDelta * 60);
    var startDate = new Date(this.startTime * 1000);
    startDate.setDate(startDate.getDate() + dayDelta);
    startDate.setHours(startDate.getHours() + hourDelta);
    startDate.setMinutes(startDate.getMinutes() + minDelta);
    startDate.setSeconds(startDate.getSeconds() + secDelta);
    time = Math.floor(startDate.getTime() / 1000);
    guessedTime = evt.guessEventTime(time);
    if(evt.event.time != guessedTime) {
      eventData = new Object();
      eventData.calendar_id = evt.event.id;
      eventData.element_id = id;
      eventData.date_begin = new Date(guessedTime * 1000).format('Y-m-d H:i:s');
      eventData.old_date_begin = new Date(evt.event.time * 1000).format('Y-m-d H:i:s');
      eventData.duration = evt.event.duration;
      eventData.title = evt.event.title;
      eventData.all_day = evt.event.all_day;
      this.sendUpdateEvent(eventData);
    } else {
      evt.setSize(evt.length);
      evt.redraw();
      this.redrawAllEvents(evt.event.time);
    }
  },

  resizeEventTo: function(id,size) {
    size = Math.round(size/this.defaultHeight);
    evt = this.events.get(id);
    if(size != evt.length) {
      eventData = new Object();
      eventData.calendar_id = evt.event.id;
      eventData.element_id = id;
      eventData.date_begin = new Date(evt.event.time * 1000).format('Y-m-d H:i:s');
      eventData.old_date_begin = eventData.date_begin;
      eventData.duration = size*evt.options.yUnit;
      eventData.title = evt.event.title;
      eventData.all_day = evt.event.all_day;
      this.sendUpdateEvent(eventData);
    } else {
      evt.redraw();
      this.redrawAllEvents(evt.event.time);
    }
  },

  register: function(id) {
    evt = this.events.get(id);
    size = evt.length;
    for(var i=0;i < size;i++) {
      t = evt.origin + i*evt.options.unit;
      if(!this.times.get(t)) {
        this.times.set(t.toInt(),new Array());
      }
      this.times.get(t).push(evt);
    }   
  },

  unregister: function(id) {
    evt = this.events.get(id);
    size = evt.length;
    for(var i=0;i < size;i++) {
      t = evt.origin + i*evt.options.unit;
      this.times.get(t).remove(evt);
      if(this.times.get(t).length == 0) {
        this.times.remove(t);
      }
    }
  },

  redrawAllEvents: function() {
    keys = this.times.keys().sort(this.compareTime);
    this.redrawEvents(keys);
  }, 

  redrawEvents: function(keys) {
    resize = new Object();
    end = 0;
    for(var k=0;k < keys.length; k++) {
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
      free = new Array;
      for(var i=0;i<time.length;i++) {
        free.push(i);
      }
      // PERFORMING Event position
      for(var i=0;i<time.length;i++) {
        evt = time[i];
        id = evt.element.id;        
        if(keys[k] == evt.origin) {
          size = evt.length;
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
    for(var i in resize) {
      evt = this.events.get(i);
      evt.conflict(resize[i].unit.size,resize[i].position);
    }
  },
 
  colorize: function () {
    this.events.each(function(evt, key) {
      evt.switchColor(obm.vars.conf.calendarColor); 
    });    
  },

  sendUpdateEvent: function(eventData) {
    ajax = new Ajax('calendar_index.php',
    {postBody:'ajax=1&action=quick_update&' + Object.toQueryString(eventData), onComplete: this.receiveUpdateEvent, method: 'post'});
    ajax.request();
  },
  
  receiveUpdateEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = 'Fatal server error, please reload';
    }
    var events = resp.eventsData;
    if(resp.error == 0) {
      showOkMessage(resp.message);
      var str = resp.elementId.split('-');
      for(var i=0;i< events.length;i++) {
        var ivent = events[i].event;
        var id = str[0]+'-'+str[1] +'-'+ivent.entity+'-'+ivent.entity_id+'-'+str[4];
        var evt = obm.calendarManager.events.get(id);
        if(ivent.state == 'A') {
          obm.calendarManager.unregister(id);
          evt.event.id = ivent.id;
          evt.setDuration(ivent.duration);
          evt.setTime(ivent.time);
          obm.calendarManager.register(id);           
          evt.setTitle(ivent.title);
        } else if (evt) {
          obm.calendarManager.unregister(id);
          obm.calendarManager.events.remove(id);
          evt.destroy();
          delete evt;
        }
      }
      obm.calendarManager.redrawAllEvents();      
    } else {
      showErrorMessage(resp.message);
      obm.calendarManager.events.each(function(evt, key) {
        evt.redraw(); 
      });      
      obm.calendarManager.redrawAllEvents();      
    }
    
  },

  sendCreateEvent: function(eventData) {
    var ajax = new Ajax('calendar_index.php',
    {postBody:Object.toQueryString(eventData), onComplete: this.receiveCreateEvent, method: 'post'});
    ajax.request();
  },

  receiveCreateEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = 'Fatal server error, please reload';
    }    
    var events = resp.eventsData;
    if(resp.error == 0) {
      showOkMessage(resp.message);
      if(resp.day == 1) {
        for(var i=0;i< events.length;i++) {
          obm.calendarManager.lock();
          var e = obm.calendarManager.newDayEvent(events[0].event,events[0].options);
          obm.calendarManager.unlock();
          e.redraw();          
        }
      } else {
        for(var i=0;i< events.length;i++) {        
          obm.calendarManager.lock();
          var e = obm.calendarManager.newEvent(events[0].event,events[0].options);
          obm.calendarManager.unlock();
          e.redraw();                
        }
      }
      obm.calendarManager.redrawAllEvents();      
    } else {
      showErrorMessage(resp.message);
    }
  }, 

    sendDeleteEvent: function(eventData) {
    ajax = new Ajax('calendar_index.php',
    {postBody:'ajax=1&action=quick_delete&' + Object.toQueryString(eventData), onComplete: this.receiveDeleteEvent, method: 'post'});
    ajax.request();
  },
  
  receiveDeleteEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = 'Fatal server error, please reload';
    }
    var events = resp.eventsData;
    if(resp.error == 0) {
      showOkMessage(resp.message);
      var str = resp.elementId.split('-');
      for(var i=0;i< events.length;i++) {
        ivent = events[i].event;
        var id = str[0]+'-'+str[1] +'-'+ivent.entity+'-'+ivent.entity_id+'-'+str[4];
        var evt = obm.calendarManager.events.get(id);
        if (evt) {
          obm.calendarManager.unregister(id);
          obm.calendarManager.events.remove(id);
          evt.destroy();
          delete evt;
        }
      }
      obm.calendarManager.redrawAllEvents();      
    } else {
      showErrorMessage(resp.message);
      obm.calendarManager.events.each(function(evt, key) {
        evt.redraw(); 
      });      
    }
  }
});


/******************************************************************************
 * Calendar Update and creation quick form
 ******************************************************************************/

Obm.CalendarQuickForm = new Class({
  initialize: function() {
    
    this.popup = $('calendarQuickForm');
    this.form = $('calendarQuickFormStore');
    this.date = $('calendarQuickFormDate');
    this.title = $('calendarQuickFormTitle');
    this.data = $('calendarQuickFormData');
    this.description = $('calendarQuickFormDescription');
    this.item = $('calendarQuickFormItem');
    this.location = $('calendarQuickFormLocation');
    this.category = $('calendarQuickFormCategory');
    this.attendees = $('calendarQuickFormAttendees');
    this.deleteButton = $('calendarQuickFormDelete');
    this.detailButton = $('calendarQuickFormDetail');
    this.editButton = $('calendarQuickFormEdit');

    this.popup.setStyle('position','absolute');

    this.eventData = new Object();
    this.eventData.ajax = 1;

    new Obm.TabbedPane(this.data);

  },
  
  compute: function(ivent, context) {
    var target = ivent.target || ivent.srcElement;
    target = $(target);
    if(obm.calendarManager.redrawLock || target.getTag() == 'a') {
      return false;
    }
    while(target.id == '') {
      if(target.getTag() == origin) {
        return false;
      }
      target = $(target.parentNode);
    }
    var str = target.id.split('-');
    if(str.length <= 1) {
      return false;
    }
    var target = $(target);
    var type = str[0];
    if(type == 'time' || type == 'hour') {
      this.setDefaultFormValues(str[1].toInt(),0, context);
    } else if (type == 'day') {
      this.setDefaultFormValues(str[1].toInt() + obm.calendarManager.startTime,1, context);
    } else {
      var elId = 'event-' + str[1] + '-' + str[2] + '-' + str[3] + '-' + str[4];
      var evt = obm.calendarManager.events.get(elId);
      this.setFormValues(evt,context);
    }
    this.show();    
    var left = target.getLeft() - Math.round((this.popup.offsetWidth - target.offsetWidth)/2);
    var top = target.getTop() - this.popup.offsetHeight + Math.round(target.offsetHeight/2);;
    this.popup.setStyles({
      'top':  top + 'px',
      'left': left  + 'px'
    });    
    this.form.tf_title.focus();
  }, 
  
  setFormValues: function(evt, context) {
    var date_begin = new Date(evt.event.time * 1000);
    var date_end = new Date((evt.event.time + evt.event.duration) * 1000);    
    this.form.tf_title.value = evt.event.title;
    this.eventData.calendar_id = evt.event.id;
    this.eventData.entity_id = evt.event.entity_id;
    this.eventData.entity = evt.event.entity;
    this.eventData.all_day = evt.event.all_day;
    this.eventData.date_begin = date_begin.format('Y-m-d H:i:s');
    this.eventData.old_date_begin = new Date(evt.event.time * 1000).format('Y-m-d H:i:s');
    this.eventData.duration = evt.event.duration;
    this.eventData.context = context;
    this.eventData.element_id = evt.element.id;
    this.eventData.action = 'quick_update';
    this.gotoURI = 'action=detailupdate&calendar_id='+evt.event.id;
    
    if(evt.event.updatable) {
      this.form.setStyle('display','block');
      this.deleteButton.setStyle('display','');
      this.editButton.setStyle('display','');
      this.detailButton.setStyle('display','');
      this.editButton.value = obm.vars.labels.edit;
    } else {
      this.form.setStyle('display','none');
      this.title.setHTML(evt.event.title);
      this.title.setStyle('display','block');
    }

    this.description.setHTML(evt.event.description);
    this.item.setHTML(evt.event.item);
    this.category.setHTML(evt.event.category);
    this.location.setHTML(evt.event.location);
    this.data.setStyle('display','block');
    if(this.eventData.all_day != 1) {
      this.date.setHTML(date_begin.format('Y/m/d H:i') + '-' + date_end.format('Y/m/d H:i'));
    } else {
      this.date.setHTML(date_begin.format('Y/m/d') + '-' + date_end.format('Y/m/d'));
    }
    this.attendees.setHTML('');
    for(var i=0;i<evt.event.attendees.length;i++) {
      var attendee = evt.event.attendees[i];
      new Element('h4').appendText(attendee.label).injectInside(this.attendees);
      var ul = new Element('ul').injectInside(this.attendees);
      for(var j=0;j<attendee.entities.length;j++) {
        entity = attendee.entities[j];
        new Element('li').appendText(entity).injectInside(ul);
      }
    }
  },

  setDefaultFormValues: function(time, allDay,context) {
    var date_begin = new Date(time * 1000);
    var date_end = new Date((time + 3600) * 1000);  
    this.form.tf_title.value = '';
    this.eventData.calendar_id = '';
    this.eventData.entity_id = '';
    this.eventData.entity = '';
    this.eventData.all_day = allDay;
    this.eventData.date_begin = date_begin.format('Y-m-d H:i:s');
    this.eventData.old_date_begin = this.eventData.date_begin;
    this.eventData.duration = 3600;
    this.eventData.context = context;
    this.eventData.element_id = '';
    this.eventData.action = 'quick_insert';   
    this.gotoURI = 'action=new';
    this.editButton.value = obm.vars.labels.edit_full;

    this.form.setStyle('display','block');
    this.data.setStyle('display','none');
    this.title.setStyle('display','none');
    this.deleteButton.setStyle('display','none');
    this.editButton.setStyle('display','');
    this.detailButton.setStyle('display','none');    
    if(this.eventData.all_day != 1) {
      this.date.setHTML(date_begin.format('Y/m/d H:i') + '-' + date_end.format('Y/m/d H:i'));
    } else {
      this.date.setHTML(date_begin.format('Y/m/d') + '-' + date_end.format('Y/m/d'));
    }    
    this.attendees.setHTML('');

  },
  
  show: function() {
    this.popup.setStyle('display','block');
  },

  hide: function() {
    this.popup.setStyle('display','none');
  },

  submit: function(action) {
    this.eventData.title = this.form.tf_title.value;
    this.eventData.action = action || this.eventData.action;
    if(this.eventData.action == 'quick_insert') {
      obm.calendarManager.sendCreateEvent(this.eventData);
    } else if (this.eventData.action == 'quick_delete') {
      obm.calendarManager.sendDeleteEvent(this.eventData); 
    } else {
      obm.calendarManager.sendUpdateEvent(this.eventData);
    }
    this.hide();
  },

  goTo: function(action) {
    if(action) {
      this.gotoURI += '&action='+action;
    }
    this.gotoURI += '&all_day='+this.eventData.all_day+'&date_begin='+this.eventData.date_begin+'&duration='+this.eventData.duration+'&title='+this.form.tf_title.value;
    window.location.href = 'calendar_index.php?'+this.gotoURI;
  }
});

Obm.TabbedPane = new Class({
  initialize: function(el) {
    this.element = el;
    this.tabs = new Array();
    var tab = this.element.getFirst();
    this.tabsContainer = new Element('p').injectBefore(tab).addClass('tabs');

    do {
      this.tabs.push(tab);
    } while (tab = tab.getNext());

    this.tabs.each(function (tabContent, index) {
      var title = tabContent.getFirst();
      tabContent.setStyle('height', '4em');
      tabContent.setStyle('overflow','auto');
      title.remove();
      title.injectInside(this.tabsContainer);
      title.tabIndex = index;
      if(index != 0) {
        tabContent.setStyle('display','none');
      } else {
        title.addClass('current');
        this.current = title;
      }

      title.addEvent('click', function(ivent) {
        var target = ivent.currentTarget || ivent.srcElement;
        this.tabs[this.current.tabIndex].setStyle('display','none');
        this.current.removeClass('current');
        this.current = target;
        this.current.addClass('current');
        this.tabs[this.current.tabIndex].setStyle('display','block');
      }.bind(this));
      
    }.bind(this));
  }
});


/******************************************************************************
 * Calendar View Creation and deletion function
 ******************************************************************************/

Obm.CalendarView = new Class({
  initialize: function(el) {
    this.view_id = $('view_id');
  },

  insert: function(label) {
    var qstring = Object.toQueryString({
      view_label: label
    });

    // Select input
    var sel = $('sel_view');
    var opt = sel.getChildren();

    // Get current options
    current_options = new Array();
    for(i=0;i<opt.length;i++) {
      var text = $(opt[i]).text;
      current_options.push(text);
    }

    // Already exists ?
    if(current_options.contains(label)) {
      alert(obm.vars.labels.conflict_view_label);
    } else {
      var ajax = new Ajax('calendar_index.php',
        {postBody: 'ajax=1&action=insert_view&'+qstring, onComplete: 
          function(response){
            try {
              var resp = eval(response);
            } catch (e) {
              resp = new Object();
              resp.error = 1;
              resp.message = 'Fatal server error, please reload';
            }
            if(resp.error == 0) {
              showOkMessage(resp.message);
              var obmbookmark_id = resp.obmbookmark_id;
              var obmbookmark_label = resp.obmbookmark_label;
              var obmbookmark_properties = resp.obmbookmarkproperties;

              var option = new Element('option')
                .setProperties({
                  'id':'opt_'+obmbookmark_id,
                  'value': 'calendar_index.php?'+obmbookmark_properties
                 })
                .setHTML(obmbookmark_label);
              sel.adopt(option);

            } else {
              showErrorMessage(resp.message);
            }
          }
        });
      ajax.request();
    }
  },
 
  remove: function() {
    if (this.view_id.value != "") {
      if (confirm(obm.vars.labels.delete_view)) {
        var qstring = Object.toQueryString({
          view_id: this.view_id.value
        });

        var ajax = new Ajax('calendar_index.php',
          {postBody: 'ajax=1&action=delete_view&'+qstring, onComplete: 
            function(response){
              try {
                var resp = eval(response);
              } catch (e) {
                resp = new Object();
                resp.error = 1;
                resp.message = 'Fatal server error, please reload';
              }
              if(resp.error == 0) {
                showOkMessage(resp.message);
                var obmbookmark_id = resp.obmbookmark_id;

                // Delete option
                $('opt_'+obmbookmark_id).remove(); 

                // Empty current view
                $('view_id').value = "";
              } else {
                showErrorMessage(resp.message);
              }
            }
          });
        ajax.request();
      }
    } else {
      alert(obm.vars.labels.no_sel_view);
    }
  },

  show: function() {
    var newView = prompt(obm.vars.labels.save_view, "");
    if (newView) {
      this.insert(newView);
    }
  }

});
