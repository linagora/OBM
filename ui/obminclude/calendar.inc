<style>
body { 
.calendar {
  position: absolute;
  left: 5px;
  top: 5px;
  width: 400px;
}
.dayofweek { 
  font-size: 13px;
} 
.month { 
  font-size: 13px;
}
.date { 
  width: 10%;
  height: 10px;
  padding-right: 3px;
  text-align: right;
  vertical-align: bottom;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  cursor: hand;
}
.selected { 
  width: 10%;
  height: 10px;
  padding-right: 3px;
  text-align: right;
  vertical-align: bottom;
  font-weight: bold;
  font-size: 13px;
  cursor: pointer;
  cursor: hand;
} 
.empty { 
  width: 10%;
  height: 10px;
} 
a {
  font-weight: bold;
}
form {
  padding: 0px;
  margin: 0px;
}
</style>

<script language="JavaScript">
// {{{ y2k()

function y2k(number)
{
    return (number < 1000) ? number + 1900 : number; 
}

// }}}
// {{{ _rgb2hex()

function _rgb2hex(red,green,blue) 
{
    var html_red = red.toString(16).toUpperCase();
    var html_green = green.toString(16).toUpperCase();
    var html_blue = blue.toString(16).toUpperCase();
    if (html_red.length == 1) {
        html_red = "0" + html_red
    }

    if (html_green.length == 1) {
        html_green = "0" + html_green
    }

    if (html_blue.length == 1) {
        html_blue = "0" + html_blue
    }

    return '#' + html_red + html_green + html_blue;
}
 
// }}}
// {{{ _hex2rgb()

function _hex2rgb(htmlcode) 
{
    var htmlcode = htmlcode.replace(/#/,'');
    var rgb = new Array();
    rgb["red"] = parseInt(htmlcode.substr(0,2),16);
    rgb["green"] = parseInt(htmlcode.substr(2,2),16);
    rgb["blue"] = parseInt(htmlcode.substr(4,2),16);
    return rgb;
}
 
// }}}
// {{{ getDarkColor()

function getDarkColor(htmlcode) 
{
    var decimalcolor = _hex2rgb(htmlcode);
    decimalcolor["red"] = Math.max(0,decimalcolor["red"]-40);
    decimalcolor["green"] = Math.max(0,decimalcolor["green"]-40);
    decimalcolor["blue"] = Math.max(0,decimalcolor["blue"]-40);
    return _rgb2hex(decimalcolor["red"],decimalcolor["green"],decimalcolor["blue"]);
}

// }}}

if (typeof(window.opener.calendarStartMonday) == 'undefined') {
    var calendarStartMonday = false;
}
else {
    var calendarStartMonday = window.opener.calendarStartMonday ? true : false;
}

// get the date format
if (typeof(window.opener.calendarFormat) == 'undefined') {
    var calendarFormat = 'y/m/d';
}
// we are doing on a leap of faith here that the user has 'm','d' and 'y' only in the format
else {
    var calendarFormat = window.opener.calendarFormat;
}

// get the calendarColors variable and setup the colors
if (typeof(window.opener.calendarColors) == 'undefined') {
    alert('Please configure the colors by setting the calendarColors array!');
    window.close();
}

// grab the color settings
var calendarColors = window.opener.calendarColors;
// set defaults for the selected date to be a darker color
if (typeof(calendarColors['dateSelectedBgColor']) == 'undefined') {
    calendarColors['dateSelectedBgColor'] = getDarkColor(calendarColors['dateBgColor']);
}

if (typeof(calendarColors['dateSelectedColor']) == 'undefined') {
    calendarColors['dateSelectedColor'] = calendarColors['dateColor'];
}

if (typeof(window.opener.calendarMonths) == 'undefined' || window.opener.calendarMonths.length != 12) {
    var calendarMonths = new Array('January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December');
}
else {
    var calendarMonths = window.opener.calendarMonths;
}

if (typeof(window.opener.calendarWeekdays) == 'undefined' || window.opener.calendarWeekdays.length != 8) {
    // we have two sundays to accomodate for calendars starting on monday
    var calendarWeekdays = new Array('S', 'M', 'T', 'W', 'T', 'F', 'S', 'S');
}
else {
    var calendarWeekdays = window.opener.calendarWeekdays;
}

var calendarDays  = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);

//images
var right_still = "arrows_r_still.gif"
var right_anim = "arrows_r_anim.gif"
var left_still = "arrows_l_still.gif"
var left_anim = "arrows_l_anim.gif"

// get the reference to the target element and setup the date
var targetDateField = window.opener.calendarTarget;
var dateString = targetDateField.value;

if (dateString != '' && 
   (typeof(window.opener.calendarUseToday) == 'undefined' || !window.opener.calendarUseToday)) {
    // convert the user format of the date into something we use to make a javascript Date object
    // we need to pad with placeholders to get the rigth offset
    tmp_format = calendarFormat.replace(/m/i, 'mm').replace(/d/i, 'dd').replace(/y/i, 'yyyy');
    tmp_yOffset = tmp_format.indexOf('y');
    tmp_mOffset = tmp_format.indexOf('m');
    tmp_dOffset = tmp_format.indexOf('d');
    var today = new Date(dateString.substring(tmp_yOffset, tmp_yOffset + 4), dateString.substring(tmp_mOffset, tmp_mOffset +
 2) - 1, dateString.substring(tmp_dOffset, tmp_dOffset + 2));

    if ((today == "Invalid Date") || (isNaN(today))) {
        var today = new Date();
    }
}
// use today's date
else {
    var today = new Date();
}

var day = today.getDate();
var year  = y2k(today.getYear());
var month = today.getMonth();
<?php
  if($month != "") {
    echo " month = $month;";
  }
  if($year != "") {
    echo " year = $year;";
  }
?>
var currentDay = day;
var currentYear = year;
var currentMonth = month;

function calendar(month, year) 
{
    var realMonth = parseInt(month) + 1;
    // we need to put in the zero placeholders
    if (realMonth < 10) {
        realMonth = '0' + realMonth;
    }

    var realDate = '';
    var output = '';
       
    firstDay = new Date(year, month, 1);
    startDay = firstDay.getDay();
    weekdayOffset = 0;
    if (parent.calendarStartMonday) {
        if (startDay) {
            startDay--;
        }
        else {
            startDay = 6;
        }

        weekdayOffset++;
    }


    // Determined whether this is a leap year or not
    if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
        parent.calendarDays[1] = 29; 
    }
    else {
        parent.calendarDays[1] = 28;
    }

    // we break up the output so it flushes the buffer frequently
    output += '<form><table border="0" cellspacing="0" cellpadding="3" width="100%"><tr><td align="left"><img src="<?php echo "/images/$set_theme/$ico_mini_larrow";?>" id="arrow_left_id" border="0" usemap="#arrow_left_map"></td><td align="center">';
	    tmp_text = "&month=";
	    output += '<select onchange="window.location=window.location + tmp_text + this.options[this.selectedIndex].value ;" title="Month Jump">';
    for (month_cnt = 0; month_cnt < 12; month_cnt++) {
        output += '<option value='+month_cnt;
        if (month_cnt == thisMonth) {
            output += ' selected';
        }

        output += '>' + parent.calendarMonths[month_cnt] + '</option>';
    }
    tmp_text2 = "&year=";
    output += '</select> <select onchange="window.location=window.location + tmp_text2 + this.options[this.selectedIndex].value ;" title="Year Jump">';
    for (year_cnt = 1; year_cnt <= 21; year_cnt++) {
        output += '<option value="'+yeararray[year_cnt]+'"';
        if (yeararray[year_cnt] == thisYear) {
            output += ' selected';
        }
        output += '>'+yeararray[year_cnt]+'</option>';
    }

    output += '</select></td>';
output += '<td align="right"><img src="<?php echo "/images/$set_theme/$ico_mini_rarrow"; ?>" name="arrow_right_id" id="arrow_right_id" border="0" usemap="#arrow_right_map"></td></tr>\n';
    output += '<tr><td colspan="3" valign="top"><table border="0" cellspacing="1" cellpadding="0" width="100%">';
    output += '<tr class="dayofweek" style="background-color: '+calendarColors['headerBgColor']+';">';
    // print out the days of the week
    for (i = weekdayOffset; i < 7 + weekdayOffset; i++) {
        output += '<th style="color: '+calendarColors['headerColor']+';">' + parent.calendarWeekdays[i] + '</th>';
    }

    output += '\n<tr>';

    var column = 0;
    for (i = 0; i < startDay; i++) {
        output += '<td class="empty" style="background-color: '+calendarColors['bgColor']+';">&nbsp</td>';
        column++;
    }

    for (i = 1; i <= parent.calendarDays[month]; i++) {
        realDate = i;
        // add the zero place holder
        if (realDate < 10) {
            realDate = '0' + realDate
        }
	 
        if ((i == parent.currentDay)  && (month == parent.currentMonth) && (year == parent.currentYear)) {
            output += '<td class="selected" style="background-color: '+calendarColors['dateSelectedBgColor']+'; color: '+calendarColors['dateSelectedColor']+';" height="10" onmouseover="colorize(this,1,\'dateSelected\');" onmouseout="colorize(this,0,\'dateSelected\');" onclick="sendDate('+realMonth+', '+realDate+', '+year+');">';
            if(document.layers) {
         output += '<a href="javascript: void(0);" onclick="sendDate('+realMonth+', '+realDate+', '+year+');">'+i+'</a>&nbsp;&nbsp;';
            }
            else {
	        output += '<br />' + i;
            }
	    output += '</td>';
        }
        else {
            output += '<td class="date" style="background-color: '+calendarColors['dateBgColor']+'; color: '+calendarColors['dateColor']+';" height="10" onmouseover="colorize(this,1,\'date\');" onmouseout="colorize(this,0,\'date\');" onclick="sendDate('+realMonth+', '+realDate+', '+year+');">';
            if(document.layers) {
	        output += '<a href="javascript: void(0);" onclick="sendDate('+realMonth+', '+realDate+', '+year+');">'+i+'</a>&nbsp;&nbsp;';
            }
            //put in some line breaks to fill up the cell
	    else {
	        output += '<br />'+i
	    }
            output += '</td>';
        }

        column++;
        // end the week
        if (column == 7) {
            output += '</tr>\n<tr>';
            column = 0;
        }
    }

    for(j = parent.calendarDays[month]; j < 42-startDay; j++) {
        output += '<td class="empty" style="background-color: '+calendarColors['bgColor']+';" height="10">&nbsp</td>';
        column++;
        // end the week
        if (column == 7) {
            output += '</tr>\n<tr>';
            column = 0;
        }
    }
     
    output += '</tr></table></td></tr></table></form>';
     
    return output;
}

