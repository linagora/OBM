<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : addresses.php                                                //
//     - Desc : Main export File                                             //
// 2002-07-14 Pierre Baudracco (from jlb)                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
/* jlb 17-jun-01 produce a formatted list for printing
   max 19-sep-01 RTF label generation added */
/* The main class can generate several logical formats: table, labels
   A class fmtXXX must be written for each physical output format  */

// light OBM environment (no menu in the exported files)
global $perm, $module;
$module = "list";
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/global.inc");

page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check_permissions($module, $action);
page_close(); //?usefull?

include("$obminclude/global_pref.inc");
include("list_query.inc") ;

////////////////////////////////////////////////////////////
//        classe to format excel ouput                    //
////////////////////////////////////////////////////////////
class fmtxls {
  var $_prev="";
  function b_doc($title) {
    header ("Content-type: application/vnd.ms-excel");
    header ("Cache-Control: no-cache, must-revalidate");
    $this->_prev="b_doc";
  }
  function e_doc() {$this->_prev="e_doc";}
  function b_table() {$this->_prev="b_table";}
  function e_table() {$this->_prev="e_table";}
  function b_lbl() {$this->_prev="b_lbl";}
  function e_lbl() {$this->_prev="e_doc";return "\n";}
  // field
  function field($s) {
    $prev=$this->_prev;
    $this->_prev="field";
    if($prev == "b_lbl") {
      return "$s";
    } else {
      return "\t$s";
    }
  }
}


////////////////////////////////////////////////////////////
//        classe to format HTML index                     //
////////////////////////////////////////////////////////////
class _fmt_htm {   //base class, do not instanciate
  var $_prev="";
  function b_doc($title) {
    header ("Content-type: text/html");
    header ("Cache-Control: no-cache, must-revalidate");
    $this->_prev="b_doc";
    return "<html>\n<header><title>$title</title>\n</header>\n";
  }
  function e_doc() {$this->_prev="e_doc";return "</html>\n";}
  function b_page() {}
  function e_page() {}
}

////gestion _prev useless -> to suppress
class fmt_htm_tab extends _fmt_htm {
  function b_table() {$this->_prev="b_table";return "<table BORDER=1 BGCOLOR=\"#FFFFFF\">\n";}
  function e_table() {$this->_prev="e_table";return "</table>\n";}
  function b_lbl() {$this->_prev="b_lbl";return "<tr>";}
  function e_lbl() {$this->_prev="e_lbl";return "</tr>\n";}
  function field($s) {
    $prev=$this->_prev;
    $this->_prev="field";
    if ($s == "") {
      return "<td>&nbsp;</td>";
    } else {
      return "<td>$s</td>";
    }
  }
}


/////////////////////////////////////////////////////////// 
//        classe to format HTML (NCOL columns of labels)  //
///////////////////////////////////////////////////////////
class fmt_htm_label extends _fmt_htm {
  var $_n=0;
  var $_ncol;
  var $_nlin;

  function fmt_htm_label($lin=8,$col=2) { 
    $this->_nlin=$lin;
    $this->_ncol=$col;
  }

  function b_table() {return "<table BORDER=1 BGCOLOR=\"#FFFFFF\">\n";}

  function e_table() {
    $r=$this->_n % $this->_ncol; 
    if ($r != 0 ) $r=$this->_ncol - $r ;
    $s="";
    while ($r > 0 ) {
      $s.=$this->b_lbl();
      $s.=$this->field("");
      $s.=$this->e_lbl();
      $r--;
    }
    $s.="</table>\n";
    return $s ;
  }

  function b_lbl() {
    $this->_n++;
    //echo "\n<!- n=$this->_n col=$this->_ncol- ->\n";
    $s="";
    if (($this->_n % $this->_ncol) == 1)  $s.="\n<tr>";  //begin line
    $s.="<td>";
    return $s;
  }

  function e_lbl() {
    $s="</td>\n";
    if ($this->_n % $this->_ncol == 0) $s .= "</tr>";
    return $s ;
  }

  function ref_field($s) {
    return "<font size=1>". $this->field(&$s). "</font>";
  }

  function field($s) {
    if ($s == "") {
      return "&nbsp;<br>\n";
    } else {
      return "$s<br>";
    }
  }
}


////////////////////////////////////////////////////////////
//        classe to format RTF labels 2C x 8L A4          //
////////////////////////////////////////////////////////////
// jlb 29-sept-2001 logics & data separation.
// could be used to replace fmt_htm_label with an addresses_htm.inc data file
class fmt_label {
  var $_n=0;
  var $_nlin=8;//rtf code only for 8L x 2C
  var $_ncol=2;
  var $_tag_doc_head;
  var $_tag_table_head1;
  var $_tag_table_head2;
  var $_tag_page_head;
  var $_tag_lbl_head;
  var $_tag_lbl1_end;
  var $_tag_lbl2_end;
  var $_tag_field_ref_head;
  var $_tag_field_ref_end;
  var $_tag_field_head;
  var $_tag_field_end;
  var $_tag_page_end;
  var $_tag_doc_end;

