<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../Pie.class.php";


$graph = new Graph(400, 250);
$graph->setAntiAliasing(TRUE);

$graph->title->set("Pie (example 2)");

$values = array(8, 4, 6, 2, 5, 3, 4);

$plot = new Pie($values, PIE_EARTH);
$plot->setCenter(0.4, 0.55);
$plot->setSize(0.7, 0.6);
$plot->set3D(10);
$plot->explode(array(1 => 20, 4 => 26, 0 => 25));

$plot->setLegend(array(
	'Mon',
	'Tue',
	'Wed',
	'Thu',
	'Fri',
	'Sat',
	'Sun'
));

$plot->legend->setPosition(1.3);

$graph->add($plot);
$graph->draw();

?>