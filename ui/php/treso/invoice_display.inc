<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_display.inc
//     - Desc : Invoice Display file
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// to see tables during dev
$border= ($set_debug>0)? 2 : 0;
$cellspacing = ($set_debug>0)? 5 : 1;

///////////////////////////////////////////////////////////////////////////////
// Display Invoice Form              
// params :
//  $q_invoice : if updating, contains the invoice current info
//  $action : action 
//  $q_status : id et label des != status
///////////////////////////////////////////////////////////////////////////////
// formulaire de création de facture :
function html_invoice_form($q_invoice, $action, $q_status, $p_nb_deals = 0, $p_deal_linked="") {
  // - Themes :
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$ico_company,$ico_contact;
  // -- Labels
  global $l_label, $l_number, $l_comment, $l_deal,
    $l_amount_HT, $l_amount_TTC, $l_status, $l_date, $l_pick_deal, $l_inout, $l_client, $l_fournisseur;
  global $l_insert,$l_update, $l_delete;
  global $default_invoice_numbering;
  global $q_company;
  global $sess, $auth;
  global $default_tax, $l_tax_rate, $l_compute_tax; 

  global $cellspacing,$border;

  if ($action == "new" && $p_deal_linked != "") 
    $js_fct_invoice = "check_invoice_link";
  else 
    $js_fct_invoice = "check_invoice";
  // depending on the action, we fill or not the form 
  // we use those vars to skip tests on the action value
  // for each form field...
  if ($action == "new") {
    $invoice = "";
    $label = $date = $comment = $invoice_status = "";
    $inout = $amount_HT = $amount_TTC = "";
    $number = date ($default_invoice_numbering);
    // when creating from a deal, we must use the same "inout" as
    // the deal...
    if ($p_deal_linked != "") {
      $inout = run_query_deal_get_inout ($p_deal_linked);
      //      $readonly = "readonly"; doesn't work @#$!
    }
  } else {
    $label = $q_invoice->f("invoice_label");
    $number = $q_invoice->f("invoice_number");
    $comment = $q_invoice->f("invoice_comment");
    $invoice_status = $q_invoice->f("invoice_invoicestatus_id");
    $date = $q_invoice->f("invoice_date");
    $inout = $q_invoice->f("invoice_inout");
    $amount_HT = $q_invoice->f("invoice_amount_HT");
    $amount_TTC = $q_invoice->f("invoice_amount_TTC");
    $invoice = $q_invoice->f("invoice_id");
  }

  // if we duplicate we have to modify some fields :
  if ($action == "duplicate") {
    $comment = "duplicata de <$label>\n".$comment;
    $date = "";
    $label .= "-".date("Y-m-d");
  }
  
  echo "
<center>
 <form method=\"post\" action=\"\">
  <table border=\"$border\" cellspacing=\"$cellspacing\">
  <tr>
   <td>
";
  echo "
    <table border=\"$border\" cellspacing=\"$cellspacing\" width=\"100%\">
     <tr>";
  //--------------------------------------------------------------------------
  // number
  echo "
      <td width=\"20%\"><font color=\"#$col_label\">$l_number</font><br>
       <input name=tf_number type=text size=11 value=\"$number\">
      </td>";
  
  // label
  echo "
      <td width=\"60%\"><font color=\"#$col_label\">$l_label</font><br>
       <input name=tf_label type=text size=40 value=\"$label\">
      </td>";

 // date 
  echo "
      <td width=\"20%\"><font color=\"#$col_label\">$l_date</font><br>
       <input name=tf_date type=text size=12 value=\"$date\">
      </td>";

  echo "
     </tr>
    </table>"; 
  echo "
   </td>
  </tr><tr>
   <td>
    <table border=$border cellspacing=$cellspacing width=\"100%\">";

  // invoicestatus 
  echo"
     <tr>
      <td><font color=\"#$col_label\">$l_status</font><br>
       <select name=\"sel_status\">"; 
  while($q_status->next_record()) {
    echo "\n<option value=".$q_status->f("invoicestatus_id"); 
    if ($q_status->f("invoicestatus_id") == $status) {
      echo " selected ";
    }
    echo ">".$q_status->f("invoicestatus_label"); 
  }
  echo "</select>
      </td>"; 

  // inout : customer or provider
  // not to be display if creating an invoice from a deal
  if ($action == "new" && $p_deal_linked != "") {
    // but we need to keep the inout...
    echo "
      <td>
       <input type=\"hidden\" name=\"hd_inout\" value=\"$inout\">
      </td>";
  } else {
    echo "
      <td>\n
       <font color=\"#$col_label\">".$l_inout."</font><br>\n";
    echo "
    $l_client &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"+\"";
    if ($inout == "+") {
      echo " checked";
    }
    echo ">\n
    $l_fournisseur &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"-\"";
    if ($inout == '-') {
      echo " checked";
    }
    echo ">\n
      </td>";
  }
  echo "
     </tr>";
  // amounts and auto-compute stuff :
  echo "
     <tr><td colspan=\"2\">
      <table border=\"$border\" cellspacing=\"$cellspacing\" width=\"100%\">";
   // amount_HT ==> HT : without charges
  echo "
       <tr>
        <td><font color=\"#$col_label\">$l_amount_HT</font><br>
         <input name=\"tf_amount_HT\" type=text size=15 value=\"$amount_HT\">
        </td>"; 
  // tax rate :
  echo "
        <td><font color=\"#$col_label\">$l_tax_rate</font></br>
         <input name=\"tf_tax_rate\" type=\"text\" value=\"$default_tax\" ".
    " size=\"8\" >
        </td>";
  // amount_TTC ==> TTC : charges included 
  echo " 
        <td><font color=\"#$col_label\">$l_amount_TTC</font><br> 
         <input name=\"tf_amount_TTC\" type=text size=15 value=\"$amount_TTC\">
        </td>";
  // button to compute TTC amount :
  echo "
        <td>
         <input type=\"button\" name=\"b_compute\" value=\"$l_compute_tax\" ".
    "onClick=\"compute_tax(this.form);\">
        </td>
       </tr>";
  echo "
      </table>
     </td></tr>";
  echo "
    </table>
   </td>
  </tr>";
//  echo "
// </table>";

  // comment
  echo "
  <tr>
   <td>
     <font color=\"#$col_label\">$l_comment</font><br>
     <textarea name=ta_comment rows=6 cols=72>";
  echo $comment;
  echo "</textarea>
   </td>
  </tr>";

  // insert or update/delete buttons
  echo "
  <tr>
   <td>
    <br><center>
     <input type=\"hidden\" name=\"hd_nb_deals\" value=\"".$p_nb_deals."\">";
  if (($action=="update") || ($action =="detailupdate")) {
    echo "<input type=\"hidden\" name=\"hd_rd_inout\" value=\"".$q_invoice->f("invoice_inout")."\">";
  }
  if ($action=="detailupdate") {
    $url_update = $sess->url ("invoice_index.php?action=update&amp;".
			      "param_invoice=".$q_invoice->f("invoice_id"));
    echo "
     <table cellspacing=\"$cellspacing\" border=\"$border\">
      <tr>
       <td>
        <input type=\"button\" value=\"$l_update\" ".
      "onClick=\"if (check_invoice (this.form)) { ".
      " this.form.action='".$url_update."' ; ".
      " this.form.submit () ".
      "}\">
       </td>";
    // we can delete only if not paid
    $payments_connected = run_query_search_payment_invoice($invoice, $new_order, $new_order2, $order_dir, -1);
    $url_delete = $sess->url ("invoice_index.php?action=delete".
			      "&amp;param_invoice=".$q_invoice->f("invoice_id"));
    if ($payments_connected->nf() == 0) {
      echo "
       <td>
         <input type=\"button\" value=\"$l_delete\" ".
	"onClick=\"if (valider_suppression (this.form)) { ".
	" this.form.action='".$url_delete."' ; ".
	" this.form.submit () ".
	"}\">
       </td>";
    }
    echo "
      </tr>
     </table>"; 
  } elseif (($action == "duplicate") || ($action == "new")) {
    if ($action == "new") {
      $url = $sess->url ("invoice_index.php?action=insert");
    } elseif ($action == "duplicate") {
      $url = $sess->url ("invoice_index.php?action=insert".
			 "&amp;param_invoice=".$q_invoice->f("invoice_id"));
    }
    // inserting or duplicating 
    echo "
     <input type=\"hidden\" name=\"hd_deal_linked\" value=\"".$p_deal_linked."\">
     <input type=\"button\" value=\"$l_insert\" ".
      "onClick=\"if (".$js_fct_invoice."(this.form)) { ".
      " this.form.action='".$url."' ; ".
      " this.form.submit () ".
      "}\">";
  }
  echo "
    </center>
   </td>
  </tr>
 </table>";
  
  echo"
 </form>
</center>
";
} // html_invoice_form

