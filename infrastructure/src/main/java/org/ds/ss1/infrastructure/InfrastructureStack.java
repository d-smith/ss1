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

    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
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
                .environment(Map.of("SERVICE_INSTANCE","AAA"))
                .logging(
                        LogDriver.awsLogs(
                                AwsLogDriverProps.builder()
                                        .logGroup(logGroup)
                                        .streamPrefix("hi")
                                        .build()
                        )
                )
                .build();


        TaskDefinition helloTaskDef = TaskDefinition.Builder.create(this, "hello-task")
                .family("task")
                .compatibility(Compatibility.EC2_AND_FARGATE)
                .cpu("512")
                .memoryMiB("1024")
                .taskRole(taskRole)
                .build();

        helloTaskDef.addContainer("hello-container",containerDefinitionOpts);






        SecurityGroup albSG = SecurityGroup.Builder.create(this, "albSG")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        albSG.addIngressRule(Peer.anyIpv4(), Port.tcp(80));

        SecurityGroup ecsSG = SecurityGroup.Builder.create(this, "ecsSG")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        ecsSG.addIngressRule(albSG, Port.allTcp());

        ApplicationLoadBalancer alb = ApplicationLoadBalancer.Builder.create(this, "alb")
                .vpc(vpc)
                .internetFacing(true)
                .securityGroup(albSG)
                .build();

        ApplicationListener applicationListener = alb.addListener("public-listener", BaseApplicationListenerProps.builder()
                .port(80)
                .open(true)
                .build());

        FargateService fargateService = FargateService.Builder.create(this, "hs")
                .serviceName("hellosvc")
                .cluster(cluster)
                .taskDefinition(helloTaskDef)
                .desiredCount(1)
                .securityGroups(List.of(ecsSG))
                .assignPublicIp(true)
                .build();

        applicationListener.addTargets("h1", AddApplicationTargetsProps.builder()
                .port(8080)
                .targets(List.of(fargateService))
                .healthCheck(software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
                        .path("/health")
                        .protocol(Protocol.HTTP)
                        .build())
                .build());

        ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "htg")
                .port(8080)
                .targetType(TargetType.IP)
                .protocol(ApplicationProtocol.HTTP)
                .vpc(vpc)
                //.healthCheck(software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
                //        .path("/health")
                //        .port("8080")
                //.build())
                .build();

        /*targetGroup.configureHealthCheck(
                software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
                        .path("/health")
                        .protocol(Protocol.HTTP)
                        .build()
        );*/

        //applicationListener.addTargetGroups("h1", AddApplicationTargetGroupsProps.builder()
        //        .targetGroups(List.of(targetGroup))
        //        .build());



        //fargateService.attachToApplicationTargetGroup(targetGroup);
    }
}
