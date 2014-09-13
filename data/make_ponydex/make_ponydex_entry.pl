#!/usr/bin/perl

require 5.013022;
use strict;
use warnings;

my $file = $ARGV[0];

open(STREAM, $file) or die('Cannot open file: ' . $file);

my $line;
my $name;
my $race;
my @stats;
my @moves;
my @type;
my @abilities;

# chomp data
foreach $line (<STREAM>) {
	if($line =~ /name = \"([A-Za-z\s']+)\"/) {
		$name = $1;

	} elsif($line =~ /type\[[0-1]\] = Type\.([A-Z]+)/) {
		push(@type, $1);

	} elsif($line =~ /race = Race.([A-Z]+)/) {
		$race = $1;
		
	} elsif($line =~ /base[A-Z][a-z]+ = ([0-9]+)/) {
		push(@stats, $1); 

	} elsif($line =~ /learnableMoves\.put\("([A-Za-z\s]+)\"/) {
		push(@moves, $1);

	} elsif($line =~ /possibleAbilities\[[0-9]+\] = \"([A-Za-z\s]+)\"/) {
		push(@abilities, $1);
	
	}
}
close(STREAM);

# output html code (a single dex entry)
print 
"      <table class='entry'>
        <tr>
          <th class='name'>$name</th>\n";

my $i = 0;
for ($i = 0; $i < scalar @type; ++$i) {
	print "          <td class='type ",lc($type[$i])."'>",substr($type[$i],0,1),lc(substr($type[$i],1,length($type[$i])-1)),"</td>\n";
}
my $sprite = 'sprites/' . $name =~ s/[\s']//gr . '/stand_right.gif';
$sprite =~ s/gif/png/g unless (-e $sprite);
print
"        </tr>
        <tr>
          <td><img src='", $sprite,"' /></td>
          <td>
            <table class='stats'>
              <tr>
                <th>HP</th>
                <td>", $stats[0], "</td>
                <td>&nbsp;&nbsp;</td>
                <th>SpA</th>
                <td>", $stats[3], "</td>
              </tr>
              <tr>
                <th>Atk</th>
                <td>", $stats[1], "</td>
                <td>&nbsp;&nbsp;</td>
                <th>SpD</th>
                <td>", $stats[4], "</td>
              </tr>
              <tr>   
                <th>Def</th>
                <td>", $stats[2], "</td>
                <td>&nbsp;&nbsp;</td>
                <th>Spe</th>
                <td>", $stats[5], "</td>
              </tr>
            </table>
          </td>
          <td>&nbsp;&nbsp;</td>
          <td>
            <table class='ability'>\n";

for($i = 0; $i < scalar @abilities; ++$i) {
	print
"              <tr>
                <td>", $abilities[$i], "</td>
              </tr>\n";
}
print
"            </table>
          </td>
        </tr>
      </table>\n";
