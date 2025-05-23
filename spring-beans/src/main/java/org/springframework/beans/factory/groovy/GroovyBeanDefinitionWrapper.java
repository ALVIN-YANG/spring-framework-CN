// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则根据许可证分发的软件按“原样”提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、适用于特定目的和不侵犯他人权利。
* 请参阅许可证以了解具体的管理权限和限制。*/
package org.springframework.beans.factory.groovy;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import groovy.lang.GroovyObjectSupport;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Spring BeanDefinition 的内部包装器，允许在 {@link GroovyBeanDefinitionReader} 的闭包中实现 Groovy 风格的属性访问。
 *
 * @author Jeff Brown
 * @author Juergen Hoeller
 * @since 4.0
 */
class GroovyBeanDefinitionWrapper extends GroovyObjectSupport {

    private static final String PARENT = "parent";

    private static final String AUTOWIRE = "autowire";

    private static final String CONSTRUCTOR_ARGS = "constructorArgs";

    private static final String FACTORY_BEAN = "factoryBean";

    private static final String FACTORY_METHOD = "factoryMethod";

    private static final String INIT_METHOD = "initMethod";

    private static final String DESTROY_METHOD = "destroyMethod";

    private static final String SINGLETON = "singleton";

    private static final Set<String> dynamicProperties = Set.of(PARENT, AUTOWIRE, CONSTRUCTOR_ARGS, FACTORY_BEAN, FACTORY_METHOD, INIT_METHOD, DESTROY_METHOD, SINGLETON);

    @Nullable
    private String beanName;

    @Nullable
    private final Class<?> clazz;

    @Nullable
    private final Collection<?> constructorArgs;

    @Nullable
    private AbstractBeanDefinition definition;

    @Nullable
    private BeanWrapper definitionWrapper;

    @Nullable
    private String parentName;

    GroovyBeanDefinitionWrapper(String beanName) {
        this(beanName, null);
    }

    GroovyBeanDefinitionWrapper(@Nullable String beanName, @Nullable Class<?> clazz) {
        this(beanName, clazz, null);
    }

    GroovyBeanDefinitionWrapper(@Nullable String beanName, Class<?> clazz, @Nullable Collection<?> constructorArgs) {
        this.beanName = beanName;
        this.clazz = clazz;
        this.constructorArgs = constructorArgs;
    }

    @Nullable
    public String getBeanName() {
        return this.beanName;
    }

    void setBeanDefinition(AbstractBeanDefinition definition) {
        this.definition = definition;
    }

    AbstractBeanDefinition getBeanDefinition() {
        if (this.definition == null) {
            this.definition = createBeanDefinition();
        }
        return this.definition;
    }

    protected AbstractBeanDefinition createBeanDefinition() {
        AbstractBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(this.clazz);
        if (!CollectionUtils.isEmpty(this.constructorArgs)) {
            ConstructorArgumentValues cav = new ConstructorArgumentValues();
            for (Object constructorArg : this.constructorArgs) {
                cav.addGenericArgumentValue(constructorArg);
            }
            bd.setConstructorArgumentValues(cav);
        }
        if (this.parentName != null) {
            bd.setParentName(this.parentName);
        }
        this.definitionWrapper = new BeanWrapperImpl(bd);
        return bd;
    }

    void setBeanDefinitionHolder(BeanDefinitionHolder holder) {
        this.definition = (AbstractBeanDefinition) holder.getBeanDefinition();
        this.beanName = holder.getBeanName();
    }

    BeanDefinitionHolder getBeanDefinitionHolder() {
        return new BeanDefinitionHolder(getBeanDefinition(), getBeanName());
    }

    void setParent(Object obj) {
        Assert.notNull(obj, "Parent bean cannot be set to a null runtime bean reference.");
        if (obj instanceof String name) {
            this.parentName = name;
        } else if (obj instanceof RuntimeBeanReference runtimeBeanReference) {
            this.parentName = runtimeBeanReference.getBeanName();
        } else if (obj instanceof GroovyBeanDefinitionWrapper wrapper) {
            this.parentName = wrapper.getBeanName();
        }
        getBeanDefinition().setParentName(this.parentName);
        getBeanDefinition().setAbstract(false);
    }

