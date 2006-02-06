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

$graph = new Graph(400, 400);
$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setBackgroundGradient(new LinearGradient(new Color(200, 200, 200), new Color(240, 240, 240), 0));
$group->setPadding(40, 40, 25, 25);

$group->setXAxisZero((bool)mt_rand(0, 1));
if(mt_rand(0, 1)) {
	$group->setYMax(0);
}

$group->axis->left->setLabelPrecision(2);

for($n = 0; $n < 4; $n++) {

	$x = array();
	
	for($i = 0; $i < 5; $i++) {
		$x[] = (cos($i * M_PI / 100) / ($n + 1) * mt_rand(700, 1300) / 1000 - 0.5) - 1;
	}
	
	$plot = new BarPlot($x, $n + 1, 4);
	$plot->barBorder->hide();
	
	$plot->setBarPadding(0.2, 0.2);
	$plot->barShadow->setSize(4);
	$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
	$plot->barShadow->setColor(new Color(255, 255, 255, 20));
	$plot->barShadow->smooth(TRUE);

	$plot->setBarGradient(
		new LinearGradient(
			color(50), color(50), 90
		)
	);
	
	$group->add($plot);
	$group->legend->add($plot, "Line #".($n + 1), LEGEND_BACKGROUND);
	
}

$graph->add($group);
$graph->draw();
?>