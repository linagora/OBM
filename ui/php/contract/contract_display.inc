<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contract_display.php                                         //
//     - Desc : Contract Support Display File                                //
// 2001-07-17 : Richard FILIPPI                                              //
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
    <option value=\"$c_all\">$l_all";
  while($obm_q_type->next_record()) {
    $dis_cont_typ .= "\n<option value=".$obm_q_type->f("contracttype_id");
    if ($p_type_id == $obm_q_type->f("contracttype_id")) 
      $dis_cont_typ .= " selected";
    $dis_cont_typ .= ">" . $obm_q_type->f("contracttype_label");
  }
  $dis_cont_typ .= "</select>";

  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }

  $dis_resp_tec = "<select name=\"sel_tech\">
    <option value=\"$c_all\">" . $l_all;
  while($usr_q->next_record()) {
    $dis_resp_tec .= "\n<OPTION value=".$usr_q->f("contact_id");
    if ($usr_q->f("contact_id") == $sel_tech) 
      $dis_resp_tec .= " selected";
    $dis_resp_tec .= ">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")." </option>";
  }
  $dis_resp_tec .="</select>";


echo "<CENTER>" .
     "<FORM METHOD=POST NAME=form_contract " .
     "onSubmit=\"if (check_form(this) == false) return false; " .
     "else return true;\" ACTION=\"".$sess->url("contract_index.php")."\">";

//---------------------------------------------------------------------------
echo "<TABLE BORDER=0>" .
     "<TR>";
//---------------------------------------------------------------------------
//---| Numéro du contract |-------------------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>" .
     $l_label_start . "</FONT><BR>" .
     "<INPUT TYPE=text NAME=tf_num size=20 value=\"".
     $p_num.
     "\"></TD>";
//---------------------------------------------------------------------------
echo "<TD>&nbsp;&nbsp;</TD>";
//---------------------------------------------------------------------------
//---| Nom de SOCIETE |------------------------------------------------------
//---------------------------------------------------------------------------
echo "<td><font size=-2>$l_company</font>
      <br>
      <input name=tf_company_name size=20 maxlength=20 value=\"$comp\"></td>";
//---------------------------------------------------------------------------
echo "<TD>&nbsp;&nbsp;</TD>";
//---------------------------------------------------------------------------
//---| DATE EXPIRATION APRES |-----------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>$l_after</FONT><BR>
    <INPUT NAME=tf_dateapres size=10 value=\"$p_dateafter\"></TD>
  <TD> &nbsp; </TD>";

//---------------------------------------------------------------------------
//---| DATE EXPIRATION AVANT |-----------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>" .
     $l_before . "</FONT><BR>" .
     "<INPUT TYPE=text NAME=tf_dateavant size=10 value=\"" .
     $p_datebefore .
     "\"></TD>";
//---------------------------------------------------------------------------
echo "<TD>&nbsp;&nbsp;</TD>";
echo "</TR>" .
     "</TABLE>";
//---------------------------------------------------------------------------


//---------------------------------------------------------------------------
echo "<TABLE BORDER=0>" .
     "<TR>";
//---------------------------------------------------------------------------
//---| Libellé du contract |-------------------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>" .
     $l_label_libelle . "</FONT><BR>" .
     "<INPUT TYPE=text NAME=tf_label size=20 value=\"".
     $p_label.
     "\"></TD>";
//---------------------------------------------------------------------------
echo "<TD>&nbsp;&nbsp;</TD>";
//---------------------------------------------------------------------------
//---| TYPE de CONTRACT |------------------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>&nbsp;&nbsp;" .
     $l_kind . "</FONT><BR>" .
     $dis_cont_typ ."
     </TD>";


//---------------------------------------------------------------------------
//---| Responsable Technique |-----------------------------------------------
//---------------------------------------------------------------------------
  echo "</td>
    <td><font SIZE=-2>$l_technical_manager</FONT><BR>$dis_resp_tec</td>
    <td>&nbsp;&nbsp;</td>";

