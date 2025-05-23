// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.support;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 实现了 {@link DisposableBean} 和 {@link Runnable} 接口的适配器，在给定的 bean 实例上执行各种销毁步骤：
 * <ul>
 * <li>销毁感知的 Bean 后置处理器；
 * <li>实现 DisposableBean 接口的 bean 本身；
 * <li>在 bean 定义中指定的自定义销毁方法。
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @author Sebastien Deleuze
 * @since 2.0
 * @see AbstractBeanFactory
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
 * @see AbstractBeanDefinition#getDestroyMethodNames()
 */
@SuppressWarnings("serial")
class DisposableBeanAdapter implements DisposableBean, Runnable, Serializable {

    private static final String DESTROY_METHOD_NAME = "destroy";

    private static final String CLOSE_METHOD_NAME = "close";

    private static final String SHUTDOWN_METHOD_NAME = "shutdown";

    private static final Log logger = LogFactory.getLog(DisposableBeanAdapter.class);

    private final Object bean;

    private final String beanName;

    private final boolean nonPublicAccessAllowed;

    private final boolean invokeDisposableBean;

    private boolean invokeAutoCloseable;

    @Nullable
    private String[] destroyMethodNames;

    @Nullable
    private transient Method[] destroyMethods;

    @Nullable
    private final List<DestructionAwareBeanPostProcessor> beanPostProcessors;

    /**
     * 为给定的bean创建一个新的DisposableBeanAdapter。
     * @param bean bean实例（永远不会为{@code null}）
     * @param beanName bean的名称
     * @param beanDefinition 合并后的bean定义
     * @param postProcessors BeanPostProcessors的列表（可能包含DestructionAwareBeanPostProcessor），如果有
     */
    public DisposableBeanAdapter(Object bean, String beanName, RootBeanDefinition beanDefinition, List<DestructionAwareBeanPostProcessor> postProcessors) {
        Assert.notNull(bean, "Disposable bean must not be null");
        this.bean = bean;
        this.beanName = beanName;
        this.nonPublicAccessAllowed = beanDefinition.isNonPublicAccessAllowed();
        this.invokeDisposableBean = (bean instanceof DisposableBean && !beanDefinition.hasAnyExternallyManagedDestroyMethod(DESTROY_METHOD_NAME));
        String[] destroyMethodNames = inferDestroyMethodsIfNecessary(bean.getClass(), beanDefinition);
        if (!ObjectUtils.isEmpty(destroyMethodNames) && !(this.invokeDisposableBean && DESTROY_METHOD_NAME.equals(destroyMethodNames[0])) && !beanDefinition.hasAnyExternallyManagedDestroyMethod(destroyMethodNames[0])) {
            this.invokeAutoCloseable = (bean instanceof AutoCloseable && CLOSE_METHOD_NAME.equals(destroyMethodNames[0]));
            if (!this.invokeAutoCloseable) {
                this.destroyMethodNames = destroyMethodNames;
                List<Method> destroyMethods = new ArrayList<>(destroyMethodNames.length);
                for (String destroyMethodName : destroyMethodNames) {
                    Method destroyMethod = determineDestroyMethod(destroyMethodName);
                    if (destroyMethod == null) {
                        if (beanDefinition.isEnforceDestroyMethod()) {
                            throw new BeanDefinitionValidationException("Could not find a destroy method named '" + destroyMethodName + "' on bean with name '" + beanName + "'");
                        }
                    } else {
                        if (destroyMethod.getParameterCount() > 0) {
                            Class<?>[] paramTypes = destroyMethod.getParameterTypes();
                            if (paramTypes.length > 1) {
                                throw new BeanDefinitionValidationException("Method '" + destroyMethodName + "' of bean '" + beanName + "' has more than one parameter - not supported as destroy method");
                            } else if (paramTypes.length == 1 && boolean.class != paramTypes[0]) {
                                throw new BeanDefinitionValidationException("Method '" + destroyMethodName + "' of bean '" + beanName + "' has a non-boolean parameter - not supported as destroy method");
                            }
                        }
                        destroyMethod = ClassUtils.getInterfaceMethodIfPossible(destroyMethod, bean.getClass());
                        destroyMethods.add(destroyMethod);
                    }
                }
                this.destroyMethods = destroyMethods.toArray(Method[]::new);
            }
        }
        this.beanPostProcessors = filterPostProcessors(postProcessors, bean);
    }

