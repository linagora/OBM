<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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



///////////////////////////////////////////////////////////////////////////////
// OBM - File  : calendar_js.inc                                             //
//     - Desc  : Agenda javascript functions File                            //
// 2005-01-13 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$extra_js .= "

function checkSearchForm() {
  if (trim($('tf_pattern').value) == '') {
    showErrorMessage('".phpStringToJsString($GLOBALS['l_invalid_data'])."');
    return false;
  }
  return true;
}

function toggleSearchForm(show, hide) {
  $(hide).setStyle('display', 'none');
  $(show).setStyle('display', '');
  if (obm.calendarManager) {
    obm.calendarManager.resizeGrid();
  }
}

function setAdvancedSearchDate(rd) {
  switch(rd.value) {
    case 'today':
      $('tf_date_begin').value = obm.vars.consts.today_begin;
      $('tf_date_end').value = obm.vars.consts.today_end;
     break;
    case 'tomorrow':
      $('tf_date_begin').value = obm.vars.consts.tomorrow_begin;
      $('tf_date_end').value = obm.vars.consts.tomorrow_end;
     break;
    case 'this_week':
      $('tf_date_begin').value = obm.vars.consts.this_week_begin;
      $('tf_date_end').value = obm.vars.consts.this_week_end;
     break;
    case 'next_week':
      $('tf_date_begin').value = obm.vars.consts.next_week_begin;
      $('tf_date_end').value = obm.vars.consts.next_week_end;
     break;
    case 'this_month':
      $('tf_date_begin').value = obm.vars.consts.this_month_begin;
      $('tf_date_end').value = obm.vars.consts.this_month_end;
     break;
    case 'next_month':
      $('tf_date_begin').value = obm.vars.consts.next_month_begin;
      $('tf_date_end').value = obm.vars.consts.next_month_end;
     break;
  }
}

function showNextRights(right, elem) {
  switch(right) {
    case 'access':
      users = obm.vars.consts.accessEntities;
      start = obm.vars.consts.accessOffset;
      container = $('accessRight');
      obm.vars.consts.accessOffset +=obm.vars.consts.rightLimit;
      end = obm.vars.consts.accessOffset;
      break;
    case 'read':
      users = obm.vars.consts.readEntities;
      start = obm.vars.consts.readOffset;
      container = $('readRight');
      obm.vars.consts.readOffset +=obm.vars.consts.rightLimit;
      end = obm.vars.consts.readOffset;
      break;
    case 'write':
      users = obm.vars.consts.writeEntities;
      start = obm.vars.consts.writeOffset;
      container = $('writeRight');
      obm.vars.consts.writeOffset +=obm.vars.consts.rightLimit;
      end = obm.vars.consts.writeOffset;
      break;
    case 'admin':
      users = obm.vars.consts.adminEntities;
      start = obm.vars.consts.adminOffset;
      container = $('adminRight');
      obm.vars.consts.adminOffset +=obm.vars.consts.rightLimit;
      end = obm.vars.consts.adminOffset;
      break;
  }
  container.empty();
  for(i=start;i<end;i++) {
    if (\$chk(users[i])) {
      li = new Element('li');
      li.innerHTML = users[i];
      container.adopt(li);
    } else {
      elem.style.display = 'none';
    }
  }
  $(right+'Prev').style.display = '';
}

function showPrevRights(right, elem) {
  switch(right) {
    case 'access':
      users = obm.vars.consts.accessEntities;
      start = obm.vars.consts.accessOffset-obm.vars.consts.rightLimit*2;
      if (start < 0) start = 0;
      container = $('accessRight');
      end = start + obm.vars.consts.rightLimit;
      obm.vars.consts.accessOffset = end;
      break;
    case 'read':
      users = obm.vars.consts.readEntities;
      start = obm.vars.consts.readOffset-obm.vars.consts.rightLimit*2;
      if (start < 0) start = 0;
      container = $('readRight');
      end = start + obm.vars.consts.rightLimit;
      obm.vars.consts.readOffset = end;
      break;
    case 'write':
      users = obm.vars.consts.writeEntities;
      start = obm.vars.consts.writeOffset-obm.vars.consts.rightLimit*2;
      if (start < 0) start = 0;
      container = $('writeRight');
      end = start + obm.vars.consts.rightLimit;
      obm.vars.consts.writeOffset = end;
      break;
    case 'admin':
      users = obm.vars.consts.adminEntities;
      start = obm.vars.consts.adminOffset-obm.vars.consts.rightLimit*2;
      if (start < 0) start = 0;
      container = $('adminRight');
      end = start + obm.vars.consts.rightLimit;
      obm.vars.consts.adminOffset = end;
      break;
  }
  container.empty();
  for(i=start;i<end;i++) {
    if (\$chk(users[i])) {
      li = new Element('li');
      li.innerHTML = users[i];
      container.adopt(li);
    }
  }
  if (start <= 0) elem.style.display = 'none';
  $(right+'Next').style.display = '';
}



