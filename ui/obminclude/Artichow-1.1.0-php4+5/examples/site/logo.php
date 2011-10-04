<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph(500, 100);

$graph->setAntiAliasing(TRUE);
$graph->border->hide();

$x = array();
for($i = 0; $i < 20; $i++) {
	$x[] = mt_rand(4, 12);
}

$plot = new LinePlot($x);

$plot->setSpace(0, 0, 50, 0);
$plot->setPadding(3, 3, 3, 3);

$plot->setBackgroundGradient(
	new LinearGradient(
		new Color(230, 230, 230),
		new Color(255, 255, 255),
		0
	)
);

$plot->setColor(new Color(0, 0, 180, 20));

$plot->setFillGradient(
	new LinearGradient(
		new Color(220, 220, 230, 25),
		new Color(240, 240, 255, 25),
		90
	)
);

$plot->xAxis->hide(TRUE);
$plot->yAxis->hide(TRUE);

$graph->add($plot);
$graph->draw();
?>