// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能无法使用此文件，除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性的保证。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import java.util.Set;
import javax.lang.model.element.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.log.LogMessage;

/**
 * 支持与{@link Element}相关的自动装配的解析器的基础类。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
abstract class AutowiredElementResolver {

    private final Log logger = LogFactory.getLog(getClass());

    protected final void registerDependentBeans(ConfigurableBeanFactory beanFactory, String beanName, Set<String> autowiredBeanNames) {
        for (String autowiredBeanName : autowiredBeanNames) {
            if (beanFactory.containsBean(autowiredBeanName)) {
                beanFactory.registerDependentBean(autowiredBeanName, beanName);
            }
            logger.trace(LogMessage.format("Autowiring by type from bean name %s' to bean named '%s'", beanName, autowiredBeanName));
        }
    }

    /**
     * 支持快捷 Bean 解析的 `@link DependencyDescriptor`。
     */
    @SuppressWarnings("serial")
    static class ShortcutDependencyDescriptor extends DependencyDescriptor {

        private final String shortcut;

        public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut) {
            super(original);
            this.shortcut = shortcut;
        }

        @Override
        public Object resolveShortcut(BeanFactory beanFactory) {
            return beanFactory.getBean(this.shortcut, getDependencyType());
        }
    }
}
