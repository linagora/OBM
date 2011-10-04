<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../BarPlot.class.php";

function color($a = NULL) {
	if($a === NULL) {
		$a = 0;
	}
	return new Color(mt_rand(20, 180), mt_rand(20, 180), mt_rand(20, 180), $a);
}

function labelFormat($value) {
	return round($value, 2);
}

$graph = new Graph(500, 400);
$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setSpace(5, 5, 15, 15);
$group->setPadding(40, 40, 25, 25);

$group->axis->left->setLabelPrecision(2);

for($n = 0; $n < 4; $n++) {

	$x = array();
	
	for($i = 0; $i < 6; $i++) {
		$x[] = (cos($i * M_PI / 100) / ($n + 1) * mt_rand(700, 1300) / 1000 - 0.5) * (($n%2) ? -0.5 : 1) + (($n%2) ? -0.4 : 0) + 0.3;
	}
	
	$plot = new BarPlot($x, floor($n / 2) + 1, 2, (($n % 2) === 0) * 6);
	$plot->barBorder->setColor(color());
	
	$plot->setBarSpace(12);
	
	$plot->barShadow->setSize(4);
	$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
	$plot->barShadow->setColor(new Color(180, 180, 180, 10));
	$plot->barShadow->smooth(TRUE);

	$plot->label->set($x);
	$plot->label->move(0, -12);
	$plot->label->setFont(new Font1);
	$plot->label->setAngle(90);
	$plot->label->setInterval(2);
	$plot->label->setAlign(NULL, LABEL_TOP);
	$plot->label->setBackgroundGradient(new LinearGradient(new Color(250, 250, 250, 10), new Color(255, 200, 200, 30), 0));
	$plot->label->border->setColor(new Color(20, 20, 20, 20));
	$plot->label->setPadding(3, 1, 1, 0);
	$plot->label->setCallbackFunction("labelFormat");

	$plot->setBarColor(color(5));
	
	$group->add($plot);
	
}

$graph->add($group);
$graph->draw();
?>