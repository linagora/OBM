<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailserver_display.php                                       //
//     - Desc : Mail Server Display File                                     //
// 2007-02-08 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames['host_name'] = $l_host;
$fieldnames['host_ip'] = $l_ip;
$fieldnames['mailserver_imap'] = $l_imap;
$fieldnames['mailserver_smtp_in'] = $l_smtp_in;
$fieldnames['mailserver_smtp_out'] = $l_smtp_out;


///////////////////////////////////////////////////////////////////////////////
// Display MailServer specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_mailserver(&$OD, $fieldname, $link_ok) {
  global $path ;

  if (($fieldname == 'host_name') && $link_ok) {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_get_id(".$OD->data_set->f('host_id').",'".addslashes($OD->data_set->f('host_name'))."');";
    } else {
     $res['url'] = "$path/mailserver/mailserver_index.php?action=detailconsult&amp;mailserver_id=".$OD->data_set->f('mailserver_id');
    }
  }

  else if (($fieldname == 'mailserver_imap') || ($fieldname == 'mailserver_smtp_in') || ($fieldname == 'mailserver_smtp_out')) {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname) == '1') {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  return $res;
}


/**
 * Display MailServer search Form
 *
 * @param mixed $host[] default form values
 * @return string XHTML search form
 */
function html_mailserver_search_form($params) {
  global $display, $l_name, $l_ip, $l_imap, $l_smtp_in, $l_smtp_out, $l_find;
  global $l_select_mailserver;

  $popup = $params['popup'];

  if ($popup) {
    $ext_action = $params['ext_action'];
    $ext_target = $params['ext_target'];
    $ext_widget = $params['ext_widget'];
    $ext_widget_text = $params['ext_widget_text'];
    $ext_url = $params['ext_url'];
    $ext_id = $params['ext_id'];
    $ext_title = $params['ext_title'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
      <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
      <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
      <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == '') {
      $ext_title = $l_select_mailserver;
    }
    $display['title'] = "<div class=\"title\">$ext_title</div>";
  }
  
  // --- Var preparation ------------------------------------------------------

  $host_name = stripslashes($params['host_name']);
  $host_ip = stripslashes($params['host_ip']);
  $imap = ($params['imap'] == '1' ? 'checked="checked" ' : '');
  $smtp_in = ($params['smtp_in'] == '1' ? 'checked="checked" ' : '');
  $smtp_out = ($params['smtp_out'] == '1' ? 'checked="checked" ' : '');

  // --- HTML Page display ----------------------------------------------------

  $block = "
    <form method=\"get\" name=\"f_search\" class=\"search\" action=\"".url_prepare("mailserver_index.php")."\">
    <div class=\"search\">
      <label>$l_name<br />
        <input name=\"tf_host_name\" size=\"24\" maxlength=\"32\" value=\"$name\" />
      </label>
      <label>$l_ip<br />
        <input name=\"tf_ip\" size=\"16\" maxlength=\"16\" value=\"$ip\" />
      </label>
      <label>$l_imap<br />
        <input type=\"checkbox\" name=\"cb_imap\" value=\"1\" $imap/>
      </label>
      <label>$l_smtp_in<br />
        <input type=\"checkbox\" name=\"cb_smtp_in\" value=\"1\" $smtp_in/>
      </label>
      <label>$l_smtp_out<br />
        <input type=\"checkbox\" name=\"cb_smtp_out\" value=\"1\" $smtp_out/>
      </label>
      <label>&nbsp;<br />
        <input name=\"action\" type=\"hidden\" value=\"search\" />
        <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
        $ext
      </label>
      <p class=\"CL\" />
    </div>
  </form>";

  return $block;
}


/**
 * Display the Mail server search result
 *
 * @param mixed $params : mailserver search criteria
 * @return Search result display
 */
function dis_mailserver_search_list($params) {
  global $l_found, $l_no_found, $display, $obm;

  $prefs = get_display_pref($obm['uid'], 'mailserver');
  $obm_q = run_query_mailserver_search($params);

  $nb = $obm_q->num_rows_total();
  if ($nb == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_ok_msg("$nb $l_found");
    $block = html_mailserver_search_list($obm_q, $prefs, $params);
  }

  return $block;
}


/**
 * XHTML Display the Mail server search result
 *
 * @param mixed $obm_q  : database object with mailserver list
 * @param mixed $pref_q : database object with user display preferences
 * @param mixed $params : mailserver parameters
 * @return XHTML Display Search result
 */
