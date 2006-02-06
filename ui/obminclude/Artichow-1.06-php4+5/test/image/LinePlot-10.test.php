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
		$a = mt_rand(20, 100);
	}
	return new Color(mt_rand(20, 180), mt_rand(20, 180), mt_rand(20, 180), $a);
}

function label($text, $font) {

	$label = new Label($text, $font, NULL, 0);
	$label->setBackgroundColor(new Color(255, 255, 255, 25));
	$label->border->show();
	$label->setPadding(1, 0, 0, 0);
	
	return $label;

}

$graph = new Graph(400, 400);
$graph->setAntiAliasing(TRUE);

$graph->title->set("It is a title");
$graph->title->setBackgroundColor(new Color(255, 255, 255, 25));
$graph->title->border->show();
$graph->title->setPadding(3, 3, 3, 3);

$label = label("Artichow", new Font5);
$graph->addLabel($label, 0.5, 0.5);

$label = label("Current timestamp: ".date("Y-m-d H:i:s")."", new Font4);
$label->setAlign(LABEL_RIGHT, LABEL_TOP);
$graph->addAbsLabel($label, new Point(395, 395));

$x = array();

for($i = 0; $i < 6; $i++) {
	$x[] = mt_rand(-100, 100);
}

$plot = new LinePlot($x);

$plot->setBackgroundGradient(new LinearGradient(color(80), color(80), 0));
$plot->setColor(color());
$plot->setFillGradient(new LinearGradient(color(), color(), 90));

$plot->mark->setType(MARK_CIRCLE);
$plot->mark->setSize(40);
$plot->mark->border->show();


// All possible backgrounds
if(mt_rand(0, 2) === 0) {
	$plot->mark->setFill(new RadialGradient(color(20), color(30)));
} else {
	$plot->mark->setFill(new LinearGradient(color(20), color(30), 90 * mt_rand(0, 1)));
}

$plot->mark->border->setColor(new Color(0, 0, 0, 50));

$plot->yAxis->setLabelNumber(15);
$plot->yAxis->setLabelPrecision(1);
$plot->yAxis->setNumberByTick('minor', 'major', 4);

$plot->xAxis->label->hideFirst(TRUE);
$plot->xAxis->setNumberByTick('minor', 'major', 4);

$graph->add($plot);
$graph->draw();
?>