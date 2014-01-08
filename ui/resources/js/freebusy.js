/*
 * FreeBusy interface
 */
Obm.CalendarFreeBusy = new Class({

  /*
   * Initialize attributes
   */
  initialize: function(time_slots, unit, first_hour) {
    this.eventStartDate = new Obm.DateTime(obm.vars.consts.begin_timestamp*1000);
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

    this.firstHour = first_hour;
    this.duration = 1;
  },


  /*
   * build panel 
   * Build meeting slider, meeting resizer
   */
  buildFreeBusyPanel: function(duration, readOnly) {
    this.duration = duration;
    $('duration').value = this.duration*3600;
    this.meeting_slots = Math.ceil(this.duration*this.unit)//this.ts.indexOf(''+(obm.vars.consts.begin_timestamp+this.duration*3600))-this.ts.indexOf(''+obm.vars.consts.begin_timestamp);

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
    this.currentPosition = this.ts.indexOf(''+obm.vars.consts.begin_timestamp);
    this.slider.set(this.currentPosition);
    this.autoScroll.toElement($('calendarFreeBusyMeeting'));
  }

});
