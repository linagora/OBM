<script language="php">
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
// OBM - File : password_display.inc                                         //
//     - Desc : Password Display functions File                              //
// 2004-03-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Password Form
// Parameters:
//   - $usr_q    : User Password database result
//   - $password : default values or updated values (if error)
//   - $field    : [optionnal] field in error
///////////////////////////////////////////////////////////////////////////////
function html_password_form($usr_q, $password, $field='') {
  global $l_password, $l_old_password, $l_new_password, $l_new_password_bis;
  global $l_update;

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  $block = "
    <form method=\"post\" name=\"form_password\"
      action=\"" . url_prepare('password_index.php') . "\">

    <fieldset class=\"detail extra\">
    <legend>$l_password</legend>

    <table>
    <tr>
      <th class=\"$class[old_password]\">$l_old_password</th>
      <td><input name=\"tf_old_password\" type=\"password\" maxlength=\"64\" size=\"32\" value=\"\"></td>
    </tr>
    <tr>
      <th class=\"$class[new_password]\">$l_new_password</th>
      <td><input name=\"tf_new_password\" type=\"password\" maxlength=\"64\" size=\"32\" value=\"\"></td>
    </tr>
    <tr>
      <th class=\"$class[new_password_bis]\">$l_new_password_bis</th>
      <td><input name=\"tf_new_password_bis\" type=\"password\" maxlength=\"64\" size=\"32\" value=\"\"></td>
    </tr>
    </table>
    </fieldset>

    <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"action\" value=\"update\">
      <input type=\"submit\" value=\"$l_update\">
    </fieldset>
    </form>";

  return $block;
}


</script>
