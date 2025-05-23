// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者们。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非适用法律要求或经书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途的适用性。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.xml;

import java.util.Collection;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.Conventions;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 简单的 {@code NamespaceHandler} 实现，它将自定义属性直接映射到 Bean 属性上。需要注意的是，这个 {@code NamespaceHandler} 没有对应的模式，因为没有方法事先知道所有可能的属性名称。
 *
 * <p>以下展示了如何使用这个 {@code NamespaceHandler} 的例子：
 *
 * <pre class="code">
 * &lt;bean id=&quot;author&quot; class=&quot;..TestBean&quot; c:name=&quot;Enescu&quot; c:work-ref=&quot;compositions&quot;/&gt;
 * </pre>
 *
 * 在这里，`{@code c:name}` 直接对应于类 `{@code TestBean}` 构造函数上声明的 `name` 参数。`{@code c:work-ref}` 属性对应于 `work` 参数，而不是具体的值，它包含将被视为参数的 Bean 的名称。
 *
 * <b>注意</b>：这个实现仅支持命名参数 - 不支持索引或类型。此外，名称被容器用作提示，容器默认进行类型反射。
 *
 * @author Costin Leau
 * @since 3.1
 * @see SimplePropertyNamespaceHandler
 */
public class SimpleConstructorNamespaceHandler implements NamespaceHandler {

    private static final String REF_SUFFIX = "-ref";

    private static final String DELIMITER_PREFIX = "_";

    @Override
    public void init() {
    }

    @Override
    @Nullable
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        parserContext.getReaderContext().error("Class [" + getClass().getName() + "] does not support custom elements.", element);
        return null;
    }

    @Override
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        if (node instanceof Attr attr) {
            String argName = parserContext.getDelegate().getLocalName(attr).strip();
            String argValue = attr.getValue().strip();
            ConstructorArgumentValues cvs = definition.getBeanDefinition().getConstructorArgumentValues();
            boolean ref = false;
            // 处理引用参数
            if (argName.endsWith(REF_SUFFIX)) {
                ref = true;
                argName = argName.substring(0, argName.length() - REF_SUFFIX.length());
            }
            ValueHolder valueHolder = new ValueHolder(ref ? new RuntimeBeanReference(argValue) : argValue);
            valueHolder.setSource(parserContext.getReaderContext().extractSource(attr));
            // 处理“转义”/“_”参数
            if (argName.startsWith(DELIMITER_PREFIX)) {
                String arg = argName.substring(1).trim();
                // 快速默认检查
                if (!StringUtils.hasText(arg)) {
                    cvs.addGenericArgumentValue(valueHolder);
                } else // 假设一个索引否则
                {
                    int index = -1;
                    try {
                        index = Integer.parseInt(arg);
                    } catch (NumberFormatException ex) {
                        parserContext.getReaderContext().error("Constructor argument '" + argName + "' specifies an invalid integer", attr);
                    }
                    if (index < 0) {
                        parserContext.getReaderContext().error("Constructor argument '" + argName + "' specifies a negative index", attr);
                    }
                    if (cvs.hasIndexedArgumentValue(index)) {
                        parserContext.getReaderContext().error("Constructor argument '" + argName + "' with index " + index + " already defined using <constructor-arg>." + " Only one approach may be used per argument.", attr);
                    }
                    cvs.addIndexedArgumentValue(index, valueHolder);
                }
            } else // 无法避免的 -> 控制器名称
            {
                String name = Conventions.attributeNameToPropertyName(argName);
                if (containsArgWithName(name, cvs)) {
                    parserContext.getReaderContext().error("Constructor argument '" + argName + "' already defined using <constructor-arg>." + " Only one approach may be used per argument.", attr);
                }
                valueHolder.setName(Conventions.attributeNameToPropertyName(argName));
                cvs.addGenericArgumentValue(valueHolder);
            }
        }
        return definition;
    }

    private boolean containsArgWithName(String name, ConstructorArgumentValues cvs) {
        return (checkName(name, cvs.getGenericArgumentValues()) || checkName(name, cvs.getIndexedArgumentValues().values()));
    }

    private boolean checkName(String name, Collection<ValueHolder> values) {
        for (ValueHolder holder : values) {
            if (name.equals(holder.getName())) {
                return true;
            }
        }
        return false;
    }
}
