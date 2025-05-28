// 翻译完成 glm-4-flash
/** 版权所有 2002-2019 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可，除非适用法律要求或书面同意，否则不得使用此文件。
* 您可以在以下链接处获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于对适销性、适用性和非侵权的保证。
* 请参阅许可证以了解管理许可权限和限制的特定语言。*/
package org.springframework.beans.factory.parsing;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Harrop
 * @author Chris Beams
 * @since 2.0
 *
 * 作者：Rob Harrop
 * 作者：Chris Beams
 * 自版本 2.0 开始使用
 */
public class ParseStateTests {

    @Test
    public void testSimple() throws Exception {
        MockEntry entry = new MockEntry();
        ParseState parseState = new ParseState();
        parseState.push(entry);
        assertThat(parseState.peek()).as("Incorrect peek value.").isEqualTo(entry);
        parseState.pop();
        assertThat(parseState.peek()).as("Should get null on peek()").isNull();
    }

    @Test
    public void testNesting() throws Exception {
        MockEntry one = new MockEntry();
        MockEntry two = new MockEntry();
        MockEntry three = new MockEntry();
        ParseState parseState = new ParseState();
        parseState.push(one);
        assertThat(parseState.peek()).isEqualTo(one);
        parseState.push(two);
        assertThat(parseState.peek()).isEqualTo(two);
        parseState.push(three);
        assertThat(parseState.peek()).isEqualTo(three);
        parseState.pop();
        assertThat(parseState.peek()).isEqualTo(two);
        parseState.pop();
        assertThat(parseState.peek()).isEqualTo(one);
    }

    @Test
    public void testSnapshot() throws Exception {
        MockEntry entry = new MockEntry();
        ParseState original = new ParseState();
        original.push(entry);
        ParseState snapshot = original.snapshot();
        original.push(new MockEntry());
        assertThat(snapshot.peek()).as("Snapshot should not have been modified.").isEqualTo(entry);
    }

    private static class MockEntry implements ParseState.Entry {
    }
}
