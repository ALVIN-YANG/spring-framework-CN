// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory.xml;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.testfixture.beans.CollectingReaderEventListener;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 *
 * 作者：Rob Harrop
 * 作者：Juergen Hoeller
 */
@SuppressWarnings("rawtypes")
class EventPublicationTests {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    private final CollectingReaderEventListener eventListener = new CollectingReaderEventListener();

    @BeforeEach
    void setUp() throws Exception {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
        reader.setEventListener(this.eventListener);
        reader.setSourceExtractor(new PassThroughSourceExtractor());
        reader.loadBeanDefinitions(new ClassPathResource("beanEvents.xml", getClass()));
    }

    @Test
    void defaultsEventReceived() throws Exception {
        List<DefaultsDefinition> defaultsList = this.eventListener.getDefaults();
        assertThat(defaultsList).isNotEmpty();
        assertThat(defaultsList.get(0)).isInstanceOf(DocumentDefaultsDefinition.class);
        DocumentDefaultsDefinition defaults = (DocumentDefaultsDefinition) defaultsList.get(0);
        assertThat(defaults.getLazyInit()).isEqualTo("true");
        assertThat(defaults.getAutowire()).isEqualTo("constructor");
        assertThat(defaults.getInitMethod()).isEqualTo("myInit");
        assertThat(defaults.getDestroyMethod()).isEqualTo("myDestroy");
        assertThat(defaults.getMerge()).isEqualTo("true");
        assertThat(defaults.getSource()).isInstanceOf(Element.class);
    }

    @Test
    void beanEventReceived() throws Exception {
        ComponentDefinition componentDefinition1 = this.eventListener.getComponentDefinition("testBean");
        assertThat(componentDefinition1).isInstanceOf(BeanComponentDefinition.class);
        assertThat(componentDefinition1.getBeanDefinitions()).hasSize(1);
        BeanDefinition beanDefinition1 = componentDefinition1.getBeanDefinitions()[0];
        assertThat(beanDefinition1.getConstructorArgumentValues().getGenericArgumentValue(String.class).getValue()).isEqualTo(new TypedStringValue("Rob Harrop"));
        assertThat(componentDefinition1.getBeanReferences()).hasSize(1);
        assertThat(componentDefinition1.getBeanReferences()[0].getBeanName()).isEqualTo("testBean2");
        assertThat(componentDefinition1.getInnerBeanDefinitions()).hasSize(1);
        BeanDefinition innerBd1 = componentDefinition1.getInnerBeanDefinitions()[0];
        assertThat(innerBd1.getConstructorArgumentValues().getGenericArgumentValue(String.class).getValue()).isEqualTo(new TypedStringValue("ACME"));
        assertThat(componentDefinition1.getSource()).isInstanceOf(Element.class);
        ComponentDefinition componentDefinition2 = this.eventListener.getComponentDefinition("testBean2");
        assertThat(componentDefinition2).isInstanceOf(BeanComponentDefinition.class);
        assertThat(componentDefinition1.getBeanDefinitions()).hasSize(1);
        BeanDefinition beanDefinition2 = componentDefinition2.getBeanDefinitions()[0];
        assertThat(beanDefinition2.getPropertyValues().getPropertyValue("name").getValue()).isEqualTo(new TypedStringValue("Juergen Hoeller"));
        assertThat(componentDefinition2.getBeanReferences()).isEmpty();
        assertThat(componentDefinition2.getInnerBeanDefinitions()).hasSize(1);
        BeanDefinition innerBd2 = componentDefinition2.getInnerBeanDefinitions()[0];
        assertThat(innerBd2.getPropertyValues().getPropertyValue("name").getValue()).isEqualTo(new TypedStringValue("Eva Schallmeiner"));
        assertThat(componentDefinition2.getSource()).isInstanceOf(Element.class);
    }

    @Test
    void aliasEventReceived() throws Exception {
        List<AliasDefinition> aliases = this.eventListener.getAliases("testBean");
        assertThat(aliases).hasSize(2);
        AliasDefinition aliasDefinition1 = aliases.get(0);
        assertThat(aliasDefinition1.getAlias()).isEqualTo("testBeanAlias1");
        assertThat(aliasDefinition1.getSource()).isInstanceOf(Element.class);
        AliasDefinition aliasDefinition2 = aliases.get(1);
        assertThat(aliasDefinition2.getAlias()).isEqualTo("testBeanAlias2");
        assertThat(aliasDefinition2.getSource()).isInstanceOf(Element.class);
    }

    @Test
    void importEventReceived() throws Exception {
        List<ImportDefinition> imports = this.eventListener.getImports();
        assertThat(imports).hasSize(1);
        ImportDefinition importDefinition = imports.get(0);
        assertThat(importDefinition.getImportedResource()).isEqualTo("beanEventsImported.xml");
        assertThat(importDefinition.getSource()).isInstanceOf(Element.class);
    }
}
