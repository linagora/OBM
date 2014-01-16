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
// OBM - File : domain_js.inc                                                //
//     - Desc : Domain javascript functions File                             //
// 2003-08-22 Aliacom - Phan David                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function check_domain(form) {
  // MANDATORY : Check that the Domain title is not empty
  if (trim(form.tf_label.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }

  return true;
}

function add_alias_field() {

  var aliasesHome = $('domainDomainHome');
  // Create the div
  var div = new Element('div').addClass('multiple');
  div.adopt(new Element('a').addEvent('click', function () {
              remove_element(this.parentNode,'domainDomainHome');
              show_hide_add_button();
            }).adopt(new Element('img').setProperty('src','$ico_delete')));
  aliasesHome.adopt(div);
  div.appendText(' ').adopt(new Element('input').setProperty('name','tf_alias[]').setProperty('type','text'));
  
}

function show_hide_add_button() {
  var button = $('addDomainButton'); 
  var parent = $('domainDomainHome');
  var childs = parent.getChildren();
  if($GLOBALS[c_max_domain_alias] > 0) {
    if(childs.length >= $GLOBALS[c_max_domain_alias] && button) {
      button.dispose();
    } else if(childs.length < $GLOBALS[c_max_domain_alias] && !button) {
      $('domainDomainLabel').adopt((new Element('a').addEvent('click', function () {
                add_alias_field();
                show_hide_add_button();
              }).setProperty('id','addDomainButton').adopt(new Element('img').setProperty('src','$ico_add'))));     
    }
  }
}

function show_domain_spinner() {
  $('buttons').hide();
  $('domain_spinner').show();
}

function confirm_del(form) {
  var rep = confirm('$l_domain_confirm_delete');
  if(rep){
    show_domain_spinner();
  }
  return true;
}


function copyField(field, storage) {
  copy = field.clone();
  copy.injectInside(storage);
}

function autoGenSid() {
// the max SID is in hexa FF FF FF FF
// in dec 4294967295 = 2^32 - 1
  var sid = 'S-1-5-21';
  for(var i=0; i<3; i++) {
    sid = sid + '-'
    sid = sid + Math.floor(Math.random() * (4294967295 - 0 + 1));
  }
  $('sambaSID').value = sid;
}
";
  
?>
