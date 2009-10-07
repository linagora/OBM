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

$graph = new Graph(400, 400);
$graph->setTiming(TRUE);


$x = array();

for($i = 0; $i < 10; $i++) {
	$x[] = mt_rand(-20, 100);
}

$plot = new LinePlot($x);
$plot->setPadding(NULL, 40, NULL, NULL);
$plot->setBackgroundColor(color(80));
$plot->setYAxis(PLOT_BOTH);
$plot->setColor(color());

if(mt_rand(0, 2) > 0) {
	$plot->setFillGradient(new LinearGradient(color(40), color(60), mt_rand(0, 1) * 90));
} else {
	$plot->setFillColor(color(64));
}

$plot->yAxis->setLabelNumber(mt_rand(0, 10));
$plot->yAxis->setLabelPrecision(1);
$plot->yAxis->title->set("Axis des Y : Quarante-deux");

$plot->xAxis->setTickInterval(2);
$plot->xAxis->setLabelInterval(2);
$plot->xAxis->label->hideFirst(TRUE);
$plot->xAxis->label->hideLast(TRUE);

$plot->label->set($x);
$plot->label->setInterval(mt_rand(1, 5));
$plot->label->setColor(color(0));
$plot->label->setBackgroundColor(new Color(mt_rand(180, 220), mt_rand(180, 220), mt_rand(180, 220), mt_rand(25, 35)));
$plot->label->border->setColor(color());
$plot->label->setPadding(1, 0, 0, 0);
$plot->label->setAngle(mt_rand(0, 1) ? 0 : 90);

$plot->legend->add($plot, "Plip Plop", LEGEND_MARK);
$plot->legend->setModel(LEGEND_MODEL_BOTTOM);

$graph->shadow->setSize(7);
$graph->shadow->setColor(new Color(0, 50, 150));
$graph->shadow->setPosition(mt_rand(1, 4));
$graph->shadow->smooth(mt_rand(0, 1) ? TRUE : FALSE);

$graph->add($plot);
$graph->draw();
?>