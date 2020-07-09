package junit.framework.refactored;

import junit.framework.Assert;
public class ComparisionCompactor {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int contextLength;
    private String expected;
    private String actual;
    private int prefixLength;
    private int suffixLength;

    public ComparisionCompactor(int contextLength,
                                String expected,
                                String actual) {
        this.contextLength = contextLength;
        this.expected = expected;
        this.actual = actual;
    }

    public String formatCompactedComparison(String message) {
        String compactExpected = expected;
        String compactActual= actual;
        if(shouldBeCompacted()){
            findCommonPrefixAndSuffix();
            compactExpected = compact(this.expected);
            compactActual = compact(this.actual);
        }
        return Assert.format(message, compactExpected, compactActual);

    }

    public boolean shouldBeCompacted() {
        return !shouldNotBeCompacted();
    }

    public boolean shouldNotBeCompacted() {
        return expected == null ||
               actual == null ||
               areStringsEqual();
    }

    private void findCommonPrefixAndSuffix() {
        findCommonPrefix();
        suffixLength = 0;
        for(; !suffixOverlapsPrefix(suffixLength); suffixLength++) {
            if (charFromEnd(expected,suffixLength) != charFromEnd(actual, suffixLength))
                break;
        }
    }

    private void findCommonPrefix() {
        prefixLength = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefixLength < end; prefixLength++) {
            if (expected.charAt(prefixLength) != actual.charAt(prefixLength))
                break;
        }
    }

    private boolean suffixOverlapsPrefix(int suffixLength) {
        return actual.length() - suffixLength <= prefixLength ||
                expected.length() - suffixLength <= prefixLength;
    }

    private char charFromEnd(String expected, int suffixLength) {
        return expected.charAt(expected.length() - suffixLength - 1);
    }

    private String compact(String source) {
        return new StringBuilder()
                .append(startingEllipsis())
                .append(startingContext())
                .append(DELTA_START)
                .append(delta(source))
                .append(DELTA_END)
                .append(endingContext())
                .append(endingEllipsis())
                .toString();
    }

    private String startingEllipsis() {
        return prefixLength > contextLength ? ELLIPSIS : "";
    }

    private String startingContext() {
        int contextStart = Math.max(0, prefixLength -contextLength);
        int contextEnd = prefixLength;
        return expected.substring(contextStart, contextEnd);
    }

    private String delta(String s) {
        int deltaStart = prefixLength;
        int deltaEnd = s.length() - suffixLength;
        return s.substring(deltaStart, deltaEnd);
    }

    private String endingContext() {
        int contextStart = expected.length() - suffixLength;
        int contextEnd =
                Math.min(contextStart + contextLength, expected.length());
        return expected.substring(contextStart, contextEnd);

    }

    private String endingEllipsis() {
        return (suffixLength > contextLength ? ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return expected.equals(actual);
    }
}

