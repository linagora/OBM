<?php
require_once '../AntiSpam.class.php';

$span = new AntiSpam;

ob_start();

assert($span->check('test', 'TeST It!') === TRUE);
assert($span->check('test', 'TeST iT!', FALSE) === FALSE);
assert($span->check('test', 'test it!', TRUE) === TRUE);

$data = ob_get_clean();

if($data === '') {
	echo "All tests passed!";
} else {
	echo "Some errors have occur:<br/>";
	echo $data;
}
echo "<ul>
	<li><a href='AntiSpam.php'>Previous page</a></li>
</ul>";
?>