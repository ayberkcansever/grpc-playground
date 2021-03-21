package com.canseverayberk.samples.grpcdemo;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class GRPCDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GRPCDemoApplication.class, args);
	}

	@Component
	public class TestClient {

		@GrpcClient("localhost:19999")
		private PullServiceGrpc.PullServiceBlockingStub pullServiceBlockingStub;

		@PostConstruct
		public void init() {
			new Thread(() -> {

				while (true) {
					try {
						PullRequest pullRequest = PullRequest.newBuilder()
								.setOffset(1)
								.setFetchCount(10)
								.build();

						PullResponse pullResponse = pullServiceBlockingStub.pull(pullRequest);

						System.out.println(pullResponse);

					} finally {
						try {
							TimeUnit.SECONDS.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			}).start();
		}
	}

	@GrpcService
	public class PullServiceServer extends PullServiceGrpc.PullServiceImplBase {

		@Override
		public void pull(PullRequest request, StreamObserver<PullResponse> responseObserver) {
			PullResponse pullResponse = PullResponse.newBuilder()
					.setItem(Item.newBuilder().setGuid(UUID.randomUUID().toString()).setContent("{\"field\":\"test\"}"))
					.build();

			responseObserver.onNext(pullResponse);
			responseObserver.onCompleted();
		}
	}

}
