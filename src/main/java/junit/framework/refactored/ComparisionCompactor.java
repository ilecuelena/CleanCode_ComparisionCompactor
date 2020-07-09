package junit.framework.refactored;

import junit.framework.Assert;
/*
    Step 1 : eliminate all the f’s prefixes for the member variables. [N6]

    Step 2 : Extract the unencapsulated conditional at the beginning
    of the compact function. [G28]

    Step 3 : Why are there variables in this function(compact) that
    have the same names as the member variables? Don’t they represent
    something else [N4]? We should make the names unambiguous.

    Step 4 : Negatives are slightly harder to understand than positives [G29].
    So let’s turn that if statement on its head and invert the sense of the conditional.

    Step 5 : The  name  of  the  function  is  strange  [N7].  Although  it
    does  compact  the  strings,  it actually  might  not  compact  the  strings
    if  canBeCompacted returns false.  So  naming  this function compact hides
    the  side  effect  of  the  error  check.  Notice  also  that  the  function
    returns a formatted message, not just the compacted strings.
    So the name of the function should  really  be  formatCompactedComparison.

    Step 6 : The body of the if statement is where the true compacting of the
    expected and actual strings is done. We should extract that as a method
    named compactExpectedAndActual. However,  we  want  the
    formatCompactedComparison function  to  do  all  the  formatting.
    The compact... function should do nothing but compacting [G30].
 */
public class ComparisionCompactor {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int contextLength;
    private String expected;
    private String actual;
    private String compactExpected;
    private String compactActual;
    private int prefix;
    private int suffix;

    public ComparisionCompactor(int contextLength,
                                String expected,
                                String actual) {
        this.contextLength = contextLength;
        this.expected = expected;
        this.actual = actual;
    }

    public String formatCompactedComparison(String message) {

        if(canBeCompacted()){
            compactExpectedAndActual();
            return Assert.format(message, compactExpected, compactActual);
        }else{
            return Assert.format(message, expected, actual);
        }
    }

    public boolean canBeCompacted() {
        return expected != null && actual != null && !areStringsEqual();
    }

    private void compactExpectedAndActual() {
        findCommonPrefix();
        findCommonSuffix();
        compactExpected = compactString(this.expected);
        compactActual = compactString(this.actual);
    }


    private String compactString(String source) {
        String result = DELTA_START +
                source.substring(prefix, source.length() -
                        suffix + 1) + DELTA_END;
        if (prefix > 0)
            result = computeCommonPrefix() + result;
        if (suffix > 0)
            result = result + computeCommonSuffix();
        return result;
    }

    private void findCommonPrefix() {
        prefix = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefix < end; prefix++) {
            if (expected.charAt(prefix) != actual.charAt(prefix))
                break;
        }
    }

    private void findCommonSuffix() {
        int expectedSuffix = expected.length() - 1;
        int actualSuffix = actual.length() - 1;
        for (;
             actualSuffix >= prefix && expectedSuffix >= prefix;
             actualSuffix--, expectedSuffix--) {
            if (expected.charAt(expectedSuffix) != actual.charAt(actualSuffix))
                break;
        }
        suffix = expected.length() - expectedSuffix;
    }

    private String computeCommonPrefix() {
        return (prefix > contextLength ? ELLIPSIS : "") +
                expected.substring(Math.max(0, prefix - contextLength),
                        prefix);
    }

    private String computeCommonSuffix() {
        int end = Math.min(expected.length() - suffix + 1 + contextLength,
                expected.length());
        return expected.substring(expected.length() - suffix + 1, end) +
                (expected.length() - suffix + 1 < expected.length() -
                        contextLength ? ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return expected.equals(actual);
    }
}
