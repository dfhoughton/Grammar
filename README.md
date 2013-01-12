Grammar
=======

David F. Houghton
19 Feb, 2011


This is an experiment to create in Java something like the Grammar facilities of Perl 6 or the recursive regexes of Perl 5.10+. It will parse by recursive decent, producing ASTs. For example, it can create a pattern matcher out of the following:

      rule = [ <a> | <b> ]{2} <b>
         a = "a"{,2}
         b = "ab"

This is idle itch scratching. I am writing this because it's more fun than figuring out how to use someone else's library. Also, I need recursive matching for a current project, so now's a good time. I aim to make it robust and well-tested but I make no hours of academic research, try ANTLR or Parboiled or something such. I confess I read none of the relevant research nor studied anyone else's algorithms before I started this, so a good bit of wheel re-invention is likely embodied in this code.

That being said, I think it's well designed and efficient. For examples of grammars it can match see the documentation and test (t/) directories. There is also an examples directory that contains benchmark tests comparing grammars to equivalent native regular expressions. Note that grammars can do things regular expressions can't, so these benchmarks don't demonstrate everything.

Full Documentation
------------------

The full documentation for this library is available at [my site](http://dfhoughton.org/grammar/).

Other Stuff
-----------

This software is distributed under the terms of the FSF Lesser Gnu 
Public License (see lgpl.txt).
