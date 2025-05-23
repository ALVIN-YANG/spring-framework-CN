// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.MethodMatchers;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * AOP Alliance {@link org.aopalliance.aop.Advice} 类的基础类，用于包装 AspectJ 的切面或 AspectJ 注解的方法。
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractAspectJAdvice implements Advice, AspectJPrecedenceInformation, Serializable {

    /**
     * 用于ReflectiveMethodInvocation用户属性映射中当前连接点的键。
     */
    protected static final String JOIN_POINT_KEY = JoinPoint.class.getName();

    /**
     * 懒加载当前调用中的连接点。
     * 需要将 MethodInvocation 绑定到 ExposeInvocationInterceptor。
     * <p>如果当前 ReflectiveMethodInvocation 可用（在环绕通知中），则不要使用。
     * @return 当前 AspectJ 连接点，或者在不是 Spring AOP 调用的情况下抛出异常。
     */
    public static JoinPoint currentJoinPoint() {
        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        if (!(mi instanceof ProxyMethodInvocation pmi)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        JoinPoint jp = (JoinPoint) pmi.getUserAttribute(JOIN_POINT_KEY);
        if (jp == null) {
            jp = new MethodInvocationProceedingJoinPoint(pmi);
            pmi.setUserAttribute(JOIN_POINT_KEY, jp);
        }
        return jp;
    }

    private final Class<?> declaringClass;

    private final String methodName;

    private final Class<?>[] parameterTypes;

    protected transient Method aspectJAdviceMethod;

    private final AspectJExpressionPointcut pointcut;

    private final AspectInstanceFactory aspectInstanceFactory;

    /**
     * 该方面（引用bean）的名称，其中定义了此通知（在确定通知优先级时使用，以便我们可以确定两个通知是否来自同一方面）。
     */
    private String aspectName = "";

    /**
     * 该建议在此方面中的声明顺序。
     */
    private int declarationOrder;

    /**
     * 如果创建此建议对象的创建者知道参数名称并显式地设置它们，则此对象将不为null。
     */
    @Nullable
    private String[] argumentNames;

    /**
     * 非空，如果在抛出建议后绑定抛出的值。
     */
    @Nullable
    private String throwingName;

    /**
     * 非空，如果返回后通知绑定了返回值。
     */
    @Nullable
    private String returningName;

    private Class<?> discoveredReturningType = Object.class;

    private Class<?> discoveredThrowingType = Object.class;

    /**
     * 该JoinPoint参数的索引（目前仅支持在存在的情况下索引0）。
     */
    private int joinPointArgumentIndex = -1;

    /**
     * 该joinPointStaticPart参数的索引（目前仅在存在的情况下支持索引0）。
     */
    private int joinPointStaticPartArgumentIndex = -1;

    @Nullable
    private Map<String, Integer> argumentBindings;

    private boolean argumentsIntrospected = false;

    @Nullable
    private Type discoveredReturningGenericType;

    // 注意：与返回类型不同，抛出类型不需要此类泛型信息。
    // 由于 Java 不允许将异常类型进行参数化。
    /**
     * 为指定的建议方法创建一个新的 AbstractAspectJAdvice 对象。
     * @param aspectJAdviceMethod AspectJ 风格的建议方法
     * @param pointcut AspectJ 表达式切点
     * @param aspectInstanceFactory 生成切面实例的工厂
     */
    public AbstractAspectJAdvice(Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {
        Assert.notNull(aspectJAdviceMethod, "Advice method must not be null");
        this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
        this.methodName = aspectJAdviceMethod.getName();
        this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.pointcut = pointcut;
        this.aspectInstanceFactory = aspectInstanceFactory;
    }

    /**
     * 返回 AspectJ 风格的咨询方法。
     */
    public final Method getAspectJAdviceMethod() {
        return this.aspectJAdviceMethod;
    }

    /**
     * 返回 AspectJ 表达式切点。
     */
    public final AspectJExpressionPointcut getPointcut() {
        calculateArgumentBindings();
        return this.pointcut;
    }

    /**
     * 构建一个排除 AspectJ 通知方法本身的“安全”切入点。
     * @return 一个基于原始 AspectJ 表达式切入点的可组合切入点
     * @see #getPointcut()
     */
    public final Pointcut buildSafePointcut() {
        Pointcut pc = getPointcut();
        MethodMatcher safeMethodMatcher = MethodMatchers.intersection(new AdviceExcludingMethodMatcher(this.aspectJAdviceMethod), pc.getMethodMatcher());
        return new ComposablePointcut(pc.getClassFilter(), safeMethodMatcher);
    }

    /**
     * 返回方面实例的工厂。
     */
    public final AspectInstanceFactory getAspectInstanceFactory() {
        return this.aspectInstanceFactory;
    }

    /**
     * 返回方面实例的 ClassLoader。
     */
    @Nullable
    public final ClassLoader getAspectClassLoader() {
        return this.aspectInstanceFactory.getAspectClassLoader();
    }

    @Override
    public int getOrder() {
        return this.aspectInstanceFactory.getOrder();
    }

    /**
     * 设置声明通知（增强）的方面（Bean）的名称。
     */
    public void setAspectName(String name) {
        this.aspectName = name;
    }

    @Override
    public String getAspectName() {
        return this.aspectName;
    }

    /**
     * 设置该通知在此切面中的声明顺序。
     */
    public void setDeclarationOrder(int order) {
        this.declarationOrder = order;
    }

    @Override
    public int getDeclarationOrder() {
        return this.declarationOrder;
    }

    /**
     * 由该建议对象的创建者设置，如果已知道参数名称。
     * <p>这可能是因为它们已在XML中或在一个建议注解中明确指定。
     * @param argumentNames 以逗号分隔的参数名称列表
     */
    public void setArgumentNames(String argumentNames) {
        String[] tokens = StringUtils.commaDelimitedListToStringArray(argumentNames);
        setArgumentNamesFromStringArray(tokens);
    }

    /**
     * 由该建议对象的创建者设置，如果已知道参数名称。
     * <p>例如，这可能是因为它们已在XML中或在一个建议注解中明确指定。
     * @param argumentNames 参数名称列表
     */
    public void setArgumentNamesFromStringArray(String... argumentNames) {
        this.argumentNames = new String[argumentNames.length];
        for (int i = 0; i < argumentNames.length; i++) {
            this.argumentNames[i] = argumentNames[i].strip();
            if (!isVariableName(this.argumentNames[i])) {
                throw new IllegalArgumentException("'argumentNames' property of AbstractAspectJAdvice contains an argument name '" + this.argumentNames[i] + "' that is not a valid Java identifier");
            }
        }
        if (this.aspectJAdviceMethod.getParameterCount() == this.argumentNames.length + 1) {
            // 可能需要添加隐式连接点参数名称...
            Class<?> firstArgType = this.aspectJAdviceMethod.getParameterTypes()[0];
            if (firstArgType == JoinPoint.class || firstArgType == ProceedingJoinPoint.class || firstArgType == JoinPoint.StaticPart.class) {
                String[] oldNames = this.argumentNames;
                this.argumentNames = new String[oldNames.length + 1];
                this.argumentNames[0] = "THIS_JOIN_POINT";
                System.arraycopy(oldNames, 0, this.argumentNames, 1, oldNames.length);
            }
        }
    }

    public void setReturningName(String name) {
        throw new UnsupportedOperationException("Only afterReturning advice can be used to bind a return value");
    }

    /**
     * 我们需要在当前级别保留返回的名称，以便进行参数绑定计算，此方法允许afterReturning建议子类设置该名称。
     */
    protected void setReturningNameNoCheck(String name) {
        // 名称可以是变量或类型...
        if (isVariableName(name)) {
            this.returningName = name;
        } else {
            // 假设一个类型
            try {
                this.discoveredReturningType = ClassUtils.forName(name, getAspectClassLoader());
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Returning name '" + name + "' is neither a valid argument name nor the fully-qualified " + "name of a Java type on the classpath. Root cause: " + ex);
            }
        }
    }

    protected Class<?> getDiscoveredReturningType() {
        return this.discoveredReturningType;
    }

    @Nullable
    protected Type getDiscoveredReturningGenericType() {
        return this.discoveredReturningGenericType;
    }

    public void setThrowingName(String name) {
        throw new UnsupportedOperationException("Only afterThrowing advice can be used to bind a thrown exception");
    }

    /**
     * 我们需要在这个级别保存抛出者的名称，以便进行参数绑定计算，此方法允许在抛出后通知的子类设置该名称。
     */
    protected void setThrowingNameNoCheck(String name) {
        // 名称可以是一个变量或一个类型...
        if (isVariableName(name)) {
            this.throwingName = name;
        } else {
            // 假设一个类型
            try {
                this.discoveredThrowingType = ClassUtils.forName(name, getAspectClassLoader());
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Throwing name '" + name + "' is neither a valid argument name nor the fully-qualified " + "name of a Java type on the classpath. Root cause: " + ex);
            }
        }
    }

    protected Class<?> getDiscoveredThrowingType() {
        return this.discoveredThrowingType;
    }

    private static boolean isVariableName(String name) {
        return AspectJProxyUtils.isVariableName(name);
    }

    /**
     * 在设置过程中尽可能多地完成工作，以便后续的advice调用中的参数绑定可以尽可能快。
     * <p>如果第一个参数是类型为JoinPoint或ProceedingJoinPoint，则在该位置传递一个JoinPoint（对于around advice传递ProceedingJoinPoint）。
     * <p>如果第一个参数是类型为{@code JoinPoint.StaticPart}，则在该位置传递一个{@code JoinPoint.StaticPart}。
     * <p>剩余的参数必须通过在给定连接点上的pointcut评估来绑定。我们将得到一个从参数名称到值的映射。我们需要计算哪个advice参数需要绑定到哪个参数名称。有多个策略用于确定这种绑定，这些策略被组织成一个责任链（ChainOfResponsibility）。
     */
    public final void calculateArgumentBindings() {
        // 简单情况...没有绑定内容。
        if (this.argumentsIntrospected || this.parameterTypes.length == 0) {
            return;
        }
        int numUnboundArgs = this.parameterTypes.length;
        Class<?>[] parameterTypes = this.aspectJAdviceMethod.getParameterTypes();
        if (maybeBindJoinPoint(parameterTypes[0]) || maybeBindProceedingJoinPoint(parameterTypes[0]) || maybeBindJoinPointStaticPart(parameterTypes[0])) {
            numUnboundArgs--;
        }
        if (numUnboundArgs > 0) {
            // 需要按照从切入点匹配返回的名称绑定参数
            bindArgumentsByName(numUnboundArgs);
        }
        this.argumentsIntrospected = true;
    }

    private boolean maybeBindJoinPoint(Class<?> candidateParameterType) {
        if (JoinPoint.class == candidateParameterType) {
            this.joinPointArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    private boolean maybeBindProceedingJoinPoint(Class<?> candidateParameterType) {
        if (ProceedingJoinPoint.class == candidateParameterType) {
            if (!supportsProceedingJoinPoint()) {
                throw new IllegalArgumentException("ProceedingJoinPoint is only supported for around advice");
            }
            this.joinPointArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    protected boolean supportsProceedingJoinPoint() {
        return false;
    }

    private boolean maybeBindJoinPointStaticPart(Class<?> candidateParameterType) {
        if (JoinPoint.StaticPart.class == candidateParameterType) {
            this.joinPointStaticPartArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    private void bindArgumentsByName(int numArgumentsExpectingToBind) {
        if (this.argumentNames == null) {
            this.argumentNames = createParameterNameDiscoverer().getParameterNames(this.aspectJAdviceMethod);
        }
        if (this.argumentNames != null) {
            // 我们已经能够确定参数名称。
            bindExplicitArguments(numArgumentsExpectingToBind);
        } else {
            throw new IllegalStateException("Advice method [" + this.aspectJAdviceMethod.getName() + "] " + "requires " + numArgumentsExpectingToBind + " arguments to be bound by name, but " + "the argument names were not specified and could not be discovered.");
        }
    }

    /**
     * 创建一个用于参数绑定的 ParameterNameDiscoverer。
     * <p>默认实现创建一个 {@link DefaultParameterNameDiscoverer}，并添加一个特别配置的 {@link AspectJAdviceParameterNameDiscoverer}。
     */
    protected ParameterNameDiscoverer createParameterNameDiscoverer() {
        // 我们需要发现它们，如果那失败了，就猜测。
        // 并且如果我们不能以100%的准确性猜测，则失败。
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        AspectJAdviceParameterNameDiscoverer adviceParameterNameDiscoverer = new AspectJAdviceParameterNameDiscoverer(this.pointcut.getExpression());
        adviceParameterNameDiscoverer.setReturningName(this.returningName);
        adviceParameterNameDiscoverer.setThrowingName(this.throwingName);
        // 链中最后一个，所以如果我们被调用且失败，那将是不好的...
        adviceParameterNameDiscoverer.setRaiseExceptions(true);
        discoverer.addDiscoverer(adviceParameterNameDiscoverer);
        return discoverer;
    }

    private void bindExplicitArguments(int numArgumentsLeftToBind) {
        Assert.state(this.argumentNames != null, "No argument names available");
        this.argumentBindings = new HashMap<>();
        int numExpectedArgumentNames = this.aspectJAdviceMethod.getParameterCount();
        if (this.argumentNames.length != numExpectedArgumentNames) {
            throw new IllegalStateException("Expecting to find " + numExpectedArgumentNames + " arguments to bind by name in advice, but actually found " + this.argumentNames.length + " arguments.");
        }
        // 因此，我们在数量上进行匹配...
        int argumentIndexOffset = this.parameterTypes.length - numArgumentsLeftToBind;
        for (int i = argumentIndexOffset; i < this.argumentNames.length; i++) {
            this.argumentBindings.put(this.argumentNames[i], i);
        }
        // 检查返回和抛出是否包含在参数名称列表中。
        // 指定了，并查找发现的参数类型。
        if (this.returningName != null) {
            if (!this.argumentBindings.containsKey(this.returningName)) {
                throw new IllegalStateException("Returning argument name '" + this.returningName + "' was not bound in advice arguments");
            } else {
                Integer index = this.argumentBindings.get(this.returningName);
                this.discoveredReturningType = this.aspectJAdviceMethod.getParameterTypes()[index];
                this.discoveredReturningGenericType = this.aspectJAdviceMethod.getGenericParameterTypes()[index];
            }
        }
        if (this.throwingName != null) {
            if (!this.argumentBindings.containsKey(this.throwingName)) {
                throw new IllegalStateException("Throwing argument name '" + this.throwingName + "' was not bound in advice arguments");
            } else {
                Integer index = this.argumentBindings.get(this.throwingName);
                this.discoveredThrowingType = this.aspectJAdviceMethod.getParameterTypes()[index];
            }
        }
        // 相应地配置切入点表达式。
        configurePointcutParameters(this.argumentNames, argumentIndexOffset);
    }

    /**
     * 从 argumentIndexOffset 开始的所有参数都是切入点参数的候选者 - 但是返回值和抛出异常变量会被以不同的方式处理，并且如果存在于列表中，则必须从列表中移除。
     */
    private void configurePointcutParameters(String[] argumentNames, int argumentIndexOffset) {
        int numParametersToRemove = argumentIndexOffset;
        if (this.returningName != null) {
            numParametersToRemove++;
        }
        if (this.throwingName != null) {
            numParametersToRemove++;
        }
        String[] pointcutParameterNames = new String[argumentNames.length - numParametersToRemove];
        Class<?>[] pointcutParameterTypes = new Class<?>[pointcutParameterNames.length];
        Class<?>[] methodParameterTypes = this.aspectJAdviceMethod.getParameterTypes();
        int index = 0;
        for (int i = 0; i < argumentNames.length; i++) {
            if (i < argumentIndexOffset) {
                continue;
            }
            if (argumentNames[i].equals(this.returningName) || argumentNames[i].equals(this.throwingName)) {
                continue;
            }
            pointcutParameterNames[index] = argumentNames[i];
            pointcutParameterTypes[index] = methodParameterTypes[i];
            index++;
        }
        this.pointcut.setParameterNames(pointcutParameterNames);
        this.pointcut.setParameterTypes(pointcutParameterTypes);
    }

    /**
     * 在方法执行连接点获取参数，并将一组参数输出到通知方法中。
     * @param jp 当前连接点
     * @param jpMatch 与此执行连接点匹配的连接点匹配
     * @param returnValue 方法执行返回的值（可能为null）
     * @param ex 方法执行抛出的异常（可能为null）
     * @return 如果没有参数，则返回空数组
     */
    protected Object[] argBinding(JoinPoint jp, @Nullable JoinPointMatch jpMatch, @Nullable Object returnValue, @Nullable Throwable ex) {
        calculateArgumentBindings();
        // AMC 开始
        Object[] adviceInvocationArgs = new Object[this.parameterTypes.length];
        int numBound = 0;
        if (this.joinPointArgumentIndex != -1) {
            adviceInvocationArgs[this.joinPointArgumentIndex] = jp;
            numBound++;
        } else if (this.joinPointStaticPartArgumentIndex != -1) {
            adviceInvocationArgs[this.joinPointStaticPartArgumentIndex] = jp.getStaticPart();
            numBound++;
        }
        if (!CollectionUtils.isEmpty(this.argumentBindings)) {
            // 绑定到切点匹配
            if (jpMatch != null) {
                PointcutParameter[] parameterBindings = jpMatch.getParameterBindings();
                for (PointcutParameter parameter : parameterBindings) {
                    String name = parameter.getName();
                    Integer index = this.argumentBindings.get(name);
                    adviceInvocationArgs[index] = parameter.getBinding();
                    numBound++;
                }
            }
            // 绑定返回子句的绑定
            if (this.returningName != null) {
                Integer index = this.argumentBindings.get(this.returningName);
                adviceInvocationArgs[index] = returnValue;
                numBound++;
            }
            // 从抛出的异常中绑定
            if (this.throwingName != null) {
                Integer index = this.argumentBindings.get(this.throwingName);
                adviceInvocationArgs[index] = ex;
                numBound++;
            }
        }
        if (numBound != this.parameterTypes.length) {
            throw new IllegalStateException("Required to bind " + this.parameterTypes.length + " arguments, but only bound " + numBound + " (JoinPointMatch " + (jpMatch == null ? "was NOT" : "WAS") + " bound in invocation)");
        }
        return adviceInvocationArgs;
    }

    /**
     * 调用建议方法。
     * @param jpMatch 与此执行连接点匹配的JoinPointMatch
     * @param returnValue 方法执行返回的值（可能为null）
     * @param ex 方法执行抛出的异常（可能为null）
     * @return 调用结果
     * @throws Throwable 在调用失败的情况下抛出异常
     */
    protected Object invokeAdviceMethod(@Nullable JoinPointMatch jpMatch, @Nullable Object returnValue, @Nullable Throwable ex) throws Throwable {
        return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, ex));
    }

    // 如上所述，但在此情况下，我们已给出连接点。
    protected Object invokeAdviceMethod(JoinPoint jp, @Nullable JoinPointMatch jpMatch, @Nullable Object returnValue, @Nullable Throwable t) throws Throwable {
        return invokeAdviceMethodWithGivenArgs(argBinding(jp, jpMatch, returnValue, t));
    }

    protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
        Object[] actualArgs = args;
        if (this.aspectJAdviceMethod.getParameterCount() == 0) {
            actualArgs = null;
        }
        try {
            ReflectionUtils.makeAccessible(this.aspectJAdviceMethod);
            return this.aspectJAdviceMethod.invoke(this.aspectInstanceFactory.getAspectInstance(), actualArgs);
        } catch (IllegalArgumentException ex) {
            throw new AopInvocationException("Mismatch on arguments to advice method [" + this.aspectJAdviceMethod + "]; pointcut expression [" + this.pointcut.getPointcutExpression() + "]", ex);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    /**
     * 在环绕通知（around advice）中重写，以返回执行的连接点。
     */
    protected JoinPoint getJoinPoint() {
        return currentJoinPoint();
    }

    /**
     * 获取当前在我们要处理的连接点上的连接点匹配。
     */
    @Nullable
    protected JoinPointMatch getJoinPointMatch() {
        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        if (!(mi instanceof ProxyMethodInvocation pmi)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        return getJoinPointMatch(pmi);
    }

    // 注意：我们不能使用JoinPointMatch.getClass().getName()作为键，因为
    // Spring AOP 在连接点进行所有匹配，然后执行所有调用。
    // 在这种情况下，如果我们仅仅使用 JoinPointMatch 作为键，那么
    // '最后一人获胜'，这完全不是我们想要的。
    // 使用该表达式是安全的，因为两个相同的表达式
    // 确保将以完全相同的方式进行绑定。
    @Nullable
    protected JoinPointMatch getJoinPointMatch(ProxyMethodInvocation pmi) {
        String expression = this.pointcut.getExpression();
        return (expression != null ? (JoinPointMatch) pmi.getUserAttribute(expression) : null);
    }

    @Override
    public String toString() {
        return getClass().getName() + ": advice method [" + this.aspectJAdviceMethod + "]; " + "aspect name '" + this.aspectName + "'";
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        try {
            this.aspectJAdviceMethod = this.declaringClass.getMethod(this.methodName, this.parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Failed to find advice method on deserialization", ex);
        }
    }

    /**
     * 排除指定通知方法的匹配器。
     * @see AbstractAspectJAdvice#buildSafePointcut()
     */
    private static class AdviceExcludingMethodMatcher extends StaticMethodMatcher {

        private final Method adviceMethod;

        public AdviceExcludingMethodMatcher(Method adviceMethod) {
            this.adviceMethod = adviceMethod;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return !this.adviceMethod.equals(method);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof AdviceExcludingMethodMatcher that && this.adviceMethod.equals(that.adviceMethod)));
        }

        @Override
        public int hashCode() {
            return this.adviceMethod.hashCode();
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + this.adviceMethod;
        }
    }
}
