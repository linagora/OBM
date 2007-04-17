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

$graph = new Graph(500, 400);
$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setSpace(5, 10, 15, 15);
$group->setPadding(40, 40, 25, 25);

$group->axis->left->setLabelPrecision(2);

for($n = 0; $n < 4; $n++) {

	$x = array();
	
	for($i = 0; $i < 6; $i++) {
		$x[] = (cos($i * M_PI / 100) / ($n + 1) * mt_rand(600, 1400) / 1000 - 0.5) + 1;
	}
	
	$plot = new BarPlot($x, 1, 1, (3 - $n) * 7);
	$plot->barBorder->setColor(color());
	
	$plot->barShadow->setSize(3);
	$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
	$plot->barShadow->setColor(new Color(180, 180, 180, 10));
	$plot->barShadow->smooth(TRUE);

	$plot->setBarColor(color(5));
	
	$group->add($plot);
	
}

$graph->add($group);
$graph->draw();
?>