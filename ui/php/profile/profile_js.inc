<?php
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

";
?>
