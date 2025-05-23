// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定权限和限制。*/
package org.springframework.beans;

import java.util.Map;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;

/**
 *  为可以访问命名属性的类提供的通用接口
 * （例如对象的bean属性或对象中的字段）。
 *
 *  <p>作为{@link BeanWrapper}的基接口。
 *
 *  @author Juergen Hoeller
 *  @since 1.1
 *  @see BeanWrapper
 *  @see PropertyAccessorFactory#forBeanPropertyAccess
 *  @see PropertyAccessorFactory#forDirectFieldAccess
 */
public interface PropertyAccessor {

    /**
     * 嵌套属性的路径分隔符。
     * 遵循正常的 Java 习惯：getFoo().getBar() 将表示为 "foo.bar"。
     */
    String NESTED_PROPERTY_SEPARATOR = ".";

    /**
     * 嵌套属性的路径分隔符。
     * 遵循正常的 Java 习惯：getFoo().getBar() 将表示为 "foo.bar"。
     */
    char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

    /**
     * 标记，表示索引或映射属性（如"person.addresses[0]"）的属性键的开始
     */
    String PROPERTY_KEY_PREFIX = "[";

    /**
     * 标记符，表示一个索引或映射属性（如 "person.addresses[0]"）属性键的开始
     */
    char PROPERTY_KEY_PREFIX_CHAR = '[';

    /**
     * 标记符，表示索引或映射属性（如 "person.addresses[0]"）的属性键的结束。
     */
    String PROPERTY_KEY_SUFFIX = "]";

    /**
     * 标记，表示索引或映射属性（如 "person.addresses[0]"）的属性键的结束。
     */
    char PROPERTY_KEY_SUFFIX_CHAR = ']';

    /**
     * 判断指定的属性是否可读。
     * <p>如果属性不存在，则返回 {@code false}。
     * @param propertyName 要检查的属性名
     * (可能是一个嵌套路径和/或一个索引/映射属性)
     * @return 属性是否可读
     */
    boolean isReadableProperty(String propertyName);

    /**
     * 判断指定的属性是否可写。
     * <p>如果属性不存在，则返回 {@code false}。
     * @param propertyName 要检查的属性名
     * （可能是一个嵌套路径和/或索引/映射属性）
     * @return 该属性是否可写
     */
    boolean isWritableProperty(String propertyName);

    /**
     * 确定指定属性的属性类型，
     * 或者检查属性描述符，或者在索引或映射元素的情况下检查值。
     * @param propertyName 要检查的属性
     * （可能是一个嵌套路径以及/或索引/映射属性）
     * @return 特定属性的属性类型，
     * 或者在不可确定的情况下返回 {@code null}
     * @throws PropertyAccessException 如果属性有效但访问器方法失败
     */
    @Nullable
    Class<?> getPropertyType(String propertyName) throws BeansException;

    /**
     * 返回指定属性的类型描述符：
     * 优先从读取方法获取，如果失败则回退到写入方法。
     * @param propertyName 要检查的属性名
     * （可能是一个嵌套路径和/或索引/映射属性）
     * @return 特定属性的属性类型，
     * 或者在无法确定时返回 {@code null}
     * @throws PropertyAccessException 如果属性有效但访问器方法失败
     */
    @Nullable
    TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException;

    /**
     * 获取指定属性的当前值。
     * @param propertyName 要获取值的属性的名称
     * （可能是一个嵌套路径以及/或者是一个索引/映射属性）
     * @return 属性的值
     * @throws InvalidPropertyException 如果没有这样的属性或
     * 如果属性不可读时
     * @throws PropertyAccessException 如果属性有效但访问器方法失败
     */
    @Nullable
    Object getPropertyValue(String propertyName) throws BeansException;

    /**
     * 将指定的值设置为当前属性值。
     * @param propertyName 要设置值的属性的名称
     * (可能是一个嵌套路径以及/或者一个索引/映射属性)
     * @param value 新值
     * @throws InvalidPropertyException 如果不存在该属性或者
     * 如果属性不可写时抛出
     * @throws PropertyAccessException 如果属性有效，但访问器方法失败或者发生类型不匹配时抛出
     */
    void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException;

