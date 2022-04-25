package B;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 文件上传案例服务器端:读取客户端上传的文件,保存到服务器的硬盘,给客户端回写"上传成功"

 明确:
 数据源:客户端上传的文件
 目的地:服务器的硬盘 d:\\upload\\1.jpg

    实现步骤:
        1.创建一个服务器ServerSocket对象,和系统要指定的端口号
        2.使用ServerSocket对象中的方法accept,获取到请求的客户端Socket对象
        3.使用Socket对象中的方法getInputStream,获取到网络字节输入流InputStream对象
        4.判断d:\\upload文件夹是否存在,不存在则创建
        5.创建一个本地字节输出流FileOutputStream对象,构造方法中绑定要输出的目的地
        6.使用网络字节输入流InputStream对象中的方法read,读取客户端上传的文件
        7.使用本地字节输出流FileOutputStream对象中的方法write,把读取到的文件保存到服务器的硬盘上
        8.使用Socket对象中的方法getOutputStream,获取到网络字节输出流OutputStream对象
        9.使用网络字节输出流OutputStream对象中的方法write,给客户端回写"上传成功"
        10.释放资源(FileOutputStream,Socket,ServerSocket)
 */
public class TCPServer {

    private static final List<String> DB = new ArrayList<String>();
    private static final List<String> DD = new ArrayList<String>();
    public static void main(String[] args) throws IOException {
        // 1.创建一个服务器ServerSocket对象,和系统要指定的端口号
        ServerSocket server = new ServerSocket(65530);
        //从DB.txt文件中那数据给DB
        initDB(DB);
        try {
            initDirectoryData(DD);
            initDD(DD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String flag = "Y";
        while(true){
            //验证登录
            check(server);
            while(flag.equals("Y")){

                Socket socket = server.accept();
                DataInputStream dis = new DataInputStream(
                        socket.getInputStream());
                String key = dis.readUTF();
                dis.close();
                socket.close();



                switch (key.charAt(0)) {
                    case '1':
                        //发送元数据给客户端
                        sendDB(server);
                        break;
                    case '2':
                        //上传文件
                        takeFile(server);
                        break;
                    case '3':
                        //下载文件
                        sendFile(server);
                        break;
                    case '4':
                        //发送磁盘空间
                        sendspace(server);
                        break;
                    case '5':
                        //发送目录内容
                        try {
                            sendDirData(server);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case '0':
                        flag = "N";
                        break;
                    default:
                        break;
                }
            }


        }



        //把新的DB重新存入DB.txt
        //	saveDB(DB);

        //	socket.close();
        //	server.close();
    }

    private static void initDD(List<String> dd) throws IOException {

        FileInputStream fis = new FileInputStream("D:\\server\\names.txt");// FileInputStream
        // 从文件系统中的某个文件中获取字节
        InputStreamReader  isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
        BufferedReader  br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
        String str = null;
        while ((str = br.readLine()) != null) {
            DD.add(str);
            System.out.println(str);//del
        }

        br.close();
    }

    private static void    sendDirData(ServerSocket server)throws Exception {
        Socket socket = server.accept();
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        int len = DD.size();
        dos.writeInt(len);

        for(int i=0;i<len;i++){
            dos.writeUTF(DD.get(i));
        }
        dos.close();
        socket.close();
    }

    public static void takeFile(ServerSocket server) throws IOException {
        // 2.使用ServerSocket对象中的方法accept,获取到请求的客户端Socket对象
        /*
         * 让服务器一直处于监听状态(死循环accept方法) 有一个客户端上传文件,就保存一个文件
         */
        String flag = "Y";
        while (flag.equals("Y")) {
            Socket socket = server.accept();


            // 完成文件的上传

            try {
                // 3.使用Socket对象中的方法getInputStream,获取到网络字节输入流InputStream对象
                InputStream is = socket.getInputStream();

                DataInputStream dis = new DataInputStream(
                        socket.getInputStream());
                // 文件名(和长度)
                String fileName = dis.readUTF();
//						long fileLength = dis.readLong();

                // 4.判断d:\\upload文件夹是否存在,不存在则创建
                File file = new File("d:\\server\\upload");
                if (!file.exists()) {
                    file.mkdirs();
                }

                // 5.创建一个本地字节输出流FileOutputStream对象,构造方法中绑定要输出的目的地
                FileOutputStream fos = new FileOutputStream(file + "\\" + fileName);
                // 6.使用网络字节输入流InputStream对象中的方法read,读取客户端上传的文件

                int len = 0;
//				StringBuilder sb = new StringBuilder();
                byte[] bytes = new byte[1024];
                while ((len = is.read(bytes)) != -1) {

                    // 7.使用本地字节输出流FileOutputStream对象中的方法write,把读取到的文件保存到服务器的硬盘上
                    fos.write(bytes, 0, len);


                    // 文件元数据
                    File f = new File(file + "\\" + fileName);

                    Date dd = new Date();
                    // 格式化
                    SimpleDateFormat sim = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    String Udate = sim.format(dd);

                    String size = "" + f.length() + "bytes";

                    DB.add(fileName + "\t" + size + "\t" + Udate);

                    flag = getmess(server);
//				socket1.close();


                    //更新数据库文件
                    saveDB(DB);
                    // 10.释放资源(FileOutputStream,Socket,ServerSocket)
                    fos.close();
                    dis.close();
                    is.close();
//				socket.close();

//				System.out.println(DB);
                }
            }
            catch (IOException e) {
                System.out.println(e);
            }

        }

    }


    public static void sendFile(ServerSocket server) throws IOException {

        String flag = "Y";
        while(flag.equals("Y")){
            /*
             * 使用多线程技术,提高程序的效率 有一个客户端下载文件,就开启一个线程,完成文件的下载(取消使用)
             */
            Socket socket = server.accept();
            try{
                //读取下载的文件名
                DataInputStream dis = new DataInputStream(
                        socket.getInputStream());
                String fileName = dis.readUTF();

                File file = new File("d:\\server\\upload\\"+fileName);
                //1.创建一个本地字节输入流FileInputStream对象,构造方法中绑定要读取的数据源
                FileInputStream fis = new FileInputStream(file);
                //3.使用Socket中的方法getOutputStream,获取网络字节输出流OutputStream对象
                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);


                //4.使用本地字节输入流FileInputStream对象中的方法read,读取本地文件
                int len = 0;
                byte[] bytes = new byte[1024];
                while((len = fis.read(bytes))!=-1){
                    //5.使用网络字节输出流OutputStream对象中的方法write,把读取到的文件发送给客户端
                    os.write(bytes,0,len);
                }
    		        /*
    		            解决:上传完文件,给服务器写一个结束标记
    		            void shutdownOutput() 禁用此套接字的输出流。
    		            对于 TCP 套接字，任何以前写入的数据都将被发送，并且后跟 TCP 的正常连接终止序列。
    		         */

                socket.shutdownOutput();

                flag = getmess(server);



                //8.释放资源(FileInputStream,Socket)

                dos.close();
                os.close();
                fis.close();
                dis.close();
//   		        socket.close();

            }catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    public static void check(ServerSocket server) throws IOException{
        Socket socket = server.accept();
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        DataInputStream dis = new DataInputStream(
                socket.getInputStream());
        String uname = null;
        String pwd = null;
        int tag = 0;
        while(tag==0){
            uname = dis.readUTF();
            pwd = dis.readUTF();
            if(uname.equals("admin")&&pwd.equals("password")){
                dos.writeInt(1);
                tag = 1;
            }else{
                dos.writeInt(0);
                tag = 0;
            }
        }

        dis.close();
        dos.close();
        socket.close();
    }

    public static void sendDB(ServerSocket server) throws IOException{
        Socket socket = server.accept();
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        int len = DB.size();
        dos.writeInt(len);

        for(int i=0;i<len;i++){
            dos.writeUTF(DB.get(i));
        }
        dos.close();
        socket.close();
    }

    public static void initDirectoryData(List<String> DD) throws Exception {

        File file = new File("D://server");
        File[] files = file.listFiles();
        try {
            File file2 = new File("D://server//names.txt");
            FileWriter fw = new FileWriter(file2);
            PrintWriter pw = new PrintWriter(fw, true);
            for (int i = 0; i < files.length; i++) {
                pw.println(files[i].getName());
                System.out.println(files[i].getName());//del
            }
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveDB(List<String> DB ) throws IOException {
        FileWriter fw = new FileWriter("D:\\server\\DB\\DB.txt");
        int len = DB.size();
        for(int i=0;i<len;i++){
            fw.write(DB.get(i));
            fw.write("\r\n");
        }
        fw.close();
    }

    public static void initDB(List<String> DB ) throws IOException{
        File file = new File("D:\\server\\DB\\DB.txt");
        if(file.length()==0){
            return;
        }
        FileInputStream fis = new FileInputStream("D:\\server\\DB\\DB.txt");// FileInputStream
        // 从文件系统中的某个文件中获取字节
        InputStreamReader  isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
        BufferedReader  br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
        String str = null;
        while ((str = br.readLine()) != null) {
            DB.add(str);
            //	    System.out.println(str);
        }

        br.close();

    }

    public static void sendspace(ServerSocket server) throws IOException{
        File file = new File("D:");
        long totalSpace = file.getTotalSpace();
        long freeSpace = file.getFreeSpace();

        Socket socket = server.accept();
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeLong(totalSpace);
        dos.writeLong(freeSpace);

        dos.close();
        socket.close();
    }

    public static String getmess(ServerSocket server) throws IOException{
        Socket socket = server.accept();
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        String mess = dis.readUTF();

        dis.close();
        socket.close();
        return mess;
    }
}





