// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面协议要求，否则在许可协议下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性的保证。
* 请参阅许可协议，了解具体的权限和限制条款。*/
package org.springframework.beans.factory.groovy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 基于 Groovy 的 Spring Bean 定义读取器：类似于 Groovy 构建器，但更像是 Spring 配置的 DSL。
 *
 * <p>此 Bean 定义读取器还理解 XML Bean 定义文件，允许与 Groovy Bean 定义文件无缝混合使用。
 *
 * <p>通常应用于
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
 * 或
 * {@link org.springframework.context.support.GenericApplicationContext}，
 * 但可以用于任何
 * {@link BeanDefinitionRegistry}
 * 实现的实例。
 *
 * <h3>示例语法</h3>
 * <pre class="code">
 * import org.hibernate.SessionFactory
 * import org.apache.commons.dbcp.BasicDataSource
 *
 * def reader = new GroovyBeanDefinitionReader(myApplicationContext)
 * reader.beans {
 *     dataSource(BasicDataSource) {                  // &lt;--- 调用方法
 *         driverClassName = "org.hsqldb.jdbcDriver"
 *         url = "jdbc:hsqldb:mem:grailsDB"
 *         username = "sa"                            // &lt;-- 设置属性
 *         password = ""
 *         settings = [mynew:"setting"]
 *     }
 *     sessionFactory(SessionFactory) {
 *         dataSource = dataSource                    // &lt;-- 获取属性以获取引用
 *     }
 *     myService(MyService) {
 *         nestedBean = { AnotherBean bean -&gt;         // &lt;-- 使用闭包设置嵌套 Bean 的属性
 *             dataSource = dataSource
 *         }
 *     }
 * }</pre>
 *
 * <p>您还可以使用
 * {@link #loadBeanDefinitions(Resource...)}
 * 或
 * {@link #loadBeanDefinitions(String...)}
 * 方法加载包含在 Groovy 脚本中定义的 Bean 的资源，脚本看起来类似于以下内容。
 *
 * <pre class="code">
 * import org.hibernate.SessionFactory
 * import org.apache.commons.dbcp.BasicDataSource
 *
 * beans {
 *     dataSource(BasicDataSource) {
 *         driverClassName = "org.hsqldb.jdbcDriver"
 *         url = "jdbc:hsqldb:mem:grailsDB"
 *         username = "sa"
 *         password = ""
 *         settings = [mynew:"setting"]
 *     }
 *     sessionFactory(SessionFactory) {
 *         dataSource = dataSource
 *     }
 *     myService(MyService) {
 *         nestedBean = { AnotherBean bean -&gt;
 *             dataSource = dataSource
 *         }
 *     }
 * }</pre>
 *
 * @author Jeff Brown
 * @author Graeme Rocher
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 * @see BeanDefinitionRegistry
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.context.support.GenericGroovyApplicationContext
 */
public class GroovyBeanDefinitionReader extends AbstractBeanDefinitionReader implements GroovyObject {

    /**
     * 使用默认设置创建标准 {@code XmlBeanDefinitionReader}，用于从 XML 文件加载 Bean 定义。
     */
    private final XmlBeanDefinitionReader standardXmlBeanDefinitionReader;

    /**
     * 使用 Groovy DSL 的 {@code XmlBeanDefinitionReader} 加载 Bean 定义
     * 通常配置为禁用 XML 验证。
     */
    private final XmlBeanDefinitionReader groovyDslXmlBeanDefinitionReader;

    private final Map<String, String> namespaces = new HashMap<>();

    private final Map<String, DeferredProperty> deferredProperties = new HashMap<>();

    private MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(getClass());

    @Nullable
    private Binding binding;

    @Nullable
    private GroovyBeanDefinitionWrapper currentBeanDefinition;

    /**
     * 为给定的 {@link BeanDefinitionRegistry} 创建一个新的 {@code GroovyBeanDefinitionReader}。
     * @param registry 要加载 bean 定义的目标 {@code BeanDefinitionRegistry}
     */
    public GroovyBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
        this.standardXmlBeanDefinitionReader = new XmlBeanDefinitionReader(registry);
        this.groovyDslXmlBeanDefinitionReader = new XmlBeanDefinitionReader(registry);
        this.groovyDslXmlBeanDefinitionReader.setValidating(false);
    }

    /**
     * 基于给定的 {@link XmlBeanDefinitionReader} 创建一个新的 {@code GroovyBeanDefinitionReader}，将Bean定义加载到其 {@code BeanDefinitionRegistry} 中，并将Groovy DSL加载委托给它。
     * 供应的 {@code XmlBeanDefinitionReader} 通常应该预先配置为禁用XML验证。
     * @param xmlBeanDefinitionReader 从中派生注册表并将其委托给Groovy DSL加载的 {@code XmlBeanDefinitionReader}
     */
    public GroovyBeanDefinitionReader(XmlBeanDefinitionReader xmlBeanDefinitionReader) {
        super(xmlBeanDefinitionReader.getRegistry());
        this.standardXmlBeanDefinitionReader = new XmlBeanDefinitionReader(xmlBeanDefinitionReader.getRegistry());
        this.groovyDslXmlBeanDefinitionReader = xmlBeanDefinitionReader;
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public MetaClass getMetaClass() {
        return this.metaClass;
    }

    /**
     * 设置绑定，即在一个 `GroovyBeanDefinitionReader` 闭包作用域中可用的 Groovy 变量。
     */
    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    /**
     * 如果存在，返回指定的 Groovy 变量的绑定。
     */
    @Nullable
    public Binding getBinding() {
        return this.binding;
    }

    // 传统Bean定义读取方法
    /**
     * 从指定的 Groovy 脚本或 XML 文件中加载 Bean 定义。
     * <p>注意，以 ".xml" 结尾的文件将被解析为 XML 内容；所有其他类型的资源将被解析为 Groovy 脚本。
     * @param resource Groovy 脚本或 XML 文件的资源描述符
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 如果在加载或解析过程中出现错误
     */
    @Override
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(new EncodedResource(resource));
    }

    /**
     * 从指定的 Groovy 脚本或 XML 文件中加载 Bean 定义。
     * <p>注意，以 ".xml" 为后缀的文件将被解析为 XML 内容；所有其他类型的资源将被解析为 Groovy 脚本。
     * @param encodedResource Groovy 脚本或 XML 文件的资源描述符，允许指定用于解析文件的编码
     * @return 找到的 Bean 定义数量
     * @throws BeanDefinitionStoreException 在加载或解析出错的情况下
     */
    public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
        // 检查 XML 文件并将它们重定向到 "标准" 的 XmlBeanDefinitionReader
        String filename = encodedResource.getResource().getFilename();
        if (StringUtils.endsWithIgnoreCase(filename, ".xml")) {
            return this.standardXmlBeanDefinitionReader.loadBeanDefinitions(encodedResource);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Loading Groovy bean definitions from " + encodedResource);
        }
        @SuppressWarnings("serial")
        Closure<Object> beans = new Closure<>(this) {

            @Override
            public Object call(Object... args) {
                invokeBeanDefiningClosure((Closure<?>) args[0]);
                return null;
            }
        };
        Binding binding = new Binding() {

            @Override
            public void setVariable(String name, Object value) {
                if (currentBeanDefinition != null) {
                    applyPropertyToBeanDefinition(name, value);
                } else {
                    super.setVariable(name, value);
                }
            }
        };
        binding.setVariable("beans", beans);
        int countBefore = getRegistry().getBeanDefinitionCount();
        try {
            GroovyShell shell = new GroovyShell(getBeanClassLoader(), binding);
            shell.evaluate(encodedResource.getReader(), "beans");
        } catch (Throwable ex) {
            throw new BeanDefinitionParsingException(new Problem("Error evaluating Groovy script: " + ex.getMessage(), new Location(encodedResource.getResource()), null, ex));
        }
        int count = getRegistry().getBeanDefinitionCount() - countBefore;
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded " + count + " bean definitions from " + encodedResource);
        }
        return count;
    }

    // Groovy闭包中的消费方法
    /**
     * 定义了给定块或闭包的一组 beans。
     * @param closure 块或闭包
     * @return 此 {@code GroovyBeanDefinitionReader} 实例
     */
    public GroovyBeanDefinitionReader beans(Closure<?> closure) {
        return invokeBeanDefiningClosure(closure);
    }

    /**
     * 定义一个内部bean定义。
     * @param type bean类型
     * @return bean定义
     */
    public GenericBeanDefinition bean(Class<?> type) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(type);
        return beanDefinition;
    }

    /**
     * 定义一个内部bean定义。
     * @param type bean类型
     * @param args 构造函数参数和闭包配置器
     * @return bean定义
     */
    public AbstractBeanDefinition bean(Class<?> type, Object... args) {
        GroovyBeanDefinitionWrapper current = this.currentBeanDefinition;
        try {
            Closure<?> callable = null;
            Collection<Object> constructorArgs = null;
            if (!ObjectUtils.isEmpty(args)) {
                int index = args.length;
                Object lastArg = args[index - 1];
                if (lastArg instanceof Closure<?> closure) {
                    callable = closure;
                    index--;
                }
                constructorArgs = resolveConstructorArguments(args, 0, index);
            }
            this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(null, type, constructorArgs);
            if (callable != null) {
                callable.call(this.currentBeanDefinition);
            }
            return this.currentBeanDefinition.getBeanDefinition();
        } finally {
            this.currentBeanDefinition = current;
        }
    }

    /**
     * 定义一个用于的 Spring XML 命名空间定义。
     * @param definition 命名空间定义参数
     */
    public void xmlns(Map<String, String> definition) {
        if (!definition.isEmpty()) {
            for (Map.Entry<String, String> entry : definition.entrySet()) {
                String namespace = entry.getKey();
                String uri = entry.getValue();
                if (uri == null) {
                    throw new IllegalArgumentException("Namespace definition must supply a non-null URI");
                }
                NamespaceHandler namespaceHandler = this.groovyDslXmlBeanDefinitionReader.getNamespaceHandlerResolver().resolve(uri);
                if (namespaceHandler == null) {
                    throw new BeanDefinitionParsingException(new Problem("No namespace handler found for URI: " + uri, new Location(new DescriptiveResource(("Groovy")))));
                }
                this.namespaces.put(namespace, uri);
            }
        }
    }

    /**
     * 从 XML 或 Groovy 源导入 Spring 的 bean 定义到当前的 bean 构建实例中。
     * @param resourcePattern 资源模式
     */
    public void importBeans(String resourcePattern) throws IOException {
        loadBeanDefinitions(resourcePattern);
    }

    // Groovy 闭包和属性的内部处理
    /**
     * 此方法覆盖了方法调用，为每个接收类参数的方法名称创建相应的 Bean。
     */
    @Override
    public Object invokeMethod(String name, Object arg) {
        Object[] args = (Object[]) arg;
        if ("beans".equals(name) && args.length == 1 && args[0] instanceof Closure<?> closure) {
            return beans(closure);
        } else if ("ref".equals(name)) {
            String refName;
            if (args[0] == null) {
                throw new IllegalArgumentException("Argument to ref() is not a valid bean or was not found");
            }
            if (args[0] instanceof RuntimeBeanReference runtimeBeanReference) {
                refName = runtimeBeanReference.getBeanName();
            } else {
                refName = args[0].toString();
            }
            boolean parentRef = false;
            if (args.length > 1 && args[1] instanceof Boolean bool) {
                parentRef = bool;
            }
            return new RuntimeBeanReference(refName, parentRef);
        } else if (this.namespaces.containsKey(name) && args.length > 0 && args[0] instanceof Closure) {
            GroovyDynamicElementReader reader = createDynamicElementReader(name);
            reader.invokeMethod("doCall", args);
        } else if (args.length > 0 && args[0] instanceof Closure) {
            // 抽象的Bean定义
            return invokeBeanDefiningMethod(name, args);
        } else if (args.length > 0 && (args[0] instanceof Class || args[0] instanceof RuntimeBeanReference || args[0] instanceof Map)) {
            return invokeBeanDefiningMethod(name, args);
        } else if (args.length > 1 && args[args.length - 1] instanceof Closure) {
            return invokeBeanDefiningMethod(name, args);
        }
        MetaClass mc = DefaultGroovyMethods.getMetaClass(getRegistry());
        if (!mc.respondsTo(getRegistry(), name, args).isEmpty()) {
            return mc.invokeMethod(getRegistry(), name, args);
        }
        return this;
    }

    private boolean addDeferredProperty(String property, Object newValue) {
        if (newValue instanceof List || newValue instanceof Map) {
            this.deferredProperties.put(this.currentBeanDefinition.getBeanName() + '.' + property, new DeferredProperty(this.currentBeanDefinition, property, newValue));
            return true;
        }
        return false;
    }

    private void finalizeDeferredProperties() {
        for (DeferredProperty dp : this.deferredProperties.values()) {
            if (dp.value instanceof List<?> list) {
                dp.value = manageListIfNecessary(list);
            } else if (dp.value instanceof Map<?, ?> map) {
                dp.value = manageMapIfNecessary(map);
            }
            dp.apply();
        }
        this.deferredProperties.clear();
    }

    /**
     * 当方法参数仅是一个闭包时，它是一组Bean定义。
     * @param callable 闭包参数
     * @return 这个 {@code GroovyBeanDefinitionReader} 实例
     */
    protected GroovyBeanDefinitionReader invokeBeanDefiningClosure(Closure<?> callable) {
        callable.setDelegate(this);
        callable.call();
        finalizeDeferredProperties();
        return this;
    }

    /**
     * 当调用一个bean定义节点时调用此方法。
     * @param beanName 要定义的bean的名称
     * @param args 传递给bean的参数。第一个参数是类名，最后一个参数有时是一个闭包。中间的所有参数都是构造函数的参数。
     * @return bean定义包装器
     */
    private GroovyBeanDefinitionWrapper invokeBeanDefiningMethod(String beanName, Object[] args) {
        boolean hasClosureArgument = (args[args.length - 1] instanceof Closure);
        if (args[0] instanceof Class<?> beanClass) {
            if (hasClosureArgument) {
                if (args.length - 1 != 1) {
                    this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName, beanClass, resolveConstructorArguments(args, 1, args.length - 1));
                } else {
                    this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName, beanClass);
                }
            } else {
                this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName, beanClass, resolveConstructorArguments(args, 1, args.length));
            }
        } else if (args[0] instanceof RuntimeBeanReference runtimeBeanReference) {
            this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName);
            this.currentBeanDefinition.getBeanDefinition().setFactoryBeanName(runtimeBeanReference.getBeanName());
        } else if (args[0] instanceof Map<?, ?> namedArgs) {
            // 命名构造器参数
            if (args.length > 1 && args[1] instanceof Class<?> clazz) {
                List<Object> constructorArgs = resolveConstructorArguments(args, 2, hasClosureArgument ? args.length - 1 : args.length);
                this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName, clazz, constructorArgs);
                for (Map.Entry<?, ?> entity : namedArgs.entrySet()) {
                    String propName = (String) entity.getKey();
                    setProperty(propName, entity.getValue());
                }
            } else // 工厂方法语法
            {
                this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName);
                // 第一个参数是包含 factoryBean 的映射：factoryMethod
                Map.Entry<?, ?> factoryBeanEntry = namedArgs.entrySet().iterator().next();
                // 如果我们有一个闭包体，那么它将是最后一个参数。
                // 在之间是构造函数的参数
                int constructorArgsTest = (hasClosureArgument ? 2 : 1);
                // 如果我们有超过这个数量的参数，我们就有了构造函数参数
                if (args.length > constructorArgsTest) {
                    // 工厂方法需要参数
                    int endOfConstructArgs = (hasClosureArgument ? args.length - 1 : args.length);
                    this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName, null, resolveConstructorArguments(args, 1, endOfConstructArgs));
                } else {
                    this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName);
                }
                this.currentBeanDefinition.getBeanDefinition().setFactoryBeanName(factoryBeanEntry.getKey().toString());
                this.currentBeanDefinition.getBeanDefinition().setFactoryMethodName(factoryBeanEntry.getValue().toString());
            }
        } else if (args[0] instanceof Closure) {
            this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName);
            this.currentBeanDefinition.getBeanDefinition().setAbstract(true);
        } else {
            List<Object> constructorArgs = resolveConstructorArguments(args, 0, hasClosureArgument ? args.length - 1 : args.length);
            this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(beanName, null, constructorArgs);
        }
        if (hasClosureArgument) {
            Closure<?> callable = (Closure<?>) args[args.length - 1];
            callable.setDelegate(this);
            callable.setResolveStrategy(Closure.DELEGATE_FIRST);
            callable.call(this.currentBeanDefinition);
        }
        GroovyBeanDefinitionWrapper beanDefinition = this.currentBeanDefinition;
        this.currentBeanDefinition = null;
        beanDefinition.getBeanDefinition().setAttribute(GroovyBeanDefinitionWrapper.class.getName(), beanDefinition);
        getRegistry().registerBeanDefinition(beanName, beanDefinition.getBeanDefinition());
        return beanDefinition;
    }

    protected List<Object> resolveConstructorArguments(Object[] args, int start, int end) {
        Object[] constructorArgs = Arrays.copyOfRange(args, start, end);
        for (int i = 0; i < constructorArgs.length; i++) {
            if (constructorArgs[i] instanceof GString) {
                constructorArgs[i] = constructorArgs[i].toString();
            } else if (constructorArgs[i] instanceof List<?> list) {
                constructorArgs[i] = manageListIfNecessary(list);
            } else if (constructorArgs[i] instanceof Map<?, ?> map) {
                constructorArgs[i] = manageMapIfNecessary(map);
            }
        }
        return List.of(constructorArgs);
    }

    /**
     * 检查是否有任何 {@link RuntimeBeanReference RuntimeBeanReferences} 在该 {@link Map} 中，并在必要时将其转换为 {@link ManagedMap}。
     * @param map 原始的 Map
     * @return 原始的 Map 或其管理的副本
     */
    private Object manageMapIfNecessary(Map<?, ?> map) {
        boolean containsRuntimeRefs = false;
        for (Object element : map.values()) {
            if (element instanceof RuntimeBeanReference) {
                containsRuntimeRefs = true;
                break;
            }
        }
        if (containsRuntimeRefs) {
            Map<Object, Object> managedMap = new ManagedMap<>();
            managedMap.putAll(map);
            return managedMap;
        }
        return map;
    }

    /**
     * 检查是否在 {@link List} 中存在任何 {@link RuntimeBeanReference}，并在必要时将其转换为 {@link ManagedList}。
     * @param list 原始的 List
     * @return 原始列表或其管理的副本
     */
    private Object manageListIfNecessary(List<?> list) {
        boolean containsRuntimeRefs = false;
        for (Object element : list) {
            if (element instanceof RuntimeBeanReference) {
                containsRuntimeRefs = true;
                break;
            }
        }
        if (containsRuntimeRefs) {
            List<Object> managedList = new ManagedList<>();
            managedList.addAll(list);
            return managedList;
        }
        return list;
    }

    /**
     * 此方法覆盖了在 {@code GroovyBeanDefinitionReader} 范围内的属性设置，用于在当前bean定义上设置属性。
     */
    @Override
    public void setProperty(String name, Object value) {
        if (this.currentBeanDefinition != null) {
            applyPropertyToBeanDefinition(name, value);
        }
    }

    protected void applyPropertyToBeanDefinition(String name, Object value) {
        if (value instanceof GString) {
            value = value.toString();
        }
        if (addDeferredProperty(name, value)) {
            return;
        } else if (value instanceof Closure<?> callable) {
            GroovyBeanDefinitionWrapper current = this.currentBeanDefinition;
            try {
                Class<?> parameterType = callable.getParameterTypes()[0];
                if (Object.class == parameterType) {
                    this.currentBeanDefinition = new GroovyBeanDefinitionWrapper("");
                    callable.call(this.currentBeanDefinition);
                } else {
                    this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(null, parameterType);
                    callable.call((Object) null);
                }
                value = this.currentBeanDefinition.getBeanDefinition();
            } finally {
                this.currentBeanDefinition = current;
            }
        }
        this.currentBeanDefinition.addProperty(name, value);
    }

    /**
     * 此方法覆盖了在 {@code GroovyBeanDefinitionReader} 范围内的属性检索。属性检索将执行以下操作之一：
     * <ul>
     * <li>如果存在，从 bean 构建器的绑定中检索一个变量
     * <li>如果存在，检索特定 bean 的 RuntimeBeanReference
     * <li>否则，委托给 MetaClass.getProperty，这将从 {@code GroovyBeanDefinitionReader} 本身解析属性
     * </ul>
     */
    @Override
    public Object getProperty(String name) {
        Binding binding = getBinding();
        if (binding != null && binding.hasVariable(name)) {
            return binding.getVariable(name);
        } else {
            if (this.namespaces.containsKey(name)) {
                return createDynamicElementReader(name);
            }
            if (getRegistry().containsBeanDefinition(name)) {
                GroovyBeanDefinitionWrapper beanDefinition = (GroovyBeanDefinitionWrapper) getRegistry().getBeanDefinition(name).getAttribute(GroovyBeanDefinitionWrapper.class.getName());
                if (beanDefinition != null) {
                    return new GroovyRuntimeBeanReference(name, beanDefinition, false);
                } else {
                    return new RuntimeBeanReference(name, false);
                }
            } else // 这是为了处理属性设置器是最后的这种情况
            // 闭包中的语句（因此有返回值）
            if (this.currentBeanDefinition != null) {
                MutablePropertyValues pvs = this.currentBeanDefinition.getBeanDefinition().getPropertyValues();
                if (pvs.contains(name)) {
                    return pvs.get(name);
                } else {
                    DeferredProperty dp = this.deferredProperties.get(this.currentBeanDefinition.getBeanName() + name);
                    if (dp != null) {
                        return dp.value;
                    } else {
                        return getMetaClass().getProperty(this, name);
                    }
                }
            } else {
                return getMetaClass().getProperty(this, name);
            }
        }
    }

    private GroovyDynamicElementReader createDynamicElementReader(String namespace) {
        XmlReaderContext readerContext = this.groovyDslXmlBeanDefinitionReader.createReaderContext(new DescriptiveResource("Groovy"));
        BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
        boolean decorating = (this.currentBeanDefinition != null);
        if (!decorating) {
            this.currentBeanDefinition = new GroovyBeanDefinitionWrapper(namespace);
        }
        return new GroovyDynamicElementReader(namespace, this.namespaces, delegate, this.currentBeanDefinition, decorating) {

            @Override
            protected void afterInvocation() {
                if (!this.decorating) {
                    currentBeanDefinition = null;
                }
            }
        };
    }

    /**
     * 该类用于延迟将属性添加到bean定义中。这在以下情况下使用：在赋值时，属性被分配给一个可能不包含bean引用的列表，但以后可能会包含；因此，它需要被管理。
     */
    private static class DeferredProperty {

        private final GroovyBeanDefinitionWrapper beanDefinition;

        private final String name;

        public Object value;

        public DeferredProperty(GroovyBeanDefinitionWrapper beanDefinition, String name, Object value) {
            this.beanDefinition = beanDefinition;
            this.name = name;
            this.value = value;
        }

        public void apply() {
            this.beanDefinition.addProperty(this.name, this.value);
        }
    }

    /**
     * 一个RuntimeBeanReference，负责管理向运行时引用添加新属性。
     */
    private class GroovyRuntimeBeanReference extends RuntimeBeanReference implements GroovyObject {

        private final GroovyBeanDefinitionWrapper beanDefinition;

        private MetaClass metaClass;

        public GroovyRuntimeBeanReference(String beanName, GroovyBeanDefinitionWrapper beanDefinition, boolean toParent) {
            super(beanName, toParent);
            this.beanDefinition = beanDefinition;
            this.metaClass = InvokerHelper.getMetaClass(this);
        }

        @Override
        public MetaClass getMetaClass() {
            return this.metaClass;
        }

        @Override
        public Object getProperty(String property) {
            if (property.equals("beanName")) {
                return getBeanName();
            } else if (property.equals("source")) {
                return getSource();
            } else if (this.beanDefinition != null) {
                return new GroovyPropertyValue(property, this.beanDefinition.getBeanDefinition().getPropertyValues().get(property));
            } else {
                return this.metaClass.getProperty(this, property);
            }
        }

        @Override
        public Object invokeMethod(String name, Object args) {
            return this.metaClass.invokeMethod(this, name, args);
        }

        @Override
        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Override
        public void setProperty(String property, Object newValue) {
            if (!addDeferredProperty(property, newValue)) {
                this.beanDefinition.getBeanDefinition().getPropertyValues().add(property, newValue);
            }
        }

        /**
         * 包装一个Bean定义属性，并确保对该属性添加的任何RuntimeBeanReference延迟处理，以便稍后解决。
         */
        private class GroovyPropertyValue extends GroovyObjectSupport {

            private final String propertyName;

            private final Object propertyValue;

            public GroovyPropertyValue(String propertyName, Object propertyValue) {
                this.propertyName = propertyName;
                this.propertyValue = propertyValue;
            }

            @SuppressWarnings("unused")
            public void leftShift(Object value) {
                InvokerHelper.invokeMethod(this.propertyValue, "leftShift", value);
                updateDeferredProperties(value);
            }

            @SuppressWarnings("unused")
            public boolean add(Object value) {
                boolean retVal = (Boolean) InvokerHelper.invokeMethod(this.propertyValue, "add", value);
                updateDeferredProperties(value);
                return retVal;
            }

            @SuppressWarnings("unused")
            public boolean addAll(Collection<?> values) {
                boolean retVal = (Boolean) InvokerHelper.invokeMethod(this.propertyValue, "addAll", values);
                for (Object value : values) {
                    updateDeferredProperties(value);
                }
                return retVal;
            }

            @Override
            public Object invokeMethod(String name, Object args) {
                return InvokerHelper.invokeMethod(this.propertyValue, name, args);
            }

            @Override
            public Object getProperty(String name) {
                return InvokerHelper.getProperty(this.propertyValue, name);
            }

            @Override
            public void setProperty(String name, Object value) {
                InvokerHelper.setProperty(this.propertyValue, name, value);
            }

            private void updateDeferredProperties(Object value) {
                if (value instanceof RuntimeBeanReference) {
                    deferredProperties.put(beanDefinition.getBeanName(), new DeferredProperty(beanDefinition, this.propertyName, this.propertyValue));
                }
            }
        }
    }
}
