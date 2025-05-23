// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（以下简称“许可证”）许可；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可的具体语言管理权限和限制，请参阅许可证。*/
package org.springframework.aop.aspectj;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 *  这是一个实现`ParameterNameDiscoverer`的类，旨在从切点表达式、返回值和抛出子句中推断出通知方法的参数名称。如果无法获得明确的解释，则返回`null`。
 *
 * *算法概述**
 *
 * <p>如果可以推断出明确的绑定，则进行绑定。如果通知要求无法满足，则返回`null`。通过将`#setRaiseExceptions(boolean) raiseExceptions`属性设置为`true`，在无法发现参数名称的情况下，将抛出描述性异常而不是返回`null`。
 *
 * *算法详细说明**
 *
 * <p>此类按以下方式解释参数：
 * <ol>
 *   <li>如果方法的第一参数类型为`JoinPoint`或`ProceedingJoinPoint`，则假定它用于将`thisJoinPoint`传递给通知，并将参数名称分配为值`"thisJoinPoint"`。</li>
 *   <li>如果方法的第一参数类型为`JoinPoint.StaticPart`，则假定它用于将`"thisJoinPointStaticPart"`传递给通知，并将参数名称分配为值`"thisJoinPointStaticPart"`。</li>
 *   <li>如果已设置`#setThrowingName(String) throwingName`，并且没有未绑定的`Throwable+`类型参数，则抛出`IllegalArgumentException`。如果有多个未绑定的`Throwable+`类型参数，则抛出`AmbiguousBindingException`。如果恰好有一个未绑定的`Throwable+`类型参数，则将相应的参数名称分配为值`<throwingName>`。</li>
 *   <li>如果还有未绑定的参数，则检查切点表达式。设`a`为在绑定形式中使用的基于注解的切点表达式（`@annotation`、`@this`、`@target`、`@args`、`@within`、`@withincode`）的数量。绑定形式的使用本身需要推断：如果切点表达式内的表达式是一个符合Java变量命名约定的单个字符串字面量，则假定它是一个变量名称。如果`a`为零，则进行下一阶段。如果`a`大于1，则抛出`AmbiguousBindingException`。如果`a`等于1，并且没有未绑定的`Annotation+`类型参数，则抛出`IllegalArgumentException`。如果恰好有一个此类参数，则将相应的参数名称分配为从切点表达式中的值。</li>
 *   <li>如果已设置`returningName`，并且没有未绑定的参数，则抛出`IllegalArgumentException`。如果有多个未绑定的参数，则抛出`AmbiguousBindingException`。如果恰好有一个未绑定的参数，则将相应的参数名称分配为`returningName`的值。</li>
 *   <li>如果还有未绑定的参数，则再次检查切点表达式，以查找用于绑定形式的`this`、`target`和`args`切点表达式（绑定形式如注解基于切点所描述的方式推断）。如果仍有多个原始类型的未绑定的参数（只能在`args`中绑定），则抛出`AmbiguousBindingException`。如果恰好有一个原始类型的参数，则如果恰好找到一个`args`绑定的变量，我们将相应的参数名称分配为变量名称。如果没有找到`args`绑定的变量，则抛出`IllegalStateException`。如果有多个`args`绑定的变量，则抛出`AmbiguousBindingException`。在此阶段，如果仍有多个未绑定的参数，则抛出`AmbiguousBindingException`。如果没有未绑定的参数剩余，则完成。如果恰好有一个未绑定的参数剩余，并且只有一个未从`this`、`target`或`args`中解绑的候选变量名称，则将其分配为相应的参数名称。如果有多个可能性，则抛出`AmbiguousBindingException`。</li>
 * </ol>
 *
 * <p>抛出`IllegalArgumentException`或`AmbiguousBindingException`的行为是可配置的，以允许将此发现器用作责任链的一部分。默认情况下，将记录条件，并且`#getParameterNames(Method)`方法将简单地返回`null`。如果将`#setRaiseExceptions(boolean) raiseExceptions`属性设置为`true`，则条件将分别作为`IllegalArgumentException`和`AmbiguousBindingException`抛出。
 *
 * *作者**
 *
 *  Adrian Colyer
 *  Juergen Hoeller
 *
 * *自**
 *
 *  2.0
 */
public class AspectJAdviceParameterNameDiscoverer implements ParameterNameDiscoverer {

