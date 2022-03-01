package org.ds.ss1.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.HealthCheck;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.Protocol;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.List;
import java.util.Map;

public class InfrastructureStack extends Stack {
    public InfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfrastructureStack(final Construct scope, final String id, final StackProps props, String... serviceNames) {
        super(scope, id, props);



        Vpc vpc = Vpc.Builder.create(this, "hello-vpc")
                .maxAzs(3)
                .build();

        Cluster cluster = Cluster.Builder.create(this, "hello-cluster")
                .vpc(vpc)
                .build();

        LogGroup logGroup = LogGroup.Builder.create(this, "ecs-service-log-group")
                .retention(RetentionDays.TWO_WEEKS)
                .build();

        Role taskRole = IamComponents.createTaskIamRole(this);
        Role executionRole = IamComponents.createTaskExecutionIamRole(this);

        HealthCheck healthCheck = HealthCheck.builder()
                .command(List.of("curl localhost:8080/health"))
                .startPeriod(Duration.seconds(10))
                .interval(Duration.seconds(5))
                .timeout(Duration.seconds(2))
                .retries(3)
                .build();




        for(String name: serviceNames) {
            ContainerDefinitionOptions containerDefinitionOpts = ContainerDefinitionOptions.builder()
                    .image(ContainerImage.fromRegistry("dasmith/hello"))
                    .healthCheck(healthCheck)
                    .portMappings(
                            List.of(PortMapping.builder()
                                    .containerPort(8080)
                                    .hostPort(8080)
                                    .build())
                    )
                    .memoryLimitMiB(512)
                    .logging(AwsLogDriver.Builder.create().streamPrefix("app-mesh-name").build())
                    .environment(Map.of("SERVICE_INSTANCE",name))
                    .logging(
                            LogDriver.awsLogs(
                                    AwsLogDriverProps.builder()
                                            .logGroup(logGroup)
                                            .streamPrefix(name + "svc")
                                            .build()
                            )
                    )
                    .build();


            ServiceComponents.instantiateService(name,this,taskRole,containerDefinitionOpts,vpc,cluster);
        }




    }
}
