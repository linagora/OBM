<?php
require_once "../../../AntiSpam.class.php";

$object = new AntiSpam();

if($object->check('example', $_GET['code'])) {
	echo "Good value :-)";
} else {
	echo "Bad value :-(";
}
?>