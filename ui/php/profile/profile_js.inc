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
// OBM - File : profile_js.inc                                               //
//     - Desc : Profile Index javascript functions File                      //
// 2008-09-11 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: organizationalchart_js.inc,v 1.35 2007/02/20 15:16:10 mehdi Exp $ //
///////////////////////////////////////////////////////////////////////////////

// Actions on form
$extra_js = '

function select_default (module_name) {
  $("cbx_" + module_name + "_right_read").checked = false;
  $("cbx_" + module_name + "_right_own").checked = false;
  $("cbx_" + module_name + "_right_write").checked = false;
  $("cbx_" + module_name + "_right_read_admin").checked = false;
  $("cbx_" + module_name + "_right_write_admin").checked = false;
}

function select_right (module_name) {
  $("cbx_" + module_name + "_default").checked = false;
}

';

$extra_js .= "
function setSectionState(state) {
    var elements = $$('.activeSection');
    for (var i=0; i<elements.length; i++) {
      elem =elements[i];
      elem.empty();
      if (state == 'active') {
        elem.innerHTML = '$l_section_disable';
      } else {
        elem.innerHTML = '$l_section_enable';
      }
    }
}
";

// Validate form
$extra_js .= "

function check_profile (form) {
	
	// Check profile name is not empty
	if (trim(form.ts_name.value) == \"\") {
		 alert(\"$l_profile_name_empty\"+\"test\");
		 return false;
	}
	
	// Check profile name is valid
	if (!trim(form.ts_name.value).match(new RegExp($js_regexp_profile_name))) {
		alert(\"$l_profile_name_error\");
		return false;
	}
	
	return true;
}

function addModule(storage, value) {
  var previous = storage.getLast();
  if($('module_' + value) || !modules.get(value)) {
    return false;
  }  
  if(previous) {
    klass = previous.get('class');
    if(klass == 'pair') {
      klass = '';
    } else {
      klass = 'pair';
    }
  } else {
    klass = 'impair';
  }
  var line = $('module_default').clone();
  line.set('id', 'module_'+value)
  line.set('class',klass);
  var label = line.getElement('td');
  var input = line.getElement('td').getElement('input');
  label.set('html','<a onclick=\"$(\'module_' + value + '\').dispose();return false\" href=\"\"><img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\"></a> ' + modules.get(value) + '<input type=\"hidden\" value=\"0\" name=\"cb_rights[' + value + '][]\"/>');
  line.getElements('td').each(function (element) {
    if(element.getElement('input')) {
      element.getElement('input').set('name','cb_rights[' + value +'][]');
    }
  })
  storage.adopt(line);
}

function addSection(storage, value) {
  var previous = storage.getLast();
  if($('section_' + value) || !sections.get(value)) {
    return false;
  }
  if(previous) {
    klass = previous.get('class');
    if(klass == 'pair') {
      klass = '';
    } else {
      klass = 'pair';
    }
  } else {
    klass = 'impair';
  }
  var line = $('section_default').clone();
  line.set('id', 'section_'+value)
  line.set('class',klass);
  var label = line.getElement('td');
  var input = line.getElement('td').getElement('input');
  label.set('html','<a onclick=\"$(\'section_' + value + '\').dispose();return false\" href=\"\"><img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\"></a> ' + sections.get(value));
  input.inject(label, 'bottom');
  line.getElements('td').each(function (element) {
    if(element.getElement('input')) {
      element.getElement('input').set('name','cb_enabled[' + value +']');
    }
  })
  storage.adopt(line);
}

function copyLine(field, storage) {
  copy = field.clone();
  copy.injectInside(storage);
}

";
?>
