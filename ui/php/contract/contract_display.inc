<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contract_display.php                                        //
//     - Desc  : Contract Support Display File                                //   
// 2001-07-17  : Richard FILIPPI                                             //
///////////////////////////////////////////////////////////////////////////////
//      $Id$
///////////////////////////////////////////////////////////////////////////////
function html_contract_form($obm_q_contract,$action,$obm_q_type,$obm_q_typedeal,$obm_q_contacts_obm,$obm_q_company,$obm_q_contacts,$p_company_id){

  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact;
  // -- Labels
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_category,$l_project_manager,$l_origin,$l_marketing_manager,$l_technical_manager,$l_proposition,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update,$l_archive,$l_comment,$l_at,$l_number_deal,$l_kind_deal,$l_clause,$l_date_expir,$l_client_manager;
 
  global $sess,$auth;

  $number=($p_number ? $p_number : $obm_q_contract->f("contract_numero"));
  $label=($p_label ? $p_label : $obm_q_contract->f("contract_label"));
  $datebegin=($p_datebegin ? $p_datebegin : $obm_q_contract->f("contract_debut"));
  $datefin=($p_datefin ? $p_datefin : $obm_q_contract->f("contract_expiration"));
  $type_id=($p_type_id ? $p_type_id : $obm_q_contract->f("contract_type_id"));
  $marketing_id=($p_marketing_id ? $p_marketing_id : $obm_q_contract->f("contract_responsable_com_id"));
  $technical_id=($p_technical_id ? $p_technical_id : $obm_q_contract->f("contract_responsable_tech_id"));
  $client_id=($p_client_id ? $p_client_id : $obm_q_contract->f("contract_responsable_client_id"));
  $type_deal_id=($p_type_deal_id ? $p_type_deal_id : $obm_q_contract->f("contract_typedeal"));
  $con1_id=($p_con1_id ? $p_con1_id : $obm_q_contract->f("contract_responsable_client_id"));
  $con2_id=($p_con2_id ? $p_con2_id : $obm_q_contract->f("contract_responsable_client2_id"));
  $datemodif=($p_datemodif ? $p_datemodif : $obm_q_contract->f("contract_timeupdate"));
  $comment=($p_comment ? $p_comment : $obm_q_contract->f("contract_comment"));
  $clause=($p_comment ? $p_clause : $obm_q_contract->f("contract_clause"));
  
  $obm_q_company->next_record();
  
  $dis_sel_taff="<SELECT name=\"sel_type_aff\">";
  while($obm_q_type->next_record()) {
    $dis_sel_taff.="\n<OPTION value=".$obm_q_type->f("contracttype_id");
    if ($type_id==$obm_q_type->f("contracttype_id"))
       $dis_sel_taff.=" selected"; 
    $dis_sel_taff.=">".$obm_q_type->f("contracttype_label")."</OPTION>";
  }
  $dis_sel_taff.="</SELECT>";

  $dis_sel_tdeal="<SELECT name=\"sel_typedeal_aff\">";
  while($obm_q_typedeal->next_record()) {
    $dis_sel_tdeal.="\n<OPTION value=".$obm_q_typedeal->f("dealtype_id");
    if ($type_deal_id==$obm_q_typedeal->f("dealtype_id")) 
        $dis_sel_tdeal.=" selected"; 
      $dis_sel_tdeal.=">".$obm_q_typedeal->f("dealtype_label")."</OPTION>";
    }
    $dis_sel_tdeal.="</SELECT>";

  $dis_sel_respc="<SELECT name=\"sel_respcomm_aff\">";
  while($obm_q_contacts_obm->next_record()) {
    $dis_sel_respc.="\n<OPTION value=".$obm_q_contacts_obm->f("contact_id");
    if ($obm_q_contacts_obm->f("contact_id") == $marketing_id) 
      $dis_sel_respc.=" selected ";
    $dis_sel_respc.=">". $obm_q_contacts_obm->f("contact_lastname") . " " . $obm_q_contacts_obm->f("contact_firstname")."</OPTION>";
  }
  $dis_sel_respc.="</SELECT>";
 
  if ($obm_q_contacts_obm->nf()>0) {
    $obm_q_contacts_obm->seek(0);
  }

  $dis_sel_respt="<SELECT name=\"sel_resptech_aff\">";
  while($obm_q_contacts_obm->next_record()) {
   $dis_sel_respt.="\n<OPTION value=".$obm_q_contacts_obm->f("contact_id");
   if ($obm_q_contacts_obm->f("contact_id") == $technical_id) 
      $dis_sel_respt.=" selected";
   $dis_sel_respt.=">". $obm_q_contacts_obm->f("contact_lastname") . " " . $obm_q_contacts_obm->f("contact_firstname")."</OPTION>";
  };
  $dis_sel_respt.="</SELECT>";

  if ($obm_q_contacts->nf()>0) {
    $obm_q_contacts->seek(0);
  }
   
  $dis_sel_con1="<SELECT name=\"sel_con1_aff\">";
  while($obm_q_contacts->next_record()) {
    $dis_sel_con1.="\n<OPTION value=".$obm_q_contacts->f("contact_id");
    if ($obm_q_contacts->f("contact_id") == $con1_id) 
      $dis_sel_con1.=" selected";
    $dis_sel_con1.=">". $obm_q_contacts->f("contact_lastname")." ". $obm_q_contacts->f("contact_firstname") .
    " - " . $obm_q_contacts->f("contact_phone")."</OPTION>";
    }
    $dis_sel_con1.="</SELECT>";

  if ($action == "detailupdate") {
    $dis_sel_con1.="<A HREF=\"".
    $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$con1_id)."\">";
    $dis_sel_con1.="<IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A>";    
    };

  if ($obm_q_contacts->nf()>0) {
    $obm_q_contacts->seek(0);
  }
   
  $dis_sel_con2="<SELECT name=\"sel_con2_aff\">";
  while($obm_q_contacts->next_record()) {
    $dis_sel_con2.="\n<OPTION value=".$obm_q_contacts->f("contact_id");
    if ($obm_q_contacts->f("contact_id") == $con2_id) 
      $dis_sel_con2.=" selected";
    $dis_sel_con2.=">". $obm_q_contacts->f("contact_lastname")." ". $obm_q_contacts->f("contact_firstname") .
    " - " . $obm_q_contacts->f("contact_phone")."</OPTION>";
    }
    $dis_sel_con2.="</SELECT>";

  if ($action == "detailupdate") {
    $dis_sel_con2.="<A HREF=\"".
    $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$con2_id)."\">";
    $dis_sel_con2.="<IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A>";    
    };

  if ($action != "new") {
   $dis_last_update=
   "<TABLE>
   <TR><TD colspan=2>
   <FONT color=\"#$col_label\">$l_last_update </FONT>";
   $dis_last_update.=timestamp_to_date($obm_q_contract->f("datemodif"));
   $dis_last_update.="<FONT color=\"#$col_label\"> $l_at </FONT>";
   $dis_last_update.=timestamp_to_time($obm_q_contract->f("datemodif"));
   $dis_last_update.="</TD></TR></TABLE>";
  }

  if ($action == "detailupdate") {

  $dis_sub="<INPUT TYPE=hidden NAME=hd_soc_aff VALUE=\"".$p_company_id."\">";
  $dis_sub.="<INPUT TYPE=hidden NAME=action VALUE=\"update\">";
  $dis_sub.="<INPUT TYPE=hidden NAME=param_contract VALUE=\"".$obm_q_contract->f("contract_id")."\">";
  $dis_sub.="<INPUT TYPE=submit value=\"$l_update\">";
  $dis_sub.="</FORM>";
  $dis_sub.="&nbsp;  &nbsp;  &nbsp;  &nbsp; ";
  $dis_sub.="<FORM METHOD=POST NAME=form_contract_delete onSubmit=\"if (valider_suppression()) return true; else return false;\" ACTION=\"".$sess->self_url()."\">";
  $dis_sub.="<INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"delete\">";
  $dis_sub.="<INPUT TYPE=\"hidden\" NAME=\"param_contract\" VALUE=\"".$obm_q_contract->f("contract_id")."\">";
  $dis_sub.="<INPUT TYPE=submit value=\"$l_delete\">";
} else {
  $dis_sub="<INPUT TYPE=\"hidden\" NAME=\"hd_soc_aff\" VALUE=\"".$p_company_id."\">";
  $dis_sub.="<INPUT TYPE=\"hidden\" name=\"action\" value=\"insert\">";
  $dis_sub.="<INPUT TYPE=\"submit\" value=\"$l_insert\">";

};

  echo "