function html_mailserver_search_list($obm_q, $pref_q, $mailserver) {
  global $display, $l_found, $l_close, $module;

  $popup = $params['popup'];

  if ($popup) {
    $ext_action = $params['ext_action'];
    $ext_url = $params['ext_url'];
    $ext_target = $params['ext_target'];
    $ext_widget = $params['ext_widget'];
    $ext_widget_text = $params['ext_widget_text'];
    $ext_id = $params['ext_id'];
    $ext_title = urlencode(stripslashes($params['ext_title']));
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_title=$ext_title&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text";
  }

  $host_name = urlencode(stripslashes($params['host_name']));
  $host_ip = urlencode(stripslashes($params['host_ip']));
  $imap = $params['imap'];
  $smtp_in = $params['smtp_in'];
  $smtp_out = $params['smtp_out'];

  $url = url_prepare("mailserver_index.php?action=search&amp;tf_host_name=$host_namet&amp;tf_host_ip=$host_ip&amp;cba_imap=$imap&amp;cba_smtp_in=$smtp_in&amp;cba_smtp_out=$smtp_out$url_ext");

  $dis_m = new OBM_DISPLAY('DATA', $pref_q, $module);

  if ($popup) {
    if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $dis_m->display_ext = 'get_id';
    }
    $dis_m->data_form_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $dis_m->data_set = $obm_q;
  $dis_m->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_m->display('dis_data_mailserver');

  return $block;
}


/**
 * Display Mail server Detail
 *
 * @param mixed $params[] : mailserver parameters
 * @return Display block
 */
