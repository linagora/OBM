<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : test-list.php                                                //
//     - Desc : Non-regression test : List                                   //
// 2006-09-04 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$time_start = microtime_float();

//-----------------------------------------------------------------------------
// Parametres modifiables
//-----------------------------------------------------------------------------
$sep = ";";
//-----------------------------------------------------------------------------

$path = "../php";
$obminclude = "../obminclude";
include("../obminclude/global.inc");
include("global_test.inc");
include("../obminclude/of/of_category.inc");
require("$path/list/list_query.inc");

$params = parse_arg($argv);
$date = date("Ymd-His");
$file = "test-list-$date.txt";

echo "\n**********************************************************************
***** Traitement Listes : OBM $obm_version : DB $obmdb_db $obmdb_dbtype
**********************************************************************";

if ($params["action"] == "dump") {
///////////////////////////////////////////////////////////////////////////////

  // Open dump file in write mode
  $handle = fopen("$file", "w");
  if (! $handle) {
    echo "ERREUR: impossible d'ouvrir le fichier $file. Fin...";
    exit;
  }
  fwrite($handle, "TESTS:List:version : $obm_version\n");

  // Process Lists
  $lists = get_list_list();
  process_list_list($lists);

} elseif ($params["action"] == "test") {
///////////////////////////////////////////////////////////////////////////////

  $handle_f = fopen("$params[from_file]", "r");
  if (! $handle_f) {
    echo "ERREUR: impossible d'ouvrir le fichier $params[from_file]. Fin...";
    exit;
  }

  $handle_t = fopen("$params[to_file]", "r");
  if (! $handle_t) {
    echo "ERREUR: impossible d'ouvrir le fichier $params[to_file]. Fin...";
    exit;
  }

  $list_num = 1;
  $end = $false;
  while ($end == false) {
    $lf = get_next_list_from_dump($handle_f);
    if ($lf == false) {
      $end = true;
    } else {
      $lt = get_list_from_dump_id($handle_t, $lf["id"]);
      compare_lists($lf, $lt);
    }
    $list_num++;
    //$end = true;
  }

  fclose($handle_f);
  fclose($handle_t);
}

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
// Recuperation de la prochaine list du fichier dump donne
// Parameters:
//   - $lf : from list
//   - $tf : to list
// Returns:
// $l hash with list infos [name], [id], [nb_contact], [nb_company], []
///////////////////////////////////////////////////////////////////////////////
function compare_lists($lf, $lt) {
  global $params;

  $diff = "";
  $nb_contact = str_pad($lf["nb_contact"],6," ", STR_PAD_LEFT);
  $nb_company = str_pad($lf["nb_company"],6," ", STR_PAD_LEFT);
  if ($lt == false) {
    $diff .= " REMOVED !";
  } else {
    if ($lf["name"] != $lt["name"]) {
      $diff .= " NAME";
    }
    if ($lf["nb_contact"] != $lt["nb_contact"]) {
      $diff .= " #Cont($lt[nb_contact])";
    }
    if ($lf["nb_company"] != $lt["nb_company"]) {
      $diff .= " #Comp($lt[nb_company])";
    }
  }
  if ($params["verbose"] == "all" || $params["verbose"] == "verbose" || $diff != "") {
    $diff = str_pad($diff,24," ", STR_PAD_LEFT);
    $log = "\n:List($lf[id]):$nb_contact:$nb_company: $diff : $lf[name]:";
    echo $log;
  }

  if (($params["verbose"] == "all" || $params["verbose"] == "detail") && $diff != "") {
    $res = array_diff($lf["cids"], $lt["cids"]);
    if (is_array($res) && count($res) > 0) {
      echo "\nContacts entries removed:";
      foreach ($res as $entry) {
	echo "$entry-";
      }
    }
    $res = array_diff($lt["cids"], $lf["cids"]);
    if (is_array($res) && count($res) > 0) {
      echo "\nNew Contacts entries:";
      foreach ($res as $entry) {
	echo "$entry-";
      }
    }
  }

  return $l;
}


///////////////////////////////////////////////////////////////////////////////
// Recuperation de la prochaine list du fichier dump donne
// Parameters:
//   - $handle : identifiant du fichier
// Returns:
// $l hash with list infos [name], [id], [nb_contact], [nb_company], []
///////////////////////////////////////////////////////////////////////////////
function get_next_list_from_dump($handle) {

  $l = array();
  $pattern = '/^\*\*\*\* List:(\d*):(.*):#Row=(\d*):#Contact=(\d*):#Company=(\d*)(.*)$/';

  $line = fgets($handle);
  while ((! feof($handle)) && (! preg_match($pattern, $line, $matches))) {
    $line = fgets($handle);
  }

  if (feof($handle)) {
    return false;
  }

  $l["id"] = $matches[1];
  $l["name"] = $matches[2];
  $l["nb_row"] = $matches[3];
  $l["nb_contact"] = $matches[4];
  $l["nb_company"] = $matches[5];
  $l["cids"] = array();

  // get contact lines
  $line = fgets($handle);
  $matches = array();
  while (preg_match('/^C:(.*)$/', $line, $matches)) {
    $cid = $matches[1];
    $l["cids"][] = $cid;
    $line = fgets($handle);
  }

  return $l;
}


