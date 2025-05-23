// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明示或暗示。有关许可的具体语言、权限和限制，
* 请参阅许可证。*/
package org.springframework.beans.factory.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.lang.Nullable;

/**
 *  实现了 {@link FactoryBean} 接口，通过调用静态方法或实例方法返回一个值。对于大多数用例，直接使用容器内建的工厂方法支持可能更好，因为这样可以更智能地转换参数。尽管如此，当需要调用不返回任何值的方法（例如，用于强制执行某种初始化的静态类方法）时，这个工厂bean仍然很有用。由于需要返回值以获取bean实例，因此这种用例不支持工厂方法。
 *
 * <p>请注意，由于预期主要用于访问工厂方法，这个工厂默认以 <b>单例</b> 模式运行。第一次由拥有bean工厂调用 {@link #getObject} 会导致方法调用，其返回值将被缓存以供后续请求使用。可以通过设置内部 {@link #setSingleton singleton} 属性为 "false"，使这个工厂每次被请求对象时都调用目标方法。
 *
 * <p><b>注意：如果目标方法不产生要公开的结果，请考虑使用 {@link MethodInvokingBean}，它避免了此 {@link MethodInvokingFactoryBean} 带来的类型确定和生命周期限制。</b>
 *
 * <p>此调用器支持任何类型的目标方法。可以通过将 {@link #setTargetMethod targetMethod} 属性设置为表示静态方法名称的字符串来指定静态方法，并通过设置 {@link #setTargetClass targetClass} 指定定义静态方法 的类。或者，可以通过设置 {@link #setTargetObject targetObject} 属性为目标对象，将 {@link #setTargetMethod targetMethod} 属性设置为要调用的目标对象上的方法名称来指定目标实例方法。可以通过设置 {@link #setArguments arguments} 属性来指定方法调用的参数。
 *
 * <p>此类依赖于按照 InitializingBean 协议调用一次所有属性都设置后的 {@link #afterPropertiesSet()}。
 *
 * <p>以下是一个使用此类调用静态工厂方法的bean定义示例（基于XML的bean工厂定义）：
 *
 * <pre class="code">
 * &lt;bean id="myObject" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
 *   &lt;property name="staticMethod" value="com.whatever.MyClassFactory.getInstance"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>以下是一个调用静态方法然后实例方法以获取Java系统属性的示例。虽然有些啰嗦，但它是有效的。
 *
 * <pre class="code">
 * &lt;bean id="sysProps" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
 *   &lt;property name="targetClass" value="java.lang.System"/&gt;
 *   &lt;property name="targetMethod" value="getProperties"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="javaVersion" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
 *   &lt;property name="targetObject" ref="sysProps"/&gt;
 *   &lt;property name="targetMethod" value="getProperty"/&gt;
 *   &lt;property name="arguments" value="java.version"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 21.11.2003
 * @see MethodInvokingBean
 * @see org.springframework.util.MethodInvoker
 */
public class MethodInvokingFactoryBean extends MethodInvokingBean implements FactoryBean<Object> {

    private boolean singleton = true;

    private boolean initialized = false;

    /**
     * 单例模式中的方法调用结果。
     */
    @Nullable
    private Object singletonObject;

    /**
     * 设置是否创建单例，或者在每个
     * `#getObject()` 请求时创建一个新的对象。默认值为 "true"。
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        prepare();
        if (this.singleton) {
            this.initialized = true;
            this.singletonObject = invokeWithTargetException();
        }
    }

    /**
     * 如果将单例属性设置为 "true"，则每次都返回相同的值；否则，在运行时调用指定方法并返回其返回值。
     */
    @Override
    @Nullable
    public Object getObject() throws Exception {
        if (this.singleton) {
            if (!this.initialized) {
                throw new FactoryBeanNotInitializedException();
            }
            // 单例：返回共享对象。
            return this.singletonObject;
        } else {
            // 原型：每次调用时创建一个新对象。
            return invokeWithTargetException();
        }
    }

    /**
     * 返回此FactoryBean创建的对象类型，
     * 或如果事先不知道，则返回{@code null}。
     */
    @Override
    public Class<?> getObjectType() {
        if (!isPrepared()) {
            // 尚未完全初始化 -> 返回 null 以指示“尚不知道”。
            return null;
        }
        return getPreparedMethod().getReturnType();
    }

    @Override
    public boolean isSingleton() {
        return this.singleton;
    }
}
