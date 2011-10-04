<?php

require_once "../../Graph.class.php";

$graph = new Graph(400, 600);

$driver = $graph->getDriver();

$driver->filledRectangle(
	new Red,
	new Line(
		new Point(200, 0),
		new Point(200, 600)
	)
);

$text = new Text(
	"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean gravida quam semper nibh. Sed orci. Aenean ullamcorper magna eget odio. Sed nonummy ante sit amet sapien.\nPhasellus nulla dui, aliquet vel, adipiscing vel, vulputate sed, velit.\nSed at neque vel ipsum commodo hendrerit.\nA. Nonyme",
	new Tuffy(mt_rand(10, 15)),
	new Color(mt_rand(0, 100), mt_rand(0, 100), mt_rand(0, 100)),
	0
);

$driver->string($text, new Point(0, 0), 200);

$text = new Text(
	"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean gravida quam semper nibh. Sed orci. Aenean ullamcorper magna eget odio. Sed nonummy ante sit amet sapien.\nPhasellus nulla dui, aliquet vel, adipiscing vel, vulputate sed, velit.\nSed at neque vel ipsum commodo hendrerit.\nA. Nonyme",
	new Font(mt_rand(2, 4)),
	new Color(mt_rand(0, 100), mt_rand(0, 100), mt_rand(0, 100)),
	0
);

$driver->string($text, new Point(0, 400), 200);

$graph->draw();

?>
