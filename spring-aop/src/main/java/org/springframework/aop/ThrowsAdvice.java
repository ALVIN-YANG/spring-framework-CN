// 翻译完成 glm-4-flash
/*版权所有 2002-2008 原作者或作者。

根据Apache License, Version 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，不提供任何形式的明示或暗示保证。
有关许可的具体语言、权限和限制，请参阅许可证。*/
package org.springframework.aop;

/**
 * 用于抛出异常建议的标签接口。
 *
 * <p>此接口没有方法，因为方法是通过对反射进行调用的。实现类必须实现以下形式的方法：
 *
 * <pre class="code">void afterThrowing([Method, args, target], ThrowableSubclass);</pre>
 *
 * <p>一些有效方法的示例包括：
 *
 * <pre class="code">public void afterThrowing(Exception ex)</pre>
 * <pre class="code">public void afterThrowing(RemoteException)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>
 *
 * 前三个参数是可选的，只有在我们需要进一步了解切入点信息时才有用，如AspectJ中的<b>after-throwing</b>建议。
 *
 * <p><b>注意：</b>如果throws-advice方法本身抛出了异常，它将覆盖原始异常（即更改传递给用户的异常）。覆盖的异常通常是RuntimeException；这与任何方法签名兼容。然而，如果throws-advice方法抛出检查型异常，它必须与目标方法的声明异常匹配，因此在一定程度上与特定的目标方法签名相关联。<b>不要抛出与目标方法签名不兼容的未声明检查型异常！</b>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AfterReturningAdvice
 * @see MethodBeforeAdvice
 */
public interface ThrowsAdvice extends AfterAdvice {
}
