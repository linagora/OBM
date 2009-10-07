<?php

require_once "../../ScatterPlot.class.php";

$graph = new Graph(280, 280);

$graph->title->move(-40, 0);
$graph->title->set('Two circles');

$group = new PlotGroup;
$group->setBackgroundGradient(
	new LinearGradient(
		new VeryLightGray,
		new Color(245, 245, 245),
		0
	)
);

$group->setPadding(25, 20, 40, 15);
$group->setSpace(5, 5, 5, 5);

$group->legend->setPosition(0.82, 0.1);
$group->legend->setAlign(LEGEND_CENTER, LEGEND_MIDDLE);

function getCircle($size) {

	$center = 0;
	
	$x = array();
	$y = array();
	
	for($i = 0; $i <= 20; $i++) {
		$rad = ($i / 20) * 2 * M_PI;
		$x[] = $center + cos($rad) * $size;
		$y[] = $center + sin($rad) * $size;
	}
	
	return array($x, $y);
	
}

list($x, $y) = getCircle(3);

$plot = new ScatterPlot($y, $x);

$plot->link(TRUE, new DarkBlue);

$plot->mark->setFill(new DarkPink);
$plot->mark->setType(MARK_CIRCLE, 6);

$group->legend->add($plot, 'Circle #1', LEGEND_MARK);
$group->add($plot);

list($x, $y) = getCircle(5);

$plot = new ScatterPlot($y, $x);

$plot->link(TRUE, new DarkGreen);

$plot->mark->setFill(new DarkOrange);
$plot->mark->setType(MARK_SQUARE, 4);

$group->legend->add($plot, 'Circle #2', LEGEND_MARK);
$group->add($plot);

$graph->add($group);
$graph->draw();

?>