// Extension : when an event is on more than one day, other days displays are
// considered as display extensions (eg month view)
Obm.CalendarDayEventExtension = new Class({
  initialize: function(parentEvent,size,origin) {
    this.event = parentEvent.event;
    this.options = parentEvent.options;
    this.context = parentEvent.context;
    this.parentEvent = parentEvent;
    this.buildExtension();
    this.size = size;
    this.setOrigin(origin);
    this.setSize(size);
    if (this.options.draggable) {
      this.dragHandler.addEvent('mousedown',this.parentEvent.drag.bound.start)
    }
  },

  setOrigin: function(origin) {
    var hr = $(this.options.type+'_'+origin);
    this.origin = origin;
    this.element.dispose();
    hr.adopt(this.element);
    this.redraw();
    return true;
  },

  setSize: function() {
    this.setWidth(this.size * (obm.calendarManager.defaultWidth +1) - 1);
  },


  buildExtension: function() {
    id = this.parentEvent.element.id+'_extension_'+origin;
    this.element = new Element('div').addClass('event')
                                     .addClass(this.event.klass)
                                     .setProperty('id',id)
                                     .setProperty('title', this.event.title)
                                     .setOpacity(this.getOpacity())
                                    .injectInside(document.body);
    this.dragHandler = new Element('h1')
                                 .setProperty('style','cursor: move;')
                                 .injectInside(this.element);     
    this.extension = new Element('img').setProperty('src',obm.vars.images.extension)
                                       .injectInside(this.dragHandler);
    if (this.event.meeting) {                                 
      this.meeting = new Element('img').setProperty('src',obm.vars.images.meeting)
                                       .injectInside(this.dragHandler);
    }
    if (this.event.private) {
      this.private = new Element('img').setProperty('src',obm.vars.images.private)
                                       .injectInside(this.dragHandler);
    }
    if (this.event.all_day) {
      this.all_day = new Element('img').setProperty('src',obm.vars.images.all_day)
                                       .injectInside(this.dragHandler);
    }
    this.setPeriodicity();
    this.titleContainer = new Element('a').setProperty('href','calendar_index.php?action=detailconsult&calendar_id='+this.event.id)
                                          .injectInside(this.dragHandler);
    this.titleContainer.addEvent('click', function (evt) {if(obm.calendarManager.redrawLock) evt.stop() ;});
    this.resetTitle();
  },

  resetTitle: function() {
    var title = this.event.title + ' ';
    // Display the location only if set
    if (this.event.location != '') {
      title = title + ' (' + this.event.location + ')'; 
    }
    this.element.setProperty('title', title)
    this.titleContainer.set('html',title);
  },

  setPeriodicity: function() {
    if (this.event.periodic) {                                 
      this.periodic = new Element('img').setProperty('src',obm.vars.images.periodic)
                        .injectInside(this.dragHandler);
    } else if (this.periodic) {
      this.periodic.dispose();
    }
  },

  setWidth: function(width) {
    if( (this.element.offsetLeft + width) > this.context.right ) {
      width = this.context.right - this.element.offsetLeft;
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
    this.element.dispose();
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
 * of it's duration (eg bar in month view or all day event)
 *****************************************************************************/
Obm.CalendarDayEvent = new Class({

  Implements: Options,

  options: {
    draggable: false,
    type: 'day',
    yUnit: 0,
    xUnit: 24*3600,
    unit : 24*3600,
    context : 'head'
  },

  initialize: function(eventData,options) {
    this.setOptions(options);
    this.initContext();
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
  
  initContext: function() {
    if(this.options.context == 'body') {
      this.context = obm.calendarManager.bodyContext;
    } else if(this.options.context == 'head') {
      this.context = obm.calendarManager.headContext;
    }
  },

  makeDraggable: function() {
    var dragOptions = {
      handle: this.dragHandler,
      //grid: {'y' : obm.calendarManager.defaultHeight + 1, 'x' : obm.calendarManager.defaultWidth - 1},
      limit: {
        'x': [this.context.left,this.context.right - obm.calendarManager.defaultWidth],
        'y': [this.context.top,this.context.bottom]
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
          this.element.setOpacity(this.getOpacity());
          obm.calendarManager.unlock();
          obm.calendarManager.moveEventTo(this.element.id,this.element.getLeft(),this.element.getTop());
        }        
      }.bind(this)

    };

    this.drag = this.element.makeDraggable(dragOptions);
  },

  getOpacity: function () {
    if(this.event.status == 'NEEDS-ACTION') {
      return .5;
    } else {
      return 1;
    }
  },

  buildEvent: function() {
    id = this.event.id+'_'+this.event.entity+'_'+this.event.entity_id+'_'+this.event.time;
    this.element = new Element('div').addClass('event')
                                     .addClass(this.event.klass)
                                     .setProperty('id','event_'+id)
                                     .setProperty('title', this.event.title)
                                     .setOpacity(this.getOpacity())
                                     .injectInside(document.body);
    this.dragHandler = new Element('h1')
                                 .setProperty('style','cursor: move;')
                                 .injectInside(this.element);
    if (this.event.meeting) {
      this.meeting = new Element('img').setProperty('src',obm.vars.images.meeting)
                        .injectInside(this.dragHandler);
    }
    if (this.event.private) {
      this.private = new Element('img').setProperty('src',obm.vars.images.private)
                                       .injectInside(this.dragHandler);
    }
    if (this.event.all_day) {
      this.all_day = new Element('img').setProperty('src',obm.vars.images.all_day)
                                       .injectInside(this.dragHandler);
    }
    this.setPeriodicity();
    this.titleContainer = new Element('a').setProperty('href','calendar_index.php?action=detailconsult&calendar_id='+this.event.id)
                                          .injectInside(this.dragHandler);
    this.titleContainer.addEvent('click', function (evt) {if(obm.calendarManager.redrawLock) evt.stop() ;});
    this.resetTitle();
  },

  // Add extension (arrow) to event extensions
  setExtension: function(time) {
    var eventTime = time * 1000;
    var calendarStartTime = obm.calendarManager.startTime * 1000;
    if (new Obm.DateTime(eventTime).format('Ymd') < new Obm.DateTime(calendarStartTime).format('Ymd')) {
      this.extension = new Element('img').setProperty('src',obm.vars.images.extension).injectTop(this.titleContainer);
    }
  },

  setPeriodicity: function() {
    if (this.event.periodic) {
      this.periodic = new Element('img').setProperty('src',obm.vars.images.periodic).injectInside(this.dragHandler);
    } else if (this.periodic) {
      this.periodic.dispose();
    }
  },

  resetTitle: function() {
    var title = this.event.title + ' ';
    if (!this.event.all_day) {
      title = new Obm.DateTime(this.event.time * 1000).format("H:i") + ' ' + title;
    }
    // Display the location only if set
    if (this.event.location != '') {
      title = title + ' (' + this.event.location + ')';
    }
    this.element.setProperty('title', title)
    this.titleContainer.set('html',title);
    this.setExtension(this.event.time);
  },

  setTitle: function(title) {
    this.event.title = title;
    this.resetTitle();
    this.extensions.each(function (extension) {
      extension.resetTitle();
    })
  },

  setTime: function(time) {
    var myDate = new Obm.DateTime(time * 1000);
    var startDate = new Obm.DateTime(obm.calendarManager.startTime * 1000);
    startDate.setHours(0);
    startDate.setMinutes(0);
    startDate.setSeconds(0)
    myDate.setHours(0);
    myDate.setMinutes(0);
    myDate.setSeconds(0);

    origin = Math.floor((myDate.getTime() - startDate.getTime())/1000);
    if (this.setOrigin(origin)) {
      this.event.time = this.guessEventTime(time);
    } else {
      if(obm.calendarManager.lock()) {
        this.redraw();
        obm.calendarManager.unlock();
      }
    }
  },
  
  guessEventTime: function(time) {
    if (this.event.time) {
      var time = new Obm.DateTime(time * 1000);
      d = new Obm.DateTime(this.event.time * 1000);
      time.setHours(d.getHours());
      time.setMinutes(d.getMinutes());
      time = Math.floor(time.getTime()/1000);
    }
    return time;
  },


  setOrigin: function(origin) {
    if (origin < 0) {   
      this.hidden = -(origin / this.options.xUnit);
      this.hidden = Math.ceil(this.hidden);
      origin = 0;
    } else if(this.hidden != 0) {
      this.hidden = 0;
      this.setSize(this.size);
    }
    var hr = $(this.options.type+'_'+origin);
    this.origin = origin;
    this.element.dispose();
    hr.adopt(this.element);
    if (obm.calendarManager.lock()) {
      this.redraw();
      obm.calendarManager.unlock();
      if (this.resizeLine()) {
        obm.calendarManager.resizeWindow();
      }      
    }
    return true;
  },

  resizeLine: function(lineElem) {
      if(!$(lineElem)) {
        var hr = $(this.options.type + '_' + this.origin);
        var lineElem = hr.parentNode;
      }
      var thead = $(lineElem).getFirst();
      var size = 0;
      do {
        if (thead.childNodes.length > size) size = thead.childNodes.length;
      } while (thead = thead.getNext());
      size = size * this.element.offsetHeight + 5;
      if (hr.parentNode.offsetHeight != size) {
        hr.parentNode.setStyle('height', size + 'px');    
        return true
      }        
      return false;
  },

  setDuration: function(duration) {
    this.event.duration = duration;
    startTime = new Obm.DateTime(this.event.time * 1000);
    startTime.setHours(0);
    startTime = startTime.getTime()/1000;
    endTime = new Obm.DateTime((this.event.time + this.event.duration)*1000);
    endTime = endTime.getTime()/1000;
    var dayDuration = Math.ceil((endTime - startTime) / 86400);
    this.setSize(dayDuration);
  },

  setSize: function(size) {
    hr = $(this.element.parentNode);
    this.trashExtensions();
    this.size = size - this.hidden;
    this.length = this.size;
    this.drawExtensions();
    if (obm.calendarManager.lock()) {
      this.setWidth(this.size * (hr.clientWidth+1) - 1);
      if (this.drag) {
        this.drag.options.xMax = this.context.right - obm.calendarManager.defaultWidth;
      }
      obm.calendarManager.unlock();
    }
  },

  redraw: function() {
    hr = $(this.element.parentNode);
    this.element.setStyles({
      'top':  hr.getTop() + hr.getStyle('padding-top').toInt() + 'px',
      'left': (hr.getLeft() + (hr.clientLeft || 0)) + 'px'
    });
    this.setWidth(this.size * (hr.clientWidth+1) - 1);
    if (this.options.draggable) {
      this.drag.options.limit = {
        'x': [this.context.left,this.context.right - obm.calendarManager.defaultWidth],
        'y': [this.context.top,this.context.bottom - this.element.offsetHeight]};      
    }   
    this.extensions.each( function (extension) {
      extension.redraw();
    });
  },

  setWidth: function(width) {
    if( (this.element.offsetLeft + width) > this.context.right ) {
      width = this.context.right - this.element.offsetLeft;
    }
    // Waiting events add a border
    status = this.event.status;
    if (status == 'W') {
      status_size = 2;
    } else {
      status_size = 0;
    }
    width -= status_size;
    this.element.setStyle('width',width + 'px');
  },

  conflict: function(size, position) {
    this.element.setStyle('margin-top',position * this.element.offsetHeight + 'px');
    this.extensions.each( function (extension) {
      extension.conflict(size, position);
    }.bind(this));
  },

  toQueryString: function() {
    date_begin = new Obm.DateTime(this.event.time * 1000);
    date_end = new Obm.DateTime(this.event.time * 1000 + this.event.duration * 1000);    
    query = 'calendar_id=' + this.event.id;
    query += '&date_begin=' + date_begin.format('O');
    query += '&duration=' + this.event.duration;
    query += '&title=' + this.event.title;
    return query;
  },
  
  drawExtensions: function() {
    if(this.size != 1) {
      dayBegin = (new Obm.DateTime(this.event.time * 1000).getDay() + this.hidden - obm.vars.consts.weekStart + 7) % 7; 
      dayEnd = (dayBegin + this.size + 6) % 7;
      var startDate = new Obm.DateTime((obm.calendarManager.startTime + this.origin) * 1000);
      while(dayEnd < dayBegin || this.size > 7) {
        var extensionSize = dayEnd + 1;
        this.size -= extensionSize;
        var extensionDate = new Obm.DateTime(startDate.getTime());
        extensionDate.setDate(startDate.getDate() + this.size);
        var extensionOrigin = Math.floor(extensionDate.getTime()/1000) - obm.calendarManager.startTime;
        dayEnd = 6;
        if($(this.options.type+'_'+extensionOrigin)) {
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
    this.element.dispose();
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
Obm.CalendarEvent = new Class({

  Extends: Obm.CalendarDayEvent,

  options: {
    draggable: false,
    resizable: false,
    type: 'time',
    yUnit: obm.vars.consts.timeUnit,
    xUnit: (3600*24),
    unit : obm.vars.consts.timeUnit,
    context: 'body'
  },

  initialize: function(eventData,options) {
    this.setOptions(options);
    this.initContext();  
    this.event = eventData;
    this.extensions = new Array();
    this.size = 1;
    this.length = 1;
    this.buildEvent();
    this.switchColor(obm.vars.conf.calendarColor);
    if (this.options.resizable)
      this.makeResizable(options);
    if (this.options.draggable)
      this.makeDraggable();
    this.setTime(this.event.time);
    this.setDuration(this.event.duration);
  },
  
  buildEvent: function() {
    id = this.event.id+'_'+this.event.entity+'_'+this.event.entity_id+'_'+this.event.time;
    this.element = new Element('div').addClass('event')
                                     .addClass(this.event.klass)
                                     .setProperty('id','event_'+id)
                                     .setProperty('title', this.event.title)
                                     .setOpacity(this.getOpacity())
                                     .injectInside(document.body);
    this.dragHandler = new Element('h1')
                                 .setProperty('style','cursor: move;')
                                 .injectInside(this.element);     
    if(this.event.meeting) {                                 
      new Element('img').setProperty('src',obm.vars.images.meeting)
                        .injectInside(this.dragHandler);
    }
    if (this.event.private) {
      this.private = new Element('img').setProperty('src',obm.vars.images.private)
                                       .injectInside(this.dragHandler);
    }
    this.setPeriodicity();
    if(this.options.resizable) {
    this.resizeHandler = new Element('div')
      .addClass(this.event.klass)
      .addClass('handle')
      .injectInside(this.element);
    
    }

    this.titleContainer = new Element('span').injectInside(this.element);
    this.locationContainer = new Element('span').injectInside(this.element);
    this.timeContainer = new Element('a')
       .setProperty('href','calendar_index.php?action=detailconsult&calendar_id='+this.event.id)
       .injectInside(this.dragHandler);
    this.timeContainer.addEvent('click', function (evt) {if(obm.calendarManager.redrawLock) evt.stop() ;});
    this.resetTitle();

  },

  resetTitle: function() {
    var location = '';
    if (this.event.location != '') {
      location = '(' + this.event.location + ')';
    }
    var title = this.event.title + ' ';
    if (this.event.duration <= this.options.unit) {
      var time = new Obm.DateTime(this.event.time * 1000).format("H:i") + ' ' + title; 
      var title = '';
    } else {
      var time = new Obm.DateTime(this.event.time * 1000).format("H:i");
    }
    this.element.setProperty('title', this.event.title + ' ' + location);
    this.timeContainer.set('html',time);
    this.titleContainer.set('html',title);
    this.locationContainer.set('html',location);
  },

  setDuration: function(duration) {
    this.event.duration = duration;
    size = Math.ceil(duration/this.options.yUnit);
    this.setSize(size);
  },

  setSize: function(size) {
    this.size = size;
    this.length = size;
    if (obm.calendarManager.lock()) {  
      height = size * obm.calendarManager.defaultHeight;
      this.setHeight(height);
      if (this.resize) {
        this.resize.options.yMax = this.context.bottom - this.element.getTop();
      }
      if (this.drag) {
        this.drag.options.yMax = this.context.bottom - this.element.offsetHeight;
      }
      obm.calendarManager.unlock();
    }
  },

  setHeight: function(height) {
    if((this.element.getTop() + height) > this.context.bottom) {
      height = this.context.bottom - this.element.getTop();
    }
    // Waiting events add a border
    status = this.event.status;
    if (status == 'W') {
      status_size = 2;
    } else {
      status_size = 0;
    }
    height -= status_size;
    this.element.setStyle('height',height + 'px');
  },

  makeResizable: function() {
    var resizeOptions = {
      handle: this.resizeHandler,
      limit: {
        'x': [obm.calendarManager.defaultWidth,obm.calendarManager.defaultWidth],
        'y': [obm.calendarManager.defaultHeight,this.context.bottom]
      },

      onStart:function() {
        obm.calendarManager.lock();
        this.element.setStyle('margin-left', '0');
        this.element.setOpacity(.7);
      },
      
      onComplete:function() {
        this.element.setOpacity(this.getOpacity());
        obm.calendarManager.resizeEventTo(this.element.id,this.element.offsetHeight);
        obm.calendarManager.unlock();
      }.bind(this)     
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
    hr = $(this.options.type+'_'+origin);
    if(hr) {
      this.origin = origin;
      this.element.dispose();
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
      'left': (hr.getLeft() + (hr.clientLeft || 0)) + 'px'      
    });
    this.setHeight(this.size * obm.calendarManager.defaultHeight);
    if (this.options.draggable) {
      this.drag.options.limit = {
        'x': [this.context.left,this.context.right - obm.calendarManager.defaultWidth],
        'y': [this.context.top,this.context.bottom - this.element.offsetHeight]};         
    }    
    if (this.options.resizable) {
      this.resize.options.limit = {
        'x': [obm.calendarManager.defaultWidth,obm.calendarManager.defaultWidth],
        'y': [obm.calendarManager.defaultHeight,this.context.bottom - this.element.getTop()]};      
    }
  },

  conflict: function(size, position) {
    hr = $(this.element.parentNode);
    var modulo       = hr.clientWidth%size;
    var correctWidth = (modulo > position ? 1 : 0);
    var correctPos   = (modulo > position ? position : modulo);
    if (size > 1)
      this.setWidth(Math.floor(hr.clientWidth/size)+correctWidth);
    else
      this.setWidth(hr.clientWidth);
    this.setMargin(Math.floor(hr.clientWidth/size)*position+correctPos);
  },

  setColor: function(color) {
    if (color) {
      this.element.setStyle('backgroundColor',color);
      this.dragHandler.setStyle('backgroundColor',color);
    } else {
      this.element.setStyle('backgroundColor','');
      this.dragHandler.setStyle('backgroundColor','');
    }
  }


});


/******************************************************************************
 * Calendar Manager Popup
 ******************************************************************************/
Obm.CalendarPopupManager = new Class({

  Implements: Events,

  initialize: function() {
    this.evt = null;
    this.chain = new Chain();
    // Close popup and redraw event
    $('popup_close').addEvent('click', function() {
      this.cancel();
    }.bind(this));

    // Redirect to conflict manager form
    $('popup_manage').addEvent('click', function() {
      this.fireEvent('conflict');
      this.cancel();
    }.bind(this));

    // Force event update
    $('popup_force').addEvent('click', function() {
      obm.popup.hide('calendarConflictPopup');
      this.chain.callChain();
    }.bind(this));

    // Close popup and redraw event
    $('popup_cancel').addEvent('click', function() {
      this.cancel();
    }.bind(this));

    // Mail Notification actions
    $('popup_sendmail_yes').addEvent('click', function() {
      this.fireEvent('mail');
      this.chain.callChain();
    }.bind(this));

    $('popup_sendmail_no').addEvent('click', function() {
      this.chain.callChain();
    }.bind(this));

    $('popup_sendmail_close').addEvent('click', function() {
      this.chain.callChain();
    }.bind(this));
  },

  show: function(evt) {
    this.evtId = evt.element_id;
    this.chain.chain(this.complete.bind(this));
    this.chain.callChain();
  },

  add: function(popup) {
    this.chain.chain(function () {
      obm.popup.show(popup)
    }.bind(this));
  },

  cancel: function() {
    this.chain.clearChain();
    this.removeEvents();
    obm.calendarManager.unlock();
    obm.calendarManager.events.get(this.evtId).redraw();
    obm.calendarManager.redrawAllEvents();
  },
  
  complete: function() {
    this.chain.clearChain();
    this.fireEvent('complete')
    this.removeEvents();
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
    zoneWidth = $$('table.calendar')[0].offsetWidth - $('calendarHourCol').offsetWidth ;//$('calendarEventContext').offsetWidth;
    this.evidence = body.getElement('td');

    this.headContext = new Object();
    if (head) {
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

    this.popupManager = new Obm.CalendarPopupManager(); 
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
      zoneWidth = $$('table.calendar')[0].offsetWidth - $('calendarHourCol').offsetWidth ;//$('calendarEventContext').offsetWidth;

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
    if (diff != 0)
      return diff;
    diff = event1.event.id - event2.event.id;
    if (diff != 0)
      return diff;
    return event1.event.entity_id - event2.event.entity_id;
  },

  compareTime: function(time1, time2) {
    return time1.toInt() - time2.toInt();
  },

  getEventNewPosition: function(elem) {
    var id = elem.id;
    var left = elem.getLeft();
    var top = elem.getTop();
    var evt = this.events.get(id);
    var xDelta = Math.round((left-evt.context.left)/this.defaultWidth);
    var yDelta = Math.round((top-evt.context.top)/this.defaultHeight);
    var secDelta = xDelta*evt.options.xUnit + yDelta*evt.options.yUnit;
    var dayDelta = Math.floor(secDelta / 86400);
    secDelta = secDelta - (dayDelta * 86400);
    var hourDelta = Math.floor(secDelta / 3600);
    secDelta = secDelta - (hourDelta * 3600);
    var minDelta = Math.floor(secDelta / 60);
    secDelta = secDelta - (minDelta * 60);
    var startDate = new Obm.DateTime(this.startTime * 1000);
    startDate.setDate(startDate.getDate() + dayDelta);
    startDate.setHours(startDate.getHours() + hourDelta);
    startDate.setMinutes(startDate.getMinutes() + minDelta);
    startDate.setSeconds(startDate.getSeconds() + secDelta);

    return startDate;
  },

  moveEventTo: function(id) {
    var evt = this.events.get(id);
    startDate = obm.calendarManager.getEventNewPosition(evt.element);
    time = Math.floor(startDate.getTime() / 1000);
    guessedTime = evt.guessEventTime(time);
    if (evt.event.time != guessedTime) {
      eventData = new Object();
      eventData.calendar_id = evt.event.id;
      eventData.element_id = id;
      eventData.date_begin = new Obm.DateTime(guessedTime * 1000).format('c');
      eventData.old_date_begin = new Obm.DateTime(evt.event.time * 1000).format('c');
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
    if (size != evt.length) {
      eventData = new Object();
      eventData.calendar_id = evt.event.id;
      eventData.element_id = id;
      eventData.date_begin = new Obm.DateTime(evt.event.time * 1000).format('c');
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
      this.times.get(t).erase(evt);
      if(this.times.get(t).length == 0) {
        this.times.erase(t);
      }
    }
  },

  redrawAllEvents: function() {
    keys = this.times.getKeys().sort(this.compareTime);
    this.redrawEvents(keys);
  }, 

  redrawEvents: function(keys) {
    resize = new Object();
    end = 0;
    for(var k=0;k < keys.length; k++) {
      time = this.times.get(keys[k]);
      if (time.length == 0)
        continue;
      if (end == 0) {
        unit = new Object()
        unit.size = 1;
      }
      if (unit.size < time.length) {
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
        if (keys[k] == evt.origin) {
          size = evt.length;
          resize[id] = new Object;
          resize[id].position = free.splice(0, 1)[0];
          resize[id].unit = unit;      
          if (size > end) {
            end = size;
          }
        } else  {
          free.erase(resize[id].position);
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
    new Request.JSON({
      url : 'calendar_index.php',
      secure : false,
      onComplete : function(response) {
        if (response.conflict) {
          obm.calendarManager.popupManager.add('calendarConflictPopup');
        }
        if (response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
        obm.calendarManager.popupManager.addEvent('mail', function () {
          eventData.send_mail = true;
        });        
        obm.calendarManager.popupManager.addEvent('conflict', function() {
          eventData = $merge({action : 'conflict_manager'}, eventData)
          window.location=obm.vars.consts.calendarUrl+'?'+Hash.toQueryString(eventData);
        });
        obm.calendarManager.popupManager.addEvent('complete', function () {
          new Request.JSON({
            url : 'calendar_index.php',
            secure : false,
            onComplete : this.receiveUpdateEvent.bind(this)
          }).post($merge({ajax : 1, action : 'quick_update'}, eventData));
        }.bind(this));
        obm.calendarManager.popupManager.show(eventData);
      }.bind(this)
    }).post($merge({ajax : 1, action : 'check_conflict'}, eventData));    
  }, 

  
  receiveUpdateEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = obm.vars.labels.fatalServerErr;
    }
    var events = response.eventsData;
    if (response.error == 0) {
      showOkMessage(response.message);
      var str = response.elementId.split('_');
      for(var i=0;i< events.length;i++) {
        var ivent = events[i].event;
        var id = str[0]+'_'+str[1] +'_'+ivent.entity+'_'+ivent.entity_id+'_'+str[4];
        var evt = obm.calendarManager.events.get(id);
        if (ivent.status == 'ACCEPTED' || ivent.status == 'NEEDS-ACTION') {
          obm.calendarManager.unregister(id);
          evt.event.id = ivent.id;
          evt.event.status = ivent.status;
          evt.setDuration(ivent.duration);
          evt.setTime(ivent.time);
          obm.calendarManager.register(id);           
          evt.setTitle(ivent.title);
        } else if (evt) {
          obm.calendarManager.unregister(id);
          obm.calendarManager.events.erase(id);
          evt.destroy();
          delete evt;
        }
      }
      obm.calendarManager.redrawAllEvents();      
      obm.calendarManager.updateLastVisitEvent(events);
    } else {
      showErrorMessage(response.message);
      obm.calendarManager.events.each(function(evt, key) {
        evt.redraw(); 
      });      
      obm.calendarManager.redrawAllEvents();      
    }
    
  },

  sendCreateEvent: function(eventData) {
    new Request.JSON({
      url : 'calendar_index.php',
      secure : false,
      onComplete : function(response) {
        if (response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
        obm.calendarManager.popupManager.addEvent('mail', function () {
          eventData.send_mail = true;
        });        
        obm.calendarManager.popupManager.addEvent('complete', function () {
          new Request.JSON({
            url : 'calendar_index.php',
            secure : false,
            onComplete : this.receiveCreateEvent
          }).post($merge({ajax : 1, action : 'quick_insert'}, eventData));          
        }.bind(this));
        obm.calendarManager.popupManager.show(eventData);
      }.bind(this)
    }).post($merge({ajax : 1, action : 'check_conflict'}, eventData));

  },

  receiveCreateEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = obm.vars.labels.fatalServerErr;
    }    
    var events = response.eventsData;
    if (response.error == 0) {
      showOkMessage(response.message);
      if (response.day == 1) {
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
      obm.calendarManager.updateLastVisitEvent(events);
    } else {
      showErrorMessage(response.message);
    }
  }, 

  sendDeleteEvent: function(eventData) {
    new Request.JSON({
      url : 'calendar_index.php',
      secure : false,
      onComplete : function(response) {
        if(response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
        obm.calendarManager.popupManager.addEvent('mail', function () {
          eventData.send_mail = true;
        });        
        obm.calendarManager.popupManager.addEvent('complete', function () {
          new Request.JSON({
            url : 'calendar_index.php',
            secure : false,
            onComplete : this.receiveDeleteEvent
          }).post($merge({ajax : 1, action : 'quick_delete'}, eventData));             
        }.bind(this));
        obm.calendarManager.popupManager.show(eventData);
      }.bind(this)
    }).post($merge({ajax : 1, action : 'check_conflict'}, eventData));    
 
  },
  
  receiveDeleteEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = obm.vars.labels.fatalServerErr;
    }
    var events = response.eventsData;
    if (response.error == 0) {
      showOkMessage(response.message);
      var str = response.elementId.split('_');
      for(var i=0;i< events.length;i++) {
        ivent = events[i].event;
        var id = str[0]+'_'+str[1] +'_'+ivent.entity+'_'+ivent.entity_id+'_'+str[4];
        var evt = obm.calendarManager.events.get(id);
        if (evt) {
          obm.calendarManager.unregister(id);
          obm.calendarManager.events.erase(id);
          evt.destroy();
          delete evt;
        }
      }
      obm.calendarManager.redrawAllEvents();      
    } else {
      showErrorMessage(response.message);
      obm.calendarManager.events.each(function(evt, key) {
        evt.redraw(); 
      });      
    }
  },

  updateLastVisitEvent: function(events) {
    var id = events[0].event.id;
    var title = events[0].event.title;
    var url = obm.vars.consts.calendarDetailconsultURL+id;
    $('last_visit_calendar_event_a').setProperty('href', url);
    $('last_visit_calendar_event_title').innerHTML = title;
    $('last_visit_calendar_event_a').getParent().setStyle('display', '');
  }
});


/******************************************************************************
 * Calendar Update and creation quick form
 ******************************************************************************/
Obm.CalendarQuickForm = new Class({
  initialize: function() {
    
    this.form = $('calendarQuickFormStore');
    this.date = $('calendarQuickFormDate');
    this.title = $('calendarQuickFormTitle');
    this.data = $('calendarQuickFormData');
    this.description = $('calendarQuickFormDescription');
    this.item = $('calendarQuickFormItem');
    this.owner = $('calendarQuickFormOwner');
    this.location = $('calendarQuickFormLocation');
    this.category = $('calendarQuickFormCategory');
    this.attendees = $('calendarQuickFormAttendees');
    this.deleteButton = $('calendarQuickFormDelete');
    this.detailButton = $('calendarQuickFormDetail');
    this.editButton = $('calendarQuickFormEdit');
    this.entityView = $('calendarViewEntity');
    this.entityKind = $('calendarKindEntity');
    this.entityList = $('calendarListEntity');

    this.eventData = new Object();

    new Obm.TabbedPane(this.data);
  },
  
  compute: function(ivent, context) {
    ivent = new Event(ivent)
    var target = ivent.target;
    target = $(target);
    if (obm.calendarManager.redrawLock || target.get('tag') == 'a') {
      return false;
    }
    while(target.id == '') {
      if (target.get('tag') == origin) {
        return false;
      }
      target = $(target.parentNode);
    }
    var str = target.id.split('_');
    if (str.length <= 1) {
      return false;
    }
    var target = $(target);
    var type = str[0];
    if (type == 'time' || type == 'hour') {
      this.setDefaultFormValues(str[1].toInt(),0, context);
    } else if (type == 'day') {
      this.setDefaultFormValues(str[1].toInt() + obm.calendarManager.startTime,1, context);
    } else {
      var elId = 'event_' + str[1] + '_' + str[2] + '_' + str[3] + '_' + str[4];
      var evt = obm.calendarManager.events.get(elId);
      this.setFormValues(evt,context);
    }
    this.show();    
    this.form.tf_title.focus();
  },

  setFormValues: function(evt, context) {
    var date_begin = new Obm.DateTime(evt.event.time * 1000);
    var date_end = new Obm.DateTime((evt.event.time + evt.event.duration) * 1000);    
    this.form.tf_title.value = evt.event.title;
    this.eventData.calendar_id = evt.event.id;
    this.eventData.entity_id = evt.event.entity_id;
    this.eventData.entity = evt.event.entity;
    this.eventData.all_day = evt.event.all_day;
    this.eventData.date_begin = date_begin.format('c');
    this.eventData.old_date_begin = new Obm.DateTime(evt.event.time * 1000).format('c');
    this.eventData.duration = evt.event.duration;
    this.eventData.context = context;
    this.eventData.element_id = evt.element.id;
    this.eventData.formAction = 'quick_update';
    this.gotoURI = 'action=detailupdate&calendar_id='+evt.event.id;
    
    if (evt.event.updatable) {
      this.form.setStyle('display','block');
      this.deleteButton.setStyle('display','');
      this.editButton.setStyle('display','');
      this.detailButton.setStyle('display','');
      this.entityList.setStyle('display','none');
      this.editButton.value = obm.vars.labels.edit;
    } else {
      this.form.setStyle('display','none');
      this.title.set('html',evt.event.title);
      this.title.setStyle('display','block');
    }

    this.description.set('html',evt.event.description);
    this.item.set('html',evt.event.item);
    this.category.set('html',evt.event.category);
    this.owner.set('html',evt.event.owner_name);
    this.location.set('html',evt.event.location);
    this.data.setStyle('display','block');
    if (!this.eventData.all_day) {
      this.date.set('html',date_begin.format('Y/m/d H:i') + '-' + date_end.format('Y/m/d H:i'));
    } else {
      this.date.set('html',date_begin.format('Y/m/d') + '-' + date_end.format('Y/m/d'));
    }
    this.attendees.set('html','');
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

  setDefaultFormValues: function(time, allDay, context) {
    var date_begin = new Obm.DateTime(time * 1000);
    var date_end = new Obm.DateTime((time + 3600) * 1000);  
    this.form.tf_title.value = '';
    this.eventData.calendar_id = '';
    this.eventData.entity_id = '';
    this.eventData.entity = '';
    this.eventData.all_day = allDay;
    this.eventData.date_begin = date_begin.format('c');
    this.eventData.old_date_begin = this.eventData.date_begin;
    this.eventData.duration = 3600;
    this.eventData.context = context;
    this.eventData.element_id = '';
    this.eventData.formAction = 'quick_insert';
    this.gotoURI = 'action=new';
    this.editButton.value = obm.vars.labels.edit_full;

    this.form.setStyle('display','block');
    this.data.setStyle('display','none');
    this.title.setStyle('display','none');
    this.deleteButton.setStyle('display','none');
    this.editButton.setStyle('display','');
    this.detailButton.setStyle('display','none');
    this.entityList.setStyle('display','block');
    if (!this.eventData.all_day) {
      this.date.set('html',date_begin.format('Y/m/d H:i') + '-' + date_end.format('Y/m/d H:i'));
    } else {
      this.date.set('html',date_begin.format('Y/m/d') + '-' + date_end.format('Y/m/d'));
    }
    this.attendees.set('html','');
  },

  show: function() {
    //this.popup.setStyle('display','');
    obm.popup.show('calendarQuickForm');
  },

  hide: function() {
    obm.popup.hide('calendarQuickForm');
    //this.popup.setStyle('display','none');
  },

  submit: function(action) {
    this.eventData.title = this.form.tf_title.value;
    this.eventData.entity_id = this.entityView.get('inputValue');
    this.eventData.entity_kind = this.entityKind.value;
    this.eventData.send_mail = null;
    action = action || this.eventData.formAction;
    if (action == 'quick_insert') {
      obm.calendarManager.sendCreateEvent(this.eventData);
    } else if (action == 'quick_delete') {
      obm.calendarManager.sendDeleteEvent(this.eventData); 
    } else {
      obm.calendarManager.sendUpdateEvent(this.eventData);
    }
    this.hide();
  },

  goTo: function(action) {
    if (action) {
      this.gotoURI += '&action='+action;
    }
    this.eventData.entity_id = this.entityView.get('inputValue');
    this.gotoURI += '&utf8=1&all_day='+this.eventData.all_day+'&date_begin='+encodeURIComponent(this.eventData.date_begin)+'&duration='+this.eventData.duration+'&title='+encodeURIComponent(this.form.tf_title.value)+'&new_user_id[]='+this.eventData.entity_id;
    window.location.href = 'calendar_index.php?'+this.gotoURI;
  }
});

Obm.TabbedPane = new Class({
  initialize: function(el) {
    this.element = el;
    this.tabs = this.element.getElements('div');
    this.tabsContainer = new Element('p').injectTop(this.element).addClass('tabs');

    this.current = this.tabs[0].getFirst();
    this.current.addClass('current');

    this.tabs.each(function (tabContent, index) {
      var title = tabContent.getFirst();
      tabContent.setStyle('height', '6em');
      tabContent.setStyle('overflow','auto');
      title.dispose();
      title.injectInside(this.tabsContainer);
      title.tabIndex = index;
      if (index != 0) {
        tabContent.setStyle('display','none');
      }

      title.addEvent('click', function(ivent) {
        var target = ivent.target;

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
    this.default_view_id = $('default_view_id');
  },

  insert: function(label) {
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
    if (current_options.contains(label)) {
      alert(obm.vars.labels.conflict_view_label);
    } else {
      new Request.JSON({
        url : 'calendar_index.php',
        secure : false,
        onComplete : function(response){
          try {
            var resp = eval(response);
          } catch (e) {
            resp = new Object();
            resp.error = 1;
            resp.message = obm.vars.labels.fatalServerErr;
          }
          if (response.error == 0) {
            showOkMessage(response.message);
            var obmbookmark_id = response.obmbookmark_id;
            var obmbookmark_label = response.obmbookmark_label;
            var obmbookmark_properties = response.obmbookmarkproperties;

            var option = new Element('option')
              .setProperties({
                'id':'opt_'+obmbookmark_id,
                'value': 'calendar_index.php?'+obmbookmark_properties
               })
              .set('html',obmbookmark_label);
            sel.adopt(option);

          } else {
            showErrorMessage(response.message);
          }
        }
      }).post({ajax : 1, action : 'insert_view', view_label : label});        
    }
  },
 
  remove: function() {
    if (this.view_id.value != "") {
      if (confirm(obm.vars.labels.delete_view)) {
        new Request.JSON({
          url :'calendar_index.php',
          secure : false,
          onComplete : 
            function(response){
              try {
                var resp = eval(response);
              } catch (e) {
                resp = new Object();
                resp.error = 1;
                resp.message = obm.vars.labels.fatalServerErr;
              }
              if (response.error == 0) {
                showOkMessage(response.message);
                var obmbookmark_id = response.obmbookmark_id;
                // Delete option
                $('opt_'+obmbookmark_id).dispose(); 

                // Empty current view
                $('view_id').value = "";
              } else {
                showErrorMessage(response.message);
              }
            }
          }
        ).post({ajax : 1, action : 'delete_view',view_id: this.view_id.value});
      }
    } else {
      alert(obm.vars.labels.no_sel_view);
    }
  },

  default_view: function() {
    var action = "";
    var confirm_message = "";
    var id = this.view_id.value;
    var default_id = this.default_view_id.value;

    if (id != "") {
      if (id != default_id) {
        action = 'insert_default_view';
        confirm_message = obm.vars.labels.insert_default_view;
      } else {
        action = 'delete_default_view';
        confirm_message = obm.vars.labels.delete_default_view;
      }
    } else {
      var message_error = obm.vars.labels.no_sel_default_view;
      if (default_id != "") {
        action = 'delete_default_view';
        confirm_message = obm.vars.labels.delete_default_view;
      } else {
        message_error = obm.vars.labels.no_default_view;
      }
    }

    if (action != "") {
      if (confirm(confirm_message)) {
        new Request.JSON({
          url :'calendar_index.php',
          secure : false,
          onComplete : 
            function(response){
              try {
                var resp = eval(response);
              } catch (e) {
                resp = new Object();
                resp.error = 1;
                resp.message = obm.vars.labels.fatalServerErr;
              }
              if (response.error == 0) {
                showOkMessage(response.message);
                
                if (action == 'insert_default_view') {
                  $('default').set('html', obm.vars.labels.delete_default_view);
                  $('default_view_id').value = id;
                  
                  //change visual for default view
                  $('opt_'+id).set('default','default');
                  $('my_view').set('href',$('opt_'+id).value);
                  if (default_id != "") {
                    $('opt_'+default_id).erase("default");
                  }

                } else { //delete default view
                  
                  $('default').set('html', obm.vars.labels.insert_default_view);
                  $('default_view_id').value = "";
                  $('my_view').set('href',$('opt_choix').value);
                  $('opt_'+default_id).erase("default");
                  
                  //hidde option for default view
                  if ($('view_id').value == "") {
                    $('default').set('styles',{'display':'none'});
                  }

                }
              } else {
                showErrorMessage(response.message);
              }
            }
          }
        ).post({ajax: 1,action: action, view_id: this.view_id.value});
      }
    } else {
      alert(message_error);
    }
  },

  show: function() {
    var newView = prompt(obm.vars.labels.save_view, "");
    if (newView) {
      this.insert(newView);
    }
  }

});


/*
 * FreeBusy interface
 */
Obm.CalendarFreeBusy = new Class({

  /*
   * Init
   * Build meeting slider, meeting resizer
   * time_slots: contains all timestamps
   * busy_time_slots: contains busy indexes (not timestamp)
   */
  initialize: function(nbSteps, duration, time_slots, busy_time_slots,unit,meeting_date) {

    this.meeting_date = meeting_date;
    this.unit = unit;
    this.stepSize = 40/this.unit;
    this.scrollDiv = $('calendarFreeBusyScroll');
    this.container = $('calendarFreeBusyGrid');
    this.meeting = $('calendarFreeBusyMeeting');
    this.resizeHandler = $('calendarFreeBusyResizeMeeting');
    this.table = $('calendarFreeBusyTable'); 
    this.scrollRight = $('scrollRight');
    this.scrollLeft = $('scrollLeft');
    this.nbSteps = nbSteps;
    this.duration = duration;
    this.bts = busy_time_slots;
    this.ts = time_slots;
    this.meeting_slots = this.duration*this.unit;
    this.oneDayWidth = this.stepSize*this.nbSteps/7; // in px
    this.oneDaySteps = this.nbSteps/7; // nb slots per day
    this.limitRight = this.scrollDiv.getLeft()+this.scrollDiv.offsetWidth;
    this.currentPosition = 0;
    // this.previousPosition = 0;

    // this.table.setStyle('width', this.nbSteps*this.stepSize+'px');  

    // /!\ meeting width must be set BEFORE Slider building
    this.meeting.setStyle('width', this.stepSize*this.meeting_slots+'px');

    // Meeting
    this.slider = new Slider(this.container, this.meeting, {
      steps: this.nbSteps-this.meeting_slots,
      snap: true,
      onChange: function(pos) {
        this.currentPosition = pos;
        var end_pos = this.currentPosition + this.meeting_slots-1;
        var date_begin_ts = this.ts[this.currentPosition]*1000;
        var date_begin = new Obm.DateTime(date_begin_ts);
        var date_end_ts = this.ts[end_pos]*1000;
        var date_end = new Obm.DateTime(date_end_ts);

        // if (this.previousPosition > this.currentPosition) {
        //   if (date_begin.format('d') < date_end.format('d')) {
        //      this.slider.set(end_pos-this.meeting_slots);
        //   }
        // } else {
        //   if (date_end.format('d') > date_begin.format('d')) {
        //      this.slider.set(end_pos);
        //    }
        // }

        // Auto-Scroll
        // FIXME: problem with onTick
        // if (this.meeting.getLeft()+this.meeting_slots*this.stepSize>=this.limitRight
        //   || this.meeting.getLeft() <= this.table.getLeft()+this.scrollDiv.scrollLeft) {
        //   this.autoScroll.toElement(this.meeting);
        // }

        // Set meeting color
        this.changeStatus(this.isBusy(end_pos));
    
        // Display meeting date begin and date end
        this.displayMeetingInfo();

        // Store old position
        // this.previousPosition = this.currentPosition;        

        this.forcePosition();
      }.bind(this),

      onComplete: function() {
        var ts = this.ts[this.currentPosition] * 1000;
        var date_begin = new Obm.DateTime(ts);
        $('date_begin').value = date_begin.format('c');
      }.bind(this)

    });

    var resizeOptions = {
      handle: this.resizeHandler,
      grid: {'x' : this.slider.stepWidth},
      limit: {
        'x': [this.stepSize],
        'y': [this.container.offsetHeight, this.container.offsetHeight]
      },
      onBeforeStart: function() {
        this.slider.drag.detach();
      }.bind(this),
      onDrag: function() {
        this.meeting_slots = Math.round(this.meeting.offsetWidth/this.slider.stepWidth);
        this.resizeHandler.setStyles({
          'margin-left' : this.meeting.offsetWidth-this.resizeHandler.offsetWidth+'px'});
        this.duration = this.meeting_slots/this.unit;
        this.changeStatus(this.isBusy(this.currentPosition+this.meeting_slots-1))
        this.displayMeetingInfo();
      }.bind(this),
      onComplete:function() {
        $('duration').value = this.duration*3600;
        var ts = this.ts[this.currentPosition] * 1000;
        var date_begin = new Obm.DateTime(ts);
        $('date_begin').value = date_begin.format('c');
        this.slider.drag.attach();
      }.bind(this)     
    };
    this.meeting.makeResizable(resizeOptions);

    this.meeting.setStyles({
      'top': '-'+this.container.offsetHeight+'px',
      'height': this.container.offsetHeight+'px',
      'width':this.slider.stepWidth*this.meeting_slots+'px'
    });
    this.meeting.setOpacity(.5);

    this.resizeHandler.setStyles({
      'margin-left' : this.meeting.offsetWidth-this.resizeHandler.offsetWidth+'px',
      'height': this.container.offsetHeight+'px'
    });

    this.scrollDiv.setStyle('height',this.table.offsetHeight+20+'px');

    // Is the first time_slot busy ?
    if(this.isBusy(this.meeting_slots)) {
      this.meeting.addClass('meetingBusy');        
    } 

    // Auto Scroll
    this.autoScroll = new Fx.Scroll(this.scrollDiv, {
      offset: {'x':-this.oneDayWidth/2, 'y':-$('calendarHead').offsetHeight} // Offset y : for ie7 !
    });

    // Navigation Scroll
    this.scrollLeft.setStyle('height', this.table.offsetHeight+'px');
    this.scrollLeft.setStyle('line-height', this.table.offsetHeight+'px');
    this.scrollRight.setStyle('line-height', this.table.offsetHeight+'px');

    this.scroll = new Fx.Scroll(this.scrollDiv);

    this.scrollLeft.addEvent('click', function() {
      this.scroll.start(this.scrollDiv.scrollLeft-this.oneDayWidth/2, 0); // - 1/2 day
    }.bind(this));

    this.scrollRight.addEvent('click', function() {
      this.scroll.start(this.scrollDiv.scrollLeft+this.oneDayWidth/2, 0); // + 1/2 day
    }.bind(this));

    this.initPosition();
  },

  /*
   * Display meeting start and end date
   */
  displayMeetingInfo: function() {
    var date_begin_ts = this.ts[this.currentPosition]*1000;
    var date_begin = new Obm.DateTime(date_begin_ts);
    $('meeting_start').innerHTML = date_begin.format(obm.vars.regexp.dateFormat + ' h:i');
    var date_end = new Obm.DateTime(date_begin_ts+(this.meeting_slots/this.unit)*3600*1000);
    $('meeting_end').innerHTML = date_end.format(obm.vars.regexp.dateFormat + ' h:i');
  },

  /*
   * Check if selected slot is busy or not
   */
  isBusy: function(end_pos) { // TODO: test if same day
    for(var i=this.currentPosition;i<=end_pos;i++) {
      var index = this.bts.indexOf(i+'');
      if (index != -1) {
        // $('submitMeeting').setProperty('disabled', 'disabled');
        return true;
      }
    }
    // $('submitMeeting').setProperty('disabled', ''); 
    return false;
  },

  /*
   * Set meeting style
   */
  changeStatus: function(isBusy) {
    if(isBusy) {
      this.meeting.addClass('meetingBusy');        
    } else {
      this.meeting.removeClass('meetingBusy');        
    }
  },

  /*
   * Go to next free slot
   */
  autoPickNext: function() {
    var initialPosition = this.currentPosition;
    do {
      this.currentPosition++;
      var end_pos = this.currentPosition + this.meeting_slots -1;
    } while(this.isBusy(end_pos) && end_pos != this.nbSteps);
    if (end_pos == this.nbSteps) {
      this.currentPosition = initialPosition;
    } else {
      this.slider.set(this.currentPosition);
      this.autoScroll.toElement(this.meeting);
      this.forcePosition();
    }
  },

  /*
   * Go to previous free slot
   */
  autoPickPrev: function() {
    var initialPosition = this.currentPosition;
    do {
      this.currentPosition--;
      var end_pos = this.currentPosition + this.meeting_slots -1;
    } while(this.isBusy(end_pos) && this.currentPosition >= 0);
    if (this.currentPosition < 0) {
      this.currentPosition = initialPosition;
    } else {
      this.slider.set(this.currentPosition);
      this.autoScroll.toElement(this.meeting);
      this.forcePosition();
    }
  },

  /*
   * Initialize meeting position
   */
  initPosition: function() {
    this.currentPosition = this.ts.indexOf(''+this.meeting_date);
    this.slider.set(this.currentPosition);
    this.autoScroll.toElement(this.meeting);
  },

  /*
   * Force meeting position
   */
  forcePosition: function() {
    // this.meeting.setStyles({
    //   'top': '-'+this.container.offsetHeight+'px', // for ie7 !
    //   'left': this.currentPosition*this.stepSize+'px'
    // });
  }

});
