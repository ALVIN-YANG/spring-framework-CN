// 翻译完成 glm-4-flash
/** 版权所有 2002-2018 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.testfixture.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Juergen Hoeller
 * @since 2003年11月11日
 */
@SuppressWarnings("rawtypes")
public class IndexedTestBean {

    private TestBean[] array;

    private Collection<?> collection;

    private List list;

    private Set<? super Object> set;

    private SortedSet<? super Object> sortedSet;

    private Map map;

    private SortedMap sortedMap;

    public IndexedTestBean() {
        this(true);
    }

    public IndexedTestBean(boolean populate) {
        if (populate) {
            populate();
        }
    }

    @SuppressWarnings("unchecked")
    public void populate() {
        TestBean tb0 = new TestBean("name0", 0);
        TestBean tb1 = new TestBean("name1", 0);
        TestBean tb2 = new TestBean("name2", 0);
        TestBean tb3 = new TestBean("name3", 0);
        TestBean tb4 = new TestBean("name4", 0);
        TestBean tb5 = new TestBean("name5", 0);
        TestBean tb6 = new TestBean("name6", 0);
        TestBean tb7 = new TestBean("name7", 0);
        TestBean tb8 = new TestBean("name8", 0);
        TestBean tbX = new TestBean("nameX", 0);
        TestBean tbY = new TestBean("nameY", 0);
        this.array = new TestBean[] { tb0, tb1 };
        this.list = new ArrayList<>();
        this.list.add(tb2);
        this.list.add(tb3);
        this.set = new TreeSet<>();
        this.set.add(tb6);
        this.set.add(tb7);
        this.map = new HashMap<>();
        this.map.put("key1", tb4);
        this.map.put("key2", tb5);
        this.map.put("key.3", tb5);
        List list = new ArrayList();
        list.add(tbX);
        list.add(tbY);
        this.map.put("key4", list);
        this.map.put("key5[foo]", tb8);
    }

    public TestBean[] getArray() {
        return array;
    }

    public void setArray(TestBean[] array) {
        this.array = array;
    }

    public Collection<?> getCollection() {
        return collection;
    }

    public void setCollection(Collection<?> collection) {
        this.collection = collection;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Set<?> getSet() {
        return set;
    }

    public void setSet(Set<? super Object> set) {
        this.set = set;
    }

    public SortedSet<? super Object> getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet<? super Object> sortedSet) {
        this.sortedSet = sortedSet;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public SortedMap getSortedMap() {
        return sortedMap;
    }

    public void setSortedMap(SortedMap sortedMap) {
        this.sortedMap = sortedMap;
    }
}
