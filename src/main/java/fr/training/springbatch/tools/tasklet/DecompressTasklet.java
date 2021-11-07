package fr.training.springbatch.tools.tasklet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * A {link Tasklet} that decompress files from an archive filename to a given
 * directory.
 */
public class DecompressTasklet implements Tasklet {

	private Resource inputResource;

	private String targetDirectory;

	private String targetFile;

	private void setParameters(final JobParameters jobParameters) {

		if (inputResource == null) {
			inputResource = new ClassPathResource(jobParameters.getString("inputResource"));
		}

		if (StringUtils.isEmpty(targetDirectory)) {
			targetDirectory = jobParameters.getString("targetDirectory");
		}

		if (StringUtils.isEmpty(targetFile)) {
			targetFile = jobParameters.getString("targetFile");
		}
	}

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		if (chunkContext != null) {
			setParameters(chunkContext.getStepContext().getStepExecution().getJobParameters());
		}

		final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputResource.getInputStream()));

		final File targetDirectoryAsFile = new File(targetDirectory);
		if (!targetDirectoryAsFile.exists()) {
			FileUtils.forceMkdir(targetDirectoryAsFile);
		}

		final File target = new File(targetDirectory, targetFile);

		BufferedOutputStream dest = null;
		while (zis.getNextEntry() != null) {
			if (!target.exists()) {
				target.createNewFile();
			}
			final FileOutputStream fos = new FileOutputStream(target);
			dest = new BufferedOutputStream(fos);
			IOUtils.copy(zis, dest);
			dest.flush();
			dest.close();
		}
		zis.close();

		if (!target.exists()) {
			throw new IllegalStateException("Could not decompress anything from the archive!");
		}

		return RepeatStatus.FINISHED;
	}

	public void setInputResource(final Resource inputResource) {
		this.inputResource = inputResource;
	}

	public void setTargetDirectory(final String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public void setTargetFile(final String targetFile) {
		this.targetFile = targetFile;
	}
}