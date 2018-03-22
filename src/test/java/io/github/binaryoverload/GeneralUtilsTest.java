/*
 * MIT License
 *
 * Copyright (c) 2018 William Oldham
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    @Test
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
        GeneralUtils.verifyPath("cat.cat.cat", '.', new char[]{});
        GeneralUtils.verifyPath("ca-t.cat.cat", '.', new char[]{'-'});
        GeneralUtils.verifyPath("cat_cat.cat", '.', new char[]{'_'});
        GeneralUtils.verifyPath("cat-cat-cat", '-', new char[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPathNegative() {
        GeneralUtils.verifyPath("cat.cat.cat..", '.', new char[]{});
        GeneralUtils.verifyPath("cat-cat-cat-", '-', new char[]{});
    }

}