    private static final String THIS_JOIN_POINT = "thisJoinPoint";

    private static final String THIS_JOIN_POINT_STATIC_PART = "thisJoinPointStaticPart";

    // 绑定算法的步骤...
    private static final int STEP_JOIN_POINT_BINDING = 1;

    private static final int STEP_THROWING_BINDING = 2;

    private static final int STEP_ANNOTATION_BINDING = 3;

    private static final int STEP_RETURNING_BINDING = 4;

    private static final int STEP_PRIMITIVE_ARGS_BINDING = 5;

    private static final int STEP_THIS_TARGET_ARGS_BINDING = 6;

    private static final int STEP_REFERENCE_PCUT_BINDING = 7;

    private static final int STEP_FINISHED = 8;

    private static final Set<String> singleValuedAnnotationPcds = Set.of("@this", "@target", "@within", "@withincode", "@annotation");

    private static final Set<String> nonReferencePointcutTokens = new HashSet<>();

    static {
        Set<PointcutPrimitive> pointcutPrimitives = PointcutParser.getAllSupportedPointcutPrimitives();
        for (PointcutPrimitive primitive : pointcutPrimitives) {
            nonReferencePointcutTokens.add(primitive.getName());
        }
        nonReferencePointcutTokens.add("&&");
        nonReferencePointcutTokens.add("!");
        nonReferencePointcutTokens.add("||");
        nonReferencePointcutTokens.add("and");
        nonReferencePointcutTokens.add("or");
        nonReferencePointcutTokens.add("not");
    }

    /**
     * 与建议相关的切点表达式，作为一个简单的字符串。
     */
    @Nullable
    private final String pointcutExpression;

    private boolean raiseExceptions;

    /**
     * 如果建议是在afterReturning阶段，并且绑定了返回值，这个参数名称将被使用。
     */
    @Nullable
    private String returningName;

    /**
     * 如果建议是 afterThrowing，并且绑定抛出的值，则这是使用的参数名称。
     */
    @Nullable
    private String throwingName;

    private Class<?>[] argumentTypes = new Class<?>[0];

    private String[] parameterNameBindings = new String[0];

    private int numberOfRemainingUnboundArguments;

    /**
     * 创建一个新的发现者，尝试从给定的切入点表达式中发现参数名称。
     */
    public AspectJAdviceParameterNameDiscoverer(@Nullable String pointcutExpression) {
        this.pointcutExpression = pointcutExpression;
    }

    /**
     * 标识是否在无法推断出通知参数名称的情况下，必须适当地抛出 {@link IllegalArgumentException} 和 {@link AmbiguousBindingException}。
     * @param raiseExceptions 如果要抛出异常，则为 {@code true}。
     */
    public void setRaiseExceptions(boolean raiseExceptions) {
        this.raiseExceptions = raiseExceptions;
    }

    /**
     * 如果 {@code afterReturning} 建议绑定返回值，则必须指定返回变量的名称。
     * @param returningName 返回变量的名称
     */
    public void setReturningName(@Nullable String returningName) {
        this.returningName = returningName;
    }

    /**
     * 如果 {@code afterThrowing} 建议绑定了抛出的值，则必须指定 {@code throwing} 变量的名称。
     * @param throwingName 抛出变量的名称
     */
    public void setThrowingName(@Nullable String throwingName) {
        this.throwingName = throwingName;
    }

