// 翻译完成 glm-4-flash
/** 版权所有 2002-2012，原作者或作者。
*
* 根据Apache License，版本2.0（“许可证”）授权；
* 您不得使用此文件除非遵守许可证。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的保证或条件，
* 明示或暗示的。有关权限和限制的特定语言，请参阅许可证。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;

/**
 * 接口，以抽象方式暴露对bean名称的引用。
 * 此接口并不一定意味着对实际bean实例的引用；它只是表达了对bean名称的逻辑引用。
 *
 * <p>作为任何类型的bean引用持有者（如{@link RuntimeBeanReference}和{@link RuntimeBeanNameReference}）实现的通用接口。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface BeanReference extends BeanMetadataElement {

    /**
     * 返回此引用指向的目标Bean名称（永远不会为null）。
     */
    String getBeanName();
}
