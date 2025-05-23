// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 用于操作 JDK 1.6 中 {@link java.util.ServiceLoader} 服务的 FactoryBeans 的抽象基类。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */
public abstract class AbstractServiceLoaderBasedFactoryBean extends AbstractFactoryBean<Object> implements BeanClassLoaderAware {

    @Nullable
    private Class<?> serviceType;

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    /**
     * 指定所需的服务类型（通常是服务的公共API）。
     */
    public void setServiceType(@Nullable Class<?> serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * 返回期望的服务类型。
     */
    @Nullable
    public Class<?> getServiceType() {
        return this.serviceType;
    }

    @Override
    public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    /**
     * 委派给 {@link #getObjectToExpose(java.util.ServiceLoader)}.
     * @return 要暴露的对象
     */
    @Override
    protected Object createInstance() {
        Assert.state(getServiceType() != null, "Property 'serviceType' is required");
        return getObjectToExpose(ServiceLoader.load(getServiceType(), this.beanClassLoader));
    }

    /**
     * 确定给定 ServiceLoader 的实际对象。
     * <p>具体实现留给子类。
     * @param serviceLoader 配置的服务类对应的 ServiceLoader
     * @return 要暴露的对象
     */
    protected abstract Object getObjectToExpose(ServiceLoader<?> serviceLoader);
}
