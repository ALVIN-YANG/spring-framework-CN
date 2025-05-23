// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory;

/**
 *  接口，由希望在被销毁时释放资源的bean实现。
 *  当作用域bean被单独销毁时，`BeanFactory` 将会调用其`destroy`方法。`ApplicationContext` 应在应用程序生命周期驱动下关闭时销毁其所有单例bean。
 *
 * <p>Spring管理的bean也可以实现Java的`AutoCloseable`接口以实现相同的目的。实现接口的另一种方法是指定一个自定义的销毁方法，例如在XML bean定义中。有关所有bean生命周期方法的列表，请参阅`BeanFactory`的javadoc。
 *
 *  @author Juergen Hoeller
 *  @since 12.08.2003
 *  @see InitializingBean
 *  @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName()
 *  @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
 *  @see org.springframework.context.ConfigurableApplicationContext#close()
 */
public interface DisposableBean {

    /**
     * 当包含的 {@code BeanFactory} 在销毁一个 Bean 时调用。
     * 在关闭过程中出现错误时抛出异常。异常将被记录但不会重新抛出，以允许其他 Bean 也能释放其资源。
     */
    void destroy() throws Exception;
}
