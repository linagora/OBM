<?
echo "
<script type=\"text/javascript\">
<!--
";
require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");
echo "
function check_calendar(form) {

  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }
  else if (check_date(form.tf_date_begin, \"$l_err_datebegin\") == false) {
    return false;
  }
  else if (check_date(form.tf_date_end, \"$l_err_dateend\") == false) {
    return false;
  }
  else if ((trim(form.tf_repeat_end.value) != \"\") && (check_date(form.tf_repeat_end, \"$l_err_repeat\") == false)) {
    return false;
  }

    return true;
  
} 
function check_all_force(form) {
  var i='';
  for(i=0;i<form.length;i++) {
    if(form.elements[i].value.toString() == 'force') {
      form.elements[i].checked = 1;

    }
  }
} 
function check_all_replace(form) {
  var i='';
  for(i=0;i<form.length;i++) {
    if(form.elements[i].value.toString() == 'replace') {
      form.elements[i].checked = 1;

    }
  }
} 
function check_all_cancel(form) {
  var i='';
  for(i=0;i<form.length;i++) {
    if(form.elements[i].value == 'cancel') {
      form.elements[i].checked = 1;
    }
  }
} 


function show ( evt, name ) {

  var currentX,		//mouse position on X axis
      currentY,		//mouse position on X axis
      x,		//layer target position on X axis
      y,		//layer target position on Y axis
      docWidth,		//width of current frame
      docHeight,	//height of current frame
      layerWidth,	//width of popup layer
      layerHeight,	//height of popup layer
      ele;		//points to the popup element

  // First let's initialize our variables
  ele = document.getElementById(name);
  currentX = evt.clientX,
  currentY = evt.clientY;
  docWidth = document.width;
  docHeight = document.height;
  layerWidth = document.getElementById(name).style.width;
  layerHeight = document.getElementById(name).style.height;


  // Then we calculate the popup element's new position
  if ( ( currentX + layerWidth ) > docWidth ) {
    x = ( currentX - layerWidth );
  }
  else {
    x = currentX;
  }
  if ( ( currentY + layerHeight ) >= docHeight ) {
     y = ( currentY - layerHeight - 10 );
  }
  else {
    y = currentY + 10;
  }

  x += window.pageXOffset;
  y += window.pageYOffset;

// alert(\"docWidth \" + docWidth + \", docHeight \" + docHeight + \"layerWidth \" + layerWidth + \", layerHeight \" + layerHeight + \"currentX \" + currentX + \", currentY \" + currentY + \"x \" + x + \", y \" + y);
    ele.style.left = x+'px';
    ele.style.top = y+'px';
    ele.style.visibility = \"visible\";
}

function hide ( name ) {
  document.getElementById(name).style.visibility = \"hidden\";
}
</script>

";
?>
