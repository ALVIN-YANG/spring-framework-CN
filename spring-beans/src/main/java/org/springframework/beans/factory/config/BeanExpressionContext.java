// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下地址获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可协议下分发的软件
* 是按“现状”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 用于在Bean定义中评估表达式的上下文对象。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public class BeanExpressionContext {

    private final ConfigurableBeanFactory beanFactory;

    @Nullable
    private final Scope scope;

    public BeanExpressionContext(ConfigurableBeanFactory beanFactory, @Nullable Scope scope) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        this.beanFactory = beanFactory;
        this.scope = scope;
    }

    public final ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Nullable
    public final Scope getScope() {
        return this.scope;
    }

    public boolean containsObject(String key) {
        return (this.beanFactory.containsBean(key) || (this.scope != null && this.scope.resolveContextualObject(key) != null));
    }

    @Nullable
    public Object getObject(String key) {
        if (this.beanFactory.containsBean(key)) {
            return this.beanFactory.getBean(key);
        } else if (this.scope != null) {
            return this.scope.resolveContextualObject(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof BeanExpressionContext that && this.beanFactory == that.beanFactory && this.scope == that.scope));
    }

    @Override
    public int hashCode() {
        return this.beanFactory.hashCode();
    }
}
