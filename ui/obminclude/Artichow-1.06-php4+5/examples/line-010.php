<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";

$graph = new Graph(375, 200);

// Set title
$graph->title->set('Star marks');
$graph->title->setFont(new Tuffy(12));
$graph->title->setColor(new DarkRed);

$plot = new LinePlot(array(5, 3, 4, 7, 6, 5, 8, 4, 7));

// Change plot size and position
$plot->setSize(0.76, 1);
$plot->setCenter(0.38, 0.5);

$plot->setPadding(30, 15, 38, 25);
$plot->setColor(new Orange());
$plot->setFillColor(new LightOrange(80));

// Change grid style
$plot->grid->setType(LINE_DASHED);

// Add customized  marks
$plot->mark->setType(MARK_STAR);
$plot->mark->setFill(new MidRed);
$plot->mark->setSize(6);

// Change legend
$plot->legend->setPosition(1, 0.5);
$plot->legend->setAlign(LEGEND_LEFT);
$plot->legend->shadow->smooth(TRUE);

$plot->legend->add($plot, 'My line', LEGEND_MARK);

$graph->add($plot);
$graph->draw();
?>