<CENTER>
<FORM METHOD=POST NAME=form_contract_update onSubmit=\"if (verifier_aff(this)) return true; else return false;\" ACTION=\"".$sess->self_url()."\">

<TABLE border=0><TR><TD>";

echo "
<TABLE border=0>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_label</FONT><BR>
  <INPUT NAME=tf_label_aff TYPE=text size=40 value=\"$label\">
</TD>
</TR>
</TABLE>

<TABLE>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_number_deal</FONT><BR>
  <INPUT NAME=tf_number_contract TYPE=text size=20 maxlength=40 value=\"$number\">
  </TD>
  <TD><FONT COLOR=\"#$col_label\">$l_date_init</FONT><BR>
  <INPUT NAME=tf_datedebut_aff TYPE=text size=10 value=\"$datebegin\">
</TD>
<TD><FONT COLOR=\"#$col_label\">$l_date_expir</FONT><BR>
  <INPUT NAME=tf_datefin_aff TYPE=text size=10 value=\"$datefin\">
</TD>
</TR>
</TABLE>

<TABLE border=0>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_kind</FONT><BR>
  ".$dis_sel_taff."
  </TD><TD> &nbsp; &nbsp; </TD>
  <TD><FONT COLOR=\"#$col_label\">$l_kind_deal</FONT><BR>
  ".$dis_sel_tdeal."
  </TD>