    /**
     * 为给定的Bean创建一个新的DisposableBeanAdapter。
     * @param bean Bean实例（永远不会为{@code null}）
     * @param postProcessors 可选的BeanPostProcessors列表（可能包含DestructionAwareBeanPostProcessor），如果有的话
     */
    public DisposableBeanAdapter(Object bean, List<DestructionAwareBeanPostProcessor> postProcessors) {
        Assert.notNull(bean, "Disposable bean must not be null");
        this.bean = bean;
        this.beanName = bean.getClass().getName();
        this.nonPublicAccessAllowed = true;
        this.invokeDisposableBean = (this.bean instanceof DisposableBean);
        this.beanPostProcessors = filterPostProcessors(postProcessors, bean);
    }

    /**
     * 为指定的bean创建一个新的DisposableBeanAdapter。
     */
    private DisposableBeanAdapter(Object bean, String beanName, boolean nonPublicAccessAllowed, boolean invokeDisposableBean, boolean invokeAutoCloseable, @Nullable String[] destroyMethodNames, @Nullable List<DestructionAwareBeanPostProcessor> postProcessors) {
        this.bean = bean;
        this.beanName = beanName;
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
        this.invokeDisposableBean = invokeDisposableBean;
        this.invokeAutoCloseable = invokeAutoCloseable;
        this.destroyMethodNames = destroyMethodNames;
        this.beanPostProcessors = postProcessors;
    }

    @Override
    public void run() {
        destroy();
    }

    @Override
    public void destroy() {
        if (!CollectionUtils.isEmpty(this.beanPostProcessors)) {
            for (DestructionAwareBeanPostProcessor processor : this.beanPostProcessors) {
                processor.postProcessBeforeDestruction(this.bean, this.beanName);
            }
        }
        if (this.invokeDisposableBean) {
            if (logger.isTraceEnabled()) {
                logger.trace("Invoking destroy() on bean with name '" + this.beanName + "'");
            }
            try {
                ((DisposableBean) this.bean).destroy();
            } catch (Throwable ex) {
                if (logger.isWarnEnabled()) {
                    String msg = "Invocation of destroy method failed on bean with name '" + this.beanName + "'";
                    if (logger.isDebugEnabled()) {
                        // 日志记录如下，但仅在调试级别添加异常堆栈跟踪：
                        logger.warn(msg, ex);
                    } else {
                        logger.warn(msg + ": " + ex);
                    }
                }
            }
        }
        if (this.invokeAutoCloseable) {
            if (logger.isTraceEnabled()) {
                logger.trace("Invoking close() on bean with name '" + this.beanName + "'");
            }
            try {
                ((AutoCloseable) this.bean).close();
            } catch (Throwable ex) {
                if (logger.isWarnEnabled()) {
                    String msg = "Invocation of close method failed on bean with name '" + this.beanName + "'";
                    if (logger.isDebugEnabled()) {
                        // 将日志记录在警告级别，如下所示，但仅在调试级别添加异常堆栈跟踪。
                        logger.warn(msg, ex);
                    } else {
                        logger.warn(msg + ": " + ex);
                    }
                }
            }
        } else if (this.destroyMethods != null) {
            for (Method destroyMethod : this.destroyMethods) {
                invokeCustomDestroyMethod(destroyMethod);
            }
        } else if (this.destroyMethodNames != null) {
            for (String destroyMethodName : this.destroyMethodNames) {
                Method destroyMethod = determineDestroyMethod(destroyMethodName);
                if (destroyMethod != null) {
                    invokeCustomDestroyMethod(ClassUtils.getInterfaceMethodIfPossible(destroyMethod, this.bean.getClass()));
                }
            }
        }
    }

