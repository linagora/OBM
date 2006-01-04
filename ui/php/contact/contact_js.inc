<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/category_js.inc");
require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");

$extra_js .= "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function check_contact(form) {
  // MANDATORY : Check that Contact name is not empty
  if (trim(form.tf_lname.value) == \"\") {
    alert (\"$l_fill_lastname\");
    return false;
  }

  // MANDATORY : Check the phone number
  lphone = form.tf_phone.value;

  if ((lphone != \"\") && (check_phone(lphone) == null)) {
    alert (\"$l_j_check_phone\");
    return false;
  }

  // Check the Mobile or personnal phone
  hphone = form.tf_hphone.value;
  if ((hphone != \"\") && (check_phone(hphone) == null)) {
    alert (\"$l_j_check_hphone\");
    return false;
  }
  mphone = form.tf_mphone.value;
  if ((mphone != \"\") && (check_phone(mphone) == null)) {
    alert (\"$l_j_check_mphone\");
    return false;
  }

  // Check the fax
  lfax = form.tf_fax.value;
  if ((lfax != \"\") && (check_fax(lfax) == null)) {
    alert (\"$l_j_check_fax\");
    return false;
  }

  // Check the email
  lemail = form.tf_email.value;
  if ((lemail != \"\") && (check_email(lemail) == null)) {
    alert (\"$l_j_check_email\");
    return false;
  }

  if (form.tf_date.value == \"\") {
    return true;
  } else if (check_date(form.tf_date, \"$l_j_check_date\") == false) {
    return false;
  } else
    return true;

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_kind_checkdel(sel)
{
  if (sel.selectedIndex >= 0) {
    return true;
  } else {
    alert(\"$l_j_kind_delete\");
    return false;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Check if the kind update is possible
///////////////////////////////////////////////////////////////////////////////
function check_kind_upd(form, sel_del)
{
  kind_pos = sel_del.selectedIndex;
  if ((kind_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_kind.value=sel_del.options[kind_pos].value;
    return true;
  } else {
    alert(\"$l_j_kind_update\");
    return false;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Check if the kind creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_kind_new(form)
{
   if ( (trim(form.tf_lang.value) == \"\")
        || (trim(form.tf_label.value) == \"\")
        || (trim(form.tf_header.value) == \"\") ) {
      alert(\"$l_j_kind_create\");
      return false;
   } else {
      return true;
   }
}

///////////////////////////////////////////////////////////////////////////////
// Fill the Kind's mini label select.
///////////////////////////////////////////////////////////////////////////////
var lang_array;

function fill_select_label(source) 
{
  destination = source.form.sel_kind_label;
  kind = source.form.sel_kind;
  destIndex = source.options[source.selectedIndex].value;
  for (i = destination.options.length-1; i >= 0; i--) {
    destination.options[i] = null; 
  }  
  for (i = kind.options.length-1; i >= 0; i--) {
    kind.options[i] = null; 
  }
  kind.options[0]=new Option('$l_undef');
  destination.options[0]=new Option('$l_undef');
  i=1;
  for (var destValue in lang_array[destIndex]) {
    destination.options[i]= new Option(destValue,destValue);
    i++;
  }
  destination.selectedIndex=0;
  kind.selectedIndex=0;
}


///////////////////////////////////////////////////////////////////////////////
// Fill the Kind's header select.
///////////////////////////////////////////////////////////////////////////////
function fill_select_header(source) 
{
  destination = source.form.sel_kind;
  lang = source.form.sel_kind_lang;
  destIndex = source.options[source.selectedIndex].value;
  for (i = destination.options.length-1; i >= 0; i--) {
    destination.options[i] = null; 
  }  
  lang.options[0]=new Option('$l_undef');
  destination.options[0]=new Option('$l_undef');
  i=1;
  for (var destValue in lang_array[lang.options[lang.selectedIndex].value][destIndex]) {
    destination.options[i]= new Option(destValue,lang_array[lang.options[lang.selectedIndex].value][destIndex][destValue]);
    i++;
  }
  destination.selectedIndex=0;
  
}

// Copy paste from mehdi's calendar.js

var calendarWindow = null;
var calendarColors = new Array();
calendarColors['bgColor'] = '#BDC5D0';
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
    var cal_height = 264;

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

if (isset($ext_target) && $ext_target!="") {
  $extra_js .= "

  function fill_ext_form(dir_path) {
    ext_field = window.opener.document.$ext_target;
    ext_field.value = dir_path;
    window.close();
  } 
  
";
}

$extra_js .= "
function copy_clip(text)
{

  if (window.clipboardData) 
  {

    window.clipboardData.setData(\"Text\", text);
  }
  else if (window.netscape) 
  { 
    try {
      netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
    } catch (err) {   
      alert('$l_j_copy_privilege_alert');;	   
      return false;
    }
    
    var clip = Components.classes['@mozilla.org/widget/clipboard;1'].createInstance(Components.interfaces.nsIClipboard);
    if (!clip) return;

    var trans = Components.classes['@mozilla.org/widget/transferable;1'].createInstance(Components.interfaces.nsITransferable);
    if (!trans) return;

    trans.addDataFlavor('text/unicode');

    var str = new Object();
    var len = new Object();

    var str = Components.classes[\"@mozilla.org/supports-string;1\"].createInstance(Components.interfaces.nsISupportsString);

    var copytext=text;

    str.data=copytext;

    trans.setTransferData(\"text/unicode\",str,copytext.length*2);

    var clipid=Components.interfaces.nsIClipboard;

    if (!clip) return false;

    clip.setData(trans,null,clipid.kGlobalClipboard);

  }
  return false;
}
";
</script>
