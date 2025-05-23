// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可范围和限制。*/
package org.springframework.beans.factory.annotation;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 提供方便的方法，用于执行与Spring特定注解相关的bean查找，例如Spring的{@link Qualifier @Qualifier}注解。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.1.2
 * @see BeanFactoryUtils
 */
public abstract class BeanFactoryAnnotationUtils {

    /**
     * 从给定的 {@code BeanFactory} 中检索所有类型为 {@code T} 的 Bean，声明了一个限定符（例如通过 {@code <qualifier>} 或 {@code @Qualifier}）与给定的限定符匹配，或者拥有与给定限定符匹配的 Bean 名称。
     * @param beanFactory 从中获取目标 Bean 的工厂（也会搜索祖先）
     * @param beanType 要检索的 Bean 类型
     * @param qualifier 选择所有类型匹配项时的限定符
     * @return 类型为 {@code T} 的匹配 Bean
     * @throws BeansException 如果任何匹配的 Bean 无法创建
     * @since 5.1.1
     * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
     */
    public static <T> Map<String, T> qualifiedBeansOfType(ListableBeanFactory beanFactory, Class<T> beanType, String qualifier) throws BeansException {
        String[] candidateBeans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanType);
        Map<String, T> result = new LinkedHashMap<>(4);
        for (String beanName : candidateBeans) {
            if (isQualifierMatch(qualifier::equals, beanName, beanFactory)) {
                result.put(beanName, beanFactory.getBean(beanName, beanType));
            }
        }
        return result;
    }

    /**
     * 从给定的 {@code BeanFactory} 中获取类型为 {@code T} 的对象，该对象声明了一个限定符（例如通过 {@code <qualifier>} 或 {@code @Qualifier}）与给定的限定符匹配，或者有一个与给定的限定符匹配的bean名称。
     * @param beanFactory 要从中获取目标bean的工厂（也会搜索祖先）
     * @param beanType 要检索的bean类型
     * @param qualifier 用于在多个匹配的bean之间选择限定符
     * @return 类型为 {@code T} 的匹配bean（永不返回 {@code null}）
     * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的类型为 {@code T} 的bean
     * @throws NoSuchBeanDefinitionException 如果没有找到匹配的类型为 {@code T} 的bean
     * @throws BeansException 如果bean无法创建
     * @see BeanFactoryUtils#beanOfTypeIncludingAncestors(ListableBeanFactory, Class)
     */
    public static <T> T qualifiedBeanOfType(BeanFactory beanFactory, Class<T> beanType, String qualifier) throws BeansException {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        if (beanFactory instanceof ListableBeanFactory lbf) {
            // 支持完全限定符匹配。
            return qualifiedBeanOfType(lbf, beanType, qualifier);
        } else if (beanFactory.containsBean(qualifier)) {
            // 回退：至少通过bean名称找到了目标bean。
            return beanFactory.getBean(qualifier, beanType);
        } else {
            throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() + " bean found for bean name '" + qualifier + "'! (Note: Qualifier matching not supported because given " + "BeanFactory does not implement ConfigurableListableBeanFactory.)");
        }
    }

    /**
     * 从给定的 {@code BeanFactory} 中获取类型为 {@code T} 的 Bean，该 Bean 声明了与给定限定符匹配的限定符（例如，使用 {@code <qualifier>} 或 {@code @Qualifier}）。
     * @param bf 获取目标 Bean 的工厂
     * @param beanType 要检索的 Bean 类型
     * @param qualifier 用于在多个 Bean 匹配项之间进行选择的限定符
     * @return 类型为 {@code T} 的匹配 Bean（绝不返回 {@code null}）
     */
    private static <T> T qualifiedBeanOfType(ListableBeanFactory bf, Class<T> beanType, String qualifier) {
        String[] candidateBeans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(bf, beanType);
        String matchingBean = null;
        for (String beanName : candidateBeans) {
            if (isQualifierMatch(qualifier::equals, beanName, bf)) {
                if (matchingBean != null) {
                    throw new NoUniqueBeanDefinitionException(beanType, matchingBean, beanName);
                }
                matchingBean = beanName;
            }
        }
        if (matchingBean != null) {
            return bf.getBean(matchingBean, beanType);
        } else if (bf.containsBean(qualifier)) {
            // 回退：至少通过bean名称找到了目标bean - 可能是一个手动注册的单例。
            return bf.getBean(qualifier, beanType);
        } else {
            throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() + " bean found for qualifier '" + qualifier + "' - neither qualifier match nor bean name match!");
        }
    }

    /**
     * 检查指定的命名bean是否声明了给定名称的限定符。
     * @param qualifier 要匹配的限定符
     * @param beanName 候选bean的名称
     * @param beanFactory 从中检索命名bean的工厂
     * @return 如果bean定义（在XML情况下）或bean的工厂方法（在@Bean情况下）定义了匹配的限定符值（通过<qualifier>或@Qualifier），则返回{@code true}
     * @since 5.0
     */
    public static boolean isQualifierMatch(Predicate<String> qualifier, String beanName, @Nullable BeanFactory beanFactory) {
        // 首先尝试快速匹配 Bean 名称或别名...
        if (qualifier.test(beanName)) {
            return true;
        }
        if (beanFactory != null) {
            for (String alias : beanFactory.getAliases(beanName)) {
                if (qualifier.test(alias)) {
                    return true;
                }
            }
            try {
                Class<?> beanType = beanFactory.getType(beanName);
                if (beanFactory instanceof ConfigurableBeanFactory cbf) {
                    BeanDefinition bd = cbf.getMergedBeanDefinition(beanName);
                    // 在bean定义上显式地指定元数据？（通常在XML定义中）
                    if (bd instanceof AbstractBeanDefinition abd) {
                        AutowireCandidateQualifier candidate = abd.getQualifier(Qualifier.class.getName());
                        if (candidate != null) {
                            Object value = candidate.getAttribute(AutowireCandidateQualifier.VALUE_KEY);
                            if (value != null && qualifier.test(value.toString())) {
                                return true;
                            }
                        }
                    }
                    // 对应于工厂方法上的限定符？（通常在配置类中）
                    if (bd instanceof RootBeanDefinition rbd) {
                        Method factoryMethod = rbd.getResolvedFactoryMethod();
                        if (factoryMethod != null) {
                            Qualifier targetAnnotation = AnnotationUtils.getAnnotation(factoryMethod, Qualifier.class);
                            if (targetAnnotation != null) {
                                return qualifier.test(targetAnnotation.value());
                            }
                        }
                    }
                }
                // 对应于Bean实现类上的限定符？（用于自定义用户类型）
                if (beanType != null) {
                    Qualifier targetAnnotation = AnnotationUtils.getAnnotation(beanType, Qualifier.class);
                    if (targetAnnotation != null) {
                        return qualifier.test(targetAnnotation.value());
                    }
                }
            } catch (NoSuchBeanDefinitionException ex) {
                // 忽略 - 无法比较手动注册的单例对象的限定符
            }
        }
        return false;
    }
}
