<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contract_display.php                                         //
//     - Desc : Contract Support Display File                                //
// 2002-01-08 : Mehdi RANDE                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Contract search form                                              //
// Parameters : 
//   - $contract[] : default form values
//     keys used      : 
//   - $usr_q      : DB result object (userobm list)
//   - $obm_q_type : DB result object (contract kind list)
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_form($contract, $usr_q, $obm_q_type) {
  global $l_label_start,$l_label_libelle,$l_company,$l_kind,$l_after,$l_before,$l_manager;
  global $l_find,$l_marketing_manager,$l_technical_manager; 
  global $sess, $c_all, $l_all;

  $p_label = $contract["label"];
  $comp = $contract["company_name"];
  $p_type_id = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $sel_market = $contract["market"];
  $sel_tech = $contract["tech"];
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

  $dis_resp_tec = "<select name=\"sel_tech\">
    <option value=\"$c_all\">$l_all</option>";
  while($usr_q->next_record()) {
    $dis_resp_tec .= "\n<option value=\"".$usr_q->f("contact_id")."\"";
    if ($usr_q->f("contact_id") == $sel_tech) 
      $dis_resp_tec .= " selected=\"selected\"";
    $dis_resp_tec .= ">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")." </option>";
  }
  $dis_resp_tec .="</select>";


  // --- HTML Template --------------------------------------------------------


echo "
    <form method=\"get\" name=\"form_contract\" 
    onsubmit=\"if (check_form(this) == false) return false;
    else return true;\" action=\"".$sess->url("contract_index.php")."\">
     <table class=\"details\">   
      <tr>
       <td class=\"textLabelForm\">$l_label_start</td>
       <td>&nbsp;&nbsp;</td>
       <td class=\"textLabelForm\">$l_company</td>
       <td>&nbsp;&nbsp;</td>
       <td class=\"textLabelForm\">$l_after</td>
       <td>&nbsp;&nbsp;</td>
       <td class=\"textLabelForm\">$l_before</td>
      </tr><tr>
       <td><input type=\"text\" name=\"tf_num\" size=\"20\" value=\"$p_num\" /></td>
       <td>&nbsp;&nbsp;</td>
       <td><input name=\"tf_company_name\" size=\"20\" maxlength=\"20\" value=\"$comp\" /></td>
       <td>&nbsp;&nbsp;</td>
       <td><input name=\"tf_dateapres\" size=\"10\" value=\"$p_dateafter\" /></td>
       <td>&nbsp;&nbsp;</td>
       <td><input type=\"text\" name=\"tf_dateavant\" size=\"10\" value=\"$p_datebefore\" /></td>
       </tr>
     </table>
     <table class=\"details\">
      <tr>
       <td class=\"textLabelForm\">$l_label_libelle</td>
       <td>&nbsp;&nbsp;</td>
       <td class=\"textLabelForm\">&nbsp;&nbsp;$l_kind</td>
       <td>&nbsp;&nbsp;</td>
       <td class=\"textLabelForm\">$l_technical_manager</td>
      </tr><tr>
       <td><input type=\"text\" name=\"tf_label\" size=\"20\" value=\"$p_label\" /></td>
       <td>&nbsp;&nbsp;</td>
       <td>$dis_cont_typ</td>
       <td>&nbsp;&nbsp;</td>
       <td>$dis_resp_tec</td>
       <td>&nbsp;&nbsp;</td>
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
function html_contract_form($action,$contract_q,$deal_q,$type_q,$usr_q,$comp_q,$contact_q, $contract){
  // - Themes
  global $set_theme,$ico_mail,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_deal,$ico_contact;
  // -- Labels
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_category,$l_project_manager,$l_origin,$l_marketing_manager;
  global $l_technical_manager,$l_proposition,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update;
  global $l_archive,$popup_height,$popup_width,$l_comment,$l_at,$l_number,$l_clause,$l_dateexp;
  global $l_contract_select_deal,$l_client_manager,$l_deal_label,$l_contract,$l_contract_company;
  global $sess,$auth;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {

    $id = $contract_q->f("contract_id");
    $label = $contract_q->f("contract_label");
    $comp_id = $contract_q->f("contract_company_id");
    $number = $contract_q->f("contract_number");
    $datebegin = $contract_q->f("datebegin");
    $dateexp = $contract_q->f("dateexp");
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

    $timeupdate = $contract_q->f("timeupdate");
    $dateu = timestamp_to_date($timeupdate);
    $timeu = timestamp_to_time($timeupdate);
    if(is_object($deal_q)) {
      $param_deal = $deal_q->f("deal_id");
      $c_name = $deal_q->f("deal_label");
      $c_id = $param_deal;
    }
  } else {
    // Values are taken from parameters
    $param_deal = $contract["deal_id"];
    $c_name = $contract["label"];
    $c_id = $contract["deal_id"];
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

      $timeupdate = $contract["timeupdate"];
      $dateu = timestamp_to_date($timeupdate);
      $timeu = timestamp_to_time($timeupdate);
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
    $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$con1_id)."\">";
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
    $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$con2_id)."\">";
    $dis_sel_con2.="<img src=\"/images/$set_theme/$ico_contact\" alt=\"[contact\" /></a>";    
  }

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_sub = "<input type=\"hidden\" name=\"hd_soc\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp_name\" />
      <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
      <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
      <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
      <input type=\"hidden\" name=\"param_contract\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />
     ";
  $dis_delete="<form method=\"get\" name=\"form_contract_delete\" onsubmit=\"if (valider_suppression()) return true; else return false;\" action=\"".$sess->url("contract_index.php")."\">
      <table class=\"details\">
        <tr>
          <td>
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input type=\"hidden\" name=\"param_contract\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_delete\" />
          </td>
        </tr>
      </table>
      </form>