///////////////////////////////////////////////////////////////////////////////
// invoice search Form
// Parameters :
//  $p_action : current action in the application
//  $q_status : infos concernant les != status
///////////////////////////////////////////////////////////////////////////////
function html_invoice_search_form ($p_action,$q_status, $invoice) {
  global $l_label_start, $l_number, $l_amount_HT, $l_amount_TTC;
  global $l_status, $l_deal, $l_date_apres, $l_date_avant, $l_inout;
  global $l_deal, $l_company, $l_include_archive;
  global $l_find, $l_all_f, $l_both, $l_client, $l_fournisseur;
  global $sess;
  global $cellspacing,$border;

  $cellspacing = 10;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $amount_HT = $invoice["HT"];
  $amount_TTC = $invoice["TTC"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $deal = $invoice["deal"];
  $company = $invoice["company"];
  $archive = ($invoice["archive"]) ? "checked" : "" ;

  $url_form = $sess->url ("invoice_index.php?action=search");

  // "external" table
  // 1 row, 2cols :
  // 1 for the form itself, 1 for the submit button
  echo "
<center>
 <table border=\"$border\" cellspacing=\"$cellspacing\">";
  echo"
  <tr><td>
  <form method=\"post\" name=\"form_invoice\"".
    " onSubmit=\"if (check_form(this) == false) return false ; ".
    "else return true;\" ".
    "action=\"".$url_form."\">";
  // form table (1 cell)
  echo "
   <table  border=\"$border\" cellspacing=\"$cellspacing\">
    <tr><td>";  
  // first form line table :
  // label, number, amounts HT & TTC
  echo "
     <table  border=\"$border\" cellspacing=\"$cellspacing\" width=\"100%\">
      <tr>";  
  //--------------------------------------------------------------------------
  // label
  echo "<td width=\"40%\">
       <font size=\"-2\">".$l_label_start."</font><br>
       <input type=\"text\" name=\"tf_label\" size=\"20\" value=\"".$label.
    "\">
      </td>";
  //--------------------------------------------------------------------------
  // number
  echo "<td width=\"20%\">
       <font size=\"-2\">".$l_number."</font><br>
       <input type=\"text\" name=\"tf_number\" size=\"12\" value=\"".$number.
    "\">
      </td>";
  //--------------------------------------------------------------------------
  // amount_HT
  echo "<td width=\"20%\">
       <font size=\"-2\">".$l_amount_HT."</font><br>
       <input type=\"text\" name=\"tf_amount_HT\" size=\"15\" value=\"".$amount_ht.
    "\">
      </td>";
  //--------------------------------------------------------------------------
  // amount_TTC
  echo "<td width=\"20%\">
       <font size=\"-2\">".$l_amount_TTC."</font><br>
       <input type=\"text\" name=\"tf_amount_TTC\" size=\"15\" value=\"".$amount_ttc.
    "\">
      </td>";
  // end of first form line table
  echo "</tr>
     </table>
      </td></tr>";
  // beginning of second form line table
  // status, date after, date before, archive 
  echo "
    <tr><td>
     <table  border=\"$border\" cellspacing=\"$cellspacing\" width=\"100%\">
      <tr>";
  // status
  echo "<td>
       <font size=-2>".$l_status."</font><br>
       <select name=\"sel_status\">
        <option value=\"_TOUT_\">".$l_all_f;

  while ($q_status->next_record()) {
    echo "
        <option value=".$q_status->f("invoicestatus_id");
    if ($q_status->f("invoicestatus_id")==$status) echo " selected";
    echo ">".$q_status->f("invoicestatus_label");
  }
  echo "
       </select>
      </td>";
  //--------------------------------------------------------------------------
  // date after :
  echo "<td>
       <font size=\"-2\">".$l_date_apres."</font><br>
       <input type=\"text\" name=\"tf_date_after\" size=12 ".
    "value=\"".$date_after.
    "\">
      </td>";
  // date before :
  echo "<td>
       <font size=\"-2\">".$l_date_avant."</font><br>
       <input type=\"text\" name=\"tf_date_before\" size=12 ".
    "value=\"".$date_before.
    "\">
      </td>";
  // archive
  echo "<td>
       <font size=\"-2\">".$l_include_archive."</font><br>
       <input type=\"checkbox\" name=\"cb_archive\" value=\"y\" $archive> 
      </td></tr>";
  // end of second form line table
  echo "
     </table>
    </td></tr>
    <tr><td>";
  // beginning of third form line table
  // kind, deal, company
  echo "
     <table  border=\"$border\" cellspacing=\"$cellspacing\" width=\"100%\">";
  // radios : customer/provider/both invoice  ?
  echo "
      <tr><td>
       <font size=-2>".$l_inout."</font><br>
       $l_both &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"_TOUT_\"";
  if (($inout == "_tout_")||($inout=="")) {
    echo "checked";
  }
  echo ">
       $l_client &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"+\"";
  if ($inout == "+") {
    echo " checked ";
  }
  echo ">
       $l_fournisseur &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"-\"";
  if ($inout == "-") {
    echo " checked ";
  }
  echo ">";
  // deal connected
  echo "
      </td><td>
       <font size=\"-2\">$l_deal</font><br>
       <input type=\"text\" name=\"tf_deal\" value=\"".$deal."\">
      </td>";
  // company of the deal of the invoice...
  echo "<td>
        <font size=\"-2\">$l_company</font><br>
        <input type=\"text\" name=\"tf_company\" value=\"".
    $company."\">
      </td></tr>";
  // end of third line form table
  echo "
     </table>";
  // end of form table
  echo "
    </td></tr>
   </table>
  </td>";
//  //--------------------------------------------------------------------------
  // bouton valider
  echo "<td align=\"center\"> 
   <input name=\"submit\" type=\"submit\" value=\"$l_find\">
  </td>";
  // end of "external" table
  echo "</tr>
  </form><hr>
  </td></tr>
 </table>
</center>
";
}

///////////////////////////////////////////////////////////////////////////////
// Display Search result (if it has to be done                               //
// if ($set_display != "no") or if it comes from the submit bouton           //
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// $q_invoice : list of invoices
// $q_display_options : fields to display in the list of invoices
// $nb_invoices : number of invoices
//
function html_invoice_search_list ($q_invoices, $q_display_options, $nb_invoices, $invoice) {
  //-- Themes
  global $col_tableh,$col_textem,$col_table,$col_error, $col_frs, $col_client;
  //-- Labels
  global $l_found, $l_archive_invoices, $l_archive_update;
  global $l_label, $l_number, $l_amount_HT, $l_amount_TTC, 
    $l_date, $l_status, $l_deal;
  //-- Search parameters
  global $set_rows, $set_rows_default;
  //-- Authentication
  global $perms_user,$l_perms_user, $perms_admin;
  global $auth, $sess;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $amount_HT = $invoice["HT"];
  $amount_TTC = $invoice["TTC"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $deal = $invoice["deal"];
  $company = $invoice["company"];

  echo "<center><font color=\"#$col_textem\">\n";
  echo "$nb_invoices $l_found\n";
  echo "</font><br></center>\n";

  $url = $sess->url("invoice_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount_HT=$amount_HT". 
		    "&amp;tf_amount_TTC=$amount_TTC".
		    "&amp;sel_status=$status".
		    "&amp;tf_deal=$deal".
		    "&amp;rd_inout=$inout"); 

  $dis_invoice = new OBM_DISPLAY ("DATA", $q_display_options);
  $dis_invoice->data_set = $q_invoices;
  $dis_invoice->data_header = "top";
  $dis_invoice->data_url = $url;

  // checkbox to allow archiving..
  if ($auth->auth["perm"] == $perms_admin) {
    $url_archive = $sess->url ("invoice_index.php?action=updatearchive");
    echo "<form method=\"post\" action=\"$url_archive\">";
    $dis_invoice->data_cb_text = $l_archive_invoices;
    $dis_invoice->data_idfield = "invoice_id";
    $dis_invoice->data_cb_side = "left";
    $dis_invoice->data_cb_name = "archive_";
  }

  $dis_invoice->display ();
  if ($auth->auth["perm"] == $perms_admin) {
    echo "<input type=\"submit\" value=\"$l_archive_update\">
 </form>";
  }
} // html_invoice_search_list ()

//////////////////////////////////////////////////////////////////////////
// Display the screen used to change the display parameters of invoices
//////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//    $q_options_display : list of fields to display or not
///////////////////////////////////////////////////////////////////////////
function dis_invoice_display_pref ($invoice_options_display, $deal_options_display) {
  // Label
  global $l_invoice_options, $l_deal_options;
  //Image
  //  global $set_theme ;
   
  echo "<center>\n" ;
  echo "<table><tr valign=\"top\">";
  
  echo "<td>";
  // invoice :
  $dis_pref = new OBM_DISPLAY("PREFERENCES",$invoice_options_display) ;
  $dis_pref->display_entity = "invoice"; 
  $dis_pref->pref_title = $l_invoice_options;
  $dis_pref->pref_dis_help = 0 ;// we display help only once...

  $dis_pref->display() ;

  echo "</td><td>" ;

  // deals :
  $dis_pref = new OBM_DISPLAY("PREFERENCES",$deal_options_display) ;
  $dis_pref->display_entity = "invoice"; 
  $dis_pref->pref_title = $l_deal_options;
  $dis_pref->pref_dis_help = 0 ;

  $dis_pref->display() ;

  echo "</td></tr></table>";
  $dis_pref->dis_pref_help ();
  
  echo  "\n</center>" ;
}
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//  $action = action that brought us here
//  $q_invoice : information about the invoice
//  $q_status : all kind of status available for an invoice
//  $q_deal/$q_payment : deals/payments connected to that invoice
//  $options_{deals,payments} : display pref for deals/payments
//
// screen to access if you want to add/remove deals linked to an invoice
///////////////////////////////////////////////////////////////////////////////
function html_invoice_consult($action, $q_invoice, $q_status, $q_deal, $options_deals, $q_payment, $options_payments) {
  // -- Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok;
  // - Labels
  global $l_update,$l_delete,$l_insert, $l_invoice_infos,$l_deals_todel, 
    $l_invoice_del_deal, $l_del_deal_chosen, $l_list_deal,$l_no_deal, 
    $l_list_payment, $l_no_payment, $l_payments_todel, $l_invoice_del_payment,
    $l_del_payment_chosen;
  global $l_add_deal, $l_del_deal;
  global $l_label,$l_comment,$l_number, $l_date, $l_amount_HT, $l_amount_TTC, 
    $l_deal, $l_status;
  global $set_rows, $set_rows_default;
  
  global $perms_user,$l_perms_user;
  global $auth, $sess, $perm, $param_invoice;

  global $border, $cellspacing;

  dis_invoice_detail ($action,$q_invoice, $q_status);
  
  // to add/remove deals
  $action_add = $sess->url("invoice_index.php?action=add_deal".
			   "&amp;param_invoice=".
			   $q_invoice->f("invoice_id"));
  $action_del = $sess->url("invoice_index.php?action=del_deal".
			   "&amp;param_invoice=".
			   $q_invoice->f("invoice_id"));
  // deals data 
  if ($q_deal->nf() == 0) {
    // no deals :
    display_ok_msg ("<BR>".$l_no_deal);
    // form to add some :
    echo "
<table cellspacing=\"0\" border=\"0\">
 <tr><td valign=\"top\">
  <form method=\"post\" action=\"\">
   <input type=\"button\" value=\"$l_add_deal\" ".
" onClick=\"this.form.action='".$action_add."' ; this.form.submit ();\">
 </td></tr>
</table>";
  } else {
    display_ok_msg("<br>".($action == ("del_deal")? $l_deals_todel : $l_list_deal));

    if ($action == "del_deal") {
      $url = $sess->url("invoice_index.php?action=del_deal&amp;param_invoice=".$q_invoice->f('invoice_id'));
    }else{
      $url = $sess->url("invoice_index.php?action=detailconsult&amp;param_invoice=".$q_invoice->f('invoice_id'));
    }
    
    $dis_deal = new OBM_DISPLAY("DATA", $options_deals);
    $dis_deal->data_set = $q_deal;
    $dis_deal->data_header = "top";
    $dis_deal->data_url = $url;
    $dis_deal->data_order = false;

    if (($action == "del_deal") && ($perm->have_perm("admin"))) {
      $url_submit=$sess->url("invoice_index.php?action=del_deal_chosen&amp;param_invoice=".$q_invoice->f('invoice_id'));
      display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
      echo "<form method=\"post\" name=\"del_deal\" action=\"".$sess->url($url_submit)."\">\n";
      $dis_deal->data_cb_text = $l_invoice_del_deal ;
      $dis_deal->data_idfield = "deal_id";
      $dis_deal->data_cb_side = "left";
      $dis_deal->data_cb_name = "del_";
    }
    
    $dis_deal->display();
    if ($perm->have_perm("admin")) {
      if ($action == "del_deal" ) {
	display_debug_msg("FIXME PERMISSIONS", $cdg_param);
	echo"<br>
         <input align=\"center\" type=\"submit\" value=\"$l_del_deal_chosen\">
        </form>
         ";
      } else {
	// we display the button to add some deals or remove some...
	echo "
<br>
<table cellspacing=\"$cellspacing\" border=\"$border\">
 <tr><td valign=\"top\">
  <form method=\"post\" action=\"\">
   <input type=\"button\" value=\"$l_add_deal\" ".
   " onClick=\"this.form.action='".$action_add."' ; this.form.submit ();\">
 </td><td valign=\"top\">
   <input type=\"button\" value=\"$l_del_deal\" ".
   " onClick=\"this.form.action='".$action_del."' ; this.form.submit ();\">
  </form>
 </td></tr>
</table>";
      }
    }
  }
  //  payments data :
  if ($q_payment->nf() == 0) {
    // no payment
    display_ok_msg ("<br>".$l_no_payment);
  } else {
    display_ok_msg ("<br>".$l_list_payment);

    $url = $sess->url("invoice_index.php?action=detailconsult".
		      "&amp;param_invoice=".$q_invoice->f("invoice_id"));

    $dis_payment = new OBM_DISPLAY("DATA",$options_payments);
    $dis_payment->data_set = $q_payment;
    $dis_payment->data_header = "top";
    $dis_payment->data_url = $url;
    $dis_payment->data_order = false;
    
    $dis_payment->display ();
  }
}

/////////////////////////////////////////////////////////////////////////////
// form to add deals to an invoice
/////////////////////////////////////////////////////////////////////////////
function dis_search_deal_form ($p_action, $q_invoice, $dis_option_deal, $q_deal=0,$p_deal_label="", $p_deal_company="", $p_deal_archive = 0) {
  global $l_search_deal, $l_invoice_deal_label, $l_invoice_infos, $l_find;
  global $l_invoice_deal_archive ;
  global $l_add_deal, $l_add_deal_chosen, $l_no_deal_found, $l_no_deal_display;
  global $cellspacing, $border;
  global $set_rows, $set_rows_default;
  global $action, $perm, $sess, $col_label;
  //$param_invoice;
  global $l_invoice_deal_company;

  // invoice data display
  dis_invoice_detail ($p_action, $q_invoice, run_query_invoicestatus());

  if ($p_deal_archive != 0) {
    $is_checked = " checked ";
  }

  $url = $sess->url ("invoice_index.php?action=search_deal".
		     "&amp;param_invoice=".$q_invoice->f("invoice_id"));
  // searching deals form 
  echo "<center><form method=\"post\" action=\"$url\">
        <table border=\"$border\" cellspacing=\"$cellspacing\">
         <tr><td>
          <font color=\"$col_label\"> $l_invoice_deal_label</font></br>
          <input type=\"text\" name=tf_deal_label value=\"$p_deal_label\">
         </td><td>
          <font color=\"$col_label\"> $l_invoice_deal_company</font></br>
          <input type=\"text\" name=tf_deal_company value=\"$p_deal_company\">
         </td><td>
          <font color=\"$col_label\"> $l_invoice_deal_archive </fnot></br>
          <input type=\"checkbox\" name=\"cb_deal_archive\" value=\"1\" $is_checked>
         </td><td>
          <input type=\"submit\" name=\"submit\" value=\"$l_find\">
         </td></tr>
        </table></form></center>";
  // search results, if performed
  if (is_object ($q_deal)) {
    if ($q_deal->nf()>0) {

      $url = $sess->url("invoice_index.php?action=search_deal".
		      "&amp;param_invoice=".$q_invoice->f("invoice_id").
		      "&amp;tf_deal_label=$p_deal_label".
		      "&amp;tf_deal_company=$p_deal_company");
    
    $dis_deal = new OBM_DISPLAY ("DATA", $dis_option_deal);
    $dis_deal->data_set = $q_deal;
    $dis_deal->data_header = "top";
    $dis_deal->data_url = $url;
    $dis_deal->data_order = false;

    if (($action == "search_deal")&&($perm->have_perm("admin"))) {
      display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
      $url = $sess->url("invoice_index.php?action=add_deal_chosen".
			"&amp;param_invoice=".$q_invoice->f("invoice_id").
			"&amp;tf_deal_label=$p_deal_label".
			"&amp;tf_deal_company=$p_deal_company");
    
      $url_form = $sess->url("invoice_index.php?action=add_deal_chosen".
	 "&amp;param_invoice=".$q_invoice->f("invoice_id"));
      echo "action = $action";
      echo "<form method=\"post\" name=\"add_deal\" action=\"".$url_form."\">";
    
      $dis_deal->data_cb_text = $l_add_deal;
      $dis_deal->data_idfield = "deal_id";
      $dis_deal->data_cb_side = "left";
      $dis_deal->data_cb_name = "add_";
    }

    $dis_deal->display ();
      
    if (($action=="search_deal")&&($perm->have_perm("admin"))) {
      display_debug_msg("FIXME PERMISSIONS", $cdg_param);
      echo "<br>
          <input type=\"submit\" value=\"$l_add_deal_chosen\">
        </form>" ; 
    }

    } else{// no matches found :
      echo "<hr>";
      display_ok_msg($l_no_deal_found);
    }      
  } else  { // search not done :
    echo "<hr>";
    display_ok_msg($l_no_deal_display);
  }
}

/////////////////////////////////////////////////////////////////////////////
// displays an invoice details :
/////////////////////////////////////////////////////////////////////////////
function dis_invoice_detail ($p_action, $q_invoice, $q_status){
  global $auth, $sess, $param_invoice;
  global $border, $cellspacing;
  global $l_update, $l_comment,$l_amount_HT, $l_amount_TTC,
    $l_status, $l_date, $l_label, $col_label,$l_number, $l_inout, 
    $l_fournisseur, $l_client, $l_duplicate;

  echo "<center>
   <table border=$border cellspacing=$cellspacing>
   <tr><td>";
  //----------------------------------------------------------------------
  echo "<table border=$border cellspacing=$cellspacing width=\"100%\">
   <tr>";
  // invoice number
  echo"
   <td width=\"30%\"><font color=\"#$col_label\">$l_number</font><br>".
   $q_invoice->f("invoice_number")."</td>"; 
  // invoice label
  echo "
   <td width=\"60%\"><font color=\"#$col_label\">$l_label</font><br>".
   $q_invoice->f("invoice_label")."</td>"; 
  // date :
  echo "
   <td><font color=\"#$col_label\">$l_date</font><br>".
    $q_invoice->f("invoice_date")."</td>";
  echo "</tr>
   </table></td></tr>"; 

  //----------------------------------------------------------------------
  // invoice status
  echo "<tr><td>
   <table border=$border cellspacing=$cellspacing width=\"100%\">
   <tr>";
  echo "<td width=\"40%\">
   <font color=\"#$col_label\">$l_status</font><br>";
  while ($q_status->next_record()) {
    if ($q_status->f("invoicestatus_id")==$q_invoice->f("invoice_invoicestatus_id")){
      echo $q_status->f("invoicestatus_label");
      break; // as soon as we catch the one we leave
    }
  }
  echo "</td>";
  // inout :
  echo "<td>\n";
  echo "<font color=\"#$col_label\">$l_inout</font><br>\n";
  echo ($q_invoice->f("invoice_inout")=='-')?$l_fournisseur:$l_client;
  echo "</td></tr>";
  // amount with and without charges 
  echo "<tr>
   <td><font color=\"#$col_label\">$l_amount_HT</font><br>".
    $q_invoice->f("invoice_amount_HT")."</td>";
  echo "
    <td><font color=\"#$col_label\">$l_amount_TTC</font><br>".
    $q_invoice->f("invoice_amount_TTC")."</td><br>";
  echo "</tr></table>";
  //----------------------------------------------------------------------
  // comment :
  echo "</td></tr><tr>
   <td><font color=\"#$col_label\">$l_comment</font><br>".
   nl2br($q_invoice->f("invoice_comment"))."</td>"; 
  echo "</tr>";
  // buttons to update or duplicate an invoice,
  if ($p_action == "detailconsult" || $p_action == "add_deal"
      || $p_action == "del_deal") {
    // actions of the form :
    $action_up = $sess->url ("invoice_index.php?action=detailupdate".
			     "&amp;param_invoice=".
			     $q_invoice->f("invoice_id"));
    $action_dup = $sess->url("invoice_index.php?action=duplicate".
			     "&amp;param_invoice=". 
			     $q_invoice->f("invoice_id"));

    echo " 
      <tr><td align=\"center\" valign=\"middle\">
       <table border=\"$border\" cellspacing=\"$cellspacing\">
        <tr><td valign=\"top\">
         <form method=\"post\" action=\"\">"; 
    echo "
          <input type=\"button\" value=\"$l_duplicate\" ".
      "onClick=\"this.form.action='".$action_dup."'; this.form.submit ();\">";
    echo "
        </td><td align=\"top\">
          <input type=\"button\" value=\"$l_update\" ".
      "onClick=\"this.form.action='".$action_up."'; this.form.submit ();\">
        </td></tr>";   
    echo "  
         </form>
       </table>
      </td></tr>"; 
  } 
  echo "</table><br>"; 
} 

//////////////////////////////////////////////////////////////////////////
// error screen : adding payments to an invoice not connected to a deal
//////////////////////////////////////////////////////////////////////////
function html_error_no_deals_related ($q_invoice) {
  global $l_no_deals_related_error;
  
  display_err_msg  ($l_no_deals_related_error);
} // html_error_no_deals_related ()

//////////////////////////////////////////////////////////////////////////
// error screen : deleting the last deal from an invoice 
// while there are still payments...
//////////////////////////////////////////////////////////////////////////
function html_error_last_deal_remove ($q_invoice) {
  global $l_last_deal_remove_error;
  
  display_err_msg  ($l_last_deal_remove_error);
} // html_error_last_deal_remove

</SCRIPT>
