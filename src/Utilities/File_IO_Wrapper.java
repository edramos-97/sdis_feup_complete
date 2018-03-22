package Utilities;

import java.io.Closeable;

public class File_IO_Wrapper {
    private ProtocolMessage message;
    private Closeable file;

    public File_IO_Wrapper(ProtocolMessage message, Closeable file){
        this.message = message;
        this.file = file;
    }

    public ProtocolMessage getMessage() {
        return message;
    }

    public Closeable getFile() {
        return file;
    }
}
