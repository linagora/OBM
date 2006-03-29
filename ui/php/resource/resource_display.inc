<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_display.inc                                             //
//     - Desc : User Display functions File                                  //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["resource_name"] = $l_name;
$fieldnames["resource_description"] = $l_desc; // description 
$fieldnames["resource_qty"] = $l_qty; // quantity 
$fieldnames["resource_id"] = $l_update;


///////////////////////////////////////////////////////////////////////////////
// Display resource specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_resource(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ext_element;

  if (($fieldname == "resource_name") && $link_ok) {
    $res["url"] = "$path/resource/resource_index.php?action=detailconsult&amp;param_resource=".$OD->data_set->f("resource_id");
  }
  if (($fieldname == "data_element") && $ext_element != "") {
    $res["name"] = "<span id=\"data-resource-".$OD->data_set->f("resource_id")."\" style=\"display:none;\">".
    $OD->data_set->f("resource_name").
    "</span>";
  }
  else if ($fieldname == "resource_id") {
    $t_id = $OD->data_set->f("resource_id");
    $res["url"] = "$path/resource/resource_index.php?action=detailupdate&amp;param_resource=$t_id";
    $res["name"] = "<img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/modify.png\" alt=\"\" />";
    $res["txt_name"] = " ";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display resource search form
// Parameters:
//   - $resource[]   : default form values
//     keys used : name, desc, qty, popup
///////////////////////////////////////////////////////////////////////////////
function html_resource_search_form($resource) {
  global $l_name, $l_desc, $l_qty, $l_find, $l_all;
  global $l_add_resources;
  global $display, $c_all;

  $lname = $resource["name"];
  $ldesc = stripslashes($resource["desc"]);
  $qtyinf = $resource["qtyinf"];
  $qtysup = $resource["qtysup"];
  $popup = $resource["popup"];
  if ($resource["restriction"]) {
    $dis_restriction = "<input name=\"restriction\" type=\"hidden\" value=\"user\">";
  }

  if ($popup) {
    $ext_action = $resource["ext_action"];
    $ext_title = stripslashes($resource["ext_title"]);
    $ext_url = $resource["ext_url"];
    $ext_id = $resource["ext_id"];
    $ext_target = $resource["ext_target"];
    $ext_widget = $resource["ext_widget"];
    $ext_element =  $resource["ext_element"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
	    <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == "") {
      $ext_title = $l_add_resources;
    }
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("resource_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"30\" maxlength=\"120\"
      value=\"$lname\" />
    </div>
    <div class=\"searchLabel\">$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"30\" maxlength=\"32\"
      value=\"$ldesc\" />
    </div>
    <div class=\"searchLabel\">&nbsp;&nbsp;<br />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input type=\"text\" name=\"tf_qtyinf\" size=\"4\" maxlength=\"8\" value=\"$qtyinf\" /> 
    </div>
    <div class=\"searchLabel\"><br />
      &nbsp;&lt;=&nbsp;$l_qty&nbsp;&lt;=&nbsp; 
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input type=\"text\" name=\"tf_qtysup\" size=\"4\" maxlength=\"8\" value=\"$qtysup\" /> 
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\">
      <input name=\"submit\" type=\"submit\" value=\"$l_find\">
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $dis_restriction
      $ext&nbsp;
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the reseource search result
// Parameters:
//   - $resource[]   : resource search criteria
//     keys used : name, desc 
///////////////////////////////////////////////////////////////////////////////
function dis_resource_search_list($resource) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $popup = $resource["popup"];
  $restriction = $resource["restriction"];

  $prefs = get_display_pref($auth->auth["uid"], "resource");
  $obm_q = run_query_resource_search($resource, $new_order, $order_dir, $restriction);
  $nb_resource = $obm_q->num_rows_total();
  if ($nb_resource == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_resource_search_list($obm_q, $prefs, $nb_resource, $resource, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Resource Search result
// Parameters:
//   - $obm_q    : database result (resource list)
//   - $prefs   : the fields which have to be displayed
//   - $nb_resource  : nb resources returned by the search query
//   - $resource[]   : resource search criteria
//     keys used : name, desc 
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_resource_search_list($obm_q, $prefs, $nb_resource, $resource, $popup) {
  global $display, $l_found, $l_close, $l_add;

  $lname = urlencode($resource["name"]);
  $ldesc = urlencode($resource["desc"]);
  $lqtyinf = urlencode($resource["qtyinf"]);
  $lqtysup = urlencode($resource["qtysup"]);

  if ($popup) {
    $ext_action = $resource["ext_action"];
    $ext_url = $resource["ext_url"];
    $ext_id = $resource["ext_id"];
    $ext_target = $resource["ext_target"];
    $ext_widget = $resource["ext_widget"];
    $ext_element =  $resource["ext_element"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget";
  }

  $url = url_prepare("resource_index.php?action=search&amp;tf_name=$lname&amp;tf_desc=$ldesc&amp;tf_qtyinf=$lqtyinf&amp;tf_qtysup=$lqtysup$url_ext");

  $resource_d = new OBM_DISPLAY("DATA", $prefs, "resource");
  if ($popup) {
    $resource_d->display_link = false;
    $resource_d->data_cb_text = "X";
    $resource_d->data_idfield = "resource_id";
    $resource_d->data_cb_name = "cb_u";
    if ($ext_widget != "") {
      $resource_d->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } elseif ($ext_element != "") {
      $resource_d->data_cb_name = "";
      $resource_d->data_form_head = "
      <form onsubmit=\"of_extmod_fill_ext_element(this); return false;\">";
    } else {
      $resource_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }
    $resource_d->data_form_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>";

    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $resource_d->data_set = $obm_q;
  $resource_d->data_header = "both";
  $resource_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $display["msg"] .= display_info_msg("$nb_resource $l_found");
  $block .= $resource_d->display("dis_data_resource");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Resource Form
// Parameters:
//   - $usr_q     : resource database result
//   - $resource  : default values or updated values (if error)
//     kes used   : id, name, passwd, perms, email
///////////////////////////////////////////////////////////////////////////////
function html_resource_form($usr_q, $resource) {
  global $l_name, $l_desc, $l_qty;
  global $l_resource, $l_insert, $l_update;
  global $action;

  // if update mode and first time value are taken from database
  if ($action == "detailupdate") {
    $id = $usr_q->f("resource_id");
    $name = $usr_q->f("resource_name");
    $desc = $usr_q->f("resource_description");
    $qty = $usr_q->f("resource_qty");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($resource["id"])) { $id = $resource["id"]; }
  if (isset($resource["name"])) { $name = $resource["name"]; }
  if (isset($resource["qty"])) { $qty = $resource["qty"]; }

  // Buttons
  if (($action == "new") || ($action == "insert")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\">
          <input type=\"submit\" value=\"$l_insert\">";

  } elseif (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\">
          <input type=\"hidden\" name=\"param_resource\" value=\"$id\">
          <input type=\"submit\" value=\"$l_update\">";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_resource(this)) return true; else return false;\"
      action=\"" . url_prepare("resource_index.php") . "\">

    <div class=\"detailHead\">$l_resource</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input id=\"tf_name\" name=\"tf_name\" size=\"40\" maxlength=\"120\" value=\"$name\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_qty</td>
      <td class=\"detailForm\"><input id=\"tf_qty\" name=\"tf_qty\" size=\"4\" maxlength=\"8\" value=\"$qty\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_desc</td>
      <td class=\"detailForm\"><textarea id=\"ta_desc\" name=\"ta_desc\" rows=\"3\" cols=\"80\">$desc</textarea></td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Resource Consultation
// Parameters:
//   - $resource : resource database result
///////////////////////////////////////////////////////////////////////////////
function dis_resource_consult($resource) {
  global $display, $l_query_error;

  $id = $resource["id"];

  $obm_q = run_query_resource_detail($id);
  if ($obm_q->num_rows() == 1) {
    $block = html_resource_consult($obm_q);
  } else {
    $display["msg"] .= display_err_msg($l_query_error);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Resoiurce Consultation
// Parameters:
//   - $obm_q : Resource database result
///////////////////////////////////////////////////////////////////////////////
function html_resource_consult($obm_q) {
  global $l_resource, $l_name, $l_desc, $l_qty;
  global $path, $display, $c_yes, $c_no;

  $id = $obm_q->f("resource_id");
  $name = $obm_q->f("resource_name");
  $desc = $obm_q->f("resource_description");
  $qty = $obm_q->f("resource_qty");

  $display["detailInfo"] = display_record_info($obm_q);
  $display["title"] = "<div class=\"title\">$l_resource : $name</div>";

  $block = "
  <div class=\"detailHead\">$l_resource</div>
 
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_qty</td>
      <td class=\"detailText\">$qty</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_desc</td>
      <td class=\"detailText\">$desc</td>
    </tr>
    </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the resource can be deleted, and the form
// Parameters:
//   - $p_id : resource id
///////////////////////////////////////////////////////////////////////////////
function dis_resource_can_delete($p_id) {
  global $display, $l_delete, $l_can_delete, $l_back;

  $url = url_prepare("resource_index.php");

  $dis_back = "<form name=\"form_back\" method=\"get\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_resource\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_resource\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);
  $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_delete</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Resource Display preference screen
// Parameters:
//   - $prefs : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_resource_display_pref($prefs) {
  global $l_resource_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "resource");
  $dis_pref->pref_title = $l_resource_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


?>