// Shivan Kaul Sahib
package pt.ulisboa.tecnico.cnv.solver;

import java.util.*;


public class SudokuSolverDLX extends AbstractSudokuSolver
{

 /* The runSolver() method should remove all the unknown characters ('x') in the solution
  * and replace them with the numbers from 1-9 that satisfy the SudokuSolverDLX puzzle. */


 /************************************************************************************************************************************************************/
 // DOCUMENTATION -- ALGORITHM X, EXACT COVER PROBLEM AND DANCING LINKS IMPLEMENTATION

 // My class AlgorithmXSolver takes an unsolved SudokuSolverDLX puzzled as int[][] (the solution) and outputs the solved SudokuSolverDLX puzzle.
 // I convert the SudokuSolverDLX puzzle into an Exact Cover problem, runSolver that using the Dancing Links algorithm as described by Dr Donald Knuth,
 // and then get the solution and map it onto the solution.

 // EXACT COVER AND DANCING LINKS
 /*An Exact Cover problem can be represented as a sparse matrix where the rows represent possibilities, and the columns
  * represent constraints. Every row will have a 1 in every column (constraint) that it satisfies, and a 0 otherwise. A set
  * of rows that together have exactly one 1 for each column can be said to be the solution set of the Exact Cover problem. Now,
  * Dancing Links is an efficient way of solving such a problem. The idea is to take the Exact Cover matrix and put it into a
  * a toroidal circular doubly-linked list. Thus, every node in such a list will be connected to 4 other nodes and the list will be circular
  * i.e. the last element will point to the first one. In the case of Dancing Links, for every column of the linked list, there is a
  * special ColumnNode (which extends the normal Node) that contains identifying information about that particular column as well as
  * the size of the column i.e. the number of nodes in it. Each Node points to four other nodes as mentioned, as well as its ColumnNode.
  *
   // SOLVING
  * To runSolver the Exact Cover problem i.e. come up with a set of rows that contain exactly one 1 for every column/constraint, we search
  * recursively using the principles of backtracking. It chooses a column, 'covers' it i.e. removes that column from the linked list completely,
  * store it in a solution list (which I implemented using an ArrayList), and then try to recursively runSolver the rest of the table. If
  * it's not possible, backtrack, restore the column (uncover it), and try a different column. For this assignment I assumed that the
  * SudokuSolverDLX problem being provided has a solution.
  *
   // SudokuSolverDLX APPLICATION
  * For SudokuSolverDLX, there are 4 constraints. Only 1 instance of a number can be in a row, in a column, and in a block. In addition, there can
  * be only one number in a cell. The rows represent every single possible position for every number. Every row would have 4 1s, representing
  * one possible place for the number (satisfying all 4 constraints).
  * To implement my solution, I created a class AlgorithmXSolver that contained all the methods and the data structures required to runSolver
  * the problem. I instantiated an instance of this class in the runSolver() method, and then ran it.
  * I had to convert the given solution into a sparse matrix, accounting for the given clues (filled in values). Then, this matrix
  * is converted into a linked list as talked about above and solved using the Dancing Links approach. We store
  * possible solutions in an ArrayList 'solution'. Once we get a set of Nodes that solves the problem, we take the solution list
  * and iterate over every single Node and map the solution over the original solution.
  *
  // TESTING
  * I tested my solver using the puzzles provided by Prof Blanchette by passing the SudokuSolverDLX text file as the args[] variable
  * of the main method. I did this in Eclipse by editing the Run Configuration (and providing the full path to the text file
  * in the Arguments tab).
  */

 // CREDITS:
 // (1) Dr Donald Knuth's original paper (http://www.ocf.berkeley.edu/~jchu/publicportal/SudokuSolverDLX/0011047.pdf) on Dancing Links
 // (2) Jonathan Chu's paper for the pseudocode for the Dancing Links implementation (http://www.ocf.berkeley.edu/~jchu/publicportal/SudokuSolverDLX/SudokuSolverDLX.paper.html)
 // (3) The Wikipedia pages on Dancing Links, Exact Cover problem, Algorithm X for helping to understand Knuth's paper
 // (4) This StackOverflow discussion to intuitively understand the Dancing Links implementation: http://stackoverflow.com/questions/1518335/the-dancing-links-algorithm-an-explanation-that-is-less-explanatory-but-more-o
 // (5) Xi Chen's implementation in C to get an understanding of the data structures (http://uaa.wtf.im/?page_id=27)
 // (6) Alex Rudnick's implementation in Python for getting ideas on how to implement some of the methods (https://code.google.com/p/narorumo/wiki/SudokuSolverDLX)
 /************************************************************************************************************************************************************/
 public boolean runSolver(Solver sol)
 {
  AlgorithmXSolver solver= new AlgorithmXSolver();
  solver.run(solution, sol);
  return true;
 }

