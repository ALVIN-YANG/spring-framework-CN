// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据Apache许可证2.0版（以下简称“许可证”）许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.aot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 一组可以由 {@link Loader 加载} 或从 {@link SpringFactoriesLoader} 或通过一个 {@link ListableBeanFactory} 获取的 AOT 服务。
 *
 * @author Phillip Webb
 * @since 6.0
 * @param <T> 服务类型
 */
public final class AotServices<T> implements Iterable<T> {

    /**
     * 查找AOT（Ahead-of-Time）工厂的位置。
     */
    public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring/aot.factories";

    private final List<T> services;

    private final Map<String, T> beans;

    private final Map<T, Source> sources;

    private AotServices(List<T> loaded, Map<String, T> beans) {
        this.services = collectServices(loaded, beans);
        this.sources = collectSources(loaded, beans.values());
        this.beans = beans;
    }

    private List<T> collectServices(List<T> loaded, Map<String, T> beans) {
        List<T> services = new ArrayList<>();
        services.addAll(beans.values());
        services.addAll(loaded);
        AnnotationAwareOrderComparator.sort(services);
        return Collections.unmodifiableList(services);
    }

    private Map<T, Source> collectSources(Collection<T> loaded, Collection<T> beans) {
        Map<T, Source> sources = new IdentityHashMap<>();
        loaded.forEach(service -> sources.put(service, Source.SPRING_FACTORIES_LOADER));
        beans.forEach(service -> sources.put(service, Source.BEAN_FACTORY));
        return Collections.unmodifiableMap(sources);
    }

    /**
     * 创建一个新的 {@link Loader}，该Loader将从
     * {@value #FACTORIES_RESOURCE_LOCATION} 获取AOT服务。
     * @return 一个新的 {@link Loader} 实例
     */
    public static Loader factories() {
        return factories((ClassLoader) null);
    }

    /**
     * 创建一个新的 {@link Loader}，用于从 {@value #FACTORIES_RESOURCE_LOCATION} 获取 AOT 服务。
     * @param classLoader 用于加载工厂资源的类加载器
     * @return 一个新的 {@link Loader} 实例
     */
    public static Loader factories(@Nullable ClassLoader classLoader) {
        return factories(getSpringFactoriesLoader(classLoader));
    }

    /**
     * 创建一个新的 {@link Loader}，该实例将从给定的
     * {@link SpringFactoriesLoader} 中获取 AOT 服务。
     * @param springFactoriesLoader Spring 工厂加载器
     * @return 一个新的 {@link Loader} 实例
     */
    public static Loader factories(SpringFactoriesLoader springFactoriesLoader) {
        Assert.notNull(springFactoriesLoader, "'springFactoriesLoader' must not be null");
        return new Loader(springFactoriesLoader, null);
    }

    /**
     * 创建一个新的 {@link Loader}，它将从 {@value #FACTORIES_RESOURCE_LOCATION} 获取 AOT 服务，以及给定的 {@link ListableBeanFactory}。
     * @param beanFactory bean 工厂
     * @return 一个新的 {@link Loader} 实例
     */
    public static Loader factoriesAndBeans(ListableBeanFactory beanFactory) {
        ClassLoader classLoader = (beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory ? configurableBeanFactory.getBeanClassLoader() : null);
        return factoriesAndBeans(getSpringFactoriesLoader(classLoader), beanFactory);
    }

    /**
     * 创建一个新的 {@link Loader}，该 Loader 将从给定的
     * {@link SpringFactoriesLoader} 和 {@link ListableBeanFactory} 中获取 AOT 服务。
     * @param springFactoriesLoader Spring 工厂加载器
     * @param beanFactory Bean 工厂
     * @return 一个新的 {@link Loader} 实例
     */
    public static Loader factoriesAndBeans(SpringFactoriesLoader springFactoriesLoader, ListableBeanFactory beanFactory) {
        Assert.notNull(beanFactory, "'beanFactory' must not be null");
        Assert.notNull(springFactoriesLoader, "'springFactoriesLoader' must not be null");
        return new Loader(springFactoriesLoader, beanFactory);
    }

    private static SpringFactoriesLoader getSpringFactoriesLoader(@Nullable ClassLoader classLoader) {
        return SpringFactoriesLoader.forResourceLocation(FACTORIES_RESOURCE_LOCATION, classLoader);
    }

    @Override
    public Iterator<T> iterator() {
        return this.services.iterator();
    }

    /**
     * 返回 AOT 服务的 {@link Stream}。
     * @return 服务的流
     */
    public Stream<T> stream() {
        return this.services.stream();
    }

    /**
     * 返回 AOT 服务作为一个 {@link List}。
     * @return 服务列表
     */
    public List<T> asList() {
        return this.services;
    }

    /**
     * 查找给定bean名称所加载的AOT服务。
     * @param beanName bean名称
     * @return AOT服务或null
     */
    @Nullable
    public T findByBeanName(String beanName) {
        return this.beans.get(beanName);
    }

    /**
     * 获取给定服务的源。
     * @param service 服务实例
     * @return 服务的源
     */
    public Source getSource(T service) {
        Source source = this.sources.get(service);
        Assert.state(source != null, () -> "Unable to find service " + ObjectUtils.identityToString(source));
        return source;
    }

    /**
     * 用于实际加载服务的 Loader 类。
     */
    public static class Loader {

        private final SpringFactoriesLoader springFactoriesLoader;

        @Nullable
        private final ListableBeanFactory beanFactory;

        Loader(SpringFactoriesLoader springFactoriesLoader, @Nullable ListableBeanFactory beanFactory) {
            this.springFactoriesLoader = springFactoriesLoader;
            this.beanFactory = beanFactory;
        }

        /**
         * 加载给定类型的所有 AOT 服务。
         * @param <T> 服务类型
         * @param type 服务类型
         * @return 一个新的 {@link AotServices} 实例
         */
        public <T> AotServices<T> load(Class<T> type) {
            return new AotServices<>(this.springFactoriesLoader.load(type), loadBeans(type));
        }

        private <T> Map<String, T> loadBeans(Class<T> type) {
            return (this.beanFactory != null) ? BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type, true, false) : Collections.emptyMap();
        }
    }

    /**
     * 获取服务的来源。
     */
    public enum Source {

        /**
         * 一个从 {@link SpringFactoriesLoader} 加载的 AOT 服务。
         */
        SPRING_FACTORIES_LOADER,
        /**
         * 从一个{@link BeanFactory}加载的AOT服务。
         */
        BEAN_FACTORY
    }
}
