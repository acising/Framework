package MarpleNu.CDMDataClassifier;

import MarpleNu.CDMDataDump.AllDataDump;
import MarpleNu.CDMDataDump.DataDump;
import MarpleNu.CDMDataDump.E5DataDump;
import MarpleNu.CDMDataParser.*;
import MarpleNu.CDMDataReader.BaseDataReader;
import MarpleNu.CDMLinuxFrameworkMain.*;
import MarpleNu.FrameworkLable.FrameworkLable;
import MarpleNu.FrameworkSupportData.SupportData;
import MarpleNu.LinuxFrameworkConfig.FrameworkConfig;
import com.bbn.tc.schema.avro.cdm19.*;
import scala.Int;
import scala.util.parsing.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CDMDataClassifier implements Runnable{
    private BaseParser eventParser;
    private BaseParser subjectParser;
    private BaseParser objectParser;
    private BaseParser principalParser;
    private FrameworkLable frameworkLable;
    private BaseDataReader baseDataReader;
    private Thread dataReader;
    private SupportData supportData = new SupportData();
    private DataDump dataDump;
    private boolean stopflag = true;
    private boolean dumpflag = true;
    private boolean detectFlag;
    private long sum = 0;
    private static int DeadNumber = 10;
    private long deadSum = 0;
    public static String topic = "";
    public static String hostid;

    public static int subject_num=0;

    public CDMDataClassifier(BaseDataReader baseDataReader) {
        this.baseDataReader = baseDataReader;
        //dataReader = new Thread(baseDataReader);
        //dataReader.start()；
        eventParser = new EventParser(supportData);
        subjectParser = new SubjectParser(supportData);
        objectParser = new ObjectParser(supportData);
        principalParser = new PrincipalParser(supportData);
        frameworkLable = new FrameworkLable(supportData);
    }

    @Override
    public void run()
    {
        dumpflag = FrameworkConfig.Search("dump_data").equals("true");
        detectFlag = FrameworkConfig.Search("detector_switch") == null || FrameworkConfig.Search("detector_switch").equals("true");
        String str = FrameworkConfig.Search("dump_way");
        switch (str){
            case "cmdline":
                try {
                    dataDump = new E5DataDump();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "allInOne":
                try {
                    dataDump = new AllDataDump();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                dataDump = new DataDump();
                break;
        }
//        long startTime = System.currentTimeMillis();
//        long startTime1 = 0;
//        int fileremove = 0;
        ServerSocket server = null;
        try {
            server = new ServerSocket(9091);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (stopflag){   //private boolean stopflag = true
            TCCDMDatum tccdmDatum;
            try {
                //shishi

//                System.out.println("ac");
//                Socket client = server.accept();// accept()从连接请求队列中(如果不为空，为空等待)取出一个客户的连接请求，然后创建与客户连接的Socket对象，并将它返回。
//                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream()); //创建一个从指定 InputStream 读取的 ObjectInputStream。 从流中读取序列化流标头并进行验证
//                tccdmDatum = (TCCDMDatum) objectInputStream.readObject();//读取对象的类、类的签名以及类及其所有超类型的非瞬态和非静态字段的值。可以使用 writeObject 和 readObject 方法覆盖类的默认反序列化。
//                System.out.println(tccdmDatum.getDatum());
//                objectInputStream.close();
//                client.close();

                //offline
                tccdmDatum = baseDataReader.getData();
            } catch (Exception e){
                System.out.println(e.toString());
                continue;
            }
            if (tccdmDatum==null)
                continue;
            ++sum;
            if(sum==2)
            {
                System.out.println(tccdmDatum.getDatum());
            }

            if (sum % 100000==0){
                System.out.printf("consumer %dk messages.%d\n",sum/1000,EventParser.MapEventLeft.size());
            }
            if (detectFlag) {
                Object datum = tccdmDatum.getDatum();
                if (datum instanceof Event) {
                    /*long nowtime = System.currentTimeMillis()-startTime;
                    long nowtime1 = (((Event)tccdmDatum.getDatum()).timestampNanos - startTime1)/1000000;
                    //System.out.printf("%d,%d\n",nowtime,nowtime1);
                    if(nowtime1 > nowtime) {
                        try {
                            Thread.sleep(nowtime1-nowtime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }*/

                    // 如果datum是event类型 就建一个eventparser
                    eventParser.parse(tccdmDatum);



                } else if (datum instanceof Subject) {
                    // 如果datum是Subject类型 就建一个subjectParser
                    subjectParser.parse(tccdmDatum);
                } else if (datum instanceof FileObject ||
                        datum instanceof RegistryKeyObject ||
                        datum instanceof NetFlowObject ||
                        datum instanceof SrcSinkObject ||
                        datum instanceof IpcObject) {
                    // 如果datum是ob类型 就建一个objectParser
                    objectParser.parse(tccdmDatum);
                } else if (datum instanceof Principal) {
                    principalParser.parse(tccdmDatum);
                } else if (datum instanceof Host) {
                    Host host = (Host) tccdmDatum.getDatum();
                    hostid = SupportData.byteArrayToHexString(host.getUuid().bytes());
                }

                if (datum instanceof EndMarker) {

                    System.out.println("EndMarker");

                }
            }
            if (dumpflag){

            }

            frameworkLable.label(tccdmDatum); //该函数相当于打上初始标签，需要对初始标签进行定义和修改的时候可以跟随这个函数进去

        }
    }
    public void writetoDisk(FileNode fileNode) throws IOException {
//        String s = this.filePath + "!" ;
//        for(ProcessNode p:visitedProcess.keySet()){
//            s += p.pid + ",";
//        }
//        s += "!";
//        for(LabelForCS l:labelList.values()){
//            s += l.labelName
//        }
        File file = new File("./file.json");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
        fw.write(fileNode.getFilePath()+'\n');
        fw.close();

    }
}
