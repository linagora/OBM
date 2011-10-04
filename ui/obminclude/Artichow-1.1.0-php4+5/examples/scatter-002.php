<?php

require_once "../ScatterPlot.class.php";

$graph = new Graph(400, 400);

$graph->title->set('Linked ScatterPlot');

$y = array(1, 10, 3,-4, 1, 4, 8, 7);
$x = array(0.5, 0.5, 3, 5, 2, 3, 4, 1.5);

$plot = new ScatterPlot($y, $x);
$plot->setBackgroundColor(new VeryLightGray);
$plot->setPadding(NULL, NULL, 40, 20);

$plot->link(TRUE, new DarkBlue);

$graph->add($plot);
$graph->draw();

?>