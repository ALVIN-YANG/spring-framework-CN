// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“现状”提供的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.lang.Nullable;

/**
 * 允许将位于类路径位置的属性文件作为 Properties 实例在 Bean 工厂中可用。可以通过 Bean 引用填充任何类型的 Properties 的 Bean 属性。
 *
 * <p>支持从属性文件加载和/或在当前 FactoryBean 上设置本地属性。创建的 Properties 实例将合并加载和本地值。如果未设置位置或本地属性，则在初始化时将抛出异常。
 *
 * <p>可以创建单例或在每个请求时创建一个新对象。默认为单例。
 *
 * @author Juergen Hoeller
 * @see #setLocation
 * @see #setProperties
 * @see #setLocalOverride
 * @see java.util.Properties
 */
public class PropertiesFactoryBean extends PropertiesLoaderSupport implements FactoryBean<Properties>, InitializingBean {

    private boolean singleton = true;

    @Nullable
    private Properties singletonInstance;

    /**
     * 设置是否创建一个共享的 'singleton' Properties 实例，或者在每个请求时创建一个新的 Properties 实例。
     * <p>默认为 "true"（共享的单例）。
     */
    public final void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public final boolean isSingleton() {
        return this.singleton;
    }

    @Override
    public final void afterPropertiesSet() throws IOException {
        if (this.singleton) {
            this.singletonInstance = createProperties();
        }
    }

    @Override
    @Nullable
    public final Properties getObject() throws IOException {
        if (this.singleton) {
            return this.singletonInstance;
        } else {
            return createProperties();
        }
    }

    @Override
    public Class<Properties> getObjectType() {
        return Properties.class;
    }

    /**
     * 模板方法，子类可以覆盖以构建由该工厂返回的对象。默认实现返回普通的合并 Properties 实例。
     * <p>在共享单例的情况下，在初始化此 FactoryBean 时调用；否则，在每次调用 {@link #getObject()} 时调用。
     * @return 由该工厂返回的对象
     * @throws IOException 如果在属性加载过程中发生异常
     * @see #mergeProperties()
     */
    protected Properties createProperties() throws IOException {
        return mergeProperties();
    }
}
