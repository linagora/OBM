<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */
 
require_once "../../Graph.class.php";

$graph = new Graph(400, 400);
$graph->setAntiAliasing(FALSE); // TRUE est bogus sa mère
$graph->title->set("Axis");

$driver = $graph->getDriver();

// Horizontal axis
$xAxis = new Axis();
$xAxis->setColor(new MidBlue);
//$xAxis->line->setThickness(2);
$xAxis->setRange(-10, 10);
$xAxis->setPadding(20, 20);
$xAxis->line->setX(10, 390);

$labels = array();
for($i = -10; $i <= 10; $i++) {
	if($i !== 0) {
		$labels[] = $i;
	}
}
$xAxis->label->set($labels);
$xAxis->label->setColor(new DarkGray);
$xAxis->label->setFont(new Tuffy(7));
$xAxis->label->move(0, 8);

$tick = new Tick(0, 2);
$xAxis->addTick('minor', $tick);

$tick = new Tick(21, 5);
$xAxis->addTick('major', $tick);

$xAxis->setNumberByTick('minor', 'major', 3);

// Vertical axis
$yAxis = new Axis();
$yAxis->setColor(new MidBlue);
//$yAxis->line->setThickness(2);
$yAxis->setRange(5, -5);
$yAxis->setPadding(20, 20);
$yAxis->line->setY(50, 240);

$labels = array();
for($i = -5; $i <= 5; $i++) {
	if($i !== 0) {
		$labels[] = $i;
	}
}
$yAxis->label->set($labels);
$yAxis->label->setColor(new DarkGray);
$yAxis->label->setAlign(LABEL_RIGHT);
$yAxis->label->setFont(new Tuffy(7));
$yAxis->label->move(-5, 0);

$tick = new Tick(0, 2);
$yAxis->addTick('minor', $tick);

$tick = new Tick(11, 5);
$yAxis->addTick('major', $tick);

$yAxis->setNumberByTick('minor', 'major', 3);

$xAxis->setYCenter($yAxis, 0);
$yAxis->setXCenter($xAxis, 0);

$xAxis->draw($driver);
$yAxis->draw($driver);

for($x = -10; $x <= 10; $x += 0.1) {

	$p = Axis::toPosition(
		$xAxis, $yAxis,
		new Point($x, cos($x) * 4)
	);
	$driver->point(new Red, $p);
	
}

// Vertical axis
$axis = new Axis();
//$axis->line->setThickness(2);
$axis->setRange(8, -8);
$axis->setPadding(10, 10);
$axis->line->setLocation(
	new Point(20, 260),
	new Point(380, 380)
);

$tick = new Tick(0, 4);
$axis->addTick('minor', $tick);

$tick = new Tick(11, 8);
$axis->addTick('major', $tick);

$axis->setNumberByTick('minor', 'major', 3);

$axis->draw($driver);

$graph->draw();
?>