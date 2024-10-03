package dadkvs.server;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

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
    int lastExecutedIndex;
    
    private ArrayList<GenericRequest> requests;
    private ArrayList<GenericRequest> frozenRequests;

    private ConcurrentHashMap<Integer, GenericRequest> requestMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> orderMap = new ConcurrentHashMap<>();  // <Integer, Integer> ---> <Value, Index>
    private AtomicInteger currentPaxosInstance;

    private boolean isFrozen = false;
    private boolean isSlowMode = false;

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
        currentPaxosInstance = new AtomicInteger(0);
        lastExecutedIndex = -1;
        frozenRequests = new ArrayList<>();
    }

    public synchronized ArrayList<GenericRequest> getFrozenRequests() {
        return frozenRequests;
    }

    public synchronized void setFrozenRequests(ArrayList<GenericRequest> frozenRequests) {
        this.frozenRequests = frozenRequests;
    }

    public synchronized void addFrozenRequest(GenericRequest request){
        frozenRequests.add(request);
    }

    public synchronized void setFreezeLock(boolean state){
        this.isFrozen = state;
    }

    public synchronized void setSlowMode(boolean state){
        this.isSlowMode = state;
        return;
    }

    public boolean isFrozen() {
        // Check if the server is frozen
        if (isFrozen) {
            System.out.println("Server is frozen, blocking client request.");
        }
        return isFrozen;    
    }

    public boolean checkAndRunSlowMode(){
        // Apply slow mode if it's on
        if (isSlowMode) {
            try {
                Random random = new Random();
                int delay = random.nextInt(3000);  // Apply a delay between 0 to 3000 milliseconds
                System.out.println("Slow mode is on, delaying request by " + delay + " ms.");
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		return isSlowMode;
    }

    public synchronized void addRequest(GenericRequest request) {
        int serializedValue = serializeRequest(request, request.getReqid());
        requests.add(request);
        requestMap.put(serializedValue, request);

        if (orderMap.get(serializedValue) != null){ // caso em que o paxos ja acabou
            executeDecidedValue(orderMap.get(serializedValue),serializedValue);
        }
    }

    public synchronized ArrayList<GenericRequest> getRequests() {
        return new ArrayList<>(requests);
    }

    public int getNextPaxosInstance() {
        return currentPaxosInstance.getAndIncrement();
    }

    public void executeDecidedValue(int index, int value) {
        
        System.out.println("Executing decided value for instance " + index);
            
        // Find the corresponding request
        GenericRequest request = findRequestByValue(value);
        if (request != null) {
            if (request.getRead_request() != null) {
                System.out.println("Executing read request for instance " + index);
                executeReadRequest(request.getRead_request(), request.getResponseObserver());
            } else if (request.getCommit_request() != null) {
                System.out.println("Executing commit request for instance " + index);
                executeCommitRequest(request.getCommit_request(), request.getCommit_responseObserver());
            }
            lastExecutedIndex +=1;
            requests.remove(request);
        }else {
            //caso em que ainda nao recebeu o pedido do cliente
            //for debug purposes
            System.out.println("Request not found for instance " + index);
            orderMap.put(value, index);
        }

    }

    private synchronized GenericRequest findRequestByValue(int value) {
        return requestMap.remove(value);
    }

    private synchronized void executeReadRequest(DadkvsMain.ReadRequest request, StreamObserver<DadkvsMain.ReadReply> responseObserver) {
        int key = request.getKey();
        VersionedValue vv = store.read(key);

        System.out.println("Read request for key " + key + " returned value " + vv.getValue() + " with version " + vv.getVersion());

        DadkvsMain.ReadReply response = DadkvsMain.ReadReply.newBuilder()
            .setReqid(request.getReqid())
            .setValue(vv.getValue())
            .setTimestamp(vv.getVersion())
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private synchronized void executeCommitRequest(DadkvsMain.CommitRequest request, StreamObserver<DadkvsMain.CommitReply> responseObserver) {
        int reqid = request.getReqid();
        int key1 = request.getKey1();
        int version1 = request.getVersion1();
        int key2 = request.getKey2();
        int version2 = request.getVersion2();
        int writekey = request.getWritekey();
        int writeval = request.getWriteval();

        TransactionRecord txrecord = new TransactionRecord(key1, version1, key2, version2, writekey, writeval);
        boolean result = store.commit(txrecord);

        System.out.println("Commit request for keys " + key1 + " and " + key2 + " returned " + result);

        DadkvsMain.CommitReply response = DadkvsMain.CommitReply.newBuilder()
            .setReqid(reqid)
            .setAck(result)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private int serializeRequest(GenericRequest request, int reqid) {
        int requestType = request.getRead_request() != null ? 0 : 1; // 0 for read, 1 for commit

        // Combine request type and ID into a single integer
        // Use the first bit for request type, and the remaining 31 bits for the ID
        return (requestType << 31) | reqid;
    }
}