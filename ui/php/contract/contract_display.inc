<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contract_display.php                                         //
//     - Desc : Contract Support Display File                                //
// 2002-01-08 : Mehdi RANDE                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
$fieldnames["contract_label"] = $l_label;
$fieldnames["contract_company_name"] = $l_contract_company;
$fieldnames["contract_number"] = $l_number;
$fieldnames["contract_dateexp"] = $l_contract_exp ;
$fieldnames["contracttype_label"] = $l_contract_contracttype;
$fieldnames["contract_techmanager"] = $l_technical_manager;
$fieldnames["contract_marketmanager"] = $l_marketing_manager;


///////////////////////////////////////////////////////////////////////////////
// Display Contract search form                                              //
// Parameters : 
//   - $contract[] : default form values
//     keys used      : 
//   - $usr_q      : DB result object (userobm list)
//   - $obm_q_type : DB result object (contract kind list)
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_form($contract, $usr_q, $obm_q_type) {
  global $l_number,$l_label_libelle,$l_company,$l_kind,$l_after,$l_before;
  global $l_find,$l_marketing_manager,$l_technical_manager; 
  global $c_all, $l_all;

  $p_label = $contract["label"];
  $comp = $contract["company_name"];
  $p_type_id = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $sel_market = $contract["market"];
  $sel_tech = $contract["tech"];
  $sel_manager = $contract["manager"];
  $p_num = $contract["number"]; 
 
  $dis_cont_typ  = "<select name=\"sel_type\">
    <option value=\"$c_all\">$l_all</option>";
  while($obm_q_type->next_record()) {
    $dis_cont_typ .= "\n<option value=\"".$obm_q_type->f("contracttype_id")."\"";
    if ($p_type_id == $obm_q_type->f("contracttype_id")) 
      $dis_cont_typ .= " selected=\"selected\"";
    $dis_cont_typ .= ">" . $obm_q_type->f("contracttype_label")."</option>";
  }
  $dis_cont_typ .= "</select>";

  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }

  $dis_resp_tec = "<select name=\"sel_manager\">
    <option value=\"$c_all\">$l_all</option>";
  while($usr_q->next_record()) {
    $dis_resp_tec .= "\n<option value=\"".$usr_q->f("userobm_id")."\"";
    if ($usr_q->f("userobm_id") == $sel_manager) 
      $dis_resp_tec .= " selected=\"selected\"";
    $dis_resp_tec .= ">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")." </option>";
  }
  $dis_resp_tec .="</select>";


  // --- HTML Template --------------------------------------------------------

  echo "
    <form method=\"get\" name=\"form_contract\" 
    onsubmit=\"if (check_search_form(this) == false) return false;
    else return true;\" action=\"".url_prepare("contract_index.php")."\">

    <table class=\"details\">
    <tr>
      <td class=\"detailHead\">$l_deal</td>
      <td>&nbsp;&nbsp;&nbsp;</td>

      <td>
      <table class=\"details\">   
      <tr>
        <td class=\"textLabelForm\">$l_number<br />
          <input type=\"text\" name=\"tf_num\" size=\"20\" value=\"$p_num\" />
        </td>
        <td>&nbsp;&nbsp;</td>
        <td class=\"textLabelForm\">$l_company<br />
          <input name=\"tf_company_name\" size=\"20\" maxlength=\"20\"
            value=\"$comp\" /></td>
        <td>&nbsp;&nbsp;</td>
        <td class=\"textLabelForm\">$l_after<br />
          <input name=\"tf_dateafter\" size=\"10\" value=\"$p_dateafter\" />
        </td>
        <td>&nbsp;&nbsp;</td>
        <td class=\"textLabelForm\">$l_before<br />
          <input type=\"text\" name=\"tf_datebefore\" size=\"10\"
            value=\"$p_datebefore\" /></td>
      </tr>
      </table>
      <table class=\"details\">
      <tr>
        <td class=\"textLabelForm\">$l_label_libelle<br />
          <input type=\"text\" name=\"tf_label\" size=\"20\"
            value=\"$p_label\" /></td>
        <td>&nbsp;&nbsp;</td>
        <td class=\"textLabelForm\">&nbsp;&nbsp;$l_kind<br />
          $dis_cont_typ</td>
        <td>&nbsp;&nbsp;</td>
        <td class=\"textLabelForm\">$l_technical_manager<br />
          $dis_resp_tec</td>
      </tr>
      </table>

      </td>
      <td>
        <input name=\"action\" type=\"hidden\" value=\"search\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      </td>
    </tr>
    </table>
    </form>
    <hr />";
}


