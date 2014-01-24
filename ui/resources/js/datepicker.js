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

in the style sheet, to make it more clear what the different style elements are for.
 */

var datePickerDivID = "datepicker";
var iFrameDivID = "datepickeriframe";
var datePickerTimer;

function displayDatePicker(dateField, displayBelowThisObject, dtFormat) {
  var targetDateField = $(dateField);

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

  var x = displayBelowThisObject.getPosition().x;
  var topMargin = 3;
  var y = displayBelowThisObject.getPosition().y + displayBelowThisObject.getSize().y + topMargin;

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
    newNode.setAttribute("id", datePickerDivID);
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
  refreshDatePicker(targetDateField, dt.getFullYear(), dt.getMonth(), dt.getDate());
  datePickerTimer.initTimer();
}


/**
  This is the function that actually draws the datepicker calendar.
 */
function refreshDatePicker(dateField, year, month, day)
{
  var thisDay = new Date();

  if ((month >= 0) && (year > 0)) {
    thisDay = new Date(year, month, 1);
  } else {
    day = thisDay.getDate();
    month = thisDay.getMonth();
    year = thisDay.getFullYear();
    thisDay.setDate(1);
  }
 
  var previousMonth = getDateMonthAndYear(thisDay, -1);
  var nextMonth = getDateMonthAndYear(thisDay, +1);
  var title = new Element('h1').adopt(
    new Element('span').setProperty('class', 'leftarrows')
      .adopt(
        new Element('a').setProperty('href','javascript: void(0);')
        .addEvent('click',refreshDatePicker.pass([dateField,year - 1,month]))
        .appendText('<<')
      )
      .adopt(
        new Element('a').setProperty('href','javascript: void(0);')
        .addEvent('click',refreshDatePicker.pass([dateField,previousMonth.year,previousMonth.month]))
        .appendText('<')
      )
    )
   .appendText(obm.vars.labels.months[thisDay.getMonth()] + ' ' + thisDay.getFullYear())
   .adopt(
      new Element('span').setProperty('class', 'rightarrows')
        .adopt(
          new Element('a').setProperty('href','javascript: void(0);')
          .addEvent('click',refreshDatePicker.pass([dateField,nextMonth.year,nextMonth.month]))
          .appendText('>')
        )
        .adopt(
          new Element('a').setProperty('href','javascript: void(0);')
          .addEvent('click',refreshDatePicker.pass([dateField,year + 1,month]))
          .appendText('>>')
        )
    );
  
  var labels = new Element('tr');
  for(i = 0; i < obm.vars.labels.dayShort.length; i++) {
    j = (i + obm.vars.consts.weekStart) % 7;
    new Element('td').appendText(obm.vars.labels.dayShort[j]).injectInside(labels);
  }
  var content = new Element('tbody');
  var line = new Element('tr').injectInside(content);

  for (i = obm.vars.consts.weekStart; i != thisDay.getDay(); i = (i+1)%7) {
    new Element('td').addClass('downlight').injectInside(line);
  }
  
  do {
    dayNum = thisDay.getDate();
    var td = new Element('td');
    td.addEvent('click',updateDateField.pass([dateField,getDateString(thisDay)]));
    if (dayNum == day) {
      td.addEvent("mouseout",function () {this.className='highlight';});
      td.addClass('highlight');
    } else {
      td.addEvent("mouseout",function () {this.className='';});
    }
    td.addEvent("mouseover",function () {this.className='hover';});
    td.appendText(dayNum).injectInside(line);
    if (thisDay.getDay() == ((obm.vars.consts.weekStart + 6) %7)) {
      var line = new Element('tr').injectInside(content);
    }
    thisDay.setDate(thisDay.getDate() + 1);
  } while (thisDay.getDate() > 1)

  while ((thisDay.getDay()) != obm.vars.consts.weekStart) {
    thisDay.setDate(thisDay.getDate() + 1);
    new Element('td').addClass('downlight')
                     .injectInside(line);
  }

  var today = new Element('a').setProperty('href','javascript:void(0)')
                              .addEvent('click',refreshDatePicker.pass([dateField]))
                              .appendText(obm.vars.labels.today);
  var table = new Element('table').adopt(new Element('thead').adopt(labels))
                                  .adopt(content);

  $("datepicker").innerHTML = '';
  $("datepicker").adopt(title).adopt(table).adopt(today);
  overListBoxFix("datepicker");
  return false;
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
  switch (obm.vars.regexp.dateFormat) {
    case "d/m/Y" :
      return dayString + '/' + monthString + '/' + dateVal.getFullYear();
    case "m/d/Y" :
      return monthString + '/' + dayString + '/' + dateVal.getFullYear();
    case "Y-m-d" :
    default :
      return dateVal.getFullYear() + '-' + monthString + '-' + dayString;
  }
}

