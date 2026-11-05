package dev.che1s.syalsdelight.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddonResourceInstallerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void missingFileRepairDoesNotOverwriteExistingFiles() throws Exception {
        Path target = temporaryDirectory.resolve("target");
        Files.createDirectories(target);
        Files.writeString(target.resolve("keep.yml"), "本地修改", StandardCharsets.UTF_8);
        Path jar = createJar("craftengine/syals_delight/keep.yml", "包内内容",
                "craftengine/syals_delight/missing.yml", "补齐内容");

        AddonResourceInstaller.InstallResult result = AddonResourceInstaller.copyPack(
                jar,
                "craftengine/syals_delight/",
                target,
                false
        );

        assertEquals(1, result.copied());
        assertEquals("本地修改", Files.readString(
                target.resolve("keep.yml"),
                StandardCharsets.UTF_8
        ));
        assertEquals("补齐内容", Files.readString(
                target.resolve("missing.yml"),
                StandardCharsets.UTF_8
        ));
    }

    @Test
    void versionReplacementRemovesFilesMissingFromNewPack() throws Exception {
        Path target = temporaryDirectory.resolve("replace-target");
        Files.createDirectories(target.resolve("old"));
        Files.writeString(target.resolve("old/stale.yml"), "旧内容", StandardCharsets.UTF_8);
        Path jar = createJar(
                "craftengine/syals_delight/current.yml",
                "新内容"
        );

        AddonResourceInstaller.InstallResult result = AddonResourceInstaller.copyPack(
                jar,
                "craftengine/syals_delight/",
                target,
                true
        );

        assertEquals(1, result.copied());
        assertEquals(1, result.removed());
        assertTrue(Files.isRegularFile(target.resolve("current.yml")));
        assertTrue(Files.notExists(target.resolve("old/stale.yml")));
    }

    private Path createJar(String... namesAndContents) throws IOException {
        Path jar = temporaryDirectory.resolve("resources-" + System.nanoTime() + ".jar");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(jar))) {
            for (int index = 0; index < namesAndContents.length; index += 2) {
                output.putNextEntry(new ZipEntry(namesAndContents[index]));
                output.write(namesAndContents[index + 1].getBytes(StandardCharsets.UTF_8));
                output.closeEntry();
            }
        }
        return jar;
    }
}

