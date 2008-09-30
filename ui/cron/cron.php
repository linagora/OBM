<?php

$path = pathinfo(__FILE__);
$path = $path['dirname'];
$includePath = realpath("$path/..");
$jobsPath = "$path/jobs/";

ini_set('error_reporting', E_ALL & ~E_NOTICE);
ini_set('include_path', ".:$includePath");

$obminclude = 'obminclude';
include_once("$obminclude/global.inc");
include_once("Logger.class.php");
include_once("Cron.class.php");

define("L_LEVEL",L_DEBUG);

set_error_handler('errorHandler');


$cron = new Cron($jobsPath);
$cron->process();
?>
