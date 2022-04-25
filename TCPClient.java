package B;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.System.err;

/*

 实现步骤:
 1.创建一个本地字节输入流FileInputStream对象,构造方法中绑定要读取的数据源
 2.创建一个客户端Socket对象,构造方法中绑定服务器的IP地址和端口号
 3.使用Socket中的方法getOutputStream,获取网络字节输出流OutputStream对象
 4.使用本地字节输入流FileInputStream对象中的方法read,读取本地文件
 5.使用网络字节输出流OutputStream对象中的方法write,把读取到的文件上传到服务器
 6.使用Socket中的方法getInputStream,获取网络字节输入流InputStream对象
 7.使用网络字节输入流InputStream对象中的方法read读取服务回写的数据
 8.释放资源(FileInputStream,Socket)
 */

public class TCPClient {

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        boolean flag = true;

        //
        // 用户登录
        login(in);

        while (flag) {
            System.out.println("**************************文件传输系统CLI****************************");
            System.out.println("0、退出系统");
            System.out.println("1、已上传文件列表");
            System.out.println("2、上传文件");
            System.out.println("3、下载文件");
            System.out.println("4、磁盘剩余空间与占用查询");
            System.out.println("5、目录列表");
            String key = in.nextLine();

            Socket socket = new Socket("127.0.0.1", 65530);
            DataOutputStream dos = new DataOutputStream(
                    socket.getOutputStream());
            dos.writeUTF(key);
            dos.close();
            socket.close();

            switch (key) {
                case "1":
                    // 从服务中获取文件的元数据
                    getDB();
                    break;
                case "2":
                    // 上传文件
                    sendFile(in);
                    break;
                case "3":
                    // 下载文件
                    receiveFile(in);
                    break;
                case "4":
                    space();
                    break;

                case "5":
                    getDB();
                    break;
                case "0":
                    flag = false;
                    break;
                default:
                    System.out.println("输入非法，请输入0-4");
                    break;
            }

        }

        // socket.close();
        in.close();
    }


    public static void sendFile(Scanner in) throws IOException {
        String fileName = null;
        String flag = "Y";
        FileInputStream fis = null;
        Socket socket = null;
        while (flag.equals("Y")) {
            System.out.println("请输入要上传的文件名：");
            fileName = in.nextLine();
            File file = new File("d:\\client\\" + fileName);
            // 1.创建一个本地字节输入流FileInputStream对象,构造方法中绑定要读取的数据源
            fis = new FileInputStream(file);
            // 2.创建一个客户端Socket对象,构造方法中绑定服务器的IP地址和端口号
            socket = new Socket("127.0.0.1", 65530);
            // 3.使用Socket中的方法getOutputStream,获取网络字节输出流OutputStream对象
            OutputStream os = socket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            // 文件名、大小等属性
            dos.writeUTF(file.getName());
            dos.flush();
            // dos.writeLong(file.length());

            // 4.使用本地字节输入流FileInputStream对象中的方法read,读取本地文件
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fis.read(bytes)) != -1) {
                // 5.使用网络字节输出流OutputStream对象中的方法write,把读取到的文件上传到服务器
                os.write(bytes, 0, len);
            }

            /*
             * 解决:上传完文件,给服务器写一个结束标记 void shutdownOutput() 禁用此套接字的输出流。 对于 TCP
             * 套接字，任何以前写入的数据都将被发送，并且后跟 TCP 的正常连接终止序列。
             */

            socket.shutdownOutput();



//		}
            System.out.println("上传成功");
            System.out.println("是否继续上传：Y/N");
            flag = in.nextLine();
            sendmess(flag);


            // 8.释放资源(FileInputStream,Socket)
//			is.close();
            dos.close();
            os.close();
            socket.close();
            fis.close();
        }

    }

    public static void receiveFile(Scanner in) throws IOException {
        String fileName = null;
        String flag = "Y";
        FileOutputStream fos = null;
        Socket socket = null;
        InputStream is = null;
        DataOutputStream dos = null;

        while (flag.equals("Y")) {
            System.out.println("请输入要下载的文件名：");
            fileName = in.nextLine();

            File dir = new File("d:\\client\\download");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir + "\\" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            // 1.创建一个本地字节输入流FileInputStream对象,构造方法中绑定要读取的数据源
            fos = new FileOutputStream(file);
            // 2.创建一个客户端Socket对象,构造方法中绑定服务器的IP地址和端口号
            socket = new Socket("127.0.0.1", 65530);
            // 3.使用Socket中的方法getInputStream,获取网络字节输入流getInputStream对象
            is = socket.getInputStream();

            // 给服务器发送要下载的文件名
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(fileName);
            dos.flush();

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = is.read(bytes)) != -1) {
                // 7.使用本地字节输入流FileIntputStream对象中的方法read,把读取到的文件保存到本地的硬盘上
                fos.write(bytes, 0, len);

            }

            System.out.println("下载成功！");
            System.out.println("是否继续下载：Y/N");
            flag = in.nextLine();
            sendmess(flag);

            // 8.释放资源(FileInputStream,Socket)
            dos.close();
            is.close();
            socket.close();
            fos.close();
        }
    }

    public static void login(Scanner in) throws IOException {
        int tag = 0;
        Socket socket=null;
        try {
            socket = new Socket("127.0.0.1", 65530);
            DataOutputStream dos = null;
            DataInputStream dis = null;
            while (tag == 0) {
                //System.out.println("请输入用户名：");

                String uname = "admin";

                // System.out.println("请输入密码：");
                String pwd = "password";

                dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF(uname);
                dos.flush();

                dos.writeUTF(pwd);
                dos.flush();

                dis = new DataInputStream(socket.getInputStream());
                tag = dis.readInt();
                if (tag == 1) {
                    System.out.println("于端口: "+socket.getPort()+" C-S连接成功");
                } else {
                    err.println("连接未成功");
                }
            }
            dis.close();
            dos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        socket.close();
    }

    public static void getDB() throws IOException {
        Socket socket = new Socket("127.0.0.1", 65530);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int len = dis.readInt();

        for (int i = 0; i < len; i++) {
            System.out.println(dis.readUTF());
        }

        dis.close();
        socket.close();
    }

    public static void space() throws IOException {
        File file = new File("D:");
        long totalSpace = file.getTotalSpace();
        long freeSpace = file.getFreeSpace();
        System.out.println("本地：");
        System.out.println("总空间大小 : " + totalSpace / 1024 / 1024 / 1024 + "G");
        System.out.println("剩余空间大小 : " + freeSpace / 1024 / 1024 / 1024 + "G\n");

        Socket socket = new Socket("127.0.0.1", 65530);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        long StotalSpace = dis.readLong();
        long SfreeSpace =  dis.readLong();
        System.out.println("服务器：");
        System.out.println("总空间大小 : " + StotalSpace / 1024 / 1024 / 1024 + "G");
        System.out.println("剩余空间大小 : " + SfreeSpace / 1024 / 1024 / 1024 + "G\n");
        dis.close();
        socket.close();

    }

    public static void sendmess(String str) throws IOException{

        Socket socket = new Socket("127.0.0.1", 65530);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        dos.writeUTF(str);

        dos.close();
        socket.close();
    }

}

