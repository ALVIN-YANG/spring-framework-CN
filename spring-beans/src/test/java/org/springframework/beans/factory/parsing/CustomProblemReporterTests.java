// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * @author Rob Harrop
 * @author Chris Beams
 * @since 2.0
 *
 * 作者：Rob Harrop
 * 作者：Chris Beams
 * 自：2.0版本以来
 */
public class CustomProblemReporterTests {

    private CollatingProblemReporter problemReporter;

    private DefaultListableBeanFactory beanFactory;

    private XmlBeanDefinitionReader reader;

    @BeforeEach
    public void setup() {
        this.problemReporter = new CollatingProblemReporter();
        this.beanFactory = new DefaultListableBeanFactory();
        this.reader = new XmlBeanDefinitionReader(this.beanFactory);
        this.reader.setProblemReporter(this.problemReporter);
    }

    @Test
    public void testErrorsAreCollated() {
        this.reader.loadBeanDefinitions(qualifiedResource(CustomProblemReporterTests.class, "context.xml"));
        assertThat(this.problemReporter.getErrors()).as("Incorrect number of errors collated").hasSize(4);
        TestBean bean = (TestBean) this.beanFactory.getBean("validBean");
        assertThat(bean).isNotNull();
    }

    private static class CollatingProblemReporter implements ProblemReporter {

        private final List<Problem> errors = new ArrayList<>();

        private final List<Problem> warnings = new ArrayList<>();

        @Override
        public void fatal(Problem problem) {
            throw new BeanDefinitionParsingException(problem);
        }

        @Override
        public void error(Problem problem) {
            this.errors.add(problem);
        }

        public Problem[] getErrors() {
            return this.errors.toArray(new Problem[this.errors.size()]);
        }

        @Override
        public void warning(Problem problem) {
            this.warnings.add(problem);
        }
    }
}
