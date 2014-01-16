<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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

function add_nomade_email_field() {
  var mailHome = $('nomadeMailHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'nomadeMailHome');
              show_hide_nomade_add_button();
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  mailHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').addClass('emailNomade').setProperty('name','attributes[email_nomade][]').setProperty('type','text'));
  
}

function show_hide_nomade_add_button() {
  var button = $('addMailNomadeButton'); 
  var parent = $('nomadeMailHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_email_nomade] > 0) {
    if(childs.length >= $GLOBALS[c_max_email_nomade] && button) {
      button.dispose();
   } else if(childs.length < $GLOBALS[c_max_email_nomade] && !button) {
      $('nomadeMailLabel').adopt((new Element('a').addEvent('click', function () {
                add_nomade_email_field();
                show_hide_nomade_add_button();
              }).setProperty('id','addMailNomadeButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
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
