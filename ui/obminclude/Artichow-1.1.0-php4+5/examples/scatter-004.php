<?php

require_once "../ScatterPlot.class.php";

$graph = new Graph(400, 400);

$graph->shadow->setSize(5);

$y = array();
for($i = 0; $i < 60; $i++) {
	$y[] = cos($i / 30 * 2 * M_PI);
}

$plot = new ScatterPlot($y);
$plot->setSpace(6, 6);

// Set impulses
$plot->setImpulse(new DarkGreen);

$plot->grid->hideVertical();

// Hide axis labels and ticks
$plot->xAxis->label->hide();
$plot->xAxis->hideTicks();

$plot->mark->setType(MARK_SQUARE);
$plot->mark->setSize(4);

$graph->add($plot);
$graph->draw();

?>