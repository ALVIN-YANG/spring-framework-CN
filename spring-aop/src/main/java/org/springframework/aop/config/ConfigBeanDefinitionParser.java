// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）许可；
除非适用法律要求或经书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下网址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.aop.aspectj.DeclareParentsAdvisor;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * 用于 `<aop:config>` 标签的 {@link BeanDefinitionParser}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @since 2.0
 */
class ConfigBeanDefinitionParser implements BeanDefinitionParser {

    private static final String ASPECT = "aspect";

    private static final String EXPRESSION = "expression";

    private static final String ID = "id";

    private static final String POINTCUT = "pointcut";

    private static final String ADVICE_BEAN_NAME = "adviceBeanName";

    private static final String ADVISOR = "advisor";

    private static final String ADVICE_REF = "advice-ref";

    private static final String POINTCUT_REF = "pointcut-ref";

    private static final String REF = "ref";

    private static final String BEFORE = "before";

    private static final String DECLARE_PARENTS = "declare-parents";

    private static final String TYPE_PATTERN = "types-matching";

    private static final String DEFAULT_IMPL = "default-impl";

    private static final String DELEGATE_REF = "delegate-ref";

    private static final String IMPLEMENT_INTERFACE = "implement-interface";

    private static final String AFTER = "after";

    private static final String AFTER_RETURNING_ELEMENT = "after-returning";

    private static final String AFTER_THROWING_ELEMENT = "after-throwing";

    private static final String AROUND = "around";

    private static final String RETURNING = "returning";

    private static final String RETURNING_PROPERTY = "returningName";

    private static final String THROWING = "throwing";

    private static final String THROWING_PROPERTY = "throwingName";

    private static final String ARG_NAMES = "arg-names";

    private static final String ARG_NAMES_PROPERTY = "argumentNames";

    private static final String ASPECT_NAME_PROPERTY = "aspectName";

    private static final String DECLARATION_ORDER_PROPERTY = "declarationOrder";

    private static final String ORDER_PROPERTY = "order";

    private static final int METHOD_INDEX = 0;

    private static final int POINTCUT_INDEX = 1;

    private static final int ASPECT_INSTANCE_FACTORY_INDEX = 2;

    private final ParseState parseState = new ParseState();

