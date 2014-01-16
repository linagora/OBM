<script language="php">
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : report_display.php                                           //
//     - Desc : Report panel Display functions File                          //
// 2009-04-07 Benoît Caudesaygues                                            //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display the help screen
// Parameters:
//   - $mode   : 'txt' ou 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_report_help($mode) {

  if ($mode == 'html')
    echo "<table class=\"details\">
      <tr>
        <td><pre>";

  include ('report_help.inc');

  if ($mode == 'html')
    echo "</pre></td>
      </tr>
      </table>";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display Report panel Consultation
// Parameters:
//   - $sys_q     : system database result
///////////////////////////////////////////////////////////////////////////////
function html_report_consult($mode) {
  global $l_report,$l_valid_report, $l_all_services, $l_service;
  global $report, $service, $l_choose_report;
  

  $display["title"] = $l_report;
  $i=1;
  $types=get_report_kind($report);
  foreach($report as $key=>$opt){
    $type = $types[$key];
    $option .=  "<option id=\"opt_$i\" type=\"$type\" value=\"$opt\"> ".$GLOBALS['l_'.$key.'_report']." </option>";
    $i++;
  }

  if(is_array($service)) {
    foreach($service as $val){
      $service_option .= "<option id=\"serv_$i\" value=\"$val\"> $val </option>"; 
    }
  } 

  $block = "
    <form method=\"post\" name=\"f_entity\" action=\"" . url_prepare('report_index.php?mode=html') . "\">
    <fieldset class=\"detail infos\">
    <legend>$l_report</legend>

    <select id=\"sel_type\" onchange=\"display_js(this.options[this.selectedIndex])\" name=\"sel_type\">
    <option id=\"opt_choix\" value=\"\">  $l_choose_report </option>
    $option
    </select>
    </fieldset>

    <fieldset id='field_serv' style='display:none' class=\"detail infos\">
      <legend>$l_service</legend>
      <select id=\"sel_service\" onchange=\"\" name=\"sel_service\">
      <option id=\"serv_choix\" value=\"\">  $l_all_services </option>
      $service_option
      </select>
    </fieldset>
    
    <fieldset class=\"buttons\">
      <input type=\"hidden\" value=\"$mode\" name=\"mode\"/>
      <input type=\"hidden\" value=\"execute\" name=\"action\"/>
      <input type=\"submit\" value=\"$l_valid_report\"/>
    </fieldset>
    </form>

    <script type='text/javascript'>
      function display_js(select) {
        var type = $(select).get('type');
        if(type == 'user') {
          $('field_serv').set('style','');
        } else {
          $('field_serv').set('style','display:none');
        }
      }
    </script>
   ";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the search form for data module
// Parameters:
//   - $mode     : 'txt' ou 'html'
///////////////////////////////////////////////////////////////////////////////
function dis_report_index($mode) {

  switch ($mode) {
  case 'txt':
    echo "try -h to help\n";
    break;
  case 'html':
    $block =  html_report_consult($mode);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

  return $block;
}

/**
 * dis_execute_report run the execution of the report choose 
 * 
 * @param mixed $reportType 
 * @access public
 * @return void
 */
function dis_execute_report($reportType) {
  global $l_err_file, $obm;

  if(isset($reportType) && $reportType != ''){
    require_once "obminclude/of/report/command/$reportType.php";
    //Stato_Mailer::setDefaultTransport(new Stato_FileTransport());
    $report = new $reportType();
    $report->doIt();
  }
}

/**
 * dis_execute_report run the execution of the report choose 
 * 
 * @param mixed $reportType 
 * @access public
 * @return void
 */
function dis_list_report($mode, $report, $verbose = false) {
  global $l_err_file, $obm;
  $blocl = '';
  switch($mode)  {
  case 'txt':
    foreach($report as $key => $klass) {
      echo "Name :        $key\n";
      if($verbose) {
        echo 'PHP Class :   '. $klass."\n";
        echo 'Description : '.($GLOBALS['l_'.$key.'_report'])."\n";
        echo "\n";
      }
    }
    break;
  case 'html':
    $block =  html_report_consult($mode);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }


  if(isset($reportType) && $reportType != ''){
    require_once "obminclude/of/report/command/$reportType.php";
    //Stato_Mailer::setDefaultTransport(new Stato_FileTransport());
    $report = new $reportType();
    $report->doIt();
  }
}


/**
 * get_report_kind get the kind of the report 
 * 
 * @param mixed $report 
 * @access public
 * @return array 
 */
function get_report_kind($report) {
  
  $reportpath = '../../obminclude/of/report/command/';
  if(is_dir($reportpath)) {
    foreach($report as $key => $val) {
      require_once ($reportpath.$val.'.php');
        $kind[$key] = call_user_func(array($val, 'getKind'));
      }
  }
  return $kind;

}
</script>
