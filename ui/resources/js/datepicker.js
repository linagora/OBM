/**
  This is a JavaScript library that will allow you to easily add some basic DHTML
  drop-down datepicker functionality to your Notes forms. This script is not as
  full-featured as others you may find on the Internet, but it's free, it's easy to
  understand, and it's easy to change.

  You'll also want to include a stylesheet that makes the datepicker elements
  look nice. An example one can be found in the database that this script was
  originally released with, at:

http://www.nsftools.com/tips/NotesTips.htm#datepicker

I've tested this lightly with Internet Explorer 6 and Mozilla Firefox. I have no idea
how compatible it is with other browsers.

version 1.5
December 4, 2005
Julian Robichaux -- http://www.nsftools.com

HISTORY
--  version 1.0 (Sept. 4, 2004):
Initial release.

--  version 1.1 (Sept. 5, 2004):
Added capability to define the date format to be used, either globally (using the
defaultDateSeparator and defaultDateFormat variables) or when the displayDatePicker
function is called.

--  version 1.2 (Sept. 7, 2004):
Fixed problem where datepicker x-y coordinates weren't right inside of a table.
Fixed problem where datepicker wouldn't display over selection lists on a page.
Added a call to the datePickerClosed function (if one exists) after the datepicker
is closed, to allow the developer to add their own custom validation after a date
has been chosen. For this to work, you must have a function called datePickerClosed
somewhere on the page, that accepts a field object as a parameter. See the
example in the comments of the updateDateField function for more details.

--  version 1.3 (Sept. 9, 2004)
Fixed problem where adding the <div> and <iFrame> used for displaying the datepicker
was causing problems on IE 6 with global variables that had handles to objects on
the page (I fixed the problem by adding the elements using document.createElement()
and document.body.appendChild() instead of document.body.innerHTML += ...).

--  version 1.4 (Dec. 20, 2004)
Added "targetDateField.focus();" to the updateDateField function (as suggested
by Alan Lepofsky) to avoid a situation where the cursor focus is at the top of the
form after a date has been picked. Added "padding: 0px;" to the dpButton CSS
style, to keep the table from being so wide when displayed in Firefox.

-- version 1.5 (Dec 4, 2005)
Added display=none when datepicker is hidden, to fix problem where cursor is
not visible on input fields that are beneath the date picker. Added additional null
date handling for date errors in Safari when the date is empty. Added additional
error handling for iFrame creation, to avoid reported errors in Opera. Added
onMouseOver event for day cells, to allow color changes when the mouse hovers
over a cell (to make it easier to determine what cell you're over). Added comments
in the style sheet, to make it more clear what the different style elements are for.
 */

var datePickerDivID = "datepicker";
var iFrameDivID = "datepickeriframe";
var datePickerTimer;
/**
  This is the main function you'll call from the onClick event of a button.
  Normally, you'll have something like this on your HTML page:

  Start Date: <input name="StartDate">
  <input type=button value="select" onclick="displayDatePicker('StartDate');">

  That will cause the datepicker to be displayed beneath the StartDate field and
  any date that is chosen will update the value of that field. If you'd rather have the
  datepicker display beneath the button that was clicked, you can code the button
  like this:

  <input type=button value="select" onclick="displayDatePicker('StartDate', this);">

  So, pretty much, the first argument (dateFieldName) is a string representing the
  name of the field that will be modified if the user picks a date, and the second
  argument (displayBelowThisObject) is optional and represents an actual node
  on the HTML document that the datepicker should be displayed below.

  In version 1.1 of this code, the dtFormat and dtSep variables were added, allowing
  you to use a specific date format or date separator for a given call to this function.
  Normally, you'll just want to set these defaults globally with the defaultDateSeparator
  and defaultDateFormat variables, but it doesn't hurt anything to add them as optional
  parameters here. An example of use is:

  <input type=button value="select" onclick="displayDatePicker('StartDate', false, 'dmy', '.');">

  This would display the datepicker beneath the StartDate field (because the
  displayBelowThisObject parameter was false), and update the StartDate field with
  the chosen value of the datepicker using a date format of dd.mm.yyyy
 */