</TR>
</TABLE>


<TABLE border=0>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_marketing_manager</FONT><BR>
  ".$dis_sel_respc."
  </TD><TD><FONT COLOR=\"#$col_label\">$l_technical_manager</FONT><BR>
  ".$dis_sel_respt."
  </TD><TD> &nbsp; </TD> 
   </TR>
   </TABLE>";
  
  // Pave Societe, contact
  
  echo " </TD><TD valign=top>
  <TABLE bgcolor=\"#$col_label\">
    <TR>
    <TD bgcolor=\"#$col_table\">
    <FONT color=\"#$col_textem\">".$obm_q_company->f("company_name")."</FONT>&nbsp;";
  echo "<A HREF=\"".
      $sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$p_company_id"). "\">";
  echo "<IMG ALIGN=MIDDLE SRC=\"/images/$set_theme/$ico_company\"></A>
    <BR>".$obm_q_company->f("company_address1") ."
    <BR>".$obm_q_company->f("company_zipcode") . " " . $obm_q_company->f("company_town") ."
    </TD>
    </TR><TR>
    <TD valign=center>$l_client_manager<BR>
    ".$dis_sel_con1."
    <BR>
    ".$dis_sel_con2."
    </TD>
    </TR>
  </TABLE>
  <P>
  ".$dis_last_update."
</TD>
</TR>
</TABLE>

<TABLE border=2>
<TR>
<TD><FONT COLOR=\"#$col_label\">".$l_comment."</FONT><BR>
<TEXTAREA NAME=ta_com_aff ROWS=6 COLS=72>";
  echo $comment;
  echo "</TEXTAREA></TD>
<TD align=center colspan=2> &nbsp;  &nbsp;  &nbsp; </TD></TR>
<TR>
<TD align=center valign=bottom colspan=3>";
  
