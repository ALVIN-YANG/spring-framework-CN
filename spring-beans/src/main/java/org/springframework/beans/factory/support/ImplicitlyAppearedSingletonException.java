// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非适用法律要求或经书面同意，
* 否则不得使用此文件。您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，
* 请参阅许可证。*/
package org.springframework.beans.factory.support;

/**
 * 内部异常，将从 {@link ConstructorResolver} 传播，传递到发起的 {@link DefaultSingletonBeanRegistry}（无需包裹在 {@code BeanCreationException} 中）。
 *
 * @author Juergen Hoeller
 * @since 5.0
 */
@SuppressWarnings("serial")
class ImplicitlyAppearedSingletonException extends IllegalStateException {

    public ImplicitlyAppearedSingletonException() {
        super("About-to-be-created singleton instance implicitly appeared through the " + "creation of the factory bean that its bean definition points to");
    }
}
