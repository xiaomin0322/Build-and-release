package sun.org.mozilla.javascript.internal.annotations;

import java.lang.annotation.*;

/**
 * An annotation that marks a Java method as JavaScript constructor. This can
 * be used as an alternative to the <code>jsConstructor</code> naming convention desribed in
 * {@link sun.org.mozilla.javascript.internal.ScriptableObject#defineClass(sun.org.mozilla.javascript.internal.Scriptable, java.lang.Class)}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface JSConstructor {
}