echo "</TD></TR>
<TR>
<TD><FONT COLOR=\"#$col_label\">".$l_clause."</FONT><BR>
<TEXTAREA NAME=ta_clause_aff ROWS=6 COLS=72>";
echo $clause;
echo "</TEXTAREA></TD>
<TD align=center colspan=2> &nbsp;  &nbsp;  &nbsp;
".$dis_sub."
</FORM>
</TD></TR></TABLE>
</CENTER>
";

  
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Consult                                                   //
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//      $obm_q_contract : information about the contract
//      $obm_q_type : list deal's types
//      $obm_q_contacts_obm : list of contacts from internal companies 
//      $obm_q_company : list of companies
//      $obm_q_contact : list of contact from the company 
//      $p_company_id  : id of the company concerned by the contract
///////////////////////////////////////////////////////////////////////////////
function html_contract_consult($obm_q_contract,$obm_q_type,$obm_q_type_deal,$obm_q_contacts_obm,$obm_q_company,$obm_q_contacts,$p_company_id) {

  // -- Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact,$ico_contact_list,$ico_contact_new;
  // - Labels
  global $l_update,$l_delete,$l_insert;
  global $l_label,$l_date_init,$l_kind,$l_category,$l_clause,$l_project_manager,$l_origin,$l_marketing_manager,$l_technical_manager,$l_proposition,$l_amount,$l_contacts,$l_date_alarm,$l_state,$l_last_update,$l_archive,$l_comment,$l_at,$l_number_deal,$l_visibility,$l_public,$l_private,$l_affect_deal,$l_date_expir,$l_client_manager,$l_kind_deal;
  global $l_incident_list,$l_incident_new; 

  global $perms_user,$l_perms_user;
  global $auth, $sess;


  while($obm_q_type->next_record()) {
   if ($obm_q_type->f("contracttype_id") == $obm_q_contract->f("contract_type_id")) 
	$dis_cont_type = $obm_q_type->f("contracttype_label");
  };

  while($obm_q_type_deal->next_record()) {
   if ($obm_q_type_deal->f("dealtype_id") == $obm_q_contract->f("contract_typedeal"))
	$dis_deal_type = $obm_q_type_deal->f("dealtype_label");
  };

  while($obm_q_contacts_obm->next_record()) {
    if ($obm_q_contacts_obm->f("contact_id") == $obm_q_contract->f("contract_responsable_com_id"))
	$dis_cont_mark = $obm_q_contacts_obm->f("contact_lastname") . " " . $obm_q_contacts_obm->f("contact_firstname");
  }

  if ($obm_q_contacts_obm->nf()>0) {
    $obm_q_contacts_obm->seek(0); 
  }

  while($obm_q_contacts_obm->next_record()) {
    if ($obm_q_contacts_obm->f("contact_id") == $obm_q_contract->f("contract_responsable_tech_id"))
	$dis_cont_tec = $obm_q_contacts_obm->f("contact_lastname") . " " . $obm_q_contacts_obm->f("contact_firstname");
  }
 
  while($obm_q_contacts->next_record()) {
    if ($obm_q_contacts->f("contact_id") == $obm_q_contract->f("contract_responsable_client_id")) 
        $dis_cont_cli1 = "<TD>".$obm_q_contacts->f("contact_lastname")." ". $obm_q_contacts->f("contact_firstname") . " - " . $obm_q_contacts->f("contact_phone")."</TD>";
    }
 

  if ($obm_q_contacts->nf()>0) {
    $obm_q_contacts->seek(0);
  }
  // 2 ième Responsable Client 
  while($obm_q_contacts->next_record()) {
   if ($obm_q_contacts->f("contact_id") == $obm_q_contract->f("contract_responsable_client2_id")) 
       $dis_cont_cli2 = "<TD>".$obm_q_contacts->f("contact_lastname")." ". $obm_q_contacts->f("contact_firstname") . " - " . $obm_q_contacts->f("contact_phone")."</TD>";
    }

  $dis_date_date = timestamp_to_date($obm_q_contract->f("datemodif"));
  $dis_date_time = timestamp_to_time($obm_q_contract->f("datemodif"));

  if ($auth->auth["perm"] != $perms_user) {
    $dis_update = "<FORM METHOD=POST NAME=form_contract_update ACTION=\"".$sess->self_url()."\">";
    $dis_update .= "<INPUT TYPE=hidden NAME=action VALUE=\"detailupdate\">";
    $dis_update .= "<INPUT TYPE=hidden NAME=param_contract VALUE=\"".$obm_q_contract->f("contract_id")."\">";
    $dis_update .= "<INPUT TYPE=submit value=\"$l_update\">";
    $dis_update .= "</FORM>";

}

  echo "
<CENTER>

<TABLE border=0><TR><TD valign=top> 

  <A HREF=\"".$sess->url("incident_index.php?action=search&amp;param_contract=".$obm_q_contract->f("contract_id")."")."\">
   <IMG BORDER=0  SRC=\"/images/$set_theme/$ico_contact_list\"></A>
        <FONT SIZE=-1><A HREF=\"" . $sess->url("incident_index.php?action=search&amp;param_contract=".$obm_q_contract->f("contract_id")."") . "\">$l_incident_list</A></FONT>";

  if ($auth->auth["perm"] != $perms_user) {
    echo "<BR>
      <A HREF=\"" . $sess->url("incident_index.php?action=new&amp;param_contract=".$obm_q_contract->f("contract_id")."") . "\"><IMG BORDER=0
        SRC=\"/images/$set_theme/$ico_contact_new\"></A>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("incident_index.php?action=new&amp;param_contract=".$obm_q_contract->f("contract_id")."") . "\">$l_incident_new</A></FONT>";
  }
echo "
</TD><TD>
<TABLE border=0><TR><TD>";

echo "
<TABLE border=0><TR><TD valign=top align=left>";
echo "<TABLE>
<TR>
   <TD><FONT COLOR=\"#$col_label\">$l_label</FONT></TD><TD>";
echo $obm_q_contract->f("contract_label");
echo "</TD>
      </TR><TR>
  <TD><FONT COLOR=\"#$col_label\">$l_number_deal</FONT></TD><TD>";
echo $obm_q_contract->f("contract_numero");
echo "</TD>
</TR>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_date_init</FONT></TD><TD>";
echo $obm_q_contract->f("contract_debut");
echo "</TD>
</TR>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_date_expir</FONT></TD><TD>";
echo $obm_q_contract->f("contract_expiration");
echo "</TD>
</TR>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_kind</FONT></TD><TD>
	".$dis_cont_type."
  </TD>
</TR><TR>
  <TD><FONT COLOR=\"#$col_label\">$l_kind_deal</FONT></TD><TD>
  ".$dis_deal_type."
  </TD>
</TR>
</TABLE>

<TABLE border=0>
<TR>
  <TD><FONT COLOR=\"#$col_label\">$l_marketing_manager</FONT></TD><TD>
  ".$dis_cont_mark." 
  </TD></TR><TR><TD><FONT COLOR=\"#$col_label\">$l_technical_manager</FONT></TD><TD>
  ".$dis_cont_tec."	 
  </TD>
</TR>
</TABLE>";

  // Pave Societe, contact
  $obm_q_company->next_record();
  echo " </TD><TD valign=top>
  <TABLE bgcolor=\"#$col_label\">
    <TR>
    <TD bgcolor=\"#$col_table\" colspan=2>
    <FONT color=\"#$col_textem\">".$obm_q_company->f("company_name")."</FONT>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    echo "<A HREF=\"".
      $sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$p_company_id")."\">";
    echo "<IMG ALIGN=MIDDLE SRC=\"/images/$set_theme/$ico_company\"></A>
    <BR>".$obm_q_company->f("company_address1") ."
    <BR>".$obm_q_company->f("company_zipcode") . " " . $obm_q_company->f("company_town") ."
    </TD>
    </TR><TR>
    <TD valign=center colspan=2>$l_client_manager</TD>
    </TR><TR>
    ".$dis_cont_cli1."
    <TD><A HREF=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$obm_q_contract->f("contract_responsable_client_id"))."\">";
    echo "<IMG ALIGN=middle SRC=\"/images/$set_theme/$ico_contact\"></A></TD>";
    echo "</TR><TR>
    ".$dis_cont_cli2."
    <TD><A HREF=\"".
      $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=".$obm_q_contract->f("contract_responsable_client2_id"))."\">";
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
echo nl2br($obm_q_contract->f("contract_comment")); 
echo "<BR><BR><TD valign=top><FONT COLOR=\"#$col_label\">".$l_clause."</FONT><BR>";
echo nl2br($obm_q_contract->f("contract_clause")); 
echo "</TD></TR><TR>
<TD align=center><BR>
".$dis_update." 
&nbsp;</TD></TR></TABLE>
</TD></TR></TABLE>
</TD></TR></TABLE>";
echo "</CENTER>";

}


