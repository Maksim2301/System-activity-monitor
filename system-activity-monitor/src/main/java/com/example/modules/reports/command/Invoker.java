package com.example.modules.reports.command;

public class Invoker {
    public boolean execute(ReportCommand command) {
        return command.execute();
    }
}
