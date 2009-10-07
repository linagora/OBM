<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../AntiSpam.class.php";

$object = new AntiSpam();

if($object->check('example', $_GET['code'])) {
	echo "Good value :-)";
} else {
	echo "Bad value :-(";
}
?>
<ul>
	<li><a href='AntiSpam.php'>Try again</a></li>
</ul>