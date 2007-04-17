<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../BarPlot.class.php";


$graph = new Graph(280, 200);

$graph->setAntiAliasing(TRUE);

$x = array(
	1, 2, 5, 0.5, 3, 8, 6
);

$plot = new BarPlot($x);

$plot->setSpace(4, 4, 10, 0);
$plot->setPadding(40, 15, 10, 40);

$plot->title->set("Zoé and friends");
$plot->title->setFont(new TuffyBold(11));
$plot->title->border->show();
$plot->title->setBackgroundColor(new Color(255, 255, 255, 25));
$plot->title->setPadding(4, 4, 4, 4);
$plot->title->move(-20, 25);

$plot->yAxis->title->set("Axe des Y");
$plot->yAxis->title->setFont(new TuffyBold(10));
$plot->yAxis->title->move(-4, 0);
$plot->yAxis->setTitleAlignment(LABEL_TOP);

$plot->xAxis->title->set("Axe des X");
$plot->xAxis->title->setFont(new TuffyBold(10));
$plot->xAxis->setTitleAlignment(LABEL_RIGHT);

$plot->setBackgroundGradient(
	new LinearGradient(
		new Color(230, 230, 230),
		new Color(255, 255, 255),
		0
	)
);

$plot->barBorder->setColor(new Color(0, 0, 150, 20));

$plot->setBarGradient(
	new LinearGradient(
		new Color(150, 150, 210, 0),
		new Color(230, 230, 255, 30),
		0
	)
);

$y = array(
	'Zoé',
	'Yvan',
	'Fred',
	'Lucie',
	'Ilia',
	'Nino',
	'Marie'
);

$plot->xAxis->setLabelText($y);
$plot->xAxis->label->setFont(new TuffyBold(7));

$graph->shadow->setSize(4);
$graph->shadow->setPosition(SHADOW_LEFT_TOP);
$graph->shadow->smooth(TRUE);
$graph->shadow->setColor(new Color(160, 160, 160));

$graph->add($plot);
$graph->draw();
?>