function displayDatePicker(dateFieldName, displayBelowThisObject, dtFormat)
{
  var targetDateField = document.getElementsByName (dateFieldName).item(0);

  // if we weren't told what node to display the datepicker beneath, just display it
  // beneath the date field we're updating
  if (!displayBelowThisObject)
    displayBelowThisObject = targetDateField;

  // if a date format was given, update the dateFormat variable
  if (dtFormat)
    dateFormat = dtFormat;
  else
    dateFormat = 'ymd';
//    dateFormat = obm.vars.regexp.dateFormat;

  var x = displayBelowThisObject.offsetLeft;
  var y = displayBelowThisObject.offsetTop + displayBelowThisObject.offsetHeight ;

  // deal with elements inside tables and such
  var parent = displayBelowThisObject;
  while (parent.offsetParent) {
    parent = parent.offsetParent;
    x += parent.offsetLeft;
    y += parent.offsetTop ;
  }

  drawDatePicker(targetDateField, x, y);
}


/**
  Draw the datepicker object (which is just a table with calendar elements) at the
  specified x and y coordinates, using the targetDateField object as the input tag
  that will ultimately be populated with a date.

  This function will normally be called by the displayDatePicker function.
 */
function drawDatePicker(targetDateField, x, y)
{
  var dt = getFieldDate(targetDateField.value );

  // the datepicker table will be drawn inside of a <div> with an ID defined by the
  // global datePickerDivID variable. If such a div doesn't yet exist on the HTML
  // document we're working with, add one.
  if (!document.getElementById(datePickerDivID)) {
    // don't use innerHTML to update the body, because it can cause global variables
    // that are currently pointing to objects on the page to have bad references
    //document.body.innerHTML += "<div id='" + datePickerDivID + "' class='dpDiv'></div>";
    var newNode = document.createElement("div");
    newNode.setAttribute("id", "datepicker");
    newNode.setAttribute("style", "visibility: hidden;");
    document.body.appendChild(newNode);
    datePickerTimer = new HideTimer(newNode);
  }

  // move the datepicker div to the proper x,y coordinate and toggle the visiblity
  var pickerDiv = document.getElementById("datepicker");
  pickerDiv.style.position = "absolute";
  pickerDiv.style.left = x + "px";
  pickerDiv.style.top = y + "px";
  pickerDiv.style.visibility = (pickerDiv.style.visibility == "visible" ? "hidden" : "visible");
  pickerDiv.style.display = (pickerDiv.style.display == "block" ? "none" : "block");
  pickerDiv.style.zIndex = 10000;

  // draw the datepicker table
  refreshDatePicker(targetDateField.name, dt.getFullYear(), dt.getMonth(), dt.getDate());
  datePickerTimer.initTimer();
}


/**
  This is the function that actually draws the datepicker calendar.
 */
