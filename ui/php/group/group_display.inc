<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : group_display.php                                            //
//     - Desc : Group Display File                                           //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["group_name"] = $l_name;
$fieldnames["group_system"] = $l_system;
$fieldnames["group_desc"] = $l_desc;
$fieldnames["group_email"] = $l_email;
$fieldnames["usercreate"] = $l_creator;
$fieldnames["userupdate"] = $l_updater;
$fieldnames["timecreate"] = $l_date_creation;
$fieldnames["timeupdate"] = $l_date_last_update;
// Calculated fields
$fieldnames["group_nb_user"] = $l_nb_user;
$fieldnames["group_user_lastname"] = $l_lastname;
$fieldnames["group_user_firstname"] = $l_firstname;
$fieldnames["group_user_function"] = $l_function;
$fieldnames["group_user_phone"] = $l_phone;
$fieldnames["group_user_email"] = $l_email;


///////////////////////////////////////////////////////////////////////////////
// Display Group specific dataset fields                                     //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_group(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new;

  if (($fieldname == "group_name") && $link_ok) {
    $res["url"] = "$path/group/group_index.php?action=detailconsult&amp;param_group=".$OD->data_set->f("group_id");
  }

  else if ($fieldname == "group_system") {
    $res["align"] = "center";
  }

  else if ($fieldname == "group_nb_user") {
    $nb_q = new DB_OBM;
    $id = $OD->data_set->f("group_id");
    $query = "select count(*) from UserObmGroup where userobmgroup_groupid='$id'";
    $nb_q->query($query);
    $nb_q->next_record();
    $res["name"] = $nb_q->f(0);
    $res["align"] = "center";
  }

  // For User member lists
  elseif (($fieldname == "group_user_lastname")  && $link_ok) {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("group_user_id");
  }


  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group search Form                                                 //
