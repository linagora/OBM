<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../Pie.class.php";


$graph = new Graph(400, 250);
$graph->setAntiAliasing(TRUE);

$graph->title->set("Pie (example 18) - Display labels > 10 %");
$graph->title->setFont(new Tuffy(14));

$values = array(1, 5, 6, 16, 18, 19, 21, 3, 4, 7, 6);

$plot = new Pie($values);
$plot->setCenter(0.4, 0.55);
$plot->setAbsSize(180, 180);
$plot->setLabelMinimum(10);

$plot->legend->setPosition(1.5);
$plot->legend->shadow->setSize(0);

$graph->add($plot);
$graph->draw();

?>