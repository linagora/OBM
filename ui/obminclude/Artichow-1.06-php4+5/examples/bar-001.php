<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../BarPlot.class.php";

$graph = new Graph(400, 400);
$graph->title->set('The title');
$graph->border->setStyle(LINE_DASHED);
$graph->border->setColor(new DarkGray);

$values = array(19, 42, 15, -25, 3);

$plot = new BarPlot($values);
$plot->setSize(1, 0.96);
$plot->setCenter(0.5, 0.52);

$plot->setBarColor(
	new VeryLightPurple(25)
);

$graph->add($plot);
$graph->draw();

?>