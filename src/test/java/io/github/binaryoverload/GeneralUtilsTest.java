package io.github.binaryoverload;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class GeneralUtilsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void checkStringLengthPositive() {
        GeneralUtils.checkStringLength("", 0, false);
        GeneralUtils.checkStringLength("s", 1);
        GeneralUtils.checkStringLength("ss", 2);
    }

    @Test()
    public void checkStringLengthNegative() {
        thrown.expect(IllegalArgumentException.class);
        GeneralUtils.checkStringLength("", 0, true);
        GeneralUtils.checkStringLength("sss", 1);
        GeneralUtils.checkStringLength("sss", 2);
        thrown.expect(NullPointerException.class);
        GeneralUtils.checkStringLength(null, 0);
    }

    @Test
    public void verifyPathPositive() {
        GeneralUtils.verifyPath("cat.cat.cat", ".");
        GeneralUtils.verifyPath("ca-t.cat.cat", ".");
        GeneralUtils.verifyPath("cat_cat.cat", ".");
        GeneralUtils.verifyPath("cat-cat-cat", "-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPathNegative() {
        GeneralUtils.verifyPath("cat.cat.cat..", ".");
        GeneralUtils.verifyPath("cat-cat-cat-", "-");
    }

}