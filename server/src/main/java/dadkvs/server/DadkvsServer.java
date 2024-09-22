package dadkvs.server;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import dadkvs.DadkvsMain;
import dadkvs.DadkvsMainServiceGrpc;

public class DadkvsServer {

	static DadkvsServerState server_state;

	/** Server host port. */
	private static int port;

	static ManagedChannel[] channels;
	static DadkvsMainServiceGrpc.DadkvsMainServiceStub[] async_stubs;
	
	public static void main(String[] args) throws Exception {
		final int kvsize = 1000;

		System.out.println(DadkvsServer.class.getSimpleName());

		// Print received arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s baseport replica-id%n", Server.class.getName());
			return;
		}

		int base_port = Integer.valueOf(args[0]);
		int my_id = Integer.valueOf(args[1]);

		server_state = new DadkvsServerState(kvsize, base_port, my_id);

		port = base_port + my_id;
		
		if(my_id == 0){
			initServerStubs();
			server_state.i_am_leader = true;
		}

		final BindableService service_impl = new DadkvsMainServiceImpl(server_state, async_stubs);
		final BindableService console_impl = new DadkvsConsoleServiceImpl(server_state);
		final BindableService paxos_impl = new DadkvsPaxosServiceImpl(server_state);

		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(port).addService(service_impl).addService(console_impl)
				.addService(paxos_impl).build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
	}

	private static void initServerStubs() {
		// Let us use plaintext communication because we do not have certificates
		channels = new ManagedChannel[4];

		for (int i = 0; i < 4; i++) {
			int aux = 8081 + i;
			channels[i] = ManagedChannelBuilder.forTarget("localhost:" + aux).usePlaintext().build();
		}

		async_stubs = new DadkvsMainServiceGrpc.DadkvsMainServiceStub[4];

		for (int i = 0; i < 4; i++) {
			async_stubs[i] = DadkvsMainServiceGrpc.newStub(channels[i]);
		}
	}

	private static void terminateServerStubs() {
		for (int i = 0; i < 4; i++) {
			channels[i].shutdownNow();
		}
	}

}


