// 翻译完成 glm-4-flash
/** 版权所有 2002-2006 原作者或作者。
*
* 根据 Apache License 2.0 版本（以下简称“许可证”）授权；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import java.util.Collection;

/**
 * 一个暴露简单属性的 Bean，该属性可以设置为引用和单个值的混合。
 *
 * @author Rod Johnson
 * @since 2003年5月27日
 */
public class MixedCollectionBean {

    private Collection<?> jumble;

    public void setJumble(Collection<?> jumble) {
        this.jumble = jumble;
    }

    public Collection<?> getJumble() {
        return jumble;
    }
}
