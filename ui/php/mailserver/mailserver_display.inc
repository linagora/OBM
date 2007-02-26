<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailserver_display.php                                       //
//     - Desc : Mail Server Display File                                     //
// 2007-02-08 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display Mail Server List
///////////////////////////////////////////////////////////////////////////////
function dis_mailserver_list($ms_q) {
  global $l_mailserver, $l_relayhost, $l_ip, $path;
  
  while ($ms_q->next_record()) {
    $id = $ms_q->f("mailserver_id");
    $name = $ms_q->f("mailserver_name");
    $ip = $ms_q->f("mailserver_ip");
    $rname = $ms_q->f("relayhost_name");
    $rip = $ms_q->f("relayhost_ip");
    ($i++%2==0)?$class="pair":$class="";
    $dis_mailserver .= "
    <tr class=\"$class\">
      <td><a href=\"$path/mailserver/mailserver_index.php?action=detailconsult&amp;id=$id\">$name</a></td>
      <td>$ip</td> 
      <td>$rname</a></td>
      <td>$rip</td>
    </tr>";
  }
  $block = "
  <div class=\"result\">
  <table>
  <thead>
  <tr>
  <th>$l_mailserver</th><th>$l_mailserver $l_ip</th><th>$l_relayhost</th><th>$l_relayhost $l_ip</th>
  </tr>
  </thead>
  <tbody>
  $dis_mailserver
  </tbody>
  </table>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mail Server Form
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result 
//   - $mailserver[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_mailserver_form($action, $mailserver, $ms_q=null, $net_q=null) {
  global $display, $cgp_hide, $l_mailserver;
  global $l_networks, $l_host, $l_ip,$l_none,$l_update,$l_insert;
  global $ico_add, $ico_delete,$l_relayhost,$php_regexp_network,$php_regexp_ip;

  if ($action == "detailupdate") {
    $id = $ms_q->f("mailserver_id");
    $host_id = $ms_q->f("mailserver_host_id");
    $relayhost_id = $ms_q->f("mailserver_relayhost_id");
    while($net_q->next_record()) {
      if ($net_q->f("mailservernetwork_host_id") == $host_id) {
        $dis_mailserver_networks .= "
      <tr>
        <td><img src=\"$ico_delete\" alt=\"Remove\" onclick=\"deleteIpRow(this)\"/></td>
        <td>
        <input type=\"text\" onkeyup=\"checkIp(this)\" onblur=\"transformIp(this)\"  name=\"mailserver_networks[]\" value=\"".$net_q->f("mailservernetwork_ip")."\" />
        </td>
      </tr>";
      } else {
        $dis_relayhost_networks .= "
      <tr>
        <td><img src=\"$ico_delete\" alt=\"Remove\" onclick=\"deleteIpRow(this)\"/></td>
        <td>
        <input type=\"text\" onkeyup=\"checkIp(this)\" onblur=\"transformIp(this)\" name=\"relayhost_networks[]\" value=\"".$net_q->f("mailservernetwork_ip")."\" />
        </td>
      </tr>
";

      }
    }
  }
  if (isset($mailserver["id"])) $id = $mailserver["id"] ;
  if (isset($mailserver["host_id"])) $host_id = $mailserver["host_id"] ;
  if (isset($mailserver["relayhost_id"])) $relayhost_id = $mailserver["relayhost_id"];
  if (isset($mailserver["mailserver_networks"])) {
    foreach($mailserver["mailserver_networks"] as $network) {
      if ($network != "") {
        $dis_mailserver_networks .= "
      <tr>
        <td><img src=\"$ico_delete\" alt=\"Remove\" onclick=\"deleteIpRow(this)\"/></td>
        <td>
        <input type=\"text\" onkeyup=\"checkIp(this)\" onblur=\"transformIp(this)\"  name=\"mailserver_networks[]\" value=\"$network\" />
        </td>
      </tr>";
      }
    }
  }
  if (isset($mailserver["relayhost_networks"])) {
    foreach($mailserver["relayhost_networks"] as $network) {
      if ($network != "") {
        $dis_relayhost_networks .= "
      <tr>
        <td><img src=\"$ico_delete\" alt=\"Remove\" onclick=\"deleteIpRow(this)\"/></td>
        <td>
        <input type=\"text\" onkeyup=\"checkIp(this)\" onblur=\"transformIp(this)\" name=\"relayhost_networks[]\" value=\"$network\" />
        </td>
      </tr>";
      }
    }
  }
  // UPDATE 
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // INSERT
  } elseif (($action=="new") || ($action=="insert")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }  
  $host_q = run_query_mailserver_server_host($id);
  $ms_select = "<select name=\"host_id\">";
  while($host_q->next_record()) {
    if ($host_id == $host_q->f("host_id")) {
      $selected = "selected=\"selected\"";
      $host_label = $host_q->f("host_name");
    } else {
      $selected="";
    }   
    $ms_select .= "<option $selected value=\"".$host_q->f("host_id")."\">".$host_q->f("host_name")."</option>";
  }
  $ms_select .= "</select>";

  $host_q = run_query_mailserver_server_host($relayhost_id);
  $rh_select = "<select name=\"relayhost_id\">";
  $rh_select .= "<option value=\"\">$l_none</option>";
  while($host_q->next_record()) {
    ($relayhost_id == $host_q->f("host_id"))?$selected = "selected=\"selected\"":$selected="";
    $rh_select .= "<option $selected value=\"".$host_q->f("host_id")."\">".$host_q->f("host_name")."</option>";
  }
  $rh_select .= "</select>";

  if (isset($id) && !check_can_change_mailserver($id)) {
    $dis_host = "
    <tr>
      <th>$l_host</th>
      <td>$host_label</td>
    </tr>";
      
  } else {
    $dis_host = "
    <tr>
      <th>$l_host</th>
      <td>$ms_select</td>
    </tr>";
  }

  $block = "
    <form method=\"get\" name=\"f_entity\"
    onsubmit=\"\" 
    action=\"mailserver_index.php\">

    <fieldset class=\"detail infos\">
    <legend>$l_mailserver</legend>

    <table>
    $dis_host
    </table>
    <h1>$l_networks</h1>
    <table>
    <thead>    
    <tr>
    <td><img src=\"$ico_add\" alt=\"Add\" onclick=\"addIpRow('mailserver')\"/></td>
    <td>$l_ip</td>
    </tr>    
    </thead>    
    $dis_mailserver_networks    
    <tbody id=\"mailserverIpTable\">
    </tbody>
    </table>
    </fieldset>

    <fieldset class=\"detail infos\">
    <legend>$l_relayhost</legend>
    <table>
    <tr>
      <th>$l_relayhost</th>
      <td>$rh_select</td>
    </tr>   
    </table>
    <h1>$l_networks</h1>
    <table>
    <thead>          
    <tr>
    <td><img src=\"$ico_add\" alt=\"Add\" onclick=\"addIpRow('relayhost')\"/></td>
    <td>$l_ip</td>
    </tr>
    </thead>
    $dis_relayhost_networks
    <tbody id=\"relayhostIpTable\">
    </tbody>
    </table>
    </fieldset>
    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>    
    </form>

    <script type=\"text/javascript\">
      function addIpRow(name) {
        ip = \$(name + 'IpTable');
        ip.adopt(new Element('tr')
           .adopt(new Element('td')
            .adopt(new Element('img')
             .setProperty('src','$ico_delete')
             .setProperty('onclick','deleteIpRow(this)')
             .setProperty('alt','Remove')))
           .adopt(new Element('td')
            .adopt(new Element('input')
             .setProperty('name',name + '_networks[]')
             .setProperty('onkeyup','checkIp(this)')
             .setProperty('onblur','transformIp(this)')
             .setProperty('type','text')))
           );
      }
      
      function deleteIpRow(link) {
        line = link.parentNode.parentNode;
        line.remove();
      }

      function checkIp(ipField) {
        \$(ipField);
        if(ipField.value == '' || ipField.value.match($php_regexp_network)) {
          ipField.removeClassName('error');
          return true;
        } else {
          ipField.addClassName('error');
          return false;
        }
      }

      function transformIp(ipField) {
        \$(ipField);
        if(ipField.value == '' || ipField.value.match($php_regexp_network)) {
          ipField.removeClassName('error');
        } else {
          ipField.addClassName('error');
          return false;
        }
        if(ipField.value.match($php_regexp_ip) && ipField.value.match(/\.(0)*$/)) {;
          ipField.value = ipField.value + '/24';
        }
        return true;
      }
    </script>
