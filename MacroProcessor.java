import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

class MacroProcessor {
    public static void main(String[] args) throws IOException, MinimumValueError {
        File file = null;
        String filePath = null;
        FileWriter writer = null;
        boolean fileAvailable = true;
        PassOne passOne = null;
        PassTwo passTwo = null;

        try {

            if (args.length == 0) {
                println("\n> No Input File Specified");
                println("\n> Scanning for file name \"scan_for_macro.txt\" on path "
                        + System.getProperty("user.home") + " --> Desktop");

                filePath = System.getProperty("user.home") + "/Desktop/scan_for_macro.txt";
                passOne = new PassOne(filePath);

            } else {
                filePath = args[0];
                passOne = new PassOne(filePath);
            }

            if (args.length <= 2 && args.length > 1) {
                println("\n> Loading the specified output file");
                file = new File(args[1]);
            } else {
                if (args.length > 2) {
                    throw new NullPointerException();
                }

                println("\n> No Output File Specified");

                try {
                    println("\n> Checking for file \"macroProcessor_output.txt\" on path "
                            + System.getProperty("user.home") + " --> Desktop");
                    file = new File(System.getProperty("user.home") + "/Desktop/macroProcessor_output.txt");

                    if (!file.canWrite()) {
                        if (!(new File(System.getProperty("user.home") + "/Desktop").exists()))
                            throw new FileNotFoundException("File is not writable or unavailable");
                    }

                } catch (Exception e) {
                    file = new File(System.getProperty("user.home") + "/Macro Processor Output");

                    if (file.mkdirs() || file.canWrite()) {
                        println("> Directory Creation Successful");
                        println(
                                "> Output will be stored in Folder Macro Processor Output on path "
                                        + System.getProperty("user.home"));
                        file = new File(file.getPath() + "/macroProcessor_output.txt");
                    } else {
                        println("> Failed to create a default directory");
                        println("> Since no output file is available, the output will not be stored");
                        fileAvailable = false;
                    }
                }

            }

            long stamp = 0;
            println("\n-- Starting Analysis --\n");

            //TimeStamp the actually time taken to analyse the file
            stamp = new Date().getTime();

            passOne.readFile();
            passTwo = new PassTwo(passOne);
            passTwo.readIntermediateFile();

            stamp = new Date().getTime() - stamp;

            //Write to available output file
            if (fileAvailable) {

                //Create output file if not available
                if (file.createNewFile()) {
                    println("Creating a new output file:\n> " + file.getPath() + "\n");
                } else {
                    println("Writing to the available output file:\n> " + file.getPath() + "\n");
                }
                writer = new FileWriter(file);

                writer.write("Results for file\n--> " + passOne.getFile().getName() + "\n\n");
                writer.write("Target File Path\n--> " + passOne.getFile().getPath() + "\n\n");
                writer.write("Pass One Output:\n");
                writer.write(passOne.getIntermediateCodeBox() + "\n");
                writer.write(passOne.getMNTBox() + "\n");
                writer.write(passOne.getMDTBox() + "\n");
                writer.write(passOne.getALABox() + "\n\n");

                writer.write("Pass Two Output:\n");
                writer.write(passTwo.getExpandedBox() + "\n");
                writer.write(passTwo.getUpdatedALABox() + "\n");

                writer.close();
            }

            println("Pass One Output:");
            println(passOne.getIntermediateCodeBox());
            println(passOne.getMNTBox());
            println(passOne.getMDTBox());
            println(passOne.getALABox() + "\n");

            println("Pass Two Output:");
            println(passTwo.getExpandedBox());
            println(passTwo.getUpdatedALABox());

            println("\nFinished in: " + stamp + " ms");

        } catch (FileNotFoundException notFoundException) {
            println("File not found");
        } catch (NullPointerException e) {
            println("No file specified");
            e.printStackTrace();
        }
    }

    public static void println(String message) {
        System.out.println(message);
    }
}

// Pass One Macro Processor 
class PassOne {

    private Scanner reader = null;
    private File file = null;
    private boolean isMacro = false, inMacro = false;
    private Box intermediateCodeBox, MNTBox, MDTBox, ALABox;

    public int MDTC = 0, ALAI = 0;

