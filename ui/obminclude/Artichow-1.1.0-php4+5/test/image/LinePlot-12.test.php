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

$group = new PlotGroup;
$group->setBackgroundColor(color(80));
$group->setPadding(NULL, NULL, 25, 25);

$group->title->set("Two filled lines");
$group->title->setFont(new TuffyBold(20));
$group->title->setAlign(NULL, LABEL_CENTER);

$group->grid->setInterval(1, 12);
$group->setXAxisZero(FALSE);

foreach(array('left', 'right') as $axis) {
	$group->axis->{$axis}->setLabelNumber(mt_rand(0, 10));
	$group->axis->{$axis}->setLabelPrecision(1);
}

foreach(array('top', 'bottom') as $axis) {
	$group->axis->{$axis}->label->hideLast(TRUE);
	$group->axis->{$axis}->label->hideFirst(TRUE);
	
	$group->axis->{$axis}->setTickInterval(mt_rand(17, 23));
	$group->axis->{$axis}->setLabelInterval(2);
}

for($n = 0; $n < 2; $n++) {

	$x = array();
	
	for($i = 0; $i < 500; $i++) {
		$x[] = cos($i * M_PI / 500) / ($n + 1) * mt_rand(800, 1200) / 1000;
	}
	
	$plot = new LinePlot($x);
	$plot->setColor(color());
	$plot->setFillColor(color(40));
	$plot->setXAxis(($n%2) ? 'top' : 'bottom');
	$plot->setYAxis(($n%2) ? 'left' : 'right');
	
	$group->add($plot);
	$group->legend->add($plot, "Line #".($n + 1), $n ? LEGEND_LINE : LEGEND_BACKGROUND);
	
}

$group->legend->border->setColor(color(0));
$group->legend->setTextColor(color(0));
$group->legend->setTextFont(new PHPFont(mt_rand(1, 3)));
$group->legend->setPadding(3, 3, 3, 3);
$group->legend->setRows(1);
$group->legend->setAlign(LEGEND_LEFT, LEGEND_BOTTOM);

$group->legend->setPosition(0.16, 0.86);

$graph->add($group);
$graph->draw();
?>