// 翻译完成 glm-4-flash
/** 版权所有 2002-2013 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import org.springframework.lang.Nullable;

/**
 *  用于为 Spring 实例创建 {@link BeanInfo} 对象的策略接口。
 *  可以用于插入自定义的 bean 属性解析策略（例如用于 JVM 上的其他语言）或更高效的 {@link BeanInfo} 获取算法。
 *
 * <p>通过使用 {@link org.springframework.core.io.support.SpringFactoriesLoader} 工具类，由 {@link CachedIntrospectionResults} 实例化 BeanInfoFactories。
 *
 * 当需要创建一个 {@link BeanInfo} 对象时，{@code CachedIntrospectionResults} 将遍历发现的工厂，并对每个工厂调用 {@link #getBeanInfo(Class)} 方法。如果返回值为 null，则查询下一个工厂。如果没有任何工厂支持该类，将创建一个标准的 {@link BeanInfo} 作为默认值。
 *
 * <p>请注意，由 {@link org.springframework.core.io.support.SpringFactoriesLoader} 对 BeanInfoFactory 实例进行排序，按照 {@link org.springframework.core.annotation.Order @Order}，这样具有更高优先级的实例将排在前面。
 *
 * @作者 Arjen Poutsma
 * @since 3.2
 * @see CachedIntrospectionResults
 * @see org.springframework.core.io.support.SpringFactoriesLoader
 */
public interface BeanInfoFactory {

    /**
     * 返回给定类的 Bean 信息，如果受支持的话。
     * @param beanClass Bean 类
     * @return BeanInfo 对象，或者如果给定的类不受支持则返回 {@code null}
     * @throws IntrospectionException 如果发生异常
     */
    @Nullable
    BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException;
}