    /**
     * 推断建议方法的参数名称。
     * <p>有关该类所使用的算法的详细信息，请参阅此类的类级别javadoc。
     * @param method 目标 {@link Method}
     * @return 参数名称
     */
    @Override
    @Nullable
    public String[] getParameterNames(Method method) {
        this.argumentTypes = method.getParameterTypes();
        this.numberOfRemainingUnboundArguments = this.argumentTypes.length;
        this.parameterNameBindings = new String[this.numberOfRemainingUnboundArguments];
        int minimumNumberUnboundArgs = 0;
        if (this.returningName != null) {
            minimumNumberUnboundArgs++;
        }
        if (this.throwingName != null) {
            minimumNumberUnboundArgs++;
        }
        if (this.numberOfRemainingUnboundArguments < minimumNumberUnboundArgs) {
            throw new IllegalStateException("Not enough arguments in method to satisfy binding of returning and throwing variables");
        }
        try {
            int algorithmicStep = STEP_JOIN_POINT_BINDING;
            while ((this.numberOfRemainingUnboundArguments > 0) && algorithmicStep < STEP_FINISHED) {
                switch(algorithmicStep++) {
                    case STEP_JOIN_POINT_BINDING ->
                        {
                            if (!maybeBindThisJoinPoint()) {
                                maybeBindThisJoinPointStaticPart();
                            }
                        }
                    case STEP_THROWING_BINDING ->
                        maybeBindThrowingVariable();
                    case STEP_ANNOTATION_BINDING ->
                        maybeBindAnnotationsFromPointcutExpression();
                    case STEP_RETURNING_BINDING ->
                        maybeBindReturningVariable();
                    case STEP_PRIMITIVE_ARGS_BINDING ->
                        maybeBindPrimitiveArgsFromPointcutExpression();
                    case STEP_THIS_TARGET_ARGS_BINDING ->
                        maybeBindThisOrTargetOrArgsFromPointcutExpression();
                    case STEP_REFERENCE_PCUT_BINDING ->
                        maybeBindReferencePointcutParameter();
                    default ->
                        throw new IllegalStateException("Unknown algorithmic step: " + (algorithmicStep - 1));
                }
            }
        } catch (AmbiguousBindingException | IllegalArgumentException ex) {
            if (this.raiseExceptions) {
                throw ex;
            } else {
                return null;
            }
        }
        if (this.numberOfRemainingUnboundArguments == 0) {
            return this.parameterNameBindings;
        } else {
            if (this.raiseExceptions) {
                throw new IllegalStateException("Failed to bind all argument names: " + this.numberOfRemainingUnboundArguments + " argument(s) could not be bound");
            } else {
                // 失败时的约定是返回 null，以允许参与责任链
                return null;
            }
        }
    }

    /**
     * 在 Spring 中，建议方法永远不会是一个构造函数。
     * @return 返回 {@code null}
     * @throws UnsupportedOperationException 如果已经将 {@link #setRaiseExceptions(boolean) raiseExceptions} 设置为 {@code true}，则抛出此异常
     */
    @Override
    @Nullable
    public String[] getParameterNames(Constructor<?> ctor) {
        if (this.raiseExceptions) {
            throw new UnsupportedOperationException("An advice method can never be a constructor");
        } else {
            // 我们返回null而不是抛出异常，这样我们的行为会更好
            // 在责任链模式中。
            return null;
        }
    }

    private void bindParameterName(int index, @Nullable String name) {
        this.parameterNameBindings[index] = name;
        this.numberOfRemainingUnboundArguments--;
    }

    /**
     * 如果第一个参数是 JoinPoint 或 ProceedingJoinPoint 类型，则将 "thisJoinPoint" 作为参数名称绑定，并返回 true，否则返回 false。
     */
    private boolean maybeBindThisJoinPoint() {
        if ((this.argumentTypes[0] == JoinPoint.class) || (this.argumentTypes[0] == ProceedingJoinPoint.class)) {
            bindParameterName(0, THIS_JOIN_POINT);
            return true;
        } else {
            return false;
        }
    }

    private void maybeBindThisJoinPointStaticPart() {
        if (this.argumentTypes[0] == JoinPoint.StaticPart.class) {
            bindParameterName(0, THIS_JOIN_POINT_STATIC_PART);
        }
    }

    /**
     * 如果指定了抛出名称，并且恰好只剩下一个选项（是 Throwable 的子类型）的话，则将其绑定。
     */
    private void maybeBindThrowingVariable() {
        if (this.throwingName == null) {
            return;
        }
        // 因此，这里有绑定工作要做……
        int throwableIndex = -1;
        for (int i = 0; i < this.argumentTypes.length; i++) {
            if (isUnbound(i) && isSubtypeOf(Throwable.class, i)) {
                if (throwableIndex == -1) {
                    throwableIndex = i;
                } else {
                    // 第二个找到的候选者 - 不明确的绑定
                    throw new AmbiguousBindingException("Binding of throwing parameter '" + this.throwingName + "' is ambiguous: could be bound to argument " + throwableIndex + " or " + i);
                }
            }
        }
        if (throwableIndex == -1) {
            throw new IllegalStateException("Binding of throwing parameter '" + this.throwingName + "' could not be completed as no available arguments are a subtype of Throwable");
        } else {
            bindParameterName(throwableIndex, this.throwingName);
        }
    }

