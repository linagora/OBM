<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : control_display.inc                                          //
//     - Desc : Control panel Display functions File                         //
// 2002-09-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Control panel Consultation
// Parameters:
//   - $sys_q     : system database result
//   - $domain_id : domain id to process
///////////////////////////////////////////////////////////////////////////////
function html_control_consult($domain_id="") {
  global $l_control, $l_system, $l_client, $l_model, $l_version;
  global $gp_client, $gp_model, $obm_version;
  global $l_network, $l_mail_domain;
  global $l_mail_mode, $l_relay, $l_server;
  global $l_global_stats, $l_private, $l_public, $l_archive;
  global $l_nb_user, $l_nb_group, $l_nb_mailshare, $l_disk_space;
  global $l_mail_stats, $l_mail_sent, $l_mail_recv, $l_mail_kb_sent, $l_mail_kb_recv;
  global $l_web_stats, $l_proxy_stats, $l_view_proxy_stats;
  global $l_maintenance, $l_sys_update, $l_allowed,$l_forbid;
  global $c_yes, $c_no, $c_virus_log;
  global $popup_width, $popup_height;
  global $obm, $cgp_use, $display;
  global $l_download_log;
  global $cgp_securinet;


  $display["title"] = "<div class=\"title\">$l_control</div>";

  if ($cgp_use["service"]["mail"]) {

    $domain = $obm["domain_name"];

    $mode_mail = ($mail_relay ? $l_relay : $l_server);
    if (! $mail_relay) {
      $mail_server = "";
    }
    if ($mail_relay) $c_mail_relay = "checked";
    if ($mail_server) $c_mail_server = "checked";

    $block_mail_info = "
    <div class=\"detail infos\">
    <h1>$l_network</h1>

    <table>
    <tr>
      <th>$l_mail_domain</th>
      <td>$domain</td>
    </tr>
    <tr>
      <th>$l_mail_mode</th>
      <td>$mode_mail</td>
    </tr>
    </table>

    </div>
";
  
    $mail_stats = get_mail_stats();
    $mail_sdate = $mail_stats["mail_start_date"];
    $mail_edate = $mail_stats["mail_end_date"];
    $mail_sent = $mail_stats["mail_sent"];
    $mail_recv = $mail_stats["mail_recv"];
    $mail_kb_sent = $mail_stats["mail_kb_sent"];
    $mail_kb_recv = $mail_stats["mail_kb_recv"];
      
    $block_mail_stats = "
    <div class=\"detail infos\">
    <h1>$l_mail_stats du $mail_sdate au $mail_edate</h1>

    <table>
    <tr>
      <th>$l_mail_sent</th>
      <td>$mail_sent</td>
    </tr>
    <tr>
      <th>$l_mail_recv</th>
      <td>$mail_recv</td>
    </tr>
    <tr>
      <th>$l_mail_kb_sent</th>
      <td>$mail_kb_sent</td>
    </tr>
    <tr>
      <th>$l_mail_kb_recv</th>
      <td>$mail_kb_recv</td>
    </tr>
    </table>
    </div>
";
  }

  $u = get_control_user_infos($domain_id);
  $g = get_control_group_infos($domain_id);
  $g_public = $g['nb'] - $g['nb_private'];
  $nb_mailshare = get_control_mailshare_infos($domain_id);
  $disk_use = get_sys_disk_use();
  $virus_stats = get_virus_stats();
  
  //  $system_update = get_update_state();
  $sys_update = ($system_update ? $c_yes : $c_no);

  if ($cgp_use["service"]["web_local"]) {
    $block_proxy_stats = display_proxy_stats();
  }

  if( $cgp_securinet ) {
    $block_virus_stats = display_virus_stats();
  }

  $block = "
    <div class=\"detail infos\">
    <h1>$l_system</h1>

    <table>
    <tr>
      <th>$l_client</th>
      <td>$gp_client</td>
    </tr>
    <tr>
      <th>$l_model</th>
      <td>$gp_model</td>
    </tr>
    <tr>
      <th>$l_version</th>
      <td>$obm_version</td>
    </tr>
    </table>
    </div>

    $block_mail_info
    
    <div class=\"detail extra\">
    <h1>$l_global_stats</h1>

    <table>
    <tr>
      <th>$l_nb_user ($l_archive)</th>
      <td>$u[nb] ($u[nb_archive])</td>
    </tr>
    <tr>
      <th>$l_nb_group ($l_public / $l_private)</th>
      <td>$g[nb] ($g_public / $g[nb_private])</td>
    </tr>
    <tr>
      <th>$l_nb_mailshare</th>
      <td>$nb_mailshare</td>
    </tr>
    <tr>
      <th>$l_disk_space</th>
      <td>$disk_use</td>
    </tr>
    </table>
    </div>

    $block_proxy_stats

    $block_mail_stats
    
    $block_virus_stats
    
    <div class=\"detail infos\">
    <h1>$l_maintenance</h1>

    <table>
    <tr>
      <th>$l_sys_update</th>
      <td>$sys_update</td>
    </tr>
    </table>
    </div>";

  return $block;
}