function dis_mailserver_consult($params) {
  global $display, $l_err_reference;

  $m_q = run_query_mailserver_detail($params['mailserver_id']);
  if ($m_q->num_rows() == 1) {
    $block = html_mailserver_consult($m_q);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

  return $block;
}


/**
 * XHTML Display Mail Server Detail
 *
 * @param mixed $m_q : mailserver database data
 * @return XHTML Display block
 */
function html_mailserver_consult($m_q) {
  global $display, $cgp_use, $c_yes, $c_no;
  global $l_mailserver, $l_host, $l_ip, $l_imap, $l_smtp_in, $l_smtp_out;

  $id = $m_q->f('mailserver_id');
  $host_name = $m_q->f('host_name');
  $host_id = $m_q->f('host_id');
  $host_ip = $m_q->f('host_ip');
  $imap = $m_q->f('mailserver_imap');
  $smtp_in = $m_q->f('mailserver_smtp_in');
  $smtp_out = $m_q->f('mailserver_smtp_out');

  // Formatage des informations de la BD
  if ($imap == '1') $imap = $c_yes; else $imap = $c_no;
  if ($smtp_in == '1') $smtp_in = $c_yes; else $smtp_in = $c_no;
  if ($smtp_out == '1') $smtp_out = $c_yes; else $smtp_out = $c_no;

  $display['detailInfo'] = display_record_info($m_q);
  $display['title'] = "<h1 class=\"title\">$l_mailserver : $host_name</h1>";

  $block = "
    <div class=\"detail extras\">
    <h1>$l_mailserver</h1>

    <table>
    <tr>
      <th>$l_host</th>
      <td><a href=\"$path/host/host_index.php?action=detailconsult&amp;host_id=$host_id\">$host_name</a></td>
    </tr>
    <tr>
      <th>$l_ip</th>
      <td>$host_ip</td>
    </tr>
    <tr>
      <th>$l_imap</th>
      <td>$imap</td>
    </tr>
    <tr>
      <th>$l_smtp_in</th>
      <td>$smtp_in</td>
    </tr>
    <tr>
      <th>$l_smtp_out</th>
      <td>$smtp_out</td>
    </tr>
    </table>
";

  return $block;
}


/**
 * Display MailServer Form
 *
 * @param mixed $action action called
 * @param mixed $params[] : default values
 * @param mixed $field  : field on error
 * @return string XHTML mailserver form
 */
function dis_mailserver_form($action, $params, $field='') {
  global $display, $l_err_reference;

  $m_id = $params['mailserver_id'];

  if ($m_id > 0) {
    $m_q = run_query_mailserver_detail($m_id);
    if ($m_q->num_rows() != 1) {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  }
  $block = html_mailserver_form($action, $m_q, $params);

  return $block;
}


/**
 * XHTML Display Mail server Form
 *
 * @param mixed $action : action called
 * @param mixed $m_q    : company database result 
 * @param mixed $params : default values
 * @param mixed $field  : field on error
 * @return string XHTML mailserver form
 */
function html_mailserver_form($action, $m_q, $params, $field='') {
  global $display, $obm, $ico_host;
  global $l_mailserver, $l_name, $l_host, $l_imap, $l_smtp_in, $l_smtp_out;
  global $l_insert, $l_update, $l_header_new;

  // if update mode and first display values are taken from database
  if ($action == 'detailupdate') {
    $id = $m_q->f('mailserver_id');
    $host_id = $m_q->f('host_id');
    $host_name = $m_q->f('host_name');
    $imap = $m_q->f('mailserver_imap');
    $smtp_in = $m_q->f('mailserver_smtp_in');
    $smtp_out = $m_q->f('mailserver_smtp_out');
  } else if ($action == 'new') {
    $dis_title = $l_header_new;
  }
  
  // If parameters have been given, they supercede the default action value
  if (isset($params['mailserver_id'])) { $id = $params['mailserver_id']; }
  if (isset($params['host_id'])) { $host_id = $params['host_id']; }
  if (isset($params['host_name'])) { $host_name = $params['host_name']; }
  if (isset($params['imap'])) { $imap = $params['imap']; }
  if (isset($params['smtp_in'])) { $smtp_in = $params['smtp_in']; }
  if (isset($params['smtp_out'])) { $smtp_out = $params['smtp_out']; }

  $imap = ($imap == '1' ? 'checked="checked" ' : '');
  $smtp_in = ($smtp_in == '1' ? 'checked="checked" ' : '');
  $smtp_out = ($smtp_out == '1' ? 'checked="checked" ' : '');

  if (($action == 'detailupdate') || ($action == 'update')) {
    // name + IP XXXXXXX? 
  }

  // Buttons
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button = "
    <!-- Update button -->
    <input type=\"hidden\" name=\"mailserver_id\" id=\"mailserver_id\" value=\"$id\" />
    <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
    <input type=\"submit\" value=\"$l_update\" />";
  } elseif (($action == 'new') || ($action == 'insert')) {
    $dis_button = "
    <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
    <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display['title'] = "<h1 class=\"title\">$l_mailserver : $dis_title</h1>";

  // --- HTML Template --------------------------------------------------------

  if ($action == 'new' || $action == 'insert') {
    $dis_host = "<a href=\"". url_prepare("$path/host/host_index.php?action=detailconsult&amp;host_id=$host_id") . "\">$host_name</a>
      <input type=\"hidden\" name=\"host_id\" value=\"$host_id\" />
      <a href=\"\" onclick=\"popup('$path/host/host_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.host_id&amp;ext_widget_text=f_entity.host_name','Host'); return false;\">
      <img src=\"$ico_host\" alt=\"\" /></a>
      <input type=\"text\" name=\"host_name\" size=\"16\" value=\"$host_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />";
  } else {
    $dis_host = $host_name;
  }

  $block = "
  <form method=\"post\" name=\"f_entity\"
    action=\"".url_prepare("mailserver_index.php")."\">
  <fieldset class=\"detail infos\">  
  <legend>$l_mailserver</legend>

  <table>
  <tr>
    <th>$l_host :</th>
    <td>$dis_host</td>
  </tr>
  <tr>
    <th>$l_imap :</th>
    <td><input type=\"checkbox\" name=\"cba_imap\" value=\"1\" $imap/></td>
  </tr>
  <tr>
    <th>$l_smtp_in :</th>
    <td><input type=\"checkbox\" name=\"cba_smtp_in\" value=\"1\" $smtp_in/></td>
  </tr>
  <tr>
    <th>$l_smtp_out :</th>
    <td><input type=\"checkbox\" name=\"cba_smtp_out\" value=\"1\" $smtp_out/></td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"buttons\">$dis_button</fieldset>
  </form>";

  return $block;
}


/**
 * Display Mail Server Delete
 *
 * @param mixed $id mailserver id
 **/
function dis_can_delete_mailserver($id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display, $path;

  $dis_back = "<a href=\"$path/mailserver/mailserver_index.php?action=detailconsult&amp;mailserver_id=$id\">$l_back</a>";

  $dis_delete = "
    <a href=\"$path/mailserver/mailserver_index.php?action=delete&amp;mailserver_id=$id\">
    $l_delete 
    </a>";

  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"buttons\">
      $dis_delete
      $dis_back
    </div>";

  return $block;
}
