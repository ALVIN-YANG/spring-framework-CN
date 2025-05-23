// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"），除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用性和非侵权性。
* 请参阅许可证以获取管理许可权限和限制的具体语言。*/
package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * 默认实现 {@link NamespaceHandlerResolver} 接口。
 * 根据映射文件中的映射关系将命名空间 URI 解析为实现类。
 *
 * <p>默认情况下，此实现会在 {@code META-INF/spring.handlers} 路径下查找映射文件，但可以通过使用
 * {@link #DefaultNamespaceHandlerResolver(ClassLoader, String)} 构造函数来更改此路径。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamespaceHandler
 * @see DefaultBeanDefinitionDocumentReader
 */
public class DefaultNamespaceHandlerResolver implements NamespaceHandlerResolver {

    /**
     * 查找映射文件的位置。可以存在于多个 JAR 文件中。
     */
    public static final String DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers";

    /**
     * 可被子类使用的日志记录器。
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * 用于NamespaceHandler类所使用的ClassLoader。
     */
    @Nullable
    private final ClassLoader classLoader;

    /**
     * 要搜索的资源位置。
     */
    private final String handlerMappingsLocation;

    /**
     * 存储从命名空间URI到NamespaceHandler类名/实例的映射。
     */
    @Nullable
    private volatile Map<String, Object> handlerMappings;

    /**
     * 使用默认的映射文件位置创建一个新的 {@code DefaultNamespaceHandlerResolver}。
     * <p>此构造函数将使用线程上下文 ClassLoader 来加载资源。
     * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
     */
    public DefaultNamespaceHandlerResolver() {
        this(null, DEFAULT_HANDLER_MAPPINGS_LOCATION);
    }

    /**
     * 使用默认的映射文件位置创建一个新的 {@code DefaultNamespaceHandlerResolver}。
     * @param classLoader 用于加载映射资源的 {@link ClassLoader} 实例（可能为 {@code null}，在这种情况下将使用线程上下文类加载器）
     * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
     */
    public DefaultNamespaceHandlerResolver(@Nullable ClassLoader classLoader) {
        this(classLoader, DEFAULT_HANDLER_MAPPINGS_LOCATION);
    }

    /**
     * 使用提供的映射文件位置创建一个新的 {@code DefaultNamespaceHandlerResolver}。
     * @param classLoader 用于加载映射资源的 {@link ClassLoader} 实例，可能为 {@code null}，在这种情况下将使用线程上下文类加载器
     * @param handlerMappingsLocation 映射文件位置
     */
    public DefaultNamespaceHandlerResolver(@Nullable ClassLoader classLoader, String handlerMappingsLocation) {
        Assert.notNull(handlerMappingsLocation, "Handler mappings location must not be null");
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
        this.handlerMappingsLocation = handlerMappingsLocation;
    }

    /**
     * 在配置的映射中定位由提供的命名空间URI指定的 {@link NamespaceHandler}
     * @param namespaceUri 相关的命名空间URI
     * @return 定位的 {@link NamespaceHandler}，如果未找到则返回 {@code null}
     */
    @Override
    @Nullable
    public NamespaceHandler resolve(String namespaceUri) {
        Map<String, Object> handlerMappings = getHandlerMappings();
        Object handlerOrClassName = handlerMappings.get(namespaceUri);
        if (handlerOrClassName == null) {
            return null;
        } else if (handlerOrClassName instanceof NamespaceHandler namespaceHandler) {
            return namespaceHandler;
        } else {
            String className = (String) handlerOrClassName;
            try {
                Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);
                if (!NamespaceHandler.class.isAssignableFrom(handlerClass)) {
                    throw new FatalBeanException("Class [" + className + "] for namespace [" + namespaceUri + "] does not implement the [" + NamespaceHandler.class.getName() + "] interface");
                }
                NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
                namespaceHandler.init();
                handlerMappings.put(namespaceUri, namespaceHandler);
                return namespaceHandler;
            } catch (ClassNotFoundException ex) {
                throw new FatalBeanException("Could not find NamespaceHandler class [" + className + "] for namespace [" + namespaceUri + "]", ex);
            } catch (LinkageError err) {
                throw new FatalBeanException("Unresolvable class definition for NamespaceHandler class [" + className + "] for namespace [" + namespaceUri + "]", err);
            }
        }
    }

    /**
     * 懒加载指定的命名空间处理程序映射。
     */
    private Map<String, Object> getHandlerMappings() {
        Map<String, Object> handlerMappings = this.handlerMappings;
        if (handlerMappings == null) {
            synchronized (this) {
                handlerMappings = this.handlerMappings;
                if (handlerMappings == null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Loading NamespaceHandler mappings from [" + this.handlerMappingsLocation + "]");
                    }
                    try {
                        Properties mappings = PropertiesLoaderUtils.loadAllProperties(this.handlerMappingsLocation, this.classLoader);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Loaded NamespaceHandler mappings: " + mappings);
                        }
                        handlerMappings = new ConcurrentHashMap<>(mappings.size());
                        CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
                        this.handlerMappings = handlerMappings;
                    } catch (IOException ex) {
                        throw new IllegalStateException("Unable to load NamespaceHandler mappings from location [" + this.handlerMappingsLocation + "]", ex);
                    }
                }
            }
        }
        return handlerMappings;
    }

    @Override
    public String toString() {
        return "NamespaceHandlerResolver using mappings " + getHandlerMappings();
    }
}
