// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用；
* 您只能根据许可证使用此文件，除非法律要求或经书面同意。
* 您可以在以下网址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何形式，无论是明示的还是暗示的保证或条件。
* 请参阅许可证以获取管理许可权限和限制的具体语言。*/
package org.springframework.beans.propertyeditors;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试将字符串转换为 {@link java.util.Properties} 对象，
 * 以及其他属性编辑器的转换功能。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rick Evans
 */
public class PropertiesEditorTests {

    @Test
    public void oneProperty() {
        String s = "foo=bar";
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(s);
        Properties p = (Properties) pe.getValue();
        assertThat(p.entrySet().size()).as("contains one entry").isEqualTo(1);
        assertThat(p.get("foo").equals("bar")).as("foo=bar").isTrue();
    }

    @Test
    public void twoProperties() {
        String s = "foo=bar with whitespace\n" + "me=mi";
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(s);
        Properties p = (Properties) pe.getValue();
        assertThat(p.entrySet().size()).as("contains two entries").isEqualTo(2);
        assertThat(p.get("foo").equals("bar with whitespace")).as("foo=bar with whitespace").isTrue();
        assertThat(p.get("me").equals("mi")).as("me=mi").isTrue();
    }

    @Test
    public void handlesEqualsInValue() {
        String s = "foo=bar\n" + "me=mi\n" + "x=y=z";
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(s);
        Properties p = (Properties) pe.getValue();
        assertThat(p.entrySet().size()).as("contains two entries").isEqualTo(3);
        assertThat(p.get("foo").equals("bar")).as("foo=bar").isTrue();
        assertThat(p.get("me").equals("mi")).as("me=mi").isTrue();
        assertThat(p.get("x").equals("y=z")).as("x='y=z'").isTrue();
    }

    @Test
    public void handlesEmptyProperty() {
        String s = "foo=bar\nme=mi\nx=";
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(s);
        Properties p = (Properties) pe.getValue();
        assertThat(p.entrySet().size()).as("contains two entries").isEqualTo(3);
        assertThat(p.get("foo").equals("bar")).as("foo=bar").isTrue();
        assertThat(p.get("me").equals("mi")).as("me=mi").isTrue();
        assertThat(p.get("x").equals("")).as("x='y=z'").isTrue();
    }

    @Test
    public void handlesEmptyPropertyWithoutEquals() {
        String s = "foo\nme=mi\nx=x";
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(s);
        Properties p = (Properties) pe.getValue();
        assertThat(p.entrySet().size()).as("contains three entries").isEqualTo(3);
        assertThat(p.get("foo").equals("")).as("foo is empty").isTrue();
        assertThat(p.get("me").equals("mi")).as("me=mi").isTrue();
    }

    /**
     * 注释以 # 符号开始
     */
    @Test
    public void ignoresCommentLinesAndEmptyLines() {
        String s = "#Ignore this comment\n" + "foo=bar\n" + "#Another=comment more junk /\n" + "me=mi\n" + "x=x\n" + "\n";
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(s);
        Properties p = (Properties) pe.getValue();
        assertThat(p.entrySet().size()).as("contains three entries").isEqualTo(3);
        assertThat(p.get("foo").equals("bar")).as("foo is bar").isTrue();
        assertThat(p.get("me").equals("mi")).as("me=mi").isTrue();
    }

    /**
     * 通常我们通过缩进（使用制表符或空格）来实现对齐。
     * 如果在行的开头，这些缩进应该被忽略。
     * 我们必须确保以空白字符开头的注释行仍然被忽略：标准语法在 JDK 1.3 上不允许这样做。
     */
    @Test
    public void ignoresLeadingSpacesAndTabs() {
        String s = "    #Ignore this comment\n" + "\t\tfoo=bar\n" + "\t#Another comment more junk \n" + " me=mi\n" + "x=x\n" + "\n";
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(s);
        Properties p = (Properties) pe.getValue();
        assertThat(p.size()).as("contains 3 entries, not " + p.size()).isEqualTo(3);
        assertThat(p.get("foo").equals("bar")).as("foo is bar").isTrue();
        assertThat(p.get("me").equals("mi")).as("me=mi").isTrue();
    }

    @Test
    public void nullValue() {
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText(null);
        Properties p = (Properties) pe.getValue();
        assertThat(p).isEmpty();
    }

    @Test
    public void emptyString() {
        PropertiesEditor pe = new PropertiesEditor();
        pe.setAsText("");
        Properties p = (Properties) pe.getValue();
        assertThat(p.isEmpty()).as("empty string means empty properties").isTrue();
    }

    @Test
    public void usingMapAsValueSource() {
        Map<String, String> map = new HashMap<>();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");
        PropertiesEditor pe = new PropertiesEditor();
        pe.setValue(map);
        Object value = pe.getValue();
        assertThat(value).isNotNull();
        assertThat(value instanceof Properties).isTrue();
        Properties props = (Properties) value;
        assertThat(props).hasSize(3);
        assertThat(props.getProperty("one")).isEqualTo("1");
        assertThat(props.getProperty("two")).isEqualTo("2");
        assertThat(props.getProperty("three")).isEqualTo("3");
    }
}
