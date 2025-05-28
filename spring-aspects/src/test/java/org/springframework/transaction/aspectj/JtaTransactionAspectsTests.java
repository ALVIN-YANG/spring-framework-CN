// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的的适用性或不侵犯他人权利。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.transaction.aspectj;

import java.io.IOException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.testfixture.CallCountingTransactionManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @作者 Stephane Nicoll
 */
@SpringJUnitConfig(JtaTransactionAspectsTests.Config.class)
public class JtaTransactionAspectsTests {

    @Autowired
    private CallCountingTransactionManager txManager;

    @BeforeEach
    public void setUp() {
        this.txManager.clear();
    }

    @Test
    public void commitOnAnnotatedPublicMethod() throws Throwable {
        assertThat(this.txManager.begun).isEqualTo(0);
        new JtaAnnotationPublicAnnotatedMember().echo(null);
        assertThat(this.txManager.commits).isEqualTo(1);
    }

    @Test
    public void matchingRollbackOnApplied() throws Throwable {
        assertThat(this.txManager.begun).isEqualTo(0);
        InterruptedException test = new InterruptedException();
        assertThatExceptionOfType(InterruptedException.class).isThrownBy(() -> new JtaAnnotationPublicAnnotatedMember().echo(test)).isSameAs(test);
        assertThat(this.txManager.rollbacks).isEqualTo(1);
        assertThat(this.txManager.commits).isEqualTo(0);
    }

    @Test
    public void nonMatchingRollbackOnApplied() throws Throwable {
        assertThat(this.txManager.begun).isEqualTo(0);
        IOException test = new IOException();
        assertThatIOException().isThrownBy(() -> new JtaAnnotationPublicAnnotatedMember().echo(test)).isSameAs(test);
        assertThat(this.txManager.commits).isEqualTo(1);
        assertThat(this.txManager.rollbacks).isEqualTo(0);
    }

    @Test
    public void commitOnAnnotatedProtectedMethod() {
        assertThat(this.txManager.begun).isEqualTo(0);
        new JtaAnnotationProtectedAnnotatedMember().doInTransaction();
        assertThat(this.txManager.commits).isEqualTo(1);
    }

    @Test
    public void nonAnnotatedMethodCallingProtectedMethod() {
        assertThat(this.txManager.begun).isEqualTo(0);
        new JtaAnnotationProtectedAnnotatedMember().doSomething();
        assertThat(this.txManager.commits).isEqualTo(1);
    }

    @Test
    public void commitOnAnnotatedPrivateMethod() {
        assertThat(this.txManager.begun).isEqualTo(0);
        new JtaAnnotationPrivateAnnotatedMember().doInTransaction();
        assertThat(this.txManager.commits).isEqualTo(1);
    }

    @Test
    public void nonAnnotatedMethodCallingPrivateMethod() {
        assertThat(this.txManager.begun).isEqualTo(0);
        new JtaAnnotationPrivateAnnotatedMember().doSomething();
        assertThat(this.txManager.commits).isEqualTo(1);
    }

    @Test
    public void notTransactional() {
        assertThat(this.txManager.begun).isEqualTo(0);
        new TransactionAspectTests.NotTransactional().noop();
        assertThat(this.txManager.begun).isEqualTo(0);
    }

    public static class JtaAnnotationPublicAnnotatedMember {

        @Transactional(rollbackOn = InterruptedException.class)
        public void echo(Throwable t) throws Throwable {
            if (t != null) {
                throw t;
            }
        }
    }

    protected static class JtaAnnotationProtectedAnnotatedMember {

        public void doSomething() {
            doInTransaction();
        }

        @Transactional
        protected void doInTransaction() {
        }
    }

    protected static class JtaAnnotationPrivateAnnotatedMember {

        public void doSomething() {
            doInTransaction();
        }

        @Transactional
        private void doInTransaction() {
        }
    }

    @Configuration
    protected static class Config {

        @Bean
        public CallCountingTransactionManager transactionManager() {
            return new CallCountingTransactionManager();
        }

        @Bean
        public JtaAnnotationTransactionAspect transactionAspect() {
            JtaAnnotationTransactionAspect aspect = JtaAnnotationTransactionAspect.aspectOf();
            aspect.setTransactionManager(transactionManager());
            return aspect;
        }
    }
}