    public MNT mnt = null;
    public MDT mdt = null;
    public ALA ala = null;

    public IntermediateCode intermediateCode = null;

    PassOne(String filePath) throws NullPointerException, FileNotFoundException {
        mnt = new MNT();
        mdt = new MDT();
        ala = new ALA();

        intermediateCode = new IntermediateCode();

        file = new File(filePath);
        reader = new Scanner(file);
    }

    public File getFile() {
        return file;
    }

    public void readFile() throws MinimumValueError {
        while (reader.hasNextLine()) {
            parseLine(reader.nextLine().trim());
        }

        setIntermediateCodeBox();
        setMNTBox();
        setMDTBox();
        setALABox();

        reader.close();
    }

    public String getIntermediateCodeBox() {
        return intermediateCodeBox.getBox();
    }

    private void setIntermediateCodeBox() throws MinimumValueError {
        intermediateCodeBox = new Box(new String[] { "Intermediate File" }, true);

        for (String code : intermediateCode.code) {
            intermediateCodeBox.addRow(new String[] { code });
        }
    }

    public String getMNTBox() {
        return MNTBox.getBox();
    }

    private void setMNTBox() throws MinimumValueError {
        MNTBox = new Box(new String[] { "Macro Name", "Number of Parameters", "ALA Index", "MDT Index" }, false);
        for (MNT.Entry entry : mnt.entries) {
            if (entry != null)
                MNTBox.addRow(new String[] { entry.macroName, "" + entry.noOfParams, "" + entry.ALAIndex,
                        "" + entry.MDTIndex });
            else
                MNTBox.closeRow();
        }
    }

    public String getMDTBox() {
        return MDTBox.getBox();
    }

    private void setMDTBox() throws MinimumValueError {
        MDTBox = new Box(new String[] { "MDTC", "Macro Statements" }, true);
        for (MDT.Entry entry : mdt.entries) {
            if (entry != null) {
                MDTBox.addRow(new String[] { "" + entry.MDTC, entry.macroStatement });
            }
            if (entry.macroStatement.equals("MEND")) {
                MDTBox.closeRow();
            }
        }
    }

    public String getALABox() {
        return ALABox.getBox();
    }

    private void setALABox() throws MinimumValueError {
        ALABox = new Box(new String[] { "Macro Name", "Position", "Parameter Name" }, true);
        String lastMacroName = "";

        for (int i = 0; i < ala.entries.size(); i++) {

            PassOne.ALA.Entry entry = ala.entries.get(i);

            if (i == 0) {
                ALABox.addRow(new String[] { entry.macroName, "" + entry.position, entry.parameterName });
            } else if (entry.macroName.equals(lastMacroName)) {
                ALABox.addRow(new String[] { entry.macroName, "" + entry.position, entry.parameterName });
            } else {
                ALABox.closeRow();
                ALABox.addRow(new String[] { entry.macroName, "" + entry.position, entry.parameterName });
            }

            lastMacroName = entry.macroName;
        }
    }

    public void parseLine(String line) {
        if (line.equals("MACRO") && !isMacro) {
            isMacro = true;
        } else if (isMacro && !inMacro) {
            ala.parseEntries(line);
            int index = ala.macroName.size() - 1;

            mnt.parseEntries(ala.macroName.get(index), ala.noOfParams.get(index), ALAI, MDTC);
            ALAI += ala.noOfParams.get(index) == 0 ? 1 : ala.noOfParams.get(index);

            inMacro = true;
        } else if (isMacro && inMacro) {
            mdt.parseEntry(MDTC++, line);

            if (line.equals("MEND")) {
                isMacro = inMacro = false;
            }
        } else {
            intermediateCode.parseEntry(line);
        }
    }

    class IntermediateCode {
        public List<String> code = new ArrayList<String>();

        public void parseEntry(String line) {
            code.add(line);
        }

        public void addNullEntry() {
            code.add(null);
        }
    }

    class MNT {
        public List<Entry> entries = new ArrayList<Entry>();

        public void parseEntries(String macroName, long noOfParams, long ALAIndex, long MDTIndex) {
            entries.add(new Entry(macroName, noOfParams, ALAIndex, MDTIndex));
        }

        public void addNullEntry() {
            entries.add(null);
        }

