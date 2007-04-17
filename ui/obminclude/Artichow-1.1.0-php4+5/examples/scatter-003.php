<?php

require_once "../ScatterPlot.class.php";

$graph = new Graph(400, 400);

$graph->shadow->setSize(5);
$graph->title->set('ScatterPlot with values');

$y = array(4, 3, 2, 5, 8, 1, 3, 6, 4, 5);
$x = array(1, 2, 5, 4, 3, 6, 2, 4, 5, 1);

$plot = new ScatterPlot($y, $x);
$plot->setSpace(6, 6, 6, 0);
$plot->setPadding(NULL, NULL, 40, 20);

// Set dashed lines on the grid
$plot->grid->setType(LINE_DASHED);

$plot->mark->setSize(30);
$plot->mark->setFill(new DarkOrange(20));


$plot->label->set($y);
$plot->label->setColor(new White);

$graph->add($plot);
$graph->draw();

?>