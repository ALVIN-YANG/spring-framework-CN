// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据Apache许可证版本2.0（“许可证”）授权；
* 您不得使用此文件除非符合许可证规定。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权和限制。*/
package org.springframework.beans.factory.annotation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 *  实现了 {@link org.springframework.beans.factory.config.BeanPostProcessor} 的类，用于调用注解的初始化和销毁方法。允许使用注解作为 Spring 的 {@link org.springframework.beans.factory.InitializingBean} 和 {@link org.springframework.beans.factory.DisposableBean} 回调接口的替代方案。
 *
 * <p>实际要检查的注解类型可以通过 {@link #setInitAnnotationType "initAnnotationType"} 和 {@link #setDestroyAnnotationType "destroyAnnotationType"} 属性进行配置。可以使用任何自定义注解，因为没有任何必需的注解属性。
 *
 * <p>初始化和销毁注解可以应用于任何可见性的方法：公共的、包保护的、受保护的或私有的。可以有多个这样的方法被注解，但建议分别只注解一个初始化方法和一个销毁方法。
 *
 * <p>Spring 的 {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor} 默认支持 {@link jakarta.annotation.PostConstruct} 和 {@link jakarta.annotation.PreDestroy} 注解作为初始化注解和销毁注解。此外，它还支持用于注解驱动的命名 bean 注入的 {@link jakarta.annotation.Resource} 注解。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 2.5
 * @see #setInitAnnotationType
 * @see #setDestroyAnnotationType
 */
@SuppressWarnings("serial")
public class InitDestroyAnnotationBeanPostProcessor implements DestructionAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, BeanRegistrationAotProcessor, PriorityOrdered, Serializable {

    private final transient LifecycleMetadata emptyLifecycleMetadata = new LifecycleMetadata(Object.class, Collections.emptyList(), Collections.emptyList()) {

        @Override
        public void checkInitDestroyMethods(RootBeanDefinition beanDefinition) {
        }

        @Override
        public void invokeInitMethods(Object target, String beanName) {
        }

        @Override
        public void invokeDestroyMethods(Object target, String beanName) {
        }

        @Override
        public boolean hasDestroyMethods() {
            return false;
        }
    };

    protected transient Log logger = LogFactory.getLog(getClass());

    private final Set<Class<? extends Annotation>> initAnnotationTypes = new LinkedHashSet<>(2);

    private final Set<Class<? extends Annotation>> destroyAnnotationTypes = new LinkedHashSet<>(2);

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Nullable
    private final transient Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache = new ConcurrentHashMap<>(256);

    /**
     * 指定要检查的初始化注解，表示在配置完一个bean之后要调用的初始化方法。
     * <p>可以使用任何自定义注解，因为没有必需的注解属性。没有默认值，尽管通常的选择是使用 {@link jakarta.annotation.PostConstruct} 注解。
     * @see #addInitAnnotationType
     */
    public void setInitAnnotationType(Class<? extends Annotation> initAnnotationType) {
        this.initAnnotationTypes.clear();
        this.initAnnotationTypes.add(initAnnotationType);
    }

    /**
     * 添加一个init注解，用于标识初始化方法，表示在配置bean之后需要调用的方法。
     * @since 6.0.11
     * @see #setInitAnnotationType
     */
    public void addInitAnnotationType(@Nullable Class<? extends Annotation> initAnnotationType) {
        if (initAnnotationType != null) {
            this.initAnnotationTypes.add(initAnnotationType);
        }
    }

    /**
     * 指定要检查的销毁注解，表示在上下文关闭时调用的销毁方法。
     * <p>可以使用任何自定义注解，因为没有要求必须的注解属性。没有默认值，尽管通常选择使用 {@link jakarta.annotation.PreDestroy} 注解。
     * @see #addDestroyAnnotationType
     */
    public void setDestroyAnnotationType(Class<? extends Annotation> destroyAnnotationType) {
        this.destroyAnnotationTypes.clear();
        this.destroyAnnotationTypes.add(destroyAnnotationType);
    }

    /**
     * 添加一个销毁注解，用于检查，指示在上下文关闭时需要调用的销毁方法。
     * @since 6.0.11
     * @see #setDestroyAnnotationType
     */
    public void addDestroyAnnotationType(@Nullable Class<? extends Annotation> destroyAnnotationType) {
        if (destroyAnnotationType != null) {
            this.destroyAnnotationTypes.add(destroyAnnotationType);
        }
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanClass, String beanName) {
        findLifecycleMetadata(beanDefinition, beanClass);
    }

    @Override
    @Nullable
    public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
        RootBeanDefinition beanDefinition = registeredBean.getMergedBeanDefinition();
        beanDefinition.resolveDestroyMethodIfNecessary();
        LifecycleMetadata metadata = findLifecycleMetadata(beanDefinition, registeredBean.getBeanClass());
        if (!CollectionUtils.isEmpty(metadata.initMethods)) {
            String[] initMethodNames = safeMerge(beanDefinition.getInitMethodNames(), metadata.initMethods);
            beanDefinition.setInitMethodNames(initMethodNames);
        }
        if (!CollectionUtils.isEmpty(metadata.destroyMethods)) {
            String[] destroyMethodNames = safeMerge(beanDefinition.getDestroyMethodNames(), metadata.destroyMethods);
            beanDefinition.setDestroyMethodNames(destroyMethodNames);
        }
        return null;
    }

    private LifecycleMetadata findLifecycleMetadata(RootBeanDefinition beanDefinition, Class<?> beanClass) {
        LifecycleMetadata metadata = findLifecycleMetadata(beanClass);
        metadata.checkInitDestroyMethods(beanDefinition);
        return metadata;
    }

    private static String[] safeMerge(@Nullable String[] existingNames, Collection<LifecycleMethod> detectedMethods) {
        Stream<String> detectedNames = detectedMethods.stream().map(LifecycleMethod::getIdentifier);
        Stream<String> mergedNames = (existingNames != null ? Stream.concat(detectedNames, Stream.of(existingNames)) : detectedNames);
        return mergedNames.distinct().toArray(String[]::new);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
        try {
            metadata.invokeInitMethods(bean, beanName);
        } catch (InvocationTargetException ex) {
            throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Failed to invoke init method", ex);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
        try {
            metadata.invokeDestroyMethods(bean, beanName);
        } catch (InvocationTargetException ex) {
            String msg = "Destroy method on bean with name '" + beanName + "' threw an exception";
            if (logger.isDebugEnabled()) {
                logger.warn(msg, ex.getTargetException());
            } else if (logger.isWarnEnabled()) {
                logger.warn(msg + ": " + ex.getTargetException());
            }
        } catch (Throwable ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to invoke destroy method on bean with name '" + beanName + "'", ex);
            }
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return findLifecycleMetadata(bean.getClass()).hasDestroyMethods();
    }

    private LifecycleMetadata findLifecycleMetadata(Class<?> beanClass) {
        if (this.lifecycleMetadataCache == null) {
            // 在反序列化之后，在销毁过程中发生...
            return buildLifecycleMetadata(beanClass);
        }
        // 首先快速检查并发映射，使用最小化锁定。
        LifecycleMetadata metadata = this.lifecycleMetadataCache.get(beanClass);
        if (metadata == null) {
            synchronized (this.lifecycleMetadataCache) {
                metadata = this.lifecycleMetadataCache.get(beanClass);
                if (metadata == null) {
                    metadata = buildLifecycleMetadata(beanClass);
                    this.lifecycleMetadataCache.put(beanClass, metadata);
                }
                return metadata;
            }
        }
        return metadata;
    }

    private LifecycleMetadata buildLifecycleMetadata(final Class<?> beanClass) {
        if (!AnnotationUtils.isCandidateClass(beanClass, this.initAnnotationTypes) && !AnnotationUtils.isCandidateClass(beanClass, this.destroyAnnotationTypes)) {
            return this.emptyLifecycleMetadata;
        }
        List<LifecycleMethod> initMethods = new ArrayList<>();
        List<LifecycleMethod> destroyMethods = new ArrayList<>();
        Class<?> currentClass = beanClass;
        do {
            final List<LifecycleMethod> currInitMethods = new ArrayList<>();
            final List<LifecycleMethod> currDestroyMethods = new ArrayList<>();
            ReflectionUtils.doWithLocalMethods(currentClass, method -> {
                for (Class<? extends Annotation> initAnnotationType : this.initAnnotationTypes) {
                    if (initAnnotationType != null && method.isAnnotationPresent(initAnnotationType)) {
                        currInitMethods.add(new LifecycleMethod(method, beanClass));
                        if (logger.isTraceEnabled()) {
                            logger.trace("Found init method on class [" + beanClass.getName() + "]: " + method);
                        }
                    }
                }
                for (Class<? extends Annotation> destroyAnnotationType : this.destroyAnnotationTypes) {
                    if (destroyAnnotationType != null && method.isAnnotationPresent(destroyAnnotationType)) {
                        currDestroyMethods.add(new LifecycleMethod(method, beanClass));
                        if (logger.isTraceEnabled()) {
                            logger.trace("Found destroy method on class [" + beanClass.getName() + "]: " + method);
                        }
                    }
                }
            });
            initMethods.addAll(0, currInitMethods);
            destroyMethods.addAll(currDestroyMethods);
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null && currentClass != Object.class);
        return (initMethods.isEmpty() && destroyMethods.isEmpty() ? this.emptyLifecycleMetadata : new LifecycleMetadata(beanClass, initMethods, destroyMethods));
    }

    // 很抱歉，您提供的代码注释内容为空。请提供具体的 Java 代码注释内容，以便我能够将其翻译成中文。
    // 序列化支持
    // 很抱歉，您只提供了代码注释前的横线（"---------------------------------------------------------------------"），并没有提供实际的代码注释内容。请提供具体的代码注释内容，我才能为您进行翻译。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 依赖默认序列化；只需在反序列化后初始化状态。
        ois.defaultReadObject();
        // 初始化瞬态字段。
        this.logger = LogFactory.getLog(getClass());
    }

    /**
     * 表示注解的初始化和销毁方法信息的类。
     */
    private class LifecycleMetadata {

        private final Class<?> beanClass;

        private final Collection<LifecycleMethod> initMethods;

        private final Collection<LifecycleMethod> destroyMethods;

        @Nullable
        private volatile Set<LifecycleMethod> checkedInitMethods;

        @Nullable
        private volatile Set<LifecycleMethod> checkedDestroyMethods;

        public LifecycleMetadata(Class<?> beanClass, Collection<LifecycleMethod> initMethods, Collection<LifecycleMethod> destroyMethods) {
            this.beanClass = beanClass;
            this.initMethods = initMethods;
            this.destroyMethods = destroyMethods;
        }

        public void checkInitDestroyMethods(RootBeanDefinition beanDefinition) {
            Set<LifecycleMethod> checkedInitMethods = new LinkedHashSet<>(this.initMethods.size());
            for (LifecycleMethod lifecycleMethod : this.initMethods) {
                String methodIdentifier = lifecycleMethod.getIdentifier();
                if (!beanDefinition.isExternallyManagedInitMethod(methodIdentifier)) {
                    beanDefinition.registerExternallyManagedInitMethod(methodIdentifier);
                    checkedInitMethods.add(lifecycleMethod);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Registered init method on class [" + this.beanClass.getName() + "]: " + methodIdentifier);
                    }
                }
            }
            Set<LifecycleMethod> checkedDestroyMethods = new LinkedHashSet<>(this.destroyMethods.size());
            for (LifecycleMethod lifecycleMethod : this.destroyMethods) {
                String methodIdentifier = lifecycleMethod.getIdentifier();
                if (!beanDefinition.isExternallyManagedDestroyMethod(methodIdentifier)) {
                    beanDefinition.registerExternallyManagedDestroyMethod(methodIdentifier);
                    checkedDestroyMethods.add(lifecycleMethod);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Registered destroy method on class [" + this.beanClass.getName() + "]: " + methodIdentifier);
                    }
                }
            }
            this.checkedInitMethods = checkedInitMethods;
            this.checkedDestroyMethods = checkedDestroyMethods;
        }

        public void invokeInitMethods(Object target, String beanName) throws Throwable {
            Collection<LifecycleMethod> checkedInitMethods = this.checkedInitMethods;
            Collection<LifecycleMethod> initMethodsToIterate = (checkedInitMethods != null ? checkedInitMethods : this.initMethods);
            if (!initMethodsToIterate.isEmpty()) {
                for (LifecycleMethod lifecycleMethod : initMethodsToIterate) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Invoking init method on bean '" + beanName + "': " + lifecycleMethod.getMethod());
                    }
                    lifecycleMethod.invoke(target);
                }
            }
        }

        public void invokeDestroyMethods(Object target, String beanName) throws Throwable {
            Collection<LifecycleMethod> checkedDestroyMethods = this.checkedDestroyMethods;
            Collection<LifecycleMethod> destroyMethodsToUse = (checkedDestroyMethods != null ? checkedDestroyMethods : this.destroyMethods);
            if (!destroyMethodsToUse.isEmpty()) {
                for (LifecycleMethod lifecycleMethod : destroyMethodsToUse) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Invoking destroy method on bean '" + beanName + "': " + lifecycleMethod.getMethod());
                    }
                    lifecycleMethod.invoke(target);
                }
            }
        }

        public boolean hasDestroyMethods() {
            Collection<LifecycleMethod> checkedDestroyMethods = this.checkedDestroyMethods;
            Collection<LifecycleMethod> destroyMethodsToUse = (checkedDestroyMethods != null ? checkedDestroyMethods : this.destroyMethods);
            return !destroyMethodsToUse.isEmpty();
        }
    }

    /**
     * 代表注解初始化或销毁方法的类。
     */
    private static class LifecycleMethod {

        private final Method method;

        private final String identifier;

        public LifecycleMethod(Method method, Class<?> beanClass) {
            if (method.getParameterCount() != 0) {
                throw new IllegalStateException("Lifecycle annotation requires a no-arg method: " + method);
            }
            this.method = method;
            this.identifier = (isPrivateOrNotVisible(method, beanClass) ? ClassUtils.getQualifiedMethodName(method) : method.getName());
        }

        public Method getMethod() {
            return this.method;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public void invoke(Object target) throws Throwable {
            ReflectionUtils.makeAccessible(this.method);
            this.method.invoke(target);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof LifecycleMethod that && this.identifier.equals(that.identifier)));
        }

        @Override
        public int hashCode() {
            return this.identifier.hashCode();
        }

        /**
         * 判断提供的生命周期 {@link Method} 是否对提供的bean {@link Class} 可见，即是否为私有
         * @since 6.0.11
         */
        private static boolean isPrivateOrNotVisible(Method method, Class<?> beanClass) {
            int modifiers = method.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                return true;
            }
            // 方法是在位于不同包中的类中声明的
            // 比Bean类和方法既不是public也不是protected？
            return (!method.getDeclaringClass().getPackageName().equals(beanClass.getPackageName()) && !(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)));
        }
    }
}
