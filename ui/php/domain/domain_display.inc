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
$fieldnames["domain_archive"] = $l_archive;
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
  global $path, $set_theme, $ico_mail,$ext_element;

  if (($fieldname == "domain_label") && $link_ok) {
    $res["url"] = "$path/domain/domain_index.php?action=detailconsult&amp;domain_id=".$OD->data_set->f("domain_id");
  }  else if ($fieldname == "domain_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
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
  global $l_label, $l_desc, $l_archive;
  global $l_find;

  $label = $domain["label"];
  $desc = $domain["desc"];
  $archive = $domain["archive"];
  
  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("domain_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_label<br />
      <input type=\"text\" name=\"tf_label\" size=\"16\" maxlength=\"32\"
      value=\"$label\" />
    </div>
    <div class=\"searchLabel\">$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"16\" maxlength=\"32\"
      value=\"$desc\" />
    </div>
    <div class=\"searchLabel\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\">
      <input name=\"submit\" type=\"submit\" value=\"$l_find\">
    </div>
    <div class=\"searchEnd\"></div>
  </div>
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
  global $display, $uid, $new_order, $order_dir;

  $obm_q = run_query_domain_search($domain, $new_order, $order_dir, $restriction);

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

  $label = urlencode($domain["label"]);
  $desc = urlencode($domain["desc"]);


  $url = url_prepare("domain_index.php?action=search&amp;tf_label=$label&amp;tf_desc=$desc&amp;cb_archive=$archive$url_ext");

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
  global $l_label, $l_desc, $l_domain, $l_archive, $l_insert, $l_update;
  global $c_cr;

  // if update mode and first time value are taken from database
  if ($domain["action"] == "detailupdate") {
    $id = $dom_q->f("domain_id");
    $label = $dom_q->f("domain_label");
    $desc = $dom_q->f("domain_description");
    $archive = ($dom_q->f("domain_archive") == "1" ? " checked" : "");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($domain["domain_id"])) { $id = $domain["domain_id"]; }
  if (isset($domain["label"])) { $label = $domain["label"]; }
  if (isset($domain["desc"])) { $desc = $domain["desc"]; }
  if (isset($domain["archive"])) { $archive = ($domain["archive"] == 1 ? "checked" : ""); }

  // Buttons
  if (($domain["action"] == "new") || ($domain["action"] == "insert")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\">
          <input type=\"submit\" value=\"$l_insert\">";

  } elseif (($domain["action"]=="detailupdate") || ($domain["action"]=="update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\">
          <input type=\"hidden\" name=\"domain_id\" value=\"$id\">
          <input type=\"submit\" value=\"$l_update\">";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"POST\" name=\"f_entity\"
      onsubmit=\"if (check_domain(this)) return true; else return false;\"
      action=\"" . url_prepare("domain_index.php") . "\">

    <div class=\"detailHead\">$l_domain</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailForm\"><input name=\"tf_label\" size=\"24\" maxlength=\"32\" value=\"$label\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_desc</td>
      <td class=\"detailForm\"><input name=\"tf_desc\" size=\"48\" maxlength=\"255\" value=\"$desc\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input name=\"cb_archive\" type=\"checkbox\" value=\"1\" $archive /></td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
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
  global $l_label, $l_archive, $l_domain, $l_desc;
  global $path, $display, $c_yes, $c_no;

  $id = $obm_q->f("domain_id");
  $label = $obm_q->f("domain_label");
  $desc = $obm_q->f("domain_description");
  $archive = ($obm_q->f("domain_archive") == 1 ? $c_yes : $c_no);
  

  $display["detailInfo"] = display_record_info($obm_q);
  $display["title"] = "<div class=\"title\">$l_domain : $label </div>";

  $block = "
  <div class=\"detailHead\">$l_domain</div>
 
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$archive</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailText\">$label</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_desc</td>
      <td class=\"detailText\">$desc</td>
    </tr>
    </table>

    $dis_group
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
  global $l_no_domain, $l_check_domaincreate, $l_check_domainupdate;
  global $display, $l_domain, $l_infos;

  $url = url_prepare("domain_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"domain_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"domain_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  // Display entities where the domain is referenced
  $obm_q = new DB_OBM;
  $tables = $obm_q->table_names();
  $i = 0;
  while ($tables[$i]["table_name"]) {
    $table = $tables[$i]["table_name"];

    // We exclude POSTGRES meta table (sql_xxxx)
    if (! (substr($table, 0, 4) == "sql_")) {
      $domaincreate = $table . "_domaincreate";
      $domainupdate = $table . "_domainupdate";

      $obm_qm = new DB_OBM;
      $fields = $obm_qm->metadata($table, false);
      $fieldexist_uc = false;
      $fieldexist_uu = false;
      $j = 0;
      while ($fields[$j]["name"]) {
	if (strtoupper($fields[$j]["name"]) == strtoupper($domaincreate)) {
	  $fieldexist_uc = true;
	}
	if (strtoupper($fields[$j]["name"]) == strtoupper($domainupdate)) {
	  $fieldexist_uu = true;
	}
	$j++;
      }
      if ($fieldexist_uc) {
	$query = "SELECT count(*) FROM $table WHERE $domaincreate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbc = $c_q->f("0");
      }
      if ($fieldexist_uu) {
	$query = "SELECT count(*) FROM $table WHERE $domainupdate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbu = $c_q->f("0");
      }
      if ($fieldexist_uc || $fieldexist_uu) {
	$dis_domain .= "
      <tr>
        <td class=\"detailHead\" rowspan=\"2\">$table</td>
        <td class=\"detailText\"><b>$nbc</b> $l_check_domaincreate</td>
      </tr><tr>
        <td class=\"detailText\"><b>$nbu</b> $l_check_domainupdate</td>
      </tr>";
      } else {
	$dis_domain .= "
      <tr>
        <td class=\"detailHead\">$table</td>
        <td class=\"detailText\">$l_no_domain</td>
      </tr>";
      }
      $dis_domain .= "<tr>
      <td colspan=\"2\"><hr /></td>
    </tr>";
    }
    $i++;
  }
 
  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_delete</p>
    <p class=\"detailButtons\">$dis_back</p>
    </div>
    <br />
    <div class=\"detailHead\">$l_domain : $l_infos</div>

    <table class=\"detail\">
      $dis_domain
    </table>";

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