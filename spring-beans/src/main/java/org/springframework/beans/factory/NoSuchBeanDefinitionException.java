// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 当请求一个无法找到定义的Bean实例时抛出的异常。这可能是由于不存在Bean、非唯一的Bean或者没有关联Bean定义的手动注册的Singleton实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @see BeanFactory#getBean(String)
 * @see BeanFactory#getBean(Class)
 * @see NoUniqueBeanDefinitionException
 */
@SuppressWarnings("serial")
public class NoSuchBeanDefinitionException extends BeansException {

    @Nullable
    private final String beanName;

    @Nullable
    private final ResolvableType resolvableType;

    /**
     * 创建一个新的 {@code NoSuchBeanDefinitionException}。
     * @param name 缺失的 Bean 的名称
     */
    public NoSuchBeanDefinitionException(String name) {
        super("No bean named '" + name + "' available");
        this.beanName = name;
        this.resolvableType = null;
    }

    /**
     * 创建一个新的 {@code NoSuchBeanDefinitionException}。
     * @param name 缺失的 bean 的名称
     * @param message 详细描述问题的消息
     */
    public NoSuchBeanDefinitionException(String name, String message) {
        super("No bean named '" + name + "' available: " + message);
        this.beanName = name;
        this.resolvableType = null;
    }

    /**
     * 创建一个新的 {@code NoSuchBeanDefinitionException}。
     * @param type 缺失的 bean 所需的类型
     */
    public NoSuchBeanDefinitionException(Class<?> type) {
        this(ResolvableType.forClass(type));
    }

    /**
     * 创建一个新的 {@code NoSuchBeanDefinitionException}。
     * @param type 缺失 bean 所需的类型
     * @param message 描述问题的详细消息
     */
    public NoSuchBeanDefinitionException(Class<?> type, String message) {
        this(ResolvableType.forClass(type), message);
    }

    /**
     * 创建一个新的 {@code NoSuchBeanDefinitionException}。
     * @param type 缺失的 bean 的完整类型声明
     * @since 4.3.4
     */
    public NoSuchBeanDefinitionException(ResolvableType type) {
        super("No qualifying bean of type '" + type + "' available");
        this.beanName = null;
        this.resolvableType = type;
    }

    /**
     * 创建一个新的 {@code NoSuchBeanDefinitionException}。
     * @param type 缺失 bean 的完整类型声明
     * @param message 描述问题的详细消息
     * @since 4.3.4
     */
    public NoSuchBeanDefinitionException(ResolvableType type, String message) {
        super("No qualifying bean of type '" + type + "' available: " + message);
        this.beanName = null;
        this.resolvableType = type;
    }

    /**
     * 如果是失败的<em>按名称查找</em>，则返回缺失的bean的名称。
     */
    @Nullable
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 如果是类型查找失败，则返回缺少的 Bean 所需的类型。
     */
    @Nullable
    public Class<?> getBeanType() {
        return (this.resolvableType != null ? this.resolvableType.resolve() : null);
    }

    /**
     * 返回缺失的bean所需的{@link ResolvableType}，如果是一个失败的类型查找。
     * @since 4.3.4
     */
    @Nullable
    public ResolvableType getResolvableType() {
        return this.resolvableType;
    }

    /**
     * 返回当只期望找到一个匹配的Bean时找到的豆子数量。
     * 对于普通的NoSuchBeanDefinitionException，这个值始终为0。
     * @see NoUniqueBeanDefinitionException
     */
    public int getNumberOfBeansFound() {
        return 0;
    }
}
