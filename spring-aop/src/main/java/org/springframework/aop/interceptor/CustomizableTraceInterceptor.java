// 翻译完成 glm-4-flash
/*版权所有 2002-2021 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
有关许可的具体语言和限制，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.core.Constants;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 * `MethodInterceptor` 实现类，允许通过占位符进行高度可定制的类方法级追踪。
 *
 * <p>追踪信息在方法进入时写入，并在方法调用成功时在方法退出时写入。如果调用引发异常，则写入异常信息。这些追踪信息的内完全可定制，并提供了特殊占位符，允许您在日志消息中包含运行时信息。可用的占位符包括：
 *
 * <p><ul>
 * <li>`$[methodName]` - 替换为正在调用的方法名称</li>
 * <li>`$[targetClassName]` - 替换为调用目标类的名称</li>
 * <li>`$[targetClassShortName]` - 替换为调用目标类的短名称</li>
 * <li>`$[returnValue]` - 替换为调用返回的值</li>
 * <li>`$[argumentTypes]` - 替换为方法参数短类名称的逗号分隔列表</li>
 * <li>`$[arguments]` - 替换为方法参数的字符串表示的逗号分隔列表</li>
 * <li>`$[exception]` - 替换为调用期间抛出的任何 `Throwable` 的字符串表示</li>
 * <li>`$[invocationTime]` - 替换为方法调用所花费的时间，单位为毫秒</li>
 * </ul>
 *
 * <p>对哪些占位符可以在哪些消息中使用有限制：请参阅各个消息属性以获取有效占位符的详细信息。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setEnterMessage
 * @see #setExitMessage
 * @see #setExceptionMessage
 * @see SimpleTraceInterceptor
 */
@SuppressWarnings("serial")
public class CustomizableTraceInterceptor extends AbstractTraceInterceptor {

    /**
     * `$[methodName]` 占位符。
     * 被替换为正在调用的方法名称。
     */
    public static final String PLACEHOLDER_METHOD_NAME = "$[methodName]";

    /**
     * 占位符 ${targetClassName}。
     * 替换为方法调用目标的 ${Class} 的全限定名。
     */
    public static final String PLACEHOLDER_TARGET_CLASS_NAME = "$[targetClassName]";

    /**
     * `{@code $[targetClassShortName]}` 占位符。
     * 替换为方法调用目标的 `Class` 的短名称。
     */
    public static final String PLACEHOLDER_TARGET_CLASS_SHORT_NAME = "$[targetClassShortName]";

    /**
     * 占位符 ${returnValue}。
     * 由方法调用的返回值的字符串表示形式替换。
     */
    public static final String PLACEHOLDER_RETURN_VALUE = "$[returnValue]";

    /**
     * {@code $[argumentTypes]} 占位符。
     * 将其替换为方法调用参数类型的一个以逗号分隔的列表。参数类型以简短的类名表示。
     */
    public static final String PLACEHOLDER_ARGUMENT_TYPES = "$[argumentTypes]";

    /**
     * `{@code $[arguments]}`占位符。
     * 在方法调用中，替换为逗号分隔的参数值列表。依赖于每个参数类型的`toString()`方法。
     */
    public static final String PLACEHOLDER_ARGUMENTS = "$[arguments]";

    /**
     * `$[exception]` 占位符。
     * 在方法调用过程中引发的任何 `Throwable` 的 `String` 表示形式将被替换。
     */
    public static final String PLACEHOLDER_EXCEPTION = "$[exception]";

    /**
     * 占位符 `${invocationTime}`。
     * 被替换为调用所需的时间（以毫秒为单位）。
     */
    public static final String PLACEHOLDER_INVOCATION_TIME = "$[invocationTime]";

    /**
     * 用于写入方法进入消息的默认消息。
     */
    private static final String DEFAULT_ENTER_MESSAGE = "Entering method '" + PLACEHOLDER_METHOD_NAME + "' of class [" + PLACEHOLDER_TARGET_CLASS_NAME + "]";

    /**
     * 用于写入方法退出消息的默认消息。
     */
    private static final String DEFAULT_EXIT_MESSAGE = "Exiting method '" + PLACEHOLDER_METHOD_NAME + "' of class [" + PLACEHOLDER_TARGET_CLASS_NAME + "]";

    /**
     * 默认的消息，用于写入异常信息。
     */
    private static final String DEFAULT_EXCEPTION_MESSAGE = "Exception thrown in method '" + PLACEHOLDER_METHOD_NAME + "' of class [" + PLACEHOLDER_TARGET_CLASS_NAME + "]";

    /**
     * 用于匹配占位符的 {@code Pattern}。
     */
    private static final Pattern PATTERN = Pattern.compile("\\$\\[\\p{Alpha}+]");

