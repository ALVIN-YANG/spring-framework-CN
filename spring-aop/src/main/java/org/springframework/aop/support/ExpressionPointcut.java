// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。

根据Apache许可证版本2.0（以下简称“许可证”）；除非符合许可证，否则您不得使用此文件。
您可以在以下链接处获得许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.lang.Nullable;

/**
 * 需要由使用字符串表达式的切点实现的接口。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public interface ExpressionPointcut extends Pointcut {

    /**
     * 返回此切入点（pointcut）的字符串表达式。
     */
    @Nullable
    String getExpression();
}
