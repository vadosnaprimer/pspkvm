#!/usr/bin/perl
# Transpose all the tables from CJK to Unicode

use strict;

for (my $i=0x4e; $i<=0x9f; $i++) {
	my $cmd = sprintf("./rdfont3.exe utable %02x > tables/page_%02x.txt", $i, $i);
	system ($cmd); }
