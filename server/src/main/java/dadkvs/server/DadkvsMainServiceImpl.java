package dadkvs.server;

import dadkvs.DadkvsMain;
import dadkvs.DadkvsMainServiceGrpc;
import dadkvs.DadkvsPaxosServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.grpc.Context;

import java.util.ArrayList;
import java.util.Iterator;


public class DadkvsMainServiceImpl extends DadkvsMainServiceGrpc.DadkvsMainServiceImplBase {

	DadkvsServerState server_state;
	DadkvsPaxosServiceImpl paxosService;

	public DadkvsMainServiceImpl(DadkvsServerState state, DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[] async_paxos_stubs) {
		this.server_state = state;
		this.paxosService = new DadkvsPaxosServiceImpl(state, async_paxos_stubs);
	}

	@Override
	public void read(DadkvsMain.ReadRequest request, StreamObserver<DadkvsMain.ReadReply> responseObserver) {

		this.server_state.checkAndRunSlowMode();

		System.out.println("Receiving read request:" + request);

		GenericRequest genericRequest = new GenericRequest(request, responseObserver);

		if(this.server_state.isFrozen()){

			server_state.addFrozenRequest(genericRequest);

		}else{
			if(!server_state.getFrozenRequests().isEmpty()) processFrozenRequests();
			server_state.addRequest(genericRequest);

			int paxosInstanceId = server_state.getNextPaxosInstance();
			int value = serializeRequest(genericRequest,request.getReqid());
	
			runPaxos(paxosInstanceId, value);
		}
	}

	@Override
	public void committx(DadkvsMain.CommitRequest request, StreamObserver<DadkvsMain.CommitReply> responseObserver) {

		this.server_state.checkAndRunSlowMode();

		System.out.println("Receiving commit request:" + request);

		GenericRequest genericRequest = new GenericRequest(request, responseObserver);

		if(this.server_state.isFrozen()){

			server_state.addFrozenRequest(genericRequest);

		}else{
			if(!server_state.getFrozenRequests().isEmpty()) processFrozenRequests();
			server_state.addRequest(genericRequest);

			int paxosInstanceId = server_state.getNextPaxosInstance();
			int value = serializeRequest(genericRequest,request.getReqid());
	
			runPaxos(paxosInstanceId, value);
		}

	}

	private void runPaxos (int paxosInstanceId, int value) {
		if (server_state.i_am_leader) {
			Context ctx = Context.current().fork();
			ctx.run(() -> {
				paxosService.startPaxosInstance(paxosInstanceId, value);
			});
		}
	}

	private int serializeRequest(GenericRequest request, int reqid) {
		int requestType = request.getRead_request() != null ? 0 : 1; // 0 for read, 1 for commit

		// Combine request type and ID into a single integer
		// Use the first bit for request type, and the remaining 31 bits for the ID
		return (requestType << 31) | reqid;
	}

	private synchronized void processFrozenRequests(){
		ArrayList<GenericRequest> frozenRequests = new ArrayList<>(server_state.getFrozenRequests());

		Iterator<GenericRequest> iterator = frozenRequests.iterator();

		while (iterator.hasNext()) {
			GenericRequest request = iterator.next();

			server_state.addRequest(request);

			int paxosInstanceId = server_state.getNextPaxosInstance();
			int value = 0;
			if(request.getCommit_request() == null){
				value = serializeRequest(request,request.getRead_request().getReqid());
			}
			else{
				value = serializeRequest(request,request.getCommit_request().getReqid());
			}
	
			runPaxos(paxosInstanceId, value);

			iterator.remove();

		}

		server_state.setFrozenRequests(frozenRequests);
	}
}