//---------------------------------------------------------------------------
//---| Bouton VALIDER |------------------------------------------------------
//---------------------------------------------------------------------------
echo "<TD VALIGN=bottom> 
     <INPUT NAME=\"action\" TYPE=\"hidden\" VALUE=\"search\">";
echo "<INPUT NAME=\"submit\" TYPE=\"submit\" VALUE=\"$l_find\">
     </TD>";
//---------------------------------------------------------------------------
echo "</TR>" .
     "</TABLE>";

//---------------------------------------------------------------------------

echo "</FORM>" .
     "<HR>";
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
  global $set_theme,$ico_mail,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact;
  // -- Labels
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_category,$l_project_manager,$l_origin,$l_marketing_manager;
  global $l_technical_manager,$l_proposition,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update;
  global $l_archive,$popup_height,$popup_width,$l_comment,$l_at,$l_number,$l_clause,$l_dateexp;
  global $l_contract_select_deal,$l_client_manager,$l_deal_label;
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
  
  $dis_sel_t="<SELECT name=\"sel_type\">";
  while($type_q->next_record()) {
    $dis_sel_t.="\n<OPTION value=".$type_q->f("contracttype_id");
    if ($type_id==$type_q->f("contracttype_id"))
       $dis_sel_t.=" selected"; 
    $dis_sel_t.=">".$type_q->f("contracttype_label")."</OPTION>";
  }
  $dis_sel_t.="</SELECT>";

  $dis_sel_respc="<select name=\"sel_market\">";
  while($usr_q->next_record()) {
    $dis_sel_respc.="\n<option value=".$usr_q->f("userobm_id");
    if ($usr_q->f("userobm_id") == $market_i) 
      $dis_sel_respc.=" selected ";
    $dis_sel_respc.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_respc.="</select>";
 
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }

  $dis_sel_respt="<select name=\"sel_tech\">";
  while($usr_q->next_record()) {
   $dis_sel_respt.="\n<option value=".$usr_q->f("userobm_id");
   if ($usr_q->f("userobm_id") == $tech) 
      $dis_sel_respt.=" selected";
   $dis_sel_respt.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_respt.="</select>";

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con1="<SELECT name=\"sel_con1\">";
  while($contact_q->next_record()) {
    $dis_sel_con1.="\n<OPTION value=".$contact_q->f("contact_id");
    if ($contact_q->f("contact_id") == $con1_id) 
      $dis_sel_con1.=" selected";
    $dis_sel_con1.=">". $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") .
    " - " . $contact_q->f("contact_phone")."</OPTION>";
    }
    $dis_sel_con1.="</SELECT>";

  if ($action == "detailupdate") {
    $dis_sel_con1.="<A HREF=\"".
    $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$con1_id)."\">";
    $dis_sel_con1.="<IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A>";    
    }

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con2="<SELECT name=\"sel_con2\">";
  while($contact_q->next_record()) {
    $dis_sel_con2.="\n<OPTION value=".$contact_q->f("contact_id");
    if ($contact_q->f("contact_id") == $con2_id) 
      $dis_sel_con2.=" selected";
    $dis_sel_con2.=">". $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") .
    " - " . $contact_q->f("contact_phone")."</OPTION>";
    }
    $dis_sel_con2.="</SELECT>";

  if ($action == "detailupdate") {
    $dis_sel_con2.="<A HREF=\"".
    $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$con2_id)."\">";
    $dis_sel_con2.="<IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A>";    
  }

  if ($action != "new") {
    $dis_last_update = "<font color=\"#$col_label\">$l_last_update </font>
    $dateu <font color=\"#$col_label\"> $l_at </font> $timeu";
   }

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_sub = "<INPUT TYPE=hidden NAME=hd_soc VALUE=\"$comp_id\">
      <INPUT TYPE=hidden NAME=action VALUE=\"update\">
      <INPUT TYPE=hidden NAME=tf_company_name VALUE=\"$comp_name\">
      <INPUT TYPE=hidden NAME=hd_company_ad1 VALUE=\"$ad1\">
      <INPUT TYPE=hidden NAME=hd_company_zip VALUE=\"$zip\">
      <INPUT TYPE=hidden NAME=hd_company_town VALUE=\"$town\">
      <INPUT TYPE=hidden NAME=param_contract VALUE=\"$id\">
      <INPUT TYPE=submit value=\"$l_update\">
      </FORM>
      &nbsp;  &nbsp;  &nbsp;  &nbsp;
      <FORM METHOD=GET NAME=form_contract_delete onSubmit=\"if (valider_suppression()) return true; else return false;\" ACTION=\"".$sess->url("contract_index.php")."\">
      <INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"delete\">
      <INPUT TYPE=\"hidden\" NAME=\"param_contract\" VALUE=\"$id\">
      <INPUT TYPE=submit value=\"$l_delete\">";
  } else {
    $dis_sub = "<INPUT TYPE=hidden NAME=hd_soc VALUE=\"$comp_id\">
      <INPUT TYPE=hidden NAME=tf_company_name VALUE=\"$comp_name\">
      <INPUT TYPE=hidden NAME=hd_company_ad1 VALUE=\"$ad1\">
      <INPUT TYPE=hidden NAME=hd_company_zip VALUE=\"$zip\">
      <INPUT TYPE=hidden NAME=hd_company_town VALUE=\"$town\">
      <INPUT TYPE=\"hidden\" name=\"action\" value=\"insert\">
      <INPUT TYPE=\"submit\" value=\"$l_insert\">";
  }

  echo "<center>
    <FORM METHOD=POST NAME=form_contract_update onSubmit=\"if (verifier_aff(this)) return true; else return false;\" ACTION=\"".$sess->url("contract_index.php")."\">

  <table border=0>
  <tr>
    <td>
      <table border=0>
      <tr>
        <td><font COLOR=\"#$col_label\">$l_label</font>
          <br>
          <INPUT NAME=tf_label TYPE=text size=40 value=\"$label\">
        </td>
      </tr>
      </table>

      <table>
      <tr>
        <td><font COLOR=\"#$col_label\">$l_number</font>
          <br>
          <INPUT NAME=tf_num TYPE=text size=20 maxlength=40 value=\"$number\">
        </td>
        <td><font COLOR=\"#$col_label\">$l_date_init</font>
          <br>
          <INPUT NAME=tf_datebegin TYPE=text size=10 value=\"$datebegin\">
        </td>
        <td><font COLOR=\"#$col_label\">$l_dateexp</font>
          <br>
          <INPUT NAME=tf_dateexp TYPE=text size=10 value=\"$dateexp\">
        </td>
      </tr>
      </table>

      <table border=0>
      <tr>
        <td>
          <font COLOR=\"#$col_label\">$l_kind</font>
          <br>$dis_sel_t
        </td>
      </tr>
      </table>
    </td>