 public class AlgorithmXSolver
 {
  private ColumnNode root = null; // this is the starting node of the linked list
  private ArrayList solution = new ArrayList(); // a raw Array List for dynamically storing the solutions. It slows things
  // down a bit, but this how I started and ran out of time before I could come up with a more efficient way to do it.

  // the run method. We pass the solution[][] as input
  private void run(int[][] initialMatrix, Solver sol)
  {
   byte[][] matrix = createMatrix(initialMatrix, sol); // create the sparse matrix. We use the type byte to speed things up. I tried using
   // using all the primitive types, expecting the same results in terms
   // of speed; the only performance boost should have been in terms of space.
   // Yet, there was a marked difference in the running times. Hence, I used byte[][] whenever possible.
   ColumnNode doubleLinkedList = createDoubleLinkedLists(matrix);   // create the circular doubly-linked toroidal list
   search(0, sol); // start the Dancing Links process of searching and covering and uncovering recursively
  }

  // data structures
  class Node  // we define a node that knows about four other nodes, as well as its column head
  {
   Node left;
   Node right;
   Node up;
   Node down;
   ColumnNode head;
  }

  class ColumnNode extends Node // a special kind of node that contains information about that column
  {
   int size = 0;
   ColumnID info;
  }

  class ColumnID  // helps us store information about that column
  {
   int constraint = -1;
   int number = -1;
   int position = -1;
  }

  // create a sparse matrix for solution
  private byte[][] createMatrix(int[][] initialMatrix, Solver sol)
  {
   int[][] clues = null; // stores the numbers that are already given on the board i.e. the 'clues'
   ArrayList cluesList = new ArrayList(); // the list used to get the clues. Because we use a raw ArrayList, we later have to cast to int[] before storing in clues
   int counter = 0;
   for(int r = 0; r < SIZE; r++) // iterates over the rows of solution
   {
    for(int c = 0; c < SIZE; c++) // iterates over the columns of solution
    {
     if(initialMatrix[r][c] > 0) // if the number on the solution is != 0 (the number is a clue and not a blank space to solved for), then store it
     {
      cluesList.add(new int[]{initialMatrix[r][c],r,c}); // store the number, the row number and the column number
      counter++;
     }else{
      sol.run();
     }
    }
   }
   clues = new int[counter][]; // store the clues once we've gotten them
   for(int i = 0; i < counter; i++)
   {
    clues[i] = (int[])cluesList.get(i);
   }

   // Now, we build our sparse matrix
   byte[][] matrix = new byte[SIZE * SIZE * SIZE][4* SIZE * SIZE];
   // The rows of our matrix represent all the possibilities, whereas the columns represent the constraints.
   // Hence, there are SIZE^3 rows (SIZE rows * SIZE columns * SIZE numbers), and SIZE^2 * 4 columns (SIZE rows * SIZE columns * 4 constraints)

   // iterate over all the possible digits d
   for(int d = 0; d < SIZE; d++)
   {
    // iterate over all the possible rows r
    for(int r = 0; r < SIZE; r++)
    {
     // iterator over all the possible columns c
     for(int c = 0; c < SIZE; c++)
     {
      if(!filled(d,r,c,clues)) // if the cell is not already filled
      {
       // this idea for this way of mapping the sparse matrix is taken from the Python implementation: https://code.google.com/p/narorumo/wiki/SudokuSolverDLX
       int rowIndex = c + (SIZE * r) + (SIZE * SIZE * d);
       // there are four 1s in each row, one for each constraint
       int blockIndex = ((c / BOX_SIZE) + ((r / BOX_SIZE) * BOX_SIZE));
       int colIndexRow = 3* SIZE *d+r;
       int colIndexCol = 3* SIZE *d+ SIZE +c;
       int colIndexBlock = 3* SIZE *d+2* SIZE +blockIndex;
       int colIndexSimple = 3* SIZE * SIZE +(c+ SIZE *r);
       // fill in the 1's
       matrix[rowIndex][colIndexRow] = 1;
       matrix[rowIndex][colIndexCol] = 1;
       matrix[rowIndex][colIndexBlock] = 1;
       matrix[rowIndex][colIndexSimple] = 1;
      }
     }
    }
   }
   return matrix;
  }

