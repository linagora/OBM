<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

function color() {
	return new Color(mt_rand(0, 255), mt_rand(0, 255), mt_rand(0, 255), mt_rand(0, 100));
}

$graph = new Graph(450, 400);
$graph->title->set('test of LINEPLOT_MIDDLE');
$graph->setAntiAliasing(TRUE);

$values = array();
for($i = 0; $i < 5; $i++) {
	$values[] = mt_rand(4, 20);
}

$group = new PlotGroup;
$group->setSpace(5, 5, 5, 5);
$group->setBackgroundColor(
	new Color(240, 240, 240)
);

$plot = new LinePlot($values, LINEPLOT_MIDDLE);
$plot->setFillColor(color());

$plot->mark->setType(MARK_CIRCLE);
$plot->mark->setSize(mt_rand(1, 20));
$plot->mark->setFill(new Color(255, 255, 255));
$plot->mark->border->show();


$group->add($plot);

$graph->add($group);
$graph->draw();
?>