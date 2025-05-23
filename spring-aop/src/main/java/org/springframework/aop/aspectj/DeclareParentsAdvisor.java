// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”）许可；
除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用性和非侵权性。
有关许可的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.support.ClassFilters;
import org.springframework.aop.support.DelegatePerTargetObjectIntroductionInterceptor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * 介绍顾问委托给指定对象。
 * 实现了 AspectJ 注解风格的 behavior 对于 DeclareParents 注解。
 *
 * @author Rod Johnson
 * @author Ramnivas Laddad
 * @since 2.0
 */
public class DeclareParentsAdvisor implements IntroductionAdvisor {

    private final Advice advice;

    private final Class<?> introducedInterface;

    private final ClassFilter typePatternClassFilter;

    /**
     * 为此 DeclareParents 字段创建一个新的顾问。
     * @param interfaceType 定义引入的静态字段
     * @param typePattern 介绍受限于的类型模式
     * @param defaultImpl 默认实现类
     */
    public DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Class<?> defaultImpl) {
        this(interfaceType, typePattern, new DelegatePerTargetObjectIntroductionInterceptor(defaultImpl, interfaceType));
    }

    /**
     * 为此 DeclareParents 字段创建一个新的顾问。
     * @param interfaceType 定义引入的静态字段
     * @param typePattern 介绍受限于的类型模式
     * @param delegateRef 代表实现的对象引用
     */
    public DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Object delegateRef) {
        this(interfaceType, typePattern, new DelegatingIntroductionInterceptor(delegateRef));
    }

    /**
     * 私有构造函数，用于在基于实现（impl-based）的代理和基于引用（reference-based）的代理之间共享公共代码
     * （由于使用了final字段，不能使用如init()这样的方法来共享公共代码）。
     * @param interfaceType 定义引入的静态字段
     * @param typePattern 引入受限的类型模式
     * @param interceptor 作为{@link IntroductionInterceptor}的代理建议（interception advice）
     */
    private DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, IntroductionInterceptor interceptor) {
        this.advice = interceptor;
        this.introducedInterface = interfaceType;
        // 排除实现的方法。
        ClassFilter typePatternFilter = new TypePatternClassFilter(typePattern);
        ClassFilter exclusion = (clazz -> !this.introducedInterface.isAssignableFrom(clazz));
        this.typePatternClassFilter = ClassFilters.intersection(typePatternFilter, exclusion);
    }

    @Override
    public ClassFilter getClassFilter() {
        return this.typePatternClassFilter;
    }

    @Override
    public void validateInterfaces() throws IllegalArgumentException {
        // 不做任何事情
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public Class<?>[] getInterfaces() {
        return new Class<?>[] { this.introducedInterface };
    }
}
