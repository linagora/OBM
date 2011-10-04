<?php

require_once "../ScatterPlot.class.php";

$graph = new Graph(400, 400);

$graph->title->set('Impulses');
$graph->title->move(0, 30);
$graph->shadow->setSize(5);

$y = array();
for($i = 0; $i < 60; $i++) {
	$y[] = cos($i / 30 * 2 * M_PI) / (1.5 + $i / 15);
}

$plot = new ScatterPlot($y);
$plot->setBackgroundColor(new VeryLightOrange);
$plot->setSpace(5);

// Set impulses
$plot->setImpulse(new DarkBlue);

$plot->grid->hideVertical();

// Hide ticks
$plot->xAxis->hideTicks();

// Change labels interval
$plot->xAxis->label->setInterval(5);

$plot->mark->setType(MARK_SQUARE);
$plot->mark->setSize(4);

$graph->add($plot);
$graph->draw();

?>