<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : domain_display.inc                                           //
//     - Desc : domain Display functions File                                //
// 2000-05-17 Phan David                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["domain_label"] = $l_label;
$fieldnames["domain_description"] = $l_desc;
$fieldnames["domain_name"] = $l_domain_name;
$fieldnames["domain_alias"] = $l_alias;
$fieldnames["domain_timecreate"] = $l_timecreate;


///////////////////////////////////////////////////////////////////////////////
// Display domain specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_domain_data(&$OD, $fieldname, $link_ok) {
  global $path, $ico_mail;

  if (($fieldname == "domain_label") && $link_ok) {
    $res["url"] = "$path/domain/domain_index.php?action=detailconsult&amp;domain_id=".$OD->data_set->f("domain_id");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display domain search form
// Parameters:
//   - $domain[]   : default form values
//     keys used : login, lastname, perms, archive, popup
///////////////////////////////////////////////////////////////////////////////
function html_domain_search_form($domain) {
  global $l_text, $l_find;

  $text = $domain["text"];
  
  $block = "
  <form method=\"get\" name=\"f_search\" class=\"search\" action=\"". url_prepare("domain_index.php")."\">

    <label>$l_text<br />
      <input type=\"text\" name=\"tf_text\" size=\"16\" maxlength=\"32\"
      value=\"$label\" />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the domain search result
// Parameters:
//   - $domain[]   : domain search criteria
//     keys used : name, perms
///////////////////////////////////////////////////////////////////////////////
function dis_domain_search_list($domain) {
  global $l_found, $l_no_found;
  global $display, $uid;

  $obm_q = run_query_domain_search($domain);

  $prefs = get_display_pref($uid, "domain");
  $nb_domain = $obm_q->num_rows_total();
  if ($nb_domain == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_domain $l_found");
    $block = html_domain_search_list($obm_q, $prefs, $domain, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the domain Search result
// Parameters:
//   - $obm_q    : database result (domain list)
//   - $prefs    : the fields which have to be displayed
//   - $domain[]   : domain search criteria
//     keys used : login, lastname, pemrs
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_domain_search_list($obm_q, $prefs, $domain, $popup) {
  global $l_close, $l_add;

  $text = urlencode($domain["text"]);

  $url = url_prepare("domain_index.php?action=search&amp;tf_text=$text");

  $domain_d = new OBM_DISPLAY("DATA", $prefs, "domain");
  $domain_d->data_set = $obm_q;
  $domain_d->data_header = "both";
  $domain_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $domain_d->display("dis_domain_data");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display domain Form
// Parameters:
//   - $usr_q     : domain database result
//   - $domain      : default values or updated values (if error)
//     kes used   : id, name, passwd, perms, email
///////////////////////////////////////////////////////////////////////////////
function html_domain_form($dom_q, $domain) {
  global $l_label, $l_desc, $l_domain, $l_archive, $l_domain_name, $l_alias;
  global $l_insert, $l_update;
  global $display;

  // if update mode and first time value are taken from database
  if ($domain["action"] == "detailupdate") {
    $id = $dom_q->f("domain_id");
    $label = $dom_q->f("domain_label");
    $desc = $dom_q->f("domain_description");
    $domain_name = $dom_q->f("domain_name");
    $alias = $dom_q->f("domain_alias");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($domain["domain_id"])) { $id = $domain["domain_id"]; }
  if (isset($domain["label"])) { $label = $domain["label"]; }
  if (isset($domain["desc"])) { $desc = $domain["desc"]; }
  if (isset($domain["domain_name"])) { $domain_name = $domain["domain_name"]; }
  if (isset($domain["alias"])) { $alias = $domain["alias"]; }

  // Buttons
  if (($domain["action"] == "new") || ($domain["action"] == "insert")) {
    $dis_domain_name = "<input name=\"tf_domain_name\" size=\"48\" maxlength=\"128\" value=\"$domain_name\" />";
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
          <input type=\"submit\" value=\"$l_insert\" />";

  } elseif (($domain["action"]=="detailupdate") || ($domain["action"]=="update")) {
    $dis_domain_name = $domain_name;
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\">
          <input type=\"hidden\" name=\"domain_id\" value=\"$id\">
          <input type=\"submit\" value=\"$l_update\">";
  }

  $display["title"] = "<div class=\"title\">$l_domain : $label </div>";

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_domain(this)) return true; else return false;\"
      action=\"" . url_prepare("domain_index.php") . "\">

    <fieldset class=\"detail infos\">
    <legend>$l_domain</legend>
    <table>
    <tr>
      <th>$l_label</th>
      <td><input name=\"tf_label\" size=\"24\" maxlength=\"32\" value=\"$label\" /></td>
    </tr>
    <tr>
      <th>$l_desc</th>
      <td><input name=\"tf_desc\" size=\"48\" maxlength=\"255\" value=\"$desc\" /></td>
    </tr>
    <tr>
      <th>$l_domain_name</th>
      <td>$dis_domain_name</td>
    </tr>
    </table>
    </fieldset>

     <fieldset class=\"detail infos\">
    <legend>$l_alias</legend>
    <table>
    <tr>
      <th>$l_alias</th>
      <td><textarea name=\"ta_alias\" rows=\"6\" cols=\"48\">$alias</textarea></td>
    </tr>
    </table>
    </fieldset>

    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display domain Consultation
// Parameters:
//   - $domain : domain database result
///////////////////////////////////////////////////////////////////////////////
function dis_domain_consult($domain) {
  global $display, $l_query_error;

  $id = $domain["domain_id"];

  $obm_q = run_query_domain_detail($id);
  if ($obm_q->num_rows() == 1) {
    $block = html_domain_consult($obm_q);
  } else {
    $display["msg"] .= display_err_msg($l_query_error);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display domain Consultation
// Parameters:
//   - $obm_q : domain database result
///////////////////////////////////////////////////////////////////////////////
function html_domain_consult($obm_q) {
  global $l_label, $l_domain, $l_desc, $l_domain_name, $l_alias;
  global $display;

  $id = $obm_q->f("domain_id");
  $label = $obm_q->f("domain_label");
  $desc = $obm_q->f("domain_description");
  $domain_name = $obm_q->f("domain_name");
  $alias = $obm_q->f("domain_alias");

  $display["detailInfo"] = display_record_info($obm_q);
  $display["title"] = "<h1 class=\"title\">$l_domain : $label </h1>";

  $block = "
  <div class=\"detail infos\">
  <h1>$l_domain</h1>
    <table>
    <tr>
      <th>$l_label</th>
      <td>$label</td>
    </tr>
    <tr>
      <th>$l_desc</th>
      <td>$desc</td>
    </tr>
    <tr>
      <th>$l_domain_name</th>
      <td>$domain_name</td>
    </tr>
    </table>
    </div>

  <div class=\"detail infos\">
  <h1>$l_alias</h1>
  <table>
    <tr>
      <th>$l_alias</th>
      <td>$alias</td>
    </tr>
    </table>
    </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the domain delete validation screen
// Parameters:
//   - $p_id : domain id
///////////////////////////////////////////////////////////////////////////////
function dis_domain_can_delete($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display, $l_domain;

  $url = url_prepare("domain_index.php");

  $dis_back = "<a href=\"$path/domain/domain_index.php?action=detailconsult&amp;domain_id=$p_id\">$l_back</a>";

  $dis_delete = "
  <a href=\"$path/domain/domain_index.php?action=delete&amp;domain_id=$p_id\" onclick=\"return confirm_del(this.form);\">
  $l_delete 
  </a>";

  $infos_q = run_query_domain_detail($p_id);
  $title = $infos_q->f("domain_label");
  $display["title"] = "<div class=\"title\">$l_domain : $title</div>";
  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"buttons\">
      $dis_delete
      $dis_back
    </div>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the domain Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_domain_display_pref($prefs) {
  global $l_domain_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "domain");
  $dis_pref->pref_title = $l_domain_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


?>
