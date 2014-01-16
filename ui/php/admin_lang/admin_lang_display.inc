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
// OBM - File : admin_lang_display.inc                                       //
//     - Desc : lang admin display File                                      //
// 2001-12-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the help screen
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_admin_lang_help($mode) {

  if ($mode == "html")
    echo "<table>
      <tr>
        <td><pre>";

  include ("admin_lang_help.inc");

  if ($mode == "html")
    echo "</pre>
        </td>
      </tr>
      </table>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for lang module
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_admin_lang_index($mode, $actions, $modules, $langs, $themes) {

  switch ($mode) {
  case "txt":
    //    txt_lang_index($actions, $modules, $langs, $themes);
    break;
  case "html":
    html_admin_lang_index($actions, $modules, $langs, $themes);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML search form for lang module
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_admin_lang_index($actions, $modules, $langs, $themes) {
  global $l_execute, $l_help;

  // modules SELECT
  $sel_module = "<select name=\"target_module\">";
  while (list ($key, $val) = each ($modules)) {
    $sel_module .= "<option value=\"$key\">$val[Name]</option>";
  }
  $sel_module .= "</select>";

  // lang1 SELECT
  $sel_lang1 = "<select name=\"lang\">";
  while (list ($key, $val) = each ($langs)) {
    $sel_lang1 .= "<option value=\"$val\">$val</option>";
  }
  $sel_lang1 .= "</select>";

  // lang2 SELECT
  reset ($langs);
  $sel_lang2 = "<select name=\"lang2\">";
  while (list ($key, $val) = each ($langs)) {
    $sel_lang2 .= "<option value=\"$val\">$val</option>";
  }
  $sel_lang2 .= "</select>";

  // theme SELECT
  reset ($themes);
  $sel_theme = "<select name=\"theme\">";
  while (list ($key, $val) = each ($themes)) {
    $sel_theme .= "<option value=\"$val\">$val</option>";
  }
  $sel_theme .= "</select>";

  echo "
    
    <form method=\"get\" action=\"" . url_prepare("admin_lang_index.php")."\">
    <div class=\"detail infos\">
    <h1>Show vars</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Action : show_src</th>
      <th>Module</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>Show vars from source</td>
      <td>$sel_module</td>
    </tr>
    </tbody>
    </table>
     <div class=\"buttons\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"show_src\" />
        <input type=\"submit\" value=\"$l_execute\" />
    </div>
    </div>
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_lang_index.php"). "\">
    <div class=\"detail infos\">
    <h1>Lang Definition</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Action : show_lang</th>
      <th>Module</th>
      <th>Lang 1</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>Show Lang definition</td>
      <td>$sel_module</td>
      <td>$sel_lang1</td>
    </tr>
    </tbody>
    </table>
    <div class=\"buttons\">    
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"show_lang\" />
        <input type=\"submit\" value=\"$l_execute\" />
    </div>
    </div>    
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_lang_index.php"). "\">
    <div class=\"detail infos\">
    <h1>Compare Lang</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Action : Comp_lang</th>
      <th>Module</th>
         </tr>
    </thead>
    <tbody>
    <tr>
      <td>Compare Lang Definition</td>
      <td>$sel_module</td>
    </tr>
    </tbody>
    <thead>
    <tr>
     <th>Lang 1</th>
     <th>Lang 2</th>
    </tr>
    </thead>
    <tbody>
    <tr>
    <td>$sel_lang1</td>
    <td>$sel_lang2</td>
    </tr>
    </tbody>
    </table>
    <div class=\"buttons\">
      <input type=\"hidden\" name=\"mode\" value=\"html\" />
      <input type=\"hidden\" name=\"action\" value=\"comp_lang\" />
      <input type=\"submit\" value=\"$l_execute\" />
    </div>
    </div>
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_lang_index.php"). "\">
    <div class=\"detail infos\">
    <h1>Compare Lang</h1>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Action : comp_global_lang</th>
      <th>Lang 1</th>
      <th>Lang 2</th>
    </tr>
    </thead>
    <tbody>
    <tr>
    <td>Compare Global Lang definition</td>
    <td>$sel_lang1</td>
    <td>$sel_lang2</td>
        </tr>
    </tbody>
    </table>
     <div class=\"buttons\">
     <input type=\"hidden\" name=\"mode\" value=\"html\" />
     <input type=\"hidden\" name=\"action\" value=\"comp_global_lang\" />
     <input type=\"submit\" value=\"$l_execute\" />
     </div>
     </div>
    </form>";
}


///////////////////////////////////////////////////////////////////////////////
// Display variables referenced in a module source code with indication about//
// real use. (A variable referenced only in a global statement is not used)  //
// Parameters:
//   - $mode   : "txt" or "html"
//   - $module : module name
///////////////////////////////////////////////////////////////////////////////
function dis_admin_lang_src_vars($mode, $module) {

  $ref_vars = get_admin_lang_src_vars($module);
  $used_vars = get_admin_lang_src_used_vars($module);
  $unused_vars = array_diff ($ref_vars, $used_vars);

  switch ($mode) {
  case "txt":
    txt_admin_lang_src_vars($module, $ref_vars, $used_vars, $unused_vars);
    break;
  case "html":
    html_admin_lang_src_vars($module, $ref_vars, $used_vars, $unused_vars);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars referenced from a module source files
// Parametres:
//   - $module      : module name
//   - $ref_vars    : existing vars in source code
//   - $used_vars   : used vars
//   - $unused_vars : unused vars (defined in global but not used)
///////////////////////////////////////////////////////////////////////////////
function txt_admin_lang_src_vars($module, $ref_vars, $used_vars, $unused_vars) {

  $nb_used_vars = count($used_vars);
  $nb_unused_vars = count($unused_vars);

  echo "
------------------------------------------------------------------------------
*** $module : Lang vars used : $nb_used_vars vars
------------------------------------------------------------------------------
";
  while (list ($nb, $myvar) = each ($used_vars)) {
    echo "$myvar\n";
  }
  echo "
------------------------------------------------------------------------------
*** $module : Lang vars referenced but not used : $nb_unused_vars vars
------------------------------------------------------------------------------
";
  while (list ($nb, $myvar) = each ($unused_vars)) {
    echo "$myvar\n";
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars referenced from a module source files
// Parameters:
//   - $module      : module name
//   - $ref_vars    : existing vars in source code
//   - $used_vars   : used vars
//   - $unused_vars : unused vars (defined in global but not used)
///////////////////////////////////////////////////////////////////////////////
function html_admin_lang_src_vars($module, $ref_vars, $used_vars, $unused_vars) {

  $nb_used_vars = count($used_vars);
  $nb_unused_vars = count($unused_vars);

  while (list ($nb, $myvar) = each ($used_vars)) {
    $dis_var .= "$myvar<br />\n";
  }

  while (list ($nb, $myvar) = each ($unused_vars)) {
    $dis_uvar .= "$myvar<br />\n";
  }

  echo "
   <div class=\"detail extra\">
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th colspan=\"2\">
      $module : Variables referenced from module source
      </th> 
    </tr>
    <tr>
      <td>used vars : $nb_used_vars</td>
      <td>UNused vars : $nb_unused_vars</td>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>
        $dis_var
      </td>
      <td>
        $dis_uvar
      </td>
    </tr>
    </tbody>
    </table>
    </div>";
}


///////////////////////////////////////////////////////////////////////////////
// Display vars from the lang and module given with information about use
// Parametres:
//   - $mode   : "txt" ou "html"
//   - $module : module name
//   - $lang   : language to display
///////////////////////////////////////////////////////////////////////////////
function dis_admin_lang_vars($mode, $module, $lang) {

  $lang_v = get_admin_lang_vars($module, $lang);
  if (! $lang_v) {
    echo "File $lang/$module.inc is missing ! Create it";
    return false;
  }

  $glang_v = get_admin_lang_global_lang_vars($lang);
  if (! $glang_v) {
    echo "File $lang/global.inc is missing ! Create it";
    return false;
  }

  $ref_v = get_admin_lang_src_vars($module);
  $used_v = get_admin_lang_src_used_vars($module);
  $unused_v = array_diff ($ref_v, $used_v);

  switch ($mode) {
  case "txt":
    txt_admin_lang_vars($module, $lang, $lang_v, $glang_v, $ref_v, $used_v, $unused_v);
    break;
  case "html":
    html_admin_lang_vars($module, $lang, $lang_v, $glang_v, $ref_v, $used_v, $unused_v);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars from the lang and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language to display
//   - $lang_v   : lang vars defined
//   - $glang_v  : lang vars defined in the lang global.inc
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function txt_admin_lang_vars($module, $lang, $lang_v, $glang_v, $ref_v, $used_v, $unused_v) {

  $text = "";
  $nb_ref = count($ref_v);
  $nb_used = count($used_v);
  $nb_unused = count($unused_v);
  $nb_lang = count($lang_v);
  $nb_glang = count($glang_v);
  $nb_used_def = 0;
  $nb_used_def_glo = 0;
  $nb_used_def_twi = 0;
  $nb_used_undef = 0;
  $nb_unused_def = 0;
  $nb_unused_undef = 0;
  $nb_def_unref = 0;

  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text .= "$lval = $value\n";
      $nb_used_def++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang
------------------------------------------------------------------------------
From Source    : $nb_ref var referenced : $nb_used used - $nb_unused unused 
From lang file : $nb_lang ($module.inc), $nb_used_def_glo ($lang/global.inc) vars defined
------------------------------------------------------------------------------
*** $module / $lang : vars used and defined : $nb_used_def vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $glang_v[$lval];
    if ($value) {
      $text .= "$lval = $value\n";
      $nb_used_def_glo++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang : vars used and defined in global : $nb_used_def_glo vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $gvalue = $glang_v[$lval];
    if (($value) && ($gvalue)) {
      $text .= "$lval = $value - (global : $gvalue)\n";
      $nb_used_def_twi++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang : vars used and defined twice ($module + global) : $nb_used_def_twi vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $value2 = $glang_v[$lval];
    if ((! $value) && (! $value2)) {
      $text .= "$lval = $value\n";
      $nb_used_undef++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang : vars used but not defined : $nb_used_undef vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text .= "$lval = $value\n";
      $nb_unused_def++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang : vars Unused but defined : $nb_unused_def vars
------------------------------------------------------------------------------
$text
";

  $text = "";
  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if (! $value) {
      $text .= "$lval = $value\n";
      $nb_unused_undef++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang : vars Unused and not defined : $nb_unused_undef vars
------------------------------------------------------------------------------
$text
";

  // Vars defined in Lang file but not referenced in source
  $text = "";
  reset($lang_v);
  while (list ($lvar, $lval) = each ($lang_v)) {
    if ((! in_array($lvar, $used_v)) && (! in_array($lvar, $unused_v))) {
      $text .= "$lvar = $lval\n";
      $nb_def_unref++;
    }
  }
  echo "
------------------------------------------------------------------------------
*** $module / $lang : vars defined in lang but not referenced : $nb_def_unref vars
------------------------------------------------------------------------------
$text
";

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars from the lang and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language to display
//   - $lang_v   : lang vars defined
//   - $glang_v  : lang vars defined in the lang global.inc
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function html_admin_lang_vars($module, $lang, $lang_v, $glang_v, $ref_v, $used_v, $unused_v) {

  $nb_ref = count($ref_v);
  $nb_used = count($used_v);
  $nb_unused = count($unused_v);
  $nb_lang = count($lang_v);
  $nb_glang = count($glang_v);
  $nb_used_def = 0;
  $nb_used_def_glo = 0;
  $nb_used_def_twi = 0;
  $nb_used_undef = 0;
  $nb_unused_def = 0;
  $nb_unused_undef = 0;
  $nb_def_unref = 0;

  // Used and defined
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text_used_def .= "
    <tr>
      <td>$lval</td>
      <td>$value</td>
    </tr>";
      $nb_used_def++;
    }
  }

  // Used and defined in global
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $glang_v[$lval];
    if ($value) {
      $text_used_def_glo .= "
    <tr>
      <td>$lval</td>
      <td>$value</td>
    </tr>";
      $nb_used_def_glo++;
    }
  }

  // Used and Defined twic ($module.inc and $lang/global.inc)
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $gvalue = $glang_v[$lval];
    if (($value)  && ($gvalue)) {
      $text_used_def_twi .= "
    <tr>
      <td>$lval</td>
      <td>$value</td>
    </tr>";
      $nb_used_def_twi++;
    }
  }

  // Used but undefined
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $value2 = $glang_v[$lval];
    if ((! $value) && (! $value2)) {
      $text_used_undef .= "
    <tr>
      <td>$lval</td>
      <td>$value</td>
    </tr>";
      $nb_used_undef++;
    }
  }

  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if ($value) {
      $text_unused_def .= "
    <tr>
      <td>$lval</td>
      <td>$value</td>
    </tr>";
      $nb_unused_def++;
    }
  }

  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    if (! $value) {
      $text_unused_undef .= "
    <tr>
      <td>$lval</td>
      <td>$value</td>
    </tr>";
      $nb_unused_undef++;
    }
  }

  reset($lang_v);
  while (list ($lvar, $lval) = each ($lang_v)) {
    if ((! in_array($lvar, $used_v)) && (! in_array($lvar, $unused_v))) {
      $text_def_unref .= "
    <tr>
      <td>$lvar</td>
      <td>$lval</td>
    </tr>";
      $nb_def_unref++;
    }
  }

  echo "
    <div class=\"detail extra\">
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th colspan=\"2\">$module / $lang<br />
      From Source : <b>$nb_ref</b> var referenced :
        <b>$nb_used</b> used - <b>$nb_unused</b> unused 
      <br />From lang file : <b>$nb_lang</b> ($module.inc),
          <b>$nb_used_def_glo</b> ($lang/global.inc) vars defined
      </th>
    </tr><tr>
      <th colspan=\"2\">
        Vars Used and defined : <b>$nb_used_def</b>
      </th>
    </tr>
    </thead>
    <tbody>
      $text_used_def
    </tbody>
    <thead>
    <tr>
      <th colspan=\"2\">
        Vars Used and defined in lang global : <b>$nb_used_def_glo</b>
      </th>
    </tr>
    </thead>
    <tbody>
      $text_used_def_glo
    </tbody>
    <thead>
    <tr>
      <th colspan=\"2\">
        Vars Used and defined twice ($module.inc + $lang/global.inc) : <b>$nb_used_def_twi</b>
      </th>
    </tr>
    </thead>
    <tbody>
      $text_used_def_twi
    </tbody>  
    <thead>
    <tr>
      <th colspan=\"2\">
        Vars used but not defined : <b>$nb_used_undef</b>
      </th>
    </tr>
    </thead>
    <tbody>
      $text_used_undef
    </tbody>
    <thead>
    <tr>
      <th colspan=\"2\">
        Vars UNused but defined : <b>$nb_unused_def</b>
      </th>
    </tr>
    </thead>
    <tbody>
      $text_unused_def
    </tbody>
    <thead>
    <tr>
      <th colspan=\"2\">
        Vars UNused and not defined : <b>$nb_unused_undef</b>
      </th>
    </tr>
    </thead>
    <tbody>
      $text_unused_undef
    </tbody>
    <thead>
    <tr>
      <th colspan=\"2\">
        Vars defined but not referenced : <b>$nb_def_unref</b>
      </th>
    </tr>
    </thead>
    <tbody>
      $text_def_unref
    </tbody>
    </table>
    </div>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the comparison of lang vars for the langs and the module given    //
// Give information about the usage of vars from both langs
// Parametres:
//   - $mode   : "txt" ou "html"
//   - $module : module name
//   - $lang   : language src to display
//   - $lang2  : language dest to compare
///////////////////////////////////////////////////////////////////////////////
function dis_admin_lang_comp_lang_vars($mode, $module, $lang, $lang2) {

  $lang_v = get_admin_lang_vars($module, $lang);
  if (! $lang_v) {
    echo "File $lang/$module.inc is missing ! Create it";
    return false;
  }
  $lang2_v = get_admin_lang_vars($module, $lang2);
  if (! $lang2_v) {
    echo "File $lang2/$module.inc is missing ! Create it";
    return false;
  }

  $glang_v = get_admin_lang_global_lang_vars($lang);
  if (! $glang_v) {
    echo "File $lang/global.inc is missing ! Create it";
    return false;
  }
  $glang2_v = get_admin_lang_global_lang_vars($lang2);
  if (! $glang2_v) {
    echo "File $lang2/global.inc is missing ! Create it";
    return false;
  }

  $ref_v = get_admin_lang_src_vars($module);
  $used_v = get_admin_lang_src_used_vars($module);
  $unused_v = array_diff ($ref_v, $used_v);

  switch ($mode) {
  case "txt":
    txt_admin_lang_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $glang_v, $glang2_v, $ref_v, $used_v, $unused_v);
    break;
  case "html":
    html_admin_lang_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $glang_v, $glang2_v, $ref_v, $used_v, $unused_v);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars comparison from 2 langs and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language src to display
//   - $lang2    : language dest to display
//   - $lang_v   : lang vars defined
//   - $lang2_v  : lang vars defined
//   - $glang_v  : lang vars defined in the lang global.inc
//   - $glang2_v  : lang vars defined in the lang global.inc
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function txt_admin_lang_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $glang_v, $glang2_v, $ref_v, $used_v, $unused_v) {

  echo "txt mode Not implemented: Run this action in HTML mode.\n";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars comparison from 2 langs and module given
// Parameters:
//   - $module   : module name
//   - $lang     : language src to display
//   - $lang2    : language dest to display
//   - $lang_v   : lang vars defined
//   - $lang2_v  : lang vars defined
//   - $glang_v  : lang vars defined in the lang global.inc
//   - $glang2_v : lang vars defined in the lang global.inc
//   - $ref_v    : lang vars referenced in source files
//   - $used_v   : lang vars used in source files
//   - $unused_v : lang vars referenced in source but not used
///////////////////////////////////////////////////////////////////////////////
function html_admin_lang_comp_lang_vars($module, $lang, $lang2, $lang_v, $lang2_v, $glang_v, $glang2_v, $ref_v, $used_v, $unused_v) {

  $nb_ref = count($ref_v);
  $nb_used = count($used_v);
  $nb_unused = count($unused_v);
  $nb_lang = count($lang_v);
  $nb_lang2 = count($lang2_v);
  $nb_glang = count($glang_v);
  $nb_glang2 = count($glang2_v);
  $nb_used_def = $nb_used_def_2 = 0;
  $nb_used_undef = $nb_used_undef_2 = 0;
  $nb_unused_def = $nb_unused_def_2 = 0;
  $nb_unused_undef = $nb_unused_undef_2 = 0;
  $nb_def_unref = $nb_def_unref_2 = 0;

  // Used and defined in module.inc
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ($value || $value2) {
      $text_used_def .= "<tr>
        <td>$lval</td>
        <td>$value</td>
        <td>$value2</td>
        </tr>";
      if ($value) $nb_used_def++;
      if ($value2) $nb_used_def_2++;
    }
  }

  // Used and defined in global
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $glang_v[$lval];
    $value2 = $glang2_v[$lval];
    if ($value || $value2) {
      $text_used_def_glo .= "<tr>
        <td>$lval</td>
        <td>$value</td>
        <td>$value2</td>
        </tr>";
      if ($value) $nb_used_def_glo++;
      if ($value2) $nb_used_def_glo_2++;
    }
  }

  // Used but not defined
  reset($used_v);
  while (list ($nb, $lval) = each ($used_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    $gvalue = $glang_v[$lval];
    $gvalue2 = $glang2_v[$lval];
    if ( ((! $value) && (! $gvalue)) || ((! $value2) && (! $gvalue2)) ) {
      $text_used_undef .= "<tr>
        <td>$lval</td>
        <td>$value $gvalue</td>
        <td>$value2 $gvalue2</td>
        </tr>";
      if (! $value) $nb_used_undef++;
      if (! $value2) $nb_used_undef_2++;
    }
  }

  // Unused (only in global) but defined
  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    $gvalue = $glang_v[$lval];
    $gvalue2 = $glang2_v[$lval];
    if ($value || $value2 || $gvalue || $gvalue2) {
      $text_unused_def .= "<tr>
        <td>$lval</td>
        <td>$value $gvalue</td>
        <td>$value2 $gvalue2</td>
        </tr>";
      if ($value) $nb_unused_def++;
      if ($value2) $nb_unused_def_2++;
    }
  }

  reset($unused_v);
  while (list ($nb, $lval) = each ($unused_v)) {
    $value = $lang_v[$lval];
    $value2 = $lang2_v[$lval];
    if ((! $value) || (! $value2)) {
      $text_unused_undef .= "<tr>
        <td>$lval</td>
        <td>$value</td>
        <td>$value2</td>
        </tr>";
      if (! $value) $nb_unused_undef++;
      if (! $value2) $nb_unused_undef_2++;
    }
  }

  reset($lang_v);
  while (list ($lvar, $lval) = each ($lang_v)) {
    if ((! in_array($lvar, $used_v)) && (! in_array($lvar, $unused_v))) {
      $lval2 = $lang2_v[$lvar];
      $text_def_unref .= "<tr>
        <td>$lvar</td>
        <td>$lval</td>
        <td>$lval2</td>
        </tr>";
      $nb_def_unref++;
      if (array_key_exists($lvar, $lang2_v)) $nb_def_unref_2++;
    }
  }
  reset($lang2_v);
  while (list ($lvar2, $lval2) = each ($lang2_v)) {
    // If not already processed by precedent step (lang_v)
    if ( (! array_key_exists($lvar2, $lang_v)) &&
         (! in_array($lvar2, $used_v)) && (! in_array($lvar2, $unused_v))) {
      $lval = $lang_v[$lvar2];
      $text_def_unref .= "<tr>
        <td>$lvar2</td>
        <td>$lval</td>
        <td>$lval2</td>
        </tr>";
      $nb_def_unref_2++;
      if (array_key_exists($lvar2, $lang_v)) $nb_def_unref++;
    }
  }

  echo "
    <div class=\"detail extra\">
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th colspan=\"3\">$module : From Source :
        <b>$nb_ref</b> var referenced :
        <b>$nb_used</b> used - <b>$nb_unused</b> unused
      </th>
    </tr>
    <tr>
      <th>&nbsp;</th>
      <th>$module / $lang</th>
      <th>$module / $lang2</th>
    </tr><tr>
      <th>From lang file :</th>
      <th>(<b>$nb_lang + $nb_used_def_glo</b>) vars defined</th>
      <th>(<b>$nb_lang2 + $nb_used_def_glo_2</b>) vars defined</th>
    </tr><tr>
      <th>Vars Used and defined :</th>
      <th><b>$nb_used_def</b></th>
      <th><b>$nb_used_def_2</b></th>
    </tr>
    </thead>
    <tbody>
      $text_used_def
    </tbody>
    <thead>
    <tr>
      <th>Vars Used and defined in global</th>
      <th><b>$nb_used_def_glo</b></th>
      <th><b>$nb_used_def_glo_2</b></th>
    </tr>
    </thead>
    <tbody>
      $text_used_def_glo
    </tbody>
    <thead>
    <tr>
      <th>Vars used but not defined</th>
      <th><b>$nb_used_undef</b></th>
      <th><b>$nb_used_undef_2</b></th>
    </tr>
    </thead>
    <tbody>
      $text_used_undef
    </tbody>
    <thead>
    <tr>
      <th>Vars UNused but defined</th>
      <th><b>$nb_unused_def</b></th>
      <th><b>$nb_unused_def_2</b></th>
    </tr>
    </thead>
    <tbody>
      $text_unused_def
    </tbody>
    <thead>
    <tr>
      <th>Vars UNused and not defined</th>
      <th><b>$nb_unused_undef</b></th>
      <th><b>$nb_unused_undef_2</b></th>
    </tr>
    </thead>
    <tbody>
    $text_unused_undef
    </tbody>
    <thead>
    <tr>
      <th>Vars defined but not referenced</th>
      <th><b>$nb_def_unref</b></th>
      <th><b>$nb_def_unref_2</b></th>
    </tr>
    </thead>
    <tbody>
      $text_def_unref
    </tbody>
    </table>
    </div>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the comparison of lang vars in the global.inc file                //
// Parametres:
//   - $mode  : "txt" ou "html"
//   - $lang  : language src to display
//   - $lang2 : language dest to compare
///////////////////////////////////////////////////////////////////////////////
function dis_admin_lang_comp_global_lang_vars($mode, $lang, $lang2) {

  $lang_v = get_admin_lang_global_lang_vars($lang);
  if (! $lang_v) {
    echo "File $lang/global.inc is missing ! Create it";
    return false;
  }
  $lang2_v = get_admin_lang_global_lang_vars($lang2);
  if (! $lang2_v) {
    echo "File $lang2/global.inc is missing ! Create it";
    return false;
  }

  $ref_v = get_admin_lang_all_src_used_vars();

  switch ($mode) {
  case "txt":
    txt_admin_lang_comp_global_lang_vars($lang, $lang2, $lang_v, $lang2_v, $ref_v);
    break;
  case "html":
    html_admin_lang_comp_global_lang_vars($lang, $lang2, $lang_v, $lang2_v, $ref_v);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }
}


///////////////////////////////////////////////////////////////////////////////
// TXT Display vars comparison from 2 langs for global lang file             //
// Parameters:
//   - $lang    : language src to display
//   - $lang2   : language dest to display
//   - $lang_v  : lang vars defined
//   - $lang2_v : lang vars defined
//   - $ref_v   : lang vars referenced in source files
///////////////////////////////////////////////////////////////////////////////
function txt_admin_lang_comp_global_lang_vars($lang, $lang2, $lang_v, $lang2_v, $ref_v) {
  echo "txt mode Not implemented: Run this action in HTML mode.\n";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display vars comparison from 2 langs for global lang file            //
// Parameters:
//   - $lang    : language src to display
//   - $lang2   : language dest to display
//   - $lang_v  : lang vars defined
//   - $lang2_v : lang vars defined
//   - $ref_v   : lang vars referenced in source files
///////////////////////////////////////////////////////////////////////////////
function html_admin_lang_comp_global_lang_vars($lang, $lang2, $lang_v, $lang2_v, $ref_v) {

  $nb_ref = count($ref_v);
  $nb_lang = count($lang_v);
  $nb_lang2 = count($lang2_v);
  $nb_ref_def = $nb_ref_def_2 = 0;
  $nb_ref_undef = $nb_ref_undef_2 = 0;
  $nb_def_unref = $nb_def_unref_2 = 0;

  $lang_vars = array_merge($lang_v, $lang2_v);
  $lang_vars = array_keys ($lang_vars);
  $lang_vars = array_unique($lang_vars);
  $nb_langs = count($lang_vars);

  while (list ($key, $lvar) = each ($lang_vars)) {
    $value = $lang_v[$lvar];
    $value2 = $lang2_v[$lvar];

    if (in_array($lvar, $ref_v)) {
      $nb_def_ref++;
      $text_def_ref .= "<tr>
        <td>$lvar</td>
        <td>$value</td>
        <td>$value2</td>
        </tr>";
    } else {
      $nb_def_unref++;
      $text_def_unref .= "<tr>
        <td>$lvar</td>
        <td>$value</td>
        <td>$value2</td>
        </tr>";
    }
  }

  echo "
    <div class=\"detail extra\">
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th colspan=\"3\">
      From global lang files : $nb_langs var defined $nb_ref
      </th>
    </tr><tr>
      <th>From lang file :</th>
      <th>$lang : $nb_lang vars defined</th>
      <th>$lang2 : $nb_lang2 vars defined</th>
    </tr><tr>
      <th colspan=\"3\">
        Vars defined and referenced : <b>$nb_def_ref</b>
    </th>
    </tr>
    </thead>
    <tbody>
      $text_def_ref
    </tbody>
    <thead>
    <tr>
      <th colspan=\"3\">
        Vars defined but not referenced : <b>$nb_def_unref</b></th>
    </tr>
    </thead>
    <tbody>
      $text_def_unref
    </tbody>
    </table>
    </div>";
}

?>
