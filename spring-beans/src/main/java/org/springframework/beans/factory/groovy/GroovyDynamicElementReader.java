// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者们。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是按照“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans.factory.groovy;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Writable;
import groovy.xml.StreamingMarkupBuilder;
import org.w3c.dom.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.lang.Nullable;

/**
 * 由GroovyBeanDefinitionReader用于读取Groovy DSL中的Spring XML命名空间表达式
 *
 * @author Jeff Brown
 * @author Juergen Hoeller
 * @author Dave Syer
 * @since 4.0
 */
class GroovyDynamicElementReader extends GroovyObjectSupport {

    private final String rootNamespace;

    private final Map<String, String> xmlNamespaces;

    private final BeanDefinitionParserDelegate delegate;

    private final GroovyBeanDefinitionWrapper beanDefinition;

    protected final boolean decorating;

    private boolean callAfterInvocation = true;

    public GroovyDynamicElementReader(String namespace, Map<String, String> namespaceMap, BeanDefinitionParserDelegate delegate, GroovyBeanDefinitionWrapper beanDefinition, boolean decorating) {
        this.rootNamespace = namespace;
        this.xmlNamespaces = namespaceMap;
        this.delegate = delegate;
        this.beanDefinition = beanDefinition;
        this.decorating = decorating;
    }

    @Override
    @Nullable
    public Object invokeMethod(String name, Object obj) {
        Object[] args = (Object[]) obj;
        if (name.equals("doCall")) {
            @SuppressWarnings("unchecked")
            Closure<Object> callable = (Closure<Object>) args[0];
            callable.setResolveStrategy(Closure.DELEGATE_FIRST);
            callable.setDelegate(this);
            Object result = callable.call();
            if (this.callAfterInvocation) {
                afterInvocation();
                this.callAfterInvocation = false;
            }
            return result;
        } else {
            StreamingMarkupBuilder builder = new StreamingMarkupBuilder();
            String myNamespace = this.rootNamespace;
            Map<String, String> myNamespaces = this.xmlNamespaces;
            @SuppressWarnings("serial")
            Closure<Object> callable = new Closure<>(this) {

                @Override
                public Object call(Object... arguments) {
                    ((GroovyObject) getProperty("mkp")).invokeMethod("declareNamespace", new Object[] { myNamespaces });
                    int len = args.length;
                    if (len > 0 && args[len - 1] instanceof Closure<?> callable) {
                        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
                        callable.setDelegate(builder);
                    }
                    return ((GroovyObject) ((GroovyObject) getDelegate()).getProperty(myNamespace)).invokeMethod(name, args);
                }
            };
            callable.setResolveStrategy(Closure.DELEGATE_FIRST);
            callable.setDelegate(builder);
            Writable writable = (Writable) builder.bind(callable);
            StringWriter sw = new StringWriter();
            try {
                writable.writeTo(sw);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            Element element = this.delegate.getReaderContext().readDocumentFromString(sw.toString()).getDocumentElement();
            this.delegate.initDefaults(element);
            if (this.decorating) {
                BeanDefinitionHolder holder = this.beanDefinition.getBeanDefinitionHolder();
                holder = this.delegate.decorateIfRequired(element, holder, null);
                this.beanDefinition.setBeanDefinitionHolder(holder);
            } else {
                BeanDefinition beanDefinition = this.delegate.parseCustomElement(element);
                if (beanDefinition != null) {
                    this.beanDefinition.setBeanDefinition((AbstractBeanDefinition) beanDefinition);
                }
            }
            if (this.callAfterInvocation) {
                afterInvocation();
                this.callAfterInvocation = false;
            }
            return element;
        }
    }

    /**
     * 子类或匿名类可以重写的钩子，用于在调用完成后实现自定义行为。
     */
    protected void afterInvocation() {
        // NOOP 是一个常用的英文缩写，通常表示 "No Operation"，即“无操作”。在编程中，NOOP 通常用于表示一个空的或者不做任何操作的方法。下面是将 "NOOP" 翻译成中文的注释内容：```java// NOOP：表示这是一个无操作的方法，不执行任何操作```
    }
}