function guessDateFormat(fieldDate) {
  reg = new Object();
  reg['Y-m-d'] = "^[0-9]{4}[-\\/][01][0-9][-\\/][0123][0-9]$";
  reg['d/m/Y'] = "^[23][0-9][-\\/]?[01][0-9][-\\/]?[0-9]{4}$";
  reg['m/d/Y'] = "^[01][0-9][-\\/]?[23][0-9][-\\/]?[0-9]{4}$";
  reg['user'] = "^[0123][0-9][-\\/]?[0123][0-9][-\\/]?[0-9]{4}$";

  for(format in reg) {
    if(fieldDate.match(reg[format])) {
      return format;
    }
  }
  if(!isNaN(fieldDate) && fieldDate > 31130000)  {
    return 't';
  }
  return false;
}

/**
 Convert a string to a iso date format string
 */
function getFieldIsoDate(dateString) {
  var dateVal = getFieldDate(dateString);
  var dayString = "00" + dateVal.getDate();
  var monthString = "00" + (dateVal.getMonth()+1);
  dayString = dayString.substring(dayString.length - 2);
  monthString = monthString.substring(monthString.length - 2);
  return dateVal.getFullYear() + '-' + monthString + '-' + dayString;
}

/**
  Convert a string to a JavaScript Date object.
 */