/////////////////////////////////////////////////////////////////////////////
function performMeeting() {

  var data = new Object();
  data.sel_user_id = new Array();
  data.sel_resource_id = new Array();
  data.sel_resource_group_id = new Array();
  data.sel_group_id = new Array();
  data.sel_contact_id = new Array();
  data.date = $('tf_date').value;
  data.others_attendees = new Array();

  $$('div.elementRow').each(function(e) {
    var id = e.id.split('-');
    if(id[2] == 'user') data.sel_user_id.push(id[3]);
    if(id[2] == 'resource') data.sel_resource_id.push(id[3]);
    if(id[2] == 'group') data.sel_group_id.push(id[3]);
    if(id[2] == 'resourcegroup') data.sel_resource_group_id.push(id[3]);
    if(id[2] == 'contact') data.sel_contact_id.push(id[3]);
  });

  var duration = parseInt($('sel_time_duration').value, 10)+parseInt($('sel_min_duration').value, 10)/60;

  display_freebusy(data, duration, false);
}


/////////////////////////////////////////////////////////////////////////////
function checkAvailability() {
  var begin = getFieldDate($('tf_date_begin').value, false);
  begin.setHours(parseInt($('sel_time_begin').value, 10));
  begin.setMinutes(parseInt($('sel_min_begin').value, 10));

  var end = getFieldDate($('tf_date_end').value, false);
  end.setHours(parseInt($('sel_time_end').value, 10));
  end.setMinutes(parseInt($('sel_min_end').value, 10));

  if (begin.getTime() > end.getTime()) {
    alert('$l_err_begin_end');
    return false;
  }

  var data = new Object();
  data.sel_user_id = new Array();
  data.sel_resource_id = new Array();
  data.sel_resource_group_id = new Array();
  data.sel_group_id = new Array();
  data.sel_contact_id = new Array();
  data.date = new Obm.DateTime(begin).format('c');
  data.others_attendees = new Array();

  $$('div.elementRow').each(function(e) {
    var id = e.id.split('-');
    if(id[2] == 'user') data.sel_user_id.push(id[3]);
    if(id[2] == 'resource') data.sel_resource_id.push(id[3]);
    if(id[2] == 'group') data.sel_group_id.push(id[3]);
    if(id[2] == 'resourcegroup') data.sel_resource_group_id.push(id[3]);
    if(id[2] == 'contact') data.sel_contact_id.push(id[3]);
  });
 
  $$('input.otherAttendee').each(function(e) {
    if (e.value != '') data.others_attendees.push(e.value);
  });

  var duration = (end.getTime() - begin.getTime())/3600000;

  display_freebusy(data, duration, false);
}


/////////////////////////////////////////////////////////////////////////////
function displayFreeBusyConflict(duration, date_begin, readOnly) {

  var data = new Object();
  data.sel_user_id = new Array();
  data.sel_resource_id = new Array();
  data.sel_resource_group_id = new Array();
  data.sel_group_id = new Array();
  data.sel_contact_id = new Array();
  data.date = new Obm.DateTime(date_begin*1000).format('c');
  data.others_attendees = new Array();

  $$('div.elementRow').each(function(e) {
    var id = e.id.split('-');
    if(id[2] == 'user') data.sel_user_id.push(id[3]);
    if(id[2] == 'resource') data.sel_resource_id.push(id[3]);
    if(id[2] == 'group') data.sel_group_id.push(id[3]);
    if(id[2] == 'resourcegroup') data.sel_resource_group_id.push(id[3]);
    if(id[2] == 'contact') data.sel_contact_id.push(id[3]);
  });
 
  $$('input.otherAttendee').each(function(e) {
    if (e.value != '') data.others_attendees.push(e.value);
  });

  display_freebusy(data, duration, readOnly);
}