///////////////////////////////////////////////////////////////////////////////
// Formulaire de recherche de contract                                        //
// Contract Contract search Form                                               //
///////////////////////////////////////////////////////////////////////////////
// Parameters : 
// ------------
//   $obm_q_type : list of contracts types available
//   $p_label : value of the "label" field
//   $p_company : value of the "company name" field
//   $p_type_id : value of the "deal type" item selected in the combo list
//   $p_dateafter : value of the "date after" field
//   $p_datebefore : value of the "date before" field
//   $p_manager_id : value of the "manager" item selected in the combo list
/////////////////////////////////////////////////////////////////////////////////
function html_contract_search_form($obm_q_contacts_obm,$obm_q_type,$contract) {

  global $l_label_start,$l_label_libelle,$l_company,$l_kind ,$l_all,$l_all_f,$l_category,$l_state,$l_after,$l_before,$l_manager;
  global $l_find,$l_marketing_manager,$l_technical_manager; 
  global $sess;

  $p_label = $contract["label"];
  $p_company = $contract["companyname"];
  $p_type_id = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $sel_com_aff = $contract["comercial"];
  $sel_tech_aff = $contract["technical"];
  $p_num_aff = $contract["numero"]; 
 
  $dis_cont_typ  = "<SELECT name=\"sel_type_aff\"><OPTION value=\"_TOUT_\">" . $l_all;
  while($obm_q_type->next_record()) {
    $dis_cont_typ .=  "\n<OPTION value=".$obm_q_type->f("contracttype_id");
    if ($p_type_id == $obm_q_type->f("contracttype_id")) 
     $dis_cont_typ .= " selected";
    $dis_cont_typ .= ">" . $obm_q_type->f("contracttype_label");
    }
    $dis_cont_typ .= "</SELECT>";

    if ($obm_q_contacts_obm->nf()>0) {
    $obm_q_contacts_obm->seek(0);
  }

  $dis_resp_tec = "<SELECT name=\"sel_resptech_aff\"><OPTION value=\"_TOUT_\">" . $l_all;
  while($obm_q_contacts_obm->next_record()) {
    $dis_resp_tec .= "\n<OPTION value=".$obm_q_contacts_obm->f("contact_id");
    if ($obm_q_contacts_obm->f("contact_id") == $sel_tech_aff) 
      $dis_resp_tec .= " selected";
    $dis_resp_tec .= ">". $obm_q_contacts_obm->f("contact_lastname") . " " . $obm_q_contacts_obm->f("contact_firstname")." </OPTION>";
  };
  $dis_resp_tec .="</SELECT>";


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
     "<INPUT TYPE=text NAME=tf_num_aff size=20 value=\"".
     $p_num_aff.
     "\"></TD>";
//---------------------------------------------------------------------------
echo "<TD>&nbsp;&nbsp;</TD>";
//---------------------------------------------------------------------------
//---| Nom de SOCIETE |------------------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>" .
     $l_company . "</FONT><BR>" .
     "<INPUT TYPE=text NAME=tf_soc_aff size=20 value=\"" .
     $p_company.
     "\"></TD>";
//---------------------------------------------------------------------------
echo "<TD>&nbsp;&nbsp;</TD>";
//---------------------------------------------------------------------------
//---| DATE EXPIRATION APRES |---------------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>" .
     $l_after . "</FONT><BR>" .
     "<INPUT TYPE=text NAME=tf_dateapres_aff size=10 value=\"" .
     $p_dateafter .
     "\"></TD>";
 echo "<TD> &nbsp; </TD>";
//---------------------------------------------------------------------------
//---| DATE EXPIRATION AVANT |---------------------------------------------------
//---------------------------------------------------------------------------
echo "<TD><FONT SIZE=-2>" .
     $l_before . "</FONT><BR>" .
     "<INPUT TYPE=text NAME=tf_dateavant_aff size=10 value=\"" .
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
     "<INPUT TYPE=text NAME=tf_label_aff size=20 value=\"".
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
  echo "</TD><TD><FONT SIZE=-2>  &nbsp; &nbsp; &nbsp; &nbsp;".$l_technical_manager. " </FONT><BR>
  ".$dis_resp_tec; 
  //  echo "</TR><TR colspan=2></TD><TD><FONT COLOR=\"#$col_label\">$l_client_manager</FONT><BR><SELECT name=\"sel_respclient_aff\">";
  //while($obm_q_contacts->next_record()) {
  //echo "\n<OPTION value=".$obm_q_contacts->f("contact_id");
  //if ($obm_q_contacts->f("contact_id") == $con2_id) echo " selected";
  //echo ">". $obm_q_contacts->f("contact_lastname")." ". $obm_q_contacts->f("contact_firstname") .
  //"</OPTION>";
  //}
  
  //echo "</SELECT></TD>";
  


  echo "<TD>&nbsp;&nbsp;</TD>";

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
// Display the Contact search result                                         //
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : state, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function dis_contract_search_list($contract) {
  global $l_no_found;
  global $auth, $new_order, $order_dir, $new_order2;

  $pref_q = run_query_display_pref($auth->auth["uid"], "contract");
  $obm_q = run_query_search($contract, $order_dir, $new_order, $new_order2);
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
//     $obm_q_deals : list of deals
//     $obm_q_display_options : fields to display in the list of deals 
//     $nb_deals : nb deals found
//     $page : number of the current page 
//
//     $p_label, 
//     $p_company,
//     $p_type_id,
//     $p_cat_id,
//     $p_status_id,
//     $p_dateafter,
//     $p_datebefore,
//     $p_manager_id, 
//     $p_new_order, $p_new_order2 : new values for the "order by" of the query
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_list($obm_q_contracts,$obm_q_display_options,$nb_contract,$contract) {

  //-- Themes
  global $col_tableh,$col_textem,$col_table,$col_error;
  //-- Labels
  global $l_archive_first,$l_label,$l_company,$l_kind,$l_category,$l_state,$l_date_alarm;
  global $l_archive_update, $l_found, $l_options_dis;
  //-- Search parameters
 
  global $set_rows, $set_rows_default;
  //-- Authentication
  global $perms_user,$l_perms_user;
  global $auth, $sess;



  $p_label = $contract["label"];
  $p_type_id = $contract["type"];
  $p_dateafter = $contract["dateafter"];
  $p_datebefore = $contract["datebefore"];
  $p_company = $contract["companyname"];

//  $p_status_id = $contract["etat"]; 
//  $p_manager_id = $contract["manager"];
//  $p_archive = $contract["arc"];
//  $p_cat_id = $contract["cat"];

  $sel_com_aff = $contract["comercial"];
  $sel_tech_aff = $contract["technical"];
  $p_num_aff = $contract["numero"]; 

  echo "<FONT color=\"#$col_textem\">";
  echo "$nb_contract $l_found";
  echo "</FONT>";



  // Table of content (pages)

 $url = $sess->url("contract_index.php?" .
      "action=search&amp;tf_label_aff=$p_label&amp;tf_soc_aff=$p_company&amp;sel_respcomm_aff=$sel_com_aff&amp;sel_type_aff=$p_type_id&amp;sel_etat_aff=$p_status_id&amp;tf_number_contract=$p_num_aff&amp;sel_resptech_aff=$sel_tech_aff&amp;tf_dateapres_aff=$p_dateafter&amp;tf_dateavant_aff=$p_datebefore");
  echo "<br>";

   $obm_q_contracts->seek($first_row);
   $cpt = 0;

  
   $dis_contracts_list=new OBM_DISPLAY("DATA",$obm_q_display_options);
   $dis_contracts_list->data_set = $obm_q_contracts;
   $dis_contracts_list->data_url = $url;
   $dis_contracts_list->data_header = "both";
   $dis_contracts_list->display() ;

}


///////////////////////////////////////////////////////////////////////////////
// Display new contract : Company Form                                        //
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//   $obm_q_company    : list of all the companies  
//   $param_marketing  : id of the marketing manager of the parentdeal (= the default marketing manager of the new deal) 
//   $param_technical  : id of the technical manager of the parentdeal (= the default technical manager of the new deal)
///////////////////////////////////////////////////////////////////////////////
function html_new_contract_company($obm_q_company,$contract) {

  global $l_deal_select_company,$l_select_company;
  global $sess;

  $param_marketing = $contract["marketing_id"];
  $param_technical = $contract["technical_id"];

  $dis_compagny="<SELECT name=\"param_company\" size=10>";
  while($obm_q_company->next_record()) {
    $dis_compagny.="\n<OPTION value=".$obm_q_company->f("company_id").">".$obm_q_company->f("company_name")."</OPTION>";
  }

  $dis_compagny.="</SELECT>";

echo "
<CENTER>
<FORM METHOD=POST NAME=form_company onSubmit=\"if (verifie(this)) return true; else return false;\" ACTION=\"".$sess->self_url()."\">";

echo $l_deal_select_company;
echo "
<P>
".$dis_compagny."
<P>
<INPUT TYPE=\"hidden\" name=\"action\" value=\"new\">
<INPUT TYPE=\"hidden\" name=\"param_marketing\" value=\"$param_marketing\">
<INPUT TYPE=\"hidden\" name=\"param_technical\" value=\"$param_technical\">
<INPUT TYPE=\"submit\" value=\"$l_select_company\">
</FORM>
</CENTER>";

}


///////////////////////////////////////////////////////////////////////////
//  Contracts Administration Forms :                                              
 ///////////////////////////////////////////////////////////////////////////
function html_contract_admin_form($obm_q_type) {
  
  // Theme : 
  global $col_a_table,$col_textem;
  // Labels : 
  global
    $l_type_manage,$l_type_new,$l_type_exist,
    $l_status_manage,$l_status_exist,$l_status_new,
    $l_category_manage,$l_category_exist,$l_category_new,
    $l_category_label,$l_category_minilabel,$l_label,$l_minus,$l_plus,$l_order;
  // Actions : 
  global
    $l_type_delete,$l_type_update,$l_type_insert;
  // Messages : 
  global 
    $l_type_insert_ok,
    $l_type_insert_error,
    $l_type_update_ok,
    $l_type_update_error,
    $l_type_delete_error,
    $l_type_del_verif_error,
    $l_type_delete_ok;
  global $sess;

  $dis_sel_type = "<SELECT NAME=\"sel_type\" SIZE=4 >";
  while ($obm_q_type->next_record()) {
    $dis_sel_type .= "\n<OPTION value=\"" . $obm_q_type->f("contracttype_id") . "\">".$obm_q_type->f("contracttype_label") .
           "</OPTION>";
  }
   $dis_sel_type .= "</SELECT>";

  //$obm_temp=new DB_OBM;	
	  
  echo "<CENTER>";
  echo "\n<FORM name=\"form_admin_type_delete\" " .
        "METHOD=post action=\"".$sess->self_url()."\">";
  echo "\n<TABLE WIDTH=90% border=1 bgcolor=\"#$col_a_table\">" .
        "\n<TR><TD colspan=5 BGCOLOR=\"#$col_a_tableh\">" .
        "<I><FONT COLOR=\"#$col_textem\">" . $l_type_manage .
        "</FONT></I></TD></TR>\n";
  echo "<TR><TD>";


//---------------------//
// Type Delete Section //
//---------------------//
 echo "\n<TABLE border=0><TR><TD>" .
        $l_type_exist . "</TD>" .
       "\n<TD> ".$dis_sel_type."</TD>";
  echo "\n<TD>".
        "<INPUT TYPE=\"hidden\" name=\"action\" value=\"admintypedelete\">".
        "<INPUT TYPE=\"submit\" name=\"sub_type\" value=\"" . $l_type_delete .
        "\" onClick=\"if (check_type_del(this.form)) return true; " .
        "else return false;\">" . "</TD>";
  echo "\n</TR>";
  echo "</FORM>\n";

//---------------------//
// Type Update Section //
//---------------------//
  echo "\n<FORM name=\"form_admin_type_update\" " .
        "METHOD=post action=\"".$sess->self_url()."\">";

  echo "\n<TR><TD colspan=2 align=center>" .
        "<INPUT TYPE=hidden name=\"sel_type\" value=\"\">".
        "<INPUT TYPE=hidden name=\"action\" value=\"admintypeupdate\">".
        "<TABLE border=0><TR><TD>".
        "<INPUT TYPE=text name=\"tf_type_upd\">" .
        "</TD></TR>".
        "</TABLE>".
        "</TD><TD>".
        "<INPUT TYPE=submit name=\"sub_type\" value=\"" . $l_type_update .
        "\" onClick=\"if (check_type_upd(this.form,document.form_admin_type_delete)) return true; " .
        "else return false;\">".
    "</TD><TD valign=center>";
        


   // End of the contract contract type management left cell
  echo "</TD></TR></TABLE>\n" .
  "</TD>";
  echo "<TD>";

  echo "</FORM>\n";
//------------------//
// New Type Section //
//------------------//
  echo "\n<FORM name=\"form_admin_type_new\" " .
        "METHOD=post action=\"".$sess->self_url()."\">";
  echo "\n<TABLE border=0><TR>\n" .
        "<TD align=right>" . $l_type_new .
        " : <INPUT type=\"text\" name=\"tf_type_new\"></TD>".
        "</TR>\n" .
        "<TR><TD align=center>" .
        "<INPUT TYPE=hidden name=\"action\" value=\"admintypeinsert\">".
        "<INPUT type=submit name=\"sub_type\" value=\"" . $l_type_insert .
        "\" onClick=\"if (check_type_new(this.form)) return true; " .
        "else return false;\">";
         

  echo "</TD></TR></TABLE>\n" .
        "</TD></TR></TABLE>\n";

  echo "</FORM>\n";
  echo "</CENTER>";

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
    <SELECT name=\"param_contract\" size=10>";

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




