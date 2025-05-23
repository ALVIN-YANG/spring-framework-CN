// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.support;

import java.io.Serializable;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 可变实现的 {@link SortDefinition} 接口。
 * 支持在设置相同的属性时切换升序值。
 *
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 26.05.2003
 * @see #setToggleAscendingOnProperty
 */
@SuppressWarnings("serial")
public class MutableSortDefinition implements SortDefinition, Serializable {

    private String property = "";

    private boolean ignoreCase = true;

    private boolean ascending = true;

    private boolean toggleAscendingOnProperty = false;

    /**
     * 创建一个空的 MutableSortDefinition，
     * 通过其Bean属性进行填充。
     * @see #setProperty
     * @see #setIgnoreCase
     * @see #setAscending
     */
    public MutableSortDefinition() {
    }

    /**
     * 复制构造函数：创建一个新的可变排序定义
     * 该定义与给定的排序定义相匹配。
     * @param source 原始排序定义
     */
    public MutableSortDefinition(SortDefinition source) {
        this.property = source.getProperty();
        this.ignoreCase = source.isIgnoreCase();
        this.ascending = source.isAscending();
    }

    /**
     * 根据给定的设置创建一个MutableSortDefinition。
     * @param property 比较的属性
     * @param ignoreCase 是否忽略字符串值中的大小写
     * @param ascending 是否按升序（true）或降序（false）排序
     */
    public MutableSortDefinition(String property, boolean ignoreCase, boolean ascending) {
        this.property = property;
        this.ignoreCase = ignoreCase;
        this.ascending = ascending;
    }

    /**
     * 创建一个新的可变排序定义。
     * @param toggleAscendingOnSameProperty 是否在相同属性再次设置时切换升序标志
     * （即，当使用已经设置的属性名称再次调用 {@code setProperty} 时）。
     */
    public MutableSortDefinition(boolean toggleAscendingOnSameProperty) {
        this.toggleAscendingOnProperty = toggleAscendingOnSameProperty;
    }

    /**
     * 设置用于比较的属性。
     * <p>如果该属性与当前属性相同，则如果启用了 "toggleAscendingOnProperty"，则排序会反转，否则将简单地忽略。
     * @see #setToggleAscendingOnProperty
     */
    public void setProperty(String property) {
        if (!StringUtils.hasLength(property)) {
            this.property = "";
        } else {
            // 隐式切换升序？
            if (isToggleAscendingOnProperty()) {
                this.ascending = (!property.equals(this.property) || !this.ascending);
            }
            this.property = property;
        }
    }

    @Override
    public String getProperty() {
        return this.property;
    }

    /**
     * 设置是否应忽略字符串值中的大小写。
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean isIgnoreCase() {
        return this.ignoreCase;
    }

    /**
     * 设置是否按升序（true）或降序（false）排序。
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public boolean isAscending() {
        return this.ascending;
    }

    /**
     * 设置是否在相同的属性再次被设置时切换升序标志
     * （即，再次调用带有已设置属性名称的 `setProperty` 方法）。
     * <p>这在通过Web请求进行参数绑定时特别有用，
     * 在这里，再次点击字段标题可能被认为是触发同一字段的重新排序，但顺序相反。
     */
    public void setToggleAscendingOnProperty(boolean toggleAscendingOnProperty) {
        this.toggleAscendingOnProperty = toggleAscendingOnProperty;
    }

    /**
     * 返回是否在相同的属性被再次设置时切换升序标志
     * 即，当再次调用带有已设置属性名的 `setProperty` 方法时。
     */
    public boolean isToggleAscendingOnProperty() {
        return this.toggleAscendingOnProperty;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof SortDefinition that && getProperty().equals(that.getProperty()) && isAscending() == that.isAscending() && isIgnoreCase() == that.isIgnoreCase()));
    }

    @Override
    public int hashCode() {
        int hashCode = getProperty().hashCode();
        hashCode = 29 * hashCode + (isIgnoreCase() ? 1 : 0);
        hashCode = 29 * hashCode + (isAscending() ? 1 : 0);
        return hashCode;
    }
}
