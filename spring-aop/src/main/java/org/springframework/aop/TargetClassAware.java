// 翻译完成 glm-4-flash
/*版权所有 2002-2015 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是隐含的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

import org.springframework.lang.Nullable;

/**
 * 最小接口，用于通过代理暴露目标类。
 *
 * <p>由AOP代理对象和代理工厂（通过{@link org.springframework.aop.framework.Advised}）
 * 以及由{@link TargetSource TargetSources}实现。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)
 */
public interface TargetClassAware {

    /**
     * 返回实现对象背后的目标类
     * （通常是一个代理配置或实际代理）。
     * @return 返回目标类，或者在未知的情况下返回 {@code null}
     */
    @Nullable
    Class<?> getTargetClass();
}
