<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph(300, 175);

$graph->setAntiAliasing(TRUE);

$x = array(
	3, 1, 5, 6, 3, 8, 6
);

$plot = new LinePlot($x);

$plot->grid->setNoBackground();

$plot->title->set("Filled line and marks");
$plot->title->setFont(new Tuffy(10));
$plot->title->setBackgroundColor(new Color(255, 255, 255, 25));
$plot->title->border->show();
$plot->title->setPadding(3, 3, 3, 3);
$plot->title->move(-20, 25);

$plot->setSpace(4, 4, 10, 0);
$plot->setPadding(25, 15, 10, 18);

$plot->setBackgroundGradient(
	new LinearGradient(
		new Color(210, 210, 210),
		new Color(255, 255, 255),
		0
	)
);

$plot->setColor(new Color(0, 0, 150, 20));

$plot->setFillGradient(
	new LinearGradient(
		new Color(150, 150, 210),
		new Color(245, 245, 245),
		0
	)
);

$plot->mark->setType(MARK_CIRCLE);
$plot->mark->border->show();

$y = array(
	'Lundi',
	'Mardi',
	'Mercredi',
	'Jeudi',
	'Vendredi',
	'Samedi',
	'Dimanche'
);

$plot->xAxis->setLabelText($y);
$plot->xAxis->label->setFont(new Tuffy(7));

$graph->add($plot);
$graph->draw();
?>