/*
 * <copyright> Copyright 1997-2003 BBNT Solutions, LLC under sponsorship of the
 * Defense Advanced Research Projects Agency (DARPA).
 * Copyright 2009 Swiss AviationSoftware Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the Cougaar Open Source License as published by DARPA on
 * the Cougaar Open Source Website (www.cougaar.org).
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.jcgm.examples;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import net.sf.jcgm.core.CGM;
import net.sf.jcgm.core.Command;
import net.sf.jcgm.core.ElementClass;
import net.sf.jcgm.core.ICommandListener;
import net.sf.jcgm.core.Message;

/**
 * An analyzer that output statistics on the commands that are used in a set of
 * CGM files.
 * 
 * <pre>
 * Usage: Analyzer /path/to/cgm/files [/path/for/result/files] [-v]
 * 	-v verbose output
 * </pre>
 * 
 * @version $Id$
 * @author Philippe Cadé
 * @since Dec 15, 2009
 */
public class Analyzer implements ICommandListener {

	/**
	 * Map containing as key: the command class and element code; as value:
	 * the number of occurrences of this command
	 */
	private final Map<CommandHelper, Integer> commands = new TreeMap<CommandHelper, Integer>();

	/**
	 * Map containing all commands that generated a message. Key: command class
	 * and element code, value: number of occurrences of this command.
	 */
	private final Map<CommandHelper, Integer> unsupportedCommands = new TreeMap<CommandHelper, Integer>();

	private PrintWriter messagesFile = null;

	private PrintWriter commandFile = null;

	private final File outputPath;

