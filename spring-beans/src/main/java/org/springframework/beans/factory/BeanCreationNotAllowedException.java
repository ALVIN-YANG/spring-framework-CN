// 翻译完成 glm-4-flash
/** 版权所有 2002-2012 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（“许可证”），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory;

/**
 * 在当前不允许创建bean的情况下请求bean时抛出异常（例如，在bean工厂的关闭阶段）。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class BeanCreationNotAllowedException extends BeanCreationException {

    /**
     * 创建一个新的BeanCreationNotAllowedException。
     * @param beanName 请求的bean名称
     * @param msg 详细消息
     */
    public BeanCreationNotAllowedException(String beanName, String msg) {
        super(beanName, msg);
    }
}
