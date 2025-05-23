// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据Apache许可证2.0版（“许可证”）；除非遵守许可证，否则您不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、特定用途适用性的保证。
* 请参阅许可证了解具体管理权限和限制的语言。*/
package org.springframework.beans.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * PagedListHolder 是一个简单的状态持有者，用于处理对象列表，并将它们分成页面。页面编号从 0 开始。
 *
 * <p>这主要针对在 Web UI 中的使用。通常，一个实例将与一个豆子列表一起实例化，放入会话中，并作为模型导出。所有属性都可以通过程序设置/获取，但最常见的方式将是数据绑定，即从请求参数中填充豆子。获取器将主要被视图使用。
 *
 * <p>支持通过一个 {@link SortDefinition} 实现对底层列表进行排序，该实现作为属性 "sort" 提供。默认情况下，将使用一个 {@link MutableSortDefinition} 实例，在设置相同属性时切换升序值。
 *
 * <p>数据绑定名称必须称为 "pageSize" 和 "sort.ascending"，这是 BeanWrapper 所期望的。请注意，名称和嵌套语法与相应的 JSTL EL 表达式相匹配，例如 "myModelAttr.pageSize" 和 "myModelAttr.sort.ascending"。
 *
 * @author Juergen Hoeller
 * @since 19.05.2003
 * @param <E> 元素类型
 * @see #getPageList()
 * @see org.springframework.beans.support.MutableSortDefinition
 */
@SuppressWarnings("serial")
public class PagedListHolder<E> implements Serializable {

    /**
     * 默认的页面大小。
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 默认的最大页面链接数。
     */
    public static final int DEFAULT_MAX_LINKED_PAGES = 10;

    private List<E> source = Collections.emptyList();

    @Nullable
    private Date refreshDate;

    @Nullable
    private SortDefinition sort;

    @Nullable
    private SortDefinition sortUsed;

    private int pageSize = DEFAULT_PAGE_SIZE;

    private int page = 0;

    private boolean newPageSet;

    private int maxLinkedPages = DEFAULT_MAX_LINKED_PAGES;

    /**
     * 创建一个新的持有者实例。
     * 您需要设置一个源列表才能使用该持有者。
     * @see #setSource
     */
    public PagedListHolder() {
        this(new ArrayList<>(0));
    }

    /**
     * 使用给定的源列表创建一个新的持有实例，以默认的排序定义（激活了“toggleAscendingOnProperty”）开始。
     * @param source 源列表
     * @see MutableSortDefinition#setToggleAscendingOnProperty
     */
    public PagedListHolder(List<E> source) {
        this(source, new MutableSortDefinition(true));
    }

    /**
     * 使用给定的源列表创建一个新的持有者实例。
     * @param source 源 List
     * @param sort 要开始的排序定义
     */
    public PagedListHolder(List<E> source, SortDefinition sort) {
        setSource(source);
        setSort(sort);
    }

    /**
     * 设置此持有者的源列表。
     */
    public void setSource(List<E> source) {
        Assert.notNull(source, "Source List must not be null");
        this.source = source;
        this.refreshDate = new Date();
        this.sortUsed = null;
    }

    /**
     * 返回此持有者的源列表。
     */
    public List<E> getSource() {
        return this.source;
    }

    /**
     * 返回列表上次从源提供者获取的时间。
     */
    @Nullable
    public Date getRefreshDate() {
        return this.refreshDate;
    }

    /**
     * 设置此持有者的排序定义。
     * 通常是一个MutableSortDefinition的实例。
     * @see org.springframework.beans.support.MutableSortDefinition
     */
    public void setSort(@Nullable SortDefinition sort) {
        this.sort = sort;
    }

    /**
     * 返回此持有者的排序定义。
     */
    @Nullable
    public SortDefinition getSort() {
        return this.sort;
    }

    /**
     * 设置当前页面大小。
     * 如果更改了当前页码，则重置当前页码。
     * <p>默认值为10。
     */
    public void setPageSize(int pageSize) {
        if (pageSize != this.pageSize) {
            this.pageSize = pageSize;
            if (!this.newPageSet) {
                this.page = 0;
            }
        }
    }

    /**
     * 返回当前页面大小。
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * 设置当前页码。
     * 页码从0开始计数。
     */
    public void setPage(int page) {
        this.page = page;
        this.newPageSet = true;
    }

