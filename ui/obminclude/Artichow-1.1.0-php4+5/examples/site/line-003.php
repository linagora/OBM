<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";

$graph = new Graph(280, 200);

$x = array();
for($i = 115; $i < 115 + 180; $i++) {
	$x[] = cos($i / 25);
}

function format($value) {
	return sprintf("%.1f", $value).' %';
}

$plot = new LinePlot($x);

$plot->setBackgroundColor(
	new Color(240, 240, 240)
);

$plot->setPadding(40, 15, 15, 15);

$plot->setColor(
	new Color(60, 60, 150)
);

$plot->setFillColor(
	new Color(120, 175, 80, 47)
);

$plot->grid->setType(LINE_DASHED); 

$plot->yAxis->setLabelNumber(6);
$plot->yAxis->setLabelPrecision(1);
$plot->yAxis->setNumberByTick('minor', 'major', 1);
$plot->yAxis->label->setCallbackFunction('format');
$plot->yAxis->label->setFont(new Tuffy(7));

$plot->xAxis->setNumberByTick('minor', 'major', 3);
$plot->xAxis->label->hideFirst(TRUE);
$plot->xAxis->setLabelInterval(50);
$plot->xAxis->label->setFont(new Tuffy(7));

$plot->grid->setInterval(1, 50);

$graph->shadow->setSize(4);
$graph->shadow->setPosition(SHADOW_RIGHT_BOTTOM);
$graph->shadow->smooth(TRUE);

$plot->label->set($x);
$plot->label->setInterval(25);
$plot->label->hideFirst(TRUE);
$plot->label->setPadding(1, 1, 1, 1);
$plot->label->setCallbackFunction('format');
$plot->label->setBackgroundColor(
	new Color(227, 223, 241, 15)
);
$plot->label->setFont(new Tuffy(7));

$graph->add($plot);
$graph->draw();
?>