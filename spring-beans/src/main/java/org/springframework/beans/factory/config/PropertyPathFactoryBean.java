// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下链接处获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件按 "原样" 提供分发，
* 不提供任何形式的明示或暗示保证或条件。有关许可权限和限制的特定语言，请参阅许可协议。*/
package org.springframework.beans.factory.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 一个评估给定目标对象上的属性路径的 {@link FactoryBean}。
 *
 * <p>目标对象可以直接指定或通过bean名称指定。
 *
 * <p>使用示例：
 *
 * <pre class="code">&lt;!-- 要通过名称引用的目标bean --&gt;
 * &lt;bean id="tb" class="org.springframework.beans.TestBean" singleton="false"&gt;
 *   &lt;property name="age" value="10"/&gt;
 *   &lt;property name="spouse"&gt;
 *     &lt;bean class="org.springframework.beans.TestBean"&gt;
 *       &lt;property name="age" value="11"/&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;!-- 将得到 12，这是内部bean的 'age' 属性的值 --&gt;
 * &lt;bean id="propertyPath1" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"&gt;
 *   &lt;property name="targetObject"&gt;
 *     &lt;bean class="org.springframework.beans.TestBean"&gt;
 *       &lt;property name="age" value="12"/&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 *   &lt;property name="propertyPath" value="age"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;!-- 将得到 11，这是bean 'tb'的 'spouse.age' 属性的值 --&gt;
 * &lt;bean id="propertyPath2" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"&gt;
 *   &lt;property name="targetBeanName" value="tb"/&gt;
 *   &lt;property name="propertyPath" value="spouse.age"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;!-- 将得到 10，这是bean 'tb'的 'age' 属性的值 --&gt;
 * &lt;bean id="tb.age" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"/&gt;</pre>
 *
 * <p>如果您在配置文件中使用Spring 2.0和XML Schema支持，您还可以使用以下风格的配置来访问属性路径。（有关更多示例，请参阅Spring参考手册中名为'基于XML Schema的配置'的附录。）
 *
 * <pre class="code"> &lt;!-- 将得到 10，这是bean 'tb'的 'age' 属性的值 --&gt;
 * &lt;util:property-path id="name" path="testBean.age"/&gt;</pre>
 *
 * 感谢Matthias Ernst的建议和初始原型！
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see #setTargetObject
 * @see #setTargetBeanName
 * @see #setPropertyPath
 */
public class PropertyPathFactoryBean implements FactoryBean<Object>, BeanNameAware, BeanFactoryAware {

    private static final Log logger = LogFactory.getLog(PropertyPathFactoryBean.class);

    @Nullable
    private BeanWrapper targetBeanWrapper;

    @Nullable
    private String targetBeanName;

    @Nullable
    private String propertyPath;

    @Nullable
    private Class<?> resultType;

    @Nullable
    private String beanName;

    @Nullable
    private BeanFactory beanFactory;

    /**
     * 指定要应用属性路径的目标对象。
     * 或者，指定目标Bean名称。
     * @param targetObject 目标对象，例如一个Bean引用或内部Bean
     * @see #setTargetBeanName
     */
    public void setTargetObject(Object targetObject) {
        this.targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(targetObject);
    }

    /**
     * 指定要应用属性路径的目标bean的名称。
     * 或者，直接指定目标对象。
     * @param targetBeanName 在包含的bean工厂中查找的bean名称（例如："testBean"）
     * @see #setTargetObject
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = StringUtils.trimAllWhitespace(targetBeanName);
    }

    /**
     * 指定要应用到目标上的属性路径。
     * @param propertyPath 属性路径，可能嵌套（例如："age" 或 "spouse.age"）
     */
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = StringUtils.trimAllWhitespace(propertyPath);
    }

    /**
     * 指定评估属性路径的结果类型。
     * <p>注意：对于直接指定的目标对象或单例目标Bean，这并非必需，因为类型可以通过反射确定。仅在目标为原型时指定此类型，前提是你需要通过类型匹配（例如，用于自动装配）。
     * @param resultType 结果类型，例如 "java.lang.Integer"
     */
    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    /**
     * 如果没有指定 "targetObject"、"targetBeanName" 或 "propertyPath"，则此 PropertyPathFactoryBean 的 Bean 名称将被解释为 "beanName.property" 模式。
     * 这允许使用仅包含 id/names 的简洁 Bean 定义。
     */
    @Override
    public void setBeanName(String beanName) {
        this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (this.targetBeanWrapper != null && this.targetBeanName != null) {
            throw new IllegalArgumentException("Specify either 'targetObject' or 'targetBeanName', not both");
        }
        if (this.targetBeanWrapper == null && this.targetBeanName == null) {
            if (this.propertyPath != null) {
                throw new IllegalArgumentException("Specify 'targetObject' or 'targetBeanName' in combination with 'propertyPath'");
            }
            // 未指定其他属性：请检查bean名称。
            int dotIndex = (this.beanName != null ? this.beanName.indexOf('.') : -1);
            if (dotIndex == -1) {
                throw new IllegalArgumentException("Neither 'targetObject' nor 'targetBeanName' specified, and PropertyPathFactoryBean " + "bean name '" + this.beanName + "' does not follow 'beanName.property' syntax");
            }
            this.targetBeanName = this.beanName.substring(0, dotIndex);
            this.propertyPath = this.beanName.substring(dotIndex + 1);
        } else if (this.propertyPath == null) {
            // 要么指定了 targetObject，要么指定了 targetBeanName。
            throw new IllegalArgumentException("'propertyPath' is required");
        }
        if (this.targetBeanWrapper == null && this.beanFactory.isSingleton(this.targetBeanName)) {
            // 积极获取单例目标bean，并确定结果类型。
            Object bean = this.beanFactory.getBean(this.targetBeanName);
            this.targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
            this.resultType = this.targetBeanWrapper.getPropertyType(this.propertyPath);
        }
    }

    @Override
    @Nullable
    public Object getObject() throws BeansException {
        BeanWrapper target = this.targetBeanWrapper;
        if (target != null) {
            if (logger.isWarnEnabled() && this.targetBeanName != null && this.beanFactory instanceof ConfigurableBeanFactory cbf && cbf.isCurrentlyInCreation(this.targetBeanName)) {
                logger.warn("Target bean '" + this.targetBeanName + "' is still in creation due to a circular " + "reference - obtained value for property '" + this.propertyPath + "' may be outdated!");
            }
        } else {
            // 获取原型目标 Bean...
            Assert.state(this.beanFactory != null, "No BeanFactory available");
            Assert.state(this.targetBeanName != null, "No target bean name specified");
            Object bean = this.beanFactory.getBean(this.targetBeanName);
            target = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        }
        Assert.state(this.propertyPath != null, "No property path specified");
        return target.getPropertyValue(this.propertyPath);
    }

    @Override
    public Class<?> getObjectType() {
        return this.resultType;
    }

    /**
     * 虽然这个FactoryBean通常用于单例目标，
     * 但是调用属性路径的getter方法可能会为每次调用返回一个新的对象，
     * 因此我们必须假设在每次调用{@link #getObject()}时不会返回同一个对象。
     */
    @Override
    public boolean isSingleton() {
        return false;
    }
}
