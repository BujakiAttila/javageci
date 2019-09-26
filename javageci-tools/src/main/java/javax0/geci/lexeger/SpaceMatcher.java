package javax0.geci.lexeger;

import javax0.geci.javacomparator.lex.LexicalElement;

import java.util.regex.Pattern;

public class SpaceMatcher extends AbstractPatternedMatcher {

    public SpaceMatcher(LexExpression factory, JavaLexed javaLexed, Pattern pattern) {
        super(factory, javaLexed, pattern);
    }

    public SpaceMatcher(LexExpression factory, JavaLexed javaLexed, String text) {
        super(factory, javaLexed, text);
    }

    public SpaceMatcher(LexExpression factory, JavaLexed javaLexed) {
        super(factory, javaLexed);
    }

    @Override
    public MatchResult match(int i) {
        return match(i, LexicalElement.Type.SPACING);
    }
}
