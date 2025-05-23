// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache License, Version 2.0 ("许可证")授权；
除非符合许可证规定或书面同意，否则不得使用此文件。
您可以在以下地址获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，无论是否明确声明。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import org.springframework.aop.framework.AopConfigException;

/**
 * 在尝试对一个不是 AspectJ 注解风格的切面类执行顾问生成操作时抛出的 AopConfigException 的扩展。
 *
 * @author Rod Johnson
 * @since 2.0
 */
@SuppressWarnings("serial")
public class NotAnAtAspectException extends AopConfigException {

    private final Class<?> nonAspectClass;

    /**
     * 为给定的类创建一个新的 NotAnAtAspectException。
     * @param nonAspectClass 导致问题的类
     */
    public NotAnAtAspectException(Class<?> nonAspectClass) {
        super(nonAspectClass.getName() + " is not an @AspectJ aspect");
        this.nonAspectClass = nonAspectClass;
    }

    /**
     * 返回有问题的类。
     */
    public Class<?> getNonAspectClass() {
        return this.nonAspectClass;
    }
}
