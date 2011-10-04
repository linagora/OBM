<?php

require_once "../../ScatterPlot.class.php";

$graph = new Graph(300, 280);

$graph->title->set('Linked ScatterPlot');
$graph->title->setFont(new TuffyItalic(14));
$graph->shadow->setSize(4);

$y = array(1, 10, 7, 8, 5, 4, 2, 4);
$x = array(0.5, 0.5, 1.5, 4, 3, 5, 2, 2);

$plot = new ScatterPlot($y, $x);
$plot->setBackgroundColor(new Color(235, 235, 235));

$plot->mark->setSize(15);
$plot->mark->setFill(
	new RadialGradient(
		new LightGreen,
		new DarkGreen
	)
);

$plot->link(TRUE);
$plot->setColor(new DarkGreen);

$plot->setSpace(6, 6, 6, 0);
$plot->setPadding(25, NULL, 40, 20);

$graph->add($plot);
$graph->draw();

?>