  // check if the cell to be filled is already filled with a digit. The idea for this is credited to Alex Rudnick as cited above
  private boolean filled(int digit, int row, int col, int[][] prefill) {
   boolean filled = false;
   if(prefill != null)
   {
    for(int i = 0; i < prefill.length; i++)
    {
     int d = prefill[i][0]-1;
     int r = prefill[i][1];
     int c = prefill[i][2];
     // calculate the block indices
     int blockStartIndexCol = (c/ BOX_SIZE)* BOX_SIZE;
     int blockEndIndexCol = blockStartIndexCol + BOX_SIZE;
     int blockStartIndexRow = (r/ BOX_SIZE)* BOX_SIZE;
     int blockEndIndexRow = blockStartIndexRow + BOX_SIZE;
     if(d != digit && row == r && col == c) {
      filled = true;
     } else if((d == digit) && (row == r || col == c) && !(row == r && col == c))
     {
      filled = true;
     } else if((d == digit) && (row > blockStartIndexRow) && (row < blockEndIndexRow) && (col > blockStartIndexCol) && (col < blockEndIndexCol) && !(row == r && col == c))
     {
      filled = true;
     }
    }
   }
   return filled;
  }


  // the method to convert the sparse matrix Exact Cover problem to a doubly-linked list, which will allow us to later
  // perform our Dancing Links magic.
  // Given that we have 4 constraints for SudokuSolverDLX, I created a new class ColumnID that is a property of all columns.
  // This ColumnID property contains the information about the constraint and allows us to identify which constraint position
  // we're on, as well as the row and the column and the digit
  // the first constraint is row constraint, the second is col, the third is block, and the fourth is cell.
  // Every constraint contains SIZE^2 columns for every cell
  // The idea for this is taken from Jonathan Chu's explanation (cited above)
  private ColumnNode createDoubleLinkedLists(byte[][] matrix)
  {
   root = new ColumnNode(); // the root is used as an entry-way to the linked list i.e. we access the list through the root
   // create the column heads
   ColumnNode curColumn = root;
   for(int col = 0; col < matrix[0].length; col++) // getting the column heads from the sparse matrix and filling in the information about the
   // constraints. We iterate for all the column heads, thus going through all the items in the first row of the sparse matrix
   {
    // We create the ColumnID that will store the information. We will later map this ID to the current curColumn
    ColumnID id = new ColumnID();
    if(col < 3* SIZE * SIZE)
    {
     // identifying the digit
     int digit = (col / (3* SIZE)) + 1;
     id.number = digit;
     // is it for a row, column or block?
     int index = col-(digit-1)*3* SIZE;
     if(index < SIZE)
     {
      id.constraint = 0; // we're in the row constraint
      id.position = index;
     } else if(index < 2* SIZE)
     {
      id.constraint = 1; // we're in the column constraint
      id.position = index- SIZE;
     } else
     {
      id.constraint = 2; // we're in the block constraint
      id.position = index-2* SIZE;
     }
    } else
    {
     id.constraint = 3; // we're in the cell constraint
     id.position = col - 3* SIZE * SIZE;
    }
    curColumn.right = new ColumnNode();
    curColumn.right.left = curColumn;
    curColumn = (ColumnNode)curColumn.right;
    curColumn.info = id; // the information about the column is set to the new column
    curColumn.head = curColumn;
   }
   curColumn.right = root; // making the list circular i.e. the right-most ColumnHead is linked to the root
   root.left = curColumn;

   // Once all the ColumnHeads are set, we iterate over the entire matrix
   // Iterate over all the rows
   for(int row = 0; row < matrix.length; row++)
   {
    // iterator over all the columns
    curColumn = (ColumnNode)root.right;
    Node lastCreatedElement = null;
    Node firstElement = null;
    for(int col = 0; col < matrix[row].length; col++) {
     if(matrix[row][col] == 1)  // i.e. if the sparse matrix element has a 1 i.e. there is a clue here i.e. we were given this value in the solution
     {
      // create a new data element and link it
      Node colElement = curColumn;
      while(colElement.down != null)
      {
       colElement = colElement.down;
      }
      colElement.down = new Node();
      if(firstElement == null) {
       firstElement = colElement.down;
      }
      colElement.down.up = colElement;
      colElement.down.left = lastCreatedElement;
      colElement.down.head = curColumn;
      if(lastCreatedElement != null)
      {
       colElement.down.left.right = colElement.down;
      }
      lastCreatedElement = colElement.down;
      curColumn.size++;
     }
     curColumn = (ColumnNode)curColumn.right;
    }
    // link the first and the last element, again making it circular
    if(lastCreatedElement != null)
    {
     lastCreatedElement.right = firstElement;
     firstElement.left = lastCreatedElement;
    }
   }
   curColumn = (ColumnNode)root.right;
   // link the last column elements with the corresponding columnHeads
   for(int i = 0; i < matrix[0].length; i++)
   {
    Node colElement = curColumn;
    while(colElement.down != null)
    {
     colElement = colElement.down;
    }
    colElement.down = curColumn;
    curColumn.up = colElement;
    curColumn = (ColumnNode)curColumn.right;
   }
   return root; // We've made the doubly-linked list; we return the root of the list
  }