        class Entry {
            public String macroName;
            public long noOfParams;
            public long ALAIndex;
            public long MDTIndex;

            Entry(String macroName, long noOfParams, long ALAIndex, long MDTIndex) {
                this.macroName = macroName;
                this.noOfParams = noOfParams;
                this.ALAIndex = ALAIndex;
                this.MDTIndex = MDTIndex;
            }
        }
    }

    class MDT {
        public List<Entry> entries = new ArrayList<Entry>();

        public void parseEntry(long MDTC, String line) {
            entries.add(new Entry(MDTC, line));
        }

        public void addNullEntry() {
            entries.add(null);
        }

        class Entry {
            public long MDTC;
            public String macroStatement;

            Entry(long MDTC, String macroStatement) {
                this.MDTC = MDTC;
                this.macroStatement = macroStatement;
            }
        }
    }

    class ALA {
        public List<Entry> entries = new ArrayList<Entry>();
        public List<String> macroName = new ArrayList<String>();
        public List<Long> noOfParams = new ArrayList<Long>();

        public void parseEntries(String line) {
            String[] args = line.split("\\s+", 2);
            macroName.add(args[0]);

            if (args.length > 1) {
                String[] params = args[1].split(", *");

                for (int i = 0; i < params.length; i++)
                    entries.add(new Entry(args[0], params[i], i + 1));

                noOfParams.add((long) params.length);
            } else {
                entries.add(new Entry(args[0], "", 0));
                noOfParams.add(0L);
            }
        }

        public void addNullEntry() {
            entries.add(null);
        }

        class Entry {
            public String macroName;
            public String parameterName;
            public long position;

            Entry(String macroName, String parameterName, long position) {
                this.macroName = macroName;
                this.parameterName = parameterName;
                this.position = position;
            }
        }
    }
}

class PassTwo {
    private PassOne passOne = null;

    public UpdatedALA updatedALA;
    public ExpandedCode expandedCode;
    public PassOne.MNT.Entry mntEntry = null;

    private Box updatedALABox, expandedCodeBox;

    PassTwo(PassOne passOne) {
        this.passOne = passOne;
        updatedALA = new UpdatedALA();
        expandedCode = new ExpandedCode();
    }

    public void readIntermediateFile() throws MinimumValueError {
        Iterator<String> iterator = passOne.intermediateCode.code.iterator();
        while (iterator.hasNext()) {
            parseLine(iterator.next());
        }

        setUpdatedALABox();
        setIntermediateCodeBox();
    }

    public void parseLine(String line) {
        String[] args = line.split("\\s+", 2);
        if (isMacro(args[0])) {
            updatedALA.parseEntries(args, mntEntry.ALAIndex, passOne.ala);

            for (int i = (int) mntEntry.MDTIndex; i < passOne.mdt.entries.size(); i++) {
                PassOne.MDT.Entry mdtEntry = passOne.mdt.entries.get(i);
                if (mdtEntry.macroStatement.equals("MEND"))
                    break;

                String[] statement = mdtEntry.macroStatement.split("\\s+", 2);

                if (statement.length > 1) {
                    String[] statementParams = statement[1].split(", *");

                    for (int j = 0; j < statementParams.length; j++) {
                        for (UpdatedALA.Entry updatedALA : updatedALA.entries) {
                            if (mntEntry.macroName.equals(updatedALA.macroName)) {
                                if (updatedALA.parameterName.equals(statementParams[j])) {
                                    statementParams[j] = updatedALA.parameterValue;
                                }
                            }
                        }
                    }

                    String newLine = statement[0] + " ";

                    for (int j = 0; j < statementParams.length; j++) {
                        if (j + 1 == statementParams.length) {
                            newLine += statementParams[j];
                            break;
                        }
                        newLine += statementParams[j] + ",";
                    }
                    expandedCode.parseEntry(newLine);
                } else {
                    expandedCode.parseEntry(line);
                }
            }

        } else {
            expandedCode.parseEntry(line);
        }
    }

    public String getUpdatedALABox() {
        return updatedALABox.getBox();
    }

