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
function getPerCent($value) {
	return sprintf('%.1f', $value).' %';
}

$graph = new Graph(450, 400);
$graph->setTiming(TRUE);

$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setBackgroundColor(color(80));
$group->setXAxisZero(FALSE);

$group->title->set("Some lines");
$group->title->setBackgroundColor(new Color(255, 255, 255, 25));
$group->title->border->show();
$group->title->setPadding(3, 3, 3, 3);
$group->title->move(0, -15);

$group->setPadding(45, NULL, 45, NULL);


$group->axis->left->setLabelNumber(mt_rand(0, 10));
$group->axis->left->label->setCallbackFunction('getPerCent');

foreach(array('left', 'right') as $axis) {
	$group->axis->{$axis}->setTickStyle(TICK_OUT);;
}

foreach(array('top', 'bottom') as $axis) {
	$group->axis->{$axis}->setTickStyle(TICK_OUT);;
}

// Set axis title
$group->axis->left->title->set("Axis des Y : Mille deux cent quarante-et-un");

$group->axis->bottom->title->set("Axis des X : Quarante-deux plus un");
$group->axis->bottom->title->setBackgroundColor(new Color(255, 255, 255, 25));
$group->axis->bottom->title->setPadding(1, 0, 0, 0);

$group->axis->top->title->set("Axis des X : Treize plus douze");
$group->axis->top->title->setBackgroundColor(new Color(240, 200, 197, 25));
$group->axis->top->title->setPadding(1, 0, 0, 0);

$count = mt_rand(2, 4);

for($n = 0; $n < $count; $n++) {

	$x = array();
	
	for($i = 0; $i < 10; $i++) {
		$x[] = round(cos($i * M_PI / 10) * mt_rand(-20, 100));
	}
	
	$plot = new LinePlot($x);
	$plot->setColor(color());
	$plot->setFillColor(color(90));
	$plot->setXAxis(mt_rand(0, 1) ? PLOT_BOTTOM : PLOT_TOP);
	$plot->setYAxis(mt_rand(0, 1) ? PLOT_LEFT : PLOT_RIGHT);

	$plot->label->set($x);
	$plot->label->setColor(color(0));
	if($n%2 === 0) {
		$plot->label->setBackgroundColor(new Color(mt_rand(220, 240), mt_rand(220, 240), mt_rand(220, 240), mt_rand(15, 35)));
	}
	$plot->label->setPadding(1, 0, 0, 0);
	
	$group->add($plot);
	$group->legend->add($plot, str_repeat("#".($n + 1), mt_rand(1, 2)), ($n%2) ? LEGEND_LINE : LEGEND_BACKGROUND);
	
}

$group->legend->setColumns(2);
$group->legend->border->hide();
$group->legend->setSpace(20);
$group->legend->setBackgroundColor(new Color(245, 255, 255));
$group->setPadding(NULL, 130, NULL, NULL);

$graph->add($group);
$graph->draw();
?>