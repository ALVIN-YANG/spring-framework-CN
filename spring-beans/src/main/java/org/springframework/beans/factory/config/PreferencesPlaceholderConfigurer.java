// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证，了解具体规定许可权限和限制的条款。*/
package org.springframework.beans.factory.config;

import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;

/**
 * PropertyPlaceholderConfigurer 的子类，支持 JDK 1.4 的 Preferences API ({@code java.util.prefs}).
 *
 * <p>首先尝试在用户偏好中解析占位符作为键，然后是在系统偏好中，最后是在此配置器的属性中。因此，如果没有定义相应的偏好，则行为类似于 PropertyPlaceholderConfigurer。
 *
 * <p>支持自定义系统和用户偏好树路径。还支持在占位符中指定的自定义路径（例如，“myPath/myPlaceholderKey”）。如果没有指定，则使用相应的根节点。
 *
 * @author Juergen Hoeller
 * @since 2004年2月16日
 * @see #setSystemTreePath
 * @see #setUserTreePath
 * @see java.util.prefs.Preferences
 * @deprecated 自 5.2 版本以来已弃用，包括 {@link PropertyPlaceholderConfigurer}
 */
@Deprecated
public class PreferencesPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

    @Nullable
    private String systemTreePath;

    @Nullable
    private String userTreePath;

    private Preferences systemPrefs = Preferences.systemRoot();

    private Preferences userPrefs = Preferences.userRoot();

    /**
     * 设置用于解析占位符的系统首选项树中的路径。默认为根节点。
     */
    public void setSystemTreePath(String systemTreePath) {
        this.systemTreePath = systemTreePath;
    }

    /**
     * 设置用于解析占位符的系统偏好树中的路径。默认为根节点。
     */
    public void setUserTreePath(String userTreePath) {
        this.userTreePath = userTreePath;
    }

    /**
     * 此实现会积极地获取所需系统及用户树节点上的Preferences实例。
     */
    @Override
    public void afterPropertiesSet() {
        if (this.systemTreePath != null) {
            this.systemPrefs = this.systemPrefs.node(this.systemTreePath);
        }
        if (this.userTreePath != null) {
            this.userPrefs = this.userPrefs.node(this.userTreePath);
        }
    }

    /**
     * 此实现首先尝试在用户偏好设置中，然后是在系统偏好设置中，最后是在传入的属性中解析占位符作为键。
     */
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String path = null;
        String key = placeholder;
        int endOfPath = placeholder.lastIndexOf('/');
        if (endOfPath != -1) {
            path = placeholder.substring(0, endOfPath);
            key = placeholder.substring(endOfPath + 1);
        }
        String value = resolvePlaceholder(path, key, this.userPrefs);
        if (value == null) {
            value = resolvePlaceholder(path, key, this.systemPrefs);
            if (value == null) {
                value = props.getProperty(placeholder);
            }
        }
        return value;
    }

    /**
     * 解析给定的路径和键相对于给定的首选项。
     * @param path 首选项路径（'/'之前的占位符部分）
     * @param key 首选项键（'/'之后的占位符部分）
     * @param preferences 要解析的首选项
     * @return 占位符的值，或如果没有找到则返回 {@code null}
     */
    @Nullable
    protected String resolvePlaceholder(@Nullable String path, String key, Preferences preferences) {
        if (path != null) {
            // 如果节点不存在，则不要创建该节点。
            try {
                if (preferences.nodeExists(path)) {
                    return preferences.node(path).get(key, null);
                } else {
                    return null;
                }
            } catch (BackingStoreException ex) {
                throw new BeanDefinitionStoreException("Cannot access specified node path [" + path + "]", ex);
            }
        } else {
            return preferences.get(key, null);
        }
    }
}