";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mail Server Consult
///////////////////////////////////////////////////////////////////////////////
function dis_mailserver_consult($ms_q,$net_q) {
  global $l_mailserver, $l_relayhost;
  global $l_network, $l_host, $l_ip,$l_none,$l_update,$l_insert;

  $relayhost = $ms_q->f("relayhost_name");
  $relayhost_ip = $ms_q->f("relayhost_ip");
  $relayhost_host_id = $ms_q->f("mailserver_relayhost_id");
  $mailserver = $ms_q->f("mailserver_name");
  $mailserver_ip = $ms_q->f("mailserver_ip");
  $mailserver_host_id = $ms_q->f("mailserver_host_id");

  while($net_q->next_record()) {
    if ($net_q->f("mailservernetwork_host_id") == $mailserver_host_id) {
      $dis_mailserver_networks .= "
    <tr>
      <th>$l_network</th>
      <td>
      ".$net_q->f("mailservernetwork_ip")."
      </td>
    </tr>";
    } else {
      $dis_relayhost_networks .= "
    <tr>
      <th>$l_network</th>
      <td>
      ".$net_q->f("mailservernetwork_ip")."
      </td>
    </tr>";        
    }
  }
  $block = "
    <div class=\"detail infos\">
    <h1>$l_mailserver</h1>
    <table>
    <tr>
    <th>$l_host</th>
    <td><a href=\"$path/host/host_index.php?action=detailconsult&id=$mailserver_host_id\">$mailserver</a> ($mailserver_ip)</td>
    </tr>
    $dis_mailserver_networks
    </table>
    </div>";
  if ($relayhost_host_id) {
    $block .="
    <div class=\"detail infos\">
    <h1>$l_relayhost</h1>
    <table>
    <tr>
    <th>$l_host</th>
    <td><a href=\"$path/host/host_index.php?action=detailconsult&id=$relayhost_host_id\">$relayhost</a> ($relayhost_ip)</td>
    </tr>
    $dis_relayhost_networks
    </table>
    </div>
";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Mail Server Delete
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_mailserver($id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display, $path;

  $dis_back = "<a href=\"$path/mailserver/mailserver_index.php?action=detailconsult&amp;id=$id\">$l_back</a>";

  $dis_delete = "
    <a href=\"$path/mailserver/mailserver_index.php?action=delete&amp;id=$id\">
    $l_delete 
    </a>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"buttons\">
      $dis_delete
      $dis_back
    </div>";

  return $block;
}
