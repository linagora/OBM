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
		$a = 0;
	}
	return new Color(mt_rand(20, 180), mt_rand(20, 180), mt_rand(20, 180), $a);
}

$graph = new Graph();
$graph->setTiming(TRUE);
$graph->setSize(400, 400);
$graph->setAntiAliasing(TRUE);


$x = array();
$k = array();

for($i = 0; $i < 100; $i++) {
	$x[] = cos($i / 10);
	$k[] = sprintf("%.1f", $i / 10);
}

$plot = new LinePlot($x, $k);
$plot->setBackgroundColor(color(80));
$plot->setYAxis(PLOT_BOTH);
$plot->setColor(color());

$plot->grid->setInterval(mt_rand(1, 4), mt_rand(1, 4));

$plot->yAxis->setLabelNumber(20);
$plot->yAxis->setLabelPrecision(1);

$plot->xAxis->setTickInterval(5);
$plot->xAxis->setLabelInterval(2);
$plot->xAxis->label->hideFirst(TRUE);
$plot->xAxis->label->hideLast(TRUE);
$plot->xAxis->setNumberByTick('minor', 'major', 1);
$plot->xAxis->setLabelText($k);

foreach($x as $k => $v) {
	$x[$k] = sprintf("%.2f", $v);
}



$plot->label->set($x);
$plot->label->setColor(color(0));
$plot->label->setBackgroundColor(new Color(mt_rand(180, 220), mt_rand(180, 220), mt_rand(180, 220), mt_rand(25, 35)));
$plot->label->border->setColor(color());
$plot->label->setPadding(1, 0, 0, 0);
$plot->label->setAngle(0);

$graph->add($plot);
$graph->draw();
?>