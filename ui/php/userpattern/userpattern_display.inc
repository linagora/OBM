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
// OBM - File : userpattern_display.inc                                      //
//     - Desc : User Pattern Display File                                    //
// 2010-01-21 Vincent ALQUIER                                                //
///////////////////////////////////////////////////////////////////////////////

$fieldnames['title']       = $GLOBALS['l_title'];
$fieldnames['description'] = $GLOBALS['l_description'];
$fieldnames['timecreate']  = $GLOBALS['l_timeupdate'];
$fieldnames['timeupdate']  = $GLOBALS['l_timecreate'];

/**
 * Display User Pattern specific dataset fields
 * @param  OBM_DISPLAY   $OD          OBM_DISPLAY object (passed by reference)
 * @param  string        $fieldname   field to display
 * @param  string        $link_ok     true if links must be displayed
 * @return array                      hash with 'name', 'url', 'align' values
 **/
function dis_userpattern_data(&$OD, $fieldname, $link_ok) {
  global $path, $perm, $ico_mail, $params,$obm,$cgp_use;
  global $l_enabled, $l_disabled, $l_forbidden, $l_perms;

  $res = array();

  if (($fieldname == 'title')) {
    if ($link_ok && $perm->check_right($GLOBALS['module'], $GLOBALS['cright_read_admin']))
      $res['url'] = "$path/userpattern/userpattern_index.php?action=detailconsult&amp;userpattern_id=".$OD->data_set->f('id');
  }

  return $res;
}

/**
 * Display User Pattern search form
 * @param  array   $userpattern  hash with userpattern values
 * @return string                html code
 **/
function dis_userpattern_search_form($userpattern='') {
  $block = html_userpattern_search_form($userpattern);
  return $block;
}

/**
 * Display: XHTML User Pattern search Form
 * @param  array   $userpattern  default form values
 * @return string                html code
 **/
function html_userpattern_search_form($userpattern) {

  $title = stripslashes($userpattern['title']);

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" 
  action=\"userpattern_index.php\">
    <label>{$GLOBALS['l_title']}<br />
      <input type=\"text\" name=\"tf_title\" size=\"12\" value=\"$title\" />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"{$GLOBALS['l_find']}\" />
    </label>
    <p class=\"CL\"></p>
  </form>";
  return $block;
}

/**
 * Display: User Pattern search result
 * @param  array   $userpattern  userpattern search criteria (keys used : title)
 * @return string                html code
 **/
function dis_userpattern_search_list($userpattern) {
  global $display, $obm;

  $prefs = get_display_pref($obm['uid'], 'userpattern');
  $obm_q = run_query_userpattern_search($userpattern);
  $nb_res = $obm_q->num_rows_total();
  if ($nb_res == 0) {
    $display['msg'] .= display_warn_msg($GLOBALS['l_no_found']);
  } else {
    $display['msg'] .= display_info_msg("$nb_res {$GLOBALS['l_found']}");
    $block = html_userpattern_search_list($obm_q, $prefs, $userpattern);
  }

  return $block;
}

/**
 * Display: User Pattern search result
 * @param  DB_OBM  $obm_q        list of userpatterns to display
 * @param  array   $prefs        the fields which have to be displayed
 * @param  array   $userpattern  userpattern search criteria
 * @return string                html code
 **/
function html_userpattern_search_list($obm_q, $prefs, $userpattern) {

  $title = urlencode(stripslashes($userpattern['title']));
  $url = url_prepare("userpattern_index.php?action=search&amp;tf_title=$title");

  $dis_userpattern = new OBM_DISPLAY('DATA', $prefs, 'userpattern');
  $dis_userpattern->data_set = $obm_q;
  $dis_userpattern->data_url = $url;
  $dis_userpattern->data_header = 'both';

  $block = $dis_userpattern->display('dis_userpattern_data');
  return $block;
}

/**
 * Display: User Pattern detail page
 * @param  UserPattern $pattern userpattern to display
 * @return string               html code
 **/
function dis_userpattern_consult($pattern) {
  $block = html_userpattern_consult($pattern);
  return $block;
}

/**
 * Display: User Pattern detail page
 * @param  UserPattern $pattern userpattern to display
 * @return string               html code
 **/
