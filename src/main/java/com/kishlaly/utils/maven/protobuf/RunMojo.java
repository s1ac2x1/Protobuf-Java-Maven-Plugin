package com.kishlaly.utils.maven.protobuf;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vladimir Kishlaly
 * @since 09.11.2017
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractMojo {

    private static final String DEFAULT_LOCATION = "/src/main/protobuf/";

    private MavenProject project;
    private BuildContext buildContext;

    @Parameter(property = "folders", defaultValue = "")
    private String[] folders;

    @Parameter(property = "compiler", defaultValue = "/usr/local/bin/protoc")
    private String compiler;

    @Parameter(property = "output", defaultValue = "generated-sources")
    private String output;

    @Parameter(property = "clean", defaultValue = "true")
    private String clean;

    @Parameter(property = "failFast", defaultValue = "true")
    private String failFast;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputFolder = new File(project.getBuild().getDirectory() + File.separator + output + File.separator);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        if ("true".equals(clean)) {
            try {
                FileUtils.cleanDirectory(outputFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String folderStr : folders) {
            File folder = new File(folderStr);
            Collection files = FileUtils.listFiles(folder, new WildcardFileFilter("*.proto"), DirectoryFileFilter.DIRECTORY);
            getLog().info("Begin processing " + files.size() + " files from " + folderStr);
            Iterator<File> iterator = files.iterator();
            while (iterator.hasNext()) {
                File next = iterator.next();
                processFile(next, outputFolder);
            }
        }

        project.addCompileSourceRoot(outputFolder.getAbsolutePath());
        buildContext.refresh(outputFolder);
    }

    private void processFile(File file, File outputDir) throws MojoExecutionException {
        getLog().info("    Processing " + file.getName());
        Runtime runtime = Runtime.getRuntime();
        List<String> command = new LinkedList<>();
        command.add(compiler);
        command.add("-I" + file.getParentFile().getAbsolutePath());
        command.add("--java_out=" + outputDir);
        command.add(file.toString());
        try {
            Process process = runtime.exec(command.toArray(new String[0]));
            if (process.waitFor() != 0) {
                if ("true".equals(failFast)) {
                    getLog().error("Failed to process " + file.getName());
                    throw new MojoExecutionException("Exit code " + process.exitValue());
                } else {
                    getLog().error("Failed to process " + file.getName());
                }

            }
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Interrupted", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to execute protoc for " + file, e);
        }
    }

}
