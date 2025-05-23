// 翻译完成 glm-4-flash
/*版权所有 2002-2016 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）；除非符合许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.TargetSource;
import org.springframework.lang.Nullable;

/**
 * 实现可以创建特殊的目标源，例如针对特定bean的池化目标源。例如，它们可能基于目标类的属性，如池化属性来做出选择。
 *
 * <p>AbstractAutoProxyCreator可以支持多个TargetSourceCreator，它们将按顺序应用。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@FunctionalInterface
public interface TargetSourceCreator {

    /**
     * 为指定的bean创建一个特殊的TargetSource，如果有的话。
     * @param beanClass 要为其实例化TargetSource的bean的类
     * @param beanName bean的名称
     * @return 特殊的TargetSource或null，如果这个TargetSourceCreator对特定的bean不感兴趣
     */
    @Nullable
    TargetSource getTargetSource(Class<?> beanClass, String beanName);
}