function html_userpattern_consult($pattern) {
  global $display, $obm, $cgp_use;

  $attributes = $pattern->get_attributes();
  $datebegin = $attributes['datebegin'];
  $dateexp = $attributes['dateexp'];

  $dis_hidden = $attributes['hidden'] ? $GLOBALS['l_yes'] : $GLOBALS['l_no'];
  $profile = $attributes['profile'];
  $dis_profile = isset($GLOBALS["l_perm_$profile"]) ? $GLOBALS["l_perm_$profile"] : $profile;
  $dis_datebegin = (empty($datebegin) || $datebegin[0]=='%') ? $datebegin : of_date_upd_format($datebegin, true);
  if (!$attributes['noexperie']) {
    $dis_dateexp = (empty($dateexp) || $dateexp[0]=='%') ? $dateexp : of_date_upd_format($dateexp, true);
    $dis_dateexp_row = "<tr>
      <th>{$GLOBALS['l_dateexp']}</th>
      <td>$dis_dateexp</td>
    </tr>";
  }

  // Target Delegation handling (if user as delegation admin realm)
  if ($cgp_use['property']['delegation']) {
    $delegation_target = $attributes['delegation_target'];
    $delegation = $attributes['delegation'];
    $dis_delegation_target = "
    <tr>
      <th>{$GLOBALS['l_delegation_target']}</th>
      <td>$delegation_target</td>
    </tr>";

    $dis_delegation = "
    <tr>
      <th>{$GLOBALS['l_delegation']}</th>
      <td>$delegation</td>
    </tr>";
  }
  $mail_state = $attributes['mail_perms']==1 ? $GLOBALS['l_enabled'] : $GLOBALS['l_disabled'];
  $mail_server_id = $attributes['mail_server_id'];
  $mail_quota = $attributes['mail_quota'];
  $email = $attributes['email'];

  // Mail
  if ($cgp_use['service']['mail']) {
    $mail_section = "
    <div class=\"detail infos\">
    <h1>$GLOBALS[l_mail]</h1>

    <table>
    <tr>
      <th>$GLOBALS[l_mail]</th>
      <td>$mail_state</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_mail_server]</th>
      <td>";
      if ($mail_server_id == "auto") {
        $mail_section .= $GLOBALS['l_mailserver_auto'];
      } else if ($mail_server_id && is_numeric($mail_server_id)) {
        $mail_server_host_name = get_last_host_text($mail_server_id);
        $mail_section .= "<a href=\"$GLOBALS[path]/host/host_index.php?action=detailconsult&host_id=".$mail_server_id."\">".$mail_server_host_name."</a>";
      } else {
        $mail_section .= '-';
      }
      $mail_section .= "</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_email]</th>
      <td>$email </td>
    </tr>
    <tr>
      <th>$GLOBALS[l_quota]</th>
      <td>$mail_quota</td>
    </tr>
    </table>
    </div>";
  } else {
    $mail_section = "
    <div class=\"detail infos\">
    <h1>$GLOBALS[l_mail]</h1>
    <table>
    <tr>
      <th>$GLOBALS[l_email]</th>
      <td>$email</td>
    </tr>
    </table>
    </div>";
  }

  $email_nomade = $attributes['email_nomade'];
  $nomade_perms = $attributes['nomade_perms'] == 1 ? $GLOBALS['l_yes'] : $GLOBALS['l_no'];
  $nomade_enable = $attributes['nomade_enable'] == 1 ? $GLOBALS['l_yes'] : $GLOBALS['l_no'];
  $nomade_local_copy = $attributes['nomade_local_copy'] == 1 ? $GLOBALS['l_yes'] : $GLOBALS['l_no'];
  if ($cgp_use['service']['mail_nomad']) {
    $nomad_section = "
    <div class=\"detail infos\">
    <h1>$GLOBALS[l_nomade]</h1>

    <table>
    <tr>
      <th>$GLOBALS[l_email_nomade]</th>
      <td>$email_nomade</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_perms]</th>
      <td>$nomade_perms</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_enabled]</th>
      <td>$nomade_enable</td>
    </tr>
    <tr>
      <th>$GLOBALS[l_local_copy]</th>
      <td>$nomade_local_copy</td>
    </tr>
    </table>
    </div>";
  }

  $block = "
<div class=\"detail extra\" style=\"overflow:hidden\">
  <h1>{$GLOBALS['l_userpattern']}</h1>
 
  <table>
  <tr>
    <th>{$GLOBALS['l_title']}</th>
    <td>{$pattern->title}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_description']}</th>
    <td>{$pattern->description}</td>
  </tr>
  </table>
</div>

