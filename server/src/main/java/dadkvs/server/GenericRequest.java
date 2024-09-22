package dadkvs.server;
import dadkvs.DadkvsMain;
import io.grpc.stub.StreamObserver;

public class GenericRequest {
    private boolean is_read;
    private DadkvsMain.ReadRequest read_request = null;
    private DadkvsMain.CommitRequest commit_request = null;
    private StreamObserver<DadkvsMain.ReadReply> responseObserver = null;

    public StreamObserver<DadkvsMain.ReadReply> getResponseObserver() {
        return responseObserver;
    }

    public void setResponseObserver(StreamObserver<DadkvsMain.ReadReply> responseObserver) {
        this.responseObserver = responseObserver;
    }

    GenericRequest(){
        is_read = false;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public DadkvsMain.ReadRequest getRead_request() {
        return read_request;
    }

    public void setRead_request(DadkvsMain.ReadRequest read_request) {
        this.read_request = read_request;
    }

    public DadkvsMain.CommitRequest getCommit_request() {
        return commit_request;
    }
    
    public void setCommit_request(DadkvsMain.CommitRequest commit_request) {
        this.commit_request = commit_request;
    }
}
