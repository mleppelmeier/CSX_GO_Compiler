import java.io.*;
import java.util.*;

/**
 * Mallory Leppelmeier
 * 8/23/2020
 * Defines SymbolTable Object
 */
class SymbolTable {
   List<Map<Object,Object>> symbolTable;

   /**
    * Creates SymbolTable
    */
   SymbolTable() {
      symbolTable = new ArrayList<Map<Object,Object>>();
   }

   /**
    * Opens a new scope at front of symbolTable
    */
   public void openScope() {
      Map<Object, Object> scope = new HashMap<Object, Object>();
      symbolTable.add(0,scope);
   }

   /**
    * Closes top scope
    * @throws EmptySTException Stack is empty
    */
   public void closeScope() throws EmptySTException {
      if(symbolTable.isEmpty()){
         throw new EmptySTException();
      }else{
         symbolTable.remove(0);
      }
   }

   /**
    * Inserts symb in current scope
    * @param symb The symb to inset
    * @throws EmptySTException Stack is empty
    * @throws DuplicateException Symb already exists in scope
    */
   public void insert(Symb symb) throws EmptySTException, DuplicateException {
      if(symbolTable.isEmpty()) {
         throw new EmptySTException();
      }
      Map<Object, Object> map = symbolTable.get(0);

      if(map.containsKey(symb.getName())){
         throw new DuplicateException();
      }

      map.put(symb.getName(),symb);
   }

   /**
    * Look for Symb with key name in top scope
    * @param name Name of the Symb
    * @return Symb found in local scope
    */
   public Symb localLookup(String name) {
      if(symbolTable.isEmpty()) {
         return null;
      }else if(symbolTable.get(0).containsKey(name)){
         return (Symb)symbolTable.get(0).get(name);
      }
      return null;
   }


   /**
    * Look for Symb with key name in all scopes starting with top scope.
    *  @param name Name of the Symb
    *  @return First Symb found in scopes
    */
   public Symb globalLookup(String name) {
      for(Map<Object, Object> map: symbolTable){
         if(map.containsKey(name)){
            return (Symb)map.get(name);
         }
      }
     return null;
   }

   /**
    * Returns string representation of symbol table
    */
   public String toString() {
      return "SymbolTable{" +
              "symbolTable=" + symbolTable +
              '}';
   }

   /**
    * Prints all contents of SymbolTable
    * @param ps The PrintStream to print content.
    */
   void dump(PrintStream ps) {
      if(symbolTable.isEmpty()){
         ps.println("The symbolTable is Empty");
      }else {
         ps.println("Contents of symbol table:");
         for (Map<Object, Object> map : symbolTable) {
            ps.print("{ ");
            for (Object symbol : map.values()) {
               ps.print(symbol.toString());
            }
            ps.println(" }");
         }
      }
   }
} // class SymbolTable

