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
$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setBackgroundGradient(new LinearGradient(new Color(200, 200, 200), new Color(240, 240, 240), 0));
$group->setPadding(30, NULL, 25, 25);

$group->axis->left->setLabelNumber(10);
$group->axis->left->setLabelPrecision(2);
$group->axis->left->setTickStyle(TICK_OUT);;
$group->axis->left->label->setCallbackFunction('formatLabel');

function formatLabel($value) {
	return $value.' %';
}

foreach(array('top', 'bottom') as $axis) {
	$group->axis->{$axis}->label->hideLast(TRUE);
	$group->axis->{$axis}->label->hideFirst(TRUE);
	$group->axis->{$axis}->setLabelInterval(5);
	$group->axis->{$axis}->setTickStyle(TICK_OUT);;
}

for($n = 0; $n < 4; $n++) {

	$x = array();
	
	for($i = 0; $i < /*100*/50; $i++) {
		$x[] = (cos($i * M_PI / 100) / ($n + 1) * mt_rand(700, 1300) / 1000 - 0.5) * (($n%2) ? -0.5 : 1) + (($n%2) ? -0.4 : 0);
	}
	
	$plot = new LinePlot($x, mt_rand(0, 1) ? LINEPLOT_LINE : LINEPLOT_MIDDLE);
	$plot->setColor(color());

	$plot->setFillGradient(
		new LinearGradient(
			color(60), color(60), 90
		)
	);
	
	$y = array();
	foreach($x as $v) {
		$y[] = sprintf("%.2f", $v);
	}

	$plot->label->set($y);
	$plot->label->setColor(color(0));
	$plot->label->setBackgroundColor(new Color(mt_rand(220, 240), mt_rand(220, 240), mt_rand(220, 240), mt_rand(10, 20)));
	$plot->label->setPadding(1, 0, 0, 0);
	$plot->label->setInterval(12);
	$plot->label->setFont(new Tuffy(6));
	
	
	$plot->setXAxis(PLOT_BOTTOM);
	$plot->setYAxis(PLOT_LEFT);
	
	$group->add($plot);
	$group->legend->add($plot, "Line #".($n + 1));
	
}

$group->legend->setAlign(LEGEND_CENTER, LEGEND_TOP);
$group->legend->setPosition(0.87, 0.1);

$graph->shadow->setSize(mt_rand(10, 20));
$graph->shadow->setPosition(mt_rand(1, 4));
$graph->shadow->smooth(mt_rand(0, 1) ? TRUE : FALSE);

$graph->add($group);
$graph->draw();
?>