// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权和限制的条款。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 具体完全功能的 {@link BeanDefinition} 类的基类，
 * 将 {@link GenericBeanDefinition}、
 * {@link RootBeanDefinition} 和 {@link ChildBeanDefinition}
 * 的共有属性抽象出来。
 *
 * <p>自动装配常量与定义在 {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * 接口中的常量相匹配。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @see GenericBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor implements BeanDefinition, Cloneable {

    /**
     * 默认作用域名称的常量：`""`，除非由父bean定义（如果适用）覆盖，否则等同于单例状态。
     */
    public static final String SCOPE_DEFAULT = "";

    /**
     * 表示完全不进行外部自动装配的常量。
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

    /**
     * 表示通过名称自动装配bean属性的常量。
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    /**
     * 常量，表示按类型自动装配 bean 属性。
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

    /**
     * 表示自动装配构造器的常量。
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

    /**
     * 表示通过反射来决定适当的自动装配策略的常量。
     * @see #setAutowireMode
     * 自 Spring 3.0 开始已弃用：如果您正在使用混合自动装配策略，请使用基于注解的自动装配，以便更清晰地标记自动装配的需求。
     */
    @Deprecated
    public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

    /**
     * 表示完全不进行依赖检查的常量。
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_NONE = 0;

    /**
     * 表示对象引用依赖检查的常量。
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_OBJECTS = 1;

    /**
     * 表示对“简单”属性进行依赖检查的常量。
     * @see #setDependencyCheck
     * @see org.springframework.beans.BeanUtils#isSimpleProperty
     */
    public static final int DEPENDENCY_CHECK_SIMPLE = 2;

    /**
     * 常量，表示对所有属性进行依赖检查
     * （包括对象引用以及“简单”属性）。
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_ALL = 3;

    /**
     * 常量，表示容器应尝试推断一个bean的`#setDestroyMethodName(destroy方法名)`，而不是显式指定方法名。该值`@value`特别设计用于包含在方法名中非法的字符，确保不会与具有相同名称的合法命名方法发生冲突。
     * <p>目前，在销毁方法推断过程中检测到的方法名是"close"和"shutdown"，如果这些方法名存在于特定的bean类中。
     */
    public static final String INFER_METHOD = "(inferred)";

    @Nullable
    private volatile Object beanClass;

    @Nullable
    private String scope = SCOPE_DEFAULT;

    private boolean abstractFlag = false;

    @Nullable
    private Boolean lazyInit;

    private int autowireMode = AUTOWIRE_NO;

    private int dependencyCheck = DEPENDENCY_CHECK_NONE;

    @Nullable
    private String[] dependsOn;

    private boolean autowireCandidate = true;

    private boolean primary = false;

    private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();

    @Nullable
    private Supplier<?> instanceSupplier;

    private boolean nonPublicAccessAllowed = true;

    private boolean lenientConstructorResolution = true;

    @Nullable
    private String factoryBeanName;

    @Nullable
    private String factoryMethodName;

    @Nullable
    private ConstructorArgumentValues constructorArgumentValues;

    @Nullable
    private MutablePropertyValues propertyValues;

    private MethodOverrides methodOverrides = new MethodOverrides();

    @Nullable
    private String[] initMethodNames;

    @Nullable
    private String[] destroyMethodNames;

    private boolean enforceInitMethod = true;

    private boolean enforceDestroyMethod = true;

    private boolean synthetic = false;

    private int role = BeanDefinition.ROLE_APPLICATION;

    @Nullable
    private String description;

    @Nullable
    private Resource resource;

    /**
     * 创建一个新的 AbstractBeanDefinition 实例，使用默认设置。
     */
    protected AbstractBeanDefinition() {
        this(null, null);
    }

    /**
     * 使用给定的构造函数参数值和属性值创建一个新的 AbstractBeanDefinition。
     */
    protected AbstractBeanDefinition(@Nullable ConstructorArgumentValues cargs, @Nullable MutablePropertyValues pvs) {
        this.constructorArgumentValues = cargs;
        this.propertyValues = pvs;
    }

    /**
     * 创建一个新的 AbstractBeanDefinition，作为给定 bean 定义的一个深拷贝。
     * @param original 要从中复制的原始 bean 定义
     */
    protected AbstractBeanDefinition(BeanDefinition original) {
        setParentName(original.getParentName());
        setBeanClassName(original.getBeanClassName());
        setScope(original.getScope());
        setAbstract(original.isAbstract());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());
        setRole(original.getRole());
        setSource(original.getSource());
        copyAttributesFrom(original);
        if (original instanceof AbstractBeanDefinition originalAbd) {
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
            if (originalAbd.hasConstructorArgumentValues()) {
                setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
            }
            if (originalAbd.hasPropertyValues()) {
                setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
            }
            if (originalAbd.hasMethodOverrides()) {
                setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
            }
            Boolean lazyInit = originalAbd.getLazyInit();
            if (lazyInit != null) {
                setLazyInit(lazyInit);
            }
            setAutowireMode(originalAbd.getAutowireMode());
            setDependencyCheck(originalAbd.getDependencyCheck());
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());
            setPrimary(originalAbd.isPrimary());
            copyQualifiersFrom(originalAbd);
            setInstanceSupplier(originalAbd.getInstanceSupplier());
            setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
            setInitMethodNames(originalAbd.getInitMethodNames());
            setEnforceInitMethod(originalAbd.isEnforceInitMethod());
            setDestroyMethodNames(originalAbd.getDestroyMethodNames());
            setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
            setSynthetic(originalAbd.isSynthetic());
            setResource(originalAbd.getResource());
        } else {
            setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
            setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
            setLazyInit(original.isLazyInit());
            setResourceDescription(original.getResourceDescription());
        }
    }

    /**
     *  在这个Bean定义中覆盖设置（可能是从父-child继承关系中的复制父Bean定义），来自给定的Bean定义（可能是子Bean定义）。
     * 	<ul>
     * 		<li>如果给定Bean定义中指定了，将覆盖beanClass。
     * 		<li>将始终从给定Bean定义中获取 {@code abstract}、{@code scope}、{@code lazyInit}、{@code autowireMode}、{@code dependencyCheck} 和 {@code dependsOn}。
     * 		<li>将给定的Bean定义中的 {@code constructorArgumentValues}、{@code propertyValues} 和 {@code methodOverrides} 添加到现有设置中。
     * 		<li>如果给定Bean定义中指定了，将覆盖 {@code factoryBeanName}、{@code factoryMethodName}、{@code initMethodName} 和 {@code destroyMethodName}。
     * 	</ul>
     */
    public void overrideFrom(BeanDefinition other) {
        if (StringUtils.hasLength(other.getBeanClassName())) {
            setBeanClassName(other.getBeanClassName());
        }
        if (StringUtils.hasLength(other.getScope())) {
            setScope(other.getScope());
        }
        setAbstract(other.isAbstract());
        if (StringUtils.hasLength(other.getFactoryBeanName())) {
            setFactoryBeanName(other.getFactoryBeanName());
        }
        if (StringUtils.hasLength(other.getFactoryMethodName())) {
            setFactoryMethodName(other.getFactoryMethodName());
        }
        setRole(other.getRole());
        setSource(other.getSource());
        copyAttributesFrom(other);
        if (other instanceof AbstractBeanDefinition otherAbd) {
            if (otherAbd.hasBeanClass()) {
                setBeanClass(otherAbd.getBeanClass());
            }
            if (otherAbd.hasConstructorArgumentValues()) {
                getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
            }
            if (otherAbd.hasPropertyValues()) {
                getPropertyValues().addPropertyValues(other.getPropertyValues());
            }
            if (otherAbd.hasMethodOverrides()) {
                getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
            }
            Boolean lazyInit = otherAbd.getLazyInit();
            if (lazyInit != null) {
                setLazyInit(lazyInit);
            }
            setAutowireMode(otherAbd.getAutowireMode());
            setDependencyCheck(otherAbd.getDependencyCheck());
            setDependsOn(otherAbd.getDependsOn());
            setAutowireCandidate(otherAbd.isAutowireCandidate());
            setPrimary(otherAbd.isPrimary());
            copyQualifiersFrom(otherAbd);
            setInstanceSupplier(otherAbd.getInstanceSupplier());
            setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
            if (otherAbd.getInitMethodNames() != null) {
                setInitMethodNames(otherAbd.getInitMethodNames());
                setEnforceInitMethod(otherAbd.isEnforceInitMethod());
            }
            if (otherAbd.getDestroyMethodNames() != null) {
                setDestroyMethodNames(otherAbd.getDestroyMethodNames());
                setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
            }
            setSynthetic(otherAbd.isSynthetic());
            setResource(otherAbd.getResource());
        } else {
            getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
            getPropertyValues().addPropertyValues(other.getPropertyValues());
            setLazyInit(other.isLazyInit());
            setResourceDescription(other.getResourceDescription());
        }
    }

    /**
     * 将提供的默认值应用到这个Bean上。
     * @param defaults 要应用的默认设置
     * @since 2.5
     */
    public void applyDefaults(BeanDefinitionDefaults defaults) {
        Boolean lazyInit = defaults.getLazyInit();
        if (lazyInit != null) {
            setLazyInit(lazyInit);
        }
        setAutowireMode(defaults.getAutowireMode());
        setDependencyCheck(defaults.getDependencyCheck());
        setInitMethodName(defaults.getInitMethodName());
        setEnforceInitMethod(false);
        setDestroyMethodName(defaults.getDestroyMethodName());
        setEnforceDestroyMethod(false);
    }

    /**
     * 指定此 Bean 定义对应的 Bean 类名称。
     */
    @Override
    public void setBeanClassName(@Nullable String beanClassName) {
        this.beanClass = beanClassName;
    }

    /**
     * 返回此Bean定义的当前Bean类名称。
     */
    @Override
    @Nullable
    public String getBeanClassName() {
        // 防御性访问 volatile beanClass 字段
        Object beanClassObject = this.beanClass;
        return (beanClassObject instanceof Class<?> clazz ? clazz.getName() : (String) beanClassObject);
    }

    /**
     * 指定此 Bean 的类。
     * @see #setBeanClassName(String)
     */
    public void setBeanClass(@Nullable Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * 返回bean定义中指定的类（假设它已经被解析）。
     * <p><b>注意：</b>这是在bean元数据定义中声明的初始类引用，可能结合了声明的工厂方法或一个
     * {@link org.springframework.beans.factory.FactoryBean}，这可能导致bean的运行时类型不同，或者
     * 在实例级工厂方法的情况下可能没有被设置（这种情况通过
     * {@link #getFactoryBeanName()} 进行解析）。
     * <b>请不要使用此方法对任意bean定义进行运行时类型检查。</b>了解特定bean的实际运行时类型的推荐方式
     * 是调用指定bean名称的
     * {@link org.springframework.beans.factory.BeanFactory#getType}；这考虑了所有上述情况，并返回了
     * 一个对象类型，该类型是调用
     * {@link org.springframework.beans.factory.BeanFactory#getBean} 方法时将返回的同一bean名称的对象类型。
     * @return 解析后的bean类（永远不会为{@code null}）
     * @throws IllegalStateException 如果bean定义没有定义bean类，或者指定的bean类名尚未解析为实际的Class
     * @see #getBeanClassName()
     * @see #hasBeanClass()
     * @see #setBeanClass(Class)
     * @see #resolveBeanClass(ClassLoader)
     */
    public Class<?> getBeanClass() throws IllegalStateException {
        // 防御性访问 volatile beanClass 字段
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class<?> clazz)) {
            throw new IllegalStateException("Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return clazz;
    }

    /**
     * 返回此定义是否指定了Bean类。
     * @see #getBeanClass()
     * @see #setBeanClass(Class)
     * @see #resolveBeanClass(ClassLoader)
     */
    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    /**
     * 确定包装豆类的类型，如有必要，从指定的类名中解析。当使用已解析的豆类调用时，还会根据名称重新加载指定的 Class。
     * @param classLoader 用于解析（潜在）类名的 ClassLoader
     * @return 解析后的豆类
     * @throws ClassNotFoundException 如果类名无法解析
     */
    @Nullable
    public Class<?> resolveBeanClass(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    /**
     * 返回此Bean定义的可解析类型。
     * <p>此实现委托给{@link #getBeanClass()}。
     * @since 5.2
     */
    @Override
    public ResolvableType getResolvableType() {
        return (hasBeanClass() ? ResolvableType.forClass(getBeanClass()) : ResolvableType.NONE);
    }

    /**
     * 设置 Bean 的目标作用域名称。
     * <p>默认为单例状态，尽管这仅在 Bean 定义在包含的工厂中变为活动状态时应用。一个 Bean 定义最终可能从父 Bean 定义继承其作用域。因此，默认作用域名称为空字符串（即，`""`），默认为单例状态，直到设置了解决的作用域。
     * @see #SCOPE_SINGLETON
     * @see #SCOPE_PROTOTYPE
     */
    @Override
    public void setScope(@Nullable String scope) {
        this.scope = scope;
    }

    /**
     * 返回该 Bean 的目标作用域的名称。
     */
    @Override
    @Nullable
    public String getScope() {
        return this.scope;
    }

    /**
     * 返回此是否为<b>单例</b>，从所有调用中返回单个共享实例。
     * @see #SCOPE_SINGLETON
     */
    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(this.scope) || SCOPE_DEFAULT.equals(this.scope);
    }

    /**
     * 返回此对象是否为<b>原型</b>，每次调用都会返回一个独立的实例。
     * @see #SCOPE_PROTOTYPE
     */
    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

    /**
     * 设置此 Bean 是否为“抽象”的，即不打算直接实例化它，而只是作为具体子 Bean 定义的超类。
     * <p>默认值为“false”。指定为 true 以告知 Bean 工厂在任何情况下都不要尝试实例化该特定的 Bean。
     */
    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    /**
     * 返回此 Bean 是否为 "抽象" 的，即不打算直接实例化自身，而是仅仅作为具体子 Bean 定义的父亲。
     */
    @Override
    public boolean isAbstract() {
        return this.abstractFlag;
    }

    /**
     * 设置此 Bean 是否应该进行懒加载。
     * <p>如果为 {@code false}，则该 Bean 将由执行单例对象 eager 初始化的 Bean 工厂在启动时进行实例化。
     */
    @Override
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    /**
     * 返回此 Bean 是否应该进行懒加载初始化，即不在启动时立即实例化。仅适用于单例 Bean。
     * @return 是否应用懒加载语义（默认为 {@code false}）
     */
    @Override
    public boolean isLazyInit() {
        return (this.lazyInit != null && this.lazyInit.booleanValue());
    }

    /**
     * 返回此Bean是否应进行懒加载初始化，即不在启动时立即实例化。仅适用于单例Bean。
     * @return 如果已显式设置，则返回懒加载标志，否则返回{@code null}
     * @since 5.2
     */
    @Nullable
    public Boolean getLazyInit() {
        return this.lazyInit;
    }

    /**
     * 设置自动装配模式。这决定了是否会发生任何自动检测和设置bean引用。默认为AUTOWIRE_NO，表示不会通过命名或类型进行基于约定的自动装配（然而，仍然可能存在显式的基于注解的自动装配）。
     * @param autowireMode 要设置的自动装配模式。
     * 必须是本类中定义的常量之一。
     * @see #AUTOWIRE_NO
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_AUTODETECT
     */
    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    /**
     * 返回在bean定义中指定的自动装配模式。
     */
    public int getAutowireMode() {
        return this.autowireMode;
    }

    /**
     * 返回解析后的自动装配代码，
     * （将AUTOWIRE_AUTODETECT解析为AUTOWIRE_CONSTRUCTOR或AUTOWIRE_BY_TYPE）。
     * @see #AUTOWIRE_AUTODETECT
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_BY_TYPE
     */
    public int getResolvedAutowireMode() {
        if (this.autowireMode == AUTOWIRE_AUTODETECT) {
            // 确定是否应用setter自动装配或构造函数自动装配。
            // 如果它有一个无参构造函数，则被视为setter自动装配。
            // 否则，我们将尝试构造器自动装配。
            Constructor<?>[] constructors = getBeanClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    return AUTOWIRE_BY_TYPE;
                }
            }
            return AUTOWIRE_CONSTRUCTOR;
        } else {
            return this.autowireMode;
        }
    }

    /**
     * 设置依赖检查代码。
     * @param dependencyCheck 要设置的代码。
     * 必须是本类中定义的四个常量之一。
     * @see #DEPENDENCY_CHECK_NONE
     * @see #DEPENDENCY_CHECK_OBJECTS
     * @see #DEPENDENCY_CHECK_SIMPLE
     * @see #DEPENDENCY_CHECK_ALL
     */
    public void setDependencyCheck(int dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }

    /**
     * 返回依赖检查代码。
     */
    public int getDependencyCheck() {
        return this.dependencyCheck;
    }

    /**
     * 设置本Bean所依赖的Bean的名称，确保这些Bean在初始化本Bean之前已初始化。
     * Bean工厂将保证这些Bean先被初始化。
     * <p>请注意，依赖关系通常通过Bean属性或构造函数参数来表示。这个属性仅在其他类型的依赖关系上必要，例如静态依赖（*真讨厌*）或启动时的数据库准备。
     */
    @Override
    public void setDependsOn(@Nullable String... dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * 返回此Bean所依赖的Bean名称。
     */
    @Override
    @Nullable
    public String[] getDependsOn() {
        return this.dependsOn;
    }

    /**
     * 设置此 Bean 是否是自动装配到其他 Bean 的候选者。
     * <p>注意，此标志仅影响基于类型的自动装配。
     * 它不影响通过名称显式引用，即使指定的 Bean 未标记为自动装配候选者，这些引用也会被解析。因此，即使名称匹配，通过名称的自动装配仍然会注入一个 Bean。
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_BY_NAME
     */
    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.autowireCandidate = autowireCandidate;
    }

    /**
     * 返回此Bean是否是注入到其他Bean中的候选者。
     */
    @Override
    public boolean isAutowireCandidate() {
        return this.autowireCandidate;
    }

    /**
     * 设置此 Bean 是否是主要的自动装配候选者。
     * <p>如果多个匹配的候选者中恰好有一个 Bean 的此值设置为 {@code true}，则它将作为裁决者。
     */
    @Override
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * 返回此Bean是否是主要自动装配的候选者。
     */
    @Override
    public boolean isPrimary() {
        return this.primary;
    }

    /**
     * 注册一个用于自动装配候选解析的限定符，键由限定符的类型名称构成。
     * @see AutowireCandidateQualifier#getTypeName()
     */
    public void addQualifier(AutowireCandidateQualifier qualifier) {
        this.qualifiers.put(qualifier.getTypeName(), qualifier);
    }

    /**
     * 返回此 Bean 是否具有指定的限定符。
     */
    public boolean hasQualifier(String typeName) {
        return this.qualifiers.containsKey(typeName);
    }

    /**
     * 返回与提供的类型名称映射的限定符。
     */
    @Nullable
    public AutowireCandidateQualifier getQualifier(String typeName) {
        return this.qualifiers.get(typeName);
    }

    /**
     * 返回所有已注册的限定符。
     * @return 包含 {@link AutowireCandidateQualifier} 对象的 Set。
     */
    public Set<AutowireCandidateQualifier> getQualifiers() {
        return new LinkedHashSet<>(this.qualifiers.values());
    }

    /**
     * 从提供的 AbstractBeanDefinition 复制限定符到当前这个 Bean 定义。
     * @param source 要从中复制的 AbstractBeanDefinition
     */
    public void copyQualifiersFrom(AbstractBeanDefinition source) {
        Assert.notNull(source, "Source must not be null");
        this.qualifiers.putAll(source.qualifiers);
    }

    /**
     * 指定一个用于创建 bean 实例的回调，作为声明性指定的工厂方法的替代方案。
     * <p>如果设置了此类回调，它将覆盖任何其他构造函数或工厂方法元数据。然而，bean 属性填充以及潜在的注解驱动注入仍将按常规应用。
     * @since 5.0
     * @see #setConstructorArgumentValues(ConstructorArgumentValues)
     * @see #setPropertyValues(MutablePropertyValues)
     */
    public void setInstanceSupplier(@Nullable Supplier<?> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    /**
     * 返回用于创建 bean 实例的回调，如果有的话。
     * @since 5.0
     */
    @Nullable
    public Supplier<?> getInstanceSupplier() {
        return this.instanceSupplier;
    }

    /**
     * 指定是否允许访问非公共构造函数和方法，对于指向这些函数的外部化元数据的情况。默认值为
     * {@code true}；将此切换为 {@code false} 仅允许公共访问。
     * <p>这适用于构造函数解析、工厂方法解析，以及初始化/销毁方法。在任何情况下，Bean属性访问器都必须是公共的，并且不受此设置的影响。
     * <p>请注意，注解驱动的配置仍然会访问已注解的非公共成员。此设置仅适用于此Bean定义中的外部化元数据。
     */
    public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
    }

    /**
     * 返回是否允许访问非公共构造函数和方法。
     */
    public boolean isNonPublicAccessAllowed() {
        return this.nonPublicAccessAllowed;
    }

    /**
     * 指定是否以宽松模式（`true`，默认值）解析构造函数，还是切换到严格解析模式（在转换参数时遇到所有匹配的模糊构造函数时抛出异常，而在宽松模式下会使用类型匹配“最接近”的那个）。
     */
    public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
        this.lenientConstructorResolution = lenientConstructorResolution;
    }

    /**
     * 返回是否以宽松模式或严格模式解析构造函数。
     */
    public boolean isLenientConstructorResolution() {
        return this.lenientConstructorResolution;
    }

    /**
     * 指定要使用的工厂bean，如果有的话。
     * 这是调用指定工厂方法的bean的名称。
     * @see #setFactoryMethodName
     */
    @Override
    public void setFactoryBeanName(@Nullable String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    /**
     * 返回工厂Bean的名称（如果有的话）。
     */
    @Override
    @Nullable
    public String getFactoryBeanName() {
        return this.factoryBeanName;
    }

    /**
     * 指定一个工厂方法（如果有的话）。此方法将使用构造函数参数调用，如果没有指定参数，则不传递任何参数。如果指定了工厂bean，此方法将在指定的工厂bean上调用，否则作为本地bean类的静态方法调用。
     * @see #setFactoryBeanName
     * @see #setBeanClassName
     */
    @Override
    public void setFactoryMethodName(@Nullable String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    /**
     * 返回一个（如果有的话）工厂方法。
     */
    @Override
    @Nullable
    public String getFactoryMethodName() {
        return this.factoryMethodName;
    }

    /**
     * 指定此 Bean 的构造函数参数值。
     */
    public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
        this.constructorArgumentValues = constructorArgumentValues;
    }

    /**
     * 返回此bean的构造函数参数值（永远不会为null）。
     */
    @Override
    public ConstructorArgumentValues getConstructorArgumentValues() {
        ConstructorArgumentValues cav = this.constructorArgumentValues;
        if (cav == null) {
            cav = new ConstructorArgumentValues();
            this.constructorArgumentValues = cav;
        }
        return cav;
    }

    /**
     * 如果此bean已定义构造函数参数值，则返回。
     */
    @Override
    public boolean hasConstructorArgumentValues() {
        return (this.constructorArgumentValues != null && !this.constructorArgumentValues.isEmpty());
    }

    /**
     * 指定此Bean的属性值，如果有任何的话。
     */
    public void setPropertyValues(MutablePropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    /**
     * 返回此 Bean 的属性值（绝不返回 {@code null}）。
     */
    @Override
    public MutablePropertyValues getPropertyValues() {
        MutablePropertyValues pvs = this.propertyValues;
        if (pvs == null) {
            pvs = new MutablePropertyValues();
            this.propertyValues = pvs;
        }
        return pvs;
    }

    /**
     * 如果为该bean定义了属性值，则返回。
     * @since 5.0.2
     */
    @Override
    public boolean hasPropertyValues() {
        return (this.propertyValues != null && !this.propertyValues.isEmpty());
    }

    /**
     * 如果有，指定 Bean 的方法重写。
     */
    public void setMethodOverrides(MethodOverrides methodOverrides) {
        this.methodOverrides = methodOverrides;
    }

    /**
     * 返回由 IoC 容器将要覆盖的方法信息。如果没有方法覆盖，则该列表将为空。
     * <p>永远不会返回 {@code null}。
     */
    public MethodOverrides getMethodOverrides() {
        return this.methodOverrides;
    }

    /**
     * 如果为此Bean定义了方法重写，则返回。
     * @since 5.0.2
     */
    public boolean hasMethodOverrides() {
        return !this.methodOverrides.isEmpty();
    }

    /**
     * 指定多个初始化方法的名字。
     * <p>默认值为 {@code null}，在这种情况下没有初始化方法。
     * @since 6.0
     * @see #setInitMethodName
     */
    public void setInitMethodNames(@Nullable String... initMethodNames) {
        this.initMethodNames = initMethodNames;
    }

    /**
     * 返回初始化方法的名称。
     * @since 6.0
     */
    @Nullable
    public String[] getInitMethodNames() {
        return this.initMethodNames;
    }

    /**
     * 设置初始化方法的名称。
     * <p>默认值为 {@code null}，在这种情况下没有初始化方法。
     * @see #setInitMethodNames
     */
    @Override
    public void setInitMethodName(@Nullable String initMethodName) {
        this.initMethodNames = (initMethodName != null ? new String[] { initMethodName } : null);
    }

    /**
     * 返回初始化方法的名字（如果有多个方法，则为第一个）。
     */
    @Override
    @Nullable
    public String getInitMethodName() {
        return (!ObjectUtils.isEmpty(this.initMethodNames) ? this.initMethodNames[0] : null);
    }

    /**
     * 指定配置的初始化方法是否为默认方法。
     * <p>对于本地指定的初始化方法，默认值为 {@code true}，但对于默认部分中的共享设置（例如XML中的
     * {@code bean init-method} 与 {@code beans default-init-method} 级别）则切换为 {@code false}，
     * 因为这可能不适用于所有包含的bean定义。
     * @see #setInitMethodName
     * @see #applyDefaults
     */
    public void setEnforceInitMethod(boolean enforceInitMethod) {
        this.enforceInitMethod = enforceInitMethod;
    }

    /**
     * 指示配置的初始化方法是否为默认方法。
     * @see #getInitMethodName()
     */
    public boolean isEnforceInitMethod() {
        return this.enforceInitMethod;
    }

    /**
     * 指定多个销毁方法的名字。
     * <p>默认值为 {@code null}，此时没有销毁方法。
     * @since 6.0
     * @see #setDestroyMethodName
     */
    public void setDestroyMethodNames(@Nullable String... destroyMethodNames) {
        this.destroyMethodNames = destroyMethodNames;
    }

    /**
     * 返回销毁方法的名字。
     * @since 6.0
     */
    @Nullable
    public String[] getDestroyMethodNames() {
        return this.destroyMethodNames;
    }

    /**
     * 设置销毁方法的名称。
     * <p>默认值为 {@code null}，在这种情况下，没有销毁方法。
     * @see #setDestroyMethodNames
     */
    @Override
    public void setDestroyMethodName(@Nullable String destroyMethodName) {
        this.destroyMethodNames = (destroyMethodName != null ? new String[] { destroyMethodName } : null);
    }

    /**
     * 返回销毁方法的名字（如果有多个方法，则返回第一个）。
     */
    @Override
    @Nullable
    public String getDestroyMethodName() {
        return (!ObjectUtils.isEmpty(this.destroyMethodNames) ? this.destroyMethodNames[0] : null);
    }

    /**
     * 指定配置的销毁方法是否为默认值。
     * <p>对于本地指定的销毁方法，默认值为 {@code true}，但对于默认配置部分中的共享设置（例如，XML中的 {@code bean destroy-method} 与 {@code beans default-destroy-method} 层级）则切换为 {@code false}，这些设置可能并不适用于所有包含的Bean定义。
     * @see #setDestroyMethodName
     * @see #applyDefaults
     */
    public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
        this.enforceDestroyMethod = enforceDestroyMethod;
    }

    /**
     * 指示配置的销毁方法是否为默认方法。
     * @see #getDestroyMethodName()
     */
    public boolean isEnforceDestroyMethod() {
        return this.enforceDestroyMethod;
    }

    /**
     * 设置此 Bean 定义是否为 '合成的'，即不是由应用程序本身定义的（例如，一个基础设施 Bean，如用于自动代理的帮助程序，通过 `<aop:config>` 创建）。
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    /**
     * 返回此Bean定义是否为“合成”的，即不是由应用程序本身定义的。
     */
    public boolean isSynthetic() {
        return this.synthetic;
    }

    /**
     * 设置此{@code BeanDefinition}的角色提示。
     */
    @Override
    public void setRole(int role) {
        this.role = role;
    }

    /**
     * 返回此{@code BeanDefinition}的角色提示。
     */
    @Override
    public int getRole() {
        return this.role;
    }

    /**
     * 设置此Bean定义的可读描述。
     */
    @Override
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    /**
     * 返回此Bean定义的人类可读描述。
     */
    @Override
    @Nullable
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置该Bean定义的来源资源
     * （用于在出错时显示上下文）。
     */
    public void setResource(@Nullable Resource resource) {
        this.resource = resource;
    }

    /**
     * 返回此Bean定义来源的资源。
     */
    @Nullable
    public Resource getResource() {
        return this.resource;
    }

    /**
     * 设置此bean定义所来自的资源描述（用于在出错时显示上下文）。
     */
    public void setResourceDescription(@Nullable String resourceDescription) {
        this.resource = (resourceDescription != null ? new DescriptiveResource(resourceDescription) : null);
    }

    /**
     * 返回此 Bean 定义所来源的资源描述（用于在出现错误时显示上下文）。
     */
    @Override
    @Nullable
    public String getResourceDescription() {
        return (this.resource != null ? this.resource.getDescription() : null);
    }

    /**
     * 设置原始（例如，装饰过的）BeanDefinition，如果有。
     */
    public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
        this.resource = new BeanDefinitionResource(originatingBd);
    }

    /**
     * 返回原始的BeanDefinition，如果没有则返回{@code null}。
     * 允许获取被装饰的BeanDefinition（如果有的话）。
     * <p>注意，此方法返回的是直接的原作者。通过遍历原作者链来找到用户定义的原始BeanDefinition。
     */
    @Override
    @Nullable
    public BeanDefinition getOriginatingBeanDefinition() {
        return (this.resource instanceof BeanDefinitionResource bdr ? bdr.getBeanDefinition() : null);
    }

    /**
     * 验证这个Bean定义。
     * @throws BeanDefinitionValidationException 在验证失败的情况下抛出异常
     */
    public void validate() throws BeanDefinitionValidationException {
        if (hasMethodOverrides() && getFactoryMethodName() != null) {
            throw new BeanDefinitionValidationException("Cannot combine factory method with container-generated method overrides: " + "the factory method must create the concrete bean instance.");
        }
        if (hasBeanClass()) {
            prepareMethodOverrides();
        }
    }

    /**
     * 验证和准备为此Bean定义的方法重写。
     * 检查是否存在具有指定名称的方法。
     * 在验证失败的情况下抛出BeanDefinitionValidationException异常
     */
    public void prepareMethodOverrides() throws BeanDefinitionValidationException {
        // 检查查找方法是否存在并确定它们的重载状态。
        if (hasMethodOverrides()) {
            getMethodOverrides().getOverrides().forEach(this::prepareMethodOverride);
        }
    }

    /**
     * 验证并准备给定的方法重写。
     * 检查是否存在具有指定名称的方法，
     * 如果未找到，则将其标记为非重载。
     * @param mo 要验证的方法重写对象
     * @throws BeanDefinitionValidationException 在验证失败的情况下
     */
    protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
        int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
        if (count == 0) {
            throw new BeanDefinitionValidationException("Invalid method override: no method with name '" + mo.getMethodName() + "' on class [" + getBeanClassName() + "]");
        } else if (count == 1) {
            // 将覆盖标记标记为未重载，以避免参数类型检查的开销。
            mo.setOverloaded(false);
        }
    }

    /**
     * 公共声明 Object 的 {@code clone()} 方法。
     * 委托给 {@link #cloneBeanDefinition()}。
     * @see Object#clone()
     */
    @Override
    public Object clone() {
        return cloneBeanDefinition();
    }

    /**
     * 克隆此Bean定义。
     * 由具体子类实现。
     * @return 克隆的Bean定义对象
     */
    public abstract AbstractBeanDefinition cloneBeanDefinition();

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof AbstractBeanDefinition that && ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName()) && ObjectUtils.nullSafeEquals(this.scope, that.scope) && this.abstractFlag == that.abstractFlag && this.lazyInit == that.lazyInit && this.autowireMode == that.autowireMode && this.dependencyCheck == that.dependencyCheck && Arrays.equals(this.dependsOn, that.dependsOn) && this.autowireCandidate == that.autowireCandidate && ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers) && this.primary == that.primary && this.nonPublicAccessAllowed == that.nonPublicAccessAllowed && this.lenientConstructorResolution == that.lenientConstructorResolution && equalsConstructorArgumentValues(that) && equalsPropertyValues(that) && ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides) && ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName) && ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName) && ObjectUtils.nullSafeEquals(this.initMethodNames, that.initMethodNames) && this.enforceInitMethod == that.enforceInitMethod && ObjectUtils.nullSafeEquals(this.destroyMethodNames, that.destroyMethodNames) && this.enforceDestroyMethod == that.enforceDestroyMethod && this.synthetic == that.synthetic && this.role == that.role && super.equals(other)));
    }

    private boolean equalsConstructorArgumentValues(AbstractBeanDefinition other) {
        if (!hasConstructorArgumentValues()) {
            return !other.hasConstructorArgumentValues();
        }
        return ObjectUtils.nullSafeEquals(this.constructorArgumentValues, other.constructorArgumentValues);
    }

    private boolean equalsPropertyValues(AbstractBeanDefinition other) {
        if (!hasPropertyValues()) {
            return !other.hasPropertyValues();
        }
        return ObjectUtils.nullSafeEquals(this.propertyValues, other.propertyValues);
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
        if (hasConstructorArgumentValues()) {
            hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
        }
        if (hasPropertyValues()) {
            hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
        }
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
        hashCode = 29 * hashCode + super.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class [");
        sb.append(getBeanClassName()).append(']');
        sb.append("; scope=").append(this.scope);
        sb.append("; abstract=").append(this.abstractFlag);
        sb.append("; lazyInit=").append(this.lazyInit);
        sb.append("; autowireMode=").append(this.autowireMode);
        sb.append("; dependencyCheck=").append(this.dependencyCheck);
        sb.append("; autowireCandidate=").append(this.autowireCandidate);
        sb.append("; primary=").append(this.primary);
        sb.append("; factoryBeanName=").append(this.factoryBeanName);
        sb.append("; factoryMethodName=").append(this.factoryMethodName);
        sb.append("; initMethodNames=").append(Arrays.toString(this.initMethodNames));
        sb.append("; destroyMethodNames=").append(Arrays.toString(this.destroyMethodNames));
        if (this.resource != null) {
            sb.append("; defined in ").append(this.resource.getDescription());
        }
        return sb.toString();
    }
}
