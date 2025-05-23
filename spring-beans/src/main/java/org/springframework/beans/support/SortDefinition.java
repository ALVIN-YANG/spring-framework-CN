// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非遵守许可证。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.support;

/**
 * 定义按属性对 bean 实例进行排序的代码。
 *
 * @author Juergen Hoeller
 * @since 2003年5月26日
 */
public interface SortDefinition {

    /**
     * 返回要比较的 Bean 属性名称。
     * 也可以是一个嵌套的 Bean 属性路径。
     */
    String getProperty();

    /**
     * 返回是否应忽略字符串值中的大小写。
     */
    boolean isIgnoreCase();

    /**
     * 返回是否为升序排序（true）或降序排序（false）。
     */
    boolean isAscending();
}
