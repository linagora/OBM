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

$graph->title->set("Pie (example 10) - Just a pie");
$graph->title->setFont(new Tuffy(10));

$values = array(8, 4, 6, 1, 2, 3, 4);

$plot = new Pie($values);
$plot->set3D(10);

$plot->legend->hide(TRUE);
$plot->label->hide(TRUE);

$graph->add($plot);
$graph->draw();

?>