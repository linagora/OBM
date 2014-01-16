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
// OBM - File : php/campaign/campaign_display.php
//     - Desc : campaign Display File
// 2008-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////

$l_campaign_status = array(
  'created' => $l_created,
  'ready' => $l_ready,
  'running' => $l_running,
  'planified' => $l_planified,
  'finished' => $l_finished,
  'archived' => $l_archived,
  'error_mail_format' => $l_error_mail_format,
  'error_target' => $l_error_target,
  'error' => $l_error_misc,
);

$l_campaignentity = array(
  'User' => $l_user,
  'Group' => $l_group,
  'List' => $l_list
);


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields

$fieldnames['campaign_name'] = $l_name;
$fieldnames['campaign_status'] = $l_status;
$fieldnames['campaign_start_date'] = $l_start_date;
$fieldnames['campaign_end_date'] = $l_end_date;
$fieldnames['campaign_progress'] = $l_progress;

/*
 * Name : dis_data_campaign
 * Description : Display campaign specific dataset fields
 * Parameters :
 *    - object $OD : OBM_DISPLAY object (passed by reference)
 *    - string $fieldname : field to display
 *    - bool $link_ok : true if links must be displayed
 * Return : bool
 */
function dis_data_campaign (&$OD, $fieldname, $link_ok) {
  global $path, $params;
  $lang = &$GLOBALS;
  
  $ext_url = $params["ext_url"];
  $ext_element = $params["ext_element"];

  if (($fieldname == 'data_element')/* && $ext_element != ''*/) {
    $res["name"] = "<span id=\"data-campaign-" . $OD->data_set->f("campaign_id") ."\" style=\"display:none;\"></span>";
  }

  if ($fieldname == 'campaign_name') {
    $res['align'] = 'center';
    $res['name'] = $OD->data_set->f('campaign_name');
    if (empty($res['name']))
      $res['name'] = '-';

    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_campaign_get_id('" . $OD->data_set->f('campaign_id') . "', '" . $OD->data_set->f($fieldname) . "');";
    } else {
      $res['url'] = "$path/campaign/campaign_index.php?action=detailconsult&amp;campaign_id=". $OD->data_set->f('campaign_id');
    }
  }
  
  if ($fieldname == 'campaign_status') {
    $res['align'] = 'center';
    $status = $OD->data_set->f('campaign_status');
    $res['name'] = $lang['l_campaign_status'][array_search($status, $GLOBALS['c_campaign_status_enum'])];
  }
  
  if ($fieldname == 'campaign_start_date') {
    $res['align'] = 'center';
    $start_date = $OD->data_set->f('campaign_start_date');
    $res['name'] = ($start_date?of_date_format($start_date):'');
  }
  
  if ($fieldname == 'campaign_end_date') {
    $res['align'] = 'center';
    $end_date = $OD->data_set->f('campaign_end_date');
    $res['name'] = ($end_date?of_date_format($end_date):'');
  }
  
  if ($fieldname == 'campaign_progress') {
    $res['align'] = 'center';
    $res['name'] = $OD->data_set->f('campaign_progress') . '%';
  }
  
  return $res;
}

/*
 * Name : dis_campaign_form
 * Description : Display  Form
 * Parameters :
 *    - string $action : action called
 *    - array $params : hash values
 * Return : array
 */