///////////////////////////////////////////////////////////////////////////////
// Recuperation de la list d'id donné dans le fichier donne
// Parameters:
//   - $handle  : identifiant du fichier
//   - $list_id : id de la liste
// Returns:
// $l hash with list infos [name], [id], [nb_contact], [nb_company], []
///////////////////////////////////////////////////////////////////////////////
function get_list_from_dump_id($handle, $list_id) {

  $l = array();

  $pattern = '/^\*\*\*\* List:(\d*):(.*):#Row=(\d*):#Contact=(\d*):#Company=(\d*)(.*)$/';

  $line = fgets($handle);
  while ((! feof($handle)) && (! preg_match($pattern, $line, $matches))) {
    $line = fgets($handle);
  }

  if (feof($handle)) {
    // we have reach end of file, rewind and start from the beginning
    // cause we have not started from beginning
    fseek($handle, 0);
    $stop = false;
    $line = fgets($handle);
    while ((! feof($handle)) && (! $stop)) {
      preg_match($pattern, $line, $matches);
      if ($list_id == $matches[1]) {
	$stop = true;
      } else {
	$line = fgets($handle);
      }
    }

    // if we reach end of file again, exit
    if (feof($handle)) {
      return false;
    }
  }

  $l["id"] = $matches[1];
  $l["name"] = $matches[2];
  $l["nb_row"] = $matches[3];
  $l["nb_contact"] = $matches[4];
  $l["nb_company"] = $matches[5];
  $l["cids"] = array();

  // get contact lines
  $line = fgets($handle);
  $matches = array();
  while (preg_match('/^C:(.*)$/', $line, $matches)) {
    $cid = $matches[1];
    $l["cids"][] = $cid;
    $line = fgets($handle);
  }

  //  print_r($l);
  //  echo "\n".$l["name"];
  return $l;
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
  $row_ids = array();
  while ($l_q->next_record()) {
    $cid = $l_q->f("contact_id");
    $compid = $l_q->f("company_id");
    if (! in_array($cid, $contact_ids)) {
      $contact_ids[] = $cid;
    }
    if (! in_array($compid, $company_ids)) {
      $company_ids[] = $compid;
    }
    $row_ids[] = $cid;

    // write the row info in the result file (which can have contact doublons)
    $to_write .= "\nC:$cid";
  }
  $nb_con = count($contact_ids);
  $nb_comp = count($company_ids);
  $nb_row = count($row_ids);

  $to_write = "\n**** List:$id:$name:#Row=$nb_row:#Contact=$nb_con:#Company=$nb_comp" . $to_write; 
  $to_write .= "\n"; 

  // write file infos
  fwrite($handle, "$to_write"); 

  $txt = "\nRow : $nb_row";
  $txt .= "\nContact : $nb_con";
  $txt .= "\nCompany : $nb_comp";
  $txt .= "\n";
  $log .= $txt;
  echo $txt;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing
///////////////////////////////////////////////////////////////////////////////
function parse_arg($argv) {
  global $debug;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      echo "Usage: php test-list.php -a dump\n";
      echo "Usage: php test-list.php -v verbose -a test -f file1 -t file2\n";
      echo "-a [dump | test]\n";
      echo "-v [normal | verbose | detail | all]\n\n";
      return true;
      break;
    case '-a':
    case '--action':
      list($nb2, $val2) = each ($argv);
      $params["action"] = $val2;
      if ($debug > 0) { echo "-f -> \$action=$val2\n"; }
      break;
    case '-f':
    case '--from-file':
      list($nb3, $val3) = each ($argv);
      $params["from_file"] = $val3;
      if ($debug > 0) { echo "-f -> \$from_file=$val3\n"; }
      break;
    case '-t':
    case '--to-file':
      list($nb4, $val4) = each ($argv);
      $params["to_file"] = $val4;
      if ($debug > 0) { echo "-t -> \$to_file=$val4\n"; }
      break;
    case '-v':
    case '--verbose':
      list($nb5, $val5) = each ($argv);
      $params["verbose"] = $val5;
      if ($debug > 0) { echo "-v -> \$verbose=$val5\n"; }
      break;
    }
  }

  if ($params["action"] == "") {
    $params["action"] = "dump";
  }

  return $params;
}


</script>
