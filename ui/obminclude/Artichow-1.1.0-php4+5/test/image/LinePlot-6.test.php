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
$graph->setAntiAliasing(TRUE);


$x = array();
$n = mt_rand(5, 5);

for($i = 0; $i < $n; $i++) {
	$x[] = mt_rand(-20, 100);
}

$plot = new LinePlot($x);
$plot->setPadding(40, 40, NULL, NULL);
$plot->setBackgroundColor(color(80));
$plot->setYAxis(PLOT_BOTH);
$plot->setColor(color());
$plot->setFillColor(color(90));

$plot->yAxis->setLabelNumber(mt_rand(0, 10));
$plot->yAxis->setLabelPrecision(1);

$plot->xAxis->label->hideLast(TRUE);

$plot->label->set($x);
$plot->label->setInterval(mt_rand(1, 5));
$plot->label->setColor(color(0));
$plot->label->setBackgroundColor(new Color(mt_rand(180, 220), mt_rand(180, 220), mt_rand(180, 220), mt_rand(25, 35)));
$plot->label->border->setColor(color());
$plot->label->setPadding(1, 0, 0, 0);
$plot->label->setAngle(mt_rand(0, 1) ? 0 : 90);

$graph->add($plot);
$graph->draw();
?>