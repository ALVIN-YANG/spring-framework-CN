// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"）进行许可；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式，无论是明示的还是暗示的，
* 保证或条件。请参阅许可证了解管理许可权和限制的具体语言。*/
package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import org.springframework.core.Ordered;
import org.springframework.core.SpringProperties;
import org.springframework.lang.NonNull;

/**
 * {@link BeanInfoFactory} 的实现，执行标准的
 * {@link java.beans.Introspector} 检查。
 *
 * <p>通过以下内容的 {@code META-INF/spring.factories} 文件进行配置：
 * {@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.StandardBeanInfoFactory}
 *
 * <p>优先级设置为 {@link Ordered#LOWEST_PRECEDENCE}，以便允许其他用户定义的
 * {@link BeanInfoFactory} 类型具有更高的优先级。
 *
 * @author Juergen Hoeller
 * @since 6.0
 * @see ExtendedBeanInfoFactory
 * @see CachedIntrospectionResults
 * @see Introspector#getBeanInfo(Class)
 */
public class StandardBeanInfoFactory implements BeanInfoFactory, Ordered {

    /**
     * 系统属性，指示 Spring 在调用 JavaBeans 的 {@link Introspector} 时使用 {@link Introspector#IGNORE_ALL_BEANINFO}
     * 模式："spring.beaninfo.ignore"，值为 "true" 时将跳过对 {@code BeanInfo} 类的搜索（通常用于没有为应用中的 beans 定义此类类的场景）。
     * <p>默认值为 "false"，考虑所有 {@code BeanInfo} 元数据类，如标准 {@link Introspector#getBeanInfo(Class)} 调用。如果您在启动或懒加载期间遇到对不存在的 {@code BeanInfo} 类的重复 ClassLoader 访问，请考虑将此标志切换为 "true"。
     * <p>请注意，这种效果也可能表明缓存没有有效工作的场景：优先考虑一种安排，其中 Spring jar 与应用类位于同一 ClassLoader 中，这样在任何情况下都可以实现干净的缓存。对于 Web 应用，如果存在多 ClassLoader 布局，请考虑在 web.xml 中声明一个本地的 {@link org.springframework.web.util.IntrospectorCleanupListener}，这样也可以实现有效的缓存。
     * @see Introspector#getBeanInfo(Class, int)
     */
    public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";

    private static final boolean shouldIntrospectorIgnoreBeaninfoClasses = SpringProperties.getFlag(IGNORE_BEANINFO_PROPERTY_NAME);

    @Override
    @NonNull
    public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
        BeanInfo beanInfo = (shouldIntrospectorIgnoreBeaninfoClasses ? Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO) : Introspector.getBeanInfo(beanClass));
        // 立即从Introspector缓存中移除类，以允许正确地进行垃圾回收
        // 在类加载器关闭时进行集合操作；我们在CachedIntrospectionResults中缓存它
        // 以垃圾回收（GC）友好的方式。这是（再次）为了 JDK ClassInfo 缓存所必需的。
        Class<?> classToFlush = beanClass;
        do {
            Introspector.flushFromCaches(classToFlush);
            classToFlush = classToFlush.getSuperclass();
        } while (classToFlush != null && classToFlush != Object.class);
        return beanInfo;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