<!-- Company, Contact -->
    <td valign=top>
      <table bgcolor=\"#$col_label\">
      <tr>
        <td bgcolor=\"#$col_table\">
          <font color=\"#$col_textem\">$comp_name</font>&nbsp;<A HREF=\"".$sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$comp_id"). "\"><IMG ALIGN=MIDDLE SRC=\"/images/$set_theme/$ico_company\"></A>
          <br>$ad1
          <br>$zip $town
        </td>
      </tr><tr>
        <td valign=center>$l_client_manager
          <br>$dis_sel_con1
          <br>$dis_sel_con2
        </td>
      </tr>
      </table>
      <p>
  
      $dis_last_update
    </td>
  </tr>
  <tr>
    <td colspan=\"2\">
     <table border=0>
      <tr>
        <td><font COLOR=\"#$col_label\">$l_marketing_manager</font>
          <br>$dis_sel_respc
        </td>
        <td><font COLOR=\"#$col_label\">$l_technical_manager</font>
          <br>$dis_sel_respt
        </td>
        <td><font COLOR=\"#$col_label\">$l_deal_label</font>
          
         <A HREF=\"".
         $sess->url("../deal/deal_index.php?action=detailconsult&amp;param_deal=$c_id") . "\">$c_name";
         echo "</A><INPUT TYPE=hidden NAME=param_deal value=\"$param_deal\">
         <INPUT TYPE=hidden NAME=deal_new_id value=\"$c_new_id\"><br>
         <a href=\"\" onclick=\"window.open('../deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;title=".urlencode($l_contract_select_deal)."&amp;comp_id=$comp_id','contract','height=$popup_height,width=$popup_width'); return false;\">
         <img src=\"/images/$set_theme/$ico_mail\"></a>
         <INPUT TYPE=text NAME=deal_name value=\"$c_new_name\" size = 40 style=\"border:none\" READONLY onfocus=\"this.blur();\">
        </td>
      </tr>
     </table>
    </td>
   </tr>
  </table>

  <table border=2>
  <tr>
    <td><font COLOR=\"#$col_label\">$l_comment</font>
      <br>
      <textarea NAME=ta_com ROWS=6 COLS=72>$comment</textarea>
    </td>
    <td align=center colspan=2> &nbsp;  &nbsp;  &nbsp; </td>
  </tr>
  <tr>
    <td align=center valign=bottom colspan=3>
    </td>
  </tr>
  <tr>
    <td><font COLOR=\"#$col_label\">$l_clause</font>
      <br>
      <textarea NAME=ta_clause ROWS=6 COLS=72>$clause</textarea>
    </td>
    <td align=center colspan=2> &nbsp;  &nbsp;  &nbsp; $dis_sub
    </td>
  </tr>
  </table>
  </form>
  </center>";
  
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
  // - Labels
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_clause,$l_project_manager,$l_marketing_manager,$l_technical_manager,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update,$l_archive,$l_comment,$l_at,$l_number,$l_visibility,$l_public,$l_private,$l_dateexp,$l_client_manager;
  global $l_incident_list,$l_incident_new; 
  global $perms_user,$l_perms_user;
  global $auth, $sess;

  $market = $contract_q->f("lname1") . " " . $contract_q->f("fname1");
  $tech = $contract_q->f("lname2") . " " . $contract_q->f("fname2");

  while($type_q->next_record()) {
    if ($type_q->f("contracttype_id") == $contract_q->f("contract_type_id"))
      $dis_cont_type = $type_q->f("contracttype_label");
  }

  while($contact_q->next_record()) {
    if ($contact_q->f("contact_id") == $contract_q->f("contract_contact1_id")) 
      $dis_cont_cli1 = "<TD>".$contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone")."</TD>";
  }

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
  // 2 ième contact client
  while($contact_q->next_record()) {
   if ($contact_q->f("contact_id") == $contract_q->f("contract_contact2_id")) 
       $dis_cont_cli2 = "<TD>".$contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone")."</TD>";
    }

  $dis_date_date = timestamp_to_date($contract_q->f("timeupdate"));
  $dis_date_time = timestamp_to_time($contract_q->f("timeupdate"));

  if ($auth->auth["perm"] != $perms_user) {
    $dis_update = "<FORM METHOD=GET NAME=form_contract_update ACTION=\"".$sess->url("contract_index.php")."\">
      <INPUT TYPE=hidden NAME=action VALUE=\"detailupdate\">
      <INPUT TYPE=hidden NAME=param_contract VALUE=\"".$contract_q->f("contract_id")."\">
      <INPUT TYPE=submit value=\"$l_update\">
      </FORM>";

}

  echo "<center>
  <table border=0>
  <tr>";
