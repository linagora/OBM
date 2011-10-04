<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../Pie.class.php";


$graph = new Graph(300, 175);
$graph->setBackgroundGradient(
	new LinearGradient(
		new White,
		new VeryLightGray(40),
		0
	)
);
$graph->title->set("Horses");
$graph->shadow->setSize(5);
$graph->shadow->smooth(TRUE);
$graph->shadow->setPosition(SHADOW_LEFT_BOTTOM);
$graph->shadow->setColor(new DarkGray);

$values = array(8, 4, 6, 2, 5);

$plot = new Pie($values);
$plot->setCenter(0.35, 0.55);
$plot->setSize(0.7, 0.6);
$plot->set3D(10);
$plot->setLabelPosition(10);

$plot->setLegend(array(
	'France',
	'Spain',
	'Italy',
	'Germany',
	'England'
));

$plot->legend->setPosition(1.40);
$plot->legend->shadow->setSize(0);
$plot->legend->setBackgroundColor(new VeryLightGray(30));

$graph->add($plot);
$graph->draw();

?>