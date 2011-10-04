<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";

$graph = new Graph(400, 300);

$x = array(
	-4, -5, -2, -8, -3, 1, 4, 9, 5, 6, 2
);

$plot = new LinePlot($x);

// Filled an area with a color
$plot->setFilledArea(7, 9, new DarkGreen(25));

// Filled the area with a gradient
$gradient = new LinearGradient(
	new Yellow(25),
	new Orange(25),
	90
);
$plot->setFilledArea(1, 4, $gradient);

// Hide first label
$plot->xAxis->label->hideFirst(TRUE);

$graph->add($plot);
$graph->draw();
?>