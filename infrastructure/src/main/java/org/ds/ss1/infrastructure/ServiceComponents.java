package org.ds.ss1.infrastructure;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.elasticloadbalancingv2.Protocol;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ServiceComponents {

    public static void instantiateService(String basename,
                                          Construct scope,
                                          Role taskRole,
                                          ContainerDefinitionOptions containerDefinitionOpts,
                                          Vpc vpc,
                                          Cluster cluster) {

        Function<String,String> makeId = (s) -> String.format("%s%s",basename,s);

        TaskDefinition helloTaskDef = TaskDefinition.Builder.create(scope, makeId.apply("hello-task"))
                .family("task")
                .compatibility(Compatibility.EC2_AND_FARGATE)
                .cpu("512")
                .memoryMiB("1024")
                .taskRole(taskRole)
                .build();

        helloTaskDef.addContainer(makeId.apply("hello-container"),containerDefinitionOpts);

        SecurityGroup albSG = SecurityGroup.Builder.create(scope, makeId.apply("albSG"))
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        albSG.addIngressRule(Peer.anyIpv4(), Port.tcp(80));

        SecurityGroup ecsSG = SecurityGroup.Builder.create(scope, makeId.apply("ecsSG"))
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        ecsSG.addIngressRule(albSG, Port.allTcp());

        ApplicationLoadBalancer alb = ApplicationLoadBalancer.Builder.create(scope, makeId.apply("alb"))
                .vpc(vpc)
                .internetFacing(true)
                .securityGroup(albSG)
                .build();

        CfnOutput.Builder.create(scope,makeId.apply("albdns"))
                .value(alb.getLoadBalancerDnsName())
                .build();

        ApplicationListener applicationListener = alb.addListener(makeId.apply("public-listener"), BaseApplicationListenerProps.builder()
                .port(80)
                .open(true)
                .build());

        FargateService fargateService = FargateService.Builder.create(scope, makeId.apply("hs"))
                .serviceName(makeId.apply("hellosvc"))
                .cluster(cluster)
                .taskDefinition(helloTaskDef)
                .desiredCount(1)
                .securityGroups(List.of(ecsSG))
                .assignPublicIp(true)
                .build();

        applicationListener.addTargets(makeId.apply("h1"), AddApplicationTargetsProps.builder()
                .port(8080)
                .targets(List.of(fargateService))
                .healthCheck(software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
                        .path("/health")
                        .protocol(Protocol.HTTP)
                        .build())
                .build());

        ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(scope, makeId.apply("htg"))
                .port(8080)
                .targetType(TargetType.IP)
                .protocol(ApplicationProtocol.HTTP)
                .vpc(vpc)
                .build();
    }
}
