package com.novoda.gradle.release

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

public class TestBintrayUploadTask {

    @Test
    public void testBintrayUploadTask() {
        BuildResult result = runTasksOnBintrayReleasePlugin('-PbintrayUser=U', '-PbintrayKey=K', "bintrayUpload")

        assert result.tasks(SUCCESS).collect { it.path } .contains(":core:bintrayUpload")
        assert result.getOutput().contains("BUILD SUCCESSFUL")
    }

    BuildResult runTasksOnBintrayReleasePlugin(String... arguments) {
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(new File(".."))
        if (arguments) {
            runner.withArguments(arguments)
        }
        return runner.build()
    }
}