    /**
     * 允许的占位符的 {@code Set}。
     */
    private static final Set<Object> ALLOWED_PLACEHOLDERS = new Constants(CustomizableTraceInterceptor.class).getValues("PLACEHOLDER_");

    /**
     * 方法进入的消息。
     */
    private String enterMessage = DEFAULT_ENTER_MESSAGE;

    /**
     * 方法退出的消息。
     */
    private String exitMessage = DEFAULT_EXIT_MESSAGE;

    /**
     * 方法执行期间异常的消息。
     */
    private String exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;

    /**
     * 设置用于方法进入日志消息的模板。
     * 此模板可以包含以下任何占位符：
     * <ul>
     * <li>{@code $[targetClassName]}</li>
     * <li>{@code $[targetClassShortName]}</li>
     * <li>{@code $[argumentTypes]}</li>
     * <li>{@code $[arguments]}</li>
     * </ul>
     */
    public void setEnterMessage(String enterMessage) throws IllegalArgumentException {
        Assert.hasText(enterMessage, "enterMessage must not be empty");
        checkForInvalidPlaceholders(enterMessage);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_RETURN_VALUE, "enterMessage cannot contain placeholder " + PLACEHOLDER_RETURN_VALUE);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_EXCEPTION, "enterMessage cannot contain placeholder " + PLACEHOLDER_EXCEPTION);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_INVOCATION_TIME, "enterMessage cannot contain placeholder " + PLACEHOLDER_INVOCATION_TIME);
        this.enterMessage = enterMessage;
    }

    /**
     * 设置用于方法退出日志消息的模板。
     * 此模板可以包含以下任意占位符：
     * <ul>
     * <li>{@code $[targetClassName]}</li>
     * <li>{@code $[targetClassShortName]}</li>
     * <li>{@code $[argumentTypes]}</li>
     * <li>{@code $[arguments]}</li>
     * <li>{@code $[returnValue]}</li>
     * <li>{@code $[invocationTime]}</li>
     * </ul>
     */
    public void setExitMessage(String exitMessage) {
        Assert.hasText(exitMessage, "exitMessage must not be empty");
        checkForInvalidPlaceholders(exitMessage);
        Assert.doesNotContain(exitMessage, PLACEHOLDER_EXCEPTION, "exitMessage cannot contain placeholder" + PLACEHOLDER_EXCEPTION);
        this.exitMessage = exitMessage;
    }

    /**
     * 设置用于方法异常日志消息的模板。
     * 此模板可以包含以下任何占位符：
     * <ul>
     * <li>{@code $[targetClassName]}</li>
     * <li>{@code $[targetClassShortName]}</li>
     * <li>{@code $[argumentTypes]}</li>
     * <li>{@code $[arguments]}</li>
     * <li>{@code $[exception]}</li>
     * </ul>
     */
    public void setExceptionMessage(String exceptionMessage) {
        Assert.hasText(exceptionMessage, "exceptionMessage must not be empty");
        checkForInvalidPlaceholders(exceptionMessage);
        Assert.doesNotContain(exceptionMessage, PLACEHOLDER_RETURN_VALUE, "exceptionMessage cannot contain placeholder " + PLACEHOLDER_RETURN_VALUE);
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * 在调用之前根据 {@code enterMessage} 的值写入日志消息。
     * 如果调用成功，则在退出时根据 {@code exitMessage} 的值写入日志消息。如果在调用过程中发生异常，则根据 {@code exceptionMessage} 的值写入消息。
     * @see #setEnterMessage
     * @see #setExitMessage
     * @see #setExceptionMessage
     */
    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
        String name = ClassUtils.getQualifiedMethodName(invocation.getMethod());
        StopWatch stopWatch = new StopWatch(name);
        Object returnValue = null;
        boolean exitThroughException = false;
        try {
            stopWatch.start(name);
            writeToLog(logger, replacePlaceholders(this.enterMessage, invocation, null, null, -1));
            returnValue = invocation.proceed();
            return returnValue;
        } catch (Throwable ex) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            exitThroughException = true;
            writeToLog(logger, replacePlaceholders(this.exceptionMessage, invocation, null, ex, stopWatch.getTotalTimeMillis()), ex);
            throw ex;
        } finally {
            if (!exitThroughException) {
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
                writeToLog(logger, replacePlaceholders(this.exitMessage, invocation, returnValue, null, stopWatch.getTotalTimeMillis()));
            }
        }
    }

    /**
     * 将给定消息中的占位符替换为提供的值，或从提供的值派生出的值。
     * @param message 包含要替换的占位符的消息模板
     * @param methodInvocation 正在被记录的 {@code MethodInvocation}。
     * 用于派生除 {@code $[exception]} 和 {@code $[returnValue]} 之外的所有占位符的值。
     * @param returnValue 调用返回的任何值。
     * 用于替换 {@code $[returnValue]} 占位符。可能为 {@code null}。
     * @param throwable 在调用期间引发的任何 {@code Throwable}。
     * 对于 {@code $[exception]} 占位符，替换为 {@code Throwable.toString()} 的值。可能为 {@code null}。
     * @param invocationTime 要写入替换 {@code $[invocationTime]} 占位符的值
     * @return 要写入日志的格式化输出
     */
    protected String replacePlaceholders(String message, MethodInvocation methodInvocation, @Nullable Object returnValue, @Nullable Throwable throwable, long invocationTime) {
        Matcher matcher = PATTERN.matcher(message);
        Object target = methodInvocation.getThis();
        Assert.state(target != null, "Target must not be null");
        StringBuilder output = new StringBuilder();
        while (matcher.find()) {
            String match = matcher.group();
            if (PLACEHOLDER_METHOD_NAME.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(methodInvocation.getMethod().getName()));
            } else if (PLACEHOLDER_TARGET_CLASS_NAME.equals(match)) {
                String className = getClassForLogging(target).getName();
                matcher.appendReplacement(output, Matcher.quoteReplacement(className));
            } else if (PLACEHOLDER_TARGET_CLASS_SHORT_NAME.equals(match)) {
                String shortName = ClassUtils.getShortName(getClassForLogging(target));
                matcher.appendReplacement(output, Matcher.quoteReplacement(shortName));
            } else if (PLACEHOLDER_ARGUMENTS.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(methodInvocation.getArguments())));
            } else if (PLACEHOLDER_ARGUMENT_TYPES.equals(match)) {
                appendArgumentTypes(methodInvocation, matcher, output);
            } else if (PLACEHOLDER_RETURN_VALUE.equals(match)) {
                appendReturnValue(methodInvocation, matcher, output, returnValue);
            } else if (throwable != null && PLACEHOLDER_EXCEPTION.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(throwable.toString()));
            } else if (PLACEHOLDER_INVOCATION_TIME.equals(match)) {
                matcher.appendReplacement(output, Long.toString(invocationTime));
            } else {
                // 这种情况不应该发生，因为占位符已经在之前进行了检查。
                throw new IllegalArgumentException("Unknown placeholder [" + match + "]");
            }
        }
        matcher.appendTail(output);
        return output.toString();
    }

    /**
     * 将方法返回值的 {@code String} 表示形式添加到提供的 {@code StringBuilder} 中。正确处理了 {@code null} 和 {@code void} 返回值。
     * @param methodInvocation 返回值的 {@code MethodInvocation}
     * @param matcher 包含匹配占位符的 {@code Matcher}
     * @param output 要写入输出的 {@code StringBuilder}
     * @param returnValue 方法调用返回的值
     */
    private void appendReturnValue(MethodInvocation methodInvocation, Matcher matcher, StringBuilder output, @Nullable Object returnValue) {
        if (methodInvocation.getMethod().getReturnType() == void.class) {
            matcher.appendReplacement(output, "void");
        } else if (returnValue == null) {
            matcher.appendReplacement(output, "null");
        } else {
            matcher.appendReplacement(output, Matcher.quoteReplacement(returnValue.toString()));
        }
    }

    /**
     * 将方法参数类型的简短类名以逗号分隔的形式添加到输出中。例如，如果一个方法的签名是 {@code put(java.lang.String, java.lang.Object)}，那么返回的值将是 {@code String, Object}。
     * @param methodInvocation 正在被记录的 {@code MethodInvocation}。
     * 参数将从相应的 {@code Method} 中检索。
     * @param matcher 包含输出状态的 {@code Matcher}
     * @param output 包含输出的 {@code StringBuilder}
     */
    private void appendArgumentTypes(MethodInvocation methodInvocation, Matcher matcher, StringBuilder output) {
        Class<?>[] argumentTypes = methodInvocation.getMethod().getParameterTypes();
        String[] argumentTypeShortNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypeShortNames.length; i++) {
            argumentTypeShortNames[i] = ClassUtils.getShortName(argumentTypes[i]);
        }
        matcher.appendReplacement(output, Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(argumentTypeShortNames)));
    }

    /**
     * 检查提供的 {@code String} 是否包含任何未在此类中指定为常量的占位符，如果包含则抛出 {@code IllegalArgumentException} 异常。
     */
    private void checkForInvalidPlaceholders(String message) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            String match = matcher.group();
            if (!ALLOWED_PLACEHOLDERS.contains(match)) {
                throw new IllegalArgumentException("Placeholder [" + match + "] is not valid");
            }
        }
    }
}
