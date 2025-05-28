// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接处获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.support;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.lang.Nullable;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Juergen Hoeller
 * @author Jean-Pierre PAWLAK
 * @author Chris Beams
 * @since 20.05.2003
 *
 * 作者：Juergen Hoeller
 * 作者：Jean-Pierre PAWLAK
 * 作者：Chris Beams
 * 自：2003年5月20日
 */
public class PagedListHolderTests {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testPagedListHolder() {
        TestBean tb1 = new TestBean();
        tb1.setName("eva");
        tb1.setAge(25);
        TestBean tb2 = new TestBean();
        tb2.setName("juergen");
        tb2.setAge(99);
        TestBean tb3 = new TestBean();
        tb3.setName("Rod");
        tb3.setAge(32);
        List tbs = new ArrayList();
        tbs.add(tb1);
        tbs.add(tb2);
        tbs.add(tb3);
        PagedListHolder holder = new PagedListHolder(tbs);
        assertThat(holder.getSource()).as("Correct source").isSameAs(tbs);
        assertThat(holder.getNrOfElements()).as("Correct number of elements").isEqualTo(3);
        assertThat(holder.getPageCount()).as("Correct number of pages").isEqualTo(1);
        assertThat(holder.getPageSize()).as("Correct page size").isEqualTo(PagedListHolder.DEFAULT_PAGE_SIZE);
        assertThat(holder.getPage()).as("Correct page number").isEqualTo(0);
        assertThat(holder.isFirstPage()).as("First page").isTrue();
        assertThat(holder.isLastPage()).as("Last page").isTrue();
        assertThat(holder.getFirstElementOnPage()).as("Correct first element").isEqualTo(0);
        assertThat(holder.getLastElementOnPage()).as("Correct first element").isEqualTo(2);
        assertThat(holder.getPageList().size()).as("Correct page list size").isEqualTo(3);
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb1);
        assertThat(holder.getPageList().get(1)).as("Correct page list contents").isSameAs(tb2);
        assertThat(holder.getPageList().get(2)).as("Correct page list contents").isSameAs(tb3);
        holder.setPageSize(2);
        assertThat(holder.getPageCount()).as("Correct number of pages").isEqualTo(2);
        assertThat(holder.getPageSize()).as("Correct page size").isEqualTo(2);
        assertThat(holder.getPage()).as("Correct page number").isEqualTo(0);
        assertThat(holder.isFirstPage()).as("First page").isTrue();
        assertThat(holder.isLastPage()).as("Last page").isFalse();
        assertThat(holder.getFirstElementOnPage()).as("Correct first element").isEqualTo(0);
        assertThat(holder.getLastElementOnPage()).as("Correct last element").isEqualTo(1);
        assertThat(holder.getPageList().size()).as("Correct page list size").isEqualTo(2);
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb1);
        assertThat(holder.getPageList().get(1)).as("Correct page list contents").isSameAs(tb2);
        holder.setPage(1);
        assertThat(holder.getPage()).as("Correct page number").isEqualTo(1);
        assertThat(holder.isFirstPage()).as("First page").isFalse();
        assertThat(holder.isLastPage()).as("Last page").isTrue();
        assertThat(holder.getFirstElementOnPage()).as("Correct first element").isEqualTo(2);
        assertThat(holder.getLastElementOnPage()).as("Correct last element").isEqualTo(2);
        assertThat(holder.getPageList().size()).as("Correct page list size").isEqualTo(1);
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb3);
        holder.setPageSize(3);
        assertThat(holder.getPageCount()).as("Correct number of pages").isEqualTo(1);
        assertThat(holder.getPageSize()).as("Correct page size").isEqualTo(3);
        assertThat(holder.getPage()).as("Correct page number").isEqualTo(0);
        assertThat(holder.isFirstPage()).as("First page").isTrue();
        assertThat(holder.isLastPage()).as("Last page").isTrue();
        assertThat(holder.getFirstElementOnPage()).as("Correct first element").isEqualTo(0);
        assertThat(holder.getLastElementOnPage()).as("Correct last element").isEqualTo(2);
        holder.setPage(1);
        holder.setPageSize(2);
        assertThat(holder.getPageCount()).as("Correct number of pages").isEqualTo(2);
        assertThat(holder.getPageSize()).as("Correct page size").isEqualTo(2);
        assertThat(holder.getPage()).as("Correct page number").isEqualTo(1);
        assertThat(holder.isFirstPage()).as("First page").isFalse();
        assertThat(holder.isLastPage()).as("Last page").isTrue();
        assertThat(holder.getFirstElementOnPage()).as("Correct first element").isEqualTo(2);
        assertThat(holder.getLastElementOnPage()).as("Correct last element").isEqualTo(2);
        holder.setPageSize(2);
        holder.setPage(1);
        ((MutableSortDefinition) holder.getSort()).setProperty("name");
        ((MutableSortDefinition) holder.getSort()).setIgnoreCase(false);
        holder.resort();
        assertThat(holder.getSource()).as("Correct source").isSameAs(tbs);
        assertThat(holder.getNrOfElements()).as("Correct number of elements").isEqualTo(3);
        assertThat(holder.getPageCount()).as("Correct number of pages").isEqualTo(2);
        assertThat(holder.getPageSize()).as("Correct page size").isEqualTo(2);
        assertThat(holder.getPage()).as("Correct page number").isEqualTo(0);
        assertThat(holder.isFirstPage()).as("First page").isTrue();
        assertThat(holder.isLastPage()).as("Last page").isFalse();
        assertThat(holder.getFirstElementOnPage()).as("Correct first element").isEqualTo(0);
        assertThat(holder.getLastElementOnPage()).as("Correct last element").isEqualTo(1);
        assertThat(holder.getPageList().size()).as("Correct page list size").isEqualTo(2);
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb3);
        assertThat(holder.getPageList().get(1)).as("Correct page list contents").isSameAs(tb1);
        ((MutableSortDefinition) holder.getSort()).setProperty("name");
        holder.resort();
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb2);
        assertThat(holder.getPageList().get(1)).as("Correct page list contents").isSameAs(tb1);
        ((MutableSortDefinition) holder.getSort()).setProperty("name");
        holder.resort();
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb3);
        assertThat(holder.getPageList().get(1)).as("Correct page list contents").isSameAs(tb1);
        holder.setPage(1);
        assertThat(holder.getPageList().size()).as("Correct page list size").isEqualTo(1);
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb2);
        ((MutableSortDefinition) holder.getSort()).setProperty("age");
        holder.resort();
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb1);
        assertThat(holder.getPageList().get(1)).as("Correct page list contents").isSameAs(tb3);
        ((MutableSortDefinition) holder.getSort()).setIgnoreCase(true);
        holder.resort();
        assertThat(holder.getPageList().get(0)).as("Correct page list contents").isSameAs(tb1);
        assertThat(holder.getPageList().get(1)).as("Correct page list contents").isSameAs(tb3);
        holder.nextPage();
        assertThat(holder.getPage()).isEqualTo(1);
        holder.previousPage();
        assertThat(holder.getPage()).isEqualTo(0);
        holder.nextPage();
        assertThat(holder.getPage()).isEqualTo(1);
        holder.nextPage();
        assertThat(holder.getPage()).isEqualTo(1);
        holder.previousPage();
        assertThat(holder.getPage()).isEqualTo(0);
        holder.previousPage();
        assertThat(holder.getPage()).isEqualTo(0);
    }

    public static class MockFilter {

        private String name = "";

        private String age = "";

        private String extendedInfo = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getExtendedInfo() {
            return extendedInfo;
        }

        public void setExtendedInfo(String extendedInfo) {
            this.extendedInfo = extendedInfo;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MockFilter mockFilter)) {
                return false;
            }
            if (!age.equals(mockFilter.age)) {
                return false;
            }
            if (!extendedInfo.equals(mockFilter.extendedInfo)) {
                return false;
            }
            if (!name.equals(mockFilter.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result;
            result = name.hashCode();
            result = 29 * result + age.hashCode();
            result = 29 * result + extendedInfo.hashCode();
            return result;
        }
    }
}
