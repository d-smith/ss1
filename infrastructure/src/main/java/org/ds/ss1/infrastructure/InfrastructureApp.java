package org.ds.ss1.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Stage;

public class InfrastructureApp {
    public static void main(final String[] args) {

        App app = new App();

        Environment env = Environment.builder()
                .account(System.getenv("PA_ACCOUNT_NO"))
                .region(System.getenv("AWS_REGION"))
                .build();


        new InfrastructureStack(app, "msstack", StackProps.builder()
                .env(env)
                .build(), "s1","s2","s3","s4","s5");

        app.synth();
    }
}

