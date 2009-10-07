<?php

require_once "../../Graph.class.php";

$graph = new Graph(400, 30);

$graph->border->hide();

$driver = $graph->getDriver();

$driver->filledRectangle(
	new LinearGradient(
		new Black,
		new White,
		0
	),
	new Line(
		new Point(0, 0),
		new Point(400, 30)
	)
);

$graph->draw();

?>
