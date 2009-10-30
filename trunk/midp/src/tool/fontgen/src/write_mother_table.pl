#!/usr/bin/perl

use strict;
use FileHandle;

my $fh = new FileHandle('build/tables.txt');
if (!defined($fh)) {
	die "Couldn't find tables list\n"; }

my @p = ();
for (my $i=0; $i<256; $i++) {
	$p[$i] = '(pfontbitmap)0'; }

my @f = <$fh>;
foreach my $l (@f) {
	if ($l =~ /page_(..)/) {
		my $hw = $1;
		chomp $l;
		my $i = hex($hw);
		$p[$i] = $l; } }
		
print "\n\n";
print "pfontbitmap FontBitmaps[0x100] = {\n\t";
print join(",\n\t", @p);
print "\n};";
