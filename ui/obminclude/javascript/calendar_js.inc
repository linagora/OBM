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
var Delimiter = '-'; // Delimiteur
var Timeout = 1; // Nb de secondes du timeout
var ImageURL = '".C_IMAGE_PATH."/$set_theme/$ico_mini_cal';
var CellWidth = 30; // Largeur d'une cellule
var NextURL = '".C_IMAGE_PATH."/$set_theme/ico_next.gif';
var PrevURL = '".C_IMAGE_PATH."/$set_theme/ico_previous.gif';
var ZCounter = 100;
var Today = new Date();
var YearPart = Today.getFullYear();
var MonthPart = Today.getMonth();
var DayPart = Today.getDate();
var MonthNames = eval($MonthNames);
var MonthDays = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
var WeekDays = eval($WeekDays);
var MaxLength = 10; // Input maxlength
var InputSize = 12; // Input size


////////////////////////////////////////////////////////////////////////////////
// Affiche la date
////////////////////////////////////////////////////////////////////////////////
function PickDisplayDay(ClickedDay) {

  this.show();
  var MonthList = this.getMonthList();
  var DayList = this.getDayList();
  var YearField = this.getYearField();

  this.setPicked(this.displayed.yearValue, this.displayed.monthIndex, ClickedDay);

  // Format de la date a afficher YYYY-MM-DD
  ClickedDay = (ClickedDay < 10) ? '0' + ClickedDay : ClickedDay;
  var DisplayDate = this.displayed.yearValue + Delimiter + this.picked.monthPad + Delimiter + ClickedDay;

  // Affiche la date selectionnée
  eval('window.document.forms[0].'+this.inputDisplay+'.value = DisplayDate');

  // Test si la date affiché est < à la date à comparer
  eval('DisplayChkDate = window.document.forms[0].'+this.checkDate+'.value');
  if(DisplayChkDate < DisplayDate) {
    eval('window.document.forms[0].'+this.checkDate+'.value = DisplayDate');
  }

}


////////////////////////////////////////////////////////////////////////////////
// Construit le calendrier
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
// Retourne le nombre de jours dans un mois
////////////////////////////////////////////////////////////////////////////////
function GetDayCount(SomeYear, SomeMonth) {

  return ((SomeMonth == 1) && ((SomeYear % 400 == 0) || ((SomeYear % 4 == 0) && (SomeYear % 100 != 0)))) ? 29 : MonthDays[SomeMonth];
}


////////////////////////////////////////////////////////////////////////////////
// Reset le timer
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
	}
	else {
		var StopTimer = false;
		this.getCalendar().style.zIndex = ++ZCounter;
		this.getCalendar().style.visibility = 'visible';
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
// Objet Calendrier
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
// Fonction principale: Créer le formulaire
////////////////////////////////////////////////////////////////////////////////
function calendar(DateName, IsoDate, ChkDate) {

  // Creates the calendar object!
  eval(DateName + '_Object=new calendarObject(\'' + DateName + '\',\'' + IsoDate + '\',\'' + ChkDate + '\')');
  var InitialDate = eval(DateName + '_Object.picked.formatted');

  // ************************************************
  // Formulaire
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
    // Champ date affichée
    // ************************************************
    writeln('<input  type=\"text\" name=\"'+DateName+'\" id=\"'+DateName+'\" value=\"' + IsoDate + '\" size=\"'+InputSize+'\" maxlength=\"'+MaxLength+'\">');

    // ************************************************
    // Lien ouverture calendrier
    // ************************************************
    write('<a href=\"javascript:' + DateName + '_Object.show()\"><img class=\"calendarButton\" src=\"' + ImageURL + '\"></a>');

    // ************************************************
    // Calendrier
    // ************************************************
    writeln('<span id=\"' + DateName + '_ID\" style=\"position:absolute;visibility:hidden;width:' + (CellWidth * 7) + 'px;\" class=\"calendarSpan\" onMouseOver=\"' + DateName + '_Object.handleTimer(true)\" onMouseOut=\"' + DateName + '_Object.handleTimer(false)\">');
    
    writeln('<table width=\"' + (CellWidth * 7) + '\" cellspacing=\"0\" cellpadding=\"1\"><tr class=\"calendarDateInputHead\">');
    // Bouton précédent
    writeln('<td class=\"calendarDateInput \" id=\"' + DateName + '_Previous_ID\" onClick=\"' + DateName + '_Object.previous.go()\"><img src=\"' + PrevURL + '\"></td>');

    // Bouton mois en cours
    writeln('<td id=\"' + DateName + '_Current_ID\" class=\"calendarDateInput\" colspan=\"5\">' + eval(DateName + '_Object.displayed.fullName') + '</td>');

    // Bouton suivant
    writeln('<td id=\"' + DateName + '_Next_ID\" class=\"calendarDateInput\" onClick=\"' + DateName + '_Object.next.go()\"><img src=\"' + NextURL + '\"></td></tr><tr>');

    // Affiche la liste des jours
    for (var w=0;w<7;w++) { 
      writeln('<td class=\"calendarDateInputDay\">' + WeekDays[w] + '</td>');
    }
    writeln('</tr></table>');

    // Affiche le numéro des jours
    writeln('<span id=\"' + DateName + '_DayTable_ID\">' + eval(DateName + '_Object.buildCalendar()') + '</span></span>');
  }
}
";
?>
