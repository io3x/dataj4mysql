package com.github.io3x.app.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {
    /**
     * 首字母变小写
     */
    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     */
    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 字符串为 null 或者内部字符全部为 ' ' '\t' '\n' '\r' 这四类字符时返回 true
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            switch (str.charAt(i)) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    // case '\b':
                    // case '\f':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean notBlank(String str) {
        return !isBlank(str);
    }

    public static boolean notBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        for (String str : strings) {
            if (isBlank(str)) {
                return false;
            }
        }
        return true;
    }

    public static boolean notNull(Object... paras) {
        if (paras == null) {
            return false;
        }
        for (Object obj : paras) {
            if (obj == null) {
                return false;
            }
        }
        return true;
    }

    public static String toCamelCase(String stringWithUnderline) {
        if (stringWithUnderline.indexOf('_') == -1) {
            return stringWithUnderline;
        }

        stringWithUnderline = stringWithUnderline.toLowerCase();
        char[] fromArray = stringWithUnderline.toCharArray();
        char[] toArray = new char[fromArray.length];
        int j = 0;
        for (int i=0; i<fromArray.length; i++) {
            if (fromArray[i] == '_') {
                // 当前字符为下划线时，将指针后移一位，将紧随下划线后面一个字符转成大写并存放
                i++;
                if (i < fromArray.length) {
                    toArray[j++] = Character.toUpperCase(fromArray[i]);
                }
            }
            else {
                toArray[j++] = fromArray[i];
            }
        }
        return new String(toArray, 0, j);
    }

    public static String join(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (String s : stringArray) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String join(String[] stringArray, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<stringArray.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(stringArray[i]);
        }
        return sb.toString();
    }



    public static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public static String getRandomUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    public static boolean areNotEmpty(String... strings) {
        if (strings == null || strings.length == 0)
            return false;

        for (String string : strings) {
            if (string == null || "".equals(string) || "null".equals(string)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 不是空数据，注意：空格不是空数据
     *
     * @param string
     * @return
     */
    public static boolean isNotEmpty(String string) {
        return string != null && !string.equals("");
    }


    /**
     * 确保不是空白字符串
     *
     * @param o
     * @return
     */
    public static boolean isNotBlank(Object o) {
        return o == null ? false : notBlank(o.toString());
    }


    /**
     * 字符串是否匹配某个正则
     *
     * @param string
     * @param regex
     * @return
     */
    public static boolean match(String string, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }

    /**
     * 这个字符串是否是全是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str == null)
            return false;
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57)
                return false;
        }
        return true;
    }

    /**
     * 是否是邮件的字符串
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        return Pattern.matches("\\w+@(\\w+.)+[a-z]{2,3}", email);
    }


    /**
     * 是否是中国地区手机号码
     *
     * @param phoneNumber
     * @return
     */
    public static boolean isMobileNumber(String phoneNumber) {
        return Pattern.matches("^(1[3,4,5,7,8,9])\\d{9}$", phoneNumber);
    }


    /**
     * 生成一个新的UUID
     *
     * @return
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * 去除特殊字符
     *
     * @param string
     * @return
     */
    public static String clearSpecialCharacter(String string) {
        if (isBlank(string)) {
            return string;
        }

        /**
         P：标点字符；
         L：字母；
         M：标记符号（一般不会单独出现）；
         Z：分隔符（比如空格、换行等）；
         S：符号（比如数学符号、货币符号等）；
         N：数字（比如阿拉伯数字、罗马数字等）；
         C：其他字符
         */
//        return string.replaceAll("[\\pP\\pZ\\pM\\pC]", "");
        return string.replaceAll("[\\\\\'\"\\/\f\n\r\t]", "");
    }


    /**
     * 把字符串拆分成一个set
     *
     * @param src
     * @param regex
     * @return
     */
    public static Set<String> splitToSet(String src, String regex) {
        if (src == null) {
            return null;
        }

        String[] strings = src.split(regex);
        Set<String> set = new HashSet<>();
        for (String table : strings) {
            if (StrUtils.isBlank(table)) {
                continue;
            }
            set.add(table.trim());
        }
        return set;
    }

}
