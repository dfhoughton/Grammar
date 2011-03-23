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
})
public class AllTests {
}