function display_virus_stats() {
  global $l_virus_stats, $l_virus_main_db_version, $l_virus_daily_db_version, $l_virus_detected, $l_this_month, $l_last_month;

  $virus_db_version = get_virus_db_version();
  $virus_daily_db_version = $virus_db_version["daily"];
  $virus_main_db_version = $virus_db_version["main"];
  $virus_db_lastupdate = $virus_db_version["lastupdate"];
  $virus_last_month = $virus_stats["virus_last_month_total"];
  $virus_this_month = $virus_stats["virus_current_month_total"];
  if ($virus_last_month > 0) {
    $download_log_last_month = "( <a href=\"$path/control/control_index.php?action=download_log&amp;log_name=$c_virus_log.0\">$l_download_log</a>)";
  }
  if ($virus_this_month > 0) {
    $download_log_this_month = "( <a href=\"$path/control/control_index.php?action=download_log&amp;log_name=$c_virus_log\">$l_download_log</a>)";
  }


  $block = "
    <div class=\"detail infos\">
    <h1>$l_virus_stats</h1>

    <table>
    <tr>
      <th colspan=\"2\">$virus_db_lastupdate</th>
    </tr>
    <tr>
      <th>$l_virus_main_db_version</th>
      <td>$virus_main_db_version</td>
    </tr>
    <tr>
      <th>$l_virus_daily_db_version</th>
      <td>$virus_daily_db_version</td>
    </tr>
    <tr>
      <th>$l_virus_detected $l_this_month</th>
      <td>$virus_this_month $download_log_this_month</td>
    </tr>
    <tr>
      <th>$l_virus_detected $l_last_month</th>
      <td>$virus_last_month $download_log_last_month</td>
    </tr>
    </table>
    </div>";

  return $block;
}


function display_proxy_stats() {
  global $proxystatspath, $l_download_csv;
  global $l_daily_csv_proxy_stats;
  global $l_weekly_csv_proxy_stats,$l_change_password;
  global $l_monthly_csv_proxy_stats,$path,$l_validate;
  global $l_web_stats, $l_proxy_stats,  $l_view_proxy_stats;

  $block = "
    <div class=\"detail infos\">
    <h1>$l_web_stats</h1>

    <table>
    <tr>
      <th>$l_proxy_stats</th>
      <td>
        <a href=\"\" onclick=\"window.name='root';window.open('$proxystatspath/index.html','$l_proxy_stats','height=$popup_height,width=$popup_width,scrollbars=yes,toolbar=yes,menubar=yes');return false;\">
          $l_view_proxy_stats
        </a>
      </td>
    </tr>
    <tr>
      <th>$l_change_password</th>
      <form method=\"post\" action=\"$path/control/control_index.php\">
      <td> 
      <input type=\"password\" name=\"tf_ht_passwd\" value=\"\" />
      <input type=\"hidden\" name=\"action\" value=\"update_htpasswd\" />
      <input type=\"submit\" value=\"$l_validate\" />
      </td>
      </form>
    </tr>
";

  // CSV file downloads
  $block.="
    <tr>
      <th>$l_download_csv</th>
      <td>
        <a href=\"$proxystatspath/csv/daily/stats.csv\">
          $l_daily_csv_proxy_stats
        </a>
        &nbsp;|&nbsp;
        <a href=\"$proxystatspath/csv/weekly/stats.csv\">
          $l_weekly_csv_proxy_stats
        </a>
        &nbsp;|&nbsp;
        <a href=\"$proxystatspath/csv/monthly/stats.csv\">
          $l_monthly_csv_proxy_stats
        </a>
      </td>
    </tr>
    </table>
    </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
function dis_download_log($controle) {
  global $l_err_file;
  $log_name = $controle["log_name"];
  header("Content-Type: text/forcedownload");
  header("Content-Disposition: inline; filename=$log_name");
  if ($handle = @fopen("/var/log/aliamin/".$log_name, "rb")) {
    $contents = '';
    $i = 0;
    while (!feof($handle)) {
      $contents .= fread($handle, 8192);
    }
    fclose($handle); 
  } else {
    echo $l_err_file;
    exit;
  }
  echo "$contents";
  exit; 

}
</script>
