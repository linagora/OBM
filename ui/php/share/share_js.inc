<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/
$extra_js .= "
function getShareUrl(type,format) {
  var url = \"$cgp_host\";
  var id = $('share_entity_id');
  var type_id = $('share_entity_type');
  new Request.JSON({
        url: '$path/calendar/calendar_index.php',
        secure: false,
        async: true,
        onComplete: function(response) {
          if(response.error == 0){
            showSharePopup(type,format,response.token, response.email)
          } else {
            showMessage('error',response.message);
          }
        }
  }).get({entity_type : type_id.value, entity_id : id.value, type : type, action : 'share', ajax : 1});
}

function sendUrl(type,format) {
  var id = $('share_entity_id');
  var type_id = $('share_entity_type');
  var mail = $('contactSearch').value;
  if(check_email(mail)){
     new Request.JSON({
        url: '$path/calendar/calendar_index.php',
        secure: false,
        async: true,
        onComplete: function(response) {
          if(response.error == 0){
            showMessage('valid',response.message);
          } else {
            showMessage('error',response.message);
          }
        }
    }).post({entity_type : type_id.value, entity_id : id.value, type : type, mail:mail, action : 'send_url', format : format, ajax : 1});
  } else {
    showMessage('error','$GLOBALS[l_invalid_email]');
  }
}

function reinitSharePopup(type){
  var url = \"$cgp_host\";
  var popup = $('sharePopup');
  if(popup){
    obm.popup.popups.erase('sharePopup');
    popup.getParent().dispose().destroy();
  }
  if(type=='private'){
    var title = \"$GLOBALS[l_reinit_calendar_private]\";
    var text = \"$GLOBALS[l_desc_reinit_adress_private]\";
  } else {
    var title = \"$GLOBALS[l_reinit_calendar_public]\";
    var text = \"$GLOBALS[l_desc_reinit_adress_public]\";
  }
  var div = new Element('div').setProperty('id','sharePopup')
                              .setProperty('class','obmPopup SharePopup')
                              .adopt(
      new Element('h1').adopt(
        new Element('a').setProperty('class','closeSticky')
                        .setProperty('id','popup_form_close')
                        .setProperty('onclick','return false')
                        .adopt(
                          new Element('img').setProperty('src','$ico_popup_close')
                                            .setProperty('alt','close')
                          ),
        new Element('span').appendText(title)
      ),
      new Element('h2').appendText(text),
      new Element('div').setProperty('class','buttons')
                      .adopt(
        new Element('input').setProperty('type','submit')
                            .setProperty('value','$GLOBALS[l_yes]')
                            .setProperty('class','closeSticky')
                            .setProperty('onClick','reinitUrl(\"'+type+'\")'),
        new Element('input').setProperty('type','submit')
                            .setProperty('value','$GLOBALS[l_no]')
                            .setProperty('class','closeSticky')
      )
  );
  div.injectInside($('calendarSharePopup'));
  obm.popup.show('sharePopup');
}

function reinitUrl(type) {
  var url = \"$cgp_host\";
  var id = $('share_entity_id');
  var type_id = $('share_entity_type');
  new Request.JSON({
        url: '$path/calendar/calendar_index.php',
        secure: false,
        async: true,
        onComplete: function(response) {
          if(response.error == 0){
            showMessage('info',response.message);
          } else {
            showMessage('error',response.message);
          }
        }
  }).get({entity_type : type_id.value, entity_id : id.value, type : type, action : 'share_reinit', ajax : 1});
}

function showSharePopup(type,format,token, userEmail){
  var url = \"$cgp_host\";
  var popup = $('sharePopup');
  if(popup){
    obm.popup.popups.erase('sharePopup');
    popup.getParent().dispose().destroy();
  }
  var elem = new Element('a');
  if(type=='private'){
    var text = \"$l_share_calendar_private\";
  } else if(type=='public') {
    var text = \"$l_share_calendar_public\";
  }else {
    var text = \"$l_share_calendar_freebusy\";
    url+='calendar/calendar_freebusy_export.php?action=freebusy_export&email='+encodeURI(userEmail);
    elem.setProperty('href',url).appendText(url);
  }
  if(format=='Ics' & type != 'freebusy'){
    url+='calendar/calendar_render.php?action=ics_export&externalToken='+token+'&email='+encodeURI(userEmail);
    elem.setProperty('href',url).appendText(url);
  } else if(format=='Html'){
    url+='calendar/calendar_render.php?externalToken='+token;
    elem.setProperty('href',url).appendText(url);
  }
  var shareurl = new Element('div').setProperty('id','lnk_public_url')
                                      .adopt(elem);
  var div = new Element('div').setProperty('id','sharePopup')
                              .setProperty('class','obmPopup SharePopup')
                              .adopt(
      new Element('h1').adopt(
        new Element('a').setProperty('class','closeSticky')
                        .setProperty('id','popup_form_close')
                        .setProperty('onclick','return false')
                        .adopt(
                          new Element('img').setProperty('src','$ico_popup_close')
                                            .setProperty('alt','close')
                          ),
        new Element('span').appendText(text)
      ),
      new Element('h2').appendText('$l_share_public')
  );
  shareurl.injectInside(div);
  div.injectInside($('calendarSharePopup'));
  obm.popup.show('sharePopup');
}

function sendSharePopup(type,format){
  var url = \"$cgp_host\";
  var popup = $('sharePopup');
  if(popup){
    obm.popup.popups.erase('sharePopup');
    popup.getParent().dispose().destroy();
  }
  if(type=='private'){
    var title = \"$l_send_calendar_private\";
    var text = \"$l_desc_share_adresse_private\";
  } else if(type=='public'){
    var title = \"$l_send_calendar_public\";
    var text = \"$l_desc_share_adresse_public\";
  } else {
    var title = \"$l_send_calendar_freebusy\";
    var text = \"$l_desc_share_adresse_freebusy\";
  }
  var div = new Element('div').setProperty('id','sharePopup')
                              .setProperty('class','obmPopup')
                              .adopt(
      new Element('h1').adopt(
        new Element('a').setProperty('class','closeSticky')
                        .setProperty('id','popup_form_close')
                        .setProperty('onclick','return false')
                        .adopt(
                          new Element('img').setProperty('src','$ico_popup_close')
                                            .setProperty('alt','close')
                          ),
        new Element('span').appendText(title)
      ),
      new Element('div').setProperty('id','sel_accept_contact'),
      new Element('div').setProperty('class','sendPopup')
                      .adopt(
        new Element('input').setProperty('id','contactSearch')
                            .setProperty('type','text')
                            .setProperty('class','downlight')
                            .setProperty('autocomplete','off'),
        new Element('input').setProperty('type','submit')
                            .setProperty('value','$GLOBALS[l_send]')
                            .setProperty('class','closeSticky')
                            .setProperty('onClick','sendUrl(\"'+type+'\",\"'+format+'\")'),
        new Element('h2').appendText(text)
      )
  );
  div.injectInside($('calendarSharePopup'));
  new obm.AutoComplete.ShareCalendarSearch('$GLOBALS[path]/contact/contact_index.php?action=ext_search_mail', 'sel_accept_contact', 'contactSearch', {fieldText:\"$GLOBALS[l_share_mail_search]\"})
  obm.popup.show('sharePopup');
}


";

?>
