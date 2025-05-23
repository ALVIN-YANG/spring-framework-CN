// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 您不得使用此文件除非遵守许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，无论是明示的还是暗示的。
* 请参阅许可证了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 用于解析占位符和表达式的 {@link StringValueResolver} 适配器，针对一个 {@link ConfigurableBeanFactory}。
 *
 * <p>注意，与 {@link ConfigurableBeanFactory#resolveEmbeddedValue} 方法不同，此适配器也会解析表达式。
 * 使用的 {@link BeanExpressionContext} 是针对普通 Bean 工厂，没有指定任何上下文对象的范围以进行访问。
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @see ConfigurableBeanFactory#resolveEmbeddedValue(String)
 * @see ConfigurableBeanFactory#getBeanExpressionResolver()
 * @see BeanExpressionContext
 */
public class EmbeddedValueResolver implements StringValueResolver {

    private final BeanExpressionContext exprContext;

    @Nullable
    private final BeanExpressionResolver exprResolver;

    public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
        this.exprContext = new BeanExpressionContext(beanFactory, null);
        this.exprResolver = beanFactory.getBeanExpressionResolver();
    }

    @Override
    @Nullable
    public String resolveStringValue(String strVal) {
        String value = this.exprContext.getBeanFactory().resolveEmbeddedValue(strVal);
        if (this.exprResolver != null && value != null) {
            Object evaluated = this.exprResolver.evaluate(value, this.exprContext);
            value = (evaluated != null ? evaluated.toString() : null);
        }
        return value;
    }
}
