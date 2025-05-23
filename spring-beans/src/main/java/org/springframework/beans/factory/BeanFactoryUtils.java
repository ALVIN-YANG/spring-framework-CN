// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 便捷方法操作bean工厂，特别是操作
 * {@link ListableBeanFactory} 接口。
 *
 * <p>返回bean数量、bean名称或bean实例，
 * 考虑到bean工厂的嵌套层次结构（与定义在
 * BeanFactory接口上的方法相比，ListableBeanFactory接口上定义的方法不这样做）。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 04.07.2003
 */
public abstract class BeanFactoryUtils {

    /**
     * 用于生成 bean 名称的分隔符。如果类名或父类名不唯一，将附加 "#1"、"#2" 等，直到名称变得唯一。
     */
    public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";

    /**
     * 从带有工厂Bean前缀的名称缓存到不带解引用的纯名称。
     * @since 5.1
     * @see BeanFactory#FACTORY_BEAN_PREFIX
     */
    private static final Map<String, String> transformedBeanNameCache = new ConcurrentHashMap<>();

    /**
     * 返回给定的名称是否是工厂引用解除（以工厂引用解除前缀开头）。
     * @param name bean的名称
     * @return 给定的名称是否是工厂引用解除
     * @see BeanFactory#FACTORY_BEAN_PREFIX
     */
    public static boolean isFactoryDereference(@Nullable String name) {
        return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
    }

