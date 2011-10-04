<?php

require_once "../../ScatterPlot.class.php";

$graph = new Graph(300, 200);

$graph->title->set('Impulses');
$graph->shadow->setSize(4);

$y = array();
for($i = 0; $i < 40; $i++) {
	$y[] = cos($i / 15 * 2 * M_PI) / (0.8 + $i / 15) * 4;
}

$plot = new ScatterPlot($y);
$plot->setPadding(25, 15, 35, 15);
$plot->setBackgroundColor(new Color(230, 230, 255));
$plot->setSpace(2, 2);

// Set impulses
$plot->setImpulse(new DarkBlue);

$plot->grid->hideVertical();
$plot->grid->setType(LINE_DASHED);

// Hide ticks
$plot->xAxis->hideTicks();
$plot->xAxis->label->hide();

$plot->mark->setType(MARK_SQUARE);
$plot->mark->setSize(4);

$graph->add($plot);
$graph->draw();

?>