	/**
	 * Builds an analyzer
	 * 
	 * @param cgmFilesPath
	 *            Path to the folder where the CGM files to analyze are
	 * @param outputPath
	 *            Path where the result files should be stored or {@code null}
	 *            to not store information
	 * @param verbose
	 *            If {@code true}, output command parameters for all commands
	 */
	Analyzer(File cgmFilesPath, File outputPath, boolean verbose) {
		this.outputPath = outputPath;

		try {
			if (this.outputPath != null) {
				this.messagesFile = new PrintWriter(new BufferedWriter(
						new FileWriter(new File(this.outputPath
								.getAbsolutePath()
								+ File.separator + "messages.txt"))));

				if (verbose) {
					this.commandFile = new PrintWriter(new BufferedWriter(
							new FileWriter(new File(this.outputPath
									.getAbsolutePath()
									+ File.separator + "commands.txt"))));
				}
			}

			if (cgmFilesPath.isDirectory()) {
				File[] cgmFiles = cgmFilesPath.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".cgm")
								|| name.endsWith(".cgm.gz")
								|| name.endsWith(".cgmz");
					}
				});

				int count = cgmFiles.length;
				int i = 0;
				int progress = 0;
				long startTime = System.currentTimeMillis();

				for (File cgmFile : cgmFiles) {
					System.out.print(cgmFile.getName());
					System.out.print(", ");
					analyze(cgmFile);

					int currentProgress = i * 100 / count;
					if (currentProgress != progress) {
						progress = currentProgress;

						long elapsedTime = System.currentTimeMillis() - startTime;
						long totalTime = currentProgress != 0 ? elapsedTime * 100 / currentProgress : 0;
						long remainingTime = totalTime - elapsedTime;
						long remainingMinutes = (remainingTime / 1000) / 60;
						long remainingSeconds = (remainingTime / 1000) % 60;

						StringBuilder sb = new StringBuilder();
						sb.append('\n');
						sb.append(progress).append("% ");
						sb.append("Estimated Remaining Time ");
						sb.append(remainingMinutes).append("min ");
						sb.append(remainingSeconds).append("s");
						System.out.println(sb.toString());
					}
					i++;
				}
			}

			saveResults();

			if (this.messagesFile != null) {
				this.messagesFile.close();
			}

			if (this.commandFile != null) {
				this.commandFile.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param cgmFile
	 */
	private void analyze(File cgmFile) {
		try {
			CGM cgm = new CGM();
			cgm.addCommandListener(this);

			InputStream inputStream;
			if (cgmFile.getName().endsWith(".cgm.gz") || cgmFile.getName().endsWith(".cgmz")) {
				inputStream = new GZIPInputStream(new FileInputStream(cgmFile));
			}
			else {
				inputStream = new FileInputStream(cgmFile);
			}

			DataInputStream in = new DataInputStream(new BufferedInputStream(inputStream));
			cgm.read(in);
			in.close();

			if (this.messagesFile != null) {
				this.messagesFile.println(cgmFile.getName());

				for (Message message : cgm.getMessages()) {
					this.messagesFile.print('\t');
					this.messagesFile.println(String.valueOf(message));

					CommandHelper commandHelper = new CommandHelper(message
							.getElementClass(), message.getElementCode());
					Integer count = this.unsupportedCommands.get(commandHelper);
					if (count == null) {
						this.unsupportedCommands.put(commandHelper, Integer
								.valueOf(1));
					} else {
						this.unsupportedCommands.put(commandHelper, Integer
								.valueOf(count + 1));
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveResults() {
		if (this.outputPath == null)
			return;

		FileWriter countFile = null;
		FileWriter unsupportedCountFile = null;
		FileWriter detailWriter = null;

		try {
			// output the command count
			countFile = new FileWriter(new
					File(this.outputPath.getAbsolutePath()+File.separator+"count.csv"));

			Set<CommandHelper> helpers = this.commands.keySet();
			saveHelpers(countFile, helpers);

			// output the unsupported command count
			unsupportedCountFile = new FileWriter(new File(this.outputPath
					.getAbsolutePath()
					+ File.separator + "unsupported-count.csv"));

			helpers = this.unsupportedCommands.keySet();
			saveHelpers(unsupportedCountFile, helpers);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (countFile != null) {
					countFile.close();
				}

				if (unsupportedCountFile != null) {
					unsupportedCountFile.close();
				}

				if (detailWriter != null) {
					detailWriter.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveHelpers(FileWriter fileWriter, Set<CommandHelper> helpers)
			throws IOException {
		for (CommandHelper helper : helpers) {
			Integer count = this.commands.get(helper);
			fileWriter.write(String.valueOf(ElementClass.getElementClass(helper.elementClass)));
			fileWriter.write(",");
			fileWriter.write(String.valueOf(ElementClass.getElement(helper.elementClass, helper.elementId)));
			fileWriter.write(",");
			fileWriter.write(String.valueOf(count));
			fileWriter.write('\n');
		}
	}

	/**
	 * Prints usage information and exits 
	 */
	private static void usage() {
		System.out.println("java Analyzer path-to-cgm-files [path-for-result-files] -v");
		System.exit(1);
	}

	private class CommandHelper implements Comparable<CommandHelper> {
		final int elementClass;
		final int elementId;

		CommandHelper(int elementClass, int elementId) {
			this.elementClass = elementClass;
			this.elementId = elementId;
		}

		@Override
		public int compareTo(CommandHelper o) {
			if (this.elementClass < o.elementClass)
				return -1;

			if (this.elementClass > o.elementClass)
				return 1;

			if (this.elementId < o.elementId) 
				return -1;

			if (this.elementId > o.elementId)
				return 1;

			return 0;
		}
	}

	@Override
	public void commandProcessed(Command command) {
		CommandHelper helper = new CommandHelper(command.getElementClass(), command.getElementCode());
		Integer count = this.commands.get(helper);
		if (count == null) {
			this.commands.put(helper, Integer.valueOf(1));
		}
		else {
			this.commands.put(helper, Integer.valueOf(count+1));
		}

		if (this.commandFile != null) {
			this.commandFile.println(command.toString());
		}
	}

	/**
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		if (args.length < 1)
			usage();

		File cgmDir = new File(args[0]); 

		// the default output dir is where the CGM files are
		File outputDir = cgmDir;

		if (args.length >= 2) {
			outputDir = new File(args[1]);
		}
		else {
			outputDir = null;
		}

		boolean verbose = false;
		if (args.length >= 3 && "-v".equals(args[2])) {
			verbose = true;
		}

		long start = System.currentTimeMillis();

		new Analyzer(cgmDir, outputDir, verbose);

		long elapsed = System.currentTimeMillis() - start;
		System.out.println("\nAnalyzing took "+elapsed+" ms.");
	}

}
