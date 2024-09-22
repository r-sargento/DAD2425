package dadkvs.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import dadkvs.DadkvsMain;
import io.grpc.stub.StreamObserver;

public class DadkvsServerState {
    boolean i_am_leader;
    int debug_mode;
    int base_port;
    int my_id;
    int store_size;
    KeyValueStore store;
    MainLoop main_loop;
    Thread main_loop_worker;
    private ArrayList<GenericRequest> requests;
    private TreeSet<SequencedID> sequence;
    private int current_id;

    public DadkvsServerState(int kv_size, int port, int myself) {
        base_port = port;
        my_id = myself;
        i_am_leader = false;
        debug_mode = 0;
        store_size = kv_size;
        store = new KeyValueStore(kv_size);
        main_loop = new MainLoop(this);
        main_loop_worker = new Thread(main_loop);
        main_loop_worker.start();
        requests = new ArrayList<>();
        sequence = new TreeSet<>(Comparator.comparingInt(SequencedID::get_seqID));
        current_id = 0;
    }

    public void addRequest (DadkvsMain.ReadRequest read_request, StreamObserver<DadkvsMain.ReadReply> responseObserver){
        if(read_request != null){
            GenericRequest current_request = new GenericRequest();
            current_request.setRead_request(read_request);
            current_request.setResponseObserver(responseObserver);
            requests.add(current_request);
        }
    }

    public void addRequest (DadkvsMain.CommitRequest commit_request, StreamObserver<DadkvsMain.ReadReply> responseObserver){
        if(commit_request != null){
            GenericRequest current_request = new GenericRequest();
            current_request.setCommit_request(commit_request);
            current_request.setResponseObserver(responseObserver);
            requests.add(current_request);
        }
    }

    public void addSequenceID (int reqID, int seqID){
        SequencedID id = new SequencedID(reqID,seqID);
        sequence.add(id);
    }

    public ArrayList<GenericRequest> getRequests() {
        return requests;
    }

    public TreeSet<SequencedID> getSequence() {
        return sequence;
    }

    public int getCurrentID(){
        return current_id;
    }

    public void IncrementCurrentID(){
        current_id++;
    }

    


}