//////////////////
//Liens Externes//
//////////////////
echo "
    <td valign=top> 
      <A HREF=\"".$sess->url("incident_index.php?action=search&amp;param_contract=".$contract_q->f("contract_id")."")."\"><img BORDER=0  SRC=\"/images/$set_theme/$ico_contact_list\"></A>
      <font SIZE=-1><A HREF=\"" . $sess->url("incident_index.php?action=search&amp;param_contract=".$contract_q->f("contract_id")."") . "\">$l_incident_list</A></font>";

  if ($auth->auth["perm"] != $perms_user) {
    echo "<BR>
      <A HREF=\"" . $sess->url("incident_index.php?action=new&amp;param_contract=".$contract_q->f("contract_id")."") . "\"><IMG BORDER=0
        SRC=\"/images/$set_theme/$ico_contact_new\"></A>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("incident_index.php?action=new&amp;param_contract=".$contract_q->f("contract_id")."") . "\">$l_incident_new</A></FONT>";
  }
  echo "<BR>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("../deal/deal_index.php?action=detailconsult&param_deal=".$deal_q->f("deal_id")."") . "\">
      <img BORDER=0  SRC=\"/images/$set_theme/$ico_contact_list\"></A></FONT>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("../deal/deal_index.php?action=detailconsult&param_deal=".$deal_q->f("deal_id")."") . "\">
      ".$deal_q->f("deal_label")."</A></FONT>
      </TD>";
