// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权或特定用途的适用性。
* 请参阅许可证了解具体语言管理权限和限制。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

/**
 * 用于持有Bean定义的注册表的接口，例如RootBeanDefinition和ChildBeanDefinition实例。通常由内部使用AbstractBeanDefinition层级的BeanFactory实现。
 *
 * <p>这是Spring的bean factory包中唯一封装了Bean定义的<i>注册</i>的接口。标准的BeanFactory接口仅涵盖对<i>完全配置的工厂实例</i>的访问。
 *
 * <p>Spring的bean定义读取器预期将与该接口的实现一起工作。Spring核心中已知的实现者包括DefaultListableBeanFactory和GenericApplicationContext。
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see PropertiesBeanDefinitionReader
 */
public interface BeanDefinitionRegistry extends AliasRegistry {

    /**
     * 向此注册表中注册一个新的Bean定义。
     * 必须支持RootBeanDefinition和ChildBeanDefinition。
     * @param beanName 要注册的Bean实例的名称
     * @param beanDefinition 要注册的Bean实例的定义
     * @throws BeanDefinitionStoreException 如果BeanDefinition无效
     * @throws BeanDefinitionOverrideException 如果已存在指定名称的BeanDefinition，并且不允许覆盖它
     * @see GenericBeanDefinition
     * @see RootBeanDefinition
     * @see ChildBeanDefinition
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException;

    /**
     * 移除给定名称的BeanDefinition。
     * @param beanName 要注册的Bean实例的名称
     * @throws NoSuchBeanDefinitionException 如果不存在这样的Bean定义异常
     */
    void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 返回给定 bean 名称的 BeanDefinition。
     * @param beanName 要查找定义的 bean 名称
     * @return 给定名称的 BeanDefinition（从不为 {@code null}）
     * @throws NoSuchBeanDefinitionException 如果不存在这样的 bean 定义
     */
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 检查此注册表中是否包含具有给定名称的Bean定义。
     * @param beanName 要查找的Bean的名称
     * @return 如果此注册表包含具有给定名称的Bean定义，则返回true
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 返回在此注册表中定义的所有 Bean 的名称。
     * @return 在此注册表中定义的所有 Bean 的名称数组，
     * 或一个空数组，如果没有定义任何 Bean。
     */
    String[] getBeanDefinitionNames();

    /**
     * 返回在注册表中定义的豆子数量。
     * @return 注册表中定义的豆子数量
     */
    int getBeanDefinitionCount();

    /**
     * 判断给定的 Bean 名称是否已在本注册表中使用，
     * 即是否在本名称下已注册了本地 Bean 或别名。
     * @param beanName 要检查的名称
     * @return 给定的 Bean 名称是否已被使用
     */
    boolean isBeanNameInUse(String beanName);
}
