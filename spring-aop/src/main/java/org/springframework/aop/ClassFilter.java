// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache许可证版本2.0（以下简称“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

/**
 * 过滤器，用于限制匹配点或引入仅针对给定目标类集。
 *
 * <p>可以作为 {@link Pointcut} 的一部分使用，也可以用于整个 {@link IntroductionAdvisor} 的目标定位。
 *
 * <p><strong>警告</strong>：此接口的具体实现必须提供适当的 {@link Object#equals(Object)}、
 * {@link Object#hashCode()} 和 {@link Object#toString()} 实现，以便在缓存场景中使用该过滤器——例如，
 * 在由 CGLIB 生成的代理中。截至 Spring Framework 6.0.13，必须生成一个与实现 {@code equals()} 逻辑一致的唯一字符串表示形式。
 * 请参阅框架中此接口的具体实现以获取示例。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @see Pointcut
 * @see MethodMatcher
 */
@FunctionalInterface
public interface ClassFilter {

    /**
     * 该切入点是否应用于给定的接口或目标类？
     * @param clazz 候选目标类
     * @return 是否应该将通知应用于给定的目标类
     */
    boolean matches(Class<?> clazz);

    /**
     * 全局实例的 {@code ClassFilter}，用于匹配所有类。
     */
    ClassFilter TRUE = TrueClassFilter.INSTANCE;
}
