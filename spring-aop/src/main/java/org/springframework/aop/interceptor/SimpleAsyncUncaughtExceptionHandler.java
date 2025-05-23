// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache许可证版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式（明示或暗示）的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.interceptor;

import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 默认的 {@link AsyncUncaughtExceptionHandler}，它仅仅记录异常。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.1
 */
public class SimpleAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Log logger = LogFactory.getLog(SimpleAsyncUncaughtExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        if (logger.isErrorEnabled()) {
            logger.error("Unexpected exception occurred invoking async method: " + method, ex);
        }
    }
}
