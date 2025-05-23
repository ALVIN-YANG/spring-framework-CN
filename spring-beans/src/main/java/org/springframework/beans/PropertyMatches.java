// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权和限制。*/
package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 用于计算属性匹配的辅助类，根据可配置的距离计算。提供潜在匹配列表以及生成错误信息的简单方式。适用于 Java Bean 属性和字段。
 *
 * <p>主要用于框架内部，特别是绑定设施。
 *
 * @author Alef Arendsen
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see #forProperty(String, Class)
 * @see #forField(String, Class)
 */
public abstract class PropertyMatches {

    /**
     * 默认最大属性距离：2。
     */
    public static final int DEFAULT_MAX_DISTANCE = 2;

    // 静态工厂方法
    /**
     * 为给定的 Bean 属性创建 PropertyMatches。
     * @param propertyName 要查找可能的匹配项的属性名称
     * @param beanClass 要搜索匹配项的 Bean 类
     */
    public static PropertyMatches forProperty(String propertyName, Class<?> beanClass) {
        return forProperty(propertyName, beanClass, DEFAULT_MAX_DISTANCE);
    }

    /**
     * 为给定的bean属性创建PropertyMatches。
     * @param propertyName 要查找可能匹配的属性的名称
     * @param beanClass 用于搜索匹配的bean类
     * @param maxDistance 允许的匹配属性的最大距离
     */
    public static PropertyMatches forProperty(String propertyName, Class<?> beanClass, int maxDistance) {
        return new BeanPropertyMatches(propertyName, beanClass, maxDistance);
    }

    /**
     * 为给定的字段属性创建 PropertyMatches。
     * @param propertyName 要查找可能匹配的字段名称
     * @param beanClass 用于搜索匹配的 Bean 类
     */
    public static PropertyMatches forField(String propertyName, Class<?> beanClass) {
        return forField(propertyName, beanClass, DEFAULT_MAX_DISTANCE);
    }

    /**
     * 为给定的字段属性创建 PropertyMatches。
     * @param propertyName 要查找可能匹配的字段名称
     * @param beanClass 要搜索匹配的 Bean 类
     * @param maxDistance 允许的匹配属性最大距离
     */
    public static PropertyMatches forField(String propertyName, Class<?> beanClass, int maxDistance) {
        return new FieldPropertyMatches(propertyName, beanClass, maxDistance);
    }

    // 实例状态
    private final String propertyName;

    private final String[] possibleMatches;

    /**
     * 为给定的属性和可能的匹配项创建一个新的 PropertyMatches 实例。
     */
    private PropertyMatches(String propertyName, String[] possibleMatches) {
        this.propertyName = propertyName;
        this.possibleMatches = possibleMatches;
    }

    /**
     * 返回请求的属性名称。
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * 返回计算得到的可能匹配项。
     */
    public String[] getPossibleMatches() {
        return this.possibleMatches;
    }

    /**
     * 为给定的无效属性名称构建一个错误消息，
     * 指示可能的属性匹配。
     */
    public abstract String buildErrorMessage();

    // 子类实现支持
    protected void appendHintMessage(StringBuilder msg) {
        msg.append("Did you mean ");
        for (int i = 0; i < this.possibleMatches.length; i++) {
            msg.append('\'');
            msg.append(this.possibleMatches[i]);
            if (i < this.possibleMatches.length - 2) {
                msg.append("', ");
            } else if (i == this.possibleMatches.length - 2) {
                msg.append("', or ");
            }
        }
        msg.append("'?");
    }

    /**
     * 根据Levenshtein算法计算给定两个字符串之间的距离。
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return 距离值
     */
    private static int calculateStringDistance(String s1, String s2) {
        if (s1.isEmpty()) {
            return s2.length();
        }
        if (s2.isEmpty()) {
            return s1.length();
        }
        int[][] d = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= s2.length(); j++) {
                int cost;
                char c2 = s2.charAt(j - 1);
                if (c1 == c2) {
                    cost = 0;
                } else {
                    cost = 1;
                }
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
            }
        }
        return d[s1.length()][s2.length()];
    }

    // 具体子类
    private static class BeanPropertyMatches extends PropertyMatches {

        public BeanPropertyMatches(String propertyName, Class<?> beanClass, int maxDistance) {
            super(propertyName, calculateMatches(propertyName, BeanUtils.getPropertyDescriptors(beanClass), maxDistance));
        }

        /**
         * 为给定的属性和类生成可能的属性替代方案。
         * 内部使用 {@code getStringDistance} 方法，该方法又使用
         * Levenshtein 算法来确定两个字符串之间的距离。
         * @param descriptors 要搜索的 JavaBeans 属性描述符
         * @param maxDistance 可接受的最大距离
         */
        private static String[] calculateMatches(String name, PropertyDescriptor[] descriptors, int maxDistance) {
            List<String> candidates = new ArrayList<>();
            for (PropertyDescriptor pd : descriptors) {
                if (pd.getWriteMethod() != null) {
                    String possibleAlternative = pd.getName();
                    if (calculateStringDistance(name, possibleAlternative) <= maxDistance) {
                        candidates.add(possibleAlternative);
                    }
                }
            }
            Collections.sort(candidates);
            return StringUtils.toStringArray(candidates);
        }

        @Override
        public String buildErrorMessage() {
            StringBuilder msg = new StringBuilder(160);
            msg.append("Bean property '").append(getPropertyName()).append("' is not writable or has an invalid setter method. ");
            if (!ObjectUtils.isEmpty(getPossibleMatches())) {
                appendHintMessage(msg);
            } else {
                msg.append("Does the parameter type of the setter match the return type of the getter?");
            }
            return msg.toString();
        }
    }

    private static class FieldPropertyMatches extends PropertyMatches {

        public FieldPropertyMatches(String propertyName, Class<?> beanClass, int maxDistance) {
            super(propertyName, calculateMatches(propertyName, beanClass, maxDistance));
        }

        private static String[] calculateMatches(final String name, Class<?> clazz, final int maxDistance) {
            final List<String> candidates = new ArrayList<>();
            ReflectionUtils.doWithFields(clazz, field -> {
                String possibleAlternative = field.getName();
                if (calculateStringDistance(name, possibleAlternative) <= maxDistance) {
                    candidates.add(possibleAlternative);
                }
            });
            Collections.sort(candidates);
            return StringUtils.toStringArray(candidates);
        }

        @Override
        public String buildErrorMessage() {
            StringBuilder msg = new StringBuilder(80);
            msg.append("Bean property '").append(getPropertyName()).append("' has no matching field.");
            if (!ObjectUtils.isEmpty(getPossibleMatches())) {
                msg.append(' ');
                appendHintMessage(msg);
            }
            return msg.toString();
        }
    }
}
