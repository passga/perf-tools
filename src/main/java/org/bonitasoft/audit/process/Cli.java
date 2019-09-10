package org.bonitasoft.audit.process;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cli {
    private static final Logger log = LoggerFactory.getLogger(Cli.class.getName());
    private String[] args = null;

    private Options options = new Options();


    public Cli(String[] args) {

        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("v", "var", true, "Here you can set parameter .");

    }


    public void parse() {
        CommandLineParser parser = new BasicParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("url"))

            if (cmd.hasOption("v")) {
                log.info("Using cli argument -v= {}", cmd.getOptionValue("v"));
                // Whatever you want to do with the setting goes here
            } else {
                log.error("MIssing v option");

            }

        } catch (ParseException e) {
            log.error("Failed to parse comand line properties", e);
        }
    }
}
