// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 简单的 {@link BeanFactoryPostProcessor} 实现类，用于将自定义的 {@link Scope Scope(s)} 注册到包含的 {@link ConfigurableBeanFactory}。
 *
 * <p>此实现在调用 {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)} 方法时，会将所有提供的 {@link #setScopes(java.util.Map) 范围} 注册到传递给该方法的 {@link ConfigurableListableBeanFactory}。
 *
 * <p>此类允许进行 <i>声明性</i> 的自定义范围注册。或者，可以考虑实现一个自定义的 {@link BeanFactoryPostProcessor}，该处理器会程序化地调用 {@link ConfigurableBeanFactory#registerScope}。
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 */
public class CustomScopeConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

    @Nullable
    private Map<String, Object> scopes;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    /**
     * 指定要注册的自定义作用域。
     * <p>键表示作用域名称（类型为String）；每个值应是对应的定制 {@link Scope} 实例或类名。
     */
    public void setScopes(Map<String, Object> scopes) {
        this.scopes = scopes;
    }

    /**
     * 将给定的作用域添加到本配置器的作用域映射中。
     * @param scopeName 作用域的名称
     * @param scope 作用域的实现
     * @since 4.1.1
     */
    public void addScope(String scopeName, Scope scope) {
        if (this.scopes == null) {
            this.scopes = new LinkedHashMap<>(1);
        }
        this.scopes.put(scopeName, scope);
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.scopes != null) {
            this.scopes.forEach((scopeKey, value) -> {
                if (value instanceof Scope scope) {
                    beanFactory.registerScope(scopeKey, scope);
                } else if (value instanceof Class<?> scopeClass) {
                    Assert.isAssignable(Scope.class, scopeClass, "Invalid scope class");
                    beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
                } else if (value instanceof String scopeClassName) {
                    Class<?> scopeClass = ClassUtils.resolveClassName(scopeClassName, this.beanClassLoader);
                    Assert.isAssignable(Scope.class, scopeClass, "Invalid scope class");
                    beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
                } else {
                    throw new IllegalArgumentException("Mapped value [" + value + "] for scope key [" + scopeKey + "] is not an instance of required type [" + Scope.class.getName() + "] or a corresponding Class or String value indicating a Scope implementation");
                }
            });
        }
    }
}
