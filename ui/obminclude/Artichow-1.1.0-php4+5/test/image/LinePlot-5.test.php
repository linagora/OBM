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

function label($value) {
	return $value.' %';
}

$graph = new Graph(400, 400);
$graph->setTiming(TRUE);
$graph->setAntiAliasing(TRUE);


$x = array();

for($i = 0; $i < 5; $i++) {
	$x[] = mt_rand(-20, 100);
}

$plot = new LinePlot($x);
$plot->setBackgroundColor(color(80));
$plot->setXAxis(PLOT_BOTH);
$plot->setYAxis(PLOT_BOTH);
$plot->setColor(color());
$plot->setStyle(LINE_DOTTED);

$plot->grid->hideHorizontal(TRUE);
$plot->grid->setBackgroundColor(new Color(235, 235, 180, 60));

$plot->yAxis->setLabelNumber(mt_rand(0, 10));
$plot->yAxis->setLabelPrecision(1);

$plot->xAxis->label->hideFirst(TRUE);
$plot->xAxis->label->hideLast(TRUE);

$plot->label->set($x);
$plot->label->move(0, mt_rand(0, 20));
$plot->label->setCallbackFunction('label');
$plot->label->setColor(color(0));
$plot->label->setBackgroundColor(new Color(mt_rand(200, 240), mt_rand(200, 240), mt_rand(200, 240), mt_rand(0, 20)));
$plot->label->border->setColor(color());
$plot->label->setPadding(mt_rand(0, 3), mt_rand(0, 3), mt_rand(0, 3), mt_rand(0, 3));
$plot->label->setAngle(mt_rand(0, 1) ? 0 : 90);

$graph->add($plot);
$graph->draw();
?>