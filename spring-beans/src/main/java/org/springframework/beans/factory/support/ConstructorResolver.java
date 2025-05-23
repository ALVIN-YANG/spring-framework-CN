// 翻译完成 glm-4-flash
/** 版权所有 2002-2024 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不遵守许可证使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.support;

import java.beans.ConstructorProperties;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.CollectionFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 构造函数和工厂方法的代理解决器。
 *
 * <p>通过参数匹配执行构造函数的解决。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 2.0
 * @see #autowireConstructor
 * @see #instantiateUsingFactoryMethod
 * @see #resolveConstructorOrFactoryMethod
 * @see AbstractAutowireCapableBeanFactory
 */
class ConstructorResolver {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private static final NamedThreadLocal<InjectionPoint> currentInjectionPoint = new NamedThreadLocal<>("Current injection point");

    private final AbstractAutowireCapableBeanFactory beanFactory;

    private final Log logger;

    /**
     * 为给定的工厂和实例化策略创建一个新的 ConstructorResolver。
     * @param beanFactory 要与之一起工作的 BeanFactory
     */
    public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.logger = beanFactory.getLogger();
    }

    // 基于 BeanWrapper 的构建
    /**
     * “自动装配构造器”（通过类型传递构造器参数）的行为。
     * 如果显式指定了构造器参数值，也将应用此行为。
     * @param beanName Bean的名称
     * @param mbd Bean的合并后的定义
     * @param chosenCtors 被选中的候选构造器（如果没有则可能为null）
     * @param explicitArgs 通过getBean方法程序化传递的参数值，
     * 或null（->使用Bean定义中的构造器参数值）
     * @return 新实例的BeanWrapper
     */
    public BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, @Nullable Constructor<?>[] chosenCtors, @Nullable Object[] explicitArgs) {
        BeanWrapperImpl bw = new BeanWrapperImpl();
        this.beanFactory.initBeanWrapper(bw);
        Constructor<?> constructorToUse = null;
        ArgumentsHolder argsHolderToUse = null;
        Object[] argsToUse = null;
        if (explicitArgs != null) {
            argsToUse = explicitArgs;
        } else {
            Object[] argsToResolve = null;
            synchronized (mbd.constructorArgumentLock) {
                constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
                if (constructorToUse != null && mbd.constructorArgumentsResolved) {
                    // 找到缓存构造函数...
                    argsToUse = mbd.resolvedConstructorArguments;
                    if (argsToUse == null) {
                        argsToResolve = mbd.preparedConstructorArguments;
                    }
                }
            }
            if (argsToResolve != null) {
                argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve);
            }
        }
        if (constructorToUse == null || argsToUse == null) {
            // 取指定的构造函数，如果有任何的话。
            Constructor<?>[] candidates = chosenCtors;
            if (candidates == null) {
                Class<?> beanClass = mbd.getBeanClass();
                try {
                    candidates = (mbd.isNonPublicAccessAllowed() ? beanClass.getDeclaredConstructors() : beanClass.getConstructors());
                } catch (Throwable ex) {
                    throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Resolution of declared constructors on bean Class [" + beanClass.getName() + "] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
                }
            }
            if (candidates.length == 1 && explicitArgs == null && !mbd.hasConstructorArgumentValues()) {
                Constructor<?> uniqueCandidate = candidates[0];
                if (uniqueCandidate.getParameterCount() == 0) {
                    synchronized (mbd.constructorArgumentLock) {
                        mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
                        mbd.constructorArgumentsResolved = true;
                        mbd.resolvedConstructorArguments = EMPTY_ARGS;
                    }
                    bw.setBeanInstance(instantiate(beanName, mbd, uniqueCandidate, EMPTY_ARGS));
                    return bw;
                }
            }
            // 需要解决构造函数。
            boolean autowiring = (chosenCtors != null || mbd.getResolvedAutowireMode() == AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
            ConstructorArgumentValues resolvedValues = null;
            int minNrOfArgs;
            if (explicitArgs != null) {
                minNrOfArgs = explicitArgs.length;
            } else {
                ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
                resolvedValues = new ConstructorArgumentValues();
                minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
            }
            AutowireUtils.sortConstructors(candidates);
            int minTypeDiffWeight = Integer.MAX_VALUE;
            Set<Constructor<?>> ambiguousConstructors = null;
            Deque<UnsatisfiedDependencyException> causes = null;
            for (Constructor<?> candidate : candidates) {
                int parameterCount = candidate.getParameterCount();
                if (constructorToUse != null && argsToUse != null && argsToUse.length > parameterCount) {
                    // 已经找到了可以满足的贪婪构造函数 ->
                    // 无需再寻找，只剩下更少贪婪的构造函数了。
                    break;
                }
                if (parameterCount < minNrOfArgs) {
                    continue;
                }
                ArgumentsHolder argsHolder;
                Class<?>[] paramTypes = candidate.getParameterTypes();
                if (resolvedValues != null) {
                    try {
                        String[] paramNames = null;
                        if (resolvedValues.containsNamedArgument()) {
                            paramNames = ConstructorPropertiesChecker.evaluate(candidate, parameterCount);
                            if (paramNames == null) {
                                ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                                if (pnd != null) {
                                    paramNames = pnd.getParameterNames(candidate);
                                }
                            }
                        }
                        argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, paramTypes, paramNames, getUserDeclaredConstructor(candidate), autowiring, candidates.length == 1);
                    } catch (UnsatisfiedDependencyException ex) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Ignoring constructor [" + candidate + "] of bean '" + beanName + "': " + ex);
                        }
                        // 吞下并尝试下一个构造函数。
                        if (causes == null) {
                            causes = new ArrayDeque<>(1);
                        }
                        causes.add(ex);
                        continue;
                    }
                } else {
                    // 显式提供的参数 -> 参数长度必须完全匹配。
                    if (parameterCount != explicitArgs.length) {
                        continue;
                    }
                    argsHolder = new ArgumentsHolder(explicitArgs);
                }
                int typeDiffWeight = (mbd.isLenientConstructorResolution() ? argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
                // 选择此构造函数，如果它表示最接近的匹配。
                if (typeDiffWeight < minTypeDiffWeight) {
                    constructorToUse = candidate;
                    argsHolderToUse = argsHolder;
                    argsToUse = argsHolder.arguments;
                    minTypeDiffWeight = typeDiffWeight;
                    ambiguousConstructors = null;
                } else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
                    if (ambiguousConstructors == null) {
                        ambiguousConstructors = new LinkedHashSet<>();
                        ambiguousConstructors.add(constructorToUse);
                    }
                    ambiguousConstructors.add(candidate);
                }
            }
            if (constructorToUse == null) {
                if (causes != null) {
                    UnsatisfiedDependencyException ex = causes.removeLast();
                    for (Exception cause : causes) {
                        this.beanFactory.onSuppressedException(cause);
                    }
                    throw ex;
                }
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Could not resolve matching constructor on bean class [" + mbd.getBeanClassName() + "] " + "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities. " + "You should also check the consistency of arguments when mixing indexed and named arguments, " + "especially in case of bean definition inheritance)");
            } else if (ambiguousConstructors != null && !mbd.isLenientConstructorResolution()) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Ambiguous constructor matches found on bean class [" + mbd.getBeanClassName() + "] " + "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " + ambiguousConstructors);
            }
            if (explicitArgs == null && argsHolderToUse != null) {
                argsHolderToUse.storeCache(mbd, constructorToUse);
            }
        }
        Assert.state(argsToUse != null, "Unresolved constructor arguments");
        bw.setBeanInstance(instantiate(beanName, mbd, constructorToUse, argsToUse));
        return bw;
    }

    private Object instantiate(String beanName, RootBeanDefinition mbd, Constructor<?> constructorToUse, Object[] argsToUse) {
        try {
            InstantiationStrategy strategy = this.beanFactory.getInstantiationStrategy();
            return strategy.instantiate(mbd, beanName, this.beanFactory, constructorToUse, argsToUse);
        } catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
        }
    }

    /**
     * 如果可能，解析指定 Bean 定义中的工厂方法。
     * 可以通过调用 {@link RootBeanDefinition#getResolvedFactoryMethod()} 来检查结果。
     * @param mbd 要检查的 Bean 定义
     */
    public void resolveFactoryMethodIfPossible(RootBeanDefinition mbd) {
        Class<?> factoryClass;
        boolean isStatic;
        if (mbd.getFactoryBeanName() != null) {
            factoryClass = this.beanFactory.getType(mbd.getFactoryBeanName());
            isStatic = false;
        } else {
            factoryClass = mbd.getBeanClass();
            isStatic = true;
        }
        Assert.state(factoryClass != null, "Unresolvable factory class");
        factoryClass = ClassUtils.getUserClass(factoryClass);
        Method[] candidates = getCandidateMethods(factoryClass, mbd);
        Method uniqueCandidate = null;
        for (Method candidate : candidates) {
            if ((!isStatic || isStaticCandidate(candidate, factoryClass)) && mbd.isFactoryMethod(candidate)) {
                if (uniqueCandidate == null) {
                    uniqueCandidate = candidate;
                } else if (isParamMismatch(uniqueCandidate, candidate)) {
                    uniqueCandidate = null;
                    break;
                }
            }
        }
        mbd.factoryMethodToIntrospect = uniqueCandidate;
    }

    private boolean isParamMismatch(Method uniqueCandidate, Method candidate) {
        int uniqueCandidateParameterCount = uniqueCandidate.getParameterCount();
        int candidateParameterCount = candidate.getParameterCount();
        return (uniqueCandidateParameterCount != candidateParameterCount || !Arrays.equals(uniqueCandidate.getParameterTypes(), candidate.getParameterTypes()));
    }

    /**
     * 根据给定的类，考虑 {@link RootBeanDefinition#isNonPublicAccessAllowed()} 标志，检索所有候选方法。
     * 作为确定工厂方法起点的调用。
     */
    private Method[] getCandidateMethods(Class<?> factoryClass, RootBeanDefinition mbd) {
        return (mbd.isNonPublicAccessAllowed() ? ReflectionUtils.getUniqueDeclaredMethods(factoryClass) : factoryClass.getMethods());
    }

    private boolean isStaticCandidate(Method method, Class<?> factoryClass) {
        return (Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass() == factoryClass);
    }

    /**
     *  使用命名工厂方法实例化该bean。如果bean定义参数指定了一个类，而不是“工厂-bean”，或者是一个通过依赖注入配置的工厂对象上的实例变量，则该方法可能是静态的。
     * 	<p>实现需要遍历具有在RootBeanDefinition中指定的名称的静态或实例方法（该方法可能重载）并尝试与参数匹配。由于我们没有与构造函数参数关联的类型，因此这里只能采用试错法。显式参数数组可能包含通过相应的getBean方法以编程方式传递的参数值。
     * 	@param beanName bean的名称
     * 	@param mbd bean的合并后的定义
     * 	@param explicitArgs 通过getBean方法以编程方式传递的参数值，或为null（-> 使用bean定义中的构造函数参数值）
     * 	@return 新实例的BeanWrapper
     */
    public BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, @Nullable Object[] explicitArgs) {
        BeanWrapperImpl bw = new BeanWrapperImpl();
        this.beanFactory.initBeanWrapper(bw);
        Object factoryBean;
        Class<?> factoryClass;
        boolean isStatic;
        String factoryBeanName = mbd.getFactoryBeanName();
        if (factoryBeanName != null) {
            if (factoryBeanName.equals(beanName)) {
                throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName, "factory-bean reference points back to the same bean definition");
            }
            factoryBean = this.beanFactory.getBean(factoryBeanName);
            if (mbd.isSingleton() && this.beanFactory.containsSingleton(beanName)) {
                throw new ImplicitlyAppearedSingletonException();
            }
            this.beanFactory.registerDependentBean(factoryBeanName, beanName);
            factoryClass = factoryBean.getClass();
            isStatic = false;
        } else {
            // 这是一个在Bean类上的静态工厂方法。
            if (!mbd.hasBeanClass()) {
                throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName, "bean definition declares neither a bean class nor a factory-bean reference");
            }
            factoryBean = null;
            factoryClass = mbd.getBeanClass();
            isStatic = true;
        }
        Method factoryMethodToUse = null;
        ArgumentsHolder argsHolderToUse = null;
        Object[] argsToUse = null;
        if (explicitArgs != null) {
            argsToUse = explicitArgs;
        } else {
            Object[] argsToResolve = null;
            synchronized (mbd.constructorArgumentLock) {
                factoryMethodToUse = (Method) mbd.resolvedConstructorOrFactoryMethod;
                if (factoryMethodToUse != null && mbd.constructorArgumentsResolved) {
                    // 找到缓存的工厂方法...
                    argsToUse = mbd.resolvedConstructorArguments;
                    if (argsToUse == null) {
                        argsToResolve = mbd.preparedConstructorArguments;
                    }
                }
            }
            if (argsToResolve != null) {
                argsToUse = resolvePreparedArguments(beanName, mbd, bw, factoryMethodToUse, argsToResolve);
            }
        }
        if (factoryMethodToUse == null || argsToUse == null) {
            // 需要确定工厂方法...
            // 尝试所有具有此名称的方法，以查看它们是否与给定的参数匹配。
            factoryClass = ClassUtils.getUserClass(factoryClass);
            List<Method> candidates = null;
            if (mbd.isFactoryMethodUnique) {
                if (factoryMethodToUse == null) {
                    factoryMethodToUse = mbd.getResolvedFactoryMethod();
                }
                if (factoryMethodToUse != null) {
                    candidates = Collections.singletonList(factoryMethodToUse);
                }
            }
            if (candidates == null) {
                candidates = new ArrayList<>();
                Method[] rawCandidates = getCandidateMethods(factoryClass, mbd);
                for (Method candidate : rawCandidates) {
                    if ((!isStatic || isStaticCandidate(candidate, factoryClass)) && mbd.isFactoryMethod(candidate)) {
                        candidates.add(candidate);
                    }
                }
            }
            if (candidates.size() == 1 && explicitArgs == null && !mbd.hasConstructorArgumentValues()) {
                Method uniqueCandidate = candidates.get(0);
                if (uniqueCandidate.getParameterCount() == 0) {
                    mbd.factoryMethodToIntrospect = uniqueCandidate;
                    synchronized (mbd.constructorArgumentLock) {
                        mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
                        mbd.constructorArgumentsResolved = true;
                        mbd.resolvedConstructorArguments = EMPTY_ARGS;
                    }
                    bw.setBeanInstance(instantiate(beanName, mbd, factoryBean, uniqueCandidate, EMPTY_ARGS));
                    return bw;
                }
            }
            if (candidates.size() > 1) {
                // 显式跳过不可变的 singletonList
                candidates.sort(AutowireUtils.EXECUTABLE_COMPARATOR);
            }
            ConstructorArgumentValues resolvedValues = null;
            boolean autowiring = (mbd.getResolvedAutowireMode() == AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
            int minTypeDiffWeight = Integer.MAX_VALUE;
            Set<Method> ambiguousFactoryMethods = null;
            int minNrOfArgs;
            if (explicitArgs != null) {
                minNrOfArgs = explicitArgs.length;
            } else {
                // 我们没有通过编程方式传递参数，因此我们需要解决
                // 构造函数中指定的参数保存在 Bean 定义中。
                if (mbd.hasConstructorArgumentValues()) {
                    ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
                    resolvedValues = new ConstructorArgumentValues();
                    minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
                } else {
                    minNrOfArgs = 0;
                }
            }
            Deque<UnsatisfiedDependencyException> causes = null;
            for (Method candidate : candidates) {
                int parameterCount = candidate.getParameterCount();
                if (parameterCount >= minNrOfArgs) {
                    ArgumentsHolder argsHolder;
                    Class<?>[] paramTypes = candidate.getParameterTypes();
                    if (explicitArgs != null) {
                        // 显式给出的参数 -> 参数长度必须完全匹配。
                        if (paramTypes.length != explicitArgs.length) {
                            continue;
                        }
                        argsHolder = new ArgumentsHolder(explicitArgs);
                    } else {
                        // 已解析构造函数参数：需要进行类型转换和/或自动装配。
                        try {
                            String[] paramNames = null;
                            if (resolvedValues != null && resolvedValues.containsNamedArgument()) {
                                ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                                if (pnd != null) {
                                    paramNames = pnd.getParameterNames(candidate);
                                }
                            }
                            argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, paramTypes, paramNames, candidate, autowiring, candidates.size() == 1);
                        } catch (UnsatisfiedDependencyException ex) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Ignoring factory method [" + candidate + "] of bean '" + beanName + "': " + ex);
                            }
                            // 吞咽并尝试下一个重载的工厂方法。
                            if (causes == null) {
                                causes = new ArrayDeque<>(1);
                            }
                            causes.add(ex);
                            continue;
                        }
                    }
                    int typeDiffWeight = (mbd.isLenientConstructorResolution() ? argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
                    // 选择此工厂方法，如果它代表最接近的匹配。
                    if (typeDiffWeight < minTypeDiffWeight) {
                        factoryMethodToUse = candidate;
                        argsHolderToUse = argsHolder;
                        argsToUse = argsHolder.arguments;
                        minTypeDiffWeight = typeDiffWeight;
                        ambiguousFactoryMethods = null;
                    } else // 了解模糊性的概念：在相同类型差异权重的情形下
                    // 对于参数数量相同的函数，收集这样的候选者
                    // 最终引发一个歧义异常。
                    // 然而，仅在非宽容构造函数解析模式下执行该检查
                    // 并且显式忽略具有相同参数签名的重写方法。
                    if (factoryMethodToUse != null && typeDiffWeight == minTypeDiffWeight && !mbd.isLenientConstructorResolution() && paramTypes.length == factoryMethodToUse.getParameterCount() && !Arrays.equals(paramTypes, factoryMethodToUse.getParameterTypes())) {
                        if (ambiguousFactoryMethods == null) {
                            ambiguousFactoryMethods = new LinkedHashSet<>();
                            ambiguousFactoryMethods.add(factoryMethodToUse);
                        }
                        ambiguousFactoryMethods.add(candidate);
                    }
                }
            }
            if (factoryMethodToUse == null || argsToUse == null) {
                if (causes != null) {
                    UnsatisfiedDependencyException ex = causes.removeLast();
                    for (Exception cause : causes) {
                        this.beanFactory.onSuppressedException(cause);
                    }
                    throw ex;
                }
                List<String> argTypes = new ArrayList<>(minNrOfArgs);
                if (explicitArgs != null) {
                    for (Object arg : explicitArgs) {
                        argTypes.add(arg != null ? arg.getClass().getSimpleName() : "null");
                    }
                } else if (resolvedValues != null) {
                    Set<ValueHolder> valueHolders = new LinkedHashSet<>(resolvedValues.getArgumentCount());
                    valueHolders.addAll(resolvedValues.getIndexedArgumentValues().values());
                    valueHolders.addAll(resolvedValues.getGenericArgumentValues());
                    for (ValueHolder value : valueHolders) {
                        String argType = (value.getType() != null ? ClassUtils.getShortName(value.getType()) : (value.getValue() != null ? value.getValue().getClass().getSimpleName() : "null"));
                        argTypes.add(argType);
                    }
                }
                String argDesc = StringUtils.collectionToCommaDelimitedString(argTypes);
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "No matching factory method found on class [" + factoryClass.getName() + "]: " + (mbd.getFactoryBeanName() != null ? "factory bean '" + mbd.getFactoryBeanName() + "'; " : "") + "factory method '" + mbd.getFactoryMethodName() + "(" + argDesc + ")'. " + "Check that a method with the specified name " + (minNrOfArgs > 0 ? "and arguments " : "") + "exists and that it is " + (isStatic ? "static" : "non-static") + ".");
            } else if (void.class == factoryMethodToUse.getReturnType()) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid factory method '" + mbd.getFactoryMethodName() + "' on class [" + factoryClass.getName() + "]: needs to have a non-void return type!");
            } else if (ambiguousFactoryMethods != null) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Ambiguous factory method matches found on class [" + factoryClass.getName() + "] " + "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " + ambiguousFactoryMethods);
            }
            if (explicitArgs == null && argsHolderToUse != null) {
                mbd.factoryMethodToIntrospect = factoryMethodToUse;
                argsHolderToUse.storeCache(mbd, factoryMethodToUse);
            }
        }
        bw.setBeanInstance(instantiate(beanName, mbd, factoryBean, factoryMethodToUse, argsToUse));
        return bw;
    }

    private Object instantiate(String beanName, RootBeanDefinition mbd, @Nullable Object factoryBean, Method factoryMethod, Object[] args) {
        try {
            return this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName, this.beanFactory, factoryBean, factoryMethod, args);
        } catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
        }
    }

    /**
     * 将该 bean 的构造函数参数解析到 resolvedValues 对象中。
     * 这可能涉及到查找其他 bean。
     * <p>此方法还用于处理对静态工厂方法的调用。
     */
    private int resolveConstructorArguments(String beanName, RootBeanDefinition mbd, BeanWrapper bw, ConstructorArgumentValues cargs, ConstructorArgumentValues resolvedValues) {
        TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
        TypeConverter converter = (customConverter != null ? customConverter : bw);
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);
        int minNrOfArgs = cargs.getArgumentCount();
        for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : cargs.getIndexedArgumentValues().entrySet()) {
            int index = entry.getKey();
            if (index < 0) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid constructor argument index: " + index);
            }
            if (index + 1 > minNrOfArgs) {
                minNrOfArgs = index + 1;
            }
            ConstructorArgumentValues.ValueHolder valueHolder = entry.getValue();
            if (valueHolder.isConverted()) {
                resolvedValues.addIndexedArgumentValue(index, valueHolder);
            } else {
                Object resolvedValue = valueResolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());
                ConstructorArgumentValues.ValueHolder resolvedValueHolder = new ConstructorArgumentValues.ValueHolder(resolvedValue, valueHolder.getType(), valueHolder.getName());
                resolvedValueHolder.setSource(valueHolder);
                resolvedValues.addIndexedArgumentValue(index, resolvedValueHolder);
            }
        }
        for (ConstructorArgumentValues.ValueHolder valueHolder : cargs.getGenericArgumentValues()) {
            if (valueHolder.isConverted()) {
                resolvedValues.addGenericArgumentValue(valueHolder);
            } else {
                Object resolvedValue = valueResolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());
                ConstructorArgumentValues.ValueHolder resolvedValueHolder = new ConstructorArgumentValues.ValueHolder(resolvedValue, valueHolder.getType(), valueHolder.getName());
                resolvedValueHolder.setSource(valueHolder);
                resolvedValues.addGenericArgumentValue(resolvedValueHolder);
            }
        }
        return minNrOfArgs;
    }

    /**
     * 创建一个参数数组以调用构造函数或工厂方法，
     * 给定已解析的构造函数参数值。
     */
    private ArgumentsHolder createArgumentArray(String beanName, RootBeanDefinition mbd, @Nullable ConstructorArgumentValues resolvedValues, BeanWrapper bw, Class<?>[] paramTypes, @Nullable String[] paramNames, Executable executable, boolean autowiring, boolean fallback) throws UnsatisfiedDependencyException {
        TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
        TypeConverter converter = (customConverter != null ? customConverter : bw);
        ArgumentsHolder args = new ArgumentsHolder(paramTypes.length);
        Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = new HashSet<>(paramTypes.length);
        Set<String> allAutowiredBeanNames = new LinkedHashSet<>(paramTypes.length * 2);
        for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
            Class<?> paramType = paramTypes[paramIndex];
            String paramName = (paramNames != null ? paramNames[paramIndex] : "");
            // 尝试查找匹配的构造函数参数值，无论是索引还是泛型。
            ConstructorArgumentValues.ValueHolder valueHolder = null;
            if (resolvedValues != null) {
                valueHolder = resolvedValues.getArgumentValue(paramIndex, paramType, paramName, usedValueHolders);
                // 如果我们找不到直接匹配，并且不应该进行自动装配，
                // 让我们尝试下一个通用的、未进行类型注解的参数值作为后备选项：
                // 它可以在类型转换后匹配（例如，String -> int）。
                if (valueHolder == null && (!autowiring || paramTypes.length == resolvedValues.getArgumentCount())) {
                    valueHolder = resolvedValues.getGenericArgumentValue(null, null, usedValueHolders);
                }
            }
            if (valueHolder != null) {
                // 我们发现了一个潜在匹配项 - 让我们试一试。
                // 不要多次考虑相同的值定义！
                usedValueHolders.add(valueHolder);
                Object originalValue = valueHolder.getValue();
                Object convertedValue;
                if (valueHolder.isConverted()) {
                    convertedValue = valueHolder.getConvertedValue();
                    args.preparedArguments[paramIndex] = convertedValue;
                } else {
                    MethodParameter methodParam = MethodParameter.forExecutable(executable, paramIndex);
                    try {
                        convertedValue = converter.convertIfNecessary(originalValue, paramType, methodParam);
                    } catch (TypeMismatchException ex) {
                        throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam), "Could not convert argument value of type [" + ObjectUtils.nullSafeClassName(valueHolder.getValue()) + "] to required type [" + paramType.getName() + "]: " + ex.getMessage());
                    }
                    Object sourceHolder = valueHolder.getSource();
                    if (sourceHolder instanceof ConstructorArgumentValues.ValueHolder constructorValueHolder) {
                        Object sourceValue = constructorValueHolder.getValue();
                        args.resolveNecessary = true;
                        args.preparedArguments[paramIndex] = sourceValue;
                    }
                }
                args.arguments[paramIndex] = convertedValue;
                args.rawArguments[paramIndex] = originalValue;
            } else {
                MethodParameter methodParam = MethodParameter.forExecutable(executable, paramIndex);
                // 没有找到显式匹配：我们要么应该自动装配，或者
                // 必须失败，因为为给定的构造函数创建参数数组。
                if (!autowiring) {
                    throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam), "Ambiguous argument values for parameter of type [" + paramType.getName() + "] - did you specify the correct bean references as arguments?");
                }
                try {
                    ConstructorDependencyDescriptor desc = new ConstructorDependencyDescriptor(methodParam, true);
                    Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
                    Object arg = resolveAutowiredArgument(desc, paramType, beanName, autowiredBeanNames, converter, fallback);
                    if (arg != null) {
                        setShortcutIfPossible(desc, paramType, autowiredBeanNames);
                    }
                    allAutowiredBeanNames.addAll(autowiredBeanNames);
                    args.rawArguments[paramIndex] = arg;
                    args.arguments[paramIndex] = arg;
                    args.preparedArguments[paramIndex] = desc;
                    args.resolveNecessary = true;
                } catch (BeansException ex) {
                    throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam), ex);
                }
            }
        }
        registerDependentBeans(executable, beanName, allAutowiredBeanNames);
        return args;
    }

    /**
     * 解析存储在给定Bean定义中的准备好的参数。
     */
    private Object[] resolvePreparedArguments(String beanName, RootBeanDefinition mbd, BeanWrapper bw, Executable executable, Object[] argsToResolve) {
        TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
        TypeConverter converter = (customConverter != null ? customConverter : bw);
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);
        Class<?>[] paramTypes = executable.getParameterTypes();
        Object[] resolvedArgs = new Object[argsToResolve.length];
        for (int argIndex = 0; argIndex < argsToResolve.length; argIndex++) {
            Object argValue = argsToResolve[argIndex];
            Class<?> paramType = paramTypes[argIndex];
            boolean convertNecessary = false;
            if (argValue instanceof ConstructorDependencyDescriptor descriptor) {
                try {
                    argValue = resolveAutowiredArgument(descriptor, paramType, beanName, null, converter, true);
                } catch (BeansException ex) {
                    // 缓存参数的预期目标 Bean 不匹配 -> 重新解析
                    Set<String> autowiredBeanNames = null;
                    if (descriptor.hasShortcut()) {
                        // 重置快捷键并尝试在本线程中重新解析它...
                        descriptor.setShortcut(null);
                        autowiredBeanNames = new LinkedHashSet<>(2);
                    }
                    logger.debug("Failed to resolve cached argument", ex);
                    argValue = resolveAutowiredArgument(descriptor, paramType, beanName, autowiredBeanNames, converter, true);
                    if (autowiredBeanNames != null && !descriptor.hasShortcut()) {
                        // 我们之前遇到过这个过时的快捷方式，这个快捷方式有
                        // 在此期间未被另一个线程重新解析...
                        if (argValue != null) {
                            setShortcutIfPossible(descriptor, paramType, autowiredBeanNames);
                        }
                        registerDependentBeans(executable, beanName, autowiredBeanNames);
                    }
                }
            } else if (argValue instanceof BeanMetadataElement) {
                argValue = valueResolver.resolveValueIfNecessary("constructor argument", argValue);
                convertNecessary = true;
            } else if (argValue instanceof String text) {
                argValue = this.beanFactory.evaluateBeanDefinitionString(text, mbd);
                convertNecessary = true;
            }
            if (convertNecessary) {
                MethodParameter methodParam = MethodParameter.forExecutable(executable, argIndex);
                try {
                    argValue = converter.convertIfNecessary(argValue, paramType, methodParam);
                } catch (TypeMismatchException ex) {
                    throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, new InjectionPoint(methodParam), "Could not convert argument value of type [" + ObjectUtils.nullSafeClassName(argValue) + "] to required type [" + paramType.getName() + "]: " + ex.getMessage());
                }
            }
            resolvedArgs[argIndex] = argValue;
        }
        return resolvedArgs;
    }

    private Constructor<?> getUserDeclaredConstructor(Constructor<?> constructor) {
        Class<?> declaringClass = constructor.getDeclaringClass();
        Class<?> userClass = ClassUtils.getUserClass(declaringClass);
        if (userClass != declaringClass) {
            try {
                return userClass.getDeclaredConstructor(constructor.getParameterTypes());
            } catch (NoSuchMethodException ex) {
                // 在用户类（超类）中没有等效的构造函数...
                // 我们将按照通常的方式继续使用给定的构造函数。
            }
        }
        return constructor;
    }

    /**
     * 解决指定的要自动装配的参数。
     */
    @Nullable
    Object resolveAutowiredArgument(DependencyDescriptor descriptor, Class<?> paramType, String beanName, @Nullable Set<String> autowiredBeanNames, TypeConverter typeConverter, boolean fallback) {
        if (InjectionPoint.class.isAssignableFrom(paramType)) {
            InjectionPoint injectionPoint = currentInjectionPoint.get();
            if (injectionPoint == null) {
                throw new IllegalStateException("No current InjectionPoint available for " + descriptor);
            }
            return injectionPoint;
        }
        try {
            return this.beanFactory.resolveDependency(descriptor, beanName, autowiredBeanNames, typeConverter);
        } catch (NoUniqueBeanDefinitionException ex) {
            throw ex;
        } catch (NoSuchBeanDefinitionException ex) {
            if (fallback) {
                // 单个构造函数或工厂方法 -> 让我们返回一个空数组/集合
                // 例如，一个可变参数（vararg）或非空的 List/Set/Map 参数。
                if (paramType.isArray()) {
                    return Array.newInstance(paramType.getComponentType(), 0);
                } else if (CollectionFactory.isApproximableCollectionType(paramType)) {
                    return CollectionFactory.createCollection(paramType, 0);
                } else if (CollectionFactory.isApproximableMapType(paramType)) {
                    return CollectionFactory.createMap(paramType, 0);
                }
            }
            throw ex;
        }
    }

    private void setShortcutIfPossible(ConstructorDependencyDescriptor descriptor, Class<?> paramType, Set<String> autowiredBeanNames) {
        if (autowiredBeanNames.size() == 1) {
            String autowiredBeanName = autowiredBeanNames.iterator().next();
            if (this.beanFactory.containsBean(autowiredBeanName) && this.beanFactory.isTypeMatch(autowiredBeanName, paramType)) {
                descriptor.setShortcut(autowiredBeanName);
            }
        }
    }

    private void registerDependentBeans(Executable executable, String beanName, Set<String> autowiredBeanNames) {
        for (String autowiredBeanName : autowiredBeanNames) {
            this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
            if (logger.isDebugEnabled()) {
                logger.debug("Autowiring by type from bean name '" + beanName + "' via " + (executable instanceof Constructor ? "constructor" : "factory method") + " to bean named '" + autowiredBeanName + "'");
            }
        }
    }

    // 面向AOT（Ahead-of-Time）的预解析
    public Executable resolveConstructorOrFactoryMethod(String beanName, RootBeanDefinition mbd) {
        Supplier<ResolvableType> beanType = () -> getBeanType(beanName, mbd);
        List<ResolvableType> valueTypes = (mbd.hasConstructorArgumentValues() ? determineParameterValueTypes(mbd) : Collections.emptyList());
        Method resolvedFactoryMethod = resolveFactoryMethod(beanName, mbd, valueTypes);
        if (resolvedFactoryMethod != null) {
            return resolvedFactoryMethod;
        }
        Class<?> factoryBeanClass = getFactoryBeanClass(beanName, mbd);
        if (factoryBeanClass != null && !factoryBeanClass.equals(mbd.getResolvableType().toClass())) {
            ResolvableType resolvableType = mbd.getResolvableType();
            boolean isCompatible = ResolvableType.forClass(factoryBeanClass).as(FactoryBean.class).getGeneric(0).isAssignableFrom(resolvableType);
            Assert.state(isCompatible, () -> String.format("Incompatible target type '%s' for factory bean '%s'", resolvableType.toClass().getName(), factoryBeanClass.getName()));
            Constructor<?> constructor = resolveConstructor(beanName, mbd, () -> ResolvableType.forClass(factoryBeanClass), valueTypes);
            if (constructor != null) {
                return constructor;
            }
            throw new IllegalStateException("No suitable FactoryBean constructor found for " + mbd + " and argument types " + valueTypes);
        }
        Constructor<?> constructor = resolveConstructor(beanName, mbd, beanType, valueTypes);
        if (constructor != null) {
            return constructor;
        }
        throw new IllegalStateException("No constructor or factory method candidate found for " + mbd + " and argument types " + valueTypes);
    }

    private List<ResolvableType> determineParameterValueTypes(RootBeanDefinition mbd) {
        List<ResolvableType> parameterTypes = new ArrayList<>();
        for (ValueHolder valueHolder : mbd.getConstructorArgumentValues().getIndexedArgumentValues().values()) {
            parameterTypes.add(determineParameterValueType(mbd, valueHolder));
        }
        return parameterTypes;
    }

    private ResolvableType determineParameterValueType(RootBeanDefinition mbd, ValueHolder valueHolder) {
        if (valueHolder.getType() != null) {
            return ResolvableType.forClass(ClassUtils.resolveClassName(valueHolder.getType(), this.beanFactory.getBeanClassLoader()));
        }
        Object value = valueHolder.getValue();
        if (value instanceof BeanReference br) {
            if (value instanceof RuntimeBeanReference rbr) {
                if (rbr.getBeanType() != null) {
                    return ResolvableType.forClass(rbr.getBeanType());
                }
            }
            return ResolvableType.forClass(this.beanFactory.getType(br.getBeanName(), false));
        }
        if (value instanceof BeanDefinition innerBd) {
            String nameToUse = "(inner bean)";
            ResolvableType type = getBeanType(nameToUse, this.beanFactory.getMergedBeanDefinition(nameToUse, innerBd, mbd));
            return (FactoryBean.class.isAssignableFrom(type.toClass()) ? type.as(FactoryBean.class).getGeneric(0) : type);
        }
        if (value instanceof Class<?> clazz) {
            return ResolvableType.forClassWithGenerics(Class.class, clazz);
        }
        return ResolvableType.forInstance(value);
    }

    @Nullable
    private Constructor<?> resolveConstructor(String beanName, RootBeanDefinition mbd, Supplier<ResolvableType> beanType, List<ResolvableType> valueTypes) {
        Class<?> type = ClassUtils.getUserClass(beanType.get().toClass());
        Constructor<?>[] ctors = this.beanFactory.determineConstructorsFromBeanPostProcessors(type, beanName);
        if (ctors == null) {
            if (!mbd.hasConstructorArgumentValues()) {
                ctors = mbd.getPreferredConstructors();
            }
            if (ctors == null) {
                ctors = (mbd.isNonPublicAccessAllowed() ? type.getDeclaredConstructors() : type.getConstructors());
            }
        }
        if (ctors.length == 1) {
            return ctors[0];
        }
        Function<Constructor<?>, List<ResolvableType>> parameterTypesFactory = executable -> {
            List<ResolvableType> types = new ArrayList<>();
            for (int i = 0; i < executable.getParameterCount(); i++) {
                types.add(ResolvableType.forConstructorParameter(executable, i));
            }
            return types;
        };
        List<Constructor<?>> matches = Arrays.stream(ctors).filter(executable -> match(parameterTypesFactory.apply(executable), valueTypes, FallbackMode.NONE)).toList();
        if (matches.size() == 1) {
            return matches.get(0);
        }
        List<Constructor<?>> assignableElementFallbackMatches = Arrays.stream(ctors).filter(executable -> match(parameterTypesFactory.apply(executable), valueTypes, FallbackMode.ASSIGNABLE_ELEMENT)).toList();
        if (assignableElementFallbackMatches.size() == 1) {
            return assignableElementFallbackMatches.get(0);
        }
        List<Constructor<?>> typeConversionFallbackMatches = Arrays.stream(ctors).filter(executable -> match(parameterTypesFactory.apply(executable), valueTypes, FallbackMode.TYPE_CONVERSION)).toList();
        return (typeConversionFallbackMatches.size() == 1 ? typeConversionFallbackMatches.get(0) : null);
    }

    @Nullable
    private Method resolveFactoryMethod(String beanName, RootBeanDefinition mbd, List<ResolvableType> valueTypes) {
        if (mbd.isFactoryMethodUnique) {
            Method resolvedFactoryMethod = mbd.getResolvedFactoryMethod();
            if (resolvedFactoryMethod != null) {
                return resolvedFactoryMethod;
            }
        }
        String factoryMethodName = mbd.getFactoryMethodName();
        if (factoryMethodName != null) {
            String factoryBeanName = mbd.getFactoryBeanName();
            Class<?> factoryClass;
            boolean isStatic;
            if (factoryBeanName != null) {
                factoryClass = this.beanFactory.getType(factoryBeanName);
                isStatic = false;
            } else {
                factoryClass = this.beanFactory.resolveBeanClass(mbd, beanName);
                isStatic = true;
            }
            Assert.state(factoryClass != null, () -> "Failed to determine bean class of " + mbd);
            Method[] rawCandidates = getCandidateMethods(factoryClass, mbd);
            List<Method> candidates = new ArrayList<>();
            for (Method candidate : rawCandidates) {
                if ((!isStatic || isStaticCandidate(candidate, factoryClass)) && mbd.isFactoryMethod(candidate)) {
                    candidates.add(candidate);
                }
            }
            Method result = null;
            if (candidates.size() == 1) {
                result = candidates.get(0);
            } else if (candidates.size() > 1) {
                Function<Method, List<ResolvableType>> parameterTypesFactory = method -> {
                    List<ResolvableType> types = new ArrayList<>();
                    for (int i = 0; i < method.getParameterCount(); i++) {
                        types.add(ResolvableType.forMethodParameter(method, i));
                    }
                    return types;
                };
                result = resolveFactoryMethod(candidates, parameterTypesFactory, valueTypes);
            }
            if (result == null) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "No matching factory method found on class [" + factoryClass.getName() + "]: " + (mbd.getFactoryBeanName() != null ? "factory bean '" + mbd.getFactoryBeanName() + "'; " : "") + "factory method '" + mbd.getFactoryMethodName() + "'. ");
            }
            return result;
        }
        return null;
    }

    @Nullable
    private Method resolveFactoryMethod(List<Method> executables, Function<Method, List<ResolvableType>> parameterTypesFactory, List<ResolvableType> valueTypes) {
        List<Method> matches = executables.stream().filter(executable -> match(parameterTypesFactory.apply(executable), valueTypes, FallbackMode.NONE)).toList();
        if (matches.size() == 1) {
            return matches.get(0);
        }
        List<Method> assignableElementFallbackMatches = executables.stream().filter(executable -> match(parameterTypesFactory.apply(executable), valueTypes, FallbackMode.ASSIGNABLE_ELEMENT)).toList();
        if (assignableElementFallbackMatches.size() == 1) {
            return assignableElementFallbackMatches.get(0);
        }
        List<Method> typeConversionFallbackMatches = executables.stream().filter(executable -> match(parameterTypesFactory.apply(executable), valueTypes, FallbackMode.TYPE_CONVERSION)).toList();
        Assert.state(typeConversionFallbackMatches.size() <= 1, () -> "Multiple matches with parameters '" + valueTypes + "': " + typeConversionFallbackMatches);
        return (typeConversionFallbackMatches.size() == 1 ? typeConversionFallbackMatches.get(0) : null);
    }

    private boolean match(List<ResolvableType> parameterTypes, List<ResolvableType> valueTypes, FallbackMode fallbackMode) {
        if (parameterTypes.size() != valueTypes.size()) {
            return false;
        }
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (!isMatch(parameterTypes.get(i), valueTypes.get(i), fallbackMode)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMatch(ResolvableType parameterType, ResolvableType valueType, FallbackMode fallbackMode) {
        if (isAssignable(valueType).test(parameterType)) {
            return true;
        }
        return switch(fallbackMode) {
            case ASSIGNABLE_ELEMENT ->
                isAssignable(valueType).test(extractElementType(parameterType));
            case TYPE_CONVERSION ->
                typeConversionFallback(valueType).test(parameterType);
            default ->
                false;
        };
    }

    private Predicate<ResolvableType> isAssignable(ResolvableType valueType) {
        return parameterType -> parameterType.isAssignableFrom(valueType);
    }

    private ResolvableType extractElementType(ResolvableType parameterType) {
        if (parameterType.isArray()) {
            return parameterType.getComponentType();
        }
        if (Collection.class.isAssignableFrom(parameterType.toClass())) {
            return parameterType.as(Collection.class).getGeneric(0);
        }
        return ResolvableType.NONE;
    }

    private Predicate<ResolvableType> typeConversionFallback(ResolvableType valueType) {
        return parameterType -> {
            if (valueOrCollection(valueType, this::isStringForClassFallback).test(parameterType)) {
                return true;
            }
            return valueOrCollection(valueType, this::isSimpleValueType).test(parameterType);
        };
    }

    private Predicate<ResolvableType> valueOrCollection(ResolvableType valueType, Function<ResolvableType, Predicate<ResolvableType>> predicateProvider) {
        return parameterType -> {
            if (predicateProvider.apply(valueType).test(parameterType)) {
                return true;
            }
            if (predicateProvider.apply(extractElementType(valueType)).test(extractElementType(parameterType))) {
                return true;
            }
            return (predicateProvider.apply(valueType).test(extractElementType(parameterType)));
        };
    }

    /**
     * 返回一个用于参数类型的 {@link Predicate}，该断言检查其目标值是否为 {@link Class} 且值类型为 {@link String}。这是一个常见的用例，其中在 Bean 定义中定义了一个类的全限定名 (FQN) 作为类。
     * @param valueType 值的类型
     * @return 一个断言，指示一个 String 到 Class 参数的回退匹配
     */
    private Predicate<ResolvableType> isStringForClassFallback(ResolvableType valueType) {
        return parameterType -> (valueType.isAssignableFrom(String.class) && parameterType.isAssignableFrom(Class.class));
    }

    private Predicate<ResolvableType> isSimpleValueType(ResolvableType valueType) {
        return parameterType -> (BeanUtils.isSimpleValueType(parameterType.toClass()) && BeanUtils.isSimpleValueType(valueType.toClass()));
    }

    @Nullable
    private Class<?> getFactoryBeanClass(String beanName, RootBeanDefinition mbd) {
        Class<?> beanClass = this.beanFactory.resolveBeanClass(mbd, beanName);
        return (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass) ? beanClass : null);
    }

    private ResolvableType getBeanType(String beanName, RootBeanDefinition mbd) {
        ResolvableType resolvableType = mbd.getResolvableType();
        if (resolvableType != ResolvableType.NONE) {
            return resolvableType;
        }
        return ResolvableType.forClass(this.beanFactory.resolveBeanClass(mbd, beanName));
    }

    static InjectionPoint setCurrentInjectionPoint(@Nullable InjectionPoint injectionPoint) {
        InjectionPoint old = currentInjectionPoint.get();
        if (injectionPoint != null) {
            currentInjectionPoint.set(injectionPoint);
        } else {
            currentInjectionPoint.remove();
        }
        return old;
    }

    /**
     * 请参阅 {@link BeanUtils#getResolvableConstructor(Class)} 以了解对齐方式。
     * 此变体在默认构造函数可用的情况下添加了一个宽容的回退，类似于
     * {@link org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#determineCandidateConstructors}。
     */
    @Nullable
    static Constructor<?>[] determinePreferredConstructors(Class<?> clazz) {
        Constructor<?> primaryCtor = BeanUtils.findPrimaryConstructor(clazz);
        Constructor<?> defaultCtor;
        try {
            defaultCtor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            defaultCtor = null;
        }
        if (primaryCtor != null) {
            if (defaultCtor != null && !primaryCtor.equals(defaultCtor)) {
                return new Constructor<?>[] { primaryCtor, defaultCtor };
            } else {
                return new Constructor<?>[] { primaryCtor };
            }
        }
        Constructor<?>[] ctors = clazz.getConstructors();
        if (ctors.length == 1) {
            // 一个单独的公共构造函数，可能结合一个非公共的默认构造函数
            if (defaultCtor != null && !ctors[0].equals(defaultCtor)) {
                return new Constructor<?>[] { ctors[0], defaultCtor };
            } else {
                return ctors;
            }
        } else if (ctors.length == 0) {
            // 没有公共构造函数 -> 检查非公共
            ctors = clazz.getDeclaredConstructors();
            if (ctors.length == 1) {
                // 一个单独的非公共构造函数，例如来自一个非公共的记录类型
                return ctors;
            }
        }
        return null;
    }

    /**
     * 用于存储参数组合的私有内部类。
     */
    private static class ArgumentsHolder {

        public final Object[] rawArguments;

        public final Object[] arguments;

        public final Object[] preparedArguments;

        public boolean resolveNecessary = false;

        public ArgumentsHolder(int size) {
            this.rawArguments = new Object[size];
            this.arguments = new Object[size];
            this.preparedArguments = new Object[size];
        }

        public ArgumentsHolder(Object[] args) {
            this.rawArguments = args;
            this.arguments = args;
            this.preparedArguments = args;
        }

        public int getTypeDifferenceWeight(Class<?>[] paramTypes) {
            // 如果找到有效的参数，确定类型差异权重。
            // 尝试在转换后的参数上应用类型差异权重。
            // 原始参数。如果原始权重更好，则使用它。
            // 将原始重量减少1024以优先选择等于转换后的重量。
            int typeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.arguments);
            int rawTypeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.rawArguments) - 1024;
            return Math.min(rawTypeDiffWeight, typeDiffWeight);
        }

        public int getAssignabilityWeight(Class<?>[] paramTypes) {
            for (int i = 0; i < paramTypes.length; i++) {
                if (!ClassUtils.isAssignableValue(paramTypes[i], this.arguments[i])) {
                    return Integer.MAX_VALUE;
                }
            }
            for (int i = 0; i < paramTypes.length; i++) {
                if (!ClassUtils.isAssignableValue(paramTypes[i], this.rawArguments[i])) {
                    return Integer.MAX_VALUE - 512;
                }
            }
            return Integer.MAX_VALUE - 1024;
        }

        public void storeCache(RootBeanDefinition mbd, Executable constructorOrFactoryMethod) {
            synchronized (mbd.constructorArgumentLock) {
                mbd.resolvedConstructorOrFactoryMethod = constructorOrFactoryMethod;
                mbd.constructorArgumentsResolved = true;
                if (this.resolveNecessary) {
                    mbd.preparedConstructorArguments = this.preparedArguments;
                } else {
                    mbd.resolvedConstructorArguments = this.arguments;
                }
            }
        }
    }

    /**
     * 检查 Java 的 {@link ConstructorProperties} 注解的代理
     */
    private static class ConstructorPropertiesChecker {

        @Nullable
        public static String[] evaluate(Constructor<?> candidate, int paramCount) {
            ConstructorProperties cp = candidate.getAnnotation(ConstructorProperties.class);
            if (cp != null) {
                String[] names = cp.value();
                if (names.length != paramCount) {
                    throw new IllegalStateException("Constructor annotated with @ConstructorProperties but not " + "corresponding to actual number of parameters (" + paramCount + "): " + candidate);
                }
                return names;
            } else {
                return null;
            }
        }
    }

    /**
     * 构造函数参数的依赖描述符标记，
     * 用于区分提供的依赖描述符实例与用于自动装配目的的内部构建的依赖描述符。
     */
    @SuppressWarnings("serial")
    private static class ConstructorDependencyDescriptor extends DependencyDescriptor {

        @Nullable
        private volatile String shortcut;

        public ConstructorDependencyDescriptor(MethodParameter methodParameter, boolean required) {
            super(methodParameter, required);
        }

        public void setShortcut(@Nullable String shortcut) {
            this.shortcut = shortcut;
        }

        public boolean hasShortcut() {
            return (this.shortcut != null);
        }

        @Override
        public Object resolveShortcut(BeanFactory beanFactory) {
            String shortcut = this.shortcut;
            return (shortcut != null ? beanFactory.getBean(shortcut, getDependencyType()) : null);
        }
    }

    private enum FallbackMode {

        NONE, ASSIGNABLE_ELEMENT, TYPE_CONVERSION
    }
}
