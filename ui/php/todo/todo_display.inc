<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : todo_display.inc                                             //
//     - Desc : Todo Display File                                            //
// 2003-09-15 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
$fieldnames["date_todo"] = $l_date;
$fieldnames["date_deadline"] = $l_deadline;
$fieldnames["todo_priority"] = $l_priority;
$fieldnames["todo_title"] = $l_todotitle;

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


//   else if ($fieldname == "company_archive") {
//     $res["align"] = "center";
//     if ($OD->data_set->f($fieldname)) $res["name"] = "X";
//     else $res["name"] = "&nbsp;";
//   }

//   else if ($fieldname == "company_email") {
//     $email = $OD->data_set->f("company_email");
//     if (strcmp($email,"") != 0) {
//       $res["url"] = "mailto:$email";
//       $res["name"] = "<img src=\"/images/$set_theme/$ico_mail\" alt=\"$email\" />";
//     }
//   } 

  return $res;
}

///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_todo_form($todo, $user_q) {
  global $l_deadline, $l_priority, $l_todotitle, $l_user, $l_add, $l_content;

  $max_priority = 5;
  $uid = $todo["uid"];

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
    $selected = ($i == intval(($max_priority+1)/2)) ? "selected=\"selected\"" : "";
    $sel_priority .= "<option value=\"$i\" $selected>$i</option>";
  }

  $sel_priority .= "</select>";

  $block = "
    <form method=\"post\" name=\"f_add_todo\"
      onsubmit=\"if (check_addtodoform(this)) return true; else return false;\"
      action=\"".url_prepare("todo_index.php")."\">

    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_todotitle</td>
      <td class=\"detailForm\" colspan=\"5\">
       <input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"100\" size=\"100\" />
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_user</td>
      <td class=\"detailForm\">$sel_user</td>

      <td class=\"detailLabel\">$l_priority</td>
      <td class=\"detailForm\">$sel_priority</td>

      <td class=\"detailLabel\">$l_deadline</td>
      <td class=\"detailForm\">
       <input type=\"text\" id=\"tf_deadline\" name=\"tf_deadline\" maxlength=\"10\" size=\"10\" />
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_content</td>
      <td class=\"detailForm\" colspan=\"5\">
       <textarea id=\"ta_content\" name=\"ta_content\" rows=\"3\" cols=\"80\"></textarea>
      </td>
     </tr>
    </table>

    <div class=\"detailButton\">
     <p class=\"detailButtons\">
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"add\" />
      <input type=\"submit\" value=\"$l_add\" />
     </p>
    </div>

    </form>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_todo_list($todo, $todo_q) {
  global $l_del, $l_delete;

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
    <div class=\"detailButton\">
     <p class=\"detailButtons\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input name=\"submit\" type=\"submit\" value=\"".$l_delete."\" />
     </p>
    </div>
    </form>
    ";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_todo_detail($todo, $todo_q) {
  global $display;
  global $l_todo, $l_title, $l_user, $l_author, $l_priority, $l_date;
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
      <td class=\"detailLabel\">$l_title</td>
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
</script>