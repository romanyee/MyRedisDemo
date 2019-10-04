package roman.commands;

import roman.Command;
import roman.Database;
import roman.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
/*LRANGE命令将返回存储在key列表的特定元素。
偏移量开始和停止是从0开始的索引，0是第一元素(该列表的头部)，
1是列表的下一个元素。这些偏移量也可以是表示开始在列表的末尾偏移负数。
例如，-1是该列表的最后一个元素，-2倒数第二个，等等。
 */
public class LRANGECommand implements Command {
    private List<Object> args;

    @Override
    public void setArgs(List<Object> args) {
        this.args = args;
    }

    @Override
    public void run(OutputStream os) throws IOException {
        String key = new String((byte[])args.get(0));
        int start = Integer.parseInt(new String((byte[])args.get(1)));
        int end = Integer.parseInt(new String((byte[])args.get(2)));

        List<String> list = Database.getList(key);
        if (end < 0) {
            end = list.size() + end;//传0  -1
        }
        List<String> result = list.subList(start, end + 1);
        try {
            Protocol.writeArray(os, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
