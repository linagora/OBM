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
  var $separator = ";";        // for file generation : .csv by default

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
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_ball, $ico_forbidden;
    global $fieldnames;
    
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
    <table class=\"OBMDisplayPref\" >
       <tr>
         <td class=\"OBMDisplayHead\" colspan=\"4\">
          ".$this->pref_title."</td>
       </tr>
       <tr>
         <td class=\"OBMDisplayLabel\">
            $l_displayed</td>
         <td class=\"OBMDisplayLabel\">
            $l_order</td>
        <td class=\"OBMDisplayLabel\">
            $l_field</td>
         <td class=\"OBMDisplayLabel\">
            $l_move</td>
       </tr>";

    $i = 0;
    $nb_field_hidden = 0;
    while($this->display_pref->next_record()) {
      $entity = $this->display_entity;
      $fieldname = $this->display_pref->f("display_fieldname");
      $display = $this->display_pref->f("display_display");
      echo "
          <tr>
            <td class=\"OBMDisplayTextCentered\">";
      if ($display == 1) {
	echo "<a class=\"OBMDisplayText\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;display=$display")."\">
        <img alt=\"$l_display_no\" src=\"/images/$set_theme/$ico_ball\" /></a>";
      } else if ($display == 0) {
	$nb_field_hidden++;
	echo "<a class=\"OBMDisplayTextCentered\" href=\"".url_prepare("$url?action=$action_display&amp;entity=$entity&amp;fieldname=$fieldname&amp;display=$display")."\">
          <img alt=\"$l_display_yes\" src=\"/images/$set_theme/$ico_crow\" /></a>";
      } else if($display == 2) {
	echo "<img alt=\"$l_mandatory\" src=\"/images/$set_theme/$ico_forbidden\" />";
      }
      echo "
            </td>
            <td class=\"OBMDisplayTextCentered\">
              ";
      if($display == 0) {
	echo "&nbsp;";
      } else {
	$pos = $this->display_pref->f("display_fieldorder") - $nb_field_hidden;
	echo "$pos";
      }
      echo "
             </td>
            <td class=\"OBMDisplayTextCentered\">
                   ".$fieldnames[$fieldname]."
            </td>
            <td class=\"OBMDisplayText\">
              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">
                <tr>";

      // If last field we doesn't display the down arrow
      if ($i == $nb_field) {
	echo "<td>&nbsp;</td>";
      } else {
	echo "<td><a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;new_level=down&amp;fieldorder=".$this->display_pref->f("display_fieldorder")."&amp;which=".$this->pref_which)."\">
           <img alt=\"$l_down\" src=\"/images/$set_theme/$ico_arrow_down\" /></a></td>";
      }

      // If first field we doesn't display the up arrow
      if ($i == 0) {
	echo "<td >&nbsp;</td>";
      } else {
	echo "<td>
              <a href=\"".url_prepare("$url?action=$action_level&amp;entity=$entity&amp;new_level=up&amp;fieldorder=".$this->display_pref->f("display_fieldorder")."&amp;which=".$this->pref_which)."\">
            <img alt=\"$l_up\" src=\"/images/$set_theme/$ico_arrow_up\" /></a></td>";
      }
      echo "
                </tr>
              </table>   
            </td>
          </tr>";
      $i++;
    }
    echo "
        </table>";

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
    global $ico_arrow_up, $ico_arrow_down, $set_theme, $ico_crow, $ico_ball, $ico_forbidden;

    echo "<hr />
      <table class=\"OBMDisplayPref\">
      <tr>
        <td class=\"OBMDisplayHead\" colspan=\"2\">
        $l_help</td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
          <img src=\"/images/$set_theme/$ico_ball\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_display_yes
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
          <img src=\"/images/$set_theme/$ico_crow\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_display_no
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
         <img src=\"/images/$set_theme/$ico_forbidden\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_mandatory
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
         <img src=\"/images/$set_theme/$ico_arrow_down\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_down
        </td>
      </tr>
      <tr>
        <td class=\"OBMDisplayLabel\">
          <img src=\"/images/$set_theme/$ico_arrow_up\" alt=\"\" />
        </td>
        <td class=\"OBMDisplayText\">
          $l_help_up
        </td>
      </tr>
    </table>";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset
  /////////////////////////////////////////////////////////////////////////////
  function dis_data() {
    global $col_textem,$col_table,$col_tableh,$col_error,$col_ok,$col_text_hot;
    global $col_frs, $col_client,$col_balance_pos, $col_balance_neg,$col_text_redhot;
    global $col_inc_redhot,$col_inc_hot,$col_inc_closed;
    global $ico_mail,$ico_contact_new,$ico_web,$ico_ball, $ico_crow;
    global $cdg_id, $set_debug, $set_theme, $set_rows;
    global $path, $time;

    $link_ok = $this->display_link;

    // We display page management only if enabled and a search url is given
    if (($this->data_page) && ($this->data_url != "")) {
      $this->dis_data_index($this->data_url);
    }

    echo "<table class=\"OBMDisplayData\">";
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
	echo "<td class=\"OBMDisplayText\">$dg_id</td>";
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
	  echo "<td class=\"OBMDisplayText\">
                <input type=\"checkbox\" id=\"$name\" name=\"$name\" $cb_check /></td>";
	}
	else
	  echo "<td class=\"OBMDisplayText\">&nbsp;</td>";
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
	$popup = false;
	$popup_width = 600;
	$popup_height = 400;
	$ico = "";
	$align = "left";
	$fieldname = $this->display_pref->f("display_fieldname");

        //=====================================================================
        // Special parameters for some fields
        // $url, $name (eg: to change color), $align,...
        //=====================================================================

	//------------------------------
	// For any field to be used as a date field
	//  -> start with "date_" 
	//   * used in timemanagement
	//------------------------------
	// For date:
	if (substr($fieldname,0,5) == "date_") {
	  $name =date_format($this->data_set->f($fieldname));
	}
	//------------------------------
	// For lists 
	//------------------------------
	// For contacts urls (link):
	if ($fieldname == "list_contact_lastname") {
	  $url = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("list_contact_id");
	}
	// For Company urls (link):
	else if ($fieldname == "list_contact_company") {
	  $url = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("list_contact_company_id");
        }
	
	//------------------------------
	// For the companies 
	//------------------------------
	else if ($fieldname == "company_name") {
	   $url = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("company_id");
	}
	else if ($fieldname == "company_archive") {
          $align = "center";
          if ($this->data_set->f($fieldname)) $name ="X";
          else $name="&nbsp;";
	}
	else if ($fieldname == "company_email") {
	  if (strcmp($this->data_set->f("company_email"),"") != 0) {
	    $url = "mailto:".$this->data_set->f("company_email");
	    $name = "<img src=\"/images/".$set_theme."/".$ico_mail."\" alt=\"".$this->data_set->f("company_email")."\" />";
          }
        } 
	else if ($fieldname == "company_web") {
	  if (strcmp($this->data_set->f("company_web"),"") != 0) {
	    if (strcmp(substr($this->data_set->f("company_web"),0,5), "http:") != 0) {
	      $url = "http://";
	    }
	    $url .= $this->data_set->f("company_web");
	    $name = "<img src=\"/images/$set_theme/$ico_web\" alt=\"".$this->data_set->f("company_web")."\" />";
	  }
        }
        else if ($fieldname == "company_contact_number") {
	  $name = $this->data_set->f("company_contact_number");
	  if ($name > 0) {
	    $url = "$path/contact/contact_index.php?action=search&amp;param_company=".$this->data_set->f("company_id")."&amp;tf_company=".urlencode("".$this->data_set->f("company_name"));
	  }
          $align = "center";
        }
        else if ($fieldname == "company_new_contact") {
          $align = "center";
	  $url = "$path/contact/contact_index.php?action=new&amp;param_company=".$this->data_set->f("company_id");
          $name = "<img src=\"/images/$set_theme/$ico_contact_new\" alt=\"\" />";
        }
        else if ($fieldname == "company_deal_number") {
	  $name = $this->data_set->f("company_deal_number") . " / ";
	  $name .= $this->data_set->f("company_deal_total");
	  $c_id = $this->data_set->f("company_id");
	  if ($name != "0 / 0") {
	    $url = "$path/deal/deal_index.php?action=search&amp;param_company=$c_id";
	  }
          $align = "center";
        }
       
        else if ($fieldname == "company_address1") {
	  if ($this->data_set->f("company_address1") != "") {
	    $name .= $this->data_set->f("company_address1")."<br />\n";
	  }
	  if ($this->data_set->f("company_address2") != "") {
	    $name .= $this->data_set->f("company_address2")."<br />\n";
	  }
	  if ($this->data_set->f("company_zipcode") != "" ) {
	    $name .= $this->data_set->f("company_zipcode") . "&nbsp;&nbsp;";
	  } else {
	    $name.= "&nbsp;&nbsp;";
	  }
	  if ($this->data_set->f("company_town") != "" ) {
	    $name .= $this->data_set->f("company_town");
	  }
	}

	//------------------------------
	// For the contacts 
	//------------------------------
        else if (($fieldname == "contact_lastname") && $link_ok) {
	  $url = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("contact_id");
	}
       	else if (($fieldname == "contact_company_name") && $link_ok){
	  $url = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("contact_company_id");
	}
	else if ($fieldname == "contact_archive") {
          $align = "center";
          if ($this->data_set->f($fieldname)) $name ="X";
          else $name="&nbsp;";
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
	    $name = "<img src=\"/images/$set_theme/$ico_mail\" alt=\"".$this->data_set->f("contact_email")."\" />";
	  }
        }

	//------------------------------
	// For the parentdeals and deals
	//------------------------------
 	else if ($fieldname == "parentdeal_label") {
	  $url = "$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=".$this->data_set->f("parentdeal_id");
	}
        else if ($fieldname == "parentdeal_marketing_lastname") {
	  $url = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$this->data_set->f("parentdeal_marketingmanager_id");
	}
        else if ($fieldname == "parentdeal_technical_lastname") {
	  $url = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$this->data_set->f("parentdeal_technicalmanager_id");
	} 
	else if ($fieldname == "deal_label") {
	  $name="";
	  $url = "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$this->data_set->f("deal_id");
	}
	else if ($fieldname == "deal_archive") {
          $align = "center";
          if ($this->data_set->f($fieldname)) $name ="X";
          else $name="&nbsp;";
	}
	else if ($fieldname == "deal_hitrate") {
          $align = "center";
          $name = $this->data_set->f($fieldname) . " %";
	}
	else if ($fieldname == "deal_company_name") {
	  $name="";
	  $url = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("deal_company_id");
        } 
	else if ($fieldname == "deal_datealarm") {
	  $date = $this->data_set->f($fieldname);
	  $datealarm = date_format($date);
	  $time = time();
	  if ($time > ($date + 86400)) {
            $name="<font color=\"#$col_error\">$datealarm</font>";
	  } else if ($time > $date) {
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
	else if ($fieldname == "timecreate") {
	  $name = datetime_format($this->data_set->f($fieldname));
	}
	else if ($fieldname == "timeupdate") {
	  $name = datetime_format($this->data_set->f($fieldname));
	}

        //-------------------------------------
	// invoice from the deal display screen
        //-------------------------------------
	elseif ($fieldname == "deal_invoice_label") {
	  $url = "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$this->data_set->f("invoice_id");
	} elseif (($fieldname == "deal_invoice_HT") 
		   || ($fieldname == "deal_invoice_TTC") ) {
	  $amount = $this->data_set->f($fieldname);
	  if ($this->data_set->f("invoice_inout") == '+') {
	    $couleur = $col_client;
	  }
	  else {
	    $couleur = $col_frs;
	  }
	  $name = "$amount";
	}

	//------------------------------
	// For the users 
	//------------------------------
	else if ($fieldname == "userobm_login") {
	   $url = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$this->data_set->f("userobm_id");
	}
	else if ($fieldname == "userobm_email") {
          $email = $this->data_set->f("userobm_email");
	  if (strcmp($email ,"") != 0) {
	    $url = "mailto:$email";
	    $name = "<img src=\"/images/$set_theme/$ico_mail\" alt=\"$email\">$email";
          }
        } 
	else if ($fieldname == "userobm_archive") {
          $align = "center";
          if ($this->data_set->f($fieldname)) $name ="X";
          else $name="&nbsp;";
	}
	else if ($fieldname == "userobm_timelastaccess") {
	  $date = $this->data_set->f($fieldname);
	  $name = date_format($date);
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
	  $url="$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$this->data_set->f("deal_id");
	} elseif ($fieldname == "invoice_label") {
	  $url="$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$this->data_set->f("invoice_id");
	} elseif ($fieldname == "invoice_company") {
	  $url="$path/company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("company_id");
	} elseif ($fieldname == "invoice_pay_label") {
	  $url = "$path/payment/payment_index.php?action=detailconsult&amp;param_payment=".$this->data_set->f("invoice_payment_id");
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
	  $name = "$amount";
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
	  $url = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("company_id");
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
	  $name = date_format($this->data_set->f("date"));
	}
        else if ($fieldname == "payment_expect_date") {
	  $name = date_format($this->data_set->f("expect_date"));
	}

	//For Image scan (crow or round):
	if($fieldname == "service_status") {
	  $name = ($this->data_set->f($fieldname) == 1) ? "<img border=0 src=\"/images/$set_theme/$ico_ball\">" : "<img border=0 src=\"/images/$set_theme/$ico_crow\">";
	  $align = "center";
	}

	//------------------------------
	// For the contracts 
	//------------------------------
        else if ($fieldname == "contract_label") {
	  $url = "$path/contract/contract_index.php?action=detailconsult&amp;param_contract=".$this->data_set->f("contract_id");
	}
       	else if ($fieldname == "contract_company_name") {
	  $url = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$this->data_set->f("contract_company_id");
       	}
        else if ($fieldname == "contract_marketmanager") {
	  $url = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$this->data_set->f("contract_techmanager_id");
	  $name = $this->data_set->f("lmarket") . " " . $this->data_set->f("fmarket");
	}
        else if ($fieldname == "contract_techmanager") {
	  $url = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$this->data_set->f("contract_marketmanager_id");
	  $name = $this->data_set->f("ltech") . " " . $this->data_set->f("ftech");
	}
        else if ($fieldname == "contract_dateexp") {
	  $date = $this->data_set->f($fieldname);
	  $name = date_format($date);
	}
	//------------------------------
	// For the incidents 
	//------------------------------
        else if ($fieldname == "incident_label") {
	  $url = "$path/incident/incident_index.php?action=detailconsult&amp;param_incident=".$this->data_set->f("incident_id");
	}
       	else if ($fieldname == "incident_owner_lastname") {
	  $url = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("incident_owner");
	}
       	else if ($fieldname == "incident_logger_lastname") {
	  $url = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$this->data_set->f("incident_logger");
	}
        else if ($fieldname == "incident_date") {
	  $name = date_format($this->data_set->f("date"));
	}
	
        //------------------------------
        // For the time management
        //------------------------------
        else if (($fieldname == "task_id")
                  && ($this->data_set->f("task_status")==0) ) {
	  $popup = true;
	  $popup_width = 970;
	  $popup_height = 270;
	  $url = "time_index.php?action=detailupdate&amp;popup=1&amp;task_id=".$this->data_set->f("task_id")."&amp;param_begin=".$time["date"]."&amp;param_end=".$time["date_end"];
	  $name = "<img border=0 src=\"/images/$set_theme/modify.png\">";
	}
	else if ($fieldname == "task_id")
           $name = " ";
 
        //===================
        // Final cell writing
        //===================
        // if ($special_col_table != "") {
	//   $td = "<td align=\"$align\" bgcolor=\"#$special_col_table\">";
        // }
	// else {
	//   $td = "<td align=\"$align\" bgcolor=\"#$col_table\">";
	// }

	// Set the alignment
	if ($align == "center") {
	  $td = "<td class=\"OBMDisplayTextCentered\">";
	} else {
	  $td = "<td class=\"OBMDisplayText\">";
	}
        $content = "";
	
	// Pre cell display (open popup, link)
	if ($popup) {
	  $content = "<a class=\"OBMDisplayText\" href=\"\" OnClick=\"window.name='root';window.open('$url','',config='height=$popup_height,width=$popup_width,scrollbars=no,toolbar=no,menubar=no,resizable=no'); return false;\">";
	} else if ($url != "") {
	  $content = "<a class=\"OBMDisplayText\" href=\"".url_prepare("$url")."\">";
	}
	
	// Cell display
	if ($name == "") {
	  $content .= $this->data_set->f($fieldname);
	} else {
	  $content .= $name;
	}
	
	// Post cell display (close link, popup)
	if ($popup || ($url != "")) {
	  $content .= "</a>";
	}
	
	// Cell writing
	echo "$td $content </td>\n";
      }
      

      //-----------------------------------------------------
      // Row end : if action and right, we display a checkbox
      //-----------------------------------------------------
      if (($this->data_cb_side == "right")
            && ($this->data_cb_text != "")) {
	echo "<td align=\"center\" bgcolor=\"#$col_table\">
                <input type=\"checkbox\" id=\"".$this->data_cb_name.$this->data_set->f($this->data_idfield)."\" 
                 name=\"".$this->data_cb_name.$this->data_set->f($this->data_idfield)."\" /></td>";
      }
      $this->display_pref->seek(0);
      echo "</tr>";
      $special_col_table="";
    }

    //--------------------------------------------------------
    // Data Set end : if header set to both, display an header
    //--------------------------------------------------------
    if ($this->data_header == "both") {
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
    global $page, $new_order;
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
    echo "<p class=\"messageInfo\">";
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
      echo "</p>";
      $this->data_set->seek($first_row);
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset headers (col names)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header($url) {
    global $page, $new_order, $order_dir;
    global $col_tableh, $col_textem;
    global $fieldnames, $cdg_id, $set_debug;

    // If displayed without links (in popup) add this in url
    if (! $this->display_link) {
      $url .= "&amp;popup=1";
    }

    $this->display_pref->seek(0);
    echo "
    <tr>";
    
    // If debug level Id is set, display the entity id
    if (($set_debug > 0) && (($set_debug & $cdg_id) == $cdg_id)) {
      echo "<td class=\"OBMDisplayHead\">Id</td>";
    }

    // Display the checkbox title if needed (exists and left side)
    if (($this->data_cb_side == "left")
          && ($this->data_cb_text != "")) {
      echo "<td class=\"OBMDisplayHead\">".$this->data_cb_text."</td>";
    }

    while($this->display_pref->next_record()) {
      $fieldname = $this->display_pref->f("display_fieldname");
      $label = $fieldnames[$fieldname];
      $order = ($order_dir == "ASC" ? "DESC" : "ASC");

      echo "
        <td class=\"OBMDisplayHead\">";
      if ($this->data_order) {
        echo "<a class=\"OBMDisplayHead\" href=\"".$url.
          (($url != "") ? "&amp;page=$page" : "" ) .
          "&amp;new_order=$fieldname&amp;order_dir=$order\">";
      }
      echo $label;
      if ($this->data_order) {
        echo "</a>";
      }

      echo "</td>";
    }

    // Display the checkbox title if needed (exists and right side)
    if (($this->data_cb_side == "right")
         && ($this->data_cb_text != "")) {
      echo "<td class=\"OBMDisplayHead\">".$this->data_cb_text."</td>";
    }
    echo "</tr>";
  }

  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset headers (col names)
  //   for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_header_file($sep) {
    global $fieldnames, $cdg_id, $set_debug;

    $this->display_pref->seek(0);
    
    while($this->display_pref->next_record()) {
      $fieldname = $this->display_pref->f("display_fieldname");
      $label = $fieldnames[$fieldname];

      echo " $label $sep ";
    }
    echo "\n";
  }


  /////////////////////////////////////////////////////////////////////////////
  // Function that display the dataset
  //   for result in a file (as .csv)
  /////////////////////////////////////////////////////////////////////////////
  function dis_data_file($sep) {
    global $cdg_id, $set_debug;
    global $time;

    $this->dis_data_header_file($sep);

	//    echo "nb lignes : ". $this->data_set->num_rows() . "\n";
    // Iteration on each data row of dataset
    while ($this->data_set->next_record()) {
      $this->display_pref->seek(0);

      // For each field in the data row we walk through the pref dbo ordered
      // and for each entry (field) we set special parameters and display it
      while ($this->display_pref->next_record()) {
   	    $fieldname = $this->display_pref->f("display_fieldname");

 
        echo $this->data_set->f($fieldname) . $sep;
	  }
	  echo "\n";
	}
 	echo "\n\n\n";

  }



} // End of the object OBM_DISPLAY



?>
