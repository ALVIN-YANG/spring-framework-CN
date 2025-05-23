// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 进行许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证以了解管理许可权和限制的具体语言。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Executable;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 一个 {@code RegisteredBean} 表示一个已与 {@link BeanFactory} 注册的 Bean，但不一定已经实例化。它提供了访问包含该 Bean 的 Bean 工厂以及 Bean 名称的方法。在内部 Bean 的情况下，Bean 名称可能已经生成。
 *
 * @author Phillip Webb
 * @since 6.0
 */
public final class RegisteredBean {

    private final ConfigurableListableBeanFactory beanFactory;

    private final Supplier<String> beanName;

    private final boolean generatedBeanName;

    private final Supplier<RootBeanDefinition> mergedBeanDefinition;

    @Nullable
    private final RegisteredBean parent;

    private RegisteredBean(ConfigurableListableBeanFactory beanFactory, Supplier<String> beanName, boolean generatedBeanName, Supplier<RootBeanDefinition> mergedBeanDefinition, @Nullable RegisteredBean parent) {
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.generatedBeanName = generatedBeanName;
        this.mergedBeanDefinition = mergedBeanDefinition;
        this.parent = parent;
    }

    /**
     * 为普通bean创建一个新的 {@link RegisteredBean} 实例。
     * @param beanFactory bean工厂的来源
     * @param beanName bean名称
     * @return 一个新的 {@link RegisteredBean} 实例
     */
    public static RegisteredBean of(ConfigurableListableBeanFactory beanFactory, String beanName) {
        Assert.notNull(beanFactory, "'beanFactory' must not be null");
        Assert.hasLength(beanName, "'beanName' must not be empty");
        return new RegisteredBean(beanFactory, () -> beanName, false, () -> (RootBeanDefinition) beanFactory.getMergedBeanDefinition(beanName), null);
    }

    /**
     * 为一个常规bean创建一个新的{@link RegisteredBean}实例。
     * @param beanFactory bean工厂的来源
     * @param beanName bean名称
     * @param mbd 预先确定的合并后的bean定义
     * @return 一个新的{@link RegisteredBean}实例
     * @since 6.0.7
     */
    static RegisteredBean of(ConfigurableListableBeanFactory beanFactory, String beanName, RootBeanDefinition mbd) {
        return new RegisteredBean(beanFactory, () -> beanName, false, () -> mbd, null);
    }

    /**
     * 为内部bean创建一个新的 {@link RegisteredBean} 实例。
     * @param parent 内部bean的父级
     * @param innerBean 内部bean的 {@link BeanDefinitionHolder}
     * @return 一个新的 {@link RegisteredBean} 实例
     */
    public static RegisteredBean ofInnerBean(RegisteredBean parent, BeanDefinitionHolder innerBean) {
        Assert.notNull(innerBean, "'innerBean' must not be null");
        return ofInnerBean(parent, innerBean.getBeanName(), innerBean.getBeanDefinition());
    }

    /**
     * 为内部bean创建一个新的 {@link RegisteredBean} 实例。
     * @param parent 内部bean的父对象
     * @param innerBeanDefinition 内部bean的定义
     * @return 一个新的 {@link RegisteredBean} 实例
     */
    public static RegisteredBean ofInnerBean(RegisteredBean parent, BeanDefinition innerBeanDefinition) {
        return ofInnerBean(parent, null, innerBeanDefinition);
    }

    /**
     * 为内部Bean创建一个新的 {@link RegisteredBean} 实例。
     * @param parent 内部Bean的父Bean
     * @param innerBeanName 内部Bean的名称，或传入 {@code null} 以生成一个名称
     * @param innerBeanDefinition 内部Bean的定义
     * @return 一个新的 {@link RegisteredBean} 实例
     */
    public static RegisteredBean ofInnerBean(RegisteredBean parent, @Nullable String innerBeanName, BeanDefinition innerBeanDefinition) {
        Assert.notNull(parent, "'parent' must not be null");
        Assert.notNull(innerBeanDefinition, "'innerBeanDefinition' must not be null");
        InnerBeanResolver resolver = new InnerBeanResolver(parent, innerBeanName, innerBeanDefinition);
        Supplier<String> beanName = (StringUtils.hasLength(innerBeanName) ? () -> innerBeanName : resolver::resolveBeanName);
        return new RegisteredBean(parent.getBeanFactory(), beanName, innerBeanName == null, resolver::resolveMergedBeanDefinition, parent);
    }

    /**
     * 返回 bean 的名称。
     * @return the beanName bean 名称
     */
    public String getBeanName() {
        return this.beanName.get();
    }

