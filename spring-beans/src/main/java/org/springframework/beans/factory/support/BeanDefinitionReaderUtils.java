// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可协议下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体规定许可权限和限制的内容。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 以下为 Bean 定义读取器实现中有用的实用方法。
 * 主要用于内部使用。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
public abstract class BeanDefinitionReaderUtils {

    /**
     * 生成Bean名称的分隔符。如果类名或父名不唯一，将附加“#1”、“#2”等，直到名称变得唯一。
     */
    public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;

    /**
     * 为指定的父类名称和类名称创建一个新的 GenericBeanDefinition，
     * 如果指定了 ClassLoader，则将预先加载豆类。
     * @param parentName 父豆的名称（如果有的话）
     * @param className 豆类的名称（如果有的话）
     * @param classLoader 用于加载豆类的 ClassLoader
     * （可以为 null 以仅通过名称注册豆类）
     * @return 豆定义
     * @throws ClassNotFoundException 如果无法加载豆类
     */
    public static AbstractBeanDefinition createBeanDefinition(@Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setParentName(parentName);
        if (className != null) {
            if (classLoader != null) {
                bd.setBeanClass(ClassUtils.forName(className, classLoader));
            } else {
                bd.setBeanClassName(className);
            }
        }
        return bd;
    }

    /**
     * 为给定的顶级bean定义生成一个名称，
     * 在给定的bean工厂中唯一。
     * @param beanDefinition 要为它生成bean名称的bean定义
     * @param registry 将要注册定义的bean工厂（用于检查现有bean名称）
     * @return 生成的bean名称
     * @throws BeanDefinitionStoreException 如果无法为给定的bean定义生成唯一的名称
     * @see #generateBeanName(BeanDefinition, BeanDefinitionRegistry, boolean)
     */
    public static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry) throws BeanDefinitionStoreException {
        return generateBeanName(beanDefinition, registry, false);
    }

    /**
     * 为给定的 Bean 定义生成一个在给定的 Bean 工厂中唯一的 Bean 名称。
     * @param definition 要为它生成 Bean 名称的 Bean 定义
     * @param registry 将要注册定义的 Bean 工厂（用于检查现有 Bean 名称）
     * @param isInnerBean 是否将给定的 Bean 定义注册为内部 Bean 或顶级 Bean（允许为内部 Bean 和顶级 Bean 生成特殊的名称）
     * @return 生成的 Bean 名称
     * @throws BeanDefinitionStoreException 如果无法为给定的 Bean 定义生成唯一的名称
     */
    public static String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry, boolean isInnerBean) throws BeanDefinitionStoreException {
        String generatedBeanName = definition.getBeanClassName();
        if (generatedBeanName == null) {
            if (definition.getParentName() != null) {
                generatedBeanName = definition.getParentName() + "$child";
            } else if (definition.getFactoryBeanName() != null) {
                generatedBeanName = definition.getFactoryBeanName() + "$created";
            }
        }
        if (!StringUtils.hasText(generatedBeanName)) {
            throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " + "'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
        }
        if (isInnerBean) {
            // 内部Bean：生成身份哈希码后缀。
            return generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
        }
        // 顶级Bean：如有必要，使用纯类名并附加唯一后缀。
        return uniqueBeanName(generatedBeanName, registry);
    }

    /**
     * 将给定的bean名称转换为给定bean工厂的唯一bean名称，
     * 如果需要，附加一个唯一的计数器作为后缀。
     * @param beanName 原始bean名称
     * @param registry 要将定义注册到的bean工厂（用于检查现有bean名称）
     * @return 要使用的唯一bean名称
     * @since 5.1
     */
    public static String uniqueBeanName(String beanName, BeanDefinitionRegistry registry) {
        String id = beanName;
        int counter = -1;
        // 增加计数器直到id是唯一的。
        String prefix = beanName + GENERATED_BEAN_NAME_SEPARATOR;
        while (counter == -1 || registry.containsBeanDefinition(id)) {
            counter++;
            id = prefix + counter;
        }
        return id;
    }

    /**
     * 将给定的bean定义注册到给定的bean工厂中。
     * @param definitionHolder 包含名称和别名的bean定义
     * @param registry 要注册的bean工厂
     * @throws BeanDefinitionStoreException 如果注册失败
     */
    public static void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) throws BeanDefinitionStoreException {
        // 在主名称下注册Bean定义。
        String beanName = definitionHolder.getBeanName();
        registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
        // 为 bean 名称注册别名，如果有的话。
        String[] aliases = definitionHolder.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                registry.registerAlias(beanName, alias);
            }
        }
    }

    /**
     * 将给定的bean定义与生成的名称注册，该名称在给定的bean工厂中是唯一的。
     * @param definition 要为bean定义生成bean名称
     * @param registry 要注册的bean工厂
     * @return 生成的bean名称
     * @throws BeanDefinitionStoreException 如果无法为给定的bean定义生成唯一名称，或者无法注册该定义
     */
    public static String registerWithGeneratedName(AbstractBeanDefinition definition, BeanDefinitionRegistry registry) throws BeanDefinitionStoreException {
        String generatedName = generateBeanName(definition, registry, false);
        registry.registerBeanDefinition(generatedName, definition);
        return generatedName;
    }
}
