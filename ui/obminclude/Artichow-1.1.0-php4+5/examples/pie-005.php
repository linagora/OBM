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
$graph->setBackgroundGradient(
	new LinearGradient(
		new VeryLightGray,
		new White,
		0
	)
);
$graph->title->set("Pie (example 5) - Initial angle: 140°");

$values = array(8, 4, 6, 2, 5, 3, 4);

$plot = new Pie($values);
$plot->setCenter(0.4, 0.55);
$plot->setSize(0.7, 0.6);
$plot->set3D(10);
$plot->setStartAngle(140);

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
$plot->legend->setBackgroundColor(new VeryLightGray(30));

$graph->add($plot);
$graph->draw();

?>