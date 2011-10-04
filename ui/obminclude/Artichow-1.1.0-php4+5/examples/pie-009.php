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

$graph->title->set("Pie (example 9) - User defined colors");
$graph->title->border->show();
$graph->title->setBackgroundColor(new LightRed(60));
$graph->title->setPadding(3, 3, 3, 3);

$values = array(8, 4, 6, 3, 4);
$colors = array(
	new LightOrange,
	new LightPurple,
	new LightBlue,
	new LightRed,
	new LightPink
);

$plot = new Pie($values, $colors);
$plot->setSize(0.70, 0.60);
$plot->setCenter(0.40, 0.55);
$plot->set3D(10);
$plot->setBorderColor(new LightGray);

$plot->setLegend(array(
	'Alpha',
	'Beta',
	'Gamma',
	'Delta',
	'Epsilon'
));

$plot->legend->setPosition(1.30);

$graph->add($plot);
$graph->draw();

?>