//////////////////////
//Fin Liens Externes//
//////////////////////
echo "
<TD>


<TABLE border=0><TR><TD>";

echo "
<TABLE border=0><TR><TD valign=top align=left>";
echo "<TABLE>
<TR>
   <TD><FONT COLOR=\"#$col_label\">$l_label</FONT></TD><TD>";
echo $contract_q->f("contract_label");
echo "</TD>
      </TR><TR>
  <TD><FONT COLOR=\"#$col_label\">$l_number</FONT></TD><TD>";
echo $contract_q->f("contract_number");
echo "</TD>
</TR>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_date_init</FONT></TD><TD>";
echo $contract_q->f("datebegin");
echo "</TD>
</TR>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_dateexp</FONT></TD><TD>";
echo $contract_q->f("dateexp");
echo "</TD>
</TR>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_kind</FONT></TD><TD>
	".$dis_cont_type."
  </TD>
</TR>
</TABLE>

<table border=0>
<tr>
  <td>
    <font color=\"#$col_label\">$l_marketing_manager</font></td>
  <td>$market</td>
</tr>
<tr>
  <td><font color=\"#$col_label\">$l_technical_manager</font></td>
  <td>$tech</td>
</tr>
</table>";

  // Pave Societe, contact
  echo " </TD><TD valign=top>
  <TABLE bgcolor=\"#$col_label\">
    <TR>
    <TD bgcolor=\"#$col_table\" colspan=2>
    <FONT color=\"#$col_textem\">".$comp_q->f("company_name")."</FONT>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    echo "<A HREF=\"".
      $sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$p_company_id")."\">";
    echo "<IMG ALIGN=MIDDLE SRC=\"/images/$set_theme/$ico_company\"></A>
    <BR>".$comp_q->f("company_address1") ."
    <BR>".$comp_q->f("company_zipcode") . " " . $comp_q->f("company_town") ."
    </TD>
    </TR><TR>
    <TD valign=center colspan=2>$l_client_manager</TD>
    </TR><TR>
    ".$dis_cont_cli1."
    <TD><A HREF=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$contract_q->f("contract_contact1_id"))."\">";
    echo "<IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A></TD>";
    echo "</TR><TR>
    ".$dis_cont_cli2."
    <TD><A HREF=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$contract_q->f("contract_contact2_id"))."\">";
    echo "<IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A></TD>";
    echo "</TR>

  </TABLE>
  <TABLE width=100%>
  <TR><TD colspan=2>
  <FONT color=\"#$col_label\">$l_last_update </FONT>";
    echo $dis_date_date;
    echo "<FONT color=\"#$col_label\"> $l_at </FONT>";
    echo $dis_date_time;
    echo "</TD></TR>
  </TABLE>";

