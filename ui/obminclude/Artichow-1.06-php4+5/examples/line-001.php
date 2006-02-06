<?php

require_once "../LinePlot.class.php";

$graph = new Graph(400, 400);

$x = array(1, 10, 3,-4, 1);

$plot = new LinePlot($x);
$plot->setSpace(6, 6, 10, 10);

$plot->hideLine(TRUE);
$plot->setFillColor(new Color(180, 180, 180, 75));

$plot->mark->setType(MARK_IMAGE);
$plot->mark->setImage(new FileImage("champignon.png"));

$plot->grid->setBackgroundColor(new Color(235, 235, 180, 60));

$plot->label->set($x);
$plot->label->move(0, -23);
$plot->label->setBackgroundGradient(new LinearGradient(new Color(250, 250, 250, 10), new Color(255, 200, 200, 30), 0));
$plot->label->border->setColor(new Color(20, 20, 20, 20));
$plot->label->setPadding(3, 1, 1, 0);
	
$graph->add($plot);
$graph->draw();

?>