function dis_campaign_form ($action, $params) {
  global $obm;
  global $c_campaign_status_enum;
  $lang = &$GLOBALS;

  $id = $params["campaign_id"];
  
  $campaign_q = run_query_campaign_detail($id);
  
  if ($action == 'update') {
    foreach (array('name', 'status', 'start_date', 'end_date', 'progress',
    	'manager_id', 'email', 'objective', 'comment',
    	'manager_firstname', 'manager_lastname') as $e) {
        ${$e} = $campaign_q->f('campaign_'. $e);
    	}
  	
    $start_date = of_date_upd_format($start_date, true);
    $end_date = of_date_upd_format($end_date, true);
  }
  
  // params revalidation
  
  foreach (array('name', 'status', 'start_date', 'end_date', 'progress',
  	'manager_id', 'email', 'objective', 'comment') as $e) {
      if (isset($params[$e]))
        $$e = $params[$e];
  	}
  
  // preprocessing data before display
  
  $status = $lang['l_campaign_status'][array_search($status, $c_campaign_status_enum)];
  $progress = "$progress %";
  
  $name = "<input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" />";
  $start_date = "<input type=\"text\" name=\"tf_start_date\" value=\"$start_date\" class=\"datePicker\" />";
  $end_date = "<input type=\"text\" name=\"tf_end_date\" value=\"$end_date\" class=\"datePicker\" />";
  
  $opts_manager_id = html_options_from_group_users($obm['group_com'], true, array($obm['uid']), $manager_id);
  $manager = "<select name=\"sel_manager_id\">$opts_manager_id</select>";

  $objective = "<textarea name=\"ta_objective\" rows=\"6\" cols=\"78\">$objective</textarea>";
  $comment = "<textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>";

  // start input campaign_email
  
  if (!empty($email)) {
    $email = "<div id=\"slide_link_campaign_email\">$lang[l_email_exists]</div>
    <div id=\"slide_detail_campaign_email\">
    <input name=\"fi_email\" size=\"30\" value=\"\" type=\"file\"></div>
    <script type=\"text/javascript\">init_fx_slide('campaign_email');</script>";
  } else {
    $email = "<input name=\"fi_email\" size=\"30\" value=\"\" type=\"file\" />";
  }

  // end input campaign_email
  
  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"post\" enctype=\"multipart/form-data\" action=\"\">
  <fieldset class=\"detail infos\">
  <legend>$lang[l_parameters]</legend>
  <table>
    <tr>
    <th>$lang[l_name]</th>
    <td>$name</td>
    </tr>
    <tr>
    <th>$lang[l_status]</th>
    <td>$status</td>
    </tr>
    <tr>
    <th>$lang[l_start_date]</th>
    <td>$start_date</td>
    </tr>
    <tr>
    <th>$lang[l_end_date]</th>
    <td>$end_date</td>
    </tr>
    <tr>
    <th>$lang[l_progress]</th>
    <td>$progress</td>
    </tr>
    <tr>
    <th>$lang[l_manager]</th>
    <td>$manager</td>
    </tr>
    <tr>
    <th>$lang[l_email]</th>
    <td>$email</td>
    </tr>
  </table>
  </fieldset>
  
  <fieldset class=\"detail extra\">
  <legend>$lang[l_objective]</legend>
  $objective
  </fieldset>
  
  <fieldset class=\"detail extra\">
  <legend>$lang[l_comment]</legend>
  $comment
  </fieldset>

  <fieldset class=\"buttons\">
    <input type=\"hidden\" name=\"campaign_id\" value=\"$id\" />
    <input type=\"hidden\" name=\"action\" value=\"$action\" />
    <input type=\"submit\" value=\"". $lang['l_'. $action]. "\" />
  </fieldset>
  
  </form>
  
";

  return $block;
}


/*
 * Name : dis_campaign_can_delete
 * Description : Display the  delete validation screen
 * Parameters :
 *    - integer $p_id : campaign id
 * Return : integer
 */