// }}}
// {{{ printCalendar()

function printCalendar(whichMonth,whichYear)
{
    var output = '';

    if (document.layers) {
        var output = '<body link="' + calendarColors['dateColor'] + '" alink="' + calendarColors['dateSelectedColor']+ '"><layer bgcolor="'+calendarColors['bgColor']+'">';
    }
    else {
        var output = '<body><style>select { font-weight: bold; font-size: 12px; }</style><div class="calendar" style="border: 2px solid ' + calendarColors['borderColor'] + '; background-color: '+calendarColors['bgColor']+';">';
    }
    output += calendar(whichMonth,thisYear);
    if (!document.layers) {
        output += '</div>';
    }
    else {
        output += '</layer>';
    }
    m = whichMonth;
    nm = nextmonth(whichMonth,whichYear);
    pm = previousmonth(whichMonth,whichYear);
    ny = yearforward(whichYear);
    py = yearback(whichYear);
    tmp_text="&month=";
    tmp_text2="&year=";
    output += '<map name="arrow_right_map"><area shape="RECT" coords="1,1,13,17" onmouseover="animate_right(\'arrow_right_id\',1)" onmouseout="animate_right(\'arrow_right_id\',0)" href="javascript:window.location=window.location + tmp_text + nm" title="Next Month" alt="Next Month"><area shape="RECT" coords="22,1,44,17" onmouseover="animate_right(\'arrow_right_id\',1)" onmouseout="animate_right(\'arrow_right_id\',0)"  href="javascript:window.location=window.location + tmp_text + m + tmp_text2 + ny" title="Next Year" alt="Next Year"></map><map name="arrow_left_map"><area shape="RECT" coords="1,1,23,17" onmouseover="animate_left(\'arrow_left_id\',1)" onmouseout="animate_left(\'arrow_left_id\',0)" onclick="yearback();" href="javascript:window.location=window.location + tmp_text + m + tmp_text2 + py" title="Previous Year" alt="Previous Year"><area shape="RECT" coords="32,1,45,17" onmouseover="animate_left(\'arrow_left_id\',1)" onmouseout="animate_left(\'arrow_left_id\',0)"  href="javascript:window.location=window.location + tmp_text + pm" title="Previous Month" alt="Previous Month"></map>';
    output += '</body>';

    document.open();
    document.write(output);
    document.write();
}

