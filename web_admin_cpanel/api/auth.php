<?php
/**
 * Compatibility alias for auth.php -> users.php
 */
$_SERVER['REQUEST_METHOD'] = 'POST';
require_once __DIR__ . '/users.php';
