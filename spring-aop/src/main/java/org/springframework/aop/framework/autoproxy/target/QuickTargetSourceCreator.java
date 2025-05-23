// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵权。
有关许可证的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.lang.Nullable;

/**
 * 使用bean名称前缀的便捷目标源创建器，用于创建三种知名目标源类型之一：
 * <ul>
 * <li>：CommonsPool2TargetSource</li>
 * <li>% ThreadLocalTargetSource</li>
 * <li>! PrototypeTargetSource</li>
 * </ul>
 *
 * @author Rod Johnson
 * @author Stephane Nicoll
 * @see org.springframework.aop.target.CommonsPool2TargetSource
 * @see org.springframework.aop.target.ThreadLocalTargetSource
 * @see org.springframework.aop.target.PrototypeTargetSource
 */
public class QuickTargetSourceCreator extends AbstractBeanFactoryBasedTargetSourceCreator {

    /**
     * CommonsPool2TargetSource 前缀。
     */
    public static final String PREFIX_COMMONS_POOL = ":";

    /**
     * ThreadLocalTargetSource的前缀。
     */
    public static final String PREFIX_THREAD_LOCAL = "%";

    /**
     * 原型目标源的前缀。
     */
    public static final String PREFIX_PROTOTYPE = "!";

    @Override
    @Nullable
    protected final AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(Class<?> beanClass, String beanName) {
        if (beanName.startsWith(PREFIX_COMMONS_POOL)) {
            CommonsPool2TargetSource cpts = new CommonsPool2TargetSource();
            cpts.setMaxSize(25);
            return cpts;
        } else if (beanName.startsWith(PREFIX_THREAD_LOCAL)) {
            return new ThreadLocalTargetSource();
        } else if (beanName.startsWith(PREFIX_PROTOTYPE)) {
            return new PrototypeTargetSource();
        } else {
            // 没有匹配。不要创建自定义目标源。
            return null;
        }
    }
}
