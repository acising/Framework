package MarpleNu.FrameworkDataStruct.FileSpecies;

public class IPCFile {
    private int fd1;
    private int fd2;
    private int pid;
    public IPCFile(int fd1, int fd2, int pid)
    {
        this.fd1 = fd1;
        this.fd2 = fd2;
        this.pid = pid;
    }
}
