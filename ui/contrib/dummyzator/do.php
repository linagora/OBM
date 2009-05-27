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
  ( 'uid' => 1, // run_query_domain_init_data() needs this
    'domain_name' => 'foo.bar' // genUniqueExtEventId()
  );

/* Hack to include obm's includes files */
$path = 'php';                          // dummy value, useless
chdir($obm_root);
$obminclude = "./obminclude";
require("$obminclude/global.inc");
/*
$event = new Event(0, 3600, 'tit', 'loc', 'Appel tel.', 0, 'desc', 'props', 0, 'daily', 3, 'UiUyS0Yqg FbtgNqME', '');
$start = new Of_Date(time());
$end   = new Of_Date(time()+3600*24*8);
$of = &OccurrenceFactory::getInstance();
$of->setBegin($start);
$of->setEnd($end);

calendar_daily_repeatition(new Of_Date(time()+3600*24*2), $start, $end,
                           2, $event, 0, 'user', 'ACCEPTED');

//print_r($of->getOccurrences());
foreach($of->getOccurrences() as $oc) {
  print "Event from ".$oc->date." to ".$oc->end." duration ".$oc->event->duration."\n";
}
exit;
*/
$gens = new DummyGenerators();
$gens->genDummyData($nb_users);

