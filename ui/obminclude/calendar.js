<?php
$extra_js .= "
var calendarWindow = null;
var calendarColors = new Array();
calendarColors['bgColor'] = '#000';
calendarColors['borderColor'] = '#333366';
calendarColors['headerBgColor'] = '#143464';
calendarColors['headerColor'] = '#FFFFFF';
calendarColors['dateBgColor'] = '#8493A8';
calendarColors['dateColor'] = '#004080';
calendarColors['dateHoverBgColor'] = '#FFFFFF';
calendarColors['dateHoverColor'] = '#8493A8';
var calendarMonths = new Array('$l_monthsofyear[0]', '$l_monthsofyear[1]', '$l_monthsofyear[2]', '$l_monthsofyear[3]', '$l_monthsofyear[4]', '$l_monthsofyear[5]', '$l_monthsofyear[6]', '$l_monthsofyear[7]', '$l_monthsofyear[8]', '$l_monthsofyear[9]', '$l_monthsofyear[10]', '$l_monthsofyear[11]');
var calendarWeekdays = new Array('$l_daysofweekfirst[0]', '$l_daysofweekfirst[1]', '$l_daysofweekfirst[2]', '$l_daysofweekfirst[3]', '$l_daysofweekfirst[4]', '$l_daysofweekfirst[5]', '$l_daysofweekfirst[6]', '$l_daysofweekfirst[0]');
var calendarUseToday = true;
var calendarFormat = 'y-m-d';
var calendarStartMonday = true;


function getCalendar(in_dateField,url) 
{
    if (calendarWindow && !calendarWindow.closed) {
        alert('Calendar window already open.  Attempting focus...');
        try {
            calendarWindow.focus();
        }
        catch(e) {}
        
        return false;
    }

    var cal_width = 284;
//    var cal_height = 264; // XXXXX why this is insufficient now with mozilla
    var cal_height = 348;

    if ((document.all) && (navigator.userAgent.indexOf(\"Konqueror\") == -1)) {
        cal_width = 284;
    }

    calendarTarget = in_dateField;
    calendarWindow = window.open(url, 'dateSelectorPopup','toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=0,dependent=no,width='+cal_width+',height='+cal_height);

    return false;
}


function killCalendar() 
{
    if (calendarWindow && !calendarWindow.closed) {
        calendarWindow.close();
    }
}
";
php?>