echo "
</TD>
</TR>
</TABLE>

</TD></TR>
<TR><TD>

<TABLE width=100% border=0>
<TR>";
echo "<TD valign=top><FONT COLOR=\"#$col_label\">".$l_comment."</FONT><BR>";
echo nl2br($contract_q->f("contract_comment")); 
echo "<BR><BR><TD valign=top><FONT COLOR=\"#$col_label\">".$l_clause."</FONT><BR>";
echo nl2br($contract_q->f("contract_clause")); 
echo "</TD></TR><TR>
<TD align=center><BR>
".$dis_update." 
&nbsp;</TD></TR></TABLE>
</TD></TR></TABLE>
</TD></TR></TABLE>";
echo "</CENTER>";

}




///////////////////////////////////////////////////////////////////////////////
// Display the Contact search result                                         //
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : state, name, kind, zip
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
// Display Search result (if it has to be done                               //
// if ($set_display != "no") or if it comes from the submit bouton           //
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//     $p_label, 
//     $p_company,
//     $p_type_id,
//     $p_cat_id,
//     $p_status_id,
//     $p_dateafter,
//     $p_datebefore,
//     $p_manager_id, 
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

  echo "<font color=\"#$col_textem\">$nb_contract $l_found</font>";

  $url = $sess->url("contract_index.php?action=search&amp;tf_label=$p_label&amp;tf_soc=$p_company&amp;sel_market=$sel_market&amp;sel_type=$p_type_id&amp;tf_num=$p_num&amp;sel_tech=$sel_tech&amp;tf_dateapres=$p_dateafter&amp;tf_dateavant=$p_datebefore");
  echo "<br>";

   $contract_q->seek($first_row);

   $dis_contracts_list=new OBM_DISPLAY("DATA",$obm_q_display_options);
   $dis_contracts_list->data_set = $contract_q;
   $dis_contracts_list->data_url = $url;
   $dis_contracts_list->data_header = "both";
   $dis_contracts_list->display() ;

}


///////////////////////////////////////////////////////////////////////////
//  Contracts Administration Forms :                                           
 ///////////////////////////////////////////////////////////////////////////
