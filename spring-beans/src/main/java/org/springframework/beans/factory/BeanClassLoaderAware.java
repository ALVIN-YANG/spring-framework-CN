// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能无法使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性的保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory;

/**
 * 回调接口，允许一个Bean感知到其使用的类加载器（ClassLoader）；即当前Bean工厂用于加载Bean类的类加载器。
 *
 * <p>此接口主要供框架类实现，这些框架类需要通过名称来获取应用类，尽管它们自身可能由共享的类加载器加载。
 *
 * <p>有关所有Bean生命周期方法的列表，请参阅{@link BeanFactory BeanFactory}的javadoc。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.0
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
public interface BeanClassLoaderAware extends Aware {

    /**
     * 回调，用于向一个bean实例提供bean的类加载器。
     * <p>在正常bean属性的填充之后，但在初始化回调（如
     * {@link InitializingBean InitializingBean} 的
     * {@link InitializingBean#afterPropertiesSet()}
     * 方法或自定义的init-method）之前被调用。
     * @param classLoader 拥有该类加载器的对象
     */
    void setBeanClassLoader(ClassLoader classLoader);
}
