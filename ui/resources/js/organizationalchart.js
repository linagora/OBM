/******************************************************************************
* Organizational Chart popup user detail
******************************************************************************/
Obm.UserDetail = new Class({
  initialize: function() {
    this.popup = $('userdetail');
    this.username = $('userdetail_name');
    this.userphoto = $('userdetail_photo');
    this.usertitle = $('userdetail_title');
    this.userphone = $('userdetail_phone');
    this.usermphone = $('userdetail_mphone');
    this.useremail = $('userdetail_email');
    
    this.nophoto = new Element('img').setProperty('src',obm.vars.images.nophoto);
    // this.ico_load = new Element('img').setProperty('src',obm.vars.images.load);

    this.popup.setStyle('position','absolute');
    this.popup.setStyle('display','none');
  },

  sendDetailEvent: function(user_id) {
    ajax = new Ajax('organizationalchart_index.php',
    {postBody:'ajax=1&action=userdetail&user_id='+user_id, onComplete: this.receiveDetailEvent, method: 'post'}).request();
  },
  
  receiveDetailEvent: function(request) {
    try {
      var resp = eval(request);
    } catch (e) {
      resp = new Object();
      resp.error = 1;
      resp.message = 'Fatal server error, please reload';
    }
    if(resp.error == 0) {
       obm.userDetail.setFormValues(resp);
    } else {
      showErrorMessage(resp.message);
    }
  },

  setFormValues: function(userData) {
    this.username.setHTML(userData.name);
    photo = userData.photo;
    if (photo != "") {
      this.userphoto.setHTML("&nbsp;<img src=\""+obm.vars.path+"/document/document_index.php?action=accessfile&document_id="+userData.photo+"\" alt=\"[Photo]\" />");
    } else {
      this.userphoto.setHTML("&nbsp;<img src=\"/images/themes/default/images/ico_nophoto.png\" alt=\"[No Photo]\" />");
      //this.userphoto.replaceWith(this.nophoto);
    }
    this.usertitle.setHTML(userData.title);
    this.userphone.setHTML(userData.phone);
    this.usermphone.setHTML(userData.mphone);
    this.useremail.setHTML(userData.email);
  },

  compute: function (user_id, evt, item) {
    if(this.popup.getStyle('display') == 'none') {
      this.userphoto.setHTML("&nbsp;<img src=\"/images/themes/default/images/ico_load.gif\" alt=\"[Photo]\" />&nbsp;");
      //this.userphoto.replaceWith(this.ico_load);
      this.sendDetailEvent(user_id);
      this.show();

      var currentX, currentY;	
      var target = $(item);
      var windowWidth = window.innerWidth;
      if (IE4) {
        evt = window.event;
        windowWidth = document.body.clientWidth;
      }
      if ( W3C ) {
        currentX = evt.clientX - this.popup.offsetWidth/2;
        currentY = evt.clientY - this.popup.offsetHeight - 15;
      } else if ( NS4 ) {
        currentX = evt.pageX - this.popup.offsetWidth/2;
        currentY = evt.pageY - this.popup.offsetHeight - 15;
      } else {
        windowWidth = document.documentElement.clientWidth;
        currentX = evt.clientX - this.popup.offsetWidth/2;
        currentY = evt.clientY - this.popup.offsetHeight - 15;
      }

      var popupLeft = currentX + this.popup.offsetWidth;
      if (popupLeft > windowWidth) {
        currentX = currentX - (popupLeft - windowWidth) - 20;
      }
      var top = target.getTop() - this.popup.offsetHeight - Math.round(target.offsetHeight);

      this.popup.setStyle('top', top+'px');
      this.popup.setStyle('left', currentX+'px');

    } else {
      this.hide();
    }
  },

  show: function() {
    this.popup.setStyle('display', 'block');
  },

  hide: function() {
    this.popup.setStyle('display', 'none');
  }


});
