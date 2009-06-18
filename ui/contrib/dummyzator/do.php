<?php

/* Get the args */
$nb_users = intval($argv[1]);
$obm_root = $argv[3];
$nb_domain = intval($argv[2]);

if($nb_users == 0 || $nb_domain == 0 || !is_dir($obm_root."/conf")) {
  print "Usage: $argv[0] <users number> <domains number> <dummized obm root dir>\n";
  exit(1);
}

if($nb_users < 6) { // DummyGenerators::createEvents() limitation
  die("You may only specify at least 6 users.\n");
}


if($nb_domain < 1) { // DummyGenerators::createEvents() limitation
  die("You may only specify at least 1 domain.\n");
}

/* Our files (need $obm_root) */
include('helpers.php');
include('generators.php');

/* Set some globals that OBM's includes need */
$obm = array( 'uid' => 1 );// run_query_domain_init_data() needs this

/* Hack to include obm's includes files */
$path = 'php';                          // dummy value, useless
chdir($obm_root);
$obminclude = "./obminclude";
require("$obminclude/global.inc");
for($i = 0; $i < $nb_domain; $i++) {
  $obm['domain_name'] = "foo$i.bar";
  $obm['domain_label'] = "foo".$i."bar";
  $gens = new DummyGenerators();
  $gens->genDummyData($nb_users);
}