  // the searching algorithm. Pseudo-code from Jonathan Chu's paper (cited above).
  private void search(int k, Solver sol)
  {
   if(root.right == root) // if we've run out of columns, we've solved the exact cover problem!
   {
    mapSolvedToGrid(); // map the solved linked list to the grid
    return;
   }
   ColumnNode c = choose(); // we choose a column to cover
   cover(c);
   Node r = c.down;
   while(r != c)
   {
    if(k < solution.size())
    {
     solution.remove(k); // if we had to enter this loop again
    }
    solution.add(k,r); // the solution is added

    Node j = r.right;
    while(j != r) {
     sol.run();
     cover(j.head);
     j = j.right;
    }
    search(k+1, sol); //recursively search

    Node r2 = (Node)solution.get(k);
    Node j2 = r2.left;
    while(j2 != r2) {
     uncover(j2.head);
     j2 = j2.left;
    }
    r = r.down;
   }
   uncover(c);
  }

  // this allows us to map the solved linked list to the solution
  private void mapSolvedToGrid()
  {
   int[] result = new int[SIZE * SIZE];
   for(Iterator it = solution.iterator(); it.hasNext();)  // we use Iterators to iterate over every single element of the ArrayList
   // we stop iterating once we run out of elements in the list
   {
    // for the first step, we pull all the values of the solved SudokuSolverDLX board from the linked list to an array result[] in order
    int number = -1; // initialize number and cell number to be a value that can't occur
    int cellNo = -1;
    Node element = (Node)it.next();
    Node next = element;
    do {
     if (next.head.info.constraint == 0)
     { // if we're in the row constraint
      number = next.head.info.number;
     }
     else if (next.head.info.constraint == 3)
     { // if we're in the cell constraint
      cellNo = next.head.info.position;
     }
     next = next.right;
    } while(element != next);
    result[cellNo] = number; // feed values into result[]
   }
   // for the second step, we feed all the values of the array result[] (in order) to the solution
   int resultCounter=0;
   for (int r = 0; r< SIZE; r++) // iterates for the rows
   {
    for (int c = 0; c< SIZE; c++) // iterates for the columns
    {
     SudokuSolverDLX.this.solution[r][c]=result[resultCounter];
     resultCounter++;
    }
   }
  }



  private ColumnNode choose() {
   // According to Donald Knuth's paper, it is most efficient to choose the column with the smallest possible size.
   // That is what we do.
   ColumnNode rightOfRoot = (ColumnNode)root.right; // we cast the node to the right of the root to be a ColumnNode
   ColumnNode smallest = rightOfRoot;
   while(rightOfRoot.right != root)
   {
    rightOfRoot = (ColumnNode)rightOfRoot.right;
    if(rightOfRoot.size < smallest.size) // choosing which column has the lowest size
    {
     smallest = rightOfRoot;
    }
   }
   return smallest;
  }

  // covers the column; used as a helper method for the search method. Pseudo code by Jonathan Chu (credited above)
  private void cover(Node column)
  {
   // we remove the column head by remapping the node to its left to the node to its right; thus, the linked list no longer contains
   // a way to access the column head. Later when we uncover it, we can easily do so by just reversing this process.
   column.right.left = column.left;
   column.left.right = column.right;

   // We also have to do this covering for all the rows in the column
   Node curRow = column.down;
   while(curRow != column) // because it's circular!
   {
    Node curNode = curRow.right;
    while(curNode != curRow)
    {
     curNode.down.up = curNode.up;
     curNode.up.down = curNode.down;
     curNode.head.size--;
     curNode = curNode.right;
    }
    curRow = curRow.down;
   }
  }

  // uncovers the column i.e. adds back all the nodes of the column to the linked list
  private void uncover(Node column)
  {
   Node curRow = column.up;
   while(curRow != column) // do this for all the nodes of the column to be uncovered first, and then reinsert the columnHead
   {
    Node curNode = curRow.left;
    while(curNode != curRow)
    {
     curNode.head.size++;
     curNode.down.up = curNode; // reinserts node into linked list
     curNode.up.down = curNode;
     curNode = curNode.left;
    }
    curRow = curRow.up;
   }
   column.right.left = column; // reinserts column head
   column.left.right = column;
  }

 }


 /*****************************************************************************/
 /* NOTE: YOU SHOULD NOT HAVE TO MODIFY ANY OF THE FUNCTIONS BELOW THIS LINE. */

 /*****************************************************************************/

 public SudokuSolverDLX() {
  super();
  name = "Dancing Links Solver";
 }


 /* Default constructor.  This will initialize all positions to the default 0
  * value.  Use the read() function to load the SudokuSolverDLX puzzle from a file or
  * the standard input. */
 public SudokuSolverDLX( int[][] puzzle)
 {
  super(puzzle);
  name = "Dancing Links Solver";

 }
}
