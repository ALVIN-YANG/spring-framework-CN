// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework;

import java.io.Closeable;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 基础类，为代理处理器提供通用功能，特别是
 * 类加载器管理和 {@link #evaluateProxyInterfaces} 算法。
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see AbstractAdvisingBeanPostProcessor
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 */
@SuppressWarnings("serial")
public class ProxyProcessorSupport extends ProxyConfig implements Ordered, BeanClassLoaderAware, AopInfrastructureBean {

    /**
     * 这个方法应该在所有其他处理器之后运行，以便它可以直接向现有的代理添加顾问，而不是双重代理。
     */
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Nullable
    private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

    private boolean classLoaderConfigured = false;

    /**
     * 设置将应用于此处理器实现中的排序，用于应用多个处理器时。
     * <p>默认值为 {@code Ordered.LOWEST_PRECEDENCE}，表示非有序。
     * @param order 排序值
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 设置用于生成代理类的类加载器。
     * <p>默认为bean类加载器，即由包含的{@link org.springframework.beans.factory.BeanFactory}用于加载所有bean类的类加载器。
     * 这里可以覆盖默认值以针对特定代理进行设置。
     */
    public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
        this.proxyClassLoader = classLoader;
        this.classLoaderConfigured = (classLoader != null);
    }

    /**
     * 返回此处理器的配置代理类加载器。
     */
    @Nullable
    protected ClassLoader getProxyClassLoader() {
        return this.proxyClassLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        if (!this.classLoaderConfigured) {
            this.proxyClassLoader = classLoader;
        }
    }

    /**
     * 检查给定Bean类的接口，并在适当的情况下应用它们到 {@link ProxyFactory}。
     * <p>调用 {@link #isConfigurationCallbackInterface} 和 {@link #isInternalLanguageInterface} 来过滤合理的代理接口，否则回退到目标类代理。
     * @param beanClass Bean的类
     * @param proxyFactory Bean的ProxyFactory
     */
    protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
        Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, getProxyClassLoader());
        boolean hasReasonableProxyInterface = false;
        for (Class<?> ifc : targetInterfaces) {
            if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) && ifc.getMethods().length > 0) {
                hasReasonableProxyInterface = true;
                break;
            }
        }
        if (hasReasonableProxyInterface) {
            // 必须允许进行自我介绍；不能仅仅将接口设置为目标的接口。
            for (Class<?> ifc : targetInterfaces) {
                proxyFactory.addInterface(ifc);
            }
        } else {
            proxyFactory.setProxyTargetClass(true);
        }
    }

    /**
     * 判断给定的接口是否仅是容器回调，因此不应被视为合理的代理接口。
     * <p>如果找不到给定bean的合理代理接口，它将使用其完整的目标类进行代理，假设这是用户的意图。
     * @param ifc 要检查的接口
     * @return 给定的接口是否仅是容器回调
     */
    protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
        return (InitializingBean.class == ifc || DisposableBean.class == ifc || Closeable.class == ifc || AutoCloseable.class == ifc || ObjectUtils.containsElement(ifc.getInterfaces(), Aware.class));
    }

    /**
     * 判断给定的接口是否为已知内部语言接口，因此不应被视为合理的代理接口。
     * <p>如果对于给定的bean没有找到合理的代理接口，它将被代理为其完整的目标类，假设这是用户的意图。
     * @param ifc 要检查的接口
     * @return 给定的接口是否为内部语言接口
     */
    protected boolean isInternalLanguageInterface(Class<?> ifc) {
        return (ifc.getName().equals("groovy.lang.GroovyObject") || ifc.getName().endsWith(".cglib.proxy.Factory") || ifc.getName().endsWith(".bytebuddy.MockAccess"));
    }
}
