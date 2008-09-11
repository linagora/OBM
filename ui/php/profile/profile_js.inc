<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_js.inc                                               //
//     - Desc : Profile Index javascript functions File                      //
// 2008-09-11 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: organizationalchart_js.inc,v 1.35 2007/02/20 15:16:10 mehdi Exp $ //
///////////////////////////////////////////////////////////////////////////////

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

?>