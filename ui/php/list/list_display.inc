<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_display.php                                             //
//     - Desc : List Display File                                            //
// 2000-01-20 Vincent MARGUERIT                                              //
// 2001-08-23 jlb : add list_comment, add export for letter mailing
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["list_name"] = $l_name;
$fieldnames["list_subject"] = $l_subject;
$fieldnames["list_email"] = $l_email;
$fieldnames["usercreate"] = $l_creator;
$fieldnames["userupdate"] = $l_updater;
$fieldnames["timecreate"] = $l_date_creation;
$fieldnames["timeupdate"] = $l_date_last_update;
// Calculated fields
$fieldnames["list_nb_contact"] = $l_nb_contact;
$fieldnames["list_contact_lastname"] = $l_lastname;
$fieldnames["list_contact_firstname"] = $l_firstname;
$fieldnames["list_contact_function"] = $l_function;
$fieldnames["list_contact_company"] = $l_company;
$fieldnames["list_contact_town"] = $l_town;
$fieldnames["list_contact_phone"] = $l_phone;
$fieldnames["list_contact_mobilephone"] = $l_hphone;
$fieldnames["list_contact_email"] = $l_email;


///////////////////////////////////////////////////////////////////////////////
// Display List search Form                                                  //
// Parameters : 
//   - $list[]        : default form values
//     keys used      : name, contact
///////////////////////////////////////////////////////////////////////////////
function html_list_search_form ($list) {
  global $l_name, $l_list_contact, $l_email, $l_find;
  global $popup;

  if ($popup) {
    $title = $list["title"];
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $ext_target = $list["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\" />
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\" />
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\" />";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($list["name"]);
  $email = stripslashes($list["email"]);
  $contact = stripslashes($list["contact"]);

  // --- HTML Page display ----------------------------------------------------

  echo "
    <form method=\"get\" name=\"f_list\"
      action=\"".url_prepare("list_index.php")."\">
      <table class=\"details\">
      <tr>
        <td class=\"textLabelForm\">$l_name
          <br />
          <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
        </td>
        <td>&nbsp;&nbsp;</td>
        <td class=\"textLabelForm\">$l_list_contact
          <br />
          <input type=\"text\" name=\"tf_contact\" size=\"16\"
            value=\"$contact\" />
        </td>
        <td>&nbsp;&nbsp;</td>
        <td class=\"textLabelForm\">$l_email
          <br />
          <input type=\"text\" name=\"tf_email\" size=\"20\" value=\"$email\" />
        </td>
        <td class=\"textLabelForm\">&nbsp;&nbsp;
          <input name=\"action\" type=\"hidden\" value=\"search\" />
          <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
          <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
          $ext
        </td>
      </tr>
      </table>
    </form><hr />";
 }


///////////////////////////////////////////////////////////////////////////////
// Display the List search result                                            //
// Parameters:
//   - $list[]    : list search criteria
//     keys used  : name, subject, email
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_list_search_list($list, $popup=false) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "list");
  $obm_q = run_query_search($list, $new_order, $order_dir);
  $nb_list = $obm_q->num_rows();
  if ($nb_list == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_list_search_list($obm_q, $pref_q, $nb_list, $list, $popup);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the List search result                                       //
// Parameters : 
//   - $obm_q    : list of the lists to display 
//   - $pref_q   : fields that have to be displayed
//   - $nb_list  : nb lists returned by the search query 
//   - $list[ ]  : list search criteria
//     keys used : name, contact, ext_id, ext_url, ext_action
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_list_search_list($obm_q, $pref_q, $nb_list, $list, $popup) {
  global $l_found, $l_add, $l_close;

  if ($popup) {
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $ext_target = $list["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id";
  }

  $name = urlencode(stripslashes($list["name"]));
  $contact = urlencode(stripslashes($list["contact"]));

  display_info_msg("$nb_list $l_found");

  $url = url_prepare("list_index.php?action=search&amp;tf_name=$namet&amp;tf_contact=$contact$url_ext");
  
  $dis_list = new OBM_DISPLAY("DATA",$pref_q);
  if ($popup) {
    $dis_list->display_link = false;
    $dis_list->data_cb_text = "X";
    $dis_list->data_idfield = "list_id";
    $dis_list->data_cb_name = "cb_list";
    echo "<form target=\"$ext_target\" method=\"get\" action=\"$ext_url\">";
  }
  $dis_list->data_set = $obm_q;
  $dis_list->data_header = "both";
  $dis_list->data_url = $url;
  $dis_list->display();

  if ($popup) {
    echo "<input type=\"submit\" value=\"$l_add\" /> 
      <input type=\"hidden\" name=\"param_list\" value=\"$ext_id\" /> 
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" /> 
      </form>
      <p align=right>
      <a href=\"\" onclick='window.close();'>$l_close</a>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display List Consultation                                            //
// Parameters:
//   - $list_q : company database result 
//   - $con_q  : contact database result 
///////////////////////////////////////////////////////////////////////////////
function html_list_consult($list_q, $pref_con_q, $con_q) {
  //-- Label
  global $l_name, $l_subject, $l_email, $l_list_infos, $l_no_contact, $l_list_infos_short;
  global $perm, $action;
  global $l_del_contact_sel, $l_con_found, $nb_contact_added, $nb_contact_deleted;

  $id = $list_q->f("list_id");
  $usercreate = $list_q->f("list_usercreate");
  $userupdate = $list_q->f("list_userupdate");
  $timecreate = $list_q->f("timecreate");
  $timeupdate = $list_q->f("timeupdate");
  $name = $list_q->f("list_name");
  $subject = $list_q->f("list_subject");
  $email = $list_q->f("list_email");

  display_record_info($usercreate, $userupdate, $timecreate, $timeupdate);

  echo "
    <table class=\"details\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_subject</td>
      <td class=\"detailText\">$subject</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$email</td>
    </tr>
    </table>";

  $nb_con = $con_q->num_rows();
  if ($nb_con == 0) {
    $message = $l_no_contact;
  } else {
    $message = "$nb_con $l_con_found";
  }

  display_info_msg($message);

  if ($nb_con != 0) {

    $url= url_prepare("list_index.php?action=detailconsult&amp;param_list=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $pref_con_q);
    $dis_infos->data_set = $con_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";

    if ($perm->have_perm("editor")) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "list_contact_id";
      $dis_infos->data_cb_name = "cb_con";
      $dis_infos->data_cb_field = "";
      echo "<form method=\"get\" action=\"list_index.php\">";
    }

    $dis_infos->display();

    if ($perm->have_perm("editor")) {
      echo "
      <table class=\"details\">
      <tr>
        <td>
        <input type=\"submit\" value=\"$l_del_contact_sel\" />
        <input type=\"hidden\" name=\"param_list\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"contact_del\" />
        </td>
      </tr>
      </table>
      </form>";
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display List Form                                                         //
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_list_form($action, $list_q, $list) {
  global $l_name, $l_subject, $l_email, $l_insert, $l_update, $l_back;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $list_q->f("list_id");
    $usercreate = $list_q->f("list_usercreate");
    $name = $list_q->f("list_name");
    $subject = $list_q->f("list_subject");
    $email = $list_q->f("list_email");
  } else {
    // Values are taken from parameters
    $id = $list["id"];
    $name = stripslashes($list["name"]);
    $subject = stripslashes($list["subject"]);
    $email = stripslashes($list["email"]);
  }
  $url = url_prepare("list_index.php");

  echo "
  <form method=\"get\" name=\"f_new action=\"$url\"
    onSubmit=\"if (check_name_list(this)) return true; else return false;\">
  <table class=\"details\"=0>
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailText\">
      <input type=\"text\" name=\"tf_name\" value=\"$name\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_subject</td>
    <td class=\"detailText\">
      <input name=\"tf_subject\" size=\"30\" value=\"$subject\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_email>
    <td class=\"detailText\">
      <input name=\"tf_email\" size=\"30\" value=\"$email\" /></td>
  </tr>
  <tr>
    <td class=\"detailHead\" colspan=\"2\">
          <br />";

  if (($action == "detailupdate") || ($action == "update")) {
     echo "<table><tr><td>
      <input type=\"hidden\" name=\"param_list\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />
      </form>
      </td><td>
 
      <form method=\"get\" name=\"f_back\"
        action=\"".url_prepare("list_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"param_list\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
      </td></tr></table>";
   } else {
    echo "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />
      </form>";
  }

  echo "
    </td>
  </tr>
  </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the List Display preference screen                                //
// Parameters:
//   - $pref_list_q    : DBO : List Field list to display
//   - $pref_contact_q : DBO : Contact Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_list_display_pref($pref_list_q, $pref_contact_q) {
  global $l_list_display, $l_contact_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_list_q);
  $dis_pref->display_entity = "list"; 
  $dis_pref->pref_title = $l_list_display;
  $dis_pref->pref_dis_help = 0;

  echo "<center>
    <table><tr><td>";
  $dis_pref->display();

  $dis_pref->display_pref = $pref_contact_q;
  $dis_pref->display_module = "list";
  $dis_pref->display_entity = "list_contact";
  $dis_pref->pref_title = $l_contact_display;

  echo "</td><td>
    ";
  $dis_pref->display();

  echo "</td></tr></table>";
  $dis_pref->dis_pref_help();
  echo "</center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a list deletion                                    //
// We ask confirmation or cancel
// Parameters:
//   - $l_id : list id
/////////////////////////////////////////////////////////////////////////////
function dis_warn_delete($l_id) {
  global $l_warn_delete, $l_delete, $l_back;

  $nb_con = get_list_nb_contact($l_id);
  echo "<p align=\"center\" />$nb_con $l_warn_delete</p>

    <table class=\"details\">
    <tr>
      <td class=\"detailHead\">
        <form method=\"get\" name=\"form_delete\"
          action=\"" .url_prepare("list_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"delete\" />
        <input type=\"hidden\" name=\"hd_list_id\" value=\"$l_id\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
        </form>
      </td><td class=\"detailHead\">
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("list_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
        <input type=\"hidden\" name=\"param_list\" value=\"$l_id\" />
        <input type=\"submit\" VALUE=\"$l_back\" />
        </form>
      </td>
    </tr>
    </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a list insertion or update                      //
// When similar lists exist we show these and ask confirmation
// Parameters:
//   - $l_id     : list id
//   - $l_q      : list database result (at least 1 row)
//   - $list[]   : values for insertion/update (if confirmation)
//     keys used : name, subject
/////////////////////////////////////////////////////////////////////////////
function dis_list_warn_insert($l_id, $l_q, $list) {
  global $l_check_samelist, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = $list["name"];
  $subject = $list["subject"];

  echo $l_check_samelist;
  while ($l_q->next_record()) {
    $id = $l_q->f("list_id");
    $samename = $l_q->f("list_name");
    $samesubject = $l_q->f("list_subject");
    echo "<br /><a href=\"" .url_prepare("list_index.php?action=detailconsult&amp;param_list=$id") . "\">$samename ($samesubject)</a>";
  }

  echo "<p />
    <table class=\"details\">
    <tr>
      <td>
        <form method=\"post\" name=\"form_insert\"
          action=\"" .url_prepare("list_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"insert\" />
        <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
        <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
        <input type=\"hidden\" name=\"tf_subject\" value=\"$subject\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
        </form>
      </td><td>
        <form name=\"form_back method=\"post\"
          action=\"" .url_prepare("list_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"new\" />
        <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
        <input type=\"hidden\" name=\"tf_subject\" value=\"$subject\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
      </td>
    </tr>
    </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the export format form                                            //
// Parameters:
//   - $list[]    : list search criteria
//     keys used  : name, subject, email
///////////////////////////////////////////////////////////////////////////////
function dis_export_form($list) {
  global $c_export_format, $l_no_found, $l_list_set, $l_export_format;
  global $l_export_intro, $l_name, $l_desc, $l_help;
  global $set_theme, $ses_list;

  display_info_msg($l_export_intro);

  $nb_list = $list["list_nb"];
  $cpt = 1;
  $d_list = "
    <table class=\"details\">
    <tr>
      <td class=\"detailHead\">$l_list_set</td>
    </tr>";
  $param_list = "&amp;list=";
  $first = true;
  while ( list( $key, $val ) = each( $ses_list ) ) {
    if ($first == true) {
      $param_list .= $val;
    } else {
      $param_list .= ",$val";
    }
    $first = false;
    $name = get_list_name($val);
    $d_list .= "
    <tr>
      <td class=\"detailText\">
        <a href=\"list_index.php?action=detailconsult&amp;param_list=$val\">$name</a></td>
    </tr>";
  }

  $d_list .= "</table>";

  $cpt = 0;
  $d_format = "
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\" colspan=\"4\">$l_export_format</td>
    </tr><tr>
      <td class=\"adminHead\" colspan=\"2\">$l_name</td>
      <td class=\"adminHead\">$l_desc</td>
      <td class=\"adminHead\">$l_help</td>
    </tr>";
  while ($c_export_format[$cpt]["name"]) {
    $name = $c_export_format[$cpt]["name"];
    $info = $c_export_format[$cpt]["info"];
    $url = url_prepare($c_export_format[$cpt]["url"] . $param_list);
    $icon = $c_export_format[$cpt]["icon"];
    $url_help = "export_format_$cpt.html";
    $d_format .= "<tr>
      <td class=\"adminLabel\"><a target=\"export\" href=\"$url\">$name</a></td>
      <td class=\"adminDetail\"><img src=\"/images/$set_theme/$icon\" alt=\"I\" /></td>
      <td class=\"adminDetail\">$info</td>
      <td class=\"adminDetail\"><a target=\"doc\" href=\"$url_help\">X</a></td>
      </tr>";
    $cpt++;
  }
  $d_format .= "</table>";

  echo "<table class=\"details\">
    <tr>
      <td>$d_list
      </td>
      <td>$d_format
      </td>
    </tr>
    </table>";

}


</SCRIPT>