  function fmt_label($lin=8,$col=2,$tags_data="addresses_rtf.inc") { 
    //init of RTF variables
    include ($tags_data);     
    $this->_nlin=$lin;
    $this->_ncol=$col;
    if ($tags_data == "addresses_ref.inc" && ($this->_nlin != 8 || $this->_ncol != 2)) {
      $this->errormsg("addresses_rtf.inc supports only pages with 2x8 labels");
    }
  }

  function b_doc($title) {
    header ("Content-type:  application/rtf");
    header ("Cache-Control: no-cache, must-revalidate");
    return $this->_tag_doc_head1. strtr($title,"\{}"," ()"). $this->_tag_doc_head2. 
      $this->b_page();
  }

  function e_doc() {return $this->_tag_doc_end;}
  function b_page() {return $this->_tag_page_head;}
  function e_page() {return $this->_tag_page_end;}
  function b_table() {return $this->_tag_table_head;}

  function e_table() {
    $r=$this->_n % $this->_ncol; 
    if ($r != 0 ) $r=$this->_ncol - $r ;
    $s="";
    while ($r > 0 ) {
      $s.=$this->b_lbl();
      $s.=$this->field("");
      $s.=$this->e_lbl();
      $r--;
    }
    $s.=$this->_tag_table_end;
    return $s ;
  }

  function b_lbl() {
    $s="";
    //change page
    if ($this->_n != 0 && ($this->_n % ($this->_ncol * $this->_nlin) == 0) ) {
      $s .= $this->e_table() ;
      $s .= $this->e_page() ;
      $s .= $this->b_table() ;
      $s .= $this->b_page() ;
    }
    $this->_n++;
    $s.=$this->_tag_lbl_head;
    //$s.="n=$this->_n col=$this->_ncol";
    return $s;
  }

  function e_lbl() {
    if ($this->_n % $this->_ncol == 0) {
      $s = $this->_tag_lbl2_end;
    } else {
      $s = $this->_tag_lbl1_end;
    }
    return $s ;
  }

  function ref_field($s) {
    return $this->_tag_field_ref_head. $this->field(&$s). $this->_tag_field_ref_end;
  }
  function field($s) {
    return $this->_tag_field_head. ( $s==""?"  ":$s). $this->_tag_field_end;
  }
}

/* Main class
jlb 17-jun-01
	list_addresses?fmt="exportformat"&amp;form="mystyle"&amp;list="mylists"
		myformat = "xls"|"htm"  	choose the export format
		mystyle  = "table"|"label"	style of the document
		mylists  = "00001,0002,...." concatenation of list ids with comma separator. 

TO-DO-LIST
	-  control of list parameter
   -  test of the rights for call ("user" or "editor" required).
*/

class list_addresses {
  var $f=""; // logical format
  var $c=""; // formatting class for physical format
  var $l=""; // lists 
  var $d=""; // pointer on data
  var $_reference; //unset
  
  function errormsg($s) {
    echo "<B>ERROR - class list_addresses: </B>$s";
    return "1";
  }
  
  // Contructor
  function list_addresses($fmt="",$form="",$list="") {
    $this->set_format($fmt,$form);
    $this->set_list($list);
  }

  function set_format($fmt,$form) {
    if ($fmt == "xls" && $form == "table") {
      $this->c= new fmtxls;
    } elseif ($fmt == "htm" && $form == "table") {
      $this->c= new fmt_htm_tab;
    } elseif ($fmt == "htm" && $form == "label") {
      $this->c= new fmt_htm_label(8,2);
    } elseif ($fmt == "rtf" && $form == "label") {
      $this->c= new fmt_label(8,2,"addresses_rtf.inc");
    } else {
      $this->errormsg("Bad parameters: (fmt=$fmt,form=$form), autorized values:(xls,table), (htm,table), (htm,label), (rtf,label)");
      exit;
    } 
    $this->f=$form ;
  }

  function set_list($list) {
    if ($list !="") {
      $this->l=$list ;
    } else {
      $this->errormsg("Bad list parameter (list=$list)");
      exit;	
    } 
    $this->d = run_query_contacts_for_letter($this->l);
  }

  function generate_page() {
    global $l_let_no_contact;
    $nb_lbl=0;
    if ($this->d->nf() <= 0) {
      echo "<b>$l_let_no_contact</b><br>";
      return 1;
		}
    if ($this->f == "table") {
      $this->__generate_table();
      //} else if ( $this->f == "synth" ) {
      //$this->__generate_synthesis();
    } else if ( $this->f == "label"   ) {
      $this->__generate_label();
    }
  }
  
