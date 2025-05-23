// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。

根据Apache License, Version 2.0（“许可证”）许可；
除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，无论是关于其适用性、无侵权或特定用途的适销性。
有关许可的具体语言，请参阅许可证中规定的权限和限制。*/
package org.springframework.aop.support;

import java.io.Serializable;
import org.springframework.lang.Nullable;

/**
 * 表达式切入点抽象超类，
 * 提供位置和表达式属性。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 * @see #setLocation
 * @see #setExpression
 */
@SuppressWarnings("serial")
public abstract class AbstractExpressionPointcut implements ExpressionPointcut, Serializable {

    @Nullable
    private String location;

    @Nullable
    private String expression;

    /**
     * 设置调试的位置。
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * 返回关于切点表达式的位置信息，如果可用。这在调试时很有用。
     * @return 以可读字符串形式的位置信息，
     * 或者在没有可用信息时返回 {@code null}
     */
    @Nullable
    public String getLocation() {
        return this.location;
    }

    public void setExpression(@Nullable String expression) {
        this.expression = expression;
        try {
            onSetExpression(expression);
        } catch (IllegalArgumentException ex) {
            // 尽可能填写位置信息。
            if (this.location != null) {
                throw new IllegalArgumentException("Invalid expression at location [" + this.location + "]: " + ex);
            } else {
                throw ex;
            }
        }
    }

    /**
     * 当设置新的切点表达式时调用。
     * 如果可能，此时应解析该表达式。
     * <p>此实现为空。
     * @param expression 要设置的表达式
     * @throws IllegalArgumentException 如果表达式无效
     * @see #setExpression
     */
    protected void onSetExpression(@Nullable String expression) throws IllegalArgumentException {
    }

    /**
     * 返回此切点的表达式。
     */
    @Override
    @Nullable
    public String getExpression() {
        return this.expression;
    }
}