";
  } else {
    $dis_sub = "<input type=\"hidden\" name=\"hd_soc\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp_name\" />
      <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
      <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
      <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  // --- html template --------------------------------------------------------

  echo "  
  <form method=\"post\" name=\"form_contract_update\" onsubmit=\"if (verifier_aff(this)) return true; else return false;\" 
        action=\"".$sess->url("contract_index.php")."\">
  <table class=\"details\">
   <tr>
    <td colspan=\"2\" class=\"detailHead\">$l_contract_company</td>
   </tr><tr>
    <td class=\"detailLabel\">
     $comp_name <a href=\"".$sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$comp_id"). "\">
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
      <input type=\"hidden\" name=\"param_deal\" value=\"$param_deal\" />
      <input type=\"hidden\" name=\"deal_new_id\" value=\"$c_new_id\" />
    </td>
    <td>
      <a href=\"".$sess->url("../deal/deal_index.php?action=detailconsult&amp;param_deal=$c_id")."\">
      $c_name</a>
      <a href=\"\" onclick=\"window.open('../deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;title=".urlencode($l_contract_select_deal)."&amp;comp_id=$comp_id','contract','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"/images/$set_theme/$ico_deal\" alt=\"[Choose Deal]\" /></a>
      <input type=\"text\" name=\"deal_name\" value=\"$c_new_name\" size =\"40\" readonly=\"readonly\" onfocus=\"this.blur();\" />
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

// Can be improve (better query, fewer parameters for consult !)
///////////////////////////////////////////////////////////////////////////////
// Display Contract Consult                                                  //
// Parameters:
//   - $contract_q : information about the contract
//   - $type_q     : contract type list
//   - $comp_q     : list of companies
//   - $contact_q  : list of contact from the company 
//      $p_company_id  : id of the company concerned by the contract
///////////////////////////////////////////////////////////////////////////////
function html_contract_consult($contract_q, $type_q, $comp_q,$contact_q,$p_company_id,$deal_q) {
  // -- Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact,$ico_contact_list,$ico_contact_new;
  // - labels
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_clause,$l_project_manager,$l_marketing_manager,$l_technical_manager,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update,$l_archive,$l_comment,$l_at,$l_number,$l_visibility,$l_public,$l_private,$l_dateexp,$l_client_manager;
  global $l_incident_list,$l_incident_new, $l_contract,$l_contract_company; 
  global $perms_user,$l_perms_user;
  global $auth, $sess;

  $market = $contract_q->f("lname1") . " " . $contract_q->f("fname1");
  $tech = $contract_q->f("lname2") . " " . $contract_q->f("fname2");
  $com = nl2br($contract_q->f("contract_comment"));
  $clause = nl2br($contract_q->f("contract_clause"));
  while($type_q->next_record()) {
    if ($type_q->f("contracttype_id") == $contract_q->f("contract_type_id"))
      $dis_cont_type = $type_q->f("contracttype_label");
  }

  while($contact_q->next_record()) {
    if ($contact_q->f("contact_id") == $contract_q->f("contract_contact1_id")) 
      $dis_cont_cli1 = $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone");
  }

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
  // 2 ième contact client
  while($contact_q->next_record()) {
   if ($contact_q->f("contact_id") == $contract_q->f("contract_contact2_id")) 
       $dis_cont_cli2 = $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone");
    }

  $dis_date_date = timestamp_to_date($contract_q->f("timeupdate"));
  $dis_date_time = timestamp_to_time($contract_q->f("timeupdate"));

    $dis_update = "<form method=\"get\" name=\"form_contract_update\" action=\"".$sess->url("contract_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"detailupdate\">
      <input type=\"hidden\" name=\"param_contract\" value=\"".$contract_q->f("contract_id")."\">
      <input type=\"submit\" value=\"$l_update\">
      </form>";


  // --- HTML Template --------------------------------------------------------
/*
//////////////////
//Liens Externes//
//////////////////
echo "
    <td valign=top> 
      <A HREF=\"".$sess->url("incident_index.php?action=search&amp;param_contract=".$contract_q->f("contract_id")."")."\">
      <img BORDER=0  SRC=\"/images/$set_theme/$ico_contact_list\"></A>
      <font SIZE=-1>
      <A HREF=\"" . $sess->url("incident_index.php?action=search&amp;param_contract=".$contract_q->f("contract_id")."") . "\">
      $l_incident_list</A></font>
      <BR>
      <A HREF=\"" . $sess->url("incident_index.php?action=new&amp;param_contract=".$contract_q->f("contract_id")."") . "\">
      <IMG BORDER=0 SRC=\"/images/$set_theme/$ico_contact_new\"></A>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("incident_index.php?action=new&amp;param_contract=".$contract_q->f("contract_id")."") . "\">
      $l_incident_new</A></FONT>
      <BR>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("../deal/deal_index.php?action=detailconsult&param_deal=".$deal_q->f("deal_id")."") . "\">
      <img BORDER=0  SRC=\"/images/$set_theme/$ico_contact_list\"></A></FONT>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("../deal/deal_index.php?action=detailconsult&param_deal=".$deal_q->f("deal_id")."") . "\">
      ".$deal_q->f("deal_label")."</A></FONT>
      </TD>";
//////////////////////
//Fin Liens Externes//
//////////////////////
*/
echo "
<table class=\"details\">
 <tr>
   <td class=\"detailHead\" colspan=\"2\">$l_contract_company</td>
 </tr><tr>
  <td class=\"detailLabel\">
   ".$comp_q->f("company_name")."
   <a href=\"".$sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$p_company_id")."\">
   <img src=\"/images/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
  </td>
  <td class=\"detailText\">
    ".$comp_q->f("company_address1") ."<br />
    ".$comp_q->f("company_zipcode") . " " . $comp_q->f("company_town") ."
  </td>
 </tr><tr>
  <td class=\"detailLabel\">$l_client_manager 1</td>
  <td class=\"detailText\">
     $dis_cont_cli1
     <a href=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$contract_q->f("contract_contact1_id"))."\">
      <img src=\"/images/$set_theme/$ico_contact\" alt=\"[Contact]\" /></a>
   </td> 
 </tr><tr>
  <td class=\"detailLabel\">$l_client_manager 1</td>
  <td class=\"detailText\">
    $dis_cont_cli2
    <a href=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$contract_q->f("contract_contact2_id"))."\">
    <img src=\"/images/$set_theme/$ico_contact\" alt=\"[Contact]\" /></a></td>
 </tr><tr>
   <td class=\"detailHead\" colspan=\"2\">$l_contract</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_label</td>
  <td class=\"detailText\">".$contract_q->f("contract_label")."</td>
 </tr><tr>
  <td class=\"detailLabel\"><font color=\"#$col_label\">$l_number</font></td>
  <td class=\"detailText\">".$contract_q->f("contract_number")."</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_date_init</td>
  <td class=\"detailText\">".$contract_q->f("datebegin")."</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_dateexp</td>
  <td class=\"detailText\">".$contract_q->f("dateexp")."</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_kind</td>
  <td class=\"detailText\">$dis_cont_type</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_marketing_manager</td>
  <td class=\"detailText\">$market</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_technical_manager</td>
  <td class=\"detailText\">$tech</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_last_update</td>
  <td class=\"detailText\">$l_last_update $dis_date_date $l_at $dis_date_time</td>
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
// DISPLAY THE CONTACT SEARCH RESULT                                         //
// PARAMETERS:
//   - $CONTACT[] : CONTACT SEARCH CRITERIA
//     KEYS USED  : STATE, NAME, KIND, ZIP
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
// DISPLAY SEARCH RESULT (IF IT HAS TO BE DONE                               //
// IF ($SET_DISPLAY != "NO") OR IF IT COMES FROM THE SUBMIT BOUTON           //
///////////////////////////////////////////////////////////////////////////////
// PARAMETERS :
// ------------
//     $P_LABEL, 
//     $P_COMPANY,
//     $P_TYPE_ID,
//     $P_CAT_ID,
//     $P_STATUS_ID,
//     $P_DATEAFTER,
//     $P_DATEBEFORE,
//     $P_MANAGER_ID, 
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_list($contract_q,$obm_q_display_options,$nb_contract,$contract) {
  global $col_textem, $l_found, $sess;

  $p_label = $contract["label"];
  $p_type_id = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $p_company = $contract["companyname"];
  $sel_market = $contract["market"];
  $sel_tech = $contract["tech"];
  $p_num = $contract["number"]; 

  $url = $sess->url("contract_index.php?action=search&amp;tf_label=$p_label&amp;tf_soc=$p_company&amp;sel_market=$sel_market&amp;sel_type=$p_type_id&amp;tf_num=$p_num&amp;sel_tech=$sel_tech&amp;tf_dateapres=$p_dateafter&amp;tf_dateavant=$p_datebefore");

   $contract_q->seek($first_row);

   $dis_contracts_list=new obm_display("DATA",$obm_q_display_options);
   $dis_contracts_list->data_set = $contract_q;
   $dis_contracts_list->data_url = $url;
   $dis_contracts_list->data_header = "both";

  // --- html template --------------------------------------------------------

   display_info_msg($nb_contract." ".$l_found);
   $dis_contracts_list->display() ;

}


///////////////////////////////////////////////////////////////////////////
//  CONTRACTS ADMINISTRATION FORMS :                                           
 ///////////////////////////////////////////////////////////////////////////
function html_contract_admin_form($type_q) {
  // theme : 
  global $col_a_table, $col_a_tableh, $col_textem;
  // labels : 
  global $l_type_manage,$l_type_new,$l_type_exist, $l_status_manage;
  global $l_status_exist,$l_status_new, $l_category_manage,$l_category_exist;
  global $l_category_new, $l_category_label,$l_category_minilabel,$l_label;
  global $l_minus,$l_plus,$l_order;
  // actions : 
  global $l_type_delete,$l_type_update,$l_type_insert;
  // messages : 
  global $l_type_insert_ok, $l_type_insert_error, $l_type_update_ok;
  global $l_type_update_error, $l_type_delete_error, $l_type_del_verif_error;
  global $l_type_delete_ok;
  global $sess;

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
     <form name=\"form_admin_type_delete\" method=\"post\" action=\"".$sess->url("contract_index.php")."\">
       <table>
        <tr>
          <td class=\"adminLabel\">$l_type_exist</td>
          <td class=\"adminLabel\">$dis_sel_type</td>
          <td>
            <input type=\"hidden\" name=\"action\" value=\"admintypedelete\" />
            <input type=\"submit\" name=\"sub_type\" value=\"$l_type_delete\"
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
       <form name=\"form_admin_type_update\" method=\"post\" action=\"".$sess->url("contract_index.php")."\">
       <table>
         <tr>
          <td>
	    <input type=\"hidden\" name=\"sel_type\" value=\"\" />
	    <input type=\"hidden\" name=\"action\" value=\"admintypeupdate\" />
            <input type=\"text\" name=\"tf_type_upd\" />
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
    <form name=\"form_admin_type_new\" method=\"post\" action=\"".$sess->url("contract_index.php")."\">
     <table>
      <tr>
       <td class=\"adminLabel\">$l_type_new : <input type=\"text\" name=\"tf_type_new\" />
       </td>
      </tr><tr>
       <td class=\"adminLabel\">
         <input type=\"hidden\" name=\"action\" value=\"admintypeinsert\" />
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

//////////////////////////////////////////////////////////////////////////
// display the screen used to change the display parameters of deals list
//////////////////////////////////////////////////////////////////////////
// parameters :
// ------------
//    $obm_q_options_display : list of fields to display or not
///////////////////////////////////////////////////////////////////////////

function dis_type_links($obm_q){
 global $l_deal, $l_type_del_verif_error;
    $dis_link = "<table class=\"details\"><tr><td class=\"detailHead\">".
                 $obm_q->num_rows()."$l_deal $l_type_del_verif_error</td></tr>";   
    while ($obm_q->next_record()) {
      $dis_link .= "
                <tr><td class=\"detailLabel\">
                <a href=\"contract_index.php?action=detailconsult&amp;param_contract=".$obm_q->f("contract_id")."\">
                ".$obm_q->f("contract_label")."</a>
		</td></tr></table>";
    }
echo $dis_link;
}

//////////////////////////////////////////////////////////////////////////
// display the screen used to change the display parameters of deals list
//////////////////////////////////////////////////////////////////////////
// parameters :
// ------------
//    $obm_q_options_display : list of fields to display or not
///////////////////////////////////////////////////////////////////////////

function dis_contract_display_pref($pref_q) {
 
  // label
  global $l_contract_display;
  
  $dis_pref = new obm_display("PREFERENCES", $pref_q);
  $dis_pref->display_entity = "contract"; 
  $dis_pref->pref_title = $l_contract_display;
  $dis_pref->pref_dis_help = 1;

  // --- html template --------------------------------------------------------

  $dis_pref->display();
}

///////////////////////////////////////////////////////////////////////////////
// display contract select form                                               //
// parameters:
//   - $comp_q : company database result
//   - $title  : title to be displayed
//   - $url    : url to call with parameter company
///////////////////////////////////////////////////////////////////////////////
function html_select_contract($contr_q, $title, $url="") {
  global $l_select_contract;
  global $sess;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }


  $display_cont = "
    <select name=\"param_contract\" size=10 style=\"font-family:fixed,courier\">";

  while($contr_q->next_record()) {
    $id = $contr_q->f("contract_id");
    $name = $contr_q->f("contract_label");
    $display_cont .= "\n<option value=\"$id\">$name</option> ";
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
  </table>";
}
 
</script>




