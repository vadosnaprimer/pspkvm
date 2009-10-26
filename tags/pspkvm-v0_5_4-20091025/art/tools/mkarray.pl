#!/usr/bin/perl

use strict;

# Generate a long array format image dump for use in 
# code running at the client level--see 
# LongArrayHandler.createImage(...) for a method
# of turning these easily into images in client code.
# Run on a .png file from this tool's home directory
# to generate a seg and segpad value for passing into
# this method at runtime.

# Work out the segpad (value to ignore at the
# end of the array, since these are long arrays
# and will fill out to boundaries of 8 bytes.
# We use long arrays because the bytecode is more
# compact this way.
sub get_segpad {
	my ($s) = @_;
	my $r = 8-($s % 8);
	if ($r == 8) {
		$r = 0; }
	return $r; }

# Read the filename, prep it as a variable name
my $s = $ARGV[0];
my $n = $s;
$n =~ s/\.//g;
$n =~ s/\//_/g;
$n =~ s/png$//;

# Generate the Java
my $t = `ls -l $s`;
my @a = split /\s+/, $t;
print "public static final int ", $n, "_segpad = " , get_segpad($a[4]), ";\n";
print "public static final long[] ", $n, "_seg = {\n"; 
my $l = `hexdump -f longformat.txt $s`;
$l =~ s/0x\s+l,//g;
$l =~ s/\s+$//;
$l =~ s/,+$//g;
chomp $l;
print $l;
print "\n};\n";
