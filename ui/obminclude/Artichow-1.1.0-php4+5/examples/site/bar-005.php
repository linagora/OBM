<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../BarPlot.class.php";

$graph = new Graph(300, 200);

$graph->setAntiAliasing(TRUE);
$graph->border->hide();

$group = new PlotGroup;
$group->grid->hide(TRUE);
$group->setSpace(2, 2, 20, 0);
$group->setPadding(30, 10, NULL, NULL);

$colors = array(
	new Orange(25),
	new LightBlue(10)
);

for($n = 0; $n < 2; $n++) {

	$x = array();
	
	for($i = 0; $i < 3 - $n * 3; $i++) {
		$x[] = NULL;
	}
	
	for($i = 3 - ($n * 3); $i < 12 - ($n * 3); $i++) {
		$x[] = cos($i * M_PI / 100) * mt_rand(800, 1200) / 1000 * (((1 - $n) * 5 + 10) / 10);
	}
	
	for($i = 0; $i < $n * 3; $i++) {
		$x[] = NULL;
	}
	
	$plot = new BarPlot($x, 1, 1, (1 - $n) * 6);
	
//	$plot->setBarPadding(2, 2);
	
	$plot->barShadow->setSize(2);
	$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
	$plot->barShadow->setColor(new Color(160, 160, 160, 10));
	$plot->barShadow->smooth(TRUE);

	$plot->setBarColor($colors[$n]);
	
	$group->add($plot);
	$group->legend->add($plot, $n + date('Y'), LEGEND_BACKGROUND);
	
}

function setPc($value) {
	return round($value * 10).'%';
}

$group->axis->left->label->setCallbackFunction('setPc');

$months = array(
	"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
);

$group->axis->bottom->setLabelText($months);
$group->axis->bottom->hideTicks(TRUE);

$group->legend->shadow->setSize(0);
$group->legend->setAlign(LEGEND_CENTER);
$group->legend->setSpace(6);
$group->legend->setTextFont(new Tuffy(8));
$group->legend->setPosition(0.50, 0.10);
$group->legend->setBackgroundColor(new Color(255, 255, 255, 25));
$group->legend->setColumns(2);

$graph->add($group);
$graph->draw();
?>