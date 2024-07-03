package test;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;
	static final int _MENU=0;
	
	public CLI(DefaultIO dio) {
		this.dio=dio;
		c=new Commands(dio); 
		commands=new ArrayList<>();
		commands.add(c.new Menu());
		commands.add(c.new UploadCSV());
		commands.add(c.new AlgorithmSettings());
		commands.add(c.new DetectAnomalies());
		commands.add(c.new DisplayResults());
		commands.add(c.new UploadAndAnalyzeResults());
		commands.add(c.new Exit());
	}

	public void start() {
		commands.get(_MENU).execute();
		String _line=dio.readText();
		int _choise=Integer.parseInt(_line);
		while (_choise>=1 && _choise<=5){
			commands.get(_choise).execute();
			_line=dio.readText();
			_choise=Integer.parseInt(_line);
			commands.get(_MENU).execute();
		}
	}
}