// }}}
// {{{ animate_right() 

function animate_right(imagename,imageswitch) 
{
    if(document.all) {
        imagename = eval("document.all." + imagename)
    }
    else if(document.getElementById) {
        imagename = document.getElementById(imagename)
    }

    if (!document.layers) {
        if (imageswitch == 1) {
            imagename.src = <?php echo "\"/images/$set_theme/$ico_mini_rarrow2\""; ?>;
        }
        else {
            imagename.src = <?php echo "\"/images/$set_theme/$ico_mini_rarrow\""; ?>;
        }
    }
}

// }}}
// {{{ animate_left()

function animate_left(imagename,imageswitch) 
{
    if (document.all) {
        imagename = eval("document.all." + imagename)
    }
    else if (document.getElementById) {
        imagename = document.getElementById(imagename)
    }
    if (!document.layers) {  
        if (imageswitch == 1) {
	imagename.src = <?php echo "\"/images/$set_theme/$ico_mini_larrow2\""; ?>;
        }
        else {
	imagename.src = <?php echo "\"/images/$set_theme/$ico_mini_larrow\""; ?>;
        }
    }
}

// }}}
// {{{ colorize()

function colorize (which, toggle, type) 
{
    if ((document.all) || (document.getElementById)) {
        if (toggle == 1) { 
            which.style.color = calendarColors['dateHoverColor'];
            which.style.backgroundColor = calendarColors['dateHoverBgColor'];
        }
        else {
            which.style.color = calendarColors[type + 'Color'];
            which.style.backgroundColor = calendarColors[type + 'BgColor'];
        }
    }
}

