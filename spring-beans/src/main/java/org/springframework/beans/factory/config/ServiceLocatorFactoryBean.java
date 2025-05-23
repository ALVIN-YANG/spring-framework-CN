// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"），除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或经书面同意，否则在许可证下分发的软件按"原样"提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
* 请参阅许可证了解具体管理许可权和限制的条款。*/
package org.springframework.beans.factory.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 *  一个实现了 {@link FactoryBean} 的类，它接受一个接口，该接口必须有一个或多个具有签名 {@code MyType xxx()} 或 {@code MyType xxx(MyIdType id)}（通常是 {@code MyService getService()} 或 {@code MyService getService(String id)}) 的方法，并创建一个实现该接口的动态代理，该代理将委托给底层的 {@link org.springframework.beans.factory.BeanFactory}。
 *
 *  <p>这种服务定位器允许通过使用适当的自定义定位器接口将调用代码与 {@link org.springframework.beans.factory.BeanFactory} API 解耦。它们通常用于 <b>原型bean</b>，即对于每次调用都应返回一个新实例的工厂方法。客户端通过setter或构造函数注入接收服务定位器的引用，以便能够在需要时调用定位器的工厂方法。<b>对于单例bean，直接setter或构造函数注入目标bean更可取。</b>
 *
 *  <p>在调用无参工厂方法或单参工厂方法（带有null或空字符串的String id）时，如果工厂中恰好有一个bean与工厂方法的返回类型匹配，则返回该bean，否则抛出 {@link org.springframework.beans.factory.NoSuchBeanDefinitionException}。
 *
 *  <p>在调用带有非null（且非空）参数的单参工厂方法时，代理返回一个使用传入id的字符串表示形式作为bean名称的 {@link org.springframework.beans.factory.BeanFactory#getBean(String)} 调用的结果。
 *
 *  <p>工厂方法参数通常是一个String，但也可以是int或自定义枚举类型，例如，通过 {@code toString} 转换。生成的String可以直接用作bean名称，前提是bean工厂中定义了相应的bean。或者，可以定义服务ID和bean名称之间的自定义映射。
 *
 *  <p>以下是一个示例服务定位器接口。
 *  注意，此接口不依赖于任何Spring API。
 *
 *  <pre class="code">package a.b.c;
 *
 * public interface ServiceFactory {
 *
 *     public MyService getService();
 * }</pre>
 *
 *  <p>在基于XML的 {@link org.springframework.beans.factory.BeanFactory} 中的配置示例可能如下所示：
 *
 *  <pre class="code">&lt;beans&gt;
 *
 *    &lt;!-- 原型bean，因为我们有状态 --&gt;
 *    &lt;bean id="myService" class="a.b.c.MyService" singleton="false"/&gt;
 *
 *    &lt;!-- 将通过 *类型* 查找上面的 'myService' bean --&gt;
 *    &lt;bean id="myServiceFactory"
 *             class="org.springframework.beans.factory.config.ServiceLocatorFactoryBean"&gt;
 *      &lt;property name="serviceLocatorInterface" value="a.b.c.ServiceFactory"/&gt;
 *    &lt;/bean&gt;
 *
 *    &lt;bean id="clientBean" class="a.b.c.MyClientBean"&gt;
 *      &lt;property name="myServiceFactory" ref="myServiceFactory"/&gt;
 *    &lt;/bean&gt;
 *
 * &lt;/beans&gt;</pre>
 *
 *  <p>相应的 {@code MyClientBean} 类实现可能如下所示：
 *
 *  <pre class="code">package a.b.c;
 *
 * public class MyClientBean {
 *
 *     private ServiceFactory myServiceFactory;
 *
 *     // 由Spring容器提供实际实现
 *     public void setServiceFactory(ServiceFactory myServiceFactory) {
 *         this.myServiceFactory = myServiceFactory;
 *     }
 *
 *     public void someBusinessMethod() {
 *         // 获取一个全新的MyService实例
 *         MyService service = this.myServiceFactory.getService();
 *         // 使用服务对象来执行业务逻辑...
 *     }
 * }</pre>
 *
 *  <p>以下是一个通过 <b>名称</b> 查找bean的示例，考虑以下服务定位器接口。同样，请注意，此接口不依赖于任何Spring API。
 *
 *  <pre class="code">package a.b.c;
 *
 * public interface ServiceFactory {
 *
 *     public MyService getService (String serviceName);
 * }</pre>
 *
 *  <p>在基于XML的 {@link org.springframework.beans.factory.BeanFactory} 中的配置示例可能如下所示：
 *
 *  <pre class="code">&lt;beans&gt;
 *
 *    &lt;!-- 原型bean，因为我们有状态（都扩展了MyService） --&gt;
 *    &lt;bean id="specialService" class="a.b.c.SpecialService" singleton="false"/&gt;
 *    &lt;bean id="anotherService" class="a.b.c.AnotherService" singleton="false"/&gt;
 *
 *    &lt;bean id="myServiceFactory"
 *             class="org.springframework.beans.factory.config.ServiceLocatorFactoryBean"&gt;
 *      &lt;property name="serviceLocatorInterface" value="a.b.c.ServiceFactory"/&gt;
 *    &lt;/bean&gt;
 *
 *    &lt;bean id="clientBean" class="a.b.c.MyClientBean"&gt;
 *      &lt;property name="myServiceFactory" ref="myServiceFactory"/&gt;
 *    &lt;/bean&gt;
 *
 * &lt;/beans&gt;</pre>
 *
 *  <p>相应的 {@code MyClientBean} 类实现可能如下所示：
 *
 *  <pre class="code">package a.b.c;
 *
 * public class MyClientBean {
 *
 *     private ServiceFactory myServiceFactory;
 *
 *     // 由Spring容器提供实际实现
 *     public void setServiceFactory(ServiceFactory myServiceFactory) {
 *         this.myServiceFactory = myServiceFactory;
 *     }
 *
 *     public void someBusinessMethod() {
 *         // 获取一个全新的MyService实例
 *         MyService service = this.myServiceFactory.getService("specialService");
 *         // 使用服务对象来执行业务逻辑...
 *     }
 *
 *     public void anotherBusinessMethod() {
 *         // 获取一个全新的MyService实例
 *         MyService service = this.myServiceFactory.getService("anotherService");
 *         // 使用服务对象来执行业务逻辑...
 *     }
 * }</pre>
 *
 *  <p>有关另一种方法的示例，请参阅 {@link ObjectFactoryCreatingFactoryBean}。
 *
 *  @author Colin Sampaleanu
 *  @author Juergen Hoeller
 *  @since 1.1.4
 *  @see #setServiceLocatorInterface
 *  @see #setServiceMappings
 *  @see ObjectFactoryCreatingFactoryBean
 */
public class ServiceLocatorFactoryBean implements FactoryBean<Object>, BeanFactoryAware, InitializingBean {

    @Nullable
    private Class<?> serviceLocatorInterface;

    @Nullable
    private Constructor<Exception> serviceLocatorExceptionConstructor;

    @Nullable
    private Properties serviceMappings;

    @Nullable
    private ListableBeanFactory beanFactory;

    @Nullable
    private Object proxy;

    /**
     * 设置要使用的服务定位器接口，该接口必须有一个或多个签名符合以下之一的`MyType xxx()`或`MyType xxx(MyIdType id)`的方法（通常为`MyService getService()`或`MyService getService(String id)`）。
     * 关于此类方法的语义信息，请参阅`ServiceLocatorFactoryBean`类的Javadoc。
     */
    public void setServiceLocatorInterface(Class<?> interfaceType) {
        this.serviceLocatorInterface = interfaceType;
    }

    /**
     * 设置服务定位器在服务查找失败时应抛出的异常类。指定的异常类必须有一个构造函数，其参数类型为以下之一：{@code (String, Throwable)}、{@code (Throwable)}或{@code (String)}。
     * <p>如果没有指定，将抛出Spring的BeansException的子类，例如NoSuchBeanDefinitionException。由于这些是未检查的异常，调用者不需要处理它们，因此只要它们被泛型处理，抛出Spring异常可能是可以接受的。
     * @see #determineServiceLocatorExceptionConstructor
     * @see #createServiceLocatorException
     */
    public void setServiceLocatorExceptionClass(Class<? extends Exception> serviceLocatorExceptionClass) {
        this.serviceLocatorExceptionConstructor = determineServiceLocatorExceptionConstructor(serviceLocatorExceptionClass);
    }

    /**
     * 设置服务ID（通过服务定位器传入）与bean名称（在bean工厂中）之间的映射。未在此定义的服务ID将被视为原样的bean名称。
     * <p>空字符串作为服务ID键定义了对于`null`和空字符串，以及没有参数的工厂方法的映射。如果未定义，将从一个bean工厂中检索单个匹配的bean。
     * @param serviceMappings 服务ID与bean名称之间的映射，以服务ID作为键，以bean名称作为值
     */
    public void setServiceMappings(Properties serviceMappings) {
        this.serviceMappings = serviceMappings;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ListableBeanFactory lbf)) {
            throw new FatalBeanException("ServiceLocatorFactoryBean needs to run in a BeanFactory that is a ListableBeanFactory");
        }
        this.beanFactory = lbf;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.serviceLocatorInterface == null) {
            throw new IllegalArgumentException("Property 'serviceLocatorInterface' is required");
        }
        // 创建服务定位器代理。
        this.proxy = Proxy.newProxyInstance(this.serviceLocatorInterface.getClassLoader(), new Class<?>[] { this.serviceLocatorInterface }, new ServiceLocatorInvocationHandler());
    }

    /**
     * 确定用于给定服务定位器异常类的构造函数。仅在自定义服务定位器异常的情况下调用。
     * <p>默认实现会查找具有以下参数类型的构造函数之一：{@code (String, Throwable)}
     * 或 {@code (Throwable)} 或 {@code (String)}。
     * @param exceptionClass 异常类
     * @return 要使用的构造函数
     * @see #setServiceLocatorExceptionClass
     */
    @SuppressWarnings("unchecked")
    protected Constructor<Exception> determineServiceLocatorExceptionConstructor(Class<? extends Exception> exceptionClass) {
        try {
            return (Constructor<Exception>) exceptionClass.getConstructor(String.class, Throwable.class);
        } catch (NoSuchMethodException ex) {
            try {
                return (Constructor<Exception>) exceptionClass.getConstructor(Throwable.class);
            } catch (NoSuchMethodException ex2) {
                try {
                    return (Constructor<Exception>) exceptionClass.getConstructor(String.class);
                } catch (NoSuchMethodException ex3) {
                    throw new IllegalArgumentException("Service locator exception [" + exceptionClass.getName() + "] neither has a (String, Throwable) constructor nor a (String) constructor");
                }
            }
        }
    }

    /**
     * 为给定的原因创建一个服务定位器异常。
     * 仅在自定义服务定位器异常的情况下调用。
     * <p>默认实现可以处理所有消息和异常参数的变体。
     * @param exceptionConstructor 要使用的构造函数
     * @param cause 服务查找失败的原因
     * @return 要抛出的服务定位器异常
     * @see #setServiceLocatorExceptionClass
     */
    protected Exception createServiceLocatorException(Constructor<Exception> exceptionConstructor, BeansException cause) {
        Class<?>[] paramTypes = exceptionConstructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (String.class == paramTypes[i]) {
                args[i] = cause.getMessage();
            } else if (paramTypes[i].isInstance(cause)) {
                args[i] = cause;
            }
        }
        return BeanUtils.instantiateClass(exceptionConstructor, args);
    }

    @Override
    @Nullable
    public Object getObject() {
        return this.proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceLocatorInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * 调用处理器，将服务定位器调用委派给Bean工厂。
     */
    private class ServiceLocatorInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ReflectionUtils.isEqualsMethod(method)) {
                // 仅当代理完全相同的时候才考虑相等。
                return (proxy == args[0]);
            } else if (ReflectionUtils.isHashCodeMethod(method)) {
                // 使用服务定位器代理的hashCode。
                return System.identityHashCode(proxy);
            } else if (ReflectionUtils.isToStringMethod(method)) {
                return "Service locator: " + serviceLocatorInterface;
            } else {
                return invokeServiceLocatorMethod(method, args);
            }
        }

        private Object invokeServiceLocatorMethod(Method method, Object[] args) throws Exception {
            Class<?> serviceLocatorMethodReturnType = getServiceLocatorMethodReturnType(method);
            try {
                String beanName = tryGetBeanName(args);
                Assert.state(beanFactory != null, "No BeanFactory available");
                if (StringUtils.hasLength(beanName)) {
                    // 特定Bean名称的服务定位器
                    return beanFactory.getBean(beanName, serviceLocatorMethodReturnType);
                } else {
                    // 服务定位器，用于 bean 类型
                    return beanFactory.getBean(serviceLocatorMethodReturnType);
                }
            } catch (BeansException ex) {
                if (serviceLocatorExceptionConstructor != null) {
                    throw createServiceLocatorException(serviceLocatorExceptionConstructor, ex);
                }
                throw ex;
            }
        }

        /**
         * 检查是否传入了服务ID。
         */
        private String tryGetBeanName(@Nullable Object[] args) {
            String beanName = "";
            if (args != null && args.length == 1 && args[0] != null) {
                beanName = args[0].toString();
            }
            // 寻找显式的serviceId到beanName的映射。
            if (serviceMappings != null) {
                String mappedName = serviceMappings.getProperty(beanName);
                if (mappedName != null) {
                    beanName = mappedName;
                }
            }
            return beanName;
        }

        private Class<?> getServiceLocatorMethodReturnType(Method method) throws NoSuchMethodException {
            Assert.state(serviceLocatorInterface != null, "No service locator interface specified");
            Class<?>[] paramTypes = method.getParameterTypes();
            Method interfaceMethod = serviceLocatorInterface.getMethod(method.getName(), paramTypes);
            Class<?> serviceLocatorReturnType = interfaceMethod.getReturnType();
            // 检查该方法是否是一个有效的服务定位器。
            if (paramTypes.length > 1 || void.class == serviceLocatorReturnType) {
                throw new UnsupportedOperationException("May only call methods with signature '<type> xxx()' or '<type> xxx(<idtype> id)' " + "on factory interface, but tried to call: " + interfaceMethod);
            }
            return serviceLocatorReturnType;
        }
    }
}
