#!/usr/bin/perl
# Transpose all the tables from CJK to Unicode

use strict;

sub gen_table {
	my ($v) = @_;
	my $cmd = sprintf("./rdfont3.exe utable %02x > tables/page_%02x.txt", $v, $v);
	system ($cmd); }

for (my $i=0x4e; $i<=0x9f; $i++) {
	gen_table($i); }
# Others to do
my @others = (0x03, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x30,
	0x31, 0x32, 0x33, 0xf9, 0xfa, 0xfe, 0xff);
foreach my $i (@others) {
	gen_table($i); }
