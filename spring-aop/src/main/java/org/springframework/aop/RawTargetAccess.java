// 翻译完成 glm-4-flash
/*版权所有 2002-2007 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

/**
 * 标记用于AOP代理接口（特别是：引入接口），这些接口明确表示要返回原始目标对象（在方法调用返回时，通常会被代理对象替换）。
 *
 * <p>请注意，这是一个类似于 {@link java.io.Serializable} 的标记接口，语义上应用于声明的接口，而不是具体对象的整个类。换句话说，这个标记只应用于特定的接口（通常是作为AOP代理的辅助接口而不是主接口的引入接口），因此不会影响具体AOP代理可能实现的其它接口。
 *
 * @author Juergen Hoeller
 * @since 2.0.5
 * @see org.springframework.aop.scope.ScopedObject
 */
public interface RawTargetAccess {
}
