<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : agenda_js.inc                                               //
//     - Desc  : Agenda javascript functions File                            //
// 2001-07-05  - Francois Bloque                                             //
///////////////////////////////////////////////////////////////////////////////
// Last update 2001-07-20                                                  //
///////////////////////////////////////////////////////////////////////////////

echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";


require("$obminclude/javascript/check_js.inc");


////////////////////////////////////////////////////
// Return the number of days in the specified month 
////////////////////////////////////////////////////
echo "
function number_of_days(month_number) {

  switch (month_number) {
    case \"01\" : return 31; break;
    case \"02\" : return 29; break;
    case \"03\" : return 31; break;
    case \"04\" : return 30; break;
    case \"05\" : return 31; break;
    case \"06\" : return 30; break;
    case \"07\" : return 31; break;
    case \"08\" : return 31; break;
    case \"09\" : return 30; break;
    case \"10\" : return 31; break;
    case \"11\": return 30; break;
    case \"12\" : return 31; break;
    default : return 0; break;  
  }
}";


/////////////////////////////////////////////////////
// Deletion confirmation message box 
/////////////////////////////////////////////////////
// ?????
echo "
function deletion_valid() {
  if (confirm (\"$mj_event_delete_confirm\"))
    return true;
  else
    return false;
}
";

/////////////////////////////////////////////////////
// Deletion confirmation message box 
/////////////////////////////////////////////////////
echo "
function deletion_all_valid() {
  if (confirm (\"$mj_event_delete_all_confirm\"))
    return true;
  else
    return false;
}
";



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Check if : 
// - the title is filled
// - the hour and the minutes of begin, and the hour and the minutes of end are filled
// - the day chosen for the begin date, the end date, or the end of repetition (if used),  exists in the chosen month (for example "31 Feb" ... ) 
// - the end date is not before the begin date 
// - the date of end of repetition (if used) is not before the begin date 
// - the hours are between 0 and 23
// - the minutes are between 0 and 59
//
// Complete the hours and minutes with "0" so they are always on 2 char.
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
//  alert(\"mois debut \" + month_begin + \" mois fin \" + month_end);

echo "
function check_insert_event(form) { 

  year_begin=form.sel_year_begin.options[form.sel_year_begin.selectedIndex].value;
  year_end=form.sel_year_end.options[form.sel_year_end.selectedIndex].value;
  year_repeatend=form.sel_year_repeatend.options[form.sel_year_repeatend.selectedIndex].value;
  month_begin=form.sel_month_begin.options[form.sel_month_begin.selectedIndex].value;
  month_end=form.sel_month_end.options[form.sel_month_end.selectedIndex].value;
  month_repeatend=form.sel_month_repeatend.options[form.sel_month_repeatend.selectedIndex].value;
  day_begin=form.sel_day_begin.options[form.sel_day_begin.selectedIndex].value; 
  day_end=form.sel_day_end.options[form.sel_day_end.selectedIndex].value; 
  day_repeatend=form.sel_day_repeatend.options[form.sel_day_repeatend.selectedIndex].value;   


  if (trim(form.tf_event_title.value) == \"\") { 
    alert (\"$mj_fill_title_event\"); 
    return false; 
  } else if (trim(form.tf_hour_begin.value) == \"\") { 
    alert (\"$mj_fill_time_begin\");
    return false; 
  } else if (trim(form.tf_minute_begin.value) == \"\") { 
    alert (\"$mj_fill_time_begin\"); 
    return false; 
  } else if (trim(form.tf_hour_end.value) == \"\") { 
    alert (\"$mj_fill_time_end\"); 
    return false; 
  } else if (trim(form.tf_minute_end.value) == \"\") {  
    alert (\"$mj_fill_time_end\"); 
    return false; 
  } else if ( (trim(form.tf_hour_begin.value) < 0) || (trim(form.tf_hour_begin.value)>23) ){  
    alert (\"$mj_fill_valid_time_begin\"); 
    return false; 
  } else if ( (trim(form.tf_hour_end.value) < 0) || (trim(form.tf_hour_end.value)>23) ){  
    alert (\"$mj_fill_valid_time_end\"); 
    return false; 
  } else if ( (trim(form.tf_minute_begin.value) < 0) || (trim(form.tf_minute_begin.value) > 59) ){  
    alert (\"$mj_fill_valid_time_begin\"); 
    return false; 
  } else if ( (trim(form.tf_minute_end.value) < 0) || (trim(form.tf_minute_end.value) > 59) ){  
    alert (\"$mj_fill_valid_time_end\"); 
    return false; 
  } else if ( day_begin > number_of_days(month_begin)) {
    alert (\"$mj_invalid_day\");
    return false;
  } else if ( day_end > number_of_days(month_end)) {
    alert (\"$mj_invalid_day\");
    return false;
  } else if (( day_repeatend > number_of_days(month_repeatend)) && (form.cb_use_repeatend.checked)) {
    alert (\"$mj_invalid_day\");
    return false;
  } else if (year_end < year_begin) {
    alert (year_begin,\"$mj_date_incompatibility\");
    return false;       
  } else if ((year_end == year_begin) && (month_end < month_begin)) {
    alert (\"$mj_date_incompatibility\");
    return false; 
  } else if ((year_end == year_begin) && (month_end == month_begin) && (day_end < day_begin)) {
    alert (\"$mj_date_incompatibility\");
    return false; 
  } else if ((year_end == year_begin) && (month_end == month_begin) && (day_end == day_begin) && (trim(form.tf_hour_end.value) < trim(form.tf_hour_begin.value)) ) {
    alert (\"$mj_date_incompatibility\");
    return false; 
  } else if ((year_end == year_begin) && (month_end == month_begin) && (day_end == day_begin) && (trim(form.tf_hour_end.value) == trim(form.tf_hour_begin.value)) &&  (trim(form.tf_minute_end.value) < trim(form.tf_minute_begin.value)) ) {
    alert (\"$mj_date_incompatibility\");
    return false; 
  } else if ((year_repeatend < year_begin) && (year_repeatend>0) && (month_repeatend>0) && (day_repeatend>0)) {
    alert (\"$mj_repeat_date_incompatibility\");
    return false;
  } else if ((year_repeatend == year_begin) && (month_repeatend < month_begin) && (year_repeatend>0) && (month_repeatend>0) && (day_repeatend>0)) {
    alert (\"$mj_repeat_date_incompatibility\");
    return false;
  } else if ((year_repeatend == year_begin) && (month_repeatend == month_begin) && (day_repeatend < day_begin) && (year_repeatend>0) && (month_repeatend>0) && (day_repeatend>0)) {
    alert (\"$mj_repeat_date_incompatibility\");
    return false;
  } else {
  if (trim(form.tf_hour_begin.value.length) == 1) {
     form.tf_hour_begin.value=\"0\"+trim(form.tf_hour_begin.value);
  }
  if (trim(form.tf_minute_begin.value.length) == 1) {
     form.tf_minute_begin.value=\"0\"+trim(form.tf_minute_begin.value);
  }
  if (trim(form.tf_hour_end.value.length) == 1) {
     form.tf_hour_end.value=\"0\"+trim(form.tf_hour_end.value);
  }
  if (trim(form.tf_minute_end.value.length) == 1) {
     form.tf_minute_end.value=\"0\"+trim(form.tf_minute_end.value);
  }

    return true;
  }
}
";
//sel_day_begin

echo "
function SynchronizeDay(formulaire) {
 formulaire.sel_day_end.selectedIndex = formulaire.sel_day_begin.selectedIndex;
}

function SynchronizeMonth(formulaire) {
 formulaire.sel_month_end.selectedIndex = formulaire.sel_month_begin.selectedIndex;
}

function SynchronizeYear(formulaire) {
 formulaire.sel_year_end.selectedIndex = formulaire.sel_year_begin.selectedIndex;
}			
";

echo "
function UnSelectChoice(champ) {
champ.checked = 0;
}
";
echo "
//-->
</SCRIPT>
";


</SCRIPT>
