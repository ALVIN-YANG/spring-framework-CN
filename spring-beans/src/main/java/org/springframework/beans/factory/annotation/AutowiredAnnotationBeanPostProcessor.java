// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aot.generate.AccessControl;
import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.support.ClassHintUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.aot.AutowiredArgumentsCodeGenerator;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.aot.AutowiredMethodArgumentsResolver;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 *  {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}
 * 实现，它自动注入注解的字段、setter 方法以及任意配置方法。要注入的这些成员通过以下注解检测：
 * 默认情况下，使用 Spring 的 {@link Autowired @Autowired} 和 {@link Value @Value}
 * 注解。
 *
 * <p>如果可用，还支持常见的 {@link jakarta.inject.Inject @Inject} 注解，作为 Spring 自身
 * 的 {@code @Autowired} 的直接替代。此外，它还保留了支持回溯到原始 JSR-330 规范的
 * {@code javax.inject.Inject} 变体（如 Java EE 6-8 所知）。
 *
 * <h3>自动注入构造函数</h3>
 * <p>任何给定的 bean 类只能有一个构造函数可以声明此注解，并将 'required' 属性设置为
 * {@code true}，表示当用作 Spring bean 时，将自动注入 <i>该</i> 构造函数。此外，如果
 * 'required' 属性设置为 {@code true}，则只能有一个构造函数可以注解为
 * {@code @Autowired}。如果有多个 <i>非必需</i> 的构造函数声明了此注解，它们将被视为
 * 自动注入的候选者。具有最多依赖关系的构造函数（可以通过匹配 Spring 容器中的 bean
 * 来满足）将被选择。如果没有候选者可以满足，则将使用主要/默认构造函数（如果存在）。
 * 如果一个类最初只声明了一个构造函数，则它将始终被使用，即使没有注解。注解的构造函数不需要是
 * public 的。
 *
 * <h3>自动注入字段</h3>
 * <p>字段在 bean 构造之后立即注入，在调用任何配置方法之前。这样的配置字段不需要是
 * public 的。
 *
 * <h3>自动注入方法</h3>
 * <p>配置方法可以有任意名称和任意数量的参数；这些参数中的每个都将通过匹配 Spring
 * 容器中的 bean 进行自动注入。bean 属性 setter 方法实际上是此类通用配置方法的特例。
 * 配置方法不需要是 public 的。
 *
 * <h3>注解配置与 XML 配置</h3>
 * <p>默认情况下，将通过 "context:annotation-config" 和 "context:component-scan" XML 标签注册
 * 默认的 {@code AutowiredAnnotationBeanPostProcessor}。如果您打算指定自定义的
 * {@code AutowiredAnnotationBeanPostProcessor} bean 定义，请删除或关闭默认的注解配置。
 *
 * <p><b>注意：</b>注解注入将在 XML 注入之前执行；因此，后者配置将覆盖前者，对于通过这两种方法
 * 连接的属性。
 *
 * <h3>{@literal @}Lookup 方法</h3>
 * <p>除了上面讨论的常规注入点之外，此后处理器还处理 Spring 的
 * {@link Lookup @Lookup} 注解，该注解标识在运行时由容器替换的查找方法。这本质上是一种类型安全的
 * 版本 of {@code getBean(Class, args)} 和 {@code getBean(String, args)}。
 * 有关详细信息，请参阅 {@link Lookup @Lookup} 的 javadoc。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 2.5
 * @see #setAutowiredAnnotationType
 * @see Autowired
 * @see Value
 */
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, BeanRegistrationAotProcessor, PriorityOrdered, BeanFactoryAware {

    private static final Constructor<?>[] EMPTY_CONSTRUCTOR_ARRAY = new Constructor<?>[0];

    protected final Log logger = LogFactory.getLog(getClass());

    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

    private String requiredParameterName = "required";

    private boolean requiredParameterValue = true;

    private int order = Ordered.LOWEST_PRECEDENCE - 2;

    @Nullable
    private ConfigurableListableBeanFactory beanFactory;

    @Nullable
    private MetadataReaderFactory metadataReaderFactory;

    private final Set<String> lookupMethodsChecked = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);

    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    /**
     * 为 Spring 的标准 {@link Autowired @Autowired} 和 {@link Value @Value} 注解创建一个新的 {@code AutowiredAnnotationBeanPostProcessor}。
     * <p>如果可用，还支持常见的 {@link jakarta.inject.Inject @Inject} 注解，以及原始的 {@code javax.inject.Inject} 变体。
     */
    @SuppressWarnings("unchecked")
    public AutowiredAnnotationBeanPostProcessor() {
        this.autowiredAnnotationTypes.add(Autowired.class);
        this.autowiredAnnotationTypes.add(Value.class);
        ClassLoader classLoader = AutowiredAnnotationBeanPostProcessor.class.getClassLoader();
        try {
            this.autowiredAnnotationTypes.add((Class<? extends Annotation>) ClassUtils.forName("jakarta.inject.Inject", classLoader));
            logger.trace("'jakarta.inject.Inject' annotation found and supported for autowiring");
        } catch (ClassNotFoundException ex) {
            // jakarta.inject API 不可用 - 直接跳过。
        }
        try {
            this.autowiredAnnotationTypes.add((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Inject", classLoader));
            logger.trace("'javax.inject.Inject' annotation found and supported for autowiring");
        } catch (ClassNotFoundException ex) {
            // javax.inject API 不可用 - 直接跳过。
        }
    }

    /**
     * 设置 'autowired' 注解类型，用于构造函数、字段、setter 方法以及任意的配置方法。
     * <p>默认的自动装配注解类型包括 Spring 提供的 {@link Autowired @Autowired} 和 {@link Value @Value} 注解，以及如果可用的话，常见的 {@code @Inject} 注解。
     * <p>此设置属性存在是为了让开发者能够提供他们自己的（非 Spring 特定）注解类型，以指示某个成员应该进行自动装配。
     */
    public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
        Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
        this.autowiredAnnotationTypes.clear();
        this.autowiredAnnotationTypes.add(autowiredAnnotationType);
    }

    /**
     * 设置 'autowired' 注解类型，用于构造函数、字段、setter 方法以及任意配置方法。
     * <p>默认的自动装配注解类型包括 Spring 提供的 {@link Autowired @Autowired} 和 {@link Value @Value} 注解，以及如果可用的话，常见的 {@code @Inject} 注解。
     * <p>此设置属性存在是为了让开发者能够提供他们自己的（非 Spring 特定）注解类型，以指示成员应该进行自动装配。
     */
    public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
        Assert.notEmpty(autowiredAnnotationTypes, "'autowiredAnnotationTypes' must not be empty");
        this.autowiredAnnotationTypes.clear();
        this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
    }

    /**
     * 设置注解中指定属性是否必需的名称。
     * @see #setRequiredParameterValue(boolean)
     */
    public void setRequiredParameterName(String requiredParameterName) {
        this.requiredParameterName = requiredParameterName;
    }

    /**
     * 设置一个布尔值，标记依赖项是否为必需的。
     * <p>例如，如果使用 'required=true'（默认值），则此值应为 {@code true}；但如果使用 'optional=false'，则此值应为 {@code false}。
     * @see #setRequiredParameterName(String)
     */
    public void setRequiredParameterValue(boolean requiredParameterValue) {
        this.requiredParameterValue = requiredParameterValue;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
            throw new IllegalArgumentException("AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
        }
        this.beanFactory = clbf;
        this.metadataReaderFactory = new SimpleMetadataReaderFactory(clbf.getBeanClassLoader());
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        // 在Bean定义上注册外部管理的配置成员。
        findInjectionMetadata(beanName, beanType, beanDefinition);
    }

    @Override
    public void resetBeanDefinition(String beanName) {
        this.lookupMethodsChecked.remove(beanName);
        this.injectionMetadataCache.remove(beanName);
    }

    @Override
    @Nullable
    public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
        Class<?> beanClass = registeredBean.getBeanClass();
        String beanName = registeredBean.getBeanName();
        RootBeanDefinition beanDefinition = registeredBean.getMergedBeanDefinition();
        InjectionMetadata metadata = findInjectionMetadata(beanName, beanClass, beanDefinition);
        Collection<AutowiredElement> autowiredElements = getAutowiredElements(metadata, beanDefinition.getPropertyValues());
        if (!ObjectUtils.isEmpty(autowiredElements)) {
            return new AotContribution(beanClass, autowiredElements, getAutowireCandidateResolver());
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Collection<AutowiredElement> getAutowiredElements(InjectionMetadata metadata, PropertyValues propertyValues) {
        return (Collection) metadata.getInjectedElements(propertyValues);
    }

    @Nullable
    private AutowireCandidateResolver getAutowireCandidateResolver() {
        if (this.beanFactory instanceof DefaultListableBeanFactory lbf) {
            return lbf.getAutowireCandidateResolver();
        }
        return null;
    }

    private InjectionMetadata findInjectionMetadata(String beanName, Class<?> beanType, RootBeanDefinition beanDefinition) {
        InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
        metadata.checkConfigMembers(beanDefinition);
        return metadata;
    }

    @Override
    public Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeanCreationException {
        checkLookupMethods(beanClass, beanName);
        // 拾取具有从上方覆盖的刷新查找方法的子类
        if (this.beanFactory instanceof AbstractAutowireCapableBeanFactory aacBeanFactory) {
            RootBeanDefinition mbd = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
            if (mbd.getFactoryMethodName() == null && mbd.hasBeanClass()) {
                return aacBeanFactory.getInstantiationStrategy().getActualBeanClass(mbd, beanName, aacBeanFactory);
            }
        }
        return beanClass;
    }

    @Override
    @Nullable
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, final String beanName) throws BeanCreationException {
        checkLookupMethods(beanClass, beanName);
        // 首先进行对并发映射的快速检查，使用最小化锁定。
        Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
        if (candidateConstructors == null) {
            // 现在进行完全同步的解析...
            synchronized (this.candidateConstructorsCache) {
                candidateConstructors = this.candidateConstructorsCache.get(beanClass);
                if (candidateConstructors == null) {
                    Constructor<?>[] rawCandidates;
                    try {
                        rawCandidates = beanClass.getDeclaredConstructors();
                    } catch (Throwable ex) {
                        throw new BeanCreationException(beanName, "Resolution of declared constructors on bean Class [" + beanClass.getName() + "] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
                    }
                    List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
                    Constructor<?> requiredConstructor = null;
                    Constructor<?> defaultConstructor = null;
                    Constructor<?> primaryConstructor = BeanUtils.findPrimaryConstructor(beanClass);
                    int nonSyntheticConstructors = 0;
                    for (Constructor<?> candidate : rawCandidates) {
                        if (!candidate.isSynthetic()) {
                            nonSyntheticConstructors++;
                        } else if (primaryConstructor != null) {
                            continue;
                        }
                        MergedAnnotation<?> ann = findAutowiredAnnotation(candidate);
                        if (ann == null) {
                            Class<?> userClass = ClassUtils.getUserClass(beanClass);
                            if (userClass != beanClass) {
                                try {
                                    Constructor<?> superCtor = userClass.getDeclaredConstructor(candidate.getParameterTypes());
                                    ann = findAutowiredAnnotation(superCtor);
                                } catch (NoSuchMethodException ex) {
                                    // 直接继续，未找到等效的超类构造函数...
                                }
                            }
                        }
                        if (ann != null) {
                            if (requiredConstructor != null) {
                                throw new BeanCreationException(beanName, "Invalid autowire-marked constructor: " + candidate + ". Found constructor with 'required' Autowired annotation already: " + requiredConstructor);
                            }
                            boolean required = determineRequiredStatus(ann);
                            if (required) {
                                if (!candidates.isEmpty()) {
                                    throw new BeanCreationException(beanName, "Invalid autowire-marked constructors: " + candidates + ". Found constructor with 'required' Autowired annotation: " + candidate);
                                }
                                requiredConstructor = candidate;
                            }
                            candidates.add(candidate);
                        } else if (candidate.getParameterCount() == 0) {
                            defaultConstructor = candidate;
                        }
                    }
                    if (!candidates.isEmpty()) {
                        // 将默认构造函数添加到可选构造函数列表中，作为后备选项。
                        if (requiredConstructor == null) {
                            if (defaultConstructor != null) {
                                candidates.add(defaultConstructor);
                            } else if (candidates.size() == 1 && logger.isInfoEnabled()) {
                                logger.info("Inconsistent constructor declaration on bean with name '" + beanName + "': single autowire-marked constructor flagged as optional - " + "this constructor is effectively required since there is no " + "default constructor to fall back to: " + candidates.get(0));
                            }
                        }
                        candidateConstructors = candidates.toArray(EMPTY_CONSTRUCTOR_ARRAY);
                    } else if (rawCandidates.length == 1 && rawCandidates[0].getParameterCount() > 0) {
                        candidateConstructors = new Constructor<?>[] { rawCandidates[0] };
                    } else if (nonSyntheticConstructors == 2 && primaryConstructor != null && defaultConstructor != null && !primaryConstructor.equals(defaultConstructor)) {
                        candidateConstructors = new Constructor<?>[] { primaryConstructor, defaultConstructor };
                    } else if (nonSyntheticConstructors == 1 && primaryConstructor != null) {
                        candidateConstructors = new Constructor<?>[] { primaryConstructor };
                    } else {
                        candidateConstructors = EMPTY_CONSTRUCTOR_ARRAY;
                    }
                    this.candidateConstructorsCache.put(beanClass, candidateConstructors);
                }
            }
        }
        return (candidateConstructors.length > 0 ? candidateConstructors : null);
    }

    private void checkLookupMethods(Class<?> beanClass, final String beanName) throws BeanCreationException {
        if (!this.lookupMethodsChecked.contains(beanName)) {
            if (AnnotationUtils.isCandidateClass(beanClass, Lookup.class)) {
                try {
                    Class<?> targetClass = beanClass;
                    do {
                        ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                            Lookup lookup = method.getAnnotation(Lookup.class);
                            if (lookup != null) {
                                Assert.state(this.beanFactory != null, "No BeanFactory available");
                                LookupOverride override = new LookupOverride(method, lookup.value());
                                try {
                                    RootBeanDefinition mbd = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
                                    mbd.getMethodOverrides().addOverride(override);
                                } catch (NoSuchBeanDefinitionException ex) {
                                    throw new BeanCreationException(beanName, "Cannot apply @Lookup to beans without corresponding bean definition");
                                }
                            }
                        });
                        targetClass = targetClass.getSuperclass();
                    } while (targetClass != null && targetClass != Object.class);
                } catch (IllegalStateException ex) {
                    throw new BeanCreationException(beanName, "Lookup method resolution failed", ex);
                }
            }
            this.lookupMethodsChecked.add(beanName);
        }
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
        }
        return pvs;
    }

    /**
     * <em>原生</em>处理方法，用于直接调用任意目标实例，解析其所有被配置的'自动装配'注解类型之一注解的字段和方法。
     * @param bean 要处理的目标实例
     * @throws BeanCreationException 如果自动装配失败
     * @see #setAutowiredAnnotationTypes(Set)
     */
    public void processInjection(Object bean) throws BeanCreationException {
        Class<?> clazz = bean.getClass();
        InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz, null);
        try {
            metadata.inject(bean, null, null);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException("Injection of autowired dependencies failed for class [" + clazz + "]", ex);
        }
    }

    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
        // 回退到使用类名作为缓存键，以支持与自定义调用者的向后兼容性。
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // 首先快速检查并发映射，使用最小化锁定。
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = buildAutowiringMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
        if (!AnnotationUtils.isCandidateClass(clazz, this.autowiredAnnotationTypes)) {
            return InjectionMetadata.EMPTY;
        }
        final List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;
        do {
            final List<InjectionMetadata.InjectedElement> fieldElements = new ArrayList<>();
            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                MergedAnnotation<?> ann = findAutowiredAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Autowired annotation is not supported on static fields: " + field);
                        }
                        return;
                    }
                    boolean required = determineRequiredStatus(ann);
                    fieldElements.add(new AutowiredFieldElement(field, required));
                }
            });
            final List<InjectionMetadata.InjectedElement> methodElements = new ArrayList<>();
            ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                MergedAnnotation<?> ann = findAutowiredAnnotation(bridgedMethod);
                if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Autowired annotation is not supported on static methods: " + method);
                        }
                        return;
                    }
                    if (method.getParameterCount() == 0) {
                        if (method.getDeclaringClass().isRecord()) {
                            // 在访问器上提供的紧凑构造函数参数上的注释将被忽略。
                            return;
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("Autowired annotation should only be used on methods with parameters: " + method);
                        }
                    }
                    boolean required = determineRequiredStatus(ann);
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                    methodElements.add(new AutowiredMethodElement(method, required, pd));
                }
            });
            elements.addAll(0, sortMethodElements(methodElements, targetClass));
            elements.addAll(0, fieldElements);
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
        return InjectionMetadata.forElements(elements, clazz);
    }

    @Nullable
    private MergedAnnotation<?> findAutowiredAnnotation(AccessibleObject ao) {
        MergedAnnotations annotations = MergedAnnotations.from(ao);
        for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
            MergedAnnotation<?> annotation = annotations.get(type);
            if (annotation.isPresent()) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * 判断被注解的字段或方法是否需要其依赖。
     * <p>一个 'required' 依赖表示当没有找到任何 Bean 时，自动装配应该失败。否则，当没有找到 Bean 时，自动装配过程将简单地绕过该字段或方法。
     * @param ann Autowired 注解
     * @return 注解是否指示依赖项是必需的
     */
    protected boolean determineRequiredStatus(MergedAnnotation<?> ann) {
        return (ann.getValue(this.requiredParameterName).isEmpty() || this.requiredParameterValue == ann.getBoolean(this.requiredParameterName));
    }

    /**
     * 尝试使用 ASM 对方法元素进行排序，以确保具有确定性的声明顺序。
     */
    private List<InjectionMetadata.InjectedElement> sortMethodElements(List<InjectionMetadata.InjectedElement> methodElements, Class<?> targetClass) {
        if (this.metadataReaderFactory != null && methodElements.size() > 1) {
            // 尝试通过ASM读取类文件以确定声明顺序...
            // 不幸的是，JVM 的标准反射以任意顺序返回方法
            // 订单，即使在同一JVM上同一应用程序的不同运行之间。
            try {
                AnnotationMetadata asm = this.metadataReaderFactory.getMetadataReader(targetClass.getName()).getAnnotationMetadata();
                Set<MethodMetadata> asmMethods = asm.getAnnotatedMethods(Autowired.class.getName());
                if (asmMethods.size() >= methodElements.size()) {
                    List<InjectionMetadata.InjectedElement> candidateMethods = new ArrayList<>(methodElements);
                    List<InjectionMetadata.InjectedElement> selectedMethods = new ArrayList<>(asmMethods.size());
                    for (MethodMetadata asmMethod : asmMethods) {
                        for (Iterator<InjectionMetadata.InjectedElement> it = candidateMethods.iterator(); it.hasNext(); ) {
                            InjectionMetadata.InjectedElement element = it.next();
                            if (element.getMember().getName().equals(asmMethod.getMethodName())) {
                                selectedMethods.add(element);
                                it.remove();
                                break;
                            }
                        }
                    }
                    if (selectedMethods.size() == methodElements.size()) {
                        // 在ASM方法集中找到的所有通过反射检测到的方法 -> 继续执行
                        return selectedMethods;
                    }
                }
            } catch (IOException ex) {
                logger.debug("Failed to read class file via ASM for determining @Autowired method order", ex);
                // 别担心，让我们继续我们开始的反射元数据...
            }
        }
        return methodElements;
    }

    /**
     * 将指定的Bean注册为依赖于自动装配的Bean。
     */
    private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames) {
        if (beanName != null) {
            for (String autowiredBeanName : autowiredBeanNames) {
                if (this.beanFactory != null && this.beanFactory.containsBean(autowiredBeanName)) {
                    this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Autowiring by type from bean name '" + beanName + "' to bean named '" + autowiredBeanName + "'");
                }
            }
        }
    }

    /**
     * 解析指定的缓存方法参数或字段值。
     */
    @Nullable
    private Object resolveCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
        if (cachedArgument instanceof DependencyDescriptor descriptor) {
            Assert.state(this.beanFactory != null, "No BeanFactory available");
            return this.beanFactory.resolveDependency(descriptor, beanName, null, null);
        } else {
            return cachedArgument;
        }
    }

    /**
     * 表示注入信息的基类。
     */
    private abstract static class AutowiredElement extends InjectionMetadata.InjectedElement {

        protected final boolean required;

        protected AutowiredElement(Member member, @Nullable PropertyDescriptor pd, boolean required) {
            super(member, pd);
            this.required = required;
        }
    }

    /**
     * 表示注解字段注入信息的类。
     */
    private class AutowiredFieldElement extends AutowiredElement {

        private volatile boolean cached;

        @Nullable
        private volatile Object cachedFieldValue;

        public AutowiredFieldElement(Field field, boolean required) {
            super(field, null, required);
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            Field field = (Field) this.member;
            Object value;
            if (this.cached) {
                try {
                    value = resolveCachedArgument(beanName, this.cachedFieldValue);
                } catch (BeansException ex) {
                    // 缓存参数的预期目标Bean不匹配 -> 重新解析
                    this.cached = false;
                    logger.debug("Failed to resolve cached argument", ex);
                    value = resolveFieldValue(field, bean, beanName);
                }
            } else {
                value = resolveFieldValue(field, bean, beanName);
            }
            if (value != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(bean, value);
            }
        }

        @Nullable
        private Object resolveFieldValue(Field field, Object bean, @Nullable String beanName) {
            DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
            desc.setContainingClass(bean.getClass());
            Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
            Assert.state(beanFactory != null, "No BeanFactory available");
            TypeConverter typeConverter = beanFactory.getTypeConverter();
            Object value;
            try {
                value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
            } catch (BeansException ex) {
                throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
            }
            synchronized (this) {
                if (!this.cached) {
                    if (value != null || this.required) {
                        Object cachedFieldValue = desc;
                        registerDependentBeans(beanName, autowiredBeanNames);
                        if (value != null && autowiredBeanNames.size() == 1) {
                            String autowiredBeanName = autowiredBeanNames.iterator().next();
                            if (beanFactory.containsBean(autowiredBeanName) && beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
                                cachedFieldValue = new ShortcutDependencyDescriptor(desc, autowiredBeanName);
                            }
                        }
                        this.cachedFieldValue = cachedFieldValue;
                        this.cached = true;
                    } else {
                        this.cachedFieldValue = null;
                        // 缓存的标志仍然为false
                    }
                }
            }
            return value;
        }
    }

    /**
     * 代表注解方法的注入信息的类。
     */
    private class AutowiredMethodElement extends AutowiredElement {

        private volatile boolean cached;

        @Nullable
        private volatile Object[] cachedMethodArguments;

        public AutowiredMethodElement(Method method, boolean required, @Nullable PropertyDescriptor pd) {
            super(method, pd, required);
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            if (!shouldInject(pvs)) {
                return;
            }
            Method method = (Method) this.member;
            Object[] arguments;
            if (this.cached) {
                try {
                    arguments = resolveCachedArguments(beanName, this.cachedMethodArguments);
                } catch (BeansException ex) {
                    // 意外的目标 Bean 匹配不匹配缓存参数 -> 重新解析
                    this.cached = false;
                    logger.debug("Failed to resolve cached argument", ex);
                    arguments = resolveMethodArguments(method, bean, beanName);
                }
            } else {
                arguments = resolveMethodArguments(method, bean, beanName);
            }
            if (arguments != null) {
                try {
                    ReflectionUtils.makeAccessible(method);
                    method.invoke(bean, arguments);
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        }

        @Nullable
        private Object[] resolveCachedArguments(@Nullable String beanName, @Nullable Object[] cachedMethodArguments) {
            if (cachedMethodArguments == null) {
                return null;
            }
            Object[] arguments = new Object[cachedMethodArguments.length];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = resolveCachedArgument(beanName, cachedMethodArguments[i]);
            }
            return arguments;
        }

        @Nullable
        private Object[] resolveMethodArguments(Method method, Object bean, @Nullable String beanName) {
            int argumentCount = method.getParameterCount();
            Object[] arguments = new Object[argumentCount];
            DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
            Set<String> autowiredBeanNames = new LinkedHashSet<>(argumentCount * 2);
            Assert.state(beanFactory != null, "No BeanFactory available");
            TypeConverter typeConverter = beanFactory.getTypeConverter();
            for (int i = 0; i < arguments.length; i++) {
                MethodParameter methodParam = new MethodParameter(method, i);
                DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, this.required);
                currDesc.setContainingClass(bean.getClass());
                descriptors[i] = currDesc;
                try {
                    Object arg = beanFactory.resolveDependency(currDesc, beanName, autowiredBeanNames, typeConverter);
                    if (arg == null && !this.required) {
                        arguments = null;
                        break;
                    }
                    arguments[i] = arg;
                } catch (BeansException ex) {
                    throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex);
                }
            }
            synchronized (this) {
                if (!this.cached) {
                    if (arguments != null) {
                        DependencyDescriptor[] cachedMethodArguments = Arrays.copyOf(descriptors, argumentCount);
                        registerDependentBeans(beanName, autowiredBeanNames);
                        if (autowiredBeanNames.size() == argumentCount) {
                            Iterator<String> it = autowiredBeanNames.iterator();
                            Class<?>[] paramTypes = method.getParameterTypes();
                            for (int i = 0; i < paramTypes.length; i++) {
                                String autowiredBeanName = it.next();
                                if (arguments[i] != null && beanFactory.containsBean(autowiredBeanName) && beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
                                    cachedMethodArguments[i] = new ShortcutDependencyDescriptor(descriptors[i], autowiredBeanName);
                                }
                            }
                        }
                        this.cachedMethodArguments = cachedMethodArguments;
                        this.cached = true;
                    } else {
                        this.cachedMethodArguments = null;
                        // 缓存标志仍然为false
                    }
                }
            }
            return arguments;
        }
    }

    /**
     * 包含预解析的目标Bean名称的DependencyDescriptor变体。
     */
    @SuppressWarnings("serial")
    private static class ShortcutDependencyDescriptor extends DependencyDescriptor {

        private final String shortcut;

        public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut) {
            super(original);
            this.shortcut = shortcut;
        }

        @Override
        public Object resolveShortcut(BeanFactory beanFactory) {
            return beanFactory.getBean(this.shortcut, getDependencyType());
        }
    }

    /**
     * 用于自动装配字段和方法的链接：`@link BeanRegistrationAotContribution`。
     */
    private static class AotContribution implements BeanRegistrationAotContribution {

        private static final String REGISTERED_BEAN_PARAMETER = "registeredBean";

        private static final String INSTANCE_PARAMETER = "instance";

        private final Class<?> target;

        private final Collection<AutowiredElement> autowiredElements;

        @Nullable
        private final AutowireCandidateResolver candidateResolver;

        AotContribution(Class<?> target, Collection<AutowiredElement> autowiredElements, @Nullable AutowireCandidateResolver candidateResolver) {
            this.target = target;
            this.autowiredElements = autowiredElements;
            this.candidateResolver = candidateResolver;
        }

        @Override
        public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
            GeneratedClass generatedClass = generationContext.getGeneratedClasses().addForFeatureComponent("Autowiring", this.target, type -> {
                type.addJavadoc("Autowiring for {@link $T}.", this.target);
                type.addModifiers(javax.lang.model.element.Modifier.PUBLIC);
            });
            GeneratedMethod generateMethod = generatedClass.getMethods().add("apply", method -> {
                method.addJavadoc("Apply the autowiring.");
                method.addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC);
                method.addParameter(RegisteredBean.class, REGISTERED_BEAN_PARAMETER);
                method.addParameter(this.target, INSTANCE_PARAMETER);
                method.returns(this.target);
                method.addCode(generateMethodCode(generatedClass.getName(), generationContext.getRuntimeHints()));
            });
            beanRegistrationCode.addInstancePostProcessor(generateMethod.toMethodReference());
            if (this.candidateResolver != null) {
                registerHints(generationContext.getRuntimeHints());
            }
        }

        private CodeBlock generateMethodCode(ClassName targetClassName, RuntimeHints hints) {
            CodeBlock.Builder code = CodeBlock.builder();
            for (AutowiredElement autowiredElement : this.autowiredElements) {
                code.addStatement(generateMethodStatementForElement(targetClassName, autowiredElement, hints));
            }
            code.addStatement("return $L", INSTANCE_PARAMETER);
            return code.build();
        }

        private CodeBlock generateMethodStatementForElement(ClassName targetClassName, AutowiredElement autowiredElement, RuntimeHints hints) {
            Member member = autowiredElement.getMember();
            boolean required = autowiredElement.required;
            if (member instanceof Field field) {
                return generateMethodStatementForField(targetClassName, field, required, hints);
            }
            if (member instanceof Method method) {
                return generateMethodStatementForMethod(targetClassName, method, required, hints);
            }
            throw new IllegalStateException("Unsupported member type " + member.getClass().getName());
        }

        private CodeBlock generateMethodStatementForField(ClassName targetClassName, Field field, boolean required, RuntimeHints hints) {
            hints.reflection().registerField(field);
            CodeBlock resolver = CodeBlock.of("$T.$L($S)", AutowiredFieldValueResolver.class, (!required ? "forField" : "forRequiredField"), field.getName());
            AccessControl accessControl = AccessControl.forMember(field);
            if (!accessControl.isAccessibleFrom(targetClassName)) {
                return CodeBlock.of("$L.resolveAndSet($L, $L)", resolver, REGISTERED_BEAN_PARAMETER, INSTANCE_PARAMETER);
            }
            return CodeBlock.of("$L.$L = $L.resolve($L)", INSTANCE_PARAMETER, field.getName(), resolver, REGISTERED_BEAN_PARAMETER);
        }

        private CodeBlock generateMethodStatementForMethod(ClassName targetClassName, Method method, boolean required, RuntimeHints hints) {
            CodeBlock.Builder code = CodeBlock.builder();
            code.add("$T.$L", AutowiredMethodArgumentsResolver.class, (!required ? "forMethod" : "forRequiredMethod"));
            code.add("($S", method.getName());
            if (method.getParameterCount() > 0) {
                code.add(", $L", generateParameterTypesCode(method.getParameterTypes()));
            }
            code.add(")");
            AccessControl accessControl = AccessControl.forMember(method);
            if (!accessControl.isAccessibleFrom(targetClassName)) {
                hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
                code.add(".resolveAndInvoke($L, $L)", REGISTERED_BEAN_PARAMETER, INSTANCE_PARAMETER);
            } else {
                hints.reflection().registerMethod(method, ExecutableMode.INTROSPECT);
                CodeBlock arguments = new AutowiredArgumentsCodeGenerator(this.target, method).generateCode(method.getParameterTypes());
                CodeBlock injectionCode = CodeBlock.of("args -> $L.$L($L)", INSTANCE_PARAMETER, method.getName(), arguments);
                code.add(".resolve($L, $L)", REGISTERED_BEAN_PARAMETER, injectionCode);
            }
            return code.build();
        }

        private CodeBlock generateParameterTypesCode(Class<?>[] parameterTypes) {
            CodeBlock.Builder code = CodeBlock.builder();
            for (int i = 0; i < parameterTypes.length; i++) {
                code.add(i != 0 ? ", " : "");
                code.add("$T.class", parameterTypes[i]);
            }
            return code.build();
        }

        private void registerHints(RuntimeHints runtimeHints) {
            this.autowiredElements.forEach(autowiredElement -> {
                boolean required = autowiredElement.required;
                Member member = autowiredElement.getMember();
                if (member instanceof Field field) {
                    DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, required);
                    registerProxyIfNecessary(runtimeHints, dependencyDescriptor);
                }
                if (member instanceof Method method) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        MethodParameter methodParam = new MethodParameter(method, i);
                        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(methodParam, required);
                        registerProxyIfNecessary(runtimeHints, dependencyDescriptor);
                    }
                }
            });
        }

        private void registerProxyIfNecessary(RuntimeHints runtimeHints, DependencyDescriptor dependencyDescriptor) {
            if (this.candidateResolver != null) {
                Class<?> proxyClass = this.candidateResolver.getLazyResolutionProxyClass(dependencyDescriptor, null);
                if (proxyClass != null) {
                    ClassHintUtils.registerProxyIfNecessary(proxyClass, runtimeHints);
                }
            }
        }
    }
}