  // Logical format: detailled table
  // generate a header, one line per record and one field per column
  function __generate_table() { //do not call directly
    echo $this->c->b_doc("Liste(s): ".$this->reference()).
      $this->c->b_table().
      // header for the table
      $this->c->b_lbl().
      //$this->c->field( "list_contact_visibility").
      $this->c->field( "id").
      $this->c->field( "kind").
      $this->c->field( "lastname").
      $this->c->field( "firstname").
      $this->c->field( "company").
      $this->c->field( "type_address").
      $this->c->field( "address1").
      $this->c->field( "address2").
      $this->c->field( "zipcode").
      $this->c->field( "town").
      $this->c->field( "expresspostal").
      $this->c->field( "country").
      //$this->c->field( "list_contact_function").
      $this->c->field( "phone").
      $this->c->field( "homephone").
      $this->c->field( "mobilephone").
      $this->c->field( "fax").
      $this->c->field( "email").
      //$this->c->field( "comment").
      $this->c->e_lbl();
    //table
    $this->d->seek(0);
    while($this->d->next_record()) {
      echo
	$this->c->b_lbl().
	//$this->c->field($this->d->f("list_contact_visibility")).
	$this->c->field($this->d->f("list_contact_id")).
	$this->c->field($this->d->f("list_contact_kind")).
	$this->c->field($this->d->f("list_contact_lastname")).
	$this->c->field($this->d->f("list_contact_firstname")).
	$this->c->field($this->d->f("contact_company_name"));
      if( $this->d->f("list_contact_town")!= "") {
	$personal_address="P";
	$add1=$this->d->f("list_contact_address1");
	$add2=$this->d->f("list_contact_address2");
	$zip=$this->d->f("list_contact_zipcode");
	$town=$this->d->f("list_contact_town");
	$cedex=$this->d->f("list_contact_expresspostal");
	$country=$this->d->f("list_contact_country");
      }else {
	$personal_address="C";
	$add1=$this->d->f("contact_company_address1");
	$add2=$this->d->f("contact_company_address2");
	$zip=$this->d->f("contact_company_zipcode");
	$town=$this->d->f("contact_company_town");
	$cedex=$this->d->f("contact_company_expresspostal");
	$country=$this->d->f("contact_company_country");
      }
      echo
	$this->c->field($personal_address).
	$this->c->field($add1).
	$this->c->field($add2).
	$this->c->field($zip).
	$this->c->field($town).
	$this->c->field($cedex).
	$this->c->field($country).
	//$this->c->field($this->d->f("list_contact_function")).
	$this->c->field($this->d->f("list_contact_phone")).
	$this->c->field($this->d->f("list_contact_homephone")).
	$this->c->field($this->d->f("list_contact_mobilephone")).
	$this->c->field($this->d->f("list_contact_fax")).
	$this->c->field($this->d->f("list_contact_email")).
	//$this->c->field($this->d->f("list_contact_comment")).
	$this->c->e_lbl();
    }
    // informations on the table
    echo
      $this->c->b_lbl().
      $this->c->field( "").
      $this->c->field( "List(s): ".$this->reference()).
      $this->c->b_lbl();
    echo 
      $this->c->e_table().
      $this->c->e_doc();
  }

  // Logical format: label
  // generate no header, one line per record, with concatenated fields in columns
  function __generate_label() { //do not call directly
    echo $this->c->b_doc("Liste(s): ".$this->reference());
    $this->d->seek(0);
    echo $this->c->b_table();
    $nb_lbl = 0;
    while($this->d->next_record()) {
      //choose the best solution for address source
      if( $this->d->f("list_contact_town")!= "") {
	$personal_address="P";
	$add1=$this->d->f("list_contact_address1");
	$add2=$this->d->f("list_contact_address2");
	$zip=$this->d->f("list_contact_zipcode");
	$town=$this->d->f("list_contact_town");
	$cedex=$this->d->f("list_contact_expresspostal");
	$country=$this->d->f("list_contact_country");
      }else {
	$personal_address="C";
	$add1=$this->d->f("contact_company_address1");
	$add2=$this->d->f("contact_company_address2");
	$zip=$this->d->f("contact_company_zipcode");
	$town=$this->d->f("contact_company_town");
	$cedex=$this->d->f("contact_company_expresspostal");
	$country=$this->d->f("contact_company_country");
      }
      echo
	$this->c->b_lbl().
	$this->c->ref_field($personal_address. $this->reference()).
	$this->c->field($this->d->f("contact_company_name")).
	$this->c->field($this->d->f("list_contact_kind")." ".
			$this->d->f("list_contact_firstname")." ".
			$this->d->f("list_contact_lastname")).
	$this->c->field($add1);
      if (strcmp($add2,"")) {echo $this->c->field($add2);}
      if (strcmp($cedex,"")) {echo  $this->c->field("BP ".$cedex);}
      echo $this->c->field($zip." ".$town);
      if (strcmp($country,"")) {echo $this->c->field($country);}
      echo $this->c->e_lbl();
    }
    echo $this->c->e_table().
      $this->c->e_doc();
  }

  function reference() {
    if (!isset($this->_reference) ) {
      $ids=explode(",",$this->l);
      $n=count($ids);
      $s="(".$n.")";
      for ($i=0; $i<$n && strlen($s) <20 ; $i++ ) {
	$s.= " " . get_list_name($ids[$i]);
      }
      if ($i != $n) $s.=" ...";
      $this->_reference=$s ;
    }
    return $this->_reference ;
  }
}	//end class list_addresses

//$time=time();
$a=new list_addresses($fmt,$form,$list);
$a->generate_page();
//$a->free();
//$time=time()-$time;
//echo "Durée d'execution: $time";
?>
