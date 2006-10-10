<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : test-list.php                                                //
//     - Desc : Non-regression test : List                                   //
// 2006-09-04 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Ce script :
// *** Groupes
// - Supprime tous les groupes non locaux
// - Ajoute les groupes presents dans le fichier
// *** Utilisateurs
// - Ajoute les nouveaux presents dans le fichier
// - Modifie les presents dans fichier et existant (selon ident unique)(si !=)
// - Archive les utilisateurs absents du fichier (Ne supprime pas)
// *** Appartenance utilisateurs aux groupes
// - Ajoute les nouvelles appartenances presentes dans le fichier
// - Ne supprime pas d'appartenance
///////////////////////////////////////////////////////////////////////////////

$time_start = microtime_float();

//-----------------------------------------------------------------------------
// Parametres modifiables
//-----------------------------------------------------------------------------
$web_perms_default = "1";         // acces web par defaut : 1 = oui, 0 = non
$sep = ";";
//-----------------------------------------------------------------------------

$path = "../php";
$obminclude = "obminclude";
include("obminclude/global.inc");
include("global_test.inc");
require("$path/list/list_query.inc");

$retour = parse_arg($argv);
$date = date("Ymd-His");
$file = "test-list-$date.txt";

// OpenGet datas from the import file
$handle = fopen("$file", "w");
if (! $handle) {
  echo "ERREUR: impossible d'ouvrir le fichier $file. Fin...";
  exit;
}
fwrite($handle, "TESTS:List:version : $obm_version\n");

// Process Lists
$lists = get_list_list();
process_list_list($lists);

$time_end = microtime_float();
$duree = $time_end - $time_start;
echo "\n**********************************************************************
***** Fin traitement : Version : $obm_version
***** Duree : $duree secondes
**********************************************************************\n";
echo $log;

exit;

function microtime_float()
{
   list($usec, $sec) = explode(" ", microtime());
   return ((float)$usec + (float)$sec);
} 


///////////////////////////////////////////////////////////////////////////////
// Recuperation de la liste des listes
// Returns:
// $l hash with : $l[$id]=array(list_infos)
///////////////////////////////////////////////////////////////////////////////
function get_list_list() {

  $l = array();

  $query = "SELECT *
    FROM List
    ORDER BY list_id";

  $l_q = new DB_OBM;
  $l_q->query($query);

  while ($l_q->next_record()) {
    $id = $l_q->f("list_id");
    $l[$id]["id"] = $id;
    $l[$id]["list_id"] = $id;
    $l[$id]["privacy"] = $l_q->f("list_privacy");
    $l[$id]["name"] = $l_q->f("list_name");
    $l[$id]["subject"] = $l_q->f("list_subject");
    $l[$id]["email"] = $l_q->f("list_email");
    $l[$id]["mode"] = $l_q->f("list_mode");
    $l[$id]["mailing_ok"] = $l_q->f("list_mailing_ok");
    $l[$id]["contact_archive"] = $l_q->f("list_contact_archive");
    $l[$id]["info_pub"] = $l_q->f("list_info_publication");
    $l[$id]["static_nb"] = $l_q->f("list_static_nb");
    $l[$id]["query_nb"] = $l_q->f("list_query_nb");
    $l[$id]["query"] = $l_q->f("list_query");
    $l[$id]["structure"] = $l_q->f("list_structure");
  }

  return $l;
}


///////////////////////////////////////////////////////////////////////////////
// Process the List list (create 1 log for each list)
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function process_list_list() {
  global $clist_mode_normal, $clist_mode_expert, $lists;

  $cpt_list_ok = 0;
  $cpt_list_err = 0;
  $cpt_list = count($lists);

  $txt = "\n***** Traitement des listes";
  $log .= $txt;
  echo $txt;

  $obm_q = new DB_OBM;

  foreach ($lists as $id => $l) {
    
    $query = "";

    // get the list query
    if ($l["mode"] == $clist_mode_normal) {
      if ($l["structure"] != "") {
	$criteria = unserialize($l["structure"]);
	if ($criteria != "") {
	  $as_criteria = $criteria;
	  if (is_array($as_criteria)) {
	    array_walk($as_criteria, 'list_add_slashes_array');
	  }
	  $l["criteria"] = $as_criteria;
	  $dynlist = make_list_query_from_criteria($l);
	  $query = $dynlist["query"];
	}
      }
      // expert mode
    } else {
      $query = $l["query"];
    }

    $obm_q->query($query);
    process_one_list($l, $obm_q);
  }



  echo "\n***** Fin traitement des listes";
  $txt .= "\n\n";
  $log .= $txt;
  echo $txt;
}


///////////////////////////////////////////////////////////////////////////////
// Process one List
// Parameters:
//   - $l   : array with list infos
//   - $l_q : DBO with list own query result
///////////////////////////////////////////////////////////////////////////////
function process_one_list($l, $l_q) {
  global $handle;

  $to_write = "";
  $id = $l["id"];
  $name = $l["name"];

  $txt = "\n***** List : $id : $name";
  $log .= $txt;
  echo $txt;

  $contact_ids = array();
  $company_ids = array();
  while ($l_q->next_record()) {
    $cid = $l_q->f("contact_id");
    $compid = $l_q->f("company_id");
    if (! in_array($cid, $contact_ids)) {
      $contact_ids[] = $cid;
    }
    if (! in_array($compid, $company_ids)) {
      $company_ids[] = $compid;
    }
    // write the row info in the result file (which can have contact doublons)
    $to_write .= "\nC:$cid";
  }    
  $nb_con = count($contact_ids);
  $nb_comp = count($company_ids);

  $to_write = "\n**** List:$id:$name:#Contact=$nb_con:#Company=$nb_comp" . $to_write; 
  $to_write .= "\n"; 

  // write file infos
  fwrite($handle, "$to_write"); 

  $txt = "\nContact : $nb_con";
  $txt .= "\nCompany : $nb_comp";
  $txt .= "\n";
  $log .= $txt;
  echo $txt;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_arg($argv) {
  global $debug, $fichier_import;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      echo "Usage: php import-ldap.php -f filename\n\n";
      return true;
      break;
    case '-f':
    case '--file':
      list($nb2, $val2) = each ($argv);
      $fichier_import = $val2;
      if ($debug > 0) { echo "-f -> \$fichier_import=$val2\n"; }
      break;
    }
  }
}


</script>