    private void setUpdatedALABox() throws MinimumValueError {
        updatedALABox = new Box(new String[] { "Macro Name", "Position", "Parameter Value", "Parameter Name" }, true);
        String lastMacroName = "";

        for (int i = 0; i < updatedALA.entries.size(); i++) {
            UpdatedALA.Entry entry = updatedALA.entries.get(i);

            if (i == 0) {
                updatedALABox.addRow(new String[] { entry.macroName, "" + entry.position, entry.parameterValue,
                        entry.parameterName });
            } else if (entry.macroName.equals(lastMacroName)) {
                updatedALABox.addRow(new String[] { entry.macroName, "" + entry.position, entry.parameterValue,
                        entry.parameterName });
            } else {
                updatedALABox.closeRow();
                updatedALABox.addRow(new String[] { entry.macroName, "" + entry.position, entry.parameterValue,
                        entry.parameterName });
            }

            lastMacroName = entry.macroName;
        }

    }

    public String getExpandedBox() {
        return expandedCodeBox.getBox();
    }

    private void setIntermediateCodeBox() throws MinimumValueError {
        expandedCodeBox = new Box(new String[] { "Expanded Code" }, true);

        for (String code : expandedCode.code) {
            expandedCodeBox.addRow(new String[] { code });
        }
    }

    public boolean isMacro(String command) {
        for (PassOne.MNT.Entry entry : passOne.mnt.entries) {
            if (entry.macroName.equals(command)) {
                mntEntry = entry;
                return true;
            }
        }
        mntEntry = null;
        return false;
    }

    class UpdatedALA {
        public List<Entry> entries = new ArrayList<Entry>();

        public void parseEntries(String[] args, long ALAIndex, PassOne.ALA ala) {
            if (args.length > 1) {
                String[] params = args[1].split(", *");

                for (int i = 0; i < params.length; i++)
                    entries.add(
                            new Entry(args[0], params[i], ala.entries.get((int) ALAIndex + i).parameterName, i + 1));

            }
        }

        public void addNullEntry() {
            entries.add(null);
        }

        class Entry {
            public String macroName;
            public String parameterValue;
            public String parameterName;
            public long position;

            Entry(String macroName, String parameterValue, String parameterName, long position) {
                this.macroName = macroName;
                this.parameterValue = parameterValue;
                this.parameterName = parameterName;
                this.position = position;
            }
        }
    }

    class ExpandedCode {
        public List<String> code = new ArrayList<String>();

        public void parseEntry(String line) {
            code.add(line);
        }

        public void addNullEntry() {
            code.add(null);
        }
    }
}

class Box {
    private String[] columnNames;
    private List<String[]> rows;
    private boolean alignToRight;

    //A Table without column labels won't be presentable
    Box(String[] columnNames) {
        this.columnNames = stringsArrayTrimmer(columnNames);
        rows = new ArrayList<String[]>();

        // Set the default alignment of the characters to the right
        alignToRight = true;
    }

    Box(String[] columnNames, boolean alignToLeft) {
        this.columnNames = stringsArrayTrimmer(columnNames);
        rows = new ArrayList<String[]>();

        // Set the alignment of the characters to the left or right based on the input value
        alignToRight = !alignToLeft;
    }

    //Add a row to the table using string array
    public Box addRow(String[] row) throws MinimumValueError {

        // If the column values in the row to be added is less than the column labels then throw an error saying so
        if (row.length < columnNames.length)
            throw new MinimumValueError("The row must have atleast " + columnNames.length + " columns");

        // Add to the rows
        rows.add(stringsArrayTrimmer(row));
        return this;
    }

    //Add a row to the table using a list of string values
    public Box addRow(List<String> row) throws MinimumValueError {

        // If the column values in the row to be added is less than the column labels then throw an error saying so
        if (row.size() < columnNames.length)
            throw new MinimumValueError("The row must have atleast " + columnNames.length + " columns");

        // Convert the items in the list to a string arrray
        String[] sRow = new String[row.size()];
        for (int i = 0; i < sRow.length; i++) {
            sRow[i] = row.get(i);
        }

        // Add to the rows
        rows.add(stringsArrayTrimmer(sRow));
        return this;
    }

    public Box closeRow() {

        // To indicate that a row must be close, adding a null value to the list
        rows.add(null);
        return this;
    }

