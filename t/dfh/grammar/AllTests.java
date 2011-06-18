package dfh.grammar;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs a series of unit test classes.
 * <p>
 * <b>Creation date:</b> Mar 23, 2011
 * 
 * @author David Houghton
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
//
		BackReferenceTest.class,//
		CommentTest.class,//
		CompilationTest.class, //
		DeferredRuleTest.class,//
		LiteralTest.class,//
		MisCompilationTest.class,//
		RepetitionTest.class,//
		SimpleMatchTest.class,//
		RecursiveTest.class,//
		AssertionTest.class,//
		IterationTest.class,//
		MultithreadTest.class,//
		TerminalGrammarTest.class,//
		ConditionTest.class,//
		BacktrackingBarrierTest.class,//
		DeepRecursionTest.class,//
		OverlapTest.class,//
		InlineRegexTest.class,//
		TaggingTest.class,//
		NoBracketsStyleCompilationTest.class,//
})
public class AllTests {
}
