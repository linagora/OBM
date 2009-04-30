<script language="php">
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
    $option .=  "<option id=\"opt_$i\" type=\"$type\" value=\"$opt\"> $key </option>";
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
      <input type=\"hidden\" value=\"download_report\" name=\"action\"/>
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
 * dis_download_report run the execution of the report choose 
 * 
 * @param mixed $reportType 
 * @access public
 * @return void
 */
function dis_download_report($reportType) {
  global $l_err_file, $obm;

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
