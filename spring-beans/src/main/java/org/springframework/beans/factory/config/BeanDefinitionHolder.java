// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接处获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可协议了解具体语言管辖的权限和限制。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 用于存储具有名称和别名的BeanDefinition的持有者。
 * 可以注册为内部Bean的占位符。
 *
 * <p>还可以用于程序化注册内部Bean定义。如果您不关心BeanNameAware等，注册RootBeanDefinition或ChildBeanDefinition就足够了。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public class BeanDefinitionHolder implements BeanMetadataElement {

    private final BeanDefinition beanDefinition;

    private final String beanName;

    @Nullable
    private final String[] aliases;

    /**
     * 创建一个新的 BeanDefinitionHolder。
     * @param beanDefinition 要包装的 BeanDefinition
     * @param beanName 该 BeanDefinition 指定的 Bean 名称
     */
    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
        this(beanDefinition, beanName, null);
    }

    /**
     * 创建一个新的BeanDefinitionHolder。
     * @param beanDefinition 要包装的BeanDefinition
     * @param beanName bean的名称，如bean定义中指定
     * @param aliases bean的别名，或如果没有则为{@code null}
     */
    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, @Nullable String[] aliases) {
        Assert.notNull(beanDefinition, "BeanDefinition must not be null");
        Assert.notNull(beanName, "Bean name must not be null");
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.aliases = aliases;
    }

    /**
     * 复制构造函数：创建一个新的 BeanDefinitionHolder，其内容与给定的 BeanDefinitionHolder 实例相同。
     * <p>注意：包装的 BeanDefinition 引用按原样取用；它<em>不是</em>深拷贝。
     * @param beanDefinitionHolder 要复制的 BeanDefinitionHolder
     */
    public BeanDefinitionHolder(BeanDefinitionHolder beanDefinitionHolder) {
        Assert.notNull(beanDefinitionHolder, "BeanDefinitionHolder must not be null");
        this.beanDefinition = beanDefinitionHolder.getBeanDefinition();
        this.beanName = beanDefinitionHolder.getBeanName();
        this.aliases = beanDefinitionHolder.getAliases();
    }

    /**
     * 返回包装的 BeanDefinition。
     */
    public BeanDefinition getBeanDefinition() {
        return this.beanDefinition;
    }

    /**
     * 返回为该bean定义指定的bean的主名称。
     */
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 返回为该Bean指定的别名名称，这些名称是直接在Bean定义中指定的。
     * @return 返回别名名称数组，如果没有则返回{@code null}
     */
    @Nullable
    public String[] getAliases() {
        return this.aliases;
    }

    /**
     * 暴露 Bean 定义的源对象。
     * @see BeanDefinition#getSource()
     */
    @Override
    @Nullable
    public Object getSource() {
        return this.beanDefinition.getSource();
    }

    /**
     * 判断给定的候选名称是否与该Bean定义中的Bean名称或别名匹配
     */
    public boolean matchesName(@Nullable String candidateName) {
        return (candidateName != null && (candidateName.equals(this.beanName) || candidateName.equals(BeanFactoryUtils.transformedBeanName(this.beanName)) || ObjectUtils.containsElement(this.aliases, candidateName)));
    }

    /**
     * 返回关于该对象的友好、简洁描述，包括名称和别名。
     * @see #getBeanName()
     * @see #getAliases()
     */
    public String getShortDescription() {
        if (this.aliases == null) {
            return "Bean definition with name '" + this.beanName + "'";
        }
        return "Bean definition with name '" + this.beanName + "' and aliases [" + StringUtils.arrayToCommaDelimitedString(this.aliases) + ']';
    }

    /**
     * 返回关于该实体的详细描述，包括名称和别名，以及包含的 {@link BeanDefinition} 的描述。
     * @see #getShortDescription()
     * @see #getBeanDefinition()
     */
    public String getLongDescription() {
        return getShortDescription() + ": " + this.beanDefinition;
    }

    /**
     * 此实现返回长描述。可以被重写以返回简短描述或任何类型的自定义描述。
     * @see #getLongDescription()
     * @see #getShortDescription()
     */
    @Override
    public String toString() {
        return getLongDescription();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof BeanDefinitionHolder that && this.beanDefinition.equals(that.beanDefinition) && this.beanName.equals(that.beanName) && ObjectUtils.nullSafeEquals(this.aliases, that.aliases)));
    }

    @Override
    public int hashCode() {
        int hashCode = this.beanDefinition.hashCode();
        hashCode = 29 * hashCode + this.beanName.hashCode();
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
        return hashCode;
    }
}
