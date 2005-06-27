<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_display.inc                                             //
//     - Desc : Todo Display File                                            //
// 2003-09-15 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
$fieldnames["date_todo"] = $l_date;
$fieldnames["date_deadline"] = $l_deadline;
$fieldnames["todo_priority"] = $l_priority;
$fieldnames["todo_percent"] = $l_percent;
$fieldnames["todo_title"] = $l_todotitle;
$fieldnames["todo_id"] = $l_update;


///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_todo(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme;

  if ($fieldname == "date_deadline") {
    $deadline = $OD->data_set->f("date_deadline");
    $res["name"] = ($deadline) ? date_format($deadline) : " ";
    $res["align"] = "center";
  }

  else if ($fieldname == "todo_title") {
    $t_id = $OD->data_set->f("todo_id");
    $res["url"] = "todo_index.php?action=detailconsult&amp;param_todo=$t_id";
  }

  else if ($fieldname == "todo_priority") {
    $res["align"] = "center";
  }

  else if ($fieldname == "todo_percent") {
    $res["align"] = "center";
    $percent = $OD->data_set->f("todo_percent");
    $res["name"] = "$percent %";
  }

  else if ($fieldname == "date_todo") {
    $res["align"] = "center";
  }

  else if ($fieldname == "todo_id") {
    $t_id = $OD->data_set->f("todo_id");
    $res["popup"] = true;
    $res["popup_width"] = 700;
    $res["popup_height"] = 260;
    $res["url"] = "todo_index.php?action=update&amp;param_todo=$t_id&amp;popup=1";
    $res["name"] = "<img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/modify.png\" alt=\"\" />";
    $res["txt_name"] = " ";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Full list
// Parameters:
//   - $todo_q    : todo list
//   - $todo[]    : default values
//     keys used  : uid
///////////////////////////////////////////////////////////////////////////////
function dis_todo_list($todo, $todo_q) {
  global $auth, $l_del, $l_delete;

  $uid = $todo["uid"];
  $title = $todo["title"];
  $users = $todo["sel_user"];
  $user = $users[0];
  $priority = $todo["priority"];

  $url = url_prepare("todo_index.php?action=index&amp;tf_title=$title&amp;tf_user=$user&amp;sel_priority=$priority");

  $block .= "
     <form method=\"post\" name=\"f_todo_del\" action=\"".url_prepare("todo_index.php").
    "\" \"onsubmit=\"if (confirm_del()) return true; else return false\" >
     ";
  $pref_q = run_query_display_pref($auth->auth["uid"], "todo");

  $todo_d = new OBM_DISPLAY("DATA", $pref_q);
  $todo_d->data_set = $todo_q;
  $todo_d->data_url = $url;
  $todo_d->data_idfield = "todo_id";
  $todo_d->data_cb_text = "$l_del";
  $todo_d->data_cb_side = "left";
  $todo_d->data_cb_name = "delete_";
  $todo_d->data_cb_show = 1;
  $todo_d->data_header = "both";

  $block .= $todo_d->display("dis_data_todo");

  $block .= "
    <p class=\"detailButton\">
     <p class=\"detailButtons\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input name=\"submit\" type=\"submit\" value=\"".$l_delete."\" />
     </p>
    </p>
    </form>
    ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Detail (all informations)
// Parameters:
//   - $todo_q    : todo list
//   - $todo[]    : default values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_todo_detail($todo, $todo_q) {
  global $display;
  global $l_todo, $l_todotitle, $l_user, $l_author, $l_priority, $l_date;
  global $l_deadline, $l_content, $l_no_deadline, $l_percent;

  $title = $todo_q->f("todo_title");
  $user = $todo_q->f("user_firstname") ." ". $todo_q->f("user_lastname");
  $author = $todo_q->f("author_firstname") ." ". $todo_q->f("author_lastname");
  $priority = $todo_q->f("todo_priority");
  $percent = $todo_q->f("todo_percent");

  $date = date_format($todo_q->f("date_todo"));
  $deadline = $todo_q->f("date_deadline");
  $deadline = ($deadline != 0) ? date_format($deadline) : $l_no_deadline;

  $content = $todo_q->f("todo_content");

  $display["title"] = "<div class=\"title\">$l_todo : $title</div>";

  $block = "
    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_todotitle</td>
      <td class=\"detailText\">$title</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_user</td>
      <td class=\"detailText\">$user</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_author</td>
      <td class=\"detailText\">$author</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_priority</td>
      <td class=\"detailText\">$priority</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_percent</td>
      <td class=\"detailText\">$percent</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailText\">$date</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_deadline</td>
      <td class=\"detailText\">$deadline</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_content</td>
      <td class=\"detailText\">$content</td>
     </tr>
    </table>
    ";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Todo Form
// Parameters:
//   - $todo[] : default values
//   - $todo_q : default values
///////////////////////////////////////////////////////////////////////////////
function dis_todo_form($todo, $todo_q = 0) {
  global $uid;
  global $set_theme, $set_date, $ico_mini_cal, $ico_add_user,$ico_delete;
  global $popup_width, $popup_height;
  global $l_add, $l_update, $l_close,$display,$l_todo,$l_user;
  global $l_deadline, $l_priority, $l_todotitle, $l_percent, $l_content;

  $max_priority = 5;
  $action = $todo["action"];
  $popup = $todo["popup"];
 
  if ($action == "detailupdate") {
    $t_id = $todo["id"];
    $uid = $todo_q->f("todo_user");
    $title = $todo_q->f("todo_title");
    $content = $todo_q->f("todo_content");
    $priority = $todo_q->f("todo_priority");
    $percent = $todo_q->f("todo_percent");
    $deadline = $todo_q->f("date_deadline");
    $deadline = ($deadline != 0) ? date_format($deadline, 1) : $l_no_deadline;
  } else {
    $uid = $todo["uid"];
    $priority = intval(($max_priority+1)/2);
    $deadline = date("Y-m-d");
  }

  if (is_array($todo["sel_user"])) {
    $p_user_array = $todo["sel_user"];
  } else {
    $p_user_array = array($uid);
  }
  $usr_q = run_query_userobm_in($p_user_array);

  // UserObm section
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $u_lname = $usr_q->f("userobm_lastname");
    $u_fname = $usr_q->f("userobm_firstname");
    if (in_array($u_id, $p_user_array)) {
      $dis_sel_user .= "<div class=\"elementRow\" id=\"sel_user_id$u_id\">
      <a href=\"javascript: remove_element('sel_user_id$u_id','sel_user_id');\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\">
      </a>
      $u_lname $u_fname
      <input value=\"$u_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }
  
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_user_id";
   
  $block_user = "
  </tr><tr>
    <td class=\"detailLabel\">
      $l_users
      <a href=\"javascript: return false;\" 
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_user\" alt=\"[Add]\" />
      </a>
    </td>    
    <td class=\"detailForm\" id=\"sel_user_id\">
      $dis_sel_user
    </td>";

  $display["title"] = "<div class=\"title\">$l_todo : $title</div>";
  // Priority select
  $sel_priority = "<select id=\"sel_priority\" name=\"sel_priority\">";

  for ($i=1; $i<=$max_priority; $i++) {
    $selected = ($i == $priority) ? "selected=\"selected\"" : "";
    $sel_priority .= "<option value=\"$i\" $selected>$i</option>";
  }
  $sel_priority .= "</select>";

  if ($action == "detailupdate") {
    $dis_button = "
      <input type=\"hidden\" id=\"param_todo\" name=\"param_todo\" value=\"$t_id\" />
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" id=\"popup\" name=\"popup\" value=\"$popup\" />
      <input type=\"submit\" value=\"$l_update\" />
    ";
  } else {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"add\" />
      <input type=\"submit\" value=\"$l_add\" />
    ";
  }

  if ($popup)
    $dis_button .=  "<input type=\"submit\" value=\"$l_close\" onclick=\"window.close();\" />";

  $block = "
  <form method=\"post\" name=\"f_add_todo\"
    onsubmit=\"if (check_addtodoform(this)) return true; else return false;\"
    action=\"".url_prepare("todo_index.php")."\">

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_todotitle</td>
    <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"100\" size=\"100\" value=\"$title\" />
    </td>
  $block_user
  </tr><tr>
    <td class=\"detailLabel\">$l_priority</td>
    <td class=\"detailForm\">$sel_priority</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_percent</td>
    <td class=\"detailForm\"><input type=\"text\" id=\"tf_percent\" name=\"tf_percent\" maxlength=\"3\" size=\"3\" value=\"$percent\" />%</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_deadline</td>
    <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_deadline\" name=\"tf_deadline\" maxlength=\"10\" size=\"10\" value=\"$deadline\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_add_todo.tf_deadline,'../agenda/agenda_index.php?action=calendar&popup=1') ;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_content</td>
    <td class=\"detailForm\">
      <textarea id=\"ta_content\" name=\"ta_content\" rows=\"3\" cols=\"80\">$content</textarea>
    </td>
  </tr>
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    $dis_button
    </p>
  </div>

  </form>
  <p /> <br />";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Todo display preference screen
// Parameters:
//   - $display_newpref_q    : DBO : Field list to display
//   - $display_searchpref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_todo_display_pref($display_searchpref_q) {
  global $l_todo_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_searchpref_q);
  $dis_pref->display_entity = "todo";
  $dis_pref->pref_title = $l_todo_display;

  $block .= "<td>";
  $block .= $dis_pref->display();
  $block .= "</td></tr></table>";

  $block .= $dis_pref->dis_pref_help();
  return $block;
}

</script>
