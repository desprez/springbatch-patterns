package fr.training.springbatch.tools.tasklet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * A {link Tasklet} that compress files from a given directory to a archive
 * filename.
 */
public class CompressTasklet implements Tasklet {

	private File archiveFilename;

	private Path baseDirectoryToCompress;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.
	 * springframework.batch.core.StepContribution,
	 * org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		if (!archiveFilename.getParentFile().exists()) {
			archiveFilename.getParentFile().mkdirs();
		}
		zipDirectory(baseDirectoryToCompress, archiveFilename);

		return RepeatStatus.FINISHED;
	}

	/**
	 *
	 * @param folder
	 * @param zipFilePath
	 * @throws IOException
	 */
	public static void zipDirectory(final Path folder, final File zipFilePath) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(zipFilePath); ZipOutputStream zos = new ZipOutputStream(fos)) {

			Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
					Files.copy(file, zos);
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
						throws IOException {
					zos.putNextEntry(new ZipEntry(folder.relativize(dir).toString() + "/"));
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	public void setArchiveFilename(final String archiveFilename) {
		this.archiveFilename = new File(archiveFilename);
	}

	public void setBaseDirectoryToCompress(final String baseDirectoryToCompress) {
		this.baseDirectoryToCompress = Paths.get(baseDirectoryToCompress);
	}
}
