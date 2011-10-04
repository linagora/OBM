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
		$a = mt_rand(0, 100);
	}
	return new Color(mt_rand(20, 180), mt_rand(20, 180), mt_rand(20, 180), $a);
}

$graph = new Graph(400, 300);
$graph->setBackgroundColor(new Color(175, 175, 175));

$x = array(
	mt_rand(-20, 20),
	42,
	mt_rand(-20, 20),
	15,
	80,
	42,
	42,
	mt_rand(-20, 20)
);

$plot = new LinePlot($x);
$plot->setBackgroundImage(new FileImage("42.png"));
$plot->setAbsSize(350, 250);
$plot->setCenter(0.5, 0.5);
$plot->setThickness(mt_rand(4, 6));
$plot->setFillColor(color());

$plot->grid->setColor(new Color(0, 200, 42));
$plot->grid->hide(FALSE);
$plot->grid->setColor(new Color(255, 25, 160));

$plot->yAxis->label->hideFirst(FALSE);
$plot->yAxis->label->setFont(new Font2);

$plot->xAxis->setNumberByTick('minor', 'major', 2);
$plot->xAxis->label->hideFirst(TRUE);

$graph->add($plot);
$graph->draw();
?>