    // Get Box as a string
    public String getBox() {

        String box = "";

        // Set prerequisites
        int[] maxLengths = getMaxLengthOfColumnElements();
        String[][] formatedTable = getFormatedTable(maxLengths);
        String border = getRowBorder(maxLengths);

        // Make labels
        box += border + "\n";
        box += getFormattedColumns(formatedTable[0]) + "\n";
        box += border + "\n";

        // Make rows
        for (int i = 1; i < formatedTable.length; i++) {
            if (rows.get(i - 1) != null)
                box += getFormattedColumns(formatedTable[i]) + "\n";
            if (rows.get(i - 1) == null || i + 1 == formatedTable.length)
                box += border + "\n";
        }

        return box;
    }

    // Prints the table
    public void printBox() {

        // Set prerequisites
        int[] maxLengths = getMaxLengthOfColumnElements();
        String[][] formatedTable = getFormatedTable(maxLengths);
        String border = getRowBorder(maxLengths);

        // Make labels
        System.out.println(border);
        System.out.println(getFormattedColumns(formatedTable[0]));
        System.out.println(border);

        // Make rows
        for (int i = 1; i < formatedTable.length; i++) {
            if (rows.get(i - 1) != null)
                System.out.println(getFormattedColumns(formatedTable[i]));
            if (rows.get(i - 1) == null || i + 1 == formatedTable.length)
                System.out.println(border);

        }
    }

    // Get the row separators 
    private String getRowBorder(int[] columnLengths) {
        String border = "";
        for (int i = 0; i < columnLengths.length; i++) {
            if (i == 0)
                border += "+";
            for (int j = 0; j < columnLengths[i] + 2; j++)
                border += "-";
            border += "+";
        }
        return border;
    }

    // Format row values by adding columns separators
    private String getFormattedColumns(String[] array) {
        String column = "";
        for (int i = 0; i < array.length; i++) {
            if (i == 0)
                column += "|";
            column += " " + array[i] + " ";
            column += "|";
        }
        return column;
    }

    // Set the maximum length of characters taken by a column for alignment
    private int[] getMaxLengthOfColumnElements() {

        // Initialize the int array for each column
        int[] maxLengths = new int[columnNames.length];

        // Set the default length of the column characters to column labels
        for (int i = 0; i < columnNames.length; i++) {
            maxLengths[i] = columnNames[i].length();
        }

        // If a string value inside a row is of greater length than the default values, replace the maximum character length of the column with the length of the string
        for (String[] strings : rows) {
            if (strings != null) {
                for (int i = 0; i < maxLengths.length; i++) {
                    if (maxLengths[i] < strings[i].length())
                        maxLengths[i] = strings[i].length();
                }
            }
        }
        return maxLengths;
    }

    // Set a 2d array with string values that are formatted based on the maximum length of the character
    private String[][] getFormatedTable(int[] maxLengths) {

        // Initialize an empty 2d array with one row more than the rows added. This will be for the column labels
        String[][] table = new String[rows.size() + 1][columnNames.length];

        // Add the column labels to the 2d array
        for (int i = 0; i < columnNames.length; i++) {

            // Set the element from having null value to an empty string
            table[0][i] = "";

            // Set alignment based on selection and spaces if the character length is less than max character length
            if (!alignToRight)
                table[0][i] += columnNames[i];

            for (int k = 0; k < (maxLengths[i] - columnNames[i].length()); k++)
                table[0][i] += " ";

            if (alignToRight)
                table[0][i] += columnNames[i];
        }

        // Set alignment and add the rows to the 2d array
        for (int i = 0; i < columnNames.length; i++) {

            for (int j = 0; j < rows.size(); j++) {
                if (rows.get(j) != null) {
                    table[j + 1][i] = "";

                    if (!alignToRight)
                        table[j + 1][i] += rows.get(j)[i];

                    for (int k = 0; k < (maxLengths[i] - rows.get(j)[i].length()); k++)
                        table[j + 1][i] += " ";

                    if (alignToRight)
                        table[j + 1][i] += rows.get(j)[i];
                }
            }
        }
        return table;
    }

    // Function to trim the input array
    private String[] stringsArrayTrimmer(String[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }
}

// Custom error for minimum value
class MinimumValueError extends Exception {
    MinimumValueError() {
        super("Minimum value not reached");
    }

    MinimumValueError(String message) {
        super(message);
    }
}