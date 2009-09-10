<?php
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

function clearPeriodSelection(period) {
alert('toto');
}

/////////////////////////////////////////////////////////////////////////////
function changePeriod(date) {
  var entities = new Array();
  var otherAttendee = new Array();
  var contacts = new Array();
  $$('input.currentAttendee').each(function(e) {
    entities.push(e.value);
  });

  var otherAttendee = new Array();
  $$('input.currentOtherAttendee').each(function(e) {
    otherAttendee.push(e.value);
  });

  displayFreeBusy(date, $('duration').value/3600, entities, false, otherAttendee);
}


/////////////////////////////////////////////////////////////////////////////
function performMeeting() {
  $$('div.freeBusyContent').each(function(e) {
    e.destroy();
  });

  var entities = new Array();
  $$('div.elementRow').each(function(e) {
    var id = e.id.split('-');
      var entity = id[1]+'-'+id[2]+'-'+id[3];
      entities.push(entity);
  });

  var duration = parseInt($('sel_time_duration').value, 10)+parseInt($('sel_min_duration').value, 10)/60;
  displayFreeBusy($('tf_date').value, duration, entities, false, new Array());
}


/////////////////////////////////////////////////////////////////////////////
function displayFreeBusyConflict(duration, d, ro) {
  var entities = new Array();
  $$('div.elementRow').each(function(e) {
    var id = e.id.split('-');
    if (id[0] == 'sel_user_id' || id[0] == 'sel_resource_id') {
      var entity = id[1]+'-'+id[2]+'-'+id[3];
      entities.push(entity);
    }
  });
  displayFreeBusy(d, duration, entities, ro, new Array());
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

  var entities = new Array();
  $$('div.elementRow').each(function(e) {
    var id = e.id.split('-');
    if (id[0] == 'sel_user_id' || id[0] == 'sel_resource_id' || id[0] == 'sel_group_id') {
      var entity = id[1]+'-'+id[2]+'-'+id[3];
      entities.push(entity);
    }
  });

  var otherAttendee = new Array();
  $$('input.otherAttendee').each(function(e) {
    if (e.value != '') otherAttendee.push(e.value);
  });

  if($('sel_attendees_id')) $('sel_attendees_id').set('html','');
  displayFreeBusy($('tf_date_begin').value, (end-begin)/1000/3600, entities, false, otherAttendee);
}


/////////////////////////////////////////////////////////////////////////////
function displayFreeBusy(date_begin, duration, entities, readOnly, otherAttendee) {
  var q = new Object();
  q.date = date_begin;
  new Request.JSON({
    url : 'calendar_index.php',
    secure : false,
    onComplete: function(resp) {
      if($('fbc')) $('fbc').destroy();
      $('fbcContainer').innerHTML = resp.html;
      eval(resp.js);
      obm.popup.popups.erase('fbc');
      obm.popup.show('fbc');
      obm.calendarFreeBusy.buildFreeBusyPanel(duration, readOnly);
      obm.calendarFreeBusy.addAttendee(entities);
      var id = 0;
      otherAttendee.each(function(e) {
        obm.calendarFreeBusy.addAttendee(['data-others_attendees-'+id], e);
        id++;
      });
      $('fbcContainer').innerHTML = '';

    }
  }).post(\$merge({ajax : 1, action : 'perform_meeting'}, q)); 
}


function add_displayed_attendee(check, attendee, uid, kind) {
  if (check) {
    for(var id in attendee) {
      var sel_id = 'sel_'+kind+'_id-data-'+kind+'-'+id;
      if ($(sel_id) == null) {
        of_select_add_element(window.document, 'sel_'+kind+'_id', 'data-'+kind+'-'+id, attendee[id]);
      }
    }
  } else {
    for(var id in attendee) {
      if (id != uid) {
        var sel_id = 'sel_'+kind+'_id-data-'+kind+'-'+id;
        if (sel_id != null) {
          $(sel_id).destroy();
        }
      }
    }
  }
}

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


function sel_public_group(date, element, label) {
  var item_id = element.getProperty('id').split('-');
  var id = item_id[item_id.length - 1];
  window.location=('calendar_index.php?date='+date+'&group_id='+id+'&new_group=1');
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


function show_hide_calendar_dates(field) {
  target = window.document;
  begin = target.getElementById('hour_begin');   
  end = target.getElementById('hour_end');
  $('hour_end').getElement('select[name=sel_time_end]').options.selectedIndex = $('hour_begin').getElement('select[name=sel_time_begin]').options.selectedIndex + 1;
  $('hour_end').getElement('select[name=sel_min_end]').options.selectedIndex = $('hour_begin').getElement('select[name=sel_min_begin]').options.selectedIndex;
  if(field.checked) {
    end.style.display = 'none';
    begin.style.display = 'none';
  } else {
    end.style.display = 'inline';
    begin.style.display = 'inline';
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
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  fileHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').addClass('otherFile').setProperty('name','fi_other_files[]').setProperty('type','file').setProperty('size',7));
  
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
    minLength: 2,
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
}

function updateExportForm(){
  $('del_action').setProperty('value','reset');
  $('del_popup').setProperty('value','0');
  $('del_force').setProperty('value','1');
}

function detachDocument(documentId, eventEntityId, el) {
  var req = new Request({
    url: '$path/calendar/calendar_index.php',
    data: 'action=detach_document&document_id='+documentId+'&event_entity_id='+eventEntityId,
    onRequest: function() {
      el.setProperty('style', 'display:none');
      var spinner = new Element('img').setProperty('src','$ico_spinner');
      spinner.inject(el, 'before');
    },
    onSuccess: function(responseText, responseXML) {
      el.getParent().destroy();
    }
  });
  req.send();
}

";

?>