    /**
     * 如果已生成 Bean 名称，则返回。
     * @return 如果名称已生成，则返回 {@code true}
     */
    public boolean isGeneratedBeanName() {
        return this.generatedBeanName;
    }

    /**
     * 返回包含该Bean的Bean工厂。
     * @return Bean工厂
     */
    public ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    /**
     * 返回该 Bean 的用户定义类。
     * @return Bean 类
     */
    public Class<?> getBeanClass() {
        return ClassUtils.getUserClass(getBeanType().toClass());
    }

    /**
     * 返回该 Bean 的 {@link ResolvableType}。
     * @return Bean 类型
     */
    public ResolvableType getBeanType() {
        return getMergedBeanDefinition().getResolvableType();
    }

    /**
     * 返回该 Bean 的合并后的 Bean 定义。
     * @return 合并后的 Bean 定义
     * @see ConfigurableListableBeanFactory#getMergedBeanDefinition(String)
     */
    public RootBeanDefinition getMergedBeanDefinition() {
        return this.mergedBeanDefinition.get();
    }

    /**
     * 如果此实例是为内部Bean创建的，则返回。
     * @return 如果是内部Bean
     */
    public boolean isInnerBean() {
        return this.parent != null;
    }

    /**
     * 返回此实例的父类或如果不是内部bean则返回{@code null}。
     * @return 父类
     */
    @Nullable
    public RegisteredBean getParent() {
        return this.parent;
    }

    /**
     * 解决用于此bean的构造函数或工厂方法的选用。
     * @return 返回 {@link java.lang.reflect.Constructor} 或 {@link java.lang.reflect.Method}
     */
    public Executable resolveConstructorOrFactoryMethod() {
        return new ConstructorResolver((AbstractAutowireCapableBeanFactory) getBeanFactory()).resolveConstructorOrFactoryMethod(getBeanName(), getMergedBeanDefinition());
    }

    /**
     * 解决自动装配的参数。
     * @param descriptor 依赖的描述符（字段/方法/构造函数）
     * @param typeConverter 用于填充数组和集合的TypeConverter
     * @param autowiredBeanNames 一个Set集合，所有自动装配的bean名称（用于解决给定的依赖）都应该添加到该集合中
     * @return 解析出的对象，如果没有找到则返回{@code null}
     * @since 6.0.9
     */
    @Nullable
    public Object resolveAutowiredArgument(DependencyDescriptor descriptor, TypeConverter typeConverter, Set<String> autowiredBeanNames) {
        return new ConstructorResolver((AbstractAutowireCapableBeanFactory) getBeanFactory()).resolveAutowiredArgument(descriptor, descriptor.getDependencyType(), getBeanName(), autowiredBeanNames, typeConverter, true);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("beanName", getBeanName()).append("mergedBeanDefinition", getMergedBeanDefinition()).toString();
    }

    /**
     * 用于获取内部Bean详细信息的解析器。
     */
    private static class InnerBeanResolver {

        private final RegisteredBean parent;

        @Nullable
        private final String innerBeanName;

        private final BeanDefinition innerBeanDefinition;

        @Nullable
        private volatile String resolvedBeanName;

        InnerBeanResolver(RegisteredBean parent, @Nullable String innerBeanName, BeanDefinition innerBeanDefinition) {
            Assert.isInstanceOf(AbstractAutowireCapableBeanFactory.class, parent.getBeanFactory());
            this.parent = parent;
            this.innerBeanName = innerBeanName;
            this.innerBeanDefinition = innerBeanDefinition;
        }

        String resolveBeanName() {
            String resolvedBeanName = this.resolvedBeanName;
            if (resolvedBeanName != null) {
                return resolvedBeanName;
            }
            resolvedBeanName = resolveInnerBean((beanName, mergedBeanDefinition) -> beanName);
            this.resolvedBeanName = resolvedBeanName;
            return resolvedBeanName;
        }

        RootBeanDefinition resolveMergedBeanDefinition() {
            return resolveInnerBean((beanName, mergedBeanDefinition) -> mergedBeanDefinition);
        }

        private <T> T resolveInnerBean(BiFunction<String, RootBeanDefinition, T> resolver) {
            // 始终在父合并的Bean定义已更改的情况下使用一个新的BeanDefinitionValueResolver。
            BeanDefinitionValueResolver beanDefinitionValueResolver = new BeanDefinitionValueResolver((AbstractAutowireCapableBeanFactory) this.parent.getBeanFactory(), this.parent.getBeanName(), this.parent.getMergedBeanDefinition());
            return beanDefinitionValueResolver.resolveInnerBean(this.innerBeanName, this.innerBeanDefinition, resolver);
        }
    }
}
