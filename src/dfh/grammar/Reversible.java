package dfh.grammar;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For marking a {@link Rule} as capable of being used in a backwards assertion.
 * This allows compile time checking of grammars using backwards assertions.
 * 
 * <b>Creation date:</b> May 17, 2011
 * 
 * @author David Houghton
 * 
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Reversible {

}