    @Nullable
    private Method determineDestroyMethod(String destroyMethodName) {
        try {
            Class<?> beanClass = this.bean.getClass();
            MethodDescriptor descriptor = MethodDescriptor.create(this.beanName, beanClass, destroyMethodName);
            String methodName = descriptor.methodName();
            Method destroyMethod = findDestroyMethod(descriptor.declaringClass(), methodName);
            if (destroyMethod != null) {
                return destroyMethod;
            }
            for (Class<?> beanInterface : ClassUtils.getAllInterfacesForClass(beanClass)) {
                destroyMethod = findDestroyMethod(beanInterface, methodName);
                if (destroyMethod != null) {
                    return destroyMethod;
                }
            }
            return null;
        } catch (IllegalArgumentException ex) {
            throw new BeanDefinitionValidationException("Could not find unique destroy method on bean with name '" + this.beanName + ": " + ex.getMessage());
        }
    }

    @Nullable
    private Method findDestroyMethod(Class<?> clazz, String name) {
        return (this.nonPublicAccessAllowed ? BeanUtils.findMethodWithMinimalParameters(clazz, name) : BeanUtils.findMethodWithMinimalParameters(clazz.getMethods(), name));
    }

    /**
     * 在给定的 bean 上调用指定的自定义销毁方法。
     * <p>此实现如果找到无参方法，则调用该方法；否则检查是否存在一个单参数布尔类型的方法（传入 "true"，假设为 "force" 参数），否则记录错误。
     */
    private void invokeCustomDestroyMethod(Method destroyMethod) {
        int paramCount = destroyMethod.getParameterCount();
        Object[] args = new Object[paramCount];
        if (paramCount == 1) {
            args[0] = Boolean.TRUE;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Invoking custom destroy method '" + destroyMethod.getName() + "' on bean with name '" + this.beanName + "'");
        }
        try {
            ReflectionUtils.makeAccessible(destroyMethod);
            destroyMethod.invoke(this.bean, args);
        } catch (InvocationTargetException ex) {
            if (logger.isWarnEnabled()) {
                String msg = "Custom destroy method '" + destroyMethod.getName() + "' on bean with name '" + this.beanName + "' threw an exception";
                if (logger.isDebugEnabled()) {
                    // 将日志记录在警告级别，如下所示，但仅在调试级别添加异常堆栈跟踪：
                    logger.warn(msg, ex.getTargetException());
                } else {
                    logger.warn(msg + ": " + ex.getTargetException());
                }
            }
        } catch (Throwable ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to invoke custom destroy method '" + destroyMethod.getName() + "' on bean with name '" + this.beanName + "'", ex);
            }
        }
    }

    /**
     * 序列化此类的状态副本，
     * 过滤掉不可序列化的Bean后处理器。
     */
    protected Object writeReplace() {
        List<DestructionAwareBeanPostProcessor> serializablePostProcessors = null;
        if (this.beanPostProcessors != null) {
            serializablePostProcessors = new ArrayList<>();
            for (DestructionAwareBeanPostProcessor postProcessor : this.beanPostProcessors) {
                if (postProcessor instanceof Serializable) {
                    serializablePostProcessors.add(postProcessor);
                }
            }
        }
        return new DisposableBeanAdapter(this.bean, this.beanName, this.nonPublicAccessAllowed, this.invokeDisposableBean, this.invokeAutoCloseable, this.destroyMethodNames, serializablePostProcessors);
    }

    /**
     * 检查给定的 Bean 是否有可调用的任何类型的销毁方法。
     * @param bean Bean 实例
     * @param beanDefinition 相应的 Bean 定义
     */
    public static boolean hasDestroyMethod(Object bean, RootBeanDefinition beanDefinition) {
        return (bean instanceof DisposableBean || inferDestroyMethodsIfNecessary(bean.getClass(), beanDefinition) != null);
    }

    /**
     * 如果给定beanDefinition的"destroyMethodName"属性的当前值是AbstractBeanDefinition#INFER_METHOD，则尝试推断一个销毁方法。候选方法目前仅限于名为"close"或"shutdown"的公共无参方法（无论是否本地声明或继承）。如果没有找到这样的方法，给定BeanDefinition的"destroyMethodName"将被更新为null，否则将设置为推断方法的名字。此常量作为@Bean#destroyMethod属性的默认值，并且常量的值也可以在XML中使用，在<bean destroy-method="">或<beans default-destroy-method="">属性中。
     * <p>同时处理java.io.Closeable和java.lang.AutoCloseable接口，反射地调用实现bean上的"close"方法。
     */
    @Nullable
    static String[] inferDestroyMethodsIfNecessary(Class<?> target, RootBeanDefinition beanDefinition) {
        String[] destroyMethodNames = beanDefinition.getDestroyMethodNames();
        if (destroyMethodNames != null && destroyMethodNames.length > 1) {
            return destroyMethodNames;
        }
        String destroyMethodName = beanDefinition.resolvedDestroyMethodName;
        if (destroyMethodName == null) {
            destroyMethodName = beanDefinition.getDestroyMethodName();
            boolean autoCloseable = (AutoCloseable.class.isAssignableFrom(target));
            if (AbstractBeanDefinition.INFER_METHOD.equals(destroyMethodName) || (destroyMethodName == null && autoCloseable)) {
                // 仅在bean的情况下执行销毁方法推断
                // 未显式实现 DisposableBean 接口
                destroyMethodName = null;
                if (!(DisposableBean.class.isAssignableFrom(target))) {
                    if (autoCloseable) {
                        destroyMethodName = CLOSE_METHOD_NAME;
                    } else {
                        try {
                            destroyMethodName = target.getMethod(CLOSE_METHOD_NAME).getName();
                        } catch (NoSuchMethodException ex) {
                            try {
                                destroyMethodName = target.getMethod(SHUTDOWN_METHOD_NAME).getName();
                            } catch (NoSuchMethodException ex2) {
                                // 未找到候选的销毁方法
                            }
                        }
                    }
                }
            }
            beanDefinition.resolvedDestroyMethodName = (destroyMethodName != null ? destroyMethodName : "");
        }
        return (StringUtils.hasLength(destroyMethodName) ? new String[] { destroyMethodName } : null);
    }

    /**
     * 检查给定的 Bean 是否有针对它的破坏感知后处理器正在应用。
     * @param bean Bean 实例
     * @param postProcessors 后处理器候选者
     */
    public static boolean hasApplicableProcessors(Object bean, List<DestructionAwareBeanPostProcessor> postProcessors) {
        if (!CollectionUtils.isEmpty(postProcessors)) {
            for (DestructionAwareBeanPostProcessor processor : postProcessors) {
                if (processor.requiresDestruction(bean)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 在列表中搜索所有 DestructionAwareBeanPostProcessors。
     * @param processors 要搜索的列表
     * @return 过滤后的 DestructionAwareBeanPostProcessors 列表
     */
    @Nullable
    private static List<DestructionAwareBeanPostProcessor> filterPostProcessors(List<DestructionAwareBeanPostProcessor> processors, Object bean) {
        List<DestructionAwareBeanPostProcessor> filteredPostProcessors = null;
        if (!CollectionUtils.isEmpty(processors)) {
            filteredPostProcessors = new ArrayList<>(processors.size());
            for (DestructionAwareBeanPostProcessor processor : processors) {
                if (processor.requiresDestruction(bean)) {
                    filteredPostProcessors.add(processor);
                }
            }
        }
        return filteredPostProcessors;
    }
}
