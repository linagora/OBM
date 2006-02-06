<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../BarPlot.class.php";

$graph = new Graph(280, 240);
$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setSpace(6, 6, 5, 5);
$group->setBackgroundColor(new Color(235, 235, 235));
$group->setPadding(40, 10, 10, 10);

$group->axis->left->setLabelPrecision(2);
$group->axis->bottom->label->hide(TRUE);
$group->axis->bottom->hideTicks(TRUE);

$gradients = array(
	new LinearGradient(
		new Color(30, 30, 160, 10), new Color(120, 120, 160, 10), 0
	),
	new LinearGradient(
		new Color(30, 160, 30, 10), new Color(120, 160, 120, 10), 0
	),
	new LinearGradient(
		new Color(160, 30, 30, 10), new Color(160, 120, 120, 10), 0
	)
);

for($n = 0; $n < 3; $n++) {

	$x = array();
	
	for($i = 0; $i < 6; $i++) {
		$x[] = (cos($i * M_PI / 100) / ($n + 1) * mt_rand(600, 900) / 1000 - 0.5) * (($n%2) ? -0.5 : 1) + (($n%2) ? -0.4 : 0);
	}
	
	$plot = new BarPlot($x, $n + 1, 3);
	$plot->barBorder->hide();
	
	$plot->setXAxis(PLOT_BOTTOM);
	
//	$plot->setBarPadding(3, 3);
	$plot->barShadow->setSize(4);
	$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
	$plot->barShadow->setColor(new Color(255, 255, 255, 10));
	$plot->barShadow->smooth(TRUE);

	$plot->setBarGradient($gradients[$n]);
	
	$group->add($plot);
	
}


$graph->add($group);
$graph->draw();
?>