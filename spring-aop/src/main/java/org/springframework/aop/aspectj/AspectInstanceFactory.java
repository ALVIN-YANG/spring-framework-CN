// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权性的保证。
有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

/**
 * 实现此接口以提供 AspectJ 切面的实例。
 * 与 Spring 的 Bean 工厂解耦。
 *
 * <p>扩展了 {@link org.springframework.core.Ordered} 接口，
 * 以在链中为底层切面表达一个排序值。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public interface AspectInstanceFactory extends Ordered {

    /**
     * 创建该工厂的方面实例。
     * @return 方面实例（绝不会为{@code null}）
     */
    Object getAspectInstance();

    /**
     * 暴露此工厂使用的方面类加载器。
     * @return 方面类加载器（或对于引导类加载器，返回 {@code null}）
     * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
     */
    @Nullable
    ClassLoader getAspectClassLoader();
}
