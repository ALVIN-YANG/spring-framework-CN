// 翻译完成 glm-4-flash
/*版权所有 2002-2024 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.ComposablePointcut;

/**
 * AspectJ aspect 类的元数据，包含用于 per 子句的额外 Spring AOP 切点。
 *
 * <p>使用 AspectJ 5 的 AJType 反射 API，使我们能够处理不同的 AspectJ 实例化模型，例如 "singleton"、"pertarget" 和 "perthis"。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
 */
@SuppressWarnings("serial")
public class AspectMetadata implements Serializable {

    /**
     * 此切面的名称定义为Spring（bean名称）-
     * 允许我们确定两条通知是否来自同一个切面，从而确定它们的相对优先级。
     */
    private final String aspectName;

    /**
     * 该方面类，存储在单独的地方，用于反序列化时重新解析相应的 AjType。
     */
    private final Class<?> aspectClass;

    /**
     * AspectJ 反射信息。
     * <p>在反序列化时重新解析，因为它自身不可序列化。
     */
    private transient AjType<?> ajType;

    /**
     * Spring AOP 的切点，对应于方面的 per 子句。如果是单例，将是对应于 {@code Pointcut.TRUE} 的规范实例，否则是一个 AspectJExpressionPointcut。
     */
    private final Pointcut perClausePointcut;

    /**
     * 为给定的方面类创建一个新的 AspectMetadata 实例。
     * @param aspectClass 方面类
     * @param aspectName 方面的名称
     */
    public AspectMetadata(Class<?> aspectClass, String aspectName) {
        this.aspectName = aspectName;
        Class<?> currClass = aspectClass;
        AjType<?> ajType = null;
        while (currClass != Object.class) {
            AjType<?> ajTypeToCheck = AjTypeSystem.getAjType(currClass);
            if (ajTypeToCheck.isAspect()) {
                ajType = ajTypeToCheck;
                break;
            }
            currClass = currClass.getSuperclass();
        }
        if (ajType == null) {
            throw new IllegalArgumentException("Class '" + aspectClass.getName() + "' is not an @AspectJ aspect");
        }
        if (ajType.getDeclarePrecedence().length > 0) {
            throw new IllegalArgumentException("DeclarePrecedence not presently supported in Spring AOP");
        }
        this.aspectClass = ajType.getJavaClass();
        this.ajType = ajType;
        switch(this.ajType.getPerClause().getKind()) {
            case SINGLETON ->
                {
                    this.perClausePointcut = Pointcut.TRUE;
                }
            case PERTARGET, PERTHIS ->
                {
                    AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
                    ajexp.setLocation(aspectClass.getName());
                    ajexp.setExpression(findPerClause(aspectClass));
                    ajexp.setPointcutDeclarationScope(aspectClass);
                    this.perClausePointcut = ajexp;
                }
            case PERTYPEWITHIN ->
                {
                    // 与类型模式协同工作
                    this.perClausePointcut = new ComposablePointcut(new TypePatternClassFilter(findPerClause(aspectClass)));
                }
            default ->
                throw new AopConfigException("PerClause " + ajType.getPerClause().getKind() + " not supported by Spring AOP for " + aspectClass);
        }
    }

    /**
     * 从形式为 {@code pertarget(contents)} 的字符串中提取内容。
     */
    private String findPerClause(Class<?> aspectClass) {
        Aspect ann = aspectClass.getAnnotation(Aspect.class);
        if (ann == null) {
            return "";
        }
        String value = ann.value();
        int beginIndex = value.indexOf('(');
        if (beginIndex < 0) {
            return "";
        }
        return value.substring(beginIndex + 1, value.length() - 1);
    }

    /**
     * 返回 AspectJ 反射信息。
     */
    public AjType<?> getAjType() {
        return this.ajType;
    }

    /**
     * 返回方面类。
     */
    public Class<?> getAspectClass() {
        return this.aspectClass;
    }

    /**
     * 返回方面名称。
     */
    public String getAspectName() {
        return this.aspectName;
    }

    /**
     * 返回一个用于单例方面的 Spring 切点表达式。
     * （例如，如果它是单例，则为 {@code Pointcut.TRUE}）。
     */
    public Pointcut getPerClausePointcut() {
        return this.perClausePointcut;
    }

    /**
     * 返回该方面是否定义为 "perthis" 或 "pertarget"。
     */
    public boolean isPerThisOrPerTarget() {
        PerClauseKind kind = getAjType().getPerClause().getKind();
        return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
    }

    /**
     * 返回该方面是否定义为“pertypewithin”。
     */
    public boolean isPerTypeWithin() {
        PerClauseKind kind = getAjType().getPerClause().getKind();
        return (kind == PerClauseKind.PERTYPEWITHIN);
    }

    /**
     * 返回该方面是否需要延迟实例化。
     */
    public boolean isLazilyInstantiated() {
        return (isPerThisOrPerTarget() || isPerTypeWithin());
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        this.ajType = AjTypeSystem.getAjType(this.aspectClass);
    }
}
