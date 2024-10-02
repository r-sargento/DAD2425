package dadkvs.server;
import dadkvs.DadkvsMain;
import io.grpc.stub.StreamObserver;

public class GenericRequest {
    private boolean is_read;
    private DadkvsMain.ReadRequest read_request = null;
    private DadkvsMain.CommitRequest commit_request = null;
    private StreamObserver<DadkvsMain.ReadReply> responseObserver = null;
    private StreamObserver<DadkvsMain.CommitReply> Commit_responseObserver = null;
    private int reqid = -1;

    public GenericRequest(DadkvsMain.ReadRequest request, StreamObserver<DadkvsMain.ReadReply> responseObserver) {
        is_read = true;
        this.read_request = request;
        this.responseObserver = responseObserver;
        this.reqid = request.getReqid();
    }

    public GenericRequest(DadkvsMain.CommitRequest request, StreamObserver<DadkvsMain.CommitReply> responseObserver) {
        is_read = false;
        this.commit_request = request;
        this.Commit_responseObserver = responseObserver;
        this.reqid = request.getReqid();
    }

    public StreamObserver<DadkvsMain.CommitReply> getCommit_responseObserver() {
        return Commit_responseObserver;
    }

    public StreamObserver<DadkvsMain.ReadReply> getResponseObserver() {
        return responseObserver;
    }

    public DadkvsMain.ReadRequest getRead_request() {
        return read_request;
    }

    public DadkvsMain.CommitRequest getCommit_request() {
        return commit_request;
    }

    public int getReqid(){
        return reqid;
    }
}
