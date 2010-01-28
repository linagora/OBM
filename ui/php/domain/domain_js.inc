<?php
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
