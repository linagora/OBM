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
  else if ((trim(form.tf_repeat_end.value) != \"\") && (trim(form.sel_repeat_kind.value) != \"none\") && (check_date(form.tf_repeat_end, \"$l_err_repeat\") == false) ) {
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



function check_count_user(formulaire) {
  var selectObject = formulaire.elements[0];
  var selectCount  = selectObject.length;
  var nb_select = 0;
  for (var i = 0; i < selectCount; i++)
  {
    if( selectObject.options[i].selected == true)
      nb_select ++;
  }
  if( nb_select > 6) {
    alert('$l_too_much_user');
    return false;
  }
  return true;
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

  if ( ( currentX + layerWidth ) > docWidth ) {
    x = ( currentX - layerWidth );
  }
  else {
    x = currentX;
  }
  if ( ( currentY + layerHeight ) >= docHeight ) {
     y = ( currentY - layerHeight - 20 );
  }
  else {
    y = currentY + 20;
  }
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
    ele.visibility = \"show\";
  } else {  // IE4 & W3C
    ele.style.left = x+'px';
    ele.style.top = y+'px';
    ele.style.visibility = \"visible\";
  }

}

function hide ( name ) {
  if (W3C) {
    document.getElementById(name).style.visibility = \"hidden\";
  } else if (NS4) {
    document.layers[name].visibility = \"hide\";
  } else {
    document.all[name].style.visibility = \"hidden\";
  }
}
-->
</script>

";
?>
