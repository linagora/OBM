<?php
require_once "../../Graph.class.php";

$graph = new Graph(250, 250);
$graph->border->hide();

$drawer = $graph->getDrawer();

$debut = new Color(125, 250, 0);
$fin = new Color(0, 125, 125);

// On dessine le dégradé radial dans un cercle
$drawer->filledEllipse(
	new RadialGradient(
		$debut,
		$fin
	),
	new Point(125, 125),
	250, 250
);

$graph->draw();
?>