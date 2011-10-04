<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph();
$graph->setTiming(TRUE);

function color($a = NULL) {
	if($a === NULL) {
		$a = mt_rand(0, 100);
	}
	return new Color(mt_rand(0, 255), mt_rand(0, 255), mt_rand(0, 255), $a);
}

$graph->setSize(400, 400);
$graph->setFormat(IMAGE_PNG);
$graph->setBackgroundColor(new Color(150, 150, 150));

for($i = 0; $i <= 1; $i++) {

	$x = array();
	
	for($j = 0, $count = mt_rand(1, 8); $j <= $count; $j++) {
		$x[] = mt_rand(-100, 100) / 10;
	}

	$plot = new LinePlot($x);

	$plot->title->set("Component #".$i);
	
	if(mt_rand(0, 1) === 0) {
		$plot->setBackgroundGradient(new LinearGradient(color(), color(), mt_rand(0, 1) * 90));
	} else {
		$plot->setBackgroundColor(color());
	}
	
	
	$mark = mt_rand(1, 2);
	
	$plot->mark->setType($mark);
	$plot->mark->setSize(mt_rand(3, 16));
	$plot->mark->border->show();

	if($mark === 1 or mt_rand(0, 1) === 1) {
		$plot->mark->setFill(color());
	} else {
		$plot->mark->setFill(new LinearGradient(color(), color(), 0));
	}
	
	$plot->mark->border->setColor(color());
		
	$plot->setSize(0.33 + $i / 2 + mt_rand(0, 15) / 100, 0.33 + mt_rand(0, 15) / 100);
	$plot->setCenter(0.25 + $i / 4 + (1 - $i) * mt_rand(0, 40) / 100, 0.25 + $i / 2 - $i * mt_rand(0, 10) / 100);
	
	$plot->grid->setColor(new Color(160, 200 * ($i - 1), 42 * $i));
	$plot->grid->hide(FALSE);
	$plot->grid->setBackgroundColor(new Color(235 - 20 * $i, 235 + 20 * $i, 180, 50));
	
	$plot->setYAxis(mt_rand(0, 1) ? PLOT_RIGHT : PLOT_LEFT);
	$plot->setXAxis(mt_rand(0, 1) ? PLOT_TOP : PLOT_BOTTOM);
	$plot->setPadding(NULL, NULL, 40, NULL);
	
	$plot->yAxis->label->hideFirst((bool)$i);
	$plot->yAxis->setLabelPrecision(2 - $i);
	$plot->yAxis->setColor(new Color(124 / ($i+1), 50, 20));
	$plot->yAxis->setNumberByTick('minor', 'major', $i * 2);
	$plot->yAxis->label->setFont(new Font3);
	$plot->yAxis->label->setAngle($i ? 90 : 0);
	$plot->yAxis->label->setColor(new Color(124 / ($i+1), 20, 200));
	
	
	$plot->xAxis->setNumberByTick('minor', 'major', 2 + $i);
	$plot->xAxis->setLabelInterval(2 - $i);
	$plot->xAxis->label->setColor(color(0));
	$plot->xAxis->label->setBackgroundColor(new Color(255, 255, 255, 10));
	$plot->xAxis->label->border->setColor(new Color(0, 0, 0, 10));
	$plot->xAxis->label->setPadding(2, 0, -1, -1);
	
	$graph->add($plot);
	
}

$graph->draw();
?>