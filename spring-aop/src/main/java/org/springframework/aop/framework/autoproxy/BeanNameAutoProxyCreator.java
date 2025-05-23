// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或原作者。

根据 Apache License 2.0（以下简称“许可协议”）授权；
除非符合许可协议，否则不得使用此文件。
您可以在以下地址获取许可协议的副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律规定或书面同意，否则在许可协议下分发的软件按“现状”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性还是其特定用途。
有关许可协议下管理权限和限制的特定语言，请参阅许可协议。*/
package org.springframework.aop.framework.autoproxy;

import java.util.ArrayList;
import java.util.List;
import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

/**
 *  自动代理创建器，通过名称列表识别要代理的Bean。
 *  检查直接匹配、"xxx*"和"*xxx"的匹配。
 *
 * <p>有关配置详细信息，请参阅父类AbstractAutoProxyCreator的javadoc。通常，您将通过"interceptorNames"属性指定要应用于所有识别到的Bean的拦截器名称列表。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see #setBeanNames
 * @see #isMatch
 * @see #setInterceptorNames
 * @see AbstractAutoProxyCreator
 */
@SuppressWarnings("serial")
public class BeanNameAutoProxyCreator extends AbstractAutoProxyCreator {

    private static final String[] NO_ALIASES = new String[0];

    @Nullable
    private List<String> beanNames;

    /**
     * 设置应自动使用代理包装的bean名称。
     * 名称可以指定一个以"*"结尾的匹配前缀，例如："myBean,tx*"将匹配名称为"myBean"的bean以及所有名称以"tx"开头的bean。
     * <p><b>注意：</b>在FactoryBean的情况下，只有FactoryBean创建的对象将被代理。这种默认行为自Spring 2.0起适用。
     * 如果你打算代理FactoryBean实例本身（这是一个罕见的使用场景，但Spring 1.2的默认行为是这样的），请指定包括factory-bean前缀"&"的FactoryBean的bean名称：例如："&myFactoryBean"。
     * @see org.springframework.beans.factory.FactoryBean
     * @see org.springframework.beans.factory.BeanFactory#FACTORY_BEAN_PREFIX
     */
    public void setBeanNames(String... beanNames) {
        Assert.notEmpty(beanNames, "'beanNames' must not be empty");
        this.beanNames = new ArrayList<>(beanNames.length);
        for (String mappedName : beanNames) {
            this.beanNames.add(mappedName.strip());
        }
    }

    /**
     * 如果 bean 名称匹配配置的支持名称列表中的名称之一，则委托给 {@link AbstractAutoProxyCreator#getCustomTargetSource(Class, String)}，否则返回 {@code null}。
     * @since 5.3
     * @see #setBeanNames(String...)
     */
    @Override
    protected TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
        return (isSupportedBeanName(beanClass, beanName) ? super.getCustomTargetSource(beanClass, beanName) : null);
    }

    /**
     * 将该对象识别为需要代理的bean，如果bean名称与配置的受支持名称列表中的任何一个名称匹配。
     * @see #setBeanNames(String...)
     */
    @Override
    @Nullable
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
        return (isSupportedBeanName(beanClass, beanName) ? PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY);
    }

    /**
     * 确定给定bean类的bean名称是否与配置的受支持名称列表中的任何一个名称匹配。
     * @param beanClass 要通知的bean的类
     * @param beanName bean的名称
     * @return 如果给定的bean名称受支持，则返回{@code true}
     * @see #setBeanNames(String...)
     */
    private boolean isSupportedBeanName(Class<?> beanClass, String beanName) {
        if (this.beanNames != null) {
            boolean isFactoryBean = FactoryBean.class.isAssignableFrom(beanClass);
            for (String mappedName : this.beanNames) {
                if (isFactoryBean) {
                    if (!mappedName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
                        continue;
                    }
                    mappedName = mappedName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
                }
                if (isMatch(beanName, mappedName)) {
                    return true;
                }
            }
            BeanFactory beanFactory = getBeanFactory();
            String[] aliases = (beanFactory != null ? beanFactory.getAliases(beanName) : NO_ALIASES);
            for (String alias : aliases) {
                for (String mappedName : this.beanNames) {
                    if (isMatch(alias, mappedName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 确定给定的bean名称是否与映射名称匹配。
     * <p>默认实现检查"xxx*"、"*xxx"和"*xxx*"的匹配，以及直接相等。可以在子类中重写。
     * @param beanName 要检查的bean名称
     * @param mappedName 配置的名称列表中的名称
     * @return 如果名称匹配则返回true
     * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
     */
    protected boolean isMatch(String beanName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, beanName);
    }
}
