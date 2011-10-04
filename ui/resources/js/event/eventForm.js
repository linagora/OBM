Obm.Event = {}
Obm.Event.Form = new Class ({
  options: {
    organizerAttendeesContainerId: "sel_user_id",
    organizerSelector: 'select[name="sel_organizer"]',
    removeImgSrc: '/images/themes/default/images/ico_trash.gif',
    removeLabel: '[Delete]'
  },

  initialize: function(){
    if($$("select[name=sel_organizer]") != ""){
      this.organizerList = $$(this.options.organizerSelector);
      this.organizerList.setProperty('formerValue', this.organizerList.get("value"));
      this.organizerList.addEvent('change', this.switchAttendees.bind(this));

      var attendeeId = this.getSelectedOrganizerValue();
      $("sel_user_id-data-user-"+attendeeId).setProperty("automatic", "true");
    }
  },

  switchAttendees: function(organizerList){
    var organizerId = this.getSelectedOrganizerValue();
    var organizerName = this.getSelectedOrganizerName();

    this.addOrganizerAttendee(organizerId, organizerName);
    this.automaticOrganizerAttendeeRemove(this.getLastOrganizerId());
    this.setLastOrganizerId(organizerId);
  },

  addOrganizerAttendee: function(attendeeId, attendeeName){
    if(!this.isPresentAttendee(attendeeId)){
      attendeeDiv = this.getDivAttendee(attendeeId, attendeeName);
      attendeeDiv.inject($(this.options.organizerAttendeesContainerId));
    }
  },

  isPresentAttendee: function(attendeeId){
    return $("sel_user_id-data-user-"+attendeeId) != null;
  },

  removeOrganizerAttendee: function(attendeeId){
    var attendeeToRemove = $('sel_user_id-data-user-'+attendeeId);
    if(attendeeToRemove != null){
      $('sel_user_id-data-user-'+attendeeId).dispose();
    }
  },

  automaticOrganizerAttendeeRemove: function(attendeeId){
    var organizerIdToRemove = $("sel_user_id-data-user-"+attendeeId);
    var automaticallyAdded = organizerIdToRemove.get("automatic");
    if(automaticallyAdded == "true"){
      this.removeOrganizerAttendee(attendeeId);
    }
  },

  getDivAttendee: function(attendeeId, attendeeName){
    var attendeeDiv = new Element( 'div' ,
                                   { id : 'sel_user_id-data-user-'+attendeeId,
                                     automatic: "true",
                                     "class": "elementRow"
                                   }
                                 );
    var removeAttendeeLink = this.getRemoveOrganizerAttendeeLink(attendeeId);
    var attendeeHidden = this.getAttendeeHiddenInfo(attendeeId);

    removeAttendeeLink.inject(attendeeDiv);
    attendeeDiv.appendText(" "+attendeeName);
    attendeeHidden.inject(attendeeDiv);

    return attendeeDiv;
  },
  
  getRemoveOrganizerAttendeeLink: function(attendeeId){
    var removeLink = new Element('a');
    var removeImage = new Element('img', {
      alt: this.options.removeLabel,
      src: this.options.removeImgSrc
    });
    removeImage.inject(removeLink);
    removeLink.addEvent('click', this.removeOrganizerAttendee.bind(this, attendeeId));
    
    return removeLink;
  },

  getAttendeeHiddenInfo: function(attendeeId){
    return new Element( 'input',
                        {
                          type: 'hidden',
                          name: "sel_user_id[]",
                          value: "data-user-"+attendeeId
                        }
                      );
  },

  getSelectedOrganizerName: function(){
    return $$(this.organizerList.getSelected()).get("text");
  },

  getSelectedOrganizerValue: function(){
    return $$($$('select[name=sel_organizer]').getSelected()).get("value");
  },

  getLastOrganizerId: function(){
    return this.organizerList.get("formerValue");
  },

  setLastOrganizerId: function(id){
    this.organizerList.setProperty('formerValue', id);
  }
});