// 翻译完成 glm-4-flash
/** 版权所有 2002-2007 原作者或作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）授权；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证，
* 无论明示还是暗示。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;
import org.springframework.beans.factory.BeanClassLoaderAware;

/**
 * 一个实现 {@link org.springframework.beans.factory.FactoryBean} 的类，用于暴露配置的服务类的 JDK 1.6 版本 {@link java.util.ServiceLoader}。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */
public class ServiceLoaderFactoryBean extends AbstractServiceLoaderBasedFactoryBean implements BeanClassLoaderAware {

    @Override
    protected Object getObjectToExpose(ServiceLoader<?> serviceLoader) {
        return serviceLoader;
    }

    @Override
    public Class<?> getObjectType() {
        return ServiceLoader.class;
    }
}
