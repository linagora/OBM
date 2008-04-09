<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_js.inc                                                 //
//     - Desc : Group javascript functions File                              //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("obminclude/javascript/check_js.inc");


// to avoid a javascript error when ext_widget is not defined
if ($params["ext_widget"] != "") {
  $extra_js .= "

function fill_ext_form(int_form) {
   size = int_form.length;
   ext_field = window.opener.document.$params[ext_widget];
   for(i=0; i <size ; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
	 for(j=0; j< ext_size; j++) {
	   if('cb_g' + ext_field.options[j].value == int_form.elements[i].name) {
	     window.opener.document.$params[ext_widget].options[j].selected =true;
	   }
	 }
       }
     }
   }
}


function check_group_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_group\");
      return false;
    } else {
      window.opener.document.$params[ext_widget].value=valeur;
      window.opener.document.$params[ext_widget_text].value=text;
      if ('$params[restriction_calendar]' == 'calendar') {
        window.opener.location=('../calendar/calendar_index.php?date=$params[date]&group_id='+valeur+'&new_group=1');
      }
      window.close();
      return true;
    }
}
";
}

$extra_js .= "
function add_email_field(template) {

  var mailHome = $('groupMailHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'groupMailHome');
              show_hide_add_button();
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  mailHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').setProperty('name','tf_email[]').setProperty('type','text'));
  div.appendText(' @ ');
  div.adopt(template.clone());
  
}  

function show_hide_add_button() {
  var button = $('addMailButton'); 
  var parent = $('groupMailHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_group_alias] > 0) {
    if(childs.length >= $GLOBALS[c_max_group_alias] && button) {
      button.remove();
   } else if(childs.length < $GLOBALS[c_max_group_alias] && !button) {
      $('groupMailLabel').adopt((new Element('a').addEvent('click', function () {
                add_email_field(aliasSelectTemplate);
                show_hide_add_button();
              }).setProperty('id','addMailButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
    }
  }
}
";
if ($params["ext_element"] != "") {
  require_once("$obminclude/of/of_select.inc");
}

?>
