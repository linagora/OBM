<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : userpattern_js.inc                                           //
//     - Desc : User Pattern Index javascript functions File                 //
// 2010-01-21 Vincent ALQUIER                                                //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

$extra_js .= "

function add_email_field(template) {

  var mailHome = $('userMailHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'userMailHome');
              show_hide_add_button();
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  mailHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').setProperty('name','tf_email[]').setProperty('type','text'));
  div.appendText(' @ ');
  div.adopt(template.clone());
  
}

function show_hide_add_button() {
  var button = $('addMailButton'); 
  var parent = $('userMailHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_user_alias] > 0) {
    if(childs.length >= $GLOBALS[c_max_user_alias] && button) {
      button.dispose();
    } else if(childs.length < $GLOBALS[c_max_user_alias] && !button) {
      $('userMailLabel').adopt((new Element('a').addEvent('click', function () {
                add_email_field(aliasSelectTemplate);
                show_hide_add_button();
              }).setProperty('id','addMailButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
    }
  }
}

function switch_mail_mode() {
  if($('userMailActive').checked == true) {
    external.dispose();
    external.addClass('H');
    internal.removeClass('H');
    internal.injectInside($('userMail'));

  } else {
    //$('userMailHome').set('html','');
    //add_email_field(aliasSelectTemplate); 
    //show_hide_add_button();    
    internal.dispose();
    internal.addClass('H');
    external.removeClass('H');
    external.getElement('input').setProperty('disabled',false);
    external.injectInside($('userMail'));
  }
}

";

?>
