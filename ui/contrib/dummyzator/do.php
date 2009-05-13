<?php

/* Get the args */
$nb_users = intval($argv[1]);
$obm_root = $argv[2];

if($nb_users == 0 || !is_dir($obm_root."/conf")) {
  print "Usage: $argv[0] <users number> <dummized obm root dir>\n";
  exit(1);
}
$nb_users = $argv[1];

/* Our files (need $obm_root) */
include('helpers.php');
include('generators.php');

/* Set some globals that OBM's includes need */
$obm = array
( 'uid' => 1 // run_query_domain_init_data() needs this
  );

/* Hack to include obm's includes files */
$path = 'php';                          // dummy value, useless
chdir($obm_root);
$obminclude = "./obminclude";
require("$obminclude/global.inc");


$gens = new DummyGenerators();
$gens->genDummyData($nb_users);