// }}}
// {{{ yearback()

function yearback(year) 
{ 
   year_t = year - 1;
   return year_t;
}

// }}}
// {{{ yearforward()

function yearforward(year) 
{
   year_t = year +1;
   return year_t;
}

// }}}
// {{{ previousmonth()

function previousmonth(month,year) 
{

    if (month > 0) {
        month_t = month-1;
        return month_t;
    }
    else {
        year_t = year -1;
        return "11&year=" + year_t;
    }  
}

// }}}
// {{{ nextmonth()

function nextmonth(month,year) 
{

    if (month < 11) {
        month_t = month + 1;
        return month_t;
    }
    else {
    year_t = year + 1;
    return "0&year=" + year_t;
    }
}

// }}}
// {{{ sendDate()

function sendDate(month, day, year) 
{
    // pad with blank zeros numbers under 10
    month = month < 10 ? '0' + month : month;
    day   = day   < 10 ? '0' + day   : day;
    selectedDate = parent.calendarFormat;
    selectedDate = selectedDate.replace(/m/, month);
    selectedDate = selectedDate.replace(/d/, day);
    selectedDate = selectedDate.replace(/y/, year);
    targetDateField.value = selectedDate;
    if(targetDateField.name == 'tf_date_begin')
    window.opener.changeDateEnd(targetDateField.form);
    parent.window.close();
    
}

// }}}

// grab the color settings from the parent, who has done validition on the variable
var calendarColors = parent.calendarColors;

// get the reference to the target element from the parent, who has done validation on variable
var targetDateField = parent.targetDateField;

var thisDay   = day;
var thisMonth = month;
var thisYear  = year;

var yeararray = new Array();
for (year_cnt = 1; year_cnt <= 21; year_cnt++) {
    yeararray[year_cnt] = thisYear - 11 + year_cnt;
}

printCalendar(month,year); 

//--></script>

