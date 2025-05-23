// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”），您可能不得使用此文件除非符合许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.aspectj.SimpleAspectInstanceFactory;
import org.springframework.aop.framework.ProxyCreatorSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 基于AspectJ的代理工厂，允许以编程方式构建包含AspectJ切面的代理（包括代码风格和注解风格）。
 *
 *  作者：Rob Harrop
 *  作者：Juergen Hoeller
 *  作者：Ramnivas Laddad
 *  自2.0版本起
 *  查看：#addAspect(Object)
 *  查看：#addAspect(Class)
 *  查看：#getProxy()
 *  查看：#getProxy(ClassLoader)
 *  查看：org.springframework.aop.framework.ProxyFactory
 */
@SuppressWarnings("serial")
public class AspectJProxyFactory extends ProxyCreatorSupport {

    /**
     * 缓存单例方面实例的缓存。
     */
    private static final Map<Class<?>, Object> aspectCache = new ConcurrentHashMap<>();

    private final AspectJAdvisorFactory aspectFactory = new ReflectiveAspectJAdvisorFactory();

    /**
     * 创建一个新的 AspectJProxyFactory。
     */
    public AspectJProxyFactory() {
    }

    /**
     * 创建一个新的AspectJProxyFactory。
     * <p>将代理给定目标实现的全部接口。
     * @param target 需要被代理的目标对象
     */
    public AspectJProxyFactory(Object target) {
        Assert.notNull(target, "Target object must not be null");
        setInterfaces(ClassUtils.getAllInterfaces(target));
        setTarget(target);
    }

    /**
     * 创建一个新的 {@code AspectJProxyFactory}。
     * 没有目标对象，只有接口。必须添加拦截器。
     */
    public AspectJProxyFactory(Class<?>... interfaces) {
        setInterfaces(interfaces);
    }

    /**
     * 将提供的方面实例添加到链中。提供的方面实例的类型必须是单例方面。使用此方法时不会遵守真正的单例生命周期 - 调用者负责管理以这种方式添加的任何方面的生命周期。
     * @param aspectInstance AspectJ方面实例
     */
    public void addAspect(Object aspectInstance) {
        Class<?> aspectClass = aspectInstance.getClass();
        String aspectName = aspectClass.getName();
        AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
        if (am.getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON) {
            throw new IllegalArgumentException("Aspect class [" + aspectClass.getName() + "] does not define a singleton aspect");
        }
        addAdvisorsFromAspectInstanceFactory(new SingletonMetadataAwareAspectInstanceFactory(aspectInstance, aspectName));
    }

    /**
     * 将指定类型的方面添加到通知链的末尾。
     * @param aspectClass AspectJ 方面类
     */
    public void addAspect(Class<?> aspectClass) {
        String aspectName = aspectClass.getName();
        AspectMetadata am = createAspectMetadata(aspectClass, aspectName);
        MetadataAwareAspectInstanceFactory instanceFactory = createAspectInstanceFactory(am, aspectClass, aspectName);
        addAdvisorsFromAspectInstanceFactory(instanceFactory);
    }

    /**
     * 将从提供的 {@link MetadataAwareAspectInstanceFactory} 中添加所有 {@link Advisor Advisor} 到当前链中。如有需要，则公开任何特殊用途的 {@link Advisor Advisor}。
     * @see AspectJProxyUtils#makeAdvisorChainAspectJCapableIfNecessary(List)
     */
    private void addAdvisorsFromAspectInstanceFactory(MetadataAwareAspectInstanceFactory instanceFactory) {
        List<Advisor> advisors = this.aspectFactory.getAdvisors(instanceFactory);
        Class<?> targetClass = getTargetClass();
        Assert.state(targetClass != null, "Unresolvable target class");
        advisors = AopUtils.findAdvisorsThatCanApply(advisors, targetClass);
        AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(advisors);
        AnnotationAwareOrderComparator.sort(advisors);
        addAdvisors(advisors);
    }

    /**
     * 为提供的方面类型创建一个 {@link AspectMetadata} 实例。
     */
    private AspectMetadata createAspectMetadata(Class<?> aspectClass, String aspectName) {
        AspectMetadata am = new AspectMetadata(aspectClass, aspectName);
        if (!am.getAjType().isAspect()) {
            throw new IllegalArgumentException("Class [" + aspectClass.getName() + "] is not a valid aspect type");
        }
        return am;
    }

    /**
     * 为提供的切面类型创建一个 {@link MetadataAwareAspectInstanceFactory}。如果切面类型没有指定per子句，则返回一个 {@link SingletonMetadataAwareAspectInstanceFactory}，否则返回一个 {@link PrototypeAspectInstanceFactory}。
     */
    private MetadataAwareAspectInstanceFactory createAspectInstanceFactory(AspectMetadata am, Class<?> aspectClass, String aspectName) {
        MetadataAwareAspectInstanceFactory instanceFactory;
        if (am.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
            // 创建一个共享的方面实例。
            Object instance = getSingletonAspectInstance(aspectClass);
            instanceFactory = new SingletonMetadataAwareAspectInstanceFactory(instance, aspectName);
        } else {
            // 创建一个用于独立方面实例的工厂。
            instanceFactory = new SimpleMetadataAwareAspectInstanceFactory(aspectClass, aspectName);
        }
        return instanceFactory;
    }

    /**
     * 获取指定方面类型的单例方面实例。
     * 如果在实例缓存中找不到实例，则创建一个新的实例。
     */
    private Object getSingletonAspectInstance(Class<?> aspectClass) {
        return aspectCache.computeIfAbsent(aspectClass, clazz -> new SimpleAspectInstanceFactory(clazz).getAspectInstance());
    }

    /**
     * 根据此工厂中的设置创建一个新的代理。
     * <p>可以重复调用。如果添加或移除了接口，效果可能会有所不同。可以添加和移除拦截器。
     * <p>使用默认类加载器：通常情况下，是线程上下文类加载器（如果创建代理时需要）。
     * @return 新的代理
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy() {
        return (T) createAopProxy().getProxy();
    }

    /**
     * 根据本工厂中的设置创建一个新的代理。
     * <p>可以重复调用。如果已经添加或删除了接口，效果可能会有所不同。可以添加和删除拦截器。
     * <p>使用给定的类加载器（如果代理创建需要）。
     * @param classLoader 用于创建代理的类加载器
     * @return 新的代理
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(ClassLoader classLoader) {
        return (T) createAopProxy().getProxy(classLoader);
    }
}
