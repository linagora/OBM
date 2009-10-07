<?php
require_once "../../LinePlot.class.php";

$graph = new Graph(400, 300);

$graph->setAntiAliasing(TRUE);

$values = array(1, 7, 3, 2.5, 5, -4.5, -5);
$plot = new LinePlot($values);
$plot->setBackgroundColor(new Color(245, 245, 245));

$plot->hideLine(TRUE);
$plot->setFillColor(new Color(180, 180, 180, 75));

$plot->grid->setBackgroundColor(new Color(235, 235, 180, 60));

$plot->yAxis->setLabelPrecision(2);
$plot->yAxis->setLabelNumber(6);

$days = array(
	'Lundi',
	'Mardi',
	'Mercredi',
	'Jeudi',
	'Vendredi',
	'Samedi',
	'Dimanche'
);
$plot->xAxis->setLabelText($days);
	
$plot->setSpace(6, 6, 10, 10);

$plot->mark->setType(MARK_IMAGE);
$plot->mark->setImage(new FileImage("smiley.png"));

$plot->label->set($values);
$plot->label->move(0, -23);
$plot->label->setBackgroundGradient(
	new LinearGradient(
		new Color(250, 250, 250, 10),
		new Color(255, 200, 200, 30),
		0
	)
);
$plot->label->border->setColor(new Color(20, 20, 20, 20));
$plot->label->setPadding(3, 1, 1, 0);

$graph->add($plot);
$graph->draw();
?>
