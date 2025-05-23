// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 抛出异常的情况是当一个Bean依赖于其他Bean或简单属性，
 * 这些属性在Bean工厂定义中未指定，尽管启用了依赖检查。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2003年9月3日
 */
@SuppressWarnings("serial")
public class UnsatisfiedDependencyException extends BeanCreationException {

    @Nullable
    private final InjectionPoint injectionPoint;

    /**
     * 创建一个新的 UnsatisfiedDependencyException。
     * @param resourceDescription 资源描述，该描述来自 bean 定义
     * @param beanName 请求的 bean 名称
     * @param propertyName 无法满足的 bean 属性名称
     * @param msg 详细消息
     */
    public UnsatisfiedDependencyException(@Nullable String resourceDescription, @Nullable String beanName, String propertyName, String msg) {
        super(resourceDescription, beanName, "Unsatisfied dependency expressed through bean property '" + propertyName + "'" + (StringUtils.hasLength(msg) ? ": " + msg : ""));
        this.injectionPoint = null;
    }

    /**
     * 创建一个新的 UnsatisfiedDependencyException。
     * @param resourceDescription 资源描述，指示 bean 定义来自的资源
     * @param beanName 请求的 bean 名称
     * @param propertyName 无法满足的 bean 属性的名称
     * @param ex 表示不满足依赖关系的 bean 创建异常
     */
    public UnsatisfiedDependencyException(@Nullable String resourceDescription, @Nullable String beanName, String propertyName, BeansException ex) {
        this(resourceDescription, beanName, propertyName, ex.getMessage());
        initCause(ex);
    }

    /**
     * 创建一个新的 UnsatisfiedDependencyException。
     * @param resourceDescription 资源描述，该描述来自 Bean 定义
     * @param beanName 请求的 Bean 名称
     * @param injectionPoint 注入点（字段或方法/构造函数参数）
     * @param msg 详细消息
     * @since 4.3
     */
    public UnsatisfiedDependencyException(@Nullable String resourceDescription, @Nullable String beanName, @Nullable InjectionPoint injectionPoint, String msg) {
        super(resourceDescription, beanName, "Unsatisfied dependency expressed through " + injectionPoint + (StringUtils.hasLength(msg) ? ": " + msg : ""));
        this.injectionPoint = injectionPoint;
    }

    /**
     * 创建一个新的 UnsatisfiedDependencyException 异常。
     * @param resourceDescription 描述了豆定义的资源来源
     * @param beanName 请求的豆名称
     * @param injectionPoint 注入点（字段或方法/构造函数参数）
     * @param ex 表明不满足依赖的豆创建异常
     * @since 4.3
     */
    public UnsatisfiedDependencyException(@Nullable String resourceDescription, @Nullable String beanName, @Nullable InjectionPoint injectionPoint, BeansException ex) {
        this(resourceDescription, beanName, injectionPoint, ex.getMessage());
        initCause(ex);
    }

    /**
     * 返回已知的注入点（字段或方法/构造函数参数）。
     * @since 4.3
     */
    @Nullable
    public InjectionPoint getInjectionPoint() {
        return this.injectionPoint;
    }
}
