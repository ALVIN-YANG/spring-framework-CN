// 翻译完成 glm-4-flash
/** 版权所有 2002-2016，原作者或作者。
*
* 根据 Apache License，版本 2.0（“许可证”），除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.aop.aspectj.annotation;

import org.springframework.aop.aspectj.SimpleAspectInstanceFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;

/**
 *  实现{@link MetadataAwareAspectInstanceFactory}，该实现为每次调用{@link #getAspectInstance()}创建指定切面类的新实例。
 *
 * @author Juergen Hoeller
 * @since 2.0.4
 */
public class SimpleMetadataAwareAspectInstanceFactory extends SimpleAspectInstanceFactory implements MetadataAwareAspectInstanceFactory {

    private final AspectMetadata metadata;

    /**
     * 为给定的切面类创建一个新的 SimpleMetadataAwareAspectInstanceFactory。
     * @param aspectClass 切面类
     * @param aspectName 切面名称
     */
    public SimpleMetadataAwareAspectInstanceFactory(Class<?> aspectClass, String aspectName) {
        super(aspectClass);
        this.metadata = new AspectMetadata(aspectClass, aspectName);
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