/////////////////////////////////////////////////////////////////////////////
function display_freebusy(data, duration, readOnly) {

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
      obm.calendarFreeBusy.buildFreeBusyPanel(duration, readOnly);      
      if (\$chk($('tf_title'))) $('fb_title').value = $('tf_title').value;
      if (\$chk($('sel_owner'))) $('fb_owner').value = $('sel_owner').value;
      $('spinner').hide();
    }
  }).get(\$merge({ajax : 1, action : 'perform_meeting'}, data));   

}

function add_displayed_attendee(check, attendee, uid, kind, container) {
  if (check) {
    for(var id in attendee) {
      var sel_id = container+'-data-'+kind+'-'+id;
      if (!\$chk($(sel_id))) {
        of_select_add_element(window.document, container, 'data-'+kind+'-'+id, attendee[id]);
        $(sel_id).addClass('x-obm-display-add')
      }
    }
  } else {
    for(var id in attendee) {
      var sel_id = 'sel_'+kind+'_id-data-'+kind+'-'+id;
      if ($(sel_id) != null && $(sel_id).hasClass('x-obm-display-add')) {
        $(sel_id).destroy();
      }
    }
  }
}

function set_autocomplete_container(entity) {
  obm.vars.consts.autocomplete_ext.setSelectedBox('sel_user_id');
  if (entity.match('contact_index.php')) {
    obm.vars.consts.autocomplete_ext.setSelectedBox('sel_contact_id');
  }
}

/////////////////////////////////////////////////////////////////////////////
function check_calendar_pdf_options() {
  if ($('cal_first_hour')) {
    var hour_begin = parseInt($('cal_first_hour').value);
  }
  if ($('cal_last_hour')) {
    var hour_end = parseInt($('cal_last_hour').value);
  }
  var display_days = $('display_days');

  if (hour_end <= hour_begin) {
    alert('$l_err_begin_end');
    return false;
  }

  if (display_days) {
    for(var i=0;i<7;i++) {
      var day = $('cba_repeatday_'+i);
      if (day.getProperty('checked')) {
        return true;
      }
    }
    alert('$l_select_pdf_day');
    return false;
  }

  return true;
}


function sel_public_group(element, label) {
  var item_id = element.getProperty('id').split('-');
  var id = item_id[item_id.length - 1];
  window.location=('calendar_index.php?group_id='+id+'&new_group=1');
}

function display_list_detail(display) {
  slides.each(function(slide) {
    if (slide != null) {
      if (display == 'show') {
        slide.show();
      } else {
        slide.hide();
      }
    }
  });
}

function change_view(url) {
  Cookie.write('calendar_view', url,{path: '/'});
  window.location=url;
}

function check_calendar_calendar(form) {
  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }
  else if (form.tf_date_begin.value == \"\") {
    alert(\"$l_datebegin\" + \" : $l_fill_date\");
    return false;
  }
  /*
  else if (form.tf_date_end.value == \"\") {
    alert(\"$l_dateend\" + \" : $l_fill_date\");
    return false;
  }
  */
  
  if ($('cbx_no_repeatend').checked) {
    $('tf_repeat_end').value = '';
  }
  
  return true;
}


function show_hide_calendar_dates(field, alldayOpacity) {
  target = window.document;
  begin = target.getElementById('hour_begin');   
  end = target.getElementById('hour_end');
  if(field.checked) {
    end.style.display = 'none';
    begin.style.display = 'none';
    $('rd_opacity_free').checked = alldayOpacity == 'TRANSPARENT';
    $('rd_opacity_busy').checked = alldayOpacity == 'OPAQUE';
  } else {
    end.style.display = 'inline';
    begin.style.display = 'inline';
    $('rd_opacity_free').checked = false;
    $('rd_opacity_busy').checked = true;
    time_index = begin.getElement('select[name=sel_time_begin]').options.selectedIndex + 1;
    min_index = begin.getElement('select[name=sel_min_begin]').options.selectedIndex;
    if (time_index > 23) {
      time_index = 23;
      min_index = 59;
    }
    end.getElement('select[name=sel_time_end]').options.selectedIndex = time_index;
    end.getElement('select[name=sel_min_end]').options.selectedIndex = min_index;
  }     
}


