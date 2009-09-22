/******************************************************************************
 * Manager class
 *****************************************************************************/
Obm.CalendarManager = new Class({

  /**
   * Constructor
   */
  initialize: function(time) {
    this.redraw = new Hash();
    this.redrawAllDay = new Hash();
    this.events = new Hash();
    this.eventGrid = new Array();
    this.alldayEventGrid = new Array();
    this.maxHeight = 1;
    this.startTime = time;
    this.forceRedraw = false;
    this.tips = new Obm.Tip(null, 'calTip'); 
    this.calendarView = obm.vars.consts.calendarView;
    var current = new Obm.DateTime(obm.vars.consts.startTime);
    this.entityEvents = new Array();
    if (this.calendarView == 'day') {
      for(i=0;i<obm.vars.consts.nbDisplayedDays;i++) {
        current.setTime(obm.vars.consts.startTime.getTime());
        current.setDate(obm.vars.consts.startTime.getDate() + i);
        this.eventGrid[current.format('Y-m-d')] = new Hash();
      }
    }

    // Window height observer 
    new Obm.Observer(window, {onStop:this.resizeGrid, property:'innerHeight'});

    this.popupManager = new Obm.CalendarPopupManager(); 
    if (obm.vars.consts.calendarView == 'day') {
      this.defaultHeight = $('calendarBody').getElement('div').offsetHeight;    
      this.scroll = new Obm.Scroller($('calendarBody'), {area: this.defaultHeight, velocity: 1});
    }

    if ($('todayHourMarker')) {
      // Set hourMarker initial position
      this.updateHourMarker();
      this.updateHourMarker.periodical(120000, this);
    }


    if (obm.vars.consts.calendarView == 'day') {
      this.resizeGrid();
      $('calendarBody').scrollTo(0, obm.vars.consts.firstHour*this.defaultHeight*(3600/obm.vars.consts.timeUnit));
    } else {
      // month view
      $$('div.alldayContainer').each(function(element, index) {
        new Obm.Observer(element, {onStop:this.resizeAlldayCell, property:'offsetHeight'});
      }.bind(this)); 
      this.resizeGrid();
    }

    // *************************************** IE6 CRAPPY FIX
    if ($('calendarGrid')) {
      var height = $('calendarGrid').getHeight();
      $$('div.dayCol').each(function(e) {
        e.setStyle('height', height+'px');
      });
    }
  },


  /**
   * Update hour marker (left panel & in-day) 
   */ 
  updateHourMarker: function() {
    var now = new Date();
    $('todayHourMarker').style.top = (now.getHours()*3600 + now.getMinutes()*60)/obm.vars.consts.timeUnit * this.defaultHeight + 'px';
    // $('hourMarker').style.top = (now.getHours()*3600 + now.getMinutes()*60)/obm.vars.consts.timeUnit * this.defaultHeight + 'px';
  },


  /**
   * Register an event
   */
  register: function(evt) {
    this.events.set(evt.event.id, evt);
    var index = new Obm.DateTime(evt.event.time * 1000).format('Y-m-d');
    if (evt.kind == 'all_day') {
      var current = new Obm.DateTime(evt.event.time*1000);
      if (evt.event.date < obm.vars.consts.startTime) {
        current = obm.vars.consts.startTime;
      }
      var beginDay = current.getTime();
      var endDay = new Obm.DateTime((evt.event.time+evt.event.duration)*1000);
      var size = Math.ceil((endDay.getTime() - beginDay)/86400000);
      if (!evt.event.all_day && evt.event.date.getDate() != endDay.getDate()) {
        size = size + 1;
      }

      // Extensions
      if (obm.vars.consts.calendarView == 'month') {
        var weeks = obm.calendarManager.getEventWeeks(evt);
        var start = weeks[0];

        if (evt.event.left) {
          var oldBegin = beginDay
          beginDay = evt.event.index*1000;
          var current = new Obm.DateTime(beginDay);
          size = size - Math.ceil((beginDay-oldBegin)/86400000);
        }

        if (evt.event.right) {
          var startWeek = obm.vars.consts.weekTime[start][0] * 1000;
          size = Math.ceil((startWeek + (86400000 * 7) - beginDay)/86400000);
        }

      }

      evt.size = size;

      for(var i=0;i<size;i++) {
        current.setTime(beginDay);
        current.setDate(current.getDate()+ i);
        var index = current.format('Y-m-d');

        if($('allday_'+index)) {
          if(!this.alldayEventGrid[index]) {
            this.alldayEventGrid[index] = new Array();
          }
          this.alldayEventGrid[index].push(evt); 
          this.redrawAllDay.set(index, true);

          if (obm.calendarManager.calendarView == 'month') {
            current.setHours(0);
            current.setMinutes(0);
            current.setSeconds(0);
            current.setMilliseconds(0);
            var day = (current.getTime()-obm.vars.consts.startTime.getTime())/1000;
            var more = $('more_'+day+'_'+index);
            if (more) {
              var title = '<b>'+evt.event.date.format('H:i')+'</b> -  '+evt.event.title;
	            var color = evt.content.getStyle('backgroundColor');
              if (evt.event.colors.event) color = evt.event.colors.event.body;
              var style = 'style="color:'+color+'"';
              if (evt.event.all_day) {
                title = evt.event.title;
                color = "#fff";
                klass='class="'+evt.event.klass+'"';
                style = 'style="background:'+evt.event.colors.event+'; color:'+color+'" '+klass ;
              }
              more.set('title', more.get('title')+'<div '+style+'>'+ title+'</div>');
            }
          }
        } else {

        }
      }

      if (this.forceRedraw) {
        this.redrawAllDayGrid();
        this.resizeGrid();
      }

    } else {
      var begin = evt.element.offsetTop.toFloat();
      var end = begin + evt.element.getStyle('height').toFloat();
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
  },


  /**
   * Unregister an event
   */
  unregister: function(evt) {
    this.oldEvent = $merge(new Object(), evt);
    this.oldEvent.event.date = new Obm.DateTime(evt.event.time*1000);
    this.oldEvent.time = evt.event.time;
    if (evt.kind == 'all_day') {
      var current = new Obm.DateTime(evt.event.time*1000);
      var beginDay = current.getTime();
      var endDay = new Obm.DateTime((evt.event.time+evt.event.duration)*1000).getTime();
      var size = Math.ceil((endDay - beginDay)/86400000);
      for(var i=0;i<size;i++) {
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
    } else {
      var event_date = new Obm.DateTime(evt.event.time * 1000);
      var day = event_date.format('Y-m-d');
      var begin = evt.element.offsetTop.toFloat();
      var end = begin + evt.element.getStyle('height').toFloat();
      for(var i=begin;i<end;i++) {
        this.eventGrid[day].get(i).erase(evt);
      }
      this.redraw.set(day, true);
      this.redrawGrid();
    }
  },


  /*
   * Set custom class
   */
  setEventsClass: function(entity, id, klass) {
    if (obm.calendarManager.entityEvents[entity+'-'+id]) {
      obm.calendarManager.entityEvents[entity+'-'+id].each(function(e) {
        e.set('class',klass);
      });
    }
  },


  /**
   * Add an all day event
   */
  newDayEvent: function(eventData,options) {
    obmEvent = new Obm.CalendarAllDayEvent(eventData,options);
    this.register(obmEvent);
    return obmEvent;
  },


  /**
   * Add an in-day event
   */
  newEvent: function(eventData,options) {
    var obmEvent = new Obm.CalendarInDayEvent(eventData,options);
    this.register(obmEvent);
    return obmEvent;
  },


  /**
   * Resize viewport
   */
  resizeGrid: function() {
    if (obm.vars.consts.calendarView == 'day') {
      $('calendarBody').setStyle('height',window.getHeight() - $('calendarBody').offsetTop -60);
    } else {
      $('mainContent').setStyle('height',window.getHeight() - $('mainContent').offsetTop -50);
      $('calendarHeaderGrid').setStyle('height',window.getHeight() - $('calendarHeaderGrid').offsetTop -50);
    }
  },


  /**
   * Ajust displayd events on month view // FIXME
   */
  resizeAlldayCell: function(element) {

    var str = element.id.split('_');
    var content = $('allday_'+str[2]);
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
      var more = $('more_'+str[1]+'_'+str[2]);
      more.style.top = canBeDisplayed*15+'px';
      more.style.display = '';
      more.set('html','+'+undisplayed+' '+obm.vars.labels.more);
      obm.calendarManager.tips.add(more);
    } else {
      $('more_'+str[1]+'_'+str[2]).style.display = 'none';
      if (obm.calendarManager.alldayEventGrid[str[2]]) {
        obm.calendarManager.alldayEventGrid[str[2]].each(function(e) {
          e.element.style.display = '';
        });
      }
    }

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
   * Redraw all-day grid (top position & size)
   */
  redrawAllDayGrid: function() {
    var updated = new Array();
    var usedPosition = new Array(); 
    var passed = new Hash(); 

    this.redrawAllDay.each (function(redraw, columnIndex) {
      //if(!redraw) return

      this.alldayEventGrid[columnIndex].each(function(evt) {

        var passedId = evt.element.uid;
        if (!passed.get(passedId)) {

          var end = new Obm.DateTime((evt.event.time+evt.event.duration)*1000);
          var begin = evt.event.date.getTime(); 
          var size = evt.size;
          var current = new Obm.DateTime(evt.event.time*1000);

          // Extensions
          if (obm.vars.consts.calendarView == 'month') {
            size = evt.size;
            var weeks = obm.calendarManager.getEventWeeks(evt);
            var start = weeks[0];

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
              size = Math.ceil((end.getTime() - begin)/86400000);
              evt.leftExtension.setStyle('display', '');
            }

            if ((evt.event.date+evt.event.duration*1000) > obm.calendarManager.startTime*1000 + (86400000 * obm.vars.consts.nbDisplayedDays)) {
              size = Math.ceil((obm.calendarManager.startTime*1000 + (86400000 * obm.vars.consts.nbDisplayedDays) - begin)/86400000);
              evt.rightExtension.setStyle('display', '');
            }

          }

          var coords = {'position': position, 'size': size,  'column': columnIndex, 'occurrence': evt};
          var currentTime = current.getTime();

          for(var i=0;i<size;i++) {
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
    }.bind(this));

    //this.redrawAllDay = new Hash();
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
    if (obm.vars.consts.calendarView == 'day') {
      $$('div.alldayContainer').each(function(element, index) {
        element.style.height = 14*(size+2)+'px';
      });
      $('calendarHeaderGrid').style.height = 14*(size+2)+'px'; // FIXME
    }
  },


  /**
   *  Show last month events // TODO
   */
  subMonth: function() {
   this.clearGrid();

  },


  /**
   *  Show last week events // TODO
   */
  subWeek: function() {
   this.clearGrid();

  },


  /**
   *  Show last day events // TODO
   */
  subDay: function() {
   this.clearGrid();

  },


  /**
   *  Show next month events // TODO
   */
  addMonth: function() {
   this.clearGrid();

  },


  /**
   * Show next week events  // TODO
   */
  addWeek: function() {
   this.clearGrid();

  },


  /**
   * Show next day events  // TODO
   */
  addDay: function() {
   this.clearGrid();

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
   * Reset the calendar grid
   */
  clearGrid: function() {
    $$('.event').each(function(evt) {
      evt.destroy();
    });
    this.events = new Hash();
    this.eventGrid = new Hash();
    this.alldayEventGrid = new Hash();
    for(i=0;i<obm.vars.consts.nbDisplayedDays;i++) {
      this.eventGrid[i] = new Hash();
      this.alldayEventGrid[i] = new Hash();
    }
    $('calendarHeaderGrid').style.height  = this.defaultHeight+'px';
  },


  /**
   * Refresh the calendar grid
   */
  refresh: function() {
  },


  /**
   * Create an object compatible with OBM server
   */
  prepareEventForUpdate: function(evt) {
    var eventData = new Object();
    eventData.eventdate = evt.event.date;
    eventData.calendar_id = evt.event.id;
    eventData.element_id = evt.element.id;
    eventData.date_begin = evt.event.date.format('c');
    eventData.old_date_begin = eventData.date_begin;
    eventData.duration = evt.event.duration;
    eventData.title = evt.event.title;
    eventData.all_day = evt.event.all_day;
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
      onComplete : function(response) {
        if (response.conflict) {
          $('popup_force').value = obm.vars.labels.insert_force;
          obm.calendarManager.popupManager.add('calendarConflictPopup');
        }
        if (response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
        obm.calendarManager.popupManager.addEvent('conflict', function() {
          eventData = $merge({action : 'insert'}, eventData)
          window.location=obm.vars.consts.calendarUrl+'?'+Hash.toQueryString(eventData)+'&repeat_kind=none&repeat_end='+eventData.date_begin;
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
    var events = response.eventsData;
    if (response.error == 0) {
      showOkMessage(response.message);

      if (response.day == 1) {
        obm.calendarManager.newDayEvent(events[0].event,events[0].options);
        if (obm.calendarManager.calendarView == 'month') {
          // var time = events[0].event.time;
          // var day = time - obm.vars.consts.startTime/1000;
          // var iso = new Obm.DateTime(time*1000).format('Y-m-d');
          //obm.calendarManager.resizeAlldayCell($('dayContainer_'+day+'_'+iso));
        }

      } else {
        obm.calendarManager.newEvent(events[0].event,events[0].options);
      }

    } else {
      showErrorMessage(response.message);
    }
  }, 



  // **************************************************************************
  // UPDATE
  // **************************************************************************

  /**
   * Update the event on OBM server 
   */
  sendUpdateEvent: function(evt) {
    var eventData = this.prepareEventForUpdate(evt);

    new Request.JSON({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      onComplete : function(response) {
        if (response.conflict) {
          $('popup_force').value = obm.vars.labels.conflict_force;
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
            url: obm.vars.consts.calendarUrl,
            secure : false,
            onComplete : this.receiveUpdateEvent.bind(this)
          }).post($merge({ajax : 1, action : 'quick_update'}, eventData));
        }.bind(this));
        obm.calendarManager.popupManager.show(eventData);
      }.bind(this)
    }).post($merge({ajax : 1, action : 'check_conflict'}, eventData));    
  },


  /**
   * Update the event on OBM server 
   */
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
        var evt = obm.calendarManager.events.get(ivent.id);
        if (ivent.status == 'ACCEPTED' || ivent.status == 'NEEDS-ACTION') {
          evt.event.id = ivent.id;
          evt.event.status = ivent.status;
          evt.event.time = ivent.time;
          evt.event.duration = ivent.duration;
          evt.event.date =  new Obm.DateTime(ivent.time * 1000);
          if (obm.calendarManager.calendarView == 'month') {
            // Delete current event and create a new one (easy way)
            $$('.evt_'+ivent.id).each(function(e) {
              e.destroy();
            });
            var weeks = obm.calendarManager.getEventWeeks(evt);
            if (weeks[0] == weeks[1]) {
                obm.calendarManager.newDayEvent(evt.event,evt.options);
            } else { // multi weeks event
              weeks.each(function(w) {
                //evt.event.week = w;
                obm.calendarManager.newDayEvent(evt.event,evt.options);
              });
            }
          } else {
            obm.calendarManager.register(evt);           
            evt.setTitle(ivent.title);
          }
        } else if (evt) {
          obm.calendarManager.events.erase(str[1]);
          evt.destroy();
          delete evt;
        }
      }
    } else {
      showErrorMessage(response.message);
      // TODO: redraw event
    }
    obm.calendarManager.redrawAllDayGrid();
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
      onComplete : function(response) {
        if(response.mail) {
          obm.calendarManager.popupManager.add('calendarSendMail');
        }
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
    }).post($merge({ajax : 1, action : 'check_conflict'}, eventData));    
 
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
      showOkMessage(response.message);
      var str = response.elementId.split('_');
      for(var i=0;i< events.length;i++) {
        var evt = obm.calendarManager.events.get(str[1]);
        if (evt) {
          obm.calendarManager.unregister(evt);
          obm.calendarManager.events.erase(str[1]);
          evt.element.destroy();
          delete evt;
        }
      }
    } else {
      showErrorMessage(response.message);
      // obm.calendarManager.events.each(function(evt, key) {
      //   evt.redraw(); 
      // });      
    }
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
      if (obm.vars.consts.calendarView == 'month') {
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
    if (this.event.updatable) this.makeUpdatable();
  },

  
  /**
   * Draw event and inject it into calendarGrid
   */
  draw: function() {
    var id = 'event_'+this.event.id+'_'+this.event.entity+'_'+this.event.entity_id+'_'+this.event.time;
    this.element = new Element('div').addClass('event')
                                     .setProperties({'id':id,'title':this.event.title})
                                     .setOpacity(this.getOpacity());

    this.content = new Element('dl').addClass(this.event.klass).injectInside(this.element);
    var dt = new Element('dt').injectInside(this.content);
    var dd = new Element('dd').injectInside(this.content);

    this.dragHandler = new Element('h1').injectInside(dt);     
    this.setTitleIcons();

    if(this.event.updatable) {
    this.dragHandler.setProperty('style','cursor: move;');
    this.resizeHandler = new Element('div')
      .addClass('handle')
      .injectInside(this.element)
    }

    this.titleContainer = new Element('span').injectInside(dd);
    this.locationContainer = new Element('span').injectInside(dd);
    this.timeContainer = new Element('a')
       .setProperty('href',obm.vars.consts.calendarDetailconsultURL+this.event.id)
       .injectInside(this.dragHandler);

    this.linkContainer = this.titleContainer;
    this.linkContainer.addEvent('mousedown', function (evt) {
      this.linkContainer.addEvent('mouseup', 
        function (evt) {
          this.linkContainer.addEvent('click',
            function(evt) {
              evt.preventDefault();
              this.linkContainer.removeEvents('click');
              this.linkContainer.removeEvents('mouseup');
            }.bind(this)
          );
        }.bind(this)
      )
    }.bind(this));       

    this.setTitle();
    this.element.injectInside($('calendarGrid'));
    if (!obm.calendarManager.entityEvents[this.event.entity+'-'+this.event.entity_id]) {
      obm.calendarManager.entityEvents[this.event.entity+'-'+this.event.entity_id] = new Array();
    }
    obm.calendarManager.entityEvents[this.event.entity+'-'+this.event.entity_id].push(this.content);
  },


  /**
   * Set event date, title & location
   */
  setTitle: function() {
    var location = '';
    if (this.event.location != '') {
      location = '(' + this.event.location + ')';
    }
    var title = this.event.title + ' ';
    if (this.event.duration <= obm.vars.consts.timeUnit) {
      var time = this.event.date.format("H:i") + ' ' + title; 
      var title = '';
      this.locationContainer.set('html', '');
    } else {
      var end =  new Obm.DateTime((this.event.time+this.event.duration) * 1000);
      var time = this.event.date.format("H:i") + ' - ' + end.format("H:i");
      this.locationContainer.set('html',location);
    }

    this.element.setProperty('title', this.event.title + ' ' + location);
    this.timeContainer.set('html',time);
    this.titleContainer.set('html',title);
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
      this.element.setStyles({
        'width': obm.vars.consts.cellWidth+'%',
        'z-index' : '1000'
      });
      this.element.setOpacity(.7);
      obm.calendarManager.unregister(this);

      // Enable scroller
      obm.calendarManager.scroll.start();

    }.bind(this));
    this.drag.addEvent('drag', this.updateTime.bind(this));
    this.drag.addEvent('complete', function() {
      this.element.setOpacity(this.getOpacity());
      this.element.setStyles({
        'z-index' : '10' 
      });
      obm.calendarManager.scroll.stop();
      obm.calendarManager.sendUpdateEvent(this);
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

    }.bind(this));
    this.resize.addEvent('drag', this.updateDuration.bind(this));
    this.resize.addEvent('complete', function() {
      this.element.setOpacity(this.getOpacity());
      this.element.setStyles({
        'z-index' : '10' 
      });
      obm.calendarManager.scroll.stop();
      obm.calendarManager.sendUpdateEvent(this);
    }.bind(this));
  },


  /**
   * Set initial html event position
   */
  setPosition: function() {
    this.element.style.top = (this.event.date.getHours()*3600 + this.event.date.getMinutes()*60)/obm.vars.consts.timeUnit * obm.calendarManager.defaultHeight + 'px';
    if (this.event.duration < obm.vars.consts.timeUnit) {
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
    var top = this.element.getStyle('top').toFloat();
    var delta = Math.floor(this.element.getStyle('left').toFloat()/obm.vars.consts.cellWidth);
    this.event.date.setTime(obm.calendarManager.startTime*1000);
    this.event.date.setDate(this.event.date.getDate() + delta);    
    this.event.date.setHours(top/obm.calendarManager.defaultHeight * obm.vars.consts.timeUnit/3600);
    this.event.date.setMinutes((top/obm.calendarManager.defaultHeight * obm.vars.consts.timeUnit)%3600 / 60);
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
    //this.setPosition();
    this.switchColor(obm.vars.conf.calendarColor);
    if (this.event.updatable) this.makeUpdatable();

    // Hide events in month view
    // Events will be visible on obm.CalendarManager.resizeAlldayCell
    // if (obm.vars.consts.calendarView == 'month') this.element.style.visibility='hidden';
  },

  
  /**
   * Draw event and inject it into calendarGrid
   */
  draw: function() {
    var id = 'event_'+this.event.id+'_'+this.event.entity+'_'+this.event.entity_id+'_'+this.event.time;
    this.element = new Element('div').addClass('event evt_'+this.event.id)
                                     .setProperties({'id':id,'title':this.event.title})
                                     .setOpacity(this.getOpacity());

    this.content = new Element('dl').addClass(this.event.klass+' allDay').injectInside(this.element);
    var dt = new Element('dt').injectInside(this.content);
    var dd = new Element('dd').injectInside(this.content);

    this.leftExtension = new Element('img').setProperty('src', obm.vars.images.extension_left).
      setStyle('display', 'none').injectInside(dt);
    this.rightExtension = new Element('img').setProperty('src', obm.vars.images.extension_right).
      setStyles({'display':'none', 'float':'right'}).injectInside(dt);
    this.dragHandler = new Element('h1').addClass('allDay').injectInside(dt);     

    this.setTitleIcons();

    if(this.event.updatable) {
      this.dragHandler.setProperty('style','cursor: move;');
    }

    this.titleContainer = new Element('a')
       .setProperty('href',obm.vars.consts.calendarDetailconsultURL+this.event.id)
       .injectInside(this.dragHandler);

    this.linkContainer = this.titleContainer;
    this.linkContainer.addEvent('mousedown', function (evt) {
      this.linkContainer.addEvent('mouseup', 
        function (evt) {
          this.linkContainer.addEvent('click',
            function(evt) {
              evt.preventDefault();
              this.linkContainer.removeEvents('click');
              this.linkContainer.removeEvents('mouseup');
            }.bind(this)
          );
        }.bind(this)
      )
    }.bind(this));       

    this.setTitle();

    this.element.injectInside($('calendarHeaderGrid'));

    if (!obm.calendarManager.entityEvents[this.event.entity+'-'+this.event.entity_id]) {
      obm.calendarManager.entityEvents[this.event.entity+'-'+this.event.entity_id] = new Array();
    }
    obm.calendarManager.entityEvents[this.event.entity+'-'+this.event.entity_id].push(this.content);
  },


  /**
   * Set event date, title & location
   */
  setTitle: function() {
    var title = this.event.title;
    var time = this.event.date.format("H:i") + ' ' + title; 
    if (this.event.all_day) {
      time = title;
    }

    this.element.setProperty('title', this.event.title);
    this.titleContainer.set('html',time);
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
      container: $('calendarHeaderGrid')
    };

    this.drag = new Obm.Drag(this.element, dragOptions);

    // Add drag events
    this.drag.addEvent('start', function() {
      this.element.setStyles({
        'z-index' : '10000',
        'width': obm.vars.consts.cellWidth+'%'
      });
      this.element.setOpacity(.7);
      obm.calendarManager.unregister(this);
    }.bind(this));
    this.drag.addEvent('complete', function() {
      this.updateTime();
      this.element.setOpacity(this.getOpacity());
      this.element.setStyles({
        'z-index' : '10' 
      });
      obm.calendarManager.sendUpdateEvent(this);
    }.bind(this));
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
    this.element.style.top = alldayColumn.offsetHeight + alldayColumn.getParent().offsetTop+ position * this.element.offsetHeight+'px';
    if (obm.calendarManager.calendarView == 'month') {
      this.element.style.top = this.element.style.top.toFloat()+$('dayMonthLabel_'+col).getHeight()+'px';
    }
    this.element.style.width = obm.vars.consts.cellWidth*size+'%';
    this.element.style.left = alldayColumn.getParent().getStyle('left');
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
    if (obm.calendarManager.calendarView == 'month') { // FIXME
      var rowHeight = $$('div.monthRow')[0].getHeight();
      var index = null;
      $$('div.monthRow').each(function(row, i) {
        if (row.offsetTop > top && index == null) {
          index = i;
        }
      });
     startTime = obm.vars.consts.weekBegin[index-1];
    }
    this.event.time = startTime + eventHour;
    this.event.date = new Obm.DateTime(this.event.time*1000);
    this.event.date.setDate(this.event.date.getDate()+delta);
    this.setTitle();
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
    this.chain.clearChain();
    this.removeEvents();
    if (this.evtId) {
      var evt = obm.calendarManager.events.get(this.ivent.calendar_id);
      // Set event initial date
      evt.event.date.setTime(obm.calendarManager.oldEvent.time*1000);
      evt.event.time = obm.calendarManager.oldEvent.time;
      // register the old event
      obm.calendarManager.register(evt);
      // fix position & title 
      evt.setPosition();
      evt.setTitle();
    }
  },
  
  complete: function() {
    this.chain.clearChain();
    this.fireEvent('complete')
    this.removeEvents();
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

    var type = str[0];
    if (type == 'time') {
      var d = obm.calendarManager.startTime + str[1].toInt() + Math.floor(ivent.event.layerX/($('calendarGrid').offsetWidth/100*obm.vars.consts.cellWidth))*86400;
      this.setDefaultFormValues(d,0, context);
    } else if (type == 'allday') {
      var d = obm.calendarManager.startTime + Math.floor($('allday_'+str[1]).style.left.toInt()/obm.vars.consts.cellWidth.toInt())*86400;
      this.setDefaultFormValues(d,1, context);
    } else if (type == 'dayContainer' || type == 'more' ) { // Month view
      var d = obm.calendarManager.startTime + str[1].toInt();
      this.setDefaultFormValues(d,1, context);
    } else {
      var evt = obm.calendarManager.events.get(str[1]);
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
      this.date.set('html',date_begin.format(obm.vars.regexp.dateFormat+' H:i') + ' - ' + date_end.format(obm.vars.regexp.dateFormat+' H:i'));
    } else {
      this.date.set('html',date_begin.format(obm.vars.regexp.dateFormat) + ' - ' + date_end.format(obm.vars.regexp.dateFormat));
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


/*
 * FreeBusy interface
 */
Obm.CalendarFreeBusy = new Class({

  /*
   * Initialize attributes
   */
  initialize: function(time_slots, unit, begin_timestamp, first_hour) {
    this.timestamp = begin_timestamp;
    // this.date = new Obm.DateTime(this.timestamp*1000).toLocaleDateString(); Doesn't work on Windows ?
    var d = new Obm.DateTime(this.timestamp*1000);
    this.date = d.format(obm.vars.regexp.dateFormat);
    d = null;
    this.unit = unit;
    this.stepSize = 40/this.unit;
    this.external_contact_count = 0;
    this.nbSteps = time_slots.length;
    this.bts = new Array();
    this.ts = time_slots;
    this.meeting_slots = this.unit;
    this.oneDayWidth = this.stepSize*this.nbSteps/7; // in px
    this.currentPosition = 0;
    this.attendeesSlot = new Array();
    $('calendarFreeBusyScroll').setStyle('width', $(document.body).offsetWidth - 400 +'px'); // FIXME: POPUP SIZE
    this.firstHour = first_hour;
    this.timeSlotNoCalendar = new Element('tr').addClass('oneAttendee');
    this.timeSlotNoEvents = new Element('tr').addClass('oneAttendee');
    this.ts.each(function() {
      this.timeSlotNoCalendar.adopt(new Element('td').addClass('timeSlotNoCalendar'));
      this.timeSlotNoEvents.adopt(new Element('td').addClass('timeSlot'));
    }.bind(this));
  },

  /*
   * build panel 
   * Build meeting slider, meeting resizer
   */
  buildFreeBusyPanel: function(duration, readOnly) {
    $('duration').value = duration*3600;
    this.meeting_slots = this.ts.indexOf(''+(this.timestamp+duration*3600))-this.ts.indexOf(''+this.timestamp);

    // /!\ meeting width must be set BEFORE Slider building
    if (Browser.Engine.trident) {
      $('calendarFreeBusyMeeting').setStyle('width', this.stepSize*this.meeting_slots-(this.meeting_slots/2)+'px');
    } else {
      $('calendarFreeBusyMeeting').setStyle('width', (this.stepSize*this.meeting_slots)-(this.meeting_slots)+'px');
    }

    // Meeting slider : change date begin
    this.slider = new Slider($('calendarFreeBusyGrid'), $('calendarFreeBusyMeeting'), {
      steps: this.nbSteps-this.meeting_slots,
      snap: true,
      onChange: function(pos) {
        this.currentPosition = pos;
        this.changeStatus(this.isBusy(this.currentPosition + this.meeting_slots-1));
        this.displayMeetingInfo();
      }.bind(this),

      onComplete: function() {
        var ts = this.ts[this.currentPosition] * 1000;
        var date_begin = new Obm.DateTime(ts);
        $('date_begin').value = date_begin.format('c');
        $('time_begin').value = date_begin.format('h');
        $('min_begin').value = date_begin.format('i');

        var date_begin_ts = this.ts[this.currentPosition]*1000;
        var date_end = new Obm.DateTime(this.ts[this.currentPosition+this.meeting_slots]*1000);
        if (date_end.getHours() == this.firstHour && date_end.getMinutes() == '0') {
          date_end = new Obm.DateTime(date_begin_ts+(this.meeting_slots/this.unit)*3600*1000);
        }
        $('duration').value = (date_end.getTime() - date_begin_ts)/1000;

        ts = null;
        date_begin = null;
        date_begin_ts = null;
        date_end = null;
      }.bind(this)

    });

    // very crappy IE fix 
    // FIXME (David)
    this.slider.element.removeEvents();
    this.slider.element.addEvent('mousedown', function(event) {
      var dir = this.range < 0 ? -1 : 1;
      if (IE4) {
        var position = event.page[this.slider.axis] + (-this.slider.element.getLeft()-$('calendarFreeBusyScroll').scrollLeft) - this.slider.half;
      } else {
        var position = event.page[this.slider.axis] - this.slider.element.getPosition()[this.slider.axis] - this.slider.half;
      }
      position = position.limit(-this.slider.options.offset, this.slider.full -this.slider.options.offset);
      this.slider.step = Math.round(this.slider.min + dir * this.slider.toStep(position));
      this.slider.checkStep();
      this.slider.end();
      this.slider.fireEvent('tick', position);
    
      dir = null;
      position = null;
    }.bind(this));

    // Meeting resizer: change duration
    if (!readOnly) {
      $('calendarFreeBusyMeeting').makeResizable({
        handle: $('calendarFreeBusyResizeMeeting'),
        grid: {'x' : this.slider.stepWidth},
        limit: {
          'x': [this.slider.stepWidth],
          'y': [$('calendarFreeBusyGrid').offsetHeight, $('calendarFreeBusyGrid').offsetHeight]
        },
        onBeforeStart: function() {
          this.slider.drag.detach();
        }.bind(this),
        onDrag: function() {

          $('calendarFreeBusyMeeting').setStyles({
            'height':$('calendarFreeBusyGrid').offsetHeight+'px'
          });

          this.meeting_slots = Math.round($('calendarFreeBusyMeeting').offsetWidth/this.slider.stepWidth);

          $('calendarFreeBusyResizeMeeting').setStyles({
            'margin-left' : $('calendarFreeBusyMeeting').offsetWidth-$('calendarFreeBusyResizeMeeting').offsetWidth+'px'
          });

          this.changeStatus(this.isBusy(this.currentPosition+this.meeting_slots-1));
          this.displayMeetingInfo();
        }.bind(this),
        onComplete:function() {
          var date_begin_ts = this.ts[this.currentPosition]*1000;
          var date_end = new Obm.DateTime(this.ts[this.currentPosition+this.meeting_slots]*1000);
          if (date_end.getHours() == this.firstHour && date_end.getMinutes() == '0') {
            date_end = new Obm.DateTime(date_begin_ts+(this.meeting_slots/this.unit)*3600*1000);
          }
          $('duration').value = (date_end.getTime() - date_begin_ts)/1000;
          this.slider.drag.attach();

          date_begin_ts = null;
          date_end = null;
        }.bind(this)     
      });
    }
    $('calendarFreeBusyMeeting').setOpacity(.5);
    $('calendarFreeBusyMeeting').setStyles({
      'position': 'absolute',
      'top' : $('calendarHead').offsetHeight+'px'
    });
    $('calendarFreeBusyResizeMeeting').setStyles({
      'margin-left' : $('calendarFreeBusyMeeting').offsetWidth-$('calendarFreeBusyResizeMeeting').offsetWidth+'px'
    });

    // Auto Scroll
    this.autoScroll = new Fx.Scroll($('calendarFreeBusyScroll'), {
      offset: {'x':-this.oneDayWidth/2, 'y':-$('calendarHead').offsetHeight} // Offset y : for ie7 !
    });

    // Navigation Scroll
    var scroll = new Fx.Scroll($('calendarFreeBusyScroll'));
    $('scrollLeft').addEvent('click', function() {
      scroll.start($('calendarFreeBusyScroll').scrollLeft-this.oneDayWidth/2, 0); // - 1/2 day
    }.bind(this));
    $('scrollRight').addEvent('click', function() {
      scroll.start($('calendarFreeBusyScroll').scrollLeft+this.oneDayWidth/2, 0); // + 1/2 day
    }.bind(this));

    // Scroll to the right position
    this.initPosition();

    if ($('new_event_form')) {
      var qstring = $('new_event_form').toQueryString().split("&");

      qstring.each(function(e) {
        var input = e.split("=");
        if (input[0] != 'action' && 
            input[0] != 'tf_date_begin' && 
            input[0]!= 'tf_date_end' && 
            input[0]!= 'sel_time_begin' && 
            input[0]!= 'sel_min_begin' && 
            input[0]!= 'sel_time_end' && 
            input[0]!= 'sel_min_end' && 
            input[0]!= 'sel_user_id[]' && 
            input[0]!= 'sel_resource_id[]' &&
            input[0] != 'tf_others_attendees[]') {
          var name = input[0];
          var value = input[1];
          var hidden = new Element('input').setProperties({
            'type':'hidden',
            'name':name,
            'value':decodeURIComponent(value)
          });
          hidden.injectInside($('freeBusyFormId'));
        }
        if (input[0] == 'action') {
          if (input[1] == 'update') {
          $('freebusy_action').value = 'detailupdate';
          }
        } 
      });
      input = null;
      qstring = null;
      name = null;
      value = null;
      hidden = null;
    }

    $('external_contact').addEvent('keypress', function(e) {
      switch(e.key) {
        case 'enter':
          this.addOtherAttendee();
          break;
      }
    }.bind(this));

    if (readOnly) {
      $$('.readable').each(function(e) {
        e.setStyle('display', 'none');
      });
      this.slider.knob.removeEvents();
      this.slider.element.removeEvents();
      $('formClose').setStyle('display', '');
    }

    this.destroyAction();
  },

  destroyAction: function() {
    $$('.close').each(function(e) {
      e.addEvent('click', function() {
        obm.calendarFreeBusy = null;
      });
    });
  },

  /*
   * Display meeting start and end date
   */
  displayMeetingInfo: function() {
    var date_begin_ts = this.ts[this.currentPosition]*1000;
    var date_begin = new Obm.DateTime(date_begin_ts);
    $('meeting_start').innerHTML = date_begin.format(obm.vars.regexp.dateFormat + ' h:i');
    var date_end = new Obm.DateTime(this.ts[this.currentPosition+this.meeting_slots]*1000);
    if (date_end.getHours() == this.firstHour && date_end.getMinutes() == '0') {
      date_end = new Obm.DateTime(date_begin_ts+(this.meeting_slots/this.unit)*3600*1000);
    }
    $('meeting_end').innerHTML = date_end.format(obm.vars.regexp.dateFormat + ' h:i');
    date_begin_ts = null;
    date_begin = null;
    date_end = null;
  },

  /*
   * Check if selected slot is busy or not
   */
  isBusy: function(end_pos) {
    for(var i=this.currentPosition;i<=end_pos;i++) {
      var index = this.bts.indexOf(i+'');
      if (index != -1) {
        var i = null;
        var index = null;
        return true;
      }
    }
    var i = null;
    var index = null;
    return false;
  },

  /*
   * Set meeting style
   */
  changeStatus: function(isBusy) {
    if(isBusy) {
      $('calendarFreeBusyMeeting').addClass('meetingBusy');        
    } else {
      $('calendarFreeBusyMeeting').removeClass('meetingBusy');        
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
      // this.autoScroll.toElement(this.meeting); // Doesn't work on IE !
      this.autoScroll.set((this.currentPosition-10)*this.stepSize); // crappy IE fix
    }
    initialPosition = null;
    end_pos = null;
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
      // this.autoScroll.toElement(this.meeting); // Doesn't work on IE !
      this.autoScroll.set((this.currentPosition-10)*this.stepSize); // crappy IE fix
    }
    initialPosition = null;
    end_pos = null;
  },

  /*
   * Initialize meeting position
   */
  initPosition: function() {
    this.currentPosition = this.ts.indexOf(''+this.timestamp);
    this.slider.set(this.currentPosition);
    this.autoScroll.toElement($('calendarFreeBusyMeeting'));
  },

  /*
   * Add an attendee dynamically
   */
  addAttendee: function(entities, label) {
    var data = new Object();
    data.sel_user_id = new Array();
    data.sel_contact_id = new Array();
    data.sel_resource_id = new Array();
    data.date = this.date;
    entities.each(function(entity) {
      var s = entity.split('-');
      var kind = s[1];
      var id = s[2];
      if (!$(kind+'-'+id)) { // Check if attendee already in free-busy interface
        if (kind == 'resource') {
          data.sel_resource_id.push(id);
        } else if (kind == 'user') {
          data.sel_user_id.push(id);
        } else if (kind == 'group') {
          this.addUserGroup(id).each(function(u) {
            data.sel_user_id.push(u.id);
          });
        } else if (kind == 'resourcegroup') {
          this.addResourceGroup(id).each(function(r) {
            data.sel_resource_id.push(r.id);
          });
        } else if (kind == 'others_attendees') {
          // Calendar is not available for "contact" and "others attendees"
          var attendee = 'others_attendees-'+id;
          var tr = this.buildNotAvailableCalendar(attendee);
          this.displayAttendee(attendee, kind, tr, label);
          $('fbc').getParent().setStyle('top', $('fbc').getParent().offsetTop-8+'px');
          this.external_contact_count++;
        } else if (kind == 'contact') {
          data.sel_contact_id.push(id);
        }
      }
    }.bind(this));

    if (data.sel_user_id.length > 0 || data.sel_resource_id.length > 0 || data.sel_contact_id.length > 0) {
      new Request.JSON({
        url: obm.vars.consts.calendarUrl,
        secure: false,
        onRequest: function() {
          $('spinner').setStyle('display', 'block');
        },
        onComplete : function(response) {
          response.listEvents.each(function(resp) {
            var kind = resp.entity;
            var attendee = kind+'-'+resp.entity_id;
            var label = resp.entity_label;
            if (!$(attendee)) {
              if (!resp.data.canRead) {
                var tr = this.buildNotAvailableCalendar(attendee);
              } else {
                var events = resp.data.events;
                if (events.length > 0) {
                  var tr = new Element('tr').setProperty('id', attendee).addClass('oneAttendee');
                  var entitySlot = new Array();
                  var events = resp.data.events;
                  this.ts.each(function(slot) {
                    entitySlot[slot] = new Array();
                    var tip = '<table>';
                    events.each(function(e) {
                      if (e != null) {
                        var begin = e.event.begin;
                        var end = e.event.end;
                        var time = e.event.time;
                        var title = '';
                        if (e.event.meeting) {
                          title += '<img src="'+obm.vars.images.ico_meeting+'" alt="[Meeting]"/> '; 
                        }
                        if (e.event.private) {
                          title += '<img src="'+obm.vars.images.ico_private+'" alt="[Private]"/> '; 
                        }
                        if (e.event.periodic) {
                          title += '<img src="'+obm.vars.images.ico_periodic+'" alt="[Periodic]"/> '; 
                        }
                        if (e.event.allday) {
                          title += '<img src="'+obm.vars.images.ico_allday+'" alt="[All day]"/> '; 
                        }
                        title += e.event.title;
                        if (begin <= slot && slot < end) {
                          entitySlot[slot].push(time);
                          if (e.event.allday) {
                            tip += '<tr><td class="B">'+obm.vars.labels.allday+'</td><tr>';
                          } else {
                            tip += '<tr><td class="B">'+new Obm.DateTime(begin*1000).format('h:i')+
                              ' - '+new Obm.DateTime(end*1000).format('h:i')+'</td><tr>';
                          }
                          tip += '<tr><td><ul>'+title+'</ul></td></tr>';
                          $(slot).addClass('haveEventAll');
                          this.bts.push(''+this.ts.indexOf(''+slot));
                        }
                        if (slot == end) {
                          events.erase(e);
                        }
                        begin = null;
                        end = null;
                        time = null;
                        title = null;
                      }
                    }.bind(this));
                    tip += '<table>';

                    if (entitySlot[slot].length > 0) {
                      // timeSlot is busy 
                      var td = new Element('td').addClass('timeSlot')
                        .setProperty('title', tip).adopt(new Element('div').addClass('haveEvent '+slot));
                      tr.adopt(td); 
                      if (!this.attendeesSlot[attendee]) this.attendeesSlot[attendee]=new Array();
                      this.attendeesSlot[attendee].push(slot);
                      obm.tip.add(td);

                      tip = null;
                      td = null;
                    } else {
                      // timeSlot is free
                      tr.adopt(new Element('td').addClass('timeSlot')); 
                    }

                  }.bind(this));
                } else {
                  var tr = this.buildNoEvents(attendee);
                }
              }

              this.displayAttendee(attendee, kind, tr, label);

              tr = null;
            }
          }.bind(this));

          // Fix popup top position
          $('fbc').getParent().setStyle('top', $('fbc').getParent().offsetTop-(response.listEvents.length*8)+'px');

          // Set meeting color
          this.changeStatus(this.isBusy(this.currentPosition+this.meeting_slots-1));

          attendee = null;
          label = null;
        }.bind(this),

        onSuccess: function() {
          this.fixHeight();
          $('spinner').setStyle('display', 'none');
        }.bind(this),

        onFailure: function() {
          $('spinner').setStyle('display', 'none');
        }
      }).post($merge({ajax : 1, action : 'add_freebusy_entity'}, data));
    }
  },

  /*
   * Add new attendee
   */
  addOtherAttendee: function(email) {
    var emailRegexp = /^[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}$/;
    if (email == null) {
      email = $('external_contact').value;
    }
    if (email != '') {
      if(emailRegexp.exec(email)) {
        this.addAttendee(['data-others_attendees-'+this.external_contact_count], email);
        this.external_contact_count ++;
        $('external_contact').value = "";
      } else {
        alert(obm.vars.labels.invalidEmail+' ('+email+')');
      }
    }
    emailRegexp = null;
  },

  /*
   * Add users from a group
   */
  addUserGroup: function(group_id) {
    data = new Object();
    data.group_id = group_id;
    var users = new Array();
    new Request.JSON({
      url: obm.vars.consts.obmUrl+'/group/group_index.php',
      secure: false,
      async: false,
      onComplete: function(r) {
        users = r.users;
      }
    }).post($merge({ajax : 1, action : 'get_json_user_group'}, data));
    return users;
  },

  /*
   * Add resources from a resource group
   */
  addResourceGroup: function(res_id) {
    data = new Object();
    data.res_id = res_id;
    var resources = new Array();
    new Request.JSON({
      url: obm.vars.consts.obmUrl+'/resourcegroup/resourcegroup_index.php',
      secure: false,
      async: false,
      onComplete: function(r) {
        resources = r.resources;
      }
    }).post($merge({ajax : 1, action : 'get_json_resource_group'}, data));
    return resources;
  },


  displayAttendee: function(attendee, kind, tr, label) {
    var sel_attendee_id = this.addToAttendeePanel(attendee, label, kind, label);
    $('sel_attendees_id').adopt(sel_attendee_id);
    var elems = $$('.'+kind);
    if (elems.length>1) {
      var sorted = elems.sort(function(a, b) {
        var label_a = a.get('text').toLowerCase().trim();
        var label_b = b.get('text').toLowerCase().trim();
        if (label_a < label_b) return -1;
        if (label_a > label_b) return 1;
        return 0;
      });

      var newPosition = sorted.indexOf(sel_attendee_id);
      if (newPosition == 0) {
        sel_attendee_id.injectBefore(sorted[1]);
        tr.injectBefore($(kind+'-'+sorted[1].id.split('-')[3]));
      } else {
        if ($(kind+'-'+sorted[newPosition-1].id.split('-')[3])!=null) {
          sel_attendee_id.injectAfter(sorted[newPosition-1]);
          tr.injectAfter($(kind+'-'+sorted[newPosition-1].id.split('-')[3]));
        } else {
          $('calendarFreeBusyGrid').adopt(tr);
        }
      }
 
      sorted = null;
      newPosition = null;
      
    } else {
      // It's the first attendee
      $('calendarFreeBusyGrid').adopt(tr);
    }
    elems = null;

    this.addToAttendeeForm(attendee, label, kind);
    this.fixHeight();
  },

  addToAttendeePanel: function(attendee, label, kind, label) {
    var klass = kind;
    if (kind == 'resource') {
      var ico = obm.vars.images.ico_resource;
    } else if (kind == 'user') {
      var ico = obm.vars.images.ico_user;
    } else if (kind == 'contact' || kind == 'others_attendees') {
      var ico = obm.vars.images.ico_contact;
    }
    var div = new Element('div').setProperty('id', 'sel_attendees_id-data-'+attendee).addClass('elementRow '+klass);
    new Element('img').setProperty('src', ico).injectInside(div); 
    new Element('img').setProperty('src',obm.vars.images.del).addEvent('mousedown', function() {
      $(attendee).destroy();
      $('sel_attendees_id-data-'+attendee).destroy();
      $('tf_'+attendee).destroy();
      if (this.attendeesSlot[attendee]) {
        this.attendeesSlot[attendee].each(function(e) {
          if (!$$('div.'+e).length) {
            $(e).removeClass('haveEventAll');
            this.bts.erase(''+this.ts.indexOf(e));
          }
        }.bind(this));
        this.attendeesSlot[attendee].empty();
      }
      this.fixHeight();
    }.bind(this)).injectInside(div);

    div.appendText(label.trim());

    return div;
  },

  addToAttendeeForm: function(attendee, label, kind) {
    if (kind == 'others_attendees') {
      var input = new Element('input').setProperties({
        'id' : 'tf_'+attendee, 
        'type' : 'hidden',
        'name': 'others_attendees[]',
        'value' : label 
      }).addClass('currentOtherAttendee');
    } else {
      if (kind == 'contact') kind = 'user';
      var input = new Element('input').setProperties({
        'id' : 'tf_'+attendee,
        'type' : 'hidden',
        'name': kind+'_id[]',
        'value' : 'data-'+attendee 
      }).addClass('currentAttendee');
    }
    $('freeBusyFormId').adopt(input);
    input = null;
  },

  buildNotAvailableCalendar: function(attendee) {
    var tr = this.timeSlotNoCalendar.clone();
    tr.setProperties({'id':attendee, 'title':obm.vars.labels.calendar_not_available});
    return tr;
  },
  
  buildNoEvents: function(attendee) {
    var tr = this.timeSlotNoEvents.clone();
    tr.setProperties({'id':attendee});
    return tr;
  },

  fixHeight: function() {
    $('calendarFreeBusyScroll').setStyles({'height':$('calendarFreeBusyTable').offsetHeight+16+'px'});
    $('calendarFreeBusyMeeting').setStyles({'height':$('calendarFreeBusyTable').offsetHeight+16+'px'});
  }

});
