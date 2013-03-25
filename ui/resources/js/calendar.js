/******************************************************************************
 * Manager class
 *****************************************************************************/
Obm.CalendarManager = new Class({

  /**
   * Constructor
   */
  initialize: function(time, write) {
    this.redraw = new Hash();
    this.redrawAllDay = new Hash();
    this.events = new Hash();
    this.eventGrid = new Array();
    this.alldayEventGrid = new Array();
    this.maxHeight = 1;
    this.startTime = time;
    this.forceRedraw = false;
    this.lock = false;
    this.entityEvents = new Array();
    this.current = new Obm.DateTime(time*1000);
    var d  = new Obm.DateTime(obm.vars.consts.startTime);
    this.write = write;
    this.popupManager = new Obm.CalendarPopupManager(); 

    // Reset minical
    if(obm.miniCalendar) {
      if (obm.miniCalendar.currentMonth == d.getMonth()) {
        obm.miniCalendar.clearSelection();
      } else {
        obm.miniCalendar.draw(d.getFullYear(), d.getMonth());
      }
    }

    if (obm.vars.consts.calendarRange != 'month') {
      this.defaultHeight = $('calendarBody').getElement('div').offsetHeight;    
      for(i=0;i<obm.vars.consts.nbDisplayedDays;i++) {
        d.setTime(obm.vars.consts.startTime.getTime());
        d.setDate(obm.vars.consts.startTime.getDate() + i);
        var index = d.format('Y-m-d');
        this.eventGrid[index] = new Hash();
        if(obm.miniCalendar) {
          obm.miniCalendar.select(index);
        }
      }
    } else if (obm.miniCalendar) {
      obm.miniCalendar.draw(obm.vars.consts.currentMonth.getFullYear(), obm.vars.consts.currentMonth.getMonth());      
      for(i=0;i<obm.vars.consts.nbDisplayedDays;i++) {
        d.setTime(obm.vars.consts.startTime.getTime());
        d.setDate(obm.vars.consts.startTime.getDate() + i);
        obm.miniCalendar.select(d.format('Y-m-d'));
      }
    }

    if ($('todayHourMarker')) {
      // Set hourMarker initial position
      this.updateHourMarker();
      this.updateHourMarkerTimer = this.updateHourMarker.periodical(120000, this);
    }

    if (obm.vars.consts.calendarRange == 'month') {
      this.tips = new Obm.Tip(null, {fixed:true, click:true}, 'calTip'); 
      this.observer = new Obm.Observer($('calendarHeaderGrid'), {onStop:this.resizeAlldayContainer, property:'offsetHeight'});
      this.resizeGrid();
    } else {
      this.scroll = new Obm.Scroller($('calendarBody'), {area: this.defaultHeight, velocity: 1});
    }

    // set waiting events panel class
    if ($('waitingEventsContainer')) {
       $('waitingEventsContainer').set('class', 'waitingEventsContainer');
    }
  },


  /*
   * Post Initialize 
   */
  setObserver: function() {
    new Obm.Observer(window, {onStop:this.resizeGrid, property:'innerHeight'});
  },


  /**
   * Update hour marker (left panel & in-day) 
   */ 
  updateHourMarker: function() {
    var d = new Date();
    $('todayHourMarker').style.top = (d.getHours()*3600 + d.getMinutes()*60)/obm.vars.consts.timeUnit * this.defaultHeight + 'px';
  },

  getDaysCount: function(time, duration, allday) {
      if ( duration < 0 ) {
	return false;
      }
      if ( duration == 0 ) {
	return 1;
      }
      var firstDate = new Obm.DateTime(time);
      firstDate.setNoon();
      var endTime = time+duration;
      var lastDate =  new Obm.DateTime(endTime);
      lastDate.setNoon();
      
      /**
       * Non-allday events appear in the 'allday' area if they span multiple days.
       * In this case, they should appear as (<duration in days> + 1) to cover all days...
       * In case of allday events, they must appear as (<duration in days>), thus
       * the variable initialization below.
       */
      var days = allday ? 0 : 1;
      while ( lastDate.format("Y-m-d") != firstDate.format("Y-m-d") ) {
          days++;
          firstDate.setTime( (firstDate.getTime() + (24*60*60*1000) ) );
          firstDate.setNoon();
      }
      return days;
  },
  /* get number of days of all day events */
  _getSizeOfAllDayEvts: function(evt, begin) {
    var size = null;
    var allday = evt.event.all_day == 1;
    size = this.getDaysCount( (evt.event.time*1000), (evt.event.duration*1000), allday );
    if (size == 0) size = 1; // very, very crappy fix 

    // Extensions
    if (obm.vars.consts.calendarRange == 'month') {
      var weeks = obm.calendarManager.getEventWeeks(evt);
      var start = weeks[0];
      if (evt.event.left) {
	if ( evt.event.right ) {
	  size = 7;
	} else {
	  size = this.getDaysCount(begin, evt.event.duration*1000 - ( begin- (evt.event.time*1000) ), allday );
	  if (size == 0) size = 1; // very, very crappy fix 
	}
      } else if (evt.event.right) {
	try {
	  var startWeek = obm.vars.consts.weekTime[start][0] * 1000;
	} catch (e) {
	  var startWeek = obm.vars.consts.startTime.getTime();
	}
	var endWeekDate = new Obm.DateTime(startWeek);
	endWeekDate.addDays(7);
	var indexMs = evt.event.index*1000;
	
	// So you wonder why the (allday=)true below? Here's why:
	// The grid now considers that the week ends 7 days after its start, which
	// makes sense I guess :p The problem with non all-day events that spans multiple
	// days is that they will appear on 3 days and thus, go past the endof the grid.
	// This is caused by an edge case of the algorithm in the getDaysCount() method
	// but is worked around by simulating the behaviour of allday events.
	// Reading the getDaysCount() method code will also probably help to understand
	// the edge case and why we did it this way.
	size = this.getDaysCount(indexMs, (endWeekDate.getTime() - indexMs), true);
      }
    }
    return size;
  },
  
  _getBeginningOfAllDayEvt: function(evt) {
    var begin;
    if ( obm.vars.consts.calendarRange == 'month' && evt.event.left ) {
      begin = evt.event.index*1000;
    } else {
      begin = evt.event.time*1000;
    }
    return begin;
  },
  
  /**
   * Register an event
   */
  register: function(evt) {
    this.events.set(evt.element.id, evt);
    if (evt.kind == 'all_day') {
      var begin = this._getBeginningOfAllDayEvt(evt);
      var size = this._getSizeOfAllDayEvts(evt, begin);
      evt.size = size;
      var current = new Obm.DateTime(begin);
      for(var i=0;i<evt.size;i++) {
        current.setTime(begin);
        current.setDate(current.getDate()+ i);
        var index = current.format('Y-m-d');

        if($('allday_'+index)) {
          if(!this.alldayEventGrid[index]) {
            this.alldayEventGrid[index] = new Array();
          }
          this.alldayEventGrid[index].push(evt); 
          this.redrawAllDay.set(index, true);

          if (obm.vars.consts.calendarRange == 'month') {
            var more = $('more_'+index);
            var target = "";
            var title = "";
            if (obm.vars.consts.action == 'portlet') target = "target='_blank'";
            if (more) {
              if (evt.event.colors.event && evt.event.colors.event.body) color = evt.event.colors.event.body;
              if (evt.event.all_day) {
                if (evt.isExternal()) {
                  title = '<a '+target+' href='+obm.vars.consts.calendarDetailconsultExtURL+evt.event.id+'&contact_id='+evt.event.entity_id+'>'+evt.event.title+'</a>';
                } else {
                  title = '<a '+target+' href='+obm.vars.consts.calendarDetailconsultURL+evt.event.id+'>'+evt.event.title+'</a>';
                }
              } else {
                if (evt.isExternal()){
                  title = '<a '+target+' href='+obm.vars.consts.calendarDetailconsultExtURL+evt.event.id+'&contact_id='+evt.event.entity_id+'><b>'+evt.event.date.format(obm.vars.regexp.dispTimeFormat)+'</b> - '+evt.event.title+'</a>';
                } else {
                  title = '<a '+target+' href='+obm.vars.consts.calendarDetailconsultURL+evt.event.id+'><b>'+evt.event.date.format(obm.vars.regexp.dispTimeFormat)+'</b> - '+evt.event.title+'</a>';
                }
              }
              color = "#fff";
              klass='class="moreEvent '+evt.event.klass+'"';
              style = 'style="background:'+evt.event.colors.event+'; color:'+color+'" '+klass ;
              more.set('title', more.get('title')+'<div '+style+'>'+ title+'</div>');
            }
          }

        }
      }

      if (this.forceRedraw) {
        this.redrawAllDayGrid();
        this.resizeGrid();
      }

    } else {
      var index = new Obm.DateTime(evt.event.time * 1000).format('Y-m-d');
      var begin = evt.element.offsetTop.toFloat();
      var height = obm.calendarManager.defaultHeight;
      if (evt.event.duration > obm.vars.consts.timeUnit) {
        height = evt.event.duration/obm.vars.consts.timeUnit * obm.calendarManager.defaultHeight;
      }
      var end = begin + height;
      for(var i=begin;i<end;i++) {
        if(!this.eventGrid[index].get(i)) {
          this.eventGrid[index].set(i,new Array());
        }
        this.eventGrid[index].get(i).push(evt);
      }
      this.redraw.set(index, true);
      if (this.forceRedraw) {
        this.redrawGrid();
      }
    }
    this.oldEvent = null;

    if (!this.entityEvents[evt.event.entity+'-'+evt.event.entity_id]) {
      this.entityEvents[evt.event.entity+'-'+evt.event.entity_id] = new Array();
    }
    this.entityEvents[evt.event.entity+'-'+evt.event.entity_id].push(evt.content);
  },


  /**
   * Unregister an event
   */
  unregister: function(evt) {
    this.oldEvent = $merge(new Object(), evt);
    this.oldEvent.elementId = evt.element.id;
    this.oldEvent.event.date = new Obm.DateTime(evt.event.time*1000);
    this.oldEvent.time = evt.event.time;
    this.oldEvent.event.duration = evt.event.duration;
    if (evt.kind == 'all_day') {
      var current = new Obm.DateTime(evt.event.time*1000);
      var beginDay = current.getTime();
      for(var i=0;i<evt.size;i++) {
        current.setTime(beginDay);
        current.setDate(current.getDate()+ i);
        var index = current.format('Y-m-d');
        this.redrawAllDay.set(index, true);
        if($('allday_'+index)) {
          this.alldayEventGrid[index].each(function(e) {
            if (evt.event.id == e.event.id) {
              this.alldayEventGrid[index].erase(e);
            }
          }.bind(this));
        }
      }
      this.redrawAllDayGrid();
      this.resizeGrid();
    } else {
      var day = evt.event.date.format('Y-m-d');
      var begin = evt.element.offsetTop.toFloat();
      var height = obm.calendarManager.defaultHeight;
      if (evt.event.duration > obm.vars.consts.timeUnit) {
        height = evt.event.duration/obm.vars.consts.timeUnit * obm.calendarManager.defaultHeight;
      }
      var end = begin + height;
      for(var i=begin;i<end;i++) {
        try{
        this.eventGrid[day].get(i).erase(evt);
        } catch(e) {}
      }
      this.redraw.set(day, true);
      this.redrawGrid();
    }
  },


  /*
   * Set custom class
   */
  setEventsClass: function(entity, id, klass) {
    if (entity == 'user' && id.toInt() == obm.vars.consts.userId) {
      obm.vars.consts.userStyle = klass;
    }
    if (this.entityEvents[entity+'-'+id]) {
      this.entityEvents[entity+'-'+id].each(function(e) {
        e.set('class',klass);
      });
    }
  },


  /**
   * Add an all day event
   */
  newDayEvent: function(eventData,options) {
    eventData = this.decodeJSONfields(eventData);
    obmEvent = new Obm.CalendarAllDayEvent(eventData,options);
    this.register(obmEvent);
    return obmEvent;
  },


  /**
   * Add an in-day event
   */
  newEvent: function(eventData,options) {
    eventData = this.decodeJSONfields(eventData);
    var obmEvent = new Obm.CalendarInDayEvent(eventData,options);
    this.register(obmEvent);
    return obmEvent;
  },

  /**
   * Because we generate JSON from the server, and include it into the page,
   * we have to encode html special chars such as the <script> tags
   * This method takes care of decoding the appropriate fields
   */
  decodeJSONfields: function(eventData) {
    eventData.title = Obm.utils.decodeSpecialChars(eventData.title);
    if ( eventData.location ) {
      eventData.location = Obm.utils.decodeSpecialChars(eventData.location);
    }
    if ( eventData.description ) {
      eventData.description = Obm.utils.decodeSpecialChars(eventData.description);
    }
    return eventData;
  },
  

  /*
   * Create in-day dummy event (for event creation)
   */
  newDummyEvent: function(evt) {

    this.destroyDummy();

    var ivent = new Event(evt);
    var target = ivent.target;
    target = $(target);
    if (target.get('tag') == 'a') {
      return false;
    }
    while(target.id == '') {
      target = $(target.parentNode);
    }
    var str = target.id.split('_');
    if (str.length <= 1) {
      return false;
    }
    /* Crappy ie fix*/
    var x = ivent.event.layerX;
    if (!x) x =ivent.event.offsetX;
    if (evt.type == 'mousedown' && str[0] == 'time' && obm.calendarManager.write == 1) {
      // dummy event data
      var eventData = new Object();
      eventData.time = obm.calendarManager.startTime + str[1].toInt() 
       + Math.floor(x/($('calendarGrid').offsetWidth/100*obm.vars.consts.cellWidth.toInt()))*86400;
      eventData.id = 'dummy';
      eventData.entity = 'dummy';
      eventData.entity_id = 'dummy';
      eventData.duration = 3600/obm.vars.consts.timeUnit;
      eventData.title = obm.vars.labels.newEvent;
      eventData.location = '';
      eventData.klass = obm.vars.consts.userStyle;
      eventData.updatable = true;
      eventData.colors = new Object();
      eventData.colors.event = new Object();
      eventData.colors.event.body = '';
      eventData.colors.event.header = '';

      // set options
      var options = new Object();
      options.updatable = true;

      // dummy event
      this.dummy = new Obm.CalendarInDayEvent(eventData,options);
      this.dummy.element.id = 'dummy';
      this.dummy.updatePosition(1, 0, 1, this.dummy.event.date.format('Y-m-d'));
      this.dummy.element.style.height = 0; // needed for dblclick
      this.dummy.resize.removeEvents('complete');
      this.dummy.resize.addEvent('complete', function() {
        var duration = obm.calendarManager.dummy.event.duration;
        var allday = 0;
        if (duration == 86400) {
          duration = 3600;
          allday = 1;
        }
        obm.calendarManager.scroll.stop();
        obm.calendarQuickForm.setDefaultFormValues(eventData.time, allday, duration);
        obm.calendarQuickForm.show();    
        obm.calendarQuickForm.form.tf_title.focus();
      });
      this.dummy.resizeHandler.fireEvent('mousedown', ivent);
    }
    return false;
  },


  /*
   * get dummy event data 
   */
  dummyPrepare: function(evt) {
    var ivent = new Event(evt);
    var target = ivent.target;
    target = $(target);
    if (target.get('tag') == 'a') {
      return false;
    }
    while(target.id == '') {
      target = $(target.parentNode);
    }
    var div = target.id.split('_');
    if (div.length <= 1) {
      return false;
    }
    if (div[0] == 'dayMonthLabel') {
      div = $(div.join('_')).parentNode.id.split('_');
    }
    if (div[0] == 'more') {
      div = $(div.join('_')).parentNode.parentNode.id.split('_');
    }
    return div;
  },


  /*
   * Set allday dummy event selection
   */
  newAlldayDummyEventHighlight: function(evt) {
    if (this.dummy) {
      var div = this.dummyPrepare(evt);
      if (div[0] == 'dayContainer') {
        $$('div.selection').each(function(e) {
          $(e).removeClass('selection');
        });
        if(this.dummy.downCell != undefined){
          var start = Math.min(div[1], this.dummy.downCell[1]);
          var end = Math.max(div[1], this.dummy.downCell[1]);
          for(var i=start;i<=end;i++) {
            if ($(obm.calendarManager.dayContainers[i])) {
              $(obm.calendarManager.dayContainers[i]).addClass('selection');
            }
          }
        }
      }
    }
  },


  /*
   * Create allday dummy event (for event creation)
   */
  newAlldayDummyEvent: function(evt) {
    var div = this.dummyPrepare(evt);
    if (evt.type == 'mousedown' && (div[0] == 'dayContainer' || div[0] == 'dayMonthLabel' ||  div[0] == 'more' ) && obm.calendarManager.write == 1) {
      this.dummy = new Object();
      this.dummy.down = obm.calendarManager.startTime + div[1].toInt()*86400;
      this.dummy.downCell = div;
    } else if (evt.type == 'mouseup' && (div[0] == 'dayContainer' || div[0] == 'dayMonthLabel' ||  div[0] == 'more' ) && obm.calendarManager.write == 1 && this.dummy) {
      this.dummy.up = obm.calendarManager.startTime + div[1].toInt()*86400;
      var begin = Math.min(this.dummy.down, this.dummy.up);
      var end = Math.max(this.dummy.down, this.dummy.up);
      this.dummy = null;
      $$('div.alldayContainer').each(function(e) {
        e.removeClass('selection');
      });
      if (begin != end) {
        obm.calendarQuickForm.setDefaultFormValues(begin, 1, end-begin);
        obm.calendarQuickForm.show();    
        obm.calendarQuickForm.form.tf_title.focus();
      }
    }
  },


  /*
   * Destroy temp dummy div
   */
  destroyDummy: function() {
    this.dummy = null;
    try {
      $('dummy').destroy();
    } catch(e) {}
    $$('div.alldayContainer').each(function(e) {
      e.removeClass('selection');
    });
  },


  /**
   * Resize viewport
   */
  resizeGrid: function() {
    if (obm.vars.consts.calendarRange != 'month') {
      $('calendarBody').setStyle('height',window.getHeight() - $('calendarBody').offsetTop - 40);
      // *************************************** 
      // IE6 CRAPPY FIX, height:100% => doesn't work
      if (Browser.Engine.trident) {
        var height = $('calendarGrid').getHeight();
        $$('div.dayCol').each(function(e) {
          e.setStyle('height', height+'px');
        });
      }
      // ***************************************
    } else {
      $('mainContent').setStyle('height',window.getHeight() - $('mainContent').offsetTop -20);
      $('calendarHeaderGrid').setStyle('height',window.getHeight() - $('calendarHeader').offsetTop - 40);
      // *************************************** 
      // IE6 CRAPPY FIX (one more time), height:100% => doesn't work
      if (Browser.Engine.trident) {
        var height = $('calendarHeaderGrid').getHeight() / 100* $$('div.monthRow')[0].style.height.toFloat();
        $$('div.alldayContainer').each(function(e) {
          e.setStyle('height', height+'px');
        });
      }
      // ***************************************
      
    }
  },


  /*
   * Scroll to user first hour param
   */
  scrollToFirstHour: function() {
    $('calendarBody').scrollTo(0, obm.vars.consts.firstHour*this.defaultHeight*(3600/obm.vars.consts.timeUnit));
  },


  /**
   * Ajust displayd events on month view // FIXME
   */
  resizeAlldayContainer: function() {
    $$('div.alldayContainer').each(function(element) {
      var str = element.id.split('_');
      var canBeDisplayed = Math.floor(element.offsetHeight/15) - 2;

      if (obm.calendarManager.alldayEventGrid[str[2]] && 
          obm.calendarManager.alldayEventGrid[str[2]].length>canBeDisplayed) {
        var undisplayed = obm.calendarManager.alldayEventGrid[str[2]].length - canBeDisplayed;
        var i = 0;
        obm.calendarManager.alldayEventGrid[str[2]].each(function(e) {
          if (i<canBeDisplayed) {
            e.element.style.display = '';
          } else {
            e.element.style.display = 'none';
          }
          i++;
        });
        var more = $('more_'+str[2]);
        more.style.top = canBeDisplayed*15+'px';
        more.style.display = '';
        more.set('html','+'+undisplayed+' '+obm.vars.labels.more);
        obm.calendarManager.tips.add(more);
      } else {
        $('more_'+str[2]).style.display = 'none';
        if (obm.calendarManager.alldayEventGrid[str[2]]) {
          obm.calendarManager.alldayEventGrid[str[2]].each(function(e) {
            e.element.style.display = '';
          });
        }
      }
    });
    obm.calendarManager.redrawAllDayGrid();
  },


  /**
   * Redraw events grid (conflicts)
   * algo powered by MRA.
   */ 
  redrawGrid: function() {
    var updated = new Hash();
    this.redraw.each (function(redraw, columnIndex){
      if(!redraw) return;
      var unit;
      this.eventGrid[columnIndex].each(function(cell, key) {
        cell.sort(function(evt1, evt2) {
          var diff = evt1.event.time - evt2.event.time;
          if(diff != 0) return diff;
          diff = evt1.event.id - evt2.event.id;
          if(diff != 0) return diff;
          return evt1.event.entity_id - evt2.event.entity_id;      
        });

        var usedPositions = new Hash();
        var position = 0;

        cell.each(function (evt, index) {
          var updatedId = evt.element.uid;
          var coords;
          if(!(coords = updated.get(updatedId))) {
            if(usedPositions.getLength() == 0) {
              unit = {'value' : 1};
            }
            while(usedPositions.get(position)) {
              position ++;
            }
            var end = {'value' : position};
            if((index + 1) == cell.length) {
              while(end.value < unit.value && !usedPositions.get(end.value)) {
                end.value++;
              }
            }
            if(end.value == unit.value) {
              end = unit;
            }
            coords = {'position': position, 'unit': unit, 'end': end, 'column': columnIndex, 'occurrence': evt};
            updated.set(updatedId, coords);
          } 
          usedPositions.set(coords.position, true);        
          if(cell.length > unit.value) {
            unit.value = cell.length;
          }
          //FIXME : if new event have position < current event, and end != unit this must not occur
          if((coords.position + 1) < cell.length && (index + 1) < cell.length) {
            coords.end = {'value' : coords.position + 1};
          }
        });

      });
    }.bind(this));

    this.redraw = new Hash();

    updated.each(function(coords, key) {
        var size = coords.end.value - coords.position;
        coords.occurrence.updatePosition(coords.unit.value, coords.position, size, coords.column);
    }.bind(this));

  },


  /**
   * Redraw all-day grid (top posttion & size)
   */
  redrawAllDayGrid: function() {
    var updated = new Array();
    var usedPosition = new Array(); 
    var passed = new Hash(); 

    this.redrawAllDay.each (function(redraw, columnIndex) {
      if (this.alldayEventGrid[columnIndex]) {
        this.alldayEventGrid[columnIndex].each(function(evt) {

          var passedId = evt.element.uid;
          if (!passed.get(passedId)) {

            var end = new Obm.DateTime((evt.event.time+evt.event.duration)*1000);
            var begin = evt.event.date.getTime(); 
            var current = new Obm.DateTime(evt.event.time*1000);

            // Extensions
            if (obm.vars.consts.calendarRange == 'month') {
              var weeks = obm.calendarManager.getEventWeeks(evt);
              var start = weeks[0];

              if (evt.event.periodic){
                evt.event.location = obm.vars.consts.calendarDetailconsultURL+evt.event.id;
                evt.linkContainer.addEvent('click', function(e){
                  e.preventDefault();
                  obm.calendarOccurenceEditPopup.compute(evt.event, evt.event.location);
                }.bind(evt));
              }

              if (evt.event.left) {
                oldBegin = begin
                begin = evt.event.index*1000;
                var current = new Obm.DateTime(begin);
                columnIndex = current.format('Y-m-d');
                evt.leftExtension.setStyle('display', '');
              }

              if (evt.event.right) {
                evt.rightExtension.setStyle('display', '');
              }

            } else {
              if (begin < obm.calendarManager.startTime*1000) {
                begin = obm.calendarManager.startTime*1000; 
                current = new Obm.DateTime(begin);
                columnIndex = current.format('Y-m-d');
                evt.size = Math.ceil((end.getTime() - begin)/86400000);
                evt.leftExtension.setStyle('display', '');
              }
              if ((evt.event.date+evt.event.duration*1000) > obm.calendarManager.startTime*1000 + (86400000 * obm.vars.consts.nbDisplayedDays)) {
                evt.size = Math.ceil((obm.calendarManager.startTime*1000 + (86400000 * obm.vars.consts.nbDisplayedDays) - begin)/86400000);
                evt.rightExtension.setStyle('display', '');
              }

            }

            var coords = {'position': position, 'size': evt.size,  'column': columnIndex, 'occurrence': evt};
            var currentTime = current.getTime();

            for(var i=0;i<evt.size;i++) {
              current.setTime(currentTime);
              current.setDate(current.getDate()+i);
              var index = current.format('Y-m-d');
              if (!updated[index]) {
                updated.push(index);
                updated[index] = new Array();
              }
              updated[index].push(coords);
            }

            updated[columnIndex].each(function(e) {
              if (!usedPosition[columnIndex]) {
                usedPosition.push(columnIndex);
                usedPosition[columnIndex] = new Hash();
              }
              usedPosition[columnIndex].set(e.position, true);
            });

            var position = 0;
            while(usedPosition[columnIndex].get(position)) {
              position++;
            }

            coords.position = position;
            passed.set(passedId, true);
          }

        });
      }
    }.bind(this));

    updated.each(function(col, i) {
      updated[col].each(function(coords) {
        this.maxHeight = Math.max(this.maxHeight, coords.position);
        coords.occurrence.updatePosition(coords.position, coords.size, coords.column);
      }.bind(this));
    }.bind(this));

    this.updateHeight(this.maxHeight);
  },


  /**
   * Update calendarHeader height
   * (all day event)
   */
  updateHeight: function(size) {
    if (obm.vars.consts.calendarRange != 'month') {
      $$('div.alldayContainer').each(function(element, index) {
        element.style.height = 14*(size+2)+'px';
      });
      $('calendarHeaderGrid').style.height = 14*(size+2)+'px'; // FIXME
      this.maxHeight=1;
    }
  },


  /**
   *  Show last month events
   */
  prevMonth: function() {
   obm.vars.consts.currentMonth.setMonth(obm.vars.consts.currentMonth.getMonth() - 1)
   this.current.setTime(obm.vars.consts.currentMonth.getTime());
   this.refresh();
  },


  /**
   *  Show last week events
   */
  prevWeek: function() {
   this.current.setDate(this.current.getDate()-7);
   this.refresh();
  },


  /**
   *  Show last day events
   */
  prevDay: function() {
   this.current.setDate(this.current.getDate()-1);
   this.refresh();
  },


  /**
   *  Show next month events
   */
  nextMonth: function() {
   obm.vars.consts.currentMonth.setMonth(obm.vars.consts.currentMonth.getMonth() + 1)
   this.current.setTime(obm.vars.consts.currentMonth.getTime());
   this.refresh();
  },


  /**
   * Show next week events
   */
  nextWeek: function() {
   this.current.setDate(this.current.getDate()+7);
   this.refresh();
  },


  /**
   * Show next day events
   */
  nextDay: function() {
   this.current.setDate(this.current.getDate()+1);
   this.refresh();
  },


  /**
   * Show previous custom view
   */
  prevCustom: function() {
   this.current.setDate(this.current.getDate()-obm.vars.consts.nbDisplayedDays.toInt());
   this.refresh();
  },


  /**
   * Show next custom view
   */
  nextCustom: function() {
   this.current.setDate(this.current.getDate()+obm.vars.consts.nbDisplayedDays.toInt());
   this.refresh();
  },


  /**
   * Show day view
   */
  showDay: function(day) {
   if (day) {
     this.current = new Obm.DateTime(day*1000);
   } else {
     this.current = null;
   }
   obm.vars.consts.calendarRange = 'day';
   obm.vars.consts.nbDisplayedDays =  1;
   this.refresh();
  },


  /**
   * Show week view
   */
  showWeek: function(day) {
   if (day) {
     this.current = new Obm.DateTime(day*1000);
   } else {
     this.current = null;
   }
   obm.vars.consts.calendarRange = 'week';
   obm.vars.consts.nbDisplayedDays =  obm.vars.consts.daysByWeek;
   this.refresh();
  },


  /**
   * Show month view
   */
  showMonth: function(t) {
   if (t) {
     this.current = new Obm.DateTime(t);
   } else {
     this.current = null;
   }
   obm.vars.consts.nbDisplayedDays = 30;
   obm.vars.consts.calendarRange = 'month';
   this.refresh();
  },


  /**
   * Show previous range
   */
  showPrev: function() {
    if ( obm.vars.consts.calendarRange == "month" ) {
      this.prevMonth();
    } else if ( obm.vars.consts.calendarRange == "week" ) {
      this.prevWeek();
    } else if ( obm.vars.consts.calendarRange == "day" ) {
      this.prevDay();
    } else if ( obm.vars.consts.calendarRange == "custom" ) {
      this.prevCustom();
    } else if ( console && "log" in console ) {
      console.log("calendar.js showPrev: unknown calendarRange ",obm.vars.consts.calendarRange);
    }
  },


  /**
   * Show next range
   */
  showNext: function() {
    if ( obm.vars.consts.calendarRange == "month" ) {
      this.nextMonth();
    } else if ( obm.vars.consts.calendarRange == "week" ) {
      this.nextWeek();
    } else if ( obm.vars.consts.calendarRange == "day" ) {
      this.nextDay();
    } else if ( obm.vars.consts.calendarRange == "custom" ) {
      this.nextCustom();
    } else if ( console && "log" in console ) {
      console.log("calendar.js showNext: unknown calendarRange ",obm.vars.consts.calendarRange);
    }
  },


  /**
   * Show today ^^
   */
  showToday: function() {
    this.current = new Obm.DateTime(new Date());
    this.refresh();
  },


  /**
   * Reload Calendar
   */
  reloadCalendar: function() {
    if(obm.vars.consts.calendarRange == "month") this.current = obm.vars.consts.currentMonth;
    this.refresh();
  },


  /*
   * Back to my view
   * Remove selected entities
   * TODO: HTML, Clear displayed calendars panel
   */
  backToMyView: function() {
    $('group_name').value = '';
    var data = new Object();
    data.new_sel = 1;
    data.new_group = 1;
    data.sel_category_filter = '';
    data.group_view = '_ALL_';
    data.sel_user_id =  new Array();
    data.date = this.current.format('c');
    new Request.HTML({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      evalScripts : true,
      update: $('calendarLayout'),
      onRequest: function() {
        $('spinner').show();
      },
      onComplete: function() {
        $('spinner').hide();
      },
      onFailure: function (response) {
        Obm.Error.parseStatus(this);
      }
    }).get($merge({ajax : 1, action : 'draw'}, data));    
  },


  /*
   * Get event begin and end weeks
   */
  getEventWeeks: function(evt) {
    var begin = evt.event.time;
    var end = evt.event.time + evt.event.duration;
    var startWeek=0;
    var endWeek=0;
    obm.vars.consts.weekTime.each(function(value, key) {
      if (value) {
        if (value[0] <= begin && begin < value[1]) startWeek = key;
        if (value[0] < end && end <= value[1]) endWeek = key;
      }
    });
    return new Array(startWeek, endWeek);
  },


  /**
   * Refresh the calendar grid
   */
  refresh: function() {
    if (this.observer) this.observer.timer = $clear(this.observer.timer);
    this.updateHourMarkerTimer = $clear(this.updateHourMarkerTimer);
    obm.vars.consts.selectedDays = new Array();
    var data = new Object();
    if (obm.calendarManager.customStart) {
      var min = Math.min(obm.calendarManager.customStart*1000, this.current.getTime());
      var max = Math.max(obm.calendarManager.customStart*1000, this.current.getTime());
      data.date = new Obm.DateTime(min).format('c');
      data.ndays = Math.ceil((max - min)/86400000)+1;
      data.cal_range = 'custom';
      obm.vars.consts.nbDisplayedDays = data.ndays;
    } else {
      if (this.current)	data.date = this.current.format('c');
      data.cal_range = obm.vars.consts.calendarRange;
      data.ndays = obm.vars.consts.nbDisplayedDays;
    }
	
    new Request.HTML({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      evalScripts : true,
      async: false,
      update: $('calendarLayout'),
      onRequest: function() {
        $('spinner').show();
      },
      onComplete: function() {
        $('spinner').hide();
      },
      onFailure: function (response) {
        Obm.Error.parseStatus(this);
      }
    }).get($merge({ajax : 1, action : 'draw'}, data));          
  },


  /*
   * Build legend bar and inject it after minicalendar
   */
  setLegendBar: function(content) {
    try{
      $('obmLegend').destroy();
    } catch(e) {}
    if (content != '') {
      var div = new Element('div').addClass('portlet').setProperty('id', 'obmLegend');
      div.innerHTML = content;
      div.injectAfter($('obmMiniCalendar'));
    }
  },


  /**
   * Create an object compatible with OBM server
   */
  prepareEventForUpdate: function(evt) {
    var eventData = new Object();
    eventData.eventdate = evt.event.date.format('c');
    eventData.calendar_id = evt.event.id;
    eventData.element_id = evt.element.id;
    eventData.date_begin = evt.event.date.format('c');
    eventData.old_date_begin = eventData.date_begin;
    if (obm.calendarManager.oldEvent) eventData.old_date_begin = obm.calendarManager.oldEvent.event.date.format('c');
    eventData.duration = evt.event.duration;
    eventData.title = evt.event.title;
    eventData.all_day = evt.event.all_day;
    eventData.periodic = evt.event.periodic;
    eventData.opacity = evt.event.opacity;
    return eventData;
  },

  //
  // TODO: REWRITE AJAX CALLS
  //


  // **************************************************************************
  // CREATE 
  // **************************************************************************

  /**
   * Create the event on OBM server 
   */
  sendCreateEvent: function(eventData) {
    new Request.JSON({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      onFailure: function (response) {
        Obm.Error.parseStatus(this);
      },
      onSuccess : function(response) {
        if (response.conflict) {
          $('popup_force').value = obm.vars.labels.insert_force;
          obm.calendarManager.popupManager.add('calendarConflictPopup');
        }
        if (response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
        obm.calendarManager.popupManager.addEvent('conflict', function() {
          eventData = $merge({action : 'insert'}, eventData);  
          if(!eventData.duration) eventData.duration = 3600;
          window.location=obm.vars.consts.calendarUrl + '?' +Hash.toQueryString(eventData)+'&repeat_kind=none&sel_user_id[]=data-user-'+eventData.entity_id;
        });
        obm.calendarManager.popupManager.addEvent('mail', function () {
          eventData.send_mail = true;
        });        
        obm.calendarManager.popupManager.addEvent('complete', function () {
          new Request.JSON({
            url: obm.vars.consts.calendarUrl,
            secure : false,
            onComplete : this.receiveCreateEvent
          }).post($merge({ajax : 1, action : 'quick_insert'}, eventData));          
        }.bind(this));
        obm.calendarManager.popupManager.show(eventData);
      }.bind(this)
    }).post($merge({ajax : 1, action : 'check_conflict'}, eventData));

  },


  /**
   * Create the event on OBM server 
   */
  receiveCreateEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = obm.vars.labels.fatalServerErr;
    }     
    if (response.error == 0) {
      showOkMessage(response.message);
      response.event.each(function(e) {
        eval(e);
      });
      obm.calendarManager.eventIndexing(response.id);
      obm.calendarManager.destroyDummy();
    } else {
      showErrorMessage(response.message);
    }

  }, 



  // **************************************************************************
  // UPDATE
  // **************************************************************************

  /**
   * Update the recurring event or the occurrency on OBM server 
   */
  sendDetailUpdate: function(evt) {
	    var eventData = this.prepareEventForUpdate(evt);
	    action_cmd = 'check_update';
	    
	    new Request.JSON({
	      url: obm.vars.consts.calendarUrl,
	      secure : false,
	      onFailure: function (response) {
	        Obm.Error.parseStatus(this);
	      },
	      onSuccess : function(response) {
	        if(response.occUpdate) {
	          obm.calendarManager.popupManager.add('calendarOccurencyUpdate');
	        }
	        obm.calendarManager.popupManager.addEvent('update_all', function () {
	            eventData.all = 1;
	        });
	        obm.calendarManager.popupManager.addEvent('update_one', function () {
	          var href = window.location.href;
	          var argLocation = href.indexOf("?");
	          var GETdetailUpdateForOccurrence = '?action=detailupdate&date_edit_occurrence='+encodeURIComponent(eventData.eventdate) + "&calendar_id="+ encodeURIComponent(eventData.calendar_id);
	          if(argLocation != -1) {
	            window.location = href.substr(0, argLocation)+GETdetailUpdateForOccurrence;
	          } else {
			    window.location = window.location+GETdetailUpdateForOccurrence;
	          }
	        });
	        obm.calendarManager.popupManager.addEvent('complete', function () {
	            obm.calendarManager.DetailUpdateRequest(eventData);
	        }.bind(this));	       
	      obm.calendarManager.popupManager.show(eventData);
	      }.bind(this)
	    }).post($merge({ajax : 1, action : action_cmd}, eventData));
	  },

  DetailUpdateRequest: function(eventData) {
      var action = eventData.specialAction ? eventData.specialAction : 'quick_update';
	  new Request.JSON({
	      url: obm.vars.consts.calendarUrl,
	      secure : false,
	      onComplete : this.goToDetailUpdateURI.bind(this)
	  }).post($merge({ajax : 1, action : action}, eventData));
  },
	  
  goToDetailUpdateURI: function(response) {
	    try {
	        var resp = eval(response);
	      } catch (e) {
	        resp = new Object();
	        resp.error = 1;
	        resp.message = obm.vars.labels.fatalServerErr;
	      }
	      if (response.error == 0) {
	        obm.calendarManager.eventIndexing(response.eventId);
	      } else {
	        showErrorMessage(response.message);
	        obm.calendarManager.cancel(obm.calendarManager.oldEvent.elementId);
	      }
	      obm.calendarManager.lock = false;
	      obm.calendarQuickForm.goTo('detailupdate&calendar_id=' + response.eventId);
  },
 
  /**
   * Update the event on OBM server after a drag'n'drop
   */
  sendUpdateEvent: function(evt, forceUpdate) {
    var eventData = this.prepareEventForUpdate(evt);
    var action_cmd = 'check_conflict';
    if (forceUpdate) {
      action_cmd = 'check_update';
    }
    new Request.JSON({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      onFailure: function (response) {
        Obm.Error.parseStatus(this);
      },
      onSuccess : function(response) {
        if(response.occUpdate) {
          obm.calendarManager.popupManager.add('calendarOccurencyUpdate');
        }
        if (response.conflict) {
          $('popup_force').value = obm.vars.labels.conflict_force;
          obm.calendarManager.popupManager.add('calendarConflictPopup');
        }
        if (response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
        obm.calendarManager.popupManager.addEvent('update_all', function () {
          eventData.all = 1;
        });
        obm.calendarManager.popupManager.addEvent('update_one', function () {
          this.chain.callChain();
        });
        obm.calendarManager.popupManager.addEvent('mail', function () {
          eventData.send_mail = true;
        });        
        obm.calendarManager.popupManager.addEvent('conflict', function() {
          eventData = $merge({action : 'conflict_manager'}, eventData)
          window.location=obm.vars.consts.calendarUrl+'?'+Hash.toQueryString(eventData);
        });
        obm.calendarManager.popupManager.addEvent('complete', function () {
          obm.calendarManager.updateRequest(eventData);
        }.bind(this));
        obm.calendarManager.popupManager.show(eventData);
      }.bind(this)
    }).post($merge({ajax : 1, action : action_cmd}, eventData));
  },


  /**
   * Update request
   */
  updateRequest: function(eventData) {
    new Request.JSON({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      onComplete : this.receiveUpdateEvent.bind(this)
    }).post($merge({ajax : 1, action : 'quick_update'}, eventData));
  },


  /**
   * Update the event on OBM server // FIXME 
   */
  receiveUpdateEvent: function(response) {
    try {
      var resp = eval(response);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = obm.vars.labels.fatalServerErr;
    }
    if (response.error == 0) {
      if(obm.vars.consts.calendarRange == "month") this.current = obm.vars.consts.currentMonth;
      this.refresh();
      obm.calendarManager.eventIndexing(response.eventId);
      showOkMessage(response.message);

    } else {
      showErrorMessage(response.message);
      obm.calendarManager.cancel(obm.calendarManager.oldEvent.elementId);
    }
    obm.calendarManager.lock = false;
  },


  // **************************************************************************
  // DELETE
  // **************************************************************************


  /**
   * Delete the event on OBM server 
   */
  sendDeleteEvent: function(eventData) {
    new Request.JSON({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      onFailure: function (response) {
        Obm.Error.parseStatus(this);
      },
      onSuccess : function(response) {
        if(response.checkDelete) {
          obm.calendarManager.popupManager.add('calendarConfirmDelete');
        }
        if(response.occDelete) {
          obm.calendarManager.popupManager.add('calendarOccurencyDelete');
        }
        if(response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
        obm.calendarManager.popupManager.addEvent('delete_all', function () {
          eventData.all = 1;
        });
        obm.calendarManager.popupManager.addEvent('mail', function () {
          eventData.send_mail = true;
        });
        obm.calendarManager.popupManager.addEvent('complete', function () {
          new Request.JSON({
            url: obm.vars.consts.calendarUrl,
            secure : false,
            onComplete : this.receiveDeleteEvent
          }).post($merge({ajax : 1, action : 'quick_delete'}, eventData));
        }.bind(this));
        obm.calendarManager.popupManager.show(eventData);
      }.bind(this)
    }).post($merge({ajax : 1, action : 'quick_check_delete'}, eventData));

  },


  /**
   * Delete the event on OBM server 
   */
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
      var str = response.elementId.split('_');
      showOkMessage(response.message);
      if (response.isPeriodic && response.all) { 
        var deleteRoutine = function(e) {
          var evt = obm.calendarManager.events.get(e.id);
          if(evt) {
            obm.calendarManager.unregister(evt);
            obm.calendarManager.events.erase(evt.element.id);
            evt.element.destroy();
            delete evt;
          }
        };
        $$('div.evt_'+str[1]).each(deleteRoutine);
        if ( response.deleted_ids ) {
            for ( var i = 0, j = response.deleted_ids.length; i<j; i++ ) {
               $$('div.evt_'+response.deleted_ids[i] ).each(deleteRoutine);
            }
        }
      } else {      
        for(var i=0;i< events.length;i++) {
          var ivent = events[i].event;
          var element_id = 'event_'+str[1]+'_'+ivent.entity+'_'+ivent.entity_id+'_'+str[4];
          var evt = obm.calendarManager.events.get(element_id);
          if (evt) {
            obm.calendarManager.unregister(evt);
            obm.calendarManager.events.erase(element_id);
            evt.element.destroy();
            delete evt;
          }
        }
      }
      obm.calendarManager.eventIndexing(str[1], 1);

    } else {
      showErrorMessage(response.message);
    }
  },


  /**
   * Cancel drag&drop
   * register/redraw initial event
   */
  cancel: function(id) {
    var evt = this.events.get(id);
    // Set event initial date&duration
    evt.event.date.setTime(obm.calendarManager.oldEvent.time*1000);
    evt.event.time = obm.calendarManager.oldEvent.time;
    evt.event.duration = obm.calendarManager.oldEvent.event.duration;
    // fix position & title 
    evt.setPosition();
    evt.setTitle();
    // register the old event
    this.register(evt);
  },


  /**
   * Add keyboard lister 
   * Cancel drag&drop, resize, event creation
   * Close popup creation/confict
   */
  keyboardListener: function(e) {
    switch(e.key) {
      case 'esc':
        if (obm.popup.isOpen) {
          obm.calendarManager.popupManager.cancel();
          obm.calendarQuickForm.hide();
        } else if (obm.calendarManager.oldEvent) {
          var evt = obm.calendarManager.events.get(obm.calendarManager.oldEvent.elementId);
          evt.resize.fireEvent('cancel');
          evt.drag.fireEvent('cancel');
          obm.calendarManager.cancel(obm.calendarManager.oldEvent.elementId);
        } else if (obm.calendarManager.dummy) {
          console.log('dummy');
        }
        break;
    }
  },


  /*
   * Store event in solr
   */
  eventIndexing: function(id, remove) {
    var eventData = new Object();
    eventData.id = id;
    eventData.remove = remove;
    new Request.JSON({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      onRequest: function() {
        $('spinner').show();
      },
      onComplete : function() {
        $('spinner').hide();
      }
    }).post($merge({ajax : 1, action : 'async_indexing'}, eventData));
  },

  /*
   * Set Tooltip with descrition (shrinked if necessary)
   */
  setTooltip: function(description, title, location, time) {
    description = description || '';
    if (location != ''){
      location = '\n'+location;
    }

    var suffix = ' [...]';
    var charLimit = obm.vars.consts.charLimit - suffix.length;
    var tooltip = '';
    if(description != ''){
      if(description.length > charLimit){
        tooltip = description.substr(0, charLimit);
        tooltip = tooltip.substr(0, Math.min(tooltip.length, tooltip.lastIndexOf(" ")));
        tooltip = tooltip+suffix;
      }else{
        tooltip = description;
      }
      tooltip = tooltip+location;
    } else {
      tooltip = title+location;
    }

    var allDay = '00:00 - 00:00';
    if (time != '' && time != allDay){
      tooltip = time+'\n'+tooltip;
    }
    return tooltip;
  }
});


