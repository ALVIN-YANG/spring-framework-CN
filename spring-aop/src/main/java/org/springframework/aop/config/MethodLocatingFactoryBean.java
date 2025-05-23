// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。
 
根据Apache License, Version 2.0（“许可协议”）许可；
除非遵守许可协议，否则您不得使用此文件。
您可以在以下地址获取许可协议的副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可协议下分发的软件
按照“现状”分发，不提供任何形式的明示或暗示保证，
包括但不限于对适销性、特定用途适用性和非侵权性的保证。
有关许可协议的具体语言、权限和限制，请参阅许可协议。*/
package org.springframework.aop.config;

import java.lang.reflect.Method;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 实现 {@link FactoryBean}，用于在指定的 bean 上定位一个 {@link Method}。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class MethodLocatingFactoryBean implements FactoryBean<Method>, BeanFactoryAware {

    @Nullable
    private String targetBeanName;

    @Nullable
    private String methodName;

    @Nullable
    private Method method;

    /**
     * 设置要定位其 {@link Method} 的 Bean 名称。
     * <p>此属性是必需的。
     * @param targetBeanName 要定位其 {@link Method} 的 Bean 的名称
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    /**
     * 设置要定位的 {@link Method} 的名称。
     * <p>此属性是必需的。
     * @param methodName 要定位的 {@link Method} 的名称
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!StringUtils.hasText(this.targetBeanName)) {
            throw new IllegalArgumentException("Property 'targetBeanName' is required");
        }
        if (!StringUtils.hasText(this.methodName)) {
            throw new IllegalArgumentException("Property 'methodName' is required");
        }
        Class<?> beanClass = beanFactory.getType(this.targetBeanName);
        if (beanClass == null) {
            throw new IllegalArgumentException("Can't determine type of bean with name '" + this.targetBeanName + "'");
        }
        this.method = BeanUtils.resolveSignature(this.methodName, beanClass);
        if (this.method == null) {
            throw new IllegalArgumentException("Unable to locate method [" + this.methodName + "] on bean [" + this.targetBeanName + "]");
        }
    }

    @Override
    @Nullable
    public Method getObject() throws Exception {
        return this.method;
    }

    @Override
    public Class<Method> getObjectType() {
        return Method.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
