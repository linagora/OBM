<?php

require_once "../ScatterPlot.class.php";

$graph = new Graph(400, 400);

$group = new PlotGroup;

$group->setBackgroundColor(new VeryLightGray);
$group->setPadding(30, 30, 30, 30);
$group->setSpace(5, 5, 5, 5);

$group->legend->setPosition(0.5, 0.62);
$group->legend->setAlign(LEGEND_CENTER, LEGEND_MIDDLE);

function getCircle($size) {

	$center = 0;
	
	$x = array();
	$y = array();
	
	for($i = 0; $i <= 30; $i++) {
		$rad = ($i / 30) * 2 * M_PI;
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