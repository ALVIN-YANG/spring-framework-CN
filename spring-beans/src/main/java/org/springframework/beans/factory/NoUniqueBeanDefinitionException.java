// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 抛出异常的情况是，当请求一个 `BeanFactory` 获取一个实例时，预期只找到一个匹配的bean，但实际上找到了多个匹配的候选者。
 *
 * @author Juergen Hoeller
 * @since 3.2.1
 * @see BeanFactory#getBean(Class)
 */
@SuppressWarnings("serial")
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

    private final int numberOfBeansFound;

    @Nullable
    private final Collection<String> beanNamesFound;

    /**
     * 创建一个新的 {@code NoUniqueBeanDefinitionException}。
     * @param type 需要的非唯一bean的类型
     * @param numberOfBeansFound 匹配的bean数量
     * @param message 描述问题的详细消息
     */
    public NoUniqueBeanDefinitionException(Class<?> type, int numberOfBeansFound, String message) {
        super(type, message);
        this.numberOfBeansFound = numberOfBeansFound;
        this.beanNamesFound = null;
    }

    /**
     * 创建一个新的 {@code NoUniqueBeanDefinitionException}。
     * @param type 非唯一bean所需类型
     * @param beanNamesFound 所有匹配bean的名称（作为一个集合）
     */
    public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound) {
        super(type, "expected single matching bean but found " + beanNamesFound.size() + ": " + StringUtils.collectionToCommaDelimitedString(beanNamesFound));
        this.numberOfBeansFound = beanNamesFound.size();
        this.beanNamesFound = new ArrayList<>(beanNamesFound);
    }

    /**
     * 创建一个新的 {@code NoUniqueBeanDefinitionException}。
     * @param type 需要的非唯一bean的类型
     * @param beanNamesFound 所有匹配的bean的名称（作为一个数组）
     */
    public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
        this(type, Arrays.asList(beanNamesFound));
    }

    /**
     * 创建一个新的 {@code NoUniqueBeanDefinitionException}。
     * @param type 需要的非唯一bean的类型
     * @param beanNamesFound 所有匹配的bean的名称（作为一个集合）
     * @since 5.1
     */
    public NoUniqueBeanDefinitionException(ResolvableType type, Collection<String> beanNamesFound) {
        super(type, "expected single matching bean but found " + beanNamesFound.size() + ": " + StringUtils.collectionToCommaDelimitedString(beanNamesFound));
        this.numberOfBeansFound = beanNamesFound.size();
        this.beanNamesFound = new ArrayList<>(beanNamesFound);
    }

    /**
     * 创建一个新的 {@code NoUniqueBeanDefinitionException}。
     * @param type 需要的非唯一Bean的类型
     * @param beanNamesFound 所有匹配的Bean的名称（作为数组）
     * @since 5.1
     */
    public NoUniqueBeanDefinitionException(ResolvableType type, String... beanNamesFound) {
        this(type, Arrays.asList(beanNamesFound));
    }

    /**
     * 返回当预期只找到一枚匹配的豆子时找到的豆子数量。
     * 对于`NoUniqueBeanDefinitionException`，这通常会大于1。
     * @see #getBeanType()
     */
    @Override
    public int getNumberOfBeansFound() {
        return this.numberOfBeansFound;
    }

    /**
     * 返回在仅期望找到单个匹配的Bean时找到的所有Bean的名称。
     * 注意，如果未在构造时指定，这可能是`null`。
     * @since 4.3
     * @see #getBeanType()
     */
    @Nullable
    public Collection<String> getBeanNamesFound() {
        return this.beanNamesFound;
    }
}