    /**
     * 如果指定了返回变量并且只剩下一个选择，则将其绑定。
     */
    private void maybeBindReturningVariable() {
        if (this.numberOfRemainingUnboundArguments == 0) {
            throw new IllegalStateException("Algorithm assumes that there must be at least one unbound parameter on entry to this method");
        }
        if (this.returningName != null) {
            if (this.numberOfRemainingUnboundArguments > 1) {
                throw new AmbiguousBindingException("Binding of returning parameter '" + this.returningName + "' is ambiguous: there are " + this.numberOfRemainingUnboundArguments + " candidates.");
            }
            // 一切准备就绪...找到未绑定的参数，并将其绑定。
            for (int i = 0; i < this.parameterNameBindings.length; i++) {
                if (this.parameterNameBindings[i] == null) {
                    bindParameterName(i, this.returningName);
                    break;
                }
            }
        }
    }

    /**
     * 解析字符串切点表达式，寻找以下内容：
     * &#64;this, &#64;target, &#64;args, &#64;within, &#64;withincode, &#64;annotation。
     * 如果找到这些切点表达式之一，尝试提取候选变量名（或者变量名，在args的情况下）。
     * <p>在这个练习中获得更多AspectJ的支持将会很棒... :)
     */
    private void maybeBindAnnotationsFromPointcutExpression() {
        List<String> varNames = new ArrayList<>();
        String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
        for (int i = 0; i < tokens.length; i++) {
            String toMatch = tokens[i];
            int firstParenIndex = toMatch.indexOf('(');
            if (firstParenIndex != -1) {
                toMatch = toMatch.substring(0, firstParenIndex);
            }
            if (singleValuedAnnotationPcds.contains(toMatch)) {
                PointcutBody body = getPointcutBody(tokens, i);
                i += body.numTokensConsumed;
                String varName = maybeExtractVariableName(body.text);
                if (varName != null) {
                    varNames.add(varName);
                }
            } else if (tokens[i].startsWith("@args(") || tokens[i].equals("@args")) {
                PointcutBody body = getPointcutBody(tokens, i);
                i += body.numTokensConsumed;
                maybeExtractVariableNamesFromArgs(body.text, varNames);
            }
        }
        bindAnnotationsFromVarNames(varNames);
    }

    /**
     * 将给定的提取变量名列表与参数槽位进行匹配。
     */
    private void bindAnnotationsFromVarNames(List<String> varNames) {
        if (!varNames.isEmpty()) {
            // 我们还有工作要做...
            int numAnnotationSlots = countNumberOfUnboundAnnotationArguments();
            if (numAnnotationSlots > 1) {
                throw new AmbiguousBindingException("Found " + varNames.size() + " potential annotation variable(s) and " + numAnnotationSlots + " potential argument slots");
            } else if (numAnnotationSlots == 1) {
                if (varNames.size() == 1) {
                    // 这是一个匹配
                    findAndBind(Annotation.class, varNames.get(0));
                } else {
                    // 多个候选变量，但只有一个槽位
                    throw new IllegalArgumentException("Found " + varNames.size() + " candidate annotation binding variables" + " but only one potential argument binding slot");
                }
            } else {
                // 没有槽位，所以假设那些候选变量实际上是类型名称
            }
        }
    }

    /**
     * 如果令牌以 Java 标识符约定开始，它就是有效的。
     */
    @Nullable
    private String maybeExtractVariableName(@Nullable String candidateToken) {
        if (AspectJProxyUtils.isVariableName(candidateToken)) {
            return candidateToken;
        }
        return null;
    }