function refreshDatePicker(dateFieldName, year, month, day)
{
  var thisDay = new Date();

  if ((month >= 0) && (year > 0)) {
    thisDay = new Date(year, month, 1);
  } else {
    day = thisDay.getDate();
    thisDay.setDate(1);
  }
 
  var previousMonth = getDateMonthAndYear(thisDay, -1);
  var nextMonth = getDateMonthAndYear(thisDay, +1);
  var title = new Element('h1').adopt(
    new Element('a').setProperty('href','javascript: void(0);')
     .addEvent('click',refreshDatePicker.pass([dateFieldName,year - 1,month]))
     .appendText('<<'))
   .adopt(
      new Element('a').setProperty('href','javascript: void(0);')
       .addEvent('click',refreshDatePicker.pass([dateFieldName,previousMonth.year,previousMonth.month]))
       .appendText('<'))
   .appendText(obm.vars.labels.months[thisDay.getMonth()] + ' ' + thisDay.getFullYear())
   .adopt(
     new Element('a').setProperty('href','javascript: void(0);')
       .addEvent('click',refreshDatePicker.pass([dateFieldName,nextMonth.year,nextMonth.month]))
       .appendText('>'))
   .adopt(
     new Element('a').setProperty('href','javascript: void(0);')
       .addEvent('click',refreshDatePicker.pass([dateFieldName,year + 1,month]))
       .appendText('>>'));
  
  var labels = new Element('tr');
  for(i = 0; i < obm.vars.labels.dayShort.length; i++) {
    new Element('td').appendText(obm.vars.labels.dayShort[i]).injectInside(labels);
  }
  var content = new Element('tbody');
  var line = new Element('tr').injectInside(content);

  for (i = 0; i < thisDay.getDay(); i++) {
    new Element('td').addClassName('downlight').injectInside(line);
  }
  
  do {
    dayNum = thisDay.getDate();
    var td = new Element('td');
    td.addEvent('click',updateDateField.pass([dateFieldName,getDateString(thisDay)]));
    if (dayNum == day) {
      td.addEvent("mouseout",function () {this.className='highlight';});
      td.addClassName('highlight');
    } else {
      td.addEvent("mouseout",function () {this.className='';});
    }
    td.addEvent("mouseover",function () {this.className='hover';});
    td.appendText(dayNum).injectInside(line);
    if (thisDay.getDay() == 6) {
      var line = new Element('tr').injectInside(content);
    }
    thisDay.setDate(thisDay.getDate() + 1);
  } while (thisDay.getDate() > 1)

  if (thisDay.getDay() > 0) {
    for (i = 7; i > thisDay.getDay(); i--) {
      new Element('td').addClassName('downlight')
                       .injectInside(line);
    }
  }

  var today = new Element('a').setProperty('href','javascript:void(0)')
                              .addEvent('click',refreshDatePicker.pass([dateFieldName]))
                              .appendText(obm.vars.labels.today);
  var table = new Element('table').adopt(new Element('thead').adopt(labels))
                                  .adopt(content);

  $("datepicker").setHTML('');
  $("datepicker").adopt(title).adopt(table).adopt(today);
  overListBoxFix("datepicker");
  return false;
}


/**
  Convenience function for writing the code for the buttons that bring us back or forward
  a month.
 */
function getGoToSource(dateFieldName, dateVal, adjust)
{
  var newMonth = (dateVal.getMonth () + adjust) % 12;
  var newYear = dateVal.getFullYear() + parseInt((dateVal.getMonth() + adjust) / 12);
  if (newMonth < 0) {
    newMonth += 12;
    newYear += -1;
  }

  return "refreshDatePicker('" + dateFieldName + "', " + newYear + ", " + newMonth + ");return false;";
}

function getDateMonthAndYear(dateVal, adjust) {
  var newMonth = (dateVal.getMonth () + adjust) % 12;
  var newYear = dateVal.getFullYear() + parseInt((dateVal.getMonth() + adjust) / 12);
  if (newMonth < 0) {
    newMonth += 12;
    newYear += -1;
  }
  
  return {year:newYear,month:newMonth} ;
}

/**
  Convert a JavaScript Date object to a string, based on the dateFormat 
  variables at the beginning of this script library.
 */
function getDateString(dateVal)
{
  var dayString = "00" + dateVal.getDate();
  var monthString = "00" + (dateVal.getMonth()+1);
  dayString = dayString.substring(dayString.length - 2);
  monthString = monthString.substring(monthString.length - 2);
  dateSeparator = '-';
  switch (dateFormat) {
    case "dmy" :
      return dayString + dateSeparator + monthString + dateSeparator + dateVal.getFullYear();
    case "ymd" :
      return dateVal.getFullYear() + dateSeparator + monthString + dateSeparator + dayString;
    case "mdy" :
    default :
      return monthString + dateSeparator + dayString + dateSeparator + dateVal.getFullYear();
  }
}