    GroovyBeanDefinitionWrapper addProperty(String propertyName, Object propertyValue) {
        if (propertyValue instanceof GroovyBeanDefinitionWrapper wrapper) {
            propertyValue = wrapper.getBeanDefinition();
        }
        getBeanDefinition().getPropertyValues().add(propertyName, propertyValue);
        return this;
    }

    @Override
    public Object getProperty(String property) {
        Assert.state(this.definitionWrapper != null, "BeanDefinition wrapper not initialized");
        if (this.definitionWrapper.isReadableProperty(property)) {
            return this.definitionWrapper.getPropertyValue(property);
        } else if (dynamicProperties.contains(property)) {
            return null;
        }
        return super.getProperty(property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        if (PARENT.equals(property)) {
            setParent(newValue);
        } else {
            AbstractBeanDefinition bd = getBeanDefinition();
            Assert.state(this.definitionWrapper != null, "BeanDefinition wrapper not initialized");
            if (AUTOWIRE.equals(property)) {
                if ("byName".equals(newValue)) {
                    bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
                } else if ("byType".equals(newValue)) {
                    bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                } else if ("constructor".equals(newValue)) {
                    bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
                } else if (Boolean.TRUE.equals(newValue)) {
                    bd.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
                }
            } else // 构造函数参数
            if (CONSTRUCTOR_ARGS.equals(property) && newValue instanceof List<?> args) {
                ConstructorArgumentValues cav = new ConstructorArgumentValues();
                for (Object arg : args) {
                    cav.addGenericArgumentValue(arg);
                }
                bd.setConstructorArgumentValues(cav);
            } else // factoryBean此单词在Java编程中通常指的是“Factory Bean”，即工厂Bean。在Spring框架中，工厂Bean是一种特殊的Bean，它不是直接返回一个目标对象，而是返回一个代理对象，这个代理对象会在运行时动态地返回一个实际的对象实例。以下是关于工厂Bean的代码注释的中文翻译示例：```java/** * 这是一个工厂Bean，它不是直接返回目标对象实例， * 而是返回一个代理对象，该代理对象在运行时会动态地 * 创建并返回实际的目标对象实例。 */public class MyFactoryBean implements FactoryBean<MyTargetObject> {    // 实现创建目标对象的方法    @Override    public MyTargetObject getObject() throws Exception {        // 创建并返回目标对象实例        return new MyTargetObject();    }    // 返回Bean的类型，通常是目标对象类型    @Override    public Class<?> getObjectType() {        return MyTargetObject.class;    }    // 是否单例，返回true表示工厂Bean是单例的    @Override    public boolean isSingleton() {        return true;    }}```
            if (FACTORY_BEAN.equals(property)) {
                if (newValue != null) {
                    bd.setFactoryBeanName(newValue.toString());
                }
            } else // 工厂方法
            if (FACTORY_METHOD.equals(property)) {
                if (newValue != null) {
                    bd.setFactoryMethodName(newValue.toString());
                }
            } else // 初始化方法
            if (INIT_METHOD.equals(property)) {
                if (newValue != null) {
                    bd.setInitMethodName(newValue.toString());
                }
            } else // destroyMethod（没有注释内容，无法翻译）
            if (DESTROY_METHOD.equals(property)) {
                if (newValue != null) {
                    bd.setDestroyMethodName(newValue.toString());
                }
            } else // 单例属性
            if (SINGLETON.equals(property)) {
                bd.setScope(Boolean.TRUE.equals(newValue) ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
            } else if (this.definitionWrapper.isWritableProperty(property)) {
                this.definitionWrapper.setPropertyValue(property, newValue);
            } else {
                super.setProperty(property, newValue);
            }
        }
    }
}