/******************************************************************************
 * Minicalendar Selection
 *****************************************************************************/
Obm.MiniCalendar = new Class({

  initialize: function(view) {
    this.view = view;
    obm.vars.consts.selectedDays = new Array();
  },

  draw: function(y, m) {
    miniCal(y, m, this.view); //datepicker.js
  },

  clearSelection: function() {
    $$('td.minical').each(function(e) {
      e.removeClass('selected');
    });
  },

  select: function(id) {
    $$('td.minical_'+id).addClass('selected');
    obm.vars.consts.selectedDays.push(id);
  }

});


/******************************************************************************
 * Event class
 *****************************************************************************/
Obm.CalendarEvent = new Class({

  Implements: Options,

  options: {
    updatable: false
  },


  /**
   * Return event opacity 
   */
  getOpacity: function () {
    if(this.event.status == 'NEEDS-ACTION') {
      return .5;
    } else {
      return 1;
    }
  },


  /**
   * Display event icons 
   */
  setTitleIcons: function() {
    if (this.event.all_day) {
      new Element('img').setProperty('src',obm.vars.images.all_day).injectInside(this.dragHandler);
    }
    if (this.event.periodic) {
      this.periodic = new Element('img').setProperty('src',obm.vars.images.periodic).injectInside(this.dragHandler);
    } else if (this.periodic) {
      this.periodic.dispose();
    }
    if(this.event.meeting) {                                 
      new Element('img').setProperty('src',obm.vars.images.meeting).injectInside(this.dragHandler);
    }
    if (this.event.private) {
      new Element('img').setProperty('src',obm.vars.images.private).injectInside(this.dragHandler);
    }
  },


  /**
   * Select custom color
   */
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


  /**
   * Fill event with custom color
   */
  setColor: function(color) {
    if(color) {
      if (this.kind == 'all_day') {
        this.content.setStyle('backgroundColor',color.body);
        this.dragHandler.setStyle('backgroundColor',color.body);
      } else {
        this.content.setStyle('backgroundColor',color.body);
        this.dragHandler.setStyle('backgroundColor',color.header);
      }
    } else {
      this.content.setStyle('backgroundColor','');
      this.dragHandler.setStyle('backgroundColor','');
    }
  },

  isExternal: function() {
    return this.event.id.split('-')[0] == "ext";
  }

});


