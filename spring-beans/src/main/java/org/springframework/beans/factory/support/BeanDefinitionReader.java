// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
* 请参阅许可证以了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * 为使用 {@link Resource} 和 {@link String} 位置参数指定加载方法的简单接口。
 *
 * <p>具体的 Bean 定义读取器当然可以添加额外的 Bean 定义加载和注册方法，这些方法针对其特定的 Bean 定义格式。
 *
 * <p>请注意，Bean 定义读取器不必实现此接口。它仅作为对希望遵循标准命名约定的 Bean 定义读取器的一种建议。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.core.io.Resource
 */
public interface BeanDefinitionReader {

    /**
     * 返回用于注册Bean定义的Bean工厂。
     * <p>该工厂通过 {@link BeanDefinitionRegistry} 接口暴露，封装了与Bean定义处理相关的操作方法。
     */
    BeanDefinitionRegistry getRegistry();

    /**
     * 返回用于资源位置的 {@link ResourceLoader}。
     * <p>可以检查是否实现了 {@code ResourcePatternResolver} 接口，并相应地进行转换，以加载给定资源模式下的多个资源。
     * <p>返回值为 {@code null} 表示此 Bean 定义读取器不支持绝对资源加载。
     * <p>这主要用于在 Bean 定义资源内部导入更多资源，例如通过 XML Bean 定义中的 "import" 标签。然而，建议将此类导入相对于定义资源进行；只有显式的完整资源位置才会触发基于绝对路径的资源加载。
     * <p>此外，还有一个可用的 {@code loadBeanDefinitions(String)} 方法，用于从资源位置（或位置模式）加载 Bean 定义。这是一个避免显式处理 {@code ResourceLoader} 的便利方法。
     * @see #loadBeanDefinitions(String)
     * @see org.springframework.core.io.support.ResourcePatternResolver
     */
    @Nullable
    ResourceLoader getResourceLoader();

    /**
     * 返回用于bean类的类加载器。
     * <p>如果返回值为`null`，表示不急于加载bean类，而是仅注册带有类名的bean定义，
     * 相应的类将在之后（或永不）被解析。
     */
    @Nullable
    ClassLoader getBeanClassLoader();

    /**
     * 返回用于匿名Bean（未指定显式Bean名称）的{@link BeanNameGenerator}
     */
    BeanNameGenerator getBeanNameGenerator();

    /**
     * 从指定的资源加载 Bean 定义。
     * @param resource 资源描述符
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     */
    int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

    /**
     * 从指定的资源中加载Bean定义。
     * @param resources 资源描述符
     * @return 找到的Bean定义的数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出异常
     */
    int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

    /**
     * 从指定的资源位置加载 Bean 定义。
     * <p>位置也可以是一个位置模式，前提是此 Bean 定义读取器的 {@link ResourceLoader} 是一个
     * {@code ResourcePatternResolver}。
     * @param location 资源位置，将由此 Bean 定义读取器的 {@code ResourceLoader}（或
     * {@code ResourcePatternResolver}）加载
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     * @see #getResourceLoader()
     * @see #loadBeanDefinitions(org.springframework.core.io.Resource)
     * @see #loadBeanDefinitions(org.springframework.core.io.Resource[])
     */
    int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

    /**
     * 从指定的资源位置加载 Bean 定义。
     * @param locations 资源位置，将由本 Bean 定义读取器的 {@code ResourceLoader}（或 {@code ResourcePatternResolver}）加载
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析错误的情况下抛出
     */
    int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;
}
