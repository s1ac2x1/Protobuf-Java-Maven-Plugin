package com.kishlaly.utils.maven.protobuf;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author Vladimir Kishlaly
 * @since 09.11.2017
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractMojo {


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
