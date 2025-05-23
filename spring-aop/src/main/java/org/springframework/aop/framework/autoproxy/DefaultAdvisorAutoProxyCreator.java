// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。

根据Apache License, Version 2.0（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的特定语言，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.lang.Nullable;

/**
 *  {@code BeanPostProcessor} 实现类，它基于当前 {@code BeanFactory} 中所有候选的 {@code Advisor} 创建 AOP 代理。此类完全通用；它不包含处理特定方面的特殊代码，例如处理池化方面。
 *
 * <p>可以通过设置 {@code usePrefix} 属性为 true 来过滤掉顾问 - 例如，在同一个工厂中使用多个此类后处理器 - 在这种情况下，只有以 DefaultAdvisorAutoProxyCreator 的 bean 名称后跟一个点（如 "aapc."）开头的顾问将被使用。可以通过设置 {@code advisorBeanNamePrefix} 属性来改变这个默认前缀。在这种情况下，也将使用分隔符（.）。
 *
 * @作者 Rod Johnson
 * @作者 Rob Harrop
 */
@SuppressWarnings("serial")
public class DefaultAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator implements BeanNameAware {

    /**
     * 前缀与 Bean 名称剩余部分之间的分隔符。
     */
    public static final String SEPARATOR = ".";

    private boolean usePrefix = false;

    @Nullable
    private String advisorBeanNamePrefix;

    /**
     * 设置是否仅包括具有特定前缀的顾问类（Advisor）在Bean名称中。
     * <p>默认值为{@code false}，包括所有类型的{@code Advisor} Bean。
     * @see #setAdvisorBeanNamePrefix
     */
    public void setUsePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
    }

    /**
     * 返回是否仅包含具有特定前缀的bean名称的顾问。
     */
    public boolean isUsePrefix() {
        return this.usePrefix;
    }

    /**
     * 设置用于自动代理的 bean 名称前缀。此前缀应设置以避免循环引用。默认值为该对象 bean 名称加一个点。
     * @param advisorBeanNamePrefix 排除前缀
     */
    public void setAdvisorBeanNamePrefix(@Nullable String advisorBeanNamePrefix) {
        this.advisorBeanNamePrefix = advisorBeanNamePrefix;
    }

    /**
     * 返回将导致它们被包含以供此对象自动代理的bean名称前缀。
     */
    @Nullable
    public String getAdvisorBeanNamePrefix() {
        return this.advisorBeanNamePrefix;
    }

    @Override
    public void setBeanName(String name) {
        // 如果尚未设置基础设施bean名称前缀，则覆盖它。
        if (this.advisorBeanNamePrefix == null) {
            this.advisorBeanNamePrefix = name + SEPARATOR;
        }
    }

    /**
     * 如果激活，考虑具有指定前缀的 {@code Advisor} 实例为有效。
     * @see #setUsePrefix
     * @see #setAdvisorBeanNamePrefix
     */
    @Override
    protected boolean isEligibleAdvisorBean(String beanName) {
        if (!isUsePrefix()) {
            return true;
        }
        String prefix = getAdvisorBeanNamePrefix();
        return (prefix != null && beanName.startsWith(prefix));
    }
}