// Parameters : 
//   - $group[]   : default form values
//     keys used  : name, user
///////////////////////////////////////////////////////////////////////////////
function html_group_search_form ($group) {
  global $display, $l_name, $l_group_user, $l_email, $l_find;

  $popup = $group["popup"];

  if ($popup) {
    $ext_action = $group["ext_action"];
    $ext_target = $group["ext_target"];
    $ext_url = $group["ext_url"];
    $ext_id = $group["ext_id"];
    $ext_title = stripslashes($group["ext_title"]);
    $ext = "<input name=\"ext_action\" type=\"hidden\" VALUE=\"$ext_action\">
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
      <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["msg"] .= display_info_msg($ext_title);
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($group["name"]);
  $email = stripslashes($group["email"]);
  $user = stripslashes($group["user"]);

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_group\" action=\"".url_prepare("group_index.php")."\">
  <div class=\"search\">

    <div class=\"searchLabel\">$l_name<br />
      <input name=\"tf_name\" size=\"16\" maxlength=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_group_user<br />
      <input name=\"tf_user\" size=\"16\" maxlength=\"16\" value=\"$user\" />
    </div>
    <div class=\"searchLabel\">$l_email<br />
      <input name=\"tf_email\" size=\"20\" maxlength=\"20\" value=\"$email\" />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Group search result                                           //
// Parameters:
//   - $group[]   : group search criteria
//     keys used  : name, description, email
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_group_search_group($group, $popup=false) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "group");
  if ($popup) {
    $obm_q = run_query_search_possible_children($group,$new_order, $order_dir);
  }  else {
    $obm_q = run_query_search($group, $new_order, $order_dir);
  }

  $nb_group = $obm_q->num_rows();
  if ($nb_group == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_group_search_group($obm_q, $pref_q, $nb_group, $group, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Group search result                                      //
// Parameters : 
//   - $obm_q     : list of the groups to display 
//   - $pref_q    : the fields which have to be displayed
//   - $nb_group  : nb groups returned by the search query 
//   - $group[]   : group search criteria
//     keys used  : name, user
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_group_search_group($obm_q, $pref_q, $nb_group, $group, $popup) {
  global $l_found, $l_add, $l_close;

  if ($popup) {
    $ext_action = $group["ext_action"];
    $ext_url = $group["ext_url"];
    $ext_target = $group["ext_target"];
    $ext_id = $group["ext_id"];
    $ext_title = urlencode(stripslashes($group["ext_title"]));
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_title=$ext_title";
  }

  $name = urlencode(stripslashes($group["name"]));
  $user = urlencode(stripslashes($group["user"]));

  $url = url_prepare("group_index.php?action=search&amp;tf_name=$namet&amp;tf_user=$user$url_ext");
  
  $dis_group = new OBM_DISPLAY("DATA", $pref_q);
  if ($popup) {
    $dis_group->display_link = false;
    $dis_group->data_cb_text = "X";
    $dis_group->data_idfield = "group_id";
    $dis_group->data_cb_name = "cb_g";
    $display_popup_head = "<form target=\"$ext_target\" method=\"get\" action=\"$ext_url\">";
    $display_popup_end = "
    <p class=\"detailButton\">
    <p class=\"detailButtons\">
      <input type=\"submit\" value=\"$l_add\" />
      <input type=\"hidden\" name=\"param_group\" value=\"$ext_id\" /> 
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" /> 
    </p>
    </p>
    </form>
    <p><a href=\"\" onclick='window.close();'>$l_close</a></p>";
  }

  $dis_group->data_set = $obm_q;
  $dis_group->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $display["msg"] .= display_ok_msg("$nb_group $l_found");
  $block .= $display_popup_head;
  $block .= $dis_group->display("dis_data_group");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group Form                                                        //
// Parameters :
//   - $action    : action called
//   - $group_q   : DBO : information about the group (null for new group)
//   - $group[]   : default or transmitted values
//     keys used  : name, desc, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_group_form($action, $group_q, $group) {
  global $l_group, $l_name, $l_desc, $l_email, $l_insert, $l_update, $l_back;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $group_q->f("group_id");
    $usercreate = $group_q->f("group_usercreate");
    $name = $group_q->f("group_name");
    $desc = $group_q->f("group_desc");
    $email = $group_q->f("group_email");
  } else {
    // Values are taken from parameters
    $id = $group["id"];
    $name = stripslashes($group["name"]);
    $desc = stripslashes($group["desc"]);
    $email = stripslashes($group["email"]);
  }

  //  $mail = get_mail_info();
  //  $main_domain = $mail["main_domain"];

  $block = "<center>
  <form method=\"get\" name=\"f_group\" action=\"".url_prepare("group_index.php")."\">

  <div class=\"detailHead\">$l_group</div>

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" /></td>
  </tr><tr>
    <td class=\"detailLabel\">$l_desc</td>
    <td class=\"detailForm\">
      <input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"70\" /></td>
  </tr><tr>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\">
      <input name=\"tf_email\" maxlength=\"64\" size=\"32\" value=\"$email\" /></td>
  </tr>
  </table>";


  if (($action == "detailupdate") || ($action == "update")) {
    $dis_but = "
      <input type=\"hidden\" name=\"param_group\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

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
// HTML Display Group Consultation                                           //
// Parameters:
//   - $group_q  : group database result 
//   - $pref_u_q : group user preference display 
//   - $u_q      : user database result 
///////////////////////////////////////////////////////////////////////////////
function html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q) {
  //-- Themes
  global $ico_mail;
  //-- Label
  global $l_group, $l_name, $l_desc, $l_email, $l_no_user, $l_no_group_group;
  global $l_del_user_sel, $l_user_member, $l_del_group_sel, $l_group_member;
  //Variable
  global $display, $perm, $action;

  $id = $group_q->f("group_id");
  $usercreate = $group_q->f("group_usercreate");
  $userupdate = $group_q->f("group_userupdate");
  $timecreate = $group_q->f("timecreate");
  $timeupdate = $group_q->f("timeupdate");
  $name = $group_q->f("group_name");
  $desc = $group_q->f("group_desc");
  $email = $group_q->f("group_email");

  //$mail = get_mail_info();
  //$main_domain = $mail["main_domain"];

  $display["detailInfo"] = display_record_info($usercreate, $userupdate, $timecreate, $timeupdate);

  $block = "
    <div class=\"detailHead\">$l_group</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_desc</td>
      <td class=\"detailText\">$desc</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$email</td>
    </tr>
    </table>";


  // Affichage des groupes membres 
  $nb_g = $g_q->num_rows();
  if ($nb_g == 0) {
    $message = $l_no_group_group;
  } else {
    $message = "$nb_g $l_group_member";
  }

  $block .= display_info_msg($message);

  if ($nb_g != 0) {
    $url = url_prepare("group_index.php?action=detailconsult&amp;param_group=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $pref_g_q);
    $dis_infos->data_set = $g_q;
    $dis_infos->entity = "group_group";
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "top";

    if ($perm->have_perm("editor")) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "child_id";
      $dis_infos->data_cb_name = "cb_g";
      $dis_infos->data_cb_field = "";
      $block .= "<form method=\"get\" action=\"group_index.php\">";
    }

    $block .= $dis_infos->display("dis_data_group");

    if ($perm->have_perm("editor")) {
      $block .= "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_group_sel\" /> 
        <input type=\"hidden\" name=\"param_group\" value=\"$id\" /> 
        <input type=\"hidden\" name=\"action\" value=\"group_del\" /> 
        </p>
      </p>
      </form>";
    }
  }


  // Affichage des utilisateurs membres 
  $nb_u = $u_q->num_rows();
  if ($nb_u == 0) {
    $message = $l_no_user;
  } else {
    $message = "$nb_u $l_user_member";
  }

  $block .= display_info_msg($message);

  if ($nb_u != 0) {

    $url = url_prepare("group_index.php?action=detailconsult&amp;param_group=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $pref_u_q);
    $dis_infos->data_set = $u_q;
    $dis_infos->entity = "group_user";
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";

    if ($perm->have_perm("editor")) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "group_user_id";
      $dis_infos->data_cb_name = "cb_u";
      $dis_infos->data_cb_field = "";
      $block .= "<form method=\"get\" action=\"group_index.php\">";
    }

    $block .= $dis_infos->display("dis_data_group");

    if ($perm->have_perm("editor")) {
      $block .= "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_user_sel\" />
        <input type=\"hidden\" name=\"param_group\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"user_del\" />
        </p>
      </p>
      </form>";
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Group Display preference screen                               //
// Parameters:
//   - $pref_group_q : DBO : Group Field group to display
//   - $pref_u_q     : DBO : User Field group to display
///////////////////////////////////////////////////////////////////////////////
function dis_group_display_pref($pref_group_q, $pref_u_q) {
  global $l_group_display, $l_user_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_group_q);
  $dis_pref->display_entity = "group"; 
  $dis_pref->pref_title = $l_group_display;
  $dis_pref->pref_dis_help = 0;

  $block = "
    <table class=\"detail\">
    <tr>
      <td>" . $dis_pref->display() . "</td>";

  $dis_pref->display_pref = $pref_u_q;
  $dis_pref->display_module = "group";
  $dis_pref->display_entity = "group_user";
  $dis_pref->pref_title = $l_user_display;

  $block .= "<td>" . $dis_pref->display() . "</td>
      </td>
    </tr>
    </table>";

  $block .= $dis_pref->dis_pref_help();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a group deletion                                   //
// We ask confirmation or cancel
// Parameters:
//   - $l_id : group id
///////////////////////////////////////////////////////////////////////////////
function dis_warn_delete($l_id) {
  global $display, $l_warn_delete, $l_delete, $l_back;

  $nb_u = get_group_nb_user($l_id);
  $display["msg"] .= display_warn_msg("$nb_u $l_warn_delete");

  $block = "
  <p class=\"detailButton\">
    <p class=\"detailButtons\">
      <form method=\"get\" name=\"form_delete\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input type=\"hidden\" name=\"hd_group_id\" value=\"$l_id\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
      </form>
    </p>
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"param_group\" value=\"$l_id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </p>";

  return $block;

}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a group insertion or update                     //
// When similar groups exist we show these and ask confirmation
// Parameters:
//   - $l_id     : group id
//   - $l_q      : group database result (at least 1 row)
//   - $group[]   : values for insertion/update (if confirmation)
//     keys used : name, desc
/////////////////////////////////////////////////////////////////////////////
function dis_group_warn_insert($l_id, $l_q, $group) {
  global $display, $l_check_samegroup, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = $group["name"];
  $desc = $group["desc"];

  $display["msg"] .= display_warn_msg($l_check_samegroup)
;
  while ($l_q->next_record()) {
    $id = $l_q->f("group_id");
    $samename = $l_q->f("group_name");
    $samedesc = $l_q->f("group_desc");
    $dis_same_group .= "
      <tr><td class=\"detailLabel\">
        <a href=\"" .url_prepare("group_index.php?action=detailconsult&amp;param_group=$id") . "\">$samename ($samedesc)</a>
      </td></tr>";
  }

  $block = "
  <table class=\"detail\">
    $dis_same_group
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
      <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      </form>
    </p>
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  return $block;
}


</SCRIPT>
