<?php

////////////////////////////////////////////////////////////////////////////////
// Convert a php array to a javascript array
////////////////////////////////////////////////////////////////////////////////
function phpArrayToJsArray($var) {
	
  $jsArray = "new Array(";
  
  while(list($key, $val) = each($var)) {
    $jsArray .= "'$val',";
  }
  $jsArray = substr($jsArray, 0, -1);
  $jsArray .= ")";

  return $jsArray;
}


////////////////////////////////////////////////////////////////////////////////
// Get date labels
////////////////////////////////////////////////////////////////////////////////
$MonthNames = phpArrayToJsArray($l_monthsofyear);
$WeekDays = phpArrayToJsArray($l_daysofweekfirst);


$extra_js .= "
////////////////////////////////////////////////////////////////////////////////
// Parameters
////////////////////////////////////////////////////////////////////////////////
var Delimiter = '-'; // Date delimiter
var Timeout = 1; // timer in second
var ImageURL = '".C_IMAGE_PATH."/$set_theme/$ico_mini_cal';
var CellWidth = 24; // Cell width
var CellHeight = 15; // Cell height
var NextURL = '".C_IMAGE_PATH."/$set_theme/ico_next.gif';
var PrevURL = '".C_IMAGE_PATH."/$set_theme/ico_previous.gif';
var ZCounter = 100;
var Today = new Date();
var YearPart = Today.getFullYear();
var MonthPart = Today.getMonth();
var DayPart = Today.getDate();
var MonthNames = eval($MonthNames);
var MonthDays = new Array(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
var WeekDays = eval($WeekDays);
var MaxLength = 10; // Input maxlength
var InputSize = 10; // Input size


/////////////////////////////////////////////////////////////////////////////////
// Hide select-list if they are behind the calendar, specific for IE ...
////////////////////////////////////////////////////////////////////////////////
// Gets the absolute pixel position of the supplied element
function GetTagPixels(StartTag, Direction) {
  var PixelAmt = (Direction == 'LEFT') ? StartTag.offsetLeft : StartTag.offsetTop;
  while ((StartTag.tagName != 'BODY') && (StartTag.tagName != 'HTML')) {
    StartTag = StartTag.offsetParent;
    PixelAmt += (Direction == 'LEFT') ? StartTag.offsetLeft : StartTag.offsetTop;
  }
  return PixelAmt;
}


// Hide select-list if behind the calendar
function FixSelectLists(Over) {
  if (navigator.appName == 'Microsoft Internet Explorer') {
    // Get the calendar absolut pixel position
    var CalDiv = this.getCalendar();
    var CalLeftX = CalDiv.offsetLeft;
    var CalRightX = CalLeftX + CalDiv.offsetWidth;
    var CalTopY = CalDiv.offsetTop;
    var CalBottomY = CalTopY + (CellHeight * 10);
    var FoundCalInput = false;
    formLoop :
    for (var j=this.formNumber;j<document.forms.length;j++) {
      for (var i=0;i<document.forms[j].elements.length;i++) {
        if (typeof document.forms[j].elements[i].type == 'string') {
          if ((document.forms[j].elements[i].type == 'hidden') && (document.forms[j].elements[i].name == this.hiddenFieldName + '_HIDDEN')) {
            FoundCalInput = true;
            i ++;
          }
          if (FoundCalInput) {
            if (document.forms[j].elements[i].type == 'select-one') {
              selectlist = document.forms[j].elements[i];
              // Get abusolute pixel position of target list
              ListLeftX = GetTagPixels(selectlist, 'LEFT');
              ListTopY = GetTagPixels(selectlist, 'TOP');
              ListRightX = ListLeftX + selectlist.offsetWidth;
              ListBottomY = ListTopY + selectlist.offsetHeight;
              // Is the select-list behind the calendar ?
              if ((ListTopY < CalBottomY && ListBottomY > CalTopY) && (ListRightX > CalRightX && ListLeftX > CalLeftX)) {
                document.forms[j].elements[i].style.visibility = (Over) ? 'hidden' : 'visible';
              } else 
                break formLoop;
              }
          }
        }
      }
    }
  }
}


////////////////////////////////////////////////////////////////////////////////
// Display the picked date
////////////////////////////////////////////////////////////////////////////////
function PickDisplayDay(ClickedDay) {

  this.show();
  var MonthList = this.getMonthList();
  var DayList = this.getDayList();
  var YearField = this.getYearField();

  this.setPicked(this.displayed.yearValue, this.displayed.monthIndex, ClickedDay);

  // Display date format YYYY-MM-DD
  ClickedDay = (ClickedDay < 10) ? '0' + ClickedDay : ClickedDay;
  var DisplayDate = this.displayed.yearValue + Delimiter + this.picked.monthPad + Delimiter + ClickedDay;

  // Test displayed date < compared date
  try {
    eval('DisplayChkDate = window.document.forms[0].'+this.checkDate+'.value');
    if(DisplayChkDate < DisplayDate) {
      eval('window.document.forms[0].'+this.checkDate+'.value = DisplayDate');
    }
  } catch (e) {}
}


////////////////////////////////////////////////////////////////////////////////
// Build the calendar html code
////////////////////////////////////////////////////////////////////////////////
function BuildCalendarDays() {

  var Rows = 5;
  if (((this.displayed.dayCount == 31) && (this.displayed.firstDay > 4))
      || ((this.displayed.dayCount == 30) && (this.displayed.firstDay == 6))) {
    Rows = 6;
  } else if ((this.displayed.dayCount == 28)
              && (this.displayed.firstDay == 0)) {
    Rows = 4;
  }
  var HTML = '<table width=\"' + (CellWidth * 7) + '\" cellspacing=\"0\" cellpadding=\"1\">';
  for (var j=0;j<Rows;j++) {
    HTML += '<tr>';
    for (var i=1;i<=7;i++) {
      Day = (j * 7) + (i - this.displayed.firstDay);
      if ((Day >= 1) && (Day <= this.displayed.dayCount)) {
        if ((this.displayed.yearValue == YearPart)
            && (this.displayed.monthIndex == MonthPart)
            && (Day == DayPart)) {
          Class =  'calendarThisDay';
        } else {
          Class =  'calendarDateInputHover';
        }
        HTML += '<td align=\"center\" class=\"'+Class+'\" onClick=\"' + this.objName + '.pickDay(' + Day + ')\">' + Day + '</td>';
      } else {
        HTML += '<td class=\"calendarDateInput\"></td>';
      }
    }
    HTML += '</tr>';
  }
  return HTML += '</table>';
}


////////////////////////////////////////////////////////////////////////////////
// Get days in a month
////////////////////////////////////////////////////////////////////////////////
function GetDayCount(SomeYear, SomeMonth) {

  return ((SomeMonth == 1) && !(SomeYear % 4 == 0)) ? 28 : MonthDays[SomeMonth];
}


////////////////////////////////////////////////////////////////////////////////
// Timer reset
////////////////////////////////////////////////////////////////////////////////
function CalTimerReset() {

  eval('clearTimeout(' + this.timerID + ')');
  eval(this.timerID + '=setTimeout(\'' + this.objName + '.show()\',' + (Timeout * 1000) + ')');
}


////////////////////////////////////////////////////////////////////////////////
// Timer
////////////////////////////////////////////////////////////////////////////////
function DoTimer(CancelTimer) {

  if (CancelTimer) {
    eval('clearTimeout(' + this.timerID + ')');
  } else {
    eval(this.timerID + '=null');
    this.resetTimer();
  }
}


////////////////////////////////////////////////////////////////////////////////
// Show or hide the calendar
////////////////////////////////////////////////////////////////////////////////
function ShowCalendar() {
	if (this.isShowing()) {
		var StopTimer = true;
		this.getCalendar().style.zIndex = --ZCounter;
		this.getCalendar().style.visibility = 'hidden';
    this.fixSelects(false);
	}
	else {
		var StopTimer = false;
		this.getCalendar().style.zIndex = ++ZCounter;
		this.getCalendar().style.visibility = 'visible';
    this.fixSelects(true);
	}
	this.handleTimer(StopTimer);
}


////////////////////////////////////////////////////////////////////////////////
// Holds characteristics about a date
////////////////////////////////////////////////////////////////////////////////
function dateObject() {

  if (Function.call) { // Used when 'call' method of the Function object is supported
    var ParentObject = this;
    var ArgumentStart = 0;
  } else { // Used with 'call' method of the Function object is NOT supported
    var ParentObject = arguments[0];
    var ArgumentStart = 1;
  }
  ParentObject.date = (arguments.length == (ArgumentStart+1)) ? new Date(arguments[ArgumentStart+0]) : new Date(arguments[ArgumentStart+0], arguments[ArgumentStart+1], arguments[ArgumentStart+2]);
  ParentObject.yearValue = ParentObject.date.getFullYear();
  ParentObject.monthIndex = ParentObject.date.getMonth();
  ParentObject.monthName = MonthNames[ParentObject.monthIndex];
  ParentObject.fullName = ParentObject.monthName + ' ' + ParentObject.yearValue;
  ParentObject.day = ParentObject.date.getDate();
  ParentObject.dayCount = GetDayCount(ParentObject.yearValue, ParentObject.monthIndex);
  var FirstDate = new Date(ParentObject.yearValue, ParentObject.monthIndex, 1);
  ParentObject.firstDay = FirstDate.getDay();
}


////////////////////////////////////////////////////////////////////////////////
// Format de la date dans les champs
////////////////////////////////////////////////////////////////////////////////
function storedMonthObject(DateYear, DateMonth, DateDay) {

  (Function.call) ? dateObject.call(this, DateYear, DateMonth, DateDay) : dateObject(this, DateYear, DateMonth, DateDay);
  this.yearPad = this.yearValue.toString();
  this.monthPad = (this.monthIndex < 9) ? '0' + String(this.monthIndex + 1) : this.monthIndex + 1;
  this.dayPad = (this.day < 10) ? '0' + this.day.toString() : this.day;

  // Année sur 2 digits au lieu de 4
  //this.yearPad = this.yearPad.substr(2);

  this.formatted = this.yearPad + Delimiter + this.monthPad + Delimiter + this.dayPad;
}


////////////////////////////////////////////////////////////////////////////////
// Object for the current displayed month
////////////////////////////////////////////////////////////////////////////////
function displayMonthObject(ParentObject, DateYear, DateMonth, DateDay) {

  (Function.call) ? dateObject.call(this, DateYear, DateMonth, DateDay) : dateObject(this, DateYear, DateMonth, DateDay);
  this.displayID = ParentObject.hiddenFieldName + '_Current_ID';
  this.getDisplay = new Function('return document.getElementById(this.displayID)');
  this.goCurrent = new Function(ParentObject.objName + '.getCalendar().style.zIndex=++ZCounter;' + ParentObject.objName + '.setDisplayed(Today.getFullYear(),Today.getMonth());');
  if (ParentObject.formNumber >= 0) {
    this.getDisplay().innerHTML = this.fullName;
  }
}


////////////////////////////////////////////////////////////////////////////////
// Object for the previous/next buttons
////////////////////////////////////////////////////////////////////////////////
function neighborMonthObject(ParentObject, IDText, DateMS) {

  (Function.call) ? dateObject.call(this, DateMS) : dateObject(this, DateMS);
  this.buttonID = ParentObject.hiddenFieldName + '_' + IDText + '_ID';
  this.go = new Function(ParentObject.objName + '.getCalendar().style.zIndex=++ZCounter;' + ParentObject.objName + '.setDisplayed(this.yearValue,this.monthIndex);');
}


////////////////////////////////////////////////////////////////////////////////
// Sets the currently-displayed month object
////////////////////////////////////////////////////////////////////////////////
function SetDisplayedMonth(DispYear, DispMonth) {

  this.displayed = new displayMonthObject(this, DispYear, DispMonth, 1);
  // Creates the previous and next month objects
  this.previous = new neighborMonthObject(this, 'Previous', this.displayed.date.getTime() - 86400000);
  this.next = new neighborMonthObject(this, 'Next', this.displayed.date.getTime() + (86400000 * (this.displayed.dayCount + 1)));
  // Creates the HTML for the calendar
  if (this.formNumber >= 0) {
    this.getDayTable().innerHTML = this.buildCalendar();
  }
}


////////////////////////////////////////////////////////////////////////////////
// Sets the current selected date
////////////////////////////////////////////////////////////////////////////////
function SetPickedMonth(PickedYear, PickedMonth, PickedDay) {

  this.picked = new storedMonthObject(PickedYear, PickedMonth, PickedDay);
  this.setHidden(this.picked.formatted);
  this.setDisplayed(PickedYear, PickedMonth);
}


////////////////////////////////////////////////////////////////////////////////
// Calendar object
////////////////////////////////////////////////////////////////////////////////
function calendarObject(DateName, IsoDate, ChkDate) {

  /* Proprietés */
  this.hiddenFieldName = DateName;
  this.calendarID = DateName + '_ID';
  this.dayTableID = DateName + '_DayTable_ID';
  this.calendarLinkID = this.calendarID + '_Link';
  this.timerID = this.calendarID + '_Timer';
  this.objName = DateName + '_Object';
  this.inputDisplay = DateName;
  this.checkDate = ChkDate;
  this.formNumber = -1;
  this.picked = null;
  this.displayed = null;
  this.previous = null;
  this.next = null;

  /*Méthodes */
  this.setPicked = SetPickedMonth;
  this.setDisplayed = SetDisplayedMonth;
  this.resetTimer = CalTimerReset;
  this.show = ShowCalendar;
  this.handleTimer = DoTimer;
  this.buildCalendar = BuildCalendarDays;
  this.pickDay = PickDisplayDay;
  this.fixSelects = FixSelectLists;

  this.setHidden = new Function('D','if (this.formNumber >= 0) this.getHiddenField().value=D');
  this.getHiddenField = new Function('return document.forms[this.formNumber].elements[this.hiddenFieldName]');
  this.getMonthList = new Function('return document.getElementById(this.monthListID)');
  this.getDayList = new Function('return document.getElementById(this.dayListID)');
  this.getYearField = new Function('return document.getElementById(this.yearFieldID)');
  this.getCalendar = new Function('return document.getElementById(this.calendarID)');
  this.getDayTable = new Function('return document.getElementById(this.dayTableID)');
  this.getCalendarLink = new Function('return document.getElementById(this.calendarLinkID)');
  this.getMonthDisplay = new Function('return document.getElementById(this.monthDisplayID)');
  this.isShowing = new Function('return !(this.getCalendar().style.visibility != \'visible\')');

  /*Constructeurs */

  if (IsoDate != '') {
    Year = IsoDate.substr(0,4);
    Month = IsoDate.substr(5,2) - 1;
    Day = IsoDate.substr(8,2);
  } else {
    Year = YearPart;
    Month = MonthPart;
    Day = DayPart;
  }

  this.setPicked(Year, Month, Day);
}


////////////////////////////////////////////////////////////////////////////////
// Main function
////////////////////////////////////////////////////////////////////////////////
function calendar(DateName, IsoDate, ChkDate) {

  // Creates the calendar object!
  eval(DateName + '_Object=new calendarObject(\'' + DateName + '\',\'' + IsoDate + '\',\'' + ChkDate + '\')');
  var InitialDate = eval(DateName + '_Object.picked.formatted');

  // ************************************************
  // Form
  // ************************************************
  with (document) {
    writeln('<input type=\"hidden\" name=\"' + DateName + '_HIDDEN\" value=\"' + IsoDate + '\">');
    // Find this form number
    for (var f=0;f<forms.length;f++) {
      for (var e=0;e<forms[f].elements.length;e++) {
        if (typeof forms[f].elements[e].type == 'string') {
      	  if ((forms[f].elements[e].type == 'hidden') && (forms[f].elements[e].name == DateName + '_HIDDEN')) {
      	    eval(DateName + '_Object.formNumber='+f);
      	    break;
      	  }
        }
      }
    }

    // ************************************************
    // Display date field
    // ************************************************
    writeln('<input  type=\"text\" name=\"'+DateName+'\" id=\"'+DateName+'\" value=\"' + IsoDate + '\" size=\"'+InputSize+'\" maxlength=\"'+MaxLength+'\" onchange=\"live_check_date('+DateName+', '+ChkDate+');\">');
    // writeln('<input  type=\"text\" name=\"'+DateName+'\" id=\"'+DateName+'\" value=\"' + IsoDate + '\" size=\"'+InputSize+'\" maxlength=\"'+MaxLength+'\">');

    // ************************************************
    // Open calendar image link
    // ************************************************
    write('<a href=\"javascript:' + DateName + '_Object.show()\"><img class=\"calendarButton\" src=\"' + ImageURL + '\" ></a>');

    // ************************************************
    // Calendar
    // ************************************************
    writeln('<span id=\"' + DateName + '_ID\" style=\"position:absolute;visibility:hidden;width:' + (CellWidth * 7) + 'px;\" class=\"calendarSpan\" onMouseOver=\"' + DateName + '_Object.handleTimer(true)\" onMouseOut=\"' + DateName + '_Object.handleTimer(false)\">');
    
    writeln('<table width=\"' + (CellWidth * 7) + '\" cellspacing=\"0\" cellpadding=\"1\"><tr class=\"calendarDateInputHead\">');

    // Previous button
    writeln('<td class=\"calendarDateInput \" id=\"' + DateName + '_Previous_ID\" onClick=\"' + DateName + '_Object.previous.go()\"><img src=\"' + PrevURL + '\"></td>');

    // Display month
    writeln('<td id=\"' + DateName + '_Current_ID\" class=\"calendarDateInput\" colspan=\"5\">' + eval(DateName + '_Object.displayed.fullName') + '</td>');

    // Next button
    writeln('<td id=\"' + DateName + '_Next_ID\" class=\"calendarDateInput\" onClick=\"' + DateName + '_Object.next.go()\"><img src=\"' + NextURL + '\"></td></tr><tr>');

    // Display days label
    for (var w=0;w<7;w++) { 
      writeln('<td class=\"calendarDateInputDay\">' + WeekDays[w] + '</td>');
    }
    writeln('</tr></table>');

    // Display days number
    writeln('<span id=\"' + DateName + '_DayTable_ID\">' + eval(DateName + '_Object.buildCalendar()') + '</span>');
    
    // Today link
    writeln('<span id=\"' + DateName + '_TODAY\"><table width=\"' + (CellWidth * 7) + '\" cellspacing=\"0\" cellpadding=\"1\"><tr class=\"calendarDateInputHead\"><td align=\"center\"  class=\"calendarDateInput\" onClick=\"' + DateName + '_Object.displayed.goCurrent()\">$l_today</td></tr></table></span>');
    
    writeln('</span>');

  }
}


////////////////////////////////////////////////////////////////////////////////
// Return days in february
////////////////////////////////////////////////////////////////////////////////
function daysInFebruary (year) {
  return ((year % 4 == 0)  ? 29 : 28 );
}


////////////////////////////////////////////////////////////////////////////////
// Check input date format
////////////////////////////////////////////////////////////////////////////////
// Transform date input value to an iso date when date field in unfocused
////////////////////////////////////////////////////////////////////////////////
function live_check_date(date_field, datecompare) {

  var dateinput = date_field.value;

   if (dateinput == \"\") {
    date_field.value = \"\";
    return false;
  }

  // Iso date regexp
  isodate_exp = /[0-9]{4}-[0-1][0-9]-[0-3][0-9]/;
  isodate = dateinput.match(isodate_exp);

  if(isodate) {
    isosplit = dateinput.split(\"-\");
    Y = isosplit[0];
    M = isosplit[1];
    D = isosplit[2];
  } else {
    date = trim(dateinput.replace(/(-|\.|\/)/g, \"\"));
    datelength = date.length;
    if(!isNaN(date)) {
      // Case 1 : DMY
      if(\"$set_date_upd\" == \"$cda_upd_dmy\") {
        if(datelength == 8) {
          Y = date.substr(4, 4);
          M = date.substr(2,2);
          D = date.substr(0,2);
        }else if(datelength == 6) {
          Y = \"20\" + date.substr(4, 2);
          M = date.substr(2,2);
          D = date.substr(0,2);
        }
      // Case 2 : MDY
      } else {
        if(datelength == 8) {
          Y = date.substr(4, 4);
          M = date.substr(0,2);
          D = date.substr(2,2);
        } else if(datelength == 6) {
          Y = \"20\" + date.substr(4, 2);
          M = date.substr(0,2);
          D = date.substr(2,2);
        }
      }
    } else { 
      alert(\"$l_invalid_date\");
      date_field.value = \"\";
      return false;
    } 
  }

  try {
    /////////////////////////////
    // Date control
    /////////////////////////////
    // Check day
    if (D < 1 || D > 31 || (M == 2 && D > daysInFebruary(Y)) || D > MonthDays[M-1]){ // M-1 because MonthDays index start at 0, not 1.
      alert(D + \" : $l_invalid_date_day\");
      date_field.value = \"\";
      return false;
      }
  
    // Check month
    if ((M < 1) || (M > 12)) {
      alert(M + \" : $l_invalid_date_month\");
      date_field.value = \"\";
      return false;
    }
  
    // Check month
    if ((Y < 2000) || (Y > 2100)) {
      alert(Y + \" : $l_invalid_date_year\");
      date_field.value = \"\";
      return false;
    }

    /////////////////////////////
    // Date is OK
    /////////////////////////////
    // Write iso date in date field
    date_field.value = Y + Delimiter + M + Delimiter +D;
  
    // if date compare < date_field 
    // write iso date in date_compare field
    if(datecompare) {
      if(datecompare.value < date_field.value ) {
        datecompare.value = Y + Delimiter + M + Delimiter +D;
      }
    }
    return true;
  } catch (e) {
      alert(\"$l_invalid_date\");
      date_field.value = \"\";
      return false;
  }
}
";
?>
