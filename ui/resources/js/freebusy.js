/*
 * FreeBusy interface
 */
Obm.CalendarFreeBusy = new Class({

  /*
   * Initialize attributes
   */
  initialize: function(time_slots, unit, day_first_hour, day_last_hour) {
    this.eventStartDate = new Obm.DateTime(obm.vars.consts.begin_timestamp*1000);
    this.hourOffset = parseInt(day_first_hour,10) - this.eventStartDate.getHours();
    this.displayEventStartDate = this.getDisplayEventStartDate(parseInt(day_first_hour));
    this.unit = unit;
    this.stepSize = 40/this.unit;
    this.external_contact_count = 0;
    this.nbSteps = time_slots.length;
    this.ts = time_slots;
    this.meeting_slots = this.unit;
    this.oneDayWidth = this.stepSize*this.nbSteps/7; // in px
    this.currentPosition = 0;
    //$('fbcContainer').setStyle('width', $(document.body).offsetWidth - 100 +'px');
    if (IE4) {
      $('calendarFreeBusyScroll').setStyle('width', $(document.body).offsetWidth - 400 +'px'); // FIXME: POPUP SIZE 
    } else {
      $('fbcContainer').setStyle('width', $(document.body).offsetWidth - 100 +'px');
    }

    // first hour of a day displayed on the grid (eg, 08:00) - events before this time
    // are not shown
    this.firstHour = day_first_hour;
    // last hour of a day displayed on the grid (eg, 20:00) - events after this
    // time are not shown
    this.lastHour = day_last_hour;
    this.duration = 1;
  },

  getDisplayEventEndDate: function(duration) {
    var endDate = new Obm.DateTime(this.displayEventStartDate);
    if ((this.displayEventStartDate.getHours() + duration) > this.lastHour) {
      endDate.setHours(this.lastHour);
    } else {
      endDate.addHours(duration);
    }
    return endDate;
  },

  getDisplayEventStartDate: function(firstHour) {
    var startDate = new Obm.DateTime(this.eventStartDate);
    if (this.hourOffset > 0){
      startDate.setHours(parseInt(firstHour,10));
    }
    return startDate;
  },

  /**
   * Normalizes a datetime so that it fits in a time slot (makes sure its hours
   * are between firstHour and lastHour). It is an "almost displayable" date,
   * because it may return a date set at lastHour (if the hours of the dateTime
   * parameters are >= to lastHour), and there is no time slot at this point.
   */
  getAlmostDisplayableDateTime: function(dateTime) {
    var firstSlotTimestampInSeconds = parseInt(this.ts[0], 10);
    var lastSlotTimestampInSeconds = parseInt(this.ts[this.ts.length - 1], 10);

    var dateTimeTimestampInSeconds = dateTime.getTime() / 1000;
    var normalizedDateTimeTimestampInSeconds = Math.min(
            Math.max(dateTimeTimestampInSeconds, firstSlotTimestampInSeconds),
            lastSlotTimestampInSeconds);
    return new Obm.DateTime(normalizedDateTimeTimestampInSeconds * 1000);
  },

  /**
   * Converts a date time to a position in the time slot array, or returns
   * null
   */
  dateTimeToTimeSlot: function(dateTime) {
    var timestampInSeconds = dateTime.getTime() / 1000;
    var maybeTimeSlot = this.ts.indexOf(timestampInSeconds.toString());
    return maybeTimeSlot != -1 ? maybeTimeSlot : null;
  },

  /**
   * Counts the (absolute) number of time slots between two dates. Throws an
   * error if things don't work out.
   */
  timeSlotCountBetween: function(dateTime1, dateTime2) {
    var isDateTime2GreaterThanDateTime1 = dateTime2.getTime() > dateTime1.getTime();

    var earlierDateTime;
    var laterDateTime;
    if (isDateTime2GreaterThanDateTime1) {
      earlierDateTime = dateTime1;
      laterDateTime = dateTime2;
    }
    else {
      earlierDateTime = dateTime2;
      laterDateTime = dateTime1;
    }

    var displayableEarlierDateTime = this.getAlmostDisplayableDateTime(earlierDateTime);
    var almostDisplaybleLaterDateTime = this.getAlmostDisplayableDateTime(laterDateTime);
    if (displayableEarlierDateTime.getTime() == almostDisplaybleLaterDateTime.getTime()) {
      return 0;
    }
    var hoursToAdd;
    var displayableLaterDateTime;
    // If the normalized later date time comes just after the last available time slot,
    // substract one hour, we'll make up for it later
    if (almostDisplaybleLaterDateTime.getHours() == this.lastHour) {
      hoursToAdd = 1;
      displayableLaterDateTime = new Obm.DateTime(almostDisplaybleLaterDateTime).addHours(-1);
    }
    else {
      displayableLaterDateTime = almostDisplaybleLaterDateTime;
      hoursToAdd = 0;
    }
    var earlierTimeSlot = this.dateTimeToTimeSlot(displayableEarlierDateTime);
    if (earlierTimeSlot == null) {
      throw new Error("No time slot available for the date time " + earlierDateTime.format("c") + " (normalized as " + displayableEarlierDateTime.format("c") + ")");
    }
    var laterTimeSlot = this.dateTimeToTimeSlot(displayableLaterDateTime);
    if (laterTimeSlot == null) {
      throw new Error("No time slot available for the date time " + laterDateTime.format("c") + " (normalized as " + displayableLaterDateTime.format("c") + ")");
    }
    // Number of time slots we need to compensate
    var timeSlotsToAdd = hoursToAdd * this.unit;
    return (laterTimeSlot - earlierTimeSlot) + timeSlotsToAdd;
  },

  /*
   * build panel
   * Build meeting slider, meeting resizer
   */
  buildFreeBusyPanel: function(duration, readOnly) {

    this.duration = parseInt(duration, 10);
    var durationDisplay = (this.hourOffset > 0) ?  (duration - this.hourOffset) : duration;
    $('duration').value = this.duration*3600;

    var eventEndDate = this.getDisplayEventEndDate(durationDisplay);
    this.meeting_slots = this.timeSlotCountBetween(eventEndDate, this.displayEventStartDate);

    // /!\ meeting width must be set BEFORE slider
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
        var date_end = new Obm.DateTime(date_begin_ts+(this.meeting_slots/this.unit)*3600*1000);
      }.bind(this)

    });

    // very crappy IE fix 
    // FIXME (David)
    this.slider.element.removeEvents();
    this.slider.element.addEvent('mousedown', function(event) {
      var dir = this.range < 0 ? -1 : 1;
      if (IE4) {
        var position = event.page[this.slider.axis] + (-this.slider.element.getLeft()-$('calendarFreeBusyScroll').scrollLeft);
      } else {
        var position = event.page[this.slider.axis] - this.slider.element.getPosition()[this.slider.axis];
      }
      position = position.limit(-this.slider.options.offset, this.slider.full -this.slider.options.offset);
      this.slider.step = Math.round(this.slider.min + dir * this.slider.toStep(position));
      this.slider.checkStep();
      this.slider.end();
      this.slider.fireEvent('tick', position);
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
          var date_end = new Obm.DateTime(date_begin_ts+(this.meeting_slots/this.unit)*3600*1000)
          this.duration = (date_end.getTime() - date_begin_ts)/3600000;
          $('duration').value = this.duration*3600;
          
          this.slider.drag.attach();
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
          this.addAttendee('data-others_attendees-'+$('external_contact').value);
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

  },


  /*
   * Show next week
   */
  showNext: function() {
    this.eventStartDate.setDate(this.eventStartDate.getDate()+7);
    this.refresh();
  },


  /*
   * Show previous week
   */
  showPrev: function() {
    this.eventStartDate.setDate(this.eventStartDate.getDate()-7)
    this.refresh();
  },


  /*
   * Refresh freebusy 
   */
  refresh: function() {
    var data = new Object();
    data.date_begin = obm.calendarFreeBusy.eventStartDate.format('c');
    data.sel_user_id = obm.calendarFreeBusy.entities.sel_user_id;
    data.sel_resource_id = obm.calendarFreeBusy.entities.sel_resource_id;
    data.sel_resource_group_id = obm.calendarFreeBusy.entities.sel_resource_group_id;
    data.sel_group_id = obm.calendarFreeBusy.entities.sel_group_id;
    data.sel_contact_id = obm.calendarFreeBusy.entities.sel_contact_id;
    data.others_attendees = obm.calendarFreeBusy.entities.others_attendees;
    var duration = obm.calendarFreeBusy.duration;

    new Request.HTML({
      url: obm.vars.consts.calendarUrl,
      secure : false,
      evalScripts : true,
      update: $('fbc'),
      onRequest: function() {
        $('spinner').show();
      },
      onComplete: function() {
        obm.popup.show('fbcContainer');
        obm.calendarFreeBusy.buildFreeBusyPanel(duration, false);      
        $('spinner').hide();
      }
    }).get($merge({ajax : 1, action : 'perform_meeting'}, data));    
  },


  /*
   * Add an attendee
   */
  addAttendee: function(attendee) {
    if (typeof attendee == 'string') 
        attendee = attendee.split(',');
    
    for(var i = 0; i < attendee.length; i++) {
      var a = attendee[i].split('-');
      if (a[1] == 'user') obm.calendarFreeBusy.entities.sel_user_id.push(a[2]);
      else if (a[1] == 'resource') obm.calendarFreeBusy.entities.sel_resource_id.push(a[2]);
      else if (a[1] == 'contact') obm.calendarFreeBusy.entities.sel_contact_id.push(a[2]);
      else if (a[1] == 'others_attendees') obm.calendarFreeBusy.entities.others_attendees.push(a[2]);
    }
    this.refresh();
  },


  /*
   * Remove an attendee
   */
  removeAttendee: function(attendee) {
    var a = attendee.split('-');
    if (a[1] == 'user') obm.calendarFreeBusy.entities.sel_user_id.erase(a[2]);
    if (a[1] == 'resource') obm.calendarFreeBusy.entities.sel_resource_id.erase(a[2]);
    if (a[1] == 'contact') obm.calendarFreeBusy.entities.sel_contact_id.erase(a[2]);
    if (a[1] == 'others_attendees') obm.calendarFreeBusy.entities.others_attendees.erase(a[2]);
    this.refresh();
  },


  /*
   * Display meeting start and end date
   */
  displayMeetingInfo: function() {
    var date_begin_ts = this.ts[this.currentPosition]*1000;
    $('meeting_start').innerHTML = new Obm.DateTime(date_begin_ts).format(obm.vars.regexp.dateFormat + ' h:i');
    var date_end = new Obm.DateTime(date_begin_ts+(this.meeting_slots/this.unit)*3600*1000)
    $('meeting_end').innerHTML = date_end.format(obm.vars.regexp.dateFormat + ' h:i');
  },


  /*
   * Check if selected slot is busy or not
   */
  isBusy: function(end_pos) {
    for(var i=this.currentPosition;i<=end_pos;i++) {
      if (this.busySlots.indexOf(i+'') != -1) {
        return true;
      }
    }
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
  },


  /*
   * Initialize meeting position
   */
  initPosition: function() {
    this.currentPosition = this.dateTimeToTimeSlot(this.getAlmostDisplayableDateTime(this.eventStartDate));
    this.slider.set(this.currentPosition);
    this.autoScroll.toElement($('calendarFreeBusyMeeting'));
  }

});
