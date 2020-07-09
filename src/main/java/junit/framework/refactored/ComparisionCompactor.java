package junit.framework.refactored;

import junit.framework.Assert;
/*
    Step 1 : eliminate all the f’s prefixIndexes for the member variables. [N6]

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

    Step 7 : Notice that this required us to promote compactExpected
    and compactActual to member variables. I don’t like the way that
    the last two lines of the new function return variables,but  the
    first  two  don’t.  They  aren’t  using  consistent  conventions  [G11].
    So  we  should change findCommonPrefix and findCommonSuffix to return the
    prefixIndex and suffix values. We should also change the names of the member
    variables to be a little more accurate[N1]; after all, they are both indices. 

    Step 8 : Careful inspection of findCommonSuffix exposes a hidden temporal
    coupling [G31]; it depends on the fact that prefixIndex is calculated by
    findCommonPrefix. If these two functions were called out of order, there
    would be a difficult debugging session ahead. So, to expose  this  temporal
    coupling,  let’s  have  findCommonSuffix take  the  prefixIndex as  an argument.

    Step 9 : The passing of the prefixIndex argument is a bit arbitrary[G32].
    It  works  to  establish  the  ordering  but  does  nothing  to  explain
    the  need  for  that ordering. Another programmer might undo what we have
    done because there’s no indication that the parameter is really needed.

    We  put findCommonPrefix and findCommonSuffix back  the  way  they  were,
    changing  the name  of  findCommonSuffix to findCommonPrefixAndSuffix
    and  having  it  call  findCommonPrefix before  doing  anything  else.
    That  establishes  the  temporal  nature  of  the  two  functions
    in a much more dramatic way than the previous solution.
    It also points out how ugly findCommonPrefixAndSuffix is.
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
    private int prefixIndex;
    private int suffixIndex;

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
        int expectedSuffixIndex = expected.length() - 1;
        int actualSuffixIndex = actual.length() - 1;
        for (;
             actualSuffixIndex >= prefixIndex && expectedSuffixIndex >= prefixIndex;
             actualSuffixIndex--, expectedSuffixIndex--) {
            if (expected.charAt(expectedSuffixIndex) != actual.charAt(actualSuffixIndex))
                break;
        }
        suffixIndex = expected.length() - expectedSuffixIndex;
    }

    private void findCommonPrefix() {
        prefixIndex = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefixIndex < end; prefixIndex++) {
            if (expected.charAt(prefixIndex) != actual.charAt(prefixIndex))
                break;
        }
    }

    private String compactString(String source) {
        String result = DELTA_START +
                source.substring(prefixIndex, source.length() -
                        suffixIndex + 1) + DELTA_END;
        if (prefixIndex > 0)
            result = computeCommonprefixIndex() + result;
        if (suffixIndex > 0)
            result = result + computeCommonsuffixIndex();
        return result;
    }

    

    private String computeCommonprefixIndex() {
        return (prefixIndex > contextLength ? ELLIPSIS : "") +
                expected.substring(Math.max(0, prefixIndex - contextLength),
                        prefixIndex);
    }

    private String computeCommonsuffixIndex() {
        int end = Math.min(expected.length() - suffixIndex + 1 + contextLength,
                expected.length());
        return expected.substring(expected.length() - suffixIndex + 1, end) +
                (expected.length() - suffixIndex + 1 < expected.length() -
                        contextLength ? ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return expected.equals(actual);
    }
}