    /**
     * 返回当前页码。
     * 页码从0开始计数。
     */
    public int getPage() {
        this.newPageSet = false;
        if (this.page >= getPageCount()) {
            this.page = getPageCount() - 1;
        }
        return this.page;
    }

    /**
     * 将页码链接的最大数量设置为当前页码周围的一些页面。
     */
    public void setMaxLinkedPages(int maxLinkedPages) {
        this.maxLinkedPages = maxLinkedPages;
    }

    /**
     * 返回当前页周围的几个页面中页码链接的最大数量。
     */
    public int getMaxLinkedPages() {
        return this.maxLinkedPages;
    }

    /**
     * 返回当前源列表的页数。
     */
    public int getPageCount() {
        float nrOfPages = (float) getNrOfElements() / getPageSize();
        return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }

    /**
     * 如果当前页面是第一页，则返回。
     */
    public boolean isFirstPage() {
        return getPage() == 0;
    }

    /**
     * 如果当前页面是最后一页，则返回。
     */
    public boolean isLastPage() {
        return getPage() == getPageCount() - 1;
    }

    /**
     * 切换到上一页。
     * 如果已经在第一页，则将保持在第一页。
     */
    public void previousPage() {
        if (!isFirstPage()) {
            this.page--;
        }
    }

    /**
     * 切换到下一页。
     * 如果已经在最后一页，则将停留在最后一页。
     */
    public void nextPage() {
        if (!isLastPage()) {
            this.page++;
        }
    }

    /**
     * 返回源列表中的元素总数。
     */
    public int getNrOfElements() {
        return getSource().size();
    }

    /**
     * 返回当前页面上第一个元素的索引。
     * 元素编号从0开始。
     */
    public int getFirstElementOnPage() {
        return (getPageSize() * getPage());
    }

    /**
     * 返回当前页面上最后一个元素的索引。
     * 元素编号从0开始。
     */
    public int getLastElementOnPage() {
        int endIndex = getPageSize() * (getPage() + 1);
        int size = getNrOfElements();
        return (endIndex > size ? size : endIndex) - 1;
    }

    /**
     * 返回表示当前页面的子列表。
     */
    public List<E> getPageList() {
        return getSource().subList(getFirstElementOnPage(), getLastElementOnPage() + 1);
    }

    /**
     * 返回当前页面周围创建链接的第一页。
     */
    public int getFirstLinkedPage() {
        return Math.max(0, getPage() - (getMaxLinkedPages() / 2));
    }

    /**
     * 返回围绕当前页面创建链接的最后一页。
     */
    public int getLastLinkedPage() {
        return Math.min(getFirstLinkedPage() + getMaxLinkedPages() - 1, getPageCount() - 1);
    }

    /**
     * 如有必要，则重新排列列表，即如果当前的 {@code sort} 实例不等于备份的 {@code sortUsed} 实例。
     * <p>调用 {@code doSort} 来触发实际排序。
     * @see #doSort
     */
    public void resort() {
        SortDefinition sort = getSort();
        if (sort != null && !sort.equals(this.sortUsed)) {
            this.sortUsed = copySortDefinition(sort);
            doSort(getSource(), sort);
            setPage(0);
        }
    }

    /**
     *  创建给定排序定义的深度副本，
     *   * 用作状态持有者，以便将修改后的排序定义与之比较。
     *   * <p>默认实现创建一个MutableSortDefinition实例。
     *   * 可在子类中重写，特别是在对SortDefinition接口进行自定义扩展的情况下。
     *   * 允许返回null，这意味着不会保留任何排序状态，触发每次调用{@code resort}时的实际排序。
     *   * @param sort 当前SortDefinition对象
     *   * @return SortDefinition对象的深度副本
     *   * @see MutableSortDefinition#MutableSortDefinition(SortDefinition)
     */
    protected SortDefinition copySortDefinition(SortDefinition sort) {
        return new MutableSortDefinition(sort);
    }

    /**
     * 实际上根据给定的排序定义对给定的源列表进行排序。
     * <p>默认实现使用Spring的PropertyComparator。
     * 可以在子类中重写。
     * @see PropertyComparator#sort(java.util.List, SortDefinition)
     */
    protected void doSort(List<E> source, SortDefinition sort) {
        PropertyComparator.sort(source, sort);
    }
}
