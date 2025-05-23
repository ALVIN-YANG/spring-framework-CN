// 翻译完成 glm-4-flash
/** 版权所有 2002-2017 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 用于记录废弃（@ Deprecated @ Deprecated）的Bean的Bean工厂后处理器。
 *
 * @author Arjen Poutsma
 * @since 3.0.3
 */
public class DeprecatedBeanWarner implements BeanFactoryPostProcessor {

    /**
     * 可供子类使用的日志记录器。
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    /**
     * 设置要使用的日志记录器名称。
     * 该名称将通过 Commons Logging 传递给底层的日志记录器实现，根据日志记录器的配置被解释为日志类别。
     * <p>这可以指定不将日志记录到该警告类别的日志中，而是记录到特定的命名类别中。
     * @see org.apache.commons.logging.LogFactory#getLog(String)
     * @see java.util.logging.Logger#getLogger(String)
     */
    public void setLoggerName(String loggerName) {
        this.logger = LogFactory.getLog(loggerName);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (isLogEnabled()) {
            String[] beanNames = beanFactory.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                String nameToLookup = beanName;
                if (beanFactory.isFactoryBean(beanName)) {
                    nameToLookup = BeanFactory.FACTORY_BEAN_PREFIX + beanName;
                }
                Class<?> beanType = beanFactory.getType(nameToLookup);
                if (beanType != null) {
                    Class<?> userClass = ClassUtils.getUserClass(beanType);
                    if (userClass.isAnnotationPresent(Deprecated.class)) {
                        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                        logDeprecatedBean(beanName, beanType, beanDefinition);
                    }
                }
            }
        }
    }

    /**
     * 记录一个针对被标记为@Deprecated注解的Bean的警告。
     * @param beanName 已废弃Bean的名称
     * @param beanType 用户指定的已废弃Bean的类型
     * @param beanDefinition 已废弃Bean的定义
     */
    protected void logDeprecatedBean(String beanName, Class<?> beanType, BeanDefinition beanDefinition) {
        StringBuilder builder = new StringBuilder();
        builder.append(beanType);
        builder.append(" ['");
        builder.append(beanName);
        builder.append('\'');
        String resourceDescription = beanDefinition.getResourceDescription();
        if (StringUtils.hasLength(resourceDescription)) {
            builder.append(" in ");
            builder.append(resourceDescription);
        }
        builder.append("] has been deprecated");
        writeToLog(builder.toString());
    }

    /**
     * 实际写入底层日志。
     * <p>默认实现将消息以“警告”级别记录。
     * @param message 要写入的消息
     */
    protected void writeToLog(String message) {
        logger.warn(message);
    }

    /**
     * 判断是否启用了 {@link #logger} 字段。
     * <p>当启用 "warn" 级别时，默认为 {@code true}。
     * 子类可以重写此方法以改变日志记录的级别。
     */
    protected boolean isLogEnabled() {
        return logger.isWarnEnabled();
    }
}