function getFieldDate(dateString, exact)
{
  var dateVal;
  var dArray;
  var d, m, y;

  type = guessDateFormat(dateString);
  if (type == "user") {
    type = obm.vars.regexp.dateFormat;
  }
  try {
    switch (type) {
      case "t" :
        return new Date(dateString * 1000);
        break;
      case "Y-m-d" :
        dArray = splitDateString(dateString);
        d = parseInt(dArray[2], 10);
        m = parseInt(dArray[1], 10) - 1;
        y = parseInt(dArray[0], 10);
        return new Date(y, m, d);
        break;
      case "d/m/Y" :
        dArray = splitDateString(dateString);
        d = parseInt(dArray[0], 10);
        m = parseInt(dArray[1], 10) - 1;
        y = parseInt(dArray[2], 10);
        return new Date(y, m, d);
        break;
      case "m/d/Y" :
        dArray = splitDateString(dateString);
        d = parseInt(dArray[1], 10);
        m = parseInt(dArray[0], 10) - 1;
        y = parseInt(dArray[2], 10);
        return new Date(y, m, d);
        break;      
      default :
        if( exact == true) {
          return false;
        } else {
          return new Date();
        }
    }
  }catch(e) {
    if( exact == true) {
      return false;
    } else {
      return new Date();
    }    
  }
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
function updateDateField(dateField, dateString)
{
  var targetDateField = $(dateField);
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


// This will generate a datePicker widget for all element with
// the css class "datePicker".
// The element with the class datePicker must have a name attribute.

function datePickerGenerator() {
  elements = $$('.datePicker');
  elements.each(function(element){
    element.setProperty('autocomplete','off');
    var span = new Element('span').injectBefore(element).addClass('NW');
    element.dispose();
    element.injectInside(span);
    var img = new Element('img');
    img.setAttribute("src", obm.vars.images.datePicker);
    img.injectInside(span);
    img.addEvent('click', function(e){
      displayDatePicker(element);
    });
  });
}



/*
 * Mini calendar portlet
 * 
 */
function miniCal(year, month, view) {
  var thisDay = new Date();

  if ((month >= 0) && (year > 0)) {
    thisDay = new Date(year, month, 1);
  } else {
    month = thisDay.getMonth();
    year = thisDay.getFullYear();
    thisDay.setDate(1);
  }
  obm.miniCalendar.currentMonth = month;

  var previousMonth = getDateMonthAndYear(thisDay, -1);
  var nextMonth = getDateMonthAndYear(thisDay, +1);

  var header = new Element('tr')
    .adopt(new Element('td').adopt(new Element('a').setProperty('href','javascript: void(0);')
       .addEvent('click',miniCal.pass([previousMonth.year,previousMonth.month, view]))
       .appendText("«")))
    .adopt(new Element('td').setProperty('colspan', '5')
       .adopt(new Element('a').setProperty('href', 'javascript: obm.calendarManager.showMonth('+thisDay.getTime()+');')
       .appendText(obm.vars.labels.months[thisDay.getMonth()] + ' ' + thisDay.getFullYear())))
    .adopt(new Element('td').adopt(new Element('a').setProperty('href','javascript: void(0);')
       .addEvent('click',miniCal.pass([nextMonth.year,nextMonth.month, view]))
       .appendText('»')));

  var labels = new Element('tr').addClass('labels');
  for(i = 0; i < obm.vars.labels.dayShort.length; i++) {
    new Element('td').appendText(obm.vars.labels.dayShort[(i + obm.vars.consts.weekStart) % 7]).injectInside(labels);
  }
  var content = new Element('tbody');
  var mousewheel = 'mousewheel';
  if (Obm.Browsers.Engines.gecko()) mousewheel = 'DOMMouseScroll';
  content.addEvent(mousewheel, function(e) {
    if(e.event.wheelDelta < 0 || e.event.detail > 0) {
      miniCal(nextMonth.year,nextMonth.month, view);
    } else {
      miniCal(previousMonth.year,previousMonth.month, view);
    }
    e.stop();
  });
  var line = new Element('tr').injectInside(content);

  if (thisDay.getDay() == 0) {
    thisDay.setDate(thisDay.getDate() - 6);
  } else {
    var day = thisDay.getDay();
    var d = thisDay.getDate();
    if ((day-obm.vars.consts.weekStart) == 0) {
      thisDay.setDate(d - 7);
    } else {
      thisDay.setDate(d - (day-obm.vars.consts.weekStart));
    }
  }
  var today = new Date();
  var nlines = 0;
  do {
    var dayNum = thisDay.getDate();
    var d = new Obm.DateTime(thisDay.getTime());
    var iso = d.format('Y-m-d');
    var td = new Element('td').setProperty('id', thisDay.getTime()).addClass('minical minical_'+iso);
    if (thisDay.getMonth() == today.getMonth() && today.getDate() == dayNum && thisDay.getFullYear() == today.getFullYear()) {
      td.addClass('today');
    }
    if (obm.vars.consts.selectedDays.indexOf(iso)>=0) td.addClass('selected');
    if (thisDay.getMonth() != month) td.addClass('down');
    if (thisDay.getDay() == 0 || thisDay.getDay() == 6) td.addClass('weekend');

    if (view == 'agenda') {
      // Agenda view
      td.addEvent("mouseover",function () {
        if (obm.calendarManager.customStart) {
          obm.miniCalendar.clearSelection();      
          start = Math.min(obm.calendarManager.customStart, this.id/1000);
          end = Math.max(obm.calendarManager.customStart, this.id/1000);
          nbdays = Math.ceil((end-start)/86400 +1);
          current = new Obm.DateTime(start*1000);
          for(i=0;i<nbdays;i++) {
            obm.miniCalendar.select(current.format('Y-m-d'));
            current.setDate(current.getDate()+1);
          }
        }
        this.addClass('hover');
      });
      td.addEvent("mouseout",function () {this.removeClass('hover')});
      td.addEvent('mousedown', function() {
        document.onselectstart = function() {
          // see https://www.obm.org/bugzilla/show_bug.cgi?id=1053
          return false;
        }
        obm.calendarManager.customStart=this.id/1000;
      });
      td.addEvent('mouseup', function() {
        document.onselectstart = function() {
          // see https://www.obm.org/bugzilla/show_bug.cgi?id=1053
          return true;
        }
        if (obm.calendarManager.customStart) {
          if (this.hasClass('selected')) {
            if (obm.vars.consts.nbDisplayedDays == 1) {
              obm.vars.consts.nbDisplayedDays = 7;
              obm.calendarManager.customStart = false;
              obm.calendarManager.showWeek(this.id/1000);
            } else {
              obm.calendarManager.showDay(this.id/1000);
              obm.calendarManager.customStart = false;
            }
          } else {
            if (obm.vars.consts.nbDisplayedDays == 1) {
              obm.calendarManager.showDay(this.id/1000);
              obm.calendarManager.customStart = false;
            } else {
              obm.vars.consts.nbDisplayedDays = 7;
              obm.calendarManager.customStart = false;
              obm.calendarManager.showWeek(this.id/1000);
            }
          }
        }
      });
    } else {
      // Planning & List view
      td.addEvent('mouseover', function() {this.addClass('hover');});
      td.addEvent('mouseout',function () {this.removeClass('hover')});
      td.addEvent('click', function() {
        var ds = new Obm.DateTime(this.id.toInt());
        window.location.href = 'calendar_index.php?date='+ds.format('Y-m-d');
      }); 
    }

    td.appendText(dayNum).injectInside(line);

    if (thisDay.getDay() == ((obm.vars.consts.weekStart + 6) %7)) {
      var line = new Element('tr').injectInside(content);
      nlines++;      
    }
    thisDay.setDate(thisDay.getDate() + 1);
  } while (nlines < 6)

  var table = new Element('table').addClass('miniCalendar2')
    .adopt(new Element('thead').adopt(header))
    .adopt(labels).adopt(content);

  $('obmMiniCalendar').innerHTML = '';
  $('obmMiniCalendar').adopt(table);
  $('obmMiniCalendar').addEvent('mouseenter', function () {
      obm.calendarManager.customStart = false;
    }); 
  $('obmMiniCalendar').addEvent('mouseleave', function () {
      if (obm.calendarManager.customStart) {
        obm.miniCalendar.clearSelection(); 
        obm.calendarManager.customStart = false;
      }});  

  return false;
}
