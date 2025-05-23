// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途适用性。
* 请参阅许可证，了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * 定义了一个工厂，当被调用时可以返回一个 Object 实例（可能是共享或独立的）。
 *
 * <p>此接口通常用于封装一个通用的工厂，在每次调用时返回某个目标对象的新实例（原型）。
 *
 * <p>此接口类似于 {@link FactoryBean}，但后者的实现通常被定义为在 {@link BeanFactory} 中的 SPI 实例，而此类的实现通常被用作其他 Bean 的 API（通过注入）。因此，`getObject()` 方法具有不同的异常处理行为。
 *
 * @author Colin Sampaleanu
 * @since 1.0.2
 * @param <T> 对象类型
 * @see FactoryBean
 */
@FunctionalInterface
public interface ObjectFactory<T> {

    /**
     * 返回由该工厂管理的对象的一个实例（可能是共享的或独立的）。
     * @return 返回的结果实例
     * @throws BeansException 在创建错误的情况下抛出异常
     */
    T getObject() throws BeansException;
}