<div class=\"detail infos\" style=\"overflow:hidden\">
  <h1>{$GLOBALS['l_user']}</h1>
 
  <table>
  <tr>
    <th>{$GLOBALS['l_login']}</th>
    <td>{$attributes['login']}</td>
  </tr>  
  <tr>
    <th>{$GLOBALS['l_commonname']}</th>
    <td>{$attributes['commonname']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_hidden']}</th>
    <td>$dis_hidden</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_profile']}</th>
    <td>$dis_profile</td>
  </tr>
  $dis_delegation
  $dis_delegation_target
  <tr>
    <th>{$GLOBALS['l_title']}</th>
    <td>{$attributes['title']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_datebegin']}</th>
    <td>$dis_datebegin</td>
  </tr>
  $dis_dateexp_row
</table>
</div>

<div class=\"detail infos\">
  <h1>{$GLOBALS['l_coord']}</h1>
  <table>
  <tr>
    <th>{$GLOBALS['l_phone']}</th>
    <td>{$attributes['phone']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_phone']} 2</th>
    <td>{$attributes['phone2']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_mphone']}</th>
    <td>{$attributes['mobile']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_fax']}</th>
    <td>{$attributes['fax']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_fax']} 2</th>
    <td>{$attributes['fax2']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_company']}</th>
    <td>{$attributes['company']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_direction']}</th>
    <td>{$attributes['direction']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_service']}</th>
    <td>{$attributes['service']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_address']} 1</th>
    <td>{$attributes['ad1']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_address']} 2</th>
    <td>{$attributes['ad2']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_address']} 3</th>
    <td>{$attributes['ad3']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_postcode']}</th>
    <td>{$attributes['zip']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_town']}</th>
    <td>{$attributes['town']}</td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_expresspostal']}</th>
    <td>{$attributes['cdx']}</td>
  </tr>
  </table>
</div>

<div class=\"detail infos\">
  <h1>{$GLOBALS['l_desc']}</h1>
  <table>
  <tr>
    <th>{$GLOBALS['l_desc']}</th>
    <td>{$attributes['desc']}</td>
  </tr>
  </table>
</div>

$web_section
$mail_section
$nomad_section";

  return $block;
}

/**
 * Display: User Pattern create form
 * @param  string      $action      current module action
 * @param  array       $params      userpattern parameters
 * @param  UserPattern $pattern     [optionnal] userpattern to update
 * @return string                   html code
 **/
function dis_userpattern_form($action, $params=array(), $pattern=null) {
  $block = html_userpattern_form($action, $params, $pattern);
  return $block;
}

/**
 * Display: User Pattern create form
 * @param  string      $action      current module action
 * @param  array       $params      userpattern parameters
 * @param  UserPattern $pattern     [optionnal] userpattern to update
 * @return string                   html code
 **/
function html_userpattern_form($action, $params=array(), $pattern=null) {
  global $display, $obm, $cgp_use;

  $domain_id = $obm['domain_id'];
  $profiles = get_all_profiles(false);

  if ($action=='new' || $action=='insert') {
    $noexperie = 'checked="checked"';
  }

  if (!is_null($pattern)) {
    $userpattern_title = $pattern->title;
    $userpattern_description = $pattern->description;

    $attributes = $pattern->attributes;
    $login             = $attributes['login'];
    $commonname        = $attributes['commonname'];
    $passwd            = $attributes['passwd']=='%random%' ? 'checked="checked"' : '';
    $hidden            = $attributes['hidden'] ? 'checked="checked"' : '';
    $profile           = $attributes['profile'];
    $delegation        = $attributes['delegation'];
    $delegation_target = $attributes['delegation_target'];
    $title             = $attributes['title'];
    $noexperie         = $attributes['noexperie'] ? 'checked="checked"' : '';
    $datebegin         = $attributes['datebegin'][0]=='%' ? $attributes['datebegin'] : of_date_upd_format($attributes['datebegin'],true);
    $dateexp           = $attributes['dateexp'][0]=='%' ? $attributes['dateexp'] : of_date_upd_format($attributes['dateexp'],true);
    $phone             = $attributes['phone'];
    $phone2            = $attributes['phone2'];
    $mobile            = $attributes['mobile'];
    $fax               = $attributes['fax'];
    $fax2              = $attributes['fax2'];
    $company           = $attributes['company'];
    $direction         = $attributes['direction'];
    $service           = $attributes['service'];
    $ad1               = $attributes['ad1'];
    $ad2               = $attributes['ad2'];
    $ad3               = $attributes['ad3'];
    $zip               = $attributes['zip'];
    $town              = $attributes['town'];
    $cdx               = $attributes['cdx'];
    $desc              = $attributes['desc'];
    $web_perms         = $attributes['web_perms'];
    $mail_perms        = $attributes['mail_perms'];
    $mail_server_id    = $attributes['mail_server_id'];
    $email             = $attributes['email'];
    $mail_quota        = $attributes['mail_quota'];
    $email_nomade      = $attributes['email_nomade'];
    $nomade_perms      = $attributes['nomade_perms'];
    $nomade_enable     = $attributes['nomade_enable'];
    $nomade_local_copy = $attributes['nomade_local_copy'];
  }

  if (isset($params['userpattern']['title']))       $userpattern_title       = $params['userpattern']['title'];
  if (isset($params['userpattern']['description'])) $userpattern_description = $params['userpattern']['description'];

  $userpattern = $params['attributes'];
  if (isset($userpattern['login']))             $login             = $userpattern['login'];
  if (isset($userpattern['commonname']))        $commonname        = $userpattern['commonname'];
  if (isset($userpattern['passwd']))            $passwd            = $userpattern['passwd']=='%random%' ? 'checked="checked"' : '';
  if (isset($userpattern['hidden']))            $hidden            = $userpattern['hidden'] ? 'checked="checked"' : '';
  if (isset($userpattern['profile']))           $profile           = $userpattern['profile'];
  if (isset($userpattern['delegation']))        $delegation        = $userpattern['delegation'];
  if (isset($userpattern['delegation_target'])) $delegation_target = $userpattern['delegation_target'];
  if (isset($userpattern['title']))             $title             = $userpattern['title'];
  if (isset($userpattern['noexperie']))         $noexperie         = $userpattern['noexperie'] ? 'checked="checked"' : '';
  $date = $userpattern['datebegin'];
  if (isset($userpattern['datebegin']))         $datebegin         = $date[0]=='%' ? $date : of_date_upd_format($date,true);
  $date = $userpattern['dateexp'];
  if (isset($userpattern['dateexp']))           $dateexp           = $date[0]=='%' ? $date : of_date_upd_format($date,true);
  if (isset($userpattern['phone']))             $phone             = $userpattern['phone'];
  if (isset($userpattern['phone2']))            $phone2            = $userpattern['phone2'];
  if (isset($userpattern['mobile']))            $mobile            = $userpattern['mobile'];
  if (isset($userpattern['fax']))               $fax               = $userpattern['fax'];
  if (isset($userpattern['fax2']))              $fax2              = $userpattern['fax2'];
  if (isset($userpattern['company']))           $company           = $userpattern['company'];
  if (isset($userpattern['direction']))         $direction         = $userpattern['direction'];
  if (isset($userpattern['service']))           $service           = $userpattern['service'];
  if (isset($userpattern['ad1']))               $ad1               = $userpattern['ad1'];
  if (isset($userpattern['ad2']))               $ad2               = $userpattern['ad2'];
  if (isset($userpattern['ad3']))               $ad3               = $userpattern['ad3'];
  if (isset($userpattern['zip']))               $zip               = $userpattern['zip'];
  if (isset($userpattern['town']))              $town              = $userpattern['town'];
  if (isset($userpattern['cdx']))               $cdx               = $userpattern['cdx'];
  if (isset($userpattern['desc']))              $desc              = $userpattern['desc'];
  if (isset($userpattern['web_perms']))         $web_perms         = $userpattern['web_perms'];
  if (isset($userpattern['mail_perms']))        $mail_perms        = $userpattern['mail_perms'];
  if (isset($userpattern['mail_server_id']))    $mail_server_id    = $userpattern['mail_server_id'];
  if (isset($userpattern['email']))             $email             = $userpattern['email'];
  if (isset($userpattern['mail_quota']))        $mail_quota        = $userpattern['mail_quota'];
  if (isset($userpattern['email_nomade']))      $email_nomade      = $userpattern['email_nomade'];
  if (isset($userpattern['nomade_perms']))      $nomade_perms      = $userpattern['nomade_perms'];
  if (isset($userpattern['nomade_enable']))     $nomade_enable     = $userpattern['nomade_enable'];
  if (isset($userpattern['nomade_local_copy'])) $nomade_local_copy = $userpattern['nomade_local_copy'];

  $class = array();
  if (is_array($GLOBALS['err']['fields'])) {
    foreach ($GLOBALS['err']['fields'] as $field) {
      $class[$field] = 'error';
    }
  }

  $extrafields = array();
  if (is_array($GLOBALS['cgp_user']['user']['field'])) {
    foreach ($GLOBALS['cgp_user']['user']['field'] as $fieldname => $properties) {
      $extrafields[$fieldname] = $attributes[$fieldname];
      if (isset($userpattern[$fieldname])) $extrafields[$fieldname] = $userpattern[$fieldname];
    }
  }
  if (is_array($GLOBALS['cgp_user']['user']['category'])) {
    foreach ($GLOBALS['cgp_user']['user']['category'] as $fieldname => $properties) {
      $extrafields[$fieldname] = $attributes[$fieldname];
      if (isset($userpattern[$fieldname])) $extrafields[$fieldname] = $userpattern[$fieldname];
    }
  }
  $block_userdata = of_userdata_dis_entity_form('user', null, $extrafields);

  $customFields = new OBM_Form_Field('user','');
  $block_userfields = $customFields->buildEntityForm($class, null, $extrafields);

  // Default exp is hidden
  if (empty($dateexp))	{
    $showexp='display:none';
    $disabled='disabled="disabled"';
  }

  // Profiles
  $delegation_modules = array(
    'user' => $GLOBALS['cright_write_admin'],
    'resource' => $GLOBALS['cright_write'], 
    'host' => $GLOBALS['cright_write'],
    'people' => $GLOBALS['cright_write'],
    'group' => $GLOBALS['cright_write_admin'],
    'mailshare' => $GLOBALS['cright_write']);
  foreach ($profiles as $k_profile => $one_profile) {
    $selected = '';
    foreach($delegation_modules as $mdl => $rgt) {
      $section = $GLOBALS['cgp_show']['module'][$mdl];
      if (Perm::check_section_right($section, $k_profile) && Perm::check_module_right($mdl, $rgt, $k_profile)) {
        $onselect .= "profiles['$k_profile'] = true\n";
        if (!isset($delegation_class) && $profile == $k_profile){
          $delegation_class = '';
          $selected = ' selected';
        } 
        break;
      }
    } 
    if (!isset($delegation_class) && $profile == $k_profile){
      $delegation_class = 'H';
      $selected = ' selected';
    } 

    $dis_profile = isset($GLOBALS["l_perm_$k_profile"]) ? $GLOBALS["l_perm_$k_profile"] : $k_profile;
    $block_profile .= "<option value=\"$k_profile\"$selected>$dis_profile</option>";

  }
  if ($cgp_use['property']['delegation']) {
    if (!isset($delegation_target)) $delegation_target = $obm['delegation_target'];
    $dis_delegation_target = "
    <tr class='$delegation_class' id='delegationTargetBlock'>
      <th class=\"$class[delegation_target]\">{$GLOBALS['l_delegation_target']}</th>
      <td>
      <input id='delegationTargetField' name=\"attributes[delegation_target]\" size=\"32\" maxlength=\"256\" value=\"$delegation_target\" />
      <script type='text/javascript'>
        $('delegationField').addEvent('keyup' , function () {
          $('delegationTargetField').value = $('delegationField').value;
        })
        $('delegationField').addEvent('change' , function () {
          $('delegationTargetField').value = $('delegationField').value;
        })          
      </script>
      </td>
    </tr>";

    $block_delegation = "
    <tr>
      <th class=\"$class[delegation]\">{$GLOBALS['l_delegation']}</th>
      <td><input name=\"attributes[delegation]\" id=\"delegationField\" size=\"32\" maxlength=\"256\" value=\"$delegation\"></td>
    </tr>";
  }

  // Section Web
  if ($cgp_use['service']['web']) {
    $web_state  = ($web_perms ? 'checked' : '');
    
    $web_section = "
  <fieldset class=\"detail infos\">
  <legend>{$GLOBALS['l_access']}</legend>

  <table>
  <tr>
    <th class=\"$class[web_perms]\"><label for=\"cb_web_perms\">{$GLOBALS['l_web']}</label></th>
    <td><input id=\"cb_web_perms\" name=\"attributes[web_perms]\" type=\"checkbox\" value=\"1\" $web_state /></td>
  </tr>
  $dis_access
  </table>
  </fieldset>";
  }

  // Section Mail
  if ($cgp_use['service']['mail']) {
    $dis_mail = dis_user_mail_field($domain_id, $email, array());
    
    // Mail perms checkbox checked values
    $mail_state  = ($mail_perms ? 'checked' : '');
    $mail_ext_state  = ($mail_ext_perms ? 'checked' : '');
    $dis_mailserver = of_domain_dis_mailserver_select('imap', $mail_server_id, $obm['domain_id']);
    
    // BEGIN - automatic mail server chooser hooks
    
    if (has_mail_server_auto()) {
      $dis_mailserver = preg_replace('/(<select [^>]+>)/',
        "\\1 <option value=\"auto\">{$GLOBALS['l_mailserver_auto']}</option>", $dis_mailserver);
    }
    
    // END - automatic mail server chooser hooks
    
    if ($mail_state) {
      $externalKlass = 'H';
      $externalEnabled = 'disabled="disabled"';
    } else {
      $internalKlass = 'H';
    }
    $main_domain = $obm['domain_name'];
    $mail_section = "
  <fieldset class=\"detail infos\" id=\"userMail\">
  <legend>{$GLOBALS['l_mail']}</legend>
  <table>
    <tr>
      <th class=\"$class[mail_perms]\"><label for=\"userMailActive\">{$GLOBALS['l_mail']}</label></th>
      <td><input name=\"attributes[mail_perms]\" type=\"checkbox\" id=\"userMailActive\" onclick=\"switch_mail_mode();\" value=\"1\" $mail_state /></td>
    </tr>          
  </table>
  <table id=\"userInternalMail\" class='$internalKlass'>
    <tr id=\"user_mail\">
      <th class=\"$class[mail_server]\">{$GLOBALS['l_mail_server']}</th>
      <td>$dis_mailserver</td>
    </tr>
    <tr>
    $dis_mail
    </tr>        
    <tr id=\"user_mail\">
    <th class=\"$class[mail_quota]\">{$GLOBALS['l_quota']}</th>
    <td><input name=\"attributes[mail_quota]\" maxlength=\"6\" size=\"6\" value=\"$mail_quota\" /></td>
    </tr>      
  </table>
  <table id=\"userExternalMail\" class='$externalKlass'>
    <tr>
      <th class=\"$class[email]\">{$GLOBALS['l_email']}</th>
      <td><input type=\"text\" name=\"attributes[email]\" $externalEnabled value=\"".((!$mail_state)?$email:'')."\" /></td>
    </tr>  
  </table>
  <script type='text/javascript'>
    var internal = $('userInternalMail');
    var external = $('userExternalMail');
    if($('userMailActive').checked == true) {
      external.dispose();
    } else {
      internal.dispose();  
    }
  </script>
  </fieldset>";
  } else {
  $mail_section = "
    <fieldset class=\"detail infos\"  id=\"user_external_mail\">
    <legend>{$GLOBALS['l_mail']}</legend>
    <table>    
    <tr>
      <th class=\"$class[email]\">{$GLOBALS['l_email']}</th>
      <td><input type=\"text\" name=\"attributes[email]\" value=\"$email\" /></td>
    </tr>
    </table>
    </fieldset>";
  }

  if ($cgp_use['service']['mail_nomad']) {
  // Nomad chekbox checked value
    if ($nomade_perms) $nomade_perms_c = ' checked';
    if ($nomade_enable) $nomade_enable_c = ' checked';
    if ($nomade_local_copy) $nomade_local_copy_c = ' checked';

    $nomade_mail_block = dis_user_nomade_mail_field($email_nomade,$class);
    $nomad_section = "
  <fieldset class=\"detail infos\">
  <legend>{$GLOBALS['l_nomade']}</legend>

  <table>
  $nomade_mail_block
  <tr>
    <th class=\"$class[nomade_perms]\"><label for=\"cb_nomade_perms\">{$GLOBALS['l_perms']}</label></th>
    <td><input id=\"cb_nomade_perms\" name=\"attributes[nomade_perms]\" type=\"checkbox\" value=\"1\" $nomade_perms_c /></td>
  </tr>
  <tr>
    <th class=\"$class[nomade_enable]\"><label for=\"cb_nomade_enable\">{$GLOBALS['l_enabled']}</label></th>
    <td><input id=\"cb_nomade_enable\" name=\"attributes[nomade_enable]\" type=\"checkbox\" value=\"1\" $nomade_enable_c /></td>
  </tr>
  <tr>
    <th class=\"$class[nomade_local_copy]\"><label for=\"cb_nomade_local_copy\">{$GLOBALS['l_local_copy']}</label></th>
    <td><input id=\"cb_nomade_local_copy\" name=\"attributes[nomade_local_copy]\" type=\"checkbox\" value=\"1\" $nomade_local_copy_c /></td>
  </tr>
  </table>
  </fieldset>";
  }

  $validation_url = url_prepare('userpattern_index.php');

  // Buttons
  if (($action == 'new') || ($action == 'insert')) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"{$GLOBALS['l_insert']}\" />";

  } elseif (($action=='detailupdate') || ($action=='update')) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"userpattern_id\" value=\"{$pattern->id}\" />
      <input type=\"submit\" value=\"{$GLOBALS['l_update']}\" />";
  }

  $block = "
  <form id=\"dataUserPattern\" method=\"post\" name=\"f_entity\"
    action=\"$validation_url\">
  <fieldset class=\"detail extra\">
  <legend>{$GLOBALS['l_userpattern']}</legend>
  <table>
  <tr>
    <th>{$GLOBALS['l_title']}</th>
    <td><input name=\"tf_userpattern[title]\" size=\"48\" maxlength=\"255\" value=\"$userpattern_title\" /></td>
  </tr>
  <tr>
    <th>{$GLOBALS['l_description']}</th>
    <td><input name=\"tf_userpattern[description]\" size=\"48\" maxlength=\"255\" value=\"$userpattern_description\" /></td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>{$GLOBALS['l_user']}</legend>
  <script type='text/javascript'>
    var profiles = new Object();
    $onselect
    function showDelegation(field) {
      var value = field.options[field.selectedIndex].value;
      if(profiles[value]) {
        $('delegationTargetBlock').removeClass('H');
      } else {
        $('delegationTargetBlock').addClass('H');
        $('delegationTargetField').value = '';
      }
    }
    function showExpiration()	{
      var Props = $('noexperie').getProperty('checked');			
      if(Props==false){
        $('exp').setStyle('display','');
        $('exp').getElement('input').setProperty('disabled',false);
      }else{
        $('exp').setStyle('display','none');
        $('exp').getElement('input').setProperty('disabled',true);
      }	
    }
  </script>
  <table>
  <tr>
    <th class=\"$class[login]\">{$GLOBALS['l_login']}</th>
    <td><input id=\"\" type=\"text\" name=\"attributes[login]\" value=\"$login\" /></td>
  </tr>
  <tr>
    <th class=\"$class[commonname]\">{$GLOBALS['l_commonname']}</th>
    <td><input id=\"\" type=\"text\" name=\"attributes[commonname]\" value=\"$commonname\" /></td>
  </tr>
  <tr>
    <th class=\"$class[passwd]\"><label for=\"cb_attributes_password\">{$GLOBALS['l_generate_password']}</label></th>
    <td><input id=\"cb_attributes_password\" name=\"attributes[passwd]\" type=\"checkbox\" value=\"%random%\" $passwd /></td>
  </tr>
  <tr>
    <th class=\"$class[hidden]\"><label for=\"cb_hidden\">{$GLOBALS['l_hidden']}</label></th>
    <td><input id=\"cb_hidden\" name=\"attributes[hidden]\" type=\"checkbox\" value=\"1\" $hidden /></td>
  </tr>
  <tr>
    <th class=\"$class[profile]\">{$GLOBALS['l_profile']}</th>
    <td><select name=\"attributes[profile]\" onchange='showDelegation(this);'>
      $block_profile
      </select>
    </td>
  </tr>
  $block_delegation
  $dis_delegation_target
  <tr>
    <th class=\"$class[title]\">{$GLOBALS['l_title']}</th>
    <td><input name=\"attributes[title]\" size=\"32\" maxlength=\"64\" value=\"$title\" /></td>
  </tr>
  <tr>
    <th class=\"$class[datebegin]\">{$GLOBALS['l_datebegin']}</th>
    <td><input type=\"text\" name=\"attributes[datebegin]\" value=\"$datebegin\" class=\"datePicker\"/></td>
  </tr>
  <tr>
    <th class=\"$class[noexperie]\"><label for=\"noexperie\">{$GLOBALS['l_noexperie']}</label></th>
    <td><input id=\"noexperie\" name=\"attributes[noexperie]\" type=\"checkbox\" value=\"1\" $noexperie onclick='showExpiration();'/></td>
  </tr>
  <tr id=\"exp\" style=\"$showexp\">
    <th class=\"$class[dateexp]\">{$GLOBALS['l_dateexp']}</th>
    <td><input type=\"text\" name=\"attributes[dateexp]\" $disabled value=\"$dateexp\" class=\"datePicker\"/></td>
  </tr>
  $block_userdata
  $block_userfields
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>{$GLOBALS['l_coord']}</legend>
  <table>
  <tr>
    <th class=\"$class[phone]\">{$GLOBALS['l_phone']}</th>
    <td><input name=\"attributes[phone]\" size=\"20\" maxlength=\"20\" value=\"$phone\" /></td>
  </tr>
  <tr>
    <th class=\"$class[phone2]\">{$GLOBALS['l_phone']} 2</th>
    <td><input name=\"attributes[phone2]\" size=\"20\" maxlength=\"20\" value=\"$phone2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[mobile]\">{$GLOBALS['l_mphone']}</th>
    <td><input name=\"attributes[mobile]\" size=\"20\" maxlength=\"20\" value=\"$mobile\" /></td>
  </tr>
  <tr>
    <th class=\"$class[fax]\">{$GLOBALS['l_fax']}</th>
    <td><input name=\"attributes[fax]\" size=\"20\" maxlength=\"20\" value=\"$fax\" /></td>
  </tr>
  <tr>
    <th class=\"$class[fax2]\">{$GLOBALS['l_fax']} 2</th>
    <td><input name=\"attributes[fax2]\" size=\"20\" maxlength=\"20\" value=\"$fax2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[company]\">{$GLOBALS['l_company']}</th>
    <td><input name=\"attributes[company]\" size=\"64\" maxlength=\"64\" value=\"$company\" /></td>
  </tr>
  <tr>
    <th class=\"$class[direction]\">{$GLOBALS['l_direction']}</th>
    <td><input name=\"attributes[direction]\" size=\"64\" maxlength=\"64\" value=\"$direction\" /></td>
  </tr>
  <tr>
    <th class=\"$class[service]\">{$GLOBALS['l_service']}</th>
    <td><input name=\"attributes[service]\" size=\"64\" maxlength=\"64\" value=\"$service\" /></td>
  </tr>
  <tr>
    <th class=\"$class[ad1]\">{$GLOBALS['l_address']} 1</th>
    <td><input name=\"attributes[ad1]\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad1\" /></td>
  </tr>
  <tr>
    <th class=\"$class[ad2]\">{$GLOBALS['l_address']} 2</th>
    <td><input name=\"attributes[ad2]\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[ad3]\">{$GLOBALS['l_address']} 3</th>
    <td><input name=\"attributes[ad3]\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad3\" /></td>
  </tr>
  <tr>
    <th class=\"$class[zip]\">{$GLOBALS['l_postcode']}</th>
    <td><input name=\"attributes[zip]\" size=\"8\" maxlength=\"8\" value=\"$zip\" /></td>
  </tr>
  <tr>
    <th class=\"$class[town]\">{$GLOBALS['l_town']}</th>
    <td><input name=\"attributes[town]\" size=\"24\" maxlength=\"24\" value=\"$town\" /></td>
  </tr>
  <tr>
    <th class=\"$class[cdx]\">{$GLOBALS['l_expresspostal']}</th>
    <td><input name=\"attributes[cdx]\" size=\"16\" maxlength=\"16\" value=\"$cdx\" /></td>
  </tr>
  </table>
  </fieldset>
$web_section

  <fieldset class=\"detail infos\">
  <legend>{$GLOBALS['l_desc']}</legend>
  <table>
  <tr>
    <th class=\"$class[desc]\">{$GLOBALS['l_desc']}</th>
    <td><input name=\"attributes[desc]\" size=\"48\" maxlength=\"255\" value=\"$desc\" /></td>
  </tr>
  </table>
  </fieldset>
$mail_section
$nomad_section

  <fieldset class=\"buttons\">
  $dis_button
  </fieldset>

  <fieldset class=\"help\">
  <legend>{$GLOBALS['l_help']}</legend>
  ".html_userpattern_help()."
  </fieldset>
  </form>
    ";

  return $block;
}

/**
 * Display: User Pattern create form
 * @return string                   html code
 **/
function html_userpattern_help() {

  foreach (array('string','date') as $type) {
    $block .= "<p>{$GLOBALS["l_userpattern_{$type}_help"]}</p>";
    $block .= "<table style=\"width: 100%;\">";
    $newline = 1;
    foreach (UserPattern::keywords($type) as $keyword) {

      if ($keyword=='%today+') {
        $keyword = '%today+x%';
        $description = $GLOBALS['l_userpattern_today_plus_help'];
      } elseif ($keyword=='%today-') {
        $keyword = '%today-x%';
        $description = $GLOBALS['l_userpattern_today_minus_help'];
      } else {
        $attribute = substr($keyword,1,-1);
        $description = $GLOBALS["l_{$attribute}"];
      }

      $help_text = "<th style=\"width:10%;text-align:right;\">$keyword</th><td style=\"width:40%;\">$description</td>";
      if ($newline)
        $block .= "<tr>$help_text";
      else
        $block .= "$help_text</tr>";
      $newline = 1-$newline;
    }
    if (!$newline)
      $block .= "<th style=\"width:10%;text-align:right;\"></th><td style=\"width:40%;\"></td></tr>";
    $block .= "</table>";
  }

  return $block;
}

/**
 * Generate email field  
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_user_mail_field($domain_id, $values, $class) {

  if (!$GLOBALS['obm']['domain_global']) {
    if(!$domain_id) $domain_id = $GLOBALS['obm']['domain_id'];
    $d = of_domain_get_domain_infos($domain_id, true);
    if(!empty($d['alias'])) {
      $aliases = explode("\r\n",$d['alias']);
    } else {
      $aliases = array();
    }
    array_unshift($aliases,$d['name']);
  } else {
    // Creation from GLOBAL, do not propose aliases as we don't know the domain
    $d = of_domain_get_domain_infos($GLOBALS['obm']['domain_id'], true);
    $aliases = array();
  }

  if(!empty($values)) {
    $emails = explode("\r\n",$values);
  }

  $count = 0;
  if(is_array($emails)) {
    foreach($emails as $key => $email) {
      $sel_alias = '';
      list($mail,$domain) = explode('@',$email);  
      foreach($aliases as $alias) {
        if( $alias == $domain) {
          $sel_alias .= "<option selected='selected' value='$alias'>$alias</option>";
        } else {
          $sel_alias .= "<option value='$alias'>$alias</option>";
        } 
      }
      $sel_alias = "<select name='attributes[aliases][]'>
        <option value=''>$GLOBALS[l_all_aliases]</option>
        $sel_alias
        </select>";

      $email_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'userMailHome');show_hide_add_button();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>        
        <input name='attributes[email][]' value=\"$mail\" /> @ $sel_alias
        </div>";
      $count ++;
    }
  }
  $sel_alias = '';
  foreach($aliases as $alias) {
    $sel_alias .= "<option value=\"$alias\">$alias</option>";
    $sel_js .= "aliasSelectTemplate.adopt(new Element('option').setProperty('value','$alias').appendText('$alias'));\n";
  }
  $sel_alias = "
    <select name=\"attributes[aliases][]\">
    <option value=\"\">$GLOBALS[l_all_aliases]</option>
    $sel_alias
    </select>";
  if($GLOBALS['c_max_user_alias'] ==0 || $count < $GLOBALS['c_max_user_alias']) {
    $email_block .= "
      <div class=\"multiple\">
      <a onclick=\"remove_element(this.parentNode,'userMailHome');show_hide_add_button();return false\" href=\"\">
       <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
      </a>        
      <input name='attributes[email][]' value='' /> @ $sel_alias 
      </div>         
      ";
    $count++;
  } 
  if($GLOBALS['c_max_user_alias'] ==0 || $count < $GLOBALS['c_max_user_alias']) {
    $add_button = "<a id='addMailButton' href='' onclick=\"add_email_field(aliasSelectTemplate);show_hide_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  }
  
  $email_block = "
  <th class=\"$class[email]\" id='userMailLabel'>
  $GLOBALS[l_email]
  <script type='text/javascript'>
    var aliasSelectTemplate = new Element('select').setProperty('name','sel_aliases[]');
    aliasSelectTemplate.adopt(new Element('option').setProperty('value','').appendText('$GLOBALS[l_all_aliases]'));
    $sel_js
  </script>
  $add_button
  </th>
  <td id='userMailHome'>
  $email_block
  </td>
  ";

  return $email_block;
}


/**
 * Generate external email field 
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_user_nomade_mail_field($emails, $class) {

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
        <input class='emailNomade' name='attributes[email_nomade][]' value=\"$email\" />
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
    <input class='emailNomade' name='attributes[email_nomade][]' value='' />
    </div>
    ";  
    $count++;
  }
  if ($GLOBALS['c_max_email_nomade'] ==0 || $count < $GLOBALS['c_max_email_nomade']) {
    $add_button = "<a id='addMailNomadeButton' href='' onclick=\"add_nomade_email_field();show_hide_nomade_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  }

  $email_block = "
  <tr><th class=\"$class[email_nomade]\" id='nomadeMailLabel'>
  $GLOBALS[l_email_nomade]
  $add_button
  </th>
  <td id='nomadeMailHome'>
  $email_block
  </td></tr>";

  return $email_block;
}


/**
 * Display: User Pattern delete form
 * @param  UserPattern $pattern     userpattern to delete
 * @return string                   html code
 **/
function dis_userpattern_can_delete($pattern) {
  $url = url_prepare('userpattern_index.php');

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"userpattern_id\" value=\"{$pattern->id}\" />
    <input type=\"submit\" value=\"{$GLOBALS['l_back']}\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\" onsubmit=\"return confirm_del(this)\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"userpattern_id\" value=\"{$pattern->id}\" />
    <input type=\"submit\" value=\"{$GLOBALS['l_delete']}\" />
    </form>";

  $GLOBALS['display']['msg'] .= display_ok_msg($GLOBALS['l_can_delete']);
 
  $block .= "
    <h1>{$GLOBALS['l_userpattern']} : {$pattern->title}</h1>
    <br />
    <div class=\"buttons\">
    $dis_delete
    $dis_back
    </div>";

  return $block;
}


?>
