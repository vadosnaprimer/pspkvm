#!/usr/bin/perl

# Script to write the 'mother table' in gxj_font_bitmap.c --
# the table of tables. See build_tables.sh, which calls this
# script after building each of the child tables.

use strict;

# Collect the table names
my $fn = $ARGV[0];
my @tlist = `grep 'unsigned char page' $fn`;
my @f = ();
foreach my $l (@tlist) {
	if ($l =~ /(page_...._....)/) {
		push (@f, $1); } }

# Prep the default array elements
my @p = ();
for (my $i=0; $i<256; $i++) {
	$p[$i] = '(pfontbitmap)0'; }

# Arrange the table
foreach my $l (@f) {
	if ($l =~ /page_(..)/) {
		my $hw = $1;
		chomp $l;
		my $i = hex($hw);
		$p[$i] = $l; } }

# Write it
print "\n\n";
print "pfontbitmap FontBitmaps[0x100] = {\n\t";
print join(",\n\t", @p);
print "\n};";
