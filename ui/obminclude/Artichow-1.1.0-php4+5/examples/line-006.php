<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";


// Use cache
$graph = new Graph(400, 400, "Example-006", time() + 5);
$graph->setTiming(TRUE);
$graph->setAntiAliasing(TRUE);


$x = array();
for($i = 0; $i < 10; $i++) {
	$x[] = mt_rand(0, 100);
}

$plot = new LinePlot($x);
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

$plot->setYMin(-5);

$plot->yAxis->setLabelNumber(8);
$plot->yAxis->setLabelPrecision(1);

$plot->xAxis->setNumberByTick('minor', 'major', 3);

$plot->setXAxisZero(TRUE);

$graph->add($plot);
$graph->draw();
?>