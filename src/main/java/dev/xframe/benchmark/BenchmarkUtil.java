package dev.xframe.benchmark;

import static dev.xframe.benchmark.BenchmarkAnnotations.makeComplete;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.openjdk.jmh.generators.core.BenchmarkGenerator;
import org.openjdk.jmh.generators.core.FileSystemDestination;
import org.openjdk.jmh.generators.reflection.RFGeneratorSource;
import org.openjdk.jmh.runner.NoBenchmarksException;
import org.openjdk.jmh.runner.ProfilersFailedException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;

public class BenchmarkUtil {
    
    private static final PrivateSecurityManager psm = new PrivateSecurityManager();
    private static class PrivateSecurityManager extends SecurityManager {
        public Class<?> getCallerClass() {
            Class<?>[] classes = getClassContext();
            int index = 2;
            int len = classes.length;
            return len > index ? classes[index] : classes[len - 1];
        }
    }
    
	/**
	 * 只能从Benchmark测试类调用
	 */
    public static void process() {
	    process(psm.getCallerClass());
	}

    public static void process(Class<?> c) {
        makeComplete(c);
        process0(c);
    }
	
    private static void process0(Class<?> c) {
		try {
			File outputFolder = new File(c.getClassLoader().getResource(".").getPath());
			RFGeneratorSource source = new RFGeneratorSource();
			FileSystemDestination destination = new FileSystemDestination(outputFolder, outputFolder);
			source.processClasses(c);
			//generate benchmarkList file and jmh java files
			generate(source, destination);
			//compile jmh java files
			compile(outputFolder);
			//start jmh runner
			start();
		} catch (IOException | RunnerException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void generate(RFGeneratorSource source, FileSystemDestination destination) throws IOException {
		BenchmarkGenerator generator = new BenchmarkGenerator();
		generator.generate(source, destination);
		generator.complete(source, destination);
	}
	
	private static void start() throws RunnerException, IOException {
		Runner runner = null;
		try {
			(runner = new Runner(new CommandLineOptions())).run();
		} catch (CommandLineOptionException e) {
			System.err.println("Error parsing command line:");
			System.err.println(" " + e.getMessage());
			System.exit(1);
		} catch (NoBenchmarksException e) {
			System.err.println("No matching benchmarks. Miss-spelled regexp?");
			if(runner != null) runner.list();
			System.exit(1);
		} catch (ProfilersFailedException e) {
			// This is not exactly an error, set non-zero exit code
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (RunnerException e) {
			System.err.print("ERROR: ");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
	
	private static void compile(File sourceFolder) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, Charset.defaultCharset());
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(sourceFolder));
		compiler
			.getTask(null, null, null, null, null, fileManager.getJavaFileObjects(getJavaFiles(sourceFolder)))
			.call();
		fileManager.close();
	}

	private static File[] getJavaFiles(File sourceFolder) {
		List<File> c = new ArrayList<>();
		listFiles(c, sourceFolder);
		return c.toArray(new File[0]);
	}

	private static void listFiles(List<File> collection, File file) {
		if(file.isFile()) {
			if(file.getName().endsWith(".java")) collection.add(file);
		} else {
			for (File lf : file.listFiles()) listFiles(collection, lf);
		}
	}

}
