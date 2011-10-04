<?php

require_once "../../LinePlot.class.php";

$graph = new Graph();
$graph->setSize(400, 400);

$x = array(1, 4, 3, -1, 1);

$plot = new LinePlot($x);
$plot->setXAxisZero(FALSE);
$plot->setFillGradient(
	new LinearGradient(
		new Color(255, 20, 20, 30),
		new Color(20, 255, 20, 30),
		0
	)
);

$graph->shadow->setSize(50);
$graph->shadow->smooth(mt_rand(0, 1) ? TRUE : FALSE);
	
$graph->add($plot);
$graph->draw();

?>