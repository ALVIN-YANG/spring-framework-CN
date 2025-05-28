// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）
* 使用本文件不得违反许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，
* 在许可证下分发的软件按“原样”分发，
* 不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guillaume Poirier
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 08.03.2004
 *
 * 作者：Guillaume Poirier
 * 作者：Juergen Hoeller
 * 作者：Chris Beams
 * 自：2004年3月8日
 */
class ConcurrentBeanWrapperTests {

    private final Log logger = LogFactory.getLog(getClass());

    private final Set<TestRun> set = ConcurrentHashMap.newKeySet();

    private Throwable ex = null;

    @RepeatedTest(100)
    void testSingleThread() {
        performSet();
    }

    @Test
    void testConcurrent() {
        for (int i = 0; i < 10; i++) {
            TestRun run = new TestRun(this);
            set.add(run);
            Thread t = new Thread(run);
            t.setDaemon(true);
            t.start();
        }
        logger.info("Thread creation over, " + set.size() + " still active.");
        synchronized (this) {
            while (!set.isEmpty() && ex == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger.info(e.toString());
                }
                logger.info(set.size() + " threads still active.");
            }
        }
        if (ex != null) {
            throw new AssertionError("Unexpected exception", ex);
        }
    }

    private static void performSet() {
        TestBean bean = new TestBean();
        Properties p = (Properties) System.getProperties().clone();
        assertThat(p).as("The System properties must not be empty").isNotEmpty();
        for (Iterator<?> i = p.entrySet().iterator(); i.hasNext(); ) {
            i.next();
            if (Math.random() > 0.9) {
                i.remove();
            }
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            p.store(buffer, null);
        } catch (IOException e) {
            // `ByteArrayOutputStream` 不抛出异常
            // 任何 IOException
        }
        String value = new String(buffer.toByteArray());
        BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
        wrapper.setPropertyValue("properties", value);
        assertThat(bean.getProperties()).isEqualTo(p);
    }

    private static class TestRun implements Runnable {

        private ConcurrentBeanWrapperTests test;

        TestRun(ConcurrentBeanWrapperTests test) {
            this.test = test;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < 100; i++) {
                    performSet();
                }
            } catch (Throwable e) {
                test.ex = e;
            } finally {
                synchronized (test) {
                    test.set.remove(this);
                    test.notifyAll();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static class TestBean {

        private Properties properties;

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }
    }
}
