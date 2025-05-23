// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性或不侵犯他人权利。
* 请参阅许可证以了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.support;

import java.util.function.Supplier;
import org.springframework.beans.factory.config.AutowiredPropertyMarker;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * 使用构建器模式构建
 * {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions}
 * 的程序化方法。主要用于在实现 Spring 2.0
 * {@link org.springframework.beans.factory.xml.NamespaceHandler NamespaceHandlers} 时使用。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public final class BeanDefinitionBuilder {

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link GenericBeanDefinition}。
     */
    public static BeanDefinitionBuilder genericBeanDefinition() {
        return new BeanDefinitionBuilder(new GenericBeanDefinition());
    }

    /**
     * 创建一个新的用于构建一个 {@link GenericBeanDefinition} 的 {@code BeanDefinitionBuilder}。
     * @param beanClassName 为正在创建定义的 Bean 的类名
     */
    public static BeanDefinitionBuilder genericBeanDefinition(String beanClassName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new GenericBeanDefinition());
        builder.beanDefinition.setBeanClassName(beanClassName);
        return builder;
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link GenericBeanDefinition}。
     * @param beanClass 正在为其创建定义的 Bean 的 {@code Class}
     */
    public static BeanDefinitionBuilder genericBeanDefinition(Class<?> beanClass) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new GenericBeanDefinition());
        builder.beanDefinition.setBeanClass(beanClass);
        return builder;
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link GenericBeanDefinition}。
     * @param beanClass 要创建定义的bean的 {@code Class}
     * @param instanceSupplier 用于创建bean实例的回调
     * @since 5.0
     */
    public static <T> BeanDefinitionBuilder genericBeanDefinition(Class<T> beanClass, Supplier<T> instanceSupplier) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new GenericBeanDefinition());
        builder.beanDefinition.setBeanClass(beanClass);
        builder.beanDefinition.setInstanceSupplier(instanceSupplier);
        return builder;
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link RootBeanDefinition}。
     * @param beanClassName 为正在创建定义的 Bean 的类名
     */
    public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName) {
        return rootBeanDefinition(beanClassName, null);
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link RootBeanDefinition}。
     * @param beanClassName 为正在创建定义的 bean 的类名
     * @param factoryMethodName 用于构建 bean 实例的方法名称
     */
    public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName, @Nullable String factoryMethodName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new RootBeanDefinition());
        builder.beanDefinition.setBeanClassName(beanClassName);
        builder.beanDefinition.setFactoryMethodName(factoryMethodName);
        return builder;
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link RootBeanDefinition}。
     * @param beanClass 为其创建定义的 Bean 的 {@code Class}
     */
    public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass) {
        return rootBeanDefinition(beanClass, (String) null);
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link RootBeanDefinition}。
     * @param beanClass 要创建定义的 bean 的 {@code Class}
     * @param factoryMethodName 用于构造 bean 实例的方法名称
     */
    public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass, @Nullable String factoryMethodName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder(new RootBeanDefinition());
        builder.beanDefinition.setBeanClass(beanClass);
        builder.beanDefinition.setFactoryMethodName(factoryMethodName);
        return builder;
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link RootBeanDefinition}。
     * @param beanType 为正在创建定义的bean的 {@link ResolvableType 类型}
     * @param instanceSupplier 创建bean实例的回调
     * @since 5.3.9
     */
    public static <T> BeanDefinitionBuilder rootBeanDefinition(ResolvableType beanType, Supplier<T> instanceSupplier) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setTargetType(beanType);
        beanDefinition.setInstanceSupplier(instanceSupplier);
        return new BeanDefinitionBuilder(beanDefinition);
    }

    /**
     * 创建一个新的用于构建 {@link RootBeanDefinition} 的 {@code BeanDefinitionBuilder}。
     * @param beanClass 正在为其创建定义的 Bean 的 {@code Class}
     * @param instanceSupplier 创建 Bean 实例的回调
     * @since 5.3.9
     * @see #rootBeanDefinition(ResolvableType, Supplier)
     */
    public static <T> BeanDefinitionBuilder rootBeanDefinition(Class<T> beanClass, Supplier<T> instanceSupplier) {
        return rootBeanDefinition(ResolvableType.forClass(beanClass), instanceSupplier);
    }

    /**
     * 创建一个新的 {@code BeanDefinitionBuilder}，用于构建一个 {@link ChildBeanDefinition}。
     * @param parentName 父Bean的名称
     */
    public static BeanDefinitionBuilder childBeanDefinition(String parentName) {
        return new BeanDefinitionBuilder(new ChildBeanDefinition(parentName));
    }

    /**
     * 我们正在创建的 {@code BeanDefinition} 实例。
     */
    private final AbstractBeanDefinition beanDefinition;

    /**
     * 我们当前相对于构造函数参数的位置。
     */
    private int constructorArgIndex;

    /**
     * 强制使用工厂方法。
     */
    private BeanDefinitionBuilder(AbstractBeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
    }

    /**
     * 返回当前 BeanDefinition 对象的原始（未验证）形式。
     * @see #getBeanDefinition()
     */
    public AbstractBeanDefinition getRawBeanDefinition() {
        return this.beanDefinition;
    }

    /**
     * 验证并返回创建的 BeanDefinition 对象。
     */
    public AbstractBeanDefinition getBeanDefinition() {
        this.beanDefinition.validate();
        return this.beanDefinition;
    }

    /**
     * 设置此 bean 定义的父定义的名称。
     */
    public BeanDefinitionBuilder setParentName(String parentName) {
        this.beanDefinition.setParentName(parentName);
        return this;
    }

    /**
     * 设置用于此定义的静态工厂方法的名称，
     * 该方法将在该bean的类上被调用。
     */
    public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
        this.beanDefinition.setFactoryMethodName(factoryMethod);
        return this;
    }

    /**
     * 设置用于此定义的非静态工厂方法的名称，包括调用该方法的工厂实例的bean名称。
     * @param factoryMethod 工厂方法的名称
     * @param factoryBean 调用指定工厂方法的bean的名称
     * @since 4.3.6
     */
    public BeanDefinitionBuilder setFactoryMethodOnBean(String factoryMethod, String factoryBean) {
        this.beanDefinition.setFactoryMethodName(factoryMethod);
        this.beanDefinition.setFactoryBeanName(factoryBean);
        return this;
    }

    /**
     * 添加一个带索引的构造函数参数值。当前索引在内部跟踪，所有添加都在当前点进行。
     */
    public BeanDefinitionBuilder addConstructorArgValue(@Nullable Object value) {
        this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(this.constructorArgIndex++, value);
        return this;
    }

    /**
     * 将一个命名 Bean 的引用作为构造函数参数添加。
     * @see #addConstructorArgValue(Object)
     */
    public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
        this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(this.constructorArgIndex++, new RuntimeBeanReference(beanName));
        return this;
    }

    /**
     * 在给定的属性名称下添加提供的属性值。
     */
    public BeanDefinitionBuilder addPropertyValue(String name, @Nullable Object value) {
        this.beanDefinition.getPropertyValues().add(name, value);
        return this;
    }

    /**
     * 在指定的属性下添加对指定bean名称的引用。
     * @param name 要添加引用的属性名称
     * @param beanName 被引用的bean的名称
     */
    public BeanDefinitionBuilder addPropertyReference(String name, String beanName) {
        this.beanDefinition.getPropertyValues().add(name, new RuntimeBeanReference(beanName));
        return this;
    }

    /**
     * 在指定的bean上为指定的属性添加自动装配标记。
     * @param name 要标记为自动装配的属性的名称
     * @since 5.2
     * @see AutowiredPropertyMarker
     */
    public BeanDefinitionBuilder addAutowiredProperty(String name) {
        this.beanDefinition.getPropertyValues().add(name, AutowiredPropertyMarker.INSTANCE);
        return this;
    }

    /**
     * 设置此定义的初始化方法。
     */
    public BeanDefinitionBuilder setInitMethodName(@Nullable String methodName) {
        this.beanDefinition.setInitMethodName(methodName);
        return this;
    }

    /**
     * 为此定义设置销毁方法。
     */
    public BeanDefinitionBuilder setDestroyMethodName(@Nullable String methodName) {
        this.beanDefinition.setDestroyMethodName(methodName);
        return this;
    }

    /**
     * 设置此定义的作用域。
     * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_SINGLETON
     * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_PROTOTYPE
     */
    public BeanDefinitionBuilder setScope(@Nullable String scope) {
        this.beanDefinition.setScope(scope);
        return this;
    }

    /**
     * 设置此定义是否为抽象的。
     */
    public BeanDefinitionBuilder setAbstract(boolean flag) {
        this.beanDefinition.setAbstract(flag);
        return this;
    }

    /**
     * 设置此定义的bean是否应该进行懒加载。
     */
    public BeanDefinitionBuilder setLazyInit(boolean lazy) {
        this.beanDefinition.setLazyInit(lazy);
        return this;
    }

    /**
     * 设置此定义的自动装配模式。
     */
    public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
        this.beanDefinition.setAutowireMode(autowireMode);
        return this;
    }

    /**
     * 设置此定义的依赖检查模式。
     */
    public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
        this.beanDefinition.setDependencyCheck(dependencyCheck);
        return this;
    }

    /**
     * 将指定的bean名称添加到当前定义所依赖的bean列表中。
     */
    public BeanDefinitionBuilder addDependsOn(String beanName) {
        if (this.beanDefinition.getDependsOn() == null) {
            this.beanDefinition.setDependsOn(beanName);
        } else {
            String[] added = ObjectUtils.addObjectToArray(this.beanDefinition.getDependsOn(), beanName);
            this.beanDefinition.setDependsOn(added);
        }
        return this;
    }

    /**
     * 设置此Bean是否是主要自动装配候选者。
     * @since 5.1.11
     */
    public BeanDefinitionBuilder setPrimary(boolean primary) {
        this.beanDefinition.setPrimary(primary);
        return this;
    }

    /**
     * 设置此定义的角色。
     */
    public BeanDefinitionBuilder setRole(int role) {
        this.beanDefinition.setRole(role);
        return this;
    }

    /**
     * 设置此 Bean 是否为“合成”的，即不是由应用程序本身定义的。
     * @since 5.3.9
     */
    public BeanDefinitionBuilder setSynthetic(boolean synthetic) {
        this.beanDefinition.setSynthetic(synthetic);
        return this;
    }

    /**
     * 将给定的自定义器应用于底层的Bean定义。
     * @since 5.0
     */
    public BeanDefinitionBuilder applyCustomizers(BeanDefinitionCustomizer... customizers) {
        for (BeanDefinitionCustomizer customizer : customizers) {
            customizer.customize(this.beanDefinition);
        }
        return this;
    }
}
