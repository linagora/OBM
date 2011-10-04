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

$graph = new Graph(300, 200);

$graph->setAntiAliasing(TRUE);
$graph->border->hide();

$group = new PlotGroup;
$group->setSpace(5, 10, 20, 15);
$group->setPadding(40, 10, NULL, 20);
$group->setXAxisZero(FALSE);

$group->axis->left->setLabelPrecision(2);

$colors = array(
	new Color(100, 180, 154, 12),
	new Color(100, 154, 180, 12),
	new Color(154, 100, 180, 12),
	new Color(180, 100, 154, 12)
);

for($n = 0; $n < 4; $n++) {

	$x = array();
	
	for($i = 0; $i < 6; $i++) {
		$x[] = (cos($i * M_PI / 100) / ($n + 1) * mt_rand(600, 1400) / 1000 - 0.5);
	}
	
	$plot = new BarPlot($x, 1, 1, (3 - $n) * 7);
	$plot->barBorder->setColor(new Color(0, 0, 0));
	
	$plot->setBarSize(0.54);
	
	$plot->barShadow->setSize(3);
	$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
	$plot->barShadow->setColor(new Color(160, 160, 160, 10));
	$plot->barShadow->smooth(TRUE);

	$plot->setBarColor($colors[$n]);
	
	$group->add($plot);
	$group->legend->add($plot, "Barre #".$n, LEGEND_BACKGROUND);
	
}

$group->legend->shadow->setSize(0);
$group->legend->setAlign(LEGEND_CENTER);
$group->legend->setSpace(6);
$group->legend->setTextFont(new Tuffy(8));
$group->legend->setPosition(0.50, 0.12);
$group->legend->setBackgroundColor(new Color(255, 255, 255, 25));
$group->legend->setColumns(2);

$graph->add($group);
$graph->draw();
?>