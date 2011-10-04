<script language="php">
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
