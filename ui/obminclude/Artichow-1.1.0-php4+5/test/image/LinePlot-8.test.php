<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

function color($a = NULL) {
	if($a === NULL) {
		$a = mt_rand(20, 100);
	}
	return new Color(mt_rand(20, 180), mt_rand(20, 180), mt_rand(20, 180), $a);
}

$graph = new Graph();
$graph->setTiming(TRUE);
$graph->setSize(400, 400);
$graph->setAntiAliasing(TRUE);


$x = array();

for($i = 0; $i < 10; $i++) {
	$x[] = mt_rand(-20, 100);
}

$plot = new LinePlot($x);
$plot->setBackgroundColor(color(80));
$plot->setColor(color());
$plot->setFillGradient(new LinearGradient(color(), color(), 90));

$plot->grid->hideVertical(TRUE);

$plot->setYMin(mt_rand(-20, -30));

$plot->yAxis->setLabelNumber(mt_rand(0, 10));
$plot->yAxis->setLabelPrecision(1);

$plot->xAxis->label->hideFirst(TRUE);
$plot->xAxis->setNumberByTick('minor', 'major', 2);

$plot->setXAxisZero((bool)mt_rand(0, 1));

$graph->add($plot);
$graph->draw();
?>