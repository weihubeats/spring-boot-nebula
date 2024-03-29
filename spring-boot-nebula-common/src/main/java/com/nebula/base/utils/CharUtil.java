/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.nebula.base.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author : wh
 * @date : 2023/11/18 13:58
 * @description:
 */
public class CharUtil {
    
    // ---------------------------------------------------------------- simple
    
    /**
     * Converts (signed) byte to (unsigned) char.
     */
    public static char toChar(final byte b) {
        return (char) (b & 0xFF);
    }
    
    /**
     * Converts char array into byte array by stripping the high byte of each character.
     */
    public static byte[] toSimpleByteArray(final char[] carr) {
        final byte[] barr = new byte[carr.length];
        for (int i = 0; i < carr.length; i++) {
            barr[i] = (byte) carr[i];
        }
        return barr;
    }
    
    /**
     * Converts char sequence into byte array.
     *
     * @see #toSimpleByteArray(char[])
     */
    public static byte[] toSimpleByteArray(final CharSequence charSequence) {
        final byte[] barr = new byte[charSequence.length()];
        for (int i = 0; i < barr.length; i++) {
            barr[i] = (byte) charSequence.charAt(i);
        }
        return barr;
    }
    
    /**
     * Converts byte array to char array by simply extending bytes to chars.
     */
    public static char[] toSimpleCharArray(final byte[] barr) {
        final char[] carr = new char[barr.length];
        for (int i = 0; i < barr.length; i++) {
            carr[i] = (char) (barr[i] & 0xFF);
        }
        return carr;
    }
    
    // ---------------------------------------------------------------- ascii
    
    /**
     * Returns ASCII value of a char. In case of overload, 0x3F is returned.
     */
    public static int toAscii(final char c) {
        if (c <= 0xFF) {
            return c;
        } else {
            return 0x3F;
        }
    }
    
    /**
     * Converts char array into {@link #toAscii(char) ASCII} array.
     */
    public static byte[] toAsciiByteArray(final char[] carr) {
        final byte[] barr = new byte[carr.length];
        for (int i = 0; i < carr.length; i++) {
            barr[i] = (byte) ((int) (carr[i] <= 0xFF ? carr[i] : 0x3F));
        }
        return barr;
    }
    
    /**
     * Converts char sequence into ASCII byte array.
     */
    public static byte[] toAsciiByteArray(final CharSequence charSequence) {
        final byte[] barr = new byte[charSequence.length()];
        for (int i = 0; i < barr.length; i++) {
            final char c = charSequence.charAt(i);
            barr[i] = (byte) ((int) (c <= 0xFF ? c : 0x3F));
        }
        return barr;
    }
    
    // ---------------------------------------------------------------- raw arrays
    
    /**
     * Converts char array into byte array by replacing each character with two bytes.
     */
    public static byte[] toRawByteArray(final char[] carr) {
        final byte[] barr = new byte[carr.length << 1];
        for (int i = 0, bpos = 0; i < carr.length; i++) {
            final char c = carr[i];
            barr[bpos++] = (byte) ((c & 0xFF00) >> 8);
            barr[bpos++] = (byte) (c & 0x00FF);
        }
        return barr;
    }
    
    public static char[] toRawCharArray(final byte[] barr) {
        int carrLen = barr.length >> 1;
        if (carrLen << 1 < barr.length) {
            carrLen++;
        }
        final char[] carr = new char[carrLen];
        int i = 0, j = 0;
        while (i < barr.length) {
            char c = (char) (barr[i] << 8);
            i++;
            
            if (i != barr.length) {
                c += barr[i] & 0xFF;
                i++;
            }
            carr[j++] = c;
        }
        return carr;
    }
    
    // ---------------------------------------------------------------- encoding
    
