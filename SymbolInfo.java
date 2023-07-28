/**************************************************
*  class used to hold information associated w/
*  Symbs (which are stored in SymbolTables)
*
****************************************************/
import java.util.*; 

class SymbolInfo extends Symb {
 public Kinds kind;
 public Types type;
 public ArrayList<SymbolInfo> list;
 public int size;
 public int  varIndex;
 public String headOfLoop;
 public String loopExit;

 public SymbolInfo(String id, Kinds k, Types t){
	super(id);
	kind = k; type = t;
 };

 public String toString(){
             return "("+getName()+": kind=" + kind+ ", type="+  type+")";};
}