///////////////////////////////////////////////////////////////////////////////
// Check if the meeting perform is possible
///////////////////////////////////////////////////////////////////////////////
function calendar_check_meeting(form) {

  if (trim(form.sel_min_duration.options[form.sel_min_duration.selectedIndex].value) == \"00\" 
    && trim(form.sel_time_duration.options[form.sel_time_duration.selectedIndex].value) == \"00\") {
      alert (\"$l_interval_null\");
      return false;
  }
  else if (form.tf_date.value == \"\") {
    alert(\"$l_datebegin\" + \" : $l_fill_date\");
    return false;
  }

  return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Add date picker form for exception date
///////////////////////////////////////////////////////////////////////////////
function add_exdate() {

  var exceptionHome = $('exceptionHome');
  // Create the div
  var div = new Element('div');
  div.adopt(
    new Element('a').addEvent('click', function () {
      remove_element(this.parentNode,'exceptionHome');
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  exceptionHome.adopt(div);
  // Create the date field
  var f = new Element('input').addClass('datePicker')
    .setProperty('type','text');
  div.adopt(f);
  f.setProperty('autocomplete','off');
  f.name = 'tf_date_exception[]';

  new Element('img').setProperty('src', obm.vars.images.datePicker)
    .injectAfter(f)
    .addEvent('click', function(e){
      displayDatePicker(f);
                              });

}

function toggleDisplay (element) {
  var toggleStyle = function (property_name, value1, value2) {
    if (element.getStyle(property_name) == value1) {
      element.setStyle(property_name, value2);
    } else {
      element.setStyle(property_name, value1);
    }
  };
  
  toggleStyle('display', 'none', '');
}

var repeatEndAuto = false;

function repeat_form(value) {
  var noRepeatEndDisplay = true;
  
  switch(value) {
  case 'none' :
    $('repeatFrequency').setStyle('display','none');
    $('repeatEnd').setStyle('display','none');
    $('repeatDays').setStyle('display','none');
    $('repeatException').setStyle('display','none');
    noRepeatEndDisplay = false;
    break;
  case 'daily' : 
    showRepeatBlockDisplay('".phpStringToJsString($l_daily_unit)."');
    $('repeatDays').setStyle('display','none');
    var dateEnd = getFieldDate($('tf_date_end').value); 
    var repeatEnd = getFieldDate($('tf_repeat_end').value);
    var interval = parseInt($('tf_repeatfrequency').value);
    dateEnd.setDate(dateEnd.getDate() + interval);
    if(repeatEndAuto || repeatEnd < dateEnd) {
      $('tf_repeat_end').value = getDateString(dateEnd);
      repeatEndAuto = true;
      } 
      break;           
    case 'weekly' :
      showRepeatBlockDisplay('".phpStringToJsString($l_weekly_unit)."');
      $('repeatDays').setStyle('display','');
      var dateEnd = getFieldDate($('tf_date_end').value); 
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value) * 7;
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEndAuto || repeatEnd < dateEnd) {
        $('tf_repeat_end').value = getDateString(dateEnd);
        repeatEndAuto = true;
      }       
      break;            
    case 'monthlybydate' :
      showRepeatBlockDisplay('".phpStringToJsString($l_monthlybydate_unit)."');
      $('repeatDays').setStyle('display','none');
      var dateEnd = getFieldDate($('tf_date_end').value); 
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value) * 31;
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEndAuto || repeatEnd < dateEnd) {
        $('tf_repeat_end').value = getDateString(dateEnd);
        repeatEndAuto = true;
      }       
      break;      
    case 'monthlybyday' : 
      showRepeatBlockDisplay('".phpStringToJsString($l_monthlybyday_unit)."');
      $('repeatDays').setStyle('display','none');
      var dateEnd = getFieldDate($('tf_date_end').value); 
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value) * 31;
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEndAuto || repeatEnd < dateEnd) {
        dateEnd.setDate(dateEnd.getDate() + interval);
        $('tf_repeat_end').value = getDateString(dateEnd);
        repeatEndAuto = true;
      }       
      break;
    case 'yearly' : 
      $('repeatFrequency').setStyle('display','');
      showRepeatBlockDisplay('".phpStringToJsString($l_yearly_unit)."');
      $('repeatDays').setStyle('display','none');
      var dateEnd = getFieldDate($('tf_date_end').value);
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value) * 366;
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEndAuto || repeatEnd < dateEnd) {
        $('tf_repeat_end').value = getDateString(dateEnd);
        repeatEndAuto = true;
      }       
      break;

  }
  
  $('noRepeatEnd').setStyle('display', noRepeatEndDisplay?'':'none');
  
  if (noRepeatEndDisplay) {
    if ($('tf_repeat_end').value == '') {
      $('cbx_no_repeatend').checked = true;
    }
    if ($('cbx_no_repeatend').checked) {
      $('repeatEnd').setStyle('display','none');
    }
  } 
}

