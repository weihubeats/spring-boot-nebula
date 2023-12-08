package com.nebula.base.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.helpers.FormattingTuple;

/**
 * @author : wh
 * @date : 2023/11/18 13:58
 * @description:
 */
public class StringUtils {

    public static final String SPACE = " ";
    public static final String TAB = "	";
    public static final String DOT = ".";
    public static final String DOUBLE_DOT = "..";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String EMPTY = "";
    public static final String CR = "\r";
    public static final String LF = "\n";
    public static final String CRLF = "\r\n";
    public static final String UNDERLINE = "_";
    public static final String DASHED = "-";
    public static final String COMMA = ",";
    public static final String DELIM_START = "{";
    public static final String DELIM_END = "}";
    public static final String BRACKET_START = "[";
    public static final String BRACKET_END = "]";
    public static final String COLON = ":";

    /**
     * 将var2参数替换成var1中出现的{}
     * <pre>
     * stringFormat("text{}", "a") = texta
     * stringFormat("text,{},{}", "a", "b") = text,a,b
     * stringFormat("text{}", Arrays.asList("1", "2", "3")) = text[1, 2, 3]
     * stringFormat("text\\{}", "a") = text{}
     * </pre>
     *
     * @param var1 字符串
     * @param var2 参数
     * @return
     */
    public static String stringFormat(String var1, Object... var2) {
        return MessageFormatter.arrayFormat(var1, var2).getMessage();
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return str != null && prefix != null && str.length() >= prefix.length() && str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * 判断字符串是否为整数
     *
     * @param str 字符串
     * @return
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[0-9]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * EMOJI encode
     *
     * @param content 带有emoji表情的字符串
     * @return 将字符串中的emoji表情编码为UTF-8
     */
    public static String emojiEncode(String content) {
        try {
            return RegexUtils.replaceAll(content, PatternEnum.EMOJI.getPattern(), a -> {
                try {
                    return "[[EMOJI:" + URLEncoder.encode(a, "UTF-8") + "]]";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException("emoji encode error");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("emoji filter error");
    }

    /**
     * EMOJI decode
     *
     * @param content 字符串
     * @return 将带有编码后emoji的字符串 解码为emoji
     */
    public static String emojiDecode(String content) {
        try {
            return RegexUtils.replaceAll(content, PatternEnum.EMOJI_DECODE.getPattern(), a -> {
                try {
                    return URLDecoder.decode(a, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException("emoji decode error");
            }, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("emoji decode error");
    }

    public static String[] splitc(final String src, final char[] delimiters) {
        if ((delimiters.length == 0) || (src.isEmpty())) {
            return new String[] {src};
        }
        final char[] srcc = src.toCharArray();

        final int maxparts = srcc.length + 1;
        final int[] start = new int[maxparts];
        final int[] end = new int[maxparts];

        int count = 0;

        start[0] = 0;
        int s = 0, e;
        if (CharUtil.equalsOne(srcc[0], delimiters)) {    // string starts with delimiter
            end[0] = 0;
            count++;
            s = CharUtil.findFirstDiff(srcc, 1, delimiters);
            if (s == -1) {                            // nothing after delimiters
                return new String[] {EMPTY, EMPTY};
            }
            start[1] = s;                            // new start
        }
        while (true) {
            // find new end
            e = CharUtil.findFirstEqual(srcc, s, delimiters);
            if (e == -1) {
                end[count] = srcc.length;
                break;
            }
            end[count] = e;

            // find new start
            count++;
            s = CharUtil.findFirstDiff(srcc, e, delimiters);
            if (s == -1) {
                start[count] = end[count] = srcc.length;
                break;
            }
            start[count] = s;
        }
        count++;
        final String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = src.substring(start[i], end[i]);
        }
        return result;
    }

    public static String[] splitc(final String src, final char delimiter) {
        if (src.isEmpty()) {
            return new String[] {EMPTY};
        }
        final char[] srcc = src.toCharArray();

        final int maxparts = srcc.length + 1;
        final int[] start = new int[maxparts];
        final int[] end = new int[maxparts];

        int count = 0;

        start[0] = 0;
        int s = 0, e;
        if (srcc[0] == delimiter) {    // string starts with delimiter
            end[0] = 0;
            count++;
            s = CharUtil.findFirstDiff(srcc, 1, delimiter);
            if (s == -1) {                            // nothing after delimiters
                return new String[] {EMPTY, EMPTY};
            }
            start[1] = s;                            // new start
        }
        while (true) {
            // find new end
            e = CharUtil.findFirstEqual(srcc, s, delimiter);
            if (e == -1) {
                end[count] = srcc.length;
                break;
            }
            end[count] = e;

            // find new start
            count++;
            s = CharUtil.findFirstDiff(srcc, e, delimiter);
            if (s == -1) {
                start[count] = end[count] = srcc.length;
                break;
            }
            start[count] = s;
        }
        count++;
        final String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = src.substring(start[i], end[i]);
        }
        return result;
    }

    public static String[] splitc(final String src, final String d) {
        if ((d.isEmpty()) || (src.isEmpty())) {
            return new String[] {src};
        }
        return splitc(src, d.toCharArray());
    }

    public static class MessageFormatter {
        static final char DELIM_START = '{';
        static final char DELIM_STOP = '}';
        static final String DELIM_STR = "{}";
        private static final char ESCAPE_CHAR = '\\';

        static Throwable getThrowableCandidate(Object[] argArray) {
            if (argArray == null || argArray.length == 0) {
                return null;
            }

            final Object lastEntry = argArray[argArray.length - 1];
            if (lastEntry instanceof Throwable) {
                return (Throwable) lastEntry;
            }
            return null;
        }

        public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray) {
            Throwable throwableCandidate = getThrowableCandidate(argArray);
            Object[] args = argArray;
            if (throwableCandidate != null) {
                args = trimmedCopy(argArray);
            }
            return arrayFormat(messagePattern, args, throwableCandidate);
        }

        private static Object[] trimmedCopy(Object[] argArray) {
            if (argArray == null || argArray.length == 0) {
                throw new IllegalStateException("non-sensical empty or null argument array");
            }
            final int trimemdLen = argArray.length - 1;
            Object[] trimmed = new Object[trimemdLen];
            System.arraycopy(argArray, 0, trimmed, 0, trimemdLen);
            return trimmed;
        }

        final public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray,
            Throwable throwable) {

            if (messagePattern == null) {
                return new FormattingTuple(null, argArray, throwable);
            }

            if (argArray == null) {
                return new FormattingTuple(messagePattern);
            }

            int i = 0;
            int j;
            // use string builder for better multicore performance
            StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);

            int L;
            for (L = 0; L < argArray.length; L++) {

                j = messagePattern.indexOf(DELIM_STR, i);

                if (j == -1) {
                    // no more variables
                    if (i == 0) { // this is a simple string
                        return new FormattingTuple(messagePattern, argArray, throwable);
                    } else { // add the tail string which contains no variables and return
                        // the result.
                        sbuf.append(messagePattern, i, messagePattern.length());
                        return new FormattingTuple(sbuf.toString(), argArray, throwable);
                    }
                } else {
                    if (isEscapedDelimeter(messagePattern, j)) {
                        if (!isDoubleEscaped(messagePattern, j)) {
                            L--; // DELIM_START was escaped, thus should not be incremented
                            sbuf.append(messagePattern, i, j - 1);
                            sbuf.append(DELIM_START);
                            i = j + 1;
                        } else {
                            // The escape character preceding the delimiter start is
                            // itself escaped: "abc x:\\{}"
                            // we have to consume one backward slash
                            sbuf.append(messagePattern, i, j - 1);
                            deeplyAppendParameter(sbuf, argArray[L], new HashMap<Object[], Object>());
                            i = j + 2;
                        }
                    } else {
                        // normal case
                        sbuf.append(messagePattern, i, j);
                        deeplyAppendParameter(sbuf, argArray[L], new HashMap<Object[], Object>());
                        i = j + 2;
                    }
                }
            }
            // append the characters following the last {} pair.
            sbuf.append(messagePattern, i, messagePattern.length());
            return new FormattingTuple(sbuf.toString(), argArray, throwable);
        }

        final static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {

            if (delimeterStartIndex == 0) {
                return false;
            }
            char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
            if (potentialEscape == ESCAPE_CHAR) {
                return true;
            } else {
                return false;
            }
        }

        final static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
            if (delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR) {
                return true;
            } else {
                return false;
            }
        }

        // special treatment of array values was suggested by 'lizongbo'
        private static void deeplyAppendParameter(StringBuilder sbuf, Object o, Map<Object[], Object> seenMap) {
            if (o == null) {
                sbuf.append("null");
                return;
            }
            if (!o.getClass().isArray()) {
                safeObjectAppend(sbuf, o);
            } else {
                // check for primitive array types because they
                // unfortunately cannot be cast to Object[]
                if (o instanceof boolean[]) {
                    booleanArrayAppend(sbuf, (boolean[]) o);
                } else if (o instanceof byte[]) {
                    byteArrayAppend(sbuf, (byte[]) o);
                } else if (o instanceof char[]) {
                    charArrayAppend(sbuf, (char[]) o);
                } else if (o instanceof short[]) {
                    shortArrayAppend(sbuf, (short[]) o);
                } else if (o instanceof int[]) {
                    intArrayAppend(sbuf, (int[]) o);
                } else if (o instanceof long[]) {
                    longArrayAppend(sbuf, (long[]) o);
                } else if (o instanceof float[]) {
                    floatArrayAppend(sbuf, (float[]) o);
                } else if (o instanceof double[]) {
                    doubleArrayAppend(sbuf, (double[]) o);
                } else {
                    objectArrayAppend(sbuf, (Object[]) o, seenMap);
                }
            }
        }

        private static void safeObjectAppend(StringBuilder sbuf, Object o) {
            try {
                String oAsString = o.toString();
                sbuf.append(oAsString);
            } catch (Throwable t) {
                System.err.println("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName() + "]");
                System.err.println("Reported exception:");
                t.printStackTrace();
                sbuf.append("[FAILED toString()]");
            }

        }

        private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Map<Object[], Object> seenMap) {
            sbuf.append('[');
            if (!seenMap.containsKey(a)) {
                seenMap.put(a, null);
                final int len = a.length;
                for (int i = 0; i < len; i++) {
                    deeplyAppendParameter(sbuf, a[i], seenMap);
                    if (i != len - 1)
                        sbuf.append(", ");
                }
                // allow repeats in siblings
                seenMap.remove(a);
            } else {
                sbuf.append("...");
            }
            sbuf.append(']');
        }

        private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }

        private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }

        private static void charArrayAppend(StringBuilder sbuf, char[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }

        private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }

        private static void intArrayAppend(StringBuilder sbuf, int[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }

        private static void longArrayAppend(StringBuilder sbuf, long[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }

        private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }

        private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
            sbuf.append('[');
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                sbuf.append(a[i]);
                if (i != len - 1)
                    sbuf.append(", ");
            }
            sbuf.append(']');
        }
    }

    public static String emojiFilter(String str) {
        if (str == null) {
            return null;
        }
        String patternString = "([\\x{10000}-\\x{10ffff}\ud800-\udfff])";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(str);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                matcher.appendReplacement(sb, "[[EMOJI:" + URLEncoder.encode(matcher.group(1), "UTF-8") + "]]");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String parseCurrency(String currency) {
        if (DataUtils.isEmpty(currency)) {
            return "";
        }
        switch (currency.toUpperCase()) {
            case "USD":
                return "$";
            case "EUR":
                return "€";
            case "CAD":
                return "CA$";
            case "GBP":
                return "£";
            default:
                return null;
        }
    }

    public static String getExceptionStackTrace(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } catch (Exception ex) {
            return "bad get Error StackTrace From Exception";
        }
    }

    public static List<String> split(String str, String reg) {
        return Arrays.stream(str.split(reg)).collect(Collectors.toList());
    }

    /**
     * 逗号字符串 变成 long 的list
     */
    public static List<Long> comma2LongList(String str) {
        if (DataUtils.isEmpty(str)) {
            return Lists.newArrayList();
        }
        return Splitter.on(",").omitEmptyStrings()
            .trimResults().splitToList(str.trim())
            .stream()
            .filter(NumberUtils::isDigits)
            .map(Long::parseLong).collect(Collectors.toList());
    }

    /**
     * 逗号字符串 变成 Integer 的list
     */
    public static List<Integer> comma2IntegerList(String str) {
        if (DataUtils.isEmpty(str)) {
            return Lists.newArrayList();
        }
        return Splitter.on(",").omitEmptyStrings()
            .trimResults().splitToList(str.trim())
            .stream()
            .filter(NumberUtils::isDigits)
            .map(Integer::parseInt).collect(Collectors.toList());
    }

    /**
     *
     */
    public static List<String> comma2StringList(String str) {
        if (DataUtils.isEmpty(str)) {
            return Lists.newArrayList();
        }
        return Splitter.on(",").omitEmptyStrings().trimResults().splitToList(str.trim());
    }

    public static <T> String joinCollection(Collection<T> originList) {
        return Joiner.on(",").join(originList);
    }

    /**
     * 解析 xml 节点数据
     *
     * @param str xml
     * @param key TrackingNumber 不带 <>
     * @return java.lang.String
     * @author wh
     * @date 2021/6/10
     */
    public static String parsXmlNode(String str, String key) {
        return parsXmlNode(str, key, 0);
    }

    /**
     * 解析 xml节点
     *
     * @param str   xml
     * @param key   xml节点
     * @param index 相同节点第几个
     * @return
     */
    public static String parsXmlNode(String str, String key, int index) {
        if (DataUtils.isEmpty(str) || DataUtils.isEmpty(key)) {
            return null;
        }
        key = String.join("", "<", key, ">");
        StringBuilder sb = new StringBuilder(key);
        sb.insert(1, "/");
        String regex = String.join("", key, "(.*?)", sb);
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(str);
        int count = 0;
        while (m.find()) {
            if (count == index) {
                return m.group(1);
            }
            ++count;
        }
        return "";
    }

    /**
     * 判断首字符是否为韩文
     *
     * @param s
     * @return
     */
    public static boolean checkKoreaChar(String s) {
        if (DataUtils.isEmpty(s)) {
            return false;
        }
        char c = s.charAt(0);
        return (c > 0x3130 && c < 0x318F)
            || (c >= 0xAC00 && c <= 0xD7A3);
    }

    /**
     * 去除字符串所有空格
     */
    public static String trimAnySpace(String str) {
        return str.replaceAll("\\s*", "");
    }

    /**
     * 首字母小写
     */
    public static String firstLowerCase(String str) {
        if (DataUtils.isEmpty(str)) {
            return "";
        }
        var chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 首字母大写
     */
    public static String firstUpperCase(String str) {
        if (DataUtils.isEmpty(str)) {
            return "";
        }
        char[] cs = str.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    public static boolean equalsIgnoreCase(final CharSequence str1, final CharSequence str2) {
        return org.apache.commons.lang3.StringUtils.equalsIgnoreCase(str1, str2);
    }

    /**
     * 判断所有字符串是否都相等，null和1个也是返回true
     * <p>
     * System.out.println(allEquals(null));        true
     * System.out.println(allEquals(null, "1"));   false
     * System.out.println(allEquals("1", null));   false
     * System.out.println(allEquals("1", "2"));    false
     * System.out.println(allEquals("1", "1"));    true
     * System.out.println(allEquals("1", "1", "3")); false
     * System.out.println(allEquals("1", "2", "3")); false
     * System.out.println(allEquals("1", "1", "1")); true
     */
    public static boolean allEquals(String... strings) {
        if (null == strings) {
            return true;
        }
        String one = strings[0];
        for (int i = 1; i < strings.length; i++) {
            if (!Objects.equals(one, strings[i])) {
                return false;
            }
        }
        return true;
    }

    public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
        return org.apache.commons.lang3.StringUtils.defaultIfEmpty(str, defaultStr);
    }

    public static <T extends CharSequence> T nullIfEmpty(final T str) {
        return org.apache.commons.lang3.StringUtils.defaultIfEmpty(str, null);
    }

    public static String substringAfter(final String str, final String separator) {
        return org.apache.commons.lang3.StringUtils.substringAfter(str, separator);
    }

    public static String toString(final Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static int indexOfChars(final String string, final String chars) {
        return indexOfChars(string, chars, 0);
    }

    public static int indexOfChars(final String string, final String chars, int startindex) {
        final int stringLen = string.length();
        final int charsLen = chars.length();
        if (startindex < 0) {
            startindex = 0;
        }
        for (int i = startindex; i < stringLen; i++) {
            final char c = string.charAt(i);
            for (int j = 0; j < charsLen; j++) {
                if (c == chars.charAt(j)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String replace(final String s, final String sub, final String with) {
        if (sub.isEmpty()) {
            return s;
        }
        int c = 0;
        int i = s.indexOf(sub, c);
        if (i == -1) {
            return s;
        }
        final int length = s.length();
        final StringBuilder sb = new StringBuilder(length + with.length());
        do {
            sb.append(s, c, i);
            sb.append(with);
            c = i + sub.length();
        }
        while ((i = s.indexOf(sub, c)) != -1);
        if (c < length) {
            sb.append(s, c, length);
        }
        return sb.toString();
    }

    public static boolean startsWithChar(final String s, final char c) {
        if (s.isEmpty()) {
            return false;
        }
        return s.charAt(0) == c;
    }

    public static boolean containsOnlyDigitsAndSigns(final CharSequence string) {
        final int size = string.length();
        for (int i = 0; i < size; i++) {
            final char c = string.charAt(i);
            if ((!CharUtil.isDigit(c)) && (c != '-') && (c != '+')) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsOnlyDigits(final CharSequence string) {
        final int size = string.length();
        for (int i = 0; i < size; i++) {
            final char c = string.charAt(i);
            if (!CharUtil.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String toHexString(final byte[] bytes) {
        final char[] chars = new char[bytes.length * 2];

        int i = 0;
        for (final byte b : bytes) {
            chars[i++] = CharUtil.int2hex((b & 0xF0) >> 4);
            chars[i++] = CharUtil.int2hex(b & 0x0F);
        }

        return new String(chars);
    }

    public static String decapitalize(final String name) {
        if (name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 &&
            Character.isUpperCase(name.charAt(1)) &&
            Character.isUpperCase(name.charAt(0))) {
            return name;
        }

        final char[] chars = name.toCharArray();
        final char c = chars[0];
        final char modifiedChar = Character.toLowerCase(c);
        if (modifiedChar == c) {
            return name;
        }
        chars[0] = modifiedChar;
        return new String(chars);
    }

    public static int count(final String source, final char c) {
        return count(source, c, 0);
    }

    public static int count(final String source, final char c, final int start) {
        int count = 0;
        int j = start;
        while (true) {
            final int i = source.indexOf(c, j);
            if (i == -1) {
                break;
            }
            count++;
            j = i + 1;
        }
        return count;
    }

    public static String repeat(final String source, int count) {
        final StringBuilder result = new StringBuilder(source.length() * count);
        while (count > 0) {
            result.append(source);
            count--;
        }
        return result.toString();
    }

    public static String repeat(final char c, final int count) {
        final char[] result = new char[count];
        for (int i = 0; i < count; i++) {
            result[i] = c;
        }
        return new String(result);
    }

}
