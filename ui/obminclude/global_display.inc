<?
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_display.inc                                           //
//     - Desc : Global Display File (OBM_DISPLAY)                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

class OBM_DISPLAY {
  var $display_type;      // determines the type of display. possible values
    // - PREFERENCES  : to set the display preference
    // - DATA         : to display all elements, for exemple all contacts
  var $display_module;   // module where to find the entity in use
  var $display_entity;   // entity used (deal, contact, parentdeal,...)
  var $display_pref;     // User preferences : DB_OBM : query from PrefDisplay
  var $display_link = 1; // If false, extern links aren't displayed

  //----------- TYPE SECTION : PREFERENCES --------//
  var $pref_title;       // title of the preference panel
  var $pref_dis_help;    // flag to tell if the pref help screen shoulg be dis.

  //----------- TYPE SECTION : DATA -----------//
  var $data_set;              // Data set :DB_OBM: data rows to be displayed
  var $data_header = "top";   // Table header location (top|both) (+bottom)
  var $data_page = "true";    // Page management (true | false)
  var $data_url = "";         // Base URL (with search) used for links in Index
  var $data_order = true;     // Order can be changed (click on column title)
  var $data_cb_text = "";     // Checkbox column text
  var $data_cb_side = "left"; // Checkbox position in the row (left|right)
  var $data_cb_name = "";     // Base name of the checkbox element (+id)
  var $data_cb_field = "";    // Field from which cb is filled (1=selected)
  var $data_idfield = "_id";  // Index field for the data row
  var $data_cb_show = 1;      // To set visible
                              //   1 -> visible
                              //   "string" if ->f["string"] == 0 -> visible


  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY Constructor
  // Parameters:
  //   - $type : display type
  //   - $pref : display pref : DBO
  /////////////////////////////////////////////////////////////////////////////
  function OBM_DISPLAY($type, $pref) {
    if( ($type != "PREFERENCES") && 
	($type != "DATA") ) {
      die("ERROR: the type MUST be PREFERENCES or DATA");
    } else {
      $this->display_type = $type;
    }
    if(isset($pref)) {
      $this->display_pref = $pref;
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // OBM_DISPLAY main display function
  /////////////////////////////////////////////////////////////////////////////
  function display() {
    if($this->display_type == "PREFERENCES") {
      $this->check_preferences_vars();
      $this->dis_preferences();
    }else if($this->display_type == "DATA") {
      $this->check_data_vars();
      $this->dis_data();
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that check if DATA variables are set, if not die               //
  /////////////////////////////////////////////////////////////////////////////
  function check_data_vars() {
    if(!is_object($this->display_pref)) {
      die("THE display_pref VARIABLE MUST BE SET");
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that check if PREFERENCES variables are set, if not die        //
  /////////////////////////////////////////////////////////////////////////////
  function check_preferences_vars() {
    if(!is_object($this->display_pref)) {
      die("THE display_pref VARIABLE MUST BE SET");
    }
    if(!isset($this->display_entity)) {
      die("THE display_entity VARIABLE MUST BE SET");
    }
    if(!isset($this->pref_title)) {
      die("THE pref_title VARIABLE MUST BE SET");
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the preferences screen for the dataset            //
  /////////////////////////////////////////////////////////////////////////////
  function dis_preferences() {
    global $col_tableh,$col_textem,$col_table;
    global $l_displayed, $l_order, $l_field, $l_move, $l_mandatory;
    global $l_display_no, $l_display_yes, $l_down, $l_up;
    // Images
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_round, $ico_forbidden;
    global $sess, $fieldnames;
    
    $nb_field = $this->display_pref->nf() - 1;

    // actions names
    $action_display = "dispref_display";
    $action_level = "dispref_level";

    if ($this->display_module != "") {
      $url = $this->display_module . "_index.php";
    } else {
      $url = $this->display_entity . "_index.php";
    }

    echo "
    <table bgcolor=\"#$col_tableh\" cellspacing=\"2\" border=\"0\" >
       <tr>
         <td align=\"center\" colspan=\"4\">
           <font color=\"#$col_textem\"><b>".$this->pref_title."</b></td>
       </tr>
       <tr>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">$l_displayed</font></td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">$l_order</font></td>
        <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">$l_field</font></td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">$l_move</font></td>
       </tr>";

    $i = 0;
    $nb_field_hidden = 0;
    while($this->display_pref->next_record()) {
      $entity = $this->display_entity;
      $fieldname = $this->display_pref->f("display_fieldname");
      $display = $this->display_pref->f("display_display");
      echo "
          <tr>
            <td bgcolor=\"#$col_table\" align=\"center\">";
      if ($display == 1) {
	echo "<a href=\"".$sess->url("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;display=$display")."\">
        <img alt=\"$l_display_no\" border=0 src=\"/images/$set_theme/$ico_round\"></a>";
      } else if ($display == 0) {
	$nb_field_hidden++;
	echo "<a href=\"".$sess->url("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;display=$display")."\">
          <img alt=\"$l_display_yes\" border=0 src=\"/images/$set_theme/$ico_crow\"></a>";
      } else if($display == 2) {
	echo "<img alt=\"$l_mandatory\" border=0 src=\"/images/$set_theme/$ico_forbidden\"></a>";
      }
      echo "
            </td>
            <td bgcolor=\"#$col_table\" align=\"center\">
               <b>";
      if($display == 0) {
	echo "<b>&nbsp;</b>";
      } else {
	$pos = $this->display_pref->f("display_fieldorder") - $nb_field_hidden;
	echo "<b>$pos</b>";
      }
      echo "
             </td>
            <td bgcolor=\"#$col_table\" align=\"center\">
                   ".$fieldnames[$fieldname]."
            </td>
            <td bgcolor=\"#$col_table\" align=\"center\">
              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">
                <tr>";

      // If last field we doesn't display the down arrow
      if ($i == $nb_field) {
	echo "<td align=\"left\">&nbsp;</td>";
      } else {
	echo "<td align=\"left\"><a href=\"".$sess->url("$url?action=$action_level&amp;entity=$entity&amp;new_level=down&amp;fieldorder=".$this->display_pref->f("display_fieldorder")."&amp;which=".$this->pref_which)."\">
           <img border=0 alt=\"$l_down\" src=\"/images/$set_theme/$ico_arrow_down\"></a></td>";
      }

      // If first field we doesn't display the up arrow
      if ($i == 0) {
	echo "<td align=\"right\">&nbsp;</td>";
      } else {
	echo "<td align=\"right\">
              <a href=\"".$sess->url("$url?action=$action_level&amp;entity=$entity&amp;new_level=up&amp;fieldorder=".$this->display_pref->f("display_fieldorder")."&amp;which=".$this->pref_which)."\">
            <img alt=\"$l_up\" border=0 src=\"/images/$set_theme/$ico_arrow_up\"></a></td>";
      }
      echo "
                </tr>
              </table>   
            </td>
          </tr>";
      $i++;
    }
    echo "
            </td>
          </tr>";
    echo "</table>";

    // If help is set, we display the help panel
    if($this->pref_dis_help == 1) {
      $this->dis_pref_help();
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the preference help screen
  /////////////////////////////////////////////////////////////////////////////
  function dis_pref_help() {
    global $col_tableh,$col_textem,$col_table;
    global $l_help, $l_help_display_yes, $l_help_display_no, $l_help_mandatory, $l_help_down, $l_help_up;
    // Images
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_round, $ico_forbidden;

    echo "<br><hr>
      <table bgcolor=\"#$col_tableh\" cellspacing=\"2\" border=\"0\" >
      <tr>
        <td align=center colspan=2><font color=\"#$col_textem\">
        <b>$l_help</b></td>
      </tr>
      <tr>
        <td align=\"center\" valign=\"middle\" bgcolor=\"#$col_table\">
          <img border=0 src=\"/images/$set_theme/$ico_round\"></a>
        </td>
        <td valign=\"middle\" bgcolor=\"#$col_table\">
          $l_help_display_yes
        </td>
      </tr>
      <tr>
        <td align=\"center\" valign=\"middle\" bgcolor=\"#$col_table\">
          <img border=0 src=\"/images/$set_theme/$ico_crow\">
        </td>
        <td valign=\"middle\" bgcolor=\"#$col_table\">
          $l_help_display_no
        </td>
      </tr>
      <tr>
        <td align=\"center\" valign=\"middle\" bgcolor=\"#$col_table\">
         <img border=0 src=\"/images/$set_theme/$ico_forbidden\">
        </td>
        <td valign=\"middle\" bgcolor=\"#$col_table\">
          $l_help_mandatory
        </td>
      </tr>
      <tr>
        <td align=\"center\" valign=\"middle\" bgcolor=\"#$col_table\">
         <img border=0 src=\"/images/$set_theme/$ico_arrow_down\">
        </td>
        <td valign=\"middle\" bgcolor=\"#$col_table\">
          $l_help_down
        </td>
      </tr>
      <tr>
        <td align=\"center\" valign=\"middle\" bgcolor=\"#$col_table\">
          <img border=0 src=\"/images/$set_theme/$ico_arrow_up\">
        </td>
        <td valign=\"middle\" bgcolor=\"#$col_table\">
          $l_help_up
        </td>
      </tr>
    </table>";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset
  /////////////////////////////////////////////////////////////////////////////
  function dis_data() {
    global $col_textem, $col_table, $col_tableh,$col_error,$col_text_hot;
    global $col_frs, $col_client,$col_balance_pos, $col_balance_neg,$col_text_redhot;
    global $l_minimphone, $l_minihphone,$col_inc_redhot,$col_inc_hot,$col_inc_closed;
    global $set_theme,$ico_mail,$ico_contact_list,$ico_contact_new,$ico_web,$ico_round, $ico_crow, $set_rows;
    global $auth, $sess, $cdg_id, $set_debug;
    global $time;

    $link_ok = $this->display_link;

    // We display page management only if enabled and a search url is given
    if (($this->data_page) && ($this->data_url != "")) {
      $this->dis_data_index($this->data_url);
    }

    echo "<table bgcolor=\"#$col_tableh\" border=\"0\" width=100%>";
    $this->dis_data_header($this->data_url);

    $this->display_pref->seek(0);
    $cpt = 0;

    // Iteration on each data row of dataset
    while ( ($this->data_set->next_record() )
            && ( ($cpt < $set_rows) || ( ! $this->data_page) ) ) {
      $cpt++;
      echo "<tr>";

      // If debug level Id is set, display the entity id
      if (($set_debug > 0) && (($set_debug & $cdg_id) == $cdg_id)) {
	$dg_id = $this->data_set->f("Id");
	echo "<td>$dg_id</td>";
      }

      // If a left checkbox has to be displayed
      if (($this->data_cb_side == "left") && ($this->data_cb_text != "")){
	$col_show = $this->data_cb_show;
        // If cb are to be displayed or column telling visibility is true
        if (($col_show == "1") || ($this->data_set->f($col_show))) {
	  $name = $this->data_cb_name;
	  $name .= $this->data_set->f($this->data_idfield);
	  if ($this->data_set->f($this->data_cb_field)) {
	    $cb_check = "checked";
	  } else {
	    $cb_check = "";
	  }
	  echo "<td align=\"center\" bgcolor=\"#$col_table\">
                <input type=checkbox name=\"$name\" $cb_check></td>";
	}
	else
	  echo "<td align=\"center\" bgcolor=\"#$col_table\">
                &nbsp;</td>";
      }

      //Incident Rows special Color
      if ($this->data_set->f("incident_state") == "CLOSED")  {
       $special_col_table=$col_inc_closed;
      }elseif ($this->data_set->f("incident_priority") == "REDHOT")  {
	$special_col_table=$col_inc_redhot;
      }elseif ($this->data_set->f("incident_priority") == "HOT") {
	$special_col_table=$col_inc_hot;
      }

      // For each field in the data row we walk through the pref dbo ordered
      // and for each entry (field) we set special parameters and display it
      while ($this->display_pref->next_record()) {
	$name = "";
	$url = "";
	$target = "";
	$ico = "";
	$align="left";
	$fieldname = $this->display_pref->f("display_fieldname");

        //=====================================================================
        // Special parameters for some fields
        // $url, $name (eg: to change color), $align,...
        //=====================================================================

	//------------------------------
	// For lists 
	//------------------------------
	// For contacts urls (link):
	if ($fieldname == "list_contact_lastname") {
	  $url = "../contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("list_contact_id");
	}
	// For Company urls (link):
	else if ($fieldname == "list_contact_company") {
	  $url = "../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("list_contact_company_id");
        }
	// To put udp||tcp in the middle: ????
	else if ($fieldname == "service_proto") {
	  $align = "middle";
	}
	
	//------------------------------
	// For the companies 
	//------------------------------
	else if ($fieldname == "company_name") {
	   $url = "../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("company_id");
	}
	else if ($fieldname == "company_email") {
	  if (strcmp($this->data_set->f("company_email"),"") != 0) {
	    $url = "mailto:".$this->data_set->f("company_email");
	    $name = "<IMG BORDER=0 SRC=\"/images/".$set_theme."/".$ico_mail."\" ALT=\"".$this->data_set->f("company_email")."\">";
          }
        } 
	else if ($fieldname == "company_web") {
	  if (strcmp($this->data_set->f("company_web"),"") != 0) {
	    if (strcmp(substr($this->data_set->f("company_web"),0,5), "http:") != 0) {
	      $url = "http://";
	    }
	    $url .= $this->data_set->f("company_web");
	    $name = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_web\" ALT=\"".$this->data_set->f("company_web")."\">";
	  }
        }
        else if ($fieldname == "company_contact_number") {
	  $name = $this->data_set->f("company_contact_number");
	  if ($name > 0) {
	    $url = "../contact/contact_index.php?action=search&amp;param_company=".$this->data_set->f("company_id")."&amp;tf_company=".urlencode("".$this->data_set->f("company_name"));
	  }
          $align = "center";
        }
        else if ($fieldname == "company_new_contact") {
	  $url = "../contact/contact_index.php?action=new&amp;param_company=".$this->data_set->f("company_id");
          $name = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_contact_new\" ALT=C>";
        }
        else if ($fieldname == "company_deal_number") {
	  $name = $this->data_set->f("company_deal_number");
	  $c_id = $this->data_set->f("company_id");
	  if ($name > 0) {
	    $url = "../deal/deal_index.php?action=search&amp;param_company=$c_id";
	  }
          $align = "center";
        }
       
        else if ($fieldname == "company_address1") {
	  $name="<FONT SIZE=-2>&nbsp;";
	  if ($this->data_set->f("company_address1") != "") {
	    $name .= $this->data_set->f("company_address1")."<BR>\n";
	  }
	  if ($this->data_set->f("company_address2") != "") {
	    $name .= $this->data_set->f("company_address2")."<BR>\n";
	  }
	  if ($this->data_set->f("company_zipcode") != "" ) {
	    $name .= $this->data_set->f("company_zipcode") . "&nbsp;&nbsp;";
	  } else {
	    $name.= "&nbsp;&nbsp;";
	  }
	  if ($this->data_set->f("company_town") != "" ) {
	    $name .= $this->data_set->f("company_town");
	  }
	  $name .="</FONT>";
	}

	//------------------------------
	// For the contacts 
	//------------------------------
        else if (($fieldname == "contact_lastname") && $link_ok) {
	  $url = "../contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("contact_id");
	}
       	else if (($fieldname == "contact_company_name") && $link_ok){
	  $url = "../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("contact_company_id");
	}
	else if ($fieldname == "contact_function") {
	  $name = $this->data_set->f("contact_function");
	}
	else if ($fieldname == "contact_phone") {
          $phone = $this->data_set->f("contact_phone");
          $c_phone = $this->data_set->f("company_phone");
          if ( (trim($phone) != "") && ($phone == $c_phone)) {
            $phone = "($phone)";
          }
	  $name = $phone;
	}
	else if ($fieldname == "contact_email") {
	  if (strcmp($this->data_set->f("contact_email"),"") != 0) {
	    $url = "mailto:".$this->data_set->f("contact_email");
	    $name = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_mail\" ALT=\"".$this->data_set->f("contact_email")."\">";
	  }
        }


	//------------------------------
	// For the parentdeals and deals
	//------------------------------
 	else if ($fieldname == "parentdeal_label") {
	  $url = "../deal/deal_index.php?action=parent_detailconsult&amp;param_parent=".$this->data_set->f("parentdeal_id");
	}
        else if ($fieldname == "parentdeal_marketing_lastname") {
	  $url = "../contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("parentdeal_marketingmanager_id");
	}
        else if ($fieldname == "parentdeal_technical_lastname") {
	  $url = "../contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("parentdeal_technicalmanager_id");
	} 
	else if ($fieldname == "deal_label") {
	  $name="";
	  $url = "../deal/deal_index.php?action=detailconsult&amp;param_deal=".$this->data_set->f("deal_id");
	}
	else if ($fieldname == "deal_archive") {
          $align = "center";
          if ($this->data_set->f($fieldname)) $name ="X";
          else $name="&nbsp;";
	}
	else if ($fieldname == "deal_company_name") {
	  $name="";
	  $url = "../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("deal_company_id");
        } 
	else if ($fieldname == "deal_datealarm") {
	  $date = $this->data_set->f($fieldname);
	  $datealarm = $this->data_set->f("datealarm");
	  $year = substr($date,0,4);
	  $month = substr($date,5,2);
	  $day = substr($date,8,2);
	  $timestamp = mktime (23,59,59,$month,$day,$year);
	  $time = time();
	  if ($time > $timestamp) {
            $name="<font color=\"#$col_error\">$datealarm</font>";
	  } else if (($time + 86400) > $timestamp) {
	    $name="<font color=\"#$col_ok\">$datealarm</font>";
	  } else {
	    $name="$datealarm";
	  }
	}

	//------------------------------
	// For the List
	//------------------------------
 	else if (($fieldname == "list_name")  && $link_ok) {
	  $url = "list_index.php?action=detailconsult&amp;param_list=".$this->data_set->f("list_id");
	}
        else if ($fieldname == "list_nb_contact") {
          $nb_q = new DB_OBM;
          $id = $this->data_set->f("list_id");
          $query = "select count(*) from ContactList where ContactList_listid='$id'";
          $nb_q->query($query);
	  $nb_q->next_record();
	  $name = $nb_q->f(0);
          $align = "center";
	}
	else if ($fieldname == "deal_company_name") {
	  $name="";
	  $url = "../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("deal_company_id");
        } 
	else if ($fieldname == "deal_datealarm") {
	  $date = $this->data_set->f($fieldname);
	  $year = substr($date,0,4);
	  $month = substr($date,5,2);
	  $day = substr($date,8,2);
	  $timestamp = mktime (23,59,59,$month,$day,$year);
	  $time = time();
	  if ($time > $timestamp) {
            $name="<FONT COLOR=\"#$col_error\">".
	      $this->data_set->f($fieldname) . "</FONT>";
	  } else if (($time + 86400) > $timestamp) {
	    $name="<FONT COLOR=\"#$col_ok\">".
	      $this->data_set->f($fieldname) . "</FONT>";
	  } else {
	    $name="";
	  }
	}

        //-------------------------------------
	// invoice from the deal display screen
        //-------------------------------------
	elseif ($fieldname == "deal_invoice_label") {
	  $url = "../treso/invoice_index.php?action=detailconsult&amp;param_invoice=".$this->data_set->f("invoice_id");
	} elseif (($fieldname == "deal_invoice_HT") 
		   || ($fieldname == "deal_invoice_TTC") ) {
	  $amount = $this->data_set->f($fieldname);
	  if ($this->data_set->f("invoice_inout") == '+') {
	    $couleur = $col_client;
	  }
	  else {
	    $couleur = $col_frs;
	  }
	  $name = "<FONT COLOR=\"#$couleur\">$amount</FONT>";
	}

	//------------------------------
	// For the users 
	//------------------------------
	else if ($fieldname == "userobm_login") {
	   $url = "../user/user_index.php?action=detailconsult&amp;param_user=".$this->data_set->f("userobm_id");
	}
	else if ($fieldname == "userobm_email") {
          $email = $this->data_set->f("userobm_email");
	  if (strcmp($email ,"") != 0) {
	    $url = "mailto:$email";
	    $name = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_mail\" ALT=\"$email\">$email";
          }
        } 
	else if ($fieldname == "userobm_archive") {
          $align = "center";
          if ($this->data_set->f($fieldname)) $name ="X";
          else $name="&nbsp;";
	}

	//------------------------
	// For the accounts :
        //------------------------
	elseif ($fieldname == "account_label") {
	  $url="./account_index.php?action=detailconsult&amp;param_account=".$this->data_set->f("account_id");
	}
	elseif ( ($fieldname == "account_today") 
		  || ($fieldname == "account_balance") ) {
	  // balance column color :
	  $balance = $this->data_set->f($fieldname);
	  if ($balance > 0) {
	    $couleur = $col_balance_pos;
	  } else {
	    $couleur = $col_balance_neg;
	  }
	  $name = "<font color=\"#$couleur\">$balance</font>";
	}
	elseif ($fieldname == "account_pay_label") {
	  $url="./payment_index.php?action=detailconsult&amp;param_pay_label=".$this->data_set->f("account_pay_id");
	}
	elseif ($fieldname == "account_pay_amount") {
	  // amount column color
	  $amount = $this->data_set->f($fieldname);
	  if ($this->data_set->f("payment_inout") == '+') {
	    $couleur = $col_balance_pos;
	  } else {
	    $couleur = $col_balance_neg;
	  }
	  $name = "<font color=\"#$couleur\">$amount</font>";
	}

	//------------------------
	// For the invoices :
	//------------------------
	elseif ($fieldname == "invoice_deal") {
	  $url="../deal/deal_index.php?action=detailconsult&amp;param_deal=".$this->data_set->f("deal_id");
	} elseif ($fieldname == "invoice_label") {
	  $url="../treso/invoice_index.php?action=detailconsult&amp;param_invoice=".$this->data_set->f("invoice_id");
	} elseif ($fieldname == "invoice_company") {
	  $url="../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("company_id");
	} elseif ($fieldname == "invoice_pay_label") {
	  $url = "../treso/payment_index.php?action=detailconsult&amp;param_payment=".$this->data_set->f("invoice_payment_id");
	} // amount column color 
	elseif (($fieldname == "invoice_amount_HT") 
		|| ($fieldname == "invoice_amount_TTC")
		|| ($fieldname == "invoice_paid")) {

	  $amount = $this->data_set->f($fieldname);
	  if ($this->data_set->f("invoice_inout") == '+') {
	    $couleur = $col_client;
	  } else {
	    $couleur = $col_frs;
	  }
	  $name = "<FONT COLOR=\"#$couleur\">$amount</FONT>";
	}
        else if ($fieldname == "invoice_date") {
	  $name = $this->data_set->f("date");
	}

        //------------------------
	// For the payments :
        //------------------------
	elseif ($fieldname == "payment_label") {
	  $url = "./payment_index.php?action=detailconsult&amp;param_payment=".$this->data_set->f("payment_id");
	} elseif ($fieldname == "payment_account_lbl") {
	  $url = "./account_index.php?action=detailconsult&amp;param_account=".$this->data_set->f("account_id");
	} elseif ($fieldname == "pay_inv_label") {
	  $url = "./invoice_index.php?action=detailconsult&amp;param_invoice=".$this->data_set->f("invoice_id");
	} elseif ($fieldname == "pay_inv_company") {
	  $url = "../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("company_id");
	}// amount column color 
	elseif (($fieldname == "payment_amount")
		 || ($fieldname == "pay_inv_paid")) {
	  $amount = $this->data_set->f($fieldname);
	  if ( ($this->data_set->f("payment_inout") == '+') // for a payment
	       // for an invoice
	       || ($this->data_set->f("invoice_inout") == '+') ) {
	    $couleur = $col_client;
	  } else {
	    $couleur = $col_frs;
	  }
	  $name = "<FONT COLOR=\"#$couleur\">$amount</FONT>";
	}
        else if ($fieldname == "payment_date") {
	  $name = $this->data_set->f("date");
	}
        else if ($fieldname == "payment_expect_date") {
	  $name = $this->data_set->f("expect_date");
	}



	//For Image scan (crow or round):
	if($fieldname == "service_status") {
	  $name = ($this->data_set->f($fieldname) == 1) ? "<img border=0 src=\"/images/$set_theme/$ico_round\">" : "<img border=0 src=\"/images/$set_theme/$ico_crow\">";
	  $align = "center";
	}

	//------------------------------
	// For the contracts 
	//------------------------------
        else if ($fieldname == "contract_label") {
	  $url = "../contract/contract_index.php?action=detailconsult_contract&amp;param_contract=".$this->data_set->f("contract_id");
	}
       	else if ($fieldname == "contract_company_name") {
	  $url = "../company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("contract_company_id");
       	}
        else if ($fieldname == "contract_dateexp") {
	  $name = $this->data_set->f("dateexp");
	}
	//------------------------------
	// For the incidentss 
	//------------------------------
        else if ($fieldname == "incident_label") {
	  if($special_col_table==$col_inc_redhot) {
	   $name = "<FONT COLOR=\"#$col_text_redhot\">".$this->data_set->f("incident_label")."</FONT>";
          }elseif ($special_col_table==$col_inc_hot) {
           $name = "<FONT COLOR=\"#$col_text_hot\">".$this->data_set->f("incident_label")."</FONT>";
          }
	  $url = "../contract/incident_index.php?action=detailconsult&amp;param_incident=".$this->data_set->f("incident_id");
	}
       	else if ($fieldname == "incident_owner_lastname") {
	  if($special_col_table==$col_inc_redhot) {
	   $name = "<FONT COLOR=\"#$col_text_redhot\">".$this->data_set->f("incident_owner_lastname")."</FONT>";
          }elseif ($special_col_table==$col_inc_hot) {
           $name = "<FONT COLOR=\"#$col_text_hot\">".$this->data_set->f("incident_owner_lastname")."</FONT>";
          }
	  $url = "../contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("incident_owner");
	}
       	else if ($fieldname == "incident_logger_lastname") {
	  if($special_col_table==$col_inc_redhot) {
	   $name = "<FONT COLOR=\"#$col_text_redhot\">".$this->data_set->f("incident_logger_lastname")."</FONT>";
          }elseif ($special_col_table==$col_inc_hot) {
           $name = "<FONT COLOR=\"#$col_text_hot\">".$this->data_set->f("incident_logger_lastname")."</FONT>";
          }
	  $url = "../contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("incident_logger");
	}
        else if ($fieldname == "incident_date") {
	  $name = $this->data_set->f("date");
	}
	
        //------------------------------
        // For the time management
        //------------------------------
        else if ($fieldname == "task_id" && $this->data_set->f("task_status")==0) { //make the popup timemanagement modification variable
	   $popup_display = "time_index.php?action=modify&amp;popup=1&amp;task_id=".$this->data_set->f("task_id")."&amp;param_begin=".$time["date"]."&amp;param_end=".$time["date_end"];
	   $name = "<img border=0 src=\"/images/$set_theme/modify.png\">";
           $url="./time_index.php?action=index&amp;param_begin=".$time["date"]."&amp;param_end=".$time["date_end"];
	}
	else if ($fieldname == "task_id")
           $name = " ";
 
        //===================
        // Final cell writing
        //===================
        if ($special_col_table != "") {
	 $td = "<td align=\"$align\" bgcolor=\"#$special_col_table\">";
        }
	else {
	 $td = "<td align=\"$align\" bgcolor=\"#$col_table\">";
	}
        $content = "<font size=-1>";
	
	if($popup_display!="") { //make the popup link for timemanagement modification
	  $content ="&nbsp;<a href=\"\" OnClick=\"window.name='root';window.open('$popup_display','',config='height=270,width=970,scrollbar=no,toolbar=no,menubar=no,resizable=no'); return false;\">";
	  if ($name == "") {
	     $content .= $this->data_set->f($fieldname);
	  } else {
	     $content .= $name;
	 }
	 $content .= "</a>";
	}
	else {
	if ($url != "") {
	  $content .= "&nbsp;<a href=\"".$sess->url("$url")."\">";
	  if ($name == "") { 
	    $content .= $this->data_set->f($fieldname);
	  } else {
	    $content .= $name;
	  }
	  $content .= "</a>";
	} else {
	  if ($name != "") {
	    $content .= $name."&nbsp;";
	  } else {
	    $content .= $this->data_set->f($fieldname) . "&nbsp;";
	  }
	}
      }
	echo "$td $content </font></td>\n";
      }
      

      //-----------------------------------------------------
      // Row end : if action and right, we display a checkbox
      //-----------------------------------------------------
      if (($this->data_cb_side == "right")
            && ($this->data_cb_text != "")) {
	echo "<td align=\"center\" bgcolor=\"#$col_table\">
                <input type=checkbox name=\"".$this->data_cb_name.$this->data_set->f($this->data_idfield)."\"></td>";
      }
      $this->display_pref->seek(0);
      echo "</tr>";
      $special_col_table="";
      $popup_display="";
    }

    //--------------------------------------------------------
    // Data Set end : if header set to both, display an header
    //--------------------------------------------------------
    if($this->data_header == "both") {
      $this->dis_data_header($this->data_url);
    }
    echo "</table>";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset index (page management)
  // Parameters:
  //   - $url : URL with search fields to link to index pages
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_index($url) {
    global $sess, $page, $new_order;
    global $set_rows, $set_rows_default;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    // Page display management
    if (!isset($page)) {
      $page=0;
    }
    if ($set_rows < 1) $set_rows = $set_rows_default;
    $nb_list = $this->data_set->num_rows();
    // If we delete all contacts of the last page, we must go to $page-1
    if($page != 0) {
      if($nb_list < ($page*$set_rows)+1) {
         $page-- ;
      }
    }

    $nb_page = floor(($nb_list-1) / $set_rows);
    $first_row = $page * $set_rows;

    // skip if display possible on a single page if nb_page==0
    if ($nb_page > 0) {
      // Table of content (pages)
      $i=0;
      while ($i <= ($nb_page)) {
        if ($i != 0) echo " | ";
        if ($i != $page) {
          echo "<a href=\"$url&amp;page=$i&amp;new_order=$new_order\">\n";
	}
	echo ($i + 1);
	if ($i != $page) echo "</a>\n";
	$i++;
      }
      echo "<br><br>";
      $this->data_set->seek($first_row);
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset headers (col names)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header($url) {
    global $sess, $page, $new_order, $order_dir;
    global $col_tableh, $col_textem;
    global $fieldnames, $cdg_id, $set_debug;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    $this->display_pref->seek(0);
    echo "<tr>";
    
    // If debug level Id is set, display the entity id
    if (($set_debug > 0) && (($set_debug & $cdg_id) == $cdg_id)) {
      echo "<td>Id</td>";
    }

    // Display the checkbox title if needed (exists and left side)
    if (($this->data_cb_side == "left")
          && ($this->data_cb_text != "")) {
      echo "<td align=\"center\" bgcolor=\"#$col_tableh\">
         <font color=\"#$col_textem\">".$this->data_cb_text."</font></td>";
    }

    while($this->display_pref->next_record()) {
      $fieldname = $this->display_pref->f("display_fieldname");
      $label = $fieldnames[$fieldname];
      $order = ($order_dir == "ASC" ? "DESC" : "ASC");

      echo "<td align=\"center\" bgcolor=\"#$col_tableh\">
                <font color=\"#$col_textem\">";

      if ($this->data_order) {
        echo "<a href=\"".$url.
          (($url != "") ? "&amp;page=$page" : "" ) .
          "&amp;new_order=$fieldname&amp;order_dir=$order\">";
      }
      echo $label;
      if ($this->data_order) {
        echo "</a>";
      }

      echo " </font>
           </td>";
    }

    // Display the checkbox title if needed (exists and right side)
    if (($this->data_cb_side == "right")
         && ($this->data_cb_text != "")) {
      echo "<td align=\"center\" bgcolor=\"#$col_tableh\">
         <font color=\"#$col_textem\">".$this->data_cb_text."</font></td>";
    }
    echo "</tr>";
  }

} // End of the object OBM_DISPLAY


?>
