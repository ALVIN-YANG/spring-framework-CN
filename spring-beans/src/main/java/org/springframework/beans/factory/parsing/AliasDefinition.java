// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于对适销性、特定用途的适用性或不侵犯他人权利的保证。
* 请参阅许可证以获取具体规定许可权和限制的内容。*/
package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 表示在解析过程中已注册的别名。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ReaderEventListener#aliasRegistered(AliasDefinition)
 */
public class AliasDefinition implements BeanMetadataElement {

    private final String beanName;

    private final String alias;

    @Nullable
    private final Object source;

    /**
     * 创建一个新的AliasDefinition。
     * @param beanName bean的规范名称
     * @param alias 为bean注册的别名
     */
    public AliasDefinition(String beanName, String alias) {
        this(beanName, alias, null);
    }

    /**
     * 创建一个新的 AliasDefinition。
     * @param beanName Bean的规范名称
     * @param alias 为Bean注册的别名
     * @param source 源对象（可能为{@code null}）
     */
    public AliasDefinition(String beanName, String alias, @Nullable Object source) {
        Assert.notNull(beanName, "Bean name must not be null");
        Assert.notNull(alias, "Alias must not be null");
        this.beanName = beanName;
        this.alias = alias;
        this.source = source;
    }

    /**
     * 返回该bean的规范名称。
     */
    public final String getBeanName() {
        return this.beanName;
    }

    /**
     * 返回注册的 Bean 的别名。
     */
    public final String getAlias() {
        return this.alias;
    }

    @Override
    @Nullable
    public final Object getSource() {
        return this.source;
    }
}
