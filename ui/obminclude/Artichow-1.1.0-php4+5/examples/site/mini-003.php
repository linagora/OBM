<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph(150, 100);

$x = array();

for($i = 0; $i < 8; $i++) {
	$x[] = cos($i / 3 * M_PI) + mt_rand(-10, 10) / 40;
}

$plot = new LinePlot($x);
$plot->setPadding(22, 5, 25, 8);

// Hide grid
$plot->grid->setType(LINE_DASHED);

// Change background color
$plot->setBackgroundColor(new Color(240, 240, 240, 50));

// Set Y on both left and rights sides
$plot->setYAxis(PLOT_BOTH);

// Change line properties
$plot->setColor(new Color(0, 0, 0));
$plot->setFillColor(new Color(240, 190, 130, 50));

// Chenge ticks and labels interval
$plot->xAxis->setTickInterval(2);
$plot->xAxis->label->hide(TRUE);
$plot->xAxis->setNumberByTick('minor', 'major', 1);

// Hide first and last values on X axis
$plot->xAxis->label->hideFirst(TRUE);
$plot->xAxis->label->hideLast(TRUE);

// Add a title
$plot->title->set("Random values");
$plot->title->move(0, 2);
$plot->title->setFont(new Tuffy(8));
$plot->title->setBackgroundColor(new Color(255, 255, 255, 25));
$plot->title->border->show();
$plot->title->setPadding(2, 2, 2, 2);

$graph->add($plot);
$graph->draw();
?>