    /**
     * 返回实际的 Bean 名称，去除工厂引用前缀（如果有的话，同时去除重复的工厂前缀）。
     * @param name Bean 的名称
     * @return 转换后的名称
     * @see BeanFactory#FACTORY_BEAN_PREFIX
     */
    public static String transformedBeanName(String name) {
        Assert.notNull(name, "'name' must not be null");
        if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            return name;
        }
        return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
            do {
                beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
            } while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
            return beanName;
        });
    }

    /**
     * 返回给定的名称是否是一个由默认命名策略生成的bean名称（包含"#..."部分）。
     * @param name bean的名称
     * @return 是否给定的名称是一个生成的bean名称
     * @see #GENERATED_BEAN_NAME_SEPARATOR
     * @see org.springframework.beans.factory.support.BeanDefinitionReaderUtils#generateBeanName
     * @see org.springframework.beans.factory.support.DefaultBeanNameGenerator
     */
    public static boolean isGeneratedBeanName(@Nullable String name) {
        return (name != null && name.contains(GENERATED_BEAN_NAME_SEPARATOR));
    }

    /**
     * 从给定的（可能已生成的）bean名称中提取“原始”bean名称，
     * 排除可能添加的任何“#...”后缀，这些后缀可能是为了确保唯一性。
     * @param name 可能已生成的bean名称
     * @return 原始bean名称
     * @see #GENERATED_BEAN_NAME_SEPARATOR
     */
    public static String originalBeanName(String name) {
        Assert.notNull(name, "'name' must not be null");
        int separatorIndex = name.indexOf(GENERATED_BEAN_NAME_SEPARATOR);
        return (separatorIndex != -1 ? name.substring(0, separatorIndex) : name);
    }

    // 获取 Bean 名称
    /**
     * 计算此工厂参与的任何层次结构中的所有豆子数量。
     * 包括祖先豆子工厂的计数。
     * <p>被“覆盖”（在具有相同名称的子工厂中指定）的豆子只计算一次。
     * @param lbf 豆子工厂
     * @return 包括在祖先工厂中定义的豆子在内的豆子总数
     * @see #beanNamesIncludingAncestors
     */
    public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
        return beanNamesIncludingAncestors(lbf).length;
    }

    /**
     * 返回工厂中所有的bean名称，包括祖先工厂。
     * @param lbf the bean factory
     * @return 匹配的bean名称数组，如果没有则返回空数组
     * @see #beanNamesForTypeIncludingAncestors
     */
    public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
        return beanNamesForTypeIncludingAncestors(lbf, Object.class);
    }

    /**
     * 获取给定类型的所有bean名称，包括在祖先工厂中定义的。如果存在重写的bean定义，将返回唯一的名称。
     * <p>考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果FactoryBean创建的对象不匹配，则将直接匹配原始的FactoryBean。
     * <p>此版本的{@code beanNamesForTypeIncludingAncestors}会自动包含原型和FactoryBeans。
     * @param lbf bean工厂
     * @param type 必须匹配的bean类型（作为{@code ResolvableType}）
     * @return 匹配的bean名称数组，如果没有则返回空数组
     * @since 4.2
     * @see ListableBeanFactory#getBeanNamesForType(ResolvableType)
     */
    public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, ResolvableType type) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type);
        if (lbf instanceof HierarchicalBeanFactory hbf) {
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
                String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type);
                result = mergeNamesWithParent(result, parentResult, hbf);
            }
        }
        return result;
    }

    /**
     * 获取给定类型的所有Bean名称，包括在祖先工厂中定义的Bean。在存在覆盖的Bean定义的情况下，将返回唯一的名称。
     * <p>如果设置了“allowEagerInit”标志，则会考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果FactoryBean创建的对象不匹配，则将直接将原始FactoryBean与类型进行匹配。如果“allowEagerInit”未设置，则只检查原始FactoryBeans（这不需要初始化每个FactoryBean）。
     * @param lbf Bean工厂
     * @param type Bean必须匹配的类型（作为{@code ResolvableType}）
     * @param includeNonSingletons 是否包括原型或作用域Bean，或者仅包括单例Bean（也适用于FactoryBeans）
     * @param allowEagerInit 是否初始化类型检查中的<i>懒加载单例</i>和<i>由FactoryBeans创建的对象</i>（或通过具有“factory-bean”引用的工厂方法）。请注意，FactoryBeans需要被预先初始化以确定其类型：因此请注意，传递“true”给此标志将初始化FactoryBeans和“factory-bean”引用。
     * @return 匹配的Bean名称数组，如果没有则返回空数组
     * @since 5.2
     * @see ListableBeanFactory#getBeanNamesForType(ResolvableType, boolean, boolean)
     */
    public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        if (lbf instanceof HierarchicalBeanFactory hbf) {
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
                String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type, includeNonSingletons, allowEagerInit);
                result = mergeNamesWithParent(result, parentResult, hbf);
            }
        }
        return result;
    }

    /**
     * 获取给定类型的所有Bean名称，包括在祖先工厂中定义的。在存在覆盖的Bean定义的情况下，将返回唯一的名称。
     * <p>考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果FactoryBean创建的对象不匹配，将针对该原始FactoryBean与类型进行匹配。
     * <p>此版本的{@code beanNamesForTypeIncludingAncestors}会自动包括原型和FactoryBeans。
     * @param lbf Bean工厂
     * @param type Bean必须匹配的类型（作为{@code Class}）
     * @return 匹配的Bean名称数组，如果没有则返回空数组
     * @see ListableBeanFactory#getBeanNamesForType(Class)
     */
    public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type);
        if (lbf instanceof HierarchicalBeanFactory hbf) {
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
                String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type);
                result = mergeNamesWithParent(result, parentResult, hbf);
            }
        }
        return result;
    }

    /**
     * 获取给定类型的所有bean名称，包括在祖先工厂中定义的名称。在覆盖bean定义的情况下，将返回唯一的名称。
     * <p>如果设置了"allowEagerInit"标志，将考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果FactoryBean创建的对象不匹配，则将直接匹配FactoryBean本身。如果"allowEagerInit"未设置，则仅检查原始FactoryBeans（无需初始化每个FactoryBean）。
     * @param lbf bean工厂
     * @param includeNonSingletons 是否包括原型或作用域bean（也适用于FactoryBeans），或仅包括单例bean
     * @param allowEagerInit 是否初始化类型检查时的<i>懒加载单例</i>和<i>由FactoryBeans创建的对象</i>（或通过具有"factory-bean"引用的工厂方法）。请注意，为了确定FactoryBeans的类型，需要急切地初始化它们：因此，请注意，传递"true"给此标志将初始化FactoryBeans和"factory-bean"引用。
     * @param type bean必须匹配的类型
     * @return 匹配的bean名称数组，如果没有则返回空数组
     * @see ListableBeanFactory#getBeanNamesForType(Class, boolean, boolean)
     */
    public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        if (lbf instanceof HierarchicalBeanFactory hbf) {
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
                String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type, includeNonSingletons, allowEagerInit);
                result = mergeNamesWithParent(result, parentResult, hbf);
            }
        }
        return result;
    }

    /**
     * 获取所有其 {@code Class} 具有提供 {@link Annotation} 类型的 Bean 名称，包括在祖先工厂中定义的，而不创建任何 Bean 实例。如果存在覆盖的 Bean 定义，将返回唯一的名称。
     * @param lbf Bean 工厂
     * @param annotationType 要查找的注解类型
     * @return 匹配的 Bean 名称数组，如果没有则返回空数组
     * @since 5.0
     * @see ListableBeanFactory#getBeanNamesForAnnotation(Class)
     */
    public static String[] beanNamesForAnnotationIncludingAncestors(ListableBeanFactory lbf, Class<? extends Annotation> annotationType) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForAnnotation(annotationType);
        if (lbf instanceof HierarchicalBeanFactory hbf) {
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
                String[] parentResult = beanNamesForAnnotationIncludingAncestors(pbf, annotationType);
                result = mergeNamesWithParent(result, parentResult, hbf);
            }
        }
        return result;
    }

    // Bean实例检索
    /**
     * 返回给定类型或子类型的所有Bean，如果当前BeanFactory是HierarchicalBeanFactory，还会收集祖先BeanFactory中定义的Bean。返回的Map将只包含此类型的Bean。
     * <p>考虑由FactoryBean创建的对象，这意味着FactoryBean将被初始化。如果FactoryBean创建的对象不匹配，则将直接匹配FactoryBean本身与类型。
     * <p><b>注意：</b>具有相同名称的Bean将在'最低'工厂级别中优先，即这些Bean将从它们被找到的最低工厂返回，从而隐藏祖先工厂中相应的Bean。此功能允许通过在子工厂中显式选择相同的Bean名称来'替换'Bean；此时，祖先工厂中的Bean将不可见，甚至在按类型查找时也是如此。
     * @param lbf BeanFactory
     * @param type 要匹配的Bean类型
     * @return 匹配的Bean实例的Map，如果没有则返回空Map
     * @throws BeansException 如果无法创建Bean
     * @see ListableBeanFactory#getBeansOfType(Class)
     */
    public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type) throws BeansException {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> result = new LinkedHashMap<>(4);
        result.putAll(lbf.getBeansOfType(type));
        if (lbf instanceof HierarchicalBeanFactory hbf) {
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
                Map<String, T> parentResult = beansOfTypeIncludingAncestors(pbf, type);
                parentResult.forEach((beanName, beanInstance) -> {
                    if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
                        result.put(beanName, beanInstance);
                    }
                });
            }
        }
        return result;
    }

    /**
     * 返回给定类型或子类型的所有Bean，如果当前Bean工厂是HierarchicalBeanFactory，则还会获取祖先Bean工厂中定义的Bean。返回的Map将只包含此类型的Bean。
     * <p>如果设置了"allowEagerInit"标志，则考虑由FactoryBean创建的对象，这意味着FactoryBean将被初始化。如果FactoryBean创建的对象不匹配，则将直接将原始FactoryBean与类型进行匹配。如果没有设置"allowEagerInit"，则只检查原始FactoryBean（这不需要初始化每个FactoryBean）。
     * <p><b>注意：</b>同一名称的Bean将在'最低'工厂级别中具有优先权，即这些Bean将从它们被找到的最低工厂返回，隐藏祖先工厂中相应的Bean。此功能允许通过在子工厂中显式选择相同的Bean名称来'替换'Bean；此时，祖先工厂中的Bean将不可见，即使对于按类型查找也是如此。
     * @param lbf Bean工厂
     * @param type 要匹配的Bean类型
     * @param includeNonSingletons 是否包括原型或作用域Bean，以及仅包括单例Bean（也适用于FactoryBean）
     * @param allowEagerInit 是否初始化<i>懒加载单例</i>和<i>由FactoryBean创建的对象</i>（或通过具有"factory-bean"引用的工厂方法）以进行类型检查。请注意，FactoryBean需要被急切初始化以确定其类型：因此请注意，将此标志传递为"true"将初始化FactoryBean和"factory-bean"引用。
     * @return 匹配的Bean实例的Map，如果没有则返回空Map
     * @throws BeansException 如果无法创建Bean
     * @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
     */
    public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> result = new LinkedHashMap<>(4);
        result.putAll(lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit));
        if (lbf instanceof HierarchicalBeanFactory hbf) {
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
                Map<String, T> parentResult = beansOfTypeIncludingAncestors(pbf, type, includeNonSingletons, allowEagerInit);
                parentResult.forEach((beanName, beanInstance) -> {
                    if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
                        result.put(beanName, beanInstance);
                    }
                });
            }
        }
        return result;
    }

    /**
     * 返回给定类型或子类型的单个Bean，如果当前Bean工厂是HierarchicalBeanFactory，则还会获取在祖先Bean工厂中定义的Bean。当预期只有一个Bean且不关心Bean名称时，这是一个有用的便捷方法。
     * <p>考虑由FactoryBean创建的对象，这意味着FactoryBean将被初始化。如果FactoryBean创建的对象不匹配，则将直接将原始FactoryBean与类型进行匹配。
     * <p>此版本的{@code beanOfTypeIncludingAncestors}自动包括原型和FactoryBean。
     * <p><b>注意：</b>同一名称的Bean将在'最低'工厂级别中具有优先权，即此类Bean将来自它们被找到的最低工厂，从而隐藏祖先工厂中的相应Bean。此功能允许通过在子工厂中显式选择相同的Bean名称来'替换'Bean；此时，祖先工厂中的Bean将不可见，即使是对于类型查找也不例外。
     * @param lbf Bean工厂
     * @param type 要匹配的Bean类型
     * @return 匹配的Bean实例
     * @throws NoSuchBeanDefinitionException 如果未找到给定类型的任何Bean
     * @throws NoUniqueBeanDefinitionException 如果找到多个给定类型的Bean
     * @throws BeansException 如果无法创建Bean
     * @see #beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
     */
    public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type) throws BeansException {
        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type);
        return uniqueBean(type, beansOfType);
    }

    /**
     * 返回给定类型或子类型的单个Bean，如果当前Bean工厂是HierarchicalBeanFactory，还会检索祖先Bean工厂中定义的Bean。当期望单个Bean且不关心Bean名称时，这是一个有用的便捷方法。
     * <p>如果设置了"allowEagerInit"标志，则考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果FactoryBean创建的对象不匹配，则将直接匹配FactoryBean本身。如果未设置"allowEagerInit"，则只检查原始FactoryBeans（这不需要初始化每个FactoryBean）。
     * <p><b>注意：</b>具有相同名称的Bean将在'最低'工厂级别中优先，即这些Bean将从它们被找到的最低工厂返回，从而隐藏祖先工厂中相应的Bean。此功能允许通过在子工厂中显式选择相同的Bean名称来'替换'Bean；此时，祖先工厂中的Bean将不可见，甚至对于按类型查找也是如此。
     * @param lbf Bean工厂
     * @param type 要匹配的Bean类型
     * @param includeNonSingletons 是否包括原型或作用域Bean，而不仅仅是单例（也适用于FactoryBeans）
     * @param allowEagerInit 是否初始化<i>懒加载单例</i>和<i>由FactoryBeans创建的对象</i>（或具有"factory-bean"引用的工厂方法）以进行类型检查。请注意，FactoryBeans需要被提前初始化以确定其类型：因此请注意，传递"true"给此标志将初始化FactoryBeans和"factory-bean"引用。
     * @return 匹配的Bean实例
     * @throws NoSuchBeanDefinitionException 如果没有找到给定类型的Bean
     * @throws NoUniqueBeanDefinitionException 如果找到了多个给定类型的Bean
     * @throws BeansException 如果无法创建Bean
     * @see #beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
     */
    public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type, includeNonSingletons, allowEagerInit);
        return uniqueBean(type, beansOfType);
    }

    /**
     * 返回给定类型或子类型的单个Bean，不查找祖先工厂。当预期只有一个Bean且不关心Bean名称时，这是一个有用的便捷方法。
     * <p>考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果由FactoryBean创建的对象不匹配，则将直接将原始FactoryBean与类型进行匹配。
     * <p>此版本的{@code beanOfType}自动包括原型和FactoryBeans。
     * @param lbf Bean工厂
     * @param type 要匹配的Bean类型
     * @return 匹配的Bean实例
     * @throws NoSuchBeanDefinitionException 如果未找到给定类型的任何Bean
     * @throws NoUniqueBeanDefinitionException 如果找到了多个给定类型的Bean
     * @throws BeansException 如果无法创建Bean
     * @see ListableBeanFactory#getBeansOfType(Class)
     */
    public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type) throws BeansException {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> beansOfType = lbf.getBeansOfType(type);
        return uniqueBean(type, beansOfType);
    }

    /**
     * 返回给定类型或子类型的单个Bean，不查找祖先工厂。当预期只有一个Bean且不关心Bean名称时，这是一个有用的便捷方法。
     * <p>如果设置了“allowEagerInit”标志，则考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化。如果由FactoryBean创建的对象不匹配，则将直接将原始FactoryBean与类型进行匹配。如果没有设置“allowEagerInit”，则只检查原始FactoryBeans（这不需要初始化每个FactoryBean）。
     * @param lbf Bean工厂
     * @param type 要匹配的Bean类型
     * @param includeNonSingletons 是否包括原型或作用域Bean（也适用于FactoryBeans），或者只包括单例Bean
     * @param allowEagerInit 是否初始化<i>懒加载单例</i>和<i>由FactoryBeans创建的对象</i>（或通过具有“factory-bean”引用的工厂方法）以进行类型检查。请注意，FactoryBeans需要被急切初始化以确定其类型：因此，请注意，将此标志的值传递为“true”将初始化FactoryBeans和“factory-bean”引用。
     * @return 匹配的Bean实例
     * @throws NoSuchBeanDefinitionException 如果未找到给定类型的Bean
     * @throws NoUniqueBeanDefinitionException 如果找到了多个给定类型的Bean
     * @throws BeansException 如果无法创建Bean
     * @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
     */
    public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> beansOfType = lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit);
        return uniqueBean(type, beansOfType);
    }

    /**
     * 将给定的bean名称结果与给定的父级结果合并。
     * @param result 本地bean名称结果
     * @param parentResult 父级bean名称结果（可能为空）
     * @param hbf 本地bean工厂
     * @return 合并后的结果（可能是本地结果本身）
     * @since 4.3.15
     */
    private static String[] mergeNamesWithParent(String[] result, String[] parentResult, HierarchicalBeanFactory hbf) {
        if (parentResult.length == 0) {
            return result;
        }
        List<String> merged = new ArrayList<>(result.length + parentResult.length);
        merged.addAll(Arrays.asList(result));
        for (String beanName : parentResult) {
            if (!merged.contains(beanName) && !hbf.containsLocalBean(beanName)) {
                merged.add(beanName);
            }
        }
        return StringUtils.toStringArray(merged);
    }

    /**
     * 从给定的匹配豆子Map中提取给定类型的唯一豆子。
     * @param type 要匹配的豆子类型
     * @param matchingBeans 找到的所有匹配的豆子
     * @return 唯一的豆子实例
     * @throws NoSuchBeanDefinitionException 如果未找到给定类型的豆子
     * @throws NoUniqueBeanDefinitionException 如果找到多个给定类型的豆子
     */
    private static <T> T uniqueBean(Class<T> type, Map<String, T> matchingBeans) {
        int count = matchingBeans.size();
        if (count == 1) {
            return matchingBeans.values().iterator().next();
        } else if (count > 1) {
            throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
        } else {
            throw new NoSuchBeanDefinitionException(type);
        }
    }
}
