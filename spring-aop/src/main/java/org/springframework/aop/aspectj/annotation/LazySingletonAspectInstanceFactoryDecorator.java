// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 装饰器，用于使 {@link MetadataAwareAspectInstanceFactory} 只实例化一次。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class LazySingletonAspectInstanceFactoryDecorator implements MetadataAwareAspectInstanceFactory, Serializable {

    private final MetadataAwareAspectInstanceFactory maaif;

    @Nullable
    private volatile Object materialized;

    /**
     * 为给定的 AspectInstanceFactory 创建一个新的懒加载装饰器。
     * @param maaif 要装饰的 MetadataAwareAspectInstanceFactory
     */
    public LazySingletonAspectInstanceFactoryDecorator(MetadataAwareAspectInstanceFactory maaif) {
        Assert.notNull(maaif, "AspectInstanceFactory must not be null");
        this.maaif = maaif;
    }

    @Override
    public Object getAspectInstance() {
        Object aspectInstance = this.materialized;
        if (aspectInstance == null) {
            Object mutex = this.maaif.getAspectCreationMutex();
            if (mutex == null) {
                aspectInstance = this.maaif.getAspectInstance();
                this.materialized = aspectInstance;
            } else {
                synchronized (mutex) {
                    aspectInstance = this.materialized;
                    if (aspectInstance == null) {
                        aspectInstance = this.maaif.getAspectInstance();
                        this.materialized = aspectInstance;
                    }
                }
            }
        }
        return aspectInstance;
    }

    public boolean isMaterialized() {
        return (this.materialized != null);
    }

    @Override
    @Nullable
    public ClassLoader getAspectClassLoader() {
        return this.maaif.getAspectClassLoader();
    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return this.maaif.getAspectMetadata();
    }

    @Override
    @Nullable
    public Object getAspectCreationMutex() {
        return this.maaif.getAspectCreationMutex();
    }

    @Override
    public int getOrder() {
        return this.maaif.getOrder();
    }

    @Override
    public String toString() {
        return "LazySingletonAspectInstanceFactoryDecorator: decorating " + this.maaif;
    }
}
