// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans;

import org.springframework.lang.Nullable;

/**
 * 用于根据 {@link PropertyAccessor} 接口执行 Bean 属性访问的类使用的实用方法。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 */
public abstract class PropertyAccessorUtils {

    /**
     * 返回给定属性路径的实际属性名称。
     * @param propertyPath 要确定属性名称的属性路径（可以包含属性键，例如用于指定映射条目）
     * @return 实际属性名称，不带任何键元素
     */
    public static String getPropertyName(String propertyPath) {
        int separatorIndex = (propertyPath.endsWith(PropertyAccessor.PROPERTY_KEY_SUFFIX) ? propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) : -1);
        return (separatorIndex != -1 ? propertyPath.substring(0, separatorIndex) : propertyPath);
    }

    /**
     * 检查给定的属性路径是否指示索引属性或嵌套属性。
     * @param propertyPath 要检查的属性路径
     * @return 路径是否指示索引属性或嵌套属性
     */
    public static boolean isNestedOrIndexedProperty(@Nullable String propertyPath) {
        if (propertyPath == null) {
            return false;
        }
        for (int i = 0; i < propertyPath.length(); i++) {
            char ch = propertyPath.charAt(i);
            if (ch == PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR || ch == PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
                return true;
            }
        }
        return false;
    }

    /**
     * 确定给定属性路径中的第一个嵌套属性分隔符，忽略键中的点（如"map[my.key]"）。
     * @param propertyPath 要检查的属性路径
     * @return 嵌套属性分隔符的索引，如果没有则返回-1
     */
    public static int getFirstNestedPropertySeparatorIndex(String propertyPath) {
        return getNestedPropertySeparatorIndex(propertyPath, false);
    }

    /**
     * 确定给定属性路径中的第一个嵌套属性分隔符，忽略键中的点（例如 "map[my.key]"）。
     * @param propertyPath 要检查的属性路径
     * @return 嵌套属性分隔符的索引，如果没有则返回 -1
     */
    public static int getLastNestedPropertySeparatorIndex(String propertyPath) {
        return getNestedPropertySeparatorIndex(propertyPath, true);
    }

    /**
     * 确定给定属性路径中的第一个（或最后一个）嵌套属性分隔符，忽略键中的点（如"map[my.key]"）。
     * @param propertyPath 要检查的属性路径
     * @param last 是否返回最后一个分隔符而不是第一个
     * @return 嵌套属性分隔符的索引，如果没有则返回-1
     */
    private static int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
        boolean inKey = false;
        int length = propertyPath.length();
        int i = (last ? length - 1 : 0);
        while (last ? i >= 0 : i < length) {
            switch(propertyPath.charAt(i)) {
                case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
                case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
                    inKey = !inKey;
                    break;
                case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
                    if (!inKey) {
                        return i;
                    }
            }
            if (last) {
                i--;
            } else {
                i++;
            }
        }
        return -1;
    }

    /**
     * 确定给定的已注册路径是否与给定的属性路径匹配，
     * 无论是指示属性本身还是属性的一个索引元素。
     * @param propertyPath 属性路径（通常不带索引）
     * @param registeredPath 已注册路径（可能带索引）
     * @return 路径是否匹配
     */
    public static boolean matchesProperty(String registeredPath, String propertyPath) {
        if (!registeredPath.startsWith(propertyPath)) {
            return false;
        }
        if (registeredPath.length() == propertyPath.length()) {
            return true;
        }
        if (registeredPath.charAt(propertyPath.length()) != PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
            return false;
        }
        return (registeredPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR, propertyPath.length() + 1) == registeredPath.length() - 1);
    }

    /**
     * 确定给定属性路径的规范名称。
     * 从映射键中移除周围的引号：<br>
     * {@code map['key']} &rarr; {@code map[key]}<br>
     * {@code map["key"]} &rarr; {@code map[key]}
     * @param propertyName 象棋属性路径
     * @return 属性路径的规范表示
     */
    public static String canonicalPropertyName(@Nullable String propertyName) {
        if (propertyName == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(propertyName);
        int searchIndex = 0;
        while (searchIndex != -1) {
            int keyStart = sb.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
            searchIndex = -1;
            if (keyStart != -1) {
                int keyEnd = sb.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length());
                if (keyEnd != -1) {
                    String key = sb.substring(keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length(), keyEnd);
                    if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
                        sb.delete(keyStart + 1, keyStart + 2);
                        sb.delete(keyEnd - 2, keyEnd - 1);
                        keyEnd = keyEnd - 2;
                    }
                    searchIndex = keyEnd + PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
                }
            }
        }
        return sb.toString();
    }

    /**
     * 确定给定属性路径的规范名称。
     * @param propertyNames 属性路径的 Bean 属性（作为数组）
     * @return 属性路径的规范表示形式（与相同大小的数组）
     * @see #canonicalPropertyName(String)
     */
    @Nullable
    public static String[] canonicalPropertyNames(@Nullable String[] propertyNames) {
        if (propertyNames == null) {
            return null;
        }
        String[] result = new String[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            result[i] = canonicalPropertyName(propertyNames[i]);
        }
        return result;
    }
}
