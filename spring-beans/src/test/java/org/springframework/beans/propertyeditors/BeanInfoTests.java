// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性和非侵权性。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.propertyeditors;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Juergen Hoeller
 * @since 2006年3月6日
 */
public class BeanInfoTests {

    @Test
    public void testComplexObject() {
        ValueBean bean = new ValueBean();
        BeanWrapper bw = new BeanWrapperImpl(bean);
        Integer value = 1;
        bw.setPropertyValue("value", value);
        assertThat(value).as("value not set correctly").isEqualTo(bean.getValue());
        value = 2;
        bw.setPropertyValue("value", value.toString());
        assertThat(value).as("value not converted").isEqualTo(bean.getValue());
        bw.setPropertyValue("value", null);
        assertThat(bean.getValue()).as("value not null").isNull();
        bw.setPropertyValue("value", "");
        assertThat(bean.getValue()).as("value not converted to null").isNull();
    }

    public static class ValueBean {

        private Integer value;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    public static class ValueBeanBeanInfo extends SimpleBeanInfo {

        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                PropertyDescriptor pd = new PropertyDescriptor("value", ValueBean.class);
                pd.setPropertyEditorClass(MyNumberEditor.class);
                return new PropertyDescriptor[] { pd };
            } catch (IntrospectionException ex) {
                throw new FatalBeanException("Couldn't create PropertyDescriptor", ex);
            }
        }
    }

    public static class MyNumberEditor extends CustomNumberEditor {

        private Object target;

        public MyNumberEditor() throws IllegalArgumentException {
            super(Integer.class, true);
        }

        public MyNumberEditor(Object target) throws IllegalArgumentException {
            super(Integer.class, true);
            this.target = target;
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            Assert.isTrue(this.target instanceof ValueBean, "Target must be available");
            super.setAsText(text);
        }
    }
}