/******************************************************************************
 * In-day event 
 *****************************************************************************/
Obm.CalendarInDayEvent = new Class({

  Extends: Obm.CalendarEvent,

  /**
   * Constructor
   */
  initialize: function(eventData,options) {
    this.kind = 'in_day';
    this.setOptions(options);
    this.event = eventData;
    this.event.date =  new Obm.DateTime(this.event.time * 1000);
    this.draw();
    this.setPosition();
    this.switchColor(obm.vars.conf.calendarColor);
    if (this.event.updatable && obm.calendarManager.write && !this.isExternal()) this.makeUpdatable();
  },

  
  /**
   * Draw event and inject it into calendarGrid
   */
  draw: function() {
    var id = 'event_'+this.event.id+'_'+this.event.entity+'_'+this.event.entity_id+'_'+this.event.time;
    serieClass = 'evt_'+this.event.id;
    this.element = new Element('div').addClass('event '+serieClass)
                                     .setProperties({'id':id,'title':this.event.title});
    var opacity = '';
    if (this.event.opacity == 'TRANSPARENT') opacity = 'transparent'; 
    this.content = new Element('dl').addClass(this.event.klass+' '+opacity).setOpacity(this.getOpacity()).injectInside(this.element);
    var dt = new Element('dt').addClass(opacity).injectInside(this.content);
    var dd = new Element('dd').addClass(opacity).injectInside(this.content);

    this.dragHandler = new Element('h1').injectInside(dt);     
    this.setTitleIcons();

    if(this.event.updatable && obm.calendarManager.write && !this.isExternal()) {
    this.element.setProperty('style','cursor: move;');
    this.resizeHandler = new Element('div')
      .addClass('handle')
      .injectInside(this.element)
    }

    this.titleContainer = new Element('span').injectInside(dd);
    this.locationContainer = new Element('span').injectInside(dd);
    if (this.isExternal()) {
	  this.timeContainer = new Element('a').setProperty('href',obm.vars.consts.calendarDetailconsultExtURL+this.event.id+'&contact_id='+this.event.entity_id );
     /* this.timeContainer = new Element('span').injectInside(this.dragHandler);*/
      this.timeContainer.injectInside(this.dragHandler);
    } else {
      this.timeContainer = new Element('a').setProperty('href',obm.vars.consts.calendarDetailconsultURL+this.event.id);
      if ( this.event.periodic )
        this.timeContainer.addEvent('click', function(evt){
          evt.preventDefault();
          obm.calendarOccurenceEditPopup.compute(this.event, this.timeContainer);
        }.bind(this));
      if (obm.vars.consts.action == 'portlet') this.timeContainer.setProperty('target', '_blank');
      this.timeContainer.injectInside(this.dragHandler);

      this.linkContainer = this.timeContainer;
      this.linkContainer.addEvent('mousedown', function (evt) {
        this.linkContainer.addEvent('mouseup', 
          function (evt) {
            this.linkContainer.addEvent('click',
              function(evt) {
                if (obm.calendarManager.lock) {
                  evt.preventDefault();
                  this.linkContainer.removeEvents('click');
                  this.linkContainer.removeEvents('mouseup');
                }
              }.bind(this)
            );
          }.bind(this)
        )
      }.bind(this));       
    }
    this.setTitle();

    this.element.injectInside($('calendarGrid'));
  },


  /**
   * Set event date, title & location
   */
  setTitle: function() {
    var location = '';
    if (this.event.location != '') {
      location = '(' + Obm.utils.locationDecode(this.event.location) + ')';
    }
    var title = this.event.title + ' ';
    if (this.event.duration <= obm.vars.consts.timeUnit) {
      var time = this.event.date.format(obm.vars.regexp.dispTimeFormat) + ' ' + title; 
      var title = '';
      this.locationContainer.set('html', '');
    } else {
      var end =  new Obm.DateTime((this.event.time+this.event.duration) * 1000);
      var time = this.event.date.format(obm.vars.regexp.dispTimeFormat) + ' - ' + end.format(obm.vars.regexp.dispTimeFormat);
      this.locationContainer.set('text',location);
    }

    var tooltip = obm.calendarManager.setTooltip(this.event.description, title, location,'');
    this.element.setProperty('title', tooltip);
    this.timeContainer.set('text',time);
    this.titleContainer.set('text',title);
  },


  /**
   * Event is updatable. Make it resizable and draggable
   */
  makeUpdatable: function() {
    var resizeOptions = {
      handle: this.resizeHandler,
      grid: obm.calendarManager.defaultHeight,
      modifiers: {'y' : 'height', 'x' : false},
      limit: {'y': [obm.calendarManager.defaultHeight,false]},
      overflow: $('calendarBody')
    };
    var dragOptions = {
      handle: this.content,
      preventDefault: true,
      units: {'x':'%', 'y':'px'},
      grid: {'x': obm.vars.consts.cellWidth, 'y': obm.calendarManager.defaultHeight},
      container: $('calendarGrid'),
      overflow: $('calendarBody')
    };

    this.drag = new Obm.Drag(this.element, dragOptions);
    this.resize = new Obm.Drag(this.element, resizeOptions);

    // Add drag events
    this.drag.addEvent('start', function() {
      obm.calendarManager.lock = true;
      this.element.setStyles({
        'width': obm.vars.consts.cellWidth+'%',
        'z-index' : '1000'
      });
      this.element.setOpacity(.7);
      obm.calendarManager.unregister(this);

      // Enable scroller
      obm.calendarManager.scroll.start();

      // Fix mouse position
      this.drag.mouse.pos.x = $('calendarGridContainer').offsetLeft.toInt() + $('calendarBody').offsetLeft.toInt();

      // Fix grid limit
      this.drag.limit.x[1] = $('calendarGridContainer').offsetWidth.toInt()-1;

    }.bind(this));
    this.drag.addEvent('drag', this.updateTime.bind(this));
    this.drag.addEvent('complete', function() {
      this.updateComplete();
      obm.calendarManager.sendUpdateEvent(this);

    }.bind(this));
    this.drag.addEvent('cancel', function() {
      this.updateComplete();
      this.drag.stop();
    }.bind(this));

    // Add resize events
    this.resize.addEvent('start', function() {
      this.element.setStyles({
        'z-index' : '1000'
      });
      this.element.setOpacity(.7);
      obm.calendarManager.unregister(this);

      // Enable scroller
      obm.calendarManager.scroll.start();

      // Fix y limit
      this.resize.limit.y[1] =  $('calendarGridContainer').offsetHeight.toFloat() - this.element.offsetTop.toFloat();
    }.bind(this));
    this.resize.addEvent('drag', this.updateDuration.bind(this));
    this.resize.addEvent('complete', function() {
      this.updateComplete();
      obm.calendarManager.sendUpdateEvent(this);
    }.bind(this));
    this.resize.addEvent('cancel', function() {
      this.updateComplete();
      this.resize.stop();
    }.bind(this));
  },


  /*
   * Complete update drag or resize
   */
  updateComplete: function() {
    this.element.setOpacity(this.getOpacity());
    this.element.setStyles({
      'z-index' : '10' 
    });
    obm.calendarManager.scroll.stop();
  },


  /**
   * Set initial html event position
   */
  setPosition: function() {
    this.element.style.top = (this.event.date.getHours()*3600 + this.event.date.getMinutes()*60)/obm.vars.consts.timeUnit * obm.calendarManager.defaultHeight + 'px';
    if (this.event.duration < obm.vars.consts.timeUnit) {
      this.element.style.height = obm.calendarManager.defaultHeight+'px';
    } else {
      this.element.style.height = this.event.duration/obm.vars.consts.timeUnit * obm.calendarManager.defaultHeight+'px';
    }
  },


  /**
   * Update event position when redrawGrid (to prevent conflict)
   */
  updatePosition: function(unit, position, size, column) {
    this.element.style.width = (obm.vars.consts.cellWidth/unit*size) + '%';
    this.element.style.left = ($('day_'+column).style.left.toFloat()) + ((obm.vars.consts.cellWidth/unit) * position) + '%';
  },


  /**
   * Update event time on drag 
   */
  updateTime: function() {
    var time = this.element.getStyle('top').toFloat()/obm.calendarManager.defaultHeight * obm.vars.consts.timeUnit;
    this.event.date.setTime(obm.calendarManager.startTime*1000);
    this.event.date.setDate(this.event.date.getDate() + Math.floor((this.element.getStyle('left').toFloat())/obm.vars.consts.cellWidth));
    this.event.date.setHours(time/3600);
    this.event.date.setMinutes((time)%3600 / 60);
    this.event.time = Math.floor(this.event.date.getTime() / 1000);
    this.setTitle();
  },


  /**
   * Update event duration on resize
   */
  updateDuration: function() {
    var height = this.element.getStyle('height').toFloat()/obm.calendarManager.defaultHeight;
    this.event.duration = height * obm.vars.consts.timeUnit;
    this.setTitle();
  }

});


