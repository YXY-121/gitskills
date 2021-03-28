package TestFile;



import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileCopyDemo {
    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public  static void main(String args[]){
        FileCopyRunner noBufferStreamCopy=new FileCopyRunner() {
            @Override
            public void copyFile(String source, String target) {
                InputStream fin=null;
                OutputStream fou=null;
                try {
                    fin= new FileInputStream(source);
                    fou=new FileOutputStream(target);

                    int result;
                    while((result = fin.read())!=-1){
                       fou.write(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    close(fin);
                    close(fou);
                }
            }
        };

        FileCopyRunner BufferStreamCopy= new FileCopyRunner() {
            @Override
            public void copyFile(String source, String target) {
                InputStream fin=null;
                OutputStream fou=null;

                try {
                    fin=new BufferedInputStream(new FileInputStream(source));

                    fou=new BufferedOutputStream(new FileOutputStream(target));

                    int result;
                    byte b[]=new byte[1024];
                    while((result=fin.read(b))!=-1){
                        fou.write(b,0,result);//todo 为啥用off=0？？
                    }



                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }finally {

                }
            }
        };


        FileCopyRunner nioBufferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(String source, String target) {
                FileChannel fin=null;
                FileChannel fou=null;

                try {
                    fin=new FileInputStream(source).getChannel();
                    fou=new FileOutputStream(target).getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                        while(!(fin.read(buffer) != -1)) {
                            fou.write(buffer);
                        }
                    buffer.clear();



                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                } catch (IOException e)

            {
                e.printStackTrace();
            }finally {
                    close(fin);
                    close(fou);
                }
            }
        };
    }
}
