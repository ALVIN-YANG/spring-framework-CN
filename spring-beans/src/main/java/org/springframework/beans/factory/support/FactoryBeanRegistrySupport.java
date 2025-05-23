// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可以使用此文件，但必须遵守许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 单例注册中心的支持基类，用于处理
 * {@link org.springframework.beans.factory.FactoryBean} 实例，
 * 与 {@link DefaultSingletonBeanRegistry} 的单例管理集成。
 *
 * <p>作为 {@link AbstractBeanFactory} 的基类。
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 */
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

    /**
     * 由FactoryBeans创建的单例对象缓存：FactoryBean名称到对象的映射。
     */
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);

    /**
     * 确定给定 FactoryBean 的类型。
     * @param factoryBean 要检查的 FactoryBean 实例
     * @return FactoryBean 的对象类型，
     * 或者在类型尚未确定时返回 {@code null}
     */
    @Nullable
    protected Class<?> getTypeForFactoryBean(FactoryBean<?> factoryBean) {
        try {
            return factoryBean.getObjectType();
        } catch (Throwable ex) {
            // 从 FactoryBean 的 getObjectType 实现中抛出。
            logger.info("FactoryBean threw exception from getObjectType, despite the contract saying " + "that it should return null if the type of its object cannot be determined yet", ex);
            return null;
        }
    }

    /**
     * 通过检查属性中是否有 {@link FactoryBean#OBJECT_TYPE_ATTRIBUTE} 值来确定 FactoryBean 的类型。
     * @param attributes 要检查的属性
     * @return 从属性中提取的 {@link ResolvableType} 或 {@code ResolvableType.NONE}
     * @since 5.2
     */
    ResolvableType getTypeForFactoryBeanFromAttributes(AttributeAccessor attributes) {
        Object attribute = attributes.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE);
        if (attribute instanceof ResolvableType resolvableType) {
            return resolvableType;
        }
        if (attribute instanceof Class<?> clazz) {
            return ResolvableType.forClass(clazz);
        }
        return ResolvableType.NONE;
    }

    /**
     * 确定给定泛型声明中的 FactoryBean 对象类型。
     * @param type FactoryBean 类型
     * @return 嵌套对象类型，或如果不可解析则返回 {@code NONE}
     */
    ResolvableType getFactoryBeanGeneric(@Nullable ResolvableType type) {
        return (type != null ? type.as(FactoryBean.class).getGeneric() : ResolvableType.NONE);
    }

    /**
     * 从给定的FactoryBean中获取一个对象，如果以缓存形式可用。进行快速检查以最小化同步。
     * @param beanName Bean的名称
     * @return 从FactoryBean获取的对象，
     * 或者在不可用的情况下返回{@code null}
     */
    @Nullable
    protected Object getCachedObjectForFactoryBean(String beanName) {
        return this.factoryBeanObjectCache.get(beanName);
    }

    /**
     * 从给定的FactoryBean中获取一个可暴露的对象。
     * @param factory FactoryBean实例
     * @param beanName Bean的名称
     * @param shouldPostProcess Bean是否需要后处理
     * @return 从FactoryBean获取的对象
     * @throws BeanCreationException 如果FactoryBean对象创建失败
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
        if (factory.isSingleton() && containsSingleton(beanName)) {
            synchronized (getSingletonMutex()) {
                Object object = this.factoryBeanObjectCache.get(beanName);
                if (object == null) {
                    object = doGetObjectFromFactoryBean(factory, beanName);
                    // 只有在上述 getObject() 调用中尚未放置时才进行后处理和存储
                    // （例如，由于由自定义 getBean 调用触发的循环引用处理）
                    Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                    if (alreadyThere != null) {
                        object = alreadyThere;
                    } else {
                        if (shouldPostProcess) {
                            if (isSingletonCurrentlyInCreation(beanName)) {
                                // 暂时返回非后处理对象，尚未存储。
                                return object;
                            }
                            beforeSingletonCreation(beanName);
                            try {
                                object = postProcessObjectFromFactoryBean(object, beanName);
                            } catch (Throwable ex) {
                                throw new BeanCreationException(beanName, "Post-processing of FactoryBean's singleton object failed", ex);
                            } finally {
                                afterSingletonCreation(beanName);
                            }
                        }
                        if (containsSingleton(beanName)) {
                            this.factoryBeanObjectCache.put(beanName, object);
                        }
                    }
                }
                return object;
            }
        } else {
            Object object = doGetObjectFromFactoryBean(factory, beanName);
            if (shouldPostProcess) {
                try {
                    object = postProcessObjectFromFactoryBean(object, beanName);
                } catch (Throwable ex) {
                    throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
                }
            }
            return object;
        }
    }

    /**
     * 从给定的FactoryBean中获取一个可暴露的对象。
     * @param factory FactoryBean实例
     * @param beanName Bean的名称
     * @return 从FactoryBean获取的对象
     * @throws BeanCreationException 如果FactoryBean对象创建失败
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    private Object doGetObjectFromFactoryBean(FactoryBean<?> factory, String beanName) throws BeanCreationException {
        Object object;
        try {
            object = factory.getObject();
        } catch (FactoryBeanNotInitializedException ex) {
            throw new BeanCurrentlyInCreationException(beanName, ex.toString());
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
        }
        // 不要接受一个非完全的 FactoryBean 的 null 值
        // 尚未初始化：许多FactoryBeans只是返回null。
        if (object == null) {
            if (isSingletonCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName, "FactoryBean which is currently in creation returned null from getObject");
            }
            object = new NullBean();
        }
        return object;
    }

    /**
     * 对从FactoryBean获得的给定对象进行后处理。
     * 处理后的对象将被公开供bean引用使用。
     * <p>默认实现简单地将给定对象原样返回。
     * 子类可以覆盖此方法，例如，应用后处理器。
     * @param object 从FactoryBean获取的对象
     * @param beanName bean的名称
     * @return 要公开的对象
     * @throws org.springframework.beans.BeansException 如果任何后处理失败
     */
    protected Object postProcessObjectFromFactoryBean(Object object, String beanName) throws BeansException {
        return object;
    }

    /**
     * 尝试获取给定 bean 的 FactoryBean。
     * @param beanName bean 的名称
     * @param beanInstance 相应的 bean 实例
     * @return 作为 FactoryBean 的 bean 实例
     * @throws BeansException 如果给定的 bean 无法作为 FactoryBean 暴露
     */
    protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
        if (!(beanInstance instanceof FactoryBean<?> factoryBean)) {
            throw new BeanCreationException(beanName, "Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
        }
        return factoryBean;
    }

    /**
     * 覆盖此方法以清除 FactoryBean 对象缓存。
     */
    @Override
    protected void removeSingleton(String beanName) {
        synchronized (getSingletonMutex()) {
            super.removeSingleton(beanName);
            this.factoryBeanObjectCache.remove(beanName);
        }
    }

    /**
     * 覆盖此方法以清除 FactoryBean 对象缓存。
     */
    @Override
    protected void clearSingletonCache() {
        synchronized (getSingletonMutex()) {
            super.clearSingletonCache();
            this.factoryBeanObjectCache.clear();
        }
    }
}
