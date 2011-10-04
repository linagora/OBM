<?php

require_once "../../ScatterPlot.class.php";

$graph = new Graph(300, 240);

$graph->title->set('Simple ScatterPlot');
$graph->shadow->setSize(4);

$y = array(1, 1.3, 1.8, 1.6, 10, 7, 8, 3, 4, 2, 4);
$x = array(0.5, 0.7, 0.65, 0.9, 0.5, 1.5, 4, 3, 5, 2, 2);

$plot = new ScatterPlot($y, $x);
$plot->setBackgroundColor(new Color(255, 245, 220));

$plot->mark->setSize(15);
$plot->mark->setFill(
	new RadialGradient(
		new LightRed,
		new Red
	)
);

$plot->setSpace(6, 6, 6, 0);
$plot->setPadding(25, NULL, 40, 20);

$graph->add($plot);
$graph->draw();

?>