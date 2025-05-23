// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory;

/**
 * 标记超接口，表示一个 Bean 有资格被 Spring 容器通过回调式方法通知特定的框架对象。
 * 实际的方法签名由各个子接口确定，但通常应仅包含一个接受单个参数的 void 返回方法。
 *
 * <p>注意，仅实现 {@link Aware} 并不会提供默认功能。相反，处理必须显式进行，例如在
 * 一个 {@link org.springframework.beans.factory.config.BeanPostProcessor} 中。有关处理特定
 * {@code *Aware} 接口回调的示例，请参阅 {@link org.springframework.context.support.ApplicationContextAwareProcessor}。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public interface Aware {
}