///////////////////////////////////////////////////////////////////////////////
// display the contract search result                                        //
// parameters:
//   - $contract[] : contract search criteria
//     keys used   : label,...
///////////////////////////////////////////////////////////////////////////////
function dis_contract_search_list($contract) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "contract");
  $obm_q = run_query_search($contract, $new_order, $order_dir);
  $nb_contract = $obm_q->num_rows();
  if ($nb_contract == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_contract_search_list($obm_q,$pref_q,$nb_contract,$contract);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Contract Search result                                   //
// Parameters :
//   - $contract_q  : contracts list
//   - $pref_q      : fields to display in the contracts list
//   - $nb_contract : nb contract found
//   - $contract[]  : contract search criteria
//     keys used    : label, type, dateafter, datebefore, companyname, market
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_list($contract_q,$obm_q_display_options,$nb_contract,$contract) {
  global $l_found;

  $label = $contract["label"];
  $type = $contract["type"];
  $dateafter = $contract["dateafter"];
  $datebefore = $contract["datebefore"];
  $company = $contract["companyname"];
  $market = $contract["market"];
  $tech = $contract["tech"];
  $num = $contract["number"]; 

  $url = url_prepare("contract_index.php?action=search&amp;tf_label=$label&amp;tf_soc=$company&amp;sel_market=$market&amp;sel_type=$type&amp;tf_num=$num&amp;sel_tech=$tech&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore");

  $contract_q->seek($first_row);

  $dis_contracts_list = new obm_display("DATA", $obm_q_display_options);
  $dis_contracts_list->data_set = $contract_q;
  $dis_contracts_list->data_url = $url;
  $dis_contracts_list->data_header = "both";

  // --- html template --------------------------------------------------------

  display_info_msg($nb_contract." ".$l_found);
  $dis_contracts_list->display() ;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Consult                                                  //
// Parameters:
//   - $contract_q : information about the contract
///////////////////////////////////////////////////////////////////////////////
function html_contract_consult($contract_q) {
  global $path, $set_theme;
  global $ico_company,$ico_contact,$ico_incident, $ico_deal;
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_clause,$l_marketing_manager,$l_technical_manager,$l_last_update,$l_comment,$l_at,$l_number,$l_dateexp,$l_client_manager;
  global $l_incident_list,$l_incident_new, $l_contract,$l_contract_company;

  $id = $contract_q->f("contract_id");
  $label = $contract_q->f("contract_label");
  $number = $contract_q->f("contract_number");
  $market = $contract_q->f("lname1") . " " . $contract_q->f("fname1");
  $tech = $contract_q->f("lname2") . " " . $contract_q->f("fname2");
  $com = nl2br($contract_q->f("contract_comment"));
  $clause = nl2br($contract_q->f("contract_clause"));
  $datebegin = date_format($contract_q->f("datebegin"));
  $dateexp = date_format($contract_q->f("dateexp"));
  $dateu = date_format($contract_q->f("timeupdate"));
  $timeu = date("H:i:s",$contract_q->f("timeupdate"));
  $comp_id = $contract_q->f("contract_company_id");
  $deal_id = $contract_q->f("contract_deal_id");
  $deal_label = $contract_q->f("deal_label");
  $type = $contract_q->f("contracttype_label");
  $comp_id = $contract_q->f("contract_company_id");
  $comp_name = $contract_q->f("company_name");
  $comp_address1 = $contract_q->f("company_address1");
  $comp_zip = $contract_q->f("company_zipcode");
  $comp_town = $contract_q->f("company_town");
  $con1_id = $contract_q->f("contract_contact1_id");
  $con2_id = $contract_q->f("contract_contact2_id");

  $con1 = $contract_q->f("clname1") . " " . $contract_q->f("cfname1") .
          " - " . $contract_q->f("cphone1"); 
  $con2 = $contract_q->f("clname2") . " " . $contract_q->f("cfname2") .
          " - " . $contract_q->f("cphone2"); 

  $dis_update = "<form method=\"get\" name=\"form_contract_update\"
      action=\"".url_prepare("contract_index.php")."\">
    <input type=\"hidden\" name=\"action\" value=\"detailupdate\">
    <input type=\"hidden\" name=\"param_contract\" value=\"$id\">
    <input type=\"submit\" value=\"$l_update\">
    </form>";


  // --- HTML Template --------------------------------------------------------
//////////////////
//Liens Externes//
//////////////////
  echo "
    <table class=\"details\">
    <tr>
    <td rowspan=\"25\"> 
      <a href=\"".url_prepare("$path/incident/incident_index.php?action=search&amp;param_contract=$id")."\">
      <img border=\"0\"  src=\"/images/$set_theme/$ico_incident\"></a>
      <font size=\"-1\">
      <a href=\"" . url_prepare("$path/incident/incident_index.php?action=search&amp;param_contract=$id") . "\">
      $l_incident_list</a></font>
      <br />
      <a href=\"" . url_prepare("$path/incident/incident_index.php?action=new&amp;param_contract=$id") . "\">
      <img border=\"0\" src=\"/images/$set_theme/$ico_incident\"></A>
      <font size=\"-1\"><a href=\"" . url_prepare("$path/incident/incident_index.php?action=new&amp;param_contract=$id&tf_lcontract=$label") . "\">
      $l_incident_new</a></font>
      <br />
      <font size=\"-1\"><a href=\"" . url_prepare("$path/deal_index.php?action=detailconsult&param_deal=$deal_id") . "\">
      <img border=0  src=\"/images/$set_theme/$ico_deal\"></a></font>
      <font size=-1><a href=\"" . url_prepare("$path/deal/deal_index.php?action=detailconsult&param_deal=$deal_id") . "\">$deal_label</a></font>
      </td>
    </tr>";
//////////////////////
//Fin Liens Externes//
//////////////////////
echo "

 <tr>
   <td class=\"detailHead\" colspan=\"2\">$l_contract_company</td>
 </tr><tr>
  <td class=\"detailLabel\">$comp_name
   <a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\">
   <img src=\"/images/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
  </td>
  <td class=\"detailText\">$comp_address1
    <br />$comp_zip $comp_town
  </td>
 </tr><tr>
  <td class=\"detailLabel\">$l_client_manager 1</td>
  <td class=\"detailText\">$con1
     <a href=\"".
      url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con1_id") . "\">
      <img src=\"/images/$set_theme/$ico_contact\" alt=\"[Contact]\" /></a>
   </td> 
 </tr><tr>
  <td class=\"detailLabel\">$l_client_manager 2</td>
  <td class=\"detailText\">$con2
    <a href=\"".
      url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con2_id") . "\">
    <img src=\"/images/$set_theme/$ico_contact\" alt=\"[Contact]\" /></a></td>
 </tr><tr>
   <td class=\"detailHead\" colspan=\"2\">$l_contract</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_label</td>
  <td class=\"detailText\">$label</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_number</td>
  <td class=\"detailText\">$number</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_date_init</td>
  <td class=\"detailText\">$datebegin</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_dateexp</td>
  <td class=\"detailText\">$dateexp</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_kind</td>
  <td class=\"detailText\">$type</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_marketing_manager</td>
  <td class=\"detailText\">$market</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_technical_manager</td>
  <td class=\"detailText\">$tech</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_last_update</td>
  <td class=\"detailText\">$dateu $l_at $timeu</td>
 </tr><tr>
   <td class=\"detailHead\" colspan=\"2\"> $l_comment</td>
 </tr><tr>
   <td class=\"detailText\" colspan=\"2\">$com</td>
 </tr><tr> 
   <td class=\"detailHead\" colspan=\"2\">$l_clause</td>
 </tr><tr>
   <td class=\"detailText\" colspan=\"2\">$clause</td>
 </tr>
</table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Form                                                     //
// Parameters :
//   - $action     : action called
//   - $contract_q : DBO : information about the contract (null for new)
//   - $type_q     : DBO : contract type list
//   - $usr_q      : DBO : list of userobm
//   - $comp_q     : DBO : company infos (name, ad1, zip, town when new)
//   - $contact_q  : DBO : list of contact from the company 
//   - contract[]  : default or transmitted values
//     keys used   : all
///////////////////////////////////////////////////////////////////////////////
function html_contract_form($action,$contract_q,$type_q,$usr_q,$comp_q,$contact_q, $contract){
  // - Themes
  global $set_theme,$ico_mail,$ico_company,$ico_deal,$ico_contact;
  // -- Labels
  global $path,$l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_marketing_manager;
  global $l_technical_manager,$l_last_update;
  global $popup_height,$popup_width,$l_comment,$l_at,$l_number,$l_clause,$l_dateexp;
  global $l_contract_select_deal,$l_client_manager,$l_deal_label,$l_contract,$l_contract_company;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $contract_q->f("contract_id");
    $label = $contract_q->f("contract_label");
    $comp_id = $contract_q->f("contract_company_id");
    $number = $contract_q->f("contract_number");
    $datebegin = isodate_format($contract_q->f("datebegin"));
    $dateexp = isodate_format($contract_q->f("dateexp"));
    $type_id = $contract_q->f("contract_type_id");
    $con1_id = $contract_q->f("contract_contact1_id");
    $con2_id = $contract_q->f("contract_contact2_id");
    $market_i = $contract_q->f("contract_marketmanager_id");
    $tech = $contract_q->f("contract_techmanager_id");
    $clause = $contract_q->f("contract_clause");
    $comment = $contract_q->f("contract_comment");

    // comp info below could be directly fetched from contract_q query XXXXX
    $comp_name = $comp_q->f("company_name");
    $ad1 = $comp_q->f("company_address1");
    $zip = $comp_q->f("company_zipcode");
    $town = $comp_q->f("company_town");

    $deal_id = $contract_q->f("deal_id");
    $deal_label = $contract_q->f("deal_label");

  } else {
    // Values are taken from parameters
    $deal_id = $contract["deal_id"];
    $deal_label = $contract["deal_label"];
    $id = $contract["id"];
    $label = $contract["label"];
    $comp_id = $contract["company_id"];
    $number = $contract["number"];
    $datebegin = $contract["datebegin"];
    $dateexp = $contract["dateexp"];
    $type_id = $contract["type_id"];
    $con1_id = $contract["contact1"];
    $con2_id = $contract["contact2"];
    $market_i = $contract["market"];
    $tech = $contract["tech"];
    $clause = $contract["clause"];
    $comment = $contract["comment"];
    if ($action == "new") {
      $comp_name = $comp_q->f("company_name");
      $ad1 = $comp_q->f("company_address1");
      $zip = $comp_q->f("company_zipcode");
      $town = $comp_q->f("company_town");
      $market = $comp_q->f("company_marketingmanager_id");
    } else {
      $comp_name = $contract["company_name"];
      $ad1 = $contract["company_ad1"];
      $zip = $contract["company_zip"];
      $town = $contract["company_town"];
    }
  }
  
  $dis_sel_t="<select name=\"sel_type\">";
  while($type_q->next_record()) {
    $dis_sel_t.="\n<option value=\"".$type_q->f("contracttype_id")."\"";
    if ($type_id==$type_q->f("contracttype_id"))
       $dis_sel_t.=" selected=\"selected\""; 
    $dis_sel_t.=">".$type_q->f("contracttype_label")."</option>";
  }
  $dis_sel_t.="</select>";

  $dis_sel_respc="<select name=\"sel_market\">";
  while($usr_q->next_record()) {
    $dis_sel_respc.="\n<option value=\"".$usr_q->f("userobm_id")."\"";
    if ($usr_q->f("userobm_id") == $market_i) 
      $dis_sel_respc.=" selected=\"selected\"";
    $dis_sel_respc.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_respc.="</select>";
 
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }

  $dis_sel_respt="<select name=\"sel_tech\">";
  while($usr_q->next_record()) {
   $dis_sel_respt.="\n<option value=\"".$usr_q->f("userobm_id")."\"";
   if ($usr_q->f("userobm_id") == $tech) 
      $dis_sel_respt.=" selected=\"selected\"";
   $dis_sel_respt.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_respt.="</select>";

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con1="<select name=\"sel_con1\">";
  while($contact_q->next_record()) {
    $dis_sel_con1.="\n<option value=\"".$contact_q->f("contact_id")."\"";
    if ($contact_q->f("contact_id") == $con1_id) 
      $dis_sel_con1.=" selected=\"selected\"";
    $dis_sel_con1.=">". $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") .
    " - " . $contact_q->f("contact_phone")."</option>";
    }
    $dis_sel_con1.="</select>";

  if ($action == "detailupdate") {
    $dis_sel_con1.="<a href=\"".
    url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$con1_id)."\">";
    $dis_sel_con1.="<img src=\"/images/$set_theme/$ico_contact\" alt=\"[contact\" /></a>";    
    }

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con2="<select name=\"sel_con2\">";
  while($contact_q->next_record()) {
    $dis_sel_con2.="\n<option value=\"".$contact_q->f("contact_id")."\"";
    if ($contact_q->f("contact_id") == $con2_id) 
      $dis_sel_con2.=" selected=\"selected\"";
    $dis_sel_con2.=">". $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") .
    " - " . $contact_q->f("contact_phone")."</option>";
    }
    $dis_sel_con2.="</select>";

  if ($action == "detailupdate") {
    $dis_sel_con2.="<a href=\"".
    url_prepare("$path/contact_index.php?action=detailconsult&amp;param_contact=".$con2_id)."\">";
    $dis_sel_con2.="<img src=\"/images/$set_theme/$ico_contact\" alt=\"[contact\" /></a>";    
  }

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_sub = "
      <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp_name\" />
      <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
      <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
      <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
      <input type=\"hidden\" name=\"param_contract\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />
     ";
  $dis_delete = "<form method=\"get\" name=\"form_contract_delete\" onsubmit=\"if (confirm_del()) return true; else return false;\" action=\"".url_prepare("contract_index.php")."\">
      <table class=\"details\">
        <tr>
          <td>
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input type=\"hidden\" name=\"param_contract\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_delete\" />
          </td>
        </tr>
      </table>
      </form>";
  } else {
    $dis_sub = "
      <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp_name\" />
      <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
      <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
      <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  // --- html template --------------------------------------------------------

  echo "  
  <form method=\"post\" name=\"form_contract\"
    onsubmit=\"if (check_contract(this)) return true; else return false;\" 
    action=\"".url_prepare("contract_index.php")."\">
  <table class=\"details\">
   <tr>
    <td colspan=\"2\" class=\"detailHead\">$l_contract_company</td>
   </tr><tr>
    <td class=\"detailLabel\">
     $comp_name <a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id"). "\">
     <img src=\"/images/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
    </td>
    <td class=\"detailText\">
        $ad1
        <br />$zip $town
    </td>
   </tr><tr>
    <td class=\"detailLabel\">$l_client_manager 1</td>
    <td>$dis_sel_con1</td>
   </tr><tr>
    <td class=\"detailLabel\">$l_client_manager 2</td>
    <td>$dis_sel_con2</td>
   </tr><tr>
    <td colspan=\"2\" class=\"detailHead\">$l_contract</td>
   </tr><tr>
    <td class=\"detailLabel\">$l_label</td>
    <td><input name=\"tf_label\" type=\"text\" size=\"40\" value=\"$label\" /></td>
   </tr><tr>
    <td class=\"detailLabel\">$l_number</td>
    <td><input name=\"tf_num\" type=\"text\" size=\"20\" maxlength=\"40\" value=\"$number\" /></td>
   </tr><tr>
    <td class=\"detailLabel\">$l_date_init</td>
    <td><input name=\"tf_datebegin\" type=\"text\" size=\"10\" value=\"$datebegin\" /></td>
   </tr><tr>
    <td class=\"detailLabel\">$l_dateexp</td>
    <td><input name=\"tf_dateexp\" type=\"text\" size=\"10\" value=\"$dateexp\" /></td>
   </tr><tr>
    <td class=\"detailLabel\">$l_kind</td>
    <td>$dis_sel_t</td>
   </tr><tr>
    <td class=\"detailLabel\">$l_marketing_manager</td>
    <td>$dis_sel_respc</td>
   </tr><tr>
    <td class=\"detailLabel\">$l_technical_manager</td>
    <td>$dis_sel_respt</td>    
   </tr><tr>   
    <td class=\"detailLabel\">
      $l_deal_label 
      <input type=\"hidden\" name=\"param_deal\" value=\"$deal_id\" />
      <input type=\"hidden\" name=\"deal_new_id\" value=\"$c_new_id\" />
    </td>
    <td>
      <a href=\"".url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$deal_id")."\">$deal_label</a>
      <a href=\"\" onclick=\"window.open('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;title=".urlencode($l_contract_select_deal)."&amp;comp_id=$comp_id','contract','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"/images/$set_theme/$ico_deal\" alt=\"[Choose Deal]\" /></a>
      <input type=\"text\" name=\"deal_label\" value=\"$deal_label\" size =\"40\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
   </tr><tr>
    <td colspan=\"2\" class=\"detailHead\">$l_comment</td>
   </tr><tr>
    <td colspan=\"2\"><textarea name=\"ta_com\" rows=\"6\" cols=\"72\">$comment</textarea></td>
   </tr><tr>
    <td colspan=\"2\" class=\"detailHead\">$l_clause</td>
   </tr><tr>
    <td colspan=\"2\"><textarea name=\"ta_clause\" rows=\"6\" cols=\"72\">$clause</textarea></td>
   </tr><tr>
    <td colspan=\"2\" class=\"detailHead\">$dis_sub</td>
   </tr>
  </table>
  <table class=\"details\">
   <tr>
    <td>
     $dis_button
    </td>
   </tr>
  </table>
  </form>
    $dis_delete
   ";
}


///////////////////////////////////////////////////////////////////////////////
// Displays and check the contract links (and delete form if ok)             //
// Parameters:
//   - $p_id : contract id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $path, $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_incident, $l_link_incident_no;

  $delete_ok = true;

  // Links from Incident
  $obm_q = run_query_contract_incident_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linki ="<tr><td class=\"detailHead\">$l_link_incident</td></tr>";
    while ($obm_q->next_record()) {
      $iid = $obm_q->f("incident_id");
      $ilabel = $obm_q->f("incident_label");
      $display_linki .= "<tr><td class=\"detailText\"><a class=\"details\" href=\"" . url_prepare("$path/incident/incident_index.php?action=detailconsult&amp;param_incident=$iid") . "\">$ilabel</a></td>
      </tr>";
    }
  } else {
    $display_linki = "
      <tr><td class=\"detailHead\">$l_link_incident_no</td></tr>";
  }

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("contract_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_contract\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  if ($delete_ok == true) {
    display_ok_msg($l_can_delete);
    echo "<table class=\"details\">
          <tr><td>
            <table>
            <tr>
              <td class=\"detailHead\">" . dis_delete_form($p_id) . "</td>
              <td class=\"detailHead\">$dis_back</td>
            </tr>
            </table>
          </td></tr>";
  } else {
    display_warn_msg($l_cant_delete);
    echo "<table class=\"details\">
          <tr><td class=\"detailHead\">$dis_back</td></tr>";
  }
  echo "
   $display_linki
   </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : deal Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  $ret = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("contract_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_contract\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Contract Administration Forms
// Parametres:
//   - $type : DBO : Contract types list
///////////////////////////////////////////////////////////////////////////////
function html_contract_admin_form($type_q) {
  global $l_type_manage, $l_type_new, $l_type_exist;
  global $l_type_checkdelete, $l_type_delete, $l_type_update, $l_type_insert;
  global $l_type_insert_ok, $l_type_insert_error, $l_type_update_ok;
  global $l_type_update_error, $l_type_delete_error, $l_type_delete_ok;

  $dis_sel_type = "<select name=\"sel_type\" size=\"4\">";
  while ($type_q->next_record()) {
    $dis_sel_type .= "\n<option value=\"".$type_q->f("contracttype_id")."\">".$type_q->f("contracttype_label") .
           "</option>";
  }
  $dis_sel_type .= "</select>";

  // --- html template --------------------------------------------------------

  echo "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_type_manage</td>
    </tr>
    <tr>
      <td>
<!-- type delete section -->
        <table>
        <tr>
         <td colspan=\"3\">
     <form name=\"form_admin_type_delete\" method=\"post\" action=\"".url_prepare("contract_index.php")."\">
       <table>
        <tr>
          <td class=\"adminLabel\">$l_type_exist</td>
          <td class=\"adminLabel\">$dis_sel_type</td>
          <td>
            <input type=\"hidden\" name=\"action\" value=\"type_checklink\" />
            <input type=\"submit\" name=\"sub_type\" value=\"$l_type_checkdelete\"
              onclick=\"if (check_type_del(this.form)) return true;
              else return false;\" />
          </td>
         </tr>
        </table>
       </form>
      </td>
     </tr>
<!--  type update section -->
     <tr>
      <td colspan=\"2\" class=\"adminLabel\">
       <form name=\"form_admin_type_update\" method=\"post\" action=\"".url_prepare("contract_index.php")."\">
       <table>
         <tr>
          <td>
	    <input type=\"hidden\" name=\"sel_type\" value=\"\" />
	    <input type=\"hidden\" name=\"action\" value=\"type_update\" />
            <input type=\"text\" name=\"tf_type\" />
	  </td>
	  <td rowspan=\"2\">
	    <input type=\"submit\" name=\"sub_type\" value=\"$l_type_update\"
	    onclick=\"if (check_type_upd(this.form,document.form_admin_type_delete)) return true; else return false;\" />
	  </td>
	</tr>
       </table>
       </form>
      </td>
      <td>&nbsp;</td>
     </tr>
    </table>
<!-- new type section -->
   </td>
   <td>
    <form name=\"form_admin_type_new\" method=\"post\" action=\"".url_prepare("contract_index.php")."\">
     <table>
      <tr>
       <td class=\"adminLabel\">
         $l_type_new : <input type=\"text\" name=\"tf_type\" />
       </td>
      </tr><tr>
       <td class=\"adminLabel\">
         <input type=\"hidden\" name=\"action\" value=\"type_insert\" />
         <input type=\"submit\" name=\"sub_type\" value=\"$l_type_insert\"
         onclick=\"if (check_type_new(this.form)) return true;else return false;\" />
       </td>
      </tr>
     </table>
    </form>
   </td>
  </tr>
 </table>

";
}


///////////////////////////////////////////////////////////////////////////////
// Displays the type links                                                   //
// Parameters:
//   - $id : type id
///////////////////////////////////////////////////////////////////////////////
function dis_type_links($id) {
  global $l_type_link_contract, $l_type_can_delete, $l_type_cant_delete;
  global $l_type_delete, $l_back;

  $label = get_type_label($id);
  $obm_q = run_query_type_links($id);

  if ($obm_q->num_rows() > 0) {
    $dis_link = "
    <table class=\"details\">
    <tr>
      <td class=\"messageWarning\">$label : $l_type_cant_delete</td>
    </tr><tr>
      <td>&nbsp;</td>
    </tr><tr>
      <td class=\"detailHead\">$label : $l_type_link_contract</td>
    </tr>";

    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contract_id");
      $clabel = $obm_q->f("contract_label");
      $dis_link .= "
    <tr>
      <td class=\"detailText\">
        <a href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;param_contract=$cid") . "\">$clabel</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link = "
    <table class=\"details\">
    <tr>
      <td colspan=\"2\" class=\"messageOk\">$label : $l_type_can_delete</td>
    </tr><tr>
      <td>
        <form name=\"form_type_delete\".
          method=\"post\" action=\"" . url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"type_delete\" />
        <input type=\"hidden\" name=\"sel_type\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_type\" value=\"$l_type_delete\" />
        </form>
      </td><td>
        <form name=\"form_back\" method=\"post\"
          action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
      </td>
    </tr>
    </table>";
  }
  echo $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contract Display preference screen
// Parameters:
//   - $pref_q : DBO : Contract Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_contract_display_pref($pref_q) {
  global $l_contract_display;
  
  $dis_pref = new obm_display("PREFERENCES", $pref_q);
  $dis_pref->display_entity = "contract"; 
  $dis_pref->pref_title = $l_contract_display;
  $dis_pref->pref_dis_help = 1;

  // --- html template --------------------------------------------------------

  $dis_pref->display();
}


///////////////////////////////////////////////////////////////////////////////
// display contract select form                                              //
// parameters:
//   - $comp_q : company database result
//   - $title  : title to be displayed
//   - $url    : url to call with parameter company
///////////////////////////////////////////////////////////////////////////////
function html_select_contract($con_q, $title, $url="") {
  global $l_select_contract, $l_close;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }


  $display_cont = "
    <select name=\"param_contract\" size=\"10\" style=\"font-family:fixed,courier\">";

  while($con_q->next_record()) {
    $id = $con_q->f("contract_id");
    $name = $con_q->f("contract_label");
    $company = $con_q->f("company_name");
    $display_cont .= "\n<option value=\"$id\">$company - $name</option> ";
  }
   $display_cont.="</select>";

  // --- html template --------------------------------------------------------
  echo "
  <table class=\"details\">
    <tr>
      <td class=\"detailHead\">
    <form method=\"get\" name=\"form_contr\"
          onsubmit=\"if ($jsfunc) return true; else return false;\"
          action=\"\">
    $title<br />
    $display_cont<br />
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"submit\" value=\"$l_select_contract\" />
    </form>
      </td>
    </tr>
  </table>

  <a href=\"\" onclick='window.close();'>$l_close</a>
";
}
 
</script>




