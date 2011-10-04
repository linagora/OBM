<?php
require_once "../../LinePlot.class.php";

$graph = new Graph(400, 400);

$graph->setAntiAliasing(FALSE);

$values = array(1, 4, 5, -2.5, 3);
$plot = new LinePlot($values);
$plot->setBackgroundGradient(
	new LinearGradient(
		new Color(210, 210, 210),
		new Color(250, 250, 250),
		0
	)
);
$plot->yAxis->setLabelPrecision(1);
$plot->setSpace(5, 5, NULL, NULL);

$graph->add($plot);
$graph->draw();
?>
