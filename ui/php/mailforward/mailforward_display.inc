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
// OBM - File : mailforward_display.inc                                      //
//     - Desc : Mail forwarding Display functions File                       //
// 2002-09-01 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Mail Forwarding Consultation
// Parameters:
//   - $usr_q : User mail forwarding database result
///////////////////////////////////////////////////////////////////////////////
function html_forward_consult($usr_q) {
  global $l_mailforward, $l_internal_email;
  global $l_nomade_not_allowed,$l_local_copy;
  global $l_email_nomade, $l_enabled, $l_disabled, $l_forbid, $l_state;
  global $obm, $c_yes, $c_no;

  $id = $usr_q->f('userobm_id');
  $email = get_entity_email($usr_q->f('userobm_email'),$usr_q->f('domain_name'));
  $email_nomade = str_replace("\r\n","<br/>",$usr_q->f('userobm_email_nomade'));
  $nomade_perms = $usr_q->f('userobm_nomade_perms');
  $nomade_enable = $usr_q->f('userobm_nomade_enable');
  $nomade_local_copy = $usr_q->f('userobm_nomade_local_copy');    

  $domain = $obm['domain_name'];

  if (! $nomade_perms) {
    $dis_nomade = "<td class=\"detailLabel\">$l_nomade_not_allowed</td>";
  } else {
    if ($nomade_enable) $state = $l_enabled;
    else $state = $l_disabled;
    if ($nomade_local_copy) $state_local = $l_enabled;
    else $state_local = $l_disabled;
 
    $dis_nomade = "
    <tr>
      <th>$l_state</th>
      <td>$state</td>
    </tr>
    <tr>
      <th>$l_email_nomade</th>
      <td>$email_nomade</td>
    </tr>
    <tr>      
      <th>$l_local_copy</th>    
      <td>$state_local</td>
    </tr>";
  }

  $block = "
    <div class=\"detail extra\">
    <h1>$l_mailforward</h1>
    <table>
    <tr>
      <th>$l_internal_email</th>
      <td>$email</td>
    </tr>
    $dis_nomade
    </table>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mail Forwarding Form
// Parameters:
//   - $usr_q   : User mail forwarding database result
//   - $forward : default values or updated values (if error)
//   - $field   : [optionnal] field in error
///////////////////////////////////////////////////////////////////////////////
function html_forward_form($usr_q, $forward, $field='') {
  global $l_mailforward, $l_internal_email, $l_nomade_not_allowed;
  global $l_email_nomade, $l_enabled, $l_local_copy;
  global $obm, $action, $c_yes, $c_no, $l_update;

  $id = $usr_q->f('userobm_id');
  $email = get_entity_email($usr_q->f('userobm_email'),$usr_q->f('domain_name'));
  $nomade_perms = $usr_q->f('userobm_nomade_perms');

  $domain = $obm['domain_name'];
  
  // if update mode and first time value are taken from database
  if ($action == 'detailupdate') {
    $email_nomade = $usr_q->f('userobm_email_nomade');
    $nomade_enable = $usr_q->f('userobm_nomade_enable');
    $nomade_local_copy = $usr_q->f('userobm_nomade_local_copy');
  }

  // If parameters have been given, they supercede the default action value
  if (isset($forward['email_nomade'])) { $email_nomade = $forward['email_nomade']; }
  if (isset($forward['nomade_enable'])) { $nomade_enable = $forward['nomade_enable']; }
  if (isset($forward['nomade_local_copy'])) { $nomade_local_copy = $forward['nomade_local_copy']; }

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  // Nomad enable chekbox checked value
  if ($nomade_enable) $nomade_enable_c = ' checked';
  if ($nomade_local_copy) $nomade_local_copy_c = ' checked';
  if (! $nomade_perms) {
    $block = display_ok_msg($l_nomade_not_allowed);
    return $block;
  } else {
    if ($nomade_enable) $state = $l_enabled;
    else $state = $l_disabled;

    $dis_nomade = "
    <tr>
      <th class=\"$class[nomade_enable]\">$l_enabled</th>
      <td><input name=\"cb_nomade_enable\" type=\"checkbox\" value=\"1\" $nomade_enable_c></td>
    </tr>";
    $dis_nomade .= dis_mailforward_mail_field($email_nomade,$class);
  }


  $block = "
    <form method=\"post\" name=\"form_forward\"
      onSubmit=\"return check_mailforward(this);\"
      action=\"" . url_prepare('mailforward_index.php') . "\">

    <fieldset class=\"detail extra\">
    <legend>$l_mailforward</legend>

    <table>
    <tr>
      <th>$l_internal_email</th>
      <td>$email</td>
    </tr>
    $dis_nomade      
    <tr>
      <th class=\"$class[nomade_local_copy]\">$l_local_copy</th>
      <td><input name=\"cb_nomade_local_copy\" type=\"checkbox\" value=\"1\" $nomade_local_copy_c /></td>
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

/**
 * Generate external email field 
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_mailforward_mail_field($emails, $class) {

  if(!empty($emails) && !is_array($emails)) {
    $emails = explode("\r\n",$emails);
  }

  $count = 0;
  if(is_array($emails)) {
    foreach($emails as $key => $email) {
      $email_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'nomadeMailHome');show_hide_nomade_add_button();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>
        <input class='emailNomade' name='tf_email_nomade[]' value=\"$email\" />
        </div>";
      $count ++;
    }
  }

  if ($GLOBALS['c_max_email_nomade'] ==0 || $count < $GLOBALS['c_max_email_nomade']) {
    $email_block .= "
    <div class=\"multiple\">
    <a onclick=\"remove_element(this.parentNode,'nomadeMailHome');show_hide_nomade_add_button();return false\" href=\"\">
     <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
    </a>
    <input class='emailNomade' name='tf_email_nomade[]' value='' />
    </div>
    ";  
    $count++;
  }
  if ($GLOBALS['c_max_email_nomade'] ==0 || $count < $GLOBALS['c_max_email_nomade']) {
    $add_button = "<a id='addMailNomadeButton' href='' onclick=\"add_nomade_email_field();show_hide_nomade_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  }

  $email_block = "
  <th class=\"$class[email_nomade]\" id='nomadeMailLabel'>
  $GLOBALS[l_email_nomade]
  $add_button
  </th>
  <td id='nomadeMailHome'>
  $email_block
  </td>
  ";

  return $email_block;
}