    @Override
    @Nullable
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), parserContext.extractSource(element));
        parserContext.pushContainingComponent(compositeDef);
        configureAutoProxyCreator(parserContext, element);
        List<Element> childElts = DomUtils.getChildElements(element);
        for (Element elt : childElts) {
            String localName = parserContext.getDelegate().getLocalName(elt);
            switch(localName) {
                case POINTCUT ->
                    parsePointcut(elt, parserContext);
                case ADVISOR ->
                    parseAdvisor(elt, parserContext);
                case ASPECT ->
                    parseAspect(elt, parserContext);
            }
        }
        parserContext.popAndRegisterContainingComponent();
        return null;
    }

    /**
     * 配置所需的自动代理创建器，以支持由 '{@code <aop:config/>}' 标签创建的 {@link BeanDefinition BeanDefinitions}。
     * 如果将 '{@code proxy-target-class}' 属性设置为 '{@code true}'，则将强制使用类代理。
     * @see AopNamespaceUtils
     */
    private void configureAutoProxyCreator(ParserContext parserContext, Element element) {
        AopNamespaceUtils.registerAspectJAutoProxyCreatorIfNecessary(parserContext, element);
    }

    /**
     * 解析提供的 {@code <advisor>} 元素，并将生成的 {@link org.springframework.aop.Advisor} 和任何生成的 {@link org.springframework.aop.Pointcut} 注册到提供的 {@link BeanDefinitionRegistry}。
     */
    private void parseAdvisor(Element advisorElement, ParserContext parserContext) {
        AbstractBeanDefinition advisorDef = createAdvisorBeanDefinition(advisorElement, parserContext);
        String id = advisorElement.getAttribute(ID);
        try {
            this.parseState.push(new AdvisorEntry(id));
            String advisorBeanName = id;
            if (StringUtils.hasText(advisorBeanName)) {
                parserContext.getRegistry().registerBeanDefinition(advisorBeanName, advisorDef);
            } else {
                advisorBeanName = parserContext.getReaderContext().registerWithGeneratedName(advisorDef);
            }
            Object pointcut = parsePointcutProperty(advisorElement, parserContext);
            if (pointcut instanceof BeanDefinition beanDefinition) {
                advisorDef.getPropertyValues().add(POINTCUT, pointcut);
                parserContext.registerComponent(new AdvisorComponentDefinition(advisorBeanName, advisorDef, beanDefinition));
            } else if (pointcut instanceof String beanName) {
                advisorDef.getPropertyValues().add(POINTCUT, new RuntimeBeanReference(beanName));
                parserContext.registerComponent(new AdvisorComponentDefinition(advisorBeanName, advisorDef));
            }
        } finally {
            this.parseState.pop();
        }
    }

    /**
     * 为所提供的描述创建一个 {@link RootBeanDefinition} 用于顾问。**不会**解析任何相关的 '{@code pointcut}' 或 '{@code pointcut-ref}' 属性。
     */
    private AbstractBeanDefinition createAdvisorBeanDefinition(Element advisorElement, ParserContext parserContext) {
        RootBeanDefinition advisorDefinition = new RootBeanDefinition(DefaultBeanFactoryPointcutAdvisor.class);
        advisorDefinition.setSource(parserContext.extractSource(advisorElement));
        String adviceRef = advisorElement.getAttribute(ADVICE_REF);
        if (!StringUtils.hasText(adviceRef)) {
            parserContext.getReaderContext().error("'advice-ref' attribute contains empty value.", advisorElement, this.parseState.snapshot());
        } else {
            advisorDefinition.getPropertyValues().add(ADVICE_BEAN_NAME, new RuntimeBeanNameReference(adviceRef));
        }
        if (advisorElement.hasAttribute(ORDER_PROPERTY)) {
            advisorDefinition.getPropertyValues().add(ORDER_PROPERTY, advisorElement.getAttribute(ORDER_PROPERTY));
        }
        return advisorDefinition;
    }

    private void parseAspect(Element aspectElement, ParserContext parserContext) {
        String aspectId = aspectElement.getAttribute(ID);
        String aspectName = aspectElement.getAttribute(REF);
        try {
            this.parseState.push(new AspectEntry(aspectId, aspectName));
            List<BeanDefinition> beanDefinitions = new ArrayList<>();
            List<BeanReference> beanReferences = new ArrayList<>();
            List<Element> declareParents = DomUtils.getChildElementsByTagName(aspectElement, DECLARE_PARENTS);
            for (int i = METHOD_INDEX; i < declareParents.size(); i++) {
                Element declareParentsElement = declareParents.get(i);
                beanDefinitions.add(parseDeclareParents(declareParentsElement, parserContext));
            }
            // 我们必须在一个循环中解析 "advice" 以及所有 advice 类型的信息，以获取
            // 排序语义正确。
            NodeList nodeList = aspectElement.getChildNodes();
            boolean adviceFoundAlready = false;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (isAdviceNode(node, parserContext)) {
                    if (!adviceFoundAlready) {
                        adviceFoundAlready = true;
                        if (!StringUtils.hasText(aspectName)) {
                            parserContext.getReaderContext().error("<aspect> tag needs aspect bean reference via 'ref' attribute when declaring advices.", aspectElement, this.parseState.snapshot());
                            return;
                        }
                        beanReferences.add(new RuntimeBeanReference(aspectName));
                    }
                    AbstractBeanDefinition advisorDefinition = parseAdvice(aspectName, i, aspectElement, (Element) node, parserContext, beanDefinitions, beanReferences);
                    beanDefinitions.add(advisorDefinition);
                }
            }
            AspectComponentDefinition aspectComponentDefinition = createAspectComponentDefinition(aspectElement, aspectId, beanDefinitions, beanReferences, parserContext);
            parserContext.pushContainingComponent(aspectComponentDefinition);
            List<Element> pointcuts = DomUtils.getChildElementsByTagName(aspectElement, POINTCUT);
            for (Element pointcutElement : pointcuts) {
                parsePointcut(pointcutElement, parserContext);
            }
            parserContext.popAndRegisterContainingComponent();
        } finally {
            this.parseState.pop();
        }
    }

    private AspectComponentDefinition createAspectComponentDefinition(Element aspectElement, String aspectId, List<BeanDefinition> beanDefs, List<BeanReference> beanRefs, ParserContext parserContext) {
        BeanDefinition[] beanDefArray = beanDefs.toArray(new BeanDefinition[0]);
        BeanReference[] beanRefArray = beanRefs.toArray(new BeanReference[0]);
        Object source = parserContext.extractSource(aspectElement);
        return new AspectComponentDefinition(aspectId, beanDefArray, beanRefArray, source);
    }

    /**
     * 返回 {@code true} 如果提供的节点描述了一个建议类型。可能是以下之一：
     * '{@code before}'、'@{code after}'、'@{code after-returning}'、'@{code after-throwing}' 或 '{@code around}'。
     */
    private boolean isAdviceNode(Node aNode, ParserContext parserContext) {
        if (!(aNode instanceof Element)) {
            return false;
        } else {
            String name = parserContext.getDelegate().getLocalName(aNode);
            return (BEFORE.equals(name) || AFTER.equals(name) || AFTER_RETURNING_ELEMENT.equals(name) || AFTER_THROWING_ELEMENT.equals(name) || AROUND.equals(name));
        }
    }

    /**
     * 解析一个 '{@code declare-parents}' 元素，并将适当的 DeclareParentsAdvisor
     * 注册到由提供的 ParserContext 封装在其中的 BeanDefinitionRegistry 中。
     */
    private AbstractBeanDefinition parseDeclareParents(Element declareParentsElement, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(DeclareParentsAdvisor.class);
        builder.addConstructorArgValue(declareParentsElement.getAttribute(IMPLEMENT_INTERFACE));
        builder.addConstructorArgValue(declareParentsElement.getAttribute(TYPE_PATTERN));
        String defaultImpl = declareParentsElement.getAttribute(DEFAULT_IMPL);
        String delegateRef = declareParentsElement.getAttribute(DELEGATE_REF);
        if (StringUtils.hasText(defaultImpl) && !StringUtils.hasText(delegateRef)) {
            builder.addConstructorArgValue(defaultImpl);
        } else if (StringUtils.hasText(delegateRef) && !StringUtils.hasText(defaultImpl)) {
            builder.addConstructorArgReference(delegateRef);
        } else {
            parserContext.getReaderContext().error("Exactly one of the " + DEFAULT_IMPL + " or " + DELEGATE_REF + " attributes must be specified", declareParentsElement, this.parseState.snapshot());
        }
        AbstractBeanDefinition definition = builder.getBeanDefinition();
        definition.setSource(parserContext.extractSource(declareParentsElement));
        parserContext.getReaderContext().registerWithGeneratedName(definition);
        return definition;
    }

    /**
     * 解析'{@code before}'、'{@code after}'、'{@code after-returning}'、
     * '{@code after-throwing}'或'{@code around}'之一，并将生成的BeanDefinition
     * 注册到提供的BeanDefinitionRegistry中。
     * @return 生成的建议RootBeanDefinition
     */
    private AbstractBeanDefinition parseAdvice(String aspectName, int order, Element aspectElement, Element adviceElement, ParserContext parserContext, List<BeanDefinition> beanDefinitions, List<BeanReference> beanReferences) {
        try {
            this.parseState.push(new AdviceEntry(parserContext.getDelegate().getLocalName(adviceElement)));
            // 创建方法工厂Bean
            RootBeanDefinition methodDefinition = new RootBeanDefinition(MethodLocatingFactoryBean.class);
            methodDefinition.getPropertyValues().add("targetBeanName", aspectName);
            methodDefinition.getPropertyValues().add("methodName", adviceElement.getAttribute("method"));
            methodDefinition.setSynthetic(true);
            // 创建实例工厂定义
            RootBeanDefinition aspectFactoryDef = new RootBeanDefinition(SimpleBeanFactoryAwareAspectInstanceFactory.class);
            aspectFactoryDef.getPropertyValues().add("aspectBeanName", aspectName);
            aspectFactoryDef.setSynthetic(true);
            // 注册切入点
            AbstractBeanDefinition adviceDef = createAdviceDefinition(adviceElement, parserContext, aspectName, order, methodDefinition, aspectFactoryDef, beanDefinitions, beanReferences);
            // 配置顾问
            RootBeanDefinition advisorDefinition = new RootBeanDefinition(AspectJPointcutAdvisor.class);
            advisorDefinition.setSource(parserContext.extractSource(adviceElement));
            advisorDefinition.getConstructorArgumentValues().addGenericArgumentValue(adviceDef);
            if (aspectElement.hasAttribute(ORDER_PROPERTY)) {
                advisorDefinition.getPropertyValues().add(ORDER_PROPERTY, aspectElement.getAttribute(ORDER_PROPERTY));
            }
            // 注册最终的顾问
            parserContext.getReaderContext().registerWithGeneratedName(advisorDefinition);
            return advisorDefinition;
        } finally {
            this.parseState.pop();
        }
    }

    /**
     * 创建用于POJO建议 Bean 的 RootBeanDefinition。同时引发点切解析，以便将点切与建议 Bean 关联。
     * 使用提供的 MutablePropertyValues，相同的点切也被配置为包围 Advisor 定义的点切。
     */
    private AbstractBeanDefinition createAdviceDefinition(Element adviceElement, ParserContext parserContext, String aspectName, int order, RootBeanDefinition methodDef, RootBeanDefinition aspectFactoryDef, List<BeanDefinition> beanDefinitions, List<BeanReference> beanReferences) {
        RootBeanDefinition adviceDefinition = new RootBeanDefinition(getAdviceClass(adviceElement, parserContext));
        adviceDefinition.setSource(parserContext.extractSource(adviceElement));
        adviceDefinition.getPropertyValues().add(ASPECT_NAME_PROPERTY, aspectName);
        adviceDefinition.getPropertyValues().add(DECLARATION_ORDER_PROPERTY, order);
        if (adviceElement.hasAttribute(RETURNING)) {
            adviceDefinition.getPropertyValues().add(RETURNING_PROPERTY, adviceElement.getAttribute(RETURNING));
        }
        if (adviceElement.hasAttribute(THROWING)) {
            adviceDefinition.getPropertyValues().add(THROWING_PROPERTY, adviceElement.getAttribute(THROWING));
        }
        if (adviceElement.hasAttribute(ARG_NAMES)) {
            adviceDefinition.getPropertyValues().add(ARG_NAMES_PROPERTY, adviceElement.getAttribute(ARG_NAMES));
        }
        ConstructorArgumentValues cav = adviceDefinition.getConstructorArgumentValues();
        cav.addIndexedArgumentValue(METHOD_INDEX, methodDef);
        Object pointcut = parsePointcutProperty(adviceElement, parserContext);
        if (pointcut instanceof BeanDefinition beanDefinition) {
            cav.addIndexedArgumentValue(POINTCUT_INDEX, pointcut);
            beanDefinitions.add(beanDefinition);
        } else if (pointcut instanceof String beanName) {
            RuntimeBeanReference pointcutRef = new RuntimeBeanReference(beanName);
            cav.addIndexedArgumentValue(POINTCUT_INDEX, pointcutRef);
            beanReferences.add(pointcutRef);
        }
        cav.addIndexedArgumentValue(ASPECT_INSTANCE_FACTORY_INDEX, aspectFactoryDef);
        return adviceDefinition;
    }

    /**
     * 获取与提供的 {@link Element} 对应的建议实现类。
     */
    private Class<?> getAdviceClass(Element adviceElement, ParserContext parserContext) {
        String elementName = parserContext.getDelegate().getLocalName(adviceElement);
        if (BEFORE.equals(elementName)) {
            return AspectJMethodBeforeAdvice.class;
        } else if (AFTER.equals(elementName)) {
            return AspectJAfterAdvice.class;
        } else if (AFTER_RETURNING_ELEMENT.equals(elementName)) {
            return AspectJAfterReturningAdvice.class;
        } else if (AFTER_THROWING_ELEMENT.equals(elementName)) {
            return AspectJAfterThrowingAdvice.class;
        } else if (AROUND.equals(elementName)) {
            return AspectJAroundAdvice.class;
        } else {
            throw new IllegalArgumentException("Unknown advice kind [" + elementName + "].");
        }
    }

    /**
     * 解析提供的 {@code <pointcut>} 并将生成的 Pointcut 注册到 BeanDefinitionRegistry。
     */
    private AbstractBeanDefinition parsePointcut(Element pointcutElement, ParserContext parserContext) {
        String id = pointcutElement.getAttribute(ID);
        String expression = pointcutElement.getAttribute(EXPRESSION);
        AbstractBeanDefinition pointcutDefinition = null;
        try {
            this.parseState.push(new PointcutEntry(id));
            pointcutDefinition = createPointcutDefinition(expression);
            pointcutDefinition.setSource(parserContext.extractSource(pointcutElement));
            String pointcutBeanName = id;
            if (StringUtils.hasText(pointcutBeanName)) {
                parserContext.getRegistry().registerBeanDefinition(pointcutBeanName, pointcutDefinition);
            } else {
                pointcutBeanName = parserContext.getReaderContext().registerWithGeneratedName(pointcutDefinition);
            }
            parserContext.registerComponent(new PointcutComponentDefinition(pointcutBeanName, pointcutDefinition, expression));
        } finally {
            this.parseState.pop();
        }
        return pointcutDefinition;
    }

    /**
     * 解析提供的 {@link Element} 的 {@code pointcut} 或 {@code pointcut-ref} 属性，并适当地添加一个 {@code pointcut} 属性。如果需要，生成一个表示点切的 {@link org.springframework.beans.factory.config.BeanDefinition}，并返回其bean名称，否则返回所引用点切的bean名称。
     */
    @Nullable
    private Object parsePointcutProperty(Element element, ParserContext parserContext) {
        if (element.hasAttribute(POINTCUT) && element.hasAttribute(POINTCUT_REF)) {
            parserContext.getReaderContext().error("Cannot define both 'pointcut' and 'pointcut-ref' on <advisor> tag.", element, this.parseState.snapshot());
            return null;
        } else if (element.hasAttribute(POINTCUT)) {
            // 创建一个针对匿名PC（Pointcut）的切入点，并将其注册。
            String expression = element.getAttribute(POINTCUT);
            AbstractBeanDefinition pointcutDefinition = createPointcutDefinition(expression);
            pointcutDefinition.setSource(parserContext.extractSource(element));
            return pointcutDefinition;
        } else if (element.hasAttribute(POINTCUT_REF)) {
            String pointcutRef = element.getAttribute(POINTCUT_REF);
            if (!StringUtils.hasText(pointcutRef)) {
                parserContext.getReaderContext().error("'pointcut-ref' attribute contains empty value.", element, this.parseState.snapshot());
                return null;
            }
            return pointcutRef;
        } else {
            parserContext.getReaderContext().error("Must define one of 'pointcut' or 'pointcut-ref' on <advisor> tag.", element, this.parseState.snapshot());
            return null;
        }
    }

    /**
     * 使用提供的切点表达式创建一个用于 {@link AspectJExpressionPointcut} 类的 {@link BeanDefinition}。
     */
    protected AbstractBeanDefinition createPointcutDefinition(String expression) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(AspectJExpressionPointcut.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanDefinition.setSynthetic(true);
        beanDefinition.getPropertyValues().add(EXPRESSION, expression);
        return beanDefinition;
    }
}
