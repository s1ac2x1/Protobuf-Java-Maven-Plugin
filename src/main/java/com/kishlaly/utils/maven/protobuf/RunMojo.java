package com.kishlaly.utils.maven.protobuf;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Vladimir Kishlaly
 * @since 09.11.2017
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractMojo {

    private static final String DEFAULT_LOCATION = "/src/main/protobuf/";

    @Component
    private MavenProject project;
    @Component
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

        Set<String> processedFiles = new HashSet<>();
        for (String folderStr : folders) {
            File folder = new File(folderStr);
            Collection files =
                FileUtils.listFiles(folder, new WildcardFileFilter("*.proto"), DirectoryFileFilter.DIRECTORY);
            getLog().info("Begin processing " + files.size() + " files from " + folderStr);
            Iterator<File> iterator = files.iterator();
            while (iterator.hasNext()) {
                File next = iterator.next();
                if (processedFiles.add(next.getName())) {
                    processFile(next, outputFolder);
                }
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
                getLog().error("Failed to process " + file.getName());
                if ("true".equals(failFast)) {
                    throw new MojoExecutionException("Exit code " + process.exitValue());
                }
            }
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Interrupted", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to execute protoc for " + file, e);
        }
    }

}