function showRepeatBlockDisplay(label) {
  $('repeatFrequency').setStyle('display','');
  $('repeatEnd').setStyle('display','');
  $('repeatException').setStyle('display','');  
  frequency = $('repeatFrequency').getFirst().getNext();
  frequencyField = frequency.getFirst();
  frequencyLabel = label.split('%s');
  frequencyField.dispose();
  frequency.empty();
  frequency.appendText(frequencyLabel[0]);
  frequency.adopt(frequencyField);
  frequency.appendText(frequencyLabel[1]);      
}

function add_email_field() {

  var mailHome = $('calendarMailHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'calendarMailHome');
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  mailHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').addClass('otherAttendee').setProperty('name','tf_others_attendees[]').setProperty('type','text'));
  
}

function add_file_field() {

  var fileHome = $('calendarFileHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
    remove_element(this.parentNode,'calendarFileHome');
    obm.vars.consts.nbdocs++;
    $('addFileButton').style.display = '';    
  }).adopt(new Element('img').setProperty('src','$ico_delete')));
  fileHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').addClass('otherFile').setProperty('name','fi_other_files[]').setProperty('type','file').setProperty('size',7));
  obm.vars.consts.nbdocs--;
  if (obm.vars.consts.nbdocs <= 0) {
    obm.vars.consts.nbdocs=0;
    $('addFileButton').style.display = 'none';
  }

}

function submitWaiting(form) {
  form = $(form);
  form.getElement('input[name=owner_notification]').value = $('owner_notification').checked ;
  form.submit();
}

obm.initialize.chain(function() {	
  // pour chaque label, dans l'onglet administration
  $$('.extColorPicker').each(function(element) {
	var startColor = element.getElement('input').getProperty('value');
	// affiche la couleur de depart
	element.getElement('div').setStyle('background-color', startColor);
	// cree un colorPicker
    var r = new MooRainbow( element,
		{'startColor': startColor.hexToRgb(true),
		'id': element.getProperty('id') + '-rainbow',
		'imgPath': '/images/themes/default/images/',
		'onChange': function(color, colorPicker) {
				colorPicker.element.getElement('div').setStyle('background-color', color.hex);
				colorPicker.element.getElement('input').setProperty('value', color.hex) ;
			}
		}) ;
  });
	
	// pour le champs display_mycolor, de la fenetre de modif d'evenement
	if ($('display_tag_color')) {
		obm.calendarColorPicker = new MooRainbow('display_tag_color',
			{'startColor': $('display_tag_color').getStyle('background-color').hexToRgb(true),
			 'id': 'display_mycolor-rainbow',
			 'imgPath': '/images/themes/default/images/',
			 'onChange': function(color) {
					$('display_tag_color').setStyle('background-color', color.hex);
					$('tf_tag_color').set('value', color.hex) ;
					//this.setProperty('value', color.hex) ;
				}
			}) ;
	}
});

