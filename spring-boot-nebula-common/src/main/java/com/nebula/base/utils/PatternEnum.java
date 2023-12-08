package com.nebula.base.utils;

import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author : wh
 * @date : 2023/11/18 13:59
 * @description:
 */
@AllArgsConstructor
@Getter
@ToString
public enum PatternEnum {

    /**
     * 英文字母 、数字和下划线
     */
    GENERAL(Pattern.compile("^\\w+$")),

    /**
     * 数字
     */
    NUMBERS(Pattern.compile("\\d+")),

    /**
     * 小写字母
     */
    LOWER_CASE(Pattern.compile("[a-z]+")),

    /**
     * 小写字母
     */
    UPPER_CASE(Pattern.compile("[A-Z]+")),

    /**
     * 英文字母
     */
    WORD(Pattern.compile("[a-zA-Z]+")),

    /**
     * 单个汉字
     */
    CHINESE(Pattern.compile("[\u4E00-\u9FFF]")),

    /**
     * 汉字
     */
    CHINESE_WORD(Pattern.compile("[\u4E00-\u9FFF]+")),

    /**
     * 电话
     */
    MOBILE(Pattern.compile("(?:0|86|\\+86)?1[3456789]\\d{9}")),

    /**
     * 身份证18位
     */
    CITIZEN_ID(Pattern.compile("[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}(\\d|X|x)")),

    /**
     * 邮箱
     */
    MAIL(Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")),

    /**
     * emoji表情
     */
    EMOJI(Pattern.compile("[\\x{10000}-\\x{10ffff}\ud800-\udfff]")),

    /**
     * emoji表情编码格式
     */
    EMOJI_DECODE(Pattern.compile("\\[\\[EMOJI:(.*?)\\]\\]")),

    /**
     * 正则分组符号格式
     */
    GROUP_VAR(Pattern.compile("\\$(\\d+)")),

    /**
     * 特殊符号(~!@#$%^&*()_+|<>,.?/:;'[]{}\)
     */
    SPEC_SYMBOL(Pattern.compile("[~!@#$%^&*()_+|<>,.?/:;'\\[\\]{}\"]+"));

    /**
     * 正则表达式
     */
    private Pattern pattern;

    public boolean isMatch(String input) {
        return pattern.matcher(input).matches();
    }


}
