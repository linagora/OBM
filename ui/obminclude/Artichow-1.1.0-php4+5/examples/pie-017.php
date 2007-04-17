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

$graph->title->set("Pie (example 17)");
$graph->title->setFont(new Tuffy(14));

$values = array(12, 16, 13, 18, 10, 20, 11);

$plot = new Pie($values, PIE_AQUA);
$plot->setCenter(0.4, 0.55);
$plot->setAbsSize(180, 180);

$plot->setLegend(array(
	'Mon',
	'Tue',
	'Wed',
	'Thu',
	'Fri',
	'Sat',
	'Sun'
));

$explode = array();
for($i = 0; $i < count($values); $i++) {
	$explode[] = 15;
}

$plot->explode($explode);

$plot->legend->setPosition(1.5);
$plot->legend->shadow->setSize(0);

$graph->add($plot);
$graph->draw();

?>