<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm_conf.inc                                                 //
//     - Desc : OBM specific site configuration file                         //
// 2003-10-17 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


// Database infos
$obmdb_host = "localhost";
$obmdb_dbtype = "MYSQL"; // (MYSQL || PGSQL)
//$obmdb_dbtype = "PGSQL"; // (MYSQL || PGSQL)
$obmdb_db = "obm";
$obmdb_user = "obm";
$obmdb_password = "obm";

// Use specific site includes
//$cgp_site_include = true;

// Use Database sessions
//$cgp_sess_db = true;

// is Mail enabled ? (agenda)
$cgp_mail_enabled = false;

// is mailing enabled by default for a new user
$cgp_mailing_default = true;

// OBM System groups (groups id)
$cg_adm = 1;
$cg_com = 1;
$cg_prod = 2;

//-----------------//
// Displayed Infos //
//-----------------//
// sections -- Default is true
//$cgp_show["section"]["prod"] = false;
//$cgp_show["section"]["compta"] = false;
$cgp_show["section"]["aliamin"] = false;

// modules -- Default is true
//$cgp_show["module"]["contact"] = false;
//$cgp_show["module"]["deal"] = false;
//$cgp_show["module"]["agenda"] = false;
//$cgp_show["module"]["todo"] = false;
//$cgp_show["module"]["user"] = false;
//$cgp_show["module"]["group"] = false;
//$cgp_show["module"]["project"] = false;
//$cgp_show["module"]["contract"] = false;
//$cgp_show["module"]["document"] = false;
//$cgp_show["module"]["account"] = false;
//$cgp_show["module"]["invoice"] = false;
//$cgp_show["module"]["payment"] = false;
//$cgp_show["module"]["time"] = false;
//$cgp_show["module"]["statistic"] = false;

// Company module
//$cgp_hide["company"]["number"] = true;
//$cgp_hide["company"]["companytype_label"] = true;
//$cgp_hide["company"]["companyactivity_label"] = true;
//$cgp_hide["company"]["companynafcode_code"] = true;
//$cgp_hide["company"]["category"] = true;
//$cgp_hide["company"]["address3"] = true;

// Contact module
//$cgp_hide["contact"]["contact_function"] = true;
//$cgp_hide["contact"]["contact_title"] = true;
//$cgp_hide["contact"]["category1"] = true;
//$cgp_hide["contact"]["category2"] = true;
//$cgp_hide["contact"]["address3"] = true;

</script>