    /**
     * 将指定的值设置为当前属性值。
     * @param pv 包含新属性值的对象
     * @throws InvalidPropertyException 如果不存在该属性或
     * 如果该属性不可写
     * @throws PropertyAccessException 如果属性有效但访问器方法失败或发生了类型不匹配
     */
    void setPropertyValue(PropertyValue pv) throws BeansException;

    /**
     * 从一个 Map 中执行批量更新。
     * <p>从 PropertyValues 中进行批量更新功能更强大：此方法提供是为了方便。行为将与 {@link #setPropertyValues(PropertyValues)} 方法的相同。
     * @param map 从中获取属性的 Map。包含属性值对象，键为属性名
     * @throws InvalidPropertyException 如果不存在这样的属性或者属性不可写
     * @throws PropertyBatchUpdateException 如果在批量更新过程中，对于特定属性发生了一个或多个 PropertyAccessExceptions。此异常包含了所有单个的 PropertyAccessExceptions。所有其他属性都将成功更新。
     */
    void setPropertyValues(Map<?, ?> map) throws BeansException;

    /**
     * 执行批量更新的首选方式。
     * <p>请注意，执行批量更新与执行单个更新不同，
     * 在执行此类的实现时，如果遇到一个可恢复的错误（例如类型不匹配，但<b>不是</b>无效字段名或类似错误），则会继续更新属性，
     * 抛出一个包含所有单个错误的 {@link PropertyBatchUpdateException}。
     * 可以稍后检查此异常以查看所有绑定错误。
     * 成功更新的属性将保持更改。
     * <p>不允许未知字段或无效字段。
     * @param pvs 要设置在目标对象上的属性值
     * @throws InvalidPropertyException 如果没有这样的属性或
     * 如果该属性不可写
     * @throws PropertyBatchUpdateException 如果在批量更新过程中发生了特定属性的 PropertyAccessExceptions。
     * 此异常包含所有单个 PropertyAccessExceptions。
     * 所有其他属性都将成功更新。
     * @see #setPropertyValues(PropertyValues, boolean, boolean)
     */
    void setPropertyValues(PropertyValues pvs) throws BeansException;

    /**
     * 执行具有更多行为控制的批量更新。
     * <p>注意，执行批量更新与执行单个更新不同，
     * 在执行此类的实现时，如果遇到一个<b>可恢复</b>的错误（例如类型不匹配，但<b>不是</b>无效的字段名或类似情况），则会继续更新属性，
     * 并抛出一个包含所有单个错误的{@link PropertyBatchUpdateException}异常。稍后可以检查此异常以查看所有绑定错误。
     * 成功更新的属性将保持更改。
     * @param pvs 要设置在目标对象上的属性值
     * @param ignoreUnknown 是否忽略未知属性（不在bean中找到）
     * @throws InvalidPropertyException 如果没有这样的属性或
     * 如果该属性不可写
     * @throws PropertyBatchUpdateException 如果在批量更新过程中发生了一个或多个PropertyAccessExceptions。
     * 此异常封装了所有单个PropertyAccessExceptions。所有其他属性都将成功更新。
     * @see #setPropertyValues(PropertyValues, boolean, boolean)
     */
    void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException;

    /**
     * 执行带有完全行为控制的批量更新。
     * <p>注意，执行批量更新与执行单个更新不同，
     * 因为此类实现的实例在遇到一个可恢复的错误（例如类型不匹配，但<b>不是</b>无效的字段名或类似错误）时将继续更新属性，
     * 抛出包含所有个别错误的 {@link PropertyBatchUpdateException}。
     * 可以稍后检查此异常以查看所有绑定错误。
     * 成功更新的属性将保持更改。
     * @param pvs 要设置在目标对象上的属性值
     * @param ignoreUnknown 是否忽略未知属性（在bean中未找到）
     * @param ignoreInvalid 是否忽略无效属性（找到但不可访问）
     * @throws InvalidPropertyException 如果没有这样的属性或
     * 如果属性不可写
     * @throws PropertyBatchUpdateException 如果在批量更新期间特定属性发生了一个或多个 PropertyAccessExceptions。
     * 此异常包含所有个别 PropertyAccessExceptions。所有其他属性都将成功更新。
     */
    void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) throws BeansException;
}
