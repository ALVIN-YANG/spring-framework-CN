// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）
* 使用本文件应遵守许可证的规定；除非法律要求或书面同意，
* 否则不得使用本文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律要求或书面同意，
* 否则根据许可证分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 实现 {@link BeanFactoryPostProcessor}，允许方便地注册自定义的 {@link PropertyEditor} 编辑器。
 *
 * <p>如果您想注册 {@link PropertyEditor} 实例，根据 Spring 2.0 的推荐用法，应使用自定义的
 * {@link PropertyEditorRegistrar} 实现来注册任何所需的编辑器实例，这些实例注册在给定的
 * {@link org.springframework.beans.PropertyEditorRegistry 注册表} 上。每个 PropertyEditorRegistrar 可以注册任意数量的自定义编辑器。
 *
 * <pre class="code">
 * &lt;bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *   &lt;property name="propertyEditorRegistrars"&gt;
 *     &lt;list&gt;
 *       &lt;bean class="mypackage.MyCustomDateEditorRegistrar"/&gt;
 *       &lt;bean class="mypackage.MyObjectEditorRegistrar"/&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>
 * 通过使用 {@code customEditors} 属性注册 {@link PropertyEditor} <em>类</em> 是完全可行的。Spring 将为每次编辑尝试创建新的实例：
 *
 * <pre class="code">
 * &lt;bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *   &lt;property name="customEditors"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="java.util.Date" value="mypackage.MyCustomDateEditor"/&gt;
 *       &lt;entry key="mypackage.MyObject" value="mypackage.MyObjectEditor"/&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>
 * 注意，您不应该通过 {@code customEditors} 属性注册 {@link PropertyEditor} 实例，因为
 * {@link PropertyEditor} 编辑器是有状态的，每个编辑尝试时实例都需要进行同步。如果您需要控制
 * {@link PropertyEditor} 编辑器的实例化过程，请使用一个 {@link PropertyEditorRegistrar} 来注册它们。
 *
 * <p>
 * 同时支持 "java.lang.String[]" 风格的数组类名和原始类名（例如 "boolean"）。实际类名解析委托给
 * {@link ClassUtils}。
 *
 * <p><b>注意：</b>使用此配置器注册的自定义属性编辑器 <i>不适用于数据绑定</i>。数据绑定的自定义编辑器需要注册在
 * {@link org.springframework.validation.DataBinder} 上：使用公共基类或委托给公共 PropertyEditorRegistrar 实现以在那里重用编辑器注册。
 *
 * @author Juergen Hoeller
 * @since 27.02.2004
 * @see java.beans.PropertyEditor
 * @see org.springframework.beans.PropertyEditorRegistrar
 * @see ConfigurableBeanFactory#addPropertyEditorRegistrar
 * @see ConfigurableBeanFactory#registerCustomEditor
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomEditorConfigurer implements BeanFactoryPostProcessor, Ordered {

    protected final Log logger = LogFactory.getLog(getClass());

    // 默认：与非有序相同
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Nullable
    private PropertyEditorRegistrar[] propertyEditorRegistrars;

    @Nullable
    private Map<Class<?>, Class<? extends PropertyEditor>> customEditors;

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 指定要应用到当前应用程序上下文中定义的bean的 {@link PropertyEditorRegistrar PropertyEditorRegistrars}
     * <p>这允许与 {@link org.springframework.validation.DataBinder DataBinders} 等共享 {@code PropertyEditorRegistrars}。
     * 此外，它避免了在自定义编辑器上同步的需求：每次尝试创建bean时，都会由一个 {@code PropertyEditorRegistrar} 创建新的编辑器实例。
     * @see ConfigurableListableBeanFactory#addPropertyEditorRegistrar
     */
    public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] propertyEditorRegistrars) {
        this.propertyEditorRegistrars = propertyEditorRegistrars;
    }

    /**
     * 通过一个 {@link Map} 指定要注册的自定义编辑器，使用所需类型的类名作为键，关联的 {@link PropertyEditor} 类名作为值。
     * @see ConfigurableListableBeanFactory#registerCustomEditor
     */
    public void setCustomEditors(Map<Class<?>, Class<? extends PropertyEditor>> customEditors) {
        this.customEditors = customEditors;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.propertyEditorRegistrars != null) {
            for (PropertyEditorRegistrar propertyEditorRegistrar : this.propertyEditorRegistrars) {
                beanFactory.addPropertyEditorRegistrar(propertyEditorRegistrar);
            }
        }
        if (this.customEditors != null) {
            this.customEditors.forEach(beanFactory::registerCustomEditor);
        }
    }
}
