#!/usr/bin/perl 
#===============================================================================
#
#        USAGE: ./update_svuid.pl [1]
#
#        if you include a parameter that evalues to true, the serialVersionUID values
#        are incremented; otherwise, they are just displayed
#
#  DESCRIPTION: for surveying or incrementing the serialVersionUID values in java
#        classes. Why is this written in Perl? Because I like Perl.
#
#       AUTHOR: David F. Houghton
#      CREATED: 12/22/2011 08:29:05 AM
#===============================================================================

use strict;
use warnings;
use FileHandle;
use File::Find;
use utf8;

my $change = shift;

my $re =
qr/^(\s++private\s++static\s++final\s++long\s++serialVersionUID\s++=\s++)(\d++)([lL];)/m;

find(
    sub {
        if ( -f && /\.java$/ ) {
            my $fh = FileHandle->new($_);
            local $/;
            my $java = <$fh>;
            utf8::decode($java);
            if ($change) {
                my $changed = $java =~ s/$re/$1.($2 + 1).$3/eg;
                if ($changed) {
                    $fh = FileHandle->new( $_, 'w' );
                    binmode $fh, ':utf8';
                    print $fh $java;
                }
            }
            else {
                if ( $java =~ $re ) {
                    my $svuid = $2;
                    ( my $class = $File::Find::name ) =~ s!/!.!g;
                    $class =~ s/^src\.//;
                    $class =~ s/\.java$//;
                    print "$class => $svuid\n";
                }
            }
        }
    },
    'src'
);
