package pt.ulisboa.tecnico.cnv.solver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SolverMain {

    public static void main(final String[] args) {


        // Get user-provided flags.
        final SolverArgumentParser ap = new SolverArgumentParser(args);

        // Create solver instance from factory.
        final Solver s = SolverFactory.getInstance().makeSolver(ap);

        s.solveSudoku();
    }
}