    /**
     * Converts char array to byte array using default Jodd encoding.
     */
    public static byte[] toByteArray(final char[] carr) {
        return new String(carr).getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Converts char array to byte array using provided encoding.
     */
    public static byte[] toByteArray(final char[] carr, final Charset charset) {
        return new String(carr).getBytes(charset);
    }
    
    /**
     * Converts byte array of default Jodd encoding to char array.
     */
    public static char[] toCharArray(final byte[] barr) {
        return new String(barr).toCharArray();
    }
    
    /**
     * Converts byte array of specific encoding to char array.
     */
    public static char[] toCharArray(final byte[] barr, final Charset charset) {
        return new String(barr, charset).toCharArray();
    }
    
    // ---------------------------------------------------------------- find
    
    /**
     * Match if one character equals to any of the given character.
     *
     * @return <code>true</code> if characters match any character from given array,
     * otherwise <code>false</code>
     */
    public static boolean equalsOne(final char c, final char[] match) {
        for (final char aMatch : match) {
            if (c == aMatch) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds index of the first character in given array the matches any from the
     * given set of characters.
     *
     * @return index of matched character or -1
     */
    public static int findFirstEqual(final char[] source, final int index, final char[] match) {
        for (int i = index; i < source.length; i++) {
            if (equalsOne(source[i], match)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Finds index of the first character in given array the matches any from the
     * given set of characters.
     *
     * @return index of matched character or -1
     */
    public static int findFirstEqual(final char[] source, final int index, final char match) {
        for (int i = index; i < source.length; i++) {
            if (source[i] == match) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Finds index of the first character in given array the differs from the
     * given set of characters.
     *
     * @return index of matched character or -1
     */
    public static int findFirstDiff(final char[] source, final int index, final char[] match) {
        for (int i = index; i < source.length; i++) {
            if (!equalsOne(source[i], match)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Finds index of the first character in given array the differs from the
     * given set of characters.
     *
     * @return index of matched character or -1
     */
    public static int findFirstDiff(final char[] source, final int index, final char match) {
        for (int i = index; i < source.length; i++) {
            if (source[i] != match) {
                return i;
            }
        }
        return -1;
    }
    
    // ---------------------------------------------------------------- is
    
    /**
     * Returns <code>true</code> if character is a white space ({@code <= ' '}).
     * White space definition is taken from String class (see: <code>trim()</code>).
     * This method has different results then <code>Character#isWhitespace</code>."
     */
    public static boolean isWhitespace(final char c) {
        return c <= ' ';
    }
    
    /**
     * Returns <code>true</code> if specified character is lowercase ASCII.
     * If user uses only ASCIIs, it is much much faster.
     */
    public static boolean isLowercaseAlpha(final char c) {
        return (c >= 'a') && (c <= 'z');
    }
    
    /**
     * Returns <code>true</code> if specified character is uppercase ASCII.
     * If user uses only ASCIIs, it is much much faster.
     */
    public static boolean isUppercaseAlpha(final char c) {
        return (c >= 'A') && (c <= 'Z');
    }
    
    public static boolean isAlphaOrDigit(final char c) {
        return isDigit(c) || isAlpha(c);
    }
    
    public static boolean isWordChar(final char c) {
        return isDigit(c) || isAlpha(c) || (c == '_');
    }
    
    public static boolean isPropertyNameChar(final char c) {
        return isDigit(c) || isAlpha(c) || (c == '_') || (c == '.') || (c == '[') || (c == ']');
    }
    
    // ---------------------------------------------------------------- RFC
    
    /**
     * Indicates whether the given character is in the {@code ALPHA} set.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
     */
    public static boolean isAlpha(final char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }
    
    /**
     * Indicates whether the given character is in the {@code DIGIT} set.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
     */
    public static boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }
    
    /**
     * Indicates whether the given character is the hexadecimal digit.
     */
    public static boolean isHexDigit(final char c) {
        return (c >= '0' && c <= '9') || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F'));
    }
    
    /**
     * Indicates whether the given character is in the <i>gen-delims</i> set.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
     */
    public static boolean isGenericDelimiter(final int c) {
        switch (c) {
            case ':':
            case '/':
            case '?':
            case '#':
            case '[':
            case ']':
            case '@':
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Indicates whether the given character is in the <i>sub-delims</i> set.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
     */
    public static boolean isSubDelimiter(final int c) {
        switch (c) {
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Indicates whether the given character is in the <i>reserved</i> set.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
     */
    public static boolean isReserved(final char c) {
        return isGenericDelimiter(c) || isSubDelimiter(c);
    }
    
    /**
     * Indicates whether the given character is in the <i>unreserved</i> set.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
     */
    public static boolean isUnreserved(final char c) {
        return isAlpha(c) || isDigit(c) || c == '-' || c == '.' || c == '_' || c == '~';
    }
    
    /**
     * Indicates whether the given character is in the <i>pchar</i> set.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
     */
    public static boolean isPchar(final char c) {
        return isUnreserved(c) || isSubDelimiter(c) || c == ':' || c == '@';
    }
    
    // ---------------------------------------------------------------- conversions
    
    /**
     * Uppers lowercase ASCII char.
     */
    public static char toUpperAscii(char c) {
        if (isLowercaseAlpha(c)) {
            c -= (char) 0x20;
        }
        return c;
    }
    
    /**
     * Lowers uppercase ASCII char.
     */
    public static char toLowerAscii(char c) {
        if (isUppercaseAlpha(c)) {
            c += (char) 0x20;
        }
        return c;
    }
    
    /**
     * Converts hex char to int value.
     */
    public static int hex2int(final char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return c - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return c - 55;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return c - 87;
            default:
                throw new IllegalArgumentException("Not a hex: " + c);
        }
    }
    
    /**
     * Converts integer digit to heck char.
     */
    public static char int2hex(final int i) {
        return HEX_CHARS[i];
    }
    
    public static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    
}
