// 翻译完成 glm-4-flash
/*版权所有 2002-2016 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;
import org.springframework.aop.aspectj.SingletonAspectInstanceFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

/**
 * 实现了基于指定单例对象的 {@link MetadataAwareAspectInstanceFactory}，对于每个 {@link #getAspectInstance()} 调用都返回相同的实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleMetadataAwareAspectInstanceFactory
 */
@SuppressWarnings("serial")
public class SingletonMetadataAwareAspectInstanceFactory extends SingletonAspectInstanceFactory implements MetadataAwareAspectInstanceFactory, Serializable {

    private final AspectMetadata metadata;

    /**
     * 为指定的方面创建一个新的 SingletonMetadataAwareAspectInstanceFactory。
     * @param aspectInstance 单例方面实例
     * @param aspectName 方面的名称
     */
    public SingletonMetadataAwareAspectInstanceFactory(Object aspectInstance, String aspectName) {
        super(aspectInstance);
        this.metadata = new AspectMetadata(aspectInstance.getClass(), aspectName);
    }

    @Override
    public final AspectMetadata getAspectMetadata() {
        return this.metadata;
    }

    @Override
    public Object getAspectCreationMutex() {
        return this;
    }

    @Override
    protected int getOrderForAspectClass(Class<?> aspectClass) {
        return OrderUtils.getOrder(aspectClass, Ordered.LOWEST_PRECEDENCE);
    }
}
