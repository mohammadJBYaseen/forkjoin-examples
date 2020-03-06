import java.io.*;

public class TryWithResourceExample {

    public static void main(String [] argc) throws IOException {
        openFile();
    }

    private static void openFile() throws IOException {
        try(InputStream in = new MyFileInputStream(TryWithResourceExample.class.getClassLoader().getResource("test.txt").getFile())){
            int read = in.read();
        }
    }

    public static class MyFileInputStream extends FileInputStream{

        public MyFileInputStream(String name) throws FileNotFoundException {
            super(name);
        }

        public MyFileInputStream(File file) throws FileNotFoundException {
            super(file);
        }

        public MyFileInputStream(FileDescriptor fdObj) {
            super(fdObj);
        }

        @Override
        public void close() throws IOException {
            System.out.println("closed even with exception");
            super.close();
        }
    }
}
