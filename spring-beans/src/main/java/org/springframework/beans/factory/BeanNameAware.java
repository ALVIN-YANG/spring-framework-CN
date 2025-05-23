// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您不得使用此文件除非符合许可证规定。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵权。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory;

/**
 * 实现此接口的bean，可以在bean工厂中感知到自己的bean名称。请注意，通常不建议对象依赖于其bean名称，因为这代表了对外部配置的潜在脆弱依赖，以及可能的不必要依赖Spring API。
 *
 * <p>有关所有bean生命周期方法的列表，请参阅{@link BeanFactory BeanFactory}的javadoc。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 01.11.2003
 * @see BeanClassLoaderAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
public interface BeanNameAware extends Aware {

    /**
     * 在创建此Bean的Bean工厂中设置Bean的名称。
     * <p>在正常Bean属性设置之后、但在初始化回调（如{@link InitializingBean#afterPropertiesSet()}）或自定义init-method之前调用。
     * @param name 工厂中Bean的名称。
     * 注意，此名称是工厂中实际使用的Bean名称，可能与最初指定的名称不同：特别是对于内部Bean名称，实际Bean名称可能通过添加"#..."后缀来使其唯一。如果需要，可以使用{@link BeanFactoryUtils#originalBeanName(String)}方法提取原始Bean名称（不带后缀）。
     */
    void setBeanName(String name);
}
