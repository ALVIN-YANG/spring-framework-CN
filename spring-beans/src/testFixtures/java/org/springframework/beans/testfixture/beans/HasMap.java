// 翻译完成 glm-4-flash
/** 版权所有 2002-2016 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”），您可能不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性、特定用途的适用性或不侵犯第三方权利。
* 请参阅许可证了解具体规定许可权限和限制。*/
package org.springframework.beans.testfixture.beans;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 暴露地图的Bean。用于Bean工厂测试。
 *
 * @author Rod Johnson
 * @since 2003年6月5日
 */
public class HasMap {

    private Map<?, ?> map;

    private Set<?> set;

    private Properties props;

    private Object[] objectArray;

    private Integer[] intArray;

    private Class<?>[] classArray;

    private List<Class<?>> classList;

    private IdentityHashMap<?, ?> identityMap;

    private CopyOnWriteArraySet<?> concurrentSet;

    private HasMap() {
    }

    public Map<?, ?> getMap() {
        return map;
    }

    public void setMap(Map<?, ?> map) {
        this.map = map;
    }

    public Set<?> getSet() {
        return set;
    }

    public void setSet(Set<?> set) {
        this.set = set;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public Object[] getObjectArray() {
        return objectArray;
    }

    public void setObjectArray(Object[] objectArray) {
        this.objectArray = objectArray;
    }

    public Integer[] getIntegerArray() {
        return intArray;
    }

    public void setIntegerArray(Integer[] is) {
        intArray = is;
    }

    public Class<?>[] getClassArray() {
        return classArray;
    }

    public void setClassArray(Class<?>[] classArray) {
        this.classArray = classArray;
    }

    public List<Class<?>> getClassList() {
        return classList;
    }

    public void setClassList(List<Class<?>> classList) {
        this.classList = classList;
    }

    public IdentityHashMap<?, ?> getIdentityMap() {
        return identityMap;
    }

    public void setIdentityMap(IdentityHashMap<?, ?> identityMap) {
        this.identityMap = identityMap;
    }

    public CopyOnWriteArraySet<?> getConcurrentSet() {
        return concurrentSet;
    }

    public void setConcurrentSet(CopyOnWriteArraySet<?> concurrentSet) {
        this.concurrentSet = concurrentSet;
    }
}
