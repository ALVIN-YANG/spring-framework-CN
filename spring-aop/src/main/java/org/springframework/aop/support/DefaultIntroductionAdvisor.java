// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据Apache License 2.0（以下简称“许可证”）许可；
* 您不得使用此文件除非遵守许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理许可和限制的语言。*/
package org.springframework.aop.support;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInfo;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 简单的 {@link org.springframework.aop.IntroductionAdvisor} 实现
 * 默认应用于任何类。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2003年11月11日
 */
@SuppressWarnings("serial")
public class DefaultIntroductionAdvisor implements IntroductionAdvisor, ClassFilter, Ordered, Serializable {

    private final Advice advice;

    private final Set<Class<?>> interfaces = new LinkedHashSet<>();

    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * 为给定的建议创建一个 DefaultIntroductionAdvisor。
     * @param advice 要应用的建议（可能实现 org.springframework.aop.IntroductionInfo 接口）
     * @see #addInterface
     */
    public DefaultIntroductionAdvisor(Advice advice) {
        this(advice, (advice instanceof IntroductionInfo introductionInfo ? introductionInfo : null));
    }

    /**
     * 为给定的建议创建一个 DefaultIntroductionAdvisor。
     * @param advice 要应用的建议
     * @param introductionInfo 描述要引入的接口的 IntroductionInfo（可能为null）
     */
    public DefaultIntroductionAdvisor(Advice advice, @Nullable IntroductionInfo introductionInfo) {
        Assert.notNull(advice, "Advice must not be null");
        this.advice = advice;
        if (introductionInfo != null) {
            Class<?>[] introducedInterfaces = introductionInfo.getInterfaces();
            if (introducedInterfaces.length == 0) {
                throw new IllegalArgumentException("IntroductionInfo defines no interfaces to introduce: " + introductionInfo);
            }
            for (Class<?> ifc : introducedInterfaces) {
                addInterface(ifc);
            }
        }
    }

    /**
     * 为给定的建议创建一个 DefaultIntroductionAdvisor。
     * @param advice 要应用的建议
     * @param ifc 要引入的接口
     */
    public DefaultIntroductionAdvisor(DynamicIntroductionAdvice advice, Class<?> ifc) {
        Assert.notNull(advice, "Advice must not be null");
        this.advice = advice;
        addInterface(ifc);
    }

    /**
     * 将指定的接口添加到要引入的接口列表中。
     * @param ifc 要引入的接口
     */
    public void addInterface(Class<?> ifc) {
        Assert.notNull(ifc, "Interface must not be null");
        if (!ifc.isInterface()) {
            throw new IllegalArgumentException("Specified class [" + ifc.getName() + "] must be an interface");
        }
        this.interfaces.add(ifc);
    }

    @Override
    public Class<?>[] getInterfaces() {
        return ClassUtils.toClassArray(this.interfaces);
    }

    @Override
    public void validateInterfaces() throws IllegalArgumentException {
        for (Class<?> ifc : this.interfaces) {
            if (this.advice instanceof DynamicIntroductionAdvice dynamicIntroductionAdvice && !dynamicIntroductionAdvice.implementsInterface(ifc)) {
                throw new IllegalArgumentException("DynamicIntroductionAdvice [" + this.advice + "] " + "does not implement interface [" + ifc.getName() + "] specified for introduction");
            }
        }
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof DefaultIntroductionAdvisor otherAdvisor && this.advice.equals(otherAdvisor.advice) && this.interfaces.equals(otherAdvisor.interfaces)));
    }

    @Override
    public int hashCode() {
        return this.advice.hashCode() * 13 + this.interfaces.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": advice [" + this.advice + "]; interfaces " + ClassUtils.classNamesToString(this.interfaces);
    }
}
