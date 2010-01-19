<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
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

define("L_LEVEL", L_INFO);

set_error_handler('errorHandler');

date_default_timezone_set('GMT');

$cron = new Cron($jobsPath);
$cron->process();
?>
