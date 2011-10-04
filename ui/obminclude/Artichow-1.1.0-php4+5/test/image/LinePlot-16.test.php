<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

$graph = new Graph(450, 400);
$graph->setAntiAliasing(TRUE);

$values = array();
for($i = 0; $i < 15; $i++) {
	$values[] = mt_rand(4, 20);
}

$graph->title->set('Mon graphique');

$plot = new LinePlot($values, LINEPLOT_MIDDLE);
$plot->setFillColor(new Color(0, 200, 0, 75));

$plot->mark->setType(MARK_CIRCLE);
$plot->mark->setSize(8);
$plot->mark->setFill(new Color(255, 255, 255));
$plot->mark->border->show();

$plot->setSpace(5, 5, 5, 5);
$plot->setBackgroundColor(
	new Color(240, 240, 240)
);

$graph->add($plot);
$graph->draw();
?>