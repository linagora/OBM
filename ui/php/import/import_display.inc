<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : import_display.php                                           //
//     - Desc : Import Display File                                          //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["import_name"] = $l_name;
$fieldnames["import_datasource"] = $l_datasource;


///////////////////////////////////////////////////////////////////////////////
// Display Import specific dataset fields                                    //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_import(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme;

  if (($fieldname == "import_name")  && $link_ok) {
    $res["url"] = "import_index.php?action=detailconsult&amp;param_import=".$OD->data_set->f("import_id");
  }
  
  // For contacts urls (link):
  else if ($fieldname == "contact_lastname") {
    $res["url"] = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$OD->data_set->f("contact_id");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import search Form
// Parameters : 
//   - $import[] : default form values
//     keys used : name
//   - $dsrc_q   : database object with datasource list
///////////////////////////////////////////////////////////////////////////////
function html_import_search_form ($import, $dsrc_q) {
  global $l_name, $l_datasource, $l_find, $l_none;

  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($import["name"]);
  $dsrc = stripslashes($import["datasource"]);

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"0\">$l_none</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_import\"
    action=\"".url_prepare("import_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_datasource<br />
      $sel_dsrc
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
 }


///////////////////////////////////////////////////////////////////////////////
// Display the Import search result
// Parameters:
//   - $import[] : list search criteria
//     keys used : name, subject, email
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_import_search_list($import, $popup=false) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "import");
  $obm_q = run_query_search($import, $new_order, $order_dir);
  $nb = $obm_q->num_rows();
  if ($nb == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_import_search_list($obm_q, $pref_q, $nb, $import, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Import search result
// Parameters : 
//   - $obm_q    : list of the imports to display 
//   - $pref_q   : fields that have to be displayed
//   - $nb       : nb imports returned by the search query 
//   - $import   : import search criteria
//     keys used : name, contact, ext_id, ext_url, ext_action
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_import_search_list($obm_q, $pref_q, $nb, $import, $popup) {
  global $display, $l_found, $l_add, $l_close;

  $name = urlencode(stripslashes($import["name"]));
  $contact = urlencode(stripslashes($list["contact"]));

  $display["msg"] .= display_info_msg("$nb $l_found");

  $url = url_prepare("import_index.php?action=search&amp;tf_name=$namet&amp;tf_contact=$contact");
  
  $od = new OBM_DISPLAY("DATA", $pref_q, "import");
  $od->data_set = $obm_q;
  $od->data_header = "both";
  $od->data_url = $url;
  $block .= $od->display("dis_data_import");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Import Consultation                                          //
// Parameters:
//   - $import_q : Import database result 
///////////////////////////////////////////////////////////////////////////////
function html_import_consult($import_q) {
  global $l_import, $l_name, $l_datasource, $l_file, $l_test_file, $l_csv_sep;
  global $ccsvd_sc, $ccsvd_tab, $l_csvd_sc, $l_csvd_tab;
  global $display, $perm, $action;

  $id = $import_q->f("import_id");
  $usercreate = $import_q->f("import_usercreate");
  $userupdate = $import_q->f("import_userupdate");
  $timecreate = $import_q->f("timecreate");
  $timeupdate = $import_q->f("timeupdate");
  $name = $import_q->f("import_name");
  $dsrc = $import_q->f("import_datasource_id");
  $datasource = $import_q->f("datasource_name");
  $format = $import_q->f("import_format");
  $desc = $import_q->f("import_desc");

  if ($csv_sep == $ccsvd_sc) $csvd_sc = "checked";
  if ($csv_sep == $ccsvd_tab) $csvd_tab = "checked";

  $display["detailInfo"] = display_record_info($import_q);
  $display["title"] = "<div class=\"title\">$l_import : $name</div>";

  $block = "
    <div class=\"detailHead\">$l_import</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_datasource</td>
      <td class=\"detailText\">$datasource</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_csv_sep</td>
      <td class=\"detailText\">
        <inpu type=\"radio\" name=\"rd_sep\" value=\"$ccsvd_sc\" $csvd_sc />$l_csvd_sc
        <inpu type=\"radio\" name=\"rd_sep\" value=\"$ccsvd_tab\" $csvd_tab />$l_csvd_tab
      </td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_file</div>
    <form method=\"post\" name=\"f_file action=\"$url\">
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_test_file</td>
      <td class=\"detailForm\">
        <input type=\"file\" name=\"fi_file\" size=\"30\" value=\"\" /></td>
    </tr>
    </table>";

  $block .= display_info_msg($message);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Import Form                                                       //
// Parameters :
//   - $action   : action called
//   - $import[] : default or transmitted values
//     keys used : name, subject, usercreate
//   - $import_q : DBO : information about the import (null for new import)
//   - $dsrc_q   : database object with datasource list
///////////////////////////////////////////////////////////////////////////////
function html_import_file_form($action, $import, $import_q, $dsrc_q) {
  global $display, $l_none, $l_insert, $l_update, $l_back;
  global $l_import, $l_name, $l_file, $l_datasource;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $import_q->f("import_id");
    $usercreate = $import_q->f("list_usercreate");
    $name = $import_q->f("import_name");
    $dsrc = $import_q->f("import_datasource_id");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($import["id"])) { $id = $import["id"]; }
  if (isset($import["name"])) { $name = stripslashes($import["name"]); }
  if (isset($import["datasource"])) { $dsrc = $import["datasource"]; }

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"0\">$l_none</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  $url = url_prepare("import_index.php");
  $display["title"] = "<div class=\"title\">$l_import : $name</div>";

  $block = "
  <form method=\"post\" name=\"f_new action=\"$url\"
    onSubmit=\"if (check_import_form(this)) return true; else return false;\">

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_name\" value=\"$name\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_datasource</td>
    <td class=\"detailForm\">$sel_dsrc</td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_file</td>
    <td class=\"detailForm\">
      <input type=\"file\" name=\"fi_file\" size=\"30\" value=\"\" /></td>
  </tr>
  </table>";

  if (($action == "detailupdate") || ($action == "update")) {
     $dis_but .= "
      <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

       $dis_back = "<form method=\"get\" name=\"f_back\"
        action=\"".url_prepare("import_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>";
   } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<div class=\"detailButton\">
    <p class=\"detailButtons\">
    $dis_but
    </p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a list deletion                                    //
// We ask confirmation or cancel
// Parameters:
//   - $id : list id
/////////////////////////////////////////////////////////////////////////////
function dis_warn_delete($id) {
  global $display, $l_warn_delete, $l_delete, $l_back;

  $display["msg"] .= display_warn_msg("$l_warn_delete");

  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <form method=\"get\" name=\"form_delete\"
          action=\"" .url_prepare("import_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"delete\" />
        <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
        </form>
      </p>
      <p class=\"detailButtons\">
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("import_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
        <input type=\"hidden\" name=\"param_import\" value=\"$id\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
      </p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about an import insertion or update                   //
// When similar imports exist we show these and ask confirmation
// Parameters:
//   - $import_q : import database result (at least 1 row)
//   - $import[] : values for insertion/update (if confirmation)
//     keys used : name, subject
/////////////////////////////////////////////////////////////////////////////
function dis_import_warn_insert($import_q, $import) {
  global $l_check_sameimport, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  $name = $import["name"];
  $dsrc = $import["datasource"];

  $display["msg"] .= display_warn_msg($l_check_sameimport);
  while ($import_q->next_record()) {
    $id = $import_q->f("import_id");
    $samename = $import_q->f("import_name");
    $samedsrc = $import_q->f("datasource_name");
    $dis_same_import .= "
      <tr><td class=\"detailLabel\">
        <a class=\"detail\" href=\"" .url_prepare("import_index.php?action=detailconsult&amp;param_import=$id") . "\">$samename ($samedsrc)</a>
      </td></tr>";
  }

  $block = "<p />
  <table class=\"detail\">
  $dis_same_import
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("import_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back method=\"post\"
      action=\"" .url_prepare("import_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
      <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

  return $block;
}


</script>
