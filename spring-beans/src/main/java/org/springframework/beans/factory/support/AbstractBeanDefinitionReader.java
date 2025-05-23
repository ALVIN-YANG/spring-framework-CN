// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 您必须遵守许可证才能使用此文件。
* 您可以在以下网址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解管理许可权和限制的具体语言。*/
package org.springframework.beans.factory.support;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 实现了 {@link BeanDefinitionReader} 接口的 Bean 定义读取器的抽象基类。
 *
 * <p>提供了一些公共属性，如要工作的 Bean 工厂以及用于加载 Bean 类的类加载器。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2003年12月11日
 * @see BeanDefinitionReaderUtils
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader, EnvironmentCapable {

    /**
     * 供子类使用的日志记录器。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private final BeanDefinitionRegistry registry;

    @Nullable
    private ResourceLoader resourceLoader;

    @Nullable
    private ClassLoader beanClassLoader;

    private Environment environment;

    private BeanNameGenerator beanNameGenerator = DefaultBeanNameGenerator.INSTANCE;

    /**
     * 为给定的bean工厂创建一个新的AbstractBeanDefinitionReader。
     * <p>如果传入的bean工厂不仅实现了BeanDefinitionRegistry接口，还实现了ResourceLoader接口，则它也将用作默认的ResourceLoader。这通常适用于{@link org.springframework.context.ApplicationContext}的实现。
     * <p>如果提供了一个普通的BeanDefinitionRegistry，则默认的ResourceLoader将是一个{@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}。
     * <p>如果传入的bean工厂还实现了{@link EnvironmentCapable}，则其环境将由该读取器使用。否则，读取器将初始化并使用一个{@link StandardEnvironment}。所有ApplicationContext实现都是EnvironmentCapable，而正常的BeanFactory实现则不是。
     * @param registry 要将bean定义加载到其中的BeanFactory，形式为一个BeanDefinitionRegistry
     * @see #setResourceLoader
     * @see #setEnvironment
     */
    protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
        this.registry = registry;
        // 确定要使用的资源加载器。
        if (this.registry instanceof ResourceLoader _resourceLoader) {
            this.resourceLoader = _resourceLoader;
        } else {
            this.resourceLoader = new PathMatchingResourcePatternResolver();
        }
        // 如果可能，继承环境
        if (this.registry instanceof EnvironmentCapable environmentCapable) {
            this.environment = environmentCapable.getEnvironment();
        } else {
            this.environment = new StandardEnvironment();
        }
    }

    @Override
    public final BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }

    /**
     * 设置用于资源位置的 ResourceLoader。
     * 如果指定了 ResourcePatternResolver，则 Bean 定义读取器将能够将资源模式解析为 Resource 数组。
     * <p>默认为 PathMatchingResourcePatternResolver，也通过 ResourcePatternResolver 接口支持资源模式解析。
     * <p>将此设置为 {@code null} 表示对于此 Bean 定义读取器，不可用绝对资源加载。
     * @see org.springframework.core.io.support.ResourcePatternResolver
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    @Nullable
    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    /**
     * 设置用于 Bean 类的 ClassLoader。
     * <p>默认值为 {@code null}，这表示不急于加载 Bean 类，而是仅注册带有类名的 Bean 定义，
     * 相应的类将在稍后（或永远不会）解析。
     * @see Thread#getContextClassLoader()
     */
    public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    @Override
    @Nullable
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    /**
     * 设置在读取bean定义时使用的环境。通常用于评估配置信息以确定应读取哪些bean定义以及应忽略哪些bean定义。
     */
    public void setEnvironment(Environment environment) {
        Assert.notNull(environment, "Environment must not be null");
        this.environment = environment;
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    /**
     * 设置用于匿名Bean的BeanNameGenerator（未指定显式Bean名称）。
     * <p>默认使用一个{@link DefaultBeanNameGenerator}。
     */
    public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
        this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : DefaultBeanNameGenerator.INSTANCE);
    }

    @Override
    public BeanNameGenerator getBeanNameGenerator() {
        return this.beanNameGenerator;
    }

    @Override
    public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
        Assert.notNull(resources, "Resource array must not be null");
        int count = 0;
        for (Resource resource : resources) {
            count += loadBeanDefinitions(resource);
        }
        return count;
    }

    @Override
    public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(location, null);
    }

    /**
     * 从指定的资源位置加载Bean定义。
     * <p>位置也可以是一个位置模式，前提是此Bean定义读取器的ResourceLoader是一个ResourcePatternResolver。
     * @param location 资源位置，将由此Bean定义读取器的ResourceLoader（或ResourcePatternResolver）加载
     * @param actualResources 一个将要被填充的Set，包含在加载过程中解析的实际Resource对象。可能为null，表示调用者不感兴趣这些Resource对象。
     * @return 找到的Bean定义数量
     * @throws BeanDefinitionStoreException 如果在加载或解析过程中发生错误
     * @see #getResourceLoader()
     * @see #loadBeanDefinitions(org.springframework.core.io.Resource)
     * @see #loadBeanDefinitions(org.springframework.core.io.Resource[])
     */
    public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
        ResourceLoader resourceLoader = getResourceLoader();
        if (resourceLoader == null) {
            throw new BeanDefinitionStoreException("Cannot load bean definitions from location [" + location + "]: no ResourceLoader available");
        }
        if (resourceLoader instanceof ResourcePatternResolver resourcePatternResolver) {
            // 资源模式匹配可用。
            try {
                Resource[] resources = resourcePatternResolver.getResources(location);
                int count = loadBeanDefinitions(resources);
                if (actualResources != null) {
                    Collections.addAll(actualResources, resources);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Loaded " + count + " bean definitions from location pattern [" + location + "]");
                }
                return count;
            } catch (IOException ex) {
                throw new BeanDefinitionStoreException("Could not resolve bean definition resource pattern [" + location + "]", ex);
            }
        } else {
            // 只能通过绝对URL加载单个资源。
            Resource resource = resourceLoader.getResource(location);
            int count = loadBeanDefinitions(resource);
            if (actualResources != null) {
                actualResources.add(resource);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Loaded " + count + " bean definitions from location [" + location + "]");
            }
            return count;
        }
    }

    @Override
    public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
        Assert.notNull(locations, "Location array must not be null");
        int count = 0;
        for (String location : locations) {
            count += loadBeanDefinitions(location);
        }
        return count;
    }
}
