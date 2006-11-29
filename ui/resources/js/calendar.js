Obm.CalendarEvent = new Class({
  
  initialize: function(el,id,time,duration,title) {
    this.element = $(el);
    this.id = id;
    this.title = title;
    this.time = time;
    this.duration = duration;


  },

  
})

Obm.CalendarManager = new Class({
  initialize: function() {
    this.events = new Object();
  },

  newEvent: function(id,time,duration,title) {
    this.events['event-'+id+'-'+time] = new Obm.CalendarEvent('event-'+id+'-'+time,id,time,duration,title);
  }

  
});

obm.calendarManager = new Obm.CalendarManager();
