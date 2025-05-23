// 翻译完成 glm-4-flash
/*版权所有 2002-2013 原作者或作者们。

根据Apache许可证版本2.0（“许可证”）授权；
除非遵守许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件
按“原样”分发，不提供任何形式的保证或条件，
无论是明示的、默示的，还是与特定目的相关的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;

/**
 * AspectJ的{@link IMessageHandler}接口实现，该实现通过与常规Spring消息相同的日志系统路由AspectJ编织消息。
 *
 * <p>将以下选项传递给编织器；例如，在一个"{@code META-INF/aop.xml}"文件中指定以下内容：
 *
 * <p><code class="code">&lt;weaver options="..."/&gt;</code>
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectJWeaverMessageHandler implements IMessageHandler {

    private static final String AJ_ID = "[AspectJ] ";

    private static final Log logger = LogFactory.getLog("AspectJ Weaver");

    @Override
    public boolean handleMessage(IMessage message) throws AbortException {
        Kind messageKind = message.getKind();
        if (messageKind == IMessage.DEBUG) {
            if (logger.isDebugEnabled()) {
                logger.debug(makeMessageFor(message));
                return true;
            }
        } else if (messageKind == IMessage.INFO || messageKind == IMessage.WEAVEINFO) {
            if (logger.isInfoEnabled()) {
                logger.info(makeMessageFor(message));
                return true;
            }
        } else if (messageKind == IMessage.WARNING) {
            if (logger.isWarnEnabled()) {
                logger.warn(makeMessageFor(message));
                return true;
            }
        } else if (messageKind == IMessage.ERROR) {
            if (logger.isErrorEnabled()) {
                logger.error(makeMessageFor(message));
                return true;
            }
        } else if (messageKind == IMessage.ABORT) {
            if (logger.isFatalEnabled()) {
                logger.fatal(makeMessageFor(message));
                return true;
            }
        }
        return false;
    }

    private String makeMessageFor(IMessage aMessage) {
        return AJ_ID + aMessage.getMessage();
    }

    @Override
    public boolean isIgnoring(Kind messageKind) {
        // 我们希望看到所有内容，并允许动态配置日志级别。
        return false;
    }

    @Override
    public void dontIgnore(Kind messageKind) {
        // 我们无论如何都没有忽视任何事情...
    }

    @Override
    public void ignore(Kind kind) {
        // 我们本来就没有忽略任何事情...
    }
}
