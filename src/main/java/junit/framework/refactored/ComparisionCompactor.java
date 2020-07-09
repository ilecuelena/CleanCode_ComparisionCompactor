package junit.framework.refactored;

import junit.framework.Assert;
public class ComparisionCompactor {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int contextLength;
    private String expected;
    private String actual;
    private String compactExpected;
    private String compactActual;
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
        findCommonPrefixAndSuffix();
        compactExpected = compactString(this.expected);
        compactActual = compactString(this.actual);
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

    private String compactString(String source) {
        return computeCommonPrefixIndex() +
                DELTA_START +
                source.substring(prefixLength, source.length() - suffixLength ) +
                DELTA_END +
                computeCommonSuffixIndex();
    }

    private String computeCommonPrefixIndex() {
        return (prefixLength > contextLength ? ELLIPSIS : "") +
                expected.substring(Math.max(0, prefixLength - contextLength),
                        prefixLength);
    }

    private String computeCommonSuffixIndex() {
        int end = Math.min(
                expected.length() - suffixLength + contextLength,
                expected.length());

        return expected.substring(expected.length() - suffixLength , end) +
                (expected.length() - suffixLength < expected.length() -
                        contextLength ? ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return expected.equals(actual);
    }
}
