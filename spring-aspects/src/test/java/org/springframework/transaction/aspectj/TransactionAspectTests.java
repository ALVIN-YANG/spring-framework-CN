// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可；
* 除非符合许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式（明示或暗示）的保证或条件。
* 请参阅许可证以了解具体的管理权限和限制。*/
package org.springframework.transaction.aspectj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.testfixture.CallCountingTransactionManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

/**
 * @作者 Rod Johnson
 * @作者 Ramnivas Laddad
 * @作者 Juergen Hoeller
 * @作者 Sam Brannen
 */
public class TransactionAspectTests {

    private final CallCountingTransactionManager txManager = new CallCountingTransactionManager();

    private final TransactionalAnnotationOnlyOnClassWithNoInterface annotationOnlyOnClassWithNoInterface = new TransactionalAnnotationOnlyOnClassWithNoInterface();

    private final ClassWithProtectedAnnotatedMember beanWithAnnotatedProtectedMethod = new ClassWithProtectedAnnotatedMember();

    private final ClassWithPrivateAnnotatedMember beanWithAnnotatedPrivateMethod = new ClassWithPrivateAnnotatedMember();

    private final MethodAnnotationOnClassWithNoInterface methodAnnotationOnly = new MethodAnnotationOnClassWithNoInterface();

    @BeforeEach
    public void initContext() {
        AnnotationTransactionAspect.aspectOf().setTransactionManager(txManager);
    }

    @Test
    public void testCommitOnAnnotatedClass() throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        annotationOnlyOnClassWithNoInterface.echo(null);
        assertThat(txManager.commits).isEqualTo(1);
    }

    @Test
    public void commitOnAnnotatedProtectedMethod() throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        beanWithAnnotatedProtectedMethod.doInTransaction();
        assertThat(txManager.commits).isEqualTo(1);
    }

    @Test
    public void commitOnAnnotatedPrivateMethod() throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        beanWithAnnotatedPrivateMethod.doSomething();
        assertThat(txManager.commits).isEqualTo(1);
    }

    @Test
    public void commitOnNonAnnotatedNonPublicMethodInTransactionalType() throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        annotationOnlyOnClassWithNoInterface.nonTransactionalMethod();
        assertThat(txManager.begun).isEqualTo(0);
    }

    @Test
    public void commitOnAnnotatedMethod() throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        methodAnnotationOnly.echo(null);
        assertThat(txManager.commits).isEqualTo(1);
    }

    @Test
    public void notTransactional() throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        new NotTransactional().noop();
        assertThat(txManager.begun).isEqualTo(0);
    }

    @Test
    public void defaultCommitOnAnnotatedClass() throws Throwable {
        Exception ex = new Exception();
        assertThatException().isThrownBy(() -> testRollback(() -> annotationOnlyOnClassWithNoInterface.echo(ex), false)).isSameAs(ex);
    }

    @Test
    public void defaultRollbackOnAnnotatedClass() throws Throwable {
        RuntimeException ex = new RuntimeException();
        assertThatRuntimeException().isThrownBy(() -> testRollback(() -> annotationOnlyOnClassWithNoInterface.echo(ex), true)).isSameAs(ex);
    }

    @Test
    public void defaultCommitOnSubclassOfAnnotatedClass() throws Throwable {
        Exception ex = new Exception();
        assertThatException().isThrownBy(() -> testRollback(() -> new SubclassOfClassWithTransactionalAnnotation().echo(ex), false)).isSameAs(ex);
    }

    @Test
    public void defaultCommitOnSubclassOfClassWithTransactionalMethodAnnotated() throws Throwable {
        Exception ex = new Exception();
        assertThatException().isThrownBy(() -> testRollback(() -> new SubclassOfClassWithTransactionalMethodAnnotation().echo(ex), false)).isSameAs(ex);
    }

    @Test
    public void noCommitOnImplementationOfAnnotatedInterface() throws Throwable {
        Exception ex = new Exception();
        testNotTransactional(() -> new ImplementsAnnotatedInterface().echo(ex), ex);
    }

    @Test
    public void noRollbackOnImplementationOfAnnotatedInterface() throws Throwable {
        Exception rollbackProvokingException = new RuntimeException();
        testNotTransactional(() -> new ImplementsAnnotatedInterface().echo(rollbackProvokingException), rollbackProvokingException);
    }

    protected void testRollback(TransactionOperationCallback toc, boolean rollback) throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        try {
            toc.performTransactionalOperation();
        } finally {
            assertThat(txManager.begun).isEqualTo(1);
            long expected1 = rollback ? 0 : 1;
            assertThat(txManager.commits).isEqualTo(expected1);
            long expected = rollback ? 1 : 0;
            assertThat(txManager.rollbacks).isEqualTo(expected);
        }
    }

    protected void testNotTransactional(TransactionOperationCallback toc, Throwable expected) throws Throwable {
        txManager.clear();
        assertThat(txManager.begun).isEqualTo(0);
        assertThatExceptionOfType(Throwable.class).isThrownBy(toc::performTransactionalOperation).isSameAs(expected);
        assertThat(txManager.begun).isEqualTo(0);
    }

    private interface TransactionOperationCallback {

        Object performTransactionalOperation() throws Throwable;
    }

    public static class SubclassOfClassWithTransactionalAnnotation extends TransactionalAnnotationOnlyOnClassWithNoInterface {
    }

    public static class SubclassOfClassWithTransactionalMethodAnnotation extends MethodAnnotationOnClassWithNoInterface {
    }

    public static class ImplementsAnnotatedInterface implements ITransactional {

        @Override
        public Object echo(Throwable t) throws Throwable {
            if (t != null) {
                throw t;
            }
            return t;
        }
    }

    public static class NotTransactional {

        public void noop() {
        }
    }
}
