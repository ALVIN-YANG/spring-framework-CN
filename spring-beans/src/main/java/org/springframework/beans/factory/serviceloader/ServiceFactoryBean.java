// 翻译完成 glm-4-flash
/** 版权所有 2002-2007 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）授权；
* 除非遵守许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.serviceloader;

import java.util.Iterator;
import java.util.ServiceLoader;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.lang.Nullable;

/**
 * 一个实现 {@link org.springframework.beans.factory.FactoryBean} 的类，用于暴露配置的服务类对应的“主”服务，该服务是通过 JDK 1.6 的 {@link java.util.ServiceLoader} 工具获取的。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */
public class ServiceFactoryBean extends AbstractServiceLoaderBasedFactoryBean implements BeanClassLoaderAware {

    @Override
    protected Object getObjectToExpose(ServiceLoader<?> serviceLoader) {
        Iterator<?> it = serviceLoader.iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException("ServiceLoader could not find service for type [" + getServiceType() + "]");
        }
        return it.next();
    }

    @Override
    @Nullable
    public Class<?> getObjectType() {
        return getServiceType();
    }
}
