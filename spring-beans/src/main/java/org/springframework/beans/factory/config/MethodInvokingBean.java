// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.beans.factory.config;

import java.lang.reflect.InvocationTargetException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 简单的方法调用器 Bean：仅调用目标方法，不期望将结果暴露给容器（与 {@link MethodInvokingFactoryBean} 相比）。
 *
 * <p>此调用器支持任何类型的目标方法。可以通过设置 {@link #setTargetMethod targetMethod} 属性为一个表示静态方法名称的字符串来指定静态方法，并通过设置 {@link #setTargetClass targetClass} 来指定定义静态方法的类。或者，可以通过设置 {@link #setTargetObject targetObject} 属性为目标对象，以及将 {@link #setTargetMethod targetMethod} 属性设置为要在该目标对象上调用的方法名称来指定目标实例方法。方法调用的参数可以通过设置 {@link #setArguments arguments} 属性来指定。
 *
 * <p>此类依赖于按照 InitializingBean 协议，在所有属性都已设置后调用一次 {@link #afterPropertiesSet()}。
 *
 * <p>以下是一个使用此类调用静态初始化方法的 Bean 定义示例（基于 XML 的 Bean 工厂定义）：
 *
 * <pre class="code">
 * &lt;bean id="myObject" class="org.springframework.beans.factory.config.MethodInvokingBean"&gt;
 *   &lt;property name="staticMethod" value="com.whatever.MyClass.init"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>以下是一个调用实例方法以启动某些服务器 Bean 的示例：
 *
 * <pre class="code">
 * &lt;bean id="myStarter" class="org.springframework.beans.factory.config.MethodInvokingBean"&gt;
 *   &lt;property name="targetObject" ref="myServer"/&gt;
 *   &lt;property name="targetMethod" value="start"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 4.0.3
 * @see MethodInvokingFactoryBean
 * @see org.springframework.util.MethodInvoker
 */
public class MethodInvokingBean extends ArgumentConvertingMethodInvoker implements BeanClassLoaderAware, BeanFactoryAware, InitializingBean {

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    @Nullable
    private ConfigurableBeanFactory beanFactory;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClassName(String className) throws ClassNotFoundException {
        return ClassUtils.forName(className, this.beanClassLoader);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory cbf) {
            this.beanFactory = cbf;
        }
    }

    /**
     * 从运行此bean的BeanFactory中获取TypeConverter，如果可能的话。
     * @see ConfigurableBeanFactory#getTypeConverter()
     */
    @Override
    protected TypeConverter getDefaultTypeConverter() {
        if (this.beanFactory != null) {
            return this.beanFactory.getTypeConverter();
        } else {
            return super.getDefaultTypeConverter();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        prepare();
        invokeWithTargetException();
    }

    /**
     * 执行调用并将InvocationTargetException转换为底层的目标异常。
     */
    @Nullable
    protected Object invokeWithTargetException() throws Exception {
        try {
            return invoke();
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof Exception exception) {
                throw exception;
            }
            if (ex.getTargetException() instanceof Error error) {
                throw error;
            }
            throw ex;
        }
    }
}
