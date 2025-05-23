// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按照“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.util.Assert;

/**
 * 用于解析自动装配候选者的限定符。包含一个或多个此类限定符的 Bean 定义可以实现对待自动装配的字段或参数上注解的精细匹配。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.annotation.Qualifier
 */
@SuppressWarnings("serial")
public class AutowireCandidateQualifier extends BeanMetadataAttributeAccessor {

    /**
     * 存储值的键的名称。
     */
    public static final String VALUE_KEY = "value";

    private final String typeName;

    /**
     * 构建一个限定符以匹配给定类型的注解。
     * @param type 注解类型
     */
    public AutowireCandidateQualifier(Class<?> type) {
        this(type.getName());
    }

    /**
     * 构造一个限定符，用于匹配给定类型名称的注解。
     * 类型名称可以匹配注解的完全限定类名或简短类名（不带包名）。
     * @param typeName 注解类型的名称
     */
    public AutowireCandidateQualifier(String typeName) {
        Assert.notNull(typeName, "Type name must not be null");
        this.typeName = typeName;
    }

    /**
     * 构造一个限定符以匹配给定类型的注解，其中注解的 `value` 属性也匹配指定的值。
     * @param type 注解类型
     * @param value 要匹配的注解值
     */
    public AutowireCandidateQualifier(Class<?> type, Object value) {
        this(type.getName(), value);
    }

    /**
     * 构建一个限定符以匹配给定类型名称的注解，其中注解的 `value` 属性也匹配指定的值。
     * <p>类型名称可以匹配注解的完全限定类名或简短类名（不带包名）。
     * @param typeName 注解类型的名称
     * @param value 要匹配的注解值
     */
    public AutowireCandidateQualifier(String typeName, Object value) {
        Assert.notNull(typeName, "Type name must not be null");
        this.typeName = typeName;
        setAttribute(VALUE_KEY, value);
    }

    /**
     * 获取类型名称。此值将与构造函数中提供的类型名称相同，或者如果构造函数中提供了一个 Class 实例，则与完全限定的类名相同。
     */
    public String getTypeName() {
        return this.typeName;
    }
}