/******************************************************************************
 * Allday event 
 *****************************************************************************/
Obm.CalendarAllDayEvent = new Class({

  Extends: Obm.CalendarEvent,

  /**
   * Constructor
   */
  initialize: function(eventData,options) {
    this.kind = 'all_day';
    this.setOptions(options);
    this.event = eventData;
    this.event.date =  new Obm.DateTime(this.event.time * 1000);
    this.draw();
    this.switchColor(obm.vars.conf.calendarColor);
    if (this.event.updatable && obm.calendarManager.write && !this.isExternal()) this.makeUpdatable();
  },

  
  /**
   * Draw event and inject it into calendarHeaderGrid
   */
  draw: function() {
    var id = 'event_'+this.event.id+'_'+this.event.entity+'_'+this.event.entity_id+'_'+this.event.time;
    var extClass='';
    var serieClass= 'evt_'+this.event.id;
    if ($(id)) { 
      extClass = id;
      id += '_'+$(id).uid; // multi-weeks event
    }

    this.element = new Element('div').addClass('event '+extClass+' '+serieClass)
                                     .setProperties({'id':id,'title':this.event.title});
    var opacity = '';
    if (this.event.opacity == 'TRANSPARENT') opacity = 'alldayTransparent'; 
    this.content = new Element('dl').addClass(this.event.klass+' allDay').setOpacity(this.getOpacity()).injectInside(this.element);
    var dt = new Element('dt').addClass(opacity).injectInside(this.content);
    var dd = new Element('dd').addClass(opacity).injectInside(this.content);

    this.rightExtension = new Element('img').setProperty('src', obm.vars.images.extension_right).
      setStyles({'display':'none', 'float':'right'}).injectInside(dt);

    this.dragHandler = new Element('h1').addClass('allDay').injectInside(dt);     

    this.leftExtension = new Element('img').setProperty('src', obm.vars.images.extension_left).
      setStyle('display', 'none').injectInside(this.dragHandler);

    this.setTitleIcons();

    if(this.event.updatable && obm.calendarManager.write && !this.isExternal()) {
      this.element.setProperty('style','cursor: move;');
    }


    if (this.isExternal()) {
      this.titleContainer = new Element('a').setProperty('href',obm.vars.consts.calendarDetailconsultExtURL+this.event.id+'&contact_id='+this.event.entity_id );
      /*this.titleContainer = new Element('span').injectInside(this.dragHandler);*/
      this.titleContainer.injectInside(this.dragHandler);
    } else {
      this.titleContainer = new Element('a').setProperty('href',obm.vars.consts.calendarDetailconsultURL+this.event.id);
      if (obm.vars.consts.action == 'portlet') this.titleContainer.setProperty('target', '_blank');
      this.titleContainer.injectInside(this.dragHandler);
    
      this.linkContainer = this.titleContainer;
      this.linkContainer.addEvent('mousedown', function (evt) {
        this.linkContainer.addEvent('mouseup', 
          function (evt) {
            this.linkContainer.addEvent('click',
              function(evt) {
                if (obm.calendarManager.lock) {
                  evt.preventDefault();
                  this.linkContainer.removeEvents('click');
                  this.linkContainer.removeEvents('mouseup');
                }
              }.bind(this)
            );
          }.bind(this)
        )
      }.bind(this));       
    }
    this.setTitle();

    this.element.injectInside($('calendarHeaderGrid'));
  },


  /**
   * Set event date, title & location
   */
  setTitle: function() {
    var title = this.event.title;
    var time = this.event.date.format(obm.vars.regexp.dispTimeFormat) + ' ' + title;
    if (this.event.all_day) {
      time = (title != '') ? title : obm.vars.labels.details;
    }

    var location = '';
    if (this.event.location != '') {
      location = '(' + Obm.utils.locationDecode(this.event.location) + ')';
    }

    var end =  new Obm.DateTime((this.event.time+this.event.duration) * 1000);
    var tooltipTime = this.event.date.format(obm.vars.regexp.dispTimeFormat) + ' - ' + end.format(obm.vars.regexp.dispTimeFormat);

    var tooltip = obm.calendarManager.setTooltip(this.event.description, title, location, tooltipTime);
    this.element.setProperty('title', tooltip);
    this.titleContainer.set('text',time);
  },


  /**
   * Event is updatable. Make it draggable
   */
  makeUpdatable: function() {
    var dragOptions = {
      handle: this.content,
      preventDefault: true,
      units: {'x':'%', 'y':'px'},
      grid: {'x':obm.vars.consts.cellWidth, 'y': 0},
      container: $('calendarHeaderGrid'),
      initialWidth: obm.vars.consts.cellWidth+'%'
    };

    this.drag = new Obm.Drag(this.element, dragOptions);

    // Add drag events
    this.drag.addEvent('start', function() {
      obm.calendarManager.lock = true;
      this.element.setStyles({
        'z-index' : '10000',
        'width': obm.vars.consts.cellWidth+'%'
      });
      this.element.setOpacity(.7);
      obm.calendarManager.unregister(this);

      this.drag.limit.x[1] += (dragOptions.container.getSize().x - this.drag.limit.x[1]) / 2;
      // Fix mouse position
      this.drag.mouse.pos.x = $('calendarHeaderGrid').offsetLeft.toInt() + $('calendarHeader').offsetLeft.toInt();

    }.bind(this));
    this.drag.addEvent('complete', function() {
      this.updateTime();
      this.updateComplete();
      obm.calendarManager.sendUpdateEvent(this);
    }.bind(this));
  },

  /*
   * Complete update drag or resize
   */
  updateComplete: function() {
    this.element.setOpacity(this.getOpacity());
    this.element.setStyles({
      'z-index' : '10' 
    });
  },


  /**
   * Set initial html event position .. or not
   */
  setPosition: function() {
    // this.element.style.left = Math.floor((this.event.date.getTime() - obm.vars.consts.startTime.getTime())/86400000) * obm.vars.consts.cellWidth + '%';
    // this.element.style.width = obm.vars.consts.cellWidth+'%';
  },


  /**
   * Update event position when redrawGrid (to prevent conflict)
   */
  updatePosition: function(position, size, col) {
    var alldayColumn = $('allday_'+col);
    this.element.style.top = alldayColumn.getParent().offsetTop+ position * this.element.offsetHeight+'px';
    if (obm.vars.consts.calendarRange == 'month') {
      this.element.style.top = this.element.style.top.toFloat()+$('dayMonthLabel_'+col).getHeight()+'px';
    }
    this.element.style.width = obm.vars.consts.cellWidth*size+'%';
    this.element.style.left = alldayColumn.getParent().getStyle('left').toFloat()+'%';
  },


  /**
   * Update event time on drag 
   */
  updateTime: function() {
    var top = this.element.getStyle('top').toFloat();
    var left = this.element.getStyle('left').toFloat();
    var eventHour = this.event.date.getHours()*3600 + this.event.date.getMinutes()*60;
    var delta = Math.floor(left/obm.vars.consts.cellWidth);
    var startTime = obm.calendarManager.startTime;
    if (obm.vars.consts.calendarRange == 'month') { // FIXME
      var index = null;
      $$('div.monthRow').each(function(row, i) {
        if (row.offsetTop > top && index == null) {
          index = i;
        }
      });
      if (index == null) index = $$('div.monthRow').length;

      startTime = obm.vars.consts.weekBegin[index-1];
    }
    this.event.time = startTime + eventHour + delta*86400;
    this.event.date = new Obm.DateTime(this.event.time*1000);
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
    $('popup_form_close').addEvent('click', function() {
      obm.calendarManager.destroyDummy();
    }.bind(this));

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

    // Event delete confirmation
    $('popup_delete').addEvent('click', function() {
      this.chain.callChain();
    }.bind(this));

    $('popup_cancel_delete').addEvent('click', function() {
      this.evtId = null;
      this.cancel();
    }.bind(this));

    // Repeated event popup
    $('popup_update_one').addEvent('click', function() {
      this.fireEvent('update_one');
    }.bind(this));

    $('popup_update_all').addEvent('click', function() {
      this.fireEvent('update_all');
      this.chain.callChain();
    }.bind(this));

    $('popup_delete_one').addEvent('click', function() {
      this.chain.callChain();
    }.bind(this));

    $('popup_delete_all').addEvent('click', function() {
      this.fireEvent('delete_all');
      this.chain.callChain();
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
    this.ivent = evt;
    this.chain.chain(this.complete.bind(this));
    this.chain.callChain();
  },

  add: function(popup) {
    this.chain.chain(function () {
      obm.popup.show(popup)
    }.bind(this));
  },

  cancel: function() {
    obm.calendarManager.destroyDummy();
    this.chain.clearChain();
    this.removeEvents();
    if (this.evtId) {
      obm.calendarManager.cancel(this.ivent.element_id);
    }
  },
  
  complete: function() {
    this.chain.clearChain();
    this.fireEvent('complete')
    this.removeEvents();
  }

});


Obm.CalendarAlarmPopup = new Class({
	initialize: function() {
		this.radios = $('calendarAlarmPopup').getElements('input[type=radio]');
	},
	compute: function(alert, uid, evtid) {  
		this.uid = uid;
		this.evtid = evtid;
		var radiochecked = this.radios.filter(function(radio){
			if(radio.getProperty('value') == alert) return true;
		});
		if(radiochecked.length > 0) radiochecked[0].setProperty('checked', true);
		this.show();
	},
	show: function() {
		obm.popup.show('calendarAlarmPopup');
	},
	hide: function() {
		obm.popup.hide('calendarAlarmPopup');
	},
	updateAlarm: function(){
		var self = this;
		var checked = this.radios.filter(function(radio){
			return radio.getProperty('checked');
		});
		new Request.JSON({
			url: obm.vars.consts.calendarUrl,
			secure : false,
			onSuccess : function(message){
				if(message.error == 0){
					showMessage('ok', message.message);
					window.location='../calendar/calendar_index.php?action=detailconsult&calendar_id='+self.evtid;
				}else{
					window.location='../calendar/calendar_index.php?action=detailconsult&calendar_id='+self.evtid+'&errormessage='+encodeURIComponent(message.message);
				}
			}
		}).post({ajax : 1, action : 'update_alert', calendar_id : this.evtid, user_id : this.uid, sel_alert : checked[0].value});
	}
});

Obm.CommentedDecisionPopup = new Class({
  initialize: function() {
    this.textarea = $('commentedDecisionPopup').getElements('textarea');
    this.charCountForDecision = $('commentedDecisionPopup').getElementById('charCountForDecision');
    this.maxLength = 255;
  },
  compute: function(uid, evtid, decision, oldDecision, type, comment, title , uriAction, propertyComment, occurrenceDate) {
    this.uid = uid;
    this.evtid = evtid;
    this.decision = decision;
    this.oldDecision = oldDecision;
    this.type = type;
    this.uriAction = uriAction;
    this.comment = comment;
    this.textarea.setProperty(propertyComment, this.comment);
    this.occurrenceDate = occurrenceDate;
    this.show();
  },
  show: function() {
    obm.popup.show('commentedDecisionPopup');
  },
  hide: function() {
    this.decision = this.oldDecision = this.comment = '';
    obm.popup.hide('commentedDecisionPopup');
  },
  sendRequest: function(decision){
    this.comment = this.textarea[0].value == '' ? null : this.textarea[0].value ;
    if ( this.updateOnlyComment(decision) ) {
      return ;
    }
    var options = {
      uriAction: this.uriAction,
      success: Obm.responseHandlers.update_decision_and_comment,
      owner_notification: $('owner_notification').checked
    };
    if ( this.occurrenceDate && this.occurrenceDate.length ) {
      options.date_edit_occurrence = this.occurrenceDate;
    }
    Obm.Rest.update_decision_and_comment(
      this.type, 
      this.uid, 
      this.evtid, 
      decision, 
      this.comment, 
      options
    );
  },
  Accept: function() {
    this.sendRequest("ACCEPTED");
  },
  Wait: function() {
    this.sendRequest("NEEDS-ACTION");
  },
  Refuse: function() {
    this.sendRequest("DECLINED");
  },
  updateOnlyComment: function(choice){
    if( choice == this.oldDecision ){
      var self = this;
      new Request.JSON({      
        url: obm.vars.consts.calendarUrl,
        secure : false,
        onSuccess : function(message){
          if(message.error == 0){
            showMessage('ok', message.message);
            window.location='../calendar/calendar_index.php?action=detailconsult&calendar_id='+self.evtid;
          }else{
            window.location='../calendar/calendar_index.php?action=detailconsult&calendar_id='+self.evtid+'&errormessage='+encodeURIComponent(message.message);
          }
        }
      }).post({ajax : 1, action : 'update_comment', calendar_id : this.evtid, user_id : this.uid, comment : this.comment, type : this.type});
      return true;
    }
    return false;
  },
  
  displayCharLimit: function(){
    this.charCountForDecision.innerHTML = this.maxLength - this.textarea[0].value.length;
  }
});

Obm.decisionCBHandler = function(decision) {
  Obm.Rest.update_decision_and_comment(
    "user",
    this.getAttribute("data-entity-id"),
    this.getAttribute("data-event-id"),
    this.getAttribute("value"),
    "",
    {
      date_edit_occurrence: this.getAttribute("data-occurrence-date"),
      success: Obm.responseHandlers.update_decision_and_comment,
      uriAction: "detailconsult"
    }
  );
};

Obm.Rest = {
  update_decision_and_comment: function(entityKind, entityId, eventId, decision, comment, options){
    var postData = {
      ajax : 1, 
      action : 'update_decision_and_comment', 
      calendar_id : eventId, 
      entity_kind : entityKind, 
      entity_id : entityId,
      comment : comment, 
      decision_event : decision, 
      uriAction: null
    };
    options = options || {};
    options.success = options.success || function() {};
    
    if ( options.uriAction ) {
      postData.uriAction = options.uriAction;
    }
    if ( options.date_edit_occurrence ) {
      postData.date_edit_occurrence = options.date_edit_occurrence;
    }
    // OBMFULL-3770
    // The "<property> not in <object>" syntax is there to guarantee that the email will be sent
    // when the option isn't defined. This way we don't need to change all calls to this function
    postData.owner_notification = !("owner_notification" in options) || options.owner_notification;
    
    new Request.JSON({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      onSuccess : options.success
    }).post(postData);
  }
};

Obm.responseHandlers = {
  update_decision_and_comment: function(message){
    if(message.error == 0){
      var redirectUrl = message.redirectUrl;
      showMessage('ok', message.message);
      if (redirectUrl != null) {
        window.location=redirectUrl;
      } else {
        window.location='../calendar/calendar_index.php?action=waiting_events';
      }
    }else{
      var redirectUrl = message.redirectUrl;
      if (redirectUrl != null) {
        showMessage('error', message.message);
        window.location=redirectUrl;
      } else {
        window.location='../calendar/calendar_index.php?action=?action=waiting_events&errormessage='+encodeURIComponent(message.message);
      }
    }
  }
};

Obm.calendarOccurenceEditPopup = new Class({
  compute: function (evt, location) {
    this.event = evt;
    this.location = location;
    this.show();
  },
  show: function() {
    obm.popup.show('calendarOccurenceEdit');
  },
  editOne: function() {
    window.location=this.location+'&date_edit_occurrence='+encodeURIComponent(this.event.time);
  },
  editAll: function() {
    window.location=this.location;
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
    this.location = $('calendarQuickFormLocation');
    this.organizer = $('calendarQuickFormOrganizer');
    this.category = $('calendarQuickFormCategory');
    this.attendees = $('calendarQuickFormAttendees');
    this.deleteButton = $('calendarQuickFormDelete');
    this.detailButton = $('calendarQuickFormDetail');
    this.advancedButton = $('calendarQuickFormAdvanced');
    this.editButton = $('calendarQuickFormEdit');
    this.entityView = $('calendarViewEntity');
    this.entityKind = $('calendarKindEntity');
    this.entityList = $('calendarListEntity');

    this.eventData = new Object();

    new Obm.TabbedPane(this.data);
  },
  
  compute: function(ivent) {
    var ivent = new Event(ivent);
    var target = ivent.target;
    target = $(target);
    if (target.get('tag') == 'a') {
      return false;
    }
    while(target.id == '') {
      target = $(target.parentNode);
    }
    var str = target.id.split('_');
    if (str.length <= 1) {
      return false;
    }

    /*
    * daylight savings diff between the startdate of the grid and the date of the selected row
    */
    var getMonthlyGridDate = function(containerIdSplit) {
      var d = obm.calendarManager.startTime + containerIdSplit[1].toInt()*86400;
      d = d + ( obm.timeZoneParser.getTimeZoneOffset(obm.calendarManager.startTime*1000) - obm.timeZoneParser.getTimeZoneOffset(d*1000) ) / 1000 ;
      return d;
    };

    var type = str[0];
    $('sel_template').style.display ='';
    if ($chk($('template_id'))) {
      $('template_id').selectedIndex = 0;
      $('calendarQuickFormSubmit').disabled = false;
    }
    if (type == 'time') {
      if(!obm.calendarManager.write) return false;
      /* Crappy ie fix*/
      var x = ivent.event.layerX;
      if (!x) x =ivent.event.offsetX;
      /* End of crappy ie fix*/

      var d = obm.calendarManager.startTime + str[1].toInt() + Math.floor(x/($('calendarGrid').offsetWidth/100*obm.vars.consts.cellWidth.toInt()))*86400;
      this.setDefaultFormValues(d,0, 3600);
    } else if (type == 'allday') {
      if(!obm.calendarManager.write) return false;
      var d = obm.calendarManager.startTime + Math.floor($('allday_'+str[1]).style.left.toInt()/obm.vars.consts.cellWidth.toInt())*86400;
      this.setDefaultFormValues(d,1, 3600);
    } else if (type == 'dayContainer' || type == 'more') { // Month view
      if(!obm.calendarManager.write) return false;
      var d = getMonthlyGridDate(str);
      this.setDefaultFormValues(d,1, 3600);
    } else if (type == 'dayMonthLabel') {
      var dayContainer = target.parentNode.id.split('_');
			if(!obm.calendarManager.write) return false;
			var d = getMonthlyGridDate(dayContainer);
			this.setDefaultFormValues(d,1, 3600);
    } else {
      $('sel_template').style.display ='none';
      var evt = obm.calendarManager.events.get(target.id);
      this.setFormValues(evt);
    }

    this.show();    
    try {
      this.form.tf_title.focus();
    } catch(e) {
    }
  },

  setFormValues: function(evt) {
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
    this.eventData.periodic = evt.event.periodic;
    this.eventData.element_id = evt.element.id;
    this.eventData.private = evt.event.private;
    this.eventData.formAction = 'quick_update';
    this.gotoURI = 'action=detailupdate&calendar_id='+evt.event.id;
    
    if (evt.event.updatable && obm.calendarManager.write && !evt.isExternal()) {
      this.form.setStyle('display','block');
      this.deleteButton.setStyle('display','');
      this.editButton.setStyle('display','');
      this.detailButton.setStyle('display','');
      this.advancedButton.setStyle('display','none');
      this.title.setStyle('display', 'none');
      $('extEventTitle').setStyle('display', 'none');
      if(this.entityList) this.entityList.setStyle('display','none');
      this.editButton.value = obm.vars.labels.edit;
    } else {
      this.form.setStyle('display','none');
      if (evt.isExternal()) {
        $('extEventTitle').set('text', evt.event.title);
        this.title.setStyle('display','none');
        $('extEventTitle').setStyle('display', '');
      } else {
        this.title.set('text',evt.event.title);
        this.title.setStyle('display','');
        $('extEventTitle').setStyle('display', 'none');
      }
    }

    this.description.set('text',evt.event.description);
    this.item.set('html',evt.event.item);
    this.category.set('html',evt.event.category);
    this.location.set('text',Obm.utils.locationDecode(evt.event.location));
    this.organizer.set('text',evt.event.organizer_name);

    this.data.setStyle('display','block');
    if (!this.eventData.all_day) {
      if (date_begin.format('Ymd') == date_end.format('Ymd')) {
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat) + ', ' + date_begin.format(obm.vars.regexp.dispTimeFormat)+' - '+date_end.format(obm.vars.regexp.dispTimeFormat));
      } else {
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat+' '+obm.vars.regexp.dispTimeFormat) + ' - ' + date_end.format(obm.vars.regexp.dispDateFormat+' '+obm.vars.regexp.dispTimeFormat));
      }
    } else {
      if (evt.event.duration <= 86400) {
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat));
      } else {
        var date_end = new Obm.DateTime((evt.event.time + evt.event.duration-1) * 1000);    
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat) + ' - ' + date_end.format(obm.vars.regexp.dispDateFormat));
      }
    }
    this.attendees.set('html','');
    if (typeof(evt.event.attendees)=='object'&&(evt.event.attendees instanceof Array)) {
      for(var i=0;i<evt.event.attendees.length;i++) {
        var attendee = evt.event.attendees[i];
        new Element('h4').appendText(attendee.label).injectInside(this.attendees);
        var ul = new Element('ul').injectInside(this.attendees);
        for(var j=0;j<attendee.entities.length;j++) {
          entity = attendee.entities[j];
          new Element('li').appendText(entity).injectInside(ul);
        }
      }
    } else {
      this.attendees.set('html',evt.event.attendees);
    }
  },

  setDefaultFormValues: function(time, allDay, duration) {
    var date_begin = new Obm.DateTime(time * 1000);
    var date_end = new Obm.DateTime((time + duration) * 1000);  
    this.form.tf_title.value = '';
    this.eventData.calendar_id = '';
    this.eventData.entity_id = '';
    this.eventData.entity = '';
    this.eventData.all_day = allDay;
    if (allDay) {
      this.eventData.opacity = 'TRANSPARENT';
    } else {
      this.eventData.opacity = 'OPAQUE';
    }
    this.eventData.date_begin = date_begin.format('c');
    this.eventData.old_date_begin = this.eventData.date_begin;
    this.eventData.duration = duration;
    if (allDay && duration != 3600) {
      this.eventData.duration+=86400;
    }
    this.eventData.element_id = '';
    this.eventData.formAction = 'quick_insert';
    this.gotoURI = 'action=new';
    this.advancedButton.value = obm.vars.labels.edit_full;

    this.form.setStyle('display','block');
    this.data.setStyle('display','none');
    this.title.setStyle('display','none');
    $('extEventTitle').setStyle('display', 'none');
    this.deleteButton.setStyle('display','none');
    this.editButton.setStyle('display','none');
    this.advancedButton.setStyle('display','');
    this.detailButton.setStyle('display','none');
    if (this.entityList) {
      this.entityList.setStyle('display','');
    }

    if (!this.eventData.all_day) {
      if (date_begin.format('Ymd') == date_end.format('Ymd')) {
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat) + ', ' + date_begin.format(obm.vars.regexp.dispTimeFormat)+' - '+date_end.format(obm.vars.regexp.dispTimeFormat));
      } else {
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat+' '+obm.vars.regexp.dispTimeFormat) + ' - ' + date_end.format(obm.vars.regexp.dispDateFormat+' '+obm.vars.regexp.dispTimeFormat));
      }
    } else {
      if (this.eventData.duration <= 86400) {
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat));
      } else {
        this.date.set('html',date_begin.format(obm.vars.regexp.dispDateFormat) + ' - ' + date_end.format(obm.vars.regexp.dispDateFormat));
      }
    }
    this.attendees.set('html','');
  },

  show: function() {
    obm.popup.show('calendarQuickForm');
  },

  hide: function() {
    obm.popup.hide('calendarQuickForm');
    obm.popup.hide('calendarConflictPopup');
  },

  submit: function(action) {
    if (action == 'quick_delete') {
      obm.calendarManager.sendDeleteEvent(this.eventData);
    } else if (action == 'detailupdate') {
      var evt = obm.calendarManager.events.get(this.eventData.element_id);
      obm.calendarManager.sendDetailUpdate(evt);
      this.hide();
    } else if (this.form.tf_title.value != '') {
      this.eventData.title = this.form.tf_title.value;
      this.eventData.entity_id = this.entityView.get('inputValue');
      this.eventData.entity_kind = this.entityKind.value;
      this.eventData.send_mail = null;
      if ($chk($('template_id'))) {
        this.eventData.template_id = $('template_id').value;
      }
      action = action || this.eventData.formAction;
      if (action == 'quick_insert') {
        obm.calendarManager.sendCreateEvent(this.eventData);
      } else {
        var evt = obm.calendarManager.events.get(this.eventData.element_id);
        if (evt.event.title != this.eventData.title)  {
          evt.event.title = this.eventData.title;
          obm.calendarManager.sendUpdateEvent(evt, true);
        }
      }
      this.hide();
    } else {
      obm.calendarManager.destroyDummy();
      showErrorMessage(obm.vars.labels.fill_title); 
    }
  },

  goTo: function(action) {
    if (action) {
      this.gotoURI += '&action='+action;
    }
    this.eventData.entity_id = this.entityView.get('inputValue');
    this.gotoURI += '&utf8=1&date_begin='
      +encodeURIComponent(this.eventData.date_begin)+'&duration='+this.eventData.duration
      +'&new_user_id[]='+this.eventData.entity_id;
    if(!this.eventData.private) {
    	this.gotoURI += '&title='+encodeURIComponent(this.form.tf_title.value);
    }
    if(this.eventData.all_day == '1'  && obm.vars.consts.calendarRange != 'month'){
      this.gotoURI += '&all_day=1';
    } 
    gotoUriContainsActionNew = new RegExp("action=new");
    if(action == "new" || gotoUriContainsActionNew.test(this.gotoURI))
      this.gotoURI +='&organizer='+this.eventData.entity_id;

    if ($chk($('template_id'))) {
      if($('template_id').value > 0) {
        this.gotoURI += '&template_id='+$('template_id').value;
      }
    }
    window.location.href = obm.vars.consts.calendarUrl+'?'+this.gotoURI;
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
        url: obm.vars.consts.calendarUrl,
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
                'value': obm.vars.consts.calendarUrl+'?'+obmbookmark_properties
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
          url: obm.vars.consts.calendarUrl,
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
          url: obm.vars.consts.calendarUrl,
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
