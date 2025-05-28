// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下地址获取许可协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议了解具体的管理权限和限制。*/
package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2004年7月31日
 */
public class CustomEditorConfigurerTests {

    @Test
    public void testCustomEditorConfigurerWithPropertyEditorRegistrar() throws ParseException {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        CustomEditorConfigurer cec = new CustomEditorConfigurer();
        final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
        cec.setPropertyEditorRegistrars(new PropertyEditorRegistrar[] { registry -> registry.registerCustomEditor(Date.class, new CustomDateEditor(df, true)) });
        cec.postProcessBeanFactory(bf);
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("date", "2.12.1975");
        RootBeanDefinition bd1 = new RootBeanDefinition(TestBean.class);
        bd1.setPropertyValues(pvs);
        bf.registerBeanDefinition("tb1", bd1);
        pvs = new MutablePropertyValues();
        pvs.add("someMap[myKey]", new TypedStringValue("2.12.1975", Date.class));
        RootBeanDefinition bd2 = new RootBeanDefinition(TestBean.class);
        bd2.setPropertyValues(pvs);
        bf.registerBeanDefinition("tb2", bd2);
        TestBean tb1 = (TestBean) bf.getBean("tb1");
        assertThat(tb1.getDate()).isEqualTo(df.parse("2.12.1975"));
        TestBean tb2 = (TestBean) bf.getBean("tb2");
        assertThat(tb2.getSomeMap().get("myKey")).isEqualTo(df.parse("2.12.1975"));
    }

    @Test
    public void testCustomEditorConfigurerWithEditorAsClass() throws ParseException {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        CustomEditorConfigurer cec = new CustomEditorConfigurer();
        Map<Class<?>, Class<? extends PropertyEditor>> editors = new HashMap<>();
        editors.put(Date.class, MyDateEditor.class);
        cec.setCustomEditors(editors);
        cec.postProcessBeanFactory(bf);
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("date", "2.12.1975");
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.setPropertyValues(pvs);
        bf.registerBeanDefinition("tb", bd);
        TestBean tb = (TestBean) bf.getBean("tb");
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
        assertThat(tb.getDate()).isEqualTo(df.parse("2.12.1975"));
    }

    @Test
    public void testCustomEditorConfigurerWithRequiredTypeArray() throws ParseException {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        CustomEditorConfigurer cec = new CustomEditorConfigurer();
        Map<Class<?>, Class<? extends PropertyEditor>> editors = new HashMap<>();
        editors.put(String[].class, MyTestEditor.class);
        cec.setCustomEditors(editors);
        cec.postProcessBeanFactory(bf);
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("stringArray", "xxx");
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.setPropertyValues(pvs);
        bf.registerBeanDefinition("tb", bd);
        TestBean tb = (TestBean) bf.getBean("tb");
        assertThat(tb.getStringArray() != null && tb.getStringArray().length == 1).isTrue();
        assertThat(tb.getStringArray()[0]).isEqualTo("test");
    }

    public static class MyDateEditor extends CustomDateEditor {

        public MyDateEditor() {
            super(DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN), true);
        }
    }

    public static class MyTestEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) {
            setValue(new String[] { "test" });
        }
    }
}
