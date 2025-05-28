// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据Apache License，版本2.0（以下简称“许可证”）授权；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件
* 是“按原样”分发的，不提供任何形式，无论是明示的、暗示的，
* 或是侵权责任或适销性保证。
* 请参阅许可证以了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 {@link PropertyMatches}。
 *
 * @author Stephane Nicoll
 */
public class PropertyMatchesTests {

    @Test
    public void simpleBeanPropertyTypo() {
        PropertyMatches matches = PropertyMatches.forProperty("naem", SampleBeanProperties.class);
        assertThat(matches.getPossibleMatches()).contains("name");
    }

    @Test
    public void complexBeanPropertyTypo() {
        PropertyMatches matches = PropertyMatches.forProperty("desriptn", SampleBeanProperties.class);
        assertThat(matches.getPossibleMatches()).isEmpty();
    }

    @Test
    public void unknownBeanProperty() {
        PropertyMatches matches = PropertyMatches.forProperty("unknown", SampleBeanProperties.class);
        assertThat(matches.getPossibleMatches()).isEmpty();
    }

    @Test
    public void severalMatchesBeanProperty() {
        PropertyMatches matches = PropertyMatches.forProperty("counter", SampleBeanProperties.class);
        assertThat(matches.getPossibleMatches()).contains("counter1");
        assertThat(matches.getPossibleMatches()).contains("counter2");
        assertThat(matches.getPossibleMatches()).contains("counter3");
    }

    @Test
    public void simpleBeanPropertyErrorMessage() {
        PropertyMatches matches = PropertyMatches.forProperty("naem", SampleBeanProperties.class);
        String msg = matches.buildErrorMessage();
        assertThat(msg).contains("naem");
        assertThat(msg).contains("name");
        assertThat(msg).contains("setter");
        assertThat(msg).doesNotContain("field");
    }

    @Test
    public void complexBeanPropertyErrorMessage() {
        PropertyMatches matches = PropertyMatches.forProperty("counter", SampleBeanProperties.class);
        String msg = matches.buildErrorMessage();
        assertThat(msg).contains("counter");
        assertThat(msg).contains("counter1");
        assertThat(msg).contains("counter2");
        assertThat(msg).contains("counter3");
    }

    @Test
    public void simpleFieldPropertyTypo() {
        PropertyMatches matches = PropertyMatches.forField("naem", SampleFieldProperties.class);
        assertThat(matches.getPossibleMatches()).contains("name");
    }

    @Test
    public void complexFieldPropertyTypo() {
        PropertyMatches matches = PropertyMatches.forField("desriptn", SampleFieldProperties.class);
        assertThat(matches.getPossibleMatches()).isEmpty();
    }

    @Test
    public void unknownFieldProperty() {
        PropertyMatches matches = PropertyMatches.forField("unknown", SampleFieldProperties.class);
        assertThat(matches.getPossibleMatches()).isEmpty();
    }

    @Test
    public void severalMatchesFieldProperty() {
        PropertyMatches matches = PropertyMatches.forField("counter", SampleFieldProperties.class);
        assertThat(matches.getPossibleMatches()).contains("counter1");
        assertThat(matches.getPossibleMatches()).contains("counter2");
        assertThat(matches.getPossibleMatches()).contains("counter3");
    }

    @Test
    public void simpleFieldPropertyErrorMessage() {
        PropertyMatches matches = PropertyMatches.forField("naem", SampleFieldProperties.class);
        String msg = matches.buildErrorMessage();
        assertThat(msg).contains("naem");
        assertThat(msg).contains("name");
        assertThat(msg).contains("field");
        assertThat(msg).doesNotContain("setter");
    }

    @Test
    public void complexFieldPropertyErrorMessage() {
        PropertyMatches matches = PropertyMatches.forField("counter", SampleFieldProperties.class);
        String msg = matches.buildErrorMessage();
        assertThat(msg).contains("counter");
        assertThat(msg).contains("counter1");
        assertThat(msg).contains("counter2");
        assertThat(msg).contains("counter3");
    }

    @SuppressWarnings("unused")
    private static class SampleBeanProperties {

        private String name;

        private String description;

        private int counter1;

        private int counter2;

        private int counter3;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getCounter1() {
            return counter1;
        }

        public void setCounter1(int counter1) {
            this.counter1 = counter1;
        }

        public int getCounter2() {
            return counter2;
        }

        public void setCounter2(int counter2) {
            this.counter2 = counter2;
        }

        public int getCounter3() {
            return counter3;
        }

        public void setCounter3(int counter3) {
            this.counter3 = counter3;
        }
    }

    @SuppressWarnings("unused")
    private static class SampleFieldProperties {

        private String name;

        private String description;

        private int counter1;

        private int counter2;

        private int counter3;
    }
}