/**
  Convert a string to a JavaScript Date object.
 */
function getFieldDate(dateString)
{
  var dateVal;
  var dArray;
  var d, m, y;

  if(dateString.match(/[0-9]{4}-[0-1][0-9]-[0-3][0-9]/)) 
    dFormat = "ymd";
  else 
    dFormat = dateFormat;
  
  try {
    dArray = splitDateString(dateString);
    if (dArray) {
      switch (dFormat) {
        case "dmy" :
          d = parseInt(dArray[0], 10);
          m = parseInt(dArray[1], 10) - 1;
          y = parseInt(dArray[2], 10);
          break;
        case "ymd" :
          d = parseInt(dArray[2], 10);
          m = parseInt(dArray[1], 10) - 1;
          y = parseInt(dArray[0], 10);
          break;
        case "mdy" :
        default :
          d = parseInt(dArray[1], 10);
          m = parseInt(dArray[0], 10) - 1;
          y = parseInt(dArray[2], 10);
          break;
      }
      dateVal = new Date(y, m, d);
    } else if (dateString) {
      dateVal = new Date(dateString);
    } else {
      dateVal = new Date();
    }
  } catch(e) {
    dateVal = new Date();
  }

  return dateVal;
}


/**
  Try to split a date string into an array of elements, using common date separators.
  If the date is split, an array is returned; otherwise, we just return false.
 */
function splitDateString(dateString)
{
  var dArray;
  if (dateString.indexOf("/") >= 0)
    dArray = dateString.split("/");
  else if (dateString.indexOf(".") >= 0)
    dArray = dateString.split(".");
  else if (dateString.indexOf("-") >= 0)
    dArray = dateString.split("-");
  else if (dateString.indexOf("\\") >= 0)
    dArray = dateString.split("\\");
  else
    dArray = false;

  return dArray;
}

/**
  Update the field with the given dateFieldName with the dateString that has been passed,
  and hide the datepicker. If no dateString is passed, just close the datepicker without
  changing the field value.

  Also, if the page developer has defined a function called datePickerClosed anywhere on
  the page or in an imported library, we will attempt to run that function with the updated
  field as a parameter. This can be used for such things as date validation, setting default
  values for related fields, etc. For example, you might have a function like this to validate
  a start date field:

  function datePickerClosed(dateField)
  {
  var dateObj = getFieldDate(dateField.value);
  var today = new Date();
  today = new Date(today.getFullYear(), today.getMonth(), today.getDate());

  if (dateField.name == "StartDate") {
  if (dateObj < today) {
// if the date is before today, alert the user and display the datepicker again
alert("Please enter a date that is today or later");
dateField.value = "";
document.getElementById(datePickerDivID).style.visibility = "visible";
adjustiFrame();
} else {
// if the date is okay, set the EndDate field to 7 days after the StartDate
dateObj.setTime(dateObj.getTime() + (7 * 24 * 60 * 60 * 1000));
var endDateField = document.getElementsByName ("EndDate").item(0);
endDateField.value = getDateString(dateObj);
}
}
}

 */
function updateDateField(dateFieldName, dateString)
{
  var targetDateField = document.getElementsByName (dateFieldName).item(0);
  if (dateString)
    targetDateField.value = dateString;
  
  var pickerDiv = $("datepicker");
  pickerDiv.style.visibility = "hidden";
  pickerDiv.style.display = "none";

  overListBoxFix("datepicker");
  if(targetDateField.onchange) 
    targetDateField.onchange();
  // after the datepicker has closed, optionally run a user-defined function called
  // datePickerClosed, passing the field that was just updated as a parameter
  // (note that this will only run if the user actually selected a date from the datepicker)
  if ((dateString) && (typeof(datePickerClosed) == "function"))
    datePickerClosed(targetDateField);
}

