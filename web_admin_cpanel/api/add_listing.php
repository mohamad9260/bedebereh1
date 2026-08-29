<?php
/**
 * Compatibility alias for POST listings.php
 */
$_SERVER['REQUEST_METHOD'] = 'POST';
require_once __DIR__ . '/listings.php';
