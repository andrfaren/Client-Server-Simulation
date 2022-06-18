package server;

import com.beust.jcommander.Parameter;

public class Args {

    @Parameter(names={"-t"})
    public Type type;

    @Parameter(names={"-k"})
    public String key;

    @Parameter(names = "-v")
    public String value;

    @Parameter(names = "-in")
    public String inputFile;

}
