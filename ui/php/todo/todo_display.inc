<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_display.inc                                             //
//     - Desc : Todo Display File                                            //
// 2003-09-15 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
$fieldnames["date_todo"] = $l_date;
$fieldnames["date_deadline"] = $l_deadline;
$fieldnames["todo_priority"] = $l_priority;
$fieldnames["todo_title"] = $l_todotitle;
$fieldnames["todo_id"] = $l_update;

///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields                                   //
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

  else if ($fieldname == "date_todo") {
    $res["align"] = "center";
  }

  else if ($fieldname == "todo_id") {
    $t_id = $OD->data_set->f("todo_id");

    $res["popup"] = true;
    $res["popup_width"] = 700;
    $res["popup_height"] = 180;
    $res["url"] = "todo_index.php?action=update&amp;param_todo=$t_id&amp;popup=1";
    $res["name"] = "<img border=\"0\" src=\"/images/$set_theme/modify.png\" alt=\"\" />";
  }

  return $res;
}

///////////////////////////////////////////////////////////////////////////////
// Display Todo Form                                                         //
// Parameters:
//   - $user_q    : userobm database result 
//   - $todo_q    : default values
//   - $todo[]    : default values
//     keys used  : id, uid
///////////////////////////////////////////////////////////////////////////////
function dis_todo_form($todo, $user_q, $todo_q = 0) {
  global $set_theme, $set_date, $ico_mini_cal;
  global $l_add, $l_update, $l_close;
  global $l_deadline, $l_priority, $l_todotitle, $l_user, $l_content;

  $max_priority = 5;
  $action = $todo["action"];
  $popup = $todo["popup"];

  if ($action == "update") {
    $t_id = $todo["id"];

    $uid = $todo_q->f("todo_user");
    $title = $todo_q->f("todo_title");
    $content = $todo_q->f("todo_content");
    $priority = $todo_q->f("todo_priority");
    $deadline = ($todo_q->f("date_deadline")) ? date_format($todo_q->f("todo_deadline")) : "";
  } else {
    $uid = $todo["uid"];
    $priority = intval(($max_priority+1)/2);
    $deadline = date("$set_date");
  }
  // User select
  $sel_user = "<select id=\"sel_user\" name=\"sel_user\">";

  while($user_q->next_record()) {
    $user_id = $user_q->f("userobm_id");
    $sel_user .= "\n<option value=\"$user_id\"";
    if ($user_id == $uid) { $sel_user .= " selected=\"selected\""; }
    $sel_user .= ">". $user_q->f("userobm_lastname")." ".$user_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_user .= "</select>";

  // Priority select
  $sel_priority = "<select id=\"sel_priority\" name=\"sel_priority\">";

  for ($i=1; $i<=$max_priority; $i++) {
    $selected = ($i == $priority) ? "selected=\"selected\"" : "";
    $sel_priority .= "<option value=\"$i\" $selected>$i</option>";
  }
  $sel_priority .= "</select>";

  if ($action == "update") {
    $dis_button = "
      <input type=\"hidden\" id=\"param_todo\" name=\"param_todo\" value=\"$t_id\" />
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"detailupdate\" />
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
     </tr><tr>
      <td class=\"detailLabel\">$l_user</td>
      <td class=\"detailForm\">$sel_user</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_priority</td>
      <td class=\"detailForm\">$sel_priority</td>
     </tr><tr>
      <td class=\"detailLabel\">$l_deadline</td>
      <td class=\"detailForm\">
       <input type=\"text\" id=\"tf_deadline\" name=\"tf_deadline\" maxlength=\"10\" size=\"10\" value=\"$deadline\" />
       <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_add_todo.tf_deadline,'../agenda/agenda_index.php?action=calendar&popup=1') ;\">
        <img src=\"/images/$set_theme/$ico_mini_cal\" border=\"0\" />
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
// Display Todo Full list                                                    //
// Parameters:
//   - $todo_q    : todo list
//   - $todo[]    : default values
//     keys used  : uid
///////////////////////////////////////////////////////////////////////////////
function dis_todo_list($todo, $todo_q) {
  global $auth, $l_del, $l_delete;

  $uid = $todo["uid"];

  $pref_q = run_query_display_pref($auth->auth["uid"], "todo");

  $title = $todo["title"];
  $user = $todo["sel_user"];
  $priority = $todo["priority"];
  
  $url = url_prepare("todo_index.php?action=index&amp;tf_title=$title&amp;tf_user=$user&amp;sel_priority=$priority");

  $todo_q->seek($first_row);

  $block .= "
     <form method=\"post\" name=\"f_todo_del\" action=\"".url_prepare("todo_index.php").
    "\" \"onsubmit=\"if (confirm_del()) return true; else return false\" >
     ";

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
// Display Todo Detail (all informations)                                    //
// Parameters:
//   - $todo_q    : todo list
//   - $todo[]    : default values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_todo_detail($todo, $todo_q) {
  global $display;
  global $l_todo, $l_todotitle, $l_user, $l_author, $l_priority, $l_date;
  global $l_deadline, $l_content, $l_no_deadline;

  $title = $todo_q->f("todo_title");
  $user = $todo_q->f("user_firstname") ." ". $todo_q->f("user_lastname");
  $author = $todo_q->f("author_firstname") ." ". $todo_q->f("author_lastname");
  $priority = $todo_q->f("todo_priority");

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
// Display the Todo Display preference screen                                //
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function dis_admin_todo() {
  global $todo_order;
  global $l_todo_prefs, $l_sortorder, $l_sortpriority, $l_sortdeadline;

  $sel_priority = ($todo_order == "todo_priority") ? "selected=\"selected\"" : "";
  $sel_deadline = ($todo_order == "todo_deadline") ? "selected=\"selected\"" : "";

  $block = "
    <table class=\"admin\">
     <tr>
      <td colspan=\"2\" class=\"adminHead\">
        $l_todo_prefs
      </td>
     </tr><tr>
      <td class=\"adminLabel\">
       $l_sortorder
      </td>
      <td class=\"adminText\">
       <form name=\"f_order_change\" method=\"post\"
             action=\"" . url_prepare("todo_index.php") . "\">
        <select name=\"sel_order\" onChange=\"forms.f_order_change.submit();\">
         <option value=\"todo_priority\" $sel_priority>$l_sortpriority</option>
         <option value=\"todo_deadline\" $sel_deadline>$l_sortdeadline</option>
        </select>
        <input type=\"hidden\" name=\"action\" value=\"adminupdate\" />
       </form>
      </td>
     </tr>
    </table>
    ";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the Todo display preference screen                                //
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