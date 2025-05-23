// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解管理许可权限和限制的具体语言。*/
package org.springframework.beans;

import org.springframework.lang.Nullable;

/**
 * 接口由携带配置源对象的bean元数据元素实现
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface BeanMetadataElement {

    /**
     * 返回此元数据元素的配置源对象（可能为空）。
     */
    @Nullable
    default Object getSource() {
        return null;
    }
}
