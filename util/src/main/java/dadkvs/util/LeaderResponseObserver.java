package dadkvs.util;

import dadkvs.DadkvsMain;
import io.grpc.Context.CancellableContext;
import io.grpc.stub.StreamObserver;
import com.google.protobuf.Empty;
import dadkvs.DadkvsMain.LeaderResponse;



// This class will handle responses from other replicas when the server is the leader
public class LeaderResponseObserver implements StreamObserver<LeaderResponse> {

    private int replicaId;

    // Constructor to pass in the replica ID (for debugging purposes)
    public LeaderResponseObserver(int replicaId) {
        this.replicaId = replicaId;
    }

    @Override
    public void onNext(LeaderResponse r) {
        // Handle the response from the replica (e.g., log it)
        System.out.println("On NEXT");
    }

    @Override
    public void onError(Throwable t) {
        // Handle error if the request fails
        System.err.println("Failed to send LeaderRequest to replica " + replicaId + ": " + t.getMessage());
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {
        // Optionally handle the completion of the communication
        System.out.println("Successfully completed communication with replica " + replicaId);
    }
}
