// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.factory;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.lang.Nullable;

/**
 * 当BeanFactory在尝试从一个Bean定义中创建Bean时遇到错误时抛出异常。
 *
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class BeanCreationException extends FatalBeanException {

    @Nullable
    private final String beanName;

    @Nullable
    private final String resourceDescription;

    @Nullable
    private List<Throwable> relatedCauses;

    /**
     * 创建一个新的 BeanCreationException 对象。
     * @param msg 详细消息
     */
    public BeanCreationException(String msg) {
        super(msg);
        this.beanName = null;
        this.resourceDescription = null;
    }

    /**
     * 创建一个新的 BeanCreationException。
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeanCreationException(String msg, Throwable cause) {
        super(msg, cause);
        this.beanName = null;
        this.resourceDescription = null;
    }

    /**
     * 创建一个新的 BeanCreationException。
     * @param beanName 所请求的 Bean 名称
     * @param msg 详细消息
     */
    public BeanCreationException(String beanName, String msg) {
        super("Error creating bean with name '" + beanName + "': " + msg);
        this.beanName = beanName;
        this.resourceDescription = null;
    }

    /**
     * 创建一个新的 BeanCreationException。
     * @param beanName 请求的 Bean 名称
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeanCreationException(String beanName, String msg, Throwable cause) {
        this(beanName, msg);
        initCause(cause);
    }

    /**
     * 创建一个新的BeanCreationException。
     * @param resourceDescription 描述了Bean定义来自的资源
     * @param beanName 请求的Bean的名称
     * @param msg 详细消息
     */
    public BeanCreationException(@Nullable String resourceDescription, @Nullable String beanName, String msg) {
        super("Error creating bean with name '" + beanName + "'" + (resourceDescription != null ? " defined in " + resourceDescription : "") + ": " + msg);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
        this.relatedCauses = null;
    }

    /**
     * 创建一个新的 BeanCreationException。
     * @param resourceDescription 描述了从该资源中获取的 bean 定义
     * @param beanName 请求的 bean 名称
     * @param msg 详细消息
     * @param cause 根本原因
     */
    public BeanCreationException(@Nullable String resourceDescription, String beanName, String msg, Throwable cause) {
        this(resourceDescription, beanName, msg);
        initCause(cause);
    }

    /**
     * 返回 bean 定义所来源的资源描述，如果有的话。
     */
    @Nullable
    public String getResourceDescription() {
        return this.resourceDescription;
    }

    /**
     * 返回请求的 bean 名称，如果有。
     */
    @Nullable
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * 向这个bean创建异常添加一个相关原因，
     * 它不是失败的直接原因，但在创建相同bean实例的早期已经发生。
     * @param ex 要添加的相关原因
     */
    public void addRelatedCause(Throwable ex) {
        if (this.relatedCauses == null) {
            this.relatedCauses = new ArrayList<>();
        }
        this.relatedCauses.add(ex);
    }

    /**
     * 返回相关原因，如果有的话。
     * @return 相关原因的数组，如果没有则返回 {@code null}
     */
    @Nullable
    public Throwable[] getRelatedCauses() {
        if (this.relatedCauses == null) {
            return null;
        }
        return this.relatedCauses.toArray(new Throwable[0]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (this.relatedCauses != null) {
            for (Throwable relatedCause : this.relatedCauses) {
                sb.append("\nRelated cause: ");
                sb.append(relatedCause);
            }
        }
        return sb.toString();
    }

    @Override
    public void printStackTrace(PrintStream ps) {
        synchronized (ps) {
            super.printStackTrace(ps);
            if (this.relatedCauses != null) {
                for (Throwable relatedCause : this.relatedCauses) {
                    ps.println("Related cause:");
                    relatedCause.printStackTrace(ps);
                }
            }
        }
    }

    @Override
    public void printStackTrace(PrintWriter pw) {
        synchronized (pw) {
            super.printStackTrace(pw);
            if (this.relatedCauses != null) {
                for (Throwable relatedCause : this.relatedCauses) {
                    pw.println("Related cause:");
                    relatedCause.printStackTrace(pw);
                }
            }
        }
    }

    @Override
    public boolean contains(@Nullable Class<?> exClass) {
        if (super.contains(exClass)) {
            return true;
        }
        if (this.relatedCauses != null) {
            for (Throwable relatedCause : this.relatedCauses) {
                if (relatedCause instanceof NestedRuntimeException nested && nested.contains(exClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