function add_label_autocomplete (el) { 
  new Autocompleter.Ajax.Json(el, '$path/calendar/calendar_index.php', {
    postVar: 'label',
    postData: {action:'tag_search'},
    ajaxOptions: {'secure': false},
    filterSubset: true,
    minLength: 0,
    selectFirst: true,
    selectMode: 'type-ahead',
    injectChoice: function(choice) {
				var el = new Element('li').set('html', this.markQueryValue(choice.label));
				new Element('div').inject(el, 'top').set('html','&nbsp;').setStyle('background-color',choice.color).addClass('colorHint') ;
				el.inputValue = choice.label;
				// hack, we nedd these values later to update
				el.suggColor = choice.color ;
				el.tag_id = choice.id ;
				this.addChoiceEvents(el).inject(this.choices);
			},
		onSelection: function(theinput, thesel, curval, suggval) {
			  $('display_tag_color').setStyle('background-color', thesel.suggColor) ;
				$('tf_tag_id').set('value', thesel.tag_id) ;
				$('tf_tag_color').set('value', thesel.suggColor) ;
				obm.calendarColorPicker.manualSet(thesel.suggColor, 'hex') ;
			}
  });       
}

function tag_reset() {
	$('tf_tag_id').set('value', '-1') ; // equal to \$c_none
	$('tf_tag_color').set('value', '') ;
	$('tf_tag_label').set('value', '') ;
	$('display_tag_color').setStyle('background-color', '#eff0f2') ;
}

function disable(id){
  var elem = $(id);
  if(elem.disabled) {
    elem.disabled=false;
  } else {
    elem.disabled=true;
  }
  return true;
}

function submitResetExportForm(){
  disable('bt_submit');
  disable('bt_cancel');
  $('export_action').setProperty('value','reset');
  $('export_popup').setProperty('value','0');
  $('export_force').setProperty('value','1');
  $('f_entity').submit();
}

function detachDocument(documentId, eventEntityId, el) {
  var req = new Request({
    url: '$path/calendar/calendar_index.php',
    data: 'action=detach_document&document_id='+documentId+'&event_entity_id='+eventEntityId,
    onRequest: function() {
      el.style.display = 'none';
      var spinner = new Element('img').setProperty('src','$ico_spinner');
      spinner.inject(el, 'before');
    },
    onSuccess: function(responseText, responseXML) {
      $(el).getParent().destroy();
    }
  });
  req.send();
}

function displayTemplateCreate() {
  obm.popup.popups.erase('createTemplatePopup');
  obm.popup.show('createTemplatePopup');
}

function setTemplateName(templateForm) {
  var templateName = $('tf_template_name').value;
  var eventForm = $('new_event_form');
  eventForm.getElement('input[id=action]').value = 'save_as_template';
  eventForm.adopt(new Element('input').setProperty('type', 'hidden').setProperty('name', 'tf_template_name').setProperty('value', templateName));
  eventForm.submit();
}

function displayTemplateDuplicate(btnForm, initialNameValue) {
  obm.popup.popups.erase('duplicateTemplatePopup');
  var popupForm = $('duplicateTemplatePopupForm');
  var input = popupForm.getElement('input[name=name]');
  popupForm.addEvent('submit', function() {
    var templateName = input.value;
    btnForm.adopt(new Element('input').setProperty('type', 'hidden').setProperty('name', 'tf_template_name').setProperty('value', templateName));
    btnForm.submit();
    return false;
  });
  input.value = initialNameValue;
  obm.popup.show('duplicateTemplatePopup');
  input.focus();
  input.select();
}

/////////////////////////////////////////////////////////////////////////////
function setEventsColors(entity, entity_id, klass) {
  var req = new Request({
    url : '$path/calendar/calendar_index.php',
    data: 'action=set_entity_class&ajax=1&entity_type='+entity+'&entity_id='+entity_id+'&entity_class='+klass
  }); 
  req.send();
}


/////////////////////////////////////////////////////////////////////////////
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

function toggleSubmitOnSelection(select) {
  $('calendarQuickFormSubmit').disabled = select.selectedIndex > 0;
}

";

?>
