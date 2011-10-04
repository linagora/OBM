<?php
require_once "../../Graph.class.php";

$graph = new Graph(250, 250);

$graph->border->hide();

$driver = $graph->getDriver();

$start = new Color(125, 250, 0);
$end = new Color(0, 125, 125);

// On dessine le dégradé radial dans un cercle
$driver->filledEllipse(
	new RadialGradient(
		$start,
		$end
	),
	new Point(125, 125),
	250, 250
);

$graph->draw();
?>