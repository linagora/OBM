<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : agenda_js.inc                                               //
//     - Desc  : Agenda javascript functions File                            //
// 2005-01-13 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");
require("$obminclude/javascript/category_js.inc");

$extra_js .= "

function check_calendar(form) {

  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }
  else if (check_date(form.tf_date_begin, \"$l_datebegin\") == false) {
    return false;
  }
  else if (check_date(form.tf_date_end, \"$l_dateend\") == false) {
    return false;
  }
  else if ((trim(form.tf_repeat_end.value) != \"\") && (trim(form.sel_repeat_kind.value) != \"none\") && (check_date(form.tf_repeat_end, \"$l_date_repeatend\") == false) ) {
    return false;
  }

  return true;
}


function show_hide_dates(field) {
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


NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
W3C = (document.getElementById) ? 1 : 0;	
function show ( evt, name ) {
  if (IE4) {
    evt = window.event;  
  }

  var currentX,		
      currentY,	
      x,
      y,
      docWidth,
      docHeight,
      layerWidth,
      layerHeight,
      ele;	

  if ( W3C ) {
    ele = document.getElementById(name);
    currentX = evt.clientX,
    currentY = evt.clientY;
    docWidth = document.width;
    docHeight = document.height;
    layerWidth = ele.style.width;
    layerHeight = ele.style.height;
  } else if ( NS4 ) {
    ele = document.layers[name];
    currentX = evt.pageX,
    currentY = evt.pageY;
    docWidth = document.width;
    docHeight = document.height;
    layerWidth = ele.clip.width;
    layerHeight = ele.clip.height;

  } else {
    ele = document.all[name];
    currentX = evt.clientX,
    currentY = evt.clientY;
    docHeight = document.body.offsetHeight;
    docWidth = document.body.offsetWidth;
    layerWidth = 200;
    layerHeight = ele.offsetHeight;
  }

  if ( ( currentX) > docWidth ) {
    x = ( currentX - layerWidth );
  }
  else {
    x = currentX;
  }
   x = x - 196;
  y = currentY + 20;
  y = y - 150;
  if ( IE4 ) {
    x += document.body.scrollLeft;
    y += document.body.scrollTop;
  } else {
    x += window.pageXOffset;
    y += window.pageYOffset;
  }

  if ( NS4 ) {
    ele.right = parseInt ( x );
    ele.top = parseInt ( y );
    ele.display = \"block\";
  } else {  // IE4 & W3C
    ele.style.position = 'absolute'
    ele.style.left = x+'px';
    ele.style.top = y+'px';
    ele.style.display = \"block\";
  }
}

function hide ( name ) {
  if (W3C) {
    document.getElementById(name).style.display = \"none\";
  } else if (NS4) {
    document.layers[name].display = \"none\";
  } else {
    document.all[name].style.display = \"none\";
  }
}

function changeDateEnd ( form ) {
  form.tf_date_end.value = form.tf_date_begin.value; 

}

///////////////////////////////////////////////////////////////////////////////
// Check if the category check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_category_checkdel(form)
{
   if (form.sel_category.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_category_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the category update is possible
///////////////////////////////////////////////////////////////////////////////
function check_category_upd(form,formd)
{
  category_pos = formd.sel_category.selectedIndex;
  if ((category_pos >= 0) && (form.tf_category_upd.value != \"\")) {
    form.sel_category.value=formd.sel_category.options[category_pos].value;
    return true;
  } else {
    alert(\"$l_j_category_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the category creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_category_new(form)
{
   if (form.tf_category_new.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_category_create\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the meeting perform is possible
///////////////////////////////////////////////////////////////////////////////
function check_meeting(form) {
  
  if (trim(form.sel_min_duration.options[form.sel_min_duration.selectedIndex].value) == \"00\" 
       && trim(form.sel_time_duration.options[form.sel_time_duration.selectedIndex].value) == \"00\") {
    alert (\"$l_interval_null\");
    return false;
  }
  else if (check_date(form.tf_date_begin, \"$l_datebegin\") == false) {
    return false;
  }

  return true;
} 

";
