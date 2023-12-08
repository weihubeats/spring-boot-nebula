package com.nebula.base.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : wh
 * @date : 2023/11/18 13:59
 * @description:
 */
public class RegexUtils {

    /**
     * 给定内容是否匹配正则表达式
     *
     * @param regex   正则表达式
     * @param content 内容
     * @return 正则为null或者""则不检查，返回true，内容为null返回false
     */
    public static boolean isMatch(String regex, String content) {
        return DataUtils.isEmpty(regex) || !DataUtils.isEmpty(content) && isMatch(Pattern.compile(regex), content);
    }

    /**
     * 给定内容是否匹配正则
     *
     * @param pattern 编译后的正则模式
     * @param content 内容
     * @return 正则为null或者""则不检查，返回true，内容为null返回false
     */
    public static boolean isMatch(Pattern pattern, String content) {
        return DataUtils.isEmpty(pattern) || !DataUtils.isEmpty(content) && pattern.matcher(content).matches();
    }

    /**
     * 删除匹配正则表达式的全部内容
     *
     * @param regex   正则表达式
     * @param content 内容
     * @return 删除后剩余的内容，如果正则为空或""则不检查返回输入的内容
     */
    public static String deleteAll(String regex, String content) {
        if (DataUtils.isEmpty(regex)) {
            return content;
        }
        return deleteAll(Pattern.compile(regex), content);
    }

    /**
     * 删除匹配正则表达式的全部内容
     *
     * @param pattern 编译后的正则模式
     * @param content 内容
     * @return 删除后剩余的内容，如果正则为空或""则不检查返回输入的内容
     */
    public static String deleteAll(Pattern pattern, String content) {
        if (DataUtils.isEmpty(pattern)) {
            return content;
        }
        return pattern.matcher(content).replaceAll(StringUtils.EMPTY);
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param regex   正则表达式
     * @param content 被查找的内容
     * @return 结果List列表
     */
    public static List<String> findAll(String regex, String content) {
        return findAll(regex, content, new ArrayList<>());
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param pattern 编译后的正则模式
     * @param content 被查找的内容
     * @return 结果List列表
     */
    public static List<String> findAll(Pattern pattern, String content) {
        return findAll(pattern, content, new ArrayList<>());
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param <T>        集合类型
     * @param regex      正则表达式
     * @param content    被查找的内容
     * @param collection 返回的集合类型
     * @return 结果集
     */
    public static <T extends Collection<String>> T findAll(String regex, String content, T collection) {
        if (DataUtils.isEmpty(regex)) {
            return null;
        }
        return findAll(Pattern.compile(regex), content, 0, collection);
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param <T>        集合类型
     * @param pattern    编译后的正则模式
     * @param content    被查找的内容
     * @param collection 返回的集合类型
     * @return 结果集
     */
    public static <T extends Collection<String>> T findAll(Pattern pattern, String content, T collection) {
        return findAll(pattern, content, 0, collection);
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param <T>        集合类型
     * @param regex      正则表达式
     * @param content    被查找的内容
     * @param group      正则的分组
     * @param collection 返回的集合类型
     * @return 结果集
     */
    public static <T extends Collection<String>> T findAll(String regex, String content, int group, T collection) {
        Pattern pattern = Pattern.compile(regex);
        return findAll(pattern, content, group, collection);
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param pattern    编译后的正则模式
     * @param content    被查找的内容
     * @param group      正则的分组
     * @param collection 返回的集合类型
     * @param <T>        集合类型
     * @return 结果集
     */
    public static <T extends Collection<String>> T findAll(Pattern pattern, String content, int group, T collection) {
        if (DataUtils.isEmpty(pattern) || DataUtils.isEmpty(content)) {
            return null;
        }
        if (null == collection) {
            throw new NullPointerException("Null collection param provided!");
        }
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            collection.add(matcher.group(group));
        }
        return collection;
    }

    /**
     * 替换内容中匹配的所有结果
     *
     * @param regex               正则表达式
     * @param content             被替换的内容
     * @param replacementTemplate 替换的文本模板，可以使用$1、$2、$3等变量提取正则匹配出的内容
     * @return 返回替换后结果
     */
    public static String replaceAll(String regex, String content, String replacementTemplate) {
        return replaceAll(content, Pattern.compile(regex), replacementTemplate);
    }

    /**
     * 替换内容中匹配的所有结果
     *
     * @param content             被替换的内容
     * @param pattern             编译后的正则模式
     * @param replacementTemplate 替换的文本模板，可以使用$1、$2、$3等变量提取正则匹配出的内容
     * @return 返回替换后结果
     */
    public static String replaceAll(String content, Pattern pattern, String replacementTemplate) {
        if (DataUtils.isEmpty(content)) {
            return content;
        }
        final Matcher matcher = pattern.matcher(content);
        if (DataUtils.isEmpty(matcher)) {
            return content;
        }
        final Set<String> varNums = findAll(PatternEnum.GROUP_VAR.getPattern(), replacementTemplate, 1, new HashSet<String>());
        if (DataUtils.isEmpty(varNums)) {
            return content;
        }
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = replacementTemplate;
            for (String var : varNums) {
                int group = Integer.parseInt(var);
                replacement = replacement.replace("$" + var, matcher.group(group));
            }
            matcher.appendReplacement(sb, (replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 替换内容中匹配的所有结果
     *
     * @param content  被替换的内容
     * @param regex    正则表达式
     * @param function 处理替换值的方法函数
     * @param group    正则的分组
     * @return 返回替换后结果
     */
    public static String replaceAll(String content, String regex, Function<String, String> function, int group) {
        Pattern pattern = Pattern.compile(regex);
        return replaceAll(content, pattern, function, group);
    }

    /**
     * 替换内容中匹配的所有结果
     *
     * @param content  被替换的内容
     * @param pattern  正则
     * @param function 处理替换值的方法函数
     * @param group    正则的分组
     * @return 返回替换后结果
     */
    public static String replaceAll(String content, Pattern pattern, Function<String, String> function, int group) {
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                String replace = function.apply(matcher.group(group));
                matcher.appendReplacement(sb, replace);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 替换内容中匹配的所有结果
     *
     * @param content  被替换的内容
     * @param pattern  正则
     * @param function 处理替换值的方法函数
     * @return 返回替换后结果
     */
    public static String replaceAll(String content, Pattern pattern, Function<String, String> function) {
        return replaceAll(content, pattern, function, 0);
    }

    /**
     * 将星号字符转为可正则匹配的字段, * 表示任何长度的[0-9][a-z][A-Z]
     *
     * 暂时不考虑 星不连续问题
     * **aaa √
     * aaa** √
     * a**a √
     * *aa* ×
     *
     * @param str 匹配原始字段
     * @return 符合正则规则的字段
     */
    public static String asteriskMatching(String str) {

        var firstIndex = str.indexOf("*");
        var lastIndexOf = str.lastIndexOf("*");
        var length = str.length() - 1;

        int fuzzyLength = lastIndexOf - firstIndex + 1;
        //todo 后续再重构兼容星不连续类型字段
        if (fuzzyLength == length + 1) {
            throw new RuntimeException("暂时不兼容该类型字段");
        }

        String fuzzyPattern = "(\\\\w)*";
        var patterStr = str.replaceAll("\\*".repeat(Math.max(0, fuzzyLength)), fuzzyPattern);
        return "^" + patterStr + "$";
    }

}
