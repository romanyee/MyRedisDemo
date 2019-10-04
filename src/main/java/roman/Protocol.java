package roman;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
/*协议解析*/
public class Protocol {
    public static Object read(InputStream is) throws IOException {
        return process(is);
    }

    public static Command readCommand(InputStream is) throws Exception {//通过此类取得命令后的字符串hello 1
        /*
        * 假如在客户端输入的是lpush hello 1，通过Redis的协议将其变为*3\r\n$5\r\nlpush\r\n$5\r\nhello\r\n$1\r\n1\r\n
        * 给服务器的传入的参数是数组List格式.然后通过read(is)将其解析为lpush hello 1，返回Object类型。
        * 将第一个元素取出并删掉。将它变成大写字母，方便匹配类名。
        * 通过命令名LPUSH反射获取类的class对象。判断class对象表示的类是否和Command的子类相同。
        * 是，则返回类的实例化对象。
        * */
        Object o  = read(is);//解析协议。返回lpush hello 1



        // 作为 Server 来说，一定不会收到 "+OK\r\n"
        if (!(o instanceof List)) //它不是数组执行
        {
            throw new Exception("命令必须是 Array 类型");
        }

        List<Object> list = (List<Object>)o;

        //把第一个元素取出来并且把它删掉
        if (list.size() < 1) {
            throw new Exception("命令元素个数必须大于 1");
        }
        //object o2 =list.get(0);
        Object o2 = list.remove(0);
        if (!(o2 instanceof byte[])) {
            throw new Exception("错误的命令类型");
        }

        byte[] array = (byte[])o2;
        String commandName = new String(array);
        String className = String.format("roman.commands.%sCommand", commandName.toUpperCase());
        Class<?> cls = Class.forName(className);//反射---获取类的class对象
        if (!Command.class.isAssignableFrom(cls)) {
            throw new Exception("错误的命令");
        }
        Command command = (Command)cls.newInstance();
        command.setArgs(list);

        return command;
        /*
        1.Class.isAssignableFrom(Class cls)
        判定此 Class 对象所表示的类或接口与指定的 Class 参数cls所表示的类或接口是否相同，
        或是否是其超类或(超)接口，如果是则返回 true，否则返回 false。
        2.instanceof   是用来判断一个对象实例是否是一个类或接口或其子类子接口的实例。
        格式是：   oo   instanceof   TypeName
                     interImpl instanceof inter
        第一个参数是对象实例名，第二个参数是具体的类名或接口名，例如   String，InputStream。
        * */
    }

    private static String processSimpleString(InputStream is) throws IOException {
        return readLine(is);
    }

    private static String processError(InputStream is) throws IOException {
        return readLine(is);
    }

    private static long processInteger(InputStream is) throws IOException {
        return readInteger(is);
    }

    private static byte[] processBulkString(InputStream is) throws IOException {
        int len = (int)readInteger(is);
        if (len == -1) {
            // "$-1\r\n"    ==> null
            return null;
        }

        byte[] r = new byte[len];
        is.read(r, 0, len);
        /*
        for (int i = 0; i < len; i++) {
            int b = is.read();
            r[i] = (byte)b;
        }
        */

        // "$5\r\nhello\r\n";
        is.read();
        is.read();

        return r;
    }

    private static List<Object> processArray(InputStream is) throws IOException {
        int len = (int)readInteger(is);
        if (len == -1) {
            // "*-1\r\n"        ==> null
            return null;
        }

        List<Object> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            try {
                list.add(process(is));
            } catch (RemoteException e) {
                list.add(e);
            }
        }

        return list;
    }
    private static Object process(InputStream is) throws IOException {
        int b = is.read();
        if (b == -1) {
            throw new RuntimeException("不应该读到结尾的");
        }

        switch (b) {
            case '+':
                return processSimpleString(is);
            case '-':
                throw new RemoteException(processError(is));
            case ':':
                return processInteger(is);
            case '$':
                return processBulkString(is);
            case '*':
                return processArray(is);
            default:
                throw new RuntimeException("不识别的类型");
        }
    }

    private static String readLine(InputStream is) throws IOException {
        boolean needRead = true;
        StringBuilder sb = new StringBuilder();
        int b = -1;
        while (true) {
            if (needRead) {
                b = is.read();
                if (b == -1) {
                    throw new RuntimeException("不应该读到结尾的");
                }
            } else {
                needRead = true;
            }

            if (b == '\r') {
                int c = is.read();
                if (c == -1) {
                    throw new RuntimeException("不应该读到结尾的");
                }

                if (c == '\n') {
                    break;
                }

                if (c == '\r') {
                    sb.append((char) b);
                    b = c;
                    needRead = false;
                } else {
                    sb.append((char) b);
                    sb.append((char) c);
                }
            } else {
                sb.append((char)b);
            }
        }
        return sb.toString();
    }

    public static long readInteger(InputStream is) throws IOException {
        boolean isNegative = false;
        StringBuilder sb = new StringBuilder();
        int b = is.read();
        if (b == -1) {
            throw new RuntimeException("不应该读到结尾");
        }

        if (b == '-') {
            isNegative = true;
        } else {
            sb.append((char)b);
        }

        while (true) {
            b = is.read();
            if (b == -1) {
                throw new RuntimeException("不应该读到结尾的");
            }

            if (b == '\r') {
                int c = is.read();
                if (c == -1) {
                    throw new RuntimeException("不应该读到结尾的");
                }

                if (c == '\n') {
                    break;
                }

                throw new RuntimeException("没有读到\\r\\n");
            } else {
                sb.append((char)b);
            }
        }

        long v = Long.parseLong(sb.toString());
        if (isNegative) {
            v = -v;
        }

        return v;
    }




    public static void writeError(OutputStream os, String message) throws IOException {
        os.write('-');
        os.write(message.getBytes("GBK" ));
        os.write("\r\n".getBytes("GBK"));
    }



    //将输出流变成协议解析中能够解析的格式：带有标记字节的一系列字符串。然后经过协议解析，输出返回值。如(integer)1等。
    public static void writeInteger(OutputStream os, long v) throws IOException {
        // v = 10  -》
        //:10\r\n

        // v = -1  -》
        //:-1\r\n

        os.write(':');
        os.write(String.valueOf(v).getBytes());
        os.write("\r\n".getBytes());
    }

    public static void writeArray(OutputStream os, List<?> list) throws Exception {
        os.write('*');
        os.write(String.valueOf(list.size()).getBytes());
        os.write("\r\n".getBytes());
        for (Object o : list) {
            if (o instanceof String) {
                writeBulkString(os, (String)o);
            } else if (o instanceof Integer) {
                writeInteger(os, (Integer)o);
            } else if (o instanceof Long) {
                writeInteger(os, (Long)o);
            } else {
                throw new Exception("错误的类型");
            }
        }
    }

    public static void writeBulkString(OutputStream os, String s) throws IOException {
        byte[] buf = s.getBytes();
        os.write('$');
        os.write(String.valueOf(buf.length).getBytes());
        os.write("\r\n".getBytes());
        os.write(buf);
        os.write("\r\n".getBytes());
    }

    public static void writeNull(OutputStream os) throws IOException {
        os.write('$');
        os.write('-');
        os.write('1');
        os.write('\r');
        os.write('\n');
    }
}
