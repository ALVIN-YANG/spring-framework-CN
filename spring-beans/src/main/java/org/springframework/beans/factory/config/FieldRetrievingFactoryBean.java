// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能仅在不违反许可证的情况下使用此文件。
* 您可以在以下链接获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照“现状”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权性的保证。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.config;

import java.lang.reflect.Field;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 *  实现 {@link FactoryBean}，用于获取静态或非静态字段值。
 *
 * <p>通常用于获取公共静态final常量。使用示例：
 *
 * <pre class="code">
 * // 标准定义，用于暴露静态字段，指定“staticField”属性
 * &lt;bean id="myField" class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean"&gt;
 *   &lt;property name="staticField" value="java.sql.Connection.TRANSACTION_SERIALIZABLE"/&gt;
 * &lt;/bean&gt;
 *
 * // 便捷版本，将静态字段模式指定为bean名称
 * &lt;bean id="java.sql.Connection.TRANSACTION_SERIALIZABLE"
 *       class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean"/&gt;
 * </pre>
 *
 * <p>如果您正在使用Spring 2.0，您还可以使用以下配置风格的公共静态字段。
 *
 * <pre class="code">&lt;util:constant static-field="java.sql.Connection.TRANSACTION_SERIALIZABLE"/&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setStaticField
 */
public class FieldRetrievingFactoryBean implements FactoryBean<Object>, BeanNameAware, BeanClassLoaderAware, InitializingBean {

    @Nullable
    private Class<?> targetClass;

    @Nullable
    private Object targetObject;

    @Nullable
    private String targetField;

    @Nullable
    private String staticField;

    @Nullable
    private String beanName;

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    // 我们将检索的字段
    @Nullable
    private Field fieldObject;

    /**
     * 设置定义该字段的目标类。
     * 仅当目标字段是静态的时才必要；否则，
     * 无论如何都需要指定一个目标对象。
     * @see #setTargetObject
     * @see #setTargetField
     */
    public void setTargetClass(@Nullable Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * 返回定义字段的目标类。
     */
    @Nullable
    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    /**
     * 设置定义字段的目标对象。
     * 仅当目标字段不是静态时才需要；
     * 否则，一个目标类就足够了。
     * @see #setTargetClass
     * @see #setTargetField
     */
    public void setTargetObject(@Nullable Object targetObject) {
        this.targetObject = targetObject;
    }

    /**
     * 返回定义该字段的目标对象。
     */
    @Nullable
    public Object getTargetObject() {
        return this.targetObject;
    }

    /**
     * 设置要检索的字段名称。
     * 根据设置的目标对象，可能是指静态字段或非静态字段。
     * @see #setTargetClass
     * @see #setTargetObject
     */
    public void setTargetField(@Nullable String targetField) {
        this.targetField = (targetField != null ? StringUtils.trimAllWhitespace(targetField) : null);
    }

    /**
     * 返回要检索的字段名称。
     */
    @Nullable
    public String getTargetField() {
        return this.targetField;
    }

    /**
     * 设置一个完全限定的静态字段名称以进行检索，
     * 例如："example.MyExampleClass.MY_EXAMPLE_FIELD"。
     * 这是指定targetClass和targetField的便捷替代方案。
     * @see #setTargetClass
     * @see #setTargetField
     */
    public void setStaticField(String staticField) {
        this.staticField = StringUtils.trimAllWhitespace(staticField);
    }

    /**
     * 如果未指定 "targetClass"、"targetObject" 或 "targetField"，则此 FieldRetrievingFactoryBean 的 Bean 名称将被解释为 "staticField" 模式。
     * 这允许使用仅包含 id/名称的简洁 Bean 定义。
     */
    @Override
    public void setBeanName(String beanName) {
        this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void afterPropertiesSet() throws ClassNotFoundException, NoSuchFieldException {
        if (this.targetClass != null && this.targetObject != null) {
            throw new IllegalArgumentException("Specify either targetClass or targetObject, not both");
        }
        if (this.targetClass == null && this.targetObject == null) {
            if (this.targetField != null) {
                throw new IllegalArgumentException("Specify targetClass or targetObject in combination with targetField");
            }
            // 如果没有指定其他属性，则将bean名称视为静态字段表达式。
            if (this.staticField == null) {
                this.staticField = this.beanName;
                Assert.state(this.staticField != null, "No target field specified");
            }
            // 尝试将静态字段解析为类和字段。
            int lastDotIndex = this.staticField.lastIndexOf('.');
            if (lastDotIndex == -1 || lastDotIndex == this.staticField.length()) {
                throw new IllegalArgumentException("staticField must be a fully qualified class plus static field name: " + "e.g. 'example.MyExampleClass.MY_EXAMPLE_FIELD'");
            }
            String className = this.staticField.substring(0, lastDotIndex);
            String fieldName = this.staticField.substring(lastDotIndex + 1);
            this.targetClass = ClassUtils.forName(className, this.beanClassLoader);
            this.targetField = fieldName;
        } else if (this.targetField == null) {
            // 已指定 targetClass 或 targetObject。
            throw new IllegalArgumentException("targetField is required");
        }
        // 尝试首先获取确切的方法。
        Class<?> targetClass = (this.targetObject != null ? this.targetObject.getClass() : this.targetClass);
        this.fieldObject = targetClass.getField(this.targetField);
    }

    @Override
    @Nullable
    public Object getObject() throws IllegalAccessException {
        if (this.fieldObject == null) {
            throw new FactoryBeanNotInitializedException();
        }
        ReflectionUtils.makeAccessible(this.fieldObject);
        if (this.targetObject != null) {
            // 实例字段
            return this.fieldObject.get(this.targetObject);
        } else {
            // class 字段
            return this.fieldObject.get(null);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return (this.fieldObject != null ? this.fieldObject.getType() : null);
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
