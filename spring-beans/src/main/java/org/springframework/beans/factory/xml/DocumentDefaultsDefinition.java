// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途的适用性。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.lang.Nullable;

/**
 * 简单的 JavaBean，用于存储在标准 Spring XML bean 定义文档中在 `<beans>` 级别指定的默认值：
 * 例如：`default-lazy-init`、`default-autowire` 等。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 */
public class DocumentDefaultsDefinition implements DefaultsDefinition {

    @Nullable
    private String lazyInit;

    @Nullable
    private String merge;

    @Nullable
    private String autowire;

    @Nullable
    private String autowireCandidates;

    @Nullable
    private String initMethod;

    @Nullable
    private String destroyMethod;

    @Nullable
    private Object source;

    /**
     * 设置当前解析的文档的默认懒加载标志。
     */
    public void setLazyInit(@Nullable String lazyInit) {
        this.lazyInit = lazyInit;
    }

    /**
     * 返回当前解析的文档的默认懒加载标志。
     */
    @Nullable
    public String getLazyInit() {
        return this.lazyInit;
    }

    /**
     * 设置当前解析的文档的默认合并设置。
     */
    public void setMerge(@Nullable String merge) {
        this.merge = merge;
    }

    /**
     * 返回当前解析的文档的默认合并设置。
     */
    @Nullable
    public String getMerge() {
        return this.merge;
    }

    /**
     * 设置当前解析的文档的默认自动装配设置。
     */
    public void setAutowire(@Nullable String autowire) {
        this.autowire = autowire;
    }

    /**
     * 返回当前解析的文档的默认自动装配设置。
     */
    @Nullable
    public String getAutowire() {
        return this.autowire;
    }

    /**
     * 设置当前解析的文档的默认autowire-candidate模式。
     * 同时接受以逗号分隔的模式列表。
     */
    public void setAutowireCandidates(@Nullable String autowireCandidates) {
        this.autowireCandidates = autowireCandidates;
    }

    /**
     * 返回当前解析的文档的默认自动装配候选模式。
     * 也可能返回以逗号分隔的模式列表。
     */
    @Nullable
    public String getAutowireCandidates() {
        return this.autowireCandidates;
    }

    /**
     * 设置当前解析文档的默认初始化方法设置。
     */
    public void setInitMethod(@Nullable String initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * 返回当前解析的文档的默认初始化方法设置。
     */
    @Nullable
    public String getInitMethod() {
        return this.initMethod;
    }

    /**
     * 设置当前解析的文档的默认destroy-method设置。
     */
    public void setDestroyMethod(@Nullable String destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    /**
     * 返回当前解析的文档的默认destroy-method设置。
     */
    @Nullable
    public String getDestroyMethod() {
        return this.destroyMethod;
    }

    /**
     * 设置此元数据元素的配置源对象。
     * <p>对象的准确类型将取决于所使用的配置机制。
     */
    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.source;
    }
}
