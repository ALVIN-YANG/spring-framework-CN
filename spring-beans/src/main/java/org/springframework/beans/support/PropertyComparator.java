// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”），您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“现状”提供，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途。
* 请参阅许可证了解具体规定权限和限制。*/
package org.springframework.beans.support;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * PropertyComparator 用于比较两个对象，
 * 通过 BeanWrapper 评估指定的对象属性。
 *
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 19.05.2003
 * @param <T> 此比较器可能比较的对象的类型
 * @see org.springframework.beans.BeanWrapper
 */
public class PropertyComparator<T> implements Comparator<T> {

    protected final Log logger = LogFactory.getLog(getClass());

    private final SortDefinition sortDefinition;

    /**
     * 为给定的 SortDefinition 创建一个新的 PropertyComparator。
     * @see MutableSortDefinition
     */
    public PropertyComparator(SortDefinition sortDefinition) {
        this.sortDefinition = sortDefinition;
    }

    /**
     * 为给定的设置创建一个 PropertyComparator。
     * @param property 要比较的属性
     * @param ignoreCase 是否忽略字符串值中的大小写
     * @param ascending 是否按升序（true）或降序（false）排序
     */
    public PropertyComparator(String property, boolean ignoreCase, boolean ascending) {
        this.sortDefinition = new MutableSortDefinition(property, ignoreCase, ascending);
    }

    /**
     * 返回此比较器使用的排序定义。
     */
    public final SortDefinition getSortDefinition() {
        return this.sortDefinition;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(T o1, T o2) {
        Object v1 = getPropertyValue(o1);
        Object v2 = getPropertyValue(o2);
        if (this.sortDefinition.isIgnoreCase() && (v1 instanceof String text1) && (v2 instanceof String text2)) {
            v1 = text1.toLowerCase();
            v2 = text2.toLowerCase();
        }
        int result;
        // 将具有 null 属性的对象放置在排序结果末尾。
        try {
            if (v1 != null) {
                result = (v2 != null ? ((Comparable<Object>) v1).compareTo(v2) : -1);
            } else {
                result = (v2 != null ? 1 : 0);
            }
        } catch (RuntimeException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not sort objects [" + o1 + "] and [" + o2 + "]", ex);
            }
            return 0;
        }
        return (this.sortDefinition.isAscending() ? result : -result);
    }

    /**
     * 获取给定对象 SortDefinition 的属性值。
     * @param obj 要获取属性值的对象
     * @return 属性值
     */
    @Nullable
    private Object getPropertyValue(Object obj) {
        // 如果嵌套属性无法读取，则直接返回null
        // （类似于JSTL EL）。如果该属性不存在于
        // 首先，允许异常通过。
        try {
            BeanWrapperImpl beanWrapper = new BeanWrapperImpl(false);
            beanWrapper.setWrappedInstance(obj);
            return beanWrapper.getPropertyValue(this.sortDefinition.getProperty());
        } catch (BeansException ex) {
            logger.debug("PropertyComparator could not access property - treating as null for sorting", ex);
            return null;
        }
    }

    /**
     * 按照给定的排序定义对给定的列表进行排序。
     * <p>注意：包含的对象必须以bean属性的形式提供给定的属性，即getXXX方法。
     * @param source 输入列表
     * @param sortDefinition 排序参数
     * @throws java.lang.IllegalArgumentException 如果缺少propertyName，将抛出异常
     */
    public static void sort(List<?> source, SortDefinition sortDefinition) throws BeansException {
        if (StringUtils.hasText(sortDefinition.getProperty())) {
            source.sort(new PropertyComparator<>(sortDefinition));
        }
    }

    /**
     * 按照给定的排序定义对给定源进行排序。
     * <p>注意：包含的对象必须以bean属性的形式提供给定的属性，即一个getXXX方法。
     * @param source 输入源
     * @param sortDefinition 排序所依据的参数
     * @throws java.lang.IllegalArgumentException 如果缺少propertyName异常
     */
    public static void sort(Object[] source, SortDefinition sortDefinition) throws BeansException {
        if (StringUtils.hasText(sortDefinition.getProperty())) {
            Arrays.sort(source, new PropertyComparator<>(sortDefinition));
        }
    }
}
