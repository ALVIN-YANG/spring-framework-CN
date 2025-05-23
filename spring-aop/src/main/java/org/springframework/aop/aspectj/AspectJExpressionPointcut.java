// 翻译完成 glm-4-flash
/*版权所有 2002-2024 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）；除非遵守许可证，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.weaver.patterns.NamePattern;
import org.aspectj.weaver.reflect.ReflectionWorld.ReflectionWorldException;
import org.aspectj.weaver.reflect.ShadowMatchImpl;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;
import org.aspectj.weaver.tools.UnsupportedPointcutPrimitiveException;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.autoproxy.ProxyCreationContext;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.AbstractExpressionPointcut;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring 的 {@link org.springframework.aop.Pointcut} 实现
 * 使用 AspectJ 编译器来评估点切表达式。
 *
 * <p>点切表达式的值是一个 AspectJ 表达式。它可以引用其他点切
 * 并使用组合以及其他操作。
 *
 * <p>自然地，由于这将通过 Spring AOP 的基于代理的模型进行处理，
 * 仅支持方法执行点切。
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Dave Syer
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJExpressionPointcut extends AbstractExpressionPointcut implements ClassFilter, IntroductionAwareMethodMatcher, BeanFactoryAware {

    private static final String AJC_MAGIC = "ajc$";

    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = Set.of(PointcutPrimitive.EXECUTION, PointcutPrimitive.ARGS, PointcutPrimitive.REFERENCE, PointcutPrimitive.THIS, PointcutPrimitive.TARGET, PointcutPrimitive.WITHIN, PointcutPrimitive.AT_ANNOTATION, PointcutPrimitive.AT_WITHIN, PointcutPrimitive.AT_ARGS, PointcutPrimitive.AT_TARGET);

    private static final Log logger = LogFactory.getLog(AspectJExpressionPointcut.class);

    @Nullable
    private Class<?> pointcutDeclarationScope;

    private boolean aspectCompiledByAjc;

    private String[] pointcutParameterNames = new String[0];

    private Class<?>[] pointcutParameterTypes = new Class<?>[0];

    @Nullable
    private BeanFactory beanFactory;

    @Nullable
    private transient ClassLoader pointcutClassLoader;

    @Nullable
    private transient PointcutExpression pointcutExpression;

    private transient boolean pointcutParsingFailed = false;

    private transient Map<Method, ShadowMatch> shadowMatchCache = new ConcurrentHashMap<>(32);

    /**
     * 创建一个新的默认的 AspectJExpressionPointcut。
     */
    public AspectJExpressionPointcut() {
    }

    /**
     * 使用给定的设置创建一个新的 AspectJExpressionPointcut。
     * @param declarationScope 切入点的声明范围
     * @param paramNames 切入点的参数名称
     * @param paramTypes 切入点的参数类型
     */
    public AspectJExpressionPointcut(Class<?> declarationScope, String[] paramNames, Class<?>[] paramTypes) {
        setPointcutDeclarationScope(declarationScope);
        if (paramNames.length != paramTypes.length) {
            throw new IllegalStateException("Number of pointcut parameter names must match number of pointcut parameter types");
        }
        this.pointcutParameterNames = paramNames;
        this.pointcutParameterTypes = paramTypes;
    }

    /**
     * 设置切入点声明的范围。
     */
    public void setPointcutDeclarationScope(Class<?> pointcutDeclarationScope) {
        this.pointcutDeclarationScope = pointcutDeclarationScope;
        this.aspectCompiledByAjc = compiledByAjc(pointcutDeclarationScope);
    }

    /**
     * 设置切点的参数名称。
     */
    public void setParameterNames(String... names) {
        this.pointcutParameterNames = names;
    }

    /**
     * 设置切点的参数类型。
     */
    public void setParameterTypes(Class<?>... types) {
        this.pointcutParameterTypes = types;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public ClassFilter getClassFilter() {
        checkExpression();
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        checkExpression();
        return this;
    }

    /**
     * 检查此切入点是否准备好进行匹配。
     */
    private void checkExpression() {
        if (getExpression() == null) {
            throw new IllegalStateException("Must set property 'expression' before attempting to match");
        }
    }

    /**
     * 懒惰地构建底层的 AspectJ 切入点表达式。
     */
    private PointcutExpression obtainPointcutExpression() {
        if (this.pointcutExpression == null) {
            this.pointcutClassLoader = determinePointcutClassLoader();
            this.pointcutExpression = buildPointcutExpression(this.pointcutClassLoader);
        }
        return this.pointcutExpression;
    }

    /**
     * 确定用于切点评估的类加载器。
     */
    @Nullable
    private ClassLoader determinePointcutClassLoader() {
        if (this.beanFactory instanceof ConfigurableBeanFactory cbf) {
            return cbf.getBeanClassLoader();
        }
        if (this.pointcutDeclarationScope != null) {
            return this.pointcutDeclarationScope.getClassLoader();
        }
        return ClassUtils.getDefaultClassLoader();
    }

    /**
     * 构建底层的 AspectJ 切点表达式。
     */
    private PointcutExpression buildPointcutExpression(@Nullable ClassLoader classLoader) {
        PointcutParser parser = initializePointcutParser(classLoader);
        PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
        for (int i = 0; i < pointcutParameters.length; i++) {
            pointcutParameters[i] = parser.createPointcutParameter(this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
        }
        return parser.parsePointcutExpression(replaceBooleanOperators(resolveExpression()), this.pointcutDeclarationScope, pointcutParameters);
    }

    private String resolveExpression() {
        String expression = getExpression();
        Assert.state(expression != null, "No expression set");
        return expression;
    }

    /**
     * 初始化底层的 AspectJ 切点解析器。
     */
    private PointcutParser initializePointcutParser(@Nullable ClassLoader classLoader) {
        PointcutParser parser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(SUPPORTED_PRIMITIVES, classLoader);
        parser.registerPointcutDesignatorHandler(new BeanPointcutDesignatorHandler());
        return parser;
    }

    /**
     * 如果在XML中指定了切点表达式，用户不能将"and"写成"&&"（尽管使用"{@code &amp;&amp;}"是可行的）。
     * <p>我们同样允许在两个切点子表达式之间使用"and"。
     * <p>此方法将转换回AspectJ切点解析器所需的"{@code &&}"。
     */
    private String replaceBooleanOperators(String pcExpr) {
        String result = StringUtils.replace(pcExpr, " and ", " && ");
        result = StringUtils.replace(result, " or ", " || ");
        result = StringUtils.replace(result, " not ", " ! ");
        return result;
    }

    /**
     * 返回底层的 AspectJ 切入点表达式。
     */
    public PointcutExpression getPointcutExpression() {
        return obtainPointcutExpression();
    }

    @Override
    public boolean matches(Class<?> targetClass) {
        if (this.pointcutParsingFailed) {
            // 点切（Pointcut）解析失败，在以下内容之前 -> 避免再次尝试。
            return false;
        }
        if (this.aspectCompiledByAjc && compiledByAjc(targetClass)) {
            // ajc 编译的方面类针对 ajc 编译的目标类 -> 已经织入。
            return false;
        }
        try {
            try {
                return obtainPointcutExpression().couldMatchJoinPointsInType(targetClass);
            } catch (ReflectionWorldException ex) {
                logger.debug("PointcutExpression matching rejected target class - trying fallback expression", ex);
                // 实际上，这仍然是一个“可能”的情况 - 如果我们目前信息不足，就将切入点视为动态的
                PointcutExpression fallbackExpression = getFallbackPointcutExpression(targetClass);
                if (fallbackExpression != null) {
                    return fallbackExpression.couldMatchJoinPointsInType(targetClass);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedPointcutPrimitiveException ex) {
            this.pointcutParsingFailed = true;
            if (logger.isDebugEnabled()) {
                logger.debug("Pointcut parser rejected expression [" + getExpression() + "]: " + ex);
            }
        } catch (Throwable ex) {
            logger.debug("PointcutExpression matching rejected target class", ex);
        }
        return false;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
        ShadowMatch shadowMatch = getTargetShadowMatch(method, targetClass);
        // 对此、目标、@this、@target、@annotation 进行特殊处理
        // 在 Spring 中 - 我们可以优化，因为我们确切知道我们恰好有这个类。
        // 并且运行时将永远不会存在匹配的子类。
        if (shadowMatch.alwaysMatches()) {
            return true;
        } else if (shadowMatch.neverMatches()) {
            return false;
        } else {
            // 可能是的情况
            if (hasIntroductions) {
                return true;
            }
            // 一个匹配测试返回可能为“-”，如果存在任何子类型敏感变量。
            // 涉及测试的元素（this, target, at_this, at_target, at_annotation）然后
            // 我们说这不是匹配，因为在Spring中永远不会有不同的
            // 运行时子类型。
            RuntimeTestWalker walker = getRuntimeTestWalker(shadowMatch);
            return (!walker.testsSubtypeSensitiveVars() || walker.testTargetInstanceOfResidue(targetClass));
        }
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return matches(method, targetClass, false);
    }

    @Override
    public boolean isRuntime() {
        return obtainPointcutExpression().mayNeedDynamicTest();
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        ShadowMatch shadowMatch = getTargetShadowMatch(method, targetClass);
        // 将 Spring AOP 代理绑定到 AspectJ 的 "this"，并将 Spring AOP 目标绑定到 AspectJ 目标。
        // 与 MethodInvocationProceedingJoinPoint 返回值一致
        ProxyMethodInvocation pmi = null;
        Object targetObject = null;
        Object thisObject = null;
        try {
            MethodInvocation curr = ExposeInvocationInterceptor.currentInvocation();
            targetObject = curr.getThis();
            if (!(curr instanceof ProxyMethodInvocation currPmi)) {
                throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + curr);
            }
            pmi = currPmi;
            thisObject = pmi.getProxy();
        } catch (IllegalStateException ex) {
            // 没有当前调用...
            if (logger.isDebugEnabled()) {
                logger.debug("Could not access current invocation - matching with limited context: " + ex);
            }
        }
        try {
            JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(thisObject, targetObject, args);
            /** 进行最终的检查，以查看是否有任何 this(TYPE) 类型的残留匹配。为此，我们使用原始方法（代理方法）的阴影来确保正确地检查了 'this'。如果没有这个检查，当 TYPE 与目标类型匹配但不是 'this' 时（如 JDK 动态代理的情况），我们将得到错误的匹配结果。
* 有关原始错误的详细信息，请参阅 SPR-2979。*/
            if (pmi != null && thisObject != null) {
                // 当前有一个调用正在进行
                RuntimeTestWalker originalMethodResidueTest = getRuntimeTestWalker(getShadowMatch(method, method));
                if (!originalMethodResidueTest.testThisInstanceOfResidue(thisObject.getClass())) {
                    return false;
                }
                if (joinPointMatch.matches()) {
                    bindParameters(pmi, joinPointMatch);
                }
            }
            return joinPointMatch.matches();
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to evaluate join point for arguments " + Arrays.toString(args) + " - falling back to non-match", ex);
            }
            return false;
        }
    }

    @Nullable
    protected String getCurrentProxiedBeanName() {
        return ProxyCreationContext.getCurrentProxiedBeanName();
    }

    /**
     * 根据目标类的加载器而非默认值获取一个新的切入点表达式。
     */
    @Nullable
    private PointcutExpression getFallbackPointcutExpression(Class<?> targetClass) {
        try {
            ClassLoader classLoader = targetClass.getClassLoader();
            if (classLoader != null && classLoader != this.pointcutClassLoader) {
                return buildPointcutExpression(classLoader);
            }
        } catch (Throwable ex) {
            logger.debug("Failed to create fallback PointcutExpression", ex);
        }
        return null;
    }

    private RuntimeTestWalker getRuntimeTestWalker(ShadowMatch shadowMatch) {
        if (shadowMatch instanceof DefensiveShadowMatch defensiveShadowMatch) {
            return new RuntimeTestWalker(defensiveShadowMatch.primary);
        }
        return new RuntimeTestWalker(shadowMatch);
    }

    private void bindParameters(ProxyMethodInvocation invocation, JoinPointMatch jpm) {
        // 注意：不能使用JoinPointMatch.getClass().getName()作为键，因为
        // Spring AOP 在连接点处进行所有匹配，然后对所有调用
        // 在这种情况下，如果我们仅仅使用JoinPointMatch作为键，那么
        // '最后一人胜出'，这根本不是我们想要的。
        // 使用该表达式是安全的，因为两个相同的表达式
        // 它们保证以完全相同的方式绑定。
        invocation.setUserAttribute(resolveExpression(), jpm);
    }

    private ShadowMatch getTargetShadowMatch(Method method, Class<?> targetClass) {
        Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        if (targetMethod.getDeclaringClass().isInterface() && targetMethod.getDeclaringClass() != targetClass && obtainPointcutExpression().getPointcutExpression().contains("." + targetMethod.getName() + "(")) {
            // 尝试构建尽可能具体的接口，以便继承的方法可以使用。
            // 也考虑用于子接口匹配，特别是对于代理类。
            // 注意：AspectJ 仅会考虑 Method.getDeclaringClass() 方法。
            Set<Class<?>> ifcs = ClassUtils.getAllInterfacesForClassAsSet(targetClass);
            if (ifcs.size() > 1) {
                try {
                    Class<?> compositeInterface = ClassUtils.createCompositeInterface(ClassUtils.toClassArray(ifcs), targetClass.getClassLoader());
                    targetMethod = ClassUtils.getMostSpecificMethod(targetMethod, compositeInterface);
                } catch (IllegalArgumentException ex) {
                    // 实现的接口可能暴露了冲突的方法签名...
                    // 继续执行原始目标方法。
                }
            }
        }
        return getShadowMatch(targetMethod, method);
    }

    private ShadowMatch getShadowMatch(Method targetMethod, Method originalMethod) {
        // 通过并发访问避免已知方法的锁竞争...
        ShadowMatch shadowMatch = this.shadowMatchCache.get(targetMethod);
        if (shadowMatch == null) {
            synchronized (this.shadowMatchCache) {
                // 未找到 - 现在使用完整锁再次检查...
                PointcutExpression fallbackExpression = null;
                shadowMatch = this.shadowMatchCache.get(targetMethod);
                if (shadowMatch == null) {
                    Method methodToMatch = targetMethod;
                    try {
                        try {
                            shadowMatch = obtainPointcutExpression().matchesMethodExecution(methodToMatch);
                        } catch (ReflectionWorldException ex) {
                            // 无法反射目标方法，可能是因为它已经被加载
                            // 在一个特殊的 ClassLoader 中。让我们尝试使用声明类（声明者）的 ClassLoader 吧...
                            try {
                                fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
                                if (fallbackExpression != null) {
                                    shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
                                }
                            } catch (ReflectionWorldException ex2) {
                                fallbackExpression = null;
                            }
                        }
                        if (targetMethod != originalMethod && (shadowMatch == null || (shadowMatch.neverMatches() && Proxy.isProxyClass(targetMethod.getDeclaringClass())))) {
                            // 在无解匹配或以下情况发生时，回退到原始的普通方法：
                            // 在代理类上发生负匹配（该代理类上没有任何注解）
                            // 重新声明的（方法）。
                            methodToMatch = originalMethod;
                            try {
                                shadowMatch = obtainPointcutExpression().matchesMethodExecution(methodToMatch);
                            } catch (ReflectionWorldException ex) {
                                // 无法对目标类或代理类进行反射
                                // 在我们放弃之前，让我们试试原始方法的声明类...
                                try {
                                    fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
                                    if (fallbackExpression != null) {
                                        shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
                                    }
                                } catch (ReflectionWorldException ex2) {
                                    fallbackExpression = null;
                                }
                            }
                        }
                    } catch (Throwable ex) {
                        // 可能 AspectJ 1.8.10 遇到了一个无效的签名
                        logger.debug("PointcutExpression matching rejected target method", ex);
                        fallbackExpression = null;
                    }
                    if (shadowMatch == null) {
                        shadowMatch = new ShadowMatchImpl(org.aspectj.util.FuzzyBoolean.NO, null, null, null);
                    } else if (shadowMatch.maybeMatches() && fallbackExpression != null) {
                        shadowMatch = new DefensiveShadowMatch(shadowMatch, fallbackExpression.matchesMethodExecution(methodToMatch));
                    }
                    this.shadowMatchCache.put(targetMethod, shadowMatch);
                }
            }
        }
        return shadowMatch;
    }

    private static boolean compiledByAjc(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().startsWith(AJC_MAGIC)) {
                return true;
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        return (superclass != null && compiledByAjc(superclass));
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof AspectJExpressionPointcut that && ObjectUtils.nullSafeEquals(getExpression(), that.getExpression()) && ObjectUtils.nullSafeEquals(this.pointcutDeclarationScope, that.pointcutDeclarationScope) && ObjectUtils.nullSafeEquals(this.pointcutParameterNames, that.pointcutParameterNames) && ObjectUtils.nullSafeEquals(this.pointcutParameterTypes, that.pointcutParameterTypes)));
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(getExpression());
        hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.pointcutDeclarationScope);
        hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.pointcutParameterNames);
        hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.pointcutParameterTypes);
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AspectJExpressionPointcut: (");
        for (int i = 0; i < this.pointcutParameterTypes.length; i++) {
            sb.append(this.pointcutParameterTypes[i].getName());
            sb.append(' ');
            sb.append(this.pointcutParameterNames[i]);
            if ((i + 1) < this.pointcutParameterTypes.length) {
                sb.append(", ");
            }
        }
        sb.append(") ");
        if (getExpression() != null) {
            sb.append(getExpression());
        } else {
            sb.append("<pointcut expression not set>");
        }
        return sb.toString();
    }

    // 由于您提供的代码注释内容是空的（只有"---------------------------------------------------------------------"），我无法进行翻译。请提供实际的 Java 代码注释内容，我将为您翻译成中文。
    // 序列化支持
    // 很抱歉，您只提供了一个英文单词 "---------------------------------------------------------------------"，这并不是有效的 Java 代码注释内容。如果您能提供具体的 Java 代码注释部分，我将很乐意为您翻译成中文。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 依赖于默认序列化，只需在反序列化后初始化状态。
        ois.defaultReadObject();
        // 初始化 transient 字段。
        // pointcutExpression 将通过 checkReadyToMatch() 方法懒加载初始化
        this.shadowMatchCache = new ConcurrentHashMap<>(32);
    }

    /**
     * Spring-specific {@code bean()}点切扩展的AspectJ处理器
     * <p>此处理器必须添加到需要处理{@code bean()}PCD的每个点切对象。匹配上下文通过检查线程局部变量自动获取，因此无需在点切上设置匹配上下文。
     */
    private class BeanPointcutDesignatorHandler implements PointcutDesignatorHandler {

        private static final String BEAN_DESIGNATOR_NAME = "bean";

        @Override
        public String getDesignatorName() {
            return BEAN_DESIGNATOR_NAME;
        }

        @Override
        public ContextBasedMatcher parse(String expression) {
            return new BeanContextMatcher(expression);
        }
    }

    /**
     * 用于BeanNamePointcutDesignatorHandler的匹配器类。
     * <p>对于此匹配器的动态匹配测试始终返回true，
     * 因为匹配决策是在代理创建时做出的。
     * 对于静态匹配测试，此匹配器会放弃匹配，即使使用了带有bean()切点的否定，
     * 也不会阻止整体切点的匹配。
     */
    private class BeanContextMatcher implements ContextBasedMatcher {

        private final NamePattern expressionPattern;

        public BeanContextMatcher(String expression) {
            this.expressionPattern = new NamePattern(expression);
        }

        @Override
        @SuppressWarnings("rawtypes")
        @Deprecated
        public boolean couldMatchJoinPointsInType(Class someClass) {
            return (contextMatch(someClass) == FuzzyBoolean.YES);
        }

        @Override
        @SuppressWarnings("rawtypes")
        @Deprecated
        public boolean couldMatchJoinPointsInType(Class someClass, MatchingContext context) {
            return (contextMatch(someClass) == FuzzyBoolean.YES);
        }

        @Override
        public boolean matchesDynamically(MatchingContext context) {
            return true;
        }

        @Override
        public FuzzyBoolean matchesStatically(MatchingContext context) {
            return contextMatch(null);
        }

        @Override
        public boolean mayNeedDynamicTest() {
            return false;
        }

        private FuzzyBoolean contextMatch(@Nullable Class<?> targetType) {
            String advisedBeanName = getCurrentProxiedBeanName();
            if (advisedBeanName == null) {
                // 当前没有正在进行的代理创建
                // 放弃；不能返回YES，因为这会导致带有否定点的切点失败
                return FuzzyBoolean.MAYBE;
            }
            if (BeanFactoryUtils.isGeneratedBeanName(advisedBeanName)) {
                return FuzzyBoolean.NO;
            }
            if (targetType != null) {
                boolean isFactory = FactoryBean.class.isAssignableFrom(targetType);
                return FuzzyBoolean.fromBoolean(matchesBean(isFactory ? BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName : advisedBeanName));
            } else {
                return FuzzyBoolean.fromBoolean(matchesBean(advisedBeanName) || matchesBean(BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName));
            }
        }

        private boolean matchesBean(String advisedBeanName) {
            return BeanFactoryAnnotationUtils.isQualifierMatch(this.expressionPattern::matches, advisedBeanName, beanFactory);
        }
    }

    private static class DefensiveShadowMatch implements ShadowMatch {

        private final ShadowMatch primary;

        private final ShadowMatch other;

        public DefensiveShadowMatch(ShadowMatch primary, ShadowMatch other) {
            this.primary = primary;
            this.other = other;
        }

        @Override
        public boolean alwaysMatches() {
            return this.primary.alwaysMatches();
        }

        @Override
        public boolean maybeMatches() {
            return this.primary.maybeMatches();
        }

        @Override
        public boolean neverMatches() {
            return this.primary.neverMatches();
        }

        @Override
        public JoinPointMatch matchesJoinPoint(Object thisObject, Object targetObject, Object[] args) {
            try {
                return this.primary.matchesJoinPoint(thisObject, targetObject, args);
            } catch (ReflectionWorldException ex) {
                return this.other.matchesJoinPoint(thisObject, targetObject, args);
            }
        }

        @Override
        public void setMatchingContext(MatchingContext aMatchContext) {
            this.primary.setMatchingContext(aMatchContext);
            this.other.setMatchingContext(aMatchContext);
        }
    }
}
