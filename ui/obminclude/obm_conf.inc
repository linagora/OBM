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
$cgp_site_include = true;

// is Mail enabled ? (agenda)
$cgp_mail_enabled = false;

//-----------------//
// Displayed Infos //
//-----------------//
// sections -- Default is true
//$cgp_show["section"]["compta"] = false;

// modules -- Default is true
//$cgp_show["module"]["user"] = false;
//$cgp_show["module"]["group"] = false;

</script>
