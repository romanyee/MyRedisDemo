package roman.commands;

import roman.Command;
import roman.Database;
import roman.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class HSETCommand implements Command {
    private List<Object> args;

    @Override
    public void setArgs(List<Object> args) {
        this.args = args;
    }

    @Override
    public void run(OutputStream os) throws IOException {
        String key = new String((byte[])args.get(0));
        String field = new String((byte[])args.get(1));
        String value = new String((byte[])args.get(2));
        Map<String, String> hash =Database.getHashes(key);
        //如果存在就更新
        boolean isUpdate = hash.containsKey(field);
        hash.put(field, value);
        if (isUpdate) {
            Protocol.writeInteger(os, 0);
        } else {
            Protocol.writeInteger(os, 1);
        }
    }
}
