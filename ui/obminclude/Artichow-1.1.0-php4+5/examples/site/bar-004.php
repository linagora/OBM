<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../BarPlot.class.php";

function labelFormat($value) {
	return round($value, 2);
}

$graph = new Graph(280, 200);

$graph->setAntiAliasing(TRUE);

$group = new PlotGroup;
$group->setSpace(5, 5, 15, 0);
$group->setPadding(40, 40);

$group->axis->left->setLabelPrecision(2);
$group->axis->right->setLabelPrecision(2);

$colors = array(
	new Color(80, 105, 190, 10),
	new Color(105, 190, 80, 10)
);

$darkColor = array(
	new Color(40, 55, 120, 10),
	new Color(55, 120, 40, 10)
);

$axis = array(
	PLOT_LEFT,
	PLOT_RIGHT
);

$group->axis->left->setColor($darkColor[0]);
$group->axis->left->label->setColor($darkColor[0]);
$group->axis->right->setColor($darkColor[1]);
$group->axis->right->label->setColor($darkColor[1]);

$group->setBackgroundGradient(
	new LinearGradient(
		new Color(225, 225, 225),
		new Color(255, 255, 255),
		0
	)
);

for($n = 0; $n < 2; $n++) {

	$x = array();
	
	for($i = 0; $i < 4; $i++) {
		$x[] = (cos($i * M_PI / 100) / ($n + 1) * mt_rand(700, 1300) / 1000 - 0.5) * (($n%2) ? -0.5 : 1) + (($n%2) ? -0.4 : 0) + 1;
	}
	
	$plot = new BarPlot($x, $n+1, 2);
	$plot->barBorder->setColor(new Color(0, 0, 0, 30));
	
	$plot->setBarPadding(0.1, 0.1);
	$plot->setBarSpace(5);
	
	$plot->barShadow->setSize(3);
	$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
	$plot->barShadow->setColor(new Color(180, 180, 180, 10));
	$plot->barShadow->smooth(TRUE);

	$plot->label->set($x);
	$plot->label->move(0, -6);
	$plot->label->setFont(new Tuffy(7));
	$plot->label->setAngle(90);
	$plot->label->setAlign(NULL, LABEL_TOP);
	$plot->label->setPadding(3, 1, 0, 6);
	$plot->label->setCallbackFunction("labelFormat");

	$plot->setBarColor($colors[$n]);
	
	$plot->setYAxis($axis[$n]);
	
	$group->add($plot);
	
}

$graph->add($group);
$graph->draw();
?>