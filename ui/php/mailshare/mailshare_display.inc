<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailshare_display.php                                        //
//         - Desc : Mailshare Display File                                   //
// 2007-02-02 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames['mailshare_name'] = $l_name;
$fieldnames['mailshare_email'] = $l_email;
$fieldnames['mailshare_quota'] = $l_quota;
$fieldnames['mailshare_delegation'] = $l_delegation;
$fieldnames['mailshare_description'] = $l_desc;
$fieldnames['usercreate'] = $l_creator;
$fieldnames['userupdate'] = $l_updater;
$fieldnames['timecreate'] = $l_date_creation;
$fieldnames['timeupdate'] = $l_date_last_update;


///////////////////////////////////////////////////////////////////////////////
// Display MailShare specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_mailshare(&$OD, $fieldname, $link_ok) {
  global $path, $ico_mail, $obm;

  if (($fieldname == 'mailshare_name') && $link_ok) {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = 'javascript:check_get_id('.$OD->data_set->f('mailshare_id').",'".addslashes($OD->data_set->f('mailshare_name'))."');";
    } else {
     $res['url'] = "$path/mailshare/mailshare_index.php?action=detailconsult&amp;mailshare_id=".$OD->data_set->f('mailshare_id');
    }
  }

  else if ($fieldname == 'mailshare_quota') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname) == '0') {
      $res['name'] = "&nbsp;";
      $res['txt_name'] = '';
    }    
  }

  else if ($fieldname == 'mailshare_email') {
    $main_domain = $obm['domain_name'];

    $email = $OD->data_set->f('mailshare_email');
    if (strcmp($email ,'') != 0) {
      $emails = explode("\n",$email);
      $email = trim($emails[0])."@$main_domain";
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\"/>$email";
      $res['txt_name'] = "$email";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mailshare search Form
// Parameters : 
//   - $mailshare[]   : default form values
//     keys used : name, user
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_search_form($mailshare) {
  global $display, $l_name, $l_email, $l_quota, $l_desc, $l_find;

  $popup = $mailshare['popup'];

  if ($popup) {
    $ext_action = $mailshare['ext_action'];
    $ext_target = $mailshare['ext_target'];
    $ext_widget = $mailshare['ext_widget'];
    $ext_widget_text = $mailshare['ext_widget_text'];
    $ext_url = $mailshare['ext_url'];
    $ext_id = $mailshare['ext_id'];
    $ext_title = stripslashes($mailshare['ext_title']);
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
      <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
      <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
      <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == '') {
      $ext_title = $l_add_mailshares;
    }
    $display['title'] = $ext_title;
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($mailshare['name']);
  $delegation = stripslashes($mailshare['delegation']);
  $quota = stripslashes($mailshare['quota']);
  $email = stripslashes($mailshare['email']);
  $desc = stripslashes($mailshare['description']);

  $dis_delegation = of_delegation_dis_search_form($delegation);

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" action=\"".url_prepare('mailshare_index.php')."\">

  <label>$l_name<br />
    <input name=\"tf_name\" size=\"24\" maxlength=\"32\" value=\"$name\" />
  </label>
  <label>$l_email<br />
    <input type=\"text\" name=\"tf_email\" size=\"24\" maxlength=\"32\" value=\"$email\" />
  </label>
  $dis_delegation
  <label>$l_quota<br />
    <input name=\"tf_quota\" size=\"8\" maxlength=\"8\" value=\"$quota\" />
  </label>
  <label>$l_desc<br />
    <input name=\"tf_desc\" size=\"24\" maxlength=\"32\" value=\"$desc\" />
  </label>
  <label>&nbsp;<br />
    <input name=\"action\" type=\"hidden\" value=\"search\" />
    <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
    <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    $ext
  </label>
  <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Mailshare search result
// Parameters:
//   - $mailshare[]   : mailshare search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_mailshare_search_list($mailshare) {
  global $l_no_found, $display, $obm;

  $prefs = get_display_pref($obm['uid'], 'mailshare');
  $obm_q = run_query_mailshare_search($mailshare);

  $nb_mailshare = $obm_q->num_rows_total();
  if ($nb_mailshare == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $block = html_mailshare_search_list($obm_q, $prefs, $nb_mailshare, $mailshare);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Mailshare search result
// Parameters : 
//   - $obm_q     : DBO : mailshare list
//   - $pref_q    : fields that have to be displayed
//   - $nb_mailshare   : nb mailshares returned by the search query 
//   - $group[]   : mailshare search criteria
//     keys used  : name, ip, popup
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_search_list($obm_q, $pref_q, $nb_mailshare, $mailshare) {
  global $display, $l_found, $l_close, $module;

  $popup = $mailshare['popup'];

  if ($popup) {
    $ext_action = $mailshare['ext_action'];
    $ext_url = $mailshare['ext_url'];
    $ext_target = $mailshare['ext_target'];
    $ext_widget = $mailshare['ext_widget'];
    $ext_widget_text = $mailshare['ext_widget_text'];
    $ext_id = $mailshare['ext_id'];
    $ext_title = urlencode(stripslashes($mailshare['ext_title']));
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_title=$ext_title&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text";
  }

  $name = urlencode(stripslashes($mailshare['name']));
  $email = urlencode(stripslashes($mailshare['email']));
  $delegation = urlencode(stripslashes($mailshare['delegation']));

  $url = url_prepare("mailshare_index.php?action=search&amp;tf_name=$name&amp;tf_email=$email&amp;tf_delegation=$delegation$url_ext");
  
  $dis_mailshare = new OBM_DISPLAY('DATA', $pref_q, $module);
  
  if ($popup) {
    if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $dis_mailshare->display_ext = 'get_id';
    }
    $dis_mailshare->data_form_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $dis_mailshare->data_set = $obm_q;
  $dis_mailshare->data_url = $url;
  $dis_mailshare->data_header = 'both';

  // --- HTML Template --------------------------------------------------------
  $display['msg'] .= display_ok_msg("$nb_mailshare $l_found");
  $block .= $dis_mailshare->display('dis_data_mailshare');

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mailshare Consultation
// Parameters:
//   - $mailshare[] : mailshare parameters
///////////////////////////////////////////////////////////////////////////////
function dis_mailshare_consult($mailshare) {

  $mailshare_q = run_query_mailshare_detail($mailshare['mailshare_id']);
  $block = html_mailshare_consult($mailshare_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Mailshare Consultation
// Parameters:
//   - $mailshare_q : mailshare database result 
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_consult($mailshare_q) {
  global $c_yes, $c_no, $path, $l_mail_server;
  global $l_mailshare, $l_name, $l_quota, $l_desc, $l_email;
  global $display, $action, $cright_write;

  $name = $mailshare_q->f('mailshare_name');
  $quota = $mailshare_q->f('mailshare_quota');
  $desc = $mailshare_q->f('mailshare_description');
  $email = get_entity_email($mailshare_q->f('mailshare_email'),null,false);
  $delegation = $mailshare_q->f('mailshare_delegation');
  $mail_server_id = $mailshare_q->f('userobm_mail_server_id');
  $mail_server_host_name = $mailshare_q->f('mailserver_hostname');

  $block_delegation = of_delegation_dis_block_consult($delegation);

  $display['detailInfo'] = display_record_info($mailshare_q);
  $display['title'] = $name;

  $block = "
  <div class=\"detail extra\">
  <h1>$l_mailshare</h1>

  <table>
  <tr>
    <th>$l_name</th>
    <td>$name</td>
  </tr>
  $block_delegation
  <tr>
    <th>$l_quota</th>
    <td>$quota</td>
  </tr>
  <tr>
    <th>$l_email</th>
    <td>$email</td>
    </tr>
  <tr>
    <th>$l_mail_server</th>
    <td>
      <a href=\"$path/mailserver/mailserver_index.php?action=detailconsult&id=$mail_server_id\">$mail_server_host_name</a>
    </td>
  </tr>
  <tr>
    <th>$l_desc</th>
    <td>$desc</td>
  </tr>
  </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mailshare Form
// Parameters :
//   - $action      : action called
//   - $mailshare_q : DBO : (null for new mailshare)
//   - $mailshare[] : default or transmitted values
//   - $field       : field in error
///////////////////////////////////////////////////////////////////////////////
function html_mailshare_form($action, $mailshare_q, $mailshare, $field='') {
  global $l_insert, $l_update, $l_back, $l_admin_permission, $l_header_new;
  global $l_mailshare, $l_name, $l_desc, $l_quota, $l_email;
  global $cmailshare_default_quota,$obm,$l_mail_server;
  global $display, $path, $obm, $cgp_use;
  
  // if update mode and first time, values are taken from db
  if ($action == 'detailupdate') {
    $id = $mailshare_q->f('mailshare_id');
    $name = $mailshare_q->f('mailshare_name');
    $quota = $mailshare_q->f('mailshare_quota');
    $email = $mailshare_q->f('mailshare_email');
    $delegation = $mailshare_q->f('mailshare_delegation');
    $desc = $mailshare_q->f('mailshare_description');
    $mail_server_id = $mailshare_q->f('mailshare_mail_server_id');
    $mail_server_host_name = $mailshare_q->f('mailserver_hostname');
    $dis_title = $name;

  } else if ($action == 'new') {
    $quota = $cmailshare_default_quota;
  }

  // If parameters have been given, they supercede the default action value
  if (isset($mailshare['mailshare_id'])) { $id = $mailshare['mailshare_id']; }
  if (isset($mailshare['name'])) { $name = strtolower(stripslashes($mailshare['name'])); }
  if (isset($mailshare['delegation'])) { $delegation = stripslashes($mailshare['delegation']); }
  if (isset($mailshare['quota'])) { $quota = $mailshare['quota']; }
  if (isset($mailshare['email'])) { $email = $mailshare['email']; }
  if (isset($mailshare['desc'])) { $desc = stripslashes($mailshare['desc']); }
  if (isset($user['mail_server_id'])) { $mail_server_id = $user['mail_server_id']; }
  if (isset($user['mail_server_hostname'])) { $mail_server_host_name = $user['mail_server_hostname']; }  

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);

  // Mail server can't be changed if set
  if (! $mail_server_host_name) {
    if (! $mail_server_id) {
      $mail_server_id = $obm['mailserver'];
    }
    $dis_mailserver = of_domain_dis_mailserver_select('imap', $mail_server_id, $obm['domain_id']);
  } else {
    $dis_mailserver = "
    <a href=\"$path/mailserver/mailserver_index.php?action=detailconsult&id=$mail_server_id\">$mail_server_host_name</a>
    <input type=\"hidden\" name=\"sel_mail_server_id\" value=\"$mail_server_id\" />
    <input type=\"hidden\" name=\"sel_mail_server_hostname\" value=\"$mail_server_host_name\" />";
  }

  $main_domain = $obm['domain_name'];

  $display['detailInfo'] = display_record_info($mailshare_q);
  $display['title'] = $dis_title;

  if (($action == 'new') || ($action == 'insert')) {
    $dis_name = "<input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" />";

    $dis_button .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";

    $right_manager = "
      <fieldset class=\"detail extra\">
      <legend>$l_admin_permission : <input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" /></legend>
      <input type=\"hidden\" name=\"sel_accept_admin[]\" value=\"data-user-$obm[uid]\" />
      <div id=\"sel_accept_admin\">     
      </div>
      </fieldset>";
    $js = "
    <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search', 'sel_accept_admin', 'userSearch');
    });
    </script>";
  } elseif (($action == 'detailupdate') || ($action == 'update')) {
    $dis_name = "$name <input name=\"tf_name\" type=\"hidden\" value=\"$name\" />";

    $dis_button = "
      <input type=\"hidden\" name=\"mailshare_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";
  }
  
  $mail_block = dis_mailshare_mail_field($email,$class);
  $block = "
  <form method=\"post\" name=\"f_mailshare\" action=\"".url_prepare('mailshare_index.php')."\">

  <fieldset class=\"detail extra\">
  <legend>$l_mailshare</legend>

  <table>
  <tr>
    <th class=\"$class[name]\">$l_name</th>
    <td>$dis_name</td>
  </tr>
  <tr>
  $mail_block
  </tr>
  $block_delegation
  <tr>
    <th class=\"$class[quota]\">$l_quota</th>
    <td><input name=\"tf_quota\" maxlength=\"8\" size=\"8\" value=\"$quota\" /></td>
  </tr>
  <tr>
    <th class=\"$class[mail_server]\">$l_mail_server</th>
    <td>$dis_mailserver</td>
  </tr>    
  <tr>
    <th>$l_desc</th>
    <td><input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"128\" /></td>
  </tr>
  </table>
  $right_manager
  <fieldset class=\"buttons\">
  $dis_button
  </fieldset>
  </form>
$js  
";

  return $block;
}


/**
 * dis_mailshare_right_dis_admin
 * Display mailshare rights admin form
 *
 * @param mixed $id mailshare id to admin
 * @return mixed xhtml display
 */
function dis_mailshare_right_dis_admin($id='') {
  global $obm, $display, $l_mailshare;

  if ($id == '') {
    return false;
  }
  $infos = get_mailshare_info($id);
  $name = $infos['name'];
  $display['title'] = $name;
  $block = of_right_dis_admin('MailShare', $id);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Mailshare Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_mailshare_display_pref($prefs) {
  global $l_mailshare_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'mailshare');
  $dis_pref->display_entity = 'mailshare'; 
  $dis_pref->pref_title = $l_mailshare_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the mailshare delete validation screen
// Parameters:
//   - $p_id : mailshare id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_mailshare($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $dis_back = "<form name=\"form_back\" method=\"get\"
    action=\"".url_prepare('mailshare_index.php') ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"mailshare_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"form\"
      action=\"" . url_prepare('mailshare_index.php') . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"mailshare_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
  <fieldset class=\"buttons\">
  $dis_delete
  $dis_back
  </fieldset>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a mailshare insertion or update
// When similar mailshares exist we show these and ask confirmation
// Parameters:
//   - $id       : mailshare id
//   - $h_q      : mailshare database result (at least 1 row)
//   - $mailshare[]  : values for insertion/update (if confirmation)
//     keys used : name, desc
/////////////////////////////////////////////////////////////////////////////
function dis_mailshare_warn_insert($id, $h_q, $mailshare) {
  global $display, $l_check_samemailshare, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = strtolower($mailshare['name']);
  $delegation = $mailshare['delegation'];
  $desc = $mailshare['desc'];
  $email = $mailshare['email'];
  $quota = $mailshare['quota'];

  $display['msg'] .= display_warn_msg($l_check_samemailshare);
  while ($h_q->next_record()) {
    $id = $h_q->f('mailshare_id');
    $samename = $h_q->f('mailshare_name');
    $samedesc = $h_q->f('mailshare_description');
    $dis_same_mailshare .= "
      <tr><td class=\"detailLabel\">
        <a href=\"" .url_prepare("mailshare_index.php?action=detailconsult&amp;mailshare_id=$id") . "\">$samename ($samedesc)</a>
      </td></tr>";
  }

  $block = "
  <table class=\"detail\">
    $dis_same_mailshare
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
      <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare('mailshare_index.php') . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_delegation\" value=\"$delegation\" />
      <input type=\"hidden\" name=\"ta_email\" value=\"$email\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"hidden\" name=\"tf_quota\" value=\"$quota\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      </form>
    </p>
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare('mailshare_index.php') . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"ta_email\" value=\"$email\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"hidden\" name=\"tf_quota\" value=\"$quota\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  return $block;
}


/**
 * Generate email field  
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_mailshare_mail_field($values) {

  $d = of_domain_get_domain_infos($GLOBALS['obm']['domain_id'], true);
  if (!empty($d['alias'])) {
    $aliases = explode("\r\n",$d['alias']);
  } else {
    $aliases = array();
  }
  array_unshift($aliases,$d['name']);
  if(!empty($values)) {
    $emails = explode("\r\n",$values);
  }
  $count = 0;
  if (is_array($emails)) {
    foreach($emails as $key => $email) {
      $sel_alias = '';
      list($mail,$domain) = explode('@',$email);  
      foreach($aliases as $alias) {
        if ($alias == $domain) {
          $sel_alias .= "<option selected='selected' value='$alias'>$alias</option>";
        } else {
          $sel_alias .= "<option value='$alias'>$alias</option>";
        } 
      }
      $sel_alias = "<select name='sel_aliases[]'>
        <option value=''>$GLOBALS[l_all_aliases]</option>
        $sel_alias
        </select>";

      $email_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'mailshareMailHome');show_hide_add_button();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>        
        <input name='tf_email[]' value='$mail' /> @ $sel_alias
        </div>";
      $count ++;
    }
  }
  $sel_alias = '';
  foreach($aliases as $alias) {
    $sel_alias .= "<option value=\"$alias\">$alias</option>";
    $sel_js .= "aliasSelectTemplate.adopt(new Element('option').setProperty('value','$alias').appendText('$alias'));\n";
  }
  $sel_alias = "
    <select name=\"sel_aliases[]\">
    <option value=\"\">$GLOBALS[l_all_aliases]</option>
    $sel_alias
    </select>";
  if ($GLOBALS['c_max_mailshare_alias'] ==0 || $count < $GLOBALS['c_max_mailshare_alias']) {
    $email_block .= "
    <div class=\"multiple\">
    <a onclick=\"remove_element(this.parentNode,'mailshareMailHome');show_hide_add_button();return false\" href=\"\">
      <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
    </a>
    <input name='tf_email[]' value='' /> @ $sel_alias
    </div>";
    $count++;
  } 
  if ($GLOBALS['c_max_mailshare_alias'] ==0 || $count < $GLOBALS['c_max_mailshare_alias']) {
    $add_button = "<a id='addMailButton' href=\'\' onclick=\"add_email_field(aliasSelectTemplate);show_hide_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  } 
  $email_block = "
  <th class=\"$class[email]\" id='mailshareMailLabel'>
  $GLOBALS[l_email]
  <script type='text/javascript'>
    var aliasSelectTemplate = new Element('select').setProperty('name','sel_aliases[]');
    aliasSelectTemplate.adopt(new Element('option').setProperty('value','').appendText('$GLOBALS[l_all_aliases]'));
    $sel_js
  </script>
  $add_button
  </th>
  <td id='mailshareMailHome'>
  $email_block
  </td>
";

  return $email_block;
}

</script>
