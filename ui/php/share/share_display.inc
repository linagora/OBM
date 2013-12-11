<?php
/******************************************************************************
Copyright (C) 2011-2013 Linagora

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
require_once(dirname(__file__)."/../../obminclude/lang/".$_SESSION['set_lang']."/calendar.inc");
require_once(dirname(__file__)."/../share/share_js.inc");

function html_share_calendar($id,$name,$entity_type) {
  global $l_html, $l_share_ics, $l_public, $l_private, $l_share_mail_search;
  global $ico_popup_close, $ico_html, $ico_ical;
  global $prevent_public_agenda_sharing_by_url, $prevent_public_agenda_sharing_by_email;

  if ($id == $GLOBALS['obm']['uid']) {
    $user = "<h1>".$GLOBALS['l_my_share_calendar']."</h1>";
  } else {
    $user = "<h1>".$GLOBALS['l_user_share_calendar']." $name</h1>";
  }

  $url_display_css = $mail_display_css = $tr_display_css = 'display:visible;';
  if ( $prevent_public_agenda_sharing_by_url == true ) $url_display_css = 'display:none;';
  if ( $prevent_public_agenda_sharing_by_email == true ) $mail_display_css = 'display:none;';
  if ( $prevent_public_agenda_sharing_by_email == true && $prevent_public_agenda_sharing_by_url == true ) $tr_display_css = 'display:none;';

  //$params = get_calendar_entity_share($id,$entity_type);
  $share_pop_up = html_share_popup($params['accept_public']);
  //get the Calendar public freebusy preferences of the current user
  $user_pref = get_one_user_pref($id, 'set_public_fb');
  $block = "
      $user
      <table class=\"spreadSheet share\">
        <thead>
          <tr>
            <th>
            </th>
            <th>
              $l_share_ics
            </th>
            <th>
              $l_html
            </th>
            <th>
            </th>
          </tr>
        </thead>
        <tbody> ";

  // test if the OBM freebusy information charing is permitted for the current user
  if($user_pref[$id]['value'] == 'yes'){
    $block .= "
            <tr>
              <th>$GLOBALS[l_share_freebusy]
              <div class=\"sharedesc\">
                  <a id=\"tooltip_freebusy\" class=\"shareTip\">$GLOBALS[l_know_more]</a>
                </div>
              </th>
              <td>
                <a href=\"javascript: getShareUrl('freebusy','Ics');\" title=\"$GLOBALS[l_share_calendar_freebusy]\"><img src=\"$GLOBALS[ico_ref]\"></a>
                <a href=\"javascript: sendSharePopup('freebusy','Freebusy');\" title=\"$GLOBALS[l_send_calendar_freebusy]\"><img src=\"$GLOBALS[ico_mailforward]\"></a>
              </td><td></td><td></td>
            </tr>";
  }
  $block .="
          <tr style=\"$tr_display_css\">
            <th>
              $GLOBALS[l_share_calendar_public]
              <div class=\"sharedesc\">
                <a id=\"tooltip_public\" class=\"shareTip\">$GLOBALS[l_know_more]</a>
              </div>
            </th>
            <td>
              <a href=\"javascript: getShareUrl('public','Ics');\" title=\"$GLOBALS[l_share_calendar_public]\" style=\"$url_display_css\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('public','Ics');\" title=\"$GLOBALS[l_send_calendar_public]\" style=\"$mail_display_css\"><img src=\"$GLOBALS[ico_mailforward]\"></a>
            </td>
            <td>
              <a href=\"javascript: getShareUrl('public','Html');\" title=\"$GLOBALS[l_share_calendar_public]\" style=\"$url_display_css\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('public','Html');\" title=\"$GLOBALS[l_send_calendar_public]\" style=\"$mail_display_css\"><img src=\"$GLOBALS[ico_mailforward]\"></a>
            </td>
            <td>
              <a href=\"javascript: reinitSharePopup('public');\"><label>$GLOBALS[l_share_reinit_public]</label></a>
            </td>
          </tr>
          <tr>
            <th>
              $GLOBALS[l_share_calendar_private]
              <div class=\"sharedesc\">
                <a id=\"tooltip_private\" class=\"shareTip\">$GLOBALS[l_know_more]</a>
              </div>
            </th>
            <td>
              <a href=\"javascript: getShareUrl('private','Ics');\" title=\"$GLOBALS[l_share_calendar_private]\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('private','Ics');\" title=\"$GLOBALS[l_send_calendar_private]\"><img src=\"$GLOBALS[ico_mailforward]\"></a>
            </td>
            <td>
              <a href=\"javascript: getShareUrl('private','Html');\" title=\"$GLOBALS[l_share_calendar_private]\"><img src=\"$GLOBALS[ico_ref]\"></a>
              <a href=\"javascript: sendSharePopup('private','Html');\" title=\"$GLOBALS[l_send_calendar_private]\"><img src=\"$GLOBALS[ico_mailforward]\"></a>
            </td>
            <td>
              <a href=\"javascript: reinitSharePopup('private');\"><label>$GLOBALS[l_share_reinit_private]</label></a>
            </td>
          </tr>";

  $block .="
        </tbody>
      </table>
      <input id=\"share_entity_id\" type=\"hidden\" value=\"$id\"/>
      <input id=\"share_entity_type\" type=\"hidden\" value=\"$entity_type\"/>
      <script>
        window.addEvent('domready', function() {
          $('tooltip_public').store('tip:text',\"<label>$GLOBALS[l_desc_share_adresse_public]</label>\");
          $('tooltip_private').store('tip:text',\"<label>$GLOBALS[l_desc_share_adresse_private]</label>\");
          new Obm.Tip($('tooltip_public'), {fixed:false, click:false}, 'shareTip');
          new Obm.Tip($('tooltip_private'), {fixed:false, click:false}, 'shareTip');
          var freebusyContainer = $('tooltip_freebusy');
          if ( freebusyContainer ) {
            freebusyContainer.store('tip:text',\"<label>$GLOBALS[l_desc_share_adresse_freebusy]</label>\");
            new Obm.Tip(freebusyContainer, {fixed:false, click:false}, 'shareTip');
          }

        });
      </script>
      $share_pop_up
    ";
  return $block;
}

function html_share_popup($token) {
  global $cgp_host, $ico_popup_close;
  global $l_share_public;

  $disp_url = "<div id=\"lnk_public_url\"></div>";

  $block = "
    <div id=\"calendarSharePopup\">
    </div>
  ";
  return $block;
}


?>
