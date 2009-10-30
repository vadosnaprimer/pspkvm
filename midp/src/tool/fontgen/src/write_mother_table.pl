#!/usr/bin/perl

use strict;
use FileHandle;

my $fh = new FileHandle('build/tables.txt');
if (!defined($fh)) {
	die "Couldn't fine tables list\n"; }

print "# In makefile:";
my @d = `ls build/*.c`;
my @m = ();
foreach my $l (@d) {
	if ($l =~ /page_../) {
		chomp $l;
		$l =~ s/build\///;
		push @m, $l; } }
print "\n\t", join(" \\\n\t", @m);
print "\n\n";

my @f = <$fh>;
print "// In source:\n\n";
print "pfontbitmap FontBitmaps[0x100] = { 0 };\n\n";
foreach my $l (@f) {
	if ($l =~ /page_(..)/) {
		my $hw = $1;
		chomp $l;
		# Grab the high bit, put it where we want it
		print "extern pfontbitmap $l;\n";
		printf "FontBitmaps[0x$hw] = $l;\n"; } }
