#!/usr/bin/perl

# Simple Perl utility for generating font blanks in which to draw 
# Syntax: ./gen_blanks page_base font_width font_height ascent descent leading
# eg. for page one ./gen_blanks 1 8 16 12 4 0
# eg. for page e0 ./gen_blanks e0 8 16 12 4 0
# AJ Milne 2008 August

use strict;

# Read command line

if (scalar(@ARGV)<6) {
	print STDERR "Syntax: ./gen_blanks page_base font_width font_height ascent descent leading\n";
	die; }

my ($page_base, $width, $height, $asc, $desc, $leading) = @ARGV;
$page_base = hex($page_base);

sub gen_header {
	print "# Font parameters\n";
	print "# width height ascent descent leading\n";
	print "\@ $width $height $asc $desc $leading\n";
	print "# high_code low_code_first low_code_last\n";
	print "\% $page_base 00 ff\n";
	print "\n"; }

sub gen_sepline {
	my ($w) = @_;
	my $r = '#';
	for(my $i=0; $i<$w; $i++) {
		$r .= '-'; }
	$r .= "\n";
	return $r; }

sub gen_blankline {
	my ($w) = @_;
	my $r = '';
	for(my $i=0; $i<$w; $i++) {
		$r .= ' '; }
	$r .= ".\n";
	return $r; }

# Generate the midlines of the
# 'default glyph' box
sub gen_sbody {
	my ($w) = @_;
	my $r = '';
	$r .= '*';
	for(my $i=0; $i<($w-3); $i++) {
		$r .= ' '; }
	$r .= "* .\n";
	return $r; }

# Generate the top or bottom border
# of the 'default glyph' box
sub gen_sborder {
	my ($w) = @_;
	my $r = '';
	for(my $i=0; $i<($w-1); $i++) {
		$r .= '*'; }
	$r .= " .\n";
	return $r; }

sub gen_blank {
	my ($w, $h) = @_;
	my $bot_blanks = $h - $asc - 1;
	my $top_blanks = $bot_blanks +1;
	my $body_height = $h - $bot_blanks - $top_blanks - 2;
	my $r = gen_sepline($w);
	for (my $i=0; $i<$top_blanks; $i++) {
		$r .= gen_blankline($w); }
	$r .= gen_sborder($w);
	for (my $i=0; $i<$body_height; $i++) {
		$r .= gen_sbody($w); }
	$r .= gen_sborder($w);
	for (my $i=0; $i<$bot_blanks; $i++) {
		$r .= gen_blankline($w); }
	$r .= gen_sepline($w);
	return $r; }
	
# START MAIN ROUTINE

gen_header();

my $blank = gen_blank($width, $height);
my $sepline = gen_sepline($width);

$page_base *= 0x100;

for(my $i=$page_base; $i<($page_base+0x100); $i++) {
	print $sepline;
	printf(": %x\n", $i);
	print $blank;  }