function html_contract_admin_form($type_q) {
  // Theme : 
  global $col_a_table, $col_a_tableh, $col_textem;
  // Labels : 
  global $l_type_manage,$l_type_new,$l_type_exist, $l_status_manage;
  global $l_status_exist,$l_status_new, $l_category_manage,$l_category_exist;
  global $l_category_new, $l_category_label,$l_category_minilabel,$l_label;
  global $l_minus,$l_plus,$l_order;
  // Actions : 
  global $l_type_delete,$l_type_update,$l_type_insert;
  // Messages : 
  global $l_type_insert_ok, $l_type_insert_error, $l_type_update_ok;
  global $l_type_update_error, $l_type_delete_error, $l_type_del_verif_error;
  global $l_type_delete_ok;
  global $sess;

  $dis_sel_type = "<SELECT NAME=\"sel_type\" SIZE=4 >";
  while ($type_q->next_record()) {
    $dis_sel_type .= "\n<OPTION value=\"" . $type_q->f("contracttype_id") . "\">".$type_q->f("contracttype_label") .
           "</OPTION>";
  }
  $dis_sel_type .= "</SELECT>";

  echo "<center>
    <FORM name=\"form_admin_type_delete\" METHOD=post action=\"".$sess->url("contract_index.php")."\">
    <table WIDTH=90% border=1 bgcolor=\"#$col_a_table\">
    <tr>
      <td colspan=5 BGCOLOR=\"#$col_a_tableh\">
        <i><font COLOR=\"#$col_textem\">$l_type_manage</font></i>
      </td>
    </tr>
    <tr>
      <td>

<!-- Type Delete Section -->

        <table border=0>
        <tr>
          <td>$l_type_exist</td>
          <td>$dis_sel_type</td>
          <td>
            <input TYPE=hidden name=action value=\"admintypedelete\">
            <input TYPE=\"submit\" name=\"sub_type\" value=\"$l_type_delete\"
              onClick=\"if (check_type_del(this.form)) return true;
                        else return false;\">
          </td>
        </tr>
        </form>

<!--  Type Update Section -->

        <form name=\"form_admin_type_update\" METHOD=post action=\"".$sess->url("contract_index.php")."\">
	<tr>
	  <td colspan=2 align=center>
	    <INPUT TYPE=hidden name=sel_type value=\"\">
	    <INPUT TYPE=hidden name=\"action\" value=\"admintypeupdate\">
	    <table border=0>
	    <tr>
	      <td><INPUT TYPE=text name=\"tf_type_upd\"></td>
	    </tr>
	    </table>
	  </td>
	  <td><INPUT TYPE=submit name=\"sub_type\" value=\"$l_type_update\"
	    onClick=\"if (check_type_upd(this.form,document.form_admin_type_delete)) return true; else return false;\">
	  </td>
	  <td valign=center>
          </td>
        </tr>
        </table>
      </td>
      <td>
      </form>

<!-- New Type Section -->

        <form name=\"form_admin_type_new\" METHOD=post action=\"".$sess->url("contract_index.php")."\">
        <table border=0>
        <tr>
          <td align=right>$l_type_new : <INPUT type=text name=\"tf_type_new\">
          </td>
        </tr><tr>
          <td align=center>
            <input TYPE=hidden name=\"action\" value=\"admintypeinsert\">
            <input type=submit name=\"sub_type\" value=\"$l_type_insert\"
              onClick=\"if (check_type_new(this.form)) return true;
                       else return false;\">
          </td>
        </tr>
        </table>
      </td>
    </tr>
    </table>
    </form>
  </center>";

}


//////////////////////////////////////////////////////////////////////////
// Display the screen used to change the display parameters of deals list
//////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//    $obm_q_options_display : list of fields to display or not
///////////////////////////////////////////////////////////////////////////
function dis_contract_display_pref($pref_q) {
 
  // Label
  global $l_contract_display;
  
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_q);
  $dis_pref->display_entity = "contract"; 
  $dis_pref->pref_title = $l_contract_display;
  $dis_pref->pref_dis_help = 1;

  echo "<center>";
  $dis_pref->display();
  echo "<br></center>";
  
}

///////////////////////////////////////////////////////////////////////////////
// Display Contract Select Form                                               //
// Parameters:
//   - $comp_q : company database result
//   - $title  : Title to be displayed
//   - $url    : URL to call with parameter company
///////////////////////////////////////////////////////////////////////////////
function html_select_contract($contr_q, $title, $url="") {
  global $l_select_contract;
  global $sess;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }

  echo "<CENTER>
    <FORM METHOD=GET NAME=form_contr
          onSubmit=\"if ($jsfunc) return true; else return false;\"
          ACTION=\"\">";

  echo "$title
    <P><font size=2 face=Courier>
    <SELECT name=\"param_contract\" size=10 style=\"font-family:fixed,courier\">";

  while($contr_q->next_record()) {
    $id = $contr_q->f("contract_id");
    $name = $contr_q->f("contract_label");
    echo "\n<OPTION value=\"$id\">$name ";
  }

  echo "</SELECT></font>
    <P>
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"new\">
    <INPUT TYPE=submit value=\"$l_select_contract\">
    </FORM>
    </CENTER>";
}
 
</SCRIPT>




