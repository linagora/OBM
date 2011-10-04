<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

$graph = new Graph(150, 100);

$graph->setAntiAliasing(TRUE);

$x = array();
for($i = 0; $i < 10; $i++) {
	$x[] = mt_rand(1, 99) / 10;
}

$plot = new LinePlot($x);
$plot->setBackgroundColor(new Color(240, 240, 240));
$plot->setPadding(30, 8, 8, 20);

$plot->setColor(
	new Color(60, 60, 150)
);
$plot->setFillGradient(
	new LinearGradient(
		new Color(120, 175, 80, 47),
		new Color(231, 172, 113, 30),
		0
	)
);

$plot->grid->setType(LINE_DASHED); 

$plot->yAxis->setLabelNumber(2);
$plot->yAxis->setLabelPrecision(1);

$plot->xAxis->setLabelInterval(2);
$plot->xAxis->setNumberByTick('minor', 'major', 2);

$graph->add($plot);
$graph->draw();
?>