    /**
     * 给定一个args切入点体（可以是{@code args}或{@code at_args}），将任何候选变量名添加到给定的列表中。
     */
    private void maybeExtractVariableNamesFromArgs(@Nullable String argsSpec, List<String> varNames) {
        if (argsSpec == null) {
            return;
        }
        String[] tokens = StringUtils.tokenizeToStringArray(argsSpec, ",");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].strip();
            String varName = maybeExtractVariableName(tokens[i]);
            if (varName != null) {
                varNames.add(varName);
            }
        }
    }

    /**
     * 解析字符串切点表达式，寻找this()、target()和args()表达式。
     * 如果找到其中之一，尝试提取一个候选变量名并将其绑定。
     */
    private void maybeBindThisOrTargetOrArgsFromPointcutExpression() {
        if (this.numberOfRemainingUnboundArguments > 1) {
            throw new AmbiguousBindingException("Still " + this.numberOfRemainingUnboundArguments + " unbound args at this()/target()/args() binding stage, with no way to determine between them");
        }
        List<String> varNames = new ArrayList<>();
        String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("this") || tokens[i].startsWith("this(") || tokens[i].equals("target") || tokens[i].startsWith("target(")) {
                PointcutBody body = getPointcutBody(tokens, i);
                i += body.numTokensConsumed;
                String varName = maybeExtractVariableName(body.text);
                if (varName != null) {
                    varNames.add(varName);
                }
            } else if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
                PointcutBody body = getPointcutBody(tokens, i);
                i += body.numTokensConsumed;
                List<String> candidateVarNames = new ArrayList<>();
                maybeExtractVariableNamesFromArgs(body.text, candidateVarNames);
                // 我们可能发现了在之前的原始参数绑定步骤中已经绑定的某些变量名。
                // 过滤掉它们...
                for (String varName : candidateVarNames) {
                    if (!alreadyBound(varName)) {
                        varNames.add(varName);
                    }
                }
            }
        }
        if (varNames.size() > 1) {
            throw new AmbiguousBindingException("Found " + varNames.size() + " candidate this(), target(), or args() variables but only one unbound argument slot");
        } else if (varNames.size() == 1) {
            for (int j = 0; j < this.parameterNameBindings.length; j++) {
                if (isUnbound(j)) {
                    bindParameterName(j, varNames.get(0));
                    break;
                }
            }
        }
        // else varNames.size 必须为 0，并且我们没有可以绑定的内容。
    }

    private void maybeBindReferencePointcutParameter() {
        if (this.numberOfRemainingUnboundArguments > 1) {
            throw new AmbiguousBindingException("Still " + this.numberOfRemainingUnboundArguments + " unbound args at reference pointcut binding stage, with no way to determine between them");
        }
        List<String> varNames = new ArrayList<>();
        String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
        for (int i = 0; i < tokens.length; i++) {
            String toMatch = tokens[i];
            if (toMatch.startsWith("!")) {
                toMatch = toMatch.substring(1);
            }
            int firstParenIndex = toMatch.indexOf('(');
            if (firstParenIndex != -1) {
                toMatch = toMatch.substring(0, firstParenIndex);
            } else {
                if (tokens.length < i + 2) {
                    // 没有括号以及其后的内容
                    continue;
                } else {
                    String nextToken = tokens[i + 1];
                    if (nextToken.charAt(0) != '(') {
                        // 下一个标记既不是 "(", 也不是一个 pc...
                        continue;
                    }
                }
            }
            // 消耗实体
            PointcutBody body = getPointcutBody(tokens, i);
            i += body.numTokensConsumed;
            if (!nonReferencePointcutTokens.contains(toMatch)) {
                // 然后，它可能是一个引用切点
                String varName = maybeExtractVariableName(body.text);
                if (varName != null) {
                    varNames.add(varName);
                }
            }
        }
        if (varNames.size() > 1) {
            throw new AmbiguousBindingException("Found " + varNames.size() + " candidate reference pointcut variables but only one unbound argument slot");
        } else if (varNames.size() == 1) {
            for (int j = 0; j < this.parameterNameBindings.length; j++) {
                if (isUnbound(j)) {
                    bindParameterName(j, varNames.get(0));
                    break;
                }
            }
        }
        // else 变量名大小必须为 0，并且没有可绑定内容。
    }

    /**
     * 我们在给定的索引位置找到了绑定切入点。现在我们需要提取切入点主体并返回它。
     */
    private PointcutBody getPointcutBody(String[] tokens, int startIndex) {
        int numTokensConsumed = 0;
        String currentToken = tokens[startIndex];
        int bodyStart = currentToken.indexOf('(');
        if (currentToken.charAt(currentToken.length() - 1) == ')') {
            // 这是一个全能的... 获取第一个（和最后一个）之间的文本
            return new PointcutBody(0, currentToken.substring(bodyStart + 1, currentToken.length() - 1));
        } else {
            StringBuilder sb = new StringBuilder();
            if (bodyStart >= 0 && bodyStart != (currentToken.length() - 1)) {
                sb.append(currentToken.substring(bodyStart + 1));
                sb.append(' ');
            }
            numTokensConsumed++;
            int currentIndex = startIndex + numTokensConsumed;
            while (currentIndex < tokens.length) {
                if (tokens[currentIndex].equals("(")) {
                    currentIndex++;
                    continue;
                }
                if (tokens[currentIndex].endsWith(")")) {
                    sb.append(tokens[currentIndex], 0, tokens[currentIndex].length() - 1);
                    return new PointcutBody(numTokensConsumed, sb.toString().trim());
                }
                String toAppend = tokens[currentIndex];
                if (toAppend.startsWith("(")) {
                    toAppend = toAppend.substring(1);
                }
                sb.append(toAppend);
                sb.append(' ');
                currentIndex++;
                numTokensConsumed++;
            }
        }
        // 我们尝试了，但失败了...
        return new PointcutBody(numTokensConsumed, null);
    }

    /**
     * 将参数与未绑定的基本类型参数进行匹配。
     */
    private void maybeBindPrimitiveArgsFromPointcutExpression() {
        int numUnboundPrimitives = countNumberOfUnboundPrimitiveArguments();
        if (numUnboundPrimitives > 1) {
            throw new AmbiguousBindingException("Found " + numUnboundPrimitives + " unbound primitive arguments with no way to distinguish between them.");
        }
        if (numUnboundPrimitives == 1) {
            // 查找 arg 变量，并在找到确切的单个实例时将其绑定。
            List<String> varNames = new ArrayList<>();
            String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
                    PointcutBody body = getPointcutBody(tokens, i);
                    i += body.numTokensConsumed;
                    maybeExtractVariableNamesFromArgs(body.text, varNames);
                }
            }
            if (varNames.size() > 1) {
                throw new AmbiguousBindingException("Found " + varNames.size() + " candidate variable names but only one candidate binding slot when matching primitive args");
            } else if (varNames.size() == 1) {
                // 1 原始参数，以及一个候选者...
                for (int i = 0; i < this.argumentTypes.length; i++) {
                    if (isUnbound(i) && this.argumentTypes[i].isPrimitive()) {
                        bindParameterName(i, varNames.get(0));
                        break;
                    }
                }
            }
        }
    }

    /** 如果给定参数索引的参数名称绑定尚未分配，则返回true。*/
    private boolean isUnbound(int i) {
        return this.parameterNameBindings[i] == null;
    }

    private boolean alreadyBound(String varName) {
        for (int i = 0; i < this.parameterNameBindings.length; i++) {
            if (!isUnbound(i) && varName.equals(this.parameterNameBindings[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 {@code true} 如果给定的参数类型是给定超类型的子类。
     */
    private boolean isSubtypeOf(Class<?> supertype, int argumentNumber) {
        return supertype.isAssignableFrom(this.argumentTypes[argumentNumber]);
    }

    private int countNumberOfUnboundAnnotationArguments() {
        int count = 0;
        for (int i = 0; i < this.argumentTypes.length; i++) {
            if (isUnbound(i) && isSubtypeOf(Annotation.class, i)) {
                count++;
            }
        }
        return count;
    }

    private int countNumberOfUnboundPrimitiveArguments() {
        int count = 0;
        for (int i = 0; i < this.argumentTypes.length; i++) {
            if (isUnbound(i) && this.argumentTypes[i].isPrimitive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 查找具有给定类型的参数索引，并将给定的
     * `varName` 绑定在该位置。
     */
    private void findAndBind(Class<?> argumentType, String varName) {
        for (int i = 0; i < this.argumentTypes.length; i++) {
            if (isUnbound(i) && isSubtypeOf(argumentType, i)) {
                bindParameterName(i, varName);
                return;
            }
        }
        throw new IllegalStateException("Expected to find an unbound argument of type '" + argumentType.getName() + "'");
    }

    /**
     * 简单的记录，用于存储从切入点体中提取的文本，以及提取该文本所消耗的标记数。
     */
    private record PointcutBody(int numTokensConsumed, @Nullable String text) {
    }

    /**
     * 在尝试解析方法参数名称时检测到模糊绑定时抛出。
     */
    @SuppressWarnings("serial")
    public static class AmbiguousBindingException extends RuntimeException {

        /**
         * 构造一个新的 AmbiguousBindingException，带有指定的消息。
         * @param msg 详细消息
         */
        public AmbiguousBindingException(String msg) {
            super(msg);
        }
    }
}