function dis_campaign_can_delete ($p_id) {
  global $l_campaign_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("campaign_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
  <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
  <input type=\"hidden\" name=\"campaign_id\" value=\"$p_id\" />
  <input type=\"submit\" value=\"$l_back\" />
  </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
  <input type=\"hidden\" name=\"action\" value=\"delete\" />
  <input type=\"hidden\" name=\"campaign_id\" value=\"$p_id\" />
  <input type=\"submit\" value=\"$l_delete\"
  onclick=\"if (confirm_campaign_del(this.form)) return true; else return false;\" />
  </form>";

  $display["msg"] .= display_ok_msg($l_campaign_can_delete);

  $block .= "
  <div class=\"buttons\">
  $dis_delete
  $dis_back
  </div>";

  return $block;
}

/*
 * Name : dis_campaign_consult
 * Description : Display the  Consult
 * Parameters :
 *    - array $params :
 * Return : array
 */
function dis_campaign_consult ($params) {
  global $c_campaign_status_enum;
  $lang = &$GLOBALS;

  $id = $params["campaign_id"];
  
  $campaign_q = run_query_campaign_detail($id);
  $targets = run_query_campaign_get_targets($id);
  $subcampaigns = run_query_campaign_get_subcampaigns($id);
  $update_enabled = can_update_campaign($campaign_q);
  
  foreach (array('name', 'status', 'start_date', 'end_date', 'progress',
  	'manager_id', 'email', 'objective', 'comment',
  	'manager_firstname', 'manager_lastname') as $e) {
      ${$e} = $campaign_q->f('campaign_'. $e);
  	}
  
  // preprocessing data before display
  
  $start_date = ($start_date?of_date_format($start_date):'');
  $end_date = ($end_date?of_date_format($end_date):'');
  
  $status = $lang['l_campaign_status'][array_search($status, $c_campaign_status_enum)];
  $progress = "$progress %";
  $manager = "$manager_firstname $manager_lastname";
  $comment = str_replace('  ', '&nbsp; ', nl2br($comment));
  $objective = str_replace('  ', '&nbsp; ', nl2br($objective));
  
  if ($email) {
    $email = "<a href=\"$path/document/document_index.php?action=accessfile&document_id=$email\">$lang[l_email_view]</a>";
  } else {
    $email = $lang['l_email_undefined'];
  }
  
  $block_targets = '';
  $i = 0;
  foreach ($targets as $target) {
    $module_entity = strtolower($lang['l_campaignentity'][$target['entity']]);
    $block_targets .= "<tr". ($i % 2 == 0 ? " class=\"pair\">":''). "
      <td class=\"C\"><input type=\"checkbox\" name=\"data-target-$target[entity_id]\"></td>
      <td class=\"C\">". $lang['l_campaignentity'][$target['entity']]. "</td>
      <td class=\"C\"><a href=\"../$module_entity/${module_entity}_index.php?action=detailconsult&${module_entity}_id=$target[source_id]\">$target[label]</a></td><tr>";
    $i += 1;
  }
  
  $block_targets = "
  <form method=\"POST\" action=\"\">
    <table class=\"result\">
      <thead><tr>
        <td class=\"C\">X</td>
        <td class=\"C\">$lang[l_target_type]</td>
        <td class=\"C\">$lang[l_target_desc]</td>
        </tr></thead>
      <tbody>$block_targets</tbody>
    </table>
    <input type=\"hidden\" name=\"action\" value=\"del_target\" />
    <input type=\"hidden\" name=\"campaign_id\" value=\"$id\" />
    <div class=\"buttons\">
      <input type=\"submit\" value=\"$lang[l_delete]\" />
    </div>
  </form>";
  
  if ($subcampaigns->num_rows() != 0) {
    $block_subcampaigns = '';
    while ($subcampaigns->next_record()) {
      $block_subcampaigns .= "<a href=\"?action=detailconsult&campaign_id="
        . $subcampaigns->f('campaign_id')
        . "\">(". dis_campaign_name_fieldset ($subcampaigns->f('campaign_name')). ")</a> ";
    }
    $block_subcampaigns = "
      <tr>
      <th>$lang[l_subcampaignss]</th>
      <td>$block_subcampaigns</td>
      </tr>";
  }
  
  if ($campaign_q->f('campaign_parent')) {
    $block_parent_campaign = "
      <tr>
      <th>$lang[l_parent_campaign]</th>
      <td><a href=\"?action=detailconsult&campaign_id=". $campaign_q->f('campaign_parent'). "\">$lang[l_go]</a></td>
      </tr>";
  }
  
  // --- HTML Template --------------------------------------------------------

  $block = "
  <div class=\"detail infos\">
  <table>
    <tr>
    <th>$lang[l_name]</th>
    <td>$name</td>
    </tr>
    <tr>
    <th>$lang[l_status]</th>
    <td>$status</td>
    </tr>
    <tr>
    <th>$lang[l_start_date]</th>
    <td>$start_date</td>
    </tr>
    <tr>
    <th>$lang[l_end_date]</th>
    <td>$end_date</td>
    </tr>
    <tr>
    <th>$lang[l_progress]</th>
    <td>$progress</td>
    </tr>
    <tr>
    <th>$lang[l_manager]</th>
    <td>$manager</td>
    </tr>
    <tr>
    <th>$lang[l_email]</th>
    <td>$email</td>
    </tr>
    $block_parent_campaign
    $block_subcampaigns
  </table>
  </div>
  
  <div class=\"detail extra\">
  <h1>$lang[l_objective]</h1>
  $objective
  </div>
  
  <div class=\"detail extra\">
  <h1>$lang[l_comment]</h1>
  $comment
  </div>
  
  <div class=\"detail extra\">
  <h1>$lang[l_targets]</h1>
  $block_targets
  </div>
  
";

  return $block;
}

function dis_campaign_name_fieldset ($value) {
  if (empty($value)) {
    return '-';
  } else {
    return $value;
  }
}

function dis_campaign_monitor ($params) {
  $lang = &$GLOBALS;
  
  $id = $params["campaign_id"];
  $mailtargets_q = run_query_campaign_get_mailtargets($id);
  
  $block_sent_emails = '';
  $i = 0;
  while ($mailtargets_q->next_record()) {
    $block_sent_emails .= "<tr". ($i % 2 == 0 ? " class=\"pair\">":''). "
      <td class=\"C\">". $mailtargets_q->f('entity'). "</td>
      <td class=\"C\">". $mailtargets_q->f('label'). "</td>
      <td class=\"C\">". $mailtargets_q->f('email'). "</td>
      <td class=\"C\">". $mailtargets_q->f('campaignmailtarget_status'). "</td>
      <tr>";
    $i += 1;
  }
  
  $block_sent_emails = "
    <table class=\"result\">
      <thead><tr>
        <td class=\"C\">$lang[l_target_type]</td>
        <td class=\"C\">$lang[l_target_desc]</td>
        <td class=\"C\">@</td>
        <td class=\"C\">$lang[l_status]</td>
        </tr></thead>
      <tbody>$block_sent_emails</tbody>
    </table>
    ";
    
  return $block_sent_emails;
}

/*
 * Name : dis_campaign_search_form
 * Description : Display  search form
 * Parameters :
 *    - array $dataobj :  data request values for search
 * Return : array
 */
function dis_campaign_search_form ($dataobj) {
  $block .= html_campaign_search_form($dataobj);

  return $block;
}

/*
 * Name : html_campaign_search_form
 * Description : Produce  search html form
 * Parameters :
 *    - array $params :  data request values for search
 * Return : array
 */
function html_campaign_search_form ($params) {
  global $l_find;
  global $l_or;
  global $l_name, $l_status, $l_created, $l_ready, $l_running, $l_planified, $l_finished, $l_archived, $l_error_mail_format, $l_error_target, $l_error_misc, $l_start_date, $l_end_date, $l_header_consult, $l_name, $l_status, $l_created, $l_ready, $l_running, $l_planified, $l_finished, $l_archived, $l_error_mail_format, $l_error_target, $l_error_misc, $l_start_date, $l_end_date, $l_progress, $l_header_display;
  global $l_archive;

  $name = $params['name'];
  $status = $params['status'];
  $start_date = $params['start_date'];
  $end_date = $params['end_date'];
  $popup = ($params['popup'] == '1');
  $archive = ($params['archive'] == '1' ? 'checked = "checked"' : '');

  if ($popup) {
    $ext_action = $params["ext_action"];
    $ext_url = $params["ext_url"];
    $ext_id = $params["ext_id"];
    $ext_title = ($params["ext_title"] ? $params["ext_title"] : $l_select_campaign);
    $ext_target = $params["ext_target"];
    $ext_widget = $params["ext_widget"];
    $ext_element = $params["ext_element"];
    $ext_widget_text = $params["ext_widget_text"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
    <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
    <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
    <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
    <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
    <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
    <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\">
    <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">
    <input name=\"popup\" type=\"hidden\" value=\"$popup\" />";
    if ($ext_title == "") {
      $ext_title = $l_add_users;
    }
    $display["title"] = $ext_title;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" class=\"search\" name=\"f_search\">
  <!--<label>Tous les champs<br/>
  <input name=\"tf_search_term\" size=\"20\" maxlength=\"30\" value=\"$search_term\" class=\"datePicker\"/>
  $l_or
  </label>-->
  <label>$l_name<br />
  <input type=\"text\" name=\"tf_name\" size=\"20\" maxlength=\"30\" value=\"$name\" />
  </label>
  <label>$l_status<br />
  <input type=\"text\" name=\"tf_status\" size=\"20\" maxlength=\"30\" value=\"$status\" />
  </label>
  <label>$l_start_date<br />
  <input type=\"text\" name=\"tf_start_date\" size=\"20\" maxlength=\"30\" value=\"$start_date\" class=\"datePicker\"/>
  </label>
  <label>$l_end_date<br />
  <input type=\"text\" name=\"tf_end_date\" size=\"20\" maxlength=\"30\" value=\"$end_date\" class=\"datePicker\"/>
  </label>
  <label>$l_archive<br />
  <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
  </label>
  <label>&nbsp;<br />
  <input name=\"action\" type=\"hidden\" value=\"search\" />
  <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
  $ext
  </label>
  <p class=\"CL\"></p>
  </form>";

  return $block;

}

/*
 * Name : dis_campaign_search_list
 * Description : Display the  search result
 * Parameters :
 *    - array $dataobj :  search criteria
 * Return : array
 */
function dis_campaign_search_list ($dataobj) {

  global $display, $obm;
  global $l_campaign_found, $l_campaign_no_found;

  $popup = ($dataobj["popup"] == 1);
  $prefs = get_display_pref($obm["uid"], "campaign");
  $obm_q = run_query_campaign_search($dataobj, array('name','status','start_date','end_date','name','status','start_date','end_date','progress'));
  $obm_count = $obm_q->num_rows_total();

  if ($obm_count == 0) {
    $display["msg"] .= display_warn_msg($l_campaign_no_found);
  } else {
    $display["msg"] .= display_info_msg($obm_count." ".$l_campaign_found);
    $block = html_campaign_search_list($dataobj, $obm_q, $prefs, $popup);
  }

  return $block;
}


/*
 * Name : html_campaign_search_list
 * Description : XHTML Display the  Search result
 * Parameters :
 *    - array $dataobj :  search criteria
 *    - array $obm_q : s list
 *    - array $prefs : fields to display in the s list
 *    - bool $popup : popup flag tell if external parameters to handle
 * Return : bool
 */
function html_campaign_search_list ($dataobj, $obm_q, $prefs, $popup) {
  global $l_close, $l_add;

  $name = urlencode($dataobj["name"]);
  $status = urlencode($dataobj["status"]);
  $start_date = urlencode($dataobj["start_date"]);
  $end_date = urlencode($dataobj["end_date"]);
  $name = urlencode($dataobj["name"]);
  $status = urlencode($dataobj["status"]);
  $start_date = urlencode($dataobj["start_date"]);
  $end_date = urlencode($dataobj["end_date"]);
  $progress = urlencode($dataobj["progress"]);
  if ($popup) {
    $ext_action = $dataobj["ext_action"];
    $ext_url = $user["ext_url"];
    $ext_id = $dataobj["ext_id"];
    $ext_target = $dataobj["ext_target"];
    $ext_widget = $dataobj["ext_widget"];
    $ext_widget_text = $dataobj["ext_widget_text"];
    $ext_element = $dataobj["ext_element"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $url = url_prepare("campaign_index.php?action=search&amp;tf_name=$name&amp;tf_status=$status&amp;tf_start_date=$start_date&amp;tf_end_date=$end_date&amp;tf_name=$name&amp;tf_status=$status&amp;tf_start_date=$start_date&amp;tf_end_date=$end_date&amp;tf_progress=$progress$url_ext");  $obm_q->seek($first_row);

  $obm_d = new OBM_DISPLAY('DATA', $prefs, 'campaign');
  if ($popup) {
    $obm_d->display_link = false;
    $obm_d->data_cb_text = "X";
    $obm_d->data_idfield = 'campaign_id';
    $obm_d->data_cb_name = "abcd";

    if ($ext_element != "") {
      $obm_d->data_cb_name = '';
      $obm_d->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this, 'campaign'); return false;\">";
    } elseif ($ext_widget != '') {
      $obm_d->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } else {
      $obm_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }

    $obm_d->data_form_end = "
    <div class=\"buttons\">
    <input type=\"submit\" value=\"$l_add\" />
    <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
    <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
    </div>
    </form>";

    $display_popup_end = "
    <p>
    <a href=\"\" onclick='window.close();'>$l_close</a>
    </p>";
  }

  $obm_d->data_set = $obm_q;
  $obm_d->data_url = $url;
  $obm_d->data_header = "both";

  $block = $obm_d->display("dis_data_campaign");
  $block .= $display_popup_end;

  return $block;
}



/**
 * Display the campaign Display preference screen
 * @param $prefs Display preferences
 * @return string $block html campaign form preferences to display
 */
function dis_campaign_display_pref($prefs) {
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "campaign");
  $dis_pref->pref_title = $GLOBALS['l_campaign'];
  $dis_pref->pref_dis_help = 1;

  // --- html template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}


/**
 * Display the campaign administration index
 * @return string $block html admin form to display
 */
function dis_admin_index() {

  $defaults_q = of_get_defaultodttemplates("campaign");
  $block = of_defaultodttemplates_dis_admin_form("campaign", $defaults_q);
  return $block;
}


function html_options_from_group_users($group_id, $unknown, $uids_arr, $initial_value) {
  global $c_undef, $l_undef;
  $users = of_usergroup_get_group_users($group_id, $unknown, $uids_arr);

  $options = '';

  if(is_array($users)) {
    $options .= "<option value=\"$c_undef\">$l_undef</option>\n";
    foreach ($users as $cid => $one_user) {
      $cname = $one_user['lastname'].' '.$one_user['firstname'];
      $options .= "<option value=\"$cid\"";
      // First time default to uid of connected user (uid) then set to name
      if ( ($initial_value == $cid)) {
        $options .= " selected = \"selected\"";
      }
      $options .= ">$cname</option>\n";
    }
  }
  return $options;
}
