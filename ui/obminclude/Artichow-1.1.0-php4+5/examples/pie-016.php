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

$graph->title->set("Pie (example 13) - Adjusting labels");

$values = array(16, 9, 13, 23, 10);

$plot = new Pie($values, PIE_EARTH);
$plot->setCenter(0.4, 0.55);
$plot->setAbsSize(220, 220);

$plot->setLegend(array(
	'Mon',
	'Tue',
	'Wed',
	'Thu',
	'Fri',
	'Sat',
	'Sun'
));

$plot->setLabelPosition(-40);
$plot->label->setPadding(2, 2, 2, 2);
$plot->label->setFont(new Tuffy(7));
$plot->label->setBackgroundColor(new White(60));

$plot->legend->setPosition(1.3);
$plot->legend->shadow->setSize(0);

$graph->add($plot);
$graph->draw();

?>