// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.serviceloader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.springframework.beans.factory.BeanClassLoaderAware;

/**
 * 实现 {@link org.springframework.beans.factory.FactoryBean} 的类，该类暴露了配置的服务类中的所有服务。
 * 这些服务以服务对象列表的形式表示，通过 JDK 1.6 的 {@link java.util.ServiceLoader} 功能获取。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */
public class ServiceListFactoryBean extends AbstractServiceLoaderBasedFactoryBean implements BeanClassLoaderAware {

    @Override
    protected Object getObjectToExpose(ServiceLoader<?> serviceLoader) {
        List<Object> result = new ArrayList<>();
        for (Object loaderObject : serviceLoader) {
            result.add(loaderObject);
        }
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return List.class;
    }
}
