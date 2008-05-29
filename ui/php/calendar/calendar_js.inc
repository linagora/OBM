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

function sel_public_group(date, element, label) {
  var item_id = element.getProperty('id').split('-');
  var id = item_id[item_id.length - 1];
  window.location=('calendar_index.php?date='+date+'&group_id='+id+'&new_group=1');
}

function display_list_detail(display) {

  slides.each(function(slide) {
    if (display == 'show') {
      slide.show();
    } else {
      slide.hide();
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
  else if (form.tf_date_end.value == \"\") {
    alert(\"$l_dateend\" + \" : $l_fill_date\");
    return false;
  }
  else if ((form.tf_repeat_end.value == \"\") && (trim(form.sel_repeat_kind.value) != \"none\") ) {
    alert(\"$l_date_repeatend\" + \" : $l_fill_date\");
    return false;
  }
  return true;
}


function show_hide_calendar_dates(field) {
  target = window.document;
  begin = target.getElementById('hour_begin');   
  end = target.getElementById('hour_end');
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
  else if (form.tf_date_begin.value == \"\") {
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
function repeat_form(value) {
  switch(value) {
    case 'none' :
      $('repeatFrequency').setStyle('display','none');
      $('repeatEnd').setStyle('display','none');
      $('repeatDays').setStyle('display','none');
      $('repeatException').setStyle('display','none');
      break;
    case 'daily' : 
      showRepeatBlockDisplay('".phpStringToJsString($l_daily_unit)."');
      $('repeatDays').setStyle('display','none');
      var dateEnd = getFieldDate($('tf_date_end').value); 
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value);
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEnd < dateEnd) {
        $('tf_repeat_end').value = getDateString(dateEnd);
      } 
      break;           
    case 'weekly' :
      showRepeatBlockDisplay('".phpStringToJsString($l_weekly_unit)."');
      $('repeatDays').setStyle('display','');
      var dateEnd = getFieldDate($('tf_date_end').value); 
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value) * 7;
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEnd < dateEnd) {
        $('tf_repeat_end').value = getDateString(dateEnd);
      }       
      break;            
     case 'monthlybydate' :
      showRepeatBlockDisplay('".phpStringToJsString($l_monthlybydate_unit)."');
      $('repeatDays').setStyle('display','none');
      var dateEnd = getFieldDate($('tf_date_end').value); 
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value) * 31;
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEnd < dateEnd) {
        $('tf_repeat_end').value = getDateString(dateEnd);
      }       
      break;      
    case 'monthlybyday' : 
      showRepeatBlockDisplay('".phpStringToJsString($l_monthlybyday_unit)."');
      $('repeatDays').setStyle('display','none');
      var dateEnd = getFieldDate($('tf_date_end').value); 
      var repeatEnd = getFieldDate($('tf_repeat_end').value);
      var interval = parseInt($('tf_repeatfrequency').value) * 31;
      dateEnd.setDate(dateEnd.getDate() + interval);
      if(repeatEnd < dateEnd) {
        dateEnd.setDate(dateEnd.getDate() + interval);
        $('tf_repeat_end').value = getDateString(dateEnd);
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
      if(repeatEnd < dateEnd) {
        $('tf_repeat_end').value = getDateString(dateEnd);
      }       
      break;
      
  }
}

function showRepeatBlockDisplay(label) {
  $('repeatFrequency').setStyle('display','');
  $('repeatEnd').setStyle('display','');
  $('repeatException').setStyle('display','');  
  frequency = $('repeatFrequency').getFirst().getNext();
  frequencyField = frequency.getFirst();
  frequencyLabel = label.split('%s');
  frequencyField.remove();
  frequency.empty();
  frequency.appendText(frequencyLabel[0]);
  frequency.adopt(frequencyField);
  frequency.appendText(frequencyLabel[1]);      
}

";

?>
