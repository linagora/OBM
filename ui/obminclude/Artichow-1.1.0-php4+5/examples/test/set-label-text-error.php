<?php
require_once '../../BarPlot.class.php';

$graph = new Graph(600, 200);
$graph->setAntiAliasing(TRUE);

$values = array(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0);

$plot = new BarPlot($values);
$plot->setBarColor(
	new Color(234, 236, 255)
);
$plot->setSpace(5, 5, NULL, NULL);

$plot->barShadow->setSize(3);
$plot->barShadow->setPosition(SHADOW_RIGHT_TOP);
$plot->barShadow->setColor(new Color(180, 180, 180, 10));
$plot->barShadow->smooth(TRUE);


$mois = array ('Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Juil', 'Août', 'Sept', 'Oct', 'Nov', 'Déc');
$label = array ();
foreach ($mois as $m) { $label []= $m; }
$label []= ' ';
foreach ($mois as $m) { $label []= $m; }


$plot->xAxis->setLabelText($label);

/* ICI */

   $max = array_max($values);
   $yValues = array();
   for($i=0; $i<= $max; $i++) {
	$yValues[]=$i;
   }
   $plot->yAxis->setLabelText($yValues);

  // Image::drawError(var_export($yValues, TRUE));
$plot->yAxis->setLabelText($yValues);

$plot->setPadding(30,5,20,15);

$labelAvant = new Label("2005");
$labelAvant->setFont (new TTFFont(ARTICHOW_FONT.'/TuffyBold.ttf', 12));
$labelAvant->move (180,10);

$labelMaintenant = new Label("2006");
$labelMaintenant->setFont (new TTFFont(ARTICHOW_FONT.'/TuffyBold.ttf', 12));
$labelMaintenant->move (450,10);

$graph->add($plot);
$graph->addLabel($labelAvant, 0, 0);
$graph->addLabel($labelMaintenant, 0, 0);

$graph->draw();

?>